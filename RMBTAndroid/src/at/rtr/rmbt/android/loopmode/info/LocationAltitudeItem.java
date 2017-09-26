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
package at.rtr.rmbt.android.loopmode.info;

import android.content.Context;
import android.location.Location;

import at.alladin.rmbt.android.R;
import at.rtr.rmbt.android.loopmode.DetailsListItem;
import at.rtr.rmbt.android.util.InformationCollector;

/**
 *
 * @author dz
 *
 */
public class LocationAltitudeItem implements DetailsListItem {

    private InformationCollector infoCollector;
    private Context context;

    public LocationAltitudeItem(final Context context, final InformationCollector infoCollector) {
        this.infoCollector = infoCollector;
        this.context = context;
    }

    @Override
    public String getTitle() {
        return context.getResources().getString(R.string.title_screen_info_overlay_location_altitude);
    }


    @Override
    public String getCurrent() {
        if (infoCollector != null) {
            String locationAltitudeString = "";
            final Location loc = infoCollector.getLocationInfo();
            if (loc != null && loc.hasAltitude()) {
                locationAltitudeString =  String.format("%.0f %s",loc.getAltitude(),"m");
            }
            else {
                locationAltitudeString = context.getString(R.string.not_available);
            }

            return locationAltitudeString;
        }

        return null;
    }

    @Override
    public int getStatusResource() {
        return DetailsListItem.STATUS_RESOURCE_NOT_AVAILABLE;
    }

}
