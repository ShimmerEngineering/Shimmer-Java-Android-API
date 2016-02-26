package com.shimmerresearch.shimmerUartProtocol;

import java.util.Arrays;

import com.shimmerresearch.driver.UtilShimmer;
import com.shimmerresearch.shimmerUartProtocol.UartPacketDetails.UART_COMPONENT;
import com.shimmerresearch.shimmerUartProtocol.UartPacketDetails.UART_PACKET_CMD;

public class UartRxPacketObject{
	
	public static final int UNKNOWN = -1;
	public byte[] mRxPacket = null;
	public byte mUartCommandByte = UNKNOWN;
	public byte mUartComponentByte = UNKNOWN;
	public byte mUartPropertyByte = UNKNOWN;
	public int mPayloadLength = UNKNOWN;

	public byte[] mPayload = null;
	public byte[] mCrc = null;
	
	public byte[] mLeftOverBytes = null;

	public UartRxPacketObject(byte[] rxBuf) {
		int packetLength = UNKNOWN;
		
		if(rxBuf[0]==UartPacketDetails.PACKET_HEADER.getBytes()[0]){
			mUartCommandByte = rxBuf[1];
			
			if(mUartCommandByte == UartPacketDetails.UART_PACKET_CMD.DATA_RESPONSE.toCmdByte()){
				mPayloadLength = rxBuf[2]&0xFF;
				mUartComponentByte = rxBuf[3];
				mUartPropertyByte = rxBuf[4];
				
				mPayload = Arrays.copyOfRange(rxBuf, 5, 5+mPayloadLength-2);
				mCrc = Arrays.copyOfRange(rxBuf, 5+mPayloadLength-1, 5+mPayloadLength);
				packetLength = 5+mPayloadLength;
			}
			else {
				mCrc = Arrays.copyOfRange(rxBuf, 2, 3);
				packetLength = 4;
			}

			mRxPacket = Arrays.copyOfRange(rxBuf, 0, packetLength);
			
			//split rxBufs if left over bytes
			if(rxBuf.length>packetLength){
				mLeftOverBytes = Arrays.copyOfRange(rxBuf, packetLength+1, rxBuf.length);
			}
		}
		else {
			//First byte isn't the expected header so remove it for next try.
			mLeftOverBytes = Arrays.copyOfRange(rxBuf, 1, rxBuf.length);
		}
	}

//	public byte[] getRxBufWithoutCrc() {
//		return Arrays.copyOfRange(mRxBuf, 0, mRxBuf.length-2);
//	}
	
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
		UART_COMPONENT component = UartPacketDetails.getUartComponentParsed(mUartComponentByte);
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

	public String getConsoleString() {
		return ("Command:" 		+ getUartCommandParsed() 
				+ (mUartComponentByte==UartRxPacketObject.UNKNOWN? "":"\tComponent:" 		+ getUartComponentParsed()) 
				+ (mUartPropertyByte==UartRxPacketObject.UNKNOWN? 	"":"\tProperty:" 		+ getUartPropertyParsed()) 
				+ (mPayload==null? 								"":"\tPayload:" 		+ UtilShimmer.bytesToHexStringWithSpacesFormatted(mPayload)) 
//				+ "\n" 				+ UtilShimmer.bytesToHexStringWithSpacesFormatted(rxBuf)
				);
	}
	
	
}