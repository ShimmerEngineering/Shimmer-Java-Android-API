package com.shimmerresearch.driverUtilities;

import java.io.File;
import java.io.Serializable;

import com.shimmerresearch.driver.ShimmerDevice;

/**
 * Holds the Shimmer's SD card capacity information which is obtained when a
 * Shimmer is docked
 * 
 * @author Mark Nolan
 *
 */
public class ShimmerSDCardDetails implements Serializable {
	
	private static final long serialVersionUID = -3210542276033436303L;

	public static final long DRIVE_SPACE_USED_CHANGE_TIMEOUT_MS = 30000;

	protected long mDriveTotalSpaceBytes = 0; //total disk space in bytes.
	protected long mDriveUsableSpaceBytes = 0; ///unallocated / free disk space in bytes.
	protected long mDriveFreeSpaceBytes = 0; //unallocated / free disk space in bytes.
	protected long mDriveUsedSpaceBytes = 0;
	
	protected boolean mFirstSdAccess = true;
	protected boolean mIsSDPresent = true;
	protected boolean mIsSDError = false;
	protected boolean mIsSDLogging = false;											// This is used to monitor whether the device is in sd log mode

	public long mDriveUsedSpaceLastTimeChanged = 0;

	public ShimmerSDCardDetails() {
		
	}

	public ShimmerSDCardDetails(File drivePath){
		setDriveUsedSpaceTotal(drivePath.getTotalSpace()); //total disk space in bytes.
		mDriveUsableSpaceBytes = drivePath.getUsableSpace(); ///unallocated / free disk space in bytes.
		mDriveFreeSpaceBytes = drivePath.getFreeSpace(); //unallocated / free disk space in bytes.
		updateDriveUsedSpace();
	}

	public long getDriveUsableSpace() {
		return mDriveUsableSpaceBytes;
	}

	public long getDriveFreeSpace() {
		return mDriveFreeSpaceBytes;
	}

	public long getDriveTotalSpace() {
		return mDriveTotalSpaceBytes;
	}

	public String getDriveTotalSpaceParsed() {
		if(mDriveTotalSpaceBytes==0){
			return ShimmerDevice.STRING_CONSTANT_NOT_AVAILABLE;
		} else {
			return spaceToString(mDriveTotalSpaceBytes);
		}
	}
	
	public void setDriveUsedSpaceTotal(long driveUsedTotal) {
		mDriveTotalSpaceBytes = driveUsedTotal;
	}

	public void setDriveUsedSpaceTotalKB(long driveUsedTotalKB) {
		mDriveTotalSpaceBytes = driveUsedTotalKB*1024;
	}

	private void updateDriveUsedSpace() {
		mDriveUsedSpaceBytes = mDriveTotalSpaceBytes - mDriveFreeSpaceBytes;
	}

	public long getDriveUsedSpace() {
		return mDriveUsedSpaceBytes;
	}

	public long getDriveUsedSpaceKB() {
		return mDriveUsedSpaceBytes/1024;
	}

	public String getDriveUsedSpaceParsed() {
		if(mDriveUsedSpaceBytes==0){
			return ShimmerDevice.STRING_CONSTANT_NOT_AVAILABLE;
		} else {
			return spaceToString(mDriveUsedSpaceBytes);
		}
	}

	public void setDriveUsedSpaceKB(long driveUsedSpace) {
		setDriveUsedSpaceBytes(driveUsedSpace*1024);
	}

	public void setDriveUsedSpaceBytes(long driveUsedSpace) {
		if(driveUsedSpace!=mDriveUsedSpaceBytes) {
			mDriveUsedSpaceLastTimeChanged = System.currentTimeMillis();
		}
		mDriveUsedSpaceBytes = driveUsedSpace;
	}
	
	public boolean isFirstSdAccess() {
		return mFirstSdAccess;
	}

	public void setFirstSdAccess(boolean state) {
		this.mFirstSdAccess = state;
	}

	public boolean isSDError() {
		return mIsSDError;
	}

	public void setIsSDError(boolean state) {
		this.mIsSDError = state;
	}

	public boolean isSDPresent() {
		return mIsSDPresent;
	}

	public void setIsSDPresent(boolean state) {
		this.mIsSDPresent = state;
	}

	public boolean isSDLogging(){
		return mIsSDLogging;
	}	

	public void setIsSDLogging(boolean state){
		mIsSDLogging = state;
	}	

    public boolean isSDSpaceIncreasing() {
    	return ((System.currentTimeMillis()-mDriveUsedSpaceLastTimeChanged)<DRIVE_SPACE_USED_CHANGE_TIMEOUT_MS);
	}

	public static String spaceToString(long spaceBytes){
	    double spaceTotal = (double)spaceBytes / 1024 / 1024 / 1024;
	    String spaceTotalTxt = " GB";
    	if (spaceTotal < 1.0) {
	        spaceTotal = spaceTotal * 1024;
	        spaceTotalTxt = " MB";
	    }
	    return String.format("%.2f", spaceTotal) + spaceTotalTxt; 
	}

	public void setFirstDockRead() {
		setFirstSdAccess(true);
		setIsSDError(false);
	}

	/** Used for Device Emulation
	 * @return
	 */
	public byte[] generateDriveStatusBytes() {
		byte[] sdCapacityStatus = new byte[4];

		long driveSpaceUsed = getDriveUsedSpaceKB();
		sdCapacityStatus[0] = (byte) ((driveSpaceUsed>>0)&0xFF); 
		sdCapacityStatus[1] = (byte) ((driveSpaceUsed>>8)&0xFF); 
		sdCapacityStatus[2] = (byte) ((driveSpaceUsed>>16)&0xFF); 
		sdCapacityStatus[3] = (byte) ((driveSpaceUsed>>24)&0xFF); 
		
		return sdCapacityStatus;
	}

}
