package com.shimmerresearch.bluetooth;

public class RawBytePacketWithPCTimeStamp {

	public byte[] mDataArray;
	public long mSystemTimeStamp;
	public RawBytePacketWithPCTimeStamp(byte[] dataArray, long systemTime){
		mDataArray = dataArray;
		mSystemTimeStamp = systemTime;
	}
	
}
