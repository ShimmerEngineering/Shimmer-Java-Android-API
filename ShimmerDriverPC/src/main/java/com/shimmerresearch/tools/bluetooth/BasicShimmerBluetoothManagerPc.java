package com.shimmerresearch.tools.bluetooth;

import java.util.ArrayList;
import java.util.List;

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
import com.shimmerresearch.driver.ble.BleRadioByteCommunication;
import com.shimmerresearch.driver.ble.JavelinBLERadioByteCommunication;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driver.shimmer4sdk.Shimmer4sdk;
import com.shimmerresearch.driverUtilities.BluetoothDeviceDetails;
import com.shimmerresearch.exceptions.ConnectionExceptionListener;
import com.shimmerresearch.exceptions.ShimmerException;
import com.shimmerresearch.grpc.GrpcBLERadioByteCommunication;
import com.shimmerresearch.grpc.GrpcBLERadioByteTools;
import com.shimmerresearch.managers.bluetoothManager.ShimmerBluetoothManager;
import com.shimmerresearch.pcDriver.ShimmerGRPC;
import com.shimmerresearch.pcDriver.ShimmerPC;
import com.shimmerresearch.pcSerialPort.SerialPortCommJssc;
import com.shimmerresearch.verisense.VerisenseDevice;
import com.shimmerresearch.verisense.communication.VerisenseProtocolByteCommunication;

import jssc.SerialPort;

public class BasicShimmerBluetoothManagerPc extends ShimmerBluetoothManager {

	String mPathToVeriBLEApp = "bleconsoleapp\\BLEConsoleApp1.exe";
	List<String> verisenseMacIdList = new ArrayList<String>();
	List<String> shimmer3BleMacIdList = new ArrayList<String>();
	List<VerisenseDevice> verisenseDeviceList = new ArrayList<VerisenseDevice>();
	List<ShimmerGRPC> shimmer3BleDeviceList = new ArrayList<ShimmerGRPC>();
	public static int mGRPCPort;
	
	public BasicShimmerBluetoothManagerPc() {
		startGrpc();
	}
	
	public BasicShimmerBluetoothManagerPc(boolean enableGRPC) {
		if(enableGRPC) {
			startGrpc();
		}
	}
	
	protected void startGrpc(String path) {
		try {
			GrpcBLERadioByteTools grpcTool = new GrpcBLERadioByteTools("ShimmerBLEGrpc.exe",path);
			mGRPCPort = grpcTool.startServer();
		}  catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private void startGrpc() {
		try {
			GrpcBLERadioByteTools grpcTool = new GrpcBLERadioByteTools();
			mGRPCPort = grpcTool.startServer();
		}  catch(Exception e) {
			e.printStackTrace();
		}
	}
		
	public void setPathToVeriBLEApp(String path) {
		mPathToVeriBLEApp = path;
	}
	
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

	protected Shimmer4sdk createNewShimmer4() {
		return new Shimmer4sdk();
	}
	
	@Override
	protected Shimmer4sdk createNewShimmer4(String comPort, String bluetoothAddress) {
		Shimmer4sdk shimmer4 = createNewShimmer4();
		shimmer4.setComPort(comPort);
		putShimmerGlobalMap(bluetoothAddress, shimmer4);
		return shimmer4;
	}

	@Override
	protected Shimmer4sdk createNewShimmer4(ShimmerRadioInitializer radioInitializer, String bluetoothAddress) {
    	SerialPortCommJssc serialPortComm = (SerialPortCommJssc) radioInitializer.getSerialCommPort();
    	String comPort = serialPortComm.mComPort;

		Shimmer4sdk shimmer4 = createNewShimmer4(comPort, bluetoothAddress);
		if(serialPortComm!=null){
			CommsProtocolRadio commsProtocolRadio = new CommsProtocolRadio(serialPortComm, new LiteProtocol(comPort));
			shimmer4.setCommsProtocolRadio(commsProtocolRadio);
		}

    	return shimmer4;
    }
	
	@Override
	public void connectVerisenseDevice(BluetoothDeviceDetails bdd) {
		VerisenseDevice verisenseDevice;
		
		if(!verisenseMacIdList.contains(bdd.mShimmerMacId)) {
			//BleRadioByteCommunication radio1 = new BleRadioByteCommunication(bdd, "bleconsoleapp\\BLEConsoleApp1.exe");
			
			GrpcBLERadioByteCommunication radio1 = new GrpcBLERadioByteCommunication(bdd,"localhost",mGRPCPort);
			VerisenseProtocolByteCommunication protocol1 = new VerisenseProtocolByteCommunication(radio1);
			verisenseDevice = new VerisenseDevice();
			verisenseDevice.setShimmerUserAssignedName(bdd.mFriendlyName);
			verisenseDevice.setMacIdFromUart(bdd.mShimmerMacId);
			verisenseDevice.setProtocol(COMMUNICATION_TYPE.BLUETOOTH, protocol1);
			initializeNewShimmerCommon(verisenseDevice);
			
			verisenseDeviceList.add(verisenseDevice);
			verisenseMacIdList.add(bdd.mShimmerMacId);
	    }
		else {
			verisenseDevice = verisenseDeviceList.get(verisenseMacIdList.indexOf(bdd.mShimmerMacId));
		}

		try {
			if(verisenseDevice.getBluetoothRadioState() == BT_STATE.CONNECTED || verisenseDevice.getBluetoothRadioState() == BT_STATE.STREAMING || verisenseDevice.getBluetoothRadioState() == BT_STATE.STREAMING_LOGGED_DATA) {
				throw new ShimmerException("Device is already connected");
			}
			verisenseDevice.connect();
		} catch (ShimmerException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void connectShimmer3BleGrpc(BluetoothDeviceDetails bdd) {
		ShimmerGRPC shimmer;
		
		if(!shimmer3BleMacIdList.contains(bdd.mShimmerMacId)) {
			
			shimmer = new ShimmerGRPC(bdd.mShimmerMacId.replace(":", ""),"localhost",mGRPCPort);
			shimmer.setShimmerUserAssignedName(bdd.mFriendlyName);
			shimmer.setMacIdFromUart(bdd.mShimmerMacId);
			initializeNewShimmerCommon(shimmer);
			
			shimmer3BleDeviceList.add(shimmer);
			shimmer3BleMacIdList.add(bdd.mShimmerMacId);
	    }
		else {
			shimmer = shimmer3BleDeviceList.get(shimmer3BleMacIdList.indexOf(bdd.mShimmerMacId));
		}

		try {
			if(shimmer.getBluetoothRadioState() == BT_STATE.CONNECTED || shimmer.getBluetoothRadioState() == BT_STATE.STREAMING || shimmer.getBluetoothRadioState() == BT_STATE.STREAMING_LOGGED_DATA) {
				throw new ShimmerException("Device is already connected");
			}
			shimmer.connect("","");
		} catch (ShimmerException e) {
			e.printStackTrace();
		}
	}
	@Override
	public void connectShimmerThroughCommPort(String comPort){
		directConnectUnknownShimmer=true;
		
		if (mMapOfBtConnectedShimmers.containsKey(comPort)){
			if(!mMapOfBtConnectedShimmers.get(comPort).isConnected()) {
				mMapOfBtConnectedShimmers.remove(comPort);
			} 
		}

		super.setConnectionExceptionListener(new ConnectionExceptionListener() {

			@Override
			public void onConnectionStart(String connectionHandle) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onConnectionException(Exception exception) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onConnectStartException(String connectionHandle) {
				// TODO Auto-generated method stub
				CallbackObject cbo = new CallbackObject(ShimmerBluetooth.NOTIFICATION_SHIMMER_STATE_CHANGE, BT_STATE.DISCONNECTED, "", connectionHandle);
				callBackObject.sendCallBackMsg(ShimmerBluetooth.MSG_IDENTIFIER_STATE_CHANGE, cbo);
				
			}});
		
		ConnectThread connectThread = new ConnectThread(comPort, null, null);
		connectThread.start();
		
	}

	@Override
	public void putShimmerGlobalMap(String bluetoothAddress, ShimmerDevice shimmerDevice) {
		// TODO Auto-generated method stub
		
	}
	
	protected void setupShimmer3BluetoothForBtManager(ShimmerDevice shimmerDevice) {
		((ShimmerPC)shimmerDevice).setUseInfoMemConfigMethod(USE_INFOMEM_CONFIG_METHOD);
    	((ShimmerPC)shimmerDevice).enableCheckifAlive(true);
	}
	


	//-------------- Callback methods start -----------------------------
	
	public BasicProcessWithCallBack callBackObject = new BasicProcessWithCallBack() {
		
		@Override
		protected void processMsgFromCallback(ShimmerMsg shimmerMSG) {
			sendCallBackMsg(shimmerMSG);
		}
	};
	
	
}






