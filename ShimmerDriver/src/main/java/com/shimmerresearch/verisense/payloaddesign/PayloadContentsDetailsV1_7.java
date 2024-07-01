package com.shimmerresearch.verisense.payloaddesign;

import java.io.IOException;
import java.util.List;

import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;
import com.shimmerresearch.sensors.AbstractSensor;
import com.shimmerresearch.sensors.AbstractSensor.SENSORS;
import com.shimmerresearch.verisense.VerisenseDevice;
import com.shimmerresearch.verisense.payloaddesign.AsmBinaryFileConstants.BYTE_COUNT;
import com.shimmerresearch.verisense.payloaddesign.DataBlockDetails.DATABLOCK_SENSOR_ID;
import com.shimmerresearch.verisense.sensors.SensorLIS2DW12;
import com.shimmerresearch.verisense.sensors.SensorLSM6DS3;
import com.shimmerresearch.verisense.sensors.SensorMAX86XXX;

public class PayloadContentsDetailsV1_7 extends PayloadContentsDetails {
	
	private static final long serialVersionUID = -8809853619338894452L;

	private static double[] expectedPayloadTsDiffLimits = null; 

	private int fifoBlockCount = 0;
	private boolean isSpiChWithHeaderByte = false;
	private int sampleSetsInFifo = 0;
	private int byteCountPerSet = 0;

	public PayloadContentsDetailsV1_7(VerisenseDevice verisenseDevice) {
		super(verisenseDevice);
	}

	@Override
	public void parsePayloadContentsMetaData(int binFileByteIndex) throws IOException {
		//consolePrintDebugLn(UtilShimmer.bytesToHexStringWithSpacesFormatted(byteBuffer));
		int byteCountPerSet = verisenseDevice.getExpectedDataPacketSize(COMMUNICATION_TYPE.SD);
		
		int fifoBlockSize = calculateFifoBlockSize();
		int sampleSetsInFifo = 0;
		if(fifoBlockSize>0) {
			sampleSetsInFifo = calculateNumberOfSampleSetsInFifo(fifoBlockSize, byteCountPerSet);
		}
		
		boolean isSpiChWithHeaderByte = verisenseDevice.isSpiChannelEnabled() && AsmBinaryFileConstants.IS_SPI_BUS_ADDING_HEADER_BYTE;
		if(isSpiChWithHeaderByte) {
			fifoBlockSize += AsmBinaryFileConstants.ACCEL_SPI_BUS_HEADER_BYTES;
		}
		
		int fifoBlockCount = (int) Math.floor(byteBuffer.length/fifoBlockSize);
		int sampleCount = fifoBlockCount*sampleSetsInFifo;
	
		int currentByteIndex = (fifoBlockCount*fifoBlockSize);
		
		long rtcEndTimeMinutes = VerisenseTimeDetails.parseTimeMinutesAtIndex(byteBuffer, currentByteIndex);
		currentByteIndex += BYTE_COUNT.PAYLOAD_CONTENTS_RTC_BYTES_MINUTES;

		long rtcEndTimeTicks = VerisenseTimeDetails.parseTimeTicksAtIndex(byteBuffer, currentByteIndex);
		currentByteIndex += BYTE_COUNT.PAYLOAD_CONTENTS_RTC_BYTES_TICKS;
		
		getTimeDetailsRwc().setEndTimeAndCalculateMs(rtcEndTimeMinutes, rtcEndTimeTicks);

		setFifoBlockCount(fifoBlockCount);
		setIsSpiChWithHeaderByte(isSpiChWithHeaderByte);
		setSampleSetsInFifo(sampleSetsInFifo);
		setByteCountPerSet(byteCountPerSet);
		
		DATABLOCK_SENSOR_ID datablockSensorId = figureOutDatablockSensorId();
		
		// Saving in global map as we only need to do this once per datablock sensor ID per payload, not for each datablock
		List<SENSORS> listOfSensorClassKeys = verisenseDevice.getOrCreateListOfSensorClassKeysForDataBlockId(datablockSensorId);
		
		// Adding a single data block per payload here so that the legacy code will fit into the newer code flow (i.e., PayloadDesign v8 onwards)
		DataBlockDetails dataBlockDetails = new DataBlockDetails(datablockSensorId, getPayloadIndex(), 0, listOfSensorClassKeys, binFileByteIndex, 0, rtcEndTimeTicks, false);
		dataBlockDetails.setSamplingRate(verisenseDevice.getSamplingRateShimmer());
		dataBlockDetails.setSampleCount(sampleCount);
		dataBlockDetails.getTimeDetailsRwc().setEndTimeTicks(rtcEndTimeTicks);
		dataBlockDetails.setRwcEndTimeMinutesAndCalculateTimings(rtcEndTimeMinutes);
		
		listOfDataBlocksInOrder.add(dataBlockDetails);
		setOfPayloadSensorIds.add(datablockSensorId);

		calculatePayloadStartTimeMsRwc();
		
		currentByteIndex = parseTemperatureBytes(currentByteIndex);
		
		//Battery Voltage
		// Unfortunately it's not possible to check the FW version in order to determine
		// whether the battery voltage is supported as it was added to the payload
		// design in FW v0.31.000 before the FW version bytes were added in FW
		// v0.034.001. Therefore the only way to check is to see if there are unparsed bytes left as below.
		if(currentByteIndex<byteBuffer.length) {
			currentByteIndex = parseBatteryVoltageBytes(currentByteIndex);
		}

	}

	private DATABLOCK_SENSOR_ID figureOutDatablockSensorId() {
		if(verisenseDevice.isSensorEnabled(Configuration.Verisense.SENSOR_ID.LIS2DW12_ACCEL)) {
			return DATABLOCK_SENSOR_ID.ACCEL_1;
		} else if(verisenseDevice.isEitherLsm6ds3ChannelEnabled()) {
			return DATABLOCK_SENSOR_ID.GYRO_ACCEL2;
		} else if(verisenseDevice.isHwPpgAndAnyMaxChEnabled()) {
			return DATABLOCK_SENSOR_ID.PPG;
		}
		return null;
	}

	@Override
	public void parsePayloadSensorData() {
		//Sensor Data:
		double timeMsCurrentSample = getStartTimeRwcMs();
		//System.out.println("Payload End Time: " + payloadContents.getEndTimeParsed());
		int currentByteIndex = 0;
		int sampleIndex = 0;
		for(int x=0;x<getFifoBlockCount();x++) {
			//Skip first byte -> 0x00 from SPI read issue
			if(getIsSpiChWithHeaderByte()) {
				currentByteIndex++;
			}
			
			for(int y=0;y<getSampleSetsInFifo();y++) {
				byte[] byteBuf = new byte[getByteCountPerSet()];
				System.arraycopy(byteBuffer, currentByteIndex, byteBuf, 0, byteBuf.length);
				ObjectCluster ojcCurrent = verisenseDevice.buildMsg(byteBuf, COMMUNICATION_TYPE.SD, false, timeMsCurrentSample);
				
				setOjcArray(sampleIndex, ojcCurrent);
	
				timeMsCurrentSample += getTimestampDiffInMs();
	
				currentByteIndex += getByteCountPerSet();
				sampleIndex++;
			}
		}
	}
	
	public static int calculatePayloadSensorDataMemAvailable(ShimmerVerObject svo) {
		int memAvailable = BYTE_COUNT.PAYLOAD_CONTENTS_RESERVED_SIZE - calculatePayloadHeaderBytesSize(svo) - BYTE_COUNT.PAYLOAD_CONTENTS_FOOTER_GEN1_TO_GEN7_AND_GEN9;
		return memAvailable;
	}
	
	public double getTimestampDiffInMs() {
		return 1000.0/verisenseDevice.getSamplingRateShimmer();
	}
	
	public double calculateTimestampDiffInS() {
		return 1.0/verisenseDevice.getSamplingRateShimmer();
	}
	
	public double calculateMaxPayloadDataSizeInMs() {
		return calculateMaxPayloadDataSizeInSec() * 1000;
	}
	
	public double calculateMaxPayloadDataSizeInSec() {
		double payloadDataSizeInSec = 0.0;
				
		int fifoBlockSize = calculateFifoBlockSize();
		int fifoBlocksInPayload = calculateFifoBlocksInPayload();
		int sampleSetsInFifo = 0;
		if(fifoBlockSize>0) {
			sampleSetsInFifo = calculateNumberOfSampleSetsInFifo(fifoBlockSize);
		}
		
		payloadDataSizeInSec = (sampleSetsInFifo * calculateTimestampDiffInS() * fifoBlocksInPayload);
		
		return payloadDataSizeInSec;
	}
	
	//TODO move to sensor classes?
	public int calculateFifoBlockSize() {
		int fifoBlockSize = 0;
		if(verisenseDevice.isSensorEnabled(Configuration.Verisense.SENSOR_ID.LIS2DW12_ACCEL)) {
			fifoBlockSize = verisenseDevice.calculateFifoBlockSize(SENSORS.LIS2DW12);
		} else if(verisenseDevice.isEitherLsm6ds3ChannelEnabled()) {
			fifoBlockSize = verisenseDevice.calculateFifoBlockSize(SENSORS.LSM6DS3);
		} else if(verisenseDevice.isHwPpgAndAnyMaxChEnabled()) {
			fifoBlockSize = verisenseDevice.calculateFifoBlockSize(SENSORS.MAX86916);
		}
		return fifoBlockSize;
	}
	
	private int calculateFifoBlocksInPayload() {
		int maxFifosInPayload = 0;
		if(verisenseDevice.isSensorEnabled(Configuration.Verisense.SENSOR_ID.LIS2DW12_ACCEL)) {
			maxFifosInPayload = SensorLIS2DW12.MAX_FIFOS_IN_PAYLOAD;
		} else if(verisenseDevice.isEitherLsm6ds3ChannelEnabled()) {
			AbstractSensor abstractSensor = verisenseDevice.getSensorClass(SENSORS.LSM6DS3);
			if(abstractSensor!=null) {
				SensorLSM6DS3 sensorLSM6DS3 = (SensorLSM6DS3)abstractSensor;
				int memAvailable = calculatePayloadSensorDataMemAvailable(verisenseDevice.getShimmerVerObject());
				maxFifosInPayload = sensorLSM6DS3.calculateMaxPayloadsInFifo(memAvailable);
			}
		} else if(verisenseDevice.isHwPpgAndAnyMaxChEnabled()) {
			int chEnabled = 0;
			if(verisenseDevice.isSensorEnabled(Configuration.Verisense.SENSOR_ID.MAX86XXX_PPG_RED)) {
				chEnabled++;
			}
			if(verisenseDevice.isSensorEnabled(Configuration.Verisense.SENSOR_ID.MAX86XXX_PPG_IR)) {
				chEnabled++;
			}
			if(verisenseDevice.isSensorEnabled(Configuration.Verisense.SENSOR_ID.MAX86150_ECG)) {
				chEnabled++;
			}
			if(verisenseDevice.isSensorEnabled(Configuration.Verisense.SENSOR_ID.MAX86916_PPG_GREEN)) {
				chEnabled++;
			}
			if(verisenseDevice.isSensorEnabled(Configuration.Verisense.SENSOR_ID.MAX86916_PPG_BLUE)) {
				chEnabled++;
			}
			
			// MAX86150 can have a max of 3 channels whereas MAX86916 can have a max of 4 channels
			if(chEnabled==1) {
				maxFifosInPayload = SensorMAX86XXX.MAX_FIFOS_IN_PAYLOAD_1_CHANNEL;
			} else if(chEnabled==2) {
				maxFifosInPayload = SensorMAX86XXX.MAX_FIFOS_IN_PAYLOAD_2_CHANNELS;
			} else if(chEnabled==3) {
				maxFifosInPayload = SensorMAX86XXX.MAX_FIFOS_IN_PAYLOAD_3_CHANNELS;
			} else if(chEnabled==4) {
				maxFifosInPayload = SensorMAX86XXX.MAX_FIFOS_IN_PAYLOAD_4_CHANNELS;
			}
		}
		return maxFifosInPayload;
	}
	
	public int calculateNumberOfSampleSetsInPayload(int fifoSizeInChip, int fifosInPayload) {
		return calculateNumberOfSampleSetsInFifo(fifoSizeInChip)*fifosInPayload;
	}
	
	public int calculateNumberOfSampleSetsInFifo(int fifoSizeInChip) {
		int defaultDataPacketSize = verisenseDevice.getExpectedDataPacketSize(COMMUNICATION_TYPE.SD);
		return calculateNumberOfSampleSetsInFifo(fifoSizeInChip, defaultDataPacketSize);
	}
	
	public int calculateNumberOfSampleSetsInFifo(int fifoSizeInChip, int defaultDataPacketSize) {
		return fifoSizeInChip/defaultDataPacketSize;
	}

	public void setFifoBlockCount(int fifoBlockCount) {
		this.fifoBlockCount = fifoBlockCount;
	}

	public int getFifoBlockCount() {
		return fifoBlockCount;
	}

	public void setIsSpiChWithHeaderByte(boolean isSpiChWithHeaderByte) {
		this.isSpiChWithHeaderByte = isSpiChWithHeaderByte;
	}

	public boolean getIsSpiChWithHeaderByte() {
		return isSpiChWithHeaderByte;
	}

	public void setSampleSetsInFifo(int sampleSetsInFifo) {
		this.sampleSetsInFifo = sampleSetsInFifo;
	}

	public int getSampleSetsInFifo() {
		return sampleSetsInFifo;
	}

	public void setByteCountPerSet(int byteCountPerSet) {
		this.byteCountPerSet = byteCountPerSet;
	}

	public int getByteCountPerSet() {
		return byteCountPerSet;
	}

	public void setOjcArray(int sampleIndex, ObjectCluster ojc) {
		listOfDataBlocksInOrder.get(0).setOjcArrayAtIndex(sampleIndex, ojc);
	}

	public ObjectCluster[] getOjcArray() {
		return listOfDataBlocksInOrder.get(0).getOjcArray();
	}

	public double[] calculateExpectedPayloadTsDiffLimits() {
		double[] tsDiffLimits = new double[2];
		
		Double expectedTsDiff = calculateMaxPayloadDataSizeInMs();
		
		tsDiffLimits[0] = expectedTsDiff*0.9; // 90%
		tsDiffLimits[1] = expectedTsDiff*1.1; // 110%
		return tsDiffLimits;
	}

	public static void updateExpectedPayloadTsDiffLimits(PayloadContentsDetailsV1_7 payloadContents) {
		expectedPayloadTsDiffLimits = payloadContents.calculateExpectedPayloadTsDiffLimits();
	}

	public boolean isTsDifferenceUnexpected(double startTsMsPreviousPayload, double startTsMsCurrentPayload) {
		if (UtilCsvSplitting.isTsDifferenceOutsideOfLimits(expectedPayloadTsDiffLimits, startTsMsCurrentPayload, startTsMsPreviousPayload)){
			super.consolePrintTsDifferenceUnexpected(
					"\n\tActual Diff (start->start)=" + (Math.abs(startTsMsCurrentPayload - startTsMsPreviousPayload)) + "ms"
					+ "\tThresholdAllowed Min=" + expectedPayloadTsDiffLimits[0] + "ms" + " Max=" + expectedPayloadTsDiffLimits[1] + "ms");
			return true;
		}
		return false;
	}

}
