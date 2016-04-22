package com.shimmerresearch.comms.radioProtocol;

public class RadioProtocol {
	ProtocolListener mProtocolListener;
	private ShimmerRadioProtocol mShimmerRadio; //every radio protocol requires radio control
	
	public RadioProtocol(ShimmerRadioProtocol shimmerRadio){
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
