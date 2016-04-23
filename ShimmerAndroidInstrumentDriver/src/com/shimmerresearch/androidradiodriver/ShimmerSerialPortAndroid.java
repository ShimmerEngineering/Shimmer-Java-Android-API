package com.shimmerresearch.androidradiodriver;

import com.shimmerresearch.comms.serialPortInterface.ShimmerSerialEventCallback;
import com.shimmerresearch.comms.serialPortInterface.ShimmerSerialPortInterface;
import com.shimmerresearch.driver.DeviceException;

public class ShimmerSerialPortAndroid implements ShimmerSerialPortInterface {

	@Override
	public void connect() throws DeviceException {
		// TODO Auto-generated method stub

	}

	@Override
	public void disconnect() throws DeviceException {
		// TODO Auto-generated method stub

	}

	@Override
	public void closeSafely() throws DeviceException {
		// TODO Auto-generated method stub

	}

	@Override
	public void clearSerialPortRxBuffer() throws DeviceException {
		// TODO Auto-generated method stub

	}

	@Override
	public void txBytes(byte[] buf) throws DeviceException {
		// TODO Auto-generated method stub

	}

	@Override
	public byte[] rxBytes(int numBytes) throws DeviceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void registerSerialPortRxEventCallback(
			ShimmerSerialEventCallback shimmerSerialEventCallback) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isSerialPortReaderStarted() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setVerboseMode(boolean verboseMode, boolean isDebugMode) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean bytesAvailableToBeRead() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int availableBytes() {
		// TODO Auto-generated method stub
		return 0;
	}

}
