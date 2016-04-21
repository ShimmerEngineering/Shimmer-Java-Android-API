package com.shimmerresearch.comms.radioProtocol;

import java.util.ArrayList;
import java.util.List;

import com.shimmerresearch.bluetooth.ProgressReportPerCmd;
import com.shimmerresearch.bluetooth.ShimmerBluetooth.BT_STATE;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.sensor.ActionSetting;

public abstract class ShimmerRadio {

	protected abstract void connect(String address);
	
	//Core radio functions to be implemented by native radio libs , jssc, android .. etc.
	protected abstract boolean bytesAvailableToBeRead();
	protected abstract int availableBytes();
	protected abstract void writeBytes(byte[] data);
	protected abstract void stop();
	protected abstract void connectionLost();
	protected abstract byte[] readBytes(int numberofBytes);
	protected abstract byte readByte();
	
	protected abstract void sendProgressReport(ProgressReportPerCmd pr);
	
	protected abstract void isReadyForStreaming();
	protected abstract void isNowStreaming();
	protected abstract void hasStopStreaming();
	protected abstract void sendStatusMsgPacketLossDetected();
	protected abstract void inquiryDone();
	
	protected abstract void sendStatusMSGtoUI(String msg);
	protected abstract void printLogDataForDebugging(String msg);
	
	protected abstract void setState(BT_STATE state);
	protected abstract void startOperation(BT_STATE currentOperation);
	protected abstract void finishOperation(BT_STATE currentOperation);
	protected abstract void startOperation(BT_STATE currentOperation, int totalNumOfCmds);
	protected abstract void logAndStreamStatusChanged();
	protected abstract void batteryStatusChanged();
	
	protected abstract void dockedStateChange();
	
	public abstract void actionSettingResolver(ActionSetting ac);
	
	List<RadioListener> mRadioListenerList = new ArrayList<RadioListener>();
	
	public void setRadioListener(RadioListener radioListener){
		mRadioListenerList.add(radioListener);
	}
	
	public interface RadioListener{
		public void connected();
		
		public void disconnected();
		
		public void eventNewPacket();
		
		public void configurationResponse(byte[] responseBytes);
	}
	
	public void removeRadioListenerList(){
		mRadioListenerList.clear();
	}
	
	
	
	
}
