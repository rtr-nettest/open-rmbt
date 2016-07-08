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
package at.alladin.rmbt.android.views;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import at.alladin.openrmbt.android.R;
import at.alladin.rmbt.android.graphview.CustomizableGraphView;
import at.alladin.rmbt.android.graphview.GraphService;
import at.alladin.rmbt.android.graphview.GraphView;
import at.alladin.rmbt.android.graphview.GraphView.GraphLabel;
import at.alladin.rmbt.android.main.RMBTMainActivity;
import at.alladin.rmbt.android.test.RMBTTestFragment;
import at.alladin.rmbt.android.test.SmoothGraph;
import at.alladin.rmbt.android.test.StaticGraph;
import at.alladin.rmbt.android.util.CheckTestResultDetailTask;
import at.alladin.rmbt.android.util.EndTaskListener;
import at.alladin.rmbt.android.util.InformationCollector;
import at.alladin.rmbt.android.util.net.NetworkUtil;
import at.alladin.rmbt.android.util.net.NetworkUtil.MinMax;
import at.alladin.rmbt.android.views.ResultDetailsView.ResultDetailType;
import at.alladin.rmbt.client.helper.IntermediateResult;

public class ResultGraphView extends ScrollView implements EndTaskListener {
	
	public final static List<GraphLabel> SPEED_LABELS;
	
	static {
		SPEED_LABELS = new ArrayList<GraphView.GraphLabel>();
		SPEED_LABELS.add(new GraphLabel("0.1", "#C800f940"));
		SPEED_LABELS.add(new GraphLabel("1", "#C800f940"));
		SPEED_LABELS.add(new GraphLabel("10", "#C800f940"));
		SPEED_LABELS.add(new GraphLabel("100", "#C800f940"));
		SPEED_LABELS.add(new GraphLabel("1000", "#C800f940"));
	}
	
	private View view;
	
    //private static final String DEBUG_TAG = "ResultGraphView";

	public final static String COLOR_SIGNAL_3G = "#ffe000";
	public final static String COLOR_SIGNAL_4G = "#40a0f8";
	public final static String COLOR_SIGNAL_WLAN = "#f8a000";
	public final static String SIGNAL_COLOR_MARKER = "#cccccc";
	
	public final static String COLOR_UL_GRAPH = "#81c1dc";
	public final static String COLOR_DL_GRAPH = "#31d13c";

	
    public static final String ARG_UID = "uid";
    
	private RMBTMainActivity activity;
    
    private String uid;
    
    private String openTestUid;
    
    private JSONArray testResult;

	private EndTaskListener resultFetchEndTaskListener;

	private CheckTestResultDetailTask testResultOpenDataTask;
	
	private JSONArray uploadArray;
	private JSONArray downloadArray;
	private JSONArray signalArray;
	
	private CustomizableGraphView signalGraph;
	private CustomizableGraphView ulGraph;
	private CustomizableGraphView dlGraph;
	
	private ProgressBar dlProgress;
	private ProgressBar ulProgress;
	private ProgressBar signalProgress;
	
	/**
	 * 
	 * @param context
	 */
	public ResultGraphView(Context context, RMBTMainActivity activity, String uid, String openTestUid, JSONArray testResult, ViewGroup vg) {
		this(context, null, activity, uid, openTestUid, testResult, vg);
	}
	
	/**
	 * 
	 * @param context
	 * @param attrs
	 */
	public ResultGraphView(Context context, AttributeSet attrs, RMBTMainActivity activity, String uid, String openTestUid, JSONArray testResult, ViewGroup vg) {
		super(context, attrs);
		setFillViewport(true);
		
		this.activity = activity;
		this.uid = uid;
		this.openTestUid = openTestUid;
		this.testResult = testResult;
		createView(vg);
	}
	
	/**
	 * 
	 * @param openTestUid
	 */
	public void setOpenTestUuid(String openTestUid) {
		this.openTestUid = openTestUid;
	}
	
    public void initialize(EndTaskListener resultFetchEndTaskListener) {
    	this.resultFetchEndTaskListener = resultFetchEndTaskListener;
    	
        if ((testResultOpenDataTask == null || testResultOpenDataTask != null || testResultOpenDataTask.isCancelled()) && uid != null)
        {
        	if (this.testResult!=null) {
        		System.out.println("TESTRESULT found GraphView");
        		taskEnded(this.testResult);
        	}
        	else {
        		System.out.println("TESTRESULT NOT found GraphView");
            	System.out.println("initializing ResultGraphView");
            	
                testResultOpenDataTask = new CheckTestResultDetailTask(activity, ResultDetailType.OPENDATA);
                
                testResultOpenDataTask.setEndTaskListener(this);
                testResultOpenDataTask.execute(openTestUid);        		
        	}
        }
    }
    
    /**
     * 
     * @param inflater
     * @return
     */
	public View createView(ViewGroup vg)
    {       
		final LayoutInflater inflater = (LayoutInflater) vg.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		try {
	    	//view = inflater.inflate(R.layout.result_graph, vg, false);
			view = inflater.inflate(R.layout.result_graph, this);
	    	
	    	signalGraph = (CustomizableGraphView) view.findViewById(R.id.graph_signal);
	    	ulGraph = (CustomizableGraphView) view.findViewById(R.id.graph_upload);
	    	dlGraph = (CustomizableGraphView) view.findViewById(R.id.graph_download);
	    	
	    	signalProgress = (ProgressBar) view.findViewById(R.id.signal_progress);
	    	ulProgress = (ProgressBar) view.findViewById(R.id.upload_progress);
	    	dlProgress = (ProgressBar) view.findViewById(R.id.download_progress);

		} catch (Exception e) {
			e.printStackTrace();
		}
        
    	
    	return view;
    }
		
	/**
	 * 
	 * @return
	 */
	public View getView() {
		return view;
	}
	
//	@Override
//	protected void onDraw(Canvas canvas) {
//		super.onDraw(canvas);
//		
//		if (view == null) {
//			Log.d("ResultGraphView", "ONDRAW");
//			//removeAllViews();
//			createView();
//			
//			taskEnded(testResult);			
//		}
//	}

	@Override
	public void taskEnded(JSONArray result) {
		//addView(view);
		if (result != null) {
			testResult = result;
		}
		
		if (view == null || result == null) {
			return;
		}
		
		if (resultFetchEndTaskListener != null) {
			resultFetchEndTaskListener.taskEnded(result);
		}
		
		if (testResult != null) {
			redraw(testResult);
		}
	}
	
	public void refresh(JSONArray result) {
		this.testResult = result;
		redraw(result);
	}
	
	private void redraw(JSONArray result) {
		try {
			JSONObject curve = result.getJSONObject(0).getJSONObject("speed_curve");
			if (curve != null) {
				uploadArray = curve.getJSONArray("upload");
				downloadArray = curve.getJSONArray("download");
				signalArray = curve.getJSONArray("signal");
			}
			
			//System.out.println(signalArray);
			
			boolean signalLabelPositionEven = true;
			long maxTimeUpload = result.getJSONObject(0).optLong("time_ul_ms") + result.getJSONObject(0).optLong("duration_upload_ms");
			long timeElapsed = Math.round((double)maxTimeUpload / 1000);
			
			if (signalGraph != null && signalArray != null && signalArray.length() > 0 && signalGraph.getGraphs().size() < 1) {
				Log.d("ResultGraphView", "DRAWING SIGNAL GRAPH\n" + signalArray);

				final long maxTimeSignal = signalArray.optJSONObject(signalArray.length()-1).optLong("time_elapsed");
				final long timeElapsedMs = Math.max(maxTimeSignal, maxTimeUpload);
				timeElapsed = Math.round((double)timeElapsedMs / 1000);
				
				signalGraph.getLabelHMaxList().clear();
				signalGraph.addLabelHMax(String.valueOf(timeElapsed < 7 ? 7 : timeElapsed));
				signalGraph.updateGrid((int)timeElapsed, 4);
				/* [{"network_type":"WLAN","time_elapsed":0,"signal_strength":-56},
				 * {"network_type":"WLAN","time_elapsed":10121,"signal_strength":-55},
				 * {"network_type":"WLAN","time_elapsed":37478,"signal_strength":-57}]
				 */
				MinMax<Integer> signalBoundsRsrp = NetworkUtil.getSignalStrengthBounds(InformationCollector.SINGAL_TYPE_RSRP);
				MinMax<Integer> signalBoundsWlan = NetworkUtil.getSignalStrengthBounds(InformationCollector.SINGAL_TYPE_WLAN);
				MinMax<Integer> signalBoundsMobile = NetworkUtil.getSignalStrengthBounds(InformationCollector.SINGAL_TYPE_MOBILE);
				
				List<GraphService> signalList = new ArrayList<GraphService>(); 
				
				boolean has4G = false;
				boolean has3G = false;
				boolean hasWlan = false;
				String lastNetworkType = "";
				String lastCatTechnology = "";
				
				GraphService curGraph = null;

				double prevValue = 0d;
				
				boolean hasSignalCategoryChanged = false;
				double lastLabelYPosition = -1d;
				double time = 0d;
				double signalChangeStartTime = 0d;
				double value = 0d;
				double oldValidValue = 0d;
				
				for (int i = 0; i < signalArray.length(); i++) {
					JSONObject signalObject = signalArray.getJSONObject(i);
					String networkType = signalObject.optString("network_type");
					String catTechnology = signalObject.optString("cat_technology");
					//set proper signal strength attribute
					String signalAttribute = "signal_strength";
					String signalColor = "#ffffff";
					
					time = i > 0 ? ((double)signalObject.getInt("time_elapsed") / (double)timeElapsedMs) : 0d;
					
					oldValidValue = value > 0d ? value : oldValidValue;
					
					if ("LTE".equals(networkType)) {
						if (!lastNetworkType.equals(networkType) && !lastCatTechnology.equals(catTechnology)) {
							if (curGraph != null) {
								curGraph.addValue((1-prevValue), time);
							}

							GraphService newGraph = StaticGraph.addGraph(signalGraph, Color.parseColor(COLOR_SIGNAL_4G), 
								 signalArray.length() == 1 ? true : false);
							signalList.add(newGraph);
							if (curGraph != null) {
								curGraph.addValue((1-prevValue), time);
							}
							signalChangeStartTime = time;
							hasSignalCategoryChanged = true;
							curGraph = newGraph;
						}
						has4G = true;
						signalAttribute = "lte_rsrp";
						signalColor = COLOR_SIGNAL_4G;
						value = getRelativeSignal(signalBoundsRsrp, signalAttribute, signalObject);
					}
					else if ("WLAN".equals(networkType)) {
						if (!lastNetworkType.equals(networkType) && !lastCatTechnology.equals(catTechnology)) {
							if (curGraph != null) {
								curGraph.addValue((1-prevValue), time);
							}

							GraphService newGraph = StaticGraph.addGraph(signalGraph, Color.parseColor(COLOR_SIGNAL_WLAN), 
									signalArray.length() == 1 ? true : false);
							signalList.add(newGraph);
							
							if (curGraph != null) {
								curGraph.addValue((1-prevValue), time);
							}
							signalChangeStartTime = time;
							hasSignalCategoryChanged = true;
							curGraph = newGraph;
						}
						hasWlan = true;
						signalAttribute = "signal_strength";
						signalColor = COLOR_SIGNAL_WLAN;
						value = getRelativeSignal(signalBoundsWlan, signalAttribute, signalObject);
					}
					else {
						if (!lastNetworkType.equals(networkType)) {
							signalChangeStartTime = time;
							hasSignalCategoryChanged = true;
							
							if (curGraph != null) {
								curGraph.addValue((1-prevValue), time);
							}
							
							if ((!lastCatTechnology.equals(catTechnology)) && 
									("4G".equals(lastCatTechnology) || "WLAN".equals(lastCatTechnology) || "".equals(lastCatTechnology))) {
								
								GraphService newGraph = StaticGraph.addGraph(signalGraph, Color.parseColor(COLOR_SIGNAL_3G), 
										signalArray.length() == 1 ? true : false);

								signalList.add(newGraph);
								if (curGraph != null) {
									curGraph.addValue((1-prevValue), time);
								}
								curGraph = newGraph;
							}
						}
						has3G = true;
						signalAttribute = "signal_strength";
						signalColor = COLOR_SIGNAL_3G;
						value = getRelativeSignal(signalBoundsMobile, signalAttribute, signalObject);
					}					
					
					if (value > 0d) {
						if (value >= 0d && curGraph != null) {
							if (hasSignalCategoryChanged) {
								curGraph.addValue((1 - value), signalChangeStartTime);
								hasSignalCategoryChanged = false;
								
								if (lastLabelYPosition == -1d) {
									lastLabelYPosition = (float) (1 - (value > 0d ? value : prevValue));
								}
								else {
									if (Math.abs(signalChangeStartTime - time) < .125d) {
										float curPosition = (float) (1 - (value > 0d ? value : prevValue));
										if (Math.abs(curPosition - lastLabelYPosition) <= .11d) {
											lastLabelYPosition = curPosition + (curPosition > lastLabelYPosition ? +.1d : -.1d);
										}
										else {
											lastLabelYPosition = curPosition;
										}
									}
									else {
										lastLabelYPosition = (float) (1 - (value > 0d ? value : prevValue));
									}
								}
								
								//lastLabelXPosition = (float) time;								
								double labelDiff = lastLabelYPosition - (1-value);
								
								System.out.println("i" + i + " -> " + lastLabelYPosition + " : " + (1-value) + " diff: " + Math.abs(labelDiff) + " istoolow (<.09d)? " + (Math.abs(labelDiff) < .09d));
								if (Math.abs(labelDiff) < .09d && i == 0) {
									if (labelDiff < 0d) {
										lastLabelYPosition = lastLabelYPosition < .50d ? lastLabelYPosition - .075d : lastLabelYPosition + .075d;
									}
									else {
										lastLabelYPosition = lastLabelYPosition < .50d ? lastLabelYPosition + .075d : lastLabelYPosition - .075d;
									}
								}
								
								float labelX = Math.min((float)signalChangeStartTime, .92f);
								float labelY = signalLabelPositionEven ? .93f : .84f;
								signalGraph.addLabel((float) labelX, labelY, networkType, signalColor);
								signalLabelPositionEven = !signalLabelPositionEven;
								
								//addStaticMarker(signalGraph, signalColor, 70, labelX, labelX);

								
								//signalGraph.addLabel((float) signalChangeStartTime, (float) lastLabelYPosition, networkType, signalColor);
							}
	
							//System.out.println("ADDING VALUE TO GRAPH " + (1 - value) + " on: " + time);
							curGraph.addValue((1 - value), time);
							prevValue = value;
						}
					}
					
					lastNetworkType = networkType;
					lastCatTechnology = catTechnology;
				}
				
				//draw signal graph to the end
				if (prevValue > 0 && curGraph != null) {
					curGraph.addValue((1 - prevValue), 1f);
				}
				
				signalGraph.clearLabels(CustomizableGraphView.LABELLIST_VERTICAL_MAX);
				signalGraph.clearLabels(CustomizableGraphView.LABELLIST_VERTICAL_MIN);
				
				if (has3G) {
					signalGraph.addLabelVMax(String.valueOf(signalBoundsMobile.max), COLOR_SIGNAL_3G);
					signalGraph.addLabelVMin(String.valueOf(signalBoundsMobile.min), COLOR_SIGNAL_3G);
				}
				if (has4G) {
					signalGraph.addLabelVMax(String.valueOf(signalBoundsRsrp.max), COLOR_SIGNAL_4G);
					signalGraph.addLabelVMin(String.valueOf(signalBoundsRsrp.min), COLOR_SIGNAL_4G);
				}
				if (hasWlan) {
					signalGraph.addLabelVMax(String.valueOf(signalBoundsWlan.max), COLOR_SIGNAL_WLAN);
					signalGraph.addLabelVMin(String.valueOf(signalBoundsWlan.min), COLOR_SIGNAL_WLAN);
				}
				//signalGraph.repaint(getContext());
			}
			else if (signalGraph != null && signalGraph.getGraphs().size() > 0) {
				Log.d("ResultGraphView", "REDRAWING SIGNAL GRAPH");
				//signalGraph.repaint(getContext());
				signalGraph.invalidate();
			}
			
			signalProgress.setVisibility(View.GONE);
			
			if (uploadArray != null && uploadArray != null && uploadArray.length() > 0 && ulGraph.getGraphs().size() < 1) {
				Log.d("ResultGraphView", "DRAWING UL GRAPH");
				drawCurve(uploadArray, ulGraph, COLOR_UL_GRAPH, 
						String.valueOf(Math.round(result.getJSONObject(0).optDouble("duration_upload_ms") / 1000d)), false);
				
				addStaticMarker(signalGraph, COLOR_UL_GRAPH, 70, 
						result.getJSONObject(0).optDouble("time_ul_ms"), 
						result.getJSONObject(0).optDouble("time_ul_ms") + result.getJSONObject(0).optDouble("duration_upload_ms"), 
						timeElapsed * 1000);
				
				double timeUl = result.getJSONObject(0).optDouble("duration_upload_ms");
				long timeElapsedUl = Math.round(timeUl / 1000);
				ulGraph.setRowLinesLabelList(SPEED_LABELS);
				ulGraph.updateGrid((int) timeElapsedUl, 4);
			}
			else if (uploadArray.length() > 0 && ulGraph != null && ulGraph.getGraphs().size() > 0) {
				Log.d("ResultGraphView", "REDRAWING UL GRAPH");
				//ulGraph.repaint(getContext());
				ulGraph.invalidate();
			}

			ulProgress.setVisibility(View.GONE);

			if (downloadArray != null && downloadArray != null && downloadArray.length() > 0 && dlGraph.getGraphs().size() < 1) {
				Log.d("ResultGraphView", "DRAWING DL GRAPH");
				drawCurve(downloadArray, dlGraph, COLOR_DL_GRAPH, 
						String.valueOf(Math.round(result.getJSONObject(0).optDouble("duration_download_ms") / 1000d)), false);
				addStaticMarker(signalGraph, COLOR_DL_GRAPH, 70, 
						result.getJSONObject(0).optDouble("time_dl_ms"), 
						result.getJSONObject(0).optDouble("time_dl_ms") + result.getJSONObject(0).optDouble("duration_download_ms"), 
						timeElapsed * 1000);
				
				double timeDl = result.getJSONObject(0).optDouble("duration_download_ms");
				long timeElapsedDl = Math.round(timeDl / 1000);
				dlGraph.setRowLinesLabelList(SPEED_LABELS);
				dlGraph.updateGrid((int) timeElapsedDl, 4);
			}
			else if (downloadArray.length() > 0 && dlGraph != null && dlGraph.getGraphs().size() > 0) {
				Log.d("ResultGraphView", "REDRAWING DL GRAPH");
				//dlGraph.repaint(getContext());
				dlGraph.invalidate();
			}

			dlProgress.setVisibility(View.GONE);

		}
		catch (Exception e) {
			if (signalGraph != null) {
				signalGraph.invalidate();
			}
			if (ulGraph != null) {
				ulGraph.invalidate();
			}
			if (dlGraph != null) {
				dlGraph.invalidate();
			}
			e.printStackTrace();
			//TODO show no data available view 
		}
	}
	
	public double getRelativeSignal(final MinMax<Integer> signalBounds, final String signalAttribute, final JSONObject signalObject) throws JSONException {
		final int signal = signalObject.getInt(signalAttribute);
		final double value = signal < 0? ((double)(signal - signalBounds.max) / (double)(signalBounds.min - signalBounds.max)) : signal;
		System.out.println("signalAttrib: " + signalAttribute + ", signal: " + signal + " value: " + value);
		return value;
	}
	
	/**
	 * 
	 * @param graphArray
	 * @param graphView
	 * @param color
	 * @throws JSONException
	 */
	public void addStaticMarker(JSONArray graphArray, CustomizableGraphView graphView, String color, double absoluteMarkerMs, double maxTimeMs) throws JSONException {
		StaticGraph markerGraph = StaticGraph.addGraph(graphView, Color.parseColor(color), false);
		final double startTime = (absoluteMarkerMs / maxTimeMs);
		markerGraph.addValue(1, startTime);
		markerGraph.addValue(0, startTime);
	}
	
	/**
	 * 
	 * @param graphArray
	 * @param graphView
	 * @param color
	 * @param alpha
	 * @param absoluteMarkerStartMs
	 * @param absoluteMarkerEndMs
	 * @param maxTimeMs
	 * @throws JSONException
	 */
	public void addStaticMarker(CustomizableGraphView graphView, String color, int alpha, double absoluteMarkerStartMs, double absoluteMarkerEndMs, double maxTimeMs) throws JSONException {
		final double startTime = (absoluteMarkerStartMs / maxTimeMs);
		final double endTime = (absoluteMarkerEndMs / maxTimeMs);
		addStaticMarker(graphView, color, alpha, startTime, endTime);
	}
	
	public void addStaticMarker(CustomizableGraphView graphView, String color, int alpha, double relativeMarkerStart, double relativeMarkerEnd) {
		StaticGraph markerGraph = StaticGraph.addGraph(graphView, Color.parseColor(color), false);
		StaticGraph markerGraphStartLine = StaticGraph.addGraph(graphView, Color.parseColor(color), false);
		StaticGraph markerGraphEndLine = StaticGraph.addGraph(graphView, Color.parseColor(color), false);
		markerGraph.setFillAlpha(alpha/2);
		markerGraph.setPaintAlpha(alpha/2);
		markerGraphStartLine.setPaintAlpha(alpha);
		markerGraphEndLine.setPaintAlpha(alpha);
		
		markerGraph.addValue(1, relativeMarkerStart);
		markerGraph.addValue(1, relativeMarkerEnd);
		markerGraphStartLine.addValue(1, relativeMarkerStart);
		markerGraphStartLine.addValue(0, relativeMarkerStart);
		markerGraphEndLine.addValue(1, relativeMarkerEnd);
		markerGraphEndLine.addValue(0, relativeMarkerEnd);		
	}

	
	/**
	 * 
	 * @param graphArray
	 * @param graphView
	 * @throws JSONException
	 */
	public void drawCurve(JSONArray graphArray, CustomizableGraphView graphView, String color, String labelHMax, boolean isSmoothed) throws JSONException {
		final double maxTime = graphArray.optJSONObject(graphArray.length()-1).optDouble("time_elapsed");

		graphView.getLabelHMaxList().clear();
		graphView.addLabelHMax(labelHMax);
		
		GraphService signal;
		//StaticGraph signal = StaticGraph.addGraph(graphView, Color.parseColor(color));
		if (!isSmoothed) {
			signal = StaticGraph.addGraph(graphView, Color.parseColor(color), graphArray.length() == 1 ? true : false);
		}
		else {
			signal = SmoothGraph.addGraph(graphView, Color.parseColor(color), RMBTTestFragment.SMOOTHING_DATA_AMOUNT, 
				RMBTTestFragment.SMOOTHING_FUNCTION, false);
		}
		
		double lastTime = 0d;
		
		if (graphArray != null && graphArray.length() > 0) {
			long bytes = 0;
			for (int i = 0; i < graphArray.length(); i++) {				
				JSONObject uploadObject = graphArray.getJSONObject(i);
				double timeElapsed = uploadObject.getInt("time_elapsed");
				bytes = uploadObject.getInt("bytes_total");
				double bitPerSec = (bytes * 8000 / timeElapsed);
				double time = (timeElapsed / maxTime);
				
				if (lastTime == 0d || (timeElapsed - lastTime >= 175d)) {
					if (lastTime == 0d && !isSmoothed) {
						time = 0d;
					}
					
					System.out.println(bitPerSec + "bps (bytes: " + bytes + ", time: " + timeElapsed + ")");					
					if (i+1 == graphArray.length()) {
						signal.addValue(IntermediateResult.toLog((long) bitPerSec), time, SmoothGraph.FLAG_ALIGN_RIGHT);
					}
					else {
						signal.addValue(IntermediateResult.toLog((long) bitPerSec), time, 
								i == 0 ? SmoothGraph.FLAG_ALIGN_LEFT : SmoothGraph.FLAG_NONE);
					}
					
					if (!isSmoothed) {
						lastTime = timeElapsed;
					}
				}				
			}
		}
	}
	
	@Override
	public void invalidate() {
		setFillViewport(true);
		super.invalidate();
	}
	
	@Override
	protected void onConfigurationChanged(Configuration newConfig) {
		view = null;
		super.onConfigurationChanged(newConfig);
	}
	

	@Override
	protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putString("test_result", testResult.toString());
        return bundle;

	}
	
	@Override
	protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle)
        {
            Bundle bundle = (Bundle) state;
            try {
				testResult = new JSONArray(bundle.getString("test_result"));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            return;
        }
	}
	
	public void recycle() {
		if (dlGraph != null) { 
			dlGraph.recycle();
			dlGraph = null;
		}
		if (ulGraph != null) {
			ulGraph.recycle();
			ulGraph = null;
		}
		if (signalGraph != null) {
			signalGraph.recycle();
			signalGraph = null;
		}
	}
}
