/*******************************************************************************
 * Copyright 2013-2015 alladin-IT GmbH
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
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import net.measurementlab.ndt.NdtTests;
import android.content.Context;
import android.location.Location;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.util.Log;
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
import at.alladin.rmbt.client.v2.task.service.TestSettings;

public class RMBTTask
{
    private static final String LOG_TAG = "RMBTTask";
    
    private final AtomicBoolean started = new AtomicBoolean();
    private final AtomicBoolean running = new AtomicBoolean();
    private final AtomicBoolean finished = new AtomicBoolean();
    private final AtomicBoolean cancelled = new AtomicBoolean();
    
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
    private RMBTClient client;
    
    private InformationCollector fullInfo;
    
    private EndTaskListener endTaskListener;
    
    interface EndTaskListener
    {
        public void taskEnded();
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
            boolean error = false;
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
                
                client = RMBTClient.getInstance(controlServer, null, controlPort, controlSSL, geoInfo, uuid,
                        Config.RMBT_CLIENT_TYPE, Config.RMBT_CLIENT_NAME,
                        fullInfo.getInfo("CLIENT_SOFTWARE_VERSION"), null, fullInfo.getInitialInfo());
                
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
                error = true;
            }
            
            if (error || client == null) {
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
                        result = client.runTest();
                    	final ControlServerConnection controlConnection = client.getControlConnection();
                    	
                        if (result != null && ! fullInfo.getIllegalNetworkTypeChangeDetcted()) {
                            client.sendResult(fullInfo.getResultValues(controlConnection.getStartTimeNs()));
                        }
                        else {
                            error = true;
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
                    error = true;
                }
                
                //client.shutdown();
                
                setPreviousTestStatus();
                QualityOfServiceTest qosTest = null;
                
                boolean runQoS = (! ConfigHelper.isSkipQoS(context) && client.getTaskDescList() != null && client.getTaskDescList().size() >= 1);
                    
                //run qos test:
                if (runQoS && !error && !cancelled.get()) {
					try {
						
					    TestSettings qosTestSettings = new TestSettings();
			            qosTestSettings.setCacheFolder(context.getCacheDir());
					    qosTestSettings.setWebsiteTestService(new WebsiteTestServiceImpl(context));
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
						error = true;
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
                        client.setStatus(TestStatus.END);
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
    
    public RMBTClient getRmbtClient() {
    	return client;
    }
}
