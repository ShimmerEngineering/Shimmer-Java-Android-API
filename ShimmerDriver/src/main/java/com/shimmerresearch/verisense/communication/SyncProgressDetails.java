package com.shimmerresearch.verisense.communication;

public class SyncProgressDetails {

	public int mPayloadIndex;
	public boolean mCRCError;
	public double mTransferRateBytes;
	public String mBinFilePath;
	public SyncProgressDetails(int payloadIndex, boolean crcError, double transferRateBytes, String binFilePath) {
		mPayloadIndex = payloadIndex;
		mCRCError = crcError;
		mTransferRateBytes = transferRateBytes;
		mBinFilePath = binFilePath;
	}
	public SyncProgressDetails(String binFilePath) {
		mBinFilePath = binFilePath;
	}
}
