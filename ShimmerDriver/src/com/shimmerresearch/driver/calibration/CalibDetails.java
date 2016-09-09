package com.shimmerresearch.driver.calibration;

import java.awt.Image;
import java.io.Serializable;

import javax.swing.ImageIcon;

import org.apache.commons.lang3.ArrayUtils;

import com.shimmerresearch.driverUtilities.UtilShimmer;

public abstract class CalibDetails implements Serializable {
	
	/** * */
	private static final long serialVersionUID = 3071359258303179516L;

	public String mRangeString = "";
	public int mRangeValue = 0;
	public long mCalibTimeMs = 0;
	
	private static ImageIcon imgCalib_Read_Source_Unknown = new ImageIcon(new ImageIcon(CalibDetails.class.getResource("/question grey.png")).getImage().getScaledInstance(16, 16,  java.awt.Image.SCALE_SMOOTH));
	private static ImageIcon imgCalib_Read_SD_Header = new ImageIcon(new ImageIcon(CalibDetails.class.getResource("/sd grey.png")).getImage().getScaledInstance(10, 13,  java.awt.Image.SCALE_SMOOTH));
	private static ImageIcon imgCalib_Read_Legacy_BT_Command = new ImageIcon(new ImageIcon(CalibDetails.class.getResource("/old_BT_command.png")).getImage().getScaledInstance(17, 17,  java.awt.Image.SCALE_SMOOTH));
	private static ImageIcon imgCalib_Read_Infomem = new ImageIcon(new ImageIcon(CalibDetails.class.getResource("/memory_grey.png")).getImage().getScaledInstance(16, 16,  java.awt.Image.SCALE_SMOOTH));
	private static ImageIcon imgCalib_Read_Radio_Dump = new ImageIcon(new ImageIcon(CalibDetails.class.getResource("/radio_dump.png")).getImage().getScaledInstance(16, 16,  java.awt.Image.SCALE_SMOOTH));
	private static ImageIcon imgCalib_Read_File_Dump = new ImageIcon(new ImageIcon(CalibDetails.class.getResource("/file_dump.png")).getImage().getScaledInstance(16, 16,  java.awt.Image.SCALE_SMOOTH));
	private static ImageIcon imgCalib_Read_User_Modified = new ImageIcon(new ImageIcon(CalibDetails.class.getResource("/user_grey.png")).getImage().getScaledInstance(18, 18,  java.awt.Image.SCALE_SMOOTH));
	
	//In order of increasing priority
	public enum CALIB_READ_SOURCE{
		UNKNOWN(imgCalib_Read_Source_Unknown),
		SD_HEADER(imgCalib_Read_SD_Header),
		LEGACY_BT_COMMAND(imgCalib_Read_Legacy_BT_Command),
		INFOMEM(imgCalib_Read_Infomem),
		RADIO_DUMP(imgCalib_Read_Radio_Dump),
		FILE_DUMP(imgCalib_Read_File_Dump),
		USER_MODIFIED(imgCalib_Read_User_Modified);
		
	    private ImageIcon imgIcon = imgCalib_Read_Source_Unknown;
		
	    private CALIB_READ_SOURCE(final ImageIcon imgIcon) {
	        this.imgIcon = imgIcon;
	    }
	    
	    public ImageIcon getImage() {
	        return this.imgIcon;
	    }
	}
	
	public CALIB_READ_SOURCE mCalibReadSource = CALIB_READ_SOURCE.UNKNOWN;

	public abstract byte[] generateCalParamByteArray();
	public abstract void parseCalParamByteArray(byte[] bufferCalibrationParameters, CALIB_READ_SOURCE calibReadSource);
	public abstract void resetToDefaultParameters();
	
	public void setCalibTimeMs(long calibTimeMs){
		mCalibTimeMs = calibTimeMs;
	}
	
	public long getCalibTimeMs(){
		return mCalibTimeMs;
	}

	public String getCalibTimeParsed(){
		return UtilShimmer.convertMilliSecondsToDateString(getCalibTimeMs());
//		return UtilShimmer.convertMilliSecondsToHrMinSecString(mCalibTime);
	}

	public boolean isCalibTimeZero(){
		return getCalibTimeMs() == 0;
	}

	public byte[] generateCalibDump() {
		byte[] rangeBytes = new byte[1];
		rangeBytes[0] = (byte)(mRangeValue&0xFF);

		byte[] timestamp = UtilShimmer.convertMilliSecondsToShimmerRtcDataBytesLSB(getCalibTimeMs());
		byte[] bufferCalibParam = generateCalParamByteArray();
		byte[] calibLength = new byte[]{(byte) bufferCalibParam.length};
		
		byte[] returnArray = ArrayUtils.addAll(rangeBytes, calibLength);
		returnArray = ArrayUtils.addAll(returnArray, timestamp);
		returnArray = ArrayUtils.addAll(returnArray, bufferCalibParam);
		return returnArray;
	}

	public void parseCalibDump(byte[] calibTimeBytesTicks, byte[] bufferCalibrationParameters, CALIB_READ_SOURCE calibReadSource) {
		long calibTimeMs = UtilShimmer.convertShimmerRtcDataBytesToMilliSecondsLSB(calibTimeBytesTicks);
		
		// Parse in order of priority
		if(calibTimeMs>getCalibTimeMs() || calibReadSource.ordinal()>=getCalibReadSource().ordinal()){
			if(UtilShimmer.isAllFF(bufferCalibrationParameters)
					||UtilShimmer.isAllZeros(bufferCalibrationParameters)){
				return;
			}

			setCalibTimeMs(calibTimeMs);
			parseCalParamByteArray(bufferCalibrationParameters, calibReadSource);
		}
	}
	
	protected void setCalibReadSource(CALIB_READ_SOURCE calibReadSource) {
		mCalibReadSource = calibReadSource;
	}
	
	public CALIB_READ_SOURCE getCalibReadSource() {
		return mCalibReadSource;
	}
	
	public void resetToDefaultParametersCommon() {
		setCalibTimeMs(0);
//		setCalibReadSource(CALIB_READ_SOURCE.USER_MODIFIED);
	}

}
