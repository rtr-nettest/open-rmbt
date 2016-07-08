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
package at.alladin.rmbt.android.fragments.history;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import at.alladin.openrmbt.android.R;
import at.alladin.rmbt.android.main.RMBTMainActivity;
import at.alladin.rmbt.android.main.RMBTMainActivity.HistoryUpdatedCallback;

public class RMBTHistoryFragment extends Fragment implements HistoryUpdatedCallback
{
    
    private static final String DEBUG_TAG = "RMBTHistoryFragment";
    
    private ListAdapter historyList;
    
    private RMBTMainActivity activity;
    
    private View view;
    
    private ListView listView;
    
    private int listViewIdx;
    private int listViewTop;
    
    private ProgressBar progessBar;
    
    private TextView emptyView;
    
    @Override
    public void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        activity = (RMBTMainActivity) getActivity();
        
        // devicesToShow = activity.getHistoryFilterDevicesFilter();
        // networksToShow = activity.getHistoryFilterNetworksFilter();
        //
        // if (devicesToShow == null && networksToShow == null) {
        // devicesToShow = new ArrayList<String>();
        // networksToShow = new ArrayList<String>();
        // }
        //
        // Log.i(DEBUG_TAG, devicesToShow.toString());
    }
    
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState)
    {
        
        super.onCreateView(inflater, container, savedInstanceState);
        
        if (savedInstanceState != null)
        {
            listViewIdx = savedInstanceState.getInt("listViewIdx", listViewIdx);
            listViewTop = savedInstanceState.getInt("listViewTop", listViewTop);
            Log.d(DEBUG_TAG, "loaded: idx:" + listViewIdx + " top:" + listViewTop);
        }
        
        view = inflater.inflate(R.layout.history, container, false);
        
        listView = (ListView) view.findViewById(R.id.historyList);
        emptyView = (TextView) view.findViewById(R.id.infoText);
        progessBar = (ProgressBar) view.findViewById(R.id.progressBar);
        
        listView.setVisibility(View.GONE);
        emptyView.setVisibility(View.GONE);
        progessBar.setVisibility(View.VISIBLE);
        
        /*
         * if ((historyTask == null || (historyTask != null ||
         * historyTask.isCancelled()))) { historyTask = new
         * CheckHistoryTask(activity);
         * 
         * historyTask.setEndTaskListener(this); historyTask.execute(); }
         */
        
        // listView.setEmptyView(emptyView);
        
        listView.setOnItemClickListener(new OnItemClickListener()
        {
            @Override
            public void onItemClick(final AdapterView<?> l, final View v, final int position, final long id)
            {
                activity.showHistoryPager(position);
            }
            
        });
        
        return view;
    }
    
    private void saveListViewState()
    {
        listViewIdx = listView.getFirstVisiblePosition();
        final View v = listView.getChildAt(0);
        listViewTop = v == null ? 0 : v.getTop();
    }
    
    @Override
    public void onSaveInstanceState(final Bundle outState)
    {
        super.onSaveInstanceState(outState);
        if (listView != null)
        {
            saveListViewState();
            outState.putInt("listViewIdx", listViewIdx);
            outState.putInt("listViewTop", listViewTop);
            Log.d(DEBUG_TAG, "saved: idx:" + listViewIdx + " top:" + listViewTop);
        }
    }
    
    @Override
    public void onStart()
    {
        super.onStart();
        
        activity.updateHistory(this);
    }
    
    @Override
    public void onStop()
    {
        super.onStop();
        saveListViewState();
    }
    
    @Override
    public void historyUpdated(final int status)
    {
        if (!isVisible())
            return;
        switch (status)
        {
        case SUCCESSFUL:
            historyList = new SimpleAdapter(getActivity(), activity.getHistoryItemList(), R.layout.history_item,
                    new String[] { "device", "type", "date", "down", "up", "ping" }, new int[] { R.id.device,
                            R.id.type, R.id.date, R.id.down, R.id.up, R.id.ping });
            
            listView.setAdapter(historyList);
            
            listView.setSelectionFromTop(listViewIdx, listViewTop);
            Log.d(DEBUG_TAG, "set: idx:" + listViewIdx + " top:" + listViewTop);
            
            listView.invalidate();
            
            progessBar.setVisibility(View.GONE);
            emptyView.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
            break;
            
        case LIST_EMPTY:
            Log.i(DEBUG_TAG, "LEERE LISTE");
            progessBar.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
            emptyView.setText(getString(R.string.error_no_data));
            emptyView.invalidate();
            break;
            
        case ERROR:
            Log.i(DEBUG_TAG, "FEHLER");
            progessBar.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
            emptyView.setText(getString(R.string.error_history_no_data_no_connection));
            emptyView.invalidate();
            break;
        }
    }
}
