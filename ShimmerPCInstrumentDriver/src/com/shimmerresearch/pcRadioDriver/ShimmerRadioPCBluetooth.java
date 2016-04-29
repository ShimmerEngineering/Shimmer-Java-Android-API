package com.shimmerresearch.pcRadioDriver;

//import jssc.SerialPort;
import jssc.SerialPortException;

import com.shimmerresearch.bluetooth.ProgressReportPerCmd;
import com.shimmerresearch.bluetooth.ShimmerBluetooth.BT_STATE;
import com.shimmerresearch.bluetooth.ShimmerBluetooth.IOThread;
import com.shimmerresearch.bluetooth.ShimmerBluetooth.ProcessingThread;
import com.shimmerresearch.comms.serialPortInterface.ShimmerSerialEventCallback;
import com.shimmerresearch.comms.serialPortInterface.ByteLevelDataComm;
import com.shimmerresearch.driver.DeviceException;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.ShimmerMsg;
import com.shimmerresearch.sensor.ActionSetting;

public class ShimmerRadioPCBluetooth extends ShimmerRadioProtocol implements ShimmerSerialEventCallback {

	@Override
	public void serialPortRxEvent(int byteLength) {
		// TODO Auto-generated method stub
		
	}}
