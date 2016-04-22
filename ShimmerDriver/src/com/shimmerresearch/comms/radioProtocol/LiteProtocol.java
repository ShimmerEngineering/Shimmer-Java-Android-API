package com.shimmerresearch.comms.radioProtocol;

import java.util.Arrays;
import java.util.Collections;

import com.shimmerresearch.bluetooth.ProgressReportPerCmd;
import com.shimmerresearch.driver.UtilShimmer;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.FW_ID;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.HW_ID;

public class LiteProtocol extends RadioProtocol{

	
	
	public LiteProtocol(ShimmerRadioProtocol shimmerRadio) {
		super(shimmerRadio);
		// TODO Auto-generated constructor stub
	}

	transient protected IOThread mIOThread;
	
	
	
	public class IOThread extends Thread {
		byte[] tb ={0};
//		byte[] newPacket=new byte[mPacketSize+1];
		public boolean stop = false;
		
		public synchronized void run() {
			while (!stop) {
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
							if(mCurrentCommand==SET_RWC_COMMAND){
								// for Real-world time -> grab PC time just before
								// writing to Shimmer
								byte[] rtcTimeArray = UtilShimmer.convertSystemTimeToShimmerRtcDataBytes(System.currentTimeMillis());
								System.arraycopy(rtcTimeArray, 0, insBytes, 1, 8);
							}
							//TODO: are the two stops needed here? better to wait for ack from Shimmer
							if(mCurrentCommand==STOP_STREAMING_COMMAND || mCurrentCommand==STOP_SDBT_COMMAND){} 
							else {
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
								}
							}
							try {
								Thread.sleep((int)((Math.random()+.1)*100.0));
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							mShimmerRadio.writeBytes(insBytes);
							printLogDataForDebugging("Command Transmitted: \t\t\t" + btCommandToString(mCurrentCommand) + " " + UtilShimmer.bytesToHexStringWithSpacesFormatted(insBytes));
	
							//TODO: are the two stops needed here? better to wait for ack from Shimmer
							if(mCurrentCommand==STOP_STREAMING_COMMAND || mCurrentCommand==STOP_SDBT_COMMAND){
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
						
						if(bytesAvailableToBeRead()){
							tb=readBytes(1);
							mNumberofTXRetriesCount = 0;
							mIamAlive = true;
							
							//TODO: ACK is probably now working for STOP_STREAMING_COMMAND so merge in with others?
							if(mCurrentCommand==STOP_STREAMING_COMMAND || mCurrentCommand==STOP_SDBT_COMMAND) { //due to not receiving the ack from stop streaming command we will skip looking for it.
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
									logAndStreamStatusChanged();	
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
								if((byte)tb[0]==ACK_COMMAND_PROCESSED) {

									mWaitForAck=false;
									printLogDataForDebugging("Ack Received for Command: \t\t" + btCommandToString(mCurrentCommand));

									// Send status report if needed by the
									// application and is not one of the below
									// commands that are triggered by timers
									if(mCurrentCommand!=GET_STATUS_COMMAND 
											&& mCurrentCommand!=TEST_CONNECTION_COMMAND 
											&& mCurrentCommand!=SET_BLINK_LED 
											//&& mCurrentCommand!= GET_VBATT_COMMAND
											&& mOperationUnderway){
										sendProgressReport(new ProgressReportPerCmd(mCurrentCommand, getListofInstructions().size(), mMyBluetoothAddress, mComPort));
									}
									
									// Process if currentCommand is a SET command
									if(mBtSetCommandMap.containsKey(mCurrentCommand)){
										stopTimerCheckForAckOrResp(); //cancel the ack timer
										
										processAckFromSetCommand(mCurrentCommand);
										
										mTransactionCompleted = true;
										setInstructionStackLock(false);
									}
									
									// Process if currentCommand is a GET command
									else if(mBtGetCommandMap.containsKey(mCurrentCommand)){
										
										//Special cases
										byte[] insBytes = getListofInstructions().get(0);
										if(mCurrentCommand==GET_EXG_REGS_COMMAND){
											// Need to store ExG chip number before receiving response
											mTempChipID = insBytes[1];
										}
										else if(mCurrentCommand==GET_INFOMEM_COMMAND){
											// store current address/InfoMem segment
											mCurrentInfoMemAddress = ((insBytes[3]&0xFF)<<8)+(insBytes[2]&0xFF);
											mCurrentInfoMemLengthToRead = (insBytes[1]&0xFF);
										}

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
							
							while (availableBytes()!=0){
								int available = availableBytes();
								if (bytesAvailableToBeRead()){
									tb=readBytes(1);
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
						
						else if(bytesAvailableToBeRead()){
							tb=readBytes(1);
							mIamAlive = true;
							
							//Check to see whether it is a response byte
							if(mBtResponseMap.containsKey(tb[0])){
								byte responseCommand = tb[0];
								
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
								if(tb[0]==FW_VERSION_RESPONSE){
									if(getHardwareVersion()==HW_ID.SHIMMER_2R){
										initializeShimmer2R();
									} 
									else if(getHardwareVersion()==HW_ID.SHIMMER_3) {
										initializeShimmer3();
									}
									
									startTimerCheckIfAlive();
	//								readShimmerVersion();
								}
							}
						}
					} 
					//endregion --------- Process RESPONSE while not streaming --------- 
					
					
					//region --------- Process LogAndStream INSTREAM_CMD_RESPONSE while not streaming --------- 
					if(getFirmwareIdentifier()==FW_ID.LOGANDSTREAM
							&& !mWaitForAck 
							&& !mWaitForResponse 
							&& bytesAvailableToBeRead()) {
						
						tb=readBytes(1);
						if(tb != null){
							if(tb[0]==ACK_COMMAND_PROCESSED) {
								consolePrintLn("ACK RECEIVED , Connected State!!");
								tb = readBytes(1);
								if (tb!=null){ //an android fix.. not fully investigated (JC)
									if(tb[0]==ACK_COMMAND_PROCESSED){
										tb = readBytes(1);
									}
									if(tb[0]==INSTREAM_CMD_RESPONSE){
										processResponseCommand(INSTREAM_CMD_RESPONSE);
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
					tb = readBytes(1);
					if(tb!=null){
						mByteArrayOutputStream.write(tb[0]);
						//Everytime a byte is received the timestamp is taken
						mListofPCTimeStamps.add(System.currentTimeMillis());
					} 
					else {
						consolePrint("readbyte null");
					}

					//If there is a full packet and the subsequent sequence number of following packet
					if(mByteArrayOutputStream.size()>=mPacketSize+2){ // +2 because there are two acks
						mIamAlive = true;
						byte[] bufferTemp = mByteArrayOutputStream.toByteArray();
						
						//Data packet followed by another data packet
						if(bufferTemp[0]==DATA_PACKET && bufferTemp[mPacketSize+1]==DATA_PACKET){
							//Handle the data packet
							processDataPacket(bufferTemp);
							clearSingleDataPacketFromBuffers(bufferTemp, mPacketSize+1);
						} 
						
						//Data packet followed by an ACK (suggesting an ACK in response to a SET BT command or else a BT response command)
						else if(bufferTemp[0]==DATA_PACKET && bufferTemp[mPacketSize+1]==ACK_COMMAND_PROCESSED){
							if(mByteArrayOutputStream.size()>mPacketSize+2){
								
								if(bufferTemp[mPacketSize+2]==DATA_PACKET){
									//Firstly handle the data packet
									processDataPacket(bufferTemp);
									clearSingleDataPacketFromBuffers(bufferTemp, mPacketSize+2);
									
									//Then handle the ACK from the last SET command
									if(mBtSetCommandMap.containsKey(mCurrentCommand)){
										stopTimerCheckForAckOrResp(); //cancel the ack timer
										mWaitForAck=false;
										
										processAckFromSetCommand(mCurrentCommand);
										
										mTransactionCompleted = true;
										setInstructionStackLock(false);
									}
									printLogDataForDebugging("Ack Received for Command: \t\t\t" + btCommandToString(mCurrentCommand));
								}
								
								//this is for LogAndStream support, command is transmitted and ack received
								else if(getFirmwareIdentifier()==FW_ID.LOGANDSTREAM && bufferTemp[mPacketSize+2]==INSTREAM_CMD_RESPONSE){ 
									consolePrintLn("COMMAND TXed and ACK RECEIVED IN STREAM");
									consolePrintLn("INS CMD RESP");

									//Firstly handle the in-stream response
									stopTimerCheckForAckOrResp(); //cancel the ack timer
									mWaitForResponse=false;
									mWaitForAck=false;

									processResponseCommand(INSTREAM_CMD_RESPONSE);

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
									consolePrintLn("Unknown parsing error while streaming");
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
				
			} //End While loop
		} // End run
	} // End IOThread
}
