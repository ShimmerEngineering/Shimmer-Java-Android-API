package com.shimmerresearch.shimmerUartProtocol;

import com.shimmerresearch.shimmerUartProtocol.ShimmerUart.UartRxPacketObject;

public interface UartRxCallback {

	public void newMsg(byte[] rxBuf);
	
	public void newParsedMsg(UartRxPacketObject uartRxPacketObject);


}
