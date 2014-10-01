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

import java.util.Map;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.location.Location;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import at.alladin.openrmbt.android.R;
import at.alladin.rmbt.android.main.RMBTMainActivity;
import at.alladin.rmbt.android.test.RMBTTask.EndTaskListener;
import at.alladin.rmbt.android.util.ConfigHelper;
import at.alladin.rmbt.android.util.InformationCollector;
import at.alladin.rmbt.android.util.NotificationIDs;
import at.alladin.rmbt.client.QualityOfServiceTest;
import at.alladin.rmbt.client.QualityOfServiceTest.Counter;
import at.alladin.rmbt.client.helper.IntermediateResult;
import at.alladin.rmbt.client.helper.NdtStatus;
import at.alladin.rmbt.client.v2.task.QoSTestEnum;
import at.alladin.rmbt.client.v2.task.result.QoSTestResultEnum;

public class RMBTService extends Service implements EndTaskListener
{
    public static String ACTION_START_TEST = "at.alladin.rmbt.android.startTest";
    public static String ACTION_LOOP_TEST = "at.alladin.rmbt.android.loopTest";
    public static String ACTION_ABORT_TEST = "at.alladin.rmbt.android.abortTest";
    public static String BROADCAST_TEST_FINISHED = "at.alladin.rmbt.android.test.RMBTService.testFinished";
    
    private RMBTTask testTask;
    // private InformationCollector fullInfo;
    
    private Handler handler;
    
    private static final String DEBUG_TAG = "RMBTService";
    
    private static WifiManager wifiManager;
    private static WifiLock wifiLock;
    private static WakeLock wakeLock;
    
    private boolean bound = false;
    
    private boolean loopMode;
    
    private static long DEADMAN_TIME = 120 * 1000;
    private final Runnable deadman = new Runnable()
    {
        @Override
        public void run()
        {
            stopTest();
        }
    };
    
    // private BroadcastReceiver mNetworkStateIntentReceiver;
    
    // This is the object that receives interactions from clients. See
    // RemoteService for a more complete example.
    private final IBinder localRMBTBinder = new RMBTBinder();
    private boolean completed = false;
    
    /**
     * Class for clients to access. Because we know this service always runs in
     * the same process as its clients, we don't need to deal with IPC.
     */
    public class RMBTBinder extends Binder
    {
        public RMBTService getService()
        {
            // Return this instance of RMBTService so clients can call public
            // methods
            return RMBTService.this;
        }
    }
    
    @Override
    public void onCreate()
    {
        Log.d(DEBUG_TAG, "created");
        super.onCreate();
        
        handler = new Handler();
        
        // initialise the locks
        loopMode = ConfigHelper.isLoopMode(this);
        
        // initialise the locks
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "RMBTWifiLock");
        wakeLock = ((PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE)).newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK, "RMBTWakeLock");
        
        // mNetworkStateIntentReceiver = new BroadcastReceiver() {
        // @Override
        // public void onReceive(Context context, Intent intent) {
        // if
        // (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION))
        // {
        //
        // final boolean connected = !
        // intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY,
        // false);
        // if (! connected)
        // stopTest();
        // }
        // }
        // };
        // final IntentFilter networkStateChangedFilter = new IntentFilter();
        // networkStateChangedFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        //
        // registerReceiver(mNetworkStateIntentReceiver,
        // networkStateChangedFilter);
    }
    
    @Override
    public void onDestroy()
    {
        Log.d(DEBUG_TAG, "destroyed");
        super.onDestroy();
        
        if (testTask != null)
        {
            Log.d(DEBUG_TAG, "RMBTTest stopped by onDestroy");
            testTask.cancel();
        }
        
        removeNotification();
        unlock();
        if (testTask != null)
        {
            testTask.cancel();
            testTask = null;
        }
        
        handler.removeCallbacks(addNotificationRunnable);
        handler.removeCallbacks(deadman);
        
        // unregisterReceiver(mNetworkStateIntentReceiver);
    }
    
    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId)
    {
        String action = null;
        if (intent != null)
            action = intent.getAction();
        
        Log.i(DEBUG_TAG, "onStartCommand; action="+action);
        
        if (ACTION_ABORT_TEST.equals(action))
        {
            Log.i(DEBUG_TAG, "ACTION_ABORT_TEST received");
            stopTest();
            return START_NOT_STICKY;
        }
        
        if (ACTION_START_TEST.equals(action) || ACTION_LOOP_TEST.equals(action))
        {
            if (testTask != null && testTask.isRunning())
            {
                if (ACTION_LOOP_TEST.equals(action)) // do not cancel test if running in loop mode 
                    return START_STICKY;
                testTask.cancel(); // otherwise cancel
            }

            completed = false;
            
            // lock wifi + power
            lock();
            testTask = new RMBTTask(getApplicationContext());
            
            testTask.setEndTaskListener(this);
            testTask.execute(handler);
            Log.d(DEBUG_TAG, "RMBTTest started");
            
            handler.postDelayed(addNotificationRunnable, 200);
            handler.postDelayed(deadman, DEADMAN_TIME);
            
            return START_STICKY;
        }
        return START_NOT_STICKY;
    }
    
    public void stopTest()
    {
        
        if (testTask != null)
        {
            Log.d(DEBUG_TAG, "RMBTTest stopped");
            testTask.cancel();
            taskEnded();
        }
    }
    
    public boolean isTestRunning()
    {
        return testTask != null && testTask.isRunning();
    }
    
    private void addNotificationIfTestRunning()
    {
        if (isTestRunning() && ! bound)
        {
            final Resources res = getResources();
            
            final PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, new Intent(
                    getApplicationContext(), RMBTMainActivity.class), 0);
            
            final Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.drawable.stat_icon_test)
                .setContentTitle(res.getText(R.string.test_notification_title))
                .setContentText(res.getText(R.string.test_notification_text))
                .setTicker(res.getText(R.string.test_notification_ticker))
                .setContentIntent(contentIntent)
                .getNotification();
            
            startForeground(NotificationIDs.TEST_RUNNING, notification);
        }
    }
    
    private void removeNotification()
    {
        handler.removeCallbacks(addNotificationRunnable);
        stopForeground(true);
    }
    
    @Override
    public IBinder onBind(final Intent intent)
    {
        bound = true;
        return localRMBTBinder;
    }
    
    private final Runnable addNotificationRunnable = new Runnable()
    {
        @Override
        public void run()
        {
            addNotificationIfTestRunning();
        }
    };
    
    @Override
    public boolean onUnbind(final Intent intent)
    {
        bound = false;
        handler.postDelayed(addNotificationRunnable, 200);
        return true;
    }
    
    @Override
    public void onRebind(final Intent intent)
    {
        bound = true;
        removeNotification();
    }
    
    public IntermediateResult getIntermediateResult(final IntermediateResult result)
    {
        if (testTask != null)
            return testTask.getIntermediateResult(result);
        else
            return null;
    }
    
    public boolean isConnectionError()
    {
        if (testTask != null)
            return testTask.isConnectionError();
        else
            return false;
    }

    public Integer getSignal()
    {
        if (testTask != null)
            return testTask.getSignal();
        else
            return null;
    }
    
    public int getSignalType()
    {
        if (testTask != null)
            return testTask.getSignalType();
        else
            return InformationCollector.SINGAL_TYPE_NO_SIGNAL;
    }
    
    public String getOperatorName()
    {
        if (testTask != null)
            return testTask.getOperatorName();
        else
            return null;
    }
    
    public int getNetworkType()
    {
        if (testTask != null)
            return testTask.getNetworkType();
        else
            return 0;
    }
    
    public Location getLocation()
    {
        if (testTask != null)
            return testTask.getLocation();
        else
            return null;
    }
    
    public String getServerName()
    {
        if (testTask != null)
            return testTask.getServerName();
        else
            return null;
    }
    
    // protected Status getStatus()
    // {
    // if (testTask != null)
    // return testTask.getStatus();
    // else
    // return null;
    // }
    
    public String getIP()
    {
        if (testTask != null)
            return testTask.getIP();
        else
            return null;
    }
    
    public String getTestUuid()
    {
        if (testTask != null) {
            return testTask.getTestUuid();
        }
        else {
            return ConfigHelper.getLastTestUuid(getApplicationContext(), true);
        }
    }
    
    public float getNDTProgress()
    {
        if (testTask != null)
            return testTask.getNDTProgress();
        else
            return 0;
    }
    
    public NdtStatus getNdtStatus()
    {
        if (testTask != null)
            return testTask.getNdtStatus();
        else
            return null;
    }

    /**
     * 
     * @return
     */
    public float getQoSTestProgress()
    {
        if (testTask != null)
            return testTask.getQoSTestProgress();
        else
            return 0;
    }
    
    /**
     * 
     * @return
     */
    public QualityOfServiceTest getQoSTest() {
    	if (testTask != null) {
    		return testTask.getQoSTest();
    	}
    	
    	return null;
    }

    /**
     * 
     * @return
     */
    public int getQoSTestSize() {
        if (testTask != null)
            return testTask.getQoSTestSize();
        else
            return 0;    	
    }
    
    /**
     * 
     * @return
     */
    public QoSTestEnum getQoSTestStatus() {
        if (testTask != null)
            return testTask.getQoSTestStatus();
        else
            return null;    	
    }
    
    /**
     * 
     * @return
     */
    public Map<QoSTestResultEnum, Counter> getQoSGroupCounterMap() {
    	if (testTask != null) {
    		return testTask.getQoSGroupCounterMap();
    	}
    	else {
    		return null;
    	}
    }
    
    public void lock()
    {
        try
        {
            if (!wakeLock.isHeld())
                wakeLock.acquire();
            if (!wifiLock.isHeld())
                wifiLock.acquire();
            
            Log.d(DEBUG_TAG, "Lock");
        }
        catch (final Exception e)
        {
            Log.e(DEBUG_TAG, "Error getting Lock: " + e.getMessage());
        }
    }
    
    public static void unlock()
    {
        if (wakeLock != null && wakeLock.isHeld())
            wakeLock.release();
        if (wifiLock != null && wifiLock.isHeld())
            wifiLock.release();

        Log.d(DEBUG_TAG, "Unlock");
    }
    
    @Override
    public void taskEnded()
    {
        unlock();
        removeNotification();
        handler.removeCallbacks(deadman);
        completed = true;
        sendBroadcast(new Intent(BROADCAST_TEST_FINISHED));
        stopSelf();
        if (testTask != null) {
        	ConfigHelper.setLastTestUuid(getApplicationContext(), testTask.getTestUuid());
        }
        Log.i("RMBTService", "stopped!");
    }
    
    public boolean isCompleted()
    {
        return completed;
    }
    
    public void runNdt() {
    	if (testTask != null) {
    		testTask.runNDT();
    	}
    }
    
    public boolean isLoopMode()
    {
        return loopMode;
    }
}
