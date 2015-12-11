package com.shimmerresearch.sensor;

import java.util.List;

import com.shimmerresearch.driver.Configuration.COMMUNICATION_ACTION;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;

public class ActionSetting {
	
	public COMMUNICATION_TYPE mComType;
	public COMMUNICATION_ACTION mAction;
	
	public ActionSetting(COMMUNICATION_TYPE comType){
		mComType = comType;
	}
	
	public byte[] mActionByteArray;
	public List<byte[]> mActionListByteArray;
	public int mIndex;
	
}
