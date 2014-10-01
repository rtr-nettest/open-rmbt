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
package at.alladin.rmbt.qos.testserver.tcp;

import java.io.BufferedReader;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import at.alladin.rmbt.qos.testserver.ServerPreferences.TestServerServiceEnum;
import at.alladin.rmbt.qos.testserver.TestServer;
import at.alladin.rmbt.qos.testserver.util.TestServerConsole;

/**
 * 
 * @author lb
 *
 */
public class TcpServer implements Runnable {
	
	/**
	 * the ttl of this socket
	 */
	public final static long TTL = 30000;
	
	/**
	 * needed verbose level of resgister/remove candidate debug output 
	 */
	public final static int VERBOSE_LEVEL_REGISTER_REMOVE_CANDIDATE = 0;
	
	/**
	 * needed verbose level of request/response debug output
	 */
	public final static int VERBOSE_LEVEL_REQUEST_RESPONSE = 0;
	
	/**
	 * if set to true the socket will be closed and unbound every time a test has finished and there are no active connections  
	 */
	public final static boolean CLOSE_CONNECTION_IF_NO_CLIENTS_LEFT = true;
	
	/**
	 * closes the connection if the TTL has been reached
	 */
	public final static boolean CLOSE_CONNECTION_IF_TTL_REACHED = true;

	/**
	 * holds the candidates for this socket
	 */
	private final Map<InetAddress, TcpTestCandidate> candidateMap = new HashMap<>();
	
	/**
	 * 
	 */
	private final AtomicLong ttlTimestamp = new AtomicLong(0);
	
	/**
	 * socket SO timeout
	 */
	public final static int TIMEOUT = 15000; 
	
	/**
	 * the socket of this tcp server
	 */
	private ServerSocket socket;
	
	/**
	 * indicates if this socket can be used for NTP tests (=no ssl)
	 */
	private final boolean isNtpSocket;
	
	/**
	 * the port of this socket
	 */
	private final int port;
	
	/**
	 * 
	 */
	private final InetAddress addr;
	
	/**
	 * counts the active connections
	 */
	private final AtomicLong currentConnections = new AtomicLong(0); 
		
	/**
	 * 
	 * @param port
	 * @throws IOException
	 */
	public TcpServer(int port, boolean isNtpSocket, InetAddress addr) throws IOException {
		this.isNtpSocket = isNtpSocket;
		this.port = port;
		this.addr = addr;
	}
	
	/**
	 * opens a socket for this server on the given port, or increases the current connection count by 1
	 * @throws Exception 
	 */
	public synchronized void open() throws Exception {
		TestServerConsole.log("Preparing TCP socket on port " + port + " for TCP/NTP test.", 2, TestServerServiceEnum.TCP_SERVICE);
		
		if (socket == null || socket.isClosed()) {
			socket = TestServer.createServerSocket(port, false, addr);
			TestServerConsole.log("Socket on " + socket.getInetAddress() + ":" + port + " has been (re)opened.", 2, TestServerServiceEnum.TCP_SERVICE);
		}
		
		if (TestServer.serverPreferences.isIpCheck()) {
			if (CLOSE_CONNECTION_IF_NO_CLIENTS_LEFT) {
				currentConnections.addAndGet(1);
				TestServerConsole.log("Socket on port " + port + " still opened. Candidate count has been increased by 1 (current count: " 
						+ currentConnections.get() + ").", 2, TestServerServiceEnum.TCP_SERVICE);
			}
		}
		
		//refresh the TTL
		refreshTtl();
	}
	
	/**
	 * decreases the number of active connections and closes the socket if necessary
	 * @throws IOException
	 */
	public synchronized void close() throws IOException {
		if (TestServer.serverPreferences.isIpCheck()) {
			if (CLOSE_CONNECTION_IF_NO_CLIENTS_LEFT) {
				final long connectionsLeft = currentConnections.addAndGet(-1);
				if (this.socket != null && !this.socket.isClosed() && connectionsLeft <= 0) {
					this.socket.close();
					this.socket = null;
					
					TestServerConsole.log("Closed socket on port " + port + " due to an empty candidate list.", 2, TestServerServiceEnum.TCP_SERVICE);
				}
			}
		}
		else {
			if (CLOSE_CONNECTION_IF_TTL_REACHED) {
				if (this.socket != null && !this.socket.isClosed() && System.currentTimeMillis() >= ttlTimestamp.get()) {
					this.socket.close();
					this.socket = null;
					
					TestServerConsole.log("Closed socket on port " + port + " due to TTL.", 2, TestServerServiceEnum.TCP_SERVICE);
				}
			}
		}
	}

	/**
	 * 
	 * @return
	 */
	public synchronized boolean isAlive() {
		if (TestServer.serverPreferences.isIpCheck()) {
			if (CLOSE_CONNECTION_IF_NO_CLIENTS_LEFT) {
				final long connectionsLeft = currentConnections.get();
				return (this.socket != null && !this.socket.isClosed() && connectionsLeft > 0);
			}	
		}
		else {
			if (CLOSE_CONNECTION_IF_TTL_REACHED) {
				return (this.socket != null && !this.socket.isClosed() && System.currentTimeMillis() < ttlTimestamp.get());
			}
		}
			
		return false;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		FilterOutputStream fos = null;
		BufferedReader br = null;

		try {
			socket.setSoTimeout(TIMEOUT);
			
			while (isAlive()) {
				Socket clientSocket = socket.accept();
				TcpClientHandler tcpClientHandler = new TcpClientHandler(clientSocket, this);
				Thread tcpThread = new Thread(tcpClientHandler);
				tcpThread.start();
			}
			
		}
		catch (SocketTimeoutException e) {
			TestServerConsole.log("TcpServer [" + addr + ":" + port + "]" + e.getLocalizedMessage(), 2, TestServerServiceEnum.TCP_SERVICE);
		}
		catch (Exception e) {
			TestServerConsole.log("TcpServer [" + addr + ":" + port + "]" + e.getLocalizedMessage(), 1, TestServerServiceEnum.TCP_SERVICE);
		} 
		finally {
			try {
				//close this socket
				close();
				
				if (br != null) {
					br.close();
				}
				if (fos != null) {
					fos.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 
	 * @return
	 */
	public ServerSocket getServerSocket() {
		return socket;
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isNtpSocket() {
		return isNtpSocket;
	}

	/**
	 * 
	 * @return
	 */
	public Map<InetAddress, TcpTestCandidate> getCandidateSet() {
		return candidateMap;
	}
	
	/**
	 * 
	 * @param candidateInetAddress
	 */
	public synchronized void registerCandidate(InetAddress candidateInetAddress) {
		TcpTestCandidate candidate = null;
		if (candidateMap.containsKey(candidateInetAddress)) {
			candidate = candidateMap.get(candidateInetAddress);
		}
		else {
			candidate = new TcpTestCandidate();
		}
		
		candidate.increaseTestCounter(true);
		
		TestServerConsole.log("Registering candidate " + candidateInetAddress + " for TCP/NTP on port " + port + " (candidate data: " + candidate + ")", 
				VERBOSE_LEVEL_REGISTER_REMOVE_CANDIDATE, TestServerServiceEnum.TCP_SERVICE);

		candidateMap.put(candidateInetAddress, candidate);
	}
	
	/**
	 * 
	 */
	public void refreshTtl() {
		ttlTimestamp.set(System.currentTimeMillis() + TTL);
		TestServerConsole.log("Refreshing TTL to: " + ttlTimestamp.get() + " of TcpServer on " + addr + ":" + port , 2, TestServerServiceEnum.TCP_SERVICE);
	}
	
	/**
	 * 
	 * @param candidateInetAddress
	 */
	public synchronized void removeCandidate(InetAddress candidateInetAddress) {
		if (candidateMap.containsKey(candidateInetAddress)) {
			TcpTestCandidate candidate = candidateMap.get(candidateInetAddress);
			if (candidate.decreaseTestCounter(true) <= 0) {
				TestServerConsole.log("Candidate " + candidateInetAddress + " has no more tests left of port " + port, 
						VERBOSE_LEVEL_REGISTER_REMOVE_CANDIDATE, TestServerServiceEnum.TCP_SERVICE);

				candidateMap.remove(candidateInetAddress);
			}
			else {

				TestServerConsole.log("Candidate (" + candidate + " - " + candidateInetAddress + ") has " + (candidate.getTestCounter()) + " tests left on port " + port, 
							VERBOSE_LEVEL_REGISTER_REMOVE_CANDIDATE, TestServerServiceEnum.TCP_SERVICE);
			}
		}		
	}
		
	/**
	 * 
	 * @return
	 */
	public int getPort() {
		return port;
	}
	
	/**
	 * 
	 * @return
	 */
	public long getTtlTimestamp() {
		return ttlTimestamp.get();
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "TcpServer [candidateMap=" + candidateMap + ", ttlTimestamp="
				+ ttlTimestamp + ", socket=" + socket + ", isNtpSocket="
				+ isNtpSocket + ", port=" + port + ", addr=" + addr
				+ ", currentConnections=" + currentConnections + "]";
	}
}
