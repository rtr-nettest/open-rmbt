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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.util.Log;
import at.alladin.rmbt.android.impl.TracerouteAndroidImpl;
import at.alladin.rmbt.android.util.Config;
import at.alladin.rmbt.android.util.ConfigHelper;
import at.alladin.rmbt.android.util.InformationCollector;
import at.alladin.rmbt.client.QualityOfServiceTest;
import at.alladin.rmbt.client.QualityOfServiceTest.Counter;
import at.alladin.rmbt.client.RMBTClient;
import at.alladin.rmbt.client.TestResult;
import at.alladin.rmbt.client.helper.ControlServerConnection;
import at.alladin.rmbt.client.helper.IntermediateResult;
import at.alladin.rmbt.client.helper.NdtStatus;
import at.alladin.rmbt.client.helper.TestStatus;
import at.alladin.rmbt.client.ndt.NDTRunner;
import at.alladin.rmbt.client.v2.task.QoSTestEnum;
import at.alladin.rmbt.client.v2.task.result.QoSResultCollector;
import at.alladin.rmbt.client.v2.task.result.QoSTestResultEnum;
import at.alladin.rmbt.client.v2.task.service.TestMeasurement;
import at.alladin.rmbt.client.v2.task.service.TestSettings;
import at.alladin.rmbt.client.v2.task.service.TrafficService;
import at.alladin.rmbt.util.model.shared.exception.ErrorStatus;
import net.measurementlab.ndt.NdtTests;

public class RMBTTask
{
    private static final String LOG_TAG = "RMBTTask";

    public static enum RMBTTaskError {
    	NONE,
    	CONNECTION_ERROR,
    	SPEEDTEST_ERROR,
    	QOS_ERROR,
    	OTHER
    }
    
    public static final String BROADCAST_TEST_REQUEST = "at.alladin.rmbt.android.test.RMBTTask.testRequest";
    public static final String BROADCAST_TEST_START = "at.alladin.rmbt.android.test.RMBTTask.testStart";
    
    private final AtomicBoolean started = new AtomicBoolean();
    private final AtomicBoolean running = new AtomicBoolean();
    private final AtomicBoolean finished = new AtomicBoolean();
    private final AtomicBoolean cancelled = new AtomicBoolean();
    private final AtomicReference<RMBTTaskError> error = new AtomicReference<RMBTTask.RMBTTaskError>(RMBTTaskError.NONE);
    
    private final AtomicReference<QualityOfServiceTest> qosReference = new AtomicReference<QualityOfServiceTest>();
    
    private Handler handler;
    private final Runnable postExecuteHandler = new Runnable()
    {
        @Override
        public void run()
        {
            if (fullInfo != null)
            {
                fullInfo.unload();
                fullInfo = null;
            }
            if (endTaskListener != null)
                endTaskListener.taskEnded();
        }
    };;
    
    final private Context context;
    
    private final ExecutorService executor = Executors.newCachedThreadPool();
    
    private final AtomicBoolean connectionError = new AtomicBoolean();
    
    private final AtomicReference<Set<ErrorStatus>> errorStatusList = new AtomicReference<Set<ErrorStatus>>();
    
    private RMBTClient client;
    
    private InformationCollector fullInfo;
    
    private EndTaskListener endTaskListener;
    
    interface EndTaskListener
    {
        public void taskEnded(final int...flag);
    }
    
    public RMBTTask(final Context ctx)
    {
        this.context = ctx;
    }
    
    public void execute(final Handler _handler)
    {
        fullInfo = new InformationCollector(context, true, true);
        cancelled.set(false);
        started.set(true);
        running.set(true);
        finished.set(false);
        
        handler = _handler;
        executor.execute(new Runnable()
        {
            @Override
            public void run()
            {
                Log.d(LOG_TAG, "executor task started");
                doInBackground();
                Log.d(LOG_TAG, "doInBackground finished");
                running.set(false);
                finished.set(true);
                if (handler != null)
                    handler.post(postExecuteHandler);
                Log.d(LOG_TAG, "executor task finished");
            }
        });
    }
    
    public void cancel()
    {
        setPreviousTestStatus();
        cancelled.set(true);
        executor.shutdownNow();
        Log.d(LOG_TAG, "shutdownNow called RMBTTask="+this);
//        try
//        {
//            executor.awaitTermination(10, TimeUnit.SECONDS);
//        }
//        catch (InterruptedException e)
//        {
//            Thread.currentThread().interrupt();
//        }
    }
    
    public boolean isFinished()
    {
        return finished.get();
    }
    
    public boolean isRunning()
    {
        return running.get() && ! cancelled.get();
    }
    
    private void setPreviousTestStatus()
    {
        final TestStatus status;
        if (client == null)
            status = null;
        else
            status = client.getStatus();
        
        final String statusString;
        if (status == TestStatus.ERROR)
        {
            final TestStatus statusBeforeError = client.getStatusBeforeError();
            if (statusBeforeError != null)
                statusString = "ERROR_" + statusBeforeError.toString();
            else
                statusString = "ERROR";
        }
        else if (status != null)
            statusString = status.toString();
        else
            statusString = null;
        
        System.out.println("test status at end: " + statusString);
        ConfigHelper.setPreviousTestStatus(context, statusString);
    }
        
    private void doInBackground()
    {
        try
        {
        	context.sendBroadcast(new Intent(BROADCAST_TEST_REQUEST));
            boolean hasError = false;

            connectionError.set(false);
        	TestResult result = null;
        	QoSResultCollector qosResult = null;

            try
            {
                final String uuid = fullInfo.getUUID();
                
                final String controlServer = ConfigHelper.getControlServerName(context);
                final int controlPort = ConfigHelper.getControlServerPort(context);
                final boolean controlSSL = ConfigHelper.isControlSeverSSL(context);
                
                final ArrayList<String> geoInfo = fullInfo.getCurLocation();
                
                final Set<ErrorStatus> errorSet = new HashSet<ErrorStatus>();
                client = RMBTClient.getInstance(controlServer, null, controlPort, controlSSL, geoInfo, uuid,
                        Config.RMBT_CLIENT_TYPE, Config.RMBT_CLIENT_NAME,
                        fullInfo.getInfo("CLIENT_SOFTWARE_VERSION"), null, fullInfo.getInitialInfo(), errorSet);
                
                if (errorSet.size() > 0) { 
                	System.out.println(errorSet);
                	errorStatusList.set(errorSet);
                }
                
                if (client != null)
                {
                	client.setTrafficService(new TrafficServiceImpl());
                    final ControlServerConnection controlConnection = client.getControlConnection();
                    if (controlConnection != null)
                    {
                   		fullInfo.setUUID(controlConnection.getClientUUID());
                   		fullInfo.setTestServerName(controlConnection.getServerName());
                    }
                }
            }
            catch (final Exception e)
            {
                e.printStackTrace();
                hasError = true;
            }
            
            if (hasError || client == null) {
            	error.set(RMBTTaskError.CONNECTION_ERROR);
                connectionError.set(true);
            }
            else
            {

                if (client.getStatus() != TestStatus.ERROR)
                {
                    try
                    {
                    	if (Thread.interrupted() || cancelled.get())
                    	    throw new InterruptedException();
                    	Log.d(LOG_TAG, "runTest RMBTTask="+this);
                    	context.sendBroadcast(new Intent(BROADCAST_TEST_START));
                        result = client.runTest();
                    	final ControlServerConnection controlConnection = client.getControlConnection();
                    	
                        if (result != null && ! fullInfo.getIllegalNetworkTypeChangeDetcted()) {
                            client.sendResult(fullInfo.getResultValues(controlConnection.getStartTimeNs()));
                            
                        }
                        else {
                            hasError = true;
                        }
                    }
                    catch (final Exception e)
                    {
                        e.printStackTrace();
                    }
                    finally
                    {
                        client.shutdown();
                    }
                }
                else
                {
                    System.err.println(client.getErrorMsg());
                    hasError = true;
                }
                
                if (hasError) {
                	error.set(RMBTTaskError.SPEEDTEST_ERROR);
                }
                
                //client.shutdown();
                
                setPreviousTestStatus();
                QualityOfServiceTest qosTest = null;
                
                boolean runQoS = (! ConfigHelper.isSkipQoS(context) && client.getTaskDescList() != null && client.getTaskDescList().size() >= 1);
                    
                //run qos test:
                if (runQoS && !hasError && !cancelled.get()) {
					try {
						
					    TestSettings qosTestSettings = new TestSettings();
			            qosTestSettings.setCacheFolder(context.getCacheDir());
					    qosTestSettings.setWebsiteTestService(new WebsiteTestServiceImpl(context));
					    qosTestSettings.setTracerouteServiceClazz(TracerouteAndroidImpl.class);
					    qosTestSettings.setTrafficService(new TrafficServiceImpl());
						qosTestSettings.setStartTimeNs(getRmbtClient().getControlConnection().getStartTimeNs());
						qosTestSettings.setUseSsl(ConfigHelper.isQoSSeverSSL(context));
						
						qosTest = new QualityOfServiceTest(client, qosTestSettings);
                        qosReference.set(qosTest);
                        client.setStatus(TestStatus.QOS_TEST_RUNNING);
                        qosResult = qosTest.call();
                        InformationCollector.qoSResult = qosResult;

						if (!cancelled.get()) {
                            if (qosResult != null && !qosTest.getStatus().equals(QoSTestEnum.ERROR)) {
                            	client.sendQoSResult(qosResult);
                            }
                    	}
                    	
					} catch (Exception e) {
						e.printStackTrace();
						hasError = true;
						error.set(RMBTTaskError.QOS_ERROR);
					}                            	                    	
                }
                
                if (qosTest != null && !cancelled.get() && qosTest.getStatus().equals(QoSTestEnum.QOS_FINISHED)) {
                    if (ConfigHelper.isNDT(context)) {
                    	qosTest.setStatus(QoSTestEnum.NDT_RUNNING);
                    	runNDT();
                    }
                    qosTest.setStatus(QoSTestEnum.STOP);
                }
            }
        }
        catch (final Exception e)
        {
            client.setStatus(TestStatus.ERROR);
        	e.printStackTrace();
            Thread.currentThread().interrupt();
        }
        finally
        {
            try
            {
                if (client != null)
                {
                    final TestStatus status = client.getStatus();
                    if (! (status == TestStatus.ABORTED || status == TestStatus.ERROR))
                    {
                        client.setStatus(TestStatus.END);
                    }
                    else {
                    	error.set(RMBTTaskError.OTHER);
                    }
                }
            }
            catch (Exception e)
            {}
        }
    }
    
    private final AtomicReference<NDTRunner> ndtRunnerHolder = new AtomicReference<NDTRunner>();
    
    public float getNDTProgress()
    {
        final NDTRunner ndtRunner = ndtRunnerHolder.get();
        if (ndtRunner == null)
            return 0;
        return ndtRunner.getNdtProgress();
    }
    
    public NdtStatus getNdtStatus()
    {
        final NDTRunner ndtRunner = ndtRunnerHolder.get();
        if (ndtRunner == null)
            return null;
        return ndtRunner.getNdtStatus();
    }
    
    public void stopNDT()
    {
        final NDTRunner ndtRunner = ndtRunnerHolder.get();
        if (ndtRunner != null)
            ndtRunner.setNdtCacelled(true);
    }
    
    public void runNDT()
    {
        final NDTRunner ndtRunner = new NDTRunner();
        ndtRunnerHolder.set(ndtRunner);
        
        Log.d(LOG_TAG, "ndt status RUNNING");
        
        final String ndtNetworkType;
        final int networkType = getNetworkType();
        switch (networkType)
        {
        case InformationCollector.NETWORK_WIFI:
            ndtNetworkType = NdtTests.NETWORK_WIFI;
            break;
        
        case TelephonyManager.NETWORK_TYPE_UNKNOWN:
            ndtNetworkType = NdtTests.NETWORK_UNKNOWN;
            break;
        
        default:
            ndtNetworkType = NdtTests.NETWORK_MOBILE;
            break;
        }
        
        ndtRunner.runNDT(ndtNetworkType, ndtRunner.new UiServices()
        {
            
            @Override
            public void sendResults()
            {
                client.getControlConnection().sendNDTResult(this, null);
            }
            
            public boolean wantToStop()
            {
                if (super.wantToStop())
                    return true;
                
                if (cancelled.get())
                {
                    cancel();
                    return true;
                }
                return false;
            }
        });
    }
    
    /**
     * 
     * @return
     */
    public float getQoSTestProgress()
    {
        final QualityOfServiceTest nnTest = qosReference.get();
        if (nnTest == null)
            return 0;
        return nnTest.getProgress();
    }
    
    /**
     * 
     * @return
     */
    public int getQoSTestSize() {
        final QualityOfServiceTest nnTest = qosReference.get();
        if (nnTest == null)
            return 0;
        return nnTest.getTestSize();    	
    }
    
    /**
     * 
     * @return
     */
    public QualityOfServiceTest getQoSTest() {
    	return qosReference.get();
    }
    
    /**
     * 
     * @return
     */
    public QoSTestEnum getQoSTestStatus()
    {
    	final QualityOfServiceTest nnTest = qosReference.get();
        if (nnTest == null)
            return null;
        return nnTest.getStatus();
    }
    
    /**
     * 
     * @return
     */
    public Map<QoSTestResultEnum, Counter> getQoSGroupCounterMap() {
    	final QualityOfServiceTest nnTest = qosReference.get();
        if (nnTest == null)
            return null;
        return nnTest.getTestGroupCounterMap();    	
    }
    
    public void setEndTaskListener(final EndTaskListener endTaskListener)
    {
        this.endTaskListener = endTaskListener;
    }
    
    public Integer getSignal()
    {
        if (fullInfo != null)
            return fullInfo.getSignal();
        else
            return null;
    }
    
    public int getSignalType()
    {
        if (fullInfo != null)
            return fullInfo.getSignalType();
        else
            return InformationCollector.SINGAL_TYPE_NO_SIGNAL;
    }
    
    public IntermediateResult getIntermediateResult(final IntermediateResult result)
    {
        if (client == null)
            return null;
        return client.getIntermediateResult(result);
    }
    
    public boolean isConnectionError()
    {
        return connectionError.get();
    }
    
    public boolean isCancelled() {
    	return cancelled.get();
    }
    
    public RMBTTaskError getError() {
    	return error.get();
    }
    
    public String getOperatorName()
    {
        if (fullInfo != null)
            return fullInfo.getOperatorName();
        else
            return null;
    }
    
    public Location getLocation()
    {
        if (fullInfo != null)
            return fullInfo.getLastLocation();
        else
            return null;
    }
    
    public String getServerName()
    {
        if (fullInfo != null)
            return fullInfo.getTestServerName();
        else
            return null;
    }
    
    public String getIP()
    {
        if (client != null)
            return client.getPublicIP();
        else
            return null;
    }
    
    public String getTestUuid()
    {
        if (cancelled.get() || connectionError.get())
            return null;
        if (client != null)
            return client.getTestUuid();
        else
            return null;
    }
    
    public int getNetworkType()
    {
        if (fullInfo != null)
        {
            final int networkType = fullInfo.getNetwork();
            if (fullInfo.getIllegalNetworkTypeChangeDetcted())
            {
                Log.e(LOG_TAG, "illegal network change detected; cancelling test");
                cancel();
            }
            return networkType;
        }
        else
            return 0;
    }
    
    public long getStartTimeMillis() {
    	return client != null ? client.getStartTimeMillis() : 0;
    }
    
    public RMBTClient getRmbtClient() {
    	return client;
    }
    
    public TrafficService getSpeedTestTrafficService() {
    	if (client != null) {
    		return client.getTrafficService();
    	}
    	return null;
    }

    public TrafficService getQoSTrafficService() {
    	if (qosReference.get() != null) {
    		return qosReference.get().getTestSettings().getTrafficService();
    	}
    	return null;
    }
    
    public Map<TestStatus, TestMeasurement> getTrafficMeasurementMap() {
    	if (client != null) return client.getTrafficMeasurementMap();
    	return null;
    }
    
    public InformationCollector getInformationCollector() {
    	return fullInfo;
    }

	public Set<ErrorStatus> getErrorStatusList() {
		return errorStatusList.get();
	}
}
