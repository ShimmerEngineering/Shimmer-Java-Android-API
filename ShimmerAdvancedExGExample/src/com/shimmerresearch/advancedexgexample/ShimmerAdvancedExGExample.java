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


package com.shimmerresearch.advancedexgexample;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import pl.flex_it.androidplot.XYSeriesShimmer;

import com.example.shimmeradvancedexgexample.R;
import com.google.common.collect.BiMap;
import com.shimmerresearch.android.Shimmer;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.FormatCluster;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.ShimmerVerDetails;
import com.shimmerresearch.driver.ShimmerObject;
import com.shimmerresearch.service.ShimmerService;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class ShimmerAdvancedExGExample extends ServiceActivity{
	
	private static Context context;
	static final int REQUEST_ENABLE_BT = 1;
	static final int REQUEST_CONNECT_SHIMMER = 2;
	static final int REQUEST_CONFIGURE_SHIMMER = 3;
	static final int REQUEST_COMMAND_SHIMMER = 5;
	private String CONNECT = "Connect";
	private String DISCONNECT = "Disconnect";
	private String START_STREAMING = "Start Streaming";
	private String STOP_STREAMING = "Stop Streaming";
	private String SHIMMER_CONFIGURATION = "Shimmer ExG Configuration";
	private String ENABLE_SENSOR = "Enable Sensor";
	private String LOAD_EXG_VALUES = "Load ExG Values";
	private String DEVICE_INFO = "Device Info";
	
	static boolean isStreaming=false;
	static String exgMode="";
	static String deviceState = "Disconnected"; 
	static TextView textDeviceName;
	static TextView textDeviceState;
	
	static ImageView statusCircle1, statusCircle2, statusCircle3, statusCircle4, statusCircle5;
	static TextView chip1Item1, chip1Item2, chip1Item3, chip1Item4, chip1Item5, chip1Item6, chip1Item7, chip1Item8, chip1Item9, chip1Item10;
	static TextView chip2Item1, chip2Item2, chip2Item3, chip2Item4, chip2Item5, chip2Item6, chip2Item7, chip2Item8, chip2Item9, chip2Item10;
	TextView mShimmerVersion, mFirmwareVersion;
	
	static byte[] exgChip1Array = new byte[10];
	static byte[] exgChip2Array = new byte[10];

	public ArrayAdapter<String> arrayAdapter;
	private static Button buttonMenu;
	public AlertDialog.Builder menuListViewDialog;
	
	// Local Bluetooth adapter
	private BluetoothAdapter mBluetoothAdapter = null;
	// Name of the connected device
	private static String mBluetoothAddress = null;
	
	long dialogEnabledSensors;
	Dialog mDialog, deviceInfoDialog;
	
	 @Override
	    public void onCreate(Bundle savedInstanceState) {
	    	super.onCreate(savedInstanceState);
	        Configuration.setTooLegacyObjectClusterSensorNames();
	        setContentView(R.layout.main);
	        context = getApplicationContext();
	        
	        
	        statusCircle1 = (ImageView) findViewById(R.id.imageLeadOff1);
	        statusCircle2 = (ImageView) findViewById(R.id.imageLeadOff2);
	        statusCircle3 = (ImageView) findViewById(R.id.imageLeadOff3);
	        statusCircle4 = (ImageView) findViewById(R.id.imageLeadOff4);
	        statusCircle5 = (ImageView) findViewById(R.id.imageLeadOff5);
	        
	        
	        chip1Item1 = (TextView) findViewById(R.id.texChip1_1);
	        chip1Item2 = (TextView) findViewById(R.id.texChip1_2);
	        chip1Item3 = (TextView) findViewById(R.id.texChip1_3);
	        chip1Item4 = (TextView) findViewById(R.id.texChip1_4);
	        chip1Item5 = (TextView) findViewById(R.id.texChip1_5);
	        chip1Item6 = (TextView) findViewById(R.id.texChip1_6);
	        chip1Item7 = (TextView) findViewById(R.id.texChip1_7);
	        chip1Item8 = (TextView) findViewById(R.id.texChip1_8);
	        chip1Item9 = (TextView) findViewById(R.id.texChip1_9);
	        chip1Item10 = (TextView) findViewById(R.id.texChip1_10);
	        
	        chip2Item1 = (TextView) findViewById(R.id.texChip2_1);
	        chip2Item2 = (TextView) findViewById(R.id.texChip2_2);
	        chip2Item3 = (TextView) findViewById(R.id.texChip2_3);
	        chip2Item4 = (TextView) findViewById(R.id.texChip2_4);
	        chip2Item5 = (TextView) findViewById(R.id.texChip2_5);
	        chip2Item6 = (TextView) findViewById(R.id.texChip2_6);
	        chip2Item7 = (TextView) findViewById(R.id.texChip2_7);
	        chip2Item8 = (TextView) findViewById(R.id.texChip2_8);
	        chip2Item9 = (TextView) findViewById(R.id.texChip2_9);
	        chip2Item10 = (TextView) findViewById(R.id.texChip2_10);
	        
	        textDeviceName = (TextView) findViewById(R.id.textDeviceName);
	        textDeviceState = (TextView) findViewById(R.id.textDeviceState);
	        if(mBluetoothAddress!=null){
	        	textDeviceName.setText(mBluetoothAddress);
	        	textDeviceState.setText(deviceState);
	        }
	        
	        mDialog = new Dialog(this);
	        deviceInfoDialog = new Dialog(this);
	        deviceInfoDialog.setTitle("Device Info");
	        deviceInfoDialog.setContentView(R.layout.device_info);
	        mShimmerVersion = (TextView) deviceInfoDialog.findViewById(R.id.TextShimmerVersion);
	        mFirmwareVersion = (TextView) deviceInfoDialog.findViewById(R.id.TextFirmwareVersion);
	        
	        menuListViewDialog =  new AlertDialog.Builder(this);
	        menuListViewDialog.setTitle("Menu");
	        buttonMenu = (Button) findViewById(R.id.menuButton);
	        buttonMenu.setEnabled(true);
	        buttonMenu.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					arrayAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.menu_item);
					if(mService.DevicesConnected(mBluetoothAddress)){
						arrayAdapter.add(DISCONNECT);
						if(mService.DeviceIsStreaming(mBluetoothAddress))
							arrayAdapter.add(STOP_STREAMING);
						else{
							arrayAdapter.add(START_STREAMING);
							arrayAdapter.add(ENABLE_SENSOR);
							arrayAdapter.add(SHIMMER_CONFIGURATION);
							arrayAdapter.add(LOAD_EXG_VALUES);
							arrayAdapter.add(DEVICE_INFO);
						}
					}
					else
						arrayAdapter.add(CONNECT);
					
					OnOptionSelected oos = new OnOptionSelected();
					menuListViewDialog.setAdapter(arrayAdapter, oos);
					menuListViewDialog.show();
				}
			});
	        
			 
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
		      }         
		           
			 
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
	 
	 
	 class OnOptionSelected implements DialogInterface.OnClickListener{

			@Override
			public void onClick(DialogInterface arg0, int which) {
				// TODO Auto-generated method stub
				String optionSelected = arrayAdapter.getItem(which);
				if(optionSelected.equals(CONNECT)){
					
					Intent serverIntent = new Intent(getApplicationContext(), DeviceListActivity.class);
					startActivityForResult(serverIntent, REQUEST_CONNECT_SHIMMER);
						
				} else if(optionSelected.equals(DISCONNECT)){
					
					mService.disconnectAllDevices();
										
				} else if(optionSelected.equals(START_STREAMING)){
					
					mService.startStreaming(mBluetoothAddress);
					deviceState="Streaming";
	                textDeviceName.setText(mBluetoothAddress);
	                textDeviceState.setText(deviceState);
	                printExGArrays();
	                if(mService.isEXGUsingECG16Configuration(mBluetoothAddress) || mService.isEXGUsingECG24Configuration(mBluetoothAddress)){
	                	exgMode="ECG";
	                }
	                else 
	                	exgMode="";
	                
				} else if(optionSelected.equals(STOP_STREAMING)){
									
					mService.stopStreaming(mBluetoothAddress);
			    	deviceState="Connected";
			    	textDeviceName.setText(mBluetoothAddress);
	                textDeviceState.setText(deviceState);
	                statusCircle1.setBackgroundResource(R.drawable.circle_red);
	                statusCircle2.setBackgroundResource(R.drawable.circle_red);
	                statusCircle3.setBackgroundResource(R.drawable.circle_red);
	                statusCircle4.setBackgroundResource(R.drawable.circle_red);
	                statusCircle5.setBackgroundResource(R.drawable.circle_red);
	               
				} else if(optionSelected.equals(ENABLE_SENSOR)){
					
					Shimmer shimmer = mService.getShimmer(mBluetoothAddress);
					String[] sensors = shimmer.getListofSupportedSensors();
					String[] enableSensorsForListView = new String[sensors.length+2]; 
					//remove from the list of sensors EXG1, EXG2, EXG1 16bits, EXG2 16bits, and add ECG,EMG,TestSignal,ECG 16Bits,EMG 16Bits, TestSignal 16Bits
					for(int i=0; i<sensors.length-5;i++)
						enableSensorsForListView[i] = sensors[i];
					
					enableSensorsForListView[sensors.length-5] = sensors[sensors.length-1]; // add "Strain gauge" which is the last one in the sensor list
					enableSensorsForListView[sensors.length-4] = "ECG";
					enableSensorsForListView[sensors.length-3] = "ECG 16Bit";
					enableSensorsForListView[sensors.length-2] = "EMG";
					enableSensorsForListView[sensors.length-1] = "EMG 16Bit";
					enableSensorsForListView[sensors.length] = "Test Signal";
					enableSensorsForListView[sensors.length+1] = "Test Signal 16Bit";
									
					showEnableSensors(enableSensorsForListView,mService.getEnabledSensors(mBluetoothAddress));
					
					
				} else if(optionSelected.equals(SHIMMER_CONFIGURATION)){
					
					Shimmer sm = mService.getShimmer(mBluetoothAddress);
					if(sm.getFirmwareCode()<3)
						Toast.makeText(getApplicationContext(), "The FW is not compatible", Toast.LENGTH_LONG).show();
					else{
						Intent commandIntent=new Intent(getApplicationContext(), CommandsActivity.class);
						commandIntent.putExtra("BluetoothAddress",mBluetoothAddress);
						startActivityForResult(commandIntent, REQUEST_COMMAND_SHIMMER);
					}
							
				} else if(optionSelected.equals(LOAD_EXG_VALUES)){
				
					printExGArrays();
				}
				else if(optionSelected.equals(DEVICE_INFO)){
					
					Shimmer shimmerTemp = mService.getShimmer(mBluetoothAddress);
					String shimmerVersion="";
					switch(shimmerTemp.getShimmerVersion()){
						case ShimmerVerDetails.HW_ID.SHIMMER_2:
							shimmerVersion = "Shimmer 2";
						break;
						case ShimmerVerDetails.HW_ID.SHIMMER_2R:
							shimmerVersion = "Shimmer 2R";
						break;
						case ShimmerVerDetails.HW_ID.SHIMMER_3:
							shimmerVersion = "Shimmer 3";
						break;
						case ShimmerVerDetails.HW_ID.SHIMMER_SR30:
							shimmerVersion = "Shimmer SR30";
						break;
					};
					String FWName = shimmerTemp.getFWVersionName();
					if(FWName.equals(""))
						FWName = "Unknown";
					
					mShimmerVersion.setText("Shimmer Version: "+shimmerVersion);
					mFirmwareVersion.setText("Firmware Version: "+FWName);
					deviceInfoDialog.setCancelable(true);
					deviceInfoDialog.show();
				}
			}
	    	
	    }
	 
	 
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
	                    mService.enableGraphingHandler(true);
	                    deviceState = "Connected";
	                    textDeviceName.setText(mBluetoothAddress);
	                    textDeviceState.setText(deviceState);
	                    buttonMenu.setEnabled(true);
	                    printExGArrays();
	                    break;
	                case Shimmer.STATE_CONNECTING:
	                	Log.d("ShimmerActivity","Driver is attempting to establish connection with Shimmer device");
	                	deviceState = "Connecting";
	                    textDeviceName.setText(mBluetoothAddress);
	                    textDeviceState.setText(deviceState);
	                    break;
	                case Shimmer.STATE_NONE:
	                	Log.d("ShimmerActivity","Shimmer No State");
	                    mBluetoothAddress=null;
	                    // this also stops streaming
	                    deviceState = "Disconnected";
	                    textDeviceName.setText("Unknown");
	                    textDeviceState.setText(deviceState);
	                    buttonMenu.setEnabled(true);
	                    break;
	                }
	                break;
	            case Shimmer.MESSAGE_READ:

	            	if ((msg.obj instanceof ObjectCluster)){
	            	    ObjectCluster objectCluster =  (ObjectCluster) msg.obj;   
	            	    FormatCluster fc1 = objectCluster.returnFormatCluster(objectCluster.mPropertyCluster.get("EXG1 STATUS"), "RAW");
	            	    FormatCluster fc2 = objectCluster.returnFormatCluster(objectCluster.mPropertyCluster.get("EXG2 STATUS"), "RAW");
	            	    
	            	    if(fc1!=null && fc2!=null){
	            	    	int statusChip1 = (int) fc1.mData;
		            	    int statusChip2 = (int) fc2.mData;
		            	    int status1 = 0, status2 = 0, status3 = 0, status4 = 0, status5 = 0;
	            	    	if(exgMode.equals("ECG")){
	            	    		status1 = (statusChip1 & 4) >> 2;
								status2 = (statusChip1 & 8) >> 3;
								status3 = (statusChip1 & 1);
								status4 = (statusChip2 & 4) >> 2;
								status5 = (statusChip1 & 16) >> 4;
	            	    	}
	            	    	
	            	    	if(status1==0)
								statusCircle1.setBackgroundResource(R.drawable.circle_green);
							else
								statusCircle1.setBackgroundResource(R.drawable.circle_red);
							
							if(status2==0)
								statusCircle2.setBackgroundResource(R.drawable.circle_green);
							else
								statusCircle2.setBackgroundResource(R.drawable.circle_red);
							
							if(status3==0)
								statusCircle3.setBackgroundResource(R.drawable.circle_green);
							else
								statusCircle3.setBackgroundResource(R.drawable.circle_red);
							
							if(status4==0)
								statusCircle4.setBackgroundResource(R.drawable.circle_green);
							else
								statusCircle4.setBackgroundResource(R.drawable.circle_red);
							
							if(status5==0)
								statusCircle5.setBackgroundResource(R.drawable.circle_green);
							else
								statusCircle5.setBackgroundResource(R.drawable.circle_red);
	            	    }
	            	                	    
	            	}
					
	                break;
	            case Shimmer.MESSAGE_ACK_RECEIVED:
	            	
	            	break;
	            case Shimmer.MESSAGE_DEVICE_NAME:
	                // save the connected device's name
	                
	                Toast.makeText(context, "Connected to "
	                               + mBluetoothAddress, Toast.LENGTH_SHORT).show();
	                break;
	       
	            	
	            case Shimmer.MESSAGE_TOAST:
	                Toast.makeText(context, msg.getData().getString(Shimmer.TOAST),
	                               Toast.LENGTH_SHORT).show();
	                break;
	           
	            }
	        }
	    };
	 
	 
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
	                buttonMenu.setEnabled(false);
	                //mShimmerDevice.connect(address,"gerdavax");
	                //mShimmerDevice.setgetdatainstruction("a");
	            }
	            break;
	        }
		}
	 
	 	public static  void printExGArrays(){
	 		
	 		Shimmer sm = mService.getShimmer(mBluetoothAddress);
	 		
	 		exgChip1Array = sm.getExG1Register();
	 		exgChip2Array = sm.getExG2Register();
	 		
	 		chip1Item1.setText(""+exgChip1Array[0]);
	 		chip1Item2.setText(""+exgChip1Array[1]);
	 		chip1Item3.setText(""+exgChip1Array[2]);
	 		chip1Item4.setText(""+exgChip1Array[3]);
	 		chip1Item5.setText(""+exgChip1Array[4]);
	 		chip1Item6.setText(""+exgChip1Array[5]);
	 		chip1Item7.setText(""+exgChip1Array[6]);
	 		chip1Item8.setText(""+exgChip1Array[7]);
	 		chip1Item9.setText(""+exgChip1Array[8]);
	 		chip1Item10.setText(""+exgChip1Array[9]);
	 		
	 		chip2Item1.setText(""+exgChip2Array[0]);
	 		chip2Item2.setText(""+exgChip2Array[1]);
	 		chip2Item3.setText(""+exgChip2Array[2]);
	 		chip2Item4.setText(""+exgChip2Array[3]);
	 		chip2Item5.setText(""+exgChip2Array[4]);
	 		chip2Item6.setText(""+exgChip2Array[5]);
	 		chip2Item7.setText(""+exgChip2Array[6]);
	 		chip2Item8.setText(""+exgChip2Array[7]);
	 		chip2Item9.setText(""+exgChip2Array[8]);
	 		chip2Item10.setText(""+exgChip2Array[9]);
	 	}
	 
		public void showEnableSensors(final String[] sensorNames, long enabledSensors){
			dialogEnabledSensors=enabledSensors;
			mDialog.setContentView(R.layout.dialog_enable_sensor_view);
			TextView title = (TextView) mDialog.findViewById(android.R.id.title);
	 		title.setText("Enable Sensor");
	 		final ListView listView = (ListView) mDialog.findViewById(android.R.id.list);
			listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
			ArrayAdapter<String> adapterSensorNames = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, android.R.id.text1, sensorNames);
			listView.setAdapter(adapterSensorNames);
			final BiMap<String,String> sensorBitmaptoName;
			sensorBitmaptoName = Shimmer.generateBiMapSensorIDtoSensorName(mService.getShimmerVersion(mBluetoothAddress));
			for (int i=0;i<sensorNames.length;i++){
				if(mService.getShimmerVersion(mBluetoothAddress)==ShimmerVerDetails.HW_ID.SHIMMER_3 && 
						(sensorNames[i].equals("ECG") || sensorNames[i].equals("EMG") || sensorNames[i].equals("Test Signal") ||
						sensorNames[i].equals("ECG 16Bit") || sensorNames[i].equals("EMG 16Bit") || sensorNames[i].equals("Test Signal 16Bit"))){
					
					if(sensorNames[i].equals("ECG") && mService.isEXGUsingECG24Configuration(mBluetoothAddress))
						listView.setItemChecked(i, true);
					else if(sensorNames[i].equals("ECG 16Bit") && mService.isEXGUsingECG16Configuration(mBluetoothAddress))
						listView.setItemChecked(i, true);
					else if(sensorNames[i].equals("EMG") && mService.isEXGUsingEMG24Configuration(mBluetoothAddress))
						listView.setItemChecked(i, true);
					else if(sensorNames[i].equals("EMG 16Bit") && mService.isEXGUsingEMG16Configuration(mBluetoothAddress))
						listView.setItemChecked(i, true);
					else if(sensorNames[i].equals("Test Signal") && mService.isEXGUsingTestSignal24Configuration(mBluetoothAddress))
						listView.setItemChecked(i, true);
					else if(sensorNames[i].equals("Test Signal 16Bit") && mService.isEXGUsingTestSignal16Configuration(mBluetoothAddress))
						listView.setItemChecked(i, true);
				}
				else{	
					int iDBMValue = Integer.parseInt(sensorBitmaptoName.inverse().get(sensorNames[i]));	
					if( (iDBMValue & enabledSensors) >0){
						listView.setItemChecked(i, true);
					}
				}
			}
					
			listView.setOnItemClickListener(new OnItemClickListener(){
				
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1, int clickIndex,
						long arg3) {
									
					if(mService.getShimmerVersion(mBluetoothAddress)==ShimmerVerDetails.HW_ID.SHIMMER_3 && sensorNames[clickIndex].equals("ECG")){
						int iDBMValue1 = Integer.parseInt(sensorBitmaptoName.inverse().get("EXG1"));
						int iDBMValue3 = Integer.parseInt(sensorBitmaptoName.inverse().get("EXG2"));
						if (!((dialogEnabledSensors & Shimmer.SENSOR_EXG1_24BIT)>0 && (dialogEnabledSensors & Shimmer.SENSOR_EXG2_24BIT)>0)){
							dialogEnabledSensors = mService.sensorConflictCheckandCorrection(mBluetoothAddress, dialogEnabledSensors,iDBMValue1);
							dialogEnabledSensors = mService.sensorConflictCheckandCorrection(mBluetoothAddress, dialogEnabledSensors,iDBMValue3);
						}
						
						if(!listView.isItemChecked(clickIndex)){
							listView.setItemChecked(clickIndex, false); //ECG
							dialogEnabledSensors = mService.sensorConflictCheckandCorrection(mBluetoothAddress, dialogEnabledSensors,iDBMValue1);
							dialogEnabledSensors = mService.sensorConflictCheckandCorrection(mBluetoothAddress, dialogEnabledSensors,iDBMValue3);
						}
						else
							listView.setItemChecked(clickIndex, true); //ECG
						
						listView.setItemChecked(clickIndex+1, false); //ECG 16Bit
						listView.setItemChecked(clickIndex+2, false);// EMG
						listView.setItemChecked(clickIndex+3, false);// EMG 16Bit
						listView.setItemChecked(clickIndex+4, false);// Test Signal
						listView.setItemChecked(clickIndex+5, false);// Test Signal 16Bit
						if(listView.isItemChecked(clickIndex))
							mService.writeEXGSetting(mBluetoothAddress, 0);
					} 
					else if(mService.getShimmerVersion(mBluetoothAddress)==ShimmerVerDetails.HW_ID.SHIMMER_3 && sensorNames[clickIndex].equals("ECG 16Bit")){
							int iDBMValue1 = Integer.parseInt(sensorBitmaptoName.inverse().get("EXG1 16Bit"));
							int iDBMValue3 = Integer.parseInt(sensorBitmaptoName.inverse().get("EXG2 16Bit"));
							if (!((dialogEnabledSensors & Shimmer.SENSOR_EXG1_16BIT)>0 && (dialogEnabledSensors & Shimmer.SENSOR_EXG2_16BIT)>0)){
								dialogEnabledSensors = mService.sensorConflictCheckandCorrection(mBluetoothAddress, dialogEnabledSensors,iDBMValue1);
								dialogEnabledSensors = mService.sensorConflictCheckandCorrection(mBluetoothAddress, dialogEnabledSensors,iDBMValue3);
							}
							
							if(!listView.isItemChecked(clickIndex)){
								listView.setItemChecked(clickIndex, false); //ECG 16Bit
								dialogEnabledSensors = mService.sensorConflictCheckandCorrection(mBluetoothAddress, dialogEnabledSensors,iDBMValue1);
								dialogEnabledSensors = mService.sensorConflictCheckandCorrection(mBluetoothAddress, dialogEnabledSensors,iDBMValue3);
							}
							else
								listView.setItemChecked(clickIndex, true); //ECG 16Bit
							
							listView.setItemChecked(clickIndex-1, false); //ECG
							listView.setItemChecked(clickIndex+1, false);// EMG
							listView.setItemChecked(clickIndex+2, false);// EMG 16Bit
							listView.setItemChecked(clickIndex+3, false);// Test Signal
							listView.setItemChecked(clickIndex+4, false);// Test Signal 16Bit
							if(listView.isItemChecked(clickIndex))
								mService.writeEXGSetting(mBluetoothAddress, 0);
					}
					else if(mService.getShimmerVersion(mBluetoothAddress)==ShimmerVerDetails.HW_ID.SHIMMER_3 && sensorNames[clickIndex].equals("EMG")){
						int iDBMValue1 = Integer.parseInt(sensorBitmaptoName.inverse().get("EXG1"));
						int iDBMValue3 = Integer.parseInt(sensorBitmaptoName.inverse().get("EXG2"));
						if (!((dialogEnabledSensors & Shimmer.SENSOR_EXG1_24BIT)>0 && (dialogEnabledSensors & Shimmer.SENSOR_EXG2_24BIT)>0)){
							dialogEnabledSensors = mService.sensorConflictCheckandCorrection(mBluetoothAddress, dialogEnabledSensors,iDBMValue1);
							dialogEnabledSensors = mService.sensorConflictCheckandCorrection(mBluetoothAddress, dialogEnabledSensors,iDBMValue3);
						}
						
						if(!listView.isItemChecked(clickIndex)){
							listView.setItemChecked(clickIndex, false); //EMG
							dialogEnabledSensors = mService.sensorConflictCheckandCorrection(mBluetoothAddress, dialogEnabledSensors,iDBMValue1);
							dialogEnabledSensors = mService.sensorConflictCheckandCorrection(mBluetoothAddress, dialogEnabledSensors,iDBMValue3);
						}
						else
							listView.setItemChecked(clickIndex, true); //EMG
						
						listView.setItemChecked(clickIndex-2, false); //ECG
						listView.setItemChecked(clickIndex-1, false);// ECG 16Bit
						listView.setItemChecked(clickIndex+1, false);// EMG 16Bit
						listView.setItemChecked(clickIndex+2, false);// Test Signal
						listView.setItemChecked(clickIndex+3, false);// Test Signal 16Bit
						if(listView.isItemChecked(clickIndex))
							mService.writeEXGSetting(mBluetoothAddress, 1);
					}
					else if(mService.getShimmerVersion(mBluetoothAddress)==ShimmerVerDetails.HW_ID.SHIMMER_3 && sensorNames[clickIndex].equals("EMG 16Bit")){
						int iDBMValue1 = Integer.parseInt(sensorBitmaptoName.inverse().get("EXG1 16Bit"));
						int iDBMValue3 = Integer.parseInt(sensorBitmaptoName.inverse().get("EXG2 16Bit"));
						if (!((dialogEnabledSensors & Shimmer.SENSOR_EXG1_16BIT)>0 && (dialogEnabledSensors & Shimmer.SENSOR_EXG2_16BIT)>0)){
							dialogEnabledSensors = mService.sensorConflictCheckandCorrection(mBluetoothAddress, dialogEnabledSensors,iDBMValue1);
							dialogEnabledSensors = mService.sensorConflictCheckandCorrection(mBluetoothAddress, dialogEnabledSensors,iDBMValue3);
						}
						
						if(!listView.isItemChecked(clickIndex)){
							listView.setItemChecked(clickIndex, false); //EMG 16Bit
							dialogEnabledSensors = mService.sensorConflictCheckandCorrection(mBluetoothAddress, dialogEnabledSensors,iDBMValue1);
							dialogEnabledSensors = mService.sensorConflictCheckandCorrection(mBluetoothAddress, dialogEnabledSensors,iDBMValue3);
						}
						else
							listView.setItemChecked(clickIndex, true); //EMG 16Bit
						
						listView.setItemChecked(clickIndex-3, false); //ECG
						listView.setItemChecked(clickIndex-2, false);// ECG 16Bit
						listView.setItemChecked(clickIndex-1, false);// EMG 
						listView.setItemChecked(clickIndex+1, false);// Test Signal
						listView.setItemChecked(clickIndex+2, false);// Test Signal 16Bit
						if(listView.isItemChecked(clickIndex))
							mService.writeEXGSetting(mBluetoothAddress, 1);
					}
					else if(mService.getShimmerVersion(mBluetoothAddress)==ShimmerVerDetails.HW_ID.SHIMMER_3 && sensorNames[clickIndex].equals("Test Signal")){
						int iDBMValue1 = Integer.parseInt(sensorBitmaptoName.inverse().get("EXG1"));
						int iDBMValue3 = Integer.parseInt(sensorBitmaptoName.inverse().get("EXG2"));
						if (!((dialogEnabledSensors & Shimmer.SENSOR_EXG1_24BIT)>0 && (dialogEnabledSensors & Shimmer.SENSOR_EXG2_24BIT)>0)){
							dialogEnabledSensors = mService.sensorConflictCheckandCorrection(mBluetoothAddress, dialogEnabledSensors,iDBMValue1);
							dialogEnabledSensors = mService.sensorConflictCheckandCorrection(mBluetoothAddress, dialogEnabledSensors,iDBMValue3);
						}
						
						if(!listView.isItemChecked(clickIndex)){
							listView.setItemChecked(clickIndex, false); //Test Signal
							dialogEnabledSensors = mService.sensorConflictCheckandCorrection(mBluetoothAddress, dialogEnabledSensors,iDBMValue1);
							dialogEnabledSensors = mService.sensorConflictCheckandCorrection(mBluetoothAddress, dialogEnabledSensors,iDBMValue3);
						}
						else
							listView.setItemChecked(clickIndex, true); //Test Signal
						
						listView.setItemChecked(clickIndex-4, false); //ECG
						listView.setItemChecked(clickIndex-3, false);// ECG 16Bit
						listView.setItemChecked(clickIndex-2, false);// EMG 
						listView.setItemChecked(clickIndex-1, false);// EMG 16Bit
						listView.setItemChecked(clickIndex+1, false);// Test Signal 16Bit
						if(listView.isItemChecked(clickIndex))
						mService.writeEXGSetting(mBluetoothAddress, 2);
					}
					else if(mService.getShimmerVersion(mBluetoothAddress)==ShimmerVerDetails.HW_ID.SHIMMER_3 && sensorNames[clickIndex].equals("Test Signal 16Bit")){
						int iDBMValue1 = Integer.parseInt(sensorBitmaptoName.inverse().get("EXG1 16Bit"));
						int iDBMValue3 = Integer.parseInt(sensorBitmaptoName.inverse().get("EXG2 16Bit"));
						if (!((dialogEnabledSensors & Shimmer.SENSOR_EXG1_16BIT)>0 && (dialogEnabledSensors & Shimmer.SENSOR_EXG2_16BIT)>0)){
							dialogEnabledSensors = mService.sensorConflictCheckandCorrection(mBluetoothAddress, dialogEnabledSensors,iDBMValue1);
							dialogEnabledSensors = mService.sensorConflictCheckandCorrection(mBluetoothAddress, dialogEnabledSensors,iDBMValue3);
						}
							
						if(!listView.isItemChecked(clickIndex)){
							listView.setItemChecked(clickIndex, false); //Test Signal 16Bit
							dialogEnabledSensors = mService.sensorConflictCheckandCorrection(mBluetoothAddress, dialogEnabledSensors,iDBMValue1);
							dialogEnabledSensors = mService.sensorConflictCheckandCorrection(mBluetoothAddress, dialogEnabledSensors,iDBMValue3);
						}
						else
							listView.setItemChecked(clickIndex, true); //Test Signal 16Bit
							
						listView.setItemChecked(clickIndex-5, false); //ECG
						listView.setItemChecked(clickIndex-4, false);// ECG 16Bit
						listView.setItemChecked(clickIndex-3, false);// EMG 
						listView.setItemChecked(clickIndex-2, false);// EMG 16Bit
						listView.setItemChecked(clickIndex-1, false);// Test Signal
						if(listView.isItemChecked(clickIndex))
							mService.writeEXGSetting(mBluetoothAddress, 2);
					}
					else{
						int sensorIdentifier = Integer.parseInt(sensorBitmaptoName.inverse().get(sensorNames[clickIndex]));
						//check and remove any old daughter boards (sensors) which will cause a conflict with sensorIdentifier 
						dialogEnabledSensors = mService.sensorConflictCheckandCorrection(mBluetoothAddress,dialogEnabledSensors,sensorIdentifier);
						//update the checkbox accordingly
						int end=0;
						if(mService.getShimmerVersion(mBluetoothAddress)==ShimmerVerDetails.HW_ID.SHIMMER_3)
							end=sensorNames.length-6;
						else
							end=sensorNames.length;
						
						for (int i=0;i<end;i++){
							int iDBMValue = Integer.parseInt(sensorBitmaptoName.inverse().get(sensorNames[i]));	
							if( (iDBMValue & dialogEnabledSensors) >0){
								listView.setItemChecked(i, true);
							} else {
								listView.setItemChecked(i, false);
							}
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
					menuListViewDialog.show();
				}});
			
			
			mDialog.show();
	 		
		}
}
