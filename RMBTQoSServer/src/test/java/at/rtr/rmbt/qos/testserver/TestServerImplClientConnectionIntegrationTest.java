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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import at.rtr.rmbt.qos.testserver.mock.ServerSocketMockup;
import at.rtr.rmbt.qos.testserver.mock.SocketWithCountDownLatchMockup;
import at.rtr.rmbt.qos.testserver.mock.util.SocketCommunicationExpectationsUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import at.rtr.rmbt.qos.testserver.ServerPreferences.TestServerServiceEnum;
import at.rtr.rmbt.qos.testserver.util.TestServerConsole;
import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;

/**
 * 
 * @author Lukasz Budryk (lb@alladin.at)
 *
 */
public class TestServerImplClientConnectionIntegrationTest {
	
	CountDownLatch latch;
	
	ServerSocketMockup ssm;
	
	TestServerImpl ts;
		
	@Mocked
	Socket socket;
	
	@Mocked
	TestServerConsole console;

	SocketWithCountDownLatchMockup slm;
	
	@Before
	public void init() throws IOException {
		//prepare a new instance of TestServerImpl
		ts = new TestServerImpl();
		ts.setShutdownHookEnabled(false);

		//initialize new CountDownLatch
		latch = new CountDownLatch(1);
		
		//fake ServerSocket.accept() method to return only once a Socket instance
		ssm = new ServerSocketMockup(ts) {
			
			int sockets = 0;
			@Mock
			public Socket accept() throws IOException {
				if (sockets < 1) {
					sockets++;
					return new Socket();
				}
				
				return null;
			}
		};
		
		slm = new SocketWithCountDownLatchMockup(latch);
	}
	
	@After
	public void teardown() {
		TestServer.newInstance();
	}
	
	@Test
	public void testClientConnectingToTestServerAndProvidingValidTokenAndQuittingSession() throws Exception {		
		ByteArrayOutputStream os = new ByteArrayOutputStream();

		final Expectations exp = SocketCommunicationExpectationsUtil.createExpectationWithOutputStream(
				socket, os, 
				"TOKEN bbd1ee96-0779-4619-b993-bb4bf7089754_1528136454_3gr2gw9lVhtVONV0XO62Vamu/uw=\nQUIT");
		
		ts.run(new ServerPreferences(getClass().getResourceAsStream("config.properties")));
		final boolean reachedZeroCountdown = latch.await(10L, TimeUnit.SECONDS);		
		ts.shutdown();
		
		final String[] answers = os.toString().split("\n");
		
		assertTrue("CountDownLatch hasn't reached 0 and ran into a timeout", reachedZeroCountdown);
		
		assertEquals("Total line of responses != 4", 4, answers.length);
		
		assertEquals("Initial response line 1 != '" + QoSServiceProtocol.RESPONSE_GREETING + "'", 
				QoSServiceProtocol.RESPONSE_GREETING, answers[0]);
		
		assertEquals("Initial response line 2 != '" + QoSServiceProtocol.RESPONSE_ACCEPT_TOKEN + "'", 
				QoSServiceProtocol.RESPONSE_ACCEPT_TOKEN, answers[1]);
		
		assertEquals("Token submit response line 1 != '" + QoSServiceProtocol.RESPONSE_OK + "'", 
				 QoSServiceProtocol.RESPONSE_OK, answers[2]);
		
		assertEquals("Token submit response line 2 != '" + QoSServiceProtocol.RESPONSE_ACCEPT_COMMANDS + "'", 
				QoSServiceProtocol.RESPONSE_ACCEPT_COMMANDS, answers[3]);
		
		assertFalse("QoSService is running", ts.getQoSService().isRunning());
	}

	//@Test
	public void testClientConnectingToTestServerAndProvidingInvalidToken() throws Exception {
		ByteArrayOutputStream os = new ByteArrayOutputStream();

		final List<Throwable> errList = new ArrayList<>();		
		new MockUp<TestServerConsole>() {
			
			@Mock
			void error(String info, Throwable t, int verboseLevelNeeded, TestServerServiceEnum service) {
				errList.add(t);
			}
		};
		
		final String errToken = "TOKEN bbd1ee96-0779-4619-b993-bb4bf70897541528136454_3gr2gw9lVhtVONV0XO62Vamu/uw=";
		final Expectations exp = SocketCommunicationExpectationsUtil.createExpectationWithOutputStream(
				socket, os, 
				errToken + "\nQUIT");

		ts.run(new ServerPreferences(getClass().getResourceAsStream("config.properties")));
		final boolean reachedZeroCountdown = latch.await(10L, TimeUnit.SECONDS);		
		ts.shutdown();
		
		final String[] answers = os.toString().split("\n");
		
		assertTrue("CountDownLatch hasn't reached 0 and ran into a timeout", reachedZeroCountdown);

		assertEquals("Total line of responses != 2", 2, answers.length);

		assertEquals("Initial response line 1 != '" + QoSServiceProtocol.RESPONSE_GREETING + "'", 
				QoSServiceProtocol.RESPONSE_GREETING, answers[0]);
		
		assertEquals("Initial response line 2 != '" + QoSServiceProtocol.RESPONSE_ACCEPT_TOKEN + "'", 
				QoSServiceProtocol.RESPONSE_ACCEPT_TOKEN, answers[1]);
						
		assertFalse("QoSService is running", ts.getQoSService().isRunning());

		assertEquals("Errors recorded != 1", 1, errList.size());
		
		assertEquals("Error [0] != IOException", IOException.class, errList.get(0).getClass());
		
		assertEquals("Error message != 'BAD TOKEN: " + errToken + "'", "BAD TOKEN: " + errToken, errList.get(0).getMessage());
	}
}
