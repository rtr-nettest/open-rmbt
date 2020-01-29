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

package at.rtr.rmbt.qos.testserver.mock;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;

import mockit.Mock;
import mockit.MockUp;

/**
 * 
 * @author Lukasz Budryk (lb@alladin.at)
 *
 */
public class SocketWithCountDownLatchMockup extends MockUp<Socket> {
		
	final CountDownLatch latch;
	
	public SocketWithCountDownLatchMockup(final CountDownLatch latch) {
		this.latch = latch;
	}
	
	@Mock
	public synchronized void close() throws IOException {
		latch.countDown();
	}

}
