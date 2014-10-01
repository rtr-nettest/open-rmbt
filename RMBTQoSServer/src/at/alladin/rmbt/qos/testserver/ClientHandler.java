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
package at.alladin.rmbt.qos.testserver;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.postgresql.util.Base64;

import at.alladin.rmbt.qos.testserver.ServerPreferences.TestServerServiceEnum;
import at.alladin.rmbt.qos.testserver.entity.ClientToken;
import at.alladin.rmbt.qos.testserver.tcp.TcpServer;
import at.alladin.rmbt.qos.testserver.udp.ClientUdpData;
import at.alladin.rmbt.qos.testserver.udp.UdpMultiClientServer;
import at.alladin.rmbt.qos.testserver.udp.UdpPacketReceivedCallback;
import at.alladin.rmbt.qos.testserver.udp.UdpSingleClientServer;
import at.alladin.rmbt.qos.testserver.udp.UdpTestCompleteCallback;
import at.alladin.rmbt.qos.testserver.udp.util.UdpUtil;
import at.alladin.rmbt.qos.testserver.util.TestServerConsole;

public class ClientHandler implements Runnable {
	
	public final static int SOCKET_TIMEOUT = 15000;
	
	public final static boolean ABORT_ON_DUPLICATE_UDP_PACKETS = false;
	
	private final ServerSocket serverSocket;
	
	private final Socket socket;
	
	protected final FilterInputStream in;
	
	protected final FilterOutputStream out;
	
	protected final BufferedReader reader;
	
	/**
	 * 
	 * @param serverSocket
	 * @param socket
	 * @throws IOException
	 */
	public ClientHandler(ServerSocket serverSocket, Socket socket) throws IOException {
		this.serverSocket = serverSocket;
		this.socket = socket;
		this.in = new BufferedInputStream(socket.getInputStream());
		this.out = new FilterOutputStream(socket.getOutputStream());
		this.reader = new BufferedReader(new InputStreamReader(in));
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		TestServerConsole.log("New connection from: " + socket.getInetAddress().toString(), 
				0, TestServerServiceEnum.TEST_SERVER);
		String message;
		
		try {
			socket.setSoTimeout(SOCKET_TIMEOUT);
			
			out.write(getBytesWithNewline(QoSServiceProtocol.GREETING));
			out.write(getBytesWithNewline(QoSServiceProtocol.ACCEPT_TOKEN));
			message = reader.readLine();
			TestServerConsole.log("GOT: " + message, 1, TestServerServiceEnum.TEST_SERVER);
			
			ClientToken token = checkToken(message);
			
			TestServerConsole.log("TOKEN OK", 1, TestServerServiceEnum.TEST_SERVER);
			
			out.write(getBytesWithNewline(QoSServiceProtocol.TOKEN_OK_RESPONSE));
			out.write(getBytesWithNewline(QoSServiceProtocol.ACCEPT_COMMANDS));
			
			boolean quit = false;
			
			while(!quit) {
				String command = reader.readLine();
				TestServerConsole.log("COMMAND: " + command + " from: " + socket.getInetAddress().toString(), 0, TestServerServiceEnum.TEST_SERVER);
				if (command != null) {
					if (command.startsWith(QoSServiceProtocol.NON_TRANSPARENT_PROXY_TEXT)) {
						runNonTransparentProxyTest(command);
						quit = true;
					}
					else if (command.startsWith(QoSServiceProtocol.TCP_TEST_IN)) {
						runIncomingTcpTest(command, token);
					}
					else if (command.startsWith(QoSServiceProtocol.TCP_TEST_OUT)) {
						runOutgoingTcpTest(command, token);
					}
					else if (command.startsWith(QoSServiceProtocol.UDP_TEST_OUT)) {
						runOutgoingUdpTestOnMultiClientServer(command, token);
					}
					else if (command.startsWith(QoSServiceProtocol.UDP_TEST_IN)) {
						runIncomingUdpTest(command, token);
					}
					else if (command.startsWith(QoSServiceProtocol.REQUEST_UDP_PORT_RANGE)) {
						out.write(getBytesWithNewline(TestServer.serverPreferences.getUdpPortMin() +  " " + TestServer.serverPreferences.getUdpPortMax()));
					}
					else if (command.startsWith(QoSServiceProtocol.REQUEST_UDP_PORT)) {
						sendRandomUdpPort();
					}
					else if (command.startsWith(QoSServiceProtocol.REQUEST_QUIT)) {
						quit = true;
					}
					else {
						out.write(getBytesWithNewline(QoSServiceProtocol.ACCEPT_COMMANDS));
						quit = true;
					}
				}
				else {
					quit = true;
				}
			}
		} 
		catch (IOException e) {
			TestServerConsole.log("ClientHandler IOException from: " + socket.getInetAddress().toString(), 
					0, TestServerServiceEnum.TEST_SERVER);
			e.printStackTrace();
		}
		catch (Exception e) {
			TestServerConsole.log("ClientHandler unknown exception from: " + socket.getInetAddress().toString(), 
					0, TestServerServiceEnum.TEST_SERVER);
			e.printStackTrace();			
		}
		finally {
			if (!socket.isClosed()) {
				try {
					socket.close();
					TestServerConsole.log("ClientHandler closed connection to: " + socket.getInetAddress().toString(), 
							0, TestServerServiceEnum.TEST_SERVER);
				} catch (IOException e) {
					TestServerConsole.log("ClientHandler: Could not close socket from: " + socket.getInetAddress().toString(), 
							0, TestServerServiceEnum.TEST_SERVER);
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 
	 * @param string
	 * @return
	 */
	public synchronized static byte[] getBytesWithNewline(String string) {
		if (string.endsWith("\n")) {
			return getBytesWithNewline(string, false);
		}
		else {
			return getBytesWithNewline(string, true);
		}
	}
	
	/**
	 * 
	 * @param string
	 * @param appendNewLine
	 * @return
	 */
	public synchronized static byte[] getBytesWithNewline(String string, boolean appendNewLine) {
		if (appendNewLine) {
			return (string + "\n").getBytes();
		}
		else {
			return string.getBytes();
		}
	}

	/**
	 * 
	 * @param token
	 * @return
	 * @throws IOException
	 */
	private ClientToken checkToken(String token) throws IOException {
		ClientToken clientToken;
		
		try {
			Pattern p = Pattern.compile("TOKEN ([\\d\\w-]*)_([\\d]*)_(.*)");
			TestServerConsole.log("Got token: " + token, 0, TestServerServiceEnum.TEST_SERVER);
			Matcher m = p.matcher(token);
			m.find();
			
			if (m.groupCount()!=3) {
				throw new IOException("BAD TOKEN: Bad Arguments!\n");
			}
			else {
				String uuid = m.group(1);
				long timeStamp = Long.parseLong(m.group(2));
				String hmac = m.group(3);

				String controlHmac = calculateHMAC(TestServer.serverPreferences.getSecretKey(), uuid + "_" + timeStamp);
				if (controlHmac.equals(hmac)) {
					clientToken = new ClientToken(uuid, timeStamp, hmac);	
					return clientToken;
				}
				else {
					throw new IOException("BAD TOKEN. Bad Key!\n" + controlHmac + " <-> " + hmac + "\n");
				}
			}
		}
		catch (IOException e) {
			throw e;
			
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new IOException("BAD TOKEN: " + token);
		}
	}
	
	/**
	 * 
	 * @param secret
	 * @param data
	 * @return
	 */
    private static String calculateHMAC(final String secret, final String data)
    {
        try
        {
            final SecretKeySpec signingKey = new SecretKeySpec(secret.getBytes(), "HmacSHA1");
            final Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(signingKey);
            final byte[] rawHmac = mac.doFinal(data.getBytes());
            final String result = new String(Base64.encodeBytes(rawHmac));
            return result;
        }
        catch (final GeneralSecurityException e)
        {
            
            TestServerConsole.log("Unexpected error while creating hash: " + e.getMessage(), 2, TestServerServiceEnum.TEST_SERVER);
            return "";
        }
    }
    
    /**
     * 
     * @param token
     * @throws IOException 
     */
    private void sendRandomUdpPort() throws IOException {
		Random rand = new Random();
		int randomPort = rand.nextInt(TestServer.serverPreferences.getUdpPortMax() - TestServer.serverPreferences.getUdpPortMin()) + 
				TestServer.serverPreferences.getUdpPortMin();
		TestServerConsole.log("Requested UDP Port. Picked random port number: " + randomPort, 0, TestServerServiceEnum.TEST_SERVER);
		out.write(getBytesWithNewline(String.valueOf(randomPort)));
    }

    /**
     * 
     * @param command
     * @param token
     * @throws IOException
     */
    private void runIncomingTcpTest(String command, ClientToken token) throws IOException {
    	int port;
    	
		Pattern p = Pattern.compile(QoSServiceProtocol.TCP_TEST_IN + " ([\\d]*)");
		Matcher m = p.matcher(command);
		m.find();
		if (m.groupCount()!=1) {
			throw new IOException("tcp incoming test command syntax error: " + command);
		}
		else {
			port = Integer.parseInt(m.group(1));
		}
		
		Socket testSocket = null;
		try {
			testSocket = new Socket(socket.getInetAddress(), port);
			BufferedOutputStream out = new BufferedOutputStream(testSocket.getOutputStream());
			out.write(getBytesWithNewline("HELLO TO " + port));
			out.flush();
			testSocket.close();
		}
		catch (Exception e) {
			throw e;
		}
		finally {
			if (testSocket != null && !testSocket.isClosed()) {
				testSocket.close();
			}			
		}
    }
    
    /**
     * 
     * @param command
     * @param token
     * @throws IOException 
     * @throws InterruptedException 
     */
    private void runOutgoingTcpTest(String command, ClientToken token) throws Exception {
    	int port;
    	
		Pattern p = Pattern.compile(QoSServiceProtocol.TCP_TEST_OUT + " ([\\d]*)");
		Matcher m = p.matcher(command);
		m.find();
		if (m.groupCount()!=1) {
			throw new IOException("tcp outgoing test command syntax error: " + command);
		}
		else {
			port = Integer.parseInt(m.group(1));
		}

		List<TcpServer> tcpServerList;
		
		synchronized (TestServer.tcpServerMap) {
			if ((tcpServerList = TestServer.tcpServerMap.get(port)) == null) {
				tcpServerList = new ArrayList<>();
				for (InetAddress addr : TestServer.serverPreferences.getInetAddrBindToSet()) {
					tcpServerList.add(new TcpServer(port, false, addr));
				}
				TestServer.tcpServerMap.put(port, tcpServerList);
			}
			
			if (TestServer.serverPreferences.isIpCheck()) {
				for (TcpServer tcpServer : tcpServerList) {
					tcpServer.registerCandidate(socket.getInetAddress());	
				}
			}
		}
		
		try {
			for (TcpServer tcpServer : tcpServerList) {
				tcpServer.open();
				Thread tcpThread = new Thread(tcpServer);
				tcpThread.start();
			}
			
			out.write(getBytesWithNewline("OK"));		
		}
		catch (Exception e) {
			throw e;
		}
		finally {
			//is beeing done inside TcpServer now:
			//tcpServer.removeCandidate(socket.getInetAddress());
		}
    }
    
    /**
     * 
     * @param command
     * @param token
     * @throws IOException
     * @throws InterruptedException 
     */
    private void runIncomingUdpTest(String command, ClientToken token) throws IOException, InterruptedException {
    	int port, numPackets;
    	//int timeout = 8000;
    	
		Pattern p = Pattern.compile(QoSServiceProtocol.UDP_TEST_IN + " ([\\d]*) ([\\d]*)");
		Matcher m = p.matcher(command);
		m.find();
		if (m.groupCount()!=2) {
			throw new IOException("udp incoming test command syntax error: " + command);
		}
		else {
			port = Integer.parseInt(m.group(1));
			numPackets = Integer.parseInt(m.group(2));
		}	
		
		//DatagramSocket sock = new DatagramSocket(port);
		DatagramSocket sock = new DatagramSocket();
		ClientUdpData clientData = sendUdpPackets(socket.getInetAddress(), sock, port, 3000, numPackets, true, token);

		
		TestServerConsole.log(socket.getInetAddress() + ": RESULT OK, RCV PACKETS: " + clientData.getPacketsReceived().size() 
				+ ", DUP: " + clientData.getPacketDuplicates().size(), 2, TestServerServiceEnum.UDP_SERVICE);
		out.write(getBytesWithNewline(QoSServiceProtocol.RESPONSE_UDP_NUM_PACKETS_RECEIVED + " " 
				+ clientData.getPacketsReceived().size() + " " + clientData.getPacketDuplicates().size()));
    }
    
    /**
     * 
     * @param sock
     * @param timeOut
     * @param numPackets
     * @param token
     * @return
     */
    private ClientUdpData sendUdpPackets(InetAddress targetHost, DatagramSocket sock, int port, int timeOut, int numPackets, boolean awaitResponse, ClientToken token) {
    	final ClientUdpData clientData = new ClientUdpData(numPackets);
	    ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
	    DataOutputStream dataOut = new DataOutputStream(byteOut);
	 
	    TestServerConsole.log("INIT sending UDP packets (amount = " + numPackets + ") to " + targetHost + " on port " + port
	    		+ " - using DatagramSocket: " + sock.getLocalAddress() + ":" + sock.getLocalPort(), 
	    		2, TestServerServiceEnum.UDP_SERVICE);

	    
	    try {
			sock.setSoTimeout(timeOut);
	    } catch (SocketException e) {
	    	e.printStackTrace();
	    	return clientData;
		}
	 
	    byte[] data;

	    for (int i = 0; i < numPackets; i++) {
            
	    	byteOut.reset();
	    	try {
	    		dataOut.writeByte(QoSServiceProtocol.UDP_TEST_AWAIT_RESPONSE_IDENTIFIER);
	    		dataOut.writeByte(i);
    			dataOut.write(token.getUuid().getBytes());
	    	} catch (IOException e) {
	    		e.printStackTrace();
	    		sock.close();
	    		return clientData;
	    	}
	      	 
	    	try {
		    	byteOut.flush();
		    	data = byteOut.toByteArray();
		    	
			    DatagramPacket packet = new DatagramPacket(data, data.length, targetHost, port);
	    		sock.send(packet);
	    		
	    		if (awaitResponse) {
	    			try {
	    			    byte buffer[] = new byte[1024];

	    			    DatagramPacket dp = new DatagramPacket(buffer, buffer.length);
    			    	sock.receive(dp);
    			    	
	    				int packetNumber = buffer[1];
	    				
	    			    TestServerConsole.log(dp.getAddress() + ": UDP Test received packet: #" + packetNumber + " -> " + buffer, 
	    			    		2, TestServerServiceEnum.UDP_SERVICE);
	    			    
	    				//check udp packet:
	    				if (buffer[0] != QoSServiceProtocol.UDP_TEST_RESPONSE) {
	    					TestServerConsole.log(dp.getAddress() + ": bad UDP IN TEST packet identifier", 0, TestServerServiceEnum.UDP_SERVICE);
	    					socket.close();
	    					throw new IOException("bad UDP IN TEST packet identifier");
	    				}
	    				
						//check for duplicate packets:
	    				if (clientData.getPacketsReceived().contains(packetNumber)) {
	    					TestServerConsole.log(dp.getAddress() + ": duplicate UDP IN TEST packet id", 0, TestServerServiceEnum.UDP_SERVICE);
	    					clientData.getPacketDuplicates().add(packetNumber);
	    					if (ABORT_ON_DUPLICATE_UDP_PACKETS) {
	    						socket.close();
	    						throw new IOException("duplicate UDP IN TEST packet id");
	    					}
	    				}
	    				else {
	    					clientData.getPacketsReceived().add(new Integer(packetNumber));
	    				}	    				
	    			}
	    			catch (SocketTimeoutException e) {
	    				//packet not received
	    				e.printStackTrace();
	    			}
	    		}
	    	} catch (IOException e) {
	    		e.printStackTrace();
	    		sock.close();
	    		return clientData;
	    	}
	    	
    		TestServerConsole.log("Sent packet pnum:" + i + " to " + targetHost + ":" + port +", sent message:" + data, 
    				2, TestServerServiceEnum.TEST_SERVER);
	    }

	    try {
	    	byteOut.close();
	    } catch (Exception e) {
	    	e.printStackTrace();
	    	return clientData;
	    }
	    finally {
	    	sock.close();
	    }
	    
	    return clientData;
    }
    
    /**
     * 
     * @param command
     * @throws IOException 
     * @throws InterruptedException 
     */
    private void runOutgoingUdpTestOnMultiClientServer(final String command, final ClientToken token) throws IOException, InterruptedException {
    	final int port;
		int numPackets = 0;
    	long timeout = 5000;
    	
		Pattern p = Pattern.compile(QoSServiceProtocol.UDP_TEST_OUT + " ([\\d]*) ([\\d]*)");
		Matcher m = p.matcher(command);
		m.find();

		if (m.groupCount()!=2) {
			throw new IOException("udp outgoing test command syntax error: " + command);
		}
		else {
			port = Integer.parseInt(m.group(1));
			numPackets = Integer.parseInt(m.group(2));
		}

		TestServerConsole.log("Starting UDP OUT TEST (requested packets: " + numPackets + ") on port :" + port + " for " + socket.getInetAddress().toString(), 
				1, TestServerServiceEnum.UDP_SERVICE);

		final AtomicBoolean udpTestFinished = new AtomicBoolean(false);
		final AtomicBoolean resetTimeout = new AtomicBoolean(true); 
		final ClientUdpData udpData = new ClientUdpData(numPackets);
		
		//packet receive callback
		udpData.setOnUdpPacketReceivedCallback(new UdpPacketReceivedCallback() {
			
			@Override
			public boolean onReceive(final DatagramPacket dp, final UdpMultiClientServer udpServer) {
				resetTimeout.set(true);
				
				final byte[] data = dp.getData();
				final int packetNumber = data[1];

				//check udp packet:
				if (data[0] != QoSServiceProtocol.UDP_TEST_ONE_DIRECTION_IDENTIFIER && data[0] != QoSServiceProtocol.UDP_TEST_AWAIT_RESPONSE_IDENTIFIER) {
					TestServerConsole.log(dp.getAddress() +  ": bad UDP IN TEST packet identifier", 0, TestServerServiceEnum.UDP_SERVICE);
					udpData.setError(true);
					udpData.setErrorMsg("bad UDP IN TEST packet identifier");
				}
								
				//check for duplicate packets:
				if (udpData.getPacketsReceived().contains(packetNumber)) {
					TestServerConsole.log(dp.getAddress() + ": duplicate UDP IN TEST packet id", 0, TestServerServiceEnum.UDP_SERVICE);
					udpData.getPacketDuplicates().add(packetNumber);
					if (ABORT_ON_DUPLICATE_UDP_PACKETS) {
						udpData.setError(true);
						udpData.setErrorMsg("duplicate UDP IN TEST packet id");
					}
				}
				else {
					udpData.getPacketsReceived().add(new Integer(packetNumber));

					if (data[0] == QoSServiceProtocol.UDP_TEST_AWAIT_RESPONSE_IDENTIFIER) {
						data[0] = QoSServiceProtocol.UDP_TEST_RESPONSE;
						DatagramPacket response = new DatagramPacket(data, dp.getLength(), dp.getAddress(), dp.getPort());
						try {
							udpServer.getDatagramSocket().send(response);
						}
						catch (Exception e) {
							//ignore exception (can be a blocked outgoing port; in this case the test should continue normally)
						}
					}
					
					return true;
				}
				
				return false;
			}
		});
		
		//add callback to udp client data in case all udp packets will arrive. in this case we can send back "RCV" before the
		//final timeout is reached
		udpData.setOnUdpTestCompleteCallback(new UdpTestCompleteCallback() {
			
			@Override
			public boolean onComplete(ClientUdpData udpData) {
				try {
					udpTestFinished.set(true);
					sendRcvResult(udpData, port);
					return true;
				}
				catch (Exception e) {
					e.printStackTrace();
				}
				
				return false;
			}
		});
		
		//register udp client data
		UdpMultiClientServer udpServer = UdpUtil.registerCandidate(socket.getLocalAddress(), port, token, udpData);
		
		//tell the client that we are ready
		out.write(getBytesWithNewline(QoSServiceProtocol.OK_RESPONSE));
		
		boolean abortTimeoutLoop = false;

		//let the udpserver do its work and wait until the final timeout is reached
		while (!udpTestFinished.get() && !abortTimeoutLoop) {
			if (!udpTestFinished.get() && resetTimeout.getAndSet(false)) {
				Thread.sleep(timeout);
			}
			else {
				abortTimeoutLoop = true;
			}
		}
		
		//if not all packets were received by the test server already, interrupt the test and send the current results back to the client
		if (!udpTestFinished.get() && udpServer != null) {
			final ClientUdpData result = udpServer.pollClientData(token.getUuid());
			TestServerConsole.log("UDP OUT TEST on port :" + port + " for " + socket.getInetAddress().toString() + ":" + socket.getPort() 
					+ " finished...", 1, TestServerServiceEnum.UDP_SERVICE);
			sendRcvResult(result, port);
		}
    }
    
    private void sendRcvResult(ClientUdpData result, int port) throws IOException {
		TestServerConsole.log("UDP OUT TEST on port :" + port + " for " + socket.getInetAddress().toString() + ":" 
				+ socket.getPort() + " finished...", 1, TestServerServiceEnum.UDP_SERVICE);
		if (result != null && result.getPacketsReceived() != null && !result.isError()) {
			TestServerConsole.log("RESULT OK, RCV PACKETS: " + result.getPacketsReceived().size() + ", DUP: " + result.getPacketDuplicates().size(), 1, TestServerServiceEnum.UDP_SERVICE);
			out.write(getBytesWithNewline(QoSServiceProtocol.RESPONSE_UDP_NUM_PACKETS_RECEIVED + " " + result.getPacketsReceived().size() + " " + result.getPacketDuplicates().size()));
		}
		else {
			TestServerConsole.log("RESULT ERROR, error: " + (result != null ? result.getErrorMsg() : "sorry, no error message available!"), 
					1, TestServerServiceEnum.UDP_SERVICE);
			out.write(getBytesWithNewline(QoSServiceProtocol.RESPONSE_UDP_NUM_PACKETS_RECEIVED + " 0 0"));
		}
    }
    
    /**
     * 
     * @param command
     * @throws IOException 
     * @throws InterruptedException 
     */
    @SuppressWarnings("unused")
	@Deprecated
    private void runOutgoingUdpTestOnSingleClientServer(String command, ClientToken token) throws IOException, InterruptedException {
    	int port, numPackets;
    	
		Pattern p = Pattern.compile(QoSServiceProtocol.UDP_TEST_IN + " ([\\d]*) ([\\d]*)");
		Matcher m = p.matcher(command);
		m.find();
		if (m.groupCount()!=2) {
			throw new IOException("udp incoming test command syntax error: " + command);
		}
		else {
			port = Integer.parseInt(m.group(1));
			numPackets = Integer.parseInt(m.group(2));
		}
		
		UdpSingleClientServer udpServer = new UdpSingleClientServer(port, numPackets, 10000, token);
		out.write(getBytesWithNewline(QoSServiceProtocol.OK_RESPONSE));
		int result = udpServer.call();
		out.write(getBytesWithNewline(QoSServiceProtocol.RESPONSE_UDP_NUM_PACKETS_RECEIVED + " " + result));
    }
    
    /**
	 * runs the non transparent proxy test:
	 * 
	 * 1. open socket on requested port and send "OK" to let client continue the test 
	 * 2. wait for incoming HTTP protocol request
	 * 3. send request back to client (echo)
	 *  
     * @param command
     * @throws IOException 
     * @throws InterruptedException 
     */
    private void runNonTransparentProxyTest(String command) throws Exception {
		int echoPort;
		
		Pattern p = Pattern.compile(QoSServiceProtocol.NON_TRANSPARENT_PROXY_TEXT + " ([\\d]*)");
		Matcher m = p.matcher(command);
		m.find();
		if (m.groupCount()!=1) {
			throw new IOException("non transparent proxy test command syntax error: " + command);
		}
		else {
			echoPort = Integer.parseInt(m.group(1));
		}
		
		TestServerConsole.log("NTP TEST, opening socket on port: " + echoPort, 1, TestServerServiceEnum.TCP_SERVICE);

		List<TcpServer> tcpServerList;
		
		synchronized (TestServer.tcpServerMap) {
			if ((tcpServerList = TestServer.tcpServerMap.get(echoPort)) == null) {
				tcpServerList = new ArrayList<>();
				for (InetAddress addr : TestServer.serverPreferences.getInetAddrBindToSet()) {
					tcpServerList.add(new TcpServer(echoPort, false, addr));
				}
				TestServer.tcpServerMap.put(echoPort, tcpServerList);
			}
			
			if (TestServer.serverPreferences.isIpCheck()) {
				for (TcpServer tcpServer : tcpServerList) {
					tcpServer.registerCandidate(socket.getInetAddress());	
				}
			}
		}
		
		try {
			for (TcpServer tcpServer : tcpServerList) {
				tcpServer.open();
				Thread tcpThread = new Thread(tcpServer);
				tcpThread.start();
			}
			
			out.write(getBytesWithNewline(QoSServiceProtocol.OK_RESPONSE));
			TestServerConsole.log("NTP: sendind OK. waiting for request...", 1, TestServerServiceEnum.TCP_SERVICE);

		}
		catch (Exception e) {
			throw e;
		}
		finally {
			//is beeing done inside TcpServer now:
			//tcpServer.removeCandidate(socket.getInetAddress());
		}
    }
    
    /**
     * 
     * @return
     */
    public ServerSocket getServerSocket() {
    	return this.serverSocket;
    }    
}
