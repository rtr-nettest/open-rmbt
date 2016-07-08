/*******************************************************************************
 * Copyright 2016 Specure GmbH
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
package at.alladin.rmbt.android.loopmode.info;

import android.content.Context;
import at.alladin.openrmbt.android.R;
import at.alladin.rmbt.android.loopmode.DetailsListItem;
import at.alladin.rmbt.android.util.Helperfunctions;
import at.alladin.rmbt.android.util.InformationCollector;
import at.alladin.rmbt.android.util.net.NetworkFamilyEnum;

/**
 * 
 * @author lb
 *
 */
public class NetworkTypeDetailsItem implements DetailsListItem {

	private InformationCollector infoCollector;
	private Context context;
	
	private String lastNetworkTypeString;
	
	public NetworkTypeDetailsItem(final Context context, final InformationCollector infoCollector) {
		this.infoCollector = infoCollector;
		this.context = context;
	}
	
	@Override
	public String getTitle() {
		return context.getResources().getString(R.string.lm_details_access);
	}

	@Override
	public String getCurrent() {
		if (infoCollector != null) {
			final int lastNetworkType = infoCollector.getNetwork();
			lastNetworkTypeString = Helperfunctions.getNetworkTypeName(lastNetworkType);
			
			if (lastNetworkTypeString != null && !"UNKNOWN".equals(lastNetworkTypeString)) {
				NetworkFamilyEnum networkFamily = NetworkFamilyEnum.getFamilyByNetworkId(lastNetworkTypeString);
	        	if (!NetworkFamilyEnum.UNKNOWN.equals(networkFamily)) {
	        		if (lastNetworkTypeString.equals(NetworkFamilyEnum.WLAN.getNetworkFamily())) {
	        			return lastNetworkTypeString;
	        		}
	        		else {
	            		if (lastNetworkTypeString.equals(networkFamily.getNetworkFamily())) {
	            			return lastNetworkTypeString;
	            		}
	            		else {
	                		return networkFamily.getNetworkFamily() + "/" + lastNetworkTypeString;
	            		}	                		
	        		}	                		
	        	}
	        }
		}
		
		return null;
	}

	@Override
	public int getStatusResource() {
		if (infoCollector != null) {
			final int lastNetworkType = infoCollector.getNetwork();
			lastNetworkTypeString = Helperfunctions.getNetworkTypeName(lastNetworkType);
			
			if (lastNetworkTypeString != null && !"UNKNOWN".equals(lastNetworkTypeString)) {
				NetworkFamilyEnum networkFamily = NetworkFamilyEnum.getFamilyByNetworkId(lastNetworkTypeString);
	        	if (!NetworkFamilyEnum.UNKNOWN.equals(networkFamily)) {
	        		if (lastNetworkTypeString.equals(NetworkFamilyEnum.WLAN.getNetworkFamily())) {
	        			return R.drawable.signal_wlan;
	        		}
	        		else {
	        			return R.drawable.signal_mobile;
	        		}	                		
	        	}
	        }
		}

		return R.drawable.signal_no_connection;
	}

}
