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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;

import org.apache.commons.lang3.ArrayUtils;

import com.shimmerresearch.comms.radioProtocol.ShimmerLiteProtocolInstructionSet.LiteProtocolInstructionSet.InstructionsGet;
import com.shimmerresearch.comms.radioProtocol.ShimmerLiteProtocolInstructionSet.LiteProtocolInstructionSet.InstructionsResponse;
import com.shimmerresearch.comms.radioProtocol.ShimmerLiteProtocolInstructionSet.LiteProtocolInstructionSet.InstructionsSet;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.FormatCluster;
import com.shimmerresearch.driver.InfoMemLayoutShimmer3;
import com.shimmerresearch.driver.Configuration.CHANNEL_UNITS;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driver.Configuration.Shimmer3;
import com.shimmerresearch.driver.calibration.CalibDetails.CALIB_READ_SOURCE;
import com.shimmerresearch.driver.ShimmerObject;
import com.shimmerresearch.driverUtilities.ExpansionBoardDetails;
import com.shimmerresearch.driverUtilities.ShimmerBattStatusDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;
import com.shimmerresearch.driverUtilities.UtilShimmer;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_TYPE;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.FW_ID;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.HW_ID;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.exgConfig.ExGConfigOptionDetails.EXG_CHIP_INDEX;

public abstract class ShimmerBluetooth extends ShimmerObject implements Serializable{
	
	/** * */
	private static final long serialVersionUID = 8439353551730215801L;

	//region --------- CLASS VARIABLES AND ABSTRACT METHODS ---------
	
	protected long mSetEnabledSensors = SENSOR_ACCEL;								// Only used during the initialization process, see initialize();
	
	private int mNumberofTXRetriesCount=1;
	private final static int NUMBER_OF_TX_RETRIES_LIMIT = 0;
	
	/**
	 * LogAndStream will try to recreate the SD config. file for each block of
	 * InfoMem that is written - need to give it time to do so.
	 */
	private static final int DELAY_BETWEEN_INFOMEM_WRITES = 100;
	/** Delay to allow LogAndStream to create SD config. file and reinitialise */
	private static final int DELAY_AFTER_INFOMEM_WRITE = 500;
	
	public enum BT_STATE{
		DISCONNECTED("Disconnected"),
		CONNECTING("Connecting"), // The class is now initiating an outgoing connection
		CONNECTED("Ready"),  // The class is now connected to a remote device
		STREAMING("Streaming"),  // The class is now connected to a remote device
		STREAMING_AND_SDLOGGING("Streaming and SD Logging"),
		SDLOGGING("SD Logging"),
		CONFIGURING("Configuring"), // The class is now initiating an outgoing connection 
		CONNECTION_LOST("Lost connection"),
		CONNECTION_FAILED("Connection Failed");
		
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
	
	private boolean mInstructionStackLock = false;
	protected boolean mSendProgressReport = true;
	protected boolean mOperationUnderway = false;
	protected byte mCurrentCommand;	
	protected boolean mWaitForAck=false;                                          // This indicates whether the device is waiting for an acknowledge packet from the Shimmer Device  
	protected boolean mWaitForResponse=false; 									// This indicates whether the device is waiting for a response packet from the Shimmer Device 
	protected boolean mTransactionCompleted=true;									// Variable is used to ensure a command has finished execution prior to executing the next command (see initialize())
	transient protected IOThread mIOThread;
	transient protected ProcessingThread mPThread;
	@Deprecated // mContinousSync doesn't do anything
	private boolean mContinousSync=false;                                       // This is to select whether to continuously check the data packets 
	protected boolean mSetupDevice=false;		
	protected Stack<Byte> byteStack = new Stack<Byte>();
	protected double mLowBattLimit=3.4;
	protected int numBytesToReadFromExpBoard=0;
	
	private boolean mUseInfoMemConfigMethod = false;

	ArrayBlockingQueue<RawBytePacketWithPCTimeStamp> mABQPacketByeArray = new ArrayBlockingQueue<RawBytePacketWithPCTimeStamp>(10000);
	public List<Long> mListofPCTimeStamps = new ArrayList<Long>();
	
	protected boolean mIamAlive = false;
	protected abstract void connect(String address,String bluetoothLibrary);
	protected abstract void dataHandler(ObjectCluster ojc);
	protected abstract boolean bytesAvailableToBeRead();
	protected abstract int availableBytes();
	
	protected abstract void writeBytes(byte[] data);
	protected abstract void stop();
	protected abstract void sendProgressReport(BluetoothProgressReportPerCmd pr);
	protected abstract void isReadyForStreaming();
	protected abstract void isNowStreaming();
	protected abstract void hasStopStreaming();
	protected abstract void sendStatusMsgPacketLossDetected();
	protected abstract void inquiryDone();
	protected abstract void sendStatusMSGtoUI(String msg);
	protected abstract void printLogDataForDebugging(String msg);
	protected abstract void connectionLost();
	//protected abstract void setBluetoothRadioState(BT_STATE state);
	protected abstract void startOperation(BT_STATE currentOperation);
	protected abstract void finishOperation(BT_STATE currentOperation);
	protected abstract void startOperation(BT_STATE currentOperation, int totalNumOfCmds);
	protected abstract void eventLogAndStreamStatusChanged(byte currentCommand);
	protected abstract void batteryStatusChanged();
	protected abstract byte[] readBytes(int numberofBytes);
	protected abstract byte readByte();
	protected abstract void dockedStateChange();
	
	private boolean mFirstPacketParsed=true;
	private double mOffsetFirstTime=-1;
	protected List<byte []> mListofInstructions = new  ArrayList<byte[]>();
	private final int ACK_TIMER_DURATION = 2; 									// Duration to wait for an ack packet (seconds)
	protected boolean mDummy=false;
	protected boolean mFirstTime=true;
	private byte mTempByteValue;												// A temporary variable used to store Byte value	
	protected int mTempIntValue;												// A temporary variable used to store Integer value, used mainly to store a value while waiting for an acknowledge packet (e.g. when writeGRange() is called, the range is stored temporarily and used to update GSRRange when the acknowledge packet is received.
	protected long tempEnabledSensors;											// This stores the enabled sensors
	private int mTempChipID;
	
	private int mCurrentMemAddress = 0;
	private int mCurrentMemLengthToRead = 0;
	private int mCalibDumpSize = 0;
	private byte[] mMemBuffer;
	private byte[] mCalibDumpBuffer;

	protected boolean mSync=true;												// Variable to keep track of sync
	protected boolean mSetupEXG = false;
	private byte[] cmdcalibrationParameters = new byte [22];  
	
	//startregion --------- TIMERS ---------
	private int mReadStatusPeriod=5000;
	private int mReadBattStatusPeriod=600000;	// Batt status is updated every 10 mins 
	private int mCheckAlivePeriod=2000;
	
	transient protected Timer mTimerCheckForAckOrResp;								// Timer variable used when waiting for an ack or response packet
	transient protected Timer mTimerCheckAlive;
	transient protected Timer mTimerReadStatus;
	transient protected Timer mTimerReadBattStatus;								// 
	
	private int mCountDeadConnection = 0;
	private boolean mCheckIfConnectionisAlive = false;
	//endregion --------- TIMERS ---------

	transient ByteArrayOutputStream mByteArrayOutputStream = new ByteArrayOutputStream();
	
//	private boolean mVerboseMode = true;
//	private String mParentClassName = "ShimmerBluetooth";
	
	protected boolean mUseProcessingThread = false;
	
    public static final Map<Byte, BtCommandDetails> mBtCommandMapOther;
    static {
        Map<Byte, BtCommandDetails> aMap = new LinkedHashMap<Byte, BtCommandDetails>();

        aMap.put(DATA_PACKET, 					new BtCommandDetails(DATA_PACKET, "DATA_PACKET"));
        aMap.put(ROUTINE_COMMUNICATION, 		new BtCommandDetails(ROUTINE_COMMUNICATION, "ROUTINE_COMMUNICATION"));
        aMap.put(ACK_COMMAND_PROCESSED, 		new BtCommandDetails(ACK_COMMAND_PROCESSED, "ACK_COMMAND_PROCESSED"));

        mBtCommandMapOther = Collections.unmodifiableMap(aMap);
    }
    
    public static final Map<Byte, BtCommandDetails> mBtGetCommandMap;
    static {
        Map<Byte, BtCommandDetails> aMap = new LinkedHashMap<Byte, BtCommandDetails>();
        
        aMap.put(INQUIRY_COMMAND, 				new BtCommandDetails(INQUIRY_COMMAND, "INQUIRY_COMMAND", INQUIRY_RESPONSE));

        aMap.put(GET_SAMPLING_RATE_COMMAND, 	new BtCommandDetails(GET_SAMPLING_RATE_COMMAND, "GET_SAMPLING_RATE_COMMAND", SAMPLING_RATE_RESPONSE));
        aMap.put(GET_ACCEL_SENSITIVITY_COMMAND, new BtCommandDetails(GET_ACCEL_SENSITIVITY_COMMAND, "GET_ACCEL_SENSITIVITY_COMMAND", ACCEL_SENSITIVITY_RESPONSE));
        aMap.put(GET_CONFIG_BYTE0_COMMAND, 		new BtCommandDetails(GET_CONFIG_BYTE0_COMMAND, "GET_CONFIG_BYTE0_COMMAND", CONFIG_BYTE0_RESPONSE));
        aMap.put(GET_ACCEL_CALIBRATION_COMMAND, new BtCommandDetails(GET_ACCEL_CALIBRATION_COMMAND, "GET_ACCEL_CALIBRATION_COMMAND", ACCEL_CALIBRATION_RESPONSE));
        aMap.put(GET_GYRO_CALIBRATION_COMMAND, 	new BtCommandDetails(GET_GYRO_CALIBRATION_COMMAND, "GET_GYRO_CALIBRATION_COMMAND", GYRO_CALIBRATION_RESPONSE));
        aMap.put(GET_MAG_CALIBRATION_COMMAND, 	new BtCommandDetails(GET_MAG_CALIBRATION_COMMAND, "GET_MAG_CALIBRATION_COMMAND", MAG_CALIBRATION_RESPONSE));
        aMap.put(GET_LSM303DLHC_ACCEL_CALIBRATION_COMMAND, new BtCommandDetails(GET_LSM303DLHC_ACCEL_CALIBRATION_COMMAND, "GET_LSM303DLHC_ACCEL_CALIBRATION_COMMAND", LSM303DLHC_ACCEL_CALIBRATION_RESPONSE));
        aMap.put(GET_GSR_RANGE_COMMAND, 		new BtCommandDetails(GET_GSR_RANGE_COMMAND, "GET_GSR_RANGE_COMMAND", GSR_RANGE_RESPONSE));
        aMap.put(GET_SHIMMER_VERSION_COMMAND, 	new BtCommandDetails(GET_SHIMMER_VERSION_COMMAND, "GET_SHIMMER_VERSION_COMMAND", GET_SHIMMER_VERSION_RESPONSE));
        aMap.put(GET_SHIMMER_VERSION_COMMAND_NEW, new BtCommandDetails(GET_SHIMMER_VERSION_COMMAND_NEW, "GET_SHIMMER_VERSION_COMMAND_NEW", GET_SHIMMER_VERSION_RESPONSE)); //this is to avoid the $ char which is used by rn42
        aMap.put(GET_EMG_CALIBRATION_COMMAND, 	new BtCommandDetails(GET_EMG_CALIBRATION_COMMAND, "GET_EMG_CALIBRATION_COMMAND", EMG_CALIBRATION_RESPONSE));
        aMap.put(GET_ECG_CALIBRATION_COMMAND, 	new BtCommandDetails(GET_ECG_CALIBRATION_COMMAND, "GET_ECG_CALIBRATION_COMMAND", ECG_CALIBRATION_RESPONSE));
        aMap.put(GET_ALL_CALIBRATION_COMMAND, 	new BtCommandDetails(GET_ALL_CALIBRATION_COMMAND, "GET_ALL_CALIBRATION_COMMAND", ALL_CALIBRATION_RESPONSE));
        aMap.put(GET_FW_VERSION_COMMAND, 		new BtCommandDetails(GET_FW_VERSION_COMMAND, "GET_FW_VERSION_COMMAND", FW_VERSION_RESPONSE));
        aMap.put(GET_BLINK_LED, 				new BtCommandDetails(GET_BLINK_LED, "GET_BLINK_LED", BLINK_LED_RESPONSE));
        aMap.put(GET_BUFFER_SIZE_COMMAND, 		new BtCommandDetails(GET_BUFFER_SIZE_COMMAND, "GET_BUFFER_SIZE_COMMAND", BUFFER_SIZE_RESPONSE));
        aMap.put(GET_MAG_GAIN_COMMAND, 			new BtCommandDetails(GET_MAG_GAIN_COMMAND, "GET_MAG_GAIN_COMMAND", MAG_GAIN_RESPONSE));
        aMap.put(GET_MAG_SAMPLING_RATE_COMMAND, new BtCommandDetails(GET_MAG_SAMPLING_RATE_COMMAND, "GET_MAG_SAMPLING_RATE_COMMAND", MAG_SAMPLING_RATE_RESPONSE));
        aMap.put(GET_ACCEL_SAMPLING_RATE_COMMAND, new BtCommandDetails(GET_ACCEL_SAMPLING_RATE_COMMAND, "GET_ACCEL_SAMPLING_RATE_COMMAND", ACCEL_SAMPLING_RATE_RESPONSE));
        aMap.put(GET_LSM303DLHC_ACCEL_LPMODE_COMMAND, new BtCommandDetails(GET_LSM303DLHC_ACCEL_LPMODE_COMMAND, "GET_LSM303DLHC_ACCEL_LPMODE_COMMAND", LSM303DLHC_ACCEL_LPMODE_RESPONSE));
        aMap.put(GET_LSM303DLHC_ACCEL_HRMODE_COMMAND, new BtCommandDetails(GET_LSM303DLHC_ACCEL_HRMODE_COMMAND, "GET_LSM303DLHC_ACCEL_HRMODE_COMMAND", LSM303DLHC_ACCEL_HRMODE_RESPONSE));
        aMap.put(GET_MPU9150_GYRO_RANGE_COMMAND, new BtCommandDetails(GET_MPU9150_GYRO_RANGE_COMMAND, "GET_MPU9150_GYRO_RANGE_COMMAND", MPU9150_GYRO_RANGE_RESPONSE));
        aMap.put(GET_MPU9150_SAMPLING_RATE_COMMAND, new BtCommandDetails(GET_MPU9150_SAMPLING_RATE_COMMAND, "GET_MPU9150_SAMPLING_RATE_COMMAND", MPU9150_SAMPLING_RATE_RESPONSE));
        aMap.put(GET_BMP180_PRES_RESOLUTION_COMMAND, new BtCommandDetails(GET_BMP180_PRES_RESOLUTION_COMMAND, "GET_BMP180_PRES_RESOLUTION_COMMAND", BMP180_PRES_RESOLUTION_RESPONSE));
        aMap.put(GET_BMP180_PRES_CALIBRATION_COMMAND, new BtCommandDetails(GET_BMP180_PRES_CALIBRATION_COMMAND, "GET_BMP180_PRES_CALIBRATION_COMMAND", BMP180_PRES_CALIBRATION_RESPONSE));
        aMap.put(GET_BMP180_CALIBRATION_COEFFICIENTS_COMMAND, new BtCommandDetails(GET_BMP180_CALIBRATION_COEFFICIENTS_COMMAND, "GET_BMP180_CALIBRATION_COEFFICIENTS_COMMAND", BMP180_CALIBRATION_COEFFICIENTS_RESPONSE));
        aMap.put(GET_MPU9150_MAG_SENS_ADJ_VALS_COMMAND, new BtCommandDetails(GET_MPU9150_MAG_SENS_ADJ_VALS_COMMAND, "GET_MPU9150_MAG_SENS_ADJ_VALS_COMMAND", MPU9150_MAG_SENS_ADJ_VALS_RESPONSE));
        aMap.put(GET_INTERNAL_EXP_POWER_ENABLE_COMMAND, new BtCommandDetails(GET_INTERNAL_EXP_POWER_ENABLE_COMMAND, "GET_INTERNAL_EXP_POWER_ENABLE_COMMAND", INTERNAL_EXP_POWER_ENABLE_RESPONSE));
        aMap.put(GET_EXG_REGS_COMMAND,			new BtCommandDetails(GET_EXG_REGS_COMMAND, "GET_EXG_REGS_COMMAND", EXG_REGS_RESPONSE));
        aMap.put(GET_DAUGHTER_CARD_ID_COMMAND, 	new BtCommandDetails(GET_DAUGHTER_CARD_ID_COMMAND, "GET_DAUGHTER_CARD_ID_COMMAND", DAUGHTER_CARD_ID_RESPONSE));
        aMap.put(GET_BAUD_RATE_COMMAND, 		new BtCommandDetails(GET_BAUD_RATE_COMMAND, "GET_BAUD_RATE_COMMAND", BAUD_RATE_RESPONSE));
        aMap.put(GET_DERIVED_CHANNEL_BYTES, 	new BtCommandDetails(GET_DERIVED_CHANNEL_BYTES, "GET_DERIVED_CHANNEL_BYTES", DERIVED_CHANNEL_BYTES_RESPONSE));
        aMap.put(GET_STATUS_COMMAND, 			new BtCommandDetails(GET_STATUS_COMMAND, "GET_STATUS_COMMAND", STATUS_RESPONSE));
        aMap.put(GET_TRIAL_CONFIG_COMMAND, 		new BtCommandDetails(GET_TRIAL_CONFIG_COMMAND, "GET_TRIAL_CONFIG_COMMAND", TRIAL_CONFIG_RESPONSE));
        aMap.put(GET_CENTER_COMMAND, 			new BtCommandDetails(GET_CENTER_COMMAND, "GET_CENTER_COMMAND", CENTER_RESPONSE));
        aMap.put(GET_SHIMMERNAME_COMMAND, 		new BtCommandDetails(GET_SHIMMERNAME_COMMAND, "GET_SHIMMERNAME_COMMAND", SHIMMERNAME_RESPONSE));
        aMap.put(GET_EXPID_COMMAND, 			new BtCommandDetails(GET_EXPID_COMMAND, "GET_EXPID_COMMAND", EXPID_RESPONSE));
        aMap.put(GET_MYID_COMMAND, 				new BtCommandDetails(GET_MYID_COMMAND, "GET_MYID_COMMAND", MYID_RESPONSE));
        aMap.put(GET_NSHIMMER_COMMAND, 			new BtCommandDetails(GET_NSHIMMER_COMMAND, "GET_NSHIMMER_COMMAND", NSHIMMER_RESPONSE));
        aMap.put(GET_CONFIGTIME_COMMAND, 		new BtCommandDetails(GET_CONFIGTIME_COMMAND, "GET_CONFIGTIME_COMMAND", CONFIGTIME_RESPONSE));
        aMap.put(GET_DIR_COMMAND, 				new BtCommandDetails(GET_DIR_COMMAND, "GET_DIR_COMMAND", DIR_RESPONSE));
        aMap.put(GET_INFOMEM_COMMAND, 			new BtCommandDetails(GET_INFOMEM_COMMAND, "GET_INFOMEM_COMMAND", INFOMEM_RESPONSE));
        aMap.put(GET_CALIB_DUMP_COMMAND, 		new BtCommandDetails(GET_CALIB_DUMP_COMMAND, "GET_CALIB_DUMP_COMMAND", RSP_CALIB_DUMP_COMMAND));
        aMap.put(GET_RWC_COMMAND, 				new BtCommandDetails(GET_RWC_COMMAND, "GET_RWC_COMMAND", RWC_RESPONSE));
        aMap.put(GET_VBATT_COMMAND, 			new BtCommandDetails(GET_VBATT_COMMAND, "GET_VBATT_COMMAND", VBATT_RESPONSE));
        
        mBtGetCommandMap = Collections.unmodifiableMap(aMap);
    }
    
    public static final Map<Byte, BtCommandDetails> mBtSetCommandMap;
    static {
        Map<Byte, BtCommandDetails> aMap = new LinkedHashMap<Byte, BtCommandDetails>();

        aMap.put(STOP_STREAMING_COMMAND, 		new BtCommandDetails(STOP_STREAMING_COMMAND, "STOP_STREAMING_COMMAND")); //doesn't receive ack
        
        aMap.put(RESET_TO_DEFAULT_CONFIGURATION_COMMAND, new BtCommandDetails(RESET_TO_DEFAULT_CONFIGURATION_COMMAND, "RESET_TO_DEFAULT_CONFIGURATION_COMMAND"));
        aMap.put(RESET_CALIBRATION_VALUE_COMMAND, new BtCommandDetails(RESET_CALIBRATION_VALUE_COMMAND, "RESET_CALIBRATION_VALUE_COMMAND"));
        
        aMap.put(TEST_CONNECTION_COMMAND, 		new BtCommandDetails(TEST_CONNECTION_COMMAND, "TEST_CONNECTION_COMMAND"));
        
        aMap.put(TOGGLE_LED_COMMAND, 			new BtCommandDetails(TOGGLE_LED_COMMAND, "TOGGLE_LED_COMMAND"));
        aMap.put(START_STREAMING_COMMAND, 		new BtCommandDetails(START_STREAMING_COMMAND, "START_STREAMING_COMMAND"));
        aMap.put(START_SDBT_COMMAND, 			new BtCommandDetails(START_SDBT_COMMAND, "START_SDBT_COMMAND"));
        aMap.put(STOP_SDBT_COMMAND, 			new BtCommandDetails(STOP_SDBT_COMMAND, "STOP_SDBT_COMMAND"));
        aMap.put(START_LOGGING_ONLY_COMMAND, 	new BtCommandDetails(START_LOGGING_ONLY_COMMAND, "START_LOGGING_ONLY_COMMAND"));
        aMap.put(STOP_LOGGING_ONLY_COMMAND, 	new BtCommandDetails(STOP_LOGGING_ONLY_COMMAND, "STOP_LOGGING_ONLY_COMMAND"));
        aMap.put(SET_SAMPLING_RATE_COMMAND, 	new BtCommandDetails(SET_SAMPLING_RATE_COMMAND, "SET_SAMPLING_RATE_COMMAND"));
        aMap.put(SET_SENSORS_COMMAND, 			new BtCommandDetails(SET_SENSORS_COMMAND, "SET_SENSORS_COMMAND"));
        aMap.put(SET_ACCEL_SENSITIVITY_COMMAND, new BtCommandDetails(SET_ACCEL_SENSITIVITY_COMMAND, "SET_ACCEL_SENSITIVITY_COMMAND"));
        aMap.put(SET_5V_REGULATOR_COMMAND, 		new BtCommandDetails(SET_5V_REGULATOR_COMMAND, "SET_5V_REGULATOR_COMMAND")); // only Shimmer 2
        aMap.put(SET_PMUX_COMMAND, 				new BtCommandDetails(SET_PMUX_COMMAND, "SET_PMUX_COMMAND")); // only Shimmer 2
        aMap.put(SET_CONFIG_BYTE0_COMMAND, 		new BtCommandDetails(SET_CONFIG_BYTE0_COMMAND, "SET_CONFIG_BYTE0_COMMAND"));
        aMap.put(SET_ACCEL_CALIBRATION_COMMAND, new BtCommandDetails(SET_ACCEL_CALIBRATION_COMMAND, "SET_ACCEL_CALIBRATION_COMMAND"));
        aMap.put(SET_LSM303DLHC_ACCEL_CALIBRATION_COMMAND, new BtCommandDetails(SET_LSM303DLHC_ACCEL_CALIBRATION_COMMAND, "SET_LSM303DLHC_ACCEL_CALIBRATION_COMMAND"));
        aMap.put(SET_GYRO_CALIBRATION_COMMAND, 	new BtCommandDetails(SET_GYRO_CALIBRATION_COMMAND, "SET_GYRO_CALIBRATION_COMMAND"));
        aMap.put(SET_MAG_CALIBRATION_COMMAND, 	new BtCommandDetails(SET_MAG_CALIBRATION_COMMAND, "SET_MAG_CALIBRATION_COMMAND"));
        aMap.put(SET_GSR_RANGE_COMMAND, 		new BtCommandDetails(SET_GSR_RANGE_COMMAND, "SET_GSR_RANGE_COMMAND"));
        aMap.put(SET_EMG_CALIBRATION_COMMAND, 	new BtCommandDetails(SET_EMG_CALIBRATION_COMMAND, "SET_EMG_CALIBRATION_COMMAND"));
        aMap.put(SET_ECG_CALIBRATION_COMMAND, 	new BtCommandDetails(SET_ECG_CALIBRATION_COMMAND, "SET_ECG_CALIBRATION_COMMAND"));
        aMap.put(SET_BLINK_LED, 				new BtCommandDetails(SET_BLINK_LED, "SET_BLINK_LED"));
        aMap.put(SET_GYRO_TEMP_VREF_COMMAND, 	new BtCommandDetails(SET_GYRO_TEMP_VREF_COMMAND, "SET_GYRO_TEMP_VREF_COMMAND"));
        aMap.put(SET_BUFFER_SIZE_COMMAND, 		new BtCommandDetails(SET_BUFFER_SIZE_COMMAND, "SET_BUFFER_SIZE_COMMAND"));
        aMap.put(SET_MAG_GAIN_COMMAND, 			new BtCommandDetails(SET_MAG_GAIN_COMMAND, "SET_MAG_GAIN_COMMAND"));
        aMap.put(SET_MAG_SAMPLING_RATE_COMMAND, new BtCommandDetails(SET_MAG_SAMPLING_RATE_COMMAND, "SET_MAG_SAMPLING_RATE_COMMAND"));
        aMap.put(SET_ACCEL_SAMPLING_RATE_COMMAND, new BtCommandDetails(SET_ACCEL_SAMPLING_RATE_COMMAND, "SET_ACCEL_SAMPLING_RATE_COMMAND"));
        aMap.put(SET_LSM303DLHC_ACCEL_LPMODE_COMMAND, new BtCommandDetails(SET_LSM303DLHC_ACCEL_LPMODE_COMMAND, "SET_LSM303DLHC_ACCEL_LPMODE_COMMAND"));
        aMap.put(SET_LSM303DLHC_ACCEL_HRMODE_COMMAND, new BtCommandDetails(SET_LSM303DLHC_ACCEL_HRMODE_COMMAND, "SET_LSM303DLHC_ACCEL_HRMODE_COMMAND"));
        aMap.put(SET_MPU9150_GYRO_RANGE_COMMAND, new BtCommandDetails(SET_MPU9150_GYRO_RANGE_COMMAND, "SET_MPU9150_GYRO_RANGE_COMMAND"));
        aMap.put(SET_MPU9150_SAMPLING_RATE_COMMAND, new BtCommandDetails(SET_MPU9150_SAMPLING_RATE_COMMAND, "SET_MPU9150_SAMPLING_RATE_COMMAND"));
        aMap.put(SET_BMP180_PRES_RESOLUTION_COMMAND, new BtCommandDetails(SET_BMP180_PRES_RESOLUTION_COMMAND, "SET_BMP180_PRES_RESOLUTION_COMMAND"));
        aMap.put(SET_BMP180_PRES_CALIBRATION_COMMAND, new BtCommandDetails(SET_BMP180_PRES_CALIBRATION_COMMAND, "SET_BMP180_PRES_CALIBRATION_COMMAND"));
        aMap.put(SET_INTERNAL_EXP_POWER_ENABLE_COMMAND, new BtCommandDetails(SET_INTERNAL_EXP_POWER_ENABLE_COMMAND, "SET_INTERNAL_EXP_POWER_ENABLE_COMMAND"));
        aMap.put(SET_EXG_REGS_COMMAND, 			new BtCommandDetails(SET_EXG_REGS_COMMAND, "SET_EXG_REGS_COMMAND"));
        aMap.put(SET_BAUD_RATE_COMMAND, 		new BtCommandDetails(SET_BAUD_RATE_COMMAND, "SET_BAUD_RATE_COMMAND"));
        aMap.put(SET_DERIVED_CHANNEL_BYTES, 	new BtCommandDetails(SET_DERIVED_CHANNEL_BYTES, "SET_DERIVED_CHANNEL_BYTES"));
        aMap.put(SET_TRIAL_CONFIG_COMMAND, 		new BtCommandDetails(SET_TRIAL_CONFIG_COMMAND, "SET_TRIAL_CONFIG_COMMAND"));
        aMap.put(SET_CENTER_COMMAND, 			new BtCommandDetails(SET_CENTER_COMMAND, "SET_CENTER_COMMAND"));
        aMap.put(SET_SHIMMERNAME_COMMAND, 		new BtCommandDetails(SET_SHIMMERNAME_COMMAND, "SET_SHIMMERNAME_COMMAND"));  //Shimmer Name
        aMap.put(SET_EXPID_COMMAND, 			new BtCommandDetails(SET_EXPID_COMMAND, "SET_EXPID_COMMAND")); //Experiment Name
        aMap.put(SET_MYID_COMMAND,				new BtCommandDetails(SET_MYID_COMMAND, "SET_MYID_COMMAND")); //Shimmer ID in trial
        aMap.put(SET_NSHIMMER_COMMAND, 			new BtCommandDetails(SET_NSHIMMER_COMMAND, "SET_NSHIMMER_COMMAND"));
        aMap.put(SET_CONFIGTIME_COMMAND, 		new BtCommandDetails(SET_CONFIGTIME_COMMAND, "SET_CONFIGTIME_COMMAND"));
        aMap.put(SET_INFOMEM_COMMAND, 			new BtCommandDetails(SET_INFOMEM_COMMAND, "SET_INFOMEM_COMMAND"));
        aMap.put(SET_CALIB_DUMP_COMMAND, 		new BtCommandDetails(SET_CALIB_DUMP_COMMAND, "SET_CALIB_DUMP_COMMAND"));
        aMap.put(UPD_CALIB_DUMP_COMMAND, 		new BtCommandDetails(UPD_CALIB_DUMP_COMMAND, "UPD_CALIB_DUMP_COMMAND"));
        aMap.put(UPD_SDLOG_CFG_COMMAND, 		new BtCommandDetails(UPD_SDLOG_CFG_COMMAND, "UPD_SDLOG_CFG_COMMAND"));
        aMap.put(SET_CRC_COMMAND, 				new BtCommandDetails(SET_CRC_COMMAND, "SET_CRC_COMMAND"));
        aMap.put(SET_RWC_COMMAND, 				new BtCommandDetails(SET_RWC_COMMAND, "SET_RWC_COMMAND"));
        
        mBtSetCommandMap = Collections.unmodifiableMap(aMap);
    }

    public static final Map<Byte, BtCommandDetails> mBtResponseMap;
    static {
        Map<Byte, BtCommandDetails> aMap = new LinkedHashMap<Byte, BtCommandDetails>();

        aMap.put(INQUIRY_RESPONSE, 				new BtCommandDetails(INQUIRY_RESPONSE, "INQUIRY_RESPONSE", -1));
        aMap.put(GET_SHIMMER_VERSION_RESPONSE, 	new BtCommandDetails(GET_SHIMMER_VERSION_RESPONSE, "GET_SHIMMER_VERSION_RESPONSE", 1));
        aMap.put(SAMPLING_RATE_RESPONSE, 		new BtCommandDetails(SAMPLING_RATE_RESPONSE, "SAMPLING_RATE_RESPONSE", -1)); 						//TODO 1 for Shimmer2R, 2 for Shimmer3
        aMap.put(ACCEL_SENSITIVITY_RESPONSE, 	new BtCommandDetails(ACCEL_SENSITIVITY_RESPONSE, "ACCEL_SENSITIVITY_RESPONSE", 1));
        aMap.put(CONFIG_BYTE0_RESPONSE, 		new BtCommandDetails(CONFIG_BYTE0_RESPONSE, "CONFIG_BYTE0_RESPONSE", -1)); 							//TODO 1 for Shimmer2R, 4 for Shimmer3
        aMap.put(ACCEL_CALIBRATION_RESPONSE, 	new BtCommandDetails(ACCEL_CALIBRATION_RESPONSE, "ACCEL_CALIBRATION_RESPONSE", 21));
        aMap.put(LSM303DLHC_ACCEL_CALIBRATION_RESPONSE, new BtCommandDetails(LSM303DLHC_ACCEL_CALIBRATION_RESPONSE, "LSM303DLHC_ACCEL_CALIBRATION_RESPONSE", 21));
        aMap.put(GYRO_CALIBRATION_RESPONSE, 	new BtCommandDetails(GYRO_CALIBRATION_RESPONSE, "GYRO_CALIBRATION_RESPONSE", 21));
        aMap.put(MAG_CALIBRATION_RESPONSE, 		new BtCommandDetails(MAG_CALIBRATION_RESPONSE, "MAG_CALIBRATION_RESPONSE", 21));
        aMap.put(GSR_RANGE_RESPONSE, 			new BtCommandDetails(GSR_RANGE_RESPONSE, "GSR_RANGE_RESPONSE", 1));
        aMap.put(EMG_CALIBRATION_RESPONSE, 		new BtCommandDetails(EMG_CALIBRATION_RESPONSE, "EMG_CALIBRATION_RESPONSE", 4));
        aMap.put(ECG_CALIBRATION_RESPONSE, 		new BtCommandDetails(ECG_CALIBRATION_RESPONSE, "ECG_CALIBRATION_RESPONSE", 8));
        aMap.put(ALL_CALIBRATION_RESPONSE, 		new BtCommandDetails(ALL_CALIBRATION_RESPONSE, "ALL_CALIBRATION_RESPONSE", -1));
        aMap.put(FW_VERSION_RESPONSE, 			new BtCommandDetails(FW_VERSION_RESPONSE, "FW_VERSION_RESPONSE", 6));
        aMap.put(BLINK_LED_RESPONSE, 			new BtCommandDetails(BLINK_LED_RESPONSE, "BLINK_LED_RESPONSE", 1));
        aMap.put(BUFFER_SIZE_RESPONSE, 			new BtCommandDetails(BUFFER_SIZE_RESPONSE, "BUFFER_SIZE_RESPONSE", 1));
        aMap.put(MAG_GAIN_RESPONSE, 			new BtCommandDetails(MAG_GAIN_RESPONSE, "MAG_GAIN_RESPONSE", 1));
        aMap.put(MAG_SAMPLING_RATE_RESPONSE, 	new BtCommandDetails(MAG_SAMPLING_RATE_RESPONSE, "MAG_SAMPLING_RATE_RESPONSE", 1));
        aMap.put(ACCEL_SAMPLING_RATE_RESPONSE,	new BtCommandDetails(ACCEL_SAMPLING_RATE_RESPONSE, "ACCEL_SAMPLING_RATE_RESPONSE", 1));
        aMap.put(LSM303DLHC_ACCEL_LPMODE_RESPONSE, new BtCommandDetails(LSM303DLHC_ACCEL_LPMODE_RESPONSE, "LSM303DLHC_ACCEL_LPMODE_RESPONSE", 1));
        aMap.put(LSM303DLHC_ACCEL_HRMODE_RESPONSE, new BtCommandDetails(LSM303DLHC_ACCEL_HRMODE_RESPONSE, "LSM303DLHC_ACCEL_HRMODE_RESPONSE", 1));
        aMap.put(MPU9150_GYRO_RANGE_RESPONSE, 	new BtCommandDetails(MPU9150_GYRO_RANGE_RESPONSE, "MPU9150_GYRO_RANGE_RESPONSE", 1)); 			
        aMap.put(MPU9150_SAMPLING_RATE_RESPONSE, new BtCommandDetails(MPU9150_SAMPLING_RATE_RESPONSE, "MPU9150_SAMPLING_RATE_RESPONSE", 1)); 		
        aMap.put(BMP180_PRES_RESOLUTION_RESPONSE, new BtCommandDetails(BMP180_PRES_RESOLUTION_RESPONSE, "BMP180_PRES_RESOLUTION_RESPONSE", 1)); 	
        aMap.put(BMP180_PRES_CALIBRATION_RESPONSE, new BtCommandDetails(BMP180_PRES_CALIBRATION_RESPONSE, "BMP180_PRES_CALIBRATION_RESPONSE", -1)); // Unhandled //TODO RS
        aMap.put(BMP180_CALIBRATION_COEFFICIENTS_RESPONSE, new BtCommandDetails(BMP180_CALIBRATION_COEFFICIENTS_RESPONSE, "BMP180_CALIBRATION_COEFFICIENTS_RESPONSE", 22));
        aMap.put(MPU9150_MAG_SENS_ADJ_VALS_RESPONSE, new BtCommandDetails(MPU9150_MAG_SENS_ADJ_VALS_RESPONSE, "MPU9150_MAG_SENS_ADJ_VALS_RESPONSE", -1)); // Unhandled
        aMap.put(INTERNAL_EXP_POWER_ENABLE_RESPONSE, new BtCommandDetails(INTERNAL_EXP_POWER_ENABLE_RESPONSE, "INTERNAL_EXP_POWER_ENABLE_RESPONSE", 1)); 
        aMap.put(EXG_REGS_RESPONSE, 			new BtCommandDetails(EXG_REGS_RESPONSE, "EXG_REGS_RESPONSE", 11));
        aMap.put(DAUGHTER_CARD_ID_RESPONSE, 	new BtCommandDetails(DAUGHTER_CARD_ID_RESPONSE, "DAUGHTER_CARD_ID_RESPONSE", 3+1));
        aMap.put(BAUD_RATE_RESPONSE, 			new BtCommandDetails(BAUD_RATE_RESPONSE, "BAUD_RATE_RESPONSE", 1));
        aMap.put(DERIVED_CHANNEL_BYTES_RESPONSE, new BtCommandDetails(DERIVED_CHANNEL_BYTES_RESPONSE, "DERIVED_CHANNEL_BYTES_RESPONSE", 3));
        aMap.put(STATUS_RESPONSE, 				new BtCommandDetails(STATUS_RESPONSE, "STATUS_RESPONSE", 1));
        aMap.put(TRIAL_CONFIG_RESPONSE, 		new BtCommandDetails(TRIAL_CONFIG_RESPONSE, "TRIAL_CONFIG_RESPONSE", 3));
        aMap.put(CENTER_RESPONSE, 				new BtCommandDetails(CENTER_RESPONSE, "CENTER_RESPONSE", 1));
        aMap.put(SHIMMERNAME_RESPONSE, 			new BtCommandDetails(SHIMMERNAME_RESPONSE, "SHIMMERNAME_RESPONSE", 1)); 							// first byte indicates length to subsequently read //TODO RS: '1' be '-1' or 1+-1?
        aMap.put(EXPID_RESPONSE, 				new BtCommandDetails(EXPID_RESPONSE, "EXPID_RESPONSE", 1)); 										// first byte indicates length to subsequently read //TODO RS: '1' be '-1' or 1+-1?
        aMap.put(MYID_RESPONSE, 				new BtCommandDetails(MYID_RESPONSE, "MYID_RESPONSE", -1)); 											// Unhandled
        aMap.put(NSHIMMER_RESPONSE, 			new BtCommandDetails(NSHIMMER_RESPONSE, "NSHIMMER_RESPONSE", -1)); 									// Unhandled
        aMap.put(CONFIGTIME_RESPONSE, 			new BtCommandDetails(CONFIGTIME_RESPONSE, "CONFIGTIME_RESPONSE", 1)); 								// first byte indicates length to subsequently read
        aMap.put(DIR_RESPONSE, 					new BtCommandDetails(DIR_RESPONSE, "DIR_RESPONSE", 1)); 											// first byte indicates length to subsequently read
        aMap.put(INSTREAM_CMD_RESPONSE, 		new BtCommandDetails(INSTREAM_CMD_RESPONSE, "INSTREAM_CMD_RESPONSE", 1)); 							// first byte indicates what in-stream command it is
        aMap.put(INFOMEM_RESPONSE, 				new BtCommandDetails(INFOMEM_RESPONSE, "INFOMEM_RESPONSE", -1)); 									// Unhandled
        aMap.put(RSP_CALIB_DUMP_COMMAND, 		new BtCommandDetails(RSP_CALIB_DUMP_COMMAND, "RSP_CALIB_DUMP_COMMAND", -1));							// first+second bytes indicate length to subsequently read
        aMap.put(RWC_RESPONSE, 					new BtCommandDetails(RWC_RESPONSE, "RWC_RESPONSE", 8));
        aMap.put(VBATT_RESPONSE, 				new BtCommandDetails(VBATT_RESPONSE, "VBATT_RESPONSE", 3));
        
        mBtResponseMap = Collections.unmodifiableMap(aMap);
    }
	
	//endregion
	
	private int mNumOfMemSetCmds = 0;

	public static final int MSG_IDENTIFIER_DATA_PACKET = 2;
	public static final int MSG_IDENTIFIER_DEVICE_PAIRED = 8;
	public static final int MSG_IDENTIFIER_DEVICE_UNPAIRED = 9;
	public static final int MSG_IDENTIFIER_NOTIFICATION_MESSAGE = 1;
	public static final int MSG_IDENTIFIER_PACKET_RECEPTION_RATE_OVERALL = 3;
	public static final int MSG_IDENTIFIER_PACKET_RECEPTION_RATE_CURRENT = 6;
	public static final int MSG_IDENTIFIER_PROGRESS_REPORT_ALL = 5;
	public static final int MSG_IDENTIFIER_PROGRESS_REPORT_PER_DEVICE = 4;
	public static final int MSG_IDENTIFIER_SHIMMER_DOCKED_STATE_CHANGE = 7;

	//	private boolean mVerboseMode = true;
	//	private String mParentClassName = "ShimmerPC";
		
	public static final int MSG_IDENTIFIER_STATE_CHANGE = 0;
	public static final int NOTIFICATION_SHIMMER_FULLY_INITIALIZED = 2;
	public static final int NOTIFICATION_SHIMMER_START_STREAMING = 1;
	public static final int NOTIFICATION_SHIMMER_STATE_CHANGE = 3;
	public static final int NOTIFICATION_SHIMMER_STOP_STREAMING = 0;

	
	public class ProcessingThread extends Thread {
		public boolean stop = false;
		int count=0;
		public synchronized void run() {
			while (!stop) {
				if(!mABQPacketByeArray.isEmpty()){
					count++;
					if(count%1000==0){
						consolePrintLn("Queue Size: " + mABQPacketByeArray.size());
						printLogDataForDebugging("Queue Size: " + mABQPacketByeArray.size() + "\n");
					}
					RawBytePacketWithPCTimeStamp rbp = mABQPacketByeArray.remove();
//					buildAndSendMsg(rbp.mDataArray, FW_TYPE_BT, false, rbp.mSystemTimeStamp);
					buildAndSendMsg(rbp.mDataArray, COMMUNICATION_TYPE.BLUETOOTH, false, rbp.mSystemTimeStamp);
				}
			} 
		} 
	} 
	
	//region --------- BLUETOOH STACK --------- 
	
	public class IOThread extends Thread {
		byte[] byteBuffer = {0};
		public boolean stop = false;
		
		public synchronized void run() {
			while (!stop) {
				// Process Instruction on stack. is an instruction running? if not proceed
				if(!isInstructionStackLock()){
					processNextInstruction();
				}
				
				if(mIsStreaming){
					processWhileStreaming();
				}
				else if(bytesAvailableToBeRead()){
					if(mWaitForAck) {
						processNotStreamingWaitForAck();
					} 
					else if(mWaitForResponse) {
						processNotStreamingWaitForResp();
					} 
					
					processBytesAvailableAndInstreamSupported();
				}
			}
		}
		
		private void processNextInstruction() {
			// check instruction stack, are there any other instructions left to be executed?
			if(!getListofInstructions().isEmpty()) {
				if(getListofInstructions().get(0)==null) {
					getListofInstructions().remove(0);
					printLogDataForDebugging("Null Removed");
				}
			}
			
			if(!getListofInstructions().isEmpty()){
				if(getListofInstructions().get(0)!=null) {
					byte[] insBytes = (byte[]) getListofInstructions().get(0);
					mCurrentCommand=insBytes[0];
					setInstructionStackLock(true);
					mWaitForAck=true;
					
					if(!mIsStreaming){
						clearSerialBuffer();
					}
					//Special cases
					if(mCurrentCommand==SET_RWC_COMMAND){
						// for Real-world time -> grab PC time just before
						// writing to Shimmer
						byte[] rtcTimeArray = UtilShimmer.convertMilliSecondsToShimmerRtcDataBytesLSB(System.currentTimeMillis());
						System.arraycopy(rtcTimeArray, 0, insBytes, 1, 8);
					}
					//TODO: are the two stops needed here? better to wait for ack from Shimmer
					if(mCurrentCommand==STOP_STREAMING_COMMAND 
							|| mCurrentCommand==STOP_SDBT_COMMAND){
						//DO NOTHING
					} 
					else {
						// Overwritten for commands that aren't supported
						// for older versions of Shimmer
						if(mCurrentCommand==SET_SENSORS_COMMAND 
								&& getShimmerVersion()==HW_ID.SHIMMER_2R){
							startTimerCheckForAckOrResp(ACK_TIMER_DURATION+8);
						} 
						else if((mCurrentCommand==GET_FW_VERSION_COMMAND)
									||(mCurrentCommand==GET_SAMPLING_RATE_COMMAND)
									||(mCurrentCommand==GET_SHIMMER_VERSION_COMMAND_NEW)){
								startTimerCheckForAckOrResp(ACK_TIMER_DURATION);
						 } 
						else {
							if(mIsStreaming){
								startTimerCheckForAckOrResp(ACK_TIMER_DURATION);
							}
							else {
								startTimerCheckForAckOrResp(ACK_TIMER_DURATION+3);
							}
						}
					}
					threadSleep((int)((Math.random()+.1)*100.0));
					writeBytes(insBytes);
					printLogDataForDebugging("Command Transmitted: \t\t\t" + btCommandToString(mCurrentCommand) + " " + UtilShimmer.bytesToHexStringWithSpacesFormatted(insBytes));

					//TODO: are the two stops needed here? better to wait for ack from Shimmer
					if(mCurrentCommand==STOP_STREAMING_COMMAND 
							|| mCurrentCommand==STOP_SDBT_COMMAND){
						mIsStreaming=false;
						if (mCurrentCommand==STOP_SDBT_COMMAND){
							mIsSDLogging = false;
						}
						getListofInstructions().removeAll(Collections.singleton(null));
					} 
					else {
						/*
						// Overwritten for commands that aren't supported
						// for older versions of Shimmer
						if((mCurrentCommand==GET_FW_VERSION_COMMAND)
								||(mCurrentCommand==GET_SAMPLING_RATE_COMMAND)
								||(mCurrentCommand==GET_SHIMMER_VERSION_COMMAND_NEW)){
							startTimerCheckForAckOrResp(ACK_TIMER_DURATION);
						}
						else {
							if(mIsStreaming){
								startTimerCheckForAckOrResp(ACK_TIMER_DURATION);
							}
							else {
								startTimerCheckForAckOrResp(ACK_TIMER_DURATION+3);
							}
						}*/
					}
					
					
					mTransactionCompleted=false;
				}
			} else {
				if (!mIsStreaming && !bytesAvailableToBeRead()){
					threadSleep(50);
				}
			}
		}

		
		private void processWhileStreaming() {
			byteBuffer = readBytes(1);
			if(byteBuffer!=null){
				mByteArrayOutputStream.write(byteBuffer[0]);
				//Everytime a byte is received the timestamp is taken
				mListofPCTimeStamps.add(System.currentTimeMillis());
			} 
			else {
				printLogDataForDebugging("readbyte null");
			}

			//If there is a full packet and the subsequent sequence number of following packet
			if(mByteArrayOutputStream.size()>=mPacketSize+2){ // +2 because there are two acks
				processPacket();
			} 
		}

		private void processPacket() {
			mIamAlive = true;
			byte[] bufferTemp = mByteArrayOutputStream.toByteArray();
			
			//Data packet followed by another data packet
			if(bufferTemp[0]==DATA_PACKET 
					&& bufferTemp[mPacketSize+1]==DATA_PACKET){
				//Handle the data packet
				processDataPacket(bufferTemp);
				clearSingleDataPacketFromBuffers(bufferTemp, mPacketSize+1);
			} 
			
			//Data packet followed by an ACK (suggesting an ACK in response to a SET BT command or else a BT response command)
			else if(bufferTemp[0]==DATA_PACKET 
					&& bufferTemp[mPacketSize+1]==ACK_COMMAND_PROCESSED){
				if(mByteArrayOutputStream.size()>mPacketSize+2){
					
					if(bufferTemp[mPacketSize+2]==DATA_PACKET){
						//Firstly handle the data packet
						processDataPacket(bufferTemp);
						clearSingleDataPacketFromBuffers(bufferTemp, mPacketSize+2);
						
						//Then handle the ACK from the last SET command
						if(isKnownSetCommand(mCurrentCommand)){
							stopTimerCheckForAckOrResp(); //cancel the ack timer
							mWaitForAck=false;
							
							processAckFromSetCommand(mCurrentCommand);
							
							mTransactionCompleted = true;
							setInstructionStackLock(false);
						}
						printLogDataForDebugging("Ack Received for Command: \t\t\t" + btCommandToString(mCurrentCommand));
					}
					
					//this is for LogAndStream support, command is transmitted and ack received
					else if(getFirmwareIdentifier()==FW_ID.LOGANDSTREAM 
							&& bufferTemp[mPacketSize+2]==INSTREAM_CMD_RESPONSE){ 
						printLogDataForDebugging("COMMAND TXed and ACK RECEIVED IN STREAM");
						printLogDataForDebugging("INS CMD RESP");

						//Firstly handle the in-stream response
						stopTimerCheckForAckOrResp(); //cancel the ack timer
						mWaitForResponse=false;
						mWaitForAck=false;

						processInstreamResponse();

						// Need to remove here because it is an
						// in-stream response while streaming so not
						// handled elsewhere
						if(getListofInstructions().size()>0){
							getListofInstructions().remove(0);
						}
						
						mTransactionCompleted=true;
						setInstructionStackLock(false);

						//Then process the Data packet
						processDataPacket(bufferTemp);
						clearBuffers();
					} 
					else {
						printLogDataForDebugging("Unknown parsing error while streaming");
					}
				} 
				if(mByteArrayOutputStream.size()>mPacketSize+2){
					printLogDataForDebugging("Unknown packet error (check with JC):\tExpected: " + (mPacketSize+2) + "bytes but buffer contains " + mByteArrayOutputStream.size() + "bytes");
					discardFirstBufferByte(); //throw the first byte away
				}
				
			} 
			//TODO: ACK in bufferTemp[0] not handled
			//else if
			else {
				printLogDataForDebugging("Packet syncing problem:\tExpected: " + (mPacketSize+2) + "bytes. Buffer contains " + mByteArrayOutputStream.size() + "bytes\n" + UtilShimmer.bytesToHexStringWithSpacesFormatted(mByteArrayOutputStream.toByteArray()));
				discardFirstBufferByte(); //throw the first byte away
			}
		}
		
		/** Process ACK from a GET or SET command while not streaming */ 
		private void processNotStreamingWaitForAck() {
			//JC TEST:: IMPORTANT TO REMOVE // This is to simulate packet loss 
			/*
			if (Math.random()>0.9 && mIsInitialised==true){
				if(bytesAvailableToBeRead() 
						&& mCurrentCommand!=TEST_CONNECTION_COMMAND	
						&& mCurrentCommand!=GET_STATUS_COMMAND	
						&& mCurrentCommand!= GET_VBATT_COMMAND 
						&& mCurrentCommand!=START_STREAMING_COMMAND
						&& mCurrentCommand!=STOP_STREAMING_COMMAND 
						&& mCurrentCommand!=SET_RWC_COMMAND 
						&& mCurrentCommand!=GET_RWC_COMMAND){
					tb=readBytes(1);
					tb=null;
				}
			}
			*/
			//JC TEST:: IMPORTANT TO REMOVE
			
			if(bytesAvailableToBeRead()){
				byteBuffer=readBytes(1);
				mNumberofTXRetriesCount = 0;
				mIamAlive = true;
				
				//TODO: ACK is probably now working for STOP_STREAMING_COMMAND so merge in with others?
				if(mCurrentCommand==STOP_STREAMING_COMMAND 
						|| mCurrentCommand==STOP_SDBT_COMMAND) { //due to not receiving the ack from stop streaming command we will skip looking for it.
					stopTimerCheckForAckOrResp();
					mIsStreaming=false;
					mTransactionCompleted=true;
					mWaitForAck=false;
					
					delayForBtResponse(200); // Wait to ensure the packet has been fully received
					byteStack.clear();

					clearSerialBuffer();
					
					hasStopStreaming();					
					getListofInstructions().remove(0);
					getListofInstructions().removeAll(Collections.singleton(null));
					if (mCurrentCommand==STOP_SDBT_COMMAND){
						eventLogAndStreamStatusChanged(mCurrentCommand);	
					}
					setInstructionStackLock(false);
				}
//				//TODO: ACK is probably now working for STOP_STREAMING_COMMAND so merge in with others?
//				if(mCurrentCommand==STOP_SDBT_COMMAND) { //due to not receiving the ack from stop streaming command we will skip looking for it.
//					stopTimerCheckForAckOrResp();
//					mIsStreaming=false;
//					mIsSDLogging=false;
//					mTransactionCompleted=true;
//					mWaitForAck=false;
//					
//					delayForBtResponse(200); // Wait to ensure the packet has been fully received
//					byteStack.clear();
//
//					clearSerialBuffer();
//					
//					hasStopStreaming();					
//					getListofInstructions().remove(0);
//					getListofInstructions().removeAll(Collections.singleton(null));
//					setInstructionStackLock(false);
//				}
				if(byteBuffer != null){
					if((byte)byteBuffer[0]==ACK_COMMAND_PROCESSED) {

						mWaitForAck=false;
						printLogDataForDebugging("Ack Received for Command: \t\t" + btCommandToString(mCurrentCommand));

						// Send status report if needed by the
						// application and is not one of the below
						// commands that are triggered by timers
						if(mCurrentCommand!=GET_STATUS_COMMAND 
								&& mCurrentCommand!=TEST_CONNECTION_COMMAND 
								&& mCurrentCommand!=SET_BLINK_LED 
//								&& mCurrentCommand!=GET_CALIB_DUMP_COMMAND 
								//&& mCurrentCommand!= GET_VBATT_COMMAND
								&& mOperationUnderway){
							sendProgressReport(new BluetoothProgressReportPerCmd(mCurrentCommand, getListofInstructions().size(), mMyBluetoothAddress, getComPort()));
						}
						
						// Process if currentCommand is a SET command
						if(isKnownSetCommand(mCurrentCommand)){
							stopTimerCheckForAckOrResp(); //cancel the ack timer
							
							processAckFromSetCommand(mCurrentCommand);
							
							mTransactionCompleted = true;
							setInstructionStackLock(false);
						}
						
						// Process if currentCommand is a GET command
						else if(isKnownGetCommand(mCurrentCommand)){
							//Special cases
							processSpecialGetCmdsAfterAck(mCurrentCommand);
							mWaitForResponse=true;
							getListofInstructions().remove(0);
						}
						
					}
				}
			}
		}
		
		/** Process RESPONSE while not streaming */ 
		private void processNotStreamingWaitForResp() {
			//Discard first read
			if(mFirstTime){
//				printLogDataForDebugging("First Time read");
//				clearSerialBuffer();
				
				while (availableBytes()!=0){
					int available = availableBytes();
					if (bytesAvailableToBeRead()){
						byteBuffer=readBytes(1);
						String msg = "First Time : " + Arrays.toString(byteBuffer);
						printLogDataForDebugging(msg);
					}
				}
				
				//TODO: Check with JC on the below!!! Or just clear seriable buffer and remove need for mFirstTime
				//Below added from original implementation -> doesn't wait for timeout on first command
				//TODO: if keeping the below, remove "mFirstTime = false" from the TimerCheckForAckOrResp
				stopTimerCheckForAckOrResp(); //cancel the ack timer
				mWaitForResponse=false;
				mTransactionCompleted=true;
				setInstructionStackLock(false);
				mFirstTime = false;
			} 
			
			else if(bytesAvailableToBeRead()){
				byteBuffer=readBytes(1);
				mIamAlive = true;
				
				//Check to see whether it is a response byte
				if(isKnownResponse(byteBuffer[0])){
					byte responseCommand = byteBuffer[0];
					
					processResponseCommand(responseCommand);
					//JD: only stop timer after process because there are readbyte opeartions in the processresponsecommand
					stopTimerCheckForAckOrResp(); //cancel the ack timer
					mWaitForResponse=false;
					mTransactionCompleted=true;
					setInstructionStackLock(false);
					printLogDataForDebugging("Response Received:\t\t\t" + btCommandToString(responseCommand));
					
					// Special case for FW_VERSION_RESPONSE because it
					// needs to initialize the Shimmer after releasing
					// the setInstructionStackLock
					if(byteBuffer[0]==FW_VERSION_RESPONSE){
						processFirmwareVerResponse();
					}
				}
			}
		}

		/** Process LogAndStream INSTREAM_CMD_RESPONSE while not streaming */ 
		private void processBytesAvailableAndInstreamSupported() {
			if(getFirmwareIdentifier()==FW_ID.LOGANDSTREAM
					&& !mWaitForAck 
					&& !mWaitForResponse 
					&& bytesAvailableToBeRead()) {
				
				byteBuffer=readBytes(1);
				if(byteBuffer != null){
					if(byteBuffer[0]==ACK_COMMAND_PROCESSED) {
						printLogDataForDebugging("ACK RECEIVED , Connected State!!");
						byteBuffer = readBytes(1);
						if (byteBuffer!=null){ //an android fix.. not fully investigated (JC)
							if(byteBuffer[0]==ACK_COMMAND_PROCESSED){
								byteBuffer = readBytes(1);
							}
							if(byteBuffer[0]==INSTREAM_CMD_RESPONSE){
								processResponseCommand(INSTREAM_CMD_RESPONSE);
							}
						}
					}
				}
				
				clearSerialBuffer();
			}
		}

	}
	
	private boolean isKnownResponse(byte response) {
		return mBtResponseMap.containsKey(response);
	}

	private boolean isKnownGetCommand(byte getCmd) {
		return mBtGetCommandMap.containsKey(getCmd);
	}

	private boolean isKnownSetCommand(byte setCmd) {
		return mBtSetCommandMap.containsKey(setCmd);
	}
	
	private void processSpecialGetCmdsAfterAck(byte currentCommand) {
		byte[] insBytes = getListofInstructions().get(0);

		if(currentCommand==GET_EXG_REGS_COMMAND){
			// Need to store ExG chip number before receiving response
			mTempChipID = insBytes[1];
		}
		else if(currentCommand==GET_INFOMEM_COMMAND || currentCommand==GET_CALIB_DUMP_COMMAND){
			// store current address/InfoMem segment
			mCurrentMemAddress = ((insBytes[3]&0xFF)<<8)+(insBytes[2]&0xFF);
			mCurrentMemLengthToRead = (insBytes[1]&0xFF);
		}
	}
	
	/** process responses to in-stream response */
	private void processInstreamResponse() {
		byte inStreamResponseCommand = readBytes(1)[0];
		consolePrintLn("In-stream received = " + btCommandToString(inStreamResponseCommand));

		if(inStreamResponseCommand==DIR_RESPONSE){ 
			byte[] responseData = readBytes(1);
			int directoryNameLength = responseData[0];
			byte[] bufferDirectoryName = new byte[directoryNameLength];
			bufferDirectoryName = readBytes(directoryNameLength);
			String tempDirectory = new String(bufferDirectoryName);
			mDirectoryName = tempDirectory;
			printLogDataForDebugging("Directory Name = " + mDirectoryName);
		}
		else if(inStreamResponseCommand==STATUS_RESPONSE){
			byte[] responseData = readBytes(1);
			parseStatusByte(responseData[0]);

			if(!mIsSensing){
				if(!isInitialised()){
					writeRealTimeClock();
				}
			}
			eventLogAndStreamStatusChanged(mCurrentCommand);
		} 
		else if(inStreamResponseCommand==VBATT_RESPONSE) {
			byte[] responseData = readBytes(3); 
			ShimmerBattStatusDetails battStatusDetails = new ShimmerBattStatusDetails(((responseData[1]&0xFF)<<8)+(responseData[0]&0xFF),responseData[2]);
			setBattStatusDetails(battStatusDetails);
			consolePrintLn("Batt data " + getBattVoltage());
		}
	}

	private void processFirmwareVerResponse() {
		if(getHardwareVersion()==HW_ID.SHIMMER_2R){
			initializeShimmer2R();
		} 
		else if(getHardwareVersion()==HW_ID.SHIMMER_3) {
			initializeShimmer3();
		}
		
		startTimerCheckIfAlive();
	}

	
	/**
	 * @param packet
	 * @param fwType
	 * @param timeSync
	 * @param pcTimeStamp
	 */
	private void buildAndSendMsg(byte[] packet, COMMUNICATION_TYPE fwType, boolean timeSync, long pcTimeStamp){
//	private void buildAndSendMsg(byte[] packet, int fwType, boolean timeSync, long pcTimeStamp){
		ObjectCluster objectCluster = null;
		try {
			objectCluster = buildMsg(packet, fwType, timeSync, pcTimeStamp);
			if(mFirstPacketParsed) {
				mFirstPacketParsed=false;
//				FormatCluster f = ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(Shimmer3.ObjectClusterSensorName.TIMESTAMP), CHANNEL_TYPE.CAL.toString());
				byte[] bSystemTS = objectCluster.mSystemTimeStamp;
				ByteBuffer bb = ByteBuffer.allocate(8);
		    	bb.put(bSystemTS);
		    	bb.flip();
		    	long systemTimeStamp = bb.getLong();
				mOffsetFirstTime = systemTimeStamp-objectCluster.getShimmerCalibratedTimeStamp();
			}
			
			double calTimestamp = objectCluster.getShimmerCalibratedTimeStamp();
			double systemTimestampPlot = calTimestamp+mOffsetFirstTime;
			objectCluster.addData(Shimmer3.ObjectClusterSensorName.SYSTEM_TIMESTAMP_PLOT,CHANNEL_TYPE.CAL.toString(), CHANNEL_UNITS.MILLISECONDS, systemTimestampPlot);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		dataHandler(objectCluster);
	}

	/**this is to clear the buffer
	 * 
	 */
	private void clearSerialBuffer() {
		/* JC: not working well on android
		if(bytesAvailableToBeRead()){
			byte[] buffer = readBytes(availableBytes());
			printLogDataForDebugging("Discarding:\t\t" + UtilShimmer.bytesToHexStringWithSpacesFormatted(buffer));
		}
		*/
		while (availableBytes()!=0){
			int available = availableBytes();
			if (bytesAvailableToBeRead()){
				byte[] tb=readBytes(1);
				String msg = "First Time : " + Arrays.toString(tb);
				printLogDataForDebugging(msg);
			}
		}		  		
	}
	
	/**
	 * @param bufferTemp
	 */
	public void processDataPacket(byte[] bufferTemp){
		//Create newPacket buffer
		byte[] newPacket = new byte[mPacketSize];
		//Skip the first byte as it is the identifier DATA_PACKET
		System.arraycopy(bufferTemp, 1, newPacket, 0, mPacketSize);
		
		if(mUseProcessingThread){
			mABQPacketByeArray.add(new RawBytePacketWithPCTimeStamp(newPacket,mListofPCTimeStamps.get(0)));
		} 
		else {
//			buildAndSendMsg(newPacket, FW_TYPE_BT, false, mListofPCTimeStamps.get(0));
			buildAndSendMsg(newPacket, COMMUNICATION_TYPE.BLUETOOTH, false, mListofPCTimeStamps.get(0));
		}
	}
	
	/**
	 * Clear the parsed packet from the mByteArrayOutputStream, NOTE the
	 * last two bytes(seq number of next packet) are added back on after the
	 * reset
	 * 
	 * @param bufferTemp
	 * @param packetSize
	 */
	private void clearSingleDataPacketFromBuffers(byte[] bufferTemp, int packetSize) {
		mByteArrayOutputStream.reset();
		mByteArrayOutputStream.write(bufferTemp[packetSize]);
//		consolePrintLn(Integer.toString(bufferTemp[mPacketSize+2]));
		for (int i=0;i<packetSize;i++){
			mListofPCTimeStamps.remove(0);
		}
	}

	/**
	 * 
	 */
	private void clearBuffers() {
		mByteArrayOutputStream.reset();
		mListofPCTimeStamps.clear();
	}
	
	/**
	 * 
	 */
	private void discardFirstBufferByte(){
		byte[] bTemp = mByteArrayOutputStream.toByteArray();
		mByteArrayOutputStream.reset();
		mByteArrayOutputStream.write(bTemp, 1, bTemp.length-1); //this will throw the first byte away
		mListofPCTimeStamps.remove(0);
		consolePrintLn("Throw Byte" + UtilShimmer.byteToHexStringFormatted(bTemp[0]));
	}
	
	/**
	 * @param responseCommand
	 */
	private void processResponseCommand(byte responseCommand) {
		if(responseCommand==INQUIRY_RESPONSE) {
			delayForBtResponse(500); // Wait to ensure the packet has been fully received
			List<Byte> buffer = new  ArrayList<Byte>();
			//JC TEST:: IMPORTANT TO REMOVE // This is to simulate packet loss
			/*
			if (Math.random()>0.5 && mIsInitialised==true){
				if(bytesAvailableToBeRead() 
						&& mCurrentCommand!=TEST_CONNECTION_COMMAND	
						&& mCurrentCommand!=GET_STATUS_COMMAND	
						&& mCurrentCommand!= GET_VBATT_COMMAND 
						&& mCurrentCommand!=START_STREAMING_COMMAND
						&& mCurrentCommand!=STOP_STREAMING_COMMAND 
						&& mCurrentCommand!=SET_RWC_COMMAND 
						&& mCurrentCommand!=GET_RWC_COMMAND){
					readByte();
				}
			}
			*/
			//JC TEST:: IMPORTANT TO REMOVE // This is to simulate packet loss
			
			//Shimmer3
			int lengthSettings = 8;// get Sampling rate, accel range, config setup byte0, num chans and buffer size
			int lengthChannels = 6;// read each channel type for the num channels
			if(!(getHardwareVersion()==HW_ID.SHIMMER_3)) {
				lengthSettings = 5;
				lengthChannels = 3;
			}
        	// get Sampling rate, accel range, config setup byte0, num chans and buffer size
			for (int i = 0; i < lengthSettings; i++) {
                buffer.add(readByte());
            }
            // read each channel type for the num channels
            for (int i = 0; i < (int)buffer.get(lengthChannels); i++) {
            	buffer.add(readByte());
            }

			byte[] bufferInquiry = new byte[buffer.size()];
			for (int i = 0; i < bufferInquiry.length; i++) {
				bufferInquiry[i] = (byte) buffer.get(i);
			}
				
			printLogDataForDebugging("Inquiry Response Received: " + UtilShimmer.bytesToHexStringWithSpacesFormatted(bufferInquiry));
			
			interpretInqResponse(bufferInquiry);
			prepareAllAfterConfigRead();
			inquiryDone();
		} 

		else if(responseCommand==SAMPLING_RATE_RESPONSE) {
			if(!mIsStreaming) {
				if(getHardwareVersion()==HW_ID.SHIMMER_2R || getHardwareVersion()==HW_ID.SHIMMER_2){    
					byte[] bufferSR = readBytes(1);
					if(mCurrentCommand==GET_SAMPLING_RATE_COMMAND) { // this is a double check, not necessary 
						double val=(double)(bufferSR[0] & (byte) ACK_COMMAND_PROCESSED);
						setSamplingRateShimmer(1024/val);
					}
				} 
				else if(getHardwareVersion()==HW_ID.SHIMMER_3){
					byte[] bufferSR = readBytes(2); //read the sampling rate
					setSamplingRateShimmer(32768/(double)((int)(bufferSR[0] & 0xFF) + ((int)(bufferSR[1] & 0xFF) << 8)));
				}
			}

			printLogDataForDebugging("Sampling Rate Response Received: " + Double.toString(getSamplingRateShimmer()));
		} 
		else if(responseCommand==FW_VERSION_RESPONSE){
			delayForBtResponse(200); // Wait to ensure the packet has been fully received
			byte[] bufferInquiry = new byte[6]; 
			bufferInquiry = readBytes(6);
			int firmwareIdentifier=(int)((bufferInquiry[1]&0xFF)<<8)+(int)(bufferInquiry[0]&0xFF);
			int firmwareVersionMajor = (int)((bufferInquiry[3]&0xFF)<<8)+(int)(bufferInquiry[2]&0xFF);
			int firmwareVersionMinor = ((int)((bufferInquiry[4]&0xFF)));
			int firmwareVersionInternal=(int)(bufferInquiry[5]&0xFF);
			ShimmerVerObject shimmerVerObject = new ShimmerVerObject(getHardwareVersion(), firmwareIdentifier, firmwareVersionMajor, firmwareVersionMinor, firmwareVersionInternal);
			setShimmerVersionInfoAndCreateSensorMap(shimmerVerObject);

			printLogDataForDebugging("FW Version Response Received. FW Code: " + getFirmwareVersionCode());
			printLogDataForDebugging("FW Version Response Received: " + getFirmwareVersionParsed());
		} 

		else if(responseCommand==ALL_CALIBRATION_RESPONSE) {
			if(getHardwareVersion()==HW_ID.SHIMMER_3){
				processAccelCalReadBytes();
				processGyroCalReadBytes();
				processMagCalReadBytes();
				processLsm303dlhcAccelCalReadBytes();
			} 
			else { //Shimmer2R etc.
				processAccelCalReadBytes();
				processGyroCalReadBytes();
				processMagCalReadBytes();
				processShimmer2EmgCalReadBytes();
				processShimmer2EcgCalReadBytes();
			}
		} 
		else if(responseCommand==ACCEL_CALIBRATION_RESPONSE) {
			processAccelCalReadBytes();
		}  
		else if(responseCommand==GYRO_CALIBRATION_RESPONSE) {
			processGyroCalReadBytes();
		} 
		else if(responseCommand==MAG_CALIBRATION_RESPONSE) {
			processMagCalReadBytes();
		} 
		else if(responseCommand==ECG_CALIBRATION_RESPONSE){
			processShimmer2EcgCalReadBytes();
		} 
		else if(responseCommand==EMG_CALIBRATION_RESPONSE){
			processShimmer2EmgCalReadBytes();
		}
		else if(responseCommand==LSM303DLHC_ACCEL_CALIBRATION_RESPONSE) {
			processLsm303dlhcAccelCalReadBytes();
		}  
		else if(responseCommand==CONFIG_BYTE0_RESPONSE) {
			if(getHardwareVersion()==HW_ID.SHIMMER_2R || getHardwareVersion()==HW_ID.SHIMMER_2){    
				byte[] bufferConfigByte0 = readBytes(1);
				mConfigByte0 = bufferConfigByte0[0] & 0xFF;
			} 
			else {
				byte[] bufferConfigByte0 = readBytes(4);
				mConfigByte0 = ((long)(bufferConfigByte0[0] & 0xFF) +((long)(bufferConfigByte0[1] & 0xFF) << 8)+((long)(bufferConfigByte0[2] & 0xFF) << 16) +((long)(bufferConfigByte0[3] & 0xFF) << 24));
			}
		} 
		else if(responseCommand==DERIVED_CHANNEL_BYTES_RESPONSE) {
			byte[] byteArray = readBytes(3);
			mDerivedSensors=(long)(((byteArray[2]&0xFF)<<16) + ((byteArray[1]&0xFF)<<8)+(byteArray[0]&0xFF));
			if (mEnabledSensors!=0){
				prepareAllAfterConfigRead();
				inquiryDone();
			}
		}
		else if(responseCommand==GET_SHIMMER_VERSION_RESPONSE) {
			delayForBtResponse(100); // Wait to ensure the packet has been fully received
			byte[] bufferShimmerVersion = new byte[1]; 
			bufferShimmerVersion = readBytes(1);
			setHardwareVersion((int)bufferShimmerVersion[0]);
			
//			if(mShimmerVersion==HW_ID.SHIMMER_2R){
//				initializeShimmer2R();
//			} 
//			else if(mShimmerVersion==HW_ID.SHIMMER_3) {
//				initializeShimmer3();
//			}
			
			printLogDataForDebugging("Shimmer Version (HW) Response Received: " + UtilShimmer.bytesToHexStringWithSpacesFormatted(bufferShimmerVersion));
			
			readFWVersion();
		} 							
		else if(responseCommand==ACCEL_SENSITIVITY_RESPONSE) {
			byte[] bufferAccelSensitivity = readBytes(1);
			setAccelRange(bufferAccelSensitivity[0]);
//			if(mDefaultCalibrationParametersAccel){
//				if(getHardwareVersion()!=HW_ID.SHIMMER_3){
//					if(getAccelRange()==0){
//						mSensitivityMatrixAnalogAccel = SensitivityMatrixAccel1p5gShimmer2; 
//					} 
//					else if(getAccelRange()==1){
//						mSensitivityMatrixAnalogAccel = SensitivityMatrixAccel2gShimmer2; 
//					} 
//					else if(getAccelRange()==2){
//						mSensitivityMatrixAnalogAccel = SensitivityMatrixAccel4gShimmer2; 
//					} 
//					else if(getAccelRange()==3){
//						mSensitivityMatrixAnalogAccel = SensitivityMatrixAccel6gShimmer2; 
//					}
//				} 
//				else if(getHardwareVersion()==HW_ID.SHIMMER_3){
//					if(getAccelRange()==0){
//						mSensitivityMatrixAnalogAccel = SensitivityMatrixLowNoiseAccel2gShimmer3;
//						mAlignmentMatrixAnalogAccel = AlignmentMatrixLowNoiseAccelShimmer3;
//						mOffsetVectorAnalogAccel = OffsetVectorLowNoiseAccelShimmer3;
//					} 
//					else if(getAccelRange()==1){
//						mSensitivityMatrixAnalogAccel = SensitivityMatrixWideRangeAccel4gShimmer3;
//						mAlignmentMatrixAnalogAccel = AlignmentMatrixWideRangeAccelShimmer3;
//						mOffsetVectorAnalogAccel = OffsetVectorWideRangeAccelShimmer3;
//					} 
//					else if(getAccelRange()==2){
//						mSensitivityMatrixAnalogAccel = SensitivityMatrixWideRangeAccel8gShimmer3;
//						mAlignmentMatrixAnalogAccel = AlignmentMatrixWideRangeAccelShimmer3;
//						mOffsetVectorAnalogAccel = OffsetVectorWideRangeAccelShimmer3;
//					} 
//					else if(getAccelRange()==3){
//						mSensitivityMatrixAnalogAccel = SensitivityMatrixWideRangeAccel16gShimmer3;
//						mAlignmentMatrixAnalogAccel = AlignmentMatrixWideRangeAccelShimmer3;
//						mOffsetVectorAnalogAccel = OffsetVectorWideRangeAccelShimmer3;
//					}
//				}
//			}
		} 
		else if(responseCommand==MPU9150_GYRO_RANGE_RESPONSE) {
			byte[] bufferGyroSensitivity = readBytes(1);
			setGyroRange(bufferGyroSensitivity[0]);
//			if(mDefaultCalibrationParametersGyro){
//				if(getHardwareVersion()==HW_ID.SHIMMER_3){
//					mAlignmentMatrixGyroscope = AlignmentMatrixGyroShimmer3;
//					mOffsetVectorGyroscope = OffsetVectorGyroShimmer3;
//					if(getGyroRange()==0){
//						mSensitivityMatrixGyroscope = SensitivityMatrixGyro250dpsShimmer3;
//					} 
//					else if(getGyroRange()==1){
//						mSensitivityMatrixGyroscope = SensitivityMatrixGyro500dpsShimmer3;
//					} 
//					else if(getGyroRange()==2){
//						mSensitivityMatrixGyroscope = SensitivityMatrixGyro1000dpsShimmer3;
//					} 
//					else if(getGyroRange()==3){
//						mSensitivityMatrixGyroscope = SensitivityMatrixGyro2000dpsShimmer3;
//					}
//				}
//			}
		}
		else if(responseCommand==GSR_RANGE_RESPONSE) {
			byte[] bufferGSRRange = readBytes(1); 
			mGSRRange=bufferGSRRange[0];
			
			printLogDataForDebugging("GSR Range Response Received: " + UtilShimmer.bytesToHexStringWithSpacesFormatted(bufferGSRRange));
		} 
		else if(responseCommand==BLINK_LED_RESPONSE) {
			byte[] byteled = readBytes(1);
			mCurrentLEDStatus = byteled[0]&0xFF;
		} 
		else if(responseCommand==BUFFER_SIZE_RESPONSE) {
			byte[] byteled = readBytes(1);
			mBufferSize = byteled[0] & 0xFF;
		} 
		else if(responseCommand==MAG_GAIN_RESPONSE) {
			byte[] bufferAns = readBytes(1); 
			setMagRange(bufferAns[0]);
		} 
		else if(responseCommand==MAG_SAMPLING_RATE_RESPONSE) {
			byte[] bufferAns = readBytes(1); 
			mLSM303MagRate=bufferAns[0];
			
			printLogDataForDebugging("Mag Sampling Rate Response Received: " + UtilShimmer.bytesToHexStringWithSpacesFormatted(bufferAns));
		} 
		else if(responseCommand==ACCEL_SAMPLING_RATE_RESPONSE) {
			byte[] bufferAns = readBytes(1); 
			mLSM303DigitalAccelRate=bufferAns[0];
		}
		else if(responseCommand==BMP180_CALIBRATION_COEFFICIENTS_RESPONSE){
			//get pressure
			delayForBtResponse(100); // Wait to ensure the packet has been fully received
			byte[] pressureResoRes = new byte[22]; 
			pressureResoRes = readBytes(22);
			mPressureCalRawParams = new byte[23];
			System.arraycopy(pressureResoRes, 0, mPressureCalRawParams, 1, 22);
			mPressureCalRawParams[0] = responseCommand;
			retrievePressureCalibrationParametersFromPacket(pressureResoRes,CALIB_READ_SOURCE.LEGACY_BT_COMMAND);
		} 
		else if(responseCommand==EXG_REGS_RESPONSE){
			delayForBtResponse(300); // Wait to ensure the packet has been fully received
			byte[] bufferAns = readBytes(11);
			if(mTempChipID==EXG_CHIP_INDEX.CHIP1.ordinal()){
				byte[] EXG1RegisterArray = new byte[10];
				System.arraycopy(bufferAns, 1, EXG1RegisterArray, 0, 10);
				setEXG1RegisterArray(EXG1RegisterArray);
			} 
			else if(mTempChipID==EXG_CHIP_INDEX.CHIP2.ordinal()){
				byte[] EXG2RegisterArray = new byte[10];
				System.arraycopy(bufferAns, 1, EXG2RegisterArray, 0, 10);
				setEXG2RegisterArray(EXG2RegisterArray);
			}
		} 
		else if(responseCommand==DAUGHTER_CARD_ID_RESPONSE) {
			byte[] expBoardArray = readBytes(numBytesToReadFromExpBoard+1);
//			getExpBoardID();//CHANGED TO NEWER UP-TO-DATE method
			byte[] expBoardArraySplit = Arrays.copyOfRange(expBoardArray, 1, 4);
			setExpansionBoardDetailsAndCreateSensorMap(new ExpansionBoardDetails(expBoardArraySplit));
		}
		else if(responseCommand==BAUD_RATE_RESPONSE) {
			byte[] bufferBaud = readBytes(1);
			mBluetoothBaudRate=bufferBaud[0] & 0xFF;
		}
		else if(responseCommand==TRIAL_CONFIG_RESPONSE) {
			byte[] data = readBytes(3);
			fillTrialShimmer3(data);
		}
		else if(responseCommand==CENTER_RESPONSE) {
			byte[] length = readBytes(1);
			byte[] data = readBytes(length[0]);
			String center = new String(data);
			setCenter(center);
		}
		else if(responseCommand==SHIMMERNAME_RESPONSE) {
			byte[] length = readBytes(1);
			byte[] data = readBytes(length[0]);
			String name = new String(data);
			setShimmerUserAssignedName(name);
		}
		else if(responseCommand==EXPID_RESPONSE) {
			byte[] length = readBytes(1);
			byte[] data = readBytes(length[0]);
			String name = new String(data);
			setTrialName(name);
		}
		else if(responseCommand==CONFIGTIME_RESPONSE) {
			byte[] length = readBytes(1);
			byte[] data = readBytes(length[0]);
			String time = new String(data);
			if(time.isEmpty()){
				setConfigTime(0);
			} 
			else {
				setConfigTime(Long.parseLong(time));	
			}
		}
		else if(responseCommand==RWC_RESPONSE) {
			byte[] rxBuf = readBytes(8);
			
			// Parse response string
			rxBuf = Arrays.copyOf(rxBuf, 8);
			ArrayUtils.reverse(rxBuf);
			long responseTime = (long)(((double)(ByteBuffer.wrap(rxBuf).getLong())/32.768)); // / 1000
			
			setLastReadRealTimeClockValue(responseTime);
		}
		else if(responseCommand==INSTREAM_CMD_RESPONSE) {
			processInstreamResponse();
		}
		else if(responseCommand==LSM303DLHC_ACCEL_LPMODE_RESPONSE) {
			byte[] responseData = readBytes(1);
			mLowPowerAccelWR = (((int)(responseData[0]&0xFF))>=1? true:false);
		} 
		else if(responseCommand==LSM303DLHC_ACCEL_HRMODE_RESPONSE) {
			byte[] responseData = readBytes(1);
			mHighResAccelWR = (((int)(responseData[0]&0xFF))>=1? true:false);
		} 
		else if(responseCommand==MYID_RESPONSE) {
			byte[] responseData = readBytes(1);
			mTrialId = (int)(responseData[0]&0xFF);
		}
		else if(responseCommand==NSHIMMER_RESPONSE) {
			byte[] responseData = readBytes(1);
			mTrialNumberOfShimmers = (int)(responseData[0]&0xFF);
		}
		else if(responseCommand==MPU9150_SAMPLING_RATE_RESPONSE) {
			byte[] responseData = readBytes(1);
			setMPU9150MPLSamplingRate(((int)(responseData[0]&0xFF)));
		}
		else if(responseCommand==BMP180_PRES_RESOLUTION_RESPONSE) {
			byte[] responseData = readBytes(1);
			setPressureResolution((int)(responseData[0]&0xFF));
		}
		else if(responseCommand==BMP180_PRES_CALIBRATION_RESPONSE) { 
			//TODO: Not used
		}
		else if(responseCommand==MPU9150_MAG_SENS_ADJ_VALS_RESPONSE) {
			//TODO: Not used
		}
		else if(responseCommand==INTERNAL_EXP_POWER_ENABLE_RESPONSE) {
			byte[] responseData = readBytes(1);
			setInternalExpPower((int)(responseData[0]&0xFF));
		}
		else if(responseCommand==INFOMEM_RESPONSE) {
			// Get data length to read
			byte[] rxBuf = readBytes(1);
			int lengthToRead = (int)(rxBuf[0]&0xFF);
			rxBuf = readBytes(lengthToRead);
			printLogDataForDebugging("INFOMEM_RESPONSE Received: " + UtilShimmer.bytesToHexStringWithSpacesFormatted(rxBuf));
			
			//Copy to local buffer
//			System.arraycopy(rxBuf, 0, mMemBuffer, mCurrentMemAddress, lengthToRead);
			mMemBuffer = ArrayUtils.addAll(mMemBuffer, rxBuf);
			
			//Update configuration when all bytes received.
			if((mCurrentMemAddress+mCurrentMemLengthToRead)==mInfoMemLayout.calculateInfoMemByteLength()){
				setShimmerInfoMemBytes(mMemBuffer);
				mMemBuffer = new byte[]{};
			}
		}
		else if(responseCommand==RSP_CALIB_DUMP_COMMAND) {
			byte[] rxBuf = readBytes(3);
			int currentMemLength = rxBuf[0]&0xFF;
			//Memory is currently read sequentially so no need to use the below at the moment.
			int currentMemOffset = ((rxBuf[2]&0xFF)<<8) | (rxBuf[1]&0xFF);
			
			//For debugging
			byte[] rxBufFull = rxBuf;
			
			rxBuf = readBytes(currentMemLength);
			mCalibDumpBuffer = ArrayUtils.addAll(mCalibDumpBuffer, rxBuf);

			//For debugging
			rxBufFull = ArrayUtils.addAll(rxBufFull, rxBuf);
			printLogDataForDebugging("CALIB_DUMP Received: " + UtilShimmer.bytesToHexStringWithSpacesFormatted(rxBufFull));
			
			if(mCurrentMemAddress==0){
				//First read
				mCalibDumpSize = (rxBuf[1]&0xFF)<<8 | (rxBuf[0]&0xFF);
				
				if(mCalibDumpSize>mCurrentMemLengthToRead){
					readCalibrationDump(mCurrentMemLengthToRead, mCalibDumpSize-mCurrentMemLengthToRead);
					rePioritiseReadCalibDumpInstructions();
				}
			}
			
			if((mCurrentMemAddress+mCurrentMemLengthToRead)>=mCalibDumpSize){
				parseCalibByteDump(mCalibDumpBuffer, CALIB_READ_SOURCE.RADIO_DUMP);
				mCalibDumpBuffer = new byte[]{};
			}
			
		}
		else {
			consolePrintLn("Unhandled BT response: " + responseCommand);
		}
	}
	
	// TODO: Consider removing this, replace by SET then GET and let the
	// RESPONSE update the variables in ShimmerObject - removes duplication of
	// code and ensure ShimmerObject it up-to-date with exactly what is on
	// Shimmer in case FW overwrites any settings
	/**
	 * @param currentCommand
	 */
	private void processAckFromSetCommand(byte currentCommand) {
		// check for null and size were put in because if Shimmer was abruptly
		// disconnected there is sometimes indexoutofboundsexceptions
		if(getListofInstructions().size() > 0){
			if(getListofInstructions().get(0)!=null){

				if(currentCommand==START_STREAMING_COMMAND || currentCommand==START_SDBT_COMMAND) {
					mIsStreaming=true;
					if(currentCommand==START_SDBT_COMMAND){
						mIsSDLogging = true;
						eventLogAndStreamStatusChanged(mCurrentCommand);
					}
					byteStack.clear();
					isNowStreaming();
				}
				else if((currentCommand==STOP_STREAMING_COMMAND)||(currentCommand==STOP_SDBT_COMMAND)){
					mIsStreaming=false;
					if(currentCommand==STOP_SDBT_COMMAND) {
						mIsSDLogging=false;
						eventLogAndStreamStatusChanged(mCurrentCommand);
					}
					
					byteStack.clear();

					clearSerialBuffer();
					
					hasStopStreaming();					
					getListofInstructions().removeAll(Collections.singleton(null));
				}
				else if(currentCommand==START_LOGGING_ONLY_COMMAND) {
					mIsSDLogging = true;
					eventLogAndStreamStatusChanged(mCurrentCommand);
				}
				else if(currentCommand==STOP_LOGGING_ONLY_COMMAND) {
					mIsSDLogging = false;
					eventLogAndStreamStatusChanged(mCurrentCommand);
				}

				else if(currentCommand==SET_SAMPLING_RATE_COMMAND) {
					byte[] instruction=getListofInstructions().get(0);
					double tempdouble=-1;
					if(getHardwareVersion()==HW_ID.SHIMMER_2 || getHardwareVersion()==HW_ID.SHIMMER_2R){
						tempdouble=(double)1024/instruction[1];
					} 
					else {
						System.err.println(((int)(instruction[1] & 0xFF) + ((int)(instruction[2] & 0xFF) << 8)));
						tempdouble = 32768/(double)((int)(instruction[1] & 0xFF) + ((int)(instruction[2] & 0xFF) << 8));
					}
					// TODO: MN Change to new method in ShimmerObject? It will
					// automatically update the individual IMU + ExG sensor rate
					// so you can then write the new values to the Shimmer3
					setSamplingRateShimmer(tempdouble);
					
					if(getHardwareVersion()==HW_ID.SHIMMER_3){ // has to be here because to ensure the current exgregister settings have been read back
						//check sampling rate and adjust accordingly;
						/*if(mShimmerSamplingRate<=128){
							writeEXGRateSetting(1,0);
							writeEXGRateSetting(2,0);
						} 
						else if(mShimmerSamplingRate<=256){
							writeEXGRateSetting(1,1);
							writeEXGRateSetting(2,1);
						}
						else if(mShimmerSamplingRate<=512){
							writeEXGRateSetting(1,2);
							writeEXGRateSetting(2,2);
						}*/
					}
				}
				else if(currentCommand==SET_BUFFER_SIZE_COMMAND) {
					mBufferSize=(int)((byte[])getListofInstructions().get(0))[1];
				}
				else if(currentCommand==SET_GYRO_TEMP_VREF_COMMAND) {
					mConfigByte0=mTempByteValue;
				}
				else if(currentCommand==SET_BLINK_LED) {
					if(((byte[])getListofInstructions().get(0)).length>2){
						mCurrentLEDStatus=(int)((byte[])getListofInstructions().get(0))[1];
					}
				}
				else if(currentCommand==TEST_CONNECTION_COMMAND) {
					//DO Nothing
					//System.err.println("TEST_CONNECTION_COMMAND RESPONSE RECEIVED");
				}
				else if(currentCommand==SET_GSR_RANGE_COMMAND) {
					mGSRRange=(int)((byte [])getListofInstructions().get(0))[1];
				}
				else if(currentCommand==SET_CONFIG_BYTE0_COMMAND) {
					mConfigByte0=(int)((byte [])getListofInstructions().get(0))[1];
				}
				else if(currentCommand==SET_PMUX_COMMAND) {
					if(((byte[])getListofInstructions().get(0))[1]==1) {
						mConfigByte0=(byte) ((byte) (mConfigByte0|64)&(0xFF)); 
					}
					else if(((byte[])getListofInstructions().get(0))[1]==0) {
						mConfigByte0=(byte) ((byte)(mConfigByte0 & 191)&(0xFF));
					}
				}
				else if(currentCommand==SET_BMP180_PRES_RESOLUTION_COMMAND){
					setPressureResolution((int)((byte [])getListofInstructions().get(0))[1]);
				}
				else if(currentCommand==SET_5V_REGULATOR_COMMAND) {
					if(((byte[])getListofInstructions().get(0))[1]==1) {
						mConfigByte0=(byte) (mConfigByte0|128); 
					}
					else if(((byte[])getListofInstructions().get(0))[1]==0) {
						mConfigByte0=(byte)(mConfigByte0 & 127);
					}
				}
				else if(currentCommand==SET_INTERNAL_EXP_POWER_ENABLE_COMMAND) {
					if(((byte[])getListofInstructions().get(0))[1]==1) {
						mConfigByte0 = (mConfigByte0|16777216); 
						mInternalExpPower = 1;
					}
					else if(((byte[])getListofInstructions().get(0))[1]==0) {
						mConfigByte0 = mConfigByte0 & 4278190079l;
						mInternalExpPower = 0;
					}
				}
				
				
				else if(currentCommand==SET_ACCEL_SENSITIVITY_COMMAND) {
					setAccelRange((int)(((byte[])getListofInstructions().get(0))[1]));
//					if(mDefaultCalibrationParametersAccel){
//						if(getHardwareVersion()!=HW_ID.SHIMMER_3){
//							if(getAccelRange()==0){
//								mSensitivityMatrixAnalogAccel = SensitivityMatrixAccel1p5gShimmer2; 
//							} 
//							else if(getAccelRange()==1){
//								mSensitivityMatrixAnalogAccel = SensitivityMatrixAccel2gShimmer2; 
//							}
//							else if(getAccelRange()==2){
//								mSensitivityMatrixAnalogAccel = SensitivityMatrixAccel4gShimmer2; 
//							} 
//							else if(getAccelRange()==3){
//								mSensitivityMatrixAnalogAccel = SensitivityMatrixAccel6gShimmer2; 
//							}
//						} 
//						else if(getHardwareVersion()==HW_ID.SHIMMER_3){
//							mSensitivityMatrixAnalogAccel = SensitivityMatrixLowNoiseAccel2gShimmer3;
//							mAlignmentMatrixAnalogAccel = AlignmentMatrixLowNoiseAccelShimmer3;
//							mOffsetVectorAnalogAccel = OffsetVectorLowNoiseAccelShimmer3;
//						}
//					}
//
//					if(mDefaultCalibrationParametersDigitalAccel){
//						if(getHardwareVersion()==HW_ID.SHIMMER_3){
//							if(getAccelRange()==1){
//								mSensitivityMatrixWRAccel = SensitivityMatrixWideRangeAccel4gShimmer3;
//								mAlignmentMatrixWRAccel = AlignmentMatrixWideRangeAccelShimmer3;
//								mOffsetVectorWRAccel = OffsetVectorWideRangeAccelShimmer3;
//							} 
//							else if(getAccelRange()==2){
//								mSensitivityMatrixWRAccel = SensitivityMatrixWideRangeAccel8gShimmer3;
//								mAlignmentMatrixWRAccel = AlignmentMatrixWideRangeAccelShimmer3;
//								mOffsetVectorWRAccel = OffsetVectorWideRangeAccelShimmer3;
//							} 
//							else if(getAccelRange()==3){
//								mSensitivityMatrixWRAccel = SensitivityMatrixWideRangeAccel16gShimmer3;
//								mAlignmentMatrixWRAccel = AlignmentMatrixWideRangeAccelShimmer3;
//								mOffsetVectorWRAccel = OffsetVectorWideRangeAccelShimmer3;
//							} 
//							else if(getAccelRange()==0){
//								mSensitivityMatrixWRAccel = SensitivityMatrixWideRangeAccel2gShimmer3;
//								mAlignmentMatrixWRAccel = AlignmentMatrixWideRangeAccelShimmer3;
//								mOffsetVectorWRAccel = OffsetVectorWideRangeAccelShimmer3;
//							}
//						}
//					}
				} 
				
				else if(currentCommand==SET_ACCEL_CALIBRATION_COMMAND) {
					byte[] calibBytes = Arrays.copyOfRange(getListofInstructions().get(0), 1, getListofInstructions().get(0).length);
					parseCalibParamFromPacketAccelAnalog(calibBytes, CALIB_READ_SOURCE.LEGACY_BT_COMMAND);
//					retrieveKinematicCalibrationParametersFromPacket(Arrays.copyOfRange(getListofInstructions().get(0), 1, getListofInstructions().get(0).length), ACCEL_CALIBRATION_RESPONSE);
				}
				else if(currentCommand==SET_GYRO_CALIBRATION_COMMAND) {
					byte[] calibBytes = Arrays.copyOfRange(getListofInstructions().get(0), 1, getListofInstructions().get(0).length);
					parseCalibParamFromPacketGyro(calibBytes, CALIB_READ_SOURCE.LEGACY_BT_COMMAND);
//					retrieveKinematicCalibrationParametersFromPacket(Arrays.copyOfRange(getListofInstructions().get(0), 1, getListofInstructions().get(0).length), GYRO_CALIBRATION_RESPONSE);
				}
				else if(currentCommand==SET_MAG_CALIBRATION_COMMAND) {
					byte[] calibBytes = Arrays.copyOfRange(getListofInstructions().get(0), 1, getListofInstructions().get(0).length);
					parseCalibParamFromPacketMag(calibBytes, CALIB_READ_SOURCE.LEGACY_BT_COMMAND);
//					retrieveKinematicCalibrationParametersFromPacket(Arrays.copyOfRange(getListofInstructions().get(0), 1, getListofInstructions().get(0).length), MAG_CALIBRATION_RESPONSE);
				}
				else if(currentCommand==SET_LSM303DLHC_ACCEL_CALIBRATION_COMMAND) {
					byte[] calibBytes = Arrays.copyOfRange(getListofInstructions().get(0), 1, getListofInstructions().get(0).length);
					parseCalibParamFromPacketAccelLsm(calibBytes, CALIB_READ_SOURCE.LEGACY_BT_COMMAND);
//					retrieveKinematicCalibrationParametersFromPacket(Arrays.copyOfRange(getListofInstructions().get(0), 1, getListofInstructions().get(0).length), LSM303DLHC_ACCEL_CALIBRATION_RESPONSE);
				}
				else if(currentCommand==SET_MPU9150_GYRO_RANGE_COMMAND) {
					setGyroRange((int)(((byte[])getListofInstructions().get(0))[1]));
//					if(mDefaultCalibrationParametersGyro){
//						if(getHardwareVersion()==HW_ID.SHIMMER_3){
//							mAlignmentMatrixGyroscope = AlignmentMatrixGyroShimmer3;
//							mOffsetVectorGyroscope = OffsetVectorGyroShimmer3;
//							if(getGyroRange()==0){
//								mSensitivityMatrixGyroscope = SensitivityMatrixGyro250dpsShimmer3;
//							} 
//							else if(getGyroRange()==1){
//								mSensitivityMatrixGyroscope = SensitivityMatrixGyro500dpsShimmer3;
//							} 
//							else if(getGyroRange()==2){
//								mSensitivityMatrixGyroscope = SensitivityMatrixGyro1000dpsShimmer3;
//							} 
//							else if(getGyroRange()==3){
//								mSensitivityMatrixGyroscope = SensitivityMatrixGyro2000dpsShimmer3;
//							}
//						}
//					}
				} 
				else if(currentCommand==SET_MAG_SAMPLING_RATE_COMMAND){
					mLSM303MagRate = mTempIntValue;
				}
				else if(currentCommand==SET_ACCEL_SAMPLING_RATE_COMMAND){
					mLSM303DigitalAccelRate = mTempIntValue;
				}
				else if(currentCommand==SET_MPU9150_SAMPLING_RATE_COMMAND){
					mMPU9150GyroAccelRate = mTempIntValue;
				}
				else if(currentCommand==SET_EXG_REGS_COMMAND){
					byte[] bytearray = getListofInstructions().get(0);
					if(bytearray[1]==EXG_CHIP_INDEX.CHIP1.ordinal()){  //0 = CHIP 1
						byte[] EXG1RegisterArray = new byte[10];
						System.arraycopy(bytearray, 4, EXG1RegisterArray, 0, 10);
						setEXG1RegisterArray(EXG1RegisterArray);
					} 
					else if(bytearray[1]==EXG_CHIP_INDEX.CHIP2.ordinal()){ //1 = CHIP 2
						byte[] EXG2RegisterArray = new byte[10];
						System.arraycopy(bytearray, 4, EXG2RegisterArray, 0, 10);
						setEXG2RegisterArray(EXG2RegisterArray);
					}
				} 
				else if(currentCommand==SET_SENSORS_COMMAND) {
					mEnabledSensors=tempEnabledSensors;
					if(getHardwareVersion()==HW_ID.SHIMMER_3){
						checkExgResolutionFromEnabledSensorsVar();
					}
					byteStack.clear(); // Always clear the packetStack after setting the sensors, this is to ensure a fresh start
				}
				else if(currentCommand==SET_MAG_GAIN_COMMAND){
					setMagRange((int)((byte [])getListofInstructions().get(0))[1]);
//					if(mDefaultCalibrationParametersMag){
//						if(getHardwareVersion()==HW_ID.SHIMMER_3){
//							mAlignmentMatrixMagnetometer = AlignmentMatrixMagShimmer3;
//							mOffsetVectorMagnetometer = OffsetVectorMagShimmer3;
//							if(getMagRange()==1){
//								mSensitivityMatrixMagnetometer = SensitivityMatrixMag1p3GaShimmer3;
//							} 
//							else if(getMagRange()==2){
//								mSensitivityMatrixMagnetometer = SensitivityMatrixMag1p9GaShimmer3;
//							} 
//							else if(getMagRange()==3){
//								mSensitivityMatrixMagnetometer = SensitivityMatrixMag2p5GaShimmer3;
//							} 
//							else if(getMagRange()==4){
//								mSensitivityMatrixMagnetometer = SensitivityMatrixMag4GaShimmer3;
//							} 
//							else if(getMagRange()==5){
//								mSensitivityMatrixMagnetometer = SensitivityMatrixMag4p7GaShimmer3;
//							} 
//							else if(getMagRange()==6){
//								mSensitivityMatrixMagnetometer = SensitivityMatrixMag5p6GaShimmer3;
//							} 
//							else if(getMagRange()==7){
//								mSensitivityMatrixMagnetometer = SensitivityMatrixMag8p1GaShimmer3;
//							}
//						}
//					}
				}
				else if(currentCommand==SET_ECG_CALIBRATION_COMMAND){
					byte[] instruction = getListofInstructions().get(0);
					//mGSRRange=mTempIntValue;
					mDefaultCalibrationParametersECG = false;
					OffsetECGLALL=(double)((instruction[0]&0xFF)<<8)+(instruction[1]&0xFF);
					GainECGLALL=(double)((instruction[2]&0xFF)<<8)+(instruction[3]&0xFF);
					OffsetECGRALL=(double)((instruction[4]&0xFF)<<8)+(instruction[5]&0xFF);
					GainECGRALL=(double)((instruction[6]&0xFF)<<8)+(instruction[7]&0xFF);
				}
				else if(currentCommand==SET_EMG_CALIBRATION_COMMAND){
					byte[] instruction = getListofInstructions().get(0);
					//mGSRRange=mTempIntValue;
					mDefaultCalibrationParametersEMG = false;
					OffsetEMG=(double)((instruction[0]&0xFF)<<8)+(instruction[1]&0xFF);
					GainEMG=(double)((instruction[2]&0xFF)<<8)+(instruction[3]&0xFF);
				}
				else if(currentCommand==SET_DERIVED_CHANNEL_BYTES){
					byte[] instruction = getListofInstructions().get(0);
					mDerivedSensors = (long)(((instruction[3]&0xFF)<<16) + ((instruction[2]&0xFF)<<8) + (instruction[1]&0xFF));
					if (mEnabledSensors!=0){
						prepareAllAfterConfigRead();
						inquiryDone();
					}
				}
				else if(currentCommand==SET_SHIMMERNAME_COMMAND){
					byte[] instruction =getListofInstructions().get(0);
					byte[] nameArray = new byte[instruction[1]];
					System.arraycopy(instruction, 2, nameArray, 0, instruction[1]);
					String name = new String(nameArray);
					setShimmerUserAssignedName(name);
				}
				else if(currentCommand==SET_EXPID_COMMAND){
					byte[] instruction =getListofInstructions().get(0);
					byte[] nameArray = new byte[instruction[1]];
					System.arraycopy(instruction, 2, nameArray, 0, instruction[1]);
					String name = new String(nameArray);
					setTrialName(name);
				}
				else if(currentCommand==SET_RWC_COMMAND){
					byte[] instruction = getListofInstructions().get(0);
					byte[] rwcTimeArray = new byte[8];
					System.arraycopy(instruction, 1, rwcTimeArray, 0, 8);
					long milliseconds = UtilShimmer.convertShimmerRtcDataBytesToMilliSecondsLSB(rwcTimeArray);
					mShimmerRealTimeClockConFigTime = milliseconds;
				}
				else if(currentCommand==SET_CONFIGTIME_COMMAND){
					byte[] instruction =getListofInstructions().get(0);
					byte[] timeArray = new byte[instruction[1]];
					System.arraycopy(instruction, 2, timeArray, 0, instruction[1]);
					String time = new String(timeArray);
					setConfigTime(Long.parseLong(time));
				}
				else if(currentCommand==SET_CENTER_COMMAND){
					byte[] instruction =getListofInstructions().get(0);
					byte[] centerArray = new byte[instruction[1]];
					System.arraycopy(instruction, 2, centerArray, 0, instruction[1]);
					String center = new String(centerArray);
					//setConfigTime(Long.parseLong(time));
					setCenter(center);
				}
				else if(currentCommand==SET_TRIAL_CONFIG_COMMAND){
					byte[] instruction =getListofInstructions().get(0);
					byte[] dataArray = new byte[3];
					System.arraycopy(instruction, 1, dataArray, 0, 3);
					
					fillTrialShimmer3(dataArray);
				}
				else if(currentCommand==SET_BAUD_RATE_COMMAND) {
					mBluetoothBaudRate=(int)((byte [])getListofInstructions().get(0))[1];
//								reconnect();
					//TODO: handle disconnect/reconnect here?
				}
				else if(currentCommand==TOGGLE_LED_COMMAND){
					// toggle command followed automatically by a GET so no need
					// to do anything here
				}
				else if(currentCommand==SET_LSM303DLHC_ACCEL_LPMODE_COMMAND) {
					mLowPowerAccelWR = ((int)((byte [])getListofInstructions().get(0))[1]>1? true:false);
				} 
				else if(currentCommand==SET_LSM303DLHC_ACCEL_HRMODE_COMMAND) {
					mHighResAccelWR = ((int)((byte [])getListofInstructions().get(0))[1]>1? true:false);
				}
				else if(currentCommand==SET_MYID_COMMAND){
					mTrialId = (int)((byte [])getListofInstructions().get(0))[1];
				}
				else if(currentCommand==SET_NSHIMMER_COMMAND){
					mTrialNumberOfShimmers = (int)((byte [])getListofInstructions().get(0))[1];
				}
				else if(currentCommand==RESET_TO_DEFAULT_CONFIGURATION_COMMAND){
					//TODO: do something?
				}
				else if(currentCommand==RESET_CALIBRATION_VALUE_COMMAND){
					//TODO: do something?
				}
				else if(currentCommand==SET_INFOMEM_COMMAND 
						|| currentCommand==SET_CALIB_DUMP_COMMAND){
					//SET InfoMem is automatically followed by a GET so no need to handle here
					
					//Sleep for Xsecs to allow Shimmer to process new configuration
					mNumOfMemSetCmds -= 1;
					if(mNumOfMemSetCmds==0){
						delayForBtResponse(DELAY_BETWEEN_INFOMEM_WRITES);
					}
					else {
						delayForBtResponse(DELAY_AFTER_INFOMEM_WRITE);
					}
				}
				else if(currentCommand==SET_CRC_COMMAND){
					mIsCrcEnabled = ((int)((byte [])getListofInstructions().get(0))[1]>1? true:false);
				}
				else {
					//unhandled set command
					printLogDataForDebugging("Unhandled set command: " + btCommandToString(currentCommand));
				}
				
				getListofInstructions().remove(0);
			}
			
		}
	}
	
	/**
	 * Due to the nature of the Bluetooth SPP stack a delay has been added to
	 * ensure the buffer is filled before it is read
	 * 
	 */
	private void delayForBtResponse(long millis){
		threadSleep(millis);
	}
	
	/**get accel
	 * 
	 */
	private void processAccelCalReadBytes(){
		delayForBtResponse(100); // Wait to ensure the packet has been fully received
		byte[] bufferCalibrationParameters = readBytes(21);
		mAccelCalRawParams = new byte[22];
		System.arraycopy(bufferCalibrationParameters, 0, mAccelCalRawParams, 1, 21);
		mAccelCalRawParams[0] = ACCEL_CALIBRATION_RESPONSE;
//		retrieveKinematicCalibrationParametersFromPacket(bufferCalibrationParameters, ACCEL_CALIBRATION_RESPONSE);
		parseCalibParamFromPacketAccelAnalog(bufferCalibrationParameters, CALIB_READ_SOURCE.LEGACY_BT_COMMAND);
	}

	/**get gyro
	 * 
	 */
	private void processGyroCalReadBytes(){
		delayForBtResponse(100); // Wait to ensure the packet has been fully received
		byte[] bufferCalibrationParameters = readBytes(21);
		mGyroCalRawParams = new byte[22];
		System.arraycopy(bufferCalibrationParameters, 0, mGyroCalRawParams, 1, 21);
		mGyroCalRawParams[0] = GYRO_CALIBRATION_RESPONSE;
//		retrieveKinematicCalibrationParametersFromPacket(bufferCalibrationParameters, GYRO_CALIBRATION_RESPONSE);
		parseCalibParamFromPacketGyro(bufferCalibrationParameters, CALIB_READ_SOURCE.LEGACY_BT_COMMAND);
	} 
	
	/**get mag
	 * 
	 */
	private void processMagCalReadBytes(){
		delayForBtResponse(100); // Wait to ensure the packet has been fully received
		byte[] bufferCalibrationParameters = readBytes(21);
		mMagCalRawParams = new byte[22];
		System.arraycopy(bufferCalibrationParameters, 0, mMagCalRawParams, 1, 21);
		mMagCalRawParams[0] = MAG_CALIBRATION_RESPONSE;
//		retrieveKinematicCalibrationParametersFromPacket(bufferCalibrationParameters, MAG_CALIBRATION_RESPONSE);
		parseCalibParamFromPacketMag(bufferCalibrationParameters, CALIB_READ_SOURCE.LEGACY_BT_COMMAND);
	} 
	
	/**second accel cal params
	 * 
	 */
	private void processLsm303dlhcAccelCalReadBytes(){
		delayForBtResponse(100); // Wait to ensure the packet has been fully received
		byte[] bufferCalibrationParameters = readBytes(21);
		mDigiAccelCalRawParams = new byte[22];
		System.arraycopy(bufferCalibrationParameters, 0, mDigiAccelCalRawParams, 1, 21);
		mDigiAccelCalRawParams[0] = LSM303DLHC_ACCEL_CALIBRATION_RESPONSE;
//		retrieveKinematicCalibrationParametersFromPacket(bufferCalibrationParameters, LSM303DLHC_ACCEL_CALIBRATION_RESPONSE);
		parseCalibParamFromPacketAccelLsm(bufferCalibrationParameters, CALIB_READ_SOURCE.LEGACY_BT_COMMAND);
	}
	
	/**get ECG
	 * 
	 */
	private void processShimmer2EcgCalReadBytes(){
		delayForBtResponse(100); // Wait to ensure the packet has been fully received
		byte[] bufferCalibrationParameters = readBytes(8);
		mECGCalRawParams = new byte[9];
		System.arraycopy(bufferCalibrationParameters, 0, mECGCalRawParams, 1, 8);
		mECGCalRawParams[0] = ECG_CALIBRATION_RESPONSE;
		retrieveBiophysicalCalibrationParametersFromPacket( bufferCalibrationParameters,ECG_CALIBRATION_RESPONSE);
	} 
	
	/**get EMG
	 * 
	 */
	private void processShimmer2EmgCalReadBytes(){
		delayForBtResponse(100); // Wait to ensure the packet has been fully received
		byte[] bufferCalibrationParameters = readBytes(4); 
		mEMGCalRawParams = new byte[5];
		System.arraycopy(bufferCalibrationParameters, 0, mEMGCalRawParams, 1, 4);
		mEMGCalRawParams[0] = EMG_CALIBRATION_RESPONSE;
		retrieveBiophysicalCalibrationParametersFromPacket( bufferCalibrationParameters,EMG_CALIBRATION_RESPONSE);
	}
	
	
	/**
	 * @param statusByte
	 */
	private void parseStatusByte(byte statusByte){
		Boolean savedDockedState = mIsDocked;
		
		setIsDocked(((statusByte & 0x01) > 0)? true:false);
		setIsSensing(((statusByte & 0x02) > 0)? true:false);
//		reserved = ((statusByte & 0x03) > 0)? true:false;
		setIsSDLogging(((statusByte & 0x08) > 0)? true:false);
		setIsStreaming(((statusByte & 0x10) > 0)? true:false); 

		consolePrintLn("Status Response = " + UtilShimmer.byteToHexStringFormatted(statusByte)
				+ "\t" + "IsDocked = " + mIsDocked
				+ "\t" + "IsSensing = " + mIsSensing
				+ "\t" + "IsSDLogging = "+ mIsSDLogging
				+ "\t" + "IsStreaming = " + mIsStreaming
				);
		
		if(savedDockedState!=mIsDocked){
			dockedStateChange();
		}
	}
	
	private byte[] convertStackToByteArray(Stack<Byte> b,int packetSize) {
		byte[] returnByte=new byte[packetSize];
		b.remove(0); //remove the Data Packet identifier 
		for (int i=0;i<packetSize;i++) {
			returnByte[packetSize-1-i]=(byte) b.pop();
		}
		return returnByte;
	}
	
	public String btCommandToString(byte command){
		
		Map<Byte, BtCommandDetails> mapToSearch = null;
		
		if(mBtCommandMapOther.containsKey(command)){
			mapToSearch = mBtCommandMapOther;
		}
		else if(isKnownSetCommand(command)){
			mapToSearch = mBtSetCommandMap;
		}
		else if(isKnownGetCommand(command)){
			mapToSearch = mBtGetCommandMap;
		}
		else if(isKnownResponse(command)){
			mapToSearch = mBtResponseMap;
		}
		
		if(mapToSearch!=null)
			return UtilShimmer.byteToHexStringFormatted(command) + " " + mapToSearch.get(command).mDescription;
		else {
			return UtilShimmer.byteToHexStringFormatted(command) + "UNKNOWN";
		}
	}
	
	//endregion --------- BLUETOOH STACK --------- 

	
	//region --------- INITIALIZE SHIMMER FUNCTIONS --------- 
	
	protected synchronized void initialize() {	    	//See two constructors for Shimmer
		//InstructionsThread instructionsThread = new InstructionsThread();
		//instructionsThread.start();
		clearShimmerVersionInfo();
		
		stopTimerReadStatus();
		stopTimerCheckForAckOrResp();
		setInstructionStackLock(false);
		
		dummyreadSamplingRate(); // it actually acts to clear the write buffer
		
		readShimmerVersionNew();
		//readFWVersion();
		//mShimmerVersion=4;
	}

	public void initializeBoilerPlate(){
		readSamplingRate();
		readConfigByte0();
		readCalibrationParameters("Accelerometer");
		readCalibrationParameters("Magnetometer");
		readCalibrationParameters("Gyroscope");
		if(mSetupDevice && getHardwareVersion()!=4){
			writeAccelRange(getAccelRange());
			writeGSRRange(mGSRRange);
			writeShimmerAndSensorsSamplingRate(getSamplingRateShimmer());	
			writeEnabledSensors(mSetEnabledSensors);
			setContinuousSync(mContinousSync);
		} 
		else {
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
		readLEDCommand();
		readConfigByte0();
		readCalibrationParameters("All");
		if(mSetupDevice){
			writeMagRange(getMagRange()); //set to default Shimmer mag gain
			writeAccelRange(getAccelRange());
			writeGSRRange(mGSRRange);
			writeShimmerAndSensorsSamplingRate(getSamplingRateShimmer());	
			writeEnabledSensors(mSetEnabledSensors);
			setContinuousSync(mContinousSync);
		} 
		else {
			if(isThisVerCompatibleWith(HW_ID.SHIMMER_2R, FW_ID.BOILER_PLATE, 0, 1, 0)){
			
			} 
			else {
				readMagRange();
			}
			inquiry();
		}
	}

	private void initializeShimmer3(){
		initialise(HW_ID.SHIMMER_3);
		mHaveAttemptedToReadConfig = true;
		
		if(mSendProgressReport){
			operationPrepare();
			setBluetoothRadioState(BT_STATE.CONNECTING);
		}
		
		if(this.mUseInfoMemConfigMethod && getFirmwareVersionCode()>=6){
			readConfigurationFromInfoMem();
			readPressureCalibrationCoefficients();
		}
		else {
			readSamplingRate();
			readMagRange();
			readAccelRange();
			readGyroRange();
			readAccelSamplingRate();
			readCalibrationParameters("All");
			readPressureCalibrationCoefficients();
			readEXGConfigurations();
			//enableLowPowerMag(mLowPowerMag);
			
			readDerivedChannelBytes();
			
			if(isThisVerCompatibleWith(HW_ID.SHIMMER_3, FW_ID.LOGANDSTREAM, 0, 5, 2)){
				readTrial();
				readConfigTime();
				readShimmerName();
				readExperimentName();
			}
		}
		
		readExpansionBoardID();
		readLEDCommand();

		if(isThisVerCompatibleWith(HW_ID.SHIMMER_3, FW_ID.LOGANDSTREAM, 0, 5, 2)){
			readStatusLogAndStream();
		}
		
		if(isThisVerCompatibleWith(HW_ID.SHIMMER_3, FW_ID.LOGANDSTREAM, 0, 5, 9)){
			readBattery();
		}
		
		// Only read calibration dump over bluetooth if the Shimmer is not
		// docked as the Shimmer won't have access to the SD card
		if(!isDocked()){
			readCalibrationDump();
		}
		
		if(mSetupDevice){
			//writeAccelRange(mDigitalAccelRange);
			if(mSetupEXG){
				writeEXGConfiguration();
				mSetupEXG = false;
			}
			writeGSRRange(mGSRRange);
			writeAccelRange(getAccelRange());
			writeGyroRange(getGyroRange());
			writeMagRange(getMagRange());
			writeShimmerAndSensorsSamplingRate(getSamplingRateShimmer());	
			writeInternalExpPower(1);
//			setContinuousSync(mContinousSync);
			writeEnabledSensors(mSetEnabledSensors); //this should always be the last command
		} 
		else {
			inquiry();
		}

		if(mSendProgressReport){
			// Just unlock instruction stack and leave logAndStream timer as
			// this is handled in the next step, i.e., no need for
			// operationStart() here
			startOperation(BT_STATE.CONNECTING, getListofInstructions().size());
			
			setInstructionStackLock(false);
		}
		
		startTimerReadStatus();	// if shimmer is using LogAndStream FW, read its status periodically
		startTimerReadBattStatus(); // if shimmer is using LogAndStream FW, read its status periodically
		
	}
	
	//endregion --------- INITIALIZE SHIMMER FUNCTIONS ---------
	
	
	//region --------- OPERATIONS --------- 
	/**
	 * 
	 */
	public void operationPrepare(){
		stopAllTimers();

		//make sure no more instructions
		//shouldnt matter since the configuration are being rewritten by consensys
		//used in initializeshimmer3 (this class) and BluetoothManager class
		getListofInstructions().clear();
		// wait for instruction stack to clear			
		while(getListofInstructions().size()>0); //TODO add timeout
		// lock the instruction stack
		setInstructionStackLock(true);
		mOperationUnderway = true; 
	}
	
	/**
	 * 
	 */
	public void operationWaitForFinish(){
		// unlock the instruction stack
		setInstructionStackLock(false);
		// wait for instruction stack to clear			
		while(getListofInstructions().size()>0); //TODO add timeout
	}
	
	/**
	 * @param btState
	 */
	public void operationStart(BT_STATE btState){
//		mOperationUnderway = true;
		startOperation(btState, getListofInstructions().size());
		//unlock instruction stack
		setInstructionStackLock(false);
	}
	
	/**
	 * 
	 */
	public void operationFinished(){
		/*
		startTimerCheckIfAlive();
		startTimerReadStatus();
		startTimerReadBattStatus();
		*/
		mOperationUnderway = false;
	}
	//endregion --------- OPERATIONS --------- 

	
	//region  --------- START/STOP STREAMING FUNCTIONS --------- 
	@Override
	public void startStreaming() {
		//mCurrentLEDStatus=-1;	
		
		initialiseStreaming();

		mByteArrayOutputStream.reset();
		mListofPCTimeStamps.clear();
		writeInstruction(START_STREAMING_COMMAND);
	}
	
	public void startDataLogAndStreaming(){
		if(getFirmwareIdentifier()==FW_ID.LOGANDSTREAM){ // if shimmer is using LogAndStream FW, stop reading its status perdiocally
			initialiseStreaming();
			
			//TODO: ask JC, should mByteArrayOutputStream.reset(); and mListofPCTimeStamps.clear(); be here as well? 
			writeInstruction(START_SDBT_COMMAND);
		}
	}
	
	private void initialiseStreaming(){
		stopTimerReadStatus(); // if shimmer is using LogAndStream FW, stop reading its status perdiocally
		
		if(getFirmwareIdentifier()==FW_ID.LOGANDSTREAM && getFirmwareVersionCode() >=6){
			readRealTimeClock();
		}

		initaliseDataProcessing();
		
		mFirstPacketParsed=true;
		resetCalibratedTimeStamp();
		resetPacketLossTrial();
		mSync=true; // a backup sync done every time you start streaming
	}
	
	public void startSDLogging(){
		if(getFirmwareIdentifier()==FW_ID.LOGANDSTREAM && getFirmwareVersionCode() >=6){
			writeInstruction(START_LOGGING_ONLY_COMMAND);
		}	
	}
	
	public void stopSDLogging(){
		if(getFirmwareIdentifier()==FW_ID.LOGANDSTREAM && getFirmwareVersionCode() >=6){
			writeInstruction(STOP_LOGGING_ONLY_COMMAND);
		}	
	}
	
	@Override
	public void stopStreaming() {
		if(isStreaming()){
			writeInstruction(STOP_STREAMING_COMMAND);
			
			// For LogAndStream
			stopTimerReadStatus();
		}
	}
	
	
	/**Only applicable for logandstream
	 * 
	 */
	public void stopStreamingAndLogging() {
		// if shimmer is using LogAndStream FW, stop reading its status periodically
		if(getFirmwareIdentifier()==FW_ID.LOGANDSTREAM){ 
			writeInstruction(STOP_SDBT_COMMAND);
			// For LogAndStream
			stopTimerReadStatus();
		}
	}
	
	//endregion
	
	
	//region --------- TIMERS --------- 
	
	public void stopAllTimers(){
		stopTimerReadStatus();
		stopTimerCheckAlive();
		stopTimerCheckForAckOrResp();
		stopTimerReadBattStatus();
	}
	
	public void stopTimerCheckForAckOrResp(){
		//Terminate the timer thread
		if(mTimerCheckForAckOrResp!=null){
			mTimerCheckForAckOrResp.cancel();
			mTimerCheckForAckOrResp.purge();
			mTimerCheckForAckOrResp = null;
		}
	}
	
	public synchronized void startTimerCheckForAckOrResp(int seconds) {
	//public synchronized void responseTimer(int seconds) {
		if(mTimerCheckForAckOrResp!=null) {
			mTimerCheckForAckOrResp.cancel();
			mTimerCheckForAckOrResp.purge();
			mTimerCheckForAckOrResp = null;
		}
		printLogDataForDebugging("Waiting for ack/response for command:\t" + btCommandToString(mCurrentCommand));
		mTimerCheckForAckOrResp = new Timer("Shimmer_" + getMacIdParsed() + "_TimerCheckForResp");
		mTimerCheckForAckOrResp.schedule(new checkForAckOrRespTask(), seconds*1000);
	}
	
	/** Handles command response timeout
	 *
	 */
	class checkForAckOrRespTask extends TimerTask {
		
		@Override
		public void run() {
			{
				int storedFirstTime = (mFirstTime? 1:0);
				
				//Timeout triggered 
				consolePrintLn("Command:\t" + btCommandToString(mCurrentCommand) +" timeout");
				if(mWaitForAck){
					printLogDataForDebugging("Ack not received");
				}
				if(mWaitForResponse) {
					printLogDataForDebugging("Response not received");
					sendStatusMSGtoUI("Response not received, please reset Shimmer Device." + mMyBluetoothAddress); //Android?
				}
				if(mIsStreaming && getPacketReceptionRateOverall()<100){
					printLogDataForDebugging("Packet RR:  " + Double.toString(getPacketReceptionRateOverall()));
				} 
				
				//handle the special case when we are starting/stopping to log in Consensys and we do not get the ACK response
				//we will send the status changed to the GUI anyway
				if(mCurrentCommand==START_LOGGING_ONLY_COMMAND){
					
					printLogDataForDebugging("START_LOGGING_ONLY_COMMAND response not received. Send feedback to the GUI without killing the connection");
					
					mIsSDLogging = true;
					eventLogAndStreamStatusChanged(mCurrentCommand);
					mWaitForAck=false;
					mWaitForResponse=false;
					
					getListofInstructions().remove(0);
					mTransactionCompleted = true;
					setInstructionStackLock(false);
					
					return;
				}
				else if(mCurrentCommand==STOP_LOGGING_ONLY_COMMAND){
					
					printLogDataForDebugging("STOP_LOGGING_ONLY_COMMAND response not received. Send feedback to the GUI without killing the connection");
					
					mIsSDLogging = false;
					eventLogAndStreamStatusChanged(mCurrentCommand);
					mWaitForAck=false;
					mWaitForResponse=false;
					
					getListofInstructions().remove(0);
					mTransactionCompleted = true;
					setInstructionStackLock(false);
					
					return;
				}
				

				if(mCurrentCommand==GET_FW_VERSION_COMMAND){
					setShimmerVersionInfoAndCreateSensorMap(new ShimmerVerObject(HW_ID.SHIMMER_2R, FW_ID.BOILER_PLATE, 0, 1, 0));
					
//					/*Message msg = mHandler.obtainMessage(MESSAGE_TOAST);
//	      	        Bundle bundle = new Bundle();
//	      	        bundle.putString(TOAST, "Firmware Version: " +mFirmwareVersionParsed);
//	      	        msg.setData(bundle);*/
//					if(!mDummy){
//						//mHandler.sendMessage(msg);
//					}
					
					mFirstTime=false;
					
					initializeBoilerPlate();
				}
				else if(mCurrentCommand==GET_SAMPLING_RATE_COMMAND && !mIsInitialised){
					mFirstTime=false;
				} 
				else if(mCurrentCommand==GET_SHIMMER_VERSION_COMMAND_NEW){ //in case the new command doesn't work, try the old command
					mFirstTime=false;
					getListofInstructions().clear();
					readShimmerVersionDeprecated();
				}

				
				
				if(mIsStreaming){
					stopTimerCheckForAckOrResp(); //Terminate the timer thread
					mWaitForAck=false;
					mTransactionCompleted=true; //should be false, so the driver will know that the command has to be executed again, this is not supported at the moment 
					setInstructionStackLock(false);
					getListofInstructions().clear();
				}
				else if(storedFirstTime==0){
					// If the command fails to get a response, the API should
					// assume that the connection has been lost and close the
					// serial port cleanly.
					
					if (bytesAvailableToBeRead()){
						readBytes(availableBytes());
					}
					stopTimerCheckForAckOrResp(); //Terminate the timer thread
					printLogDataForDebugging("RETRY TX COUNT: " + Integer.toString(mNumberofTXRetriesCount));
					if (mNumberofTXRetriesCount>=NUMBER_OF_TX_RETRIES_LIMIT && mCurrentCommand!=GET_SHIMMER_VERSION_COMMAND_NEW && !mIsInitialised){
						killConnection(); //If command fail exit device	
					} else if(mNumberofTXRetriesCount>=NUMBER_OF_TX_RETRIES_LIMIT && mIsInitialised){
						killConnection(); //If command fail exit device	
					} else {
						mWaitForAck=false;
						mWaitForResponse=false;
						mTransactionCompleted=true; //should be false, so the driver will know that the command has to be executed again, this is not supported at the moment 
						setInstructionStackLock(false);
						//this is needed because if u dc the shimmer the write call gets stuck
						startTimerCheckForAckOrResp(ACK_TIMER_DURATION+3);
					}
					
					mNumberofTXRetriesCount++;
				}
			}
		} //End Run
	} //End TimerTask
	
//	private void retryTXCommand(){
//		//NOT USED
//	}
	
	private void killConnection(){
		printLogDataForDebugging("Killing Connection");
		stop(); //If command fail exit device 
	}

	public void startTimerReadStatus(){
		// if shimmer is using LogAndStream FW, stop reading its status periodically
		if(getFirmwareIdentifier()==FW_ID.LOGANDSTREAM){ 
			if(mTimerReadStatus==null){ 
				mTimerReadStatus = new Timer("Shimmer_" + getMacIdParsed() + "_TimerReadStatus");
			} else {
				mTimerReadStatus.cancel();
				mTimerReadStatus.purge();
				mTimerReadStatus = null;
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
			if(getListofInstructions().size()==0 
					&& !getListofInstructions().contains(GET_STATUS_COMMAND)){
				readStatusLogAndStream();
			}
		}
		
	}
	
	public void startTimerCheckIfAlive(){
		if(mCheckIfConnectionisAlive){
			if(mTimerCheckAlive==null){ 
				mTimerCheckAlive = new Timer("Shimmer_" + getMacIdParsed() + "_TimerCheckAlive");
			}
			//dont really need this for log and stream since we already have the get status timer
			if(getFirmwareIdentifier()==FW_ID.LOGANDSTREAM){ // check if Shimmer is using LogAndStream firmware
				mTimerCheckAlive.schedule(new checkIfAliveTask(), mCheckAlivePeriod, mCheckAlivePeriod);
			} else if (getFirmwareIdentifier()==FW_ID.BTSTREAM) {
				mTimerCheckAlive.schedule(new checkIfAliveTask(), mCheckAlivePeriod, mCheckAlivePeriod);
			}
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
			if(mIamAlive){
				mCountDeadConnection = 0;
				mIamAlive=false;
			}
			else{
				if(getFirmwareIdentifier()==FW_ID.LOGANDSTREAM & mIsStreaming){
					mCountDeadConnection++;
				} else if(getFirmwareIdentifier()==FW_ID.BTSTREAM) {
					mCountDeadConnection++;
				}
				if(getFirmwareVersionCode()>=6 && !mIsStreaming){
					if(getListofInstructions().size()==0 
							&&!getListofInstructions().contains(TEST_CONNECTION_COMMAND)){
						consolePrintLn("Check Alive Task");
						if(getFirmwareIdentifier()==FW_ID.LOGANDSTREAM){
							//writeTestConnectionCommand(); //dont need this because of the get status command
						} else if (getFirmwareIdentifier()==FW_ID.BTSTREAM){
							writeTestConnectionCommand();
						}
					}
				} 
				else {
					consolePrintLn("Check Alive Task");
					writeLEDCommand(0);
				}
				if(mCountDeadConnection>5){
//					setState(BT_STATE.NONE);
					killConnection(); //If command fail exit device
				}
			} 
		} //End Run
	} //End TimerTask
	
	public void startTimerReadBattStatus(){
		//Instream response only supported in LogAndStream
		if((getFirmwareIdentifier()==FW_ID.LOGANDSTREAM)&&(getFirmwareVersionCode()>=6)){
			if(mTimerReadBattStatus==null){ 
				mTimerReadBattStatus = new Timer("Shimmer_" + getMacIdParsed() + "_TimerBattStatus");
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
			consolePrintLn("Read Batt Task");
			readBattery();
		} //End Run
	} //End TimerTask
	
	public void restartTimersIfNull() {
		if (mTimerCheckAlive==null && mTimerReadStatus==null && mTimerReadBattStatus==null){
        	//super.operationFinished();
			startTimerCheckIfAlive();
			startTimerReadStatus();
			startTimerReadBattStatus();
			
        }
	}
	
	//endregion --------- TIMERS ---------
	
	
	//region --------- READ ONLY FUNCTIONS (NO WRITE EQUIVALENT - implemented anyway) --------- 
	
	public void readFWVersion() {
		mDummy=false;//false
		writeInstruction(GET_FW_VERSION_COMMAND);
	}

	@Deprecated
	public void readShimmerVersionDeprecated(){
		writeInstruction(GET_SHIMMER_VERSION_COMMAND);
	}

	public void readShimmerVersionNew() {
		mDummy=false;//false
//		if(mFirmwareVersionParsed.equals(boilerPlateString)){
//			mShimmerVersion = HW_ID.SHIMMER_2R; // on Shimmer2r has 
			
//		} 
//		else if(mFWVersion!=1.2){
			writeInstruction(GET_SHIMMER_VERSION_COMMAND_NEW);
//		} 
//			else {
//			mListofInstructions.add(GET_SHIMMER_VERSION_COMMAND);
//		}
	}
	
	public void readPressureCalibrationCoefficients() {
		if(getHardwareVersion()==HW_ID.SHIMMER_3){
			if(getFirmwareVersionCode()>1){
				writeInstruction(GET_BMP180_CALIBRATION_COEFFICIENTS_COMMAND);
			}
		}
	}

	public void readBattery(){
		writeInstruction(GET_VBATT_COMMAND);
	}
	
	/**
	 * Read the number of bytes specified starting in the offset from the expansion board attached to the Shimmer Device
	 * @param numBytes number of bytes to be read. there can be read up to 256 bytes
	 * @param offset point from where the function starts to read
	 */
	public void readExpansionBoardByBytes(int numBytes, int offset){
		if(getFirmwareVersionCode()>=5){ 
			if(numBytes+offset<=256){
				numBytesToReadFromExpBoard = numBytes;
				writeInstruction(new byte[]{GET_DAUGHTER_CARD_ID_COMMAND, (byte) numBytes, (byte) offset});
			}
		}
	}

	public void readExpansionBoardID(){
		if(getFirmwareVersionCode()>=5){ 
			numBytesToReadFromExpBoard=3;
			int offset=0;
			writeInstruction(new byte[]{GET_DAUGHTER_CARD_ID_COMMAND, (byte) numBytesToReadFromExpBoard, (byte) offset});
		}
	}
	
	public void readDirectoryName(){
		if(getFirmwareIdentifier()==FW_ID.LOGANDSTREAM){ // check if Shimmer is using LogAndStream firmware
			writeInstruction(GET_DIR_COMMAND);
		}
	}
	
	public void readStatusLogAndStream(){
		if(getFirmwareIdentifier()==FW_ID.LOGANDSTREAM){ // check if Shimmer is using LogAndStream firmware
			writeInstruction(GET_STATUS_COMMAND);
			consolePrintLn("Instruction added to the list");
		}
	}

	
	//endregion --------- READ ONLY FUNCTIONS (NO WRITE EQUIVALENT - implemented anyway) --------- 
	
	//region --------- WRITE ONLY FUNCTIONS (NO READ EQUIVALENT - implemented anyway) --------- 
	/**
	 * writeGyroSamplingRate(range) sets the GyroSamplingRate on the Shimmer (version 3) to the value of the input range. Note that when using writesamplingrate this value will be overwritten based on the lowpowergyro mode setting.
	 * @param rate it is a value between 0 and 255; 6 = 1152Hz, 77 = 102.56Hz, 255 = 31.25Hz
	 */
	public void writeGyroSamplingRate(int rate) {
		if(getHardwareVersion()==HW_ID.SHIMMER_3){
			mTempIntValue=rate;
			writeInstruction(new byte[]{SET_MPU9150_SAMPLING_RATE_COMMAND, (byte)rate});
		}
	}
	
	/**
	 * Transmits a command to the Shimmer device to enable the sensors. To enable multiple sensors an or operator should be used (e.g. writeEnabledSensors(SENSOR_ACCEL|SENSOR_GYRO|SENSOR_MAG)). Command should not be used consecutively. Valid values are SENSOR_ACCEL, SENSOR_GYRO, SENSOR_MAG, SENSOR_ECG, SENSOR_EMG, SENSOR_GSR, SENSOR_EXP_BOARD_A7, SENSOR_EXP_BOARD_A0, SENSOR_BRIDGE_AMP and SENSOR_HEART.
    SENSOR_BATT
	 * @param enabledSensors e.g SENSOR_ACCEL|SENSOR_GYRO|SENSOR_MAG
	 */
	public void writeEnabledSensors(long enabledSensors) {
		
		if(getFirmwareVersionCode()<=1){
			if(!sensorConflictCheck(enabledSensors)){ //sensor conflict check - not needed with new SensorMap
				return;
			} 
			else {
				enabledSensors=generateSensorBitmapForHardwareControl(enabledSensors);
			}
		}
			
		tempEnabledSensors=enabledSensors;

		byte secondByte=(byte)((enabledSensors & 0xFF00)>>8);
		byte firstByte=(byte)(enabledSensors & 0xFF);

		//write(new byte[]{SET_SENSORS_COMMAND,(byte) lowByte, highByte});
		if(getHardwareVersion()==HW_ID.SHIMMER_3){
			byte thirdByte=(byte)((enabledSensors & 0xFF0000)>>16);
			writeInstruction(new byte[]{SET_SENSORS_COMMAND,(byte) firstByte,(byte) secondByte,(byte) thirdByte});
		} 
		else {
			writeInstruction(new byte[]{SET_SENSORS_COMMAND,(byte) firstByte,(byte) secondByte});
		}
		inquiry();
	}
	
	/**
	 * writePressureResolution(range) sets the resolution of the pressure sensor on the Shimmer3
	 * @param settinge Numeric value defining the desired resolution of the pressure sensor. Valid range settings are 0 (low), 1 (normal), 2 (high), 3 (ultra high)
	 * 
	 * */
	public void writePressureResolution(int setting) {
		if(getHardwareVersion()==HW_ID.SHIMMER_3){
			writeInstruction(new byte[]{SET_BMP180_PRES_RESOLUTION_COMMAND, (byte)setting});
		}
	}

	public void writeTestConnectionCommand() {
		if(getFirmwareVersionCode()>=6){
			writeInstruction(TEST_CONNECTION_COMMAND);
		}
	}
	
	/**
	 * Sets the Pmux bit value on the Shimmer to the value of the input SETBIT. The PMux bit is the 2nd MSB of config byte0.
	 * @param setBit value defining the desired setting of the PMux (1=ON, 0=OFF).
	 */
	public void writePMux(int setBit) {
		writeInstruction(new byte[]{SET_PMUX_COMMAND,(byte) setBit});
	}

	/**
	 * Sets the configGyroTempVref bit value on the Shimmer to the value of the input SETBIT. The configGyroTempVref bit is the 2nd MSB of config byte0.
	 * @param setBit value defining the desired setting of the Gyro Vref (1=ON, 0=OFF).
	 */
	/*public void writeConfigGyroTempVref(int setBit) {
    	while(getInstructionStatus()==false) {};
			//Bit value defining the desired setting of the PMux (1=ON, 0=OFF).
			if(setBit==1) {
				mTempByteValue=(byte) (mConfigByte0|32); 
			} 
			else if(setBit==0) {
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
		if(getHardwareVersion()==HW_ID.SHIMMER_3 && getFirmwareVersionCode()>=2){
			writeInstruction(new byte[]{SET_INTERNAL_EXP_POWER_ENABLE_COMMAND,(byte) setBit});
		} 
		else {
			
		}
	}
	
	/**
	 * Enable/disable the 5 Volt Regulator on the Shimmer ExpBoard board
	 * @param setBit value defining the desired setting of the Volt regulator (1=ENABLED, 0=DISABLED).
	 */
	public void writeFiveVoltReg(int setBit) {
		writeInstruction(new byte[]{SET_5V_REGULATOR_COMMAND,(byte) setBit});
	}

	//endregion --------- WRITE ONLY FUNCTIONS (NO READ EQUIVALENT - implemented anyway) --------- 
	
	//region --------- READ/WRITE FUNCTIONS --------- 
	
	public void readMagSamplingRate() {
		if(isThisVerCompatibleWith(HW_ID.SHIMMER_2R, FW_ID.BOILER_PLATE, 0, 1, 0)){
		} 
		else {
			writeInstruction(GET_MAG_SAMPLING_RATE_COMMAND);
		}
	}

	/**
	 * writeMagSamplingRate(range) sets the MagSamplingRate on the Shimmer to the value of the input range. Note that when using writesamplingrate this value will be overwritten based on the lowpowermag mode setting.
	 * @param rate for Shimmer 2 it is a value between 1 and 6; 0 = 0.5 Hz; 1 = 1.0 Hz; 2 = 2.0 Hz; 3 = 5.0 Hz; 4 = 10.0 Hz; 5 = 20.0 Hz; 6 = 50.0 Hz, for Shimmer 3 it is a value between 0-7; 0 = 0.75Hz; 1 = 1.5Hz; 2 = 3Hz; 3 = 7.5Hz; 4 = 15Hz ; 5 = 30 Hz; 6 = 75Hz ; 7 = 220Hz 
	 * 
	 * */
	public void writeMagSamplingRate(int rate) {
		if(isThisVerCompatibleWith(HW_ID.SHIMMER_2R, FW_ID.BOILER_PLATE, 0, 1, 0)){
//			if(mFirmwareVersionParsed.equals(boilerPlateStringDescription)){
		} 
		else {
			mTempIntValue=rate;
			writeInstruction(new byte[]{SET_MAG_SAMPLING_RATE_COMMAND, (byte)rate});
		}
	}
	
	/**
	 * Used to retrieve the data rate of the Accelerometer on Shimmer 3
	 */
	public void readAccelSamplingRate() {
		if(getHardwareVersion()!=HW_ID.SHIMMER_3){
		} 
		else {
			writeInstruction(GET_ACCEL_SAMPLING_RATE_COMMAND);
		}
	}

	/**
	 * writeAccelSamplingRate(range) sets the AccelSamplingRate on the Shimmer (version 3) to the value of the input range. Note that when using writesamplingrate this value will be overwritten based on the lowpowerwraccel mode setting.
	 * @param rate it is a value between 1 and 7; 1 = 1 Hz; 2 = 10 Hz; 3 = 25 Hz; 4 = 50 Hz; 5 = 100 Hz; 6 = 200 Hz; 7 = 400 Hz
	 */
	public void writeAccelSamplingRate(int rate) {
		if(getHardwareVersion()==HW_ID.SHIMMER_3){
			mTempIntValue=rate;
			writeInstruction(new byte[]{SET_ACCEL_SAMPLING_RATE_COMMAND, (byte)rate});
		}
	}
	
	public void readAccelRange() {
		writeInstruction(GET_ACCEL_SENSITIVITY_COMMAND);
	}

	/**
	 * writeAccelRange(range) sets the Accelerometer range on the Shimmer to the value of the input range. When setting/changing the accel range, please ensure you have the correct calibration parameters. Note that the Shimmer device can only carry one set of accel calibration parameters at a single time.
	 * @param range is a numeric value defining the desired accelerometer range. Valid range setting values for the Shimmer 2 are 0 (+/- 1.5g), 1 (+/- 2g), 2 (+/- 4g) and 3 (+/- 6g). Valid range setting values for the Shimmer 2r are 0 (+/- 1.5g) and 3 (+/- 6g).
	 */
	public void writeAccelRange(int range) {
		writeInstruction(new byte[]{SET_ACCEL_SENSITIVITY_COMMAND, (byte)range});
		setAccelRange((int)range);
		
	}
	
	/**Read the derived channel bytes. Currently only supported on logandstream
	 * 
	 */
	public void readDerivedChannelBytes() {
		if(getFirmwareVersionCode()>=6){
			writeInstruction(GET_DERIVED_CHANNEL_BYTES);
		}
	}
	
	/** Only applicable for Log and Stream
	 * @param channel The derived channels (3 bytes), MSB = (channel[2]), and LSB = channel[0])
	 */
	public void writeDerivedChannelBytes(byte[] channel) {
		if(getFirmwareVersionCode()>=6){
			writeInstruction(new byte[]{SET_DERIVED_CHANNEL_BYTES, channel[0], channel[1], channel[2]});
		}
	}
	
	/** Only applicable for Log and Stream
	 * @param channel The derived channels (3 bytes), MSB = (channel[2]), and LSB = channel[0])
	 */
	public void writeDerivedChannels(long channels) {
		if(getFirmwareVersionCode()>=6){
			byte[] channel = new byte[3];
			channel[2] = (byte) ((channels >> 16) & 0xFF);
			channel[1] = (byte) ((channels >> 8) & 0xFF);
			channel[0] = (byte) ((channels >> 0) & 0xFF);
			writeInstruction(new byte[]{SET_DERIVED_CHANNEL_BYTES, channel[0], channel[1], channel[2]});
		}
	}
	
	public void readTrial(){
		if(getFirmwareIdentifier()==FW_ID.LOGANDSTREAM){
			writeInstruction(GET_TRIAL_CONFIG_COMMAND);
		}
	}
	
	/**Writes trial config, note only userbutton only works (FW)
	 * 
	 */
	public void writeTrial(){
		if(getFirmwareIdentifier()==FW_ID.LOGANDSTREAM){
			byte[] trial_config_byte = combineTrialConfig();
			byte[] tosend = new byte[4];
			tosend[0] = SET_TRIAL_CONFIG_COMMAND;
			tosend[1] = trial_config_byte[0];
			tosend[2] = trial_config_byte[1];
			tosend[3] = (byte)getSyncBroadcastInterval();
			writeInstruction(tosend);
		}
	}

    public void fillTrialShimmer3(byte[] packet) {
        SplitTrialConfig(packet[0] + (packet[1] << 8));
        setSyncBroadcastInterval((int)packet[2]);
    }
	
    // btsd changes
    public void SplitTrialConfig(int val) {
        //trialConfig = val;
        mSync = ((val >> 2) & 0x01)==1;
        setButtonStart(((val >> 5) & 0x01)==1); // currently FW only supports this
        setMasterShimmer(((val >> 1) & 0x01)==1);
        setSingleTouch(((val >> 15) & 0x01)==1);
        setTCXO(((val >> 12) & 0x01)==1);
        setInternalExpPower(((val >> 11) & 0x01)==1);
        //monitor = ((val >> 10) & 0x01)==1;
    }
    
	// btsd changes
    public byte[] combineTrialConfig() {
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
    
	public void readConfigTime(){
		if(getFirmwareIdentifier()==FW_ID.LOGANDSTREAM){
			writeInstruction(GET_CONFIGTIME_COMMAND);
		}
	}
	
	/**Write the config time to the Shimmer device. Only applicable for Log and Stream.
	 * 
	 */
	public void writeConfigTime(String time){
		if(getFirmwareIdentifier()==FW_ID.LOGANDSTREAM){
			byte[] toSendTime = time.getBytes();
			byte[] toSend = new byte[2+toSendTime.length];
			toSend[0]= SET_CONFIGTIME_COMMAND;
			toSend[1]= (byte)toSendTime.length;
			System.arraycopy(toSendTime, 0, toSend, 2, toSendTime.length);
			writeInstruction(toSend);
		}
	}

	/**Write the config time to the Shimmer device. Only applicable for Log and Stream.
	 * 
	 */
	public void writeConfigTime(long time){
		if(getFirmwareIdentifier()==FW_ID.LOGANDSTREAM){
			String timeString = Long.toString(time);
			byte[] toSendTime = timeString.getBytes();
			byte[] toSend = new byte[2+toSendTime.length];
			toSend[0]= SET_CONFIGTIME_COMMAND;
			toSend[1]= (byte)toSendTime.length;
			System.arraycopy(toSendTime, 0, toSend, 2, toSendTime.length);
			writeInstruction(toSend);
		}
	}

	public void readShimmerName(){
		if(getFirmwareIdentifier()==FW_ID.LOGANDSTREAM){
			writeInstruction(GET_SHIMMERNAME_COMMAND);
		}
	}
	
	/**Write the current shimmer name to the Shimmer device. Only applicable for Log and Stream.
	 * 
	 */
	public void writeShimmerName(){
		if(getFirmwareIdentifier()==FW_ID.LOGANDSTREAM){
			writeShimmerUserAssignedName(getShimmerUserAssignedName());
		}
	}
	
	/**
	 * @param name Name to write to shimmer device
	 */
	public void writeShimmerUserAssignedName(String name){
		if(getFirmwareIdentifier()==FW_ID.LOGANDSTREAM){
			byte[] toSendName = name.getBytes();
			byte[] toSend = new byte[2+toSendName.length];
			toSend[0]= SET_SHIMMERNAME_COMMAND;
			toSend[1]= (byte)toSendName.length;
			System.arraycopy(toSendName, 0, toSend, 2, toSendName.length);
			writeInstruction(toSend);
		}
	}
	
	public void readCenter(){
		if(getFirmwareIdentifier()==FW_ID.LOGANDSTREAM){
			writeInstruction(GET_CENTER_COMMAND);
		}
	}
	
	/** 
	 * @param center 
	 */
	public void writeCenter(String center){
		if(getFirmwareIdentifier()==FW_ID.LOGANDSTREAM){
			byte[] toSendCenter = center.getBytes();
			byte[] toSend = new byte[2+toSendCenter.length];
			toSend[0]= SET_CENTER_COMMAND;
			toSend[1]= (byte)toSendCenter.length;
			System.arraycopy(toSendCenter, 0, toSend, 2, toSendCenter.length);
			writeInstruction(toSend);
		}
	}
	
	public void readExperimentName(){
		if(getFirmwareIdentifier()==FW_ID.LOGANDSTREAM){
			writeInstruction(GET_EXPID_COMMAND);
		}
	}

	/**
	 * @param name Name to write to shimmer device
	 */
	public void writeExperimentName(String name){
		if(getFirmwareIdentifier()==FW_ID.LOGANDSTREAM){
			byte[] toSendName = name.getBytes();
			byte[] toSend = new byte[2+toSendName.length];
			toSend[0]= SET_EXPID_COMMAND;
			toSend[1]= (byte)toSendName.length;
			System.arraycopy(toSendName, 0, toSend, 2, toSendName.length);
			writeInstruction(toSend);
		}
	}
	
	public void readRealTimeClock(){
		if(getFirmwareIdentifier()==FW_ID.LOGANDSTREAM){
			writeInstruction(GET_RWC_COMMAND);
		}
	}
	
	/**Gets pc time and writes the 8 byte value to shimmer device
	 * 
	 */
	public void writeRealTimeClock(){
		if(getFirmwareIdentifier()==FW_ID.LOGANDSTREAM){
			//Just fill empty bytes here for RTC, set them just before writing to Shimmer
		    byte[] bytearraycommand= new byte[9];
			bytearraycommand[0]=SET_RWC_COMMAND;
			writeInstruction(bytearraycommand);
		}
	}
	
	public void readGyroRange() {
		writeInstruction(GET_MPU9150_GYRO_RANGE_COMMAND);
	}
	
	/**
	 * writeGyroRange(range) sets the Gyroscope range on the Shimmer3 to the value of the input range. When setting/changing the range, please ensure you have the correct calibration parameters.
	 * @param range is a numeric value defining the desired gyroscope range. 
	 */
	public void writeGyroRange(int range) {
		if(getHardwareVersion()==HW_ID.SHIMMER_3){
			writeInstruction(new byte[]{SET_MPU9150_GYRO_RANGE_COMMAND, (byte)range});
			setGyroRange((int)range);
		}
	}

	public void readSamplingRate() {
		writeInstruction(GET_SAMPLING_RATE_COMMAND);
	}

	/**
	 * The reason for this is because sometimes the 1st response is not received by the phone
	 */
	protected void dummyreadSamplingRate() {
		mDummy=true;
		writeInstruction(GET_SAMPLING_RATE_COMMAND);
	}

	/**
	 * @param rate Defines the sampling rate to be set (e.g.51.2 sets the sampling rate to 51.2Hz). User should refer to the document Sampling Rate Table to see all possible values.
	 */
	public void writeShimmerAndSensorsSamplingRate(double rate) {
		if(mIsInitialised) {
			setShimmerAndSensorsSamplingRate(rate);
			if(getHardwareVersion()==HW_ID.SHIMMER_2 || getHardwareVersion()==HW_ID.SHIMMER_2R){

				writeMagSamplingRate(mShimmer2MagRate);
				
				int samplingByteValue = (int) (1024/getSamplingRateShimmer()); //the equivalent hex setting
				writeInstruction(new byte[]{SET_SAMPLING_RATE_COMMAND, (byte)Math.rint(samplingByteValue), 0x00});
			} 
			else if(getHardwareVersion()==HW_ID.SHIMMER_3) {
				
				writeMagSamplingRate(mLSM303MagRate);
				writeAccelSamplingRate(mLSM303DigitalAccelRate);
				writeGyroSamplingRate(mMPU9150GyroAccelRate);
				writeExgSamplingRate(rate);
				
				int samplingByteValue;
				
				// RM: get Shimmer compatible sampling rate (use ceil or floor depending on which is appropriate to the user entered sampling rate)
		    	if((Math.ceil(32768/getSamplingRateShimmer()) - 32768/getSamplingRateShimmer()) < 0.05){
		    		samplingByteValue = (int)Math.ceil(32768/getSamplingRateShimmer());
		    	}
		    	else{
		    		samplingByteValue = (int)Math.floor(32768/getSamplingRateShimmer());
		    	}	
				
				writeInstruction(new byte[]{SET_SAMPLING_RATE_COMMAND, (byte)(samplingByteValue&0xFF), (byte)((samplingByteValue>>8)&0xFF)});
			}
		}
	}
	
	
	private void writeExgSamplingRate(double rate) {
		if (rate<=125){
			writeEXGRateSetting(0);
		} else if (rate<=250){
			writeEXGRateSetting(1);
		} else if (rate<=500){
			writeEXGRateSetting(2);
		} else if (rate<=1000){
			writeEXGRateSetting(3);
		} else {
			writeEXGRateSetting(4);
		}
	}
	
	/**
	 * This reads the configuration of both chips from the EXG board
	 */
	public void readEXGConfigurations(){
		if((getFirmwareVersionInternal() >=8 && getFirmwareVersionCode()==2) || getFirmwareVersionCode()>2){
			readEXGConfigurations(EXG_CHIP_INDEX.CHIP1);
			readEXGConfigurations(EXG_CHIP_INDEX.CHIP2);
		}
	}
	
	/**This reads the configuration of a chip from the EXG board
	 * @param chipID enum for the Chip number
	 * @see EXG_CHIP_INDEX
	 */
	public void readEXGConfigurations(EXG_CHIP_INDEX chipID){
		if((getFirmwareVersionInternal() >=8 && getFirmwareVersionCode()==2) || getFirmwareVersionCode()>2){
			writeInstruction(new byte[]{GET_EXG_REGS_COMMAND,(byte)(chipID.ordinal()),0,10});
		}
	}
	

	/**
	 * Only supported on Shimmer3, note that unlike previous write commands
	 * where the values are only set within the instrument driver after the ACK
	 * is received, this is set immediately. Fail safe should the settings not
	 * be actually set successfully is a timeout will occur, and the ID will
	 * disconnect from the device
	 * 
	 * This function set the treshold of the ExG Lead-Off Comparator. There are
	 * 8 possible values: 1. Pos:95% - Neg:5%, 2. Pos:92.5% - Neg:7.5%, 3.
	 * Pos:90% - Neg:10%, 4. Pos:87.5% - Neg:12.5%, 5. Pos:85% - Neg:15%, 6.
	 * Pos:80% - Neg:20%, 7. Pos:75% - Neg:25%, 8. Pos:70% - Neg:30%
	 * 
	 * @param treshold
	 *            where 0 = 95-5, 1 = 92.5-7.5, 2 = 90-10, 3 = 87.5-12.5, 4 =
	 *            85-15, 5 = 80-20, 6 = 75-25, 7 = 70-30
	 */
	public void writeEXGLeadOffComparatorTreshold(int treshold){
		if(getFirmwareVersionCode()>2){
			if(treshold>=0 && treshold<=7){ 
				setEXGLeadOffComparatorTreshold(treshold);
				writeEXGConfiguration();
			}
		}
	}
	
	
	/**
	 * Only supported on Shimmer3, note that unlike previous write commands
	 * where the values are only set within the instrument driver after the ACK
	 * is received, this is set immediately. Fail safe should the settings not
	 * be actually set successfully is a timeout will occur, and the ID will
	 * disconnect from the device
	 * 
	 * This function set the ExG Lead-Off Current. There are 4 possible values:
	 * 6nA (default), 22nA, 6uA and 22uA.
	 * 
	 * @param LeadOffCurrent
	 *            where 0 = 6nA, 1 = 22nA, 2 = 6uA and 3 = 22uA
	 */
	public void writeEXGLeadOffDetectionCurrent(int leadOffCurrent){
		if(getFirmwareVersionCode()>2){
			if(leadOffCurrent>=0 && leadOffCurrent<=3){
				setEXGLeadOffDetectionCurrent(leadOffCurrent);
				writeEXGConfiguration();
			}
		}
	}
	
	/**
	 * Only supported on Shimmer3
	 * This function set the ExG Lead-Off detection mode. There are 3 possible modes: DC Current, AC Current (not supported yet), and Off.
	 * @param detectionMode where 0 = Off, 1 = DC Current, and 2 = AC Current
	 */
	public void writeEXGLeadOffDetectionMode(int detectionMode){
		
		if(getFirmwareVersionCode()>2){
			if(detectionMode>=0 && detectionMode<=2){
				setEXGLeadOffCurrentMode(detectionMode);
				writeEXGConfiguration();
			}
		}
	}
	
	/**
	 * Only supported on Shimmer3, note that unlike previous write commands
	 * where the values are only set within the instrument driver after the ACK
	 * is received, this is set immediately. Fail safe should the settings not
	 * be actually set successfully is a timeout will occur, and the ID will
	 * disconnect from the device
	 * 
	 * This function set the ExG reference electrode. There are 2 possible
	 * values when using ECG configuration: Inverse Wilson CT (default) and
	 * Fixed Potential and 2 possible values when using EMG configuration: Fixed
	 * Potential (default) and Inverse of Ch 1
	 * 
	 * @param referenceElectrode
	 *            reference electrode code where 0 = Fixed Potential and 13 =
	 *            Inverse Wilson CT (default) for an ECG configuration, and
	 *            where 0 = Fixed Potential (default) and 3 = Inverse Ch1 for an
	 *            EMG configuration
	 */
	public void writeEXGReferenceElectrode(int referenceElectrode){
		if(getFirmwareVersionCode()>2){
			if(referenceElectrode>=0 && referenceElectrode<=15){
				setEXGReferenceElectrode(referenceElectrode);
				writeEXGConfiguration(getEXG1RegisterArray(),EXG_CHIP_INDEX.CHIP1); //Specific to Chip1
			}
		}
	}

	/**
	 * @param rateSetting
	 *            , where 0=125SPS ; 1=250SPS; 2=500SPS; 3=1000SPS; 4=2000SPS
	 */
	public void writeEXGRateSetting(int rateSetting){
		if((getFirmwareVersionInternal() >=8 && getFirmwareVersionCode()==2) || getFirmwareVersionCode()>2){
			if(rateSetting>=0 && rateSetting<=4){
				setEXGRateSetting(rateSetting);
				writeEXGConfiguration();
			}		
		}
	}
	
	/**
	 * Only supported on Shimmer3, note that unlike previous write commands
	 * where the values are only set within the instrument driver after the ACK
	 * is received, this is set immediately. Fail safe should the settings not
	 * be actually set successfully is a timeout will occur, and the ID will
	 * disconnect from the device
	 * 
	 * @param chipID enum for the Chip number
	 * @see EXG_CHIP_INDEX
	 * @param rateSetting
	 *            , where 0=125SPS ; 1=250SPS; 2=500SPS; 3=1000SPS; 4=2000SPS
	 */
	public void writeEXGRateSetting(EXG_CHIP_INDEX chipID, int rateSetting){
		if((getFirmwareVersionInternal() >=8 && getFirmwareVersionCode()==2) || getFirmwareVersionCode()>2){
			if(rateSetting>=0 && rateSetting<=4){
				setEXGRateSetting(chipID, rateSetting);
				if(chipID==EXG_CHIP_INDEX.CHIP1){
					writeEXGConfiguration(getEXG1RegisterArray(), chipID);
				} 
				else if(chipID==EXG_CHIP_INDEX.CHIP2){
					writeEXGConfiguration(getEXG2RegisterArray(), chipID);
				}
			}
		}
	}
	
	/**
	 * @param gainSetting
	 *            , where 0 = 6x Gain, 1 = 1x , 2 = 2x , 3 = 3x, 4 = 4x, 5 = 8x,
	 *            6 = 12x
	 */
	public void writeEXGGainSetting(int gainSetting){
		if((getFirmwareVersionInternal() >=8 && getFirmwareVersionCode()==2) || getFirmwareVersionCode()>2){
			if(gainSetting>=0 && gainSetting<=6){
				setExGGainSetting(gainSetting);
				writeEXGConfiguration();
			}		
		}
	}
	
	/**
	 * This is only supported on SHimmer3,, note that unlike previous write
	 * commands where the values are only set within the instrument driver after
	 * the ACK is received, this is set immediately. Fail safe should the
	 * settings not be actually set successfully is a timeout will occur, and
	 * the ID will disconnect from the device
	 * 
	 * @param chipID enum for the Chip number
	 * @see EXG_CHIP_INDEX
	 * @param gainSetting
	 *            , where 0 = 6x Gain, 1 = 1x , 2 = 2x , 3 = 3x, 4 = 4x, 5 = 8x,
	 *            6 = 12x
	 * @param channel
	 *            Either a 1 or 2 value
	 */
	public void writeEXGGainSetting(EXG_CHIP_INDEX chipID,  int channel, int gainSetting){
		if((getFirmwareVersionInternal() >=8 && getFirmwareVersionCode()==2) || getFirmwareVersionCode()>2){
			if(gainSetting>=0 && gainSetting<=6){
				setExGGainSetting(chipID, channel, gainSetting);
				if(chipID==EXG_CHIP_INDEX.CHIP1){
					writeEXGConfiguration(getEXG1RegisterArray(), chipID);
				}
				else if(chipID==EXG_CHIP_INDEX.CHIP2){
					writeEXGConfiguration(getEXG2RegisterArray(), chipID);
				}
			}
		}
	}
	
	public void writeEXGConfiguration(){
		if((getFirmwareVersionInternal() >=8 && getFirmwareVersionCode()==2) || getFirmwareVersionCode()>2){
			writeEXGConfiguration(getEXG1RegisterArray(),EXG_CHIP_INDEX.CHIP1);
			writeEXGConfiguration(getEXG2RegisterArray(),EXG_CHIP_INDEX.CHIP2);
		}
	}
	
	/**
	 * Only supported on Shimmer3, note that unlike previous write commands
	 * where the values are only set within the instrument driver after the ACK
	 * is received, this is set immediately. Fail safe should the settings not
	 * be actually set successfully is a timeout will occur, and the ID will
	 * disconnect from the device
	 * 
	 * @param reg	A 10 byte value
	 * @param chipID	enum for the Chip number
	 * @see EXG_CHIP_INDEX
	 */
	public void writeEXGConfiguration(byte[] reg, EXG_CHIP_INDEX chipID){
		if((getFirmwareVersionInternal() >=8 && getFirmwareVersionCode()==2) || getFirmwareVersionCode()>2){
			writeInstruction(new byte[]{SET_EXG_REGS_COMMAND,(byte)(chipID.ordinal()),0,10,reg[0],reg[1],reg[2],reg[3],reg[4],reg[5],reg[6],reg[7],reg[8],reg[9]});
		}
	}
	
	public void readGSRRange() {
		writeInstruction(GET_GSR_RANGE_COMMAND);
	}

	/**
	 * writeGSRRange(range) sets the GSR range on the Shimmer to the value of the input range. 
	 * @param range numeric value defining the desired GSR range. Valid range settings are 0 (10kOhm to 56kOhm), 1 (56kOhm to 220kOhm), 2 (220kOhm to 680kOhm), 3 (680kOhm to 4.7MOhm) and 4 (Auto Range).
	 */
	public void writeGSRRange(int range) {
		if(getHardwareVersion()==HW_ID.SHIMMER_3){
			if(getFirmwareVersionCode()!=1 || getFirmwareVersionInternal() >4){
				writeInstruction(new byte[]{SET_GSR_RANGE_COMMAND, (byte)range});
			}
		} 
		else {
			writeInstruction(new byte[]{SET_GSR_RANGE_COMMAND, (byte)range});
		}
	}
	
	public void readMagRange() {
		writeInstruction(GET_MAG_GAIN_COMMAND);
	}

	/**
	 * writeMagRange(range) sets the MagSamplingRate on the Shimmer to the value of the input range. When setting/changing the accel range, please ensure you have the correct calibration parameters. Note that the Shimmer device can only carry one set of accel calibration parameters at a single time.
	 * @param range is the mag rang
	 */
	public void writeMagRange(int range) {
		if(isThisVerCompatibleWith(HW_ID.SHIMMER_2R, FW_ID.BOILER_PLATE, 0, 1, 0)){
//			if(mFirmwareVersionParsed.equals(boilerPlateStringDescription)){
		} 
		else {
			writeInstruction(new byte[]{SET_MAG_GAIN_COMMAND, (byte)range});
		}
	}

	/**
	 * @param sensor is a string value that defines the sensor. Accepted sensor 
	 * values are "Accelerometer", "Gyroscope", "Magnetometer", "ECG", "EMG", 
	 * "All", "Wide-Range Accelerometer"
	 */
	public void readCalibrationParameters(String sensor) {
		if(!mIsInitialised){
			if(getFirmwareVersionCode()==1 && getFirmwareVersionInternal()==0  && getHardwareVersion()!=HW_ID.SHIMMER_3) {
				//mFirmwareVersionParsed=boilerPlateString;
				/*Message msg = mHandler.obtainMessage(MESSAGE_TOAST);
      	        Bundle bundle = new Bundle();
      	        bundle.putString(TOAST, "Firmware Version: " +mFirmwareVersionParsed);
      	        msg.setData(bundle);
      	        mHandler.sendMessage(msg);*/
			}	
		}
		if(sensor.equals("Accelerometer")) {
			writeInstruction(GET_ACCEL_CALIBRATION_COMMAND);
		}
		else if(sensor.equals("Gyroscope")) {
			writeInstruction(GET_GYRO_CALIBRATION_COMMAND);
		}
		else if(sensor.equals("Magnetometer")) {
			writeInstruction(GET_MAG_CALIBRATION_COMMAND);
		}
		else if(sensor.equals("All")){
			writeInstruction(GET_ALL_CALIBRATION_COMMAND);
		} 
		else if(sensor.equals("ECG")){
			writeInstruction(GET_ECG_CALIBRATION_COMMAND);
		} 
		else if(sensor.equals("EMG")){
			writeInstruction(GET_EMG_CALIBRATION_COMMAND);
		}
		else if(sensor.equals("Wide-Range Accelerometer")){
			writeInstruction(GET_LSM303DLHC_ACCEL_CALIBRATION_COMMAND);
		}
	}
	
	public void writeAccelCalibrationParameters(byte[] calibrationParameters) {
		cmdcalibrationParameters[0] = SET_ACCEL_CALIBRATION_COMMAND;
		System.arraycopy(calibrationParameters, 0, cmdcalibrationParameters, 1, 21);
		writeInstruction(cmdcalibrationParameters);	
	}
	
	public void writeGyroCalibrationParameters(byte[] calibrationParameters) {
		cmdcalibrationParameters[0] = SET_GYRO_CALIBRATION_COMMAND;
		System.arraycopy(calibrationParameters, 0, cmdcalibrationParameters, 1, 21);
		writeInstruction(cmdcalibrationParameters);	
	}
	
	public void writeMagCalibrationParameters(byte[] calibrationParameters) {
		cmdcalibrationParameters[0] = SET_MAG_CALIBRATION_COMMAND;
		System.arraycopy(calibrationParameters, 0, cmdcalibrationParameters, 1, 21);
		writeInstruction(cmdcalibrationParameters);	
	}

	public void writeWRAccelCalibrationParameters(byte[] calibrationParameters) {
		if(getHardwareVersion()==HW_ID.SHIMMER_3){
			cmdcalibrationParameters[0] = SET_LSM303DLHC_ACCEL_CALIBRATION_COMMAND;
			System.arraycopy(calibrationParameters, 0, cmdcalibrationParameters, 1, 21);
			writeInstruction(cmdcalibrationParameters);	
		}
	}
	
	public void readECGCalibrationParameters() {
		if(isThisVerCompatibleWith(HW_ID.SHIMMER_2R, FW_ID.BOILER_PLATE, 0, 1, 0)){
		} 
		else {
			writeInstruction(GET_ECG_CALIBRATION_COMMAND);
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
		if(isThisVerCompatibleWith(HW_ID.SHIMMER_2R, FW_ID.BOILER_PLATE, 0, 1, 0)){
//			if(mFirmwareVersionParsed.equals(boilerPlateStringDescription)){
		} 
		else {
			writeInstruction(new byte[]{SET_ECG_CALIBRATION_COMMAND,data[0],data[1],data[2],data[3],data[4],data[5],data[6],data[7]});
		}
	}
	
	public void readEMGCalibrationParameters() {
		if(isThisVerCompatibleWith(HW_ID.SHIMMER_2R, FW_ID.BOILER_PLATE, 0, 1, 0)){
//			if(mFirmwareVersionParsed.equals(boilerPlateStringDescription)){
		} 
		else {
			writeInstruction(GET_EMG_CALIBRATION_COMMAND);
		}
	}
	
	public void writeEMGCalibrationParameters(int offset, int gain) {
		byte[] data = new byte[4];
		data[0] = (byte) ((offset>>8)& 0xFF); //MSB offset
		data[1] = (byte) ((offset)& 0xFF);
		data[2] = (byte) ((gain>>8)& 0xFF); //MSB gain
		data[3] = (byte) ((gain)& 0xFF);
		if(isThisVerCompatibleWith(HW_ID.SHIMMER_2R, FW_ID.BOILER_PLATE, 0, 1, 0)){
//			if(mFirmwareVersionParsed.equals(boilerPlateStringDescription)){
		} 
		else {
			writeInstruction(new byte[]{SET_EMG_CALIBRATION_COMMAND,data[0],data[1],data[2],data[3]});
		}
	}
	
	public void readBaudRate(){
		if(getFirmwareVersionCode()>=5){ 
			writeInstruction(GET_BAUD_RATE_COMMAND);
		}
	}

	public void writeBaudRate() {
		writeBaudRate(mBluetoothBaudRate);
	}

	/**
	 * writeBaudRate(value) sets the baud rate on the Shimmer. 
	 * @param value numeric value defining the desired Baud rate. Valid rate settings are 0 (115200 default),
	 *  1 (1200), 2 (2400), 3 (4800), 4 (9600) 5 (19200),
	 *  6 (38400), 7 (57600), 8 (230400), 9 (460800) and 10 (921600)
	 */
	public void writeBaudRate(int value) {
		if(getFirmwareVersionCode()>=5){ 
			if(value>=0 && value<=10){
				mBluetoothBaudRate = value;
				writeInstruction(new byte[]{SET_BAUD_RATE_COMMAND, (byte)value});
				delayForBtResponse(200);
				this.reconnect();
			}
		}
	}

	public void readConfigByte0() {
		writeInstruction(GET_CONFIG_BYTE0_COMMAND);
	}
	
	/**
	 * writeConfigByte0(configByte0) sets the config byte0 value on the Shimmer to the value of the input configByte0. 
	 * @param configByte0 is an unsigned 8 bit value defining the desired config byte 0 value.
	 */
	public void writeConfigByte0(byte configByte0) {
		writeInstruction(new byte[]{SET_CONFIG_BYTE0_COMMAND,(byte) configByte0});
	}
	
	public void readBufferSize() {
		writeInstruction(GET_BUFFER_SIZE_COMMAND);
	}

	/**
	 * writeAccelRange(range) sets the Accelerometer range on the Shimmer to the value of the input range. When setting/changing the accel range, please ensure you have the correct calibration parameters. Note that the Shimmer device can only carry one set of accel calibration parameters at a single time.
	 * @param range is a numeric value defining the desired accelerometer range. Valid range setting values for the Shimmer 2 are 0 (+/- 1.5g), 1 (+/- 2g), 2 (+/- 4g) and 3 (+/- 6g). Valid range setting values for the Shimmer 2r are 0 (+/- 1.5g) and 3 (+/- 6g).
	 */
	public void writeBufferSize(int size) {
		writeInstruction(new byte[]{SET_BUFFER_SIZE_COMMAND, (byte)size});
	}
	
	public void readLEDCommand() {
		writeInstruction(GET_BLINK_LED);
	}

	public void writeLEDCommand(int command) {
//		if(mShimmerVersion!=HW_ID.SHIMMER_3){
			if(isThisVerCompatibleWith(HW_ID.SHIMMER_2R, FW_ID.BOILER_PLATE, 0, 1, 0)){
	//			if(mFirmwareVersionParsed.equals(boilerPlateStringDescription)){
			}
			else {
				writeInstruction(new byte[]{SET_BLINK_LED, (byte)command});
			}
//		}
	}
    
	public void readCalibrationDump(){
		if(this.getFirmwareVersionCode()>=7){
			mCalibDumpSize = 0;
			mCalibDumpBuffer = new byte[]{};
			readCalibrationDump(0, 128);
		}
	}

	private void readCalibrationDump(int address, int size){
		if(this.getFirmwareVersionCode()>=7){
			readMem(GET_CALIB_DUMP_COMMAND, address, size, MAX_CALIB_DUMP_MAX); //some max number
		}
	}

	public void rePioritiseReadCalibDumpInstructions(){
		List<byte[]> listOfInstructions = new ArrayList<byte[]>();

		//This for loop will prioritse the GET_CALIB_DUMP_COMMAND
		Iterator<byte[]> iterator = mListofInstructions.iterator();
		while(iterator.hasNext()){
			byte[] instruction = iterator.next();
			if(instruction[0]==GET_CALIB_DUMP_COMMAND){
				listOfInstructions.add(instruction);
				iterator.remove();
			}
		}
		
		if(listOfInstructions.size()>0){
			mListofInstructions.addAll(0, listOfInstructions);
		}
	}


	public void writeCalibrationDump(){
		writeCalibrationDump(mCalibBytes);
	}

	public void writeCalibrationDump(byte[] calibDump){
		if(this.getFirmwareVersionCode()>=7){
			writeMem(SET_CALIB_DUMP_COMMAND, 0, calibDump, MAX_CALIB_DUMP_MAX);
			readCalibrationDump(0, calibDump.length);
		}
	}

	public void readConfigurationFromInfoMem(){
		if(this.getFirmwareVersionCode()>=6){
//			int size = InfoMemLayoutShimmer3.calculateInfoMemByteLength(getFirmwareIdentifier(), getFirmwareVersionMajor(), getFirmwareVersionMinor(), getFirmwareVersionInternal());
			int size = mInfoMemLayout.calculateInfoMemByteLength(getFirmwareIdentifier(), getFirmwareVersionMajor(), getFirmwareVersionMinor(), getFirmwareVersionInternal());
			readInfoMem(mInfoMemLayout.MSP430_5XX_INFOMEM_D_ADDRESS, size, mInfoMemLayout.MSP430_5XX_INFOMEM_LAST_ADDRESS);
		}
	}

	public void readInfoMem(int address, int size, int maxMemAddress){
		readMem(GET_INFOMEM_COMMAND, address, size, maxMemAddress);
	}

	public void readMem(int command, int address, int size, int maxMemAddress){
		if(this.getFirmwareVersionCode()>=6){
//			mMemBuffer = new byte[size];
			mMemBuffer = new byte[]{};

			if (size > (maxMemAddress - address + 1)) {
//				DockException de = new DockException(mDockID,mSlotNumber,ErrorCodesShimmerUart.SHIMMERUART_CMD_ERR_INFOMEM_GET ,ErrorCodesShimmerUart.SHIMMERUART_INFOMEM_READ_REQEST_EXCEEDS_INFO_RANGE);
//				throw(de);
			} 
			else {
				int maxBytesRXed = 128;
				int numBytesRemaining = size;
				int currentPacketNumBytes;
				int currentBytePointer = 0;
				int currentStartAddr = address;

				while (numBytesRemaining > 0) {
					if (numBytesRemaining > maxBytesRXed) {
						currentPacketNumBytes = maxBytesRXed;
					} 
					else {
						currentPacketNumBytes = numBytesRemaining;
					}

					byte[] rxBuf = new byte[] {};
					readMemBlock(command, currentStartAddr, currentPacketNumBytes);
					
					currentBytePointer += currentPacketNumBytes;
					numBytesRemaining -= currentPacketNumBytes;
					currentStartAddr += currentPacketNumBytes;
				}
//				utilDock.consolePrintLn(mDockID + " - InfoMem Configuration Read = SUCCESS");
			}
		}
	}
	
	private void readMemBlock(int command, int currentStartAddr, int currentPacketNumBytes){
		readMemCommand(command, currentStartAddr, currentPacketNumBytes);
	}

	
	public void readMemCommand(int command, int address, int size) {
		if(this.getFirmwareVersionCode()>=6){
	    	byte[] memLengthToRead = new byte[]{(byte) size};
	    	byte[] memAddressToRead = ByteBuffer.allocate(2).putShort((short)(address&0xFFFF)).array();
			ArrayUtils.reverse(memAddressToRead);

	    	byte[] instructionBuffer = new byte[1 + memLengthToRead.length + memAddressToRead.length];
	    	instructionBuffer[0] = (byte)command;
	    	System.arraycopy(memLengthToRead, 0, instructionBuffer, 1, memLengthToRead.length);
	    	System.arraycopy(memAddressToRead, 0, instructionBuffer, 1 + memLengthToRead.length, memAddressToRead.length);

			writeInstruction(instructionBuffer);
		}
	}
	
	public void writeConfigurationToInfoMem(){
		if(this.getFirmwareVersionCode()>=6){
			writeInfoMem(mInfoMemLayout.MSP430_5XX_INFOMEM_D_ADDRESS, generateInfoMemBytesForWritingToShimmer(), mInfoMemLayout.MSP430_5XX_INFOMEM_LAST_ADDRESS);
		}
	}
	
	public void writeConfigurationToInfoMem(byte[] buf){
		if(this.getFirmwareVersionCode()>=6){
			writeInfoMem(mInfoMemLayout.MSP430_5XX_INFOMEM_D_ADDRESS, buf, mInfoMemLayout.MSP430_5XX_INFOMEM_LAST_ADDRESS);
		}
	}

	public void writeInfoMem(int startAddress, byte[] buf, int maxMemAddress){
		if(this.getFirmwareVersionCode()>=6){
			writeMem(SET_INFOMEM_COMMAND, startAddress, buf, maxMemAddress);
			readConfigurationFromInfoMem();
		}
	}

	public void writeMem(int command, int startAddress, byte[] buf, int maxMemAddress){
		this.mNumOfMemSetCmds  = 0;
		
		if(this.getFirmwareVersionCode()>=6){
			int address = startAddress;
			if (buf.length > (maxMemAddress - address + 1)) {
//				err = ErrorCodesShimmerUart.SHIMMERUART_INFOMEM_WRITE_BUFFER_EXCEEDS_INFO_RANGE;
//				DockException de = new DockException(mDockID,mSlotNumber,ErrorCodesShimmerUart.SHIMMERUART_CMD_ERR_INFOMEM_SET ,ErrorCodesShimmerUart.SHIMMERUART_INFOMEM_WRITE_BUFFER_EXCEEDS_INFO_RANGE);
//				throw(de);
			} 
			else {
				int currentStartAddr = startAddress;
				int currentPacketNumBytes;
				int numBytesRemaining = buf.length;
				int currentBytePointer = 0;
				int maxPacketSize = 128;

				while (numBytesRemaining > 0) {
					if (numBytesRemaining > maxPacketSize) {
						currentPacketNumBytes = maxPacketSize;
					} else {
						currentPacketNumBytes = numBytesRemaining;
					}

					byte[] infoSegBuf = Arrays.copyOfRange(buf, currentBytePointer, currentBytePointer + currentPacketNumBytes);

					writeMemCommand(command, currentStartAddr, infoSegBuf);
					mNumOfMemSetCmds += 1;

					currentStartAddr += currentPacketNumBytes;
					numBytesRemaining -= currentPacketNumBytes;
					currentBytePointer += currentPacketNumBytes;
				}
			}
		}
		
//		// Thread sleeps for 1s when ACK from SET_INFOMEM_COMMAND is received so
//		// it is safe to cue the below here to allow Shimmer time to process
//		// configuration
////		readInfoMem(startAddress, buf.length);
//		readConfigurationFromInfoMem();
//		
////		writeEnabledSensors(mEnabledSensors);
////		inquiry();
	}
    
	/**Could be used by InfoMem or Expansion board memory
	 * @param command
	 * @param address
	 * @param infoMemBytes
	 */
	public void writeMemCommand(int command, int address, byte[] infoMemBytes) {
		if(this.getFirmwareVersionCode()>=6){
			byte[] memLengthToWrite = new byte[]{(byte) infoMemBytes.length};
			byte[] memAddressToWrite = ByteBuffer.allocate(2).putShort((short)(address&0xFFFF)).array();
			ArrayUtils.reverse(memAddressToWrite);

			// TODO check I'm not missing the last two bytes here because the mem
			// address length is not being included in the length field
			byte[] instructionBuffer = new byte[1 + memLengthToWrite.length + memAddressToWrite.length + infoMemBytes.length];
	    	instructionBuffer[0] = (byte)command;
			System.arraycopy(memLengthToWrite, 0, instructionBuffer, 1, memLengthToWrite.length);
			System.arraycopy(memAddressToWrite, 0, instructionBuffer, 1 + memLengthToWrite.length, memAddressToWrite.length);
			System.arraycopy(infoMemBytes, 0, instructionBuffer, 1 + memLengthToWrite.length + memAddressToWrite.length, infoMemBytes.length);
			
			writeInstruction(instructionBuffer);
		}
	}

	//endregion --------- READ/WRITE FUNCTIONS --------- 
	
	
	//region --------- GET/IS FUNCTIONS --------- 
	
	/**** GET FUNCTIONS *****/

	/**
	 * This returns the variable mTransactionCompleted which indicates whether the Shimmer device is in the midst of a command transaction. True when no transaction is taking place. This is deprecated since the update to a thread model for executing commands
	 * @return mTransactionCompleted
	 */
	public boolean getInstructionStatus(){	
		boolean instructionStatus=false;
		if(mTransactionCompleted) {
			instructionStatus=true;
		} 
		else {
			instructionStatus=false;
		}
		return instructionStatus;
	}
	
	public int getPacketSize(){
		return mPacketSize;
	}

	public String getDirectoryName(){
		if(mDirectoryName!=null)
			return mDirectoryName;
		else
			return "Directory not read yet";
	}
	
	public double getBattLimitWarning(){
		return mLowBattLimit;
	}

	/**
	 * Get the Gain value for the ExG1 Channel 1
	 * 
	 * @return the value of the gain. The Gain can be 1, 2, 3, 4, 6 (default), 8
	 *         or 12. The function return -1 when it is not possible to get the
	 *         value.
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
	 * 
	 * @return the value of the gain. The Gain can be 1, 2, 3, 4, 6 (default), 8
	 *         or 12. The function return -1 when it is not possible to get the
	 *         value.
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
	 * 
	 * @return the value of the gain. The Gain can be 1, 2, 3, 4, 6 (default), 8
	 *         or 12. The function return -1 when it is not possible to get the
	 *         value.
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
	 * Get the Gain value for the ExG1 Channel 2
	 * 
	 * @return the value of the gain. The Gain can be 1, 2, 3, 4, 6 (default), 8
	 *         or 12. The function return -1 when it is not possible to get the
	 *         value.
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
    //endregion --------- GET FUNCTIONS ---------

	
    //region --------- IS FUNCTIONS ---------
	
//	/**
//	 * Checks if 16 bit ECG configuration is set on the Shimmer device. Do not
//	 * use this command right after setting an EXG setting, as due to the
//	 * execution model, the old settings might be returned, if this command is
//	 * executed before an ack is received.
//	 * 
//	 * @return true if 16 bit ECG is set
//	 */
//	@Override
//	public boolean isEXGUsingECG16Configuration(){
//		while(!getListofInstructions().isEmpty());
//		return super.isEXGUsingECG16Configuration();
//	}
//	
//	/**
//	 * Checks if 24 bit ECG configuration is set on the Shimmer device. Do not
//	 * use this command right after setting an EXG setting, as due to the
//	 * execution model, the old settings might be returned, if this command is
//	 * executed before an ack is received.
//	 * 
//	 * @return true if 24 bit ECG is set
//	 */
//	@Override
//	public boolean isEXGUsingECG24Configuration(){
//		while(!getListofInstructions().isEmpty());
//		return super.isEXGUsingECG24Configuration();
//	}
//	
//	/**
//	 * Checks if 16 bit EMG configuration is set on the Shimmer device. Do not
//	 * use this command right after setting an EXG setting, as due to the
//	 * execution model, the old settings might be returned, if this command is
//	 * executed before an ack is received.
//	 * 
//	 * @return true if 16 bit EMG is set
//	 */
//	@Override
//	public boolean isEXGUsingEMG16Configuration(){
//		while(!getListofInstructions().isEmpty());
//		return super.isEXGUsingEMG16Configuration();
//	}
//	
//	/**
//	 * Checks if 24 bit EMG configuration is set on the Shimmer device. Do not
//	 * use this command right after setting an EXG setting, as due to the
//	 * execution model, the old settings might be returned, if this command is
//	 * executed before an ack is received.
//	 * 
//	 * @return true if 24 bit EMG is set
//	 */
//	@Override
//	public boolean isEXGUsingEMG24Configuration(){
//		while(!getListofInstructions().isEmpty());
//		return super.isEXGUsingEMG24Configuration();
//	}
//	
//	/**
//	 * Checks if 16 bit test signal configuration is set on the Shimmer device.
//	 * Do not use this command right after setting an EXG setting, as due to the
//	 * execution model, the old settings might be returned, if this command is
//	 * executed before an ack is received.
//	 * 
//	 * @return true if 24 bit test signal is set
//	 */
//	@Override
//	public boolean isEXGUsingTestSignal16Configuration(){
//		while(!getListofInstructions().isEmpty());
//		return super.isEXGUsingTestSignal16Configuration();
//	}
//	
//	/**
//	 * Checks if 24 bit test signal configuration is set on the Shimmer device.
//	 * @return true if 24 bit test signal is set
//	 */
//	@Override
//	public boolean isEXGUsingTestSignal24Configuration(){
//		while(!getListofInstructions().isEmpty());
//		return super.isEXGUsingTestSignal24Configuration();
//	}
	
    //endregion --------- IS FUNCTIONS ---------
	
    
    
    //region --------- SET FUNCTIONS ---------
    
	/**
	 * Set the battery voltage limit, when the Shimmer device goes below the
	 * limit while streaming the LED on the Shimmer device will turn Yellow, in
	 * order to use battery voltage monitoring the Battery has to be enabled.
	 * See writeenabledsensors. Only to be used with Shimmer2. Calibration also
	 * has to be enabled, see enableCalibration.
	 * 
	 * @param limit
	 */
	public void setBattLimitWarning(double limit){
		mLowBattLimit=limit;
	}
	
	@Deprecated //mContinousSync doesn't do anything
	public void setContinuousSync(boolean continousSync){
		mContinousSync=continousSync;
	}
	
	@Deprecated //mContinousSync doesn't do anything
	public boolean getContinuousSync(){
		return mContinousSync;
	}
	
    //region --------- SET FUNCTIONS ---------
	
    

	//region --------- ENABLE/DISABLE FUNCTIONS --------- 

	/**** ENABLE FUNCTIONS *****/
	
	//TODO: use set3DOrientation(enable) in ShimmerObject instead -> check that the "enable the sensors if they have not been enabled" comment is correct
	/**
	 * This enables the calculation of 3D orientation through the use of the
	 * gradient descent algorithm, note that the user will have to ensure that
	 * mEnableCalibration has been set to true (see enableCalibration), and that
	 * the accel, gyro and mag has been enabled
	 * 
	 * @param enable
	 */
	public void enable3DOrientation(boolean enable){
		//enable the sensors if they have not been enabled 
		set3DOrientation(enable);
	}

	/**
	 * This enables the low power accel option. When not enabled the sampling
	 * rate of the accel is set to the closest value to the actual sampling rate
	 * that it can achieve. In low power mode it defaults to 10Hz. Also and
	 * additional low power mode is used for the LSM303DLHC. This command will
	 * only supports the following Accel range +4g, +8g , +16g
	 * 
	 * @param enable
	 */
	public void enableLowPowerAccel(boolean enable){
		enableHighResolutionMode(!enable);
		writeAccelSamplingRate(mLSM303DigitalAccelRate);
	}

	private void enableHighResolutionMode(boolean enable) {
		while(!getInstructionStatus()) {};
		
		if(getFirmwareVersionCode()==1 && getFirmwareVersionInternal()==0) {

		} 
		else if(getHardwareVersion()==HW_ID.SHIMMER_3) {
			setLowPowerAccelWR(!enable);
//			setHighResAccelWR(enable);
			if(enable) {
				// High-Res = On, Low-power = Off
				writeInstruction(new byte[]{SET_LSM303DLHC_ACCEL_HRMODE_COMMAND, (byte)0x01});
				writeInstruction(new byte[]{SET_LSM303DLHC_ACCEL_LPMODE_COMMAND, (byte)0x00});
			} 
			else {
				// High-Res = Off, Low-power = On
				writeInstruction(new byte[]{SET_LSM303DLHC_ACCEL_HRMODE_COMMAND, (byte)0x00});
				writeInstruction(new byte[]{SET_LSM303DLHC_ACCEL_LPMODE_COMMAND, (byte)0x01});
			}
		}
	}
	
//	private void enableLowResolutionMode(boolean enable){
//		while(getInstructionStatus()==false) {};
//		if(getFirmwareVersionCode()==1 && mFirmwareVersionRelease==0) {
//
//		} 
//		else if(mShimmerVersion==HW_ID.SHIMMER_3) {
//			if(enable) {
//				mListofInstructions.add(new byte[]{SET_LSM303DLHC_ACCEL_LPMODE_COMMAND, (byte)0x01});
//				mListofInstructions.add(new byte[]{SET_LSM303DLHC_ACCEL_HRMODE_COMMAND, (byte)0x00});
//			} 
//			else {
//				mListofInstructions.add(new byte[]{SET_LSM303DLHC_ACCEL_HRMODE_COMMAND, (byte)0x01});
//				mListofInstructions.add(new byte[]{SET_LSM303DLHC_ACCEL_LPMODE_COMMAND, (byte)0x00});
//			}
//		}
//	}
	
	/**
	 * This enables the low power accel option. When not enabled the sampling
	 * rate of the accel is set to the closest value to the actual sampling rate
	 * that it can achieve. In low power mode it defaults to 10Hz. Also and
	 * additional low power mode is used for the LSM303DLHC. This command will
	 * only supports the following Accel range +4g, +8g , +16g
	 * 
	 * @param enable
	 */
	public void enableLowPowerGyro(boolean enable){
		setLowPowerGyro(enable);
		writeGyroSamplingRate(mMPU9150GyroAccelRate);
	}
	
	/**
	 * This enables the low power mag option. When not enabled the sampling rate
	 * of the mag is set to the closest value to the actual sampling rate that
	 * it can achieve. In low power mode it defaults to 10Hz
	 * 
	 * @param enable
	 */
	public void enableLowPowerMag(boolean enable){
		setLowPowerMag(enable);
		writeMagSamplingRate(mLSM303MagRate);
	}
	
	/**
	 * This can only be used for Shimmer3 devices (EXG) When a enable
	 * configuration is loaded, the advanced ExG configuration is removed, so it
	 * needs to be set again
	 */
	 public void enableDefaultECGConfiguration() {
		 if(getHardwareVersion()==3){
			setDefaultECGConfiguration(getSamplingRateShimmer());
			writeEXGConfiguration();
//			writeEXGConfiguration(mEXG1RegisterArray,1);
//			writeEXGConfiguration(mEXG2RegisterArray,2);
		 }
	}

	/**
	 * This can only be used for Shimmer3 devices (EXG) When a enable
	 * configuration is loaded, the advanced ExG configuration is removed, so it
	 * needs to be set again
	 */
	public void enableDefaultEMGConfiguration(){
		if(getHardwareVersion()==3){
			setDefaultEMGConfiguration(getSamplingRateShimmer());
			writeEXGConfiguration();
//			writeEXGConfiguration(mEXG1RegisterArray,1);
//			writeEXGConfiguration(mEXG2RegisterArray,2);
		}
	}

	/**
	 * This can only be used for Shimmer3 devices (EXG). Enables the test signal
	 * (square wave) of both EXG chips, to use, both EXG1 and EXG2 have to be
	 * enabled
	 */
	public void enableEXGTestSignal(){
		if(getHardwareVersion()==3){
			setEXGTestSignal(getSamplingRateShimmer());
			writeEXGConfiguration();
//			writeEXGConfiguration(mEXG1RegisterArray,1);
//			writeEXGConfiguration(mEXG2RegisterArray,2);
		}
	}
	
	/**** DISABLE FUNCTIONS *****/
	
	private long disableBit(long number,long disablebitvalue){
		if((number&disablebitvalue)>0){
			number = number ^ disablebitvalue;
		}
		return number;
	}
	
	//endregion
	
	
	//region --------- MISCELLANEOUS FUNCTIONS ---------
	
	public void reconnect(){
        if(isConnected() && !mIsStreaming){
        	String msgReconnect = "Reconnecting the Shimmer...";
			sendStatusMSGtoUI(msgReconnect);
            stop();
            threadSleep(300);
            connect(mMyBluetoothAddress,"default");
            mUniqueID = this.mMacIdFromUart; 
        }
    }

	
	/**
	 * An inquiry is used to request for the current configuration parameters
	 * from the Shimmer device (e.g. Accelerometer settings, Configuration Byte,
	 * Sampling Rate, Number of Enabled Sensors and Sensors which have been
	 * enabled).
	 */
	public void inquiry() {
		writeInstruction(INQUIRY_COMMAND);
	}
	
	/**
	 * @param enabledSensors This takes in the current list of enabled sensors 
	 * @param sensorToCheck This takes in a single sensor which is to be enabled
	 * @return enabledSensors This returns the new set of enabled sensors, where any sensors which conflicts with sensorToCheck is disabled on the bitmap, so sensorToCheck can be accomodated (e.g. for Shimmer2 using ECG will disable EMG,GSR,..basically any daughter board)
	 *  
	 */
	public long sensorConflictCheckandCorrection(long enabledSensors,long sensorToCheck){

		if(getHardwareVersion()==HW_ID.SHIMMER_2R || getHardwareVersion()==HW_ID.SHIMMER_2){
			if((sensorToCheck & SENSOR_GYRO) >0 || (sensorToCheck & SENSOR_MAG) >0){
				enabledSensors = disableBit(enabledSensors,SENSOR_ECG);
				enabledSensors = disableBit(enabledSensors,SENSOR_EMG);
				enabledSensors = disableBit(enabledSensors,SENSOR_GSR);
				enabledSensors = disableBit(enabledSensors,SENSOR_BRIDGE_AMP);
			} 
			else if((sensorToCheck & SENSOR_BRIDGE_AMP) >0){
				enabledSensors = disableBit(enabledSensors,SENSOR_ECG);
				enabledSensors = disableBit(enabledSensors,SENSOR_EMG);
				enabledSensors = disableBit(enabledSensors,SENSOR_GSR);
				enabledSensors = disableBit(enabledSensors,SENSOR_GYRO);
				enabledSensors = disableBit(enabledSensors,SENSOR_MAG);
			} 
			else if((sensorToCheck & SENSOR_GSR) >0){
				enabledSensors = disableBit(enabledSensors,SENSOR_ECG);
				enabledSensors = disableBit(enabledSensors,SENSOR_EMG);
				enabledSensors = disableBit(enabledSensors,SENSOR_BRIDGE_AMP);
				enabledSensors = disableBit(enabledSensors,SENSOR_GYRO);
				enabledSensors = disableBit(enabledSensors,SENSOR_MAG);
			} 
			else if((sensorToCheck & SENSOR_ECG) >0){
				enabledSensors = disableBit(enabledSensors,SENSOR_GSR);
				enabledSensors = disableBit(enabledSensors,SENSOR_EMG);
				enabledSensors = disableBit(enabledSensors,SENSOR_BRIDGE_AMP);
				enabledSensors = disableBit(enabledSensors,SENSOR_GYRO);
				enabledSensors = disableBit(enabledSensors,SENSOR_MAG);
			} 
			else if((sensorToCheck & SENSOR_EMG) >0){
				enabledSensors = disableBit(enabledSensors,SENSOR_GSR);
				enabledSensors = disableBit(enabledSensors,SENSOR_ECG);
				enabledSensors = disableBit(enabledSensors,SENSOR_BRIDGE_AMP);
				enabledSensors = disableBit(enabledSensors,SENSOR_GYRO);
				enabledSensors = disableBit(enabledSensors,SENSOR_MAG);
			} 
			else if((sensorToCheck & SENSOR_HEART) >0){
				enabledSensors = disableBit(enabledSensors,SENSOR_EXP_BOARD_A0);
				enabledSensors = disableBit(enabledSensors,SENSOR_EXP_BOARD_A7);
			} 
			else if((sensorToCheck & SENSOR_EXP_BOARD_A0) >0 || (sensorToCheck & SENSOR_EXP_BOARD_A7) >0){
				enabledSensors = disableBit(enabledSensors,SENSOR_HEART);
				enabledSensors = disableBit(enabledSensors,SENSOR_BATT);
			} 
			else if((sensorToCheck & SENSOR_BATT) >0){
				enabledSensors = disableBit(enabledSensors,SENSOR_EXP_BOARD_A0);
				enabledSensors = disableBit(enabledSensors,SENSOR_EXP_BOARD_A7);
			}
		}

		else if(getHardwareVersion()==HW_ID.SHIMMER_3){
			
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
			else if((sensorToCheck & SENSOR_INT_ADC_A14) >0){
				enabledSensors = disableBit(enabledSensors,SENSOR_GSR);
				enabledSensors = disableBit(enabledSensors,SENSOR_EXG1_16BIT);
				enabledSensors = disableBit(enabledSensors,SENSOR_EXG2_16BIT);
				enabledSensors = disableBit(enabledSensors,SENSOR_EXG1_24BIT);
				enabledSensors = disableBit(enabledSensors,SENSOR_EXG2_24BIT);
			}
			else if((sensorToCheck & SENSOR_INT_ADC_A12) >0){
				enabledSensors = disableBit(enabledSensors,SENSOR_EXG1_16BIT);
				enabledSensors = disableBit(enabledSensors,SENSOR_EXG2_16BIT);
				enabledSensors = disableBit(enabledSensors,SENSOR_EXG1_24BIT);
				enabledSensors = disableBit(enabledSensors,SENSOR_EXG2_24BIT);
				enabledSensors = disableBit(enabledSensors,SENSOR_BRIDGE_AMP);
			}
			else if((sensorToCheck & SENSOR_INT_ADC_A13) >0){
				enabledSensors = disableBit(enabledSensors,SENSOR_EXG1_16BIT);
				enabledSensors = disableBit(enabledSensors,SENSOR_EXG2_16BIT);
				enabledSensors = disableBit(enabledSensors,SENSOR_EXG1_24BIT);
				enabledSensors = disableBit(enabledSensors,SENSOR_EXG2_24BIT);
				enabledSensors = disableBit(enabledSensors,SENSOR_BRIDGE_AMP);
			}
			else if((sensorToCheck & SENSOR_INT_ADC_A14) >0){
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
	
	//TODO: MN -> replace with sensormap conflict checks? unfinished for Shimmer2R
	public boolean sensorConflictCheck(long enabledSensors){
		boolean pass=true;
		if(getHardwareVersion()!=HW_ID.SHIMMER_3){
			if(((enabledSensors & 0xFF)& SENSOR_GYRO) > 0){
				if(((enabledSensors & 0xFF)& SENSOR_EMG) > 0){
					pass=false;
				} 
				else if(((enabledSensors & 0xFF)& SENSOR_ECG) > 0){
					pass=false;
				} 
				else if(((enabledSensors & 0xFF)& SENSOR_GSR) > 0){
					pass=false;
				} 
				else if(((enabledSensors & 0xFF00) & SENSOR_BRIDGE_AMP) > 0){
					pass=false;
				}
			}

			if(((enabledSensors & 0xFF)& SENSOR_MAG) > 0){
				if(((enabledSensors & 0xFF)& SENSOR_EMG) > 0){
					pass=false;
				} 
				else if(((enabledSensors & 0xFF)& SENSOR_ECG) > 0){
					pass=false;
				} 
				else if(((enabledSensors & 0xFF)& SENSOR_GSR) > 0){
					pass=false;
				} 
				else if(((enabledSensors & 0xFF00) & SENSOR_BRIDGE_AMP) > 0){
					pass=false;
				}
			}

			if(((enabledSensors & 0xFF)& SENSOR_EMG) > 0){
				if(((enabledSensors & 0xFF)& SENSOR_GYRO) > 0){
					pass=false;
				} 
				else if(((enabledSensors & 0xFF)& SENSOR_MAG) > 0){
					pass=false;
				} 
				else if(((enabledSensors & 0xFF)& SENSOR_ECG) > 0){
					pass=false;
				} 
				else if(((enabledSensors & 0xFF)& SENSOR_GSR) > 0){
					pass=false;
				} 
				else if(((enabledSensors & 0xFF00) & SENSOR_BRIDGE_AMP) > 0){
					pass=false;
				}
			}

			if(((enabledSensors & 0xFF)& SENSOR_ECG) > 0){
				if(((enabledSensors & 0xFF)& SENSOR_GYRO) > 0){
					pass=false;
				} 
				else if(((enabledSensors & 0xFF)& SENSOR_MAG) > 0){
					pass=false;
				} 
				else if(((enabledSensors & 0xFF)& SENSOR_EMG) > 0){
					pass=false;
				} 
				else if(((enabledSensors & 0xFF)& SENSOR_GSR) > 0){
					pass=false;
				}
				else if(((enabledSensors & 0xFF00) & SENSOR_BRIDGE_AMP) > 0){
					pass=false;
				}
			}

			if(((enabledSensors & 0xFF)& SENSOR_GSR) > 0){
				if(((enabledSensors & 0xFF)& SENSOR_GYRO) > 0){
					pass=false;
				} 
				else if(((enabledSensors & 0xFF)& SENSOR_MAG) > 0){
					pass=false;
				} 
				else if(((enabledSensors & 0xFF)& SENSOR_EMG) > 0){
					pass=false;
				} 
				else if(((enabledSensors & 0xFF)& SENSOR_ECG) > 0){
					pass=false;
				}
				else if(((enabledSensors & 0xFF00) & SENSOR_BRIDGE_AMP) > 0){
					pass=false;
				}
			}

			if(((enabledSensors & 0xFF00) & SENSOR_BRIDGE_AMP) > 0){
				if(((enabledSensors & 0xFF)& SENSOR_GYRO) > 0){
					pass=false;
				} 
				else if(((enabledSensors & 0xFF)& SENSOR_MAG) > 0){
					pass=false;
				} 
				else if(((enabledSensors & 0xFF)& SENSOR_EMG) > 0){
					pass=false;
				} 
				else if(((enabledSensors & 0xFF)& SENSOR_ECG) > 0){
					pass=false;
				} 
				else if(((enabledSensors & 0xFF)& SENSOR_GSR) > 0){
					pass=false;
				} 
				else if(get5VReg()==1){ // if the 5volt reg is set 
					pass=false;
				}
			}

			if(((enabledSensors & 0xFF) & SENSOR_EXP_BOARD_A0) > 0) {
				if(((enabledSensors & 0xFFFFF) & SENSOR_BATT) > 0) {
					pass=false;
				} 
				else if(getPMux()==1){
					writePMux(0);
				}
			}

			if(((enabledSensors & 0xFF) & SENSOR_EXP_BOARD_A7) > 0) {
				if(((enabledSensors & 0xFFFFF) & SENSOR_BATT) > 0) {
					pass=false;
				}
				else if(getPMux()==1){
					writePMux(0);
				}
			}

			if(((enabledSensors & 0xFFFFF) & SENSOR_BATT) > 0) {
				if(((enabledSensors & 0xFF) & SENSOR_EXP_BOARD_A7) > 0){
					pass=false;
				} 
				if(((enabledSensors & 0xFF) & SENSOR_EXP_BOARD_A0) > 0){
					pass=false;
				}
				if(((enabledSensors & 0xFFFFF) & SENSOR_BATT) > 0){
					if(getPMux()==0){
						
						writePMux(1);
					}
				}
			}
			if(!pass){
				
			}
		}
		
		else{
			
			if(((enabledSensors & 0xFF0000) & SENSOR_EXG1_16BIT) > 0 || ((enabledSensors & 0xFF0000) & SENSOR_EXG2_16BIT) > 0){
				
				if(((enabledSensors & 0xFF00) & SENSOR_INT_ADC_A1) > 0){
					pass=false; 
				} 
				else if(((enabledSensors & 0xFF00) & SENSOR_INT_ADC_A12) > 0){
					pass=false;
				} 
				else if(((enabledSensors & 0xFF00) & SENSOR_INT_ADC_A13) > 0){
					pass=false;
				} 
				else if(((enabledSensors & 0xFF0000) & SENSOR_INT_ADC_A14) > 0){
					pass=false;
				} 
				else if(((enabledSensors & 0xFF) & SENSOR_GSR) > 0){
					pass=false;
				} 
				else if(((enabledSensors & 0xFF) & SENSOR_EXG1_24BIT) > 0){
					pass=false;
				} 
				else if(((enabledSensors & 0xFF) & SENSOR_EXG2_24BIT) > 0){
					pass=false;
				} 
				else if(((enabledSensors & 0xFF00) & SENSOR_BRIDGE_AMP) > 0){
					pass=false;
				}
			}
			
			if(((enabledSensors & 0xFF) & SENSOR_EXG1_24BIT) > 0 || ((enabledSensors & 0xFF) & SENSOR_EXG2_24BIT) > 0){
				
				if(((enabledSensors & 0xFF00) & SENSOR_INT_ADC_A1) > 0){
					pass=false; 
				} 
				else if(((enabledSensors & 0xFF00) & SENSOR_INT_ADC_A12) > 0){
					pass=false;
				} 
				else if(((enabledSensors & 0xFF00) & SENSOR_INT_ADC_A13) > 0){
					pass=false;
				} 
				else if(((enabledSensors & 0xFF0000) & SENSOR_INT_ADC_A14) > 0){
					pass=false;
				} 
				else if(((enabledSensors & 0xFF) & SENSOR_GSR) > 0){
					pass=false;
				} 
				else if(((enabledSensors & 0xFF) & SENSOR_EXG1_16BIT) > 0){
					pass=false;
				} 
				else if(((enabledSensors & 0xFF) & SENSOR_EXG2_16BIT) > 0){
					pass=false;
				} 
				else if(((enabledSensors & 0xFF00) & SENSOR_BRIDGE_AMP) > 0){
					pass=false;
				}
			}

			
			if(((enabledSensors & 0xFF) & SENSOR_GSR) > 0){
				
				if(((enabledSensors & 0xFF0000) & SENSOR_INT_ADC_A1) > 0){
					pass=false;
				} 
				else if(((enabledSensors & 0xFF0000) & SENSOR_INT_ADC_A14) > 0){
					pass=false;
				} 
				else if(((enabledSensors & 0xFF0000) & SENSOR_EXG1_16BIT) > 0){
					pass=false;
				} 
				else if(((enabledSensors & 0xFF0000) & SENSOR_EXG2_16BIT) > 0){
					pass=false;
				} 
				else if(((enabledSensors & 0xFF) & SENSOR_EXG1_24BIT) > 0){
					pass=false;
				} 
				else if(((enabledSensors & 0xFF) & SENSOR_EXG2_24BIT) > 0){
					pass=false;
				} 
				else if(((enabledSensors & 0xFF00) & SENSOR_BRIDGE_AMP) > 0){
					pass=false;
				}
			}
			
			if(((enabledSensors & 0xFF00) & SENSOR_BRIDGE_AMP) > 0){
				  
				if(((enabledSensors & 0xFF00) & SENSOR_INT_ADC_A12) > 0){
					pass=false;
				} 
				else if(((enabledSensors & 0xFF00) & SENSOR_INT_ADC_A13) > 0){
					pass=false;
				} 
				else if(((enabledSensors & 0xFF0000) & SENSOR_INT_ADC_A14) > 0){
					pass=false;
				} 
				else if(((enabledSensors & 0xFF) & SENSOR_GSR) > 0){
					pass=false;		
				} 
				else if(((enabledSensors & 0xFF0000) & SENSOR_EXG1_16BIT) > 0){
					pass=false;
				} 
				else if(((enabledSensors & 0xFF0000) & SENSOR_EXG2_16BIT) > 0){
					pass=false;
				} 
				else if(((enabledSensors & 0xFF) & SENSOR_EXG1_24BIT) > 0){
					pass=false;
				} 
				else if(((enabledSensors & 0xFF) & SENSOR_EXG2_24BIT) > 0){
					pass=false;
				}
			}
			
			if(((enabledSensors & 0xFF00) & SENSOR_INT_ADC_A1) > 0){
				  
				 if(((enabledSensors & 0xFF) & SENSOR_GSR) > 0){
					pass=false;
				 } 
				 else if(((enabledSensors & 0xFF0000) & SENSOR_EXG1_16BIT) > 0){
					pass=false;
				 } 
				 else if(((enabledSensors & 0xFF0000) & SENSOR_EXG2_16BIT) > 0){
					pass=false;
				 } 
				 else if(((enabledSensors & 0xFF) & SENSOR_EXG1_24BIT) > 0){
					pass=false;
				 } 
				 else if(((enabledSensors & 0xFF) & SENSOR_EXG2_24BIT) > 0){
					pass=false;
				 }
			}
			
			if(((enabledSensors & 0xFF00) & SENSOR_INT_ADC_A12) > 0){
				  
				if(((enabledSensors & 0xFF0000) & SENSOR_EXG1_16BIT) > 0){
					pass=false;
				} 
				else if(((enabledSensors & 0xFF0000) & SENSOR_EXG2_16BIT) > 0){
					pass=false;
				} 
				else if(((enabledSensors & 0xFF) & SENSOR_EXG1_24BIT) > 0){
					pass=false;
				} 
				else if(((enabledSensors & 0xFF) & SENSOR_EXG2_24BIT) > 0){
					pass=false;
				} 
				else if(((enabledSensors & 0xFF00) & SENSOR_BRIDGE_AMP) > 0){
					pass=false;
				}
			}
			
			if(((enabledSensors & 0xFF00) & SENSOR_INT_ADC_A13) > 0){
				  
				if(((enabledSensors & 0xFF0000) & SENSOR_EXG1_16BIT) > 0){
					pass=false;
				} 
				else if(((enabledSensors & 0xFF0000) & SENSOR_EXG2_16BIT) > 0){
					pass=false;
				} 
				else if(((enabledSensors & 0xFF) & SENSOR_EXG1_24BIT) > 0){
					pass=false;
				} 
				else if(((enabledSensors & 0xFF) & SENSOR_EXG2_24BIT) > 0){
					pass=false;
				} 
				else if(((enabledSensors & 0xFF00) & SENSOR_BRIDGE_AMP) > 0){
					pass=false;
				}
			}
			
			if(((enabledSensors & 0xFF0000) & SENSOR_INT_ADC_A14) > 0){
				  
				 if(((enabledSensors & 0xFF) & SENSOR_GSR) > 0){
					pass=false;
				 } 
				 else if(((enabledSensors & 0xFF0000) & SENSOR_EXG1_16BIT) > 0){
					pass=false;
				 } 
				 else if(((enabledSensors & 0xFF0000) & SENSOR_EXG2_16BIT) > 0){
					pass=false;
				 } 
				 else if(((enabledSensors & 0xFF) & SENSOR_EXG1_24BIT) > 0){
					pass=false;
				 } 
				 else if(((enabledSensors & 0xFF) & SENSOR_EXG2_24BIT) > 0){
					pass=false;
				 } 
				 else if(((enabledSensors & 0xFF00) & SENSOR_BRIDGE_AMP) > 0){
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
		if(getHardwareVersion()==HW_ID.SHIMMER_2R || getHardwareVersion()==HW_ID.SHIMMER_2){
			if(((enabledSensors & 0xFFFFF) & SENSOR_BATT) > 0 ){
				enabledSensors = enabledSensors & 0xFFFF;
				enabledSensors = enabledSensors|SENSOR_EXP_BOARD_A0|SENSOR_EXP_BOARD_A7;
			}
			hardwareSensorBitmap  = enabledSensors;
		} 
		else if(getHardwareVersion()==HW_ID.SHIMMER_3){
			if(((enabledSensors & 0xFF)& SENSOR_ACCEL) > 0){
				hardwareSensorBitmap = hardwareSensorBitmap|Configuration.Shimmer3.SensorBitmap.SENSOR_A_ACCEL;
			}
			if((enabledSensors & SENSOR_DACCEL) > 0){
				hardwareSensorBitmap = hardwareSensorBitmap|Configuration.Shimmer3.SensorBitmap.SENSOR_D_ACCEL;
			}
			if(((enabledSensors & 0xFF)& SENSOR_EXG1_24BIT) > 0){
				hardwareSensorBitmap = hardwareSensorBitmap|Configuration.Shimmer3.SensorBitmap.SENSOR_EXG1_24BIT;
			}
			if(((enabledSensors & 0xFF)& SENSOR_EXG2_24BIT) > 0){
				hardwareSensorBitmap = hardwareSensorBitmap|Configuration.Shimmer3.SensorBitmap.SENSOR_EXG2_24BIT;
			}

			if((enabledSensors& SENSOR_EXG1_16BIT) > 0){
				hardwareSensorBitmap = hardwareSensorBitmap|Configuration.Shimmer3.SensorBitmap.SENSOR_EXG1_16BIT;
			}
			if((enabledSensors & SENSOR_EXG2_16BIT) > 0){
				hardwareSensorBitmap = hardwareSensorBitmap|Configuration.Shimmer3.SensorBitmap.SENSOR_EXG2_16BIT;
			}
			if(((enabledSensors & 0xFF)& SENSOR_GYRO) > 0){
				hardwareSensorBitmap = hardwareSensorBitmap|Configuration.Shimmer3.SensorBitmap.SENSOR_GYRO;
			}
			if(((enabledSensors & 0xFF)& SENSOR_MAG) > 0){
				hardwareSensorBitmap = hardwareSensorBitmap|Configuration.Shimmer3.SensorBitmap.SENSOR_MAG;
			}
			if((enabledSensors & SENSOR_BATT) > 0){
				hardwareSensorBitmap = hardwareSensorBitmap|Configuration.Shimmer3.SensorBitmap.SENSOR_VBATT;
			}
			if((enabledSensors & SENSOR_EXT_ADC_A7) > 0){
				hardwareSensorBitmap = hardwareSensorBitmap|Configuration.Shimmer3.SensorBitmap.SENSOR_EXT_A7;
			}
			if((enabledSensors & SENSOR_EXT_ADC_A6) > 0){
				hardwareSensorBitmap = hardwareSensorBitmap|Configuration.Shimmer3.SensorBitmap.SENSOR_EXT_A6;
			}
			if((enabledSensors & SENSOR_EXT_ADC_A15) > 0){
				hardwareSensorBitmap = hardwareSensorBitmap|Configuration.Shimmer3.SensorBitmap.SENSOR_EXT_A15;
			}
			if((enabledSensors & SENSOR_INT_ADC_A1) > 0){
				hardwareSensorBitmap = hardwareSensorBitmap|Configuration.Shimmer3.SensorBitmap.SENSOR_INT_A1;
			}
			if((enabledSensors & SENSOR_INT_ADC_A12) > 0){
				hardwareSensorBitmap = hardwareSensorBitmap|Configuration.Shimmer3.SensorBitmap.SENSOR_INT_A12;
			}
			if((enabledSensors & SENSOR_INT_ADC_A13) > 0){
				hardwareSensorBitmap = hardwareSensorBitmap|Configuration.Shimmer3.SensorBitmap.SENSOR_INT_A13;
			}
			if((enabledSensors & SENSOR_INT_ADC_A14) > 0){
				hardwareSensorBitmap = hardwareSensorBitmap|Configuration.Shimmer3.SensorBitmap.SENSOR_INT_A14;
			}
			if  ((enabledSensors & SENSOR_BMP180) > 0){
				hardwareSensorBitmap = hardwareSensorBitmap|Configuration.Shimmer3.SensorBitmap.SENSOR_BMP180;
			} 
			if((enabledSensors & SENSOR_GSR) > 0){
				hardwareSensorBitmap = hardwareSensorBitmap|Configuration.Shimmer3.SensorBitmap.SENSOR_GSR;
			}
			if((enabledSensors & SENSOR_BRIDGE_AMP) > 0){
				hardwareSensorBitmap = hardwareSensorBitmap|Configuration.Shimmer3.SensorBitmap.SENSOR_BRIDGE_AMP;
			} 
		} 
		else { 
			hardwareSensorBitmap  = enabledSensors;
		}

		return hardwareSensorBitmap;
	}

	
	
	public void toggleLed() {
		writeInstruction(TOGGLE_LED_COMMAND);
		readLEDCommand();
	}
	
	@Override
	protected void checkBattery(){
		if(mIsStreaming ){
			if(getHardwareVersion()==HW_ID.SHIMMER_3 && getFirmwareVersionCode()==FW_ID.LOGANDSTREAM){
				if(!mWaitForAck) {	
					if(mVSenseBattMA.getMean()<mLowBattLimit*1000*0.8) {
						if(mCurrentLEDStatus!=2) {
							writeLEDCommand(2);
						}
					} 
					else if(mVSenseBattMA.getMean()<mLowBattLimit*1000) {
						if(mCurrentLEDStatus!=1) {
							writeLEDCommand(1);
						}
					} 
					else if(mVSenseBattMA.getMean()>mLowBattLimit*1000+100) { //+100 is to make sure the limits are different to prevent excessive switching when the batt value is at the threshold
						if(mCurrentLEDStatus!=0) {
							writeLEDCommand(0);
						}
					}

				}
			}
			if(getHardwareVersion()==HW_ID.SHIMMER_2R){
				if(!mWaitForAck) {	
					if(mVSenseBattMA.getMean()<mLowBattLimit*1000) {
						if(mCurrentLEDStatus!=1) {
							writeLEDCommand(1);
						}
					} 
					else if(mVSenseBattMA.getMean()>mLowBattLimit*1000+100) { //+100 is to make sure the limits are different to prevent excessive switching when the batt value is at the threshold
						if(mCurrentLEDStatus!=0) {
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

//	@Override
//	public Object setValueUsingGuiComponent(String componentName, Object valueToSet) {
//		return super.setValueUsingGuiComponent(componentName, valueToSet);
//	}


	public void setSendProgressReport(boolean send){
		mSendProgressReport = send;
	}
	
	/**
	 * @return the mListofInstructions
	 */
	public List<byte []> getListofInstructions() {
		return mListofInstructions;
	}
	
	private void writeInstruction(int commandValue) {
		writeInstruction(new byte[]{(byte) (commandValue&0xFF)});
	}

	public void writeInstruction(byte[] instruction){
		mListofInstructions.add(instruction);
	};

	public void writeInstructionFirst(byte[] instruction){
		mListofInstructions.add(0, instruction);
	};
	
	/**
	 * @return the mInstructionStackLock
	 */
	public boolean isInstructionStackLock() {
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
	public byte[] generateInfoMemBytesForWritingToShimmer() {
		super.setConfigFileCreationFlag(true);
		return super.infoMemByteArrayGenerate(true);
	}

//	public void clearShimmerVersionInfo() {
//		setShimmerVersionInfoAndCreateSensorMap(new ShimmerVerObject());
//	}
	
//	public void setExpansionBoardDetails(ExpansionBoardDetails eBD){
//		super.mExpansionBoardDetails  = eBD;
//		super.mExpansionBoardId = eBD.mExpBoardId;
//		super.mExpansionBoardRev = eBD.mExpBoardRev;
//		super.mExpansionBoardRevSpecial = eBD.mExpBoardRevSpecial;
//		super.mExpansionBoardParsed = eBD.mExpBoardParsed;
//		super.mExpansionBoardParsedWithVer = eBD.mExpBoardParsedWithVer;
//		super.mExpBoardArray = eBD.mExpBoardArray;
//	}
	
	@Override
	public void setBattStatusDetails(ShimmerBattStatusDetails s) {
		super.setBattStatusDetails(s);
		
		batteryStatusChanged();
	}

	@Override
	public void clearBattStatusDetails() {
		super.clearBattStatusDetails();
		
		batteryStatusChanged();
	}
	
	public void setUseInfoMemConfigMethod(boolean useInfoMemConfigMethod) {
		this.mUseInfoMemConfigMethod = useInfoMemConfigMethod;
	}
	
	public boolean isUseInfoMemConfigMethod() {
		return this.mUseInfoMemConfigMethod;
	}

	/**
	 * @param hardwareVersion
	 * @param firmwareIdentifier
	 * @param firmwareVersionMajor
	 * @param firmwareVersionMinor
	 * @param firmwareVersionInternal
	 * @return
	 */
	public boolean isThisVerCompatibleWith(int hardwareVersion, int firmwareIdentifier, int firmwareVersionMajor, int firmwareVersionMinor, int firmwareVersionInternal){
		return UtilShimmer.compareVersions(getHardwareVersion(), getFirmwareIdentifier(), getFirmwareVersionMajor(), getFirmwareVersionMinor(), getFirmwareVersionInternal(),
				hardwareVersion, firmwareIdentifier, firmwareVersionMajor, firmwareVersionMinor, firmwareVersionInternal);
	}


//	private void consolePrintLn(String message) {
//		if(mVerboseMode) {
//			Calendar rightNow = Calendar.getInstance();
//			String rightNowString = "[" + String.format("%02d",rightNow.get(Calendar.HOUR_OF_DAY)) 
//					+ ":" + String.format("%02d",rightNow.get(Calendar.MINUTE)) 
//					+ ":" + String.format("%02d",rightNow.get(Calendar.SECOND)) 
//					+ ":" + String.format("%03d",rightNow.get(Calendar.MILLISECOND)) + "]";
//			System.out.println(rightNowString + " " + mParentClassName + ": " + mComPort + " " + getMacIdFromBtParsed() + " " + message);
//		}		
//	}
//	
//	public void consolePrint(String message) {
//		if(mVerboseMode) {
//			System.out.print(message);
//		}		
//	}
//
//	public void setVerboseMode(boolean verboseMode) {
//		mVerboseMode = verboseMode;
//	}
	
	
}