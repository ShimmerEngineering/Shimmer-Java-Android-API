package com.shimmerresearch.shimmerUartProtocol;

public interface UartRxCallback {

	public void newMsg(byte[] rxBuf);
	
	public void newParsedMsg(UartRxPacketObject uartRxPacketObject);


}
