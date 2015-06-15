package com.shimmerresearch.driver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;

import com.shimmerresearch.driver.ShimmerVerDetails.*;

/**
 * Object to processes and stores the information related to a firmware image
 * file. It tries to identify what hardware it belongs to, what type of firmware
 * it is and what the version number is.
 * 
 * @author Mark Nolan
 *
 */
public class FwImageVersionDetails {

	public File mFile;
	public String mFileName = "";
	public int mHardwareVersion = -1;
	public int mFirmwareIdentifier = -1;
	public int mFirmwareVersionMajor = -1;
	public int mFirmwareVersionMinor = -1;
	public int mFirmwareVersionInternal = -1;
	public boolean mIsLatestSampleFw = false;
	public boolean mSuccess = false;
	public long mFileLength = 0;
	public String mFileLengthParsed = "";
	public FileTime mFileCreationTime;
	public String mFileCreationTimeParsed;
	public FileTime mFileModifiedTime;
	public String mFileModifiedTimeParsed;
	
	public final static int FW_ID_UNKNOWN = -1;
	
	private final static class FW_STRING {
		private final static String SHIMMER3_BTSTREAM = "BtStream_Shimmer3";
		private final static String SHIMMER3_SDLOG = "SDLog_Shimmer3";
		private final static String SHIMMER3_LOGANDSTREAM = "LogAndStream_Shimmer3";
		private final static String SHIMMER3_GQ_GSR = "GQ_GSR";
		private final static String SMARTDOCK15U_REV2 = "CBase15_SR41-2";
		private final static String SMARTDOCK15U_REV4 = "Consensys_Base15";
		private final static String SMARTDOCK6 = "Consensys_Base6";
		private final static String GPIO_TEST = "S3_GPIO_EXTERNAL_Test";
	}
	
	public FwImageVersionDetails(File file, boolean isLatestSampleFw) {
		mSuccess = checkIfTiMsp430TxtFwFile(file);
		if(mSuccess){
			mSuccess = parseFileName(file);
			mIsLatestSampleFw = isLatestSampleFw;
		}
	}
	
	/**
	 * Performs a number of checks to verify that it is a valid MSP430
	 * firmware file.
	 * 
	 * @param file
	 * @return
	 */
	private static boolean checkIfTiMsp430TxtFwFile(File file) {
		//Check if it is a file
		if(!file.exists() || file.isDirectory()) {
			return false;
		}

		//Check if file extension is ".txt"
		String extension = "";
		int i = file.getName().lastIndexOf('.');
		if (i >= 0) {
		    extension = file.getName().substring(i+1);
		}
		if(!extension.equals("txt")){
			return false;
		}
			
		// Check if first line of file contains the MSP430 information memory
		// address "@5c00"
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
		    String line = br.readLine();
		    if(line==null){
				return false;
		    }
		    if(line.equals("@5c00")){
		    	return true;
		    }
		    else {
		    	return false;
		    }
		} catch (FileNotFoundException e) {
	    	return false;
		} catch (IOException e) {
	    	return false;
		}
	}

	/**
	 * Parse the firmware file name to try and identify what firmware and
	 * version it is.
	 * 
	 * @param file
	 * @return
	 */
	private boolean parseFileName(File file) {
		mFile = file;
		if(file.isFile()) {
			String fileNameWithExtension = mFile.getName();

			try {
				
				mFileName = fileNameWithExtension.substring(0, fileNameWithExtension.lastIndexOf("."));
				
				if(mFileName.matches("(.*)v(.*)\\.(.*)\\.(.*)")) {				
					String versionId = mFileName.substring(mFileName.lastIndexOf("v")+1, mFileName.length());
					String[] versionIdArray = versionId.split("\\D+");
					mFirmwareVersionMajor = Integer.parseInt(versionIdArray[0]);
					mFirmwareVersionMinor = Integer.parseInt(versionIdArray[1]);
					mFirmwareVersionInternal = Integer.parseInt(versionIdArray[2]);
					
					if(fileNameWithExtension.contains(FW_STRING.SHIMMER3_BTSTREAM)) {
						mHardwareVersion = HW_ID.SHIMMER_3;
						mFirmwareIdentifier = FW_ID.SHIMMER3.BTSTREAM;
					}
					else if(fileNameWithExtension.contains(FW_STRING.SHIMMER3_SDLOG)) {
						mHardwareVersion = HW_ID.SHIMMER_3;
						mFirmwareIdentifier = FW_ID.SHIMMER3.SDLOG;
					}
					else if(fileNameWithExtension.contains(FW_STRING.SHIMMER3_LOGANDSTREAM)) {
						mHardwareVersion = HW_ID.SHIMMER_3;
						mFirmwareIdentifier = FW_ID.SHIMMER3.LOGANDSTREAM;
					}
					else if(fileNameWithExtension.contains(FW_STRING.SHIMMER3_GQ_GSR)) {
						mHardwareVersion = HW_ID.SHIMMER_3;
						mFirmwareIdentifier = FW_ID.SHIMMER3.GQ_GSR;
					}
					else if(fileNameWithExtension.contains(FW_STRING.SMARTDOCK15U_REV2)) {
						mHardwareVersion = HW_ID_SR_CODES.BASE15U;
						mFirmwareIdentifier = FW_ID.BASES.BASE15U_REV2;
					}
					else if(fileNameWithExtension.contains(FW_STRING.SMARTDOCK15U_REV4)) {
						mHardwareVersion = HW_ID_SR_CODES.BASE15U;
						mFirmwareIdentifier = FW_ID.BASES.BASE15U_REV4;
					}
					else if(fileNameWithExtension.contains(FW_STRING.SMARTDOCK6)) {
						mHardwareVersion = HW_ID_SR_CODES.BASE6U;
						mFirmwareIdentifier = FW_ID.BASES.BASE6U;
					}
					else {
						//Else assume Shimmer3
						mHardwareVersion = FW_ID_UNKNOWN;
						mFirmwareIdentifier = FW_ID_UNKNOWN;
					}
				}
				else if(fileNameWithExtension.equals(FW_STRING.GPIO_TEST)) {
					System.err.println(mFileName);
					mHardwareVersion = HW_ID.SHIMMER_3;
					mFirmwareIdentifier = FW_ID.SHIMMER3.GPIO_TEST;
				}
				else {
					mHardwareVersion = FW_ID_UNKNOWN;
					mFirmwareIdentifier = FW_ID_UNKNOWN;
				}

//				String path = mFile.getAbsolutePath();
				
				Path filePath = file.toPath();
				BasicFileAttributes attr = Files.readAttributes(filePath, BasicFileAttributes.class);
	
				mFileCreationTime = attr.creationTime();
				mFileCreationTimeParsed = Util.fromMilToDateExcelCompatible(Long.toString(mFileCreationTime.toMillis()), false);
				mFileModifiedTime = attr.lastModifiedTime();
				mFileModifiedTimeParsed = Util.fromMilToDateExcelCompatible(Long.toString(mFileModifiedTime.toMillis()), false);
				mFileLength = attr.size();
				mFileLengthParsed = String.valueOf(Math.round(attr.size()/1000.0)) + "kB";

				return true;
			}
			catch (Exception e) {
				return false;
			}
		}
		else {
			return false;
		}
	}
	
}
