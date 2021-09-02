package com.shimmerresearch.verisense.communication.payloads;

import com.shimmerresearch.verisense.communication.VerisenseMessage.VERISENSE_PROPERTY;

/**
 * @author Mark Nolan
 *
 */
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
			if(b==VERISENSE_PROPERTY.STATUS.getPropertyMask()) {
				pendingEventStatus = true;
			} else if(b==VERISENSE_PROPERTY.DATA.getPropertyMask()) {
				pendingEventData = true;
			} else if(b==VERISENSE_PROPERTY.TIME.getPropertyMask()) {
				pendingEventTimeSync = true;
			}
		}
		
		isSuccess = true;
		return isSuccess;
	}
	
	@Override
	public byte[] generatePayloadContents() {
		// TODO Auto-generated method stub
		return null;
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
