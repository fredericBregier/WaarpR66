/**
 * This file is part of Waarp Project.
 * 
 * Copyright 2009, Frederic Bregier, and individual contributors by the @author tags. See the
 * COPYRIGHT.txt in the distribution for a full listing of individual contributors.
 * 
 * All Waarp Project is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 * 
 * Waarp is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with Waarp . If not, see
 * <http://www.gnu.org/licenses/>.
 */
package org.waarp.openr66.protocol.networkhandler.ssl;

import java.util.concurrent.TimeUnit;

import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.handler.traffic.ChannelTrafficShapingHandler;
import io.netty.util.HashedWheelTimer;

import org.waarp.common.crypto.ssl.WaarpSecureKeyStore;
import org.waarp.common.crypto.ssl.WaarpSslContextFactory;
import org.waarp.openr66.protocol.configuration.Configuration;
import org.waarp.openr66.protocol.exception.OpenR66ProtocolNoDataException;
import org.waarp.openr66.protocol.networkhandler.GlobalTrafficHandler;
import org.waarp.openr66.protocol.networkhandler.NetworkServerInitializer;
import org.waarp.openr66.protocol.networkhandler.packet.NetworkPacketCodec;

/**
 * @author Frederic Bregier
 * 
 */
public class NetworkSslServerInitializer extends ChannelInitializer<SocketChannel> {
    protected final boolean isClient;
    public static WaarpSslContextFactory waarpSslContextFactory;
    public static WaarpSecureKeyStore waarpSecureKeyStore;
    /**
     * Global HashedWheelTimer
     */
    public HashedWheelTimer timer = (HashedWheelTimer) Configuration.configuration.getTimerClose();

    /**
     * 
     * @param isClient
     *            True if this Factory is to be used in Client mode
     */
    public NetworkSslServerInitializer(boolean isClient) {
        super();
        this.isClient = isClient;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        final ChannelPipeline pipeline = ch.pipeline();
        // Add SSL handler first to encrypt and decrypt everything.
        SslHandler sslHandler = null;
        if (isClient) {
            // Not server: no clientAuthent, no renegotiation
            sslHandler =
                    waarpSslContextFactory.initInitializer(false, false);
        } else {
            // Server: no renegotiation still, but possible clientAuthent
            sslHandler =
                    waarpSslContextFactory.initInitializer(true,
                            waarpSslContextFactory.needClientAuthentication());
        }
        pipeline.addLast("ssl", sslHandler);

        pipeline.addLast("codec", new NetworkPacketCodec());
        pipeline.addLast(NetworkServerInitializer.TIMEOUT,
                new IdleStateHandler(0, 0, Configuration.configuration.TIMEOUTCON, TimeUnit.MILLISECONDS));
        GlobalTrafficHandler handler = Configuration.configuration
                .getGlobalTrafficShapingHandler();
        if (handler != null) {
            pipeline.addLast(NetworkServerInitializer.LIMIT, handler);
        }
        ChannelTrafficShapingHandler trafficChannel = null;
        try {
            trafficChannel =
                    Configuration.configuration
                            .newChannelTrafficShapingHandler();
            pipeline.addLast(NetworkServerInitializer.LIMITCHANNEL, trafficChannel);
        } catch (OpenR66ProtocolNoDataException e) {
        }
        pipeline.addLast(Configuration.configuration.getHandlerGroup(), "handler", new NetworkSslServerHandler(
                !this.isClient));
    }
}
