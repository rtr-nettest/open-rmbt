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

import java.io.DataInputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import mockit.Mock;
import mockit.MockUp;

/**
 * 
 * @author Lukasz Budryk (lb@alladin.at)
 *
 */
public class DataInputStreamMockup extends MockUp<DataInputStream> {
	
	private final String[] messages;
	
	private final AtomicInteger indexAvailable = new AtomicInteger(0);
	
	private final AtomicInteger index = new AtomicInteger(0);
	
	public DataInputStreamMockup(final String[] messages) throws IOException {
		this.messages = messages;
	}
	
	@Mock
    public int available() throws IOException {
		if (indexAvailable.get() >= messages.length) {
			return -1;
		}
		final String msg = messages[indexAvailable.getAndAdd(1)];
		return msg.length();
    }

    @Mock
    public int read(byte b[]) throws IOException {
		if (index.get() >= messages.length) {
			return -1;
		}						
		final byte[] msg = messages[index.getAndAdd(1)].getBytes();
		System.arraycopy(msg, 0, b, 0, msg.length);
		return msg.length;
    }

}