package com.shimmerresearch.driverUtilities;

import java.io.File;
import java.io.Serializable;

/**
 * Holds the Shimmer's SD card capacity information which is obtained when a
 * Shimmer is docked
 * 
 * @author Mark Nolan
 *
 */
public class ShimmerSDCardDetails implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3210542276033436303L;
	
	protected long mDriveTotalSpace = 0; //total disk space in bytes.
	protected long mDriveUsableSpace = 0; ///unallocated / free disk space in bytes.
	protected long mDriveFreeSpace = 0; //unallocated / free disk space in bytes.
	protected long mDriveUsedSpace = 0;

	public ShimmerSDCardDetails() {
	}

	public ShimmerSDCardDetails(File drivePath){
		mDriveTotalSpace = drivePath.getTotalSpace(); //total disk space in bytes.
		mDriveUsableSpace = drivePath.getUsableSpace(); ///unallocated / free disk space in bytes.
		mDriveFreeSpace = drivePath.getFreeSpace(); //unallocated / free disk space in bytes.
		mDriveUsedSpace = mDriveTotalSpace - mDriveFreeSpace;
	}

	public long getDriveUsableSpace() {
		return mDriveUsableSpace;
	}

	public long getDriveFreeSpace() {
		return mDriveFreeSpace;
	}

	public String getDriveUsedSpaceParsed() {
		return spaceToString(mDriveUsedSpace);
	}

	public String getDriveTotalSpaceParsed() {
		return spaceToString(mDriveTotalSpace);
	}
	
	private String spaceToString(long space){
	    double spaceTotal = (double)space / 1024 / 1024 / 1024;
	    String spaceTotalTxt = " GB";
	    if (spaceTotal < 1.0) {
	        spaceTotal = spaceTotal * 1024;
	        spaceTotalTxt = " MB";
	    }
	    return String.format("%.2f", spaceTotal) + spaceTotalTxt; 
	}


}
