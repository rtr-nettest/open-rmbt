/*******************************************************************************
 * Copyright 2015 alladin-IT GmbH
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
 *******************************************************************************/
package at.alladin.rmbt.util.net.rtp;

import java.nio.ByteOrder;
import java.util.Arrays;

import at.alladin.rmbt.util.ByteUtil;
import at.alladin.rmbt.util.net.rtp.RealtimeTransportProtocol.PayloadType;
import at.alladin.rmbt.util.net.rtp.RealtimeTransportProtocol.RtpException;
import at.alladin.rmbt.util.net.rtp.RealtimeTransportProtocol.RtpException.RtpErrorType;
import at.alladin.rmbt.util.net.rtp.RealtimeTransportProtocol.RtpVersion;

/**
 * rtp packet including header and payload
 * @author lb
 *
 */
public class RtpPacket {
	byte[] header;
	byte[] csrcIdentifier;
	byte[] payload;
	
	public RtpPacket(PayloadType payloadType, int csrcCount, long[] csrc, int seqNumber, long timeStamp, long ssrc) {
		this(payloadType, csrcCount, csrc, seqNumber, timeStamp, ssrc, null);
	}
	
	public RtpPacket(PayloadType payloadType, int csrcCount, long[] csrc, int seqNumber, long timeStamp, long ssrc, byte[] payload) {
		this.header = RealtimeTransportProtocol.createHeaderBytes(RtpVersion.VER2, false, false, 
				csrcCount, false, payloadType, seqNumber, timeStamp, ssrc);
		
		this.csrcIdentifier = RealtimeTransportProtocol.createCsrcIdentifierBytes(csrc);
		this.payload = payload;
	}
	
	public RtpPacket(byte[] packet) throws RtpException {
		if (packet == null || packet.length < 12) {
			throw new RtpException(RtpErrorType.PACKET_SIZE_TOO_SMALL);
		}

		try {
			header = new byte[12];
			System.arraycopy(packet, 0, header, 0, header.length);
			int curPos = header.length;
			int csrsCount = getCsrcCount();
			if (csrsCount > 0) {
				csrcIdentifier = new byte[csrsCount * 4];
				System.arraycopy(packet, curPos, csrcIdentifier, 0, csrcIdentifier.length);
				curPos += csrcIdentifier.length;
			}
			if (packet.length > curPos) {
				int payloadSize = packet.length - curPos;
				payload = new byte[payloadSize];
				System.arraycopy(packet, curPos, payload, 0, payload.length);
			}
		}
		catch (Exception e)  {
			throw new RtpException(RtpErrorType.INVALID_HEADER);
		}
	}

	public byte[] getHeader() {
		return header;
	}

	public void setHeader(byte[] header) {
		this.header = header;
	}

	public byte[] getCsrcIdentifier() {
		return csrcIdentifier;
	}

	public void setCsrcIdentifier(byte[] csrcIdentifier) {
		this.csrcIdentifier = csrcIdentifier;
	}

	public byte[] getPayload() {
		return payload;
	}

	public void setPayload(byte[] payload) {
		this.payload = payload;
	}
	
	/**
	 * 
	 * @param packet
	 * @return
	 */
	public PayloadType getPayloadType() {
		return PayloadType.getByCodecValue(header[1] & 0x7F);
	}
	
	/**
	 * 
	 * @param packet
	 * @param payloadType
	 */
	public void setPayloadType( PayloadType payloadType) {
		header[1] = ByteUtil.setRightBitsValue(header[1], 7, payloadType.getValue());
	}
	
	/**
	 * 
	 * @param packet
	 * @return
	 */
	public boolean hasMarker() {
		return ByteUtil.getBit(header[1], 7);
	}

	/**
	 * 
	 * @param packet
	 * @param hasMarker
	 */
	public void setHasMarker(boolean hasMarker) {
		header[1] = ByteUtil.setBit(header[1], 7, hasMarker);
	}
		
	/**
	 * 
	 * @param packet
	 * @return
	 */
	public RtpVersion getVersion() {
		return RtpUtil.getVersion(header[0]);
	}
	
	/**
	 * 
	 * @param version
	 */
	public void setVersion(RtpVersion version) {
		header[0] = ByteUtil.setLeftBitsValue(header[0], 2, version.getVersion());
	}
	
	/**
	 * 
	 * @param packet
	 * @return
	 */
	public int getCsrcCount() {
		return (header[0] & 0x0F);
	}
	
	/**
	 * 
	 * @param csrcCount
	 */
	public void setCsrcCount(int csrcCount) {
		header[0] = ByteUtil.setRightBitsValue(header[0], 4, csrcCount);
	}
	
	/**
	 * 
	 * @return
	 */
	public long[] getCsrcIdentifiersAsLong() {
		if (csrcIdentifier != null && csrcIdentifier.length > 0) {
			long[] csrcIds = new long[csrcIdentifier.length / 4];
			for (int i = 0; i < csrcIds.length; i++) {
				csrcIds[i] = ByteUtil.getLong(csrcIdentifier, i*4, 3 + i*4, ByteOrder.BIG_ENDIAN);
			}
			
			return csrcIds;
		}
		
		return new long[] {};
	}
	
	/**
	 * 
	 * @param packet
	 * @return
	 */
	public boolean hasPadding() {
		return ByteUtil.getBit(header[0], 5);
	}
	
	/**
	 * 
	 * @param hasPadding
	 */
	public void setHasPadding(boolean hasPadding) {
		header[0] = ByteUtil.setBit(header[0], 5, hasPadding);
	}
	
	/**
	 * 
	 * @param packet
	 * @return
	 */
	public boolean hasExtension() {
		return ByteUtil.getBit(header[0], 4);
	}
	
	/**
	 * 
	 * @param hasExtension
	 */
	public void setHasExtension(boolean hasExtension) {
		header[0] = ByteUtil.setBit(header[0], 4, hasExtension);
	}

	/**
	 * 
	 * @return
	 */
	public int getSequnceNumber() {
		return ByteUtil.getInt(header, 2, 3, ByteOrder.BIG_ENDIAN);
	}

	/**
	 * 
	 * @param seqNumber
	 */
	public void setSequnceNumber(int seqNumber) {
		header = ByteUtil.setInt(header, 2, 3, seqNumber, ByteOrder.BIG_ENDIAN);
	}

	/**
	 * 
	 * @param delta
	 */
	public void increaseSequenceNumber(int delta) {
		setSequnceNumber(getSequnceNumber() + delta);
	}
	
	/**
	 * 
	 * @return
	 */
	public long getTimestamp() {
		return ByteUtil.getLong(header, 4, 7, ByteOrder.BIG_ENDIAN);
	}

	/**
	 * 
	 * @param timestamp
	 */
	public void setTimestamp(long timestamp) {
		header = ByteUtil.setLong(header, 4, 7, timestamp, ByteOrder.BIG_ENDIAN);
	}
	
	/**
	 * 
	 * @param delta
	 */
	public void increaseTimestamp(long delta) {
		setTimestamp(getTimestamp() + delta);
	}

	/**
	 * 
	 * @return
	 */
	public long getSsrc() {
		return ByteUtil.getLong(header, 8, 11, ByteOrder.BIG_ENDIAN);
	}
	
	/**
	 * 
	 * @param ssrc
	 * @return
	 */
	public void setSsrc(long ssrc) {
		header = ByteUtil.setLong(header, 8, 11, ssrc, ByteOrder.BIG_ENDIAN);
	}
	
	/**
	 * 
	 * @return
	 */
	public byte[] getBytes() {
		final byte[] d = new byte[header.length 
		                    + (csrcIdentifier != null ? csrcIdentifier.length : 0) 
		                    + (payload != null ? payload.length : 0)];
		
		int curPos = 0;
		System.arraycopy(header, 0, d, 0, header.length);
		curPos += header.length;
		if (csrcIdentifier != null) {
			System.arraycopy(csrcIdentifier, 0, d, curPos, csrcIdentifier.length);
			curPos += csrcIdentifier.length;
		}
		if (payload != null) {
			System.arraycopy(payload, 0, d, curPos, payload.length);
		}
		
		return d;
	}

	@Override
	public String toString() {
		return "RtpPacket [payload=" + Arrays.toString(payload)
				+ ", getPayloadType()=" + getPayloadType() + ", hasMarker()="
				+ hasMarker() + ", getVersion()=" + getVersion()
				+ ", getCsrcCount()=" + getCsrcCount()
				+ ", getCsrcIdentifiersAsLong()="
				+ Arrays.toString(getCsrcIdentifiersAsLong())
				+ ", hasPadding()=" + hasPadding() + ", hasExtension()="
				+ hasExtension() + ", getSequnceNumber()=" + getSequnceNumber()
				+ ", getTimestamp()=" + getTimestamp() + ", getSsrc()="
				+ getSsrc() + "]";
	}
}
