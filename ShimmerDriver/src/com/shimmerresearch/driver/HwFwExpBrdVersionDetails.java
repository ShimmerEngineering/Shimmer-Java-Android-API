package com.shimmerresearch.driver;

import java.io.Serializable;

public class HwFwExpBrdVersionDetails implements Serializable {

	//TODO: use already defined ShimmerHwFwDetails and ShimmerExpansionBoardDetails instead
	
	public int mHardwareVersion = 0;
	public int mFirmwareIndentifier = 0;
	public int mFirmwareVersionMajor = 0;
	public int mFirmwareVersionMinor = 0;
	public int mFirmwareVersionRelease = 0;
	public int mExpansionBoardVersion = 0;
	
	public HwFwExpBrdVersionDetails(int hardwareVersion, 
			int firmwareIndentifier,
			int firmwareVersionMajor, 
			int firmwareVersionMinor, 
			int firmwareVersionRelease,
			int expansionBoardVersion) {
		
		mHardwareVersion = hardwareVersion;
		mFirmwareIndentifier = firmwareIndentifier;
		mFirmwareVersionMajor = firmwareVersionMajor;
		mFirmwareVersionMinor = firmwareVersionMinor;
		mFirmwareVersionRelease = firmwareVersionRelease;
		mExpansionBoardVersion = expansionBoardVersion;
		
	}

}
