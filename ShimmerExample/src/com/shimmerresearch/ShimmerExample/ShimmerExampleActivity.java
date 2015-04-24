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

package com.shimmerresearch.ShimmerExample;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;
import android.os.Bundle;
import android.app.Activity;

import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;

import com.shimmerresearch.driver.*;
import com.shimmerresearch.android.*;


public class ShimmerExampleActivity extends Activity {
    /** Called when the activity is first created. */

    Timer mTimer;
    private Shimmer mShimmerDevice1 = null;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.setTooLegacyObjectClusterSensorNames();
        setContentView(R.layout.main);
        mShimmerDevice1 = new Shimmer(this, mHandler,"RightArm", 51.2, 0, 0, Shimmer.SENSOR_ACCEL|Shimmer.SENSOR_GYRO|Shimmer.SENSOR_MAG, false);
        String bluetoothAddress="00:06:66:66:96:86";
        
        mShimmerDevice1.connect(bluetoothAddress,"default"); 
        Log.d("ConnectionStatus","Trying"); 
    }
    
    @Override
	protected void onPause() {
		super.onPause();
		//device is disconnected if user leaves the application
		mShimmerDevice1.stop();
	}
    
    @Override
    protected void onResume() {
		super.onResume();
		// this is an example of how to do something to the device depending on state when you resume an application
		if(mShimmerDevice1 !=null ){ 
			if (mShimmerDevice1.getState()==Shimmer.STATE_CONNECTED){
				mShimmerDevice1.startStreaming();
			}
			if (mShimmerDevice1.getState()==Shimmer.STATE_NONE){
				mShimmerDevice1 = new Shimmer(this, mHandler,"RightArm", 51.2, 0, 0, Shimmer.SENSOR_ACCEL|Shimmer.SENSOR_GYRO|Shimmer.SENSOR_MAG, false);
			      String bluetoothAddress="00:06:66:66:96:86";
			      mShimmerDevice1.connect(bluetoothAddress,"default");
			}
		} else {
			  mShimmerDevice1 = new Shimmer(this, mHandler,"RightArm", 51.2, 0, 0, Shimmer.SENSOR_ACCEL|Shimmer.SENSOR_GYRO|Shimmer.SENSOR_MAG, false);
		      String bluetoothAddress="00:06:66:66:96:86";
		      mShimmerDevice1.connect(bluetoothAddress,"default"); 
		}
	}
    
 // The Handler that gets information back from the BluetoothChatService
    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) { // handlers have a what identifier which is used to identify the type of msg
            case Shimmer.MESSAGE_READ:
            	if ((msg.obj instanceof ObjectCluster)){	// within each msg an object can be include, objectclusters are used to represent the data structure of the shimmer device
            	    ObjectCluster objectCluster =  (ObjectCluster) msg.obj; 
            	    Collection<FormatCluster> accelXFormats = objectCluster.mPropertyCluster.get(Configuration.Shimmer2.ObjectClusterSensorName.ACCEL_X);  // first retrieve all the possible formats for the current sensor device
					FormatCluster formatCluster = ((FormatCluster)ObjectCluster.returnFormatCluster(accelXFormats,"CAL")); // retrieve the calibrated data
					if (formatCluster!=null){
						Log.d("CalibratedData",objectCluster.mMyName + " AccelX: " + formatCluster.mData + " "+ formatCluster.mUnits);
					}
					Collection<FormatCluster> accelYFormats = objectCluster.mPropertyCluster.get(Configuration.Shimmer2.ObjectClusterSensorName.ACCEL_Y);  // first retrieve all the possible formats for the current sensor device
					formatCluster = ((FormatCluster)ObjectCluster.returnFormatCluster(accelYFormats,"CAL")); // retrieve the calibrated data
					if (formatCluster!=null){
						Log.d("CalibratedData",objectCluster.mMyName + " AccelY: " + formatCluster.mData + " "+formatCluster.mUnits);
					}
					Collection<FormatCluster> accelZFormats = objectCluster.mPropertyCluster.get(Configuration.Shimmer2.ObjectClusterSensorName.ACCEL_Z);  // first retrieve all the possible formats for the current sensor device
					formatCluster = ((FormatCluster)ObjectCluster.returnFormatCluster(accelZFormats,"CAL")); // retrieve the calibrated data
					if (formatCluster!=null){
						Log.d("CalibratedData",objectCluster.mMyName + " AccelZ: " + formatCluster.mData + " "+formatCluster.mUnits);
					}


					accelXFormats = objectCluster.mPropertyCluster.get(Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_LN_X);  // first retrieve all the possible formats for the current sensor device
					formatCluster = ((FormatCluster)ObjectCluster.returnFormatCluster(accelXFormats,"CAL")); // retrieve the calibrated data
					if (formatCluster!=null){
						Log.d("CalibratedData",objectCluster.mMyName + " AccelLNX: " + formatCluster.mData + " "+ formatCluster.mUnits);
					}
					accelYFormats = objectCluster.mPropertyCluster.get(Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_LN_Y);  // first retrieve all the possible formats for the current sensor device
					formatCluster = ((FormatCluster)ObjectCluster.returnFormatCluster(accelYFormats,"CAL")); // retrieve the calibrated data
					if (formatCluster!=null){
						Log.d("CalibratedData",objectCluster.mMyName + " AccelLNY: " + formatCluster.mData + " "+formatCluster.mUnits);
					}
					accelZFormats = objectCluster.mPropertyCluster.get(Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_LN_Z);  // first retrieve all the possible formats for the current sensor device
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
	        	mShimmerDevice1.stopStreaming(); 
	        	mShimmerDevice1.stop(); 
	        	mShimmerDevice1 = new Shimmer(ShimmerExampleActivity.this, mHandler,"RightArm", 51.2, 0, 0, Shimmer.SENSOR_ACCEL|Shimmer.SENSOR_GYRO|Shimmer.SENSOR_MAG, false);
	             String bluetoothAddress="00:06:66:46:B8:9E";
	             mShimmerDevice1.connect(bluetoothAddress,"default"); 
	        }
	    }
    }
    



    
    