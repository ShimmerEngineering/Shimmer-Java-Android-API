//v0.2 -  8 January 2013

/*
 * Copyright (c) 2010, Shimmer Research, Ltd.
 * All rights reserved
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:

 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above
 *       copyright notice, this list of conditions and the following
 *       disclaimer in the documentation and/or other materials provided
 *       with the distribution.
 *     * Neither the name of Shimmer Research, Ltd. nor the names of its
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.

 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * @author Jong Chern Lim
 * @date   October, 2013
 */

package com.shimmerresearch.shimmergraphandlogservice;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import pl.flex_it.androidplot.XYSeriesShimmer;
import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidplot.Plot;
import com.androidplot.ui.SizeLayoutType;
import com.androidplot.ui.SizeMetrics;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYStepMode;
import com.google.common.collect.BiMap;
import com.shimmerresearch.android.Shimmer;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.Configuration.Shimmer2;
import com.shimmerresearch.driver.FormatCluster;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.ShimmerVerDetails;
import com.shimmerresearch.driver.Configuration.Shimmer3;
import com.shimmerresearch.service.ShimmerService;
import com.shimmerresearch.service.ShimmerService.LocalBinder;
import com.shimmerresearch.tools.Logging;


public class ShimmerGraphandLogService extends ServiceActivity {

	private static Context context;
	static final int REQUEST_ENABLE_BT = 1;
	static final int REQUEST_CONNECT_SHIMMER = 2;
	static final int REQUEST_CONFIGURE_SHIMMER = 3;
	static final int REQUEST_CONFIGURE_VIEW_SENSOR = 4;
	static final int REQUEST_COMMAND_SHIMMER = 5;
	static final int REQUEST_LOGFILE_SHIMMER = 6;
	private static XYPlot dynamicPlot;
	static List<Number> dataList = new ArrayList<Number>();
	static List<Number> dataTimeList = new ArrayList<Number>();
	static LineAndPointFormatter lineAndPointFormatter;
	final static int X_AXIS_LENGTH = 500;
	public static HashMap<String, List<Number>> mPlotDataMap = new HashMap<String, List<Number>>(3);
	public static HashMap<String, XYSeriesShimmer> mPlotSeriesMap = new HashMap<String, XYSeriesShimmer>(3);
	static LineAndPointFormatter lineAndPointFormatter1, lineAndPointFormatter2, lineAndPointFormatter3;
	private Paint LPFpaint, transparentPaint, outlinePaint;
		
	//private DataMethods DM;
	private static TextView mTitle;
	private static TextView mTitleLogging;
	private static TextView mValueSensor1;
	private static TextView mValueSensor2;
	private static TextView mValueSensor3;
	
	private static TextView mTextSensor1;
	private static TextView mTextSensor2;
	private static TextView mTextSensor3;
	// Local Bluetooth adapter
	private BluetoothAdapter mBluetoothAdapter = null;
	// Name of the connected device
    private static String mBluetoothAddress = null;
    // Member object for communication services
    private String mSignaltoGraph;
    private static String mSensorView = ""; //The sensor device which should be viewed on the graph
    private static int mGraphSubSamplingCount = 0; //10 
    private static String mFileName = "myFirstDataSet";
    static Logging log = new Logging(mFileName,"\t"); //insert file name
    private static boolean mEnableLogging = false;
    Dialog mDialog;
    long dialogEnabledSensors=0;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	Configuration.setTooLegacyObjectClusterSensorNames();
        Log.d("ShimmerActivity","On Create");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);

        setContentView(R.layout.main);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);
        getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);
        Log.d("ShimmerActivity","On Create");
        mTitle = (TextView) findViewById(R.id.title_right_text);
        // Set up the custom title
        mTitleLogging = (TextView) findViewById(R.id.title_left_text);
            
        mDialog = new Dialog(this);
        
        
       /**  --GRAPH SET UP--  **/
        
        dynamicPlot   = (XYPlot)   findViewById(R.id.dynamicPlot);
        
        dynamicPlot.getGraphWidget().setDomainValueFormat(new DecimalFormat("0"));
        lineAndPointFormatter1 = new LineAndPointFormatter(Color.rgb(51, 153, 255), null, null); // line color, point color, fill color
        LPFpaint = lineAndPointFormatter1.getLinePaint();
        LPFpaint.setStrokeWidth(3);
        lineAndPointFormatter1.setLinePaint(LPFpaint);
        lineAndPointFormatter2 = new LineAndPointFormatter(Color.rgb(245, 146, 107), null, null);
        LPFpaint = lineAndPointFormatter2.getLinePaint();
        LPFpaint.setStrokeWidth(3);
        lineAndPointFormatter2.setLinePaint(LPFpaint);
        lineAndPointFormatter3 = new LineAndPointFormatter(Color.rgb(150, 150, 150), null, null);
        LPFpaint = lineAndPointFormatter3.getLinePaint();
        LPFpaint.setStrokeWidth(3);
        lineAndPointFormatter3.setLinePaint(LPFpaint);
        transparentPaint = new Paint();
        transparentPaint.setColor(Color.TRANSPARENT);
        //lineAndPointFormatter1.setLinePaint(p);
        dynamicPlot.setDomainStepMode(XYStepMode.SUBDIVIDE);
        //dynamicPlot.setDomainStepValue(series1.size());
        dynamicPlot.getLegendWidget().setSize(new SizeMetrics(0, SizeLayoutType.ABSOLUTE, 0, SizeLayoutType.ABSOLUTE));
        // thin out domain/range tick labels so they dont overlap each other:
        dynamicPlot.setTicksPerDomainLabel(5);
        dynamicPlot.setTicksPerRangeLabel(3);
        dynamicPlot.disableAllMarkup();
        Paint gridLinePaint = new Paint();
        gridLinePaint.setColor(Color.parseColor("#D6D6D6"));
        dynamicPlot.setRangeBoundaries(-15, 15, BoundaryMode.AUTO); // freeze the range boundary:
        dynamicPlot.setDomainBoundaries(0, X_AXIS_LENGTH, BoundaryMode.FIXED); // freeze the domain boundary:
        dynamicPlot.getGraphWidget().setMargins(0, 20, 10, 10);
        dynamicPlot.setBorderStyle(Plot.BorderStyle.NONE, null, null);
        dynamicPlot.getBackgroundPaint().setColor(Color.TRANSPARENT);
        dynamicPlot.setBackgroundColor(Color.TRANSPARENT);
        dynamicPlot.getGraphWidget().getGridBackgroundPaint().setColor(Color.TRANSPARENT);
        dynamicPlot.getGraphWidget().getBackgroundPaint().setColor(Color.TRANSPARENT);
        dynamicPlot.getGraphWidget().setGridLinePaint(transparentPaint);
        dynamicPlot.getGraphWidget().setDomainOriginLabelPaint(gridLinePaint);
        dynamicPlot.getGraphWidget().setDomainOriginLinePaint(gridLinePaint);
        dynamicPlot.getGraphWidget().setDomainLabelPaint(gridLinePaint);
        dynamicPlot.getGraphWidget().getDomainLabelPaint().setTextSize(20);
        dynamicPlot.getDomainLabelWidget().pack();
        outlinePaint = dynamicPlot.getGraphWidget().getDomainOriginLinePaint();
        outlinePaint.setStrokeWidth(3);
        dynamicPlot.getGraphWidget().setClippingEnabled(false);
        dynamicPlot.getGraphWidget().setRangeOriginLabelPaint(gridLinePaint);
        dynamicPlot.getGraphWidget().setRangeOriginLinePaint(gridLinePaint);
        dynamicPlot.getGraphWidget().setRangeLabelPaint(gridLinePaint);
        dynamicPlot.getGraphWidget().getRangeLabelPaint().setTextSize(20);
        dynamicPlot.getRangeLabelWidget().pack();
        outlinePaint = dynamicPlot.getGraphWidget().getRangeOriginLinePaint();
        outlinePaint.setStrokeWidth(3);
        dynamicPlot.getLayoutManager().remove(dynamicPlot.getRangeLabelWidget());
        dynamicPlot.getLayoutManager().remove(dynamicPlot.getDomainLabelWidget());
        
        
        mValueSensor1 = (TextView) findViewById(R.id.sensorvalue1);
        mValueSensor2 = (TextView) findViewById(R.id.sensorvalue2);
        mValueSensor3 = (TextView) findViewById(R.id.sensorvalue3);
        mTextSensor1 =  (TextView) findViewById(R.id.LabelSensor1);
        mTextSensor2 =  (TextView) findViewById(R.id.LabelSensor2);
        mTextSensor3 =  (TextView) findViewById(R.id.LabelSensor3);
        DisplayMetrics metrics = this.getResources().getDisplayMetrics();
		 switch(metrics.densityDpi){
	     case DisplayMetrics.DENSITY_LOW:
	    	 mTitleLogging.setTextSize(6);
	    	 mTitle.setTextSize(10);
	    	 mValueSensor1.setTextSize(10);
	    	 mValueSensor2.setTextSize(10);
	    	 mValueSensor3.setTextSize(10);
	    	 mTextSensor1.setTextSize(10);
	    	 mTextSensor2.setTextSize(10);
	    	 mTextSensor3.setTextSize(10);
	                break;
	     case DisplayMetrics.DENSITY_MEDIUM:
	    	 mTitleLogging.setTextSize(6);
	    	 mTitle.setTextSize(10);
	    	 mValueSensor1.setTextSize(14);
	    	 mValueSensor2.setTextSize(14);
	    	 mValueSensor3.setTextSize(14);
	    	 mTextSensor1.setTextSize(14);
	    	 mTextSensor2.setTextSize(14);
	    	 mTextSensor3.setTextSize(14);
	                 break;
	     case DisplayMetrics.DENSITY_HIGH:
	    	 mTitleLogging.setTextSize(12);
	    	 mTitle.setTextSize(16);
	    	 mValueSensor1.setTextSize(16);
	    	 mValueSensor2.setTextSize(16);
	    	 mValueSensor3.setTextSize(16);
	    	 mTextSensor1.setTextSize(16);
	    	 mTextSensor2.setTextSize(16);
	    	 mTextSensor3.setTextSize(16);
	                 break;
	     case DisplayMetrics.DENSITY_XHIGH:
	    	 mTitle.setTextSize(14);
	    	 mTitleLogging.setTextSize(18);
	    	 mValueSensor1.setTextSize(18);
	    	 mValueSensor2.setTextSize(18);
	    	 mValueSensor3.setTextSize(18);
	    	 mTextSensor1.setTextSize(18);
	    	 mTextSensor2.setTextSize(18);
	    	 mTextSensor3.setTextSize(18);
	    	 break;
		 }
		 
		 if (!isMyServiceRunning())
	      {
	      	Log.d("ShimmerH","Oncreate2");
	      	Intent intent=new Intent(this, ShimmerService.class);
	      	startService(intent);
	      	if (mServiceFirstTime==true){
	      		Log.d("ShimmerH","Oncreate3");
	  			getApplicationContext().bindService(intent,mTestServiceConnection, Context.BIND_AUTO_CREATE);
	  			mServiceFirstTime=false;
	  		}
	      	mTitle.setText(R.string.title_not_connected); // if no service is running means no devices are connected
	      }         
	      
	      if (mBluetoothAddress!=null){
	      	mTitle.setText(R.string.title_connected_to);
	          mTitle.append(mBluetoothAddress);    
	      }
		 
		 
		 if (mEnableLogging==false){
		      	mTitleLogging.setText("Logging Disabled");
		      } else if (mEnableLogging==true){
		      	mTitleLogging.setText("Logging Enabled");
	      }
		      
		 
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter == null) {
        	Toast.makeText(this, "Device does not support Bluetooth\nExiting...", Toast.LENGTH_LONG).show();
        	finish();
        }
        
        ShimmerGraphandLogService.context = getApplicationContext();
 
    }
    
    @Override
    public void onStart() {
    	super.onStart();

    	if(!mBluetoothAdapter.isEnabled()) {     	
        	Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        	startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    	}
    	else {
    		
    		
    		
    	}
    }
    
    
    
    @Override
	public void onPause() {
		super.onPause();
		Log.d("ShimmerActivity","On Pause");
		//finish();
		 mPlotSeriesMap.clear();
    	 mPlotDataMap.clear();
    	 dynamicPlot.clear();
		if(mServiceBind == true){
  		  //getApplicationContext().unbindService(mTestServiceConnection); 
  	  }
	}

	public void onResume() {
		super.onResume();
		ShimmerGraphandLogService.context = getApplicationContext();
		Log.d("ShimmerActivity","On Resume");
		Intent intent=new Intent(this, ShimmerService.class);
  	  	Log.d("ShimmerH","on Resume");
  	  	getApplicationContext().bindService(intent,mTestServiceConnection, Context.BIND_AUTO_CREATE);
  	  	mPlotSeriesMap.clear();
  	  	mPlotDataMap.clear();
  	  	dynamicPlot.clear();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}
	
	@Override
	protected void onDestroy() {		
		super.onDestroy();
		Log.d("ShimmerActivity","On Destroy");
	}
	
	
	 protected ServiceConnection mTestServiceConnection = new ServiceConnection() {

	      	public void onServiceConnected(ComponentName arg0, IBinder service) {
	      		// TODO Auto-generated method stub
	      		Log.d("ShimmerService", "service connected from main activity");
	      		LocalBinder binder = (LocalBinder) service;
	      		mService = binder.getService();
	      		mServiceBind = true;
	      		mService.setGraphHandler(mHandler);
	      		
	      	}
	      	public void onServiceDisconnected(ComponentName arg0) {
	      		// TODO Auto-generated method stub
	      		mServiceBind = false;
	      	}
	    };
	
	
	// The Handler that gets information back from the BluetoothChatService
    private static Handler mHandler = new Handler() {
   

		public void handleMessage(Message msg) {
			switch (msg.what) {
            
            case Shimmer.MESSAGE_STATE_CHANGE:
                switch (msg.arg1) {
                case Shimmer.STATE_CONNECTED:
                	//this has been deprecated
                    break;
                case Shimmer.MSG_STATE_FULLY_INITIALIZED:
                	Log.d("ShimmerActivity","Message Fully Initialized Received from Shimmer driver");
                    mTitle.setText(R.string.title_connected_to);
                    mBluetoothAddress=((ObjectCluster)msg.obj).mBluetoothAddress;
                    mTitle.append(mBluetoothAddress);    
                    mService.enableGraphingHandler(true);
                    break;
                case Shimmer.STATE_CONNECTING:
                	Log.d("ShimmerActivity","Driver is attempting to establish connection with Shimmer device");
                    mTitle.setText(R.string.title_connecting);
                    break;
                case Shimmer.STATE_NONE:
                	Log.d("ShimmerActivity","Shimmer No State");
                    mTitle.setText(R.string.title_not_connected);;
                    mBluetoothAddress=null;
                    // this also stops streaming
                    break;
                }
                break;
            case Shimmer.MESSAGE_READ:

            	    if ((msg.obj instanceof ObjectCluster)){
            	    ObjectCluster objectCluster =  (ObjectCluster) msg.obj; 
            	   
            	    
            	
            		double[] calibratedDataArray = new double[0];
            		String[] sensorName = new String[0];
            		String calibratedUnits="";
            		String calibratedUnits2="";
            		//mSensorView determines which sensor to graph
            		if (mSensorView.equals("Accelerometer")){
            			sensorName = new String[3]; // for x y and z axis
            			calibratedDataArray = new double[3];
            			sensorName[0] = Configuration.Shimmer2.ObjectClusterSensorName.ACCEL_X;
            			sensorName[1] = Configuration.Shimmer2.ObjectClusterSensorName.ACCEL_Y;
            			sensorName[2] = Configuration.Shimmer2.ObjectClusterSensorName.ACCEL_Z;
            		}
            		if (mSensorView.equals("Low Noise Accelerometer")){
            			sensorName = new String[3]; // for x y and z axis
            			calibratedDataArray = new double[3];
            			sensorName[0] = Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_LN_X;
            			sensorName[1] = Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_LN_Y;
            			sensorName[2] = Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_LN_Z;
            			
            		}
            		if (mSensorView.equals("Wide Range Accelerometer")){
            			sensorName = new String[3]; // for x y and z axis
            			calibratedDataArray = new double[3];
            			sensorName[0] = "Wide Range Accelerometer X";
            			sensorName[1] = "Wide Range Accelerometer Y";
            			sensorName[2] = "Wide Range Accelerometer Z";
            		}
            		if (mSensorView.equals("Gyroscope")){
            			sensorName = new String[3]; // for x y and z axis
            			calibratedDataArray = new double[3];
            			sensorName[0] = "Gyroscope X";
            			sensorName[1] = "Gyroscope Y";
            			sensorName[2] = "Gyroscope Z";
            		}
            		if (mSensorView.equals("Magnetometer")){
            			sensorName = new String[3]; // for x y and z axis
            			calibratedDataArray = new double[3];
            			sensorName[0] = "Magnetometer X";
            			sensorName[1] = "Magnetometer Y";
            			sensorName[2] = "Magnetometer Z";
            		}
            		if (mSensorView.equals("GSR")){
            			sensorName = new String[1]; 
            			calibratedDataArray = new double[1];
            			sensorName[0] = "GSR";
            		}
            		if (mSensorView.equals("EMG")){
            			sensorName = new String[1]; 
            			calibratedDataArray = new double[1];
            			sensorName[0] = "EMG";
            		}
            		if (mSensorView.equals("ECG")){
            			
            			calibratedDataArray = new double[2];
            			Shimmer shmr = mService.getShimmer(mBluetoothAddress);
            			if(shmr!=null){
	            			if( mService.getShimmer(mBluetoothAddress).getShimmerVersion() == ShimmerVerDetails.HW_ID.SHIMMER_2 ||
	            					 mService.getShimmer(mBluetoothAddress).getShimmerVersion() == ShimmerVerDetails.HW_ID.SHIMMER_2R)
	            			{
	            				sensorName = new String[2]; 
	            				sensorName[0] = "ECG RA-LL";
	            				sensorName[1] = "ECG LA-LL";
	            			}
            			}
            		}
            		if (mSensorView.equals("EXG1") || mSensorView.equals("EXG2") || mSensorView.equals("EXG1 16Bit") || mSensorView.equals("EXG2 16Bit")){
            			Shimmer shmr = mService.getShimmer(mBluetoothAddress);
            			if(shmr!=null){
            				if( mService.getShimmer(mBluetoothAddress).getShimmerVersion() == ShimmerVerDetails.HW_ID.SHIMMER_3)
	            			{
	            				if (mService.getShimmer(mBluetoothAddress).isEXGUsingECG24Configuration() ||
	            						mService.getShimmer(mBluetoothAddress).isEXGUsingECG16Configuration()){
	            					sensorName = new String[3]; 
	            					calibratedDataArray = new double[3];
	            					//same name for both 16 and 24 bit
	            					sensorName[0] = Shimmer3.ObjectClusterSensorName.ECG_LL_RA_24BIT;
	            					sensorName[1] = Shimmer3.ObjectClusterSensorName.ECG_LA_RA_24BIT;
	            					sensorName[2] = Shimmer3.ObjectClusterSensorName.ECG_VX_RL_24BIT;
	            				} else if (mService.getShimmer(mBluetoothAddress).isEXGUsingEMG24Configuration() ||
	            						mService.getShimmer(mBluetoothAddress).isEXGUsingEMG16Configuration()){
	                				sensorName = new String[2]; 
	                				calibratedDataArray = new double[2];
	                				//same name for both 16 and 24 bit
	                    			sensorName[0] = Shimmer3.ObjectClusterSensorName.EMG_CH1_24BIT;
	                    			sensorName[1] = Shimmer3.ObjectClusterSensorName.EMG_CH2_24BIT;
	                			} else if (mService.getShimmer(mBluetoothAddress).isEXGUsingTestSignal24Configuration()){
	                				sensorName = new String[3]; 
	                				calibratedDataArray = new double[3];
	                    			sensorName[0] = Shimmer3.ObjectClusterSensorName.EXG1_CH1_24BIT;
	                    			sensorName[1] = Shimmer3.ObjectClusterSensorName.EXG1_CH2_24BIT;
	                    			sensorName[2] = Shimmer3.ObjectClusterSensorName.EXG2_CH1_24BIT;
	                			} else if (mService.getShimmer(mBluetoothAddress).isEXGUsingTestSignal16Configuration()){
	                				sensorName = new String[3]; 
	                				calibratedDataArray = new double[3];
	                    			sensorName[0] = Shimmer3.ObjectClusterSensorName.EXG1_CH1_16BIT;
	                    			sensorName[1] = Shimmer3.ObjectClusterSensorName.EXG1_CH2_16BIT;
	                    			sensorName[2] = Shimmer3.ObjectClusterSensorName.EXG2_CH1_16BIT;
	                			} else {
	                				sensorName = new String[3]; 
	                				calibratedDataArray = new double[3];
	                				if (mSensorView.equals("EXG1 16Bit") || mSensorView.equals("EXG2 16Bit")){
	                					sensorName[0] = Shimmer3.ObjectClusterSensorName.EXG1_CH1_16BIT;
	                        			sensorName[1] = Shimmer3.ObjectClusterSensorName.EXG1_CH2_16BIT;
	                        			sensorName[2] = Shimmer3.ObjectClusterSensorName.EXG2_CH1_16BIT;
	                				} else {
	                					sensorName[0] = Shimmer3.ObjectClusterSensorName.EXG1_CH1_24BIT;
	                					sensorName[1] = Shimmer3.ObjectClusterSensorName.EXG1_CH2_24BIT;
	                					sensorName[2] = Shimmer3.ObjectClusterSensorName.EXG2_CH1_24BIT;
	                				}
	                			}
	            			}
            			}
            		}
            		if (mSensorView.equals("Bridge Amplifier")){
            			sensorName = new String[2]; 
            			calibratedDataArray = new double[2];
            			sensorName[0] = "Bridge Amplifier High";
            			sensorName[1] = "Bridge Amplifier Low";
            		}
            		if (mSensorView.equals("Heart Rate")){
            			sensorName = new String[1]; 
            			calibratedDataArray = new double[1];
            			sensorName[0] = "Heart Rate";
            		}
            		if (mSensorView.equals("ExpBoard A0")){
            			sensorName = new String[1]; 
            			calibratedDataArray = new double[1];
            			sensorName[0] = Shimmer2.ObjectClusterSensorName.EXP_BOARD_A0;
            			
            		}
            		if (mSensorView.equals("ExpBoard A7")){
            			sensorName = new String[1]; 
            			calibratedDataArray = new double[1];
            			sensorName[0] = Shimmer2.ObjectClusterSensorName.EXP_BOARD_A7;
            		}
            		if (mSensorView.equals("Battery Voltage")){
            			sensorName = new String[2]; 
            			calibratedDataArray = new double[2];
            			sensorName[0] = "VSenseReg";
            			sensorName[1] = "VSenseBatt";
            		}
            		if (mSensorView.equals("Timestamp")){
            			sensorName = new String[1]; 
            			calibratedDataArray = new double[1];
            			sensorName[0] = "Timestamp";
            		}
            		if (mSensorView.equals("External ADC A7")){
            			sensorName = new String[1]; 
            			calibratedDataArray = new double[1];
            			Shimmer shmr = mService.getShimmer(mBluetoothAddress);
            			if(shmr!=null){
	            			if( mService.getShimmer(mBluetoothAddress).getShimmerVersion() == ShimmerVerDetails.HW_ID.SHIMMER_3){
	            				sensorName[0] = Shimmer3.ObjectClusterSensorName.EXT_EXP_A7;
		            		} else {
		            			sensorName[0] = Shimmer2.ObjectClusterSensorName.EXT_EXP_A7;
	            		}
            			}
            	    }
            		if (mSensorView.equals("External ADC A6")){
            			sensorName = new String[1]; 
            			calibratedDataArray = new double[1];
            			Shimmer shmr = mService.getShimmer(mBluetoothAddress);
            			if(shmr!=null){
	            			if( mService.getShimmer(mBluetoothAddress).getShimmerVersion() == ShimmerVerDetails.HW_ID.SHIMMER_3){
	            				sensorName[0] = Shimmer3.ObjectClusterSensorName.EXT_EXP_A6;
		            		} else {
		            			sensorName[0] = Shimmer2.ObjectClusterSensorName.EXT_EXP_A6;
		            		}
            			}
            		}
            		if (mSensorView.equals("External ADC A15")){
            			sensorName = new String[1]; 
            			calibratedDataArray = new double[1];
            			sensorName[0] = "External ADC A15";
            		}
            		if (mSensorView.equals("Internal ADC A1")){
            			sensorName = new String[1]; 
            			calibratedDataArray = new double[1];
            			sensorName[0] = "Internal ADC A1";
            		}
            		if (mSensorView.equals("Internal ADC A12")){
            			sensorName = new String[1]; 
            			calibratedDataArray = new double[1];
            			sensorName[0] = "Internal ADC A12";
            		}
            		if (mSensorView.equals("Internal ADC A13")){
            			sensorName = new String[1]; 
            			calibratedDataArray = new double[1];
            			sensorName[0] = "Internal ADC A13";
            		}
            		if (mSensorView.equals("Internal ADC A14")){
            			sensorName = new String[1]; 
            			calibratedDataArray = new double[1];
            			sensorName[0] = "Internal ADC A14";
            		}
            		if (mSensorView.equals("Pressure")){
            			sensorName = new String[2]; 
            			calibratedDataArray = new double[2];
            			sensorName[0] = "Pressure";
            			sensorName[1] = "Temperature";
            		}
         	    
            		
            		if (sensorName.length!=0){  // Device 1 is the assigned user id, see constructor of the Shimmer
				 	    if (sensorName.length>0){
//				 	    	
				 	    	Collection<FormatCluster> ofFormats = objectCluster.mPropertyCluster.get(sensorName[0]);  // first retrieve all the possible formats for the current sensor device
				 	    	FormatCluster formatCluster = ((FormatCluster)ObjectCluster.returnFormatCluster(ofFormats,"CAL")); 
				 	    	if (formatCluster != null) {
//				 	    		//Obtain data for text view
				 	    		calibratedDataArray[0] = formatCluster.mData;
				 	    		calibratedUnits = formatCluster.mUnits;

						 	 	//first check if there is data       		 	    		
        		 	    		List<Number> data;
    		 	    			if (mPlotDataMap.get("serie 1")!=null){
    		 	    				data = mPlotDataMap.get("serie 1");
    		 	    			} else {
    		 	    				data = new ArrayList<Number>();
    		 	    			}
    		 	    			if (data.size()>X_AXIS_LENGTH){
    		 	    				data.clear();
    		 	    			}
    		 	    			data.add(formatCluster.mData);
    		 	    			mPlotDataMap.put("serie 1", data);
    		 	    			
    		 	    			//next check if the series exist 
    		 	    			LineAndPointFormatter lapf = new LineAndPointFormatter(Color.rgb(0, 255, 0), null, null);
    		 	    			if (mPlotSeriesMap.get("serie 1")!=null){
    		 	    				//if the series exist get the line format
    		 	    				mPlotSeriesMap.get("serie 1").updateData(data);
        		 	    		} else {
        		 	    			XYSeriesShimmer series = new XYSeriesShimmer(data, 0, "serie 1");
            		 	    		mPlotSeriesMap.put("serie 1", series);
        		 	    			dynamicPlot.addSeries(mPlotSeriesMap.get("serie 1"), lapf);
        		 	    			
            		 	    	}
				 	    	}
//					 	    	
					 	  }
				 	   	}
				 	    if (sensorName.length>1) {
				 	    	Collection<FormatCluster> ofFormats = objectCluster.mPropertyCluster.get(sensorName[1]);  // first retrieve all the possible formats for the current sensor device
				 	    	FormatCluster formatCluster = ((FormatCluster)ObjectCluster.returnFormatCluster(ofFormats,"CAL"));
				 	    	if (formatCluster != null ) {
					 	    	calibratedDataArray[1] = formatCluster.mData;
					 	    	//Obtain data for text view
					 	    	calibratedUnits2 = formatCluster.mUnits;
					 	    	
					 	    	List<Number> data;
    		 	    			if (mPlotDataMap.get("serie 2")!=null){
    		 	    				data = mPlotDataMap.get("serie 2");
    		 	    			} else {
    		 	    				data = new ArrayList<Number>();
    		 	    			}
    		 	    			if (data.size()>X_AXIS_LENGTH){
    		 	    				data.clear();
    		 	    			}
    		 	    			data.add(formatCluster.mData);
    		 	    			mPlotDataMap.put("serie 2", data);
    		 	    			
    		 	    			//next check if the series exist 
    		 	    			LineAndPointFormatter lapf = new LineAndPointFormatter(Color.rgb(255, 255, 0), null, null);
    		 	    			if (mPlotSeriesMap.get("serie 2")!=null){
    		 	    				//if the series exist get the line format
    		 	    				mPlotSeriesMap.get("serie 2").updateData(data);
        		 	    		} else {
        		 	    			XYSeriesShimmer series = new XYSeriesShimmer(data, 0, "serie 2");
            		 	    		mPlotSeriesMap.put("serie 2", series);
        		 	    			dynamicPlot.addSeries(mPlotSeriesMap.get("serie 2"), lapf);
        		 	    			
            		 	    	}

				 	    	}
				 	    }
				 	    if (sensorName.length>2){
				 	    
				 	    	Collection<FormatCluster> ofFormats = objectCluster.mPropertyCluster.get(sensorName[2]);  // first retrieve all the possible formats for the current sensor device
				 	    	FormatCluster formatCluster = ((FormatCluster)ObjectCluster.returnFormatCluster(ofFormats,"CAL")); 
				 	    	if (formatCluster != null) {
				 	    		calibratedDataArray[2] = formatCluster.mData;
					 	    						 	   	  
					 	    	
				 	    		List<Number> data;
    		 	    			if (mPlotDataMap.get("serie 3")!=null){
    		 	    				data = mPlotDataMap.get("serie 3");
    		 	    			} else {
    		 	    				data = new ArrayList<Number>();
    		 	    			}
    		 	    			if (data.size()>X_AXIS_LENGTH){
    		 	    				data.clear();
    		 	    			}
    		 	    			data.add(formatCluster.mData);
    		 	    			mPlotDataMap.put("serie 3", data);
    		 	    			
    		 	    			//next check if the series exist 
    		 	    			LineAndPointFormatter lapf = new LineAndPointFormatter(Color.rgb(255, 0, 0), null, null);
    		 	    			if (mPlotSeriesMap.get("serie 3")!=null){
    		 	    				//if the series exist get the line format
    		 	    				mPlotSeriesMap.get("serie 3").updateData(data);
        		 	    		} else {        		 	    			
        		 	    			XYSeriesShimmer series = new XYSeriesShimmer(data, 0, "serie 3");
            		 	    		mPlotSeriesMap.put("serie 3", series);
        		 	    			dynamicPlot.addSeries(mPlotSeriesMap.get("serie 3"), lapf);
        		 	    			
            		 	    	}
				 	    	}
				 	    	
			            }
				 	   dynamicPlot.redraw();

				 	   
						if (calibratedDataArray.length>0) {
							mValueSensor1.setText(String.format("%.4f",calibratedDataArray[0]));
							mTextSensor1.setText(sensorName[0] + "("+calibratedUnits+")");
						}
						if (calibratedDataArray.length>1) {
							mValueSensor2.setText(String.format("%.4f",calibratedDataArray[1]));
							mTextSensor2.setText(sensorName[1] + "("+calibratedUnits2+")");
						}
						if (calibratedDataArray.length>2) {
							mValueSensor3.setText(String.format("%.4f",calibratedDataArray[2]));
							mTextSensor3.setText(sensorName[2] + "("+calibratedUnits+")");
						}         	    
            	    
            	}
				
                break;
            case Shimmer.MESSAGE_ACK_RECEIVED:
            	
            	break;
            case Shimmer.MESSAGE_DEVICE_NAME:
                // save the connected device's name
                
                Toast.makeText(getContext(), "Connected to "
                               + mBluetoothAddress, Toast.LENGTH_SHORT).show();
                break;
       
            	
            case Shimmer.MESSAGE_TOAST:
                Toast.makeText(getContext(), msg.getData().getString(Shimmer.TOAST),
                               Toast.LENGTH_SHORT).show();
                break;
           
            }
        }
    };
	
    private static Context getContext(){
    	return ShimmerGraphandLogService.context;
    }
    
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		
    	switch (requestCode) {
    	case REQUEST_ENABLE_BT:
            // When the request to enable Bluetooth returns
            if (resultCode == Activity.RESULT_OK) {
            	
                //setMessage("\nBluetooth is now enabled");
                Toast.makeText(this, "Bluetooth is now enabled", Toast.LENGTH_SHORT).show();
            } else {
                // User did not enable Bluetooth or an error occured
            	Toast.makeText(this, "Bluetooth not enabled\nExiting...", Toast.LENGTH_SHORT).show();
                finish();       
            }
            break;
    	case REQUEST_CONNECT_SHIMMER:
            // When DeviceListActivity returns with a device to connect
            if (resultCode == Activity.RESULT_OK) {
                String address = data.getExtras()
                        .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                Log.d("ShimmerActivity",address);
          		mService.connectShimmer(address, "Device");
          		mBluetoothAddress = address;
          		mService.setGraphHandler(mHandler);
                
                //mShimmerDevice.connect(address,"gerdavax");
                //mShimmerDevice.setgetdatainstruction("a");
            }
            break;
    	case REQUEST_COMMAND_SHIMMER:
    		
    		if (resultCode == Activity.RESULT_OK) {
	    		if(data.getExtras().getBoolean("ToggleLED",false) == true)
	    		{
	    			mService.toggleAllLEDS();
	    		}
	    		
	    		if(data.getExtras().getDouble("SamplingRate",-1) != -1)
	    		{
	    			mService.writeSamplingRate(mBluetoothAddress, data.getExtras().getDouble("SamplingRate",-1));
	    			Log.d("ShimmerActivity",Double.toString(data.getExtras().getDouble("SamplingRate",-1)));
	    			mGraphSubSamplingCount=0;
	    		}
	    		
	    		if(data.getExtras().getInt("AccelRange",-1) != -1)
	    		{
	    			mService.writeAccelRange(mBluetoothAddress, data.getExtras().getInt("AccelRange",-1));
	    		}
	    		
	    		if(data.getExtras().getInt("GyroRange",-1) != -1)
	    		{
	    			mService.writeGyroRange(mBluetoothAddress, data.getExtras().getInt("GyroRange",-1));
	    		}
	    		
	    		if(data.getExtras().getInt("PressureResolution",-1) != -1)
	    		{
	    			mService.writePressureResolution(mBluetoothAddress, data.getExtras().getInt("PressureResolution",-1));
	    		}
	    		
	    		if(data.getExtras().getInt("MagRange",-1) != -1)
	    		{
	    			mService.writeMagRange(mBluetoothAddress, data.getExtras().getInt("MagRange",-1));
	    		}
	    		
	    		if(data.getExtras().getInt("GSRRange",-1) != -1)
	    		{
	    			mService.writeGSRRange(mBluetoothAddress,data.getExtras().getInt("GSRRange",-1));
	    		}
	    		if(data.getExtras().getDouble("BatteryLimit",-1) != -1)
	    		{
	    			mService.setBattLimitWarning(mBluetoothAddress, data.getExtras().getDouble("BatteryLimit",-1));
	    		}
	    		
    		}
    		break;
    	case REQUEST_LOGFILE_SHIMMER:
    		if (resultCode == Activity.RESULT_OK) {
    			mEnableLogging = data.getExtras().getBoolean("LogFileEnableLogging");
    			if (mEnableLogging==true){
    				mService.setEnableLogging(mEnableLogging);
    			}
    			//set the filename in the LogFile
    			mFileName=data.getExtras().getString("LogFileName");
    			mService.setLoggingName(mFileName);
    			
    			if (mEnableLogging==false){
    	        	mTitleLogging.setText("Logging Disabled");
    	        } else if (mEnableLogging==true){
    	        	mTitleLogging.setText("Logging Enabled");
    	        }
    			
    		}
    		break;
        }
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.options_menu, menu);
		MenuItem streamItem = menu.findItem(R.id.stream);
		streamItem.setEnabled(false);
		MenuItem settingsItem = menu.findItem(R.id.settings);
		settingsItem.setEnabled(false);
		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
	
		//disable graph edit for sensors which are not enabled
		
		MenuItem scanItem = menu.findItem(R.id.scan);
		MenuItem streamItem = menu.findItem(R.id.stream);
		MenuItem settingsItem = menu.findItem(R.id.settings);
		MenuItem commandsItem = menu.findItem(R.id.commands);
		MenuItem viewItem = menu.findItem(R.id.viewsensor);
		if((mService.DevicesConnected(mBluetoothAddress) == true)){
			scanItem.setIcon(android.R.drawable.ic_menu_close_clear_cancel);
			scanItem.setTitle(R.string.disconnect);
			streamItem.setIcon(R.drawable.ic_menu_play_clip);
			streamItem.setTitle(R.string.startstream);
			streamItem.setEnabled(true);
			settingsItem.setEnabled(true);
			commandsItem.setEnabled(true);
			viewItem.setEnabled(true);
		}
		else {
			scanItem.setIcon(android.R.drawable.ic_menu_search);
			scanItem.setTitle(R.string.connect);
			streamItem.setIcon(R.drawable.ic_menu_play_clip);
			streamItem.setEnabled(false);
			settingsItem.setEnabled(false);
			commandsItem.setEnabled(false);
			viewItem.setEnabled(false);
		}
		if(mService.DeviceIsStreaming(mBluetoothAddress) == true && mService.DevicesConnected(mBluetoothAddress) == true){
			streamItem.setIcon(R.drawable.ic_menu_stop);
			streamItem.setTitle(R.string.stopstream);
			
		}
		if(mService.DeviceIsStreaming(mBluetoothAddress) == false && mService.DevicesConnected(mBluetoothAddress) == true && mService.GetInstructionStatus(mBluetoothAddress)==true){
			streamItem.setIcon(R.drawable.ic_menu_play_clip);
			streamItem.setTitle(R.string.startstream);
		}	
		if (mService.GetInstructionStatus(mBluetoothAddress)==false || (mService.GetInstructionStatus(mBluetoothAddress)==false)){ 
			streamItem.setEnabled(false);
			settingsItem.setEnabled(false);
			commandsItem.setEnabled(false);
		}
		if (mService.DeviceIsStreaming(mBluetoothAddress)){
			settingsItem.setEnabled(false);
			commandsItem.setEnabled(false);
		}
		if (mService.GetInstructionStatus(mBluetoothAddress)==false)
		{
			streamItem.setEnabled(false);
			settingsItem.setEnabled(false);
			commandsItem.setEnabled(false);
		}
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		if (itemId == R.id.scan) {
			if ((mService.DevicesConnected(mBluetoothAddress) == true)) {
				mService.disconnectAllDevices();
			} else {
				Intent serverIntent = new Intent(this, DeviceListActivity.class);
				startActivityForResult(serverIntent, REQUEST_CONNECT_SHIMMER);
			}
			return true;
		} else if (itemId == R.id.stream) {
			if (mService.DeviceIsStreaming(mBluetoothAddress) == true) {
				mService.stopStreaming(mBluetoothAddress);
				mPlotSeriesMap.clear();
		    	mPlotDataMap.clear();
		    	dynamicPlot.clear();
				
			} else {
				mService.startStreaming(mBluetoothAddress);
				log = new Logging(mFileName,"\t");
			}
			return true;
		} else if (itemId == R.id.settings) {
     		Shimmer shimmer = mService.getShimmer(mBluetoothAddress);
			showEnableSensors(shimmer.getListofSupportedSensors(),mService.getEnabledSensors(mBluetoothAddress));
			return true;
		} else if (itemId == R.id.viewsensor) {
			showSelectSensorPlot();
			return true;
		} else if (itemId == R.id.commands) {
			Intent commandIntent=new Intent(this, CommandsActivity.class);
			commandIntent.putExtra("BluetoothAddress",mBluetoothAddress);
			commandIntent.putExtra("SamplingRate",mService.getSamplingRate(mBluetoothAddress));
			commandIntent.putExtra("AccelerometerRange",mService.getAccelRange(mBluetoothAddress));
			commandIntent.putExtra("GSRRange",mService.getGSRRange(mBluetoothAddress));
			commandIntent.putExtra("BatteryLimit",mService.getBattLimitWarning(mBluetoothAddress));
			startActivityForResult(commandIntent, REQUEST_COMMAND_SHIMMER);
			return true;
		} else if (itemId == R.id.logfile) {
			Intent logfileIntent=new Intent(this, LogFileActivity.class);
			startActivityForResult(logfileIntent, REQUEST_LOGFILE_SHIMMER);
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}
	
	public void showSelectSensorPlot(){
		mDialog.setContentView(R.layout.dialog_sensor_view);
		TextView title = (TextView) mDialog.findViewById(android.R.id.title);
 		title.setText("Select Signal");
 		final ListView listView = (ListView) mDialog.findViewById(android.R.id.list);
		listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		List<String> sensorList = mService.getListofEnabledSensors(mBluetoothAddress);
		sensorList.add("Timestamp");
		final String[] sensorNames = sensorList.toArray(new String[sensorList.size()]);
		ArrayAdapter<String> adapterSensorNames = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, android.R.id.text1, sensorNames);
		listView.setAdapter(adapterSensorNames);
		
		listView.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
    			mSensorView=sensorNames[arg2];
    			mTextSensor1.setText("");
    			mTextSensor2.setText("");
    			mTextSensor3.setText("");
    			if (mSensorView.equals("Accelerometer")){
        			mTextSensor1.setText("AccelerometerX");
        			mTextSensor2.setText("AccelerometerY");
        			mTextSensor3.setText("AccelerometerZ");
        		}
    			if (mSensorView.equals("Wide Range Accelerometer")){
        			mTextSensor1.setText("AccelerometerX");
        			mTextSensor2.setText("AccelerometerY");
        			mTextSensor3.setText("AccelerometerZ");
        		}
    			if (mSensorView.equals("Low Noise Accelerometer")){
        			mTextSensor1.setText("AccelerometerX");
        			mTextSensor2.setText("AccelerometerY");
        			mTextSensor3.setText("AccelerometerZ");
        		}
    			if (mSensorView.equals("Accelerometer")){
        			mTextSensor1.setText("AccelerometerX");
        			mTextSensor2.setText("AccelerometerY");
        			mTextSensor3.setText("AccelerometerZ");
        		}
        		if (mSensorView.equals("Gyroscope")){
        			mTextSensor1.setText("GyroscopeX");
        			mTextSensor2.setText("GyroscopeY");
        			mTextSensor3.setText("GyroscopeZ");
        		}
        		if (mSensorView.equals("Magnetometer")){
        			mTextSensor1.setText("MagnetometerX");
        			mTextSensor2.setText("MagnetometerY");
        			mTextSensor3.setText("MagnetometerZ");
        		}
        		if (mSensorView.equals("GSR")){
        			mTextSensor1.setText("GSR");
        		}
        		if (mSensorView.equals("EMG")){
        			mTextSensor1.setText("EMG");
        		}
        		if (mSensorView.equals("ECG")){
        			mTextSensor1.setText("ECGRALL");
        			mTextSensor2.setText("ECGLALL");
        		}
        		if (mSensorView.equals("Bridge Amplifier")){
        			mTextSensor1.setText("Bridge Amplifier High");
        			mTextSensor2.setText("Bridge Amplifier Low");
        		}
        		if (mSensorView.equals("Heart Rate")){
        			mTextSensor1.setText("Heart Rate");
        		}
        		if (mSensorView.equals("ExpBoardA0")){
        			mTextSensor1.setText("ExpBoardA0");
        		}
        		if (mSensorView.equals("ExpBoardA7")){
        			mTextSensor1.setText("ExpBoardA7");
        		}
        		if (mSensorView.equals("Timestamp")){
        			mTextSensor1.setText("TimeStamp");
        		} 
        		if (mSensorView.equals("Battery Voltage")){
        			mTextSensor1.setText("VSenseReg");
        			mTextSensor2.setText("VSenseBatt");
        		}
        		if (mSensorView.equals("External ADC A7")){
        			mTextSensor1.setText("External ADC A7");
        		}
        		if (mSensorView.equals("External ADC A6")){
        			mTextSensor1.setText("External ADC A6");
        		}
        		if (mSensorView.equals("External ADC A15")){
        			mTextSensor1.setText("External ADC A15");
        		}
        		if (mSensorView.equals("Internal ADC A1")){
        			mTextSensor1.setText("Internal ADC A1");
        		}
        		if (mSensorView.equals("Internal ADC A12")){
        			mTextSensor1.setText("Internal ADC A12");
        		}
        		if (mSensorView.equals("Internal ADC A13")){
        			mTextSensor1.setText("Internal ADC A13");
        		}
        		if (mSensorView.equals("Internal ADC A14")){
        			mTextSensor1.setText("Internal ADC A14");
        		}
        		if (mSensorView.equals("Pressure")){
        			mTextSensor1.setText("Pressure");
        			mTextSensor2.setText("Temperature");
        		}
        		mValueSensor1.setText("");
        		mValueSensor2.setText("");
        		mValueSensor3.setText("");
        		mPlotDataMap.clear();
        		dynamicPlot.clear();
        		mPlotSeriesMap.clear();
    			mDialog.dismiss();
			}
			
		});
		
		mDialog.show();
 		
	}
	
	
	public void showEnableSensors(final String[] sensorNames, long enabledSensors){
		dialogEnabledSensors=enabledSensors;
		mDialog.setContentView(R.layout.dialog_enable_sensor_view);
		TextView title = (TextView) mDialog.findViewById(android.R.id.title);
 		title.setText("Select Signal");
 		final ListView listView = (ListView) mDialog.findViewById(android.R.id.list);
		listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		ArrayAdapter<String> adapterSensorNames = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, android.R.id.text1, sensorNames);
		listView.setAdapter(adapterSensorNames);
		final BiMap<String,String> sensorBitmaptoName;
		sensorBitmaptoName = Shimmer.generateBiMapSensorIDtoSensorName(mService.getShimmerVersion(mBluetoothAddress));
		for (int i=0;i<sensorNames.length;i++){
			int iDBMValue = Integer.parseInt(sensorBitmaptoName.inverse().get(sensorNames[i]));	
			if( (iDBMValue & enabledSensors) >0){
				listView.setItemChecked(i, true);
			}
		}
				
		listView.setOnItemClickListener(new OnItemClickListener(){
			
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int clickIndex,
					long arg3) {
					int sensorIdentifier = Integer.parseInt(sensorBitmaptoName.inverse().get(sensorNames[clickIndex]));
					//check and remove any old daughter boards (sensors) which will cause a conflict with sensorIdentifier 
					dialogEnabledSensors = mService.sensorConflictCheckandCorrection(mBluetoothAddress,dialogEnabledSensors,sensorIdentifier);
					//update the checkbox accordingly
					for (int i=0;i<sensorNames.length;i++){
						int iDBMValue = Integer.parseInt(sensorBitmaptoName.inverse().get(sensorNames[i]));	
						if( (iDBMValue & dialogEnabledSensors) >0){
							listView.setItemChecked(i, true);
						} else {
							listView.setItemChecked(i, false);
						}
					}
			}
			
		});
		
		Button mDoneButton = (Button)mDialog.findViewById(R.id.buttonEnableSensors);
		
		mDoneButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				mService.setEnabledSensors(dialogEnabledSensors,mBluetoothAddress);
				mDialog.dismiss();
			}});
		
		
		mDialog.show();
 		
	}
	
	
}