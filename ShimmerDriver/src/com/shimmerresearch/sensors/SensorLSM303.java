package com.shimmerresearch.sensors;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.ArrayUtils;

import com.shimmerresearch.bluetooth.BtCommandDetails;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.FormatCluster;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driver.Configuration.CHANNEL_UNITS;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driver.Configuration.Shimmer3.CompatibilityInfoForMaps;
import com.shimmerresearch.driver.ShimmerDevice.DatabaseConfigHandle;
import com.shimmerresearch.driver.calibration.CalibDetails;
import com.shimmerresearch.driver.calibration.CalibDetailsKinematic;
import com.shimmerresearch.driver.calibration.OldCalDetails;
import com.shimmerresearch.driver.calibration.UtilCalibration;
import com.shimmerresearch.driver.calibration.CalibDetails.CALIB_READ_SOURCE;
import com.shimmerresearch.driverUtilities.ChannelDetails;
import com.shimmerresearch.driverUtilities.ConfigOptionDetailsSensor;
import com.shimmerresearch.driverUtilities.SensorDetails;
import com.shimmerresearch.driverUtilities.SensorDetailsRef;
import com.shimmerresearch.driverUtilities.SensorGroupingDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_ENDIAN;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_TYPE;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_TYPE;

/**
 * Sensor class for the LSM303DLHC combined Accelerometer and Magnetometer 
 * (commonly referred to as the wide-range accel in Shimmer literature)
 * 
 * Accelerometer: one 12-bit reading (left-justified) per axis, LSB. 
 * Magnetometer: one 12-bit reading (right-justified) per axis, MSB.
 * 
 * @author Ruud Stolk
 * @author Mark Nolan
 * 
 */
public class SensorLSM303 extends AbstractSensor{	
	/**
	 * Sensorclass for LSM303 - digital/wide-range accelerometer + magnetometer 
	 *  
	 *  @param svo
	 * * */  
	private static final long serialVersionUID = -2119834127313796684L;

	//--------- Sensor specific variables start --------------	
	protected boolean mLowPowerAccelWR = false;
	protected boolean mHighResAccelWR = true;
	protected boolean mLowPowerMag = false;
	
	protected int mAccelRange = 0;
	protected int mLSM303DigitalAccelRate = 0;
	protected int mMagRange = 1;
	protected int mLSM303MagRate = 4;
	
	// ----------   Wide-range accel start ---------------
	public static final double[][] AlignmentMatrixWideRangeAccelShimmer3 = {{-1,0,0},{0,1,0},{0,0,-1}};	
	public static final double[][] OffsetVectorWideRangeAccelShimmer3 = {{0},{0},{0}};	
	public static final double[][] SensitivityMatrixWideRangeAccel2gShimmer3 = {{1631,0,0},{0,1631,0},{0,0,1631}};
	public static final double[][] SensitivityMatrixWideRangeAccel4gShimmer3 = {{815,0,0},{0,815,0},{0,0,815}};
	public static final double[][] SensitivityMatrixWideRangeAccel8gShimmer3 = {{408,0,0},{0,408,0},{0,0,408}};
	public static final double[][] SensitivityMatrixWideRangeAccel16gShimmer3 = {{135,0,0},{0,135,0},{0,0,135}};
	
    public static final Map<String, OldCalDetails> mOldCalRangeMap;
    static {
        Map<String, OldCalDetails> aMap = new LinkedHashMap<String, OldCalDetails>();
        aMap.put("accel_wr_2g", new OldCalDetails("accel_wr_2g", Configuration.Shimmer3.SensorMapKey.SHIMMER_LSM303DLHC_ACCEL, 0));
        aMap.put("accel_wr_4g", new OldCalDetails("accel_wr_4g", Configuration.Shimmer3.SensorMapKey.SHIMMER_LSM303DLHC_ACCEL, 1));
        aMap.put("accel_wr_8g", new OldCalDetails("accel_wr_8g", Configuration.Shimmer3.SensorMapKey.SHIMMER_LSM303DLHC_ACCEL, 2));
        aMap.put("accel_wr_16g", new OldCalDetails("accel_wr_16g", Configuration.Shimmer3.SensorMapKey.SHIMMER_LSM303DLHC_ACCEL, 3));
        
        aMap.put("mag_13ga", new OldCalDetails("mag_13ga", Configuration.Shimmer3.SensorMapKey.SHIMMER_LSM303DLHC_MAG, 1));
        aMap.put("mag_19ga", new OldCalDetails("mag_19ga", Configuration.Shimmer3.SensorMapKey.SHIMMER_LSM303DLHC_MAG, 2));
        aMap.put("mag_25ga", new OldCalDetails("mag_25ga", Configuration.Shimmer3.SensorMapKey.SHIMMER_LSM303DLHC_MAG, 3));
        aMap.put("mag_4ga",  new OldCalDetails("mag_4ga",  Configuration.Shimmer3.SensorMapKey.SHIMMER_LSM303DLHC_MAG, 4));
        aMap.put("mag_47ga", new OldCalDetails("mag_47ga", Configuration.Shimmer3.SensorMapKey.SHIMMER_LSM303DLHC_MAG, 5));
        aMap.put("mag_56ga", new OldCalDetails("mag_56ga", Configuration.Shimmer3.SensorMapKey.SHIMMER_LSM303DLHC_MAG, 6));
        aMap.put("mag_81ga", new OldCalDetails("mag_81ga", Configuration.Shimmer3.SensorMapKey.SHIMMER_LSM303DLHC_MAG, 7));

        mOldCalRangeMap = Collections.unmodifiableMap(aMap);
    }

	
	private CalibDetailsKinematic calibDetailsAccelWr2g = new CalibDetailsKinematic(
			ListofLSM303DLHCAccelRangeConfigValues[0],
			ListofAccelRange[0],
			AlignmentMatrixWideRangeAccelShimmer3, 
			SensitivityMatrixWideRangeAccel2gShimmer3, 
			OffsetVectorWideRangeAccelShimmer3);
	private CalibDetailsKinematic calibDetailsAccelWr4g = new CalibDetailsKinematic(
			ListofLSM303DLHCAccelRangeConfigValues[1], 
			ListofAccelRange[1],
			AlignmentMatrixWideRangeAccelShimmer3,
			SensitivityMatrixWideRangeAccel4gShimmer3, 
			OffsetVectorWideRangeAccelShimmer3);
	private CalibDetailsKinematic calibDetailsAccelWr8g = new CalibDetailsKinematic(
			ListofLSM303DLHCAccelRangeConfigValues[2], 
			ListofAccelRange[2],
			AlignmentMatrixWideRangeAccelShimmer3, 
			SensitivityMatrixWideRangeAccel8gShimmer3, 
			OffsetVectorWideRangeAccelShimmer3);
	private CalibDetailsKinematic calibDetailsAccelWr16g = new CalibDetailsKinematic(
			ListofLSM303DLHCAccelRangeConfigValues[3], 
			ListofAccelRange[3],
			AlignmentMatrixWideRangeAccelShimmer3,
			SensitivityMatrixWideRangeAccel16gShimmer3, 
			OffsetVectorWideRangeAccelShimmer3);
	
	public CalibDetailsKinematic mCurrentCalibDetailsAccelWr = calibDetailsAccelWr2g;
	// ----------   Wide-range accel end ---------------

	// ----------   Mag start ---------------
	public static final double[][] AlignmentMatrixMagShimmer3 = {{-1,0,0},{0,1,0},{0,0,-1}}; 				
	public static final double[][] OffsetVectorMagShimmer3 = {{0},{0},{0}};	
	public static final double[][] SensitivityMatrixMag1p3GaShimmer3 = {{1100,0,0},{0,1100,0},{0,0,980}};
	public static final double[][] SensitivityMatrixMag1p9GaShimmer3 = {{855,0,0},{0,855,0},{0,0,760}};
	public static final double[][] SensitivityMatrixMag2p5GaShimmer3 = {{670,0,0},{0,670,0},{0,0,600}};
	public static final double[][] SensitivityMatrixMag4GaShimmer3 = {{450,0,0},{0,450,0},{0,0,400}};
	public static final double[][] SensitivityMatrixMag4p7GaShimmer3 = {{400,0,0},{0,400,0},{0,0,355}};
	public static final double[][] SensitivityMatrixMag5p6GaShimmer3 = {{330,0,0},{0,330,0},{0,0,295}};
	public static final double[][] SensitivityMatrixMag8p1GaShimmer3 = {{230,0,0},{0,230,0},{0,0,205}};

	private CalibDetailsKinematic calibDetailsMag1p3 = new CalibDetailsKinematic(
			ListofMagRangeConfigValues[0],
			ListofMagRange[0],
			AlignmentMatrixMagShimmer3,
			SensitivityMatrixMag1p3GaShimmer3,
			OffsetVectorMagShimmer3);
	private CalibDetailsKinematic calibDetailsMag1p9 = new CalibDetailsKinematic(
			ListofMagRangeConfigValues[1],
			ListofMagRange[1],
			AlignmentMatrixMagShimmer3, 
			SensitivityMatrixMag1p9GaShimmer3,
			OffsetVectorMagShimmer3);
	private CalibDetailsKinematic calibDetailsMag2p5 = new CalibDetailsKinematic(
			ListofMagRangeConfigValues[2], 
			ListofMagRange[2],
			AlignmentMatrixMagShimmer3,
			SensitivityMatrixMag2p5GaShimmer3, 
			OffsetVectorMagShimmer3);
	private CalibDetailsKinematic calibDetailsMag4p0 = new CalibDetailsKinematic(
			ListofMagRangeConfigValues[3],
			ListofMagRange[3],
			AlignmentMatrixMagShimmer3,
			SensitivityMatrixMag4GaShimmer3,
			OffsetVectorMagShimmer3);
	private CalibDetailsKinematic calibDetailsMag4p7 = new CalibDetailsKinematic(
			ListofMagRangeConfigValues[4],
			ListofMagRange[4],
			AlignmentMatrixMagShimmer3, 
			SensitivityMatrixMag4p7GaShimmer3,
			OffsetVectorMagShimmer3);
	private CalibDetailsKinematic calibDetailsMag5p6 = new CalibDetailsKinematic(
			ListofMagRangeConfigValues[5],
			ListofMagRange[5],
			AlignmentMatrixMagShimmer3, 
			SensitivityMatrixMag5p6GaShimmer3,
			OffsetVectorMagShimmer3);
	private CalibDetailsKinematic calibDetailsMag8p1 = new CalibDetailsKinematic(
			ListofMagRangeConfigValues[6],
			ListofMagRange[6],
			AlignmentMatrixMagShimmer3, 
			SensitivityMatrixMag8p1GaShimmer3, 
			OffsetVectorMagShimmer3);
	
	public CalibDetailsKinematic mCurrentCalibDetailsMag = calibDetailsMag1p3;

	// ----------   Mag end ---------------
	
	public class GuiLabelConfig{
		public static final String LSM303DLHC_ACCEL_RATE = "Wide Range Accel Rate";  
		public static final String LSM303DLHC_ACCEL_RANGE = "Wide Range Accel Range"; 
				
		public static final String LSM303DLHC_MAG_RANGE = "Mag Range";
		public static final String LSM303DLHC_MAG_RATE = "Mag Rate";
		
		public static final String LSM303DLHC_ACCEL_LPM = "Wide Range Accel Low-Power Mode"; 
		public static final String LSM303DLHC_MAG_LPM = "Mag Low-Power Mode";

		public static final String LSM303DLHC_ACCEL_DEFAULT_CALIB = "Wide Range Accel Default Calibration";
		public static final String LSM303DLHC_MAG_DEFAULT_CALIB = "Mag Default Calibration";

		//NEW
		public static final String LSM303DLHC_ACCEL_CALIB_PARAM = "Wide Range Accel Calibration Details";
		public static final String LSM303DLHC_ACCEL_VALID_CALIB = "Wide Range Accel Valid Calibration";
		public static final String LSM303DLHC_MAG_CALIB_PARAM = "Mag Calibration Details";
		public static final String LSM303DLHC_MAG_VALID_CALIB = "Mag Valid Calibration";
	}
	

	public class GuiLabelSensors{
		public static final String ACCEL_WR = "Wide-Range Accelerometer"; 
		public static final String MAG = "Magnetometer"; 
	}

	
	public class GuiLabelSensorTiles{
		public static final String MAG = GuiLabelSensors.MAG;
		public static final String WIDE_RANGE_ACCEL = GuiLabelSensors.ACCEL_WR;
	}
	
	
	public static class DatabaseChannelHandles{
		public static final String WR_ACC_X = "LSM303DLHC_ACC_X";
		public static final String WR_ACC_Y = "LSM303DLHC_ACC_Y";
		public static final String WR_ACC_Z = "LSM303DLHC_ACC_Z";
		public static final String MAG_X = "LSM303DLHC_MAG_X";
		public static final String MAG_Y = "LSM303DLHC_MAG_Y";
		public static final String MAG_Z = "LSM303DLHC_MAG_Z";
	}
	
	public static final class DatabaseConfigHandle{
		public static final String MAG_RANGE = "LSM303DLHC_Mag_Range";
		public static final String MAG_RATE = "LSM303DLHC_Mag_Rate";
		public static final String MAG = "LSM303DLHC_Mag";
		
		public static final String WR_ACC = "LSM303DLHC_Acc";
		public static final String WR_ACC_RATE = "LSM303DLHC_Acc_Rate";
		public static final String WR_ACC_RANGE = "LSM303DLHC_Acc_Range";
		
		public static final String WR_ACC_LPM = "LSM303DLHC_Acc_LPM";
		public static final String WR_ACC_HRM = "LSM303DLHC_Acc_HRM";
		
		public static final String WR_ACC_CALIB_TIME = "LSM303DLHC_Acc_Calib_Time";
		public static final String WR_ACC_OFFSET_X = "LSM303DLHC_Acc_Offset_X";
		public static final String WR_ACC_OFFSET_Y = "LSM303DLHC_Acc_Offset_Y";
		public static final String WR_ACC_OFFSET_Z = "LSM303DLHC_Acc_Offset_Z";
		public static final String WR_ACC_GAIN_X = "LSM303DLHC_Acc_Gain_X";
		public static final String WR_ACC_GAIN_Y = "LSM303DLHC_Acc_Gain_Y";
		public static final String WR_ACC_GAIN_Z = "LSM303DLHC_Acc_Gain_Z";
		public static final String WR_ACC_ALIGN_XX = "LSM303DLHC_Acc_Align_XX";
		public static final String WR_ACC_ALIGN_XY = "LSM303DLHC_Acc_Align_XY";
		public static final String WR_ACC_ALIGN_XZ = "LSM303DLHC_Acc_Align_XZ";
		public static final String WR_ACC_ALIGN_YX = "LSM303DLHC_Acc_Align_YX";
		public static final String WR_ACC_ALIGN_YY = "LSM303DLHC_Acc_Align_YY";
		public static final String WR_ACC_ALIGN_YZ = "LSM303DLHC_Acc_Align_YZ";
		public static final String WR_ACC_ALIGN_ZX = "LSM303DLHC_Acc_Align_ZX";
		public static final String WR_ACC_ALIGN_ZY = "LSM303DLHC_Acc_Align_ZY";
		public static final String WR_ACC_ALIGN_ZZ = "LSM303DLHC_Acc_Align_ZZ";
		
		public static final String MAG_CALIB_TIME = "LSM303DLHC_Mag_Calib_Time";
		public static final String MAG_OFFSET_X = "LSM303DLHC_Mag_Offset_X";
		public static final String MAG_OFFSET_Y = "LSM303DLHC_Mag_Offset_Y";
		public static final String MAG_OFFSET_Z = "LSM303DLHC_Mag_Offset_Z";
		public static final String MAG_GAIN_X = "LSM303DLHC_Mag_Gain_X";
		public static final String MAG_GAIN_Y = "LSM303DLHC_Mag_Gain_Y";
		public static final String MAG_GAIN_Z = "LSM303DLHC_Mag_Gain_Z";
		public static final String MAG_ALIGN_XX = "LSM303DLHC_Mag_Align_XX";
		public static final String MAG_ALIGN_XY = "LSM303DLHC_Mag_Align_XY";
		public static final String MAG_ALIGN_XZ = "LSM303DLHC_Mag_Align_XZ";
		public static final String MAG_ALIGN_YX = "LSM303DLHC_Mag_Align_YX";
		public static final String MAG_ALIGN_YY = "LSM303DLHC_Mag_Align_YY";
		public static final String MAG_ALIGN_YZ = "LSM303DLHC_Mag_Align_YZ";
		public static final String MAG_ALIGN_ZX = "LSM303DLHC_Mag_Align_ZX";
		public static final String MAG_ALIGN_ZY = "LSM303DLHC_Mag_Align_ZY";
		public static final String MAG_ALIGN_ZZ = "LSM303DLHC_Mag_Align_ZZ";
		

		public static final List<String> LIST_OF_CALIB_HANDLES_MAG = Arrays.asList(
				DatabaseConfigHandle.MAG_OFFSET_X, DatabaseConfigHandle.MAG_OFFSET_Y, DatabaseConfigHandle.MAG_OFFSET_Z,
				DatabaseConfigHandle.MAG_GAIN_X, DatabaseConfigHandle.MAG_GAIN_Y, DatabaseConfigHandle.MAG_GAIN_Z,
				DatabaseConfigHandle.MAG_ALIGN_XX, DatabaseConfigHandle.MAG_ALIGN_XY, DatabaseConfigHandle.MAG_ALIGN_XZ,
				DatabaseConfigHandle.MAG_ALIGN_YX, DatabaseConfigHandle.MAG_ALIGN_YY, DatabaseConfigHandle.MAG_ALIGN_YZ,
				DatabaseConfigHandle.MAG_ALIGN_ZX, DatabaseConfigHandle.MAG_ALIGN_ZY, DatabaseConfigHandle.MAG_ALIGN_ZZ);
		
		public static final List<String> LIST_OF_CALIB_HANDLES_WR_ACCEL = Arrays.asList(
				DatabaseConfigHandle.WR_ACC_OFFSET_X, DatabaseConfigHandle.WR_ACC_OFFSET_Y, DatabaseConfigHandle.WR_ACC_OFFSET_Z,
				DatabaseConfigHandle.WR_ACC_GAIN_X, DatabaseConfigHandle.WR_ACC_GAIN_Y, DatabaseConfigHandle.WR_ACC_GAIN_Z,
				DatabaseConfigHandle.WR_ACC_ALIGN_XX, DatabaseConfigHandle.WR_ACC_ALIGN_XY, DatabaseConfigHandle.WR_ACC_ALIGN_XZ,
				DatabaseConfigHandle.WR_ACC_ALIGN_YX, DatabaseConfigHandle.WR_ACC_ALIGN_YY, DatabaseConfigHandle.WR_ACC_ALIGN_YZ,
				DatabaseConfigHandle.WR_ACC_ALIGN_ZX, DatabaseConfigHandle.WR_ACC_ALIGN_ZY, DatabaseConfigHandle.WR_ACC_ALIGN_ZZ);
	}
	
	
	public static class ObjectClusterSensorName{
		public static  String ACCEL_WR_X = "Accel_WR_X";
		public static  String ACCEL_WR_Y = "Accel_WR_Y";
		public static  String ACCEL_WR_Z= "Accel_WR_Z";
		
		public static  String MAG_X = "Mag_X";
		public static  String MAG_Y = "Mag_Y";
		public static  String MAG_Z = "Mag_Z";		
	}
	//--------- Sensor specific variables end --------------
	
	
	//--------- Bluetooth commands start --------------
	public static final byte SET_ACCEL_SENSITIVITY_COMMAND    		= (byte) 0x09;
	public static final byte ACCEL_SENSITIVITY_RESPONSE       		= (byte) 0x0A;
	public static final byte GET_ACCEL_SENSITIVITY_COMMAND    		= (byte) 0x0B;
	
	public static final byte SET_MAG_CALIBRATION_COMMAND      		= (byte) 0x17;
	public static final byte MAG_CALIBRATION_RESPONSE         		= (byte) 0x18;
	public static final byte GET_MAG_CALIBRATION_COMMAND      		= (byte) 0x19;
	
	public static final byte SET_LSM303DLHC_ACCEL_CALIBRATION_COMMAND = (byte) 0x1A;
	public static final byte LSM303DLHC_ACCEL_CALIBRATION_RESPONSE 	= (byte) 0x1B;
	public static final byte GET_LSM303DLHC_ACCEL_CALIBRATION_COMMAND = (byte) 0x1C;
	
	public static final byte SET_MAG_GAIN_COMMAND             		= (byte) 0x37;
	public static final byte MAG_GAIN_RESPONSE                		= (byte) 0x38;
	public static final byte GET_MAG_GAIN_COMMAND             		= (byte) 0x39;
	
	public static final byte SET_MAG_SAMPLING_RATE_COMMAND    		= (byte) 0x3A;
	public static final byte MAG_SAMPLING_RATE_RESPONSE       		= (byte) 0x3B;
	public static final byte GET_MAG_SAMPLING_RATE_COMMAND    		= (byte) 0x3C;
	
	public static final byte SET_ACCEL_SAMPLING_RATE_COMMAND  		= (byte) 0x40;
	public static final byte ACCEL_SAMPLING_RATE_RESPONSE  			= (byte) 0x41;
	public static final byte GET_ACCEL_SAMPLING_RATE_COMMAND  		= (byte) 0x42;
	
	public static final byte SET_LSM303DLHC_ACCEL_LPMODE_COMMAND 	= (byte) 0x43;
	public static final byte LSM303DLHC_ACCEL_LPMODE_RESPONSE		= (byte) 0x44;
	public static final byte GET_LSM303DLHC_ACCEL_LPMODE_COMMAND 	= (byte) 0x45;
	
	public static final byte SET_LSM303DLHC_ACCEL_HRMODE_COMMAND	= (byte) 0x46;
	public static final byte LSM303DLHC_ACCEL_HRMODE_RESPONSE		= (byte) 0x47;
	public static final byte GET_LSM303DLHC_ACCEL_HRMODE_COMMAND 	= (byte) 0x48;
	
    public static final Map<Byte, BtCommandDetails> mBtGetCommandMap;
    static {
        Map<Byte, BtCommandDetails> aMap = new LinkedHashMap<Byte, BtCommandDetails>();
        aMap.put(GET_ACCEL_SENSITIVITY_COMMAND, new BtCommandDetails(GET_ACCEL_SENSITIVITY_COMMAND, "GET_ACCEL_SENSITIVITY_COMMAND", ACCEL_SENSITIVITY_RESPONSE));
        aMap.put(GET_MAG_CALIBRATION_COMMAND, new BtCommandDetails(GET_MAG_CALIBRATION_COMMAND, "GET_MAG_CALIBRATION_COMMAND", MAG_CALIBRATION_RESPONSE));
        aMap.put(GET_LSM303DLHC_ACCEL_CALIBRATION_COMMAND, new BtCommandDetails(GET_LSM303DLHC_ACCEL_CALIBRATION_COMMAND, "GET_LSM303DLHC_ACCEL_CALIBRATION_COMMAND", LSM303DLHC_ACCEL_CALIBRATION_RESPONSE));
        aMap.put(GET_MAG_GAIN_COMMAND, new BtCommandDetails(GET_MAG_GAIN_COMMAND, "GET_MAG_GAIN_COMMAND", MAG_GAIN_RESPONSE));
        aMap.put(GET_MAG_SAMPLING_RATE_COMMAND, new BtCommandDetails(GET_MAG_SAMPLING_RATE_COMMAND, "GET_MAG_SAMPLING_RATE_COMMAND", MAG_SAMPLING_RATE_RESPONSE));
        aMap.put(GET_ACCEL_SAMPLING_RATE_COMMAND, new BtCommandDetails(GET_ACCEL_SAMPLING_RATE_COMMAND, "GET_ACCEL_SAMPLING_RATE_COMMAND", ACCEL_SAMPLING_RATE_RESPONSE));
        aMap.put(GET_LSM303DLHC_ACCEL_LPMODE_COMMAND, new BtCommandDetails(GET_LSM303DLHC_ACCEL_LPMODE_COMMAND, "GET_LSM303DLHC_ACCEL_LPMODE_COMMAND", LSM303DLHC_ACCEL_LPMODE_RESPONSE));
        aMap.put(GET_LSM303DLHC_ACCEL_HRMODE_COMMAND, new BtCommandDetails(GET_LSM303DLHC_ACCEL_HRMODE_COMMAND, "GET_LSM303DLHC_ACCEL_HRMODE_COMMAND", LSM303DLHC_ACCEL_HRMODE_RESPONSE));
        mBtGetCommandMap = Collections.unmodifiableMap(aMap);
    }
    
    public static final Map<Byte, BtCommandDetails> mBtSetCommandMap;
    static {
        Map<Byte, BtCommandDetails> aMap = new LinkedHashMap<Byte, BtCommandDetails>();
        aMap.put(SET_ACCEL_SENSITIVITY_COMMAND, new BtCommandDetails(SET_ACCEL_SENSITIVITY_COMMAND, "SET_ACCEL_SENSITIVITY_COMMAND"));
        aMap.put(SET_MAG_CALIBRATION_COMMAND, new BtCommandDetails(SET_MAG_CALIBRATION_COMMAND, "SET_MAG_CALIBRATION_COMMAND"));
        aMap.put(SET_LSM303DLHC_ACCEL_CALIBRATION_COMMAND, new BtCommandDetails(SET_LSM303DLHC_ACCEL_CALIBRATION_COMMAND, "SET_LSM303DLHC_ACCEL_CALIBRATION_COMMAND"));
        aMap.put(SET_MAG_GAIN_COMMAND, new BtCommandDetails(SET_MAG_GAIN_COMMAND, "SET_MAG_GAIN_COMMAND"));
        aMap.put(SET_MAG_SAMPLING_RATE_COMMAND, new BtCommandDetails(SET_MAG_SAMPLING_RATE_COMMAND, "SET_MAG_SAMPLING_RATE_COMMAND"));
        aMap.put(SET_ACCEL_SAMPLING_RATE_COMMAND, new BtCommandDetails(SET_ACCEL_SAMPLING_RATE_COMMAND, "SET_ACCEL_SAMPLING_RATE_COMMAND"));
        aMap.put(SET_LSM303DLHC_ACCEL_LPMODE_COMMAND, new BtCommandDetails(SET_LSM303DLHC_ACCEL_LPMODE_COMMAND, "SET_LSM303DLHC_ACCEL_LPMODE_COMMAND"));
        aMap.put(SET_LSM303DLHC_ACCEL_HRMODE_COMMAND, new BtCommandDetails(SET_LSM303DLHC_ACCEL_HRMODE_COMMAND, "SET_LSM303DLHC_ACCEL_HRMODE_COMMAND"));
        mBtSetCommandMap = Collections.unmodifiableMap(aMap);
    }
	//--------- Bluetooth commands end --------------
	

	//--------- Configuration options start --------------
	public static final String[] ListofAccelRange={"+/- 2g","+/- 4g","+/- 8g","+/- 16g"};  
	public static final Integer[] ListofLSM303DLHCAccelRangeConfigValues={0,1,2,3};  
	
	public static final String[] ListofMagRange={"+/- 1.3Ga","+/- 1.9Ga","+/- 2.5Ga","+/- 4.0Ga","+/- 4.7Ga","+/- 5.6Ga","+/- 8.1Ga"}; 
	public static final Integer[] ListofMagRangeConfigValues={1,2,3,4,5,6,7}; // no '0' option  
	

	
	public static final String[] ListofLSM303DLHCAccelRate={"Power-down","1.0Hz","10.0Hz","25.0Hz","50.0Hz","100.0Hz","200.0Hz","400.0Hz","1344.0Hz"};
	public static final Integer[] ListofLSM303DLHCAccelRateConfigValues={0,1,2,3,4,5,6,7,9};
	
	public static final String[] ListofLSM303DLHCMagRate={"0.75Hz","1.5Hz","3.0Hz","7.5Hz","15.0Hz","30.0Hz","75.0Hz","220.0Hz"};
	public static final Integer[] ListofLSM303DLHCMagRateConfigValues={0,1,2,3,4,5,6,7};
	
	public static final String[] ListofLSM303DLHCAccelRateLpm={"Power-down","1Hz","10Hz","25Hz","50Hz","100Hz","200Hz","400Hz","1620Hz","5376Hz"}; // 1620Hz and 5376Hz are only available in low-power mode
	public static final Integer[] ListofLSM303DLHCAccelRateLpmConfigValues={0,1,2,3,4,5,6,7,8,9};
	
	public static final ConfigOptionDetailsSensor configOptionAccelRange = new ConfigOptionDetailsSensor(
			ListofAccelRange, 
			ListofLSM303DLHCAccelRangeConfigValues, 
			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW);
	
	public static final ConfigOptionDetailsSensor configOptionMagRange = new ConfigOptionDetailsSensor(
			ListofMagRange, 
			ListofMagRangeConfigValues, 
			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW);
	
	public static final ConfigOptionDetailsSensor configOptionAccelRate = new ConfigOptionDetailsSensor(
			ListofLSM303DLHCAccelRate, 
			ListofLSM303DLHCAccelRateConfigValues, 
			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW);
	{
		
		configOptionAccelRate.setGuiValues(ConfigOptionDetailsSensor.VALUE_INDEXES.LSM303DLHC_ACCEL_RATE.IS_LPM, SensorLSM303.ListofLSM303DLHCAccelRateLpm);
		configOptionAccelRate.setConfigValues(ConfigOptionDetailsSensor.VALUE_INDEXES.LSM303DLHC_ACCEL_RATE.IS_LPM, SensorLSM303.ListofLSM303DLHCAccelRateLpmConfigValues);
	}
	
	public static final ConfigOptionDetailsSensor configOptionMagRate = new ConfigOptionDetailsSensor(
			ListofLSM303DLHCMagRate, 
			ListofLSM303DLHCMagRateConfigValues, 
			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW);
	
	public static final ConfigOptionDetailsSensor configOptionAccelLpm = new ConfigOptionDetailsSensor(
			 ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.CHECKBOX);
	
	//--------- Configuration options end --------------
	
	
	//--------- Sensor info start --------------
	public static final SensorDetailsRef sensorLSM303DLHCAccel = new SensorDetailsRef(
			0x10<<8, //== Configuration.Shimmer3.SensorBitmap.SENSOR_D_ACCEL will be: SensorBitmap.SENSOR_D_ACCEL
			0x10<<8, //== Configuration.Shimmer3.SensorBitmap.SENSOR_D_ACCEL will be: SensorBitmap.SENSOR_D_ACCEL
			GuiLabelSensors.ACCEL_WR,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW,
			Arrays.asList(GuiLabelConfig.LSM303DLHC_ACCEL_RANGE,GuiLabelConfig.LSM303DLHC_ACCEL_RATE),
			Arrays.asList(ObjectClusterSensorName.ACCEL_WR_X,
					ObjectClusterSensorName.ACCEL_WR_Y,
					ObjectClusterSensorName.ACCEL_WR_Z));
	
	public static final SensorDetailsRef sensorLSM303DLHCMag = new SensorDetailsRef(
			0x20, //== Configuration.Shimmer3.SensorBitmap.SENSOR_MAG will be: SensorBitmap.SENSOR_MAG, 
			0x20, //== Configuration.Shimmer3.SensorBitmap.SENSOR_MAG will be: SensorBitmap.SENSOR_MAG, 
			GuiLabelSensors.MAG,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW,
			Arrays.asList(GuiLabelConfig.LSM303DLHC_MAG_RANGE,GuiLabelConfig.LSM303DLHC_MAG_RATE),
			//MAG channel order is XZY instead of XYZ
			Arrays.asList(ObjectClusterSensorName.MAG_X,
					ObjectClusterSensorName.MAG_Z,
					ObjectClusterSensorName.MAG_Y));
	
	public static final Map<Integer, SensorDetailsRef> mSensorMapRef;
    static {
        Map<Integer, SensorDetailsRef> aMap = new LinkedHashMap<Integer, SensorDetailsRef>();
        aMap.put(Configuration.Shimmer3.SensorMapKey.SHIMMER_LSM303DLHC_ACCEL, SensorLSM303.sensorLSM303DLHCAccel);  
        aMap.put(Configuration.Shimmer3.SensorMapKey.SHIMMER_LSM303DLHC_MAG, SensorLSM303.sensorLSM303DLHCMag);	
		mSensorMapRef = Collections.unmodifiableMap(aMap);
    }
	//--------- Sensor info end --------------
	
	
	//--------- Channel info start --------------
    public static final ChannelDetails channelLSM303AccelX = new ChannelDetails(
			ObjectClusterSensorName.ACCEL_WR_X,
			ObjectClusterSensorName.ACCEL_WR_X,
			DatabaseChannelHandles.WR_ACC_X,
			CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.LSB,
			CHANNEL_UNITS.METER_PER_SECOND_SQUARE,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL),
			0x04);
    
    public static final ChannelDetails channelLSM303AccelY = new ChannelDetails(
			ObjectClusterSensorName.ACCEL_WR_Y,
			ObjectClusterSensorName.ACCEL_WR_Y,
			DatabaseChannelHandles.WR_ACC_Y,
			CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.LSB,
			CHANNEL_UNITS.METER_PER_SECOND_SQUARE,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL),
			0x05);
    
    public static final ChannelDetails channelLSM303AccelZ = new ChannelDetails(
			ObjectClusterSensorName.ACCEL_WR_Z,
			ObjectClusterSensorName.ACCEL_WR_Z,
			DatabaseChannelHandles.WR_ACC_Z,
			CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.LSB,
			CHANNEL_UNITS.METER_PER_SECOND_SQUARE,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL),
			0x06);
    
    public static final ChannelDetails channelLSM303MagX = new ChannelDetails(
			ObjectClusterSensorName.MAG_X,
			ObjectClusterSensorName.MAG_X,
			DatabaseChannelHandles.MAG_X,
			CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.MSB,
			CHANNEL_UNITS.LOCAL_FLUX,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL),
			0x07);
	
	public static final ChannelDetails channelLSM303MagY = new ChannelDetails(
			ObjectClusterSensorName.MAG_Y,
			ObjectClusterSensorName.MAG_Y,
			DatabaseChannelHandles.MAG_Y,
			CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.MSB,
			CHANNEL_UNITS.LOCAL_FLUX,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL),
			0x08);
	
	public static final ChannelDetails channelLSM303MagZ = new ChannelDetails(
			ObjectClusterSensorName.MAG_Z,
			ObjectClusterSensorName.MAG_Z,
			DatabaseChannelHandles.MAG_Z,
			CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.MSB,
			CHANNEL_UNITS.LOCAL_FLUX,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL),
			0x09);
    
    public static final Map<String, ChannelDetails> mChannelMapRef;
    static {
        Map<String, ChannelDetails> aMap = new LinkedHashMap<String, ChannelDetails>();
        aMap.put(SensorLSM303.ObjectClusterSensorName.ACCEL_WR_X, SensorLSM303.channelLSM303AccelX);
        aMap.put(SensorLSM303.ObjectClusterSensorName.ACCEL_WR_Y, SensorLSM303.channelLSM303AccelY);
        aMap.put(SensorLSM303.ObjectClusterSensorName.ACCEL_WR_Z, SensorLSM303.channelLSM303AccelZ);
        aMap.put(SensorLSM303.ObjectClusterSensorName.MAG_X, SensorLSM303.channelLSM303MagX);
        aMap.put(SensorLSM303.ObjectClusterSensorName.MAG_Z, SensorLSM303.channelLSM303MagZ);
        aMap.put(SensorLSM303.ObjectClusterSensorName.MAG_Y, SensorLSM303.channelLSM303MagY);
		mChannelMapRef = Collections.unmodifiableMap(aMap);
    }
	//--------- Channel info end --------------
    
    
    public static final SensorGroupingDetails sensorGDLsmAccel = new SensorGroupingDetails(
			GuiLabelSensorTiles.WIDE_RANGE_ACCEL,
			Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_LSM303DLHC_ACCEL),
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW);
    
    public static final SensorGroupingDetails sensorGDLsmMag = new SensorGroupingDetails(
			GuiLabelSensorTiles.MAG,
			Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_LSM303DLHC_MAG),
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW);
    
    //--------- Constructors for this class start --------------
    /**Just used for accessing calibration*/
    public SensorLSM303() {
		super(SENSORS.LSM303);
		initialise();
    }
    
    public SensorLSM303(ShimmerVerObject svo) {
		super(SENSORS.LSM303, svo);
		initialise();
	}
   //--------- Constructors for this class end --------------

	
	//--------- Abstract methods implemented start --------------
	@Override 
	public void generateSensorMap() {
		super.createLocalSensorMapWithCustomParser(mSensorMapRef, mChannelMapRef);
	}

	
	@Override 
	public void generateConfigOptionsMap() {
		mConfigOptionsMap.put(GuiLabelConfig.LSM303DLHC_ACCEL_RANGE, configOptionAccelRange);
		mConfigOptionsMap.put(GuiLabelConfig.LSM303DLHC_MAG_RANGE, configOptionMagRange);
		mConfigOptionsMap.put(GuiLabelConfig.LSM303DLHC_ACCEL_RATE, configOptionAccelRate);
		mConfigOptionsMap.put(GuiLabelConfig.LSM303DLHC_MAG_RATE, configOptionMagRate);
		mConfigOptionsMap.put(GuiLabelConfig.LSM303DLHC_ACCEL_LPM, configOptionAccelLpm);
	}
	
	@Override 
	public void generateSensorGroupMapping() {
		mSensorGroupingMap = new LinkedHashMap<Integer, SensorGroupingDetails>();
		if(mShimmerVerObject.isShimmerGen3() || mShimmerVerObject.isShimmerGen4()){
			mSensorGroupingMap.put(Configuration.Shimmer3.GuiLabelSensorTiles.WIDE_RANGE_ACCEL.ordinal(), sensorGDLsmAccel);
			mSensorGroupingMap.put(Configuration.Shimmer3.GuiLabelSensorTiles.MAG.ordinal(), sensorGDLsmMag);
		}
		super.updateSensorGroupingMap();	
	}	

	
	@Override 
	public ObjectCluster processDataCustom(SensorDetails sensorDetails, byte[] rawData, COMMUNICATION_TYPE commType, ObjectCluster objectCluster, boolean isTimeSyncEnabled, long pcTimestamp) {
		
		// process data originating from the Shimmer
		objectCluster = sensorDetails.processDataCommon(rawData, commType, objectCluster, isTimeSyncEnabled, pcTimestamp);
		
		//Calibration
		if(mEnableCalibration){
			// get uncalibrated data for each (sub)sensor
			if(sensorDetails.mSensorDetailsRef.mGuiFriendlyLabel.equals(GuiLabelSensors.ACCEL_WR) && mCurrentCalibDetailsAccelWr!=null){
				double[] unCalibratedAccelWrData = new double[3];
				for (ChannelDetails channelDetails:sensorDetails.mListOfChannels){
					//Uncalibrated Accelerometer data
					if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.ACCEL_WR_X)){
						unCalibratedAccelWrData[0] = ((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(channelDetails.mObjectClusterName), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
					}
					else if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.ACCEL_WR_Y)){
						unCalibratedAccelWrData[1]  = ((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(channelDetails.mObjectClusterName), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
					}
					else if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.ACCEL_WR_Z)){
						unCalibratedAccelWrData[2]  = ((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(channelDetails.mObjectClusterName), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
					}
				}
				
				double[] calibratedAccelWrData = UtilCalibration.calibrateInertialSensorData(unCalibratedAccelWrData, mCurrentCalibDetailsAccelWr);
//				double[] calibratedAccelWrData = UtilCalibration.calibrateInertialSensorData(unCalibratedAccelWrData, mAlignmentMatrixWRAccel, mSensitivityMatrixWRAccel, mOffsetVectorWRAccel);
	
				//Add calibrated data to Object cluster
				if(sensorDetails.mSensorDetailsRef.mGuiFriendlyLabel.equals(GuiLabelSensors.ACCEL_WR)){	
					for (ChannelDetails channelDetails:sensorDetails.mListOfChannels){
						if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.ACCEL_WR_X)){
							objectCluster.addCalData(channelDetails, calibratedAccelWrData[0], objectCluster.getIndexKeeper()-3, isUsingDefaultWRAccelParam());
						}
						else if(channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.ACCEL_WR_Y)){
							objectCluster.addCalData(channelDetails, calibratedAccelWrData[1], objectCluster.getIndexKeeper()-2, isUsingDefaultWRAccelParam());
						}
						else if(channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.ACCEL_WR_Z)){
							objectCluster.addCalData(channelDetails, calibratedAccelWrData[2], objectCluster.getIndexKeeper()-1, isUsingDefaultWRAccelParam());
						}
					}
				}
	
				//Debugging
				if(mIsDebugOutput){
					super.consolePrintChannelsCal(objectCluster, Arrays.asList(
							new String[]{ObjectClusterSensorName.ACCEL_WR_X, CHANNEL_TYPE.UNCAL.toString()}, 
							new String[]{ObjectClusterSensorName.ACCEL_WR_Y, CHANNEL_TYPE.UNCAL.toString()}, 
							new String[]{ObjectClusterSensorName.ACCEL_WR_Z, CHANNEL_TYPE.UNCAL.toString()}, 
							new String[]{ObjectClusterSensorName.ACCEL_WR_X, CHANNEL_TYPE.CAL.toString()}, 
							new String[]{ObjectClusterSensorName.ACCEL_WR_Y, CHANNEL_TYPE.CAL.toString()},
							new String[]{ObjectClusterSensorName.ACCEL_WR_Z, CHANNEL_TYPE.CAL.toString()}));
				}
	
			}
			else if(sensorDetails.mSensorDetailsRef.mGuiFriendlyLabel.equals(GuiLabelSensors.MAG) && mCurrentCalibDetailsMag!=null){
				double[] unCalibratedMagData = new double[3];
				for (ChannelDetails channelDetails:sensorDetails.mListOfChannels){
					//Uncalibrated Magnetometer data
					if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.MAG_X)){
						unCalibratedMagData[0] = ((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(channelDetails.mObjectClusterName), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
					}
					else if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.MAG_Y)){
						unCalibratedMagData[1]  = ((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(channelDetails.mObjectClusterName), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
					}
					else if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.MAG_Z)){
						unCalibratedMagData[2]  = ((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(channelDetails.mObjectClusterName), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
					}	
				}
				
//				double[] calibratedMagData = UtilCalibration.calibrateInertialSensorData(unCalibratedMagData, mAlignmentMatrixMagnetometer, mSensitivityMatrixMagnetometer, mOffsetVectorMagnetometer);
				double[] calibratedMagData = UtilCalibration.calibrateInertialSensorData(unCalibratedMagData, mCurrentCalibDetailsMag);

				if(sensorDetails.mSensorDetailsRef.mGuiFriendlyLabel.equals(GuiLabelSensors.MAG)){
					for (ChannelDetails channelDetails:sensorDetails.mListOfChannels){
						if(channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.MAG_X)){
							objectCluster.addCalData(channelDetails, calibratedMagData[0], objectCluster.getIndexKeeper()-3, isUsingDefaultMagParam());
						}
						else if(channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.MAG_Y)){
							objectCluster.addCalData(channelDetails, calibratedMagData[1], objectCluster.getIndexKeeper()-2, isUsingDefaultMagParam());
						}
						else if(channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.MAG_Z)){
							objectCluster.addCalData(channelDetails, calibratedMagData[2], objectCluster.getIndexKeeper()-1, isUsingDefaultMagParam());
						}
					}
				}
	
				
				//Debugging
				if(mIsDebugOutput){
					super.consolePrintChannelsCal(objectCluster, Arrays.asList(
							new String[]{ObjectClusterSensorName.MAG_X, CHANNEL_TYPE.UNCAL.toString()}, 
							new String[]{ObjectClusterSensorName.MAG_Y, CHANNEL_TYPE.UNCAL.toString()}, 
							new String[]{ObjectClusterSensorName.MAG_Z, CHANNEL_TYPE.UNCAL.toString()}, 
							new String[]{ObjectClusterSensorName.MAG_X, CHANNEL_TYPE.CAL.toString()}, 
							new String[]{ObjectClusterSensorName.MAG_Y, CHANNEL_TYPE.CAL.toString()},
							new String[]{ObjectClusterSensorName.MAG_Z, CHANNEL_TYPE.CAL.toString()}));
				}
			}
		}
		return objectCluster;
	}

	
	@Override 
	public void infoMemByteArrayGenerate(ShimmerDevice shimmerDevice, byte[] mInfoMemBytes) {//XXX - What is "ShimmerDevice shimmerDevice" doing here? 
		int idxConfigSetupByte0 =              		6; 
		int idxConfigSetupByte2 =              		8;
//		int idxLSM303DLHCAccelCalibration =    	   94; 
//		int idxLSM303DLHCMagCalibration =          73;
		//fix for newer firmware -> see InfomemLayoutShimmer3
		int idxLSM303DLHCMagCalibration =   76;
		int idxLSM303DLHCAccelCalibration = 97;
		int bitShiftLSM303DLHCAccelSamplingRate =   4;
		int bitShiftLSM303DLHCAccelRange =          2;
		int bitShiftLSM303DLHCAccelLPM =            1;
		int bitShiftLSM303DLHCAccelHRM =            0;
		int bitShiftLSM303DLHCMagRange =            5;
		int bitShiftLSM303DLHCMagSamplingRate =     2;
		int maskLSM303DLHCAccelSamplingRate =    0x0F;   
		int maskLSM303DLHCAccelRange =           0x03;
		int maskLSM303DLHCAccelLPM =             0x01;
		int maskLSM303DLHCAccelHRM =             0x01;
		int maskLSM303DLHCMagRange =             0x07;
		int maskLSM303DLHCMagSamplingRate =      0x07;
		int lengthGeneralCalibrationBytes =        21;
		
		//idxConfigSetupByte0 
		mInfoMemBytes[idxConfigSetupByte0] |= (byte) ((mLSM303DigitalAccelRate & maskLSM303DLHCAccelSamplingRate) << bitShiftLSM303DLHCAccelSamplingRate);
		mInfoMemBytes[idxConfigSetupByte0] |= (byte) ((getAccelRange() & maskLSM303DLHCAccelRange) << bitShiftLSM303DLHCAccelRange);
		if(mLowPowerAccelWR) {
			mInfoMemBytes[idxConfigSetupByte0] |= (maskLSM303DLHCAccelLPM << bitShiftLSM303DLHCAccelLPM);
		}
		if(mHighResAccelWR) {
			mInfoMemBytes[idxConfigSetupByte0] |= (maskLSM303DLHCAccelHRM << bitShiftLSM303DLHCAccelHRM);
		}
		
		//idxConfigSetupByte2
		mInfoMemBytes[idxConfigSetupByte2] |= (byte) ((getMagRange() & maskLSM303DLHCMagRange) << bitShiftLSM303DLHCMagRange);
		mInfoMemBytes[idxConfigSetupByte2] |= (byte) ((getLSM303MagRate() & maskLSM303DLHCMagSamplingRate) << bitShiftLSM303DLHCMagSamplingRate);
		
		// LSM303DLHC Digital Accel Calibration Parameters
		byte[] bufferCalibrationParameters = generateCalParamLSM303DLHCAccel();
		System.arraycopy(bufferCalibrationParameters, 0, mInfoMemBytes, idxLSM303DLHCAccelCalibration, lengthGeneralCalibrationBytes);
		
		// LSM303DLHC Magnetometer Calibration Parameters
		bufferCalibrationParameters = generateCalParamLSM303DLHCMag();
		System.arraycopy(bufferCalibrationParameters, 0, mInfoMemBytes, idxLSM303DLHCMagCalibration, lengthGeneralCalibrationBytes);
	}

	
	@Override 
	public void infoMemByteArrayParse(ShimmerDevice shimmerDevice, byte[] mInfoMemBytes) {//XXX - What is "ShimmerDevice shimmerDevice" doing here? 
		int idxConfigSetupByte0 =              		6; 
		int idxConfigSetupByte2 =              		8;
//		int idxLSM303DLHCAccelCalibration =    	   94; 
//		int idxLSM303DLHCMagCalibration =          73;
		//fix for newer firmware -> see InfomemLayoutShimmer3
		int idxLSM303DLHCMagCalibration =   76;
		int idxLSM303DLHCAccelCalibration = 97;
		int bitShiftLSM303DLHCAccelSamplingRate =   4;
		int bitShiftLSM303DLHCAccelRange =          2;
		int bitShiftLSM303DLHCAccelLPM =            1;
		int bitShiftLSM303DLHCAccelHRM =            0;
		int bitShiftLSM303DLHCMagRange =            5;
		int bitShiftLSM303DLHCMagSamplingRate =     2;
		int maskLSM303DLHCAccelSamplingRate =    0x0F;   
		int maskLSM303DLHCAccelRange =           0x03;
		int maskLSM303DLHCAccelLPM =             0x01;
		int maskLSM303DLHCAccelHRM =             0x01;
		int maskLSM303DLHCMagRange =             0x07;
		int maskLSM303DLHCMagSamplingRate =      0x07;
		int lengthGeneralCalibrationBytes =        21;
		
		//idxConfigSetupByte0 
		mLSM303DigitalAccelRate = (mInfoMemBytes[idxConfigSetupByte0] >> bitShiftLSM303DLHCAccelSamplingRate) & maskLSM303DLHCAccelSamplingRate; 
		setLSM303AccelRange((mInfoMemBytes[idxConfigSetupByte0] >> bitShiftLSM303DLHCAccelRange) & maskLSM303DLHCAccelRange);
		if(((mInfoMemBytes[idxConfigSetupByte0] >> bitShiftLSM303DLHCAccelLPM) & maskLSM303DLHCAccelLPM) == maskLSM303DLHCAccelLPM) {
			mLowPowerAccelWR = true;
		}
		else {
			mLowPowerAccelWR = false;
		}
		if(((mInfoMemBytes[idxConfigSetupByte0] >> bitShiftLSM303DLHCAccelHRM) & maskLSM303DLHCAccelHRM) == maskLSM303DLHCAccelHRM) {
			mHighResAccelWR = true;
		}
		else {
			mHighResAccelWR = false;
		}
		
		//idxConfigSetupByte2
		setLSM303MagRange((mInfoMemBytes[idxConfigSetupByte2] >> bitShiftLSM303DLHCMagRange) & maskLSM303DLHCMagRange);
		setLSM303MagRate((mInfoMemBytes[idxConfigSetupByte2] >> bitShiftLSM303DLHCMagSamplingRate) & maskLSM303DLHCMagSamplingRate);
		checkLowPowerMag(); // check rate to determine if Sensor is in LPM mode
		
		// LSM303DLHC Digital Accel Calibration Parameters
		byte[] bufferCalibrationParameters = new byte[lengthGeneralCalibrationBytes];
		System.arraycopy(mInfoMemBytes, idxLSM303DLHCAccelCalibration, bufferCalibrationParameters, 0 , lengthGeneralCalibrationBytes);
//		retrieveKinematicCalibrationParametersFromPacket(bufferCalibrationParameters, LSM303DLHC_ACCEL_CALIBRATION_RESPONSE);
		parseCalibParamFromPacketAccelLsm(bufferCalibrationParameters, CALIB_READ_SOURCE.INFOMEM);
		
		// LSM303DLHC Magnetometer Calibration Parameters
		bufferCalibrationParameters = new byte[lengthGeneralCalibrationBytes];
		System.arraycopy(mInfoMemBytes, idxLSM303DLHCMagCalibration, bufferCalibrationParameters, 0 , lengthGeneralCalibrationBytes);
//		retrieveKinematicCalibrationParametersFromPacket(bufferCalibrationParameters, MAG_CALIBRATION_RESPONSE);
		parseCalibParamFromPacketMag(bufferCalibrationParameters, CALIB_READ_SOURCE.INFOMEM);
	}

	
	@Override 
	public Object setConfigValueUsingConfigLabel(Integer sensorMapKey, String configLabel, Object valueToSet) {
		Object returnValue = null;
		
		switch(configLabel){
			case(GuiLabelConfig.LSM303DLHC_ACCEL_LPM):
				setLowPowerAccelWR((boolean)valueToSet);
				break;
				
			case(GuiLabelConfig.LSM303DLHC_MAG_LPM):
				setLowPowerMag((boolean)valueToSet);
				break;
			case(GuiLabelConfig.LSM303DLHC_ACCEL_RANGE):
				setLSM303AccelRange((int)valueToSet);
				break;
				
			case(GuiLabelConfig.LSM303DLHC_MAG_RANGE):
				setLSM303MagRange((int)valueToSet);
				break;
				
			case(GuiLabelConfig.LSM303DLHC_ACCEL_RATE):
				setLSM303DigitalAccelRate((int)valueToSet);
				break;
				
			case(GuiLabelConfig.LSM303DLHC_MAG_RATE):
				setLSM303MagRate((int)valueToSet);
				break;
				
//			case(GuiLabelConfigCommon.KINEMATIC_CALIBRATION_ALL):
//				TreeMap<Integer, TreeMap<Integer, CalibDetails>> mapOfKinematicSensorCalibration = (TreeMap<Integer, TreeMap<Integer, CalibDetails>>) valueToSet;
//				setCalibration(mapOfKinematicSensorCalibration);
//				returnValue = valueToSet;
//	    		break;
			case(GuiLabelConfigCommon.RANGE):
				if(sensorMapKey==Configuration.Shimmer3.SensorMapKey.SHIMMER_LSM303DLHC_ACCEL){
					this.setConfigValueUsingConfigLabel(GuiLabelConfig.LSM303DLHC_ACCEL_RANGE, valueToSet);
				}
				else if(sensorMapKey==Configuration.Shimmer3.SensorMapKey.SHIMMER_LSM303DLHC_MAG){
					this.setConfigValueUsingConfigLabel(GuiLabelConfig.LSM303DLHC_MAG_RANGE, valueToSet);
				}
				break;
			case(GuiLabelConfigCommon.RATE):
				if(sensorMapKey==Configuration.Shimmer3.SensorMapKey.SHIMMER_LSM303DLHC_ACCEL){
					this.setConfigValueUsingConfigLabel(GuiLabelConfig.LSM303DLHC_ACCEL_RATE, valueToSet);
				}
				else if(sensorMapKey==Configuration.Shimmer3.SensorMapKey.SHIMMER_LSM303DLHC_MAG){
					this.setConfigValueUsingConfigLabel(GuiLabelConfig.LSM303DLHC_MAG_RANGE, valueToSet);
				}
				break;
			default:
				returnValue = super.setConfigValueUsingConfigLabelCommon(sensorMapKey, configLabel, valueToSet);
				break;
		}		
		return returnValue;
	}

	
	@Override 
	public Object getConfigValueUsingConfigLabel(Integer sensorMapKey, String configLabel) {
		Object returnValue = null;
		
		if(configLabel.equals(GuiLabelConfig.LSM303DLHC_ACCEL_RATE)){
        	checkConfigOptionValues(configLabel);
        }
		
		switch(configLabel){
			case(GuiLabelConfig.LSM303DLHC_ACCEL_LPM):
				returnValue = isLSM303DigitalAccelLPM();
	        	break;
	        	
			case(GuiLabelConfig.LSM303DLHC_MAG_LPM):
				returnValue = checkLowPowerMag();
	        	break;
	        	
			case(GuiLabelConfig.LSM303DLHC_ACCEL_RANGE): 
				returnValue = getAccelRange();
		    	break;
		    	
			case(GuiLabelConfig.LSM303DLHC_MAG_RANGE):
				//TODO check below and commented out code (RS (20/5/2016): Same as in ShimmerObject.)
				returnValue = getMagRange();
			
		//						// firmware sets mag range to 7 (i.e. index 6 in combobox) if user set mag range to 0 in config file
		//						if(getMagRange() == 0) cmBx.setSelectedIndex(6);
		//						else cmBx.setSelectedIndex(getMagRange()-1);
				break;
			
			case(GuiLabelConfig.LSM303DLHC_ACCEL_RATE): 
				int configValue = getLSM303DigitalAccelRate(); 
				 
		    	if(!isLSM303DigitalAccelLPM()) {
		        	if(configValue==8) {
		        		//TODO:
		        		/*RS (20/5/2016): Why returning a different value?
		        		 * In the Set-method the compatibility-check for Accel Rates supported for Low Power Mode is made.
		        		 * In this get-method the it should just read/get the value, not manipulating it.
		        		 * */
		        		configValue = 9;
		        	}
		    	}
				returnValue = configValue;
				break;
		
			case(GuiLabelConfig.LSM303DLHC_MAG_RATE):
				returnValue = getLSM303MagRate();
	        	break;
	        	
//			case(Configuration.Shimmer3.GuiLabelConfig.KINEMATIC_CALIBRATION_ALL):
//				returnValue = getKinematicCalibration();
//				break;
			case(GuiLabelConfigCommon.RANGE):
				if(sensorMapKey==Configuration.Shimmer3.SensorMapKey.SHIMMER_LSM303DLHC_ACCEL){
					returnValue = this.getConfigValueUsingConfigLabel(GuiLabelConfig.LSM303DLHC_ACCEL_RANGE);
				}
				else if(sensorMapKey==Configuration.Shimmer3.SensorMapKey.SHIMMER_LSM303DLHC_MAG){
					returnValue = this.getConfigValueUsingConfigLabel(GuiLabelConfig.LSM303DLHC_MAG_RANGE);
				}
				break;
			case(GuiLabelConfigCommon.RATE):
				if(sensorMapKey==Configuration.Shimmer3.SensorMapKey.SHIMMER_LSM303DLHC_ACCEL){
					returnValue = this.getConfigValueUsingConfigLabel(GuiLabelConfig.LSM303DLHC_ACCEL_RATE);
				}
				else if(sensorMapKey==Configuration.Shimmer3.SensorMapKey.SHIMMER_LSM303DLHC_MAG){
					returnValue = this.getConfigValueUsingConfigLabel(GuiLabelConfig.LSM303DLHC_MAG_RATE);
				}
				break;
			default:
				returnValue = super.getConfigValueUsingConfigLabelCommon(sensorMapKey, configLabel);
				break;
			
		}
		return returnValue;
	
	}

	
	@Override 
	public void setSensorSamplingRate(double samplingRateHz) {
		//set sampling rate of the sensors as close to the Shimmer sampling rate as possible (sensor sampling rate >= shimmer sampling rate)
		setLowPowerAccelWR(false);
		setLowPowerMag(false);
		
		setLSM303AccelRateFromFreq(samplingRateHz);
		setLSM303MagRateFromFreq(samplingRateHz);
		
    	//TODO
//    	checkLowPowerAccelWR();
		checkLowPowerMag();
	}
	
	@Override 
	public boolean setDefaultConfigForSensor(int sensorMapKey, boolean isSensorEnabled) {
		if(mSensorMap.containsKey(sensorMapKey)){
			if(sensorMapKey==Configuration.Shimmer3.SensorMapKey.SHIMMER_LSM303DLHC_ACCEL) {
				setDefaultLsm303dlhcAccelSensorConfig(isSensorEnabled);		
			}
			else if(sensorMapKey==Configuration.Shimmer3.SensorMapKey.SHIMMER_LSM303DLHC_MAG) {
				setDefaultLsm303dlhcMagSensorConfig(isSensorEnabled);
			}
			return true;
		}
		return false;
	}
	
	
	@Override 
	public boolean checkConfigOptionValues(String stringKey) {		
		if(mConfigOptionsMap.containsKey(stringKey)){
			if(stringKey==GuiLabelConfig.LSM303DLHC_ACCEL_RATE){
				if(isLSM303DigitalAccelLPM()) {
					mConfigOptionsMap.get(stringKey).setIndexOfValuesToUse(ConfigOptionDetailsSensor.VALUE_INDEXES.LSM303DLHC_ACCEL_RATE.IS_LPM);
				}
				else {
					mConfigOptionsMap.get(stringKey).setIndexOfValuesToUse(ConfigOptionDetailsSensor.VALUE_INDEXES.LSM303DLHC_ACCEL_RATE.NOT_LPM);
					// double check that rate is compatible with LPM (8 not compatible so set to higher rate) 
					setLSM303DigitalAccelRate(mLSM303DigitalAccelRate);
				}
			}		
			return true;
		}
		return false;
	}

	
	@Override 
	public Object getSettings(String componentName, COMMUNICATION_TYPE commType) {
		// TODO Auto-generated method stub
		//XXX - RS: Also returning null in BMP180 and GSR sensors classes 
		return null;
	}

	
	@Override 
	public ActionSetting setSettings(String componentName, Object valueToSet, COMMUNICATION_TYPE commType) {
		// 		Object returnValue = null;
		ActionSetting actionsetting = new ActionSetting(commType);
		
//		 switch(componentName){
//			case(Configuration.Shimmer3.GuiLabelConfig.LSM303DLHC_ACCEL_RANGE):
//				mAccelRange = ((int)valueToSet);
//				//
//			if(mFirmwareType==FW_ID.BTSTREAM||mFirmwareType==FW_ID.LOGANDSTREAM){ //mcommtype
//				actionsetting.mActionByteArray = new byte[]{ShimmerObject.SET_ACCEL_SENSITIVITY_COMMAND, (byte)mAccelRange};
//				return actionsetting;
//			} else if (mFirmwareType==FW_ID.SDLOG){
//				//compatiblity check and instruction generation
//			}
//        	break;
		
//		public static final byte SET_ACCEL_SENSITIVITY_COMMAND    		= (byte) 0x09;
//		public static final byte SET_MAG_CALIBRATION_COMMAND      		= (byte) 0x17;		
//		public static final byte SET_LSM303DLHC_ACCEL_CALIBRATION_COMMAND = (byte) 0x1A;
//		public static final byte SET_MAG_GAIN_COMMAND             		= (byte) 0x37;	
//		public static final byte SET_MAG_SAMPLING_RATE_COMMAND    		= (byte) 0x3A;		
//		public static final byte SET_ACCEL_SAMPLING_RATE_COMMAND  		= (byte) 0x40;	
//		public static final byte SET_LSM303DLHC_ACCEL_LPMODE_COMMAND 	= (byte) 0x43;	
//		public static final byte SET_LSM303DLHC_ACCEL_HRMODE_COMMAND	= (byte) 0x46;
//		public boolean mLowPowerAccelWR = false;
//		public boolean mHighResAccelWR = false;
//		
//		public int mAccelRange = 0;
//		public int mLSM303DigitalAccelRate = 0;
//		public int mMagRange = 1;
//		public int mLSM303MagRate = 4;
		
		//Might be used like this - RS 
//		switch(componentName){
//			case(GuiLabelConfig.LSM303DLHC_ACCEL_RANGE):
//				mAccelRange = ((int)valueToSet);
//				if(mFirmwareType==FW_ID.BTSTREAM||mFirmwareType==FW_ID.LOGANDSTREAM){ //mcommtype
//					actionsetting.mActionByteArray = new byte[]{SET_ACCEL_SENSITIVITY_COMMAND, (byte)mAccelRange};
//					return actionsetting;
//				} else if (mFirmwareType==FW_ID.SDLOG){
//					//compatiblity check and instruction generation
//				}
//				break;
//			case(GuiLabelConfig.LSM303DLHC_MAG_RANGE):
//				mMagRange = ((int)valueToSet);
//				if(mFirmwareType==FW_ID.BTSTREAM||mFirmwareType==FW_ID.LOGANDSTREAM){ //mcommtype
//					actionsetting.mActionByteArray = new byte[]{SET_MAG_GAIN_COMMAND, (byte)mMagRange};
//					return actionsetting;
//				} else if (mFirmwareType==FW_ID.SDLOG){
//					//compatiblity check and instruction generation
//				}
//				break;
//			case(GuiLabelConfig.LSM303DLHC_ACCEL_RATE):
//				mLSM303DigitalAccelRate = ((int)valueToSet);
//				if(mFirmwareType==FW_ID.BTSTREAM||mFirmwareType==FW_ID.LOGANDSTREAM){ //mcommtype
//					actionsetting.mActionByteArray = new byte[]{SET_ACCEL_SAMPLING_RATE_COMMAND, (byte)mLSM303DigitalAccelRate};
//					return actionsetting;
//				} else if (mFirmwareType==FW_ID.SDLOG){
//					//compatiblity check and instruction generation
//				}
//				break;
//			case(GuiLabelConfig.LSM303DLHC_MAG_RATE):
//				mLSM303MagRate = ((int)valueToSet);
//				if(mFirmwareType==FW_ID.BTSTREAM||mFirmwareType==FW_ID.LOGANDSTREAM){ //mcommtype
//					actionsetting.mActionByteArray = new byte[]{SET_MAG_SAMPLING_RATE_COMMAND, (byte)mLSM303MagRate};
//					return actionsetting;
//				} else if (mFirmwareType==FW_ID.SDLOG){
//					//compatiblity check and instruction generation
//				}
//				break;
//			case(GuiLabelConfig.LSM303DLHC_ACCEL_LPM):
//				mLowPowerAccelWR = ((boolean)valueToSet);
//			if(mFirmwareType==FW_ID.BTSTREAM||mFirmwareType==FW_ID.LOGANDSTREAM){ //mcommtype
//				actionsetting.mActionByteArray = new byte[]{SET_MAG_SAMPLING_RATE_COMMAND, (byte)mLowPowerAccelWR};
//				return actionsetting;
//			} else if (mFirmwareType==FW_ID.SDLOG){
//				//compatiblity check and instruction generation
//			}
//			break;
//			case(GuiLabelConfig.LSM303DLHC_MAG_LPM):
//				
//				
//			//TODO Above: Do LPM for Accel and Mag as is done in ShimmerObject. Below: Should these settings to be included in here as well? 
//			/*
//			case(GuiLabelConfig.LSM303DLHC_ACCEL_DEFAULT_CALIB):
//			case(GuiLabelConfig.LSM303DLHC_MAG_DEFAULT_CALIB):
//			*/
//		}
		
		return actionsetting;
		
	}
	
	@Override
	public LinkedHashMap<String, Object> getConfigMapForDb() {
		LinkedHashMap<String, Object> mapOfConfig = new LinkedHashMap<String, Object>();
		mapOfConfig.put(DatabaseConfigHandle.MAG_RANGE, getMagRange());
		mapOfConfig.put(DatabaseConfigHandle.MAG_RATE, getLSM303MagRate());
		
		mapOfConfig.put(DatabaseConfigHandle.WR_ACC, getSensorName());
		
		mapOfConfig.put(DatabaseConfigHandle.WR_ACC_RATE, getLSM303DigitalAccelRate());
		mapOfConfig.put(DatabaseConfigHandle.WR_ACC_RANGE, getAccelRange());
		mapOfConfig.put(DatabaseConfigHandle.WR_ACC_LPM, getLowPowerAccelEnabled());
		mapOfConfig.put(DatabaseConfigHandle.WR_ACC_HRM, isHighResAccelWr());
		
		super.addCalibDetailsToDbMap(mapOfConfig, getCurrentCalibDetailsMag(), DatabaseConfigHandle.LIST_OF_CALIB_HANDLES_MAG);

		super.addCalibDetailsToDbMap(mapOfConfig, getCurrentCalibDetailsAccelWr(), DatabaseConfigHandle.LIST_OF_CALIB_HANDLES_WR_ACCEL);

		return mapOfConfig;
	}	
	
	@Override
	public void parseConfigMapFromDb(LinkedHashMap<String, Object> mapOfConfigPerShimmer) {
		
		if(mapOfConfigPerShimmer.containsKey(DatabaseConfigHandle.WR_ACC_RATE)){
			setLSM303DigitalAccelRate(((Double) mapOfConfigPerShimmer.get(DatabaseConfigHandle.WR_ACC_RATE)).intValue());
		}
		if(mapOfConfigPerShimmer.containsKey(DatabaseConfigHandle.WR_ACC_RANGE)){
			setLSM303AccelRange(((Double) mapOfConfigPerShimmer.get(DatabaseConfigHandle.WR_ACC_RANGE)).intValue());
		}
		if(mapOfConfigPerShimmer.containsKey(DatabaseConfigHandle.WR_ACC_LPM)){
			setLowPowerAccelWR(((Double) mapOfConfigPerShimmer.get(DatabaseConfigHandle.WR_ACC_LPM))>0? true:false);
		}
		if(mapOfConfigPerShimmer.containsKey(DatabaseConfigHandle.WR_ACC_HRM)){
			setHighResAccelWR(((Double) mapOfConfigPerShimmer.get(DatabaseConfigHandle.WR_ACC_HRM))>0? true:false);
		}
		
		if(mapOfConfigPerShimmer.containsKey(DatabaseConfigHandle.MAG_RANGE)){
			setLSM303MagRange(((Double) mapOfConfigPerShimmer.get(DatabaseConfigHandle.MAG_RANGE)).intValue());
		}
		if(mapOfConfigPerShimmer.containsKey(DatabaseConfigHandle.MAG_RATE)){
			setLSM303MagRate(((Double) mapOfConfigPerShimmer.get(DatabaseConfigHandle.MAG_RATE)).intValue());
		}
		
		//Digital Accel Calibration Configuration
		parseCalibDetailsKinematicFromDb(mapOfConfigPerShimmer, Configuration.Shimmer3.SensorMapKey.SHIMMER_LSM303DLHC_ACCEL, getAccelRange(), SensorLSM303.DatabaseConfigHandle.LIST_OF_CALIB_HANDLES_WR_ACCEL);
		
		//Magnetometer Calibration Configuration
		parseCalibDetailsKinematicFromDb(mapOfConfigPerShimmer, Configuration.Shimmer3.SensorMapKey.SHIMMER_LSM303DLHC_MAG, getMagRange(), SensorLSM303.DatabaseConfigHandle.LIST_OF_CALIB_HANDLES_MAG);
	}


	@Override
	public void processResponse(Object obj, COMMUNICATION_TYPE commType) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void checkShimmerConfigBeforeConfiguring() {
//		// If Shimmer name is default, update with MAC ID if available.
//		if(mShimmerUserAssignedName.equals(DEFAULT_SHIMMER_NAME)){
//			setDefaultShimmerName();
//		}

		if(!isSensorEnabled(Configuration.Shimmer3.SensorMapKey.SHIMMER_LSM303DLHC_ACCEL)) {
			setDefaultLsm303dlhcAccelSensorConfig(false);
		}
		
		if(!isSensorEnabled(Configuration.Shimmer3.SensorMapKey.SHIMMER_LSM303DLHC_MAG)) {
			setDefaultLsm303dlhcMagSensorConfig(false);
		}
		
	}
	//--------- Abstract methods implemented end --------------


	//--------- Sensor specific methods start --------------
	private byte[] generateCalParamLSM303DLHCAccel(){
		return mCurrentCalibDetailsAccelWr.generateCalParamByteArray();
	}
	
	
	private byte[] generateCalParamLSM303DLHCMag(){
		return mCurrentCalibDetailsMag.generateCalParamByteArray();
	}
	
	public void parseCalibParamFromPacketAccelLsm(byte[] bufferCalibrationParameters, CALIB_READ_SOURCE calibReadSource) {
		mCurrentCalibDetailsAccelWr.parseCalParamByteArray(bufferCalibrationParameters, calibReadSource);
	}

	public void parseCalibParamFromPacketMag(byte[] bufferCalibrationParameters, CALIB_READ_SOURCE calibReadSource) {
		mCurrentCalibDetailsMag.parseCalParamByteArray(bufferCalibrationParameters, calibReadSource);
	}
	
	
	private void setDefaultCalibrationShimmer3WideRangeAccel() {
		mCurrentCalibDetailsAccelWr.resetToDefaultParameters();
	}

	
	private void setDefaultCalibrationShimmer3Mag() {
		mCurrentCalibDetailsMag.resetToDefaultParameters();
	}
	
	private boolean checkLowPowerMag() {
		mLowPowerMag = (getLSM303MagRate() <= 4)? true:false;
		return mLowPowerMag;
	}
	
	
	/**XXX
	 * RS (17/05/2016): Two questions with regards to the information below the questions:
	 * 
	 * 		What additional lower power mode is used?
	 * 		Why would the '2g' range not be support by this low power mode -> where is this mentioned in the datasheet?
	 *  
	 * This enables the low power accel option. When not enabled the sampling
	 * rate of the accel is set to the closest value to the actual sampling rate
	 * that it can achieve. In low power mode it defaults to 10Hz. Also and
	 * additional low power mode is used for the LSM303DLHC. This command will
	 * only supports the following Accel range +4g, +8g , +16g
	 */
	public void setHighResAccelWR(boolean enable) {
		mHighResAccelWR = enable;
	}
	
	public void setLowPowerAccelWR(boolean enable){
		mLowPowerAccelWR = enable;
		mHighResAccelWR = !enable;
		setLSM303AccelRateFromFreq(mMaxSetShimmerSamplingRate);
	}
	
	
	public void	setLowPowerMag(boolean enable){
		mLowPowerMag = enable;
		setLSM303MagRateFromFreq(mMaxSetShimmerSamplingRate);
	}
		
	
	public void setLSM303AccelRange(int valueToSet){
		if(ArrayUtils.contains(ListofLSM303DLHCAccelRangeConfigValues, valueToSet)){
			mAccelRange = valueToSet;
			updateCurrentAccelWrCalibInUse();
		}
	}


	public void setLSM303DigitalAccelRate(int valueToSet) {
		mLSM303DigitalAccelRate = valueToSet;
		//LPM is not compatible with mLSM303DigitalAccelRate == 8, set to next higher rate
		if(mLowPowerAccelWR && (valueToSet==8)) {
			mLSM303DigitalAccelRate = 9;
		}
	}
	

	public void setLSM303MagRange(int valueToSet){
		if(ArrayUtils.contains(ListofMagRangeConfigValues, valueToSet)){
			mMagRange = valueToSet;
			updateCurrentAccelWrCalibInUse();
		}
	}
	
	/**
	 * Computes next higher available sensor sampling rate setting based on
	 * passed in "freq" variable and dependent on whether low-power mode is set.
	 * 
	 * @param freq
	 * @return int the rate configuration setting for the respective sensor
	 */
	private int setLSM303AccelRateFromFreq(double freq) {
		boolean isEnabled = isSensorEnabled(Configuration.Shimmer3.SensorMapKey.SHIMMER_LSM303DLHC_ACCEL);
		mLSM303DigitalAccelRate = SensorLSM303.getAccelRateFromFreq(isEnabled, freq, mLowPowerAccelWR);
		return mLSM303DigitalAccelRate;
	}
	
	/**
	 * Unused: 8 = 1.620kHz (only low-power mode), 9 = 1.344kHz (normal-mode) / 5.376kHz (low-power mode)
	 * */
	public static int getAccelRateFromFreq(boolean isEnabled, double freq, boolean isLowPowerMode) {
		int accelRate = 0; // Power down
		
		if(isEnabled){
			if (freq<=1){
				accelRate = 1; // 1Hz
			} else if (freq<=10 || isLowPowerMode){ // 'Low-power mode' max is 10 - Shimmer defined
				accelRate = 2; // 10Hz
			} else if (freq<=25){
				accelRate = 3; // 25Hz
			} else if (freq<=50){
				accelRate = 4; // 50Hz
			} else if (freq<=100){
				accelRate = 5; // 100Hz
			} else if (freq<=200){
				accelRate = 6; // 200Hz
			} else if (freq<=400){
				accelRate = 7; // 400Hz
			} else {
				accelRate = 9; // 1344Hz
			}
		}
		return accelRate;
	}
	
	
	/**
	 * Computes next higher available sensor sampling rate setting based on
	 * passed in "freq" variable and dependent on whether low-power mode is set.
	 * 
	 * @param freq
	 * @return int the rate configuration setting for the respective sensor
	 */
	private int setLSM303MagRateFromFreq(double freq) {
		boolean isEnabled = isSensorEnabled(Configuration.Shimmer3.SensorMapKey.SHIMMER_LSM303DLHC_MAG);
		mLSM303MagRate = SensorLSM303.getMagRateFromFreq(isEnabled, freq, mLowPowerMag);
		return mLSM303MagRate;
	}

	public static int getMagRateFromFreq(boolean isEnabled, double freq, boolean isLowPowerMode) {
		int magRate = 0; // 0.75Hz
		
		if(isEnabled){
			if (freq<=0.75){
				magRate = 0; // 0.75Hz
			} else if (freq<=1){
				magRate = 1; // 1.5Hz
			} else if (freq<=3) {
				magRate = 2; // 3Hz
			} else if (freq<=7.5) {
				magRate = 3; // 7.5Hz
			} else if (freq<=15 || isLowPowerMode) { // 'Low-power mode' max is 15 - Shimmer defined
				magRate = 4; // 15Hz
			} else if (freq<=30) {
				magRate = 5; // 30Hz
			} else if (freq<=75) {
				magRate = 6; // 75Hz
			} else {
				magRate = 7; // 220Hz
			}
		}
		return magRate;
	}
	
	
	public void setLSM303MagRate(int valueToSet){
		mLSM303MagRate = valueToSet;
	}
	
	public int getLSM303MagRate() {
		return mLSM303MagRate;
	}
	
	private void setDefaultLsm303dlhcMagSensorConfig(boolean isSensorEnabled) {
		if(isSensorEnabled) {
			setLowPowerMag(false);
		}
		else {
			setLSM303MagRange(1);
			setLowPowerMag(true);
		}		
	}

	
	private void setDefaultLsm303dlhcAccelSensorConfig(boolean isSensorEnabled) {
		if(isSensorEnabled) {
			setLowPowerAccelWR(false);
		}
		else {
			setLSM303AccelRange(0);
			setLowPowerAccelWR(true);
		}
	}
	
	
	public boolean isHighResAccelWr(){
		return isLSM303DigitalAccelHRM();
	}
	

	//TODO Returning same variable as isHighResAccelWr() -> remove one method?
	public boolean isLSM303DigitalAccelHRM() {
		return mHighResAccelWR;
	}
	

	//TODO Returning same variable as isLowPowerAccelWr() -> remove one method?
	public boolean isLSM303DigitalAccelLPM() {
		return mLowPowerAccelWR;
	}

	public int getLowPowerAccelEnabled(){
		return (isLSM303DigitalAccelLPM()? 1:0);
	}

	public boolean isLowPowerAccelWr(){
		return isLSM303DigitalAccelLPM();
	}
	
	//TODO Returning same variable as isLowPowerAccelWr() -> remove one method?
	public boolean isLowPowerAccelEnabled() {
		return isLSM303DigitalAccelLPM();
	}

	public boolean isUsingDefaultWRAccelParam(){
		return mCurrentCalibDetailsAccelWr.isUsingDefaultParameters(); 
	}
	
	public boolean isUsingDefaultMagParam(){
		return mCurrentCalibDetailsMag.isUsingDefaultParameters(); 
	}

	public boolean isLowPowerMagEnabled(){
		return mLowPowerMag;
	}

	public int getLowPowerMagEnabled() {
		return (isLowPowerMagEnabled()? 1:0);
	}
	

	public int getAccelRange() {
		return mAccelRange;
	}
	
	
	public int getMagRange() {
		return mMagRange;
	}
	
	public int getLSM303DigitalAccelRate() {
		return mLSM303DigitalAccelRate;
	}

	
	public double[][] getAlignmentMatrixWRAccel(){
		return mCurrentCalibDetailsAccelWr.getCurrentAlignmentMatrix();
	}

	
	public double[][] getSensitivityMatrixWRAccel(){
		return mCurrentCalibDetailsAccelWr.getCurrentSensitivityMatrix();
	}

	
	public double[][] getOffsetVectorMatrixWRAccel(){
		return mCurrentCalibDetailsAccelWr.getCurrentOffsetVector();
	}

	
	public double[][] getAlignmentMatrixMag(){
		return mCurrentCalibDetailsMag.getCurrentAlignmentMatrix();
	}

	public double[][] getSensitivityMatrixMag(){
		return mCurrentCalibDetailsMag.getCurrentSensitivityMatrix();
	}

	public double[][] getOffsetVectorMatrixMag(){
		return mCurrentCalibDetailsMag.getCurrentOffsetVector();
	}
	
	public void updateCurrentMagCalibInUse(){
		mCurrentCalibDetailsMag = getCurrentCalibDetailsMag();
	}
	
	public CalibDetailsKinematic getCurrentCalibDetailsMag(){
		return getCurrentCalibDetails(Configuration.Shimmer3.SensorMapKey.SHIMMER_LSM303DLHC_MAG, getMagRange());
	}

	public void updateCurrentAccelWrCalibInUse(){
		mCurrentCalibDetailsAccelWr = getCurrentCalibDetailsAccelWr();
	}

	public CalibDetailsKinematic getCurrentCalibDetailsAccelWr(){
		return getCurrentCalibDetails(Configuration.Shimmer3.SensorMapKey.SHIMMER_LSM303DLHC_ACCEL, getAccelRange());
	}
	
	public CalibDetailsKinematic getCurrentCalibDetails(int sensorMapKey, int range){
		CalibDetails calibPerSensor = getCalibForSensor(sensorMapKey, range);
		if(calibPerSensor!=null){
			return (CalibDetailsKinematic) calibPerSensor;
		}
		return null;
	}

	//--------- Sensor specific methods end --------------


	

	//--------- Optional methods to override in Sensor Class start --------
	@Override
	public void generateCalibMap() {
		super.generateCalibMap();

		TreeMap<Integer, CalibDetails> calibMapMag = new TreeMap<Integer, CalibDetails>();
		calibMapMag.put(calibDetailsMag1p3.mRangeValue, calibDetailsMag1p3);
		calibMapMag.put(calibDetailsMag1p9.mRangeValue, calibDetailsMag1p9);
		calibMapMag.put(calibDetailsMag2p5.mRangeValue, calibDetailsMag2p5);
		calibMapMag.put(calibDetailsMag4p0.mRangeValue, calibDetailsMag4p0);
		calibMapMag.put(calibDetailsMag4p7.mRangeValue, calibDetailsMag4p7);
		calibMapMag.put(calibDetailsMag5p6.mRangeValue, calibDetailsMag5p6);
		calibMapMag.put(calibDetailsMag8p1.mRangeValue, calibDetailsMag8p1);
		addCalibrationPerSensor(Configuration.Shimmer3.SensorMapKey.SHIMMER_LSM303DLHC_MAG, calibMapMag);
		
		TreeMap<Integer, CalibDetails> calibMapAccelWr = new TreeMap<Integer, CalibDetails>();
		calibMapAccelWr.put(calibDetailsAccelWr2g.mRangeValue, calibDetailsAccelWr2g);
		calibMapAccelWr.put(calibDetailsAccelWr4g.mRangeValue, calibDetailsAccelWr4g);
		calibMapAccelWr.put(calibDetailsAccelWr8g.mRangeValue, calibDetailsAccelWr8g);
		calibMapAccelWr.put(calibDetailsAccelWr16g.mRangeValue, calibDetailsAccelWr16g);
		addCalibrationPerSensor(Configuration.Shimmer3.SensorMapKey.SHIMMER_LSM303DLHC_ACCEL, calibMapAccelWr);
		
		updateCurrentMagCalibInUse();
		updateCurrentAccelWrCalibInUse();

	}
	
	/* (non-Javadoc)
	 * @see com.shimmerresearch.sensors.AbstractSensor#isSensorUsingDefaultCal(int)
	 */
	@Override
	public boolean isSensorUsingDefaultCal(int sensorMapKey) {
		if(sensorMapKey==Configuration.Shimmer3.SensorMapKey.SHIMMER_LSM303DLHC_ACCEL){
			return isUsingDefaultWRAccelParam();
		}
		else if(sensorMapKey==Configuration.Shimmer3.SensorMapKey.SHIMMER_LSM303DLHC_MAG){
			return isUsingDefaultMagParam();
		}
		return false;
	}

	
	
	//--------- Optional methods to override in Sensor Class end --------

}

