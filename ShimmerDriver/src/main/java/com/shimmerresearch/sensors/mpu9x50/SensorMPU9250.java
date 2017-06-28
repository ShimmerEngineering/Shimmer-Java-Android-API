package com.shimmerresearch.sensors.mpu9x50;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driver.Configuration.CHANNEL_UNITS;
import com.shimmerresearch.driver.calibration.CalibDetailsKinematic.CALIBRATION_SCALE_FACTOR;
import com.shimmerresearch.driverUtilities.ChannelDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_ENDIAN;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_TYPE;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_TYPE;
import com.shimmerresearch.sensors.AbstractSensor;
import com.shimmerresearch.sensors.mpu9x50.SensorMPU9150.DatabaseChannelHandles;
import com.shimmerresearch.sensors.mpu9x50.SensorMPU9150.DatabaseConfigHandle;
import com.shimmerresearch.sensors.mpu9x50.SensorMPU9X50.ObjectClusterSensorName;

public class SensorMPU9250 extends SensorMPU9X50 {

	private static final long serialVersionUID = 6559532137082204767L;

	
	//--------- Sensor specific variables start --------------
	public static class DatabaseChannelHandles{
		
		public static final String MPU_HEADING = "MPU9250_MPL_Heading"; // not available but supported in FW
		public static final String MPU_TEMP = "MPU9250_Temperature";

		public static final String GYRO_X = "MPU9250_GYRO_X";
		public static final String GYRO_Y = "MPU9250_GYRO_Y";
		public static final String GYRO_Z = "MPU9250_GYRO_Z";

		public static final String ALTERNATIVE_ACC_X = "MPU9250_ACC_X"; // not available but supported in FW
		public static final String ALTERNATIVE_ACC_Y = "MPU9250_ACC_Y"; // not available but supported in FW
		public static final String ALTERNATIVE_ACC_Z = "MPU9250_ACC_Z"; // not available but supported in FW
		public static final String ALTERNATIVE_MAG_X = "MPU9250_MAG_X"; // not available but supported in FW
		public static final String ALTERNATIVE_MAG_Y = "MPU9250_MAG_Y"; // not available but supported in FW
		public static final String ALTERNATIVE_MAG_Z = "MPU9250_MAG_Z"; // not available but supported in FW
		
		public static final String MPU_QUAT_6DOF_W = "MPU9250_MPL_QUAT_6DOF_W";
		public static final String MPU_QUAT_6DOF_X = "MPU9250_MPL_QUAT_6DOF_X";
		public static final String MPU_QUAT_6DOF_Y = "MPU9250_MPL_QUAT_6DOF_Y";
		public static final String MPU_QUAT_6DOF_Z = "MPU9250_MPL_QUAT_6DOF_Z";
		public static final String MPU_QUAT_9DOF_W = "MPU9250_MPL_QUAT_9DOF_W";
		public static final String MPU_QUAT_9DOF_X = "MPU9250_MPL_QUAT_9DOF_X";
		public static final String MPU_QUAT_9DOF_Y = "MPU9250_MPL_QUAT_9DOF_Y";
		public static final String MPU_QUAT_9DOF_Z = "MPU9250_MPL_QUAT_9DOF_Z";
		public static final String MPU_EULER_6DOF_X = "MPU9250_MPL_EULER_6DOF_X"; // not available but supported in FW
		public static final String MPU_EULER_6DOF_Y = "MPU9250_MPL_EULER_6DOF_Y"; // not available but supported in FW
		public static final String MPU_EULER_6DOF_Z = "MPU9250_MPL_EULER_6DOF_Z"; // not available but supported in FW
		public static final String MPU_EULER_9DOF_X = "MPU9250_MPL_EULER_9DOF_X"; // not available but supported in FW
		public static final String MPU_EULER_9DOF_Y = "MPU9250_MPL_EULER_9DOF_Y"; // not available but supported in FW
		public static final String MPU_EULER_9DOF_Z = "MPU9250_MPL_EULER_9DOF_Z"; // not available but supported in FW
//		public static final String MPU_HEADING = "MPU9250_MPL_HEADING"; -> already define for the shimmerCongig Table
//		public static final String MPU_TEMP = "MPU9250_Temperature"; -> already define for the shimmerCongig Table
		public static final String PEDOMETER_CNT = "MPU9250_MPL_PEDOM_CNT"; // not available but supported in FW
		public static final String PEDOMETER_TIME = "MPU9250_MPL_PEDOM_TIME"; // not available but supported in FW
		public static final String TAP_DIR_AND_CNT = "MPU9250_MPL_TAP"; // not available but supported in FW
		public static final String TAP_DIR = "MPU9250_MPL_TAP_DIR"; // not available but supported in FW
		public static final String TAP_CNT = "MPU9250_MPL_TAP_CNT"; // not available but supported in FW
		public static final String MOTION_AND_ORIENT = "MPU9250_MPL_MOTION_AND_ORIENT"; // not available but supported in FW
		public static final String MOTION = "MPU9250_MPL_MOTION"; // not available but supported in FW
		public static final String ORIENT = "MPU9250_MPL_ORIENT"; // not available but supported in FW

		public static final String MPU_MPL_GYRO_X = "MPU9250_MPL_GYRO_X_CAL";
		public static final String MPU_MPL_GYRO_Y = "MPU9250_MPL_GYRO_Y_CAL";
		public static final String MPU_MPL_GYRO_Z = "MPU9250_MPL_GYRO_Z_CAL";
		public static final String MPU_MPL_ACC_X = "MPU9250_MPL_ACC_X_CAL";
		public static final String MPU_MPL_ACC_Y = "MPU9250_MPL_ACC_Y_CAL";
		public static final String MPU_MPL_ACC_Z = "MPU9250_MPL_ACC_Z_CAL";
		public static final String MPU_MPL_MAG_X = "MPU9250_MPL_MAG_X_CAL";
		public static final String MPU_MPL_MAG_Y = "MPU9250_MPL_MAG_Y_CAL";
		public static final String MPU_MPL_MAG_Z = "MPU9250_MPL_MAG_Z_CAL";
		public static final String MPU_QUAT_6DOF_DMP_W = "MPU9250_QUAT_6DOF_W";
		public static final String MPU_QUAT_6DOF_DMP_X = "MPU9250_QUAT_6DOF_X";
		public static final String MPU_QUAT_6DOF_DMP_Y = "MPU9250_QUAT_6DOF_Y";
		public static final String MPU_QUAT_6DOF_DMP_Z = "MPU9250_QUAT_6DOF_Z";
		
	}
	public static final class DatabaseConfigHandle{
//		public static final String GYRO = "MPU9250_Gyro";
//		public static final String ALTERNATIVE_ACC = "MPU9250_Acc"; // not available but supported in FW
//		public static final String ALTERNATIVE_MAG = "MPU9250_Mag"; // not available but supported in FW
		
		public static final String MPU_QUAT_6DOF = "MPU9250_MPL_Quat_6DOF"; 
		public static final String MPU_EULER_6DOF = "MPU9250_MPL_Euler_6DOF"; 
		public static final String MPU_HEADING_ENABLE = "MPU9250_MPL_Heading"; // not available but supported in FW //channel
		
		public static final String MPU_PEDOMETER = "MPU9250_MPL_Pedometer"; 
		public static final String MPU_TAP = "MPU9250_MPL_Tap"; 
		public static final String MPU_MOTION_ORIENT = "MPU9250_MPL_Motion"; 
		public static final String MPU_GYRO = "MPU9250_MPL_Gyro_Cal";
		
		public static final String GYRO_RATE = "MPU9250_Gyro_Rate";
		public static final String GYRO_RANGE = "MPU9250_Gyro_Range";
		public static final String ALTERNATIVE_ACC_RANGE = "MPU9250_Acc_Range";
		
		public static final String MPU_ACC = "MPU9250_MPL_Acc_Cal";
		public static final String MPU_MAG = "MPU9250_MPL_Mag_Cal";
		public static final String MPU_QUAT_6DOF_DMP = "MPU9250_Quat_6DOF_Dmp";
		
		public static final String MPU_DMP = "MPU9250_DMP";
		public static final String MPU_LPF = "MPU9250_LFP";
		
		public static final String MPU_MOT_CAL_CFG = "MPU9250_MOT_Cal_Cfg";
		public static final String MPU_MPL_SAMPLING_RATE = "MPU9250_MPL_Sampling_rate";
		public static final String MPU_MAG_SAMPLING_RATE = "MPU9250_MAG_Sampling_rate";
		public static final String MPU_MPL_SENSOR_FUSION = "MPU9250_MPL_Sensor_Fusion";
		public static final String MPU_MPL_GYRO_TC = "MPU9250_MPL_Gyro_TC";
		public static final String MPU_MPL_VECT_COMP = "MPU9250_MPL_Vect_Comp";
		public static final String MPU_MAG_DIST = "MPU9250_MAG_Dist";
		public static final String MPU_MPL_ENABLE = "MPU9250_MPL_Enable";
		// MPU GYRO
		public static final String GYRO_CALIB_TIME = "MPU9250_Gyro_Calib_Time";
		public static final String GYRO_OFFSET_X = "MPU9250_Gyro_Offset_X";
		public static final String GYRO_OFFSET_Y = "MPU9250_Gyro_Offset_Y";
		public static final String GYRO_OFFSET_Z = "MPU9250_Gyro_Offset_Z";
		public static final String GYRO_GAIN_X = "MPU9250_Gyro_Gain_X";
		public static final String GYRO_GAIN_Y = "MPU9250_Gyro_Gain_Y";
		public static final String GYRO_GAIN_Z = "MPU9250_Gyro_Gain_Z";
		public static final String GYRO_ALIGN_XX = "MPU9250_Gyro_Align_XX";
		public static final String GYRO_ALIGN_XY = "MPU9250_Gyro_Align_XY";
		public static final String GYRO_ALIGN_XZ = "MPU9250_Gyro_Align_XZ";
		public static final String GYRO_ALIGN_YX = "MPU9250_Gyro_Align_YX";
		public static final String GYRO_ALIGN_YY = "MPU9250_Gyro_Align_YY";
		public static final String GYRO_ALIGN_YZ = "MPU9250_Gyro_Align_YZ";
		public static final String GYRO_ALIGN_ZX = "MPU9250_Gyro_Align_ZX";
		public static final String GYRO_ALIGN_ZY = "MPU9250_Gyro_Align_ZY";
		public static final String GYRO_ALIGN_ZZ = "MPU9250_Gyro_Align_ZZ";
		// MPU MPL ACCEL
		public static final String MPU_ACC_OFFSET_X = "MPU9250_MPL_Acc_Cal_Offset_X";
		public static final String MPU_ACC_OFFSET_Y = "MPU9250_MPL_Acc_Cal_Offset_Y";
		public static final String MPU_ACC_OFFSET_Z = "MPU9250_MPL_Acc_Cal_Offset_Z";
		public static final String MPU_ACC_GAIN_X = "MPU9250_MPL_Acc_Cal_Gain_X";
		public static final String MPU_ACC_GAIN_Y = "MPU9250_MPL_Acc_Cal_Gain_Y";
		public static final String MPU_ACC_GAIN_Z = "MPU9250_MPL_Acc_Cal_Gain_Z";
		public static final String MPU_ACC_ALIGN_XX = "MPU9250_MPL_Acc_Cal_Align_XX";
		public static final String MPU_ACC_ALIGN_XY = "MPU9250_MPL_Acc_Cal_Align_XY";
		public static final String MPU_ACC_ALIGN_XZ = "MPU9250_MPL_Acc_Cal_Align_XZ";
		public static final String MPU_ACC_ALIGN_YX = "MPU9250_MPL_Acc_Cal_Align_YX";
		public static final String MPU_ACC_ALIGN_YY = "MPU9250_MPL_Acc_Cal_Align_YY";
		public static final String MPU_ACC_ALIGN_YZ = "MPU9250_MPL_Acc_Cal_Align_YZ";
		public static final String MPU_ACC_ALIGN_ZX = "MPU9250_MPL_Acc_Cal_Align_ZX";
		public static final String MPU_ACC_ALIGN_ZY = "MPU9250_MPL_Acc_Cal_Align_ZY";
		public static final String MPU_ACC_ALIGN_ZZ = "MPU9250_MPL_Acc_Cal_Align_ZZ";
		// MPU MPL MAG
		public static final String MPU_MAG_OFFSET_X = "MPU9250_MPL_Mag_Cal_Offset_X";
		public static final String MPU_MAG_OFFSET_Y = "MPU9250_MPL_Mag_Cal_Offset_Y";
		public static final String MPU_MAG_OFFSET_Z = "MPU9250_MPL_Mag_Cal_Offset_Z";
		public static final String MPU_MAG_GAIN_X = "MPU9250_MPL_Mag_Cal_Gain_X";
		public static final String MPU_MAG_GAIN_Y = "MPU9250_MPL_Mag_Cal_Gain_Y";
		public static final String MPU_MAG_GAIN_Z = "MPU9250_MPL_Mag_Cal_Gain_Z";
		public static final String MPU_MAG_ALIGN_XX = "MPU9250_MPL_Mag_Cal_Align_XX";
		public static final String MPU_MAG_ALIGN_XY = "MPU9250_MPL_Mag_Cal_Align_XY";
		public static final String MPU_MAG_ALIGN_XZ = "MPU9250_MPL_Mag_Cal_Align_XZ";
		public static final String MPU_MAG_ALIGN_YX = "MPU9250_MPL_Mag_Cal_Align_YX";
		public static final String MPU_MAG_ALIGN_YY = "MPU9250_MPL_Mag_Cal_Align_YY";
		public static final String MPU_MAG_ALIGN_YZ = "MPU9250_MPL_Mag_Cal_Align_YZ";
		public static final String MPU_MAG_ALIGN_ZX = "MPU9250_MPL_Mag_Cal_Align_ZX";
		public static final String MPU_MAG_ALIGN_ZY = "MPU9250_MPL_Mag_Cal_Align_ZY";
		public static final String MPU_MAG_ALIGN_ZZ = "MPU9250_MPL_Mag_Cal_Align_ZZ";
		// MPU MPL GYRO
		public static final String MPU_GYRO_OFFSET_X = "MPU9250_MPL_Gyro_Cal_Offset_X";
		public static final String MPU_GYRO_OFFSET_Y = "MPU9250_MPL_Gyro_Cal_Offset_Y";
		public static final String MPU_GYRO_OFFSET_Z = "MPU9250_MPL_Gyro_Cal_Offset_Z";
		public static final String MPU_GYRO_GAIN_X = "MPU9250_MPL_Gyro_Cal_Gain_X";
		public static final String MPU_GYRO_GAIN_Y = "MPU9250_MPL_Gyro_Cal_Gain_Y";
		public static final String MPU_GYRO_GAIN_Z = "MPU9250_MPL_Gyro_Cal_Gain_Z";
		public static final String MPU_GYRO_ALIGN_XX = "MPU9250_MPL_Gyro_Cal_Align_XX";
		public static final String MPU_GYRO_ALIGN_XY = "MPU9250_MPL_Gyro_Cal_Align_XY";
		public static final String MPU_GYRO_ALIGN_XZ = "MPU9250_MPL_Gyro_Cal_Align_XZ";
		public static final String MPU_GYRO_ALIGN_YX = "MPU9250_MPL_Gyro_Cal_Align_YX";
		public static final String MPU_GYRO_ALIGN_YY = "MPU9250_MPL_Gyro_Cal_Align_YY";
		public static final String MPU_GYRO_ALIGN_YZ = "MPU9250_MPL_Gyro_Cal_Align_YZ";
		public static final String MPU_GYRO_ALIGN_ZX = "MPU9250_MPL_Gyro_Cal_Align_ZX";
		public static final String MPU_GYRO_ALIGN_ZY = "MPU9250_MPL_Gyro_Cal_Align_ZY";
		public static final String MPU_GYRO_ALIGN_ZZ = "MPU9250_MPL_Gyro_Cal_Align_ZZ";
		
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
	// MPU9250 Gyro
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

	// MPU9250 Temperature
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

	// Raw 6DOF Quaterian's from the DMP hardware module of the MPU9250
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
		// MPU9250 Gyro
		aMap.put(ObjectClusterSensorName.GYRO_X, SensorMPU9250.channelGyroX);
		aMap.put(ObjectClusterSensorName.GYRO_Y, SensorMPU9250.channelGyroY);
		aMap.put(ObjectClusterSensorName.GYRO_Z, SensorMPU9250.channelGyroZ);
		// MPU9250 Accel
		aMap.put(ObjectClusterSensorName.ACCEL_MPU_X, SensorMPU9250.channelAccelX);
		aMap.put(ObjectClusterSensorName.ACCEL_MPU_Y, SensorMPU9250.channelAccelY);
		aMap.put(ObjectClusterSensorName.ACCEL_MPU_Z, SensorMPU9250.channelAccelZ);
		// MPU9250 Mag
		aMap.put(ObjectClusterSensorName.MAG_MPU_X, SensorMPU9250.channelMagX);
		aMap.put(ObjectClusterSensorName.MAG_MPU_Y, SensorMPU9250.channelMagY);
		aMap.put(ObjectClusterSensorName.MAG_MPU_Z, SensorMPU9250.channelMagZ);
		
		// MPL Gyro Calibrated
		aMap.put(ObjectClusterSensorName.GYRO_MPU_MPL_X, SensorMPU9250.channelGyroMpuMplX);
		aMap.put(ObjectClusterSensorName.GYRO_MPU_MPL_Y, SensorMPU9250.channelGyroMpuMplY);
		aMap.put(ObjectClusterSensorName.GYRO_MPU_MPL_Z, SensorMPU9250.channelGyroMpuMplZ);

		// MPL Accelerometer Calibrated
		aMap.put(ObjectClusterSensorName.ACCEL_MPU_MPL_X, SensorMPU9250.channelAccelMpuMplX);
		aMap.put(ObjectClusterSensorName.ACCEL_MPU_MPL_Y, SensorMPU9250.channelAccelMpuMplY);
		aMap.put(ObjectClusterSensorName.ACCEL_MPU_MPL_Z, SensorMPU9250.channelAccelMpuMplZ);
				
		// MPL Magnetometer Calibrated
		aMap.put(ObjectClusterSensorName.MAG_MPU_MPL_X, SensorMPU9250.channelMagMpuMplX);
		aMap.put(ObjectClusterSensorName.MAG_MPU_MPL_Y, SensorMPU9250.channelMagMpuMplY);
		aMap.put(ObjectClusterSensorName.MAG_MPU_MPL_Z, SensorMPU9250.channelMagMpuMplZ);
		

		// MPL Quaternions 6DOF
		aMap.put(ObjectClusterSensorName.QUAT_MPL_6DOF_W, SensorMPU9250.channelQuatMpl6DofW);
		aMap.put(ObjectClusterSensorName.QUAT_MPL_6DOF_X, SensorMPU9250.channelQuatMpl6DofX);
		aMap.put(ObjectClusterSensorName.QUAT_MPL_6DOF_Y, SensorMPU9250.channelQuatMpl6DofY);
		aMap.put(ObjectClusterSensorName.QUAT_MPL_6DOF_Z, SensorMPU9250.channelQuatMpl6DofZ);

		// MPL Quaternions 9DOF
		aMap.put(ObjectClusterSensorName.QUAT_MPL_9DOF_W, SensorMPU9250.channelQuatMpl9DofW);
		aMap.put(ObjectClusterSensorName.QUAT_MPL_9DOF_X, SensorMPU9250.channelQuatMpl9DofX);
		aMap.put(ObjectClusterSensorName.QUAT_MPL_9DOF_Y, SensorMPU9250.channelQuatMpl9DofY);
		aMap.put(ObjectClusterSensorName.QUAT_MPL_9DOF_Z, SensorMPU9250.channelQuatMpl9DofZ);
		
		// MPL Euler
		aMap.put(ObjectClusterSensorName.EULER_MPL_6DOF_X, SensorMPU9250.channelEulerMpl6DofX);
		aMap.put(ObjectClusterSensorName.EULER_MPL_6DOF_Y, SensorMPU9250.channelEulerMpl6DofY);
		aMap.put(ObjectClusterSensorName.EULER_MPL_6DOF_Z, SensorMPU9250.channelEulerMpl6DofZ);
		
		aMap.put(ObjectClusterSensorName.EULER_MPL_9DOF_X, SensorMPU9250.channelEulerMpl9DofX);
		aMap.put(ObjectClusterSensorName.EULER_MPL_9DOF_Y, SensorMPU9250.channelEulerMpl9DofY);
		aMap.put(ObjectClusterSensorName.EULER_MPL_9DOF_Z, SensorMPU9250.channelEulerMpl9DofZ);

		// MPL Heading
		aMap.put(ObjectClusterSensorName.MPL_HEADING, SensorMPU9250.channelMplHeading);

		// MPU9250 Temperature
		aMap.put(ObjectClusterSensorName.MPL_TEMPERATURE, SensorMPU9250.channelMplTemperature);

		// MPL Pedometer
		aMap.put(ObjectClusterSensorName.MPL_PEDOM_CNT, SensorMPU9250.channelMplPedomCount);
		aMap.put(ObjectClusterSensorName.MPL_PEDOM_TIME, SensorMPU9250.channelMplPedomTime);

//		// MPL Tap
//		aMap.put(ObjectClusterSensorName.TAPDIRANDTAPCNT, SensorMPU9250.channelMplTapDirAndTapCnt);
		// MPL Tap Direction
		aMap.put(ObjectClusterSensorName.TAPDIR, SensorMPU9250.channelMplTapDir);
		// MPL Tap Count
		aMap.put(ObjectClusterSensorName.TAPCNT, SensorMPU9250.channelMplTapCnt);
		
		// MPL Motion Orient
		aMap.put(ObjectClusterSensorName.MOTIONANDORIENT, SensorMPU9250.channelMplMotionAndOrient);
		aMap.put(ObjectClusterSensorName.MOTION, SensorMPU9250.channelMplMotion);
		aMap.put(ObjectClusterSensorName.ORIENT, SensorMPU9250.channelMplOrient);
		// Raw 6DOF Quaterian's from the DMP hardware module of the MPU9250
		aMap.put(ObjectClusterSensorName.QUAT_DMP_6DOF_W, SensorMPU9250.channelQuatDmp6DofW);
		aMap.put(ObjectClusterSensorName.QUAT_DMP_6DOF_X, SensorMPU9250.channelQuatDmp6DofX);
		aMap.put(ObjectClusterSensorName.QUAT_DMP_6DOF_Y, SensorMPU9250.channelQuatDmp6DofY);
		aMap.put(ObjectClusterSensorName.QUAT_DMP_6DOF_Z, SensorMPU9250.channelQuatDmp6DofZ);
		mChannelMapRef = Collections.unmodifiableMap(aMap);
    }

	//--------- Channel info end --------------
	//--------- Sensor specific variables end --------------


    //--------- Constructors for this class start --------------

	/**Just used for accessing calibration*/
	public SensorMPU9250(){
		super(SENSORS.MPU9X50);
		initialise();
		
		setCalibSensitivityScaleFactor(Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_GYRO, CALIBRATION_SCALE_FACTOR.ONE_HUNDRED);
	}


	/** Constructor for this class
	 * @param svo
	 */
	public SensorMPU9250(ShimmerVerObject svo){
		super(SENSORS.MPU9X50, svo);
		initialise();
		
		setCalibSensitivityScaleFactor(Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_GYRO, CALIBRATION_SCALE_FACTOR.ONE_HUNDRED);
	}

	/** Constructor for this class
	 * @param svo
	 */
	public SensorMPU9250(ShimmerDevice shimmerDevice){
		super(SENSORS.MPU9X50, shimmerDevice);
		initialise();
		
		setCalibSensitivityScaleFactor(Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_GYRO, CALIBRATION_SCALE_FACTOR.ONE_HUNDRED);
	}

    //--------- Constructors for this class end --------------

	@Override
	public void generateSensorMap() {
		super.createLocalSensorMapWithCustomParser(mSensorMapRef, mChannelMapRef);
	}


	@Override
	public LinkedHashMap<String, Object> getConfigMapForDb() {
		LinkedHashMap<String, Object> mapOfConfig = new LinkedHashMap<String, Object>();
		mapOfConfig.put(DatabaseConfigHandle.GYRO_RANGE, getGyroRange());
		mapOfConfig.put(DatabaseConfigHandle.GYRO_RATE, getMPU9150GyroAccelRate());
		
		mapOfConfig.put(DatabaseConfigHandle.MPU_MPL_SAMPLING_RATE, getMPU9150MPLSamplingRate());
		mapOfConfig.put(DatabaseConfigHandle.MPU_MAG_SAMPLING_RATE, getMPU9150MagSamplingRate());
		mapOfConfig.put(DatabaseConfigHandle.MPU_MOT_CAL_CFG, getMPU9150MotCalCfg());
		mapOfConfig.put(DatabaseConfigHandle.MPU_DMP, getMPU9150DMP());
		mapOfConfig.put(DatabaseConfigHandle.MPU_LPF, getMPU9150LPF());

		super.addCalibDetailsToDbMap(mapOfConfig, 
				getCurrentCalibDetailsGyro(), 
				DatabaseConfigHandle.LIST_OF_CALIB_HANDLES_GYRO,
				DatabaseConfigHandle.GYRO_CALIB_TIME);
		
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
	public void parseConfigMapFromDb(LinkedHashMap<String, Object> mapOfConfigPerShimmer) {
		if(mapOfConfigPerShimmer.containsKey(DatabaseConfigHandle.GYRO_RATE)){
			setMPU9150GyroAccelRate(((Double) mapOfConfigPerShimmer.get(DatabaseConfigHandle.GYRO_RATE)).intValue());
		}
		if(mapOfConfigPerShimmer.containsKey(DatabaseConfigHandle.GYRO_RANGE)){
			setGyroRange(((Double) mapOfConfigPerShimmer.get(DatabaseConfigHandle.GYRO_RANGE)).intValue());
		}
		if(mapOfConfigPerShimmer.containsKey(DatabaseConfigHandle.ALTERNATIVE_ACC_RANGE)){
			setMPU9150AccelRange(((Double) mapOfConfigPerShimmer.get(DatabaseConfigHandle.ALTERNATIVE_ACC_RANGE)).intValue());
		}
		if(mapOfConfigPerShimmer.containsKey(DatabaseConfigHandle.MPU_DMP)){
			setMPU9150DMP(((Double) mapOfConfigPerShimmer.get(DatabaseConfigHandle.MPU_DMP)).intValue());
		}
		if(mapOfConfigPerShimmer.containsKey(DatabaseConfigHandle.MPU_LPF)){
			setMPU9150LPF(((Double) mapOfConfigPerShimmer.get(DatabaseConfigHandle.MPU_LPF)).intValue());
		}
		if(mapOfConfigPerShimmer.containsKey(DatabaseConfigHandle.MPU_MOT_CAL_CFG)){
			setMPU9150MotCalCfg(((Double) mapOfConfigPerShimmer.get(DatabaseConfigHandle.MPU_MOT_CAL_CFG)).intValue());
		}
		if(mapOfConfigPerShimmer.containsKey(DatabaseConfigHandle.MPU_MPL_SAMPLING_RATE)){
			setMPU9150MPLSamplingRate(((Double) mapOfConfigPerShimmer.get(DatabaseConfigHandle.MPU_MPL_SAMPLING_RATE)).intValue());
		}
		if(mapOfConfigPerShimmer.containsKey(DatabaseConfigHandle.MPU_MAG_SAMPLING_RATE)){
			setMPU9150MagSamplingRate(((Double) mapOfConfigPerShimmer.get(DatabaseConfigHandle.MPU_MAG_SAMPLING_RATE)).intValue());
		}
		if(mapOfConfigPerShimmer.containsKey(DatabaseConfigHandle.MPU_MPL_SENSOR_FUSION)){
			setmMPLSensorFusion(((Double) mapOfConfigPerShimmer.get(DatabaseConfigHandle.MPU_MPL_SENSOR_FUSION))>0? true:false);
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
			setMPLEnable(((Double) mapOfConfigPerShimmer.get(DatabaseConfigHandle.MPU_MPL_ENABLE))>0? true:false);
		}
		
		//Gyroscope Calibration Configuration
		parseCalibDetailsKinematicFromDb(mapOfConfigPerShimmer, 
				Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_GYRO, 
				getGyroRange(), 
				SensorMPU9150.DatabaseConfigHandle.LIST_OF_CALIB_HANDLES_GYRO,
				SensorMPU9150.DatabaseConfigHandle.GYRO_CALIB_TIME);
		
//		//TODO
//		//MPL Accel Calibration Configuration
//		parseCalibDetailsKinematicFromDb(mapOfConfigPerShimmer, Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_MPL_ACCEL, getMPU9150AccelRange(), SensorMPU9X50.DatabaseConfigHandle.LIST_OF_CALIB_HANDLES_MPU_MPL_ACC);
//
//		//TODO
//		//MPL Mag Calibration Configuration
//		parseCalibDetailsKinematicFromDb(mapOfConfigPerShimmer, Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_MPL_MAG, 0, SensorMPU9X50.DatabaseConfigHandle.LIST_OF_CALIB_HANDLES_MPU_MPL_MAG);
//		
//		//TODO
//		//MPL Gyro Calibration Configuration
//		parseCalibDetailsKinematicFromDb(mapOfConfigPerShimmer, Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_MPL_GYRO, getGyroRange(), SensorMPU9X50.DatabaseConfigHandle.LIST_OF_CALIB_HANDLES_MPU_MPL_GYRO);
	}


	
	public static String parseFromDBColumnToGUIChannel(String databaseChannelHandle) {
		return AbstractSensor.parseFromDBColumnToGUIChannel(mChannelMapRef, databaseChannelHandle);
	}

	public static String parseFromGUIChannelsToDBColumn(String objectClusterName) {
		return AbstractSensor.parseFromGUIChannelsToDBColumn(mChannelMapRef, objectClusterName);
	}
}
