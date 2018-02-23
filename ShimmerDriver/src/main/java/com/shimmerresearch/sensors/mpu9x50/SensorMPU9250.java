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
import com.shimmerresearch.driverUtilities.ChannelDetails;
import com.shimmerresearch.driverUtilities.ConfigOptionDetailsSensor;
import com.shimmerresearch.driverUtilities.SensorDetailsRef;
import com.shimmerresearch.driverUtilities.SensorGroupingDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_ENDIAN;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_TYPE;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_TYPE;
import com.shimmerresearch.sensors.AbstractSensor;
import com.shimmerresearch.sensors.mpu9x50.SensorMPU9X50.GuiLabelConfig;

public class SensorMPU9250 extends SensorMPU9X50 {

	private static final long serialVersionUID = 6559532137082204767L;

	
	//--------- Sensor specific variables start --------------
	public static class DatabaseChannelHandles{
		public static final String GYRO_X = "MPU9250_GYRO_X";
		public static final String GYRO_Y = "MPU9250_GYRO_Y";
		public static final String GYRO_Z = "MPU9250_GYRO_Z";
		public static final String ALTERNATIVE_ACC_X = "MPU9250_ACC_X"; // not available but supported in FW
		public static final String ALTERNATIVE_ACC_Y = "MPU9250_ACC_Y"; // not available but supported in FW
		public static final String ALTERNATIVE_ACC_Z = "MPU9250_ACC_Z"; // not available but supported in FW
		public static final String ALTERNATIVE_MAG_X = "MPU9250_MAG_X"; // not available but supported in FW
		public static final String ALTERNATIVE_MAG_Y = "MPU9250_MAG_Y"; // not available but supported in FW
		public static final String ALTERNATIVE_MAG_Z = "MPU9250_MAG_Z"; // not available but supported in FW
	}
	
	public static final class DatabaseConfigHandle{
		public static final String MPU_QUAT_6DOF = "MPU9250_MPL_Quat_6DOF"; 
		public static final String MPU_EULER_6DOF = "MPU9250_MPL_Euler_6DOF"; 
		public static final String MPU_HEADING_ENABLE = "MPU9250_MPL_Heading"; // not available but supported in FW //channel
		
		public static final String GYRO_RATE = "MPU9250_Gyro_Rate";
		public static final String GYRO_RANGE = "MPU9250_Gyro_Range";
		public static final String ALTERNATIVE_ACC_RANGE = "MPU9250_Acc_Range";
		
		
		public static final String MPU_MAG_SAMPLING_RATE = "MPU9250_MAG_Sampling_rate";

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
		// MPU ACCEL
		public static final String MPU_ACC_OFFSET_X = "MPU9250_Acc_Offset_X";
		public static final String MPU_ACC_OFFSET_Y = "MPU9250_Acc_Offset_Y";
		public static final String MPU_ACC_OFFSET_Z = "MPU9250_Acc_Offset_Z";
		public static final String MPU_ACC_GAIN_X = "MPU9250_Acc_Gain_X";
		public static final String MPU_ACC_GAIN_Y = "MPU9250_Acc_Gain_Y";
		public static final String MPU_ACC_GAIN_Z = "MPU9250_Acc_Gain_Z";
		public static final String MPU_ACC_ALIGN_XX = "MPU9250_Acc_Align_XX";
		public static final String MPU_ACC_ALIGN_XY = "MPU9250_Acc_Align_XY";
		public static final String MPU_ACC_ALIGN_XZ = "MPU9250_Acc_Align_XZ";
		public static final String MPU_ACC_ALIGN_YX = "MPU9250_Acc_Align_YX";
		public static final String MPU_ACC_ALIGN_YY = "MPU9250_Acc_Align_YY";
		public static final String MPU_ACC_ALIGN_YZ = "MPU9250_Acc_Align_YZ";
		public static final String MPU_ACC_ALIGN_ZX = "MPU9250_Acc_Align_ZX";
		public static final String MPU_ACC_ALIGN_ZY = "MPU9250_Acc_Align_ZY";
		public static final String MPU_ACC_ALIGN_ZZ = "MPU9250_Acc_Align_ZZ";
		// MPU MAG
		public static final String MPU_MAG_OFFSET_X = "MPU9250_Mag_Offset_X";
		public static final String MPU_MAG_OFFSET_Y = "MPU9250_Mag_Offset_Y";
		public static final String MPU_MAG_OFFSET_Z = "MPU9250_Mag_Offset_Z";
		public static final String MPU_MAG_GAIN_X = "MPU9250_Mag_Gain_X";
		public static final String MPU_MAG_GAIN_Y = "MPU9250_Mag_Gain_Y";
		public static final String MPU_MAG_GAIN_Z = "MPU9250_Mag_Gain_Z";
		public static final String MPU_MAG_ALIGN_XX = "MPU9250_Mag_Align_XX";
		public static final String MPU_MAG_ALIGN_XY = "MPU9250_Mag_Align_XY";
		public static final String MPU_MAG_ALIGN_XZ = "MPU9250_Mag_Align_XZ";
		public static final String MPU_MAG_ALIGN_YX = "MPU9250_Mag_Align_YX";
		public static final String MPU_MAG_ALIGN_YY = "MPU9250_Mag_Align_YY";
		public static final String MPU_MAG_ALIGN_YZ = "MPU9250_Mag_Align_YZ";
		public static final String MPU_MAG_ALIGN_ZX = "MPU9250_Mag_Align_ZX";
		public static final String MPU_MAG_ALIGN_ZY = "MPU9250_Mag_Align_ZY";
		public static final String MPU_MAG_ALIGN_ZZ = "MPU9250_Mag_Align_ZZ";
		
		public static final List<String> LIST_OF_CALIB_HANDLES_GYRO = Arrays.asList(
				DatabaseConfigHandle.GYRO_OFFSET_X, DatabaseConfigHandle.GYRO_OFFSET_Y, DatabaseConfigHandle.GYRO_OFFSET_Z,
				DatabaseConfigHandle.GYRO_GAIN_X, DatabaseConfigHandle.GYRO_GAIN_Y, DatabaseConfigHandle.GYRO_GAIN_Z,
				DatabaseConfigHandle.GYRO_ALIGN_XX, DatabaseConfigHandle.GYRO_ALIGN_XY, DatabaseConfigHandle.GYRO_ALIGN_XZ,
				DatabaseConfigHandle.GYRO_ALIGN_YX, DatabaseConfigHandle.GYRO_ALIGN_YY, DatabaseConfigHandle.GYRO_ALIGN_YZ,
				DatabaseConfigHandle.GYRO_ALIGN_ZX, DatabaseConfigHandle.GYRO_ALIGN_ZY, DatabaseConfigHandle.GYRO_ALIGN_ZZ);
		
		public static final List<String> LIST_OF_CALIB_HANDLES_MPU_ACC = Arrays.asList(
				DatabaseConfigHandle.MPU_ACC_OFFSET_X, DatabaseConfigHandle.MPU_ACC_OFFSET_Y, DatabaseConfigHandle.MPU_ACC_OFFSET_Z,
				DatabaseConfigHandle.MPU_ACC_GAIN_X, DatabaseConfigHandle.MPU_ACC_GAIN_Y, DatabaseConfigHandle.MPU_ACC_GAIN_Z,
				DatabaseConfigHandle.MPU_ACC_ALIGN_XX, DatabaseConfigHandle.MPU_ACC_ALIGN_XY, DatabaseConfigHandle.MPU_ACC_ALIGN_XZ,
				DatabaseConfigHandle.MPU_ACC_ALIGN_YX, DatabaseConfigHandle.MPU_ACC_ALIGN_YY, DatabaseConfigHandle.MPU_ACC_ALIGN_YZ,
				DatabaseConfigHandle.MPU_ACC_ALIGN_ZX, DatabaseConfigHandle.MPU_ACC_ALIGN_ZY, DatabaseConfigHandle.MPU_ACC_ALIGN_ZZ);
		
		public static final List<String> LIST_OF_CALIB_HANDLES_MPU_MAG = Arrays.asList(
				DatabaseConfigHandle.MPU_MAG_OFFSET_X, DatabaseConfigHandle.MPU_MAG_OFFSET_Y, DatabaseConfigHandle.MPU_MAG_OFFSET_Z,
				DatabaseConfigHandle.MPU_MAG_GAIN_X, DatabaseConfigHandle.MPU_MAG_GAIN_Y, DatabaseConfigHandle.MPU_MAG_GAIN_Z,
				DatabaseConfigHandle.MPU_MAG_ALIGN_XX, DatabaseConfigHandle.MPU_MAG_ALIGN_XY, DatabaseConfigHandle.MPU_MAG_ALIGN_XZ,
				DatabaseConfigHandle.MPU_MAG_ALIGN_YX, DatabaseConfigHandle.MPU_MAG_ALIGN_YY, DatabaseConfigHandle.MPU_MAG_ALIGN_YZ,
				DatabaseConfigHandle.MPU_MAG_ALIGN_ZX, DatabaseConfigHandle.MPU_MAG_ALIGN_ZY, DatabaseConfigHandle.MPU_MAG_ALIGN_ZZ);
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

		mChannelMapRef = Collections.unmodifiableMap(aMap);
    }

	//--------- Channel info end --------------
    
	//--------- Sensor info start --------------

	public static final SensorDetailsRef sensorMpu9250GyroRef = new SensorDetailsRef(0x40<<(0*8), 0x40<<(0*8), GuiLabelSensors.GYRO,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPU9250,
			Arrays.asList(Configuration.Shimmer3.SENSOR_ID.SHIMMER_MPU9X50_MPL_GYRO),
			Arrays.asList(
					GuiLabelConfig.MPU9X50_GYRO_RANGE, 
					GuiLabelConfig.MPU9X50_GYRO_RATE,
					GuiLabelConfig.GYRO_ON_THE_FLY_CALIB_STATE,
					GuiLabelConfig.GYRO_ON_THE_FLY_CALIB_THRESHOLD),
			Arrays.asList(
					ObjectClusterSensorName.GYRO_X, 
					ObjectClusterSensorName.GYRO_Y, 
					ObjectClusterSensorName.GYRO_Z),
			false);
//	{
//		sensorMpu9150GyroRef.mCalibSensorKey = 0x01;
//	}
	
	public static final SensorDetailsRef sensorMpu9250AccelRef = new SensorDetailsRef(0x40<<(2*8), 0x40<<(2*8), GuiLabelSensors.ACCEL_MPU,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoShimmer4,
			Arrays.asList(Configuration.Shimmer3.SENSOR_ID.SHIMMER_MPU9X50_MPL_ACCEL), 
			Arrays.asList(GuiLabelConfig.MPU9X50_ACCEL_RANGE),
			Arrays.asList(
					ObjectClusterSensorName.ACCEL_MPU_X,
					ObjectClusterSensorName.ACCEL_MPU_Y,
					ObjectClusterSensorName.ACCEL_MPU_Z),
			false);

	public static final SensorDetailsRef sensorMpu9250MagRef = new SensorDetailsRef(0x20<<(2*8), 0x20<<(2*8), GuiLabelSensors.MAG_MPU,
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
		aMap.put(Configuration.Shimmer3.SENSOR_ID.SHIMMER_MPU9X50_GYRO, SensorMPU9250.sensorMpu9250GyroRef);
		aMap.put(Configuration.Shimmer3.SENSOR_ID.SHIMMER_MPU9X50_ACCEL, SensorMPU9250.sensorMpu9250AccelRef);
		aMap.put(Configuration.Shimmer3.SENSOR_ID.SHIMMER_MPU9X50_MAG,SensorMPU9250.sensorMpu9250MagRef);
		mSensorMapRef = Collections.unmodifiableMap(aMap);
    }
	//--------- Sensor info end --------------
    
	//--------- Configuration options start --------------

	public static final ConfigOptionDetailsSensor configOptionMpu9250GyroRange = new ConfigOptionDetailsSensor(
			SensorMPU9X50.GuiLabelConfig.MPU9X50_GYRO_RANGE,
			SensorMPU9250.DatabaseConfigHandle.GYRO_RANGE,
			ListofGyroRange, 
			ListofMPU9X50GyroRangeConfigValues, 
			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW);
	//MPL Options
	public static final ConfigOptionDetailsSensor configOptionMpu9250AccelRange = new ConfigOptionDetailsSensor(
			SensorMPU9X50.GuiLabelConfig.MPU9X50_ACCEL_RANGE,
			SensorMPU9250.DatabaseConfigHandle.ALTERNATIVE_ACC_RANGE,
			ListofMPU9X50AccelRange, 
			ListofMPU9X50AccelRangeConfigValues, 
			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors);
	
	public static final ConfigOptionDetailsSensor configOptionMpu9250MagRate = new ConfigOptionDetailsSensor(
			SensorMPU9X50.GuiLabelConfig.MPU9X50_MAG_RATE,
			SensorMPU9250.DatabaseConfigHandle.MPU_MAG_SAMPLING_RATE,
			ListofMPU9X50MagRate, 
			ListofMPU9X50MagRateConfigValues, 
			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors);
	//General Config
	public static final ConfigOptionDetailsSensor configOptionMpu9250GyroRate = new ConfigOptionDetailsSensor(
			SensorMPU9X50.GuiLabelConfig.MPU9X50_GYRO_RATE,
			SensorMPU9250.DatabaseConfigHandle.GYRO_RATE,
			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.TEXTFIELD,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW);

	public static final ConfigOptionDetailsSensor configOptionMpu9250GyroLpm = new ConfigOptionDetailsSensor(
			SensorMPU9X50.GuiLabelConfig.MPU9X50_GYRO_LPM,
			null,
			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.CHECKBOX,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW);
	//--------- Configuration options end --------------


	//--------- Sensor specific variables end --------------


    //--------- Constructors for this class start --------------

	/**Just used for accessing calibration*/
	public SensorMPU9250(){
		super(SENSORS.MPU9X50);
		initialise();
	}

	/** Constructor for this class
	 * @param svo
	 */
	public SensorMPU9250(ShimmerDevice shimmerDevice){
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
		addConfigOption(configOptionMpu9250GyroRange);
		//MPL Options
		addConfigOption(configOptionMpu9250AccelRange);
		addConfigOption(configOptionMpu9250MagRate);
		//General Config
		addConfigOption(configOptionMpu9250GyroRate);
		addConfigOption(configOptionMpu9250GyroLpm);
		//Calibration Config
		addConfigOption(configOptionGyroOnTheFlyCalibState);
		addConfigOption(configOptionGyroOnTheFlyCalibThreshold);
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

		mSensorGroupingMap.put(Configuration.Shimmer3.LABEL_SENSOR_TILE.MPU.ordinal(), new SensorGroupingDetails(
				LABEL_SENSOR_TILE.MPU,
				Arrays.asList(mSensorIdAccel,
						mSensorIdGyro,
						mSensorIdMag),
				CompatibilityInfoForMaps.listOfCompatibleVersionInfoShimmer4));
		
		mSensorGroupingMap.put(Configuration.Shimmer3.LABEL_SENSOR_TILE.GYRO.ordinal(), new SensorGroupingDetails(
				LABEL_SENSOR_TILE.GYRO,
				Arrays.asList(mSensorIdGyro),
				CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPU9250));

		super.updateSensorGroupingMap();
	}
	

	@Override
	public LinkedHashMap<String, Object> generateConfigMap() {
		LinkedHashMap<String, Object> mapOfConfig = new LinkedHashMap<String, Object>();
		mapOfConfig.put(SensorMPU9250.DatabaseConfigHandle.GYRO_RANGE, getGyroRange());
		mapOfConfig.put(SensorMPU9250.DatabaseConfigHandle.GYRO_RATE, getMPU9X50GyroAccelRate());
		
		mapOfConfig.put(SensorMPU9250.DatabaseConfigHandle.ALTERNATIVE_ACC_RANGE, getMPU9X50AccelRange());

		mapOfConfig.put(SensorMPU9250.DatabaseConfigHandle.MPU_MAG_SAMPLING_RATE, getMPU9X50MagSamplingRate());

		super.addCalibDetailsToDbMap(mapOfConfig, 
				getCurrentCalibDetailsGyro(), 
				DatabaseConfigHandle.LIST_OF_CALIB_HANDLES_GYRO,
				DatabaseConfigHandle.GYRO_CALIB_TIME);
		
		AbstractSensor.addCalibDetailsToDbMap(mapOfConfig, 
				SensorMPU9250.DatabaseConfigHandle.LIST_OF_CALIB_HANDLES_MPU_ACC,
				getOffsetVectorMPLAccel(),
				getSensitivityMatrixMPLAccel(),
				getAlignmentMatrixMPLAccel());
		
		AbstractSensor.addCalibDetailsToDbMap(mapOfConfig, 
				SensorMPU9250.DatabaseConfigHandle.LIST_OF_CALIB_HANDLES_MPU_MAG,
				getOffsetVectorMPLMag(),
				getSensitivityMatrixMPLMag(),
				getAlignmentMatrixMPLMag());

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
		if(mapOfConfigPerShimmer.containsKey(DatabaseConfigHandle.MPU_MAG_SAMPLING_RATE)){
			setMPU9X50MagSamplingRate(((Double) mapOfConfigPerShimmer.get(DatabaseConfigHandle.MPU_MAG_SAMPLING_RATE)).intValue());
		}
		
		//Gyroscope Calibration Configuration
		parseCalibDetailsKinematicFromDb(mapOfConfigPerShimmer, 
				mSensorIdGyro, 
				getGyroRange(), 
				SensorMPU9250.DatabaseConfigHandle.LIST_OF_CALIB_HANDLES_GYRO,
				SensorMPU9250.DatabaseConfigHandle.GYRO_CALIB_TIME);
	}


	
	public static String parseFromDBColumnToGUIChannel(String databaseChannelHandle) {
		return AbstractSensor.parseFromDBColumnToGUIChannel(mChannelMapRef, databaseChannelHandle);
	}

	public static String parseFromGUIChannelsToDBColumn(String objectClusterName) {
		return AbstractSensor.parseFromGUIChannelsToDBColumn(mChannelMapRef, objectClusterName);
	}
}
