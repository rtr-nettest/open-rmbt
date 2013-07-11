/*******************************************************************************
 * Copyright 2013 alladin-IT OG
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
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import at.alladin.openrmbt.android.R;
import at.alladin.rmbt.android.main.RMBTMainActivity;
import at.alladin.rmbt.android.util.SectionListAdapter;

/**
 * 
 * @author bp
 * 
 */
public class RMBTMapFilterFragment extends Fragment implements OnItemClickListener
{
    
    // private static final String DEBUG_TAG = "RMBTMapFilterFragment";
    
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
        
        view = inflater.inflate(R.layout.map_filter, container, false);
        
        
        final ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        
        progressBar.setVisibility(View.VISIBLE);
        
        // /
        
        recheckRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                final RMBTMainActivity activity = getRMBTMainActivity();
                if (activity.getMapFilterListSelectionList() == null)
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
        // /
        
//        final TextView infoTextView = (TextView) rl.findViewById(R.id.infoText);
//        infoTextView.setText(getString(R.string.error_no_data));
            
        // /
        
        
        return view;
    }
    
    @Override
    public void onDestroy()
    {
        super.onDestroy();
        if (recheckRunnable != null)
            handler.removeCallbacks(recheckRunnable);
    }
    
    private RMBTMainActivity getRMBTMainActivity()
    {
        return (RMBTMainActivity) getActivity();
    }
    
    protected void populateList()
    {
        final RMBTMainActivity activity = getRMBTMainActivity();
        final SectionListAdapter sectionListAdapter = new SectionListAdapter(activity,
                R.layout.preferences_category);
        
        // add filter options
        
        for (final MapListSection mapListSection : activity.getMapFilterListSelectionList())
            sectionListAdapter.addSection(mapListSection.getTitle(), new MapListSectionAdapter(activity,
                    mapListSection));
        
        // /
        
        final ListView listView = (ListView) view.findViewById(R.id.valueList);
        
        listView.setAdapter(sectionListAdapter);
        
        listView.setItemsCanFocus(true);
        listView.setOnItemClickListener(this);
    }

    /**
	 * 
	 */
    @Override
    public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id)
    {
        
        final SectionListAdapter sectionListAdapter = (SectionListAdapter) parent.getAdapter();
        
        final MapListSectionAdapter mapListSectionAdapter = (MapListSectionAdapter) sectionListAdapter
                .getAdapter(position);
        
        // /
        
        final MapListEntry clickedEntry = (MapListEntry) sectionListAdapter.getItem(position);
        
        final List<MapListEntry> mapListEntryList = mapListSectionAdapter.mapListSection.getMapListEntryList();
        
        // uncheck all entries in section:
        for (final MapListEntry mapListEntry : mapListEntryList)
            mapListEntry.setChecked(false);
        
        // recheck checked entry:
        clickedEntry.setChecked(true);
        
        // handle sat differently
        final String value = clickedEntry.getValue();
        final RMBTMainActivity activity = getRMBTMainActivity();
        if (MapProperties.MAP_SAT_KEY.equals(clickedEntry.getKey()))
            activity.setMapTypeSatellite(MapProperties.MAP_SAT_VALUE.equals(value));
        else if (MapProperties.MAP_OVERLAY_KEY.equals(clickedEntry.getKey()))
        {
            if (MapProperties.MAP_AUTO_VALUE.equals(value))
                activity.setMapOverlayType(MapProperties.MAP_OVERLAY_TYPE_AUTO);
            else if (MapProperties.MAP_HEATMAP_VALUE.equals(value))
                activity.setMapOverlayType(MapProperties.MAP_OVERLAY_TYPE_HEATMAP);
            else if (MapProperties.MAP_POINTS_VALUE.equals(value))
                activity.setMapOverlayType(MapProperties.MAP_OVERLAY_TYPE_POINTS);
        }
        else
            // set new filter options:
            activity.updateMapFilter();
        
        // reload list view:
        ((SectionListAdapter) parent.getAdapter()).notifyDataSetChanged();
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
                
                convertView = inflater.inflate(R.layout.map_filter_item, parent, false);
            }
            
            final TextView titleTextView = (TextView) convertView.findViewById(android.R.id.title);
            final TextView summaryTextView = (TextView) convertView.findViewById(android.R.id.summary);
            
            final RadioButton checkedTextView = (RadioButton) convertView.findViewById(R.id.radiobutton);
            
            // /
            
            final MapListEntry entry = (MapListEntry) getItem(position);
            
            // /
            
            titleTextView.setText(entry.getTitle());
            summaryTextView.setText(entry.getSummary());
            
            checkedTextView.setChecked(entry.isChecked());
            
            return convertView;
        }
    }
}
