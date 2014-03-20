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
package at.alladin.rmbt.android.test;

import java.util.ArrayList;
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
import at.alladin.rmbt.client.RMBTClient;
import at.alladin.rmbt.client.TestResult;
import at.alladin.rmbt.client.helper.ControlServerConnection;
import at.alladin.rmbt.client.helper.IntermediateResult;
import at.alladin.rmbt.client.helper.NdtStatus;
import at.alladin.rmbt.client.helper.TestStatus;
import at.alladin.rmbt.client.ndt.NDTRunner;

public class RMBTTask
{
    private static final String LOG_TAG = "RMBTTask";
    
    private final AtomicBoolean started = new AtomicBoolean();
    private final AtomicBoolean running = new AtomicBoolean();
    private final AtomicBoolean finished = new AtomicBoolean();
    private final AtomicBoolean cancelled = new AtomicBoolean();
    
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
    
    final private Context ctx;
    
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
        this.ctx = ctx;
    }
    
    public void execute(final Handler _handler)
    {
        fullInfo = new InformationCollector(ctx);
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
                doInBackground();
                running.set(false);
                finished.set(true);
                if (handler != null)
                    handler.post(postExecuteHandler);
            }
        });
    }
    
    public void cancel()
    {
        setPreviousTestStatus();
        cancelled.set(true);
        synchronized (holdNdtLock)
        {
            holdNdtLock.notify();
        }
        executor.shutdownNow();
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
        ConfigHelper.setPreviousTestStatus(ctx, statusString);
    }
    
    private void doInBackground()
    {
        try
        {
            boolean error = false;
            connectionError.set(false);
            try
            {
                final String uuid = fullInfo.getUUID();
                
                final String controlServer = ConfigHelper.getControlServerName(ctx);
                final int controlPort = ConfigHelper.getControlServerPort(ctx);
                final boolean controlSSL = ConfigHelper.isControlSeverSSL(ctx);
                
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
            
            if (client == null)
                connectionError.set(true);
            else
            {
                if (client.getStatus() != TestStatus.ERROR)
                {
                    
                    TestResult result;
                    try
                    {
                        result = client.runTest();
                        if (result != null && ! fullInfo.getIllegalNetworkTypeChangeDetcted()) {
                        	final ControlServerConnection controlConnection = client.getControlConnection();
                            client.sendResult(fullInfo.getResultValues(controlConnection.getStartTimeNs()));
                        }
                        else
                            error = true;
                    }
                    catch (final Exception e)
                    {
                        e.printStackTrace();
                    }
                }
                else
                {
                    System.err.println(client.getErrorMsg());
                    error = true;
                }
                
                client.shutdown();
                
                setPreviousTestStatus();
                
                if (!error && !cancelled.get() && ConfigHelper.isNDT(ctx))
                {
                    if (! ConfigHelper.isLoopMode(ctx))
                        synchronized (holdNdtLock)
                        {
                            while (holdNdt)
                                holdNdtLock.wait();
                        }
                    if (! cancelled.get())
                        runNDT();
                }
            }

        }
        catch (final InterruptedException e)
        {
            Thread.currentThread().interrupt();
        }
    }
    
    private boolean holdNdt = true;
    private final Object holdNdtLock = new Object();
    private final AtomicReference<NDTRunner> ndtRunnerHolder = new AtomicReference<NDTRunner>();
    
    public void letNDTStart()
    {
        synchronized (holdNdtLock)
        {
            if (holdNdt)
            {
                holdNdt = false;
                holdNdtLock.notify();
            }
        }
    }
    
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
    
    private void runNDT()
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
}
