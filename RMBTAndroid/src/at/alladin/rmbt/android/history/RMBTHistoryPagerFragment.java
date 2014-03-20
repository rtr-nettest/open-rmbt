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
package at.alladin.rmbt.android.history;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import at.alladin.openrmbt.android.R;
import at.alladin.rmbt.android.main.RMBTMainActivity;
import at.alladin.rmbt.android.test.RMBTService;
import at.alladin.rmbt.android.test.RMBTService.RMBTBinder;

public class RMBTHistoryPagerFragment extends Fragment implements ServiceConnection
{
    
    public static final String ARG_POS = "pos";
    public static final String ARG_ITEMS = "items";
    public static final String ARG_RESULT_AFTER_TEST = "result_after_test";
    
    // When requested, this adapter returns a DemoObjectFragment,
    // representing an object in the collection.
    private RMBTHistoryPagerAdapter pagerAdapter;
    private ViewPager viewPager;
    
    private Handler handler;
    private boolean resultAfterTest;
    
    @Override
    public void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        handler = new Handler();
        
        final Bundle args = getArguments();
        
        @SuppressWarnings("unchecked")
        final ArrayList<HashMap<String, String>> itemList = (ArrayList<HashMap<String, String>>) args
                .getSerializable(ARG_ITEMS);
        resultAfterTest = args.getBoolean(ARG_RESULT_AFTER_TEST, false);
        final int pos = args.getInt(ARG_POS);
        pagerAdapter = new RMBTHistoryPagerAdapter((RMBTMainActivity)getActivity(), itemList, resultAfterTest ? pos : -1, handler);
    }
    
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState)
    {
        
        super.onCreateView(inflater, container, savedInstanceState);
        
        // ViewPager and its adapters use support library
        // fragments, so use getSupportFragmentManager.
        
        viewPager = (ViewPager) inflater.inflate(R.layout.history_pager, container, false);
        viewPager.setAdapter(pagerAdapter);
        
        viewPager.setCurrentItem(getArguments().getInt(ARG_POS));
        
        return viewPager;
    }
    
    @Override
    public void onStart()
    {
        super.onStart();
        
        final Intent serviceIntent = new Intent(getActivity(), RMBTService.class);
        getActivity().bindService(serviceIntent, this, Context.BIND_AUTO_CREATE);
    }
    
    @Override
    public void onStop()
    {
        super.onStop();
        getActivity().unbindService(this);
    }
    
    @Override
    public void onPause()
    {
        super.onPause();
        pagerAdapter.onPause();
    }
    
    @Override
    public void onResume()
    {
        super.onResume();
        pagerAdapter.onResume();
    }
    
    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        pagerAdapter.destroy();
    }
    
    public void setCurrentPosition(final int pos)
    {
        viewPager.setCurrentItem(pos);
    }
    
    @Override
    public void onServiceConnected(final ComponentName component, final IBinder service)
    {
        final RMBTBinder binder = (RMBTBinder) service;
        pagerAdapter.setService(binder.getService());
    }
    
    @Override
    public void onServiceDisconnected(final ComponentName arg0)
    {
        pagerAdapter.removeService();
    }
    
    public boolean onBackPressed()
    {
        if (pagerAdapter == null)
            return false;
        return pagerAdapter.onBackPressed();
    }
}
