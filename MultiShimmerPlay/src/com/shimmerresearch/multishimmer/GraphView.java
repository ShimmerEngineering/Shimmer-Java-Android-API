package com.shimmerresearch.multishimmer;
/*
  Copyright (c) 2009 Bonifaz Kaufmann. 
  
  This library is free software; you can redistribute it and/or
  modify it under the terms of the GNU Lesser General Public
  License as published by the Free Software Foundation; either
  version 2.1 of the License, or (at your option) any later version.

  This library is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public
  License along with this library; if not, write to the Free Software
  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
*/


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import com.shimmerresearch.multishimmerplay.R;


public class GraphView extends View {

	private Bitmap  mBitmap;
	private Paint   mPaint = new Paint();
	private Paint   mPaintText = new Paint();
    private Canvas  mCanvas = new Canvas();
    private float   mSpeed = 1.0f;
	private float   mLastX;
    private float   mScale;
    private float[] mLastValue = new float[3];
    private float   mYOffset;
    private int[]   mColor = new int[3];
    private float   mWidth;
    private float   maxValue = 1024f;
    public float mTempV;
    public GraphView(Context context) {
        super(context);
        init();
    }
    
    public GraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    private void init(){
    	//mColor = Color.argb(192, 64, 128, 64);
    	mColor[0] = Color.argb(255, 100, 255, 100); // g
    	mColor[1] = Color.argb(255, 255, 255, 100); // y
    	mColor[2] = Color.argb(255, 255, 100, 100); // r
    	//mColor[x] = Color.argb(255, 100, 255, 255); // c
        mPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
    }
       
    public void setData(float value){
    	addDataPoint(value, mColor[0], mLastValue[0], 0);
    	invalidate();
    }

    public void setData(int[] values,String deviceID){
    	final int length = values.length;
        final Paint paintText = mPaintText;
        paintText.setColor(Color.argb(255, 255, 255, 255));
        mCanvas.drawText(deviceID, 5, 10, mPaintText);
    	try {
	    	for (int i=0;i<length;i++){
	    		addDataPoint(values[i], mColor[i%3], mLastValue[i], i);
	    	}
    	} catch (ArrayIndexOutOfBoundsException e){ 
    		/* mLastValue might run into this in extreme situations */
    		// but then we just do not want to support more than 3 values in our graph
    	}
    	invalidate();
    }
    
    public void setDataWithAdjustment(int[] values,String deviceID,String dataType){
    	final int length = values.length;
        final Paint paintText = mPaintText;
        int offset=0;
        if (dataType=="u8"){setMaxValue(255);offset=0;}
        else if (dataType=="i8"){setMaxValue(255);offset=127;} //center the graph, so the negative values will be displayed
        else if (dataType=="u12"){setMaxValue(4095);offset=0;}
        else if (dataType=="u16"){setMaxValue(65535);offset=0;}
        else if (dataType=="i16"){setMaxValue(4095);offset=2047;}        // it is actually a signed 12bit value for magnetometer
        	paintText.setColor(Color.argb(255, 255, 255, 255));
        mCanvas.drawText(deviceID, 5, 10, mPaintText);
    	try {
	    	for (int i=0;i<length;i++){
	    		addDataPoint(values[i]+offset, mColor[i%3], mLastValue[i], i);
	    	}
    	} catch (ArrayIndexOutOfBoundsException e){ 
    		/* mLastValue might run into this in extreme situations */
    		// but then we just do not want to support more than 3 values in our graph
    	}
    	invalidate();
    }
    
    private void addDataPoint(float value, final int color, final float lastValue, final int pos){
        final Paint paint = mPaint;

        float newX = mLastX + mSpeed;
        final float v = mYOffset + value * mScale;
        
        paint.setColor(color);


        mCanvas.drawLine(mLastX, lastValue, newX, v, mPaint);
        mTempV=v;
        mLastValue[pos] = v;
        if (pos == 0)
        	mLastX += mSpeed;
    }
    
    public void setMaxValue(int max){
    	maxValue = max;
    	mScale = - (mYOffset * (1.0f / maxValue));
    }
    public void setmYoffset(int m){
    	mYOffset=m;
    }
    public void setSpeed(float speed){
    	mSpeed = speed;
    }
      
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565);
        mCanvas.setBitmap(mBitmap);
        mCanvas.drawColor(0xFF000000);
        mYOffset = h;
        mScale = - (mYOffset * (1.0f / maxValue));
        mWidth = w;
        mLastX = mWidth;
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        synchronized (this) {
            if (mBitmap != null) {
                if (mLastX >= mWidth) {
                    mLastX = 0;
                    final Canvas cavas = mCanvas;
                    cavas.drawColor(0xFF000000);
                    //mPaint.setColor(0xFF777777);
                    mPaint.setColor(0xFF444444);
                    cavas.drawLine(0, mYOffset, mWidth, mYOffset, mPaint);
                   
                }
                canvas.drawBitmap(mBitmap, 0, 0, null);
            }
        } 
    }
}

