package com.shimmerresearch.verisense.communication;

import com.shimmerresearch.driverUtilities.UtilParseData;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_ENDIAN;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_TYPE;

public abstract class AbstractPayload {
	
	public boolean IsSuccess;
	protected byte[] payloadContents;

	public static final byte VALID_CONFIG_BYTE = 0x5A;

	abstract public boolean parsePayloadContents(byte[] payloadContents);

	abstract public String generateDebugString();

	public long parseByteArrayAtIndex(byte[] byteArray, int startByteIndex, int numBytes) {
		byte[] buf = new byte[numBytes];
		System.arraycopy(byteArray, startByteIndex, buf, 0, numBytes);
		
		CHANNEL_DATA_TYPE channelDataType = CHANNEL_DATA_TYPE.UINT16;
		if(numBytes==2) {
			channelDataType = CHANNEL_DATA_TYPE.UINT16;
		} else if(numBytes==3) {
			channelDataType = CHANNEL_DATA_TYPE.UINT24;
		} else if(numBytes==4) {
			channelDataType = CHANNEL_DATA_TYPE.UINT32;
		} else if(numBytes==8) {
			channelDataType = CHANNEL_DATA_TYPE.UINT64;
		}
		
		return UtilParseData.parseData(buf, channelDataType, CHANNEL_DATA_ENDIAN.LSB);
	}

	public byte[] getPayloadContents() {
		return payloadContents;
	}
}
