package com.shimmerresearch.driverUtilities;

import com.shimmerresearch.driverUtilities.HwDriverShimmerDeviceDetails.DEVICE_TYPE;


public class ComPortDetails {

	public String mComPort = "";
	public String mComPortDescription = "";
	public String mFriendlyName = "";
	
	public String mShimmerMacId = "";
	public String mShimmerMacIdParsed = "";
	
	public DEVICE_TYPE mDeviceTypeDetected = DEVICE_TYPE.UNKOWN;

	 public ComPortDetails(String comPort, String comPortDescription){
		 mComPort = comPort;
		 mComPortDescription = comPortDescription;
	 }
	 
	 public ComPortDetails(String comPort, String macId, String friendlyName){
		 mComPort = comPort;
		 mFriendlyName = friendlyName;
		 setMacId(macId);
		 setDeviceType();
	 }

	 public void setMacId(String mac){
		 
		 mac = mac.replace("-", "");
		 
		 if(mac.length()==12) {
			 mShimmerMacId = mac.toUpperCase();
			 mShimmerMacIdParsed = mShimmerMacId.substring(8).toUpperCase();
		 }
	 }

	public String getGuiName() {
		if(!mFriendlyName.isEmpty())
			return mFriendlyName;
		else
			return mShimmerMacIdParsed;
	}
	
	public void setDeviceType() {
		if(!mShimmerMacId.equals(UtilShimmer.MAC_ADDRESS_ZEROS)){
			if(mFriendlyName.contains(HwDriverShimmerDeviceDetails.SH_SEARCH.BT.SHIMMER3)){
				mDeviceTypeDetected = DEVICE_TYPE.SHIMMER3;
			}
			else if(mFriendlyName.contains(HwDriverShimmerDeviceDetails.SH_SEARCH.BT.SHIMMER4)){
				mDeviceTypeDetected = DEVICE_TYPE.SHIMMER4;
			}
			else if(mFriendlyName.contains(HwDriverShimmerDeviceDetails.SH_SEARCH.BT.RN42)){
				mDeviceTypeDetected = DEVICE_TYPE.RN42;
			}
			else if(mFriendlyName.contains(HwDriverShimmerDeviceDetails.SH_SEARCH.BT.MANUFACTURER_LUMAFIT)){
				mDeviceTypeDetected = DEVICE_TYPE.LUMAFIT;
			}
			else if(mFriendlyName.contains(HwDriverShimmerDeviceDetails.SH_SEARCH.BT.MANUFACTURER_NONIN)){
				mDeviceTypeDetected = DEVICE_TYPE.NONIN_ONYX_II;
			}
		}
	}
}
