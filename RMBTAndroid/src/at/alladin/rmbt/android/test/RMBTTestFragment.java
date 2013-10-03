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
package at.alladin.rmbt.android.test;

import java.text.DecimalFormat;
import java.text.Format;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;

import org.json.JSONArray;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import at.alladin.openrmbt.android.R;
import at.alladin.rmbt.android.history.RMBTHistoryPagerFragment;
import at.alladin.rmbt.android.main.RMBTMainActivity;
import at.alladin.rmbt.android.main.RMBTMainActivity.HistoryUpdatedCallback;
import at.alladin.rmbt.android.test.RMBTService.RMBTBinder;
import at.alladin.rmbt.android.util.ConfigHelper;
import at.alladin.rmbt.android.util.EndTaskListener;
import at.alladin.rmbt.android.util.Helperfunctions;
import at.alladin.rmbt.client.helper.IntermediateResult;
import at.alladin.rmbt.client.helper.NdtStatus;
import at.alladin.rmbt.client.helper.TestStatus;

public class RMBTTestFragment extends Fragment implements ServiceConnection
{
    private static final String TAG = "RMBTTestFragment";
    
    private static final long UPDATE_DELAY = 100;
    private static final int SLOW_UPDATE_COUNT = 20;
    
    private static final int PROGRESS_SEGMENTS_TOTAL = 96;
    private static final int PROGRESS_SEGMENTS_INIT = 14;
    private static final int PROGRESS_SEGMENTS_PING = 15;
    private static final int PROGRESS_SEGMENTS_DOWN = 34;
    private static final int PROGRESS_SEGMENTS_UP = 33;
    
    private static final long GRAPH_MAX_NSECS = 8000000000L;
    
    private final Format pingFormat = new DecimalFormat("@@ ms");
    private Format speedFormat;
    private final Format percentFormat = DecimalFormat.getPercentInstance();
    
    private Context context;
    
    private TestView testView;
    private GraphView graphView;
    private Graph speedGraph;
    private Graph signalGraph;
    private boolean uploadGraph;
    private boolean graphStarted;
    private TextView textView;
    
    private IntermediateResult intermediateResult;
    private int lastNetworkType;
    private String lastNetworkTypeString;
    
    private RMBTService rmbtService;
    
    private long updateCounter;
    
    private String bottomText;
    private Location lastLocation;
    private String lastOperatorName;
    private String lastServerName;
    private String lastIP;
    private TestStatus lastStatus;
    private String lastStatusString;
    private long lastShownWaitTime = -1;
    private String waitText;
    private boolean suppressError;
    
    private Dialog dialog;
    private ProgressDialog progressDialog;
    
    private Handler handler;
    
    private static final int RESULT_SWITCHER_POST_TIME = 3000;
    private static final long RESULT_SWITCHER_MAX_WAIT_TIME = 20l * 1000000000l; // 20s
    private static final long MAX_COUNTER_WITHOUT_RESULT = 100;
    
    private Long resultSwitcherTime;
    private final Runnable resultSwitcherRunnable = new Runnable()
    {
        @Override
        public void run()
        {
            getMainActivity().setHistoryDirty(true);
            getMainActivity().checkSettings(true, new EndTaskListener()
            {
                @Override
                public void taskEnded(final JSONArray resultList)
                {
                    final RMBTMainActivity mainActivity = getMainActivity();
                    if (mainActivity == null)
                        return;
                    mainActivity.updateHistory(new HistoryUpdatedCallback()
                    {
                        @Override
                        public void historyUpdated(final boolean success)
                        {
                            switchToResult();
                        }
                    });
                }
            });
        }
    };
    
    @Override
    public void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        handler = new Handler();
        context = getActivity().getApplicationContext();
        
        bottomText = getResources().getString(R.string.test_bottom_test);
        waitText = getResources().getString(R.string.test_progress_text_wait);
        
        speedFormat = new DecimalFormat(String.format("@@ %s",
                getActivity().getResources().getString(R.string.test_mbps)));
    }
    
    protected RMBTMainActivity getMainActivity()
    {
        return (RMBTMainActivity) getActivity();
    }
    
    @Override
    public void onServiceConnected(final ComponentName name, final IBinder service)
    {
        Log.d(TAG, "service connected");
        final RMBTBinder binder = (RMBTBinder) service;
        rmbtService = binder.getService();
    }
    
    @Override
    public void onServiceDisconnected(final ComponentName name)
    {
        Log.d(TAG, "service disconnected");
        rmbtService = null;
    }
    
    @Override
    public void onStop()
    {
        super.onStop();
        
        handler.removeCallbacks(updateTask);
        handler.removeCallbacks(resultSwitcherRunnable);
        
        context.unbindService(this);
    }
    
    @Override
    public void onStart()
    {
        super.onStart();
        
        // Bind to RMBTService
        final Intent serviceIntent = new Intent(context, RMBTService.class);
        context.bindService(serviceIntent, this, Context.BIND_AUTO_CREATE);
        
        handler.post(updateTask);
    }
    
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState)
    {
        final View view = inflater.inflate(R.layout.test, container, false);
        
        testView = (TestView) view.findViewById(R.id.test_view);
        graphView = (GraphView) view.findViewById(R.id.test_graph);
        textView = (TextView) view.findViewById(R.id.test_text);
        
        if (graphView != null)
        {
            speedGraph = Graph.addGraph(graphView, Color.parseColor("#00f940"), GRAPH_MAX_NSECS);
            signalGraph = Graph.addGraph(graphView, Color.parseColor("#f8a000"), GRAPH_MAX_NSECS);
        }
        uploadGraph = false;
        graphStarted = false;
        
        textView.setText("\n\n\n");
        
        final Resources res = getActivity().getResources();
        final String progressTitle = res.getString(R.string.test_progress_title);
        final String progressText = res.getString(R.string.test_progress_text);
        
        lastShownWaitTime = -1;
        progressDialog = ProgressDialog.show(getActivity(), progressTitle, progressText, true, false);
        progressDialog.setOnKeyListener(new DialogInterface.OnKeyListener()
        {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event)
            {
                if (keyCode == KeyEvent.KEYCODE_BACK)
                {
                    onBackPressed();
                    return true;
                }
                return false;
            }
        });
        
        return view;
    }
    
    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        if (progressDialog != null)
        {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }
    
    @Override
    public void onDestroy()
    {
        super.onDestroy();
        if (testView != null)
            testView.recycle();
        if (graphView != null)
            graphView.recycle();
        System.gc();
        if (dialog != null)
            dialog.dismiss();
    }
    
    @Override
    public void onDetach()
    {
        super.onDetach();
        if (dialog != null)
            dialog.dismiss();
    }
    
    private void resetGraph()
    {
        if (signalGraph != null)
            signalGraph.reset();
        if (speedGraph != null)
            speedGraph.reset();
        uploadGraph = false;
        graphStarted = false;
        if (graphView != null)
            graphView.invalidate();
    }
    
    private final Runnable updateTask = new Runnable()
    {
        @Override
        public void run()
        {
            if (getActivity() == null)
                return;
            updateUI();
            handler.postDelayed(this, UPDATE_DELAY);
        }
    };
    
    private void updateUI()
    {
        String teststatus;
        updateCounter++;
        
        if (rmbtService == null)
            return;
        
        intermediateResult = rmbtService.getIntermediateResult(intermediateResult);
        
        if (intermediateResult == null)
        {
            if (rmbtService.isConnectionError())
            {
                if (progressDialog != null)
                {
                    progressDialog.dismiss();
                    progressDialog = null;
                }
                if (!ConfigHelper.isRepeatTest(getActivity()))
                    showErrorDialog(true);
            }
            
            if (!rmbtService.isTestRunning() && updateCounter > MAX_COUNTER_WITHOUT_RESULT)
                getActivity().getSupportFragmentManager().popBackStack(); // leave
                                                                          // fragment
                                                                          // if
                                                                          // not
                                                                          // running
            return;
        }
        
        if (intermediateResult.status == TestStatus.WAIT)
        {
            if (progressDialog != null)
            {
                long wait = (intermediateResult.remainingWait + 999) / 1000; // round
                                                                             // up
                if (wait < 0)
                    wait = 0;
                if (wait != lastShownWaitTime)
                {
                    lastShownWaitTime = wait;
                    progressDialog.setMessage(MessageFormat.format(waitText, wait));
                }
            }
            return;
        }
        
        if (progressDialog != null)
        {
            progressDialog.dismiss();
            progressDialog = null;
        }
        
        boolean forceUpdate = false;
        
        if (rmbtService.getNdtStatus() == NdtStatus.RUNNING)
        {
            final String ndtStatus = String.format(Locale.US, "NDT (%d%%)", Math.round(rmbtService.getNDTProgress() * 100));
            if (lastStatusString == null || !ndtStatus.equals(lastStatusString))
            {
                forceUpdate = true;
                lastStatusString = ndtStatus;
            }
        }
        else if (lastStatus != intermediateResult.status)
        {
            lastStatus = intermediateResult.status;
            lastStatusString = Helperfunctions.getTestStatusString(getResources(), intermediateResult.status);
            forceUpdate = true;
        }
        
        if (updateCounter % SLOW_UPDATE_COUNT == 0 || forceUpdate)
        {
            final int networkType = rmbtService.getNetworkType();
            if (lastNetworkType != networkType && networkType != 0)
            {
                lastNetworkType = networkType;
                lastNetworkTypeString = Helperfunctions.getNetworkTypeName(networkType);
            }
            
            final String operatorName = rmbtService.getOperatorName();
            if (operatorName != null)
                lastOperatorName = operatorName;
            testView.setHeaderString(lastOperatorName);
            testView.setSubHeaderString(lastNetworkTypeString);
            final String serverName = rmbtService.getServerName();
            if (serverName != null)
                lastServerName = serverName;
            if (lastServerName == null)
                lastServerName = "?";
            
            final Location location = rmbtService.getLocation();
            if (location != null && !location.equals(lastLocation))
                lastLocation = location;
            
            final String locationStr_line1;
            final String locationStr_line2;
            if (lastLocation != null)
            {    
                locationStr_line1 = Helperfunctions.getLocationString(context, getResources(), lastLocation,1);
                locationStr_line2 = Helperfunctions.getLocationString(context, getResources(), lastLocation,2);
            }    
            else
            {    
                locationStr_line1 = "";
                locationStr_line2 = "";
            }
            
            final String ip = rmbtService.getIP();
            if (ip != null)
                lastIP = ip;
            if (lastIP == null)
                lastIP = "?";
            
            if (ConfigHelper.isRepeatTest(context))
                teststatus = String.format("%s #%d - %s",getString(R.string.test_loop),
                        rmbtService.testLoops(),lastStatusString);
            else
                teststatus = lastStatusString;
            
            textView.setText(MessageFormat.format(bottomText, teststatus, lastServerName, lastIP, 
                    locationStr_line1, locationStr_line2));
        }
        
        final Integer signal = rmbtService.getSignal();
        Double relativeSignal = null;
        if (signal != null)
            if (signal == 0)
                relativeSignal = null;
            else
                relativeSignal = (double) signal / 80d + 110d / 80d;
        
        double speedValueRelative = 0d;
        int progressSegments = 0;
        switch (intermediateResult.status)
        {
        case WAIT:
            break;
        
        case INIT:
            progressSegments = Math.round(PROGRESS_SEGMENTS_INIT * intermediateResult.progress);
            if (graphStarted)
                resetGraph();
            break;
        
        case PING:
            progressSegments = PROGRESS_SEGMENTS_INIT
                    + Math.round(PROGRESS_SEGMENTS_PING * intermediateResult.progress);
            break;
        
        case DOWN:
            progressSegments = PROGRESS_SEGMENTS_INIT + PROGRESS_SEGMENTS_PING
                    + Math.round(PROGRESS_SEGMENTS_DOWN * intermediateResult.progress);
            speedValueRelative = intermediateResult.downBitPerSecLog;
            
            if (graphView != null)
            {
                if (uploadGraph)
                    resetGraph();
                
                if (graphStarted || speedValueRelative != 0)
                {
                    graphStarted = true;
                    speedGraph.addValue(speedValueRelative);
                    if (relativeSignal != null)
                        signalGraph.addValue(relativeSignal);
                    graphView.invalidate();
                }
            }
            break;
        
        case INIT_UP:
            progressSegments = PROGRESS_SEGMENTS_INIT + PROGRESS_SEGMENTS_PING + PROGRESS_SEGMENTS_DOWN;
            speedValueRelative = intermediateResult.downBitPerSecLog;
            break;
        
        case UP:
            progressSegments = PROGRESS_SEGMENTS_INIT + PROGRESS_SEGMENTS_PING + PROGRESS_SEGMENTS_DOWN
                    + Math.round(PROGRESS_SEGMENTS_UP * intermediateResult.progress);
            speedValueRelative = intermediateResult.upBitPerSecLog;
            
            if (graphView != null)
            {
                if (!uploadGraph)
                {
                    resetGraph();
                    uploadGraph = true;
                }
                if (graphStarted || speedValueRelative != 0)
                {
                    graphStarted = true;
                    speedGraph.addValue(speedValueRelative);
                    if (relativeSignal != null)
                        signalGraph.addValue(relativeSignal);
                    graphView.invalidate();
                }
            }
            break;
        
        case END:
            progressSegments = PROGRESS_SEGMENTS_INIT + PROGRESS_SEGMENTS_PING + PROGRESS_SEGMENTS_DOWN
                    + PROGRESS_SEGMENTS_UP;
            speedValueRelative = intermediateResult.upBitPerSecLog;
            
            if (!ConfigHelper.isRepeatTest(context) || !rmbtService.loopContinue())
                if (resultSwitcherTime == null)
                {
                    resultSwitcherTime = System.nanoTime();
                    handler.postDelayed(resultSwitcherRunnable, RESULT_SWITCHER_POST_TIME);
                }
                else if (resultSwitcherTime + RESULT_SWITCHER_MAX_WAIT_TIME < System.nanoTime())
                {
                    resultSwitcherTime = System.nanoTime();
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            break;
        
        case ERROR:
        case ABORTED:
            progressSegments = 0;
            resetGraph();
            
            if (!ConfigHelper.isRepeatTest(getActivity()))
                showErrorDialog(true);
            
            break;
        }
        testView.setSpeedValue(speedValueRelative);
        
        if (signal != null)
            testView.setSignalString(String.valueOf(signal));
        if (relativeSignal != null)
            testView.setSignalValue(relativeSignal);
        
        final double progressValue = (double) progressSegments / PROGRESS_SEGMENTS_TOTAL;
        testView.setProgressValue(progressValue);
        testView.setProgressString(percentFormat.format(progressValue));
        
        final String pingStr;
        if (intermediateResult.pingNano < 0)
            pingStr = "–";
        else
            pingStr = pingFormat.format(intermediateResult.pingNano / 1000000.0);
        testView.setResultPingString(pingStr);
        final String downStr;
        if (intermediateResult.downBitPerSec < 0)
            downStr = "–";
        else
            downStr = speedFormat.format(intermediateResult.downBitPerSec / 1000000.0);
        testView.setResultDownString(downStr);
        final String upStr;
        if (intermediateResult.upBitPerSec < 0)
            upStr = "–";
        else
            upStr = speedFormat.format(intermediateResult.upBitPerSec / 1000000.0);
        testView.setResultUpString(upStr);
        
        testView.setTestStatus(intermediateResult.status);
        
        testView.invalidate();
    }
    
    public boolean onBackPressed()
    {
        if (rmbtService == null || !rmbtService.isTestRunning())
            return false;
        
        if (dialog == null || !dialog.isShowing())
        {
            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.test_dialog_abort_title);
            builder.setMessage(R.string.test_dialog_abort_text);
            builder.setPositiveButton(R.string.test_dialog_abort_yes, new OnClickListener()
            {
                @Override
                public void onClick(final DialogInterface dialog, final int which)
                {
                    if (rmbtService != null)
                        rmbtService.stopTest();
                    suppressError = true;
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            });
            builder.setNegativeButton(R.string.test_dialog_abort_no, null);
            dialog = builder.create();
            dialog.show();
        }
        return true;
    }
    
    protected void showErrorDialog(final boolean popAfterOk)
    {
        if (suppressError)
            return;
        suppressError = true;
        
        if (suppressError)
            return;

        if (dialog != null && dialog.isShowing())
            dialog.dismiss();
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.test_dialog_error_title);
        builder.setMessage(R.string.test_dialog_error_text);
        final OnClickListener onClickListener;
        if (popAfterOk)
            onClickListener = new OnClickListener()
            {
                @Override
                public void onClick(final DialogInterface dialog, final int which)
                {
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            };
        else
            onClickListener = null;
        builder.setNeutralButton(android.R.string.ok, onClickListener);
        dialog = builder.create();
        dialog.show();
    }
    
    private void switchToResult()
    {
        if (!isVisible())
            return;
        
        final String testUuid = rmbtService == null ? null : rmbtService.getTestUuid();
        
        if (dialog != null)
            dialog.dismiss();
        
        ArrayList<Map<String, String>> itemList = getMainActivity().getHistoryStorageList();
        if (itemList == null)
            itemList = new ArrayList<Map<String, String>>();
        int pos = 0;
        boolean found = false;
        if (testUuid != null)
        {
            for (final Map<String, String> item : itemList)
            {
                if (item.get("test_uuid").equals(testUuid))
                {
                    found = true;
                    break;
                }
                pos++;
            }
            if (!found)
                showErrorDialog(true);
        }
        
//        getMainActivity().setCurrentMapType(Helperfunctions.getMapType(lastNetworkType) + "/download");
        
        final RMBTHistoryPagerFragment fragment = new RMBTHistoryPagerFragment();
        final Bundle args = new Bundle();
        args.putInt(RMBTHistoryPagerFragment.ARG_POS, pos);
        args.putBoolean(RMBTHistoryPagerFragment.ARG_RESULT_AFTER_TEST, true);
        args.putSerializable(RMBTHistoryPagerFragment.ARG_ITEMS, itemList);
        fragment.setArguments(args);
        
        final FragmentManager fm = getActivity().getSupportFragmentManager();
        final FragmentTransaction ft;
        ft = fm.beginTransaction();
        ft.replace(R.id.fragment_content, fragment, "history_pager");
        ft.addToBackStack(null);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        fm.popBackStack();
        ft.commit();
    }
}
