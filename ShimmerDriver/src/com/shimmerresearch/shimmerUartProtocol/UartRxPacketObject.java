package com.shimmerresearch.shimmerUartProtocol;

import java.util.Arrays;

import com.shimmerresearch.driver.UtilShimmer;
import com.shimmerresearch.shimmerUartProtocol.UartPacketDetails.UART_COMPONENT;
import com.shimmerresearch.shimmerUartProtocol.UartPacketDetails.UART_COMPONENT_PROPERTY;
import com.shimmerresearch.shimmerUartProtocol.UartPacketDetails.UART_PACKET_CMD;

public class UartRxPacketObject{
	
	public static final int UNKNOWN = -1;
	public byte[] mRxBuf = null;
	public byte mUartCommandByte = UNKNOWN;
	public byte mUartComponentByte = UNKNOWN;
	public byte mUartPropertyByte = UNKNOWN;
	public int mPayloadLength = UNKNOWN;

	public byte[] mPayload = null;
	public byte[] mCrc = null;

	public UartRxPacketObject(byte[] rxBuf) {
		mRxBuf = rxBuf;
		mUartCommandByte = mRxBuf[1];
		
		if(mUartCommandByte == UartPacketDetails.UART_PACKET_CMD.DATA_RESPONSE.toCmdByte()){
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
	
	public String getUartCommandParsed(){
		UART_PACKET_CMD command = UartPacketDetails.getUartCommandParsed(mUartCommandByte);
		if(command!=null){
			return command.toString();
		}
		else {
			return UtilShimmer.byteToHexStringFormatted(mUartCommandByte);
		}
	}
	
	public String getUartComponentParsed(){
		UART_COMPONENT component = UartPacketDetails.getUartComponentParsed(mUartCommandByte);
		if(component!=null){
			return component.toString();
		}
		else {
			return UtilShimmer.byteToHexStringFormatted(mUartComponentByte);
		}
	}
	
	public String getUartPropertyParsed(){
		UartComponentPropertyDetails componentProperty = UartPacketDetails.getUartPropertyParsed(mUartComponentByte, mUartPropertyByte);
		if(componentProperty!=null){
			return componentProperty.mPropertyName;
		}
		else {
			return UtilShimmer.byteToHexStringFormatted(mUartPropertyByte);
		}
	}
	
	
}