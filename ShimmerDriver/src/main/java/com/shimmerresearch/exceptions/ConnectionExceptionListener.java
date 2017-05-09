package com.shimmerresearch.exceptions;

import com.shimmerresearch.driverUtilities.BluetoothDeviceDetails;

public interface ConnectionExceptionListener {
	
	public void onConnectionStart(String connectionHandle);

	public void onConnectionException(Exception exception);
	
	public void onConnectStartException(String connectionHandle);

}
