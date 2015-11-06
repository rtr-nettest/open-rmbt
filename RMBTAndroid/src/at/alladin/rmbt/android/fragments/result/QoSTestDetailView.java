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
package at.alladin.rmbt.android.fragments.result;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import at.alladin.openrmbt.android.R;
import at.alladin.rmbt.android.main.RMBTMainActivity;
import at.alladin.rmbt.client.v2.task.result.QoSServerResult;
import at.alladin.rmbt.client.v2.task.result.QoSServerResult.DetailType;
import at.alladin.rmbt.client.v2.task.result.QoSServerResultDesc;
import at.alladin.rmbt.client.v2.task.result.QoSTestResultEnum;

public class QoSTestDetailView extends ScrollView {
	
	public static final String OPTION_TEST_CATEGORY = "test_category";
	
	public static final String BUNDLE_QOS_RESULT = "qos_result";
	
	public static final String BUNDLE_QOS_DESC_LIST = "qos_desc_list";
	
	QoSTestResultEnum curTestResult;
	
	private QoSServerResult result;
	
    private List<QoSServerResultDesc> descList;

	private SimpleAdapter successListAdapter;
	private SimpleAdapter failureListAdapter;
	
	private RMBTMainActivity activity;
	
	public QoSTestDetailView(Context context, RMBTMainActivity activity, QoSServerResult result, List<QoSServerResultDesc> descList) {
		super(context);
		
		this.activity = activity;
		this.result = result;
		this.descList = descList;
		setFillViewport(true);
		final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		createView(inflater, this);
	}

	public View createView(LayoutInflater inflater, ViewGroup container) {
        View view = null;
               
        view = inflater.inflate(R.layout.qos_test_view, this);

        List<HashMap<String, String>> successList = new ArrayList<HashMap<String, String>>();
        List<HashMap<String, String>> failureList = new ArrayList<HashMap<String, String>>();
        HashMap<String, String> viewItem;

        System.out.println(result);
        
        if (descList != null) {
            for (int i = 0; i < descList.size(); i++) {
            	QoSServerResultDesc desc = descList.get(i);
            	if (desc.getUidSet().contains(result.getUid())) {
                	viewItem = new HashMap<String, String>();
                	viewItem.put("name", desc.getDesc());
                	
            		if (DetailType.OK.equals(desc.getStatus())) {
                    	successList.add(viewItem);	
            		}
            		else {
                    	failureList.add(viewItem);
            		}        		
            	}
            }
        }
        
    	LinearLayout successListView = (LinearLayout) view.findViewById(R.id.value_list_success);
    	
        if (successList.size() > 0) {            
        	successListAdapter = new SimpleAdapter(activity, successList, R.layout.qos_category_test_desc_item, new String[] {
            "name"}, new int[] { R.id.name });
        	
        	for (int i = 0; i < successList.size(); i++) {
        		View v = successListAdapter.getView(i, null, null);
        		successListView.addView(v);
        	}
        	
        	successListView.invalidate();
        }
        
        //hide success results if success list is empty or failure list is not empty and "on_failure_behaviour" is set to "hide_success"
        if (successList.size() == 0 || 
        		(failureList.size() > 0 && QoSServerResult.ON_FAILURE_BEHAVIOUR_HIDE_SUCCESS.equals(result.getOnFailureBehaviour()))) {
        	View group = view.findViewById(R.id.result_text_layout_success);
        	if (group != null) {
        		group.setVisibility(View.GONE);
        	}
        	else {
            	successListView.setVisibility(View.GONE);
        	}
        }

    	LinearLayout failureListView = (LinearLayout) view.findViewById(R.id.value_list_fail);
    	
        if (failureList.size() > 0) {
        	if (QoSServerResult.ON_FAILURE_BEHAVIOUR_INFO_SUCCESS.equals(result.getOnFailureBehaviour())) {
        		View group = view.findViewById(R.id.result_text_layout_success);
            	if (group != null) {
            		group.setBackgroundResource(R.color.result_info);
            	}
            	
            	TextView textSuccess = (TextView) view.findViewById(R.id.subheader_text_success);
            	if (textSuccess != null) {
            		textSuccess.setText(R.string.result_details_qos_info);
            	}
        	}
        	
        	failureListAdapter = new SimpleAdapter(activity, failureList, R.layout.qos_category_test_desc_item, new String[] {
            "name"}, new int[] { R.id.name });
        	
        	for (int i = 0; i < failureList.size(); i++) {
        		View v = failureListAdapter.getView(i, null, null);
        		failureListView.addView(v);
        	}
        	
        	failureListView.invalidate();
        }
        else {
        	View group = view.findViewById(R.id.result_text_layout_fail);
        	if (group != null) {
        		group.setVisibility(View.GONE);
        	}
        	else {
            	failureListView.setVisibility(View.GONE);
        	}
        }
        
        LinearLayout headerList = (LinearLayout) view.findViewById(R.id.value_list_header);
        View headerView = inflater.inflate(R.layout.qos_category_test_desc_item, container, false);
        TextView header = (TextView) headerView.findViewById(R.id.name);
        header.setText(result.getTestSummary());
        headerList.addView(headerView);

        LinearLayout infoList = (LinearLayout) view.findViewById(R.id.value_list_test);
        View infoView = inflater.inflate(R.layout.qos_category_test_desc_item, container, false);
        TextView info = (TextView) infoView.findViewById(R.id.name);
        info.setText(result.getTestDescription());
        infoList.addView(infoView);

        return view;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		super.onRestoreInstanceState(state);
		
		if (state != null) {
			curTestResult = QoSTestResultEnum.values()[((Bundle) state).getInt(OPTION_TEST_CATEGORY, 0)];
			if (((Bundle) state).containsKey(BUNDLE_QOS_RESULT)) {
				result = (QoSServerResult) ((Bundle) state).getSerializable(BUNDLE_QOS_RESULT);
				descList = (List<QoSServerResultDesc>) ((Bundle) state).getSerializable(BUNDLE_QOS_DESC_LIST);
			}			
		}
	}
	
	@Override
	protected Parcelable onSaveInstanceState() {
		Bundle outState = new Bundle();
		outState.putSerializable(BUNDLE_QOS_DESC_LIST, (Serializable) descList);
		outState.putSerializable(BUNDLE_QOS_RESULT, (Serializable) result);
		outState.putInt(OPTION_TEST_CATEGORY, curTestResult.ordinal());
		
		return outState;
	}
	
	public void setResult(QoSServerResult result) {
		this.result = result;
	}
	
	public void setResultDescList(List<QoSServerResultDesc> list) {
		this.descList = list;
	}
}
