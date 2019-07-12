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
 * You should have received a copy of the GNU General Public License along with Waarp . If not, see
 * <http://www.gnu.org/licenses/>.
 */
package org.waarp.openr66.protocol.localhandler.packet;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.waarp.openr66.protocol.exception.OpenR66ProtocolPacketException;
import org.waarp.openr66.protocol.localhandler.LocalChannelReference;

/**
 * Keep Alive class
 * <p>
 * header = empty middle = way end = empty
 *
 * @author frederic bregier
 */
public class KeepAlivePacket extends AbstractLocalPacket {
    private static final byte ASKVALIDATE = 0;

    private static final byte ANSWERVALIDATE = 1;

    private byte way;

    /**
     * @param valid
     */
    private KeepAlivePacket(byte valid) {
        way = valid;
    }

    /**
     *
     */
    public KeepAlivePacket() {
        way = ASKVALIDATE;
    }

    /**
     * @param headerLength
     * @param middleLength
     * @param endLength
     * @param buf
     *
     * @return the new EndTransferPacket from buffer
     *
     * @throws OpenR66ProtocolPacketException
     */
    public static KeepAlivePacket createFromBuffer(int headerLength,
                                                   int middleLength, int endLength, ByteBuf buf)
            throws OpenR66ProtocolPacketException {
        if (middleLength != 1) {
            throw new OpenR66ProtocolPacketException("Not enough data");
        }
        byte valid = buf.readByte();
        return new KeepAlivePacket(valid);
    }

    @Override
    public void createEnd(LocalChannelReference lcr) {
        end = Unpooled.EMPTY_BUFFER;
    }

    @Override
    public void createHeader(LocalChannelReference lcr) {
        header = Unpooled.EMPTY_BUFFER;
    }

    @Override
    public void createMiddle(LocalChannelReference lcr) {
        byte[] newbytes = {
                way
        };
        middle = Unpooled.wrappedBuffer(newbytes);
    }

    @Override
    public byte getType() {
        return LocalPacketFactory.KEEPALIVEPACKET;
    }

    @Override
    public String toString() {
        return "KeepAlivePacket: " + way;
    }

    /**
     * @return True if this packet is to be validated
     */
    public boolean isToValidate() {
        return way == ASKVALIDATE;
    }

    /**
     * Validate the connection
     */
    public void validate() {
        way = ANSWERVALIDATE;
        header = null;
        middle = null;
        end = null;
    }
}
