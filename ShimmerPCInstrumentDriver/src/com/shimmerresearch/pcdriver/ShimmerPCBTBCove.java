/*Rev0.1
 * This implementation is based of ShimmerPCBT. Purpose is to demonstrate the use of the BlueCove Bluetooth library. BlueCove http://bluecove.org/
 * The blueooth address of the device should be specified as follows btspp://000666669686:1 , where 000666669686 is the Bluetooth address
 * Note that BlueCove has other useful Bluetooth functions such as Bluetooth Scanning
 * Also note that using BlueCove on linux will require downloading additional libraries bluecove-gpl and bluecove (need to replace the snapshot library) from their site http://sourceforge.net/projects/bluecove/files/BlueCove/2.1.0/
 * Note that the Linux library is licensed under a GPL license
 * 
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
 * @date   November, 2013
 * 
 */

package com.shimmerresearch.pcdriver;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;

import com.shimmerresearch.bluetooth.ProgressReportPerCmd;
import com.shimmerresearch.bluetooth.ProgressReportPerDevice;
import com.shimmerresearch.bluetooth.ShimmerBluetooth;
import com.shimmerresearch.bluetooth.ShimmerBluetooth.BT_STATE;
import com.shimmerresearch.bluetooth.ShimmerBluetooth.ProcessingThread;
import com.shimmerresearch.driver.Callable;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.ShimmerMsg;


public class ShimmerPCBTBCove extends ShimmerBluetooth implements Serializable{
			/**
	 * 
	 */
	private static final long serialVersionUID = -7067087273053149229L;
	// Used by the constructor when the user intends to write new settings to the Shimmer device after connection
	StreamConnection conn=null;
	ObjectCluster objectClusterTemp = null;
	//InputStream mIN;
	DataInputStream mIN;
	OutputStream mOUT;
	


	public static final int MSG_IDENTIFIER_STATE_CHANGE = 0;
	public static final int MSG_IDENTIFIER_NOTIFICATION_MESSAGE = 1; 
	public static final int MSG_IDENTIFIER_DATA_PACKET = 2;
	public static final int MSG_IDENTIFIER_PACKET_RECEPTION_RATE = 3;
	public static final int MSG_IDENTIFIER_PROGRESS_REPORT_PER_DEVICE = 4;
	public static final int MSG_IDENTIFIER_PROGRESS_REPORT_ALL = 5;
	public static final int MSG_IDENTIFIER_PACKET_RECEPTION_RATE_CURRENT = 6;
	
	public static final int NOTIFICATION_SHIMMER_STOP_STREAMING = 0;
	public static final int NOTIFICATION_SHIMMER_START_STREAMING = 1;
	public static final int NOTIFICATION_SHIMMER_FULLY_INITIALIZED = 2;
	public final static int NOTIFICATION_SHIMMER_STATE_CHANGE = 3;

	/**
	 * Constructor. Prepares a new Bluetooth session.
	 * @param context  The UI Activity Context
	 * @param handler  A Handler to send messages back to the UI Activity
	 * @param myname  To allow the user to set a unique identifier for each Shimmer device
	 * @param countiousSync A boolean value defining whether received packets should be checked continuously for the correct start and end of packet.
	 */
	public ShimmerPCBTBCove(String myName, Boolean continousSync) {
		mShimmerUserAssignedName=myName;
		mContinousSync=continousSync;
		mSetupDevice=false;
	}

	public ShimmerPCBTBCove( String myName, double samplingRate, int accelRange, int gsrRange, int setEnabledSensors, boolean continousSync) {
		mShimmerSamplingRate = samplingRate;
		mAccelRange = accelRange;
		mGSRRange = gsrRange;
		mSetEnabledSensors=setEnabledSensors;
		mShimmerUserAssignedName = myName;
		mSetupDevice = true;
		mContinousSync = continousSync;
	}
	
	/**
	 * Shimmer3 Constructor. Prepares a new Bluetooth session. Additional fields allows the device to be set up immediately.
	 * @param context  The UI Activity Context
	 * @param handler  A Handler to send messages back to the UI Activity
	 * @param myname  To allow the user to set a unique identifier for each Shimmer device
	 * @param samplingRate Defines the sampling rate
	 * @param accelRange Defines the Acceleration range. Valid range setting values for the Shimmer 2 are 0 (+/- 1.5g), 1 (+/- 2g), 2 (+/- 4g) and 3 (+/- 6g). Valid range setting values for the Shimmer 2r are 0 (+/- 1.5g) and 3 (+/- 6g).
	 * @param gsrRange Numeric value defining the desired gsr range. Valid range settings are 0 (10kOhm to 56kOhm),  1 (56kOhm to 220kOhm), 2 (220kOhm to 680kOhm), 3 (680kOhm to 4.7MOhm) and 4 (Auto Range).
	 * @param setEnabledSensors Defines the sensors to be enabled (e.g. 'Shimmer.SENSOR_ACCEL|Shimmer.SENSOR_GYRO' enables the Accelerometer and Gyroscope)
	 * @param countiousSync A boolean value defining whether received packets should be checked continuously for the correct start and end of packet.
	 */
	public ShimmerPCBTBCove(String myName, double samplingRate, int accelRange, int gsrRange, int setEnabledSensors, boolean continousSync, boolean enableLowPowerAccel, boolean enableLowPowerGyro, boolean enableLowPowerMag, int gyroRange, int magRange,byte[] exg1,byte[] exg2) {
		mState = BT_STATE.DISCONNECTED;
		mShimmerSamplingRate = samplingRate;
		mAccelRange = accelRange;
		mGSRRange = gsrRange;
		mSetEnabledSensors=setEnabledSensors;
		mShimmerUserAssignedName = myName;
		mSetupDevice = true;
		mContinousSync = continousSync;
		mLowPowerMag = enableLowPowerMag;
		mLowPowerAccelWR = enableLowPowerAccel;
		mLowPowerGyro = enableLowPowerGyro;
		mGyroRange = gyroRange;
		mMagRange = magRange;
		mSetupEXG = true;
		mEXG1RegisterArray = exg1;
		mEXG2RegisterArray = exg2;
	}
	
	/**
	 * Shimmer2, Constructor.Additional fields allows the device to be set up immediately. 
	 * @param myname  To allow the user to set a unique identifier for each Shimmer device
	 * @param samplingRate Defines the sampling rate
	 * @param accelRange Defines the Acceleration range. Valid range setting values for the Shimmer 2 are 0 (+/- 1.5g), 1 (+/- 2g), 2 (+/- 4g) and 3 (+/- 6g). Valid range setting values for the Shimmer 2r are 0 (+/- 1.5g) and 3 (+/- 6g).
	 * @param gsrRange Numeric value defining the desired gsr range. Valid range settings are 0 (10kOhm to 56kOhm),  1 (56kOhm to 220kOhm), 2 (220kOhm to 680kOhm), 3 (680kOhm to 4.7MOhm) and 4 (Auto Range).
	 * @param setEnabledSensors Defines the sensors to be enabled (e.g. 'Shimmer.SENSOR_ACCEL|Shimmer.SENSOR_GYRO' enables the Accelerometer and Gyroscope)
	 * @param countiousSync A boolean value defining whether received packets should be checked continuously for the correct start and end of packet.
	 */
	public ShimmerPCBTBCove(String myName, double samplingRate, int accelRange, int gsrRange, int setEnabledSensors, boolean continousSync, int magGain) {
		mState = BT_STATE.DISCONNECTED;
		mShimmerSamplingRate = samplingRate;
		mAccelRange = accelRange;
		mMagRange = magGain;
		mGSRRange = gsrRange;
		mSetEnabledSensors=setEnabledSensors;
		mShimmerUserAssignedName = myName;
		mSetupDevice = true;
		mContinousSync = continousSync;
	}
	
	/**
	 * Connect to device specified by address
	 * @param address  The Bluetooth address format is btspp://000666669686:1 , where 000666669686 is the Bluetooth address
	 * @param empty  This is for forward compatibility, in the event a choice of library is offered, any string value can be entered now ~ does nothing
	 */
	public synchronized void connect(final String address, String empty) {
		mIamAlive = false;
		if (conn==null){
		mMyBluetoothAddress = address;
		getListofInstructions().clear();
		mFirstTime=true;
		try {
			setState(BT_STATE.CONNECTING);
//			setState(BT_STATE.CONNECTING);
			conn = (StreamConnection)Connector.open(address);
			mIN = new DataInputStream(conn.openInputStream());
			mOUT = conn.openOutputStream();
			if (mIOThread != null) { 
				mIOThread = null;
				mPThread = null;
				}
			mIOThread = new IOThread();
			mIOThread.start();
			mPThread = new ProcessingThread();
			mPThread.start();
			initialize();
//			setState(BT_STATE.CONNECTED);
			setState(BT_STATE.CONNECTED);
		}
		catch ( IOException e ) { 
			System.err.print(e.toString()); 
			System.out.println("Connection Lost");
		}
			
		}
	}
	
	
	@Override
	public boolean bytesAvailableToBeRead() {
		// TODO Auto-generated method stub
		
			try {
				if (mIN.available()!=0){
					return true;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println("Connection Lost");
			}
		 
		return false;
	}
	
	public int availableBytes(){
		try {
			return mIN.available();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Connection Lost");
			connectionLost();
			e.printStackTrace();
			return 0;
		}
		
	}

	@Override
	public void writeBytes(byte[] data) {
		// TODO Auto-generated method stub
		try {
			mOUT.write(data);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Connection Lost");
			connectionLost();
			e.printStackTrace();
		}
	}

	@Override
	protected byte[] readBytes(int numberofBytes) {
		// TODO Auto-generated method stub
		byte[] b = new byte[numberofBytes];
		try {
			//mIN.read(b,0,numberofBytes);
			mIN.readFully(b,0,numberofBytes);
			return(b);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Connection Lost");
			e.printStackTrace();
		}
			
			
		return null;
	}
	
	@Override
	protected void stop() {
		// TODO Auto-generated method stub
		setState(BT_STATE.DISCONNECTED);
	}
	
	@Override
	protected void isNowStreaming() {
		// Send a notification msg to the UI through a callback (use a msg identifier notification message)
		// Do something here
		
		CallbackObject callBackObject = new CallbackObject(NOTIFICATION_SHIMMER_START_STREAMING, getBluetoothAddress(), mUniqueID);
		sendCallBackMsg(MSG_IDENTIFIER_NOTIFICATION_MESSAGE, callBackObject);
		
		if (mIsSDLogging){
			setState(BT_STATE.STREAMING_AND_SDLOGGING);
		} else {
			setState(BT_STATE.STREAMING);
		}
		
	}

	@Override
	protected byte readByte() {
		byte[] b = readBytes(1);
		return b[0];
	}

	@Override
	protected void inquiryDone() {
		CallbackObject callBackObject = new CallbackObject(NOTIFICATION_SHIMMER_STATE_CHANGE, mState, getBluetoothAddress(), mUniqueID);
		sendCallBackMsg(MSG_IDENTIFIER_STATE_CHANGE, callBackObject);
		isReadyForStreaming();
	}

	@Override
	protected void isReadyForStreaming() {
		// Send msg fully initialized, send notification message,  
		// Do something here
        mIsInitialised = true;
        sensorAndConfigMapsCreate();
        sensorMapUpdateFromEnabledSensorsVars();

        if (mSendProgressReport){
        	finishOperation(BT_STATE.CONNECTING);
        }
        
		CallbackObject callBackObject = new CallbackObject(NOTIFICATION_SHIMMER_FULLY_INITIALIZED, getBluetoothAddress(), mUniqueID);
		sendCallBackMsg(MSG_IDENTIFIER_NOTIFICATION_MESSAGE, callBackObject);
		if (mTimerCheckAlive==null && mTimerReadStatus==null && mTimerReadBattStatus==null){
        	//super.operationFinished();
			startTimerCheckIfAlive();
			startTimerReadStatus();
			startTimerReadBattStatus();
			
        }
		setState(BT_STATE.CONNECTED);
	}

	@Override
	protected void dataHandler(ObjectCluster ojc) {
		CallbackObject callBackObject = new CallbackObject(MSG_IDENTIFIER_PACKET_RECEPTION_RATE, getBluetoothAddress(), mUniqueID, getPacketReceptionRate());
		sendCallBackMsg(MSG_IDENTIFIER_PACKET_RECEPTION_RATE, callBackObject);
		
//		sendCallBackMsg(MSG_IDENTIFIER_PACKET_RECEPTION_RATE, getBluetoothAddress());
		sendCallBackMsg(MSG_IDENTIFIER_DATA_PACKET, ojc);
		}

	public byte[] returnRawData(){
		if (objectClusterTemp!=null){
			byte[] data= objectClusterTemp.mRawData;
			//objectClusterTemp = null;
			return data;
		
		}
		else 
			return null;
	}
	
	public synchronized void disconnect(){
		stopAllTimers();
		
		try {
			if (mIOThread != null) {
				mIOThread.stop = true;
				mIOThread = null;
				mPThread.stop = true;
				mPThread = null;
			}
			mIsStreaming = false;
			mIsInitialised = false;
			mIN.close();
			mOUT.close();
			conn.close();
			conn = null;
			setState(BT_STATE.DISCONNECTED);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			setState(BT_STATE.DISCONNECTED);
			System.out.println("Connection Lost");
			e.printStackTrace();
		}
		
	}

	@Override
	protected void sendStatusMsgPacketLossDetected() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void connectionLost() {
		System.out.println("Connection Lost");
		try {
			if (mIOThread != null) {
				mIOThread.stop = true;
				mIOThread = null;
				mPThread.stop = true;
				mPThread = null;
			}
			mIsStreaming = false;
			mIsInitialised = false;
			conn.close();
			conn = null;
			setState(BT_STATE.CONNECTION_LOST);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			setState(BT_STATE.CONNECTION_LOST);
			System.out.println("Connection Lost");
			e.printStackTrace();
		}	
		
	}

	@Override
	protected void sendStatusMSGtoUI(String msg) {
		// TODO Auto-generated method stub
		
	}

	
	public String message;
	@Override
	protected void printLogDataForDebugging(String msg) {
		// TODO Auto-generated method stub
		System.out.println(msg);
	}

	@Override
	protected void setState(BT_STATE state) {

		
		//TODO: below not needed any more?
//		if (state==STATE.NONE && mIsStreaming==true){
//			disconnect();
//		}
		mState = state;
		
//		if(mState==BT_STATE.CONNECTED){
//			mIsConnected = true;
//			mIsStreaming = false;
//		}
		if(mState==BT_STATE.CONNECTED){
			mIsInitialised = true;
			mIsStreaming = false;
		}
		else if(mState==BT_STATE.STREAMING){
			mIsStreaming = true;
		}		
		else if((mState==BT_STATE.DISCONNECTED)
				||(mState==BT_STATE.CONNECTION_LOST)
//				||(mState==BT_STATE.NONE)
				||(mState==BT_STATE.CONNECTION_FAILED)){
			mIsConnected = false;
			mIsStreaming = false;
			mIsInitialised = false;
		}
		
//		System.out.println("SetState: " + mUniqueID + "\tState:" + mState + "\tisConnected:" + mIsConnected + "\tisInitialised:" + mIsInitialised + "\tisStreaming:" + mIsStreaming);
		
		CallbackObject callBackObject = new CallbackObject(NOTIFICATION_SHIMMER_STATE_CHANGE, mState, getBluetoothAddress(), mUniqueID);
		sendCallBackMsg(MSG_IDENTIFIER_STATE_CHANGE, callBackObject);
	
	}
	
	@Override
	protected void hasStopStreaming() {
		// Send a notification msg to the UI through a callback (use a msg identifier notification message)
		// Do something here
		CallbackObject callBackObject = new CallbackObject(NOTIFICATION_SHIMMER_STOP_STREAMING, getBluetoothAddress(), mUniqueID);
		sendCallBackMsg(MSG_IDENTIFIER_NOTIFICATION_MESSAGE, callBackObject);
		startTimerReadStatus();
		setState(BT_STATE.CONNECTED);
	}

	@Override
	protected void logAndStreamStatusChanged() {
		
//		if(mCurrentCommand==START_LOGGING_ONLY_COMMAND){
//			TODO this causing a problem Shimmer Bluetooth disconnects
//			setState(BT_STATE.SDLOGGING);
//		}
		if(mCurrentCommand==STOP_LOGGING_ONLY_COMMAND){
			//TODO need to query the Bluetooth connection here!
			if(mIsStreaming){
				setState(BT_STATE.STREAMING);
			}
			else if(mIsConnected){
				setState(BT_STATE.CONNECTED);
			}
			else{
				setState(BT_STATE.DISCONNECTED);
			}
		}
		else{
			if(mIsStreaming && mIsSDLogging){
				setState(BT_STATE.STREAMING_AND_SDLOGGING);
			}
			else if(mIsStreaming){
				setState(BT_STATE.STREAMING);
			}
			else if(mIsSDLogging){
				setState(BT_STATE.SDLOGGING);
			}
			else{
				if(!mIsStreaming && !mIsSDLogging && mIsConnected){
					setState(BT_STATE.CONNECTED);	
				}
//				if(getBTState() == BT_STATE.INITIALISED){
//					
//				}
//				else if(getBTState() != BT_STATE.CONNECTED){
//					setState(BT_STATE.CONNECTED);
//				}
				
				CallbackObject callBackObject = new CallbackObject(NOTIFICATION_SHIMMER_STATE_CHANGE, mState, getBluetoothAddress(), mUniqueID);
				sendCallBackMsg(MSG_IDENTIFIER_STATE_CHANGE, callBackObject);
			}
		}
		
	}

	@Override
	protected void processMsgFromCallback(ShimmerMsg shimmerMSG) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void sendProgressReport(ProgressReportPerCmd pr) {
		
	}

	@Override
	protected void startOperation(BT_STATE currentOperation) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void startOperation(BT_STATE currentOperation, int totalNumOfCmds) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void batteryStatusChanged() {
		// TODO Auto-generated method stub
		
	}

	public void finishOperation(BT_STATE btState){
		
	}
	
	
	
}

