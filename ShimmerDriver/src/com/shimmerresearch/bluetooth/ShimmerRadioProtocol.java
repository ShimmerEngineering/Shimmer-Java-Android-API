package com.shimmerresearch.bluetooth;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

import com.shimmerresearch.comms.radioProtocol.ProtocolListener;
import com.shimmerresearch.comms.radioProtocol.RadioListener;
import com.shimmerresearch.comms.radioProtocol.ByteLevelProtocol;
import com.shimmerresearch.comms.radioProtocol.ShimmerLiteProtocolInstructionSet.LiteProtocolInstructionSet;
import com.shimmerresearch.comms.serialPortInterface.ByteLevelDataComm;
import com.shimmerresearch.comms.serialPortInterface.ByteLevelDataCommListener;
import com.shimmerresearch.driver.BasicProcessWithCallBack;
import com.shimmerresearch.driver.DeviceException;
import com.shimmerresearch.driver.ShimmerMsg;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.HW_ID;

public class ShimmerRadioProtocol extends BasicProcessWithCallBack implements Serializable{

	/** * */
	private static final long serialVersionUID = -5368287098255841194L;
	
	public int mPacketSize;
	public transient List<RadioListener> mRadioListenerList = new ArrayList<RadioListener>();
	public transient ByteLevelProtocol mRadioProtocol = null; //pass the radio controls to the protocol, lite protocol can be replaced by any protocol
	public ByteLevelDataComm mSerialPort;
	
	public ShimmerRadioProtocol(ByteLevelDataComm dataComm, ByteLevelProtocol radioProtocol){
		mSerialPort = dataComm;
		mSerialPort.clearByteLevelDataCommListener();
		mRadioProtocol = radioProtocol;
		mRadioProtocol.setByteLevelDataComm(dataComm);
		initialize();
	}
	
	public void connect() throws DeviceException{
		try{
			mSerialPort.connect();
		}
		catch (DeviceException e) {
        	
			throw(e);
        }
	};
	
	public void disconnect() throws DeviceException{
		mRadioProtocol.stop();
		try{
			mSerialPort.disconnect();
		}
		catch (DeviceException e) {
        	
			throw(e);
        }
	};

	public void stopStreaming(){
		mRadioProtocol.writeInstruction(new byte[]{LiteProtocolInstructionSet.InstructionsSet.STOP_STREAMING_COMMAND_VALUE});
	}
	
	public void startStreaming(){
		mRadioProtocol.writeInstruction(new byte[]{LiteProtocolInstructionSet.InstructionsSet.START_STREAMING_COMMAND_VALUE});
	}
	
	public void startSDLogging(){
		
	}
	
	/**Could be used by InfoMem or Expansion board memory
	 * @param command
	 * @param address
	 * @param infoMemBytes
	 */
	public void writeMemCommand(int command, int address, byte[] infoMemBytes) {
			
			byte[] memLengthToWrite = new byte[]{(byte) infoMemBytes.length};
			byte[] memAddressToWrite = ByteBuffer.allocate(2).putShort((short)(address&0xFFFF)).array();
			ArrayUtils.reverse(memAddressToWrite);

			// TODO check I'm not missing the last two bytes here because the mem
			// address length is not being included in the length field
			byte[] instructionBuffer = new byte[1 + memLengthToWrite.length + memAddressToWrite.length + infoMemBytes.length];
	    	instructionBuffer[0] = (byte)command;
			System.arraycopy(memLengthToWrite, 0, instructionBuffer, 1, memLengthToWrite.length);
			System.arraycopy(memAddressToWrite, 0, instructionBuffer, 1 + memLengthToWrite.length, memAddressToWrite.length);
			System.arraycopy(infoMemBytes, 0, instructionBuffer, 1 + memLengthToWrite.length + memAddressToWrite.length, infoMemBytes.length);
			
			mRadioProtocol.writeInstruction(instructionBuffer);
		
	}
	
	/**
	 * Transmits a command to the Shimmer device to enable the sensors. To enable multiple sensors an or operator should be used (e.g. writeEnabledSensors(SENSOR_ACCEL|SENSOR_GYRO|SENSOR_MAG)). Command should not be used consecutively. Valid values are SENSOR_ACCEL, SENSOR_GYRO, SENSOR_MAG, SENSOR_ECG, SENSOR_EMG, SENSOR_GSR, SENSOR_EXP_BOARD_A7, SENSOR_EXP_BOARD_A0, SENSOR_BRIDGE_AMP and SENSOR_HEART.
    SENSOR_BATT
	 * @param enabledSensors e.g SENSOR_ACCEL|SENSOR_GYRO|SENSOR_MAG
	 */
	public void writeEnabledSensors(long enabledSensors) {
		
		byte secondByte=(byte)((enabledSensors & 0xFF00)>>8);
		byte firstByte=(byte)(enabledSensors & 0xFF);
		byte thirdByte=(byte)((enabledSensors & 0xFF0000)>>16);
		mRadioProtocol.writeInstruction(new byte[]{LiteProtocolInstructionSet.InstructionsSet.SET_SENSORS_COMMAND_VALUE,(byte) firstByte,(byte) secondByte,(byte) thirdByte});
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
	
	
	
	public void setRadioListener(RadioListener radioListener){
		mRadioListenerList.add(radioListener);
	}
	
	public void removeRadioListenerList(){
		mRadioListenerList.clear();
	}

	
	
	
	private void initialize(){
		
		mSerialPort.setVerboseMode(false,false);
		
		mSerialPort.setByteLevelDataCommListener(new ByteLevelDataCommListener(){

			@Override
			public void eventConnected() {
				// TODO Auto-generated method stub
				mRadioProtocol.initialize();
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
								BluetoothProgressReportPerCmd progressReportPerCmd) {
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

			@Override
			public void eventDisconnected() {
				// TODO Auto-generated method stub

				for (RadioListener rl:mRadioListenerList){
					rl.disconnected();
				}
			
			}

			
			});
		if (mSerialPort.isConnected()){
			mSerialPort.eventDeviceConnected();
		}
		
	}

	@Override
	protected void processMsgFromCallback(ShimmerMsg shimmerMSG) {
		// TODO Auto-generated method stub
		
	}

	
	
	
}
