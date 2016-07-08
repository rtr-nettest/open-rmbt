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
package at.alladin.rmbt.qos.testserver.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.text.DateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

import at.alladin.rmbt.qos.testserver.ServerPreferences.TestServerServiceEnum;
import at.alladin.rmbt.qos.testserver.servers.AbstractUdpServer;
import at.alladin.rmbt.qos.testserver.TestServer;
import at.alladin.rmbt.qos.testserver.util.TestServerConsole;
import at.alladin.rmbt.util.net.rtp.RealtimeTransportProtocol.RtpVersion;
import at.alladin.rmbt.util.net.rtp.RtpUtil;

/**
 * 
 * @author lb
 *
 */
public class UdpMultiClientServer extends AbstractUdpServer<DatagramSocket> implements Runnable {

	private final static String TAG = UdpMultiClientServer.class.getCanonicalName();
		
	private final AtomicBoolean isRunning;
	
	public final static int BUFFER_LENGTH = 1024;

	final InetAddress address;
	
	final int port;
	
	private final String name;
	
	private long lastClientTime = 0;
	
	protected final DatagramSocket socket;
	
	/**
	 * 
	 * @param port
	 * @param timeOut
	 * @throws Exception 
	 */
	public UdpMultiClientServer(int port, InetAddress address) throws Exception {
		super(DatagramSocket.class);
		TestServerConsole.log("Initializing " + TAG + " on " + address + ":" + port, 1, TestServerServiceEnum.TEST_SERVER);
		//this.socket = new DatagramSocket(port, TestServer.serverPreferences.getInetAddrBindTo());
		this.socket = TestServer.createDatagramSocket(port, address);
		this.port = port;
		this.isRunning = new AtomicBoolean(false);
		this.address = address;
		this.name = "UdpMultiClientServer [" + address + ":" + port + "]";
	}
	
	/**
	 * 
	 * @param socket
	 */
	public UdpMultiClientServer(DatagramSocket socket) {
		super(DatagramSocket.class);
		this.socket = socket;
		this.port = socket.getLocalPort();
		this.address = socket.getLocalAddress();
		this.isRunning = new AtomicBoolean(false);
		this.name = "UdpMultiClientServer [" + address + ":" + socket.getLocalPort() + "]";
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		isRunning.set(true);

		TestServerConsole.log("Starting " + TAG + " on address: " + socket.getLocalAddress() + ":" + socket.getLocalPort() + " ...", 2, TestServerServiceEnum.UDP_SERVICE);
		
		try {
			while (isRunning.get()) {
				byte[] buffer = new byte[BUFFER_LENGTH];
				final DatagramPacket dp = new DatagramPacket(buffer, BUFFER_LENGTH);				
				socket.receive(dp);
				
				//set last client timestamp
				lastClientTime = System.currentTimeMillis();
			
				final byte[] data = dp.getData();
				
				final RtpVersion rtpVersion = RtpUtil.getVersion(data[0]);
				
				String clientUuid = null;
				
				if (!RtpVersion.VER2.equals(rtpVersion)) {
					//Non RTP packet:
					final int packetNumber = data[1];
					
					String timeStamp = null;
					
					try {
						char[] uuid = new char[36];
						
						for (int i = 2; i < 38; i++) {
							uuid[i - 2] = (char) data[i];
						}
						clientUuid = String.valueOf(uuid);
						
						char[] ts = new char[dp.getLength() - 38];
						for (int i = 38; i < dp.getLength(); i++) {
							ts[i - 38] = (char) data[i];
						}
						
						timeStamp = String.valueOf(ts);
	
					}
					catch (Exception e) {
						TestServerConsole.error(getName(), e, 1, TestServerServiceEnum.UDP_SERVICE);
					}
					
					TestServerConsole.log("received UDP from: " + dp.getAddress().toString() + ":" + dp.getPort() 
							+ " (on local port :" + socket.getLocalPort() + ") , #" + packetNumber + " TimeStamp: " + timeStamp + ", containing: " + clientUuid, 1, TestServerServiceEnum.UDP_SERVICE);
					
				}
				else {
					//RtpPacket received:
					clientUuid = "VOIP_" + RtpUtil.getSsrc(data);
				}
				
				if (clientUuid != null) {
					synchronized (incomingMap) {
						final UdpTestCandidate clientData;
						final String uuid = clientUuid;
						if (!incomingMap.containsKey(clientUuid)) {
							clientData = new UdpTestCandidate();
							clientData.setNumPackets(Integer.MAX_VALUE);
							clientData.setRemotePort(dp.getPort());
							incomingMap.put(clientUuid, clientData);
						}
						else {
							clientData = (UdpTestCandidate) incomingMap.get(clientUuid);
							if (clientData.isError()) {
								continue;
							}
						}
						
						//if a callback has been provided by the clienthandler run it in the background:
						if (clientData.getOnUdpPacketReceivedCallback() != null) {
							Runnable onReceiveRunnable = new Runnable() {
								
								@Override
								public void run() {
									clientData.getOnUdpPacketReceivedCallback().onReceive(dp, uuid, UdpMultiClientServer.this);									
								}
							};
							
							TestServer.getCommonThreadPool().submit(onReceiveRunnable);
						}						
					}
				}
			}		
		} 
		catch (IOException e) {
			TestServerConsole.error(getName(), e, 0, TestServerServiceEnum.UDP_SERVICE);
		}
		finally {
			if (!socket.isClosed()) {
				socket.close();
			}
		}
		
		TestServerConsole.log("UdpMultiServer shutdown on port: " + socket.getLocalPort(), 1, TestServerServiceEnum.UDP_SERVICE);
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean getIsRunning() {
		return isRunning.get();
	}

	/**
	 * 
	 */
	public void quit() {
		isRunning.set(false);
		TestServerConsole.log("UdpServer received quit command on port: " + socket.getLocalPort(), 1, TestServerServiceEnum.UDP_SERVICE);
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "UdpMultiClientServer [isRunning=" + isRunning + ", socket="
				+ socket + ", address=" + address + ", incomingMap="
				+ incomingMap + "]" + toStringExtra();
	}
	
	/**
	 * 
	 * @return
	 */
	public String toStringExtra() {
		return " \n\t Socket additional info [local port=" + socket.getLocalPort() + ", connected=" 
				+ socket.isConnected() + ", bound=" + socket.isBound() + ", closed=" + socket.isClosed() +"]";
	}
	
	/**
	 * 
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * 
	 * @return
	 */
	public InetAddress getAddress() {
		return address;
	}
	
	/**
	 * 
	 */
	public int getLocalPort() {
		return port;
	}

	/*
	 * (non-Javadoc)
	 * @see at.alladin.rmbt.qos.testserver.udp.AbstractCandidateHandler#getSocket()
	 */
	@Override
	public DatagramSocket getSocket() {
		return socket;
	}

	/*
	 * (non-Javadoc)
	 * @see at.alladin.rmbt.qos.testserver.udp.AbstractUdpServer#send(java.net.DatagramPacket)
	 */
	@Override
	public void send(DatagramPacket dp) throws IOException {
		socket.send(dp);
	}

	/*
	 * (non-Javadoc)
	 * @see at.alladin.rmbt.qos.testserver.udp.AbstractUdpServer#isHealthy()
	 */
	@Override
	public boolean isHealthy() {
		return socket != null && !socket.isClosed();
	}

	/*
	 * (non-Javadoc)
	 * @see at.alladin.rmbt.qos.testserver.entity.Observable#getStatusMessage()
	 */
	@Override
	public String getStatusMessage() {
		return "Last client timestamp: " + DateFormat.getDateTimeInstance().format(new Date(lastClientTime));
	}
}
