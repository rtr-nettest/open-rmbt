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

package at.rtr.rmbt.qos.testserver.tcp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import at.rtr.rmbt.qos.testserver.mock.SocketWithCountDownLatchMockup;
import at.rtr.rmbt.qos.testserver.mock.util.SocketCommunicationExpectationsUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import at.rtr.rmbt.qos.testserver.ServerPreferences;
import at.rtr.rmbt.qos.testserver.ServerPreferences.TestServerServiceEnum;
import at.rtr.rmbt.qos.testserver.TestServer;
import at.rtr.rmbt.qos.testserver.entity.TestCandidate;
import at.rtr.rmbt.qos.testserver.tcp.competences.Action;
import at.rtr.rmbt.qos.testserver.tcp.competences.BasicCompetence;
import at.rtr.rmbt.qos.testserver.tcp.competences.Competence;
import at.rtr.rmbt.qos.testserver.tcp.competences.ResponseAction;
import at.rtr.rmbt.qos.testserver.tcp.competences.sip.SipCompetence;
import at.rtr.rmbt.qos.testserver.util.TestServerConsole;
import mockit.Delegate;
import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;

/**
 * 
 * @author Lukasz Budryk (lb@alladin.at)
 *
 */
public class TcpClientHandlerIntegrationTest {

	@Mocked Socket socket;
	
	@Mocked TcpMultiClientServer tcpServer;
	
	CountDownLatch latch;
	
	SocketWithCountDownLatchMockup slm;
	
	@Before
	public void init() throws IOException {
		latch = new CountDownLatch(1);
		slm = new SocketWithCountDownLatchMockup(latch);
	}
	
	@After
	public void teardown() {
		TestServer.newInstance();
	}
	
	@Test
	public void testClientHandlerWithSingleLineRequestMessage() throws Exception {
		TestServer.getInstance().serverPreferences = new ServerPreferences();
		final ByteArrayOutputStream os = new ByteArrayOutputStream();		
		SocketCommunicationExpectationsUtil.createExpectationWithOutputStream(socket, os, "MESSAGE\n");
		TcpClientHandler tch = new TcpClientHandler(socket, tcpServer);

		new Expectations() {
			{
				tcpServer.getCompetences();
				result = new Delegate<Deque<Competence>>() {
					public Deque<Competence> delegate() {
						final Deque<Competence> r = new ArrayDeque<>();
						r.addFirst(new BasicCompetence());
						r.addFirst(new SipCompetence());
						return r;
					}
				};
			};
		};
		
		tch.run();
		
		final boolean reachedZeroCountdown = latch.await(10L, TimeUnit.SECONDS);				
		assertTrue("CountDownLatch hasn't reached 0 and ran into a timeout", reachedZeroCountdown);
		assertEquals("TCP/NTP response != 'MESSAGE\n'", "MESSAGE\n", os.toString());		
	}
	
	@Test
	public void testClientHandlerWithSingleMultiLineRequestMessage() throws Exception {
		TestServer.getInstance().serverPreferences = new ServerPreferences();
		final ByteArrayOutputStream os = new ByteArrayOutputStream();
		SocketCommunicationExpectationsUtil.createExpectationWithOutputStream(socket, os, "MULTILINE\n");		
		TcpClientHandler tch = new TcpClientHandler(socket, tcpServer);

		new Expectations() {
			{
				tcpServer.getCompetences(); 
				result = new Delegate<Deque<Competence>>() {
					public Deque<Competence> delegate() {
						final Deque<Competence> r = new ArrayDeque<>();
						r.addFirst(new BasicCompetence());
						r.addFirst(new SipCompetence());
						return r;
					}
				};
			}
		};
		
		tch.run();
		
		final boolean reachedZeroCountdown = latch.await(10L, TimeUnit.SECONDS);				
		assertTrue("CountDownLatch hasn't reached 0 and ran into a timeout", reachedZeroCountdown);
		assertEquals("TCP/NTP response != 'MULTILINE\n'", "MULTILINE\n", os.toString());
	}

	@Test
	public void testClientHandlerWithSingleMultiLineRequestMessageAndCustomCompetenceBeingUsed() throws Exception {
		TestServer.getInstance().serverPreferences = new ServerPreferences();
		final ByteArrayOutputStream os = new ByteArrayOutputStream();
		SocketCommunicationExpectationsUtil.createExpectationWithOutputStream(socket, os, "MULTILINE\nMESSAGE\n");		
		TcpClientHandler tch = new TcpClientHandler(socket, tcpServer);

		new Expectations() {
			{
				tcpServer.getCompetences(); 
				result = new Delegate<Deque<Competence>>() {
					public Deque<Competence> delegate() {
						final Deque<Competence> r = new ArrayDeque<>();
						r.addFirst(new BasicCompetence());
						r.addFirst(new Competence() {
							
							@Override
							public List<Action> processRequest(String data) {
								final List<Action> result = new ArrayList<>();
								result.add(new ResponseAction(data.getBytes()));
								return result;
							}
							
							@Override
							public boolean appliesTo(String data) {
								return "MULTILINE\n".equals(data);
							}

							@Override
							public String readFullRequest(String firstLine, BufferedReader br) throws IOException {
								return "MULTILINE\nMESSAGE\n";
							}
						});
						return r;
					}
				};
			}
		};
		
		tch.run();
		
		final boolean reachedZeroCountdown = latch.await(10L, TimeUnit.SECONDS);
		assertTrue("CountDownLatch hasn't reached 0 and ran into a timeout", reachedZeroCountdown);
		assertEquals("TCP/NTP response != 'MULTILINE\nMESSAGE\n'", "MULTILINE\nMESSAGE\n", os.toString());		
	}
	
	@Test
	public void testClientHandlerWithSingleMultiLineRequestMessageAndCustomCompetenceBeingIgnored() throws Exception {
		TestServer.getInstance().serverPreferences = new ServerPreferences();
		final ByteArrayOutputStream os = new ByteArrayOutputStream();
		SocketCommunicationExpectationsUtil.createExpectationWithOutputStream(socket, os, "MULTILINES\nMESSAGE");		
		TcpClientHandler tch = new TcpClientHandler(socket, tcpServer);

		new Expectations() {
			{
				tcpServer.getCompetences(); 
				result = new Delegate<Deque<Competence>>() {
					public Deque<Competence> delegate() {
						final Deque<Competence> r = new ArrayDeque<>();
						r.addFirst(new BasicCompetence());
						r.addFirst(new Competence() {
							
							@Override
							public List<Action> processRequest(String data) {
								final List<Action> result = new ArrayList<>();
								result.add(new ResponseAction("MULTILINES\n".getBytes()));
								return result;
							}
							
							@Override
							public boolean appliesTo(String data) {
								final String s = new String(data);
								final boolean a = "MULTILINES\nMESSAGE".equals(s);
								return a;
							}

							@Override
							public String readFullRequest(String firstLine, BufferedReader br) throws IOException {
								return firstLine;
							}
						});
						return r;
					}
				};
			}
		};
		
		tch.run();
		
		final boolean reachedZeroCountdown = latch.await(10L, TimeUnit.SECONDS);
		assertTrue("CountDownLatch hasn't reached 0 and ran into a timeout", reachedZeroCountdown);
		assertEquals("TCP/NTP response != 'MULTILINES\n'", "MULTILINES\n", os.toString());		
	}

	@Test
	public void testClientHandlerWithIpCheckEnabledAndClientSocketContainingValidSocketAddress() throws Exception {
		TestServer.getInstance().serverPreferences = new ServerPreferences();
		TestServer.getInstance().serverPreferences.setIpCheck(true);
		
		new Expectations() {
			{
				tcpServer.getCandidateMap(); 
				result = new Delegate<Map<InetAddress, TestCandidate>>() {
					public Map<InetAddress, TestCandidate> delegate() {
						final Map<InetAddress, TestCandidate> map = new HashMap<InetAddress, TestCandidate>();
						map.put(socket.getInetAddress(), new TestCandidate());
						return map;
					}
				};
				
				tcpServer.getCompetences(); 
				result = new Delegate<Deque<Competence>>() {
					public Deque<Competence> delegate() {
						final Deque<Competence> r = new ArrayDeque<>();
						r.addFirst(new BasicCompetence());
						r.addFirst(new SipCompetence());
						return r;
					}
				};
			}
		};
				
		final ByteArrayOutputStream os = new ByteArrayOutputStream();		
		SocketCommunicationExpectationsUtil.createExpectationWithOutputStream(socket, os, "MESSAGE\n");		
		TcpClientHandler tch = new TcpClientHandler(socket, tcpServer);

		tch.run();

		final boolean reachedZeroCountdown = latch.await(10L, TimeUnit.SECONDS);				
		assertTrue("CountDownLatch hasn't reached 0 and ran into a timeout", reachedZeroCountdown);	
		assertEquals("TCP/NTP response != 'MESSAGE\n'", "MESSAGE\n", os.toString());
	}

	@Test
	public void testClientHandlerWithIpCheckEnabledAndClientSocketContainingUnknownSocketAddress() throws Exception {
		TestServer.getInstance().serverPreferences = new ServerPreferences();
		TestServer.getInstance().serverPreferences.setIpCheck(true);

		final List<String> logList = new ArrayList<>();
		
		new MockUp<TestServerConsole>() {
			
			@Mock
			public void log(String msg, int verboseLevelNeeded, TestServerServiceEnum service) {
				logList.add(msg);
			}
		};
		
		final ByteArrayOutputStream os = new ByteArrayOutputStream();		
		SocketCommunicationExpectationsUtil.createExpectationWithOutputStream(socket, os, "MESSAGE");		
		TcpClientHandler tch = new TcpClientHandler(socket, tcpServer);

		tch.run();

		final boolean reachedZeroCountdown = latch.await(10L, TimeUnit.SECONDS);				
		assertTrue("CountDownLatch hasn't reached 0 and ran into a timeout", reachedZeroCountdown);	
		
		assertEquals("Amount of logged messages != 2", 2, logList.size());
		
		assertEquals("Logged messages [1] != '/1.1.1.1: not a valid candidate for TCP/NTP'", 
				"/1.1.1.1: not a valid candidate for TCP/NTP", logList.get(1));
	}
	
	@Test
	public void testClientHandlerCatchingSocketTimeoutExceptionThenClosingSocket() throws Exception {
		TestServer.getInstance().serverPreferences = new ServerPreferences();
		SocketCommunicationExpectationsUtil.createExpectationWithThrownException(socket, new SocketTimeoutException(), "PING");		
		TcpClientHandler tch = new TcpClientHandler(socket, tcpServer);		
		tch.run();
		final boolean reachedZeroCountdown = latch.await(10L, TimeUnit.SECONDS);				
		assertTrue("CountDownLatch hasn't reached 0 and ran into a timeout", reachedZeroCountdown);
	}
	
	@Test
	public void testClientHandlerCatchingIOExceptionThenClosingSocket() throws Exception {
		TestServer.getInstance().serverPreferences = new ServerPreferences();
		SocketCommunicationExpectationsUtil.createExpectationWithThrownException(socket, new IOException(), "PING");		
		TcpClientHandler tch = new TcpClientHandler(socket, tcpServer);
		tch.run();
		final boolean reachedZeroCountdown = latch.await(10L, TimeUnit.SECONDS);				
		assertTrue("CountDownLatch hasn't reached 0 and ran into a timeout", reachedZeroCountdown);
	}
	
	@Test
	public void testClientHandlerCatchingMultipleExceptionWhileClosingClientSocket() throws Exception {
		TestServer.getInstance().serverPreferences = new ServerPreferences();
		
		slm = new SocketWithCountDownLatchMockup(latch) {
			
			@Mock
			public synchronized void close() throws IOException {
				super.close();
				throw new IOException();
			}
		};
		
		SocketCommunicationExpectationsUtil.createExpectationWithThrownException(socket, new SocketTimeoutException(), "PING");
		//SocketCommunicationExpectationsUtil.createExpectationWithThrownExceptionOnClose(socket, new IOException());

		TcpClientHandler tch = new TcpClientHandler(socket, tcpServer);
		
		tch.run();
		final boolean reachedZeroCountdown = latch.await(10L, TimeUnit.SECONDS);				
		assertTrue("CountDownLatch hasn't reached 0 and ran into a timeout", reachedZeroCountdown);
	}
}
