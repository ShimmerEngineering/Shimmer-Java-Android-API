package com.shimmerresearch.verisense.communication;

import com.shimmerresearch.verisense.communication.VerisenseProtocolByteCommunication.VERISENSE_PROPERTY;

public class PendingEventsPayload extends AbstractPayload {
	
	public boolean pendingEventStatus = false;
	public boolean pendingEventData = false;
	public boolean pendingEventTimeSync = false;
	
	@Override
	public boolean parsePayloadContents(byte[] payloadContents) {
		super.payloadContents = payloadContents;
		isSuccess = false;

		pendingEventStatus = false;
		pendingEventData = false;
		pendingEventTimeSync = false;

		for(byte b : payloadContents) {
			if(b==VERISENSE_PROPERTY.STATUS.mask()) {
				pendingEventStatus = true;
			} else if(b==VERISENSE_PROPERTY.DATA.mask()) {
				pendingEventData = true;
			} else if(b==VERISENSE_PROPERTY.TIME.mask()) {
				pendingEventTimeSync = true;
			}
		}
		
		isSuccess = true;
		return isSuccess;
	}

	@Override
	public String generateDebugString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Pending Events Response:\n");
		if (!pendingEventStatus && !pendingEventData && !pendingEventTimeSync) {
			sb.append("\tNone\n");
		} else {
			if (pendingEventStatus) {
				sb.append("\tStatus\n");
			}
			if (pendingEventData) {
				sb.append("\tData\n");
			}
			if (pendingEventTimeSync) {
				sb.append("\tTime sync\n");
			}
			sb.append(CONSOLE_DIVIDER_STRING);
		}
		return sb.toString();
	}

}
