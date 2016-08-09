package com.shimmerresearch.pcSerialPort;

import com.shimmerresearch.driverUtilities.ShimmerVerObject;

public abstract class ShimmerRadioInitializer extends SerialPortCommJssc {

	public abstract ShimmerVerObject getShimmerVerObject();
	
	public ShimmerRadioInitializer(String comPort, String bluetoothAddress, int baudToUse) {
		super(comPort, bluetoothAddress, baudToUse);
	}

}
