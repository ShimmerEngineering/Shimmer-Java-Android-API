package com.shimmerresearch.verisense.payloaddesign;

import java.io.IOException;
import java.io.InputStream;

import com.shimmerresearch.driver.Configuration.CHANNEL_UNITS;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;
import com.shimmerresearch.driverUtilities.UtilShimmer;
import com.shimmerresearch.sensors.AbstractSensor.SENSORS;
import com.shimmerresearch.verisense.UtilVerisenseDriver;
import com.shimmerresearch.verisense.VerisenseDevice;
import com.shimmerresearch.verisense.payloaddesign.AsmBinaryFileConstants.BYTE_COUNT;

public class PayloadDetails {

	private CRC16CCITT CRC16CCITT = new CRC16CCITT();

	public int payloadIndex;
	public int payloadWhiteSpace;
	/** Includes payload index bytes, payload length bytes and payload config bytes **/
	public int payloadHeaderLength;
	public int crcOriginal;
	public int payloadLengthBytesValue;

	public byte[] payloadConfig = null;
	public byte[] payloadWithoutCrc = null;
	/**
	 * Structure is [sensor data, end time bytes (minutes and ticks), battery
	 * voltage and temperature].
	 * <p>
	 * Support is being maintained in the file parser for this section of the
	 * payload ("termed the RAM block") to be compressed by the FW such that the
	 * file parser will need to decompress it before parsing each of it's fields.
	 */
	public byte[] ramBlockDataBytes;

	public PayloadContentsDetails payloadContentsDetails = null;

	public PayloadDetails() {
		// TODO Auto-generated constructor stub
	}
	
	public boolean parsePayload(InputStream inputStream, boolean isZipFile, int byteIndex) throws IOException {
		//Payload Header
		byte[] payloadHeader = new byte[BYTE_COUNT.PAYLOAD_INDEX + BYTE_COUNT.PAYLOAD_LENGTH + BYTE_COUNT.PAYLOAD_CONFIG_CORE];
		try {
			readBytesFromStream(inputStream, payloadHeader);
		} catch (IOException e) {
			// If not a zip or this is the first byte to be read, throw error.
			if(!isZipFile || byteIndex==0){
				throw(e);
			} else {
				// in the case of a zip file (especially while streaming from S3 and not a local
				// PC file), this is used to indicate there are no bytes left in the inputstream
				System.out.println("Reached end of ZIP file");
				return false;
			}
		}

		byte[] payloadIndexBytes = new byte[BYTE_COUNT.PAYLOAD_INDEX];
		System.arraycopy(payloadHeader, 0, payloadIndexBytes, 0, payloadIndexBytes.length);
		payloadIndex = UtilVerisenseDriver.lsbByteArrayToInt(payloadIndexBytes);
		
		byte[] payloadLengthBytes = new byte[BYTE_COUNT.PAYLOAD_LENGTH];
		System.arraycopy(payloadHeader, 2, payloadLengthBytes, 0, payloadLengthBytes.length);
		payloadLengthBytesValue = UtilVerisenseDriver.lsbByteArrayToInt(payloadLengthBytes);
		
		// ASM-1248: A Bad Crc binary file might have 0xFF bytes for both payload index and payload size
		// Below conditional checks this condition and throws an exception
		if (payloadIndex == payloadLengthBytesValue && payloadLengthBytesValue == 0xFFFF) {
			throw new IOException("Bad Crc binary file: Bytes for payload index and size are " + UtilShimmer.bytesToHexStringWithSpacesFormatted(payloadLengthBytes));
		}
		
		byte[] payloadConfigCore = new byte[BYTE_COUNT.PAYLOAD_CONFIG_CORE];
		System.arraycopy(payloadHeader, 4, payloadConfigCore, 0, payloadConfigCore.length);
		
		// Originally there were 2 config bytes (as parsed above). From FW v0.35.000
		// onwards, the FW version is included in the configuration and subsequently
		// support can be added to increase the number of config bytes in the future
		// depending on the FW version.
		ShimmerVerObject svo = null;
		byte[] payloadConfigExtended = new byte[0];
		if(VerisenseDevice.isExtendedPayloadConfig(payloadConfigCore[0])) {
			byte[] payloadConfigFwVer = new byte[4];
			readBytesFromStream(inputStream, payloadConfigFwVer);
			
			svo = VerisenseDevice.parseFirmwareVersionToShimmerVerObject(payloadConfigFwVer);
			byte[] payloadConfigRemaining = new byte[PayloadContentsDetails.calculateExtendedPayloadConfigBytesSize(svo)];
			readBytesFromStream(inputStream, payloadConfigRemaining);
			
			payloadConfigExtended = new byte[payloadConfigFwVer.length + payloadConfigRemaining.length];
			System.arraycopy(payloadConfigFwVer, 0, payloadConfigExtended, 0, payloadConfigFwVer.length);
			System.arraycopy(payloadConfigRemaining, 0, payloadConfigExtended, payloadConfigFwVer.length, payloadConfigRemaining.length);
		}
		
		payloadConfig = new byte[payloadConfigCore.length + payloadConfigExtended.length];
		System.arraycopy(payloadConfigCore, 0, payloadConfig, 0, payloadConfigCore.length);
		System.arraycopy(payloadConfigExtended, 0, payloadConfig, payloadConfigCore.length, payloadConfigExtended.length);
		
		payloadHeaderLength = payloadHeader.length+payloadConfigExtended.length;

		//Payload contents
		int ramBlockDataLength = calculateRamBlockDataLength(payloadLengthBytesValue, svo);
		ramBlockDataBytes = new byte[ramBlockDataLength];
		readBytesFromStream(inputStream, ramBlockDataBytes);

		//Payload footer
		byte[] crcBytes = new byte[BYTE_COUNT.PAYLOAD_CRC];
		readBytesFromStream(inputStream, crcBytes);
		crcOriginal = CRC16CCITT.crcBytesToInt(crcBytes);
		//consolePrintLn("CRC=" + UtilShimmer.bytesToHexStringWithSpacesFormatted(crcBytes));

		//Rebuild payload in order to check CRC 
		payloadWithoutCrc = new byte[payloadHeaderLength+ramBlockDataBytes.length];
		System.arraycopy(payloadHeader, 0, payloadWithoutCrc, 0, payloadHeader.length);
		System.arraycopy(payloadConfigExtended, 0, payloadWithoutCrc, payloadHeader.length, payloadConfigExtended.length);
		System.arraycopy(ramBlockDataBytes, 0, payloadWithoutCrc, payloadHeader.length+payloadConfigExtended.length, ramBlockDataBytes.length);

		if(!AsmBinaryFileConstants.OPTIMISE_32KB_PAYLOAD_SIZE) {
			payloadWhiteSpace = BYTE_COUNT.PAYLOAD_CONTENTS_RESERVED_SIZE-payloadWithoutCrc.length-BYTE_COUNT.PAYLOAD_FOOTER;
			//Clear off the empty bytes from the buffer 
			readBytesFromStream(inputStream, new byte[payloadWhiteSpace]);
		}
		
		return true;
	}

	private int calculateRamBlockDataLength(int payloadLengthBytesValue, ShimmerVerObject svo) {
		int ramBlockDataLength = payloadLengthBytesValue;
		if(svo!=null && PayloadContentsDetails.isPayloadDesignV8orAbove(svo)) {
			ramBlockDataLength -= payloadConfig.length;
		}
		return ramBlockDataLength;
	}

	/**
	 * @param inputStream
	 * @param count
	 * @return
	 * @throws IOException
	 */
	private int readBytesFromStream(InputStream inputStream, int count) throws IOException {
		byte[] lengthBuffer = new byte[count];
		readBytesFromStream(inputStream, lengthBuffer);
//		consolePrintDebugLn("Length Bytes" + UtilShimmer.bytesToHexStringWithSpacesFormatted(lengthBuffer));
		return UtilVerisenseDriver.lsbByteArrayToInt(lengthBuffer);
	}
	
	private void readBytesFromStream(InputStream inputStream, byte[] buffer) throws IOException {
//		inputStream.read(buffer);
		int lenRemaining = buffer.length;
		int lenRead = 0;
		int offset = 0;
		while(lenRemaining>0) {
			if(inputStream.available()==0) {
				System.err.println("readBytesFromStream: Tried To Read=" + buffer.length + "\tLength Read=" + lenRead + "\tBytes=" + UtilShimmer.bytesToHexStringWithSpacesFormatted(buffer));
				throw new IOException("No bytes available in input stream");
			}
			lenRead = inputStream.read(buffer, offset, lenRemaining);
			if(lenRead>0) {
				offset+=lenRead;
				lenRemaining-=lenRead;
			}
		}
	}

	public boolean checkCrc() {
		return CRC16CCITT.checkCrc(payloadWithoutCrc, crcOriginal);
	}

	public void printCrcDetails() {
    	int crcComputed = CRC16CCITT.computeCrc(payloadWithoutCrc);
		System.err.println("\tCRC fail on Payload " + payloadIndex 
				+ "\tcrcParsed=" + UtilShimmer.intToHexStringFormatted(crcOriginal, 2, false)
				+ "\tcrcComputed=" + UtilShimmer.intToHexStringFormatted(crcComputed, 2, false) 
				);
	}
	
	/**
	 * @param ramBlockDataBytes
	 * @param payloadIndex
	 * @param ramBlockLength
	 * @param verisenseDevice
	 * @param binFileByteIndex 
	 * @return
	 * @throws IOException 
	 */
	public PayloadContentsDetails parsePayloadContentsHeaderFooter(byte[] ramBlockDataBytes, 
			int payloadIndex, 
			VerisenseDevice verisenseDevice, int binFileByteIndex) throws IOException {
	
		PayloadContentsDetails payloadContentsDetails = createPayloadContentsDetailsForVersionObject(verisenseDevice);
		boolean success = payloadContentsDetails.parsePayloadContentsHeaderAndFooter(ramBlockDataBytes, payloadIndex, binFileByteIndex);
		if(success) {
			this.payloadContentsDetails = payloadContentsDetails;
		} else {
			this.payloadContentsDetails = null;
		}
		return this.payloadContentsDetails;
	}
	
	private PayloadContentsDetails createPayloadContentsDetailsForVersionObject(VerisenseDevice verisenseDevice) {
		if(PayloadContentsDetails.isPayloadDesignV8orAbove(verisenseDevice.getShimmerVerObject())) {
			return new PayloadContentsDetailsV8orAbove(verisenseDevice);
		} else {
			return new PayloadContentsDetailsV1_7(verisenseDevice);
		}
	}

	public int calculateTotalPayloadLength() {
		int totalPayloadLength = payloadHeaderLength+payloadLengthBytesValue+BYTE_COUNT.PAYLOAD_FOOTER-1;
		
		if(payloadContentsDetails!=null
				&& payloadContentsDetails.verisenseDevice!=null
				&& payloadContentsDetails.verisenseDevice.isPayloadDesignV8orAbove()) {
			totalPayloadLength -= payloadConfig.length;
//			totalPayloadLength -= BYTE_COUNT.PAYLOAD_CONTENTS_FOOTER_GEN8;
//		} else {
//			totalPayloadLength = payloadHeaderLength+payloadLengthBytesValue+BYTE_COUNT.PAYLOAD_FOOTER-1;
		}
		
		return totalPayloadLength;
	}

	public void consolePrintPayloadHeaderInfo(VerisenseDevice verisenseDevice, int binFileByteIndex) {
		int payloadLengthByteValue = payloadLengthBytesValue;
		
		int endByteIndex = binFileByteIndex + calculateTotalPayloadLength();
		
		StringBuilder sb = new StringBuilder();
		sb.append("Payload Info:\n" 
				+ " |_ ByteIndexes [Start = " + UtilShimmer.intToHexStringFormatted(binFileByteIndex, 4, true)
				+ ", End = " + UtilShimmer.intToHexStringFormatted(endByteIndex, 4, true) + "]"
				+ ", Payload Length Bytes = " + payloadLengthByteValue + " bytes (" + UtilShimmer.intToHexStringFormatted(payloadLengthByteValue, 2, false) + ")\n");
		
		for(SENSORS sensorClassKey:payloadContentsDetails.getMapOfDataSegmentsPerSensor().keySet()) {
			sb.append(" |_ " + verisenseDevice.generateSensorConfigStrSingleSensor(sensorClassKey, Double.NaN));
			sb.append("\n");
		}

		sb.append(" |_ FW Ver = " + verisenseDevice.getFirmwareVersionParsed());
		sb.append(", [" + verisenseDevice.generateResetReasonStr() + "]");

		if(payloadContentsDetails==null) {
			sb.append("\n");
		} else {
			sb.append( 
//			 "\tTemperature -> Uncal:" + getTemperatureUncal()  + "(" + UtilShimmer.intToHexStringFormatted((int) getTemperatureUncal(), 2, true) + ")" + " Cal:" + getTemperatureCal() 
			 ", Temperature=" + payloadContentsDetails.getTemperatureCal() + " " + CHANNEL_UNITS.DEGREES_CELSIUS_SHORT
//			 + "\tBattery Voltage -> " + getBatteryVoltageCal() + "(" + UtilShimmer.intToHexStringFormatted((int) getBatteryVoltageCal(), 2, true) + ")" 
			 + ", Battery Voltage=" + payloadContentsDetails.getBatteryVoltageCal() + " mV\n" 
			 + " |_ Payload Timing [Start=" + payloadContentsDetails.getStartTimeRwcStr() 
			 + ", End=" + payloadContentsDetails.getEndTimeRwcStr()
			 + ", Duration=" + payloadContentsDetails.getPayloadDurationStr()
			 + ", Packaging Delay=" + payloadContentsDetails.getPayloadPackagingDelayStr() + "]");

//			if(payloadContentsDetails.DEBUG_ACCEL_ARRAYS) {
				//TODO Get back working
//				sb.append("\tAccel Default Cal Average\tX=" + Arrays.stream(payloadContents.accelDataArrayDefaultCal[0]).average().getAsDouble()
//				+ "\tY=" + Arrays.stream(payloadContents.accelDataArrayDefaultCal[1]).average().getAsDouble()
//				+ "\tZ=" + Arrays.stream(payloadContents.accelDataArrayDefaultCal[2]).average().getAsDouble());
//			}
		}

		System.out.println(sb.toString());
		
		if(payloadContentsDetails!=null) {
			payloadContentsDetails.datasetToSave.printReportOfDataSegments();
		}

//		System.out.println("");
//		System.out.println("DataBlock Details");
//		if(payloadDetails.payloadContentsDetails instanceof PayloadContentsDetailsV8orAbove) {
//			((PayloadContentsDetailsV8orAbove)payloadDetails.payloadContentsDetails).printListOfDataBlockDetails();
//		}
//		System.out.println("");
	}

}
