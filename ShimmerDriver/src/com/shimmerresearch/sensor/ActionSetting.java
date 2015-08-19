package com.shimmerresearch.sensor;

public class ActionSetting {
	
	public enum Modes{
		Bluetooth,
		SD,
		Radio802154
	}
	
	public int mAction;
	public byte[] mActionByteArray;
	
}
