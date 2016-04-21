package com.shimmerresearch.pcRadioDriver;

import jssc.SerialPort;
import jssc.SerialPortException;

import com.shimmerresearch.bluetooth.ProgressReportPerCmd;
import com.shimmerresearch.bluetooth.ShimmerBluetooth.BT_STATE;
import com.shimmerresearch.bluetooth.ShimmerBluetooth.IOThread;
import com.shimmerresearch.bluetooth.ShimmerBluetooth.ProcessingThread;
import com.shimmerresearch.comms.radioProtocol.ShimmerRadio;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.sensor.ActionSetting;

public class ShimmerBTRadioPC extends ShimmerRadio{

	transient SerialPort mSerialPort=null;
	
	@Override
	protected void connect(String address) {}

	@Override
	protected boolean bytesAvailableToBeRead() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected int availableBytes() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected void writeBytes(byte[] data) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void stop() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void sendProgressReport(ProgressReportPerCmd pr) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void isReadyForStreaming() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void isNowStreaming() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void hasStopStreaming() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void sendStatusMsgPacketLossDetected() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void inquiryDone() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void sendStatusMSGtoUI(String msg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void printLogDataForDebugging(String msg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void connectionLost() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void setState(BT_STATE state) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void startOperation(BT_STATE currentOperation) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void finishOperation(BT_STATE currentOperation) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void startOperation(BT_STATE currentOperation, int totalNumOfCmds) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void logAndStreamStatusChanged() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void batteryStatusChanged() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected byte[] readBytes(int numberofBytes) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected byte readByte() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected void dockedStateChange() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void actionSettingResolver(ActionSetting ac) {
		// TODO Auto-generated method stub
		
	}

}
