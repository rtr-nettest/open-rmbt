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

import android.os.AsyncTask;
import android.util.Log;
import at.alladin.rmbt.android.main.RMBTMainActivity;
import at.alladin.rmbt.util.model.option.ServerOptionContainer;
import at.alladin.rmbt.util.model.shared.MapOptions;

public class GetMapOptionsInfoTask extends AsyncTask<Void, Void, MapOptions>
{
    /**
     * 
     */
    private static final String DEBUG_TAG = "GetMapOptionsInfoTask";
    
    /**
     * 
     */
    private final RMBTMainActivity activity;
    
    /**
     * 
     */
    private ControlServerConnection serverConn;
    
    /**
     * 
     */
    private boolean hasError = false;
    
    /**
     * 
     * @param activity
     */
    public GetMapOptionsInfoTask(final RMBTMainActivity activity)
    {
        this.activity = activity;
    }
    
    /**
     * 
     */
    @Override
    protected MapOptions doInBackground(final Void... params)
    {
        try {
            serverConn = new ControlServerConnection(activity.getApplicationContext(), true);
            final MapOptions result = serverConn.requestMapOptions();
            return result;
        }
        catch (Exception e)
        {
        	e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 
     */
    @Override
    protected void onPostExecute(final MapOptions result)
    {
        if (serverConn.hasError()) {
            hasError = true;
        }
        else if (result != null && result.getMapFilterList() != null) {
        	final ServerOptionContainer mapOptions = new ServerOptionContainer(result.getMapFilterList());
        	mapOptions.setDefault();
        	activity.setMapOptions(mapOptions);
            
        }
        else {
            Log.i(DEBUG_TAG, "LEERE LISTE");
        }        
    }
    
    /**
     * 
     * @return
     */
    public boolean hasError()
    {
        return hasError;
    }
}
