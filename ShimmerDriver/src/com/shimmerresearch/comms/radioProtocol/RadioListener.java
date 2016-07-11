package com.shimmerresearch.comms.radioProtocol;

import com.shimmerresearch.bluetooth.BluetoothProgressReportPerCmd;
import com.shimmerresearch.bluetooth.ShimmerBluetooth.BT_STATE;

public interface RadioListener {

	//Inherited from ByteLevelDataCommListener()
	public void connected();
	public void disconnected();

	//Inherited from ProtocolListener()
	public void eventNewPacket(byte[] packetByteArray);
	@Deprecated
	public void eventResponseReceived(byte[] responseBytes);
	public void eventResponseReceived(byte response, Object parsedResponse);
	public void eventAckReceived(byte[] instructionSent);
	
	public void startOperationCallback(BT_STATE currentOperation, int totalNumOfCmds);
	public void finishOperationCallback(BT_STATE currentOperation);
	public void sendProgressReportCallback(BluetoothProgressReportPerCmd progressReportPerCmd);

	

}
