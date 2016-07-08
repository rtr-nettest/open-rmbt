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

import java.io.Serializable;
import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import at.alladin.openrmbt.android.R;
import at.alladin.rmbt.android.adapter.result.QoSTestDetailPagerAdapter;
import at.alladin.rmbt.android.main.ExtendedViewPager;
import at.alladin.rmbt.android.main.RMBTMainActivity;
import at.alladin.rmbt.client.v2.task.result.QoSServerResult;
import at.alladin.rmbt.client.v2.task.result.QoSServerResultDesc;

public class QoSTestDetailPagerFragment extends Fragment implements OnPageChangeListener, OnTabChangeListener {
    
	public final static String BUNDLE_QOS_RESULT_LIST = "result_list";
	
	public final static String BUNDLE_QOS_DESC_LIST = "desc_list";
	
	public final static String OPTIONS_PAGE_INDEX = "page_index";
	
    private List<QoSServerResult> resultList;
	private List<QoSServerResultDesc> descList;
	
    private QoSTestDetailPagerAdapter pagerAdapter;
    private ExtendedViewPager viewPager;
    
    private int initPageIndex;
	private TabHost tabHost;
	private HorizontalScrollView scroller;
	
    @SuppressWarnings("unchecked")
	@Override
    public void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        //final Bundle args = getArguments();
        if (savedInstanceState != null) {
        	initPageIndex = savedInstanceState.getInt(OPTIONS_PAGE_INDEX, 0);
        	if (savedInstanceState.containsKey(BUNDLE_QOS_RESULT_LIST)) {
        		setQoSResultList((List<QoSServerResult>) ((Bundle)savedInstanceState).getSerializable(BUNDLE_QOS_RESULT_LIST));
        		setQoSDescList((List<QoSServerResultDesc>) ((Bundle)savedInstanceState).getSerializable(BUNDLE_QOS_DESC_LIST));
        	}
        }
        //DetailType detailType = DetailType.valueOf(args.getString(ARG_DETAIL_TYPE));
        pagerAdapter = new QoSTestDetailPagerAdapter((RMBTMainActivity) getActivity(), resultList, descList);
    }
    
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState)
    {
        super.onCreateView(inflater, container, savedInstanceState);
        
        View v = inflater.inflate(R.layout.result_tabhost_pager, container, false);
    	tabHost = (TabHost) v.findViewById(android.R.id.tabhost);
    	tabHost.setup();
    	tabHost.setOnTabChangedListener(this);
    	
    	for (int i = 0; i < pagerAdapter.getCount(); i++) {
    		TabSpec tab = tabHost.newTabSpec(String.valueOf(i));
    		//tab.setIndicator(getActivity().getResources().getStringArray(R.array.result_page_title)[i]);
    		tab.setContent(android.R.id.tabcontent);
    		    		    		
    		View indicator = inflater.inflate(R.layout.tabhost_indicator, null);
    		TextView title = (TextView) indicator.findViewById(android.R.id.title);
    		title.setText(pagerAdapter.getPageTitle(i));
    		tab.setIndicator(indicator);
    		tabHost.addTab(tab);
    	}

        viewPager = (ExtendedViewPager) v.findViewById(R.id.pager);
        viewPager.setAdapter(pagerAdapter);
        
        viewPager.setOnPageChangeListener(this);
        setCurrentPosition(0);

    	scroller = (HorizontalScrollView) v.findViewById(R.id.tabwidget_scrollview);
       	viewPager.setCurrentItem(initPageIndex);
    	
        return v;
    }
    
    public void setQoSResultList(List<QoSServerResult> resultList) {
    	this.resultList = resultList;
    }

    public void setQoSDescList(List<QoSServerResultDesc> descList) {
    	this.descList = descList;
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
    }
    
    public void setInitPosition(final int pos) {
    	this.initPageIndex = pos;
    }
    
    public void setCurrentPosition(final int pos)
    {
        viewPager.setCurrentItem(pos);
    }
    
    public boolean onBackPressed()
    {
        if (pagerAdapter == null)
            return false;
        return pagerAdapter.onBackPressed();
    }

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable(BUNDLE_QOS_RESULT_LIST, (Serializable) resultList);
		outState.putSerializable(BUNDLE_QOS_DESC_LIST, (Serializable) descList);
	}

	@Override
	public void onTabChanged(String tabId) {
		int tabIndex = Integer.valueOf(tabId);
		if (viewPager != null && (tabIndex != viewPager.getCurrentItem())) {
			viewPager.setCurrentItem(tabIndex);
			scrollToTabTab(tabIndex);
		}		
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onPageSelected(int arg0) {
		tabHost.setCurrentTab(arg0);
		scrollToTabTab(arg0);
	}
	
	private void scrollToTabTab(int scrollToPosition) {
		if (scroller != null  && tabHost != null && tabHost.getTabWidget() != null) {
			int startX = (scroller.getWidth() / 2);
			scroller.scrollTo(tabHost.getTabWidget().getChildAt(0).getWidth() * scrollToPosition - startX, 0);	
		}
	}
}
