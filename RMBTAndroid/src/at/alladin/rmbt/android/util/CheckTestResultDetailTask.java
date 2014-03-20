/*******************************************************************************
 * Copyright 2013-2014 alladin-IT GmbH
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

import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;

public class CheckTestResultDetailTask extends AsyncTask<String, Void, JSONArray>
{
    
    private final FragmentActivity activity;
    
    private JSONArray resultList;
    
    private ControlServerConnection serverConn;
    
    private EndTaskListener endTaskListener;
    
    private boolean hasError = false;
    
    public CheckTestResultDetailTask(final FragmentActivity activity)
    {
        this.activity = activity;
        
    }
    
    @Override
    protected JSONArray doInBackground(final String... uid)
    {
        serverConn = new ControlServerConnection(activity.getApplicationContext());
        
        if (uid != null && uid[0] != null)
            resultList = serverConn.requestTestResultDetail(uid[0]);
        
        return resultList;
    }
    
    @Override
    protected void onCancelled()
    {
        if (serverConn != null)
        {
            serverConn.unload();
            serverConn = null;
        }
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
