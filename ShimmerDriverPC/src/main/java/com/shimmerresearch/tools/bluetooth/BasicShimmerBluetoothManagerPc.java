package com.shimmerresearch.tools.bluetooth;

import com.shimmerresearch.bluetooth.BluetoothProgressReportPerDevice;
import com.shimmerresearch.bluetooth.ShimmerBluetooth;
import com.shimmerresearch.bluetooth.ShimmerRadioInitializer;
import com.shimmerresearch.bluetooth.ShimmerBluetooth.BT_STATE;
import com.shimmerresearch.comms.radioProtocol.CommsProtocolRadio;
import com.shimmerresearch.comms.radioProtocol.LiteProtocol;
import com.shimmerresearch.comms.serialPortInterface.AbstractSerialPortHal;
import com.shimmerresearch.driver.BasicProcessWithCallBack;
import com.shimmerresearch.driver.CallbackObject;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driver.ShimmerMsg;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driver.shimmer4sdk.Shimmer4;
import com.shimmerresearch.exceptions.ShimmerException;
import com.shimmerresearch.managers.bluetoothManager.ShimmerBluetoothManager;
import com.shimmerresearch.pcDriver.ShimmerPC;
import com.shimmerresearch.pcSerialPort.SerialPortCommJssc;

import jssc.SerialPort;

public class BasicShimmerBluetoothManagerPc extends ShimmerBluetoothManager {

	@Override
	protected void loadBtShimmers(Object... params) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addCallBack(BasicProcessWithCallBack basicProcess) {
		// TODO Auto-generated method stub
		callBackObject.setWaitForDataWithSingleInstanceCheck(basicProcess);
	}

	@Override
	public void printMessage(String message) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ShimmerDevice getShimmerGlobalMap(String bluetoothAddress) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected AbstractSerialPortHal createNewSerialPortComm(String comPort, String bluetoothAddress) {
		SerialPortCommJssc serialPortCommJssc = new SerialPortCommJssc(comPort, comPort, SerialPort.BAUDRATE_115200);
		serialPortCommJssc.setTimeout(AbstractSerialPortHal.SERIAL_PORT_TIMEOUT_500);
		return serialPortCommJssc;
	}

	@Override
	protected void connectExistingShimmer(Object... params) throws ShimmerException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see com.shimmerresearch.managers.bluetoothManager.ShimmerBluetoothManager#createNewShimmer3(java.lang.String, java.lang.String)
	 */
	@Override
	protected ShimmerDevice createNewShimmer3(String comPort, String bluetoothAddress) {
		ShimmerPC shimmerPcmss = new ShimmerPC(comPort);
		putShimmerGlobalMap(bluetoothAddress, shimmerPcmss);
		return shimmerPcmss;
	}


	/* (non-Javadoc)
	 * @see com.shimmerresearch.managers.bluetoothManager.ShimmerBluetoothManager#createNewShimmer3(com.shimmerresearch.bluetooth.ShimmerRadioInitializer, java.lang.String)
	 */
	@Override
	protected ShimmerDevice createNewShimmer3(ShimmerRadioInitializer radioInitializer, String bluetoothAddress) {
    	SerialPortCommJssc serialPortComm = (SerialPortCommJssc) radioInitializer.getSerialCommPort();
    	String comPort = serialPortComm.mComPort;
    	
    	ShimmerPC shimmerDevice = (ShimmerPC)createNewShimmer3(comPort, bluetoothAddress);
    	
    	setupShimmer3BluetoothForBtManager(shimmerDevice);
		if(serialPortComm!=null){
			shimmerDevice.setSerialPort(serialPortComm.getSerialPort());
		}
    	return shimmerDevice;
    }

	protected Shimmer4 createNewShimmer4() {
		return new Shimmer4();
	}
	
	@Override
	protected Shimmer4 createNewShimmer4(String comPort, String bluetoothAddress) {
		Shimmer4 shimmer4 = createNewShimmer4();
		shimmer4.setComPort(comPort);
		putShimmerGlobalMap(bluetoothAddress, shimmer4);
		return shimmer4;
	}

	@Override
	protected Shimmer4 createNewShimmer4(ShimmerRadioInitializer radioInitializer, String bluetoothAddress) {
    	SerialPortCommJssc serialPortComm = (SerialPortCommJssc) radioInitializer.getSerialCommPort();
    	String comPort = serialPortComm.mComPort;

		Shimmer4 shimmer4 = createNewShimmer4(comPort, bluetoothAddress);
		if(serialPortComm!=null){
			CommsProtocolRadio commsProtocolRadio = new CommsProtocolRadio(serialPortComm, new LiteProtocol(comPort));
			shimmer4.setCommsProtocolRadio(commsProtocolRadio);
		}

    	return shimmer4;
    }
	
	@Override
	public void connectShimmerThroughCommPort(String comPort){
		directConnectUnknownShimmer=true;
		ConnectThread connectThread = new ConnectThread(comPort, null, null);
		connectThread.start();
	}

	@Override
	public void putShimmerGlobalMap(String bluetoothAddress, ShimmerDevice shimmerDevice) {
		// TODO Auto-generated method stub
		
	}
	
	protected void setupShimmer3BluetoothForBtManager(ShimmerDevice shimmerDevice) {
		((ShimmerPC)shimmerDevice).setUseInfoMemConfigMethod(USE_INFOMEM_CONFIG_METHOD);
	}
	


	//-------------- Callback methods start -----------------------------
	
	public BasicProcessWithCallBack callBackObject = new BasicProcessWithCallBack() {
		
		@Override
		protected void processMsgFromCallback(ShimmerMsg shimmerMSG) {
			sendCallBackMsg(shimmerMSG);
		}
	};
	
	
}






