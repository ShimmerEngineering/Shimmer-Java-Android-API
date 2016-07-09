package com.shimmerresearch.comms.radioProtocol;

import java.util.Arrays;
import java.util.List;

import com.shimmerresearch.bluetooth.ShimmerBluetooth.BT_STATE;
import com.shimmerresearch.comms.serialPortInterface.InterfaceByteLevelDataComm;

public abstract class AbstractByteLevelProtocol {
	
	ProtocolListener mProtocolListener;
	InterfaceByteLevelDataComm mShimmerRadio; //every radio protocol requires radio control
	protected int mPacketSize = 0;
	
	protected int mNumOfInfoMemSetCmds;
	protected byte[] mInfoMemBuffer;
	protected int mTotalInfoMemLengthToRead = 0;
	protected int mCurrentInfoMemAddress = 0;
	protected int mCurrentInfoMemLengthToRead = 0;
	
	public boolean mFirstTime=false;
	public boolean mIamAlive = false;



	/**When using this, it is required that the byteleveldatacomm is set using the serByteLevelDataComm
	 * 
	 */
	public AbstractByteLevelProtocol(){
	
	}
	
	public AbstractByteLevelProtocol(InterfaceByteLevelDataComm shimmerRadio){
		mShimmerRadio = shimmerRadio;
	}
	
	public void setByteLevelDataComm(InterfaceByteLevelDataComm shimmerRadio){
		mShimmerRadio = shimmerRadio;
	}
	
	public void setProtocolListener(ProtocolListener protocolListener) throws Exception{
		if (mProtocolListener==null){
			mProtocolListener = protocolListener;
		} else {
			throw new Exception("Only One Listener Allowed");
		}
	}

	public abstract void initialize();
	public abstract void writeInstruction(byte[] instruction);
	public abstract void stop();
	public abstract void stopStreaming();
	public abstract void startStreaming();
	protected abstract void writeInfoMem(int startAddress, byte[] buf);
	protected abstract void readInfoMem(int startAddress, int size);
	
	public abstract void readMemCommand(int command, int address, int size);
	public abstract void writeMemCommand(int command, int address, byte[] infoMemBytes);
	
	public abstract List<byte []> getListofInstructions();
	
	public void setPacketSize(int pSize){
		mPacketSize = pSize;
	}
	
	public int getPacketSize(){
		return mPacketSize;
	}

	public void writeInfoMem(int startAddress, byte[] buf, int maxMemAddress){
		this.mNumOfInfoMemSetCmds  = 0;
		
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

					writeInfoMem(currentStartAddr, infoSegBuf);
					mNumOfInfoMemSetCmds += 1;

					currentStartAddr += currentPacketNumBytes;
					numBytesRemaining -= currentPacketNumBytes;
					currentBytePointer += currentPacketNumBytes;
				}
			}
//		}
		
	}
	
	public void readInfoMem(int address, int size, int maxMemAddress){
//		if(this.getFirmwareVersionCode()>=6){
			mInfoMemBuffer = new byte[size];
			this.mTotalInfoMemLengthToRead = size;


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
					readInfoMem(currentStartAddr, currentPacketNumBytes);
					
					currentBytePointer += currentPacketNumBytes;
					numBytesRemaining -= currentPacketNumBytes;
					currentStartAddr += currentPacketNumBytes;
				}
//				utilDock.consolePrintLn(mDockID + " - InfoMem Configuration Read = SUCCESS");
			}
//		}
	}
	

	public abstract void toggleLed();
	public abstract void readFWVersion();
	public abstract void readShimmerVersion();
	public abstract void readPressureCalibrationCoefficients();

	public abstract void setInstructionStackLock(boolean state);

	public abstract void startTimerCheckIfAlive();
	public abstract void readExpansionBoardID();
	public abstract void readLEDCommand();
	public abstract void readStatusLogAndStream();
	public abstract void readBattery();
	public abstract void inquiry();
	public abstract void startTimerReadStatus();
	public abstract void startTimerReadBattStatus();

	public abstract void operationPrepare();

	public abstract void writeEnabledSensors(long enabledSensors);
	
}