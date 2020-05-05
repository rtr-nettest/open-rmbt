/*******************************************************************************
 * Copyright 2019 alladin-IT GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package at.rtr.rmbt.shared.qos.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import at.rtr.rmbt.shared.qos.UdpPayload;

/**
 *
 */
public class UdpPayloadUtil {

    public enum UdpPayloadEntry {
        COMMUNICATION_FLAG(0,1),
        PACKET_NUMBER(1,1),
        TOKEN(2,36),
        TIMESTAMP(38,8);

        int length;
        int position;

        UdpPayloadEntry(final int position, final int length) {
            this.position = position;
            this.length = length;
        }

        public int getPosition() {
            return this.position;
        }

        public int getLength() {
            return this.length;
        }
    }


    public static UdpPayload toUdpPayload(final byte[] payload) {
        final UdpPayload udpPayload = new UdpPayload();

        udpPayload.setCommunicationFlag(payload[UdpPayloadEntry.COMMUNICATION_FLAG.getPosition()]);
        udpPayload.setPacketNumber(payload[UdpPayloadEntry.PACKET_NUMBER.getPosition()]);

        try(final ByteArrayInputStream bais = new ByteArrayInputStream(payload);
        		final DataInputStream dis = new DataInputStream(bais)) {
            final byte[] token = new byte[UdpPayloadEntry.TOKEN.getLength()];
            dis.read(token, 0, UdpPayloadEntry.TOKEN.getPosition());
            dis.read(token, 0, UdpPayloadEntry.TOKEN.getLength());
            udpPayload.setUuid(new String(token));
            udpPayload.setTimestamp(dis.readLong());
        } catch (final IOException e) {
            e.printStackTrace();
        }

        return udpPayload;
    }

    public static byte[] toBytes(final UdpPayload udpPayload) {
        try (final ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             final DataOutputStream dataOut = new DataOutputStream(byteOut)) {

            dataOut.writeByte(udpPayload.getCommunicationFlag());
            dataOut.writeByte(udpPayload.getPacketNumber());
            dataOut.write(udpPayload.getUuid().getBytes());
            dataOut.writeLong(udpPayload.getTimestamp());
            
            return byteOut.toByteArray();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

}
