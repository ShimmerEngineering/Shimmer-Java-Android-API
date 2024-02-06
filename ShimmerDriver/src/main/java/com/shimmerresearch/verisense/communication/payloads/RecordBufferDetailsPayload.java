package com.shimmerresearch.verisense.communication.payloads;

import java.util.ArrayList;
import java.util.List;

import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_TYPE;

/**
 * @author Mark Nolan
 *
 */
public class RecordBufferDetailsPayload extends AbstractPayload {

	private static int NUM_MEMORY_BANKS = 2;
	
	private List<RecordBufferDetails> listOfRecordBufferDetails = new ArrayList<RecordBufferDetails>();
	
	@Override
	public boolean parsePayloadContents(byte[] payloadContents) {
		this.payloadContents = payloadContents;
		isSuccess = false;
		
		listOfRecordBufferDetails.clear();
		
		int numBytesPerRecordBuffer = payloadContents.length/NUM_MEMORY_BANKS;
		
		for(int i=0;i<payloadContents.length;i+=numBytesPerRecordBuffer) {
			RecordBufferDetails memoryBankDetails = new RecordBufferDetails(numBytesPerRecordBuffer);
			memoryBankDetails.bufferIndex = payloadContents[i];
			memoryBankDetails.bufferState = payloadContents[i+1];
			memoryBankDetails.packagedPayloadIndex = parseByteArrayAtIndex(payloadContents, i+2, CHANNEL_DATA_TYPE.UINT16);
			memoryBankDetails.currentByteIndexForSensorData = parseByteArrayAtIndex(payloadContents, i+4, CHANNEL_DATA_TYPE.UINT16);
			memoryBankDetails.usedBufferLength = parseByteArrayAtIndex(payloadContents, i+6, CHANNEL_DATA_TYPE.UINT16);
			memoryBankDetails.fifoTicks = parseByteArrayAtIndex(payloadContents, i+8, CHANNEL_DATA_TYPE.UINT16);
			memoryBankDetails.dataTsRwcMinutes = parseByteArrayAtIndex(payloadContents, i+10, CHANNEL_DATA_TYPE.UINT32);
			memoryBankDetails.dataTsRwcTicks = parseByteArrayAtIndex(payloadContents, i+14, CHANNEL_DATA_TYPE.UINT24);
			memoryBankDetails.temperatureData = parseByteArrayAtIndex(payloadContents, i+17, CHANNEL_DATA_TYPE.UINT16);
			if (numBytesPerRecordBuffer >= 25) {
				memoryBankDetails.dataTsUcClockMinutes = parseByteArrayAtIndex(payloadContents, i+19, CHANNEL_DATA_TYPE.UINT32);
				memoryBankDetails.dataTsUcClockTicks = parseByteArrayAtIndex(payloadContents, i+23, CHANNEL_DATA_TYPE.UINT24);
			}
			listOfRecordBufferDetails.add(memoryBankDetails);
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

		for(RecordBufferDetails recordBufferDetails : listOfRecordBufferDetails) {
			sb.append(recordBufferDetails.generateDebugString());
		}

		return sb.toString();
	}

	public class RecordBufferDetails {

		public byte bufferIndex;
		public byte bufferState;
		public long packagedPayloadIndex;
		public long currentByteIndexForSensorData;
		public long usedBufferLength;
		public long fifoTicks;
		public long dataTsRwcMinutes;
		public long dataTsRwcTicks;
		public long temperatureData;
		
		public long dataTsUcClockMinutes;
		public long dataTsUcClockTicks;
		
		private int numBytesPerRecordBuffer;
		
		public RecordBufferDetails(int numBytesPerRecordBuffer) {
			this.numBytesPerRecordBuffer = numBytesPerRecordBuffer;
		}

		public String generateDebugString() {
			StringBuilder sb = new StringBuilder();

			sb.append("Index = " + bufferIndex + ", State = " + bufferState + "\n");
			sb.append("\tpackagedPayloadIndex = " + packagedPayloadIndex + "\n");
			sb.append("\tcurrentByteIndexForSensorData = " + currentByteIndexForSensorData + "\n");
			sb.append("\tusedBufferLength = " + usedBufferLength + "\n");
			sb.append("\tfifoTicks = " + fifoTicks + "\n");
			sb.append("\tdataTsRwcMinutes = " + dataTsRwcMinutes + "\n");
			sb.append("\tdataTsRwcTicks = " + dataTsRwcTicks + "\n");
			sb.append("\ttemperatureData = " + temperatureData + "\n");
			if (numBytesPerRecordBuffer >= 25) {
				sb.append("\tdataTsUcClockMinutes = " + dataTsUcClockMinutes + "\n");
				sb.append("\tdataTsUcClockTicks = " + dataTsUcClockTicks + "\n");
			}
	
			return sb.toString();
		}
		
		
		
	}

	public List<RecordBufferDetails> getListOfRecordBufferDetails() {
		return listOfRecordBufferDetails;
	}
}
