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

import at.alladin.rmbt.util.ByteUtil;

/**
 * contains basic RTP definitions and oprations (payload types, versions, codecs, exceptions, header generation)
 * @author lb
 *
 */
public class RealtimeTransportProtocol {
	
	public static class RtpException extends Exception {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public static enum RtpErrorType {
			PACKET_SIZE_TOO_SMALL,
			INVALID_HEADER
		}
		
		protected RtpErrorType rtpErrorType;
		
		public RtpException(RtpErrorType rtpErrorType) {
			this.rtpErrorType = rtpErrorType;
		}

		public RtpErrorType getRtpErrorType() {
			return rtpErrorType;
		}

		public void setRtpErrorType(RtpErrorType rtpErrorType) {
			this.rtpErrorType = rtpErrorType;
		}
	}
	
	public static enum CodecType {
		AUDIO,
		VIDEO,
		BOTH,
		UNKNOWN
	}
	
	/**
	 * RTP payload types as defined in RFC 3551
	 * @author lb
	 *
	 */
	public static enum PayloadType {
		UNKNOWN(-1, -1, -1, CodecType.UNKNOWN),
		PCMU(0, 8000, 1, CodecType.AUDIO),
		GSM(3, 8000, 1, CodecType.AUDIO),
		G723(4, 8000, 1, CodecType.AUDIO),
		DVI4_8(5, 8000, 1, CodecType.AUDIO),
		DVI4_16(6, 16000, 1, CodecType.AUDIO),
		LPC(7, 8000, 1, CodecType.AUDIO),
		PCMA(8, 8000, 1, CodecType.AUDIO),
		G722(9, 8000, 1, CodecType.AUDIO),
		L16_1(10, 44100, 2, CodecType.AUDIO),
		L16_2(11, 44100, 1, CodecType.AUDIO),
		QCELP(12, 8000, 1, CodecType.AUDIO),
		CN(13, 8000, 1, CodecType.AUDIO),
		MPA(14, 90000, 1, CodecType.AUDIO),
		G728(15, 8000, 1, CodecType.AUDIO),
		DVI4_11(16, 11025, 1, CodecType.AUDIO),
		DVI4_22(17, 22050, 1, CodecType.AUDIO),
		G729(18, 8000, 1, CodecType.AUDIO),
		G726_40(-1, 8000, 1, CodecType.AUDIO),
		G726_32(-1, 8000, 1, CodecType.AUDIO),
		G726_24(-1, 8000, 1, CodecType.AUDIO),
		G726_16(-1, 8000, 1, CodecType.AUDIO),
		G729D(-1, 8000, 1, CodecType.AUDIO),
		G729E(-1, 8000, 1, CodecType.AUDIO),
		GSM_EFR(-1, 8000, 1, CodecType.AUDIO),
		L8(-1, -1, -1, CodecType.AUDIO),
		RED(-1, -1, -1, CodecType.AUDIO),
		VDVI(-1, -1, 1, CodecType.AUDIO),
		CELB(25, 90000, -1, CodecType.VIDEO),
		JPEG(26, 90000, -1, CodecType.VIDEO),
		NV(28, 90000, -1, CodecType.VIDEO),
		H261(31, 90000, -1, CodecType.VIDEO),
		MPV(32, 90000, -1, CodecType.VIDEO),
		MP2T(33, 90000, -1, CodecType.BOTH),
		H263(34, 90000, -1, CodecType.VIDEO),
		H263_1998(-1, 90000, -1, CodecType.VIDEO);
		
		protected final int value;
		protected final int sampleRate;
		protected final int channels;
		protected final CodecType codecType;
		
		private PayloadType(int value, int sampleRate, int channels, CodecType codecType) {
			this.value = value;
			this.sampleRate = sampleRate;
			this.channels = channels;
			this.codecType = codecType;
		}

		/**
		 * 
		 * @return payload type value or -1 if the codec is defined as dynamic (see RFC 3551)
		 */
		public int getValue() {
			return value;
		}

		/**
		 * 
		 * @return sample rate or -1 if the codec's sample rate is variable or undefined (see RFC 3551)
		 */
		public int getSampleRate() {
			return sampleRate;
		}

		/**
		 * 
		 * @return number of channels used for this codec or -1 for unknown/special values (see RFC 3551)
		 */
		public int getChannels() {
			return channels;
		}

		/**
		 * 
		 * @return the codec type (either {@link CodecType#AUDIO} or {@link CodecType#VIDEO})
		 */
		public CodecType getCodecType() {
			return codecType;
		}
		
		public static PayloadType getByCodecValue(int value) {
			for (PayloadType p : PayloadType.values()) {
				if (p.getValue() == value) {
					return p;
				}
			}
			
			return UNKNOWN;
		}
		
		public static PayloadType getByCodecValue(int value, PayloadType defaultType) {
			final PayloadType p = getByCodecValue(value);
			if (UNKNOWN.equals(p)) {
				return defaultType;
			}
			return p;
		}
	}
	
	public static enum RtpVersion {
		VER0(0),
		VER1(1),
		VER2(2),
		UNKNOWN(-1);
		
		final int version;
		
		RtpVersion(int version) {
			this.version = version;
		}
		
		public int getVersion() {
			return version;
		}
		
		public static RtpVersion getByVersion(int version) {
			for (RtpVersion v : RtpVersion.values()) {
				if (v.getVersion() == version) {
					return v;
				}
			}
			
			return UNKNOWN;
		}
	}
	
	/**
	 * creates the first 4 bytes of the RTP header
	 * @param version
	 * @param hasPadding
	 * @param hasExtension
	 * @return
	 */
	public static byte[] createHeaderBytes(RtpVersion version, boolean hasPadding, boolean hasExtension, 
			int csrcCount, boolean setMarker, PayloadType payloadType, int sequenceNumber, long timeStamp, long ssrc) {
		byte[] h = new byte[12];
		h[0] = ByteUtil.setLeftBitsValue(h[0], 2, version.getVersion());
		h[0] = ByteUtil.setBit(h[0], 5, hasPadding);
		h[0] = ByteUtil.setBit(h[0], 4, hasExtension);
		h[0] = ByteUtil.setRightBitsValue(h[0], 4, csrcCount);
		h[1] = ByteUtil.setBit(h[1], 7, setMarker);
		h[1] = ByteUtil.setRightBitsValue(h[1], 7, payloadType.getValue());
		//network byte order = big endian
		ByteUtil.setInt(h, 2, 3, sequenceNumber, ByteOrder.BIG_ENDIAN);
		ByteUtil.setLong(h, 4, 7, timeStamp, ByteOrder.BIG_ENDIAN);
		ByteUtil.setLong(h, 8, 11, ssrc, ByteOrder.BIG_ENDIAN);
		return h;
	}
	
	/**
	 * 
	 * @param csrcIds
	 * @return
	 */
	public static byte[] createCsrcIdentifierBytes(long[] csrcIds) {
		if (csrcIds != null && csrcIds.length > 0) {
			byte[] h = new byte[csrcIds.length * 4];
			for (int i = 0; i < csrcIds.length; i++) {
				ByteUtil.setLong(h, i*4, 3 + i*4, csrcIds[i], ByteOrder.BIG_ENDIAN);
			}
			return h;
		}
		return null;
	}
}
