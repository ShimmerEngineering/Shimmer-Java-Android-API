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


package com.shimmerresearch.broadcastserviceexample;

import java.util.Collection;

import com.shimmerresearch.android.Shimmer;
import com.shimmerresearch.driver.*;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class MyService extends Service {
	private static final String TAG = "MyService";
    private Shimmer shimmerDevice1 = null;
	private BluetoothAdapter mBluetoothAdapter = null;
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onCreate() {
		Configuration.setTooLegacyObjectClusterSensorNames();
		Toast.makeText(this, "My Service Created", Toast.LENGTH_LONG).show();
		Log.d(TAG, "onCreate");
	}

	@Override
	public void onDestroy() {
		Toast.makeText(this, "My Service Stopped", Toast.LENGTH_LONG).show();
		Log.d(TAG, "onDestroy");
		shimmerDevice1.stopStreaming();
		shimmerDevice1.stop();
	}
	
	@Override
	public void onStart(Intent intent, int startid) {
		Toast.makeText(this, "My Service Started", Toast.LENGTH_LONG).show();
		Log.d(TAG, "onStart");
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        shimmerDevice1 = new Shimmer(this, mHandler,"RightArm",51.2, 0,0, Shimmer.SENSOR_ACCEL, false); 
        String bluetoothAddress="00:06:66:46:B7:BE";
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(bluetoothAddress);
        shimmerDevice1.connect(bluetoothAddress,"default"); //Right Arm is a unique identifier for the shimmer unit
       
       
	}
	
	  private final Handler mHandler = new Handler() {
	        public void handleMessage(Message msg) {
	            switch (msg.what) { // handlers have a what identifier which is used to identify the type of msg
	            case Shimmer.MESSAGE_READ:
	            	if ((msg.obj instanceof ObjectCluster)){	// within each msg an object can be include, objectclusters are used to represent the data structure of the shimmer device
	            	    ObjectCluster objectCluster =  (ObjectCluster) msg.obj; 
	            	    if (objectCluster.mMyName=="RightArm"){
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
	     						Intent intent = new Intent();
			            	    intent.setAction("my.shimmer");
			            	    intent.putExtra("AccelZ", formatCluster.mData);  
				 	    		sendBroadcast(intent);
				 	    	
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
	     						Intent intent = new Intent();
			            	    intent.setAction("my.shimmer");
			            	    intent.putExtra("AccelZ", formatCluster.mData);  
				 	    		sendBroadcast(intent);
				 	    	
	     					}
				 	    	
				 	    	
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
	                    	    if (shimmerDevice1.getShimmerState()==Shimmer.STATE_CONNECTED){
	                    	        Log.d("ConnectionStatus","Successful");
	                    	        shimmerDevice1.startStreaming();
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
}
