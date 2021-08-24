package com.shimmerresearch.verisense.communication;

public class OpConfigPayload extends AbstractPayload {

	@Override
	public boolean parsePayloadContents(byte[] payloadContents) {
		super.payloadContents = payloadContents;
		
		IsSuccess = true;
		return IsSuccess;
	}

	@Override
	public String generateDebugString() {
		return "";
	}
}
