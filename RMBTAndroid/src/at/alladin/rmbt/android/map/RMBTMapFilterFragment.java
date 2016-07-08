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
package at.alladin.rmbt.android.map;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
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
import at.alladin.rmbt.util.model.option.ServerOption;
import at.alladin.rmbt.util.model.option.ServerOptionContainer;

/**
 * 
 * @author bp
 * 
 */
public class RMBTMapFilterFragment extends Fragment implements OnItemClickListener, OnKeyListener
{
    
    // private static final String DEBUG_TAG = "RMBTMapFilterFragment";
    
    private Runnable recheckRunnable;
    private Handler handler;
    private View view;
    private ServerOptionContainer options;
    private boolean isRoot = true;
    
    private List<ServerOption> currentOptionList;
    
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
                if (activity.getMapOptions() == null)
                {
                    activity.fetchMapOptions();
                    handler.postDelayed(recheckRunnable, 500);
                }
                else
                {
                    progressBar.setVisibility(View.GONE);
                    options = activity.getMapOptions();
                    populateList(options.getRootOptions(), true);
                }
            }
        };
        recheckRunnable.run();
        
        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener(this);
        
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
    
    protected void populateList(List<ServerOption> optionList, boolean isRoot)
    {
    	this.isRoot = isRoot;
    	this.currentOptionList = optionList;
        final RMBTMainActivity activity = getRMBTMainActivity();
        final SectionListAdapter sectionListAdapter = new SectionListAdapter(activity, R.layout.preferences_category, !isRoot);
     
        // add filter options
        if (optionList != null && optionList.size() > 0) {
	        for (final ServerOption option : optionList) {
	        	if (option.isEnabled()) {
	        		if (option.getOptionList() != null && option.getOptionList().size() > 0 && !options.isAnyChildSelected(option)) {
	        			options.select(option.getOptionList().get(0));
	        		}
	        		sectionListAdapter.addSection(option.getTitle(), new MapListSectionAdapter(activity, option, isRoot));
	        	}
	        }
        }

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
        
        final Object item = sectionListAdapter.getItem(position);
        
        if (options.select((ServerOption) item) != null) {
        	final List<ServerOption> newOptionList = new ArrayList<ServerOption>();
        	newOptionList.add((ServerOption) item);
        	populateList(newOptionList, false);
        }
        else if (((ServerOption) item).getParent() != null) {
        	populateList(options.getRootOptions(), true);
        }
                
        ((SectionListAdapter) parent.getAdapter()).notifyDataSetChanged();
        ((RMBTMainActivity) getActivity()).updateMapFilter(null);
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
        private final ServerOption option;
        
        /**
		 * 
		 */
        private final Context context;
        
        /**
         * 
         */
        private final boolean isRootElement;
        
        /**
         * 
         * @param mapListSection
         */
        public MapListSectionAdapter(final Context context, final ServerOption option, final boolean isRootElement)
        {
            this.context = context;
            this.option = option;
            this.isRootElement = isRootElement;
        }
        
        /**
		 * 
		 */
        @Override
        public int getCount()
        {
        	if (option.isEnabled() && isRootElement) {
        		return 1;
        	}
        	else if (option.isEnabled() && option.getEnabledOptionList() != null) {
       			return option.getEnabledOptionList().size();
        	}
            return 0;
        }
        
        /**
		 * 
		 */
        @Override
        public Object getItem(final int position)
        {
        	if (isRootElement) {
        		return option;
        	}
       		return (option.getEnabledOptionList().get(position));
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
            final TextView subselectTextView = (TextView) convertView.findViewById(R.id.subselect);
            
            final RadioButton checkedTextView = (RadioButton) convertView.findViewById(R.id.radiobutton);
            
            // /
            
            final ServerOption entry = (ServerOption) getItem(position);
            
            if (isRootElement) {
            	checkedTextView.setVisibility(View.GONE);
            }
            
            String subOptions = "";
            if (entry.isChecked()) {
	            for (ServerOption o : options.getSelectedSubOptions(entry)) {
	            	subOptions = o.getTitle() + (subOptions.length() > 0 ? ", " : "") + subOptions ; 
	            }
            }
            
            // /
            
            subselectTextView.setText(subOptions);
            titleTextView.setText(entry.getTitle());
            summaryTextView.setText(entry.getSummary());
            checkedTextView.setChecked(entry.isChecked());
            
            return convertView;
        }
    }

	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
			if (currentOptionList != null && !isRoot) {
				populateList(options.getRootOptions(), true);
				return true;
			}
		}
		return false;
	}
}
