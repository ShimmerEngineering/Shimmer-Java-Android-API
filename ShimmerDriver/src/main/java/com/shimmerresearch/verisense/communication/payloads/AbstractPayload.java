package com.shimmerresearch.verisense.communication.payloads;

import java.util.TimeZone;

import com.shimmerresearch.driverUtilities.UtilParseData;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_ENDIAN;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_TYPE;
import com.shimmerresearch.verisense.UtilVerisenseDriver;

/**
 * @author Mark Nolan
 *
 */
public abstract class AbstractPayload {
	
	public boolean isSuccess;
	protected byte[] payloadContents;

	public static final byte VALID_CONFIG_BYTE = 0x5A;

	protected static final String CONSOLE_DIVIDER_STRING = "********************************************************\n";

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

	public static String millisecondsToStringWitNanos(double timeMs) {
		double timeUs = timeMs/1000;
		int nanos = (int) ((timeUs - ((int)timeUs)) * 1000000);
		
		return millisecondsToString((long) timeMs) + "." + String.format("%06d", nanos);
	}

	public static String millisecondsToString(double timeMs) {
		//TODO decide whether this should be parsed as local time or GMT+0
//		return UtilVerisenseDriver.convertMilliSecondsToCsvHeaderFormat((long) timeMs);
		
		return UtilVerisenseDriver.convertMilliSecondsToFormat((long) timeMs, 
				"yyyy/MM/dd HH:mm:ss", 
				TimeZone.getDefault().getID());
	}

}
