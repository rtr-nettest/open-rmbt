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

import java.util.Map;

import org.json.JSONArray;

import android.app.Activity;
import android.os.AsyncTask;
import at.alladin.rmbt.android.main.RMBTMainActivity;

public class CheckMarker extends AsyncTask<Void, Void, JSONArray>
{
    private final Activity activity;
    
    private JSONArray resultList;
    
    private ControlServerConnection serverConn;
    
    private EndTaskListener endTaskListener;
    
    private final double lat, lon;
    private final int zoom, size;
    
    private boolean hasError = false;
    
    public CheckMarker(final Activity activity, final double lat, final double lon, final int zoom,
            final int size)
    {
        this.activity = activity;
        this.lat = lat;
        this.lon = lon;
        this.zoom = zoom;
        this.size = size;
    }
    
    @Override
    protected JSONArray doInBackground(final Void... params)
    {
        serverConn = new ControlServerConnection(activity.getApplicationContext(), true);
        
        final Map<String, String> optionMap = ((RMBTMainActivity) activity).getCurrentMapOptions(null);
        
        resultList = serverConn.requestMapMarker(lat, lon, zoom, size, optionMap);
        
        return resultList;
    }
    
    @Override
    protected void onPostExecute(final JSONArray resultList)
    {
        if (serverConn.hasError())
            hasError = true;
        if (endTaskListener != null)
            endTaskListener.taskEnded(resultList);
    }
    
    public void setEndTaskListener(final EndTaskListener endTaskListener)
    {
        this.endTaskListener = endTaskListener;
    }
    
    public boolean hasError()
    {
        return hasError;
    }
}
