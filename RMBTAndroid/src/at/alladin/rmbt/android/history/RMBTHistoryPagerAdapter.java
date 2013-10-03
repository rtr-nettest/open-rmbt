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
package at.alladin.rmbt.android.history;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import at.alladin.openrmbt.android.R;
import at.alladin.rmbt.android.main.RMBTMainActivity;
import at.alladin.rmbt.android.test.RMBTService;
import at.alladin.rmbt.android.util.CheckTestResultTask;
import at.alladin.rmbt.android.util.ConfigHelper;
import at.alladin.rmbt.android.util.EndTaskListener;
import at.alladin.rmbt.android.util.Helperfunctions;
import at.alladin.rmbt.client.helper.NdtStatus;

import com.google.android.gms.maps.model.LatLng;

public class RMBTHistoryPagerAdapter extends PagerAdapter
{
    private static final String DEBUG_TAG = "RMBTHistoryPagerAdapter";
    
    private final DateFormat format = Helperfunctions.getDateFormat(false);
    private final Date tmpDate = new Date();
    
    private final ArrayList<HashMap<String, String>> itemList;
    
    private final RMBTMainActivity activity;
    
    private final int resultAfterTestPos;
    
    private final boolean ndt;
    private final Handler handler;
    private RMBTService service;
    private Runnable progressUpdater;
    private ProgressView extendedProgressView;
    private TextView extendedStatusView;
    private Button extendedResultButtonCancel;
    private Button extendedButtonDetails;
    
    private NdtStatus lastNdtStatus;
    
    private Dialog dialog;
    
    public RMBTHistoryPagerAdapter(final RMBTMainActivity _activity, final ArrayList<HashMap<String, String>> itemList,
            final int resultAfterTestPos, final Handler _handler)
    {
        activity = _activity;
        this.itemList = itemList;
        this.resultAfterTestPos = resultAfterTestPos;
        handler = _handler;
        ndt = ConfigHelper.isNDT(_activity);
        
        if (ndt && resultAfterTestPos != -1)
            progressUpdater = new Runnable()
            {
                @Override
                public void run()
                {
//                    Log.d(DEBUG_TAG, "refreshing progress");
                    final NdtStatus status;
                    float progress = 0f;
                    if (service != null)
                    {
//                        Log.d(DEBUG_TAG, "we have the service");
                        NdtStatus _status = service.getNdtStatus();
                        if (_status == null)
                            _status = lastNdtStatus == null ? NdtStatus.NOT_STARTED : lastNdtStatus;
                        status = _status;
                        lastNdtStatus = status;
                        progress = service.getNDTProgress();
                    }
                    else
                    {
//                        Log.d(DEBUG_TAG, "we have no service");
                        NdtStatus _status = lastNdtStatus;
                        if (_status == null)
                            _status = NdtStatus.NOT_STARTED;
                        if (_status == NdtStatus.RUNNING)
                            _status = NdtStatus.FINISHED;
                        status = _status;
                    }
                    
//                    Log.d(DEBUG_TAG, String.format("status: %s", status == null ? "null" : status.toString()));
                    
                    switch (status)
                    {
                    case NOT_STARTED:
                    case ABORTED:
                    case ERROR:
                        progress = 0f;
                        break;
                    
                    case FINISHED:
                    case RESULTS:
                        progress = 1f;
                        break;
                    }
                    
//                    Log.d(DEBUG_TAG, String.format("progress: %f", progress));
                    
                    if (extendedProgressView != null)
                        extendedProgressView.setProgress(progress);
                    if (extendedStatusView != null)
                        switch (status)
                        {
                        case NOT_STARTED:
                            extendedStatusView.setText(R.string.result_extended_test_not_started);
                            break;
                        
                        case RUNNING:
                            extendedStatusView.setText(String.format("%s (%d%%)",
                                    activity.getString(R.string.result_extended_test_running),
                                    Math.round(progress * 100f)));
                            break;
                            
                        case RESULTS:
                            extendedStatusView.setText(activity.getString(R.string.result_extended_test_results));
                            break;
                            
                        case FINISHED:
                            extendedStatusView.setText(R.string.result_extended_test_finished);
                            break;
                        
                        case ABORTED:
                            extendedStatusView.setText(R.string.result_extended_test_aborted);
                            break;
                        
                        case ERROR:
                            extendedStatusView.setText(R.string.result_extended_test_error);
                            break;
                        }
                    
                    if (status == NdtStatus.RUNNING && extendedResultButtonCancel != null && extendedResultButtonCancel.getVisibility() == View.GONE)
                    {
                        extendedResultButtonCancel.setVisibility(View.VISIBLE);
                        if (extendedButtonDetails != null)
                        extendedButtonDetails.setVisibility(View.GONE);
                    }
                    
                    if (status == NdtStatus.NOT_STARTED || status == NdtStatus.RUNNING || status == NdtStatus.RESULTS)
                    {
//                        Log.d(DEBUG_TAG, "rescheduling progressUpdater");
                        handler.postDelayed(progressUpdater, 500);
                    }
                    else
                    {
//                        Log.d(DEBUG_TAG, "no reschulding");
                        if (extendedResultButtonCancel != null)
                            extendedResultButtonCancel.setVisibility(View.GONE);
                        if (extendedButtonDetails != null)
                            extendedButtonDetails.setVisibility(View.VISIBLE);
                    }
                }
            };
    }
    
    @Override
    public Parcelable saveState()
    {
        Bundle bundle = new Bundle();
        bundle.putParcelable("instanceState", super.saveState());
        bundle.putSerializable("lastNdtStatus", lastNdtStatus);
        return bundle;
    }
    
    @Override
    public void restoreState(Parcelable state, ClassLoader cl)
    {
        if (state instanceof Bundle)
        {
            Bundle bundle = (Bundle) state;
            super.restoreState(bundle.getParcelable("instanceState"), cl);
            lastNdtStatus = (NdtStatus)bundle.getSerializable("lastNdtStatus");
            return;
        }
    }
    
    @Override
    public Object instantiateItem(final ViewGroup vg, final int i)
    {
        final Context context = vg.getContext();
        final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        
        final String testUuid = itemList.get(i).get("test_uuid");
        final boolean resultAfterText = resultAfterTestPos == i;
        final View view = inflater.inflate(resultAfterText ? R.layout.test_result_after_test : R.layout.test_result,
                vg, false);
        
        setExtendedTestStatus(view, true);
        
        final Button helpButton = (Button) view.findViewById(R.id.resultButtonHelp);
        if (helpButton != null)
            helpButton.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(final View v)
                {
                    final Runnable runnable = new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            activity.showHelp("");
                        }
                    };
                    final boolean wasRunning = checkTestRunning(runnable);
                    if (!wasRunning)
                        runnable.run();
                }
            });
        
        final Button shareButton = (Button) view.findViewById(R.id.resultButtonShare);
        if (shareButton != null)
            shareButton.setEnabled(false);
        
        final Button buttonMap = (Button) view.findViewById(R.id.mapButton);
        final TextView textMap = (TextView) view.findViewById(R.id.mapButtonText);
        final ImageView iconMap = (ImageView) view.findViewById(R.id.mapButtonImage);
        if (buttonMap != null)
            buttonMap.setEnabled(false);
        
        if (textMap != null)
            textMap.setTextColor(Color.parseColor("#bbcdd6"));
        
        if (iconMap != null)
            iconMap.setBackgroundResource(R.drawable.icon_map_medium_deact);
        
        final Button newTestButton = (Button) view.findViewById(R.id.result_new_test_button);
        if (newTestButton != null)
            newTestButton.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(final View v)
                {
                    final Runnable runnable = new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            activity.startTest(true);
                        }
                    };
                    final boolean wasRunning = checkTestRunning(runnable);
                    if (!wasRunning)
                        runnable.run();
                }
            });
        
        final Button historyButton = (Button) view.findViewById(R.id.result_history_button);
        if (historyButton != null)
            historyButton.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(final View v)
                {
                    final Runnable runnable = new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            activity.showHistory(true);
                        }
                    };
                    final boolean wasRunning = checkTestRunning(runnable);
                    if (!wasRunning)
                        runnable.run();
                }
            });
        
        final LinearLayout measurementLayout = (LinearLayout) view.findViewById(R.id.resultMeasurementList);
        measurementLayout.setVisibility(View.GONE);
        
        final LinearLayout netLayout = (LinearLayout) view.findViewById(R.id.resultNetList);
        netLayout.setVisibility(View.GONE);
        
        final TextView measurementHeader = (TextView) view.findViewById(R.id.resultMeasurement);
        measurementHeader.setVisibility(View.GONE);
        
        final TextView netHeader = (TextView) view.findViewById(R.id.resultNet);
        netHeader.setVisibility(View.GONE);
        
        final TextView emptyView = (TextView) view.findViewById(R.id.infoText);
        emptyView.setVisibility(View.GONE);
        
        final ProgressBar progessBar = (ProgressBar) view.findViewById(R.id.progressBar);
        
        Button buttonDetails = (Button) view.findViewById(R.id.detailsButton);
        
        if (ndt && resultAfterText) // should only be true for only
                                                // 1 item
        {
            view.findViewById(R.id.detailsButtonWrapper).setVisibility(View.GONE);
            view.findViewById(R.id.resultHeader).setVisibility(View.VISIBLE);
            view.findViewById(R.id.resultExtended).setVisibility(View.VISIBLE);
            
            buttonDetails = (Button) view.findViewById(R.id.extendedDetailsButton);
            
            final ProgressView extendedProgressView = (ProgressView) view.findViewById(R.id.resultExtendedProgressView);
            if (extendedProgressView != null)
                this.extendedProgressView = extendedProgressView;
            
            final TextView extendedStatusView = (TextView) view.findViewById(R.id.resultExtendedTestStatus);
            if (extendedStatusView != null)
                this.extendedStatusView = extendedStatusView;
            
            final Button resultButtonCancel = (Button) view.findViewById(R.id.resultButtonCancel);
            if (resultButtonCancel != null)
            {
                extendedResultButtonCancel = resultButtonCancel;
                resultButtonCancel.setOnClickListener(new OnClickListener()
                {
                    @Override
                    public void onClick(final View arg0)
                    {
                        checkTestRunning(null);
                    }
                });
            }
            
            if (buttonDetails != null)
                extendedButtonDetails = buttonDetails;
        }
        
        if (buttonDetails != null)
            buttonDetails.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(final View v)
                {
                    final Runnable runnable = new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            activity.showResultDetail(testUuid);
                        }
                    };
                    final boolean wasRunning = checkTestRunning(runnable);
                    if (!wasRunning)
                        runnable.run();
                }
            });
        
        final float scale = activity.getResources().getDisplayMetrics().density;
        
        final CheckTestResultTask testResultTask = new CheckTestResultTask(activity);
        testResultTask.setEndTaskListener(new EndTaskListener()
        {
            @Override
            public void taskEnded(final JSONArray testResult)
            {
                if (ndt && service != null && resultAfterText)
                    service.letNDTStart();
                
                if (testResult != null && testResult.length() > 0 && !testResultTask.hasError())
                {
                    
                    JSONObject resultListItem;
                    
                    try
                    {
                        resultListItem = testResult.getJSONObject(0);
                        
                        if (buttonMap != null && resultListItem.has("geo_lat") && resultListItem.has("geo_long")
                                && resultListItem.getDouble("geo_lat") != 0
                                && resultListItem.getDouble("geo_long") != 0)
                        {
                            final double geoLat = resultListItem.getDouble("geo_lat");
                            final double geoLong = resultListItem.getDouble("geo_long");
                            final int networkType = resultListItem.getInt("network_type");
                            final String mapType = Helperfunctions.getMapType(networkType) + "/download";
                            
                            buttonMap.setOnClickListener(new OnClickListener()
                            {
                                @Override
                                public void onClick(final View v)
                                {
                                    final Runnable runnable = new Runnable()
                                    {
                                        @Override
                                        public void run()
                                        {
                                            final LatLng testPoint = new LatLng(geoLat, geoLong);
                                            activity.showMap(mapType, testPoint, true);
                                        }
                                    };
                                    final boolean wasRunning = checkTestRunning(runnable);
                                    if (!wasRunning)
                                        runnable.run();
                                }
                            });
                            buttonMap.setEnabled(true);
                            if (textMap != null)
                                textMap.setTextColor(Color.parseColor("#006398"));
                            if (iconMap != null)
                                iconMap.setBackgroundResource(R.drawable.icon_map_medium);
                        }
                        
                        if (shareButton != null && resultListItem.has("share_text"))
                        {
                            final String shareText = resultListItem.getString("share_text");
                            shareButton.setOnClickListener(new OnClickListener()
                            {
                                @Override
                                public void onClick(final View v)
                                {
                                    final Runnable runnable = new Runnable()
                                    {
                                        @Override
                                        public void run()
                                        {
                                            final Intent sendIntent = new Intent();
                                            sendIntent.setAction(Intent.ACTION_SEND);
                                            sendIntent.putExtra(Intent.EXTRA_TEXT, shareText);
                                            sendIntent.setType("text/plain");
                                            activity.startActivity(Intent.createChooser(sendIntent, null));
                                        }
                                    };
                                    final boolean wasRunning = checkTestRunning(runnable);
                                    if (!wasRunning)
                                        runnable.run();
                                }
                            });
                            shareButton.setEnabled(true);
                        }
                        
                        final JSONArray measurementArray = resultListItem.getJSONArray("measurement");
                        
                        final JSONArray netArray = resultListItem.getJSONArray("net");
                        
                        final int leftRightItem = Helperfunctions.dpToPx(5, scale);
                        final int topBottomItem = Helperfunctions.dpToPx(5, scale);
                        
                        final int leftRightDiv = Helperfunctions.dpToPx(0, scale);
                        final int topBottomDiv = Helperfunctions.dpToPx(0, scale);
                        final int heightDiv = Helperfunctions.dpToPx(1, scale);
                        
                        final int topBottomImg = Helperfunctions.dpToPx(1, scale);
                        
                        for (int i = 0; i < measurementArray.length(); i++)
                        {
                            
                            final JSONObject singleItem = measurementArray.getJSONObject(i);
                            
                            final LinearLayout measurememtItemLayout = new LinearLayout(activity); // (LinearLayout)measurememtItemView.findViewById(R.id.measurement_item);
                            
                            measurememtItemLayout.setLayoutParams(new LinearLayout.LayoutParams(
                                    LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
                            
                            measurememtItemLayout.setGravity(Gravity.CENTER_VERTICAL);
                            measurememtItemLayout
                                    .setPadding(leftRightItem, topBottomItem, leftRightItem, topBottomItem);
                            
                            final TextView itemTitle = new TextView(activity, null, R.style.listResultItemTitle);
                            itemTitle.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                                    LayoutParams.WRAP_CONTENT, 0.4f));
                            itemTitle.setWidth(0);
                            itemTitle.setGravity(Gravity.LEFT);
                            itemTitle.setText(singleItem.getString("title"));
                            
                            measurememtItemLayout.addView(itemTitle);
                            
                            final ImageView itemClassification = new ImageView(activity);
                            itemClassification.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                                    LayoutParams.MATCH_PARENT, 0.1f));
                            itemClassification.setPadding(0, topBottomImg, 0, topBottomImg);
                            // itemClassification.set setGravity(Gravity.LEFT);
                            
                            itemClassification.setImageDrawable(activity.getResources().getDrawable(
                                    Helperfunctions.getClassificationColor(singleItem.getInt("classification"))));
                            
                            measurememtItemLayout.addView(itemClassification);
                            
                            itemClassification.setOnClickListener(new OnClickListener()
                            {
                                @Override
                                public void onClick(View v)
                                {
                                    activity.showHelp(R.string.url_help_result);
                                }
                            });
                            
                            final TextView itemValue = new TextView(activity, null, R.style.listResultItemValue);
                            itemValue.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                                    LayoutParams.WRAP_CONTENT, 0.5f));
                            itemValue.setWidth(0);
                            itemValue.setGravity(Gravity.LEFT);
                            itemValue.setText(singleItem.getString("value"));
                            
                            measurememtItemLayout.addView(itemValue);
                            
                            measurementLayout.addView(measurememtItemLayout);
                            
                            final View divider = new View(activity);
                            divider.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, heightDiv,
                                    1));
                            divider.setPadding(leftRightDiv, topBottomDiv, leftRightDiv, topBottomDiv);
                            
                            divider.setBackgroundResource(R.drawable.bg_trans_light_10);
                            
                            measurementLayout.addView(divider);
                            
                            measurementLayout.invalidate();
                        }
                        
                        for (int i = 0; i < netArray.length(); i++)
                        {
                            
                            final JSONObject singleItem = netArray.getJSONObject(i);
                            
                            final LinearLayout netItemLayout = new LinearLayout(activity); // (LinearLayout)measurememtItemView.findViewById(R.id.measurement_item);
                            
                            netItemLayout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                                    LayoutParams.WRAP_CONTENT));
                            netItemLayout.setPadding(leftRightItem, topBottomItem, leftRightItem, topBottomItem);
                            
                            netItemLayout.setGravity(Gravity.CENTER_VERTICAL);
                            
                            final TextView itemTitle = new TextView(activity, null, R.style.listResultItemTitle);
                            itemTitle.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                                    LayoutParams.WRAP_CONTENT, 0.4f));
                            itemTitle.setWidth(0);
                            itemTitle.setGravity(Gravity.LEFT);
                            itemTitle.setText(singleItem.getString("title"));
                            
                            netItemLayout.addView(itemTitle);
                            
                            final ImageView itemClassification = new ImageView(activity);
                            itemClassification.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                                    LayoutParams.MATCH_PARENT, 0.1f));
                            itemClassification.setPadding(0, topBottomImg, 0, topBottomImg);
                            
                            itemClassification.setImageDrawable(activity.getResources().getDrawable(
                                    R.drawable.traffic_lights_none));
                            netItemLayout.addView(itemClassification);
                            
                            final TextView itemValue = new TextView(activity, null, R.style.listResultItemValue);
                            itemValue.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                                    LayoutParams.WRAP_CONTENT, 0.5f));
                            itemValue.setWidth(0);
                            itemValue.setGravity(Gravity.LEFT);
                            itemValue.setText(singleItem.optString("value", null));
                            
                            netItemLayout.addView(itemValue);
                            
                            netLayout.addView(netItemLayout);
                            
                            final View divider = new View(activity);
                            divider.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, heightDiv,
                                    1));
                            divider.setPadding(leftRightDiv, topBottomDiv, leftRightDiv, topBottomDiv);
                            
                            divider.setBackgroundResource(R.drawable.bg_trans_light_10);
                            
                            netLayout.addView(divider);
                            
                            netLayout.invalidate();
                        }
                        
                    }
                    catch (final JSONException e)
                    {
                        e.printStackTrace();
                    }
                    
                    progessBar.setVisibility(View.GONE);
                    emptyView.setVisibility(View.GONE);
                    
                    measurementHeader.setVisibility(View.VISIBLE);
                    netHeader.setVisibility(View.VISIBLE);
                    
                    measurementLayout.setVisibility(View.VISIBLE);
                    netLayout.setVisibility(View.VISIBLE);
                    
                }
                else
                {
                    Log.i(DEBUG_TAG, "LEERE LISTE");
                    progessBar.setVisibility(View.GONE);
                    emptyView.setVisibility(View.VISIBLE);
                    emptyView.setText(activity.getString(R.string.error_no_data));
                    emptyView.invalidate();
                }
            }
        });
        testResultTask.execute(testUuid);
        
        vg.addView(view);
        return view;
    }
    
    private static void setExtendedTestStatus(final View view, final boolean finished)
    {
        final ProgressView extendedProgress = (ProgressView) view.findViewById(R.id.resultExtendedProgressView);
        final TextView extendedStatus = (TextView) view.findViewById(R.id.resultExtendedTestStatus);
        final Button extendedButtonCancel = (Button) view.findViewById(R.id.resultButtonCancel);
        final Button extendedButtonDetails = (Button) view.findViewById(R.id.extendedDetailsButton);
        
        if (extendedProgress != null)
            extendedProgress.setProgress(finished ? 1f : 0f);
        if (extendedButtonCancel != null)
            extendedButtonCancel.setVisibility(finished ? View.GONE : View.VISIBLE);
        if (extendedButtonDetails != null)
            extendedButtonDetails.setVisibility(finished ? View.VISIBLE : View.GONE);
        if (extendedStatus != null)
            extendedStatus.setText(finished ? R.string.result_extended_test_finished
                    : R.string.result_extended_test_running);
    }
    
    /**
	 * 
	 */
    @Override
    public void destroyItem(final ViewGroup vg, final int i, final Object obj)
    {
        final View view = (View) obj;
        vg.removeView(view);
    }
    
    @Override
    public boolean isViewFromObject(final View view, final Object object)
    {
        return view == object;
    }
    
    @Override
    public int getCount()
    {
        return itemList.size();
    }
    
    @Override
    public CharSequence getPageTitle(final int position)
    {
        final HashMap<String, String> item = itemList.get(position);
        
        return Helperfunctions.formatTimestampWithTimezone(tmpDate, format, Long.valueOf(item.get("time")),
                item.get("timezone"));
    }
    
    public void setService(final RMBTService service)
    {
        this.service = service;
    }
    
    public void removeService()
    {
        service = null;
    }
    
    public void destroy()
    {
        if (handler != null && progressUpdater != null)
            handler.removeCallbacks(progressUpdater);
    }
    
    public void onPause()
    {
        if (handler != null && progressUpdater != null)
            handler.removeCallbacks(progressUpdater);
    }
    
    public void onResume()
    {
        if (ndt && resultAfterTestPos != -1)
            handler.post(progressUpdater);
    }
    
    public boolean onBackPressed()
    {
        return checkTestRunning(new Runnable()
        {
            @Override
            public void run()
            {
                activity.getSupportFragmentManager().popBackStack();
            }
        });
    }
    
    public boolean checkTestRunning(final Runnable runnable)
    {
        if (service == null || !service.isTestRunning())
            return false;
        
        if (dialog == null || !dialog.isShowing())
        {
            final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle(R.string.test_dialog_abort_title);
            builder.setMessage(R.string.test_dialog_abort_extended_text);
            builder.setPositiveButton(R.string.test_dialog_abort_yes, new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(final DialogInterface dialog, final int which)
                {
                    if (extendedResultButtonCancel != null && extendedResultButtonCancel.isShown())
                        extendedResultButtonCancel.setEnabled(false);
                    
                    if (service != null)
                        service.stopTest();
                    
                    if (runnable != null)
                        runnable.run();
                }
            });
            builder.setNegativeButton(R.string.test_dialog_abort_no, null);
            dialog = builder.create();
            dialog.show();
        }
        return true;
    }
    
    /*
     * @Override public int getItemPosition(Object object) { return
     * POSITION_NONE; }
     */
    
}
