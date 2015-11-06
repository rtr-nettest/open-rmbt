/*******************************************************************************
 * Copyright 2015 alladin-IT GmbH
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
package at.alladin.rmbt.android.views;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import at.alladin.openrmbt.android.R;
import at.alladin.rmbt.android.util.ConfigHelper;
import at.alladin.rmbt.client.QualityOfServiceTest;
import at.alladin.rmbt.client.QualityOfServiceTest.Counter;
import at.alladin.rmbt.client.v2.task.AbstractQoSTask;
import at.alladin.rmbt.client.v2.task.QoSTestEnum;
import at.alladin.rmbt.client.v2.task.result.QoSTestResultEnum;
import at.alladin.rmbt.client.v2.task.service.TestProgressListener;

/**
 * 
 * @author lb
 *
 */
public class GroupCountView extends LinearLayout implements TestProgressListener {

	/**
	 * max test time in ms
	 */
	public final static int TEST_MAX_TIME = 3000;
	
	/**
	 * highest value for an unfinished test (0-1)
	 */
	public final static float MAX_VALUE_UNFINISHED_TEST = 0.9f;
	
	Map<QoSTestResultEnum, Counter> counterMap;
	Map<QoSTestResultEnum, View> viewMap = new HashMap<QoSTestResultEnum, View>();
	Map<QoSTestResultEnum, List<AbstractQoSTask>> taskMap = new HashMap<QoSTestResultEnum, List<AbstractQoSTask>>();
	float ndtProgress = -1f;
	View ndtView;
	QoSTestEnum status;
	
	Handler handler = new Handler();
	
	public GroupCountView(Context context) {
		super(context);
		
		setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		setOrientation(LinearLayout.VERTICAL);
		createView((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE), true);
	}
	
	/**
	 * 
	 * @param taskMap
	 */
	public void setTaskMap(Map<QoSTestResultEnum, List<AbstractQoSTask>> taskMap) {
		this.taskMap = taskMap;
	}
	
	/**
	 * 
	 * @param ndtProgress
	 */
	public void setNdtProgress(float ndtProgress) {
		this.ndtProgress = ndtProgress;
	}

	/**
	 * 
	 * @param status
	 */
	public void setQoSTestStatus(QoSTestEnum status) {
		this.status = status;
	}
	
	/**
	 * 
	 * @param inflater
	 * @param isInitializing
	 * @return
	 */
	public View createView(final LayoutInflater inflater, boolean isInitializing)
    {       
		try {
			if (!isInitializing && counterMap != null && counterMap.keySet()!= null) {
				removeAllViews();
				Iterator<QoSTestResultEnum> keys = counterMap.keySet().iterator();
				while (keys.hasNext()) {
			    	LinearLayout l = new LinearLayout(getContext());
			    	l.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			    	l.setOrientation(LinearLayout.HORIZONTAL);

					QoSTestResultEnum testType = keys.next();
					View viewLeft = createSubView(inflater, testType, counterMap.get(testType));
					viewMap.put(testType, viewLeft);
					l.addView(viewLeft);
					
					addView(l);
				}
				
				if (ConfigHelper.isNDT(getContext())) {
			    	LinearLayout l = new LinearLayout(getContext());
			    	l.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			    	l.setOrientation(LinearLayout.HORIZONTAL);

			    	ndtView = createNdtView(inflater);
					l.addView(ndtView);
					
					addView(l);					
				}
			}
			else {
				removeAllViews();
		    	LinearLayout l = (LinearLayout) inflate(getContext(), R.layout.test_view_qos_group_init, null);
		    	l.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		    	addView(l);
			}
	    	
		} catch (Exception e) {
			e.printStackTrace();
		}
    	return this;
    }
	
	/**
	 * 
	 * @param inflater
	 * @param testType
	 * @param counter
	 * @return
	 */
	private View createSubView(LayoutInflater inflater, QoSTestResultEnum testType, Counter counter) {
		View view = null;
    	view = inflater.inflate(R.layout.test_view_qos_group_counter, null);
    	view.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, .5f));
    	TextView title = (TextView) view.findViewById(R.id.test_view_qos_groupname);
    	TextView progress = (TextView) view.findViewById(R.id.test_view_qos_progress);
		title.setText(ConfigHelper.getCachedQoSNameByTestType(testType, getContext()));
		progress.setText("(" + counter.value + "/" + counter.target + ")");
		System.out.println("adding new subview with title=" + title.getText() + " and progress=" + progress.getText());
    	return view;
	}
	
	/**
	 * 
	 * @param inflater
	 * @return
	 */
	private View createNdtView(LayoutInflater inflater) {
		View view = null;
    	view = inflater.inflate(R.layout.test_view_qos_group_counter, null);
    	view.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, .5f));
    	TextView title = (TextView) view.findViewById(R.id.test_view_qos_groupname);
    	TextView progress = (TextView) view.findViewById(R.id.test_view_qos_progress);
		title.setText("NDT");
		progress.setText("0%");
		System.out.println("adding new subview with title=" + title.getText() + " and progress=" + progress.getText());
    	return view;		
	}
	
	/**
	 * 
	 * @param counterMap
	 */
	public void updateView(Map<QoSTestResultEnum, Counter> counterMap) {
		//if (counterMap != null && counterMap.keySet() != null) {
			Iterator<QoSTestResultEnum> keys = counterMap.keySet().iterator();
			if ((viewMap == null || getChildCount() <= 1 || (viewMap.size() == 0  && status.equals(QoSTestEnum.QOS_RUNNING))) && counterMap != null) {
				//init and create view if empty:
				this.counterMap = counterMap;
				LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				createView(layoutInflater, false);
			}
			
			if (ConfigHelper.isNDT(getContext()) && ndtProgress >= 0f && ndtView != null) {
		    	TextView progress = (TextView) ndtView.findViewById(R.id.test_view_qos_progress);
		    	ProgressBar progressBar = (ProgressBar) ndtView.findViewById(R.id.test_view_qos_progress_bar);
		    	progress.setText((int)(ndtProgress * 100) + "%");
				progressBar.setProgress((int) (ndtProgress * 100));
			}
			
			while (keys.hasNext()) {
				QoSTestResultEnum key = keys.next();
				View view = viewMap.get(key);
				if (view != null) {
					Counter counter = counterMap.get(key);
					final int value = counter.value;
					if (value > 0) {
						updateProgressBar(key);
					}
					
					/*
					 * calculate current test group progress
					 */
					float testGroupProgress = 0f;					
					long currentTs = System.nanoTime(); 

					List<AbstractQoSTask> taskList = taskMap.get(key);
					if (taskList != null && value < counter.target) {
						for (AbstractQoSTask task : taskList) {
							if (task.hasStarted()) {
								int runningMs = (int) (task.getRelativeDurationNs(currentTs) / 1000000);
								//System.out.println("TASKMAP - RUNNING MS: " +runningMs+ "/" + TEST_MAX_TIME +  "  counter:" + counter.target);
								if (runningMs >= ((float)TEST_MAX_TIME * MAX_VALUE_UNFINISHED_TEST) && !task.hasFinished()) {
									testGroupProgress += ((1f / (float)counter.target) * MAX_VALUE_UNFINISHED_TEST);
								}
								else if (!task.hasFinished()) {
									testGroupProgress += ((1f / (float)counter.target) * (runningMs / (float)TEST_MAX_TIME));
								}
							}
						}
						
						testGroupProgress += ((float)value / (float)counter.target);
						testGroupProgress *= 100f;
					}
					else if (value == counter.target) {
						testGroupProgress = 100f;
					}
					else {
						System.out.println("NO TASKMAP FOUND: " + key);
					}
					
			    	TextView progress = (TextView) view.findViewById(R.id.test_view_qos_progress);
			    	ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.test_view_qos_progress_bar);
					//progress.setText("(" + value + "/" + counter.target + ") - " + testGroupProgress + "%");
			    	progress.setText("(" + value + "/" + counter.target + ")");
					progressBar.setProgress((int) testGroupProgress);
					if (value == counter.target) {
						viewMap.remove(key);
						ImageView image = (ImageView) view.findViewById(R.id.test_view_qos_image);
						image.setAnimation(null);
						image.setImageResource(R.drawable.traffic_lights_green);
					}
				}
			}
			//}
	}

	/**
	 * 
	 * @param testType
	 */
	public void updateProgressBar(final QoSTestResultEnum testType) {
		if (viewMap != null) {
			View view = viewMap.get(testType);
			if (view != null) {
				ImageView image = (ImageView) view.findViewById(R.id.test_view_qos_image);
				if (image.getAnimation() == null) {
					image.setImageResource(R.drawable.traffic_lights_yellow);
					image.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.rotate));
				}
			}
		}
	}

	@Override
	public void onQoSTestStart(final AbstractQoSTask test) {
		
		handler.post(new Runnable() {
			
			@Override
			public void run() {
				updateProgressBar(test.getTestType());	
			}
		});
	}


	@Override
	public void onQoSTestEnd(AbstractQoSTask test) {
		// TODO Auto-generated method stub
	}

	/*
	 * (non-Javadoc)
	 * @see at.alladin.rmbt.client.v2.task.service.TestProgressListener#onQoSCreated(at.alladin.rmbt.client.QualityOfServiceTest)
	 */
	@Override
	public void onQoSCreated(QualityOfServiceTest qosTest) {
		setTaskMap(qosTest.getTestMap());		
	}
}
