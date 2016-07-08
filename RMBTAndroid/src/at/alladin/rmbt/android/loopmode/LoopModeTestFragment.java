/*******************************************************************************
 * Copyright 2016 Specure GmbH
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
 *******************************************************************************/
package at.alladin.rmbt.android.loopmode;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import at.alladin.openrmbt.android.R;
import at.alladin.rmbt.android.loopmode.LoopModeResults.Status;
import at.alladin.rmbt.android.loopmode.info.AdditionalInfoFragment;
import at.alladin.rmbt.android.loopmode.info.DetailsInfoListAdapter;
import at.alladin.rmbt.android.loopmode.info.LoopModeTriggerItem;
import at.alladin.rmbt.android.loopmode.info.LoopModeTriggerItem.TriggerType;
import at.alladin.rmbt.android.loopmode.info.TrafficUsageItem;
import at.alladin.rmbt.android.loopmode.measurement.IpAddressItem;
import at.alladin.rmbt.android.loopmode.measurement.MeasurementDetailsFragment;
import at.alladin.rmbt.android.loopmode.measurement.ServerNameItem;
import at.alladin.rmbt.android.main.RMBTMainActivity;
import at.alladin.rmbt.android.test.RMBTLoopService;
import at.alladin.rmbt.android.test.RMBTLoopService.RMBTLoopBinder;
import at.alladin.rmbt.android.test.RMBTLoopService.RMBTLoopServiceConnection;
import at.alladin.rmbt.android.test.RMBTService;
import at.alladin.rmbt.android.test.RMBTService.RMBTBinder;
import at.alladin.rmbt.android.test.RMBTTestFragment;
import at.alladin.rmbt.android.util.InformationCollector;
import at.alladin.rmbt.client.helper.IntermediateResult;

public class LoopModeTestFragment extends Fragment implements ServiceConnection, RMBTLoopServiceConnection {

	public final static String TAG = "LoopModeStartFragment";
	
	public static final class LoopModeViewHolder {
		private class StringsHolder {
			String testRunningString;
			String loopModeString;
			String waiting;
			String finished;
		}
		
		DetailsInfoListAdapter unimportantListAdapter;
		
		TextView counterText;
		TextView lastTestText;
		MeasurementDetailsFragment measurementFragment;
		AdditionalInfoFragment infoFragment;
		ProgressBar progressBar;
		TextView progressText;
		ListView unimportantInfoList;
		Button cancelButton;
		final StringsHolder strings = new StringsHolder();
	}
	
	private class RMBTServiceConnection implements ServiceConnection {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
	        final RMBTBinder binder = (RMBTBinder) service;
	        Log.d(TAG, "connect RMBT Service");
	        rmbtService = binder.getService();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			Log.d(TAG, "disconnect RMBT Service");
	        rmbtService = null;
		}
		
	}
	
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (RMBTService.BROADCAST_TEST_FINISHED.equals(intent.getAction())) {

            }
        }
    };
	
	Handler handler;
	
	RMBTLoopService loopService;
	
	RMBTService rmbtService;
	RMBTServiceConnection rmbtServiceConnection = new RMBTServiceConnection();
	
	LoopModeViewHolder holder;
	
	IntermediateResult intermediateResult; 
	
	InformationCollector infoCollector;
	
    @Override
    public void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        
        handler = new Handler();
        infoCollector = new InformationCollector(getActivity(), false, false);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	final View v = inflater.inflate(R.layout.loop_mode_test_fragment, container, false);
    	
    	holder = new LoopModeViewHolder();
    	holder.counterText = (TextView) v.findViewById(R.id.loop_test_counter);
    	holder.lastTestText = (TextView) v.findViewById(R.id.loop_test_last_test_status);
    	holder.progressBar = (ProgressBar) v.findViewById(R.id.loop_test_progress_bar);
    	holder.progressText = (TextView) v.findViewById(R.id.loop_test_progress_bar_text);
    	holder.unimportantInfoList = (ListView) v.findViewById(R.id.loop_test_unimportant_info_list);
    	holder.cancelButton = (Button) v.findViewById(R.id.loop_test_close_button);
    	holder.cancelButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				getActivity().onBackPressed();
			}
		});
    	
    	holder.strings.testRunningString = getResources().getString(R.string.loop_test_progress_test_running);
    	holder.strings.loopModeString = getResources().getString(R.string.loop_test_progress_test_not_running);
    	holder.strings.waiting = getResources().getString(R.string.loop_test_progress_test_waiting);
    	holder.strings.finished = getResources().getString(R.string.loop_test_progress_test_finished);

    	holder.infoFragment = AdditionalInfoFragment.newInstance();
    	getChildFragmentManager().beginTransaction().add(R.id.loop_test_details_holder, 
    			holder.infoFragment, "lm_additional_info").commit();
    	
    	holder.measurementFragment = MeasurementDetailsFragment.newInstance();
    	getChildFragmentManager().beginTransaction().add(R.id.loop_test_measurement_holder, 
    			holder.measurementFragment, "lm_measurement").commit();
    	return v;
    }
    
    @Override
    public void onStop()
    {
        super.onStop();

        handler.removeCallbacks(updateTask);
        
        getActivity().unregisterReceiver(receiver);
        getActivity().unbindService(this);
        getActivity().unbindService(rmbtServiceConnection);
        
        Log.d(TAG, "UNBIND LOOP MODE SERVICES");
        
        getActivity().getActionBar().show();
        ((RMBTMainActivity) getActivity()).setLockNavigationDrawer(false);
        
        //getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        if (infoCollector != null) {
        	infoCollector.unload();
        }
    }
    
    @Override
    public void onStart()
    {
        super.onStart();
        
        if (infoCollector != null) {
        	infoCollector.init();
        }
        
        getActivity().getActionBar().hide();
        ((RMBTMainActivity) getActivity()).setLockNavigationDrawer(true);
        
        // Bind to RMBTLoopService
        final Intent loopServiceIntent = new Intent(getActivity(), RMBTLoopService.class);
        getActivity().bindService(loopServiceIntent, this, 0);

        final Intent rmbtServiceIntent = new Intent(getActivity(), RMBTService.class);
        getActivity().bindService(rmbtServiceIntent, rmbtServiceConnection, Context.BIND_AUTO_CREATE);

        final IntentFilter actionFilter = new IntentFilter(RMBTService.BROADCAST_TEST_FINISHED);
        getActivity().registerReceiver(receiver, actionFilter);
        
        Log.d(TAG, "BIND LOOP MODE SERVICES");
        
        handler.post(updateTask);
        
        //getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
    
    @Override
    public void onServiceConnected(final ComponentName name, final IBinder service)
    {
        Log.d(TAG, "loopService connected");
        final RMBTLoopBinder binder = (RMBTLoopBinder) service;
        loopService = binder.getService();
    }
    
    @Override
    public void onServiceDisconnected(final ComponentName name)
    {
        Log.d(TAG, "loopService disconnected");
        loopService = null;
    }


    public final Runnable updateTask = new Runnable() {

    	@Override
		public void run() {
    		updateUi();
    		handler.postDelayed(updateTask, 250);
		}
	};
	
	public void updateUi() {
		if (holder != null && loopService != null) {
			LoopModeResults loopModeResults = loopService.getLoopModeResults();
			holder.counterText.setText(holder.strings.loopModeString + ": " 
					+ loopModeResults.getNumberOfTests() + "/" + loopModeResults.getMaxTests()
					+ (Status.RUNNING.equals(loopModeResults.getStatus()) ? "" : " (" 
					+ (loopService.isActive() ? holder.strings.waiting : holder.strings.finished) + ")"));
			
			if (Status.RUNNING.equals(loopModeResults.getStatus())) {
				loopModeResults = loopService.updateLoopModeResults(true);
				
				if (holder.lastTestText.getVisibility() == View.VISIBLE) {
					//loop mode just switched from idle/waiting state to test mode
					holder.lastTestText.setVisibility(View.GONE);					
					holder.unimportantListAdapter = null;					
				}
				
				if (holder.progressBar.getVisibility() == View.GONE) {
					holder.progressBar.setVisibility(View.VISIBLE);
					holder.progressText.setVisibility(View.VISIBLE);
				}
			}
			else {
				if (holder.lastTestText.getVisibility() != View.VISIBLE) {
					//loop mode just switched from test mode to idle/waiting state
					holder.unimportantListAdapter = null;
					holder.lastTestText.setVisibility(View.VISIBLE);					
				}
				
				if (holder.progressBar.getVisibility() == View.VISIBLE) {
					holder.progressBar.setVisibility(View.GONE);
					holder.progressText.setVisibility(View.GONE);
				}
			}

			//update or initialize the "not so important list" (reason why it's at the bottom of the view)
			if (holder.unimportantListAdapter != null) {
				holder.unimportantListAdapter.notifyDataSetChanged();
			}
			else {
				//different "not so important lists", depending on current test status (running/idle)
				if (Status.RUNNING.equals(loopModeResults.getStatus())) {
					//during test mode, show current test server and client's ip
					final List<DetailsListItem> list = new ArrayList<DetailsListItem>();
					list.add(new ServerNameItem(getActivity(), loopModeResults));
					list.add(new IpAddressItem(getActivity(), loopModeResults));
					holder.unimportantListAdapter = new DetailsInfoListAdapter(getActivity(), list);
					holder.unimportantInfoList.setAdapter(holder.unimportantListAdapter);
				}
				else {
					//during idle mode, show current data usage, and both loop mode trigger statuses
					final List<DetailsListItem> list = new ArrayList<DetailsListItem>();
					list.add(new TrafficUsageItem(getActivity(), loopModeResults));
					list.add(new LoopModeTriggerItem(getActivity(), loopModeResults, TriggerType.MOVEMENT));
					list.add(new LoopModeTriggerItem(getActivity(), loopModeResults, TriggerType.TIME));
					holder.unimportantListAdapter = new DetailsInfoListAdapter(getActivity(), list);
					holder.unimportantInfoList.setAdapter(holder.unimportantListAdapter);
				}
			}
			
			//update last test status
			if (holder.lastTestText != null && holder.lastTestText.getVisibility() == View.VISIBLE) {
				String lastTestResult = "";
				if (loopModeResults != null && loopModeResults.getLastTestResults() != null) {
					final Date d = new Date(loopModeResults.getLastTestResults().getLocalStartTimeStamp());
					final DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, Locale.getDefault());
					switch (loopModeResults.getLastTestResults().getStatus()) {
					case OK:
						lastTestResult = getString(R.string.loop_test_last_status_ok);
						break;
					case REJECTED:
						lastTestResult = getString(R.string.loop_test_last_status_rejected);
						break;
					default:
						lastTestResult = getString(R.string.loop_test_last_status_error);
					}
					lastTestResult += " (" + df.format(d) + ")";
				}
				holder.lastTestText.setText(lastTestResult);
			}
			
			//update measurement list (up/down/ping/qos: last/current test + median values)
			if (holder.measurementFragment != null) {
				if (!holder.measurementFragment.hasResults()) {
					holder.measurementFragment.initList(loopService.getLoopModeResults());
				}
				else {
					holder.measurementFragment.updateList();
				}
			}
			
			//update info list (location, network, etc...)
			if (holder.infoFragment != null) {
				if (!holder.infoFragment.hasResults()) {
					holder.infoFragment.initList(infoCollector);
				}
				else {
					holder.infoFragment.updateList();
				}
			}
			
			//loop service is active (= at least one test still has to be run or finished) 
			if (loopService.isActive()) {
				int progress = 0;
				String progressText = "";
				int maxProgress = 100;
				
				if (rmbtService != null) {
					if (Status.RUNNING.equals(loopModeResults.getStatus())) {
						//test currently running, test mode:
						intermediateResult = rmbtService.getIntermediateResult(intermediateResult);
						if (intermediateResult != null) {
							progress = (int) (RMBTTestFragment.calculateProgress(intermediateResult, rmbtService.getQoSTestProgress(), rmbtService.getQoSTestSize()) * 100d);
							progressText = holder.strings.testRunningString + ": " + progress + "%";
							maxProgress = 100;
						}
						else if (rmbtService.isConnectionError()) {

						}
						else {

						}
					}
					else {
						//no test running, waiting mode:
						progress = loopModeResults.getNumberOfTests();
						progressText = holder.strings.loopModeString + ": " + loopModeResults.getNumberOfTests() + "/" + loopModeResults.getMaxTests();
						maxProgress = loopModeResults.getMaxTests();
					}
					
					holder.progressBar.setMax(maxProgress);
					holder.progressBar.setProgress(progress);
					holder.progressText.setText(progressText);
				}
				else  {
					//connecting?
				}
			}
			else {
				holder.cancelButton.setText(R.string.loop_test_quit);
			}
		}
		else if (holder != null) {
			//service (still) not bound...
		}
	}

    public boolean onBackPressed()
    {
        if (loopService == null) {
            return false;
        }
        
        else if (loopService.isRunning() || (loopService.getLoopModeResults().getMaxTests() > loopService.getLoopModeResults().getNumberOfTests())) {

        	AlertDialog stopDialog = new AlertDialog.Builder(getActivity())
        			.setTitle(R.string.test_dialog_abort_title)
        			.setMessage(R.string.loop_mode_test_dialog_abort)
        			.setPositiveButton(android.R.string.yes, new OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							stopLoopService();
				            ((RMBTMainActivity)getActivity()).popBackStackFull();
						}
					})
        			.setNegativeButton(android.R.string.no, new OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					})
        			.create();
        	
        	stopDialog.setCancelable(false);
        	stopDialog.show();
        }
        else if (loopService.getLoopModeResults().getMaxTests() <= loopService.getLoopModeResults().getNumberOfTests()) {
        	//if max tests is reached simply remove this fragment without alert dialog
        	stopLoopService();
        	return false;
        }
        
        return true;
    }
    
    private void stopLoopService() {
        if (rmbtService != null) {
            rmbtService.stopTest(RMBTService.BROADCAST_TEST_ABORTED);
        }
        else
        {
            // to be sure test is stopped:
            final Intent service = new Intent(RMBTService.ACTION_ABORT_TEST, null, getActivity(), RMBTService.class);
            getActivity().startService(service);
        }

    	((RMBTMainActivity) getActivity()).stopLoopService();
    }    

	@Override
	public RMBTLoopService getRMBTLoopService() {
		return loopService;
	}    
}
