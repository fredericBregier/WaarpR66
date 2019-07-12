/**
 * This file is part of Waarp Project.
 * <p>
 * Copyright 2009, Frederic Bregier, and individual contributors by the @author tags. See the COPYRIGHT.txt in the
 * distribution for a full listing of individual contributors.
 * <p>
 * All Waarp Project is free software: you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * <p>
 * Waarp is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with Waarp. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package org.waarp.openr66.protocol.localhandler;

import io.netty.channel.Channel;
import io.netty.channel.local.LocalChannel;
import io.netty.handler.traffic.ChannelTrafficShapingHandler;
import org.waarp.common.database.DbSession;
import org.waarp.common.database.exception.WaarpDatabaseNoConnectionException;
import org.waarp.common.logging.WaarpLogger;
import org.waarp.common.logging.WaarpLoggerFactory;
import org.waarp.openr66.client.RecvThroughHandler;
import org.waarp.openr66.commander.ClientRunner;
import org.waarp.openr66.context.ErrorCode;
import org.waarp.openr66.context.R66FiniteDualStates;
import org.waarp.openr66.context.R66Result;
import org.waarp.openr66.context.R66Session;
import org.waarp.openr66.context.task.exception.OpenR66RunnerErrorException;
import org.waarp.openr66.database.DbConstant;
import org.waarp.openr66.database.data.DbTaskRunner;
import org.waarp.openr66.protocol.configuration.Configuration;
import org.waarp.openr66.protocol.configuration.PartnerConfiguration;
import org.waarp.openr66.protocol.exception.OpenR66Exception;
import org.waarp.openr66.protocol.exception.OpenR66ProtocolNoConnectionException;
import org.waarp.openr66.protocol.exception.OpenR66ProtocolRemoteShutdownException;
import org.waarp.openr66.protocol.networkhandler.NetworkChannelReference;
import org.waarp.openr66.protocol.networkhandler.NetworkServerHandler;
import org.waarp.openr66.protocol.networkhandler.NetworkServerInitializer;
import org.waarp.openr66.protocol.networkhandler.NetworkTransaction;
import org.waarp.openr66.protocol.utils.R66Future;
import org.waarp.openr66.protocol.utils.R66Versions;

/**
 * Reference of one object using Local Channel localId and containing local channel and network channel.
 *
 * @author Frederic Bregier
 */
public class LocalChannelReference {
    /**
     * Internal Logger
     */
    private static final WaarpLogger logger = WaarpLoggerFactory.getLogger(LocalChannelReference.class);

    /**
     * Local Channel
     */
    private final LocalChannel localChannel;

    /**
     * Network Channel Ref
     */
    private final NetworkChannelReference networkChannelRef;
    /**
     * Traffic handler associated if any
     */
    private final ChannelTrafficShapingHandler cts;

    /**
     * Network Server Handler
     */
    private final NetworkServerHandler networkServerHandler;

    /**
     * Local Id
     */
    private final Integer localId;
    /**
     * Future on Request
     */
    private final R66Future futureRequest;
    /**
     * Future on Valid Starting Request
     */
    private final R66Future futureValidRequest = new R66Future(true);
    /**
     * Future on Connection
     */
    private final R66Future futureConnection = new R66Future(true);
    /**
     * Future on Startup
     */
    private final R66Future futureStartup = new R66Future(true);
    /**
     * Remote Id
     */
    private Integer remoteId;
    /**
     * Requested_requester_specialId
     */
    private String requestId;
    /**
     * Future on Transfer
     */
    private R66Future futureEndTransfer = new R66Future(true);
    /**
     * Session
     */
    private R66Session session;

    /**
     * Last error message
     */
    private String errorMessage = "NoError";

    /**
     * Last error code
     */
    private ErrorCode code = ErrorCode.Unknown;

    /**
     * RecvThroughHandler
     */
    private RecvThroughHandler recvThroughHandler;

    private boolean isSendThroughMode = false;
    /**
     * Thread for ClientRunner if any
     */
    private ClientRunner clientRunner = null;

    /**
     * To be able to check hash once all transfer is over once again
     */
    private String hashComputeDuringTransfer = null;
    /**
     * If partial hash, no global hash validation can be done
     */
    private boolean partialHash = false;

    /**
     * PartnerConfiguration
     */
    private volatile PartnerConfiguration partner;
    /**
     * DbSession for Database that do not support concurrency in access
     */
    private volatile DbSession noconcurrencyDbSession = null;

    /**
     * @param localChannel
     * @param networkChannelRef
     * @param remoteId
     * @param futureRequest
     *
     * @throws OpenR66ProtocolRemoteShutdownException
     */
    public LocalChannelReference(LocalChannel localChannel, NetworkChannelReference networkChannelRef,
                                 Integer remoteId, R66Future futureRequest)
            throws OpenR66ProtocolRemoteShutdownException {
        this.localChannel = localChannel;
        this.networkChannelRef = networkChannelRef;
        networkServerHandler = (NetworkServerHandler) this.networkChannelRef.channel().pipeline().last();
        localId = this.localChannel.id().hashCode();
        this.remoteId = remoteId;
        if (futureRequest == null) {
            this.futureRequest = new R66Future(true);
        } else {
            if (futureRequest.isDone()) {
                futureRequest.reset();
            }
            this.futureRequest = futureRequest;
        }
        cts = (ChannelTrafficShapingHandler) networkChannelRef.channel().pipeline()
                                                              .get(NetworkServerInitializer.LIMITCHANNEL);
        if (DbConstant.admin.isActive()) {
            try {
                this.noconcurrencyDbSession = new DbSession(DbConstant.admin, false);
                logger.info("LocalChannel {}, will use DB session {}",
                            localId, this.noconcurrencyDbSession.getInternalId());
            } catch (WaarpDatabaseNoConnectionException e) {
                // Cannot connect so use default connection
                logger.warn("Use default database connection");
                this.noconcurrencyDbSession = null;
            }
        } else {
            logger.warn("DbAdmin is not active");
            this.noconcurrencyDbSession = null;
        }
        networkChannelRef.add(this);
    }

    /**
     * Special empty LCR constructor
     */
    public LocalChannelReference() {
        this.localChannel = null;
        this.networkChannelRef = null;
        networkServerHandler = null;
        localId = 0;
        this.futureRequest = new R66Future(true);
        cts = null;
    }

    /**
     * Close the localChannelReference
     */
    public void close() {
        Configuration.configuration.getLocalTransaction().remove(this);
        // Now force the close of the database after a wait
        if (noconcurrencyDbSession != null && DbConstant.admin != null && DbConstant.admin.getSession() != null
            && !noconcurrencyDbSession.equals(DbConstant.admin.getSession())) {
            noconcurrencyDbSession.forceDisconnect();
            noconcurrencyDbSession = null;
        }
    }

    /**
     * @return the localChannel
     */
    public LocalChannel getLocalChannel() {
        return localChannel;
    }

    /**
     * @return the networkChannelRef
     */
    public Channel getNetworkChannel() {
        return networkChannelRef.channel();
    }

    /**
     * @return the id
     */
    public Integer getLocalId() {
        return localId;
    }

    /**
     * @return the remoteId
     */
    public Integer getRemoteId() {
        return remoteId;
    }

    /**
     * @param remoteId the remoteId to set
     */
    public void setRemoteId(Integer remoteId) {
        this.remoteId = remoteId;
    }

    /**
     * @return the ChannelTrafficShapingHandler
     */
    public ChannelTrafficShapingHandler getChannelTrafficShapingHandler() {
        return cts;
    }

    /**
     * @return the networkChannelObject
     */
    public NetworkChannelReference getNetworkChannelObject() {
        return networkChannelRef;
    }

    /**
     * @return the networkServerHandler
     */
    public NetworkServerHandler getNetworkServerHandler() {
        return networkServerHandler;
    }

    /**
     * @return the actual dbSession
     */
    public DbSession getDbSession() {
        if (noconcurrencyDbSession != null) {
            return noconcurrencyDbSession;
        }
        if (networkServerHandler != null) {
            return networkServerHandler.getDbSession();
        }
        logger.info("SHOULD NOT BE");
        return DbConstant.admin.getSession();
    }

    /**
     * @return the session
     */
    public R66Session getSession() {
        return session;
    }

    /**
     * @param session the session to set
     */
    public void setSession(R66Session session) {
        this.session = session;
    }

    /**
     * @return the current errorMessage
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * @param errorMessage the errorMessage to set
     */
    public void setErrorMessage(String errorMessage, ErrorCode code) {
        this.errorMessage = errorMessage;
        this.code = code;
    }

    /**
     * @return the code
     */
    public ErrorCode getCurrentCode() {
        return code;
    }

    /**
     * Validate or not the Startup (before connection)
     *
     * @param validate
     */
    public void validateStartup(boolean validate) {
        if (futureStartup.isDone()) {
            return;
        }
        if (validate) {
            futureStartup.setSuccess();
        } else {
            futureStartup.cancel();
        }
    }

    /**
     * @return the futureValidateStartup
     */
    public R66Future getFutureValidateStartup() {
        try {
            if (!futureStartup.await(Configuration.configuration.getTIMEOUTCON())) {
                validateStartup(false);
                return futureStartup;
            }
        } catch (InterruptedException e) {
            validateStartup(false);
            return futureStartup;
        }
        return futureStartup;
    }

    /**
     * @return True if the connection is validated (in OK or KO status)
     */
    public boolean isConnectionValidate() {
        return futureConnection.isDone();
    }

    /**
     * Validate or Invalidate the connection (authentication)
     *
     * @param validate
     */
    public void validateConnection(boolean validate, R66Result result) {
        if (futureConnection.isDone()) {
            logger.debug("LocalChannelReference already validated: " +
                         futureConnection.isSuccess());
            return;
        }
        if (validate) {
            futureConnection.setResult(result);
            futureConnection.setSuccess();
        } else {
            futureConnection.setResult(result);
            setErrorMessage(result.getMessage(), result.getCode());
            futureConnection.cancel();
        }
    }

    /**
     * @return the futureValidateConnection
     */
    public R66Future getFutureValidateConnection() {
        R66Result result;
        try {
            for (int i = 0; i < Configuration.RETRYNB; i++) {
                Channel channel = this.networkChannelRef.channel();
                if (channel != null && channel.isActive()) {
                    if (!futureConnection.await(Configuration.configuration.getTIMEOUTCON())) {
                        if (futureConnection.isDone()) {
                            return futureConnection;
                        } else {
                            if (channel.isActive()) {
                                continue;
                            }
                            result = new R66Result(
                                    new OpenR66ProtocolNoConnectionException(
                                            "Out of time"), session, false,
                                    ErrorCode.ConnectionImpossible, null);
                            validateConnection(false, result);
                            return futureConnection;
                        }
                    } else {
                        return futureConnection;
                    }
                } else {
                    break;
                }
            }
        } catch (InterruptedException e) {
            result = new R66Result(
                    new OpenR66ProtocolNoConnectionException(
                            "Interrupted connection"), session, false,
                    ErrorCode.ConnectionImpossible, null);
            validateConnection(false, result);
            return futureConnection;
        }
        logger.info("Cannot get Connection due to out of Time: {}", this);
        result = new R66Result(
                new OpenR66ProtocolNoConnectionException(
                        "Out of time"), session, false,
                ErrorCode.ConnectionImpossible, null);
        validateConnection(false, result);
        return futureConnection;
    }

    /**
     * Validate the End of a Transfer
     *
     * @param finalValue
     */
    public void validateEndTransfer(R66Result finalValue) {
        if (!futureEndTransfer.isDone()) {
            futureEndTransfer.setResult(finalValue);
            futureEndTransfer.setSuccess();
        } else {
            logger.debug("Could not validate since Already validated: " +
                         futureEndTransfer.isSuccess() + " " + finalValue);
            if (!futureEndTransfer.getResult().isAnswered()) {
                futureEndTransfer.getResult().setAnswered(finalValue.isAnswered());
            }
        }
    }

    /**
     * @return the futureEndTransfer
     */
    public R66Future getFutureEndTransfer() {
        return futureEndTransfer;
    }

    /**
     * Special waiter for Send Through method. It reset the EndTransfer future.
     *
     * @throws OpenR66Exception
     */
    public void waitReadyForSendThrough() throws OpenR66Exception {
        logger.debug("Wait for End of Prepare Transfer");
        try {
            this.futureEndTransfer.await();
        } catch (InterruptedException e) {
            throw new OpenR66RunnerErrorException("Interrupted", e);
        }
        if (this.futureEndTransfer.isSuccess()) {
            // reset since transfer will start now
            this.futureEndTransfer = new R66Future(true);
        } else {
            throw this.futureEndTransfer.getResult().getException();
        }
    }

    /**
     * @return the futureValidRequest
     */
    public R66Future getFutureValidRequest() {
        return futureValidRequest;
    }

    /**
     * @return the futureRequest
     */
    public R66Future getFutureRequest() {
        return futureRequest;
    }

    /**
     * Invalidate the current request
     *
     * @param finalvalue
     */
    public void invalidateRequest(R66Result finalvalue) {
        R66Result finalValue = finalvalue;
        if (finalValue == null) {
            finalValue = new R66Result(session, false, ErrorCode.Unknown, this.session.getRunner());
        }
        logger.debug("FET: " + futureEndTransfer.isDone() + ":" +
                     futureEndTransfer.isSuccess() + " FVR: " +
                     futureValidRequest.isDone() + ":" +
                     futureValidRequest.isSuccess() + " FR: " +
                     futureRequest.isDone() + ":" + futureRequest.isSuccess() + " " +
                     finalValue.getMessage());
        if (!futureEndTransfer.isDone()) {
            futureEndTransfer.setResult(finalValue);
            if (finalValue.getException() != null) {
                futureEndTransfer.setFailure(finalValue.getException());
            } else {
                futureEndTransfer.cancel();
            }
        }
        if (!futureValidRequest.isDone()) {
            futureValidRequest.setResult(finalValue);
            if (finalValue.getException() != null) {
                futureValidRequest.setFailure(finalValue.getException());
            } else {
                futureValidRequest.cancel();
            }
        }
        logger.debug("Invalidate Request", new Exception(
                "Trace for Invalidation"));
        if (finalValue.getCode() != ErrorCode.ServerOverloaded) {
            if (!futureRequest.isDone()) {
                setErrorMessage(finalValue.getMessage(), finalValue.getCode());
                futureRequest.setResult(finalValue);
                if (finalValue.getException() != null) {
                    futureRequest.setFailure(finalValue.getException());
                } else {
                    futureRequest.cancel();
                }
            } else {
                logger.debug("Could not invalidate since Already finished: " +
                             futureEndTransfer.getResult());
            }
        } else {
            setErrorMessage(finalValue.getMessage(), finalValue.getCode());
            logger.debug("Overloaded");
        }
        if (this.session != null) {
            DbTaskRunner runner = this.session.getRunner();
            if (runner != null) {
                if (runner.isSender()) {
                    NetworkTransaction.stopRetrieve(this);
                }
            }
        }
    }

    /**
     * Validate the current Request
     *
     * @param finalValue
     */
    public void validateRequest(R66Result finalValue) {
        setErrorMessage("NoError", null);
        if (!futureEndTransfer.isDone()) {
            logger.debug("Will validate EndTransfer");
            validateEndTransfer(finalValue);
        }
        if (!futureValidRequest.isDone()) {
            futureValidRequest.setResult(finalValue);
            futureValidRequest.setSuccess();
        }
        logger.debug("Validate Request");
        if (!futureRequest.isDone()) {
            if (finalValue.getOther() == null &&
                session.getBusinessObject() != null &&
                session.getBusinessObject().getInfo(session) != null) {
                finalValue.setOther(session.getBusinessObject().getInfo(session));
            }
            futureRequest.setResult(finalValue);
            futureRequest.setSuccess();
        } else {
            logger.info("Already validated: " + futureRequest.isSuccess() +
                        " " + finalValue);
            if (!futureRequest.getResult().isAnswered()) {
                futureRequest.getResult().setAnswered(finalValue.isAnswered());
            }
        }
    }

    private long getMinLimit(long a, long b) {
        long res = a;
        if (a <= 0) {
            res = b;
        } else if (b > 0 && b < a) {
            res = b;
        }
        return res;
    }

    public void setChannelLimit(boolean isSender, long limit) {
        ChannelTrafficShapingHandler limitHandler =
                (ChannelTrafficShapingHandler) networkChannelRef.channel()
                                                                .pipeline().get("CHANNELLIMIT");
        if (isSender) {
            limitHandler.setWriteLimit(limit);
            logger.info("Will write at {} Bytes/sec", limit);
        } else {
            limitHandler.setReadLimit(limit);
            logger.info("Will read at {} Bytes/sec", limit);
        }
    }

    public long getChannelLimit(boolean isSender) {
        long global = 0;
        long channel = 0;
        if (isSender) {
            global = Configuration.configuration.getServerGlobalWriteLimit();
            channel = Configuration.configuration.getServerChannelWriteLimit();
        } else {
            global = Configuration.configuration.getServerGlobalReadLimit();
            channel = Configuration.configuration.getServerChannelReadLimit();
        }
        return getMinLimit(global, channel);
    }

    @Override
    public String toString() {
        return "LCR: L: " + localId + " R: " + remoteId + " Startup[" +
               (futureStartup != null? futureStartup : "noStartup") + "] Conn[" +
               (futureConnection != null? futureConnection : "noConn")
               + "] ValidRequestRequest[" +
               (futureValidRequest != null? futureValidRequest : "noValidRequest")
               + "] EndTransfer[" +
               (futureEndTransfer != null? futureEndTransfer : "noEndTransfer") + "] Request[" +
               (futureRequest != null? futureRequest : "noRequest") + "]";
    }

    /**
     * @return the recvThroughHandler
     */
    public RecvThroughHandler getRecvThroughHandler() {
        return recvThroughHandler;
    }

    /**
     * @param recvThroughHandler the recvThroughHandler to set
     */
    public void setRecvThroughHandler(RecvThroughHandler recvThroughHandler) {
        this.recvThroughHandler = recvThroughHandler;
    }

    /**
     * @return True if in RecvThrough Mode
     */
    public boolean isRecvThroughMode() {
        return recvThroughHandler != null;
    }

    /**
     * @return True if in SendThrough Mode
     */
    public boolean isSendThroughMode() {
        return isSendThroughMode;
    }

    /**
     * @param isSendThroughMode the isSendThroughMode to set
     */
    public void setSendThroughMode(boolean isSendThroughMode) {
        this.isSendThroughMode = isSendThroughMode;
    }

    /**
     * @return the clientRunner
     */
    public ClientRunner getClientRunner() {
        return clientRunner;
    }

    /**
     * @param clientRunner the clientRunner to set
     */
    public void setClientRunner(ClientRunner clientRunner) {
        this.clientRunner = clientRunner;
    }

    /**
     * Shortcut to set a new state in Session
     *
     * @param desiredState
     */
    public void sessionNewState(R66FiniteDualStates desiredState) {
        if (session != null) {
            session.newState(desiredState);
        }
    }

    /**
     * @return the current state or TEST if no session exists
     */
    public R66FiniteDualStates getSessionState() {
        if (session != null) {
            return session.getState();
        }
        return R66FiniteDualStates.TEST;
    }

    /**
     * @return the hashComputeDuringTransfer
     */
    public String getHashComputeDuringTransfer() {
        return hashComputeDuringTransfer;
    }

    /**
     * @param hashComputeDuringTransfer the hashComputeDuringTransfer to set
     */
    public void setHashComputeDuringTransfer(String hashComputeDuringTransfer) {
        this.hashComputeDuringTransfer = hashComputeDuringTransfer;
    }

    public void setPartialHash() {
        this.partialHash = true;
    }

    public boolean isPartialHash() {
        return this.partialHash;
    }

    /**
     * @return the partner
     */
    public PartnerConfiguration getPartner() {
        return partner;
    }

    /**
     * @param hostId the partner to set
     */
    public void setPartner(String hostId) {
        logger.debug("host:" + hostId);
        partner = Configuration.configuration.getVersions().get(hostId);
        if (partner == null) {
            partner = new PartnerConfiguration(hostId, R66Versions.V2_4_12.getVersion());
        }
    }

    /**
     * @return the requestId
     */
    public String getRequestId() {
        return requestId;
    }

    /**
     * @param requestId the requestId to set
     */
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

}
