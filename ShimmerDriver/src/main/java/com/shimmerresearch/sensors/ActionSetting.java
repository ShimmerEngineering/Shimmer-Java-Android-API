package com.shimmerresearch.sensors;

import java.util.List;

import com.shimmerresearch.driver.Configuration.COMMUNICATION_ACTION;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;

public class ActionSetting {
	
	public COMMUNICATION_TYPE mCommType;
	public COMMUNICATION_ACTION mAction;
	
	public ActionSetting(COMMUNICATION_TYPE commType){
		mCommType = commType;
	}
	
	public byte[] mActionByteArray;
	public List<byte[]> mActionListByteArray;
	public int mIndex;
	
}
