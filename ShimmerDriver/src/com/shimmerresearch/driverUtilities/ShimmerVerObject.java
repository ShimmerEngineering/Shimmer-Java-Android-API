package com.shimmerresearch.driverUtilities;

import java.io.Serializable;

import com.shimmerresearch.driver.UtilShimmer;
import com.shimmerresearch.driverUtilities.HwDriverShimmerDeviceDetails.SPAN_VERSION;
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
	
	//TODO handle SPAN_VERSION for SPANs? It is obtained from the PlatformHwManager
	//public SPAN_VERSION hardwareVersion = SPAN_VERSION.UNKNOWN;
	
    /** byte order of the Version Response Data Packet
    *
    */
   private enum VerReponsePacketOrder {
       hwVer,
       fwVerLSB,
       fwVerMSB,
       fwMajorLSB,
       fwMajorMSB,
       fwMinor,
       fwRevision
   }
   
	public ShimmerVerObject() {
		// TODO Auto-generated constructor stub
	}

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

		parseShimmerVerDetails();
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
		
		parseShimmerVerDetails();
	}
	
	/**
	 * Empty constructor used when finding the current information from a docked
	 * Shimmer/SPAN through the dock's UART communication channel.
	 * @param byteArray 
	 * 
	 */
	public ShimmerVerObject(byte[] byteArray) {
		if(byteArray.length >= 7) {
			mHardwareVersion = byteArray[VerReponsePacketOrder.hwVer.ordinal()];
			mFirmwareIdentifier = (byteArray[VerReponsePacketOrder.fwVerLSB.ordinal()]) | (byteArray[VerReponsePacketOrder.fwVerMSB.ordinal()]<<8);
			mFirmwareVersionMajor = (byteArray[VerReponsePacketOrder.fwMajorLSB.ordinal()]) | (byteArray[(int)VerReponsePacketOrder.fwMajorMSB.ordinal()]<<8);
			mFirmwareVersionMinor = byteArray[VerReponsePacketOrder.fwMinor.ordinal()];
			mFirmwareVersionInternal = byteArray[VerReponsePacketOrder.fwRevision.ordinal()];
			parseShimmerVerDetails();
		}
	}
	
	private void parseShimmerVerDetails() {
		if(mHardwareVersion!=HW_ID.UNKNOWN){
			if (ShimmerVerDetails.mMapOfShimmerRevisions.containsKey(mHardwareVersion)) {
				mHardwareVersionParsed = ShimmerVerDetails.mMapOfShimmerRevisions.get(mHardwareVersion);
			} else {
				mHardwareVersionParsed = ShimmerVerDetails.mMapOfShimmerRevisions.get(HW_ID.UNKNOWN);
			}
			
			mFirmwareIdentifierParsed = FW_LABEL.UNKNOWN;
			// Set default on Shimmer2R
			if ((mHardwareVersion==HW_ID.SHIMMER_2R)
				|| (mHardwareVersion==HW_ID.SHIMMER_3)
				|| ((mHardwareVersion==HW_ID.SHIMMER_GQ_BLE)&&(mFirmwareIdentifier==FW_ID.GQ_BLE))
				|| (((mHardwareVersion==HW_ID.SHIMMER_GQ_802154_NR)||(mHardwareVersion==HW_ID.SHIMMER_GQ_802154_LR)) && (mFirmwareIdentifier==FW_ID.GQ_802154))
				|| ((mHardwareVersion==HW_ID.SHIMMER_2R_GQ)&&(mFirmwareIdentifier==FW_ID.GQ_802154))
				|| (mHardwareVersion==HW_ID.SPAN)
				|| (mHardwareVersion==HW_ID.SHIMMER_4_SDK)
				){
				if(FW_ID.mMapOfFirmwareLabels.containsKey(mFirmwareIdentifier)){
					mFirmwareIdentifierParsed = FW_ID.mMapOfFirmwareLabels.get(mFirmwareIdentifier);
				}
			}
			
			mFirmwareVersionParsed = mFirmwareIdentifierParsed;
		}
		
		if(mFirmwareVersionMajor!=FW_ID.UNKNOWN
				&&mFirmwareVersionMinor!=FW_ID.UNKNOWN
				&&mFirmwareVersionInternal!=FW_ID.UNKNOWN){
			// Handle FW version code.
			mFirmwareVersionCode = -1;
			
			if(mHardwareVersion==HW_ID.SHIMMER_GQ_802154_NR || mHardwareVersion==HW_ID.SHIMMER_GQ_802154_LR || mHardwareVersion==HW_ID.SHIMMER_4_SDK){
				mFirmwareVersionCode = 6;
			}
			else if((UtilShimmer.compareVersions(mHardwareVersion,mFirmwareIdentifier,mFirmwareVersionMajor,mFirmwareVersionMinor,mFirmwareVersionInternal,HW_ID.SHIMMER_3,FW_ID.BTSTREAM,0,7,3))
					||(UtilShimmer.compareVersions(mHardwareVersion,mFirmwareIdentifier,mFirmwareVersionMajor,mFirmwareVersionMinor,mFirmwareVersionInternal,HW_ID.SHIMMER_3,FW_ID.LOGANDSTREAM,0,5,4))
					||(UtilShimmer.compareVersions(mHardwareVersion,mFirmwareIdentifier,mFirmwareVersionMajor,mFirmwareVersionMinor,mFirmwareVersionInternal,HW_ID.SHIMMER_3,FW_ID.SDLOG,0,11,5))){
				mFirmwareVersionCode = 6;
			}
			else if((UtilShimmer.compareVersions(mHardwareVersion,mFirmwareIdentifier,mFirmwareVersionMajor,mFirmwareVersionMinor,mFirmwareVersionInternal,HW_ID.SHIMMER_3,FW_ID.BTSTREAM,0,5,0))
					||(UtilShimmer.compareVersions(mHardwareVersion,mFirmwareIdentifier,mFirmwareVersionMajor,mFirmwareVersionMinor,mFirmwareVersionInternal,HW_ID.SHIMMER_3,FW_ID.LOGANDSTREAM,0,3,0))){
				mFirmwareVersionCode = 5;
			}
			else if((UtilShimmer.compareVersions(mHardwareVersion,mFirmwareIdentifier,mFirmwareVersionMajor,mFirmwareVersionMinor,mFirmwareVersionInternal,HW_ID.SHIMMER_3,FW_ID.BTSTREAM,0,4,0))
					||(UtilShimmer.compareVersions(mHardwareVersion,mFirmwareIdentifier,mFirmwareVersionMajor,mFirmwareVersionMinor,mFirmwareVersionInternal,HW_ID.SHIMMER_3,FW_ID.LOGANDSTREAM,0,2,0))){
				mFirmwareVersionCode = 4;
			}
			else if((UtilShimmer.compareVersions(mHardwareVersion,mFirmwareIdentifier,mFirmwareVersionMajor,mFirmwareVersionMinor,mFirmwareVersionInternal,HW_ID.SHIMMER_3,FW_ID.BTSTREAM,0,3,0))
					||(UtilShimmer.compareVersions(mHardwareVersion,mFirmwareIdentifier,mFirmwareVersionMajor,mFirmwareVersionMinor,mFirmwareVersionInternal,HW_ID.SHIMMER_3,FW_ID.LOGANDSTREAM,0,1,0))){
				mFirmwareVersionCode = 3;
			}
			else if(UtilShimmer.compareVersions(mHardwareVersion,mFirmwareIdentifier,mFirmwareVersionMajor,mFirmwareVersionMinor,mFirmwareVersionInternal,HW_ID.SHIMMER_3,FW_ID.BTSTREAM,0,2,0)){
				mFirmwareVersionCode = 2;
			}
			else if((UtilShimmer.compareVersions(mHardwareVersion,mFirmwareIdentifier,mFirmwareVersionMajor,mFirmwareVersionMinor,mFirmwareVersionInternal,HW_ID.SHIMMER_2R,FW_ID.BTSTREAM,1,2,0))
					||(UtilShimmer.compareVersions(mHardwareVersion,mFirmwareIdentifier,mFirmwareVersionMajor,mFirmwareVersionMinor,mFirmwareVersionInternal,HW_ID.SHIMMER_3,FW_ID.BTSTREAM,0,1,0))){
				mFirmwareVersionCode = 1;
			}
			else if(UtilShimmer.compareVersions(mHardwareVersion,mFirmwareVersionMajor,mFirmwareVersionMinor,mFirmwareVersionInternal,HW_ID.SHIMMER_2R,0,0,0)){
				mFirmwareVersionCode = 0;
			}
	
			mFirmwareVersionParsedJustVersionNumber = "v" + mFirmwareVersionMajor + "." + mFirmwareVersionMinor + "." + mFirmwareVersionInternal;
			mFirmwareVersionParsed = mFirmwareIdentifierParsed + " " + mFirmwareVersionParsedJustVersionNumber;		
		}
	}

	public int getFirmwareIdentifier() {
		return mFirmwareIdentifier;
	}

	public String getFirmwareVersionParsed() {
//		return mFirmwareVersionParsedJustVersionNumber;
		return mFirmwareVersionParsed;
	}

	public String getFirmwareVersionParsedJustVersionNumber() {
		return mFirmwareVersionParsedJustVersionNumber;
	}

	public int getHardwareVersion() {
		return mHardwareVersion;
	}

	public int getFirmwareVersionMajor() {
		return mFirmwareVersionMajor;
	}

	public int getFirmwareVersionMinor() {
		return mFirmwareVersionMinor;
	}

	public int getFirmwareVersionInternal() {
		return mFirmwareVersionInternal;
	}

	public int getFirmwareVersionCode() {
		return mFirmwareVersionCode;
	}

}
