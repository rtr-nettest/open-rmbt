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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import android.content.Context;
import android.net.NetworkInfo;
import at.alladin.openrmbt.android.R;
import at.alladin.rmbt.android.main.RMBTMainActivity;
import at.alladin.rmbt.android.util.net.NetworkInfoCollector.OnNetworkInfoChangedListener.InfoFlagEnum;

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
	
	private RMBTMainActivity activity;
	
	private boolean hasConnectionFromAndroidApi = false;
	
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
		checkForCaptivePortal();
	}
	
	/**
	 * 
	 */
	public void checkForCaptivePortal() {
		if (!isCaptivePortalTestRunning.getAndSet(true)) {
			captivePortalStatus = CaptivePortalStatusEnum.TESTING;
			
			Runnable captivePortalCheck = new Runnable() {
				
				@Override
				public void run() {
					final boolean status = NetworkUtil.isWalledGardenConnection();
					captivePortalStatus = status ? CaptivePortalStatusEnum.FOUND : CaptivePortalStatusEnum.NOT_FOUND;
					isCaptivePortalTestRunning.set(false);
				}
			};
			
			Thread t = new Thread(captivePortalCheck);
			t.start();
		}
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
