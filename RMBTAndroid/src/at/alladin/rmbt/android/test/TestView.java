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
import at.alladin.rmbt.android.util.InformationCollector;
import at.alladin.rmbt.client.helper.TestStatus;

public class TestView extends View
{
	public final static int POS_PING_Y  = 78;
	public final static int POS_DOWN_Y  = 115;
	public final static int POS_UP_Y  = 152;
	
    private boolean recycled;
    
    private final Bitmap genBackgroundBitmap;
    
    private float scale = 1f;
    
    private final int internalPaddingLeft;
    private final int internalPaddingTop;
    
//    private final Bitmap reflectionBitmap;
//    private final float reflectionX;
//    private final float reflectionY;
    
//    private final Bitmap resultBackgroundBitmap;
    private final Bitmap speedStatusDownBitmap;
    private final Bitmap speedStatusUpBitmap;
    
    private final int width;
    private final int height;
    private final Gauge speedGauge;
    private final Gauge progressGauge;
    private final Gauge signalGauge;
    
//    private final float resultBgX;
//    private final float resultBgY;
    
    private final float speedStatusDownX;
    private final float speedStatusDownY;
    private final float speedStatusUpX;
    private final float speedStatusUpY;
    
    private final Paint bitmapPaint;
    private final Paint headerPaint;
    private final Paint resultPaint;
    private final Paint progressPaint;
    private final Paint signalPaint;
    
    private final float headerX;
    private final float headerY;
    private final float subHeaderX;
    private final float subHeaderY;
    
    private final float resultPingX;
    private final float resultPingY;
    private final float resultDownX;
    private final float resultDownY;
    private final float resultUpX;
    private final float resultUpY;
    
    private final float progressX;
    private final float progressY;
    
    private final float signalX;
    private final float signalY;
    
    private String headerString;
    private String subHeaderString;
    private String progressString;
    private String signalString;
    private int signalType = InformationCollector.SINGAL_TYPE_NO_SIGNAL;
    private String resultPingString;
    private String resultDownString;
    private String resultUpString;
    private TestStatus testStatus;

    private float signalRingX;
    private float signalRingY;
    private Bitmap signalRingBitmapMobile;
    private Bitmap signalRingBitmapRsrp;
    private Bitmap signalRingBitmapWlan;

    private String signalDescriptionString;
    private float signalDescriptionX;
    private float signalDescriptionY;
    private Paint signalDescriptionPaint;

    private Bitmap signalStatusBgBitmap;

    private float signalStatusBgX;

    private float signalStatusBgY;
    
    public TestView(final Context context, final AttributeSet attrs, final int defStyle)
    {
        super(context, attrs, defStyle);
        
        // scale = attrs.getAttributeFloatValue(null, "scale", 1f);
        
        final Resources res = context.getResources();
        
        bitmapPaint = new Paint();
        bitmapPaint.setFilterBitmap(true);
        
        final Bitmap backgroundBitmap = getBitmap(res,R.drawable.test_box_large);
        width = backgroundBitmap.getWidth();
        height = backgroundBitmap.getHeight();
        
        final int relW = 610;
        final int relH = 560;
        
        internalPaddingLeft = coordW(18, relW);
        internalPaddingTop = coordW(-30, relH);
        
        final Bitmap speedRingBitmap = getBitmap(res, R.drawable.ringskala_speed);
        final float speedRingX = coordFW(114, relW);
        final float speedRingY = coordFH(285, relH);
        final Bitmap speedStatusBgBitmap = getBitmap(res, R.drawable.statuscircles_speed);
        final float speedStatusBgX = coordFW(127, relW);
        final float speedStatusBgY = coordFH(312, relH);
        final Bitmap progressRingBitmap = getBitmap(res, R.drawable.ringskala_progress);
        final float progressRingX = coordFW(292, relW);
        final float progressRingY = coordFH(89, relH);
        final Bitmap progressStatusBgBitmap = getBitmap(res, R.drawable.statuscircles_progress);
        final float progressStatusBgX = coordFW(320, relW);
        final float progressStatusBgY = coordFH(125, relH);
        signalRingBitmapMobile = getBitmap(res, R.drawable.ringskala_signal_mobile);
        signalRingBitmapRsrp = getBitmap(res, R.drawable.ringskala_signal_rsrp);
        signalRingBitmapWlan = getBitmap(res, R.drawable.ringskala_signal_wlan);
        signalRingX = coordFW(382, relW);
        signalRingY = coordFH(348, relH);
        signalStatusBgBitmap = getBitmap(res, R.drawable.statuscircles_signal);
        signalStatusBgX = coordFW(370, relW);
        signalStatusBgY = coordFH(344, relH);
        
        speedStatusDownBitmap = getBitmap(res, R.drawable.statuscircles_speed_download);
        speedStatusDownX = coordFW(194, relW);
        speedStatusDownY = coordFH(354, relH);
        speedStatusUpBitmap = getBitmap(res, R.drawable.statuscircles_speed_upload);
        speedStatusUpX = coordFW(194, relW);
        speedStatusUpY = coordFH(349, relH);
        
        genBackgroundBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(genBackgroundBitmap);
        
        //canvas.drawBitmap(backgroundBitmap, 0, 0, bitmapPaint);
        //canvas.drawBitmap(resultBackgroundBitmap, resultBgX, resultBgY, bitmapPaint);
        //if (! isInEditMode())
        //    backgroundBitmap.recycle();
        
        canvas.drawBitmap(speedRingBitmap, speedRingX, speedRingY, bitmapPaint);
        speedRingBitmap.recycle();
        canvas.drawBitmap(speedStatusBgBitmap, speedStatusBgX, speedStatusBgY , bitmapPaint);
        speedStatusBgBitmap.recycle();
        
        canvas.drawBitmap(progressRingBitmap, progressRingX, progressRingY, bitmapPaint);
        progressRingBitmap.recycle();
        canvas.drawBitmap(progressStatusBgBitmap, progressStatusBgX, progressStatusBgY, bitmapPaint);
        progressStatusBgBitmap.recycle();
        
        final Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        paint.setFakeBoldText(true);
        paint.setColor(Color.parseColor("#ffffff"));
        paint.setTextSize(coordFH(23, relH));
        paint.setTextAlign(Align.LEFT);
        canvas.drawText("Ping:", coordFW(20, relW), coordFH(POS_PING_Y, relH), paint);
        canvas.drawText("Down:", coordFW(20, relW), coordFH(POS_DOWN_Y, relH), paint);
        canvas.drawText("Up:", coordFW(20, relW), coordFH(POS_UP_Y, relH), paint);
        
        paint.setColor(Color.parseColor("#32c90e"));
        paint.setTextAlign(Align.CENTER);
        paint.setTextSize(coordFH(14, relH));
        canvas.drawText(res.getString(R.string.test_mbps), coordFW(216, relW), coordFH(418, relH), paint);
        paint.setColor(Color.parseColor("#ffa200"));
        signalDescriptionString = res.getString(R.string.test_dbm);
        signalDescriptionX = coordFW(432, relW);
        signalDescriptionY = coordFH(427, relH);
        signalDescriptionPaint = paint;
        
        Bitmap background, dynamic, foreground;
        int ih, iw, x, y;
        
        background = getBitmap(res, R.drawable.test_gauge_speed_background);
        dynamic = getBitmap(res, R.drawable.test_gauge_speed_dynamic);
        foreground = getBitmap(res, R.drawable.test_gauge_speed_foreground);
        speedGauge = new Gauge(2f, 302.38f, background, dynamic, foreground, 17f / 325, 18f / 325, 302f / 325,
                303f / 325);
        iw = speedGauge.getIntrinsicWidth();
        ih = speedGauge.getIntrinsicHeight();
        x = coordW(56, 610);
        y = coordH(228, 560);
        speedGauge.setBounds(x, y, x + iw, y + ih);
        
        background = getBitmap(res, R.drawable.test_gauge_progress_background);
        dynamic = getBitmap(res, R.drawable.test_gauge_progress_dynamic);
        foreground = getBitmap(res, R.drawable.test_gauge_progress_foreground);
        progressGauge = new Gauge(210f, 270f, background, dynamic, foreground, -2 / 300, 16f / 318, 280f / 300,
                298f / 318);
        iw = progressGauge.getIntrinsicWidth();
        ih = progressGauge.getIntrinsicHeight();
        x = coordW(247, 610);
        y = coordH(33, 560);
        progressGauge.setBounds(x, y, x + iw, y + ih);
        
        background = getBitmap(res, R.drawable.test_gauge_signal_background);
        dynamic = getBitmap(res, R.drawable.test_gauge_signal_dynamic);
        foreground = getBitmap(res, R.drawable.test_gauge_signal_foreground);
        signalGauge = new Gauge(118.75f, -155.88f, background, dynamic, foreground, -46 / 201f, -32 / 215f, 183 / 201f,
                197 / 215f);
        iw = signalGauge.getIntrinsicWidth();
        ih = signalGauge.getIntrinsicHeight();
        x = coordW(364, 610);
        y = coordH(322, 560);
        signalGauge.setBounds(x, y, x + iw, y + ih);
        
        resultPaint = new Paint();
        resultPaint.setAntiAlias(true);
        resultPaint.setLinearText(true);
        resultPaint.setTypeface(Typeface.DEFAULT_BOLD);
        resultPaint.setColor(Color.parseColor("#ffffff")); //#002c44
        resultPaint.setTextSize(coordFH(23, relH));
        resultPaint.setTextAlign(Align.LEFT);
        
        progressPaint = new Paint(resultPaint);
        progressPaint.setColor(Color.WHITE);
        progressPaint.setTextSize(coordFH(30, relH));
        progressPaint.setTextAlign(Align.CENTER);
        
        signalPaint = new Paint(progressPaint);
        signalPaint.setTextSize(coordFH(20, relH));
        
        headerPaint = new Paint(resultPaint);
        headerPaint.setTextSize(coordFH(19, relH));
        
        headerX = coordFW(10, relW);
        headerY = coordFH(relH + 10, relH); //33
        subHeaderX = coordFW(relW - 20, relW);
        subHeaderY = coordFH(relH + 10, relH); //60
        
        resultPingX = coordFW(90, relW);
        resultPingY = coordFH(POS_PING_Y, relH);
        resultDownX = coordFW(90, relW);
        resultDownY = coordFH(POS_DOWN_Y, relH);
        resultUpX = coordFW(90, relW);
        resultUpY = coordFH(POS_UP_Y, relH);
        
        progressX = coordFW(386, relW);
        progressY = coordFH(203, relH);
        
        signalX = coordFW(427, relW);
        signalY = coordFH(409, relH);
    }
    
    protected Bitmap getBitmap(Resources res, int id)
    {
        return BitmapFactory.decodeResource(res, id);
    }
    
    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec)
    {
        final int paddingH = getPaddingLeft() + internalPaddingLeft + getPaddingRight();
        final int paddingW = getPaddingTop() + internalPaddingTop + getPaddingBottom();
        
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
        scale = (float) (w - getPaddingLeft() - getPaddingRight()) / (width + internalPaddingLeft);
        
        int h = MeasureSpec.getSize(heightMeasureSpec);
        final int newH = Math.round((height + 20) * scale) + paddingW;
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
    
    public TestView(final Context context, final AttributeSet attrs)
    {
        this(context, attrs, 0);
    }
    
    public TestView(final Context context)
    {
        this(context, null, 0);
    }
    
    public void setSpeedValue(final double speedValueRelative)
    {
        speedGauge.setValue(speedValueRelative);
    }
    
    public double setProgressValue(final double progressValue)
    {
    	final double currentValue = progressGauge.value;
        progressGauge.setValue(progressValue >= currentValue ? progressValue : currentValue);
        return progressGauge.value;
    }
    
    public void setSignalValue(final double relativeSignal)
    {
        signalGauge.setValue(relativeSignal);
    }
    
    public void setHeaderString(final String headerString)
    {
        this.headerString = headerString;
    	//this.headerString = "TestTest_0TestTest_0TestTest_012";
    }
    
    public void setSubHeaderString(final String subHeaderString)
    {
        this.subHeaderString = subHeaderString;
    }
    
    public void setResultPingString(final String resultPingString)
    {
        this.resultPingString = resultPingString;
    }
    
    public void setResultDownString(final String resultDownString)
    {
        this.resultDownString = resultDownString;
    }
    
    public void setResultUpString(final String resultUpString)
    {
        this.resultUpString = resultUpString;
    }
    
    public void setProgressString(final String progressString)
    {
        this.progressString = progressString;
    }
    
    public void setSignalString(final String signalString)
    {
        this.signalString = signalString;
    }
    
    public void setSignalType(final int signalType)
    {
        this.signalType = signalType;
    }
    
    public void setTestStatus(final TestStatus testStatus)
    {
        this.testStatus = testStatus;
    }
    
    // @Override
    // protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    // {
    // get
    // Log.d("TestView",
    // getSuggestedMinimumWidth()+"/"+getSuggestedMinimumHeight());
    // setMeasuredDimension(Math.round(width * scale), Math.round(height *
    // scale));
    // }
    
    @Override
    protected void onDraw(final Canvas canvas)
    {
        if (recycled)
            return;
        
        final int saveCount = canvas.save();
        
        canvas.translate(getPaddingLeft(), getPaddingTop());
        canvas.scale(scale, scale);
        canvas.translate(internalPaddingLeft, internalPaddingTop);
        
        canvas.drawBitmap(genBackgroundBitmap, 0, 0, bitmapPaint);
        
        if (testStatus != null)
            switch (testStatus)
            {
            case DOWN:
            case INIT_UP:
                canvas.drawBitmap(speedStatusDownBitmap, speedStatusDownX, speedStatusDownY, bitmapPaint);
                break;
            
            case UP:
            case SPEEDTEST_END:
                canvas.drawBitmap(speedStatusUpBitmap, speedStatusUpX, speedStatusUpY, bitmapPaint);
                break;
            }
        
        final Bitmap signalRingBitmap;
        switch (signalType)
        {
        case InformationCollector.SINGAL_TYPE_MOBILE:
            signalRingBitmap = signalRingBitmapMobile;
            break;
            
        case InformationCollector.SINGAL_TYPE_WLAN:
            signalRingBitmap = signalRingBitmapWlan;
            break;
            
        case InformationCollector.SINGAL_TYPE_RSRP:
            signalRingBitmap = signalRingBitmapRsrp;
            break;
            
        default:
            signalRingBitmap = null;
        }
        
        if (signalRingBitmap != null)
        {
            canvas.drawBitmap(signalRingBitmap, signalRingX, signalRingY, bitmapPaint);
            canvas.drawBitmap(signalStatusBgBitmap, signalStatusBgX, signalStatusBgY, bitmapPaint);
            canvas.drawText(signalDescriptionString, signalDescriptionX, signalDescriptionY, signalDescriptionPaint);
            if (signalString != null)
                canvas.drawText(signalString, signalX, signalY, signalPaint);
            signalGauge.draw(canvas);
        }
        
        if (headerString != null)
            canvas.drawText(headerString, headerX, headerY, headerPaint);
        if (subHeaderString != null) {
            headerPaint.setTextAlign(Align.RIGHT);
            canvas.drawText(subHeaderString, subHeaderX, subHeaderY, headerPaint);
            headerPaint.setTextAlign(Align.LEFT);
        }
        if (resultPingString != null)
            canvas.drawText(resultPingString, resultPingX, resultPingY, resultPaint);
        if (resultDownString != null)
            canvas.drawText(resultDownString, resultDownX, resultDownY, resultPaint);
        if (resultUpString != null)
            canvas.drawText(resultUpString, resultUpX, resultUpY, resultPaint);
        
        if (progressString != null)
            canvas.drawText(progressString, progressX, progressY, progressPaint);
        
        speedGauge.draw(canvas);
        progressGauge.draw(canvas);
        
        //canvas.drawBitmap(reflectionBitmap, reflectionX, reflectionY, bitmapPaint);
        
        canvas.restoreToCount(saveCount);
    }
    
    public void recycle()
    {
        recycled = true;
        genBackgroundBitmap.recycle();
        //reflectionBitmap.recycle();
        //resultBackgroundBitmap.recycle();
        speedStatusDownBitmap.recycle();
        speedStatusUpBitmap.recycle();
        signalRingBitmapMobile.recycle();
        signalRingBitmapWlan.recycle();
        signalRingBitmapRsrp.recycle();
        signalStatusBgBitmap.recycle();
        
        speedGauge.recycle();
        progressGauge.recycle();
        signalGauge.recycle();
    }

	public String getHeaderString() {
		return headerString;
	}

	public String getSubHeaderString() {
		return subHeaderString;
	}

	public String getResultPingString() {
		return resultPingString;
	}

	public String getResultDownString() {
		return resultDownString;
	}

	public String getResultUpString() {
		return resultUpString;
	}
}
