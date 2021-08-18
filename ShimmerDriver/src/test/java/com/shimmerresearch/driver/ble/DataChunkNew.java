package com.shimmerresearch.driver.ble;

public class DataChunkNew {
	public byte[] mPackets;
    public int mExpectedLength;
    public int mCurrentLength;

    public double mTransfer;
    public final int mPacketMaxSize = 32767; //Considering we are using int16 to get the length the maximum value is 7F FF which is 32767
    public boolean mCRCErrorPayload = false;
    public DataChunkNew()
    {
    	mPackets = new byte[mPacketMaxSize];
    }

    public DataChunkNew(int memoryPacketSize)
    {
        mPackets = new byte[memoryPacketSize];
    }
}
