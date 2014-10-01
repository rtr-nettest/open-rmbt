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

/**
 * 
 * @author lb
 *
 */
public enum NetworkFamilyEnum {
	LAN("LAN"),
	ETHERNET("ETHERNET"),
	BLUETOOTH("BLUETOOTH"),	
	WLAN("WLAN"),
	_1xRTT("1xRTT","2G"),
	_2G3G("2G/3G"),
	_3G4G("3G/4G"),
	_2G4G("2G/4G"),
	_2G3G4G("2G/3G/4G"),
	CLI("CLI"),
	CELLULAR_ANY("MOBILE","CELLULAR_ANY"),
	GSM("GSM","2G"),
	EDGE("EDGE","2G"),
	UMTS("UMTS","3G"),
	CDMA("CDMA","2G"),
	EVDO_0("EVDO_0","2G"),
	EVDO_A("EVDO_A","2G"),
	HSDPA("HSDPA","3G"),
	HSUPA("HSUPA","3G"),
	HSPA("HSPA","3G"),
	IDEN("IDEN","2G"),
	EVDO_B("EVDO_B","2G"),
	LTE("LTE","4G"),
	EHRPD("EHRPD","2G"),
	HSPA_PLUS("HSPA+","3G"),
	UNKNOWN("UNKNOWN");
	
	protected final String networkId;
	protected final String networkFamily;
	
	NetworkFamilyEnum(String networkId, String family) {
		this.networkFamily = family;
		this.networkId = networkId;
	}
	
	NetworkFamilyEnum(String family) {
		this(family, family);
	}
	
	public String getNetworkId() {
		return networkId;
	}

	public String getNetworkFamily() {
		return networkFamily;
	}

	public static NetworkFamilyEnum getFamilyByNetworkId(String networkId) {
		for (NetworkFamilyEnum item : NetworkFamilyEnum.values()) {
			if (item.getNetworkId().equals(networkId)) {
				return item;
			}
		}

		return UNKNOWN;
	}
}
