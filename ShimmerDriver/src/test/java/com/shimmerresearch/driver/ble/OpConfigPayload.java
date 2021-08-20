package com.shimmerresearch.driver.ble;

public class OpConfigPayload {

	public byte[] mOpConfig;

	public boolean processPayload(byte[] response) {
		mOpConfig = new byte[response.length - 3];
		System.arraycopy(response, 3, mOpConfig, 0, mOpConfig.length);
		return true;
	}
}
