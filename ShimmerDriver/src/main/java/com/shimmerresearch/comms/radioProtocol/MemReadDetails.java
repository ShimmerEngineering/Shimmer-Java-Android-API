package com.shimmerresearch.comms.radioProtocol;

import java.io.Serializable;

public class MemReadDetails implements Serializable {
	
	private static final long serialVersionUID = 6209428298720646676L;
	
	public int mCommand = 0;
//	protected byte[] mMemBuffer = new byte[]{};
	public int mCurrentMemAddress = 0;
	public int mCurrentMemLengthToRead = 0;
	private int mTotalMemLengthToRead = 0;
	public int mEndMemAddress = 0;

	protected int mMemAddressLimit = 0;
	
	public MemReadDetails(int command, int address, int size, int memAddressLimit) {
		mCommand = command;
		mCurrentMemAddress = address;
//		mMemBuffer = new byte[size];
		setTotalMemLengthToRead(size);
		mMemAddressLimit = memAddressLimit;
	}
	
	public void setTotalMemLengthToRead(int size){
		mTotalMemLengthToRead = size;
		updateEndAddress();
	}
	
	public int getTotalMemLengthToRead(){
		return mTotalMemLengthToRead;
	}
	
	private void updateEndAddress(){
		mEndMemAddress = mCurrentMemAddress + mTotalMemLengthToRead;
	}
}
