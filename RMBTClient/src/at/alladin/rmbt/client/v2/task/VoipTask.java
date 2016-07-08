/*******************************************************************************
 * Copyright 2013-2016 alladin-IT GmbH
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
package at.alladin.rmbt.client.v2.task;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import at.alladin.rmbt.client.QualityOfServiceTest;
import at.alladin.rmbt.client.v2.task.result.QoSTestResult;
import at.alladin.rmbt.client.v2.task.result.QoSTestResultEnum;
import at.alladin.rmbt.util.net.rtp.RealtimeTransportProtocol.PayloadType;
import at.alladin.rmbt.util.net.rtp.RealtimeTransportProtocol.RtpException;
import at.alladin.rmbt.util.net.rtp.RtpPacket;
import at.alladin.rmbt.util.net.rtp.RtpUtil;
import at.alladin.rmbt.util.net.rtp.RtpUtil.RtpControlData;
import at.alladin.rmbt.util.net.rtp.RtpUtil.RtpQoSResult;
import at.alladin.rmbt.util.net.udp.StreamSender.UdpStreamCallback;

/**
 * 
 * @author lb
 * 
 * 	As of RFC 3550 and RFC 3551 most RTP (VoIP) Codecs have a sampling rate of 8kHz.<br>
 * 	The delay between the packets is set to 20ms for most codecs.<br>
 * 	The sample size varies from 2 (G726-16) to 16 (L16) bits per sample. Some codecs have a variable sample size.<br>
 * 	<br>
 *
 *	The default VoIP test will be:
 *	<ul>
 *		<li>sampling rate: 8000 Hz</li>
 *		<li>size: 8bit per sample</li>
 *		<li>time/packet: 20ms</li>
 *	</ul>
 *	This is similar to the G722 audio codec (ITU-T Recommendation G.722).<br> 
 *	The G722 codec's actual sampling rate is 16kHz but because it was erroneously assigned in RFC 1890 with 8kHz 
 *	it needs to have this sampling rate to assure backward compatibility. 
 * 
 */
public class VoipTask extends AbstractQoSTask {
	
	private final static Pattern VOIP_RECEIVE_RESPONSE_PATTERN = Pattern.compile("VOIPRESULT (-?[\\d]*) (-?[\\d]*) (-?[\\d]*) (-?[\\d]*) (-?[\\d]*) (-?[\\d]*) (-?[\\d]*) (-?[\\d]*)");
	
	private final static Pattern VOIP_OK_PATTERN = Pattern.compile("OK ([\\d]*)");
	
	private final Integer outgoingPort;
	
	private final Integer incomingPort;
	
	private final long callDuration;
	
	private final long timeout;
	
	private final long delay;
	
	private final int sampleRate;
	
	private final int bitsPerSample;
	
	private final PayloadType payloadType;
	
	private final static long DEFAULT_TIMEOUT = 3000000000L; //3s
	
	private final static long DEFAULT_CALL_DURATION = 1000000000L; //1s
	
	private final static long DEFAULT_DELAY = 20000000L; //20ms
	
	private final static int DEFAULT_SAMPLE_RATE = 8000; //8kHz
	
	private final static int DEFAULT_BITS_PER_SAMPLE = 8; //8 bits per sample
	
	private final static PayloadType DEFAULT_PAYLOAD_TYPE = PayloadType.PCMA;
	
	public final static String PARAM_BITS_PER_SAMLE = "bits_per_sample";
	
	public final static String PARAM_SAMPLE_RATE = "sample_rate";
	
	public final static String PARAM_DURATION = "call_duration"; //call duration in ns
	
	public final static String PARAM_PORT = "in_port";
	
	public final static String PARAM_PORT_OUT = "out_port";
		
	public final static String PARAM_TIMEOUT = "timeout";
	
	public final static String PARAM_DELAY = "delay";
	
	public final static String PARAM_PAYLOAD = "payload";
	
	public final static String RESULT_PAYLOAD = "voip_objective_payload";
	
	public final static String RESULT_IN_PORT = "voip_objective_in_port";
	
	public final static String RESULT_OUT_PORT = "voip_objective_out_port";
	
	public final static String RESULT_CALL_DURATION = "voip_objective_call_duration";
	
	public final static String RESULT_BITS_PER_SAMPLE = "voip_objective_bits_per_sample";
	
	public final static String RESULT_SAMPLE_RATE = "voip_objective_sample_rate";
	
	public final static String RESULT_DELAY = "voip_objective_delay";
	
	public final static String RESULT_TIMEOUT = "voip_objective_timeout";
	
	public final static String RESULT_STATUS = "voip_result_status";
	
	public final static String RESULT_VOIP_PREFIX = "voip_result";
	
	public final static String RESULT_INCOMING_PREFIX = "_in_";
	
	public final static String RESULT_OUTGOING_PREFIX = "_out_";
	
	public final static String RESULT_SHORT_SEQUENTIAL = "short_seq";
	
	public final static String RESULT_LONG_SEQUENTIAL = "long_seq";
	
	public final static String RESULT_MAX_JITTER = "max_jitter";
	
	public final static String RESULT_MEAN_JITTER = "mean_jitter";
	
	public final static String RESULT_MAX_DELTA = "max_delta";
	
	public final static String RESULT_SKEW = "skew";
	
	public final static String RESULT_NUM_PACKETS = "num_packets";
	
	public final static String RESULT_SEQUENCE_ERRORS = "sequence_error";
	
	/**
	 * 
	 * @param taskDesc
	 */
	public VoipTask(QualityOfServiceTest nnTest, TaskDesc taskDesc, int threadId) {
		super(nnTest, taskDesc, threadId, threadId);
		String value = (String) taskDesc.getParams().get(PARAM_DURATION);
		this.callDuration = value != null ? Long.valueOf(value) : DEFAULT_CALL_DURATION;
		
		value = (String) taskDesc.getParams().get(PARAM_PORT);
		this.incomingPort = value != null ? Integer.valueOf(value) : null;

		value = (String) taskDesc.getParams().get(PARAM_PORT_OUT);
		this.outgoingPort = value != null ? Integer.valueOf(value) : null;
		
		value = (String) taskDesc.getParams().get(PARAM_TIMEOUT);
		this.timeout = value != null ? Long.valueOf(value) : DEFAULT_TIMEOUT;
		
		value = (String) taskDesc.getParams().get(PARAM_DELAY);
		this.delay = value != null ? Long.valueOf(value) : DEFAULT_DELAY;
		
		value = (String) taskDesc.getParams().get(PARAM_BITS_PER_SAMLE);
		this.bitsPerSample = value != null ? Integer.valueOf(value) : DEFAULT_BITS_PER_SAMPLE;

		value = (String) taskDesc.getParams().get(PARAM_SAMPLE_RATE);
		this.sampleRate = value != null ? Integer.valueOf(value) : DEFAULT_SAMPLE_RATE;
		
		value = (String) taskDesc.getParams().get(PARAM_PAYLOAD);
		this.payloadType = value != null ? PayloadType.getByCodecValue(Integer.valueOf(value), DEFAULT_PAYLOAD_TYPE) : DEFAULT_PAYLOAD_TYPE;
	}

	/**
	 * 
	 */
	public QoSTestResult call() throws Exception {
		final AtomicInteger ssrc = new AtomicInteger(-1);
		final QoSTestResult result = initQoSTestResult(QoSTestResultEnum.VOIP);
		
		result.getResultMap().put(RESULT_BITS_PER_SAMPLE, bitsPerSample);
		result.getResultMap().put(RESULT_CALL_DURATION, callDuration);
		result.getResultMap().put(RESULT_DELAY, delay);
		result.getResultMap().put(RESULT_IN_PORT, incomingPort);
		result.getResultMap().put(RESULT_OUT_PORT, outgoingPort);
		result.getResultMap().put(RESULT_SAMPLE_RATE, sampleRate);
		result.getResultMap().put(RESULT_PAYLOAD, payloadType.getValue());
		result.getResultMap().put(RESULT_STATUS, "OK");
		
		try {
			onStart(result);

			final Random r = new Random();
			final int initialSequenceNumber = r.nextInt(10000);
			final CountDownLatch latch = new CountDownLatch(1);			
			final Map<Integer, RtpControlData> rtpControlDataList = new HashMap<Integer, RtpUtil.RtpControlData>();
			
			final ControlConnectionResponseCallback callback = new ControlConnectionResponseCallback() {
				
				public void onResponse(String response, String request) {
					if (response != null && response.startsWith("OK")) {
						final Matcher m = VOIP_OK_PATTERN.matcher(response);
						if (m.find()) {
							DatagramSocket dgsock = null;
							try {
								ssrc.set(Integer.parseInt(m.group(1)));
								dgsock = new DatagramSocket();
								
								final UdpStreamCallback receiveCallback = new UdpStreamCallback() {
									
									public boolean onSend(DataOutputStream dataOut, int packetNumber)
											throws IOException {
										//nothing to do here
										return true;
									}
									
									public synchronized void onReceive(DatagramPacket dp) throws IOException {
										final long receivedNs = System.nanoTime();
										final byte[] data = dp.getData();
										try {
											final RtpPacket rtp = new RtpPacket(data);
										    rtpControlDataList.put(rtp.getSequnceNumber(), new RtpControlData(rtp, receivedNs));
										} catch (RtpException e) {
											e.printStackTrace();
										}
									}

									public void onBind(Integer port)
											throws IOException {
										result.getResultMap().put(RESULT_IN_PORT, port);
									}
								};
								
								RtpUtil.runVoipStream(null, true, InetAddress.getByName(getTestServerAddr()), outgoingPort, incomingPort, sampleRate, bitsPerSample, 
										payloadType, initialSequenceNumber, ssrc.get(), 
										TimeUnit.MILLISECONDS.convert(callDuration, TimeUnit.NANOSECONDS), 
										TimeUnit.MILLISECONDS.convert(delay, TimeUnit.NANOSECONDS), 
										TimeUnit.MILLISECONDS.convert(timeout, TimeUnit.NANOSECONDS), true, receiveCallback);
							} 
							catch (InterruptedException e) {
								result.getResultMap().put(RESULT_STATUS, "TIMEOUT");
								e.printStackTrace();
							} 
							catch (TimeoutException e) {
								result.getResultMap().put(RESULT_STATUS, "TIMEOUT");
								e.printStackTrace();
							} 
							catch (Exception e) {
								result.getResultMap().put(RESULT_STATUS, "ERROR");
								e.printStackTrace();
							} 
							finally {
								if (dgsock != null && !dgsock.isClosed()) {
									dgsock.close();
								}
							}
						}
					}
					else {
						result.getResultMap().put(RESULT_STATUS, "ERROR");
					}
					
					latch.countDown();
				}
			};
			
	    	/*
	    	 * syntax: VOIPTEST 0 1 2 3 4 5 6 7 
	    	 * 	0 = outgoing port (server port)
	    	 * 	1 = incoming port (client port) 
	    	 *  2 = sample rate (in Hz)
	    	 * 	3 = bits per sample
	    	 * 	4 = packet delay in ms 
	    	 * 	5 = call duration (test duration) in ms 
	    	 * 	6 = starting sequence number (see rfc3550, rtp header: sequence number)
	    	 *  7 = payload type
	    	 */			
			sendCommand("VOIPTEST " + outgoingPort + " " + (incomingPort == null ? outgoingPort : incomingPort) + " " + sampleRate + " " + bitsPerSample + " " 
					+ TimeUnit.MILLISECONDS.convert(delay, TimeUnit.NANOSECONDS) + " " 
					+ TimeUnit.MILLISECONDS.convert(callDuration, TimeUnit.NANOSECONDS) + " " 
					+ initialSequenceNumber + " " + payloadType.getValue(), callback);
			
			//wait for countdownlatch or timeout:
			latch.await(timeout, TimeUnit.NANOSECONDS);
			
			//if rtpreceivestream did not finish cancel the task
			/*
			if (!rtpInTimeoutTask.isDone()) {
				rtpInTimeoutTask.cancel(true);
			}
			*/
			
			final CountDownLatch resultLatch = new CountDownLatch(1);
			
			final ControlConnectionResponseCallback incomingResultRequestCallback = new ControlConnectionResponseCallback() {
				
				public void onResponse(final String response, final String request) {
					if (response != null && response.startsWith("VOIPRESULT")) {
						System.out.println(response);
						Matcher m = VOIP_RECEIVE_RESPONSE_PATTERN.matcher(response);
						if (m.find()) {
							final String prefix = RESULT_VOIP_PREFIX + RESULT_OUTGOING_PREFIX;
							result.getResultMap().put(prefix + RESULT_MAX_JITTER, Long.parseLong(m.group(1)));
							result.getResultMap().put(prefix + RESULT_MEAN_JITTER, Long.parseLong(m.group(2)));
							result.getResultMap().put(prefix + RESULT_MAX_DELTA, Long.parseLong(m.group(3)));
							result.getResultMap().put(prefix + RESULT_SKEW, Long.parseLong(m.group(4)));
							result.getResultMap().put(prefix + RESULT_NUM_PACKETS, Long.parseLong(m.group(5)));
							result.getResultMap().put(prefix + RESULT_SEQUENCE_ERRORS, Long.parseLong(m.group(6)));
							result.getResultMap().put(prefix + RESULT_SHORT_SEQUENTIAL, Long.parseLong(m.group(7)));
							result.getResultMap().put(prefix + RESULT_LONG_SEQUENTIAL, Long.parseLong(m.group(8)));
						}
						resultLatch.countDown();
					}
				}
			};
			
			//wait a short amount of time until requesting results
			Thread.sleep(100);
			//request server results:
			if (ssrc.get() >= 0) {
				sendCommand("GET VOIPRESULT " + ssrc.get(), incomingResultRequestCallback);
				resultLatch.await(CONTROL_CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS);
			}

			final RtpQoSResult rtpResults = rtpControlDataList.size() > 0 ? RtpUtil.calculateQoS(rtpControlDataList, initialSequenceNumber, sampleRate) : null;
			
			final String prefix = RESULT_VOIP_PREFIX + RESULT_INCOMING_PREFIX;
			if (rtpResults != null) {
				result.getResultMap().put(prefix + RESULT_MAX_JITTER, rtpResults.getMaxJitter());
				result.getResultMap().put(prefix + RESULT_MEAN_JITTER, rtpResults.getMeanJitter());
				result.getResultMap().put(prefix + RESULT_MAX_DELTA, rtpResults.getMaxDelta());
				result.getResultMap().put(prefix + RESULT_SKEW, rtpResults.getSkew());
				result.getResultMap().put(prefix + RESULT_NUM_PACKETS, rtpResults.getReceivedPackets());
				result.getResultMap().put(prefix + RESULT_SEQUENCE_ERRORS, rtpResults.getOutOfOrder());
				result.getResultMap().put(prefix + RESULT_SHORT_SEQUENTIAL, rtpResults.getMinSequential());
				result.getResultMap().put(prefix + RESULT_LONG_SEQUENTIAL, rtpResults.getMaxSequencial());
			}
			else {
				result.getResultMap().put(prefix + RESULT_MAX_JITTER, null);
				result.getResultMap().put(prefix + RESULT_MEAN_JITTER, null);
				result.getResultMap().put(prefix + RESULT_MAX_DELTA, null);
				result.getResultMap().put(prefix + RESULT_SKEW, null);
				result.getResultMap().put(prefix + RESULT_NUM_PACKETS, 0);
				result.getResultMap().put(prefix + RESULT_SEQUENCE_ERRORS, null);
				result.getResultMap().put(prefix + RESULT_SHORT_SEQUENTIAL, null);
				result.getResultMap().put(prefix + RESULT_LONG_SEQUENTIAL, null);
			}
			
	        return result;			
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		finally {
			onEnd(result);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see at.alladin.rmbt.client.v2.task.AbstractRmbtTask#initTask()
	 */
	@Override
	public void initTask() {

	}
	
	/*
	 * (non-Javadoc)
	 * @see at.alladin.rmbt.client.v2.task.QoSTask#getTestType()
	 */
	public QoSTestResultEnum getTestType() {
		return QoSTestResultEnum.VOIP;
	}

	/*
	 * (non-Javadoc)
	 * @see at.alladin.rmbt.client.v2.task.QoSTask#needsQoSControlConnection()
	 */
	public boolean needsQoSControlConnection() {
		return true;
	}
}
