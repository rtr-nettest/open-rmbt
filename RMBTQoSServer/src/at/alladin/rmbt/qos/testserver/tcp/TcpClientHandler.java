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
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import at.alladin.rmbt.qos.testserver.ClientHandler;
import at.alladin.rmbt.qos.testserver.ServerPreferences.TestServerServiceEnum;
import at.alladin.rmbt.qos.testserver.TestServer;
import at.alladin.rmbt.qos.testserver.util.TestServerConsole;

/**
 * 
 * @author lb
 *
 */
public class TcpClientHandler implements Runnable {
	
	/**
	 * 
	 */
	public final static int TCP_HANDLER_TIMEOUT = 10000;

	/**
	 * 
	 */
	private final Socket clientSocket;
	
	private final String name;
	
	/**
	 * 
	 */
	private final AtomicReference<TcpMultiClientServer> tcpServer;
	
	public TcpClientHandler(Socket clientSocket, TcpMultiClientServer tcpServer) {
		this.clientSocket = clientSocket;
		this.tcpServer = new AtomicReference<TcpMultiClientServer>(tcpServer);
		this.name = "[TcpClientHandler " + clientSocket.getInetAddress().toString() + "]";
	}
	
	@Override
	public void run() {
		TestServerConsole.log("New TCP ClientHander Thread started. Client: " + clientSocket, 1, TestServerServiceEnum.TCP_SERVICE);
		
		try (BufferedReader br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				FilterOutputStream fos = new FilterOutputStream(clientSocket.getOutputStream());) {
			clientSocket.setSoTimeout(TCP_HANDLER_TIMEOUT);
			
			boolean validCandidate = false;
	
			if (TestServer.serverPreferences.isIpCheck()) {
				//check for test candidate if ip check is enabled
				if (!tcpServer.get().getCandidateMap().containsKey(clientSocket.getInetAddress())) {
					if (clientSocket != null && !clientSocket.isClosed()) {
						clientSocket.close();
					}
	
					TestServerConsole.log(clientSocket.getInetAddress() + ": not a valid candidate for TCP/NTP", 
							TcpMultiClientServer.VERBOSE_LEVEL_REQUEST_RESPONSE, TestServerServiceEnum.TCP_SERVICE);
				}
				else {
					validCandidate = true;
				}
			}
			else {
				//else allow connection
				validCandidate = true;
			}
			
			if (validCandidate) {
				tcpServer.get().refreshTtl(TcpMultiClientServer.TTL);
				
				//remove test candidate if ip check is enabled
				if (TestServer.serverPreferences.isIpCheck()) {
					tcpServer.get().removeCandidate(clientSocket.getInetAddress());
				}
				
				String clientRequest = br.readLine();
				
				TestServerConsole.log("TCP/NTP Server (" + tcpServer.get().getServerSocket() + ") (:" + tcpServer.get().getPort() + "), connection from: " + clientSocket.getInetAddress().toString() + ", request: " + clientRequest, 
						TcpMultiClientServer.VERBOSE_LEVEL_REQUEST_RESPONSE, TestServerServiceEnum.TCP_SERVICE);
	
				//send echo
				byte[] response = ClientHandler.getBytesWithNewline(clientRequest);
				
				TestServerConsole.log("TCP/NTP Server (" + tcpServer.get().getServerSocket() + ") (:" + tcpServer.get().getPort() + "), response: " + new String(response) + " to: " + clientSocket.getInetAddress().toString(), 
						TcpMultiClientServer.VERBOSE_LEVEL_REQUEST_RESPONSE, TestServerServiceEnum.TCP_SERVICE);
	
				fos.write(response);				
			}
		}
		catch (SocketTimeoutException e) {
			TestServerConsole.error("TcpClientHandler Thread " + clientSocket.toString(), e, 2, TestServerServiceEnum.TCP_SERVICE);
		}
		catch (Exception e) {
			TestServerConsole.error("TcpClientHandler Thread " + clientSocket.toString(), e, 1, TestServerServiceEnum.TCP_SERVICE);
		}
		finally {
			try {
				if (clientSocket != null && !clientSocket.isClosed()) {
					clientSocket.close();
				}
			} catch (Exception e) {
				TestServerConsole.error(name, e, 2, TestServerServiceEnum.TCP_SERVICE);
			}
		}
	}
}
