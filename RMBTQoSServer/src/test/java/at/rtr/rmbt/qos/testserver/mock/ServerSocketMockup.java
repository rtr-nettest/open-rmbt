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
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;

import at.rtr.rmbt.qos.testserver.TestServerImpl;
import mockit.Mock;
import mockit.MockUp;

/**
 * 
 * @author Lukasz Budryk (lb@alladin.at)
 *
 */
public class ServerSocketMockup extends MockUp<ServerSocket> {
	
	public final TestServerImpl testServer;
	
	public SocketAddress endpoint;
	
	public ServerSocketMockup(final TestServerImpl ts) throws IOException {
		this.testServer = ts;
	}
	
	@Mock
	public boolean isBound() {
		return true;
	}
	
	@Mock
	public void bind(SocketAddress endpoint) throws IOException {
		this.endpoint = endpoint;
	}
	
	@Mock
    public boolean isClosed() {
		return false;
	}
	
	@Mock
	public Socket accept() throws IOException {
		return new Socket();
	}
}