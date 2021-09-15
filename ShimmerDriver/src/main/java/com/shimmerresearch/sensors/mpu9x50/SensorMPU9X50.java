package com.shimmerresearch.sensors.mpu9x50;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.ArrayUtils;

import com.shimmerresearch.bluetooth.BtCommandDetails;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driver.Configuration.Shimmer3.CompatibilityInfoForMaps;
import com.shimmerresearch.driver.calibration.CalibDetails;
import com.shimmerresearch.driver.calibration.CalibDetailsKinematic;
import com.shimmerresearch.driver.calibration.UtilCalibration;
import com.shimmerresearch.driver.calibration.CalibDetails.CALIB_READ_SOURCE;
import com.shimmerresearch.driver.calibration.CalibDetailsKinematic.CALIBRATION_SCALE_FACTOR;
import com.shimmerresearch.driver.shimmer2r3.ConfigByteLayoutShimmer3;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driverUtilities.ChannelDetails;
import com.shimmerresearch.driverUtilities.SensorDetails;
import com.shimmerresearch.driverUtilities.SensorDetailsRef;
import com.shimmerresearch.driverUtilities.UtilParseData;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_TYPE;
import com.shimmerresearch.sensors.AbstractSensor;
import com.shimmerresearch.sensors.ActionSetting;
import com.shimmerresearch.driver.ConfigByteLayout;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.FormatCluster;
import com.shimmerresearch.driver.ShimmerDevice;


/**
 * @author Ronan McCormack
 * @author Mark Nolan
 */
public abstract class SensorMPU9X50 extends AbstractSensor implements Serializable {

	/** * */
	private static final long serialVersionUID = -1137540822708521997L;
	
	protected int mSensorIdAccel = -1;
	protected int mSensorIdMag = -1;
	protected int mSensorIdGyro = -1;

	//--------- Sensor specific variables start --------------
	private boolean debugGyroRate = false;

	/** This stores the current Gyro Range, it is a value between 0 and 3; 0 = +/- 250dps,1 = 500dps, 2 = 1000dps, 3 = 2000dps */
	private int mGyroRange = 1;													 
	protected boolean mLowPowerGyro = false;
	
	public boolean mIsUsingDefaultGyroParam = true;

//	protected int mMPU9X50GyroRate = 0;

	protected int mMPU9X50AccelRange=0;											// This stores the current MPU9150 Accel Range. 0 = 2g, 1 = 4g, 2 = 8g, 4 = 16g
	protected int mMPU9X50GyroAccelRate=0;
//	protected int mMagRange=1;
	
	protected int mMPU9X50DMP = 0;
	protected int mMPU9X50LPF = 0;
	protected int mMPU9X50MotCalCfg = 0;
	protected int mMPU9X50MPLSamplingRate = 0;
	protected int mMPU9X50MagSamplingRate = 0;
	protected int mMPLSensorFusion = 0;
	protected int mMPLGyroCalTC = 0;
	protected int mMPLVectCompCal = 0;
	protected int mMPLMagDistCal = 0;
	protected int mMPLEnable = 0;
	
	protected double[][] AlignmentMatrixMPLAccel = {{-1,0,0},{0,1,0},{0,0,-1}}; 			
	protected double[][] SensitivityMatrixMPLAccel = {{1631,0,0},{0,1631,0},{0,0,1631}}; 	
	protected double[][] OffsetVectorMPLAccel = {{0},{0},{0}};
	
	private CalibDetailsKinematic calibDetailsMplAccel = new CalibDetailsKinematic(
			0,
			"0",
			AlignmentMatrixMPLAccel, 
			SensitivityMatrixMPLAccel, 
			OffsetVectorMPLAccel,
			CALIBRATION_SCALE_FACTOR.ONE_HUNDRED);
	
	protected double[][] AlignmentMatrixMPLMag = {{-1,0,0},{0,1,0},{0,0,-1}}; 			
	protected double[][] SensitivityMatrixMPLMag = {{1631,0,0},{0,1631,0},{0,0,1631}}; 	
	protected double[][] OffsetVectorMPLMag = {{0},{0},{0}};
	
	private CalibDetailsKinematic calibDetailsMplMag = new CalibDetailsKinematic(
			0,
			"0",
			AlignmentMatrixMPLMag, 
			SensitivityMatrixMPLMag, 
			OffsetVectorMPLMag,
			CALIBRATION_SCALE_FACTOR.ONE_HUNDRED);
	
	protected double[][] AlignmentMatrixMPLGyro = {{-1,0,0},{0,1,0},{0,0,-1}}; 			
	protected double[][] SensitivityMatrixMPLGyro = {{1631,0,0},{0,1631,0},{0,0,1631}}; 	
	protected double[][] OffsetVectorMPLGyro = {{0},{0},{0}};

	private CalibDetailsKinematic calibDetailsMplGyro = new CalibDetailsKinematic(
			0,
			"0",
			AlignmentMatrixMPLGyro, 
			SensitivityMatrixMPLGyro, 
			OffsetVectorMPLGyro,
			CALIBRATION_SCALE_FACTOR.ONE_HUNDRED);
	
	//MPU Mag (AK8975A) - obtained from: 
	// http://www.akm.com/akm/en/file/datasheet/AK8975.pdf
	// https://github.com/kriswiner/MPU-9150/blob/master/MPU9150BasicAHRS.ino
	public double[][] mAlignmentMatrixMagnetometer = AlignmentMatrixMPLMag; 				
	public double[][] mSensitivityMatrixMagnetometer = {{0.3,0,0},{0,0.3,0},{0,0,0.3}}; 		
	public double[][] mOffsetVectorMagnetometer = {{-5.0},{-95.0},{-260.0}};

	// ----------   Gyro start ---------------

	//Shimmer3
	public static final double[][] AlignmentMatrixGyroShimmer3 = {{0,-1,0},{-1,0,0},{0,0,-1}}; 				
	public static final double[][] OffsetVectorGyroShimmer3 = {{0},{0},{0}};	
	public static final double[][] SensitivityMatrixGyro250dpsShimmer3 = {{131,0,0},{0,131,0},{0,0,131}};
	public static final double[][] SensitivityMatrixGyro500dpsShimmer3 = {{65.5,0,0},{0,65.5,0},{0,0,65.5}};
	public static final double[][] SensitivityMatrixGyro1000dpsShimmer3 = {{32.8,0,0},{0,32.8,0},{0,0,32.8}};
	public static final double[][] SensitivityMatrixGyro2000dpsShimmer3 = {{16.4,0,0},{0,16.4,0},{0,0,16.4}};
		
	
	private CalibDetailsKinematic calibDetailsGyro250 = new CalibDetailsKinematic(
			ListofMPU9X50GyroRangeConfigValues[0], 
			ListofGyroRange[0],
			AlignmentMatrixGyroShimmer3,
			SensitivityMatrixGyro250dpsShimmer3,
			OffsetVectorGyroShimmer3,
			CALIBRATION_SCALE_FACTOR.ONE_HUNDRED);
	private CalibDetailsKinematic calibDetailsGyro500 = new CalibDetailsKinematic(
			ListofMPU9X50GyroRangeConfigValues[1], 
			ListofGyroRange[1],
			AlignmentMatrixGyroShimmer3, 
			SensitivityMatrixGyro500dpsShimmer3,
			OffsetVectorGyroShimmer3,
			CALIBRATION_SCALE_FACTOR.ONE_HUNDRED);
	private CalibDetailsKinematic calibDetailsGyro1000 = new CalibDetailsKinematic(
			ListofMPU9X50GyroRangeConfigValues[2], 
			ListofGyroRange[2],
			AlignmentMatrixGyroShimmer3, 
			SensitivityMatrixGyro1000dpsShimmer3, 
			OffsetVectorGyroShimmer3,
			CALIBRATION_SCALE_FACTOR.ONE_HUNDRED);
	private CalibDetailsKinematic calibDetailsGyro2000 = new CalibDetailsKinematic(
			ListofMPU9X50GyroRangeConfigValues[3],
			ListofGyroRange[3],
			AlignmentMatrixGyroShimmer3, 
			SensitivityMatrixGyro2000dpsShimmer3, 
			OffsetVectorGyroShimmer3,
			CALIBRATION_SCALE_FACTOR.ONE_HUNDRED);
	
	public CalibDetailsKinematic mCurrentCalibDetailsGyro = calibDetailsGyro250;
	
//	protected OnTheFlyGyroOffsetCal mOnTheFlyGyroOffsetCal = new OnTheFlyGyroOffsetCal(); 

	// ----------   Gyro end ---------------

//	public boolean mEnableXCalibration = true;
//	public byte[] mInquiryResponseXBytes;
	
//	public byte[] mGyroCalRawXParams = new byte[22];
//	public byte[] mMagCalRawXParams = new byte[22];
	
	
//	------------ Keep in Configuration? ------------
//	public class SENSOR_ID{
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
		public static final String MPU9X50_GYRO_RANGE = "Gyro Range";
		public static final String MPU9X50_GYRO_RATE = "Gyro Sampling Rate";
		public static final String MPU9X50_GYRO_RATE_HZ = "Gyro Sampling Rate Hertz";
		
	    public static final String MPU9X50_ACCEL_RANGE = "MPU Accel Range";
		public static final String MPU9X50_DMP_GYRO_CAL = "MPU Gyro Cal";
		public static final String MPU9X50_MPL_LPF = "MPU LPF";
		public static final String MPU9X50_MPL_RATE = "MPL Rate";
		public static final String MPU9X50_MAG_RATE = "MPU Mag Rate";

		public static final String MPU9X50_DMP = "DMP";
		public static final String MPU9X50_MPL = "MPL";
		public static final String MPU9X50_MPL_9DOF_SENSOR_FUSION = "9DOF Sensor Fusion";
		public static final String MPU9X50_MPL_GYRO_CAL = "Gyro Calibration";
		public static final String MPU9X50_MPL_VECTOR_CAL = "Vector Compensation Calibration";
		public static final String MPU9X50_MPL_MAG_CAL = "Magnetic Disturbance Calibration";

		public static final String MPU9X50_GYRO_LPM = "Gyro Low-Power Mode";
		public static final String MPU9X50_GYRO_DEFAULT_CALIB = "Gyro Default Calibration";
		
		public static final String MPU9X50_GYRO_VALID_CALIB = "Gyro Valid Calibration";
		public static final String MPU9X50_GYRO_CALIB_PARAM = "Gyro Calibration Details";
		
//		public static final String GYRO_ON_THE_FLY_CALIB_STATE = "On-the-fly_Calib";
//		public static final String GYRO_ON_THE_FLY_CALIB_THRESHOLD = "On-the-fly_Calib_Threshold";
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
	
	public class LABEL_SENSOR_TILE{
		public static final String MPU = "Kinematics";
		public static final String GYRO = GuiLabelSensors.GYRO;
		public static final String MPU_ACCEL_GYRO_MAG = "MPU 9DoF";
		public static final String MPU_OTHER = "MPU Other";
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
	
//	public static final class DatabaseConfigHandle{
//		public static final String GYRO_ON_THE_FLY_CALIB_STATE = "Gyro_on_the_fly_offset_cal_state";
//		public static final String GYRO_ON_THE_FLY_CALIB_THRESHOLD = "Gyro_on_the_fly_offset_cal_threshold";
//	}
	
	//--------- Algorithm specific variables end --------------
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
	    
	public static final String[] ListofGyroRange = {"+/- 250dps","+/- 500dps","+/- 1000dps","+/- 2000dps"};
	public static final Integer[] ListofMPU9X50GyroRangeConfigValues = {0,1,2,3};
	
	public static final String[] ListofMPU9X50AccelRange = {"+/- 2g","+/- 4g","+/- 8g","+/- 16g"};
	public static final Integer[] ListofMPU9X50AccelRangeConfigValues = {0,1,2,3};
	public static final String[] ListofMPU9X50MagRate = {"10.0Hz","20.0Hz","40.0Hz","50.0Hz","100.0Hz"};
	public static final Integer[] ListofMPU9X50MagRateConfigValues = {0,1,2,3,4};

	public static final String[] ListofMPU9150MplCalibrationOptions = {"No Cal","Fast Cal","1s no motion","2s no motion","5s no motion","10s no motion","30s no motion","60s no motion"};
	public static final Integer[] ListofMPU9150MplCalibrationOptionsConfigValues = {0,1,2,3,4,5,6,7};
	public static final String[] ListofMPU9150MplLpfOptions = {"No LPF","188.0Hz","98.0Hz","42.0Hz","20.0Hz","10.0Hz","5.0Hz"};
	public static final Integer[] ListofMPU9150MplLpfOptionsConfigValues = {0,1,2,3,4,5,6};
	public static final String[] ListofMPU9150MplRate = {"10.0Hz","20.0Hz","40.0Hz","50.0Hz","100.0Hz"};
	public static final Integer[] ListofMPU9150MplRateConfigValues = {0,1,2,3,4};
	
	public static final List<Integer> mListOfMplChannels = Arrays.asList(
			Configuration.Shimmer3.SENSOR_ID.SHIMMER_MPU9X50_TEMP,
			Configuration.Shimmer3.SENSOR_ID.SHIMMER_MPU9X50_MPL_QUAT_6DOF,
			Configuration.Shimmer3.SENSOR_ID.SHIMMER_MPU9X50_MPL_QUAT_9DOF,
			Configuration.Shimmer3.SENSOR_ID.SHIMMER_MPU9X50_MPL_EULER_6DOF,
			Configuration.Shimmer3.SENSOR_ID.SHIMMER_MPU9X50_MPL_EULER_9DOF,
			Configuration.Shimmer3.SENSOR_ID.SHIMMER_MPU9X50_MPL_HEADING,
			Configuration.Shimmer3.SENSOR_ID.SHIMMER_MPU9X50_MPL_PEDOMETER,
			Configuration.Shimmer3.SENSOR_ID.SHIMMER_MPU9X50_MPL_TAP,
			Configuration.Shimmer3.SENSOR_ID.SHIMMER_MPU9X50_MPL_MOTION_ORIENT,
			Configuration.Shimmer3.SENSOR_ID.SHIMMER_MPU9X50_MPL_GYRO,
			Configuration.Shimmer3.SENSOR_ID.SHIMMER_MPU9X50_MPL_ACCEL,
			Configuration.Shimmer3.SENSOR_ID.SHIMMER_MPU9X50_MPL_MAG,
			Configuration.Shimmer3.SENSOR_ID.SHIMMER_MPU9X50_MPL_QUAT_6DOF_RAW);

	
//	public static final ConfigOptionDetailsSensor configOptionGyroOnTheFlyCalibState = new ConfigOptionDetailsSensor(
//			SensorMPU9X50.GuiLabelConfig.GYRO_ON_THE_FLY_CALIB_STATE,
//			SensorMPU9X50.DatabaseConfigHandle.GYRO_ON_THE_FLY_CALIB_STATE,
//			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.CHECKBOX,
//			CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW);
//
//	public static final ConfigOptionDetailsSensor configOptionGyroOnTheFlyCalibThreshold = new ConfigOptionDetailsSensor(
//			SensorMPU9X50.GuiLabelConfig.GYRO_ON_THE_FLY_CALIB_THRESHOLD,
//			SensorMPU9X50.DatabaseConfigHandle.GYRO_ON_THE_FLY_CALIB_THRESHOLD,
//			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.CHECKBOX,
//			CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW);

	//--------- Configuration options end --------------

	//--------- Sensor info start --------------
	//TODO fill in all conflicting sensors for each sensor listed below -> not all were done in Configuration.Shimmer3
	//TODO should MPU9150_MPL_RATE be in all mListOfConfigOptionKeysAssociated??

   // ------------ Check byte index for Temp----------------
//	public static final SensorDetailsRef sensorMpu9150TempRef = new SensorDetailsRef(0x02<<(streamingByteIndex*8), 0x02<<(logHeaderByteIndex*8), Shimmer3.GuiLabelSensors.MPL_TEMPERATURE));
	public static final SensorDetailsRef sensorMpu9150TempRef = new SensorDetailsRef(0x02<<(2*8), 0x02<<(2*8), GuiLabelSensors.MPL_TEMPERATURE,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors,
			null, 
			Arrays.asList(GuiLabelConfig.MPU9X50_MPL_RATE),
			Arrays.asList(ObjectClusterSensorName.MPL_TEMPERATURE),
			false);

	
	public static final SensorDetailsRef sensorMpu9150MplQuat6Dof = new SensorDetailsRef((long)0, (long)0x80<<(3*8), GuiLabelSensors.QUAT_MPL_6DOF,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors,
			null,//Arrays.asList(Configuration.Shimmer3.SENSOR_ID.SHIMMER_MPU9150_MPL_QUAT_6DOF),
			Arrays.asList(GuiLabelConfig.MPU9X50_MPL_RATE),
			Arrays.asList(
					ObjectClusterSensorName.QUAT_MPL_6DOF_W,
					ObjectClusterSensorName.QUAT_MPL_6DOF_X,
					ObjectClusterSensorName.QUAT_MPL_6DOF_Y,
					ObjectClusterSensorName.QUAT_MPL_6DOF_Z),
			false);
	
	
	public static final SensorDetailsRef sensorMpu9150MplQuat9Dof = new SensorDetailsRef((long)0, (long)0x40<<(3*8), GuiLabelSensors.QUAT_MPL_9DOF,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors,
			null,//Arrays.asList(Configuration.Shimmer3.SENSOR_ID.SHIMMER_MPU9150_MPL_QUAT_9DOF),
			Arrays.asList(GuiLabelConfig.MPU9X50_MPL_RATE),
			Arrays.asList(
					ObjectClusterSensorName.QUAT_MPL_9DOF_W,
					ObjectClusterSensorName.QUAT_MPL_9DOF_X,
					ObjectClusterSensorName.QUAT_MPL_9DOF_Y,
					ObjectClusterSensorName.QUAT_MPL_9DOF_Z),
			false);
	
	public static final SensorDetailsRef sensorMpu9150MplEuler6Dof = new SensorDetailsRef((long)0, (long)0x20<<(3*8), GuiLabelSensors.EULER_ANGLES_6DOF,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors,
			null,//Arrays.asList(Configuration.Shimmer3.SENSOR_ID.SHIMMER_MPU9150_MPL_EULER_6DOF),
			Arrays.asList(GuiLabelConfig.MPU9X50_MPL_RATE),
			Arrays.asList(
					ObjectClusterSensorName.EULER_MPL_6DOF_X,
					ObjectClusterSensorName.EULER_MPL_6DOF_Y,
					ObjectClusterSensorName.EULER_MPL_6DOF_Z),
			false);
	
	public static final SensorDetailsRef sensorMpu9150MplEuler9Dof = new SensorDetailsRef((long)0, (long)0x10<<(3*8), GuiLabelSensors.EULER_ANGLES_9DOF,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors,
			null,//Arrays.asList(Configuration.Shimmer3.SENSOR_ID.SHIMMER_MPU9150_MPL_EULER_9DOF),
			Arrays.asList(GuiLabelConfig.MPU9X50_MPL_RATE),
			Arrays.asList(
					ObjectClusterSensorName.EULER_MPL_9DOF_X,
					ObjectClusterSensorName.EULER_MPL_9DOF_Y,
					ObjectClusterSensorName.EULER_MPL_9DOF_Z),
			false);
	
	public static final SensorDetailsRef sensorMpu9150MplHeading = new SensorDetailsRef((long)0, (long)0x08<<(3*8), GuiLabelSensors.MPL_HEADING,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors,
			null,
			Arrays.asList(GuiLabelConfig.MPU9X50_MPL_RATE),
			Arrays.asList(
					ObjectClusterSensorName.MPL_HEADING),
			false);
	
	public static final SensorDetailsRef sensorMpu9150MplPedometer = new SensorDetailsRef((long)0, (long)0x04<<(3*8), GuiLabelSensors.MPL_PEDOM_CNT,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors,
			null,//Arrays.asList(Configuration.Shimmer3.SENSOR_ID.SHIMMER_MPU9150_MPL_PEDOMETER),
			Arrays.asList(GuiLabelConfig.MPU9X50_MPL_RATE),
			Arrays.asList(
					ObjectClusterSensorName.MPL_PEDOM_CNT,
					ObjectClusterSensorName.MPL_PEDOM_TIME),
			false);
	
	public static final SensorDetailsRef sensorMpu9150MplTap = new SensorDetailsRef((long)0, (long)0x02<<(3*8), GuiLabelSensors.MPL_TAPDIRANDTAPCNT,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors,
			null,//Arrays.asList(Configuration.Shimmer3.SENSOR_ID.SHIMMER_MPU9150_MPL_TAP),
			Arrays.asList(GuiLabelConfig.MPU9X50_MPL_RATE),
			Arrays.asList(
					ObjectClusterSensorName.TAPDIR,
					ObjectClusterSensorName.TAPCNT),
			false);
	
	public static final SensorDetailsRef sensorMpu9150MplMotion = new SensorDetailsRef((long)0, (long)0x01<<(3*8), GuiLabelSensors.MPL_MOTIONANDORIENT,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors,
			null,//Arrays.asList(Configuration.Shimmer3.SENSOR_ID.SHIMMER_MPU9150_MPL_MOTION_ORIENT),
			Arrays.asList(GuiLabelConfig.MPU9X50_MPL_RATE),
			Arrays.asList(
					ObjectClusterSensorName.MOTION,
					ObjectClusterSensorName.ORIENT),
			false);

	//MPL calibrated sensors
	public static final SensorDetailsRef sensorMpu9150MplGyro = new SensorDetailsRef((long)0, (long)0x80<<(4*8), GuiLabelSensors.GYRO_MPU_MPL,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors,
			Arrays.asList(Configuration.Shimmer3.SENSOR_ID.SHIMMER_MPU9X50_MPL_GYRO),
			Arrays.asList(
					GuiLabelConfig.MPU9X50_GYRO_RANGE,
					GuiLabelConfig.MPU9X50_GYRO_RATE,
					GuiLabelConfig.MPU9X50_MPL_LPF,
					GuiLabelConfig.MPU9X50_MPL_GYRO_CAL),
			Arrays.asList(
					ObjectClusterSensorName.GYRO_MPU_MPL_X,
					ObjectClusterSensorName.GYRO_MPU_MPL_Y,
					ObjectClusterSensorName.GYRO_MPU_MPL_Z),
			false);

	public static final SensorDetailsRef sensorMpu9150MplAccel = new SensorDetailsRef((long)0, (long)0x40<<(4*8), GuiLabelSensors.ACCEL_MPU_MPL,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors,
			Arrays.asList(Configuration.Shimmer3.SENSOR_ID.SHIMMER_MPU9X50_MPL_ACCEL),
			Arrays.asList(
					GuiLabelConfig.MPU9X50_ACCEL_RANGE,
					GuiLabelConfig.MPU9X50_MPL_LPF),
			Arrays.asList(
					ObjectClusterSensorName.ACCEL_MPU_MPL_X,
					ObjectClusterSensorName.ACCEL_MPU_MPL_Y,
					ObjectClusterSensorName.ACCEL_MPU_MPL_Z),
			false);
	
	public static final SensorDetailsRef sensorMpu9150MplMag = new SensorDetailsRef((long)0, (long)0x20<<(4*8), GuiLabelSensors.MAG_MPU_MPL,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors,
			Arrays.asList(Configuration.Shimmer3.SENSOR_ID.SHIMMER_MPU9X50_MPL_MAG),
			Arrays.asList(
					GuiLabelConfig.MPU9X50_MPL_LPF),
			Arrays.asList(
					ObjectClusterSensorName.MAG_MPU_MPL_X,
					ObjectClusterSensorName.MAG_MPU_MPL_Y,
					ObjectClusterSensorName.MAG_MPU_MPL_Z),
			false);
	
	public static final SensorDetailsRef sensorMpu9150MplQuat6DofRaw = new SensorDetailsRef((long)0, (long)0x10<<(4*8), GuiLabelSensors.QUAT_DMP_6DOF,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors,
			null,//Arrays.asList(Configuration.Shimmer3.SENSOR_ID.SHIMMER_MPU9150_MPL_QUAT_6DOF_RAW),
			Arrays.asList(GuiLabelConfig.MPU9X50_MPL_RATE),
			Arrays.asList(
					ObjectClusterSensorName.QUAT_DMP_6DOF_W,
					ObjectClusterSensorName.QUAT_DMP_6DOF_X,
					ObjectClusterSensorName.QUAT_DMP_6DOF_Y,
					ObjectClusterSensorName.QUAT_DMP_6DOF_Z),
			false);

	
    public static final Map<Integer, SensorDetailsRef> mSensorMapRefCommon;
    static {
        Map<Integer, SensorDetailsRef> aMap = new LinkedHashMap<Integer, SensorDetailsRef>();
		//TODO decide what to do with below -> update, I can't remember why I added this message
		aMap.put(Configuration.Shimmer3.SENSOR_ID.SHIMMER_MPU9X50_TEMP, SensorMPU9X50.sensorMpu9150TempRef);

		aMap.put(Configuration.Shimmer3.SENSOR_ID.SHIMMER_MPU9X50_MPL_QUAT_6DOF, SensorMPU9X50.sensorMpu9150MplQuat6Dof);
		aMap.put(Configuration.Shimmer3.SENSOR_ID.SHIMMER_MPU9X50_MPL_QUAT_9DOF, SensorMPU9X50.sensorMpu9150MplQuat9Dof);
		aMap.put(Configuration.Shimmer3.SENSOR_ID.SHIMMER_MPU9X50_MPL_EULER_6DOF, SensorMPU9X50.sensorMpu9150MplEuler6Dof);
		aMap.put(Configuration.Shimmer3.SENSOR_ID.SHIMMER_MPU9X50_MPL_EULER_9DOF, SensorMPU9X50.sensorMpu9150MplEuler9Dof);
		aMap.put(Configuration.Shimmer3.SENSOR_ID.SHIMMER_MPU9X50_MPL_HEADING, SensorMPU9X50.sensorMpu9150MplHeading);
		aMap.put(Configuration.Shimmer3.SENSOR_ID.SHIMMER_MPU9X50_MPL_PEDOMETER, SensorMPU9X50.sensorMpu9150MplPedometer);
		aMap.put(Configuration.Shimmer3.SENSOR_ID.SHIMMER_MPU9X50_MPL_TAP, SensorMPU9X50.sensorMpu9150MplTap);
		aMap.put(Configuration.Shimmer3.SENSOR_ID.SHIMMER_MPU9X50_MPL_MOTION_ORIENT, SensorMPU9X50.sensorMpu9150MplMotion);
		aMap.put(Configuration.Shimmer3.SENSOR_ID.SHIMMER_MPU9X50_MPL_GYRO, SensorMPU9X50.sensorMpu9150MplGyro);
		aMap.put(Configuration.Shimmer3.SENSOR_ID.SHIMMER_MPU9X50_MPL_ACCEL, SensorMPU9X50.sensorMpu9150MplAccel);
		aMap.put(Configuration.Shimmer3.SENSOR_ID.SHIMMER_MPU9X50_MPL_MAG, SensorMPU9X50.sensorMpu9150MplMag);
		aMap.put(Configuration.Shimmer3.SENSOR_ID.SHIMMER_MPU9X50_MPL_QUAT_6DOF_RAW, SensorMPU9X50.sensorMpu9150MplQuat6DofRaw);

		mSensorMapRefCommon = Collections.unmodifiableMap(aMap);
    }
	//--------- Sensor info end --------------
    
	//--------- Abstract methods implemented start --------------
	
    //--------- Constructors for this class start --------------
	public SensorMPU9X50(SENSORS sensorType) {
		super(sensorType);
	}

	public SensorMPU9X50(SENSORS sensorType, ShimmerDevice shimmerDevice) {
		super(sensorType, shimmerDevice);
	}
	
    //--------- Constructors for this class end --------------

	
	@Override
	public ObjectCluster processDataCustom(SensorDetails sensorDetails, byte[] rawData, COMMUNICATION_TYPE commType, ObjectCluster objectCluster, boolean isTimeSyncEnabled, double pcTimestampMs) {

		sensorDetails.processDataCommon(rawData, commType, objectCluster, isTimeSyncEnabled, pcTimestampMs);
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
		
		if (mEnableCalibration && mCurrentCalibDetailsGyro!=null){

			//Uncalibrated Gyro data
			if(sensorDetails.mSensorDetailsRef.mGuiFriendlyLabel.equals(GuiLabelSensors.GYRO)){
				double[] uncalibratedGyroData = new double[3];
				for(ChannelDetails channelDetails:sensorDetails.mListOfChannels){
					if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.GYRO_X)){
						uncalibratedGyroData[0] = (double)((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(channelDetails.mObjectClusterName), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
					}
					else if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.GYRO_Y)){
						uncalibratedGyroData[1] = (double)((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(channelDetails.mObjectClusterName), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
					}
					else if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.GYRO_Z)){
						uncalibratedGyroData[2] = (double)((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(channelDetails.mObjectClusterName), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
					}
				}
	
				double[] gyroCalibratedData = UtilCalibration.calibrateInertialSensorData(uncalibratedGyroData, getCurrentCalibDetailsGyro());
//				double[] gyroCalibratedData = UtilCalibration.calibrateInertialSensorData(unCalibratedGyroData, mAlignmentMatrixGyroscope, mSensitivityMatrixGyroscope, mOffsetVectorGyroscope);
//				//for testing
////				double[] gyroCalibratedData = UtilCalibration.calibrateInertialSensorData(unCalibratedGyroData, AlignmentMatrixGyroShimmer3, SensitivityMatrixGyro1000dpsShimmer3, OffsetVectorGyroShimmer3);
				
//				if (isGyroOnTheFlyCalEnabled()){
//					getOnTheFlyCalGyro().updateGyroOnTheFlyGyroOVCal(getCurrentCalibDetailsGyro(), gyroCalibratedData, uncalibratedGyroData);
//				}
				
				//Add calibrated data to Object cluster
				for(ChannelDetails channelDetails:sensorDetails.mListOfChannels){
					if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.GYRO_X)){
						objectCluster.addCalData(channelDetails, gyroCalibratedData[0], objectCluster.getIndexKeeper()-3, isUsingDefaultGyroParam());
					}
					else if(channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.GYRO_Y)){
						objectCluster.addCalData(channelDetails, gyroCalibratedData[1], objectCluster.getIndexKeeper()-2, isUsingDefaultGyroParam());
					}
					else if(channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.GYRO_Z)){
						objectCluster.addCalData(channelDetails, gyroCalibratedData[2], objectCluster.getIndexKeeper()-2, isUsingDefaultGyroParam());
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
	public void configBytesGenerate(ShimmerDevice shimmerDevice, byte[] configBytes, COMMUNICATION_TYPE commType) {
		
		ConfigByteLayout configByteLayout = shimmerDevice.getConfigByteLayout();
		if(configByteLayout instanceof ConfigByteLayoutShimmer3){
			ConfigByteLayoutShimmer3 configByteLayoutCast = (ConfigByteLayoutShimmer3) configByteLayout;
			
			configBytes[configByteLayoutCast.idxConfigSetupByte1] |= (byte) ((getMPU9X50GyroAccelRate() & configByteLayoutCast.maskMPU9150AccelGyroSamplingRate) << configByteLayoutCast.bitShiftMPU9150AccelGyroSamplingRate);
			configBytes[configByteLayoutCast.idxConfigSetupByte2] |= (byte) ((getGyroRange() & configByteLayoutCast.maskMPU9150GyroRange) << configByteLayoutCast.bitShiftMPU9150GyroRange);
			configBytes[configByteLayoutCast.idxConfigSetupByte3] |= (byte) ((getMPU9X50AccelRange() & configByteLayoutCast.maskMPU9150AccelRange) << configByteLayoutCast.bitShiftMPU9150AccelRange);
			
			// MPU9150 Gyroscope Calibration Parameters
			byte[] bufferCalibrationParameters = generateCalParamGyroscope();
			System.arraycopy(bufferCalibrationParameters, 0, configBytes, configByteLayoutCast.idxMPU9150GyroCalibration, configByteLayoutCast.lengthGeneralCalibrationBytes);
			
			configBytes[configByteLayoutCast.idxConfigSetupByte6] = (byte) (0x00);

			if(shimmerDevice.getShimmerVerObject().isSupportedMpl()) {
				configBytes[configByteLayoutCast.idxConfigSetupByte4] = (byte) (0x00);
				configBytes[configByteLayoutCast.idxConfigSetupByte5] = (byte) (0x00);

				configBytes[configByteLayoutCast.idxConfigSetupByte4] |= (byte) ((mMPU9X50DMP & configByteLayoutCast.maskMPU9150DMP) << configByteLayoutCast.bitShiftMPU9150DMP);
				configBytes[configByteLayoutCast.idxConfigSetupByte4] |= (byte) ((mMPU9X50LPF & configByteLayoutCast.maskMPU9150LPF) << configByteLayoutCast.bitShiftMPU9150LPF);
				configBytes[configByteLayoutCast.idxConfigSetupByte4] |= (byte) ((mMPU9X50MotCalCfg & configByteLayoutCast.maskMPU9150MotCalCfg) << configByteLayoutCast.bitShiftMPU9150MotCalCfg);

				configBytes[configByteLayoutCast.idxConfigSetupByte5] |= (byte) ((mMPU9X50MPLSamplingRate & configByteLayoutCast.maskMPU9150MPLSamplingRate) << configByteLayoutCast.bitShiftMPU9150MPLSamplingRate);
				configBytes[configByteLayoutCast.idxConfigSetupByte5] |= (byte) ((mMPU9X50MagSamplingRate & configByteLayoutCast.maskMPU9150MPLSamplingRate) << configByteLayoutCast.bitShiftMPU9150MagSamplingRate);

				configBytes[configByteLayoutCast.idxSensors3] = (byte) ((shimmerDevice.getEnabledSensors() >> configByteLayoutCast.bitShiftSensors3) & 0xFF);
				configBytes[configByteLayoutCast.idxSensors4] = (byte) ((shimmerDevice.getEnabledSensors() >> configByteLayoutCast.bitShiftSensors4) & 0xFF);

				configBytes[configByteLayoutCast.idxConfigSetupByte6] |= (byte) ((mMPLSensorFusion & configByteLayoutCast.maskMPLSensorFusion) << configByteLayoutCast.bitShiftMPLSensorFusion);
				configBytes[configByteLayoutCast.idxConfigSetupByte6] |= (byte) ((mMPLGyroCalTC & configByteLayoutCast.maskMPLGyroCalTC) << configByteLayoutCast.bitShiftMPLGyroCalTC);
				configBytes[configByteLayoutCast.idxConfigSetupByte6] |= (byte) ((mMPLVectCompCal & configByteLayoutCast.maskMPLVectCompCal) << configByteLayoutCast.bitShiftMPLVectCompCal);
				configBytes[configByteLayoutCast.idxConfigSetupByte6] |= (byte) ((mMPLMagDistCal & configByteLayoutCast.maskMPLMagDistCal) << configByteLayoutCast.bitShiftMPLMagDistCal);
				configBytes[configByteLayoutCast.idxConfigSetupByte6] |= (byte) ((mMPLEnable & configByteLayoutCast.maskMPLEnable) << configByteLayoutCast.bitShiftMPLEnable);

				//TODO: decide what to do
				//MPL Accel Calibration Parameters
				//MPL Mag Calibration Configuration
				//MPL Gyro Calibration Configuration
			}
			
//			if(configByteLayoutCast.idxSwConfigByte0>0){
//				configBytes[configByteLayoutCast.idxSwConfigByte0] |= (byte) ((isGyroOnTheFlyCalEnabled()? (configByteLayoutCast.maskGyroOnTheFly << configByteLayoutCast.bitShiftGyroOnTheFly):0));
//			}

		}
	}

	@Override
	public void configBytesParse(ShimmerDevice shimmerDevice, byte[] configBytes, COMMUNICATION_TYPE commType) {
		
		ConfigByteLayout configByteLayout = shimmerDevice.getConfigByteLayout();
		if(configByteLayout instanceof ConfigByteLayoutShimmer3){
			ConfigByteLayoutShimmer3 configByteLayoutCast = (ConfigByteLayoutShimmer3) configByteLayout;
			
			setMPU9X50GyroAccelRate((configBytes[configByteLayoutCast.idxConfigSetupByte1] >> configByteLayoutCast.bitShiftMPU9150AccelGyroSamplingRate) & configByteLayoutCast.maskMPU9150AccelGyroSamplingRate);
			checkLowPowerGyro(); // check rate to determine if Sensor is in LPM mode

			setGyroRange((configBytes[configByteLayoutCast.idxConfigSetupByte2] >> configByteLayoutCast.bitShiftMPU9150GyroRange) & configByteLayoutCast.maskMPU9150GyroRange);
			
			setMPU9X50AccelRange((configBytes[configByteLayoutCast.idxConfigSetupByte3] >> configByteLayoutCast.bitShiftMPU9150AccelRange) & configByteLayoutCast.maskMPU9150AccelRange);

			//if bt connected use the infomem, otherwise if its docked the infomem read is skipped when u reset to default using bt
			if (shimmerDevice.isConnected()){
				getCurrentCalibDetailsGyro().mCalibReadSource=CALIB_READ_SOURCE.INFOMEM;
			}
			
			// MPU9150 Gyroscope Calibration Parameters
			byte[] bufferCalibrationParameters = new byte[configByteLayoutCast.lengthGeneralCalibrationBytes];
			System.arraycopy(configBytes, configByteLayoutCast.idxMPU9150GyroCalibration, bufferCalibrationParameters, 0 , configByteLayoutCast.lengthGeneralCalibrationBytes);
			parseCalibParamFromPacketGyro(bufferCalibrationParameters, CALIB_READ_SOURCE.INFOMEM);

			if(shimmerDevice.getShimmerVerObject().isSupportedMpl()) {
				setMPU9150DMP((configBytes[configByteLayoutCast.idxConfigSetupByte4] >> configByteLayoutCast.bitShiftMPU9150DMP) & configByteLayoutCast.maskMPU9150DMP);
				setMPU9X50LPF((configBytes[configByteLayoutCast.idxConfigSetupByte4] >> configByteLayoutCast.bitShiftMPU9150LPF) & configByteLayoutCast.maskMPU9150LPF);
				setMPU9X150MotCalCfg( (configBytes[configByteLayoutCast.idxConfigSetupByte4] >> configByteLayoutCast.bitShiftMPU9150MotCalCfg) & configByteLayoutCast.maskMPU9150MotCalCfg);
				
				setMPU9150MPLSamplingRate((configBytes[configByteLayoutCast.idxConfigSetupByte5] >> configByteLayoutCast.bitShiftMPU9150MPLSamplingRate) & configByteLayoutCast.maskMPU9150MPLSamplingRate);
				setMPU9X50MagSamplingRate((configBytes[configByteLayoutCast.idxConfigSetupByte5] >> configByteLayoutCast.bitShiftMPU9150MagSamplingRate) & configByteLayoutCast.maskMPU9150MagSamplingRate);
				
				setMPLSensorFusion((configBytes[configByteLayoutCast.idxConfigSetupByte6] >> configByteLayoutCast.bitShiftMPLSensorFusion) & configByteLayoutCast.maskMPLSensorFusion);
				setMPLGyroCalTC((configBytes[configByteLayoutCast.idxConfigSetupByte6] >> configByteLayoutCast.bitShiftMPLGyroCalTC) & configByteLayoutCast.maskMPLGyroCalTC);
				setMPLVectCompCal((configBytes[configByteLayoutCast.idxConfigSetupByte6] >> configByteLayoutCast.bitShiftMPLVectCompCal) & configByteLayoutCast.maskMPLVectCompCal);
				setMPLMagDistCal((configBytes[configByteLayoutCast.idxConfigSetupByte6] >> configByteLayoutCast.bitShiftMPLMagDistCal) & configByteLayoutCast.maskMPLMagDistCal);
				setMPLEnabled((configBytes[configByteLayoutCast.idxConfigSetupByte6] >> configByteLayoutCast.bitShiftMPLEnable) & configByteLayoutCast.maskMPLEnable);
				
				//MPL Accel Calibration Parameters
				bufferCalibrationParameters = new byte[configByteLayoutCast.lengthGeneralCalibrationBytes];
				System.arraycopy(configBytes, configByteLayoutCast.idxMPLAccelCalibration, bufferCalibrationParameters, 0 , configByteLayoutCast.lengthGeneralCalibrationBytes);
				setCalibParamMPLAccel(bufferCalibrationParameters);
		
				//MPL Mag Calibration Configuration
				bufferCalibrationParameters = new byte[configByteLayoutCast.lengthGeneralCalibrationBytes];
				System.arraycopy(configBytes, configByteLayoutCast.idxMPLMagCalibration, bufferCalibrationParameters, 0 , configByteLayoutCast.lengthGeneralCalibrationBytes);
				setCalibParamMPLMag(bufferCalibrationParameters);
		
				//MPL Gyro Calibration Configuration
				bufferCalibrationParameters = new byte[configByteLayoutCast.lengthGeneralCalibrationBytes];
				System.arraycopy(configBytes, configByteLayoutCast.idxMPLGyroCalibration, bufferCalibrationParameters, 0 , configByteLayoutCast.lengthGeneralCalibrationBytes);
				setCalibParamMPLGyro(bufferCalibrationParameters);
			}

//			if(configByteLayoutCast.idxSwConfigByte0>0){
//				setOnTheFlyGyroCal(((configBytes[configByteLayoutCast.idxSwConfigByte0] >> configByteLayoutCast.bitShiftGyroOnTheFly) & configByteLayoutCast.maskGyroOnTheFly)>0? true:false);
//			}

			//TODO calib times
		}
	}
		

	@Override
	public Object setConfigValueUsingConfigLabel(Integer sensorId, String configLabel, Object valueToSet) {

		Object returnValue = null;
		int buf = 0;

		switch(configLabel){
//Booleans
			case(SensorMPU9X50.GuiLabelConfig.MPU9X50_GYRO_LPM):
				setLowPowerGyro((boolean)valueToSet);
	        	break;
			case(SensorMPU9X50.GuiLabelConfig.MPU9X50_DMP):
	        	setMPU9150DMP((boolean)valueToSet);
	        	break;
			case(SensorMPU9X50.GuiLabelConfig.MPU9X50_MPL):
	        	setMPLEnabled((boolean)valueToSet);
	        	break;
			case(SensorMPU9X50.GuiLabelConfig.MPU9X50_MPL_9DOF_SENSOR_FUSION):
	        	setMPLSensorFusion((boolean)valueToSet);
	        	break;
			case(SensorMPU9X50.GuiLabelConfig.MPU9X50_MPL_GYRO_CAL):
	        	setMPLGyroCalTC((boolean)valueToSet);
	        	break;
			case(SensorMPU9X50.GuiLabelConfig.MPU9X50_MPL_VECTOR_CAL):
	        	setMPLVectCompCal((boolean)valueToSet);
	        	break;
			case(SensorMPU9X50.GuiLabelConfig.MPU9X50_MPL_MAG_CAL):
	        	setMPLMagDistCal((boolean)valueToSet);
	        	break;
//			case(SensorMPU9X50.GuiLabelConfig.GYRO_ON_THE_FLY_CALIB_STATE):
//				setOnTheFlyGyroCal((boolean)valueToSet);
//	        	break;
//Integers
			case(SensorMPU9X50.GuiLabelConfig.MPU9X50_GYRO_RANGE):
	        	setMPU9150GyroRange((int)valueToSet);
	        	break;
			case(SensorMPU9X50.GuiLabelConfig.MPU9X50_ACCEL_RANGE):
				setMPU9X50AccelRange((int)valueToSet);
	        	break;
			case(SensorMPU9X50.GuiLabelConfig.MPU9X50_DMP_GYRO_CAL):
				setMPU9X150MotCalCfg((int)valueToSet);
	        	break;
			case(SensorMPU9X50.GuiLabelConfig.MPU9X50_MPL_LPF):
				setMPU9X50LPF((int)valueToSet);
	        	break;
			case(SensorMPU9X50.GuiLabelConfig.MPU9X50_MPL_RATE):
				setMPU9150MPLSamplingRate((int)valueToSet);
	        	break;
			case(SensorMPU9X50.GuiLabelConfig.MPU9X50_MAG_RATE):
				setMPU9X50MagSamplingRate((int)valueToSet);
	        	break;
//			case(SensorMPU9X50.GuiLabelConfig.GYRO_ON_THE_FLY_CALIB_THRESHOLD):
//	        	setOnTheFlyCalGyroThreshold((double)valueToSet);
//	        	break;
//Strings
			case(SensorMPU9X50.GuiLabelConfig.MPU9X50_GYRO_RATE):
	        	double bufDouble = 4.0; // Minimum = 4Hz
	        	if(((String)valueToSet).isEmpty()) {
	        		bufDouble = 4.0;
	        	}
	        	else {
	        		bufDouble = Double.parseDouble((String)valueToSet);
	        	}
	        	
	        	// Since user is manually entering a freq., clear low-power mode so that their chosen rate will be set correctly. Tick box will be re-enabled automatically if they enter LPM freq. 
	        	setLowPowerGyro(false); 
				if(debugGyroRate && mShimmerDevice!=null){
					System.out.println("Gyro Rate change from freq:\t" + mShimmerDevice.getMacId() + "\tGuiLabelConfig\t" + bufDouble);
				}
	    		setMPU9150GyroAccelRateFromFreq(bufDouble);
	
	    		returnValue = Double.toString((double)Math.round(getMPU9X50GyroAccelRateInHz() * 100) / 100); // round sampling rate to two decimal places
	//    		System.out.println("Gyro Sampling rate: " + getMPU9X50GyroAccelRateInHz() + " " + returnValue);
	
	        	break;
			case(SensorMPU9X50.GuiLabelConfig.MPU9X50_GYRO_RATE_HZ):
				System.err.print("BUG");
	//			returnValue = getMPU9X50GyroAccelRateInHz();
				break;

			
	        default:
	        	returnValue = super.setConfigValueUsingConfigLabelCommon(sensorId, configLabel, valueToSet);
	        	break;
		}
		return returnValue;
	}
	
	
	@Override
	public Object getConfigValueUsingConfigLabel(Integer sensorId, String configLabel) {
		Object returnValue = null;
		switch(configLabel){
//Booleans
			case(SensorMPU9X50.GuiLabelConfig.MPU9X50_GYRO_LPM):
				returnValue = checkLowPowerGyro();
	        	break;
			case(GuiLabelConfig.MPU9X50_DMP):
				returnValue = isMPU9150DMP();
	        	break;
			case(GuiLabelConfig.MPU9X50_MPL):
				returnValue = isMPLEnable();
	        	break;
			case(GuiLabelConfig.MPU9X50_MPL_9DOF_SENSOR_FUSION):
				returnValue = isMPLSensorFusion();
	        	break;
			case(GuiLabelConfig.MPU9X50_MPL_GYRO_CAL):
				returnValue = isMPLGyroCalTC();
	        	break;
			case(GuiLabelConfig.MPU9X50_MPL_VECTOR_CAL):
				returnValue = isMPLVectCompCal();
	        	break;
			case(GuiLabelConfig.MPU9X50_MPL_MAG_CAL):
				returnValue = isMPLMagDistCal();
	        	break;
//			case(SensorMPU9X50.GuiLabelConfig.GYRO_ON_THE_FLY_CALIB_STATE):
//				returnValue = isGyroOnTheFlyCalEnabled();
//	        	break;

//Integers
			case(GuiLabelConfig.MPU9X50_GYRO_RANGE):
				returnValue = getGyroRange();
	        	break;

			case(GuiLabelConfig.MPU9X50_ACCEL_RANGE):
				returnValue = getMPU9X50AccelRange();
		    	break;
			case(GuiLabelConfig.MPU9X50_DMP_GYRO_CAL):
				returnValue = getMPU9X50MotCalCfg();
		    	break;
			case(GuiLabelConfig.MPU9X50_MPL_LPF):
				returnValue = getMPU9X50LPF();
		    	break;
			case(GuiLabelConfig.MPU9X50_MPL_RATE):
				returnValue = getMPU9X50MPLSamplingRate();
				break;
			case(GuiLabelConfig.MPU9X50_MAG_RATE):
				returnValue = getMPU9X50MagSamplingRate();
		    	break;
//			case(SensorMPU9X50.GuiLabelConfig.GYRO_ON_THE_FLY_CALIB_THRESHOLD):
//				returnValue = getOnTheFlyCalGyroThreshold();
//	        	break;
		    	
//Strings
			case(GuiLabelConfig.MPU9X50_GYRO_RATE):
				returnValue = Double.toString((double)Math.round(getMPU9X50GyroAccelRateInHz() * 100) / 100); // round sampling rate to two decimal places
//    		    		System.out.println("Gyro Sampling rate: " + getMPU9X50GyroAccelRateInHz() + " " + returnValue);
	        	break;
			case(GuiLabelConfig.MPU9X50_GYRO_RATE_HZ):
				returnValue = getMPU9X50GyroAccelRateInHz();
				break;
				
			case(GuiLabelConfigCommon.RANGE):
				if(sensorId==mSensorIdGyro){
					returnValue = this.getConfigValueUsingConfigLabel(GuiLabelConfig.MPU9X50_GYRO_RANGE);
				}
				else if(sensorId==mSensorIdAccel){
					returnValue = this.getConfigValueUsingConfigLabel(GuiLabelConfig.MPU9X50_ACCEL_RANGE);
				}
				break;
			case(GuiLabelConfigCommon.RATE):
				if(sensorId==mSensorIdGyro){
					returnValue = this.getConfigValueUsingConfigLabel(GuiLabelConfig.MPU9X50_GYRO_RATE);
				}
				else if(sensorId==mSensorIdAccel){
					returnValue = this.getConfigValueUsingConfigLabel(GuiLabelConfig.MPU9X50_GYRO_RATE);
				}
				break;
				
			default:
				returnValue = super.getConfigValueUsingConfigLabelCommon(sensorId, configLabel);
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
			case(GuiLabelConfig.MPU9X50_ACCEL_RANGE):
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
		setLowPowerGyro(false);

		if(debugGyroRate && mShimmerDevice!=null){
			System.out.println("Gyro Rate change from freq:\t" + mShimmerDevice.getMacId() + "\tsetSamplingRateSensors\t" + samplingRateHz);
		}

		setMPU9150GyroAccelRateFromFreq(samplingRateHz);
		setMPU9150MagRateFromFreq(samplingRateHz);
		if(mShimmerVerObject.isSupportedMpl()){
			setMPU9150MplRateFromFreq(samplingRateHz);
		}
		
//		setOnTheFlyCalGyroBufferSizeFromSamplingRate(samplingRateHz);
		
    	checkLowPowerGyro();
	}

	@Override
	public boolean setDefaultConfigForSensor(int sensorId, boolean isSensorEnabled) {
		if(mSensorMap.containsKey(sensorId)){
			if(sensorId==mSensorIdAccel){
				setDefaultMpu9150AccelSensorConfig(isSensorEnabled);
			}
			else if(sensorId==mSensorIdGyro){
				setDefaultMpu9150GyroSensorConfig(isSensorEnabled);
			}
			else if(sensorId==mSensorIdMag){
				if(mShimmerDevice!=null){
					setMPU9150MagRateFromFreq(getSamplingRateShimmer());
				}
			}
			else if(SensorMPU9X50.mListOfMplChannels.contains(sensorId)){ //RS (30/5/2016) - Why is a default config set if only one MPL sensor is enabled? 
				if(!checkIfAnyOtherMplChannelEnabled(sensorId)) {
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
		if(debugGyroRate && mShimmerDevice!=null){
			System.out.println("Gyro Rate change from freq:\t" + mShimmerDevice.getMacId() + "\t" + freq);
		}
		
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
			if(mMPU9X50LPF == 0) {
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
				setMPU9X50GyroAccelRate(result);
	
			}
			else {
				setMPU9X50GyroAccelRate(0xFF); // Dec. = 255, Freq. = 31.25Hz (or 3.92Hz when LPF enabled)
			}
		}
		else {
			setMPU9X50GyroAccelRate(0xFF); // Dec. = 255, Freq. = 31.25Hz (or 3.92Hz when LPF enabled)
		}
		return getMPU9X50GyroAccelRate();			
	}
	
	/**
	 * Computes next higher available sensor sampling rate setting based on
	 * passed in "freq" variable and dependent on whether low-power mode is set.
	 * 
	 * @param freq
	 * @return int the rate configuration setting for the respective sensor
	 */
	public int setMPU9150MagRateFromFreq(double freq) {
		boolean setFreq = false;
		// Check if channel is enabled 
		if(checkIfAnyMplChannelEnabled()){
			setFreq = true;
		}
		else if (isSensorEnabled(mSensorIdMag)) {
			setFreq = true;
		}
		
		if(setFreq){
			if (freq<=10){
				mMPU9X50MagSamplingRate = 0; // 10Hz
			} else if (freq<=20){
				mMPU9X50MagSamplingRate = 1; // 20Hz
			} else if (freq<=40) {
				mMPU9X50MagSamplingRate = 2; // 40Hz
			} else if (freq<=50) {
				mMPU9X50MagSamplingRate = 3; // 50Hz
			} else {
				mMPU9X50MagSamplingRate = 4; // 100Hz
			}
		}
		else {
			mMPU9X50MagSamplingRate = 0; // 10 Hz
		}
		return mMPU9X50MagSamplingRate;
	}
	
	/**
	 * Computes next higher available sensor sampling rate setting based on
	 * passed in "freq" variable and dependent on whether low-power mode is set.
	 * 
	 * @param freq
	 * @return int the rate configuration setting for the respective sensor
	 */
	public int setMPU9150MplRateFromFreq(double freq) {
		// Check if channel is enabled 
		if(!checkIfAnyMplChannelEnabled()){
			mMPU9X50MPLSamplingRate = 0; // 10 Hz
			return mMPU9X50MPLSamplingRate;
		}
		
		if (freq<=10){
			mMPU9X50MPLSamplingRate = 0; // 10Hz
		} else if (freq<=20){
			mMPU9X50MPLSamplingRate = 1; // 20Hz
		} else if (freq<=40) {
			mMPU9X50MPLSamplingRate = 2; // 40Hz
		} else if (freq<=50) {
			mMPU9X50MPLSamplingRate = 3; // 50Hz
		} else {
			mMPU9X50MPLSamplingRate = 4; // 100Hz
		}
		return mMPU9X50MPLSamplingRate;
	}
	
	//TODO check why is the Accel SensorId is referenced here
	public void setDefaultMpu9150GyroSensorConfig(boolean isSensorEnabled) {
		if(!checkIfAnyMplChannelEnabled()) {
			if(!isSensorEnabled(mSensorIdAccel)) {
				if(isSensorEnabled) {
					setLowPowerGyro(false);
				}
				else {
					setLowPowerGyro(true);
				}
			}
			
			setGyroRange(1);
//			if(!state){
//				mGyroRange=1; // 500dps
//			}
		}
		else {
			setGyroRange(3); // 2000dps
		}
	}
	
	//TODO check why is the Gyro SensorId is referenced here
	public void setDefaultMpu9150AccelSensorConfig(boolean isSensorEnabled) {
		if(!checkIfAnyMplChannelEnabled()) {
			if(!isSensorEnabled(mSensorIdGyro)) {
				if(isSensorEnabled) {
					setLowPowerGyro(false);
				}
				else {
					setLowPowerGyro(true);
				}
			}
			
			if(!isSensorEnabled){
				mMPU9X50AccelRange = 0; //=2g
			}
		}
		else {
			mMPU9X50AccelRange = 0; //=2g
		}
	}
	
	public void setDefaultMpu9150MplSensorConfig(boolean isSensorEnabled) {
		mMPU9X50DMP = isSensorEnabled? 1:0;
		mMPLEnable = isSensorEnabled? 1:0;
		mMPU9X50LPF = isSensorEnabled? 1:0; // 1 = 188Hz
		mMPU9X50MotCalCfg = isSensorEnabled? 1:0; // 1= Fast Calibration
		mMPLGyroCalTC = isSensorEnabled? 1:0;
		mMPLVectCompCal = isSensorEnabled? 1:0;
		mMPLMagDistCal = isSensorEnabled? 1:0;
		mMPLSensorFusion = 0;

		if(isSensorEnabled){
//			//Gyro rate can not be set to 250dps when DMP is on
//			if(mGyroRange==0){
//				mGyroRange=1;
//			}
			
			//force gyro range to be 2000dps and accel range to be +-2g - others untested
			setGyroRange(3); // 2000dps
			mMPU9X50AccelRange= 0; // 2g
			
			setLowPowerGyro(false);
		}
		else {
			if(checkIfAMpuGyroOrAccelEnabled()){
				if(debugGyroRate && mShimmerDevice!=null){
					System.out.println("Gyro Rate change from freq:\t" + mShimmerDevice.getMacId() + "\tMPL off but Gyro/Accel still enabled\t" + mShimmerDevice.getSamplingRateShimmer());
				}
				if(mShimmerDevice!=null){
					setMPU9150GyroAccelRateFromFreq(getSamplingRateShimmer());
				}
			}
			else {
				setLowPowerGyro(true);
			}
		}
		if(mShimmerDevice!=null){
			setMPU9150MagRateFromFreq(getSamplingRateShimmer());
			setMPU9150MplRateFromFreq(getSamplingRateShimmer());
		}
	}
	
	public boolean checkIfAMpuGyroOrAccelEnabled(){
		if(isSensorEnabled(mSensorIdGyro)) {
			return true;
		}
		if(isSensorEnabled(mSensorIdAccel)) {
			return true;
		}
//		if(mSensorMap.get(SENSOR_ID.SHIMMER_MPU9150_MAG) != null) {
//			if(mSensorMap.get(SENSOR_ID.SHIMMER_MPU9150_MAG).mIsEnabled) {
//				return true;
//			}
//		}
		return false;
	}	
	
	public boolean checkIfAnyOtherMplChannelEnabled(int sensorId){
		if(mShimmerVerObject.isSupportedMpl()){
			if(mSensorMap.keySet().size()>0){
				for(int key:SensorMPU9X50.mListOfMplChannels){
					if(key!=sensorId && isSensorEnabled(key)) {
						return true;
					}
				}
			}
		}
		return false;
	}
			
	public boolean checkIfAnyMplChannelEnabled(){
		if(mShimmerVerObject.isSupportedMpl()){
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
	
	/**
	 * @return the mMPU9X50AccelRange
	 */
	public int getMPU9X50AccelRange() {
		return mMPU9X50AccelRange;
	}

	/**
	 * @return the mMPU9X50GyroAccelRate
	 */
	public int getMPU9X50GyroAccelRate() {
		return mMPU9X50GyroAccelRate;
	}

	/**
	 * @return the mMPU9X50MotCalCfg
	 */
	public int getMPU9X50MotCalCfg() {
		return mMPU9X50MotCalCfg;
	}
	/**
	 * @return the mMPU9X50LPF
	 */
	public int getMPU9X50LPF() {
		return mMPU9X50LPF;
	}
	
	public int getMPU9X50DMP() {
		return mMPU9X50DMP;
	}

	/**
	 * @return the mMPU9X50MPLSamplingRate
	 */
	public int getMPU9X50MPLSamplingRate() {
		return mMPU9X50MPLSamplingRate;
	}

	/**
	 * @return the mMPU9X50MagSamplingRate
	 */
	public int getMPU9X50MagSamplingRate() {
		return mMPU9X50MagSamplingRate;
	}
	
	/**
	 * @return the mMPU9X50GyroAccelRate in Hz
	 */
	public double getMPU9X50GyroAccelRateInHz() {
		// Gyroscope Output Rate = 8kHz when the DLPF is disabled (DLPF_CFG = 0 or 7), and 1kHz when the DLPF is enabled
		double numerator = 1000.0;
		if(mMPU9X50LPF == 0) {
			numerator = 8000.0;
		}
		
		if(getMPU9X50GyroAccelRate() == 0) {
			return numerator;
		}
		else {
			return (numerator / (getMPU9X50GyroAccelRate()+1));
		}
	}
	
	/**
	 * @param mMPU9X50AccelRange the mMPU9X50AccelRange to set
	 */
	public void setMPU9X50AccelRange(int i) {
		if(ArrayUtils.contains(SensorMPU9X50.ListofMPU9X50GyroRangeConfigValues, i)){
			if(checkIfAnyMplChannelEnabled()){
				i=0; // 2g
			}
			mMPU9X50AccelRange = i;
		}
	}
	
	public int getGyroRange(){
		return mGyroRange;
	}
	
	public void setGyroRange(int gyroRange){
		setMPU9150GyroRange(gyroRange);
	}
	
	public void setMPU9150GyroRange(int i){
		if(ArrayUtils.contains(ListofMPU9X50GyroRangeConfigValues, i)){
//			//Gyro rate can not be set to 250dps when DMP is on
//			if((checkIfAnyMplChannelEnabled()) && (i==0)){
//				i=1;
//			}
			
			if(checkIfAnyMplChannelEnabled()){
				i=3; // 2000dps
			}
			
			mGyroRange = i;
			updateCurrentGyroCalibInUse();
		}
	}

	/**
	 * @param mMPU9X50MPLSamplingRate the mMPU9X50MPLSamplingRate to set
	 */
	public void setMPU9150MPLSamplingRate(int mMPU9X50MPLSamplingRate) {
		this.mMPU9X50MPLSamplingRate = mMPU9X50MPLSamplingRate;
	}

	/**
	 * @param mMPU9X50MagSamplingRate the mMPU9X50MagSamplingRate to set
	 */
	public void setMPU9X50MagSamplingRate(int mMPU9X50MagSamplingRate) {
		this.mMPU9X50MagSamplingRate = mMPU9X50MagSamplingRate;
	}
	
	// MPL options
	/**
	 * @return the mMPU9X50DMP
	 */
	public boolean isMPU9150DMP() {
		return (mMPU9X50DMP>0)? true:false;
	}


	/**
	 * @param state the mMPU9X50DMP state to set
	 */
	public void setMPU9150DMP(boolean state) {
		setMPU9150DMP((state? 1:0));
	}
	
	public void setMPU9150DMP(int i) {
		this.mMPU9X50DMP = i;
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
	public void setMPLEnabled(boolean state) {
		setMPLEnabled(state? 1:0);
	}

	/**
	 * @param state the mMPLEnable state to set
	 */
	public void setMPLEnabled(int state) {
		mMPLEnable = state;
	}

	public int getMPLEnable() {
		return mMPLEnable;
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
	public void setMPLGyroCalTC(boolean state) {
		mMPLGyroCalTC = state? 1:0;
	}
	
	public void setMPLGyroCalTC(int state) {
		mMPLGyroCalTC = state;
	}
	
	public int getMPLGyroCalTC() {
		return mMPLGyroCalTC;
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
	public void setMPLVectCompCal(boolean state) {
		setMPLVectCompCal(state? 1:0);
	}
	
	/**
	 * @param state the mMPLVectCompCal state to set
	 */
	public void setMPLVectCompCal(int state) {
		this.mMPLVectCompCal = state;
	}
	
	public int getMPLVectCompCal() {
		return mMPLVectCompCal;
	}

	
	public int getMPLMagDistCal() {
		return mMPLMagDistCal;
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
	public void setMPLMagDistCal(boolean state) {
		setMPLMagDistCal(state? 1:0);
	}

	
	/**
	 * @param state the mMPLMagDistCal state to set
	 */
	public void setMPLMagDistCal(int state) {
		mMPLMagDistCal = state;
	}

	/**
	 * @return the mMPLSensorFusion
	 */
	public int getMPLSensorFusion() {
		return mMPLSensorFusion;
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
	public void setMPLSensorFusion(boolean state) {
		setMPLSensorFusion(state? 1:0);
	}

	public void setMPLSensorFusion(int state) {
		mMPLSensorFusion = state;
	}


	/**
	 * @param mMPU9X50MotCalCfg the mMPU9X50MotCalCfg to set
	 */
	public void setMPU9150MotCalCfg(boolean state) {
		setMPU9X150MotCalCfg(state? 1:0);
	}

	/**
	 * @param mMPU9X50MotCalCfg the mMPU9X50MotCalCfg to set
	 */
	public void setMPU9X150MotCalCfg(int state) {
		this.mMPU9X50MotCalCfg = state;
	}

	/**
	 * @param mMPU9X50LPF the mMPU9X50LPF to set
	 */
	public void setMPU9X50LPF(int mMPU9X50LPF) {
		this.mMPU9X50LPF = mMPU9X50LPF;
	}
	
	/**
	 * @return the mMPU9X50GyroAccelRate
	 */

	public void setMPU9X50GyroAccelRate(int rate) {
		mMPU9X50GyroAccelRate = rate;
	}
    
	public byte[] generateCalParamGyroscope(){
		return mCurrentCalibDetailsGyro.generateCalParamByteArray();
	}
	
	private boolean isGyroUsingDefaultParameters() {
		return mCurrentCalibDetailsGyro.isUsingDefaultParameters();
	}
	
	private void setDefaultCalibrationShimmer3Gyro() {
		mCurrentCalibDetailsGyro.resetToDefaultParameters();
	}
	

	public double[][] getAlignmentMatrixGyro(){
		return mCurrentCalibDetailsGyro.getValidAlignmentMatrix();
	}

	public double[][] getSensitivityMatrixGyro(){
		return mCurrentCalibDetailsGyro.getValidSensitivityMatrix();
	}

	public double[][] getOffsetVectorMatrixGyro(){
		return mCurrentCalibDetailsGyro.getValidOffsetVector();
	}
	
	public double[][] getOffsetVectorMPLAccel(){
		return OffsetVectorMPLAccel;
	}
	
	public double[][] getSensitivityMatrixMPLAccel(){
		return SensitivityMatrixMPLAccel;
	}
	
	public double[][] getAlignmentMatrixMPLAccel(){
		return AlignmentMatrixMPLAccel;
	}
	
	public double[][] getOffsetVectorMPLMag(){
		return OffsetVectorMPLMag;
	}
	
	public double[][] getSensitivityMatrixMPLMag(){
		return SensitivityMatrixMPLMag;
	}
	
	public double[][] getAlignmentMatrixMPLMag(){
		return AlignmentMatrixMPLMag;
	}
	
	public double[][] getOffsetVectorMPLGyro(){
		return OffsetVectorMPLGyro;
	}
	
	public double[][] getSensitivityMatrixMPLGyro(){
		return SensitivityMatrixMPLGyro;
	}
	
	public double[][] getAlignmentMatrixMPLGyro(){
		return AlignmentMatrixMPLGyro;
	}

	public boolean isLowPowerGyroEnabled() {
		return mLowPowerGyro;
	}
	
	public boolean isUsingDefaultGyroParam(){
		return mCurrentCalibDetailsGyro.isUsingDefaultParameters();
	}
	
	/**
	 * This enables the low-power gyro option. When not enabled the sampling
	 * rate of the gyro is set to the closest supported value to the actual
	 * sampling rate that it can achieve. For the Shimmer2, in low power mode it
	 * defaults to 10Hz.
	 * 
	 * @param enable
	 */
	public void setLowPowerGyro(boolean enable){
		if(!checkIfAnyMplChannelEnabled()) {
			mLowPowerGyro = enable;
		}
		else{
			mLowPowerGyro = false;
		}
		
		if(debugGyroRate && mShimmerDevice!=null){
			System.out.println("Gyro Rate change from freq:\t" + mShimmerDevice.getMacId() + "\tsetLowPowerGyro\t" + mShimmerDevice.getSamplingRateShimmer());
		}
		if(mShimmerDevice!=null){
			setMPU9150GyroAccelRateFromFreq(getSamplingRateShimmer());
		}
	}
	
	public int getLowPowerGyroEnabled() {
		return mLowPowerGyro? 1:0;
	}

	/**
	 * Checks to see if the MPU9150 gyro is in low power mode. As determined by
	 * the sensor's sampling rate being set to the lowest possible value and not
	 * related to any specific configuration bytes sent to the Shimmer/MPU9150.
	 * 
	 * @return boolean, true if low-power mode enabled
	 */
	public boolean checkLowPowerGyro() {
		if(mMPU9X50GyroAccelRate == 0xFF) {
			mLowPowerGyro = true;
		}
		else {
			mLowPowerGyro = false;
		}
		return mLowPowerGyro;
	}
	
	public void setCalibParamMPLAccel(byte[] bufferCalibrationParameters) {
		String[] dataType={"i16","i16","i16","i16","i16","i16","i8","i8","i8","i8","i8","i8","i8","i8","i8"};

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
	}

	public void setCalibParamMPLMag(byte[] bufferCalibrationParameters) {
		String[] dataType={"i16","i16","i16","i16","i16","i16","i8","i8","i8","i8","i8","i8","i8","i8","i8"};
		
		int[] formattedPacket = UtilParseData.formatDataPacketReverse(bufferCalibrationParameters,dataType);
		double[] AM=new double[9];
		for (int i=0;i<9;i++)
		{
			AM[i]=((double)formattedPacket[6+i])/100;
		}
		double[][] alignmentMatrixMPLMag = {{AM[0],AM[1],AM[2]},{AM[3],AM[4],AM[5]},{AM[6],AM[7],AM[8]}}; 				
		double[][] sensitivityMatrixMPLMag = {{formattedPacket[3],0,0},{0,formattedPacket[4],0},{0,0,formattedPacket[5]}}; 
		double[][] offsetVectorMPLMag = {{formattedPacket[0]},{formattedPacket[1]},{formattedPacket[2]}};
		AlignmentMatrixMPLMag = alignmentMatrixMPLMag; 			
		SensitivityMatrixMPLMag = sensitivityMatrixMPLMag; 	
		OffsetVectorMPLMag = offsetVectorMPLMag;
	}

	public void setCalibParamMPLGyro(byte[] bufferCalibrationParameters) {
		String[] dataType={"i16","i16","i16","i16","i16","i16","i8","i8","i8","i8","i8","i8","i8","i8","i8"};
		
		int[] formattedPacket = UtilParseData.formatDataPacketReverse(bufferCalibrationParameters,dataType);
		double[] AM=new double[9];
		for (int i=0;i<9;i++)
		{
			AM[i]=((double)formattedPacket[6+i])/100;
		}
		double[][] alignmentMatrixMPLGyro = {{AM[0],AM[1],AM[2]},{AM[3],AM[4],AM[5]},{AM[6],AM[7],AM[8]}}; 				
		double[][] sensitivityMatrixMPLGyro = {{formattedPacket[3],0,0},{0,formattedPacket[4],0},{0,0,formattedPacket[5]}}; 
		double[][] offsetVectorMPLGyro = {{formattedPacket[0]},{formattedPacket[1]},{formattedPacket[2]}};
		AlignmentMatrixMPLGyro = alignmentMatrixMPLGyro; 			
		SensitivityMatrixMPLGyro = sensitivityMatrixMPLGyro; 	
		OffsetVectorMPLGyro = offsetVectorMPLGyro;						
	}

	// ----------- MPU9X50 options end -------------------------

	
	// ----------- extra MPU9X50 options copied from ShimmerObject -------------------------


	@Override
	public boolean checkConfigOptionValues(String stringKey) {
		// TODO Auto-generated method stub
		return false;
	}
	
	public void parseCalibParamFromPacketGyro(byte[] bufferCalibrationParameters, CALIB_READ_SOURCE calibReadSource) {
		mCurrentCalibDetailsGyro.parseCalParamByteArray(bufferCalibrationParameters, calibReadSource);
	}
	
	private boolean checkIfDefaultGyroCal(double[][] offsetVectorToTest, double[][] sensitivityMatrixToTest, double[][] alignmentMatrixToTest) {
		return mCurrentCalibDetailsGyro.isUsingDefaultParameters();
	}
	
	@Override
	public boolean processResponse(int responseCommand, Object parsedResponse, COMMUNICATION_TYPE commType) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public void checkShimmerConfigBeforeConfiguring() {
		// If Shimmer name is default, update with MAC ID if available.
//		if(ShimmerDevice.mShimmerUserAssignedName.equals(ShimmerDevice.DEFAULT_SHIMMER_NAME)){
//			setDefaultShimmerName();
//			}
		
		setLowPowerGyro(false);
		
		if(!isSensorEnabled(mSensorIdGyro)){
			setDefaultMpu9150GyroSensorConfig(false);
		}
		if(!isSensorEnabled(mSensorIdAccel)){
			setDefaultMpu9150AccelSensorConfig(false);
		}
		if(!isSensorEnabled(mSensorIdMag)){
			if(mShimmerDevice!=null){
				setMPU9150MagRateFromFreq(getSamplingRateShimmer());
			}
		}
		if(!checkIfAnyMplChannelEnabled()) {
			setDefaultMpu9150MplSensorConfig(false);
		}
		
//		checkIfMPLandDMPIsNeeded();

		//Added this for Conensys 1.0.0 release - assumes individual sampling rates of each sensor matches the Shimmer sampling
		setLowPowerGyro(false);
	}
	
//	/**
//	 * @param enable this enables the calibration of the gyroscope while streaming
//	 * @param bufferSize sets the buffersize of the window used to determine the new calibration parameters, see implementation for more details
//	 * @param threshold sets the threshold of when to use the incoming data to recalibrate gyroscope offset, this is in degrees, and the default value is 1.2
//	 */
//	public void enableOnTheFlyGyroCal(boolean state, int bufferSize, double threshold){
//		mOnTheFlyGyroOffsetCal.enableOnTheFlyGyroCal(state, bufferSize, threshold);
//	}
//	
//	public void setOnTheFlyGyroCal(boolean state){
//		mOnTheFlyGyroOffsetCal.enableOnTheFlyGyroCal(state);
//	}
//
//    public boolean isGyroOnTheFlyCalEnabled(){
//    	return mOnTheFlyGyroOffsetCal.isGyroOnTheFlyCalEnabled();
//	}
//
//    public OnTheFlyGyroOffsetCal getOnTheFlyCalGyro(){
//    	return mOnTheFlyGyroOffsetCal;
//    }
//    
//    public void setOnTheFlyCalGyroBufferSize(double samplingRate) {
//    	mOnTheFlyGyroOffsetCal.setBufferSizeFromSamplingRate(samplingRate);
//    }
//    
//    public void setOnTheFlyCalGyroThreshold(double threshold) {
//    	mOnTheFlyGyroOffsetCal.setGyroOnTheFlyCalThreshold(threshold);
//    }
//
//    public double getOnTheFlyCalGyroThreshold() {
//    	return mOnTheFlyGyroOffsetCal.getGyroOnTheFlyCalThreshold();
//    }

	
	//--------- Optional methods to override in Sensor Class start --------	
	@Override
	public void generateCalibMap() {
		super.generateCalibMap();
		
		TreeMap<Integer, CalibDetails> calibMapGyro = new TreeMap<Integer, CalibDetails>();
		calibMapGyro.put(calibDetailsGyro250.mRangeValue, calibDetailsGyro250);
		calibMapGyro.put(calibDetailsGyro500.mRangeValue, calibDetailsGyro500);
		calibMapGyro.put(calibDetailsGyro1000.mRangeValue, calibDetailsGyro1000);
		calibMapGyro.put(calibDetailsGyro2000.mRangeValue, calibDetailsGyro2000);
		
		mCalibMap.put(mSensorIdGyro, calibMapGyro);
		updateCurrentGyroCalibInUse();
		
		if(mShimmerVerObject.isSupportedMpl()){
			TreeMap<Integer, CalibDetails> mCalibMapMplAccel = new TreeMap<Integer, CalibDetails>();
			mCalibMapMplAccel.put(calibDetailsMplAccel.mRangeValue, calibDetailsMplAccel);
			mCalibMap.put(Configuration.Shimmer3.SENSOR_ID.SHIMMER_MPU9X50_MPL_ACCEL, calibMapGyro);
			
			TreeMap<Integer, CalibDetails> mCalibMapMplMag = new TreeMap<Integer, CalibDetails>();
			mCalibMapMplMag.put(calibDetailsMplMag.mRangeValue, calibDetailsMplMag);
			mCalibMap.put(Configuration.Shimmer3.SENSOR_ID.SHIMMER_MPU9X50_MPL_MAG, calibMapGyro);
			
			TreeMap<Integer, CalibDetails> mCalibMapMplGyro = new TreeMap<Integer, CalibDetails>();
			mCalibMapMplGyro.put(calibDetailsMplGyro.mRangeValue, calibDetailsMplGyro);
			mCalibMap.put(Configuration.Shimmer3.SENSOR_ID.SHIMMER_MPU9X50_MPL_GYRO, calibMapGyro);
		}
	}

	/* (non-Javadoc)
	 * @see com.shimmerresearch.sensors.AbstractSensor#isSensorUsingDefaultCal(int)
	 */
	@Override
	public boolean isSensorUsingDefaultCal(int sensorId) {
		if(sensorId==mSensorIdGyro){
			return isUsingDefaultGyroParam();
		}
		return false;
	}

	@Override
	public void setCalibrationMapPerSensor(int sensorId, TreeMap<Integer, CalibDetails> mapOfSensorCalibration) {
		super.setCalibrationMapPerSensor(sensorId, mapOfSensorCalibration);
		updateCurrentGyroCalibInUse();
	}

	@Override
	public double calcMaxSamplingRate() {
		//check if an MPL channel is enabled - limit rate to 51.2Hz
		if(checkIfAnyMplChannelEnabled() && mShimmerVerObject.isSupportedMpl()){
			return 51.2;
		} else {
			return super.calcMaxSamplingRate();
		}
	}
	//--------- Optional methods to override in Sensor Class end -------- 
	
	public void updateCurrentGyroCalibInUse(){
		mCurrentCalibDetailsGyro = getCurrentCalibDetailsIfKinematic(mSensorIdGyro, getGyroRange());
	}
	
	public CalibDetailsKinematic getCurrentCalibDetailsGyro(){
		return mCurrentCalibDetailsGyro;
	}

	public void updateIsUsingDefaultGyroParam() {
		mIsUsingDefaultGyroParam = getCurrentCalibDetailsGyro().isUsingDefaultParameters();
	}

}
	

