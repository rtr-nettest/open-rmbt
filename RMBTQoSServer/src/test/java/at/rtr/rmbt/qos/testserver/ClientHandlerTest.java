/*******************************************************************************
 * Copyright 2019 alladin-IT GmbH
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

package at.rtr.rmbt.qos.testserver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadPoolExecutor;

import at.rtr.rmbt.qos.testserver.mock.RecordingFilterOutputStreamMock;
import org.junit.Test;

import at.rtr.rmbt.qos.testserver.entity.ClientToken;
import mockit.Expectations;
import mockit.Invocation;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;

/**
 * 	
 * @author Lukasz Budryk (lb@alladin.at)
 *
 */
public class ClientHandlerTest {

	public final String TOKEN = "bbd1ee96-0779-4619-b993-bb4bf7089754";
	
	public final String TOKEN_COMMAND = "TOKEN " + TOKEN + "_1528136454_3gr2gw9lVhtVONV0XO62Vamu/uw=";
	
	@Mocked Socket socket;
		
	@Mocked ServerSocket serverSocket;
	
	//@Mocked TestServerConsole console;
	
	@Mocked BufferedReader reader;
	
	@Mocked ThreadPoolExecutor executor;
	
	@Mocked DatagramSocket datagramSocket;
	
	@Test
	public void testSendMessage() throws IOException {
		final RecordingFilterOutputStreamMock ros = new RecordingFilterOutputStreamMock();
		final ClientHandler ch = new ClientHandler(serverSocket, socket);
		
		ch.sendCommand("TEST");
		assertEquals("command received by client != 'TEST\\n'", "TEST\n", ros.getContent());
		
		ch.sendCommand("TEST", null);
		assertEquals("command received by client != 'TEST\\n'", "TEST\n", ros.getContent());
		
		ch.sendCommand("TEST", "TEST REQUEST");
		assertEquals("command received by client != 'TEST\\n'", "TEST\n", ros.getContent());
	}
	
	@Test
	public void testSendMessageContainingIDAppendix() throws IOException {
		final RecordingFilterOutputStreamMock ros = new RecordingFilterOutputStreamMock();		
		final ClientHandler ch = new ClientHandler(serverSocket, socket);
		
		ch.sendCommand("TEST", "TEST +ID0");		
		assertEquals("command received by client != 'TEST +ID0\\n'", "TEST +ID0\n", ros.getContent());
	}
	
	@Test(expected=IOException.class)
	public void testCheckTokenWithBadToken() throws IOException {
		final RecordingFilterOutputStreamMock ros = new RecordingFilterOutputStreamMock();		
		final ClientHandler ch = new ClientHandler(serverSocket, socket);
		ch.checkToken("TOKEN 1234");
	}
	
	@Test
	public void testCheckTokenWithValidToken() throws NoSuchFieldException, SecurityException, Exception {
		final RecordingFilterOutputStreamMock ros = new RecordingFilterOutputStreamMock();
		final ClientHandler ch = new ClientHandler(serverSocket, socket);
		final ClientToken token = 
				ch.checkToken("TOKEN bbd1ee96-0779-4619-b993-bb4bf7089754_1528136454_3gr2gw9lVhtVONV0XO62Vamu/uw=\n");
		
		assertNotNull("Token is null", token);
		assertEquals("token timestamp != 1528136454", 1528136454, token.getTimeStamp());
		assertEquals("token UUID != 'bbd1ee96-0779-4619-b993-bb4bf7089754'", 
				"bbd1ee96-0779-4619-b993-bb4bf7089754", token.getUuid());
		assertEquals("token Hmac != '3gr2gw9lVhtVONV0XO62Vamu/uw='", 
				"3gr2gw9lVhtVONV0XO62Vamu/uw=", token.getHmac());		
	}

	@Test
	public void testMultipleCommandsReceived() throws Exception {

		final Map<String, String> cmdMap = new HashMap<>();
		
		new MockUp<ClientHandler>() {
			
			@Mock
			protected ClientToken checkToken(String token) throws IOException {
				return new ClientToken(UUID.randomUUID().toString(), System.currentTimeMillis(), "ABC");
			}
			
			@Mock
			protected void runNonTransparentProxyTest(String command) throws Exception {
				cmdMap.put(QoSServiceProtocol.CMD_NON_TRANSPARENT_PROXY_TEXT, command);
			}
			
			@Mock
			protected void runIncomingTcpTest(String command, ClientToken token) throws IOException {
				cmdMap.put(QoSServiceProtocol.CMD_TCP_TEST_IN, command);
			}
			
			@Mock
			protected void runOutgoingTcpTest(String command, ClientToken token) throws Exception {
				cmdMap.put(QoSServiceProtocol.CMD_TCP_TEST_OUT, command);
			}
					
			@Mock
			protected void runIncomingUdpTest(final String command, final ClientToken token) throws IOException, InterruptedException {
				cmdMap.put(QoSServiceProtocol.CMD_UDP_TEST_IN, command);
			}
			
			@Mock
			protected void runOutgoingUdpTest(final String command, final ClientToken token) throws IOException, InterruptedException {
				cmdMap.put(QoSServiceProtocol.CMD_UDP_TEST_OUT, command);
			}
			
			@Mock
			protected void runVoipTest(final String command, final ClientToken token) throws IOException, InterruptedException {
				cmdMap.put(QoSServiceProtocol.CMD_VOIP_TEST, command);
			}
		};

		new Expectations() {
			{
				reader.readLine();
				returns("TOKEN ABC", QoSServiceProtocol.CMD_NON_TRANSPARENT_PROXY_TEXT + " +ID0",
						QoSServiceProtocol.CMD_TCP_TEST_IN + " +ID1",
						QoSServiceProtocol.CMD_TCP_TEST_OUT + " +ID2",
						QoSServiceProtocol.CMD_UDP_TEST_IN  + " +ID3",
						QoSServiceProtocol.CMD_UDP_TEST_OUT + " +ID4",
						QoSServiceProtocol.CMD_VOIP_TEST + " +ID5",
						"QUIT");
			}
		};

		final ClientHandler ch = new ClientHandler(serverSocket, socket);

		ch.run();
		
		assertEquals("Test command count != 6", 6, cmdMap.size());
		assertEquals("NTP command != 'NTPTEST +ID0'", "NTPTEST +ID0", cmdMap.get(QoSServiceProtocol.CMD_NON_TRANSPARENT_PROXY_TEXT));
		assertEquals("TCP IN command != 'TCPTEST IN +ID1'", "TCPTEST IN +ID1", cmdMap.get(QoSServiceProtocol.CMD_TCP_TEST_IN));
		assertEquals("TCP OUT command != 'TCPTEXT OUT +ID2'", "TCPTEST OUT +ID2", cmdMap.get(QoSServiceProtocol.CMD_TCP_TEST_OUT));
		assertEquals("UDP IN command != 'UDPTEST IN +ID3'", "UDPTEST IN +ID3", cmdMap.get(QoSServiceProtocol.CMD_UDP_TEST_IN));
		assertEquals("UDP OUT command != 'UDPTEST OUT +ID4'", "UDPTEST OUT +ID4", cmdMap.get(QoSServiceProtocol.CMD_UDP_TEST_OUT));
		assertEquals("VOIP command != 'VOIPTEST +ID5'", "VOIPTEST +ID5", cmdMap.get(QoSServiceProtocol.CMD_VOIP_TEST));
		
	}
	
	@Test
	public void testHandshakeAndRunIncomingTcpTestAfterReceivingCommand() throws Exception {

		final List<String> clientMessages = new ArrayList<>();
		
		new MockUp<BufferedOutputStream>() {
			
			@Mock
			public void write(byte b[]) throws IOException {
				clientMessages.add(new String(b));
			}
		};
		
		new MockUp<ThreadPoolExecutor>() {
			
			@Mock
			public void execute(Runnable runnable) {
				runnable.run();
			}
		};
		
		new Expectations() {
			{
				reader.readLine();
				returns(TOKEN_COMMAND,
						QoSServiceProtocol.CMD_TCP_TEST_IN + " 100 +ID1",
						QoSServiceProtocol.CMD_TCP_TEST_IN + " 200 +ID2",
						QoSServiceProtocol.CMD_TCP_TEST_IN + " 300 +ID3",
						"QUIT");				
			}
		};

		final ClientHandler ch = new ClientHandler(serverSocket, socket);

		ch.run();

		assertEquals("TCP IN test count != 3", 3, clientMessages.size());
		assertEquals("Message [0] to client != 'HELLO TO 100\\n'", "HELLO TO 100\n", clientMessages.get(0));
		assertEquals("Message [1] to client != 'HELLO TO 200\\n'", "HELLO TO 200\n", clientMessages.get(1));
		assertEquals("Message [1] to client != 'HELLO TO 300\\n'", "HELLO TO 300\n", clientMessages.get(2));
	}
	
	@Test
	public void testHandeshakeAndRunIncomingUdpTestAfterReceivingCommand() throws Exception {

		final List<byte[]> clientMessages = new ArrayList<>();
		
		new MockUp<DataOutputStream>() {
			
			@Mock
		    public void write(Invocation invocation, byte[] b) throws IOException {
				if (TOKEN.equals(new String(b))) {
					invocation.proceed(b);
				}
				else {
					clientMessages.add(b);
				}
		    }

		};
		
		new MockUp<ThreadPoolExecutor>() {
			
			@Mock
			public void execute(Runnable runnable) {
				runnable.run();
			}
		};
		
		new MockUp<DatagramSocket>() {
			
			@Mock
			public synchronized void receive(DatagramPacket p) throws IOException {
				p.setData(new byte[] {2});
			}
		};
		
		new Expectations() {
			{
				reader.readLine();
				returns(TOKEN_COMMAND,
						QoSServiceProtocol.CMD_UDP_TEST_IN + " 100 1 +ID1",
						QoSServiceProtocol.CMD_UDP_TEST_IN + " 200 1 +ID2",
						QoSServiceProtocol.CMD_UDP_TEST_IN + " 300 1 +ID3",
						"QUIT");				
			}
		};

		final ClientHandler ch = new ClientHandler(serverSocket, socket);

		ch.run();
		
		assertEquals("UDP IN test count != 3", 3, clientMessages.size());
	}

}
