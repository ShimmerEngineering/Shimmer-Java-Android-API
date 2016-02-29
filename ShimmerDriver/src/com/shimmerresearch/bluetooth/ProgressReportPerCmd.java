package com.shimmerresearch.bluetooth;

public class ProgressReportPerCmd {

	public int mCommandCompleted;
	public int mNumberofRemainingCMDsInBuffer;
	public String mBluetoothAddress;
	public String mComPort;
	
//	public ProgressReportPerCmd(int command, int numberofcmdsleft, String address){
//		mCommandCompleted = command;
//		mNumberofRemainingCMDsInBuffer = numberofcmdsleft;
//		mBluetoothAddress = address;
//	}
	
	public ProgressReportPerCmd(int command, int numberofcmdsleft, String address, String comPort){
		mCommandCompleted = command;
		mNumberofRemainingCMDsInBuffer = numberofcmdsleft;
		mBluetoothAddress = address;
		mComPort = comPort;
	}

}
