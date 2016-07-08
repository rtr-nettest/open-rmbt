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

import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import at.alladin.rmbt.qos.testserver.ServerPreferences.TestServerServiceEnum;
import at.alladin.rmbt.qos.testserver.entity.TestCandidate;
import at.alladin.rmbt.qos.testserver.util.TestServerConsole;

/**
 * 
 * @author lb
 *
 */
public abstract class AbstractServer<T extends AutoCloseable, H extends TestCandidate> implements Runnable {

	////////////////////////////////////////////////
	// fields:
	////////////////////////////////////////////////

	private final String name;
	private final TestServerServiceEnum testServerService;

	protected final Class<T> serverSocketTypeClazz;
	protected final Class<H> clientDataHolderClazz;
	
	/**
	 * holds the candidates for this socket
	 */
	protected final ConcurrentHashMap<InetAddress, H> candidateMap = new ConcurrentHashMap<>();

	
	/**
	 * time to live of this server
	 */
	protected final AtomicLong ttlTimestamp = new AtomicLong(0);
	
	/**
	 * the server socket
	 */
	protected T serverSocket;
	
	/**
	 * the port of this socket
	 */
	protected final int port;
	
	/**
	 * 
	 */
	protected final InetAddress inetAddr;

	////////////////////////////////////////////////
	// constructors:
	////////////////////////////////////////////////
	
	public AbstractServer(Class<T> serverSocketTypeClazz, Class<H> clientHandlerRunnableClazz, 
			InetAddress inetAddr, int port, String tag, TestServerServiceEnum testServerService) {
		this.serverSocketTypeClazz = serverSocketTypeClazz;
		this.clientDataHolderClazz = clientHandlerRunnableClazz;
		this.name = tag + " [" + inetAddr + ":" + port + "]";
		this.port = port;
		this.inetAddr = inetAddr;
		this.testServerService = testServerService;
	}

	////////////////////////////////////////////////
	// abstract methods:
	////////////////////////////////////////////////
	
	public abstract void prepare() throws Exception;
	
	public abstract boolean close() throws Exception;
	
	public abstract boolean isAlive();
	
	protected abstract void execute() throws Exception;

	////////////////////////////////////////////////
	// getter/setter methods:
	////////////////////////////////////////////////

	public T getServerSocket() {
		return serverSocket;
	}
	
	public void setServerSocket(T serverSocket) {
		this.serverSocket = serverSocket;
	}

	public String getName() {
		return name;
	}

	public AtomicLong getTtlTimestamp() {
		return ttlTimestamp;
	}
	
	public void refreshTtl(long byValue) {
		ttlTimestamp.set(System.currentTimeMillis() + byValue);
		TestServerConsole.log(getName() + " Refreshing TTL to: " + ttlTimestamp.get(), 2, testServerService);
	}

	public int getPort() {
		return port;
	}

	public InetAddress getInetAddr() {
		return inetAddr;
	}
	
	////////////////////////////////////////////////
	// methods:
	////////////////////////////////////////////////
	
	protected synchronized void closeSocket() {
		try {
			this.serverSocket.close();
			this.serverSocket = null;
		}
		catch (Exception e) {
			TestServerConsole.error(getName() + " could not close socket", e, 2, testServerService);
		}
	}

	@Override
	public void run() {
		TestServerConsole.log(getName() + " started!" , 2, testServerService);
		
		try {
			while(isAlive()) {
				execute();
			}
			
		}
		catch (SocketTimeoutException e) {
			TestServerConsole.error(getName(), e, 2, testServerService);
		}
		catch (Exception e) {
			TestServerConsole.error(getName(), e, 1, testServerService);
		} 
		finally {
			try {
				//close this socket if necessary
				close();									
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		TestServerConsole.log(getName() +  " closed!", 0, testServerService);
	}
	
	/**
	 * 
	 * @param candidateInetAddress
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	@SuppressWarnings("unchecked")
	public synchronized TestCandidate registerCandidate(InetAddress candidateInetAddress, int resetTtl) {
		try {
			TestCandidate candidate = null;
			if (candidateMap.containsKey(candidateInetAddress)) {
				candidate = (TestCandidate) candidateMap.get(candidateInetAddress);
			}
			else {
				candidate = clientDataHolderClazz.newInstance();
				candidate.setTtl(resetTtl);
			}
			
			candidate.increaseTestCounter(true);
			
			TestServerConsole.log(getName() + " Registering candidate " + candidateInetAddress + ": " + candidate + ")", 1, testServerService);

			candidateMap.put(candidateInetAddress, (H) candidate);
			
			return candidate;			
		}
		catch (Exception e) {
			TestServerConsole.error(getName(), e, 2, testServerService);
			return null;
		}
	}
	
	/**
	 * 
	 * @param candidateInetAddress
	 */
	public synchronized void removeCandidate(InetAddress candidateInetAddress) {
		if (candidateMap.containsKey(candidateInetAddress)) {
			TestCandidate candidate = candidateMap.get(candidateInetAddress);
			if (candidate.decreaseTestCounter(true) <= 0) {
				TestServerConsole.log(getName() + " Candidate " + candidateInetAddress + " has no more tests left.", 1, testServerService);
				candidateMap.remove(candidateInetAddress);
			}
			else {
				TestServerConsole.log(getName() + " Candidate (" + candidate + " - " + candidateInetAddress + ") has " + (candidate.getTestCounter()) + " tests left.", 1, testServerService);
			}
		}		
	}

	/**
	 * 
	 * @return
	 */
	public Map<InetAddress, H> getCandidateMap() {
		return candidateMap;
	}
}
