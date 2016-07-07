
package com.shimmerresearch.comms.radioProtocol;

import com.shimmerresearch.bluetooth.BluetoothProgressReportPerCmd;

public interface ProtocolListener {

	public void eventAckReceived(byte[] b);
	public void eventNewPacket(byte[] b);
	public void eventNewResponse(byte[] b);
	public void hasStopStreaming();
	public void eventLogAndStreamStatusChanged();
	public void sendProgressReport(BluetoothProgressReportPerCmd progressReportPerCmd);
	public void eventAckInstruction(byte[] bs);
	public void eventByteResponseWhileStreaming(byte[] b);
	public void isNowStreaming();
	
}

