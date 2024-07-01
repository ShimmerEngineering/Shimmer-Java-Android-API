package com.shimmerresearch.verisense.communication.payloads;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_TYPE;

/**
 * @author Mark Nolan
 *
 */
public class MemoryLookupTablePayload extends AbstractPayload {

	private boolean headTailReceived;
	private long tail;
	private long head;

	List<MemoryBankDetails> listOfMemoryBankDetails = new ArrayList<MemoryBankDetails>();

	public enum BANK_STATUS {
		ZERO(""),
		FULL("Full"),
		TO_DEL("2Del"),
		EMPTY("Emty"),
		BAD("Bad"),
		NUSE("NUse");
		
		private String description;

		private BANK_STATUS(String description) {
			this.description = description;
		}
		
		public static BANK_STATUS getBankStatus(int i) {
			for(BANK_STATUS bankStatus : BANK_STATUS.values()) {
				if(bankStatus.ordinal()==i) {
					return bankStatus;
				}
			}
			return null;
		}
	}
		
	@Override
	public boolean parsePayloadContents(byte[] payloadContents) {
		this.payloadContents = payloadContents;
		isSuccess = false;

		listOfMemoryBankDetails.clear();
		
		int idx = 0;
		if(payloadContents.length== 50692) {
			this.tail = parseByteArrayAtIndex(payloadContents, 0, CHANNEL_DATA_TYPE.UINT16);
			this.head = parseByteArrayAtIndex(payloadContents, 2, CHANNEL_DATA_TYPE.UINT16);
			idx = 4;
			this.headTailReceived = true;
		} else {
			this.headTailReceived = false;
		}
		
		for(int i=idx;i<payloadContents.length;i+=3) {
			byte statusByte = payloadContents[i];
			if(i+1 == payloadContents.length) {
				//this cant be parsed, this should be revisited
				System.out.println("This can't be parsed " + (i+1) + " " + payloadContents.length);
			} else {
				long payloadIndex = parseByteArrayAtIndex(payloadContents, i+1, CHANNEL_DATA_TYPE.UINT16);
				MemoryBankDetails memoryBankDetails = new MemoryBankDetails(statusByte, payloadIndex);
				listOfMemoryBankDetails.add(memoryBankDetails);
			}
		}

			
		isSuccess = true;
		return isSuccess;
	}
	
	@Override
	public byte[] generatePayloadContents() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String generateDebugString() {
		StringBuilder sb = new StringBuilder();

		sb.append("Long-term flash:\n");
		
		int rowIndex = 0;
		for (int bankIndex = 0; bankIndex < listOfMemoryBankDetails.size(); bankIndex+=8) {
			if (bankIndex == 16384) {
				sb.append("\nShort-term flash 1:\n");
			} else if (bankIndex == 16384+256) {
				sb.append("\nShort-term flash 2:\n");
			}

			MemoryBankDetails memoryBankDetails = listOfMemoryBankDetails.get(bankIndex);
			sb.append(rowIndex + "->");
			for (int bankIndexPerRow = 0; bankIndexPerRow < 8; bankIndexPerRow++) {
				sb.append("[");
				sb.append(bankIndex);
				sb.append(", ");
				
				BANK_STATUS bankStatus = BANK_STATUS.getBankStatus(memoryBankDetails.getBankStatus());
				sb.append(bankStatus==null? memoryBankDetails.getBankStatus():bankStatus.description);
				
				sb.append(", ");
				sb.append(memoryBankDetails.payloadIndex);
				sb.append("]");
			}
			sb.append("\n");
			rowIndex++;
		}

		sb.append("\n");
		sb.append(generateSummaryTableString());
		
		return sb.toString();
	}
	
	public String generateSummaryTableString() {
		HashMap<Byte, Integer> summaryTable = new HashMap<Byte, Integer>();

		int qtyWaitingToBeWrittenToEeprom = 0;
		for (MemoryBankDetails memoryBankDetails : listOfMemoryBankDetails) {
			Integer count = summaryTable.getOrDefault(memoryBankDetails.getBankStatus(), 0);
			count++;
			summaryTable.put(memoryBankDetails.getBankStatus(), count);

			if(memoryBankDetails.isWaitingToBeWrittenToFlash()) {
				qtyWaitingToBeWrittenToEeprom++;
			}
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append("Summary table:\n");
		for(Entry<Byte, Integer> summary:summaryTable.entrySet()) {
			BANK_STATUS bankStatus = BANK_STATUS.getBankStatus(summary.getKey());
			sb.append(bankStatus==null? summary.getKey():bankStatus.description);
			sb.append(",\tCount=" + summary.getValue() + "\n");
		}
		
		sb.append("Qty banks waiting to be written from RAM to EEPROM (i.e., '*') = " + qtyWaitingToBeWrittenToEeprom + "\n");
		
		if(headTailReceived) {
			sb.append("Current Tail Location = Bank Idx " + tail + "\n");
			sb.append("Current Head Location = Bank Idx " + head + "\n");
		}

		return sb.toString();
	}
	
	public class MemoryBankDetails {

		private byte statusByte;
		private long payloadIndex;

		public MemoryBankDetails(byte statusByte, long payloadIndex) {
			this.statusByte = statusByte;
			this.payloadIndex = payloadIndex;
		}
		
		public boolean isWaitingToBeWrittenToFlash() {
			return (statusByte & 0x80) == 0x80;
		}

		public byte getBankStatus() {
			byte statusMasked = (byte) (statusByte & 0x7F);
			return statusMasked;
		}
		

	}

}
