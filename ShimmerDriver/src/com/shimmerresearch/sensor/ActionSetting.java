package com.shimmerresearch.sensor;

import com.shimmerresearch.driver.Configuration.COMMUNICATION_ACTION;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;

public class ActionSetting {
	
	public COMMUNICATION_TYPE mComType;
	public COMMUNICATION_ACTION mAction;
	
	public ActionSetting(COMMUNICATION_TYPE comType){
		mComType = comType;
	}
	
	public byte[] mActionByteArray;
	
	
}
