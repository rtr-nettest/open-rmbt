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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import at.rtr.rmbt.qos.testserver.mock.SocketWithCountDownLatchMockup;
import at.rtr.rmbt.qos.testserver.mock.TestServerConsoleMockup;
import at.rtr.rmbt.qos.testserver.mock.util.SocketCommunicationExpectationsUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import at.rtr.rmbt.qos.testserver.ServerPreferences;
import at.rtr.rmbt.qos.testserver.TestServer;
import at.rtr.rmbt.qos.testserver.tcp.competences.BasicCompetence;
import at.rtr.rmbt.qos.testserver.tcp.competences.Competence;
import at.rtr.rmbt.qos.testserver.tcp.competences.sip.SipCompetence;
import at.rtr.rmbt.util.net.sip.SipResponseMessage;
import at.rtr.rmbt.util.net.sip.SipResponseMessage.SipResponseType;
import at.rtr.rmbt.util.net.sip.SipUtil;
import mockit.Delegate;
import mockit.Expectations;
import mockit.Mocked;

/**
 * 
 * @author Lukasz Budryk (lb@alladin.at)
 *
 */
public class TcpClientHandlerSipItegrationTest {

	@Mocked Socket socket;
	
	@Mocked TcpMultiClientServer tcpServer;
	
	CountDownLatch latch;
	
	SocketWithCountDownLatchMockup slm;
	
	TestServerConsoleMockup tscm;
	
	@Before
	public void init() throws IOException {
		latch = new CountDownLatch(1);
		slm = new SocketWithCountDownLatchMockup(latch);
		tscm = new TestServerConsoleMockup();
	}
	
	@After
	public void teardown() {
		TestServer.newInstance();
	}

	@Test
	public void testClientHandlerWithSimpleSipWorkflowAndMockedCompetencesIncludingInviteAndTrying(
			@Mocked FilterOutputStream fos) throws Exception {
		TestServer.getInstance().serverPreferences = new ServerPreferences();
		final AtomicReference<String[]> results = new AtomicReference<>(new String[2]);
		SocketCommunicationExpectationsUtil.createExpectationWithMultipleResultStrings(socket, fos, results,
						"INVITE sip:bob@home SIP/2.0\n" + 
						"Via: SIP/2.0/TCP localhost:5060\n" + 
						"Max-Forwards: 70\n" + 
						"From: Alice <sip:alice@home>\n" + 
						"To: Bob <sip:bob@home>\n\n");
		
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

		final SipResponseMessage resTrying = SipUtil.parseResponseData(results.get()[0]);
		assertNotNull("SIP response == null", resTrying);
		assertEquals("SIP response != TRYING", SipResponseType.TRYING, resTrying.getType());
		assertEquals("FROM != Bob <sip:bob@home>", "Bob <sip:bob@home>", resTrying.getFrom());
		assertEquals("TO != Alice <sip:alice@home>", "Alice <sip:alice@home>", resTrying.getTo());
		assertEquals("VIA != SIP/2.0/TCP localhost:5060", "SIP/2.0/TCP localhost:5060", resTrying.getVia());
		
		final SipResponseMessage resRinging = SipUtil.parseResponseData(results.get()[1]);
		assertNotNull("SIP response == null", resRinging);
		assertEquals("SIP response != RINGING", SipResponseType.RINGING, resRinging.getType());
		assertEquals("FROM != Bob <sip:bob@home>", "Bob <sip:bob@home>", resRinging.getFrom());
		assertEquals("TO != Alice <sip:alice@home>", "Alice <sip:alice@home>", resRinging.getTo());
		assertEquals("VIA != SIP/2.0/TCP localhost:5060", "SIP/2.0/TCP localhost:5060", resRinging.getVia());
	}
	
	@Test
	public void testClientHandlerWithSimpleSipWorkflowAndMockedCompetencesIncludingInviteAndTryingAndCheckDelayBetweenPackets(
			@Mocked FilterOutputStream fos) throws Exception {
		TestServer.getInstance().serverPreferences = new ServerPreferences();
		final AtomicReference<String[]> results = new AtomicReference<>(new String[2]);
		SocketCommunicationExpectationsUtil.createExpectationWithMultipleResultStrings(socket, fos, results,
						"INVITE sip:bob@home SIP/2.0\n" + 
						"Via: SIP/2.0/TCP localhost:5060\n" + 
						"Max-Forwards: 70\n" + 
						"From: Alice <sip:alice@home>\n" + 
						"To: Bob <sip:bob@home>\n\n");
		
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

		final List<TestServerConsoleMockup.ConsoleLog> lc = tscm.getLogList();
		assertNotNull("Log is null", lc);
		TestServer.getInstance().getTempOut().println(lc);
		
		assertTrue("Log size < 4", lc.size() >= 4);

		final TestServerConsoleMockup.ConsoleLog tryingLog = lc.get(2);
		final TestServerConsoleMockup.ConsoleLog ringingLog = lc.get(3);
		assertNotNull("tryingLog is null", tryingLog);
		assertNotNull("ringingLog is null", ringingLog);
		
		assertTrue("tryingLog not after ringingLog", ringingLog.getTimeMs() > tryingLog.getTimeMs());
		assertTrue("tryingLog and ringingLog time diff < 100 (=" + ((ringingLog.getTimeMs() - tryingLog.getTimeMs())) + ")", (ringingLog.getTimeMs() - tryingLog.getTimeMs()) >= 100);
		
	}
	
	@Test
	public void testClientHandlerWithSimpleSipWorkflowIncludingInviteAndTryingAndCheckDelayBetweenPackets(
			@Mocked FilterOutputStream fos) throws Exception {
		TestServer.getInstance().serverPreferences = new ServerPreferences();
		final AtomicReference<String[]> results = new AtomicReference<>(new String[2]);
		SocketCommunicationExpectationsUtil.createExpectationWithMultipleResultStrings(socket, fos, results,
						"INVITE sip:bob@home SIP/2.0\n" + 
						"Via: SIP/2.0/TCP localhost:5060\n" + 
						"Max-Forwards: 70\n" + 
						"From: Alice <sip:alice@home>\n" + 
						"To: Bob <sip:bob@home>\n\n",
						"BYE sip:bob@home SIP/2.0\n" + 
						"Via: SIP/2.0/TCP localhost:5060\n" + 
						"Max-Forwards: 70\n" + 
						"From: Alice <sip:alice@home>\n" + 
						"To: Bob <sip:bob@home>\n\n");
		
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

		final List<TestServerConsoleMockup.ConsoleLog> lc = tscm.getLogList();
		
		assertNotNull("Log is null", lc);
		assertTrue("Log size < 4", lc.size() >= 4);
		
		final TestServerConsoleMockup.ConsoleLog tryingLog = lc.get(2);
		final TestServerConsoleMockup.ConsoleLog ringingLog = lc.get(3);
		assertNotNull("tryingLog is null", tryingLog);
		assertNotNull("ringingLog is null", ringingLog);
				
		assertTrue("tryingLog not after ringingLog", ringingLog.getTimeMs() > tryingLog.getTimeMs());
		
		assertTrue("tryingLog and ringingLog time diff < 100 (=" + ((ringingLog.getTimeMs() - tryingLog.getTimeMs())) + ")", (ringingLog.getTimeMs() - tryingLog.getTimeMs()) >= 100);
	}
}