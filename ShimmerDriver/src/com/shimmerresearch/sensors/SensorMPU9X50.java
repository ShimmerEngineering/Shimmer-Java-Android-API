package com.shimmerresearch.sensors;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.vecmath.Vector3d;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import com.shimmerresearch.driver.Configuration.CHANNEL_UNITS;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driver.Configuration.Shimmer3;
import com.shimmerresearch.driver.Configuration.Shimmer3.CompatibilityInfoForMaps;
import com.shimmerresearch.driver.Configuration.Shimmer3.DatabaseChannelHandles;
import com.shimmerresearch.driver.ShimmerMsg;
import com.shimmerresearch.driver.ShimmerObject.BTStream;
import com.shimmerresearch.driver.ShimmerObject.SDLogHeader;
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
import com.shimmerresearch.driver.FormatCluster;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driver.UtilShimmer;
import com.shimmerresearch.sensors.AbstractSensor.SENSORS;

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

	public static final int MPU9150_GYRO = 1;
	
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
	//TODO fill in all conflicting sensors for each sensor listed below -> not all were done in Configuration.Shimmer3
	//TODO should MPU9150_MPL_RATE be in all mListOfConfigOptionKeysAssociated??

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
	
	public static final SensorDetailsRef sensorMpu9150AccelRef = new SensorDetailsRef(0x40<<(2*8), 0x40<<(2*8), Configuration.Shimmer3.GuiLabelSensors.ACCEL_MPU,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors,
			Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_MPL_ACCEL), 
			Arrays.asList(Configuration.Shimmer3.GuiLabelConfig.MPU9150_ACCEL_RANGE),
			Arrays.asList(
					Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_MPU_X,
					Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_MPU_Y,
					Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_MPU_Z),
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
	public static final SensorDetailsRef sensorMpu9150TempRef = new SensorDetailsRef(0x02<<(2*8), 0x02<<(2*8), Shimmer3.GuiLabelSensors.MPL_TEMPERATURE,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors,
			Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_TEMP), 
			Arrays.asList(Configuration.Shimmer3.GuiLabelConfig.MPU9150_MPL_RATE),
			Arrays.asList(
					Configuration.Shimmer3.ObjectClusterSensorName.MPL_TEMPERATURE),
			false);

	
	public static final SensorDetailsRef sensorMpu9150MplQuat6Dof = new SensorDetailsRef((long)0, (long)0x80<<(3*8), Configuration.Shimmer3.GuiLabelSensors.QUAT_MPL_6DOF,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors,
			null,//Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_MPL_QUAT_6DOF),
			Arrays.asList(Configuration.Shimmer3.GuiLabelConfig.MPU9150_MPL_RATE),
			Arrays.asList(
					Configuration.Shimmer3.ObjectClusterSensorName.QUAT_MPL_6DOF_W,
					Configuration.Shimmer3.ObjectClusterSensorName.QUAT_MPL_6DOF_X,
					Configuration.Shimmer3.ObjectClusterSensorName.QUAT_MPL_6DOF_Y,
					Configuration.Shimmer3.ObjectClusterSensorName.QUAT_MPL_6DOF_Z),
			false);
	
	
	public static final SensorDetailsRef sensorMpu9150MplQuat9Dof = new SensorDetailsRef((long)0, (long)0x40<<(3*8), Configuration.Shimmer3.GuiLabelSensors.QUAT_MPL_9DOF,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors,
			null,//Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_MPL_QUAT_9DOF),
			Arrays.asList(Configuration.Shimmer3.GuiLabelConfig.MPU9150_MPL_RATE),
			Arrays.asList(
					Configuration.Shimmer3.ObjectClusterSensorName.QUAT_MPL_9DOF_W,
					Configuration.Shimmer3.ObjectClusterSensorName.QUAT_MPL_9DOF_X,
					Configuration.Shimmer3.ObjectClusterSensorName.QUAT_MPL_9DOF_Y,
					Configuration.Shimmer3.ObjectClusterSensorName.QUAT_MPL_9DOF_Z),
			false);
	
	public static final SensorDetailsRef sensorMpu9150MplEuler6Dof = new SensorDetailsRef((long)0, (long)0x20<<(3*8), Configuration.Shimmer3.GuiLabelSensors.EULER_ANGLES_6DOF,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors,
			null,//Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_MPL_EULER_6DOF),
			Arrays.asList(Configuration.Shimmer3.GuiLabelConfig.MPU9150_MPL_RATE),
			Arrays.asList(
					Configuration.Shimmer3.ObjectClusterSensorName.EULER_MPL_6DOF_X,
					Configuration.Shimmer3.ObjectClusterSensorName.EULER_MPL_6DOF_Y,
					Configuration.Shimmer3.ObjectClusterSensorName.EULER_MPL_6DOF_Z),
			false);
	
	public static final SensorDetailsRef sensorMpu9150MplEuler9Dof = new SensorDetailsRef((long)0, (long)0x10<<(3*8), Configuration.Shimmer3.GuiLabelSensors.EULER_ANGLES_9DOF,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors,
			null,//Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_MPL_EULER_9DOF),
			Arrays.asList(Configuration.Shimmer3.GuiLabelConfig.MPU9150_MPL_RATE),
			Arrays.asList(
					Configuration.Shimmer3.ObjectClusterSensorName.EULER_MPL_9DOF_X,
					Configuration.Shimmer3.ObjectClusterSensorName.EULER_MPL_9DOF_Y,
					Configuration.Shimmer3.ObjectClusterSensorName.EULER_MPL_9DOF_Z),
			false);
	
	public static final SensorDetailsRef sensorMpu9150MplHeading = new SensorDetailsRef((long)0, (long)0x08<<(3*8), Configuration.Shimmer3.GuiLabelSensors.MPL_HEADING,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors,
			null,
			Arrays.asList(Configuration.Shimmer3.GuiLabelConfig.MPU9150_MPL_RATE),
			Arrays.asList(
					Configuration.Shimmer3.ObjectClusterSensorName.MPL_HEADING),
			false);
	
	public static final SensorDetailsRef sensorMpu9150MplPedometer = new SensorDetailsRef((long)0, (long)0x04<<(3*8), Configuration.Shimmer3.GuiLabelSensors.MPL_PEDOM_CNT,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors,
			null,//Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_MPL_PEDOMETER),
			Arrays.asList(Configuration.Shimmer3.GuiLabelConfig.MPU9150_MPL_RATE),
			Arrays.asList(
					Configuration.Shimmer3.ObjectClusterSensorName.MPL_PEDOM_CNT,
					Configuration.Shimmer3.ObjectClusterSensorName.MPL_PEDOM_TIME),
			false);
	
	public static final SensorDetailsRef sensorMpu9150MplTap = new SensorDetailsRef((long)0, (long)0x02<<(3*8), Configuration.Shimmer3.GuiLabelSensors.MPL_TAPDIRANDTAPCNT,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors,
			null,//Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_MPL_TAP),
			Arrays.asList(Configuration.Shimmer3.GuiLabelConfig.MPU9150_MPL_RATE),
			Arrays.asList(
					Configuration.Shimmer3.ObjectClusterSensorName.TAPDIRANDTAPCNT),
			false);
	
	public static final SensorDetailsRef sensorMpu9150MplMotion = new SensorDetailsRef((long)0, (long)0x01<<(3*8), Configuration.Shimmer3.GuiLabelSensors.MPL_MOTIONANDORIENT,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors,
			null,//Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_MPL_MOTION_ORIENT),
			Arrays.asList(Configuration.Shimmer3.GuiLabelConfig.MPU9150_MPL_RATE),
			Arrays.asList(
					Configuration.Shimmer3.ObjectClusterSensorName.TAPDIRANDTAPCNT),
			false);

	//MPL calibrated sensors
	public static final SensorDetailsRef sensorMpu9150MplGyro = new SensorDetailsRef((long)0, (long)0x80<<(4*8), Configuration.Shimmer3.GuiLabelSensors.GYRO_MPU_MPL,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors,
			Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_GYRO),
			Arrays.asList(
					Configuration.Shimmer3.GuiLabelConfig.MPU9150_GYRO_RANGE,
					Configuration.Shimmer3.GuiLabelConfig.MPU9150_MPL_LPF,
					Configuration.Shimmer3.GuiLabelConfig.MPU9150_GYRO_RATE,
					Configuration.Shimmer3.GuiLabelConfig.MPU9150_MPL_GYRO_CAL),
			Arrays.asList(
					Configuration.Shimmer3.ObjectClusterSensorName.GYRO_MPU_MPL_X,
					Configuration.Shimmer3.ObjectClusterSensorName.GYRO_MPU_MPL_Y,
					Configuration.Shimmer3.ObjectClusterSensorName.GYRO_MPU_MPL_Z),
			false);

	public static final SensorDetailsRef sensorMpu9150MplAccel = new SensorDetailsRef((long)0, (long)0x40<<(4*8), Configuration.Shimmer3.GuiLabelSensors.ACCEL_MPU_MPL,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors,
			Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_ACCEL),
			Arrays.asList(
					Configuration.Shimmer3.GuiLabelConfig.MPU9150_ACCEL_RANGE,
					Configuration.Shimmer3.GuiLabelConfig.MPU9150_MPL_LPF),
			Arrays.asList(
					Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_MPU_MPL_X,
					Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_MPU_MPL_Y,
					Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_MPU_MPL_Z),
			false);
	
	public static final SensorDetailsRef sensorMpu9150MplMag = new SensorDetailsRef((long)0, (long)0x20<<(4*8), Configuration.Shimmer3.GuiLabelSensors.MAG_MPU_MPL,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors,
			Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_MAG),
			Arrays.asList(
					Configuration.Shimmer3.GuiLabelConfig.MPU9150_MPL_LPF),
			Arrays.asList(
					Configuration.Shimmer3.ObjectClusterSensorName.MAG_MPU_MPL_X,
					Configuration.Shimmer3.ObjectClusterSensorName.MAG_MPU_MPL_Y,
					Configuration.Shimmer3.ObjectClusterSensorName.MAG_MPU_MPL_Z),
			false);
	
	public static final SensorDetailsRef sensorMpu9150MplQuat6DofRaw = new SensorDetailsRef((long)0, (long)0x10<<(4*8), Configuration.Shimmer3.GuiLabelSensors.QUAT_DMP_6DOF,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors,
			null,//Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_MPL_QUAT_6DOF_RAW),
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
		setSensorName(SENSORS.MPU9X50.toString());
	}


	@Override
	public void generateSensorMap(ShimmerVerObject svo) {
		//TODO populate the other channels depending on firmware version
		super.createLocalSensorMap(mSensorMapRef, mChannelMapRef);
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
	
	
	@Override
	public ObjectCluster processDataCustom(SensorDetails sensorDetails, byte[] sensorByteArray, COMMUNICATION_TYPE commType, ObjectCluster objectCluster, boolean isTimeSyncEnabled, long pcTimestamp) {
//
//		//TODO needed for 6DOF/9DOF
//		Vector3d gyroscope = new Vector3d();
////		String calUnitToUse = Configuration.CHANNEL_UNITS.MAG_CAL_UNIT;
//		
////		sensorDetails.processData(sensorByteArray, commType, objectCluster);
//		
//		//Parsing stage
//		int index = 0;
//		for (ChannelDetails channelDetails:sensorDetails.mListOfChannels){
//			//first process the data originating from the Shimmer sensor
//			byte[] channelByteArray = new byte[channelDetails.mDefaultNumBytes];
//			System.arraycopy(sensorByteArray, index, channelByteArray, 0, channelDetails.mDefaultNumBytes);
//			objectCluster = SensorDetails.processShimmerChannelData(sensorByteArray, channelDetails, objectCluster);
//			objectCluster.indexKeeper++;
//			index += channelDetails.mDefaultNumBytes;
//		}
//		
//		
//		//Calibration stage
//		double[] tempData = new double[3];
//		String[] gyroAxes = new String[]{
//				Configuration.Shimmer3.ObjectClusterSensorName.GYRO_X,
//				Configuration.Shimmer3.ObjectClusterSensorName.GYRO_Y,
//				Configuration.Shimmer3.ObjectClusterSensorName.GYRO_Z
//		};
//		for(int i=0;i<gyroAxes.length;i++){
//			Collection<FormatCluster> ocAxis = objectCluster.mPropertyCluster.get(gyroAxes[i]);
//			if(ocAxis!=null){
//				tempData[i] = ((FormatCluster)ObjectCluster.returnFormatCluster(ocAxis, CHANNEL_TYPE.UNCAL.toString())).mData;
//			}
//		}
//
//		
////			if (channelDetails.mObjectClusterName.equals()){
////			}
////			if (channelDetails.mObjectClusterName.equals(Configuration.Shimmer3.ObjectClusterSensorName.GYRO_Y)){
////				tempData[1] = ((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.mPropertyCluster.get(channelDetails.mObjectClusterName), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
////			}
////
////			if (channelDetails.mObjectClusterName.equals(Configuration.Shimmer3.ObjectClusterSensorName.GYRO_Z)){
////				tempData[2] = ((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.mPropertyCluster.get(channelDetails.mObjectClusterName), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
////			}
//			
//			double[] gyroCalData=calibrateInertialSensorData(tempData, mAlignmentMatrixGyroscope, mSensitivityMatrixGyroscope, mOffsetVectorGyroscope);
//
//			if (mEnableCalibration){
//				gyroscope.x=gyroCalData[0]*Math.PI/180;
//				gyroscope.y=gyroCalData[1]*Math.PI/180;
//				gyroscope.z=gyroCalData[2]*Math.PI/180;
//
//				if (mEnableOntheFlyGyroOVCal){
//						mGyroXX.addValue(gyroCalData[0]);
//						mGyroXY.addValue(gyroCalData[1]);
//						mGyroXZ.addValue(gyroCalData[2]);
//						
//						if (mGyroXX.getStandardDeviation()<mGyroXOVCalThreshold && mGyroXY.getStandardDeviation()<mGyroXOVCalThreshold && mGyroXZ.getStandardDeviation()<mGyroXOVCalThreshold){
//							mOffsetVectorGyroscope[0][0]=mGyroXXRaw.getMean();
//							mOffsetVectorGyroscope[1][0]=mGyroXYRaw.getMean();
//							mOffsetVectorGyroscope[2][0]=mGyroXZRaw.getMean();
//						}
//	
//					}
//					for(int i=0; i<3; i++){
//						objectCluster.addCalData(channelDetails, gyroCalData[i]);
//						objectCluster.indexKeeper++;
//					}
//			}
//
//		}
		
		return objectCluster;
	}
	
	protected static double[] calibrateInertialSensorData(double[] data, double[][] AM, double[][] SM, double[][] OV) {
		
		double [][] data2d=new double [3][1];
		data2d[0][0]=data[0];
		data2d[1][0]=data[1];
		data2d[2][0]=data[2];
		data2d= matrixmultiplication(matrixmultiplication(matrixinverse3x3(AM),matrixinverse3x3(SM)),matrixminus(data2d,OV));
		double[] ansdata=new double[3];
		ansdata[0]=data2d[0][0];
		ansdata[1]=data2d[1][0];
		ansdata[2]=data2d[2][0];
		return ansdata;
	}
	private static double[][] matrixmultiplication(double[][] a, double[][] b) {

		int aRows = a.length,
				aColumns = a[0].length,
				bRows = b.length,
				bColumns = b[0].length;

		if ( aColumns != bRows ) {
			throw new IllegalArgumentException("A:Rows: " + aColumns + " did not match B:Columns " + bRows + ".");
		}

		double[][] resultant = new double[aRows][bColumns];

		for(int i = 0; i < aRows; i++) { // aRow
			for(int j = 0; j < bColumns; j++) { // bColumn
				for(int k = 0; k < aColumns; k++) { // aColumn
					resultant[i][j] += a[i][k] * b[k][j];
				}
			}
		}

		return resultant;
	}

	private static double[][] matrixinverse3x3(double[][] data) {
		double a,b,c,d,e,f,g,h,i;
		a=data[0][0];
		b=data[0][1];
		c=data[0][2];
		d=data[1][0];
		e=data[1][1];
		f=data[1][2];
		g=data[2][0];
		h=data[2][1];
		i=data[2][2];
		//
		double deter=a*e*i+b*f*g+c*d*h-c*e*g-b*d*i-a*f*h;
		double[][] answer=new double[3][3];
		answer[0][0]=(1/deter)*(e*i-f*h);

		answer[0][1]=(1/deter)*(c*h-b*i);
		answer[0][2]=(1/deter)*(b*f-c*e);
		answer[1][0]=(1/deter)*(f*g-d*i);
		answer[1][1]=(1/deter)*(a*i-c*g);
		answer[1][2]=(1/deter)*(c*d-a*f);
		answer[2][0]=(1/deter)*(d*h-e*g);
		answer[2][1]=(1/deter)*(g*b-a*h);
		answer[2][2]=(1/deter)*(a*e-b*d);
		return answer;
	}
	private static double[][] matrixminus(double[][] a ,double[][] b) {
		int aRows = a.length,
				aColumns = a[0].length,
				bRows = b.length,
				bColumns = b[0].length;
		if (( aColumns != bColumns )&&( aRows != bRows )) {
			throw new IllegalArgumentException(" Matrix did not match");
		}
		double[][] resultant = new double[aRows][bColumns];
		for(int i = 0; i < aRows; i++) { // aRow
			for(int k = 0; k < aColumns; k++) { // aColumn

				resultant[i][k]=a[i][k]-b[i][k];

			}
		}
		return resultant;
	}
	@Override
	public void infoMemByteArrayGenerate(ShimmerDevice shimmerDevice, byte[] mInfoMemBytes) {
//		if(getHardwareVersion()==HW_ID.SHIMMER_3){
		int	idxConfigSetupByte2 = 8;
		int bitShiftMPU9150GyroRange = 0;
		int maskMPU9150GyroRange = 0x03;
		mInfoMemBytes[idxConfigSetupByte2] |= (byte) ((mGyroRange & maskMPU9150GyroRange) << bitShiftMPU9150GyroRange);
//		}
//		byte[] bufferCalibrationParameters = generateCalParamGyroscope(); // check if needed
//		System.arraycopy(bufferCalibrationParameters, 0, mInfoMemBytes, infoMemLayout.idxMPU9150GyroCalibration, infoMemLayout.lengthGeneralCalibrationBytes);
//		
		
	}

	@Override
	public void infoMemByteArrayParse(ShimmerDevice shimmerDevice, byte[] mInfoMemBytes) {
		int	idxConfigSetupByte2 = 8;
		int bitShiftMPU9150GyroRange = 0;
		int maskMPU9150GyroRange = 0x03;
		mGyroRange = (mInfoMemBytes[idxConfigSetupByte2] >> bitShiftMPU9150GyroRange) & maskMPU9150GyroRange;
		
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
//	************** Check if needed ****************
//	protected void interpretInqResponse(byte[] bufferInquiry){
//		if (getHardwareVersion()==HW_ID.SHIMMER_2 || getHardwareVersion()==HW_ID.SHIMMER_2R){
//			mPacketSize = mTimeStampPacketByteSize +bufferInquiry[3]*2; 
//			setSamplingRateShimmer((double)1024/bufferInquiry[0]);
//			
//			mAccelRange = bufferInquiry[1];
//			mConfigByte0 = bufferInquiry[2] & 0xFF; //convert the byte to unsigned integer
//			mNChannels = bufferInquiry[3];
//			mBufferSize = bufferInquiry[4];
//			byte[] signalIdArray = new byte[mNChannels];
//			System.arraycopy(bufferInquiry, 5, signalIdArray, 0, mNChannels);
//			updateEnabledSensorsFromChannels(signalIdArray);
//			interpretDataPacketFormat(mNChannels,signalIdArray);
//			mInquiryResponseBytes = new byte[5+mNChannels];
//			System.arraycopy(bufferInquiry, 0, mInquiryResponseBytes , 0, mInquiryResponseBytes.length);
//		} 
//		else if (getHardwareVersion()==HW_ID.SHIMMER_3) {
//			mPacketSize = mTimeStampPacketByteSize+bufferInquiry[6]*2; 
//			setSamplingRateShimmer((32768/(double)((int)(bufferInquiry[0] & 0xFF) + ((int)(bufferInquiry[1] & 0xFF) << 8))));
//			mNChannels = bufferInquiry[6];
//			mBufferSize = bufferInquiry[7];
//			mConfigByte0 = ((long)(bufferInquiry[2] & 0xFF) +((long)(bufferInquiry[3] & 0xFF) << 8)+((long)(bufferInquiry[4] & 0xFF) << 16) +((long)(bufferInquiry[5] & 0xFF) << 24));
//			mAccelRange = ((int)(mConfigByte0 & 0xC))>>2;
//			mGyroRange = ((int)(mConfigByte0 & 196608))>>16;
//			mMagRange = ((int)(mConfigByte0 & 14680064))>>21;
//			
//			mMPU9150GyroAccelRate = ((int)(mConfigByte0 & 65280))>>8;
//			
//			mInternalExpPower = (((int)(mConfigByte0 >>24)) & 1);
//			mInquiryResponseBytes = new byte[8+mNChannels];
//			System.arraycopy(bufferInquiry, 0, mInquiryResponseBytes , 0, mInquiryResponseBytes.length);
//			
//			if ((mMPU9150GyroAccelRate==0xFF && getSamplingRateShimmer()>10)){
//				mLowPowerGyro = true;
//			}
//			if ((mLSM303MagRate==4 && getSamplingRateShimmer()>10)){
//				mLowPowerMag = true;
//			}
//			byte[] signalIdArray = new byte[mNChannels];
//			System.arraycopy(bufferInquiry, 8, signalIdArray, 0, mNChannels);
//			updateEnabledSensorsFromChannels(signalIdArray);
//			interpretDataPacketFormat(mNChannels,signalIdArray);
//			checkExgResolutionFromEnabledSensorsVar();
//		} 
//		else if (getHardwareVersion()==HW_ID.SHIMMER_SR30) {
//			mPacketSize = mTimeStampPacketByteSize+bufferInquiry[2]*2; 
//			setSamplingRateShimmer((double)1024/bufferInquiry[0]);
//			mAccelRange = bufferInquiry[1];
//			mNChannels = bufferInquiry[2];
//			mBufferSize = bufferInquiry[3];
//			byte[] signalIdArray = new byte[mNChannels];
//			System.arraycopy(bufferInquiry, 4, signalIdArray, 0, mNChannels); // this is 4 because there is no config byte
//			interpretDataPacketFormat(mNChannels,signalIdArray);
//
//		}
//	}
	
	
	

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
	
	
	@Override
	public void setSensorSamplingRate() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean setDefaultConfigForSensor(int sensorMapKey, boolean state) {
		if(mSensorMap.containsKey(sensorMapKey)){
			//TODO set defaults for particular sensor
			
			if(sensorMapKey==Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_ACCEL){
				setDefaultMpu9150AccelSensorConfig(state);
			}
			else if(sensorMapKey==Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_GYRO){
				setDefaultMpu9150GyroSensorConfig(state);
			}
//			else if(sensorMapKey==Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_){
//				setDefaultMpu9150MplSensorConfig(state);
//			}
			
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
	


	
}
