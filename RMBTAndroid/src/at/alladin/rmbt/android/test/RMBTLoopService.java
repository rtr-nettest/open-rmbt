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

import java.text.MessageFormat;
import java.util.concurrent.atomic.AtomicBoolean;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;
import at.alladin.openrmbt.android.R;
import at.alladin.rmbt.android.util.ConfigHelper;
import at.alladin.rmbt.android.util.GeoLocation;
import at.alladin.rmbt.android.util.NotificationIDs;

public class RMBTLoopService extends Service
{
    private static final String TAG = "RMBTLoopService";
    
    private WakeLock partialWakeLock;
    private WakeLock dimWakeLock;
    
    public class RMBTLoopBinder extends Binder
    {
        public RMBTLoopService getService()
        {
            return RMBTLoopService.this;
        }
    }
    
    private class LocalGeoLocation extends GeoLocation
    {
        public LocalGeoLocation(Context ctx)
        {
            super(ctx, ConfigHelper.isLoopModeGPS(ctx), 1000, 5);
        }

        @Override
        public void onLocationChanged(Location curLocation)
        {
            if (lastTestLocation != null)
            {
                final float distance = curLocation.distanceTo(lastTestLocation);
                lastDistance = distance;
                Log.d(TAG, "location distance: " + distance + "; maxMovement: " + maxMovement);
                onAlarmOrLocation(false);    
            }
            lastLocation = curLocation;
        }
    }
    
    private static final String ACTION_ALARM = "at.alladin.rmbt.android.Alarm";
    private static final String ACTION_WAKEUP_ALARM = "at.alladin.rmbt.android.WakeupAlarm";
    private static final String ACTION_STOP = "at.alladin.rmbt.android.Stop";
    private static final String ACTION_FORCE = "at.alladin.rmbt.android.Force";
    
    private static final long ACCEPT_INACCURACY = 1000; // accept 1 sec inaccuracy
    
    private final RMBTLoopBinder localBinder = new RMBTLoopBinder();
    
    private AlarmManager alarmManager;
    private PendingIntent alarm;
    private PendingIntent wakeupAlarm;
    private NotificationManager notificationManager;
    private NotificationCompat.Builder notificationBuilder;
    
    private LocalGeoLocation geoLocation;

    private int numberOfTests;
    
    private AtomicBoolean isRunning = new AtomicBoolean();
    
    private Location lastLocation;
    private Location lastTestLocation;
    private long lastTestTime; // SystemClock.elapsedRealtime()
    private float lastDistance;
    
    private long minDelay;
    private long maxDelay;
    private float maxMovement;
    private int maxTests;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            if (RMBTService.BROADCAST_TEST_FINISHED.equals(intent.getAction()))
            {
                isRunning.set(false);
                onAlarmOrLocation(false);
            }
        }
    };
    
    @Override
    public IBinder onBind(Intent intent)
    {
        return localBinder;
    }
    
    public void triggerTest()
    {
        numberOfTests++;
        lastTestLocation = lastLocation;
        lastDistance = 0;
        lastTestTime = SystemClock.elapsedRealtime();
        final Intent service = new Intent(RMBTService.ACTION_LOOP_TEST, null, this, RMBTService.class);
        isRunning.set(true);
        startService(service);
        
        updateNotification();
    }

    private void readConfig()
    {
        minDelay = ConfigHelper.getLoopModeMinDelay(this) * 1000;
        maxDelay = ConfigHelper.getLoopModeMaxDelay(this) * 1000;
        maxMovement = ConfigHelper.getLoopModeMaxMovement(this);
        maxTests = ConfigHelper.getLoopModeMaxTests(this);
    }
    
    @Override
    public void onCreate()
    {
        Log.d(TAG, "created");
        super.onCreate();
        
        partialWakeLock = ((PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE)).newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK, "RMBTLoopWakeLock");
        partialWakeLock.acquire();
        
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        
        readConfig();
        
        geoLocation = new LocalGeoLocation(this);
        geoLocation.start();
        
        notificationBuilder = createNotificationBuilder();
        
        startForeground(NotificationIDs.LOOP_ACTIVE, notificationBuilder.build());
        registerReceiver(receiver , new IntentFilter(RMBTService.BROADCAST_TEST_FINISHED));
        
        final Intent alarmIntent = new Intent(ACTION_ALARM, null, this, getClass());
        alarm = PendingIntent.getService(this, 0, alarmIntent, 0);
        
        if (ConfigHelper.isLoopModeWakeLock(this))
        {
            Log.d(TAG, "using dimWakeLock");
            dimWakeLock = ((PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE)).newWakeLock(
                    PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "RMBTLoopDimWakeLock");
            dimWakeLock.acquire();
        
            final Intent wakeupAlarmIntent = new Intent(ACTION_WAKEUP_ALARM, null, this, getClass());
            wakeupAlarm = PendingIntent.getService(this, 0, wakeupAlarmIntent, 0);
            
            final long now = SystemClock.elapsedRealtime();
            alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, now + 10000, 10000, wakeupAlarm);
        }
    }
    
    private void setAlarm(long millis)
    {
        Log.d(TAG, "setAlarm: " + millis);
        
        final long now = SystemClock.elapsedRealtime();
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, now + millis, alarm);
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.d(TAG, "onStartCommand: " + intent);
        
        readConfig();
        if (intent != null)
        {
            final String action = intent.getAction();
            if (action != null && action.equals(ACTION_STOP))
                stopSelf();
            else if (action != null && action.equals(ACTION_FORCE))
                onAlarmOrLocation(true);
            else if (action != null && action.equals(ACTION_ALARM))
                onAlarmOrLocation(false);
            else if (action != null && action.equals(ACTION_WAKEUP_ALARM))
                onWakeup();
            else
            {
                if (lastTestTime == 0)
                {
                    Toast.makeText(this, R.string.loop_started, Toast.LENGTH_LONG).show();
                    onAlarmOrLocation(true);
                }
                else
                {
                    Toast.makeText(this, R.string.loop_already_active, Toast.LENGTH_LONG).show();
                    onAlarmOrLocation(true);
                }
            }
        }
        return START_NOT_STICKY;
    }
    
    @SuppressLint("Wakelock")
    private void onWakeup()
    {
        if (dimWakeLock != null)
        {
            if (dimWakeLock.isHeld())
                dimWakeLock.release();
            dimWakeLock.acquire();
        }
    }
    
    private void onAlarmOrLocation(boolean force)
    {
        updateNotification();
        final long now = SystemClock.elapsedRealtime();
        final long lastTestDelta = now - lastTestTime;
        
        Log.d(TAG, "onAlarmOrLocation; force:" + force);
        
        if (isRunning.get())
        {
            if (lastTestDelta >= RMBTService.DEADMAN_TIME)
            {
                Log.e(TAG, "still running after " + lastTestDelta + " - assuming crash");
                isRunning.set(false); // assume crash and carry on
            }
            else
            {
                setAlarm(10000); // check again in 10s
                return;
            }
        }
        
        boolean run = false;

        if (force)
            run = true;
        
        if (! run)
        {
            Log.d(TAG, "lastTestDelta: " + lastTestDelta);
            
            long delay = maxDelay;
            if (lastDistance >= maxMovement)
            {
                Log.d(TAG, "lastDistance >= maxMovement; triggerTest");
                delay = minDelay;
            }
        
            if (lastTestDelta + ACCEPT_INACCURACY >= delay)
            {
                Log.d(TAG, "accept delay (" + delay + "); triggerTest");
                run = true;
            }
        }
        
        if (run)
            triggerTest();
        
        if (maxTests == 0 || numberOfTests < maxTests)
            setAlarm(10000);
        else
            stopSelf();

        //setAlarm(delay - lastTestDelta);
    }

    @Override
    public void onDestroy()
    {
        if (partialWakeLock != null && partialWakeLock.isHeld())
            partialWakeLock.release();
        if (dimWakeLock != null && dimWakeLock.isHeld())
            dimWakeLock.release();
        Log.d(TAG, "destroyed");
        super.onDestroy();
        unregisterReceiver(receiver);
        stopForeground(true);
        if (geoLocation != null)
            geoLocation.stop();
        if (alarmManager != null)
        {
            alarmManager.cancel(alarm);
            alarmManager.cancel(wakeupAlarm);
        }
    }
    
    private NotificationCompat.Builder createNotificationBuilder()
    {
        final Resources res = getResources();
        
        
        
//        final PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, new Intent(
//                getApplicationContext(), RMBTMainActivity.class), 0);
        
        final Intent stopIntent = new Intent(ACTION_STOP, null, getApplicationContext(), getClass());
        final PendingIntent stopPIntent = PendingIntent.getService(getApplicationContext(), 0, stopIntent, 0);
        
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
            .setSmallIcon(R.drawable.stat_icon_loop)
            .setContentTitle(res.getText(R.string.loop_notification_title))
            .setTicker(res.getText(R.string.loop_notification_ticker))
            .setContentIntent(stopPIntent);
        
        setNotificationText(builder);
                
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
        {
            final Intent forceIntent = new Intent(ACTION_FORCE, null, getApplicationContext(), getClass());
            final PendingIntent forcePIntent = PendingIntent.getService(getApplicationContext(), 0, forceIntent, 0);
            addActionToNotificationBuilder(builder, stopPIntent, forcePIntent);
            
        }
        
        return builder;
    }
    
    private NotificationCompat.BigTextStyle bigTextStyle;
    
    private void setNotificationText(NotificationCompat.Builder builder)
    {
        final Resources res = getResources();
        
        final long now = SystemClock.elapsedRealtime();
        final long lastTestDelta = lastTestTime == 0 ? 0 : now - lastTestTime;
        
        final CharSequence textTemplate = res.getText(R.string.loop_notification_text);
        final CharSequence text = MessageFormat.format(textTemplate.toString(), numberOfTests, Math.round(lastTestDelta / 1000), Math.round(lastDistance));
        builder.setContentText(text);
        
        if (bigTextStyle == null)
        {
            bigTextStyle = (new NotificationCompat.BigTextStyle());
            builder.setStyle(bigTextStyle);
        }
        
        bigTextStyle.bigText(text);
    }
    
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private static void addActionToNotificationBuilder(NotificationCompat.Builder builder, PendingIntent stopIntent, PendingIntent forceIntent)
    {
        builder.addAction(android.R.drawable.ic_menu_delete, "stop", stopIntent);
        builder.addAction(android.R.drawable.ic_media_play, "force", forceIntent);
    }
    
    private void updateNotification()
    {
        setNotificationText(notificationBuilder);
        final Notification notification = notificationBuilder.build();
        notificationManager.notify(NotificationIDs.LOOP_ACTIVE, notification);
    }
}
