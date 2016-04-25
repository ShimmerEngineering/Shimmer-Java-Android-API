package com.shimmerresearch.comms.radioProtocol;

public interface RadioListener {

	public void connected();
	
	public void disconnected();
	
	public void eventNewPacket();
	
	public void configurationResponse(byte[] responseBytes);

}
