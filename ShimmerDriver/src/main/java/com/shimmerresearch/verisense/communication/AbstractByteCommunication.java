package com.shimmerresearch.verisense.communication;

public abstract class AbstractByteCommunication {

	protected ByteCommunicationListener mByteCommunicationListener;

	public abstract void connect();

	public abstract void disconnect();

	public abstract void writeBytes(byte[] bytes);

	public abstract void stop();

	public void setByteCommunicationListener(ByteCommunicationListener byteCommListener) {
		mByteCommunicationListener = byteCommListener;
	}

	public void removeRadioListenerList() {
		mByteCommunicationListener = null;
	}
}
