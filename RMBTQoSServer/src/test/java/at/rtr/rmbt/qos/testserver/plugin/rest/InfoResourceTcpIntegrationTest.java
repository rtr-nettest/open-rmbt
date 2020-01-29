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

package at.rtr.rmbt.qos.testserver.plugin.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.junit.After;
import org.junit.Before;
import org.restlet.resource.ServerResource;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import at.rtr.rmbt.qos.testserver.ClientHandler;
import at.rtr.rmbt.qos.testserver.ServerPreferences;
import at.rtr.rmbt.qos.testserver.TestServer;
import at.rtr.rmbt.qos.testserver.tcp.TcpMultiClientServer;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;

/**
 * 
 * @author Lukasz Budryk (lb@alladin.at)
 *
 */
public class InfoResourceTcpIntegrationTest {

	InfoResource ir;
	
	@Mocked ServerSocket serverSocket;
	
	@Mocked DatagramChannel datagramChannel;
	
	@Mocked DatagramSocket datagramSocket;
	
	@Mocked DatagramPacket datagramPacket;
	
	@Mocked Socket socket;
	
	@Mocked ExecutorService executorService;
	
	@Mocked ClientHandler clientHandler;
	
	//@Mocked TestServerConsole console;
	
	@Before
	public void init() throws Exception {
		TestServer./*get*/newInstance().run(new ServerPreferences(getClass().getResourceAsStream("config_udp.properties")));
		ir = new InfoResource();
		
		new MockUp<RestService>() {
			
			@Mock
			public void start() throws UnknownHostException {
				//do nothing;
			}
		};
	}
	
	@After
	public void teardown() {
		TestServer.getInstance().shutdown();
		//TestServer.newInstance();
	}

	//@Test
	public void testRequestTcp() throws Exception {		
		new MockUp<ServerResource>() {
			@Mock
			public String getAttribute(String name) {
				return "tcp";
			}
		};
		
		final TcpMultiClientServer s = new TcpMultiClientServer(1000, InetAddress.getByName("1.2.3.4"));
		s.refreshTtl(4321);
		s.setServerSocket(serverSocket);
		final List<TcpMultiClientServer> tcpList = new ArrayList<>();
		tcpList.add(s);
		TestServer.getInstance().tcpServerMap.put(1000, tcpList);
		
		final JsonObject json = (JsonObject) new Gson().fromJson(ir.request(), JsonObject.class);
		assertNotNull(json);
		assertEquals("'protocol_type' != 'tcp'", "tcp", json.get("protocol_type").getAsString());
		assertEquals("Amount of TCP servers != 1", 1, json.get("servers").getAsJsonArray().size());
		assertEquals("TCP server [0] port != 1000", 1000, json.get("servers").getAsJsonArray().get(0).getAsJsonObject().get("port").getAsInt());
	}
	
	//@Test
	public void testRequestUnknownProtocol() throws Exception {		
		new MockUp<ServerResource>() {
			@Mock
			public String getAttribute(String name) {
				return "unknown_protocol_xyz";
			}
		};
		
		final JsonObject json = (JsonObject) new Gson().fromJson(ir.request(), JsonObject.class);
		assertNotNull(json);
		assertEquals("'protocol_type' != 'unknown'", "unknown", json.get("protocol_type").getAsString());
	}

}
