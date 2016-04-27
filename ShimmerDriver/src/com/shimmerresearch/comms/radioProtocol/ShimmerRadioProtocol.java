package com.shimmerresearch.comms.radioProtocol;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import jssc.SerialPort;
import jssc.SerialPortException;

import com.shimmerresearch.bluetooth.ProgressReportPerCmd;
import com.shimmerresearch.bluetooth.ShimmerBluetooth.BT_STATE;
import com.shimmerresearch.bluetooth.ShimmerBluetooth.IOThread;
import com.shimmerresearch.comms.radioProtocol.ShimmerLiteProtocolInstructionSet.LiteProtocolInstructionSet;
import com.shimmerresearch.comms.serialPortInterface.ErrorCodesSerialPort;
import com.shimmerresearch.comms.serialPortInterface.ShimmerSerialEventCallback;
import com.shimmerresearch.comms.serialPortInterface.ShimmerSerialPortInterface;
import com.shimmerresearch.comms.serialPortInterface.ShimmerSerialPortJssc;
import com.shimmerresearch.driver.BasicProcessWithCallBack;
import com.shimmerresearch.driver.DeviceException;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.ShimmerMsg;
import com.shimmerresearch.driver.UtilShimmer;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.FW_ID;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.HW_ID;
import com.shimmerresearch.sensor.ActionSetting;

public class ShimmerRadioProtocol extends BasicProcessWithCallBack {

	int mPacketSize;
	
	public void connect() throws DeviceException{
		try{
			mSerialPort.connect();
		}
		catch (DeviceException e) {
        	
			throw(e);
        }
		
		isConnected();
		
	};
	
	public void disconnect() throws DeviceException{
		mRadioProtocol.stop();
		try{
			mSerialPort.disconnect();
		}
		catch (DeviceException e) {
        	
			throw(e);
        }
		
		isDisconnected();
		
	};
	
	public void startStreaming(){
		mRadioProtocol.writeInstruction(new byte[]{LiteProtocolInstructionSet.InstructionsSet.START_STREAMING_COMMAND_VALUE});
		
		
	}
	
	//Core radio functions to be implemented by native radio libs , jssc, android .. etc.
	/*
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
	*/
	
	
	List<RadioListener> mRadioListenerList = new ArrayList<RadioListener>();
	RadioProtocol mRadioProtocol = null; //pass the radio controls to the protocol, lite protocol can be replaced by any protocol
	ShimmerSerialPortInterface mSerialPort;
	
	public void setRadioListener(RadioListener radioListener){
		mRadioListenerList.add(radioListener);
	}
	
	public void removeRadioListenerList(){
		mRadioListenerList.clear();
	}
	
	public void isConnected(){
		
		int OS=1;
		if (OS==1){
			mRadioProtocol = new LiteProtocol(mSerialPort);	
			
		} else {
			//mSerialPort = new ShimmerSerialPortAndroid();
			mRadioProtocol = new LiteProtocol(mSerialPort);
		}
		
		try {
			mRadioProtocol.setProtocolListener(new ProtocolListener(){

				@Override
				public void eventAckReceived(byte[] sentInstruction) {
					// TODO Auto-generated method stub
					for (RadioListener rl:mRadioListenerList){
						rl.eventAckReceived(sentInstruction);
					}
				}

				@Override
				public void eventNewPacket(byte[] packet) {
					// TODO Auto-generated method stub
					
					for (RadioListener rl:mRadioListenerList){
						rl.eventNewPacket(packet);
					}
				}

				@Override
				public void eventNewResponse(byte[] respB) {
					// TODO Auto-generated method stub
					
					for (RadioListener rl:mRadioListenerList){
						rl.eventResponseReceived(respB);
					}
				}

				@Override
				public void hasStopStreaming() {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void eventLogAndStreamStatusChanged() {
					// TODO Auto-generated method stub
					if (mSerialPort.isConnected()){
						mRadioProtocol.setPacketSize(41);
					}
				}

				@Override
				public void sendProgressReport(
						ProgressReportPerCmd progressReportPerCmd) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void eventAckInstruction(byte[] bs) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void eventByteResponseWhileStreaming(byte[] b) {
					// TODO Auto-generated method stub
					
				}});
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for (RadioListener rl:mRadioListenerList){
			rl.connected();
		}
	}
	
	public void isDisconnected(){
		for (RadioListener rl:mRadioListenerList){
			rl.disconnected();
		}
	}
	
	public void initialize(String address){
		int OS = 1;
		if (OS==1){
			mSerialPort = new ShimmerSerialPortJssc(address, address, SerialPort.BAUDRATE_115200); 
			mSerialPort.setVerboseMode(false,false);
				
			
		} else {
			//mSerialPort = new ShimmerSerialPortAndroid();
			
		}
		
	}

	@Override
	protected void processMsgFromCallback(ShimmerMsg shimmerMSG) {
		// TODO Auto-generated method stub
		
	}
	
	
}
