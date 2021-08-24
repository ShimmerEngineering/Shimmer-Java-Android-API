package com.shimmerresearch.verisense.communication;

public interface ByteCommunicationListener {

	public void eventConnected();

	public void eventDisconnected();

	public void eventNewBytesReceived(byte[] rxBytes);

}
