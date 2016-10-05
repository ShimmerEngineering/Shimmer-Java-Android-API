package com.shimmerresearch.driverUtilities;

import java.io.Serializable;
import java.util.Calendar;

import com.shimmerresearch.driverUtilities.HwDriverShimmerDeviceDetails.SPAN_VERSION;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.FW_ID;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.FW_LABEL;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.HW_ID;
import com.shimmerresearch.sensors.SensorEXG.GuiLabelSensorTiles;

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

	//TODO MNtoMN: below is a bad implementation, consider using ExpansionBoardDetails 
	public int mShimmerExpansionBoardId = 0;
	
	//TODO handle SPAN_VERSION for SPANs? It is obtained from the PlatformHwManager
	//public SPAN_VERSION hardwareVersion = SPAN_VERSION.UNKNOWN;
	
//    /** byte order of the Version Response Data Packet for a 7 byte ver response */
//   private enum VerReponsePacketOrderLegacy {
//       hwVer,
//       fwVerLSB,
//       fwVerMSB,
//       fwMajorLSB,
//       fwMajorMSB,
//       fwMinor,
//       fwRevision
//   }
//   
//   /** byte order of the Version Response Data Packet for an 8 byte ver response */
//   private enum VerReponsePacketOrder {
//       hwVerLSB,
//       hwVerMSB,
//       fwVerLSB,
//       fwVerMSB,
//       fwMajorLSB,
//       fwMajorMSB,
//       fwMinor,
//       fwRevision
//   }
   
	public ShimmerVerObject() {
		// TODO Auto-generated constructor stub
	}
	
	/**Not enough to parseShimmerVerDetails
	 * @param firmwareIdentifier
	 * @param firmwareVersionMajor
	 * @param firmwareVersionMinor
	 * @param firmwareVersionInternal
	 */
	public ShimmerVerObject(
			int firmwareIdentifier,
			int firmwareVersionMajor, 
			int firmwareVersionMinor,
			int firmwareVersionInternal) {
		mFirmwareIdentifier = firmwareIdentifier;
		mFirmwareVersionMajor = firmwareVersionMajor;
		mFirmwareVersionMinor = firmwareVersionMinor;
		mFirmwareVersionInternal = firmwareVersionInternal;
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
		
		this(firmwareIdentifier,
				firmwareVersionMajor,
				firmwareVersionMinor,
				firmwareVersionInternal);

		mHardwareVersion = hardwareVersion;
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

		this(firmwareIdentifier,
				firmwareVersionMajor,
				firmwareVersionMinor,
				firmwareVersionInternal);

		mHardwareVersion = hardwareVersion;
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
		parseVersionByteArray(byteArray);
	}
	
	public void parseVersionByteArray(byte[] byteArray) {
		if((byteArray.length == 7) || (byteArray.length == 8)){
//			mHardwareVersion = byteArray[VerReponsePacketOrderLegacy.hwVer.ordinal()];
//			mFirmwareIdentifier = (byteArray[VerReponsePacketOrderLegacy.fwVerLSB.ordinal()]) | (byteArray[VerReponsePacketOrderLegacy.fwVerMSB.ordinal()]<<8);
//			mFirmwareVersionMajor = (byteArray[VerReponsePacketOrderLegacy.fwMajorLSB.ordinal()]) | (byteArray[(int)VerReponsePacketOrderLegacy.fwMajorMSB.ordinal()]<<8);
//			mFirmwareVersionMinor = byteArray[VerReponsePacketOrderLegacy.fwMinor.ordinal()];
//			mFirmwareVersionInternal = byteArray[VerReponsePacketOrderLegacy.fwRevision.ordinal()];
//			parseShimmerVerDetails();
			
			int index = 0;
			if(byteArray.length == 7) {
				mHardwareVersion = byteArray[index++]&0xFF;
			}
			else if(byteArray.length == 8) {
				mHardwareVersion = (byteArray[index++] | (byteArray[index++]<<8))&0xFFFF;
			}
			
			mFirmwareIdentifier = ((byteArray[index++]) | (byteArray[index++]<<8))&0xFFFF;
			mFirmwareVersionMajor = ((byteArray[index++]) | (byteArray[index++]<<8))&0xFFFF;
			mFirmwareVersionMinor = byteArray[index++]&0xFF;
			mFirmwareVersionInternal = byteArray[index++]&0xFF;
			
			parseShimmerVerDetails();
		}
	}
	
	public byte[] generateVersionByteArrayNew() {
		byte[] byteArray = new byte[8];
		
		int index = 0;
		byteArray[index++] = (byte) (mHardwareVersion&0xFF);
		byteArray[index++] = (byte) ((mHardwareVersion>>8)&0xFF);

		byteArray[index++] = (byte) (mFirmwareIdentifier&0xFF);
		byteArray[index++] = (byte) ((mFirmwareIdentifier>>8)&0xFF);

		byteArray[index++] = (byte) (mFirmwareVersionMajor&0xFF);
		byteArray[index++] = (byte) ((mFirmwareVersionMajor>>8)&0xFF);

		byteArray[index++] = (byte) (mFirmwareVersionMinor&0xFF);
		byteArray[index++] = (byte) (mFirmwareVersionInternal&0xFF);
		
		return byteArray;
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
			
			if((UtilShimmer.compareVersions(mHardwareVersion,mFirmwareIdentifier,mFirmwareVersionMajor,mFirmwareVersionMinor,mFirmwareVersionInternal,HW_ID.SHIMMER_3,FW_ID.LOGANDSTREAM,0,6,5))
					|| mHardwareVersion==HW_ID.SHIMMER_4_SDK){
				mFirmwareVersionCode = 7;
			}
			else if((UtilShimmer.compareVersions(mHardwareVersion,mFirmwareIdentifier,mFirmwareVersionMajor,mFirmwareVersionMinor,mFirmwareVersionInternal,HW_ID.SHIMMER_3,FW_ID.BTSTREAM,0,7,3))
					||(UtilShimmer.compareVersions(mHardwareVersion,mFirmwareIdentifier,mFirmwareVersionMajor,mFirmwareVersionMinor,mFirmwareVersionInternal,HW_ID.SHIMMER_3,FW_ID.LOGANDSTREAM,0,5,4))
					||(UtilShimmer.compareVersions(mHardwareVersion,mFirmwareIdentifier,mFirmwareVersionMajor,mFirmwareVersionMinor,mFirmwareVersionInternal,HW_ID.SHIMMER_3,FW_ID.SDLOG,0,11,5))
					|| mHardwareVersion==HW_ID.SHIMMER_GQ_802154_NR 
					|| mHardwareVersion==HW_ID.SHIMMER_GQ_802154_LR){
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

	public void setHardwareVersion(int hardwareVersion) {
		mHardwareVersion = hardwareVersion;
		parseShimmerVerDetails();
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
	
	
	public boolean isMplSupported() {
		return isMplSupported(this, getHardwareVersion(), getFirmwareIdentifier());
	}

	public static boolean isMplSupported(ShimmerVerObject svo, int hwVer, int fwId) {
		if (isVerCompatibleWith(svo, HW_ID.SHIMMER_3, FW_ID.SDLOG, 0, 7, 0)
//				|| (hwVer==HW_ID.SHIMMER_4_SDK)
				){
			return true;
		}
		return false;
	}
	

	public boolean isRtcConfigViaUartSupported() {
		return isRtcConfigViaUartSupported(getHardwareVersion(), getFirmwareIdentifier());
	}

	public static boolean isRtcConfigViaUartSupported(int hwVer, int fwId) {
		if (((hwVer==HW_ID.SHIMMER_3)&&(fwId == FW_ID.SDLOG))
				|| ((hwVer==HW_ID.SHIMMER_3)&&(fwId == FW_ID.LOGANDSTREAM))
				|| ((hwVer==HW_ID.SHIMMER_GQ_BLE)&&(fwId == FW_ID.GQ_BLE))
				|| (hwVer==HW_ID.SHIMMER_GQ_802154_NR)
				|| (hwVer==HW_ID.SHIMMER_GQ_802154_LR)
				|| (hwVer==HW_ID.SHIMMER_2R_GQ)
				|| (hwVer==HW_ID.SHIMMER_4_SDK)){
			return true;
		}
		return false;
	}
	
	public boolean isConfigViaUartSupported() {
		return isConfigViaUartSupported(getHardwareVersion(), getFirmwareIdentifier());
	}
	
	public static boolean isConfigViaUartSupported(int hwVer, int fwId) {
		if((hwVer==HW_ID.SHIMMER_3)
				||(hwVer==HW_ID.SHIMMER_GQ_802154_NR)
				||(hwVer==HW_ID.SHIMMER_GQ_802154_LR)
				||(hwVer==HW_ID.SHIMMER_2R_GQ)
				|| (hwVer==HW_ID.SHIMMER_4_SDK)){
			return true;
		}
		return false;
	}

	public boolean isSdCardAccessSupported() {
		return isSdCardAccessSupported(getHardwareVersion(), getFirmwareIdentifier());
	}
	
	public static boolean isSdCardAccessSupported(int hwVer, int fwId) {
		if (((hwVer==HW_ID.SHIMMER_3) && (fwId == FW_ID.SDLOG))
				|| ((hwVer==HW_ID.SHIMMER_3) && (fwId == FW_ID.LOGANDSTREAM))
				|| ((hwVer==HW_ID.SHIMMER_GQ_BLE) && (fwId == FW_ID.GQ_BLE))
				|| (hwVer==HW_ID.SHIMMER_GQ_802154_NR)
				|| (hwVer==HW_ID.SHIMMER_GQ_802154_LR)
				|| (hwVer==HW_ID.SHIMMER_2R_GQ)
				|| (hwVer==HW_ID.SHIMMER_4_SDK)
				){
			return true;
		}
		return false;
	}

	public boolean isCalibDumpSupported() {
		return isCalibDumpSupported(this);
	}
	
	public static boolean isCalibDumpSupported(ShimmerVerObject shimmerVerObject) {
		if((isVerCompatibleWith(shimmerVerObject, HW_ID.SHIMMER_3, FW_ID.LOGANDSTREAM, 0, 6, 7))
				||(isVerCompatibleWith(shimmerVerObject, HW_ID.SHIMMER_3, FW_ID.SDLOG, 0, 12, 6))){
			return true;
		}
		return false;
	}


	public boolean isShimmerGen2(){
		return isShimmerGen2(getHardwareVersion());
	}

	public static boolean isShimmerGen2(int hwVer){
		if (hwVer==HW_ID.SHIMMER_2 || hwVer==HW_ID.SHIMMER_2R){
			return true;
		}
		return false;
	}

	public boolean isShimmerGen3(){
		return isShimmer3Gen(getHardwareVersion(), getFirmwareIdentifier());
	}

	//TODO decide whether GQ belongs here or not
	public static boolean isShimmer3Gen(int hwVer, int fwId) {
		if((hwVer==HW_ID.SHIMMER_3 || hwVer==HW_ID.SHIMMER_GQ_BLE || hwVer==HW_ID.SHIMMER_GQ_802154_LR || hwVer==HW_ID.SHIMMER_GQ_802154_NR)
				&&((fwId==FW_ID.BTSTREAM)||(fwId==FW_ID.SDLOG)||(fwId==FW_ID.LOGANDSTREAM)||(fwId==FW_ID.GQ_BLE))){
			return true;
		}
		return false;
	}

	public boolean isShimmerGen4(){
		return isShimmer4Gen(getHardwareVersion());
	}

	public static boolean isShimmer4Gen(int hwVer){
		if(hwVer==HW_ID.SHIMMER_4_SDK){
			return true;
		}
		return false;
	}

	/**
	 * This needs to be performed before a check for Gen3/Gen4 etc. as they
	 * share some entries in common.
	 * 
	 * @return
	 */
	public boolean isShimmerGenGq() {
		return isShimmerGenGq(getHardwareVersion(), getFirmwareIdentifier());
	}
	
	/**
	 * This needs to be performed before a check for Gen3/Gen4 etc. as they
	 * share some entries in common.
	 * 
	 * @param hwVer
	 * @return
	 */
	public boolean isShimmerGenGq(int hwVer, int fwId) {
		if(((hwVer==HW_ID.SHIMMER_GQ_802154_LR) || (hwVer==HW_ID.SHIMMER_GQ_802154_NR) || (hwVer==HW_ID.SHIMMER_2R_GQ))
				&& (fwId==FW_ID.GQ_802154 || fwId==FW_ID.GQ_BLE)){
			return true;
		}
		return false;
	}

	public boolean isVerCompatibleWith(int hardwareVersion, int firmwareIdentifier, int firmwareVersionMajor, int firmwareVersionMinor, int firmwareVersionInternal) {
		return isVerCompatibleWith(this, hardwareVersion, firmwareIdentifier, firmwareVersionMajor, firmwareVersionMinor, firmwareVersionInternal);
	}
	
	public static boolean isVerCompatibleWith(ShimmerVerObject svo, int hardwareVersion, int firmwareIdentifier, int firmwareVersionMajor, int firmwareVersionMinor, int firmwareVersionInternal) {
		return UtilShimmer.compareVersions(svo.getFirmwareIdentifier(), svo.getFirmwareVersionMajor(), svo.getFirmwareVersionMinor(), svo.getFirmwareVersionInternal(),
				firmwareIdentifier, firmwareVersionMajor, firmwareVersionMinor, firmwareVersionInternal);
	}


}
