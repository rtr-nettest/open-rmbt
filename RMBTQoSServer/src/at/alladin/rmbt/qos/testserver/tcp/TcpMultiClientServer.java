/*******************************************************************************
 * Copyright 2016 Specure GmbH
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
package at.alladin.rmbt.qos.testserver.tcp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.text.DateFormat;
import java.util.Date;

import at.alladin.rmbt.qos.testserver.ServerPreferences.TestServerServiceEnum;
import at.alladin.rmbt.qos.testserver.TestServer;
import at.alladin.rmbt.qos.testserver.servers.AbstractTcpServer;
import at.alladin.rmbt.qos.testserver.util.TestServerConsole;

/**
 * 
 * @author lb
 *
 */
public class TcpMultiClientServer extends AbstractTcpServer {
	
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
	 * socket SO timeout
	 */
	public final static int TIMEOUT = 20000;
	
	/**
	 * set a socket timeout?
	 */
	public final static boolean HAS_TIMEOUT = false;
	
	/**
	 * start up timestamp
	 */
	private final long startUp = System.currentTimeMillis();
			
	/**
	 * 
	 * @param port
	 * @throws IOException
	 */
	public TcpMultiClientServer(int port, InetAddress addr) throws IOException {
		super(addr, port);
	}
	
	/*
	 * prepares this server for listening (=starts thread), or increases the current connection count by 1
	 * (non-Javadoc)
	 * @see at.alladin.rmbt.qos.testserver.servers.AbstractServer#prepare()
	 */
	@Override
	@SuppressWarnings("unused")
	public synchronized void prepare() throws Exception {
		TestServerConsole.log("Preparing TCP socket on port " + port + " for TCP/NTP test.", 2, TestServerServiceEnum.TCP_SERVICE);
				
		if (TestServer.serverPreferences.isIpCheck()) {
			if (CLOSE_CONNECTION_IF_NO_CLIENTS_LEFT) {
				currentConnections.addAndGet(1);
				TestServerConsole.log("Socket on port " + port + " still opened. Candidate count has been increased by 1 (current count: " 
						+ currentConnections.get() + ").", 2, TestServerServiceEnum.TCP_SERVICE);
			}
		}
		
		//check if this thread is alive
		boolean isThreadRunning = isAlive();
		
		//refresh the TTL
		refreshTtl(TTL);

		if (serverSocket == null) {
			serverSocket = TestServer.createServerSocket(getPort(), false, getInetAddr());
			TestServerConsole.log(getName() + " has been (re)opened.", 2, TestServerServiceEnum.TCP_SERVICE);
		}

		if (serverSocket != null && HAS_TIMEOUT) {
			serverSocket.setSoTimeout(TIMEOUT);
		}
		
		//start thread if not running
		if (!isThreadRunning) {
			TestServer.getCommonThreadPool().submit(this);
		}
	}
	
	/*decreases the number of active connections and closes the socket if necessary
	 * (non-Javadoc)
	 * @see at.alladin.rmbt.qos.testserver.servers.AbstractServer#close()
	 */
	@Override
	public synchronized boolean close() throws IOException {
		if (TestServer.serverPreferences.isIpCheck()) {
			if (CLOSE_CONNECTION_IF_NO_CLIENTS_LEFT) {
				final long connectionsLeft = currentConnections.addAndGet(-1);
				if (this.serverSocket != null && !this.serverSocket.isClosed() && connectionsLeft <= 0) {
					closeSocket();
					TestServerConsole.log("Closed socket on port " + port + "; Reason: empty candidate list.", 2, TestServerServiceEnum.TCP_SERVICE);
					return true;
				}
			}
		}
		else {
			if (CLOSE_CONNECTION_IF_TTL_REACHED) {
				if (this.serverSocket != null && !this.serverSocket.isClosed() && System.currentTimeMillis() >= ttlTimestamp.get()) {
					closeSocket();
					TestServerConsole.log("Closed socket on port " + port + "; Reason: TTL of " + TTL + "ms reached.", 2, TestServerServiceEnum.TCP_SERVICE);
					return true;
				}
			}
		}
		
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see at.alladin.rmbt.qos.testserver.servers.AbstractServer#isAlive()
	 */
	@Override
	public synchronized boolean isAlive() {
		if (TestServer.serverPreferences.isIpCheck()) {
			if (CLOSE_CONNECTION_IF_NO_CLIENTS_LEFT) {
				final long connectionsLeft = currentConnections.get();
				return (this.serverSocket != null && !this.serverSocket.isClosed() && connectionsLeft > 0);
			}
		}
		else {
			if (CLOSE_CONNECTION_IF_TTL_REACHED) {
				return (this.serverSocket != null && !this.serverSocket.isClosed() && System.currentTimeMillis() < ttlTimestamp.get());
			}
		}
		
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see at.alladin.rmbt.qos.testserver.servers.AbstractServer#execute()
	 */
	@Override
	protected void execute() throws Exception {
		Socket clientSocket = serverSocket.accept();
		TcpClientHandler tcpClientHandler = new TcpClientHandler(clientSocket, this);
		TestServer.getCommonThreadPool().execute(tcpClientHandler);
	}
	
	
	@Override
	public String toString() {
		return "TcpMultiClientServer [candidateMap=" + candidateMap
				+ ", currentConnections=" + currentConnections
				+ ", serverSocketTypeClazz=" + serverSocketTypeClazz
				+ ", clientHandlerRunnableClazz=" + clientDataHolderClazz
				+ ", ttlTimestamp=" + ttlTimestamp + ", serverSocket="
				+ serverSocket + ", port=" + port + ", inetAddr=" + inetAddr
				+ "]";
	}

	/*
	 * (non-Javadoc)
	 * @see at.alladin.rmbt.qos.testserver.entity.Observable#isHealthy()
	 */
	@Override
	public boolean isHealthy() {
		return serverSocket != null && serverSocket.isBound();
	}

	/*
	 * (non-Javadoc)
	 * @see at.alladin.rmbt.qos.testserver.entity.Observable#getStatusMessage()
	 */
	@Override
	public String getStatusMessage() {
		return "Server start up: " + DateFormat.getDateTimeInstance().format(new Date(startUp));
	}
}
