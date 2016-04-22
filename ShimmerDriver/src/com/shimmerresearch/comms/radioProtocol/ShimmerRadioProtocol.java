package com.shimmerresearch.comms.radioProtocol;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.shimmerresearch.bluetooth.ProgressReportPerCmd;
import com.shimmerresearch.bluetooth.ShimmerBluetooth.BT_STATE;
import com.shimmerresearch.bluetooth.ShimmerBluetooth.IOThread;
import com.shimmerresearch.comms.serialPortInterface.ShimmerSerialEventCallback;
import com.shimmerresearch.comms.serialPortInterface.ShimmerSerialPortInterface;
import com.shimmerresearch.driver.BasicProcessWithCallBack;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.UtilShimmer;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.FW_ID;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.HW_ID;
import com.shimmerresearch.sensor.ActionSetting;

public abstract class ShimmerRadioProtocol extends BasicProcessWithCallBack {

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
	RadioProtocol mRadioProtocol = new LiteProtocol(this); //pass the radio controls to the protocol, lite protocol can be replaced by any protocol
	
	
	public void setRadioListener(RadioListener radioListener){
		mRadioListenerList.add(radioListener);
	}
	
	public void removeRadioListenerList(){
		mRadioListenerList.clear();
	}
	
	public void initialize(){
		mRadioProtocol = new LiteProtocol(this);
		try {
			mRadioProtocol.setProtocolListener(new ProtocolListener(){

				@Override
				public byte[] eventAckReceived() {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public byte[] eventNewPacket() {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public byte[] eventNewResponse() {
					// TODO Auto-generated method stub
					return null;
				}});
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
}
