package com.shimmerresearch.verisense.communication;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;

import org.junit.FixMethodOrder;
import org.junit.Test;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.HW_ID;
import com.shimmerresearch.driverUtilities.UtilShimmer;
import com.shimmerresearch.sensors.AbstractSensor;
import com.shimmerresearch.sensors.AbstractSensor.SENSORS;
import com.shimmerresearch.verisense.SensorBattVoltageVerisense;
import com.shimmerresearch.verisense.SensorLIS2DW12;
import com.shimmerresearch.verisense.VerisenseDevice;
import com.shimmerresearch.verisense.communication.payloads.EventLogPayload;
import com.shimmerresearch.verisense.communication.payloads.MemoryLookupTablePayload;
import com.shimmerresearch.verisense.communication.payloads.PendingEventsPayload;
import com.shimmerresearch.verisense.communication.payloads.ProdConfigPayload;
import com.shimmerresearch.verisense.communication.payloads.RecordBufferDetailsPayload;
import com.shimmerresearch.verisense.communication.payloads.RwcSchedulePayload;
import com.shimmerresearch.verisense.communication.payloads.StatusPayload;
import com.shimmerresearch.verisense.communication.payloads.TimePayload;

@FixMethodOrder
public class API_00003_VerisenseProtocolByteCommunicationTest {
	
	
	@Test
	public void test001_status_payload() {
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
		assertTrue(statusPayload.failCounterFlashWrite==0);
		assertTrue(statusPayload.timestampNextSyncAttempt==0);
	}

	@Test
	public void test002_production_config_payload() {
		byte[] messageBytes = new byte[] {0x33, 0x0D, 0x00, 0x5A, (byte) 0x98, 0x58, 0x01, 0x26, 0x09, 0x18, 0x40, 0x01, 0x01, 0x02, 0x5B, 0x00};

		VerisenseMessage verisenseMessage = new VerisenseMessage(messageBytes, System.currentTimeMillis());
		
		ProdConfigPayload prodConfigPayload = new ProdConfigPayload();
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
	}


	@Test
	public void test003_time_payload() {
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
	public void test005_read_op_config() {
		byte[] messageBytes = new byte[] {0x34, 0x48, 0x00, 0x5A, (byte) 0x97, 0x00, 0x02, 0x00, 
				0x30, 0x20, 0x00, 0x7F, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0x80, 
				0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 
				0x03, (byte) 0xF4, 0x18, 0x3C, 0x00, 0x0A, 0x0F, 0x00, 0x18, 0x3C, 0x00, 0x0A, 
				0x0F, 0x00, 0x18, 0x3C, 0x00, 0x0A, 0x0F, 0x00, 0x17, (byte) 0xFF, (byte) 0xFF, 
				(byte) 0xFF, (byte) 0xFF, 0x3C, 0x00, 0x0E, 0x00, 0x00, 0x63, 0x28, (byte) 0xCC, 
				(byte) 0xCC, 0x1E, 0x00, 0x0A, 0x00, 0x00, 0x00, 0x00, 0x01};

		VerisenseMessage verisenseMessage = new VerisenseMessage(messageBytes, System.currentTimeMillis());

		//TODO improve below (sensor class access and running through VerisenseProtocolByteCommunication) and implement checks for more settings
		
		VerisenseDevice verisenseDevice = new VerisenseDevice();
		verisenseDevice.setShimmerVersionObject(VerisenseDevice.FW_CHANGES.CCF21_010_3);
		verisenseDevice.setHardwareVersionAndCreateSensorMaps(HW_ID.VERISENSE_DEV_BRD);
		verisenseDevice.configBytesParse(verisenseMessage.payloadBytes, COMMUNICATION_TYPE.BLUETOOTH);
		
		verisenseDevice.printSensorParserAndAlgoMaps();
		
		assertTrue(verisenseDevice.isSensorEnabled(Configuration.Verisense.SENSOR_ID.LIS2DW12_ACCEL));
		assertTrue(verisenseDevice.isSensorEnabled(Configuration.Verisense.SENSOR_ID.VBATT));
		assertTrue(!verisenseDevice.isSensorEnabled(Configuration.Verisense.SENSOR_ID.LSM6DS3_ACCEL));
		
		AbstractSensor abstractSensorLis2dw12 = verisenseDevice.getSensorClass(SENSORS.LIS2DW12);
		assertTrue(abstractSensorLis2dw12!=null);
		SensorLIS2DW12 sensorLIS2DW12 = (SensorLIS2DW12)abstractSensorLis2dw12;
		assertTrue(sensorLIS2DW12.getAccelRateFreq()==25.0);
		assertTrue(sensorLIS2DW12.getAccelRangeString().equals("+- 8 g"));

		AbstractSensor abstractSensorGsr = verisenseDevice.getSensorClass(SENSORS.Battery);
		assertTrue(abstractSensorGsr!=null);
		SensorBattVoltageVerisense sensorBattVoltageVerisense = (SensorBattVoltageVerisense)abstractSensorGsr;
		assertTrue(sensorBattVoltageVerisense.getSensorSamplingRate()==51.2);

		//Settings above should look like the following (copied from the Python console output)
//		Complete Response Received
//		GEN_CFG_0
//			ACCEL_1_EN=1 (TEST DONE ABOVE) 
//			ACCEL_2_EN=0  (TEST DONE ABOVE)
//			GYRO_EN=0 
//			BLUETOOTH_EN=1 
//			USB_EN=0 
//			PRIORITISE_LONG_TERM_FLASH=1 
//			DEVICE_EN=1 
//			RECORDING_EN=1 
//		GEN_CFG_1 
//			GSR_EN=0 
//			PPG_GREEN_EN=0 
//			PPG_RED_EN=0 
//			PPG_IR_EN=0 
//			ECG_EN=0 
//			PPG_BLUE_EN=0 
//		GEN_CFG_2 
//			VBATT_EN=1  (TEST DONE ABOVE)
//		ACCEL1_CFG_0 
//			ODR=3,	'High-Performance / Low-Power mode 25 Hz' 
//			MODE=0,	'Low-Power Mode (12/14-bit resolution)' 
//			LP_MODE=0,	'Low-Power Mode 1 (12-bit resolution, Noise = 4.5mg(RMS)) 
//		'ACCEL1_CFG_1 
//			BW_FILT=0 
//			FS=2	FS=2,	'+-8 g' (TEST DONE ABOVE)
//			FDS=0 
//			LOW_NOISE=0 
//		ACCEL1_CFG_2 
//			HP_REF_MODE=0 
//		ACCEL1_CFG_3 
//			FMode=3 
//			FTH=15 
//		START_TIME= 0 
//		END_TIME= 0 
//		BLE_CONNECTION_TRIES_PER_DAY= 3 
//		BLE_TX_POWER= -12 dBm
//		BLE_DATA_TRANS=	 Hours=24	WKUP_TIME=60 minutes (01:00)	WKUP_DUR=10 minutes	RETRY_INT=15 minutes
//		BLE_STATUS=		 Hours=24	WKUP_TIME=60 minutes (01:00)	WKUP_DUR=10 minutes	RETRY_INT=15 minutes
//		BLE_RTC_SYNC=	 Hours=24	WKUP_TIME=60 minutes (01:00)	WKUP_DUR=10 minutes	RETRY_INT=15 minutes
//		ADC SAMPLING RATE = 51.2 Hz  (TEST DONE ABOVE)
//		ADAPTIVE SCHEDULER INTERVAL = 65535
//		ADAPTIVE_SCHEDULER_FAILCOUNT_MAX = 255
	}

	@Test
	public void test006_read_rwc_schedule() {
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
		
		//TODO add tests
	}

	@Test
	public void test008_read_event_log() {
		String messageStr = readVerisenseMessageBytesFromFile("EventLogPayload_01.txt");
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
		String messageStr = readVerisenseMessageBytesFromFile("MemoryLookupTable_01.txt");
		byte[] messageBytes = UtilShimmer.hexStringToByteArray(messageStr);
		
		VerisenseMessage verisenseMessage = new VerisenseMessage(messageBytes, System.currentTimeMillis());
		
		MemoryLookupTablePayload memoryLookupTablePayload = new MemoryLookupTablePayload();
		memoryLookupTablePayload.parsePayloadContents(verisenseMessage.payloadBytes);
		System.out.println(memoryLookupTablePayload.generateDebugString());
		
		assertTrue(memoryLookupTablePayload.isSuccess);
		
		//TODO add tests
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
