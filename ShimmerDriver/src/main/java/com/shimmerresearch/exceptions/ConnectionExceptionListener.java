package com.shimmerresearch.exceptions;

import com.shimmerresearch.driverUtilities.BluetoothDeviceDetails;

public interface ConnectionExceptionListener {

	public void onConnectionException(Exception exception);

	public void onConnectionStart(BluetoothDeviceDetails portDetails);
}
