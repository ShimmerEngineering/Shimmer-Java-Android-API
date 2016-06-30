package com.shimmerresearch.sensors;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import com.shimmerresearch.bluetooth.BtCommandDetails;
import com.shimmerresearch.driver.Configuration.CHANNEL_UNITS;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driver.Configuration.Shimmer3;
import com.shimmerresearch.driver.Configuration.Shimmer3.CompatibilityInfoForMaps;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driverUtilities.CalibDetailsKinematic;
import com.shimmerresearch.driverUtilities.ChannelDetails;
import com.shimmerresearch.driverUtilities.ConfigOptionDetailsSensor;
import com.shimmerresearch.driverUtilities.SensorDetails;
import com.shimmerresearch.driverUtilities.SensorDetailsRef;
import com.shimmerresearch.driverUtilities.SensorGroupingDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;
import com.shimmerresearch.driverUtilities.UtilCalibration;
import com.shimmerresearch.driverUtilities.UtilParseData;
import com.shimmerresearch.driverUtilities.UtilShimmer;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_ENDIAN;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_TYPE;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_TYPE;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.FW_ID;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.HW_ID;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.FormatCluster;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.sensors.AbstractSensor.GuiLabelConfigCommon;
import com.shimmerresearch.sensors.SensorLSM303.GuiLabelConfig;

/**
 * @author Ronan McCormack
 * @author Mark Nolan
 */
public class SensorMPU9X50 extends AbstractSensor implements Serializable {

	/** * */
	private static final long serialVersionUID = -1137540822708521997L;
	
	//--------- Sensor specific variables start --------------
	/** This stores the current Gyro Range, it is a value between 0 and 3; 0 = +/- 250dps,1 = 500dps, 2 = 1000dps, 3 = 2000dps */
	protected int mGyroRange=1;													 
	protected boolean mLowPowerGyro = false;

//	protected int mMPU9150GyroRate = 0;

	protected int mMPU9150AccelRange=0;											// This stores the current MPU9150 Accel Range. 0 = 2g, 1 = 4g, 2 = 8g, 4 = 16g
	protected int mMPU9150GyroAccelRate=0;
	protected int mAccelRange=0;
	protected int mMagRange=1;
	protected long mConfigByte0;
	protected int mNChannels=0;	                                                // Default number of sensor channels set to three because of the on board accelerometer 
	protected int mBufferSize;  
	
	protected int mMPU9150DMP = 0;
	protected int mMPU9150LPF = 0;
	protected int mMPU9150MotCalCfg = 0;
	protected int mMPU9150MPLSamplingRate = 0;
	protected int mMPU9150MagSamplingRate = 0;
	protected int mMPLSensorFusion = 0;
	protected int mMPLGyroCalTC = 0;
	protected int mMPLVectCompCal = 0;
	protected int mMPLMagDistCal = 0;
	protected int mMPLEnable = 0;
	protected int mPacketSize=0;
	protected int mTimeStampPacketByteSize = 2;
	
	protected double[][] AlignmentMatrixMPLAccel = {{-1,0,0},{0,1,0},{0,0,-1}}; 			
	protected double[][] SensitivityMatrixMPLAccel = {{1631,0,0},{0,1631,0},{0,0,1631}}; 	
	protected double[][] OffsetVectorMPLAccel = {{0},{0},{0}};
	
	protected double[][] AlignmentMatrixMPLMag = {{-1,0,0},{0,1,0},{0,0,-1}}; 			
	protected double[][] SensitivityMatrixMPLMag = {{1631,0,0},{0,1631,0},{0,0,1631}}; 	
	protected double[][] OffsetVectorMPLMag = {{0},{0},{0}};
	
	protected double[][] AlignmentMatrixMPLGyro = {{-1,0,0},{0,1,0},{0,0,-1}}; 			
	protected double[][] SensitivityMatrixMPLGyro = {{1631,0,0},{0,1631,0},{0,0,1631}}; 	
	protected double[][] OffsetVectorMPLGyro = {{0},{0},{0}};

	//MPU Mag (AK8975A) - obtained from: 
	// http://www.akm.com/akm/en/file/datasheet/AK8975.pdf
	// https://github.com/kriswiner/MPU-9150/blob/master/MPU9150BasicAHRS.ino
	public double[][] mAlignmentMatrixMagnetometer = AlignmentMatrixMPLMag; 				
	public double[][] mSensitivityMatrixMagnetometer = {{0.3,0,0},{0,0.3,0},{0,0,0.3}}; 		
	public double[][] mOffsetVectorMagnetometer = {{-5.0},{-95.0},{-260.0}};

	// ----------   Gyro start ---------------
	/**TODO use calibration map instead*/
	@Deprecated
	public boolean mDefaultCalibrationParametersGyro = true;
	/**TODO use calibration map instead*/
	@Deprecated
	public double[][] mAlignmentMatrixGyroscope = {{0,-1,0},{-1,0,0},{0,0,-1}}; 				
	/**TODO use calibration map instead*/
	@Deprecated
	public double[][] mSensitivityMatrixGyroscope = {{2.73,0,0},{0,2.73,0},{0,0,2.73}}; 		
	/**TODO use calibration map instead*/
	@Deprecated
	public double[][] mOffsetVectorGyroscope = {{1843},{1843},{1843}};

	//TODO Shimmer2 doesn't belong here
	//Default values Shimmer2
	protected static final double[][] AlignmentMatrixGyroShimmer2 = {{0,-1,0},{-1,0,0},{0,0,-1}}; 				
	protected static final double[][] SensitivityMatrixGyroShimmer2 = {{2.73,0,0},{0,2.73,0},{0,0,2.73}}; 		
	protected static final double[][] OffsetVectorGyroShimmer2 = {{1843},{1843},{1843}};
	//Shimmer3
	public static final double[][] AlignmentMatrixGyroShimmer3 = {{0,-1,0},{-1,0,0},{0,0,-1}}; 				
	public static final double[][] OffsetVectorGyroShimmer3 = {{0},{0},{0}};	
	public static final double[][] SensitivityMatrixGyro250dpsShimmer3 = {{131,0,0},{0,131,0},{0,0,131}};
	public static final double[][] SensitivityMatrixGyro500dpsShimmer3 = {{65.5,0,0},{0,65.5,0},{0,0,65.5}};
	public static final double[][] SensitivityMatrixGyro1000dpsShimmer3 = {{32.8,0,0},{0,32.8,0},{0,0,32.8}};
	public static final double[][] SensitivityMatrixGyro2000dpsShimmer3 = {{16.4,0,0},{0,16.4,0},{0,0,16.4}};
	
	protected TreeMap<Integer, CalibDetailsKinematic> mCalibMapGyroShimmer3 = new TreeMap<Integer, CalibDetailsKinematic>(); 
	{
		//TODO improve the way these are loaded - using array indexes is too hard coded?
		mCalibMapGyroShimmer3.put(Shimmer3.ListofMPU9150GyroRangeConfigValues[0], 
				new CalibDetailsKinematic(Shimmer3.ListofMPU9150GyroRangeConfigValues[0], Shimmer3.ListofGyroRange[0],
						AlignmentMatrixGyroShimmer3, SensitivityMatrixGyro250dpsShimmer3, OffsetVectorGyroShimmer3));
		mCalibMapGyroShimmer3.put(Shimmer3.ListofMPU9150GyroRangeConfigValues[1], 
				new CalibDetailsKinematic(Shimmer3.ListofMPU9150GyroRangeConfigValues[1], Shimmer3.ListofGyroRange[1],
						AlignmentMatrixGyroShimmer3, SensitivityMatrixGyro500dpsShimmer3, OffsetVectorGyroShimmer3));
		mCalibMapGyroShimmer3.put(Shimmer3.ListofMPU9150GyroRangeConfigValues[2], 
				new CalibDetailsKinematic(Shimmer3.ListofMPU9150GyroRangeConfigValues[2], Shimmer3.ListofGyroRange[2],
						AlignmentMatrixGyroShimmer3, SensitivityMatrixGyro1000dpsShimmer3, OffsetVectorGyroShimmer3));
		mCalibMapGyroShimmer3.put(Shimmer3.ListofMPU9150GyroRangeConfigValues[3], 
				new CalibDetailsKinematic(Shimmer3.ListofMPU9150GyroRangeConfigValues[3], Shimmer3.ListofGyroRange[3],
						AlignmentMatrixGyroShimmer3, SensitivityMatrixGyro2000dpsShimmer3, OffsetVectorGyroShimmer3));
	}
	// ----------   Gyro end ---------------

	
	
//	------------ Keep in Configuration? ------------
//	public class SensorMapKey{
////		public static final int MPU9150_ACCEL = 17;
////		public static final int MPU9150_GYRO = 1;
////		public static final int MPU9150_MAG = 18;
//		public static final int SHIMMER_MPU9150_GYRO = 1;
//		public static final int SHIMMER_MPU9150_ACCEL = 17;
//		/** Shimmer3 Alternative magnetometer */
//		public static final int SHIMMER_MPU9150_MAG = 18;
//		public static final int SHIMMER_MPU9150_TEMP = 25;
//		public static final int SHIMMER_MPU9150_MPL_QUAT_6DOF = 27;
//		public static final int SHIMMER_MPU9150_MPL_QUAT_9DOF = 28;
//		public static final int SHIMMER_MPU9150_MPL_EULER_6DOF = 29;
//		public static final int SHIMMER_MPU9150_MPL_EULER_9DOF = 30;
//		public static final int SHIMMER_MPU9150_MPL_HEADING = 31;
//		public static final int SHIMMER_MPU9150_MPL_PEDOMETER = 32;
//		public static final int SHIMMER_MPU9150_MPL_TAP = 33;
//		public static final int SHIMMER_MPU9150_MPL_MOTION_ORIENT = 34;
//		public static final int SHIMMER_MPU9150_MPL_GYRO = 35;
//		public static final int SHIMMER_MPU9150_MPL_ACCEL = 36;
//		public static final int SHIMMER_MPU9150_MPL_MAG = 37;
//		public static final int SHIMMER_MPU9150_MPL_QUAT_6DOF_RAW = 38;
//
//	}
	
	public class GuiLabelConfig{
		public static final String MPU9150_GYRO_RANGE = "Gyro Range";
		public static final String MPU9150_GYRO_RATE = "Gyro Sampling Rate";
		public static final String MPU9150_GYRO_RATE_HZ = "Gyro Sampling Rate Hertz";
		
	    public static final String MPU9150_ACCEL_RANGE = "MPU Accel Range";
		public static final String MPU9150_DMP_GYRO_CAL = "MPU Gyro Cal";
		public static final String MPU9150_MPL_LPF = "MPU LPF";
		public static final String MPU9150_MPL_RATE = "MPL Rate";
		public static final String MPU9150_MAG_RATE = "MPU Mag Rate";

		public static final String MPU9150_DMP = "DMP";
		public static final String MPU9150_MPL = "MPL";
		public static final String MPU9150_MPL_9DOF_SENSOR_FUSION = "9DOF Sensor Fusion";
		public static final String MPU9150_MPL_GYRO_CAL = "Gyro Calibration";
		public static final String MPU9150_MPL_VECTOR_CAL = "Vector Compensation Calibration";
		public static final String MPU9150_MPL_MAG_CAL = "Magnetic Disturbance Calibration";

		public static final String MPU9150_GYRO_LPM = "Gyro Low-Power Mode";
		public static final String MPU9150_GYRO_DEFAULT_CALIB = "Gyro Default Calibration";
	}
	
	public class GuiLabelSensors{
		public static final String GYRO = "Gyroscope";
		public static final String ACCEL_MPU = "Alternative Accel";
		public static final String MAG_MPU = "Alternative Mag";
		
		public static final String GYRO_MPU_MPL = "MPU Gyro";
		public static final String ACCEL_MPU_MPL = "MPU Accel";
		public static final String MAG_MPU_MPL = "MPU Mag";
		
		public static final String QUAT_MPL_6DOF = "MPU Quat 6DOF";
		public static final String QUAT_MPL_9DOF = "MPU Quat 9DOF";
		public static final String EULER_ANGLES_6DOF = "Euler Angles (6DOF)"; 
		public static final String EULER_ANGLES_9DOF = "Euler Angles (9DOF)";
		public static final String EULER_MPL_6DOF = "MPU Euler 6DOF";
		public static final String EULER_MPL_9DOF = "MPU Euler 9DOF";
		public static final String QUAT_DMP_6DOF = "MPU Quat 6DOF (from DMP)";

		public static final String MPL_HEADING = "MPU Heading";
		public static final String MPL_TEMPERATURE = "MPU Temp";
		public static final String MPL_PEDOMETER = "MPL_Pedometer"; 		// not currently supported
		public static final String MPL_PEDOM_CNT = "MPL_Pedom_cnt"; 		// not currently supported
		public static final String MPL_PEDOM_TIME = "MPL_Pedom_Time"; 		// not currently supported
		public static final String MPL_TAPDIRANDTAPCNT = "TapDirAndTapCnt"; // not currently supported
		public static final String MPL_TAPDIR = "TapDir";                   // not currently supported
		public static final String MPL_TAPCNT = "TapCnt"; 					// not currently supported
		public static final String MPL_MOTIONANDORIENT = "MotionAndOrient"; // not currently supported
		public static final String MPL_MOTION = "Motion"; // not currently supported
		public static final String MPL_ORIENT = "Orient"; // not currently supported
	}
	
	public class GuiLabelSensorTiles{
		public static final String MPU = "Kinematics";
		public static final String GYRO = GuiLabelSensors.GYRO;
		public static final String MPU_ACCEL_GYRO_MAG = "MPU 9DoF";
		public static final String MPU_OTHER = "MPU Other";
	}
	
	public static class DatabaseChannelHandles{
		
		public static final String MPU_HEADING = "MPU9150_MPL_Heading"; // not available but supported in FW
		public static final String MPU_TEMP = "MPU9150_Temperature";

		public static final String GYRO_X = "MPU9150_GYRO_X";
		public static final String GYRO_Y = "MPU9150_GYRO_Y";
		public static final String GYRO_Z = "MPU9150_GYRO_Z";

		public static final String ALTERNATIVE_ACC_X = "MPU9150_ACC_X"; // not available but supported in FW
		public static final String ALTERNATIVE_ACC_Y = "MPU9150_ACC_Y"; // not available but supported in FW
		public static final String ALTERNATIVE_ACC_Z = "MPU9150_ACC_Z"; // not available but supported in FW
		public static final String ALTERNATIVE_MAG_X = "MPU9150_MAG_X"; // not available but supported in FW
		public static final String ALTERNATIVE_MAG_Y = "MPU9150_MAG_Y"; // not available but supported in FW
		public static final String ALTERNATIVE_MAG_Z = "MPU9150_MAG_Z"; // not available but supported in FW
		
		public static final String MPU_QUAT_6DOF_W = "MPU9150_MPL_QUAT_6DOF_W";
		public static final String MPU_QUAT_6DOF_X = "MPU9150_MPL_QUAT_6DOF_X";
		public static final String MPU_QUAT_6DOF_Y = "MPU9150_MPL_QUAT_6DOF_Y";
		public static final String MPU_QUAT_6DOF_Z = "MPU9150_MPL_QUAT_6DOF_Z";
		public static final String MPU_QUAT_9DOF_W = "MPU9150_MPL_QUAT_9DOF_W";
		public static final String MPU_QUAT_9DOF_X = "MPU9150_MPL_QUAT_9DOF_X";
		public static final String MPU_QUAT_9DOF_Y = "MPU9150_MPL_QUAT_9DOF_Y";
		public static final String MPU_QUAT_9DOF_Z = "MPU9150_MPL_QUAT_9DOF_Z";
		public static final String MPU_EULER_6DOF_X = "MPU9150_MPL_EULER_6DOF_X"; // not available but supported in FW
		public static final String MPU_EULER_6DOF_Y = "MPU9150_MPL_EULER_6DOF_Y"; // not available but supported in FW
		public static final String MPU_EULER_6DOF_Z = "MPU9150_MPL_EULER_6DOF_Z"; // not available but supported in FW
		public static final String MPU_EULER_9DOF_X = "MPU9150_MPL_EULER_9DOF_X"; // not available but supported in FW
		public static final String MPU_EULER_9DOF_Y = "MPU9150_MPL_EULER_9DOF_Y"; // not available but supported in FW
		public static final String MPU_EULER_9DOF_Z = "MPU9150_MPL_EULER_9DOF_Z"; // not available but supported in FW
//		public static final String MPU_HEADING = "MPU9150_MPL_HEADING"; -> already define for the shimmerCongig Table
//		public static final String MPU_TEMP = "MPU9150_Temperature"; -> already define for the shimmerCongig Table
		public static final String PEDOMETER_CNT = "MPU9150_MPL_PEDOM_CNT"; // not available but supported in FW
		public static final String PEDOMETER_TIME = "MPU9150_MPL_PEDOM_TIME"; // not available but supported in FW
		public static final String TAP_DIR_AND_CNT = "MPU9150_MPL_TAP"; // not available but supported in FW
		public static final String TAP_DIR = "MPU9150_MPL_TAP_DIR"; // not available but supported in FW
		public static final String TAP_CNT = "MPU9150_MPL_TAP_CNT"; // not available but supported in FW
		public static final String MOTION_AND_ORIENT = "MPU9150_MPL_MOTION_AND_ORIENT"; // not available but supported in FW
		public static final String MOTION = "MPU9150_MPL_MOTION"; // not available but supported in FW
		public static final String ORIENT = "MPU9150_MPL_ORIENT"; // not available but supported in FW

		public static final String MPU_MPL_GYRO_X = "MPU9150_MPL_GYRO_X_CAL";
		public static final String MPU_MPL_GYRO_Y = "MPU9150_MPL_GYRO_Y_CAL";
		public static final String MPU_MPL_GYRO_Z = "MPU9150_MPL_GYRO_Z_CAL";
		public static final String MPU_MPL_ACC_X = "MPU9150_MPL_ACC_X_CAL";
		public static final String MPU_MPL_ACC_Y = "MPU9150_MPL_ACC_Y_CAL";
		public static final String MPU_MPL_ACC_Z = "MPU9150_MPL_ACC_Z_CAL";
		public static final String MPU_MPL_MAG_X = "MPU9150_MPL_MAG_X_CAL";
		public static final String MPU_MPL_MAG_Y = "MPU9150_MPL_MAG_Y_CAL";
		public static final String MPU_MPL_MAG_Z = "MPU9150_MPL_MAG_Z_CAL";
		public static final String MPU_QUAT_6DOF_DMP_W = "MPU9150_QUAT_6DOF_W";
		public static final String MPU_QUAT_6DOF_DMP_X = "MPU9150_QUAT_6DOF_X";
		public static final String MPU_QUAT_6DOF_DMP_Y = "MPU9150_QUAT_6DOF_Y";
		public static final String MPU_QUAT_6DOF_DMP_Z = "MPU9150_QUAT_6DOF_Z";
		
	}
	
	public static class ObjectClusterSensorName{
		public static String GYRO_X = "Gyro_X";
		public static String GYRO_Y = "Gyro_Y";
		public static String GYRO_Z = "Gyro_Z";
		public static String ACCEL_MPU_X = "Accel_MPU_X";
		public static String ACCEL_MPU_Y = "Accel_MPU_Y";
		public static String ACCEL_MPU_Z = "Accel_MPU_Z";
		public static String MAG_MPU_X = "Mag_MPU_X";
		public static String MAG_MPU_Y = "Mag_MPU_Y";
		public static String MAG_MPU_Z = "Mag_MPU_Z";
		
		public static String GYRO_MPU_MPL_X = "Gyro_MPU_MPL_X";
		public static String GYRO_MPU_MPL_Y = "Gyro_MPU_MPL_Y";
		public static String GYRO_MPU_MPL_Z = "Gyro_MPU_MPL_Z";
		public static String ACCEL_MPU_MPL_X = "Accel_MPU_MPL_X";
		public static String ACCEL_MPU_MPL_Y = "Accel_MPU_MPL_Y";
		public static String ACCEL_MPU_MPL_Z = "Accel_MPU_MPL_Z";
		public static String MAG_MPU_MPL_X = "Mag_MPU_MPL_X";
		public static String MAG_MPU_MPL_Y = "Mag_MPU_MPL_Y";
		public static String MAG_MPU_MPL_Z = "Mag_MPU_MPL_Z";
		public static String QUAT_DMP_6DOF_W = "Quat_DMP_6DOF_W";
		public static String QUAT_DMP_6DOF_X = "Quat_DMP_6DOF_X";
		public static String QUAT_DMP_6DOF_Y = "Quat_DMP_6DOF_Y";
		public static String QUAT_DMP_6DOF_Z = "Quat_DMP_6DOF_Z";
		
		public static String QUAT_MPL_6DOF_W = "Quat_MPL_6DOF_W";
		public static String QUAT_MPL_6DOF_X = "Quat_MPL_6DOF_X";
		public static String QUAT_MPL_6DOF_Y = "Quat_MPL_6DOF_Y";
		public static String QUAT_MPL_6DOF_Z = "Quat_MPL_6DOF_Z";
		public static String QUAT_MPL_9DOF_W = "Quat_MPL_9DOF_W";
		public static String QUAT_MPL_9DOF_X = "Quat_MPL_9DOF_X";
		public static String QUAT_MPL_9DOF_Y = "Quat_MPL_9DOF_Y";
		public static String QUAT_MPL_9DOF_Z = "Quat_MPL_9DOF_Z";
		
		public static String EULER_MPL_6DOF_X = "Euler_MPL_6DOF_X";
		public static String EULER_MPL_6DOF_Y = "Euler_MPL_6DOF_Y";
		public static String EULER_MPL_6DOF_Z = "Euler_MPL_6DOF_Z";
		public static String EULER_MPL_9DOF_X = "Euler_MPL_9DOF_X";
		public static String EULER_MPL_9DOF_Y = "Euler_MPL_9DOF_Y";
		public static String EULER_MPL_9DOF_Z = "Euler_MPL_9DOF_Z";
		
		public static String MPL_HEADING = "MPL_heading";
		public static String MPL_TEMPERATURE = "MPL_Temperature";
		public static String MPL_PEDOM_CNT = "MPL_Pedom_cnt";
		public static String MPL_PEDOM_TIME = "MPL_Pedom_Time";
		public static String TAPDIRANDTAPCNT = "TapDirAndTapCnt";
		public static String TAPDIR = "Tap_Dirirection";
		public static String TAPCNT = "Tap_Count";
		public static String MOTIONANDORIENT = "MotionAndOrient";
		
		public static String MOTION = "Motion";
		public static String ORIENT = "Orient";
	}
	
	public boolean mEnableOntheFlyGyroOVCal = false;

	public double mGyroXOVCalThreshold = 1.2;
	DescriptiveStatistics mGyroXX;
	DescriptiveStatistics mGyroXY;
	DescriptiveStatistics mGyroXZ;
	DescriptiveStatistics mGyroXXRaw;
	DescriptiveStatistics mGyroXYRaw;
	DescriptiveStatistics mGyroXZRaw;
	public boolean mEnableXCalibration = true;
	public byte[] mInquiryResponseXBytes;
	
	public byte[] mGyroCalRawXParams = new byte[22];
	public byte[] mMagCalRawXParams = new byte[22];
	//--------- Sensor specific variables end --------------


	//--------- Bluetooth commands start --------------
	
	public static final byte SET_GYRO_CALIBRATION_COMMAND 	  		= (byte) 0x14;
	public static final byte GYRO_CALIBRATION_RESPONSE        		= (byte) 0x15;
	public static final byte GET_GYRO_CALIBRATION_COMMAND     		= (byte) 0x16;
	public static final byte SET_GYRO_TEMP_VREF_COMMAND       		= (byte) 0x33;
	public static final byte SET_MPU9150_GYRO_RANGE_COMMAND 		= (byte) 0x49;
	public static final byte MPU9150_GYRO_RANGE_RESPONSE 			= (byte) 0x4A;
	public static final byte GET_MPU9150_GYRO_RANGE_COMMAND 		= (byte) 0x4B;
	public static final byte SET_MPU9150_SAMPLING_RATE_COMMAND 		= (byte) 0x4C;
	public static final byte MPU9150_SAMPLING_RATE_RESPONSE 		= (byte) 0x4D;
	public static final byte GET_MPU9150_SAMPLING_RATE_COMMAND 		= (byte) 0x4E;
	public static final byte MPU9150_MAG_SENS_ADJ_VALS_RESPONSE 	= (byte) 0x5C;
	public static final byte GET_MPU9150_MAG_SENS_ADJ_VALS_COMMAND 	= (byte) 0x5D;
	
	
	 public static final Map<Byte, BtCommandDetails> mBtGetCommandMap;
	    static {
	        Map<Byte, BtCommandDetails> aMap = new LinkedHashMap<Byte, BtCommandDetails>();
	        aMap.put(GET_GYRO_CALIBRATION_COMMAND, new BtCommandDetails(GET_GYRO_CALIBRATION_COMMAND, "GET_GYRO CALIBRATION_COMMAND", GYRO_CALIBRATION_RESPONSE));
	        aMap.put(GET_MPU9150_GYRO_RANGE_COMMAND, new BtCommandDetails(GET_MPU9150_GYRO_RANGE_COMMAND, "GET_MPU9150 GYRO RANGE_COMMAND", MPU9150_GYRO_RANGE_RESPONSE));
	        aMap.put(GET_MPU9150_SAMPLING_RATE_COMMAND, new BtCommandDetails(GET_MPU9150_SAMPLING_RATE_COMMAND, "GET_MPU9150_SAMPLING_RATE_COMMAND", MPU9150_SAMPLING_RATE_RESPONSE));
	        aMap.put(GET_MPU9150_MAG_SENS_ADJ_VALS_COMMAND, new BtCommandDetails(GET_MPU9150_MAG_SENS_ADJ_VALS_COMMAND, "GET_MPU9150_MAG_SENS_ADJ_VALS_COMMAND", MPU9150_MAG_SENS_ADJ_VALS_RESPONSE));

	        mBtGetCommandMap = Collections.unmodifiableMap(aMap);
	    }
	    
	    public static final Map<Byte, BtCommandDetails> mBtSetCommandMap;
	    static {
	        Map<Byte, BtCommandDetails> aMap = new LinkedHashMap<Byte, BtCommandDetails>();
	        aMap.put(SET_GYRO_CALIBRATION_COMMAND, new BtCommandDetails(SET_GYRO_CALIBRATION_COMMAND, "SET_GYRO_CALIBRATION_COMMAND"));
	        aMap.put(SET_MPU9150_GYRO_RANGE_COMMAND, new BtCommandDetails(SET_MPU9150_GYRO_RANGE_COMMAND, "SET_MPU9150_GYRO_RANGE_COMMAND"));
	        aMap.put(SET_MPU9150_SAMPLING_RATE_COMMAND, new BtCommandDetails(SET_MPU9150_SAMPLING_RATE_COMMAND, "SET_MPU9150_SAMPLING_RATE_COMMAND"));
	        aMap.put(SET_GYRO_TEMP_VREF_COMMAND, new BtCommandDetails(SET_GYRO_TEMP_VREF_COMMAND, "SET_GYRO_TEMP_VREF_COMMAND"));
	        mBtSetCommandMap = Collections.unmodifiableMap(aMap);
	    }
	
	
	//--------- Bluetooth commands end --------------
	
	//--------- Configuration options start --------------
	public static final String[] ListofGyroRange = {"+/- 250dps","+/- 0dps","+/- 1000dps","+/- 2000dps"};
	public static final Integer[] ListofMPU9150GyroRangeConfigValues = {0,1,2,3};
	
	public static final String[] ListofMPU9150AccelRange = {"+/- 2g","+/- 4g","+/- 8g","+/- 16g"};
	public static final Integer[] ListofMPU9150AccelRangeConfigValues = {0,1,2,3};
	public static final String[] ListofMPU9150MagRate = {"10.0Hz","20.0Hz","40.0Hz","50.0Hz","100.0Hz"};
	public static final Integer[] ListofMPU9150MagRateConfigValues = {0,1,2,3,4};

	public static final String[] ListofMPU9150MplCalibrationOptions = {"No Cal","Fast Cal","1s no motion","2s no motion","5s no motion","10s no motion","30s no motion","60s no motion"};
	public static final Integer[] ListofMPU9150MplCalibrationOptionsConfigValues = {0,1,2,3,4,5,6,7};
	public static final String[] ListofMPU9150MplLpfOptions = {"No LPF","188.0Hz","98.0Hz","42.0Hz","20.0Hz","10.0Hz","5.0Hz"};
	public static final Integer[] ListofMPU9150MplLpfOptionsConfigValues = {0,1,2,3,4,5,6};
	public static final String[] ListofMPU9150MplRate = {"10.0Hz","20.0Hz","40.0Hz","50.0Hz","100.0Hz"};
	public static final Integer[] ListofMPU9150MplRateConfigValues = {0,1,2,3,4};
	
	public static final List<Integer> mListOfMplChannels = Arrays.asList(
			Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_TEMP,
			Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_MPL_QUAT_6DOF,
			Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_MPL_QUAT_9DOF,
			Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_MPL_EULER_6DOF,
			Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_MPL_EULER_9DOF,
			Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_MPL_HEADING,
			Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_MPL_PEDOMETER,
			Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_MPL_TAP,
			Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_MPL_MOTION_ORIENT,
			Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_MPL_GYRO,
			Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_MPL_ACCEL,
			Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_MPL_MAG,
			Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_MPL_QUAT_6DOF_RAW);

	public static final ConfigOptionDetailsSensor configOptionMpu9150GyroRange = new ConfigOptionDetailsSensor(
			ListofGyroRange, 
			ListofMPU9150GyroRangeConfigValues, 
			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW);
	//MPL Options
	public static final ConfigOptionDetailsSensor configOptionMpu9150AccelRange = new ConfigOptionDetailsSensor(
			ListofMPU9150AccelRange, 
			ListofMPU9150AccelRangeConfigValues, 
			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors);
	
	public static final ConfigOptionDetailsSensor configOptionMpu9150DmpGyroCal = new ConfigOptionDetailsSensor(
			ListofMPU9150MplCalibrationOptions, 
			ListofMPU9150MplCalibrationOptionsConfigValues, 
			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors);
	
	public static final ConfigOptionDetailsSensor configOptionMpu9150MplLpf = new ConfigOptionDetailsSensor(
			ListofMPU9150MplLpfOptions, 
			ListofMPU9150MplLpfOptionsConfigValues, 
			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors);
	
	public static final ConfigOptionDetailsSensor configOptionMpu9150MplRate = new ConfigOptionDetailsSensor(
			ListofMPU9150MplRate, 
			ListofMPU9150MplRateConfigValues, 
			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors);
	
	public static final ConfigOptionDetailsSensor configOptionMpu9150MagRate = new ConfigOptionDetailsSensor(
			ListofMPU9150MagRate, 
			ListofMPU9150MagRateConfigValues, 
			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors);
	//MPL CheckBoxes
	public static final ConfigOptionDetailsSensor configOptionMpu9150Dmp = new ConfigOptionDetailsSensor(
			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.CHECKBOX,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors);
	
	public static final ConfigOptionDetailsSensor configOptionMpu9150Mpl = new ConfigOptionDetailsSensor(
			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.CHECKBOX,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors);
	
	public static final ConfigOptionDetailsSensor configOptionMpu9150Mpl9DofSensorFusion = new ConfigOptionDetailsSensor(
			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.CHECKBOX,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors);

	public static final ConfigOptionDetailsSensor configOptionMpu9150MplGyroCal = new ConfigOptionDetailsSensor(
			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.CHECKBOX,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors);

	public static final ConfigOptionDetailsSensor configOptionMpu9150MplVectorCal = new ConfigOptionDetailsSensor(
			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.CHECKBOX,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors);

	public static final ConfigOptionDetailsSensor configOptionMpu9150MplMagCal = new ConfigOptionDetailsSensor(
			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.CHECKBOX,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors);
	//General Config
	public static final ConfigOptionDetailsSensor configOptionMpu9150GyroRate = new ConfigOptionDetailsSensor(
			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.TEXTFIELD,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW);

	public static final ConfigOptionDetailsSensor configOptionMpu9150GyroLpm = new ConfigOptionDetailsSensor(
			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.CHECKBOX,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW);

	//--------- Configuration options end --------------

	//--------- Sensor info start --------------
	//TODO fill in all conflicting sensors for each sensor listed below -> not all were done in Configuration.Shimmer3
	//TODO should MPU9150_MPL_RATE be in all mListOfConfigOptionKeysAssociated??

	public static final SensorDetailsRef sensorMpu9150GyroRef = new SensorDetailsRef(0x40<<(0*8), 0x40<<(0*8), GuiLabelSensors.GYRO,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW,
			Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_MPL_GYRO),
			Arrays.asList(
					GuiLabelConfig.MPU9150_GYRO_RANGE, 
					GuiLabelConfig.MPU9150_GYRO_RATE),
			Arrays.asList(
					ObjectClusterSensorName.GYRO_X, 
					ObjectClusterSensorName.GYRO_Y, 
					ObjectClusterSensorName.GYRO_Z),
			false);
	
	public static final SensorDetailsRef sensorMpu9150AccelRef = new SensorDetailsRef(0x40<<(2*8), 0x40<<(2*8), GuiLabelSensors.ACCEL_MPU,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoShimmer4,
			Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_MPL_ACCEL), 
			Arrays.asList(GuiLabelConfig.MPU9150_ACCEL_RANGE),
			Arrays.asList(
					ObjectClusterSensorName.ACCEL_MPU_X,
					ObjectClusterSensorName.ACCEL_MPU_Y,
					ObjectClusterSensorName.ACCEL_MPU_Z),
			false);

	public static final SensorDetailsRef sensorMpu9150MagRef = new SensorDetailsRef(0x20<<(2*8), 0x20<<(2*8), GuiLabelSensors.MAG_MPU,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoShimmer4,
			Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_MPL_MAG),
			Arrays.asList(GuiLabelConfig.MPU9150_MAG_RATE),
			Arrays.asList(
					ObjectClusterSensorName.MAG_MPU_X,
					ObjectClusterSensorName.MAG_MPU_Y,
					ObjectClusterSensorName.MAG_MPU_Z),
			false);
	
   // ------------ Check byte index for Temp----------------
//	public static final SensorDetailsRef sensorMpu9150TempRef = new SensorDetailsRef(0x02<<(streamingByteIndex*8), 0x02<<(logHeaderByteIndex*8), Shimmer3.GuiLabelSensors.MPL_TEMPERATURE));
	public static final SensorDetailsRef sensorMpu9150TempRef = new SensorDetailsRef(0x02<<(2*8), 0x02<<(2*8), GuiLabelSensors.MPL_TEMPERATURE,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors,
			Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_TEMP), 
			Arrays.asList(GuiLabelConfig.MPU9150_MPL_RATE),
			Arrays.asList(
					ObjectClusterSensorName.MPL_TEMPERATURE),
			false);

	
	public static final SensorDetailsRef sensorMpu9150MplQuat6Dof = new SensorDetailsRef((long)0, (long)0x80<<(3*8), GuiLabelSensors.QUAT_MPL_6DOF,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors,
			null,//Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_MPL_QUAT_6DOF),
			Arrays.asList(GuiLabelConfig.MPU9150_MPL_RATE),
			Arrays.asList(
					ObjectClusterSensorName.QUAT_MPL_6DOF_W,
					ObjectClusterSensorName.QUAT_MPL_6DOF_X,
					ObjectClusterSensorName.QUAT_MPL_6DOF_Y,
					ObjectClusterSensorName.QUAT_MPL_6DOF_Z),
			false);
	
	
	public static final SensorDetailsRef sensorMpu9150MplQuat9Dof = new SensorDetailsRef((long)0, (long)0x40<<(3*8), GuiLabelSensors.QUAT_MPL_9DOF,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors,
			null,//Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_MPL_QUAT_9DOF),
			Arrays.asList(GuiLabelConfig.MPU9150_MPL_RATE),
			Arrays.asList(
					ObjectClusterSensorName.QUAT_MPL_9DOF_W,
					ObjectClusterSensorName.QUAT_MPL_9DOF_X,
					ObjectClusterSensorName.QUAT_MPL_9DOF_Y,
					ObjectClusterSensorName.QUAT_MPL_9DOF_Z),
			false);
	
	public static final SensorDetailsRef sensorMpu9150MplEuler6Dof = new SensorDetailsRef((long)0, (long)0x20<<(3*8), GuiLabelSensors.EULER_ANGLES_6DOF,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors,
			null,//Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_MPL_EULER_6DOF),
			Arrays.asList(GuiLabelConfig.MPU9150_MPL_RATE),
			Arrays.asList(
					ObjectClusterSensorName.EULER_MPL_6DOF_X,
					ObjectClusterSensorName.EULER_MPL_6DOF_Y,
					ObjectClusterSensorName.EULER_MPL_6DOF_Z),
			false);
	
	public static final SensorDetailsRef sensorMpu9150MplEuler9Dof = new SensorDetailsRef((long)0, (long)0x10<<(3*8), GuiLabelSensors.EULER_ANGLES_9DOF,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors,
			null,//Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_MPL_EULER_9DOF),
			Arrays.asList(GuiLabelConfig.MPU9150_MPL_RATE),
			Arrays.asList(
					ObjectClusterSensorName.EULER_MPL_9DOF_X,
					ObjectClusterSensorName.EULER_MPL_9DOF_Y,
					ObjectClusterSensorName.EULER_MPL_9DOF_Z),
			false);
	
	public static final SensorDetailsRef sensorMpu9150MplHeading = new SensorDetailsRef((long)0, (long)0x08<<(3*8), GuiLabelSensors.MPL_HEADING,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors,
			null,
			Arrays.asList(GuiLabelConfig.MPU9150_MPL_RATE),
			Arrays.asList(
					ObjectClusterSensorName.MPL_HEADING),
			false);
	
	public static final SensorDetailsRef sensorMpu9150MplPedometer = new SensorDetailsRef((long)0, (long)0x04<<(3*8), GuiLabelSensors.MPL_PEDOM_CNT,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors,
			null,//Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_MPL_PEDOMETER),
			Arrays.asList(GuiLabelConfig.MPU9150_MPL_RATE),
			Arrays.asList(
					ObjectClusterSensorName.MPL_PEDOM_CNT,
					ObjectClusterSensorName.MPL_PEDOM_TIME),
			false);
	
	public static final SensorDetailsRef sensorMpu9150MplTap = new SensorDetailsRef((long)0, (long)0x02<<(3*8), GuiLabelSensors.MPL_TAPDIRANDTAPCNT,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors,
			null,//Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_MPL_TAP),
			Arrays.asList(GuiLabelConfig.MPU9150_MPL_RATE),
			Arrays.asList(
					ObjectClusterSensorName.TAPDIR,
					ObjectClusterSensorName.TAPCNT),
			false);
	
	public static final SensorDetailsRef sensorMpu9150MplMotion = new SensorDetailsRef((long)0, (long)0x01<<(3*8), GuiLabelSensors.MPL_MOTIONANDORIENT,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors,
			null,//Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_MPL_MOTION_ORIENT),
			Arrays.asList(GuiLabelConfig.MPU9150_MPL_RATE),
			Arrays.asList(
					ObjectClusterSensorName.MOTION,
					ObjectClusterSensorName.ORIENT),
			false);

	//MPL calibrated sensors
	public static final SensorDetailsRef sensorMpu9150MplGyro = new SensorDetailsRef((long)0, (long)0x80<<(4*8), GuiLabelSensors.GYRO_MPU_MPL,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors,
			Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_GYRO),
			Arrays.asList(
					GuiLabelConfig.MPU9150_GYRO_RANGE,
					GuiLabelConfig.MPU9150_MPL_LPF,
					GuiLabelConfig.MPU9150_GYRO_RATE,
					GuiLabelConfig.MPU9150_MPL_GYRO_CAL),
			Arrays.asList(
					ObjectClusterSensorName.GYRO_MPU_MPL_X,
					ObjectClusterSensorName.GYRO_MPU_MPL_Y,
					ObjectClusterSensorName.GYRO_MPU_MPL_Z),
			false);

	public static final SensorDetailsRef sensorMpu9150MplAccel = new SensorDetailsRef((long)0, (long)0x40<<(4*8), GuiLabelSensors.ACCEL_MPU_MPL,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors,
			Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_ACCEL),
			Arrays.asList(
					GuiLabelConfig.MPU9150_ACCEL_RANGE,
					GuiLabelConfig.MPU9150_MPL_LPF),
			Arrays.asList(
					ObjectClusterSensorName.ACCEL_MPU_MPL_X,
					ObjectClusterSensorName.ACCEL_MPU_MPL_Y,
					ObjectClusterSensorName.ACCEL_MPU_MPL_Z),
			false);
	
	public static final SensorDetailsRef sensorMpu9150MplMag = new SensorDetailsRef((long)0, (long)0x20<<(4*8), GuiLabelSensors.MAG_MPU_MPL,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors,
			Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_MAG),
			Arrays.asList(
					GuiLabelConfig.MPU9150_MPL_LPF),
			Arrays.asList(
					ObjectClusterSensorName.MAG_MPU_MPL_X,
					ObjectClusterSensorName.MAG_MPU_MPL_Y,
					ObjectClusterSensorName.MAG_MPU_MPL_Z),
			false);
	
	public static final SensorDetailsRef sensorMpu9150MplQuat6DofRaw = new SensorDetailsRef((long)0, (long)0x10<<(4*8), GuiLabelSensors.QUAT_DMP_6DOF,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors,
			null,//Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_MPL_QUAT_6DOF_RAW),
			Arrays.asList(GuiLabelConfig.MPU9150_MPL_RATE),
			Arrays.asList(
					ObjectClusterSensorName.QUAT_DMP_6DOF_W,
					ObjectClusterSensorName.QUAT_DMP_6DOF_X,
					ObjectClusterSensorName.QUAT_DMP_6DOF_Y,
					ObjectClusterSensorName.QUAT_DMP_6DOF_Z),
			false);

	
    public static final Map<Integer, SensorDetailsRef> mSensorMapRef;
    static {
        Map<Integer, SensorDetailsRef> aMap = new LinkedHashMap<Integer, SensorDetailsRef>();
		aMap.put(Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_GYRO, SensorMPU9X50.sensorMpu9150GyroRef);
		aMap.put(Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_ACCEL, SensorMPU9X50.sensorMpu9150AccelRef);
		aMap.put(Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_MAG,SensorMPU9X50.sensorMpu9150MagRef);

		//TODO decide what to do with below -> update, I can't remember why I added this message
		aMap.put(Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_TEMP, SensorMPU9X50.sensorMpu9150TempRef);

		aMap.put(Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_MPL_QUAT_6DOF, SensorMPU9X50.sensorMpu9150MplQuat6Dof);
		aMap.put(Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_MPL_QUAT_9DOF, SensorMPU9X50.sensorMpu9150MplQuat9Dof);
		aMap.put(Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_MPL_EULER_6DOF, SensorMPU9X50.sensorMpu9150MplEuler6Dof);
		aMap.put(Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_MPL_EULER_9DOF, SensorMPU9X50.sensorMpu9150MplEuler9Dof);
		aMap.put(Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_MPL_HEADING, SensorMPU9X50.sensorMpu9150MplHeading);
		aMap.put(Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_MPL_PEDOMETER, SensorMPU9X50.sensorMpu9150MplPedometer);
		aMap.put(Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_MPL_TAP, SensorMPU9X50.sensorMpu9150MplTap);
		aMap.put(Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_MPL_MOTION_ORIENT, SensorMPU9X50.sensorMpu9150MplMotion);
		aMap.put(Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_MPL_GYRO, SensorMPU9X50.sensorMpu9150MplGyro);
		aMap.put(Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_MPL_ACCEL, SensorMPU9X50.sensorMpu9150MplAccel);
		aMap.put(Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_MPL_MAG, SensorMPU9X50.sensorMpu9150MplMag);
		aMap.put(Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_MPL_QUAT_6DOF_RAW, SensorMPU9X50.sensorMpu9150MplQuat6DofRaw);

		mSensorMapRef = Collections.unmodifiableMap(aMap);
    }
	//--------- Sensor info end --------------
    
	//--------- Channel info start --------------
	// MPU9150 Gyro
    public static final ChannelDetails channelGyroX = new ChannelDetails(
    		ObjectClusterSensorName.GYRO_X,
    		ObjectClusterSensorName.GYRO_X,
    		DatabaseChannelHandles.GYRO_X,
    		CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.MSB,
    		CHANNEL_UNITS.DEGREES_PER_SECOND,
    		Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
    public static final ChannelDetails channelGyroY = new ChannelDetails(
    		ObjectClusterSensorName.GYRO_Y,
    		ObjectClusterSensorName.GYRO_Y,
    		DatabaseChannelHandles.GYRO_Y,
    		CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.MSB,
    		CHANNEL_UNITS.DEGREES_PER_SECOND,
    		Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
    public static final ChannelDetails channelGyroZ = new ChannelDetails(
    		ObjectClusterSensorName.GYRO_Z,
    		ObjectClusterSensorName.GYRO_Z,
    		DatabaseChannelHandles.GYRO_Z,
    		CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.MSB,
    		CHANNEL_UNITS.DEGREES_PER_SECOND,
    		Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
    
    // MPU Accel
	public static final ChannelDetails channelAccelX = new ChannelDetails(
			ObjectClusterSensorName.ACCEL_MPU_X,
			ObjectClusterSensorName.ACCEL_MPU_X,
			DatabaseChannelHandles.ALTERNATIVE_ACC_X,
			CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.MSB,
			CHANNEL_UNITS.METER_PER_SECOND_SQUARE,
			// no CAL channel currently as calibration parameters are not stored anywhere
			Arrays.asList(CHANNEL_TYPE.UNCAL));
	public static final ChannelDetails channelAccelY = new ChannelDetails(
			ObjectClusterSensorName.ACCEL_MPU_Y,
			ObjectClusterSensorName.ACCEL_MPU_Y,
			DatabaseChannelHandles.ALTERNATIVE_ACC_Y,
			CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.MSB,
			CHANNEL_UNITS.METER_PER_SECOND_SQUARE,
			// no CAL channel currently as calibration parameters are not stored anywhere
			Arrays.asList(CHANNEL_TYPE.UNCAL));	
	public static final ChannelDetails channelAccelZ = new ChannelDetails(
			ObjectClusterSensorName.ACCEL_MPU_Z,
			ObjectClusterSensorName.ACCEL_MPU_Z,
			DatabaseChannelHandles.ALTERNATIVE_ACC_Z,
			CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.MSB,
			CHANNEL_UNITS.METER_PER_SECOND_SQUARE,
			// no CAL channel currently as calibration parameters are not stored anywhere
			Arrays.asList(CHANNEL_TYPE.UNCAL));
	
	//MPU MAG
	//Mag is actually 13-bit, signed and LSB
	//refer to https://github.com/kriswiner/MPU-9150/blob/master/MPU9150BasicAHRS.ino for calibration
	public static final ChannelDetails channelMagX = new ChannelDetails(
			ObjectClusterSensorName.MAG_MPU_X,
			ObjectClusterSensorName.MAG_MPU_X,
			DatabaseChannelHandles.ALTERNATIVE_MAG_X,
			CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.LSB,
			CHANNEL_UNITS.U_TESLA,
    		Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
	public static final ChannelDetails channelMagY = new ChannelDetails(
			ObjectClusterSensorName.MAG_MPU_Y,
			ObjectClusterSensorName.MAG_MPU_Y,
			DatabaseChannelHandles.ALTERNATIVE_MAG_Y,
			CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.LSB,
			CHANNEL_UNITS.U_TESLA,
    		Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
	public static final ChannelDetails channelMagZ = new ChannelDetails(
			ObjectClusterSensorName.MAG_MPU_Z,
			ObjectClusterSensorName.MAG_MPU_Z,
			DatabaseChannelHandles.ALTERNATIVE_MAG_Z,
			CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.LSB,
			CHANNEL_UNITS.U_TESLA,
    		Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));

	// MPL Quaternions 6DOF
	public static final ChannelDetails channelQuatMpl6DofW = new ChannelDetails(
					ObjectClusterSensorName.QUAT_MPL_6DOF_W,
					ObjectClusterSensorName.QUAT_MPL_6DOF_W,
					DatabaseChannelHandles.MPU_QUAT_6DOF_W,
					CHANNEL_DATA_TYPE.INT32, 4, CHANNEL_DATA_ENDIAN.MSB,
					CHANNEL_UNITS.NO_UNITS,
					Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
	public static final ChannelDetails channelQuatMpl6DofX = new ChannelDetails(
					ObjectClusterSensorName.QUAT_MPL_6DOF_X,
					ObjectClusterSensorName.QUAT_MPL_6DOF_X,
					DatabaseChannelHandles.MPU_QUAT_6DOF_X,
					CHANNEL_DATA_TYPE.INT32, 4, CHANNEL_DATA_ENDIAN.MSB,
					CHANNEL_UNITS.NO_UNITS,
					Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
	public static final ChannelDetails channelQuatMpl6DofY = new ChannelDetails(
					ObjectClusterSensorName.QUAT_MPL_6DOF_Y,
					ObjectClusterSensorName.QUAT_MPL_6DOF_Y,
					DatabaseChannelHandles.MPU_QUAT_6DOF_Y,
					CHANNEL_DATA_TYPE.INT32, 4, CHANNEL_DATA_ENDIAN.MSB,
					CHANNEL_UNITS.NO_UNITS,
					Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
	public static final ChannelDetails channelQuatMpl6DofZ = new ChannelDetails(
					ObjectClusterSensorName.QUAT_MPL_6DOF_Z,
					ObjectClusterSensorName.QUAT_MPL_6DOF_Z,
					DatabaseChannelHandles.MPU_QUAT_6DOF_Z,
					CHANNEL_DATA_TYPE.INT32, 4, CHANNEL_DATA_ENDIAN.MSB,
					CHANNEL_UNITS.NO_UNITS,
					Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));

	// MPL Quaternions 9DOF
	public static final ChannelDetails channelQuatMpl9DofW = new ChannelDetails(
					ObjectClusterSensorName.QUAT_MPL_9DOF_W,
					ObjectClusterSensorName.QUAT_MPL_9DOF_W,
					DatabaseChannelHandles.MPU_QUAT_9DOF_W,
					CHANNEL_DATA_TYPE.INT32, 4, CHANNEL_DATA_ENDIAN.MSB,
					CHANNEL_UNITS.NO_UNITS,
					Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
	public static final ChannelDetails channelQuatMpl9DofX = new ChannelDetails(
					ObjectClusterSensorName.QUAT_MPL_9DOF_X,
					ObjectClusterSensorName.QUAT_MPL_9DOF_X,
					DatabaseChannelHandles.MPU_QUAT_9DOF_X,
					CHANNEL_DATA_TYPE.INT32, 4, CHANNEL_DATA_ENDIAN.MSB,
					CHANNEL_UNITS.NO_UNITS,
					Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
	public static final ChannelDetails channelQuatMpl9DofY = new ChannelDetails(
					ObjectClusterSensorName.QUAT_MPL_9DOF_Y,
					ObjectClusterSensorName.QUAT_MPL_9DOF_Y,
					DatabaseChannelHandles.MPU_QUAT_9DOF_Y,
					CHANNEL_DATA_TYPE.INT32, 4, CHANNEL_DATA_ENDIAN.MSB,
					CHANNEL_UNITS.NO_UNITS,
					Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
	public static final ChannelDetails channelQuatMpl9DofZ = new ChannelDetails(
					ObjectClusterSensorName.QUAT_MPL_9DOF_Z,
					ObjectClusterSensorName.QUAT_MPL_9DOF_Z,
					DatabaseChannelHandles.MPU_QUAT_9DOF_Z,
					CHANNEL_DATA_TYPE.INT32, 4, CHANNEL_DATA_ENDIAN.MSB,
					CHANNEL_UNITS.NO_UNITS,
					Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
	
	// MPL Euler
	public static final ChannelDetails channelEulerMpl6DofX = new ChannelDetails(
					ObjectClusterSensorName.EULER_MPL_6DOF_X,
					ObjectClusterSensorName.EULER_MPL_6DOF_X,
					DatabaseChannelHandles.MPU_EULER_6DOF_X,
					CHANNEL_DATA_TYPE.INT32, 4, CHANNEL_DATA_ENDIAN.MSB,
					CHANNEL_UNITS.NO_UNITS,
					Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
	public static final ChannelDetails channelEulerMpl6DofY = new ChannelDetails(
					ObjectClusterSensorName.EULER_MPL_6DOF_Y,
					ObjectClusterSensorName.EULER_MPL_6DOF_Y,
					DatabaseChannelHandles.MPU_EULER_6DOF_Y,
					CHANNEL_DATA_TYPE.INT32, 4, CHANNEL_DATA_ENDIAN.MSB,
					CHANNEL_UNITS.NO_UNITS,
					Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
	public static final ChannelDetails channelEulerMpl6DofZ = new ChannelDetails(
					ObjectClusterSensorName.EULER_MPL_6DOF_Z,
					ObjectClusterSensorName.EULER_MPL_6DOF_Z,
					DatabaseChannelHandles.MPU_EULER_6DOF_Z,
					CHANNEL_DATA_TYPE.INT32, 4, CHANNEL_DATA_ENDIAN.MSB,
					CHANNEL_UNITS.NO_UNITS,
					Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));

	public static final ChannelDetails channelEulerMpl9DofX = new ChannelDetails(
					ObjectClusterSensorName.EULER_MPL_9DOF_X,
					ObjectClusterSensorName.EULER_MPL_9DOF_X,
					DatabaseChannelHandles.MPU_EULER_9DOF_X,
					CHANNEL_DATA_TYPE.INT32, 4, CHANNEL_DATA_ENDIAN.MSB,
					CHANNEL_UNITS.NO_UNITS,
					Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
	public static final ChannelDetails channelEulerMpl9DofY = new ChannelDetails(
					ObjectClusterSensorName.EULER_MPL_9DOF_Y,
					ObjectClusterSensorName.EULER_MPL_9DOF_Y,
					DatabaseChannelHandles.MPU_EULER_9DOF_Y,
					CHANNEL_DATA_TYPE.INT32, 4, CHANNEL_DATA_ENDIAN.MSB,
					CHANNEL_UNITS.NO_UNITS,
					Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
	public static final ChannelDetails channelEulerMpl9DofZ = new ChannelDetails(
					ObjectClusterSensorName.EULER_MPL_9DOF_Z,
					ObjectClusterSensorName.EULER_MPL_9DOF_Z,
					DatabaseChannelHandles.MPU_EULER_9DOF_Z,
					CHANNEL_DATA_TYPE.INT32, 4, CHANNEL_DATA_ENDIAN.MSB,
					CHANNEL_UNITS.NO_UNITS,
					Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));

	// MPL Heading
	public static final ChannelDetails channelMplHeading = new ChannelDetails(
					ObjectClusterSensorName.MPL_HEADING,
					ObjectClusterSensorName.MPL_HEADING,
					DatabaseChannelHandles.MPU_HEADING,
					CHANNEL_DATA_TYPE.INT32, 4, CHANNEL_DATA_ENDIAN.MSB,
					CHANNEL_UNITS.DEGREES,
					Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));

	// MPU9150 Temperature
	public static final ChannelDetails channelMplTemperature = new ChannelDetails(
					ObjectClusterSensorName.MPL_TEMPERATURE,
					ObjectClusterSensorName.MPL_TEMPERATURE,
					DatabaseChannelHandles.MPU_TEMP,
					CHANNEL_DATA_TYPE.INT32, 4, CHANNEL_DATA_ENDIAN.MSB,
					CHANNEL_UNITS.DEGREES_CELSUIS,
					Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
	
	// MPL Pedometer
	public static final ChannelDetails channelMplPedomCount = new ChannelDetails(
					ObjectClusterSensorName.MPL_PEDOM_CNT,
					ObjectClusterSensorName.MPL_PEDOM_CNT,
					DatabaseChannelHandles.PEDOMETER_CNT,
					CHANNEL_DATA_TYPE.UINT32, 4, CHANNEL_DATA_ENDIAN.MSB,
					CHANNEL_UNITS.NO_UNITS,
					Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
	public static final ChannelDetails channelMplPedomTime = new ChannelDetails(
					ObjectClusterSensorName.MPL_PEDOM_TIME,
					ObjectClusterSensorName.MPL_PEDOM_TIME,
					DatabaseChannelHandles.PEDOMETER_TIME,
					CHANNEL_DATA_TYPE.UINT32, 4, CHANNEL_DATA_ENDIAN.MSB,
					CHANNEL_UNITS.NO_UNITS,
					Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));

//	// MPL Tap
//	public static final ChannelDetails channelMplTapDirAndTapCnt = new ChannelDetails(
//					ObjectClusterSensorName.TAPDIRANDTAPCNT,
//					ObjectClusterSensorName.TAPDIRANDTAPCNT,
//					DatabaseChannelHandles.TAP_DIR_AND_CNT,
//					CHANNEL_DATA_TYPE.UINT8, 1, CHANNEL_DATA_ENDIAN.MSB,
//					CHANNEL_UNITS.NO_UNITS,
//					Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
	// MPL Tap Direction
	public static final ChannelDetails channelMplTapDir = new ChannelDetails(
			ObjectClusterSensorName.TAPDIR,
			ObjectClusterSensorName.TAPDIR,
			DatabaseChannelHandles.TAP_DIR,
			CHANNEL_DATA_TYPE.UINT8, 1, CHANNEL_DATA_ENDIAN.MSB,
			CHANNEL_UNITS.NO_UNITS,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));

	//MPL Tap Count
	public static final ChannelDetails channelMplTapCnt = new ChannelDetails(
			ObjectClusterSensorName.TAPCNT,
			ObjectClusterSensorName.TAPCNT,
			DatabaseChannelHandles.TAP_CNT,
			CHANNEL_DATA_TYPE.UINT8, 1, CHANNEL_DATA_ENDIAN.MSB,
			CHANNEL_UNITS.NO_UNITS,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));

	// MPL Motion Orient
	public static final ChannelDetails channelMplMotionAndOrient = new ChannelDetails(
			ObjectClusterSensorName.MOTIONANDORIENT,
			ObjectClusterSensorName.MOTIONANDORIENT,
			DatabaseChannelHandles.MOTION_AND_ORIENT,
			CHANNEL_DATA_TYPE.UINT8, 1, CHANNEL_DATA_ENDIAN.MSB,
			CHANNEL_UNITS.NO_UNITS,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
	// MPL Motion 
	public static final ChannelDetails channelMplMotion = new ChannelDetails(
			ObjectClusterSensorName.MOTION,
			ObjectClusterSensorName.MOTION,
			DatabaseChannelHandles.MOTION,
			CHANNEL_DATA_TYPE.UINT8, 1, CHANNEL_DATA_ENDIAN.MSB,
			CHANNEL_UNITS.NO_UNITS,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
	// MPL Orient
	public static final ChannelDetails channelMplOrient = new ChannelDetails(
			ObjectClusterSensorName.ORIENT,
			ObjectClusterSensorName.ORIENT,
			DatabaseChannelHandles.ORIENT,
			CHANNEL_DATA_TYPE.UINT8, 1, CHANNEL_DATA_ENDIAN.MSB,
			CHANNEL_UNITS.NO_UNITS,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
	
	// MPL Gyro Calibrated
	public static final ChannelDetails channelGyroMpuMplX = new ChannelDetails(
			ObjectClusterSensorName.GYRO_MPU_MPL_X,
			ObjectClusterSensorName.GYRO_MPU_MPL_X,
			DatabaseChannelHandles.MPU_MPL_GYRO_X,
			CHANNEL_DATA_TYPE.INT32, 4, CHANNEL_DATA_ENDIAN.MSB,
			CHANNEL_UNITS.DEGREES_PER_SECOND,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
	public static final ChannelDetails channelGyroMpuMplY = new ChannelDetails(
			ObjectClusterSensorName.GYRO_MPU_MPL_Y,
			ObjectClusterSensorName.GYRO_MPU_MPL_Y,
			DatabaseChannelHandles.MPU_MPL_GYRO_Y,
			CHANNEL_DATA_TYPE.INT32, 4, CHANNEL_DATA_ENDIAN.MSB,
			CHANNEL_UNITS.DEGREES_PER_SECOND,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
	public static final ChannelDetails channelGyroMpuMplZ = new ChannelDetails(
			ObjectClusterSensorName.GYRO_MPU_MPL_Z,
			ObjectClusterSensorName.GYRO_MPU_MPL_Z,
			DatabaseChannelHandles.MPU_MPL_GYRO_Z,
			CHANNEL_DATA_TYPE.INT32, 4, CHANNEL_DATA_ENDIAN.MSB,
			CHANNEL_UNITS.DEGREES_PER_SECOND,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));

	// MPL MPU Accelerometer Calibrated
	public static final ChannelDetails channelAccelMpuMplX = new ChannelDetails(
			ObjectClusterSensorName.ACCEL_MPU_MPL_X,
			ObjectClusterSensorName.ACCEL_MPU_MPL_X,
			DatabaseChannelHandles.MPU_MPL_ACC_X,
			CHANNEL_DATA_TYPE.INT32, 4, CHANNEL_DATA_ENDIAN.MSB,
			CHANNEL_UNITS.GRAVITY,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
	public static final ChannelDetails channelAccelMpuMplY = new ChannelDetails(
			ObjectClusterSensorName.ACCEL_MPU_MPL_Y,
			ObjectClusterSensorName.ACCEL_MPU_MPL_Y,
			DatabaseChannelHandles.MPU_MPL_ACC_Y,
			CHANNEL_DATA_TYPE.INT32, 4, CHANNEL_DATA_ENDIAN.MSB,
			CHANNEL_UNITS.GRAVITY,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
	public static final ChannelDetails channelAccelMpuMplZ = new ChannelDetails(
			ObjectClusterSensorName.ACCEL_MPU_MPL_Z,
			ObjectClusterSensorName.ACCEL_MPU_MPL_Z,
			DatabaseChannelHandles.MPU_MPL_ACC_Z,
			CHANNEL_DATA_TYPE.INT32, 4, CHANNEL_DATA_ENDIAN.MSB,
			CHANNEL_UNITS.GRAVITY,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));

	// MPL Magnetometer Calibrated
	public static final ChannelDetails channelMagMpuMplX = new ChannelDetails(
			ObjectClusterSensorName.MAG_MPU_MPL_X,
			ObjectClusterSensorName.MAG_MPU_MPL_X,
			DatabaseChannelHandles.MPU_MPL_MAG_X,
			CHANNEL_DATA_TYPE.INT32, 4, CHANNEL_DATA_ENDIAN.MSB,
			CHANNEL_UNITS.U_TESLA,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
	public static final ChannelDetails channelMagMpuMplY = new ChannelDetails(
			ObjectClusterSensorName.MAG_MPU_MPL_Y,
			ObjectClusterSensorName.MAG_MPU_MPL_Y,
			DatabaseChannelHandles.MPU_MPL_MAG_Y,
			CHANNEL_DATA_TYPE.INT32, 4, CHANNEL_DATA_ENDIAN.MSB,
			CHANNEL_UNITS.U_TESLA,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
	public static final ChannelDetails channelMagMpuMplZ = new ChannelDetails(
			ObjectClusterSensorName.MAG_MPU_MPL_Z,
			ObjectClusterSensorName.MAG_MPU_MPL_Z,
			DatabaseChannelHandles.MPU_MPL_MAG_Z,
			CHANNEL_DATA_TYPE.INT32, 4, CHANNEL_DATA_ENDIAN.MSB,
			CHANNEL_UNITS.U_TESLA,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));

	// Raw 6DOF Quaterian's from the DMP hardware module of the MPU9150
	public static final ChannelDetails channelQuatDmp6DofW = new ChannelDetails(
			ObjectClusterSensorName.QUAT_DMP_6DOF_W,
			ObjectClusterSensorName.QUAT_DMP_6DOF_W,
			DatabaseChannelHandles.MPU_QUAT_6DOF_DMP_W,
			CHANNEL_DATA_TYPE.INT32, 4, CHANNEL_DATA_ENDIAN.MSB,
			CHANNEL_UNITS.NO_UNITS,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
	public static final ChannelDetails channelQuatDmp6DofX = new ChannelDetails(
			ObjectClusterSensorName.QUAT_DMP_6DOF_X,
			ObjectClusterSensorName.QUAT_DMP_6DOF_X,
			DatabaseChannelHandles.MPU_QUAT_6DOF_DMP_X,
			CHANNEL_DATA_TYPE.INT32, 4, CHANNEL_DATA_ENDIAN.MSB,
			CHANNEL_UNITS.NO_UNITS,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
	public static final ChannelDetails channelQuatDmp6DofY = new ChannelDetails(
			ObjectClusterSensorName.QUAT_DMP_6DOF_Y,
			ObjectClusterSensorName.QUAT_DMP_6DOF_Y,
			DatabaseChannelHandles.MPU_QUAT_6DOF_DMP_Y,
			CHANNEL_DATA_TYPE.INT32, 4, CHANNEL_DATA_ENDIAN.MSB,
			CHANNEL_UNITS.NO_UNITS,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
	public static final ChannelDetails channelQuatDmp6DofZ = new ChannelDetails(
			ObjectClusterSensorName.QUAT_DMP_6DOF_Z,
			ObjectClusterSensorName.QUAT_DMP_6DOF_Z,
			DatabaseChannelHandles.MPU_QUAT_6DOF_DMP_Z,
			CHANNEL_DATA_TYPE.INT32, 4, CHANNEL_DATA_ENDIAN.MSB,
			CHANNEL_UNITS.NO_UNITS,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
	
    public static final Map<String, ChannelDetails> mChannelMapRef;
    static {
        Map<String, ChannelDetails> aMap = new LinkedHashMap<String, ChannelDetails>();
		// MPU9150 Gyro
		aMap.put(ObjectClusterSensorName.GYRO_X, SensorMPU9X50.channelGyroX);
		aMap.put(ObjectClusterSensorName.GYRO_Y, SensorMPU9X50.channelGyroY);
		aMap.put(ObjectClusterSensorName.GYRO_Z, SensorMPU9X50.channelGyroZ);
		// MPU9150 Accel
		aMap.put(ObjectClusterSensorName.ACCEL_MPU_X, SensorMPU9X50.channelAccelX);
		aMap.put(ObjectClusterSensorName.ACCEL_MPU_Y, SensorMPU9X50.channelAccelY);
		aMap.put(ObjectClusterSensorName.ACCEL_MPU_Z, SensorMPU9X50.channelAccelZ);
		// MPU9150 Mag
		aMap.put(ObjectClusterSensorName.MAG_MPU_X, SensorMPU9X50.channelMagX);
		aMap.put(ObjectClusterSensorName.MAG_MPU_Y, SensorMPU9X50.channelMagY);
		aMap.put(ObjectClusterSensorName.MAG_MPU_Z, SensorMPU9X50.channelMagZ);
		
		// MPL Gyro Calibrated
		aMap.put(ObjectClusterSensorName.GYRO_MPU_MPL_X, SensorMPU9X50.channelGyroMpuMplX);
		aMap.put(ObjectClusterSensorName.GYRO_MPU_MPL_Y, SensorMPU9X50.channelGyroMpuMplY);
		aMap.put(ObjectClusterSensorName.GYRO_MPU_MPL_Z, SensorMPU9X50.channelGyroMpuMplZ);

		// MPL Accelerometer Calibrated
		aMap.put(ObjectClusterSensorName.ACCEL_MPU_MPL_X, SensorMPU9X50.channelAccelMpuMplX);
		aMap.put(ObjectClusterSensorName.ACCEL_MPU_MPL_Y, SensorMPU9X50.channelAccelMpuMplY);
		aMap.put(ObjectClusterSensorName.ACCEL_MPU_MPL_Z, SensorMPU9X50.channelAccelMpuMplZ);
				
		// MPL Magnetometer Calibrated
		aMap.put(ObjectClusterSensorName.MAG_MPU_MPL_X, SensorMPU9X50.channelMagMpuMplX);
		aMap.put(ObjectClusterSensorName.MAG_MPU_MPL_Y, SensorMPU9X50.channelMagMpuMplY);
		aMap.put(ObjectClusterSensorName.MAG_MPU_MPL_Z, SensorMPU9X50.channelMagMpuMplZ);
		

		// MPL Quaternions 6DOF
		aMap.put(ObjectClusterSensorName.QUAT_MPL_6DOF_W, SensorMPU9X50.channelQuatMpl6DofW);
		aMap.put(ObjectClusterSensorName.QUAT_MPL_6DOF_X, SensorMPU9X50.channelQuatMpl6DofX);
		aMap.put(ObjectClusterSensorName.QUAT_MPL_6DOF_Y, SensorMPU9X50.channelQuatMpl6DofY);
		aMap.put(ObjectClusterSensorName.QUAT_MPL_6DOF_Z, SensorMPU9X50.channelQuatMpl6DofZ);

		// MPL Quaternions 9DOF
		aMap.put(ObjectClusterSensorName.QUAT_MPL_9DOF_W, SensorMPU9X50.channelQuatMpl9DofW);
		aMap.put(ObjectClusterSensorName.QUAT_MPL_9DOF_X, SensorMPU9X50.channelQuatMpl9DofX);
		aMap.put(ObjectClusterSensorName.QUAT_MPL_9DOF_Y, SensorMPU9X50.channelQuatMpl9DofY);
		aMap.put(ObjectClusterSensorName.QUAT_MPL_9DOF_Z, SensorMPU9X50.channelQuatMpl9DofZ);
		
		// MPL Euler
		aMap.put(ObjectClusterSensorName.EULER_MPL_6DOF_X, SensorMPU9X50.channelEulerMpl6DofX);
		aMap.put(ObjectClusterSensorName.EULER_MPL_6DOF_Y, SensorMPU9X50.channelEulerMpl6DofY);
		aMap.put(ObjectClusterSensorName.EULER_MPL_6DOF_Z, SensorMPU9X50.channelEulerMpl6DofZ);
		
		aMap.put(ObjectClusterSensorName.EULER_MPL_9DOF_X, SensorMPU9X50.channelEulerMpl9DofX);
		aMap.put(ObjectClusterSensorName.EULER_MPL_9DOF_Y, SensorMPU9X50.channelEulerMpl9DofY);
		aMap.put(ObjectClusterSensorName.EULER_MPL_9DOF_Z, SensorMPU9X50.channelEulerMpl9DofZ);

		// MPL Heading
		aMap.put(ObjectClusterSensorName.MPL_HEADING, SensorMPU9X50.channelMplHeading);

		// MPU9150 Temperature
		aMap.put(ObjectClusterSensorName.MPL_TEMPERATURE, SensorMPU9X50.channelMplTemperature);

		// MPL Pedometer
		aMap.put(ObjectClusterSensorName.MPL_PEDOM_CNT, SensorMPU9X50.channelMplPedomCount);
		aMap.put(ObjectClusterSensorName.MPL_PEDOM_TIME, SensorMPU9X50.channelMplPedomTime);

//		// MPL Tap
//		aMap.put(ObjectClusterSensorName.TAPDIRANDTAPCNT, SensorMPU9X50.channelMplTapDirAndTapCnt);
		// MPL Tap Direction
		aMap.put(ObjectClusterSensorName.TAPDIR, SensorMPU9X50.channelMplTapDir);
		// MPL Tap Count
		aMap.put(ObjectClusterSensorName.TAPCNT, SensorMPU9X50.channelMplTapCnt);
		
		// MPL Motion Orient
		aMap.put(ObjectClusterSensorName.MOTIONANDORIENT, SensorMPU9X50.channelMplMotionAndOrient);
		aMap.put(ObjectClusterSensorName.MOTION, SensorMPU9X50.channelMplMotion);
		aMap.put(ObjectClusterSensorName.ORIENT, SensorMPU9X50.channelMplOrient);
		// Raw 6DOF Quaterian's from the DMP hardware module of the MPU9150
		aMap.put(ObjectClusterSensorName.QUAT_DMP_6DOF_W, SensorMPU9X50.channelQuatDmp6DofW);
		aMap.put(ObjectClusterSensorName.QUAT_DMP_6DOF_X, SensorMPU9X50.channelQuatDmp6DofX);
		aMap.put(ObjectClusterSensorName.QUAT_DMP_6DOF_Y, SensorMPU9X50.channelQuatDmp6DofY);
		aMap.put(ObjectClusterSensorName.QUAT_DMP_6DOF_Z, SensorMPU9X50.channelQuatDmp6DofZ);
		mChannelMapRef = Collections.unmodifiableMap(aMap);
    }

	//--------- Channel info end --------------


	/** Constructor for this class
	 * @param svo
	 */
	public SensorMPU9X50(ShimmerVerObject svo){
		super(svo);
		setSensorName(SENSORS.MPU9X50.toString());
	}
	
	
	//--------- Abstract methods implemented start --------------
	
	@Override
	public void generateSensorMap(ShimmerVerObject svo) {
		//TODO populate the other channels depending on firmware version
		super.createLocalSensorMapWithCustomParser(mSensorMapRef, mChannelMapRef);
	}
	
	@Override
	public void generateConfigOptionsMap(ShimmerVerObject svo) {
		mConfigOptionsMap.clear();
		mConfigOptionsMap.put(GuiLabelConfig.MPU9150_GYRO_RANGE,configOptionMpu9150GyroRange); 
		//MPL Options
		mConfigOptionsMap.put(GuiLabelConfig.MPU9150_ACCEL_RANGE, configOptionMpu9150AccelRange);
		mConfigOptionsMap.put(GuiLabelConfig.MPU9150_DMP_GYRO_CAL, configOptionMpu9150DmpGyroCal); 
		mConfigOptionsMap.put(GuiLabelConfig.MPU9150_MPL_LPF, configOptionMpu9150MplLpf); 
		mConfigOptionsMap.put(GuiLabelConfig.MPU9150_MPL_RATE, configOptionMpu9150MplRate); 
		mConfigOptionsMap.put(GuiLabelConfig.MPU9150_MAG_RATE,configOptionMpu9150MagRate);
		//MPL CheckBoxes
		mConfigOptionsMap.put(GuiLabelConfig.MPU9150_DMP, configOptionMpu9150Dmp); 
		mConfigOptionsMap.put(GuiLabelConfig.MPU9150_MPL, configOptionMpu9150Mpl);
		mConfigOptionsMap.put(GuiLabelConfig.MPU9150_MPL_9DOF_SENSOR_FUSION, configOptionMpu9150Mpl9DofSensorFusion);
		mConfigOptionsMap.put(GuiLabelConfig.MPU9150_MPL_GYRO_CAL, configOptionMpu9150MplGyroCal);
		mConfigOptionsMap.put(GuiLabelConfig.MPU9150_MPL_VECTOR_CAL, configOptionMpu9150MplVectorCal);
		mConfigOptionsMap.put(GuiLabelConfig.MPU9150_MPL_MAG_CAL,configOptionMpu9150MplMagCal); 
		//General Config
		mConfigOptionsMap.put(GuiLabelConfig.MPU9150_GYRO_RATE,configOptionMpu9150GyroRate); 
		mConfigOptionsMap.put(GuiLabelConfig.MPU9150_GYRO_LPM,configOptionMpu9150GyroLpm); 
	}

	@Override
	public void generateSensorGroupMapping(ShimmerVerObject svo) {
		mSensorGroupingMap = new LinkedHashMap<Integer, SensorGroupingDetails>();

		if((svo.mHardwareVersion==HW_ID.SHIMMER_3 && svo.getFirmwareIdentifier()==FW_ID.SDLOG) //){
				|| svo.mHardwareVersion==HW_ID.SHIMMER_4_SDK){
			
			if(svo.mHardwareVersion==HW_ID.SHIMMER_4_SDK){
				mSensorGroupingMap.put(Configuration.Shimmer3.GuiLabelSensorTiles.MPU.ordinal(), new SensorGroupingDetails(
						GuiLabelSensorTiles.MPU,
						Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_ACCEL,
								Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_GYRO,
								Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_MAG),
						CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW));
			}
			else {
				mSensorGroupingMap.put(Configuration.Shimmer3.GuiLabelSensorTiles.GYRO.ordinal(), new SensorGroupingDetails(
						GuiLabelSensorTiles.GYRO,
						Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_GYRO),
						CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW));
			}

			mSensorGroupingMap.put(Configuration.Shimmer3.GuiLabelSensorTiles.MPU_ACCEL_GYRO_MAG.ordinal(), new SensorGroupingDetails(
					GuiLabelSensorTiles.MPU_ACCEL_GYRO_MAG,
					Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_MPL_ACCEL,
							Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_MPL_GYRO,
							Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_MPL_MAG),
					CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors));
			
			mSensorGroupingMap.put(Configuration.Shimmer3.GuiLabelSensorTiles.MPU_OTHER.ordinal(), new SensorGroupingDetails(
					GuiLabelSensorTiles.MPU_OTHER,
					Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_TEMP,
								Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_MPL_QUAT_6DOF),
					CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors));
			
		}
//		else {
//			mSensorGroupingMap.put(GuiLabelSensorTiles.GYRO, new SensorGroupingDetails(
//					Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_GYRO)));
//			mSensorGroupingMap.get(GuiLabelSensorTiles.GYRO).mListOfCompatibleVersionInfo = CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW;
//		}
		super.updateSensorGroupingMap();
	}
	
	
	@Override
	public ObjectCluster processDataCustom(SensorDetails sensorDetails, byte[] rawData, COMMUNICATION_TYPE commType, ObjectCluster objectCluster, boolean isTimeSyncEnabled, long pcTimestamp) {

		sensorDetails.processDataCommon(rawData, commType, objectCluster, isTimeSyncEnabled, pcTimestamp);
//		int index = 0;
//		for (ChannelDetails channelDetails:sensorDetails.mListOfChannels){
//			//first process the data originating from the Shimmer sensor
//			byte[] channelByteArray = new byte[channelDetails.mDefaultNumBytes];
//			System.arraycopy(sensorByteArray, index, channelByteArray, 0, channelDetails.mDefaultNumBytes);
//			objectCluster = SensorDetails.processShimmerChannelData(channelByteArray, channelDetails, objectCluster);
//			objectCluster.incrementIndexKeeper();
//			index = index + channelDetails.mDefaultNumBytes;
//		}
		
//		-----------------------Calibration Start----------------------------------
		
		if (mEnableCalibration){

			//Uncalibrated Gyro data
			if(sensorDetails.mSensorDetailsRef.mGuiFriendlyLabel.equals(GuiLabelSensors.GYRO)){
				double[] unCalibratedGyroData = new double[3];
				for(ChannelDetails channelDetails:sensorDetails.mListOfChannels){
					if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.GYRO_X)){
						unCalibratedGyroData[0] = (double)((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(channelDetails.mObjectClusterName), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
					}
					else if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.GYRO_Y)){
						unCalibratedGyroData[1] = (double)((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(channelDetails.mObjectClusterName), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
					}
					else if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.GYRO_Z)){
						unCalibratedGyroData[2] = (double)((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(channelDetails.mObjectClusterName), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
					}
				}
	
				double[] gyroCalibratedData = UtilCalibration.calibrateInertialSensorData(unCalibratedGyroData, mAlignmentMatrixGyroscope, mSensitivityMatrixGyroscope, mOffsetVectorGyroscope);
				//for testing
//				double[] gyroCalibratedData = UtilCalibration.calibrateInertialSensorData(unCalibratedGyroData, AlignmentMatrixGyroShimmer3, SensitivityMatrixGyro1000dpsShimmer3, OffsetVectorGyroShimmer3);
				
				if (mEnableOntheFlyGyroOVCal){
					mGyroXX.addValue(gyroCalibratedData[0]);
					mGyroXY.addValue(gyroCalibratedData[1]);
					mGyroXZ.addValue(gyroCalibratedData[2]);
	
					if (mGyroXX.getStandardDeviation()<mGyroXOVCalThreshold && mGyroXY.getStandardDeviation()<mGyroXOVCalThreshold && mGyroXZ.getStandardDeviation()<mGyroXOVCalThreshold){
						mOffsetVectorGyroscope[0][0]=mGyroXXRaw.getMean();
						mOffsetVectorGyroscope[1][0]=mGyroXYRaw.getMean();
						mOffsetVectorGyroscope[2][0]=mGyroXZRaw.getMean();
					}
				}
				
				//Add calibrated data to Object cluster
				for(ChannelDetails channelDetails:sensorDetails.mListOfChannels){
					if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.GYRO_X)){
						objectCluster.addCalData(channelDetails, gyroCalibratedData[0], objectCluster.getIndexKeeper()-3);
					}
					else if(channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.GYRO_Y)){
						objectCluster.addCalData(channelDetails, gyroCalibratedData[1], objectCluster.getIndexKeeper()-2);
					}
					else if(channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.GYRO_Z)){
						objectCluster.addCalData(channelDetails, gyroCalibratedData[2], objectCluster.getIndexKeeper()-2);
					}
				}
			}
			
	
			//Uncalibrated Accel data
			if(sensorDetails.mSensorDetailsRef.mGuiFriendlyLabel.equals(GuiLabelSensors.ACCEL_MPU)){
				double[] unCalibratedAccelData = new double[3];
				for(ChannelDetails channelDetails:sensorDetails.mListOfChannels){
					if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.ACCEL_MPU_X)){
						unCalibratedAccelData[0] = ((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(channelDetails.mObjectClusterName), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
					}
					else if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.ACCEL_MPU_Y)){
						unCalibratedAccelData[1] = ((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(channelDetails.mObjectClusterName), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
					}
					else if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.ACCEL_MPU_Z)){
						unCalibratedAccelData[2] = ((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(channelDetails.mObjectClusterName), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
					}
				}
			}
			
			//Uncalibrated Mag data
			if(sensorDetails.mSensorDetailsRef.mGuiFriendlyLabel.equals(GuiLabelSensors.MAG_MPU)){
				double[] unCalibratedMagData = new double[3];
				for(ChannelDetails channelDetails:sensorDetails.mListOfChannels){
					if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.MAG_MPU_X)){
						unCalibratedMagData[0] = ((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(channelDetails.mObjectClusterName), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
					}
					else if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.MAG_MPU_Y)){
						unCalibratedMagData[1] = ((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(channelDetails.mObjectClusterName), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
					}
					else if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.MAG_MPU_Z)){
						unCalibratedMagData[2] = ((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(channelDetails.mObjectClusterName), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
					}
				}
				
				double[] calData = UtilCalibration.calibrateInertialSensorData(unCalibratedMagData, mAlignmentMatrixMagnetometer, mSensitivityMatrixMagnetometer, mOffsetVectorMagnetometer);
				//Add calibrated data to Object cluster
				for(ChannelDetails channelDetails:sensorDetails.mListOfChannels){
					if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.MAG_MPU_X)){
						objectCluster.addCalData(channelDetails, calData[0], objectCluster.getIndexKeeper()-3);
					}
					else if(channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.MAG_MPU_Y)){
						objectCluster.addCalData(channelDetails, calData[1], objectCluster.getIndexKeeper()-2);
					}
					else if(channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.MAG_MPU_Z)){
						objectCluster.addCalData(channelDetails, calData[2], objectCluster.getIndexKeeper()-2);
					}
				}
			}
			
			//UnCal + Cal Gyro_Mpu_Mpl_data
			if(sensorDetails.mSensorDetailsRef.mGuiFriendlyLabel.equals(GuiLabelSensors.GYRO_MPU_MPL)){
				for(ChannelDetails channelDetails:sensorDetails.mListOfChannels){
					if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.GYRO_MPU_MPL_X)){
	//						unCalibratedGyroMpuMplData[0] = ((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(channelDetails.mObjectClusterName), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
						double unCalData = ((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(channelDetails.mObjectClusterName), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
						double calData = unCalData/Math.pow(2, 16);
						objectCluster.addCalData(channelDetails, calData, objectCluster.getIndexKeeper()-3);
					}
					else if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.GYRO_MPU_MPL_Y)){
	//						unCalibratedGyroMpuMplData[1] = ((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(channelDetails.mObjectClusterName), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
						double unCalData = ((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(channelDetails.mObjectClusterName), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
						double calData = unCalData/Math.pow(2, 16);
						objectCluster.addCalData(channelDetails, calData, objectCluster.getIndexKeeper()-2);
					}
					else if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.GYRO_MPU_MPL_Z)){
	//						unCalibratedGyroMpuMplData[2] = ((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(channelDetails.mObjectClusterName), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
						double unCalData = ((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(channelDetails.mObjectClusterName), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
						double calData = unCalData/Math.pow(2, 16);
						objectCluster.addCalData(channelDetails, calData, objectCluster.getIndexKeeper()-1);
					}
				}
			}
			// UnCal + Cal Accel_Mpu_Mpl_data
			if(sensorDetails.mSensorDetailsRef.mGuiFriendlyLabel.equals(GuiLabelSensors.ACCEL_MPU_MPL)){
				for(ChannelDetails channelDetails:sensorDetails.mListOfChannels){
					if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.ACCEL_MPU_MPL_X)){
	//						unCalibratedAccelMpuMplData[0] = ((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(channelDetails.mObjectClusterName), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
						double unCalData = ((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(channelDetails.mObjectClusterName), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
						double calData = unCalData/Math.pow(2, 16);
						objectCluster.addCalData(channelDetails, calData, objectCluster.getIndexKeeper()-3);
					}
					else if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.ACCEL_MPU_MPL_Y)){
	//						unCalibratedAccelMpuMplData[1] = ((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(channelDetails.mObjectClusterName), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
						double unCalData = ((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(channelDetails.mObjectClusterName), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
						double calData = unCalData/Math.pow(2, 16);
						objectCluster.addCalData(channelDetails, calData, objectCluster.getIndexKeeper()-2);
					}
					else if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.ACCEL_MPU_MPL_Z)){
	//						unCalibratedAccelMpuMplData[2] = ((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(channelDetails.mObjectClusterName), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
						double unCalData = ((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(channelDetails.mObjectClusterName), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
						double calData = unCalData/Math.pow(2, 16);
						objectCluster.addCalData(channelDetails, calData, objectCluster.getIndexKeeper()-1);
					}
				}
			}
			//UnCal + Cal Mag_Mpu_Mpl_data
			if(sensorDetails.mSensorDetailsRef.mGuiFriendlyLabel.equals(GuiLabelSensors.MAG_MPU_MPL)){
				for(ChannelDetails channelDetails:sensorDetails.mListOfChannels){
					if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.MAG_MPU_MPL_X)){
	//						unCalibratedMagMpuMplData[0] = ((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(channelDetails.mObjectClusterName), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
						double unCalData = ((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(channelDetails.mObjectClusterName), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
						double calData = unCalData/Math.pow(2, 16);
						objectCluster.addCalData(channelDetails, calData, objectCluster.getIndexKeeper()-3);
					}
					else if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.MAG_MPU_MPL_Y)){
	//						unCalibratedMagMpuMplData[1] = ((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(channelDetails.mObjectClusterName), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
						double unCalData = ((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(channelDetails.mObjectClusterName), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
						double calData = unCalData/Math.pow(2, 16);
						objectCluster.addCalData(channelDetails, calData, objectCluster.getIndexKeeper()-2);
					}
					else if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.MAG_MPU_MPL_Z)){
	//						unCalibratedMagMpuMplData[2] = ((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(channelDetails.mObjectClusterName), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
						double unCalData = ((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(channelDetails.mObjectClusterName), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
						double calData = unCalData/Math.pow(2, 16);
						objectCluster.addCalData(channelDetails, calData, objectCluster.getIndexKeeper()-1);
					}
				}
			}
			//Uncal + Cal Quat_Mpl_6DOF_data
			if(sensorDetails.mSensorDetailsRef.mGuiFriendlyLabel.equals(GuiLabelSensors.QUAT_DMP_6DOF)){
				for(ChannelDetails channelDetails:sensorDetails.mListOfChannels){
					if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.QUAT_MPL_6DOF_W)){
	//						unCalibratedMplQuat6DOFData[0] = ((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(channelDetails.mObjectClusterName), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
						double unCalData = ((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(channelDetails.mObjectClusterName), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
						double calData = unCalData/Math.pow(2, 30);
						objectCluster.addCalData(channelDetails, calData, objectCluster.getIndexKeeper()-4);
					}
					else if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.QUAT_MPL_6DOF_X)){
	//						unCalibratedMplQuat6DOFData[1] = ((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(channelDetails.mObjectClusterName), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
						double unCalData = ((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(channelDetails.mObjectClusterName), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
						double calData = unCalData/Math.pow(2, 30);
						objectCluster.addCalData(channelDetails, calData, objectCluster.getIndexKeeper()-3);
					}
					else if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.QUAT_MPL_6DOF_Y)){
	//						unCalibratedMplQuat6DOFData[2] = ((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(channelDetails.mObjectClusterName), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
						double unCalData = ((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(channelDetails.mObjectClusterName), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
						double calData = unCalData/Math.pow(2, 30);
						objectCluster.addCalData(channelDetails, calData, objectCluster.getIndexKeeper()-2);
					}
					else if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.QUAT_MPL_6DOF_Z)){
	//						unCalibratedMplQuat6DOFData[3] = ((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(channelDetails.mObjectClusterName), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
						double unCalData = ((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(channelDetails.mObjectClusterName), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
						double calData = unCalData/Math.pow(2, 30);
						objectCluster.addCalData(channelDetails, calData, objectCluster.getIndexKeeper()-1);
					}
					
				}
			}
			//Unca + Cal MPL_Temperature_Data
			if(sensorDetails.mSensorDetailsRef.mGuiFriendlyLabel.equals(GuiLabelSensors.MPL_TEMPERATURE)){
				for(ChannelDetails channelDetails:sensorDetails.mListOfChannels){
					if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.MPL_TEMPERATURE)){
						double unCalData = ((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(channelDetails.mObjectClusterName), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
						double calData = unCalData/Math.pow(2, 16);
						objectCluster.addCalData(channelDetails, calData, objectCluster.getIndexKeeper()-1);
					}
				}
			}
			//Uncal + Cal MPL_Pedometer_Data
			if(sensorDetails.mSensorDetailsRef.mGuiFriendlyLabel.equals(GuiLabelSensors.MPL_PEDOMETER)){
				for(ChannelDetails channelDetails:sensorDetails.mListOfChannels){
					if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.MPL_PEDOM_CNT)){
	//						unCalibratedMplPedometerData[0] = ((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(channelDetails.mObjectClusterName), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
						double calData = ((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(channelDetails.mObjectClusterName), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
						objectCluster.addCalData(channelDetails, calData, objectCluster.getIndexKeeper()-2);
					}
					else if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.MPL_PEDOM_TIME)){
	//						unCalibratedMplPedometerData[1] = ((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(channelDetails.mObjectClusterName), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
						double calData = ((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(channelDetails.mObjectClusterName), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
						objectCluster.addCalData(channelDetails, calData, objectCluster.getIndexKeeper()-1);
					}
				}
			}
			
			
			//Uncal + Cal MPL_HEADING_data
			if(sensorDetails.mSensorDetailsRef.mGuiFriendlyLabel.equals(GuiLabelSensors.MPL_HEADING)){
				for(ChannelDetails channelDetails:sensorDetails.mListOfChannels){
					if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.MPL_HEADING)){
	//						unCalibratedMplHeadingData[0] = ((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(channelDetails.mObjectClusterName), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
						double unCalData = ((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(channelDetails.mObjectClusterName), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
						double calData = unCalData/Math.pow(2, 16);
						objectCluster.addCalData(channelDetails, calData, objectCluster.getIndexKeeper()-1);
					}
				}
			}
			//separate out tap dir and cnt to two channels 
			//Bits 7-5 - Direction,	Bits 4-0 - Count
			//Uncalibrated MPL_TAP_data
			if(sensorDetails.mSensorDetailsRef.mGuiFriendlyLabel.equals(GuiLabelSensors.MPL_TAPDIRANDTAPCNT)){
				for(ChannelDetails channelDetails:sensorDetails.mListOfChannels){
					if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.TAPDIR)){
						double calData = ((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(channelDetails.mObjectClusterName), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
						objectCluster.addCalData(channelDetails, calData, objectCluster.getIndexKeeper()-2);
					}
					else if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.TAPCNT)){
						double calData = ((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(channelDetails.mObjectClusterName), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
						objectCluster.addCalData(channelDetails, calData, objectCluster.getIndexKeeper()-1);
					}
				}
			}
			
			
			//Bit 7 - Motion/No motion,	Bits 5-4 - Display Orientation,	Bits 3-1 - Orientation,	Bit 0 - Flip indicator
			//Uncalibrated MPL_MOTION_ORIENT_data
			if(sensorDetails.mSensorDetailsRef.mGuiFriendlyLabel.equals(GuiLabelSensors.MPL_MOTIONANDORIENT)){
				for(ChannelDetails channelDetails:sensorDetails.mListOfChannels){
	//					if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.MOTIONANDORIENT)){
	//						unCalibratedMplMotionOrientData[0] = ((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(channelDetails.mObjectClusterName), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
					if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.MOTION)){
						double calData = ((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(channelDetails.mObjectClusterName), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
						objectCluster.addCalData(channelDetails, calData, objectCluster.getIndexKeeper()-2);
					}
					else if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.ORIENT)){
						double calData = ((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(channelDetails.mObjectClusterName), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
						objectCluster.addCalData(channelDetails, calData, objectCluster.getIndexKeeper()-1);
					}
				}
			}
		}
		
		
		//Debugging
		if(mIsDebugOutput){
			super.consolePrintChannelsCal(objectCluster, Arrays.asList(
					new String[]{ObjectClusterSensorName.GYRO_X, CHANNEL_TYPE.UNCAL.toString()}, 
					new String[]{ObjectClusterSensorName.GYRO_Y, CHANNEL_TYPE.UNCAL.toString()}, 
					new String[]{ObjectClusterSensorName.GYRO_Z, CHANNEL_TYPE.UNCAL.toString()}, 
					new String[]{ObjectClusterSensorName.GYRO_X, CHANNEL_TYPE.CAL.toString()}, 
					new String[]{ObjectClusterSensorName.GYRO_Y, CHANNEL_TYPE.CAL.toString()}, 
					new String[]{ObjectClusterSensorName.GYRO_Z, CHANNEL_TYPE.CAL.toString()} 
	//				new String[]{ObjectClusterSensorName.GYRO_MPU_MPL_X, CHANNEL_TYPE.CAL.toString()},
	//				new String[]{ObjectClusterSensorName.GYRO_MPU_MPL_Y, CHANNEL_TYPE.CAL.toString()},
	//				new String[]{ObjectClusterSensorName.GYRO_MPU_MPL_Z, CHANNEL_TYPE.CAL.toString()},
	//				new String[]{ObjectClusterSensorName.ACCEL_MPU_MPL_X, CHANNEL_TYPE.CAL.toString()},
	//				new String[]{ObjectClusterSensorName.ACCEL_MPU_MPL_Y, CHANNEL_TYPE.CAL.toString()},
	//				new String[]{ObjectClusterSensorName.ACCEL_MPU_MPL_Z, CHANNEL_TYPE.CAL.toString()},
	//				new String[]{ObjectClusterSensorName.MAG_MPU_MPL_X, CHANNEL_TYPE.CAL.toString()},
	//				new String[]{ObjectClusterSensorName.MAG_MPU_MPL_Y, CHANNEL_TYPE.CAL.toString()},
	//				new String[]{ObjectClusterSensorName.MAG_MPU_MPL_Z, CHANNEL_TYPE.CAL.toString()}
					));
		}
				
		return objectCluster;
	}
	
	@Override
	public void infoMemByteArrayGenerate(ShimmerDevice shimmerDevice, byte[] mInfoMemBytes) {
		int idxConfigSetupByte4 = 128+0;
		int idxConfigSetupByte5 = 128+1;
		int idxConfigSetupByte6 = 128+4;	
		int idxConfigSetupByte1 = 7;
		int idxConfigSetupByte2 = 8;
		int idxConfigSetupByte3 = 9;
//		int idxMPU9150GyroCalibration = 52;
		//fix for newer firmware -> see InfomemLayoutShimmer3
		int idxMPU9150GyroCalibration =     55;
		//Config Byte1
		int bitShiftMPU9150AccelGyroSamplingRate =	0;
		int maskMPU9150AccelGyroSamplingRate = 0xFF;
		//Config Byte2
		int bitShiftMPU9150GyroRange = 0;
		int maskMPU9150GyroRange = 0x03;
		//Config Byte3
		int bitShiftMPU9150AccelRange = 6;
		int maskMPU9150AccelRange = 0x03;
		// MPL related
		int bitShiftMPU9150DMP = 7;
		int maskMPU9150DMP = 0x01;
		int bitShiftMPU9150LPF = 3;
		int maskMPU9150LPF = 0x07;
		int bitShiftMPU9150MotCalCfg = 0;
		int maskMPU9150MotCalCfg = 0x07;
		int bitShiftMPU9150MPLSamplingRate = 5;
		int maskMPU9150MPLSamplingRate = 0x07;
		int bitShiftMPU9150MagSamplingRate = 2;
//		int maskMPU9150MagSamplingRate = 0x07;
		int lengthGeneralCalibrationBytes =	21;
		int bitShiftMPLSensorFusion = 7;
		int maskMPLSensorFusion = 0x01;
		int bitShiftMPLGyroCalTC = 6;
		int maskMPLGyroCalTC = 0x01;
		int bitShiftMPLVectCompCal = 5;
		int maskMPLVectCompCal = 0x01;
		int bitShiftMPLMagDistCal = 4;
		int maskMPLMagDistCal = 0x01;
		int bitShiftMPLEnable = 3;
		int maskMPLEnable = 0x01;

		mInfoMemBytes[idxConfigSetupByte1] |= (byte) ((mMPU9150GyroAccelRate & maskMPU9150AccelGyroSamplingRate) << bitShiftMPU9150AccelGyroSamplingRate);
		mInfoMemBytes[idxConfigSetupByte2] |= (byte) ((mGyroRange & maskMPU9150GyroRange) << bitShiftMPU9150GyroRange);
		mInfoMemBytes[idxConfigSetupByte3] |= (byte) ((mMPU9150AccelRange & maskMPU9150AccelRange) << bitShiftMPU9150AccelRange);

		// MPU9150 Gyroscope Calibration Parameters
		byte[] bufferCalibrationParameters = generateCalParamGyroscope();
		System.arraycopy(bufferCalibrationParameters, 0, mInfoMemBytes, idxMPU9150GyroCalibration, lengthGeneralCalibrationBytes);

		//if(getFirmwareIdentifier()==FW_ID.SDLOG) {
		mInfoMemBytes[idxConfigSetupByte4] |= (byte) ((mMPU9150DMP & maskMPU9150DMP) << bitShiftMPU9150DMP);
		mInfoMemBytes[idxConfigSetupByte4] |= (byte) ((mMPU9150LPF & maskMPU9150LPF) << bitShiftMPU9150LPF);
		mInfoMemBytes[idxConfigSetupByte4] |= (byte) ((mMPU9150MotCalCfg & maskMPU9150MotCalCfg) << bitShiftMPU9150MotCalCfg);

		mInfoMemBytes[idxConfigSetupByte5] |= (byte) ((mMPU9150MPLSamplingRate & maskMPU9150MPLSamplingRate) << bitShiftMPU9150MPLSamplingRate);
		mInfoMemBytes[idxConfigSetupByte5] |= (byte) ((mMPU9150MagSamplingRate & maskMPU9150MPLSamplingRate) << bitShiftMPU9150MagSamplingRate);

		mInfoMemBytes[idxConfigSetupByte6] |= (byte) ((mMPLSensorFusion & maskMPLSensorFusion) << bitShiftMPLSensorFusion);
		mInfoMemBytes[idxConfigSetupByte6] |= (byte) ((mMPLGyroCalTC & maskMPLGyroCalTC) << bitShiftMPLGyroCalTC);
		mInfoMemBytes[idxConfigSetupByte6] |= (byte) ((mMPLVectCompCal & maskMPLVectCompCal) << bitShiftMPLVectCompCal);
		mInfoMemBytes[idxConfigSetupByte6] |= (byte) ((mMPLMagDistCal & maskMPLMagDistCal) << bitShiftMPLMagDistCal);
		mInfoMemBytes[idxConfigSetupByte6] |= (byte) ((mMPLEnable & maskMPLEnable) << bitShiftMPLEnable);

	}

	@Override
	public void infoMemByteArrayParse(ShimmerDevice shimmerDevice, byte[] mInfoMemBytes) {
		int idxConfigSetupByte4 = 128+0;
		int idxConfigSetupByte5 = 128+1;
		int idxConfigSetupByte6 = 128+4;	
		int idxConfigSetupByte1 = 7;
		int idxConfigSetupByte2 = 8;
		int idxConfigSetupByte3 = 9;
//		int idxMPU9150GyroCalibration = 52;
		//fix for newer firmware -> see InfomemLayoutShimmer3
		int idxMPU9150GyroCalibration =     55;

		int idxMPLAccelCalibration = 128+5; //+21
		int idxMPLMagCalibration = 128+26; //+21
		int idxMPLGyroCalibration = 128+47; //+12
		//Config Byte1
		int bitShiftMPU9150AccelGyroSamplingRate =	0;
		int maskMPU9150AccelGyroSamplingRate = 0xFF;
		//Config Byte2
		int bitShiftMPU9150GyroRange = 0;
		int maskMPU9150GyroRange = 0x03;
		//Config Byte3
		int bitShiftMPU9150AccelRange = 6;
		int maskMPU9150AccelRange = 0x03;
		// MPL related
		int bitShiftMPU9150DMP = 7;
		int maskMPU9150DMP = 0x01;
		int bitShiftMPU9150LPF = 3;
		int maskMPU9150LPF = 0x07;
		int bitShiftMPU9150MotCalCfg = 0;
		int maskMPU9150MotCalCfg = 0x07;
		int bitShiftMPU9150MPLSamplingRate = 5;
		int maskMPU9150MPLSamplingRate = 0x07;
		int bitShiftMPU9150MagSamplingRate = 2;
		int maskMPU9150MagSamplingRate = 0x07;
		int lengthGeneralCalibrationBytes =	21;
		int bitShiftMPLSensorFusion = 7;
		int maskMPLSensorFusion = 0x01;
		int bitShiftMPLGyroCalTC = 6;
		int maskMPLGyroCalTC = 0x01;
		int bitShiftMPLVectCompCal = 5;
		int maskMPLVectCompCal = 0x01;
		int bitShiftMPLMagDistCal = 4;
		int maskMPLMagDistCal = 0x01;
		int bitShiftMPLEnable = 3;
		int maskMPLEnable = 0x01;

		mMPU9150GyroAccelRate = (mInfoMemBytes[idxConfigSetupByte1] >> bitShiftMPU9150AccelGyroSamplingRate) & maskMPU9150AccelGyroSamplingRate;
		checkLowPowerGyro(); // check rate to determine if Sensor is in LPM mode

		mGyroRange = (mInfoMemBytes[idxConfigSetupByte2] >> bitShiftMPU9150GyroRange) & maskMPU9150GyroRange;
		mMPU9150AccelRange = (mInfoMemBytes[idxConfigSetupByte3] >> bitShiftMPU9150AccelRange) & maskMPU9150AccelRange;

		// MPU9150 Gyroscope Calibration Parameters
		byte[] bufferCalibrationParameters = new byte[lengthGeneralCalibrationBytes];
		System.arraycopy(mInfoMemBytes, idxMPU9150GyroCalibration, bufferCalibrationParameters, 0 ,lengthGeneralCalibrationBytes);
		retrieveKinematicCalibrationParametersFromPacket(bufferCalibrationParameters, GYRO_CALIBRATION_RESPONSE);

		// InfoMem C - Start - used by SdLog and LogAndStream
		if(mShimmerVerObject.getFirmwareIdentifier()==FW_ID.SDLOG) {
			mMPU9150DMP = (mInfoMemBytes[idxConfigSetupByte4] >> bitShiftMPU9150DMP) & maskMPU9150DMP;
			mMPU9150LPF = (mInfoMemBytes[idxConfigSetupByte4] >> bitShiftMPU9150LPF) & maskMPU9150LPF;
			mMPU9150MotCalCfg =  (mInfoMemBytes[idxConfigSetupByte4] >> bitShiftMPU9150MotCalCfg) & maskMPU9150MotCalCfg;
	
			mMPU9150MPLSamplingRate = (mInfoMemBytes[idxConfigSetupByte5] >> bitShiftMPU9150MPLSamplingRate) & maskMPU9150MPLSamplingRate;
			mMPU9150MagSamplingRate = (mInfoMemBytes[idxConfigSetupByte5] >> bitShiftMPU9150MagSamplingRate) & maskMPU9150MagSamplingRate;
	
	
	
			mMPLSensorFusion = (mInfoMemBytes[idxConfigSetupByte6] >> bitShiftMPLSensorFusion) & maskMPLSensorFusion;
			mMPLGyroCalTC = (mInfoMemBytes[idxConfigSetupByte6] >> bitShiftMPLGyroCalTC) & maskMPLGyroCalTC;
			mMPLVectCompCal = (mInfoMemBytes[idxConfigSetupByte6] >> bitShiftMPLVectCompCal) & maskMPLVectCompCal;
			mMPLMagDistCal = (mInfoMemBytes[idxConfigSetupByte6] >> bitShiftMPLMagDistCal) & maskMPLMagDistCal;
			mMPLEnable = (mInfoMemBytes[idxConfigSetupByte6] >> bitShiftMPLEnable) & maskMPLEnable;
	
			String[] dataType={"i16","i16","i16","i16","i16","i16","i8","i8","i8","i8","i8","i8","i8","i8","i8"};
	
			//MPL Accel Calibration Parameters
			bufferCalibrationParameters = new byte[lengthGeneralCalibrationBytes];
			System.arraycopy(mInfoMemBytes, idxMPLAccelCalibration, bufferCalibrationParameters, 0 , lengthGeneralCalibrationBytes);
			int[] formattedPacket = UtilParseData.formatDataPacketReverse(bufferCalibrationParameters,dataType);
			double[] AM=new double[9];
			for (int i=0;i<9;i++) {
				AM[i]=((double)formattedPacket[6+i])/100;
			}
			double[][] alignmentMatrixMPLA = {{AM[0],AM[1],AM[2]},{AM[3],AM[4],AM[5]},{AM[6],AM[7],AM[8]}}; 				
			double[][] sensitivityMatrixMPLA = {{formattedPacket[3],0,0},{0,formattedPacket[4],0},{0,0,formattedPacket[5]}}; 
			double[][] offsetVectorMPLA = {{formattedPacket[0]},{formattedPacket[1]},{formattedPacket[2]}};
			AlignmentMatrixMPLAccel = alignmentMatrixMPLA; 			
			SensitivityMatrixMPLAccel = sensitivityMatrixMPLA; 	
			OffsetVectorMPLAccel = offsetVectorMPLA;
	
			//MPL Mag Calibration Configuration
			bufferCalibrationParameters = new byte[lengthGeneralCalibrationBytes];
			System.arraycopy(mInfoMemBytes, idxMPLMagCalibration, bufferCalibrationParameters, 0 , lengthGeneralCalibrationBytes);
			formattedPacket = UtilParseData.formatDataPacketReverse(bufferCalibrationParameters,dataType);
			AM=new double[9];
			for (int i=0;i<9;i++) {
				AM[i]=((double)formattedPacket[6+i])/100;
			}
			double[][] alignmentMatrixMPLMag = {{AM[0],AM[1],AM[2]},{AM[3],AM[4],AM[5]},{AM[6],AM[7],AM[8]}}; 				
			double[][] sensitivityMatrixMPLMag = {{formattedPacket[3],0,0},{0,formattedPacket[4],0},{0,0,formattedPacket[5]}}; 
			double[][] offsetVectorMPLMag = {{formattedPacket[0]},{formattedPacket[1]},{formattedPacket[2]}};
			AlignmentMatrixMPLMag = alignmentMatrixMPLMag; 			
			SensitivityMatrixMPLMag = sensitivityMatrixMPLMag; 	
			OffsetVectorMPLMag = offsetVectorMPLMag;
	
			//MPL Gyro Calibration Configuration
			bufferCalibrationParameters = new byte[lengthGeneralCalibrationBytes];
			System.arraycopy(mInfoMemBytes, idxMPLGyroCalibration, bufferCalibrationParameters, 0 , lengthGeneralCalibrationBytes);
			formattedPacket = UtilParseData.formatDataPacketReverse(bufferCalibrationParameters,dataType);
			AM=new double[9];
			for (int i=0;i<9;i++) {
				AM[i]=((double)formattedPacket[6+i])/100;
			}
			double[][] alignmentMatrixMPLGyro = {{AM[0],AM[1],AM[2]},{AM[3],AM[4],AM[5]},{AM[6],AM[7],AM[8]}}; 				
			double[][] sensitivityMatrixMPLGyro = {{formattedPacket[3],0,0},{0,formattedPacket[4],0},{0,0,formattedPacket[5]}}; 
			double[][] offsetVectorMPLGyro = {{formattedPacket[0]},{formattedPacket[1]},{formattedPacket[2]}};
			AlignmentMatrixMPLGyro = alignmentMatrixMPLGyro; 			
			SensitivityMatrixMPLGyro = sensitivityMatrixMPLGyro; 	
			OffsetVectorMPLGyro = offsetVectorMPLGyro;
		}
	}
		

	@Override
	public Object setConfigValueUsingConfigLabel(Integer sensorMapKey, String configLabel, Object valueToSet) {

		Object returnValue = null;
		int buf = 0;

		switch(configLabel){
//Booleans
			case(GuiLabelConfig.MPU9150_DMP):
		    	setMPU9150DMP((boolean)valueToSet);
		    	break;
			case(GuiLabelConfig.MPU9150_MPL):
		    	setMPLEnable((boolean)valueToSet);
		    	break;
			case(GuiLabelConfig.MPU9150_MPL_9DOF_SENSOR_FUSION):
		    	setMPLSensorFusion((boolean)valueToSet);
		    	break;
			case(GuiLabelConfig.MPU9150_MPL_GYRO_CAL):
		    	setMPLGyroCalTC((boolean)valueToSet);
		    	break;
			case(GuiLabelConfig.MPU9150_MPL_VECTOR_CAL):
		    	setMPLVectCompCal((boolean)valueToSet);
		    	break;
			case(GuiLabelConfig.MPU9150_MPL_MAG_CAL):
		    	setMPLMagDistCal((boolean)valueToSet);
		    	break;
//Integers
			case(GuiLabelConfig.MPU9150_GYRO_RANGE):
	        	setMPU9150GyroRange((int)valueToSet);
	        	break;

			case(GuiLabelConfig.MPU9150_ACCEL_RANGE):
				setMPU9150AccelRange((int)valueToSet);
	        	break;
			case(GuiLabelConfig.MPU9150_DMP_GYRO_CAL):
				setMPU9150MotCalCfg((int)valueToSet);
	        	break;
			case(GuiLabelConfig.MPU9150_MPL_LPF):
				setMPU9150LPF((int)valueToSet);
	        	break;
			case(GuiLabelConfig.MPU9150_MPL_RATE):
				setMPU9150MPLSamplingRate((int)valueToSet);
	        	break;
			case(GuiLabelConfig.MPU9150_MAG_RATE):
				setMPU9150MagSamplingRate((int)valueToSet);
	        	break;

//Strings
			case(GuiLabelConfig.MPU9150_GYRO_RATE):
            	double bufDouble = 4.0; // Minimum = 4Hz
            	if(((String)valueToSet).isEmpty()) {
            		bufDouble = 4.0;
            	}
            	else {
            		bufDouble = Double.parseDouble((String)valueToSet);
            	}
            	
            	// Since user is manually entering a freq., clear low-power mode so that their chosen rate will be set correctly. Tick box will be re-enabled automatically if they enter LPM freq. 
            	setLowPowerGyro(false); 
        		setMPU9150GyroAccelRateFromFreq(bufDouble);

        		returnValue = Double.toString((double)Math.round(getMPU9150GyroAccelRateInHz() * 100) / 100); // round sampling rate to two decimal places
//        		System.out.println("Gyro Sampling rate: " + getMPU9150GyroAccelRateInHz() + " " + returnValue);

	        	break;
			case(GuiLabelConfig.MPU9150_GYRO_RATE_HZ):
//				returnValue = getMPU9150GyroAccelRateInHz();
	        	break;
			
			case(GuiLabelConfigCommon.KINEMATIC_CALIBRATION_ALL):
				TreeMap<Integer, CalibDetailsKinematic> mapOfKinematicSensorCalibration = (TreeMap<Integer, CalibDetailsKinematic>) valueToSet;
				setKinematicCalibration(mapOfKinematicSensorCalibration);
				returnValue = valueToSet;
	    		break;

			case(GuiLabelConfigCommon.RANGE):
				if(sensorMapKey==Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_GYRO){
					this.setConfigValueUsingConfigLabel(GuiLabelConfig.MPU9150_GYRO_RANGE, valueToSet);
				}
				else if(sensorMapKey==Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_ACCEL){
					this.setConfigValueUsingConfigLabel(GuiLabelConfig.MPU9150_ACCEL_RANGE, valueToSet);
				}
				break;
			case(GuiLabelConfigCommon.RATE):
				if(sensorMapKey==Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_GYRO){
					this.setConfigValueUsingConfigLabel(GuiLabelConfig.MPU9150_GYRO_RATE, valueToSet);
				}
				else if(sensorMapKey==Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_ACCEL){
					this.setConfigValueUsingConfigLabel(GuiLabelConfig.MPU9150_GYRO_RATE, valueToSet);
				}
				break;			
			
	        default:
//	        	returnValue = super.setConfigValueUsingConfigLabel(componentName, valueToSet);
	        	break;
		}
		return returnValue;
	}
	
	
	@Override
	public Object getConfigValueUsingConfigLabel(Integer sensorMapKey, String configLabel) {
		Object returnValue = null;
		switch(configLabel){
//Booleans
			case(GuiLabelConfig.MPU9150_DMP):
				returnValue = isMPU9150DMP();
	        	break;
			case(GuiLabelConfig.MPU9150_MPL):
				returnValue = isMPLEnable();
	        	break;
			case(GuiLabelConfig.MPU9150_MPL_9DOF_SENSOR_FUSION):
				returnValue = isMPLSensorFusion();
	        	break;
			case(GuiLabelConfig.MPU9150_MPL_GYRO_CAL):
				returnValue = isMPLGyroCalTC();
	        	break;
			case(GuiLabelConfig.MPU9150_MPL_VECTOR_CAL):
				returnValue = isMPLVectCompCal();
	        	break;
			case(GuiLabelConfig.MPU9150_MPL_MAG_CAL):
				returnValue = isMPLMagDistCal();
	        	break;

//Integers
			case(GuiLabelConfig.MPU9150_GYRO_RANGE):
				returnValue = getGyroRange();
	        	break;

			case(GuiLabelConfig.MPU9150_ACCEL_RANGE):
				returnValue = getMPU9150AccelRange();
		    	break;
			case(GuiLabelConfig.MPU9150_DMP_GYRO_CAL):
				returnValue = getMPU9150MotCalCfg();
		    	break;
			case(GuiLabelConfig.MPU9150_MPL_LPF):
				returnValue = getMPU9150LPF();
		    	break;
			case(GuiLabelConfig.MPU9150_MPL_RATE):
				returnValue = getMPU9150MPLSamplingRate();
				break;
			case(GuiLabelConfig.MPU9150_MAG_RATE):
				returnValue = getMPU9150MagSamplingRate();
		    	break;
		    	
//Strings
			case(GuiLabelConfig.MPU9150_GYRO_RATE):
				returnValue = Double.toString((double)Math.round(getMPU9150GyroAccelRateInHz() * 100) / 100); // round sampling rate to two decimal places
//    		    		System.out.println("Gyro Sampling rate: " + getMPU9150GyroAccelRateInHz() + " " + returnValue);
	        	break;
			case(GuiLabelConfig.MPU9150_GYRO_RATE_HZ):
				returnValue = getMPU9150GyroAccelRateInHz();
				break;
				
//			case(Configuration.Shimmer3.GuiLabelConfig.KINEMATIC_CALIBRATION_ALL):
//				returnValue = getKinematicCalibration();
//				break;
			case(GuiLabelConfigCommon.KINEMATIC_CALIBRATION):
				returnValue = getKinematicCalibration();
				break;
			case(GuiLabelConfigCommon.RANGE):
				if(sensorMapKey==Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_GYRO){
					returnValue = this.getConfigValueUsingConfigLabel(GuiLabelConfig.MPU9150_GYRO_RANGE);
				}
				else if(sensorMapKey==Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_ACCEL){
					returnValue = this.getConfigValueUsingConfigLabel(GuiLabelConfig.MPU9150_ACCEL_RANGE);
				}
				break;
			case(GuiLabelConfigCommon.RATE):
				if(sensorMapKey==Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_GYRO){
					returnValue = this.getConfigValueUsingConfigLabel(GuiLabelConfig.MPU9150_GYRO_RATE);
				}
				else if(sensorMapKey==Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_ACCEL){
					returnValue = this.getConfigValueUsingConfigLabel(GuiLabelConfig.MPU9150_GYRO_RATE);
				}
				break;
				
		    default:
	        	break;
	    }

		return returnValue;
	}

	@Override
	public Object getSettings(String componentName, COMMUNICATION_TYPE commType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ActionSetting setSettings(String componentName, Object valueToSet, COMMUNICATION_TYPE commType) {
		
		ActionSetting actionSetting = new ActionSetting(commType);
		switch(componentName){
			case(GuiLabelConfig.MPU9150_ACCEL_RANGE):
//				if (commType == COMMUNICATION_TYPE.BLUETOOTH){
//					
//				} else if (commType == COMMUNICATION_TYPE.DOCK){
//					
//				} else if (commType == COMMUNICATION_TYPE.CLASS){
//					
//				}
			break;
		}
		return actionSetting;
	}
	
	
	@Override
	public void setSensorSamplingRate(double samplingRateHz) {
		setMPU9150GyroAccelRateFromFreq(samplingRateHz);
		if(mShimmerVerObject.getFirmwareIdentifier()==FW_ID.SDLOG
				||mShimmerVerObject.getFirmwareIdentifier()==FW_ID.SHIMMER4_SDK_STOCK){
			setMPU9150MagRateFromFreq(samplingRateHz);
			setMPU9150MplRateFromFreq(samplingRateHz);
		}
	}

	@Override
	public boolean setDefaultConfigForSensor(int sensorMapKey, boolean isSensorEnabled) {
		if(mSensorMap.containsKey(sensorMapKey)){
			//RS (30/5/2016) - commented in ShimmerObject			
			if(sensorMapKey==Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_ACCEL){
				setDefaultMpu9150AccelSensorConfig(isSensorEnabled);
			}
			else if(sensorMapKey==Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_GYRO){
				setDefaultMpu9150GyroSensorConfig(isSensorEnabled);
			}
			else if(sensorMapKey == Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_MAG){
		//		setMPU9150MagRateFromFreq(getSamplingRateShimmer());
				setMPU9150MagRateFromFreq(mMaxSetShimmerSamplingRate);
			}
			else if(SensorMPU9X50.mListOfMplChannels.contains(sensorMapKey)){ //RS (30/5/2016) - Why is a default config set if only one MPL sensor is enabled? 
				if(!checkIfAnyOtherMplChannelEnabled(sensorMapKey)) {
					setDefaultMpu9150MplSensorConfig(isSensorEnabled);
				}
			}
			
			return true;
		}
		return false;
	}
	
	// ----------- MPU9X50 options start -------------------------

	/**
	 * Computes next higher available sensor sampling rate setting based on
	 * passed in "freq" variable and dependent on whether low-power mode is set.
	 * 
	 * @param freq
	 * @return int the rate configuration setting for the respective sensor
	 */
	public int setMPU9150GyroAccelRateFromFreq(double freq) {
		boolean setFreq = false;
		// Check if channel is enabled 
		if(checkIfAnyMplChannelEnabled()){
			setFreq = true;
		}
		else if(checkIfAMpuGyroOrAccelEnabled()){
			setFreq = true;
		}
		
		if(setFreq){
			// Gyroscope Output Rate = 8kHz when the DLPF (Digital Low-pass filter) is disabled (DLPF_CFG = 0 or 7), and 1kHz when the DLPF is enabled
			double numerator = 1000;
			if(mMPU9150LPF == 0) {
				numerator = 8000;
			}
	
			if (!mLowPowerGyro){
				if(freq<4) {
					freq = 4;
				}
				else if(freq>numerator) {
					freq = numerator;
				}
				int result = (int) Math.floor(((numerator / freq) - 1));
				if(result>255) {
					result = 255;
				}
				mMPU9150GyroAccelRate = result;
	
			}
			else {
				mMPU9150GyroAccelRate = 0xFF; // Dec. = 255, Freq. = 31.25Hz (or 3.92Hz when LPF enabled)
			}
		}
		else {
			mMPU9150GyroAccelRate = 0xFF; // Dec. = 255, Freq. = 31.25Hz (or 3.92Hz when LPF enabled)
		}
		return mMPU9150GyroAccelRate;
	}
	
	/**
	 * Computes next higher available sensor sampling rate setting based on
	 * passed in "freq" variable and dependent on whether low-power mode is set.
	 * 
	 * @param freq
	 * @return int the rate configuration setting for the respective sensor
	 */
	private int setMPU9150MagRateFromFreq(double freq) {
		boolean setFreq = false;
		// Check if channel is enabled 
		if(checkIfAnyMplChannelEnabled()){
			setFreq = true;
		}
		else if (isSensorEnabled(Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_MAG)) {
			setFreq = true;
		}
		
		if(setFreq){
			if (freq<=10){
				mMPU9150MagSamplingRate = 0; // 10Hz
			} else if (freq<=20){
				mMPU9150MagSamplingRate = 1; // 20Hz
			} else if (freq<=40) {
				mMPU9150MagSamplingRate = 2; // 40Hz
			} else if (freq<=50) {
				mMPU9150MagSamplingRate = 3; // 50Hz
			} else {
				mMPU9150MagSamplingRate = 4; // 100Hz
			}
		}
		else {
			mMPU9150MagSamplingRate = 0; // 10 Hz
		}
		return mMPU9150MagSamplingRate;
	}
	
	/**
	 * Computes next higher available sensor sampling rate setting based on
	 * passed in "freq" variable and dependent on whether low-power mode is set.
	 * 
	 * @param freq
	 * @return int the rate configuration setting for the respective sensor
	 */
	private int setMPU9150MplRateFromFreq(double freq) {
		// Check if channel is enabled 
		if(!checkIfAnyMplChannelEnabled()){
			mMPU9150MPLSamplingRate = 0; // 10 Hz
			return mMPU9150MPLSamplingRate;
		}
		
		if (freq<=10){
			mMPU9150MPLSamplingRate = 0; // 10Hz
		} else if (freq<=20){
			mMPU9150MPLSamplingRate = 1; // 20Hz
		} else if (freq<=40) {
			mMPU9150MPLSamplingRate = 2; // 40Hz
		} else if (freq<=50) {
			mMPU9150MPLSamplingRate = 3; // 50Hz
		} else {
			mMPU9150MPLSamplingRate = 4; // 100Hz
		}
		return mMPU9150MPLSamplingRate;
	}
	
	private void setDefaultMpu9150GyroSensorConfig(boolean isSensorEnabled) {
		if(!checkIfAnyMplChannelEnabled()) {
			if(!isSensorEnabled(Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_ACCEL)) {
				if(isSensorEnabled) {
					setLowPowerGyro(false);
				}
				else {
					setLowPowerGyro(true);
				}
			}
			
			mGyroRange=1;
//			if(!state){
//				mGyroRange=1; // 500dps
//			}
		}
		else {
			mGyroRange=3; // 2000dps
		}
	}
	
	private void setDefaultMpu9150AccelSensorConfig(boolean isSensorEnabled) {
		if(!checkIfAnyMplChannelEnabled()) {
			if(!isSensorEnabled(Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_GYRO)) {
				if(isSensorEnabled) {
					setLowPowerGyro(false);
				}
				else {
					setLowPowerGyro(true);
				}
			}
			
			if(!isSensorEnabled){
				mMPU9150AccelRange = 0; //=2g
			}
		}
		else {
			mMPU9150AccelRange = 0; //=2g
		}
	}
	
	private void setDefaultMpu9150MplSensorConfig(boolean isSensorEnabled) {
		if(isSensorEnabled){
			mMPU9150DMP = 1;
			mMPLEnable = 1;
			mMPU9150LPF = 1; // 188Hz
			mMPU9150MotCalCfg = 1; // Fast Calibration
			mMPLGyroCalTC = 1;
			mMPLVectCompCal = 1;
			mMPLMagDistCal = 1;
			mMPLSensorFusion = 0;
			
//			//Gyro rate can not be set to 250dps when DMP is on
//			if(mGyroRange==0){
//				mGyroRange=1;
//			}
			
			//force gyro range to be 2000dps and accel range to be +-2g - others untested
			mGyroRange=3; // 2000dps
			mMPU9150AccelRange= 0; // 2g
			
			setLowPowerGyro(false);
			setMPU9150MagRateFromFreq(mMaxSetShimmerSamplingRate);
			setMPU9150MplRateFromFreq(mMaxSetShimmerSamplingRate);
		}
		else {
			mMPU9150DMP = 0;
			mMPLEnable = 0;
			mMPU9150LPF = 0;
			mMPU9150MotCalCfg = 0;
			mMPLGyroCalTC = 0;
			mMPLVectCompCal = 0;
			mMPLMagDistCal = 0;
			mMPLSensorFusion = 0;
			
			if(checkIfAMpuGyroOrAccelEnabled()){
				setMPU9150GyroAccelRateFromFreq(mMaxSetShimmerSamplingRate);
			}
			else {
				setLowPowerGyro(true);
			}
			
			setMPU9150MagRateFromFreq(mMaxSetShimmerSamplingRate);
			setMPU9150MplRateFromFreq(mMaxSetShimmerSamplingRate);
		}
	}
	
	public byte[] generateCalParamGyroscope(){
		// MPU9150 Gyroscope Calibration Parameters
		byte[] bufferCalibrationParameters = new byte[21];
		// offsetVector -> buffer offset = 0
		for (int i=0; i<3; i++) {
			bufferCalibrationParameters[0+(i*2)] = (byte) ((((int)mOffsetVectorGyroscope[i][0]) >> 8) & 0xFF);
			bufferCalibrationParameters[0+(i*2)+1] = (byte) ((((int)mOffsetVectorGyroscope[i][0]) >> 0) & 0xFF);
		}
		// sensitivityMatrix -> buffer offset = 6
		for (int i=0; i<3; i++) {
			bufferCalibrationParameters[6+(i*2)] = (byte) ((((int)mSensitivityMatrixGyroscope[i][i]*100) >> 8) & 0xFF);
			bufferCalibrationParameters[6+(i*2)+1] = (byte) ((((int)mSensitivityMatrixGyroscope[i][i]*100) >> 0) & 0xFF);
		}
		// alignmentMatrix -> buffer offset = 12
		for (int i=0; i<3; i++) {
			bufferCalibrationParameters[12+(i*3)] = (byte) (((int)(mAlignmentMatrixGyroscope[i][0]*100)) & 0xFF);
			bufferCalibrationParameters[12+(i*3)+1] = (byte) (((int)(mAlignmentMatrixGyroscope[i][1]*100)) & 0xFF);
			bufferCalibrationParameters[12+(i*3)+2] = (byte) (((int)(mAlignmentMatrixGyroscope[i][2]*100)) & 0xFF);
		}
		return bufferCalibrationParameters;
	}
	
	private boolean checkIfDefaulGyroCal(double[][] offsetVectorToTest, double[][] sensitivityMatrixToTest, double[][] alignmentMatrixToTest) {
		double[][] offsetVectorToCompare = OffsetVectorGyroShimmer3;
		double[][] sensitivityVectorToCompare = mSensitivityMatrixGyroscope;
		double[][] alignmentVectorToCompare = mAlignmentMatrixGyroscope;
		
		if(getHardwareVersion()==HW_ID.SHIMMER_3){
			if (mGyroRange==0){
				sensitivityVectorToCompare = UtilShimmer.deepCopyDoubleMatrix(SensitivityMatrixGyro250dpsShimmer3);

			} else if (mGyroRange==1){
				sensitivityVectorToCompare = UtilShimmer.deepCopyDoubleMatrix(SensitivityMatrixGyro500dpsShimmer3);

			} else if (mGyroRange==2){
				sensitivityVectorToCompare = UtilShimmer.deepCopyDoubleMatrix(SensitivityMatrixGyro1000dpsShimmer3);

			} else if (mGyroRange==3){
				sensitivityVectorToCompare = UtilShimmer.deepCopyDoubleMatrix(SensitivityMatrixGyro2000dpsShimmer3);
			}
			alignmentVectorToCompare = AlignmentMatrixGyroShimmer3;
			offsetVectorToCompare = OffsetVectorGyroShimmer3;
		}
		for(int i=0;i<=2;i++){
			sensitivityVectorToCompare[i][i] = sensitivityVectorToCompare[i][i]*100;
		}
		
		boolean alignmentPass = Arrays.deepEquals(alignmentVectorToCompare, alignmentMatrixToTest);
		boolean offsetPass = Arrays.deepEquals(offsetVectorToCompare, offsetVectorToTest);
		boolean sensitivityPass = Arrays.deepEquals(sensitivityVectorToCompare, sensitivityMatrixToTest);
		
		if(alignmentPass&&offsetPass&&sensitivityPass){
			return true;
		}
		return false;
	}
	
	private void setDefaultCalibrationShimmer3Gyro() {
		mDefaultCalibrationParametersGyro = true;
		if (mGyroRange==0){
			mSensitivityMatrixGyroscope = UtilShimmer.deepCopyDoubleMatrix(SensitivityMatrixGyro250dpsShimmer3);

		} else if (mGyroRange==1){
			mSensitivityMatrixGyroscope = UtilShimmer.deepCopyDoubleMatrix(SensitivityMatrixGyro500dpsShimmer3);

		} else if (mGyroRange==2){
			mSensitivityMatrixGyroscope = UtilShimmer.deepCopyDoubleMatrix(SensitivityMatrixGyro1000dpsShimmer3);

		} else if (mGyroRange==3){
			mSensitivityMatrixGyroscope = UtilShimmer.deepCopyDoubleMatrix(SensitivityMatrixGyro2000dpsShimmer3);
		}
		mAlignmentMatrixGyroscope = UtilShimmer.deepCopyDoubleMatrix(AlignmentMatrixGyroShimmer3);
		mOffsetVectorGyroscope = UtilShimmer.deepCopyDoubleMatrix(OffsetVectorGyroShimmer3);
	}
	
	private boolean checkIfAMpuGyroOrAccelEnabled(){
		if(isSensorEnabled(Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_GYRO)) {
			return true;
		}
		if(isSensorEnabled(Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_ACCEL)) {
			return true;
		}
//		if(mSensorMap.get(SensorMapKey.SHIMMER_MPU9150_MAG) != null) {
//			if(mSensorMap.get(SensorMapKey.SHIMMER_MPU9150_MAG).mIsEnabled) {
//				return true;
//			}
//		}
		return false;
	}	
	
	private boolean checkIfAnyOtherMplChannelEnabled(int sensorMapKey){
		if (mShimmerVerObject.getHardwareVersion()==HW_ID.SHIMMER_3 
				|| mShimmerVerObject.getHardwareVersion()==HW_ID.SHIMMER_GQ_BLE) {
			if(mSensorMap.keySet().size()>0){
				
				for(int key:SensorMPU9X50.mListOfMplChannels){
//				for(int key:mListOfMplChannels){
					if(key==sensorMapKey){
						continue;
					}
					if(isSensorEnabled(key)) {
						return true;
					}
				}
			}
		}
		return false;
	}
			
	protected boolean checkIfAnyMplChannelEnabled(){
		if (mShimmerVerObject.getHardwareVersion()==HW_ID.SHIMMER_3 
				|| mShimmerVerObject.getHardwareVersion()==HW_ID.SHIMMER_GQ_BLE) {
			if(mSensorMap.keySet().size()>0){
				
				for(int key:SensorMPU9X50.mListOfMplChannels){
//					for(int key:mListOfMplChannels){
					if(isSensorEnabled(key)) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public int getGyroRange(){
		return mGyroRange;
	}
	
	/**
	 * @return the mMPU9150AccelRange
	 */
	public int getMPU9150AccelRange() {
		return mMPU9150AccelRange;
	}

	/**
	 * @return the mMPU9150GyroAccelRate
	 */
	public int getMPU9150GyroAccelRate() {
		return mMPU9150GyroAccelRate;
	}

	/**
	 * @return the mMPU9150MotCalCfg
	 */
	public int getMPU9150MotCalCfg() {
		return mMPU9150MotCalCfg;
	}
	/**
	 * @return the mMPU9150LPF
	 */
	public int getMPU9150LPF() {
		return mMPU9150LPF;
	}
	
	public int getMPU9150DMP() {
		return mMPU9150DMP;
	}

	/**
	 * @return the mMPU9150MPLSamplingRate
	 */
	public int getMPU9150MPLSamplingRate() {
		return mMPU9150MPLSamplingRate;
	}

	/**
	 * @return the mMPU9150MagSamplingRate
	 */
	public int getMPU9150MagSamplingRate() {
		return mMPU9150MagSamplingRate;
	}
	
	/**
	 * @return the mMPU9150GyroAccelRate in Hz
	 */
	public double getMPU9150GyroAccelRateInHz() {
		// Gyroscope Output Rate = 8kHz when the DLPF is disabled (DLPF_CFG = 0 or 7), and 1kHz when the DLPF is enabled
		double numerator = 1000.0;
		if(mMPU9150LPF == 0) {
			numerator = 8000.0;
		}
		
		if(mMPU9150GyroAccelRate == 0) {
			return numerator;
		}
		else {
			return (numerator / (mMPU9150GyroAccelRate + 1));
		}
	}
	
	/**
	 * @param mMPU9150AccelRange the mMPU9150AccelRange to set
	 */
	protected void setMPU9150AccelRange(int i) {
		if(checkIfAnyMplChannelEnabled()){
			i=0; // 2g
		}
		
		mMPU9150AccelRange = i;
	}
	
	protected void setMPU9150GyroRange(int i){
//		//Gyro rate can not be set to 250dps when DMP is on
//		if((checkIfAnyMplChannelEnabled()) && (i==0)){
//			i=1;
//		}
		
		if(checkIfAnyMplChannelEnabled()){
			i=3; // 2000dps
		}
		
		mGyroRange = i;
	}

	/**
	 * @param mMPU9150MPLSamplingRate the mMPU9150MPLSamplingRate to set
	 */
	protected void setMPU9150MPLSamplingRate(int mMPU9150MPLSamplingRate) {
		this.mMPU9150MPLSamplingRate = mMPU9150MPLSamplingRate;
	}

	/**
	 * @param mMPU9150MagSamplingRate the mMPU9150MagSamplingRate to set
	 */
	protected void setMPU9150MagSamplingRate(int mMPU9150MagSamplingRate) {
		this.mMPU9150MagSamplingRate = mMPU9150MagSamplingRate;
	}
	
	// MPL options
	/**
	 * @return the mMPU9150DMP
	 */
	public boolean isMPU9150DMP() {
		return (mMPU9150DMP>0)? true:false;
	}


	/**
	 * @param state the mMPU9150DMP state to set
	 */
	protected void setMPU9150DMP(boolean state) {
		if(state) 
			this.mMPU9150DMP = 0x01;
		else 
			this.mMPU9150DMP = 0x00;
	}
	
	/**
	 * @return the mMPLEnable
	 */
	public boolean isMPLEnable() {
		return (mMPLEnable>0)? true:false;
	}
	
	/**
	 * @param state the mMPLEnable state to set
	 */
	protected void setMPLEnable(boolean state) {
		if(state) 
			this.mMPLEnable = 0x01;
		else 
			this.mMPLEnable = 0x00;
	}

	/**
	 * @return the mMPLSensorFusion
	 */
	public boolean isMPLSensorFusion() {
		return (mMPLSensorFusion>0)? true:false;
	}

	/**
	 * @param state the mMPLSensorFusion state to set
	 */
	protected void setMPLSensorFusion(boolean state) {
		if(state) 
			this.mMPLSensorFusion = 0x01;
		else 
			this.mMPLSensorFusion = 0x00;
	}

	/**
	 * @return the mMPLGyroCalTC
	 */
	public boolean isMPLGyroCalTC() {
		return (mMPLGyroCalTC>0)? true:false;
	}
	
	/**
	 * @param state the mMPLGyroCalTC state to set
	 */
	protected void setMPLGyroCalTC(boolean state) {
		if(state) 
			this.mMPLGyroCalTC = 0x01;
		else 
			this.mMPLGyroCalTC = 0x00;
	}

	/**
	 * @return the mMPLVectCompCal
	 */
	public boolean isMPLVectCompCal() {
		return (mMPLVectCompCal>0)? true:false;
	}

	/**
	 * @param state the mMPLVectCompCal state to set
	 */
	protected void setMPLVectCompCal(boolean state) {
		if(state) 
			this.mMPLVectCompCal = 0x01;
		else 
			this.mMPLVectCompCal = 0x00;
	}

	/**
	 * @return the mMPLMagDistCal
	 */
	public boolean isMPLMagDistCal() {
		return (mMPLMagDistCal>0)? true:false;
	}
	
	/**
	 * @param state the mMPLMagDistCal state to set
	 */
	protected void setMPLMagDistCal(boolean state) {
		if(state) 
			this.mMPLMagDistCal = 0x01;
		else 
			this.mMPLMagDistCal = 0x00;
	}
	
	/**
	 * @return the mMPLSensorFusion
	 */
	public boolean getmMPLSensorFusion() {
		return (mMPLSensorFusion>0)? true:false;
	}

	/**
	 * @param state the mMPLSensorFusion state to set
	 */
	protected void setmMPLSensorFusion(boolean state) {
		if(state) 
			this.mMPLSensorFusion = 0x01;
		else 
			this.mMPLSensorFusion = 0x00;
	}


	/**
	 * @param mMPU9150MotCalCfg the mMPU9150MotCalCfg to set
	 */
	protected void setMPU9150MotCalCfg(int mMPU9150MotCalCfg) {
		this.mMPU9150MotCalCfg = mMPU9150MotCalCfg;
	}

	/**
	 * @param mMPU9150LPF the mMPU9150LPF to set
	 */
	protected void setMPU9150LPF(int mMPU9150LPF) {
		this.mMPU9150LPF = mMPU9150LPF;
	}

	// ----------- MPU9X50 options end -------------------------

	
	// ----------- extra MPU9X50 options copied from ShimmerObject -------------------------

	/**
	 * This enables the low-power gyro option. When not enabled the sampling
	 * rate of the gyro is set to the closest supported value to the actual
	 * sampling rate that it can achieve. For the Shimmer2, in low power mode it
	 * defaults to 10Hz.
	 * 
	 * @param enable
	 */
	protected void setLowPowerGyro(boolean enable){
		if(mShimmerVerObject.getHardwareVersion()==HW_ID.SHIMMER_3 || mShimmerVerObject.getHardwareVersion()==HW_ID.SHIMMER_4_SDK){
			if(!checkIfAnyMplChannelEnabled()) {
				mLowPowerGyro = enable;
				setMPU9150GyroAccelRateFromFreq(mMaxSetShimmerSamplingRate);
			}
			else{
				mLowPowerGyro = false;
				setMPU9150GyroAccelRateFromFreq(mMaxSetShimmerSamplingRate);
			}
		}
	}
	
	public int getLowPowerGyroEnabled() {
		if(mLowPowerGyro)
			return 1;
		else
			return 0;
	}

	//TODO TODO
	public void setSamplingRateShimmer(double samplingRate){
//		//In Shimmer3 the SD and BT have the same sampling rate 
//		setSamplingRateShimmer(COMMUNICATION_TYPE.SD, samplingRate);
//		setSamplingRateShimmer(COMMUNICATION_TYPE.BLUETOOTH, samplingRate);
	}


	@Override
	public boolean checkConfigOptionValues(String stringKey) {
		// TODO Auto-generated method stub
		return false;
		
	}
	
	public boolean checkLowPowerGyro() {
		if(mMPU9150GyroAccelRate == 0xFF) {
			mLowPowerGyro = true;
		}
		else {
			mLowPowerGyro = false;
		}
		return mLowPowerGyro;
	}

	public void retrieveKinematicCalibrationParametersFromPacket(byte[] bufferCalibrationParameters, int packetType) {
		
			String[] dataType={"i16","i16","i16","i16","i16","i16","i8","i8","i8","i8","i8","i8","i8","i8","i8"}; 
			int[] formattedPacket = UtilParseData.formatDataPacketReverse(bufferCalibrationParameters,dataType); // using the datatype the calibration parameters are converted
			double[] AM=new double[9];
			for (int i=0;i<9;i++){
				AM[i]=((double)formattedPacket[6+i])/100;
			}

			double[][] AlignmentMatrix = {{AM[0],AM[1],AM[2]},{AM[3],AM[4],AM[5]},{AM[6],AM[7],AM[8]}}; 				
			double[][] SensitivityMatrix = {{formattedPacket[3],0,0},{0,formattedPacket[4],0},{0,0,formattedPacket[5]}}; 
			double[][] OffsetVector = {{formattedPacket[0]},{formattedPacket[1]},{formattedPacket[2]}};
			
			if(packetType==GYRO_CALIBRATION_RESPONSE && checkIfDefaulGyroCal(OffsetVector, SensitivityMatrix, AlignmentMatrix)){
				mDefaultCalibrationParametersGyro = true;
				mAlignmentMatrixGyroscope = AlignmentMatrix;
				mOffsetVectorGyroscope = OffsetVector;
				mSensitivityMatrixGyroscope = SensitivityMatrix;
				for(int i=0;i<=2;i++){
					mSensitivityMatrixGyroscope[i][i] = mSensitivityMatrixGyroscope[i][i]/100;
				}
			}
			else if (packetType==GYRO_CALIBRATION_RESPONSE && SensitivityMatrix[0][0]!=-1) {
				mDefaultCalibrationParametersGyro = false;
				mAlignmentMatrixGyroscope = AlignmentMatrix;
				mOffsetVectorGyroscope = OffsetVector;
				mSensitivityMatrixGyroscope = SensitivityMatrix;
				for(int i=0;i<=2;i++){
					mSensitivityMatrixGyroscope[i][i] = mSensitivityMatrixGyroscope[i][i]/100;
				}
			} 
			else if(packetType==GYRO_CALIBRATION_RESPONSE && SensitivityMatrix[0][0]==-1){
				if(getHardwareVersion()!=HW_ID.SHIMMER_3
						&&getHardwareVersion()!=HW_ID.SHIMMER_4_SDK){
					mDefaultCalibrationParametersGyro = true;
					mAlignmentMatrixGyroscope = AlignmentMatrixGyroShimmer2;
					mOffsetVectorGyroscope = OffsetVectorGyroShimmer2;
					mSensitivityMatrixGyroscope = SensitivityMatrixGyroShimmer2;	
				} 
				else {
					setDefaultCalibrationShimmer3Gyro();
				}
			} 
			updateCalibMapGyro();
	}
	
	public boolean isUsingDefaultGyroParam(){
		return mDefaultCalibrationParametersGyro;
	}


	@Override
	public void processResponse(Object obj, COMMUNICATION_TYPE commType) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void checkShimmerConfigBeforeConfiguring() {
		// If Shimmer name is default, update with MAC ID if available.
//		if(ShimmerDevice.mShimmerUserAssignedName.equals(ShimmerDevice.DEFAULT_SHIMMER_NAME)){
//			setDefaultShimmerName();
//			}
		if(!isSensorEnabled(Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_GYRO)){
			setDefaultMpu9150GyroSensorConfig(false);
		}
		if(!isSensorEnabled(Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_ACCEL)){
			setDefaultMpu9150AccelSensorConfig(false);
		}
		if(!isSensorEnabled(Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_MAG)){
//			setMPU9150MagRateFromFreq(getSamplingRateShimmer());
			setMPU9150MagRateFromFreq(mMaxSetShimmerSamplingRate);
		}
		if(!checkIfAnyMplChannelEnabled()) {
			setDefaultMpu9150MplSensorConfig(false);
		}
	}
	
	//--------- Optional methods to override in Sensor Class start --------	
	/* (non-Javadoc)
	 * @see com.shimmerresearch.sensors.AbstractSensor#isSensorUsingDefaultCal(int)
	 */
	@Override
	public boolean isSensorUsingDefaultCal(int sensorMapKey) {
		if(sensorMapKey==Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_GYRO){
			return isUsingDefaultGyroParam();
		}
		return false;
	}
	
	private void setKinematicCalibration(TreeMap<Integer, CalibDetailsKinematic> mapOfKinematicSensorCalibration) {
		mCalibMapGyroShimmer3.putAll(mapOfKinematicSensorCalibration);
	}
	
	private TreeMap<Integer, TreeMap<Integer, CalibDetailsKinematic>> getKinematicCalibration() {
		TreeMap<Integer, TreeMap<Integer, CalibDetailsKinematic>> mapOfKinematicSensorCalibration = new TreeMap<Integer, TreeMap<Integer, CalibDetailsKinematic>>();
		mapOfKinematicSensorCalibration.put(Shimmer3.SensorMapKey.SHIMMER_MPU9150_GYRO, mCalibMapGyroShimmer3);
		return mapOfKinematicSensorCalibration;
	}
	
	private void updateCalibMapGyro() {
		int rangeValue = getGyroRange();
		CalibDetailsKinematic calDetails = mCalibMapGyroShimmer3.get(rangeValue);
		if(calDetails==null){
			String rangeString = getSensorRangeFromConfigValue(Shimmer3.ListofMPU9150GyroRangeConfigValues, Shimmer3.ListofGyroRange, rangeValue);
			calDetails = new CalibDetailsKinematic(rangeValue, rangeString);
		}
		calDetails.setCurrentValues(mAlignmentMatrixGyroscope, mSensitivityMatrixGyroscope, mOffsetVectorGyroscope);
		mCalibMapGyroShimmer3.put(rangeValue, calDetails);
	}
	
	public static String getSensorRangeFromConfigValue(Integer[] listOfConfigValues, String[] listOfConfigValueStrings, Integer configValueToFind){
		int index = Arrays.asList(listOfConfigValues).indexOf(configValueToFind);
		if(index>=0 && listOfConfigValueStrings.length>index){
			return listOfConfigValueStrings[index];
		}
		return "?";
	}

	//--------- Optional methods to override in Sensor Class end -------- 


}
	

