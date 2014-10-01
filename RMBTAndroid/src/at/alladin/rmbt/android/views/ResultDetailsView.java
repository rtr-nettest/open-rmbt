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
package at.alladin.rmbt.android.views;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import at.alladin.openrmbt.android.R;
import at.alladin.rmbt.android.main.RMBTMainActivity;
import at.alladin.rmbt.android.util.CheckTestResultDetailTask;
import at.alladin.rmbt.android.util.EndTaskListener;
import at.alladin.rmbt.android.util.Helperfunctions;

public class ResultDetailsView extends LinearLayout implements EndTaskListener {
	
	public static enum ResultDetailType {
		SPEEDTEST,
		QUALITY_OF_SERVICE_TEST,
		OPENDATA,
	}

	private View view;
	
    private static final String DEBUG_TAG = "ResultDetailsView";
    
    public static final String ARG_UID = "uid";
    
    private RMBTMainActivity activity;
    
    private CheckTestResultDetailTask testResultDetailTask;
    
    private ListAdapter valueList;
    
    private ListView listView;
    
    private TextView emptyView;
    
    private ProgressBar progessBar;
    
    private ArrayList<HashMap<String, String>> itemList;
    
    private String uid;
    
    private JSONArray testResult;
    
    private EndTaskListener resultFetchEndTaskListener;

	    
	/**
	 * 
	 * @param context
	 */
	public ResultDetailsView(Context context, RMBTMainActivity activity, String uid, JSONArray testResult) {
		this(context, null, activity, uid, testResult);
	}
	
	/**
	 * 
	 * @param context
	 * @param attrs
	 */
	public ResultDetailsView(Context context, AttributeSet attrs, RMBTMainActivity activity, String uid, JSONArray testResult) {
		super(context, attrs);

		LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.view = createView(layoutInflater);
		this.activity = activity;
		this.uid = uid;
		this.testResult = testResult;
	}
    
    /**
     * 
     * @param inflater
     * @return
     */
    public View createView(final LayoutInflater inflater)
    {        
        final View view = inflater.inflate(R.layout.test_result_detail, this);
        
        listView = (ListView) view.findViewById(R.id.valueList);
        listView.setVisibility(View.GONE);
        
        emptyView = (TextView) view.findViewById(R.id.infoText);
        emptyView.setVisibility(View.GONE);
        
        progessBar = (ProgressBar) view.findViewById(R.id.progressBar);
                
        return view;
    }
    
    public void initialize(EndTaskListener resultFetchEndTaskListener) {        
        itemList = new ArrayList<HashMap<String, String>>();

    	this.resultFetchEndTaskListener = resultFetchEndTaskListener;
    	
        if ((testResultDetailTask == null || testResultDetailTask != null || testResultDetailTask.isCancelled()) && uid != null)
        {
        	if (this.testResult!=null) {
        		System.out.println("TESTRESULT found ResultDetailsView");
        		taskEnded(this.testResult);
        	}
        	else {
        		System.out.println("TESTRESULT NOT found ResultDetailsView");
            	System.out.println("initializing ResultDetailsView");
            	
                testResultDetailTask = new CheckTestResultDetailTask(activity, ResultDetailType.SPEEDTEST);
                
                testResultDetailTask.setEndTaskListener(this);
                testResultDetailTask.execute(uid);        		
        	}
        }
    }
    
    @Override
    public void taskEnded(final JSONArray testResultDetail)
    {
        //if (getVisibility()!=View.VISIBLE)
        //    return;
        
    	if (this.resultFetchEndTaskListener != null) {
    		this.resultFetchEndTaskListener.taskEnded(testResultDetail);
    	}
    	
        if (testResultDetail != null && testResultDetail.length() > 0 && (testResultDetailTask==null || !testResultDetailTask.hasError()))
        {
        	this.testResult = testResultDetail;
        	
        	System.out.println("testResultDetail: " + testResultDetail);
        	
            try
            {
                
                HashMap<String, String> viewItem;

                for (int i = 0; i < testResultDetail.length(); i++)
                {
                    
                    final JSONObject singleItem = testResultDetail.getJSONObject(i);
                    
                    viewItem = new HashMap<String, String>();
                    viewItem.put("name", singleItem.optString("title", ""));
                    
                    if (singleItem.has("time"))
                    {
                        final String timeString = Helperfunctions
                                .formatTimestampWithTimezone(singleItem.optLong("time", 0),
                                        singleItem.optString("timezone", null), true /* seconds */);
                        viewItem.put("value", timeString == null ? "-" : timeString);
                    }
                    else
                        viewItem.put("value", singleItem.optString("value", ""));
                    itemList.add(viewItem);
                }	
            }
            catch (final JSONException e)
            {
                e.printStackTrace();
            }
            
            valueList = new SimpleAdapter(activity, itemList, R.layout.test_result_detail_item, new String[] {
                    "name", "value" }, new int[] { R.id.name, R.id.value });
            
            listView.setAdapter(valueList);
            
            listView.invalidate();
            
            progessBar.setVisibility(View.GONE);
            emptyView.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
            
        }
        else
        {
            Log.i(DEBUG_TAG, "LEERE LISTE");
            progessBar.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
            emptyView.setText(activity.getString(R.string.error_no_data));
            emptyView.invalidate();
        }
        
    }
    
    /**
     * 
     * @return
     */
    public JSONArray getTestResult() {
    	return testResult;
    }
}
