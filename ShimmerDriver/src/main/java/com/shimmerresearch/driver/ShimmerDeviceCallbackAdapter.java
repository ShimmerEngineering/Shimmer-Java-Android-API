package com.shimmerresearch.driver;

import java.io.Serializable;

import com.shimmerresearch.bluetooth.ShimmerBluetooth;
import com.shimmerresearch.bluetooth.ShimmerBluetooth.BT_STATE;
import com.shimmerresearch.exceptions.ShimmerException;

/**
 * @author Mark Nolan
 * 
 * TODO migrate common callback code from Shimmer4/Sweatch/ArduinoDevice etc. to here
 *
 */
public class ShimmerDeviceCallbackAdapter implements Serializable {

	private static final long serialVersionUID = -3826489309767259792L;
	
	//TODO needs testing - only send update if there is a change to reduce callbacks
	public static final boolean ONLY_UPDATE_RATE_IF_CHANGED = false;

	private ShimmerDevice shimmerDevice = null;
	
	public double mLastSentPacketReceptionRateOverall = ShimmerDevice.DEFAULT_RECEPTION_RATE;
	public double mLastSentPacketReceptionRateCurrent = ShimmerDevice.DEFAULT_RECEPTION_RATE;
	
	public ShimmerDeviceCallbackAdapter(ShimmerDevice shimmerDevice){
		this.shimmerDevice = shimmerDevice;
	}
	
	//TODO neaten below. Copy/Paste from ShimmerBluetooth
	public void setBluetoothRadioState(BT_STATE state) {
//		super.setBluetoothRadioState(state);
		
		BT_STATE btState = shimmerDevice.getBluetoothRadioState();
		
		if(btState==BT_STATE.CONNECTED
				|| btState==BT_STATE.STREAMING){
//				|| btState==BT_STATE.RECORDING){
			shimmerDevice.setIsConnected(true);
			shimmerDevice.setIsInitialised(true);
			
			shimmerDevice.setIsStreaming(false);
			if(btState==BT_STATE.STREAMING){
//					|| btState==BT_STATE.RECORDING){
				shimmerDevice.setIsStreaming(true);
			}
			
//			if(btState==BT_STATE.RECORDING){
//				
//			}
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
		//TODO, don't do this every data packet 
		sendCallbackPacketReceptionRateOverall();
		
		shimmerDevice.sendCallBackMsg(ShimmerBluetooth.MSG_IDENTIFIER_DATA_PACKET, ojc);
	}
	
	public void sendCallbackPacketReceptionRateOverall() {
		double packetReceptionRateOverall = shimmerDevice.getPacketReceptionRateOverall();
		boolean sendUpdate = true;
		if(ONLY_UPDATE_RATE_IF_CHANGED && mLastSentPacketReceptionRateOverall==packetReceptionRateOverall){
			sendUpdate = false;
		}
		if(sendUpdate) {
			sendCallBackMsgWithSameId(new CallbackObject(ShimmerBluetooth.MSG_IDENTIFIER_PACKET_RECEPTION_RATE_OVERALL, shimmerDevice.getMacId(), shimmerDevice.getComPort(), packetReceptionRateOverall));
		}
		mLastSentPacketReceptionRateOverall = packetReceptionRateOverall;
	}

	public void sendCallbackPacketReceptionRateCurrent() {
		double packetReceptionRateCurrent = shimmerDevice.getPacketReceptionRateCurrent();
		boolean sendUpdate = true;
		if(ONLY_UPDATE_RATE_IF_CHANGED && mLastSentPacketReceptionRateCurrent==packetReceptionRateCurrent){
			sendUpdate = false;
		}
		if(sendUpdate) {
			sendCallBackMsgWithSameId(new CallbackObject(ShimmerBluetooth.MSG_IDENTIFIER_PACKET_RECEPTION_RATE_CURRENT, shimmerDevice.getMacId(), shimmerDevice.getComPort(), packetReceptionRateCurrent));
		}
		mLastSentPacketReceptionRateCurrent = packetReceptionRateCurrent;
	}
	
	public void sendCallBackMsgWithSameId(CallbackObject callBackObject) {
		shimmerDevice.sendCallBackMsg(callBackObject.mIndicator, callBackObject);
	}

	public void sendCallBackDeviceException(ShimmerException dE) {
		shimmerDevice.sendCallBackMsg(ShimmerBluetooth.MSG_IDENTIFIER_DEVICE_ERROR, dE);
	}


}
