package com.shimmerresearch.verisense.communication;

public class SyncProgressDetails {

	public int mPayloadIndex;
	public boolean mCRCError;
	public double mTransferRateBytes;
	public SyncProgressDetails(int payloadIndex, boolean crcError, double transferRateBytes) {
		mPayloadIndex = payloadIndex;
		mCRCError = crcError;
		mTransferRateBytes = transferRateBytes;
	}
}
