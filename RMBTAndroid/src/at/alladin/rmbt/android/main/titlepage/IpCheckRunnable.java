/*******************************************************************************
 * Copyright 2015 alladin-IT GmbH
 * Copyright 2017 Rundfunk und Telekom Regulierungs-GmbH (RTR-GmbH)
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
package at.alladin.rmbt.android.main.titlepage;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import android.app.Activity;
import android.util.Log;
import at.alladin.rmbt.android.R;
import at.alladin.rmbt.android.adapter.result.OnCompleteListener;
import at.alladin.rmbt.android.util.CheckIpTask;
import at.alladin.rmbt.android.util.CheckIpTask.IpVersionType;

public class IpCheckRunnable implements Runnable {
	
	private final static String LOG = "IpCheckRunnable";

	public final static int FLAG_IP_PRIVATE = 1;
	public final static int FLAG_IP_PUBLIC = 2;
	public final static int FLAG_IP_FINISHED = 3;
	public final static int FLAG_IP_CHECK_ERROR = -1;

	/**
	 * poll delay in ms
	 */
	public final static long IP_FETCH_INITAL_DELAY = 250;
	
	/**
	 * max backoff for IP delay in ms
	 */
	public final static long IP_FETCH_BACKOFF_LIMIT = 60000;


	
	public static interface OnIpCheckFinishedListener {
		void onFinish(final InetAddress privAddress, final InetAddress pubAddress, final InetAddress oldPrivAddress, final InetAddress oldPubAddress);
	}
	
	public class IpFetchController {
		public long lastRetryTimestamp = 0;
		public int retryCount = 0;
		
		public boolean isCheckAllowed(long currentTimestamp) {
             // retry with exponential backoff starting with IP_FETCH_INITAL_DELAY ms
			if ((currentTimestamp - lastRetryTimestamp) > (long) IP_FETCH_INITAL_DELAY*Math.pow(2,retryCount)) {
                //Log.d(LOG, "IPCheck allowed with retryCount " + retryCount + " at " + (long) (currentTimestamp - lastRetryTimestamp)/1000L + "s");
				return true;
			}
			// limit backoff to IP_FETCH_BACKOFF_LIMIT ms
			if ((currentTimestamp - lastRetryTimestamp) >  IP_FETCH_BACKOFF_LIMIT) {
			                //Log.d(LOG, "IPCheck allowed by backoff-limit  " + (long) (currentTimestamp - lastRetryTimestamp)/1000L + "s");
				return true;
			}

			return false;
		}
		
		public void storeIpCheck(long timestamp) {
			this.retryCount++;
			this.lastRetryTimestamp = timestamp;
		}

		public int getRetryCount() {
			return retryCount;
		}

		public void reset() {
			this.lastRetryTimestamp = 0;
			this.retryCount = 0;
		}
	}
	
	public static enum IpStatus {
		STATUS_NOT_AVAILABLE(R.drawable.traffic_lights_grey),
		NO_ADDRESS(R.drawable.traffic_lights_red),
		ONLY_LOCAL(R.drawable.traffic_lights_yellow),
		CONNECTED_NAT(R.drawable.traffic_lights_yellow),
		CONNECTED_NO_NAT(R.drawable.traffic_lights_green),
		CONNECTED_NAT_IPv4_TO_IPv6(R.drawable.traffic_lights_yellow),
		CONNECTED_NAT_IPv6_TO_IPv4(R.drawable.traffic_lights_yellow);

		protected int resourceId;
		IpStatus(int resourceId) {
			this.resourceId = resourceId;
		}
		public int getResourceId() {
			return resourceId;
		}
	}
	
	private final AtomicBoolean needsIpCheck = new AtomicBoolean(true);
	private final AtomicBoolean isIpCheckRunning = new AtomicBoolean(false);
	private final AtomicBoolean hasIpFromControlServer = new AtomicBoolean(false);
	
	/**
	 * strict ipv4/v6 = only accept ipv6 if ipVersionType is set to ipv6 etc.
	 */
	private final boolean isStrict;
	
	private InetAddress oldPrivAddress;
	private InetAddress oldPubAddress;
	
	private InetAddress privAddress;
	private InetAddress pubAddress;

	private final AtomicInteger ipCheckCounter = new AtomicInteger(0);

	private final IpFetchController ipFetchController = new IpFetchController();
	
	private final Activity activity;
	private final IpVersionType ipVersionType;
		
	private final List<OnIpCheckFinishedListener> listenerList = new ArrayList<OnIpCheckFinishedListener>();
	
	public IpCheckRunnable(final Activity activity, final IpVersionType ipVersionType, final boolean isStrict) {
		this.activity = activity;
		this.ipVersionType = ipVersionType;
		this.isStrict = isStrict;
	}
	
	
	public void addListener(final OnIpCheckFinishedListener listener) {
		if (!listenerList.contains(listener)) {
			listenerList.add(listener);
		}
	}
	
	/**
	 * true if the list was modified, otherwise false
	 * @param listener
	 * @return
	 */
	public boolean removeListener(final OnIpCheckFinishedListener listener) {
		return listenerList.remove(listener);
	}
	
	@Override
	public void run() {
		if (!isIpCheckRunning.get() && needsIpCheck.get() && ipFetchController.isCheckAllowed(System.currentTimeMillis())) {
			ipFetchController.storeIpCheck(System.currentTimeMillis());
			isIpCheckRunning.set(true);
			needsIpCheck.set(false);
			ipCheckCounter.addAndGet(1);

			final CheckIpTask ipTask = new CheckIpTask(activity, ipVersionType);
			ipTask.setOnCompleteListener(new OnCompleteListener() {
				
				@Override
				public void onComplete(int flag, Object object) {
					try {
						if (flag == FLAG_IP_PRIVATE) {
							if (ipCheckCounter.get()>0) {
								oldPrivAddress = privAddress;
							}
							privAddress = checkIp((InetAddress) object);
							
						}
						else if (flag == FLAG_IP_PUBLIC) {
							if (ipCheckCounter.get()>0) {
								oldPubAddress = pubAddress;
							}
							pubAddress = checkIp(InetAddress.getByName((String)object));
						}
						else if (flag == FLAG_IP_FINISHED) {
							isIpCheckRunning.set(false);
							hasIpFromControlServer.set(true);
							for (OnIpCheckFinishedListener listener : listenerList) {
								listener.onFinish(privAddress, pubAddress, oldPrivAddress, oldPubAddress);
							}
						}
						else if (flag == FLAG_IP_CHECK_ERROR){
							needsIpCheck.set(true);
							isIpCheckRunning.set(false);
							for (OnIpCheckFinishedListener listener : listenerList) {
								listener.onFinish(null, null, null, null);
							}
						}
					}
					catch (Exception e) {
						e.printStackTrace();
						needsIpCheck.set(true);
						isIpCheckRunning.set(false);
						for (OnIpCheckFinishedListener listener : listenerList) {
							listener.onFinish(null, null, null, null);
						}						
					}
				}
			});
			
			ipTask.execute();
		}
	}
	
	private InetAddress checkIp(final InetAddress inetAddress) {
		if (isStrict) {
			switch (ipVersionType) {
			case V4:
				if (!(inetAddress instanceof Inet4Address)) {
					Log.d(LOG, "v4 strict error bad address: " + inetAddress);
					return null;
				}
				break;
			case V6:
				if (!(inetAddress instanceof Inet6Address)) {
					Log.d(LOG, "v6 strict error bad address: " + inetAddress);
					return null;
				}
				break;
			}
		}
		
		return inetAddress;
	}
	
	public boolean hasPrivateIp() {
		return privAddress != null;
	}
	
    public IpStatus getIpStatus(IpCheckRunnable...checkRunnables) {
    	if (hasIpFromControlServer.get()) {
			//public IP and private IP
    		if (pubAddress != null && privAddress != null) {
				//special treatment if ipv4 vs ipv6 (#928)
				if (privAddress instanceof Inet4Address && pubAddress instanceof Inet6Address) {
					return IpStatus.CONNECTED_NAT_IPv4_TO_IPv6;
				}
				else if (privAddress instanceof Inet6Address && pubAddress instanceof Inet4Address) {
					return IpStatus.CONNECTED_NAT_IPv6_TO_IPv4;
				}

				//if public IP == private IP, no NAT, otherwise, NAT
    			return pubAddress.equals(privAddress) ? IpStatus.CONNECTED_NO_NAT : IpStatus.CONNECTED_NAT;
    		}

    		//only private IP
			if (hasPrivateIp()) {
				return IpStatus.ONLY_LOCAL;
			}

			//only public IP (?)
			if (pubAddress != null) {
				return IpStatus.NO_ADDRESS;
			}

			//neither public nor private IP
			return IpStatus.NO_ADDRESS;
    	}

    	boolean hasOtherIpFromControlServer = false;
    	if (checkRunnables != null) {
	    	for (IpCheckRunnable r : checkRunnables) {
	    		if (r.getHasIpFromControlServer()) {
	    			hasOtherIpFromControlServer = true;
	    			break;
	    		}
	    	}
    	}
    	
    	return hasOtherIpFromControlServer && ipCheckCounter.get() > 1 ? IpStatus.NO_ADDRESS : IpStatus.STATUS_NOT_AVAILABLE;
    }
	
	public void clearIps() {
		setPrivAddress(null);
		setPubAddress(null);
		needsIpCheck.set(true);
		ipFetchController.reset();
		ipCheckCounter.set(0);
		hasIpFromControlServer.set(false);
	}

	public InetAddress getPrivAddress() {
		return privAddress;
	}

	public void setPrivAddress(InetAddress privAddress) {
		this.privAddress = privAddress;
	}

	public InetAddress getPubAddress() {
		return pubAddress;
	}

	public void setPubAddress(InetAddress pubAddress) {
		this.pubAddress = pubAddress;
	}

	public IpVersionType getIpVersionType() {
		return ipVersionType;
	}

	public boolean getHasIpFromControlServer() {
		return hasIpFromControlServer.get();
	}
}

