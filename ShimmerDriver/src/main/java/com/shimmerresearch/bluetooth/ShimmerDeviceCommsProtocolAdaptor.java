package com.shimmerresearch.bluetooth;

import com.shimmerresearch.bluetooth.ShimmerBluetooth.BT_STATE;
import com.shimmerresearch.comms.radioProtocol.CommsProtocolRadio;
import com.shimmerresearch.comms.radioProtocol.LiteProtocol;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.exceptions.ShimmerException;

/**
 * Still in development. Trying to figure out the best way to share common code
 * between certain devices that support this connection approach.
 * 
 * @author Mark Nolan
 *
 */
public class ShimmerDeviceCommsProtocolAdaptor {

	private ShimmerDevice mShimmerDevice = null;
	
	public ShimmerDeviceCommsProtocolAdaptor(ShimmerDevice shimmerDevice){
		mShimmerDevice = shimmerDevice;
	}
	
	public void connect() throws ShimmerException {
//		clearShimmerVersionObject();
		
		mShimmerDevice.setBluetoothRadioState(BT_STATE.CONNECTING);
		if(mShimmerDevice.mCommsProtocolRadio!=null){
			try {
				mShimmerDevice.mCommsProtocolRadio.connect();
			} catch (ShimmerException dE) {
				mShimmerDevice.consolePrintLn("Failed to Connect");
				mShimmerDevice.consolePrintLn(dE.getErrStringFormatted());
				
				mShimmerDevice.disconnect();
				mShimmerDevice.setBluetoothRadioState(BT_STATE.CONNECTION_FAILED);
				throw(dE);
			}
		}
	}

	public void setIsInitialised(boolean state) {
		CommsProtocolRadio commsProtocolRadio = mShimmerDevice.mCommsProtocolRadio;
		if(commsProtocolRadio!=null && commsProtocolRadio.mRadioProtocol!=null && commsProtocolRadio.mRadioProtocol instanceof LiteProtocol){
			((LiteProtocol)(commsProtocolRadio.mRadioProtocol)).mIsInitialised = state;
		}
	}

	public void setIsSensing(boolean state) {
		CommsProtocolRadio commsProtocolRadio = mShimmerDevice.mCommsProtocolRadio;
		if(commsProtocolRadio!=null && commsProtocolRadio.mRadioProtocol!=null && commsProtocolRadio.mRadioProtocol instanceof LiteProtocol){
			((LiteProtocol)(commsProtocolRadio.mRadioProtocol)).mIsSensing = state;
		}
	}

	public void setIsStreaming(boolean state) {
		CommsProtocolRadio commsProtocolRadio = mShimmerDevice.mCommsProtocolRadio;
		if(commsProtocolRadio!=null && commsProtocolRadio.mRadioProtocol!=null && commsProtocolRadio.mRadioProtocol instanceof LiteProtocol){
			((LiteProtocol)(commsProtocolRadio.mRadioProtocol)).mIsStreaming = state;
		}
	}

	public void setHaveAttemptedToReadConfig(boolean state) {
		CommsProtocolRadio commsProtocolRadio = mShimmerDevice.mCommsProtocolRadio;
		if(commsProtocolRadio!=null && commsProtocolRadio.mRadioProtocol!=null && commsProtocolRadio.mRadioProtocol instanceof LiteProtocol){
			((LiteProtocol)(commsProtocolRadio.mRadioProtocol)).mHaveAttemptedToReadConfig = state;
		}
	}

	public void setIsSDLogging(boolean state) {
		CommsProtocolRadio commsProtocolRadio = mShimmerDevice.mCommsProtocolRadio;
		if(commsProtocolRadio!=null && commsProtocolRadio.mRadioProtocol!=null && commsProtocolRadio.mRadioProtocol instanceof LiteProtocol){
			((LiteProtocol)(commsProtocolRadio.mRadioProtocol)).mIsSDLogging = state;
		}
	}

	public void setIsDocked(boolean state) {
		CommsProtocolRadio commsProtocolRadio = mShimmerDevice.mCommsProtocolRadio;
		if(commsProtocolRadio!=null && commsProtocolRadio.mRadioProtocol!=null && commsProtocolRadio.mRadioProtocol instanceof LiteProtocol){
			((LiteProtocol)(commsProtocolRadio.mRadioProtocol)).mIsDocked = state;
		}
	}

	public boolean isConnected() {
		boolean isConnected = false;
		CommsProtocolRadio commsProtocolRadio = mShimmerDevice.mCommsProtocolRadio;
		if(commsProtocolRadio!=null && commsProtocolRadio.isConnected()){
			isConnected = true;
		}
		return isConnected;
	}

}
