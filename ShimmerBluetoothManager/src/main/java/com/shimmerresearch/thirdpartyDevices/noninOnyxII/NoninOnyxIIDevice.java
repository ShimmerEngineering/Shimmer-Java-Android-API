package com.shimmerresearch.thirdpartyDevices.noninOnyxII;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedHashMap;
import java.util.TreeMap;

import com.shimmerresearch.bluetooth.ShimmerBluetooth;
import com.shimmerresearch.bluetooth.ShimmerBluetooth.BT_STATE;
import com.shimmerresearch.comms.serialPortInterface.ByteLevelDataCommListener;
import com.shimmerresearch.comms.serialPortInterface.AbstractSerialPortHal;
import com.shimmerresearch.comms.serialPortInterface.SerialPortListener;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driver.CallbackObject;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driver.ShimmerDeviceCallbackAdapter;
import com.shimmerresearch.driver.ShimmerMsg;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;
import com.shimmerresearch.driverUtilities.UtilShimmer;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.HW_ID;
import com.shimmerresearch.exceptions.ShimmerException;
import com.shimmerresearch.sensors.AbstractSensor.SENSORS;
import com.shimmerresearch.sensors.AbstractSensor;
import com.shimmerresearch.sensors.SensorSystemTimeStamp;
import com.shimmerresearch.sensors.ShimmerClock;

//https://www.numed.co.uk/files/uploads/Product/Nonin%209560%20Bluetooth%20Specification.pdf
public class NoninOnyxIIDevice extends ShimmerDevice implements SerialPortListener{
	
	/** * */
	private static final long serialVersionUID = -4620570962788027578L;
	
	public transient AbstractSerialPortHal mSerialPortComm;
	
	private static final byte[] NONIN_CMD_START_STREAMING = new byte[]{0x02, 0x70, 0x02, 0x02, 0x08, 0x03};
	private static final byte NONIN_RESPONSE_ACK = 0x06;
	private static final byte NONIN_RESPONSE_NACK = 0x15;
	
	public static ShimmerVerObject SVO = new ShimmerVerObject(HW_ID.NONIN_ONYX_II, ShimmerVerDetails.ANY_VERSION, ShimmerVerDetails.ANY_VERSION, ShimmerVerDetails.ANY_VERSION, ShimmerVerDetails.ANY_VERSION);
	
	protected ShimmerDeviceCallbackAdapter mDeviceCallbackAdapter = new ShimmerDeviceCallbackAdapter(this);
	
	public NoninOnyxIIDevice(String comPort, String uniqueId){
		mComPort = comPort;
		mUniqueID = uniqueId;
		
		setDefaultShimmerConfiguration();
		mIsTrialDetailsStoredOnDevice = false;
	}

	@Override
	public void setDefaultShimmerConfiguration() {
		super.setDefaultShimmerConfiguration();
		
		addCommunicationRoute(COMMUNICATION_TYPE.BLUETOOTH);

		setShimmerUserAssignedName(mUniqueID);
		setSamplingRateShimmer(1.0);
		setMacIdFromUart(mUniqueID);
		setShimmerUserAssignedNameWithMac("Nonin");
		
		setShimmerVersionObjectAndCreateSensorMap(SVO);
	}
	
	@Override
	public void connect() throws ShimmerException{
		try {
			if(mSerialPortComm!=null){
				mSerialPortComm.connect();
			}
		} catch (ShimmerException e) {
			consolePrintLn("Failed to BT connect");
			consolePrintLn(e.getErrStringFormatted());
//			e.printStackTrace();
		}
	}

	@Override
	public void disconnect() throws ShimmerException {
		try {
			if(mSerialPortComm!=null){
				mSerialPortComm.disconnect();
			}
		} catch (ShimmerException e) {
			consolePrintLn("Failed to BT disconnect");
			e.printStackTrace();
		}
	}

	@Override
	public void startStreaming() {
		try {
			mSerialPortComm.txBytes(NONIN_CMD_START_STREAMING);
			
			threadSleep(1000);
			
			byte[] rxBytes = mSerialPortComm.rxBytes(1);
			if(rxBytes.length>0){
				if(rxBytes[0]==NONIN_RESPONSE_ACK){
					System.out.println("Nonin ACK received");
					setBluetoothRadioState(BT_STATE.STREAMING);
				}
				else if(rxBytes[0]==NONIN_RESPONSE_NACK){
					System.out.println("Nonin NACK received");
					disconnect();
				}
				else{
					//TODO Hack incase device is already streaming
//					setBluetoothRadioState(BT_STATE.STREAMING);
				}
			}
		} catch (ShimmerException e) {
			consolePrintLn("Failed to start streaming");
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/* ******************************************
	 * ShimmerSerialEventCallback Overrides start
	 * ******************************************/

	@Override
	public void serialPortRxEvent(int byteLength) {
		try {
			long pcTimestamp = System.currentTimeMillis();
			
			byte[] rxBytes = mSerialPortComm.rxBytes(byteLength);
//			consolePrintLn(UtilShimmer.bytesToHexStringWithSpacesFormatted(rxBytes));
			
			if(rxBytes.length==4){
				incrementPacketReceivedCountCurrent();
				ObjectCluster objectCluster = buildMsg(rxBytes, COMMUNICATION_TYPE.BLUETOOTH, false, pcTimestamp);
				
//				consolePrintLn("\tTimestamp=" + UtilShimmer.convertMilliSecondsToHrMinSecString(pcTimestamp)
//						+ "\tUnknown=" + rxBytes[0] 
//						+ "\tHeart rate=" + rxBytes[1] 
//						+ "\t%SpO2=" + rxBytes[2] 
//						+ "\tUnknown=" + rxBytes[3]);
				
				dataHandler(objectCluster);
			}
			
		} catch (ShimmerException e) {
			consolePrintLn("Failed to read serial port bytes");
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/* ******************************************
	 * ShimmerSerialEventCallback Overrides end
	 * ******************************************/

	
	/* ******************************************
	 * Shimmer Device Overrides start
	 * ******************************************/

	/* (non-Javadoc)
	 * @see com.shimmerresearch.driver.ShimmerDevice#deepClone()
	 */
	@Override
	public ShimmerDevice deepClone() {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(this);

			ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
			ObjectInputStream ois = new ObjectInputStream(bais);
			return (NoninOnyxIIDevice) ois.readObject();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public void sensorAndConfigMapsCreate() {
		mMapOfSensorClasses = new LinkedHashMap<SENSORS, AbstractSensor>();
		
		mMapOfSensorClasses.put(SENSORS.SYSTEM_TIMESTAMP, new SensorSystemTimeStamp(this));
		mMapOfSensorClasses.put(SENSORS.NONIN_ONYX_II, new SensorNonin(this));
		
		getSensorClass(SENSORS.SYSTEM_TIMESTAMP).setIsEnabledSensor(COMMUNICATION_TYPE.BLUETOOTH, true, Configuration.Shimmer3.SENSOR_ID.HOST_SYSTEM_TIMESTAMP);
		getSensorClass(SENSORS.NONIN_ONYX_II).setIsEnabledSensor(COMMUNICATION_TYPE.BLUETOOTH, true, Configuration.Shimmer3.SENSOR_ID.THIRD_PARTY_NONIN);
		
		super.sensorAndConfigMapsCreateCommon();
//		generateSensorAndParserMaps();
	}

	@Override
	protected void interpretDataPacketFormat(Object object, COMMUNICATION_TYPE commType) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void configBytesParse(byte[] infoMemContents) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public byte[] configBytesGenerate(boolean generateForWritingToShimmer) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void createConfigBytesLayout() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void processMsgFromCallback(ShimmerMsg shimmerMSG) {
		// TODO Auto-generated method stub
		
	}
	
	/* ******************************************
	 * Shimmer Device Overrides end
	 * ******************************************/

	//TODO neaten below. Copy/Paste from Shimmer4
	protected void dataHandler(ObjectCluster ojc) {
//		sendCallBackMsg(ShimmerBluetooth.MSG_IDENTIFIER_DATA_PACKET, ojc);
		mDeviceCallbackAdapter.dataHandler(ojc);
	}

	//TODO neaten below. Copy/Paste from ShimmerBluetooth
	@Override
	public boolean setBluetoothRadioState(BT_STATE state) {
		boolean changed = super.setBluetoothRadioState(state);
		mDeviceCallbackAdapter.setBluetoothRadioState(state);
		
//		if(mBluetoothRadioState==BT_STATE.CONNECTED){
//			setIsConnected(true);
//			setIsInitialised(true);
//			setIsStreaming(false);
//		}
//		else if(mBluetoothRadioState==BT_STATE.STREAMING){
//			setIsStreaming(true);
//		}		
//		else if((mBluetoothRadioState==BT_STATE.DISCONNECTED)
//				||(mBluetoothRadioState==BT_STATE.CONNECTION_LOST)
//				||(mBluetoothRadioState==BT_STATE.CONNECTION_FAILED)){
//			setIsConnected(false);
//			setIsStreaming(false);
//			setIsInitialised(false);
//		}
//		
//		CallbackObject callBackObject = new CallbackObject(ShimmerBluetooth.NOTIFICATION_SHIMMER_STATE_CHANGE, state, getMacIdFromUart(), getComPort());
//		sendCallBackMsg(ShimmerBluetooth.MSG_IDENTIFIER_STATE_CHANGE, callBackObject);
		return changed;
	}
	
	//TODO neaten below. Copy/Paste from Shimmer4
	public void isReadyForStreaming(){
		mDeviceCallbackAdapter.isReadyForStreaming();
//		setIsInitialised(true);
//		
//		CallbackObject callBackObject = new CallbackObject(ShimmerBluetooth.NOTIFICATION_SHIMMER_FULLY_INITIALIZED, getMacIdFromUart(), getComPort());
//		sendCallBackMsg(ShimmerBluetooth.MSG_IDENTIFIER_NOTIFICATION_MESSAGE, callBackObject);
//		if (getBluetoothRadioState()==BT_STATE.CONNECTING){
//			setBluetoothRadioState(BT_STATE.CONNECTED);
//		}
	}

	//TODO neaten below. Copy/Paste from Shimmer4
	public void setRadio(AbstractSerialPortHal serialPortComm) {
		mSerialPortComm = serialPortComm;
		
//		mSerialPortComm = new SerialPortJssc(comPort, uniqueId, SerialPort.BAUDRATE_9600);
		mSerialPortComm.registerSerialPortRxEventCallback(this);
		mSerialPortComm.addByteLevelDataCommListener(new ByteLevelDataCommListener() {
			@Override
			public void eventDisconnected() {
				System.out.println("eventDisconnected");
				setBluetoothRadioState(BT_STATE.DISCONNECTED);
			}
			
			@Override
			public void eventConnected() {
				System.out.println("eventConnected");
				setBluetoothRadioState(BT_STATE.CONNECTED);
				isReadyForStreaming();
				//Autostart streaming on connect -> easier to handle in the GUI as there is no command to stop streaming
				startStreaming();
			}
		});
	}

	@Override
	public void calculatePacketReceptionRateCurrent(int timeDifference) {
		setPacketReceptionRateCurrent(calculatePacketLossCurrent(timeDifference, getSamplingRateShimmer()));
		CallbackObject callBackObject = new CallbackObject(ShimmerBluetooth.MSG_IDENTIFIER_PACKET_RECEPTION_RATE_CURRENT, getMacId(), getComPort(), getPacketReceptionRateCurrent());
		sendCallBackMsg(ShimmerBluetooth.MSG_IDENTIFIER_PACKET_RECEPTION_RATE_CURRENT, callBackObject);
		resetPacketReceptionCurrentCounters();
	}

	//TODO neaten below. Copy/Paste from ShimmerGQ
	public double calculatePacketLossCurrent(long timeDifference, double samplingRate){
		mPacketExpectedCountCurrent = (long) ((double)((timeDifference/1000)*samplingRate));
		return calculatePacketLossCurrent(mPacketExpectedCountCurrent, mPacketReceivedCountCurrent);
	}

	//TODO neaten below. Copy/Paste from ShimmerGQ
	public double calculatePacketLossCurrent(){
		return calculatePacketLossCurrent(mPacketExpectedCountCurrent, mPacketReceivedCountCurrent);
	}

	//TODO neaten below. Copy/Paste from ShimmerGQ
	private double calculatePacketLossCurrent(long packetExpectedCount, long packetReceivedCount){
		setPacketReceptionRateCurrent((double)((packetReceivedCount)/(double)packetExpectedCount)*100);
		//TODO 2016-09-06 remove below because if it is going to be implemented it should be in the method setPacketReceptionRateCurrent()?
		setPacketReceptionRateCurrent(UtilShimmer.nudgeDouble(getPacketReceptionRateCurrent(), 0.0, 100.0));
		return getPacketReceptionRateCurrent();
	}

}
