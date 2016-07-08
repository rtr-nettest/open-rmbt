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
package at.alladin.rmbt.android.fragments.result;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import at.alladin.rmbt.android.util.InformationCollector;
import at.alladin.rmbt.client.v2.task.result.QoSTestResult;

public class RMBTTestResultDetailFragment extends Fragment implements EndTaskListener
{
	public static enum ResultDetailType {
		SPEEDTEST,
		QOS_TEST
	}
	
    private static final String DEBUG_TAG = "RMBTTestResultDetailFragment";
    
    public static final String ARG_UID = "uid";
    
    private CheckTestResultDetailTask testResultDetailTask;
    
    private ListAdapter valueList;
    
    private ListView listView;
    
    private TextView emptyView;
    
    private ProgressBar progessBar;
    
    private ArrayList<HashMap<String, String>> itemList;
    
    private ResultDetailType testType;
    
    /**
     * 
     */
    public RMBTTestResultDetailFragment() {
    	
    }
    
//    /**
//     * 
//     * @param testType
//     */
//    public RMBTTestResultDetailFragment(ResultDetailType testType) {
//    	this.testType = testType;
//    }
    
    public void setTestResultDetailType(ResultDetailType testType) {
    	this.testType = testType;
    }
    
    
    @Override
    public void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);        
    }
    
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState)
    {
        // The last two arguments ensure LayoutParams are inflated
        // properly.
        super.onCreateView(inflater, container, savedInstanceState);
        
        final View view = inflater.inflate(R.layout.test_result_detail, container, false);
        
        final Bundle args = getArguments();
        /*
         * ((TextView) view.findViewById(R.id.text1)).setText(Integer
         * .toString(args.getInt(ARG_UID)));
         */
        
        listView = (ListView) view.findViewById(R.id.valueList);
        listView.setVisibility(View.GONE);
        
        emptyView = (TextView) view.findViewById(R.id.infoText);
        emptyView.setVisibility(View.GONE);
        
        progessBar = (ProgressBar) view.findViewById(R.id.progressBar);
        
        if ((testResultDetailTask == null || testResultDetailTask != null || testResultDetailTask.isCancelled())
                && args.getString(ARG_UID) != null)
        {
            testResultDetailTask = new CheckTestResultDetailTask((RMBTMainActivity) getActivity(), at.alladin.rmbt.android.views.ResultDetailsView.ResultDetailType.SPEEDTEST);
            
            testResultDetailTask.setEndTaskListener(this);
            testResultDetailTask.execute(args.getString(ARG_UID));
        }
        
        itemList = new ArrayList<HashMap<String, String>>();
        
        // listView.setEmptyView(emptyView);
        
        return view;
    }
    
    @Override
    public void taskEnded(final JSONArray testResultDetail)
    {
        if (!isVisible())
            return;
        
        if (testResultDetail != null && testResultDetail.length() > 0 && !testResultDetailTask.hasError())
        {
            
            try
            {
                
                HashMap<String, String> viewItem;
                
                if (testType == ResultDetailType.SPEEDTEST) {
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
                else if (testType == ResultDetailType.QOS_TEST) {
                    if (InformationCollector.qoSResult != null) {
                    	int c = 0;
                        for (QoSTestResult nnResult : InformationCollector.qoSResult.getResults()) {
                            viewItem = new HashMap<String, String>();
                            viewItem.put("name", "TEST " + (c++) + "");
                            viewItem.put("value", nnResult.getTestType().name());
                            itemList.add(viewItem);

                        	for (Entry<String, Object> item : nnResult.getResultMap().entrySet()) {
                                viewItem = new HashMap<String, String>();
                                viewItem.put("name", item.getKey());
                                viewItem.put("value", String.valueOf(item.getValue()));
                                itemList.add(viewItem);
                        	}
                        }                	
                    }
                    
                	
                }
                                
            }
            catch (final JSONException e)
            {
                e.printStackTrace();
            }
            
            valueList = new SimpleAdapter(getActivity(), itemList, R.layout.test_result_detail_item, new String[] {
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
            emptyView.setText(getString(R.string.error_no_data));
            emptyView.invalidate();
        }
        
    }
    
}
