package com.shimmerresearch.verisense.communication;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.junit.Test;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driverUtilities.ExpansionBoardDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.HW_ID;
import com.shimmerresearch.driverUtilities.UtilShimmer;
import com.shimmerresearch.sensors.AbstractSensor;
import com.shimmerresearch.sensors.AbstractSensor.SENSORS;
import com.shimmerresearch.verisense.PendingEventSchedule;
import com.shimmerresearch.verisense.VerisenseDevice;
import com.shimmerresearch.verisense.VerisenseDevice.BATTERY_TYPE;
import com.shimmerresearch.verisense.VerisenseDevice.BLE_TX_POWER;
import com.shimmerresearch.verisense.VerisenseDevice.PASSKEY_MODE;
import com.shimmerresearch.verisense.communication.payloads.EventLogPayload;
import com.shimmerresearch.verisense.communication.payloads.MemoryLookupTablePayload;
import com.shimmerresearch.verisense.communication.payloads.PendingEventsPayload;
import com.shimmerresearch.verisense.communication.payloads.ProductionConfigPayload;
import com.shimmerresearch.verisense.communication.payloads.RecordBufferDetailsPayload;
import com.shimmerresearch.verisense.communication.payloads.RecordBufferDetailsPayload.RecordBufferDetails;
import com.shimmerresearch.verisense.communication.payloads.RwcSchedulePayload;
import com.shimmerresearch.verisense.communication.payloads.StatusPayload;
import com.shimmerresearch.verisense.communication.payloads.TimePayload;
import com.shimmerresearch.verisense.payloaddesign.AsmBinaryFileConstants.DATA_COMPRESSION_MODE;
import com.shimmerresearch.verisense.sensors.SensorBattVoltageVerisense;
import com.shimmerresearch.verisense.sensors.SensorLIS2DW12;
import com.shimmerresearch.verisense.sensors.SensorBattVoltageVerisense.ADC_SAMPLING_RATES;
import com.shimmerresearch.verisense.sensors.SensorLIS2DW12.LIS2DW12_ACCEL_RATE;

/**
 * @author Mark Nolan
 *
 */
public class API_00003_VerisenseProtocolByteCommunicationTest {
	
	@Test
	public void test001_status_payload() {
		testStartCommon("Test001");
		
		byte[] messageBytes = new byte[] { 
				0x31, 0x38, 0x00, (byte) 0x98, 0x58, 0x01, 0x26, 0x09, 0x18, 0x01, 
				0x00, 0x00, 0x00, 0x57, 0x05, 0x64, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, 
				(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, 0x00, 0x01, 0x07, 0x00, 0x00, 0x49, 
				0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x65, (byte) 0x91, 0x18, 
				(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, 0x00, 0x00, 0x00, 
				0x00, 0x00, (byte) 0xFC, 0x00, 0x00, 0x00, 0x00, 0x00, 0x03, 0x00};
		
		//TODO would be better to be passing statusPayloadBytes through verisenseProtocolByteCommunication rather then splitting out the payload contents
//		VerisenseProtocolByteCommunication verisenseProtocolByteCommunication = new VerisenseProtocolByteCommunication(null);
//		verisenseProtocolByteCommunication.addRadioListener(new RadioListener);
//		verisenseProtocolByteCommunication.handleCommonResponse(statusPayload);
		
		VerisenseMessage verisenseMessage = new VerisenseMessage(messageBytes, System.currentTimeMillis());
		
		StatusPayload statusPayload = new StatusPayload();
		statusPayload.parsePayloadContents(verisenseMessage.payloadBytes);
		System.out.println(statusPayload.generateDebugString());

		assertTrue(statusPayload.isSuccess);

		assertTrue(statusPayload.verisenseId.equals("180926015898"));
		
		assertTrue(statusPayload.batteryLevelMillivolts==1367);
		assertTrue(statusPayload.batteryPercentage==100);
		
		assertTrue(statusPayload.verisenseStatusTimestampMs==109135.89477539062);
		assertTrue(statusPayload.lastTransferSuccessTimestampMs==-1);
		assertTrue(statusPayload.lastTransferFailTimestampMs==-1);
		assertTrue(statusPayload.baseStationTimestampMs==0);
		assertTrue(statusPayload.nextSyncAttemptTimestampMs==0);
		
		assertTrue(statusPayload.batteryVoltageFallCounter==0);
		
		assertTrue(statusPayload.usbPowered==true);
		assertTrue(statusPayload.recordingPaused==false);
		assertTrue(statusPayload.flashIsFull==false);
		assertTrue(statusPayload.powerIsGood==true);
		assertTrue(statusPayload.adaptiveSchedulerEnabled==false);
		assertTrue(statusPayload.dfuServiceOn==false);
		
		assertTrue(statusPayload.storageFreekB==459008);
		assertTrue(statusPayload.storageFullkB==64512);
		assertTrue(statusPayload.storageToDelkB==0);
		assertTrue(statusPayload.storageBadkB==768);

		assertTrue(statusPayload.failedBleConnectionAttemptCount==0);
		assertTrue(statusPayload.flashWriteFailCounter==0);
		assertTrue(statusPayload.timestampNextSyncAttempt==0);
	}

	@Test
	public void test002_production_config_payload() {
		testStartCommon("Test002");
		
		String messageStr = readVerisenseMessageBytesFromFile("/ProdConfigPayload_01.txt");
		byte[] messageBytes = UtilShimmer.hexStringToByteArray(messageStr);

		VerisenseMessage verisenseMessage = new VerisenseMessage(messageBytes, System.currentTimeMillis());
		
		ProductionConfigPayload prodConfigPayload = new ProductionConfigPayload();
		prodConfigPayload.parsePayloadContents(verisenseMessage.payloadBytes);
		System.out.println(prodConfigPayload.generateDebugString());
		
		assertTrue(prodConfigPayload.isSuccess);

		assertTrue(prodConfigPayload.verisenseId.equals("180926015898"));
		assertTrue(prodConfigPayload.manufacturingOrderNumber.equals("18092601"));
		assertTrue(prodConfigPayload.macIdShort.equals("5898"));
		assertTrue(prodConfigPayload.expansionBoardDetails.getExpansionBoardId()==64);
		assertTrue(prodConfigPayload.expansionBoardDetails.getExpansionBoardRev()==1);
		assertTrue(prodConfigPayload.shimmerVerObject.getFirmwareVersionMajor()==1);
		assertTrue(prodConfigPayload.shimmerVerObject.getFirmwareVersionMinor()==2);
		assertTrue(prodConfigPayload.shimmerVerObject.getFirmwareVersionInternal()==91);
		
		byte[] generatedBytes = prodConfigPayload.generatePayloadContents();
		testByteArrays(verisenseMessage.payloadBytes, generatedBytes);
	}

	@Test
	public void test003_time_payload() {
		testStartCommon("Test003");

		byte[] messageBytes = new byte[] {0x35, 0x07, 0x00, 0x02, 0x00, 0x00, 0x00, 0x3D, (byte) 0xD5, 0x15};
		
		VerisenseMessage verisenseMessage = new VerisenseMessage(messageBytes, System.currentTimeMillis());
		
		TimePayload timePayload = new TimePayload();
		timePayload.parsePayloadContents(verisenseMessage.payloadBytes);
		System.out.println(timePayload.generateDebugString());

		assertTrue(timePayload.isSuccess);

		assertTrue(timePayload.getTimeMinutes()==2);
		assertTrue(timePayload.getTimeTicks()==1430845);
		assertTrue(timePayload.getTimeMs()==163665.92407226562);
	}

	@Test
	public void test004_pending_events_payload() {
		testStartCommon("Test004");

		byte[] messageBytes = new byte[] {0x37, 0x03, 0x00, 0x01, 0x05, 0x02};

		VerisenseMessage verisenseMessage = new VerisenseMessage(messageBytes, System.currentTimeMillis());

		PendingEventsPayload pendingEventsPayload = new PendingEventsPayload();
		pendingEventsPayload.parsePayloadContents(verisenseMessage.payloadBytes);
		System.out.println(pendingEventsPayload.generateDebugString());
		
		assertTrue(pendingEventsPayload.isSuccess);

		assertTrue(pendingEventsPayload.pendingEventStatus);
		assertTrue(pendingEventsPayload.pendingEventTimeSync);
		assertTrue(pendingEventsPayload.pendingEventData);
	}

	@Test
	public void test005_operational_config_payload() {
		testStartCommon("Test005");
		
		String messageStr = readVerisenseMessageBytesFromFile("/OpConfigPayload_01.txt");
		byte[] messageBytes = UtilShimmer.hexStringToByteArray(messageStr);

		VerisenseMessage verisenseMessage = new VerisenseMessage(messageBytes, System.currentTimeMillis());

		//TODO improve below (sensor class access and running through VerisenseProtocolByteCommunication) and implement checks for more settings
		
		VerisenseDevice verisenseDevice = new VerisenseDevice(COMMUNICATION_TYPE.BLUETOOTH);
		verisenseDevice.setShimmerVersionObject(VerisenseDevice.FW_CHANGES.CCF21_010_3);
		verisenseDevice.setExpansionBoardDetails(new ExpansionBoardDetails(HW_ID.VERISENSE_DEV_BRD, 1, 0));
		verisenseDevice.setHardwareVersionAndCreateSensorMaps(HW_ID.VERISENSE_DEV_BRD);
		verisenseDevice.configBytesParse(verisenseMessage.payloadBytes, COMMUNICATION_TYPE.BLUETOOTH);
		
		verisenseDevice.printSensorParserAndAlgoMaps();

		//Dev board v64-1 supports Clock, LIS2DW12, LSM6DS3, MAX86916, Battery
		LinkedHashMap<SENSORS, AbstractSensor> mapOfSensorClasses = verisenseDevice.getMapOfSensorsClasses();
		assertTrue(mapOfSensorClasses.size()==5);
		assertTrue(mapOfSensorClasses.containsKey(SENSORS.CLOCK));
		assertTrue(mapOfSensorClasses.containsKey(SENSORS.LIS2DW12));
		assertTrue(mapOfSensorClasses.containsKey(SENSORS.LSM6DS3));
		assertTrue(mapOfSensorClasses.containsKey(SENSORS.MAX86916));
		assertTrue(mapOfSensorClasses.containsKey(SENSORS.Battery));

		assertTrue(verisenseDevice.isSensorEnabled(Configuration.Verisense.SENSOR_ID.LIS2DW12_ACCEL));
		assertTrue(verisenseDevice.isSensorEnabled(Configuration.Verisense.SENSOR_ID.VBATT));
		assertTrue(!verisenseDevice.isSensorEnabled(Configuration.Verisense.SENSOR_ID.LSM6DS3_ACCEL));
		assertTrue(!verisenseDevice.isSensorEnabled(Configuration.Verisense.SENSOR_ID.LSM6DS3_GYRO));
		assertTrue(!verisenseDevice.isSensorEnabled(Configuration.Verisense.SENSOR_ID.GSR));
		assertTrue(!verisenseDevice.isSensorEnabled(Configuration.Verisense.SENSOR_ID.MAX86150_ECG));
		assertTrue(!verisenseDevice.isSensorEnabled(Configuration.Verisense.SENSOR_ID.MAX86916_PPG_BLUE));
		assertTrue(!verisenseDevice.isSensorEnabled(Configuration.Verisense.SENSOR_ID.MAX86916_PPG_GREEN));
		assertTrue(!verisenseDevice.isSensorEnabled(Configuration.Verisense.SENSOR_ID.MAX86XXX_PPG_IR));
		assertTrue(!verisenseDevice.isSensorEnabled(Configuration.Verisense.SENSOR_ID.MAX86XXX_PPG_RED));

		// GEN_CFG_0
		assertTrue(verisenseDevice.isBluetoothEnabled());
		assertTrue(!verisenseDevice.isUsbEnabled());
		assertTrue(verisenseDevice.isPrioritiseLongTermFlash());
		assertTrue(verisenseDevice.isDeviceEnabled());
		assertTrue(verisenseDevice.isRecordingEnabled());

		assertTrue(verisenseDevice.getDataCompressionMode()==DATA_COMPRESSION_MODE.NONE);
		assertTrue(verisenseDevice.getPasskeyMode()==PASSKEY_MODE.SECURE);
		assertTrue(verisenseDevice.getBatteryType()==BATTERY_TYPE.ZINC_AIR);

		SensorLIS2DW12 sensorLIS2DW12 = verisenseDevice.getSensorLIS2DW12();
		assertTrue(sensorLIS2DW12!=null);
		// ACCEL1_CFG_0 
		assertTrue(sensorLIS2DW12.getAccelRate()==LIS2DW12_ACCEL_RATE.LOW_POWER_25_0_HZ);
		assertTrue(sensorLIS2DW12.getAccelLpMode()==SensorLIS2DW12.LIS2DW12_LP_MODE.LOW_POWER1_12BIT_4_5_MG_NOISE);
		assertTrue(sensorLIS2DW12.getAccelMode()==SensorLIS2DW12.LIS2DW12_MODE.LOW_POWER);
		// ACCEL1_CFG_1 
		assertTrue(sensorLIS2DW12.getBwFilt()==SensorLIS2DW12.LIS2DW12_BW_FILT.ODR_DIVIDED_BY_2);
		assertTrue(sensorLIS2DW12.getAccelRange()==SensorLIS2DW12.LIS2DW12_ACCEL_RANGE.RANGE_8G);
		assertTrue(sensorLIS2DW12.getFilteredDataTypeSelection()==SensorLIS2DW12.LIS2DW12_FILTERED_DATA_TYPE_SELECTION.LOW_PASS_FILTER_PATH_SELECTED);
		assertTrue(sensorLIS2DW12.getLowNoise()==SensorLIS2DW12.LIS2DW12_LOW_NOISE.DISABLED);
		// ACCEL1_CFG_2 
		assertTrue(sensorLIS2DW12.getHpFilterMode()==SensorLIS2DW12.LIS2DW12_HP_REF_MODE.DISABLED);
		// ACCEL1_CFG_3 
		assertTrue(sensorLIS2DW12.getFifoMode()==SensorLIS2DW12.LIS2DW12_FIFO_MODE.CONTINUOUS_TO_FIFO_MODE);
		assertTrue(sensorLIS2DW12.getFifoThreshold()==SensorLIS2DW12.LIS2DW12_FIFO_THRESHOLD.SAMPLE_31);

		SensorBattVoltageVerisense sensorBattVoltageVerisense = verisenseDevice.getSensorBatteryVoltage();
		assertTrue(sensorBattVoltageVerisense!=null);
		assertTrue(sensorBattVoltageVerisense.getSensorSamplingRate()==ADC_SAMPLING_RATES.FREQ_51_2_HZ);

		assertTrue(verisenseDevice.getRecordingStartTimeMinutes()==0);
		assertTrue(verisenseDevice.getRecordingEndTimeMinutes()==0);
		assertTrue(verisenseDevice.getBleConnectionRetriesPerDay()==3);
		assertTrue(verisenseDevice.getBleTxPower()==BLE_TX_POWER.MINUS_12_DBM);

		PendingEventSchedule pendingEventScheduleDataTransfer = verisenseDevice.getPendingEventScheduleDataTransfer();
		assertTrue(pendingEventScheduleDataTransfer.getIntervalHours()==24);
		assertTrue(pendingEventScheduleDataTransfer.getWakeupTimeMinutes()==60);
		assertTrue(pendingEventScheduleDataTransfer.getWakeupDurationMinutes()==10);
		assertTrue(pendingEventScheduleDataTransfer.getRetryIntervalMinutes()==15);

		PendingEventSchedule pendingEventScheduleStatusSync = verisenseDevice.getPendingEventScheduleStatusSync();
		assertTrue(pendingEventScheduleStatusSync.getIntervalHours()==24);
		assertTrue(pendingEventScheduleStatusSync.getWakeupTimeMinutes()==60);
		assertTrue(pendingEventScheduleStatusSync.getWakeupDurationMinutes()==10);
		assertTrue(pendingEventScheduleStatusSync.getRetryIntervalMinutes()==15);
		
		PendingEventSchedule pendingEventScheduleRwcSync = verisenseDevice.getPendingEventScheduleRwcSync();
		assertTrue(pendingEventScheduleRwcSync.getIntervalHours()==24);
		assertTrue(pendingEventScheduleRwcSync.getWakeupTimeMinutes()==60);
		assertTrue(pendingEventScheduleRwcSync.getWakeupDurationMinutes()==10);
		assertTrue(pendingEventScheduleRwcSync.getRetryIntervalMinutes()==15);

		assertTrue(verisenseDevice.getAdaptiveSchedulerInterval()==65535);
		assertTrue(verisenseDevice.getAdaptiveSchedulerFailCount()==255);

		// Byte generation test
		byte[] generatedBytes = verisenseDevice.configBytesGenerate(true, COMMUNICATION_TYPE.BLUETOOTH);
		testByteArrays(verisenseMessage.payloadBytes, generatedBytes);
	}

	@Test
	public void test006_read_rwc_schedule() {
		testStartCommon("Test006");

		byte[] messageBytes = new byte[] {0x39, 0x3F, 0x00, 0x0D, (byte) 0xA2, (byte) 0x9E, 0x01, 
				(byte) 0xEF, (byte) 0x8B, 0x12, 0x00, (byte) 0x9C, (byte) 0xA5, (byte) 0x9E, 
				0x01, 0x00, 0x00, 0x00, 0x00, (byte) 0x9C, (byte) 0xA5, (byte) 0x9E, 0x01, 
				0x00, 0x00, 0x00, 0x00, (byte) 0x9C, (byte) 0xA5, (byte) 0x9E, 0x01, 0x00, 
				0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 
				0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 
				0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

		VerisenseMessage verisenseMessage = new VerisenseMessage(messageBytes, System.currentTimeMillis());
		
		RwcSchedulePayload rwcSchedulePayload = new RwcSchedulePayload();
		rwcSchedulePayload.parsePayloadContents(verisenseMessage.payloadBytes);
		System.out.println(rwcSchedulePayload.generateDebugString());
		
		assertTrue(rwcSchedulePayload.isSuccess);

		//TODO add tests
	}
	
	@Test
	public void test007_read_record_buffer_details() {
		testStartCommon("Test007");

		byte[] messageBytes = new byte[] {0x39, 0x34, 0x00, 0x00, 0x01, (byte) 0xFF, (byte) 0xFF, 0x5C, 0x61, 
				0x00, 0x00, 0x7E, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 
				0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, (byte) 0xFF, (byte) 0xFF, 0x20, 0x00, 0x00, 
				0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 
				0x00, 0x00, 0x00, 0x00};

		VerisenseMessage verisenseMessage = new VerisenseMessage(messageBytes, System.currentTimeMillis());
		
		RecordBufferDetailsPayload recordBufferDetailsPayload = new RecordBufferDetailsPayload();
		recordBufferDetailsPayload.parsePayloadContents(verisenseMessage.payloadBytes);
		System.out.println(recordBufferDetailsPayload.generateDebugString());
		
		assertTrue(recordBufferDetailsPayload.isSuccess);
		
		RecordBufferDetails buf0 = recordBufferDetailsPayload.getListOfRecordBufferDetails().get(0);
		assertTrue(buf0.bufferIndex==0);
		assertTrue(buf0.bufferState==1);
		assertTrue(buf0.packagedPayloadIndex==65535);
		assertTrue(buf0.currentByteIndexForSensorData==24924);
		assertTrue(buf0.usedBufferLength==0);
		assertTrue(buf0.fifoTicks==126);
		assertTrue(buf0.dataTsRwcMinutes==0);
		assertTrue(buf0.dataTsRwcTicks==0);
		assertTrue(buf0.temperatureData==0);
		assertTrue(buf0.dataTsUcClockMinutes==0);
		assertTrue(buf0.dataTsUcClockTicks==0);
		
		RecordBufferDetails buf1 = recordBufferDetailsPayload.getListOfRecordBufferDetails().get(1);
		assertTrue(buf1.bufferIndex==1);
		assertTrue(buf1.bufferState==0);
		assertTrue(buf1.packagedPayloadIndex==65535);
		assertTrue(buf1.currentByteIndexForSensorData==32);
		assertTrue(buf1.usedBufferLength==0);
		assertTrue(buf1.fifoTicks==0);
		assertTrue(buf1.dataTsRwcMinutes==0);
		assertTrue(buf1.dataTsRwcTicks==0);
		assertTrue(buf1.temperatureData==0);
		assertTrue(buf1.dataTsUcClockMinutes==0);
		assertTrue(buf1.dataTsUcClockTicks==0);
	}

	@Test
	public void test008_read_event_log() {
		testStartCommon("Test008");

		String messageStr = readVerisenseMessageBytesFromFile("/EventLogPayload_01.txt");
		byte[] messageBytes = UtilShimmer.hexStringToByteArray(messageStr);
		
		VerisenseMessage verisenseMessage = new VerisenseMessage(messageBytes, System.currentTimeMillis());
		
		EventLogPayload recordBufferDetailsPayload = new EventLogPayload();
		recordBufferDetailsPayload.parsePayloadContents(verisenseMessage.payloadBytes);
		System.out.println(recordBufferDetailsPayload.generateDebugString());
		
		assertTrue(recordBufferDetailsPayload.isSuccess);
		
		//TODO add tests
	}

	@Test
	public void test009_read_memory_lookup_table() {
		testStartCommon("Test009");

		String messageStr = readVerisenseMessageBytesFromFile("/MemoryLookupTablePayload_01.txt");
		byte[] messageBytes = UtilShimmer.hexStringToByteArray(messageStr);
		
		VerisenseMessage verisenseMessage = new VerisenseMessage(messageBytes, System.currentTimeMillis());
		
		MemoryLookupTablePayload memoryLookupTablePayload = new MemoryLookupTablePayload();
		memoryLookupTablePayload.parsePayloadContents(verisenseMessage.payloadBytes);
		System.out.println(memoryLookupTablePayload.generateDebugString());
		
		assertTrue(memoryLookupTablePayload.isSuccess);
		
		//TODO add tests
	}

	private void testStartCommon(String testId) {
		System.out.println("\n" + "******************************      " + testId + "      ******************************\n");
	}

	private void testByteArrays(byte[] payloadBytes, byte[] generatedBytes) {
		assertTrue(payloadBytes.length == generatedBytes.length);
		List<Integer> listOfByteIndexesWithDifferences = new ArrayList<Integer>();
		for(int i=0;i<payloadBytes.length;i++) {
			if(payloadBytes[i] != generatedBytes[i]) {
				listOfByteIndexesWithDifferences.add(i);
			}
		}
		if(listOfByteIndexesWithDifferences.size()>0) {
			String result = "byte comparison failed at indexes " + listOfByteIndexesWithDifferences;
			System.out.println(result);
			assertTrue(result, false);
		}
	}

	private String readVerisenseMessageBytesFromFile(String filePath) {
		URL url = getClass().getResource(filePath);
		StringBuilder contentBuilder = new StringBuilder();
		try (BufferedReader br = new BufferedReader(new FileReader(url.getPath()))) {

			String sCurrentLine;
			while ((sCurrentLine = br.readLine()) != null) {
				contentBuilder.append(sCurrentLine);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return contentBuilder.toString();
	}
	
}
