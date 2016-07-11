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
public class CommsProtocolRadio extends BasicProcessWithCallBack implements Serializable{

	/** * */
	private static final long serialVersionUID = -5368287098255841194L;
	
	public int mPacketSize;
	public transient List<RadioListener> mRadioListenerList = new ArrayList<RadioListener>();
	public transient AbstractByteLevelProtocol mRadioProtocol = null; //pass the radio controls to the protocol, lite protocol can be replaced by any protocol
	public InterfaceByteLevelDataComm mSerialPort;
	
	public CommsProtocolRadio(InterfaceByteLevelDataComm dataComm, AbstractByteLevelProtocol radioProtocol){
		if(dataComm!=null){
			mSerialPort = dataComm;
			mSerialPort.clearByteLevelDataCommListener();
			
			if(radioProtocol!=null){
				mRadioProtocol = radioProtocol;
				mRadioProtocol.setByteLevelDataComm(dataComm);
			}
			initialize();
		}
	}
	
	
	private void initialize(){
		mSerialPort.setVerboseMode(false,false);
		mSerialPort.setByteLevelDataCommListener(new RadioByteLevelListener());
		
		if (mSerialPort.isConnected()){
			mSerialPort.eventDeviceConnected();
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
			mSerialPort.connect();
		}
		catch (DeviceException dE) {
			disconnect();
			throw(dE);
        }
	};
	
	public void disconnect() throws DeviceException{
		if(mRadioProtocol!=null){
			mRadioProtocol.stop();
		}
		if(mSerialPort!=null){
			try{
				mSerialPort.disconnect();
			}
			catch (DeviceException e) {
				throw(e);
	        } finally {
				mSerialPort=null;
	        }
		}
	};

	public void stopStreaming(){
		mRadioProtocol.stopStreaming();
	}
	
	public void startStreaming(){
		mRadioProtocol.startStreaming();
	}
	
	public void startSDLogging(){
		
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

	public void readInfoMem(int address, int size, int maxMemAddress) {
		mRadioProtocol.readInfoMem(address, size, maxMemAddress);
	}
	
	public void writeInfoMem(int startAddress, byte[] buf, int maxMemAddress) {
		mRadioProtocol.writeInfoMem(startAddress, buf, maxMemAddress);
	}

	public void readPressureCalibrationCoefficients() {
		mRadioProtocol.readPressureCalibrationCoefficients();
	}

	public void startTimerCheckIfAlive() {
		mRadioProtocol.startTimerCheckIfAlive();
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

	public void inquiry() {
		mRadioProtocol.inquiry();
	}

	public void startTimerReadStatus() {
		mRadioProtocol.startTimerReadStatus();
	}

	public void startTimerReadBattStatus() {
		mRadioProtocol.startTimerReadBattStatus();
	}

	public void operationPrepare() {
		mRadioProtocol.operationPrepare();
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
			try {
				mRadioProtocol.setProtocolListener(new CommsProtocolListener());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			for (RadioListener rl:mRadioListenerList){
				rl.connected();
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
		public void eventAckReceived(byte[] sentInstruction) {
			for (RadioListener rl:mRadioListenerList){
				rl.eventAckReceived(sentInstruction);
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
			// TODO Auto-generated method stub
			
		}

		@Override
		public void eventLogAndStreamStatusChanged() {
			if (mSerialPort.isConnected()){
				mRadioProtocol.setPacketSize(41);
			}
		}

		@Override
		public void eventAckInstruction(byte[] bs) {
			for (RadioListener rl:mRadioListenerList){
				rl.eventAckReceived(bs);
			}
		}

		@Override
		public void eventByteResponseWhileStreaming(byte[] b) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void isNowStreaming() {
//			for (RadioListener rl:mRadioListenerList){
//				rl.isNowStreaming();
//			}
		}

		@Override
		public void eventNewResponse(byte responseCommand, Object parsedResponse) {
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

	}
	
}
