package com.shimmerresearch.comms.radioProtocol;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;

import org.apache.commons.lang3.ArrayUtils;

import com.shimmerresearch.bluetooth.BluetoothProgressReportPerCmd;
import com.shimmerresearch.bluetooth.RawBytePacketWithPCTimeStamp;
import com.shimmerresearch.bluetooth.ShimmerBluetooth.BT_STATE;
import com.shimmerresearch.comms.radioProtocol.ShimmerLiteProtocolInstructionSet.LiteProtocolInstructionSet.*;
import com.shimmerresearch.comms.radioProtocol.ShimmerLiteProtocolInstructionSet.LiteProtocolInstructionSet;
import com.shimmerresearch.comms.radioProtocol.ShimmerLiteProtocolInstructionSet.LiteProtocolInstructionSet.InstructionsResponse;
import com.shimmerresearch.comms.serialPortInterface.InterfaceByteLevelDataComm;
import com.shimmerresearch.driver.DeviceException;
import com.shimmerresearch.driverUtilities.ExpansionBoardDetails;
import com.shimmerresearch.driverUtilities.ShimmerBattStatusDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;
import com.shimmerresearch.driverUtilities.UtilShimmer;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.FW_ID;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.HW_ID;

public class LiteProtocol extends AbstractByteLevelProtocol{

	
	//TODO - items that I've had to comment out
	//getFirmwareVersionCode()
	
	
	protected List<byte []> mListofInstructions = new  ArrayList<byte[]>();
	protected int mCurrentCommand;
	
//	/**
//	 * LogAndStream will try to recreate the SD config. file for each block of
//	 * InfoMem that is written - need to give it time to do so.
//	 */
//	private static final int DELAY_BETWEEN_INFOMEM_WRITES = 100;
//	/** Delay to allow LogAndStream to create SD config. file and reinitialise */
//	private static final int DELAY_AFTER_INFOMEM_WRITE = 500;
//	private int mNumOfInfoMemSetCmds = 0;

	transient protected IOThread mIOThread;
	private boolean mInstructionStackLock = false;
	protected boolean mWaitForAck=false;                                          // This indicates whether the device is waiting for an acknowledge packet from the Shimmer Device  
	protected boolean mWaitForResponse=false; 									// This indicates whether the device is waiting for a response packet from the Shimmer Device 
	protected boolean mTransactionCompleted=true;									// Variable is used to ensure a command has finished execution prior to executing the next command (see initialize())
	private final int ACK_TIMER_DURATION = 10; 									// Duration to wait for an ack packet (seconds)
	protected boolean mDummy=false;
	transient ByteArrayOutputStream mByteArrayOutputStream = new ByteArrayOutputStream();
	public long mPacketReceivedCount = 0; 	//Used by ShimmerGQ
	public long mPacketExpectedCount = 0; 	//Used by ShimmerGQ
	protected long mPacketLossCount = 0;		//Used by ShimmerBluetooth
	protected double mPacketReceptionRate = 100;
	protected double mPacketReceptionRateCurrent = 100;
	ArrayBlockingQueue<RawBytePacketWithPCTimeStamp> mABQPacketByeArray = new ArrayBlockingQueue<RawBytePacketWithPCTimeStamp>(10000);
	List<Long> mListofPCTimeStamps = new ArrayList<Long>();
	private int mNumberofTXRetriesCount=1;
	private final static int NUMBER_OF_TX_RETRIES_LIMIT = 0;
	protected Stack<Byte> byteStack = new Stack<Byte>();
	protected boolean mOperationUnderway = false;
	private int mTempChipID;

	/**
	 * LogAndStream will try to recreate the SD config. file for each block of
	 * InfoMem that is written - need to give it time to do so.
	 */
	private static final int DELAY_BETWEEN_INFOMEM_WRITES = 100;
	/** Delay to allow LogAndStream to create SD config. file and reinitialise */
	private static final int DELAY_AFTER_INFOMEM_WRITE = 500;

	//startregion --------- TIMERS ---------
	private int mReadStatusPeriod=5000;
	private int mReadBattStatusPeriod=600000;	// Batt status is updated every 10 mins 
	private int mCheckAlivePeriod=2000;
	
	transient protected Timer mTimerCheckForAckOrResp;								// Timer variable used when waiting for an ack or response packet
	transient protected Timer mTimerCheckAlive;
	transient protected Timer mTimerReadStatus;
	transient protected Timer mTimerReadBattStatus;								// 
	
	private int mCountDeadConnection = 0;
	private boolean mCheckIfConnectionisAlive = true;
	//endregion --------- TIMERS ---------


	//TODO Should not be here?
	public String mMyBluetoothAddress;
	public String mComPort;
	
	//States -> these need to be synced with the same booleans in the parent device
	public boolean mIsSensing = false;
	public boolean mIsSDLogging = false;											// This is used to monitor whether the device is in sd log mode
	public boolean mIsStreaming = false;											// This is used to monitor whether the device is in streaming mode
	public boolean mIsInitialised = false;
	public boolean mIsDocked = false;
	public boolean mHaveAttemptedToReadConfig = false;

	//TODO Should not be here
	protected String mDirectoryName;
	protected int numBytesToReadFromExpBoard=0;
	
	//Local copy of 
	public int mHardwareVersion;
	private ShimmerVerObject mShimmerVerObject;

	
	public UtilShimmer mUtilShimmer = new UtilShimmer(getClass().getSimpleName(), true);
	
	private boolean isShimmerBluetoothApproach = true;
	
	public LiteProtocol(){
		super();
	}
	
	public LiteProtocol(InterfaceByteLevelDataComm mSerialPort) {
		super(mSerialPort);
//		String uniqueId = mSerialPort.
	}
	
	private void writeInstruction(int commandValue) {
		writeInstruction(new byte[]{(byte) (commandValue&0xFF)});
	}
	
	@Override
	public void writeInstruction(byte[] instruction){
		getListofInstructions().add(instruction);
	};
	
	
	/**
	 * @return the mFirmwareIdentifier
	 */
	public int getFirmwareIdentifier() {
		if(mShimmerVerObject!=null){
			return mShimmerVerObject.getFirmwareIdentifier();
		}
		return FW_ID.UNKNOWN;
	}

	public int getShimmerVersion() {
		return mHardwareVersion;
	}

	/**
	 * @return the mInstructionStackLock
	 */
	public boolean isInstructionStackLock() {
		return mInstructionStackLock;
	}
	
	/**
	 * @return the mListofInstructions
	 */
	public List<byte []> getListofInstructions() {
		return mListofInstructions;
	}
	
	/**
	 * @param state the mInstructionStackLock to set
	 */
	@Override
	public void setInstructionStackLock(boolean state) {
		this.mInstructionStackLock = state;
	}
	
	/**this is to clear the buffer
	 * @throws DeviceException 
	 * 
	 */
	private void clearSerialBuffer() throws DeviceException {
		if(bytesAvailableToBeRead()){
			byte[] buffer;
			try {
				buffer = readBytes(availableBytes());
			
			printLogDataForDebugging("Discarding:\t\t" + UtilShimmer.bytesToHexStringWithSpacesFormatted(buffer));
			} catch (DeviceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		while (availableBytes()!=0){
			int available = availableBytes();
			if (bytesAvailableToBeRead()){
				byte[] tb=readBytes(1);
				String msg = "First Time : " + Arrays.toString(tb);
				printLogDataForDebugging(msg);
			}
		}		
	}
	
	public double getPacketReceptionRate(){
		return mPacketReceptionRate;
	}

	@Override
	public void initialize() {
		mIamAlive = false;
		getListofInstructions().clear();
		mFirstTime=true;

		if (mIOThread != null) { 
			mIOThread = null;
		}

		mIOThread = new IOThread();
		mIOThread.setName(getClass().getSimpleName()+"-"+mMyBluetoothAddress+"-"+mComPort);
		mIOThread.start();
		
		stopTimerReadStatus();
		stopTimerCheckForAckOrResp();
		setInstructionStackLock(false);
		
		dummyreadSamplingRate(); // it actually acts to clear the write buffer
		readShimmerVersionNew();
	}

	@Override
	public void stop() {
		setIsStreaming(false);
		setIsInitialised(false);

		if(mIOThread!=null){
			mIOThread.stop=true;
			mIOThread = null;
		}
	}

	
	public class IOThread extends Thread {
		byte[] byteBuffer = {0};
		public boolean stop = false;
		
		public synchronized void run() {
			while (!stop) {
				try {
					// Process Instruction on stack. is an instruction running? if not proceed
					if(!isInstructionStackLock()){
						processNextInstruction();
					}
				
					if(isStreaming()){
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
				} catch (DeviceException e) {
					// TODO Auto-generated catch block
					stop=true;
					e.printStackTrace();
					
					//TODO send event up the ladder
				}
			} 
		} 
		
		private void processNextInstruction() throws DeviceException {
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
					mCurrentCommand=((int)insBytes[0])&0xFF;
					setInstructionStackLock(true);
					mWaitForAck=true;
					
					if(!isStreaming()){
						clearSerialBuffer();
					}
					//Special cases
					if(mCurrentCommand==InstructionsSet.SET_RWC_COMMAND_VALUE){
						// for Real-world time -> grab PC time just before
						// writing to Shimmer
						byte[] rtcTimeArray = UtilShimmer.convertSystemTimeToShimmerRtcDataBytes(System.currentTimeMillis());
						System.arraycopy(rtcTimeArray, 0, insBytes, 1, 8);
					}
					
					//TODO: are the two stops needed here? better to wait for ack from Shimmer
					if(mCurrentCommand==InstructionsSet.STOP_STREAMING_COMMAND_VALUE
							|| mCurrentCommand==InstructionsSet.STOP_SDBT_COMMAND_VALUE){
						//DO NOTHING
					} 
					else {
						// Overwritten for commands that aren't supported
						// for older versions of Shimmer
						if(mCurrentCommand==InstructionsSet.SET_SENSORS_COMMAND_VALUE
								&& getShimmerVersion()==HW_ID.SHIMMER_2R){
							startTimerCheckForAckOrResp(ACK_TIMER_DURATION+8);
						}
						else if((mCurrentCommand==InstructionsGet.GET_FW_VERSION_COMMAND_VALUE)
								||(mCurrentCommand==InstructionsGet.GET_SAMPLING_RATE_COMMAND_VALUE)
								||(mCurrentCommand==InstructionsGet.GET_SHIMMER_VERSION_COMMAND_NEW_VALUE)){
							startTimerCheckForAckOrResp(ACK_TIMER_DURATION);
						}
						else {
							if(isStreaming()){
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
					if(mCurrentCommand==InstructionsSet.STOP_STREAMING_COMMAND_VALUE
							|| mCurrentCommand==InstructionsSet.STOP_SDBT_COMMAND_VALUE){
						setIsStreaming(false);
						if (mCurrentCommand==InstructionsSet.STOP_SDBT_COMMAND_VALUE){
							setIsSDLogging(false);
						}
						
						if(!isShimmerBluetoothApproach){
						//TODO 2016-07-06 MN removed to make consistent with ShimmerBluetooth implementation 
//							eventAckReceived((byte[]) getListofInstructions().get(0)); //DUMMY
//							hasStopStreaming();
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
			} 
			else {
				if (!isStreaming() && !bytesAvailableToBeRead()){
					threadSleep(50);
				}
			}
		}

		private void processWhileStreaming() throws DeviceException {
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
			if(bufferTemp[0]==InstructionsSet.DATA_PACKET_VALUE 
					&& bufferTemp[mPacketSize+1]==InstructionsSet.DATA_PACKET_VALUE){
				//Handle the data packet
				processDataPacket(bufferTemp);
				clearSingleDataPacketFromBuffers(bufferTemp, mPacketSize+1);
			} 
			
			//Data packet followed by an ACK (suggesting an ACK in response to a SET BT command or else a BT response command)
			else if(bufferTemp[0]==InstructionsSet.DATA_PACKET_VALUE 
					&& bufferTemp[mPacketSize+1]==InstructionsSet.ACK_COMMAND_PROCESSED_VALUE){
				if(mByteArrayOutputStream.size()>mPacketSize+2){
					
					if(bufferTemp[mPacketSize+2]==InstructionsSet.DATA_PACKET_VALUE){
						//Firstly handle the data packet
						processDataPacket(bufferTemp);
						clearSingleDataPacketFromBuffers(bufferTemp, mPacketSize+2);
						
						//Then handle the ACK from the last SET command
						if(isKnownSetCommand(mCurrentCommand)){
							stopTimerCheckForAckOrResp(); //cancel the ack timer
							mWaitForAck=false;
							
							processAckFromSetCommand(((int)mCurrentCommand)&0xFF);
							
							mTransactionCompleted = true;
							setInstructionStackLock(false);
						}
						printLogDataForDebugging("Ack Received for Command: \t\t\t" + btCommandToString(mCurrentCommand));
					}
					
					//this is for LogAndStream support, command is transmitted and ack received
					else if(((getHardwareVersion()==HW_ID.SHIMMER_3 && getFirmwareIdentifier()==FW_ID.LOGANDSTREAM)
							||(getHardwareVersion()==HW_ID.SHIMMER_4_SDK && getFirmwareIdentifier()==FW_ID.SHIMMER4_SDK_STOCK))
							&& bufferTemp[mPacketSize+2]==((byte)(InstructionsResponse.INSTREAM_CMD_RESPONSE_VALUE))){ 
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
		private void processNotStreamingWaitForAck() throws DeviceException {
			//JC TEST:: IMPORTANT TO REMOVE // This is to simulate packet loss 
			/*
			if (Math.random()>0.9 && mIsInitialised==true){
				if(bytesAvailableToBeRead() && mCurrentCommand!=TEST_CONNECTION_COMMAND	&& mCurrentCommand!=GET_STATUS_COMMAND	&& mCurrentCommand!= GET_VBATT_COMMAND && mCurrentCommand!=START_STREAMING_COMMAND&& mCurrentCommand!=STOP_STREAMING_COMMAND && mCurrentCommand!=SET_RWC_COMMAND && mCurrentCommand!=GET_RWC_COMMAND){
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
				if(mCurrentCommand==InstructionsSet.STOP_STREAMING_COMMAND_VALUE 
						|| mCurrentCommand==InstructionsSet.STOP_SDBT_COMMAND_VALUE) { //due to not receiving the ack from stop streaming command we will skip looking for it.
					stopTimerCheckForAckOrResp();
					setIsStreaming(false);
					mTransactionCompleted=true;
					mWaitForAck=false;
					
					delayForBtResponse(200); // Wait to ensure the packet has been fully received
					byteStack.clear();

					clearSerialBuffer();
					
					hasStopStreaming();					
					getListofInstructions().remove(0);
					getListofInstructions().removeAll(Collections.singleton(null));
					if (mCurrentCommand==InstructionsSet.STOP_SDBT_COMMAND_VALUE){
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
					if((((int)byteBuffer[0])&0xFF)==InstructionsSet.ACK_COMMAND_PROCESSED_VALUE) {

						mWaitForAck=false;
						printLogDataForDebugging("Ack Received for Command: \t\t" + btCommandToString(mCurrentCommand));

						// Send status report if needed by the
						// application and is not one of the below
						// commands that are triggered by timers
						if(mCurrentCommand!=InstructionsGet.GET_STATUS_COMMAND_VALUE 
								&& mCurrentCommand!=InstructionsSet.TEST_CONNECTION_COMMAND_VALUE 
								&& mCurrentCommand!=InstructionsSet.SET_BLINK_LED_VALUE
								//&& mCurrentCommand!= GET_VBATT_COMMAND
								&& mOperationUnderway){
							sendProgressReport(new BluetoothProgressReportPerCmd(mCurrentCommand, getListofInstructions().size(), mMyBluetoothAddress, mComPort));
						}
						
						// Process if currentCommand is a SET command
						if(isKnownSetCommand(mCurrentCommand)){
							stopTimerCheckForAckOrResp(); //cancel the ack timer
							
							//TODO 2016-07-06 MN removed to make consistent with ShimmerBluetooth implementation
							if(!isShimmerBluetoothApproach){
//								byte[] insBytes = getListofInstructions().get(0);
//								eventAckReceived(insBytes);
							}
							
							processAckFromSetCommand(((int)mCurrentCommand)&0xFF);
							mTransactionCompleted = true;
							setInstructionStackLock(false);
						}
						
						// Process if currentCommand is a GET command
						else if(isKnownGetCommand(mCurrentCommand)){
							//Special cases
							processSpecialGetCmdsAfterAck(((int)mCurrentCommand)&0xFF);
							mWaitForResponse=true;
							getListofInstructions().remove(0);
						}
						
					}
				}
			}
		}

		/** Process RESPONSE while not streaming */ 
		private void processNotStreamingWaitForResp() throws DeviceException {
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
					
					if(isShimmerBluetoothApproach){
						processResponseCommand(((int)responseCommand)&0xFF);
					}
					else {
//						processResponseCommand(responseCommand, byteBuffer);
					}
					//JD: only stop timer after process because there are readbyte opeartions in the processresponsecommand
					stopTimerCheckForAckOrResp(); //cancel the ack timer
					mWaitForResponse=false;
					mTransactionCompleted=true;
					setInstructionStackLock(false);
					printLogDataForDebugging("Response Received:\t\t\t" + btCommandToString(responseCommand));
					
					// Special case for FW_VERSION_RESPONSE because it
					// needs to initialize the Shimmer after releasing
					// the setInstructionStackLock
					if(byteBuffer[0]==InstructionsResponse.FW_VERSION_RESPONSE_VALUE){
//						processFirmwareVerResponse();
						eventNewResponse(byteBuffer[0], null);
					}
				}
			}
		}

		/** Process LogAndStream INSTREAM_CMD_RESPONSE while not streaming */ 
		private void processBytesAvailableAndInstreamSupported() throws DeviceException {
			if(((getHardwareVersion()==HW_ID.SHIMMER_3 && getFirmwareIdentifier()==FW_ID.LOGANDSTREAM) 
					|| (getHardwareVersion()==HW_ID.SHIMMER_4_SDK && getFirmwareIdentifier()==FW_ID.SHIMMER4_SDK_STOCK))
					&& !mWaitForAck 
					&& !mWaitForResponse 
					&& bytesAvailableToBeRead()) {
				
				byteBuffer=readBytes(1);
				if(byteBuffer != null){
					if(byteBuffer[0]==InstructionsSet.ACK_COMMAND_PROCESSED_VALUE) {
						printLogDataForDebugging("ACK RECEIVED , Connected State!!");
						byteBuffer = readBytes(1);
						if (byteBuffer!=null){ //an android fix. not fully investigated (JC)
							if(byteBuffer[0]==InstructionsSet.ACK_COMMAND_PROCESSED_VALUE){
								byteBuffer = readBytes(1);
							}
							if(byteBuffer[0]==InstructionsResponse.INSTREAM_CMD_RESPONSE_VALUE){
								processInstreamResponse();
							}
						}
					}
				}
				
				clearSerialBuffer();
			}
		}

	}

//	@Deprecated
//	private void processResponseCommand(byte responseCommand, byte[] tb) throws DeviceException {
//		// response have to read bytes and return the values
//		 int lengthOfResponse = (int)InstructionsResponse.valueOf(responseCommand&0xff).getValueDescriptor().getOptions().getField(LiteProtocolInstructionSet.getDescriptor().findFieldByName("response_size"));
//		if (lengthOfResponse ==-1){
//			lengthOfResponse = (int)(readBytes(1)[0] & 0xFF);
//		} 
//		byte[] response = readBytes(lengthOfResponse);
//		response = ArrayUtils.addAll(tb,response);
//		eventNewResponse(response);
//	}
	
	/**
	 * @param responseCommand
	 */
	private void processResponseCommand(int responseCommand) {
		byte[] length = null;
		byte[] rxBuf = null;
		
		try{
			if(responseCommand==InstructionsResponse.INQUIRY_RESPONSE_VALUE){
				delayForBtResponse(500); // Wait to ensure the packet has been fully received
				List<Byte> buffer = new  ArrayList<Byte>();
				//JC TEST:: IMPORTANT TO REMOVE // This is to simulate packet loss
				/*
				if (Math.random()>0.5 && mIsInitialised==true){
					if(bytesAvailableToBeRead() && mCurrentCommand!=TEST_CONNECTION_COMMAND	&& mCurrentCommand!=GET_STATUS_COMMAND	&& mCurrentCommand!= GET_VBATT_COMMAND && mCurrentCommand!=START_STREAMING_COMMAND&& mCurrentCommand!=STOP_STREAMING_COMMAND && mCurrentCommand!=SET_RWC_COMMAND && mCurrentCommand!=GET_RWC_COMMAND){
						readByte();
					}
				}
				*/
				//JC TEST:: IMPORTANT TO REMOVE // This is to simulate packet loss
				
				//Shimmer3
				int lengthSettings = 8;// get Sampling rate, accel range, config setup byte0, num chans and buffer size
				int lengthChannels = 6;// read each channel type for the num channels
				if(!(getHardwareVersion()==HW_ID.SHIMMER_3)
						&&!(getHardwareVersion()==HW_ID.SHIMMER_4_SDK)) {
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

               rxBuf = new byte[buffer.size()];
				for (int i = 0; i < rxBuf.length; i++) {
					rxBuf[i] = (byte) buffer.get(i);
				}
					
				printLogDataForDebugging("Inquiry Response Received: " + UtilShimmer.bytesToHexStringWithSpacesFormatted(rxBuf));
				
//				interpretInqResponse(rxBuf);
//				prepareAllAfterConfigRead();
//				inquiryDone();
				eventNewResponse(responseCommand, rxBuf);
			}
//			else if(responseCommand==InstructionsResponse.SAMPLING_RATE_RESPONSE_VALUE){
//				if(!mIsStreaming) {
//					if(getHardwareVersion()==HW_ID.SHIMMER_2R || getHardwareVersion()==HW_ID.SHIMMER_2){    
//						byte[] bufferSR = readBytes(1);
//						if(mCurrentCommand==GET_SAMPLING_RATE_COMMAND) { // this is a double check, not necessary 
//							double val=(double)(bufferSR[0] & (byte) ACK_COMMAND_PROCESSED);
//							setSamplingRateShimmer(1024/val);
//						}
//					} 
//					else if(getHardwareVersion()==HW_ID.SHIMMER_3){
//						byte[] bufferSR = readBytes(2); //read the sampling rate
//						setSamplingRateShimmer(32768/(double)((int)(bufferSR[0] & 0xFF) + ((int)(bufferSR[1] & 0xFF) << 8)));
//					}
//				}
//
//				printLogDataForDebugging("Sampling Rate Response Received: " + Double.toString(getSamplingRateShimmer()));
//			}
			else if(responseCommand==InstructionsResponse.FW_VERSION_RESPONSE_VALUE){
				delayForBtResponse(200); // Wait to ensure the packet has been fully received
				rxBuf = readBytes(6);
				int firmwareIdentifier=(int)((rxBuf[1]&0xFF)<<8)+(int)(rxBuf[0]&0xFF);
				int firmwareVersionMajor = (int)((rxBuf[3]&0xFF)<<8)+(int)(rxBuf[2]&0xFF);
				int firmwareVersionMinor = ((int)((rxBuf[4]&0xFF)));
				int firmwareVersionInternal=(int)(rxBuf[5]&0xFF);
//				ShimmerVerObject shimmerVerObject = new ShimmerVerObject(getHardwareVersion(), firmwareIdentifier, firmwareVersionMajor, firmwareVersionMinor, firmwareVersionInternal);
//				setShimmerVersionInfoAndCreateSensorMap(shimmerVerObject);

				ShimmerVerObject shimmerVerObject = new ShimmerVerObject(getHardwareVersion(), firmwareIdentifier, firmwareVersionMajor, firmwareVersionMinor, firmwareVersionInternal);
				this.mShimmerVerObject = shimmerVerObject; // store a local copy
				eventNewResponse(responseCommand, shimmerVerObject);
				
				printLogDataForDebugging("FW Version Response Received. FW Code: " + shimmerVerObject.getFirmwareVersionCode());
				printLogDataForDebugging("FW Version Response Received: " + shimmerVerObject.getFirmwareVersionParsed());
			}

//			else if(responseCommand==InstructionsResponse.ALL_CALIBRATION_RESPONSE_VALUE){
//				if(getHardwareVersion()==HW_ID.SHIMMER_3){
//					processAccelCalReadBytes();
//					processGyroCalReadBytes();
//					processMagCalReadBytes();
//					processLsm303dlhcAccelCalReadBytes();
//				} 
//				else { //Shimmer2R etc.
//					processAccelCalReadBytes();
//					processGyroCalReadBytes();
//					processMagCalReadBytes();
//					processShimmer2EmgCalReadBytes();
//					processShimmer2EcgCalReadBytes();
//				}
//			}
//			else if(responseCommand==InstructionsResponse.ACCEL_CALIBRATION_RESPONSE_VALUE){
//				processAccelCalReadBytes();
//			}
//			else if(responseCommand==InstructionsResponse.GYRO_CALIBRATION_RESPONSE_VALUE){
//				processGyroCalReadBytes();
//			}
//			else if(responseCommand==InstructionsResponse.MAG_CALIBRATION_RESPONSE_VALUE){
//				processMagCalReadBytes();
//			}
//			else if(responseCommand==InstructionsResponse.ECG_CALIBRATION_RESPONSE_VALUE){
//				processShimmer2EcgCalReadBytes();
//			}
//			else if(responseCommand==InstructionsResponse.EMG_CALIBRATION_RESPONSE_VALUE){
//				processShimmer2EmgCalReadBytes();
//			}
//			else if(responseCommand==InstructionsResponse.LSM303DLHC_ACCEL_CALIBRATION_RESPONSE_VALUE){
//				processLsm303dlhcAccelCalReadBytes();
//			}
//			else if(responseCommand==InstructionsResponse.CONFIG_BYTE0_RESPONSE_VALUE){
//				if(getHardwareVersion()==HW_ID.SHIMMER_2R || getHardwareVersion()==HW_ID.SHIMMER_2){    
//					byte[] bufferConfigByte0 = readBytes(1);
//					mConfigByte0 = bufferConfigByte0[0] & 0xFF;
//				} 
//				else {
//					byte[] bufferConfigByte0 = readBytes(4);
//					mConfigByte0 = ((long)(bufferConfigByte0[0] & 0xFF) +((long)(bufferConfigByte0[1] & 0xFF) << 8)+((long)(bufferConfigByte0[2] & 0xFF) << 16) +((long)(bufferConfigByte0[3] & 0xFF) << 24));
//				}
//			}
//			else if(responseCommand==InstructionsResponse.DERIVED_CHANNEL_BYTES_RESPONSE_VALUE){
//				byte[] byteArray = readBytes(3);
//				mDerivedSensors=(long)(((byteArray[2]&0xFF)<<16) + ((byteArray[1]&0xFF)<<8)+(byteArray[0]&0xFF));
//				if (mEnabledSensors!=0){
//					prepareAllAfterConfigRead();
//					inquiryDone();
//				}
//			}
			else if(responseCommand==InstructionsResponse.GET_SHIMMER_VERSION_RESPONSE_VALUE){
				delayForBtResponse(100); // Wait to ensure the packet has been fully received
				byte[] bufferShimmerVersion = new byte[1]; 
				bufferShimmerVersion = readBytes(1);
				
				setHardwareVersion((int)bufferShimmerVersion[0]); //Save a local copy
				eventNewResponse(responseCommand, (int)bufferShimmerVersion[0]);

//				if(mShimmerVersion==HW_ID.SHIMMER_2R){
//					initializeShimmer2R();
//				} 
//				else if(mShimmerVersion==HW_ID.SHIMMER_3) {
//					initializeShimmer3();
//				}
				
				printLogDataForDebugging("Shimmer Version (HW) Response Received: " + UtilShimmer.bytesToHexStringWithSpacesFormatted(bufferShimmerVersion));
				
				readFWVersion();
			}
//			else if(responseCommand==InstructionsResponse.ACCEL_SENSITIVITY_RESPONSE_VALUE){
//				byte[] bufferAccelSensitivity = readBytes(1);
//				mAccelRange=bufferAccelSensitivity[0];
//				if(mDefaultCalibrationParametersAccel){
//					if(getHardwareVersion()!=HW_ID.SHIMMER_3){
//						if(getAccelRange()==0){
//							mSensitivityMatrixAnalogAccel = SensitivityMatrixAccel1p5gShimmer2; 
//						} 
//						else if(getAccelRange()==1){
//							mSensitivityMatrixAnalogAccel = SensitivityMatrixAccel2gShimmer2; 
//						} 
//						else if(getAccelRange()==2){
//							mSensitivityMatrixAnalogAccel = SensitivityMatrixAccel4gShimmer2; 
//						} 
//						else if(getAccelRange()==3){
//							mSensitivityMatrixAnalogAccel = SensitivityMatrixAccel6gShimmer2; 
//						}
//					} 
//					else if(getHardwareVersion()==HW_ID.SHIMMER_3){
//						if(getAccelRange()==0){
//							mSensitivityMatrixAnalogAccel = SensitivityMatrixLowNoiseAccel2gShimmer3;
//							mAlignmentMatrixAnalogAccel = AlignmentMatrixLowNoiseAccelShimmer3;
//							mOffsetVectorAnalogAccel = OffsetVectorLowNoiseAccelShimmer3;
//						} 
//						else if(getAccelRange()==1){
//							mSensitivityMatrixAnalogAccel = SensitivityMatrixWideRangeAccel4gShimmer3;
//							mAlignmentMatrixAnalogAccel = AlignmentMatrixWideRangeAccelShimmer3;
//							mOffsetVectorAnalogAccel = OffsetVectorWideRangeAccelShimmer3;
//						} 
//						else if(getAccelRange()==2){
//							mSensitivityMatrixAnalogAccel = SensitivityMatrixWideRangeAccel8gShimmer3;
//							mAlignmentMatrixAnalogAccel = AlignmentMatrixWideRangeAccelShimmer3;
//							mOffsetVectorAnalogAccel = OffsetVectorWideRangeAccelShimmer3;
//						} 
//						else if(getAccelRange()==3){
//							mSensitivityMatrixAnalogAccel = SensitivityMatrixWideRangeAccel16gShimmer3;
//							mAlignmentMatrixAnalogAccel = AlignmentMatrixWideRangeAccelShimmer3;
//							mOffsetVectorAnalogAccel = OffsetVectorWideRangeAccelShimmer3;
//						}
//					}
//				}
//			}
//			else if(responseCommand==InstructionsResponse.MPU9150_GYRO_RANGE_RESPONSE_VALUE){
//				byte[] bufferGyroSensitivity = readBytes(1);
//				mGyroRange=bufferGyroSensitivity[0];
//				if(mDefaultCalibrationParametersGyro){
//					if(getHardwareVersion()==HW_ID.SHIMMER_3){
//						mAlignmentMatrixGyroscope = AlignmentMatrixGyroShimmer3;
//						mOffsetVectorGyroscope = OffsetVectorGyroShimmer3;
//						if(mGyroRange==0){
//							mSensitivityMatrixGyroscope = SensitivityMatrixGyro250dpsShimmer3;
//						} 
//						else if(mGyroRange==1){
//							mSensitivityMatrixGyroscope = SensitivityMatrixGyro500dpsShimmer3;
//						} 
//						else if(mGyroRange==2){
//							mSensitivityMatrixGyroscope = SensitivityMatrixGyro1000dpsShimmer3;
//						} 
//						else if(mGyroRange==3){
//							mSensitivityMatrixGyroscope = SensitivityMatrixGyro2000dpsShimmer3;
//						}
//					}
//				}
//			}
//			else if(responseCommand==InstructionsResponse.GSR_RANGE_RESPONSE_VALUE){
//				byte[] bufferGSRRange = readBytes(1); 
//				mGSRRange=bufferGSRRange[0];
//				
//				printLogDataForDebugging("GSR Range Response Received: " + UtilShimmer.bytesToHexStringWithSpacesFormatted(bufferGSRRange));
//			}
			else if(responseCommand==InstructionsResponse.BLINK_LED_RESPONSE_VALUE){
				byte[] byteled = readBytes(1);
//				mCurrentLEDStatus = byteled[0]&0xFF;
				eventNewResponse(responseCommand, byteled[0]&0xFF);
			}
//			else if(responseCommand==InstructionsResponse.BUFFER_SIZE_RESPONSE_VALUE){
//				byte[] byteled = readBytes(1);
//				mBufferSize = byteled[0] & 0xFF;
//			}
//			else if(responseCommand==InstructionsResponse.MAG_GAIN_RESPONSE_VALUE){
//				byte[] bufferAns = readBytes(1); 
//				mMagRange=bufferAns[0];
//			}
//			else if(responseCommand==InstructionsResponse.MAG_SAMPLING_RATE_RESPONSE_VALUE){
//				byte[] bufferAns = readBytes(1); 
//				mLSM303MagRate=bufferAns[0];
//				
//				printLogDataForDebugging("Mag Sampling Rate Response Received: " + UtilShimmer.bytesToHexStringWithSpacesFormatted(bufferAns));
//			}
//			else if(responseCommand==InstructionsResponse.ACCEL_SAMPLING_RATE_RESPONSE_VALUE){
//				byte[] bufferAns = readBytes(1); 
//				mLSM303DigitalAccelRate=bufferAns[0];
//			}
			else if(responseCommand==InstructionsResponse.BMP180_CALIBRATION_COEFFICIENTS_RESPONSE_VALUE){
				//get pressure
				delayForBtResponse(100); // Wait to ensure the packet has been fully received
				byte[] pressureResoRes = new byte[22]; 
				pressureResoRes = readBytes(22);
				byte[] mPressureCalRawParams = new byte[23];
				System.arraycopy(pressureResoRes, 0, mPressureCalRawParams, 1, 22);
				mPressureCalRawParams[0] = (byte) responseCommand;
//				retrievePressureCalibrationParametersFromPacket(pressureResoRes,responseCommand);
				eventNewResponse(responseCommand, pressureResoRes);
			}
//			else if(responseCommand==InstructionsResponse.EXG_REGS_RESPONSE_VALUE){
//				delayForBtResponse(300); // Wait to ensure the packet has been fully received
//				byte[] bufferAns = readBytes(11);
//				if(mTempChipID==EXG_CHIP_INDEX.CHIP1.ordinal()){
//					byte[] EXG1RegisterArray = new byte[10];
//					System.arraycopy(bufferAns, 1, EXG1RegisterArray, 0, 10);
//					setEXG1RegisterArray(EXG1RegisterArray);
//				} 
//				else if(mTempChipID==EXG_CHIP_INDEX.CHIP2.ordinal()){
//					byte[] EXG2RegisterArray = new byte[10];
//					System.arraycopy(bufferAns, 1, EXG2RegisterArray, 0, 10);
//					setEXG2RegisterArray(EXG2RegisterArray);
//				}
//			}
			else if(responseCommand==InstructionsResponse.DAUGHTER_CARD_ID_RESPONSE_VALUE){
				byte[] expBoardArray = readBytes(numBytesToReadFromExpBoard+1);
//				getExpBoardID();//CHANGED TO NEWER UP-TO-DATE method
				byte[] expBoardArraySplit = Arrays.copyOfRange(expBoardArray, 1, 4);
//				setExpansionBoardDetails(new ExpansionBoardDetails(expBoardArraySplit));
				eventNewResponse(responseCommand, new ExpansionBoardDetails(expBoardArraySplit));
			}
			else if(responseCommand==InstructionsResponse.BAUD_RATE_RESPONSE_VALUE){
				byte[] bufferBaud = readBytes(1);
				int mBluetoothBaudRate=bufferBaud[0] & 0xFF;
				eventNewResponse(responseCommand, mBluetoothBaudRate);
			}
			else if(responseCommand==InstructionsResponse.TRIAL_CONFIG_RESPONSE_VALUE){
				rxBuf = readBytes(3);
//				fillTrialShimmer3(data);
				eventNewResponse(responseCommand, rxBuf);
			}
			else if(responseCommand==InstructionsResponse.CENTER_RESPONSE_VALUE){
				length = readBytes(1);
				rxBuf = readBytes(length[0]);
				String center = new String(rxBuf);
//				setCenter(center);
				eventNewResponse(responseCommand, center);
			}
			else if(responseCommand==InstructionsResponse.SHIMMERNAME_RESPONSE_VALUE){
				length = readBytes(1);
				rxBuf = readBytes(length[0]);
				String name = new String(rxBuf);
//				setShimmerUserAssignedName(name);
				eventNewResponse(responseCommand, name);
			}
			else if(responseCommand==InstructionsResponse.EXPID_RESPONSE_VALUE){
				length = readBytes(1);
				rxBuf = readBytes(length[0]);
				String expId = new String(rxBuf);
//				setTrialName(expId);
				eventNewResponse(responseCommand, expId);
			}
			else if(responseCommand==InstructionsResponse.CONFIGTIME_RESPONSE_VALUE){
				length = readBytes(1);
				rxBuf = readBytes(length[0]);
				String time = new String(rxBuf);
//				if(time.isEmpty()){
//					setConfigTime(0);
//				} 
//				else {
//					setConfigTime(Long.parseLong(time));	
//				}
				eventNewResponse(responseCommand, time);
			}
			else if(responseCommand==InstructionsResponse.RWC_RESPONSE_VALUE){
				rxBuf = readBytes(8);
				
				// Parse response string
				rxBuf = Arrays.copyOf(rxBuf, 8);
				ArrayUtils.reverse(rxBuf);
				long responseTime = (long)(((double)(ByteBuffer.wrap(rxBuf).getLong())/32.768)); // / 1000
				
//				setLastReadRealTimeClockValue(responseTime);
				eventNewResponse(responseCommand, responseTime);
			}
			else if(responseCommand==InstructionsResponse.INSTREAM_CMD_RESPONSE_VALUE){
				processInstreamResponse();
			}
//			else if(responseCommand==InstructionsResponse.LSM303DLHC_ACCEL_LPMODE_RESPONSE_VALUE){
//				byte[] responseData = readBytes(1);
//				mLowPowerAccelWR = (((int)(responseData[0]&0xFF))>=1? true:false);
//			}
//			else if(responseCommand==InstructionsResponse.LSM303DLHC_ACCEL_HRMODE_RESPONSE_VALUE){
//				byte[] responseData = readBytes(1);
//				mHighResAccelWR = (((int)(responseData[0]&0xFF))>=1? true:false);
//			}
//			else if(responseCommand==InstructionsResponse.MYID_RESPONSE_VALUE){
//				byte[] responseData = readBytes(1);
//				mTrialId = (int)(responseData[0]&0xFF);
//			}
//			else if(responseCommand==InstructionsResponse.NSHIMMER_RESPONSE_VALUE){
//				byte[] responseData = readBytes(1);
//				mTrialNumberOfShimmers = (int)(responseData[0]&0xFF);
//			}
//			else if(responseCommand==InstructionsResponse.MPU9150_SAMPLING_RATE_RESPONSE_VALUE){
//				byte[] responseData = readBytes(1);
//				setMPU9150MPLSamplingRate(((int)(responseData[0]&0xFF)));
//			}
//			else if(responseCommand==InstructionsResponse.BMP180_PRES_RESOLUTION_RESPONSE_VALUE){
//				byte[] responseData = readBytes(1);
//				mPressureResolution = (int)(responseData[0]&0xFF);
//			}
//			else if(responseCommand==InstructionsResponse.BMP180_PRES_CALIBRATION_RESPONSE_VALUE){
//				//TODO: Not used
//			}
//			else if(responseCommand==InstructionsResponse.MPU9150_MAG_SENS_ADJ_VALS_RESPONSE_VALUE){
//				//TODO: Not used
//			}
//			else if(responseCommand==InstructionsResponse.INTERNAL_EXP_POWER_ENABLE_RESPONSE_VALUE){
//				byte[] responseData = readBytes(1);
//				setInternalExpPower((int)(responseData[0]&0xFF));
//			}
			else if(responseCommand==InstructionsResponse.INFOMEM_RESPONSE_VALUE){
				// Get data length to read
				rxBuf = readBytes(1);
				int lengthToRead = (int)(rxBuf[0]&0xFF);
				rxBuf = readBytes(lengthToRead);
				printLogDataForDebugging("INFOMEM_RESPONSE Received: " + UtilShimmer.bytesToHexStringWithSpacesFormatted(rxBuf));
				
				//Copy to local buffer
				System.arraycopy(rxBuf, 0, mInfoMemBuffer, mCurrentInfoMemAddress, lengthToRead);
				//Update configuration when all bytes received.
				
				if((mCurrentInfoMemAddress+mCurrentInfoMemLengthToRead)==mTotalInfoMemLengthToRead){
//				if((mCurrentInfoMemAddress+mCurrentInfoMemLengthToRead)==mInfoMemLayout.calculateInfoMemByteLength()){
//					setShimmerInfoMemBytes(mInfoMemBuffer);
					eventNewResponse(responseCommand, mInfoMemBuffer);
				}
			}
			else{
				printLogDataForDebugging("Unhandled BT response: " + UtilShimmer.bytesToHexStringWithSpacesFormatted(new byte[]{(byte) responseCommand}));
			}

		} catch(DeviceException dE){
			
		}

	}
	

//	private void processFirmwareVerResponse() {
//		if(isShimmerBluetoothApproach){
////			if(getHardwareVersion()==HW_ID.SHIMMER_2R){
////				initializeShimmer2R();
////			} 
////			else if(getHardwareVersion()==HW_ID.SHIMMER_3) {
////				initializeShimmer3();
////			}
////			
////			startTimerCheckIfAlive();
////	//		readShimmerVersion();
//		}
//	}
	

	private void processSpecialGetCmdsAfterAck(int currentCommand) {
		byte[] insBytes = getListofInstructions().get(0);
		if(isShimmerBluetoothApproach){
			if(currentCommand==InstructionsGet.GET_EXG_REGS_COMMAND_VALUE){
				// Need to store ExG chip number before receiving response
				mTempChipID = insBytes[1];
			}
			else if(currentCommand==InstructionsGet.GET_INFOMEM_COMMAND_VALUE){
				// store current address/InfoMem segment
				mCurrentInfoMemAddress = ((insBytes[3]&0xFF)<<8)+(insBytes[2]&0xFF);
				mCurrentInfoMemLengthToRead = (insBytes[1]&0xFF);
			}
		}
		else{
			//TODO 2016-07-06 MN removed to make consistent with ShimmerBluetooth implementation 
			eventAckReceived(insBytes[0]);
		}
	}

	private boolean isKnownResponse(byte response) {
		return ((InstructionsResponse.valueOf(response&0xff)==null)? false:true);
	}

	private boolean isKnownGetCommand(int getCmd) {
		return ((InstructionsGet.valueOf(getCmd&0xff)==null)? false:true);
	}

	private boolean isKnownGetCommand(byte getCmd) {
		return ((InstructionsGet.valueOf(getCmd&0xff)==null)? false:true);
	}

	private boolean isKnownSetCommand(int setCmd) {
		return ((InstructionsSet.valueOf(setCmd)==null)? false:true);
	}

	private boolean isKnownSetCommand(byte setCmd) {
		return ((InstructionsSet.valueOf(setCmd&0xff)==null)? false:true);
	}

	private boolean bytesAvailableToBeRead() throws DeviceException {
		return mShimmerRadio.bytesAvailableToBeRead();
	}

	private void writeBytes(byte[] insBytes) throws DeviceException {
		mShimmerRadio.txBytes(insBytes);
	}

	private byte[] readBytes(int i) throws DeviceException {
		return mShimmerRadio.rxBytes(i);
	}

	private byte readByte() throws DeviceException {
		byte[] rxBytes = readBytes(1);
		return rxBytes[0];
	}

	private int availableBytes() throws DeviceException {
		return mShimmerRadio.availableBytes();
	}
	
	private void disconnect() {
		try {
			mShimmerRadio.disconnect();
		} catch (DeviceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	
	public void eventLogAndStreamStatusChanged(int currentCommand){
		mProtocolListener.eventLogAndStreamStatusChangedCallback(currentCommand);
	}

	private void isNowStreaming() {
		mProtocolListener.isNowStreaming();
	}
	
	private void hasStopStreaming() {
		mProtocolListener.hasStopStreaming();
	}

	private void eventAckReceived(int lastSentInstruction) {
		mProtocolListener.eventAckReceived(lastSentInstruction);
	}

	private void sendProgressReport(BluetoothProgressReportPerCmd bluetoothProgressReportPerCmd) {
		mProtocolListener.sendProgressReport(bluetoothProgressReportPerCmd);
	}

	private void eventNewResponse(byte[] response) {
		mProtocolListener.eventNewResponse(response);
	}
	
	private void eventNewResponse(int responseCommand, Object parsedResponse) {
		mProtocolListener.eventNewResponse(responseCommand, parsedResponse);
	}

	@Deprecated
	private void eventAckInstruction(byte[] bs) {
		mProtocolListener.eventAckInstruction(bs);
	}

	private void eventNewPacket(byte[] newPacket) {
		mProtocolListener.eventNewPacket(newPacket);
	}
	
	private void startOperation(BT_STATE currentOperation, int totalNumOfCmds) {
		mProtocolListener.startOperation(currentOperation, totalNumOfCmds);
	}

	private void sendStatusMSGtoUI(String msg) {
		mProtocolListener.sendStatusMSGtoUI(msg);
	}

	private void eventDockedStateChange() {
		mProtocolListener.eventDockedStateChange();
	}
	
	private void threadSleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private String btCommandToString(int cmd) {
		InstructionsResponse instructionResponse = InstructionsResponse.valueOf(cmd&0xff); 
		if(instructionResponse!=null){
			return instructionResponse.name();
		}
		else{
			InstructionsGet instructionGet = InstructionsGet.valueOf(cmd&0xff); 
			if(instructionGet!=null){
				return instructionGet.name();
			}
			else{
				InstructionsSet instructionSet = InstructionsSet.valueOf(cmd&0xff); 
				if(instructionSet!=null){
					return instructionSet.name();
				}
				else {
					return Integer.toHexString(mCurrentCommand);
				}
			}
		}
	}

	public void printLogDataForDebugging(String msg){
		mUtilShimmer.consolePrintLn(msg);
	}
	
	/**
	 * Due to the nature of the Bluetooth SPP stack a delay has been added to
	 * ensure the buffer is filled before it is read
	 * 
	 */
	private void delayForBtResponse(long millis){
		threadSleep(millis);
	}
	

	private void processAckFromSetCommand(int currentCommand) {
		// check for null and size were put in because if Shimmer was abruptly
		// disconnected there is sometimes indexoutofboundsexceptions
		if(getListofInstructions().size() > 0){
			byte[] currentInstruction = getListofInstructions().get(0);
			if(currentInstruction!=null){
//			if(getListofInstructions().get(0)!=null){
				
				if(currentCommand==InstructionsSet.START_STREAMING_COMMAND_VALUE 
						|| currentCommand==InstructionsSet.START_SDBT_COMMAND_VALUE) {
					mByteArrayOutputStream.reset();
					setIsStreaming(true);
					if(currentCommand==InstructionsSet.START_SDBT_COMMAND_VALUE){
						setIsSDLogging(true);
						eventLogAndStreamStatusChanged(mCurrentCommand);
					}
					byteStack.clear();
					isNowStreaming();
				}
				else if((currentCommand==InstructionsSet.STOP_STREAMING_COMMAND_VALUE)
						||(currentCommand==InstructionsSet.STOP_SDBT_COMMAND_VALUE)){
					setIsStreaming(false);
					if(currentCommand==InstructionsSet.STOP_SDBT_COMMAND_VALUE) {
						setIsSDLogging(false);
						eventLogAndStreamStatusChanged(mCurrentCommand);
					}
					mByteArrayOutputStream = new ByteArrayOutputStream();
					byteStack.clear();

					//TODO try statement is not used in ShimmerBluetooth
					try {
						clearSerialBuffer();
					} catch (DeviceException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					hasStopStreaming();					
					getListofInstructions().removeAll(Collections.singleton(null));
				}
				else if(currentCommand==InstructionsSet.START_LOGGING_ONLY_COMMAND_VALUE) {
					setIsSDLogging(true);
					eventLogAndStreamStatusChanged(mCurrentCommand);
				}
				else if(currentCommand==InstructionsSet.STOP_LOGGING_ONLY_COMMAND_VALUE) {
					setIsSDLogging(false);
					eventLogAndStreamStatusChanged(mCurrentCommand);
				}
				else if(currentCommand==InstructionsSet.SET_INFOMEM_COMMAND_VALUE){
					if(isShimmerBluetoothApproach){
						//SET InfoMem is automatically followed by a GET so no need to handle here
						
						//Sleep for Xsecs to allow Shimmer to process new configuration
						mNumOfInfoMemSetCmds -= 1;
						if(mNumOfInfoMemSetCmds==0){
							delayForBtResponse(DELAY_BETWEEN_INFOMEM_WRITES);
						}
						else {
							delayForBtResponse(DELAY_AFTER_INFOMEM_WRITE);
						}
					}
				}
				else{
//					printLogDataForDebugging("Unhandled ACK for current command:\t" + btCommandToString(currentCommand));
				}
				
				if(getListofInstructions().size() > 0){
					getListofInstructions().remove(0);
				}

				eventAckInstruction(currentInstruction);

			}
		}
	}
		
	
	public void readRealTimeClock(){
		if((getHardwareVersion()==HW_ID.SHIMMER_3 && getFirmwareIdentifier()==FW_ID.LOGANDSTREAM) 
				|| (getHardwareVersion()==HW_ID.SHIMMER_4_SDK && getFirmwareIdentifier()==FW_ID.SHIMMER4_SDK_STOCK)){
			writeInstruction(InstructionsGet.GET_RWC_COMMAND_VALUE);
		}
	}
	
	/**Gets pc time and writes the 8 byte value to shimmer device
	 * 
	 */
	public void writeRealTimeClock(){
		if((getHardwareVersion()==HW_ID.SHIMMER_3 && getFirmwareIdentifier()==FW_ID.LOGANDSTREAM) 
				|| (getHardwareVersion()==HW_ID.SHIMMER_4_SDK && getFirmwareIdentifier()==FW_ID.SHIMMER4_SDK_STOCK)){
			//Just fill empty bytes here for RTC, set them just before writing to Shimmer
		    byte[] bytearraycommand = new byte[9];
			bytearraycommand[0]=(byte) InstructionsSet.SET_RWC_COMMAND_VALUE;
			getListofInstructions().add(bytearraycommand);
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
		printLogDataForDebugging("Throw Byte" + UtilShimmer.byteToHexStringFormatted(bTemp[0]));
	}
	
	/**
	 * @param bufferTemp
	 */
	private void processDataPacket(byte[] bufferTemp){
		//Create newPacket buffer
		byte[] newPacket = new byte[mPacketSize];
		//Skip the first byte as it is the identifier DATA_PACKET
		System.arraycopy(bufferTemp, 1, newPacket, 0, mPacketSize);
		
		eventNewPacket(newPacket);
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
//		printLogDataForDebugging(Integer.toString(bufferTemp[mPacketSize+2]));
		for (int i=0;i<packetSize;i++){
			mListofPCTimeStamps.remove(0);
		}
	}

	/** process responses to in-stream response */
	private void processInstreamResponse(){
		try {
			byte[] inStreamResponseCommandArray = readBytes(1);
			int inStreamResponseCommand = ((int)inStreamResponseCommandArray[0])&0xFF;
			printLogDataForDebugging("In-stream received = " + btCommandToString(inStreamResponseCommand));

			if(inStreamResponseCommand==InstructionsResponse.DIR_RESPONSE_VALUE){ 
				byte[] responseData = readBytes(1);
				int directoryNameLength = responseData[0];
				byte[] bufferDirectoryName = new byte[directoryNameLength];
				bufferDirectoryName = readBytes(directoryNameLength);
				String tempDirectory = new String(bufferDirectoryName);
				mDirectoryName = tempDirectory;
				printLogDataForDebugging("Directory Name = " + mDirectoryName);
				
				if(!isShimmerBluetoothApproach){
					bufferDirectoryName = ArrayUtils.addAll(responseData, bufferDirectoryName);
					eventNewResponse(bufferDirectoryName);
				}
			}
			else if(inStreamResponseCommand==InstructionsResponse.STATUS_RESPONSE_VALUE){
				if(isShimmerBluetoothApproach){
					byte[] responseData = readBytes(1);
					parseStatusByte(responseData[0]);
//					eventNewResponse(inStreamResponseCommand, responseData[0]);
				}
				
				if(!isSensing()){
					if(isShimmerBluetoothApproach){
						if(!isInitialised()){
							writeRealTimeClock();
						}
					}
					else{
						writeRealTimeClock();
					}
				}
				eventLogAndStreamStatusChanged(mCurrentCommand);
			} 
			else if(inStreamResponseCommand==InstructionsResponse.VBATT_RESPONSE_VALUE) {
				byte[] responseData = readBytes(3); 
				if(isShimmerBluetoothApproach){
					ShimmerBattStatusDetails battStatusDetails = new ShimmerBattStatusDetails(((responseData[1]&0xFF)<<8)+(responseData[0]&0xFF),responseData[2]);
//					setBattStatusDetails(battStatusDetails);
					eventNewResponse(inStreamResponseCommand, battStatusDetails);
					printLogDataForDebugging("Batt data " + battStatusDetails.getBattVoltage());
				}
				else{
					responseData = ArrayUtils.addAll(inStreamResponseCommandArray, responseData);
					eventNewResponse(responseData);
				}
			}

		} catch (DeviceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	//TODO copied from ShimmerBluetooth
	private void parseStatusByte(byte statusByte) {
		Boolean savedDockedState = isDocked();
		
		setIsDocked(((statusByte & 0x01) > 0)? true:false);
		setIsSensing(((statusByte & 0x02) > 0)? true:false);
//		reserved = ((statusByte & 0x03) > 0)? true:false;
		setIsSDLogging(((statusByte & 0x08) > 0)? true:false);
		setIsStreaming(((statusByte & 0x10) > 0)? true:false); 
		
		printLogDataForDebugging("Status Response = " + UtilShimmer.byteToHexStringFormatted(statusByte)
				+ "\t" + "IsDocked = " + isDocked()
				+ "\t" + "IsSensing = " + isSensing()
				+ "\t" + "IsSDLogging = "+ isSDLogging()
				+ "\t" + "IsStreaming = " + isStreaming()
				);
		
		if(savedDockedState!=isDocked()){
			eventDockedStateChange();
		}
	}
	
	/**
	 * The reason for this is because sometimes the 1st response is not received by the phone
	 */
	protected void dummyreadSamplingRate() {
		mDummy=true;
		writeInstruction(InstructionsGet.GET_SAMPLING_RATE_COMMAND_VALUE);
	}

	@Override
	public void toggleLed() {
		byte[] instructionLED = {InstructionsSet.TOGGLE_LED_COMMAND_VALUE};
		writeInstruction(instructionLED);
	}

	@Override
	public void readFWVersion() {
		mDummy=false;//false
		writeInstruction(InstructionsGet.GET_FW_VERSION_COMMAND_VALUE);
	}

	@Override
	public void readShimmerVersionNew() {
		mDummy=false;//false
//		if(mFirmwareVersionParsed.equals(boilerPlateString)){
//			mShimmerVersion = HW_ID.SHIMMER_2R; // on Shimmer2r has 
			
//		} 
//		else if(mFWVersion!=1.2){
			writeInstruction(InstructionsGet.GET_SHIMMER_VERSION_COMMAND_NEW_VALUE);
//		} 
//			else {
//			mListofInstructions.add(GET_SHIMMER_VERSION_COMMAND});
//		}
	}
	
	@Override
	public void readPressureCalibrationCoefficients() {
		if((getHardwareVersion()==HW_ID.SHIMMER_3)
				||(getHardwareVersion()==HW_ID.SHIMMER_4_SDK)){
			if(getFirmwareVersionCode()>1){
				writeInstruction(InstructionsGet.GET_BMP180_CALIBRATION_COEFFICIENTS_COMMAND_VALUE);
			}
		}
	}

	@Override
	protected void readInfoMem(int address, int size) {
		readMemCommand(InstructionsGet.GET_INFOMEM_COMMAND_VALUE, address, size);
	}

	@Override
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

	@Override
	public void writeInfoMem(int startAddress, byte[] buf) {
		writeMemCommand(InstructionsSet.SET_INFOMEM_COMMAND_VALUE, startAddress, buf);
	}

	/**Could be used by InfoMem or Expansion board memory
	 * @param command
	 * @param address
	 * @param infoMemBytes
	 */
	public void writeMemCommand(int command, int address, byte[] infoMemBytes) {
//		if(this.getFirmwareVersionCode()>=6){
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
//			getListofInstructions().add(instructionBuffer);
//		}
	}

	//region --------- OPERATIONS --------- 
	/**
	 * 
	 */
	@Override
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
		writeInstruction(InstructionsSet.START_STREAMING_COMMAND_VALUE);
	}
	
	@Override
	public void startDataLogAndStreaming(){
		if((getShimmerVersion()==HW_ID.SHIMMER_3 && getFirmwareIdentifier()==FW_ID.LOGANDSTREAM)
				||(getShimmerVersion()==HW_ID.SHIMMER_4_SDK && getFirmwareIdentifier()==FW_ID.SHIMMER4_SDK_STOCK)){
//		if(getFirmwareIdentifier()==FW_ID.LOGANDSTREAM){ // if shimmer is using LogAndStream FW, stop reading its status perdiocally
			initialiseStreaming();
			
			//TODO: ask JC, should mByteArrayOutputStream.reset(); and mListofPCTimeStamps.clear(); be here as well? 
			writeInstruction(InstructionsSet.START_SDBT_COMMAND_VALUE);
		}
	}
	
	private void initialiseStreaming(){
		if(((getShimmerVersion()==HW_ID.SHIMMER_3 && getFirmwareIdentifier()==FW_ID.LOGANDSTREAM)
				||(getShimmerVersion()==HW_ID.SHIMMER_4_SDK && getFirmwareIdentifier()==FW_ID.SHIMMER4_SDK_STOCK))
				&& getFirmwareVersionCode() >=6){
			readRealTimeClock();
		}

		stopTimerReadStatus(); // if shimmer is using LogAndStream FW, stop reading its status perdiocally

		mProtocolListener.initialiseStreamingCallback();
//		//provides a callback for users to initialize their algorithms when start streaming is called
//		if(mDataProcessing!=null){
//			mDataProcessing.InitializeProcessData();
//		} 	
//		else {
//			//do nothing
//		}
//		
//		mPacketLossCount = 0;
//		setPacketReceptionRateOverall(100);
//		mFirstPacketParsed=true;
//		mFirstTimeCalTime=true;
//		resetCalibratedTimeStamp();
//		mLastReceivedCalibratedTimeStamp = -1;
//		mSync=true; // a backup sync done every time you start streaming
	}
	
	@Override
	public void startSDLogging() {
		if(((getShimmerVersion()==HW_ID.SHIMMER_3 && getFirmwareIdentifier()==FW_ID.LOGANDSTREAM)
				||(getShimmerVersion()==HW_ID.SHIMMER_4_SDK && getFirmwareIdentifier()==FW_ID.SHIMMER4_SDK_STOCK))
				&& getFirmwareVersionCode() >=6){
//		if(getFirmwareIdentifier()==FW_ID.LOGANDSTREAM && getFirmwareVersionCode() >=6){
			writeInstruction(InstructionsSet.START_LOGGING_ONLY_COMMAND_VALUE);
		}	
	}

	@Override
	public void stopSDLogging() {
		if(((getShimmerVersion()==HW_ID.SHIMMER_3 && getFirmwareIdentifier()==FW_ID.LOGANDSTREAM)
				||(getShimmerVersion()==HW_ID.SHIMMER_4_SDK && getFirmwareIdentifier()==FW_ID.SHIMMER4_SDK_STOCK))
				&& getFirmwareVersionCode() >=6){
//		if(getFirmwareIdentifier()==FW_ID.LOGANDSTREAM && getFirmwareVersionCode() >=6){
			writeInstruction(InstructionsSet.STOP_LOGGING_ONLY_COMMAND_VALUE);
		}	
	}

	@Override
	public void stopStreaming() {
		if(isStreaming()){
			writeInstruction(InstructionsSet.STOP_STREAMING_COMMAND_VALUE);
			
			// For LogAndStream
			stopTimerReadStatus();
		}
	}
	
	
	/**Only applicable for logandstream
	 * 
	 */
	@Override
	public void stopStreamingAndLogging() {
		// if shimmer is using LogAndStream FW, stop reading its status periodically
		if((getShimmerVersion()==HW_ID.SHIMMER_3 && getFirmwareIdentifier()==FW_ID.LOGANDSTREAM)
				||(getShimmerVersion()==HW_ID.SHIMMER_4_SDK && getFirmwareIdentifier()==FW_ID.SHIMMER4_SDK_STOCK)){
//		if(getFirmwareIdentifier()==FW_ID.LOGANDSTREAM){ 
			writeInstruction(InstructionsSet.STOP_SDBT_COMMAND_VALUE);
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
			int storedFirstTime = (mFirstTime? 1:0);
			
			//Timeout triggered 
			printLogDataForDebugging("Command:\t" + btCommandToString(mCurrentCommand) +" timeout");
			if(mWaitForAck){
				printLogDataForDebugging("Ack not received");
			}
			if(mWaitForResponse) {
				printLogDataForDebugging("Response not received");
				sendStatusMSGtoUI("Response not received, please reset Shimmer Device." + mMyBluetoothAddress); //Android?
			}
			//TODO
//				if(mIsStreaming && getPacketReceptionRateOverall()<100){
//					printLogDataForDebugging("Packet RR:  " + Double.toString(getPacketReceptionRateOverall()));
//				} 
			
			//handle the special case when we are starting/stopping to log in Consensys and we do not get the ACK response
			//we will send the status changed to the GUI anyway
			if(mCurrentCommand==InstructionsSet.START_LOGGING_ONLY_COMMAND_VALUE){
				
				printLogDataForDebugging("START_LOGGING_ONLY_COMMAND response not received. Send feedback to the GUI without killing the connection");
				
				setIsSDLogging(true);
				eventLogAndStreamStatusChanged(mCurrentCommand);
				mWaitForAck=false;
				mWaitForResponse=false;
				
				getListofInstructions().remove(0);
				mTransactionCompleted = true;
				setInstructionStackLock(false);
				
				return;
			}
			else if(mCurrentCommand==InstructionsSet.STOP_LOGGING_ONLY_COMMAND_VALUE){
				
				printLogDataForDebugging("STOP_LOGGING_ONLY_COMMAND response not received. Send feedback to the GUI without killing the connection");
				
				setIsSDLogging(false);
				eventLogAndStreamStatusChanged(mCurrentCommand);
				mWaitForAck=false;
				mWaitForResponse=false;
				
				getListofInstructions().remove(0);
				mTransactionCompleted = true;
				setInstructionStackLock(false);
				
				return;
			}
			

			if(mCurrentCommand==InstructionsGet.GET_FW_VERSION_COMMAND_VALUE){
				mFirstTime=false;
//				eventAckReceived(mCurrentCommand);
//					setShimmerVersionInfoAndCreateSensorMap(new ShimmerVerObject(HW_ID.SHIMMER_2R, FW_ID.BOILER_PLATE, 0, 1, 0));
//					
////					/*Message msg = mHandler.obtainMessage(MESSAGE_TOAST);
////	      	        Bundle bundle = new Bundle();
////	      	        bundle.putString(TOAST, "Firmware Version: " +mFirmwareVersionParsed);
////	      	        msg.setData(bundle);*/
////					if(!mDummy){
////						//mHandler.sendMessage(msg);
////					}
//					
//					initializeBoilerPlate();
			}
			else if(mCurrentCommand==InstructionsGet.GET_SAMPLING_RATE_COMMAND_VALUE && !isInitialised()){
				mFirstTime=false;
			} 
			else if(mCurrentCommand==InstructionsGet.GET_SHIMMER_VERSION_COMMAND_NEW_VALUE){ //in case the new command doesn't work, try the old command
				mFirstTime=false;
				getListofInstructions().clear();
				readShimmerVersionDeprecated();
			}

			
			
			if(isStreaming()){
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
				
				try {
					if (bytesAvailableToBeRead()){
						readBytes(availableBytes());
					}
					stopTimerCheckForAckOrResp(); //Terminate the timer thread
					printLogDataForDebugging("RETRY TX COUNT: " + Integer.toString(mNumberofTXRetriesCount));
					if (mNumberofTXRetriesCount>=NUMBER_OF_TX_RETRIES_LIMIT 
							&& mCurrentCommand!=InstructionsGet.GET_SHIMMER_VERSION_COMMAND_NEW_VALUE 
							&& !isInitialised()){
						killConnection(); //If command fail exit device	
					} else if(mNumberofTXRetriesCount>=NUMBER_OF_TX_RETRIES_LIMIT && isInitialised()){
						killConnection(); //If command fail exit device	
					} else {
						mWaitForAck=false;
						mWaitForResponse=false;
						mTransactionCompleted=true; //should be false, so the driver will know that the command has to be executed again, this is not supported at the moment 
						setInstructionStackLock(false);
						//this is needed because if u dc the shimmer the write call gets stuck
						startTimerCheckForAckOrResp(ACK_TIMER_DURATION+3);
					}
				} catch (DeviceException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				mNumberofTXRetriesCount++;
			}
		} //End Run
	} //End TimerTask
	
	private void retryTXCommand(){
		//NOT USED
		//NOT USED in ShimmerBluetooth either
	}
	
	private void killConnection(){
		printLogDataForDebugging("Killing Connection");
		stop(); //If command fail exit device 
	}

	@Override
	public void startTimerReadStatus(){
		// if shimmer is using LogAndStream FW, stop reading its status periodically
		if((getHardwareVersion()==HW_ID.SHIMMER_3 && getFirmwareIdentifier()==FW_ID.LOGANDSTREAM) 
				|| (getHardwareVersion()==HW_ID.SHIMMER_4_SDK && getFirmwareIdentifier()==FW_ID.SHIMMER4_SDK_STOCK)){
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
					&& !getListofInstructions().contains(InstructionsGet.GET_STATUS_COMMAND_VALUE)){
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
			if((getHardwareVersion()==HW_ID.SHIMMER_3 && getFirmwareIdentifier()==FW_ID.LOGANDSTREAM)
					|| (getHardwareVersion()==HW_ID.SHIMMER_3 && getFirmwareIdentifier()==FW_ID.BTSTREAM)
					|| (getHardwareVersion()==HW_ID.SHIMMER_4_SDK && getFirmwareIdentifier()==FW_ID.SHIMMER4_SDK_STOCK)){
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
				if((getHardwareVersion()==HW_ID.SHIMMER_3 && getFirmwareIdentifier()==FW_ID.LOGANDSTREAM && isStreaming())
						|| (getHardwareVersion()==HW_ID.SHIMMER_3 && getFirmwareIdentifier()==FW_ID.BTSTREAM)
						|| (getHardwareVersion()==HW_ID.SHIMMER_4_SDK && getFirmwareIdentifier()==FW_ID.SHIMMER4_SDK_STOCK && isStreaming())){
					mCountDeadConnection++;
				}
				
				if(getFirmwareVersionCode()>=6 && !isStreaming()){
					if(getListofInstructions().size()==0 
							&&!getListofInstructions().contains(InstructionsSet.TEST_CONNECTION_COMMAND_VALUE)){
						printLogDataForDebugging("Check Alive Task");
						if((getShimmerVersion()==HW_ID.SHIMMER_3 && getFirmwareIdentifier()==FW_ID.LOGANDSTREAM)
								||(getShimmerVersion()==HW_ID.SHIMMER_4_SDK && getFirmwareIdentifier()==FW_ID.SHIMMER4_SDK_STOCK)){
//						if(getShimmerVersion()==HW_ID.SHIMMER_3 && getFirmwareIdentifier()==FW_ID.LOGANDSTREAM){
							//writeTestConnectionCommand(); //dont need this because of the get status command
						} 
						else if (getShimmerVersion()==HW_ID.SHIMMER_3 && getFirmwareIdentifier()==FW_ID.BTSTREAM){
							writeTestConnectionCommand();
						}
					}
				} 
				else {
					printLogDataForDebugging("Check Alive Task");
					writeLEDCommand(0);
				}
				
				if(mCountDeadConnection>5){
//					setState(BT_STATE.NONE);
					killConnection(); //If command fail exit device
				}
			} 
		} //End Run
	} //End TimerTask
	
	@Override
	public void startTimerReadBattStatus(){
		//Instream response only supported in LogAndStream
		if(((getShimmerVersion()==HW_ID.SHIMMER_3 && getFirmwareIdentifier()==FW_ID.LOGANDSTREAM)
				||(getShimmerVersion()==HW_ID.SHIMMER_4_SDK && getFirmwareIdentifier()==FW_ID.SHIMMER4_SDK_STOCK))
				&&(getFirmwareVersionCode()>=6)){
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
			printLogDataForDebugging("Read Batt Task");
			readBattery();
		} //End Run
	} //End TimerTask

	@Override
	public void restartTimersIfNull() {
		if (mTimerCheckAlive==null && mTimerReadStatus==null && mTimerReadBattStatus==null){
			startTimerCheckIfAlive();
			startTimerReadStatus();
			startTimerReadBattStatus();
        }
	}

//	public synchronized void startTimerCheckForAckOrResp(int seconds) {
//		//public synchronized void responseTimer(int seconds) {
//		if(mTimerCheckForAckOrResp!=null) {
//			mTimerCheckForAckOrResp.cancel();
//			mTimerCheckForAckOrResp.purge();
//			mTimerCheckForAckOrResp = null;
//		}
//
//		printLogDataForDebugging("Waiting for ack/response for command:\t" + mCurrentCommand);
//		mTimerCheckForAckOrResp = new Timer("Shimmer_" + "_TimerCheckForResp");
//		mTimerCheckForAckOrResp.schedule(new checkForAckOrRespTask(), seconds*1000);
//	}
//	
//	public void stopTimerCheckForAckOrResp(){
//		//Terminate the timer thread
//		if(mTimerCheckForAckOrResp!=null){
//			mTimerCheckForAckOrResp.cancel();
//			mTimerCheckForAckOrResp.purge();
//			mTimerCheckForAckOrResp = null;
//		}
//	}
//
//	/** Handles command response timeout
//	 *
//	 */
//	class checkForAckOrRespTask extends TimerTask {
//		
//		@Override
//		public void run() {
//			{
//				int storedFirstTime = (mFirstTime? 1:0);
//				
//				//Timeout triggered 
//				printLogDataForDebugging("Command:\t" + mCurrentCommand +" timeout");
//				if(mWaitForAck){
//					printLogDataForDebugging("Ack not received");
//				}
//				if(mWaitForResponse) {
//					printLogDataForDebugging("Response not received");
//					
//				}
//				if(mIsStreaming && getPacketReceptionRate()<100){
//					printLogDataForDebugging("Packet RR:  " + Double.toString(getPacketReceptionRate()));
//				} 
//				
//				//handle the special case when we are starting/stopping to log in Consensys and we do not get the ACK response
//				//we will send the status changed to the GUI anyway
//				if(mCurrentCommand==InstructionsSet.START_LOGGING_ONLY_COMMAND_VALUE){
//					
//					printLogDataForDebugging("START_LOGGING_ONLY_COMMAND response not received. Send feedback to the GUI without killing the connection");
//					
//					mIsSDLogging = true;
//					eventLogAndStreamStatusChanged(mCurrentCommand);
//					mWaitForAck=false;
//					mWaitForResponse=false;
//					
//					getListofInstructions().remove(0);
//					mTransactionCompleted = true;
//					setInstructionStackLock(false);
//					
//					return;
//				}
//				else if(mCurrentCommand==InstructionsSet.STOP_LOGGING_ONLY_COMMAND_VALUE){
//					
//					printLogDataForDebugging("STOP_LOGGING_ONLY_COMMAND response not received. Send feedback to the GUI without killing the connection");
//					
//					mIsSDLogging = false;
//					eventLogAndStreamStatusChanged(mCurrentCommand);
//					mWaitForAck=false;
//					mWaitForResponse=false;
//					
//					getListofInstructions().remove(0);
//					mTransactionCompleted = true;
//					setInstructionStackLock(false);
//					
//					return;
//				}
//				
//
//				if(mCurrentCommand==InstructionsGet.GET_FW_VERSION_COMMAND_VALUE){
//					
//				}
//				else if(mCurrentCommand==InstructionsGet.GET_SAMPLING_RATE_COMMAND_VALUE && !mIsInitialised){
//					mFirstTime=false;
//				} 
//				else if(mCurrentCommand==InstructionsGet.GET_SHIMMER_VERSION_COMMAND_NEW_VALUE){ //in case the new command doesn't work, try the old command
//					mFirstTime=false;
//					
//				}
//
//				
//				
//				if(mIsStreaming){
//					stopTimerCheckForAckOrResp(); //Terminate the timer thread
//					mWaitForAck=false;
//					mTransactionCompleted=true; //should be false, so the driver will know that the command has to be executed again, this is not supported at the moment 
//					setInstructionStackLock(false);
//					getListofInstructions().clear();
//				}
//				else if(storedFirstTime==0){
//					// If the command fails to get a response, the API should
//					// assume that the connection has been lost and close the
//					// serial port cleanly.
//					
//					try {
//						if (bytesAvailableToBeRead()){
//							try {
//								readBytes(availableBytes());
//							} catch (DeviceException e) {
//								// TODO Auto-generated catch block
//								e.printStackTrace();
//							}
//						}
//					} catch (DeviceException e1) {
//						// TODO Auto-generated catch block
//						e1.printStackTrace();
//					}
//					stopTimerCheckForAckOrResp(); //Terminate the timer thread
//					printLogDataForDebugging("RETRY TX COUNT: " + Integer.toString(mNumberofTXRetriesCount));
//					if (mNumberofTXRetriesCount>=NUMBER_OF_TX_RETRIES_LIMIT){
//						disconnect();
//					} else {
//						mWaitForAck=false;
//						mWaitForResponse=false;
//						mTransactionCompleted=true; //should be false, so the driver will know that the command has to be executed again, this is not supported at the moment 
//						setInstructionStackLock(false);
//						//this is needed because if u dc the shimmer the write call gets stuck
//						startTimerCheckForAckOrResp(ACK_TIMER_DURATION+3);
//					}
//					
//					mNumberofTXRetriesCount++;
//				}
//			}
//		} //End Run
//	} //End TimerTask
	
	//endregion --------- TIMERS ---------
	
	@Override
	public void readExpansionBoardID() {
		if(getFirmwareVersionCode()>=5){ 
			numBytesToReadFromExpBoard=3;
			int offset=0;
			writeInstruction(new byte[]{InstructionsGet.GET_DAUGHTER_CARD_ID_COMMAND_VALUE, (byte) numBytesToReadFromExpBoard, (byte) offset});
		}
	}

	@Override
	public void readLEDCommand() {
		writeInstruction(InstructionsGet.GET_BLINK_LED_VALUE);
	}
	
	public void writeLEDCommand(int command) {
////		if(mShimmerVersion!=HW_ID.SHIMMER_3){
//			if(isThisVerCompatibleWith(HW_ID.SHIMMER_2R, FW_ID.BOILER_PLATE, 0, 1, 0)){
//	//			if(mFirmwareVersionParsed.equals(boilerPlateStringDescription)){
//			}
//			else {
//				writeInstruction(InstructionsSet.SET_BLINK_LED_VALUE, (byte)command});
//			}
////		}
	}


	@Override
	public void readStatusLogAndStream() {
		if((getShimmerVersion()==HW_ID.SHIMMER_3 && getFirmwareIdentifier()==FW_ID.LOGANDSTREAM)
				||(getShimmerVersion()==HW_ID.SHIMMER_4_SDK && getFirmwareIdentifier()==FW_ID.SHIMMER4_SDK_STOCK)){ // check if Shimmer is using LogAndStream firmware
			writeInstruction(InstructionsGet.GET_STATUS_COMMAND_VALUE);
			printLogDataForDebugging("Instruction added to the list");
		}
	}

	@Override
	public void readBattery() {
		writeInstruction(InstructionsGet.GET_VBATT_COMMAND_VALUE);
	}

	@Override
	public void inquiry() {
		writeInstruction(InstructionsGet.INQUIRY_COMMAND_VALUE);
	}
	
	public void writeTestConnectionCommand() {
		if(getFirmwareVersionCode()>=6){
			writeInstruction(InstructionsSet.TEST_CONNECTION_COMMAND_VALUE);
		}
	}

	@Deprecated
	public void readShimmerVersionDeprecated(){
		writeInstruction(InstructionsGet.GET_SHIMMER_VERSION_COMMAND_VALUE);
	}

	/**
	 * Transmits a command to the Shimmer device to enable the sensors. To enable multiple sensors an or operator should be used (e.g. writeEnabledSensors(SENSOR_ACCEL|SENSOR_GYRO|SENSOR_MAG)). Command should not be used consecutively. Valid values are SENSOR_ACCEL, SENSOR_GYRO, SENSOR_MAG, SENSOR_ECG, SENSOR_EMG, SENSOR_GSR, SENSOR_EXP_BOARD_A7, SENSOR_EXP_BOARD_A0, SENSOR_BRIDGE_AMP and SENSOR_HEART.
    SENSOR_BATT
	 * @param enabledSensors e.g SENSOR_ACCEL|SENSOR_GYRO|SENSOR_MAG
	 */
	@Override
	public void writeEnabledSensors(long enabledSensors) {
		
//		if(getFirmwareVersionCode()<=1){
//			if(!sensorConflictCheck(enabledSensors)){ //sensor conflict check - not needed with new SensorMap
//				return;
//			} 
//			else {
//				enabledSensors=generateSensorBitmapForHardwareControl(enabledSensors);
//			}
//		}
//			
//		tempEnabledSensors=enabledSensors;

		byte secondByte=(byte)((enabledSensors & 0xFF00)>>8);
		byte firstByte=(byte)(enabledSensors & 0xFF);

		//write(SET_SENSORS_COMMAND,(byte) lowByte, highByte});
		if((getHardwareVersion()==HW_ID.SHIMMER_3)
			||(getHardwareVersion()==HW_ID.SHIMMER_4_SDK)){
			byte thirdByte=(byte)((enabledSensors & 0xFF0000)>>16);
			writeInstruction(new byte[]{InstructionsSet.SET_SENSORS_COMMAND_VALUE,(byte) firstByte,(byte) secondByte,(byte) thirdByte});
		} 
		else {
			writeInstruction(new byte[]{InstructionsSet.SET_SENSORS_COMMAND_VALUE,(byte) firstByte,(byte) secondByte});
		}
		inquiry();
	}
	
	
	//TODO TEMP HERE
	private int getFirmwareVersionCode() {
		if(mShimmerVerObject!=null){
			return mShimmerVerObject.getFirmwareVersionCode();
		}
		return 6;
	}

	//TODO TEMP HERE
	private int getHardwareVersion() {
		return mHardwareVersion;
	}
	
	private void setHardwareVersion(int hardwareVersion) {
		mHardwareVersion = hardwareVersion;
	}
	
	//TODO TEMP HERE
	private String getMacIdParsed() {
		return "TEMP_MAC";
	}

	
	//***********************************
	// Copied from ShimmerDevice Start
	//*********************************** 
	
//	private void eventSyncStates(boolean isDocked, boolean isInitialised, boolean isSdLogging, boolean isSensing, boolean isStreaming, boolean haveAttemptedToRead) {
//		mProtocolListener.eventSyncStates(isDocked, isInitialised, isSdLogging, isSensing, isStreaming, haveAttemptedToRead);
//	}

	/**
	 * @param docked the mDocked to set
	 */
	public void setIsDocked(boolean docked) {
		mIsDocked = docked;
		mProtocolListener.eventSetIsDocked(mIsDocked);
	}

	/**
	 * @return the mDocked
	 */
	public boolean isDocked() {
		return mIsDocked;
	}

    /** Returns true if device is streaming (Bluetooth)
     * @return
     */
    public boolean isStreaming(){
    	return mIsStreaming;
    }

	public void setIsStreaming(boolean state) {
		mIsStreaming = state;
		mProtocolListener.eventSetIsStreaming(mIsStreaming);
	}

	/**Only used for LogAndStream
	 * @return
	 */
	public boolean isSensing(){
		return (mIsSensing || mIsSDLogging || mIsStreaming);
	}
	
	public void setIsSensing(boolean state) {
		mIsSensing = state;
		mProtocolListener.eventSetIsSensing(mIsSensing);
	}
	
	public boolean isSDLogging(){
		return mIsSDLogging;
	}	

	public void setIsSDLogging(boolean state){
		mIsSDLogging = state;
		mProtocolListener.eventSetIsSDLogging(mIsSDLogging);
	}	


	/**
	 * @param isInitialized the mSuccessfullyInitialized to set
	 */
	public void setIsInitialised(boolean isInitialized) {
		mIsInitialised = isInitialized;
		mProtocolListener.eventSetIsInitialised(mIsInitialised);
	}
	
	/**
	 * @return the mIsInitialized
	 */
	public boolean isInitialised() {
		return mIsInitialised;
	}
	
	/**
	 * @return the mHaveAttemptedToRead
	 */
	public boolean isHaveAttemptedToRead() {
		return mHaveAttemptedToReadConfig;
	}

	/**
	 * @param haveAttemptedToRead the mHaveAttemptedToRead to set
	 */
	public void setHaveAttemptedToRead(boolean haveAttemptedToRead) {
		mHaveAttemptedToReadConfig = haveAttemptedToRead;
		mProtocolListener.eventSetHaveAttemptedToRead(mHaveAttemptedToReadConfig);
	}
	
	//***********************************
	// Copied from ShimmerDevice end
	//*********************************** 

//	public void eventSyncStates(){
//		eventSyncStates(isDocked(), isInitialised(), isSDLogging(), isSensing(), isStreaming(), isHaveAttemptedToRead());
//	}

	
}
