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

import java.text.DateFormat;
import java.util.Locale;

import android.content.Context;
import android.os.SystemClock;
import at.alladin.openrmbt.android.R;
import at.alladin.rmbt.android.loopmode.DetailsListItem;
import at.alladin.rmbt.android.loopmode.LoopModeResults;

/**
 * provides information about the loop mode triggers 
 * @author lb
 *
 */
public class LoopModeTriggerItem implements DetailsListItem {

	final LoopModeResults results;
	final Context context;
	final TriggerType triggerType;
	
	final DateFormat timeFormat = DateFormat.getTimeInstance(DateFormat.MEDIUM);
	
	public static enum TriggerType {
		MOVEMENT,
		TIME
	}
	
	public LoopModeTriggerItem(final Context context, final LoopModeResults results, final TriggerType triggerType) {
		this.results = results;
		this.context = context;
		this.triggerType = triggerType;
	}
	
	@Override
	public String getTitle() {
		switch (triggerType) {
		case MOVEMENT:
			return context.getResources().getString(R.string.lm_measurement_movement);
		case TIME:
			return context.getResources().getString(R.string.lm_measurement_delay);
		}
		
		return null;
	}

	@Override
	public String getCurrent() {
		if (results != null) {
			if (results.getMaxTests() <= results.getNumberOfTests()) {
				//loop mode finished?
				return context.getString(R.string.loop_notification_finished_title);
			}

			switch (triggerType) {
			case MOVEMENT:
				return ((int)results.getLastDistance()) + "/" + ((int)results.getMaxMovement()) + " m";
			case TIME:
		        final String elapsedTimeString = formatSeconds((long) ((SystemClock.elapsedRealtime() - results.getLastTestTime()) / 1e3), 1);
		        final String maxTimeString = formatSeconds((long) (results.getMaxDelay() / 1e3), 1);
		        return elapsedTimeString + "/" + maxTimeString;
			}
		}
		
		return null;
	}

	@Override
	public int getStatusResource() {
		return DetailsListItem.STATUS_RESOURCE_NOT_AVAILABLE;
	}

	/**
	 * formats seconds to a digital time format (00:00:00)
	 * @param totalSeconds
	 * @param minLeadingZeroesUnits minimum leading zeroes units: 1 = minutes (00:??), 2 = (hours 00:00:??), etc..
	 * @return
	 */
	public static String formatSeconds(final long totalSeconds, final int minLeadingZeroesUnits) {
        final long hours = totalSeconds / 3600;
        final long minutes = (totalSeconds % 3600) / 60;
        final long seconds = totalSeconds % 60;

        if (hours > 0 || minLeadingZeroesUnits >= 2) {
        	return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds);
        }
        
        if (minutes > 0 || minLeadingZeroesUnits >= 1) {
        	return String.format(Locale.US, "%02d:%02d", minutes, seconds);
        }
        
    	return String.format(Locale.US, "%02d", seconds);
	}
}
