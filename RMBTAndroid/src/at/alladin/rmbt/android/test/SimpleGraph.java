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

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.os.Bundle;
import at.alladin.rmbt.android.graphview.GraphService;
import at.alladin.rmbt.android.graphview.GraphView;

public class SimpleGraph implements GraphService
{
	public static final String OPTION_STARTTIME = "startTime";
	public static final String OPTION_STARTX = "startX";
	public static final String OPTION_VALUES = "values";
	
    // private final float width;
    private final float height;
    private final float nsecWidth;
    
    private final Path pathStroke;
    private final Path pathFill;
    private final Paint paintStroke;
    private final Paint paintFill;
    
    private long maxNsecs;
    private long startTime = -1;
    private int values;
    private float startX;
    
    public static SimpleGraph addGraph(final GraphView graphView, final int color, final long maxNsecs)
    {
        final SimpleGraph graph = new SimpleGraph(color, maxNsecs, graphView.getGraphWidth(), graphView.getGraphHeight(),
                graphView.getGraphStrokeWidth());
        graphView.addGraph(graph);
        return graph;
    }

    public static SimpleGraph addGraph(final GraphView graphView, final long maxNsecs, final GraphData graphData) {
    	final SimpleGraph graph = SimpleGraph.addGraph(graphView, maxNsecs, graphData.getPathStroke(), graphData.getPathFill(), 
    			graphData.getPaintStroke(), graphData.getPaintFill());
    	
    	graph.startX = graphData.getOptions().getFloat(OPTION_STARTX, 0f);
    	graph.startTime = graphData.getOptions().getLong(OPTION_STARTTIME, -1);
    	graph.values = graphData.getOptions().getInt(OPTION_VALUES, 0);
    	
    	return graph;
    }
    
    public static SimpleGraph addGraph(final GraphView graphView, final long maxNsecs, 
    		final Path pathStroke, final Path pathFill, final Paint paintStroke, final Paint paintFill) 
    {
        final SimpleGraph graph = new SimpleGraph(maxNsecs, graphView.getGraphWidth(), graphView.getGraphHeight(), 
        		pathStroke, pathFill, paintStroke, paintFill);
        graphView.addGraph(graph);
        return graph;
    }

    private SimpleGraph(final int color, final long maxNsecs, final float width, final float height, final float strokeWidth)
    {
        this.maxNsecs = maxNsecs;
        // this.width = width;
        this.height = height;
        nsecWidth = width / maxNsecs;
        
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
    
	private SimpleGraph(final long maxNsecs, final float width, final float height, final Path pathStroke, final Path pathFill, final Paint paintStroke, final Paint paintFill) {
        this.maxNsecs = maxNsecs;
        // this.width = width;
        this.height = height;
        nsecWidth = width / maxNsecs;
        
		this.paintFill = new Paint(paintFill);
		this.paintStroke = new Paint(paintStroke);
		this.pathFill = new Path(pathFill);
		this.pathStroke = new Path(pathStroke);
	}
    
    /*
     * (non-Javadoc)
     * @see at.alladin.rmbt.android.test.Graph#addValue(double, double)
     */
    public void addValue(double value, double time) {
    	addValue(value);
    }
    
    /*
     * (non-Javadoc)
     * @see at.alladin.rmbt.android.graphview.GraphService#addValue(double, double, int)
     */
	public void addValue(double value, double time, int flag) {
		addValue(value);
	}

	/*
	 * (non-Javadoc)
	 * @see at.alladin.rmbt.android.graphview.GraphService#addValue(double, int)
	 */
	public void addValue(double value, int flag) {
		addValue(value);
	}
	
    /*
     * (non-Javadoc)
     * @see at.alladin.rmbt.android.test.Graph#addValue(double)
     */
    // 0 <= value <= 1
    public void addValue(double value)
    {
        final long relTime;
        if (startTime == -1)
        {
            startTime = System.nanoTime();
            relTime = 0;
        }
        else
            relTime = System.nanoTime() - startTime;
        
        if (value < 0)
            value = 0;
        else if (value > 1)
            value = 1;
        if (relTime >= maxNsecs)
            return;

        final float x = nsecWidth * relTime;
        if (values == 0)
        {
            startX = x;
            pathStroke.moveTo(x, (float) (height * (1 - value)));
            values++;
        }
        else
        {
            pathStroke.lineTo(x, (float) (height * (1 - value)));
            
            pathFill.rewind();
            pathFill.addPath(pathStroke);
            pathFill.lineTo(x, height);
            pathFill.lineTo(startX, height);
        }
    }
    
    public void draw(final Canvas canvas)
    {
        canvas.drawPath(pathStroke, paintStroke);
        canvas.drawPath(pathFill, paintFill);
    }
    
    public void reset()
    {
        pathStroke.rewind();
        pathFill.rewind();
        values = 0;
        startTime = -1;
    }
    
    public void clearGraphDontResetTime()
    {
        pathStroke.rewind();
        pathFill.rewind();
        values = 0;
    }
    
    public boolean hasBeenStarted()
    {
        return startTime != -1;
    }

	@Override
	public void setMaxTime(long maxTimeNs) {
		this.maxNsecs = maxTimeNs;
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
		options.putFloat(OPTION_STARTX, startX);
		options.putInt(OPTION_VALUES, values);
		return new GraphData(pathStroke, pathFill, paintStroke, paintFill, options);
	}
}
