package com.shimmerresearch.verisense.communication.payloads;

import org.apache.commons.lang3.ArrayUtils;

import com.shimmerresearch.driverUtilities.UtilParseData;
import com.shimmerresearch.driverUtilities.UtilShimmer;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_ENDIAN;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_TYPE;

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

	public static String parseVerisenseId(byte[] payloadContents, int startIndex) {
		byte[] idBytes = new byte[6];
		System.arraycopy(payloadContents, startIndex, idBytes, 0, idBytes.length);
		ArrayUtils.reverse(idBytes);
		String verisenseId = UtilShimmer.bytesToHexString(idBytes);
		verisenseId = verisenseId.toUpperCase();
		return verisenseId;
	}
	
	public static byte[] generateVerisenseIdBytes(String verisenseId) {
		byte[] idBytes = UtilShimmer.hexStringToByteArray(verisenseId);
		ArrayUtils.reverse(idBytes);
		return idBytes;
	}

}
