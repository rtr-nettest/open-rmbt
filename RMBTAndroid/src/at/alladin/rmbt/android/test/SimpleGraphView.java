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

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import at.alladin.openrmbt.android.R;
import at.alladin.rmbt.android.graphview.GraphService;
import at.alladin.rmbt.android.graphview.GraphView;
import at.alladin.rmbt.android.util.net.NetworkUtil.MinMax;

public class SimpleGraphView extends View implements GraphView
{
    private boolean recycled;
    
    final List<GraphService> graphs = new ArrayList<GraphService>();
    final int width;
    final int height;
    
    private float scale = 1f;
    
    private final int relW = 593;
    private final int relH = 237;
    
    private final Paint bitmapPaint;
    private final Paint signalTextPaint;
    
    private final String signalText;
    
    final int graphWidth;
    final int graphHeight;
    final float graphStrokeWidth;
    
    final Bitmap genBackgroundBitmap;
    final Bitmap gridBitmap;
    final float gridX;
    final float gridY;
    final float graphX;
    final float graphY;
    
    private Integer signalMin;
    private Integer signalMax;
    
    public SimpleGraphView(final Context context, final AttributeSet attrs, final int defStyle)
    {
        super(context, attrs, defStyle);
        
        final Resources res = context.getResources();
        
        bitmapPaint = new Paint();
        bitmapPaint.setFilterBitmap(true);
        
        final Bitmap backgroundBitmap = BitmapFactory.decodeResource(res, R.drawable.test_box_small);
        width = backgroundBitmap.getWidth();
        height = backgroundBitmap.getHeight();
        genBackgroundBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(genBackgroundBitmap);
        //canvas.drawBitmap(backgroundBitmap, 0, 0, bitmapPaint);
        
        if (! isInEditMode())
            backgroundBitmap.recycle();
        
        final Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        paint.setTextSize(coordFH(15, relH));
        paint.setColor(Color.parseColor("#C8ffffff"));
        paint.setTextAlign(Align.LEFT);
        canvas.drawText("0", coordFW(88, relW), coordFH(220, relH), paint);
        paint.setTextAlign(Align.RIGHT);
        canvas.drawText("8", coordFW(567, relW), coordFH(220, relH), paint);
        paint.setTextAlign(Align.CENTER);
        canvas.drawText("s", coordFW(326, relW), coordFH(220, relH), paint);
        
        paint.setTextAlign(Align.LEFT);
        paint.setColor(Color.parseColor("#C800f940"));
        canvas.drawText(String.format("– %s", res.getString(R.string.test_mbps)), coordFW(9, relW), coordFH(110, relH),
                paint);
        paint.setTextAlign(Align.RIGHT);
        canvas.drawText("0", coordFW(72, relW), coordFH(220, relH), paint);
        canvas.drawText("100", coordFW(72, relW), coordFH(38, relH), paint);
        
        gridBitmap = BitmapFactory.decodeResource(res, R.drawable.test_grid);
        gridX = coordFW(55, relW);
        gridY = coordFH(16, relH);
        graphX = coordFW(80, relW);
        graphY = coordFH(16, relH);
        graphWidth = coordW(493, relW);
        graphHeight = coordH(183, relH);
        graphStrokeWidth = coordFW(4, relW);
        
        signalTextPaint = new Paint();
        signalTextPaint.setAntiAlias(true);
        signalTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
        signalTextPaint.setTextSize(coordFH(15, relH));
        signalTextPaint.setColor(Color.parseColor("#C8f8a000"));
        
        signalText = res.getString(R.string.test_dbm);
    }
    
    public SimpleGraphView(final Context context, final AttributeSet attrs)
    {
        this(context, attrs, 0);
    }
    
    public SimpleGraphView(final Context context)
    {
        this(context, null, 0);
    }
    
    protected float coordFW(final int x, final int y)
    {
        return (float) x / y * width;
    }
    
    protected float coordFH(final int x, final int y)
    {
        return (float) x / y * height;
    }
    
    protected int coordW(final int x, final int y)
    {
        return Math.round((float) x / y * width);
    }
    
    protected int coordH(final int x, final int y)
    {
        return Math.round((float) x / y * height);
    }
    
    public int getGraphWidth()
    {
        return graphWidth;
    }
    
    public int getGraphHeight()
    {
        return graphHeight;
    }
    
    public float getGraphStrokeWidth()
    {
        return graphStrokeWidth;
    }
    
    public void setSignalRange(int min, int max)
    {
        signalMin = min;
        signalMax = max;
    }
    
    public void removeSignalRange()
    {
        signalMin = null;
        signalMax = null;
    }
    
    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec)
    {
        final int paddingH = getPaddingLeft() + getPaddingRight();
        final int paddingW = getPaddingTop() + getPaddingBottom();
        
        // super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int w = MeasureSpec.getSize(widthMeasureSpec);
        final int newW = width + paddingH;
        switch (MeasureSpec.getMode(widthMeasureSpec))
        {
        case MeasureSpec.AT_MOST:
            if (newW < w)
                w = newW;
            break;
        
        case MeasureSpec.EXACTLY:
            break;
        
        case MeasureSpec.UNSPECIFIED:
            w = newW;
            break;
        }
        scale = (float) (w - getPaddingLeft() - getPaddingRight()) / width;
        
        int h = MeasureSpec.getSize(heightMeasureSpec);
        final int newH = Math.round(height * scale) + paddingW;
        switch (MeasureSpec.getMode(heightMeasureSpec))
        {
        case MeasureSpec.AT_MOST:
            if (newH < h)
                h = newH;
            break;
        
        case MeasureSpec.EXACTLY:
            break;
        
        case MeasureSpec.UNSPECIFIED:
            h = newH;
            break;
        }
        
        setMeasuredDimension(w, h);
    }
    
    @Override
    protected void onDraw(final Canvas canvas)
    {
        if (recycled)
            return;
        
        final int canvasSave = canvas.save();
        
        canvas.translate(getPaddingLeft(), getPaddingTop());
        canvas.scale(scale, scale);
        
        canvas.drawBitmap(genBackgroundBitmap, 0, 0, bitmapPaint);
        
        final boolean drawSignal = signalMin != null && signalMax != null;
        if (drawSignal)
        {
            signalTextPaint.setTextAlign(Align.LEFT);
            canvas.drawText(String.format("– %s", signalText), coordFW(9, relW), coordFH(130, relH),
                    signalTextPaint);
            signalTextPaint.setTextAlign(Align.RIGHT);
            canvas.drawText(Integer.toString(signalMin), coordFW(72, relW), coordFH(195, relH), signalTextPaint);
            canvas.drawText(Integer.toString(signalMax), coordFW(72, relW), coordFH(58, relH), signalTextPaint);
        }

        canvas.drawBitmap(gridBitmap, gridX, gridY, bitmapPaint);
        
        final int canvasSave2 = canvas.save();
        canvas.translate(graphX, graphY);
        
        for (final GraphService graph : graphs)
            graph.draw(canvas);
        
        canvas.restoreToCount(canvasSave2);
        
        canvas.restoreToCount(canvasSave);
    }
    
    public void addGraph(final GraphService graph)
    {
        graphs.add(graph);
    }
    
    public void recycle()
    {
        recycled = true;
        genBackgroundBitmap.recycle();
        gridBitmap.recycle();
    }

	@Override
	public MinMax<Integer> getSignalRange() {
		return new MinMax<Integer>(signalMin, signalMax);
	}

	@Override
	public List<GraphLabel> getLabelInfoVerticalList() {
		final ArrayList<GraphLabel> graphLabelList = new ArrayList<GraphView.GraphLabel>();
		graphLabelList.add(new GraphLabel(signalText, GraphLabel.colorToHex(signalTextPaint.getColor())));
		return graphLabelList;
	}

	@Override
	public void setLabelInfoVerticalList(List<GraphLabel> labelInfoVerticalList) {
		//not supported by this view
	}

	@Override
	public void updateGrid(int cells, float rows) {
		//not supported by this view
	}

	@Override
	public List<GraphLabel> getRowLinesLabelList() {
		//not supported by this view
		return null;
	}

	@Override
	public void setRowLinesLabelList(List<GraphLabel> rowLabelList) {
		//not supported by this view
	}
}
