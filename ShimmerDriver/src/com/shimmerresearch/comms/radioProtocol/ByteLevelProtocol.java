package com.shimmerresearch.comms.radioProtocol;

import java.util.List;

import com.shimmerresearch.comms.serialPortInterface.ByteLevelDataComm;

public abstract class ByteLevelProtocol {
	ProtocolListener mProtocolListener;
	ByteLevelDataComm mShimmerRadio; //every radio protocol requires radio control
	int mPacketSize;
	
	/**When using this, it is required that the byteleveldatacomm is set using the serByteLevelDataComm
	 * 
	 */
	public ByteLevelProtocol(){
	
	}
	
	public ByteLevelProtocol(ByteLevelDataComm shimmerRadio){
		mShimmerRadio = shimmerRadio;
	}
	
	public void setByteLevelDataComm(ByteLevelDataComm shimmerRadio){
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
	public abstract List<byte []> getListofInstructions();
	
	public void setPacketSize(int pSize){
		mPacketSize = pSize;
	}
	
	public int getPacketSize(){
		return mPacketSize;
	}
}