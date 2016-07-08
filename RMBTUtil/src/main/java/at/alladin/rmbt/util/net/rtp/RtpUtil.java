/*******************************************************************************
 * Copyright 2015, 2016 alladin-IT GmbH
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

import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteOrder;
import java.nio.channels.DatagramChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import at.alladin.rmbt.util.ByteUtil;
import at.alladin.rmbt.util.net.udp.NioUdpStreamSender;
import at.alladin.rmbt.util.net.udp.StreamSender.UdpStreamCallback;
import at.alladin.rmbt.util.net.udp.StreamSender.UdpStreamSenderSettings;
import at.alladin.rmbt.util.net.udp.UdpStreamSender;

/**
 * 
 * @author lb
 *
 */
public class RtpUtil {

	
	public static <T extends Closeable> T runVoipStream(T socket, final boolean closeOnFinish, InetAddress targetHost, int targetPort, int sampleRate, 
			int bps, RealtimeTransportProtocol.PayloadType payloadType, long sequenceNumber, int ssrc, 
			long callDuration, final long delay, final long timeout, final boolean useNio, final UdpStreamCallback receiveCallback) throws InterruptedException, TimeoutException, IOException {
		return RtpUtil.runVoipStream(socket, closeOnFinish, targetHost, targetPort, null, sampleRate, bps, payloadType, sequenceNumber, ssrc, callDuration, delay, timeout, useNio, receiveCallback);
	}

		
	
	/**
	 * runs an rtp/voip stream (incoming and outgoing)
	 * @param socket
	 * @param targetHost
	 * @param targetPort
	 * @param sampleRate
	 * @param bps
	 * @param payloadType
	 * @param sequenceNumber
	 * @param ssrc
	 * @param callDuration
	 * @param delay
	 * @param timeout
	 * @param useNio
	 * @param receiveCallback
	 * @throws InterruptedException
	 * @throws TimeoutException
	 * @throws IOException 
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Closeable> T runVoipStream(T socket, final boolean closeOnFinish, InetAddress targetHost, int targetPort, final Integer incomingPort, int sampleRate, 
			int bps, RealtimeTransportProtocol.PayloadType payloadType, long sequenceNumber, int ssrc, 
			long callDuration, final long delay, final long timeout, final boolean useNio, final UdpStreamCallback receiveCallback) throws InterruptedException, TimeoutException, IOException {
		
		final int payloadSize = (int) (sampleRate / (1000 / delay) * (bps / 8));
		final Random r = new Random();
		final int payloadTimestamp = (int) (sampleRate / (1000 / delay));
		final RtpPacket initialRtpPacket = new RtpPacket(payloadType, 0, new long[] {}, (int) sequenceNumber, 0, ssrc);
		final int numPackets = (int) (callDuration / delay);
		final UdpStreamSenderSettings<T> settings = new UdpStreamSenderSettings<>(socket, closeOnFinish, targetHost, targetPort, numPackets, delay, timeout, TimeUnit.MILLISECONDS, false, 0);
		settings.setIncomingPort(incomingPort);
		
		if (receiveCallback == null) {
			settings.setWriteOnly(true);
		}

		final UdpStreamCallback callback = new UdpStreamCallback() {
			
			@Override
			public boolean onSend(DataOutputStream dataOut, int packetNumber)
					throws IOException {
				if (packetNumber > 0) {
					initialRtpPacket.increaseSequenceNumber(1);
					initialRtpPacket.increaseTimestamp(payloadTimestamp);
					initialRtpPacket.setHasMarker(false);
				}
				else {
					initialRtpPacket.setHasMarker(true);
				}
				
				final byte[] payload = new byte[payloadSize];
				r.nextBytes(payload);
				initialRtpPacket.setPayload(payload);
				
				final byte[] data = initialRtpPacket.getBytes();
				dataOut.write(data);
				return true;
			}
			
			@Override
			public void onReceive(DatagramPacket dp) throws IOException {
				if (receiveCallback != null) {
					receiveCallback.onReceive(dp);
				}
			}

			@Override
			public void onBind(Integer port) throws IOException {
				receiveCallback.onBind(incomingPort);
			}
		};
		
		if (!useNio) {
			final UdpStreamSender udpStreamSender = new UdpStreamSender((UdpStreamSenderSettings<DatagramSocket>) settings, callback);
			return (T) udpStreamSender.send();
		}
		else {
			final NioUdpStreamSender udpStreamSender = new NioUdpStreamSender((UdpStreamSenderSettings<DatagramChannel>) settings, callback);
			return (T) udpStreamSender.send();			
		}
	}

	/**
	 * extract the rtp version from the first header byte 
	 * @param firstHeaderByte
	 * @return
	 */
	public static RealtimeTransportProtocol.RtpVersion getVersion(byte firstHeaderByte) {
		return RealtimeTransportProtocol.RtpVersion.getByVersion((firstHeaderByte >> 6) & 0x03);	
	}
	
	/**
	 * get the synchronization source identifier
	 * @param data
	 * @return rtp packet ssrc or -1 if packet data is invalid 
	 */
	public static long getSsrc(byte[] data) {
		if (data != null && data.length >= 11) {
			return ByteUtil.getLong(data, 8, 11, ByteOrder.BIG_ENDIAN);	
		}
		else {
			return -1;
		}
	}

	/**
	 * 
	 * @param rtpControlDataMap
	 */
	public static RtpQoSResult calculateQoS(Map<Integer, RtpControlData> rtpControlDataMap, long initialSequenceNumber, int sampleRate) {
		TreeSet<Integer> sequenceNumberSet = new TreeSet<>(rtpControlDataMap.keySet());
		
		Map<Integer, Float> jitterMap = new HashMap<>();
		TreeSet<RtpSequence> sequenceSet = new TreeSet<>();
		
		long maxJitter = 0;
		long meanJitter = 0;
		long skew = 0;
		long maxDelta = 0;
		long tsDiff = 0;
		
		int prevSeqNr = -1;
		for (int x : sequenceNumberSet) {
			RtpControlData i = rtpControlDataMap.get(prevSeqNr);
			RtpControlData j = rtpControlDataMap.get(x);
			if (prevSeqNr >= 0) {
				tsDiff = j.receivedNs - i.receivedNs;
				final float prevJitter = jitterMap.get(prevSeqNr);
				final long delta = Math.abs(calculateDelta(i, j, sampleRate));
				final float jitter = prevJitter + ((float)delta - prevJitter) / 16f;
				jitterMap.put(x, jitter);
				maxDelta = Math.max(delta, maxDelta);;
				skew += TimeUnit.NANOSECONDS.convert((long) (((float)(j.rtpPacket.getTimestamp() - i.rtpPacket.getTimestamp()) / (float)sampleRate) * 1000f), TimeUnit.MILLISECONDS) - tsDiff;
				maxJitter = Math.max((long)jitter, maxJitter);
				meanJitter += jitter;
			}
			else {
				jitterMap.put(x, 0f);
			}
			prevSeqNr = x;
			sequenceSet.add(new RtpSequence(j.receivedNs, x));
		}

		long nextSeq = initialSequenceNumber;
		int packetsOutOfOrder = 0;
		int maxSequential = 0;
		int minSequential = 0;
		int curSequential = 0;
		for (RtpSequence i : sequenceSet) {
			if (i.seq != nextSeq) {
				packetsOutOfOrder++;
				maxSequential = Math.max(curSequential, maxSequential);
				if (curSequential > 1) {
					minSequential = curSequential < minSequential ? curSequential : (minSequential == 0 ? curSequential : minSequential);
				}
				curSequential = 0;
			}
			else {
				curSequential++;
			}
			
			nextSeq++;
		}
		
		maxSequential = Math.max(curSequential, maxSequential);
		if (curSequential > 1) {
			minSequential = curSequential < minSequential ? curSequential : (minSequential == 0 ? curSequential : minSequential);
		}
		
		if (minSequential == 0 && maxSequential > 0) {
			minSequential = maxSequential;
		}
		
		return new RtpQoSResult(maxJitter, jitterMap.size() > 0 ? meanJitter / jitterMap.size() : 0, skew, maxDelta, packetsOutOfOrder, minSequential, maxSequential, jitterMap);
	}
	
	private static long calculateDelta(RtpControlData i, RtpControlData j, int sampleRate) {
		final long msDiff = j.receivedNs - i.receivedNs;
		final long tsDiff = TimeUnit.NANOSECONDS.convert((long) (((float)(j.rtpPacket.getTimestamp() - i.rtpPacket.getTimestamp()) / (float)sampleRate) * 1000f), TimeUnit.MILLISECONDS);
		return msDiff - tsDiff;
	}
	
	/**
	 * 
	 * @author lb
	 *
	 */
	public final static class RtpControlData {
		RtpPacket rtpPacket;
		long receivedNs;
		
		public RtpControlData(RtpPacket rtpPacket, long receivedNs) {
			this.rtpPacket = rtpPacket;
			this.receivedNs = receivedNs;
		}
	}
	
	private final static class RtpSequence implements Comparable<RtpSequence> {
		long timestampNs;
		int seq;
		
		public RtpSequence(long timestampNs, int seq) {
			this.timestampNs = timestampNs;
			this.seq = seq;
		}
		
		@Override
		public int compareTo(RtpSequence o) {
			return Long.valueOf(timestampNs).compareTo(o.timestampNs); 
		}
		
		
	}
	
	public final static class RtpQoSResult {
		final Map<Integer, Float> jitterMap;
		final int receivedPackets;
		final long maxJitter;
		final long meanJitter;
		final long skew;
		final long maxDelta;
		final int outOfOrder;
		final int minSequential;
		final int maxSequencial;
		
		public RtpQoSResult(long maxJitter, long meanJitter, long skew, long maxDelta, int outOfOrder, int minSequential, int maxSequential, Map<Integer, Float> jitterMap) {
			this.jitterMap = jitterMap;
			this.maxJitter = maxJitter;
			this.meanJitter = meanJitter;
			this.skew = skew;
			this.maxDelta = maxDelta;
			this.outOfOrder = outOfOrder;
			this.receivedPackets = jitterMap.size();
			this.minSequential = minSequential > receivedPackets ? receivedPackets : minSequential;
			this.maxSequencial = maxSequential > receivedPackets ? receivedPackets : maxSequential;
		}

		public Map<Integer, Float> getJitterMap() {
			return jitterMap;
		}

		public int getReceivedPackets() {
			return receivedPackets;
		}

		public long getMaxJitter() {
			return maxJitter;
		}

		public long getMeanJitter() {
			return meanJitter;
		}

		public long getSkew() {
			return skew;
		}

		public long getMaxDelta() {
			return maxDelta;
		}
		
		public int getOutOfOrder() {
			return outOfOrder;
		}
		
		public int getMinSequential() {
			return minSequential;
		}

		public int getMaxSequencial() {
			return maxSequencial;
		}

		@Override
		public String toString() {
			return "RtpQoSResult [jitterMap=" + jitterMap
					+ ", receivedPackets=" + receivedPackets 
					+ ", outOfOrder=" + outOfOrder + ", minSequential=" + minSequential + ", maxSequencial=" + maxSequencial  
					+ ", maxJitter=" + ((float)maxJitter / 1000000f) 
					+ ", meanJitter=" + ((float) meanJitter / 1000000f) + ", skew="
					+ ((float)skew / 1000000f) + ", maxDelta=" + ((float) maxDelta / 1000000) + "]";
		}
	}
}
