package com.shimmerresearch.driver;

import java.io.Serializable;

import com.shimmerresearch.driver.ShimmerVerDetails.FW_LABEL;
import com.shimmerresearch.driver.ShimmerVerDetails.HW_ID;
import com.shimmerresearch.driver.ShimmerVerDetails.FW_ID;

/**
 * Holds HW, FW and expansion board infomation. Used for docked Shimmers current
 * info and also for the purposes of compatible version checking.
 * 
 * @author Mark Nolan
 *
 */
public class ShimmerVerObject implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1966526754185423783L;
	
	public int mHardwareVersion = 0;
	public String mHardwareVersionParsed = "";
	
	public int mFirmwareIdentifier = 0;
	public int mFirmwareVersionMajor = 0;
	public int mFirmwareVersionMinor = 0;
	public int mFirmwareVersionInternal = 0;
	public int mFirmwareVersionCode = 0;
	public String mFirmwareIdentifierParsed = "";
	public String mFirmwareVersionParsed = "";
	public String mFirmwareVersionParsedJustVersionNumber = "";

	public int mShimmerExpansionBoardId = 0;
	
	/**
	 * Used specifically when finding the current information from a docked
	 * Shimmer through the dock's UART communication channel.

	 * @param hardwareVersion
	 * @param firmwareIdentifier
	 * @param firmwareVersionMajor
	 * @param firmwareVersionMinor
	 * @param firmwareVersionInternal
	 */
	public ShimmerVerObject(
			int hardwareVersion, 
			int firmwareIdentifier,
			int firmwareVersionMajor, 
			int firmwareVersionMinor,
			int firmwareVersionInternal) {

		mHardwareVersion = hardwareVersion;
		mFirmwareIdentifier = firmwareIdentifier;
		mFirmwareVersionMajor = firmwareVersionMajor;
		mFirmwareVersionMinor = firmwareVersionMinor;
		mFirmwareVersionInternal = firmwareVersionInternal;

		parseVerDetails();
	}

	/**
	 * Used specifically for compatible version checking
	 * 
	 * @param hardwareVersion
	 * @param firmwareIdentifier
	 * @param firmwareVersionMajor
	 * @param firmwareVersionMinor
	 * @param firmwareVersionInternal
	 * @param shimmerExpansionBoardId
	 */
	public ShimmerVerObject(int hardwareVersion, 
			int firmwareIdentifier,
			int firmwareVersionMajor, 
			int firmwareVersionMinor, 
			int firmwareVersionInternal,
			int shimmerExpansionBoardId) {
		
		mHardwareVersion = hardwareVersion;
		mFirmwareIdentifier = firmwareIdentifier;
		mFirmwareVersionMajor = firmwareVersionMajor;
		mFirmwareVersionMinor = firmwareVersionMinor;
		mFirmwareVersionInternal = firmwareVersionInternal;
		mShimmerExpansionBoardId = shimmerExpansionBoardId;
		
		parseVerDetails();
	}
	
	/**
	 * Empty constructor used when finding the current information from a docked
	 * Shimmer through the dock's UART communication channel.
	 * 
	 */
	public ShimmerVerObject() {
		// TODO Auto-generated constructor stub
	}
	

	private void parseVerDetails() {
		if (ShimmerVerDetails.mMapOfShimmerRevisions.containsKey(mHardwareVersion)) {
			mHardwareVersionParsed = ShimmerVerDetails.mMapOfShimmerRevisions.get(mHardwareVersion);
		} else {
			mHardwareVersionParsed = "Unknown";
		}
		
		// Handle parsed FW description. Keep first entry as a separate IF
		// statement
		mFirmwareIdentifierParsed = "Unknown";
		if (mHardwareVersion==HW_ID.SHIMMER_2R){
			mFirmwareIdentifierParsed = FW_LABEL.BOILERPLATE;
		}
		if (((mHardwareVersion==HW_ID.SHIMMER_2R)&&(mFirmwareIdentifier==FW_ID.SHIMMER2R.BTSTREAM))
				||((mHardwareVersion==HW_ID.SHIMMER_3)&&(mFirmwareIdentifier==FW_ID.SHIMMER3.BTSTREAM))){
			mFirmwareIdentifierParsed = FW_LABEL.BTSTREAM;
		}
		else if ((mHardwareVersion==HW_ID.SHIMMER_3)&&(mFirmwareIdentifier==FW_ID.SHIMMER3.LOGANDSTREAM)){
			mFirmwareIdentifierParsed = FW_LABEL.LOGANDSTREAM;
		}
		else if ((mHardwareVersion==HW_ID.SHIMMER_3)&&(mFirmwareIdentifier==FW_ID.SHIMMER3.SDLOG)){
			mFirmwareIdentifierParsed = FW_LABEL.SDLOG;
		}
		else if ((mHardwareVersion==HW_ID.SHIMMER_3)&&(mFirmwareIdentifier==FW_ID.SHIMMER3.DCU_SWEATSENSOR)){
			mFirmwareIdentifierParsed = FW_LABEL.DCU_SWEATSENSOR;
		}
		else if ((mHardwareVersion==HW_ID.SHIMMER_3)&&(mFirmwareIdentifier==FW_ID.SHIMMER3.GPIO_TEST)){
			mFirmwareIdentifierParsed = FW_LABEL.GPIO_TEST;
		}
		else if ((mHardwareVersion==HW_ID.SHIMMER_3)&&(mFirmwareIdentifier==FW_ID.SHIMMER3.GQ_GSR)){
			mFirmwareIdentifierParsed = FW_LABEL.GQ_GSR;
		}
		
		//TODO update with BtStream FW info when released for code = 6
		// Handle FW version code.
		mFirmwareVersionCode = -1;
		if((Util.compareVersions(mHardwareVersion,mFirmwareIdentifier,mFirmwareVersionMajor,mFirmwareVersionMinor,mFirmwareVersionInternal,HW_ID.SHIMMER_3,FW_ID.SHIMMER3.LOGANDSTREAM,0,5,11))){
			mFirmwareVersionCode = 7;
		}
		if((Util.compareVersions(mHardwareVersion,mFirmwareIdentifier,mFirmwareVersionMajor,mFirmwareVersionMinor,mFirmwareVersionInternal,HW_ID.SHIMMER_3,FW_ID.SHIMMER3.BTSTREAM,0,8,0))
				||(Util.compareVersions(mHardwareVersion,mFirmwareIdentifier,mFirmwareVersionMajor,mFirmwareVersionMinor,mFirmwareVersionInternal,HW_ID.SHIMMER_3,FW_ID.SHIMMER3.LOGANDSTREAM,0,5,4))){
			mFirmwareVersionCode = 6;
		}
		else if((Util.compareVersions(mHardwareVersion,mFirmwareIdentifier,mFirmwareVersionMajor,mFirmwareVersionMinor,mFirmwareVersionInternal,HW_ID.SHIMMER_3,FW_ID.SHIMMER3.BTSTREAM,0,5,0))
				||(Util.compareVersions(mHardwareVersion,mFirmwareIdentifier,mFirmwareVersionMajor,mFirmwareVersionMinor,mFirmwareVersionInternal,HW_ID.SHIMMER_3,FW_ID.SHIMMER3.LOGANDSTREAM,0,3,0))){
			mFirmwareVersionCode = 5;
		}
		else if((Util.compareVersions(mHardwareVersion,mFirmwareIdentifier,mFirmwareVersionMajor,mFirmwareVersionMinor,mFirmwareVersionInternal,HW_ID.SHIMMER_3,FW_ID.SHIMMER3.BTSTREAM,0,4,0))
				||(Util.compareVersions(mHardwareVersion,mFirmwareIdentifier,mFirmwareVersionMajor,mFirmwareVersionMinor,mFirmwareVersionInternal,HW_ID.SHIMMER_3,FW_ID.SHIMMER3.LOGANDSTREAM,0,2,0))){
			mFirmwareVersionCode = 4;
		}
		else if((Util.compareVersions(mHardwareVersion,mFirmwareIdentifier,mFirmwareVersionMajor,mFirmwareVersionMinor,mFirmwareVersionInternal,HW_ID.SHIMMER_3,FW_ID.SHIMMER3.BTSTREAM,0,3,0))
				||(Util.compareVersions(mHardwareVersion,mFirmwareIdentifier,mFirmwareVersionMajor,mFirmwareVersionMinor,mFirmwareVersionInternal,HW_ID.SHIMMER_3,FW_ID.SHIMMER3.LOGANDSTREAM,0,1,0))){
			mFirmwareVersionCode = 3;
		}
		else if(Util.compareVersions(mHardwareVersion,mFirmwareIdentifier,mFirmwareVersionMajor,mFirmwareVersionMinor,mFirmwareVersionInternal,HW_ID.SHIMMER_3,FW_ID.SHIMMER3.BTSTREAM,0,2,0)){
			mFirmwareVersionCode = 2;
		}
		else if((Util.compareVersions(mHardwareVersion,mFirmwareIdentifier,mFirmwareVersionMajor,mFirmwareVersionMinor,mFirmwareVersionInternal,HW_ID.SHIMMER_2R,FW_ID.SHIMMER2R.BTSTREAM,1,2,0))
				||(Util.compareVersions(mHardwareVersion,mFirmwareIdentifier,mFirmwareVersionMajor,mFirmwareVersionMinor,mFirmwareVersionInternal,HW_ID.SHIMMER_3,FW_ID.SHIMMER3.BTSTREAM,0,1,0))){
			mFirmwareVersionCode = 1;
		}
		else if(Util.compareVersions(mHardwareVersion,mFirmwareVersionMajor,mFirmwareVersionMinor,mFirmwareVersionInternal,HW_ID.SHIMMER_2R,0,0,0)){
			mFirmwareVersionCode = 0;
		}

		//Old code
//		if (mFirmwareIdentifier == FW_ID.SHIMMER3.BOILER_PLATE) {
//			mFirmwareIdentifierParsed = "Boilerplate";
//		} else if (mFirmwareIdentifier == FW_ID.SHIMMER3.BTSTREAM) {
//			mFirmwareIdentifierParsed = "BtStream";
//
//			if ((mFirmwareVersionMajor == 0 && mFirmwareVersionMinor == 1)
//					|| (mFirmwareVersionMajor == 1 && mFirmwareVersionMinor == 2 && mHardwareVersion == HW_ID.SHIMMER_2R))
//				mFirmwareVersionCode = 1;
//			else if (mFirmwareVersionMajor == 0 && mFirmwareVersionMinor == 2)
//				mFirmwareVersionCode = 2;
//			else if (mFirmwareVersionMajor == 0 && mFirmwareVersionMinor == 3)
//				mFirmwareVersionCode = 3;
//			else if (mFirmwareVersionMajor == 0 && mFirmwareVersionMinor == 4)
//				mFirmwareVersionCode = 4;
//			else
//				// if(mFirmwareVersionMajor==0 && mFirmwareVersionMinor==5)
//				mFirmwareVersionCode = 5;
//
//		} else if (mFirmwareIdentifier == FW_ID.SHIMMER3.SDLOG) {
//			mFirmwareIdentifierParsed = "SDLog";
//
//			// TODO
//			mFirmwareVersionCode = 6;
//
//			// if(mFirmwareVersionMajor==0 && mFirmwareVersionMinor==1)
//			// mFirmwareVersionCode = 3;
//			// else if(mFirmwareVersionMajor==0 && mFirmwareVersionMinor==2)
//			// mFirmwareVersionCode = 4;
//			// else if(mFirmwareVersionMajor==0 && mFirmwareVersionMinor==3)
//			// mFirmwareVersionCode = 5;
//
//		} else if (mFirmwareIdentifier == FW_ID.SHIMMER3.LOGANDSTREAM) {
//			mFirmwareIdentifierParsed = "LogAndStream";
//
//			if (mFirmwareVersionMajor == 0 && mFirmwareVersionMinor == 1)
//				mFirmwareVersionCode = 3;
//			else if (mFirmwareVersionMajor == 0 && mFirmwareVersionMinor == 2)
//				mFirmwareVersionCode = 4;
//			else
//				// if(mFirmwareVersionMajor==0 && mFirmwareVersionMinor==3)
//				mFirmwareVersionCode = 5;
//		} else if (mFirmwareIdentifier == FW_ID.SHIMMER3.GQ_GSR) {
//			mFirmwareIdentifierParsed = "GQ GSR";
//
//			// TODO
//			mFirmwareVersionCode = 7;
//		} else {
//			mFirmwareIdentifierParsed = "Unknown";
//		}

		mFirmwareVersionParsed = mFirmwareIdentifierParsed + " v"
				+ mFirmwareVersionMajor + "." + mFirmwareVersionMinor + "."
				+ mFirmwareVersionInternal;		
		
		mFirmwareVersionParsedJustVersionNumber = "v"
				+ mFirmwareVersionMajor + "." + mFirmwareVersionMinor + "."
				+ mFirmwareVersionInternal;
	}

}
