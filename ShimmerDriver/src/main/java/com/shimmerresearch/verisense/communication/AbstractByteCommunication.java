package com.shimmerresearch.verisense.communication;

import com.shimmerresearch.exceptions.ShimmerException;

public abstract class AbstractByteCommunication {

	protected ByteCommunicationListener mByteCommunicationListener;

	public abstract void connect() throws ShimmerException;

	public abstract void disconnect() throws ShimmerException;

	public abstract void writeBytes(byte[] bytes);

	public abstract void stop();
	
	public abstract String getUuid();

	public void setByteCommunicationListener(ByteCommunicationListener byteCommListener) {
		mByteCommunicationListener = byteCommListener;
	}

	public void removeRadioListenerList() {
		mByteCommunicationListener = null;
	}
}
