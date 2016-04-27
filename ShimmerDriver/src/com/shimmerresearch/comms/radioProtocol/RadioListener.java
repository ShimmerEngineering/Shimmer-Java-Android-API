package com.shimmerresearch.comms.radioProtocol;

public interface RadioListener {

	public void connected();
	
	public void disconnected();
	
	public void eventNewPacket(byte[] packetByteArray);
	
	public void eventResponseReceived(byte[] responseBytes);
	
	public void eventAckReceived(byte[] instructionSent);
	
	

}
