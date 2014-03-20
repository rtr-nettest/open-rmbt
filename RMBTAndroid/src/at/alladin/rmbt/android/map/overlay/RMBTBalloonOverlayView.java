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
package at.alladin.rmbt.android.map.overlay;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import at.alladin.openrmbt.android.R;
import at.alladin.rmbt.android.util.Helperfunctions;

public class RMBTBalloonOverlayView extends View
{
    private static final String DEBUG_TAG = "RMBTBalloonOverlayView";
    
//    private TextView title;
    private JSONArray resultItems;
    private Context context;
    private static LinearLayout resultListView;
    private static TextView emptyView;
    private static ProgressBar progessBar;
    
    public RMBTBalloonOverlayView(final Context context)
    {
        super(context);
    }
    
    public View setupView(final Context context, final ViewGroup parent)
    {
        this.context = context;
        
        // inflate our custom layout into parent
        final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View v = inflater.inflate(R.layout.balloon_overlay, parent);
        // setup our fields
//        title = (TextView) v.findViewById(R.id.balloon_item_title);
        
        resultListView = (LinearLayout) v.findViewById(R.id.resultList);
        resultListView.setVisibility(View.GONE);
        
        emptyView = (TextView) v.findViewById(R.id.infoText);
        emptyView.setVisibility(View.GONE);
        
        progessBar = (ProgressBar) v.findViewById(R.id.progressBar);
        
        return v;
        
    }
    
    public void setBalloonData(final RMBTBalloonOverlayItem item, final ViewGroup parent)
    {
        // map our custom item data to fields
//        title.setText(item.getTitle());
        resultItems = item.getResultItems();
        
        resultListView.removeAllViews();
        
        final float scale = getResources().getDisplayMetrics().density;
        
        final int leftRightItem = Helperfunctions.dpToPx(5, scale);
        final int topBottomItem = Helperfunctions.dpToPx(3, scale);
        
        final int leftRightDiv = Helperfunctions.dpToPx(0, scale);
        final int topBottomDiv = Helperfunctions.dpToPx(0, scale);
        final int heightDiv = Helperfunctions.dpToPx(1, scale);
        
        final int topBottomImg = Helperfunctions.dpToPx(1, scale);
        
        if (resultItems != null && resultItems.length() > 0)
        {
            
            for (int i = 0; i < 1; i++)
                // JSONObject resultListItem;
                try
                {
                    final JSONObject result = resultItems.getJSONObject(i);
                    
                    final LayoutInflater resultInflater = (LayoutInflater) context
                            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    
                    final View resultView = resultInflater.inflate(R.layout.balloon_overlay_listitem, parent);
                    
                    final LinearLayout measurementLayout = (LinearLayout) resultView
                            .findViewById(R.id.resultMeasurementList);
                    measurementLayout.setVisibility(View.GONE);
                    
                    final LinearLayout netLayout = (LinearLayout) resultView.findViewById(R.id.resultNetList);
                    netLayout.setVisibility(View.GONE);
                    
                    final TextView measurementHeader = (TextView) resultView.findViewById(R.id.resultMeasurement);
                    measurementHeader.setVisibility(View.GONE);
                    
                    final TextView netHeader = (TextView) resultView.findViewById(R.id.resultNet);
                    netHeader.setVisibility(View.GONE);
                    
                    final TextView dateHeader = (TextView) resultView.findViewById(R.id.resultDate);
                    dateHeader.setVisibility(View.GONE);
                    
                    dateHeader.setText(result.optString("time_string"));
                    
                    final JSONArray measurementArray = result.getJSONArray("measurement");
                    
                    final JSONArray netArray = result.getJSONArray("net");
                    
                    for (int j = 0; j < measurementArray.length(); j++)
                    {
                        
                        final JSONObject singleItem = measurementArray.getJSONObject(j);
                        
                        final LinearLayout measurememtItemLayout = new LinearLayout(context); // (LinearLayout)measurememtItemView.findViewById(R.id.measurement_item);
                        
                        measurememtItemLayout.setLayoutParams(new LinearLayout.LayoutParams(
                                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                                android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
                        
                        measurememtItemLayout.setGravity(Gravity.CENTER_VERTICAL);
                        measurememtItemLayout.setPadding(leftRightItem, topBottomItem, leftRightItem, topBottomItem);
                        
                        final TextView itemTitle = new TextView(context, null, R.style.balloonResultItemTitle);
                        itemTitle.setLayoutParams(new LinearLayout.LayoutParams(
                                android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                                android.view.ViewGroup.LayoutParams.WRAP_CONTENT, 0.4f));
                        itemTitle.setTextAppearance(context, R.style.balloonResultItemTitle);
                        itemTitle.setWidth(0);
                        itemTitle.setGravity(Gravity.LEFT);
                        itemTitle.setText(singleItem.getString("title"));
                        
                        measurememtItemLayout.addView(itemTitle);
                        
                        final ImageView itemClassification = new ImageView(context);
                        itemClassification.setLayoutParams(new LinearLayout.LayoutParams(
                                android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                                android.view.ViewGroup.LayoutParams.MATCH_PARENT, 0.1f));
                        itemClassification.setPadding(0, topBottomImg, 0, topBottomImg);
                        // itemClassification.set setGravity(Gravity.LEFT);
                        
                        itemClassification.setImageDrawable(getResources().getDrawable(
                                Helperfunctions.getClassificationColor(singleItem.getInt("classification"))));
                        
                        measurememtItemLayout.addView(itemClassification);
                        
                        final TextView itemValue = new TextView(context, null, R.style.balloonResultItemValue);
                        itemValue.setLayoutParams(new LinearLayout.LayoutParams(
                                android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                                android.view.ViewGroup.LayoutParams.WRAP_CONTENT, 0.5f));
                        itemValue.setTextAppearance(context, R.style.balloonResultItemValue);
                        itemValue.setWidth(0);
                        itemValue.setGravity(Gravity.LEFT);
                        itemValue.setText(singleItem.getString("value"));
                        
                        measurememtItemLayout.addView(itemValue);
                        
                        measurementLayout.addView(measurememtItemLayout);
                        
                        final View divider = new View(context);
                        divider.setLayoutParams(new LinearLayout.LayoutParams(
                                android.view.ViewGroup.LayoutParams.MATCH_PARENT, heightDiv, 1));
                        divider.setPadding(leftRightDiv, topBottomDiv, leftRightDiv, topBottomDiv);
                        
                        divider.setBackgroundResource(R.drawable.bg_trans_light_10);
                        
                        measurementLayout.addView(divider);
                        
                        measurementLayout.invalidate();
                    }
                    
                    for (int j = 0; j < netArray.length(); j++)
                    {
                        
                        final JSONObject singleItem = netArray.getJSONObject(j);
                        
                        final LinearLayout netItemLayout = new LinearLayout(context); // (LinearLayout)measurememtItemView.findViewById(R.id.measurement_item);
                        
                        netItemLayout.setLayoutParams(new LinearLayout.LayoutParams(
                                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                                android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
                        netItemLayout.setPadding(leftRightItem, topBottomItem, leftRightItem, topBottomItem);
                        
                        netItemLayout.setGravity(Gravity.CENTER_VERTICAL);
                        
                        final TextView itemTitle = new TextView(context, null, R.style.balloonResultItemTitle);
                        itemTitle.setLayoutParams(new LinearLayout.LayoutParams(
                                android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                                android.view.ViewGroup.LayoutParams.WRAP_CONTENT, 0.4f));
                        itemTitle.setTextAppearance(context, R.style.balloonResultItemTitle);
                        itemTitle.setWidth(0);
                        itemTitle.setGravity(Gravity.LEFT);
                        itemTitle.setText(singleItem.getString("title"));
                        
                        netItemLayout.addView(itemTitle);
                        
                        final ImageView itemClassification = new ImageView(context);
                        itemClassification.setLayoutParams(new LinearLayout.LayoutParams(
                                android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                                android.view.ViewGroup.LayoutParams.MATCH_PARENT, 0.1f));
                        itemClassification.setPadding(0, topBottomImg, 0, topBottomImg);
                        
                        itemClassification.setImageDrawable(context.getResources().getDrawable(
                                R.drawable.traffic_lights_none));
                        netItemLayout.addView(itemClassification);
                        
                        final TextView itemValue = new TextView(context, null, R.style.balloonResultItemValue);
                        itemValue.setLayoutParams(new LinearLayout.LayoutParams(
                                android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                                android.view.ViewGroup.LayoutParams.WRAP_CONTENT, 0.5f));
                        itemValue.setTextAppearance(context, R.style.balloonResultItemValue);
                        itemValue.setWidth(0);
                        itemValue.setGravity(Gravity.LEFT);
                        itemValue.setText(singleItem.optString("value", null));
                        
                        netItemLayout.addView(itemValue);
                        
                        netLayout.addView(netItemLayout);
                        
                        final View divider = new View(context);
                        divider.setLayoutParams(new LinearLayout.LayoutParams(
                                android.view.ViewGroup.LayoutParams.MATCH_PARENT, heightDiv, 1));
                        divider.setPadding(leftRightDiv, topBottomDiv, leftRightDiv, topBottomDiv);
                        
                        divider.setBackgroundResource(R.drawable.bg_trans_light_10);
                        
                        netLayout.addView(divider);
                        
                        netLayout.invalidate();
                    }
                    
                    measurementHeader.setVisibility(View.VISIBLE);
                    netHeader.setVisibility(View.VISIBLE);
                    
                    measurementLayout.setVisibility(View.VISIBLE);
                    netLayout.setVisibility(View.VISIBLE);
                    
                    dateHeader.setVisibility(View.VISIBLE);
                    
                    resultListView.addView(resultView);
                    
                    Log.d(DEBUG_TAG, "View Added");
                    // codeText.setText(resultListItem.getString("sync_code"));
                    
                }
                catch (final JSONException e)
                {
                    e.printStackTrace();
                }
            
            progessBar.setVisibility(View.GONE);
            emptyView.setVisibility(View.GONE);
            
            resultListView.setVisibility(View.VISIBLE);
            
            resultListView.invalidate();
        }
        else
        {
            Log.i(DEBUG_TAG, "LEERE LISTE");
            progessBar.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
            emptyView.setText(context.getString(R.string.error_no_data));
            emptyView.invalidate();
        }
    }
}
