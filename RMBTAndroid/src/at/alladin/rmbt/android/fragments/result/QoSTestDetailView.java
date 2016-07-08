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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

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
	public enum QoSResultListType {
		FAIL (R.string.result_details_qos_failure, R.color.result_red, DetailType.FAIL),
		SUCCESS (R.string.result_details_qos_success, R.color.result_green, DetailType.OK),
		INFO (R.string.result_details_qos_info, R.color.result_info, DetailType.INFO);
		
		final int titleResourceId;
		final int colorResourceId;
		final DetailType detailType;
		
		private QoSResultListType(final int titleResourceId, final int colorResourceId, final DetailType detailType) {
			this.titleResourceId = titleResourceId;
			this.colorResourceId = colorResourceId;
			this.detailType = detailType;
		}

		public int getTitleResourceId() {
			return titleResourceId;
		}

		public int getColorResourceId() {
			return colorResourceId;
		}

		public DetailType getDetailType() {
			return detailType;
		}
	}
	
	public class QoSResultListAdapter {
		private SimpleAdapter listAdapter;
		private final ViewGroup viewGroup;
		private final LinearLayout listView;
		private final List<HashMap<String, String>> descMap = new ArrayList<HashMap<String, String>>();

		public QoSResultListAdapter(final ViewGroup container) {
			this.viewGroup = container;
			listView = (LinearLayout) viewGroup.findViewById(R.id.value_list);
		}
		
		public void build() {
			if (this.listAdapter == null) {
				this.listAdapter =  new SimpleAdapter(activity, descMap, R.layout.qos_category_test_desc_item, new String[] {
						"name"}, new int[] { R.id.name });
			}

        	for (int i = 0; i < descMap.size(); i++) {
        		View v = listAdapter.getView(i, null, null);
        		listView.addView(v);
        	}
        	
        	listView.invalidate();
		}

		public ViewGroup getViewGroup() {
			return viewGroup;
		}

		public LinearLayout getListView() {
			return listView;
		}

		public List<HashMap<String, String>> getDescMap() {
			return descMap;
		}
	}
	
	public static final String OPTION_TEST_CATEGORY = "test_category";
	
	public static final String BUNDLE_QOS_RESULT = "qos_result";
	
	public static final String BUNDLE_QOS_DESC_LIST = "qos_desc_list";
	
	QoSTestResultEnum curTestResult;
	
	private QoSServerResult result;
	
    private List<QoSServerResultDesc> descList;

    private Map<DetailType, QoSResultListAdapter> resultViewList;
    
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

        System.out.println(result);
        
        resultViewList = new TreeMap<QoSServerResult.DetailType, QoSTestDetailView.QoSResultListAdapter>(new Comparator<QoSServerResult.DetailType>() {

			@Override
			public int compare(DetailType lhs, DetailType rhs) {
				// TODO Auto-generated method stub
				if (lhs.ordinal()>rhs.ordinal()) return 1;
				else if (lhs.ordinal()<rhs.ordinal()) return -1;
				return 0;
			}
		});
        
        for (QoSResultListType type : QoSResultListType.values()) {
        	final View v = inflater.inflate(R.layout.qos_test_view_result_list, null);
        	
        	final TextView titleTest = (TextView) v.findViewById(R.id.subheader_text);
        	titleTest.setText(type.getTitleResourceId());
        	
        	v.setBackgroundResource(type.getColorResourceId());
        	
        	resultViewList.put(type.getDetailType(), new QoSResultListAdapter((ViewGroup) v));
        }

        HashMap<String, String> viewItem;
        
        if (descList != null) {
            for (int i = 0; i < descList.size(); i++) {
            	QoSServerResultDesc desc = descList.get(i);
            	if (desc.getUidSet().contains(result.getUid())) {
                	viewItem = new HashMap<String, String>();
                	viewItem.put("name", desc.getDesc());
                	
                	resultViewList.get(desc.getStatus()).getDescMap().add(viewItem);
            	}
            }
        }
        
        final LinearLayout descContainerView = (LinearLayout) view.findViewById(R.id.result_desc_container);
        
        int posIndex = 1;
        for (Entry<DetailType, QoSResultListAdapter> e : resultViewList.entrySet()) {
        	if (e.getValue().getDescMap().size() > 0) {
        		e.getValue().build();
        		
        		descContainerView.addView(e.getValue().getViewGroup(), posIndex++);
        		
        		switch(e.getKey()) {
        		case OK:
        			if (resultViewList.get(DetailType.FAIL) != null && resultViewList.get(DetailType.FAIL).getDescMap().size() > 0) {
	                	if (QoSServerResult.ON_FAILURE_BEHAVIOUR_INFO_SUCCESS.equals(result.getOnFailureBehaviour())) {
                    		e.getValue().getViewGroup().setBackgroundResource(R.color.result_info);	                    	
	                	}
	                	else if(QoSServerResult.ON_FAILURE_BEHAVIOUR_HIDE_SUCCESS.equals(result.getOnFailureBehaviour())) {
	                		e.getValue().getViewGroup().setVisibility(View.GONE);
	                	}
        			}
        		default:
        		}
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
