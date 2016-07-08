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

import java.util.concurrent.atomic.AtomicInteger;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
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
import at.alladin.rmbt.android.adapter.result.OnCompleteListener;
import at.alladin.rmbt.android.adapter.result.OnDataChangedListener;
import at.alladin.rmbt.android.adapter.result.RMBTResultPagerAdapter;
import at.alladin.rmbt.android.main.ExtendedViewPager;
import at.alladin.rmbt.android.main.RMBTMainActivity;


public class RMBTResultPagerFragment extends Fragment implements OnTabChangeListener, OnPageChangeListener
{
	/**
	 * use this flag to make the map indicator visible only if coordinates are available
	 */
	public final static boolean MAP_INDICATOR_DYNAMIC_VISIBILITY = false;
    
    public static final String ARG_TEST_UUID = "test_uuid";
    
    private RMBTResultPagerAdapter pagerAdapter;
    private ExtendedViewPager viewPager;
    private TabHost tabHost;
    private HorizontalScrollView scroller;
    
    private Handler handler = new Handler();
    
    final AtomicInteger counter = new AtomicInteger(0);
    
    @Override
    public void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        
        final Bundle args = getArguments();
        
        final String uuid = args.getString(ARG_TEST_UUID);
        System.out.println("ResultPagerFragment: test uuid: " + uuid);

        pagerAdapter = new RMBTResultPagerAdapter((RMBTMainActivity) getActivity(), handler, uuid);
        pagerAdapter.setOnCompleteListener(new OnCompleteListener() {
			
			@Override
			public void onComplete(int flag, Object object) {
				if (pagerAdapter.getCount() > tabHost.getTabWidget().getChildCount()) {
					if (getActivity() != null) {
						LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
						
						for (int i = tabHost.getTabWidget().getChildCount(); i < pagerAdapter.getCount(); i++) {
				    		TabSpec tab = tabHost.newTabSpec(String.valueOf(i));
				    		//tab.setIndicator(getActivity().getResources().getStringArray(R.array.result_page_title)[i]);
				    		tab.setContent(android.R.id.tabcontent);
				    		    		
				    		
				    		View indicator = inflater.inflate(R.layout.tabhost_indicator, null);
				    		TextView title = (TextView) indicator.findViewById(android.R.id.title);
				    		title.setText(getActivity().getResources()
				    				.getStringArray(R.array.result_page_title)[RMBTResultPagerAdapter.RESULT_PAGE_TAB_TITLE_MAP.get(i)]);
				    		
				    		tab.setIndicator(indicator);
				    		
				    		tabHost.addTab(tab);						
						}
					}
				}
			}
		});
        
        if (MAP_INDICATOR_DYNAMIC_VISIBILITY) {
	        pagerAdapter.setOnDataChangedListener(new OnDataChangedListener() {
				
				@Override
				public void onChange(Object oldValue, Object newValue, Object flag) {
					if (flag.equals("HAS_MAP")) {
						boolean b = (Boolean) newValue;
						if (b) {
							tabHost.getTabWidget().getChildTabViewAt(RMBTResultPagerAdapter.RESULT_PAGE_MAP).setVisibility(View.VISIBLE);
						}
						else {
							tabHost.getTabWidget().getChildTabViewAt(RMBTResultPagerAdapter.RESULT_PAGE_MAP).setVisibility(View.GONE);
						}
					}
				}
			});
        }
    }
    
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState)
    {
    	pagerAdapter.setActivity((RMBTMainActivity) getActivity());
        View v = inflater.inflate(R.layout.result_tabhost_pager, container, false);
        return createView(v, inflater, RMBTResultPagerAdapter.RESULT_PAGE_MAIN_MENU);
    }
    
    private View createView(View v, LayoutInflater inflater, int currentPage) {
    	tabHost = (TabHost) v.findViewById(android.R.id.tabhost);
    	tabHost.setup();
    	tabHost.setOnTabChangedListener(this);
    	
    	for (int i = 0; i < pagerAdapter.getCount(); i++) {
    		TabSpec tab = tabHost.newTabSpec(String.valueOf(i));
    		//tab.setIndicator(getActivity().getResources().getStringArray(R.array.result_page_title)[i]);
    		tab.setContent(android.R.id.tabcontent);
    		    		
    		
    		View indicator = inflater.inflate(R.layout.tabhost_indicator, null);
    		TextView title = (TextView) indicator.findViewById(android.R.id.title);
    		title.setText(getActivity().getResources().getStringArray(R.array.result_page_title)[RMBTResultPagerAdapter.RESULT_PAGE_TAB_TITLE_MAP.get(i)]);
    		
    		if (MAP_INDICATOR_DYNAMIC_VISIBILITY) {
    			if (i == RMBTResultPagerAdapter.RESULT_PAGE_MAP) {
    				indicator.setVisibility(View.GONE);
    			}
    		}
    		tab.setIndicator(indicator);
    		
    		tabHost.addTab(tab);
    	}
    	
    	scroller = (HorizontalScrollView) v.findViewById(R.id.tabwidget_scrollview);
    	
        viewPager = (ExtendedViewPager) v.findViewById(R.id.pager);
        viewPager.setAdapter(pagerAdapter);
        
        viewPager.setOnPageChangeListener(this);
        setCurrentPosition(currentPage);

        return v;
    }
    
	@Override
	public void onTabChanged(String tabId) {
		int tabIndex = Integer.valueOf(tabId);
		if (viewPager != null && (tabIndex != viewPager.getCurrentItem())) {
			scrollToTabTab(tabIndex);
			viewPager.setCurrentItem(tabIndex);
		}
	}
    
    @Override
    public void onPause()
    {
        super.onPause();
        pagerAdapter.onPause();
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
    	super.onViewCreated(view, savedInstanceState);
    	setActionBarItems();
    }
    
    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        pagerAdapter.destroy();
    }
    
    public void setCurrentPosition(final int pos)
    {
    	//tabHost.setCurrentTab(pos);
        viewPager.setCurrentItem(pos);
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
	public void onPageSelected(int index) {
		tabHost.setCurrentTab(index);
		scrollToTabTab(index);
		setActionBarItems();
	}
	
	private void scrollToTabTab(int scrollToPosition) {
		if (scroller != null  && tabHost != null && tabHost.getTabWidget() != null) {
			int startX = (scroller.getWidth() / 2);
			scroller.scrollTo(tabHost.getTabWidget().getChildAt(0).getWidth() * scrollToPosition - startX, 0);	
		}
	}	
	
    public TabHost getTabHost() {
    	return tabHost;
    }

    public ViewPager getViewPager() {
    	return viewPager; 
    }
    
    public RMBTResultPagerAdapter getPagerAdapter() {
    	return pagerAdapter; 
    }

    public void setActionBarItems() {
    	System.out.println("SET ACTIONBARI TEMS");
    	if (viewPager != null) {
    		switch (viewPager.getCurrentItem()) {
    		case RMBTResultPagerAdapter.RESULT_PAGE_MAIN_MENU:
    			((RMBTMainActivity) getActivity()).setVisibleMenuItems(R.id.action_menu_help, R.id.action_menu_share);
    			break;
    		case RMBTResultPagerAdapter.RESULT_PAGE_MAP:
    			((RMBTMainActivity) getActivity()).setVisibleMenuItems(R.id.action_menu_map);
    			break;
    		default:
    			((RMBTMainActivity) getActivity()).setVisibleMenuItems();
    			break;
    		}
    	}
    }
    
    public View getChildAt(int position) {
    	if (viewPager != null) {
    		return viewPager.getChildAt(position);
    	}
    	return null;
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
    	super.onConfigurationChanged(newConfig);
    	LayoutInflater inflater = LayoutInflater.from(getActivity());
    	populateViewForOrientation(inflater, (ViewGroup) getView());
    }

    /**
     * 
     * @param inflater
     * @param view
     */
	private void populateViewForOrientation(LayoutInflater inflater, ViewGroup view) {
		int page = getViewPager().getCurrentItem();
		view.removeAllViewsInLayout();
        View v = inflater.inflate(R.layout.result_tabhost_pager, view);
        createView(v, inflater, page);
	}
}