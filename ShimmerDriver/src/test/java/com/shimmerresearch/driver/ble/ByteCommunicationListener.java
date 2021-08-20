package com.shimmerresearch.driver.ble;

public interface ByteCommunicationListener {

	public void eventConnected();

	public void eventDisconnected();

	public void eventNewBytesReceived(byte[] rxBytes);

}
