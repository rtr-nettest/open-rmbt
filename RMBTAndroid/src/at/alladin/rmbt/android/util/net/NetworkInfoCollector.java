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
package at.alladin.rmbt.android.util.net;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import at.alladin.openrmbt.android.R;
import at.alladin.rmbt.android.adapter.result.OnCompleteListener;
import at.alladin.rmbt.android.main.RMBTMainActivity;
import at.alladin.rmbt.android.util.CheckIpTask;
import at.alladin.rmbt.android.util.ConfigHelper;
import at.alladin.rmbt.android.util.Helperfunctions;
import at.alladin.rmbt.android.util.net.NetworkInfoCollector.OnNetworkInfoChangedListener.InfoFlagEnum;
import at.alladin.rmbt.android.util.net.NetworkUtil.NetworkInterface46;

/**
 * 
 * @author lb
 *
 */
public class NetworkInfoCollector {
	

    /**
	 * 
	 */
    private static final String DEBUG_TAG = "NetworkInfoCollector";
    
	
	public final static int IP_METHOD_ACTIVE_NETWORK = 1;
	public final static int IP_METHOD_NETWORKINTERFACE = 2;

	/**
	 * 
	 */
	public static int IP_METHOD = IP_METHOD_ACTIVE_NETWORK;
	
	public final static boolean DJ_DEBUG = false;
	
	/**
	 * ip fetch series count
	 */
	public final static int IP_FETCH_RETRIES = 3;
	
	/**
	 * delay between a full ip fetch series
	 */
	public final static int IP_FETCH_RETRY_SERIES_DELAY = 10000;
	
	/**
	 * delay between single tries
	 */
	public final static int IP_FETCH_RETRY_DELAY = 2500;
	
	/**
	 * poll delay
	 */
	public final static int IP_FETCH_POLL_DELAY = 5000;
	
	/**
	 * 
	 */
	public final static int IP_FETCH_POLL_SERIES_DELAY = 30000;

	public class IpFetchController {
		public long lastRetryTimestamp = 0;
		public int retryCount = 0;
		
		public boolean isCheckAllowed(long currentTimestamp, boolean isPolling) {
			if (!isPolling) {
				if (retryCount < IP_FETCH_RETRIES && ((lastRetryTimestamp + IP_FETCH_RETRY_DELAY) <= currentTimestamp)) {
					return true;
				}
				
				if ((lastRetryTimestamp + IP_FETCH_RETRY_SERIES_DELAY) <= currentTimestamp) {
					retryCount = 0;
					return true;
				}
				
				return false;
			}
			else {
				if (retryCount < IP_FETCH_RETRIES && ((lastRetryTimestamp + IP_FETCH_POLL_DELAY) <= currentTimestamp)) {
					return true;
				}

				
				if ((lastRetryTimestamp + IP_FETCH_POLL_SERIES_DELAY) <= currentTimestamp) {
					return true;
				}
				
				return false;
			}
		}
		
		public void storeIpCheck(long timestamp) {
			this.retryCount++;
			this.lastRetryTimestamp = timestamp;
		}
		
		public void reset() {
			this.lastRetryTimestamp = 0;
			this.retryCount = 0;
		}
	}

	private IpFetchController ipFetchController = new IpFetchController();

	/**
	 * 
	 * @author lb
	 *
	 */
	public static interface OnNetworkInfoChangedListener {
		public static enum InfoFlagEnum {
			IP_CHECK_ERROR, IP_CHECK_SUCCESS, PUBLIC_IPV4_CHANGED, PUBLIC_IPV6_CHANGED, PRIVATE_IPV4_CHANGED, PRIVATE_IPV6_CHANGED,
			NETWORK_CONNECTION_CHANGED
		}
		
		public void onChange(InfoFlagEnum infoFlag, Object newValue);
	}
	
	public final static int FLAG_IPV4 = 1;
	public final static int FLAG_IPV6 = 2;
	public final static int FLAG_PRIVATE_IPV6 = 3;
	public final static int FLAG_PRIVATE_IPV4 = 4;
	public final static int FLAG_IP_TASK_COMPLETED = 5;
	public final static int FLAG_IP_TASK_NEEDS_RETRY = 6;

	public static enum IpStatus {
		STATUS_NOT_AVAILABLE(R.drawable.traffic_lights_grey),
		NO_ADDRESS(R.drawable.traffic_lights_red),
		ONLY_LOCAL(R.drawable.traffic_lights_yellow),
		CONNECTED_NAT(R.drawable.traffic_lights_yellow),
		CONNECTED_NO_NAT(R.drawable.traffic_lights_green);
		
		protected int resourceId;
		IpStatus(int resourceId) {
			this.resourceId = resourceId;
		}
		public int getResourceId() {
			return resourceId;
		}
	}
	
	public static enum CaptivePortalStatusEnum {
		NOT_TESTED(R.string.not_available),
		FOUND(R.string.captive_portal_found),
		NOT_FOUND(R.string.captive_portal_not_found),
		TESTING(R.string.captive_portal_testing);

		protected final int resourceId;
		
		private CaptivePortalStatusEnum(final int resourceId) {
			this.resourceId = resourceId;
		}
		
		public String getTitle(Context context) {
			return context.getString(resourceId);
		}
	}
	private CaptivePortalStatusEnum captivePortalStatus = CaptivePortalStatusEnum.NOT_TESTED;
	private AtomicBoolean isCaptivePortalTestRunning = new AtomicBoolean();
	private AtomicBoolean isCaptivePortalTestNeeded = new AtomicBoolean(false);
	
	private RMBTMainActivity activity;
	private boolean hasIpv6Support = false;
	private boolean hasIpFromControlServer = false;
	private NetworkInterface46 networkInterface;
	private NetworkInterface46 loopbackInterface;
	private ConnectivityManager connectivityManager;
	private String publicIpv4Plain;
	private InetAddress publicIpv4;
	private String publicIpv6Plain;
	private InetAddress publicIpv6;
	private InetAddress privateIpv4;
	private InetAddress privateIpv6;
	private String ssid;
	private Set<InetAddress> ipSet;
	
	private boolean hasConnectionFromAndroidApi = false;
	private final AtomicBoolean isIpChecking = new AtomicBoolean(false);
	private final AtomicBoolean isIpFetchAllowed = new AtomicBoolean(false);
	private boolean lastIpCheckWasError = false;
	
	private NetworkInfo activeNetworkInfo;
	private List<OnNetworkInfoChangedListener> listenerList = new ArrayList<NetworkInfoCollector.OnNetworkInfoChangedListener>();
	
	/**
	 * 
	 */
	private static NetworkInfoCollector instance;
	
	/**
	 * 
	 * @param activity
	 * @return
	 */
	public static void init(RMBTMainActivity activity) {
		if (instance == null) {
			instance = new NetworkInfoCollector(activity);
		}
		
		IP_METHOD = (ConfigHelper.useNetworkInterfaceIpMethod(activity) ? IP_METHOD_NETWORKINTERFACE : IP_METHOD_ACTIVE_NETWORK);
		//System.out.println("Setting IP_METHOD to: " + IP_METHOD);
	}
	
	/**
	 * 
	 * @return
	 */
	public static synchronized NetworkInfoCollector getInstance() {
		return instance;
	}
	
	/**
	 * 
	 * @param activity
	 */
	private NetworkInfoCollector(RMBTMainActivity activity) {
		this.activity = activity;
		gatherInterfaceInformation(false);
	}
	
	/**
	 * 
	 */
	public synchronized void gatherInterfaceInformation(boolean allowIpFetch) {	
		this.loopbackInterface = NetworkUtil.getLocalIpAddresses(true);
		if (IP_METHOD == IP_METHOD_NETWORKINTERFACE) {
			networkInterface = NetworkUtil.getLocalIpAddresses(false);
			setPrivateIpv4(networkInterface != null ? networkInterface.getIpv4() : null);
			setPrivateIpv6(networkInterface != null ? networkInterface.getIpv6() : null);
		}
		
		this.hasIpv6Support = NetworkUtil.isLoopbackInterfaceIpv6Available(loopbackInterface);
		this.connectivityManager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        
        if (DJ_DEBUG) {
        	System.err.println("\n-FOUND INTERFACES/IPs-\n");
        	System.err.println(networkInterface != null ? networkInterface : "NetworkInterface n/a");
        	System.err.println(loopbackInterface != null ? loopbackInterface : "LoopbackInterface n/a");
        	System.err.println("\n-END INTERFACES-\n");
        }
        
        isIpFetchAllowed.set(allowIpFetch);
        gatherIpInformation(false);
	}
	
	/**
	 * 
	 */
	public synchronized void gatherIpInformation(boolean overrideIpFetchAllow) {
		if (isIpFetchAllowed.get() || overrideIpFetchAllow) {
			if (isIpFetchAllowed.get() == false) {
				isIpFetchAllowed.set(true);
			}

			final boolean isPoll = ConfigHelper.isIpPolling(activity);
//			System.out.println(hasIpFromControlServer + " - " + isIpChecking.get() + " - " 
//					+ ipFetchController.retryCount + " - check allowd: " 
//					+ ipFetchController.isCheckAllowed(System.currentTimeMillis(), isPoll));
			if ((!hasIpFromControlServer || overrideIpFetchAllow) && !isIpChecking.get() && ipFetchController.isCheckAllowed(System.currentTimeMillis(), isPoll)) {
				isIpChecking.set(true);
				checkIp();
				ipFetchController.storeIpCheck(System.currentTimeMillis());
				isIpChecking.set(false);
			}
		}
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean hasIpv6Support() {
		return this.hasIpv6Support;
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isConnected() {
		if (this.connectivityManager != null && this.connectivityManager.getActiveNetworkInfo() != null) {
			return this.connectivityManager.getActiveNetworkInfo().isConnected();
		}
		
		return false;
	}
	
	/**
	 * 
	 * @return
	 */
	public NetworkInterface46 getNetworkInterface() {
		return networkInterface;
	}
	
	/**
	 * 
	 * @return
	 */
	public NetworkInterface46 getLoopbackInterface() {
		return loopbackInterface;
	}
	
	/**
	 * 
	 * @param ctx
	 * @param intent
	 * @throws SocketException 
	 */
	public void onNetworkChange(Context ctx, Intent intent) {
		ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();
//		Log.i(DEBUG_TAG, "" + (intent != null ? intent.getAction() : "check network change"));
		
		String currentSsid = null;
		
		if (ni != null) {

			if (ni.getType() == ConnectivityManager.TYPE_MOBILE) {
				TelephonyManager telManager = (TelephonyManager) activity.getSystemService(Context.TELEPHONY_SERVICE);
				currentSsid = telManager.getNetworkOperatorName();
			}
			else {
				WifiManager wifiManager = (WifiManager) activity.getSystemService(Context.WIFI_SERVICE);
				WifiInfo wifiInfo = wifiManager.getConnectionInfo();
				currentSsid = String.valueOf(Helperfunctions.removeQuotationsInCurrentSSIDForJellyBean(wifiInfo.getSSID()));
			}
		}
		
		Set<InetAddress> currentIpSet = null;
				
		try {
			currentIpSet = NetworkUtil.getAllInterfaceIpAddresses();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		if ((activeNetworkInfo == null && ni != null) || (activeNetworkInfo != null && ni == null) 
				|| (activeNetworkInfo != null && !activeNetworkInfo.equals(ni)) || (currentSsid != null && !currentSsid.equals(ssid)) 
				|| (currentIpSet != null && !currentIpSet.equals(ipSet))) {
			
			if (ssid == null || !ssid.equals(currentSsid)) {
				if (ConfigHelper.isIpPolling(activity)) {
					ipFetchController.reset();
				}
				
				Log.d(DEBUG_TAG, "CHECK FOR CAPTIVE PORTAL step 1");
//				Log.d(DEBUG_TAG, "ssid = " + ssid + ", new ssid = " + currentSsid);
				resetAllPublicIps();
				resetAllPrivateIps();				
				
				ipSet = currentIpSet;
				ssid = currentSsid;
//				Log.d(DEBUG_TAG, "" + ni);
//				Log.d(DEBUG_TAG, "" + activeNetworkInfo);
				//active network has changed
				activeNetworkInfo = ni;
//				Log.d(DEBUG_TAG, "" + activeNetworkInfo);
				if (activeNetworkInfo != null && activeNetworkInfo.isConnected() && currentSsid != null) {
					Log.d(DEBUG_TAG,"CHECK FOR CAPTIVE PORTAL step 2");
					
					isIpFetchAllowed.set(true);

					if (!isCaptivePortalTestRunning.get()) {
						checkForCaptivePortal();
					}
					if (IP_METHOD == IP_METHOD_ACTIVE_NETWORK) {
						resetAllPublicIps();
						resetAllPrivateIps();
						gatherInterfaceInformation(true);
					}
				}
			}
		}
	}
	
	/**
	 * 
	 */
	public void resetAllPublicIps() {
		//System.out.println("RESETTING ALL PUBLIC IPs");
		setPublicIpv4(null);
		setPublicIpv6(null);
		hasIpFromControlServer = false;
		
		if (IP_METHOD == IP_METHOD_ACTIVE_NETWORK) {
			networkInterface = null;
		}
	}
	
	/**
	 * 
	 */
	public void resetAllPrivateIps() {
		setPrivateIpv4(null);
		setPrivateIpv6(null);
	}
	
	/**
	 * 
	 */
	private void checkForCaptivePortal() {
		captivePortalStatus = CaptivePortalStatusEnum.TESTING;
		
		Runnable captivePortalCheck = new Runnable() {
			
			@Override
			public void run() {
				isCaptivePortalTestRunning.set(true);
				final boolean status = NetworkUtil.isWalledGardenConnection();
				captivePortalStatus = status ? CaptivePortalStatusEnum.FOUND : CaptivePortalStatusEnum.NOT_FOUND;
				isCaptivePortalTestRunning.set(false);
			}
		};
		
		Thread t = new Thread(captivePortalCheck);
		t.start();
		//activity.runOnUiThread(captivePortalCheck);
	}
	
	/**
	 * 
	 */
    public void checkIp() {
    	final boolean isPolling = ConfigHelper.isIpPolling(activity);
    	if (!isPolling) {
    		resetAllPublicIps();
    	}

    	//System.out.println("GETTING PUBLIC IP FROM CONTROL SERVER");
    	CheckIpTask ipTask = new CheckIpTask(activity);
    	ipTask.setOnCompleteListener(new OnCompleteListener() {
			
			@Override
			public void onComplete(int flag, Object object) {
				try {
					switch (flag) {
					case OnCompleteListener.ERROR:
						Log.d(DEBUG_TAG,"try again");
						hasIpFromControlServer = false;
						lastIpCheckWasError = true;
						publicIpv6Plain = null;
						publicIpv4Plain = null;
						isIpChecking.set(false);
						dispatchEvent(InfoFlagEnum.IP_CHECK_ERROR, null);
						break;
					case FLAG_PRIVATE_IPV4:
						if (object != null && (IP_METHOD == IP_METHOD_ACTIVE_NETWORK)) {
							setPrivateIpv4((Inet4Address) object);
						}
						break;
					case FLAG_PRIVATE_IPV6:
						if ((object != null) && (IP_METHOD == IP_METHOD_ACTIVE_NETWORK) && (object instanceof Inet6Address)) {
							setPrivateIpv6((Inet6Address) object);
						}
						break;
					case FLAG_IP_TASK_COMPLETED:
						
						if (!isPolling) {
							ipFetchController.reset();
						}
						lastIpCheckWasError = false;
						hasIpFromControlServer = true;
						isIpChecking.set(false);
						isIpFetchAllowed.set(false);
						break;
					default:
						//System.out.println("GOT INFO FROM IPTASK: " + object);
						if (object != null) {
							if (flag == FLAG_IPV4) {
								setPublicIpv4((String) object);
							}
							else {
								setPublicIpv6((String) object);
							}
						}
						dispatchEvent(InfoFlagEnum.IP_CHECK_SUCCESS, null);
						break;
					}
				}
				catch (Exception e) {
					e.printStackTrace();
					lastIpCheckWasError = true;
				}
			}
		});
    	ipTask.execute();
    }
    
    /**
     * 
     */
    private void setPublicIpv4(String ip) {
		if ((publicIpv4Plain != null && !publicIpv4Plain.equals(ip)) || publicIpv4Plain == null) {
			publicIpv4Plain = ip;
			try {
				if (ip != null) {
					publicIpv4 = InetAddress.getByName(ip);
				}
				else {
					publicIpv4 = null;
				}
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
			dispatchEvent(InfoFlagEnum.PUBLIC_IPV4_CHANGED, ip);
		}
		//System.out.println("AFTER SET: " + publicIpv6);
    }
    
    /**
     * 
     */
    private void setPublicIpv6(String ip) {
		if ((publicIpv6Plain != null && !publicIpv6Plain.equals(ip)) || publicIpv6Plain == null) {
			publicIpv6Plain = ip;
			try {
				if (ip != null) {
					publicIpv6 = InetAddress.getByName(ip);
				}
				else {
					publicIpv6 = null;
				}
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
			dispatchEvent(InfoFlagEnum.PUBLIC_IPV6_CHANGED, ip);
		}
		//System.out.println("AFTER SET: " + publicIpv6);
    }
    
    private void setPrivateIpv4(Inet4Address ip) {
    	if (privateIpv4 != null && !privateIpv4.equals(ip) || privateIpv4 == null) {
    		privateIpv4 = ip;
    		dispatchEvent(InfoFlagEnum.PRIVATE_IPV4_CHANGED, ip);
    	}
    }
    
    private void setPrivateIpv6(Inet6Address ip) {
    	if (privateIpv6 != null && !privateIpv6.equals(ip) || privateIpv6 == null) {
    		privateIpv6 = ip;
    		dispatchEvent(InfoFlagEnum.PRIVATE_IPV6_CHANGED, ip);
    	}
    }
    
    /**
     * 
     * @return
     */
    public String getPublicIpv6() {
    	return publicIpv6 != null ? publicIpv6.getHostAddress() : null;
    }
    
    /**
     * 
     * @return
     */
    public String getPublicIpv4() {
    	return publicIpv4 != null ? publicIpv4.getHostAddress() : null;
    }
    
    /**
     * 
     * @return
     */
    public Inet6Address getPrivateIpv6() {
    	if (networkInterface != null) {
    		return networkInterface.getIpv6();
    	}
   		return (Inet6Address) privateIpv6;
    }
    
    /**
     * 
     * @return
     */
    public String getPrivateIpv6String() {
    	Inet6Address ipv6Addr = getPrivateIpv6();
    	if (ipv6Addr != null) {
    		String ipv6 = getPrivateIpv6().getHostAddress();
    		int interfaceNamePosition = ipv6.indexOf('%'); 
    		return interfaceNamePosition >= 0 ? ipv6.substring(0, interfaceNamePosition) : ipv6;
    	}
    	
    	return null;
    }
    
    /**
     * 
     * @return
     */
    public Inet4Address getPrivateIpv4() {
    	if (networkInterface != null) {
    		return networkInterface.getIpv4();
    	}
    	return (Inet4Address) privateIpv4;
    }
    
    /**
     * 
     * @return
     */
    public boolean hasIpFromControlServer() {
    	return hasIpFromControlServer;
    }
    
    /**
     * 
     * @return
     */
    public CaptivePortalStatusEnum getCaptivePortalStatus() {
    	return captivePortalStatus;
    }
    
    /**
     * 
     * @param captivePortalStatus
     */
    public void setCaptivePortalStatus(CaptivePortalStatusEnum captivePortalStatus) {
    	this.captivePortalStatus = captivePortalStatus;
    }
    
    /**
     * 
     * @return
     */
    public boolean hasPrivateIpv4() {
    	if (getPrivateIpv4() != null) {
    		if (getPublicIpv4() != null) {
    			return !getPrivateIpv4().equals(getPublicIpv4());
    		}
    		return true;
    	}
    	return false;
    }
    
    /**
     * 
     * @return
     */
    public boolean hasPrivateIpv6() {
    	if (getPrivateIpv6() != null) {
    		if (getPublicIpv6() != null) {
    			return !getPrivateIpv6().equals(publicIpv6);
    		}
    		return !getPrivateIpv6().isLinkLocalAddress();
    	}
    	return false;
    }
    
    /**
     * 
     * @return
     */
    public IpStatus getIpv4Status() {
    	if (hasIpFromControlServer) {
    		if (publicIpv4 != null && getPrivateIpv4() != null) {
    			return publicIpv4.equals(getPrivateIpv4()) ? IpStatus.CONNECTED_NO_NAT : IpStatus.CONNECTED_NAT;
    		}
    	
   			return hasPrivateIpv4() ? IpStatus.ONLY_LOCAL : IpStatus.NO_ADDRESS;
    	}
    	
    	return IpStatus.STATUS_NOT_AVAILABLE;
    }

    /**
     * 
     * @return
     */
    public IpStatus getIpv6Status() {
    	if (hasIpFromControlServer) {
	    	if (publicIpv6 != null && getPrivateIpv6() != null) {
	    		return publicIpv6.equals(getPrivateIpv6()) ? IpStatus.CONNECTED_NO_NAT : IpStatus.CONNECTED_NAT;
	    	}
	    	
	    	return hasPrivateIpv6() ? IpStatus.ONLY_LOCAL : IpStatus.NO_ADDRESS;
    	}
    	
    	return IpStatus.STATUS_NOT_AVAILABLE;
    }

    /**
     * 
     * @return
     */
	public boolean hasConnectionFromAndroidApi() {
		return hasConnectionFromAndroidApi;
	}
	
	/**
	 * 
	 */
	public void setHasConnectionFromAndroidApi(boolean hasConnection) {
		this.hasConnectionFromAndroidApi = hasConnection;
		dispatchEvent(InfoFlagEnum.NETWORK_CONNECTION_CHANGED, hasConnection);
	}

	/**
	 * 
	 * @param listener
	 */
	public void addOnNetworkChangedListener(OnNetworkInfoChangedListener listener) {
		if (!listenerList.contains(listener)) {
			listenerList.add(listener);
		}
	}
	
	/**
	 * 
	 * @param listener
	 */
	public void removeOnNetworkInfoChangedListener(OnNetworkInfoChangedListener listener) {
		listenerList.remove(listener);
	}
	
	/**
	 * 
	 * @return
	 */
	public List<OnNetworkInfoChangedListener> getOnNetworkInfoChangedListeners() {
		return listenerList;
	}
	
	/**
	 * 
	 * @param event
	 */
	private void dispatchEvent(OnNetworkInfoChangedListener.InfoFlagEnum event, Object newValue) {
		//Log.d(DEBUG_TAG, "Dispatching Event: " + event + ", Listeners: " + listenerList.size());
		
		if (IP_METHOD == IP_METHOD_NETWORKINTERFACE) {
			if (event == InfoFlagEnum.PRIVATE_IPV4_CHANGED || event == InfoFlagEnum.PRIVATE_IPV6_CHANGED) {
				Log.d(DEBUG_TAG, "Dispatching Event: " + event + ", Listeners: " + listenerList.size());
				resetAllPublicIps();
			}
		}
		
		for (OnNetworkInfoChangedListener l : listenerList) {
			l.onChange(event, newValue);
		}
	}
	
	/**
	 * 
	 * @return
	 */
	public NetworkInfo getActiveNetworkInfo() {
		return activeNetworkInfo;
	}
}
