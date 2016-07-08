/*******************************************************************************
 * Copyright 2013-2016 alladin-IT GmbH
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
import at.alladin.rmbt.qos.testserver.servers.AbstractUdpServer;
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
	public final static String TAG = UdpWatcherRunnable.class.getCanonicalName();
	
	/**
	 * 
	 */
	public final static boolean RESTART_ON_ERROR = true;
	
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
	
	/*
	 * (non-Javadoc)
	 * @see at.alladin.rmbt.qos.testserver.service.AbstractJob#execute()
	 */
	@Override
	public String execute() {
		int healthy = 0;
		int unhealthy = 0;
		if (TestServer.udpServerMap != null) {
			synchronized (TestServer.udpServerMap) {
				Iterator<List<AbstractUdpServer<?>>> listIterator = TestServer.udpServerMap.values().iterator();
				while (listIterator.hasNext()) {
					Iterator<AbstractUdpServer<?>> iterator = listIterator.next().iterator();
					while (iterator.hasNext()) {
						AbstractUdpServer<?> udpServer = iterator.next();
						Iterator<?> incomingMapIterator = udpServer.getIncomingMap().entrySet().iterator();
						if (!udpServer.isHealthy()) {
							log("UDP Server " +  udpServer.getAddress() + ":" + udpServer.getLocalPort() + " found HEALTH-ERROR", 0);
							log("UDP Server " +  udpServer.getAddress() + ":" + udpServer.getLocalPort() + " status: " + udpServer.getStatusMessage(), 0);
							unhealthy++;
						}
						else {
							healthy++;
						}
						
						while (incomingMapIterator.hasNext()) {
							@SuppressWarnings("unchecked")
							Entry<String, UdpTestCandidate> entry = (Entry<String, UdpTestCandidate>) incomingMapIterator.next();
							if (entry.getValue().getTtl() < System.currentTimeMillis()) {
								log("UDP Client (ServerPort: " + udpServer.getLocalPort() + ") TTL reached and removed: " + entry.getValue(), 0);
								incomingMapIterator.remove();
								removeCounter++;
							}
						}
					}
				}
			}
		}
		return "healthy servers: " + healthy + ", unhealthy servers: " + unhealthy + "; removed dead candidates: " + removeCounter;
	}

	/*
	 * (non-Javadoc)
	 * @see at.alladin.rmbt.qos.testserver.service.IntervalJob#restartOnError()
	 */
	@Override
	public boolean restartOnError() {
		return RESTART_ON_ERROR;
	}

	/*
	 * (non-Javadoc)
	 * @see at.alladin.rmbt.qos.testserver.service.AbstractJob#getNewInstance()
	 */
	@Override
	public UdpWatcherRunnable getNewInstance() {
		return new UdpWatcherRunnable();
	}

	/*
	 * (non-Javadoc)
	 * @see at.alladin.rmbt.qos.testserver.service.AbstractJob#getId()
	 */
	@Override
	public String getId() {
		return TAG;
	}	
}
