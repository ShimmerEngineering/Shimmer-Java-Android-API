package com.shimmerresearch.bluetooth;

public class ProgressReportPerCmd {

	public int mCommandCompleted;
	public int mNumberofRemainingCMDsInBuffer;
	public String mBluetoothAddress;
	
	public ProgressReportPerCmd(int command, int numberofcmdsleft, String address){
		mCommandCompleted = command;
		mNumberofRemainingCMDsInBuffer = numberofcmdsleft;
		mBluetoothAddress = address;
	}
}
