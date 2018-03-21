package com.shimmerresearch.comms.wiredProtocol;

import java.util.Arrays;

import com.shimmerresearch.comms.wiredProtocol.UartPacketDetails.UART_COMPONENT;
import com.shimmerresearch.comms.wiredProtocol.UartPacketDetails.UART_COMPONENT_AND_PROPERTY;
import com.shimmerresearch.comms.wiredProtocol.UartPacketDetails.UART_PACKET_CMD;
import com.shimmerresearch.driverUtilities.UtilShimmer;

/**
 * @author Mark Nolan
 *
 */
public class UartRxPacketObject{
	
	public static final int UNKNOWN = -1;
	public byte[] mRxPacket = null;
	public byte mUartCommandByte = UNKNOWN;
	public byte mUartComponentByte = UNKNOWN;
	public byte mUartPropertyByte = UNKNOWN;
	public int mPayloadLength = UNKNOWN;

	public byte[] mPayload = null;
	public byte[] mCrc = null;
	
	//For IEEE802.15.4 AM packets wrapped in the Shimmer CommsProtocol UART protocol
	public int mAmPktRadioDestId = UNKNOWN; 
	public byte mAmPktType = UNKNOWN;
	public byte[] mAmPktPayload = null;
	
	public byte[] mLeftOverBytes = null;
	public long mSystemTimeMillis = UNKNOWN;

	public UartRxPacketObject(byte[] rxBuf) {
		int packetLength = UNKNOWN;
		
		if(rxBuf[0]==UartPacketDetails.PACKET_HEADER.getBytes()[0]){
			mUartCommandByte = rxBuf[1];
			
			if(mUartCommandByte == UartPacketDetails.UART_PACKET_CMD.DATA_RESPONSE.toCmdByte()
					|| mUartCommandByte == UartPacketDetails.UART_PACKET_CMD.READ.toCmdByte()
					|| mUartCommandByte == UartPacketDetails.UART_PACKET_CMD.WRITE.toCmdByte()){
				mPayloadLength = rxBuf[2]&0xFF;
				mUartComponentByte = rxBuf[3];
				mUartPropertyByte = rxBuf[4];
				
				mPayload = Arrays.copyOfRange(rxBuf, 5, 5+mPayloadLength-2);
				
				//Parse wrapped AM packets 
				if(mUartCommandByte==UART_PACKET_CMD.WRITE.toCmdByte()){
					if(mUartComponentByte==UART_COMPONENT.RADIO_802154.toCmdByte()){
						if(mUartPropertyByte==UART_COMPONENT_AND_PROPERTY.RADIO_802154.TX_TO_SHIMMER.mPropertyByte){
							if(mPayload.length>=3) {
								mAmPktRadioDestId = ((mPayload[1]&0xFF)<<8) | (mPayload[0]&0xFF);
								mAmPktType = mPayload[2];
								
								if(mPayload.length>3) {
									mAmPktPayload = new byte[mPayload.length-3];
									System.arraycopy(mPayload, 3, mAmPktPayload, 0, mAmPktPayload.length);
								}
							}
						}
					}
				}
				
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

	public UartRxPacketObject(byte[] rxBuf, long timestampMs) {
		this(rxBuf);
		mSystemTimeMillis = timestampMs;
	}

	public UartRxPacketObject() {
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
		String consoleString = "Command:" 		+ getUartCommandParsed() 
				+ (mUartComponentByte==UartRxPacketObject.UNKNOWN? "":"\tComponent:" 		+ getUartComponentParsed()) 
				+ (mUartPropertyByte==UartRxPacketObject.UNKNOWN? 	"":"\tProperty:" 		+ getUartPropertyParsed());
		
		consoleString += (mPayload==null? "":"\tPayload" + "(" + mPayload.length + "):" + UtilShimmer.bytesToHexStringWithSpacesFormatted(mPayload)); 
//		consoleString += "\n" 				+ UtilShimmer.bytesToHexStringWithSpacesFormatted(mRxPacket);

		if(mAmPktType!=UNKNOWN) {
			consoleString += "\n\tAM Packet -> \tType=" + UtilShimmer.byteToHexStringFormatted((byte) (mAmPktType&0xFF))//AmCommandDetails.getAmTypeParsedmAmType(mAmType);
			+ "\tDest=" + UtilShimmer.intToHexStringFormatted(mAmPktRadioDestId, 2, true)
			+ "\tPayload=" + (mAmPktPayload==null? "NULL":UtilShimmer.bytesToHexStringWithSpacesFormatted(mAmPktPayload));
		}

		return consoleString;
	}
	
	
}