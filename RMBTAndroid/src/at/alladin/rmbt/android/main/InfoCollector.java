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
package at.alladin.rmbt.android.main;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.location.Location;
import android.util.Log;
import at.alladin.rmbt.android.util.net.InterfaceTrafficGatherer.TrafficClassificationEnum;

public class InfoCollector implements Serializable {
	public static enum InfoCollectorType {
		SIGNAL, SIGNAL_RSRQ, NETWORK_FAMILY, NETWORK_TYPE, NETWORK_NAME, LOCATION, IPV4, IPV6, UL_TRAFFIC, DL_TRAFFIC, CONTROL_SERVER_CONNECTION, 
		CAPTIVE_PORTAL_STATUS, CONNECTION_STATUS,
	}
	public static interface OnInformationChangedListener {
		void onInformationChanged(InfoCollectorType type, Object oldValue, Object newValue);
	}
	
	private static InfoCollector instance;
	
	/**
	 * 
	*/
	private static final String DEBUG_TAG = "InfoCollector";
	  
	private static final long serialVersionUID = 1L;
	private Integer signalRsrq;
	private Integer signal;
	private boolean isRsrqSignal = false;
	private boolean hasControlServerConnection;
	private boolean captivePortalFound = false;
	private String networkTypeString;
	private String networkFamily;
	private String networkName;
	private String ipv4;
	private String ipv6;
	private TrafficClassificationEnum ulTraffic;
	private TrafficClassificationEnum dlTraffic;
	private Location location;
	private List<OnInformationChangedListener> listener = new ArrayList<InfoCollector.OnInformationChangedListener>();
	
	private InfoCollector() {
		//private Constructor
	}
	
	public static InfoCollector getInstance() {
		if (instance == null) {
			Log.d(DEBUG_TAG, "new Instance");
			instance = new InfoCollector();	
		}
		return instance;
	}
	
	public Integer getSignalRsrq() {
		return signalRsrq;
	}
	
	public void setSignalRsrq(Integer signalRsrq) {
		if (this.signalRsrq != null && listener != null && !this.signalRsrq.equals(signalRsrq) || (this.signalRsrq == null && signalRsrq != null)) {
			dispatchInfoChangedEvent(InfoCollectorType.SIGNAL_RSRQ, this.signalRsrq, signalRsrq);
		}
		this.signalRsrq = signalRsrq;
	}
	
	public Integer getSignal() {
		return signal;
	}
	
	public void setSignal(Integer signal) {
		if (this.signal != null && listener != null && !this.signal.equals(signal) || (this.signal == null && signal != null)) {
			dispatchInfoChangedEvent(InfoCollectorType.SIGNAL, this.signal, signal);
		}
		this.signal = signal;
	}
	
	public boolean isRsrqSignal() {
		return isRsrqSignal;
	}
	
	public void setRsrqSignal(boolean isRsrqSignal) {
		this.isRsrqSignal = isRsrqSignal;
	}
	
	public String getNetworkTypeString() {
		return networkTypeString;
	}
	
	public void setNetworkTypeString(String networkTypeString) {
		if (this.networkTypeString != null && listener != null && !this.networkTypeString.equals(networkTypeString) || (this.networkTypeString == null && networkTypeString != null)) {
			dispatchInfoChangedEvent(InfoCollectorType.NETWORK_TYPE, this.networkTypeString, networkTypeString);
		}
		this.networkTypeString = networkTypeString;
	}
	
	public String getNetworkFamily() {
		return networkFamily;
	}
	
	public void setNetworkFamily(String networkFamily) {
		if (this.networkFamily != null && listener != null && !this.networkFamily.equals(networkFamily) || (this.networkFamily == null && networkFamily != null)) {
			dispatchInfoChangedEvent(InfoCollectorType.NETWORK_FAMILY, this.networkFamily, networkFamily);
		}
		this.networkFamily = networkFamily;
	}
	
	public String getNetworkName() {
		return networkName;
	}
	
	public void setNetworkName(String networkName) {
		if (this.networkName != null && listener != null && !this.networkName.equals(networkName) || (this.networkName == null && networkName != null)) {
			dispatchInfoChangedEvent(InfoCollectorType.NETWORK_NAME, this.networkName, networkName);
		}
		this.networkName = networkName;
	}
	
	public Location getLocation() {
		return location;
	}
	
	public void setLocation(Location location) {
		if (this.location != null && listener != null && !this.location.equals(location) || (this.location == null && location != null)) {
			dispatchInfoChangedEvent(InfoCollectorType.LOCATION, this.location, location);
		}
		this.location = location;
	}
	
	public void setIpv4(String ip) {
		if (this.ipv4 != null && listener != null && !this.ipv4.equals(ip) || (this.ipv4 == null && ip != null)) {
			dispatchInfoChangedEvent(InfoCollectorType.IPV4, this.ipv4, ip);
		}
		this.ipv4 = ip;		
	}

	public void setIpv6(String ip) {
		if (this.ipv6 != null && listener != null && !this.ipv6.equals(ip) || (this.ipv6 == null && ip != null)) {
			dispatchInfoChangedEvent(InfoCollectorType.IPV6, this.ipv6, ip);
		}
		this.ipv6 = ip;		
	}

	public String getIpv4() {
		return ipv4;
	}

	public String getIpv6() {
		return ipv6;
	}

	public TrafficClassificationEnum getUlTraffic() {
		return ulTraffic;
	}

	public void setUlTraffic(TrafficClassificationEnum ulTraffic) {
		if (this.ulTraffic != null && listener != null && !this.ulTraffic.equals(ulTraffic) || (this.ulTraffic == null && ulTraffic != null)) {
			dispatchInfoChangedEvent(InfoCollectorType.UL_TRAFFIC, this.ulTraffic, ulTraffic);
		}
		this.ulTraffic = ulTraffic;
	}

	public TrafficClassificationEnum getDlTraffic() {
		return dlTraffic;
	}

	public void setDlTraffic(TrafficClassificationEnum dlTraffic) {
		if (this.dlTraffic != null && listener != null && !this.dlTraffic.equals(dlTraffic) || (this.dlTraffic == null && dlTraffic != null)) {
			dispatchInfoChangedEvent(InfoCollectorType.DL_TRAFFIC, this.dlTraffic, dlTraffic);
		}
		this.dlTraffic = dlTraffic;
	}
	
	public boolean isHasControlServerConnection() {
		return hasControlServerConnection;
	}

	public void setHasControlServerConnection(boolean hasControlServerConnection) {
		if (listener != null && this.hasControlServerConnection != hasControlServerConnection) {
			dispatchInfoChangedEvent(InfoCollectorType.CONTROL_SERVER_CONNECTION, this.hasControlServerConnection, hasControlServerConnection);
		}
		this.hasControlServerConnection = hasControlServerConnection;
	}

	public boolean isCaptivePortalFound() {
		return captivePortalFound;
	}

	public void setCaptivePortalFound(boolean captivePortalFound) {
		if (listener != null && this.captivePortalFound != captivePortalFound) {
			dispatchInfoChangedEvent(InfoCollectorType.CAPTIVE_PORTAL_STATUS, this.captivePortalFound, captivePortalFound);
		}
		this.captivePortalFound = captivePortalFound;
	}

	public List<OnInformationChangedListener> getListenerList() {
		return listener;
	}
	
	public void addListener(OnInformationChangedListener listener) {
		if (!this.listener.contains(listener)) {
			this.listener.add(listener);
		}
	}
	
	public void removeListener(OnInformationChangedListener listener) {
		this.listener.remove(listener);
	}
	
	/**
	 * 
	 * @param type
	 * @param oldValue
	 * @param newValue
	 */
	public void dispatchInfoChangedEvent(InfoCollectorType type, Object oldValue, Object newValue) {
		//Log.d(DEBUG_TAG, "Dispatching Event: " + type + ", Listeners: " + listener.size());
		for (OnInformationChangedListener l : listener) {
			if (l != null) {
				l.onInformationChanged(type, oldValue, newValue);
			}
		}
	}
	
	/**
	 * 
	 */
	public void refresh() {
		dispatchInfoChangedEvent(InfoCollectorType.LOCATION, null, getLocation());
		dispatchInfoChangedEvent(InfoCollectorType.IPV4, null, getIpv4());
		dispatchInfoChangedEvent(InfoCollectorType.IPV6, null, getIpv6());
	}
	
	/**
	 * 
	 */
	public void refreshIpAndAntenna() {
		dispatchInfoChangedEvent(InfoCollectorType.IPV4, null, getIpv4());
		dispatchInfoChangedEvent(InfoCollectorType.IPV6, null, getIpv6());
	}
}
