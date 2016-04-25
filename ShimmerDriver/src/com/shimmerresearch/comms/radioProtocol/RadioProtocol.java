package com.shimmerresearch.comms.radioProtocol;

import com.shimmerresearch.comms.serialPortInterface.ShimmerSerialPortInterface;

public abstract class RadioProtocol {
	ProtocolListener mProtocolListener;
	ShimmerSerialPortInterface mShimmerRadio; //every radio protocol requires radio control
	int mPacketSize;
	
	public RadioProtocol(ShimmerSerialPortInterface shimmerRadio){
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
	
	public void setPacketSize(int pSize){
		mPacketSize = pSize;
	}
}

