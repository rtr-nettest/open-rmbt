/*******************************************************************************
 * Copyright 2013 alladin-IT OG
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;
import at.alladin.rmbt.android.main.RMBTMainActivity;

/**
 * 
 * @author
 * 
 */
public class CheckSettingsTask extends AsyncTask<Void, Void, JSONArray>
{
    
    /**
	 * 
	 */
    private static final String DEBUG_TAG = "CheckSettingsTask";
    
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
    private EndTaskListener endTaskListener;
    
    /**
	 * 
	 */
    private boolean hasError = false;
    
    /**
     * 
     * @param activity
     */
    public CheckSettingsTask(final RMBTMainActivity activity)
    {
        this.activity = activity;
        
    }
    
    /**
	 * 
	 */
    @Override
    protected JSONArray doInBackground(final Void... params)
    {
        JSONArray resultList = null;
        
        serverConn = new ControlServerConnection(activity.getApplicationContext());
        
        resultList = serverConn.requestSettings();
        
        return resultList;
    }
    
    /**
	 * 
	 */
    @Override
    protected void onCancelled()
    {
        if (serverConn != null)
        {
            serverConn.unload();
            serverConn = null;
        }
    }
    
    /**
	 * 
	 */
    @Override
    protected void onPostExecute(final JSONArray resultList)
    {
        try
        {
        if (serverConn.hasError())
            hasError = true;
        else if (resultList != null && resultList.length() > 0)
        {
            
                JSONObject resultListItem;
                
                try
                {
                    resultListItem = resultList.getJSONObject(0);
                    
                    final String uuid = resultListItem.optString("uuid", "");
                    if (uuid != null && uuid.length() != 0)
                        ConfigHelper.setUUID(activity.getApplicationContext(), uuid);
                    
                    // ///////////////////////////////////////////////////////
                    // HISTORY / FILTER
                    
                    final JSONObject historyObject = resultListItem.getJSONObject("history");
                    
                    final JSONArray deviceArray = historyObject.getJSONArray("devices");
                    final JSONArray networkArray = historyObject.getJSONArray("networks");
                    
                    final String historyDevices[] = new String[deviceArray.length()];
                    
                    for (int i = 0; i < deviceArray.length(); i++)
                        historyDevices[i] = deviceArray.getString(i);
                    
                    final String historyNetworks[] = new String[networkArray.length()];
                    
                    for (int i = 0; i < networkArray.length(); i++)
                        historyNetworks[i] = networkArray.getString(i);
                    
                    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                    
                    activity.setSettings(historyDevices, historyNetworks);
                    
                    activity.setHistoryDirty(true);
                    
                }
                catch (final JSONException e)
                {
                    e.printStackTrace();
                }
                
            }
            else
                Log.i(DEBUG_TAG, "LEERE LISTE");
        }
        finally
        {
            if (endTaskListener != null)
                endTaskListener.taskEnded(resultList);
        }
    }
    
    /**
     * 
     * @param endTaskListener
     */
    public void setEndTaskListener(final EndTaskListener endTaskListener)
    {
        this.endTaskListener = endTaskListener;
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
