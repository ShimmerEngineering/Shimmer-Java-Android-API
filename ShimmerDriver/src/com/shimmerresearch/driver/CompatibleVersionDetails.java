package com.shimmerresearch.driver;

import java.io.Serializable;

public class CompatibleVersionDetails implements Serializable {

	//TODO: use already defined ShimmerHwFwDetails and ShimmerExpansionBoardDetails instead
	
	public int mShimmerHardwareVersion = 0;
	public int mFirmwareIdentifier = 0;
	public int mFirmwareVersionMajor = 0;
	public int mFirmwareVersionMinor = 0;
	public int mFirmwareVersionInternal = 0;
	public int mShimmerExpansionBoardId = 0;
	
	public CompatibleVersionDetails(int hardwareVersion, 
			int firmwareIdentifier,
			int firmwareVersionMajor, 
			int firmwareVersionMinor, 
			int firmwareVersionInternal,
			int shimmerExpansionBoardId) {
		
		mShimmerHardwareVersion = hardwareVersion;
		mFirmwareIdentifier = firmwareIdentifier;
		mFirmwareVersionMajor = firmwareVersionMajor;
		mFirmwareVersionMinor = firmwareVersionMinor;
		mFirmwareVersionInternal = firmwareVersionInternal;
		mShimmerExpansionBoardId = shimmerExpansionBoardId;
		
	}

}
