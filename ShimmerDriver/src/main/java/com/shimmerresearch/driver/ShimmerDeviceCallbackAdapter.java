package com.shimmerresearch.driver;

import com.shimmerresearch.bluetooth.ShimmerBluetooth;
import com.shimmerresearch.bluetooth.ShimmerBluetooth.BT_STATE;

public class ShimmerDeviceCallbackAdapter {

	private ShimmerDevice shimmerDevice = null;
	
	public ShimmerDeviceCallbackAdapter(ShimmerDevice shimmerDevice){
		this.shimmerDevice = shimmerDevice;
	}
	
	//TODO neaten below. Copy/Paste from ShimmerBluetooth
	public void setBluetoothRadioState(BT_STATE state) {
//		super.setBluetoothRadioState(state);
		
		BT_STATE btState = shimmerDevice.getBluetoothRadioState();
		
		if(btState==BT_STATE.CONNECTED){
			shimmerDevice.setIsConnected(true);
			shimmerDevice.setIsInitialised(true);
			shimmerDevice.setIsStreaming(false);
		}
		else if(btState==BT_STATE.STREAMING){
			shimmerDevice.setIsStreaming(true);
		}		
		else if((btState==BT_STATE.DISCONNECTED)
				||(btState==BT_STATE.CONNECTION_LOST)
				||(btState==BT_STATE.CONNECTION_FAILED)){
			shimmerDevice.setIsConnected(false);
			shimmerDevice.setIsInitialised(false);
			shimmerDevice.setIsStreaming(false);
		}
		
		CallbackObject callBackObject = new CallbackObject(ShimmerBluetooth.NOTIFICATION_SHIMMER_STATE_CHANGE, state, shimmerDevice.getMacIdFromUart(), shimmerDevice.getComPort());
		shimmerDevice.sendCallBackMsg(ShimmerBluetooth.MSG_IDENTIFIER_STATE_CHANGE, callBackObject);
	}
	
	//TODO neaten below. Copy/Paste from Shimmer4
	public void isReadyForStreaming(){
		shimmerDevice.setIsInitialised(true);

		BT_STATE btState = shimmerDevice.getBluetoothRadioState();

		CallbackObject callBackObject = new CallbackObject(ShimmerBluetooth.NOTIFICATION_SHIMMER_FULLY_INITIALIZED, shimmerDevice.getMacIdFromUart(), shimmerDevice.getComPort());
		shimmerDevice.sendCallBackMsg(ShimmerBluetooth.MSG_IDENTIFIER_NOTIFICATION_MESSAGE, callBackObject);
		
		if (btState==BT_STATE.CONNECTING){
			shimmerDevice.setBluetoothRadioState(BT_STATE.CONNECTED);
		}
	}
	
	//TODO neaten below. Copy/Paste from Shimmer4
	public void dataHandler(ObjectCluster ojc) {
		shimmerDevice.sendCallBackMsg(ShimmerBluetooth.MSG_IDENTIFIER_DATA_PACKET, ojc);
	}
}
