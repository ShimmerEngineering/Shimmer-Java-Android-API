//Rev_1.9
/*
 * Copyright (c) 2010 - 2014, Shimmer Research, Ltd.
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
 * @author Jong Chern Lim, Ruaidhri Molloy, Mark Nolan
 * @date  September, 2014
 * 
 * Changes since 1.8
 * - set mInstructionStackLock in initialize(), this fix a bug when upon a ack timeout disconnect shimmer is unable to reconnect
 * - add mtimer cancel and purge to intialize for precaution
 * - added reset, to prevent API thinking it is the wrong fwidentifier (e.g. using btstream after logandstream) causing the get_status timer to be called
 * - added readBlinkLED to initializeShimmer3, remove mCurrentLEDStatus from startStreaming
 * - added a check for get_dir and get_status timeout, device won't disconnect if there is packet loss detected
 * 
 * Changes since since 1.7 
 * - updated logandstream support, now supports push button, start-stop streaming
 * 
 * Changes since 1.6
 * - updated to support LogAndStream
 * - updated checkBatt()
 * 
 * Changes since 1.5
 * - updated comments
 * - Baud rate setting support
 *
 * Changes since 1.4.04
 * - Reduce timeout for get_shimmer_version_command_new, to speed up connection for Shimmer2r 
 * - Move timeout response task to here, removed from Shimmer and ShimmerPCBT
 * - Added 
 *
 * Changes since 1.4.03
 * - support for Shimmer3 bridge amplifier, sensor conflict handling for Shimmer3
 * - Added isEXGUsingTestSignal24Configuration() isEXGUsingTestSignal16Configuration() isEXGUsingECG24Configuration() isEXGUsingECG16Configuration() isEXGUsingEMG24Configuration() isEXGUsingEMG16Configuration()
 * 
 * Changes since 1.4.02
 * - moved setting of writeexg setting to after the ack, otherwise readexg and writeexg in the instruction stack will yield wrong results
 * 
 *  Changes since 1.4.01
 *  - added exg set configuration to initialize shimmer3 exg from constructor
 * 
 *  Changes since 1.4
 *  - removed mShimmerSamplingRate decimal formatter, decimal formatter should be done on the UI
 *  - remove null characters from mListofInstructions, after a stop streaming command, this was causing a race condition error
 *  
 */

package com.shimmerresearch.bluetooth;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;

import org.apache.commons.lang3.ArrayUtils;

import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.ExpansionBoardDetails;
import com.shimmerresearch.driver.ShimmerBattStatusDetails;
import com.shimmerresearch.driver.ShimmerMsg;
import com.shimmerresearch.driver.ShimmerVerDetails.FW_ID;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.ShimmerVerDetails.HW_ID;
import com.shimmerresearch.driver.ShimmerObject;
import com.shimmerresearch.driver.ShimmerVerObject;
import com.shimmerresearch.driver.Util;
import com.sun.javafx.css.parser.StopConverter;

public abstract class ShimmerBluetooth extends ShimmerObject implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8439353551730215801L;

	//region --------- CLASS VARIABLES AND ABSTRACT METHODS ---------
	
	protected long mSetEnabledSensors = SENSOR_ACCEL;								// Only used during the initialization process, see initialize();
	
	public enum BT_STATE{
		NONE("None"),       // The class is doing nothing
		CONNECTING("Connecting"), // The class is now initiating an outgoing connection
		CONNECTED("Connected"),  // The class is now connected to a remote device
		STREAMING("Streaming"),  // The class is now connected to a remote device
		STREAMING_AND_SDLOGGING("Streaming and SD Logging"),
		SDLOGGING("SD Logging"),
		INITIALISING("Initialising"), // 
		INITIALISED("Initialised"), // 
		CONFIGURING("Configuring"), // The class is now initiating an outgoing connection 
		CONFIGURED("Configured"), // 
		DISCONNECTED("Disconnected"),
		CONNECTION_LOST("Lost connection"),
		CONNECTION_FAILED("Connection Failed");
//		FAILED(),  // The class is now connected to a remote device
		
	    private final String text;

	    /**
	     * @param text
	     */
	    private BT_STATE(final String text) {
	        this.text = text;
	    }

	    /* (non-Javadoc)
	     * @see java.lang.Enum#toString()
	     */
	    @Override
	    public String toString() {
	        return text;
	    }
	}
	public BT_STATE mState = BT_STATE.NONE;

	protected boolean mIsConnected = false;
	
	private boolean mInstructionStackLock = false;
	protected boolean mSendProgressReport = false;
	protected byte mCurrentCommand;	
	protected boolean mWaitForAck=false;                                          // This indicates whether the device is waiting for an acknowledge packet from the Shimmer Device  
	protected boolean mWaitForResponse=false; 									// This indicates whether the device is waiting for a response packet from the Shimmer Device 
	protected boolean mTransactionCompleted=true;									// Variable is used to ensure a command has finished execution prior to executing the next command (see initialize())
	transient protected IOThread mIOThread;
	transient protected ProcessingThread mPThread;
	protected boolean mContinousSync=false;                                       // This is to select whether to continuously check the data packets 
	protected boolean mSetupDevice=false;		
	protected Stack<Byte> byteStack = new Stack<Byte>();
	protected double mLowBattLimit=3.4;
	protected int numBytesToReadFromExpBoard=0;
	
	ArrayBlockingQueue<RawBytePacketWithPCTimeStamp> mABQPacketByeArray = new ArrayBlockingQueue<RawBytePacketWithPCTimeStamp>(10000);
	List<Long> mListofPCTimeStamps = new ArrayList<Long>();
	
	protected boolean mIamAlive = false;
//	public String mUniqueID = ""; //Stores the Bluetooth Com port
	protected abstract void connect(String address,String bluetoothLibrary);
	protected abstract void dataHandler(ObjectCluster ojc);
	protected abstract boolean bytesToBeRead();
	protected abstract int availableBytes();
	
	protected abstract void writeBytes(byte[] data);
	protected abstract void stop();
	protected abstract void sendProgressReport(ProgressReportPerCmd pr);
	protected abstract void isReadyForStreaming();
	protected abstract void isNowStreaming();
	protected abstract void hasStopStreaming();
	protected abstract void sendStatusMsgPacketLossDetected();
	protected abstract void inquiryDone();
	protected abstract void sendStatusMSGtoUI(String msg);
	protected abstract void printLogDataForDebugging(String msg);
	protected abstract void connectionLost();
	protected abstract void setState(BT_STATE state);
	protected abstract void startOperation(BT_STATE currentOperation);
	protected abstract void startOperation(BT_STATE currentOperation, int totalNumOfCmds);
	protected abstract void logAndStreamStatusChanged();
	protected abstract void batteryStatusChanged();
	
	protected abstract byte[] readBytes(int numberofBytes);
	protected abstract byte readByte();
	protected List<byte []> mListofInstructions = new  ArrayList<byte[]>();
	private final int ACK_TIMER_DURATION = 2; 									// Duration to wait for an ack packet (seconds)
	protected boolean mDummy=false;
	protected boolean mFirstTime=true;
	private byte mTempByteValue;												// A temporary variable used to store Byte value	
	protected int mTempIntValue;												// A temporary variable used to store Integer value, used mainly to store a value while waiting for an acknowledge packet (e.g. when writeGRange() is called, the range is stored temporarily and used to update GSRRange when the acknowledge packet is received.
	protected long tempEnabledSensors;											// This stores the enabled sensors
	private int mTempChipID;
	protected boolean mSync=true;												// Variable to keep track of sync
	protected boolean mSetupEXG = false;
	private byte[] cmdcalibrationParameters = new byte [22];  
	
	private int mReadStatusPeriod=5000;
	private int mReadBattStatusPeriod=600000;	// Batt status is updated every 10 mins 
	private int mCheckAlivePeriod=2000;
	
	transient protected Timer mTimerWaitForAckOrResp;								// Timer variable used when waiting for an ack or response packet
	transient protected Timer mTimerCheckAlive;
	transient protected Timer mTimerReadStatus;
	transient protected Timer mTimerReadBattStatus;								// 
	
	private int mCountDeadConnection = 0;
	private boolean mCheckIfConnectionisAlive = false;
	
	transient ByteArrayOutputStream mByteArrayOutputStream = new ByteArrayOutputStream();
	
	public boolean mVerboseMode = true;
	public String mParentClassName = "ShimmerBluetooth";
	
	protected boolean mUseProcessingThread = false;
	
	//endregion
	
	/**
	 * Provides an interface directly to the method BuildMSG. This can be used
	 * to implement algorithm/filters/etc. Two methods are provided, processdata
	 * to implement your methods, and InitializeProcessData which is called
	 * everytime you startstreaming, in the event you need to reinitialize your
	 * method/algorithm everytime a Shimmer starts streaming
	 *
	 */
	public interface DataProcessing {

		/**
		 * Initialise your method/algorithm here, this callback is called when
		 * startstreaming is called
		 */
		
		/**
		 * Initialise Process Data here. This is called whenever the
		 * startStreaming command is called and can be used to initialise
		 * algorithms
		 * 
		 */
		public void InitializeProcessData();
		
		/**
		 * Process data here, algorithms can access the object cluster built by
		 * the buildMsg method here
		 * 
		 * @param ojc
		 *            the objectCluster built by the buildMsg method
		 * @return the processed objectCluster
		 */
		public ObjectCluster ProcessData(ObjectCluster ojc);

	}
	DataProcessing mDataProcessing;

	
	public class ProcessingThread extends Thread {
		byte[] tb ={0};
		byte[] newPacket=new byte[mPacketSize+1];
		public boolean stop = false;
		int count=0;
		public synchronized void run() {
			while (!stop) {
				if (!mABQPacketByeArray.isEmpty()){
					count++;
					if (count%1000==0){
						consolePrintLn("Queue Size: " + mABQPacketByeArray.size());
						printLogDataForDebugging("Queue Size: " + mABQPacketByeArray.size() + "\n");
					}
					RawBytePacketWithPCTimeStamp rbp = mABQPacketByeArray.remove();
					buildAndSendMsg(rbp.mDataArray, FW_TYPE_BT, 0,rbp.mSystemTimeStamp);
				}
			}
		}
	}
	
	//region --------- BLUETOOH STACK --------- 
	
	public class IOThread extends Thread {
		byte[] tb ={0};
		byte[] newPacket=new byte[mPacketSize+1];
		public boolean stop = false;
		public synchronized void run() {
			while (!stop) {
				/////////////////////////
				// is an instruction running ? if not proceed
				if (ismInstructionStackLock()==false){
					// check instruction stack, are there any other instructions left to be executed?
					if (!getListofInstructions().isEmpty()) {
						if (getListofInstructions().get(0)==null) {
							getListofInstructions().remove(0);
							String msg = "Null Removed ";
							printLogDataForDebugging(msg);
						}
					}
					if (!getListofInstructions().isEmpty()){

						byte[] insBytes = (byte[]) getListofInstructions().get(0);
						mCurrentCommand=insBytes[0];
						setInstructionStackLock(true);
						mWaitForAck=true;
						
						String msg = "Command Transmitted: \t\t\t" + Util.bytesToHexStringWithSpacesFormatted(insBytes);
						printLogDataForDebugging(msg);

						if(!mIsStreaming){
							while(availableBytes()>0){ //this is to clear the buffer 
								tb=readBytes(availableBytes());
							}
						}
						 

						writeBytes(insBytes);

						
						if (mCurrentCommand==STOP_STREAMING_COMMAND){
							mIsStreaming=false;
							getListofInstructions().removeAll(Collections.singleton(null));
						} else {
							if (mCurrentCommand==GET_FW_VERSION_COMMAND){
								startTimerWaitForAckOrResp(ACK_TIMER_DURATION);
							} else if (mCurrentCommand==GET_SAMPLING_RATE_COMMAND){
								startTimerWaitForAckOrResp(ACK_TIMER_DURATION);
							} else if (mCurrentCommand==GET_SHIMMER_VERSION_COMMAND_NEW){
								startTimerWaitForAckOrResp(ACK_TIMER_DURATION);
							} else {
								if(mIsStreaming){
									startTimerWaitForAckOrResp(ACK_TIMER_DURATION);
								} else {
									startTimerWaitForAckOrResp(ACK_TIMER_DURATION+10);
								}
							}
						}
						
							mTransactionCompleted=false;
						
					}


				}
				
				
				if (mWaitForAck==true && mIsStreaming ==false) {

					if (bytesToBeRead()){
						tb=readBytes(1);
						mIamAlive = true;
						String msg="";
						//	msg = "rxb resp : " + Arrays.toString(tb);
						//	printLogDataForDebugging(msg);

						if (mCurrentCommand==STOP_STREAMING_COMMAND) { //due to not receiving the ack from stop streaming command we will skip looking for it.
							stopTimerWaitForAckOrResp();
							mIsStreaming=false;
							mTransactionCompleted=true;
							mWaitForAck=false;
							try {
								Thread.sleep(200);	// Wait to ensure that we dont missed any bytes which need to be cleared
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							byteStack.clear();

							while(availableBytes()>0){ //this is to clear the buffer 

								tb=readBytes(availableBytes());

							}
							hasStopStreaming();					
							getListofInstructions().remove(0);
							getListofInstructions().removeAll(Collections.singleton(null));
							setInstructionStackLock(false);
						}

						if ((byte)tb[0]==ACK_COMMAND_PROCESSED)
						{	
							msg = "Ack Received for Command: \t\t" + Util.byteToHexStringFormatted(mCurrentCommand);
							printLogDataForDebugging(msg);
							if (mCurrentCommand != GET_STATUS_COMMAND && mCurrentCommand != TEST_CONNECTION_COMMAND && mCurrentCommand != SET_BLINK_LED && mSendProgressReport){
//								sendProgressReport(new ProgressReportPerCmd(mCurrentCommand,getmListofInstructions().size(),mMyBluetoothAddress));
								sendProgressReport(new ProgressReportPerCmd(mCurrentCommand,getListofInstructions().size(),mMyBluetoothAddress, mUniqueID));
							}
							
							if (mCurrentCommand==START_STREAMING_COMMAND || mCurrentCommand==START_SDBT_COMMAND) {
								stopTimerWaitForAckOrResp();
								mIsStreaming=true;
								if (mCurrentCommand==START_SDBT_COMMAND){
									mIsSDLogging = true;
								}
								mTransactionCompleted=true;
								byteStack.clear();
								isNowStreaming();
								mWaitForAck=false;
								getListofInstructions().remove(0);
								setInstructionStackLock(false);
							}
							else if (mCurrentCommand==SET_SAMPLING_RATE_COMMAND) {
								stopTimerWaitForAckOrResp(); //cancel the ack timer
								mTransactionCompleted=true;
								mWaitForAck=false;
								byte[] instruction=getListofInstructions().get(0);
								double tempdouble=-1;
								if (mHardwareVersion==HW_ID.SHIMMER_2 || mHardwareVersion==HW_ID.SHIMMER_2R){
									tempdouble=(double)1024/instruction[1];
								} else {
									tempdouble = 32768/(double)((int)(instruction[1] & 0xFF) + ((int)(instruction[2] & 0xFF) << 8));
								}
								mShimmerSamplingRate = tempdouble;
								getListofInstructions().remove(0);
								setInstructionStackLock(false);
								if (mHardwareVersion == HW_ID.SHIMMER_3){ // has to be here because to ensure the current exgregister settings have been read back
									//check sampling rate and adjust accordingly;
									/*if (mShimmerSamplingRate<=128){
										writeEXGRateSetting(1,0);
										writeEXGRateSetting(2,0);
									} else if (mShimmerSamplingRate<=256){
										writeEXGRateSetting(1,1);
										writeEXGRateSetting(2,1);
									}
									else if (mShimmerSamplingRate<=512){
										writeEXGRateSetting(1,2);
										writeEXGRateSetting(2,2);
									}*/
								}
								
							}
							else if (mCurrentCommand==SET_BUFFER_SIZE_COMMAND) {
								stopTimerWaitForAckOrResp(); //cancel the ack timer
								mTransactionCompleted = true;
								mWaitForAck=false;
								mBufferSize=(int)((byte[])getListofInstructions().get(0))[1];
								getListofInstructions().remove(0);
								setInstructionStackLock(false);
							}
							else if (mCurrentCommand==INQUIRY_COMMAND) {
								mWaitForResponse=true;
								mWaitForAck=false;
								getListofInstructions().remove(0);
							}
							else if (mCurrentCommand==GET_LSM303DLHC_ACCEL_LPMODE_COMMAND) {
								mWaitForResponse=true;
								mWaitForAck=false;
								getListofInstructions().remove(0);
							}
							else if (mCurrentCommand==GET_LSM303DLHC_ACCEL_HRMODE_COMMAND) {
								mWaitForResponse=true;
								mWaitForAck=false;
								getListofInstructions().remove(0);
							}
							else if (mCurrentCommand==GET_BUFFER_SIZE_COMMAND) {
								mWaitForAck=false;
								mWaitForResponse=true;
								getListofInstructions().remove(0);
							}
							else if (mCurrentCommand==GET_BLINK_LED) {
								mWaitForAck=false;
								mWaitForResponse=true;
								getListofInstructions().remove(0);
							}
							else if (mCurrentCommand==GET_MAG_SAMPLING_RATE_COMMAND) {
								mWaitForAck=false;
								mWaitForResponse=true;
								getListofInstructions().remove(0);
							}
							else if (mCurrentCommand==GET_MAG_GAIN_COMMAND) {
								mWaitForAck=false;
								mWaitForResponse=true;
								getListofInstructions().remove(0);
							}
							else if (mCurrentCommand==GET_ACCEL_SENSITIVITY_COMMAND) {
								mWaitForAck=false;
								mWaitForResponse=true;
							}
							else if (mCurrentCommand==GET_MPU9150_GYRO_RANGE_COMMAND) {
								mWaitForAck=false;
								mWaitForResponse=true;
							}
							else if (mCurrentCommand==GET_GSR_RANGE_COMMAND) {
								mWaitForAck=false;
								mWaitForResponse=true;
								getListofInstructions().remove(0);
							}
							else if (mCurrentCommand==GET_FW_VERSION_COMMAND) {
								mWaitForResponse = true;
								mWaitForAck=false;
							}
							else if (mCurrentCommand==GET_ECG_CALIBRATION_COMMAND) {
								mWaitForResponse = true;
								mWaitForAck=false;
								getListofInstructions().remove(0);
							}
							else if (mCurrentCommand==GET_EMG_CALIBRATION_COMMAND) {
								mWaitForResponse = true;
								mWaitForAck=false;
								getListofInstructions().remove(0);
							}
							else if (mCurrentCommand==SET_BLINK_LED) {
								
								// TODO: check for null and size were put in
								// because if Shimmer was abruptly disconnected
								// there is sometimes indexoutofboundsexceptions
								if(getListofInstructions().size() > 0){
									if(getListofInstructions().get(0)!=null){
										if(((byte[])getListofInstructions().get(0)).length>2){
											mCurrentLEDStatus=(int)((byte[])getListofInstructions().get(0))[1];
										}
										mTransactionCompleted = true;
										//mWaitForAck=false;
										stopTimerWaitForAckOrResp(); //cancel the ack timer
										getListofInstructions().remove(0);
										setInstructionStackLock(false);
									}
								}
							}
							else if (mCurrentCommand==TEST_CONNECTION_COMMAND) {
								
								// TODO: check for null and size were put in
								// because if Shimmer was abruptly disconnected
								// there is sometimes indexoutofboundsexceptions
								if(getListofInstructions().size() > 0){
									if(getListofInstructions().get(0)!=null){
										mTransactionCompleted = true;
										//mWaitForAck=false;
										stopTimerWaitForAckOrResp(); //cancel the ack timer
										getListofInstructions().remove(0);
										setInstructionStackLock(false);
									}
								}
							}
							else if (mCurrentCommand==SET_GSR_RANGE_COMMAND) {

								mTransactionCompleted = true;
								mWaitForAck=false;
								stopTimerWaitForAckOrResp(); //cancel the ack timer
								mGSRRange=(int)((byte [])getListofInstructions().get(0))[1];
								getListofInstructions().remove(0);
								setInstructionStackLock(false);
							}
							else if (mCurrentCommand==START_LOGGING_ONLY_COMMAND) {
								mTransactionCompleted = true;
								mWaitForAck=false;
								stopTimerWaitForAckOrResp(); //cancel the ack timer
								mIsSDLogging = true;
								logAndStreamStatusChanged();
								getListofInstructions().remove(0);
								setInstructionStackLock(false);
							}
							else if (mCurrentCommand==STOP_LOGGING_ONLY_COMMAND) {

								mTransactionCompleted = true;
								mWaitForAck=false;
								stopTimerWaitForAckOrResp(); //cancel the ack timer
								mIsSDLogging = false;
								logAndStreamStatusChanged();
								getListofInstructions().remove(0);
								setInstructionStackLock(false);
							}
							else if (mCurrentCommand==GET_SAMPLING_RATE_COMMAND) {
								mWaitForResponse=true;
								mWaitForAck=false;

							}
							else if (mCurrentCommand==GET_CONFIG_BYTE0_COMMAND) {
								mWaitForResponse=true;
								mWaitForAck=false;
								getListofInstructions().remove(0);
							}
							else if (mCurrentCommand==SET_CONFIG_BYTE0_COMMAND) {
								stopTimerWaitForAckOrResp(); //cancel the ack timer
								mConfigByte0=(int)((byte [])getListofInstructions().get(0))[1];
								mWaitForAck=false;
								mTransactionCompleted=true;
								getListofInstructions().remove(0);
								setInstructionStackLock(false);
							} else if (mCurrentCommand==SET_LSM303DLHC_ACCEL_LPMODE_COMMAND) {
								stopTimerWaitForAckOrResp(); //cancel the ack timer
								mWaitForAck=false;
								mTransactionCompleted=true;
								getListofInstructions().remove(0);
								setInstructionStackLock(false);
							} else if (mCurrentCommand==SET_LSM303DLHC_ACCEL_HRMODE_COMMAND) {
								stopTimerWaitForAckOrResp(); //cancel the ack timer
								mWaitForAck=false;
								mTransactionCompleted=true;
								getListofInstructions().remove(0);
								setInstructionStackLock(false);
							}
							else if (mCurrentCommand==SET_PMUX_COMMAND) {
								stopTimerWaitForAckOrResp(); //cancel the ack timer
								if (((byte[])getListofInstructions().get(0))[1]==1) {
									mConfigByte0=(byte) ((byte) (mConfigByte0|64)&(0xFF)); 
								}
								else if (((byte[])getListofInstructions().get(0))[1]==0) {
									mConfigByte0=(byte) ((byte)(mConfigByte0 & 191)&(0xFF));
								}
								mTransactionCompleted=true;
								mWaitForAck=false;
								getListofInstructions().remove(0);
								setInstructionStackLock(false);
							}
							else if(mCurrentCommand==SET_BMP180_PRES_RESOLUTION_COMMAND){
								stopTimerWaitForAckOrResp(); //cancel the ack timer
								mPressureResolution=(int)((byte [])getListofInstructions().get(0))[1];
								mTransactionCompleted=true;
								mWaitForAck=false;
								getListofInstructions().remove(0);
								setInstructionStackLock(false);
							}
							else if (mCurrentCommand==SET_GYRO_TEMP_VREF_COMMAND) {
								stopTimerWaitForAckOrResp(); //cancel the ack timer
								mTransactionCompleted=true;
								mConfigByte0=mTempByteValue;
								mWaitForAck=false;
							}
							else if (mCurrentCommand==SET_5V_REGULATOR_COMMAND) {
								stopTimerWaitForAckOrResp(); //cancel the ack timer
								if (((byte[])getListofInstructions().get(0))[1]==1) {
									mConfigByte0=(byte) (mConfigByte0|128); 
								}
								else if (((byte[])getListofInstructions().get(0))[1]==0) {
									mConfigByte0=(byte)(mConfigByte0 & 127);
								}
								mTransactionCompleted=true;
								mWaitForAck=false;
								getListofInstructions().remove(0);
								setInstructionStackLock(false);
							}
							else if (mCurrentCommand==SET_INTERNAL_EXP_POWER_ENABLE_COMMAND) {
								stopTimerWaitForAckOrResp(); //cancel the ack timer
								if (((byte[])getListofInstructions().get(0))[1]==1) {
									mConfigByte0 = (mConfigByte0|16777216); 
									mInternalExpPower = 1;
								}
								else if (((byte[])getListofInstructions().get(0))[1]==0) {
									mConfigByte0 = mConfigByte0 & 4278190079l;
									mInternalExpPower = 0;
								}
								mTransactionCompleted=true;
								mWaitForAck=false;
								getListofInstructions().remove(0);
								setInstructionStackLock(false);
							}
							else if (mCurrentCommand==SET_ACCEL_SENSITIVITY_COMMAND) {
								stopTimerWaitForAckOrResp(); //cancel the ack timer
								mAccelRange=(int)(((byte[])getListofInstructions().get(0))[1]);
								if (mDefaultCalibrationParametersAccel == true){
									if (mHardwareVersion != HW_ID.SHIMMER_3){
										if (getAccelRange()==0){
											mSensitivityMatrixAnalogAccel = SensitivityMatrixAccel1p5gShimmer2; 
										} else if (getAccelRange()==1){
											mSensitivityMatrixAnalogAccel = SensitivityMatrixAccel2gShimmer2; 
										} else if (getAccelRange()==2){
											mSensitivityMatrixAnalogAccel = SensitivityMatrixAccel4gShimmer2; 
										} else if (getAccelRange()==3){
											mSensitivityMatrixAnalogAccel = SensitivityMatrixAccel6gShimmer2; 
										}
									} else if(mHardwareVersion == HW_ID.SHIMMER_3){
										mSensitivityMatrixAnalogAccel = SensitivityMatrixLowNoiseAccel2gShimmer3;
										mAlignmentMatrixAnalogAccel = AlignmentMatrixLowNoiseAccelShimmer3;
										mOffsetVectorAnalogAccel = OffsetVectorLowNoiseAccelShimmer3;
									}
								}

								if (mDefaultCalibrationParametersDigitalAccel){
									if (mHardwareVersion == HW_ID.SHIMMER_3){
										if (getAccelRange()==1){
											mSensitivityMatrixWRAccel = SensitivityMatrixWideRangeAccel4gShimmer3;
											mAlignmentMatrixWRAccel = AlignmentMatrixWideRangeAccelShimmer3;
											mOffsetVectorWRAccel = OffsetVectorWideRangeAccelShimmer3;
										} else if (getAccelRange()==2){
											mSensitivityMatrixWRAccel = SensitivityMatrixWideRangeAccel8gShimmer3;
											mAlignmentMatrixWRAccel = AlignmentMatrixWideRangeAccelShimmer3;
											mOffsetVectorWRAccel = OffsetVectorWideRangeAccelShimmer3;
										} else if (getAccelRange()==3){
											mSensitivityMatrixWRAccel = SensitivityMatrixWideRangeAccel16gShimmer3;
											mAlignmentMatrixWRAccel = AlignmentMatrixWideRangeAccelShimmer3;
											mOffsetVectorWRAccel = OffsetVectorWideRangeAccelShimmer3;
										} else if (getAccelRange()==0){
											mSensitivityMatrixWRAccel = SensitivityMatrixWideRangeAccel2gShimmer3;
											mAlignmentMatrixWRAccel = AlignmentMatrixWideRangeAccelShimmer3;
											mOffsetVectorWRAccel = OffsetVectorWideRangeAccelShimmer3;
										}
									}
								}
								mTransactionCompleted=true;
								mWaitForAck=false;
								getListofInstructions().remove(0);
								setInstructionStackLock(false);
							} 
							
							else if (mCurrentCommand==SET_ACCEL_CALIBRATION_COMMAND) {
								stopTimerWaitForAckOrResp(); //cancel the ack timer
								retrievekinematiccalibrationparametersfrompacket(Arrays.copyOfRange(getListofInstructions().get(0), 1, getListofInstructions().get(0).length), ACCEL_CALIBRATION_RESPONSE);	
								mTransactionCompleted = true;
								mWaitForAck=false;
								getListofInstructions().remove(0);
								setInstructionStackLock(false);
								}
							else if (mCurrentCommand==SET_GYRO_CALIBRATION_COMMAND) {
								stopTimerWaitForAckOrResp(); //cancel the ack timer
								retrievekinematiccalibrationparametersfrompacket(Arrays.copyOfRange(getListofInstructions().get(0), 1, getListofInstructions().get(0).length), GYRO_CALIBRATION_RESPONSE);	
								mTransactionCompleted = true;
								mWaitForAck=false;
								getListofInstructions().remove(0);
								setInstructionStackLock(false);
							}
							else if (mCurrentCommand==SET_MAG_CALIBRATION_COMMAND) {
								stopTimerWaitForAckOrResp(); //cancel the ack timer
								retrievekinematiccalibrationparametersfrompacket(Arrays.copyOfRange(getListofInstructions().get(0), 1, getListofInstructions().get(0).length), MAG_CALIBRATION_RESPONSE);	
								mTransactionCompleted = true;
								mWaitForAck=false;
								getListofInstructions().remove(0);
								setInstructionStackLock(false);
							}
							else if (mCurrentCommand==SET_LSM303DLHC_ACCEL_CALIBRATION_COMMAND) {
								stopTimerWaitForAckOrResp(); //cancel the ack timer
								retrievekinematiccalibrationparametersfrompacket(Arrays.copyOfRange(getListofInstructions().get(0), 1, getListofInstructions().get(0).length), LSM303DLHC_ACCEL_CALIBRATION_RESPONSE);	
								mTransactionCompleted = true;
								mWaitForAck=false;
								getListofInstructions().remove(0);
								setInstructionStackLock(false);
							}
							
							else if (mCurrentCommand==SET_MPU9150_GYRO_RANGE_COMMAND) {
								stopTimerWaitForAckOrResp(); //cancel the ack timer
								mGyroRange=(int)(((byte[])getListofInstructions().get(0))[1]);
								if (mDefaultCalibrationParametersGyro == true){
									if(mHardwareVersion == HW_ID.SHIMMER_3){
										mAlignmentMatrixGyroscope = AlignmentMatrixGyroShimmer3;
										mOffsetVectorGyroscope = OffsetVectorGyroShimmer3;
										if (mGyroRange==0){
											mSensitivityMatrixGyroscope = SensitivityMatrixGyro250dpsShimmer3;

										} else if (mGyroRange==1){
											mSensitivityMatrixGyroscope = SensitivityMatrixGyro500dpsShimmer3;

										} else if (mGyroRange==2){
											mSensitivityMatrixGyroscope = SensitivityMatrixGyro1000dpsShimmer3;

										} else if (mGyroRange==3){
											mSensitivityMatrixGyroscope = SensitivityMatrixGyro2000dpsShimmer3;

										}
									}
								}
								mTransactionCompleted=true;
								mWaitForAck=false;
								getListofInstructions().remove(0);
								setInstructionStackLock(false);
							} 
							else if (mCurrentCommand==SET_MAG_SAMPLING_RATE_COMMAND){
								stopTimerWaitForAckOrResp(); //cancel the ack timer
								mTransactionCompleted = true;
								mLSM303MagRate = mTempIntValue;
								mWaitForAck = false;
								getListofInstructions().remove(0);
								setInstructionStackLock(false);
							} else if (mCurrentCommand==GET_ACCEL_SAMPLING_RATE_COMMAND){
								mWaitForAck=false;
								mWaitForResponse=true;
								getListofInstructions().remove(0);
							} else if (mCurrentCommand==SET_ACCEL_SAMPLING_RATE_COMMAND){
								stopTimerWaitForAckOrResp(); //cancel the ack timer
								mTransactionCompleted = true;
								mLSM303DigitalAccelRate = mTempIntValue;
								mWaitForAck = false;
								getListofInstructions().remove(0);
								setInstructionStackLock(false);
							} else if (mCurrentCommand==SET_MPU9150_SAMPLING_RATE_COMMAND){
								stopTimerWaitForAckOrResp(); //cancel the ack timer
								mTransactionCompleted = true;
								mMPU9150GyroAccelRate = mTempIntValue;
								mWaitForAck = false;
								getListofInstructions().remove(0);
								setInstructionStackLock(false);
							} else if (mCurrentCommand==SET_EXG_REGS_COMMAND){
								stopTimerWaitForAckOrResp(); //cancel the ack timer
								byte[] bytearray = getListofInstructions().get(0);
								if (bytearray[1]==EXG_CHIP1){  //0 = CHIP 1
									System.arraycopy(bytearray, 4, mEXG1RegisterArray, 0, 10);
									mEXG1RateSetting = mEXG1RegisterArray[0] & 7;
									mEXG1CH1GainSetting = (mEXG1RegisterArray[3] >> 4) & 7;
									mEXG1CH1GainValue = convertEXGGainSettingToValue(mEXG1CH1GainSetting);
									mEXG1CH2GainSetting = (mEXG1RegisterArray[4] >> 4) & 7;
									mEXG1CH2GainValue = convertEXGGainSettingToValue(mEXG1CH2GainSetting);
									mEXGReferenceElectrode = mEXG1RegisterArray[5] & 0x0f;
								
								} else if (bytearray[1]==EXG_CHIP2){ //1 = CHIP 2
									System.arraycopy(bytearray, 4, mEXG2RegisterArray, 0, 10);
									mEXG2RateSetting = mEXG2RegisterArray[0] & 7;
									mEXG2CH1GainSetting = (mEXG2RegisterArray[3] >> 4) & 7;
									mEXG2CH1GainValue = convertEXGGainSettingToValue(mEXG2CH1GainSetting);
									mEXG2CH2GainSetting = (mEXG2RegisterArray[4] >> 4) & 7;
									mEXG2CH2GainValue = convertEXGGainSettingToValue(mEXG2CH2GainSetting);
								}
								mTransactionCompleted = true;
								mWaitForAck = false;
								getListofInstructions().remove(0);
								setInstructionStackLock(false);
							} else if (mCurrentCommand==SET_SENSORS_COMMAND) {
								stopTimerWaitForAckOrResp(); //cancel the ack timer
								mWaitForAck=false;
								mEnabledSensors=tempEnabledSensors;
								byteStack.clear(); // Always clear the packetStack after setting the sensors, this is to ensure a fresh start
								mTransactionCompleted=true;
								getListofInstructions().remove(0);
								setInstructionStackLock(false);
							}
							else if (mCurrentCommand==SET_MAG_GAIN_COMMAND){
								stopTimerWaitForAckOrResp(); //cancel the ack timer
								mTransactionCompleted = true;
								mWaitForAck = false;
								mMagRange=(int)((byte [])getListofInstructions().get(0))[1];
								if (mDefaultCalibrationParametersMag == true){
									if(mHardwareVersion == HW_ID.SHIMMER_3){
										mAlignmentMatrixMagnetometer = AlignmentMatrixMagShimmer3;
										mOffsetVectorMagnetometer = OffsetVectorMagShimmer3;
										if (mMagRange==1){
											mSensitivityMatrixMagnetometer = SensitivityMatrixMag1p3GaShimmer3;
										} else if (mMagRange==2){
											mSensitivityMatrixMagnetometer = SensitivityMatrixMag1p9GaShimmer3;
										} else if (mMagRange==3){
											mSensitivityMatrixMagnetometer = SensitivityMatrixMag2p5GaShimmer3;
										} else if (mMagRange==4){
											mSensitivityMatrixMagnetometer = SensitivityMatrixMag4GaShimmer3;
										} else if (mMagRange==5){
											mSensitivityMatrixMagnetometer = SensitivityMatrixMag4p7GaShimmer3;
										} else if (mMagRange==6){
											mSensitivityMatrixMagnetometer = SensitivityMatrixMag5p6GaShimmer3;
										} else if (mMagRange==7){
											mSensitivityMatrixMagnetometer = SensitivityMatrixMag8p1GaShimmer3;
										}
									}
								}
								getListofInstructions().remove(0);
								setInstructionStackLock(false);
							}
							else if (mCurrentCommand==GET_ACCEL_CALIBRATION_COMMAND || mCurrentCommand==GET_GYRO_CALIBRATION_COMMAND || mCurrentCommand==GET_MAG_CALIBRATION_COMMAND || mCurrentCommand==GET_ALL_CALIBRATION_COMMAND) {
								mWaitForResponse = true;
								mWaitForAck=false;
								getListofInstructions().remove(0);
							}
							else if (mCurrentCommand==GET_SHIMMER_VERSION_COMMAND_NEW) {
								mWaitForResponse = true;
								mWaitForAck=false;
								getListofInstructions().remove(0);
							}
							else if (mCurrentCommand==GET_BMP180_CALIBRATION_COEFFICIENTS_COMMAND) {
								mWaitForResponse = true;
								mWaitForAck=false;
								getListofInstructions().remove(0);
							} else if (mCurrentCommand==GET_SHIMMER_VERSION_COMMAND) {
								mWaitForResponse = true;
								mWaitForAck=false;
								getListofInstructions().remove(0);
							} else if (mCurrentCommand==GET_EXG_REGS_COMMAND){
								byte[] bytearray = getListofInstructions().get(0);
								mTempChipID = bytearray[1];
								mWaitForResponse = true;
								mWaitForAck=false;
								getListofInstructions().remove(0);
							}
							else if (mCurrentCommand==SET_ECG_CALIBRATION_COMMAND){
								//mGSRRange=mTempIntValue;
								mDefaultCalibrationParametersECG = false;
								OffsetECGLALL=(double)((((byte[])getListofInstructions().get(0))[0]&0xFF)<<8)+(((byte[])getListofInstructions().get(0))[1]&0xFF);
								GainECGLALL=(double)((((byte[])getListofInstructions().get(0))[2]&0xFF)<<8)+(((byte[])getListofInstructions().get(0))[3]&0xFF);
								OffsetECGRALL=(double)((((byte[])getListofInstructions().get(0))[4]&0xFF)<<8)+(((byte[])getListofInstructions().get(0))[5]&0xFF);
								GainECGRALL=(double)((((byte[])getListofInstructions().get(0))[6]&0xFF)<<8)+(((byte[])getListofInstructions().get(0))[7]&0xFF);
								stopTimerWaitForAckOrResp(); //cancel the ack timer
								mTransactionCompleted = true;
								mWaitForAck=false;
								getListofInstructions().remove(0);
								setInstructionStackLock(false);
							}
							else if (mCurrentCommand==SET_EMG_CALIBRATION_COMMAND){
								//mGSRRange=mTempIntValue;
								mDefaultCalibrationParametersEMG = false;
								OffsetEMG=(double)((((byte[])getListofInstructions().get(0))[0]&0xFF)<<8)+(((byte[])getListofInstructions().get(0))[1]&0xFF);
								GainEMG=(double)((((byte[])getListofInstructions().get(0))[2]&0xFF)<<8)+(((byte[])getListofInstructions().get(0))[3]&0xFF);
								mTransactionCompleted = true;
								mWaitForAck=false;
								stopTimerWaitForAckOrResp(); //cancel the ack timer
								getListofInstructions().remove(0);
								setInstructionStackLock(false);
							}
							else if(mCurrentCommand==SET_DERIVED_CHANNEL_BYTES){
								mTransactionCompleted = true;
								mWaitForAck=false;
								stopTimerWaitForAckOrResp(); //cancel the ack timer
								mDerivedSensors=(long)(((((byte[])getListofInstructions().get(0))[0]&0xFF)<<16) + ((((byte[])getListofInstructions().get(0))[1]&0xFF)<<8)+(((byte[])getListofInstructions().get(0))[2]&0xFF));
								getListofInstructions().remove(0);
								setInstructionStackLock(false);
							}
							else if(mCurrentCommand==SET_SHIMMERNAME_COMMAND){
								mTransactionCompleted = true;
								mWaitForAck=false;
								stopTimerWaitForAckOrResp(); //cancel the ack timer
								byte[] instruction =getListofInstructions().get(0);
								byte[] nameArray = new byte[instruction[1]];
								System.arraycopy(instruction, 2, nameArray, 0, instruction[1]);
								String name = new String(nameArray);
								setShimmerUserAssignedName(name);
								getListofInstructions().remove(0);
								setInstructionStackLock(false);
							}
							else if(mCurrentCommand==SET_EXPID_COMMAND){
								mTransactionCompleted = true;
								mWaitForAck=false;
								stopTimerWaitForAckOrResp(); //cancel the ack timer
								byte[] instruction =getListofInstructions().get(0);
								byte[] nameArray = new byte[instruction[1]];
								System.arraycopy(instruction, 2, nameArray, 0, instruction[1]);
								String name = new String(nameArray);
								setExperimentName(name);
								getListofInstructions().remove(0);
								setInstructionStackLock(false);
							}
							else if(mCurrentCommand==SET_RWC_COMMAND){
								mTransactionCompleted = true;
								mWaitForAck=false;
								stopTimerWaitForAckOrResp(); //cancel the ack timer
								byte[] instruction =getListofInstructions().get(0);
								byte[] byteTime = new byte[8];
								System.arraycopy(instruction, 1, byteTime, 0, 8);
								mSetRWC = byteTime; 
								getListofInstructions().remove(0);
								setInstructionStackLock(false);
							}
							else if(mCurrentCommand==SET_CONFIGTIME_COMMAND){
								mTransactionCompleted = true;
								mWaitForAck=false;
								stopTimerWaitForAckOrResp(); //cancel the ack timer
								byte[] instruction =getListofInstructions().get(0);
								byte[] timeArray = new byte[instruction[1]];
								System.arraycopy(instruction, 2, timeArray, 0, instruction[1]);
								String time = new String(timeArray);
								setConfigTime(Long.parseLong(time));
								getListofInstructions().remove(0);
								setInstructionStackLock(false);
							}
							else if(mCurrentCommand==SET_CENTER_COMMAND){
								mTransactionCompleted = true;
								mWaitForAck=false;
								stopTimerWaitForAckOrResp(); //cancel the ack timer
								byte[] instruction =getListofInstructions().get(0);
								byte[] centerArray = new byte[instruction[1]];
								System.arraycopy(instruction, 2, centerArray, 0, instruction[1]);
								String center = new String(centerArray);
								//setConfigTime(Long.parseLong(time));
								setCenter(center);
								getListofInstructions().remove(0);
								setInstructionStackLock(false);
							}
							else if(mCurrentCommand==SET_TRIAL_CONFIG_COMMAND){
								mTransactionCompleted = true;
								mWaitForAck=false;
								stopTimerWaitForAckOrResp(); //cancel the ack timer
								byte[] instruction =getListofInstructions().get(0);
								byte[] dataArray = new byte[3];
								System.arraycopy(instruction, 1, dataArray, 0, 3);
								//settrial
								getListofInstructions().remove(0);
								setInstructionStackLock(false);
							}
							else if (mCurrentCommand==TOGGLE_LED_COMMAND){
								//mGSRRange=mTempIntValue;
								mTransactionCompleted = true;
								mWaitForAck=false;
								stopTimerWaitForAckOrResp(); //cancel the ack timer
								getListofInstructions().remove(0);
								setInstructionStackLock(false);
							}
							else if (mCurrentCommand==GET_BAUD_RATE_COMMAND) {
								mWaitForResponse = true;
								mWaitForAck=false;
								getListofInstructions().remove(0);
							}
							else if (mCurrentCommand==SET_BAUD_RATE_COMMAND) {
								mTransactionCompleted = true;
								mWaitForAck=false;
								stopTimerWaitForAckOrResp(); //cancel the ack timer
								mBluetoothBaudRate=(int)((byte [])getListofInstructions().get(0))[1];
								getListofInstructions().remove(0);
								setInstructionStackLock(false);
//								reconnect();
							}
							else if(mCurrentCommand==GET_DAUGHTER_CARD_ID_COMMAND){
								mWaitForResponse = true;
								mWaitForAck=false;
								getListofInstructions().remove(0);
							}
							else if(mCurrentCommand==GET_DIR_COMMAND){
								mWaitForResponse = true;
								mWaitForAck=false;
								getListofInstructions().remove(0);
							}
							else if (mCurrentCommand==GET_VBATT_COMMAND) {
								mWaitForAck=false;
								mWaitForResponse=true;
								getListofInstructions().remove(0);
								
							}
							else if(mCurrentCommand==GET_STATUS_COMMAND){
								mWaitForResponse = true;
								mWaitForAck=false;
								getListofInstructions().remove(0);
							}
							else if(mCurrentCommand==GET_DERIVED_CHANNEL_BYTES){
								mWaitForResponse = true;
								mWaitForAck=false;
								getListofInstructions().remove(0);
							}
							else if(mCurrentCommand==GET_RWC_COMMAND){
								mWaitForResponse = true;
								mWaitForAck=false;
								getListofInstructions().remove(0);
							}
							else if(mCurrentCommand==GET_TRIAL_CONFIG_COMMAND){
								mWaitForResponse = true;
								mWaitForAck=false;
								getListofInstructions().remove(0);
							}

							else if(mCurrentCommand==GET_CENTER_COMMAND){
								mWaitForResponse = true;
								mWaitForAck=false;
								getListofInstructions().remove(0);
							}
							
							else if(mCurrentCommand==GET_SHIMMERNAME_COMMAND){
								mWaitForResponse = true;
								mWaitForAck=false;
								getListofInstructions().remove(0);
							}
							
							else if(mCurrentCommand==GET_EXPID_COMMAND){
								mWaitForResponse = true;
								mWaitForAck=false;
								getListofInstructions().remove(0);
							}
//							else if(mCurrentCommand==SET_MYID_COMMAND){
//								
//							}
//							else if(mCurrentCommand==GET_MYID_COMMAND){
//								
//							}
//							else if(mCurrentCommand==SET_NSHIMMER_COMMAND){
//								
//							}
//							else if(mCurrentCommand==GET_NSHIMMER_COMMAND){
//								
//							}							
							else if(mCurrentCommand==GET_CONFIGTIME_COMMAND){
								mWaitForResponse = true;
								mWaitForAck=false;
								getListofInstructions().remove(0);
							}

						}


					}
				} else if (mWaitForResponse==true && !mIsStreaming) {
					if (mFirstTime){
						while (availableBytes()!=0){
								int avaible = availableBytes();
								if (bytesToBeRead()){
									tb=readBytes(1);
									String msg = "First Time : " + Util.bytesToHexStringWithSpacesFormatted(tb);
									printLogDataForDebugging(msg);
								}
							
						}
						
					} else if (availableBytes()!=0){

						tb=readBytes(1);
						mIamAlive = true;
						String msg="";
						//msg = "rxb : " + Arrays.toString(tb);
						//printLogDataForDebugging(msg);
						
						if (tb[0]==FW_VERSION_RESPONSE){
							
							stopTimerWaitForAckOrResp(); //cancel the ack timer

							try {
								Thread.sleep(200);	// Wait to ensure the packet has been fully received
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							byte[] bufferInquiry = new byte[6]; 
							bufferInquiry = readBytes(6);
							mFirmwareIdentifier=(int)((bufferInquiry[1]&0xFF)<<8)+(int)(bufferInquiry[0]&0xFF);
//							mFWVersion=(double)((bufferInquiry[3]&0xFF)<<8)+(double)(bufferInquiry[2]&0xFF)+((double)((bufferInquiry[4]&0xFF))/10);
							mFirmwareVersionMajor = (int)((bufferInquiry[3]&0xFF)<<8)+(int)(bufferInquiry[2]&0xFF);
							mFirmwareVersionMinor = ((int)((bufferInquiry[4]&0xFF)));
							mFirmwareVersionInternal=(int)(bufferInquiry[5]&0xFF);
							
//							if (((double)((bufferInquiry[4]&0xFF))/10)==0){
//								mFirmwareVersionParsed = "BtStream " + Double.toString(mFWVersion) + "."+ Integer.toString(mFirmwareVersionRelease);
//							} else {
//								mFirmwareVersionParsed = "BtStream " + Double.toString(mFWVersion) + "."+ Integer.toString(mFirmwareVersionRelease);
//							}
							
//							if(mFirmwareIdentifier==1){ //BTStream
//								if((mFirmwareVersionMajor==0 && mFirmwareVersionMinor==1) || (mFirmwareVersionMajor==1 && mFirmwareVersionMinor==2 && mHardwareVersion==HW_ID.SHIMMER_2R))
//									mFirmwareVersionCode = 1;
//								else if(mFirmwareVersionMajor==0 && mFirmwareVersionMinor==2)
//									mFirmwareVersionCode = 2;
//								else if(mFirmwareVersionMajor==0 && mFirmwareVersionMinor==3)
//									mFirmwareVersionCode = 3;
//								else if(mFirmwareVersionMajor==0 && mFirmwareVersionMinor==4)
//									mFirmwareVersionCode = 4;
//								else if(mFirmwareVersionMajor==0 && mFirmwareVersionMinor>=5)
//									mFirmwareVersionCode = 5;
//								
//								mFirmwareVersionParsed = "BtStream v" + mFirmwareVersionMajor + "." + mFirmwareVersionMinor + "."+ mFirmwareVersionInternal;
//							}
//							else if(mFirmwareIdentifier==FW_ID.SHIMMER3.LOGANDSTREAM){ //LogAndStream
//								if(mFirmwareVersionMajor==0 && mFirmwareVersionMinor==1)
//									mFirmwareVersionCode = 3;
//								else if(mFirmwareVersionMajor==0 && mFirmwareVersionMinor==2)
//									mFirmwareVersionCode = 4;
//								else if(mFirmwareVersionMajor==0 && mFirmwareVersionMinor>=3)
//									mFirmwareVersionCode = 5;
//								
//								mFirmwareVersionParsed = "LogAndStream v" + mFirmwareVersionMajor + "." + mFirmwareVersionMinor + "."+ mFirmwareVersionInternal;
//							}
							
							ShimmerVerObject shimmerVerObject = new ShimmerVerObject(mHardwareVersion, mFirmwareIdentifier, mFirmwareVersionMajor, mFirmwareVersionMinor, mFirmwareVersionInternal);
							setShimmerVersionInfo(shimmerVerObject);

							printLogDataForDebugging("FW Version Response Received. FW Code: " + mFirmwareVersionCode);
							msg = "FW Version Response Received: " + mFirmwareVersionParsed;
							printLogDataForDebugging(msg);
							getListofInstructions().remove(0);
							setInstructionStackLock(false);
							mTransactionCompleted=true;
							if (mHardwareVersion == HW_ID.SHIMMER_2R){
								initializeShimmer2R();
							} else if (mHardwareVersion == HW_ID.SHIMMER_3) {
								initializeShimmer3();
							}
							
							startTimerCheckIfAlive();
							
//							readShimmerVersion();
						} else if (tb[0]==BMP180_CALIBRATION_COEFFICIENTS_RESPONSE){
							stopTimerWaitForAckOrResp(); //cancel the ack timer
							mWaitForResponse=false;
							mTransactionCompleted=true;
							
							//get pressure
							try {
								Thread.sleep(100);	// Due to the nature of the Bluetooth SPP stack a delay has been added to ensure the buffer is filled before it is read
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							
							byte[] pressureResoRes = new byte[22]; 
						
							pressureResoRes = readBytes(22);
							mPressureCalRawParams = new byte[23];
							System.arraycopy(pressureResoRes, 0, mPressureCalRawParams, 1, 22);
							mPressureCalRawParams[0] = tb[0];
							retrievepressurecalibrationparametersfrompacket(pressureResoRes,tb[0]);
							msg = "BMP180 Response Received";
							printLogDataForDebugging(msg);
							setInstructionStackLock(false);
						} else if (tb[0]==INQUIRY_RESPONSE) {
							stopTimerWaitForAckOrResp(); //cancel the ack timer
							try {
								Thread.sleep(500);	// Wait to ensure the packet has been fully received
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							List<Byte> buffer = new  ArrayList<Byte>();
							if (!(mHardwareVersion==HW_ID.SHIMMER_3))
							{
								 for (int i = 0; i < 5; i++)
	                                {
	                                    // get Sampling rate, accel range, config setup byte0, num chans and buffer size
	                                    buffer.add(readByte());
	                                }
								 
	                                for (int i = 0; i < (int)buffer.get(3); i++)
	                                {
	                                    // read each channel type for the num channels
	                                	buffer.add(readByte());
	                                }
							}
							else
							{
								  for (int i = 0; i < 8; i++)
	                                {
	                                    // get Sampling rate, accel range, config setup byte0, num chans and buffer size
									  buffer.add(readByte());
	                                }
	                                for (int i = 0; i < (int)buffer.get(6); i++)
	                                {
	                                    // read each channel type for the num channels
	                                	buffer.add(readByte());
	                                }
							}
							byte[] bufferInquiry = new byte[buffer.size()];
							for (int i = 0; i < bufferInquiry.length; i++) {
								bufferInquiry[i] = (byte) buffer.get(i);
							}
								
							msg = "Inquiry Response Received: " + Util.bytesToHexStringWithSpacesFormatted(bufferInquiry);
							printLogDataForDebugging(msg);
							interpretInqResponse(bufferInquiry);
							inquiryDone();
							mWaitForResponse = false;
							mTransactionCompleted=true;
							setInstructionStackLock(false);
						} else if(tb[0] == GSR_RANGE_RESPONSE) {
							stopTimerWaitForAckOrResp(); //cancel the ack timer
							mWaitForResponse=false;
							mTransactionCompleted=true;
							byte[] bufferGSRRange = readBytes(1); 
							mGSRRange=bufferGSRRange[0];
							msg = "GSR Range Response Received: " + Util.bytesToHexStringWithSpacesFormatted(bufferGSRRange);
							printLogDataForDebugging(msg);
							setInstructionStackLock(false);
						} else if(tb[0] == MAG_SAMPLING_RATE_RESPONSE) {
							stopTimerWaitForAckOrResp(); //cancel the ack timer
							mWaitForResponse=false;
							mTransactionCompleted=true;
							byte[] bufferAns = readBytes(1); 
							mLSM303MagRate=bufferAns[0];
							msg = "Mag Sampling Rate Response Received: " + Util.bytesToHexStringWithSpacesFormatted(bufferAns);
							printLogDataForDebugging(msg);
							setInstructionStackLock(false);
						} else if(tb[0] == ACCEL_SAMPLING_RATE_RESPONSE) {
							stopTimerWaitForAckOrResp(); //cancel the ack timer
							mWaitForResponse=false;
							mTransactionCompleted=true;
							byte[] bufferAns = readBytes(1); 
							mLSM303DigitalAccelRate=bufferAns[0];
							setInstructionStackLock(false);
						}else if(tb[0] == VBATT_RESPONSE) {
							stopTimerWaitForAckOrResp(); //cancel the ack timer
							mWaitForResponse=false;
							mTransactionCompleted=true;
							byte[] bufferAns = readBytes(3); 
							mBattVoltage=Integer.toString(((bufferAns[1]&0xFF)<<8)+(bufferAns[0]&0xFF));
							consolePrintLn("Batt data " + mBattVoltage);
							setInstructionStackLock(false);
						}  
						
						else if(tb[0] == 	EXG_REGS_RESPONSE){
							stopTimerWaitForAckOrResp(); //cancel the ack timer
							mWaitForResponse=false;
							mTransactionCompleted=true;
							try {
								Thread.sleep(300);	// Due to the nature of the Bluetooth SPP stack a delay has been added to ensure the buffer is filled before it is read
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							byte[] bufferAns = readBytes(11);
							if (mTempChipID==0){
								System.arraycopy(bufferAns, 1, mEXG1RegisterArray, 0, 10);
								// retrieve the gain and rate from the the registers
								mEXG1RateSetting = mEXG1RegisterArray[0] & 7;
								mEXG1CH1GainSetting = (mEXG1RegisterArray[3] >> 4) & 7;
								mEXG1CH1GainValue = convertEXGGainSettingToValue(mEXG1CH1GainSetting);
								mEXG1CH2GainSetting = (mEXG1RegisterArray[4] >> 4) & 7;
								mEXG1CH2GainValue = convertEXGGainSettingToValue(mEXG1CH2GainSetting);
								mEXGReferenceElectrode = mEXG1RegisterArray[5] & 0x0F;
								mEXG1LeadOffCurrentMode = mEXG1RegisterArray[2] & 1;
								mEXG1Comparators = mEXG1RegisterArray[1] & 0x40;								
								mEXGRLDSense = mEXG1RegisterArray[5] & 0x10;
								mEXG1LeadOffSenseSelection = mEXG1RegisterArray[6] & 0x0f;
								mEXGLeadOffDetectionCurrent = (mEXG1RegisterArray[2] >> 2) & 3;
								mEXGLeadOffComparatorTreshold = (mEXG1RegisterArray[2] >> 5) & 7;
							} else if (mTempChipID==1){
								System.arraycopy(bufferAns, 1, mEXG2RegisterArray, 0, 10);						
								mEXG2RateSetting = mEXG2RegisterArray[0] & 7;
								mEXG2CH1GainSetting = (mEXG2RegisterArray[3] >> 4) & 7;
								mEXG2CH1GainValue = convertEXGGainSettingToValue(mEXG2CH1GainSetting);
								mEXG2CH2GainSetting = (mEXG2RegisterArray[4] >> 4) & 7;
								mEXG2CH2GainValue = convertEXGGainSettingToValue(mEXG2CH2GainSetting);
								mEXG2LeadOffCurrentMode = mEXG2RegisterArray[2] & 1;
								mEXG2Comparators = mEXG2RegisterArray[1] & 0x40;
								mEXG2LeadOffSenseSelection = mEXG2RegisterArray[6] & 0x0f;
							}
							if(mEXG1Comparators == 0 && mEXG2Comparators == 0 && mEXG1LeadOffSenseSelection == 0 && mEXG2LeadOffSenseSelection == 0){
								mLeadOffDetectionMode = 0; // Off
							}
							else if(mEXG1LeadOffCurrentMode == mEXG2LeadOffCurrentMode && mEXG1LeadOffCurrentMode == 0){
								mLeadOffDetectionMode = 1; // DC Current
							}
							else if(mEXG1LeadOffCurrentMode == mEXG2LeadOffCurrentMode && mEXG1LeadOffCurrentMode == 1){
								mLeadOffDetectionMode = 2; // AC Current. Not supported yet
							}
							setInstructionStackLock(false);
						} else if(tb[0] == MAG_GAIN_RESPONSE) {
							stopTimerWaitForAckOrResp(); //cancel the ack timer
							mWaitForResponse=false;
							mTransactionCompleted=true;
							byte[] bufferAns = readBytes(1); 
							mMagRange=bufferAns[0];
							setInstructionStackLock(false);
						} else if(tb[0] == LSM303DLHC_ACCEL_HRMODE_RESPONSE) {
							stopTimerWaitForAckOrResp(); //cancel the ack timer
							mWaitForResponse=false;
							mTransactionCompleted=true;
							byte[] bufferAns = readBytes(1);
							setInstructionStackLock(false);
						} else if(tb[0] == LSM303DLHC_ACCEL_LPMODE_RESPONSE) {
							stopTimerWaitForAckOrResp(); //cancel the ack timer
							mWaitForResponse=false;
							mTransactionCompleted=true;
							byte[] bufferAns = readBytes(1);
							setInstructionStackLock(false);
						} else if(tb[0]==BUFFER_SIZE_RESPONSE) {
							stopTimerWaitForAckOrResp(); //cancel the ack timer
							mWaitForResponse=false;
							mTransactionCompleted=true;
							byte[] byteled = readBytes(1);
							mBufferSize = byteled[0] & 0xFF;
							setInstructionStackLock(false);
						} else if(tb[0]==BLINK_LED_RESPONSE) {
							stopTimerWaitForAckOrResp(); //cancel the ack timer
							mWaitForResponse=false;
							mTransactionCompleted=true;
							byte[] byteled = readBytes(1);
							mCurrentLEDStatus = byteled[0]&0xFF;
							setInstructionStackLock(false);
						} else if(tb[0]==ACCEL_SENSITIVITY_RESPONSE) {
							stopTimerWaitForAckOrResp(); //cancel the ack timer
							mWaitForResponse=false;
							mTransactionCompleted=true;
							byte[] bufferAccelSensitivity = readBytes(1);
							mAccelRange=bufferAccelSensitivity[0];
							if (mDefaultCalibrationParametersAccel == true){
								if (mHardwareVersion != HW_ID.SHIMMER_3){
									if (getAccelRange()==0){
										mSensitivityMatrixAnalogAccel = SensitivityMatrixAccel1p5gShimmer2; 
									} else if (getAccelRange()==1){
										mSensitivityMatrixAnalogAccel = SensitivityMatrixAccel2gShimmer2; 
									} else if (getAccelRange()==2){
										mSensitivityMatrixAnalogAccel = SensitivityMatrixAccel4gShimmer2; 
									} else if (getAccelRange()==3){
										mSensitivityMatrixAnalogAccel = SensitivityMatrixAccel6gShimmer2; 
									}
								} else if(mHardwareVersion == HW_ID.SHIMMER_3){
									if (getAccelRange()==0){
										mSensitivityMatrixAnalogAccel = SensitivityMatrixLowNoiseAccel2gShimmer3;
										mAlignmentMatrixAnalogAccel = AlignmentMatrixLowNoiseAccelShimmer3;
										mOffsetVectorAnalogAccel = OffsetVectorLowNoiseAccelShimmer3;
									} else if (getAccelRange()==1){
										mSensitivityMatrixAnalogAccel = SensitivityMatrixWideRangeAccel4gShimmer3;
										mAlignmentMatrixAnalogAccel = AlignmentMatrixWideRangeAccelShimmer3;
										mOffsetVectorAnalogAccel = OffsetVectorWideRangeAccelShimmer3;
									} else if (getAccelRange()==2){
										mSensitivityMatrixAnalogAccel = SensitivityMatrixWideRangeAccel8gShimmer3;
										mAlignmentMatrixAnalogAccel = AlignmentMatrixWideRangeAccelShimmer3;
										mOffsetVectorAnalogAccel = OffsetVectorWideRangeAccelShimmer3;
									} else if (getAccelRange()==3){
										mSensitivityMatrixAnalogAccel = SensitivityMatrixWideRangeAccel16gShimmer3;
										mAlignmentMatrixAnalogAccel = AlignmentMatrixWideRangeAccelShimmer3;
										mOffsetVectorAnalogAccel = OffsetVectorWideRangeAccelShimmer3;
									}
								}
							}   
							getListofInstructions().remove(0);
							setInstructionStackLock(false);
						} else if(tb[0]==MPU9150_GYRO_RANGE_RESPONSE) {
							stopTimerWaitForAckOrResp(); //cancel the ack timer
							mWaitForResponse=false;
							mTransactionCompleted=true;
							byte[] bufferGyroSensitivity = readBytes(1);
							mGyroRange=bufferGyroSensitivity[0];
							if (mDefaultCalibrationParametersGyro == true){
								if(mHardwareVersion == HW_ID.SHIMMER_3){
									mAlignmentMatrixGyroscope = AlignmentMatrixGyroShimmer3;
									mOffsetVectorGyroscope = OffsetVectorGyroShimmer3;
									if (mGyroRange==0){
										mSensitivityMatrixGyroscope = SensitivityMatrixGyro250dpsShimmer3;

									} else if (mGyroRange==1){
										mSensitivityMatrixGyroscope = SensitivityMatrixGyro500dpsShimmer3;

									} else if (mGyroRange==2){
										mSensitivityMatrixGyroscope = SensitivityMatrixGyro1000dpsShimmer3;

									} else if (mGyroRange==3){
										mSensitivityMatrixGyroscope = SensitivityMatrixGyro2000dpsShimmer3;
									}
								}
							}   
							getListofInstructions().remove(0);
							setInstructionStackLock(false);
						}else if (tb[0]==SAMPLING_RATE_RESPONSE) {
							stopTimerWaitForAckOrResp(); //cancel the ack timer
							mWaitForResponse=false;
							if(mIsStreaming==false) {
								if (mHardwareVersion==HW_ID.SHIMMER_2R || mHardwareVersion==HW_ID.SHIMMER_2){    
									byte[] bufferSR = readBytes(1);
									if (mCurrentCommand==GET_SAMPLING_RATE_COMMAND) { // this is a double check, not necessary 
										double val=(double)(bufferSR[0] & (byte) ACK_COMMAND_PROCESSED);
										mShimmerSamplingRate=1024/val;
									}
								} else if (mHardwareVersion==HW_ID.SHIMMER_3){
									byte[] bufferSR = readBytes(2); //read the sampling rate
									mShimmerSamplingRate = 32768/(double)((int)(bufferSR[0] & 0xFF) + ((int)(bufferSR[1] & 0xFF) << 8));
								}
							}

							msg = "Sampling Rate Response Received: " + Double.toString(mShimmerSamplingRate);
							printLogDataForDebugging(msg);
							mTransactionCompleted=true;
							getListofInstructions().remove(0);
							setInstructionStackLock(false);
						} else if (tb[0]==ACCEL_CALIBRATION_RESPONSE ) {
							stopTimerWaitForAckOrResp(); //cancel the ack timer
								try {
								Thread.sleep(100);	// Due to the nature of the Bluetooth SPP stack a delay has been added to ensure the buffer is filled before it is read
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							mWaitForResponse=false;
							byte[] bufferCalibrationParameters = readBytes(21);
							
							mAccelCalRawParams = new byte[22];
							System.arraycopy(bufferCalibrationParameters, 0, mAccelCalRawParams, 1, 21);
							mAccelCalRawParams[0] = tb[0];
							
							int packetType=tb[0];
							retrievekinematiccalibrationparametersfrompacket(bufferCalibrationParameters, packetType);
							msg = "Accel Calibration Response Received";
							printLogDataForDebugging(msg);
							mTransactionCompleted=true;
							setInstructionStackLock(false);
						}  else if (tb[0]==ALL_CALIBRATION_RESPONSE ) {
							stopTimerWaitForAckOrResp(); //cancel the ack timer
							mWaitForResponse=false;
					
							try {
								Thread.sleep(100);	// Due to the nature of the Bluetooth SPP stack a delay has been added to ensure the buffer is filled before it is read
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							if (mHardwareVersion != HW_ID.SHIMMER_3){
								byte[] bufferCalibrationParameters = readBytes(21);
								mAccelCalRawParams = new byte[22];
								System.arraycopy(bufferCalibrationParameters, 0, mAccelCalRawParams, 1, 21);
								mAccelCalRawParams[0] = ACCEL_CALIBRATION_RESPONSE;
								
								retrievekinematiccalibrationparametersfrompacket(bufferCalibrationParameters, ACCEL_CALIBRATION_RESPONSE);

								//get gyro
								try {
									Thread.sleep(100);	// Due to the nature of the Bluetooth SPP stack a delay has been added to ensure the buffer is filled before it is read
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
								bufferCalibrationParameters = readBytes(21);
								mGyroCalRawParams = new byte[22];
								System.arraycopy(bufferCalibrationParameters, 0, mGyroCalRawParams, 1, 21);
								mGyroCalRawParams[0] = GYRO_CALIBRATION_RESPONSE;
								retrievekinematiccalibrationparametersfrompacket(bufferCalibrationParameters, GYRO_CALIBRATION_RESPONSE);

								//get mag
								try {
									Thread.sleep(100);	// Due to the nature of the Bluetooth SPP stack a delay has been added to ensure the buffer is filled before it is read
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
								bufferCalibrationParameters = readBytes(21);
								mMagCalRawParams = new byte[22];
								System.arraycopy(bufferCalibrationParameters, 0, mMagCalRawParams, 1, 21);
								mMagCalRawParams[0] = MAG_CALIBRATION_RESPONSE;
								retrievekinematiccalibrationparametersfrompacket(bufferCalibrationParameters, MAG_CALIBRATION_RESPONSE);

								try {
									Thread.sleep(100);	// Due to the nature of the Bluetooth SPP stack a delay has been added to ensure the buffer is filled before it is read
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
								
								bufferCalibrationParameters = readBytes(4); 
								mEMGCalRawParams = new byte[5];
								System.arraycopy(bufferCalibrationParameters, 0, mEMGCalRawParams, 1, 4);
								mEMGCalRawParams[0] = EMG_CALIBRATION_RESPONSE;
								retrievebiophysicalcalibrationparametersfrompacket( bufferCalibrationParameters,EMG_CALIBRATION_RESPONSE);
								
								bufferCalibrationParameters = readBytes(8);
								
								mECGCalRawParams = new byte[9];
								System.arraycopy(bufferCalibrationParameters, 0, mECGCalRawParams, 1, 8);
								mECGCalRawParams[0] = ECG_CALIBRATION_RESPONSE;
								retrievebiophysicalcalibrationparametersfrompacket( bufferCalibrationParameters,ECG_CALIBRATION_RESPONSE);
								
								mTransactionCompleted=true;
								setInstructionStackLock(false);

							} else {


								byte[] bufferCalibrationParameters =readBytes(21); 
								
								mAccelCalRawParams = new byte[22];
								System.arraycopy(bufferCalibrationParameters, 0, mAccelCalRawParams, 1, 21);
								mAccelCalRawParams[0] = ACCEL_CALIBRATION_RESPONSE;
								
								retrievekinematiccalibrationparametersfrompacket(bufferCalibrationParameters, ACCEL_CALIBRATION_RESPONSE);

								//get gyro
								try {
									Thread.sleep(100);	// Due to the nature of the Bluetooth SPP stack a delay has been added to ensure the buffer is filled before it is read
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
								bufferCalibrationParameters = readBytes(21); 
								
								mGyroCalRawParams = new byte[22];
								System.arraycopy(bufferCalibrationParameters, 0, mGyroCalRawParams, 1, 21);
								mGyroCalRawParams[0] = GYRO_CALIBRATION_RESPONSE;
								
								retrievekinematiccalibrationparametersfrompacket(bufferCalibrationParameters, GYRO_CALIBRATION_RESPONSE);

								//get mag
								try {
									Thread.sleep(100);	// Due to the nature of the Bluetooth SPP stack a delay has been added to ensure the buffer is filled before it is read
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
								bufferCalibrationParameters = readBytes(21); 
								
								mMagCalRawParams = new byte[22];
								System.arraycopy(bufferCalibrationParameters, 0, mMagCalRawParams, 1, 21);
								mMagCalRawParams[0] = MAG_CALIBRATION_RESPONSE;
								
								retrievekinematiccalibrationparametersfrompacket(bufferCalibrationParameters, MAG_CALIBRATION_RESPONSE);

								//second accel cal params
								try {
									Thread.sleep(100);	// Due to the nature of the Bluetooth SPP stack a delay has been added to ensure the buffer is filled before it is read
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
								bufferCalibrationParameters = readBytes(21);
								
								mDigiAccelCalRawParams = new byte[22];
								System.arraycopy(bufferCalibrationParameters, 0, mDigiAccelCalRawParams, 1, 21);
								mDigiAccelCalRawParams[0] = LSM303DLHC_ACCEL_CALIBRATION_RESPONSE;
								msg = "All Calibration Response Received";
								printLogDataForDebugging(msg);
								retrievekinematiccalibrationparametersfrompacket(bufferCalibrationParameters, LSM303DLHC_ACCEL_CALIBRATION_RESPONSE);
								mTransactionCompleted=true;
								setInstructionStackLock(false);

							}
						} else if (tb[0]==GYRO_CALIBRATION_RESPONSE) {
							stopTimerWaitForAckOrResp(); //cancel the ack timer
							try {
								Thread.sleep(100);	// Due to the nature of the Bluetooth SPP stack a delay has been added to ensure the buffer is filled before it is read
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							mWaitForResponse=false;
							byte[] bufferCalibrationParameters = readBytes(21);
							mGyroCalRawParams = new byte[22];
							System.arraycopy(bufferCalibrationParameters, 0, mGyroCalRawParams, 1, 21);
							mGyroCalRawParams[0] = tb[0];
							
							int packetType=tb[0];
							retrievekinematiccalibrationparametersfrompacket(bufferCalibrationParameters, packetType);
							msg = "Gyro Calibration Response Received";
							printLogDataForDebugging(msg);
							mTransactionCompleted=true;
							setInstructionStackLock(false);
						} else if (tb[0]==MAG_CALIBRATION_RESPONSE ) {
							stopTimerWaitForAckOrResp(); //cancel the ack timer
							mWaitForResponse=false;
							try {
								Thread.sleep(100);	// Due to the nature of the Bluetooth SPP stack a delay has been added to ensure the buffer is filled before it is read
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							byte[] bufferCalibrationParameters = readBytes(21);
							mMagCalRawParams = new byte[22];
							System.arraycopy(bufferCalibrationParameters, 0, mMagCalRawParams, 1, 21);
							mMagCalRawParams[0] = tb[0];
							int packetType=tb[0];
							retrievekinematiccalibrationparametersfrompacket(bufferCalibrationParameters, packetType);
							msg = "Mag Calibration Response Received";
							printLogDataForDebugging(msg);
							mTransactionCompleted=true;
							setInstructionStackLock(false);
						} else if(tb[0]==CONFIG_BYTE0_RESPONSE) {
							stopTimerWaitForAckOrResp(); //cancel the ack timer
							
							if (mHardwareVersion==HW_ID.SHIMMER_2R || mHardwareVersion==HW_ID.SHIMMER_2){    
								byte[] bufferConfigByte0 = readBytes(1);
								mConfigByte0 = bufferConfigByte0[0] & 0xFF;
							} else {
								byte[] bufferConfigByte0 = readBytes(4);
								mConfigByte0 = ((long)(bufferConfigByte0[0] & 0xFF) +((long)(bufferConfigByte0[1] & 0xFF) << 8)+((long)(bufferConfigByte0[2] & 0xFF) << 16) +((long)(bufferConfigByte0[3] & 0xFF) << 24));
							}
							msg = "ConfigByte0 response received Response Received";
							printLogDataForDebugging(msg);
							mTransactionCompleted=true;
							setInstructionStackLock(false);
						} else if(tb[0]==GET_SHIMMER_VERSION_RESPONSE) {
							stopTimerWaitForAckOrResp(); //cancel the ack timer
							try {
								Thread.sleep(100);	// Due to the nature of the Bluetooth SPP stack a delay has been added to ensure the buffer is filled before it is read
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							byte[] bufferShimmerVersion = new byte[1]; 
							bufferShimmerVersion = readBytes(1);
							mHardwareVersion=(int)bufferShimmerVersion[0];
							mTransactionCompleted=true;
							setInstructionStackLock(false);
//							if (mShimmerVersion == HW_ID.SHIMMER_2R){
//								initializeShimmer2R();
//							} else if (mShimmerVersion == HW_ID.SHIMMER_3) {
//								initializeShimmer3();
//							}
							msg = "Shimmer Version (HW) Response Received: " + Util.bytesToHexStringWithSpacesFormatted(bufferShimmerVersion);
							printLogDataForDebugging(msg);
							readFWVersion();
						} else if (tb[0]==ECG_CALIBRATION_RESPONSE){
							stopTimerWaitForAckOrResp(); //cancel the ack timer
							try {
								Thread.sleep(100);	// Due to the nature of the Bluetooth SPP stack a delay has been added to ensure the buffer is filled before it is read
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							byte[] bufferCalibrationParameters = new byte[8]; 
							bufferCalibrationParameters = readBytes(4);
															
							mECGCalRawParams = new byte[9];
							System.arraycopy(bufferCalibrationParameters, 0, mECGCalRawParams, 1, 8);
							mECGCalRawParams[0] = ECG_CALIBRATION_RESPONSE;
							//get ecg 
							retrievebiophysicalcalibrationparametersfrompacket( bufferCalibrationParameters,ECG_CALIBRATION_RESPONSE);
							msg = "ECG Calibration Response Received";
							printLogDataForDebugging(msg);
							mTransactionCompleted=true;
							setInstructionStackLock(false);
						} else if (tb[0]==EMG_CALIBRATION_RESPONSE){
							stopTimerWaitForAckOrResp(); //cancel the ack timer
							try {
								Thread.sleep(100);	// Due to the nature of the Bluetooth SPP stack a delay has been added to ensure the buffer is filled before it is read
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							byte[] bufferCalibrationParameters = new byte[4]; 
							bufferCalibrationParameters = readBytes(4);
							
							mEMGCalRawParams = new byte[5];
							System.arraycopy(bufferCalibrationParameters, 0, mEMGCalRawParams, 1, 4);
							mEMGCalRawParams[0] = EMG_CALIBRATION_RESPONSE;
							//get EMG
							msg = "EMG Calibration Response Received";
							printLogDataForDebugging(msg);
							retrievebiophysicalcalibrationparametersfrompacket( bufferCalibrationParameters,EMG_CALIBRATION_RESPONSE);
							mTransactionCompleted=true;
							setInstructionStackLock(false);
						}
						else if(tb[0] == RWC_RESPONSE) {

							stopTimerWaitForAckOrResp(); //cancel the ack timer
							mWaitForResponse=false;
							mTransactionCompleted=true;
							printLogDataForDebugging(msg);
							byte[] byteTime = readBytes(8);
							mGetRWC = byteTime;
							mTransactionCompleted=true;
							setInstructionStackLock(false);
						
						}
						else if(tb[0] == BAUD_RATE_RESPONSE) {
							stopTimerWaitForAckOrResp(); //cancel the ack timer
							mWaitForResponse=false;
							mTransactionCompleted=true;
							byte[] bufferBaud = readBytes(1); 
							printLogDataForDebugging(msg);
							mBluetoothBaudRate=bufferBaud[0] & 0xFF;
							setInstructionStackLock(false);
						}
						else if(tb[0] == DAUGHTER_CARD_ID_RESPONSE) {
							stopTimerWaitForAckOrResp(); //cancel the ack timer
							mWaitForResponse=false;
							mTransactionCompleted=true;
							printLogDataForDebugging(msg);
							mExpBoardArray = readBytes(numBytesToReadFromExpBoard+1);
							
//							getExpBoardID();//CHANGED TO NEWER UP-TO-DATE method
							
							byte[] mExpBoardArraySplit = Arrays.copyOfRange(mExpBoardArray, 1, 4);
							setExpansionBoardDetails(new ExpansionBoardDetails(mExpBoardArraySplit));
							setInstructionStackLock(false);
						}
						else if(tb[0] == DERIVED_CHANNEL_BYTES_RESPONSE) {

							stopTimerWaitForAckOrResp(); //cancel the ack timer
							mWaitForResponse=false;
							mTransactionCompleted=true;
							printLogDataForDebugging(msg);
							byte[] byteArray = readBytes(3);
							mDerivedSensors=(long)(((byteArray[0]&0xFF)<<16) + ((byteArray[1]&0xFF)<<8)+(byteArray[2]&0xFF));
//							setInstructionStackLock(false);
						}
						else if(tb[0] == TRIAL_CONFIG_RESPONSE) {
							stopTimerWaitForAckOrResp(); //cancel the ack timer
							mWaitForResponse=false;
							printLogDataForDebugging(msg);
							byte[] data = readBytes(3);
							fillTrialShimmer3(data);
							mTransactionCompleted=true;
							setInstructionStackLock(false);
						}
						else if(tb[0] == CENTER_RESPONSE) {
							stopTimerWaitForAckOrResp(); //cancel the ack timer
							mWaitForResponse=false;
							mTransactionCompleted=true;
							printLogDataForDebugging(msg);
							byte[] length = readBytes(1);
							byte[] data = readBytes(length[0]);
							String center = new String(data);
							setCenter(center);
							mTransactionCompleted=true;
							setInstructionStackLock(false);
						}
						else if(tb[0] == SHIMMERNAME_RESPONSE) {
							stopTimerWaitForAckOrResp(); //cancel the ack timer
							mWaitForResponse=false;
							mTransactionCompleted=true;
							printLogDataForDebugging(msg);
							byte[] length = readBytes(1);
							byte[] data = readBytes(length[0]);
							String name = new String(data);
							setShimmerUserAssignedName(name);
							mTransactionCompleted=true;
							setInstructionStackLock(false);
						}
						else if(tb[0] == EXPID_RESPONSE) {

							stopTimerWaitForAckOrResp(); //cancel the ack timer
							mWaitForResponse=false;
							mTransactionCompleted=true;
							printLogDataForDebugging(msg);
							byte[] length = readBytes(1);
							byte[] data = readBytes(length[0]);
							String name = new String(data);
							setExperimentName(name);
							mTransactionCompleted=true;
							setInstructionStackLock(false);
						
						}
						
						else if(tb[0] == MYID_RESPONSE) {
							
						}
						else if(tb[0] == NSHIMMER_RESPONSE) {
							
						}
						else if(tb[0] == CONFIGTIME_RESPONSE) {
							stopTimerWaitForAckOrResp(); //cancel the ack timer
							mWaitForResponse=false;
							mTransactionCompleted=true;
							printLogDataForDebugging(msg);
							byte[] length = readBytes(1);
							byte[] data = readBytes(length[0]);
							String time = new String(data);
							if (time.isEmpty()){
								setConfigTime(0);
							} else {
								setConfigTime(Long.parseLong(time));	
							}
							
							mTransactionCompleted=true;
							setInstructionStackLock(false);
						}
						else if(tb[0] == INSTREAM_CMD_RESPONSE) {
							stopTimerWaitForAckOrResp(); //cancel the ack timer
							mWaitForResponse=false;
							mTransactionCompleted=true;
							printLogDataForDebugging(msg);
							byte[] bufferLogCommandType = new byte[2];
							bufferLogCommandType =	readBytes(2);
							consolePrintLn("Instream received = " + Util.byteToHexStringFormatted(bufferLogCommandType[0]));
							if(bufferLogCommandType[0]==DIR_RESPONSE){
								mDirectoryNameLength = bufferLogCommandType[1];
								byte[] bufferDirectoryName = new byte[mDirectoryNameLength];
								bufferDirectoryName = readBytes(mDirectoryNameLength);
								String tempDirectory = new String(bufferDirectoryName);
								mDirectoryName = tempDirectory;
								consolePrintLn("Directory Name = "+ mDirectoryName);
							}
							else if(bufferLogCommandType[0]==STATUS_RESPONSE){
								parseStatusByte(bufferLogCommandType[1]);

								if(!mIsSensing){
									if (!isInitialized()){
										writeRealWorldClock();
									}
								}
								
								logAndStreamStatusChanged();
								
							} else if(bufferLogCommandType[0] == VBATT_RESPONSE) {
								byte[] bufferAns = readBytes(2); 
								//mBattVoltage=Integer.toString(((bufferAns[0]&0xFF)<<8)+(bufferLogCommandType[1]&0xFF));
								setBattStatusDetails(new ShimmerBattStatusDetails(((bufferAns[0]&0xFF)<<8)+(bufferLogCommandType[1]&0xFF),bufferAns[1]));
								consolePrintLn("Batt data " + mBattVoltage);
							}  
							setInstructionStackLock(false);
						}
						else {
							consolePrintLn("Unknown BT response: " + tb[0]);
						}
					}
				} 
				
				
				if (mWaitForAck==false && mWaitForResponse == false && mIsStreaming ==false && availableBytes()!=0 && mFirmwareIdentifier==FW_ID.SHIMMER3.LOGANDSTREAM) {
					tb=readBytes(1);
					if(tb[0]==ACK_COMMAND_PROCESSED) {
						consolePrintLn("ACK RECEIVED , Connected State!!");
						tb = readBytes(1);
						if (tb[0]==ACK_COMMAND_PROCESSED){
							tb = readBytes(1);
						}
						if(tb[0]==INSTREAM_CMD_RESPONSE){
							consolePrintLn("INS CMD RESP");
							byte[] command = readBytes(2);
							if(command[0]==DIR_RESPONSE){
								mDirectoryNameLength = command[1];
								byte[] bufferDirectoryName = new byte[mDirectoryNameLength];
								bufferDirectoryName = readBytes(mDirectoryNameLength);
								String tempDirectory = new String(bufferDirectoryName);
								mDirectoryName = tempDirectory;

								consolePrintLn("DIR RESP : " + mDirectoryName);
							}
							else if(command[0]==STATUS_RESPONSE){
								parseStatusByte(command[1]);

								if (mIsStreaming){
//								if (mIsSensing){
									//flush all the bytes
									while(availableBytes()!=0){
										consolePrintLn("Throwing away = " + Util.bytesToHexStringWithSpacesFormatted(readBytes(1)));
									}
									startStreaming();
								}
								
								logAndStreamStatusChanged();
							}  else if(command[0] == VBATT_RESPONSE) {
								byte[] bufferAns = readBytes(2); 
							//	mBattVoltage=Integer.toString(((bufferAns[0]&0xFF)<<8)+(command[1]&0xFF));
							//	consolePrintLn("Batt data " + mBattVoltage);
								setBattStatusDetails(new ShimmerBattStatusDetails(((bufferAns[0]&0xFF)<<8)+(command[1]&0xFF),bufferAns[1]));
								consolePrintLn("Batt data " + mBattVoltage);
							}  
						}
					}
					
					while(availableBytes()!=0){
						consolePrintLn("Throwing away = " + Util.bytesToHexStringWithSpacesFormatted(readBytes(1)));
					}
				}
				
				
				if (mIsStreaming==true) {
					
					tb = readBytes(1);
					if (tb!=null){
						mByteArrayOutputStream.write(tb[0]);
						//Everytime a byte is received the timestamp is taken
						mListofPCTimeStamps.add(System.currentTimeMillis());
					} else {
						consolePrint("readbyte null");
					}
					
					

					//If there is a full packet and the subsequent sequence number of following packet
					if (mByteArrayOutputStream.size()>=mPacketSize+2){ // +2 because there are two acks
						mIamAlive = true;
						byte[] bufferTemp = mByteArrayOutputStream.toByteArray();
						if (bufferTemp[0]==DATA_PACKET && bufferTemp[mPacketSize+1]==DATA_PACKET){
							newPacket = new byte[mPacketSize];
							//Skip the first byte as it is the identifier DATA_PACKET
							System.arraycopy(bufferTemp, 1, newPacket, 0, mPacketSize);
							if (mUseProcessingThread){
								
								mABQPacketByeArray.add(new RawBytePacketWithPCTimeStamp(newPacket,mListofPCTimeStamps.get(0)));
							} else {
								buildAndSendMsg(newPacket, FW_TYPE_BT, 0, mListofPCTimeStamps.get(0));
							}
							
							//Finally clear the parsed packet from the bytearrayoutputstream, NOTE the last two bytes(seq number of next packet) are added back on after the reset
							//consolePrint("Byte size reset: " + mByteArrayOutputStream.size() + "\n");
							mByteArrayOutputStream.reset();
							mByteArrayOutputStream.write(bufferTemp[mPacketSize+1]);
							for (int i=0;i<mPacketSize+1;i++){
								mListofPCTimeStamps.remove(0);
							}
							
							//consolePrint(bufferTemp[mPacketSize+1] + "\n");
							
						} else if (bufferTemp[0]==DATA_PACKET && bufferTemp[mPacketSize+1]==ACK_COMMAND_PROCESSED){
							if (mByteArrayOutputStream.size()>mPacketSize+2){
								if (bufferTemp[mPacketSize+2]==DATA_PACKET){
									newPacket = new byte[mPacketSize];
									System.arraycopy(bufferTemp, 1, newPacket, 0, mPacketSize);
									if (mUseProcessingThread){
										mABQPacketByeArray.add(new RawBytePacketWithPCTimeStamp(newPacket,mListofPCTimeStamps.get(0)));
									} else {
										buildAndSendMsg(newPacket, FW_TYPE_BT, 0, mListofPCTimeStamps.get(0));
									}
									//Finally clear the parsed packet from the bytearrayoutputstream, NOTE the last two bytes(seq number of next packet) are added back on after the reset
									mByteArrayOutputStream.reset();
									mByteArrayOutputStream.write(bufferTemp[mPacketSize+2]);
									consolePrintLn(Integer.toString(bufferTemp[mPacketSize+2]));
									for (int i=0;i<mPacketSize+2;i++){
										mListofPCTimeStamps.remove(0);
									}
									
									if (mCurrentCommand==SET_BLINK_LED){
										consolePrintLn("LED COMMAND ACK RECEIVED");
										mCurrentLEDStatus=(int)((byte[])getListofInstructions().get(0))[1];
										getListofInstructions().remove(0);
										stopTimerWaitForAckOrResp(); //cancel the ack timer
										mWaitForAck=false;
										mTransactionCompleted = true;
										setInstructionStackLock(false);
									} else if (mCurrentCommand==TEST_CONNECTION_COMMAND){
										consolePrintLn("Test Connection Ack Received");
										getListofInstructions().remove(0);
										stopTimerWaitForAckOrResp(); //cancel the ack timer
										mWaitForAck=false;
										mTransactionCompleted = true;
										setInstructionStackLock(false);
									} else if (mCurrentCommand==START_LOGGING_ONLY_COMMAND){
										consolePrintLn("START LOGGING ACK RECEIVED");
										getListofInstructions().remove(0);
										stopTimerWaitForAckOrResp(); //cancel the ack timer
										mIsSDLogging = true;
										logAndStreamStatusChanged();
										mWaitForAck=false;
										mTransactionCompleted = true;
										setInstructionStackLock(false);
									}  else if (mCurrentCommand==STOP_LOGGING_ONLY_COMMAND){
										consolePrintLn("STOP LOGGING ACK RECEIVED");
										getListofInstructions().remove(0);
										stopTimerWaitForAckOrResp(); //cancel the ack timer
										mIsSDLogging = false;
										logAndStreamStatusChanged();
										mWaitForAck=false;
										mTransactionCompleted = true;
										setInstructionStackLock(false);
									} 
									
									
								} else if (mFirmwareIdentifier == FW_ID.SHIMMER3.LOGANDSTREAM && bufferTemp[mPacketSize+2]==INSTREAM_CMD_RESPONSE){ //this is for logandstream stupport, command is trasmitted and ack received
									consolePrintLn("COMMAND TXed and ACK RECEIVED IN STREAM");
									consolePrintLn("INS CMD RESP");
									byte[] command = readBytes(2);
									if(command[0]==DIR_RESPONSE){
										int mDirectoryNameLength = command[1];
										byte[] bufferDirectoryName = new byte[mDirectoryNameLength];
										bufferDirectoryName = readBytes(mDirectoryNameLength);
										String tempDirectory = new String(bufferDirectoryName);
										mDirectoryName = tempDirectory;

										consolePrintLn("DIR RESP : " + mDirectoryName);
									}
									else if(command[0]==STATUS_RESPONSE){
										
										parseStatusByte(command[1]);
										logAndStreamStatusChanged();
										
//										int sensing = command[1] & 2;
//										if(sensing==2)
//											mIsSensing = true;
//										else
//											mIsSensing = false;
//
//										int docked = command[1] & 1;
//										if(docked==1)
//											mDockedStatus = true;
//										else
//											mDockedStatus = false;
//
//										consolePrintLn("Sensing = "+sensing);
//										consolePrintLn("Sensing status = "+mIsSensing);
//										consolePrintLn("Docked = "+docked);
//										consolePrintLn("Docked status = "+mDockedStatus);
									}  else if(command[0] == VBATT_RESPONSE) {
										byte[] bufferAns = readBytes(2); 
										setBattStatusDetails(new ShimmerBattStatusDetails(((bufferAns[0]&0xFF)<<8)+(command[1]&0xFF),bufferAns[1]));
										consolePrintLn("Batt data " + mBattVoltage);
									}  
									
									mWaitForAck=false;
									mTransactionCompleted = true;   
									stopTimerWaitForAckOrResp(); //cancel the ack timer
									if (getListofInstructions().size()>0){
										getListofInstructions().remove(0);
									}
									setInstructionStackLock(false);
									newPacket = new byte[mPacketSize];
									System.arraycopy(bufferTemp, 1, newPacket, 0, mPacketSize);
									if (mUseProcessingThread){
										mABQPacketByeArray.add(new RawBytePacketWithPCTimeStamp(newPacket,mListofPCTimeStamps.get(0)));
									} else {
										buildAndSendMsg(newPacket, FW_TYPE_BT, 0, mListofPCTimeStamps.get(0));
									}
									mByteArrayOutputStream.reset();
									mListofPCTimeStamps.clear();
									
								} else {
									consolePrintLn("??");
								}
							} 
							if (mByteArrayOutputStream.size()>mPacketSize+2){ //throw the first byte away
								
								byte[] bTemp = mByteArrayOutputStream.toByteArray();
								mByteArrayOutputStream.reset();
								mByteArrayOutputStream.write(bTemp, 1, bTemp.length-1); //this will throw the first byte away
								mListofPCTimeStamps.remove(0);
								consolePrintLn("Throw Byte");
							}
							
						} else {//throw the first byte away
							byte[] bTemp = mByteArrayOutputStream.toByteArray();
							mByteArrayOutputStream.reset();
							mByteArrayOutputStream.write(bTemp, 1, bTemp.length-1); //this will throw the first byte away
							mListofPCTimeStamps.remove(0);
							consolePrintLn("Throw Byte");
						}
					} 
				}
			}
		}
	}
	
	
	private void parseStatusByte(byte statusByte){
		
		mIsDocked = ((statusByte & 0x01) > 0)? true:false;
		mIsSensing = ((statusByte & 0x02) > 0)? true:false;
//		reserved = ((statusByte & 0x03) > 0)? true:false;
		mIsSDLogging = ((statusByte & 0x08) > 0)? true:false;
		mIsStreaming = ((statusByte & 0x10) > 0)? true:false; 

		consolePrintLn("Status Response = " + Util.byteToHexStringFormatted(statusByte)
				+ "\t" + "IsDocked = " + mIsDocked
				+ "\t" + "IsSensing = " + mIsSensing
				+ "\t" + "IsSDLogging = "+ mIsSDLogging
				+ "\t" + "IsStreaming = " + mIsStreaming
				);
	}
	
	private byte[] convertStackToByteArray(Stack<Byte> b,int packetSize) {
		byte[] returnByte=new byte[packetSize];
		b.remove(0); //remove the Data Packet identifier 
		for (int i=0;i<packetSize;i++) {
			returnByte[packetSize-1-i]=(byte) b.pop();
		}
		return returnByte;
	}
	
	
	
	protected void startTimerWaitForAckOrResp(int duration) {
		responseTimer(duration);
	}
	
	public void stopTimerWaitForAckOrResp(){
		//Terminate the timer thread
		if (mTimerWaitForAckOrResp!=null){
			mTimerWaitForAckOrResp.cancel();
			mTimerWaitForAckOrResp.purge();
			mTimerWaitForAckOrResp = null;
		}
	}

	public synchronized void responseTimer(int seconds) {
		if (mTimerWaitForAckOrResp!=null) {
			mTimerWaitForAckOrResp.cancel();
			mTimerWaitForAckOrResp.purge();
		}
		printLogDataForDebugging("Waiting for ack/response for command:\t" + Util.byteToHexStringFormatted(mCurrentCommand));
		mTimerWaitForAckOrResp = new Timer();
		mTimerWaitForAckOrResp.schedule(new responseTask(), seconds*1000);
	}

	class responseTask extends TimerTask {
		public void run() {
			{
				if (mCurrentCommand==GET_FW_VERSION_COMMAND){
					printLogDataForDebugging("FW Response Timeout");
					//					mFWVersion=0.1;
					mFirmwareVersionMajor=0;
					mFirmwareVersionMinor=1;
					mFirmwareVersionInternal=0;
					mFirmwareVersionCode=0;
					mFirmwareVersionParsed="BoilerPlate 0.1.0";
					mHardwareVersion = HW_ID.SHIMMER_2R; // on Shimmer2r has
					/*Message msg = mHandler.obtainMessage(MESSAGE_TOAST);
          	        Bundle bundle = new Bundle();
          	        bundle.putString(TOAST, "Firmware Version: " +mFirmwareVersionParsed);
          	        msg.setData(bundle);*/
					if (!mDummy){
						//mHandler.sendMessage(msg);
					}
					mWaitForAck=false;
					mTransactionCompleted=true; //should be false, so the driver will know that the command has to be executed again, this is not supported at the moment 
					stopTimerWaitForAckOrResp();  //Terminate the timer thread
					mFirstTime=false;
					getListofInstructions().remove(0);
					setInstructionStackLock(false);
					initializeBoilerPlate();
				} else if(mCurrentCommand==GET_SAMPLING_RATE_COMMAND && mIsInitialised==false){
					printLogDataForDebugging("Get Sampling Rate Timeout");
					mWaitForAck=false;
					mTransactionCompleted=true; //should be false, so the driver will know that the command has to be executed again, this is not supported at the moment 
					stopTimerWaitForAckOrResp();  //Terminate the timer thread
					mFirstTime=false;
					getListofInstructions().remove(0);
					setInstructionStackLock(false);
					mFirstTime=false;
				} else if(mCurrentCommand==GET_SHIMMER_VERSION_COMMAND_NEW){ //in case the new command doesn't work, try the old command
					printLogDataForDebugging("Shimmer Version Response Timeout. Trying the old version command");
					mWaitForAck=false;
					mTransactionCompleted=true; 
					stopTimerWaitForAckOrResp(); //Terminate the timer thread
					mFirstTime=false;
					getListofInstructions().remove(0);
					setInstructionStackLock(false);
					readShimmerVersionDepracated();
				}
				else if(mCurrentCommand==GET_VBATT_COMMAND){
					// If the command fails to get a response, the API should assume that the connection has been lost and close the serial port cleanly.
					consolePrintLn("Command " + Integer.toString(mCurrentCommand) +" failed");
					stopTimerWaitForAckOrResp(); //Terminate the timer thread
					mWaitForAck=false;
					mTransactionCompleted=true; //should be false, so the driver will know that the command has to be executed again, this is not supported at the moment
					setInstructionStackLock(false);
					
					if (mIsStreaming && getPacketReceptionRate()<100){
						getListofInstructions().clear();
						printLogDataForDebugging("Response not received for Get_Status_Command. Loss bytes detected.");
					} else if(!mIsStreaming) {
						//CODE TO BE USED
						printLogDataForDebugging("Command " + Integer.toString(mCurrentCommand) +" failed; Killing Connection. Packet RR:  " + Double.toString(getPacketReceptionRate()));
						if (mWaitForResponse){
							printLogDataForDebugging("Response not received");
							sendStatusMSGtoUI("Connection lost." + mMyBluetoothAddress);
						}
						stop(); //If command fail exit device
					}
				}
				else if(mCurrentCommand==GET_STATUS_COMMAND){
					// If the command fails to get a response, the API should assume that the connection has been lost and close the serial port cleanly.
					consolePrintLn("Command " + Integer.toString(mCurrentCommand) +" failed");
					stopTimerWaitForAckOrResp(); //Terminate the timer thread
					mWaitForAck=false;
					mTransactionCompleted=true; //should be false, so the driver will know that the command has to be executed again, this is not supported at the moment
					setInstructionStackLock(false);
					
					if (mIsStreaming && getPacketReceptionRate()<100){
						getListofInstructions().clear();
						printLogDataForDebugging("Response not received for Get_Status_Command. Loss bytes detected.");
					} else if(!mIsStreaming) {
						//CODE TO BE USED
						printLogDataForDebugging("Command " + Integer.toString(mCurrentCommand) +" failed; Killing Connection. Packet RR:  " + Double.toString(getPacketReceptionRate()));
						if (mWaitForResponse){
							printLogDataForDebugging("Response not received");
							sendStatusMSGtoUI("Connection lost." + mMyBluetoothAddress);
						}
						stop(); //If command fail exit device
					}
				}
				else if(mCurrentCommand==GET_DIR_COMMAND){
					// If the command fails to get a response, the API should assume that the connection has been lost and close the serial port cleanly.

					consolePrintLn("Command " + Integer.toString(mCurrentCommand) +" failed");
					stopTimerWaitForAckOrResp(); //Terminate the timer thread
					mWaitForAck=false;
					mTransactionCompleted=true; //should be false, so the driver will know that the command has to be executed again, this is not supported at the moment 
					setInstructionStackLock(false);
					if (mIsStreaming && getPacketReceptionRate()<100){
						printLogDataForDebugging("Response not received for Get_Dir_Command. Loss bytes detected.");
						getListofInstructions().clear();
					} else  if(!mIsStreaming){
						//CODE TO BE USED
						printLogDataForDebugging("Command " + Integer.toString(mCurrentCommand) +" failed; Killing Connection  ");
						if (mWaitForResponse){
							printLogDataForDebugging("Response not received");
							sendStatusMSGtoUI("Connection lost." + mMyBluetoothAddress);
						}
						stop(); //If command fail exit device
					}
				}
				else {
					
					if(!mIsStreaming){
						printLogDataForDebugging("Command " + Integer.toString(mCurrentCommand) +" failed; Killing Connection  ");
						if (mWaitForResponse){
							printLogDataForDebugging("Response not received");
							sendStatusMSGtoUI("Response not received, please reset Shimmer Device." + mMyBluetoothAddress);
						}
						mWaitForAck=false;
						mTransactionCompleted=true; //should be false, so the driver will know that the command has to be executed again, this is not supported at the moment 
						stop(); //If command fail exit device 
					} else {
						printLogDataForDebugging("Command " + Integer.toString(mCurrentCommand) +" failed;");
						mWaitForAck=false;
						mTransactionCompleted=true; //should be false, so the driver will know that the command has to be executed again, this is not supported at the moment 
						setInstructionStackLock(false);
					}
				}
			}
		}
	}
	
	//endregion
	
	

	public void clearShimmerVersionInfo() {
		super.mShimmerVerObject = new ShimmerVerObject();
		super.mHardwareVersion = 0;
		super.mHardwareVersionParsed = "";
		super.mFirmwareIdentifier = 0;
		super.mFirmwareVersionMajor = 0;
		super.mFirmwareVersionMinor = 0;
		super.mFirmwareVersionInternal = 0;
		super.mFirmwareVersionParsed = "";
		super.mFirmwareVersionCode = 0;
	}
	
	public void setExpansionBoardDetails(ExpansionBoardDetails eBD){
		super.mExpansionBoardDetails  = eBD;
		super.mExpansionBoardId = eBD.mExpBoardId;
		super.mExpansionBoardRev = eBD.mExpBoardRev;
		super.mExpansionBoardRevSpecial = eBD.mExpBoardRevSpecial;
		super.mExpansionBoardParsed = eBD.mExpBoardParsed;
		super.mExpansionBoardParsedWithVer = eBD.mExpBoardParsedWithVer;
		super.mExpBoardArray = eBD.mExpBoardArray;
	}


	//region --------- INITIALIZE SHIMMER FUNCTIONS --------- 
	
	protected synchronized void initialize() {	    	//See two constructors for Shimmer
		//InstructionsThread instructionsThread = new InstructionsThread();
		//instructionsThread.start();
		clearShimmerVersionInfo();
		
		stopTimerReadStatus();
		stopTimerWaitForAckOrResp();
		
		setInstructionStackLock(false);
		dummyreadSamplingRate(); // it actually acts to clear the write buffer
		readShimmerVersion();
		//readFWVersion();
		//mShimmerVersion=4;
	}

	public void initializeBoilerPlate(){
		readSamplingRate();
		readConfigByte0();
		readCalibrationParameters("Accelerometer");
		readCalibrationParameters("Magnetometer");
		readCalibrationParameters("Gyroscope");
		if (mSetupDevice==true && mHardwareVersion!=4){
			writeAccelRange(mAccelRange);
			writeGSRRange(mGSRRange);
			writeSamplingRate(mShimmerSamplingRate);	
			writeEnabledSensors(mSetEnabledSensors);
			setContinuousSync(mContinousSync);
		} else {
			inquiry();
		}
	}
	
	/**
	 * By default once connected no low power modes will be enabled. Low power modes should be enabled post connection once the MSG_STATE_FULLY_INITIALIZED is sent 
	 */
	private void initializeShimmer2R(){ 
		readSamplingRate();
		readMagSamplingRate();
		writeBufferSize(1);
		readBlinkLED();
		readConfigByte0();
		readCalibrationParameters("All");
		if (mSetupDevice==true){
			writeMagRange(mMagRange); //set to default Shimmer mag gain
			writeAccelRange(mAccelRange);
			writeGSRRange(mGSRRange);
			writeSamplingRate(mShimmerSamplingRate);	
			writeEnabledSensors(mSetEnabledSensors);
			setContinuousSync(mContinousSync);
		} else {
			if (mFirmwareVersionParsed.equals("BoilerPlate 0.1.0")){
			
			} else {
				readMagRange();
			}
			inquiry();
		}
	}

	private void initializeShimmer3(){
		
		mHaveAttemptedToReadConfig = true;
		
		if(mSendProgressReport){
			operationPrepare();
			setState(BT_STATE.INITIALISING);
		}
			
		readSamplingRate();
		readMagRange();
		readAccelRange();
		readGyroRange();
		readAccelSamplingRate();
		readExpansionBoardID();
		readBlinkLED();
		readCalibrationParameters("All");
		readpressurecalibrationcoefficients();
		readEXGConfigurations(1);
		readEXGConfigurations(2);
		//enableLowPowerMag(mLowPowerMag);
		
		if(isThisVerCompatibleWith(FW_ID.SHIMMER3.LOGANDSTREAM, 0, 5, 2)){
			readStatusLogAndStream();
			readTrial();
			readConfigTime();
			readShimmerName();
			readExperimentName();
		}
		if(isThisVerCompatibleWith(FW_ID.SHIMMER3.LOGANDSTREAM, 0, 5, 9)){
			readBattery();
		}
//		else if(isThisVerCompatibleWith(FW_ID.SHIMMER3.BTSTREAM, 0, 7, 2)){
//			readInfoMem();
//		}
		
		if (mSetupDevice==true){
			//writeAccelRange(mDigitalAccelRange);
			if (mSetupEXG){
				writeEXGConfiguration(mEXG1RegisterArray,1);
				writeEXGConfiguration(mEXG2RegisterArray,2);
				mSetupEXG = false;
			}
			writeGSRRange(mGSRRange);
			writeAccelRange(mAccelRange);
			writeGyroRange(mGyroRange);
			writeMagRange(mMagRange);
			writeSamplingRate(mShimmerSamplingRate);	
			writeInternalExpPower(1);
//			setContinuousSync(mContinousSync);
			writeEnabledSensors(mSetEnabledSensors); //this should always be the last command
		} else {
			inquiry();
		}

		if(mSendProgressReport){
			// Just unlock instruction stack and leave logAndStream timer as
			// this is handled in the next step, i.e., no need for
			// operationStart() here
//			startOperation(CURRENT_OPERATION.INITIALISING, getmListofInstructions().size());
			startOperation(BT_STATE.INITIALISING, getListofInstructions().size());
			
			setInstructionStackLock(false);
		}
		
		//TODO remove below
//		if(mFirmwareIdentifier==FW_ID.SHIMMER3.LOGANDSTREAM){ 
//			stopTimerReadStatus(); // if shimmer is using LogAndStream FW, stop reading its status perdiocally
//			printLogDataForDebugging("Waiting for ack/response for command: " + Integer.toString(mCurrentCommand));
//			startTimerReadStatus();
//		}
		
		startTimerReadStatus();	// if shimmer is using LogAndStream FW, read its status perdiocally
		startTimerReadBattStatus();
		
	}
	
	//endregion
	
	
	public boolean isThisVerCompatibleWith(int firmwareIdentifier, int firmwareVersionMajor, int firmwareVersionMinor, int firmwareVersionInternal){
		return Util.compareVersions(mFirmwareIdentifier, mFirmwareVersionMajor, mFirmwareVersionMinor, mFirmwareVersionInternal,
				firmwareIdentifier, firmwareVersionMajor, firmwareVersionMinor, firmwareVersionInternal);
	}
	
	public void operationPrepare(){
		stopTimerCheckAlive();
		stopTimerReadStatus();
		stopTimerReadBattStatus();
		
		// wait for instruction stack to clear			
		while(getListofInstructions().size()>0); //TODO add timeout
		// lock the instruction stack
		setInstructionStackLock(true);
	}
	
	public void operationWaitForFinish(){
		// unlock the instruction stack
		setInstructionStackLock(false);
		// wait for instruction stack to clear			
		while(getListofInstructions().size()>0); //TODO add timeout
	}
	
	public void operationStart(BT_STATE btState){
		startOperation(btState, getListofInstructions().size());
		//unlock instruction stack
		setInstructionStackLock(false);
	}
	
	public void operationFinished(){
		startTimerCheckIfAlive();
		startTimerReadStatus();
		startTimerReadBattStatus();
	}
	
	
	//region  --------- START/STOP STREAMING FUNCTIONS --------- 
	
	public void startStreaming() {
		if (mFirmwareIdentifier==FW_ID.SHIMMER3.LOGANDSTREAM && mFirmwareVersionCode >=6){
			readRealWorldClock();
		}
		//mCurrentLEDStatus=-1;	
		//provides a callback for users to initialize their algorithms when start streaming is called
		if (mDataProcessing!=null){
			mDataProcessing.InitializeProcessData();
		} 	
		else {
			//do nothing
		}
		
		stopTimerReadStatus(); // if shimmer is using LogAndStream FW, stop reading its status perdiocally
		
		mPacketLossCount = 0;
		mPacketReceptionRate = 100;
		mFirstTimeCalTime=true;
		resetCalibratedTimeStamp();
		mLastReceivedCalibratedTimeStamp = -1;
		mSync=true; // a backup sync done every time you start streaming
		mByteArrayOutputStream.reset();
		mListofPCTimeStamps.clear();
		getListofInstructions().add(new byte[]{START_STREAMING_COMMAND});
	}
	
	public void startSDLogging(){
		if (mFirmwareIdentifier == FW_ID.SHIMMER3.LOGANDSTREAM && mFirmwareVersionCode >=6){
			getListofInstructions().add(new byte[]{START_LOGGING_ONLY_COMMAND});
		}	
	}
	
	public void stopSDLogging(){
		if (mFirmwareIdentifier == FW_ID.SHIMMER3.LOGANDSTREAM && mFirmwareVersionCode >=6){
			getListofInstructions().add(new byte[]{STOP_LOGGING_ONLY_COMMAND});
		}	
	}
	
	public void startDataLogAndStreaming(){
		if(mFirmwareIdentifier==FW_ID.SHIMMER3.LOGANDSTREAM){ // if shimmer is using LogAndStream FW, stop reading its status perdiocally
			readRealWorldClock();
			if (mDataProcessing!=null){
				mDataProcessing.InitializeProcessData();
			} 	
			else {
				//do nothing
			}
			
			stopTimerReadStatus(); // if shimmer is using LogAndStream FW, stop reading its status perdiocally

			mPacketLossCount = 0;
			mPacketReceptionRate = 100;
			mFirstTimeCalTime=true;
			resetCalibratedTimeStamp();
			mLastReceivedCalibratedTimeStamp = -1;
			mSync=true; // a backup sync done every time you start streaming
			getListofInstructions().add(new byte[]{START_SDBT_COMMAND});
		}
	}
	
	public void stopStreaming() {
		getListofInstructions().add(new byte[]{STOP_STREAMING_COMMAND});
		
		// For LogAndStream
		startTimerReadStatus();
	}
	
	//endregion
	
	
	//region --------- READ FUNCTIONS --------- 
	
	public void readShimmerVersion() {
		mDummy=false;//false
//		if (mFirmwareVersionParsed.equals("BoilerPlate 0.1.0")){
//			mShimmerVersion = HW_ID.SHIMMER_2R; // on Shimmer2r has 
			
//		} else if (mFWVersion!=1.2){
			getListofInstructions().add(new byte[]{GET_SHIMMER_VERSION_COMMAND_NEW});
//		} else {
//			mListofInstructions.add(new byte[]{GET_SHIMMER_VERSION_COMMAND});
//		}
	}
	
	@Deprecated
	public void readShimmerVersionDepracated(){
		getListofInstructions().add(new byte[]{GET_SHIMMER_VERSION_COMMAND});
	}
	
	/**
	 * The reason for this is because sometimes the 1st response is not received by the phone
	 */
	protected void dummyreadSamplingRate() {
		mDummy=true;
		getListofInstructions().add(new byte[]{GET_SAMPLING_RATE_COMMAND});
	}

	/**
	 * This reads the configuration of a chip from the EXG board
	 * @param chipID Chip id can either be 1 or 2
	 */
	public void readEXGConfigurations(int chipID){
		if ((mFirmwareVersionInternal >=8 && mFirmwareVersionCode==2) || mFirmwareVersionCode>2){
			if (chipID==1 || chipID==2){
				getListofInstructions().add(new byte[]{GET_EXG_REGS_COMMAND,(byte)(chipID-1),0,10});
			}
		}
	}

	public void readpressurecalibrationcoefficients() {
		if (mHardwareVersion == HW_ID.SHIMMER_3){
			if (mFirmwareVersionCode>1){
				getListofInstructions().add(new byte[]{ GET_BMP180_CALIBRATION_COEFFICIENTS_COMMAND});
			}
		}
	}

	
	/**
	 * @param sensor is a string value that defines the sensor. Accepted sensor values are "Accelerometer","Gyroscope","Magnetometer","ECG","EMG","All"
	 */
	public void readCalibrationParameters(String sensor) {
	
			if (!mIsInitialised){
				if (mFirmwareVersionCode==1 && mFirmwareVersionInternal==0  && mHardwareVersion!=3) {
					//mFirmwareVersionParsed="BoilerPlate 0.1.0";
					/*Message msg = mHandler.obtainMessage(MESSAGE_TOAST);
          	        Bundle bundle = new Bundle();
          	        bundle.putString(TOAST, "Firmware Version: " +mFirmwareVersionParsed);
          	        msg.setData(bundle);
          	        mHandler.sendMessage(msg);*/
				}	
			}
			if (sensor.equals("Accelerometer")) {
				getListofInstructions().add(new byte[]{GET_ACCEL_CALIBRATION_COMMAND});
			}
			else if (sensor.equals("Gyroscope")) {
				getListofInstructions().add(new byte[]{GET_GYRO_CALIBRATION_COMMAND});
			}
			else if (sensor.equals("Magnetometer")) {
				getListofInstructions().add(new byte[]{GET_MAG_CALIBRATION_COMMAND});
			}
			else if (sensor.equals("All")){
				getListofInstructions().add(new byte[]{GET_ALL_CALIBRATION_COMMAND});
			} 
			else if (sensor.equals("ECG")){
				getListofInstructions().add(new byte[]{GET_ECG_CALIBRATION_COMMAND});
			} 
			else if (sensor.equals("EMG")){
				getListofInstructions().add(new byte[]{GET_EMG_CALIBRATION_COMMAND});
			}
		
	}
	
	public void readSamplingRate() {
		getListofInstructions().add(new byte[]{GET_SAMPLING_RATE_COMMAND});
	}
	
	public void readGSRRange() {
		getListofInstructions().add(new byte[]{GET_GSR_RANGE_COMMAND});
	}

	public void readAccelRange() {
		getListofInstructions().add(new byte[]{GET_ACCEL_SENSITIVITY_COMMAND});
	}

	public void readBattery(){
		getListofInstructions().add(new byte[]{GET_VBATT_COMMAND});
	}
	
	public void readGyroRange() {
		getListofInstructions().add(new byte[]{GET_MPU9150_GYRO_RANGE_COMMAND});
	}

	public void readBufferSize() {
		getListofInstructions().add(new byte[]{GET_BUFFER_SIZE_COMMAND});
	}

	public void readMagSamplingRate() {
		if (mFirmwareVersionParsed.equals("BoilerPlate 0.1.0")){
		} else {
			getListofInstructions().add(new byte[]{GET_MAG_SAMPLING_RATE_COMMAND});
		}
	}

	/**
	 * Used to retrieve the data rate of the Accelerometer on Shimmer 3
	 */
	public void readAccelSamplingRate() {
		if (mHardwareVersion!=3){
		} else {
			getListofInstructions().add(new byte[]{GET_ACCEL_SAMPLING_RATE_COMMAND});
		}
	}

	public void readMagRange() {
		getListofInstructions().add(new byte[]{GET_MAG_GAIN_COMMAND});
	}

	public void readBlinkLED() {
		getListofInstructions().add(new byte[]{GET_BLINK_LED});
	}
	
	public void readECGCalibrationParameters() {
		if (mFirmwareVersionParsed.equals("BoilerPlate 0.1.0")){
		} else {
			getListofInstructions().add(new byte[]{GET_ECG_CALIBRATION_COMMAND});
		}
	}

	public void readEMGCalibrationParameters() {
		if (mFirmwareVersionParsed.equals("BoilerPlate 0.1.0")){
		} else {
			getListofInstructions().add(new byte[]{GET_EMG_CALIBRATION_COMMAND});
		}
	}
	
	public void readBaudRate(){
		if(mFirmwareVersionCode>=5){ 
			getListofInstructions().add(new byte[]{GET_BAUD_RATE_COMMAND});
		}
	}
	
	/**
	 * Read the number of bytes specified starting in the offset from the expansion board attached to the Shimmer Device
	 * @param numBytes number of bytes to be read. there can be read up to 256 bytes
	 * @param offset point from where the function starts to read
	 */
	public void readExpansionBoardByBytes(int numBytes, int offset){
		if(mFirmwareVersionCode>=5){ 
			if(numBytes+offset<=256){
				numBytesToReadFromExpBoard = numBytes;
				getListofInstructions().add(new byte[]{GET_DAUGHTER_CARD_ID_COMMAND, (byte) numBytes, (byte) offset});
			}
		}
	}

	public void readExpansionBoardID(){
		if(mFirmwareVersionCode>=5){ 
			numBytesToReadFromExpBoard=3;
			int offset=0;
			getListofInstructions().add(new byte[]{GET_DAUGHTER_CARD_ID_COMMAND, (byte) numBytesToReadFromExpBoard, (byte) offset});
		}
	}
	
	public void readDirectoryName(){
		if(mFirmwareIdentifier==FW_ID.SHIMMER3.LOGANDSTREAM){ // check if Shimmer is using LogAndStream firmware
			getListofInstructions().add(new byte[]{GET_DIR_COMMAND});
		}
	}
	
	public void readStatusLogAndStream(){
		if(mFirmwareIdentifier==FW_ID.SHIMMER3.LOGANDSTREAM){ // check if Shimmer is using LogAndStream firmware
			getListofInstructions().add(new byte[]{GET_STATUS_COMMAND});
			consolePrintLn("Instruction added to the list");
		}
	}

	public void readConfigByte0() {
		getListofInstructions().add(new byte[]{GET_CONFIG_BYTE0_COMMAND});
	}
	
	public void readFWVersion() {
		mDummy=false;//false
		getListofInstructions().add(new byte[]{GET_FW_VERSION_COMMAND});
	}
	
	//endregion
	
	
	//region --------- TIMERS --------- 
	
	public void startTimerReadStatus(){
		if(mFirmwareIdentifier==FW_ID.SHIMMER3.LOGANDSTREAM){ // if shimmer is using LogAndStream FW, stop reading its status perdiocally
			if(mTimerReadStatus==null){ 
				mTimerReadStatus = new Timer();
			}
			mTimerReadStatus.schedule(new readStatusTask(), mReadStatusPeriod, mReadStatusPeriod);
		}
	}
	
	public void stopTimerReadStatus(){
		if(mTimerReadStatus!=null){
			mTimerReadStatus.cancel();
			mTimerReadStatus.purge();
			mTimerReadStatus = null;
		}
	}
	
	/**
	 * Class used to read perdiocally the shimmer status when LogAndStream FW is installed
	 */
	public class readStatusTask extends TimerTask {

		@Override
		public void run() {
			readStatusLogAndStream();
		}
		
	}
	
	public void startTimerCheckIfAlive(){
		if (mCheckIfConnectionisAlive){
			if(mTimerCheckAlive==null){ 
				mTimerCheckAlive = new Timer();
			}
			mTimerCheckAlive.schedule(new checkIfAliveTask(), mCheckAlivePeriod, mCheckAlivePeriod);
		}
	}
	
	public void stopTimerCheckAlive(){
		if(mTimerCheckAlive!=null){
			mTimerCheckAlive.cancel();
			mTimerCheckAlive.purge();
			mTimerCheckAlive = null;
		}
	}
	
	/**
	 * @author Lim
	 * Used to check if the connection is alive 
	 */
	private class checkIfAliveTask extends TimerTask {

		@Override
		public void run() {
			if (!mIamAlive){
				mCountDeadConnection++;
				System.out.println("CHECKALIVETASK");
				if(isThisVerCompatibleWith(FW_ID.SHIMMER3.LOGANDSTREAM, 0, 5, 11)){
					writeTestConnectionCommand();
				} else {
					writeLEDCommand(0);
				}
				if (mCountDeadConnection>5){
					setState(BT_STATE.NONE);
				}
			} else {
				mCountDeadConnection = 0;
				mIamAlive=false;
			}
		}

	}
	
	public void startTimerReadBattStatus(){
		if(mFirmwareVersionCode>=6){
			if(mTimerReadBattStatus==null){ 
				mTimerReadBattStatus = new Timer();
			}
			mTimerReadBattStatus.schedule(new readBattStatusTask(), mReadBattStatusPeriod, mReadBattStatusPeriod);
		}
	}
	
	public void stopTimerReadBattStatus(){
		if(mTimerReadBattStatus!=null){
			mTimerReadBattStatus.cancel();
			mTimerReadBattStatus.purge();
			mTimerReadBattStatus = null;
		}
	}
	
	/**
	 * Used to check the current battery status 
	 */
	private class readBattStatusTask extends TimerTask {

		@Override
		public void run() {
			System.out.println("READBATTTASK");
			readBattery();
		}

	}
	
	//endregion
	
	
	//region --------- WRITE FUNCTIONS --------- 
	
	
	/**
	 * writeGyroSamplingRate(range) sets the GyroSamplingRate on the Shimmer (version 3) to the value of the input range. Note that when using writesamplingrate this value will be overwritten based on the lowpowergyro mode setting.
	 * @param rate it is a value between 0 and 255; 6 = 1152Hz, 77 = 102.56Hz, 255 = 31.25Hz
	 */
	public void writeGyroSamplingRate(int rate) {
		if (mHardwareVersion == HW_ID.SHIMMER_3){
			mTempIntValue=rate;
			getListofInstructions().add(new byte[]{SET_MPU9150_SAMPLING_RATE_COMMAND, (byte)rate});
		}
	}
	
	/**
	 * writeMagSamplingRate(range) sets the MagSamplingRate on the Shimmer to the value of the input range. Note that when using writesamplingrate this value will be overwritten based on the lowpowermag mode setting.
	 * @param rate for Shimmer 2 it is a value between 1 and 6; 0 = 0.5 Hz; 1 = 1.0 Hz; 2 = 2.0 Hz; 3 = 5.0 Hz; 4 = 10.0 Hz; 5 = 20.0 Hz; 6 = 50.0 Hz, for Shimmer 3 it is a value between 0-7; 0 = 0.75Hz; 1 = 1.5Hz; 2 = 3Hz; 3 = 7.5Hz; 4 = 15Hz ; 5 = 30 Hz; 6 = 75Hz ; 7 = 220Hz 
	 * 
	 * */
	public void writeMagSamplingRate(int rate) {
		if (mFirmwareVersionParsed.equals("BoilerPlate 0.1.0")){
		} else {
			mTempIntValue=rate;
			getListofInstructions().add(new byte[]{SET_MAG_SAMPLING_RATE_COMMAND, (byte)rate});
		}
	}
	
	/**
	 * writeAccelSamplingRate(range) sets the AccelSamplingRate on the Shimmer (version 3) to the value of the input range. Note that when using writesamplingrate this value will be overwritten based on the lowpowerwraccel mode setting.
	 * @param rate it is a value between 1 and 7; 1 = 1 Hz; 2 = 10 Hz; 3 = 25 Hz; 4 = 50 Hz; 5 = 100 Hz; 6 = 200 Hz; 7 = 400 Hz
	 */
	public void writeAccelSamplingRate(int rate) {
		if (mHardwareVersion == HW_ID.SHIMMER_3){
			mTempIntValue=rate;
			getListofInstructions().add(new byte[]{SET_ACCEL_SAMPLING_RATE_COMMAND, (byte)rate});
		}
	}
	
	/**
	 * Transmits a command to the Shimmer device to enable the sensors. To enable multiple sensors an or operator should be used (e.g. writeEnabledSensors(SENSOR_ACCEL|SENSOR_GYRO|SENSOR_MAG)). Command should not be used consecutively. Valid values are SENSOR_ACCEL, SENSOR_GYRO, SENSOR_MAG, SENSOR_ECG, SENSOR_EMG, SENSOR_GSR, SENSOR_EXP_BOARD_A7, SENSOR_EXP_BOARD_A0, SENSOR_BRIDGE_AMP and SENSOR_HEART.
    SENSOR_BATT
	 * @param enabledSensors e.g SENSOR_ACCEL|SENSOR_GYRO|SENSOR_MAG
	 */
	public void writeEnabledSensors(long enabledSensors) {
		
		if (!sensorConflictCheck(enabledSensors)){ //sensor conflict check
		
		} else {
			enabledSensors=generateSensorBitmapForHardwareControl(enabledSensors);
			tempEnabledSensors=enabledSensors;

			byte secondByte=(byte)((enabledSensors & 65280)>>8);
			byte firstByte=(byte)(enabledSensors & 0xFF);

			//write(new byte[]{SET_SENSORS_COMMAND,(byte) lowByte, highByte});
			if (mHardwareVersion == HW_ID.SHIMMER_3){
				byte thirdByte=(byte)((enabledSensors & 16711680)>>16);

				getListofInstructions().add(new byte[]{SET_SENSORS_COMMAND,(byte) firstByte,(byte) secondByte,(byte) thirdByte});
			} else {
				getListofInstructions().add(new byte[]{SET_SENSORS_COMMAND,(byte) firstByte,(byte) secondByte});
			}
			inquiry();
			
		}
	}
		
	/**
	 * writePressureResolution(range) sets the resolution of the pressure sensor on the Shimmer3
	 * @param settinge Numeric value defining the desired resolution of the pressure sensor. Valid range settings are 0 (low), 1 (normal), 2 (high), 3 (ultra high)
	 * 
	 * */
	public void writePressureResolution(int setting) {
		if (mHardwareVersion==HW_ID.SHIMMER_3){
			getListofInstructions().add(new byte[]{SET_BMP180_PRES_RESOLUTION_COMMAND, (byte)setting});
		}
	}

	/**
	 * writeAccelRange(range) sets the Accelerometer range on the Shimmer to the value of the input range. When setting/changing the accel range, please ensure you have the correct calibration parameters. Note that the Shimmer device can only carry one set of accel calibration parameters at a single time.
	 * @param range is a numeric value defining the desired accelerometer range. Valid range setting values for the Shimmer 2 are 0 (+/- 1.5g), 1 (+/- 2g), 2 (+/- 4g) and 3 (+/- 6g). Valid range setting values for the Shimmer 2r are 0 (+/- 1.5g) and 3 (+/- 6g).
	 */
	public void writeAccelRange(int range) {
		getListofInstructions().add(new byte[]{SET_ACCEL_SENSITIVITY_COMMAND, (byte)range});
		mAccelRange=(int)range;
		
	}
	
	
	/** Only applicable for Log and Stream
	 * @param channel The derived channels (3 bytes), first array element = MSB (channel[0]), and channel[2]) = LSB
	 */
	public void writeDerivedChannelBytes(byte[] channel) {
		if (mFirmwareIdentifier==FW_ID.SHIMMER3.LOGANDSTREAM){
			getListofInstructions().add(new byte[]{SET_DERIVED_CHANNEL_BYTES, channel[0], channel[1], channel[2]});
		}
		
	}

	/**Write the current shimmer name to the Shimmer device. Only applicable for Log and Stream.
	 * 
	 */
	public void writeShimmerName(){
		if (mFirmwareIdentifier==FW_ID.SHIMMER3.LOGANDSTREAM){
			writeShimmerUserAssignedName(getShimmerUserAssignedName());
		}
	}
	
	/**Writes trial config, note only userbutton only works (FW)
	 * 
	 */
	public void writeTrial(){
		if (mFirmwareIdentifier==FW_ID.SHIMMER3.LOGANDSTREAM){
			byte[] trial_config_byte = combineTrialConfig();
			byte[] tosend = new byte[4];
			tosend[0] = SET_TRIAL_CONFIG_COMMAND;
			tosend[1] = trial_config_byte[0];
			tosend[2] = trial_config_byte[1];
			tosend[3] = (byte)getSyncBroadcastInterval();
			getListofInstructions().add(tosend);
		}
	}
	
//	public void writeInfoMem(int address, byte[] infoMemBytes) {
//		if ((Util.compareVersions(
//				this.mFirmwareIdentifier, 
//				this.mFirmwareVersionMajor,
//				this.mFirmwareVersionMinor, 
//				this.mFirmwareVersionInternal, 
//				FW_ID.SHIMMER3.BTSTREAM, 
//				0,7,2))
//				||(Util.compareVersions(
//						this.mFirmwareIdentifier, 
//						this.mFirmwareVersionMajor,
//						this.mFirmwareVersionMinor, 
//						this.mFirmwareVersionInternal, 
//						FW_ID.SHIMMER3.LOGANDSTREAM, 
//						0,5,4))){
//			
//			
//			byte[] trial_config_byte = combineTrialConfig();
//			byte dataLength = (byte)(infoMemBytes.length&0xFF);
//			byte[] tosend = new byte[4+dataLength];
//			tosend[0] = SET_INFOMEM_COMMAND;
//			tosend[1] = dataLength;
//			tosend[2] = (byte)((address>>8)&0xFF);
//			tosend[3] = (byte)(address&0xFF);
//			tosend[4] = (byte)getSyncBroadcastInterval();
//			getmListofInstructions().add(tosend);
//		}
//	}

    public void fillTrialShimmer3(byte[] packet)
    {
        SplitTrialConfig(packet[0] + (packet[1] << 8));
        setSyncBroadcastInterval((int)packet[2]);
    }
	
    // btsd changes
    public void SplitTrialConfig(int val)
    {
        //trialConfig = val;
        mSync = ((val >> 2) & 0x01) == 1;
        setButtonStart(((val >> 5) & 0x01) == 1); // currently FW only supports this
        setMasterShimmer(((val >> 1) & 0x01) == 1);
        setSingleTouch(((val >> 15) & 0x01) == 1);
        setTCXO(((val >> 12) & 0x01) == 1);
        setInternalExpPower(((val >> 11) & 0x01) == 1);
        //monitor = ((val >> 10) & 0x01) == 1;
        
    }
    
	// btsd changes
    public byte[] combineTrialConfig()
    {
        short trialConfig = (short) ((((mSync ? 1 : 0) & 0x01) << 2) +
                      (((isButtonStart() ? 1 : 0) & 0x01) << 5) +  //currently only this is supported
                      (((isMasterShimmer() ? 1 : 0) & 0x01) << 1) +
                      (((isSingleTouch() ? 1 : 0) & 0x01) << 15) +
                      (((isTCXO() ? 1 : 0) & 0x01) << 12) +
                      (((isInternalExpPower() ? 1 : 0) & 0x01) << 11) +
                      (((true ? 1 : 0) & 0x01) << 10));
                      //(((monitor ? 1 : 0) & 0x01) << 10);
    	/*short trialConfig = (short) ((((true ? 1 : 0) & 0x01) << 2) +
                (((true ? 1 : 0) & 0x01) << 5) +
                (((true ? 1 : 0) & 0x01) << 1) +
                (((true ? 1 : 0) & 0x01) << 15) +
                (((true ? 1 : 0) & 0x01) << 12) +
                (((true ? 1 : 0) & 0x01) << 11) +
                (((true ? 1 : 0) & 0x01) << 10));*/
        byte[] ret = new byte[2];
        ret[0] = (byte)(trialConfig & 0xff);
        ret[1] = (byte)((trialConfig >> 8) & 0xff);
        return ret;
    }
	
	/**Write the config time to the Shimmer device. Only applicable for Log and Stream.
	 * 
	 */
	public void writeConfigTime(String time){
		if (mFirmwareIdentifier==FW_ID.SHIMMER3.LOGANDSTREAM){
			byte[] toSendTime = time.getBytes();
			byte[] toSend = new byte[2+toSendTime.length];
			toSend[0]= SET_CONFIGTIME_COMMAND;
			toSend[1]= (byte)toSendTime.length;
			System.arraycopy(toSendTime, 0, toSend, 2, toSendTime.length);
			getListofInstructions().add(toSend);
		}
	}

	/**Write the config time to the Shimmer device. Only applicable for Log and Stream.
	 * 
	 */
	public void writeConfigTime(long time){
		if (mFirmwareIdentifier==FW_ID.SHIMMER3.LOGANDSTREAM){
			String timeString = Long.toString(time);
			byte[] toSendTime = timeString.getBytes();
			byte[] toSend = new byte[2+toSendTime.length];
			toSend[0]= SET_CONFIGTIME_COMMAND;
			toSend[1]= (byte)toSendTime.length;
			System.arraycopy(toSendTime, 0, toSend, 2, toSendTime.length);
			getListofInstructions().add(toSend);
		}
	}

	
	/**
	 * @param name Name to write to shimmer device
	 */
	public void writeShimmerUserAssignedName(String name){
		if (mFirmwareIdentifier==FW_ID.SHIMMER3.LOGANDSTREAM){
			byte[] toSendName = name.getBytes();
			byte[] toSend = new byte[2+toSendName.length];
			toSend[0]= SET_SHIMMERNAME_COMMAND;
			toSend[1]= (byte)toSendName.length;
			System.arraycopy(toSendName, 0, toSend, 2, toSendName.length);
			getListofInstructions().add(toSend);
		}
	}
	

	/** 
	 * @param center 
	 */
	public void writeCenter(String center){
		if (mFirmwareIdentifier==FW_ID.SHIMMER3.LOGANDSTREAM){
			byte[] toSendCenter = center.getBytes();
			byte[] toSend = new byte[2+toSendCenter.length];
			toSend[0]= SET_CENTER_COMMAND;
			toSend[1]= (byte)toSendCenter.length;
			System.arraycopy(toSendCenter, 0, toSend, 2, toSendCenter.length);
			getListofInstructions().add(toSend);
		}
	}
	
	
	/**
	 * @param name Name to write to shimmer device
	 */
	public void writeExperimentName(String name){
		if (mFirmwareIdentifier==FW_ID.SHIMMER3.LOGANDSTREAM){
			byte[] toSendName = name.getBytes();
			byte[] toSend = new byte[2+toSendName.length];
			toSend[0]= SET_EXPID_COMMAND;
			toSend[1]= (byte)toSendName.length;
			System.arraycopy(toSendName, 0, toSend, 2, toSendName.length);
			getListofInstructions().add(toSend);
		}
	}
	
	
	/**Gets pc time and writes the 8 byte value to shimmer device
	 * 
	 */
	public void writeRealWorldClock(){
		if (mFirmwareIdentifier==FW_ID.SHIMMER3.LOGANDSTREAM){
			long systemTime = System.currentTimeMillis();
			byte[] bytearray=ByteBuffer.allocate(8).putLong(systemTime).array();
			ArrayUtils.reverse(bytearray);
		    byte[] bytearraycommand= new byte[9];
			bytearraycommand[0]=SET_RWC_COMMAND;
			System.arraycopy(bytearray, 0, bytearraycommand, 1, 8);
			getListofInstructions().add(bytearraycommand);
		}
	}
	
	public void readRealWorldClock(){
		if (mFirmwareIdentifier==FW_ID.SHIMMER3.LOGANDSTREAM){
			getListofInstructions().add(new byte[]{GET_RWC_COMMAND});
		}
	}
	
	/**Read the derived channel bytes. Currently only supported on logandstream
	 * 
	 */
	public void readDerivedChannelBytes() {
		if (mFirmwareIdentifier==FW_ID.SHIMMER3.LOGANDSTREAM){
			getListofInstructions().add(new byte[]{GET_DERIVED_CHANNEL_BYTES});
		}
	}
	
	public void readShimmerName(){
		if (mFirmwareIdentifier==FW_ID.SHIMMER3.LOGANDSTREAM){
			getListofInstructions().add(new byte[]{GET_SHIMMERNAME_COMMAND});
		}
	}
	
	public void readExperimentName(){
		if (mFirmwareIdentifier==FW_ID.SHIMMER3.LOGANDSTREAM){
			getListofInstructions().add(new byte[]{GET_EXPID_COMMAND});
		}
	}

	public void readTrial(){
		if (mFirmwareIdentifier==FW_ID.SHIMMER3.LOGANDSTREAM){
			getListofInstructions().add(new byte[]{GET_TRIAL_CONFIG_COMMAND});
		}
	}
	
	public void readConfigTime(){
		if (mFirmwareIdentifier==FW_ID.SHIMMER3.LOGANDSTREAM){
			getListofInstructions().add(new byte[]{GET_CONFIGTIME_COMMAND});
		}
	}
	
	public void readCenter(){
		if (mFirmwareIdentifier==FW_ID.SHIMMER3.LOGANDSTREAM){
			getListofInstructions().add(new byte[]{GET_CENTER_COMMAND});
		}
	}
	
	/**
	 * writeGyroRange(range) sets the Gyroscope range on the Shimmer3 to the value of the input range. When setting/changing the range, please ensure you have the correct calibration parameters.
	 * @param range is a numeric value defining the desired gyroscope range. 
	 */
	public void writeGyroRange(int range) {
		if (mHardwareVersion==HW_ID.SHIMMER_3){
			getListofInstructions().add(new byte[]{SET_MPU9150_GYRO_RANGE_COMMAND, (byte)range});
			mGyroRange=(int)range;
		}
	}

	/**
	 * @param rate Defines the sampling rate to be set (e.g.51.2 sets the sampling rate to 51.2Hz). User should refer to the document Sampling Rate Table to see all possible values.
	 */
	public void writeSamplingRate(double rate) {
		if (mIsInitialised=true) {
			setShimmerSamplingRate(rate);
			if (mHardwareVersion==HW_ID.SHIMMER_2 || mHardwareVersion==HW_ID.SHIMMER_2R){

				writeMagSamplingRate(mShimmer2MagRate);
				
				int samplingByteValue = (int) (1024/mShimmerSamplingRate); //the equivalent hex setting
				getListofInstructions().add(new byte[]{SET_SAMPLING_RATE_COMMAND, (byte)Math.rint(samplingByteValue), 0x00});
			} else if (mHardwareVersion==HW_ID.SHIMMER_3) {
				
				writeMagSamplingRate(mLSM303MagRate);
				writeAccelSamplingRate(mLSM303DigitalAccelRate);
				writeGyroSamplingRate(mMPU9150GyroAccelRate);
				
				int samplingByteValue = (int) (32768/mShimmerSamplingRate);
				getListofInstructions().add(new byte[]{SET_SAMPLING_RATE_COMMAND, (byte)(samplingByteValue&0xFF), (byte)((samplingByteValue>>8)&0xFF)});
			}
		}
		
//		if (mInitialized=true) {
//
//			if (mShimmerVersion==HW_ID.SHIMMER_2 || mShimmerVersion==HW_ID.SHIMMER_2R){
//				if (!mLowPowerMag){
//					if (rate<=10) {
//						writeMagSamplingRate(4);
//					} else if (rate<=20) {
//						writeMagSamplingRate(5);
//					} else {
//						writeMagSamplingRate(6);
//					}
//				} else {
//					writeMagSamplingRate(4);
//				}
//				rate=1024/rate; //the equivalent hex setting
//				mListofInstructions.add(new byte[]{SET_SAMPLING_RATE_COMMAND, (byte)Math.rint(rate), 0x00});
//			} else if (mShimmerVersion==HW_ID.SHIMMER_3) {
//				if (!mLowPowerMag){
//					if (rate<=1) {
//						writeMagSamplingRate(1);
//					} else if (rate<=15) {
//						writeMagSamplingRate(4);
//					} else if (rate<=30){
//						writeMagSamplingRate(5);
//					} else if (rate<=75){
//						writeMagSamplingRate(6);
//					} else {
//						writeMagSamplingRate(7);
//					}
//				} else {
//					if (rate >=10){
//						writeMagSamplingRate(4);
//					} else {
//						writeMagSamplingRate(1);
//					}
//				}
//
//				if (!mLowPowerAccelWR){
//					if (rate<=1){
//						writeAccelSamplingRate(1);
//					} else if (rate<=10){
//						writeAccelSamplingRate(2);
//					} else if (rate<=25){
//						writeAccelSamplingRate(3);
//					} else if (rate<=50){
//						writeAccelSamplingRate(4);
//					} else if (rate<=100){
//						writeAccelSamplingRate(5);
//					} else if (rate<=200){
//						writeAccelSamplingRate(6);
//					} else {
//						writeAccelSamplingRate(7);
//					}
//				}
//				else {
//					if (rate>=10){
//						writeAccelSamplingRate(2);
//					} else {
//						writeAccelSamplingRate(1);
//					}
//				}
//
//				if (!mLowPowerGyro){
//					if (rate<=51.28){
//						writeGyroSamplingRate(0x9B);
//					} else if (rate<=102.56){
//						writeGyroSamplingRate(0x4D);
//					} else if (rate<=129.03){
//						writeGyroSamplingRate(0x3D);
//					} else if (rate<=173.91){
//						writeGyroSamplingRate(0x2D);
//					} else if (rate<=205.13){
//						writeGyroSamplingRate(0x26);
//					} else if (rate<=258.06){
//						writeGyroSamplingRate(0x1E);
//					} else if (rate<=533.33){
//						writeGyroSamplingRate(0xE);
//					} else {
//						writeGyroSamplingRate(6);
//					}
//				}
//				else {
//					writeGyroSamplingRate(0xFF);
//				}
//
//				
//
//				int samplingByteValue = (int) (32768/rate);
//				mListofInstructions.add(new byte[]{SET_SAMPLING_RATE_COMMAND, (byte)(samplingByteValue&0xFF), (byte)((samplingByteValue>>8)&0xFF)});
//
//
//
//
//			}
//		}
	}
	
	/**
	 * Only supported on Shimmer3, note that unlike previous write commands where the values are only set within the instrument driver after the ACK is received, this is set immediately. Fail safe should the settings not be actually set successfully is a timeout will occur, and the ID will disconnect from the device
	 * This function set the treshold of the ExG Lead-Off Comparator. There are 8 possible values:
	 * 1. Pos:95% - Neg:5%, 2. Pos:92.5% - Neg:7.5%, 3. Pos:90% - Neg:10%, 4. Pos:87.5% - Neg:12.5%, 5. Pos:85% - Neg:15%,
	 * 6. Pos:80% - Neg:20%, 7. Pos:75% - Neg:25%, 8. Pos:70% - Neg:30%
	 * @param treshold where 0 = 95-5, 1 = 92.5-7.5, 2 = 90-10, 3 = 87.5-12.5, 4 = 85-15, 5 = 80-20, 6 = 75-25, 7 = 70-30
	 */
	public void writeEXGLeadOffComparatorTreshold(int treshold){
		if(mFirmwareVersionCode>2){
			if(treshold >=0 && treshold<8){ 
				byte[] reg1 = mEXG1RegisterArray;
				byte[] reg2 = mEXG2RegisterArray;
				byte currentLeadOffTresholdChip1 = reg1[2];
				byte currentLeadOffTresholdChip2 = reg2[2];
				currentLeadOffTresholdChip1 = (byte) (currentLeadOffTresholdChip1 & 31);
				currentLeadOffTresholdChip2 = (byte) (currentLeadOffTresholdChip2 & 31);
				currentLeadOffTresholdChip1 = (byte) (currentLeadOffTresholdChip1 | (treshold<<5));
				currentLeadOffTresholdChip2 = (byte) (currentLeadOffTresholdChip2 | (treshold<<5));
				getListofInstructions().add(new byte[]{SET_EXG_REGS_COMMAND,(byte) 0,0,10,reg1[0],reg1[1],currentLeadOffTresholdChip1,reg1[3],reg1[4],reg1[5],reg1[6],reg1[7],reg1[8],reg1[9]});
				getListofInstructions().add(new byte[]{SET_EXG_REGS_COMMAND,(byte) 1,0,10,reg2[0],reg2[1],currentLeadOffTresholdChip2,reg2[3],reg2[4],reg2[5],reg2[6],reg2[7],reg2[8],reg2[9]});
			}
		}
	}
	
	
	/**
	 * Only supported on Shimmer3, note that unlike previous write commands where the values are only set within the instrument driver after the ACK is received, this is set immediately. Fail safe should the settings not be actually set successfully is a timeout will occur, and the ID will disconnect from the device
	 * This function set the ExG Lead-Off Current. There are 4 possible values: 6nA (default), 22nA, 6uA and 22uA.
	 * @param LeadOffCurrent where 0 = 6nA, 1 = 22nA, 2 = 6uA and 3 = 22uA
	 */
	public void writeEXGLeadOffDetectionCurrent(int leadOffCurrent){
		if(mFirmwareVersionCode>2){
			if(leadOffCurrent >=0 && leadOffCurrent<4){
				byte[] reg1 = mEXG1RegisterArray;
				byte[] reg2 = mEXG2RegisterArray;
				byte currentLeadOffDetectionCurrentChip1 = reg1[2];
				byte currentLeadOffDetectionCurrentChip2 = reg2[2];
				currentLeadOffDetectionCurrentChip1 = (byte) (currentLeadOffDetectionCurrentChip1 & 243);
				currentLeadOffDetectionCurrentChip2 = (byte) (currentLeadOffDetectionCurrentChip2 & 243);
				currentLeadOffDetectionCurrentChip1 = (byte) (currentLeadOffDetectionCurrentChip1 | (leadOffCurrent<<2));
				currentLeadOffDetectionCurrentChip2 = (byte) (currentLeadOffDetectionCurrentChip2 | (leadOffCurrent<<2));
				getListofInstructions().add(new byte[]{SET_EXG_REGS_COMMAND,(byte) 0,0,10,reg1[0],reg1[1],currentLeadOffDetectionCurrentChip1,reg1[3],reg1[4],reg1[5],reg1[6],reg1[7],reg1[8],reg1[9]});
				getListofInstructions().add(new byte[]{SET_EXG_REGS_COMMAND,(byte) 1,0,10,reg2[0],reg2[1],currentLeadOffDetectionCurrentChip2,reg2[3],reg2[4],reg2[5],reg2[6],reg2[7],reg2[8],reg2[9]});
			}
		}
	}
	
	
	/**
	 * Only supported on Shimmer3
	 * This function set the ExG Lead-Off detection mode. There are 3 possible modes: DC Current, AC Current (not supported yet), and Off.
	 * @param detectionMode where 0 = Off, 1 = DC Current, and 2 = AC Current
	 */
	public void writeEXGLeadOffDetectionMode(int detectionMode){
		
		if(mFirmwareVersionCode>2){
			if(detectionMode == 0){
				mLeadOffDetectionMode = detectionMode;
				byte[] reg1 = mEXG1RegisterArray;
				byte[] reg2 = mEXG2RegisterArray;
				byte currentComparatorChip1 = reg1[1];
				byte currentComparatorChip2 = reg2[1];
				currentComparatorChip1 = (byte) (currentComparatorChip1 & 191);
				currentComparatorChip2 = (byte) (currentComparatorChip2 & 191);
				byte currentRDLSense = reg1[5];
				currentRDLSense = (byte) (currentRDLSense & 239);
				byte current2P1N1P = reg1[6];
				current2P1N1P = (byte) (current2P1N1P & 240);
				byte current2P = reg2[6];
				current2P = (byte) (current2P & 240);
				getListofInstructions().add(new byte[]{SET_EXG_REGS_COMMAND,(byte) 0,0,10,reg1[0],currentComparatorChip1,reg1[2],reg1[3],reg1[4],currentRDLSense,current2P1N1P,reg1[7],reg1[8],reg1[9]});
				if(isEXGUsingDefaultEMGConfiguration()){
					byte currentEMGConfiguration = reg2[4];
					currentEMGConfiguration = (byte) (currentEMGConfiguration & 127);
					currentEMGConfiguration = (byte) (currentEMGConfiguration | 128);
					getListofInstructions().add(new byte[]{SET_EXG_REGS_COMMAND,(byte) 1,0,10,reg2[0],currentComparatorChip2,reg2[2],reg2[3],currentEMGConfiguration,reg2[5],current2P,reg2[7],reg2[8],reg2[9]});
				}
				else
					getListofInstructions().add(new byte[]{SET_EXG_REGS_COMMAND,(byte) 1,0,10,reg2[0],currentComparatorChip2,reg2[2],reg2[3],reg2[4],reg2[5],current2P,reg2[7],reg2[8],reg2[9]});
			}
			else if(detectionMode == 1){
				mLeadOffDetectionMode = detectionMode;
				
				byte[] reg1 = mEXG1RegisterArray;
				byte[] reg2 = mEXG2RegisterArray;
				byte currentDetectionModeChip1 = reg1[2];
				byte currentDetectionModeChip2 = reg2[2];
				currentDetectionModeChip1 = (byte) (currentDetectionModeChip1 & 254);	// set detection mode chip1 
				currentDetectionModeChip2 = (byte) (currentDetectionModeChip2 & 254);  // set detection mode chip2
				byte currentComparatorChip1 = reg1[1];
				byte currentComparatorChip2 = reg2[1];
				currentComparatorChip1 = (byte) (currentComparatorChip1 & 191);	
				currentComparatorChip2 = (byte) (currentComparatorChip2 & 191);
				currentComparatorChip1 = (byte) (currentComparatorChip1 | 64); // set comparator chip1 
				currentComparatorChip2 = (byte) (currentComparatorChip2 | 64); // set comparator chip2 
				byte currentRDLSense = reg1[5];
				currentRDLSense = (byte) (currentRDLSense & 239); 
				currentRDLSense = (byte) (currentRDLSense | 16); // set RLD sense
				byte current2P1N1P = reg1[6];
				current2P1N1P = (byte) (current2P1N1P & 240);
				current2P1N1P = (byte) (current2P1N1P | 7); // set 2P, 1N, 1P
				byte current2P = reg2[6];
				current2P = (byte) (current2P & 240);
				current2P = (byte) (current2P | 4); // set 2P
				
				getListofInstructions().add(new byte[]{SET_EXG_REGS_COMMAND,(byte) 0,0,10,reg1[0], currentComparatorChip1, currentDetectionModeChip1,reg1[3],reg1[4], currentRDLSense, current2P1N1P,reg1[7],reg1[8],reg1[9]});
				if(isEXGUsingDefaultEMGConfiguration()){ //if the EMG configuration is used, then enable the chanel 2 since it is needed for the Lead-off detection
					byte currentEMGConfiguration = reg2[4];
					currentEMGConfiguration = (byte) (currentEMGConfiguration & 127);
					getListofInstructions().add(new byte[]{SET_EXG_REGS_COMMAND,(byte) 1,0,10,reg2[0], currentComparatorChip2, currentDetectionModeChip2,reg2[3],currentEMGConfiguration,reg2[5],current2P,reg2[7],reg2[8],reg2[9]});
				}
				else
					getListofInstructions().add(new byte[]{SET_EXG_REGS_COMMAND,(byte) 1,0,10,reg2[0], currentComparatorChip2, currentDetectionModeChip2,reg2[3],reg2[4],reg2[5],current2P,reg2[7],reg2[8],reg2[9]});
			}
			else if(detectionMode == 2){
				mLeadOffDetectionMode = detectionMode;
				//NOT SUPPORTED YET
			}
		}
	}
	
	/**
	 * Only supported on Shimmer3, note that unlike previous write commands where the values are only set within the instrument driver after the ACK is received, this is set immediately. Fail safe should the settings not be actually set successfully is a timeout will occur, and the ID will disconnect from the device
	 * This function set the ExG reference electrode. There are 2 possible values when using ECG configuration: Inverse Wilson CT (default) and Fixed Potential
	 * and 2 possible values when using EMG configuration: Fixed Potential (default) and Inverse of Ch 1
	 * @param referenceElectrode reference electrode code where 0 = Fixed Potential and 13 = Inverse Wilson CT (default) for an ECG configuration, and
	 * 													where 0 = Fixed Potential (default) and 3 = Inverse Ch1 for an EMG configuration
	 */
	public void writeEXGReferenceElectrode(int referenceElectrode){
		if (mFirmwareVersionCode>2){
			byte currentByteValue = mEXG1RegisterArray[5];
			byte[] reg = mEXG1RegisterArray;
			currentByteValue = (byte) (currentByteValue & 240);
			currentByteValue = (byte) (currentByteValue | referenceElectrode);
			getListofInstructions().add(new byte[]{SET_EXG_REGS_COMMAND,(byte) 0,0,10,reg[0],reg[1],reg[2],reg[3],reg[4],currentByteValue,reg[6],reg[7],reg[8],reg[9]});
		}
	}

	/**
	 * Only supported on Shimmer3, note that unlike previous write commands where the values are only set within the instrument driver after the ACK is received, this is set immediately. Fail safe should the settings not be actually set successfully is a timeout will occur, and the ID will disconnect from the device
	 * @param chipID Either a 1 or 2 value
	 * @param rateSettingsam , where 0=125SPS ; 1=250SPS; 2=500SPS; 3=1000SPS; 4=2000SPS  
	 */
	public void writeEXGRateSetting(int chipID, int rateSetting){
		if ((mFirmwareVersionInternal >=8 && mFirmwareVersionCode==2) || mFirmwareVersionCode>2){
			if (chipID==1 || chipID==2){
				if (chipID==1){
					byte currentByteValue = mEXG1RegisterArray[0];
					byte[] reg = mEXG1RegisterArray;
					currentByteValue = (byte) (currentByteValue & 248);
					currentByteValue = (byte) (currentByteValue | rateSetting);
					getListofInstructions().add(new byte[]{SET_EXG_REGS_COMMAND,(byte) (chipID-1),0,10,currentByteValue,reg[1],reg[2],reg[3],reg[4],reg[5],reg[6],reg[7],reg[8],reg[9]});
				} else if (chipID==2){
					byte currentByteValue = mEXG2RegisterArray[0];
					byte[] reg = mEXG2RegisterArray;
					currentByteValue = (byte) (currentByteValue & 248);
					currentByteValue = (byte) (currentByteValue | rateSetting);
					getListofInstructions().add(new byte[]{SET_EXG_REGS_COMMAND,(byte) (chipID-1),0,10,currentByteValue,reg[1],reg[2],reg[3],reg[4],reg[5],reg[6],reg[7],reg[8],reg[9]});
				}
			}
		}
	}
	
	
	/**
	 * This is only supported on SHimmer3,, note that unlike previous write commands where the values are only set within the instrument driver after the ACK is received, this is set immediately. Fail safe should the settings not be actually set successfully is a timeout will occur, and the ID will disconnect from the device 
	 * @param chipID Either a 1 or 2 value
	 * @param gainSetting , where 0 = 6x Gain, 1 = 1x , 2 = 2x , 3 = 3x, 4 = 4x, 5 = 8x, 6 = 12x
	 * @param channel Either a 1 or 2 value
	 */
	public void writeEXGGainSetting(int chipID,  int channel, int gainSetting){
		if ((mFirmwareVersionInternal >=8 && mFirmwareVersionCode==2) || mFirmwareVersionCode>2){
			if ((chipID==1 || chipID==2) && (channel==1 || channel==2)){
				if (chipID==1){
					if (channel==1){
						byte currentByteValue = mEXG1RegisterArray[3];
						byte[] reg = mEXG1RegisterArray;
						currentByteValue = (byte) (currentByteValue & 143);
						currentByteValue = (byte) (currentByteValue | (gainSetting<<4));
						getListofInstructions().add(new byte[]{SET_EXG_REGS_COMMAND,(byte) (chipID-1),0,10,reg[0],reg[1],reg[2],currentByteValue,reg[4],reg[5],reg[6],reg[7],reg[8],reg[9]});
					} else {
						byte currentByteValue = mEXG1RegisterArray[4];
						byte[] reg = mEXG1RegisterArray;
						currentByteValue = (byte) (currentByteValue & 143);
						currentByteValue = (byte) (currentByteValue | (gainSetting<<4));
						getListofInstructions().add(new byte[]{SET_EXG_REGS_COMMAND,(byte) (chipID-1),0,10,reg[0],reg[1],reg[2],reg[3],currentByteValue,reg[5],reg[6],reg[7],reg[8],reg[9]});
					}
				} else if (chipID==2){
					if (channel==1){
						byte currentByteValue = mEXG2RegisterArray[3];
						byte[] reg = mEXG2RegisterArray;
						currentByteValue = (byte) (currentByteValue & 143);
						currentByteValue = (byte) (currentByteValue | (gainSetting<<4));
						getListofInstructions().add(new byte[]{SET_EXG_REGS_COMMAND,(byte) (chipID-1),0,10,reg[0],reg[1],reg[2],currentByteValue,reg[4],reg[5],reg[6],reg[7],reg[8],reg[9]});
					} else {
						byte currentByteValue = mEXG2RegisterArray[4];
						byte[] reg = mEXG2RegisterArray;
						currentByteValue = (byte) (currentByteValue & 143);
						currentByteValue = (byte) (currentByteValue | (gainSetting<<4));
						getListofInstructions().add(new byte[]{SET_EXG_REGS_COMMAND,(byte) (chipID-1),0,10,reg[0],reg[1],reg[2],reg[3],currentByteValue,reg[5],reg[6],reg[7],reg[8],reg[9]});
					}
				}
			}
		}
	}
	
	/**
	 * Only supported on Shimmer3, note that unlike previous write commands where the values are only set within the instrument driver after the ACK is received, this is set immediately. Fail safe should the settings not be actually set successfully is a timeout will occur, and the ID will disconnect from the device
	 * @param reg A 10 byte value
	 * @param chipID value can either be 1 or 2.
	 */
	public void writeEXGConfiguration(byte[] reg,int chipID){
		if ((mFirmwareVersionInternal >=8 && mFirmwareVersionCode==2) || mFirmwareVersionCode>2){
			if (chipID==1 || chipID==2){
				getListofInstructions().add(new byte[]{SET_EXG_REGS_COMMAND,(byte)(chipID-1),0,10,reg[0],reg[1],reg[2],reg[3],reg[4],reg[5],reg[6],reg[7],reg[8],reg[9]});
			}
		}}
		
	/**
	 * writeGSRRange(range) sets the GSR range on the Shimmer to the value of the input range. 
	 * @param range numeric value defining the desired GSR range. Valid range settings are 0 (10kOhm to 56kOhm), 1 (56kOhm to 220kOhm), 2 (220kOhm to 680kOhm), 3 (680kOhm to 4.7MOhm) and 4 (Auto Range).
	 */
	public void writeGSRRange(int range) {
		if (mHardwareVersion == HW_ID.SHIMMER_3){
			if (mFirmwareVersionCode!=1 || mFirmwareVersionInternal >4){
				getListofInstructions().add(new byte[]{SET_GSR_RANGE_COMMAND, (byte)range});
			}
		} else {
			getListofInstructions().add(new byte[]{SET_GSR_RANGE_COMMAND, (byte)range});
		}
	}
	
	/**
	 * writeMagRange(range) sets the MagSamplingRate on the Shimmer to the value of the input range. When setting/changing the accel range, please ensure you have the correct calibration parameters. Note that the Shimmer device can only carry one set of accel calibration parameters at a single time.
	 * @param range is the mag rang
	 */
	public void writeMagRange(int range) {
		if (mFirmwareVersionParsed.equals("BoilerPlate 0.1.0")){
		} else {
			getListofInstructions().add(new byte[]{SET_MAG_GAIN_COMMAND, (byte)range});
		}
	}

	public void writeLEDCommand(int command) {
		
//		if (mShimmerVersion!=HW_ID.SHIMMER_3){
			if (mFirmwareVersionParsed.equals("BoilerPlate 0.1.0")){
			} else {
				getListofInstructions().add(new byte[]{SET_BLINK_LED, (byte)command});
			}
//		}
	}

	public void writeTestConnectionCommand() {
		if (mFirmwareVersionCode>=6){
			getListofInstructions().add(new byte[]{TEST_CONNECTION_COMMAND});
		}
	}
	
	public void writeAccelCalibrationParameters(byte[] calibrationParameters) {
		cmdcalibrationParameters[0] = SET_ACCEL_CALIBRATION_COMMAND;
		System.arraycopy(calibrationParameters, 0, cmdcalibrationParameters, 1, 21);
		getListofInstructions().add(cmdcalibrationParameters);	
	}
	
	public void writeGyroCalibrationParameters(byte[] calibrationParameters) {
		cmdcalibrationParameters[0] = SET_GYRO_CALIBRATION_COMMAND;
		System.arraycopy(calibrationParameters, 0, cmdcalibrationParameters, 1, 21);
		getListofInstructions().add(cmdcalibrationParameters);	
	}
	
	public void writeMagCalibrationParameters(byte[] calibrationParameters) {
		cmdcalibrationParameters[0] = SET_MAG_CALIBRATION_COMMAND;
		System.arraycopy(calibrationParameters, 0, cmdcalibrationParameters, 1, 21);
		getListofInstructions().add(cmdcalibrationParameters);	
	}

	public void writeWRAccelCalibrationParameters(byte[] calibrationParameters) {
		if(mHardwareVersion==HW_ID.SHIMMER_3){
			cmdcalibrationParameters[0] = SET_LSM303DLHC_ACCEL_CALIBRATION_COMMAND;
			System.arraycopy(calibrationParameters, 0, cmdcalibrationParameters, 1, 21);
			getListofInstructions().add(cmdcalibrationParameters);	
		}
	}

	public void writeECGCalibrationParameters(int offsetrall, int gainrall,int offsetlall, int gainlall) {
		byte[] data = new byte[8];
		data[0] = (byte) ((offsetlall>>8)& 0xFF); //MSB offset
		data[1] = (byte) ((offsetlall)& 0xFF);
		data[2] = (byte) ((gainlall>>8)& 0xFF); //MSB gain
		data[3] = (byte) ((gainlall)& 0xFF);
		data[4] = (byte) ((offsetrall>>8)& 0xFF); //MSB offset
		data[5] = (byte) ((offsetrall)& 0xFF);
		data[6] = (byte) ((gainrall>>8)& 0xFF); //MSB gain
		data[7] = (byte) ((gainrall)& 0xFF);
		if (mFirmwareVersionParsed.equals("BoilerPlate 0.1.0")){
		} else {
			getListofInstructions().add(new byte[]{SET_ECG_CALIBRATION_COMMAND,data[0],data[1],data[2],data[3],data[4],data[5],data[6],data[7]});
		}
	}

	public void writeEMGCalibrationParameters(int offset, int gain) {
		byte[] data = new byte[4];
		data[0] = (byte) ((offset>>8)& 0xFF); //MSB offset
		data[1] = (byte) ((offset)& 0xFF);
		data[2] = (byte) ((gain>>8)& 0xFF); //MSB gain
		data[3] = (byte) ((gain)& 0xFF);
		if (mFirmwareVersionParsed.equals("BoilerPlate 0.1.0")){
		} else {
			getListofInstructions().add(new byte[]{SET_EMG_CALIBRATION_COMMAND,data[0],data[1],data[2],data[3]});
		}
	}
	
	/**
	 * writeBaudRate(value) sets the baud rate on the Shimmer. 
	 * @param value numeric value defining the desired Baud rate. Valid rate settings are 0 (115200 default),
	 *  1 (1200), 2 (2400), 3 (4800), 4 (9600) 5 (19200),
	 *  6 (38400), 7 (57600), 8 (230400), 9 (460800) and 10 (921600)
	 */
	public void writeBaudRate(int value) {
		if (mFirmwareVersionCode>=5){ 
			if(value>=0 && value<=10){
				mBluetoothBaudRate = value;
				getListofInstructions().add(new byte[]{SET_BAUD_RATE_COMMAND, (byte)value});
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				this.reconnect();
			}
			
		}
	}

	/**
	 * writeConfigByte0(configByte0) sets the config byte0 value on the Shimmer to the value of the input configByte0. 
	 * @param configByte0 is an unsigned 8 bit value defining the desired config byte 0 value.
	 */
	public void writeConfigByte0(byte configByte0) {
		getListofInstructions().add(new byte[]{SET_CONFIG_BYTE0_COMMAND,(byte) configByte0});
	}
	
	/**
	 * writeAccelRange(range) sets the Accelerometer range on the Shimmer to the value of the input range. When setting/changing the accel range, please ensure you have the correct calibration parameters. Note that the Shimmer device can only carry one set of accel calibration parameters at a single time.
	 * @param range is a numeric value defining the desired accelerometer range. Valid range setting values for the Shimmer 2 are 0 (+/- 1.5g), 1 (+/- 2g), 2 (+/- 4g) and 3 (+/- 6g). Valid range setting values for the Shimmer 2r are 0 (+/- 1.5g) and 3 (+/- 6g).
	 */
	public void writeBufferSize(int size) {
		getListofInstructions().add(new byte[]{SET_BUFFER_SIZE_COMMAND, (byte)size});
	}
	
	/**
	 * Sets the Pmux bit value on the Shimmer to the value of the input SETBIT. The PMux bit is the 2nd MSB of config byte0.
	 * @param setBit value defining the desired setting of the PMux (1=ON, 0=OFF).
	 */
	public void writePMux(int setBit) {
		getListofInstructions().add(new byte[]{SET_PMUX_COMMAND,(byte) setBit});
	}

	/**
	 * Sets the configGyroTempVref bit value on the Shimmer to the value of the input SETBIT. The configGyroTempVref bit is the 2nd MSB of config byte0.
	 * @param setBit value defining the desired setting of the Gyro Vref (1=ON, 0=OFF).
	 */
	/*public void writeConfigGyroTempVref(int setBit) {
    	while(getInstructionStatus()==false) {};
			//Bit value defining the desired setting of the PMux (1=ON, 0=OFF).
			if (setBit==1) {
				mTempByteValue=(byte) (mConfigByte0|32); 
			} else if (setBit==0) {
				mTempByteValue=(byte)(mConfigByte0 & 223);
			}
			mCurrentCommand=SET_GYRO_TEMP_VREF_COMMAND;
			write(new byte[]{SET_GYRO_TEMP_VREF_COMMAND,(byte) setBit});
			mWaitForAck=true;
			mTransactionCompleted=false;
			responseTimer(ACK_TIMER_DURATION);
	}*/

	/**
	 * Enable/disable the Internal Exp Power on the Shimmer3
	 * @param setBit value defining the desired setting of the Volt regulator (1=ENABLED, 0=DISABLED).
	 */
	public void writeInternalExpPower(int setBit) {
		if (mHardwareVersion == HW_ID.SHIMMER_3 && mFirmwareVersionCode>=2){
			getListofInstructions().add(new byte[]{SET_INTERNAL_EXP_POWER_ENABLE_COMMAND,(byte) setBit});
		} else {
			
		}
	}
	
	/**
	 * Enable/disable the 5 Volt Regulator on the Shimmer ExpBoard board
	 * @param setBit value defining the desired setting of the Volt regulator (1=ENABLED, 0=DISABLED).
	 */
	public void writeFiveVoltReg(int setBit) {
		getListofInstructions().add(new byte[]{SET_5V_REGULATOR_COMMAND,(byte) setBit});
	}
	
	//endregion
	
	
	//region --------- GET/SET FUNCTIONS --------- 
	
	/**** GET FUNCTIONS *****/

	/**
	 * This returns the variable mTransactionCompleted which indicates whether the Shimmer device is in the midst of a command transaction. True when no transaction is taking place. This is deprecated since the update to a thread model for executing commands
	 * @return mTransactionCompleted
	 */
	public boolean getInstructionStatus(){	
		boolean instructionStatus=false;
		if (mTransactionCompleted == true) {
			instructionStatus=true;
		} else {
			instructionStatus=false;
		}
		return instructionStatus;
	}
	
	public int getLowPowerAccelEnabled(){
		// TODO Auto-generated method stub
		if (mLowPowerAccelWR)
			return 1;
		else
			return 0;
	}

	public int getLowPowerGyroEnabled() {
		// TODO Auto-generated method stub
		if (mLowPowerGyro)
			return 1;
		else
			return 0;
	}

	public int getLowPowerMagEnabled() {
		// TODO Auto-generated method stub
		if (mLowPowerMag)
			return 1;
		else
			return 0;
	}
	
	public int getPacketSize(){
		return mPacketSize;
	}
	
	public boolean getInitialized(){
		return mIsInitialised;
	}

	public double getPacketReceptionRate(){
		return mPacketReceptionRate;
	}
	
	/**
	 * Get the 5V Reg. Only supported on Shimmer2/2R.
	 * @return 0 in case the 5V Reg is disableb, 1 in case the 5V Reg is enabled, and -1 in case the device doesn't support this feature
	 */
	public int get5VReg(){
		if(mHardwareVersion!=HW_ID.SHIMMER_3){
			if ((mConfigByte0 & (byte)128)!=0) {
				//then set ConfigByte0 at bit position 7
				return 1;
			} else {
				return 0;
			}
		}
		else
			return -1;
	}
	
	public String getDirectoryName(){
		if(mDirectoryName!=null)
			return mDirectoryName;
		else
			return "Directory not read yet";
	}

	public int getCurrentLEDStatus() {
		return mCurrentLEDStatus;
	}

	public int getBaudRate(){
		return mBluetoothBaudRate;
	}
	
	public int getReferenceElectrode(){
		return mEXGReferenceElectrode;
	}
	
	public int getLeadOffDetectionMode(){
		return mLeadOffDetectionMode;
	}
	
	public int getLeadOffDetectionCurrent(){
		return mEXGLeadOffDetectionCurrent;
	}
	
	public int getLeadOffComparatorTreshold(){
		return mEXGLeadOffComparatorTreshold;
	}
	
	public byte[] getExG1Register(){

	       return mEXG1RegisterArray;

	    }

	   

	public byte[] getExG2Register(){

	       return mEXG2RegisterArray;

	    }
	
	public int getExGComparatorsChip1(){
		return mEXG1Comparators;
	}
	
	public int getExGComparatorsChip2(){
		return mEXG2Comparators;
	}
	
//	public String getExpBoardID(){
//		
//		if(mExpBoardArray!=null){
////			if(mExpBoardName==null){
//				int boardID = mExpBoardArray[1] & 0xFF;
//				mExpansionBoardId = boardID;
//				int boardRev = mExpBoardArray[2] & 0xFF;
//				int specialRevision = mExpBoardArray[3] & 0xFF;
//				String boardName;
//				switch(boardID){
//					case 8:
//						boardName="Bridge Amplifier+";
//					break;
//					case 14:
//						boardName="GSR+";
//					break;
//					case 36:
//						boardName="PROTO3 Mini";
//					break;
//					case 37:
//						boardName="ExG";
//					break;
//					case 38:
//						boardName="PROTO3 Deluxe";
//					break;
//					default:
//						boardName="Unknown";
//					break;
//					
//				}
//				if(!boardName.equals("Unknown")){
//					boardName += " (SR" + boardID + "." + boardRev + "." + specialRevision +")";
//				}
//				
//				mExpBoardName = boardName;
////			}
//		}
//		else
//			return "Need to read ExpBoard ID first";
//		
//		return mExpBoardName;
//	}
	
	public double getBattLimitWarning(){
		return mLowBattLimit;
	}

	public int getShimmerVersion(){
		return mHardwareVersion;
	}

//	public String getShimmerName(){
//		return mShimmerUserAssignedName;
//	}
//	
//	public void setShimmerName(String name){
//		mShimmerUserAssignedName = name;
//	}
	
	/**
	 * Get the Gain value for the ExG1 Channel 1
	 * @return the value of the gain. The Gain can be 1, 2, 3, 4, 6 (default), 8 or 12. The function return -1 when it is not possible to get the value.
	 */
	public int getEXG1CH1GainValue(){
		
		int gain = -1;
		while(!getListofInstructions().isEmpty());
		int tmpGain = getExg1CH1GainValue();
		if(tmpGain==1 || tmpGain==2 || tmpGain==3 || tmpGain==4 || tmpGain==6 || tmpGain==8 || tmpGain==12){
			gain = tmpGain;
		}
		return gain;
	}
	
	/**
	 * Get the Gain value for the ExG1 Channel 1
	 * @return the value of the gain. The Gain can be 1, 2, 3, 4, 6 (default), 8 or 12. The function return -1 when it is not possible to get the value.
	 */
	public int getEXG1CH2GainValue(){
		
		int gain = -1;
		while(!getListofInstructions().isEmpty());
		int tmpGain = getExg1CH2GainValue();
		if(tmpGain==1 || tmpGain==2 || tmpGain==3 || tmpGain==4 || tmpGain==6 || tmpGain==8 || tmpGain==12){
			gain = tmpGain;
		}
		return gain;
	}
	
	/**
	 * Get the Gain value for the ExG1 Channel 1
	 * @return the value of the gain. The Gain can be 1, 2, 3, 4, 6 (default), 8 or 12. The function return -1 when it is not possible to get the value.
	 */
	public int getEXG2CH1GainValue(){
		
		int gain = -1;
		while(!getListofInstructions().isEmpty());
		int tmpGain = getExg2CH1GainValue();
		if(tmpGain==1 || tmpGain==2 || tmpGain==3 || tmpGain==4 || tmpGain==6 || tmpGain==8 || tmpGain==12){
			gain = tmpGain;
		}
		return gain;
	}

	/**
	 * Get the Gain value for the ExG1 Channel 1
	 * @return the value of the gain. The Gain can be 1, 2, 3, 4, 6 (default), 8 or 12. The function return -1 when it is not possible to get the value.
	 */
	public int getEXG2CH2GainValue(){
	
		int gain = -1;
		while(!getListofInstructions().isEmpty());
		int tmpGain = getExg2CH2GainValue();
		if(tmpGain==1 || tmpGain==2 || tmpGain==3 || tmpGain==4 || tmpGain==6 || tmpGain==8 || tmpGain==12){
			gain = tmpGain;
		}
		return gain;
	}
	
    public BT_STATE getBTState(){
        return mState;
    }

    /** Returns true if device is streaming (Bluetooth)
     * @return
     */
    public boolean isStreaming(){
    	return mIsStreaming;
    }
    
    /**** SET FUNCTIONS *****/
    
    /**
	 * 
	 * Register a callback to be invoked after buildmsg has executed (A new packet has been successfully received -> raw bytes interpreted into Raw and Calibrated Sensor data)
	 * 
	 * @param d The callback that will be invoked
	 */
	public void setDataProcessing(DataProcessing d) {
		mDataProcessing=d;
	}
    
	/**
	 * Set the battery voltage limit, when the Shimmer device goes below the limit while streaming the LED on the Shimmer device will turn Yellow, in order to use battery voltage monitoring the Battery has to be enabled. See writeenabledsensors. Only to be used with Shimmer2. Calibration also has to be enabled, see enableCalibration.
	 * @param limit
	 */
	public void setBattLimitWarning(double limit){
		mLowBattLimit=limit;
	}
	
	public void setContinuousSync(boolean continousSync){
		mContinousSync=continousSync;
	}
	
	//endregion
	
    
    //region --------- IS+something FUNCTIONS --------- 
    
    public boolean isLowPowerMagEnabled(){
		return mLowPowerMag;
	}
    
    public boolean isGyroOnTheFlyCalEnabled(){
		return mEnableOntheFlyGyroOVCal;
	}

	public boolean is3DOrientatioEnabled(){
		return mOrientationEnabled;
	}
    
	/**Only used for LogAndStream
	 * @return
	 */
	public boolean isSensing(){
		return mIsSensing;
	}
	
	public boolean isDocked(){
		return mIsDocked;
	}
	
	public boolean isLowPowerAccelEnabled() {
		// TODO Auto-generated method stub
		return mLowPowerAccelWR;
	}

	public boolean isLowPowerGyroEnabled() {
		// TODO Auto-generated method stub
		return mLowPowerGyro;
	}
	
	public boolean isUsingDefaultLNAccelParam(){
		return mDefaultCalibrationParametersAccel;
	}
	
	public boolean isUsingDefaultAccelParam(){
		return mDefaultCalibrationParametersAccel;
	}
	
	public boolean isUsingDefaultWRAccelParam(){
		return mDefaultCalibrationParametersDigitalAccel; 
	}

	public boolean isUsingDefaultGyroParam(){
		return mDefaultCalibrationParametersGyro;
	}
	
	public boolean isUsingDefaultMagParam(){
		return mDefaultCalibrationParametersMag;
	}
	
	public boolean isUsingDefaultECGParam(){
		return mDefaultCalibrationParametersECG;
	}
	
	public boolean isUsingDefaultEMGParam(){
		return mDefaultCalibrationParametersEMG;
	}
	

	
	
	/**
	 * Checks if 16 bit ECG configuration is set on the Shimmer device. Do not use this command right after setting an EXG setting, as due to the execution model, the old settings might be returned, if this command is executed before an ack is received.
	 * @return true if 16 bit ECG is set
	 */
	@Override
	public boolean isEXGUsingECG16Configuration(){
		while(!getListofInstructions().isEmpty());
		return super.isEXGUsingECG16Configuration();
	}
	
	/**
	 * Checks if 24 bit ECG configuration is set on the Shimmer device. Do not use this command right after setting an EXG setting, as due to the execution model, the old settings might be returned, if this command is executed before an ack is received.
	 * @return true if 24 bit ECG is set
	 */
	@Override
	public boolean isEXGUsingECG24Configuration(){
		while(!getListofInstructions().isEmpty());
		return super.isEXGUsingECG24Configuration();
	}
	
	/**
	 * Checks if 16 bit EMG configuration is set on the Shimmer device.  Do not use this command right after setting an EXG setting, as due to the execution model, the old settings might be returned, if this command is executed before an ack is received. 
	 * @return true if 16 bit EMG is set
	 */
	@Override
	public boolean isEXGUsingEMG16Configuration(){
		while(!getListofInstructions().isEmpty());
		return super.isEXGUsingEMG16Configuration();
	}
	
	/**
	 * Checks if 24 bit EMG configuration is set on the Shimmer device.  Do not use this command right after setting an EXG setting, as due to the execution model, the old settings might be returned, if this command is executed before an ack is received.
	 * @return true if 24 bit EMG is set
	 */
	@Override
	public boolean isEXGUsingEMG24Configuration(){
		while(!getListofInstructions().isEmpty());
		return super.isEXGUsingEMG24Configuration();
	}
	
	/**
	 * Checks if 16 bit test signal configuration is set on the Shimmer device. Do not use this command right after setting an EXG setting, as due to the execution model, the old settings might be returned, if this command is executed before an ack is received.
	 * @return true if 24 bit test signal is set
	 */
	@Override
	public boolean isEXGUsingTestSignal16Configuration(){
		while(!getListofInstructions().isEmpty());
		return super.isEXGUsingTestSignal16Configuration();
	}
	
	/**
	 * Checks if 24 bit test signal configuration is set on the Shimmer device.
	 * @return true if 24 bit test signal is set
	 */
	@Override
	public boolean isEXGUsingTestSignal24Configuration(){
		while(!getListofInstructions().isEmpty());
		return super.isEXGUsingTestSignal24Configuration();
	}
	
    //endregion
    

	//region --------- ENABLE/DISABLE FUNCTIONS --------- 

	/**** ENABLE FUNCTIONS *****/
	
	//TODO: use set3DOrientation(enable) in ShimmerObject instead -> check that the "enable the sensors if they have not been enabled" comment is correct
	/**
	 * This enables the calculation of 3D orientation through the use of the gradient descent algorithm, note that the user will have to ensure that mEnableCalibration has been set to true (see enableCalibration), and that the accel, gyro and mag has been enabled
	 * @param enable
	 */
	public void enable3DOrientation(boolean enable){
		//enable the sensors if they have not been enabled 
		mOrientationEnabled = enable;
	}

	/**
	 * This enables the low power accel option. When not enabled the sampling rate of the accel is set to the closest value to the actual sampling rate that it can achieve. In low power mode it defaults to 10Hz. Also and additional low power mode is used for the LSM303DLHC. This command will only supports the following Accel range +4g, +8g , +16g 
	 * @param enable
	 */
	public void enableLowPowerAccel(boolean enable){
		enableHighResolutionMode(!enable);
		writeAccelSamplingRate(mLSM303DigitalAccelRate);
	}

	private void enableHighResolutionMode(boolean enable) {
		while(getInstructionStatus()==false) {};
		
		if (mFirmwareVersionCode==1 && mFirmwareVersionInternal==0) {

		} else if (mHardwareVersion == HW_ID.SHIMMER_3) {
			setLowPowerAccelWR(!enable);
//			setHighResAccelWR(enable);
			if (enable) {
				// High-Res = On, Low-power = Off
				getListofInstructions().add(new byte[]{SET_LSM303DLHC_ACCEL_HRMODE_COMMAND, (byte)0x01});
				getListofInstructions().add(new byte[]{SET_LSM303DLHC_ACCEL_LPMODE_COMMAND, (byte)0x00});
			} else {
				// High-Res = Off, Low-power = On
				getListofInstructions().add(new byte[]{SET_LSM303DLHC_ACCEL_HRMODE_COMMAND, (byte)0x00});
				getListofInstructions().add(new byte[]{SET_LSM303DLHC_ACCEL_LPMODE_COMMAND, (byte)0x01});
			}
		}
	}
	
//	private void enableLowResolutionMode(boolean enable){
//		while(getInstructionStatus()==false) {};
//		if (mFirmwareVersionCode==1 && mFirmwareVersionRelease==0) {
//
//		} else if (mShimmerVersion == HW_ID.SHIMMER_3) {
//			if (enable) {
//				mListofInstructions.add(new byte[]{SET_LSM303DLHC_ACCEL_LPMODE_COMMAND, (byte)0x01});
//				mListofInstructions.add(new byte[]{SET_LSM303DLHC_ACCEL_HRMODE_COMMAND, (byte)0x00});
//			} else {
//				mListofInstructions.add(new byte[]{SET_LSM303DLHC_ACCEL_HRMODE_COMMAND, (byte)0x01});
//				mListofInstructions.add(new byte[]{SET_LSM303DLHC_ACCEL_LPMODE_COMMAND, (byte)0x00});
//			}
//		}
//	}
	
	/**
	 * This enables the low power accel option. When not enabled the sampling rate of the accel is set to the closest value to the actual sampling rate that it can achieve. In low power mode it defaults to 10Hz. Also and additional low power mode is used for the LSM303DLHC. This command will only supports the following Accel range +4g, +8g , +16g 
	 * @param enable
	 */
	public void enableLowPowerGyro(boolean enable){
		setLowPowerGyro(enable);
		writeGyroSamplingRate(mMPU9150GyroAccelRate);
	}
	
	/**
	 * This enables the low power mag option. When not enabled the sampling rate of the mag is set to the closest value to the actual sampling rate that it can achieve. In low power mode it defaults to 10Hz
	 * @param enable
	 */
	public void enableLowPowerMag(boolean enable){
		setLowPowerMag(enable);
		writeMagSamplingRate(mLSM303MagRate);
	}
	

	/**
	 *This can only be used for Shimmer3 devices (EXG) 
	 *When a enable configuration is loaded, the advanced ExG configuration is removed, so it needs to be set again
	 */
	 public void enableDefaultECGConfiguration() {
		 if (mHardwareVersion==3){
			setDefaultECGConfiguration();
			writeEXGConfiguration(mEXG1RegisterArray,1);
			writeEXGConfiguration(mEXG2RegisterArray,2);
		 }
	}

	/**
	 * This can only be used for Shimmer3 devices (EXG)
	 * When a enable configuration is loaded, the advanced ExG configuration is removed, so it needs to be set again
	 */
	public void enableDefaultEMGConfiguration(){
		if (mHardwareVersion==3){
			setDefaultEMGConfiguration();
			writeEXGConfiguration(mEXG1RegisterArray,1);
			writeEXGConfiguration(mEXG2RegisterArray,2);
		}
	}

	/**
	 * This can only be used for Shimmer3 devices (EXG). Enables the test signal (square wave) of both EXG chips, to use, both EXG1 and EXG2 have to be enabled
	 */
	public void enableEXGTestSignal(){
		if (mHardwareVersion==3){
			setEXGTestSignal();
			writeEXGConfiguration(mEXG1RegisterArray,1);
			writeEXGConfiguration(mEXG2RegisterArray,2);
		}
	}
	
	/**** DISABLE FUNCTIONS *****/
	
	private long disableBit(long number,long disablebitvalue){
		if ((number&disablebitvalue)>0){
			number = number ^ disablebitvalue;
		}
		return number;
	}
	
	//endregion
	
	
	//region --------- MISCELLANEOUS FUNCTIONS ---------
	
	public void reconnect(){
        if (mIsConnected && !mIsStreaming){
        	String msgReconnect = "Reconnecting the Shimmer...";
			sendStatusMSGtoUI(msgReconnect);
            stop();
            try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            connect(mMyBluetoothAddress,"default");
            mUniqueID = this.mMacIdFromUart; 
        }
    }

	
	/**
	 * An inquiry is used to request for the current configuration parameters from the Shimmer device (e.g. Accelerometer settings, Configuration Byte, Sampling Rate, Number of Enabled Sensors and Sensors which have been enabled). 
	 */
	public void inquiry() {
		getListofInstructions().add(new byte[]{INQUIRY_COMMAND});
	}
	
	/**
	 * @param enabledSensors This takes in the current list of enabled sensors 
	 * @param sensorToCheck This takes in a single sensor which is to be enabled
	 * @return enabledSensors This returns the new set of enabled sensors, where any sensors which conflicts with sensorToCheck is disabled on the bitmap, so sensorToCheck can be accomodated (e.g. for Shimmer2 using ECG will disable EMG,GSR,..basically any daughter board)
	 *  
	 */
	public long sensorConflictCheckandCorrection(long enabledSensors,long sensorToCheck){

		if (mHardwareVersion==HW_ID.SHIMMER_2R || mHardwareVersion==HW_ID.SHIMMER_2){
			if ((sensorToCheck & SENSOR_GYRO) >0 || (sensorToCheck & SENSOR_MAG) >0){
				enabledSensors = disableBit(enabledSensors,SENSOR_ECG);
				enabledSensors = disableBit(enabledSensors,SENSOR_EMG);
				enabledSensors = disableBit(enabledSensors,SENSOR_GSR);
				enabledSensors = disableBit(enabledSensors,SENSOR_BRIDGE_AMP);
			} else if ((sensorToCheck & SENSOR_BRIDGE_AMP) >0){
				enabledSensors = disableBit(enabledSensors,SENSOR_ECG);
				enabledSensors = disableBit(enabledSensors,SENSOR_EMG);
				enabledSensors = disableBit(enabledSensors,SENSOR_GSR);
				enabledSensors = disableBit(enabledSensors,SENSOR_GYRO);
				enabledSensors = disableBit(enabledSensors,SENSOR_MAG);
			} else if ((sensorToCheck & SENSOR_GSR) >0){
				enabledSensors = disableBit(enabledSensors,SENSOR_ECG);
				enabledSensors = disableBit(enabledSensors,SENSOR_EMG);
				enabledSensors = disableBit(enabledSensors,SENSOR_BRIDGE_AMP);
				enabledSensors = disableBit(enabledSensors,SENSOR_GYRO);
				enabledSensors = disableBit(enabledSensors,SENSOR_MAG);
			} else if ((sensorToCheck & SENSOR_ECG) >0){
				enabledSensors = disableBit(enabledSensors,SENSOR_GSR);
				enabledSensors = disableBit(enabledSensors,SENSOR_EMG);
				enabledSensors = disableBit(enabledSensors,SENSOR_BRIDGE_AMP);
				enabledSensors = disableBit(enabledSensors,SENSOR_GYRO);
				enabledSensors = disableBit(enabledSensors,SENSOR_MAG);
			} else if ((sensorToCheck & SENSOR_EMG) >0){
				enabledSensors = disableBit(enabledSensors,SENSOR_GSR);
				enabledSensors = disableBit(enabledSensors,SENSOR_ECG);
				enabledSensors = disableBit(enabledSensors,SENSOR_BRIDGE_AMP);
				enabledSensors = disableBit(enabledSensors,SENSOR_GYRO);
				enabledSensors = disableBit(enabledSensors,SENSOR_MAG);
			} else if ((sensorToCheck & SENSOR_HEART) >0){
				enabledSensors = disableBit(enabledSensors,SENSOR_EXP_BOARD_A0);
				enabledSensors = disableBit(enabledSensors,SENSOR_EXP_BOARD_A7);
			} else if ((sensorToCheck & SENSOR_EXP_BOARD_A0) >0 || (sensorToCheck & SENSOR_EXP_BOARD_A7) >0){
				enabledSensors = disableBit(enabledSensors,SENSOR_HEART);
				enabledSensors = disableBit(enabledSensors,SENSOR_BATT);
			} else if ((sensorToCheck & SENSOR_BATT) >0){
				enabledSensors = disableBit(enabledSensors,SENSOR_EXP_BOARD_A0);
				enabledSensors = disableBit(enabledSensors,SENSOR_EXP_BOARD_A7);
			}
		}

		else if(mHardwareVersion==HW_ID.SHIMMER_3){
			
			if((sensorToCheck & SENSOR_GSR) >0){
				enabledSensors = disableBit(enabledSensors,SENSOR_INT_ADC_A1);
				enabledSensors = disableBit(enabledSensors,SENSOR_INT_ADC_A14);
				enabledSensors = disableBit(enabledSensors,SENSOR_EXG1_16BIT);
				enabledSensors = disableBit(enabledSensors,SENSOR_EXG2_16BIT);
				enabledSensors = disableBit(enabledSensors,SENSOR_EXG1_24BIT);
				enabledSensors = disableBit(enabledSensors,SENSOR_EXG2_24BIT);
				enabledSensors = disableBit(enabledSensors,SENSOR_BRIDGE_AMP);
			} 
			else if((sensorToCheck & SENSOR_EXG1_16BIT) >0 || (sensorToCheck & SENSOR_EXG2_16BIT) >0){
				enabledSensors = disableBit(enabledSensors,SENSOR_INT_ADC_A1);
				enabledSensors = disableBit(enabledSensors,SENSOR_INT_ADC_A12);
				enabledSensors = disableBit(enabledSensors,SENSOR_INT_ADC_A13);
				enabledSensors = disableBit(enabledSensors,SENSOR_INT_ADC_A14);
				enabledSensors = disableBit(enabledSensors,SENSOR_GSR);
				enabledSensors = disableBit(enabledSensors,SENSOR_EXG1_24BIT);
				enabledSensors = disableBit(enabledSensors,SENSOR_EXG2_24BIT);
				enabledSensors = disableBit(enabledSensors,SENSOR_BRIDGE_AMP);
			}
			else if((sensorToCheck & SENSOR_EXG1_24BIT) >0 || (sensorToCheck & SENSOR_EXG2_24BIT) >0){
				enabledSensors = disableBit(enabledSensors,SENSOR_INT_ADC_A1);
				enabledSensors = disableBit(enabledSensors,SENSOR_INT_ADC_A12);
				enabledSensors = disableBit(enabledSensors,SENSOR_INT_ADC_A13);
				enabledSensors = disableBit(enabledSensors,SENSOR_INT_ADC_A14);
				enabledSensors = disableBit(enabledSensors,SENSOR_GSR);
				enabledSensors = disableBit(enabledSensors,SENSOR_EXG1_16BIT);
				enabledSensors = disableBit(enabledSensors,SENSOR_EXG2_16BIT);
				enabledSensors = disableBit(enabledSensors,SENSOR_BRIDGE_AMP);
			}
			else if((sensorToCheck & SENSOR_BRIDGE_AMP) >0){
				enabledSensors = disableBit(enabledSensors,SENSOR_INT_ADC_A12);
				enabledSensors = disableBit(enabledSensors,SENSOR_INT_ADC_A13);
				enabledSensors = disableBit(enabledSensors,SENSOR_INT_ADC_A14);
				enabledSensors = disableBit(enabledSensors,SENSOR_GSR);
				enabledSensors = disableBit(enabledSensors,SENSOR_EXG1_16BIT);
				enabledSensors = disableBit(enabledSensors,SENSOR_EXG2_16BIT);
				enabledSensors = disableBit(enabledSensors,SENSOR_EXG1_24BIT);
				enabledSensors = disableBit(enabledSensors,SENSOR_EXG2_24BIT);
			}
			else if ((sensorToCheck & SENSOR_INT_ADC_A14) >0){
				enabledSensors = disableBit(enabledSensors,SENSOR_GSR);
				enabledSensors = disableBit(enabledSensors,SENSOR_EXG1_16BIT);
				enabledSensors = disableBit(enabledSensors,SENSOR_EXG2_16BIT);
				enabledSensors = disableBit(enabledSensors,SENSOR_EXG1_24BIT);
				enabledSensors = disableBit(enabledSensors,SENSOR_EXG2_24BIT);
			}
			else if ((sensorToCheck & SENSOR_INT_ADC_A12) >0){
				enabledSensors = disableBit(enabledSensors,SENSOR_EXG1_16BIT);
				enabledSensors = disableBit(enabledSensors,SENSOR_EXG2_16BIT);
				enabledSensors = disableBit(enabledSensors,SENSOR_EXG1_24BIT);
				enabledSensors = disableBit(enabledSensors,SENSOR_EXG2_24BIT);
				enabledSensors = disableBit(enabledSensors,SENSOR_BRIDGE_AMP);
			}
			else if ((sensorToCheck & SENSOR_INT_ADC_A13) >0){
				enabledSensors = disableBit(enabledSensors,SENSOR_EXG1_16BIT);
				enabledSensors = disableBit(enabledSensors,SENSOR_EXG2_16BIT);
				enabledSensors = disableBit(enabledSensors,SENSOR_EXG1_24BIT);
				enabledSensors = disableBit(enabledSensors,SENSOR_EXG2_24BIT);
				enabledSensors = disableBit(enabledSensors,SENSOR_BRIDGE_AMP);
			}
			else if ((sensorToCheck & SENSOR_INT_ADC_A14) >0){
				enabledSensors = disableBit(enabledSensors,SENSOR_GSR);
				enabledSensors = disableBit(enabledSensors,SENSOR_EXG1_16BIT);
				enabledSensors = disableBit(enabledSensors,SENSOR_EXG2_16BIT);
				enabledSensors = disableBit(enabledSensors,SENSOR_EXG1_24BIT);
				enabledSensors = disableBit(enabledSensors,SENSOR_EXG2_24BIT);
				enabledSensors = disableBit(enabledSensors,SENSOR_BRIDGE_AMP);
			}
			
		}
		enabledSensors = enabledSensors ^ sensorToCheck;
		return enabledSensors;
	}
	
	public boolean sensorConflictCheck(long enabledSensors){
		boolean pass=true;
		if (mHardwareVersion != HW_ID.SHIMMER_3){
			if (((enabledSensors & 0xFF)& SENSOR_GYRO) > 0){
				if (((enabledSensors & 0xFF)& SENSOR_EMG) > 0){
					pass=false;
				} else if (((enabledSensors & 0xFF)& SENSOR_ECG) > 0){
					pass=false;
				} else if (((enabledSensors & 0xFF)& SENSOR_GSR) > 0){
					pass=false;
				} else if (((enabledSensors & 0xFF00) & SENSOR_BRIDGE_AMP) > 0){
					pass=false;
				}
			}

			if (((enabledSensors & 0xFF)& SENSOR_MAG) > 0){
				if (((enabledSensors & 0xFF)& SENSOR_EMG) > 0){
					pass=false;
				} else if (((enabledSensors & 0xFF)& SENSOR_ECG) > 0){
					pass=false;
				} else if (((enabledSensors & 0xFF)& SENSOR_GSR) > 0){
					pass=false;
				} else if (((enabledSensors & 0xFF00) & SENSOR_BRIDGE_AMP) > 0){
					pass=false;
				}
			}

			if (((enabledSensors & 0xFF)& SENSOR_EMG) > 0){
				if (((enabledSensors & 0xFF)& SENSOR_GYRO) > 0){
					pass=false;
				} else if (((enabledSensors & 0xFF)& SENSOR_MAG) > 0){
					pass=false;
				} else if (((enabledSensors & 0xFF)& SENSOR_ECG) > 0){
					pass=false;
				} else if (((enabledSensors & 0xFF)& SENSOR_GSR) > 0){
					pass=false;
				} else if (((enabledSensors & 0xFF00) & SENSOR_BRIDGE_AMP) > 0){
					pass=false;
				}
			}

			if (((enabledSensors & 0xFF)& SENSOR_ECG) > 0){
				if (((enabledSensors & 0xFF)& SENSOR_GYRO) > 0){
					pass=false;
				} else if (((enabledSensors & 0xFF)& SENSOR_MAG) > 0){
					pass=false;
				} else if (((enabledSensors & 0xFF)& SENSOR_EMG) > 0){
					pass=false;
				} else if (((enabledSensors & 0xFF)& SENSOR_GSR) > 0){
					pass=false;
				}else if (((enabledSensors & 0xFF00) & SENSOR_BRIDGE_AMP) > 0){
					pass=false;
				}
			}

			if (((enabledSensors & 0xFF)& SENSOR_GSR) > 0){
				if (((enabledSensors & 0xFF)& SENSOR_GYRO) > 0){
					pass=false;
				} else if (((enabledSensors & 0xFF)& SENSOR_MAG) > 0){
					pass=false;
				} else if (((enabledSensors & 0xFF)& SENSOR_EMG) > 0){
					pass=false;
				} else if (((enabledSensors & 0xFF)& SENSOR_ECG) > 0){
					pass=false;
				}else if (((enabledSensors & 0xFF00) & SENSOR_BRIDGE_AMP) > 0){
					pass=false;
				}
			}

			if (((enabledSensors & 0xFF00) & SENSOR_BRIDGE_AMP) > 0){
				if (((enabledSensors & 0xFF)& SENSOR_GYRO) > 0){
					pass=false;
				} else if (((enabledSensors & 0xFF)& SENSOR_MAG) > 0){
					pass=false;
				} else if (((enabledSensors & 0xFF)& SENSOR_EMG) > 0){
					pass=false;
				} else if (((enabledSensors & 0xFF)& SENSOR_ECG) > 0){
					pass=false;
				} else if (((enabledSensors & 0xFF)& SENSOR_GSR) > 0){
					pass=false;
				} else if (get5VReg()==1){ // if the 5volt reg is set 
					pass=false;
				}
			}

			if (((enabledSensors & 0xFF) & SENSOR_EXP_BOARD_A0) > 0) {
				if (((enabledSensors & 0xFFFFF) & SENSOR_BATT) > 0) {
					pass=false;
				} else if (getPMux()==1){
					
					writePMux(0);
				}
			}

			if (((enabledSensors & 0xFF) & SENSOR_EXP_BOARD_A7) > 0) {
				if (((enabledSensors & 0xFFFFF) & SENSOR_BATT) > 0) {
					pass=false;
				}else if (getPMux()==1){
					writePMux(0);
				}
			}

			if (((enabledSensors & 0xFFFFF) & SENSOR_BATT) > 0) {
				if (((enabledSensors & 0xFF) & SENSOR_EXP_BOARD_A7) > 0){
					pass=false;
				} 
				if (((enabledSensors & 0xFF) & SENSOR_EXP_BOARD_A0) > 0){
					pass=false;
				}
				if (((enabledSensors & 0xFFFFF) & SENSOR_BATT) > 0){
					if (getPMux()==0){
						
						writePMux(1);
					}
				}
			}
			if (!pass){
				
			}
		}
		
		else{
			
			if(((enabledSensors & 0xFF0000) & SENSOR_EXG1_16BIT) > 0 || ((enabledSensors & 0xFF0000) & SENSOR_EXG2_16BIT) > 0){
				
				if (((enabledSensors & 0xFF00) & SENSOR_INT_ADC_A1) > 0){
					pass=false; 
				} else if (((enabledSensors & 0xFF00) & SENSOR_INT_ADC_A12) > 0){
					pass=false;
				} else if (((enabledSensors & 0xFF00) & SENSOR_INT_ADC_A13) > 0){
					pass=false;
				} else if (((enabledSensors & 0xFF0000) & SENSOR_INT_ADC_A14) > 0){
					pass=false;
				} else if(((enabledSensors & 0xFF) & SENSOR_GSR) > 0){
					pass=false;
				} else if (((enabledSensors & 0xFF) & SENSOR_EXG1_24BIT) > 0){
					pass=false;
				} else if (((enabledSensors & 0xFF) & SENSOR_EXG2_24BIT) > 0){
					pass=false;
				} else if (((enabledSensors & 0xFF00) & SENSOR_BRIDGE_AMP) > 0){
					pass=false;
				}
			}
			
			if(((enabledSensors & 0xFF) & SENSOR_EXG1_24BIT) > 0 || ((enabledSensors & 0xFF) & SENSOR_EXG2_24BIT) > 0){
				
				if (((enabledSensors & 0xFF00) & SENSOR_INT_ADC_A1) > 0){
					pass=false; 
				} else if (((enabledSensors & 0xFF00) & SENSOR_INT_ADC_A12) > 0){
					pass=false;
				} else if (((enabledSensors & 0xFF00) & SENSOR_INT_ADC_A13) > 0){
					pass=false;
				} else if (((enabledSensors & 0xFF0000) & SENSOR_INT_ADC_A14) > 0){
					pass=false;
				} else if(((enabledSensors & 0xFF) & SENSOR_GSR) > 0){
					pass=false;
				} else if (((enabledSensors & 0xFF) & SENSOR_EXG1_16BIT) > 0){
					pass=false;
				} else if (((enabledSensors & 0xFF) & SENSOR_EXG2_16BIT) > 0){
					pass=false;
				} else if (((enabledSensors & 0xFF00) & SENSOR_BRIDGE_AMP) > 0){
					pass=false;
				}
			}

			
			if(((enabledSensors & 0xFF) & SENSOR_GSR) > 0){
				
				if (((enabledSensors & 0xFF0000) & SENSOR_INT_ADC_A1) > 0){
					pass=false;
				} else if (((enabledSensors & 0xFF0000) & SENSOR_INT_ADC_A14) > 0){
					pass=false;
				} else if (((enabledSensors & 0xFF0000) & SENSOR_EXG1_16BIT) > 0){
					pass=false;
				} else if (((enabledSensors & 0xFF0000) & SENSOR_EXG2_16BIT) > 0){
					pass=false;
				} else if (((enabledSensors & 0xFF) & SENSOR_EXG1_24BIT) > 0){
					pass=false;
				} else if (((enabledSensors & 0xFF) & SENSOR_EXG2_24BIT) > 0){
					pass=false;
				} else if (((enabledSensors & 0xFF00) & SENSOR_BRIDGE_AMP) > 0){
					pass=false;
				}
			}
			
			if (((enabledSensors & 0xFF00) & SENSOR_BRIDGE_AMP) > 0){
				  
				if (((enabledSensors & 0xFF00) & SENSOR_INT_ADC_A12) > 0){
					pass=false;
				} else if (((enabledSensors & 0xFF00) & SENSOR_INT_ADC_A13) > 0){
					pass=false;
				} else if (((enabledSensors & 0xFF0000) & SENSOR_INT_ADC_A14) > 0){
					pass=false;
				} else if(((enabledSensors & 0xFF) & SENSOR_GSR) > 0){
					pass=false;		
				} else if (((enabledSensors & 0xFF0000) & SENSOR_EXG1_16BIT) > 0){
					pass=false;
				} else if (((enabledSensors & 0xFF0000) & SENSOR_EXG2_16BIT) > 0){
					pass=false;
				} else if (((enabledSensors & 0xFF) & SENSOR_EXG1_24BIT) > 0){
					pass=false;
				} else if (((enabledSensors & 0xFF) & SENSOR_EXG2_24BIT) > 0){
					pass=false;
				}
			}
			
			if (((enabledSensors & 0xFF00) & SENSOR_INT_ADC_A1) > 0){
				  
				 if(((enabledSensors & 0xFF) & SENSOR_GSR) > 0){
					pass=false;
				 } else if (((enabledSensors & 0xFF0000) & SENSOR_EXG1_16BIT) > 0){
					pass=false;
				 } else if (((enabledSensors & 0xFF0000) & SENSOR_EXG2_16BIT) > 0){
					pass=false;
				 } else if (((enabledSensors & 0xFF) & SENSOR_EXG1_24BIT) > 0){
					pass=false;
				 } else if (((enabledSensors & 0xFF) & SENSOR_EXG2_24BIT) > 0){
					pass=false;
				 }
			}
			
			if (((enabledSensors & 0xFF00) & SENSOR_INT_ADC_A12) > 0){
				  
				if (((enabledSensors & 0xFF0000) & SENSOR_EXG1_16BIT) > 0){
					pass=false;
				} else if (((enabledSensors & 0xFF0000) & SENSOR_EXG2_16BIT) > 0){
					pass=false;
				} else if (((enabledSensors & 0xFF) & SENSOR_EXG1_24BIT) > 0){
					pass=false;
				} else if (((enabledSensors & 0xFF) & SENSOR_EXG2_24BIT) > 0){
					pass=false;
				} else if (((enabledSensors & 0xFF00) & SENSOR_BRIDGE_AMP) > 0){
					pass=false;
				}
			}
			
			if (((enabledSensors & 0xFF00) & SENSOR_INT_ADC_A13) > 0){
				  
				if (((enabledSensors & 0xFF0000) & SENSOR_EXG1_16BIT) > 0){
					pass=false;
				} else if (((enabledSensors & 0xFF0000) & SENSOR_EXG2_16BIT) > 0){
					pass=false;
				} else if (((enabledSensors & 0xFF) & SENSOR_EXG1_24BIT) > 0){
					pass=false;
				} else if (((enabledSensors & 0xFF) & SENSOR_EXG2_24BIT) > 0){
					pass=false;
				} else if (((enabledSensors & 0xFF00) & SENSOR_BRIDGE_AMP) > 0){
					pass=false;
				}
			}
			
			if (((enabledSensors & 0xFF0000) & SENSOR_INT_ADC_A14) > 0){
				  
				 if(((enabledSensors & 0xFF) & SENSOR_GSR) > 0){
					pass=false;
				 } else if (((enabledSensors & 0xFF0000) & SENSOR_EXG1_16BIT) > 0){
					pass=false;
				 } else if (((enabledSensors & 0xFF0000) & SENSOR_EXG2_16BIT) > 0){
					pass=false;
				 } else if (((enabledSensors & 0xFF) & SENSOR_EXG1_24BIT) > 0){
					pass=false;
				 } else if (((enabledSensors & 0xFF) & SENSOR_EXG2_24BIT) > 0){
					pass=false;
				 } else if (((enabledSensors & 0xFF00) & SENSOR_BRIDGE_AMP) > 0){
					pass=false;
				 }
			}
		}
		
		return pass;
	}
	
	/**
	 * @param enabledSensors this bitmap is only applicable for the instrument driver and does not correspond with the values in the firmware
	 * @return enabledSensorsFirmware returns the bitmap for the firmware
	 * The reason for this is hardware and firmware change may eventually need a different sensor bitmap, to keep the ID forward compatible, this function is used. Therefor the ID can have its own seperate sensor bitmap if needed
	 */
	private long generateSensorBitmapForHardwareControl(long enabledSensors){
		long hardwareSensorBitmap=0;

		//check if the batt volt is enabled (this is only applicable for HW_ID.SHIMMER_2R
		if (mHardwareVersion == HW_ID.SHIMMER_2R || mHardwareVersion == HW_ID.SHIMMER_2){
			if (((enabledSensors & 0xFFFFF) & SENSOR_BATT) > 0 ){
				enabledSensors = enabledSensors & 0xFFFF;
				enabledSensors = enabledSensors|SENSOR_EXP_BOARD_A0|SENSOR_EXP_BOARD_A7;
			}
			hardwareSensorBitmap  = enabledSensors;
		} else if (mHardwareVersion == HW_ID.SHIMMER_3){
			if (((enabledSensors & 0xFF)& SENSOR_ACCEL) > 0){
				hardwareSensorBitmap = hardwareSensorBitmap|Configuration.Shimmer3.SensorBitmap.SENSOR_A_ACCEL;
			}
			if ((enabledSensors & SENSOR_DACCEL) > 0){
				hardwareSensorBitmap = hardwareSensorBitmap|Configuration.Shimmer3.SensorBitmap.SENSOR_D_ACCEL;
			}
			if (((enabledSensors & 0xFF)& SENSOR_EXG1_24BIT) > 0){
				hardwareSensorBitmap = hardwareSensorBitmap|Configuration.Shimmer3.SensorBitmap.SENSOR_EXG1_24BIT;
			}
			if (((enabledSensors & 0xFF)& SENSOR_EXG2_24BIT) > 0){
				hardwareSensorBitmap = hardwareSensorBitmap|Configuration.Shimmer3.SensorBitmap.SENSOR_EXG2_24BIT;
			}

			if ((enabledSensors& SENSOR_EXG1_16BIT) > 0){
				hardwareSensorBitmap = hardwareSensorBitmap|Configuration.Shimmer3.SensorBitmap.SENSOR_EXG1_16BIT;
			}
			if ((enabledSensors & SENSOR_EXG2_16BIT) > 0){
				hardwareSensorBitmap = hardwareSensorBitmap|Configuration.Shimmer3.SensorBitmap.SENSOR_EXG2_16BIT;
			}
			if (((enabledSensors & 0xFF)& SENSOR_GYRO) > 0){
				hardwareSensorBitmap = hardwareSensorBitmap|Configuration.Shimmer3.SensorBitmap.SENSOR_GYRO;
			}
			if (((enabledSensors & 0xFF)& SENSOR_MAG) > 0){
				hardwareSensorBitmap = hardwareSensorBitmap|Configuration.Shimmer3.SensorBitmap.SENSOR_MAG;
			}
			if ((enabledSensors & SENSOR_BATT) > 0){
				hardwareSensorBitmap = hardwareSensorBitmap|Configuration.Shimmer3.SensorBitmap.SENSOR_VBATT;
			}
			if ((enabledSensors & SENSOR_EXT_ADC_A7) > 0){
				hardwareSensorBitmap = hardwareSensorBitmap|Configuration.Shimmer3.SensorBitmap.SENSOR_EXT_A7;
			}
			if ((enabledSensors & SENSOR_EXT_ADC_A6) > 0){
				hardwareSensorBitmap = hardwareSensorBitmap|Configuration.Shimmer3.SensorBitmap.SENSOR_EXT_A6;
			}
			if ((enabledSensors & SENSOR_EXT_ADC_A15) > 0){
				hardwareSensorBitmap = hardwareSensorBitmap|Configuration.Shimmer3.SensorBitmap.SENSOR_EXT_A15;
			}
			if ((enabledSensors & SENSOR_INT_ADC_A1) > 0){
				hardwareSensorBitmap = hardwareSensorBitmap|Configuration.Shimmer3.SensorBitmap.SENSOR_INT_A1;
			}
			if ((enabledSensors & SENSOR_INT_ADC_A12) > 0){
				hardwareSensorBitmap = hardwareSensorBitmap|Configuration.Shimmer3.SensorBitmap.SENSOR_INT_A12;
			}
			if ((enabledSensors & SENSOR_INT_ADC_A13) > 0){
				hardwareSensorBitmap = hardwareSensorBitmap|Configuration.Shimmer3.SensorBitmap.SENSOR_INT_A13;
			}
			if ((enabledSensors & SENSOR_INT_ADC_A14) > 0){
				hardwareSensorBitmap = hardwareSensorBitmap|Configuration.Shimmer3.SensorBitmap.SENSOR_INT_A14;
			}
			if  ((enabledSensors & SENSOR_BMP180) > 0){
				hardwareSensorBitmap = hardwareSensorBitmap|Configuration.Shimmer3.SensorBitmap.SENSOR_BMP180;
			} 
			if ((enabledSensors & SENSOR_GSR) > 0){
				hardwareSensorBitmap = hardwareSensorBitmap|Configuration.Shimmer3.SensorBitmap.SENSOR_GSR;
			}
			if ((enabledSensors & SENSOR_BRIDGE_AMP) > 0){
				hardwareSensorBitmap = hardwareSensorBitmap|Configuration.Shimmer3.SensorBitmap.SENSOR_BRIDGE_AMP;
			} 
		} else { 
			hardwareSensorBitmap  = enabledSensors;
		}

		return hardwareSensorBitmap;
	}

	public void toggleLed() {
		getListofInstructions().add(new byte[]{TOGGLE_LED_COMMAND});
	}
	
	@Override
	protected void checkBattery(){
		if (mIsStreaming ){
			if(mHardwareVersion == HW_ID.SHIMMER_3 && mFirmwareVersionCode==FW_ID.SHIMMER3.LOGANDSTREAM){
				if (!mWaitForAck) {	
					if (mVSenseBattMA.getMean()<mLowBattLimit*1000*0.8) {
						if (mCurrentLEDStatus!=2) {
							writeLEDCommand(2);
						}
					} else if (mVSenseBattMA.getMean()<mLowBattLimit*1000) {
						if (mCurrentLEDStatus!=1) {
							writeLEDCommand(1);
						}
					} else if(mVSenseBattMA.getMean()>mLowBattLimit*1000+100) { //+100 is to make sure the limits are different to prevent excessive switching when the batt value is at the threshold
						if (mCurrentLEDStatus!=0) {
							writeLEDCommand(0);
						}
					}

				}
			}
			if(mHardwareVersion == HW_ID.SHIMMER_2R){
				if (!mWaitForAck) {	
					if (mVSenseBattMA.getMean()<mLowBattLimit*1000) {
						if (mCurrentLEDStatus!=1) {
							writeLEDCommand(1);
						}
					} else if(mVSenseBattMA.getMean()>mLowBattLimit*1000+100) { //+100 is to make sure the limits are different to prevent excessive switching when the batt value is at the threshold
						if (mCurrentLEDStatus!=0) {
							writeLEDCommand(0);
						}
					}

				}
			}
			

		
		}
	
	}
	
	public void enableCheckifAlive(boolean set){
		mCheckIfConnectionisAlive = set;
	}
	
	public void resetCalibratedTimeStamp(){
		mLastReceivedTimeStamp = 0;
		mLastReceivedCalibratedTimeStamp = -1;
		mFirstTimeCalTime = true;
		mCurrentTimeStampCycle = 0;
	}
	
	//endregion

	public Object setValueUsingGuiComponent(String componentName, Object valueToSet) {
		return super.setValueUsingGuiComponent(componentName, valueToSet);
	}


	public void setSendProgressReport(boolean send){
		mSendProgressReport = send;
	}
	
	/**
	 * @return the mListofInstructions
	 */
	public List<byte []> getListofInstructions() {
		return mListofInstructions;
	}
	
	/**
	 * @return the mInstructionStackLock
	 */
	public boolean ismInstructionStackLock() {
		return mInstructionStackLock;
	}
	/**
	 * @param state the mInstructionStackLock to set
	 */
	public void setInstructionStackLock(boolean state) {
		this.mInstructionStackLock = state;
	}
	
	/**
	 * @return the mShimmerInfoMemBytes generated from an empty byte array. This
	 *         is called to generate the InfoMem bytes for writing to the
	 *         Shimmer.
	 */
	public byte[] generateShimmerInfoMemBytes() {
		return super.infoMemByteArrayGenerate(true);
	}


	
	public void consolePrintLn(String message) {
		if(mVerboseMode) {
			Calendar rightNow = Calendar.getInstance();
			String rightNowString = "[" + String.format("%02d",rightNow.get(Calendar.HOUR_OF_DAY)) 
					+ ":" + String.format("%02d",rightNow.get(Calendar.MINUTE)) 
					+ ":" + String.format("%02d",rightNow.get(Calendar.SECOND)) 
					+ ":" + String.format("%03d",rightNow.get(Calendar.MILLISECOND)) + "]";
			System.out.println(rightNowString + " " + mParentClassName + ": " + mUniqueID + " " + getMacIdFromBtParsed() + " " + message);
		}		
	}
	
	public void consolePrint(String message) {
		if(mVerboseMode) {
			System.out.print(message);
		}		
	}

	public void setVerboseMode(boolean verboseMode) {
		mVerboseMode = verboseMode;
	}
	
	public void setBattStatusDetails(ShimmerBattStatusDetails s) {
		super.mBattVoltage = s.mBattVoltage;
		super.mEstimatedChargePercentage = s.mEstimatedChargePercentage;
		super.mEstimatedChargePercentageParsed = s.mEstimatedChargePercentageParsed;
		super.mChargingState = s.mChargingStatusParsed;
		batteryStatusChanged();
	}

	public void clearBattStatusDetails() {
		super.mBattVoltage = "";
		super.mEstimatedChargePercentage = 0.0;
		super.mEstimatedChargePercentageParsed = "";
		super.mChargingState = "";
		batteryStatusChanged();
	}
	
	private void buildAndSendMsg(byte[] packet, int fwID, int timeSync, long pcTimeStamp){
		ObjectCluster objectCluster = null;
		try {
			objectCluster = buildMsg(packet, fwID, timeSync, pcTimeStamp);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (mDataProcessing!=null){
			objectCluster = mDataProcessing.ProcessData(objectCluster);
		}
		dataHandler(objectCluster);
	}
}
