package com.shimmerresearch.shimmerUartProtocol;

import java.util.Arrays;

public class UartRxPacketObject{
	public byte[] mRxBuf = null;
	public byte mUartCommand = 0;
	public byte mUartComponentByte = 0;
	public byte mUartPropertyByte = 0;
	public int mPayloadLength = 0;

	public byte[] mPayload = null;
	public byte[] mCrc = null;

	public UartRxPacketObject(byte[] rxBuf) {
		mRxBuf = rxBuf;
		mUartCommand = mRxBuf[1];
		
		if(mUartCommand == UartPacketDetails.UART_PACKET_CMD.DATA_RESPONSE.toCmdByte()){
			mPayloadLength = mRxBuf[2]&0xFF;
			mUartComponentByte = mRxBuf[3];
			mUartPropertyByte = mRxBuf[4];
			
			mPayload = Arrays.copyOfRange(rxBuf, 5, 5+mPayloadLength-2);
		}
		
		mCrc = Arrays.copyOfRange(rxBuf, rxBuf.length-2, rxBuf.length);
	}

	public byte[] getRxBufWithoutCrc() {
		return Arrays.copyOfRange(mRxBuf, 0, mRxBuf.length-2);
	}
	
	public byte[] getPayload() {
		return mPayload;
	}
}