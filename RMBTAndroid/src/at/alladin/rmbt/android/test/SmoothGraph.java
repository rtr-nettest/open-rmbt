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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PointF;
import android.os.Bundle;
import at.alladin.rmbt.android.graphview.GraphService;
import at.alladin.rmbt.android.graphview.GraphView;

public class SmoothGraph implements GraphService {

	public final static int FLAG_NONE = 0;
	public final static int FLAG_ALIGN_RIGHT = 1;
	public final static int FLAG_ALIGN_LEFT = 2;
	
	/**
	 * some functions don't calculate the current point but previous ones (like the centered moving avarage).
	 * this flag let the smooth graph take the position of the current added node to draw the current calcutaled avarage (instead of a previous time) 
	 */
	public final static int FLAG_USE_CURRENT_NODE_TIME = 4;
	
	public static final String OPTION_STARTTIME = "startTime";
	public static final String OPTION_FIRSTPOINT = "firstPoint";
	public static final String OPTION_VALUELIST = "valueList";

	
	/**
	 * available smoothing function
	 * @author lb
	 *
	 */
	public enum SmoothingFunction {
		/**
		 * equal number of data on each side<br>
		 * function for centered moving avarage with n amount of data for element with the index x:<br>
		 * f(x, n) = 1/n * (element(x-n/2) + element(x-n/2+1) + ... + element (x-n/2+n))<br>
		 */
		CENTERED_MOVING_AVARAGE,
		
		/**
		 * previous n number of data<br>
		 * function for simple moving avarage with n amount of data for element with the index x:<br>
		 * f(x, n) = 1/n * (element(x-n) + element(x-n+1) + ... + element(x))<br>
		 */
		SIMPLE_MOVING_AVARAGE;
		
		public static double smooth(final SmoothingFunction smoothingFunction, final int element, final List<ValueEntry> valueList, final int dataAmount) {
			if (valueList == null || valueList.size() < 1) {
				return 0d;
			}

			int startingIndex = 0;
			int realDataAmount = SmoothingFunction.getDataAmountNeeded(smoothingFunction, dataAmount);
			
			switch (smoothingFunction) {
			case CENTERED_MOVING_AVARAGE:
				startingIndex = element - dataAmount/2;
				break;
			case SIMPLE_MOVING_AVARAGE:
				startingIndex = element - dataAmount;
				break;
			default:
				startingIndex = 0;
			}
			
			double sum = 0d;
			
			if (startingIndex < 0) {
				final int underflow = Math.abs(startingIndex);
				sum += underflow * valueList.get(0).value;
				realDataAmount -= underflow;
				startingIndex = 0;
			}
						
			if (startingIndex >= valueList.size()) {
				return 0d;
			}
			
			if ((startingIndex + realDataAmount) > valueList.size()) {
				final int overlfow = Math.abs(valueList.size() - (startingIndex + realDataAmount));
				sum += overlfow * valueList.get(valueList.size()-1).value;
				realDataAmount -= overlfow;
			}
				
			for (int i = startingIndex; i < (startingIndex + realDataAmount); i++) {
				sum += valueList.get(i).getValue();
//				System.out.print("[i:" + i + ", sum: " + sum + "]");
			}
			
			return (sum / (double)SmoothingFunction.getDataAmountNeeded(smoothingFunction, dataAmount));
		}
		
		public static int getDataAmountNeeded(SmoothingFunction smoothingFunction, int dataAmount) {
			switch (smoothingFunction) {
			case CENTERED_MOVING_AVARAGE:
				return dataAmount;
			case SIMPLE_MOVING_AVARAGE:
				return dataAmount;
			default:
				return 0;
			}
		}
	}
	
	private class ValueEntry {
		protected final double value;
		protected final double time;
		protected int flag;
		
		public ValueEntry(final double value, final double time, final int flag) {
			this.value = value;
			this.time = time;
			this.flag = flag;
		}

		public double getValue() {
			return value;
		}

		public double getTime() {
			return time;
		}
		
		public int getFlag() {
			return flag;
		}
	}
	
    private final float height;
    private final float width;
    private final List<ValueEntry> valueList = new ArrayList<ValueEntry>();
    private final Path pathStroke;
    private final Path pathFill;
    private final Paint paintStroke;
    private final Paint paintFill;
    private PointF firstPoint;
    
    private SmoothingFunction smoothingFunction = SmoothingFunction.CENTERED_MOVING_AVARAGE;
    private int dataAmount = 0;
    
    private boolean matchHorizontally = false;
    
    private long startTime = -1;
    private long maxTimeNs = 0;
    
    /**
     * 
     * @param graphView
     * @param color
     * @param dataAmount number of data to use for smoothing function
     * @return
     */
    public static SmoothGraph addGraph(final GraphView graphView, final int color, final int dataAmount, final SmoothingFunction smoothingFunction) {
    	return SmoothGraph.addGraph(graphView, color, dataAmount, smoothingFunction, true);
    }
    
    /**
     * 
     * @param graphView
     * @param color
     * @param dataAmount number of data to use for smoothing function
     * @param matchHorizontally
     * @return
     */
    public static SmoothGraph addGraph(final GraphView graphView, final int color, final int dataAmount, final SmoothingFunction smoothingFunction, final boolean matchHorizontally)
    {
        final SmoothGraph graph = new SmoothGraph(color, graphView.getGraphWidth(), graphView.getGraphHeight(),
                graphView.getGraphStrokeWidth());
        graph.setMatchHorizontally(matchHorizontally);
        graph.setSmoothingFunction(smoothingFunction);
        graph.setDataAmount(dataAmount);
        graphView.addGraph(graph);
        return graph;
    }
    
    /**
     * 
     * @param graphView
     * @param dataAmount
     * @param smoothingFunction
     * @param matchHorizontally
     * @param graphData
     * @return
     */
    @SuppressWarnings("unchecked")
	public static SmoothGraph addGraph(final GraphView graphView,final int dataAmount, final SmoothingFunction smoothingFunction, 
    		final boolean matchHorizontally, final GraphData graphData) {
    	final SmoothGraph graph = SmoothGraph.addGraph(graphView, dataAmount, smoothingFunction, matchHorizontally, graphData.getPathStroke(), graphData.getPathFill(), 
    			graphData.getPaintStroke(), graphData.getPaintFill());
    	
    	graph.valueList.addAll((Collection<? extends ValueEntry>) graphData.getOptions().getSerializable(OPTION_VALUELIST));
    	if (graphData.getOptions().getParcelable(OPTION_FIRSTPOINT) != null) {
    		graph.firstPoint = new PointF();
    		graph.firstPoint.set((PointF) graphData.getOptions().getParcelable(OPTION_FIRSTPOINT));
    	}
    	graph.startTime = graphData.getOptions().getLong(OPTION_STARTTIME);
    	
    	return graph;
    }
    
    /**
     * 
     * @param graphView
     * @param color
     * @param dataAmount
     * @param smoothingFunction
     * @param matchHorizontally
     * @param pathStroke
     * @param pathFill
     * @param paintStroke
     * @param paintFill
     * @return
     */
    public static SmoothGraph addGraph(final GraphView graphView,final int dataAmount, final SmoothingFunction smoothingFunction, 
    		final boolean matchHorizontally, final Path pathStroke, final Path pathFill, final Paint paintStroke, final Paint paintFill)
    {
        final SmoothGraph graph = new SmoothGraph(graphView.getGraphWidth(), graphView.getGraphHeight(), pathStroke, pathFill, paintStroke, paintFill);
        graph.setMatchHorizontally(matchHorizontally);
        graph.setSmoothingFunction(smoothingFunction);
        graph.setDataAmount(dataAmount);
        graphView.addGraph(graph);
        return graph;
    }
    
    
    public SmoothingFunction getSmoothingFunction() {
		return smoothingFunction;
	}

	public void setSmoothingFunction(SmoothingFunction smoothingFunction) {
		this.smoothingFunction = smoothingFunction;
	}
	
	public int getDataAmount() {
		return dataAmount;
	}

	public void setDataAmount(int dataAmount) {
		this.dataAmount = dataAmount;
	}

	private SmoothGraph(final int color, final float width, final float height, final float strokeWidth)
    {
        this.height = height;
        this.width = width;
        
        paintStroke = new Paint();
        paintStroke.setColor(color);
        paintStroke.setAlpha(204); // 80%
        paintStroke.setStyle(Style.STROKE);
        paintStroke.setStrokeWidth(strokeWidth);
        paintStroke.setStrokeCap(Cap.ROUND);
        paintStroke.setStrokeJoin(Join.ROUND);
        paintStroke.setAntiAlias(true);
        
        paintFill = new Paint();
        paintFill.setColor(color);
        paintFill.setAlpha(51); // 20%
        paintFill.setStyle(Style.FILL);
        paintFill.setAntiAlias(true);

        pathStroke = new Path();
        pathFill = new Path();
    }
	
	private SmoothGraph(final float width, final float height, final Path pathStroke, final Path pathFill, final Paint paintStroke, final Paint paintFill) {
		this.height = height;
		this.width = width;
		this.paintFill = new Paint(paintFill);
		this.paintStroke = new Paint(paintStroke);
		this.pathFill = new Path(pathFill);
		this.pathStroke = new Path(pathStroke);
	}
    
    /*
     * (non-Javadoc)
     * @see at.alladin.rmbt.android.test.Graph#addValue(double)
     */
    public void addValue(double value) {
    	addValue(value, FLAG_NONE);
    }
    
    public void addValue(double value, int flag) {
        final long relTime;
        if (startTime == -1) {
            startTime = System.nanoTime();
            relTime = 0;
        }
        else {
            relTime = System.nanoTime() - startTime;
        }
        
        if (relTime >= maxTimeNs) return;

        final double time = (double)relTime / (double)maxTimeNs;
        
    	addValue(value, time, flag);
    }

	/*
	 * (non-Javadoc)
	 * @see at.alladin.rmbt.android.graphview.GraphService#addValue(double, double)
	 */
	public void addValue(double value, double time) {
		addValue(value, time, FLAG_NONE);
	}

    /*
     * (non-Javadoc)
     * @see at.alladin.rmbt.android.graphview.GraphService#addValue(double, double, int)
     */
    public void addValue(double value, double time, int flag)
    {
        if (value < 0d) {
        	value = 0d;
        }
        else if (value > 1d) {
        	value = 1d;
        }
        
        if (time < 0d) {
        	time = 0d;
        }
        else if (time > 1d) {
        	time = 1d;
        }
        
        valueList.add(new ValueEntry(value, time, flag));
        
        if (valueList.size() >= SmoothingFunction.getDataAmountNeeded(smoothingFunction, dataAmount)) {
        	
        	final int index;
        	final int timeIndex;
        	
        	switch (smoothingFunction) {
        	case CENTERED_MOVING_AVARAGE:
        		index = valueList.size() - dataAmount / 2 - 1;
        		break;
        	case SIMPLE_MOVING_AVARAGE:
        		index = valueList.size() - 1;
        		break;
        	default:
        		index = dataAmount;
        		break;
        	}
        	
        	if ((flag & FLAG_USE_CURRENT_NODE_TIME) == FLAG_USE_CURRENT_NODE_TIME) {
        		timeIndex = valueList.size() - 1;
        	}
        	else {
        		timeIndex = index;
        	}
        	
            if (firstPoint == null) {
            	for (int i = 0; i < index; i++) {
                	value = SmoothingFunction.smooth(smoothingFunction, i, valueList, dataAmount);
                	time = valueList.get(i).getTime();
                    final float x = getXCoord(time, valueList.get(i).getFlag());            
                    final float y = (float) (height * (1 - value));
                	
                	if (firstPoint == null) {
                		pathStroke.moveTo(x, y);
                		firstPoint = new PointF(x, y);
                	}
                	else {
                		pathStroke.lineTo(x, y);
                        pathFill.rewind();
                        pathFill.addPath(pathStroke);
                        pathFill.lineTo(x, height);
                       	pathFill.lineTo(firstPoint.x, height);
                	}
            	}
            }
        	
        	value = SmoothingFunction.smooth(smoothingFunction, index, valueList, dataAmount);
        	time = valueList.get(timeIndex).getTime();
            final float x = getXCoord(time, flag);            
            final float y = (float) (height * (1 - value));
            pathStroke.lineTo(x, y);
            pathFill.rewind();
            pathFill.addPath(pathStroke);
            pathFill.lineTo(x, height);
           	pathFill.lineTo(firstPoint.x, height);
        }
    }
    
    protected float getXCoord(final double time, final int flag) {
        if ((flag & FLAG_ALIGN_LEFT) == FLAG_ALIGN_LEFT) {
        	return 0f;
        }
       	else if ((flag & FLAG_ALIGN_RIGHT) == FLAG_ALIGN_RIGHT) {
       		return width;
       	}
       	else {
       		return (float) (width * time);
       	}
    }
    
    public void draw(final Canvas canvas)
    {
    	if (valueList.size() == 1 && isMatchHorizontally()) {
    		pathStroke.lineTo(width, firstPoint.y);
    		pathFill.rewind();
    		pathFill.addPath(pathStroke);
    		pathFill.lineTo(width, height);
    		pathFill.lineTo(0, height);
    	}
    	
        canvas.drawPath(pathStroke, paintStroke);
        canvas.drawPath(pathFill, paintFill);
    }
    
    public void reset()
    {
    	firstPoint = null;
    	valueList.clear();
        pathStroke.rewind();
        pathFill.rewind();
        startTime = -1;
    }
    
    public boolean hasBeenStarted()
    {
        return true;
    }
    
    public void clearGraphDontResetTime()
    {
    	firstPoint = null;
    	valueList.clear();
        pathStroke.rewind();
        pathFill.rewind();        
    }

	public boolean isMatchHorizontally() {
		return matchHorizontally;
	}

	public void setMatchHorizontally(boolean matchHorizontally) {
		this.matchHorizontally = matchHorizontally;
	}
	
    /**
     * 
     * @param alpha
     */
    public void setPaintAlpha(int alpha) {
    	paintStroke.setAlpha(alpha);
    }
    
    /**
     * 
     * @return
     */
    public int getPaintAlpha() {
    	return paintStroke.getAlpha();
    }
    
    
    /**
     * 
     * @param alpha
     */
    public void setFillAlpha(int alpha) {
    	paintFill.setAlpha(alpha);
    }
    
    /**
     * 
     * @return
     */
    public int getFillAlpha() {
    	return paintFill.getAlpha();
    }

	@Override
	public void setMaxTime(long maxTimeNs) {
		this.maxTimeNs = maxTimeNs;
	}

	@Override
	public Path getPathStroke() {
		return pathStroke;
	}

	@Override
	public Path getPathFill() {
		return pathFill;
	}

	@Override
	public Paint getPaintStroke() {
		return paintStroke;
	}

	@Override
	public Paint getPaintFill() {
		return paintFill;
	}
	
	/*
	 * (non-Javadoc)
	 * @see at.alladin.rmbt.android.graphview.GraphService#getGraphData()
	 */
	@Override
	public GraphData getGraphData() {
		final Bundle options = new Bundle();
		options.putLong(OPTION_STARTTIME, startTime);
		options.putSerializable(OPTION_VALUELIST, (Serializable) valueList);
		options.putParcelable(OPTION_FIRSTPOINT, firstPoint);

		return new GraphData(pathStroke, pathFill, paintStroke, paintFill, options);
	}
}
