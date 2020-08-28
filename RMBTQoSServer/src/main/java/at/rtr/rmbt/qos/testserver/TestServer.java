/*******************************************************************************
 * Copyright 2013-2016 alladin-IT GmbH
 * Copyright 2013-2016 Rundfunk und Telekom Regulierungs-GmbH (RTR-GmbH)
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

import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

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
	public final static String QOS_KEY_FILE = "/crt/qosserver.jks";
	
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
	
	private static TestServerImpl instance; 
	
	public static TestServerImpl newInstance() {
		instance = new TestServerImpl();
		return instance;
	}
	
	public static TestServerImpl getInstance() {
		if (instance == null) {
			instance = new TestServerImpl();
		}
		return instance;
	}
	
	/**
	 * 
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		getInstance().run(args);
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

}
