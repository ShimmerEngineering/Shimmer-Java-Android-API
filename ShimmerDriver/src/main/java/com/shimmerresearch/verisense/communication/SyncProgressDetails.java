package com.shimmerresearch.verisense.communication;

public class SyncProgressDetails {

	public int mPayloadIndex;
	public boolean mCRCError;
	public SyncProgressDetails(int payloadIndex, boolean crcError) {
		mPayloadIndex = payloadIndex;
		mCRCError = crcError;
	}
}
