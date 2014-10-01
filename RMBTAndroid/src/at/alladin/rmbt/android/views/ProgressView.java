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
package at.alladin.rmbt.android.views;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import at.alladin.openrmbt.android.R;
import at.alladin.rmbt.android.util.Helperfunctions;

public class ProgressView extends View
{
    private final static float DISTANCE = 5f;
    
    private final Bitmap progressFill;
    private final Bitmap progressEmpty;
    
    private int width;
    private final int height;
    
    private int numElements;
    private final float distance;
    
    private float progress;
    
    public ProgressView(final Context context, final AttributeSet attrs)
    {
        this(context, attrs, 0);
    }
    
    public ProgressView(final Context context)
    {
        this(context, null, 0);
    }
    
    public ProgressView(final Context context, final AttributeSet attrs, final int defStyle)
    {
        super(context, attrs, defStyle);
        
        final Resources res = context.getResources();
        
        progressEmpty = BitmapFactory.decodeResource(res, R.drawable.result_progress_darkblue);
        progressFill = BitmapFactory.decodeResource(res, R.drawable.result_progress_lightblue);
        
        final float density = getResources().getDisplayMetrics().density;
        height = Math.max(progressFill.getHeight(), progressEmpty.getHeight());
        distance = Helperfunctions.dpToPx(DISTANCE, density);
    }
    
    @Override
    protected void onSizeChanged(final int w, final int h, final int oldw, final int oldh)
    {
        super.onSizeChanged(w, h, oldw, oldh);
        // System.out.println(w+" "+h+" "+oldw+" "+oldh);
        // System.out.println(getWidth());
        
        width = w;
        numElements = width / Math.round(distance);
    }
    
    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec)
    {
        // System.out.println(widthMeasureSpec+" "+heightMeasureSpec);
        setMeasuredDimension(width, height);
    }
    
    public void setProgress(final float progress)
    {
        this.progress = progress;
        invalidate();
    }
    
    @Override
    protected void onDraw(final Canvas canvas)
    {
        final int numFill = Math.round(progress * numElements);
        for (int i = 0; i < numElements; i++)
            canvas.drawBitmap(i >= numFill ? progressEmpty : progressFill, i * distance, 0, null);
    }
}
