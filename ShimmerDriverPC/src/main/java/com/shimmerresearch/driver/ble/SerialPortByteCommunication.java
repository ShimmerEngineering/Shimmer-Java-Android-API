package com.shimmerresearch.driver.ble;

import com.shimmerresearch.comms.serialPortInterface.AbstractSerialPortHal;
import com.shimmerresearch.exceptions.ShimmerException;
import com.shimmerresearch.verisense.communication.AbstractByteCommunication;

public class SerialPortByteCommunication extends AbstractByteCommunication {

	AbstractSerialPortHal abstractSerialPortHal;
	
	public SerialPortByteCommunication(AbstractSerialPortHal abstractSerialPortHal) {
		this.abstractSerialPortHal = abstractSerialPortHal;
	}

	@Override
	public void connect() {
		try {
			abstractSerialPortHal.connect();
		} catch (ShimmerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void disconnect() {
		try {
			abstractSerialPortHal.disconnect();
		} catch (ShimmerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void writeBytes(byte[] bytes) {
		try {
			abstractSerialPortHal.txBytes(bytes);
		} catch (ShimmerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		
	}
	

}
