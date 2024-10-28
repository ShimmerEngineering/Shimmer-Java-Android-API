package com.shimmerresearch.sensors.lsm6dsv;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.ArrayUtils;

import com.shimmerresearch.bluetooth.BtCommandDetails;
import com.shimmerresearch.driver.Configuration.CHANNEL_UNITS;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driver.Configuration.Shimmer3.CompatibilityInfoForMaps;
import com.shimmerresearch.driver.calibration.CalibDetails;
import com.shimmerresearch.driver.calibration.CalibDetailsKinematic;
import com.shimmerresearch.driver.calibration.CalibDetails.CALIB_READ_SOURCE;
import com.shimmerresearch.driver.calibration.CalibDetailsKinematic.CALIBRATION_SCALE_FACTOR;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.ShimmerDevice;
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
import com.shimmerresearch.sensors.AbstractSensor.SENSORS;
import com.shimmerresearch.sensors.kionix.SensorKionixAccel;
import com.shimmerresearch.sensors.kionix.SensorKionixKXTC92050;
import com.shimmerresearch.sensors.kionix.SensorKionixAccel.GuiLabelSensors;
import com.shimmerresearch.sensors.kionix.SensorKionixAccel.ObjectClusterSensorName;
import com.shimmerresearch.sensors.kionix.SensorKionixKXTC92050.DatabaseChannelHandles;
import com.shimmerresearch.sensors.kionix.SensorKionixKXTC92050.DatabaseConfigHandle;
import com.shimmerresearch.sensors.lsm303.SensorLSM303;
import com.shimmerresearch.sensors.lsm303.SensorLSM303AH;
import com.shimmerresearch.sensors.mpu9x50.SensorMPU9250;
import com.shimmerresearch.sensors.mpu9x50.SensorMPU9X50;
import com.shimmerresearch.sensors.mpu9x50.SensorMPU9X50.GuiLabelConfig;
import com.shimmerresearch.sensors.mpu9x50.SensorMPU9X50.LABEL_SENSOR_TILE;
import com.shimmerresearch.sensors.ActionSetting;

public class SensorLSM6DSV extends AbstractSensor{
	
	
	//--------- Sensor specific variables start --------------

	// LN ACCEL
	public static final double[][] AlignmentMatrixLowNoiseAccelShimmer3r = {{-1,0,0},{0,1,0},{0,0,-1}};
	public static final double[][] OffsetVectorLowNoiseAccelShimmer3r = {{0},{0},{0}}; 
	
	public static final double[][] SensitivityMatrixLowNoiseAccel2gShimmer3r = {{1672,0,0},{0,1672,0},{0,0,1672}};  
	public static final double[][] SensitivityMatrixLowNoiseAccel4gShimmer3r = {{836,0,0},{0,836,0},{0,0,836}};  
	public static final double[][] SensitivityMatrixLowNoiseAccel8gShimmer3r = {{418,0,0},{0,418,0},{0,0,418}};  
	public static final double[][] SensitivityMatrixLowNoiseAccel16gShimmer3r = {{209,0,0},{0,209,0},{0,0,209}};
	
	private static final int LN_ACCEL_RANGE_VALUE_2G = 0;
	private static final String LN_ACCEL_RANGE_STRING_2G = UtilShimmer.UNICODE_PLUS_MINUS + " 2g" ; 
	private static final int LN_ACCEL_RANGE_VALUE_4G = 1;
	private static final String LN_ACCEL_RANGE_STRING_4G = UtilShimmer.UNICODE_PLUS_MINUS + " 4g" ; 
	private static final int LN_ACCEL_RANGE_VALUE_8G = 2;
	private static final String LN_ACCEL_RANGE_STRING_8G = UtilShimmer.UNICODE_PLUS_MINUS + " 8g" ; 
	private static final int LN_ACCEL_RANGE_VALUE_16G = 3;
	private static final String LN_ACCEL_RANGE_STRING_16G = UtilShimmer.UNICODE_PLUS_MINUS + " 16g" ; 
	
	private CalibDetailsKinematic calibDetailsAccelLn2g = new CalibDetailsKinematic(
			LN_ACCEL_RANGE_VALUE_2G, LN_ACCEL_RANGE_STRING_2G, 
			AlignmentMatrixLowNoiseAccelShimmer3r, SensitivityMatrixLowNoiseAccel2gShimmer3r, OffsetVectorLowNoiseAccelShimmer3r);
	public CalibDetailsKinematic mCurrentCalibDetailsAccelLn2g = calibDetailsAccelLn2g;
	
	private CalibDetailsKinematic calibDetailsAccelLn4g = new CalibDetailsKinematic(
			LN_ACCEL_RANGE_VALUE_4G, LN_ACCEL_RANGE_STRING_4G, 
			AlignmentMatrixLowNoiseAccelShimmer3r, SensitivityMatrixLowNoiseAccel4gShimmer3r, OffsetVectorLowNoiseAccelShimmer3r);
	public CalibDetailsKinematic mCurrentCalibDetailsAccelLn4g = calibDetailsAccelLn4g;
	
	private CalibDetailsKinematic calibDetailsAccelLn8g = new CalibDetailsKinematic(
			LN_ACCEL_RANGE_VALUE_8G, LN_ACCEL_RANGE_STRING_8G, 
			AlignmentMatrixLowNoiseAccelShimmer3r, SensitivityMatrixLowNoiseAccel8gShimmer3r, OffsetVectorLowNoiseAccelShimmer3r);
	public CalibDetailsKinematic mCurrentCalibDetailsAccelLn8g = calibDetailsAccelLn8g;
	
	private CalibDetailsKinematic calibDetailsAccelLn16g = new CalibDetailsKinematic(
			LN_ACCEL_RANGE_VALUE_16G, LN_ACCEL_RANGE_STRING_16G, 
			AlignmentMatrixLowNoiseAccelShimmer3r, SensitivityMatrixLowNoiseAccel16gShimmer3r, OffsetVectorLowNoiseAccelShimmer3r);
	public CalibDetailsKinematic mCurrentCalibDetailsAccelLn16g = calibDetailsAccelLn16g;
	
	// GYRO
	private int mGyroRange = 1;	
	protected int mLSM6DSVGyroAccelRate=0;
	protected int mSensorIdGyro = -1;
	
	public static final double[][] AlignmentMatrixGyroShimmer3r = {{-1,0,0},{0,1,0},{0,0,-1}}; 				
	public static final double[][] OffsetVectorGyroShimmer3r = {{0},{0},{0}};	
	public static final double[][] SensitivityMatrixGyro125dpsShimmer3r = {{229,0,0},{0,229,0},{0,0,229}};
	public static final double[][] SensitivityMatrixGyro250dpsShimmer3r = {{114,0,0},{0,114,0},{0,0,114}};
	public static final double[][] SensitivityMatrixGyro500dpsShimmer3r = {{57,0,0},{0,57,0},{0,0,57}};
	public static final double[][] SensitivityMatrixGyro1000dpsShimmer3r = {{29,0,0},{0,29,0},{0,0,29}};
	public static final double[][] SensitivityMatrixGyro2000dpsShimmer3r = {{14,0,0},{0,14,0},{0,0,14}};
	public static final double[][] SensitivityMatrixGyro4000dpsShimmer3r = {{7,0,0},{0,7,0},{0,0,7}};
	
	private CalibDetailsKinematic calibDetailsGyro125 = new CalibDetailsKinematic(
			ListofLSM6DSVGyroRangeConfigValues[0], 
			ListofGyroRange[0],
			AlignmentMatrixGyroShimmer3r,
			SensitivityMatrixGyro125dpsShimmer3r,
			OffsetVectorGyroShimmer3r,
			CALIBRATION_SCALE_FACTOR.ONE_HUNDRED);
	
	private CalibDetailsKinematic calibDetailsGyro250 = new CalibDetailsKinematic(
			ListofLSM6DSVGyroRangeConfigValues[1], 
			ListofGyroRange[1],
			AlignmentMatrixGyroShimmer3r, 
			SensitivityMatrixGyro250dpsShimmer3r,
			OffsetVectorGyroShimmer3r,
			CALIBRATION_SCALE_FACTOR.ONE_HUNDRED);
	
	private CalibDetailsKinematic calibDetailsGyro500 = new CalibDetailsKinematic(
			ListofLSM6DSVGyroRangeConfigValues[2], 
			ListofGyroRange[2],
			AlignmentMatrixGyroShimmer3r, 
			SensitivityMatrixGyro500dpsShimmer3r, 
			OffsetVectorGyroShimmer3r,
			CALIBRATION_SCALE_FACTOR.ONE_HUNDRED);
	
	private CalibDetailsKinematic calibDetailsGyro1000 = new CalibDetailsKinematic(
			ListofLSM6DSVGyroRangeConfigValues[3],
			ListofGyroRange[3],
			AlignmentMatrixGyroShimmer3r, 
			SensitivityMatrixGyro1000dpsShimmer3r, 
			OffsetVectorGyroShimmer3r,
			CALIBRATION_SCALE_FACTOR.ONE_HUNDRED);
	
	private CalibDetailsKinematic calibDetailsGyro2000 = new CalibDetailsKinematic(
			ListofLSM6DSVGyroRangeConfigValues[4],
			ListofGyroRange[4],
			AlignmentMatrixGyroShimmer3r, 
			SensitivityMatrixGyro2000dpsShimmer3r, 
			OffsetVectorGyroShimmer3r,
			CALIBRATION_SCALE_FACTOR.ONE_HUNDRED);
	
	private CalibDetailsKinematic calibDetailsGyro4000 = new CalibDetailsKinematic(
			ListofLSM6DSVGyroRangeConfigValues[5],
			ListofGyroRange[5],
			AlignmentMatrixGyroShimmer3r, 
			SensitivityMatrixGyro4000dpsShimmer3r, 
			OffsetVectorGyroShimmer3r,
			CALIBRATION_SCALE_FACTOR.ONE_HUNDRED);
	
	public CalibDetailsKinematic mCurrentCalibDetailsGyro = calibDetailsGyro250;

	
	public static class DatabaseChannelHandles{
		public static final String LN_ACC_X = "LSM6DSV_X";
		public static final String LN_ACC_Y = "LSM6DSV_X";
		public static final String LN_ACC_Z = "LSM6DSV_X";
		
		public static final String GYRO_X = "LSM6DSV_GYRO_X";
		public static final String GYRO_Y = "LSM6DSV_GYRO_Y";
		public static final String GYRO_Z = "LSM6DSV_GYRO_Z";
	}
	
	public static final class DatabaseConfigHandle{
		public static final String LN_ACC_CALIB_TIME = "LSM6DSV_Acc_Calib_Time";
		public static final String LN_ACC_OFFSET_X = "LSM6DSV_Acc_Offset_X";
		public static final String LN_ACC_OFFSET_Y = "LSM6DSV_Acc_Offset_Y";
		public static final String LN_ACC_OFFSET_Z = "LSM6DSV_Acc_Offset_Z";
		public static final String LN_ACC_GAIN_X = "LSM6DSV_Acc_Gain_X";
		public static final String LN_ACC_GAIN_Y = "LSM6DSV_Acc_Gain_Y";
		public static final String LN_ACC_GAIN_Z = "LSM6DSV_Acc_Gain_Z";
		public static final String LN_ACC_ALIGN_XX = "LSM6DSV_Acc_Align_XX";
		public static final String LN_ACC_ALIGN_XY = "LSM6DSV_Acc_Align_XY";
		public static final String LN_ACC_ALIGN_XZ = "LSM6DSV_Acc_Align_XZ";
		public static final String LN_ACC_ALIGN_YX = "LSM6DSV_Acc_Align_YX";
		public static final String LN_ACC_ALIGN_YY = "LSM6DSV_Acc_Align_YY";
		public static final String LN_ACC_ALIGN_YZ = "LSM6DSV_Acc_Align_YZ";
		public static final String LN_ACC_ALIGN_ZX = "LSM6DSV_Acc_Align_ZX";
		public static final String LN_ACC_ALIGN_ZY = "LSM6DSV_Acc_Align_ZY";
		public static final String LN_ACC_ALIGN_ZZ = "LSM6DSV_Acc_Align_ZZ";
		
		public static final List<String> LIST_OF_CALIB_HANDLES_LN_ACC = Arrays.asList(
				DatabaseConfigHandle.LN_ACC_OFFSET_X, DatabaseConfigHandle.LN_ACC_OFFSET_Y, DatabaseConfigHandle.LN_ACC_OFFSET_Z,
				DatabaseConfigHandle.LN_ACC_GAIN_X, DatabaseConfigHandle.LN_ACC_GAIN_Y, DatabaseConfigHandle.LN_ACC_GAIN_Z,
				DatabaseConfigHandle.LN_ACC_ALIGN_XX, DatabaseConfigHandle.LN_ACC_ALIGN_XY, DatabaseConfigHandle.LN_ACC_ALIGN_XZ,
				DatabaseConfigHandle.LN_ACC_ALIGN_YX, DatabaseConfigHandle.LN_ACC_ALIGN_YY, DatabaseConfigHandle.LN_ACC_ALIGN_YZ,
				DatabaseConfigHandle.LN_ACC_ALIGN_ZX, DatabaseConfigHandle.LN_ACC_ALIGN_ZY, DatabaseConfigHandle.LN_ACC_ALIGN_ZZ);
		
		
		public static final String GYRO_RATE = "LSM6DSV_Gyro_Rate";
		public static final String GYRO_RANGE = "LSM6DSV_Gyro_Range";
		public static final String GYRO_CALIB_TIME = "LSM6DSV_Gyro_Calib_Time";
	
		public static final String GYRO_OFFSET_X = "LSM6DSV_Gyro_Offset_X";
		public static final String GYRO_OFFSET_Y = "LSM6DSV_Gyro_Offset_Y";
		public static final String GYRO_OFFSET_Z = "LSM6DSV_Gyro_Offset_Z";
		public static final String GYRO_GAIN_X = "LSM6DSV_Gyro_Gain_X";
		public static final String GYRO_GAIN_Y = "LSM6DSV_Gyro_Gain_Y";
		public static final String GYRO_GAIN_Z = "LSM6DSV_Gyro_Gain_Z";
		public static final String GYRO_ALIGN_XX = "LSM6DSV_Gyro_Align_XX";
		public static final String GYRO_ALIGN_XY = "LSM6DSV_Gyro_Align_XY";
		public static final String GYRO_ALIGN_XZ = "LSM6DSV_Gyro_Align_XZ";
		public static final String GYRO_ALIGN_YX = "LSM6DSV_Gyro_Align_YX";
		public static final String GYRO_ALIGN_YY = "LSM6DSV_Gyro_Align_YY";
		public static final String GYRO_ALIGN_YZ = "LSM6DSV_Gyro_Align_YZ";
		public static final String GYRO_ALIGN_ZX = "LSM6DSV_Gyro_Align_ZX";
		public static final String GYRO_ALIGN_ZY = "LSM6DSV_Gyro_Align_ZY";
		public static final String GYRO_ALIGN_ZZ = "LSM6DSV_Gyro_Align_ZZ";
		
		public static final List<String> LIST_OF_CALIB_HANDLES_GYRO = Arrays.asList(
				DatabaseConfigHandle.GYRO_OFFSET_X, DatabaseConfigHandle.GYRO_OFFSET_Y, DatabaseConfigHandle.GYRO_OFFSET_Z,
				DatabaseConfigHandle.GYRO_GAIN_X, DatabaseConfigHandle.GYRO_GAIN_Y, DatabaseConfigHandle.GYRO_GAIN_Z,
				DatabaseConfigHandle.GYRO_ALIGN_XX, DatabaseConfigHandle.GYRO_ALIGN_XY, DatabaseConfigHandle.GYRO_ALIGN_XZ,
				DatabaseConfigHandle.GYRO_ALIGN_YX, DatabaseConfigHandle.GYRO_ALIGN_YY, DatabaseConfigHandle.GYRO_ALIGN_YZ,
				DatabaseConfigHandle.GYRO_ALIGN_ZX, DatabaseConfigHandle.GYRO_ALIGN_ZY, DatabaseConfigHandle.GYRO_ALIGN_ZZ);
	}
	
	public class GuiLabelConfig{
		public static final String LSM6DSV_ACCEL_DEFAULT_CALIB = "Low Noise Accel Default Calibration";
		public static final String LSM6DSV_ACCEL_VALID_CALIB = "Low Noise Accel Valid Calibration";
		public static final String LSM6DSV_ACCEL_CALIB_PARAM = "Low Noise Accel Calibration Details";
		
		public static final String LSM6DSV_GYRO_RANGE = "Gyro Range";
		public static final String LSM6DSV_GYRO_RATE = "Gyro Sampling Rate";
		public static final String LSM6DSV_GYRO_RATE_HZ = "Gyro Sampling Rate Hertz";
		public static final String LSM6DSV_GYRO_LPM = "Gyro Low-Power Mode";
	}
	
	public static class ObjectClusterSensorName{
		public static  String ACCEL_LN_X = "Accel_LN_X";
		public static  String ACCEL_LN_Y = "Accel_LN_Y";
		public static  String ACCEL_LN_Z = "Accel_LN_Z";
		
		public static String GYRO_X = "Gyro_X";
		public static String GYRO_Y = "Gyro_Y";
		public static String GYRO_Z = "Gyro_Z";
	}	
	
	public CalibDetailsKinematic mCurrentCalibDetailsAccelLn = null;
	
	public class GuiLabelSensors{
		public static final String ACCEL_LN = "Low-Noise Accelerometer";
		public static final String GYRO = "Gyroscope";
	}
	
	public class LABEL_SENSOR_TILE{
		public static final String LOW_NOISE_ACCEL = GuiLabelSensors.ACCEL_LN;
		public static final String GYRO = GuiLabelSensors.GYRO;

	}
	//--------- Sensor specific variables end --------------
	
	
	
	//--------- Configuration options start --------------
	// ACCEL_LN
	public static final Integer[] ListofLSM6DSVAccelRangeConfigValues={0,1,2,3};  
	public static final String[] ListofLSM6DSVGyroRate={"Power-down","1.875Hz","7.5Hz","12.0Hz","30.0Hz","60.0Hz","120.0Hz","240.0Hz","480.0Hz","960.0Hz","1920.0Hz","3840.0Hz","7680.0Hz"};
	public static final Integer[] ListofLSM6DSVGyroRateConfigValues={0,1,2,3,4,5,6,7,8,9,10,11,12,13};
	
	// GYRO
	public static final String[] ListofGyroRange = {"+/- 125dps","+/- 250dps","+/- 500dps","+/- 1000dps","+/- 2000dps","+/- 4000dps"};
	public static final Integer[] ListofLSM6DSVGyroRangeConfigValues = {0,1,2,3,4,5};

	public static final ConfigOptionDetailsSensor configOptionLSM6DSVGyroRange = new ConfigOptionDetailsSensor(
			SensorLSM6DSV.GuiLabelConfig.LSM6DSV_GYRO_RANGE,
			SensorLSM6DSV.DatabaseConfigHandle.GYRO_RANGE,
			ListofGyroRange, 
			ListofLSM6DSVGyroRangeConfigValues, 
			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW);

	//General Config
	public static final ConfigOptionDetailsSensor configOptionLSM6DSVGyroRate = new ConfigOptionDetailsSensor(
			SensorLSM6DSV.GuiLabelConfig.LSM6DSV_GYRO_RATE,
			SensorLSM6DSV.DatabaseConfigHandle.GYRO_RATE,
			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.TEXTFIELD,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW);

	public static final ConfigOptionDetailsSensor configOptionLSM6DSVGyroLpm = new ConfigOptionDetailsSensor(
			SensorLSM6DSV.GuiLabelConfig.LSM6DSV_GYRO_LPM,
			null,
			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.CHECKBOX,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW);
	//--------- Configuration options end --------------
	
	
	
	//--------- Sensor info start --------------
	public static final SensorDetailsRef sensorLSM6DSV = new SensorDetailsRef(
			0x80, //== Configuration.Shimmer3.SensorBitmap.SENSOR_A_ACCEL will be: SensorBitmap.SENSOR_A_ACCEL, 	// To Be Changed
			0x80, //== Configuration.Shimmer3.SensorBitmap.SENSOR_A_ACCEL will be: SensorBitmap.SENSOR_A_ACCEL, 
			GuiLabelSensors.ACCEL_LN,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoKionixKXTC92050,
			null,
			Arrays.asList(SensorLSM6DSV.ObjectClusterSensorName.ACCEL_LN_X,
					SensorLSM6DSV.ObjectClusterSensorName.ACCEL_LN_Y,
					SensorLSM6DSV.ObjectClusterSensorName.ACCEL_LN_Z));
	
	public static final SensorDetailsRef sensorLSM6DSVGyroRef = new SensorDetailsRef(0x40<<(0*8), 0x40<<(0*8), GuiLabelSensors.GYRO,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPU9250,
			Arrays.asList(Configuration.Shimmer3.SENSOR_ID.SHIMMER_LSM6DSV_GYRO),
			Arrays.asList(
					GuiLabelConfig.LSM6DSV_GYRO_RANGE, 
					GuiLabelConfig.LSM6DSV_GYRO_RATE),
			Arrays.asList(
					ObjectClusterSensorName.GYRO_X, 
					ObjectClusterSensorName.GYRO_Y, 
					ObjectClusterSensorName.GYRO_Z),
			false);
	
	public static final Map<Integer, SensorDetailsRef> mSensorMapRef;
    static {
        Map<Integer, SensorDetailsRef> aMap = new LinkedHashMap<Integer, SensorDetailsRef>();
        aMap.put(Configuration.Shimmer3.SENSOR_ID.SHIMMER_LSM6DSV_ACCEL_LN, SensorLSM6DSV.sensorLSM6DSV);
        aMap.put(Configuration.Shimmer3.SENSOR_ID.SHIMMER_LSM6DSV_GYRO, SensorLSM6DSV.sensorLSM6DSVGyroRef);

		mSensorMapRef = Collections.unmodifiableMap(aMap);
    }
	//--------- Sensor info end --------------
    
    
    
	//--------- Channel info start --------------  // To Be Changed
    // LN ACCEL
    public static final ChannelDetails channelAccelX = new ChannelDetails(
			ObjectClusterSensorName.ACCEL_LN_X,
			ObjectClusterSensorName.ACCEL_LN_X,
			DatabaseChannelHandles.LN_ACC_X,
//			CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.LSB,
			CHANNEL_DATA_TYPE.UINT12, 2, CHANNEL_DATA_ENDIAN.LSB,
			CHANNEL_UNITS.METER_PER_SECOND_SQUARE,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL),
			0x00);
    
    public static final ChannelDetails channelAccelY = new ChannelDetails(
			ObjectClusterSensorName.ACCEL_LN_Y,
			ObjectClusterSensorName.ACCEL_LN_Y,
			DatabaseChannelHandles.LN_ACC_Y,
//			CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.LSB,
			CHANNEL_DATA_TYPE.UINT12, 2, CHANNEL_DATA_ENDIAN.LSB,
			CHANNEL_UNITS.METER_PER_SECOND_SQUARE,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL),
			0x01);
    
    public static final ChannelDetails channelAccelZ = new ChannelDetails(
			ObjectClusterSensorName.ACCEL_LN_Z,
			ObjectClusterSensorName.ACCEL_LN_Z,
			DatabaseChannelHandles.LN_ACC_Z,
//			CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.LSB,
			CHANNEL_DATA_TYPE.UINT12, 2, CHANNEL_DATA_ENDIAN.LSB,
			CHANNEL_UNITS.METER_PER_SECOND_SQUARE,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL),
			0x02);
    
    // GYRO
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
    
    public static final Map<String, ChannelDetails> mChannelMapRef;
    static {
        Map<String, ChannelDetails> aMap = new LinkedHashMap<String, ChannelDetails>();
        aMap.put(SensorLSM6DSV.ObjectClusterSensorName.ACCEL_LN_X, SensorLSM6DSV.channelAccelX);
        aMap.put(SensorLSM6DSV.ObjectClusterSensorName.ACCEL_LN_Y, SensorLSM6DSV.channelAccelY);
        aMap.put(SensorLSM6DSV.ObjectClusterSensorName.ACCEL_LN_Z, SensorLSM6DSV.channelAccelZ);
        
		aMap.put(ObjectClusterSensorName.GYRO_X, SensorLSM6DSV.channelGyroX);
		aMap.put(ObjectClusterSensorName.GYRO_Y, SensorLSM6DSV.channelGyroY);
		aMap.put(ObjectClusterSensorName.GYRO_Z, SensorLSM6DSV.channelGyroZ);
		mChannelMapRef = Collections.unmodifiableMap(aMap);
    }
	//--------- Channel info end --------------
    
    public static final SensorGroupingDetails sensorGroupLnAccelLSM6DSV = new SensorGroupingDetails(
    		SensorLSM6DSV.LABEL_SENSOR_TILE.LOW_NOISE_ACCEL,
			Arrays.asList(Configuration.Shimmer3.SENSOR_ID.SHIMMER_LSM6DSV_ACCEL_LN),
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoKionixKXTC92050);
	
    
    
	//--------- Bluetooth commands start --------------
    // ACCEL_LN
	public static final byte SET_LN_ACCEL_CALIBRATION_COMMAND			= (byte) 0x11;
	public static final byte LN_ACCEL_CALIBRATION_RESPONSE       		= (byte) 0x12;
	public static final byte GET_LN_ACCEL_CALIBRATION_COMMAND    		= (byte) 0x13;
	
	public static final byte SET_ALT_ACCEL_RANGE_COMMAND				= (byte) 0x4F;
	public static final byte ALT_ACCEL_RANGE_RESPONSE					= (byte) 0x50;
	public static final byte GET_ALT_ACCEL_RANGE_COMMAND				= (byte) 0x51;
	
	//GYRO
	public static final byte SET_GYRO_CALIBRATION_COMMAND 	  		= (byte) 0x14;
	public static final byte GYRO_CALIBRATION_RESPONSE        		= (byte) 0x15;
	public static final byte GET_GYRO_CALIBRATION_COMMAND     		= (byte) 0x16;
	public static final byte SET_LSM6DSV_GYRO_RANGE_COMMAND 		= (byte) 0x49;
	public static final byte LSM6DSV_GYRO_RANGE_RESPONSE 			= (byte) 0x4A;
	public static final byte GET_LSM6DSV_GYRO_RANGE_COMMAND 		= (byte) 0x4B;
	public static final byte SET_LSM6DSV_SAMPLING_RATE_COMMAND 		= (byte) 0x4C;
	public static final byte LSM6DSV_SAMPLING_RATE_RESPONSE 		= (byte) 0x4D;
	public static final byte GET_LSM6DSV_SAMPLING_RATE_COMMAND 		= (byte) 0x4E;
	
	 public static final Map<Byte, BtCommandDetails> mBtGetCommandMap;
	    static {
	        Map<Byte, BtCommandDetails> aMap = new LinkedHashMap<Byte, BtCommandDetails>();
	        
	        aMap.put(GET_LN_ACCEL_CALIBRATION_COMMAND, new BtCommandDetails(GET_LN_ACCEL_CALIBRATION_COMMAND,"GET_LN_ACCEL_CALIBRATION_COMMAND",LN_ACCEL_CALIBRATION_RESPONSE));
	        
	        aMap.put(GET_GYRO_CALIBRATION_COMMAND, new BtCommandDetails(GET_GYRO_CALIBRATION_COMMAND, "GET_GYRO CALIBRATION_COMMAND", GYRO_CALIBRATION_RESPONSE));
	        aMap.put(GET_LSM6DSV_GYRO_RANGE_COMMAND, new BtCommandDetails(GET_LSM6DSV_GYRO_RANGE_COMMAND, "GET_LSM6DSV GYRO RANGE_COMMAND", LSM6DSV_GYRO_RANGE_RESPONSE));
	        aMap.put(GET_LSM6DSV_SAMPLING_RATE_COMMAND, new BtCommandDetails(GET_LSM6DSV_SAMPLING_RATE_COMMAND, "GET_LSM6DSV_SAMPLING_RATE_COMMAND", LSM6DSV_SAMPLING_RATE_RESPONSE));
	        mBtGetCommandMap = Collections.unmodifiableMap(aMap);
	    }
	    
	    public static final Map<Byte, BtCommandDetails> mBtSetCommandMap;
	    static {
	        Map<Byte, BtCommandDetails> aMap = new LinkedHashMap<Byte, BtCommandDetails>();
	        
			aMap.put(SET_LN_ACCEL_CALIBRATION_COMMAND, new BtCommandDetails(SET_LN_ACCEL_CALIBRATION_COMMAND, "SET_LN_ACCEL_CALIBRATION_COMMAND"));
	        
	        aMap.put(SET_GYRO_CALIBRATION_COMMAND, new BtCommandDetails(SET_GYRO_CALIBRATION_COMMAND, "SET_GYRO_CALIBRATION_COMMAND"));
	        aMap.put(SET_LSM6DSV_GYRO_RANGE_COMMAND, new BtCommandDetails(SET_LSM6DSV_GYRO_RANGE_COMMAND, "SET_LSM6DSV_GYRO_RANGE_COMMAND"));
	        aMap.put(SET_LSM6DSV_SAMPLING_RATE_COMMAND, new BtCommandDetails(SET_LSM6DSV_SAMPLING_RATE_COMMAND, "SET_LSM6DSV_SAMPLING_RATE_COMMAND"));
	        mBtSetCommandMap = Collections.unmodifiableMap(aMap);
	    }
	//--------- Bluetooth commands end --------------
	
	
	
	//--------- Sensor specific methods start --------------
	private byte[] generateCalParamAnalogAccel(){
		return mCurrentCalibDetailsAccelLn.generateCalParamByteArray();
	}
	
	public void parseCalibParamFromPacketAccelAnalog(byte[] bufferCalibrationParameters, CALIB_READ_SOURCE calibReadSource) {
		mCurrentCalibDetailsAccelLn.parseCalParamByteArray(bufferCalibrationParameters, calibReadSource);
	}
	
	private void setDefaultCalibrationShimmer3LowNoiseAccel() {
		mCurrentCalibDetailsAccelLn.resetToDefaultParameters();
	}

	public String getSensorName(){
		return mSensorName;
	}
	
	public boolean isUsingDefaultLNAccelParam(){
		return mCurrentCalibDetailsAccelLn.isUsingDefaultParameters();
	}
	
	public double[][] getAlignmentMatrixAccel(){
		return mCurrentCalibDetailsAccelLn.getValidAlignmentMatrix();
	}

	public double[][] getSensitivityMatrixAccel(){
		return mCurrentCalibDetailsAccelLn.getValidSensitivityMatrix();
	}

	public double[][] getOffsetVectorMatrixAccel(){
		return mCurrentCalibDetailsAccelLn.getValidOffsetVector();
	}
	
	public void updateCurrentAccelLnCalibInUse(){
		mCurrentCalibDetailsAccelLn = getCurrentCalibDetailsAccelLn();
	}
	
	public CalibDetailsKinematic getCurrentCalibDetailsAccelLn(){
		CalibDetails calibPerSensor = getCalibForSensor(Configuration.Shimmer3.SENSOR_ID.SHIMMER_LSM6DSV_ACCEL_LN, LN_ACCEL_RANGE_VALUE_2G);
		if(calibPerSensor!=null){
			return (CalibDetailsKinematic) calibPerSensor;
		}
		return null;
	}
	//--------- Sensor specific methods end --------------
	
	
	
	// Constructors for Class START ------------------------------------------
	public SensorLSM6DSV() {
		super(SENSORS.LSM6DSV);
		initialise();
	}

	public SensorLSM6DSV(ShimmerVerObject shimmerVerObject) {
		super(SENSORS.LSM6DSV, shimmerVerObject);
		initialise();
	}
	
	public SensorLSM6DSV(ShimmerDevice shimmerDevice) {
		super(SENSORS.LSM6DSV, shimmerDevice);
		initialise();
	}
	// Constructors for Class END ------------------------------------------

	
	
	// GYRO Methods ----------------------------------------------------------
	public int getGyroRange(){
		return mGyroRange;
	}
	
	public int getLSM6DSVGyroAccelRate() {
		return mLSM6DSVGyroAccelRate;
	}
	
	public CalibDetailsKinematic getCurrentCalibDetailsGyro(){
		return mCurrentCalibDetailsGyro;
	}
	
	public void setLSM6DSVGyroAccelRate(int rate) {
		mLSM6DSVGyroAccelRate = rate;
	}
	
	public void setGyroRange(int gyroRange){
		setLSM6DSVGyroRange(gyroRange);
	}
	
	public void setLSM6DSVGyroRange(int i){
		if(ArrayUtils.contains(ListofLSM6DSVGyroRangeConfigValues, i)){
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
	// GYRO Methods end ------------------------------------------------------
	
	
	
	@Override
	public void generateSensorMap() {
		// TODO Auto-generated method stub
		super.createLocalSensorMapWithCustomParser(mSensorMapRef, mChannelMapRef);
	}
	
	@Override
	public LinkedHashMap<String, Object> generateConfigMap() {
		LinkedHashMap<String, Object> mapOfConfig = new LinkedHashMap<String, Object>();
		
		mapOfConfig.put(SensorMPU9250.DatabaseConfigHandle.GYRO_RANGE, getGyroRange());
		mapOfConfig.put(SensorMPU9250.DatabaseConfigHandle.GYRO_RATE, getLSM6DSVGyroAccelRate());
		
		super.addCalibDetailsToDbMap(mapOfConfig, 
				getCurrentCalibDetailsAccelLn(), 
				SensorLSM6DSV.DatabaseConfigHandle.LIST_OF_CALIB_HANDLES_LN_ACC,
				SensorLSM6DSV.DatabaseConfigHandle.LN_ACC_CALIB_TIME);
		
		super.addCalibDetailsToDbMap(mapOfConfig, 
				getCurrentCalibDetailsGyro(), 
				DatabaseConfigHandle.LIST_OF_CALIB_HANDLES_GYRO,
				DatabaseConfigHandle.GYRO_CALIB_TIME);
		
		return mapOfConfig;
	}
	
	@Override
	public void parseConfigMap(LinkedHashMap<String, Object> mapOfConfigPerShimmer) {
		//Analog Accel Calibration Configuration
		parseCalibDetailsKinematicFromDb(mapOfConfigPerShimmer, 
				Configuration.Shimmer3.SENSOR_ID.SHIMMER_ANALOG_ACCEL, 
				0, 
				SensorLSM6DSV.DatabaseConfigHandle.LIST_OF_CALIB_HANDLES_LN_ACC,
				SensorLSM6DSV.DatabaseConfigHandle.LN_ACC_CALIB_TIME);
		
		if(mapOfConfigPerShimmer.containsKey(DatabaseConfigHandle.GYRO_RATE)){
			setLSM6DSVGyroAccelRate(((Double) mapOfConfigPerShimmer.get(DatabaseConfigHandle.GYRO_RATE)).intValue());
		}
		if(mapOfConfigPerShimmer.containsKey(DatabaseConfigHandle.GYRO_RANGE)){
			setGyroRange(((Double) mapOfConfigPerShimmer.get(DatabaseConfigHandle.GYRO_RANGE)).intValue());
		}
		
		//Gyroscope Calibration Configuration
		parseCalibDetailsKinematicFromDb(mapOfConfigPerShimmer, 
				mSensorIdGyro, 
				getGyroRange(), 
				SensorLSM6DSV.DatabaseConfigHandle.LIST_OF_CALIB_HANDLES_GYRO,
				SensorLSM6DSV.DatabaseConfigHandle.GYRO_CALIB_TIME);
	}

	@Override
	public void generateConfigOptionsMap() {
		// No Configuration Needed for Accel
		
		// For Gyro
		mConfigOptionsMap.clear();
		addConfigOption(configOptionLSM6DSVGyroRange);
		//General Config
		addConfigOption(configOptionLSM6DSVGyroRate);
		addConfigOption(configOptionLSM6DSVGyroLpm);
	}

	@Override
	public void generateSensorGroupMapping() {
		mSensorGroupingMap = new LinkedHashMap<Integer, SensorGroupingDetails>();
		mSensorGroupingMap.put(Configuration.Shimmer3.LABEL_SENSOR_TILE.LOW_NOISE_ACCEL.ordinal(), sensorGroupLnAccelLSM6DSV);
		mSensorGroupingMap.put(Configuration.Shimmer3.LABEL_SENSOR_TILE.GYRO.ordinal(), new SensorGroupingDetails(
				LABEL_SENSOR_TILE.GYRO,
				Arrays.asList(mSensorIdGyro),
				CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPU9250));
		
		super.updateSensorGroupingMap();	
	}

	@Override
	public ObjectCluster processDataCustom(SensorDetails sensorDetails, byte[] rawData, COMMUNICATION_TYPE commType,
			ObjectCluster objectCluster, boolean isTimeSyncEnabled, double pctimeStampMs) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void checkShimmerConfigBeforeConfiguring() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void configBytesGenerate(ShimmerDevice shimmerDevice, byte[] configBytes, COMMUNICATION_TYPE commType) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void configBytesParse(ShimmerDevice shimmerDevice, byte[] configBytes, COMMUNICATION_TYPE commType) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Object setConfigValueUsingConfigLabel(Integer sensorId, String configLabel, Object valueToSet) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getConfigValueUsingConfigLabel(Integer sensorId, String configLabel) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setSensorSamplingRate(double samplingRateHz) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean setDefaultConfigForSensor(int sensorId, boolean isSensorEnabled) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean checkConfigOptionValues(String stringKey) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Object getSettings(String componentName, COMMUNICATION_TYPE commType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ActionSetting setSettings(String componentName, Object valueToSet, COMMUNICATION_TYPE commType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean processResponse(int responseCommand, Object parsedResponse, COMMUNICATION_TYPE commType) {
		// TODO Auto-generated method stub
		return false;
	}
	
	
	
	//--------- Optional methods to override in Sensor Class start --------
	@Override
	public void initialise() {
		mSensorIdGyro = Configuration.Shimmer3.SENSOR_ID.SHIMMER_LSM6DSV_GYRO;

		super.initialise();
		
		updateCurrentAccelLnCalibInUse();
		setCalibSensitivityScaleFactor(mSensorIdGyro, CALIBRATION_SCALE_FACTOR.ONE_HUNDRED);
		updateCurrentGyroCalibInUse();
	}
	
	@Override
	public void generateCalibMap() {
		super.generateCalibMap();
		
		// ACCEL_LN
		TreeMap<Integer, CalibDetails> calibMapAccelLn = new TreeMap<Integer, CalibDetails>();
		calibMapAccelLn.put(calibDetailsAccelLn2g.mRangeValue, calibDetailsAccelLn2g);
		
		setCalibrationMapPerSensor(Configuration.Shimmer3.SENSOR_ID.SHIMMER_LSM6DSV_ACCEL_LN, calibMapAccelLn);
		
		updateCurrentAccelLnCalibInUse();
		
		// GYRO
		TreeMap<Integer, CalibDetails> calibMapGyro = new TreeMap<Integer, CalibDetails>();
		calibMapGyro.put(calibDetailsGyro125.mRangeValue, calibDetailsGyro125);
		calibMapGyro.put(calibDetailsGyro250.mRangeValue, calibDetailsGyro250);
		calibMapGyro.put(calibDetailsGyro500.mRangeValue, calibDetailsGyro500);
		calibMapGyro.put(calibDetailsGyro1000.mRangeValue, calibDetailsGyro1000);
		calibMapGyro.put(calibDetailsGyro2000.mRangeValue, calibDetailsGyro2000);
		calibMapGyro.put(calibDetailsGyro4000.mRangeValue, calibDetailsGyro4000);
		
		mCalibMap.put(mSensorIdGyro, calibMapGyro);
		updateCurrentGyroCalibInUse();
	}
	//--------- Optional methods to override in Sensor Class end --------
	
	
	public void updateCurrentGyroCalibInUse(){
		mCurrentCalibDetailsGyro = getCurrentCalibDetailsIfKinematic(mSensorIdGyro, getGyroRange());
	}
	
}