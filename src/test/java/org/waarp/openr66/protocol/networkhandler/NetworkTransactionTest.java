package org.waarp.openr66.protocol.networkhandler;

import io.netty.channel.Channel;
import org.junit.Test;

import java.net.InetSocketAddress;

import static org.mockito.Mockito.*;

public class NetworkTransactionTest {

    @Test
    public void testisBlacklistedPreventNPE() {
        Channel chan = mock(Channel.class);
        when(chan.remoteAddress()).thenReturn(null);
        NetworkTransaction.isBlacklisted(chan);

        reset(chan);

        InetSocketAddress addr = new InetSocketAddress("cannotberesolved", 6666);
        //assertNull(addr.getAddress());
        doReturn(addr).when(chan).remoteAddress();
        NetworkTransaction.isBlacklisted(chan);
    }
}
