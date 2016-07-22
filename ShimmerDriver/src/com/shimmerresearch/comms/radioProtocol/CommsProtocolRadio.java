package com.shimmerresearch.comms.radioProtocol;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.shimmerresearch.bluetooth.BluetoothProgressReportPerCmd;
import com.shimmerresearch.bluetooth.ShimmerBluetooth;
import com.shimmerresearch.bluetooth.ShimmerBluetooth.BT_STATE;
import com.shimmerresearch.comms.serialPortInterface.InterfaceByteLevelDataComm;
import com.shimmerresearch.comms.serialPortInterface.ByteLevelDataCommListener;
import com.shimmerresearch.driver.BasicProcessWithCallBack;
import com.shimmerresearch.driver.DeviceException;
import com.shimmerresearch.driver.ShimmerMsg;


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

/**
 * @author JC Lim, Mark Nolan
 *
 */
public class CommsProtocolRadio extends BasicProcessWithCallBack {

	/** * */
	private static final long serialVersionUID = -5368287098255841194L;
	
	public int mPacketSize;
	public transient List<RadioListener> mRadioListenerList = new ArrayList<RadioListener>();
	public transient AbstractByteLevelProtocol mCommsProtocol = null; //pass the radio controls to the protocol, lite protocol can be replaced by any protocol
	public InterfaceByteLevelDataComm mCommsInterface;
	
	public CommsProtocolRadio(InterfaceByteLevelDataComm commsInterface, AbstractByteLevelProtocol commsProtocol){
		if(commsInterface!=null){
			mCommsInterface = commsInterface;
			mCommsInterface.clearByteLevelDataCommListener();
			
			if(commsProtocol!=null){
				mCommsProtocol = commsProtocol;
				mCommsProtocol.setByteLevelDataComm(commsInterface);
			}
			initialize();
		}
	}
	
	
	private void initialize(){
		mCommsInterface.setVerboseMode(false,false);
		mCommsInterface.addByteLevelDataCommListener(new RadioByteLevelListener());
		
		if (mCommsInterface.isConnected()){
			mCommsInterface.eventDeviceConnected();
		}
	}
	
	
	public void addRadioListener(RadioListener radioListener){
		mRadioListenerList.add(radioListener);
	}
	
	public void removeRadioListenerList(){
		mRadioListenerList.clear();
	}

	
	public void connect() throws DeviceException{
		try{
			mCommsInterface.connect();
		}
		catch (DeviceException dE) {
			disconnect();
			throw(dE);
        }
	};
	
	public void disconnect() throws DeviceException{
		if(mCommsProtocol!=null){
			mCommsProtocol.stop();
		}
		if(mCommsInterface!=null){
			try{
				mCommsInterface.disconnect();
			}
			catch (DeviceException e) {
				throw(e);
	        } finally {
				mCommsInterface=null;
	        }
		}
	};
	
	public boolean isConnected(){
		if(mCommsInterface!=null){
			return mCommsInterface.isConnected();
		}
		return false;
	}

	public void stopStreaming(){
		mCommsProtocol.stopStreaming();
	}
	
	public void startStreaming(){
		mCommsProtocol.startStreaming();
	}
	
	public void startSDLogging(){
		mCommsProtocol.startSDLogging();
	}
	
	public void stopSDLogging() {
		mCommsProtocol.stopSDLogging();
	}

	/**
	 * Transmits a command to the Shimmer device to enable the sensors. To enable multiple sensors an or operator should be used (e.g. writeEnabledSensors(SENSOR_ACCEL|SENSOR_GYRO|SENSOR_MAG)). Command should not be used consecutively. Valid values are SENSOR_ACCEL, SENSOR_GYRO, SENSOR_MAG, SENSOR_ECG, SENSOR_EMG, SENSOR_GSR, SENSOR_EXP_BOARD_A7, SENSOR_EXP_BOARD_A0, SENSOR_BRIDGE_AMP and SENSOR_HEART.
    SENSOR_BATT
	 * @param enabledSensors e.g SENSOR_ACCEL|SENSOR_GYRO|SENSOR_MAG
	 */
	public void writeEnabledSensors(long enabledSensors) {
		mCommsProtocol.writeEnabledSensors(enabledSensors);
	}

	public void toggleLed() {
		mCommsProtocol.toggleLed();
	}

	public void readFWVersion() {
		mCommsProtocol.readFWVersion();
	}

	public void readShimmerVersion() {
		mCommsProtocol.readShimmerVersionNew();
	}

	public void readInfoMem(int address, int size, int maxMemAddress) {
		mCommsProtocol.readInfoMem(address, size, maxMemAddress);
	}
	
	public void writeInfoMem(int startAddress, byte[] buf, int maxMemAddress) {
		mCommsProtocol.writeInfoMem(startAddress, buf, maxMemAddress);
	}

	public void readPressureCalibrationCoefficients() {
		mCommsProtocol.readPressureCalibrationCoefficients();
	}

	public void startTimerCheckIfAlive() {
		mCommsProtocol.startTimerCheckIfAlive();
	}

	public void readExpansionBoardID() {
		mCommsProtocol.readExpansionBoardID();
	}

	public void readLEDCommand() {
		mCommsProtocol.readLEDCommand();
	}

	public void readStatusLogAndStream() {
		mCommsProtocol.readStatusLogAndStream();
	}

	public void readBattery() {
		mCommsProtocol.readBattery();
	}

	public void inquiry() {
		mCommsProtocol.inquiry();
	}
	
	public void startTimerReadStatus() {
		mCommsProtocol.startTimerReadStatus();
	}

	public void startTimerReadBattStatus() {
		mCommsProtocol.startTimerReadBattStatus();
	}

	public void operationPrepare() {
		mCommsProtocol.operationPrepare();
	}
	
	
//	@Override
//	protected void connectionLost() {
//		closeConnection();
////		consolePrintLn("Connection Lost");
//		setBluetoothRadioState(BT_STATE.CONNECTION_LOST);
//	}
//	
//	private void closeConnection(){
//		disconnect();
//		try {
////			if (mIOThread != null) {
////				mIOThread.stop = true;
////				mIOThread = null;
////				if(mUseProcessingThread){
////				mPThread.stop = true;
////				mPThread = null;
////				}
////			}
////			mIsStreaming = false;
////			mIsInitialised = false;
//
//			setBluetoothRadioState(BT_STATE.DISCONNECTED);
//			if (mSerialPort != null){
//				
//				if(mSerialPort.isOpened ()) {
//				  mSerialPort.purgePort (1);
//				  mSerialPort.purgePort (2);
//				  mSerialPort.closePort ();
//				}
//				
//			}
//			 mSerialPort = null;
//		} catch (SerialPortException ex) {
//			consolePrintException(ex.getMessage(), ex.getStackTrace());
//			setBluetoothRadioState(BT_STATE.DISCONNECTED);
//		}			
//	}

	@Override
	protected void processMsgFromCallback(ShimmerMsg shimmerMSG) {
		// TODO Auto-generated method stub
		
	}
	
	
	public class RadioByteLevelListener implements ByteLevelDataCommListener {

		@Override
		public void eventConnected() {
			for (RadioListener rl:mRadioListenerList){
				rl.connected();
			}

			try {
				mCommsProtocol.setProtocolListener(new CommsProtocolListener());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}


			mCommsProtocol.initialize();
		}

		@Override
		public void eventDisconnected() {
			for (RadioListener rl:mRadioListenerList){
				rl.disconnected();
			}
		}
	}
	
	public class CommsProtocolListener implements ProtocolListener{

		@Override
		public void eventAckReceived(int lastSentInstruction) {
			for (RadioListener rl:mRadioListenerList){
				rl.eventAckReceived(lastSentInstruction);
			}
		}

		@Override
		public void eventNewPacket(byte[] packet) {
			for (RadioListener rl:mRadioListenerList){
				rl.eventNewPacket(packet);
			}
		}

		@Override
		public void eventNewResponse(byte[] respB) {
			for (RadioListener rl:mRadioListenerList){
				rl.eventResponseReceived(respB);
			}
		}

		@Override
		public void hasStopStreaming() {
			for (RadioListener rl:mRadioListenerList){
				rl.hasStopStreamingCallback();
			}
		}

		@Override
		public void eventLogAndStreamStatusChangedCallback(int lastSentInstruction) {
//			if (mSerialPort.isConnected()){
//			mRadioProtocol.setPacketSize(41);
//		}
			for (RadioListener rl:mRadioListenerList){
				rl.eventLogAndStreamStatusChangedCallback(lastSentInstruction);
			}
		}

		@Override
		public void eventAckInstruction(byte[] bs) {
			for (RadioListener rl:mRadioListenerList){
//				rl.eventAckInstruction(bs);
			}
		}

		@Override
		public void eventByteResponseWhileStreaming(byte[] b) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void isNowStreaming() {
			for (RadioListener rl:mRadioListenerList){
				rl.isNowStreamingCallback();
			}
		}

		@Override
		public void eventNewResponse(int responseCommand, Object parsedResponse) {
			for (RadioListener rl:mRadioListenerList){
				rl.eventResponseReceived(responseCommand, parsedResponse);
			}
		}

		@Override
		public void sendStatusMSGtoUI(String msg) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void startOperation(BT_STATE currentOperation, int totalNumOfCmds) {
			for (RadioListener rl:mRadioListenerList){
				rl.startOperationCallback(currentOperation, totalNumOfCmds);
			}
		}

		@Override
		public void finishOperation(BT_STATE currentOperation) {
			for (RadioListener rl:mRadioListenerList){
				rl.finishOperationCallback(currentOperation);
			}
		}
		
		@Override
		public void sendProgressReport(BluetoothProgressReportPerCmd progressReportPerCmd) {
			for (RadioListener rl:mRadioListenerList){
				rl.sendProgressReportCallback(progressReportPerCmd);
			}
		}

		@Override
		public void eventDockedStateChange() {
			for (RadioListener rl:mRadioListenerList){
				rl.eventDockedStateChange();
			}
		}

		@Override
		public void initialiseStreamingCallback() {
			for (RadioListener rl:mRadioListenerList){
				rl.initialiseStreamingCallback();
			}
		}

//		@Override
//		public void eventSyncStates(boolean isDocked, boolean isInitialised, boolean isSdLogging, boolean isSensing, boolean isStreaming, boolean haveAttemptedToRead) {
//			for (RadioListener rl:mRadioListenerList){
//				rl.eventSyncStates(isDocked, isInitialised, isSdLogging, isSensing, isStreaming, haveAttemptedToRead);;
//			}
//		}

		@Override
		public void eventSetIsDocked(boolean isDocked) {
			for (RadioListener rl:mRadioListenerList){
				rl.eventSetIsDocked(isDocked);
			}
		}

		@Override
		public void eventSetIsStreaming(boolean isStreaming) {
			for (RadioListener rl:mRadioListenerList){
				rl.eventSetIsStreaming(isStreaming);
			}
		}

		@Override
		public void eventSetIsSensing(boolean isSensing) {
			for (RadioListener rl:mRadioListenerList){
				rl.eventSetIsSensing(isSensing);
			}
		}

		@Override
		public void eventSetIsSDLogging(boolean isSdLogging) {
			for (RadioListener rl:mRadioListenerList){
				rl.eventSetIsSDLogging(isSdLogging);
			}
		}

		@Override
		public void eventSetIsInitialised(boolean isInitialised) {
			for (RadioListener rl:mRadioListenerList){
				rl.eventSetIsInitialised(isInitialised);
			}
		}

		@Override
		public void eventSetHaveAttemptedToRead(boolean haveAttemptedToRead) {
			for (RadioListener rl:mRadioListenerList){
				rl.eventSetHaveAttemptedToRead(haveAttemptedToRead);
			}
		}



	}

	
}
