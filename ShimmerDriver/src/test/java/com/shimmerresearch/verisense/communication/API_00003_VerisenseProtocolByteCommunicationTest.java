package com.shimmerresearch.verisense.communication;

import static org.junit.Assert.assertTrue;

import org.junit.Test;import com.shimmerresearch.bluetooth.BluetoothProgressReportPerCmd;
import com.shimmerresearch.bluetooth.ShimmerBluetooth.BT_STATE;
import com.shimmerresearch.comms.radioProtocol.RadioListener;
import com.shimmerresearch.exceptions.ShimmerException;
import com.shimmerresearch.verisense.communication.ProdConfigPayload;
import com.shimmerresearch.verisense.communication.StatusPayload;
import com.shimmerresearch.verisense.communication.StatusPayload.SyncType;
import com.shimmerresearch.verisense.payloaddesign.VerisenseTimeDetails;

public class API_00003_VerisenseProtocolByteCommunicationTest {
	
	
	@Test
	public void test001_status_payload() {
		byte[] payload = new byte[] { 
				0x31, 0x38, 0x00, (byte) 0x98, 0x58, 0x01, 0x26, 0x09, 0x18, 0x01, 
				0x00, 0x00, 0x00, 0x57, 0x05, 0x64, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, 
				(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, 0x00, 0x01, 0x07, 0x00, 0x00, 0x49, 
				0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x65, (byte) 0x91, 0x18, 
				(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, 0x00, 0x00, 0x00, 
				0x00, 0x00, (byte) 0xFC, 0x00, 0x00, 0x00, 0x00, 0x00, 0x03, 0x00 };
		
		//TODO would be better to be passing statusPayloadBytes through verisenseProtocolByteCommunication rather then splitting out the payload contents
//		VerisenseProtocolByteCommunication verisenseProtocolByteCommunication = new VerisenseProtocolByteCommunication(null);
//		verisenseProtocolByteCommunication.addRadioListener(new RadioListener);
//		verisenseProtocolByteCommunication.handleCommonResponse(statusPayload);
		
		
		byte[] payloadContents = new byte[payload.length-3];
		System.arraycopy(payload, 3, payloadContents, 0, payloadContents.length);
		
		StatusPayload statusPayload = new StatusPayload();
		statusPayload.parsePayloadContents(payloadContents);
		System.out.println(statusPayload.generateDebugString());
		
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
		
		assertTrue(statusPayload.IsSuccess==true);
	}

	@Test
	public void test002_production_config_payload() {
		byte[] payload = new byte[] {0x33, 0x0D, 0x00, 0x5A, (byte) 0x98, 0x58, 0x01, 0x26, 0x09, 0x18, 0x40, 0x01, 0x01, 0x02, 0x5B, 0x00};

		byte[] payloadContents = new byte[payload.length-3];
		System.arraycopy(payload, 3, payloadContents, 0, payloadContents.length);
		
		ProdConfigPayload prodConfigPayload = new ProdConfigPayload();
		prodConfigPayload.parsePayloadContents(payloadContents);
		System.out.println(prodConfigPayload.generateDebugString());

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
		byte[] payload = new byte[] {0x35, 0x07, 0x00, 0x02, 0x00, 0x00, 0x00, 0x3D, (byte) 0xD5, 0x15};
		
		byte[] payloadContents = new byte[payload.length-3];
		System.arraycopy(payload, 3, payloadContents, 0, payloadContents.length);
		
		TimePayload timePayload = new TimePayload();
		timePayload.parsePayloadContents(payloadContents);
		System.out.println(timePayload.generateDebugString());

		assertTrue(timePayload.getTimeMinutes()==2);
		assertTrue(timePayload.getTimeTicks()==1430845);
		assertTrue(timePayload.getTimeMs()==163665.92407226562);
	}
	
}
