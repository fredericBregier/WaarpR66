/**
 * Copyright 2009, Frederic Bregier, and individual contributors
 * by the @author tags. See the COPYRIGHT.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package openr66.protocol.localhandler;

import goldengate.common.logging.GgInternalLogger;
import goldengate.common.logging.GgInternalLoggerFactory;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.Channels;

import openr66.filesystem.R66Session;
import openr66.protocol.exception.OpenR66ProtocolPacketException;
import openr66.protocol.exception.OpenR66ProtocolSystemException;
import openr66.protocol.exception.OpenR66RunnerErrorException;
import openr66.protocol.localhandler.packet.AbstractLocalPacket;
import openr66.protocol.localhandler.packet.ErrorPacket;
import openr66.protocol.localhandler.packet.LocalPacketFactory;
import openr66.protocol.localhandler.packet.ValidPacket;
import openr66.protocol.networkhandler.packet.NetworkPacket;
import openr66.protocol.utils.ChannelUtils;

/**
 * @author Frederic Bregier
 *
 */
public class RetrieveRunner implements Runnable {
    /**
     * Internal Logger
     */
    private static final GgInternalLogger logger = GgInternalLoggerFactory
            .getLogger(RetrieveRunner.class);

    private final R66Session session;
    private final LocalChannelReference localChannelReference;
    private final Channel channel;

    public RetrieveRunner(R66Session session, Channel channel) {
        this.session = session;
        this.localChannelReference = this.session.getLocalChannelReference();
        this.channel = channel;
    }
    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        try {
            this.session.getFile().retrieveBlocking();
        } catch (OpenR66RunnerErrorException e) {
            this.localChannelReference.validateAction(false, e);
            ErrorPacket error = new ErrorPacket("Transfer in error",
                    e.toString(),
                    ErrorPacket.FORWARDCLOSECODE);
            try {
                writeBack(error, true);
            } catch (OpenR66ProtocolPacketException e1) {
            }
            ChannelUtils.close(channel);
            return;
        } catch (OpenR66ProtocolSystemException e) {
            this.localChannelReference.validateAction(false, e);
            ErrorPacket error = new ErrorPacket("Transfer in error",
                    e.toString(),
                    ErrorPacket.FORWARDCLOSECODE);
            try {
                writeBack(error, true);
            } catch (OpenR66ProtocolPacketException e1) {
            }
            ChannelUtils.close(channel);
            return;
        }
        this.localChannelReference.getFutureAction().awaitUninterruptibly();
        if (this.localChannelReference.getFutureAction().isSuccess()) {
            // send a validation
            ValidPacket validPacket = new ValidPacket("File transmitted",
                    Integer.toString(this.session.getRunner().getRank()),
                    LocalPacketFactory.REQUESTPACKET);
            try {
                writeBack(validPacket, true);
            } catch (OpenR66ProtocolPacketException e) {
            }
            ChannelUtils.close(channel);
            return;
        } else {
            ErrorPacket error = new ErrorPacket("Transfer in error",
                    this.localChannelReference.getFutureAction().getResult().toString(),
                    ErrorPacket.FORWARDCLOSECODE);
            try {
                writeBack(error, true);
            } catch (OpenR66ProtocolPacketException e) {
            }
            ChannelUtils.close(channel);
            return;
        }
    }

    private void writeBack(AbstractLocalPacket packet, boolean await)
    throws OpenR66ProtocolPacketException {
        NetworkPacket networkPacket;
        try {
            networkPacket = new NetworkPacket(localChannelReference
                    .getLocalId(), localChannelReference.getRemoteId(), packet);
        } catch (OpenR66ProtocolPacketException e) {
            logger.error("Cannot construct message from " + packet.toString(),
                    e);
            throw e;
        }
        if (await) {
            Channels.write(localChannelReference.getNetworkChannel(),
                    networkPacket).awaitUninterruptibly();
        } else {
            Channels.write(localChannelReference.getNetworkChannel(),
                    networkPacket);
        }
    }

}
