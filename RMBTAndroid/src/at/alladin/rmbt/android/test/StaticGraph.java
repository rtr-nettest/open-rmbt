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

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PointF;

public class StaticGraph implements GraphService {
    // private final float width;
    private final float height;
    private final float width;
    
    private final Path pathStroke = new Path();
    private final Path pathFill = new Path();
    private final Paint paintStroke = new Paint();
    private final Paint paintFill = new Paint();
    private PointF firstPoint;
    
    private int values;
    
    private boolean matchHorizontally = false;
    
    public static StaticGraph addGraph(final at.alladin.rmbt.android.graphview.GraphView graphView, final int color) {
    	return StaticGraph.addGraph(graphView, color, true);
    }
    
    public static StaticGraph addGraph(final at.alladin.rmbt.android.graphview.GraphView graphView, final int color, final boolean matchHorizontally)
    {
        final StaticGraph graph = new StaticGraph(color, graphView.getGraphWidth(), graphView.getGraphHeight(),
                graphView.getGraphStrokeWidth());
        graph.setMatchHorizontally(matchHorizontally);
        graphView.addGraph(graph);
        return graph;
    }
    
    private StaticGraph(final int color, final float width, final float height, final float strokeWidth)
    {
        this.height = height;
        this.width = width;
        
        paintStroke.setColor(color);
        paintStroke.setAlpha(204); // 80%
        paintStroke.setStyle(Style.STROKE);
        paintStroke.setStrokeWidth(strokeWidth);
        paintStroke.setStrokeCap(Cap.ROUND);
        paintStroke.setStrokeJoin(Join.ROUND);
        paintStroke.setAntiAlias(true);
        
        paintFill.setColor(color);
        paintFill.setAlpha(51); // 20%
        paintFill.setStyle(Style.FILL);
        paintFill.setAntiAlias(true);
    }
    
    /*
     * (non-Javadoc)
     * @see at.alladin.rmbt.android.test.Graph#addValue(double)
     */
    public void addValue(double value) {
    	addValue(value, 0);
    }

    /*
     * (non-Javadoc)
     * @see at.alladin.rmbt.android.test.Graph#addValue(double, double)
     */
    public void addValue(double value, double time)
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
        
        if (values == 0)
        {
        	final float x = isMatchHorizontally() ? 0f : (float) (width * time);
        	final float y = (float) (height * (1 - value));
            pathStroke.moveTo(x, y);
            values++;
            firstPoint = new PointF(x, y);
        }
        else
        {
            final float x = (float) (width * time);
            final float y = (float) (height * (1 - value));
            pathStroke.lineTo(x, y);
            //System.out.println("x=" + x + ", y=" + y);
            
            pathFill.rewind();
            pathFill.addPath(pathStroke);
            pathFill.lineTo(x, height);
            if (firstPoint != null && !isMatchHorizontally()) {
            	pathFill.lineTo(firstPoint.x, height);
            }
            else if (isMatchHorizontally()) {
            	pathFill.lineTo(0, height);
            }
        }
    }
    
    public void draw(final Canvas canvas)
    {
    	if (values == 1 && isMatchHorizontally()) {
    		pathStroke.lineTo(width, firstPoint.y);
    		pathFill.rewind();
    		pathFill.addPath(pathStroke);
    		pathFill.lineTo(width, height);
    		pathFill.lineTo(0, height);
    		values++;
    	}
    	
        canvas.drawPath(pathStroke, paintStroke);
        canvas.drawPath(pathFill, paintFill);
    }
    
    public void reset()
    {
        pathStroke.rewind();
        pathFill.rewind();
        values = 0;
    }
    
    public boolean hasBeenStarted()
    {
        return true;
    }
    
    public void clearGraphDontResetTime()
    {
    	reset();
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
}
