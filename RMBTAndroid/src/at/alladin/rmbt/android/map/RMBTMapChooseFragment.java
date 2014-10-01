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
package at.alladin.rmbt.android.map;

import java.util.List;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import at.alladin.openrmbt.android.R;
import at.alladin.rmbt.android.main.RMBTMainActivity;
import at.alladin.rmbt.android.util.SectionListAdapter;

/**
 * 
 * @author bp
 * 
 */
public class RMBTMapChooseFragment extends Fragment implements OnItemClickListener
{
    
    // private static final String DEBUG_TAG = "RMBTMapChooseFragment";
    
    /**
	 * 
	 */
    private List<MapListSection> mapTypeListSectionList;
    
    private Runnable recheckRunnable;
    private Handler handler;
    private View view;
    
    /**
	 * 
	 */
    @Override
    public void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        handler = new Handler();
    }
    
    /**
	 * 
	 */
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState)
    {
        view = inflater.inflate(R.layout.map_choose, container, false);
        
        final ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        
        progressBar.setVisibility(View.VISIBLE);
        
        // /
        
        recheckRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                final RMBTMainActivity activity = (RMBTMainActivity) getActivity();
                mapTypeListSectionList = activity.getMapTypeListSectionList();
                if (mapTypeListSectionList == null)
                {
                    activity.fetchMapOptions();
                    handler.postDelayed(recheckRunnable, 500);
                }
                else
                {
                    progressBar.setVisibility(View.GONE);
                    populateList();
                }
            }
        };
        recheckRunnable.run();
        
//        final TextView infoTextView = (TextView) rl.findViewById(R.id.infoText);
//        infoTextView.setText(getString(R.string.error_no_data));
            
        return view;
    }
    
    protected void populateList()
    {
        final SectionListAdapter sectionListAdapter = new SectionListAdapter(getActivity(),
                R.layout.preferences_category);
        
        for (final MapListSection mapListSection : mapTypeListSectionList)
            sectionListAdapter.addSection(mapListSection.getTitle(), new MapListSectionAdapter(getActivity(),
                    mapListSection));
        
        // /
        
        final ListView listView = (ListView) view.findViewById(R.id.valueList);
        
        listView.setAdapter(sectionListAdapter);
        listView.setItemChecked(sectionListAdapter.indexOf(((RMBTMainActivity) getActivity()).getCurrentMapType()), true);
        listView.setOnItemClickListener(this);
    }
    
    @Override
    public void onDestroy()
    {
        super.onDestroy();
        if (recheckRunnable != null)
            handler.removeCallbacks(recheckRunnable);
    }
    
    /**
	 * 
	 */
    @Override
    public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id)
    {
        
        final MapListEntry entry = (MapListEntry) ((SectionListAdapter) parent.getAdapter()).getItem(position);
        
        ((RMBTMainActivity) getActivity()).setCurrentMapType(entry);
    }
    
    /**
     * 
     * @author bp
     * 
     */
    private class MapListSectionAdapter extends BaseAdapter
    {
        
        /**
		 * 
		 */
        private final MapListSection mapListSection;
        
        /**
		 * 
		 */
        private final Context context;
        
        /**
         * 
         * @param mapListSection
         */
        public MapListSectionAdapter(final Context context, final MapListSection mapListSection)
        {
            this.context = context;
            this.mapListSection = mapListSection;
        }
        
        /**
		 * 
		 */
        @Override
        public int getCount()
        {
            return mapListSection.getMapListEntryList().size();
        }
        
        /**
		 * 
		 */
        @Override
        public Object getItem(final int position)
        {
            return mapListSection.getMapListEntryList().get(position);
        }
        
        /**
		 * 
		 */
        @Override
        public long getItemId(final int position)
        {
            return position;
        }
        
        /**
		 * 
		 */
        @Override
        public View getView(final int position, View convertView, final ViewGroup parent)
        {
            
            if (convertView == null)
            {
                
                final LayoutInflater inflater = ((Activity) context).getLayoutInflater();
                
                convertView = inflater.inflate(R.layout.map_choose_item, parent, false);
            }
            
            final TextView titleTextView = (TextView) convertView.findViewById(android.R.id.title);
            final TextView summaryTextView = (TextView) convertView.findViewById(android.R.id.summary);
            
            final MapListEntry entry = (MapListEntry) getItem(position);
            
            titleTextView.setText(entry.getTitle());
            summaryTextView.setText(entry.getSummary());
            
            return convertView;
        }
    }
}
