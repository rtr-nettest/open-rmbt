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

import java.util.ArrayList;

import org.json.JSONArray;

import android.os.AsyncTask;
import at.alladin.rmbt.android.main.RMBTMainActivity;

public class CheckHistoryTask extends AsyncTask<Void, Void, JSONArray>
{
    
    private final RMBTMainActivity activity;
    
    private JSONArray historyList;
    
    private String uuid;
    
    private ControlServerConnection serverConn;
    
    private EndTaskListener endTaskListener;
    
    private final ArrayList<String> devicesToShow;
    
    private final ArrayList<String> networksToShow;
    
    private boolean hasError = false;
    
    public CheckHistoryTask(final RMBTMainActivity rmbtMainActivity, final ArrayList<String> devicesToShow,
            final ArrayList<String> networksToShow)
    {
        this.activity = rmbtMainActivity;
        
        this.devicesToShow = devicesToShow;
        
        this.networksToShow = networksToShow;
    }
    
    @Override
    protected JSONArray doInBackground(final Void... params)
    {
        serverConn = new ControlServerConnection(activity.getApplicationContext());
        
        uuid = ConfigHelper.getUUID(activity.getApplicationContext());
        
        if (uuid.length() > 0)
            historyList = serverConn.requestHistory(uuid, devicesToShow, networksToShow,
                    ((RMBTMainActivity) activity).getHistoryResultLimit());
        
        return historyList;
    }
    
    @Override
    protected void onPostExecute(final JSONArray historyList)
    {
        if (serverConn.hasError())
            hasError = true;
        if (endTaskListener != null)
            endTaskListener.taskEnded(historyList);
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
