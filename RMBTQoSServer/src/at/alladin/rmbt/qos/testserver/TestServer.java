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
package at.alladin.rmbt.qos.testserver;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import at.alladin.rmbt.qos.testserver.ServerPreferences.TestServerServiceEnum;
import at.alladin.rmbt.qos.testserver.service.EventJob.EventType;
import at.alladin.rmbt.qos.testserver.service.ServiceManager;
import at.alladin.rmbt.qos.testserver.tcp.TcpServer;
import at.alladin.rmbt.qos.testserver.tcp.TcpWatcherRunnable;
import at.alladin.rmbt.qos.testserver.udp.UdpMultiClientServer;
import at.alladin.rmbt.qos.testserver.udp.UdpWatcherRunnable;
import at.alladin.rmbt.qos.testserver.util.RuntimeGuardService;
import at.alladin.rmbt.qos.testserver.util.TestServerConsole;

/**
 * 
 * @author lb
 *
 */
public class TestServer {
	/**
	 * major version number
	 */
	public final static String TEST_SERVER_VERSION_MAJOR = "0";
	
	/**
	 * minor version number
	 */
	public final static String TEST_SERVER_VERSION_MINOR = "57";
	
	/**
	 * full version string
	 */
	public final static String TEST_SERVER_VERSION_NAME = "QoSTS " + TEST_SERVER_VERSION_MAJOR + "." + TEST_SERVER_VERSION_MINOR;
	
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
	
	public final static TreeMap<Integer, List<UdpMultiClientServer>> udpServerMap = new TreeMap<Integer, List<UdpMultiClientServer>>();
	public volatile static TreeMap<Integer, List<TcpServer>> tcpServerMap = new TreeMap<Integer, List<TcpServer>>();
	public volatile static TreeMap<Integer, ServerSocket> tcpServerSocketMap = new TreeMap<Integer, ServerSocket>();
	
	/**
	 * server socket list (=awaiting client test requests)
	 */
    public volatile static List<ServerSocket> serverSocketList = new ArrayList<ServerSocket>();
	
	public static SSLServerSocketFactory sslServerSocketFactory;
	public static ServerPreferences serverPreferences;
	public final static ServiceManager serviceManager = new ServiceManager(); 
	
    private static ExecutorService pool;
	
	/**
	 * 
	 * @param args
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException {
		
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
	
	    TestServerConsole.log("Starting QoSTestServer (" + TEST_SERVER_VERSION_NAME + ") with settings: " + serverPreferences.toString(), 
	    		0, TestServerServiceEnum.TEST_SERVER);
	    
		console.start();
	    
	    if (!USE_FIXED_THREAD_POOL) {
	    	pool = Executors.newCachedThreadPool();
	    } 
	    else {
	    	pool = Executors.newFixedThreadPool(serverPreferences.getMaxThreads());
	    }
	    
	    try {
	    	
	    	/*
		    switch (serverPreferences.getVerboseLevel()) {
		    case 1:
		    	System.setProperty("javax.net.debug", "ssl,handshake");
		    	break;
		    case 2:
		    	System.setProperty("javax.net.debug", "all");
		    	break;
		    }
		    */
		    
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
		    
		    TestServerConsole.log("\n\nStarting QoSTestServer with settings: " + serverPreferences.toString() +" \n", -1);
		    
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
				
				Thread mainThread = new Thread(new QoSService(pool, serverSocket, null));
			    mainThread.start();
			}

		    Iterator<Integer> portIterator = serverPreferences.getUdpPortSet().iterator();

		    while (portIterator.hasNext()) {
		    	final List<UdpMultiClientServer> udpServerList = new ArrayList<UdpMultiClientServer>();
	    		final int port = portIterator.next();
		    	for (InetAddress addr : serverPreferences.getInetAddrBindToSet()) {
		    		UdpMultiClientServer udpServer = new UdpMultiClientServer(port, addr);
		    		udpServerList.add(udpServer);
		    		Thread t2 = new Thread(udpServer);
		    		t2.start();
		    	}
			    
			    udpServerMap.put(port, udpServerList);		    	
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
		
		pool.shutdownNow(); 
		try {
			pool.awaitTermination(4L, TimeUnit.SECONDS);
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
		
		socket.bind(sa);
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
	    public static synchronized TreeMap<Integer, List<TcpServer>> getTcpServerMap() {
	    	return tcpServerMap;
	    }
}
