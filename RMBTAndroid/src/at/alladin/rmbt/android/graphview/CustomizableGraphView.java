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
package at.alladin.rmbt.android.graphview;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import at.alladin.openrmbt.android.R;
import at.alladin.rmbt.android.util.net.NetworkUtil.MinMax;
import at.alladin.rmbt.util.NumberUtil;

/**
 * 
 * @author lb
 *
 */
public class CustomizableGraphView extends View implements GraphView {

	public static class PositionedGraphLabel extends GraphView.GraphLabel {
		private int x;
		private int y;
		
		public PositionedGraphLabel(String text, String color, int x, int y) {
			super(text, color);
			this.x = x;
			this.y = y;
		}

		public int getX() {
			return x;
		}

		public void setX(int x) {
			this.x = x;
		}

		public int getY() {
			return y;
		}

		public void setY(int y) {
			this.y = y;
		}

		@Override
		public String toString() {
			return "PositionedGraphLabel [x=" + x + ", y=" + y + ", color="
					+ color + ", text=" + text + "]";
		}
	}
	
    private boolean recycled;
    
    private List<GraphService> graphs = new ArrayList<GraphService>();
    private int width;
    private int height;
    
    private float scale = 1f;
    
    private Paint bitmapPaint;
    
    private int graphWidth;
    private int graphHeight;
    private float graphStrokeWidth;
    
    private Bitmap genBackgroundBitmap;
    private Bitmap gridBitmap;
    private int gridCells = 7;
    private float gridRows = 4;
    private float gridX;
    private float gridY;
    private float graphX;
    private float graphY;

    private String title;
    
    private List<GraphView.GraphLabel> labelHMinList = new ArrayList<GraphView.GraphLabel>();
    private List<GraphView.GraphLabel> labelHMaxList = new ArrayList<GraphView.GraphLabel>();
    private List<GraphView.GraphLabel> labelVMinList = new ArrayList<GraphView.GraphLabel>();
    private List<GraphView.GraphLabel> labelVMaxList = new ArrayList<GraphView.GraphLabel>();
    private List<GraphView.GraphLabel> labelRowList = new ArrayList<GraphView.GraphLabel>();
    private List<GraphView.GraphLabel> labelInfoVerticalList = new ArrayList<GraphView.GraphLabel>();
    private List<PositionedGraphLabel> labelList = new ArrayList<CustomizableGraphView.PositionedGraphLabel>();
    private static String DEFAULT_H_LABEL_COLOR = "#C8ffffff";
    private static String DEFAULT_V_LABEL_COLOR = "#C800f940";
    
    private String labelInfoHorizontal;

	private int internalPaddingTop;
	private boolean showLog10Lines = true;
    
	/**
	 * following parameters can be set in an xml layout file:
	 * <ul>
	 * <li>labelh - x-axis label</li>
 	 * <li>labelh_min - x-axis min value</li>
	 * <li>labelh_max - x-axis max value</li>
	 * <li>labelv - y-axis label</li>
 	 * <li>labelv_min - y-axis min value</li>
	 * <li>labelv_max - y-axis max value</li>
	 * <li>grid_cells - number of cells</li>
	 * <li>grid_rows - number of rows</li>
	 * <li>show_log10_lines - draws log10 grid lines for each row<li>
	 * </ul>
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
    public CustomizableGraphView(final Context context, final AttributeSet attrs, final int defStyle)
    {
        super(context, attrs, defStyle);

        String labelHMin;
        String labelHMax;
        String labelVMin;
        String labelVMax;

        labelInfoHorizontal = getAttributeValue(attrs, "labelh", "->");
        final String labelInfoVertical = getAttributeValue(attrs, "labelv", null);
        if (labelInfoVertical != null) {
        	labelInfoVerticalList.add(new GraphView.GraphLabel(labelInfoVertical, DEFAULT_V_LABEL_COLOR));
        }
        
        labelHMin = getAttributeValue(attrs, "labelh_min", "0");
        labelHMinList.add(new GraphView.GraphLabel(labelHMin, DEFAULT_H_LABEL_COLOR));
        
        labelHMax = getAttributeValue(attrs, "labelh_max", "10");
        labelHMaxList.add(new GraphView.GraphLabel(labelHMax, DEFAULT_H_LABEL_COLOR));

        labelVMin = getAttributeValue(attrs, "labelv_min", null);
        if (labelVMin != null) {
        	labelVMinList.add(new GraphView.GraphLabel(labelVMin, DEFAULT_V_LABEL_COLOR));
        }
        
        labelVMax = getAttributeValue(attrs, "labelv_max", null);
        if (labelVMax != null) {
        	labelVMaxList.add(new GraphView.GraphLabel(labelVMax, DEFAULT_V_LABEL_COLOR));
        }

        this.gridCells = Integer.valueOf(getAttributeValue(attrs, "grid_cells", "7"));
        this.gridRows = Integer.valueOf(getAttributeValue(attrs, "grid_rows", "4"));
        this.showLog10Lines = Boolean.valueOf(getAttributeValue(attrs, "show_log10_lines", "true"));
        
        title = getAttributeValue(attrs, "title", null);

        repaint(context);
    }
    
    final int relW = 593;
    final int relH = 237;
    
    public void repaint(final Context context) {
        final Resources res = context.getResources();
        
        bitmapPaint = new Paint();
        bitmapPaint.setFilterBitmap(true);
              
        internalPaddingTop = 0;
        
        final Bitmap backgroundBitmap = BitmapFactory.decodeResource(res, R.drawable.test_box_small);
        width = backgroundBitmap.getWidth();
        height = backgroundBitmap.getHeight();

        if (genBackgroundBitmap != null) {
        	genBackgroundBitmap.recycle();
        	genBackgroundBitmap = null;
        }
        
        genBackgroundBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(genBackgroundBitmap);
        
        final Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        paint.setTextSize(coordFH(15, relH));
        
        for (int i = 0; i < labelHMinList.size(); i++) {
        	GraphView.GraphLabel label = labelHMinList.get(i);
            paint.setColor(Color.parseColor(label.getColor()));
            paint.setTextAlign(Align.LEFT);
            canvas.drawText(label.getText(), coordFW(88 + i * 60, relW), coordFH(220, relH), paint);        	
        }
        
        for (int i = 0; i < labelHMaxList.size(); i++) {
        	GraphView.GraphLabel label = labelHMaxList.get(i);
            paint.setColor(Color.parseColor(label.getColor()));
            paint.setTextAlign(Align.RIGHT);
            canvas.drawText(label.getText(), coordFW(567 - i * 60, relW), coordFH(220, relH), paint);        	
        }

        paint.setTextAlign(Align.CENTER);
        canvas.drawText(labelInfoHorizontal, coordFW(326, relW), coordFH(220, relH), paint);
        
        paint.setTextAlign(Align.LEFT);
        paint.setColor(Color.parseColor("#C800f940"));

        final int vLabelStartY = (labelInfoVerticalList.size() % 2 == 0 ? 140 : 130) - (20 * labelInfoVerticalList.size()); 
        for (int i = 0; i < labelInfoVerticalList.size(); i++) {
        	final GraphView.GraphLabel label = labelInfoVerticalList.get(i);
        	paint.setColor(Color.parseColor(label.getColor()));
            canvas.drawText(String.format("– %s", label.getText()), coordFW(2, relW), coordFH(vLabelStartY + i * 20, relH), paint);
        }
        
        for (int i = 0; i < labelList.size(); i++) {
        	final PositionedGraphLabel label = labelList.get(i);
            paint.setColor(Color.parseColor(label.getColor()));
            paint.setTextAlign(Align.LEFT);
            canvas.drawText(label.getText(), label.getX(), label.getY(), paint);
        }     
   
        paint.setTextAlign(Align.RIGHT);
        for (int i = 0; i < labelVMinList.size(); i++) {
        	final  GraphView.GraphLabel label = labelVMinList.get(i);
            paint.setColor(Color.parseColor(label.getColor()));
            canvas.drawText(label.getText(), coordFW(72, relW), coordFH(190 - i * 20, relH), paint);        	
        }
        for (int i = 0; i < labelVMaxList.size(); i++) {
        	final GraphView.GraphLabel label = labelVMaxList.get(i);
            paint.setColor(Color.parseColor(label.getColor()));
            canvas.drawText(label.getText(), coordFW(72, relW), coordFH(38 + i * 20, relH), paint);        	
        }

        paint.setColor(Color.parseColor("#C8f8a000"));
        
        if (title != null) {
        	paint.setTextAlign(Align.LEFT);
        	paint.setTextSize(coordFH(18, relH));
        	//paint.setUnderlineText(true);
        	paint.setShadowLayer(0.5f, 2, 2, Color.parseColor("#FF000000"));
            canvas.drawText(title, coordFW(112, relW), coordFH(38, relH), paint);
        }
        
//        gridBitmap = getBitmap(res, R.drawable.test_grid);
        gridBitmap = generateGridBitmap();
        gridX = coordFW(55, relW);
        gridY = coordFH(16, relH);
        graphX = coordFW(80, relW);
        graphY = coordFH(16, relH);
        graphWidth = coordW(493, relW);
        graphHeight = coordH(183, relH);
        graphStrokeWidth = coordFW(4, relW);
    }
    
    public CustomizableGraphView(final Context context, final AttributeSet attrs)
    {
        this(context, attrs, 0);
    }
    
    public CustomizableGraphView(final Context context)
    {
        this(context, null, 0);
    }
    
    protected Bitmap generateGridBitmap() {
    	return generateGridBitmap(null);
    }
    
    protected Bitmap generateGridBitmap(Canvas canvas) {
    	int x = 20;
    	int y = 0;
    	int h = 180;
    	int w = 520;
    	
    	float startX = coordFW(x, relW);
    	float endX = coordFW(w, relW);
    	float startY =  coordFH(y, relW);
    	float endY = coordFH(h, relH);
    	
    	int partH = Math.round((float)(h-y) / (float)gridRows);
    	int partW = Math.round((float)(w-x) / (float)gridCells); 
    	
    	Bitmap grid = Bitmap.createBitmap(width, height, Config.ARGB_8888);
    	Canvas gridCanvas = new Canvas(grid);
    	Paint p = new Paint();
    	p.setColor(Color.WHITE);
    	p.setAlpha(100);
    	p.setStyle(Style.STROKE);
    	
        final Paint paintNames = new Paint();
        paintNames.setAntiAlias(true);
        paintNames.setTypeface(Typeface.DEFAULT_BOLD);
        paintNames.setTextSize(coordFH(15, relH));
        paintNames.setTextAlign(Align.RIGHT);
        
    	gridCanvas.drawRect(startX, startY, endX, endY, p);
    	for (int cy = 0; cy <= gridRows; cy++) {
    		final int posY = Math.max(h - partH * cy, 0);
    		gridCanvas.drawLine((cy == 0 || cy == gridRows ? 0 : coordFW(x - 5, relW)), 
    				coordFH(posY, relH), endX, coordFH(posY, relH), p);
    		
    		if (showLog10Lines) {
    			p.setAlpha(30);
	    		for (int logy = 1; logy < 10; logy++) {
	    			final int log = (int) (Math.log10(logy) * partH);
	        		gridCanvas.drawLine(coordFW(x, relW), coordFH(h - (partH * cy) - log, relH), endX, coordFH(h - (partH * cy) - log, relH), p);
	
	    		}
				p.setAlpha(100);
    		}    		
    	}

    	if (!NumberUtil.isInteger(gridRows)) {
    		gridCanvas.drawLine(0, coordFH(0, relH), endX, coordFH(0, relH), p);
    	}
    	
    	if (labelRowList != null && canvas != null) {
    		for (int i = 0; i < labelRowList.size(); i++) {
    			final  GraphView.GraphLabel label = labelRowList.get(i);
            	paintNames.setColor(Color.parseColor(label.getColor()));
            	canvas.drawText(label.getText(), coordFW((int) (gridX - 3), relW), coordFH((int) ((gridY + h) - partH * i + 7), relH), paintNames);
    		}
		}

    	
    	for (int cx = 1; cx < gridCells; cx++) {
    		final float posX = coordFW(partW * cx, relW) + startX;
    		gridCanvas.drawLine(posX, startY, posX, endY, p);    		
    	}
    	
    	return grid;
    }
    
    public void updateGrid(int cells, float rows) {
    	this.gridCells = cells;
    	this.gridRows = rows;
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
    	try {
	        if (recycled)
	            return;
	
	        repaint(getContext());
	        final int canvasSave = canvas.save();
	        canvas.translate(getPaddingLeft(), getPaddingTop());
	        canvas.translate(0, internalPaddingTop);
	        canvas.scale(scale, scale);
	        
	        if (!genBackgroundBitmap.isRecycled()) {
	        	canvas.drawBitmap(genBackgroundBitmap, 0, 0, bitmapPaint);
	        }
	  
	       	gridBitmap = null;
	       	gridBitmap = generateGridBitmap(canvas);
	        
	    	canvas.drawBitmap(gridBitmap, gridX, gridY, bitmapPaint);
	        
	        final int canvasSave2 = canvas.save();
	        canvas.translate(graphX, graphY);
	
	        for (final GraphService graph : graphs)
	            graph.draw(canvas);
	        
	        canvas.restoreToCount(canvasSave2);
	//        if (!reflectionBitmap.isRecycled()) {
	//        	canvas.drawBitmap(reflectionBitmap, reflectionX, reflectionY, bitmapPaint);
	//        }
	        
	        canvas.restoreToCount(canvasSave);
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    	}
    }
    
    public void addGraph(final GraphService graph)
    {
        graphs.add(graph);
    }
    
    public void recycle()
    {
        Log.d("GraphView", "recycling");
        recycled = true;
        genBackgroundBitmap.recycle();
        genBackgroundBitmap = null;
        gridBitmap.recycle();
        gridBitmap = null;
    }
    
    public String getAttributeValue(AttributeSet attr, String attrName, String defaultValue) {
    	String value;
    	int resId = attr.getAttributeResourceValue(null, attrName, -1);
    	
        if (resId < 0) {
        	value = attr.getAttributeValue(null, attrName);
        }
        else {
        	value = getContext().getResources().getString(resId);
        }
        
        return (value != null ? value : defaultValue);
    }
    
    /**
     * 
     * @return
     */
    public List<GraphService> getGraphs() {
    	return graphs;
    }
    
    public int getGraphSize() {
    	return (graphs == null ? 0 : graphs.size());
    }

    public final static int LABELLIST_ALL = 0;
    public final static int LABELLIST_HORIZONTAL_MIN = 1;
    public final static int LABELLIST_HORIZONTAL_MAX = 2;
    public final static int LABELLIST_VERTICAL_MIN = 3;
    public final static int LABELLIST_VERTICAL_MAX = 4;
    
    public void clearLabels(int labelList) {
    	switch (labelList) {
    	case LABELLIST_ALL:
    		getLabelHMaxList().clear();
    		getLabelHMinList().clear();
    		getLabelVMaxList().clear();
    		getLabelVMinList().clear();
    		break;
    		
    	case LABELLIST_HORIZONTAL_MAX:
    		getLabelHMaxList().clear();
    		break;
    		
    	case LABELLIST_HORIZONTAL_MIN:
    		getLabelHMinList().clear();
    		break;
    		
    	case LABELLIST_VERTICAL_MAX:
    		getLabelVMaxList().clear();
    		break;
    		
    	case LABELLIST_VERTICAL_MIN:
    		getLabelVMinList().clear();
    		break;
    	}
    }
    
	public void addLabelHMin(String labelHMin) {
		addLabelHMin(labelHMin, DEFAULT_H_LABEL_COLOR);
	}
	
	public void addLabelHMin(String labelHMin, String color) {
		this.labelHMinList.add(new GraphView.GraphLabel(labelHMin, color));
	}

	public void addLabelHMax(String labelHMax) {
		addLabelHMax(labelHMax, DEFAULT_H_LABEL_COLOR);
	}

	public void addLabelHMax(String labelHMax, String color) {
		this.labelHMaxList.add(new GraphView.GraphLabel(labelHMax, color));
	}

	public void addLabelVMin(String labelVMin) {
		addLabelVMin(labelVMin, DEFAULT_V_LABEL_COLOR);
	}
	
	public void addLabelVMin(String labelVMin, String color) {
		this.labelVMinList.add(new GraphView.GraphLabel(labelVMin, color));
	}

	public void addLabelVMax(String labelVMax) {
		addLabelVMax(labelVMax, DEFAULT_V_LABEL_COLOR);
	}
	
	public void addLabelVMax(String labelVMax, String color) {
		this.labelVMaxList.add(new GraphView.GraphLabel(labelVMax, color));
	}

	public String getLabelInfoHorizontal() {
		return labelInfoHorizontal;
	}

	public void setLabelInfoHorizontal(String labelInfoHorizontal) {
		this.labelInfoHorizontal = labelInfoHorizontal;
	}

	public List<GraphView.GraphLabel> getLabelInfoVerticalList() {
		return labelInfoVerticalList;
	}

	public void setLabelInfoVerticalList(List<GraphView.GraphLabel> labelInfoVerticalList) {
		this.labelInfoVerticalList = labelInfoVerticalList;
	}

	public List<GraphView.GraphLabel> getLabelHMinList() {
		return labelHMinList;
	}

	public void setLabelHMinList(List<GraphView.GraphLabel> labelHMinList) {
		this.labelHMinList = labelHMinList;
	}

	public List<GraphView.GraphLabel> getLabelHMaxList() {
		return labelHMaxList;
	}

	public void setLabelHMaxList(List<GraphView.GraphLabel> labelHMaxList) {
		this.labelHMaxList = labelHMaxList;
	}

	public List<GraphView.GraphLabel> getLabelVMinList() {
		return labelVMinList;
	}

	public void setLabelVMinList(List<GraphView.GraphLabel> labelVMinList) {
		this.labelVMinList = labelVMinList;
	}

	public List<GraphView.GraphLabel> getLabelVMaxList() {
		return labelVMaxList;
	}

	public void setLabelVMaxList(List<GraphView.GraphLabel> labelVMaxList) {
		this.labelVMaxList = labelVMaxList;
	}

	public List<PositionedGraphLabel> getLabelList() {
		return labelList;
	}

	public void setLabelList(List<PositionedGraphLabel> labelList) {
		this.labelList = labelList;
	}
	
	public void addLabel(float x, float y, String text, String color) {
		PositionedGraphLabel label = createPositionedGraphLabel(x, y, text, color);
    	labelList.add(label);
	}
	
	public PositionedGraphLabel createPositionedGraphLabel(float x, float y, String text, String color) {
        final float relW = 593;
        final float relH = 220;
        
        final int posx = (int) coordFW((int) (83f + x * 500f) , (int) relW);
        final int posy = (int) coordFH((int) (220f - y * 207f), (int) relH);
        
    	return new PositionedGraphLabel(text, color, posx, posy);		
	}

	@Override
	public void setSignalRange(int min, int max) {
		labelVMaxList.add(new GraphView.GraphLabel(String.valueOf(max), "#C8f8a000"));
		labelVMinList.add(new GraphView.GraphLabel(String.valueOf(min), "#C8f8a000"));
	}

	@Override
	public void removeSignalRange() {
		labelVMaxList.remove(labelVMaxList.size()-1);
		labelVMinList.remove(labelVMinList.size()-1);
	}

	@Override
	public MinMax<Integer> getSignalRange() {
		try {
			if (labelVMinList.size() >= 1 && labelVMaxList.size() >= 1) {
				Integer min = Integer.valueOf(labelVMinList.get(labelVMinList.size()-1).getText());
				Integer max = Integer.valueOf(labelVMaxList.get(labelVMaxList.size()-1).getText());
				return new MinMax<Integer>(min, max);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}

	@Override
	public List<GraphLabel> getRowLinesLabelList() {
		return labelRowList;
	}

	@Override
	public void setRowLinesLabelList(List<GraphLabel> rowLabelList) {
		labelRowList = rowLabelList;
	}
}