/*******************************************************************************
 * Copyright 2013-2014 alladin-IT GmbH
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

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import at.alladin.rmbt.client.QualityOfServiceTest;
import at.alladin.rmbt.client.RMBTClient;
import at.alladin.rmbt.client.v2.task.result.QoSTestResult;
import at.alladin.rmbt.client.v2.task.result.QoSTestResultEnum;

/**
 * 
 * @author lb
 *
 */
public class UdpTask extends AbstractQoSTask {
	
	private final static boolean ABORT_ON_DUPLICATE_UDP_PACKETS = false;
	
	private final static Pattern QOS_RECEIVE_RESPONSE_PATTERN = Pattern.compile("RCV ([\\d]*) ([\\d]*)");
	
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
		
		public UdpPacketData(int remotePort, int numPackets, int dupNumPackets) {
			this.remotePort = remotePort;
			this.numPackets = numPackets;
			this.dupNumPackets = dupNumPackets;
		}

		@Override
		public String toString() {
			return "UdpPacketData [remotePort=" + remotePort + ", numPackets="
					+ numPackets + ", dupNumPackets=" + dupNumPackets + "]";
		}
	}
	/**
	 * 
	 * @param taskDesc
	 */
	public UdpTask(QualityOfServiceTest nnTest, TaskDesc taskDesc, int threadId) {
		super(nnTest, taskDesc, threadId);
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
		final QoSTestResult result = initQoSTestResult(QoSTestResultEnum.UDP);
		try {
			onStart(result);

		    DatagramSocket socket = null;
		    Socket initSocket;
		    
		    int outgoingPacketsRequest = 0;
		    int incomingPacketsResponse = 0;
		    
		    boolean outgoingTimeoutReached = false;
		    boolean incomingTimeoutReached = false;
		    
		    UdpPacketData outgoingPacketData = new UdpPacketData(0,0, 0);
		    UdpPacketData incomingPacketData = null;

		    try {
				//TODO: Secure connection
		    	try {
		    		initSocket = connect(result, InetAddress.getByName(getTestServerAddr()), getTestServerPort(), 
		    			QOS_SERVER_PROTOCOL_VERSION, "ACCEPT", getQoSTest().getTestSettings().isUseSsl(), CONTROL_CONNECTION_TIMEOUT);
				}
		    	catch (IOException e) {
                    result.setFatalError(true);
                    throw e;
                }

		    	initSocket.setSoTimeout(15000);
		    	
		    	//run UDP OUT test:
		    	if (this.packetCountOutgoing != null) {
		    		
			    	/**
			    	 * method 1: get udp port range and pick a random port:
			    	 */
			    	/*
			    	sendMessage("GET UDPPORTS\n");
					String response = reader.readLine();
					
					if (response != null) {
						System.out.println("UDPTEST opened udp ports range: " + response);
						String[] ports = response.split("[ ]");
						Random rand = new Random();
						int minPort = Integer.valueOf(ports[0]);
						int maxPort = Integer.valueOf(ports[1]);
						
						outgoingPort = rand.nextInt(maxPort - minPort) + minPort; 
					}
					*/
			    	
			    	/**
			    	 * method 2: get udp port from test server / or use port from settings
			    	 */
		    		String response = null;
		    		
		    		if (outgoingPort == null) {
		    			sendMessage("GET UDPPORT\n");
		    			response = reader.readLine();
					
		    			if (response != null) {
		    				outgoingPort = Integer.valueOf(response); 
		    			}
		    		}
					
					sendMessage("UDPTEST OUT " + outgoingPort + " " + packetCountOutgoing + "\n");
					response = reader.readLine();
					if (response != null && response.startsWith("OK")) {

						Future<UdpPacketData> udpOutTimeoutTask = RMBTClient.getCommonThreadPool().submit(new Callable<UdpPacketData>() {

							public UdpPacketData call() throws Exception {
								final UdpPacketData packetData = new UdpPacketData(0, 0, 0);
								sendUdpPackets(packetData);
								return packetData;
							}
							
						});
						
						try {
							outgoingPacketData = udpOutTimeoutTask.get((int)(timeout/1000000), TimeUnit.MILLISECONDS);
							
							//wait for response. number of packets received
							response = reader.readLine();
							
							if (response != null && response.startsWith("RCV")) {
								System.out.println("UDPTASK OUT :" + outgoingPort + " -> " + response);
								
								Matcher m = QOS_RECEIVE_RESPONSE_PATTERN.matcher(response);
								if (m.find()) {
									outgoingPacketsRequest = Integer.valueOf(m.group(1));	
								}
							}
							
							if (socket != null && !socket.isClosed()) {
								socket.close(); 
							}
						}
						catch (TimeoutException e) {
							System.err.println("UDP Outgoing Timeout reached!");
							udpOutTimeoutTask.cancel(true);
							outgoingTimeoutReached = true;
						}
					}
		    	}
				
				//run UDP IN test:
				if (this.packetCountIncoming != null && this.incomingPort != null) {
					socket = new DatagramSocket(incomingPort);
					final DatagramSocket dgSocket = socket;
					sendMessage("UDPTEST IN " + incomingPort + " " + packetCountIncoming + "\n");
					socket.setSoTimeout((int)(timeout/1000000));
					
					Future<UdpPacketData> udpInTimeoutTask = RMBTClient.getCommonThreadPool().submit(new Callable<UdpPacketData>() {

						public UdpPacketData call() throws Exception {
							UdpPacketData incomingPacketData = receiveUdpPackets(dgSocket, packetCountIncoming);
							return incomingPacketData;
						}
						
					});
					
					try {
						incomingPacketData = udpInTimeoutTask.get((int)(timeout/1000000), TimeUnit.MILLISECONDS);
						
						System.out.println(incomingPacketData);
						
						String response = reader.readLine();
						
						if (response != null && response.startsWith("RCV")) {
							System.out.println("UDPTASK IN :" + incomingPort + " -> " + response);
							Matcher m = QOS_RECEIVE_RESPONSE_PATTERN.matcher(response);
							if (m.find()) {
								incomingPacketsResponse = Integer.valueOf(m.group(1));	
							}
						}
					}
					catch (TimeoutException e) {
						System.err.println("UDP Incoming Timeout reached!");
						udpInTimeoutTask.cancel(true);
						incomingTimeoutReached = true;
					}

				}				
				//close connection:
				sendMessage("QUIT\n");
				
		    }
		    catch (InterruptedException e) {
		    	e.printStackTrace();
		    }
		    catch (SocketTimeoutException e) {
		    	e.printStackTrace();
		    }
		    catch (IOException e) {
		    	e.printStackTrace();
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
	   			result.getResultMap().put(RESULT_NUM_PACKETS_OUTGOING, packetCountOutgoing);
	   			result.getResultMap().put(RESULT_PORT_OUTGOING, outgoingPort);
	   	   		result.getResultMap().put(RESULT_OUTGOING_PACKETS, outgoingPacketsRequest);
	   			result.getResultMap().put(RESULT_NUM_PACKETS_OUTGOING_RESPONSE, outgoingPacketData != null ? outgoingPacketData.numPackets : 0);
	   			
	   			/*
	   			 * METHOD 1:
	   	   			final int outgoingPackets = ((outgoingPacketData != null ? outgoingPacketData.numPackets : 0) + outgoingPacketsRequest);
	   				final int lostPackets = (packetCountOutgoing * 2) - outgoingPackets;
	   			*/
	   			
	   			
	   			//METHOD 2:
	   			final int outgoingPackets = (outgoingPacketData != null ? outgoingPacketData.numPackets : 0);
	   	   		final int lostPackets = packetCountOutgoing - outgoingPackets;
	   	   		
	   	   		System.out.println("UDP Test: outgoing all: " + outgoingPackets + ", lost: " + lostPackets);
	   	   		if (lostPackets > 0) {
	   	   			int packetLossRate = (int) (((float)lostPackets / (float)packetCountOutgoing) * 100f);
		   	   		result.getResultMap().put(RESULT_OUTGOING_PLR, String.valueOf(packetLossRate));	
	   	   		}
	   	   		else {
		   	   		result.getResultMap().put(RESULT_OUTGOING_PLR, "0");
	   	   		}
	   		}
	   		
	   		else if (this.packetCountIncoming != null && this.incomingPort != null) {
	   	   		result.getResultMap().put(RESULT_NUM_PACKETS_INCOMING, packetCountIncoming);
	   	   		result.getResultMap().put(RESULT_PORT_INCOMING, incomingPort);
	   	   		result.getResultMap().put(RESULT_INCOMING_PACKETS, incomingPacketData != null ? incomingPacketData.numPackets : 0);
	   	   		result.getResultMap().put(RESULT_NUM_PACKETS_INCOMING_RESPONSE, incomingPacketsResponse);
	   	   		/*
	   	   		 * METHOD 1:
	   	   			final int incomingPackets = ((incomingPacketData != null ? incomingPacketData.numPackets : 0) + incomingPacketsResponse);
	   	   			final int lostPackets = (packetCountIncoming * 2) - incomingPackets;
	   	   		*/
	   	   		
	   	   		//METHOD 2:
	   	   		final int incomingPackets = incomingPacketsResponse;
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
	 * @param socket
	 * @return
	 * @throws InterruptedException
	 */
	public DatagramSocket sendUdpPackets(DatagramSocket socket, int packets, int port, boolean awaitResponse, UdpPacketData packetData) throws InterruptedException {
		final TreeSet<Integer> packetsReceived = new TreeSet<Integer>();
		final TreeSet<Integer> duplicatePackets = new TreeSet<Integer>();
		
	    ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
	    DataOutputStream dataOut = new DataOutputStream(byteOut);
	    
	    byte[] data;

	    for (int i = 0; i < packets; i++) {
	    	if (Thread.interrupted()) {
	    		socket.close();
	            throw new InterruptedException();	
            }

	    	Thread.sleep((int)(delay/1000000));
	    	
	    	byteOut.reset();
	    	try {
	    		dataOut.writeByte(awaitResponse ? UDP_TEST_AWAIT_RESPONSE_IDENTIFIER : UDP_TEST_ONE_DIRECTION_IDENTIFIER);
	    		dataOut.writeByte(i);
    			dataOut.write(params.getUUID().getBytes());
    			dataOut.write(String.valueOf(System.currentTimeMillis()).getBytes());
	    	} catch (IOException e) {
	    		e.printStackTrace();
	    		socket.close();
	    		return null;
	    	}
	      	 
	    	try {

		    	data = byteOut.toByteArray();
		    	
		    	DatagramPacket packet = null;
		    	if (!socket.isConnected()) {
				    packet = new DatagramPacket(data, data.length, InetAddress.getByName(getTestServerAddr()), port);		    		
		    	}
		    	else {
		    		packet = new DatagramPacket(data, data.length);
		    	}
		    	
	    		socket.send(packet);
	    		System.out.println("UDP Test: sent packet. Udp FLAG: " + packet.getData()[0] + " #" + packet.getData()[1] + " to " +  packet.getAddress() + ":" + packet.getPort());
	    		
	    		if (awaitResponse) {
	    			try {
	    			    byte buffer[] = new byte[1024];

	    			    DatagramPacket dp = new DatagramPacket(buffer, buffer.length);
	    			    socket.setSoTimeout((int)(timeout/1000000));
	    			    socket.receive(dp);
	    				int packetNumber = buffer[1];
	    			    
	    			    System.out.println("UDP Test: received packet: #" + packetNumber + " -> " + buffer);
	    			    
	    				//check udp packet:
	    				if (buffer[0] != UDP_TEST_RESPONSE) {
	    					socket.close();
	    					throw new IOException("bad UDP IN TEST packet identifier");
	    				}
	    				
	    				//check for duplicate packets:
	    				if (packetsReceived.contains(packetNumber)) {
	    					duplicatePackets.add(packetNumber);
	    					if (ABORT_ON_DUPLICATE_UDP_PACKETS) {
	    						socket.close();
	    						throw new IOException("duplicate UDP IN TEST packet id");
	    					}
	    					else {
	    						System.out.println("duplicate UDP IN TEST packet id");
	    					}
	    				}
	    				else {
	    					packetsReceived.add(packetNumber);
	    				}
	    				
	    				if (packetData == null) {
	    					packetData = new UdpPacketData(dp.getPort(), packetsReceived.size(), duplicatePackets.size());
	    				}
	    			}
	    			catch (SocketTimeoutException e) {
	    				e.printStackTrace();
	    			}
	    		}
	    	} catch (IOException e) {
	    		e.printStackTrace();
	    		socket.close();
	    		return null;
	    	}
	    }

	    if (packetData != null && packetsReceived != null) {
	    	packetData.numPackets = packetsReceived.size();
	    }
	    return socket;
	}
	
	/**
	 * 
	 * @return
	 * @throws InterruptedException 
	 */
	public DatagramSocket sendUdpPackets(UdpPacketData packetData) throws InterruptedException {
	    DatagramSocket sock = null;
	 
	    try {
	    	//sock =  new DatagramSocket(outgoingPort);
	    	sock = new DatagramSocket();
			sock.setSoTimeout((int)(timeout/1000000));
	    } catch (Exception e) {
	    	e.printStackTrace();
			return null;
		}
	 
	    return sendUdpPackets(sock, packetCountOutgoing, outgoingPort, true, packetData);
	}
	
	/**
	 * 
	 * @param socket
	 * @return
	 * @throws InterruptedException
	 */
	public UdpPacketData receiveUdpPackets(DatagramSocket socket, int packets) throws InterruptedException {
		final TreeSet<Integer> packetsReceived = new TreeSet<Integer>();
		final TreeSet<Integer> duplicatePackets = new TreeSet<Integer>();
		int incomingCounter = 0;
		int remotePort = 0;
		
		try {			
			socket.setSoTimeout((int)(timeout/1000000));
			
			while(true) {
				
		    	if (Thread.interrupted()) {
		    		socket.close();
		            throw new InterruptedException();	
	            }
				
			    byte data[] = new byte[1024];
			    DatagramPacket packet = new DatagramPacket(data, data.length);
			    
			    System.out.println("UDP Test: waiting for incoming data on port: " + socket.getLocalPort());
			    socket.receive(packet);
				int packetNumber = data[1];
			    
			    System.out.println("UDP Test: received packet #" + packetNumber + " on port: " + socket.getLocalPort() + " -> " + data);
			    
				//check udp packet:
				if (data[0] != UDP_TEST_ONE_DIRECTION_IDENTIFIER && data[0] != UDP_TEST_AWAIT_RESPONSE_IDENTIFIER) {
					socket.close();
					throw new IOException("bad UDP IN TEST packet identifier");
				}
				
				//check for duplicate packets:
				if (packetsReceived.contains(packetNumber)) {
					duplicatePackets.add(packetNumber);
					if (ABORT_ON_DUPLICATE_UDP_PACKETS) {
						socket.close();
						throw new IOException("duplicate UDP IN TEST packet id");
					}
				}
				else {
					packetsReceived.add(packetNumber);
				    incomingCounter++;
				    
				    if (data[0] == UDP_TEST_AWAIT_RESPONSE_IDENTIFIER) {
				    	data[0] = UDP_TEST_RESPONSE;
				    	DatagramPacket dp = new DatagramPacket(data, packet.getLength(), packet.getAddress(), packet.getPort());
				    	socket.send(dp);
				    }
				}

				if (remotePort == 0 && packet != null) {
					remotePort = packet.getPort();
				}
			    
			    if (incomingCounter >= packets) {
			    	break;
			    }
			}			
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			if (socket != null && !socket.isClosed()) {
				socket.close();
			}
		}

		return new UdpPacketData(remotePort, packetsReceived.size(), duplicatePackets.size());
	}
	
	/*
	 * (non-Javadoc)
	 * @see at.alladin.rmbt.client.v2.task.QoSTask#getTestType()
	 */
	public QoSTestResultEnum getTestType() {
		return QoSTestResultEnum.UDP;
	}
}
