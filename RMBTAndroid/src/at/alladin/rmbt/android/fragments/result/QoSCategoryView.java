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
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import at.alladin.openrmbt.android.R;
import at.alladin.rmbt.android.main.RMBTMainActivity;
import at.alladin.rmbt.client.v2.task.result.QoSServerResult;
import at.alladin.rmbt.client.v2.task.result.QoSServerResultDesc;
import at.alladin.rmbt.client.v2.task.result.QoSServerResultTestDesc;
import at.alladin.rmbt.client.v2.task.result.QoSTestResultEnum;

/**
 * 
 * @author lb
 *
 */
public class QoSCategoryView extends ScrollView implements OnClickListener {
	
	public static final String OPTION_TEST_CATEGORY = "test_category";
	
	public static final String BUNDLE_QOS_RESULT_LIST = "qos_result_list";
	
	public static final String BUNDLE_QOS_DESC_LIST = "qos_desc_list";
	
	public static final String BUNDLE_QOS_TEST_DESC = "qos_test_desc";
	
	QoSTestResultEnum curTestResult;
	
	private QoSServerResultTestDesc testDesc;
	
	private List<QoSServerResult> resultList;
	
    private List<QoSServerResultDesc> descList;

	private SimpleAdapter valueList;
	
	private RMBTMainActivity activity;
	
	public QoSCategoryView(Context context, RMBTMainActivity activity, QoSServerResultTestDesc testDesc, List<QoSServerResult> resultList, List<QoSServerResultDesc> descList) {
		super(context);
		
		this.activity = activity;
		this.testDesc = testDesc;
		this.resultList = resultList;
		this.descList = descList;
		setFillViewport(true);
		final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		createView(inflater, this);
	}

	@SuppressWarnings("unchecked")
	public View createView(LayoutInflater inflater, ViewGroup container) {
        View view = null;
               
        view = inflater.inflate(R.layout.qos_category_view, this);

        ViewGroup listView = (ViewGroup) view.findViewById(R.id.valueList);
        listView.setVisibility(View.VISIBLE);

        List<HashMap<String, String>> itemList = new ArrayList<HashMap<String, String>>();
        HashMap<String, String> viewItem;

        if (resultList != null) {
        	for (int i = 0; i < resultList.size(); i++) {
        		QoSServerResult result = resultList.get(i);
        		viewItem = new HashMap<String, String>();
        		viewItem.put("name", "Test #" + (i+1)); // + ", uid: " + result.getUid());
        		viewItem.put("uid", String.valueOf(result.getUid()));
        		viewItem.put("desc", result.getTestSummary());
        		viewItem.put("status", String.valueOf(result.getFailureCount()));
        		viewItem.put("index", String.valueOf(i));
        		itemList.add(viewItem);
        	}
        }
        valueList = new SimpleAdapter(activity, itemList, R.layout.qos_category_test_list_item, new String[] {
            "name", "desc"}, new int[] { R.id.name, R.id.description });
    	
        List<View> succeededTests = new ArrayList<View>();

        for (int i = 0; i < valueList.getCount(); i++) {
        	boolean hasFailed = false;
        	View v = valueList.getView(i, null, null);
    		if (Integer.valueOf(((HashMap<String, String>) valueList.getItem(i)).get("status")) > 0) {
        		ImageView img = (ImageView) v.findViewById(R.id.status);
        		img.setImageResource(R.drawable.traffic_lights_red);
        		hasFailed = true;
    		}
    		
    		v.setOnClickListener(this);
    		v.setTag(valueList.getItem(i));
    		if (hasFailed) {
    			listView.addView(v);
    		}
    		else {
    			succeededTests.add(v);
    		}
        }
        
        for (View v : succeededTests) {
        	listView.addView(v);
        }
        
        listView.invalidate();
        
        View infoList = view.findViewById(R.id.result_text_list);
        infoList.setVisibility(View.VISIBLE);
        
        View testInfo = inflater.inflate(R.layout.qos_category_test_desc_item, this, false);
        TextView infoText = (TextView) testInfo.findViewById(R.id.name);
        infoText.setText(testDesc.getDescription());
        ((ViewGroup) infoList).addView(testInfo);
        
        return view;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		super.onRestoreInstanceState(state);
		
		if (state != null) {
			curTestResult = QoSTestResultEnum.values()[((Bundle) state).getInt(OPTION_TEST_CATEGORY, 0)];
			if (((Bundle) state).containsKey(BUNDLE_QOS_RESULT_LIST)) {
				resultList = (List<QoSServerResult>) ((Bundle) state).getSerializable(BUNDLE_QOS_RESULT_LIST);
				descList = (List<QoSServerResultDesc>) ((Bundle) state).getSerializable(BUNDLE_QOS_DESC_LIST);
				testDesc =  (QoSServerResultTestDesc) ((Bundle) state).getSerializable(BUNDLE_QOS_TEST_DESC);
			}
			
		}
	}
	
	@Override
	protected Parcelable onSaveInstanceState() {
		Bundle outState = new Bundle();
		outState.putSerializable(BUNDLE_QOS_DESC_LIST, (Serializable) descList);
		outState.putSerializable(BUNDLE_QOS_RESULT_LIST, (Serializable) resultList);
		outState.putSerializable(BUNDLE_QOS_TEST_DESC, (Serializable) testDesc);
		outState.putInt(OPTION_TEST_CATEGORY, curTestResult.ordinal());
		
		return outState;
	}
	
	public void setResultList(List<QoSServerResult> list) {
		this.resultList = list;
	}
	
	public void setResultDescList(List<QoSServerResultDesc> list) {
		this.descList = list;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onClick(View v) {
		if (v.getTag() != null) {
			final int index = Integer.valueOf(((HashMap<String, String>) v.getTag()).get("index"));
			activity.showQoSTestDetails(resultList, descList, index);
		}
	}
}
