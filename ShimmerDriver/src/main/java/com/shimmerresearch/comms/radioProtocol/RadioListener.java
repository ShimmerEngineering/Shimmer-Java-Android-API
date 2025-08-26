package com.shimmerresearch.comms.radioProtocol;

import com.shimmerresearch.bluetooth.BluetoothProgressReportPerCmd;
import com.shimmerresearch.bluetooth.ShimmerBluetooth.BT_STATE;
import com.shimmerresearch.exceptions.ShimmerException;

public interface RadioListener {

	//Inherited from ByteLevelDataCommListener()
	public void connected();
	public void disconnected();

	//Inherited from ProtocolListener()
	public void eventNewPacket(byte[] packetByteArray, long pcTimestamp);
	@Deprecated
	public void eventNewResponse(byte[] responseBytes);
	public void eventResponseReceived(int responseCommand, Object parsedResponse);
	public void eventAckReceived(int lastSentInstruction);
	
	public void startOperationCallback(BT_STATE currentOperation, int totalNumOfCmds);
	public void finishOperationCallback(BT_STATE currentOperation);
	public void sendProgressReportCallback(BluetoothProgressReportPerCmd progressReportPerCmd);
	
	public void eventLogAndStreamStatusChangedCallback(int lastSentInstruction);
	public void eventDockedStateChange();
	public void isNowStreamingCallback();
	public void hasStopStreamingCallback();
	public void isNowStreamLoggedDataCallback();
	public void hasStopStreamLoggedDataCallback(String binPath);
	public void initialiseStreamingCallback();
	
//	public void eventSyncStates(boolean isDocked, boolean isInitialised, boolean isSdLogging, boolean isSensing, boolean isStreaming, boolean haveAttemptedToRead);
	public void eventSetIsDocked(boolean isDocked);
	public void eventSetIsStreaming(boolean isStreaming);
	public void eventSetIsSensing(boolean isSensing);
	public void eventSetIsSDLogging(boolean isSdLogging);
	public void eventSetIsInitialised(boolean isInitialised);
	public void eventSetHaveAttemptedToRead(boolean haveAttemptedToRead);
	public void eventError(ShimmerException dE);
	public void eventNewSyncPayloadReceived(int payloadIndex, boolean crcError, double transferRateBytes, String binFilePath);

}
