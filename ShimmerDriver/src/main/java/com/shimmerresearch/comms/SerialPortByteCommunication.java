package com.shimmerresearch.comms;

import com.shimmerresearch.comms.serialPortInterface.AbstractSerialPortHal;
import com.shimmerresearch.comms.serialPortInterface.ByteLevelDataCommListener;
import com.shimmerresearch.comms.serialPortInterface.SerialPortListener;
import com.shimmerresearch.exceptions.ShimmerException;
import com.shimmerresearch.verisense.communication.AbstractByteCommunication;

public class SerialPortByteCommunication extends AbstractByteCommunication {

	AbstractSerialPortHal abstractSerialPortHal;
	
	public SerialPortByteCommunication(AbstractSerialPortHal abstractSerialPortHal) {
		this.abstractSerialPortHal = abstractSerialPortHal;
		abstractSerialPortHal.addByteLevelDataCommListener(new ByteLevelDataCommListener() {
			
			@Override
			public void eventDisconnected() {
				if (mByteCommunicationListener != null) {
					mByteCommunicationListener.eventDisconnected();
				}
			}
			
			@Override
			public void eventConnected() {
				if (mByteCommunicationListener != null) {
					mByteCommunicationListener.eventConnected();
				}
			}
		});
		
		abstractSerialPortHal.registerSerialPortRxEventCallback(new SerialPortListener() {
			
			@Override
			public void serialPortRxEvent(int byteLength) {
				try {
					mByteCommunicationListener.eventNewBytesReceived(abstractSerialPortHal.rxBytes(byteLength));
				} catch (ShimmerException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		});
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
			abstractSerialPortHal.closeSafely();
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
		this.disconnect();
	}

	@Override
	public String getUuid() {
		// TODO Auto-generated method stub
		return null;
	}
	

}
