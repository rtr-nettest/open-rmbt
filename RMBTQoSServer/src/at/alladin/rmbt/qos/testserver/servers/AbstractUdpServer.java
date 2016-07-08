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
package at.alladin.rmbt.qos.testserver.servers;

import java.io.Closeable;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.concurrent.ConcurrentHashMap;

import at.alladin.rmbt.qos.testserver.entity.Observable;
import at.alladin.rmbt.qos.testserver.entity.TestCandidate;
import at.alladin.rmbt.qos.testserver.udp.UdpTestCandidate;

public abstract class AbstractUdpServer<T extends Closeable> implements Runnable, Observable {
	
	protected final ConcurrentHashMap<String, UdpTestCandidate> incomingMap = new ConcurrentHashMap<>();
	
	protected final Class<?> clazz;
	
	public AbstractUdpServer(Class<?> clazz) {
		this.clazz = clazz;
	}
	
	public Class<?> getClazz() {
		return clazz;
	}

	/**
	 * 
	 * @param uuid
	 */
	public synchronized UdpTestCandidate getClientData(String uuid) {
		return incomingMap.get(uuid);
	}
	
	/**
	 * 
	 * @param uuid
	 * @return
	 */
	public synchronized TestCandidate pollClientData(String uuid) {
		return incomingMap.remove(uuid);
	}

	/**
	 * 
	 * @return
	 */
	public synchronized ConcurrentHashMap<String, UdpTestCandidate> getIncomingMap() {
		return incomingMap;
	}
	
	/**
	 * 
	 * @return
	 */
	public abstract T getSocket();
	
	/**
	 * 
	 * @param dp
	 * @throws IOException 
	 */
	public abstract void send(DatagramPacket dp) throws IOException;
	
	/**
	 * 
	 * @return
	 */
	public abstract boolean getIsRunning();
	
	/**
	 * 
	 * @return
	 */
	public abstract InetAddress getAddress();
	
	/**
	 * 
	 * @return
	 */
	public abstract int getLocalPort();	
}
