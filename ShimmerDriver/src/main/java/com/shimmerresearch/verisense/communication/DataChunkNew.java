package com.shimmerresearch.verisense.communication;

public class DataChunkNew {
	
	public byte[] mPackets;
	public int mExpectedLength;
	public int mCurrentLength;
	public long mUnixStartTimeinMS;
	public long mUnixFinishTimeinMS;
	public double mTransfer;
	public final int mPacketMaxSize = 32767; // Considering we are using int16 to get the length the maximum value is 7F FF which is 32767
	public boolean mCRCErrorPayload = false;

	public DataChunkNew() {
		mPackets = new byte[mPacketMaxSize];
	}

	public DataChunkNew(int expectedLength) {
		mPackets = new byte[expectedLength];
		mExpectedLength = expectedLength;
	}

	public boolean isCurrentLengthGreaterThanExpectedLength() {
		return mCurrentLength > mExpectedLength;
	}

	public boolean isCurrentLengthEqualToExpectedLength() {
		return mCurrentLength == mExpectedLength;
	}

	public boolean isCurrentLengthGreaterThanOrEqualToExpectedLength() {
		return mCurrentLength >= mExpectedLength;
	}

	public void handleStreamDataChunk(byte[] payload) {
		System.arraycopy(payload, 0, mPackets, mCurrentLength, payload.length);
		mCurrentLength += payload.length;
	}

}
