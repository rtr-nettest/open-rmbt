/*******************************************************************************
 * Copyright 2013-2019 alladin-IT GmbH
 * Copyright 2014-2016 SPECURE GmbH
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

package at.rtr.rmbt.qos.testserver;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.channels.DatagramChannel;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.TrustManager;

import at.rtr.rmbt.qos.testserver.ServerPreferences.ServiceSetting;
import at.rtr.rmbt.qos.testserver.ServerPreferences.TestServerServiceEnum;
import at.rtr.rmbt.qos.testserver.ServerPreferences.UdpPort;
import at.rtr.rmbt.qos.testserver.entity.TestCandidate;
import at.rtr.rmbt.qos.testserver.servers.AbstractUdpServer;
import at.rtr.rmbt.qos.testserver.service.EventJob.EventType;
import at.rtr.rmbt.qos.testserver.service.ServiceManager;
import at.rtr.rmbt.qos.testserver.tcp.TcpMultiClientServer;
import at.rtr.rmbt.qos.testserver.tcp.TcpWatcherRunnable;
import at.rtr.rmbt.qos.testserver.udp.NioUdpMultiClientServer;
import at.rtr.rmbt.qos.testserver.udp.UdpMultiClientServer;
import at.rtr.rmbt.qos.testserver.udp.UdpTestCandidate;
import at.rtr.rmbt.qos.testserver.udp.UdpWatcherRunnable;
import at.rtr.rmbt.qos.testserver.util.LoggingService;
import at.rtr.rmbt.qos.testserver.util.RuntimeGuardService;
import at.rtr.rmbt.qos.testserver.util.TestServerConsole;
import at.rtr.rmbt.util.Randomizer;

/**
 * 
 * @author lb
 *
 */
public class TestServerImpl {

	public final ConcurrentHashMap<Integer, List<AbstractUdpServer<?>>> udpServerMap = new ConcurrentHashMap<Integer, List<AbstractUdpServer<?>>>();
	public final ConcurrentHashMap<Integer, List<TcpMultiClientServer>> tcpServerMap = new ConcurrentHashMap<Integer, List<TcpMultiClientServer>>();
	
	/**
	 * server socket list (=awaiting client test requests)
	 */
    public volatile List<ServerSocket> serverSocketList = new ArrayList<ServerSocket>();
	
	public SSLServerSocketFactory sslServerSocketFactory;
	public ServerPreferences serverPreferences;
	public final ServiceManager serviceManager = new ServiceManager(); 
	
	public final Set<ClientHandler> clientHandlerSet = new HashSet<>();
	
	/**
	 * 
	 */
	public final Randomizer randomizer = new Randomizer(8000, 12000, 3);
	
	/**
	 * is used for all control connection threads
	 */
    private ExecutorService mainServerPool;
    
    /**
     * is used for all other threads
     */
    private final ExecutorService COMMON_THREAD_POOL = Executors.newCachedThreadPool();
    
    /**
     * 
     */
    private final TestServerConsole console;
    
	/**
	 *     
	 */
    private QoSService qosService = null;
    
    /**
     * 
     */
    private AtomicBoolean isShutdownHookEnabled = new AtomicBoolean(true);
    
    /**
     * 
     */
    private AtomicBoolean isAlreadyShuttingDown = new AtomicBoolean(false);
    
	/**
	 * 
	 * @param port
	 * @param isSsl
	 * @param inetAddress
	 * @return
	 * @throws IOException
	 */
	public ServerSocket createServerSocket(int port, boolean isSsl, InetAddress inetAddress) throws IOException {
		ServerSocket socket = null;
		SocketAddress sa = new InetSocketAddress(inetAddress, port);
		if (!isSsl || !serverPreferences.useSsl()) {
			socket = new ServerSocket();
		}
		else {
			socket = sslServerSocketFactory.createServerSocket();
		}
		
		try {
			socket.bind(sa);
		}
		catch (Exception e) {
			TestServerConsole.errorReport("TCP " + port, "TCP Socket on port " + port, e, 0, TestServerServiceEnum.TCP_SERVICE);
			throw e;
		}
		
		socket.setReuseAddress(true);
		return socket;
	}
	
	final PrintStream tempErr;
	final PrintStream tempOut;
	
	public TestServerImpl() {
		console = createTestServerConsole();
		
		tempErr = System.err;
		tempOut = System.out;
		
		System.setErr(console);
		System.setOut(console);
	}
	
	public TestServerConsole createTestServerConsole() {
		return new TestServerConsole(this);
	}
	
	public void run() throws Exception {
		run(new String[0]);
	}

	public void run(final String[] args ) throws Exception {
		ServerPreferences sp = null;
		try {
			if (args.length>0) {
				sp = new ServerPreferences(args);
			}
			else {
				sp = new ServerPreferences();
			}
		} catch (TestServerException e) {
			ServerPreferences.writeErrorString();
			e.printStackTrace();
			System.exit(0);
		}
		
		run(sp);
	}
	
	
	/**
	 * 
	 * @return
	 */
	public QoSService getQoSService() {
		return this.qosService;
	}

	/**
	 * @throws TestServerException 
	 * 
	 */
	public void run(final ServerPreferences serverPreferences) throws Exception {
		this.serverPreferences = serverPreferences;
		LoggingService.init(this);
		
		if (!LoggingService.IS_LOGGING_AVAILABLE) {
			throw new TestServerException("ERROR: All logging disabled! Cannot start server. Please enable at least one logging target [syslog, console, file]", null);
		}
		
	    TestServerConsole.log("\nStarting QoSTestServer (" + TestServer.TEST_SERVER_VERSION_NAME + ") with settings: \n" + serverPreferences.toString() + "\n\n", 
	    		-1, TestServerServiceEnum.TEST_SERVER);
	    
		console.start();
	    
	    if (!TestServer.USE_FIXED_THREAD_POOL) {
	    	mainServerPool = Executors.newCachedThreadPool();
	    } 
	    else {
	    	mainServerPool = Executors.newFixedThreadPool(serverPreferences.getMaxThreads());
	    }
	    
	    try {
	    	
		    if (serverPreferences.useSsl()) {
			    /*******************************
			     * initialize SSLContext and SSLServerSocketFactory:
			     */

	            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
	            
	            // Load the JKS key chain
	            KeyStore ks = KeyStore.getInstance(TestServer.QOS_KEY_TYPE);
	            InputStream fis = TestServer.class.getResourceAsStream(TestServer.QOS_KEY_FILE);//new FileInputStream(serverKey);
	            ks.load(fis, TestServer.QOS_KEY_PASSWORD.toCharArray());
	            fis.close();
	            kmf.init(ks, TestServer.QOS_KEY_PASSWORD.toCharArray());
			    final SSLContext sslContext = SSLContext.getInstance("TLS");
	            // Initialize the SSL context
	            sslContext.init(kmf.getKeyManagers(), new TrustManager[] {TestServer.getTrustingManager()}, new SecureRandom());
	            
				sslServerSocketFactory = (SSLServerSocketFactory) sslContext.getServerSocketFactory();    	
		    }
		    
			for (InetAddress addr : serverPreferences.getInetAddrBindToSet()) {
				ServerSocket serverSocket;
				if (serverPreferences.useSsl()) {
			    	serverSocket = (SSLServerSocket) sslServerSocketFactory.createServerSocket();
				}
				else {
					serverSocket = new ServerSocket();
				}
			
				serverSocket.setReuseAddress(true);
				serverSocket.bind(new InetSocketAddress(addr, serverPreferences.getServerPort()));
				serverSocketList.add(serverSocket);
				
				this.qosService = new QoSService(mainServerPool, serverSocket, null);
				Thread mainThread = new Thread(this.qosService);
			    mainThread.start();
			}

		    Iterator<UdpPort> portIterator = serverPreferences.getUdpPortSet().iterator();

		    while (portIterator.hasNext()) {
		    	final List<AbstractUdpServer<?>> udpServerList = new ArrayList<AbstractUdpServer<?>>();
	    		final UdpPort udpPort = portIterator.next();
		    	for (InetAddress addr : serverPreferences.getInetAddrBindToSet()) {
		    		AbstractUdpServer<?> udpServer = null;
		    		try {
		    			//udpServer = new UdpMultiClientServer(port, addr);
		    			udpServer = udpPort.isNio ? new NioUdpMultiClientServer(udpPort.port, addr) : new UdpMultiClientServer(udpPort.port, addr);
		    		}
		    		catch (Exception e) { 
		    			TestServerConsole.error("TestServer INIT; Opening UDP Server on port: " + udpPort, e, 0, TestServerServiceEnum.TEST_SERVER); 
		    		}
		    		
		    		if (udpServer != null) {
		    			udpServerList.add(udpServer);
		    			getCommonThreadPool().execute(udpServer);
		    		}
		    	}
			    
		    	if (udpServerList.size() > 0) {
		    		udpServerMap.put(udpPort.port, udpServerList);
		    	}
		    }
		    
		    //start UDP watcher service:
		    final UdpWatcherRunnable udpWatcherRunnable = new UdpWatcherRunnable();
		    serviceManager.addService(udpWatcherRunnable);
		    //start TCP watcher service:
		    final TcpWatcherRunnable tcpWatcherRunnable = new TcpWatcherRunnable();
		    serviceManager.addService(tcpWatcherRunnable);
		    //register runtime guard service:
		    final RuntimeGuardService runtimeGuardService = new RuntimeGuardService();
		    serviceManager.addService(runtimeGuardService);
		    
		    //dispatch onStart event:
		    serviceManager.dispatchEvent(EventType.ON_TEST_SERVER_START);

		    //finally, start all plugins & services:
		    for (ServiceSetting service : serverPreferences.getPluginMap().values()) {
		    	TestServerConsole.log("Starting plugin '" + service.getName() + "'...", 0, TestServerServiceEnum.TEST_SERVER);
		    	service.start();
		    }
		    
		    //add shutdown hook (ctrl+c):
		    Runtime.getRuntime().addShutdownHook(
		    		new Thread() {
		    			public void run() {
		    				if (isShutdownHookEnabled.get()) {
			    				TestServerConsole.log("CTRL-C. Close! ", -1, TestServerServiceEnum.TEST_SERVER);
			    				shutdown();
		    				}
		    			}
		    		});
		} 
	    catch (IOException e) {
			e.printStackTrace();
		}
	    catch (Exception e) {
	    	e.printStackTrace();
	    }
	}
	
	/**
	 * 
	 */
	public void shutdown() 
	{
		if (qosService != null) {
			qosService.stop();
		}

		if (isAlreadyShuttingDown.getAndSet(true)) {
			return;
		}
		
		TestServerConsole.log("Shutting down QoS TestServer...", 0, TestServerServiceEnum.TEST_SERVER);
		
	    //stop all plugins & services:
	    for (ServiceSetting service : serverPreferences.getPluginMap().values()) {
	    	service.stop();
	    	TestServerConsole.log("Plugin '" + service.getName() + "' stopped.", 0, TestServerServiceEnum.TEST_SERVER);
	    }
		
		//dispatch onStop event:
		serviceManager.dispatchEvent(EventType.ON_TEST_SERVER_STOP);

		Map<String, Object> serviceResultMap = serviceManager.shutdownAll(true);
		if (serviceResultMap != null) {
			for (Entry<String, Object> entry : serviceResultMap.entrySet()) {
				if (entry.getValue() != null) {
					TestServerConsole.log("Service '" + entry.getKey() + "' result: " + entry.getValue(), 0, TestServerServiceEnum.TEST_SERVER);
				}
			}
		}
		
		mainServerPool.shutdownNow(); 
		try {
			mainServerPool.awaitTermination(4L, TimeUnit.SECONDS);
		} 
		catch (InterruptedException e) { 
			//e.printStackTrace();
		}
	    finally {
	    	System.setErr(tempErr);
	    	System.setOut(tempOut);
	    	
	    	for (ServerSocket serverSocket : serverSocketList) {
				if (serverSocket != null && !serverSocket.isClosed()) {
					try {
						serverSocket.close();
						System.out.println("\nServerSocket: " + serverSocket + " closed.");
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
	    	}
	    }
	}

	/**
	 * 
	 * @param port
	 * @return
	 * @throws Exception
	 */
	public DatagramSocket createDatagramSocket(int port, InetAddress addr) throws Exception {
		if (addr == null) {
			return new DatagramSocket(port);
		}
		else {
			return new DatagramSocket(port, addr);
		}
	}
	
	/**
	 * 
	 * @param port
	 * @param addr
	 * @return
	 * @throws Exception
	 */
	public DatagramChannel createDatagramChannel(int port, InetAddress addr) throws Exception {
		final DatagramChannel channel = DatagramChannel.open();
		if (addr == null) {
			channel.bind(new InetSocketAddress(port));
		}
		else {
			channel.bind(new InetSocketAddress(addr, port));
		}
		
		return channel;
	}
	
    /**
     * 
     * @return
     */
    public ConcurrentHashMap<Integer, List<TcpMultiClientServer>> getTcpServerMap() {
    	return tcpServerMap;
    }
    
    /**
     * 
     * @param port
     */
    public synchronized void unregisterTcpServer(Integer port) {
    	if (tcpServerMap.remove(port) != null) {
	    	TestServerConsole.log("Removed TCP server object on port: " + port, 1, TestServerServiceEnum.TCP_SERVICE);	
    	}
    }
    
    /**
     * 
     * @param port
     * @param server
     * @throws Exception 
     */
    public List<TcpMultiClientServer> registerTcpCandidate(Integer port, Socket socket) throws Exception {
    	List<TcpMultiClientServer> tcpServerList;

    	synchronized(tcpServerMap) {
	    	//if port not mapped create complete tcp server list
			if ((tcpServerList = tcpServerMap.get(port)) == null) {
				tcpServerList = new ArrayList<>();
				for (InetAddress addr : serverPreferences.getInetAddrBindToSet()) {
					tcpServerList.add(new TcpMultiClientServer(port, addr));
				}
				tcpServerMap.put(port, tcpServerList);
			}
			else {
				//there are some tcp servers active on this port, compare ip list with tcp server list and create missing servers
				tcpServerList = tcpServerMap.get(port);

				Set<InetAddress> inetAddrSet = new HashSet<>();
				for (TcpMultiClientServer tcpServer : tcpServerList) {
					inetAddrSet.add(tcpServer.getInetAddr());
				}
				Set<InetAddress> inetAddrBindToSet = new HashSet<>(serverPreferences.getInetAddrBindToSet());
				inetAddrBindToSet.removeAll(inetAddrSet);
				for (InetAddress addr : inetAddrBindToSet) {
					tcpServerList.add(new TcpMultiClientServer(port, addr));
				}
			}

			//if ip check is enabled register the candidate's ip on the server's whitelist
			if (serverPreferences.isIpCheck()) {
				for (TcpMultiClientServer tcpServer : tcpServerList) {
					tcpServer.registerCandidate(socket.getInetAddress(), TestCandidate.DEFAULT_TTL);	
				}
			}
			
			//prepare all servers on this port for incoming connections (open sockets/refresh ttl)
			for (TcpMultiClientServer tcpServer : tcpServerList) {
				tcpServer.prepare();
			}
    	}
    	
		return tcpServerList;
    }
    
    /**
     * 
     * @param <T>
     * @param localAddr
     * @param port
     * @param uuid
     * @param udpData
     * @return
     */
    @SuppressWarnings("unchecked")
	public synchronized <T extends Closeable> AbstractUdpServer<T> registerUdpCandidate(final InetAddress localAddr, final int port, final String uuid, final UdpTestCandidate udpData) {
		try {
			TestServerConsole.log("Trying to register UDP Candidate on " + localAddr + ":" + port , 0, TestServerServiceEnum.UDP_SERVICE);
			for (AbstractUdpServer<?> udpServer : udpServerMap.get(port)) {
				TestServerConsole.log("Comparing: " + localAddr + " <-> " + udpServer.getAddress(), 0, TestServerServiceEnum.UDP_SERVICE);
				if (udpServer.getAddress().equals(localAddr) || (udpServer.getAddress() != null && udpServer.getAddress().isAnyLocalAddress())) {
					TestServerConsole.log("Registering UDP Candidate on " + localAddr + ":" + port , 0, TestServerServiceEnum.UDP_SERVICE);
					TestServerConsole.log("Registering UDP Candidate for UdpServer: " + udpServer.toString(), 2, TestServerServiceEnum.UDP_SERVICE);
					((AbstractUdpServer<T>) udpServer).getIncomingMap().put(uuid, udpData);
					return (AbstractUdpServer<T>) udpServer;
				}
			}				
		}
		catch (Exception e) {
			TestServerConsole.error("Register UDP candidate on port: " + port, e, 1, TestServerServiceEnum.UDP_SERVICE);
		}
		
		return null;
	}	

    /**
     * 
     * @return
     */
	public ExecutorService getCommonThreadPool() {
		return COMMON_THREAD_POOL;
	}
	
	public synchronized void setShutdownHookEnabled(boolean enabled) {
		this.isShutdownHookEnabled.set(enabled);
	}
	
	public synchronized boolean isShutdownHookEnabled() {
		return isShutdownHookEnabled.get();
	}

	/**
	 * 
	 * @return original error PrintStream
	 */
	public PrintStream getTempErr() {
		return tempErr;
	}

	/**
	 * 
	 * @return original output PrintStream
	 */
	public PrintStream getTempOut() {
		return tempOut;
	}
}
