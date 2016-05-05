package com.shimmerresearch.comms.radioProtocol;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;

import org.apache.commons.lang3.ArrayUtils;

import com.shimmerresearch.bluetooth.ProgressReportPerCmd;
import com.shimmerresearch.bluetooth.RawBytePacketWithPCTimeStamp;
import com.shimmerresearch.bluetooth.ShimmerBluetooth.IOThread;
import com.shimmerresearch.bluetooth.ShimmerBluetooth.ProcessingThread;
import com.shimmerresearch.comms.radioProtocol.ShimmerLiteProtocolInstructionSet.LiteProtocolInstructionSet;
import com.shimmerresearch.comms.serialPortInterface.ByteLevelDataComm;
import com.shimmerresearch.driver.DeviceException;
import com.shimmerresearch.driver.UtilShimmer;
import com.shimmerresearch.driverUtilities.ShimmerBattStatusDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.FW_ID;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.HW_ID;



public class LiteProtocol extends ByteLevelProtocol{

	protected List<byte []> mListofInstructions = new  ArrayList<byte[]>();
	protected byte mCurrentCommand;
	public LiteProtocol(){
		
	}
	public LiteProtocol(ByteLevelDataComm mSerialPort) {
		super(mSerialPort);
		// TODO Auto-generated constructor stub
	}

	transient protected IOThread mIOThread;
	private boolean mInstructionStackLock = false;
	protected boolean mWaitForAck=false;                                          // This indicates whether the device is waiting for an acknowledge packet from the Shimmer Device  
	protected boolean mWaitForResponse=false; 									// This indicates whether the device is waiting for a response packet from the Shimmer Device 
	protected boolean mTransactionCompleted=true;									// Variable is used to ensure a command has finished execution prior to executing the next command (see initialize())
	protected boolean mIsConnected = false;
	protected boolean mIsSensing = false;
	protected boolean mIsSDLogging = false;											// This is used to monitor whether the device is in sd log mode
	protected boolean mIsStreaming = false;											// This is used to monitor whether the device is in streaming mode
	protected boolean mIsInitialised = false;
	protected boolean mIsDocked = false;
	protected boolean mHaveAttemptedToReadConfig = false;
	private final int ACK_TIMER_DURATION = 10; 									// Duration to wait for an ack packet (seconds)
	protected boolean mDummy=false;
	protected boolean mFirstTime=false;
	transient ByteArrayOutputStream mByteArrayOutputStream = new ByteArrayOutputStream();
	transient protected Timer mTimerCheckForAckOrResp;								// Timer variable used when waiting for an ack or response packet
	transient protected Timer mTimerCheckAlive;
	transient protected Timer mTimerReadStatus;
	transient protected Timer mTimerReadBattStatus;								// 
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
	protected boolean mSendProgressReport = false;
	protected boolean mOperationUnderway = false;
	public int mFirmwareIdentifier;
	protected boolean mIamAlive = false;
	public String mMyBluetoothAddress;
	public String mComPort;
	
	public void writeInstruction(byte[] instruction){
		getListofInstructions().add(instruction);
	};
	
	/**
	 * @return the mFirmwareIdentifier
	 */
	public int getFirmwareIdentifier() {
		return mFirmwareIdentifier;
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
	public void setInstructionStackLock(boolean state) {
		this.mInstructionStackLock = state;
	}
	
	/**this is to clear the buffer
	 * @throws DeviceException 
	 * 
	 */
	private void clearSerialBuffer() throws DeviceException {
		if(mShimmerRadio.bytesAvailableToBeRead()){
			byte[] buffer;
			try {
				buffer = mShimmerRadio.rxBytes(mShimmerRadio.availableBytes());
			
			printLogDataForDebugging("Discarding:\t\t" + UtilShimmer.bytesToHexStringWithSpacesFormatted(buffer));
			} catch (DeviceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public double getPacketReceptionRate(){
		return mPacketReceptionRate;
	}

	
	public class IOThread extends Thread {
		byte[] tb ={0};
//		byte[] newPacket=new byte[mPacketSize+1];
		public boolean stop = false;
		
		public synchronized void run() {
			while (!stop) {
				try {
				//region --------- Process Instruction on stack --------- 
				// is an instruction running ? if not proceed
				if(!isInstructionStackLock()){
					
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
							if(mCurrentCommand==LiteProtocolInstructionSet.InstructionsSet.SET_RWC_COMMAND_VALUE){
								// for Real-world time -> grab PC time just before
								// writing to Shimmer
								byte[] rtcTimeArray = UtilShimmer.convertSystemTimeToShimmerRtcDataBytes(System.currentTimeMillis());
								System.arraycopy(rtcTimeArray, 0, insBytes, 1, 8);
							}
							//TODO: are the two stops needed here? better to wait for ack from Shimmer
							if(mCurrentCommand==LiteProtocolInstructionSet.InstructionsSet.STOP_STREAMING_COMMAND_VALUE || mCurrentCommand==LiteProtocolInstructionSet.InstructionsSet.STOP_SDBT_COMMAND_VALUE){} 
							else {
								// Overwritten for commands that aren't supported
								// for older versions of Shimmer
								if((mCurrentCommand==LiteProtocolInstructionSet.InstructionsGet.GET_FW_VERSION_COMMAND_VALUE)
										||(mCurrentCommand==LiteProtocolInstructionSet.InstructionsGet.GET_SAMPLING_RATE_COMMAND_VALUE)
										||(mCurrentCommand==LiteProtocolInstructionSet.InstructionsGet.GET_SHIMMER_VERSION_COMMAND_NEW_VALUE)){
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
							try {
								Thread.sleep((int)((Math.random()+.1)*100.0));
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							
								mShimmerRadio.txBytes(insBytes);
							
							printLogDataForDebugging("Command Transmitted: \t\t\t" + mCurrentCommand + " " + UtilShimmer.bytesToHexStringWithSpacesFormatted(insBytes));
	
							//TODO: are the two stops needed here? better to wait for ack from Shimmer
							if(mCurrentCommand==LiteProtocolInstructionSet.InstructionsSet.STOP_STREAMING_COMMAND_VALUE || mCurrentCommand==LiteProtocolInstructionSet.InstructionsSet.STOP_SDBT_COMMAND_VALUE){
								mIsStreaming=false;
								if (mCurrentCommand==LiteProtocolInstructionSet.InstructionsSet.STOP_SDBT_COMMAND_VALUE){
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
						if (!mIsStreaming && !mShimmerRadio.bytesAvailableToBeRead()){
							try {
								Thread.sleep(50);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				}
				//endregion --------- Process Instruction on stack --------- 
				
				if(!mIsStreaming){
					//region --------- Process ACK from a GET or SET command while not streaming --------- 
					if(mWaitForAck) {
	
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
						
						if(mShimmerRadio.bytesAvailableToBeRead()){
							tb=mShimmerRadio.rxBytes(1);
							mNumberofTXRetriesCount = 0;
							mIamAlive = true;
							
							//TODO: ACK is probably now working for STOP_STREAMING_COMMAND so merge in with others?
							if(mCurrentCommand==LiteProtocolInstructionSet.InstructionsSet.STOP_STREAMING_COMMAND_VALUE || mCurrentCommand==LiteProtocolInstructionSet.InstructionsSet.STOP_SDBT_COMMAND_VALUE) { //due to not receiving the ack from stop streaming command we will skip looking for it.
								stopTimerCheckForAckOrResp();
								mIsStreaming=false;
								mTransactionCompleted=true;
								mWaitForAck=false;
								
								delayForBtResponse(200); // Wait to ensure the packet has been fully received
								byteStack.clear();
	
								clearSerialBuffer();
								
								mProtocolListener.hasStopStreaming();					
								getListofInstructions().remove(0);
								getListofInstructions().removeAll(Collections.singleton(null));
								if (mCurrentCommand==LiteProtocolInstructionSet.InstructionsSet.STOP_SDBT_COMMAND_VALUE){
									mProtocolListener.eventLogAndStreamStatusChanged();	
								}
								setInstructionStackLock(false);
							}
//							//TODO: ACK is probably now working for STOP_STREAMING_COMMAND so merge in with others?
//							if(mCurrentCommand==STOP_SDBT_COMMAND) { //due to not receiving the ack from stop streaming command we will skip looking for it.
//								stopTimerCheckForAckOrResp();
//								mIsStreaming=false;
//								mIsSDLogging=false;
//								mTransactionCompleted=true;
//								mWaitForAck=false;
//								
//								delayForBtResponse(200); // Wait to ensure the packet has been fully received
//								byteStack.clear();
//	
//								clearSerialBuffer();
//								
//								hasStopStreaming();					
//								getListofInstructions().remove(0);
//								getListofInstructions().removeAll(Collections.singleton(null));
//								setInstructionStackLock(false);
//							}
							if(tb != null){
								if((byte)tb[0]==(byte)LiteProtocolInstructionSet.InstructionsSet.ACK_COMMAND_PROCESSED_VALUE) {

									mWaitForAck=false;
									printLogDataForDebugging("Ack Received for Command: \t\t" + mCurrentCommand);

									// Send status report if needed by the
									// application and is not one of the below
									// commands that are triggered by timers
									if(mCurrentCommand!=LiteProtocolInstructionSet.InstructionsGet.GET_STATUS_COMMAND_VALUE 
											&& mCurrentCommand!=LiteProtocolInstructionSet.InstructionsSet.TEST_CONNECTION_COMMAND_VALUE 
											&& mCurrentCommand!=LiteProtocolInstructionSet.InstructionsSet.SET_BLINK_LED_VALUE
											//&& mCurrentCommand!= GET_VBATT_COMMAND
											&& mOperationUnderway){
										mProtocolListener.sendProgressReport(new ProgressReportPerCmd(mCurrentCommand, getListofInstructions().size(), mMyBluetoothAddress, mComPort));
									}
									
									// Process if currentCommand is a SET command
									if(LiteProtocolInstructionSet.InstructionsSet.valueOf(mCurrentCommand&0xff)!=null){
										stopTimerCheckForAckOrResp(); //cancel the ack timer
										
										byte[] insBytes = getListofInstructions().get(0);
										mProtocolListener.eventAckReceived(insBytes);
										processAckFromSetCommand(mCurrentCommand);
										mTransactionCompleted = true;
										setInstructionStackLock(false);
									}
									
									// Process if currentCommand is a GET command
									else if(LiteProtocolInstructionSet.InstructionsGet.valueOf(mCurrentCommand&0xff)!=null){
										
										//Special cases
										
										byte[] insBytes = getListofInstructions().get(0);
										mProtocolListener.eventAckReceived(insBytes);
										mWaitForResponse=true;
										getListofInstructions().remove(0);
									}
									
									
								}
							}
						}
					} 
					//endregion --------- Process ACK from a GET or SET command while not streaming --------- 
	
					//region --------- Process RESPONSE while not streaming --------- 
					else if(mWaitForResponse) {
						//Discard first read
						if(mFirstTime){
//							printLogDataForDebugging("First Time read");
//							clearSerialBuffer();
							
							while (mShimmerRadio.availableBytes()!=0){
								int available = mShimmerRadio.availableBytes();
								if (mShimmerRadio.bytesAvailableToBeRead()){
									tb=mShimmerRadio.rxBytes(1);
									String msg = "First Time : " + Arrays.toString(tb);
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
						
						else if(mShimmerRadio.bytesAvailableToBeRead()){
							tb=mShimmerRadio.rxBytes(1);
							mIamAlive = true;
							
							//Check to see whether it is a response byte
							if(LiteProtocolInstructionSet.InstructionsResponse.valueOf(tb[0]&0xff)!=null){
								byte responseCommand = tb[0];
								// response have to read bytes and return the values
								 int lengthOfResponse = (int)LiteProtocolInstructionSet.InstructionsResponse.valueOf(responseCommand&0xff).getValueDescriptor().getOptions().getField(LiteProtocolInstructionSet.getDescriptor().findFieldByName("response_size"));
								if (lengthOfResponse ==-1){
									lengthOfResponse = (int)(mShimmerRadio.rxBytes(1)[0] & 0xFF);
								} 
								byte[] response = mShimmerRadio.rxBytes(lengthOfResponse);
								response = ArrayUtils.addAll(tb,response);
								mProtocolListener.eventNewResponse(response);
								
								//JD: only stop timer after process because there are readbyte opeartions in the processresponsecommand
								stopTimerCheckForAckOrResp(); //cancel the ack timer
								mWaitForResponse=false;
								mTransactionCompleted=true;
								setInstructionStackLock(false);
								printLogDataForDebugging("Response Received:\t\t\t" + LiteProtocolInstructionSet.InstructionsResponse.valueOf(tb[0]&0xff).name());
								
								// Special case for FW_VERSION_RESPONSE because it
								// needs to initialize the Shimmer after releasing
								// the setInstructionStackLock
								/*if(tb[0]==LiteProtocolInstructionSet.Instructions.FW_VERSION_RESPONSE_VALUE){
									if(getHardwareVersion()==HW_ID.SHIMMER_2R){
										initializeShimmer2R();
									} 
									else if(getHardwareVersion()==HW_ID.SHIMMER_3) {
										initializeShimmer3();
									}
									
									startTimerCheckIfAlive();
	//								readShimmerVersion();
								}*/
							}
						}
					} 
					//endregion --------- Process RESPONSE while not streaming --------- 
					
					
					//region --------- Process LogAndStream INSTREAM_CMD_RESPONSE while not streaming --------- 
					if(getFirmwareIdentifier()==FW_ID.LOGANDSTREAM
							&& !mWaitForAck 
							&& !mWaitForResponse 
							&& mShimmerRadio.bytesAvailableToBeRead()) {
						
						tb=mShimmerRadio.rxBytes(1);
						if(tb != null){
							if(tb[0]==LiteProtocolInstructionSet.InstructionsSet.ACK_COMMAND_PROCESSED_VALUE) {
								printLogDataForDebugging("ACK RECEIVED , Connected State!!");
								tb = mShimmerRadio.rxBytes(1);
								if (tb!=null){ //an android fix.. not fully investigated (JC)
									if(tb[0]==LiteProtocolInstructionSet.InstructionsSet.ACK_COMMAND_PROCESSED_VALUE){
										tb = mShimmerRadio.rxBytes(1);
									}
									if(tb[0]==LiteProtocolInstructionSet.InstructionsResponse.INSTREAM_CMD_RESPONSE_VALUE){
										processInstreamResponse();
									}
								}
							}
						}

						
						clearSerialBuffer();
					}
					//endregion --------- Process LogAndStream INSTREAM_CMD_RESPONSE while not streaming --------- 
				}
				
				
				//region --------- Process while streaming --------- 
				else if(mIsStreaming){ // no need for if statement, just for readability
					// TODO: currently reads byte-by-byte. E.g. a
					// Shimmer with fs=1000Hz with 20 bytes payload will
					// enter this loop 20,000 a second -> change to read all
					// from serial and then process
					tb = mShimmerRadio.rxBytes(1);
					if(tb!=null){
						mByteArrayOutputStream.write(tb[0]);
						//Everytime a byte is received the timestamp is taken
						mListofPCTimeStamps.add(System.currentTimeMillis());
					} 
					else {
						printLogDataForDebugging("readbyte null");
					}

					//If there is a full packet and the subsequent sequence number of following packet
					if(mByteArrayOutputStream.size()>=mPacketSize+2){ // +2 because there are two acks
						mIamAlive = true;
						byte[] bufferTemp = mByteArrayOutputStream.toByteArray();
						
						//Data packet followed by another data packet
						if(bufferTemp[0]==LiteProtocolInstructionSet.InstructionsSet.DATA_PACKET_VALUE && bufferTemp[mPacketSize+1]==LiteProtocolInstructionSet.InstructionsSet.DATA_PACKET_VALUE){
							//Handle the data packet
							processDataPacket(bufferTemp);
							clearSingleDataPacketFromBuffers(bufferTemp, mPacketSize+1);
						} 
						
						//Data packet followed by an ACK (suggesting an ACK in response to a SET BT command or else a BT response command)
						else if(bufferTemp[0]==LiteProtocolInstructionSet.InstructionsSet.DATA_PACKET_VALUE && bufferTemp[mPacketSize+1]==LiteProtocolInstructionSet.InstructionsSet.ACK_COMMAND_PROCESSED_VALUE){
							if(mByteArrayOutputStream.size()>mPacketSize+2){
								
								if(bufferTemp[mPacketSize+2]==LiteProtocolInstructionSet.InstructionsSet.DATA_PACKET_VALUE){
									//Firstly handle the data packet
									processDataPacket(bufferTemp);
									clearSingleDataPacketFromBuffers(bufferTemp, mPacketSize+2);
									
									//Then handle the ACK from the last SET command
									if(LiteProtocolInstructionSet.InstructionsSet.valueOf(mCurrentCommand&0xff)!=null){
										stopTimerCheckForAckOrResp(); //cancel the ack timer
										mWaitForAck=false;
										
										processAckFromSetCommand(mCurrentCommand);
										
										mTransactionCompleted = true;
										setInstructionStackLock(false);
									}
									printLogDataForDebugging("Ack Received for Command: \t\t\t" + mCurrentCommand);
								}
								
								//this is for LogAndStream support, command is transmitted and ack received
								else if(getFirmwareIdentifier()==FW_ID.LOGANDSTREAM && bufferTemp[mPacketSize+2]==LiteProtocolInstructionSet.InstructionsResponse.INSTREAM_CMD_RESPONSE_VALUE){ 
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
								discardFirstBufferByte(); //throw the first byte away
							}
							
						} 
						//TODO: ACK in bufferTemp[0] not handled
						//else if
						else {
							discardFirstBufferByte(); //throw the first byte away
						}
					} 
				}
				//endregion --------- Process while streaming --------- 
				} catch (DeviceException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} //End While loop
		} // End run
	} // End IOThread
	
	public void printLogDataForDebugging(String msg){
		System.out.println(msg);
		
	}
	public synchronized void startTimerCheckForAckOrResp(int seconds) {
		//public synchronized void responseTimer(int seconds) {
			if(mTimerCheckForAckOrResp!=null) {
				mTimerCheckForAckOrResp.cancel();
				mTimerCheckForAckOrResp.purge();
				mTimerCheckForAckOrResp = null;
			}
			
			printLogDataForDebugging("Waiting for ack/response for command:\t" + mCurrentCommand);
			mTimerCheckForAckOrResp = new Timer("Shimmer_" + "_TimerCheckForResp");
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
				printLogDataForDebugging("Command:\t" + mCurrentCommand +" timeout");
				if(mWaitForAck){
					printLogDataForDebugging("Ack not received");
				}
				if(mWaitForResponse) {
					printLogDataForDebugging("Response not received");
					
				}
				if(mIsStreaming && getPacketReceptionRate()<100){
					printLogDataForDebugging("Packet RR:  " + Double.toString(getPacketReceptionRate()));
				} 
				
				//handle the special case when we are starting/stopping to log in Consensys and we do not get the ACK response
				//we will send the status changed to the GUI anyway
				if(mCurrentCommand==LiteProtocolInstructionSet.InstructionsSet.START_LOGGING_ONLY_COMMAND_VALUE){
					
					printLogDataForDebugging("START_LOGGING_ONLY_COMMAND response not received. Send feedback to the GUI without killing the connection");
					
					mIsSDLogging = true;
					mProtocolListener.eventLogAndStreamStatusChanged();
					mWaitForAck=false;
					mWaitForResponse=false;
					
					getListofInstructions().remove(0);
					mTransactionCompleted = true;
					setInstructionStackLock(false);
					
					return;
				}
				else if(mCurrentCommand==LiteProtocolInstructionSet.InstructionsSet.STOP_LOGGING_ONLY_COMMAND_VALUE){
					
					printLogDataForDebugging("STOP_LOGGING_ONLY_COMMAND response not received. Send feedback to the GUI without killing the connection");
					
					mIsSDLogging = false;
					mProtocolListener.eventLogAndStreamStatusChanged();
					mWaitForAck=false;
					mWaitForResponse=false;
					
					getListofInstructions().remove(0);
					mTransactionCompleted = true;
					setInstructionStackLock(false);
					
					return;
				}
				

				if(mCurrentCommand==LiteProtocolInstructionSet.InstructionsGet.GET_FW_VERSION_COMMAND_VALUE){
					
				}
				else if(mCurrentCommand==LiteProtocolInstructionSet.InstructionsGet.GET_SAMPLING_RATE_COMMAND_VALUE && !mIsInitialised){
					mFirstTime=false;
				} 
				else if(mCurrentCommand==LiteProtocolInstructionSet.InstructionsGet.GET_SHIMMER_VERSION_COMMAND_NEW_VALUE){ //in case the new command doesn't work, try the old command
					mFirstTime=false;
					
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
					
					try {
						if (mShimmerRadio.bytesAvailableToBeRead()){
							try {
								mShimmerRadio.rxBytes(mShimmerRadio.availableBytes());
							} catch (DeviceException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					} catch (DeviceException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					stopTimerCheckForAckOrResp(); //Terminate the timer thread
					printLogDataForDebugging("RETRY TX COUNT: " + Integer.toString(mNumberofTXRetriesCount));
					if (mNumberofTXRetriesCount>=NUMBER_OF_TX_RETRIES_LIMIT){
						try {
							mShimmerRadio.disconnect();
						} catch (DeviceException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}	
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
	
	
	
	public void stopTimerCheckForAckOrResp(){
		//Terminate the timer thread
		if(mTimerCheckForAckOrResp!=null){
			mTimerCheckForAckOrResp.cancel();
			mTimerCheckForAckOrResp.purge();
			mTimerCheckForAckOrResp = null;
		}
	}
	/**
	 * Due to the nature of the Bluetooth SPP stack a delay has been added to
	 * ensure the buffer is filled before it is read
	 * 
	 */
	private void delayForBtResponse(long millis){
		try {
			Thread.sleep(millis);	
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private void processAckFromSetCommand(byte currentCommand) {
		// check for null and size were put in because if Shimmer was abruptly
		// disconnected there is sometimes indexoutofboundsexceptions
		if(getListofInstructions().size() > 0){
			if(getListofInstructions().get(0)!=null){
				mProtocolListener.eventAckInstruction(getListofInstructions().get(0));
				
				if(currentCommand==LiteProtocolInstructionSet.InstructionsSet.START_STREAMING_COMMAND_VALUE || currentCommand==LiteProtocolInstructionSet.InstructionsSet.START_SDBT_COMMAND_VALUE) {
					mIsStreaming=true;
					if(currentCommand==LiteProtocolInstructionSet.InstructionsSet.START_SDBT_COMMAND_VALUE){
						mIsSDLogging = true;
						//logAndStreamStatusChanged();
					}
					byteStack.clear();
					//isNowStreaming();
				}
				else if((currentCommand==LiteProtocolInstructionSet.InstructionsSet.STOP_STREAMING_COMMAND_VALUE)||(currentCommand==LiteProtocolInstructionSet.InstructionsSet.STOP_SDBT_COMMAND_VALUE)){
					mIsStreaming=false;
					if(currentCommand==LiteProtocolInstructionSet.InstructionsSet.STOP_SDBT_COMMAND_VALUE) {
						mIsSDLogging=false;
						//logAndStreamStatusChanged();
					}
					
					byteStack.clear();

					try {
						clearSerialBuffer();
					} catch (DeviceException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					//hasStopStreaming();					
					getListofInstructions().removeAll(Collections.singleton(null));
				}
			}
		}
		
		getListofInstructions().remove(0);
	}
		
	
	/**Gets pc time and writes the 8 byte value to shimmer device
	 * 
	 */
	public void writeRealTimeClock(){
		if(getFirmwareIdentifier()==FW_ID.LOGANDSTREAM){
			//Just fill empty bytes here for RTC, set them just before writing to Shimmer
		    byte[] bytearraycommand= new byte[9];
			bytearraycommand[0]=(byte) LiteProtocolInstructionSet.InstructionsSet.SET_RWC_COMMAND_VALUE;
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
		
		mProtocolListener.eventNewPacket(newPacket);
		
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

	
	
	
	private void processInstreamResponse(){


		// responses to in-stream response
		
		byte[] inStreamResponseCommand;
		try {
			inStreamResponseCommand = mShimmerRadio.rxBytes(1);
		
		printLogDataForDebugging("In-stream received = " + LiteProtocolInstructionSet.InstructionsResponse.valueOf(inStreamResponseCommand[0]).name());

		if(inStreamResponseCommand[0]==LiteProtocolInstructionSet.InstructionsResponse.DIR_RESPONSE_VALUE){ 
			byte[] responseData = mShimmerRadio.rxBytes(1);
			int directoryNameLength = responseData[0];
			byte[] bufferDirectoryName = new byte[directoryNameLength];
			bufferDirectoryName = mShimmerRadio.rxBytes(directoryNameLength);
			String tempDirectory = new String(bufferDirectoryName);
			String directoryName = tempDirectory;
			printLogDataForDebugging("Directory Name = "+ directoryName);
			bufferDirectoryName = ArrayUtils.addAll(responseData, bufferDirectoryName);
			mProtocolListener.eventNewResponse(bufferDirectoryName);
		}
		else if(inStreamResponseCommand[0]==LiteProtocolInstructionSet.InstructionsResponse.STATUS_RESPONSE_VALUE){
			byte[] responseData = mShimmerRadio.rxBytes(1);
			

			if(!mIsSensing){
				//if(!isInitialised()){
					writeRealTimeClock();
				//}
			}
			mProtocolListener.eventLogAndStreamStatusChanged();
			
			
		} 
		else if(inStreamResponseCommand[0]==LiteProtocolInstructionSet.InstructionsResponse.VBATT_RESPONSE_VALUE) {
			byte[] responseData = mShimmerRadio.rxBytes(3); 
			responseData = ArrayUtils.addAll(inStreamResponseCommand, responseData);
			mProtocolListener.eventNewResponse(responseData);
		}
	
		} catch (DeviceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}

	@Override
	public void initialize() {
		// TODO Auto-generated method stub
		mIOThread = new IOThread();
		mIOThread.start();
	
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		mIOThread.stop=true;
		mIOThread = null;
	}
}
