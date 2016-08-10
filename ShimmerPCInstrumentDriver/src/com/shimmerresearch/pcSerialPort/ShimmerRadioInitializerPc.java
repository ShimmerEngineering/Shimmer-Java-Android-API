package com.shimmerresearch.pcSerialPort;

import com.shimmerresearch.bluetooth.ShimmerRadioInitializer;
import com.shimmerresearch.comms.serialPortInterface.AbstractSerialPortComm;

public class ShimmerRadioInitializerPc extends ShimmerRadioInitializer {
	
	public ShimmerRadioInitializerPc(String comPort, int baudToUse) {
		this.serialCommPort = new SerialPortCommJssc(comPort, comPort, baudToUse);
	}
	
	public ShimmerRadioInitializerPc(String comPort, String bluetoothAddress, int baudToUse) {
		this.serialCommPort = new SerialPortCommJssc(comPort, bluetoothAddress, baudToUse);
		this.serialCommPort.SERIAL_PORT_TIMEOUT = 2000; // was 2000
	}
	
	@Override
	public AbstractSerialPortComm getSerialCommPort() {
		return this.serialCommPort;
	}

}