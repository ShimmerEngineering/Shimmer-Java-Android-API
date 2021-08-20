package com.shimmerresearch.driver.ble;

import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.exceptions.ShimmerException;
import com.shimmerresearch.bluetooth.BluetoothProgressReportPerCmd;
import com.shimmerresearch.bluetooth.ShimmerBluetooth.BT_STATE;
import com.shimmerresearch.comms.radioProtocol.RadioListener;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driver.ShimmerMsg;

public class VerisenseDevice extends ShimmerDevice {

	VerisenseProtocolByteCommunication mProtocol;

	public void setProtocol(VerisenseProtocolByteCommunication protocol) {
		protocol.addRadioListener(new RadioListener() {

			@Override
			public void startOperationCallback(BT_STATE currentOperation, int totalNumOfCmds) {
				// TODO Auto-generated method stub

			}

			@Override
			public void sendProgressReportCallback(BluetoothProgressReportPerCmd progressReportPerCmd) {
				// TODO Auto-generated method stub

			}

			@Override
			public void isNowStreamingCallback() {
				// TODO Auto-generated method stub

			}

			@Override
			public void initialiseStreamingCallback() {
				// TODO Auto-generated method stub

			}

			@Override
			public void hasStopStreamingCallback() {
				// TODO Auto-generated method stub

			}

			@Override
			public void finishOperationCallback(BT_STATE currentOperation) {
				// TODO Auto-generated method stub

			}

			@Override
			public void eventSetIsStreaming(boolean isStreaming) {
				// TODO Auto-generated method stub

			}

			@Override
			public void eventSetIsSensing(boolean isSensing) {
				// TODO Auto-generated method stub

			}

			@Override
			public void eventSetIsSDLogging(boolean isSdLogging) {
				// TODO Auto-generated method stub

			}

			@Override
			public void eventSetIsInitialised(boolean isInitialised) {
				// TODO Auto-generated method stub

			}

			@Override
			public void eventSetIsDocked(boolean isDocked) {
				// TODO Auto-generated method stub

			}

			@Override
			public void eventSetHaveAttemptedToRead(boolean haveAttemptedToRead) {
				// TODO Auto-generated method stub

			}

			@Override
			public void eventResponseReceived(int responseCommand, Object parsedResponse) {
				// TODO Auto-generated method stub
				System.out.println("VerisenseDevice Response: " + responseCommand + " " + parsedResponse.getClass().getSimpleName());
			}

			@Override
			public void eventNewResponse(byte[] responseBytes) {
				// TODO Auto-generated method stub

			}

			@Override
			public void eventNewPacket(byte[] packetByteArray, long pcTimestamp) {
				// TODO Auto-generated method stub
				System.out.println("VerisenseDevice New Packet: " + pcTimestamp);
			}

			@Override
			public void eventLogAndStreamStatusChangedCallback(int lastSentInstruction) {
				// TODO Auto-generated method stub

			}

			@Override
			public void eventError(ShimmerException dE) {
				// TODO Auto-generated method stub

			}

			@Override
			public void eventDockedStateChange() {
				// TODO Auto-generated method stub

			}

			@Override
			public void eventAckReceived(int lastSentInstruction) {
				// TODO Auto-generated method stub

			}

			@Override
			public void disconnected() {
				// TODO Auto-generated method stub

			}

			@Override
			public void connected() {
				// TODO Auto-generated method stub

			}
		});
	}

	@Override
	public ShimmerDevice deepClone() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void sensorAndConfigMapsCreate() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void interpretDataPacketFormat(Object object, COMMUNICATION_TYPE commType) {
		// TODO Auto-generated method stub

	}

	@Override
	public void configBytesParse(byte[] configBytes) {
		// TODO Auto-generated method stub

	}

	@Override
	public byte[] configBytesGenerate(boolean generateForWritingToShimmer) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void createConfigBytesLayout() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void processMsgFromCallback(ShimmerMsg shimmerMSG) {
		// TODO Auto-generated method stub

	}

}
