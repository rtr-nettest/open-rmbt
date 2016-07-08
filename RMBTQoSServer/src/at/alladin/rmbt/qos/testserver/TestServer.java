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
package at.alladin.rmbt.qos.testserver;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.channels.DatagramChannel;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
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

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import at.alladin.rmbt.qos.testserver.ServerPreferences.ServiceSetting;
import at.alladin.rmbt.qos.testserver.ServerPreferences.TestServerServiceEnum;
import at.alladin.rmbt.qos.testserver.ServerPreferences.UdpPort;
import at.alladin.rmbt.qos.testserver.entity.TestCandidate;
import at.alladin.rmbt.qos.testserver.servers.AbstractUdpServer;
import at.alladin.rmbt.qos.testserver.service.EventJob.EventType;
import at.alladin.rmbt.qos.testserver.service.ServiceManager;
import at.alladin.rmbt.qos.testserver.tcp.TcpMultiClientServer;
import at.alladin.rmbt.qos.testserver.tcp.TcpWatcherRunnable;
import at.alladin.rmbt.qos.testserver.udp.NioUdpMultiClientServer;
import at.alladin.rmbt.qos.testserver.udp.UdpMultiClientServer;
import at.alladin.rmbt.qos.testserver.udp.UdpTestCandidate;
import at.alladin.rmbt.qos.testserver.udp.UdpWatcherRunnable;
import at.alladin.rmbt.qos.testserver.util.LoggingService;
import at.alladin.rmbt.qos.testserver.util.RuntimeGuardService;
import at.alladin.rmbt.qos.testserver.util.TestServerConsole;
import at.alladin.rmbt.util.Randomizer;

/**
 * 
 * @author lb
 *
 */
public class TestServer {
	/**
	 * major version number
	 */
	public final static String TEST_SERVER_VERSION_MAJOR = "3";
	
	/**
	 * minor version number
	 */
	public final static String TEST_SERVER_VERSION_MINOR = "1";
	
	/**
	 * patch version number
	 */
	public final static String TEST_SERVER_VERSION_PATCH = "1";
	
	/**
	 * server code name
	 * <ul>
	 * 	<li><b>0.01 - 0.61</b> - ANONYMOUS PROXY</li>
	 * 	<li><b>0.61 - 0.91</b> - BANDWIDTH THROTTLING
	 * 		<p>major changes: support for multiple tests with single connection (client and server side)</p>  
	 * 	</li>
	 * 	<li><b>1.00 - 1.04</b> - CENSORSHIP'N CONTROL
	 * 		<p>major changes: implement rest services</p>
	 * 	</li>
	 * 	<li><b>2.00 - 2.19</b> - DROPPED PACKET
	 * 		<p>major changes: voip test, rmbtutil</p></li>
	 * 	<li><b>2.20 - 2.30</b> - DROPPED PACKET
	 * 		<p>major changes: non blocking udp server for udp and voip tests</p>
	 * 	</li>
	 * 	<li><b>3.0.0 - x.x.x</b> - ???
	 * 		<p>major changes: syslog support, logging framework: log4j</p>
	 * 		<p>major fixes: udp nio empty array fix</p>
	 * 	</li>
	 * </ul>
	 */
	public final static String TEST_SERVER_CODENAME = "DROPPED PACKET";
	
	/**
	 * full version string
	 */
	public final static String TEST_SERVER_VERSION_NAME = "QoSTS \"" + TEST_SERVER_CODENAME + "\" " + TEST_SERVER_VERSION_MAJOR + "." + TEST_SERVER_VERSION_MINOR;
	
	/**
	 * key path
	 */
	public final static String QOS_KEY_FILE = "crt/qosserver.jks";
	
	/**
	 * key type
	 */
	public final static String QOS_KEY_TYPE = "JKS";
	
	/**
	 * key password
	 */
	public final static String QOS_KEY_PASSWORD = "123456qwertz"; 
	
	public final static String RMBT_ENCRYPTION_STRING = "TLS";
	
	public final static boolean USE_FIXED_THREAD_POOL = true;
	public final static int MAX_THREADS = 200;
	
	public final static ConcurrentHashMap<Integer, List<AbstractUdpServer<?>>> udpServerMap = new ConcurrentHashMap<Integer, List<AbstractUdpServer<?>>>();
	public final static ConcurrentHashMap<Integer, List<TcpMultiClientServer>> tcpServerMap = new ConcurrentHashMap<Integer, List<TcpMultiClientServer>>();
	
	/**
	 * server socket list (=awaiting client test requests)
	 */
    public volatile static List<ServerSocket> serverSocketList = new ArrayList<ServerSocket>();
	
	public static SSLServerSocketFactory sslServerSocketFactory;
	public static ServerPreferences serverPreferences;
	public final static ServiceManager serviceManager = new ServiceManager(); 
	
	public final static Set<ClientHandler> clientHandlerSet = new HashSet<>();
	
	/**
	 * 
	 */
	public final static Randomizer randomizer = new Randomizer(8000, 12000, 3);
	
	/**
	 * is used for all control connection threads
	 */
    private static ExecutorService mainServerPool;
    
    /**
     * is used for all other threads
     */
    private static final ExecutorService COMMON_THREAD_POOL = Executors.newCachedThreadPool();
	
	/**
	 * 
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		
		TestServerConsole console = new TestServerConsole();
		System.setErr(console);
		System.setOut(console);
		
		try {
			if (args.length>0) {
				serverPreferences = new ServerPreferences(args);
			}
			else {
				serverPreferences = new ServerPreferences();
			}
		} catch (TestServerException e) {
			ServerPreferences.writeErrorString();
			e.printStackTrace();
			System.exit(0);
		}
	
		if (!LoggingService.IS_LOGGING_AVAILABLE) {
			throw new TestServerException("ERROR: All logging disabled! Cannot start server. Please enable at least one logging target [syslog, console, file]", null);
		}
		
	    TestServerConsole.log("\nStarting QoSTestServer (" + TEST_SERVER_VERSION_NAME + ") with settings: \n" + serverPreferences.toString() + "\n\n", 
	    		-1, TestServerServiceEnum.TEST_SERVER);
	    
		console.start();
	    
	    if (!USE_FIXED_THREAD_POOL) {
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
	            KeyStore ks = KeyStore.getInstance(QOS_KEY_TYPE);
	            InputStream fis = TestServer.class.getResourceAsStream(QOS_KEY_FILE);//new FileInputStream(serverKey);
	            ks.load(fis, QOS_KEY_PASSWORD.toCharArray());
	            fis.close();
	            kmf.init(ks, QOS_KEY_PASSWORD.toCharArray());
			    final SSLContext sslContext = SSLContext.getInstance("TLS");
	            // Initialize the SSL context
	            sslContext.init(kmf.getKeyManagers(), new TrustManager[] {getTrustingManager()}, new SecureRandom());
	            
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
				
				Thread mainThread = new Thread(new QoSService(mainServerPool, serverSocket, null));
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
		    				TestServerConsole.log("CTRL-C. Close! ", -1, TestServerServiceEnum.TEST_SERVER);
		    				shutdown();
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
	
	public static void shutdown() 
	{
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
			e.printStackTrace();
		}
	    finally {
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
	 * @param isSsl
	 * @param inetAddress
	 * @return
	 * @throws IOException
	 */
	public static ServerSocket createServerSocket(int port, boolean isSsl, InetAddress inetAddress) throws IOException {
		ServerSocket socket = null;
		SocketAddress sa = new InetSocketAddress(inetAddress, port);
		if (!isSsl || !TestServer.serverPreferences.useSsl()) {
			socket = new ServerSocket();
		}
		else {
			socket = TestServer.sslServerSocketFactory.createServerSocket();
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

	/**
	 * 
	 * @param port
	 * @return
	 * @throws Exception
	 */
	public static DatagramSocket createDatagramSocket(int port, InetAddress addr) throws Exception {
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
	public static DatagramChannel createDatagramChannel(int port, InetAddress addr) throws Exception {
		final DatagramChannel channel = DatagramChannel.open();
		if (addr == null) {
			channel.bind(new InetSocketAddress(port));
		}
		else {
			channel.bind(new InetSocketAddress(addr, port));
		}
		
		return channel;
	}
	
	
	   public static TrustManager getTrustingManager()
	   {
		   return new javax.net.ssl.X509TrustManager()
		   {
			   public X509Certificate[] getAcceptedIssuers()
	           {
	               return new X509Certificate[] {};
	           }
	            
	           public void checkClientTrusted(final X509Certificate[] certs, final String authType)
	        		   throws CertificateException
	           {
	                System.out.println("[TRUSTING] checkClientTrusted: " +
	                Arrays.toString(certs) + " - " + authType);
	           }
	            
	           public void checkServerTrusted(final X509Certificate[] certs, final String authType)
	        		   throws CertificateException
	           {
	                System.out.println("[TRUSTING] checkServerTrusted: " +
	                Arrays.toString(certs) + " - " + authType);
	           }
	       };
	   }
	    
	    public static SSLContext getSSLContext(final String keyResource, final String certResource)
	            throws NoSuchAlgorithmException, KeyManagementException
	    {	        
	        X509Certificate _cert = null;
	        try
	        {
	            if (certResource != null)
	            {
	                final CertificateFactory cf = CertificateFactory.getInstance("X.509");
	                _cert = (X509Certificate) cf.generateCertificate(TestServer.class.getClassLoader().getResourceAsStream(
	                        certResource));
	            }
	        }
	        catch (final Exception e)
	        {
	            e.printStackTrace();
	        }
	        final X509Certificate cert = _cert;
	        
	        TrustManagerFactory tmf = null;
	        try {
	        	if (cert != null) {
	        		final KeyStore ks = KeyStore.getInstance("");
	        		ks.load(TestServer.class.getClassLoader().getResourceAsStream(keyResource), "199993ec".toCharArray());
	        		ks.setCertificateEntry("crt", cert);
	        
	        		tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
	        		tmf.init(ks);
	        	}
	        }
	        catch (Exception e)
	        {
	        	e.printStackTrace();
	        }
	        
	        
	        final TrustManager tm;
	        if (cert == null)
	        	tm = getTrustingManager();
	        else
	            tm = new javax.net.ssl.X509TrustManager()
	            {
	                public X509Certificate[] getAcceptedIssuers()
	                {
                        return new X509Certificate[] { cert };
	                }
	                
	                public void checkClientTrusted(final X509Certificate[] certs, final String authType)
	                        throws CertificateException
	                {
	                     System.out.println("checkClientTrusted: " +
	                     Arrays.toString(certs) + " - " + authType);
	                }
	                
	                public void checkServerTrusted(final X509Certificate[] certs, final String authType)
	                        throws CertificateException
	                {
	                     System.out.println("checkServerTrusted: " +
	                     Arrays.toString(certs) + " - " + authType);
	                    if (certs == null)
	                        throw new CertificateException();
	                    for (final X509Certificate c : certs)
	                        if (cert.equals(c))
	                            return;
	                    throw new CertificateException();
	                }
	            };
	        
	        final TrustManager[] trustManagers = new TrustManager[] { tm };
	        
	        javax.net.ssl.SSLContext sc;
	        sc = javax.net.ssl.SSLContext.getInstance(RMBT_ENCRYPTION_STRING);
	        
	        sc.init(null, trustManagers, new java.security.SecureRandom());
	        return sc;
	    }
	    
	    /**
	     * 
	     * @return
	     */
	    public static ConcurrentHashMap<Integer, List<TcpMultiClientServer>> getTcpServerMap() {
	    	return tcpServerMap;
	    }
	    
	    /**
	     * 
	     * @param port
	     */
	    public static synchronized void unregisterTcpServer(Integer port) {
	    	if (TestServer.tcpServerMap.remove(port) != null) {
		    	TestServerConsole.log("Removed TCP server object on port: " + port, 1, TestServerServiceEnum.TCP_SERVICE);	
	    	}
	    }
	    
	    /**
	     * 
	     * @param port
	     * @param server
	     * @throws Exception 
	     */
	    public static List<TcpMultiClientServer> registerTcpCandidate(Integer port, Socket socket) throws Exception {
	    	List<TcpMultiClientServer> tcpServerList;

	    	synchronized(TestServer.tcpServerMap) {
		    	//if port not mapped create complete tcp server list
				if ((tcpServerList = TestServer.tcpServerMap.get(port)) == null) {
					tcpServerList = new ArrayList<>();
					for (InetAddress addr : TestServer.serverPreferences.getInetAddrBindToSet()) {
						tcpServerList.add(new TcpMultiClientServer(port, addr));
					}
					TestServer.tcpServerMap.put(port, tcpServerList);
				}
				else {
					//there are some tcp servers active on this port, compare ip list with tcp server list and create missing servers
					tcpServerList = TestServer.tcpServerMap.get(port);
	
					Set<InetAddress> inetAddrSet = new HashSet<>();
					for (TcpMultiClientServer tcpServer : tcpServerList) {
						inetAddrSet.add(tcpServer.getInetAddr());
					}
					Set<InetAddress> inetAddrBindToSet = new HashSet<>(TestServer.serverPreferences.getInetAddrBindToSet());
					inetAddrBindToSet.removeAll(inetAddrSet);
					for (InetAddress addr : inetAddrBindToSet) {
						tcpServerList.add(new TcpMultiClientServer(port, addr));
					}
				}
	
				//if ip check is enabled register the candidate's ip on the server's whitelist
				if (TestServer.serverPreferences.isIpCheck()) {
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
		public static synchronized <T extends Closeable> AbstractUdpServer<T> registerUdpCandidate(final InetAddress localAddr, final int port, final String uuid, final UdpTestCandidate udpData) {
			try {
				TestServerConsole.log("Trying to register UDP Candidate on " + localAddr + ":" + port , 0, TestServerServiceEnum.UDP_SERVICE);
				for (AbstractUdpServer<?> udpServer : TestServer.udpServerMap.get(port)) {
					TestServerConsole.log("Comparing: " + localAddr + " <-> " + udpServer.getAddress(), 0, TestServerServiceEnum.UDP_SERVICE);
					if (udpServer.getAddress().equals(localAddr)) {
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
		public static ExecutorService getCommonThreadPool() {
			return COMMON_THREAD_POOL;
		}
}
