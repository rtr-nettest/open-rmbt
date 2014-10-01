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

public class Graph implements GraphService
{
    // private final float width;
    private final float height;
    private final float nsecWidth;
    
    private final Path pathStroke = new Path();
    private final Path pathFill = new Path();
    private final Paint paintStroke = new Paint();
    private final Paint paintFill = new Paint();
    
    private final long maxNsecs;
    private long startTime = -1;
    private int values;
    private float startX;
    
    public static Graph addGraph(final GraphView graphView, final int color, final long maxNsecs)
    {
        final Graph graph = new Graph(color, maxNsecs, graphView.getGraphWidth(), graphView.getGraphHeight(),
                graphView.getGraphStrokeWidth());
        graphView.addGraph(graph);
        return graph;
    }
    
    private Graph(final int color, final long maxNsecs, final float width, final float height, final float strokeWidth)
    {
        this.maxNsecs = maxNsecs;
        // this.width = width;
        this.height = height;
        nsecWidth = width / maxNsecs;
        
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
     * @see at.alladin.rmbt.android.test.Graph#addValue(double, double)
     */
    public void addValue(double value, double time) {
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
    
}
