package com.shimmerresearch.bluetooth;

import com.shimmerresearch.bluetooth.ShimmerBluetooth.BT_STATE;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.exceptions.ShimmerException;

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

}
