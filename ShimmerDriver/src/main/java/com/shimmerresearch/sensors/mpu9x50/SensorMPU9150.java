package com.shimmerresearch.sensors.mpu9x50;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driver.Configuration.CHANNEL_UNITS;
import com.shimmerresearch.driver.Configuration.Shimmer3.CompatibilityInfoForMaps;
import com.shimmerresearch.driver.calibration.CalibDetailsKinematic.CALIBRATION_SCALE_FACTOR;
import com.shimmerresearch.driver.calibration.OldCalDetails;
import com.shimmerresearch.driverUtilities.ChannelDetails;
import com.shimmerresearch.driverUtilities.SensorDetailsRef;
import com.shimmerresearch.driverUtilities.SensorGroupingDetails;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_ENDIAN;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_TYPE;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_TYPE;
import com.shimmerresearch.driverUtilities.ConfigOptionDetailsSensor;
import com.shimmerresearch.sensors.AbstractSensor;

public class SensorMPU9150 extends SensorMPU9X50 {
	
	private static final long serialVersionUID = -8462796728357952990L;

	//--------- Sensor specific variables start --------------
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
	
	public static final class DatabaseConfigHandle{
//		public static final String GYRO = "MPU9150_Gyro";
//		public static final String ALTERNATIVE_ACC = "MPU9150_Acc"; // not available but supported in FW
//		public static final String ALTERNATIVE_MAG = "MPU9150_Mag"; // not available but supported in FW
		
		public static final String MPU_QUAT_6DOF = "MPU9150_MPL_Quat_6DOF"; 
		public static final String MPU_EULER_6DOF = "MPU9150_MPL_Euler_6DOF"; 
		public static final String MPU_HEADING_ENABLE = "MPU9150_MPL_Heading"; // not available but supported in FW //channel
		
		public static final String MPU_PEDOMETER = "MPU9150_MPL_Pedometer"; 
		public static final String MPU_TAP = "MPU9150_MPL_Tap"; 
		public static final String MPU_MOTION_ORIENT = "MPU9150_MPL_Motion"; 
		public static final String MPU_GYRO = "MPU9150_MPL_Gyro_Cal";
		
		public static final String GYRO_RATE = "MPU9150_Gyro_Rate";
		public static final String GYRO_RANGE = "MPU9150_Gyro_Range";
		public static final String ALTERNATIVE_ACC_RANGE = "MPU9150_Acc_Range";
		
		public static final String MPU_ACC = "MPU9150_MPL_Acc_Cal";
		public static final String MPU_MAG = "MPU9150_MPL_Mag_Cal";
		public static final String MPU_QUAT_6DOF_DMP = "MPU9150_Quat_6DOF_Dmp";
		
		public static final String MPU_DMP = "MPU9150_DMP";
		public static final String MPU_LPF = "MPU9150_LFP";
		
		public static final String MPU_MOT_CAL_CFG = "MPU9150_MOT_Cal_Cfg";
		public static final String MPU_MPL_SAMPLING_RATE = "MPU9150_MPL_Sampling_rate";
		public static final String MPU_MAG_SAMPLING_RATE = "MPU9150_MAG_Sampling_rate";
		public static final String MPU_MPL_SENSOR_FUSION = "MPU9150_MPL_Sensor_Fusion";
		public static final String MPU_MPL_GYRO_TC = "MPU9150_MPL_Gyro_TC";
		public static final String MPU_MPL_VECT_COMP = "MPU9150_MPL_Vect_Comp";
		public static final String MPU_MAG_DIST = "MPU9150_MAG_Dist";
		public static final String MPU_MPL_ENABLE = "MPU9150_MPL_Enable";
		// MPU GYRO
		public static final String GYRO_CALIB_TIME = "MPU9150_Gyro_Calib_Time";
		public static final String GYRO_OFFSET_X = "MPU9150_Gyro_Offset_X";
		public static final String GYRO_OFFSET_Y = "MPU9150_Gyro_Offset_Y";
		public static final String GYRO_OFFSET_Z = "MPU9150_Gyro_Offset_Z";
		public static final String GYRO_GAIN_X = "MPU9150_Gyro_Gain_X";
		public static final String GYRO_GAIN_Y = "MPU9150_Gyro_Gain_Y";
		public static final String GYRO_GAIN_Z = "MPU9150_Gyro_Gain_Z";
		public static final String GYRO_ALIGN_XX = "MPU9150_Gyro_Align_XX";
		public static final String GYRO_ALIGN_XY = "MPU9150_Gyro_Align_XY";
		public static final String GYRO_ALIGN_XZ = "MPU9150_Gyro_Align_XZ";
		public static final String GYRO_ALIGN_YX = "MPU9150_Gyro_Align_YX";
		public static final String GYRO_ALIGN_YY = "MPU9150_Gyro_Align_YY";
		public static final String GYRO_ALIGN_YZ = "MPU9150_Gyro_Align_YZ";
		public static final String GYRO_ALIGN_ZX = "MPU9150_Gyro_Align_ZX";
		public static final String GYRO_ALIGN_ZY = "MPU9150_Gyro_Align_ZY";
		public static final String GYRO_ALIGN_ZZ = "MPU9150_Gyro_Align_ZZ";
		// MPU MPL ACCEL
		public static final String MPU_ACC_OFFSET_X = "MPU9150_MPL_Acc_Cal_Offset_X";
		public static final String MPU_ACC_OFFSET_Y = "MPU9150_MPL_Acc_Cal_Offset_Y";
		public static final String MPU_ACC_OFFSET_Z = "MPU9150_MPL_Acc_Cal_Offset_Z";
		public static final String MPU_ACC_GAIN_X = "MPU9150_MPL_Acc_Cal_Gain_X";
		public static final String MPU_ACC_GAIN_Y = "MPU9150_MPL_Acc_Cal_Gain_Y";
		public static final String MPU_ACC_GAIN_Z = "MPU9150_MPL_Acc_Cal_Gain_Z";
		public static final String MPU_ACC_ALIGN_XX = "MPU9150_MPL_Acc_Cal_Align_XX";
		public static final String MPU_ACC_ALIGN_XY = "MPU9150_MPL_Acc_Cal_Align_XY";
		public static final String MPU_ACC_ALIGN_XZ = "MPU9150_MPL_Acc_Cal_Align_XZ";
		public static final String MPU_ACC_ALIGN_YX = "MPU9150_MPL_Acc_Cal_Align_YX";
		public static final String MPU_ACC_ALIGN_YY = "MPU9150_MPL_Acc_Cal_Align_YY";
		public static final String MPU_ACC_ALIGN_YZ = "MPU9150_MPL_Acc_Cal_Align_YZ";
		public static final String MPU_ACC_ALIGN_ZX = "MPU9150_MPL_Acc_Cal_Align_ZX";
		public static final String MPU_ACC_ALIGN_ZY = "MPU9150_MPL_Acc_Cal_Align_ZY";
		public static final String MPU_ACC_ALIGN_ZZ = "MPU9150_MPL_Acc_Cal_Align_ZZ";
		// MPU MPL MAG
		public static final String MPU_MAG_OFFSET_X = "MPU9150_MPL_Mag_Cal_Offset_X";
		public static final String MPU_MAG_OFFSET_Y = "MPU9150_MPL_Mag_Cal_Offset_Y";
		public static final String MPU_MAG_OFFSET_Z = "MPU9150_MPL_Mag_Cal_Offset_Z";
		public static final String MPU_MAG_GAIN_X = "MPU9150_MPL_Mag_Cal_Gain_X";
		public static final String MPU_MAG_GAIN_Y = "MPU9150_MPL_Mag_Cal_Gain_Y";
		public static final String MPU_MAG_GAIN_Z = "MPU9150_MPL_Mag_Cal_Gain_Z";
		public static final String MPU_MAG_ALIGN_XX = "MPU9150_MPL_Mag_Cal_Align_XX";
		public static final String MPU_MAG_ALIGN_XY = "MPU9150_MPL_Mag_Cal_Align_XY";
		public static final String MPU_MAG_ALIGN_XZ = "MPU9150_MPL_Mag_Cal_Align_XZ";
		public static final String MPU_MAG_ALIGN_YX = "MPU9150_MPL_Mag_Cal_Align_YX";
		public static final String MPU_MAG_ALIGN_YY = "MPU9150_MPL_Mag_Cal_Align_YY";
		public static final String MPU_MAG_ALIGN_YZ = "MPU9150_MPL_Mag_Cal_Align_YZ";
		public static final String MPU_MAG_ALIGN_ZX = "MPU9150_MPL_Mag_Cal_Align_ZX";
		public static final String MPU_MAG_ALIGN_ZY = "MPU9150_MPL_Mag_Cal_Align_ZY";
		public static final String MPU_MAG_ALIGN_ZZ = "MPU9150_MPL_Mag_Cal_Align_ZZ";
		// MPU MPL GYRO
		public static final String MPU_GYRO_OFFSET_X = "MPU9150_MPL_Gyro_Cal_Offset_X";
		public static final String MPU_GYRO_OFFSET_Y = "MPU9150_MPL_Gyro_Cal_Offset_Y";
		public static final String MPU_GYRO_OFFSET_Z = "MPU9150_MPL_Gyro_Cal_Offset_Z";
		public static final String MPU_GYRO_GAIN_X = "MPU9150_MPL_Gyro_Cal_Gain_X";
		public static final String MPU_GYRO_GAIN_Y = "MPU9150_MPL_Gyro_Cal_Gain_Y";
		public static final String MPU_GYRO_GAIN_Z = "MPU9150_MPL_Gyro_Cal_Gain_Z";
		public static final String MPU_GYRO_ALIGN_XX = "MPU9150_MPL_Gyro_Cal_Align_XX";
		public static final String MPU_GYRO_ALIGN_XY = "MPU9150_MPL_Gyro_Cal_Align_XY";
		public static final String MPU_GYRO_ALIGN_XZ = "MPU9150_MPL_Gyro_Cal_Align_XZ";
		public static final String MPU_GYRO_ALIGN_YX = "MPU9150_MPL_Gyro_Cal_Align_YX";
		public static final String MPU_GYRO_ALIGN_YY = "MPU9150_MPL_Gyro_Cal_Align_YY";
		public static final String MPU_GYRO_ALIGN_YZ = "MPU9150_MPL_Gyro_Cal_Align_YZ";
		public static final String MPU_GYRO_ALIGN_ZX = "MPU9150_MPL_Gyro_Cal_Align_ZX";
		public static final String MPU_GYRO_ALIGN_ZY = "MPU9150_MPL_Gyro_Cal_Align_ZY";
		public static final String MPU_GYRO_ALIGN_ZZ = "MPU9150_MPL_Gyro_Cal_Align_ZZ";
		
		public static final List<String> LIST_OF_CALIB_HANDLES_GYRO = Arrays.asList(
				DatabaseConfigHandle.GYRO_OFFSET_X, DatabaseConfigHandle.GYRO_OFFSET_Y, DatabaseConfigHandle.GYRO_OFFSET_Z,
				DatabaseConfigHandle.GYRO_GAIN_X, DatabaseConfigHandle.GYRO_GAIN_Y, DatabaseConfigHandle.GYRO_GAIN_Z,
				DatabaseConfigHandle.GYRO_ALIGN_XX, DatabaseConfigHandle.GYRO_ALIGN_XY, DatabaseConfigHandle.GYRO_ALIGN_XZ,
				DatabaseConfigHandle.GYRO_ALIGN_YX, DatabaseConfigHandle.GYRO_ALIGN_YY, DatabaseConfigHandle.GYRO_ALIGN_YZ,
				DatabaseConfigHandle.GYRO_ALIGN_ZX, DatabaseConfigHandle.GYRO_ALIGN_ZY, DatabaseConfigHandle.GYRO_ALIGN_ZZ);
		
		public static final List<String> LIST_OF_CALIB_HANDLES_MPU_MPL_ACC = Arrays.asList(
				DatabaseConfigHandle.MPU_ACC_OFFSET_X, DatabaseConfigHandle.MPU_ACC_OFFSET_Y, DatabaseConfigHandle.MPU_ACC_OFFSET_Z,
				DatabaseConfigHandle.MPU_ACC_GAIN_X, DatabaseConfigHandle.MPU_ACC_GAIN_Y, DatabaseConfigHandle.MPU_ACC_GAIN_Z,
				DatabaseConfigHandle.MPU_ACC_ALIGN_XX, DatabaseConfigHandle.MPU_ACC_ALIGN_XY, DatabaseConfigHandle.MPU_ACC_ALIGN_XZ,
				DatabaseConfigHandle.MPU_ACC_ALIGN_YX, DatabaseConfigHandle.MPU_ACC_ALIGN_YY, DatabaseConfigHandle.MPU_ACC_ALIGN_YZ,
				DatabaseConfigHandle.MPU_ACC_ALIGN_ZX, DatabaseConfigHandle.MPU_ACC_ALIGN_ZY, DatabaseConfigHandle.MPU_ACC_ALIGN_ZZ);
		
		public static final List<String> LIST_OF_CALIB_HANDLES_MPU_MPL_MAG = Arrays.asList(
				DatabaseConfigHandle.MPU_MAG_OFFSET_X, DatabaseConfigHandle.MPU_MAG_OFFSET_Y, DatabaseConfigHandle.MPU_MAG_OFFSET_Z,
				DatabaseConfigHandle.MPU_MAG_GAIN_X, DatabaseConfigHandle.MPU_MAG_GAIN_Y, DatabaseConfigHandle.MPU_MAG_GAIN_Z,
				DatabaseConfigHandle.MPU_MAG_ALIGN_XX, DatabaseConfigHandle.MPU_MAG_ALIGN_XY, DatabaseConfigHandle.MPU_MAG_ALIGN_XZ,
				DatabaseConfigHandle.MPU_MAG_ALIGN_YX, DatabaseConfigHandle.MPU_MAG_ALIGN_YY, DatabaseConfigHandle.MPU_MAG_ALIGN_YZ,
				DatabaseConfigHandle.MPU_MAG_ALIGN_ZX, DatabaseConfigHandle.MPU_MAG_ALIGN_ZY, DatabaseConfigHandle.MPU_MAG_ALIGN_ZZ);
		
		public static final List<String> LIST_OF_CALIB_HANDLES_MPU_MPL_GYRO = Arrays.asList(
				DatabaseConfigHandle.MPU_GYRO_OFFSET_X, DatabaseConfigHandle.MPU_GYRO_OFFSET_Y, DatabaseConfigHandle.MPU_GYRO_OFFSET_Z,
				DatabaseConfigHandle.MPU_GYRO_GAIN_X, DatabaseConfigHandle.MPU_GYRO_GAIN_Y, DatabaseConfigHandle.MPU_GYRO_GAIN_Z,
				DatabaseConfigHandle.MPU_GYRO_ALIGN_XX, DatabaseConfigHandle.MPU_GYRO_ALIGN_XY, DatabaseConfigHandle.MPU_GYRO_ALIGN_XZ,
				DatabaseConfigHandle.MPU_GYRO_ALIGN_YX, DatabaseConfigHandle.MPU_GYRO_ALIGN_YY, DatabaseConfigHandle.MPU_GYRO_ALIGN_YZ,
				DatabaseConfigHandle.MPU_GYRO_ALIGN_ZX, DatabaseConfigHandle.MPU_GYRO_ALIGN_ZY, DatabaseConfigHandle.MPU_GYRO_ALIGN_ZZ);
	}
	
	//--------- Channel info start --------------
	// MPU9150 Gyro
    public static final ChannelDetails channelGyroX = new ChannelDetails(
    		ObjectClusterSensorName.GYRO_X,
    		ObjectClusterSensorName.GYRO_X,
    		DatabaseChannelHandles.GYRO_X,
    		CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.MSB,
    		CHANNEL_UNITS.DEGREES_PER_SECOND,
    		Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL),
    		0x0A);
    public static final ChannelDetails channelGyroY = new ChannelDetails(
    		ObjectClusterSensorName.GYRO_Y,
    		ObjectClusterSensorName.GYRO_Y,
    		DatabaseChannelHandles.GYRO_Y,
    		CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.MSB,
    		CHANNEL_UNITS.DEGREES_PER_SECOND,
    		Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL),
    		0x0B);
    public static final ChannelDetails channelGyroZ = new ChannelDetails(
    		ObjectClusterSensorName.GYRO_Z,
    		ObjectClusterSensorName.GYRO_Z,
    		DatabaseChannelHandles.GYRO_Z,
    		CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.MSB,
    		CHANNEL_UNITS.DEGREES_PER_SECOND,
    		Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL),
    		0x0C);
    
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
					CHANNEL_UNITS.DEGREES_CELSIUS,
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
		aMap.put(ObjectClusterSensorName.GYRO_X, SensorMPU9150.channelGyroX);
		aMap.put(ObjectClusterSensorName.GYRO_Y, SensorMPU9150.channelGyroY);
		aMap.put(ObjectClusterSensorName.GYRO_Z, SensorMPU9150.channelGyroZ);
		// MPU9150 Accel
		aMap.put(ObjectClusterSensorName.ACCEL_MPU_X, SensorMPU9150.channelAccelX);
		aMap.put(ObjectClusterSensorName.ACCEL_MPU_Y, SensorMPU9150.channelAccelY);
		aMap.put(ObjectClusterSensorName.ACCEL_MPU_Z, SensorMPU9150.channelAccelZ);
		// MPU9150 Mag
		aMap.put(ObjectClusterSensorName.MAG_MPU_X, SensorMPU9150.channelMagX);
		aMap.put(ObjectClusterSensorName.MAG_MPU_Y, SensorMPU9150.channelMagY);
		aMap.put(ObjectClusterSensorName.MAG_MPU_Z, SensorMPU9150.channelMagZ);
		
		// MPL Gyro Calibrated
		aMap.put(ObjectClusterSensorName.GYRO_MPU_MPL_X, SensorMPU9150.channelGyroMpuMplX);
		aMap.put(ObjectClusterSensorName.GYRO_MPU_MPL_Y, SensorMPU9150.channelGyroMpuMplY);
		aMap.put(ObjectClusterSensorName.GYRO_MPU_MPL_Z, SensorMPU9150.channelGyroMpuMplZ);

		// MPL Accelerometer Calibrated
		aMap.put(ObjectClusterSensorName.ACCEL_MPU_MPL_X, SensorMPU9150.channelAccelMpuMplX);
		aMap.put(ObjectClusterSensorName.ACCEL_MPU_MPL_Y, SensorMPU9150.channelAccelMpuMplY);
		aMap.put(ObjectClusterSensorName.ACCEL_MPU_MPL_Z, SensorMPU9150.channelAccelMpuMplZ);
				
		// MPL Magnetometer Calibrated
		aMap.put(ObjectClusterSensorName.MAG_MPU_MPL_X, SensorMPU9150.channelMagMpuMplX);
		aMap.put(ObjectClusterSensorName.MAG_MPU_MPL_Y, SensorMPU9150.channelMagMpuMplY);
		aMap.put(ObjectClusterSensorName.MAG_MPU_MPL_Z, SensorMPU9150.channelMagMpuMplZ);
		

		// MPL Quaternions 6DOF
		aMap.put(ObjectClusterSensorName.QUAT_MPL_6DOF_W, SensorMPU9150.channelQuatMpl6DofW);
		aMap.put(ObjectClusterSensorName.QUAT_MPL_6DOF_X, SensorMPU9150.channelQuatMpl6DofX);
		aMap.put(ObjectClusterSensorName.QUAT_MPL_6DOF_Y, SensorMPU9150.channelQuatMpl6DofY);
		aMap.put(ObjectClusterSensorName.QUAT_MPL_6DOF_Z, SensorMPU9150.channelQuatMpl6DofZ);

		// MPL Quaternions 9DOF
		aMap.put(ObjectClusterSensorName.QUAT_MPL_9DOF_W, SensorMPU9150.channelQuatMpl9DofW);
		aMap.put(ObjectClusterSensorName.QUAT_MPL_9DOF_X, SensorMPU9150.channelQuatMpl9DofX);
		aMap.put(ObjectClusterSensorName.QUAT_MPL_9DOF_Y, SensorMPU9150.channelQuatMpl9DofY);
		aMap.put(ObjectClusterSensorName.QUAT_MPL_9DOF_Z, SensorMPU9150.channelQuatMpl9DofZ);
		
		// MPL Euler
		aMap.put(ObjectClusterSensorName.EULER_MPL_6DOF_X, SensorMPU9150.channelEulerMpl6DofX);
		aMap.put(ObjectClusterSensorName.EULER_MPL_6DOF_Y, SensorMPU9150.channelEulerMpl6DofY);
		aMap.put(ObjectClusterSensorName.EULER_MPL_6DOF_Z, SensorMPU9150.channelEulerMpl6DofZ);
		
		aMap.put(ObjectClusterSensorName.EULER_MPL_9DOF_X, SensorMPU9150.channelEulerMpl9DofX);
		aMap.put(ObjectClusterSensorName.EULER_MPL_9DOF_Y, SensorMPU9150.channelEulerMpl9DofY);
		aMap.put(ObjectClusterSensorName.EULER_MPL_9DOF_Z, SensorMPU9150.channelEulerMpl9DofZ);

		// MPL Heading
		aMap.put(ObjectClusterSensorName.MPL_HEADING, SensorMPU9150.channelMplHeading);

		// MPU9150 Temperature
		aMap.put(ObjectClusterSensorName.MPL_TEMPERATURE, SensorMPU9150.channelMplTemperature);

		// MPL Pedometer
		aMap.put(ObjectClusterSensorName.MPL_PEDOM_CNT, SensorMPU9150.channelMplPedomCount);
		aMap.put(ObjectClusterSensorName.MPL_PEDOM_TIME, SensorMPU9150.channelMplPedomTime);

//		// MPL Tap
//		aMap.put(ObjectClusterSensorName.TAPDIRANDTAPCNT, SensorMPU9150.channelMplTapDirAndTapCnt);
		// MPL Tap Direction
		aMap.put(ObjectClusterSensorName.TAPDIR, SensorMPU9150.channelMplTapDir);
		// MPL Tap Count
		aMap.put(ObjectClusterSensorName.TAPCNT, SensorMPU9150.channelMplTapCnt);
		
		// MPL Motion Orient
		aMap.put(ObjectClusterSensorName.MOTIONANDORIENT, SensorMPU9150.channelMplMotionAndOrient);
		aMap.put(ObjectClusterSensorName.MOTION, SensorMPU9150.channelMplMotion);
		aMap.put(ObjectClusterSensorName.ORIENT, SensorMPU9150.channelMplOrient);
		// Raw 6DOF Quaterian's from the DMP hardware module of the MPU9150
		aMap.put(ObjectClusterSensorName.QUAT_DMP_6DOF_W, SensorMPU9150.channelQuatDmp6DofW);
		aMap.put(ObjectClusterSensorName.QUAT_DMP_6DOF_X, SensorMPU9150.channelQuatDmp6DofX);
		aMap.put(ObjectClusterSensorName.QUAT_DMP_6DOF_Y, SensorMPU9150.channelQuatDmp6DofY);
		aMap.put(ObjectClusterSensorName.QUAT_DMP_6DOF_Z, SensorMPU9150.channelQuatDmp6DofZ);
		mChannelMapRef = Collections.unmodifiableMap(aMap);
    }

	//--------- Channel info end --------------
	
	//--------- Sensor info start --------------

	public static final SensorDetailsRef sensorMpu9150GyroRef = new SensorDetailsRef(0x40<<(0*8), 0x40<<(0*8), GuiLabelSensors.GYRO,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW,
			Arrays.asList(Configuration.Shimmer3.SENSOR_ID.SHIMMER_MPU9X50_MPL_GYRO),
			Arrays.asList(
					GuiLabelConfig.MPU9X50_GYRO_RANGE, 
					GuiLabelConfig.MPU9X50_GYRO_RATE),
			Arrays.asList(
					ObjectClusterSensorName.GYRO_X, 
					ObjectClusterSensorName.GYRO_Y, 
					ObjectClusterSensorName.GYRO_Z),
			false);
//	{
//		sensorMpu9150GyroRef.mCalibSensorKey = 0x01;
//	}
	
	public static final SensorDetailsRef sensorMpu9150AccelRef = new SensorDetailsRef(0x40<<(2*8), 0x40<<(2*8), GuiLabelSensors.ACCEL_MPU,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoShimmer4,
			Arrays.asList(Configuration.Shimmer3.SENSOR_ID.SHIMMER_MPU9X50_MPL_ACCEL), 
			Arrays.asList(GuiLabelConfig.MPU9X50_ACCEL_RANGE),
			Arrays.asList(
					ObjectClusterSensorName.ACCEL_MPU_X,
					ObjectClusterSensorName.ACCEL_MPU_Y,
					ObjectClusterSensorName.ACCEL_MPU_Z),
			false);

	public static final SensorDetailsRef sensorMpu9150MagRef = new SensorDetailsRef(0x20<<(2*8), 0x20<<(2*8), GuiLabelSensors.MAG_MPU,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoShimmer4,
			Arrays.asList(Configuration.Shimmer3.SENSOR_ID.SHIMMER_MPU9X50_MPL_MAG),
			Arrays.asList(GuiLabelConfig.MPU9X50_MAG_RATE),
			Arrays.asList(
					ObjectClusterSensorName.MAG_MPU_X,
					ObjectClusterSensorName.MAG_MPU_Y,
					ObjectClusterSensorName.MAG_MPU_Z),
			false);

    public static final Map<Integer, SensorDetailsRef> mSensorMapRef;
    static {
        Map<Integer, SensorDetailsRef> aMap = new LinkedHashMap<Integer, SensorDetailsRef>();
		aMap.put(Configuration.Shimmer3.SENSOR_ID.SHIMMER_MPU9X50_GYRO, SensorMPU9150.sensorMpu9150GyroRef);
		aMap.put(Configuration.Shimmer3.SENSOR_ID.SHIMMER_MPU9X50_ACCEL, SensorMPU9150.sensorMpu9150AccelRef);
		aMap.put(Configuration.Shimmer3.SENSOR_ID.SHIMMER_MPU9X50_MAG,SensorMPU9150.sensorMpu9150MagRef);
		mSensorMapRef = Collections.unmodifiableMap(aMap);
    }
	//--------- Sensor info end --------------

	public static final Map<String, OldCalDetails> mOldCalRangeMap;
	static {
	    Map<String, OldCalDetails> aMap = new LinkedHashMap<String, OldCalDetails>();
	    aMap.put("gyro_250dps", new OldCalDetails("gyro_250dps", Configuration.Shimmer3.SENSOR_ID.SHIMMER_MPU9X50_GYRO, 0));
	    aMap.put("gyro_500dps", new OldCalDetails("gyro_500dps", Configuration.Shimmer3.SENSOR_ID.SHIMMER_MPU9X50_GYRO, 1));
	    aMap.put("gyro_1000ps", new OldCalDetails("gyro_1000ps", Configuration.Shimmer3.SENSOR_ID.SHIMMER_MPU9X50_GYRO, 2));
	    aMap.put("gyro_2000dps", new OldCalDetails("gyro_2000dps", Configuration.Shimmer3.SENSOR_ID.SHIMMER_MPU9X50_GYRO, 3));
	    mOldCalRangeMap = Collections.unmodifiableMap(aMap);
	}

	//--------- Sensor specific variables end --------------

	//--------- Configuration options start --------------

	public static final ConfigOptionDetailsSensor configOptionMpu9150GyroRange = new ConfigOptionDetailsSensor(
			SensorMPU9X50.GuiLabelConfig.MPU9X50_GYRO_RANGE,
			SensorMPU9150.DatabaseConfigHandle.GYRO_RANGE,
			ListofGyroRange, 
			ListofMPU9X50GyroRangeConfigValues, 
			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW);
	//MPL Options
	public static final ConfigOptionDetailsSensor configOptionMpu9150AccelRange = new ConfigOptionDetailsSensor(
			SensorMPU9X50.GuiLabelConfig.MPU9X50_ACCEL_RANGE,
			SensorMPU9150.DatabaseConfigHandle.ALTERNATIVE_ACC_RANGE,
			ListofMPU9X50AccelRange, 
			ListofMPU9X50AccelRangeConfigValues, 
			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors);
	
	public static final ConfigOptionDetailsSensor configOptionMpu9150DmpGyroCal = new ConfigOptionDetailsSensor(
			SensorMPU9X50.GuiLabelConfig.MPU9X50_DMP_GYRO_CAL,
			SensorMPU9150.DatabaseConfigHandle.MPU_GYRO,
			ListofMPU9150MplCalibrationOptions, 
			ListofMPU9150MplCalibrationOptionsConfigValues, 
			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors);
	
	public static final ConfigOptionDetailsSensor configOptionMpu9150MplLpf = new ConfigOptionDetailsSensor(
			SensorMPU9X50.GuiLabelConfig.MPU9X50_MPL_LPF,
			SensorMPU9150.DatabaseConfigHandle.MPU_LPF,
			ListofMPU9150MplLpfOptions, 
			ListofMPU9150MplLpfOptionsConfigValues, 
			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors);
	
	public static final ConfigOptionDetailsSensor configOptionMpu9150MplRate = new ConfigOptionDetailsSensor(
			SensorMPU9X50.GuiLabelConfig.MPU9X50_MPL_RATE,
			SensorMPU9150.DatabaseConfigHandle.MPU_MPL_SAMPLING_RATE,
			ListofMPU9150MplRate, 
			ListofMPU9150MplRateConfigValues, 
			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors);
	
	public static final ConfigOptionDetailsSensor configOptionMpu9150MagRate = new ConfigOptionDetailsSensor(
			SensorMPU9X50.GuiLabelConfig.MPU9X50_MAG_RATE,
			SensorMPU9150.DatabaseConfigHandle.MPU_MAG_SAMPLING_RATE,
			ListofMPU9X50MagRate, 
			ListofMPU9X50MagRateConfigValues, 
			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors);
	//MPL CheckBoxes
	public static final ConfigOptionDetailsSensor configOptionMpu9150Dmp = new ConfigOptionDetailsSensor(
			SensorMPU9X50.GuiLabelConfig.MPU9X50_DMP,
			SensorMPU9150.DatabaseConfigHandle.MPU_DMP,
			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.CHECKBOX,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors);
	
	public static final ConfigOptionDetailsSensor configOptionMpu9150Mpl = new ConfigOptionDetailsSensor(
			SensorMPU9X50.GuiLabelConfig.MPU9X50_MPL,
			SensorMPU9150.DatabaseConfigHandle.MPU_MPL_ENABLE,
			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.CHECKBOX,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors);
	
	public static final ConfigOptionDetailsSensor configOptionMpu9150Mpl9DofSensorFusion = new ConfigOptionDetailsSensor(
			SensorMPU9X50.GuiLabelConfig.MPU9X50_MPL_9DOF_SENSOR_FUSION,
			SensorMPU9150.DatabaseConfigHandle.MPU_MPL_SENSOR_FUSION,
			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.CHECKBOX,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors);

	public static final ConfigOptionDetailsSensor configOptionMpu9150MplGyroCal = new ConfigOptionDetailsSensor(
			SensorMPU9X50.GuiLabelConfig.MPU9X50_MPL_GYRO_CAL,
			SensorMPU9150.DatabaseConfigHandle.MPU_GYRO,
			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.CHECKBOX,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors);

	public static final ConfigOptionDetailsSensor configOptionMpu9150MplVectorCal = new ConfigOptionDetailsSensor(
			SensorMPU9X50.GuiLabelConfig.MPU9X50_MPL_VECTOR_CAL,
			SensorMPU9150.DatabaseConfigHandle.MPU_MPL_VECT_COMP,
			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.CHECKBOX,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors);

	public static final ConfigOptionDetailsSensor configOptionMpu9150MplMagCal = new ConfigOptionDetailsSensor(
			SensorMPU9X50.GuiLabelConfig.MPU9X50_MPL_MAG_CAL,
			SensorMPU9150.DatabaseConfigHandle.MPU_MAG,
			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.CHECKBOX,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors);
	//General Config
	public static final ConfigOptionDetailsSensor configOptionMpu9150GyroRate = new ConfigOptionDetailsSensor(
			SensorMPU9X50.GuiLabelConfig.MPU9X50_GYRO_RATE,
			SensorMPU9150.DatabaseConfigHandle.GYRO_RATE,
			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.TEXTFIELD,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW);

	public static final ConfigOptionDetailsSensor configOptionMpu9150GyroLpm = new ConfigOptionDetailsSensor(
			SensorMPU9X50.GuiLabelConfig.MPU9X50_GYRO_LPM,
			null,
			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.CHECKBOX,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW);
	//--------- Configuration options end --------------

	
    //--------- Constructors for this class start --------------

	/**Just used for accessing calibration*/
	public SensorMPU9150(){
		super(SENSORS.MPU9X50);
		initialise();
	}

	/** Constructor for this class
	 * @param svo
	 */
	public SensorMPU9150(ShimmerDevice shimmerDevice){
		super(SENSORS.MPU9X50, shimmerDevice);
		initialise();
	}

	@Override
	public void initialise() {
		mSensorIdGyro = Configuration.Shimmer3.SENSOR_ID.SHIMMER_MPU9X50_GYRO;
		mSensorIdAccel = Configuration.Shimmer3.SENSOR_ID.SHIMMER_MPU9X50_ACCEL;
		mSensorIdMag = Configuration.Shimmer3.SENSOR_ID.SHIMMER_MPU9X50_MAG;

		super.initialise();

		setCalibSensitivityScaleFactor(mSensorIdGyro, CALIBRATION_SCALE_FACTOR.ONE_HUNDRED);
		updateCurrentGyroCalibInUse();
	}
	
    //--------- Constructors for this class end --------------

	@Override
	public void generateConfigOptionsMap() {
		mConfigOptionsMap.clear();
		addConfigOption(configOptionMpu9150GyroRange);
		//MPL Options
		addConfigOption(configOptionMpu9150AccelRange);
		addConfigOption(configOptionMpu9150DmpGyroCal);
		addConfigOption(configOptionMpu9150MplLpf);
		addConfigOption(configOptionMpu9150MplRate);
		addConfigOption(configOptionMpu9150MagRate);
		//MPL CheckBoxes
		addConfigOption(configOptionMpu9150Dmp);
		addConfigOption(configOptionMpu9150Mpl);
		addConfigOption(configOptionMpu9150Mpl9DofSensorFusion);
		addConfigOption(configOptionMpu9150MplGyroCal);
		addConfigOption(configOptionMpu9150MplVectorCal);
		addConfigOption(configOptionMpu9150MplMagCal);
		//General Config
		addConfigOption(configOptionMpu9150GyroRate);
		addConfigOption(configOptionMpu9150GyroLpm);
	}

	@Override
	public void generateSensorMap() {
		LinkedHashMap<Integer, SensorDetailsRef> sensorMap = new LinkedHashMap<Integer, SensorDetailsRef>();
		sensorMap.putAll(mSensorMapRefCommon);
		sensorMap.putAll(mSensorMapRef);
		super.createLocalSensorMapWithCustomParser(sensorMap, mChannelMapRef);
	}
	
	@Override
	public void generateSensorGroupMapping() {
		mSensorGroupingMap = new LinkedHashMap<Integer, SensorGroupingDetails>();

		if(mShimmerVerObject.isShimmerGen3()){
			mSensorGroupingMap.put(Configuration.Shimmer3.LABEL_SENSOR_TILE.GYRO.ordinal(), new SensorGroupingDetails(
					LABEL_SENSOR_TILE.GYRO,
					Arrays.asList(mSensorIdGyro),
					CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW));
		}

		// RM commented out the MPU channels prior to ConsensysPRO release as the firmware and maybe software also need update to support these channels
		if(mShimmerVerObject.isSupportedMpl()){
//			mSensorGroupingMap.put(Configuration.Shimmer3.LABEL_SENSOR_TILE.MPU_ACCEL_GYRO_MAG.ordinal(), new SensorGroupingDetails(
//					LABEL_SENSOR_TILE.MPU_ACCEL_GYRO_MAG,
//					Arrays.asList(Configuration.Shimmer3.SENSOR_ID.SHIMMER_MPU9150_MPL_ACCEL,
//							Configuration.Shimmer3.SENSOR_ID.SHIMMER_MPU9150_MPL_GYRO,
//							Configuration.Shimmer3.SENSOR_ID.SHIMMER_MPU9150_MPL_MAG),
//					CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors));
//			
//			mSensorGroupingMap.put(Configuration.Shimmer3.LABEL_SENSOR_TILE.MPU_OTHER.ordinal(), new SensorGroupingDetails(
//					LABEL_SENSOR_TILE.MPU_OTHER,
//					Arrays.asList(Configuration.Shimmer3.SENSOR_ID.SHIMMER_MPU9X50_TEMP,
//								Configuration.Shimmer3.SENSOR_ID.SHIMMER_MPU9150_MPL_QUAT_6DOF),
//					CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors));
		}
		
		super.updateSensorGroupingMap();
	}
	
	
	
	@Override
	public LinkedHashMap<String, Object> generateConfigMap() {
		LinkedHashMap<String, Object> mapOfConfig = new LinkedHashMap<String, Object>();
		mapOfConfig.put(SensorMPU9150.DatabaseConfigHandle.GYRO_RATE, getMPU9X50GyroAccelRate());
		mapOfConfig.put(SensorMPU9150.DatabaseConfigHandle.GYRO_RANGE, getGyroRange());

		mapOfConfig.put(SensorMPU9150.DatabaseConfigHandle.ALTERNATIVE_ACC_RANGE, getMPU9X50AccelRange());

		mapOfConfig.put(SensorMPU9150.DatabaseConfigHandle.MPU_DMP, getMPU9X50DMP());
		mapOfConfig.put(SensorMPU9150.DatabaseConfigHandle.MPU_LPF, getMPU9X50LPF());
		mapOfConfig.put(SensorMPU9150.DatabaseConfigHandle.MPU_MOT_CAL_CFG, getMPU9X50MotCalCfg());
		mapOfConfig.put(SensorMPU9150.DatabaseConfigHandle.MPU_MPL_SAMPLING_RATE, getMPU9X50MPLSamplingRate());
		mapOfConfig.put(SensorMPU9150.DatabaseConfigHandle.MPU_MAG_SAMPLING_RATE, getMPU9X50MagSamplingRate());

		mapOfConfig.put(SensorMPU9150.DatabaseConfigHandle.MPU_MPL_SENSOR_FUSION, (double) getMPLSensorFusion());
		mapOfConfig.put(SensorMPU9150.DatabaseConfigHandle.MPU_MPL_GYRO_TC, (double) getMPLGyroCalTC());
		mapOfConfig.put(SensorMPU9150.DatabaseConfigHandle.MPU_MPL_VECT_COMP, (double) getMPLVectCompCal());
		mapOfConfig.put(SensorMPU9150.DatabaseConfigHandle.MPU_MAG_DIST, (double) getMPLMagDistCal());
		mapOfConfig.put(SensorMPU9150.DatabaseConfigHandle.MPU_MPL_ENABLE, (double) getMPLEnable());			
		
		
		AbstractSensor.addCalibDetailsToDbMap(mapOfConfig, 
				getCurrentCalibDetailsGyro(), 
				SensorMPU9150.DatabaseConfigHandle.LIST_OF_CALIB_HANDLES_GYRO,
				SensorMPU9150.DatabaseConfigHandle.GYRO_CALIB_TIME);
		
		AbstractSensor.addCalibDetailsToDbMap(mapOfConfig, 
				SensorMPU9150.DatabaseConfigHandle.LIST_OF_CALIB_HANDLES_MPU_MPL_ACC,
				getOffsetVectorMPLAccel(),
				getSensitivityMatrixMPLAccel(),
				getAlignmentMatrixMPLAccel());
		
		AbstractSensor.addCalibDetailsToDbMap(mapOfConfig, 
				SensorMPU9150.DatabaseConfigHandle.LIST_OF_CALIB_HANDLES_MPU_MPL_MAG,
				getOffsetVectorMPLMag(),
				getSensitivityMatrixMPLMag(),
				getAlignmentMatrixMPLMag());

		AbstractSensor.addCalibDetailsToDbMap(mapOfConfig, 
				SensorMPU9150.DatabaseConfigHandle.LIST_OF_CALIB_HANDLES_MPU_MPL_GYRO,
				getOffsetVectorMPLGyro(),
				getSensitivityMatrixMPLGyro(),
				getAlignmentMatrixMPLGyro());

		return mapOfConfig;
	}
	
	@Override
	public void parseConfigMap(LinkedHashMap<String, Object> mapOfConfigPerShimmer) {
		if(mapOfConfigPerShimmer.containsKey(DatabaseConfigHandle.GYRO_RATE)){
			setMPU9X50GyroAccelRate(((Double) mapOfConfigPerShimmer.get(DatabaseConfigHandle.GYRO_RATE)).intValue());
		}
		if(mapOfConfigPerShimmer.containsKey(DatabaseConfigHandle.GYRO_RANGE)){
			setGyroRange(((Double) mapOfConfigPerShimmer.get(DatabaseConfigHandle.GYRO_RANGE)).intValue());
		}
		if(mapOfConfigPerShimmer.containsKey(DatabaseConfigHandle.ALTERNATIVE_ACC_RANGE)){
			setMPU9X50AccelRange(((Double) mapOfConfigPerShimmer.get(DatabaseConfigHandle.ALTERNATIVE_ACC_RANGE)).intValue());
		}
		if(mapOfConfigPerShimmer.containsKey(DatabaseConfigHandle.MPU_DMP)){
			setMPU9150DMP(((Double) mapOfConfigPerShimmer.get(DatabaseConfigHandle.MPU_DMP)).intValue());
		}
		if(mapOfConfigPerShimmer.containsKey(DatabaseConfigHandle.MPU_LPF)){
			setMPU9X50LPF(((Double) mapOfConfigPerShimmer.get(DatabaseConfigHandle.MPU_LPF)).intValue());
		}
		if(mapOfConfigPerShimmer.containsKey(DatabaseConfigHandle.MPU_MOT_CAL_CFG)){
			setMPU9X150MotCalCfg(((Double) mapOfConfigPerShimmer.get(DatabaseConfigHandle.MPU_MOT_CAL_CFG)).intValue());
		}
		if(mapOfConfigPerShimmer.containsKey(DatabaseConfigHandle.MPU_MPL_SAMPLING_RATE)){
			setMPU9150MPLSamplingRate(((Double) mapOfConfigPerShimmer.get(DatabaseConfigHandle.MPU_MPL_SAMPLING_RATE)).intValue());
		}
		if(mapOfConfigPerShimmer.containsKey(DatabaseConfigHandle.MPU_MAG_SAMPLING_RATE)){
			setMPU9X50MagSamplingRate(((Double) mapOfConfigPerShimmer.get(DatabaseConfigHandle.MPU_MAG_SAMPLING_RATE)).intValue());
		}
		if(mapOfConfigPerShimmer.containsKey(DatabaseConfigHandle.MPU_MPL_SENSOR_FUSION)){
			setMPLSensorFusion(((Double) mapOfConfigPerShimmer.get(DatabaseConfigHandle.MPU_MPL_SENSOR_FUSION))>0? true:false);
		}
		if(mapOfConfigPerShimmer.containsKey(DatabaseConfigHandle.MPU_MPL_GYRO_TC)){
			setMPLGyroCalTC(((Double) mapOfConfigPerShimmer.get(DatabaseConfigHandle.MPU_MPL_GYRO_TC))>0? true:false);
		}
		if(mapOfConfigPerShimmer.containsKey(DatabaseConfigHandle.MPU_MPL_VECT_COMP)){
			setMPLVectCompCal(((Double) mapOfConfigPerShimmer.get(DatabaseConfigHandle.MPU_MPL_VECT_COMP))>0? true:false);
		}
		if(mapOfConfigPerShimmer.containsKey(DatabaseConfigHandle.MPU_MAG_DIST)){
			setMPLMagDistCal(((Double) mapOfConfigPerShimmer.get(DatabaseConfigHandle.MPU_MAG_DIST))>0? true:false);
		}
		if(mapOfConfigPerShimmer.containsKey(DatabaseConfigHandle.MPU_MPL_ENABLE)){
			setMPLEnabled(((Double) mapOfConfigPerShimmer.get(DatabaseConfigHandle.MPU_MPL_ENABLE))>0? true:false);
		}
		
		//Gyroscope Calibration Configuration
		parseCalibDetailsKinematicFromDb(mapOfConfigPerShimmer, 
				mSensorIdGyro, 
				getGyroRange(), 
				SensorMPU9150.DatabaseConfigHandle.LIST_OF_CALIB_HANDLES_GYRO,
				SensorMPU9150.DatabaseConfigHandle.GYRO_CALIB_TIME);
		
		//TODO
		//MPL Accel Calibration Configuration
		parseCalibDetailsKinematicFromDb(mapOfConfigPerShimmer, 
				Configuration.Shimmer3.SENSOR_ID.SHIMMER_MPU9X50_MPL_ACCEL, 
				getMPU9X50AccelRange(), 
				SensorMPU9150.DatabaseConfigHandle.LIST_OF_CALIB_HANDLES_MPU_MPL_ACC);

		//TODO
		//MPL Mag Calibration Configuration
		parseCalibDetailsKinematicFromDb(mapOfConfigPerShimmer, 
				Configuration.Shimmer3.SENSOR_ID.SHIMMER_MPU9X50_MPL_MAG, 
				0, 
				SensorMPU9150.DatabaseConfigHandle.LIST_OF_CALIB_HANDLES_MPU_MPL_MAG);
		
		//TODO
		//MPL Gyro Calibration Configuration
		parseCalibDetailsKinematicFromDb(mapOfConfigPerShimmer, 
				Configuration.Shimmer3.SENSOR_ID.SHIMMER_MPU9X50_MPL_GYRO, 
				getGyroRange(), 
				SensorMPU9150.DatabaseConfigHandle.LIST_OF_CALIB_HANDLES_MPU_MPL_GYRO);
	}
	
	
	
	public static String parseFromDBColumnToGUIChannel(String databaseChannelHandle) {
		//TODO Old approach, can be removed
//		String objectClusterName = "";
//		if (databaseChannelHandle.equals(SensorMPU9X50.DatabaseChannelHandles.GYRO_X)) {
//			objectClusterName = SensorMPU9X50.ObjectClusterSensorName.GYRO_X;
//		} else if (databaseChannelHandle.equals(SensorMPU9X50.DatabaseChannelHandles.GYRO_Y)) {
//			objectClusterName = SensorMPU9X50.ObjectClusterSensorName.GYRO_Y;
//		} else if (databaseChannelHandle.equals(SensorMPU9X50.DatabaseChannelHandles.GYRO_Z)) {
//			objectClusterName = SensorMPU9X50.ObjectClusterSensorName.GYRO_Z;
//			
//		} else if (databaseChannelHandle.equals(SensorMPU9X50.DatabaseChannelHandles.ALTERNATIVE_ACC_X)) {
//			objectClusterName = SensorMPU9X50.ObjectClusterSensorName.ACCEL_MPU_X;
//		} else if (databaseChannelHandle.equals(SensorMPU9X50.DatabaseChannelHandles.ALTERNATIVE_ACC_Y)) {
//			objectClusterName = SensorMPU9X50.ObjectClusterSensorName.ACCEL_MPU_Y;
//		} else if (databaseChannelHandle.equals(SensorMPU9X50.DatabaseChannelHandles.ALTERNATIVE_ACC_Z)) {
//			objectClusterName = SensorMPU9X50.ObjectClusterSensorName.ACCEL_MPU_Z;
//		
//		} else if (databaseChannelHandle.equals(SensorMPU9X50.DatabaseChannelHandles.MPU_QUAT_6DOF_W)) {
//			objectClusterName = SensorMPU9X50.ObjectClusterSensorName.QUAT_MPL_6DOF_W;
//		} else if (databaseChannelHandle.equals(SensorMPU9X50.DatabaseChannelHandles.MPU_QUAT_6DOF_X)) {
//			objectClusterName = SensorMPU9X50.ObjectClusterSensorName.QUAT_MPL_6DOF_X;
//		} else if (databaseChannelHandle.equals(SensorMPU9X50.DatabaseChannelHandles.MPU_QUAT_6DOF_Y)) {
//			objectClusterName = SensorMPU9X50.ObjectClusterSensorName.QUAT_MPL_6DOF_Y;
//		} else if (databaseChannelHandle.equals(SensorMPU9X50.DatabaseChannelHandles.MPU_QUAT_6DOF_Z)) {
//			objectClusterName = SensorMPU9X50.ObjectClusterSensorName.QUAT_MPL_6DOF_Z;
//		} else if (databaseChannelHandle.equals(SensorMPU9X50.DatabaseChannelHandles.MPU_EULER_6DOF_X)) {
//			objectClusterName = SensorMPU9X50.ObjectClusterSensorName.EULER_MPL_6DOF_X;
//		} else if (databaseChannelHandle.equals(SensorMPU9X50.DatabaseChannelHandles.MPU_EULER_6DOF_Y)) {
//			objectClusterName = SensorMPU9X50.ObjectClusterSensorName.EULER_MPL_6DOF_Y;
//		} else if (databaseChannelHandle.equals(SensorMPU9X50.DatabaseChannelHandles.MPU_EULER_6DOF_Z)) {
//			objectClusterName = SensorMPU9X50.ObjectClusterSensorName.EULER_MPL_6DOF_Z;
//		} else if (databaseChannelHandle.equals(SensorMPU9X50.DatabaseChannelHandles.MPU_HEADING)) {
//			objectClusterName = SensorMPU9X50.ObjectClusterSensorName.MPL_HEADING;
//		} else if (databaseChannelHandle.equals(SensorMPU9X50.DatabaseChannelHandles.MPU_TEMP)) {
//			objectClusterName = SensorMPU9X50.ObjectClusterSensorName.MPL_TEMPERATURE;
//		} else if (databaseChannelHandle.equals(SensorMPU9X50.DatabaseChannelHandles.PEDOMETER_CNT)) {
//			objectClusterName = SensorMPU9X50.ObjectClusterSensorName.MPL_PEDOM_CNT;
//		} else if (databaseChannelHandle.equals(SensorMPU9X50.DatabaseChannelHandles.PEDOMETER_TIME)) {
//			objectClusterName = SensorMPU9X50.ObjectClusterSensorName.MPL_PEDOM_TIME;
//		} else if (databaseChannelHandle.equals(SensorMPU9X50.DatabaseChannelHandles.TAP_DIR_AND_CNT)) {
//			objectClusterName = SensorMPU9X50.ObjectClusterSensorName.TAPDIRANDTAPCNT;
//		} else if (databaseChannelHandle.equals(SensorMPU9X50.DatabaseChannelHandles.MOTION_AND_ORIENT)) {
//			objectClusterName = SensorMPU9X50.ObjectClusterSensorName.MOTIONANDORIENT;
//		} else if (databaseChannelHandle.equals(SensorMPU9X50.DatabaseChannelHandles.MPU_MPL_GYRO_X)) {
//			objectClusterName = SensorMPU9X50.ObjectClusterSensorName.GYRO_MPU_MPL_X;
//		} else if (databaseChannelHandle.equals(SensorMPU9X50.DatabaseChannelHandles.MPU_MPL_GYRO_Y)) {
//			objectClusterName = SensorMPU9X50.ObjectClusterSensorName.GYRO_MPU_MPL_Y;
//		} else if (databaseChannelHandle.equals(SensorMPU9X50.DatabaseChannelHandles.MPU_MPL_GYRO_Z)) {
//			objectClusterName = SensorMPU9X50.ObjectClusterSensorName.GYRO_MPU_MPL_Z;
//		} else if (databaseChannelHandle.equals(SensorMPU9X50.DatabaseChannelHandles.MPU_MPL_ACC_X)) {
//			objectClusterName = SensorMPU9X50.ObjectClusterSensorName.ACCEL_MPU_MPL_X;
//		} else if (databaseChannelHandle.equals(SensorMPU9X50.DatabaseChannelHandles.MPU_MPL_ACC_Y)) {
//			objectClusterName = SensorMPU9X50.ObjectClusterSensorName.ACCEL_MPU_MPL_Y;
//		} else if (databaseChannelHandle.equals(SensorMPU9X50.DatabaseChannelHandles.MPU_MPL_ACC_Z)) {
//			objectClusterName = SensorMPU9X50.ObjectClusterSensorName.ACCEL_MPU_MPL_Z;
//		} else if (databaseChannelHandle.equals(SensorMPU9X50.DatabaseChannelHandles.MPU_MPL_MAG_X)) {
//			objectClusterName = SensorMPU9X50.ObjectClusterSensorName.MAG_MPU_MPL_X;
//		} else if (databaseChannelHandle.equals(SensorMPU9X50.DatabaseChannelHandles.MPU_MPL_MAG_Y)) {
//			objectClusterName = SensorMPU9X50.ObjectClusterSensorName.MAG_MPU_MPL_Y;
//		} else if (databaseChannelHandle.equals(SensorMPU9X50.DatabaseChannelHandles.MPU_MPL_MAG_Z)) {
//			objectClusterName = SensorMPU9X50.ObjectClusterSensorName.MAG_MPU_MPL_Z;
//		} else if (databaseChannelHandle.equals(SensorMPU9X50.DatabaseChannelHandles.MPU_QUAT_6DOF_DMP_W)) {
//			objectClusterName = SensorMPU9X50.ObjectClusterSensorName.QUAT_DMP_6DOF_W;
//		} else if (databaseChannelHandle.equals(SensorMPU9X50.DatabaseChannelHandles.MPU_QUAT_6DOF_DMP_X)) {
//			objectClusterName = SensorMPU9X50.ObjectClusterSensorName.QUAT_DMP_6DOF_X;
//		} else if (databaseChannelHandle.equals(SensorMPU9X50.DatabaseChannelHandles.MPU_QUAT_6DOF_DMP_Y)) {
//			objectClusterName = SensorMPU9X50.ObjectClusterSensorName.QUAT_DMP_6DOF_Y;
//		} else if (databaseChannelHandle.equals(SensorMPU9X50.DatabaseChannelHandles.MPU_QUAT_6DOF_DMP_Z)) {
//			objectClusterName = SensorMPU9X50.ObjectClusterSensorName.QUAT_DMP_6DOF_Z;
//		}
//		return objectClusterName;
		
		return AbstractSensor.parseFromDBColumnToGUIChannel(mChannelMapRef, databaseChannelHandle);
	}

	public static String parseFromGUIChannelsToDBColumn(String objectClusterName) {
		//TODO Old approach, can be removed
//		String databaseChannelHandle = "";
//		if (objectClusterName.equals(SensorMPU9X50.ObjectClusterSensorName.GYRO_X)) {
//			databaseChannelHandle = SensorMPU9X50.DatabaseChannelHandles.GYRO_X;
//		} else if (objectClusterName.equals(SensorMPU9X50.ObjectClusterSensorName.GYRO_Y)) {
//			databaseChannelHandle = SensorMPU9X50.DatabaseChannelHandles.GYRO_Y;
//		} else if (objectClusterName.equals(SensorMPU9X50.ObjectClusterSensorName.GYRO_Z)) {
//			databaseChannelHandle = SensorMPU9X50.DatabaseChannelHandles.GYRO_Z;
//			
//		} else if (objectClusterName.equals(SensorMPU9X50.ObjectClusterSensorName.ACCEL_MPU_X)) {
//			databaseChannelHandle = SensorMPU9X50.DatabaseChannelHandles.ALTERNATIVE_ACC_X;
//		} else if (objectClusterName.equals(SensorMPU9X50.ObjectClusterSensorName.ACCEL_MPU_Y)) {
//			databaseChannelHandle = SensorMPU9X50.DatabaseChannelHandles.ALTERNATIVE_ACC_Y;
//		} else if (objectClusterName.equals(SensorMPU9X50.ObjectClusterSensorName.ACCEL_MPU_Z)) {
//			databaseChannelHandle = SensorMPU9X50.DatabaseChannelHandles.ALTERNATIVE_ACC_Z;
//	
//		} else if (objectClusterName.equals(SensorMPU9X50.ObjectClusterSensorName.QUAT_MPL_6DOF_W)) {
//			databaseChannelHandle = SensorMPU9X50.DatabaseChannelHandles.MPU_QUAT_6DOF_W;
//		} else if (objectClusterName.equals(SensorMPU9X50.ObjectClusterSensorName.QUAT_MPL_6DOF_X)) {
//			databaseChannelHandle = SensorMPU9X50.DatabaseChannelHandles.MPU_QUAT_6DOF_X;
//		} else if (objectClusterName.equals(SensorMPU9X50.ObjectClusterSensorName.QUAT_MPL_6DOF_Y)) {
//			databaseChannelHandle = SensorMPU9X50.DatabaseChannelHandles.MPU_QUAT_6DOF_Y;
//		} else if (objectClusterName.equals(SensorMPU9X50.ObjectClusterSensorName.QUAT_MPL_6DOF_Z)) {
//			databaseChannelHandle = SensorMPU9X50.DatabaseChannelHandles.MPU_QUAT_6DOF_Z;
//		} else if (objectClusterName.equals(SensorMPU9X50.ObjectClusterSensorName.EULER_MPL_6DOF_X)) {
//			databaseChannelHandle = SensorMPU9X50.DatabaseChannelHandles.MPU_EULER_6DOF_X;
//		} else if (objectClusterName.equals(SensorMPU9X50.ObjectClusterSensorName.EULER_MPL_6DOF_Y)) {
//			databaseChannelHandle = SensorMPU9X50.DatabaseChannelHandles.MPU_EULER_6DOF_Y;
//		} else if (objectClusterName.equals(SensorMPU9X50.ObjectClusterSensorName.EULER_MPL_6DOF_Z)) {
//			databaseChannelHandle = SensorMPU9X50.DatabaseChannelHandles.MPU_EULER_6DOF_Z;
//		} else if (objectClusterName.equals(SensorMPU9X50.ObjectClusterSensorName.MPL_HEADING)) {
//			databaseChannelHandle = SensorMPU9X50.DatabaseChannelHandles.MPU_HEADING;
//		} else if (objectClusterName.equals(SensorMPU9X50.ObjectClusterSensorName.MPL_TEMPERATURE)) {
//			databaseChannelHandle = SensorMPU9X50.DatabaseChannelHandles.MPU_TEMP;
//		} else if (objectClusterName.equals(SensorMPU9X50.ObjectClusterSensorName.MPL_PEDOM_CNT)) {
//			databaseChannelHandle = SensorMPU9X50.DatabaseChannelHandles.PEDOMETER_CNT;
//		} else if (objectClusterName.equals(SensorMPU9X50.ObjectClusterSensorName.MPL_PEDOM_TIME)) {
//			databaseChannelHandle = SensorMPU9X50.DatabaseChannelHandles.PEDOMETER_TIME;
//		} else if (objectClusterName.equals(SensorMPU9X50.ObjectClusterSensorName.TAPDIRANDTAPCNT)) {
//			databaseChannelHandle = SensorMPU9X50.DatabaseChannelHandles.TAP_DIR_AND_CNT;
//		} else if (objectClusterName.equals(SensorMPU9X50.ObjectClusterSensorName.MOTIONANDORIENT)) {
//			databaseChannelHandle = SensorMPU9X50.DatabaseChannelHandles.MOTION_AND_ORIENT;
//		} else if (objectClusterName.equals(SensorMPU9X50.ObjectClusterSensorName.GYRO_MPU_MPL_X)) {
//			databaseChannelHandle = SensorMPU9X50.DatabaseChannelHandles.MPU_MPL_GYRO_X;
//		} else if (objectClusterName.equals(SensorMPU9X50.ObjectClusterSensorName.GYRO_MPU_MPL_Y)) {
//			databaseChannelHandle = SensorMPU9X50.DatabaseChannelHandles.MPU_MPL_GYRO_Y;
//		} else if (objectClusterName.equals(SensorMPU9X50.ObjectClusterSensorName.GYRO_MPU_MPL_Z)) {
//			databaseChannelHandle = SensorMPU9X50.DatabaseChannelHandles.MPU_MPL_GYRO_Z;
//		} else if (objectClusterName.equals(SensorMPU9X50.ObjectClusterSensorName.ACCEL_MPU_MPL_X)) {
//			databaseChannelHandle = SensorMPU9X50.DatabaseChannelHandles.MPU_MPL_ACC_X;
//		} else if (objectClusterName.equals(SensorMPU9X50.ObjectClusterSensorName.ACCEL_MPU_MPL_Y)) {
//			databaseChannelHandle = SensorMPU9X50.DatabaseChannelHandles.MPU_MPL_ACC_Y;
//		} else if (objectClusterName.equals(SensorMPU9X50.ObjectClusterSensorName.ACCEL_MPU_MPL_Z)) {
//			databaseChannelHandle = SensorMPU9X50.DatabaseChannelHandles.MPU_MPL_ACC_Z;
//		} else if (objectClusterName.equals(SensorMPU9X50.ObjectClusterSensorName.MAG_MPU_MPL_X)) {
//			databaseChannelHandle = SensorMPU9X50.DatabaseChannelHandles.MPU_MPL_MAG_X;
//		} else if (objectClusterName.equals(SensorMPU9X50.ObjectClusterSensorName.MAG_MPU_MPL_Y)) {
//			databaseChannelHandle = SensorMPU9X50.DatabaseChannelHandles.MPU_MPL_MAG_Y;
//		} else if (objectClusterName.equals(SensorMPU9X50.ObjectClusterSensorName.MAG_MPU_MPL_Z)) {
//			databaseChannelHandle = SensorMPU9X50.DatabaseChannelHandles.MPU_MPL_MAG_Z;
//		} else if (objectClusterName.equals(SensorMPU9X50.ObjectClusterSensorName.QUAT_DMP_6DOF_W)) {
//			databaseChannelHandle = SensorMPU9X50.DatabaseChannelHandles.MPU_QUAT_6DOF_DMP_W;
//		} else if (objectClusterName.equals(SensorMPU9X50.ObjectClusterSensorName.QUAT_DMP_6DOF_X)) {
//			databaseChannelHandle = SensorMPU9X50.DatabaseChannelHandles.MPU_QUAT_6DOF_DMP_X;
//		} else if (objectClusterName.equals(SensorMPU9X50.ObjectClusterSensorName.QUAT_DMP_6DOF_Y)) {
//			databaseChannelHandle = SensorMPU9X50.DatabaseChannelHandles.MPU_QUAT_6DOF_DMP_Y;
//		} else if (objectClusterName.equals(SensorMPU9X50.ObjectClusterSensorName.QUAT_DMP_6DOF_Z)) {
//			databaseChannelHandle = SensorMPU9X50.DatabaseChannelHandles.MPU_QUAT_6DOF_DMP_Z;
//		}
//		return databaseChannelHandle;
		
		return AbstractSensor.parseFromGUIChannelsToDBColumn(mChannelMapRef, objectClusterName);
	}
}
