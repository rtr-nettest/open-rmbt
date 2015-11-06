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
import org.json.JSONException;
import org.json.JSONObject;

import android.app.DialogFragment;
import android.os.AsyncTask;
import at.alladin.openrmbt.android.R;
import at.alladin.rmbt.android.main.RMBTMainActivity;

public class CheckNewsTask extends AsyncTask<Void, Void, JSONArray>
{
    
    private final RMBTMainActivity activity;
    
    private JSONArray newsList;
    
    long lastNewsUid;
    
    ControlServerConnection serverConn;
    
    public CheckNewsTask(final RMBTMainActivity activity)
    {
        this.activity = activity;
        
    }
    
    @Override
    protected JSONArray doInBackground(final Void... params)
    {
        serverConn = new ControlServerConnection(activity);
        
        lastNewsUid = ConfigHelper.getLastNewsUid(activity);
        
        newsList = serverConn.requestNews(lastNewsUid);
        
        return newsList;
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
    protected void onPostExecute(final JSONArray newsList)
    {
        
        if (newsList != null && newsList.length() > 0 && !serverConn.hasError())
            for (int i = 0; i < newsList.length(); i++)
                if (!isCancelled() && !Thread.interrupted())
                    try
                    {
                        
                        final JSONObject newsItem = newsList.getJSONObject(i);
                        
                        final DialogFragment newFragment = RMBTAlertDialogFragment.newInstance(
                                newsItem.optString("title", activity.getString(R.string.news_title)),
                                newsItem.optString("text", activity.getString(R.string.news_no_message)), null);
                        
                        newFragment.show(activity.getFragmentManager(), "dialog");
                        
                        if (newsItem.has("uid"))
                        {
                            if (lastNewsUid < newsItem.getLong("uid"))
                                lastNewsUid = newsItem.getLong("uid");
                        }
                    }
                    catch (final JSONException e)
                    {
                        e.printStackTrace();
                    }
        
        ConfigHelper.setLastNewsUid(activity.getApplicationContext(), lastNewsUid);
    }
    
}
