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

//Future updates needed
//- the handler should be converted to static 


package com.shimmerresearch.shimmerlogexample;


import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;

import com.shimmerresearch.shimmerlogexample.R;

import android.os.Handler;
import android.os.Message;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.util.Log;
import android.widget.Toast;

import android.os.Bundle;
import android.app.Activity;
import com.shimmerresearch.driver.*;
import com.shimmerresearch.android.*;
import com.shimmerresearch.tools.Logging;

public class ShimmerLogExampleActivity extends Activity {
    /** Called when the activity is first created. */

    
    private Shimmer mShimmerDevice1 = null;
	private BluetoothAdapter mBluetoothAdapter = null;
	boolean mFirstWrite=true;
    private static String mFileName = "shimmerbasicloggingexample";
    static Logging log = new Logging(mFileName,"\t"); //insert file name
    Timer mTimer;	
    boolean stop=false;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.setTooLegacyObjectClusterSensorNames();
        setContentView(R.layout.main);
        
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mShimmerDevice1 = new Shimmer(this, mHandler,"uppertorso",10,0,4,Shimmer.SENSOR_ACCEL,true); //Right Arm is a unique identifier for the shimmer unit
        String bluetoothAddress="00:06:66:66:96:86";
        mShimmerDevice1.connect(bluetoothAddress,"default"); 
        Log.d("ConnectionStatus","Trying");


    }
 // The Handler that gets information back from the BluetoothChatService
    private final Handler mHandler = new Handler() {
        @SuppressWarnings("null")
		public void handleMessage(Message msg) {
            switch (msg.what) { // handlers have a what identifier which is used to identify the type of msg
            case Shimmer.MESSAGE_READ:
        		String[] signalNameArray=new String[4]; 
        		String myAddress="";
        		double[] dataValues=new double[4]; 
            	if ((msg.obj instanceof ObjectCluster)){	// within each msg an object can be include, objectclusters are used to represent the data structure of the shimmer device
            	    ObjectCluster objectCluster =  (ObjectCluster) msg.obj; 
            	    log.logData(objectCluster);
            	    Collection<FormatCluster> accelXFormats = objectCluster.mPropertyCluster.get("Accelerometer X");  // first retrieve all the possible formats for the current sensor device
					FormatCluster formatCluster = ((FormatCluster)ObjectCluster.returnFormatCluster(accelXFormats,"CAL")); // retrieve the calibrated data
					if (formatCluster!=null){
						Log.d("CalibratedData",objectCluster.mMyName + " AccelX: " + formatCluster.mData + " "+ formatCluster.mUnits);
					}
					Collection<FormatCluster> accelYFormats = objectCluster.mPropertyCluster.get("Accelerometer Y");  // first retrieve all the possible formats for the current sensor device
					formatCluster = ((FormatCluster)ObjectCluster.returnFormatCluster(accelYFormats,"CAL")); // retrieve the calibrated data
					if (formatCluster!=null){
						Log.d("CalibratedData",objectCluster.mMyName + " AccelY: " + formatCluster.mData + " "+formatCluster.mUnits);
					}
					Collection<FormatCluster> accelZFormats = objectCluster.mPropertyCluster.get("Accelerometer Z");  // first retrieve all the possible formats for the current sensor device
					formatCluster = ((FormatCluster)ObjectCluster.returnFormatCluster(accelZFormats,"CAL")); // retrieve the calibrated data
					if (formatCluster!=null){
						Log.d("CalibratedData",objectCluster.mMyName + " AccelZ: " + formatCluster.mData + " "+formatCluster.mUnits);
					}


					accelXFormats = objectCluster.mPropertyCluster.get("Low Noise Accelerometer X");  // first retrieve all the possible formats for the current sensor device
					formatCluster = ((FormatCluster)ObjectCluster.returnFormatCluster(accelXFormats,"CAL")); // retrieve the calibrated data
					if (formatCluster!=null){
						Log.d("CalibratedData",objectCluster.mMyName + " AccelLNX: " + formatCluster.mData + " "+ formatCluster.mUnits);
					}
					accelYFormats = objectCluster.mPropertyCluster.get("Low Noise Accelerometer Y");  // first retrieve all the possible formats for the current sensor device
					formatCluster = ((FormatCluster)ObjectCluster.returnFormatCluster(accelYFormats,"CAL")); // retrieve the calibrated data
					if (formatCluster!=null){
						Log.d("CalibratedData",objectCluster.mMyName + " AccelLNY: " + formatCluster.mData + " "+formatCluster.mUnits);
					}
					accelZFormats = objectCluster.mPropertyCluster.get("Low Noise Accelerometer Z");  // first retrieve all the possible formats for the current sensor device
					formatCluster = ((FormatCluster)ObjectCluster.returnFormatCluster(accelZFormats,"CAL")); // retrieve the calibrated data
					if (formatCluster!=null){
						Log.d("CalibratedData",objectCluster.mMyName + " AccelLNZ: " + formatCluster.mData + " "+formatCluster.mUnits);
					}

            	}
            	
                break;
                 case Shimmer.MESSAGE_TOAST:
                	Log.d("toast",msg.getData().getString(Shimmer.TOAST));
                	Toast.makeText(getApplicationContext(), msg.getData().getString(Shimmer.TOAST),
                            Toast.LENGTH_SHORT).show();
                break;
                 case Shimmer.MESSAGE_STATE_CHANGE:
                switch (msg.arg1) {
             	case Shimmer.MSG_STATE_FULLY_INITIALIZED:
            	    if (mShimmerDevice1.getShimmerState()==Shimmer.STATE_CONNECTED){
            	        Log.d("ConnectionStatus","Successful");
            	        mShimmerDevice1.startStreaming();
            	        shimmerTimer(30); //Disconnect in 30 seconds
            	     }
            	    break;
                case Shimmer.STATE_CONNECTING:
                	Log.d("ConnectionStatus","Connecting");
        	        break;
                case Shimmer.STATE_NONE:
                	Log.d("ConnectionStatus","No State");
                	break;
             }
        break;
            }
        }
    };

	  public synchronized void shimmerTimer(int seconds) {
	        mTimer = new Timer();
	        mTimer.schedule(new responseTask(), seconds*1000);
	        
		}
	    
	    class responseTask extends TimerTask {
	        public void run() {
	        	stop=true;
	        	log.closeFile();
	        	mShimmerDevice1.stop();
	        }
	    }
    
    
    }
    



    
    