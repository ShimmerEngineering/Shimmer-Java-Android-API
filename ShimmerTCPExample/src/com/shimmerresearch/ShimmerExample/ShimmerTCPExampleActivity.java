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

import java.io.IOException;

import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;

import com.shimmerresearch.ShimmerExample.R;

import com.shimmerresearch.driver.*;
import com.shimmerresearch.android.*;


public class ShimmerTCPExampleActivity extends Activity {
    /** Called when the activity is first created. */

    Timer mTimer;
    private Shimmer mShimmerDevice1 = null;
    PrintWriter outToServer=null;
    Socket clientSocket;
    String FromServer="";
    String ToServer;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mShimmerDevice1 = new Shimmer(this, mHandler,"RightArm",false); 
        String bluetoothAddress="00:06:66:46:B8:A2";
        mShimmerDevice1.connect(bluetoothAddress,"default"); 
        Log.d("ConnectionStatus","Trying"); 
        
        
       
        
    }
    
 // The Handler that gets information back from the BluetoothChatService
    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) { // handlers have a what identifier which is used to identify the type of msg
            case Shimmer.MESSAGE_READ:
            	if ((msg.obj instanceof ObjectCluster)){	// within each msg an object can be include, objectclusters are used to represent the data structure of the shimmer device
            	    ObjectCluster objectCluster =  (ObjectCluster) msg.obj; 
            	    if (objectCluster.mMyName=="RightArm"){
            	    	Collection<FormatCluster> accelXFormats = null;
            	    	if (mShimmerDevice1.getShimmerVersion()!=Shimmer.SHIMMER_3){
            	    		accelXFormats = objectCluster.mPropertyCluster.get("Accelerometer X");  // first retrieve all the possible formats for the current sensor device
            	    	} else {
            	    		accelXFormats = objectCluster.mPropertyCluster.get("Low Noise Accelerometer X");  // first retrieve all the possible formats for the current sensor device
            	    	}
			 	    	if (accelXFormats != null){
			 	    		FormatCluster formatCluster = ((FormatCluster)ObjectCluster.returnFormatCluster(accelXFormats,"CAL")); // retrieve the calibrated data
			 	    		Log.d("CalibratedData","AccelX: " + formatCluster.mData + " "+ formatCluster.mUnits);
			 	    		if (outToServer!=null){
			 	    			DecimalFormat formatter = new java.text.DecimalFormat("0000.00");
			 	    			if(formatCluster.mData<0){
			 	    				 formatter = new java.text.DecimalFormat("000.00");
			 	    			} 
			 	    			outToServer.println ("Accel X=" + formatter.format(formatCluster.mData)) ;
			 	    		}
			 	    	}
			 	    	Collection<FormatCluster> accelYFormats = null;
			 	    	
			 	    	if (mShimmerDevice1.getShimmerVersion()!=Shimmer.SHIMMER_3){
			 	    		accelYFormats = objectCluster.mPropertyCluster.get("Accelerometer Y");  // first retrieve all the possible formats for the current sensor device
			 	    	} else{
			 	    		accelYFormats = objectCluster.mPropertyCluster.get("Low Noise Accelerometer Y");  // first retrieve all the possible formats for the current sensor device
				 	    }
			 	    	if (accelYFormats != null){
			 	    		FormatCluster formatCluster = ((FormatCluster)ObjectCluster.returnFormatCluster(accelYFormats,"CAL")); // retrieve the calibrated data
			 	    		Log.d("CalibratedData","AccelY: " + formatCluster.mData + " "+formatCluster.mUnits);
			 	    		if (outToServer!=null){
			 	    			DecimalFormat formatter = new java.text.DecimalFormat("0000.00");
			 	    			if(formatCluster.mData<0){
			 	    				 formatter = new java.text.DecimalFormat("000.00");
			 	    			} 
			 	    			outToServer.println ("Accel Y=" + formatter.format(formatCluster.mData)) ;
			 	    		}
			 	    	}
            	    }
		 	    	Collection<FormatCluster> accelZFormats = null;
		 	    	
		 	    	if (mShimmerDevice1.getShimmerVersion()!=Shimmer.SHIMMER_3){
		 	    		accelZFormats = objectCluster.mPropertyCluster.get("Accelerometer Z");  // first retrieve all the possible formats for the current sensor device
		 	    	} else{
		 	    		accelZFormats = objectCluster.mPropertyCluster.get("Low Noise Accelerometer Z");  // first retrieve all the possible formats for the current sensor device
			 	    }
			 	    	if (accelZFormats != null){
			 	    		FormatCluster formatCluster = ((FormatCluster)ObjectCluster.returnFormatCluster(accelZFormats,"CAL")); // retrieve the calibrated data
			 	    		Log.d("CalibratedData","AccelZ: " + formatCluster.mData + " "+formatCluster.mUnits);
			 	    		if (outToServer!=null){
			 	    			DecimalFormat formatter = new java.text.DecimalFormat("0000.00");
			 	    			if(formatCluster.mData<0){
			 	    				 formatter = new java.text.DecimalFormat("000.00");
			 	    			} 
			 	    			outToServer.println ("Accel Z=" + formatter.format(formatCluster.mData)) ;
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
                    	    if (mShimmerDevice1.getShimmerState()==Shimmer.STATE_CONNECTED){
                    	    	
                    	    	Thread thread = new Thread()
                    			{
                    			    @Override
                    			    public void run() {
                    			    	try {
                    			    		clientSocket = new Socket("10.0.0.178", 5000);
                    				        outToServer = new PrintWriter(clientSocket.getOutputStream(),true);
                    				        
                    					} catch (UnknownHostException e) {
                    						// TODO Auto-generated catch block
                    						e.printStackTrace();
                    					} catch (IOException e) {
                    						// TODO Auto-generated catch block
                    						e.printStackTrace();
                    					}
                    			    }
                    			};

                    			thread.start();
                    	    	
                    	    	
                    	        Log.d("ConnectionStatus","Successful");
                    	        mShimmerDevice1.writeEnabledSensors(Shimmer.SENSOR_ACCEL);
                    	        mShimmerDevice1.writeAccelRange(2);
                    	        mShimmerDevice1.readAccelRange();
                    	        mShimmerDevice1.writeSamplingRate(51.2);
                    	        mShimmerDevice1.startStreaming();
                    	        shimmerTimer(5); //Disconnect in 30 seconds
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
	        	mShimmerDevice1.writeEnabledSensors(Shimmer.SENSOR_ACCEL);
	        	mShimmerDevice1.startStreaming();
	        	shimmerTimer(5); //Disconnect in 30 seconds
	        }
	    }
    }
    



    
    