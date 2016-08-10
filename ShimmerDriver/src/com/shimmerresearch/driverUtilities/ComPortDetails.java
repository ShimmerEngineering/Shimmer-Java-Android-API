package com.shimmerresearch.driverUtilities;

import com.shimmerresearch.driverUtilities.HwDriverShimmerDeviceDetails.DEVICE_TYPE;


public class ComPortDetails {

	public String mComPort = "";
	public String mComPortDescription = "";
	public String mWinRegFriendlyName = "";
	
	public String mShimmerMacId = "";
	public String mShimmerMacIdParsed = "";
	
	public DEVICE_TYPE mDeviceTypeDetected = DEVICE_TYPE.UNKOWN;

	 public ComPortDetails(String comPort, String comPortDescription){
		 mComPort = comPort;
		 mComPortDescription = comPortDescription;
	 }
	 
	 public ComPortDetails(String comPort, String macId, String winRegFriendlyName){
		 mComPort = comPort;
		 mWinRegFriendlyName = winRegFriendlyName;
		 setMacId(macId);
	 }

	 public void setMacId(String mac){
		 
		 mac = mac.replace("-", "");
		 
		 if(mac.length()==12) {
			 mShimmerMacId = mac.toUpperCase();
			 mShimmerMacIdParsed = mShimmerMacId.substring(8).toUpperCase();
		 }
	 }

	public String getGuiName() {
		if(!mWinRegFriendlyName.isEmpty())
			return mWinRegFriendlyName;
		else
			return mShimmerMacIdParsed;
	}	 
}
