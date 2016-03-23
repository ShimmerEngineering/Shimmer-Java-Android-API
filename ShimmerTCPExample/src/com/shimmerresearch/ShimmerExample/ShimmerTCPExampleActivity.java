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

import java.io.DataOutputStream;
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
import com.shimmerresearch.bluetooth.ShimmerBluetooth.BT_STATE;


public class ShimmerTCPExampleActivity extends Activity {
    /** Called when the activity is first created. */

    Timer mTimer;
    private Shimmer mShimmerDevice1 = null;
    DataOutputStream dOut = null;
    Socket clientSocket;
    String FromServer="";
    String ToServer;
    boolean firstTime = true;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mShimmerDevice1 = new Shimmer(this, mHandler,"RightArm",false); 
        String bluetoothAddress="00:06:66:66:96:86";
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
            	    byte[] dataojc = objectCluster.serialize();
            	    if(dOut!=null){
            	    try {
						dOut.writeInt(dataojc.length);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
            	    try {
						dOut.write(dataojc);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
            	    } else {
            	    	System.out.print("dout fail \n");
            	    }

		 	    	
            	}
                break;
                 case Shimmer.MESSAGE_TOAST:
                	Log.d("toast",msg.getData().getString(Shimmer.TOAST));
                	Toast.makeText(getApplicationContext(), msg.getData().getString(Shimmer.TOAST),
                            Toast.LENGTH_SHORT).show();
                break;

                 case Shimmer.MESSAGE_STATE_CHANGE:


                     switch (((ObjectCluster)msg.obj).mState) {
                     case CONNECTED:


                         
              	    	if (firstTime){
              	    	mShimmerDevice1.writeEnabledSensors(ShimmerObject.SENSOR_ACCEL);
              	    	Thread thread = new Thread()
              			{
              			    @Override
              			    public void run() {
              			    	try {
              			    		clientSocket = new Socket("10.1.1.1", 5000);
              				        dOut = new DataOutputStream(clientSocket.getOutputStream());
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
              	
              	        mShimmerDevice1.startStreaming();
              	        firstTime = false;
              	    	}
                         break;
                     case CONNECTING:

	                    	Log.d("ConnectionStatus","Connecting");
                         break;
                     case STREAMING:
                     	break;
                     case STREAMING_AND_SDLOGGING:
                     	break;
                     case SDLOGGING:
                    	 break;
                     case DISCONNECTED:
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
	        	//mShimmerDevice1.stopStreaming();
	        	//mShimmerDevice1.writeEnabledSensors(Shimmer.SENSOR_ACCEL);
	        	//mShimmerDevice1.startStreaming();
	        	shimmerTimer(5); //Disconnect in 30 seconds
	        }
	    }
    }
    



    
    