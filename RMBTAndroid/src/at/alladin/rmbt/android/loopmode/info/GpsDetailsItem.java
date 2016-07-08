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
import android.location.Location;
import at.alladin.openrmbt.android.R;
import at.alladin.rmbt.android.loopmode.DetailsListItem;
import at.alladin.rmbt.android.main.AppConstants;
import at.alladin.rmbt.android.util.Helperfunctions;
import at.alladin.rmbt.android.util.InformationCollector;

/**
 * 
 * @author lb
 *
 */
public class GpsDetailsItem implements DetailsListItem {

	private InformationCollector infoCollector;
	private Context context;
	
	public GpsDetailsItem(final Context context, final InformationCollector infoCollector) {
		this.infoCollector = infoCollector;
		this.context = context;
	}
	
	@Override
	public String getTitle() {
		return context.getResources().getString(R.string.lm_details_gps);
	}

	@Override
	public String getCurrent() {
		if (infoCollector != null) {
	    	String locationString = "";
	    	final Location loc = infoCollector.getLocationInfo();
	    	if (loc != null) {
	    		locationString = Helperfunctions.getLocationString(context, context.getResources(), loc,0);
	    	}
	    	else {
	    		locationString = context.getString(R.string.not_available);
	    	}
    		
	    	return locationString;
		}
		
		return null;
	}

	@Override
	public int getStatusResource() {
		if (infoCollector != null) {
	    	final Location loc = infoCollector.getLocationInfo();
	    	if (loc != null) {
	    		if (loc.getAccuracy() <= AppConstants.LOOP_MODE_GPS_ACCURACY_CRITERIA) {
	    			return R.drawable.ic_action_location_found;	
	    		}
	    		
	    		return R.drawable.ic_action_location_found_network;
	    	}    		
		}
		
		return R.drawable.ic_action_location_no_permission;
	}

}
