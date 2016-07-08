/*******************************************************************************
 * Copyright 2013-2016 alladin-IT GmbH
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

import android.content.Context;
import android.os.AsyncTask;
import at.alladin.rmbt.android.views.ResultDetailsView.ResultDetailType;

public class CheckTestResultDetailTask extends AsyncTask<String, Void, JSONArray>
{
	private final ResultDetailType resultType;
    
    private final Context context;
    
    private JSONArray resultList;
    
    private ControlServerConnection serverConn;
    
    private EndTaskListener endTaskListener;
    
    private boolean hasError = false;
    
    public CheckTestResultDetailTask(final Context context, final ResultDetailType resultType)
    {
        this.context = context;
        this.resultType = resultType;
    }
    
    @Override
    protected JSONArray doInBackground(final String... uid)
    {
        serverConn = new ControlServerConnection(context);

        try {
            if (uid != null && uid[0] != null)
            {
            	switch(this.resultType) {
            	case SPEEDTEST:
                	resultList = serverConn.requestTestResultDetail(uid[0]);
                	break;
            	case QUALITY_OF_SERVICE_TEST:
            		resultList = serverConn.requestTestResultQoS(uid[0]);
            		break;
            	case OPENDATA:
            		resultList = new JSONArray();
    				resultList.put(0, serverConn.requestOpenDataTestResult(uid[0]));
            		break;
            	}
            }	
        }
        catch (JSONException e) {
        	e.printStackTrace();
        }
        
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
