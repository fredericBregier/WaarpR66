/**
   This file is part of GoldenGate Project (named also GoldenGate or GG).

   Copyright 2009, Frederic Bregier, and individual contributors by the @author
   tags. See the COPYRIGHT.txt in the distribution for a full listing of
   individual contributors.

   All GoldenGate Project is free software: you can redistribute it and/or 
   modify it under the terms of the GNU General Public License as published 
   by the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   GoldenGate is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with GoldenGate .  If not, see <http://www.gnu.org/licenses/>.
 */
package openr66.protocol.exception;

import goldengate.common.logging.GgInternalLogger;
import goldengate.common.logging.GgInternalLoggerFactory;

import java.io.IOException;
import java.net.BindException;
import java.net.ConnectException;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedChannelException;
import java.util.concurrent.RejectedExecutionException;

import javax.net.ssl.SSLException;

import openr66.protocol.configuration.Configuration;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelException;
import org.jboss.netty.channel.ExceptionEvent;

/**
 * Class that filter exceptions
 *
 * @author frederic bregier
 */
public class OpenR66ExceptionTrappedFactory {
    /**
     * Internal Logger
     */
    private static final GgInternalLogger logger = GgInternalLoggerFactory
            .getLogger(OpenR66ExceptionTrappedFactory.class);

    /**
     * @param channel
     * @param e
     * @return the OpenR66Exception corresponding to the ExceptionEvent, or null
     *         if the exception should be ignored
     */
    public static OpenR66Exception getExceptionFromTrappedException(
            Channel channel, ExceptionEvent e) {
        final Throwable e1 = e.getCause();
        if (e1 instanceof ConnectException) {
            final ConnectException e2 = (ConnectException) e1;
            logger.debug("Connection impossible since {} with Channel {}", e2
                    .getMessage(), channel);
            return new OpenR66ProtocolNoConnectionException(
                    "Connection impossible", e2);
        } else if (e1 instanceof ChannelException) {
            final ChannelException e2 = (ChannelException) e1;
            logger
                    .info(
                            "Connection (example: timeout) impossible since {} with Channel {}",
                            e2.getMessage(), channel);
            return new OpenR66ProtocolNetworkException(
                    "Connection (example: timeout) impossible", e2);
        } else if (e1 instanceof CancelledKeyException) {
            final CancelledKeyException e2 = (CancelledKeyException) e1;
            logger.error("Connection aborted since {}", e2.getMessage());
            // Is it really what we should do ?
            // Yes, No action
            return null;
        } else if (e1 instanceof ClosedChannelException) {
            logger.debug("Connection closed before end");
            return new OpenR66ProtocolBusinessNoWriteBackException(
                    "Connection closed before end", e1);
        } else if (e1 instanceof OpenR66ProtocolBusinessCancelException) {
            final OpenR66ProtocolBusinessCancelException e2 = (OpenR66ProtocolBusinessCancelException) e1;
            logger.debug("Request is canceled: {}", e2.getMessage());
            return e2;
        } else if (e1 instanceof OpenR66ProtocolBusinessStopException) {
            final OpenR66ProtocolBusinessStopException e2 = (OpenR66ProtocolBusinessStopException) e1;
            logger.debug("Request is stopped: {}", e2.getMessage());
            return e2;
        } else if (e1 instanceof OpenR66ProtocolBusinessQueryAlreadyFinishedException) {
            final OpenR66ProtocolBusinessQueryAlreadyFinishedException e2 =
                (OpenR66ProtocolBusinessQueryAlreadyFinishedException) e1;
            logger.debug("Request is already finished: {}", e2.getMessage());
            return e2;
        } else if (e1 instanceof OpenR66ProtocolBusinessQueryStillRunningException) {
            final OpenR66ProtocolBusinessQueryStillRunningException e2 =
                (OpenR66ProtocolBusinessQueryStillRunningException) e1;
            logger.debug("Request is still running: {}", e2.getMessage());
            return e2;
        } else if (e1 instanceof OpenR66ProtocolBusinessRemoteFileNotFoundException) {
            final OpenR66ProtocolBusinessRemoteFileNotFoundException e2 =
                (OpenR66ProtocolBusinessRemoteFileNotFoundException) e1;
            logger.debug("Remote server did not find file: {}", e2.getMessage());
            return e2;
        } else if (e1 instanceof OpenR66ProtocolBusinessNoWriteBackException) {
            final OpenR66ProtocolBusinessNoWriteBackException e2 = (OpenR66ProtocolBusinessNoWriteBackException) e1;
            logger.error("Command Error Reply: {}", e2.getMessage());
            return e2;
        } else if (e1 instanceof OpenR66ProtocolShutdownException) {
            final OpenR66ProtocolShutdownException e2 = (OpenR66ProtocolShutdownException) e1;
            logger.debug("Command Shutdown {}", e2.getMessage());
            return e2;
        } else if (e1 instanceof OpenR66Exception) {
            final OpenR66Exception e2 = (OpenR66Exception) e1;
            logger.debug("Command Error Reply: {}", e2.getMessage());
            return e2;
        } else if (e1 instanceof BindException) {
            final BindException e2 = (BindException) e1;
            logger.debug("Address already in use {}", e2.getMessage());
            return new OpenR66ProtocolNetworkException(
                    "Address already in use", e2);
        } else if (e1 instanceof ConnectException) {
            final ConnectException e2 = (ConnectException) e1;
            logger.debug("Timeout occurs {}", e2.getMessage());
            return new OpenR66ProtocolNetworkException("Timeout occurs", e2);
        } else if (e1 instanceof NullPointerException) {
            final NullPointerException e2 = (NullPointerException) e1;
            logger.error("Null pointer Exception", e2);
            return new OpenR66ProtocolSystemException("Null Pointer Exception",
                    e2);
        } else if (e1 instanceof SSLException) {
            final SSLException e2 = (SSLException) e1;
            logger.error("Connection aborted since SSL Error {} with Channel {}", e2
                    .getMessage(), channel);
            return new OpenR66ProtocolBusinessNoWriteBackException("SSL Connection aborted", e2);
        } else if (e1 instanceof IOException) {
            final IOException e2 = (IOException) e1;
            logger.debug("Connection aborted since {} with Channel {}", e2
                    .getMessage(), channel);
            if (channel.isConnected()) {
                return new OpenR66ProtocolSystemException("Connection aborted", e2);
            } else {
                return new OpenR66ProtocolBusinessNoWriteBackException("Connection aborted", e2);
            }
        } else if (e1 instanceof RejectedExecutionException) {
            final RejectedExecutionException e2 = (RejectedExecutionException) e1;
            logger.debug("Connection aborted since {} with Channel {}", e2
                    .getMessage(), channel);
            if (channel.isConnected()) {
                return new OpenR66ProtocolSystemException("Execution aborted", e2);
            } else {
                return new OpenR66ProtocolBusinessNoWriteBackException("Execution aborted", e2);
            }
        } else {
            logger.error("Unexpected exception from downstream" +
                    " Ref Channel: " + channel.toString(), e1);
        }
        if (Configuration.configuration.r66Mib != null) {
            Configuration.configuration.r66Mib.notifyWarning(
                    "Unexpected exception", e1.getMessage());
        }
        return new OpenR66ProtocolSystemException("Unexpected exception: "+e1.getMessage(), e1);
    }
}
