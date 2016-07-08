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

import org.json.JSONArray;

import android.app.Activity;
import android.os.AsyncTask;

public class CheckSyncTask extends AsyncTask<String, Void, JSONArray>
{
    
    // private static final String DEBUG_TAG = "CheckSyncTask";
    
    private final Activity activity;
    
    private JSONArray resultList;
    
    private ControlServerConnection serverConn;
    
    private EndTaskListener endTaskListener;
    
    private boolean hasError = false;
    
    public CheckSyncTask(final Activity activity)
    {
        this.activity = activity;
        
    }
    
    @Override
    protected JSONArray doInBackground(final String... syncCode)
    {
        serverConn = new ControlServerConnection(activity.getApplicationContext());
        
        final String uuid = ConfigHelper.getUUID(activity.getApplicationContext());
        
        if (uuid != null && uuid.length() > 0 && syncCode[0] != null)
            resultList = serverConn.requestSyncCode(uuid, syncCode[0]);
        
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
