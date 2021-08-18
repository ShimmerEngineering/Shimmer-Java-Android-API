package com.shimmerresearch.driver.ble;

import com.shimmerresearch.comms.radioProtocol.RadioListener;

public abstract class AbstractByteCommunication {
	
	protected ByteCommunicationListener mByteCommunicationListener;
	
	public abstract void connect();
	
	public abstract void disconnect();
	
	public abstract void writeBytes(byte[] bytes);
	
	public void setByteCommunicationListener(ByteCommunicationListener byteCommListener){
		mByteCommunicationListener = byteCommListener;
	}
	
	public void removeRadioListenerList(){
		mByteCommunicationListener = null;
	}
}
