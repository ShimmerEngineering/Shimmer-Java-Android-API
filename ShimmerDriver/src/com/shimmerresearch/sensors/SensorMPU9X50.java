package com.shimmerresearch.sensors;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import com.shimmerresearch.driver.Configuration.CHANNEL_UNITS;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driver.Configuration.Shimmer3;
import com.shimmerresearch.driver.Configuration.Shimmer3.CompatibilityInfoForMaps;
import com.shimmerresearch.driver.Configuration.Shimmer3.DatabaseChannelHandles;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driverUtilities.ChannelDetails;
import com.shimmerresearch.driverUtilities.SensorConfigOptionDetails;
import com.shimmerresearch.driverUtilities.SensorDetails;
import com.shimmerresearch.driverUtilities.SensorDetailsRef;
import com.shimmerresearch.driverUtilities.SensorGroupingDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_ENDIAN;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_TYPE;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_SOURCE;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_TYPE;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.FW_ID;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.HW_ID;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.ShimmerDevice;

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

	protected double[][] AlignmentMatrixMPLAccel = {{-1,0,0},{0,1,0},{0,0,-1}}; 			
	protected double[][] SensitivityMatrixMPLAccel = {{1631,0,0},{0,1631,0},{0,0,1631}}; 	
	protected double[][] OffsetVectorMPLAccel = {{0},{0},{0}};
	
	protected double[][] AlignmentMatrixMPLMag = {{-1,0,0},{0,1,0},{0,0,-1}}; 			
	protected double[][] SensitivityMatrixMPLMag = {{1631,0,0},{0,1631,0},{0,0,1631}}; 	
	protected double[][] OffsetVectorMPLMag = {{0},{0},{0}};
	
	protected double[][] AlignmentMatrixMPLGyro = {{-1,0,0},{0,1,0},{0,0,-1}}; 			
	protected double[][] SensitivityMatrixMPLGyro = {{1631,0,0},{0,1631,0},{0,0,1631}}; 	
	protected double[][] OffsetVectorMPLGyro = {{0},{0},{0}};

	public boolean mDefaultCalibrationParametersGyro = true;
	public double[][] mAlignmentMatrixGyroscope = {{0,-1,0},{-1,0,0},{0,0,-1}}; 				
	public double[][] mSensitivityMatrixGyroscope = {{2.73,0,0},{0,2.73,0},{0,0,2.73}}; 		
	public double[][] mOffsetVectorGyroscope = {{1843},{1843},{1843}};
	
	//Shimmer3
	public static final double[][] SensitivityMatrixGyro250dpsShimmer3 = {{131,0,0},{0,131,0},{0,0,131}};
	public static final double[][] SensitivityMatrixGyro500dpsShimmer3 = {{65.5,0,0},{0,65.5,0},{0,0,65.5}};
	public static final double[][] SensitivityMatrixGyro1000dpsShimmer3 = {{32.8,0,0},{0,32.8,0},{0,0,32.8}};
	public static final double[][] SensitivityMatrixGyro2000dpsShimmer3 = {{16.4,0,0},{0,16.4,0},{0,0,16.4}};
	public static final double[][] AlignmentMatrixGyroShimmer3 = {{0,-1,0},{-1,0,0},{0,0,-1}}; 				
	public static final double[][] OffsetVectorGyroShimmer3 = {{0},{0},{0}};		

	public boolean mEnableOntheFlyGyroOVCal = false;

	public double mGyroXOVCalThreshold = 1.2;
	DescriptiveStatistics mGyroXX;
	DescriptiveStatistics mGyroXY;
	DescriptiveStatistics mGyroXZ;
	DescriptiveStatistics mGyroXRaw;
	DescriptiveStatistics mGyroXYRaw;
	DescriptiveStatistics mGyroXZRaw;
	public boolean mEnableXCalibration = true;
	public byte[] mInquiryResponseXBytes;
	
	public byte[] mGyroCalRawXParams  = new byte[22];
	public byte[] mMagCalRawXParams  = new byte[22];
		
	
	//These can be used to enable/disable GUI options depending on what HW, FW, Expansion boards versions are present
//	private static final ShimmerVerObject MPU9150 =new ShimmerVerObject(
//			HW_ID.SHIMMER_4,
//			ShimmerVerDetails.ANY_VERSION,
//			ShimmerVerDetails.ANY_VERSION,
//			ShimmerVerDetails.ANY_VERSION,
//			ShimmerVerDetails.ANY_VERSION,
//			ShimmerVerDetails.ANY_VERSION);
//	
//	private static final List<ShimmerVerObject> listOfCompatibleVersionMPU9150 = Arrays.asList(MPU9150);
	
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

	//--------- Sensor specific variables end --------------

	//--------- Bluetooth commands start --------------
	//--------- Bluetooth commands end --------------

	//--------- Configuration options start --------------
	//--------- Configuration options end --------------

	//--------- Sensor info start --------------
	public static final SensorDetailsRef sensorMpu9150GyroRef = new SensorDetailsRef(0x40<<(0*8), 0x40<<(0*8), Configuration.Shimmer3.GuiLabelSensors.GYRO,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW,
			Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_MPL_GYRO),
			Arrays.asList(
					Configuration.Shimmer3.GuiLabelConfig.MPU9150_GYRO_RANGE, 
					Configuration.Shimmer3.GuiLabelConfig.MPU9150_GYRO_RATE),
			Arrays.asList(
					Configuration.Shimmer3.ObjectClusterSensorName.GYRO_X, 
					Configuration.Shimmer3.ObjectClusterSensorName.GYRO_Y, 
					Configuration.Shimmer3.ObjectClusterSensorName.GYRO_Z),
			false);
	
	//TODO fill in the below Sensors from Configuration.Shimmer3
	public static final SensorDetailsRef sensorMpu9150AccelRef = new SensorDetailsRef(0x40<<(2*8), 0x40<<(2*8), Configuration.Shimmer3.GuiLabelSensors.ACCEL_MPU,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors,
			Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_MPL_ACCEL), 
			Arrays.asList(Configuration.Shimmer3.GuiLabelConfig.MPU9150_ACCEL_RANGE),
			Arrays.asList(
					Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_MPU_MPL_X,
					Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_MPU_MPL_Y,
					Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_MPU_MPL_Z),
			false);

	public static final SensorDetailsRef sensorMpu9150MagRef = new SensorDetailsRef(0x20<<(2*8), 0x20<<(2*8), Configuration.Shimmer3.GuiLabelSensors.MAG_MPU,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors,
			Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_MPL_MAG),
			Arrays.asList(Configuration.Shimmer3.GuiLabelConfig.MPU9150_MAG_RATE),
			Arrays.asList(
					Configuration.Shimmer3.ObjectClusterSensorName.MAG_MPU_X,
					Configuration.Shimmer3.ObjectClusterSensorName.MAG_MPU_Y,
					Configuration.Shimmer3.ObjectClusterSensorName.MAG_MPU_Z),
			false);
	
   // ------------ Check byte index for Temp----------------
//	public static final SensorDetailsRef sensorMpu9150TempRef = new SensorDetailsRef(0x02<<(streamingByteIndex*8), 0x02<<(logHeaderByteIndex*8), Shimmer3.GuiLabelSensors.MPL_TEMPERATURE));
//	public static final SensorDetailsRef sensorMpu9150TempRef = new SensorDetailsRef(0x02<<(2*8), 0x02<<(2*8), Shimmer3.GuiLabelSensors.MPL_TEMPERATURE,
//			CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors,
//			Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_TEMP), 
//			Arrays.asList(Configuration.Shimmer3.GuiLabelConfig.MPU9150_MPL_RATE),
//			Arrays.asList(
//					Configuration.Shimmer3.ObjectClusterSensorName.MPL_TEMPERATURE),
//			false);

	
	public static final SensorDetailsRef sensorMpu9150MplQuat6Dof = new SensorDetailsRef((long)0, (long)0x80<<(3*8), Configuration.Shimmer3.GuiLabelSensors.QUAT_MPL_6DOF,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors,
			Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_MPL_QUAT_6DOF),
			Arrays.asList(Configuration.Shimmer3.GuiLabelConfig.MPU9150_MPL_RATE),
			Arrays.asList(
					Configuration.Shimmer3.ObjectClusterSensorName.QUAT_MPL_6DOF_W,
					Configuration.Shimmer3.ObjectClusterSensorName.QUAT_MPL_6DOF_X,
					Configuration.Shimmer3.ObjectClusterSensorName.QUAT_MPL_6DOF_Y,
					Configuration.Shimmer3.ObjectClusterSensorName.QUAT_MPL_6DOF_Z),
			false);
	
	
	public static final SensorDetailsRef sensorMpu9150MplQuat9Dof = new SensorDetailsRef((long)0, (long)0x40<<(3*8), Configuration.Shimmer3.GuiLabelSensors.QUAT_MPL_9DOF,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors,
			Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_MPL_QUAT_9DOF),
			Arrays.asList(Configuration.Shimmer3.GuiLabelConfig.MPU9150_MPL_RATE),
			Arrays.asList(
					Configuration.Shimmer3.ObjectClusterSensorName.QUAT_MPL_9DOF_W,
					Configuration.Shimmer3.ObjectClusterSensorName.QUAT_MPL_9DOF_X,
					Configuration.Shimmer3.ObjectClusterSensorName.QUAT_MPL_9DOF_Y,
					Configuration.Shimmer3.ObjectClusterSensorName.QUAT_MPL_9DOF_Z),
			false);
	
	public static final SensorDetailsRef sensorMpu9150MplEuler6Dof = new SensorDetailsRef((long)0, (long)0x20<<(3*8), Configuration.Shimmer3.GuiLabelSensors.EULER_ANGLES_6DOF,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors,
			Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_MPL_EULER_6DOF),
			Arrays.asList(Configuration.Shimmer3.GuiLabelConfig.MPU9150_MPL_RATE),
			Arrays.asList(
					Configuration.Shimmer3.ObjectClusterSensorName.EULER_MPL_6DOF_X,
					Configuration.Shimmer3.ObjectClusterSensorName.EULER_MPL_6DOF_Y,
					Configuration.Shimmer3.ObjectClusterSensorName.EULER_MPL_6DOF_Z),
			false);
	
	public static final SensorDetailsRef sensorMpu9150MplEuler9Dof = new SensorDetailsRef((long)0, (long)0x10<<(3*8), Configuration.Shimmer3.GuiLabelSensors.EULER_ANGLES_9DOF,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors,
			Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_MPL_EULER_9DOF),
			Arrays.asList(Configuration.Shimmer3.GuiLabelConfig.MPU9150_MPL_RATE),
			Arrays.asList(
					Configuration.Shimmer3.ObjectClusterSensorName.EULER_MPL_9DOF_X,
					Configuration.Shimmer3.ObjectClusterSensorName.EULER_MPL_9DOF_Y,
					Configuration.Shimmer3.ObjectClusterSensorName.EULER_MPL_9DOF_Z),
			false);
	
	public static final SensorDetailsRef sensorMpu9150MplHeading = new SensorDetailsRef((long)0, (long)0x08<<(3*8), Configuration.Shimmer3.GuiLabelSensors.MPL_HEADING,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors,
			Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_MPL_HEADING),
			Arrays.asList(Configuration.Shimmer3.GuiLabelConfig.MPU9150_MPL_RATE),
			Arrays.asList(
					Configuration.Shimmer3.ObjectClusterSensorName.MPL_HEADING),
			false);
	
	public static final SensorDetailsRef sensorMpu9150MplPedometer = new SensorDetailsRef((long)0, (long)0x04<<(3*8), Configuration.Shimmer3.GuiLabelSensors.MPL_PEDOM_CNT,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors,
			Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_MPL_PEDOMETER),
			Arrays.asList(Configuration.Shimmer3.GuiLabelConfig.MPU9150_MPL_RATE),
			Arrays.asList(
					Configuration.Shimmer3.ObjectClusterSensorName.MPL_PEDOM_CNT,
					Configuration.Shimmer3.ObjectClusterSensorName.MPL_PEDOM_TIME),
			false);
	
	public static final SensorDetailsRef sensorMpu9150MplTap = new SensorDetailsRef((long)0, (long)0x02<<(3*8), Configuration.Shimmer3.GuiLabelSensors.MPL_TAPDIRANDTAPCNT,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors,
			Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_MPL_TAP),
			Arrays.asList(Configuration.Shimmer3.GuiLabelConfig.MPU9150_MPL_RATE),
			Arrays.asList(
					Configuration.Shimmer3.ObjectClusterSensorName.TAPDIRANDTAPCNT),
			false);
	
	public static final SensorDetailsRef sensorMpu9150MplMotion = new SensorDetailsRef((long)0, (long)0x01<<(3*8), Configuration.Shimmer3.GuiLabelSensors.MPL_MOTIONANDORIENT,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors,
			Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_MPL_MOTION_ORIENT),
			Arrays.asList(Configuration.Shimmer3.GuiLabelConfig.MPU9150_MPL_RATE),
			Arrays.asList(
					Configuration.Shimmer3.ObjectClusterSensorName.TAPDIRANDTAPCNT),
			false);

	//MPL calibrated sensors
	public static final SensorDetailsRef sensorMpu9150MplGyro = new SensorDetailsRef((long)0, (long)0x80<<(4*8), Configuration.Shimmer3.GuiLabelSensors.GYRO_MPU_MPL,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors,
			Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_GYRO),
			Arrays.asList(Configuration.Shimmer3.GuiLabelConfig.MPU9150_MPL_RATE),
			Arrays.asList(
					Configuration.Shimmer3.ObjectClusterSensorName.GYRO_MPU_MPL_X,
					Configuration.Shimmer3.ObjectClusterSensorName.GYRO_MPU_MPL_Y,
					Configuration.Shimmer3.ObjectClusterSensorName.GYRO_MPU_MPL_Z),
			false);

	public static final SensorDetailsRef sensorMpu9150MplAccel = new SensorDetailsRef((long)0, (long)0x40<<(4*8), Configuration.Shimmer3.GuiLabelSensors.ACCEL_MPU_MPL,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors,
			Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_ACCEL),
			Arrays.asList(Configuration.Shimmer3.GuiLabelConfig.MPU9150_MPL_RATE),
			Arrays.asList(
					Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_MPU_MPL_X,
					Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_MPU_MPL_Y,
					Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_MPU_MPL_Z),
			false);
	
	public static final SensorDetailsRef sensorMpu9150MplMag = new SensorDetailsRef((long)0, (long)0x20<<(4*8), Configuration.Shimmer3.GuiLabelSensors.MAG_MPU_MPL,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors,
			Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_MAG),
			Arrays.asList(Configuration.Shimmer3.GuiLabelConfig.MPU9150_MPL_RATE),
			Arrays.asList(
					Configuration.Shimmer3.ObjectClusterSensorName.MAG_MPU_MPL_X,
					Configuration.Shimmer3.ObjectClusterSensorName.MAG_MPU_MPL_Y,
					Configuration.Shimmer3.ObjectClusterSensorName.MAG_MPU_MPL_Z),
			false);
	
	public static final SensorDetailsRef sensorMpu9150MplQuat6DofRaw = new SensorDetailsRef((long)0, (long)0x10<<(4*8), Configuration.Shimmer3.GuiLabelSensors.QUAT_DMP_6DOF,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors,
			Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_MPL_QUAT_6DOF_RAW),
			Arrays.asList(Configuration.Shimmer3.GuiLabelConfig.MPU9150_MPL_RATE),
			Arrays.asList(
					Configuration.Shimmer3.ObjectClusterSensorName.QUAT_DMP_6DOF_W,
					Configuration.Shimmer3.ObjectClusterSensorName.QUAT_DMP_6DOF_X,
					Configuration.Shimmer3.ObjectClusterSensorName.QUAT_DMP_6DOF_Y,
					Configuration.Shimmer3.ObjectClusterSensorName.QUAT_DMP_6DOF_Z),
			false);

	
    public static final Map<Integer, SensorDetailsRef> mSensorMapRef;
    static {
        Map<Integer, SensorDetailsRef> aMap = new LinkedHashMap<Integer, SensorDetailsRef>();
		aMap.put(Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_GYRO, SensorMPU9X50.sensorMpu9150GyroRef);
		aMap.put(Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_ACCEL, SensorMPU9X50.sensorMpu9150AccelRef);
		aMap.put(Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_MAG,SensorMPU9X50.sensorMpu9150MagRef);

		//TODO decide what to do with below
//		Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_TEMP,

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
					Configuration.Shimmer3.ObjectClusterSensorName.GYRO_X,
					Configuration.Shimmer3.ObjectClusterSensorName.GYRO_X,
					DatabaseChannelHandles.GYRO_X,
					CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.MSB,
					CHANNEL_UNITS.DEGREES_PER_SECOND,
					Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
	public static final ChannelDetails channelGyroY = new ChannelDetails(
					Configuration.Shimmer3.ObjectClusterSensorName.GYRO_Y,
					Configuration.Shimmer3.ObjectClusterSensorName.GYRO_Y,
					DatabaseChannelHandles.GYRO_Y,
					CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.MSB,
					CHANNEL_UNITS.DEGREES_PER_SECOND,
					Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
	public static final ChannelDetails channelGyroZ = new ChannelDetails(
					Configuration.Shimmer3.ObjectClusterSensorName.GYRO_Z,
					Configuration.Shimmer3.ObjectClusterSensorName.GYRO_Z,
					DatabaseChannelHandles.GYRO_Z,
					CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.MSB,
					CHANNEL_UNITS.DEGREES_PER_SECOND,
					Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
	
	public static final ChannelDetails channelMpuMplX = new ChannelDetails(
					Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_MPU_MPL_X,
					Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_MPU_MPL_X,
					DatabaseChannelHandles.ALTERNATIVE_ACC_X,
					CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.MSB,
					CHANNEL_UNITS.METER_PER_SECOND_SQUARE,
					Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
	public static final ChannelDetails channelMpuMplY = new ChannelDetails(
					Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_MPU_MPL_Y,
					Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_MPU_MPL_Y,
					DatabaseChannelHandles.ALTERNATIVE_ACC_Y,
					CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.MSB,
					CHANNEL_UNITS.METER_PER_SECOND_SQUARE,
					Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
	public static final ChannelDetails channelMpuMplZ = new ChannelDetails(
					Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_MPU_MPL_Z,
					Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_MPU_MPL_Z,
					DatabaseChannelHandles.ALTERNATIVE_ACC_Z,
					CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.MSB,
					CHANNEL_UNITS.METER_PER_SECOND_SQUARE,
					Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));

	public static final ChannelDetails channelMagMpuX = new ChannelDetails(
					Configuration.Shimmer3.ObjectClusterSensorName.MAG_MPU_X,
					Configuration.Shimmer3.ObjectClusterSensorName.MAG_MPU_X,
					DatabaseChannelHandles.ALTERNATIVE_MAG_X,
					CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.LSB,
					CHANNEL_UNITS.LOCAL_FLUX,
					Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
	public static final ChannelDetails channelMagMpuY = new ChannelDetails(
					Configuration.Shimmer3.ObjectClusterSensorName.MAG_MPU_Y,
					Configuration.Shimmer3.ObjectClusterSensorName.MAG_MPU_Y,
					DatabaseChannelHandles.ALTERNATIVE_MAG_Y,
					CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.LSB,
					CHANNEL_UNITS.LOCAL_FLUX,
					Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
	public static final ChannelDetails channelMagMpuZ = new ChannelDetails(
					Configuration.Shimmer3.ObjectClusterSensorName.MAG_MPU_Z,
					Configuration.Shimmer3.ObjectClusterSensorName.MAG_MPU_Z,
					DatabaseChannelHandles.ALTERNATIVE_MAG_Z,
					CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.LSB,
					CHANNEL_UNITS.LOCAL_FLUX,
					Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
	
	// MPL Quaternions 6DOF
	public static final ChannelDetails channelQuatMpl6DofW = new ChannelDetails(
					Configuration.Shimmer3.ObjectClusterSensorName.QUAT_MPL_6DOF_W,
					Configuration.Shimmer3.ObjectClusterSensorName.QUAT_MPL_6DOF_W,
					DatabaseChannelHandles.MPU_QUAT_6DOF_W,
					CHANNEL_DATA_TYPE.INT32, 4, CHANNEL_DATA_ENDIAN.MSB,
					CHANNEL_UNITS.NO_UNITS,
					Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
	public static final ChannelDetails channelQuatMpl6DofX = new ChannelDetails(
					Configuration.Shimmer3.ObjectClusterSensorName.QUAT_MPL_6DOF_X,
					Configuration.Shimmer3.ObjectClusterSensorName.QUAT_MPL_6DOF_X,
					DatabaseChannelHandles.MPU_QUAT_6DOF_X,
					CHANNEL_DATA_TYPE.INT32, 4, CHANNEL_DATA_ENDIAN.MSB,
					CHANNEL_UNITS.NO_UNITS,
					Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
	public static final ChannelDetails channelQuatMpl6DofY = new ChannelDetails(
					Configuration.Shimmer3.ObjectClusterSensorName.QUAT_MPL_6DOF_Y,
					Configuration.Shimmer3.ObjectClusterSensorName.QUAT_MPL_6DOF_Y,
					DatabaseChannelHandles.MPU_QUAT_6DOF_Y,
					CHANNEL_DATA_TYPE.INT32, 4, CHANNEL_DATA_ENDIAN.MSB,
					CHANNEL_UNITS.NO_UNITS,
					Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
	public static final ChannelDetails channelQuatMpl6DofZ = new ChannelDetails(
					Configuration.Shimmer3.ObjectClusterSensorName.QUAT_MPL_6DOF_Z,
					Configuration.Shimmer3.ObjectClusterSensorName.QUAT_MPL_6DOF_Z,
					DatabaseChannelHandles.MPU_QUAT_6DOF_Z,
					CHANNEL_DATA_TYPE.INT32, 4, CHANNEL_DATA_ENDIAN.MSB,
					CHANNEL_UNITS.NO_UNITS,
					Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));

	// MPL Quaternions 9DOF
	public static final ChannelDetails channelQuatMpl9DofW = new ChannelDetails(
					Configuration.Shimmer3.ObjectClusterSensorName.QUAT_MPL_9DOF_W,
					Configuration.Shimmer3.ObjectClusterSensorName.QUAT_MPL_9DOF_W,
					DatabaseChannelHandles.MPU_QUAT_9DOF_W,
					CHANNEL_DATA_TYPE.INT32, 4, CHANNEL_DATA_ENDIAN.MSB,
					CHANNEL_UNITS.NO_UNITS,
					Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
	public static final ChannelDetails channelQuatMpl9DofX = new ChannelDetails(
					Configuration.Shimmer3.ObjectClusterSensorName.QUAT_MPL_9DOF_X,
					Configuration.Shimmer3.ObjectClusterSensorName.QUAT_MPL_9DOF_X,
					DatabaseChannelHandles.MPU_QUAT_9DOF_X,
					CHANNEL_DATA_TYPE.INT32, 4, CHANNEL_DATA_ENDIAN.MSB,
					CHANNEL_UNITS.NO_UNITS,
					Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
	public static final ChannelDetails channelQuatMpl9DofY = new ChannelDetails(
					Configuration.Shimmer3.ObjectClusterSensorName.QUAT_MPL_9DOF_Y,
					Configuration.Shimmer3.ObjectClusterSensorName.QUAT_MPL_9DOF_Y,
					DatabaseChannelHandles.MPU_QUAT_9DOF_Y,
					CHANNEL_DATA_TYPE.INT32, 4, CHANNEL_DATA_ENDIAN.MSB,
					CHANNEL_UNITS.NO_UNITS,
					Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
	public static final ChannelDetails channelQuatMpl9DofZ = new ChannelDetails(
					Configuration.Shimmer3.ObjectClusterSensorName.QUAT_MPL_9DOF_Z,
					Configuration.Shimmer3.ObjectClusterSensorName.QUAT_MPL_9DOF_Z,
					DatabaseChannelHandles.MPU_QUAT_9DOF_Z,
					CHANNEL_DATA_TYPE.INT32, 4, CHANNEL_DATA_ENDIAN.MSB,
					CHANNEL_UNITS.NO_UNITS,
					Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
	
	// MPL Euler
	public static final ChannelDetails channelEulerMpl6DofX = new ChannelDetails(
					Configuration.Shimmer3.ObjectClusterSensorName.EULER_MPL_6DOF_X,
					Configuration.Shimmer3.ObjectClusterSensorName.EULER_MPL_6DOF_X,
					DatabaseChannelHandles.MPU_EULER_6DOF_X,
					CHANNEL_DATA_TYPE.INT32, 4, CHANNEL_DATA_ENDIAN.MSB,
					CHANNEL_UNITS.NO_UNITS,
					Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
	public static final ChannelDetails channelEulerMpl6DofY = new ChannelDetails(
					Configuration.Shimmer3.ObjectClusterSensorName.EULER_MPL_6DOF_Y,
					Configuration.Shimmer3.ObjectClusterSensorName.EULER_MPL_6DOF_Y,
					DatabaseChannelHandles.MPU_EULER_6DOF_Y,
					CHANNEL_DATA_TYPE.INT32, 4, CHANNEL_DATA_ENDIAN.MSB,
					CHANNEL_UNITS.NO_UNITS,
					Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
	public static final ChannelDetails channelEulerMpl6DofZ = new ChannelDetails(
					Configuration.Shimmer3.ObjectClusterSensorName.EULER_MPL_6DOF_Z,
					Configuration.Shimmer3.ObjectClusterSensorName.EULER_MPL_6DOF_Z,
					DatabaseChannelHandles.MPU_EULER_6DOF_Z,
					CHANNEL_DATA_TYPE.INT32, 4, CHANNEL_DATA_ENDIAN.MSB,
					CHANNEL_UNITS.NO_UNITS,
					Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));

	public static final ChannelDetails channelEulerMpl9DofX = new ChannelDetails(
					Configuration.Shimmer3.ObjectClusterSensorName.EULER_MPL_9DOF_X,
					Configuration.Shimmer3.ObjectClusterSensorName.EULER_MPL_9DOF_X,
					DatabaseChannelHandles.MPU_EULER_9DOF_X,
					CHANNEL_DATA_TYPE.INT32, 4, CHANNEL_DATA_ENDIAN.MSB,
					CHANNEL_UNITS.NO_UNITS,
					Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
	public static final ChannelDetails channelEulerMpl9DofY = new ChannelDetails(
					Configuration.Shimmer3.ObjectClusterSensorName.EULER_MPL_9DOF_Y,
					Configuration.Shimmer3.ObjectClusterSensorName.EULER_MPL_9DOF_Y,
					DatabaseChannelHandles.MPU_EULER_9DOF_Y,
					CHANNEL_DATA_TYPE.INT32, 4, CHANNEL_DATA_ENDIAN.MSB,
					CHANNEL_UNITS.NO_UNITS,
					Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
	public static final ChannelDetails channelEulerMpl9DofZ = new ChannelDetails(
					Configuration.Shimmer3.ObjectClusterSensorName.EULER_MPL_9DOF_Z,
					Configuration.Shimmer3.ObjectClusterSensorName.EULER_MPL_9DOF_Z,
					DatabaseChannelHandles.MPU_EULER_9DOF_Z,
					CHANNEL_DATA_TYPE.INT32, 4, CHANNEL_DATA_ENDIAN.MSB,
					CHANNEL_UNITS.NO_UNITS,
					Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));

	// MPL Heading
	public static final ChannelDetails channelMplHeading = new ChannelDetails(
					Configuration.Shimmer3.ObjectClusterSensorName.MPL_HEADING,
					Configuration.Shimmer3.ObjectClusterSensorName.MPL_HEADING,
					DatabaseChannelHandles.MPU_HEADING,
					CHANNEL_DATA_TYPE.INT32, 4, CHANNEL_DATA_ENDIAN.MSB,
					CHANNEL_UNITS.DEGREES,
					Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));

	// MPU9150 Temperature
	public static final ChannelDetails channelMplTemperature = new ChannelDetails(
					Configuration.Shimmer3.ObjectClusterSensorName.MPL_TEMPERATURE,
					Configuration.Shimmer3.ObjectClusterSensorName.MPL_TEMPERATURE,
					DatabaseChannelHandles.MPU_TEMP,
					CHANNEL_DATA_TYPE.INT32, 4, CHANNEL_DATA_ENDIAN.MSB,
					CHANNEL_UNITS.DEGREES_CELSUIS,
					Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
	
	// MPL Pedometer
	public static final ChannelDetails channelMplPedomCount = new ChannelDetails(
					Configuration.Shimmer3.ObjectClusterSensorName.MPL_PEDOM_CNT,
					Configuration.Shimmer3.ObjectClusterSensorName.MPL_PEDOM_CNT,
					DatabaseChannelHandles.PEDOMETER_CNT,
					CHANNEL_DATA_TYPE.UINT32, 4, CHANNEL_DATA_ENDIAN.MSB,
					CHANNEL_UNITS.NO_UNITS,
					Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
	public static final ChannelDetails channelMplPedomTime = new ChannelDetails(
					Configuration.Shimmer3.ObjectClusterSensorName.MPL_PEDOM_TIME,
					Configuration.Shimmer3.ObjectClusterSensorName.MPL_PEDOM_TIME,
					DatabaseChannelHandles.PEDOMETER_TIME,
					CHANNEL_DATA_TYPE.UINT32, 4, CHANNEL_DATA_ENDIAN.MSB,
					CHANNEL_UNITS.NO_UNITS,
					Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));

	// MPL Tap
	public static final ChannelDetails channelMplTapDirAndTapCnt = new ChannelDetails(
					Configuration.Shimmer3.ObjectClusterSensorName.TAPDIRANDTAPCNT,
					Configuration.Shimmer3.ObjectClusterSensorName.TAPDIRANDTAPCNT,
					DatabaseChannelHandles.TAP_DIR_AND_CNT,
					CHANNEL_DATA_TYPE.UINT8, 1, CHANNEL_DATA_ENDIAN.MSB,
					CHANNEL_UNITS.NO_UNITS,
					Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));

	// MPL Motion Orient
	public static final ChannelDetails channelMplMotionAndOrient = new ChannelDetails(
					Configuration.Shimmer3.ObjectClusterSensorName.MOTIONANDORIENT,
					Configuration.Shimmer3.ObjectClusterSensorName.MOTIONANDORIENT,
					DatabaseChannelHandles.MOTION_AND_ORIENT,
					CHANNEL_DATA_TYPE.UINT8, 1, CHANNEL_DATA_ENDIAN.MSB,
					CHANNEL_UNITS.NO_UNITS,
					Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));

	// MPL Gyro Calibrated
	public static final ChannelDetails channelGyroMpuMplX = new ChannelDetails(
					Configuration.Shimmer3.ObjectClusterSensorName.GYRO_MPU_MPL_X,
					Configuration.Shimmer3.ObjectClusterSensorName.GYRO_MPU_MPL_X,
					DatabaseChannelHandles.MPU_MPL_GYRO_X,
					CHANNEL_DATA_TYPE.INT32, 4, CHANNEL_DATA_ENDIAN.MSB,
					CHANNEL_UNITS.DEGREES_PER_SECOND,
					Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
	public static final ChannelDetails channelGyroMpuMplY = new ChannelDetails(
					Configuration.Shimmer3.ObjectClusterSensorName.GYRO_MPU_MPL_Y,
					Configuration.Shimmer3.ObjectClusterSensorName.GYRO_MPU_MPL_Y,
					DatabaseChannelHandles.MPU_MPL_GYRO_Y,
					CHANNEL_DATA_TYPE.INT32, 4, CHANNEL_DATA_ENDIAN.MSB,
					CHANNEL_UNITS.DEGREES_PER_SECOND,
					Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
	public static final ChannelDetails channelGyroMpuMplZ = new ChannelDetails(
					Configuration.Shimmer3.ObjectClusterSensorName.GYRO_MPU_MPL_Z,
					Configuration.Shimmer3.ObjectClusterSensorName.GYRO_MPU_MPL_Z,
					DatabaseChannelHandles.MPU_MPL_GYRO_Z,
					CHANNEL_DATA_TYPE.INT32, 4, CHANNEL_DATA_ENDIAN.MSB,
					CHANNEL_UNITS.DEGREES_PER_SECOND,
					Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));

	// MPL Accelerometer Calibrated
	public static final ChannelDetails channelAccelMpuMplX = new ChannelDetails(
					Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_MPU_MPL_X,
					Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_MPU_MPL_X,
					DatabaseChannelHandles.MPU_MPL_ACC_X,
					CHANNEL_DATA_TYPE.INT32, 4, CHANNEL_DATA_ENDIAN.MSB,
					CHANNEL_UNITS.GRAVITY,
					Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
	public static final ChannelDetails channelAccelMpuMplY = new ChannelDetails(
					Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_MPU_MPL_Y,
					Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_MPU_MPL_Y,
					DatabaseChannelHandles.MPU_MPL_ACC_Y,
					CHANNEL_DATA_TYPE.INT32, 4, CHANNEL_DATA_ENDIAN.MSB,
					CHANNEL_UNITS.GRAVITY,
					Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
	public static final ChannelDetails channelAccelMpuMplZ = new ChannelDetails(
					Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_MPU_MPL_Z,
					Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_MPU_MPL_Z,
					DatabaseChannelHandles.MPU_MPL_ACC_Z,
					CHANNEL_DATA_TYPE.INT32, 4, CHANNEL_DATA_ENDIAN.MSB,
					CHANNEL_UNITS.GRAVITY,
					Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));

	// MPL Magnetometer Calibrated
	public static final ChannelDetails channelMagMpuMplX = new ChannelDetails(
					Configuration.Shimmer3.ObjectClusterSensorName.MAG_MPU_MPL_X,
					Configuration.Shimmer3.ObjectClusterSensorName.MAG_MPU_MPL_X,
					DatabaseChannelHandles.MPU_MPL_MAG_X,
					CHANNEL_DATA_TYPE.INT32, 4, CHANNEL_DATA_ENDIAN.MSB,
					CHANNEL_UNITS.U_TESLA,
					Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
	public static final ChannelDetails channelMagMpuMplY = new ChannelDetails(
					Configuration.Shimmer3.ObjectClusterSensorName.MAG_MPU_MPL_Y,
					Configuration.Shimmer3.ObjectClusterSensorName.MAG_MPU_MPL_Y,
					DatabaseChannelHandles.MPU_MPL_MAG_Y,
					CHANNEL_DATA_TYPE.INT32, 4, CHANNEL_DATA_ENDIAN.MSB,
					CHANNEL_UNITS.U_TESLA,
					Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
	public static final ChannelDetails channelMagMpuMplZ = new ChannelDetails(
					Configuration.Shimmer3.ObjectClusterSensorName.MAG_MPU_MPL_Z,
					Configuration.Shimmer3.ObjectClusterSensorName.MAG_MPU_MPL_Z,
					DatabaseChannelHandles.MPU_MPL_MAG_Z,
					CHANNEL_DATA_TYPE.INT32, 4, CHANNEL_DATA_ENDIAN.MSB,
					CHANNEL_UNITS.U_TESLA,
					Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
	
	// Raw 6DOF Quaterian's from the DMP hardware module of the MPU9150
	public static final ChannelDetails channelQuatDmp6DofW = new ChannelDetails(
					Configuration.Shimmer3.ObjectClusterSensorName.QUAT_DMP_6DOF_W,
					Configuration.Shimmer3.ObjectClusterSensorName.QUAT_DMP_6DOF_W,
					DatabaseChannelHandles.MPU_QUAT_6DOF_DMP_W,
					CHANNEL_DATA_TYPE.INT32, 4, CHANNEL_DATA_ENDIAN.MSB,
					CHANNEL_UNITS.NO_UNITS,
					Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
	public static final ChannelDetails channelQuatDmp6DofX = new ChannelDetails(
					Configuration.Shimmer3.ObjectClusterSensorName.QUAT_DMP_6DOF_X,
					Configuration.Shimmer3.ObjectClusterSensorName.QUAT_DMP_6DOF_X,
					DatabaseChannelHandles.MPU_QUAT_6DOF_DMP_X,
					CHANNEL_DATA_TYPE.INT32, 4, CHANNEL_DATA_ENDIAN.MSB,
					CHANNEL_UNITS.NO_UNITS,
					Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
	public static final ChannelDetails channelQuatDmp6DofY = new ChannelDetails(
					Configuration.Shimmer3.ObjectClusterSensorName.QUAT_DMP_6DOF_Y,
					Configuration.Shimmer3.ObjectClusterSensorName.QUAT_DMP_6DOF_Y,
					DatabaseChannelHandles.MPU_QUAT_6DOF_DMP_Y,
					CHANNEL_DATA_TYPE.INT32, 4, CHANNEL_DATA_ENDIAN.MSB,
					CHANNEL_UNITS.NO_UNITS,
					Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
	public static final ChannelDetails channelQuatDmp6DofZ = new ChannelDetails(
					Configuration.Shimmer3.ObjectClusterSensorName.QUAT_DMP_6DOF_Z,
					Configuration.Shimmer3.ObjectClusterSensorName.QUAT_DMP_6DOF_Z,
					DatabaseChannelHandles.MPU_QUAT_6DOF_DMP_Z,
					CHANNEL_DATA_TYPE.INT32, 4, CHANNEL_DATA_ENDIAN.MSB,
					CHANNEL_UNITS.NO_UNITS,
					Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
	
    public static final Map<String, ChannelDetails> mChannelMapRef;
    static {
        Map<String, ChannelDetails> aMap = new LinkedHashMap<String, ChannelDetails>();
		// MPU9150 Gyro
		aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.GYRO_X, SensorMPU9X50.channelGyroX);
		aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.GYRO_Y, SensorMPU9X50.channelGyroY);
		aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.GYRO_Z, SensorMPU9X50.channelGyroZ);
		
		aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_MPU_MPL_X, SensorMPU9X50.channelMpuMplX);
		aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_MPU_MPL_Y, SensorMPU9X50.channelMpuMplY);
		aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_MPU_MPL_Z, SensorMPU9X50.channelMpuMplZ);

		aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.MAG_MPU_X, SensorMPU9X50.channelMagMpuX);
		aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.MAG_MPU_Y, SensorMPU9X50.channelMagMpuY);
		aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.MAG_MPU_Z, SensorMPU9X50.channelMagMpuZ);

		// MPL Quaternions 6DOF
		aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.QUAT_MPL_6DOF_W, SensorMPU9X50.channelQuatMpl6DofW);
		aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.QUAT_MPL_6DOF_X, SensorMPU9X50.channelQuatMpl6DofX);
		aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.QUAT_MPL_6DOF_Y, SensorMPU9X50.channelQuatMpl6DofY);
		aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.QUAT_MPL_6DOF_Z, SensorMPU9X50.channelQuatMpl6DofZ);

		// MPL Quaternions 9DOF
		aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.QUAT_MPL_9DOF_W, SensorMPU9X50.channelQuatMpl9DofW);
		aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.QUAT_MPL_9DOF_X, SensorMPU9X50.channelQuatMpl9DofX);
		aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.QUAT_MPL_9DOF_Y, SensorMPU9X50.channelQuatMpl9DofY);
		aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.QUAT_MPL_9DOF_Z, SensorMPU9X50.channelQuatMpl9DofZ);
		
		// MPL Euler
		aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.EULER_MPL_6DOF_X, SensorMPU9X50.channelEulerMpl6DofX);
		aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.EULER_MPL_6DOF_Y, SensorMPU9X50.channelEulerMpl6DofY);
		aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.EULER_MPL_6DOF_Z, SensorMPU9X50.channelEulerMpl6DofZ);
		
		aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.EULER_MPL_9DOF_X, SensorMPU9X50.channelEulerMpl9DofX);
		aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.EULER_MPL_9DOF_Y, SensorMPU9X50.channelEulerMpl9DofY);
		aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.EULER_MPL_9DOF_Z, SensorMPU9X50.channelEulerMpl9DofZ);

		// MPL Heading
		aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.MPL_HEADING, SensorMPU9X50.channelMplHeading);

		// MPU9150 Temperature
		aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.MPL_TEMPERATURE, SensorMPU9X50.channelMplTemperature);

		// MPL Pedometer
		aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.MPL_PEDOM_CNT, SensorMPU9X50.channelMplPedomCount);
		aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.MPL_PEDOM_TIME, SensorMPU9X50.channelMplPedomTime);

		// MPL Tap
		aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.TAPDIRANDTAPCNT, SensorMPU9X50.channelMplTapDirAndTapCnt);

		// MPL Motion Orient
		aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.MOTIONANDORIENT, SensorMPU9X50.channelMplMotionAndOrient);

		// MPL Gyro Calibrated
		aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.GYRO_MPU_MPL_X, SensorMPU9X50.channelGyroMpuMplX);
		aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.GYRO_MPU_MPL_Y, SensorMPU9X50.channelGyroMpuMplY);
		aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.GYRO_MPU_MPL_Z, SensorMPU9X50.channelGyroMpuMplZ);

		// MPL Accelerometer Calibrated
		aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_MPU_MPL_X, SensorMPU9X50.channelAccelMpuMplX);
		aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_MPU_MPL_Y, SensorMPU9X50.channelAccelMpuMplY);
		aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_MPU_MPL_Z, SensorMPU9X50.channelAccelMpuMplZ);

		// MPL Magnetometer Calibrated
		aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.MAG_MPU_MPL_X, SensorMPU9X50.channelMagMpuMplX);
		aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.MAG_MPU_MPL_Y, SensorMPU9X50.channelMagMpuMplY);
		aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.MAG_MPU_MPL_Z, SensorMPU9X50.channelMagMpuMplZ);
		
		// Raw 6DOF Quaterian's from the DMP hardware module of the MPU9150
		aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.QUAT_DMP_6DOF_W, SensorMPU9X50.channelQuatDmp6DofW);
		aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.QUAT_DMP_6DOF_X, SensorMPU9X50.channelQuatDmp6DofX);
		aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.QUAT_DMP_6DOF_Y, SensorMPU9X50.channelQuatDmp6DofY);
		aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.QUAT_DMP_6DOF_Z, SensorMPU9X50.channelQuatDmp6DofZ);
		mChannelMapRef = Collections.unmodifiableMap(aMap);
    }

	//--------- Channel info end --------------


	/** Constructor for this class
	 * @param svo
	 */
	public SensorMPU9X50(ShimmerVerObject svo){
		super(svo);
		mSensorName = SENSORS.MPU9X50.toString();
	}


	@Override
	public void generateSensorMap(ShimmerVerObject svo) {
		//TODO populate the other channels depending on firmware version
		super.updateSensorMap(mSensorMapRef, mChannelMapRef);
	}
	
	@Override
	public void generateConfigOptionsMap(ShimmerVerObject svo) {
		mConfigOptionsMap.clear();
		
//		if (svo.mFirmwareIdentifier == ShimmerVerDetails.FW_ID.BTSTREAM 
//				|| svo.mFirmwareIdentifier == ShimmerVerDetails.FW_ID.SDLOG
//				|| svo.mFirmwareIdentifier == ShimmerVerDetails.FW_ID.GQ_802154) {
			
			mConfigOptionsMap.put(Configuration.Shimmer3.GuiLabelConfig.MPU9150_GYRO_RANGE, 
					new SensorConfigOptionDetails(Configuration.Shimmer3.ListofGyroRange, 
											Configuration.Shimmer3.ListofMPU9150GyroRangeConfigValues, 
											SensorConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX,
											CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW));
			//MPL Options
			mConfigOptionsMap.put(Configuration.Shimmer3.GuiLabelConfig.MPU9150_ACCEL_RANGE, 
					new SensorConfigOptionDetails(Configuration.Shimmer3.ListofMPU9150AccelRange, 
											Configuration.Shimmer3.ListofMPU9150AccelRangeConfigValues, 
											SensorConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX,
											CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors));
			mConfigOptionsMap.put(Configuration.Shimmer3.GuiLabelConfig.MPU9150_DMP_GYRO_CAL, 
					new SensorConfigOptionDetails(Configuration.Shimmer3.ListofMPU9150MplCalibrationOptions, 
											Configuration.Shimmer3.ListofMPU9150MplCalibrationOptionsConfigValues, 
											SensorConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX,
											CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors));
			mConfigOptionsMap.put(Configuration.Shimmer3.GuiLabelConfig.MPU9150_MPL_LPF, 
					new SensorConfigOptionDetails(Configuration.Shimmer3.ListofMPU9150MplLpfOptions, 
											Configuration.Shimmer3.ListofMPU9150MplLpfOptionsConfigValues, 
											SensorConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX,
											CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors));
			mConfigOptionsMap.put(Configuration.Shimmer3.GuiLabelConfig.MPU9150_MPL_RATE, 
					new SensorConfigOptionDetails(Configuration.Shimmer3.ListofMPU9150MplRate, 
											Configuration.Shimmer3.ListofMPU9150MplRateConfigValues, 
											SensorConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX,
											CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors));
			mConfigOptionsMap.put(Configuration.Shimmer3.GuiLabelConfig.MPU9150_MAG_RATE, 
					new SensorConfigOptionDetails(Configuration.Shimmer3.ListofMPU9150MagRate, 
											Configuration.Shimmer3.ListofMPU9150MagRateConfigValues, 
											SensorConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX,
											CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors));
			
			//MPL CheckBoxes
			mConfigOptionsMap.put(Configuration.Shimmer3.GuiLabelConfig.MPU9150_DMP, 
					new SensorConfigOptionDetails(SensorConfigOptionDetails.GUI_COMPONENT_TYPE.CHECKBOX,CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors));
			mConfigOptionsMap.put(Configuration.Shimmer3.GuiLabelConfig.MPU9150_MPL, 
					new SensorConfigOptionDetails(SensorConfigOptionDetails.GUI_COMPONENT_TYPE.CHECKBOX,CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors));
			mConfigOptionsMap.put(Configuration.Shimmer3.GuiLabelConfig.MPU9150_MPL_9DOF_SENSOR_FUSION, 
					new SensorConfigOptionDetails(SensorConfigOptionDetails.GUI_COMPONENT_TYPE.CHECKBOX,CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors));
			mConfigOptionsMap.put(Configuration.Shimmer3.GuiLabelConfig.MPU9150_MPL_GYRO_CAL, 
					new SensorConfigOptionDetails(SensorConfigOptionDetails.GUI_COMPONENT_TYPE.CHECKBOX,CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors));
			mConfigOptionsMap.put(Configuration.Shimmer3.GuiLabelConfig.MPU9150_MPL_VECTOR_CAL, 
					new SensorConfigOptionDetails(SensorConfigOptionDetails.GUI_COMPONENT_TYPE.CHECKBOX,CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors));
			mConfigOptionsMap.put(Configuration.Shimmer3.GuiLabelConfig.MPU9150_MPL_MAG_CAL, 
					new SensorConfigOptionDetails(SensorConfigOptionDetails.GUI_COMPONENT_TYPE.CHECKBOX,CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors));
			
			//General Config
			mConfigOptionsMap.put(Configuration.Shimmer3.GuiLabelConfig.MPU9150_GYRO_RATE, 
					new SensorConfigOptionDetails(SensorConfigOptionDetails.GUI_COMPONENT_TYPE.TEXTFIELD,CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW));
			mConfigOptionsMap.put(Configuration.Shimmer3.GuiLabelConfig.MPU9150_GYRO_LPM, 
					new SensorConfigOptionDetails(SensorConfigOptionDetails.GUI_COMPONENT_TYPE.CHECKBOX,CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW));
//		}
	}

	@Override
	public void generateSensorGroupMapping(ShimmerVerObject svo) {
		mSensorGroupingMap = new LinkedHashMap<String, SensorGroupingDetails>();
		
		if((svo.mHardwareVersion==HW_ID.SHIMMER_3 && svo.getFirmwareIdentifier()==FW_ID.SDLOG) //){
				|| svo.mHardwareVersion==HW_ID.SHIMMER_4_SDK){
			mSensorGroupingMap.put(Configuration.Shimmer3.GuiLabelSensorTiles.MPU_ACCEL_GYRO_MAG, new SensorGroupingDetails(
					Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_MPL_ACCEL,
							Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_MPL_GYRO,
							Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_MPL_MAG)));
			mSensorGroupingMap.get(Configuration.Shimmer3.GuiLabelSensorTiles.MPU_ACCEL_GYRO_MAG).mListOfCompatibleVersionInfo = CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors;
			
			mSensorGroupingMap.put(Configuration.Shimmer3.GuiLabelSensorTiles.MPU_OTHER, new SensorGroupingDetails(
					Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_TEMP,
								Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_MPL_QUAT_6DOF)));
			mSensorGroupingMap.get(Configuration.Shimmer3.GuiLabelSensorTiles.MPU_OTHER).mListOfCompatibleVersionInfo = CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors;
		}
		else {
			mSensorGroupingMap.put(Configuration.Shimmer3.GuiLabelSensorTiles.GYRO, new SensorGroupingDetails(
					Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_GYRO)));
			mSensorGroupingMap.get(Configuration.Shimmer3.GuiLabelSensorTiles.GYRO).mListOfCompatibleVersionInfo = CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW;
		}
		super.updateSensorGroupingMap();
	}
	

//	@Override
//	public ObjectCluster processData(byte[] rawData, COMMUNICATION_TYPE commType, ObjectCluster object) {
//		
//		int index = 0;
//		for (ChannelDetails channelDetails:mMapOfChannelDetails.get(commType).values()){
//			//first process the data originating from the Shimmer sensor
//			byte[] channelByteArray = new byte[channelDetails.mDefaultNumBytes];
//			System.arraycopy(rawData, index, channelByteArray, 0, channelDetails.mDefaultNumBytes);
//			object = processShimmerChannelData(rawData, channelDetails, object);
//		}
//		
////		if (channelDetails.mObjectClusterName.equals(Configuration.Shimmer3.ObjectClusterSensorName.GSR)){
//////			ObjectCluster objectCluster = (ObjectCluster) object;
////			double rawXData = ((FormatCluster)ObjectCluster.returnFormatCluster(object.mPropertyCluster.get(channelDetails.mObjectClusterName), ChannelDetails.channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
////			  
////		}
//		return object;
//	}

	@Override
	public void infoMemByteArrayGenerate(ShimmerDevice shimmerDevice, byte[] mInfoMemBytes) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void infoMemByteArrayParse(ShimmerDevice shimmerDevice, byte[] mInfoMemBytes) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Object setConfigValueUsingConfigLabel(String componentName, Object valueToSet) {

		Object returnValue = null;
		int buf = 0;

		switch(componentName){
//Booleans
			case(Configuration.Shimmer3.GuiLabelConfig.MPU9150_DMP):
		    	setMPU9150DMP((boolean)valueToSet);
		    	break;
			case(Configuration.Shimmer3.GuiLabelConfig.MPU9150_MPL):
		    	setMPLEnable((boolean)valueToSet);
		    	break;
			case(Configuration.Shimmer3.GuiLabelConfig.MPU9150_MPL_9DOF_SENSOR_FUSION):
		    	setMPLSensorFusion((boolean)valueToSet);
		    	break;
			case(Configuration.Shimmer3.GuiLabelConfig.MPU9150_MPL_GYRO_CAL):
		    	setMPLGyroCalTC((boolean)valueToSet);
		    	break;
			case(Configuration.Shimmer3.GuiLabelConfig.MPU9150_MPL_VECTOR_CAL):
		    	setMPLVectCompCal((boolean)valueToSet);
		    	break;
			case(Configuration.Shimmer3.GuiLabelConfig.MPU9150_MPL_MAG_CAL):
		    	setMPLMagDistCal((boolean)valueToSet);
		    	break;
//Integers
			case(Configuration.Shimmer3.GuiLabelConfig.MPU9150_GYRO_RANGE):
	        	setMPU9150GyroRange((int)valueToSet);
	        	break;

			case(Configuration.Shimmer3.GuiLabelConfig.MPU9150_ACCEL_RANGE):
				setMPU9150AccelRange((int)valueToSet);
	        	break;
			case(Configuration.Shimmer3.GuiLabelConfig.MPU9150_DMP_GYRO_CAL):
				setMPU9150MotCalCfg((int)valueToSet);
	        	break;
			case(Configuration.Shimmer3.GuiLabelConfig.MPU9150_MPL_LPF):
				setMPU9150LPF((int)valueToSet);
	        	break;
			case(Configuration.Shimmer3.GuiLabelConfig.MPU9150_MPL_RATE):
				setMPU9150MPLSamplingRate((int)valueToSet);
	        	break;
			case(Configuration.Shimmer3.GuiLabelConfig.MPU9150_MAG_RATE):
				setMPU9150MagSamplingRate((int)valueToSet);
	        	break;

//Strings
			case(Configuration.Shimmer3.GuiLabelConfig.MPU9150_GYRO_RATE):
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
	        default:
//	        	returnValue = super.setConfigValueUsingConfigLabel(componentName, valueToSet);
	        	break;
		}
		return returnValue;
	}

	@Override
	public Object getConfigValueUsingConfigLabel(String componentName) {
		Object returnValue = null;
		switch(componentName){
//Booleans
			case(Configuration.Shimmer3.GuiLabelConfig.MPU9150_DMP):
				returnValue = isMPU9150DMP();
	        	break;
			case(Configuration.Shimmer3.GuiLabelConfig.MPU9150_MPL):
				returnValue = isMPLEnable();
	        	break;
			case(Configuration.Shimmer3.GuiLabelConfig.MPU9150_MPL_9DOF_SENSOR_FUSION):
				returnValue = isMPLSensorFusion();
	        	break;
			case(Configuration.Shimmer3.GuiLabelConfig.MPU9150_MPL_GYRO_CAL):
				returnValue = isMPLGyroCalTC();
	        	break;
			case(Configuration.Shimmer3.GuiLabelConfig.MPU9150_MPL_VECTOR_CAL):
				returnValue = isMPLVectCompCal();
	        	break;
			case(Configuration.Shimmer3.GuiLabelConfig.MPU9150_MPL_MAG_CAL):
				returnValue = isMPLMagDistCal();
	        	break;

//Integers
			case(Configuration.Shimmer3.GuiLabelConfig.MPU9150_GYRO_RANGE):
				returnValue = getGyroRange();
	        	break;

			case(Configuration.Shimmer3.GuiLabelConfig.MPU9150_ACCEL_RANGE):
				returnValue = getMPU9150AccelRange();
		    	break;
			case(Configuration.Shimmer3.GuiLabelConfig.MPU9150_DMP_GYRO_CAL):
				returnValue = getMPU9150MotCalCfg();
		    	break;
			case(Configuration.Shimmer3.GuiLabelConfig.MPU9150_MPL_LPF):
				returnValue = getMPU9150LPF();
		    	break;
			case(Configuration.Shimmer3.GuiLabelConfig.MPU9150_MPL_RATE):
				returnValue = getMPU9150MPLSamplingRate();
				break;
			case(Configuration.Shimmer3.GuiLabelConfig.MPU9150_MAG_RATE):
				returnValue = getMPU9150MagSamplingRate();
		    	break;
		    	
//Strings
			case(Configuration.Shimmer3.GuiLabelConfig.MPU9150_GYRO_RATE):
				returnValue = Double.toString((double)Math.round(getMPU9150GyroAccelRateInHz() * 100) / 100); // round sampling rate to two decimal places
//    		    		System.out.println("Gyro Sampling rate: " + getMPU9150GyroAccelRateInHz() + " " + returnValue);
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
			case(Configuration.Shimmer3.GuiLabelConfig.MPU9150_ACCEL_RANGE):
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
	
	private void setDefaultMpu9150GyroSensorConfig(boolean state) {
		if(!checkIfAnyMplChannelEnabled()) {
			if(!isSensorEnabled(Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_ACCEL)) {
				if(state) {
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
	
	private void setDefaultMpu9150AccelSensorConfig(boolean state) {
		if(!checkIfAnyMplChannelEnabled()) {
			if(!isSensorEnabled(Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_GYRO)) {
				if(state) {
					setLowPowerGyro(false);
				}
				else {
					setLowPowerGyro(true);
				}
			}
			
			if(!state){
				mMPU9150AccelRange = 0; //=2g
			}
		}
		else {
			mMPU9150AccelRange = 0; //=2g
		}
	}
	
	private void setDefaultMpu9150MplSensorConfig(boolean state) {
		if(state){
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
	
	private boolean checkIfAMpuGyroOrAccelEnabled(){
		if(isSensorEnabled(Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_GYRO)) {
			return true;
		}
		if(isSensorEnabled(Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_ACCEL)) {
			return true;
		}
//		if(mSensorMap.get(Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_MAG) != null) {
//			if(mSensorMap.get(Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_MAG).mIsEnabled) {
//				return true;
//			}
//		}
		return false;
	}	
	
	private boolean checkIfAnyOtherMplChannelEnabled(int sensorMapKey){
		if (mShimmerVerObject.getHardwareVersion()==HW_ID.SHIMMER_3 || mShimmerVerObject.getHardwareVersion()==HW_ID.SHIMMER_GQ_BLE) {
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
		if (mShimmerVerObject.getHardwareVersion()==HW_ID.SHIMMER_3 || mShimmerVerObject.getHardwareVersion()==HW_ID.SHIMMER_GQ_BLE) {
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
			return (numerator / mMPU9150GyroAccelRate);
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
	
	@Override
	public void setSamplingRateFromFreq() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDefaultConfiguration() {
		// TODO Auto-generated method stub

//		setDefaultMpu9150MplSensorConfig(state);
//		setDefaultMpu9150GyroSensorConfig(state);
//		setDefaultMpu9150AccelSensorConfig(state);
	}
	
	
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

	
}
