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
import at.alladin.rmbt.qos.testserver.entity.TestCandidate;
import at.alladin.rmbt.qos.testserver.service.IntervalJob;
import at.alladin.rmbt.qos.testserver.util.TestServerConsole;

/**
 * 
 * @author lb
 *
 */
public class TcpWatcherRunnable extends IntervalJob<String> {
	
	/**
	 * 
	 */
	public final static String TAG = TcpWatcherRunnable.class.getCanonicalName();
	
	/**
	 * 
	 */
	public final static boolean RESTART_ON_ERROR = true;

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
	/*
	 * (non-Javadoc)
	 * @see at.alladin.rmbt.qos.testserver.service.AbstractJob#execute()
	 */
	@Override
	public String execute() throws Exception {
		if (TestServer.tcpServerMap != null) {
			synchronized (TestServer.tcpServerMap) {
				Iterator<List<TcpMultiClientServer>> tcpListIterator = TestServer.tcpServerMap.values().iterator();
				while(tcpListIterator.hasNext()) {
					List<TcpMultiClientServer> tcpServerList = tcpListIterator.next();
					Iterator<TcpMultiClientServer> iterator = tcpServerList.iterator();
					
					while (iterator.hasNext()) {
						TcpMultiClientServer tcpServer = iterator.next();
						
						if (!TestServer.serverPreferences.isIpCheck()) {
							//if ip checking is disabled and the ttl has been reached: close tcp socket
							if (System.currentTimeMillis() >= tcpServer.getTtlTimestamp().get()) {
								if (tcpServer.close()) {
									iterator.remove();
									TestServerConsole.log(service.getName() + " Removed object: " + tcpServer.getName(), 1, TestServerServiceEnum.TCP_SERVICE);
								}
							}
						}
						else {
							//if ip checking is enabled
							//iterate through all test candidates and remove all where the ttl has been reached
							synchronized (tcpServer.getCandidateMap()) {
								Iterator<Entry<InetAddress, TestCandidate>> incomingMapIterator = tcpServer.getCandidateMap().entrySet().iterator();
								while (incomingMapIterator.hasNext()) {
									Entry<InetAddress, TestCandidate> entry = incomingMapIterator.next();
									if (entry.getValue().getTtl() < System.currentTimeMillis()) {
										log(service.getName() + " TCP Client TTL reached and removed: " + entry.getValue(), 0);
										incomingMapIterator.remove();
										removeCounter++;
									}
								}								
							}
						}				
					}
				}
			}
		}
		
		return "removed dead candidates: " + removeCounter;
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
	public TcpWatcherRunnable getNewInstance() {
		return new TcpWatcherRunnable(); 
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
