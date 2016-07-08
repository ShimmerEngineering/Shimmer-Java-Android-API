package com.shimmerresearch.comms.radioProtocol;

public interface RadioListener {

	public void connected();
	
	public void disconnected();
	
	public void eventNewPacket(byte[] packetByteArray);
	
	@Deprecated
	public void eventResponseReceived(byte[] responseBytes);
	
	public void eventResponseReceived(byte response, Object parsedResponse);
	
	
	public void eventAckReceived(byte[] instructionSent);
	

}
