package com.shimmerresearch.verisense.communication.payloads;

import java.io.Serializable;

import org.apache.commons.lang3.ArrayUtils;
import org.bouncycastle.util.encoders.Hex;

import com.shimmerresearch.driverUtilities.ExpansionBoardDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.FW_ID;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;
import com.shimmerresearch.verisense.VerisenseDevice;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_TYPE;

/**
 * @author Mark Nolan
 *
 */
public class ProductionConfigPayload extends AbstractPayload implements Serializable{

	public String verisenseId;
	public String manufacturingOrderNumber;
	public String macIdShort;
	
	public ShimmerVerObject shimmerVerObject;
	public ExpansionBoardDetails expansionBoardDetails;

	public ProductionConfigPayload() {
	}

	public ProductionConfigPayload(String macIdShort, String manufacturingOrderNumber, int hwRevMajor, int hwRevMinor, int fwRevMajor, int fwRevMinor, int fwRevInternal) {
		this.macIdShort = macIdShort.toUpperCase();
		this.manufacturingOrderNumber = manufacturingOrderNumber;
		expansionBoardDetails = new ExpansionBoardDetails(hwRevMajor, hwRevMinor, 0);
		shimmerVerObject = new ShimmerVerObject(-1, fwRevMajor, fwRevMinor, fwRevInternal);
	}

	@Override
	public boolean parsePayloadContents(byte[] payloadContents) {
		super.payloadContents = payloadContents;
		isSuccess = false;

		if (payloadContents[0] != VALID_CONFIG_BYTE) {
			return false;
		}

		verisenseId = parseVerisenseId(payloadContents, 1);
		manufacturingOrderNumber = verisenseId.substring(0, 8);
		macIdShort = verisenseId.substring(8, 12);
		
		int hwRevMajor = payloadContents[7];
		hwRevMajor = VerisenseDevice.correctHwVersion(hwRevMajor);
		int hwRevMinor = payloadContents[8];
		expansionBoardDetails = new ExpansionBoardDetails(hwRevMajor, hwRevMinor, 0);
		
		int fwRevMajor = payloadContents[9];
		int fwRevMinor = payloadContents[10];
		int fwRevInternal = 0;
		if (payloadContents.length >= 12) {
			fwRevInternal = (int) parseByteArrayAtIndex(payloadContents, 11, CHANNEL_DATA_TYPE.UINT16);
		}
		shimmerVerObject = new ShimmerVerObject(FW_ID.VERISENSE, fwRevMajor, fwRevMinor, fwRevInternal);
		
		isSuccess = true;
		return isSuccess;
	}
	
	@Override
	public byte[] generatePayloadContents() {
		byte[] payloadContents = new byte[13];
		
		payloadContents[0] = VALID_CONFIG_BYTE;
		
		verisenseId = manufacturingOrderNumber + macIdShort;
		byte[] idBytes = generateVerisenseIdBytes(verisenseId);
		System.arraycopy(idBytes, 0, payloadContents, 1, idBytes.length);

		payloadContents[7] = (byte) expansionBoardDetails.getExpansionBoardId();
		payloadContents[8] = (byte) expansionBoardDetails.getExpansionBoardRev();
		//NOTE: Verisense FW won't allow it's own FW version to be overwritten but no harm in putting structure here
		payloadContents[9] = (byte) shimmerVerObject.getFirmwareVersionMajor();
		payloadContents[10] = (byte) shimmerVerObject.getFirmwareVersionMinor();

		payloadContents[11] = (byte) (shimmerVerObject.getFirmwareVersionInternal() & 0xFF);
		payloadContents[12] = (byte) ((shimmerVerObject.getFirmwareVersionInternal() >> 8) & 0xFF);

		super.payloadContents = payloadContents;
		return payloadContents;
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
