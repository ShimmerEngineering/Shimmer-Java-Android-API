package com.shimmerresearch.tools.bluetooth;

import com.shimmerresearch.bluetooth.ShimmerRadioInitializer;
import com.shimmerresearch.comms.serialPortInterface.AbstractSerialPortHal;
import com.shimmerresearch.driver.BasicProcessWithCallBack;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driver.shimmer4sdk.Shimmer4;
import com.shimmerresearch.exceptions.ShimmerException;
import com.shimmerresearch.managers.bluetoothManager.ShimmerBluetoothManager;

public class BasicShimmerBluetoothManagerPc extends ShimmerBluetoothManager {

	@Override
	protected void loadBtShimmers(Object... params) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addCallBack(BasicProcessWithCallBack basicProcess) {
		// TODO Auto-generated method stub
		
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
	public void putShimmerGlobalMap(String bluetoothAddress, ShimmerDevice shimmerDevice) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected AbstractSerialPortHal createNewSerialPortComm(String comPort, String bluetoothAddress) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void connectExistingShimmer(Object... params) throws ShimmerException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected ShimmerDevice createNewShimmer3(String comPort, String bluetoothAddress) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected ShimmerDevice createNewShimmer3(ShimmerRadioInitializer bldc, String bluetoothAddress) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Shimmer4 createNewShimmer4(String comPort, String bluetoothAddress) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Shimmer4 createNewShimmer4(ShimmerRadioInitializer radioInitializer, String bluetoothAddress) {
		// TODO Auto-generated method stub
		return null;
	}}





