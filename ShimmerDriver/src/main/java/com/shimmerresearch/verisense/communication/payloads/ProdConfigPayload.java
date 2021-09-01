package com.shimmerresearch.verisense.communication.payloads;

import org.apache.commons.lang3.ArrayUtils;
import org.bouncycastle.util.encoders.Hex;

import com.shimmerresearch.driverUtilities.ExpansionBoardDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_TYPE;

/**
 * @author Mark Nolan
 *
 */
public class ProdConfigPayload extends AbstractPayload {

	public String verisenseId;
	public String manufacturingOrderNumber;
	public String macIdShort;
	
	public ShimmerVerObject shimmerVerObject;
	public ExpansionBoardDetails expansionBoardDetails;
	
	@Override
	public boolean parsePayloadContents(byte[] payloadContents) {
		super.payloadContents = payloadContents;
		isSuccess = false;

		if (payloadContents[0] != VALID_CONFIG_BYTE) {
			return false;
		}

		byte[] idBytes = new byte[6];
		System.arraycopy(payloadContents, 1, idBytes, 0, idBytes.length);
		ArrayUtils.reverse(idBytes);
		verisenseId = Hex.toHexString(idBytes).replace("-", "");

		manufacturingOrderNumber = verisenseId.substring(0, 8);
		macIdShort = verisenseId.substring(8, 12);
		
		int hwRevMajor = payloadContents[7];
		int hwRevMinor = payloadContents[8];
		expansionBoardDetails = new ExpansionBoardDetails(hwRevMajor, hwRevMinor, 0);
		
		int fwRevMajor = payloadContents[9];
		int fwRevMinor = payloadContents[10];
		int fwRevInternal = 0;
		if (payloadContents.length >= 12) {
			fwRevInternal = (int) parseByteArrayAtIndex(payloadContents, 11, CHANNEL_DATA_TYPE.UINT16);
		}
		shimmerVerObject = new ShimmerVerObject(-1, fwRevMajor, fwRevMinor, fwRevInternal);
		
		isSuccess = true;
		return isSuccess;
	}

	@Override
	public String generateDebugString(){
		StringBuilder sb = new StringBuilder();
		
		sb.append("ASM Production Config:");
		sb.append("\tASM Identifier = " + verisenseId + "\n");
		sb.append("\tManufacturing Order Number = " + manufacturingOrderNumber + "\n");
		sb.append("\tMAC ID = " + macIdShort + "\n");
		sb.append("\tHW = v" + expansionBoardDetails.getExpansionBoardId() + "." + expansionBoardDetails.getExpansionBoardRev() + "\n");
		sb.append("\tFW = v" + shimmerVerObject.getFirmwareVersionMajor() 
		+ "." + shimmerVerObject.getFirmwareVersionMinor() 
		+ "." + shimmerVerObject.getFirmwareVersionInternal() + "\n");

		return sb.toString();
	}
	
}
