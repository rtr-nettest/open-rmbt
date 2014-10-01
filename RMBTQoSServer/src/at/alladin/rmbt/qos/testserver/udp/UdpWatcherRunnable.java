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
package at.alladin.rmbt.qos.testserver.udp;

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
public class UdpWatcherRunnable extends IntervalJob<String> {

	/**
	 * 
	 */
	public UdpWatcherRunnable() {
		super(TestServerServiceEnum.UDP_SERVICE);
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
	public String execute() {
		executionCounter++;
		if (TestServer.udpServerMap != null) {
			Iterator<List<UdpMultiClientServer>> listIterator = TestServer.udpServerMap.values().iterator();
			while (listIterator.hasNext()) {
				Iterator<UdpMultiClientServer> iterator = listIterator.next().iterator();
				while (iterator.hasNext()) {
					UdpMultiClientServer udpServer = iterator.next();
					Iterator<Entry<String, ClientUdpData>> incomingMapIterator = udpServer.getIncomingMap().entrySet().iterator();
					while (incomingMapIterator.hasNext()) {
						Entry<String, ClientUdpData> entry = incomingMapIterator.next();
						if (entry.getValue().getTtl() < System.currentTimeMillis()) {
							log("UDP Client (ServerPort: " + udpServer.getDatagramSocket().getLocalPort() + ") TTL reached and removed: " + entry.getValue(), 0);
							incomingMapIterator.remove();
							removeCounter++;
						}
					}
				}
			}
		}
		return "times executed: " + executionCounter + ",  removed dead candidates: " + removeCounter;
	}	
}
