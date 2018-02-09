package com.shimmerresearch.comms.radioProtocol;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.shimmerresearch.bluetooth.BluetoothProgressReportPerCmd;
import com.shimmerresearch.bluetooth.ShimmerBluetooth;
import com.shimmerresearch.bluetooth.ShimmerBluetooth.BT_STATE;
import com.shimmerresearch.comms.serialPortInterface.InterfaceSerialPortHal;
import com.shimmerresearch.comms.serialPortInterface.AbstractSerialPortHal;
import com.shimmerresearch.comms.serialPortInterface.ByteLevelDataCommListener;
import com.shimmerresearch.driver.BasicProcessWithCallBack;
import com.shimmerresearch.driver.ShimmerMsg;
import com.shimmerresearch.exceptions.ShimmerException;


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

	private static final long serialVersionUID = -5368287098255841194L;
	
	public int mPacketSize;
	public transient List<RadioListener> mRadioListenerList = new ArrayList<RadioListener>();
	public transient AbstractCommsProtocol mRadioProtocol = null; //pass the radio controls to the protocol, lite protocol can be replaced by any protocol
	/** Hardware abstraction layer */
	public InterfaceSerialPortHal mRadioHal;
	
	public CommsProtocolRadio(InterfaceSerialPortHal radioHal, AbstractCommsProtocol radioProtocol){
		if(radioHal!=null){
			mRadioHal = radioHal;
			mRadioHal.clearByteLevelDataCommListener();
			mRadioHal.setTimeout(AbstractSerialPortHal.SERIAL_PORT_TIMEOUT_2000);
			
			if(radioProtocol!=null){
				mRadioProtocol = radioProtocol;
				mRadioProtocol.setByteLevelDataComm(mRadioHal);
			}
			initialize();
		}
	}
	
	
	private void initialize(){
		mRadioHal.setVerboseMode(false,false);
		mRadioHal.addByteLevelDataCommListener(new RadioByteLevelListener());
		
		if (mRadioHal.isConnected()){
			mRadioHal.eventDeviceConnected();
		}
	}
	
	
	public void addRadioListener(RadioListener radioListener){
		mRadioListenerList.add(radioListener);
	}
	
	public void removeRadioListenerList(){
		mRadioListenerList.clear();
	}

	
	public void connect() throws ShimmerException{
		try{
			mRadioHal.connect();
		}
		catch (ShimmerException dE) {
			disconnect();
			throw(dE);
        }
	};
	
	public void disconnect() throws ShimmerException{
		if(mRadioProtocol!=null){
			mRadioProtocol.stopProtocol();
		}
		if(mRadioHal!=null){
			try{
				mRadioHal.disconnect();
			}
			catch (ShimmerException e) {
				//TODO
				eventError(e);
				throw(e);
	        } finally {
	        	//TODO 2016/07/26 Not sure if this should be here
//				mRadioHal=null;
	        }
		}
	};
	
	public boolean isConnected(){
		if(mRadioHal!=null){
			return mRadioHal.isConnected();
		}
		return false;
	}

	public void stopStreaming(){
		mRadioProtocol.stopStreaming();
	}
	
	public void startStreaming(){
		mRadioProtocol.startStreaming();
	}
	
	public void startSDLogging(){
		mRadioProtocol.startSDLogging();
	}
	
	public void stopSDLogging() {
		mRadioProtocol.stopSDLogging();
	}
	
	public void startDataLogAndStreaming(){
		mRadioProtocol.startDataLogAndStreaming();
	}
	
	public void stopLoggingAndStreaming(){
		mRadioProtocol.stopStreamingAndLogging();
	}


	/**
	 * Transmits a command to the Shimmer device to enable the sensors. To enable multiple sensors an or operator should be used (e.g. writeEnabledSensors(SENSOR_ACCEL|SENSOR_GYRO|SENSOR_MAG)). Command should not be used consecutively. Valid values are SENSOR_ACCEL, SENSOR_GYRO, SENSOR_MAG, SENSOR_ECG, SENSOR_EMG, SENSOR_GSR, SENSOR_EXP_BOARD_A7, SENSOR_EXP_BOARD_A0, SENSOR_BRIDGE_AMP and SENSOR_HEART.
    SENSOR_BATT
	 * @param enabledSensors e.g SENSOR_ACCEL|SENSOR_GYRO|SENSOR_MAG
	 */
	public void writeEnabledSensors(long enabledSensors) {
		mRadioProtocol.writeEnabledSensors(enabledSensors);
	}

	public void toggleLed() {
		mRadioProtocol.toggleLed();
	}

	public void readFWVersion() {
		mRadioProtocol.readFWVersion();
	}

	public void readShimmerVersion() {
		mRadioProtocol.readShimmerVersionNew();
	}

	public void readInfoMem(int startAddress, int size, int maxMemAddress) {
		mRadioProtocol.readInfoMem(startAddress, size);
	}
	
	public void writeInfoMem(int startAddress, byte[] buf, int maxMemAddress) {
		mRadioProtocol.writeInfoMem(startAddress, buf);
	}

	public void readCalibrationDump() {
		mRadioProtocol.readCalibrationDump();
	}

	public void writeCalibrationDump(byte[] calibDump) {
		mRadioProtocol.writeCalibrationDump(calibDump);
	}

	public void readPressureCalibrationCoefficients() {
		mRadioProtocol.readPressureCalibrationCoefficients();
	}

	public void readExpansionBoardID() {
		mRadioProtocol.readExpansionBoardID();
	}

	public void readLEDCommand() {
		mRadioProtocol.readLEDCommand();
	}

	public void readStatusLogAndStream() {
		mRadioProtocol.readStatusLogAndStream();
	}

	public void readBattery() {
		mRadioProtocol.readBattery();
	}

	public void readRealTimeClock() {
		mRadioProtocol.readRealTimeClock();
	}

	public void inquiry() {
		mRadioProtocol.inquiry();
	}

	public void operationPrepare() {
		mRadioProtocol.operationPrepare();
	}
	
	public void operationStart(BT_STATE btState) {
		mRadioProtocol.operationStart(btState);
	}

	public void setPacketSize(int expectedDataPacketSize) {
		if(mRadioProtocol!=null){
			mRadioProtocol.setPacketSize(expectedDataPacketSize);
		}
	}
	
	// -------------- Timers Start ----------------
	public void startTimerCheckIfAlive() {
		mRadioProtocol.startTimerCheckIfAlive();
	}
	
	public void stopTimerCheckIfAlive() {
		mRadioProtocol.stopTimerCheckIfAlive();
	}

	public void startTimerReadStatus() {
		mRadioProtocol.startTimerReadStatus();
	}

	public void stopTimerReadStatus() {
		mRadioProtocol.stopTimerReadStatus();
	}

	public void startTimerReadBattStatus() {
		mRadioProtocol.startTimerReadBattStatus();
	}
	
	public void stopTimerReadBattStatus() {
		mRadioProtocol.stopTimerReadBattStatus();
	}
	// -------------- Timers End ----------------
	
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
				mRadioProtocol.setProtocolListener(new CommsProtocolListener());
			} catch (ShimmerException e) {
				//TODO
				eventError(e);
			}


			mRadioProtocol.initialize();
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
		public void eventNewPacket(byte[] packet, long pcTimestamp) {
			for (RadioListener rl:mRadioListenerList){
				rl.eventNewPacket(packet, pcTimestamp);
			}
		}

		@Override
		public void eventNewResponse(byte[] respB) {
			for (RadioListener rl:mRadioListenerList){
				rl.eventNewResponse(respB);
			}
		}
		
		@Override
		public void eventResponseReceived(int responseCommand, Object parsedResponse) {
			for (RadioListener rl:mRadioListenerList){
				rl.eventResponseReceived(responseCommand, parsedResponse);
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
			int index = 0;
			for (RadioListener rl:mRadioListenerList){
				System.out.println("initialiseStreamingCallback:\t" + index++);
				rl.initialiseStreamingCallback();
			}
			System.out.println("initialiseStreamingCallback:\tFINISHED");
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

		@Override
		public void eventKillConnectionRequest(ShimmerException dE) {
			eventError(dE);
			
			try {
				disconnect();
			} catch (ShimmerException e) {
				//TODO
				eventError(e);
			}
		}

	}

	public void eventError(ShimmerException dE){
		for (RadioListener rl:mRadioListenerList){
			if(rl!=null){
				rl.eventError(dE);
			}
		}
	}


}
