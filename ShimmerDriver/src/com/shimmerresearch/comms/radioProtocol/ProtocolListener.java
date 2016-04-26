package com.shimmerresearch.comms.radioProtocol;

import com.shimmerresearch.bluetooth.ProgressReportPerCmd;

public interface ProtocolListener {

	public void eventAckReceived();
	public void eventNewPacket(byte[] b);
	public void eventNewResponse(byte[] b);
	public void hasStopStreaming();
	public void eventLogAndStreamStatusChanged();
	public void sendProgressReport(ProgressReportPerCmd progressReportPerCmd);
	public void eventAckInstruction(byte[] bs);
	public void eventByteResponseWhileStreaming(byte[] b);
	
}
