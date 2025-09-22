package com.shimmerresearch.driver;

import java.io.Serializable;

import com.shimmerresearch.bluetooth.BluetoothProgressReportPerCmd;
import com.shimmerresearch.bluetooth.BluetoothProgressReportPerDevice;
import com.shimmerresearch.bluetooth.ShimmerBluetooth;
import com.shimmerresearch.bluetooth.ShimmerBluetooth.BT_STATE;
import com.shimmerresearch.exceptions.ShimmerException;
import com.shimmerresearch.verisense.communication.SyncProgressDetails;

/**
 * Still in development. Trying to figure out the best way to share common code
 * between certain devices that support this connection approach.
 * 
 * @author Mark Nolan
 *
 */
public class ShimmerDeviceCallbackAdapter implements Serializable {

	private static final long serialVersionUID = -3826489309767259792L;
	
	//TODO needs testing/development - when true, sometimes the first reception value is isn't being sent to the GUI 
	//only send update if there is a change to reduce callbacks
	public static final boolean ONLY_UPDATE_RATE_IF_CHANGED = false;

	private ShimmerDevice mShimmerDevice = null;
	
	private BluetoothProgressReportPerDevice progressReportPerDevice;
	
	public double mLastSentPacketReceptionRateOverall = ShimmerDevice.DEFAULT_RECEPTION_RATE;
	public double mLastSentPacketReceptionRateCurrent = ShimmerDevice.DEFAULT_RECEPTION_RATE;

	private boolean useUniqueIdForFeedback = false;
	
	public ShimmerDeviceCallbackAdapter(ShimmerDevice shimmerDevice){
		this.mShimmerDevice = shimmerDevice;
	}
	
	public ShimmerDeviceCallbackAdapter(ShimmerDevice shimmerDevice, boolean useUniqueIdForFeedback) {
		this(shimmerDevice);
		this.useUniqueIdForFeedback = useUniqueIdForFeedback;
	}
	
	public void setBluetoothRadioState(BT_STATE connectionState, boolean isChanged) {
//		boolean isChanged = shimmerDevice.setBluetoothRadioState(connectionState);
		
		if(connectionState==BT_STATE.CONNECTED
				|| connectionState==BT_STATE.STREAMING){
//				|| connectionState==BT_STATE.RECORDING){
			mShimmerDevice.setIsConnected(true);
			mShimmerDevice.setIsInitialised(true);
			
			if(connectionState==BT_STATE.STREAMING){
//					|| connectionState==BT_STATE.RECORDING){
				mShimmerDevice.setIsStreaming(true);
			} else {
				mShimmerDevice.setIsStreaming(false);
			}
			
//			if(btState==BT_STATE.RECORDING){
//				
//			}
		}
		else if((connectionState==BT_STATE.DISCONNECTED)
				||(connectionState==BT_STATE.CONNECTION_LOST)
				||(connectionState==BT_STATE.CONNECTION_FAILED)){
			mShimmerDevice.setIsConnected(false);
			mShimmerDevice.setIsInitialised(false);
			mShimmerDevice.setIsStreaming(false);
		}
		
		mShimmerDevice.consolePrintLn("State change: " + connectionState.toString());
		
		if(isChanged) {
			CallbackObject callBackObject = new CallbackObject(ShimmerBluetooth.NOTIFICATION_SHIMMER_STATE_CHANGE, connectionState, getMacId(), getComPort());
			mShimmerDevice.sendCallBackMsg(ShimmerBluetooth.MSG_IDENTIFIER_STATE_CHANGE, callBackObject);
		}
		
//		return changed;
	}
	
	public void isReadyForStreaming(){
		mShimmerDevice.setIsInitialised(true);

		BT_STATE btState = mShimmerDevice.getBluetoothRadioState();

		CallbackObject callBackObject = new CallbackObject(ShimmerBluetooth.NOTIFICATION_SHIMMER_FULLY_INITIALIZED, getMacId(), getComPort());
		mShimmerDevice.sendCallBackMsg(ShimmerBluetooth.MSG_IDENTIFIER_NOTIFICATION_MESSAGE, callBackObject);
		
		if (btState==BT_STATE.CONNECTING){
			mShimmerDevice.setBluetoothRadioState(BT_STATE.CONNECTED);
		}
		
		if(mShimmerDevice.isAutoStartStreaming()) {
			try {
				mShimmerDevice.startStreaming();
			} catch (ShimmerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void newSyncPayloadReceived(int payloadIndex, boolean crcError, double transferRateBytes, String binFilePath) {
		CallbackObject callBackObject = new CallbackObject(getMacId(), getComPort(),new SyncProgressDetails(payloadIndex,crcError, transferRateBytes, binFilePath));
		mShimmerDevice.sendCallBackMsg(ShimmerBluetooth.MSG_IDENTIFIER_SYNC_PROGRESS, callBackObject);
	}
	
	public void readLoggedDataCompleted(String binFilePath) {
		CallbackObject callBackObject = new CallbackObject(getMacId(), getComPort(), new SyncProgressDetails(binFilePath));
		mShimmerDevice.sendCallBackMsg(ShimmerBluetooth.MSG_IDENTIFIER_SYNC_COMPLETED, callBackObject);
	}
	
	public void eraseDataCompleted() {
		CallbackObject callBackObject = new CallbackObject(getMacId(), getComPort(), true);
		mShimmerDevice.sendCallBackMsg(ShimmerBluetooth.MSG_IDENTIFIER_VERISENSE_ERASE_DATA_COMPLETED, callBackObject);
	}
	
	public void writeOpConfigCompleted() {
		CallbackObject callBackObject = new CallbackObject(getMacId(), getComPort(), true);
		mShimmerDevice.sendCallBackMsg(ShimmerBluetooth.MSG_IDENTIFIER_VERISENSE_WRITE_OPCONFIG_COMPLETED, callBackObject);
	}
	
	public void hasStopStreaming() {
		// Send a notification msg to the UI through a callback (use a msg identifier notification message)
		CallbackObject callBackObject = new CallbackObject(ShimmerBluetooth.NOTIFICATION_SHIMMER_STOP_STREAMING, getMacId(), getComPort());
		mShimmerDevice.sendCallBackMsg(ShimmerBluetooth.MSG_IDENTIFIER_NOTIFICATION_MESSAGE, callBackObject);
		if (mShimmerDevice.isSDLogging()) {
			mShimmerDevice.setBluetoothRadioState(BT_STATE.SDLOGGING);
		} else {
			mShimmerDevice.setBluetoothRadioState(BT_STATE.CONNECTED);
		}
	}
	
	public void startStreaming() {
		mLastSentPacketReceptionRateOverall = ShimmerDevice.DEFAULT_RECEPTION_RATE;
		mLastSentPacketReceptionRateCurrent = ShimmerDevice.DEFAULT_RECEPTION_RATE;
	}

	public void isNowStreaming() {
		// Send a notification msg to the UI through a callback (use a msg identifier notification message)
		CallbackObject callBackObject = new CallbackObject(ShimmerBluetooth.NOTIFICATION_SHIMMER_START_STREAMING, getMacId(), getComPort());
		mShimmerDevice.sendCallBackMsg(ShimmerBluetooth.MSG_IDENTIFIER_NOTIFICATION_MESSAGE, callBackObject);
		
		//shimmerDevice.setBluetoothRadioState(BT_STATE.STREAMING);
		if (mShimmerDevice.isSDLogging()){
			mShimmerDevice.setBluetoothRadioState(BT_STATE.STREAMING_AND_SDLOGGING);
		} else {
			mShimmerDevice.setBluetoothRadioState(BT_STATE.STREAMING);
		}
	}
	
	public void dockedStateChange() {
		CallbackObject callBackObject = new CallbackObject(ShimmerBluetooth.MSG_IDENTIFIER_SHIMMER_DOCKED_STATE_CHANGE, getMacId(), getComPort());
		mShimmerDevice.sendCallBackMsg(ShimmerBluetooth.MSG_IDENTIFIER_SHIMMER_DOCKED_STATE_CHANGE, callBackObject);
	}

	
	//Is this needed? just go straight to isReadyForStreaming()?
	public void inquiryDone() {
		CallbackObject callBackObject = new CallbackObject(ShimmerBluetooth.NOTIFICATION_SHIMMER_STATE_CHANGE, mShimmerDevice.getBluetoothRadioState(), getMacId(), getComPort());
		mShimmerDevice.sendCallBackMsg(ShimmerBluetooth.MSG_IDENTIFIER_STATE_CHANGE, callBackObject);
//		mShimmerDevice.isReadyForStreaming();
	}

	public void batteryStatusChanged() {
		CallbackObject callBackObject = new CallbackObject(ShimmerBluetooth.NOTIFICATION_SHIMMER_STATE_CHANGE, mShimmerDevice.getBluetoothRadioState(), getMacId(), getComPort());
		mShimmerDevice.sendCallBackMsg(ShimmerBluetooth.MSG_IDENTIFIER_STATE_CHANGE, callBackObject);
	}

	
	public void dataHandler(ObjectCluster ojc) {
		//TODO, don't do this every data packet 
		sendCallbackPacketReceptionRateOverall();
		
		mShimmerDevice.sendCallBackMsg(ShimmerBluetooth.MSG_IDENTIFIER_DATA_PACKET, ojc);
	}
	
	public void sendCallbackPacketReceptionRateOverall() {
		double packetReceptionRateOverall = mShimmerDevice.getPacketReceptionRateOverall();
		boolean sendUpdate = true;
		if(ONLY_UPDATE_RATE_IF_CHANGED && mLastSentPacketReceptionRateOverall==packetReceptionRateOverall){
			sendUpdate = false;
		}
		if(sendUpdate) {
			sendCallBackMsgWithSameId(new CallbackObject(ShimmerBluetooth.MSG_IDENTIFIER_PACKET_RECEPTION_RATE_OVERALL, getMacId(), getComPort(), packetReceptionRateOverall));
		}
		mLastSentPacketReceptionRateOverall = packetReceptionRateOverall;
	}

	public void sendCallbackPacketReceptionRateCurrent() {
		double packetReceptionRateCurrent = mShimmerDevice.getPacketReceptionRateCurrent();
		boolean sendUpdate = true;
		if(ONLY_UPDATE_RATE_IF_CHANGED && mLastSentPacketReceptionRateCurrent==packetReceptionRateCurrent){
			sendUpdate = false;
		}
		if(sendUpdate) {
			sendCallBackMsgWithSameId(new CallbackObject(ShimmerBluetooth.MSG_IDENTIFIER_PACKET_RECEPTION_RATE_CURRENT, getMacId(), getComPort(), packetReceptionRateCurrent));
		}
		mLastSentPacketReceptionRateCurrent = packetReceptionRateCurrent;
	}
	
	public void sendCallBackMsgWithSameId(CallbackObject callBackObject) {
		mShimmerDevice.sendCallBackMsg(callBackObject.mIndicator, callBackObject);
	}

	public void sendCallBackDeviceException(ShimmerException dE) {
		mShimmerDevice.sendCallBackMsg(ShimmerBluetooth.MSG_IDENTIFIER_DEVICE_ERROR, dE);
	}
	
	public void sendProgressReport(BluetoothProgressReportPerCmd pRPC) {
		if(progressReportPerDevice!=null){
			progressReportPerDevice.updateProgress(pRPC);
			//int progress = progressReportPerDevice.mProgressPercentageComplete;
			
			sendNewCallbackObjectProgressPerDevice(progressReportPerDevice);
			
			mShimmerDevice.consolePrintLn("MAC:\t" + mShimmerDevice.getMacId()
					+ "\tCOM:\t" + mShimmerDevice.getComPort()
					+ "\tProgressCounter" + progressReportPerDevice.mProgressCounter 
					+ "\tProgressEndValue " + progressReportPerDevice.mProgressEndValue);
			
			// From Shimmer4/SweatchDevice/Arduino but shouldn't be needed here as it's a bit of a hack
//			if(progressReportPerDevice.mCurrentOperationBtState==BT_STATE.CONNECTING){
//				if(progressReportPerDevice.mProgressCounter==progressReportPerDevice.mProgressEndValue){
//					isReadyForStreaming();
//				}
//			}
			
			//From ShimmerPC
			//TODO 2018-02-26 MN: does this need to be accounted for in other devices or is it needed at all here
			if(mShimmerDevice instanceof ShimmerBluetooth) {
				if(progressReportPerDevice.mProgressCounter==progressReportPerDevice.mProgressEndValue){
					finishOperation(progressReportPerDevice.mCurrentOperationBtState);
				}
			}

		}
	}

	public void startOperation(BT_STATE currentOperation, int totalNumOfCmds) {
		mShimmerDevice.consolePrintLn(currentOperation + " START");

		progressReportPerDevice = new BluetoothProgressReportPerDevice(mShimmerDevice, currentOperation, totalNumOfCmds);
		progressReportPerDevice.mOperationState = BluetoothProgressReportPerDevice.OperationState.INPROGRESS;
		
		sendNewCallbackObjectProgressPerDevice(progressReportPerDevice);
	}

	public void finishOperation(BT_STATE btState) {
		if(progressReportPerDevice!=null){
			mShimmerDevice.consolePrintLn("CURRENT OPERATION " + progressReportPerDevice.mCurrentOperationBtState + "\tFINISHED:" + btState);
			
			if(progressReportPerDevice.mCurrentOperationBtState == btState){

				progressReportPerDevice.finishOperation();
				progressReportPerDevice.mOperationState = BluetoothProgressReportPerDevice.OperationState.SUCCESS;
				//JC: moved operationFinished to is ready for streaming, seems to be called before the inquiry response is received
				//TODO 2018-02-26 MN: does this need to be accounted for in other devices or is it needed at all here
				if(mShimmerDevice instanceof ShimmerBluetooth) {
					ShimmerBluetooth shimmerBluetooth = (ShimmerBluetooth) mShimmerDevice;
					shimmerBluetooth.operationFinished();
				}
				sendNewCallbackObjectProgressPerDevice(progressReportPerDevice);
				progressReportPerDevice = null;
			}
		}
		else {
			mShimmerDevice.consolePrintLn("CURRENT OPERATION - UNKNOWN, null progressReportPerDevice" + "\tFINISHED:" + btState);
		}
	}

	private void sendNewCallbackObjectProgressPerDevice(BluetoothProgressReportPerDevice progressReportPerDevice){
		CallbackObject callBackObject = new CallbackObject(mShimmerDevice.getBluetoothRadioState(), getMacId(), getComPort(), progressReportPerDevice);
		mShimmerDevice.sendCallBackMsg(ShimmerBluetooth.MSG_IDENTIFIER_PROGRESS_REPORT_PER_DEVICE, callBackObject);
	}
	
	
	private String getMacId(){
		if(useUniqueIdForFeedback) {
			return mShimmerDevice.getUniqueId();
		} else {
			return mShimmerDevice.getMacId();
		}
	}

	private String getComPort(){
		if(useUniqueIdForFeedback) {
			return mShimmerDevice.getUniqueId();
		} else {
			return mShimmerDevice.getComPort();
		}
	}


	
}
