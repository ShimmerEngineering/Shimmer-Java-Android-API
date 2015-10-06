/*Rev1.7
 * 
 * 
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
 * Changes since 1.6
 * - cancel timers for log and stream upon disconnect
 * 
 * Changes since 1.5
 * - updates to constructors
 * 
 * Changes since 1.4.3
 * - remove responsetimer to ShimmerBluetooth
 *  
 * Changes since 1.4.2
 * - included call to isreadyforstreaming
 * - new object for callback method (msg_identifier 1 and 2 only)
 * - only runs connect() if mSerialPort==null
 * - added packet reception rate callback
 * 
 * Changes since 1.4
 * - updated states, and comments
 * 

 */

package com.shimmerresearch.pcdriver;

import java.io.Serializable;
import java.util.Calendar;

import com.shimmerresearch.bluetooth.ProgressReportPerCmd;
import com.shimmerresearch.bluetooth.ProgressReportPerDevice;
import com.shimmerresearch.bluetooth.ShimmerBluetooth;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.ShimmerMsg;

import jssc.SerialPort;
import jssc.SerialPortException;


public class ShimmerPC extends ShimmerBluetooth implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5927054314345918072L;
	
	// Used by the constructor when the user intends to write new settings to the Shimmer device after connection
	transient SerialPort mSerialPort=null;
	ObjectCluster objectClusterTemp = null;
	
	private boolean mVerboseMode = true;
	private String mParentClassName = "ShimmerPC";
	
	public static final int MSG_IDENTIFIER_STATE_CHANGE = 0;
	public static final int MSG_IDENTIFIER_NOTIFICATION_MESSAGE = 1; 
	public static final int MSG_IDENTIFIER_DATA_PACKET = 2;
	public static final int MSG_IDENTIFIER_PACKET_RECEPTION_RATE = 3;
	public static final int MSG_IDENTIFIER_PROGRESS_REPORT_PER_DEVICE = 4;
	public static final int MSG_IDENTIFIER_PROGRESS_REPORT_ALL = 5;
	public static final int MSG_IDENTIFIER_PACKET_RECEPTION_RATE_CURRENT = 6;
	
	public static final int NOTIFICATION_STOP_STREAMING = 0;
	public static final int NOTIFICATION_START_STREAMING = 1;
	public static final int NOTIFICATION_FULLY_INITIALIZED = 2;
	public final static int NOTIFICATION_STATE_CHANGE = 3;
	
	double mLastSavedCalibratedTimeStamp = 0.0;
	public ProgressReportPerDevice progressReportPerDevice;
	
	
	/**
	 * Constructor. Prepares a new Bluetooth session. Upon Connection the configuration of the device is read back and used. No device setup is done. To setup device see other Constructors.
	 * @param context  The UI Activity Context
	 * @param handler  A Handler to send messages back to the UI Activity
	 * @param myname  To allow the user to set a unique identifier for each Shimmer device
	 * @param countiousSync A boolean value defining whether received packets should be checked continuously for the correct start and end of packet.
	 */
	public ShimmerPC(String myName, Boolean continousSync) {
		mShimmerUserAssignedName=myName;
		mContinousSync=continousSync;
		mSetupDevice=false;
	}
	
	/**
	 * Constructor. Prepares a new Bluetooth session. Upon Connection the configuration of the device is read back and used. No device setup is done. To setup device see other Constructors.
	 * @param context  The UI Activity Context
	 * @param handler  A Handler to send messages back to the UI Activity
	 * @param myname  To allow the user to set a unique identifier for each Shimmer device
	 * @param countiousSync A boolean value defining whether received packets should be checked continuously for the correct start and end of packet.
	 */
	public ShimmerPC(String comPort, String myBluetoothAddress, String myName, Boolean continousSync) {
		mUniqueID=comPort;
		mMyBluetoothAddress=myBluetoothAddress;
		mShimmerUserAssignedName=myName;
		mContinousSync=continousSync;
		mSetupDevice=false;
	}

	/**Shimmer 3 Constructor
	 * @param myname  To allow the user to set a unique identifier for each Shimmer device
	 * @param samplingRate Defines the sampling rate
	 * @param accelRange Defines the Acceleration range. Valid range setting values for the Shimmer 2 are 0 (+/- 1.5g), 1 (+/- 2g), 2 (+/- 4g) and 3 (+/- 6g). Valid range setting values for the Shimmer 2r are 0 (+/- 1.5g) and 3 (+/- 6g).
	 * @param gsrRange Numeric value defining the desired gsr range. Valid range settings are 0 (10kOhm to 56kOhm),  1 (56kOhm to 220kOhm), 2 (220kOhm to 680kOhm), 3 (680kOhm to 4.7MOhm) and 4 (Auto Range).
	 * @param setEnabledSensors Defines the sensors to be enabled (e.g. 'Shimmer.SENSOR_ACCEL|Shimmer.SENSOR_GYRO' enables the Accelerometer and Gyroscope)
	 * @param countiousSync A boolean value defining whether received packets should be checked continuously for the correct start and end of packet.
	 * @param enableLowPowerAccel Enables low power Accel on the wide range accelerometer
	 * @param enableLowPowerGyro Enables low power Gyro
	 * @param enableLowPowerMag Enables low power Mag
	 * @param gyroRange Sets the Gyro Range of the accelerometer
	 * @param magRange Sets the Mag Range
	 * @param exg1 Sets the register of EXG chip 1
	 * @param exg2 Setes the register of EXG chip 2
	 */
	public ShimmerPC(String myName, double samplingRate, int accelRange, int gsrRange, int setEnabledSensors, boolean continousSync, boolean enableLowPowerAccel, boolean enableLowPowerGyro, boolean enableLowPowerMag, int gyroRange, int magRange,byte[] exg1,byte[] exg2) {
//		mState = BT_STATE.NONE;
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
	*  Shimmer2, Constructor. Prepares a new Bluetooth session. Additional fields allows the device to be set up immediately.
	 * @param myname  To allow the user to set a unique identifier for each Shimmer device
	 * @param samplingRate Defines the sampling rate
	 * @param accelRange Defines the Acceleration range. Valid range setting values for the Shimmer 2 are 0 (+/- 1.5g), 1 (+/- 2g), 2 (+/- 4g) and 3 (+/- 6g). Valid range setting values for the Shimmer 2r are 0 (+/- 1.5g) and 3 (+/- 6g).
	 * @param gsrRange Numeric value defining the desired gsr range. Valid range settings are 0 (10kOhm to 56kOhm),  1 (56kOhm to 220kOhm), 2 (220kOhm to 680kOhm), 3 (680kOhm to 4.7MOhm) and 4 (Auto Range).
	 * @param setEnabledSensors Defines the sensors to be enabled (e.g. 'Shimmer.SENSOR_ACCEL|Shimmer.SENSOR_GYRO' enables the Accelerometer and Gyroscope)
	 * @param countiousSync A boolean value defining whether received packets should be checked continuously for the correct start and end of packet.
	 * @param magGain Set mag gain
	 */
	public ShimmerPC(String myName, double samplingRate, int accelRange, int gsrRange, int setEnabledSensors, boolean continousSync, int magGain) {
//		mState = BT_STATE.NONE;
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
	
	// Javadoc comment follows
    /**
     * @deprecated
     * The Shimmer constructor should only have one Shimmer2R constructor
     */
    @Deprecated
	public ShimmerPC( String myName, double samplingRate, int accelRange, int gsrRange, int setEnabledSensors, boolean continousSync) {
    	mShimmerSamplingRate = samplingRate;
    	mAccelRange = accelRange;
		mGSRRange = gsrRange;
		mSetEnabledSensors=setEnabledSensors;
		mShimmerUserAssignedName = myName;
		mSetupDevice = true;
		mContinousSync = continousSync;
	}
	
	/**
	 * Connect to device specified by address
	 * @param address  The comport of the device e.g. COM32, note device will have to be paired first
	 * @param empty  This is for forward compatibility, in the event a choice of library is offered, any string value can be entered now ~ does nothing
	 */
	@Override
	public synchronized void connect(final String address, String a) {
		
		Thread thread = new Thread(){
			public void run(){
				setState(BT_STATE.CONNECTING);
//				setState(BT_STATE.CONNECTING);
				mIamAlive = false;
				if (mSerialPort==null){
					mUniqueID = address;
			//		mMyBluetoothAddress = address;
					mSerialPort = new SerialPort(address);
					getListofInstructions().clear();
					mFirstTime=true;
					try {
						consolePrintLn("Port open: " + mSerialPort.openPort());
						consolePrintLn("Params set: " + mSerialPort.setParams(115200, 8, 1, 0));
						consolePrintLn("Port Status : " + Boolean.toString(mSerialPort.isOpened()));
						if (mIOThread != null) { 
							mIOThread = null;
							mPThread = null;
						}
						if (mSerialPort.isOpened() && mState!=BT_STATE.DISCONNECTED){
//						if (mSerialPort.isOpened() && mState!=BT_STATE.NONE && mState!=BT_STATE.DISCONNECTED){
//							setState(BT_STATE.CONNECTED);
							mIsConnected = true;

							mIOThread = new IOThread();
							mIOThread.start();
							if(mUseProcessingThread){
							mPThread = new ProcessingThread();
							mPThread.start();
							}
							initialize();
						} else {
							disconnect();
						}
					}
					catch (SerialPortException ex){
						connectionLost();
						closeConnection();
						setState(BT_STATE.CONNECTION_FAILED);
						//System.out.println(ex);
					}
				} else {
					
				}
				
			}
	    };
	    
	    if (!mIsConnected){
	    	thread.start();
	    }
	    
//	    if (getState()==STATE.NONE
//	    		|| getState()==STATE.CONNECTION_LOST
//	    		|| getState()==STATE.DISCONNECTED){
//	    	thread.start();
//	    }
	}
	
	
	@Override
	public boolean bytesAvailableToBeRead() {
		try {
			if(mSerialPort != null){
				if (mSerialPort.getInputBufferBytesCount()!=0){
					return true;
				}
			}

		} catch (SerialPortException e) {
			connectionLost();
			e.printStackTrace();
		}
		return false;
	}
	
	public int availableBytes(){
		try {
			if(mSerialPort != null){
				return mSerialPort.getInputBufferBytesCount();
			}
			else{
				return 0;
			}
			
		} catch (SerialPortException e) {
			connectionLost();
			e.printStackTrace();
			return 0;
		}
	}

	@Override
	public void writeBytes(byte[] data) {
		// TODO Auto-generated method stub
		try {
			mSerialPort.writeBytes(data);
		} catch (SerialPortException e) {
			// TODO Auto-generated catch block
			connectionLost();
			e.printStackTrace();
		}
	}

	@Override
	protected byte[] readBytes(int numberofBytes) {
		// TODO Auto-generated method stub
		try {
			if(mSerialPort != null){
				if (mSerialPort.isOpened())
				{
					return(mSerialPort.readBytes(numberofBytes));
				} else {
					System.out.println("ALERT!!");
				}
			}
		} catch (SerialPortException e) {
			// TODO Auto-generated catch block
			connectionLost();
			e.printStackTrace();
		}
		catch (NullPointerException e) {
			// TODO Auto-generated catch block
			connectionLost();
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void stop() {
		disconnect();
	}

	@Override
	protected void isNowStreaming() {
		// Send a notification msg to the UI through a callback (use a msg identifier notification message)
		// Do something here
		
		CallbackObject callBackObject = new CallbackObject(NOTIFICATION_START_STREAMING, getBluetoothAddress(), mUniqueID);
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
		CallbackObject callBackObject = new CallbackObject(NOTIFICATION_STATE_CHANGE, mState, getBluetoothAddress(), mUniqueID);
		sendCallBackMsg(MSG_IDENTIFIER_STATE_CHANGE, callBackObject);
		isReadyForStreaming();
	}

	@Override
	protected void isReadyForStreaming() {
		// Send msg fully initialized, send notification message,  
		// Do something here
        mIsInitialised = true;
        sensorAndConfigMapsCreate();
        sensorMapUpdateFromEnabledSensorsVar();

        if (mSendProgressReport){
        	finishOperation(BT_STATE.CONNECTING);
        }
        
		CallbackObject callBackObject = new CallbackObject(NOTIFICATION_FULLY_INITIALIZED, getBluetoothAddress(), mUniqueID);
		sendCallBackMsg(MSG_IDENTIFIER_NOTIFICATION_MESSAGE, callBackObject);
		if (mTimerCheckAlive==null && mTimerReadStatus==null && mTimerReadBattStatus==null){
        	//super.operationFinished();
			startTimerCheckIfAlive();
			startTimerReadStatus();
			startTimerReadBattStatus();
			
        }
		setState(BT_STATE.CONNECTED);
	}


	public void calculatePacketReceptionRateCurrent(int intervalMs) {
		
		double numPacketsShouldHaveReceived = (((double)intervalMs)/1000) * mShimmerSamplingRate;
		
		if (mLastReceivedCalibratedTimeStamp!=-1){
			double timeDifference=mLastReceivedCalibratedTimeStamp-mLastSavedCalibratedTimeStamp;
			double numPacketsReceived= ((timeDifference/1000) * mShimmerSamplingRate);
			mPacketReceptionRateCurrent = (numPacketsReceived/numPacketsShouldHaveReceived)*100.0;
		}	

		mPacketReceptionRateCurrent = (mPacketReceptionRateCurrent>100.0? 100.0:mPacketReceptionRateCurrent);
		mPacketReceptionRateCurrent = (mPacketReceptionRateCurrent<0? 0.0:mPacketReceptionRateCurrent);

		mLastSavedCalibratedTimeStamp = mLastReceivedCalibratedTimeStamp;

		CallbackObject callBackObject = new CallbackObject(MSG_IDENTIFIER_PACKET_RECEPTION_RATE_CURRENT, getBluetoothAddress(), mUniqueID, mPacketReceptionRateCurrent);
		sendCallBackMsg(MSG_IDENTIFIER_PACKET_RECEPTION_RATE_CURRENT, callBackObject);
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
		closeConnection();
		setState(BT_STATE.DISCONNECTED);
	}

	@Override
	protected void sendStatusMsgPacketLossDetected() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void connectionLost() {
		closeConnection();
		System.out.println("Connection Lost");
		setState(BT_STATE.CONNECTION_LOST);
	}
	
	private void closeConnection(){
		try {
			if (mIOThread != null) {
				mIOThread.stop = true;
				mIOThread = null;
				if(mUseProcessingThread){
				mPThread.stop = true;
				mPThread = null;
				}
				
			}
			mIsStreaming = false;
			mIsInitialised = false;

//			setState(BT_STATE.NONE);
			setState(BT_STATE.DISCONNECTED);
			if (mSerialPort != null){
				
				if(mSerialPort.isOpened ()) {
				  mSerialPort.purgePort (1);
				  mSerialPort.purgePort (2);
				  mSerialPort.closePort ();
				 
				}
				
			}
			 mSerialPort = null;
		} catch (SerialPortException e) {
			// TODO Auto-generated catch block
//			setState(BT_STATE.NONE);
			setState(BT_STATE.DISCONNECTED);
			
		}			
	}

	@Override
	protected void sendStatusMSGtoUI(String msg) {
		// TODO Auto-generated method stub
		
	}

	
	public String message;
	@Override
	protected void printLogDataForDebugging(String msg) {
		consolePrintLn(msg);
//		System.out.println(msg);
	}

	@Override
	public void setState(BT_STATE state) {
		
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
		
		CallbackObject callBackObject = new CallbackObject(NOTIFICATION_STATE_CHANGE, mState, getBluetoothAddress(), mUniqueID);
		sendCallBackMsg(MSG_IDENTIFIER_STATE_CHANGE, callBackObject);
	}
	
	public boolean isConnected(){
		return mIsConnected;
	}

	public boolean isInitialised(){
		return mIsInitialised;
	}
	
	public boolean isStreaming(){
		return mIsStreaming;
	}
	
	public boolean isSDLogging(){
		return mIsSDLogging;
	}

	@Override
	public BT_STATE getBTState() {
		return mState;
	}
	
	@Override
	public void startOperation(BT_STATE currentOperation){
		consolePrintLn(currentOperation + " START");
		
		progressReportPerDevice = new ProgressReportPerDevice(this, currentOperation, 1);
		progressReportPerDevice.mOperationState = ProgressReportPerDevice.OperationState.INPROGRESS;
		
		CallbackObject callBackObject = new CallbackObject(mState, getBluetoothAddress(), mUniqueID, progressReportPerDevice);
		sendCallBackMsg(MSG_IDENTIFIER_PROGRESS_REPORT_PER_DEVICE, callBackObject);
	}
	

	@Override
	public void startOperation(BT_STATE currentOperation, int totalNumOfCmds){
		consolePrintLn(currentOperation + " START");

		progressReportPerDevice = new ProgressReportPerDevice(this, currentOperation, totalNumOfCmds);
		progressReportPerDevice.mOperationState = ProgressReportPerDevice.OperationState.INPROGRESS;
		
		CallbackObject callBackObject = new CallbackObject(mState, getBluetoothAddress(), mUniqueID, progressReportPerDevice);
		sendCallBackMsg(MSG_IDENTIFIER_PROGRESS_REPORT_PER_DEVICE, callBackObject);
	}
	
//	@Override
	public void finishOperation(BT_STATE btState){
		
		consolePrintLn("CURRENT OPERATION " + progressReportPerDevice.mCurrentOperationBtState + "\tFINISHED:" + btState);
		
		if(progressReportPerDevice.mCurrentOperationBtState == btState){

			progressReportPerDevice.finishOperation();
			progressReportPerDevice.mOperationState = ProgressReportPerDevice.OperationState.SUCCESS;
			//JC: moved operationFinished to is ready for streaming, seems to be called before the inquiry response is received
			super.operationFinished();
			CallbackObject callBackObject = new CallbackObject(mState, getBluetoothAddress(), mUniqueID, progressReportPerDevice);
			sendCallBackMsg(MSG_IDENTIFIER_PROGRESS_REPORT_PER_DEVICE, callBackObject);
			
			//Removed to try and stop progress going to 0% after finishing
//			progressReportPerDevice = new ProgressReportPerDevice(this, BT_STATE.NONE, 1);
//			callBackObject = new CallbackObject(mState, getBluetoothAddress(), mUniqueID, progressReportPerDevice);
//			sendCallBackMsg(MSG_IDENTIFIER_PROGRESS_REPORT_PER_DEVICE, callBackObject);
		}

	}
	
	@Override
	protected void hasStopStreaming() {
		// Send a notification msg to the UI through a callback (use a msg identifier notification message)
				// Do something here
		CallbackObject callBackObject = new CallbackObject(NOTIFICATION_STOP_STREAMING, getBluetoothAddress(), mUniqueID);
		sendCallBackMsg(MSG_IDENTIFIER_NOTIFICATION_MESSAGE, callBackObject);
		
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
				
				CallbackObject callBackObject = new CallbackObject(NOTIFICATION_STATE_CHANGE, mState, getBluetoothAddress(), mUniqueID);
				sendCallBackMsg(MSG_IDENTIFIER_STATE_CHANGE, callBackObject);
			}
		}
		
	}

	@Override
	protected void processMsgFromCallback(ShimmerMsg shimmerMSG) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	protected void sendProgressReport(ProgressReportPerCmd pRPC) {
		if(progressReportPerDevice!=null){
//			if(progressReportPerDevice.mCurrentOperationBtState!=BT_STATE.NONE){
				progressReportPerDevice.updateProgress(pRPC);
				
				CallbackObject callBackObject = new CallbackObject(mState, getBluetoothAddress(), mUniqueID, progressReportPerDevice);
				sendCallBackMsg(MSG_IDENTIFIER_PROGRESS_REPORT_PER_DEVICE, callBackObject);
				
				System.out.println("ProgressCounter" + progressReportPerDevice.mProgressCounter + "\tProgressEndValue " + progressReportPerDevice.mProgressEndValue);
				if(progressReportPerDevice.mProgressCounter==progressReportPerDevice.mProgressEndValue){
					finishOperation(progressReportPerDevice.mCurrentOperationBtState);
				}
//			}
			
		}
	}

	@Override
	protected void batteryStatusChanged() {
		CallbackObject callBackObject = new CallbackObject(NOTIFICATION_STATE_CHANGE, mState, getBluetoothAddress(), mUniqueID);
		sendCallBackMsg(MSG_IDENTIFIER_STATE_CHANGE, callBackObject);
	}
	
	
	private void consolePrintLn(String message) {
		if(mVerboseMode) {
			Calendar rightNow = Calendar.getInstance();
			String rightNowString = "[" + String.format("%02d",rightNow.get(Calendar.HOUR_OF_DAY)) 
					+ ":" + String.format("%02d",rightNow.get(Calendar.MINUTE)) 
					+ ":" + String.format("%02d",rightNow.get(Calendar.SECOND)) 
					+ ":" + String.format("%03d",rightNow.get(Calendar.MILLISECOND)) + "]";
			System.out.println(rightNowString + " " + mParentClassName + ": " + mUniqueID + " " + getMacIdFromBtParsed() + " " + message);
		}		
	}
}

