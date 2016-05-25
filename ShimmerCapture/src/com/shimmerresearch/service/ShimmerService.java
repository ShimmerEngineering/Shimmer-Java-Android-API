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

package com.shimmerresearch.service;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;














import com.shimmerresearch.algorithms.Filter;
import com.shimmerresearch.android.Shimmer;
import com.shimmerresearch.biophysicalprocessing.ECGtoHRAdaptive;
import com.shimmerresearch.biophysicalprocessing.ECGtoHRAlgorithm;
import com.shimmerresearch.biophysicalprocessing.PPGtoHRAlgorithm;
import com.shimmerresearch.bluetooth.ShimmerBluetooth.BT_STATE;
import com.shimmerresearch.driver.*;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_TYPE;
import com.shimmerresearch.tools.Logging;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class ShimmerService extends Service {
	private static final String TAG = "MyService";
    public Shimmer shimmerDevice1 = null;
    public Logging shimmerLog1 = null;
    private boolean mEnableLogging=false;
	private BluetoothAdapter mBluetoothAdapter = null;
	private final IBinder mBinder = new LocalBinder();
	public HashMap<String, Object> mMultiShimmer = new HashMap<String, Object>(7);
	public HashMap<String, Logging> mLogShimmer = new HashMap<String, Logging>(7);
	private Handler mHandlerGraph=null;
	private boolean mGraphing=false;
	public String mLogFileName="Default";
	Filter mFilter;
	Filter mLPFilterECG;
	Filter mHPFilterECG;
	private double[] mLPFcECG = {51.2};
	private double[] mHPFcECG = {0.5};
	PPGtoHRAlgorithm mPPGtoHR;
	ECGtoHRAdaptive mECGtoHR;
	private int mNumberOfBeatsToAvg = 2;
	private int mNumberOfBeatsToAvgECG = 2;
	private int mECGTrainingInterval = 10;
	double[] mLPFc = {5};
	private boolean mPPGtoHREnabled = false;
	private boolean mECGtoHREnabled = false;
	private boolean mConvertGSRtoSiemens = true;
	private String mPPGtoHRSignalName = Configuration.Shimmer3.ObjectClusterSensorName.INT_EXP_ADC_A13;
	private String mECGtoHRSignalName = Configuration.Shimmer3.ObjectClusterSensorName.ECG_LA_RA_24BIT;
	
	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
	
	public boolean isGSRtoSiemensEnabled(){
		return mConvertGSRtoSiemens;
	}
	
	@Override
	public void onCreate() {
		Toast.makeText(this, "My Service Created", Toast.LENGTH_LONG).show();
		Log.d(TAG, "onCreate");
	}

	public class LocalBinder extends Binder {
        public ShimmerService getService() {
            // Return this instance of LocalService so clients can call public methods
            return ShimmerService.this;
        }
    }
	
	public void setPPGtoHRSignal(String ppgtoHR){
		mPPGtoHRSignalName = ppgtoHR;
	}
	
	public void setECGtoHRSignal(String ecgtoHR){
		mECGtoHRSignalName = ecgtoHR;
	}
	
	@Override
	public void onDestroy() {
		Toast.makeText(this, "My Service Stopped", Toast.LENGTH_LONG).show();
		Log.d(TAG, "onDestroy");
		Collection<Object> colS=mMultiShimmer.values();
		Iterator<Object> iterator = colS.iterator();
		while (iterator.hasNext()) {
			Shimmer stemp=(Shimmer) iterator.next();
			stemp.stop();
		}
		
	}
	
	public void disconnectAllDevices(){
		Collection<Object> colS=mMultiShimmer.values();
		Iterator<Object> iterator = colS.iterator();
		while (iterator.hasNext()) {
			Shimmer stemp=(Shimmer) iterator.next();
			stemp.stop();
		}
		//mMultiShimmer.clear();
		mLogShimmer.clear();
	}
	
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("LocalService", "Received start id " + startId + ": " + intent);
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }
	
	@Override
	public void onStart(Intent intent, int startid) {
		Toast.makeText(this, "My Service Started", Toast.LENGTH_LONG).show();

		Log.d(TAG, "onStart");

	}
	
	public void enablePPGtoHR(String bluetoothAddress, boolean enable){
		if (enable){
			double sR = getSamplingRate(bluetoothAddress);
			mPPGtoHR = new PPGtoHRAlgorithm(sR, mNumberOfBeatsToAvg, true);
			try {
				mFilter = new Filter(Filter.LOW_PASS, sR,mLPFc);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		mPPGtoHREnabled = enable; 
	}
	
	public void enableECGtoHR(String bluetoothAddress, boolean enable){
		if (enable){
			double sR = getSamplingRate(bluetoothAddress);
			mECGtoHR = new ECGtoHRAdaptive(sR);
	    	try {
				mLPFilterECG = new Filter(Filter.LOW_PASS, sR, mLPFcECG);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	
	    	try {
				mHPFilterECG = new Filter(Filter.HIGH_PASS, sR, mHPFcECG);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		mECGtoHREnabled = enable; 
	}
	
	public boolean isPPGtoHREnabled(){
		return mPPGtoHREnabled;
	}
	
	public boolean isECGtoHREnabled(){
		return mECGtoHREnabled;
	}
	
	public void connectShimmer(String bluetoothAddress,String selectedDevice){
		Log.d("Shimmer","net Connection");
		Shimmer shimmerDevice=new Shimmer(this, mHandler,selectedDevice,true);
		mMultiShimmer.remove(bluetoothAddress);
		if (mMultiShimmer.get(bluetoothAddress)==null){
			mMultiShimmer.put(bluetoothAddress,shimmerDevice); 
			((Shimmer) mMultiShimmer.get(bluetoothAddress)).connect(bluetoothAddress,"default");
		}
	}
	
	public void onStop(){
		Toast.makeText(this, "My Service Stopped", Toast.LENGTH_LONG).show();
		Log.d(TAG, "onDestroy");
		Collection<Object> colS=mMultiShimmer.values();
		Iterator<Object> iterator = colS.iterator();
		while (iterator.hasNext()) {
			Shimmer stemp=(Shimmer) iterator.next();
			stemp.stop();
		}
	}
	
	public void toggleAllLEDS(){
		Collection<Object> colS=mMultiShimmer.values();
		Iterator<Object> iterator = colS.iterator();
		while (iterator.hasNext()) {
			Shimmer stemp=(Shimmer) iterator.next();
			if (stemp.getBluetoothRadioState()==BT_STATE.CONNECTED || stemp.getBluetoothRadioState()==BT_STATE.SDLOGGING){
				stemp.toggleLed();
			}
		}
	}
	
	
	  public final Handler mHandler = new Handler() {
	        public void handleMessage(Message msg) {
	            switch (msg.what) { // handlers have a what identifier which is used to identify the type of msg
	            case Shimmer.MESSAGE_READ:
	            	if ((msg.obj instanceof ObjectCluster)){	// within each msg an object can be include, objectclusters are used to represent the data structure of the shimmer device
	            	    ObjectCluster objectCluster =  (ObjectCluster) msg.obj; 
	            	    
	            	    //Filter Signal
	            	    //PPG to HR
	            	    if (mPPGtoHREnabled){
	            	    	Collection<FormatCluster> dataFormats = objectCluster.getCollectionOfFormatClusters(mPPGtoHRSignalName);  // first retrieve all the possible formats for the current sensor device
	            			FormatCluster formatCluster = ((FormatCluster)ObjectCluster.returnFormatCluster(dataFormats,CHANNEL_TYPE.CAL.toString())); // retrieve the calibrated data
	            			if (formatCluster!=null){
	            				double ppgdata = formatCluster.mData;

	            				try {
	            					ppgdata = mFilter.filterData(ppgdata);
	            				} catch (Exception e) {
	            					// TODO Auto-generated catch block
	            					e.printStackTrace();
	            				}

	            				dataFormats = objectCluster.getCollectionOfFormatClusters(Configuration.Shimmer3.ObjectClusterSensorName.TIMESTAMP);  // first retrieve all the possible formats for the current sensor device
	            				formatCluster = ((FormatCluster)ObjectCluster.returnFormatCluster(dataFormats,CHANNEL_TYPE.CAL.toString())); // retrieve the calibrated data

	            				double calts = formatCluster.mData;

	            				double hr = mPPGtoHR.ppgToHrConversion(ppgdata, calts);
	            				System.out.print("Heart Rate: " + Integer.toString((int)hr) + "\n");
	            				objectCluster.addData(Configuration.Shimmer3.ObjectClusterSensorName.PPG_TO_HR,CHANNEL_TYPE.CAL.toString(),Configuration.CHANNEL_UNITS.BEATS_PER_MINUTE,hr);

	            			}
	            		
	            			
	            	    }
	            	  
	            	    //Filter Signal
	            	    //ECG to HR
	            	    if (mECGtoHREnabled){
	            	    	Collection<FormatCluster> dataFormats = objectCluster.getCollectionOfFormatClusters(mECGtoHRSignalName);  // first retrieve all the possible formats for the current sensor device
	            			FormatCluster formatCluster = ((FormatCluster)ObjectCluster.returnFormatCluster(dataFormats,CHANNEL_TYPE.CAL.toString())); // retrieve the calibrated data
	            			if (formatCluster!=null){
	            				double ecgdata = formatCluster.mData;

	            				try {
	            					ecgdata = mLPFilterECG.filterData(ecgdata);
	            					ecgdata = mHPFilterECG.filterData(ecgdata);
	            				} catch (Exception e) {
	            					// TODO Auto-generated catch block
	            					e.printStackTrace();
	            				}

	            				dataFormats = objectCluster.getCollectionOfFormatClusters(Configuration.Shimmer3.ObjectClusterSensorName.TIMESTAMP);  // first retrieve all the possible formats for the current sensor device
	            				formatCluster = ((FormatCluster)ObjectCluster.returnFormatCluster(dataFormats,CHANNEL_TYPE.CAL.toString())); // retrieve the calibrated data

	            				double calts = formatCluster.mData;

	            				double hr = mECGtoHR.ecgToHrConversion(ecgdata, calts);
	            				System.out.print("Heart Rate: " + Integer.toString((int)hr) + "\n");
	            				objectCluster.addData(Configuration.Shimmer3.ObjectClusterSensorName.ECG_TO_HR,CHANNEL_TYPE.CAL.toString(),Configuration.CHANNEL_UNITS.BEATS_PER_MINUTE,hr);
	            			}
	            	    }
	            	    
	            	    //PPG to HR
	            	    
	            		if (mConvertGSRtoSiemens){
            				Collection<FormatCluster> dataFormatsGSR = objectCluster.getCollectionOfFormatClusters(Configuration.Shimmer3.ObjectClusterSensorName.GSR);  // first retrieve all the possible formats for the current sensor device
	            			FormatCluster formatClusterGSR = ((FormatCluster)ObjectCluster.returnFormatCluster(dataFormatsGSR,CHANNEL_TYPE.CAL.toString())); // retrieve the calibrated data
	            			if (formatClusterGSR!=null){
	            				double gsrdata = formatClusterGSR.mData * 1000; //in ohms
	            				double conductance = 1/gsrdata;
	            				conductance = conductance * 1000000; //convert to microSiemens
	            				//objectCluster.mPropertyCluster.remove(Configuration.Shimmer3.ObjectClusterSensorName.GSR, formatClusterGSR);
	            				objectCluster.addData(Configuration.Shimmer3.ObjectClusterSensorName.GSR_CONDUCTANCE,CHANNEL_TYPE.CAL.toString(),"microSiemens",conductance);
	            			}
	            				
            			}
	            		
	            		objectCluster.removeAll(Configuration.Shimmer3.ObjectClusterSensorName.BATT_PERCENTAGE);
	            		objectCluster.removeAll(Configuration.Shimmer3.ObjectClusterSensorName.SYSTEM_TIMESTAMP_PLOT);
	            		objectCluster.removeAll(Configuration.Shimmer3.ObjectClusterSensorName.PACKET_RECEPTION_RATE_TRIAL);
	            		objectCluster.removeAll(Configuration.Shimmer3.ObjectClusterSensorName.PACKET_RECEPTION_RATE_CURRENT);
	            		objectCluster.removeAll(Configuration.Shimmer3.ObjectClusterSensorName.SYSTEM_TIMESTAMP);
	            		
	            	   if (mEnableLogging==true){
		            	   shimmerLog1= (Logging)mLogShimmer.get(objectCluster.getMacAddress());
		            	   if (shimmerLog1!=null){
		            		   shimmerLog1.logData(objectCluster);
		            	   } else {
		            			char[] bA=objectCluster.getMacAddress().toCharArray();
		            			Logging shimmerLog;
		            			if (mLogFileName.equals("Default")){
		            				shimmerLog=new Logging(fromMilisecToDate(System.currentTimeMillis()) + " Device" + bA[12] + bA[13] + bA[15] + bA[16],"\t", "ShimmerCapture");
		            			} else {
		            				shimmerLog=new Logging(fromMilisecToDate(System.currentTimeMillis()) + mLogFileName,"\t", "ShimmerCapture");
		            			}
		            			mLogShimmer.remove(objectCluster.getMacAddress());
		            			if (mLogShimmer.get(objectCluster.getMacAddress())==null){
		            				mLogShimmer.put(objectCluster.getMacAddress(),shimmerLog); 
		            			}
		            	   }
	            	   }
	            	   
	            	   if (mGraphing==true){
	            		  // Log.d("ShimmerGraph","Sending");
	            		   mHandlerGraph.obtainMessage(Shimmer.MESSAGE_READ, objectCluster)
               	        .sendToTarget();
	            	   } 
	            	}
	                break;
	                 case Shimmer.MESSAGE_TOAST:
	                	Log.d("toast",msg.getData().getString(Shimmer.TOAST));
	                	Toast.makeText(getApplicationContext(), msg.getData().getString(Shimmer.TOAST),
	                            Toast.LENGTH_SHORT).show();
	                	if (msg.getData().getString(Shimmer.TOAST).equals("Device connection was lost")){
	                		
	                	}
	                break;
	                 case Shimmer.MESSAGE_STATE_CHANGE:
	                	 Intent intent = new Intent("com.shimmerresearch.service.ShimmerService");
	                	 Log.d("ShimmerGraph","Sending");
	            		   mHandlerGraph.obtainMessage(Shimmer.MESSAGE_STATE_CHANGE, msg.arg1, -1, msg.obj).sendToTarget();
	            		   if(msg.arg1==Shimmer.MSG_STATE_STOP_STREAMING){
	            			     closeAndRemoveFile(((ObjectCluster)msg.obj).getMacAddress());
	            		   } else {
	            			   switch (((ObjectCluster)msg.obj).mState) {
	            			   case CONNECTED:
	            				   Log.d("Shimmer",((ObjectCluster) msg.obj).getMacAddress() + "  " + ((ObjectCluster) msg.obj).getShimmerName());

	            				   intent.putExtra("ShimmerBluetoothAddress", ((ObjectCluster) msg.obj).getMacAddress() );
	            				   intent.putExtra("ShimmerDeviceName", ((ObjectCluster) msg.obj).getShimmerName() );
	            				   intent.putExtra("ShimmerState",BT_STATE.CONNECTED);
	            				   sendBroadcast(intent);
	            				   break;
	            			   case CONNECTING:
	            				   intent.putExtra("ShimmerBluetoothAddress", ((ObjectCluster) msg.obj).getMacAddress() );
	            				   intent.putExtra("ShimmerDeviceName", ((ObjectCluster) msg.obj).getShimmerName() );
	            				   intent.putExtra("ShimmerState",BT_STATE.CONNECTING);	
	            				   break;
	            			   case STREAMING:
	            				   break;
	            			   case STREAMING_AND_SDLOGGING:
	            				   break;
	            			   case SDLOGGING:
	            				   Log.d("Shimmer",((ObjectCluster) msg.obj).getMacAddress() + "  " + ((ObjectCluster) msg.obj).getShimmerName());

	            				   intent.putExtra("ShimmerBluetoothAddress", ((ObjectCluster) msg.obj).getMacAddress() );
	            				   intent.putExtra("ShimmerDeviceName", ((ObjectCluster) msg.obj).getShimmerName() );
	            				   intent.putExtra("ShimmerState",BT_STATE.CONNECTED);
	            				   sendBroadcast(intent);
	            				   break;
	            			   case DISCONNECTED:
	            				   intent.putExtra("ShimmerBluetoothAddress", ((ObjectCluster) msg.obj).getMacAddress() );
	            				   intent.putExtra("ShimmerDeviceName", ((ObjectCluster) msg.obj).getShimmerName() );
	            				   intent.putExtra("ShimmerState",BT_STATE.DISCONNECTED);
	            				   sendBroadcast(intent);
	            				   break;
	            			   }

	            			   break;
	            		   }
                 case Shimmer.MESSAGE_STOP_STREAMING_COMPLETE:
                	 String address =  msg.getData().getString("Bluetooth Address");
                	 boolean stop  =  msg.getData().getBoolean("Stop Streaming");
                	 if (stop==true ){
                		 closeAndRemoveFile(address);
                	 }
                	break;
                 case Shimmer.MESSAGE_LOG_AND_STREAM_STATUS_CHANGED:
                 	mHandlerGraph.obtainMessage(Shimmer.MESSAGE_LOG_AND_STREAM_STATUS_CHANGED, msg.arg1, msg.arg2).sendToTarget();
                 	break;
	            }
	        }
	    };


    public void stopStreamingAllDevices() {
		// TODO Auto-generated method stub
		Collection<Object> colS=mMultiShimmer.values();
		Iterator<Object> iterator = colS.iterator();
		while (iterator.hasNext()) {
			Shimmer stemp=(Shimmer) iterator.next();
			
			if (stemp.getBluetoothRadioState()==BT_STATE.CONNECTED || stemp.getBluetoothRadioState()==BT_STATE.SDLOGGING){
				stemp.stopStreaming();
				
			}
		}
	}
	    
	public void startStreamingAllDevices() {
		// TODO Auto-generated method stub
		Collection<Object> colS=mMultiShimmer.values();
		Iterator<Object> iterator = colS.iterator();
		while (iterator.hasNext()) {
			Shimmer stemp=(Shimmer) iterator.next();
			if ((stemp.getBluetoothRadioState()==BT_STATE.CONNECTED || stemp.getBluetoothRadioState()==BT_STATE.SDLOGGING)){
				if (mPPGtoHREnabled){
					enablePPGtoHR(stemp.getBluetoothAddress(),true);	
				}
				if (mECGtoHREnabled){
					enableECGtoHR(stemp.getBluetoothAddress(),true);	
				}
				
				stemp.startStreaming();
			}
		}
	}
	
	public void setEnableLogging(boolean enableLogging){
		mEnableLogging=enableLogging;
		Log.d("Shimmer","Logging :" + Boolean.toString(mEnableLogging));
	}
	public boolean getEnableLogging(){
		return mEnableLogging;
	}
	public void setAllSampingRate(double samplingRate) {
		// TODO Auto-generated method stub
		Collection<Object> colS=mMultiShimmer.values();
		Iterator<Object> iterator = colS.iterator();
		while (iterator.hasNext()) {
			Shimmer stemp=(Shimmer) iterator.next();
			if ((stemp.getBluetoothRadioState()==BT_STATE.CONNECTED || stemp.getBluetoothRadioState()==BT_STATE.SDLOGGING)){
				stemp.writeShimmerAndSensorsSamplingRate(samplingRate);
				if (mPPGtoHREnabled){
					mPPGtoHR = new PPGtoHRAlgorithm(samplingRate, mNumberOfBeatsToAvg, true);
					try {
						mFilter = new Filter(Filter.LOW_PASS, samplingRate,mLPFc);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				if (mECGtoHREnabled){
					mECGtoHR = new ECGtoHRAdaptive(samplingRate);
					try {
						mLPFilterECG = new Filter(Filter.LOW_PASS, samplingRate, mLPFcECG);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					try {
						mHPFilterECG = new Filter(Filter.HIGH_PASS, samplingRate, mHPFcECG);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			}
		}
	}

	public void setAllAccelRange(int accelRange) {
		// TODO Auto-generated method stub
		Collection<Object> colS=mMultiShimmer.values();
		Iterator<Object> iterator = colS.iterator();
		while (iterator.hasNext()) {
			Shimmer stemp=(Shimmer) iterator.next();
			if ((stemp.getBluetoothRadioState()==BT_STATE.CONNECTED || stemp.getBluetoothRadioState()==BT_STATE.SDLOGGING)){
				stemp.writeAccelRange(accelRange);
			}
		}
	}

	public void setAllGSRRange(int gsrRange) {
		// TODO Auto-generated method stub
		Collection<Object> colS=mMultiShimmer.values();
		Iterator<Object> iterator = colS.iterator();
		while (iterator.hasNext()) {
			Shimmer stemp=(Shimmer) iterator.next();
			if ((stemp.getBluetoothRadioState()==BT_STATE.CONNECTED || stemp.getBluetoothRadioState()==BT_STATE.SDLOGGING)){
				stemp.writeGSRRange(gsrRange);
			}
		}
	}
	
	public void setAllEnabledSensors(int enabledSensors) {
		// TODO Auto-generated method stub
		Collection<Object> colS=mMultiShimmer.values();
		Iterator<Object> iterator = colS.iterator();
		while (iterator.hasNext()) {
			Shimmer stemp=(Shimmer) iterator.next();
			if ((stemp.getBluetoothRadioState()==BT_STATE.CONNECTED || stemp.getBluetoothRadioState()==BT_STATE.SDLOGGING)){
				stemp.writeEnabledSensors(enabledSensors);
			}
		}
	}
	
	
	public void setEnabledSensors(long enabledSensors,String bluetoothAddress) {
		// TODO Auto-generated method stub
		Collection<Object> colS=mMultiShimmer.values();
		Iterator<Object> iterator = colS.iterator();
		while (iterator.hasNext()) {
			Shimmer stemp=(Shimmer) iterator.next();
			if ((stemp.getBluetoothRadioState()==BT_STATE.CONNECTED || stemp.getBluetoothRadioState()==BT_STATE.SDLOGGING) && stemp.getBluetoothAddress().equals(bluetoothAddress)){
				
				if (((enabledSensors & Shimmer.SENSOR_EXG1_16BIT)>0 && (enabledSensors & Shimmer.SENSOR_EXG2_16BIT)>0)||((enabledSensors & Shimmer.SENSOR_EXG1_24BIT)>0 && (enabledSensors & Shimmer.SENSOR_EXG2_24BIT)>0)){
				
				} else {
					mECGtoHREnabled = false;
				}
				
				if (stemp.getInternalExpPower()==1 && (((enabledSensors & Shimmer.SENSOR_INT_ADC_A1)>0)||((enabledSensors & Shimmer.SENSOR_INT_ADC_A12)>0)|((enabledSensors & Shimmer.SENSOR_INT_ADC_A13)>0)||((enabledSensors & Shimmer.SENSOR_INT_ADC_A14)>0))){
					
				} else {
					mPPGtoHREnabled = false;
				}
				stemp.writeEnabledSensors(enabledSensors);
			}
		}
	}

	public void toggleLED(String bluetoothAddress) {
		// TODO Auto-generated method stub
		Collection<Object> colS=mMultiShimmer.values();
		Iterator<Object> iterator = colS.iterator();
		while (iterator.hasNext()) {
			Shimmer stemp=(Shimmer) iterator.next();
			if ((stemp.getBluetoothRadioState()==BT_STATE.CONNECTED || stemp.getBluetoothRadioState()==BT_STATE.SDLOGGING) && stemp.getBluetoothAddress().equals(bluetoothAddress)){
				stemp.toggleLed();
			}
		}
	}
	
	public void writePMux(String bluetoothAddress,int setBit) {
		// TODO Auto-generated method stub
		Collection<Object> colS=mMultiShimmer.values();
		Iterator<Object> iterator = colS.iterator();
		while (iterator.hasNext()) {
			Shimmer stemp=(Shimmer) iterator.next();
			if ((stemp.getBluetoothRadioState()==BT_STATE.CONNECTED || stemp.getBluetoothRadioState()==BT_STATE.SDLOGGING) && stemp.getBluetoothAddress().equals(bluetoothAddress)){
				stemp.writePMux(setBit);
			}
		}
	}
	
	public void write5VReg(String bluetoothAddress,int setBit) {
		// TODO Auto-generated method stub
		Collection<Object> colS=mMultiShimmer.values();
		Iterator<Object> iterator = colS.iterator();
		while (iterator.hasNext()) {
			Shimmer stemp=(Shimmer) iterator.next();
			if ((stemp.getBluetoothRadioState()==BT_STATE.CONNECTED || stemp.getBluetoothRadioState()==BT_STATE.SDLOGGING) && stemp.getBluetoothAddress().equals(bluetoothAddress)){
				stemp.writeFiveVoltReg(setBit);
			}
		}
	}
	

	
	
	public long getEnabledSensors(String bluetoothAddress) {
		// TODO Auto-generated method stub
		Collection<Object> colS=mMultiShimmer.values();
		Iterator<Object> iterator = colS.iterator();
		long enabledSensors=0;
		while (iterator.hasNext()) {
			Shimmer stemp=(Shimmer) iterator.next();
			if ((stemp.getBluetoothRadioState()!=BT_STATE.DISCONNECTED)){
				enabledSensors = stemp.getEnabledSensors();
			}
		}
		return enabledSensors;
	}
	
	
	public void writeSamplingRate(String bluetoothAddress,double samplingRate) {
		// TODO Auto-generated method stub
		Collection<Object> colS=mMultiShimmer.values();
		Iterator<Object> iterator = colS.iterator();
		while (iterator.hasNext()) {
			Shimmer stemp=(Shimmer) iterator.next();
			if ((stemp.getBluetoothRadioState()==BT_STATE.CONNECTED || stemp.getBluetoothRadioState()==BT_STATE.SDLOGGING) && stemp.getBluetoothAddress().equals(bluetoothAddress)){
				stemp.writeShimmerAndSensorsSamplingRate(samplingRate);
				if (mPPGtoHREnabled){
					mPPGtoHR = new PPGtoHRAlgorithm(samplingRate, mNumberOfBeatsToAvg, true);
					try {
						mFilter = new Filter(Filter.LOW_PASS, samplingRate,mLPFc);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				if (mECGtoHREnabled){
					mECGtoHR = new ECGtoHRAdaptive(samplingRate);
					try {
						mLPFilterECG = new Filter(Filter.LOW_PASS, samplingRate, mLPFcECG);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					try {
						mHPFilterECG = new Filter(Filter.HIGH_PASS, samplingRate, mHPFcECG);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	public void writeAccelRange(String bluetoothAddress,int accelRange) {
		Collection<Object> colS=mMultiShimmer.values();
		Iterator<Object> iterator = colS.iterator();
		while (iterator.hasNext()) {
			Shimmer stemp=(Shimmer) iterator.next();
			if ((stemp.getBluetoothRadioState()==BT_STATE.CONNECTED || stemp.getBluetoothRadioState()==BT_STATE.SDLOGGING) && stemp.getBluetoothAddress().equals(bluetoothAddress)){
				stemp.writeAccelRange(accelRange);
			}
		}
	}
	
	public void writeGyroRange(String bluetoothAddress,int range) {
		Collection<Object> colS=mMultiShimmer.values();
		Iterator<Object> iterator = colS.iterator();
		while (iterator.hasNext()) {
			Shimmer stemp=(Shimmer) iterator.next();
			if ((stemp.getBluetoothRadioState()==BT_STATE.CONNECTED || stemp.getBluetoothRadioState()==BT_STATE.SDLOGGING) && stemp.getBluetoothAddress().equals(bluetoothAddress)){
				stemp.writeGyroRange(range);
			}
		}
	}

	public void writePressureResolution(String bluetoothAddress,int resolution) {
		Collection<Object> colS=mMultiShimmer.values();
		Iterator<Object> iterator = colS.iterator();
		while (iterator.hasNext()) {
			Shimmer stemp=(Shimmer) iterator.next();
			if ((stemp.getBluetoothRadioState()==BT_STATE.CONNECTED || stemp.getBluetoothRadioState()==BT_STATE.SDLOGGING) && stemp.getBluetoothAddress().equals(bluetoothAddress)){
				//currently not supported
				stemp.writePressureResolution(resolution);
			}
		}
	}
	
	public void writeMagRange(String bluetoothAddress,int range) {
		Collection<Object> colS=mMultiShimmer.values();
		Iterator<Object> iterator = colS.iterator();
		while (iterator.hasNext()) {
			Shimmer stemp=(Shimmer) iterator.next();
			if ((stemp.getBluetoothRadioState()==BT_STATE.CONNECTED || stemp.getBluetoothRadioState()==BT_STATE.SDLOGGING) && stemp.getBluetoothAddress().equals(bluetoothAddress)){
				stemp.writeMagRange(range);
			}
		}
	}
	
	public void writeGSRRange(String bluetoothAddress,int gsrRange) {
		Collection<Object> colS=mMultiShimmer.values();
		Iterator<Object> iterator = colS.iterator();
		while (iterator.hasNext()) {
			Shimmer stemp=(Shimmer) iterator.next();
			if ((stemp.getBluetoothRadioState()==BT_STATE.CONNECTED || stemp.getBluetoothRadioState()==BT_STATE.SDLOGGING) && stemp.getBluetoothAddress().equals(bluetoothAddress)){
				stemp.writeGSRRange(gsrRange);
			}
		}
	}
	
	
	public double getSamplingRate(String bluetoothAddress) {
		// TODO Auto-generated method stub
		
		Collection<Object> colS=mMultiShimmer.values();
		Iterator<Object> iterator = colS.iterator();
		double SRate=-1;
		while (iterator.hasNext()) {
			Shimmer stemp=(Shimmer) iterator.next();
			if (stemp.getBluetoothAddress().equals(bluetoothAddress)){
				SRate= stemp.getSamplingRateShimmer();
			}
		}
		return SRate;
	}

	public int getAccelRange(String bluetoothAddress) {
		// TODO Auto-generated method stub
		Collection<Object> colS=mMultiShimmer.values();
		Iterator<Object> iterator = colS.iterator();
		int aRange=-1;
		while (iterator.hasNext()) {
			Shimmer stemp=(Shimmer) iterator.next();
			if (stemp.getBluetoothAddress().equals(bluetoothAddress)){
				aRange = stemp.getAccelRange();
			}
		}
		return aRange;
	}

	public BT_STATE getShimmerState(String bluetoothAddress){

		// TODO Auto-generated method stub
		Collection<Object> colS=mMultiShimmer.values();
		Iterator<Object> iterator = colS.iterator();
		BT_STATE status=BT_STATE.DISCONNECTED;
		while (iterator.hasNext()) {
			Shimmer stemp=(Shimmer) iterator.next();
			if (stemp.getBluetoothAddress().equals(bluetoothAddress)){
				status = stemp.getBluetoothRadioState();
				
			}
		}
		return status;
	
	}
	
	public int getGSRRange(String bluetoothAddress) {
		// TODO Auto-generated method stub
		Collection<Object> colS=mMultiShimmer.values();
		Iterator<Object> iterator = colS.iterator();
		int gRange=-1;
		while (iterator.hasNext()) {
			Shimmer stemp=(Shimmer) iterator.next();
			if (stemp.getBluetoothAddress().equals(bluetoothAddress)){
				gRange = stemp.getGSRRange();
			}
		}
		return gRange;
	}

	public int get5VReg(String bluetoothAddress) {
		// TODO Auto-generated method stub
		Collection<Object> colS=mMultiShimmer.values();
		Iterator<Object> iterator = colS.iterator();
		int fiveVReg=-1;
		while (iterator.hasNext()) {
			Shimmer stemp=(Shimmer) iterator.next();
			if (stemp.getBluetoothAddress().equals(bluetoothAddress)){
				fiveVReg = stemp.get5VReg();
			}
		}
		return fiveVReg;
	}
	
	public boolean isLowPowerMagEnabled(String bluetoothAddress) {
		// TODO Auto-generated method stub
		Collection<Object> colS=mMultiShimmer.values();
		Iterator<Object> iterator = colS.iterator();
		boolean enabled=false;
		while (iterator.hasNext()) {
			Shimmer stemp=(Shimmer) iterator.next();
			if (stemp.getBluetoothAddress().equals(bluetoothAddress)){
				enabled = stemp.isLowPowerMagEnabled();
			}
		}
		return enabled;
	}
	
	
	public int getpmux(String bluetoothAddress) {
		// TODO Auto-generated method stub
		Collection<Object> colS=mMultiShimmer.values();
		Iterator<Object> iterator = colS.iterator();
		int pmux=-1;
		while (iterator.hasNext()) {
			Shimmer stemp=(Shimmer) iterator.next();
			if (stemp.getBluetoothAddress().equals(bluetoothAddress)){
				pmux = stemp.getPMux();
			}
		}
		return pmux;
	}
	
	
	public void startStreaming(String bluetoothAddress) {
		// TODO Auto-generated method stub
				Collection<Object> colS=mMultiShimmer.values();
				Iterator<Object> iterator = colS.iterator();
				while (iterator.hasNext()) {
					Shimmer stemp=(Shimmer) iterator.next();
					if ((stemp.getBluetoothRadioState()==BT_STATE.CONNECTED || stemp.getBluetoothRadioState()==BT_STATE.SDLOGGING) && stemp.getBluetoothAddress().equals(bluetoothAddress)){
						if (mPPGtoHREnabled){
							enablePPGtoHR(stemp.getBluetoothAddress(),true);	
						}
						if (mECGtoHREnabled){
							enableECGtoHR(stemp.getBluetoothAddress(),true);	
						}
						stemp.startStreaming();
					}
				}
	}
	
	public void startLogging(String bluetoothAddress) {
		// TODO Auto-generated method stub
				Collection<Object> colS=mMultiShimmer.values();
				Iterator<Object> iterator = colS.iterator();
				while (iterator.hasNext()) {
					Shimmer stemp=(Shimmer) iterator.next();
					if ((stemp.getBluetoothRadioState()==BT_STATE.CONNECTED || stemp.getBluetoothRadioState()==BT_STATE.STREAMING) && stemp.getBluetoothAddress().equals(bluetoothAddress)){
						stemp.startSDLogging();
					}
				}
	}
	
	public void stopLogging(String bluetoothAddress) {
		// TODO Auto-generated method stub
				Collection<Object> colS=mMultiShimmer.values();
				Iterator<Object> iterator = colS.iterator();
				while (iterator.hasNext()) {
					Shimmer stemp=(Shimmer) iterator.next();
					if ((stemp.getBluetoothRadioState()==BT_STATE.STREAMING_AND_SDLOGGING || stemp.getBluetoothRadioState()==BT_STATE.SDLOGGING) && stemp.getBluetoothAddress().equals(bluetoothAddress)){
						stemp.stopSDLogging();
					}
				}
	}
	
	public void startLogAndStreaming(String bluetoothAddress) {
		// TODO Auto-generated method stub
				Collection<Object> colS=mMultiShimmer.values();
				Iterator<Object> iterator = colS.iterator();
				while (iterator.hasNext()) {
					Shimmer stemp=(Shimmer) iterator.next();
					if (stemp.getBluetoothAddress().equals(bluetoothAddress)){
						stemp.startDataLogAndStreaming();
					}
				}
	}

	public long sensorConflictCheckandCorrection(String bluetoothAddress, long enabledSensors, int sensorToCheck) {
		// TODO Auto-generated method stub
		long newSensorBitmap = 0;
		Collection<Object> colS=mMultiShimmer.values();
		Iterator<Object> iterator = colS.iterator();
		while (iterator.hasNext()) {
			Shimmer stemp=(Shimmer) iterator.next();
			if (stemp.getBluetoothAddress().equals(bluetoothAddress)){
				newSensorBitmap = stemp.sensorConflictCheckandCorrection(enabledSensors,sensorToCheck);
			}
		}
		return newSensorBitmap;
	}
	public List<String> getListofEnabledSensors(String bluetoothAddress) {
		// TODO Auto-generated method stub
		List<String> listofSensors = null;
		Collection<Object> colS=mMultiShimmer.values();
		Iterator<Object> iterator = colS.iterator();
		while (iterator.hasNext()) {
			Shimmer stemp=(Shimmer) iterator.next();
			if (stemp.getBluetoothAddress().equals(bluetoothAddress)){
				listofSensors = stemp.getListofEnabledSensors();
			}
		}
		return listofSensors;
	}
	

	
	
	public void stopStreaming(String bluetoothAddress) {
		// TODO Auto-generated method stub
				Collection<Object> colS=mMultiShimmer.values();
				Iterator<Object> iterator = colS.iterator();
				while (iterator.hasNext()) {
					Shimmer stemp=(Shimmer) iterator.next();
					if ((stemp.getBluetoothRadioState()==BT_STATE.STREAMING || stemp.getBluetoothRadioState()==BT_STATE.STREAMING_AND_SDLOGGING) && stemp.getBluetoothAddress().equals(bluetoothAddress)){
						stemp.stopStreaming();
					}
				}
	}
	
	public void setBlinkLEDCMD(String bluetoothAddress) {
		// TODO Auto-generated method stub
		Shimmer stemp=(Shimmer) mMultiShimmer.get(bluetoothAddress);
		if (stemp!=null){
			if ((stemp.getBluetoothRadioState()==BT_STATE.CONNECTED || stemp.getBluetoothRadioState()==BT_STATE.SDLOGGING) && stemp.getBluetoothAddress().equals(bluetoothAddress)){
				if (stemp.getCurrentLEDStatus()==0){
					stemp.writeLEDCommand(1);
				} else {
					stemp.writeLEDCommand(0);
				}
			}
		}
				
	}
	
	public void enableLowPowerMag(String bluetoothAddress,boolean enable) {
		// TODO Auto-generated method stub
		Shimmer stemp=(Shimmer) mMultiShimmer.get(bluetoothAddress);
		if (stemp!=null){
			if ((stemp.getBluetoothRadioState()==BT_STATE.CONNECTED || stemp.getBluetoothRadioState()==BT_STATE.SDLOGGING) && stemp.getBluetoothAddress().equals(bluetoothAddress)){
				stemp.enableLowPowerMag(enable);
			}
		}		
	}
	

	public void setBattLimitWarning(String bluetoothAddress, double limit) {
		// TODO Auto-generated method stub
		Shimmer stemp=(Shimmer) mMultiShimmer.get(bluetoothAddress);
		if (stemp!=null){
			if (stemp.getBluetoothAddress().equals(bluetoothAddress)){
				stemp.setBattLimitWarning(limit);
			}
		}		
	
	}
	
	
	public double getBattLimitWarning(String bluetoothAddress) {
		// TODO Auto-generated method stub
		double limit=-1;
		Shimmer stemp=(Shimmer) mMultiShimmer.get(bluetoothAddress);
		if (stemp!=null){
			if (stemp.getBluetoothAddress().equals(bluetoothAddress)){
				limit=stemp.getBattLimitWarning();
			}
		}		
		return limit;
	}

	
	public double getPacketReceptionRate(String bluetoothAddress) {
		// TODO Auto-generated method stub
		double rate=-1;
		Shimmer stemp=(Shimmer) mMultiShimmer.get(bluetoothAddress);
		if (stemp!=null){
			if (stemp.getBluetoothAddress().equals(bluetoothAddress)){
				rate=stemp.getPacketReceptionRate();
			}
		}		
		return rate;
	}
	
	
	public void disconnectShimmer(String bluetoothAddress){
		Collection<Object> colS=mMultiShimmer.values();
		Iterator<Object> iterator = colS.iterator();
		while (iterator.hasNext()) {
			Shimmer stemp=(Shimmer) iterator.next();
			if ((stemp.getBluetoothRadioState()==BT_STATE.CONNECTED || stemp.getBluetoothRadioState()==BT_STATE.SDLOGGING) && stemp.getBluetoothAddress().equals(bluetoothAddress)){
				stemp.stop();
				
			}
		}
		
		
		mLogShimmer.remove(bluetoothAddress);		
		mMultiShimmer.remove(bluetoothAddress);
		
	}
	
	public void setGraphHandler(Handler handler){
		mHandlerGraph=handler;
		
	}
	
	public void enableGraphingHandler(boolean setting){
		mGraphing=setting;
	}
	
	public boolean DevicesConnected(String bluetoothAddress){
		boolean deviceConnected=false;
		Collection<Object> colS=mMultiShimmer.values();
		Iterator<Object> iterator = colS.iterator();
		while (iterator.hasNext()) {
			Shimmer stemp=(Shimmer) iterator.next();
			if (stemp.getBluetoothRadioState()!=BT_STATE.DISCONNECTED && stemp.getBluetoothAddress().equals(bluetoothAddress)){
				deviceConnected=true;
			}
		}
		return deviceConnected;
	}
	
	public boolean DeviceIsLogging(String bluetoothAddress){
		boolean deviceLogging=false;
		Collection<Object> colS=mMultiShimmer.values();
		Iterator<Object> iterator = colS.iterator();
		while (iterator.hasNext()) {
			Shimmer stemp=(Shimmer) iterator.next();
			if ((stemp.mBluetoothRadioState == BT_STATE.SDLOGGING || stemp.mBluetoothRadioState == BT_STATE.STREAMING_AND_SDLOGGING) && stemp.getBluetoothAddress().equals(bluetoothAddress)){
				deviceLogging=true;
			}
		}
		return deviceLogging;
	}
	
	public boolean DeviceIsStreaming(String bluetoothAddress){
		boolean deviceStreaming=false;
		Collection<Object> colS=mMultiShimmer.values();
		Iterator<Object> iterator = colS.iterator();
		while (iterator.hasNext()) {
			Shimmer stemp=(Shimmer) iterator.next();
			if (stemp.getStreamingStatus() == true  && stemp.getBluetoothAddress().equals(bluetoothAddress)){
				deviceStreaming=true;
			}
		}
		return deviceStreaming;
	}
	
	public boolean GetInstructionStatus(String bluetoothAddress){
		boolean instructionStatus=false;
		Collection<Object> colS=mMultiShimmer.values();
		Iterator<Object> iterator = colS.iterator();
		while (iterator.hasNext()) {
			Shimmer stemp=(Shimmer) iterator.next();
			if (stemp.getBluetoothAddress().equals(bluetoothAddress)){
				instructionStatus=stemp.getInstructionStatus();
			}
		}
		return instructionStatus;
	}

	public void setLoggingName(String name){
		mLogFileName=name;
	}
	
	public void closeAndRemoveFile(String bluetoothAddress){
		if (mLogShimmer.get(bluetoothAddress)!=null){
			mLogShimmer.get(bluetoothAddress).closeFile();
			MediaScannerConnection.scanFile(this, new String[] { mLogShimmer.get(bluetoothAddress).getAbsoluteName() }, null, null);
			mLogShimmer.remove(bluetoothAddress);
			
		}
	}
	
	public String getFWVersion (String bluetoothAddress){
		String version="";
		Shimmer stemp=(Shimmer) mMultiShimmer.get(bluetoothAddress);
		if (stemp!=null){
			version=stemp.getFirmwareVersionMajor()+"."+stemp.getFirmwareVersionMinor();
		}
		return version;
	}
	
	public int getShimmerVersion (String bluetoothAddress){
		int version=0;
		Shimmer stemp=(Shimmer) mMultiShimmer.get(bluetoothAddress);
		if (stemp!=null){
			version=stemp.getShimmerVersion();
		}
		return version;
	}
	

	
	public Shimmer getShimmer(String bluetoothAddress){
		// TODO Auto-generated method stub
		Shimmer shimmer = null;
		Collection<Object> colS=mMultiShimmer.values();
		Iterator<Object> iterator = colS.iterator();
		while (iterator.hasNext()) {
			Shimmer stemp=(Shimmer) iterator.next();
			if (stemp.getBluetoothAddress().equals(bluetoothAddress)){
				return stemp;
			}
		}
		return shimmer;
	}
	
	public void test(){
		Log.d("ShimmerTest","Test");
	}
	
	
	public boolean isEXGUsingTestSignal24Configuration(String bluetoothAddress){
		
		Shimmer tmp = (Shimmer) mMultiShimmer.get(bluetoothAddress);
		return tmp.isEXGUsingTestSignal24Configuration();
	}
	
	public boolean isEXGUsingTestSignal16Configuration(String bluetoothAddress){
		
		Shimmer tmp = (Shimmer) mMultiShimmer.get(bluetoothAddress);
		return tmp.isEXGUsingTestSignal16Configuration();
	} 
	
	public boolean isEXGUsingECG24Configuration(String bluetoothAddress){
		
		Shimmer tmp = (Shimmer) mMultiShimmer.get(bluetoothAddress);
		return tmp.isEXGUsingECG24Configuration();
	}
	
	public boolean isEXGUsingECG16Configuration(String bluetoothAddress){
		
		Shimmer tmp = (Shimmer) mMultiShimmer.get(bluetoothAddress);
		return tmp.isEXGUsingECG16Configuration();
	}
	
	public boolean isEXGUsingEMG24Configuration(String bluetoothAddress){
		
		Shimmer tmp = (Shimmer) mMultiShimmer.get(bluetoothAddress);
		return tmp.isEXGUsingEMG24Configuration();
	}
	
	public boolean isEXGUsingEMG16Configuration(String bluetoothAddress){
		
		Shimmer tmp = (Shimmer) mMultiShimmer.get(bluetoothAddress);
		return tmp.isEXGUsingEMG16Configuration();
	}
	
	public void writeEXGSetting(String bluetoothAddress,int setting) {
		// TODO Auto-generated method stub
		Collection<Object> colS=mMultiShimmer.values();
		Iterator<Object> iterator = colS.iterator();
		while (iterator.hasNext()) {
			Shimmer stemp=(Shimmer) iterator.next();
			if ((stemp.getBluetoothRadioState()==BT_STATE.CONNECTED || stemp.getBluetoothRadioState()==BT_STATE.SDLOGGING) && stemp.getBluetoothAddress().equals(bluetoothAddress)){
				if (setting==0){
					stemp.enableDefaultECGConfiguration();
				} else if (setting==1){
					stemp.enableDefaultEMGConfiguration();
				} else if (setting==2){
					stemp.enableEXGTestSignal();
				
				}
				
			} else {
				
			}
		}
	}
	
	
	
	//convert the system time in miliseconds to a "readable" date format with the next format: YYYY MM DD HH MM SS
	private String fromMilisecToDate(long miliseconds){
		
		String date="";
		Date dateToParse = new Date(miliseconds);
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		date = dateFormat.format(dateToParse);
		
		return date;
	}
	
	public boolean isUsingLogAndStreamFW(String bluetoothAddress){
		
		boolean logAndStream = false;
		Shimmer stemp=(Shimmer) mMultiShimmer.get(bluetoothAddress);
		if (stemp!=null){
			if(stemp.getFirmwareIdentifier()==3)
				logAndStream = true;
		}
		return logAndStream;
		
	}
	
	public void readStatusLogAndStream(String bluetoothAddress){
		
		Shimmer stemp=(Shimmer) mMultiShimmer.get(bluetoothAddress);
		if (stemp!=null){
			if(stemp.getFirmwareIdentifier()==3)
				stemp.readStatusLogAndStream();
		}
	}
	
	public boolean isSensing(String bluetoothAddress){
		
		Shimmer stemp=(Shimmer) mMultiShimmer.get(bluetoothAddress);
		if (stemp!=null){
			if(stemp.getFirmwareIdentifier()==3)
				return stemp.isSensing();
		}
		
		return false;
	}
	
	public boolean isDocked(String bluetoothAddress){
		
		Shimmer stemp=(Shimmer) mMultiShimmer.get(bluetoothAddress);
		if (stemp!=null){
			if(stemp.getFirmwareIdentifier()==3)
				return stemp.isDocked();
		}
		
		return false;
	}
	
}
