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
import at.alladin.rmbt.android.util.Helperfunctions;
import at.alladin.rmbt.android.util.InformationCollector;

/**
 * 
 * @author lb
 *
 */
public class GpsAccuracyDetailsItem implements DetailsListItem {

	private InformationCollector infoCollector;
	private Context context;
	
	public GpsAccuracyDetailsItem(final Context context, final InformationCollector infoCollector) {
		this.infoCollector = infoCollector;
		this.context = context;
	}
	
	@Override
	public String getTitle() {
		return context.getResources().getString(R.string.lm_details_gps_accuracy);
	}

	@Override
	public String getCurrent() {
		if (infoCollector != null) {
	    	String locationString = null;
	    	final Location loc = infoCollector.getLocationInfo();
	    	if (loc != null) { 
	    		final int satellites;
	    		if (loc.getExtras() != null) {
	    		    satellites = loc.getExtras().getInt("satellites");
	    		}
	    		else {
	    		    satellites = 0;
	    		}
	    		
                locationString = Helperfunctions.convertLocationAccuracy(context.getResources(), 
	    				loc.hasAccuracy(), loc.getAccuracy(), satellites);
                
    	    	locationString += " (" + Helperfunctions.convertLocationTime(loc.getTime()) + ")";
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
		return DetailsListItem.STATUS_RESOURCE_NOT_AVAILABLE;
	}

}
