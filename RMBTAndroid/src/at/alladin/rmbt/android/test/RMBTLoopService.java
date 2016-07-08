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

import java.text.MessageFormat;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.json.JSONArray;
import org.json.JSONException;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
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
import at.alladin.rmbt.android.loopmode.LoopModeCurrentTest;
import at.alladin.rmbt.android.loopmode.LoopModeLastTestResults;
import at.alladin.rmbt.android.loopmode.LoopModeLastTestResults.RMBTLoopFetchingResultsStatus;
import at.alladin.rmbt.android.loopmode.LoopModeLastTestResults.RMBTLoopLastTestStatus;
import at.alladin.rmbt.android.loopmode.LoopModeResults;
import at.alladin.rmbt.android.loopmode.LoopModeResults.Status;
import at.alladin.rmbt.android.loopmode.LoopModeResults.TrafficStats;
import at.alladin.rmbt.android.loopmode.info.LoopModeTriggerItem;
import at.alladin.rmbt.android.main.AppConstants;
import at.alladin.rmbt.android.main.RMBTMainActivity;
import at.alladin.rmbt.android.test.RMBTService.RMBTBinder;
import at.alladin.rmbt.android.util.CheckTestResultDetailTask;
import at.alladin.rmbt.android.util.CheckTestResultTask;
import at.alladin.rmbt.android.util.ConfigHelper;
import at.alladin.rmbt.android.util.EndTaskListener;
import at.alladin.rmbt.android.util.GeoLocation;
import at.alladin.rmbt.android.util.NotificationIDs;
import at.alladin.rmbt.android.views.ResultDetailsView.ResultDetailType;
import at.alladin.rmbt.client.helper.IntermediateResult;
import at.alladin.rmbt.client.helper.TestStatus;
import at.alladin.rmbt.client.v2.task.service.TestMeasurement;
import at.alladin.rmbt.util.model.shared.exception.ErrorStatus;

public class RMBTLoopService extends Service implements ServiceConnection
{
    private static final String TAG = "RMBTLoopService";
    
    private static final boolean SHOW_DEV_BUTTONS = false;
    
    private WakeLock partialWakeLock;
    private WakeLock dimWakeLock;
    	
    final LoopModeResults loopModeResults = new LoopModeResults();

    public static interface RMBTLoopServiceConnection {
    	RMBTLoopService getRMBTLoopService();
    }
    
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
                loopModeResults.setLastDistance(distance);
                loopModeResults.setLastAccuracy(curLocation.getAccuracy());
                Log.d(TAG, "location distance: " + distance + "; maxMovement: " + loopModeResults.getMaxMovement());
                onAlarmOrLocation(false);    
            }
            lastLocation = curLocation;
        }
    }
    
    private static final String ACTION_ALARM = "at.alladin.rmbt.android.Alarm";
    private static final String ACTION_WAKEUP_ALARM = "at.alladin.rmbt.android.WakeupAlarm";
    public static final String ACTION_STOP = "at.alladin.rmbt.android.Stop";
    public static final String ACTION_START = "at.alladin.rmbt.android.Start";
    private static final String ACTION_FORCE = "at.alladin.rmbt.android.Force";
    
    public static final String BROADCAST_QOS_RESULT_FETCHED = "at.alladin.rmbt.android.test.RMBTLoopService.qosResultFetched"; 
    public static final String BROADCAST_TEST_RESULT_FETCHED = "at.alladin.rmbt.android.test.RMBTLoopService.testResultFetched";
    
    private static final long ACCEPT_INACCURACY = 1000; // accept 1 sec inaccuracy
    
    private final RMBTLoopBinder localBinder = new RMBTLoopBinder();
    
    private AlarmManager alarmManager;
    private PendingIntent alarm;
    private PendingIntent wakeupAlarm;
    private NotificationManager notificationManager;
    private NotificationCompat.Builder notificationBuilder;
    
    private LocalGeoLocation geoLocation;
    
    private AtomicBoolean isRunning = new AtomicBoolean();
    
    private Location lastLocation;
    private Location lastTestLocation;
        
    private RMBTService rmbtService;

    private AtomicBoolean isActive = new AtomicBoolean(false);
    
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent)
        {
        	Log.d(TAG, "Received intent: " +  intent.getAction());
            if (RMBTService.BROADCAST_TEST_FINISHED.equals(intent.getAction()))
            {
            	System.out.println("BROADCAST TEST FINISHED ERROR: " + rmbtService.getError());
            	updateLoopModeResults(false);
                if (loopModeResults.getMaxTests() == loopModeResults.getNumberOfTests()) {
                	setFinishedNotification();
                	isActive.set(false);
                }
                
            	isRunning.set(false);
                onAlarmOrLocation(false);
            }
            else if (RMBTService.BROADCAST_TEST_ABORTED.equals(intent.getAction())) {
            	isRunning.set(false);
            	onStartCommand(new Intent(ACTION_STOP), 0, 0);
            }
        }
    };
    
    private BroadcastReceiver rmbtTaskReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d(TAG, "RMBTTask Intent: " + intent.getAction());
		}
    	
    };
    
	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
        final RMBTBinder binder = (RMBTBinder) service;
        rmbtService = binder.getService();
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
        rmbtService = null;
	}
    
    @Override
    public IBinder onBind(Intent intent)
    {
        return localBinder;
    }
    
    public void triggerTest()
    {
        loopModeResults.setStatus(Status.RUNNING);
        isRunning.set(true);
        loopModeResults.setNumberOfTests(loopModeResults.getNumberOfTests()+1);
        lastTestLocation = lastLocation;
        loopModeResults.setLastDistance(0);
        loopModeResults.setLastTestTime(SystemClock.elapsedRealtime());
        final Intent service = new Intent(RMBTService.ACTION_LOOP_TEST, null, this, RMBTService.class);
        ConfigHelper.setLoopModeTestCounter(getApplicationContext(), loopModeResults.getNumberOfTests());
        startService(service);
        updateNotification();
    }

    private void readConfig()
    {
        loopModeResults.setMinDelay((long) (ConfigHelper.getLoopModeMinDelay(this) * 1000));
        loopModeResults.setMaxDelay((long) (ConfigHelper.getLoopModeMaxDelay(this) * 1000 * AppConstants.LOOP_MODE_TIME_MOD));
        loopModeResults.setMaxMovement(ConfigHelper.getLoopModeMaxMovement(this));
        loopModeResults.setMaxTests(ConfigHelper.getLoopModeMaxTests(this));
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
        final IntentFilter actionFilter = new IntentFilter(RMBTService.BROADCAST_TEST_FINISHED);
        actionFilter.addAction(RMBTService.BROADCAST_TEST_ABORTED);
        registerReceiver(receiver, actionFilter);
        
        final IntentFilter rmbtTaskActionFilter = new IntentFilter(RMBTTask.BROADCAST_TEST_START);
        registerReceiver(rmbtTaskReceiver, rmbtTaskActionFilter);
        
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
        
        bindService(new Intent(getApplicationContext(), RMBTService.class), this, BIND_AUTO_CREATE);
    }
    
    private void setAlarm(long millis)
    {
        Log.d(TAG, "setAlarm: " + millis);
        
        final long now = SystemClock.elapsedRealtime();
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, now + millis, alarm);
    }
    
    private void stopAlarm() {
        if (alarmManager != null) {
            alarmManager.cancel(alarm);
            alarmManager.cancel(wakeupAlarm);
        }
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.d(TAG, "onStartCommand: " + intent);
        
        readConfig();
        if (intent != null)
        {
            final String action = intent.getAction();
            if (ACTION_START.equals(action)) {
            	isActive.set(true);
            }
            else if (action != null && action.equals(ACTION_STOP)) {
            	stopAlarm();
            	loopModeResults.setStatus(Status.IDLE);
            	isActive.set(false);
            	stopForeground(true);
                stopSelf();
            }

            if (isActive.get()) {
	            if (action != null && action.equals(ACTION_FORCE))
	                onAlarmOrLocation(true);
	            else if (action != null && action.equals(ACTION_ALARM))
	                onAlarmOrLocation(false);
	            else if (action != null && action.equals(ACTION_WAKEUP_ALARM))
	                onWakeup();
	            else
	            {
	                if (loopModeResults.getLastTestTime() == 0)
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
            else {
            	
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
    	if (!isActive.get()) return;
    	
        updateNotification();
        final long now = SystemClock.elapsedRealtime();
        final long lastTestDelta = now - loopModeResults.getLastTestTime();
        
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
            
            long delay = loopModeResults.getMaxDelay();
            if (loopModeResults.getLastDistance() >= loopModeResults.getMaxMovement() 
            		&& loopModeResults.getLastAccuracy() > 0.0f 
            		&& loopModeResults.getLastAccuracy() <= AppConstants.LOOP_MODE_GPS_ACCURACY_CRITERIA)
            {
                Log.d(TAG, "lastDistance >= maxMovement; triggerTest");
                delay = loopModeResults.getMinDelay();
            }
        
            if (lastTestDelta + ACCEPT_INACCURACY >= delay)
            {
                Log.d(TAG, "accept delay (" + delay + "); triggerTest");
                run = true;
            }
        }
        
        if (run)
            triggerTest();
        
        if (loopModeResults.getMaxTests() == 0 || loopModeResults.getNumberOfTests() <= loopModeResults.getMaxTests()) {
            setAlarm(10000);
        }
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
        unbindService(this);
        Log.d(TAG, "destroyed");
        super.onDestroy();
        unregisterReceiver(receiver);
        unregisterReceiver(rmbtTaskReceiver);
        if (geoLocation != null) {
            geoLocation.stop();
        }
        stopAlarm();
    }
    
    private NotificationCompat.Builder createNotificationBuilder()
    {
        final Resources res = getResources();
        
        Intent notificationIntent = new Intent(getApplicationContext(), RMBTMainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent openAppIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, 0);
        
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
            .setSmallIcon(R.drawable.stat_icon_loop)
            .setContentTitle(res.getText(R.string.loop_notification_title))
            .setTicker(res.getText(R.string.loop_notification_ticker))
            .setContentIntent(openAppIntent);
        
        setNotificationText(builder);
                
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
        {
            if (SHOW_DEV_BUTTONS) {
            	final Intent stopIntent = new Intent(ACTION_STOP, null, getApplicationContext(), getClass());
           		final PendingIntent stopPIntent = PendingIntent.getService(getApplicationContext(), 0, stopIntent, 0);
                final Intent forceIntent = new Intent(ACTION_FORCE, null, getApplicationContext(), getClass());
                final PendingIntent forcePIntent = PendingIntent.getService(getApplicationContext(), 0, forceIntent, 0);

            	addActionToNotificationBuilder(builder, stopPIntent, forcePIntent);
            }
        }
        
        return builder;
    }
    
    public Intent getStopAction() {
    	return new Intent(ACTION_STOP, null, getApplicationContext(), getClass());
    }
    
    private NotificationCompat.BigTextStyle bigTextStyle;
    
    private void setNotificationText(NotificationCompat.Builder builder)
    {
        final Resources res = getResources();
        
        final long now = SystemClock.elapsedRealtime();
        final long lastTestDelta = loopModeResults.getLastTestTime() == 0 ? 0 : now - loopModeResults.getLastTestTime();
        
        final String elapsedTimeString = LoopModeTriggerItem.formatSeconds(Math.round(lastTestDelta / 1000), 1);
        
        final CharSequence textTemplate = res.getText(R.string.loop_notification_text_without_stop);
        final CharSequence text = MessageFormat.format(textTemplate.toString(), 
        		loopModeResults.getNumberOfTests(), elapsedTimeString, Math.round(loopModeResults.getLastDistance()));
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
    
    private void setFinishedNotification() {
        Intent notificationIntent = new Intent(getApplicationContext(), RMBTMainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent openAppIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, 0);
        
    	final Resources res = getResources();
    	final Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.stat_icon_loop)
                .setContentTitle(res.getText(R.string.loop_notification_finished_title))
                .setTicker(res.getText(R.string.loop_notification_finished_ticker))
                .setContentIntent(openAppIntent)
                .build();

        //notification.flags |= Notification.FLAG_AUTO_CANCEL;

    	notificationManager.notify(NotificationIDs.LOOP_ACTIVE, notification);
    }
    
    public boolean isRunning() {
    	return isRunning.get();
    }

	public Location getLastTestLocation() {
		return lastTestLocation;
	}

	public boolean isActive() {
		return isActive.get();
	}
	
	public LoopModeResults getLoopModeResults() {
		return loopModeResults;
	}
	
	public LoopModeResults updateLoopModeResults(boolean isDuringTest) {
    	if (rmbtService != null && isActive()) {
    		final TrafficStats ts = new TrafficStats(0,0);
	    	if (rmbtService.getTrafficMeasurementMap() != null && rmbtService.getTrafficMeasurementMap().size() > 0) {
	    		for (final TestMeasurement tm : rmbtService.getTrafficMeasurementMap().values()) {
	    			ts.add(new TrafficStats(tm.getTxBytes(), tm.getRxBytes()));
	    		}
    		}
	    	
    		IntermediateResult intermediateResult = loopModeResults.getCurrentTest().getResult();
    		
			final LoopModeLastTestResults lastTestResults = new LoopModeLastTestResults();
			
			if (isDuringTest) {
    			//during a test:
    			//update current traffic stats
    			loopModeResults.setStatus(Status.RUNNING);
    			final LoopModeCurrentTest test = loopModeResults.getCurrentTest();
    			test.setResult(rmbtService.getIntermediateResult(intermediateResult));
    			test.setIp(rmbtService.getIP());
    			test.setServerName(rmbtService.getServerName());
    			test.setStartTimeMillis(rmbtService.getStartTimeMillis());

    			loopModeResults.setCurrentTrafficStats(ts);
    		}
    		else {
    			//after test:
    			//update all test results
    			loopModeResults.setStatus(Status.IDLE);
    			
    			if((intermediateResult = rmbtService.getIntermediateResult(intermediateResult)) != null) {
    				//if test could be started (= intermediate results are available):
	    			final TestStatus testStatus = intermediateResult.status;
	    			
	    			switch (testStatus) {
	    			case ABORTED:
	    			case ERROR:	    				
	    				lastTestResults.setStatus(RMBTLoopLastTestStatus.ERROR);
	    			default:
	    				lastTestResults.setStatus(LoopModeLastTestResults.RMBTLoopLastTestStatus.OK);
	    			}
	
	    			if (lastTestResults.getStatus().equals(LoopModeLastTestResults.RMBTLoopLastTestStatus.OK)) {
		    			final String testUuid = rmbtService == null ? null : rmbtService.getTestUuid(true);
		    	        if (testUuid == null) {
		    	        	//last test uuid not available = test results cannot be fetched
		    	        	lastTestResults.setStatus(LoopModeLastTestResults.RMBTLoopLastTestStatus.TEST_RESULTS_MISSING_UUID);
		    	        }
		    	        else {
		    	        	lastTestResults.setTestUuid(testUuid);
		    	        	try {
			    	        	//test uuid available: fetch test results
		    	        		final CheckTestResultTask testResultTask = new CheckTestResultTask(getApplicationContext());
		    	        		testResultTask.setEndTaskListener(new EndTaskListener() {
									
									@Override
									public void taskEnded(JSONArray result) {
										//got test results. read open-test-uuid and fetch test details and qos results
										boolean hasError = testResultTask.hasError();
										
										//if result is null or has no length something went wrong
										if (result == null || result.length() == 0) {
											hasError = true;
										}
										
										//try to get the open test uuid
										String openTestUuid = null;
										if (!hasError) {
											try {
												openTestUuid = result.getJSONObject(0).optString("open_test_uuid");
												lastTestResults.setOpenTestUuid(openTestUuid);
											} catch (Exception e) {
												e.printStackTrace();
												hasError = true;
											}
										}
										
										//if error occurred or the open test uuid is not set then set the last test result to error 
										if (hasError || openTestUuid == null) {
											lastTestResults.setTestResultStatus(RMBTLoopFetchingResultsStatus.ERROR);
											lastTestResults.setQosResultStatus(RMBTLoopFetchingResultsStatus.ERROR);
											lastTestResults.setStatus(RMBTLoopLastTestStatus.ERROR);
											return;
										}
										
										//everything went right until now. fetching opendata and qos results is allowed
					        			final CheckTestResultDetailTask testResultDetailTask = new CheckTestResultDetailTask(getApplicationContext(),
					        					ResultDetailType.OPENDATA);
					        			testResultDetailTask.setEndTaskListener(new EndTaskListener() {				        				
											
											@Override
											public void taskEnded(JSONArray result) {
												lastTestResults.setTestResults(result);
												if (lastTestResults.getTestResultStatus() == RMBTLoopFetchingResultsStatus.OK) {
													try {
														final long pingNs = (long) (lastTestResults.getTestResults().getDouble("ping_ms") * 1e6);
														final long uploadKbit = lastTestResults.getTestResults().getLong("upload_kbit");
														final long downloadKbit = lastTestResults.getTestResults().getLong("download_kbit");
														loopModeResults.updateMedians(pingNs, uploadKbit, downloadKbit);
														RMBTLoopService.this.sendBroadcast(new Intent(BROADCAST_TEST_RESULT_FETCHED));
													} catch (JSONException e) {
														e.printStackTrace();
														lastTestResults.setTestResultStatus(RMBTLoopFetchingResultsStatus.ERROR);
													}
												}
											}
										});
					        			testResultDetailTask.execute(lastTestResults.getOpenTestUuid());
					        			
					        			final CheckTestResultDetailTask qosResultTask = new CheckTestResultDetailTask(getApplicationContext(), 
					        					ResultDetailType.QUALITY_OF_SERVICE_TEST);
					        			
					        			qosResultTask.setEndTaskListener(new EndTaskListener() {
											
											@Override
											public void taskEnded(JSONArray result) {
												lastTestResults.setQoSResult(result);
												RMBTLoopService.this.sendBroadcast(new Intent(BROADCAST_QOS_RESULT_FETCHED));
												if (lastTestResults.getQosResultStatus() == RMBTLoopFetchingResultsStatus.OK) {
													System.out.println("GOT QOS RESULTS...");
													System.out.println(lastTestResults.getQosResult().getQoSStatistics().toString());
												}
											}
										});
					        			
					        			qosResultTask.execute(lastTestResults.getTestUuid());
									}
								});
		    	        		
		    	        		//after setting everything up we can start the result task:
		    	        		testResultTask.execute(lastTestResults.getTestUuid());
		    	        	}
		    	        	catch (Exception e) {
		    	        		e.printStackTrace();
		    	        		lastTestResults.setStatus(RMBTLoopLastTestStatus.ERROR);
		    	        	}
		    	        }
	    			}
	
	    			loopModeResults.updateTrafficStats(ts);
	    		}
    			else {
    				//no intermediate results found, some pre-speedtest error occurred (connection error? test rejected?)
    				final Set<ErrorStatus> errorSet = rmbtService.getErrorStatusList();
    				RMBTLoopLastTestStatus lastStatus = RMBTLoopLastTestStatus.ERROR;
    				if (errorSet != null) {
    					if (errorSet.contains(ErrorStatus.TEST_REJECTED)) {
    						lastStatus = RMBTLoopLastTestStatus.REJECTED;
    					}
    				}
    				
    				lastTestResults.setStatus(lastStatus);
    			}
    			
    			//finally add a "last test result" object to the loop mode result
    			loopModeResults.setLastTestResults(lastTestResults);
    		}
    	}
    	
    	return loopModeResults;
	}
}
