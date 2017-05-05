/*******************************************************************************
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
package at.alladin.rmbt.android.loopmode.info;

import android.content.Context;
import android.location.Location;

import java.text.DecimalFormat;

import at.alladin.rmbt.android.R;
import at.alladin.rmbt.android.loopmode.DetailsListItem;
import at.alladin.rmbt.android.main.AppConstants;
import at.alladin.rmbt.android.util.InformationCollector;

/**
 * @author dz
 */
public class LocationSpeedItem implements DetailsListItem {

    private InformationCollector infoCollector;
    private Context context;

    public LocationSpeedItem(final Context context, final InformationCollector infoCollector) {
        this.infoCollector = infoCollector;
        this.context = context;
    }

    @Override
    public String getTitle() {
        return context.getResources().getString(R.string.title_screen_info_overlay_location_speed);
    }


    @Override
    public String getCurrent() {
        if (infoCollector != null) {
            String locationSpeedString = "";
            final Location loc = infoCollector.getLocationInfo();
            if (loc != null && loc.hasSpeed()) {
                locationSpeedString = String.format("%.1f %s", loc.getSpeed() * 3.6d, context.getResources().getString(R.string.test_location_km_h));
            } else {
                locationSpeedString = context.getString(R.string.not_available);
            }

            return locationSpeedString;
        }

        return null;
    }

    @Override
    public int getStatusResource() {
        return DetailsListItem.STATUS_RESOURCE_NOT_AVAILABLE;
    }

}
