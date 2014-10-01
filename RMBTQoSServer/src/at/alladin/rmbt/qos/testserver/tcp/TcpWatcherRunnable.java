/*******************************************************************************
 * Copyright 2013-2014 alladin-IT GmbH
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
package at.alladin.rmbt.qos.testserver.tcp;

import java.net.InetAddress;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import at.alladin.rmbt.qos.testserver.ServerPreferences.TestServerServiceEnum;
import at.alladin.rmbt.qos.testserver.TestServer;
import at.alladin.rmbt.qos.testserver.service.IntervalJob;

/**
 * 
 * @author lb
 *
 */
public class TcpWatcherRunnable extends IntervalJob<String> {
	
	/**
	 * 
	 */
	public TcpWatcherRunnable() {
		super(TestServerServiceEnum.TCP_SERVICE);
	}
	
	/**
	 * 
	 */
	protected long removeCounter = 0;
	
	/**
	 * 
	 */
	protected long executionCounter = 0;
	
	/*
	 * (non-Javadoc)
	 * @see at.alladin.rmbt.qos.testserver.service.AbstractJob#execute()
	 */
	@Override
	public String execute() throws Exception {
		executionCounter++;
		if (TestServer.udpServerMap != null) {
			Iterator<List<TcpServer>> tcpListIterator = TestServer.tcpServerMap.values().iterator();
			while(tcpListIterator.hasNext()) {
				Iterator<TcpServer> iterator = tcpListIterator.next().iterator();
				while (iterator.hasNext()) {
					TcpServer tcpServer = iterator.next();
					
					if (!TestServer.serverPreferences.isIpCheck()) {
						//if ip checking is disabled and the ttl has been reached: close tcp socket
						if (System.currentTimeMillis() >= tcpServer.getTtlTimestamp()) {
							tcpServer.close();
						}
					}
					else {
						//if ip checking is enabled
						//iterate through all test candidates and remove all where the ttl has been reached
						Iterator<Entry<InetAddress, TcpTestCandidate>> incomingMapIterator = tcpServer.getCandidateSet().entrySet().iterator();
						while (incomingMapIterator.hasNext()) {
							Entry<InetAddress, TcpTestCandidate> entry = incomingMapIterator.next();
							if (entry.getValue().getTtl() < System.currentTimeMillis()) {
								log("TCP Client (ServerPort: " + tcpServer.getPort() + ") TTL reached and removed: " + entry.getValue(), 0);
								incomingMapIterator.remove();
								removeCounter++;
							}
						}
					}
				}
			}
		}
		
		return "times executed: " + executionCounter + ",  removed dead candidates: " + removeCounter;
	}
}
