package com.shimmerresearch.bluetooth;

public class ProgressReport {

	public int mCommandCompleted;
	public int mNumberofRemainingCMDsInBuffer;
	public String mBluetoothAddress;
	
	public ProgressReport(int command, int numberofcmdsleft, String address){
		mCommandCompleted = command;
		mNumberofRemainingCMDsInBuffer = numberofcmdsleft;
		mBluetoothAddress = address;
	}
}
