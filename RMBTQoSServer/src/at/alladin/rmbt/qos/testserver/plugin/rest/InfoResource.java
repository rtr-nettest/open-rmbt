/*******************************************************************************
 * Copyright 2016 Specure GmbH
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
 *******************************************************************************/
package at.alladin.rmbt.qos.testserver.plugin.rest;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import at.alladin.rmbt.qos.testserver.TestServer;
import at.alladin.rmbt.qos.testserver.servers.AbstractUdpServer;
import at.alladin.rmbt.qos.testserver.tcp.TcpMultiClientServer;
import at.alladin.rmbt.qos.testserver.udp.UdpTestCandidate;

/**
 * 
 * @author lb
 *
 */
public class InfoResource extends ServerResource {

	@Get
	public String request() throws JSONException {
		JSONObject json = new JSONObject();
		
		String typeAttr = getAttribute("type");
		
		if ("udp".equals(typeAttr)) {
			json.put("protocol_type", typeAttr);
			
			final JSONArray serverArray = new JSONArray();
			
			synchronized(TestServer.udpServerMap) {
				Iterator<Entry<Integer, List<AbstractUdpServer<?>>>> i = TestServer.udpServerMap.entrySet().iterator();
				while (i.hasNext()) {
					Entry<Integer, List<AbstractUdpServer<?>>> e = i.next();
					JSONObject udpObject = new JSONObject();
					udpObject.put("port", e.getKey());
					
					final JSONArray udpArray = new JSONArray();
					for (AbstractUdpServer<?> u : e.getValue()) {
						final JSONObject udpServerObject = new JSONObject();
						udpServerObject.put("address", u.getAddress().toString());
						udpServerObject.put("running", u.getIsRunning());
						
						final JSONArray clientArray = new JSONArray();
						for (Entry<String, UdpTestCandidate> ud : u.getIncomingMap().entrySet()) {
							final JSONObject clientObject = new JSONObject();
							clientObject.put("client", ud.getKey());
							if (ud.getValue() instanceof UdpTestCandidate) {
								clientObject.put("rcv", ud.getValue().getPacketsReceived().size());
								clientObject.put("dup", ud.getValue().getPacketDuplicates().size());
							}
							clientArray.put(clientObject);
						}
						udpServerObject.put("clients", clientArray);
						udpArray.put(udpServerObject);
					}
					udpObject.put("server_list", udpArray);
					serverArray.put(udpObject);
				}
			}
			
			json.put("servers", serverArray);
		}
		else if ("tcp".equals(typeAttr)) {
			json.put("protocol_type", typeAttr);
			
			final JSONArray serverArray = new JSONArray();
			
			synchronized(TestServer.tcpServerMap) {
				Iterator<Entry<Integer, List<TcpMultiClientServer>>> i = TestServer.tcpServerMap.entrySet().iterator();
				while (i.hasNext()) {
					Entry<Integer, List<TcpMultiClientServer>> e = i.next();
					
					JSONObject tcpObject = new JSONObject();
					tcpObject.put("port", e.getKey());
					
					final JSONArray tcpArray = new JSONArray();
					for (TcpMultiClientServer t : e.getValue()) {
						final JSONObject tcpServerObject = new JSONObject();
						tcpServerObject.put("address", t.getServerSocket().toString());
						tcpServerObject.put("ttl", t.getTtlTimestamp());				
						tcpArray.put(tcpServerObject);
					}
					tcpObject.put("server_list", tcpArray);
					serverArray.put(tcpObject);
				}
			}
			
			json.put("servers", serverArray);
		}
		else {
			json.put("protocol_type", "unknown");
			RestService.addError(json, "unknown protocol");
			RestService.addError(json, "allowed protocols: 'udp', 'tcp'");
		}
		
		return json.toString();
	}
}
