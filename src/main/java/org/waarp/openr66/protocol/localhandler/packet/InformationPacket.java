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
 * Information of files class
 *
 * header = "rulename" middle = requestedInfo end = "FILENAME"
 *
 * @author frederic bregier
 */
public class InformationPacket extends AbstractLocalPacket {

    private final String rulename;
    private final byte requestedInfo;
    private final String filename;

    /**
     * @param rulename
     * @param request
     * @param filename
     */
    public InformationPacket(String rulename, byte request, String filename) {
        this.rulename = rulename;
        this.requestedInfo = request;
        this.filename = filename;
    }

    /**
     * @param headerLength
     * @param middleLength
     * @param endLength
     * @param buf
     * @return the new EndTransferPacket from buffer
     * @throws OpenR66ProtocolPacketException
     */
    public static InformationPacket createFromBuffer(int headerLength,
                                                     int middleLength, int endLength, ByteBuf buf)
            throws OpenR66ProtocolPacketException {
        if (headerLength - 1 <= 0) {
            throw new OpenR66ProtocolPacketException("Not enough data");
        }
        if (middleLength != 1) {
            throw new OpenR66ProtocolPacketException("Not enough data");
        }
        final byte[] bheader = new byte[headerLength - 1];
        final byte[] bend = new byte[endLength];
        if (headerLength - 1 > 0) {
            buf.readBytes(bheader);
        }
        byte request = buf.readByte();
        if (endLength > 0) {
            buf.readBytes(bend);
        }
        final String sheader = new String(bheader);
        final String send = new String(bend);
        return new InformationPacket(sheader, request, send);
    }

    @Override
    public void createEnd(LocalChannelReference lcr) {
        if (filename != null) {
            end = Unpooled.wrappedBuffer(filename.getBytes());
        }
    }

    @Override
    public void createHeader(LocalChannelReference lcr) throws OpenR66ProtocolPacketException {
        if (rulename == null) {
            throw new OpenR66ProtocolPacketException("Not enough data");
        }
        header = Unpooled.wrappedBuffer(rulename.getBytes());
    }

    @Override
    public void createMiddle(LocalChannelReference lcr) {
        byte[] newbytes = {
                requestedInfo
        };
        middle = Unpooled.wrappedBuffer(newbytes);
    }

    @Override
    public byte getType() {
        return LocalPacketFactory.INFORMATIONPACKET;
    }

    @Override
    public String toString() {
        return "InformationPacket: " + requestedInfo + " " + rulename + " " + filename;
    }

    /**
     * @return the requestId
     */
    public byte getRequest() {
        return requestedInfo;
    }

    /**
     * @return the rulename
     */
    public String getRulename() {
        return rulename;
    }

    /**
     * @return the filename
     */
    public String getFilename() {
        if (filename != null) {
            return filename;
        } else {
            return "";
        }
    }

    public static enum ASKENUM {
        ASKEXIST, ASKMLSDETAIL, ASKLIST, ASKMLSLIST;
    }
}
