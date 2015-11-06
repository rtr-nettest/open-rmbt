/*******************************************************************************
 * Copyright 2013-2015 alladin-IT GmbH
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
package at.alladin.rmbt.android.util.net;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import android.util.Log;
import at.alladin.rmbt.android.util.InformationCollector;

/**
 * 
 * @author lb
 *
 */
public class NetworkUtil {


    /**
	 * 
	 */
    private static final String DEBUG_TAG = "NetworkUtil";
    
	
	/**
	 * 
	 */
	public static final String WALLED_GARDEN_URL = "http://webtest.nettest.at/generate_204";
    	
	/**
	 * 
	 */
	public static final int WALLED_GARDEN_SOCKET_TIMEOUT_MS = 10000;
	
	/**
	 * 
	 * @author lb
	 *
	 * @param <T>
	 */
	public static class MinMax<T> {
		public T min;
		public T max;
		
		public MinMax(T min, T max) {
			this.min = min;
			this.max = max;
		}

		@Override
		public String toString() {
			return "MinMax [min=" + min + ", max=" + max + "]";
		}
	}

	/**
	 * 
	 * @param signalType
	 * 	can (should) contain one of the following values:
	 * 	<ul>
	 * 		<li>{@link InformationCollector#SINGAL_TYPE_MOBILE}</li>
	 * 		<li>{@link InformationCollector#SINGAL_TYPE_RSRP}</li>
	 * 		<li>{@link InformationCollector#SINGAL_TYPE_WLAN}</li>
	 *	</ul>
	 * @return
	 */
	public static MinMax<Integer> getSignalStrengthBounds(int signalType) {
        int min = Integer.MIN_VALUE;
        int max = Integer.MAX_VALUE;
        switch (signalType)
        {
        case InformationCollector.SINGAL_TYPE_MOBILE:
            min = -110;
            max = -50;
            break;
            
        case InformationCollector.SINGAL_TYPE_WLAN:
            min = -100;
            max = -40;
            break;
            
        case InformationCollector.SINGAL_TYPE_RSRP:
            min = -130;
            max = -70;
            break;            
        }
        return new MinMax<Integer>(min, max);
	}

	/**
	 * 
	 * @return
	 */
	public static boolean isWalledGardenConnection() {
	    HttpURLConnection urlConnection = null;
	    try {
	    	Log.i(DEBUG_TAG, "checking for walled garden...");
	        URL url = new URL(WALLED_GARDEN_URL);
	        urlConnection = (HttpURLConnection) url.openConnection();
	        urlConnection.setInstanceFollowRedirects(false);
	        urlConnection.setConnectTimeout(WALLED_GARDEN_SOCKET_TIMEOUT_MS);
	        urlConnection.setReadTimeout(WALLED_GARDEN_SOCKET_TIMEOUT_MS);
	        urlConnection.setUseCaches(false);
	        urlConnection.getInputStream();
	        Log.d(DEBUG_TAG, "check completed, response: " + urlConnection.getResponseCode());
	        // We got a valid response, but not from the real google
	        return urlConnection.getResponseCode() != 204;
	    } catch (IOException e) {
	    	e.printStackTrace();
	        return false;
	    } finally {
	        if (urlConnection != null) {
	            urlConnection.disconnect();
	        }
	    }
	}
	
	public static NetworkInterface46 getLocalIpAddresses(InetAddress privateIp) {
	    try {
	    	NetworkInterface46 iface = null;
	        for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
	            NetworkInterface intf = en.nextElement();
	            //if (NetworkInfoCollector.DJ_DEBUG) {
	            	System.out.println(intf.toString() + ", isUp: " + intf.isUp() 
	            		+ ", isLoopback: " + intf.isLoopback() 
	            		+ ", isP2P: " + intf.isPointToPoint() 
	            		+ ", isVirtual: " + intf.isVirtual() + "\n");
	            //}
               	for (Enumeration<InetAddress> addrList = intf.getInetAddresses(); addrList.hasMoreElements();) {
               		InetAddress addr = addrList.nextElement();
               		if (addr.equals(privateIp)) {
                   		return new NetworkInterface46(intf, false);		
               		}
               	}                	
	        }


	        return iface;
	    } catch (Exception ex) {
	        ex.printStackTrace();
	    }
	    return null;	
	}
	
	/**
	 * 
	 * @return
	 * @throws SocketException
	 * @throws UnknownHostException 
	 */
	public static Set<InetAddress> getAllInterfaceIpAddresses() throws SocketException, UnknownHostException {
		Set<InetAddress> ipSet = new HashSet<InetAddress>();
		
        for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
            NetworkInterface intf = en.nextElement();

            if (intf.getInetAddresses().hasMoreElements()) {
            	Enumeration<InetAddress> ipAddressEnumeration = intf.getInetAddresses();
            	while (ipAddressEnumeration.hasMoreElements()) {
            		ipSet.add(ipAddressEnumeration.nextElement());
            	}
            }
        }
        
        return ipSet;
	}
	
	/**
	 * Returns the requested interface including IPv4 and (if available) IPv6
	 * @return
	 */
	public static NetworkInterface46 getLocalIpAddresses(boolean isLoopback) {
	    try {
	    	NetworkInterface46 iface = null;
	        for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
	            NetworkInterface intf = en.nextElement();
	            //ifaceList.add(new NetworkInterface46(intf, isLoopback));
	            //System.out.println(intf.toString() + " isUp: " + intf.isUp());
	            //xnor:
                if ((intf.isLoopback() == isLoopback) && intf.isUp() && intf.getInetAddresses().hasMoreElements()) {
                	if (isLoopback) {
                		return new NetworkInterface46(intf, isLoopback);
                	}
                	
               		if (iface != null) {
               			iface.registerNetworkIface(intf, isLoopback);
               		}
               		else {
               			iface = new NetworkInterface46(intf, isLoopback);
               		}
	            }
	        }


	        return iface;
	    } catch (Exception ex) {
	        ex.printStackTrace();
	    }
	    return null;
	}
	

	/**
	 * check if there is an ipv6 in the loopback interface (=could be used to determine ipv6 support)
	 * @return
	 */
	public static boolean isLoopbackIntefaceIpv6Available() {
		return isLoopbackInterfaceIpv6Available(null);
	}
	
	/**
	 * check if there is an ipv6 in the loopback interface (=could be used to determine ipv6 support)
	 * @return
	 */
	public static boolean isLoopbackInterfaceIpv6Available(NetworkInterface46 iface) {
		if (iface == null) {
			iface = getLocalIpAddresses(true);
		}

		if (iface != null) {
			return iface.getIpv6() != null;
		}
		
		return false;
	}
	
	/**
	 * 
	 * @author lb
	 *
	 */
	public static class NetworkInterface46 {
		private Inet4Address ipv4;
		private Inet6Address ipv6;
		private InetAddress ip;
		private NetworkInterface networkInterface;
		
		public NetworkInterface46(NetworkInterface networkInterface, boolean isLoopback) {
			registerNetworkIface(networkInterface, isLoopback);
		}
		
		public void registerNetworkIface(NetworkInterface networkInterface, boolean isLoopback) {
			this.networkInterface = networkInterface;
			
            for (Enumeration<InetAddress> enumIpAddr = networkInterface.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                InetAddress inetAddress = enumIpAddr.nextElement();

            	if (inetAddress instanceof Inet6Address) {
            		if (ipv6 == null || (!inetAddress.isLinkLocalAddress() && (inetAddress.isLoopbackAddress() == isLoopback))) {
            			ipv6 = (Inet6Address) inetAddress;
            		}
            	}
            	else if (inetAddress instanceof Inet4Address) {
            		if (ipv4 == null || (!inetAddress.isLinkLocalAddress() && (inetAddress.isLoopbackAddress() == isLoopback))) {
            			ipv4 = (Inet4Address) inetAddress;
            		}
            	}
            	else {
            		ip = inetAddress;
            	}
            }
		}

		public Inet4Address getIpv4() {
			return ipv4;
		}

		public void setIpv4(Inet4Address ipv4) {
			this.ipv4 = ipv4;
		}

		public Inet6Address getIpv6() {
			return ipv6;
		}

		public void setIpv6(Inet6Address ipv6) {
			this.ipv6 = ipv6;
		}

		public NetworkInterface getNetworkInterface() {
			return networkInterface;
		}

		public void setNetworkInterface(NetworkInterface networkInterface) {
			this.networkInterface = networkInterface;
		}

		@Override
		public String toString() {
			return "NetworkInterface46 [ipv4=" + ipv4 + ", ipv6=" + ipv6 + ", ip=" + ip
					+ ", networkInterface=" + (networkInterface != null ? networkInterface : "")  
					+ ", name=" + (networkInterface != null ?  networkInterface.getName() :  "") 
					+ ", displayName=" + (networkInterface != null ?  networkInterface.getDisplayName() : "") + "]";
		}
	}
}
