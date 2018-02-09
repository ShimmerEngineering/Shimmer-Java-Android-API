package com.shimmerresearch.comms.radioProtocol;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.shimmerresearch.bluetooth.ShimmerBluetooth.BT_STATE;
import com.shimmerresearch.comms.serialPortInterface.InterfaceSerialPortHal;
import com.shimmerresearch.exceptions.ShimmerException;

public abstract class AbstractCommsProtocol {
	
	protected ProtocolListener mProtocolListener;
	protected InterfaceSerialPortHal mCommsInterface; //every radio protocol requires radio control
	protected int mPacketSize = 0;
	
	protected int mNumOfMemSetCmds;
	protected byte[] mMemBuffer = new byte[]{};
	protected HashMap<Integer, MemReadDetails> mMapOfMemReadDetails = new HashMap<Integer, MemReadDetails>();

	public boolean mFirstTime=false;
	public boolean mIamAlive = false;

	public abstract void initialize();
	public abstract void writeInstruction(byte[] instruction);
	public abstract void stopProtocol();
	protected abstract void writeInfoMem(int startAddress, byte[] buf);
	protected abstract void readInfoMem(int startAddress, int size);
	public abstract void readCalibrationDump();
	public abstract void readCalibrationDump(int startAddress, int size);
	public abstract void writeCalibrationDump(byte[] calibDump);

	public abstract void readMemCommand(int command, int address, int size);
	public abstract void writeMemCommand(int command, int address, byte[] infoMemBytes);
	
	public abstract List<byte []> getListofInstructions();
	

	public abstract void toggleLed();
	public abstract void readFWVersion();
	public abstract void readShimmerVersionNew();
	public abstract void readPressureCalibrationCoefficients();

	public abstract void setInstructionStackLock(boolean state);

	public abstract void readExpansionBoardID();
	public abstract void readLEDCommand();
	public abstract void readStatusLogAndStream();
	public abstract void readBattery();
	public abstract void readRealTimeClock();
	public abstract void inquiry();
	
	public abstract void startTimerCheckIfAlive();
	public abstract void stopTimerCheckIfAlive();
	public abstract void startTimerReadStatus();
	public abstract void stopTimerReadStatus();
	public abstract void startTimerReadBattStatus();
	public abstract void stopTimerReadBattStatus();

	public abstract void operationPrepare();
	public abstract void operationStart(BT_STATE btState);

	public abstract void writeEnabledSensors(long enabledSensors);

	public abstract void startStreaming();
	public abstract void startDataLogAndStreaming();
	public abstract void startSDLogging();
	public abstract void stopSDLogging();
	public abstract void stopStreaming();
	public abstract void stopStreamingAndLogging();
	
	/** only used for the STC3100 chip in the Shimmer4*/
	public abstract void writeBattStatusPeriod(int periodInSec);
	public abstract void readBattStatusPeriod();

	public abstract void restartTimersIfNull();
	

	/**When using this, it is required that the byteleveldatacomm is set using the serByteLevelDataComm
	 * 
	 */
	public AbstractCommsProtocol(){
	}
	
	public AbstractCommsProtocol(InterfaceSerialPortHal commsInterface){
		setByteLevelDataComm(commsInterface);
	}
	
	public void setByteLevelDataComm(InterfaceSerialPortHal commsInterface){
		mCommsInterface = commsInterface;
	}
	
	public void setProtocolListener(ProtocolListener protocolListener) throws ShimmerException{
		if (mProtocolListener==null){
			mProtocolListener = protocolListener;
		} else {
			throw new ShimmerException("Only One Listener Allowed");
		}
	}


	public void setPacketSize(int pSize){
		mPacketSize = pSize;
	}
	
	public int getPacketSize(){
		return mPacketSize;
	}

	public void writeMem(int command, int startAddress, byte[] buf, int maxMemAddress){
		this.mNumOfMemSetCmds  = 0;
		
//		if(this.getFirmwareVersionCode()>=6){
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
//		}
		
	}
	
	public void readMem(int command, int address, int size, int maxMemAddress){
//		if(this.getFirmwareVersionCode()>=6){
			mMapOfMemReadDetails.put(command, new MemReadDetails(command, address, size, maxMemAddress));

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
					readMemCommand(command, currentStartAddr, currentPacketNumBytes);
					
					currentBytePointer += currentPacketNumBytes;
					numBytesRemaining -= currentPacketNumBytes;
					currentStartAddr += currentPacketNumBytes;
				}
//				utilDock.consolePrintLn(mDockID + " - InfoMem Configuration Read = SUCCESS");
			}
//		}
	}

	protected void clearMemReadBuffers() {
		mMapOfMemReadDetails.clear();
		mMemBuffer = new byte[]{};
	}

	protected void clearMemReadBuffer(int command) {
		mMapOfMemReadDetails.remove(command);
		mMemBuffer = new byte[]{};
	}
	
}