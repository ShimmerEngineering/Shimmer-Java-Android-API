package com.shimmerresearch.sensors.lsm303;

import java.util.Arrays;
import java.util.Collections;
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
import com.shimmerresearch.driver.calibration.CalibDetails;
import com.shimmerresearch.driver.calibration.CalibDetailsKinematic;
import com.shimmerresearch.driver.calibration.OldCalDetails;
import com.shimmerresearch.driver.calibration.UtilCalibration;
import com.shimmerresearch.driver.calibration.CalibDetails.CALIB_READ_SOURCE;
import com.shimmerresearch.driverUtilities.ChannelDetails;
import com.shimmerresearch.driverUtilities.ConfigOptionDetailsSensor;
import com.shimmerresearch.driverUtilities.ConfigOptionObject;
import com.shimmerresearch.driverUtilities.SensorDetails;
import com.shimmerresearch.driverUtilities.SensorDetailsRef;
import com.shimmerresearch.driverUtilities.SensorGroupingDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;
import com.shimmerresearch.driverUtilities.UtilShimmer;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_ENDIAN;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_TYPE;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_TYPE;
import com.shimmerresearch.sensors.AbstractSensor;
import com.shimmerresearch.sensors.ActionSetting;
import com.shimmerresearch.sensors.lsm303.SensorLSM303.ObjectClusterSensorName;

/**
 * Sensor class for the LSM303DLHC combined Accelerometer and Magnetometer 
 * (commonly referred to as the wide-range accel in Shimmer literature)
 * 
 * Accelerometer: If HR=1 -> 12-bit reading (left-justified) per axis, LSB. 
 * Accelerometer: If HR=0 -> 10-bit reading (left-justified) per axis, LSB. 
 * Magnetometer: one 12-bit reading (right-justified) per axis, MSB.
 * 
 * @author Ruud Stolk
 * @author Mark Nolan
 * 
 */
public class SensorLSM303DLHC extends SensorLSM303 {	

	private static final long serialVersionUID = -2119834127313796684L;

	//--------- Sensor specific variables start --------------	
	
	// ----------   Wide-range accel start ---------------

	public static final Map<String, OldCalDetails> mOldCalRangeMap;
    static {
        Map<String, OldCalDetails> aMap = new LinkedHashMap<String, OldCalDetails>();
        aMap.put("accel_wr_2g", new OldCalDetails("accel_wr_2g", Configuration.Shimmer3.SENSOR_ID.SHIMMER_LSM303_ACCEL, 0));
        aMap.put("accel_wr_4g", new OldCalDetails("accel_wr_4g", Configuration.Shimmer3.SENSOR_ID.SHIMMER_LSM303_ACCEL, 1));
        aMap.put("accel_wr_8g", new OldCalDetails("accel_wr_8g", Configuration.Shimmer3.SENSOR_ID.SHIMMER_LSM303_ACCEL, 2));
        aMap.put("accel_wr_16g", new OldCalDetails("accel_wr_16g", Configuration.Shimmer3.SENSOR_ID.SHIMMER_LSM303_ACCEL, 3));
        
        aMap.put("mag_13ga", new OldCalDetails("mag_13ga", Configuration.Shimmer3.SENSOR_ID.SHIMMER_LSM303_MAG, 1));
        aMap.put("mag_19ga", new OldCalDetails("mag_19ga", Configuration.Shimmer3.SENSOR_ID.SHIMMER_LSM303_MAG, 2));
        aMap.put("mag_25ga", new OldCalDetails("mag_25ga", Configuration.Shimmer3.SENSOR_ID.SHIMMER_LSM303_MAG, 3));
        aMap.put("mag_4ga",  new OldCalDetails("mag_4ga",  Configuration.Shimmer3.SENSOR_ID.SHIMMER_LSM303_MAG, 4));
        aMap.put("mag_47ga", new OldCalDetails("mag_47ga", Configuration.Shimmer3.SENSOR_ID.SHIMMER_LSM303_MAG, 5));
        aMap.put("mag_56ga", new OldCalDetails("mag_56ga", Configuration.Shimmer3.SENSOR_ID.SHIMMER_LSM303_MAG, 6));
        aMap.put("mag_81ga", new OldCalDetails("mag_81ga", Configuration.Shimmer3.SENSOR_ID.SHIMMER_LSM303_MAG, 7));

        mOldCalRangeMap = Collections.unmodifiableMap(aMap);
    }

	public static final double[][] DefaultAlignmentLSM303DLHC = {{-1,0,0},{0,1,0},{0,0,-1}};	

	public static final double[][] DefaultAlignmentMatrixWideRangeAccelShimmer3 = DefaultAlignmentLSM303DLHC;	
	public static final double[][] DefaultOffsetVectorWideRangeAccelShimmer3 = {{0},{0},{0}};	
	// Manufacturer stated +-2g: 1 mg/LSB -> or 1000 LSB/g with 16-bit left-aligned data 
	// -> bitshift by dividing by 16 and then by gravity -> 1631 LSB/(m/s2) 
	public static final double[][] DefaultSensitivityMatrixWideRangeAccel2gShimmer3 = {{1631,0,0},{0,1631,0},{0,0,1631}};
	// Manufacturer stated +-4g: 2 mg/LSB -> or 500 LSB/g with 16-bit left-aligned data 
	// -> bitshift by dividing by 16 and then by gravity -> 815.49 LSB/(m/s2)
	public static final double[][] DefaultSensitivityMatrixWideRangeAccel4gShimmer3 = {{815,0,0},{0,815,0},{0,0,815}};
	// Manufacturer stated +-8g: 4 mg/LSB -> or 250 LSB/g with 16-bit left-aligned data 
	// -> bitshift by dividing by 16 and then by gravity -> 407.75 LSB/(m/s2)
	public static final double[][] DefaultSensitivityMatrixWideRangeAccel8gShimmer3 = {{408,0,0},{0,408,0},{0,0,408}};
	// Manufacturer stated +-16g: 12 mg/LSB -> or 83.3 LSB/g with 16-bit left-aligned data 
	// -> bitshift by dividing by 16 and then by gravity -> 135.92 LSB/(m/s2)
	public static final double[][] DefaultSensitivityMatrixWideRangeAccel16gShimmer3 = {{135,0,0},{0,135,0},{0,0,135}};

	private CalibDetailsKinematic calibDetailsAccelWr2g = new CalibDetailsKinematic(
			ListofLSM303AccelRangeConfigValues[0],
			ListofLSM303AccelRange[0],
			DefaultAlignmentMatrixWideRangeAccelShimmer3, 
			DefaultSensitivityMatrixWideRangeAccel2gShimmer3, 
			DefaultOffsetVectorWideRangeAccelShimmer3);
	private CalibDetailsKinematic calibDetailsAccelWr4g = new CalibDetailsKinematic(
			ListofLSM303AccelRangeConfigValues[1], 
			ListofLSM303AccelRange[1],
			DefaultAlignmentMatrixWideRangeAccelShimmer3,
			DefaultSensitivityMatrixWideRangeAccel4gShimmer3, 
			DefaultOffsetVectorWideRangeAccelShimmer3);
	private CalibDetailsKinematic calibDetailsAccelWr8g = new CalibDetailsKinematic(
			ListofLSM303AccelRangeConfigValues[2], 
			ListofLSM303AccelRange[2],
			DefaultAlignmentMatrixWideRangeAccelShimmer3, 
			DefaultSensitivityMatrixWideRangeAccel8gShimmer3, 
			DefaultOffsetVectorWideRangeAccelShimmer3);
	private CalibDetailsKinematic calibDetailsAccelWr16g = new CalibDetailsKinematic(
			ListofLSM303AccelRangeConfigValues[3], 
			ListofLSM303AccelRange[3],
			DefaultAlignmentMatrixWideRangeAccelShimmer3,
			DefaultSensitivityMatrixWideRangeAccel16gShimmer3, 
			DefaultOffsetVectorWideRangeAccelShimmer3);
	

	// ----------   Wide-range accel end ---------------

	// ----------   Mag start ---------------
	public static final double[][] DefaultAlignmentMatrixMagShimmer3 = DefaultAlignmentLSM303DLHC; 				
	public static final double[][] DefaultOffsetVectorMagShimmer3 = {{0},{0},{0}};	
	// Manufacturer stated: X any Y axis @ 1100 LSB/gauss, Z axis @ 980 LSB/gauss
	public static final double[][] DefaultSensitivityMatrixMag1p3GaShimmer3 = {{1100,0,0},{0,1100,0},{0,0,980}};
	// Manufacturer stated: X any Y axis @ 855 LSB/gauss, Z axis @ 760 LSB/gauss
	public static final double[][] DefaultSensitivityMatrixMag1p9GaShimmer3 = {{855,0,0},{0,855,0},{0,0,760}};
	// Manufacturer stated: X any Y axis @ 670 LSB/gauss, Z axis @ 600 LSB/gauss
	public static final double[][] DefaultSensitivityMatrixMag2p5GaShimmer3 = {{670,0,0},{0,670,0},{0,0,600}};
	// Manufacturer stated: X any Y axis @ 450 LSB/gauss, Z axis @ 400 LSB/gauss
	public static final double[][] DefaultSensitivityMatrixMag4GaShimmer3 = {{450,0,0},{0,450,0},{0,0,400}};
	// Manufacturer stated: X any Y axis @ 400 LSB/gauss, Z axis @ 355 LSB/gauss
	public static final double[][] DefaultSensitivityMatrixMag4p7GaShimmer3 = {{400,0,0},{0,400,0},{0,0,355}};
	// Manufacturer stated: X any Y axis @ 330 LSB/gauss, Z axis @ 295 LSB/gauss
	public static final double[][] DefaultSensitivityMatrixMag5p6GaShimmer3 = {{330,0,0},{0,330,0},{0,0,295}};
	// Manufacturer stated: X any Y axis @ 230 LSB/gauss, Z axis @ 205 LSB/gauss
	public static final double[][] DefaultSensitivityMatrixMag8p1GaShimmer3 = {{230,0,0},{0,230,0},{0,0,205}};

	private CalibDetailsKinematic calibDetailsMag1p3 = new CalibDetailsKinematic(
			ListofLSM303DLHCMagRangeConfigValues[0],
			ListofLSM303DLHCMagRange[0],
			DefaultAlignmentMatrixMagShimmer3,
			DefaultSensitivityMatrixMag1p3GaShimmer3,
			DefaultOffsetVectorMagShimmer3);
	private CalibDetailsKinematic calibDetailsMag1p9 = new CalibDetailsKinematic(
			ListofLSM303DLHCMagRangeConfigValues[1],
			ListofLSM303DLHCMagRange[1],
			DefaultAlignmentMatrixMagShimmer3, 
			DefaultSensitivityMatrixMag1p9GaShimmer3,
			DefaultOffsetVectorMagShimmer3);
	private CalibDetailsKinematic calibDetailsMag2p5 = new CalibDetailsKinematic(
			ListofLSM303DLHCMagRangeConfigValues[2], 
			ListofLSM303DLHCMagRange[2],
			DefaultAlignmentMatrixMagShimmer3,
			DefaultSensitivityMatrixMag2p5GaShimmer3, 
			DefaultOffsetVectorMagShimmer3);
	private CalibDetailsKinematic calibDetailsMag4p0 = new CalibDetailsKinematic(
			ListofLSM303DLHCMagRangeConfigValues[3],
			ListofLSM303DLHCMagRange[3],
			DefaultAlignmentMatrixMagShimmer3,
			DefaultSensitivityMatrixMag4GaShimmer3,
			DefaultOffsetVectorMagShimmer3);
	private CalibDetailsKinematic calibDetailsMag4p7 = new CalibDetailsKinematic(
			ListofLSM303DLHCMagRangeConfigValues[4],
			ListofLSM303DLHCMagRange[4],
			DefaultAlignmentMatrixMagShimmer3, 
			DefaultSensitivityMatrixMag4p7GaShimmer3,
			DefaultOffsetVectorMagShimmer3);
	private CalibDetailsKinematic calibDetailsMag5p6 = new CalibDetailsKinematic(
			ListofLSM303DLHCMagRangeConfigValues[5],
			ListofLSM303DLHCMagRange[5],
			DefaultAlignmentMatrixMagShimmer3, 
			DefaultSensitivityMatrixMag5p6GaShimmer3,
			DefaultOffsetVectorMagShimmer3);
	private CalibDetailsKinematic calibDetailsMag8p1 = new CalibDetailsKinematic(
			ListofLSM303DLHCMagRangeConfigValues[6],
			ListofLSM303DLHCMagRange[6],
			DefaultAlignmentMatrixMagShimmer3, 
			DefaultSensitivityMatrixMag8p1GaShimmer3, 
			DefaultOffsetVectorMagShimmer3);
	
	// ----------   Mag end ---------------
	
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
//		public static final String MAG = "LSM303DLHC_Mag";
		
//		public static final String WR_ACC = "LSM303DLHC_Acc";
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
	
	public static final Integer[] ListofLSM303AccelRangeConfigValues={0,1,2,3};  

	public static final String[] ListofLSM303DLHCAccelRateHr={"Power-down","1.0Hz","10.0Hz","25.0Hz","50.0Hz","100.0Hz","200.0Hz","400.0Hz","1344.0Hz"};
	public static final Integer[] ListofLSM303DLHCAccelRateHrConfigValues={0,1,2,3,4,5,6,7,9};

	public static final String[] ListofLSM303DLHCAccelRateLpm={"Power-down","1.0Hz","10.0Hz","25.0Hz","50.0Hz","100.0Hz","200.0Hz","400.0Hz","1620.0Hz","5376.0Hz"}; // 1620Hz and 5376Hz are only available in low-power mode
	public static final Integer[] ListofLSM303DLHCAccelRateLpmConfigValues={0,1,2,3,4,5,6,7,8,9};

	public static final ConfigOptionDetailsSensor configOptionAccelRange = new ConfigOptionDetailsSensor(
			SensorLSM303.GuiLabelConfig.LSM303_ACCEL_RANGE,
			SensorLSM303DLHC.DatabaseConfigHandle.WR_ACC_RANGE,
			ListofLSM303AccelRange, 
			ListofLSM303AccelRangeConfigValues, 
			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW);

	public static final ConfigOptionDetailsSensor configOptionAccelRate = new ConfigOptionDetailsSensor(
			SensorLSM303.GuiLabelConfig.LSM303_ACCEL_RATE,
			SensorLSM303DLHC.DatabaseConfigHandle.WR_ACC_RATE,
			ListofLSM303DLHCAccelRateHr, 
			ListofLSM303DLHCAccelRateHrConfigValues, 
			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW,
			Arrays.asList(
				new ConfigOptionObject(ConfigOptionDetailsSensor.VALUE_INDEXES.LSM303_ACCEL_RATE.IS_LPM, 
						SensorLSM303DLHC.ListofLSM303DLHCAccelRateLpm, 
						SensorLSM303DLHC.ListofLSM303DLHCAccelRateLpmConfigValues)));

	
	public static final String[] ListofLSM303DLHCMagRate={"0.75Hz","1.5Hz","3.0Hz","7.5Hz","15.0Hz","30.0Hz","75.0Hz","220.0Hz"};
	public static final Integer[] ListofLSM303DLHCMagRateConfigValues={0,1,2,3,4,5,6,7};

	public static final String[] ListofLSM303DLHCMagRange={"+/- 1.3Ga","+/- 1.9Ga","+/- 2.5Ga","+/- 4.0Ga","+/- 4.7Ga","+/- 5.6Ga","+/- 8.1Ga"}; 
	public static final Integer[] ListofLSM303DLHCMagRangeConfigValues={1,2,3,4,5,6,7}; // no '0' option  

	public static final ConfigOptionDetailsSensor configOptionMagRange = new ConfigOptionDetailsSensor(
			SensorLSM303.GuiLabelConfig.LSM303_MAG_RANGE,
			SensorLSM303DLHC.DatabaseConfigHandle.MAG_RANGE,
			ListofLSM303DLHCMagRange, 
			ListofLSM303DLHCMagRangeConfigValues, 
			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW);

	public static final ConfigOptionDetailsSensor configOptionMagRate = new ConfigOptionDetailsSensor(
			SensorLSM303.GuiLabelConfig.LSM303_MAG_RATE,
			SensorLSM303DLHC.DatabaseConfigHandle.MAG_RATE,
			ListofLSM303DLHCMagRate, 
			ListofLSM303DLHCMagRateConfigValues, 
			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW);
	
	public static final ConfigOptionDetailsSensor configOptionAccelLpm = new ConfigOptionDetailsSensor(
			SensorLSM303.GuiLabelConfig.LSM303_ACCEL_LPM,
			SensorLSM303DLHC.DatabaseConfigHandle.WR_ACC_LPM,
			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.CHECKBOX);

	//TODO not currently added to the maps in the Sensor classes, only ShimmerObject 
	public static final ConfigOptionDetailsSensor configOptionMagLpm = new ConfigOptionDetailsSensor(
			SensorLSM303.GuiLabelConfig.LSM303_MAG_LPM,
			null,
			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.CHECKBOX,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW);


	
	//--------- Configuration options end --------------
	
	
	//--------- Sensor info start --------------
	public static final SensorDetailsRef sensorLSM303DLHCAccel = new SensorDetailsRef(
			0x10<<8, //== Configuration.Shimmer3.SensorBitmap.SENSOR_D_ACCEL will be: SensorBitmap.SENSOR_D_ACCEL
			0x10<<8, //== Configuration.Shimmer3.SensorBitmap.SENSOR_D_ACCEL will be: SensorBitmap.SENSOR_D_ACCEL
			GuiLabelSensors.ACCEL_WR,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW,
			Arrays.asList(GuiLabelConfig.LSM303_ACCEL_RANGE,
				GuiLabelConfig.LSM303_ACCEL_RATE),
			Arrays.asList(ObjectClusterSensorName.ACCEL_WR_X,
					ObjectClusterSensorName.ACCEL_WR_Y,
					ObjectClusterSensorName.ACCEL_WR_Z));
	
	public static final SensorDetailsRef sensorLSM303DLHCMag = new SensorDetailsRef(
			0x20, //== Configuration.Shimmer3.SensorBitmap.SENSOR_MAG will be: SensorBitmap.SENSOR_MAG, 
			0x20, //== Configuration.Shimmer3.SensorBitmap.SENSOR_MAG will be: SensorBitmap.SENSOR_MAG, 
			GuiLabelSensors.MAG,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW,
			Arrays.asList(GuiLabelConfig.LSM303_MAG_RANGE,
					GuiLabelConfig.LSM303_MAG_RATE),
			//MAG channel parsing order is XZY instead of XYZ but it would be better to represent it on the GUI in XYZ
			Arrays.asList(ObjectClusterSensorName.MAG_X,
					ObjectClusterSensorName.MAG_Y,
					ObjectClusterSensorName.MAG_Z));
	
	public static final Map<Integer, SensorDetailsRef> mSensorMapRef;
    static {
        Map<Integer, SensorDetailsRef> aMap = new LinkedHashMap<Integer, SensorDetailsRef>();
        aMap.put(Configuration.Shimmer3.SENSOR_ID.SHIMMER_LSM303_ACCEL, SensorLSM303DLHC.sensorLSM303DLHCAccel);  
        aMap.put(Configuration.Shimmer3.SENSOR_ID.SHIMMER_LSM303_MAG, SensorLSM303DLHC.sensorLSM303DLHCMag);	
		mSensorMapRef = Collections.unmodifiableMap(aMap);
    }
	//--------- Sensor info end --------------
	
	//--------- Channel info start --------------
    public static final ChannelDetails channelLSM303DLHCAccelX = new ChannelDetails(
			ObjectClusterSensorName.ACCEL_WR_X,
			ObjectClusterSensorName.ACCEL_WR_X,
			DatabaseChannelHandles.WR_ACC_X,
			CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.LSB,
			CHANNEL_UNITS.METER_PER_SECOND_SQUARE,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL),
			0x04);
    
    public static final ChannelDetails channelLSM303DLHCAccelY = new ChannelDetails(
			ObjectClusterSensorName.ACCEL_WR_Y,
			ObjectClusterSensorName.ACCEL_WR_Y,
			DatabaseChannelHandles.WR_ACC_Y,
			CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.LSB,
			CHANNEL_UNITS.METER_PER_SECOND_SQUARE,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL),
			0x05);
    
    public static final ChannelDetails channelLSM303DLHCAccelZ = new ChannelDetails(
			ObjectClusterSensorName.ACCEL_WR_Z,
			ObjectClusterSensorName.ACCEL_WR_Z,
			DatabaseChannelHandles.WR_ACC_Z,
			CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.LSB,
			CHANNEL_UNITS.METER_PER_SECOND_SQUARE,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL),
			0x06);
    
    public static final ChannelDetails channelLSM303DLHCMagX = new ChannelDetails(
			ObjectClusterSensorName.MAG_X,
			ObjectClusterSensorName.MAG_X,
			DatabaseChannelHandles.MAG_X,
			CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.MSB,
			CHANNEL_UNITS.LOCAL_FLUX,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL),
			0x07);
	
	public static final ChannelDetails channelLSM303DLHCMagY = new ChannelDetails(
			ObjectClusterSensorName.MAG_Y,
			ObjectClusterSensorName.MAG_Y,
			DatabaseChannelHandles.MAG_Y,
			CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.MSB,
			CHANNEL_UNITS.LOCAL_FLUX,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL),
			0x08);
	
	public static final ChannelDetails channelLSM303DLHCMagZ = new ChannelDetails(
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
        aMap.put(SensorLSM303DLHC.ObjectClusterSensorName.ACCEL_WR_X, SensorLSM303DLHC.channelLSM303DLHCAccelX);
        aMap.put(SensorLSM303DLHC.ObjectClusterSensorName.ACCEL_WR_Y, SensorLSM303DLHC.channelLSM303DLHCAccelY);
        aMap.put(SensorLSM303DLHC.ObjectClusterSensorName.ACCEL_WR_Z, SensorLSM303DLHC.channelLSM303DLHCAccelZ);
        aMap.put(SensorLSM303DLHC.ObjectClusterSensorName.MAG_X, SensorLSM303DLHC.channelLSM303DLHCMagX);
        aMap.put(SensorLSM303DLHC.ObjectClusterSensorName.MAG_Z, SensorLSM303DLHC.channelLSM303DLHCMagZ);
        aMap.put(SensorLSM303DLHC.ObjectClusterSensorName.MAG_Y, SensorLSM303DLHC.channelLSM303DLHCMagY);
		mChannelMapRef = Collections.unmodifiableMap(aMap);
    }
	//--------- Channel info end --------------
	
    public static final SensorGroupingDetails sensorGroupLsmAccel = new SensorGroupingDetails(
			LABEL_SENSOR_TILE.WIDE_RANGE_ACCEL,
			Arrays.asList(Configuration.Shimmer3.SENSOR_ID.SHIMMER_LSM303_ACCEL),
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW);
    
    public static final SensorGroupingDetails sensorGroupLsmMag = new SensorGroupingDetails(
			LABEL_SENSOR_TILE.MAG,
			Arrays.asList(Configuration.Shimmer3.SENSOR_ID.SHIMMER_LSM303_MAG),
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW);
    
    //--------- Constructors for this class start --------------
    /** This constructor is just used for accessing calibration*/
    public SensorLSM303DLHC() {
		super();
		initialise();
    }
    
	public SensorLSM303DLHC(ShimmerDevice shimmerDevice) {
		super(shimmerDevice);
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
		addConfigOption(configOptionAccelRange);
		addConfigOption(configOptionMagRange);
		addConfigOption(configOptionAccelRate);
		addConfigOption(configOptionMagRate);
		addConfigOption(configOptionAccelLpm);
	}
	
	@Override 
	public void generateSensorGroupMapping() {
		mSensorGroupingMap = new LinkedHashMap<Integer, SensorGroupingDetails>();
		if(mShimmerVerObject.isShimmerGen3() || mShimmerVerObject.isShimmerGen4()){
			mSensorGroupingMap.put(Configuration.Shimmer3.LABEL_SENSOR_TILE.WIDE_RANGE_ACCEL.ordinal(), sensorGroupLsmAccel);
			mSensorGroupingMap.put(Configuration.Shimmer3.LABEL_SENSOR_TILE.MAG.ordinal(), sensorGroupLsmMag);
		}
		super.updateSensorGroupingMap();	
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
	public LinkedHashMap<String, Object> generateConfigMap() {
		LinkedHashMap<String, Object> mapOfConfig = new LinkedHashMap<String, Object>();
		
		mapOfConfig.put(SensorLSM303DLHC.DatabaseConfigHandle.WR_ACC_RATE, getLSM303DigitalAccelRate());
		mapOfConfig.put(SensorLSM303DLHC.DatabaseConfigHandle.WR_ACC_RANGE, getAccelRange());
		mapOfConfig.put(SensorLSM303DLHC.DatabaseConfigHandle.WR_ACC_LPM, getLowPowerAccelEnabled());
		mapOfConfig.put(SensorLSM303DLHC.DatabaseConfigHandle.WR_ACC_HRM, getHighResAccelWREnabled());
		
		super.addCalibDetailsToDbMap(mapOfConfig, 
				getCurrentCalibDetailsAccelWr(), 
				SensorLSM303DLHC.DatabaseConfigHandle.LIST_OF_CALIB_HANDLES_WR_ACCEL,
				SensorLSM303DLHC.DatabaseConfigHandle.WR_ACC_CALIB_TIME);

		mapOfConfig.put(SensorLSM303DLHC.DatabaseConfigHandle.MAG_RANGE, getMagRange());
		mapOfConfig.put(SensorLSM303DLHC.DatabaseConfigHandle.MAG_RATE, getLSM303MagRate());

		super.addCalibDetailsToDbMap(mapOfConfig, 
				getCurrentCalibDetailsMag(), 
				SensorLSM303DLHC.DatabaseConfigHandle.LIST_OF_CALIB_HANDLES_MAG,
				SensorLSM303DLHC.DatabaseConfigHandle.MAG_CALIB_TIME);

		return mapOfConfig;
	}	
	
	@Override
	public void parseConfigMap(LinkedHashMap<String, Object> mapOfConfigPerShimmer) {
		
		//Better if LPM/HRM are processed first as they can override the sampling rate
		if(mapOfConfigPerShimmer.containsKey(SensorLSM303DLHC.DatabaseConfigHandle.WR_ACC_LPM)){
			setLowPowerAccelWR(((Double) mapOfConfigPerShimmer.get(SensorLSM303DLHC.DatabaseConfigHandle.WR_ACC_LPM))>0? true:false);
		}
		if(mapOfConfigPerShimmer.containsKey(SensorLSM303DLHC.DatabaseConfigHandle.WR_ACC_HRM)){
			setHighResAccelWR(((Double) mapOfConfigPerShimmer.get(SensorLSM303DLHC.DatabaseConfigHandle.WR_ACC_HRM))>0? true:false);
		}
		if(mapOfConfigPerShimmer.containsKey(SensorLSM303DLHC.DatabaseConfigHandle.WR_ACC_RATE)){
			setLSM303DigitalAccelRate(((Double) mapOfConfigPerShimmer.get(SensorLSM303DLHC.DatabaseConfigHandle.WR_ACC_RATE)).intValue());
		}
		if(mapOfConfigPerShimmer.containsKey(SensorLSM303DLHC.DatabaseConfigHandle.WR_ACC_RANGE)){
			setLSM303AccelRange(((Double) mapOfConfigPerShimmer.get(SensorLSM303DLHC.DatabaseConfigHandle.WR_ACC_RANGE)).intValue());
		}
		
		//Digital Accel Calibration Configuration
		parseCalibDetailsKinematicFromDb(mapOfConfigPerShimmer, 
				Configuration.Shimmer3.SENSOR_ID.SHIMMER_LSM303_ACCEL, 
				getAccelRange(), 
				SensorLSM303DLHC.DatabaseConfigHandle.LIST_OF_CALIB_HANDLES_WR_ACCEL,
				SensorLSM303DLHC.DatabaseConfigHandle.WR_ACC_CALIB_TIME);
		
		
		if(mapOfConfigPerShimmer.containsKey(SensorLSM303DLHC.DatabaseConfigHandle.MAG_RANGE)){
			setLSM303MagRange(((Double) mapOfConfigPerShimmer.get(SensorLSM303DLHC.DatabaseConfigHandle.MAG_RANGE)).intValue());
		}
		if(mapOfConfigPerShimmer.containsKey(SensorLSM303DLHC.DatabaseConfigHandle.MAG_RATE)){
			setLSM303MagRate(((Double) mapOfConfigPerShimmer.get(SensorLSM303DLHC.DatabaseConfigHandle.MAG_RATE)).intValue());
		}
		
		//Magnetometer Calibration Configuration
		parseCalibDetailsKinematicFromDb(mapOfConfigPerShimmer, 
				Configuration.Shimmer3.SENSOR_ID.SHIMMER_LSM303_MAG, 
				getMagRange(), 
				SensorLSM303DLHC.DatabaseConfigHandle.LIST_OF_CALIB_HANDLES_MAG,
				SensorLSM303DLHC.DatabaseConfigHandle.MAG_CALIB_TIME);
	}


	@Override
	public boolean processResponse(int responseCommand, Object parsedResponse, COMMUNICATION_TYPE commType) {
		// TODO Auto-generated method stub
		return false;
	}
	
	//--------- Abstract methods implemented end --------------

	//--------- Optional methods to override in Sensor Class start --------
	@Override
	public void initialise() {
		mSensorIdAccel = Configuration.Shimmer3.SENSOR_ID.SHIMMER_LSM303_ACCEL;
		mSensorIdMag = Configuration.Shimmer3.SENSOR_ID.SHIMMER_LSM303_MAG;
		super.initialise();
		
		mMagRange = ListofLSM303DLHCMagRangeConfigValues[0];
		
		updateCurrentAccelWrCalibInUse();
		updateCurrentMagCalibInUse();
	}

	@Override
	public void generateCalibMap() {
		super.generateCalibMap();
		
		TreeMap<Integer, CalibDetails> calibMapAccelWr = new TreeMap<Integer, CalibDetails>();
		calibMapAccelWr.put(calibDetailsAccelWr2g.mRangeValue, calibDetailsAccelWr2g);
		calibMapAccelWr.put(calibDetailsAccelWr4g.mRangeValue, calibDetailsAccelWr4g);
		calibMapAccelWr.put(calibDetailsAccelWr8g.mRangeValue, calibDetailsAccelWr8g);
		calibMapAccelWr.put(calibDetailsAccelWr16g.mRangeValue, calibDetailsAccelWr16g);
		setCalibrationMapPerSensor(Configuration.Shimmer3.SENSOR_ID.SHIMMER_LSM303_ACCEL, calibMapAccelWr);

		updateCurrentAccelWrCalibInUse();

		TreeMap<Integer, CalibDetails> calibMapMag = new TreeMap<Integer, CalibDetails>();
		calibMapMag.put(calibDetailsMag1p3.mRangeValue, calibDetailsMag1p3);
		calibMapMag.put(calibDetailsMag1p9.mRangeValue, calibDetailsMag1p9);
		calibMapMag.put(calibDetailsMag2p5.mRangeValue, calibDetailsMag2p5);
		calibMapMag.put(calibDetailsMag4p0.mRangeValue, calibDetailsMag4p0);
		calibMapMag.put(calibDetailsMag4p7.mRangeValue, calibDetailsMag4p7);
		calibMapMag.put(calibDetailsMag5p6.mRangeValue, calibDetailsMag5p6);
		calibMapMag.put(calibDetailsMag8p1.mRangeValue, calibDetailsMag8p1);
		setCalibrationMapPerSensor(Configuration.Shimmer3.SENSOR_ID.SHIMMER_LSM303_MAG, calibMapMag);
		
		updateCurrentMagCalibInUse();
	}
	
	//--------- Optional methods to override in Sensor Class end --------

	//--------- Sensor specific methods start --------------
	
	@Override
	public void setLSM303AccelRange(int valueToSet){
		if(ArrayUtils.contains(ListofLSM303AccelRangeConfigValues, valueToSet)){
			mAccelRange = valueToSet;
			updateCurrentAccelWrCalibInUse();
		}
	}

	@Override
	public void setLSM303MagRange(int valueToSet){
		if(ArrayUtils.contains(ListofLSM303DLHCMagRangeConfigValues, valueToSet)){
//			if(valueToSet!=7){
//				UtilShimmer.consolePrintCurrentStackTrace();
//			}
			mMagRange = valueToSet;
			updateCurrentAccelWrCalibInUse();
		}
	}
	
	@Override
	public boolean checkLowPowerMag() {
		setLowPowerMag((getLSM303MagRate() <= 4)? true:false); // <=15Hz
		return isLowPowerMagEnabled();
	}

	
	@Override
	public int getAccelRateFromFreqForSensor(boolean isEnabled, double freq, boolean isLowPowerMode) {
		return SensorLSM303DLHC.getAccelRateFromFreq(isEnabled, freq, isLowPowerMode);
	}
	
	@Override
	public void setLSM303DigitalAccelRate(int valueToSet) {
		super.setLSM303DigitalAccelRateInternal(valueToSet);
		//LPM is not compatible with mLSM303DigitalAccelRate == 8, set to next higher rate
		if(mLowPowerAccelWR && valueToSet==8) {
			super.setLSM303DigitalAccelRateInternal(9);
		}
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
	
	@Override
	public int getMagRateFromFreqForSensor(boolean isEnabled, double freq, boolean isLowPowerMode) {
		return SensorLSM303DLHC.getMagRateFromFreq(isEnabled, freq, isLowPowerMode);
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

	public static String parseFromDBColumnToGUIChannel(String databaseChannelHandle) {
		//TODO Old approach, can be removed
//		String objectClusterName = "";
//		if (databaseChannelHandle.equals(SensorLSM303DLHC.DatabaseChannelHandles.WR_ACC_X)) {
//			objectClusterName = SensorLSM303.ObjectClusterSensorName.ACCEL_WR_X;
//		} else if (databaseChannelHandle.equals(SensorLSM303DLHC.DatabaseChannelHandles.WR_ACC_Y)) {
//			objectClusterName = SensorLSM303.ObjectClusterSensorName.ACCEL_WR_Y;
//		} else if (databaseChannelHandle.equals(SensorLSM303DLHC.DatabaseChannelHandles.WR_ACC_Z)) {
//			objectClusterName = SensorLSM303.ObjectClusterSensorName.ACCEL_WR_Z;
//		} else if (databaseChannelHandle.equals(SensorLSM303DLHC.DatabaseChannelHandles.MAG_X)) {
//			objectClusterName = SensorLSM303.ObjectClusterSensorName.MAG_X;
//		} else if (databaseChannelHandle.equals(SensorLSM303DLHC.DatabaseChannelHandles.MAG_Y)) {
//			objectClusterName = SensorLSM303.ObjectClusterSensorName.MAG_Y;
//		} else if (databaseChannelHandle.equals(SensorLSM303DLHC.DatabaseChannelHandles.MAG_Z)) {
//			objectClusterName = SensorLSM303.ObjectClusterSensorName.MAG_Z;
//		}
//		return objectClusterName;
		
		return AbstractSensor.parseFromDBColumnToGUIChannel(mChannelMapRef, databaseChannelHandle);
	}

	public static String parseFromGUIChannelsToDBColumn(String objectClusterName) {
		//TODO Old approach, can be removed
//		String databaseChannelHandle = "";
//		if (objectClusterName.equals(SensorLSM303.ObjectClusterSensorName.ACCEL_WR_X)) {
//			databaseChannelHandle = SensorLSM303DLHC.DatabaseChannelHandles.WR_ACC_X;
//		} else if (objectClusterName.equals(SensorLSM303.ObjectClusterSensorName.ACCEL_WR_Y)) {
//			databaseChannelHandle = SensorLSM303DLHC.DatabaseChannelHandles.WR_ACC_Y;
//		} else if (objectClusterName.equals(SensorLSM303.ObjectClusterSensorName.ACCEL_WR_Z)) {
//			databaseChannelHandle = SensorLSM303DLHC.DatabaseChannelHandles.WR_ACC_Z;
//		} else if (objectClusterName.equals(SensorLSM303.ObjectClusterSensorName.MAG_X)) {
//			databaseChannelHandle = SensorLSM303DLHC.DatabaseChannelHandles.MAG_X;
//		} else if (objectClusterName.equals(SensorLSM303.ObjectClusterSensorName.MAG_Y)) {
//			databaseChannelHandle = SensorLSM303DLHC.DatabaseChannelHandles.MAG_Y;
//		} else if (objectClusterName.equals(SensorLSM303.ObjectClusterSensorName.MAG_Z)) {
//			databaseChannelHandle = SensorLSM303DLHC.DatabaseChannelHandles.MAG_Z;
//		}
//		return dbColumn;
		
		return AbstractSensor.parseFromGUIChannelsToDBColumn(mChannelMapRef, objectClusterName);
	}
	
	//--------- Sensor specific methods end --------------

}

