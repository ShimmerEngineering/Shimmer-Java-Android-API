package com.shimmerresearch.verisense.communication.payloads;

import java.util.TimeZone;

import org.apache.commons.lang3.ArrayUtils;
import org.bouncycastle.util.encoders.Hex;

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
	abstract public byte[] generatePayloadContents();

	abstract public String generateDebugString();

	public static long parseByteArrayAtIndex(byte[] byteArray, int startByteIndex, CHANNEL_DATA_TYPE dataType) {
		return parseByteArrayAtIndex(byteArray, startByteIndex, dataType, CHANNEL_DATA_ENDIAN.LSB);
	}

	public static long parseByteArrayAtIndex(byte[] byteArray, int startByteIndex, CHANNEL_DATA_TYPE dataType, CHANNEL_DATA_ENDIAN byteEndian) {
		byte[] buf = new byte[dataType.getNumBytes()];
		System.arraycopy(byteArray, startByteIndex, buf, 0, buf.length);
		return UtilParseData.parseData(buf, dataType, byteEndian);
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

	public static String parseVerisenseId(byte[] payloadContents, int startIndex) {
		byte[] idBytes = new byte[6];
		System.arraycopy(payloadContents, startIndex, idBytes, 0, idBytes.length);
		ArrayUtils.reverse(idBytes);
		String verisenseId = Hex.toHexString(idBytes).replace("-", "");
		verisenseId = verisenseId.toUpperCase();
		return verisenseId;
	}
	
	public static byte[] generateVerisenseIdBytes(String verisenseId) {
		byte[] idBytes = Hex.decode(verisenseId);
		ArrayUtils.reverse(idBytes);
		return idBytes;
	}

}
