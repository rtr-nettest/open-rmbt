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
package at.alladin.rmbt.android.util;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

public abstract class GeoLocation
{
    
    private static final String DEBUG_TAG = "Geolocation";
    
    private final LocationManager locationManager;
    
    /** Allows to obtain the phone's location, to determine the country. */
    /** The location Listener */
    private LocListener coarseLocationListener;
    
    /** Allows to obtain the phone's location, to determine the country. */
    /** The location Listener */
    private LocListener fineLocationListener;
    
    private Location curLocation = null;
    
    private boolean started = false;
    
    private final boolean gpsEnabled;
    private final long minTime;      // minimum time interval between location updates, in milliseconds
    private final float minDistance; // minimum distance between location updates, in meters
    
    public GeoLocation(final Context context, final boolean gpsEnabled)
    {
        this(context, gpsEnabled, 1000, 5);
    }
    
    public GeoLocation(final Context context, final boolean gpsEnabled, final long minTime, final float minDistance)
    {
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        this.gpsEnabled = gpsEnabled;
        this.minTime = minTime;
        this.minDistance = minDistance;
    }
    
    public static Location getLastKnownLocation(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        
        final Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.POWER_HIGH);
        final String providerName = locationManager.getBestProvider(criteria, true);
        if (providerName == null)
            return null;
        return locationManager.getLastKnownLocation(providerName);
    }
    
    /** Load Listeners */
    public void start()
    {
        if (!started)
        {
            started = true;
            
            final Criteria criteriaCoarse = new Criteria();
            /* "Coarse" accuracy means "no need to use GPS". */
            criteriaCoarse.setAccuracy(Criteria.ACCURACY_COARSE);
            criteriaCoarse.setPowerRequirement(Criteria.POWER_LOW);
            final String coarseProviderName = locationManager.getBestProvider(criteriaCoarse, true);
            
            if (coarseProviderName != null)
            {
                isBetterLocation(locationManager.getLastKnownLocation(coarseProviderName));
                
                coarseLocationListener = new LocListener();
                locationManager.requestLocationUpdates(coarseProviderName,
                minTime,
                minDistance, coarseLocationListener);
            }
            
            if (gpsEnabled)
            {
                final Criteria criteriaFine = new Criteria();
                /* "Fine" accuracy means "use GPS". */
                criteriaFine.setAccuracy(Criteria.ACCURACY_FINE);
                criteriaFine.setPowerRequirement(Criteria.POWER_HIGH);
                final String fineProviderName = locationManager.getBestProvider(criteriaFine, true);
                
                if (fineProviderName != null)
                {
                    isBetterLocation(locationManager.getLastKnownLocation(fineProviderName));
                    
                    fineLocationListener = new LocListener();
                    locationManager.requestLocationUpdates(fineProviderName,
                    minTime,
                    minDistance, fineLocationListener);
                }
            }
        }
    }
    
    /** Unload Listeners */
    public void stop()
    {
        // reset all Managers & Listeners
        if (coarseLocationListener != null)
        {
            locationManager.removeUpdates(coarseLocationListener);
            coarseLocationListener = null;
        }
        if (fineLocationListener != null)
        {
            locationManager.removeUpdates(fineLocationListener);
            fineLocationListener = null;
        }
        started = false;
    }
    
    /**
     * Abstract Method. Called when location has changed
     * 
     */
    public abstract void onLocationChanged(Location curLocation);
    
    /**
     * A listener that logs callbacks.
     */
    private class LocListener implements LocationListener
    {
        
        /* (non-Javadoc)
         * @see android.location.LocationListener#onLocationChanged(android.location.Location)
         */
        @Override
        public void onLocationChanged(final Location location)
        {
            final String outString = "Location: " + String.valueOf(location.getLatitude()) + "/"
                    + String.valueOf(location.getLongitude()) + " +/-" + String.valueOf(location.getAccuracy())
                    + "m provider: " + String.valueOf(location.getProvider());
            Log.v(DEBUG_TAG, outString);
            isBetterLocation(location);
        }
        
        @Override
        public void onProviderDisabled(final String provider)
        {
            Log.d(DEBUG_TAG, "provider disabled: " + provider);
        }
        
        @Override
        public void onProviderEnabled(final String provider)
        {
            Log.d(DEBUG_TAG, "provider enabled: " + provider);
        }
        
        @Override
        public void onStatusChanged(final String provider, final int status, final Bundle extras)
        {
            Log.d(DEBUG_TAG, "status changed: " + provider + "=" + status);
        }
    }
    
    /**
     * Determines whether one Location reading is better than the current
     * Location fix
     * 
     * @param newLocation
     *            The new Location that you want to evaluate
     * @param curLocation
     *            The current Location fix, to which you want to compare the new
     *            one
     */
    private void isBetterLocation(final Location newLocation)
    {
        
        if (newLocation == null)
            return;
        
        final long locTime = newLocation.getTime(); //milliseconds since January 1, 1970
        
        // discard locations older than Config.GEO_ACCEPT_TIME milliseconds
        // System.nanoTime() and newLocation.getElapsedRealtimeNanos() would be 
        // more accurate but would require API level 17
        final long now = System.currentTimeMillis();
        Log.d(DEBUG_TAG, "age: " + (now - locTime) + " ms");
        if (now > locTime + Config.GEO_ACCEPT_TIME)
            return;
        
        if (curLocation == null || ! gpsEnabled)
        {
            // A new location is always better than no location
            curLocation = newLocation;
            onLocationChanged(curLocation);
        }
        else
        {
            // Check whether the new location fix is newer or older
            
            final long timeDelta = locTime - curLocation.getTime();
            final boolean isSignificantlyNewer = timeDelta > Config.GEO_MIN_TIME;
            final boolean isSignificantlyOlder = timeDelta < -Config.GEO_MIN_TIME;
            final boolean isNewer = timeDelta > 0;
            
            // If it's been more than two minutes since the current location,
            // use the new location
            // because the user has likely moved
            if (isSignificantlyNewer)
            {
                curLocation = newLocation;
                onLocationChanged(curLocation);
                // If the new location is more than two minutes older, it must
                // be worse
            }
            else if (isSignificantlyOlder)
            {
                // keep old value
            }
            else
            {
                // Check whether the new location fix is more or less accurate
                final int accuracyDelta = (int) (newLocation.getAccuracy() - curLocation.getAccuracy());
                final boolean isLessAccurate = accuracyDelta > 0;
                final boolean isMoreAccurate = accuracyDelta < 0;
                final boolean isSignificantlyLessAccurate = accuracyDelta > 200;
                
                // Check if the old and new location are from the same provider
                final boolean isFromSameProvider = isSameProvider(newLocation.getProvider(), curLocation.getProvider());
                
                // Determine location quality using a combination of timeliness
                // and accuracy
                if (isMoreAccurate)
                {
                    curLocation = newLocation;
                    onLocationChanged(curLocation);
                }
                else if (isNewer && !isLessAccurate)
                {
                    curLocation = newLocation;
                    onLocationChanged(curLocation);
                }
                else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider)
                {
                    curLocation = newLocation;
                    onLocationChanged(curLocation);
                }
                // keep old location otherwise
            }
        }
    }
    
    /** Checks whether two providers are the same */
    private static boolean isSameProvider(final String provider1, final String provider2)
    {
        if (provider1 == null)
            return provider2 == null;
        return provider1.equals(provider2);
    }
    
    public Location getLastKnownLocation()
    {
        if (!started)
            throw new IllegalStateException();
        return curLocation;
    }
    
    @Override
    protected void finalize() throws Throwable
    {
        // should be called manually, but just to be sure
        stop();
        super.finalize();
    }
    
}
