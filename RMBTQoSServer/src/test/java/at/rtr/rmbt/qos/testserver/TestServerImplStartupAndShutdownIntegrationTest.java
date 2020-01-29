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

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import at.rtr.rmbt.qos.testserver.mock.ServerSocketMockup;
import org.junit.Test;

import mockit.Mock;

/**
 * 
 * @author Lukasz Budryk (lb@alladin.at)
 *
 */
public class TestServerImplStartupAndShutdownIntegrationTest {
	
	/**
	 * 
	 * @author Lukasz Budryk (lb@alladin.at)
	 *
	 */
	public class ServerSocketAcceptCounterMockup extends ServerSocketMockup {
		
		public long acceptCounter = 0;
		
		public ServerSocketAcceptCounterMockup(final TestServerImpl ts) throws IOException {
			super(ts);
		}
		
		@Mock
		public Socket accept() throws IOException {
			acceptCounter += 1;
			return null;
		}		
	}

	@Test
	public void testTestServerStartUpWithShutdownAfterOneIncomingConnection() throws Exception {
		final TestServerImpl ts = new TestServerImpl();
		ts.setShutdownHookEnabled(false);
		
		final CountDownLatch latch = new CountDownLatch(1);
		
		//mock server socket
		final ServerSocketAcceptCounterMockup ssm = new ServerSocketAcceptCounterMockup(ts) {
			
			@Mock
			public Socket accept() throws IOException {
				super.accept();
				testServer.shutdown();
				latch.countDown();
				return null;
			}
		};
		
		ts.run(new ServerPreferences(getClass().getResourceAsStream("config.properties")));
		final boolean reachedZeroCountdown = latch.await(10L, TimeUnit.SECONDS);
		
		assertTrue("CountDownLatch hasn't reached 0 and ran into a timeout", reachedZeroCountdown);
		assertFalse("QoSService is running", ts.getQoSService().isRunning());
		assertEquals("ServerSocket.accept() has been called != 1 time", 1, ssm.acceptCounter);
	}
}
