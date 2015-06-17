package com.shimmerresearch.exgConfig;

import com.shimmerresearch.driver.ShimmerMsg;
import com.shimmerresearch.driver.ShimmerObject;
import com.shimmerresearch.driver.ShimmerVerObject;
import com.shimmerresearch.exgConfig.ExGConfigOptionDetails.CHIP_INDEX;

public class ShimmerAbstract extends ShimmerObject{

	public void setShimmerVersionInfo(ShimmerVerObject hwfw) {
		super.mHardwareVersion = hwfw.mHardwareVersion;
		super.mHardwareVersionParsed = hwfw.mHardwareVersionParsed;
		super.mFirmwareIdentifier = hwfw.mFirmwareIdentifier;
		super.mFirmwareVersionMajor = hwfw.mFirmwareVersionMajor;
		super.mFirmwareVersionMinor = hwfw.mFirmwareVersionMinor;
		super.mFirmwareVersionInternal = hwfw.mFirmwareVersionInternal;
		super.mFirmwareVersionParsed = hwfw.mFirmwareVersionParsed;
		super.mFirmwareVersionCode = hwfw.mFirmwareVersionCode;
		super.sensorAndConfigMapsCreate();
	}
	
	public void setExpansionBoardId(int expansionBoardId){
		super.mExpansionBoardId = expansionBoardId;
	}
	
	public Object slotDetailsSetMethods(String componentName, Object valueToSet) {
		return super.slotDetailsSetMethods(componentName, valueToSet);
	}


	@Override
	public void setDefaultEMGConfiguration(){
		super.setDefaultEMGConfiguration();
	}

	@Override
	public void setDefaultECGConfiguration(){
		super.setDefaultECGConfiguration();
	}

	
	@Override
	public void setDefaultRespirationConfiguration(){
		super.setDefaultRespirationConfiguration();
	}

	@Override
	public void setEXGTestSignal(){
		super.setEXGTestSignal();
	}

	@Override
	public void setEXGCustom(){
		super.setEXGCustom();
	}

	@Override
	public void setExgPropertyValue(CHIP_INDEX chipIndex, String propertyName, Object value){
		super.setExgPropertyValue(chipIndex, propertyName, value);
	}
	
	
	@Override
	protected void checkBattery() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void processMsgFromCallback(ShimmerMsg shimmerMSG) {
		// TODO Auto-generated method stub
		
	}
	
}