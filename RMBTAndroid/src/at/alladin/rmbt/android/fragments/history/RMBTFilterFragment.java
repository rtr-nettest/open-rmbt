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

import java.util.ArrayList;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import at.alladin.openrmbt.android.R;
import at.alladin.rmbt.android.main.RMBTMainActivity;
import at.alladin.rmbt.android.util.Helperfunctions;

public class RMBTFilterFragment extends Fragment
{
    
    // private static final String DEBUG_TAG = "RMBTFilterFragment";
    
    private RMBTMainActivity activity;
    
    private View view;
    
    private LinearLayout deviceListView;
    
    private LinearLayout networkListView;
    
    private ArrayList<String> devicesToShow;
    
    private ArrayList<String> networksToShow;
    
    private CheckBox limit25CheckBox;
    
    @Override
    public void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        activity = (RMBTMainActivity) getActivity();
    }
    
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState)
    {
        
        super.onCreateView(inflater, container, savedInstanceState);
        
        view = inflater.inflate(R.layout.history_filter, container, false);
        
        deviceListView = (LinearLayout) view.findViewById(R.id.deviceList);
        networkListView = (LinearLayout) view.findViewById(R.id.networkList);
        
        final RelativeLayout resultLimitView = (RelativeLayout) view.findViewById(R.id.Limit25Wrapper);
        limit25CheckBox = (CheckBox) view.findViewById(R.id.Limit25CheckBox);
        
        if (activity.getHistoryResultLimit() == 250)
            limit25CheckBox.setChecked(true);
        else
            limit25CheckBox.setChecked(false);
        
        resultLimitView.setOnClickListener(new OnClickListener()
        {
            
            @Override
            public void onClick(final View v)
            {
                if (limit25CheckBox.isChecked())
                {
                    limit25CheckBox.setChecked(false);
                    activity.setHistoryResultLimit(0);
                }
                else
                {
                    limit25CheckBox.setChecked(true);
                    activity.setHistoryResultLimit(250);
                }
                
            }
            
        });
        
        devicesToShow = activity.getHistoryFilterDevicesFilter();
        networksToShow = activity.getHistoryFilterNetworksFilter();
        
        if (devicesToShow == null && networksToShow == null)
        {
            devicesToShow = new ArrayList<String>();
            networksToShow = new ArrayList<String>();
        }
        
        final float scale = activity.getResources().getDisplayMetrics().density;
        
        final int leftRightItem = Helperfunctions.dpToPx(5, scale);
        // int topBottomItem = Helperfunctions.dpToPx(5, scale);
        
        // int leftRightDiv = Helperfunctions.dpToPx(0, scale);
        // int topBottomDiv = Helperfunctions.dpToPx(0, scale);
        final int heightDiv = Helperfunctions.dpToPx(1, scale);
        
        // int topBottomImg = Helperfunctions.dpToPx(1, scale);
        
        final String historyDevices[] = activity.getHistoryFilterDevices();
        
        if (historyDevices != null)
        {
            
            for (int i = 0; i < historyDevices.length; i++)
            {
                
                final RelativeLayout singleItemLayout = new RelativeLayout(activity); // (LinearLayout)measurememtItemView.findViewById(R.id.measurement_item);
                
                singleItemLayout.setId(i);
                singleItemLayout.setClickable(true);
                singleItemLayout.setBackgroundResource(R.drawable.list_selector);
                
                singleItemLayout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                        LayoutParams.WRAP_CONTENT));
                
                final TextView itemTitle = new TextView(activity, null, R.style.textMediumLight);
                
                RelativeLayout.LayoutParams layout = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                        LayoutParams.MATCH_PARENT);
                layout.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                layout.addRule(RelativeLayout.CENTER_VERTICAL);
                
                // itemTitle.setLayoutParams(layout);
                itemTitle.setGravity(Gravity.LEFT);
                itemTitle.setPadding(leftRightItem, 0, leftRightItem, 0);
                itemTitle.setText(historyDevices[i]);
                
                singleItemLayout.addView(itemTitle, layout);
                
                final CheckBox itemCheck = new CheckBox(activity);
                
                layout = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
                layout.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                layout.addRule(RelativeLayout.CENTER_VERTICAL);
                
                // itemCheck.setLayoutParams(layout);
                
                itemCheck.setGravity(Gravity.RIGHT);
                itemCheck.setPadding(leftRightItem, 0, leftRightItem, 0);
                itemCheck.setOnClickListener(null);
                itemCheck.setClickable(false);
                itemCheck.setId(i + historyDevices.length);
                
                if (devicesToShow.isEmpty() || devicesToShow.contains(historyDevices[i]))
                    itemCheck.setChecked(true);
                else
                    itemCheck.setChecked(false);
                
                singleItemLayout.addView(itemCheck, layout);
                
                // layout = new
                // RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                // RelativeLayout.LayoutParams.WRAP_CONTENT);
                
                // singleItemLayout.setLayoutParams(layout);
                
                singleItemLayout.setOnClickListener(new OnClickListener()
                {
                    
                    @Override
                    public void onClick(final View v)
                    {
                        final CheckBox check = (CheckBox) v.findViewById(v.getId() + historyDevices.length);
                        if (check.isChecked())
                        {
                            check.setChecked(false);
                            devicesToShow.remove(historyDevices[v.getId()]);
                        }
                        else
                        {
                            check.setChecked(true);
                            devicesToShow.add(historyDevices[v.getId()]);
                        }
                        
                    }
                    
                });
                
                deviceListView.addView(singleItemLayout);
                
                final View divider = new View(activity);
                
                layout = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, heightDiv);
                
                divider.setBackgroundResource(R.drawable.bg_trans_light_10);
                
                deviceListView.addView(divider, layout);
                
            }
            deviceListView.invalidate();
        }
        
        final String historyNetworks[] = activity.getHistoryFilterNetworks();
        
        if (historyNetworks != null)
        {
            
            for (int i = 0; i < historyNetworks.length; i++)
            {
                
                final RelativeLayout singleItemLayout = new RelativeLayout(activity); // (LinearLayout)measurememtItemView.findViewById(R.id.measurement_item);
                
                singleItemLayout.setId(i);
                singleItemLayout.setClickable(true);
                singleItemLayout.setBackgroundResource(R.drawable.list_selector);
                
                singleItemLayout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                        LayoutParams.WRAP_CONTENT));
                
                final TextView itemTitle = new TextView(activity, null, R.style.textMediumLight);
                RelativeLayout.LayoutParams layout = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                        LayoutParams.MATCH_PARENT);
                layout.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                layout.addRule(RelativeLayout.CENTER_VERTICAL);
                
                // itemTitle.setLayoutParams(layout);
                itemTitle.setGravity(Gravity.LEFT);
                itemTitle.setPadding(leftRightItem, 0, leftRightItem, 0);
                itemTitle.setText(historyNetworks[i]);
                
                singleItemLayout.addView(itemTitle, layout);
                
                final CheckBox itemCheck = new CheckBox(activity);
                
                layout = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
                layout.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                layout.addRule(RelativeLayout.CENTER_VERTICAL);
                
                // itemCheck.setLayoutParams(layout);
                
                itemCheck.setGravity(Gravity.RIGHT);
                itemCheck.setPadding(leftRightItem, 0, leftRightItem, 0);
                itemCheck.setOnClickListener(null);
                itemCheck.setClickable(false);
                itemCheck.setId(i + historyNetworks.length);
                
                if (networksToShow.isEmpty() || networksToShow.contains(historyNetworks[i]))
                    itemCheck.setChecked(true);
                else
                    itemCheck.setChecked(false);
                
                singleItemLayout.addView(itemCheck, layout);
                
                singleItemLayout.setOnClickListener(new OnClickListener()
                {
                    
                    @Override
                    public void onClick(final View v)
                    {
                        final CheckBox check = (CheckBox) v.findViewById(v.getId() + historyNetworks.length);
                        if (check.isChecked())
                        {
                            check.setChecked(false);
                            networksToShow.remove(historyNetworks[v.getId()]);
                        }
                        else
                        {
                            check.setChecked(true);
                            networksToShow.add(historyNetworks[v.getId()]);
                        }
                        System.out.println(networksToShow.toString());
                    }
                    
                });
                
                networkListView.addView(singleItemLayout);
                
                final View divider = new View(activity);
                
                layout = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, heightDiv);
                
                divider.setBackgroundResource(R.drawable.bg_trans_light_10);
                
                networkListView.addView(divider, layout);
                
            }
            networkListView.invalidate();
        }
        /*
         * // Set option as Multiple Choice. So that user can able to select
         * more the one option from list deviceListView.setAdapter(new
         * ArrayAdapter<String>(activity,
         * android.R.layout.simple_list_item_multiple_choice, historyDevices));
         * deviceListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
         * 
         * for (int i = 0; i < historyDevices.length; i++) {
         * //deviceListView.setItemChecked(i, true); }
         * 
         * deviceListView.setOnItemClickListener(new OnItemClickListener() {
         * 
         * @Override public void onItemClick(AdapterView<?> l, View v, int
         * position, long id) {
         * 
         * }
         * 
         * });
         * 
         * 
         * // Set option as Multiple Choice. So that user can able to select
         * more the one option from list networkListView.setAdapter(new
         * ArrayAdapter<String>(activity,
         * android.R.layout.simple_list_item_multiple_choice, networkDevices));
         * networkListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
         * 
         * for (int i = 0; i < networkDevices.length; i++) {
         * networkListView.setItemChecked(i, true); }
         * 
         * SparseBooleanArray checked =
         * deviceListView.getCheckedItemPositions(); ArrayList<String>
         * devicesToShow = new ArrayList<String>(); for(int i = 0; i <
         * checked.size()+1; i++){ if(checked.get(i))
         * devicesToShow.add(historyDevices[i]); }
         */
        
        return view;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see android.support.v4.app.Fragment#onDestroyView()
     */
    @Override
    public void onDestroyView()
    {
        
        activity.setHistoryFilterDevicesFilter(devicesToShow);
        activity.setHistoryFilterNetworksFilter(networksToShow);
        
        super.onDestroyView();
    }
    
}
