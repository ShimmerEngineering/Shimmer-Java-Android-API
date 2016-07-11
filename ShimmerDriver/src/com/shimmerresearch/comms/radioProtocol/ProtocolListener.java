
package com.shimmerresearch.comms.radioProtocol;

import com.shimmerresearch.bluetooth.BluetoothProgressReportPerCmd;
import com.shimmerresearch.bluetooth.ShimmerBluetooth.BT_STATE;

public interface ProtocolListener {

	public void eventAckReceived(byte[] b);
	public void eventAckInstruction(byte[] bs);
	public void eventNewPacket(byte[] b);
	public void eventNewResponse(byte[] b);
	public void eventNewResponse(byte responseCommand, Object parsedResponse);
	public void eventByteResponseWhileStreaming(byte[] b);
	
	public void isNowStreaming();
	public void hasStopStreaming();
	public void eventLogAndStreamStatusChanged();
	
	
	public void sendStatusMSGtoUI(String msg);
	public void startOperation(BT_STATE currentOperation, int totalNumOfCmds);
	public void finishOperation(BT_STATE currentOperation);
	public void sendProgressReport(BluetoothProgressReportPerCmd progressReportPerCmd);

	
}

