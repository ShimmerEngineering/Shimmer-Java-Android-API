package com.shimmerresearch.driverUtilities;

import java.io.Serializable;

import com.shimmerresearch.driver.UtilShimmer;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.FW_ID;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.FW_LABEL;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.HW_ID;

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
	
	public int mHardwareVersion = HW_ID.UNKNOWN;
	public String mHardwareVersionParsed = UtilShimmer.STRING_CONSTANT_FOR_UNKNOWN;
	
	public int mFirmwareIdentifier = FW_ID.UNKNOWN;
	public int mFirmwareVersionMajor = FW_ID.UNKNOWN;
	public int mFirmwareVersionMinor = FW_ID.UNKNOWN;
	public int mFirmwareVersionInternal = FW_ID.UNKNOWN;
	public int mFirmwareVersionCode = FW_ID.UNKNOWN;
	public String mFirmwareIdentifierParsed = UtilShimmer.STRING_CONSTANT_FOR_UNKNOWN;
	public String mFirmwareVersionParsed = UtilShimmer.STRING_CONSTANT_FOR_UNKNOWN;
	public String mFirmwareVersionParsedJustVersionNumber = UtilShimmer.STRING_CONSTANT_FOR_UNKNOWN;

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
	public ShimmerVerObject(
			int hardwareVersion, 
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
		if(mHardwareVersion!=HW_ID.UNKNOWN){
			if (ShimmerVerDetails.mMapOfShimmerRevisions.containsKey(mHardwareVersion)) {
				mHardwareVersionParsed = ShimmerVerDetails.mMapOfShimmerRevisions.get(mHardwareVersion);
			} else {
				mHardwareVersionParsed = ShimmerVerDetails.mMapOfShimmerRevisions.get(HW_ID.UNKNOWN);
			}
			
			//TODO change to static map approach rather then if statements
			// Handle parsed FW description. Keep first entry as a separate IF
			// statement
			mFirmwareIdentifierParsed = FW_LABEL.UNKNOWN;
			// Set default on Shimmer2R
			if (mHardwareVersion==HW_ID.SHIMMER_2R){
				if(FW_ID.SHIMMER2R.mMapOfFirmwareLabels.containsKey(mFirmwareIdentifier)){
					mFirmwareIdentifierParsed = FW_ID.SHIMMER2R.mMapOfFirmwareLabels.get(mFirmwareIdentifier);
				}
//				mFirmwareIdentifierParsed = FW_LABEL.BOILERPLATE;
//				if (mFirmwareIdentifier==FW_ID.SHIMMER2R.BTSTREAM){
//					mFirmwareIdentifierParsed = FW_LABEL.BTSTREAM;
//				}
			}
			else if (mHardwareVersion==HW_ID.SHIMMER_3){
				if(FW_ID.SHIMMER3.mMapOfFirmwareLabels.containsKey(mFirmwareIdentifier)){
					mFirmwareIdentifierParsed = FW_ID.SHIMMER3.mMapOfFirmwareLabels.get(mFirmwareIdentifier);
				}

//				if (mFirmwareIdentifier==FW_ID.SHIMMER3.BTSTREAM){
//					mFirmwareIdentifierParsed = FW_LABEL.BTSTREAM;
//				}
//				else if (mFirmwareIdentifier==FW_ID.SHIMMER3.LOGANDSTREAM){
//					mFirmwareIdentifierParsed = FW_LABEL.LOGANDSTREAM;
//				}
//				else if (mFirmwareIdentifier==FW_ID.SHIMMER3.SDLOG){
//					mFirmwareIdentifierParsed = FW_LABEL.SDLOG;
//				}
//				else if (mFirmwareIdentifier==FW_ID.SHIMMER3.DCU_SWEATSENSOR){
//					mFirmwareIdentifierParsed = FW_LABEL.DCU_SWEATSENSOR;
//				}
//				else if (mFirmwareIdentifier==FW_ID.SHIMMER3.GPIO_TEST){
//					mFirmwareIdentifierParsed = FW_LABEL.GPIO_TEST;
//				}
			}
			else if ((mHardwareVersion==HW_ID.SHIMMER_GQ_BLE)&&(mFirmwareIdentifier==FW_ID.SHIMMER3.GQ_BLE)){
//				mFirmwareIdentifierParsed = FW_LABEL.GQ_BLE;
				if(FW_ID.SHIMMER3.mMapOfFirmwareLabels.containsKey(mFirmwareIdentifier)){
					mFirmwareIdentifierParsed = FW_ID.SHIMMER3.mMapOfFirmwareLabels.get(mFirmwareIdentifier);
				}
			}
			else if ((mHardwareVersion==HW_ID.SHIMMER_GQ_802154)&&(mFirmwareIdentifier==FW_ID.SHIMMER_GQ.GQ_802154)){
//				mFirmwareIdentifierParsed = FW_LABEL.GQ_802154;
				if(FW_ID.SHIMMER_GQ.mMapOfFirmwareLabels.containsKey(mFirmwareIdentifier)){
					mFirmwareIdentifierParsed = FW_ID.SHIMMER_GQ.mMapOfFirmwareLabels.get(mFirmwareIdentifier);
				}
			}
			
			mFirmwareVersionParsed = mFirmwareIdentifierParsed;
		}
		
		if(mFirmwareVersionMajor!=FW_ID.UNKNOWN
				&&mFirmwareVersionMinor!=FW_ID.UNKNOWN
				&&mFirmwareVersionInternal!=FW_ID.UNKNOWN){
			// Handle FW version code.
			mFirmwareVersionCode = -1;
			if((UtilShimmer.compareVersions(mHardwareVersion,mFirmwareIdentifier,mFirmwareVersionMajor,mFirmwareVersionMinor,mFirmwareVersionInternal,HW_ID.SHIMMER_3,FW_ID.SHIMMER3.BTSTREAM,0,7,3))
					||(UtilShimmer.compareVersions(mHardwareVersion,mFirmwareIdentifier,mFirmwareVersionMajor,mFirmwareVersionMinor,mFirmwareVersionInternal,HW_ID.SHIMMER_3,FW_ID.SHIMMER3.LOGANDSTREAM,0,5,4))
					||(UtilShimmer.compareVersions(mHardwareVersion,mFirmwareIdentifier,mFirmwareVersionMajor,mFirmwareVersionMinor,mFirmwareVersionInternal,HW_ID.SHIMMER_3,FW_ID.SHIMMER3.SDLOG,0,11,5))){
				mFirmwareVersionCode = 6;
			}
			else if((UtilShimmer.compareVersions(mHardwareVersion,mFirmwareIdentifier,mFirmwareVersionMajor,mFirmwareVersionMinor,mFirmwareVersionInternal,HW_ID.SHIMMER_3,FW_ID.SHIMMER3.BTSTREAM,0,5,0))
					||(UtilShimmer.compareVersions(mHardwareVersion,mFirmwareIdentifier,mFirmwareVersionMajor,mFirmwareVersionMinor,mFirmwareVersionInternal,HW_ID.SHIMMER_3,FW_ID.SHIMMER3.LOGANDSTREAM,0,3,0))){
				mFirmwareVersionCode = 5;
			}
			else if((UtilShimmer.compareVersions(mHardwareVersion,mFirmwareIdentifier,mFirmwareVersionMajor,mFirmwareVersionMinor,mFirmwareVersionInternal,HW_ID.SHIMMER_3,FW_ID.SHIMMER3.BTSTREAM,0,4,0))
					||(UtilShimmer.compareVersions(mHardwareVersion,mFirmwareIdentifier,mFirmwareVersionMajor,mFirmwareVersionMinor,mFirmwareVersionInternal,HW_ID.SHIMMER_3,FW_ID.SHIMMER3.LOGANDSTREAM,0,2,0))){
				mFirmwareVersionCode = 4;
			}
			else if((UtilShimmer.compareVersions(mHardwareVersion,mFirmwareIdentifier,mFirmwareVersionMajor,mFirmwareVersionMinor,mFirmwareVersionInternal,HW_ID.SHIMMER_3,FW_ID.SHIMMER3.BTSTREAM,0,3,0))
					||(UtilShimmer.compareVersions(mHardwareVersion,mFirmwareIdentifier,mFirmwareVersionMajor,mFirmwareVersionMinor,mFirmwareVersionInternal,HW_ID.SHIMMER_3,FW_ID.SHIMMER3.LOGANDSTREAM,0,1,0))){
				mFirmwareVersionCode = 3;
			}
			else if(UtilShimmer.compareVersions(mHardwareVersion,mFirmwareIdentifier,mFirmwareVersionMajor,mFirmwareVersionMinor,mFirmwareVersionInternal,HW_ID.SHIMMER_3,FW_ID.SHIMMER3.BTSTREAM,0,2,0)){
				mFirmwareVersionCode = 2;
			}
			else if((UtilShimmer.compareVersions(mHardwareVersion,mFirmwareIdentifier,mFirmwareVersionMajor,mFirmwareVersionMinor,mFirmwareVersionInternal,HW_ID.SHIMMER_2R,FW_ID.SHIMMER2R.BTSTREAM,1,2,0))
					||(UtilShimmer.compareVersions(mHardwareVersion,mFirmwareIdentifier,mFirmwareVersionMajor,mFirmwareVersionMinor,mFirmwareVersionInternal,HW_ID.SHIMMER_3,FW_ID.SHIMMER3.BTSTREAM,0,1,0))){
				mFirmwareVersionCode = 1;
			}
			else if(UtilShimmer.compareVersions(mHardwareVersion,mFirmwareVersionMajor,mFirmwareVersionMinor,mFirmwareVersionInternal,HW_ID.SHIMMER_2R,0,0,0)){
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
	
			mFirmwareVersionParsedJustVersionNumber = "v" + mFirmwareVersionMajor + "." + mFirmwareVersionMinor + "." + mFirmwareVersionInternal;
			mFirmwareVersionParsed = mFirmwareIdentifierParsed + " " + mFirmwareVersionParsedJustVersionNumber;		
		}
	}

}
