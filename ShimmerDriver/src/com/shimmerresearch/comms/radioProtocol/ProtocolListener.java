package com.shimmerresearch.comms.radioProtocol;

public interface ProtocolListener {

	public byte[] eventAckReceived();
	public byte[] eventNewPacket();
	public byte[] eventNewResponse();
	
}
