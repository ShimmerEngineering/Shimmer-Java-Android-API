//v0.1

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
 * @author Alejandro Saez
 * @date   September, 2014
 * 
 */


package com.shimmerresearch.shimmerlogandstreamexample;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.shimmerresearch.android.Shimmer;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.ObjectCluster;



public class LogAndStreamExample extends Activity{

	
	private static Context context;
	static final int REQUEST_ENABLE_BT = 1;
	static final int REQUEST_CONNECT_SHIMMER = 2;
	static private String CONNECT = "Connect";
	static private String DISCONNECT = "Disconnect";
	static String deviceState = "Disconnected"; 
	static TextView textDeviceName;
	static TextView textDeviceState;
	static TextView textDocked, textSensing, textDirectory;
	
	static Shimmer logAndStreamShimmer;

	static Button buttonStreaming, buttonStreamingAndLogging, buttonConnectDisconnect, buttonStopStreaming, buttonReadDirectory, buttonStatus;
	
	// Local Bluetooth adapter
	private BluetoothAdapter mBluetoothAdapter = null;
	// Name of the connected device
    private static String mBluetoothAddress = null;
	
	
	 /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	Configuration.setTooLegacyObjectClusterSensorNames();
        Log.d("ShimmerActivity","On Create");
        super.onCreate(savedInstanceState);
        
        LogAndStreamExample.context = getApplicationContext();
        setContentView(R.layout.main);
        Log.d("ShimmerActivity","On Create");


        textDeviceName = (TextView) findViewById(R.id.textDeviceName);
        textDeviceState = (TextView) findViewById(R.id.textDeviceState);
        if(mBluetoothAddress!=null){
        	textDeviceName.setText(mBluetoothAddress);
        	textDeviceState.setText(deviceState);
        }      
        
        textDocked = (TextView) findViewById(R.id.textDockedStatus);
        textSensing = (TextView) findViewById(R.id.textSensingStatus);
        textDirectory = (TextView) findViewById(R.id.textDirectoryName);
        
        textDocked.setText("");
        textSensing.setText("");
        textDirectory.setText("");
		      
	    buttonStreaming = (Button) findViewById(R.id.buttonStreaming);
	    buttonStreaming.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				logAndStreamShimmer.startStreaming();
				deviceState = "Streaming";
                textDeviceState.setText(deviceState);
                buttonStopStreaming.setEnabled(true);
        	    buttonStreaming.setEnabled(false);
        	    buttonStreamingAndLogging.setEnabled(false);
			}
		});
	    
	    buttonStreamingAndLogging = (Button) findViewById(R.id.buttonStreamingAndLogging);
	    buttonStreamingAndLogging.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				logAndStreamShimmer.startDataLogAndStreaming();
				deviceState = "Streaming";
                textDeviceState.setText(deviceState);
                buttonStopStreaming.setEnabled(true);
        	    buttonStreaming.setEnabled(false);
        	    buttonStreamingAndLogging.setEnabled(false);
			}
		});
	    
	    buttonStopStreaming = (Button) findViewById(R.id.buttonStopStreaming);
	    buttonStopStreaming.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				logAndStreamShimmer.stopStreaming();
				deviceState = "Connected";
                textDeviceState.setText(deviceState);
                buttonStopStreaming.setEnabled(false);
        	    buttonStreaming.setEnabled(true);
        	    buttonStreamingAndLogging.setEnabled(true);
			}
		});
	    
	    buttonReadDirectory = (Button) findViewById(R.id.buttonDirectory);
	    buttonReadDirectory.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//read the directory name
				logAndStreamShimmer.readDirectoryName();
				//wait the directory name from the Shimmer
				try {
					Thread.sleep(300);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//get the directory name
				String directory = logAndStreamShimmer.getDirectoryName();
				textDirectory.setText(directory);
			}
		});
	    
	    buttonStatus = (Button) findViewById(R.id.buttonStatus);
	    buttonStatus.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				//read the current status
				logAndStreamShimmer.readStatusLogAndStream();
				//wait the directory name from the Shimmer
				try {
					Thread.sleep(300);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//get the current status
				boolean docked = logAndStreamShimmer.isDocked();
				boolean sensing = logAndStreamShimmer.isSensing();
				if(docked)
					textDocked.setText("Yes");
				else
					textDocked.setText("No");
				
				if(sensing)
					textSensing.setText("Yes");
				else
					textSensing.setText("No");
			}
		});
	    
	    buttonStopStreaming.setEnabled(false);
	    buttonStreaming.setEnabled(false);
	    buttonStreamingAndLogging.setEnabled(false);
	    buttonReadDirectory.setEnabled(false);
	    buttonStatus.setEnabled(false);
	    
	    buttonConnectDisconnect = (Button) findViewById(R.id.buttonConnect);
	    buttonConnectDisconnect.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(deviceState.equals("Disconnected")){
					Intent serverIntent = new Intent(getApplicationContext(), DeviceListActivity.class);
					startActivityForResult(serverIntent, REQUEST_CONNECT_SHIMMER);
				}
				else
					logAndStreamShimmer.stop();
				
			}
		}); 
	    
	    
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter == null) {
        	Toast.makeText(this, "Device does not support Bluetooth\nExiting...", Toast.LENGTH_LONG).show();
        	finish();
        }
 
    }
	
    
    @Override
    public void onStart() {
    	super.onStart();

    	if(!mBluetoothAdapter.isEnabled()) {     	
        	Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        	startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    	}

    }
    
    
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
                    mBluetoothAddress=((ObjectCluster)msg.obj).mBluetoothAddress; 
                    deviceState = "Connected";
                    textDeviceName.setText(mBluetoothAddress);
                    textDeviceState.setText(deviceState);
                    buttonConnectDisconnect.setEnabled(true);
                    buttonConnectDisconnect.setText(DISCONNECT);
                    buttonStopStreaming.setEnabled(false);
            	    buttonStreaming.setEnabled(true);
            	    buttonStreamingAndLogging.setEnabled(true);
            	    buttonReadDirectory.setEnabled(true);
            	    buttonStatus.setEnabled(true);
            	    logAndStreamShimmer.readStatusLogAndStream();
            	    try {
						Thread.sleep(300);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
            	    if(logAndStreamShimmer.isDocked())
            	    	textDocked.setText("Yes");
            	    else
            	    	textDocked.setText("No");
            	    
            	    if(logAndStreamShimmer.isSensing())
                		textSensing.setText("Yes");
            	    else
            	    	textSensing.setText("No");
                    break;
                case Shimmer.STATE_CONNECTING:
                	Log.d("ShimmerActivity","Driver is attempting to establish connection with Shimmer device");
                	deviceState = "Connecting";
                    textDeviceName.setText(mBluetoothAddress);
                    textDeviceState.setText(deviceState);
                    buttonConnectDisconnect.setEnabled(false);
                    break;
                case Shimmer.MSG_STATE_STREAMING:
                	buttonStreaming.setEnabled(false);
            	    buttonStreamingAndLogging.setEnabled(false);
            	    buttonStopStreaming.setEnabled(true);
            	    buttonReadDirectory.setEnabled(false);
            	    buttonStatus.setEnabled(false);
            	    textDeviceState.setText("Streaming");
            	    textSensing.setText("Yes");
                	break;
                case Shimmer.MSG_STATE_STOP_STREAMING:
                	buttonStreaming.setEnabled(true);
            	    buttonStreamingAndLogging.setEnabled(true);
            	    buttonStopStreaming.setEnabled(false);
            	    buttonReadDirectory.setEnabled(true);
            	    buttonStatus.setEnabled(true);
            	    textDeviceState.setText("Connected");
           	    	textSensing.setText("No");
                	break;
                case Shimmer.STATE_NONE:
                	Log.d("ShimmerActivity","Shimmer No State");
                    mBluetoothAddress=null;
                    // this also stops streaming
                    deviceState = "Disconnected";
                    textDeviceName.setText("Unknown");
                    textDeviceState.setText(deviceState);
                    buttonConnectDisconnect.setEnabled(true);
                    buttonConnectDisconnect.setText(CONNECT);
                    buttonStopStreaming.setEnabled(false);
            	    buttonStreaming.setEnabled(false);
            	    buttonStreamingAndLogging.setEnabled(false);
            	    buttonReadDirectory.setEnabled(false);
            	    buttonStatus.setEnabled(false);
                    break;
                }
                break;
            case Shimmer.MESSAGE_READ:


				
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
                
            case Shimmer.MESSAGE_LOG_AND_STREAM_STATUS_CHANGED:
            	int docked = msg.arg1;
            	int sensing = msg.arg2;
            	if(docked==1)
            		textDocked.setText("Yes");
            	else
            		textDocked.setText("No");
            	
            	if(sensing==1)
            		textSensing.setText("Yes");
            	else
            		textSensing.setText("No");
            	break;
           
            }
			
			
        }
    };
	
	
    private static Context getContext(){
    	return LogAndStreamExample.context;
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
	          		mBluetoothAddress = address;
	                logAndStreamShimmer=new Shimmer(this, mHandler,"Shimmer",false);
	                logAndStreamShimmer.connect(mBluetoothAddress,"default");

	            }
	            break;

    	}
	}
	
}
