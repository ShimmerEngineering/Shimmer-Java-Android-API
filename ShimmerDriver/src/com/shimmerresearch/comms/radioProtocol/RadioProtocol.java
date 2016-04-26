package com.shimmerresearch.comms.radioProtocol;

import com.shimmerresearch.comms.serialPortInterface.ShimmerSerialPortInterface;

public class RadioProtocol {
	ProtocolListener mProtocolListener;
	ShimmerSerialPortInterface mShimmerRadio; //every radio protocol requires radio control
	
	public RadioProtocol(ShimmerSerialPortInterface shimmerRadio){
		mShimmerRadio = shimmerRadio;
	}
	
	public void setProtocolListener(ProtocolListener protocolListener) throws Exception{
		if (mProtocolListener!=null){
			mProtocolListener = protocolListener;
		} else {
			throw new Exception("Only One Listener Allowed");
		}
	}
}

