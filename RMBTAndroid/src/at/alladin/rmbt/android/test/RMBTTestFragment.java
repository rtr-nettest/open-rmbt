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
package at.alladin.rmbt.android.test;

import java.text.DecimalFormat;
import java.text.Format;
import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import at.alladin.openrmbt.android.R;
import at.alladin.rmbt.android.graphview.GraphService;
import at.alladin.rmbt.android.graphview.GraphService.GraphData;
import at.alladin.rmbt.android.graphview.GraphView;
import at.alladin.rmbt.android.main.RMBTMainActivity;
import at.alladin.rmbt.android.test.RMBTService.RMBTBinder;
import at.alladin.rmbt.android.test.SmoothGraph.SmoothingFunction;
import at.alladin.rmbt.android.util.Helperfunctions;
import at.alladin.rmbt.android.util.InformationCollector;
import at.alladin.rmbt.android.util.net.NetworkUtil;
import at.alladin.rmbt.android.util.net.NetworkUtil.MinMax;
import at.alladin.rmbt.android.views.GroupCountView;
import at.alladin.rmbt.android.views.ProgressView;
import at.alladin.rmbt.android.views.ResultGraphView;
import at.alladin.rmbt.client.helper.IntermediateResult;
import at.alladin.rmbt.client.helper.NdtStatus;
import at.alladin.rmbt.client.helper.TestStatus;
import at.alladin.rmbt.client.v2.task.QoSTestEnum;

public class RMBTTestFragment extends Fragment implements ServiceConnection
{
    private static final String TAG = "RMBTTestFragment";

    /**
     * used for smoothing the speed graph: amount of data needed for smoothing function
     */
    public static final int SMOOTHING_DATA_AMOUNT = 3; // min 3
    
    /**
     * smoothing function used for speed graph.
     * BEWARE: different functions could require different data amounts
     */
    public static final SmoothingFunction SMOOTHING_FUNCTION = SmoothingFunction.CENTERED_MOVING_AVARAGE;
    
    private static final long UPDATE_DELAY = 100;
    private static final int SLOW_UPDATE_COUNT = 20;
    
    private static final int PROGRESS_SEGMENTS_TOTAL = 132;
    private static final int PROGRESS_SEGMENTS_INIT = 16;
    private static final int PROGRESS_SEGMENTS_PING = 17;
    private static final int PROGRESS_SEGMENTS_DOWN = 33;
    private static final int PROGRESS_SEGMENTS_UP = 33;
    private static final int PROGRESS_SEGMENTS_QOS = 33;
    
    private static final long GRAPH_MAX_NSECS = 8000000000L;
    
    private final Format pingFormat = new DecimalFormat("@@ ms");
    private Format speedFormat;
    private final Format percentFormat = DecimalFormat.getPercentInstance();
    
    private boolean qosMode = false;
    
    private Context context;
    
    private TestView testView;
    private GraphView graphView;
    private GraphService speedGraph;
    private GraphService signalGraph;
    private boolean uploadGraph;
    private boolean graphStarted;
    private TextView textView;
    private ViewGroup groupCountContainerView;
    private ViewGroup qosProgressView;
    private ViewGroup infoView;
    
    private IntermediateResult intermediateResult;
    private int lastNetworkType;
    private String lastNetworkTypeString;
    
    private Integer lastSignal;
    private int lastSignalType;
    
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
    private boolean stopLoop;
    
    private Dialog errorDialog;
    private Dialog abortDialog;
    private ProgressDialog progressDialog;
    
    private boolean showQoSErrorToast = false;
    
    private Handler handler;
    
    private static final long MAX_COUNTER_WITHOUT_RESULT = 100;
    
	GraphData signalGraphData;
	GraphData speedGraphData;

    
    private final Runnable resultSwitcherRunnable = new Runnable()
    {
        @Override
        public void run()
        {
            final RMBTMainActivity mainActivity = getMainActivity();
            if (mainActivity == null)
                return;

            mainActivity.setHistoryDirty(true);
            mainActivity.checkSettings(true, null);
            if (getMainActivity() == null)
                return;
            if (isVisible())
                switchToResult();
        }
    };
    
    @Override
    public void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        
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

        speedGraphData = speedGraph.getGraphData();
        signalGraphData = signalGraph.getGraphData();
        
        handler.removeCallbacks(updateTask);
        handler.removeCallbacks(resultSwitcherRunnable);
        
        context.unbindService(this);
        
        getActivity().getActionBar().show();
        ((RMBTMainActivity) getActivity()).setLockNavigationDrawer(false);
        
        //getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
    
    @Override
    public void onStart()
    {
        super.onStart();
        
        getActivity().getActionBar().hide();
        ((RMBTMainActivity) getActivity()).setLockNavigationDrawer(true);
        
        // Bind to RMBTService
        final Intent serviceIntent = new Intent(context, RMBTService.class);
        context.bindService(serviceIntent, this, Context.BIND_AUTO_CREATE);
        
        handler.post(updateTask);
        
        //getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
    
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState)
    {
        final View view = inflater.inflate(R.layout.test, container, false);
        
        return createView(view, inflater, savedInstanceState);
    }
    
    /**
     * 
     * @param view
     * @param inflater
     * @return
     */
    private View createView(final View view, final LayoutInflater inflater, final Bundle savedInstanceState) {
        testView = (TestView) view.findViewById(R.id.test_view);
        graphView = (GraphView) view.findViewById(R.id.test_graph);
        infoView = (ViewGroup) view.findViewById(R.id.test_view_info_container);
        textView = (TextView) view.findViewById(R.id.test_text);
        qosProgressView = (ViewGroup) view.findViewById(R.id.test_view_qos_container);
        groupCountContainerView = (ViewGroup) view.findViewById(R.id.test_view_group_count_container);
        
        
        if (savedInstanceState != null) {
        	if (testView != null) {
        		testView.setHeaderString(savedInstanceState.getString("header_string"));
        		testView.setSubHeaderString(savedInstanceState.getString("sub_header_string", testView.getSubHeaderString()));
        		testView.setResultPingString(savedInstanceState.getString("ping_string", testView.getResultPingString()));
        		testView.setResultDownString(savedInstanceState.getString("down_string", testView.getResultDownString()));
        		testView.setResultUpString(savedInstanceState.getString("up_string", testView.getResultUpString()));
        	}
        	
            if (textView != null) {
            	textView.setText(savedInstanceState.getString("test_info"));
            }
        }
        else {
        	if (textView != null) {
        		textView.setText("\n\n\n");
        	}
        }
        
        if (graphView != null)
        {
        	if (speedGraphData == null) {
        		speedGraph = SmoothGraph.addGraph(graphView, Color.parseColor("#00f940"), SMOOTHING_DATA_AMOUNT, SMOOTHING_FUNCTION, false);
        	}
        	else {
        		speedGraph = SmoothGraph.addGraph(graphView, SMOOTHING_DATA_AMOUNT, SMOOTHING_FUNCTION, false, speedGraphData);
        	}
        	
    		speedGraph.setMaxTime(GRAPH_MAX_NSECS);
        	
        	if (signalGraphData == null) {
        		signalGraph = SimpleGraph.addGraph(graphView, Color.parseColor("#f8a000"), GRAPH_MAX_NSECS);
        	}
        	else {
        		signalGraph = SimpleGraph.addGraph(graphView, GRAPH_MAX_NSECS, signalGraphData);
        	}
        	
        	//graphView.getLabelInfoVerticalList().add(new GraphLabel(getActivity().getString(R.string.test_dbm), "#f8a000"));
        	graphView.setRowLinesLabelList(ResultGraphView.SPEED_LABELS);
        }
        //uploadGraph = false;
        graphStarted = false;
        
        final Resources res = getActivity().getResources();
        final String progressTitle = res.getString(R.string.test_progress_title);
        final String progressText = res.getString(R.string.test_progress_text);
        
        lastShownWaitTime = -1;
        if (progressDialog == null) {
        	progressDialog = ProgressDialog.show(getActivity(), progressTitle, progressText, true, false);
        	progressDialog.setOnKeyListener(backKeyListener);
        }
        
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
        dismissDialogs();
        System.gc();
    }
    
    @Override
    public void onDetach()
    {
        super.onDetach();
        dismissDialogs();
    }
    
    private void dismissDialogs()
    {
        dismissDialog(errorDialog);
        errorDialog = null;
        dismissDialog(abortDialog);
        abortDialog = null;
        dismissDialog(progressDialog);
        progressDialog = null;
    }
    
    private static void dismissDialog(Dialog dialog)
    {
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
            
            if (qosMode)
                updateQoSUI();
            else
                updateUI();
            
            if (rmbtService != null) {
//            	System.out.println(rmbtService.isCompleted() + " - " + rmbtService.isConnectionError());
                if (rmbtService.isCompleted() && rmbtService.getTestUuid(false) != null) {
                   	handler.postDelayed(resultSwitcherRunnable, 300);
                }
            }
            
            if (!stopLoop)
                handler.postDelayed(this, UPDATE_DELAY);
        }
    };

    private final DialogInterface.OnKeyListener backKeyListener = new DialogInterface.OnKeyListener()
    {
        @Override
        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event)
        {
            if (keyCode == KeyEvent.KEYCODE_BACK)
                return onBackPressedHandler();
            return false;
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
                if (! rmbtService.isLoopMode())
                {
                    showErrorDialog(R.string.test_dialog_error_control_server_conn);
                    return;
                }
            }
            
            if (!rmbtService.isTestRunning() && updateCounter > MAX_COUNTER_WITHOUT_RESULT && ! (errorDialog != null && errorDialog.isShowing()))
                getActivity().getSupportFragmentManager().popBackStack(); // leave
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
            
            teststatus = lastStatusString;
            
            textView.setText(MessageFormat.format(bottomText, teststatus, lastServerName, lastIP, 
                    locationStr_line1, locationStr_line2));
        }
        
        Integer signal = rmbtService.getSignal();
        int signalType = rmbtService.getSignalType();
        
        if (signal == null || signal == 0)
            signalType = InformationCollector.SINGAL_TYPE_NO_SIGNAL;
        
        boolean signalTypeChanged = false;
        
        if (signalType != InformationCollector.SINGAL_TYPE_NO_SIGNAL)
        {
            signalTypeChanged = lastSignalType != signalType;
            lastSignal = signal;
            lastSignalType = signalType;
        }
        
        if (signalType == InformationCollector.SINGAL_TYPE_NO_SIGNAL && lastSignalType != InformationCollector.SINGAL_TYPE_NO_SIGNAL)
        {
            // keep old signal if we had one before
            signal = lastSignal;
            signalType = lastSignalType;
        }
        
        Double relativeSignal = null;
        MinMax<Integer> signalBounds = NetworkUtil.getSignalStrengthBounds(signalType);
        if (! (signalBounds.min == Integer.MIN_VALUE || signalBounds.max == Integer.MAX_VALUE))
            relativeSignal = (double)(signal - signalBounds.min) / (double)(signalBounds.max - signalBounds.min);
        
        if (signalTypeChanged && graphView != null)
        {
            if (signalGraph != null)
                signalGraph.clearGraphDontResetTime();
            if (relativeSignal != null)
                graphView.setSignalRange(signalBounds.min, signalBounds.max);
            else
                graphView.removeSignalRange();
            graphView.invalidate();
        }
        
        double speedValueRelative = 0d;
        int progressSegments = 0;
        switch (intermediateResult.status)
        {
        case WAIT:
        	textView.setText("UDP progress: " + intermediateResult.progress);
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
                    speedGraph.addValue(speedValueRelative, SmoothGraph.FLAG_USE_CURRENT_NODE_TIME);
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
                    speedGraph.addValue(speedValueRelative, SmoothGraph.FLAG_USE_CURRENT_NODE_TIME);
                    if (relativeSignal != null)
                        signalGraph.addValue(relativeSignal);
                    graphView.invalidate();
                }
            }
            break;
                    
        case SPEEDTEST_END:
        case QOS_TEST_RUNNING:
            progressSegments = PROGRESS_SEGMENTS_INIT + PROGRESS_SEGMENTS_PING + PROGRESS_SEGMENTS_DOWN
                    + PROGRESS_SEGMENTS_UP;
            speedValueRelative = intermediateResult.upBitPerSecLog;
            
            qosMode = true;
        	
            break;
        
        case ERROR:
        case ABORTED:
            progressSegments = 0;
            resetGraph();
            
            if (! rmbtService.isLoopMode())
                showErrorDialog(R.string.test_dialog_error_text);
            return;
        }
        testView.setSpeedValue(speedValueRelative);
        
        testView.setSignalType(signalType);
        if (signal != null)
            testView.setSignalString(String.valueOf(signal));
        if (relativeSignal != null)
            testView.setSignalValue(relativeSignal);
        
        final double progressValue = (double) progressSegments / PROGRESS_SEGMENTS_TOTAL;
        final double correctProgressValue = testView.setProgressValue(progressValue);
        testView.setProgressString(percentFormat.format(correctProgressValue));
        
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
        return onBackPressedHandler();
    }
    
    public boolean onBackPressedHandler()
    {
    	Log.d("RMBTTestFragment", "onbackpressed");
        final RMBTMainActivity activity = (RMBTMainActivity) getActivity();
        if (activity == null)
            return false;
        if ((errorDialog != null && errorDialog.isShowing())
                ||
            (progressDialog != null && progressDialog.isShowing()))
        {
            if (rmbtService != null)
                rmbtService.stopTest(RMBTService.BROADCAST_TEST_ABORTED);
            else
            {
                // to be sure test is stopped:
                final Intent service = new Intent(RMBTService.ACTION_ABORT_TEST, null, context, RMBTService.class);
                context.startService(service);
            }
            dismissDialogs();
            activity.getSupportFragmentManager().popBackStack();
            return true;
        }
        
        
        if (abortDialog != null && abortDialog.isShowing())
        {
            dismissDialog(abortDialog);
            abortDialog = null;
        }
        else
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
                        rmbtService.stopTest(RMBTService.BROADCAST_TEST_ABORTED);
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            });
            builder.setNegativeButton(R.string.test_dialog_abort_no, null);
            
            dismissDialogs();
            abortDialog = builder.show();
        }
        return true;
    }
    
    protected void showErrorDialog(int errorMessageId)
    {
        stopLoop = true;
        
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.test_dialog_error_title);
        builder.setMessage(errorMessageId);
        builder.setNeutralButton(android.R.string.ok, null);
        dismissDialogs();
        errorDialog = builder.create();
        errorDialog.setOnDismissListener(new OnDismissListener()
        {
            @Override
            public void onDismiss(DialogInterface dialog)
            {
                final RMBTMainActivity activity = (RMBTMainActivity) getActivity();
                if (activity != null)
                    activity.getSupportFragmentManager().popBackStack();
            }
        });
        errorDialog.show();
    }
    
    private void switchToResult()
    {
        if (!isVisible()) {
            return;
        }
        
        if (showQoSErrorToast)
        {
            final Toast toast = Toast.makeText(getActivity(), R.string.test_toast_error_text_qos, Toast.LENGTH_LONG);
            toast.show();
        }
        
        dismissDialogs();
        
        final String testUuid = rmbtService == null ? null : rmbtService.getTestUuid(true);
        if (testUuid == null)
        {
        	showErrorDialog(R.string.test_dialog_error_text);
        	return;
        }
        
        ((RMBTMainActivity) getActivity()).showResultsAfterTest(testUuid);
    }
    

    private ProgressView extendedProgressView;
    private Button extendedResultButtonCancel;
    private Button extendedButtonDetails;
    
    private QoSTestEnum lastQoSTestStatus;

    public void updateQoSUI() {
    	
        if (progressDialog != null)
        {
            progressDialog.dismiss();
            progressDialog = null;
        }
        
        QoSTestEnum status = null;
        float progress = 0f;

    	try {
            if (rmbtService != null)
            {
                QoSTestEnum _status = rmbtService.getQoSTestStatus();
                if (_status == null) {
                    _status = lastQoSTestStatus == null ? QoSTestEnum.START : lastQoSTestStatus;	
                }
                status = _status;
                lastQoSTestStatus = status;
                progress = rmbtService.getQoSTestProgress();
            }
            else
            {
//                        Log.d(DEBUG_TAG, "we have no service");
                QoSTestEnum _status = lastQoSTestStatus;
                if (_status == null)
                    _status = QoSTestEnum.START;
                if (_status == QoSTestEnum.QOS_RUNNING)
                    _status = QoSTestEnum.STOP;
                
                status = _status;
            }
            
//            Log.d(" DEBUG TEST", String.format("status: %s", status == null ? "null" : status.toString()));
            
            switch (status)
            {
            case START:
            case ERROR:
                progress = 0f;
                break;
            
            case STOP:
                progress = 1f;
                break;
            }
            
            double progressSegments = 0;
            
            if (extendedProgressView != null) {
                extendedProgressView.setProgress((progress/rmbtService.getQoSTestSize()));
            }

            
            switch (status)
            {
            case START:
                progressSegments = PROGRESS_SEGMENTS_INIT + PROGRESS_SEGMENTS_PING + PROGRESS_SEGMENTS_DOWN +
                	PROGRESS_SEGMENTS_UP + Math.round(PROGRESS_SEGMENTS_QOS * (progress / rmbtService.getQoSTestSize()));
                break;
            
            case QOS_RUNNING:
                progressSegments = PROGRESS_SEGMENTS_INIT + PROGRESS_SEGMENTS_PING + PROGRESS_SEGMENTS_DOWN +
            		PROGRESS_SEGMENTS_UP + Math.round(PROGRESS_SEGMENTS_QOS * (float)(progress / rmbtService.getQoSTestSize()));
                break;

            case QOS_FINISHED:
            	progressSegments = PROGRESS_SEGMENTS_TOTAL - 1;
            	break;
            
            case NDT_RUNNING:
            	progressSegments = PROGRESS_SEGMENTS_TOTAL - 1;
            	break;
            	
            case STOP:
                progressSegments = PROGRESS_SEGMENTS_INIT + PROGRESS_SEGMENTS_PING + PROGRESS_SEGMENTS_DOWN +
            		PROGRESS_SEGMENTS_UP + PROGRESS_SEGMENTS_QOS;
                break;
            
            case ERROR:
            default:
                break;
            }	    

            final double progressValue = (double) progressSegments / PROGRESS_SEGMENTS_TOTAL;
            
            Integer signal = rmbtService.getSignal();
            int signalType = rmbtService.getSignalType();
            
            if (signal == null || signal == 0)
                signalType = InformationCollector.SINGAL_TYPE_NO_SIGNAL;
            
            
            if (signalType != InformationCollector.SINGAL_TYPE_NO_SIGNAL)
            {
                lastSignal = signal;
                lastSignalType = signalType;
            }
            
            if (signalType == InformationCollector.SINGAL_TYPE_NO_SIGNAL && lastSignalType != InformationCollector.SINGAL_TYPE_NO_SIGNAL)
            {
                // keep old signal if we had one before
                signal = lastSignal;
                signalType = lastSignalType;
            }
            
            Double relativeSignal = null;
            MinMax<Integer> signalBoungs = NetworkUtil.getSignalStrengthBounds(signalType);
            if (! (signalBoungs.min == Integer.MIN_VALUE || signalBoungs.max == Integer.MAX_VALUE))
                relativeSignal = (double)(signal - signalBoungs.min) / (double)(signalBoungs.max - signalBoungs.min);

            testView.setSignalType(signalType);
            if (signal != null)
                testView.setSignalString(String.valueOf(signal));
            if (relativeSignal != null)
                testView.setSignalValue(relativeSignal);
            
            final double correctProgressValue = testView.setProgressValue(progressValue);
            testView.setProgressString(percentFormat.format(correctProgressValue));
            testView.invalidate();
            
            if (status == QoSTestEnum.QOS_RUNNING && extendedResultButtonCancel != null && extendedResultButtonCancel.getVisibility() == View.GONE)
            {
                extendedResultButtonCancel.setVisibility(View.VISIBLE);
                if (extendedButtonDetails != null)
                extendedButtonDetails.setVisibility(View.GONE);
            }

            if (qosProgressView != null && qosProgressView.getVisibility()!=View.VISIBLE 
        			&& rmbtService != null && rmbtService.getQoSTest() != null) {
        		final GroupCountView groupCountView = new GroupCountView(getMainActivity());
        		qosProgressView.setVisibility(View.VISIBLE);
        		//register group counter view as a test progress listener:
        		rmbtService.getQoSTest().getTestSettings().addTestProgressListener(groupCountView);
        		groupCountView.setTaskMap(rmbtService.getQoSTest().getTestMap());
        		groupCountContainerView.addView(groupCountView);
        		groupCountContainerView.invalidate();
        		((GroupCountView) groupCountContainerView.getChildAt(0)).setNdtProgress(rmbtService.getNDTProgress());
        		if (graphView != null) {
            		graphView.setVisibility(View.GONE);	
        		}
        		if (infoView != null) {
        			infoView.setVisibility(View.GONE);
        		}
        	}
        	else if (qosProgressView != null 
        			&& qosProgressView.getVisibility()==View.VISIBLE && rmbtService.getQoSGroupCounterMap() != null) {
        		((GroupCountView) groupCountContainerView.getChildAt(0)).setTaskMap(rmbtService.getQoSTest().getTestMap());
        		((GroupCountView) groupCountContainerView.getChildAt(0)).setNdtProgress(rmbtService.getNDTProgress());
        		((GroupCountView) groupCountContainerView.getChildAt(0)).setQoSTestStatus(rmbtService.getQoSTestStatus());
        		((GroupCountView) groupCountContainerView.getChildAt(0)).updateView(rmbtService.getQoSGroupCounterMap());
        	}
            
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    	}
    	finally
    	{
    	    if (status != null && status == QoSTestEnum.ERROR)
    	        showQoSErrorToast = true;
    	}
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
    	super.onSaveInstanceState(outState);
    	if (testView != null) {
        	outState.putString("header_string", testView.getHeaderString());
        	outState.putString("sub_header_string", testView.getSubHeaderString());
        	outState.putString("ping_string", testView.getResultPingString());
        	outState.putString("down_string", testView.getResultDownString());
        	outState.putString("up_string", testView.getResultUpString());
    	}
    	
        if (textView != null) {
        	outState.putString("test_info", textView.getText().toString());
        }
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
    	super.onConfigurationChanged(newConfig);
    	LayoutInflater inflater = LayoutInflater.from(getActivity());
    	populateViewForOrientation(inflater, (ViewGroup) getView());
    }

    private final String OPTION_ON_CREATE_VIEW_CREATE_SPEED_GRAPH = "create_speed_graph";
    private final String OPTION_ON_CREATE_VIEW_CREATE_SIGNAL_GRAPH = "create_signal_graph";
    
    /**
     * 
     * @param inflater
     * @param view
     */
	private void populateViewForOrientation(final LayoutInflater inflater, final ViewGroup view) {
		
		System.out.println("RECREATING INSTANCE FOR ORIENTATION CHANGE");
		
		GroupCountView groupCountView = null;
		if (groupCountContainerView != null && groupCountContainerView.getChildAt(0) != null) {
            groupCountView = (GroupCountView) groupCountContainerView.getChildAt(0);
            groupCountContainerView.removeAllViews();
		}
		
		final String infoText = String.valueOf(textView.getText());
		final String header = testView.getHeaderString();
		
		final String subHeader = testView.getSubHeaderString();
		final String resultPing = testView.getResultPingString();
		final String resultDown = testView.getResultDownString();
		final String resultUp = testView.getResultUpString();
		
		final GraphData signalGraphData;
		final GraphData speedGraphData;
		
		final MinMax<Integer> currentSignalBounds;
		final List<GraphView.GraphLabel> graphLabelList;
		
		if (graphView != null) {
			graphLabelList = graphView.getLabelInfoVerticalList();
			currentSignalBounds = graphView.getSignalRange();
		}
		else {
			graphLabelList = null;
			currentSignalBounds = null;
		}
		
		if (signalGraph != null) {
			signalGraphData = signalGraph.getGraphData();
		}
		else {
			signalGraphData = null;
		}
		
		if (speedGraph != null) {
			speedGraphData = speedGraph.getGraphData();
		}
		else {
			speedGraphData = null;
		}
		
		view.removeAllViewsInLayout();
        final View v = inflater.inflate(R.layout.test, view);

        final Bundle options = new Bundle();
        options.putBoolean(OPTION_ON_CREATE_VIEW_CREATE_SIGNAL_GRAPH, false);
        options.putBoolean(OPTION_ON_CREATE_VIEW_CREATE_SPEED_GRAPH, false);
        
        createView(v, inflater, options);
        
        if (groupCountContainerView != null && groupCountView != null && qosProgressView != null) {
            groupCountContainerView.addView(groupCountView);
            qosProgressView.setVisibility(View.VISIBLE);
            //groupCountContainerView.setVisibility(View.VISIBLE);
            
    		if (graphView != null) {
        		graphView.setVisibility(View.GONE);	
    		}
    		if (infoView != null) {
    			infoView.setVisibility(View.GONE);
    		}
        }
        
        if (testView != null) {
        	testView.setHeaderString(header);
        	testView.setSubHeaderString(subHeader);
        	testView.setResultPingString(resultPing);
        	testView.setResultDownString(resultDown);
        	testView.setResultUpString(resultUp);
        }
        
        if (textView != null) {
        	textView.setText(infoText);
        }
        
        if (graphView != null) {
        	graphView.setRowLinesLabelList(ResultGraphView.SPEED_LABELS);
        	
        	if (signalGraphData != null) {
        		signalGraph = SimpleGraph.addGraph(graphView, GRAPH_MAX_NSECS, signalGraphData);
        		
        		if (currentSignalBounds != null) {
       				graphView.setLabelInfoVerticalList(graphLabelList);
        			graphView.setSignalRange(currentSignalBounds.min, currentSignalBounds.max);
        		}
        	}
        	if (speedGraphData != null) {
        		speedGraph = SmoothGraph.addGraph(graphView, SMOOTHING_DATA_AMOUNT, SMOOTHING_FUNCTION, false, speedGraphData);
        		speedGraph.setMaxTime(GRAPH_MAX_NSECS);
        	}
        }
        
	}
	
	public static double calculateProgress(IntermediateResult result, final float qosTestProgress, final int qosTestSize) {
		double progressSegments = 0;
        switch (result.status)
        {
        case WAIT:
            break;
        
        case INIT:
            progressSegments = Math.round(PROGRESS_SEGMENTS_INIT * result.progress);
            break;
        
        case PING:
            progressSegments = PROGRESS_SEGMENTS_INIT + Math.round(PROGRESS_SEGMENTS_PING * result.progress);
            break;
        
        case DOWN:
            progressSegments = PROGRESS_SEGMENTS_INIT + PROGRESS_SEGMENTS_PING + Math.round(PROGRESS_SEGMENTS_DOWN * result.progress);
            break;
        
        case INIT_UP:
            progressSegments = PROGRESS_SEGMENTS_INIT + PROGRESS_SEGMENTS_PING + PROGRESS_SEGMENTS_DOWN;
            break;
        
        case UP:
            progressSegments = PROGRESS_SEGMENTS_INIT + PROGRESS_SEGMENTS_PING + PROGRESS_SEGMENTS_DOWN
                    + Math.round(PROGRESS_SEGMENTS_UP * result.progress);
            break;
                    
        case SPEEDTEST_END:
            progressSegments = PROGRESS_SEGMENTS_INIT + PROGRESS_SEGMENTS_PING + PROGRESS_SEGMENTS_DOWN + PROGRESS_SEGMENTS_UP;
            break;

        case QOS_TEST_RUNNING:
        	progressSegments = PROGRESS_SEGMENTS_INIT + PROGRESS_SEGMENTS_PING + PROGRESS_SEGMENTS_DOWN +
    		PROGRESS_SEGMENTS_UP + Math.round((PROGRESS_SEGMENTS_QOS-1) * (float)(qosTestProgress / qosTestSize));
        	break;

        case QOS_END:
        	progressSegments = PROGRESS_SEGMENTS_TOTAL - 1;
        	break;
        	
        case ERROR:
        case ABORTED:
            progressSegments = 0;
            break;
		default:
			break;
        }
        return (progressSegments / (double)PROGRESS_SEGMENTS_TOTAL);
	}
}
