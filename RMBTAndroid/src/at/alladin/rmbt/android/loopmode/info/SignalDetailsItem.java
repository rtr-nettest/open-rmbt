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

/**
 * 
 * @author lb
 *
 */
public class SignalDetailsItem implements DetailsListItem {

	private InformationCollector informationCollector;
	private Context context;
	private Integer lastSignal;
	
	public SignalDetailsItem(final Context context, final InformationCollector infoCollector) {
		this.informationCollector = infoCollector;
		this.context = context;
	}
	
	@Override
	public String getTitle() {
		return context.getResources().getString(R.string.lm_details_signal);
	}

	@Override
	public String getCurrent() {
		String signalString = null;
		
		if (informationCollector != null) {
			Integer curSignal = lastSignal;
			final int lastNetworkType = informationCollector.getNetwork();
            final String lastNetworkTypeString = Helperfunctions.getNetworkTypeName(lastNetworkType);

			if (!"UNKNOWN".equals(lastNetworkTypeString)) {				
                Integer signal = informationCollector.getSignal();
                if (!"BLUETOOTH".equals(lastNetworkTypeString) && !"ETHERNET".equals(lastNetworkTypeString)) {
                	if (signal != null && signal > Integer.MIN_VALUE && signal < 0) {
	                	int signalType = informationCollector.getSignalType();
	                	curSignal = signal;

	                	if (signalType == InformationCollector.SINGAL_TYPE_RSRP) {
		                	signalString = signal + " dBm";
		                	Integer signalRsrq = informationCollector.getSignalRsrq();
		                	if (signalRsrq != null) {
		    					signalString += "/" + context.getString(R.string.term_signal_quality) + ": " + signalRsrq + " dB";
		                	}
	                	}
	                	else {
	                		signalString = signal + " dBm";
	                	}
                	}
                	else {
                		curSignal = Integer.MIN_VALUE;
                	}
                }
			}
			else {
                curSignal = Integer.MIN_VALUE;
			}
			
            lastSignal = curSignal;
		}
		
		return signalString;
	}

	@Override
	public int getStatusResource() {
		return DetailsListItem.STATUS_RESOURCE_NOT_AVAILABLE;
	}

}
