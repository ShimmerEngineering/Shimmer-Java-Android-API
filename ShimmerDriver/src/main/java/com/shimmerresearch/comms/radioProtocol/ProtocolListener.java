
package com.shimmerresearch.comms.radioProtocol;

import com.shimmerresearch.bluetooth.BluetoothProgressReportPerCmd;
import com.shimmerresearch.bluetooth.ShimmerBluetooth.BT_STATE;
import com.shimmerresearch.exceptions.ShimmerException;

public interface ProtocolListener {

	public void eventAckReceived(int lastSentInstruction);
	@Deprecated
	public void eventAckInstruction(byte[] bs);
	public void eventNewPacket(byte[] b, long pcTimestamp);
	public void eventNewResponse(byte[] b);
	public void eventResponseReceived(int responseCommand, Object parsedResponse);
	
	@Deprecated
	public void eventByteResponseWhileStreaming(byte[] b);
	
	public void isNowStreaming();
	public void hasStopStreaming();
	public void eventLogAndStreamStatusChangedCallback(int lastSentInstruction);
	public void eventDockedStateChange();
	
	
	public void sendStatusMSGtoUI(String msg);
	public void startOperation(BT_STATE currentOperation, int totalNumOfCmds);
	public void finishOperation(BT_STATE currentOperation);
	public void sendProgressReport(BluetoothProgressReportPerCmd progressReportPerCmd);
	public void initialiseStreamingCallback();
	
//	public void eventSyncStates(boolean isDocked, boolean isInitialised, boolean isSdLogging, boolean isSensing, boolean isStreaming, boolean haveAttemptedToRead);
	public void eventSetIsDocked(boolean isDocked);
	public void eventSetIsStreaming(boolean isStreaming);
	public void eventSetIsSensing(boolean isSensing);
	public void eventSetIsSDLogging(boolean isSdLogging);
	public void eventSetIsInitialised(boolean isInitialised);
	public void eventSetHaveAttemptedToRead(boolean haveAttemptedToRead);

	public void eventKillConnectionRequest(ShimmerException dE);
	
	
}

