/*******************************************************************************
 * Copyright 2013-2019 alladin-IT GmbH
 * Copyright 2013-2016 Rundfunk und Telekom Regulierungs-GmbH (RTR-GmbH)
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
package at.rtr.rmbt.client.v2.task;

import at.rtr.rmbt.client.RMBTClient;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.channels.DatagramChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import at.rtr.rmbt.client.helper.Globals;
import at.rtr.rmbt.shared.qos.QosMeasurementType;
import at.rtr.rmbt.shared.qos.UdpPayload;
import at.rtr.rmbt.shared.qos.util.UdpPayloadUtil;
import at.rtr.rmbt.client.QualityOfServiceTest;
import at.rtr.rmbt.client.v2.task.result.QoSTestResult;
import at.rtr.rmbt.util.net.udp.NioUdpStreamSender;
import at.rtr.rmbt.util.net.udp.StreamSender.UdpStreamCallback;
import at.rtr.rmbt.util.net.udp.StreamSender.UdpStreamSenderSettings;
import at.rtr.rmbt.util.net.udp.UdpStreamReceiver;
import at.rtr.rmbt.util.net.udp.UdpStreamReceiver.UdpStreamReceiverSettings;

/**
 *
 * @author lb
 *
 */
public class UdpTask extends AbstractQoSTask {

	private final static boolean ABORT_ON_DUPLICATE_UDP_PACKETS = false;

	private final static Pattern QOS_RECEIVE_RESPONSE_PATTERN = Pattern.compile("RCV ([\\d]*) ([\\d]*)( (\\{.*\\}))?");

	private final Integer packetCountIncoming;

	private final Integer packetCountOutgoing;

	private Integer outgoingPort;

	private final Integer incomingPort;

	private final long timeout;

	private final long delay;

	private final static long DEFAULT_TIMEOUT = 3000000000L;

	private final static long DEFAULT_DELAY = 300000000L;

	private final static int UDP_TEST_ONE_DIRECTION_IDENTIFIER = 1;

	private final static int UDP_TEST_AWAIT_RESPONSE_IDENTIFIER = 3;

	private final static int UDP_TEST_RESPONSE = 2;

	public final static String PARAM_NUM_PACKETS_INCOMING = "in_num_packets";

	public final static String PARAM_NUM_PACKETS_OUTGOING = "out_num_packets";

	public final static String PARAM_PORT = "in_port";

	public final static String PARAM_PORT_OUT = "out_port";

	public final static String PARAM_TIMEOUT = "timeout";

	public final static String PARAM_DELAY = "delay";

	public final static String RESULT_OUTGOING_RTTS = "udp_result_out_rtts_ns";

	public final static String RESULT_OUTGOING_RTT_AVG = "udp_result_out_rtt_avg_ns";

	public final static String RESULT_INCOMING_RTTS = "udp_result_in_rtts_ns";

	public final static String RESULT_INCOMING_RTT_AVG = "udp_result_in_rtt_avg_ns";

	public final static String RESULT_OUTGOING_PACKETS = "udp_result_out_num_packets";

	public final static String RESULT_INCOMING_PACKETS = "udp_result_in_num_packets";

	public final static String RESULT_INCOMING_PLR = "udp_result_in_packet_loss_rate";

	public final static String RESULT_NUM_PACKETS_INCOMING_RESPONSE = "udp_result_in_response_num_packets";

	public final static String RESULT_OUTGOING_PLR = "udp_result_out_packet_loss_rate";

	public final static String RESULT_NUM_PACKETS_OUTGOING_RESPONSE = "udp_result_out_response_num_packets";

	public final static String RESULT_PORT_OUTGOING = "udp_objective_out_port";

	public final static String RESULT_PORT_INCOMING = "udp_objective_in_port";

	public final static String RESULT_NUM_PACKETS_INCOMING = "udp_objective_in_num_packets";

	public final static String RESULT_NUM_PACKETS_OUTGOING = "udp_objective_out_num_packets";

	public final static String RESULT_DELAY = "udp_objective_delay";

	public final static String RESULT_TIMEOUT = "udp_objective_timeout";


	/**
	 *
	 * @author lb
	 *
	 */
	public static class UdpPacketData {
		int remotePort;
		int numPackets;
		int dupNumPackets;
		int rcvServerResponse;
		TreeMap<Integer, Long> rtts = new TreeMap<>();

		public UdpPacketData(int remotePort, int numPackets, int dupNumPackets) {
			this.remotePort = remotePort;
			this.numPackets = numPackets;
			this.dupNumPackets = dupNumPackets;
			this.rcvServerResponse = 0;
		}

		@Override
		public String toString() {
			return "UdpPacketData [remotePort=" + remotePort + ", numPackets="
					+ numPackets + ", dupNumPackets=" + dupNumPackets
					+ ", rcvServerResponse=" + rcvServerResponse + "]";
		}
	}
	/**
	 *
	 * @param taskDesc
	 */
	public UdpTask(QualityOfServiceTest nnTest, TaskDesc taskDesc, int threadId) {
		super(nnTest, taskDesc, threadId, threadId);
		String value = (String) taskDesc.getParams().get(PARAM_NUM_PACKETS_INCOMING);
		this.packetCountIncoming = value != null ? Integer.valueOf(value) : null;

		value = (String) taskDesc.getParams().get(PARAM_NUM_PACKETS_OUTGOING);
		this.packetCountOutgoing = value != null ? Integer.valueOf(value) : null;

		value = (String) taskDesc.getParams().get(PARAM_PORT);
		this.incomingPort = value != null ? Integer.valueOf(value) : null;

		value = (String) taskDesc.getParams().get(PARAM_PORT_OUT);
		this.outgoingPort = value != null ? Integer.valueOf(value) : null;

		value = (String) taskDesc.getParams().get(PARAM_TIMEOUT);
		this.timeout = value != null ? Long.valueOf(value) : DEFAULT_TIMEOUT;

		value = (String) taskDesc.getParams().get(PARAM_DELAY);
		this.delay = value != null ? Long.valueOf(value) : DEFAULT_DELAY;
	}

	/**
	 *
	 */
	public QoSTestResult call() throws Exception {
		final QoSTestResult result = initQoSTestResult(QosMeasurementType.UDP);
		try {
			onStart(result);

			DatagramSocket socket = null;

			final UdpPacketData outgoingPacketData = new UdpPacketData(0, 0, 0);
			final UdpPacketData incomingPacketData = new UdpPacketData(0, 0, 0);

			try {
				final CountDownLatch outgoingLatch = new CountDownLatch(1);

				//run UDP OUT test:
				if (this.packetCountOutgoing != null) {

					ControlConnectionResponseCallback outgoingRequestCallback = new ControlConnectionResponseCallback() {
						public void onResponse(final String response, final String request) {
							try {
								if (request.startsWith("GET UDPPORT")) {
									if (response != null && !response.startsWith("ERR")) {
										outgoingPort = Integer.valueOf(response);
										sendCommand("UDPTEST OUT " + outgoingPort + " " + packetCountOutgoing, this);
									}
								}
								else if (request.startsWith("UDPTEST OUT")) {
									if (response != null && response.startsWith("OK")) {

										Future<UdpPacketData> udpOutTimeoutTask = RMBTClient.getCommonThreadPool().submit(new Callable<UdpPacketData>() {

											public UdpPacketData call() throws Exception {
												sendUdpPackets(outgoingPacketData);
												return outgoingPacketData;
											}

										});

										try {
											udpOutTimeoutTask.get(timeout, TimeUnit.NANOSECONDS);
										}
										catch (Exception e) {
											System.err.println("UDP Outgoing Timeout reached!");
											e.printStackTrace();
											udpOutTimeoutTask.cancel(true);
										}

										outgoingLatch.countDown();
									}
								}
							}
							catch (Exception e) {
								e.printStackTrace();
							}
						}
					};

					if (outgoingPort == null) {
						sendCommand("GET UDPPORT", outgoingRequestCallback);
					}
					else {
						sendCommand("UDPTEST OUT " + outgoingPort + " " + packetCountOutgoing, outgoingRequestCallback);
					}

					if (!outgoingLatch.await(timeout, TimeUnit.NANOSECONDS)) {
						if(Globals.DEBUG_CLI) 
							System.out.println("OUT " + outgoingPort + " TIMEOUT REACHED: " + outgoingPacketData);
					}

					//request results;
					final CountDownLatch outgoingResultLatch = new CountDownLatch(1);
					final ControlConnectionResponseCallback outgoingResultRequestCallback = new ControlConnectionResponseCallback() {

						public void onResponse(final String response, final String request) {
							if (response != null && response.startsWith("RCV")) {
								if(Globals.DEBUG_CLI) 
									System.out.println("UDPTASK OUT :" + outgoingPort + " -> " + response);

								Matcher m = QOS_RECEIVE_RESPONSE_PATTERN.matcher(response);
								if (m.find()) {
									outgoingPacketData.rcvServerResponse = Integer.valueOf(m.group(1));
								}

								outgoingResultLatch.countDown();
							}
						}
					};

					sendCommand("GET UDPRESULT OUT " + outgoingPort, outgoingResultRequestCallback);
					outgoingResultLatch.await(CONTROL_CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS);

				}

				//run UDP IN test:
				if (this.packetCountIncoming != null && this.incomingPort != null) {
					socket = new DatagramSocket(incomingPort);
					final DatagramSocket dgSocket = socket;
					sendCommand("UDPTEST IN " + incomingPort + " " + packetCountIncoming, null);
					socket.setSoTimeout((int)(timeout/1000000));
					
					Future<UdpPacketData> udpInTimeoutTask = RMBTClient.getCommonThreadPool().submit(new Callable<UdpPacketData>() {

						public UdpPacketData call() throws Exception {
							receiveUdpPackets(dgSocket, packetCountIncoming, incomingPacketData);
							return incomingPacketData;
						}

					});

					try {
						udpInTimeoutTask.get(timeout, TimeUnit.NANOSECONDS);
					}
					catch (TimeoutException e) {
						System.err.println("UDP Incoming Timeout reached!");
						udpInTimeoutTask.cancel(true);
					}

					final CountDownLatch incomingLatch = new CountDownLatch(1);
					final ControlConnectionResponseCallback incomingResultRequestCallback = new ControlConnectionResponseCallback() {

						public void onResponse(final String response, final String request) {
							if (response != null && response.startsWith("RCV")) {
								if(Globals.DEBUG_CLI) 
									System.out.println("UDPTASK IN :" + incomingPort + " -> " + response);
								Matcher m = QOS_RECEIVE_RESPONSE_PATTERN.matcher(response);
								if (m.find()) {
									incomingPacketData.rcvServerResponse = Integer.valueOf(m.group(1));
									if (m.groupCount()>=4) {
										final String json = m.group(3);
										if (json != null) {
											try {
												final Gson gson = new Gson();
												final Type mapType = new TypeToken<Map<Integer,Long>>() {}.getType();
												incomingPacketData.rtts.putAll((Map<? extends Integer, ? extends Long>) gson.fromJson(json, mapType));
											}
											catch (final Exception e) {
												e.printStackTrace();
											}
										}
									}
								}
								incomingLatch.countDown();
							}
						}
					};

					//wait a short amount of time until requesting results
					Thread.sleep(150);
					//request server results:
					sendCommand("GET UDPRESULT IN " + incomingPort, incomingResultRequestCallback);
					incomingLatch.await(CONTROL_CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS);
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			finally {
				if (socket != null && socket.isConnected()) {
					socket.close();
				}
			}

			if (this.packetCountOutgoing != null) {
				if(Globals.DEBUG_CLI) 
					System.out.println("OUT " + outgoingPort + ": " + outgoingPacketData);
				result.getResultMap().put(RESULT_NUM_PACKETS_OUTGOING, packetCountOutgoing);
				result.getResultMap().put(RESULT_PORT_OUTGOING, outgoingPort);
				result.getResultMap().put(RESULT_OUTGOING_PACKETS, outgoingPacketData != null ? outgoingPacketData.rcvServerResponse : 0);
				result.getResultMap().put(RESULT_NUM_PACKETS_OUTGOING_RESPONSE, outgoingPacketData != null ? outgoingPacketData.numPackets : 0);

				final Map<String, Long> rtts = new HashMap<>();
				long rttAvg = 0L;

				if (outgoingPacketData.rtts.size() > 0) {
					for (Entry<Integer, Long> e : outgoingPacketData.rtts.entrySet()) {
						rtts.put(String.valueOf(e.getKey()), e.getValue());
						rttAvg += e.getValue();
					}

					result.getResultMap().put(RESULT_OUTGOING_RTT_AVG, (rttAvg / outgoingPacketData.rtts.size()));
					result.getResultMap().put(RESULT_OUTGOING_RTTS, rtts);
				}

				final int outgoingPackets = (outgoingPacketData != null ? outgoingPacketData.numPackets : 0);
				final int lostPackets = packetCountOutgoing - outgoingPackets;

				if(Globals.DEBUG_CLI) 
					System.out.println("UDP Test: outgoing all: " + outgoingPackets + ", lost: " + lostPackets);
				if (lostPackets > 0) {
					int packetLossRate = (int) (((float)lostPackets / (float)packetCountOutgoing) * 100f);
					result.getResultMap().put(RESULT_OUTGOING_PLR, String.valueOf(packetLossRate));
				}
				else {
					result.getResultMap().put(RESULT_OUTGOING_PLR, "0");
				}
			}

			if (this.packetCountIncoming != null && this.incomingPort != null) {
				if(Globals.DEBUG_CLI) 
					System.out.println("IN " + incomingPort + ": " + incomingPacketData);
				final int incomingPackets = incomingPacketData != null ? incomingPacketData.rcvServerResponse : 0;

				result.getResultMap().put(RESULT_NUM_PACKETS_INCOMING, packetCountIncoming);
				result.getResultMap().put(RESULT_PORT_INCOMING, incomingPort);
				result.getResultMap().put(RESULT_INCOMING_PACKETS, incomingPacketData != null ? incomingPacketData.numPackets : 0);
				result.getResultMap().put(RESULT_NUM_PACKETS_INCOMING_RESPONSE, incomingPackets);

				final Map<String, Long> rtts = new HashMap<>();
				long rttAvg = 0L;

				if (incomingPacketData.rtts.size() > 0) {
					for (Entry<Integer, Long> e : incomingPacketData.rtts.entrySet()) {
						rtts.put(String.valueOf(e.getKey()), e.getValue());
						rttAvg += e.getValue();
					}

					result.getResultMap().put(RESULT_INCOMING_RTT_AVG, (rttAvg / incomingPacketData.rtts.size()));
					result.getResultMap().put(RESULT_INCOMING_RTT_AVG, rtts);
				}


				final int lostPackets = packetCountIncoming - incomingPackets;
				if (lostPackets > 0) {
					int packetLossRate = (int) (((float)lostPackets / (float)packetCountIncoming) * 100f);
					result.getResultMap().put(RESULT_INCOMING_PLR, String.valueOf(packetLossRate));
				}
				else {
					result.getResultMap().put(RESULT_INCOMING_PLR, "0");
				}
			}

			result.getResultMap().put(RESULT_DELAY, delay);
			result.getResultMap().put(RESULT_TIMEOUT, timeout);

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

	/**
	 *
	 * @return
	 * @throws Exception
	 */
	public DatagramChannel sendUdpPackets(final UdpPacketData packetData) throws Exception {
		final UdpStreamSenderSettings<DatagramChannel> udpSettings = new UdpStreamSenderSettings<DatagramChannel>(null, true,
				InetAddress.getByName(getTestServerAddr()), outgoingPort, packetCountOutgoing, delay,
				timeout, TimeUnit.NANOSECONDS, false, 10000);

		final NioUdpStreamSender udpStreamSender = new NioUdpStreamSender(udpSettings, new UdpStreamCallback() {
			final TreeSet<Integer> packetsReceived = new TreeSet<Integer>();
			final TreeSet<Integer> duplicatePackets = new TreeSet<Integer>();
			final TreeMap<Integer, Long> rttMap = new TreeMap<>();

			public boolean onSend(DataOutputStream dataOut, int packetNumber, byte[] receivedPayload) throws IOException {

				if(Globals.DEBUG_CLI) 
					System.out.println("UDP OUT Test: sending packet #" + packetNumber);
//	    		dataOut.writeByte(UDP_TEST_AWAIT_RESPONSE_IDENTIFIER);
//	    		dataOut.writeByte(packetNumber);
//    			dataOut.write(params.getUUID().getBytes());
//    			dataOut.write(String.valueOf(System.currentTimeMillis()).getBytes());

				final UdpPayload udpPayload = new UdpPayload();
				udpPayload.setCommunicationFlag(UDP_TEST_AWAIT_RESPONSE_IDENTIFIER);
				udpPayload.setPacketNumber(packetNumber);
				udpPayload.setUuid(params.getUUID());
				udpPayload.setTimestamp(System.nanoTime());

				dataOut.write(UdpPayloadUtil.toBytes(udpPayload));
				return true;
			}

			public synchronized void onReceive(final DatagramPacket dp) throws IOException {
				final byte[] buffer = dp.getData();
				final UdpPayload udpPayload = UdpPayloadUtil.toUdpPayload(buffer);
				int packetNumber = udpPayload.getPacketNumber();
				final long rtt = (System.nanoTime() - udpPayload.getTimestamp());

				if(Globals.DEBUG_CLI) 
					System.out.println("UDP OUT Test: received packet: #" + packetNumber + " (RTT: " + rtt  + "ms) -> " + buffer);
				//check udp packet:
				if (buffer[0] != UDP_TEST_RESPONSE) {
					udpSettings.getSocket().close();
					throw new IOException("bad UDP IN TEST packet identifier");
				}

				//check for duplicate packets:
				if (packetsReceived.contains(packetNumber)) {
					duplicatePackets.add(packetNumber);
					if (ABORT_ON_DUPLICATE_UDP_PACKETS) {
						udpSettings.getSocket().close();
						throw new IOException("duplicate UDP IN TEST packet id");
					}
					else {
						if(Globals.DEBUG_CLI) 
							System.out.println("duplicate UDP IN TEST packet id");
					}
				}
				else {
					packetsReceived.add(packetNumber);
					rttMap.put(packetNumber, rtt);
				}

				packetData.numPackets = packetsReceived.size();
				packetData.dupNumPackets = duplicatePackets.size();
				packetData.rtts = rttMap;
			}

			public void onBind(Integer port) throws IOException {
				// TODO Auto-generated method stub

			}
		});

		return udpStreamSender.send();
	}

	/**
	 *
	 * @param socket
	 * @return
	 * @throws InterruptedException
	 */
	public void receiveUdpPackets(final DatagramSocket socket, int packets, final UdpPacketData packetData) throws InterruptedException {
		final TreeSet<Integer> packetsReceived = new TreeSet<Integer>();
		final TreeSet<Integer> duplicatePackets = new TreeSet<Integer>();
		final ConcurrentHashMap<Integer, Long> rttMap = new ConcurrentHashMap<Integer, Long>();

		try {
			final int timeOutMs = (int) TimeUnit.MILLISECONDS.convert(timeout, TimeUnit.NANOSECONDS);
			socket.setSoTimeout(timeOutMs);

			final UdpStreamReceiverSettings settings = new UdpStreamReceiverSettings(socket, packets, true);

			final UdpStreamReceiver udpStreamReceiver = new UdpStreamReceiver(settings, new UdpStreamCallback() {

				public boolean onSend(DataOutputStream dataOut, int packetNumber, byte[] receivedPayload)
						throws IOException {
					if(Globals.DEBUG_CLI) 
						System.out.println("UDP IN SEND #" + (packetNumber-1) + " -> " + rttMap);

					// only update the previously received payload
					final UdpPayload udpPayload = UdpPayloadUtil.toUdpPayload(receivedPayload);
					udpPayload.setCommunicationFlag(UDP_TEST_RESPONSE);
					dataOut.write(UdpPayloadUtil.toBytes(udpPayload));

					return true;
				}

				public void onReceive(DatagramPacket dp) throws IOException {
					final byte[] data = dp.getData();
					final UdpPayload udpPayload = UdpPayloadUtil.toUdpPayload(data);
					final int packetNumber = udpPayload.getPacketNumber();

					if(Globals.DEBUG_CLI) 
						System.out.println("UDP IN Test: received packet #" + packetNumber + " on port: " + socket.getLocalPort() + " -> " + udpPayload);

					//check udp packet:
					if (data[0] != UDP_TEST_ONE_DIRECTION_IDENTIFIER && data[0] != UDP_TEST_AWAIT_RESPONSE_IDENTIFIER) {
						throw new IOException("bad UDP IN TEST packet identifier");
					}

					//check for duplicate packets:
					if (packetsReceived.contains(packetNumber)) {
						duplicatePackets.add(packetNumber);
						if (ABORT_ON_DUPLICATE_UDP_PACKETS) {
							throw new IOException("duplicate UDP IN TEST packet id");
						}
					}
					else {
						packetsReceived.add(packetNumber);
						rttMap.put(packetNumber, udpPayload.getTimestamp());
					}

					packetData.dupNumPackets = duplicatePackets.size();
					packetData.numPackets = packetsReceived.size();

					if(Globals.DEBUG_CLI) 
						System.out.println("UDP IN FINISHED ON RECEIVE");
				}

				public void onBind(Integer port) throws IOException {
					// TODO Auto-generated method stub

				}
			});


			udpStreamReceiver.receive();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			if (socket != null && !socket.isClosed()) {
				socket.close();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see at.alladin.rmbt.client.v2.task.QoSTask#getTestType()
	 */
	public QosMeasurementType getTestType() {
		return QosMeasurementType.UDP;
	}

	/*
	 * (non-Javadoc)
	 * @see at.alladin.rmbt.client.v2.task.QoSTask#needsQoSControlConnection()
	 */
	public boolean needsQoSControlConnection() {
		return true;
	}
}
