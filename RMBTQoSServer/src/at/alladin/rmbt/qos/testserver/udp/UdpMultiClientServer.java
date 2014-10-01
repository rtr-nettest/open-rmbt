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
package at.alladin.rmbt.qos.testserver.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import at.alladin.rmbt.qos.testserver.ServerPreferences.TestServerServiceEnum;
import at.alladin.rmbt.qos.testserver.TestServer;
import at.alladin.rmbt.qos.testserver.util.TestServerConsole;

/**
 * 
 * @author lb
 *
 */
public class UdpMultiClientServer implements Runnable {
	
	private final AtomicBoolean isRunning;
	
	public final static int BUFFER_LENGTH = 1024;

	final DatagramSocket socket;
	
	final InetAddress address;
	
	private ConcurrentHashMap<String, ClientUdpData> incomingMap;
	
	/**
	 * 
	 * @param port
	 * @param timeOut
	 * @throws Exception 
	 */
	public UdpMultiClientServer(int port, InetAddress address) throws Exception {
		TestServerConsole.log("Initializing UdpMultiServer on " + address + ":" + port, 1, TestServerServiceEnum.TEST_SERVER);
		//this.socket = new DatagramSocket(port, TestServer.serverPreferences.getInetAddrBindTo());
		this.socket = TestServer.createDatagramSocket(port, address);
		this.isRunning = new AtomicBoolean(false);
		this.incomingMap = new ConcurrentHashMap<>();
		this.address = address;
	}
	
	/**
	 * 
	 * @param socket
	 */
	public UdpMultiClientServer(DatagramSocket socket) {
		this.socket = socket;
		this.address = socket.getLocalAddress();
		this.isRunning = new AtomicBoolean(false);
		this.incomingMap = new ConcurrentHashMap<>();		
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		isRunning.set(true);

		TestServerConsole.log("Starting UdpMultiServer on address: " + socket.getLocalAddress() + ":" + socket.getLocalPort() + " ...", 2, TestServerServiceEnum.UDP_SERVICE);
		
		try {
			while (isRunning.get()) {
				byte[] buffer = new byte[BUFFER_LENGTH];
				DatagramPacket dp = new DatagramPacket(buffer, BUFFER_LENGTH);
				
				socket.receive(dp);
				
				synchronized(this) {
					byte[] data = dp.getData();
					
					final int packetNumber = data[1];
					
					String clientUuid = null;
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
						e.printStackTrace();
					}
					
					TestServerConsole.log("received UDP from: " + dp.getAddress().toString() + ":" + dp.getPort() 
							+ " (on local port :" + socket.getLocalPort() + ") , #" + packetNumber + " TimeStamp: " + timeStamp + ", containing: " + clientUuid, 1, TestServerServiceEnum.UDP_SERVICE);
					
					if (clientUuid != null) {
						//synchronized (incomingMap) {
							ClientUdpData clientData;
							if (!incomingMap.containsKey(clientUuid)) {
								clientData = new ClientUdpData(Integer.MAX_VALUE);
								clientData.setRemotePort(dp.getPort());
								incomingMap.put(clientUuid, clientData);
							}
							else {
								clientData = incomingMap.get(clientUuid);
								if (clientData.isError()) {
									continue;
								}
							}
							
							if (clientData.getOnUdpPacketReceivedCallback() != null) {
								clientData.getOnUdpPacketReceivedCallback().onReceive(dp, this);
							}
							
							
							if (clientData.getPacketsReceived().size() >= clientData.getNumPackets() 
									&& clientData.getOnUdpTestCompleteCallback() != null) {
								if (clientData.getOnUdpTestCompleteCallback().onComplete(clientData)) {
									pollClientData(clientUuid);
								}
							}
						//}
					}
				}
			}		
		} 
		catch (IOException e) {
			e.printStackTrace();
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
	
	/**
	 * 
	 * @param uuid
	 */
	public synchronized ClientUdpData getClientData(String uuid) {
		return incomingMap.get(uuid);
	}
	
	/**
	 * 
	 * @param uuid
	 * @return
	 */
	public synchronized ClientUdpData pollClientData(String uuid) {
		return incomingMap.remove(uuid);
	}

	/**
	 * 
	 * @return
	 */
	public synchronized ConcurrentHashMap<String, ClientUdpData> getIncomingMap() {
		return incomingMap;
	}

	/**
	 * 
	 * @return
	 */
	public DatagramSocket getDatagramSocket() {
		return socket;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "UdpMultiClientServer [isRunning=" + isRunning + ", socket="
				+ socket + ", address=" + address + ", incomingMap="
				+ incomingMap + "]";
	}
	
	/**
	 * 
	 * @return
	 */
	public String toStringExtra() {
		return " // Socket additional info [local port=" + socket.getLocalPort() + ", connected=" 
				+ socket.isConnected() + ", bound=" + socket.isBound() + ", closed=" + socket.isClosed() +"]";
	}

	/**
	 * 
	 * @return
	 */
	public InetAddress getAddress() {
		return address;
	}
}
