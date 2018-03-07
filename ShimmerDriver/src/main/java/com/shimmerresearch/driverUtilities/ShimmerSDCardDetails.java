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
	
	protected long mDriveTotalSpace = 0; //total disk space in bytes.
	protected long mDriveUsableSpace = 0; ///unallocated / free disk space in bytes.
	protected long mDriveFreeSpace = 0; //unallocated / free disk space in bytes.
	protected long mDriveUsedSpace = 0;
	
	protected boolean mFirstSdAccess = true;
	protected boolean mIsSDPresent = true;
	protected boolean mIsSDError = false;
	protected boolean mIsSDLogging = false;											// This is used to monitor whether the device is in sd log mode

	public ShimmerSDCardDetails() {
	}

	public ShimmerSDCardDetails(File drivePath){
		mDriveTotalSpace = drivePath.getTotalSpace(); //total disk space in bytes.
		mDriveUsableSpace = drivePath.getUsableSpace(); ///unallocated / free disk space in bytes.
		mDriveFreeSpace = drivePath.getFreeSpace(); //unallocated / free disk space in bytes.
		updateDriveUsedSpace();
	}

	public long getDriveUsableSpace() {
		return mDriveUsableSpace;
	}

	public long getDriveFreeSpace() {
		return mDriveFreeSpace;
	}

	public long getDriveTotalSpace() {
		return mDriveTotalSpace;
	}

	public String getDriveTotalSpaceParsed() {
		if(mDriveTotalSpace==0){
			return ShimmerDevice.STRING_CONSTANT_NOT_AVAILABLE;
		} else {
			return spaceToString(mDriveTotalSpace);
		}
	}

	private void updateDriveUsedSpace() {
		mDriveUsedSpace = mDriveTotalSpace - mDriveFreeSpace;
	}

	public long getDriveUsedSpace() {
		return mDriveUsedSpace;
	}

	public long getDriveUsedSpaceKB() {
		return mDriveUsedSpace/1024;
	}

	public String getDriveUsedSpaceParsed() {
		if(mDriveUsedSpace==0){
			return ShimmerDevice.STRING_CONSTANT_NOT_AVAILABLE;
		} else {
			return spaceToString(mDriveUsedSpace);
		}
	}

	public void setDriveUsedSpaceKB(long driveUsedSpace) {
		setDriveUsedSpace(driveUsedSpace*1024);
	}

	public void setDriveUsedSpace(long driveUsedSpace) {
		mDriveUsedSpace = driveUsedSpace;
		
		System.err.println("Drive used space received = " + driveUsedSpace + " Bytes" + "\tor\t" + spaceToString(driveUsedSpace));
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
