package com.shimmerresearch.verisense;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.ArrayUtils;

import com.shimmerresearch.driver.Configuration.CHANNEL_UNITS;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driver.Configuration.Verisense.CompatibilityInfoForMaps;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driver.calibration.CalibDetails;
import com.shimmerresearch.driver.calibration.CalibDetailsKinematic;
import com.shimmerresearch.driver.calibration.UtilCalibration;
import com.shimmerresearch.driverUtilities.ChannelDetails;
import com.shimmerresearch.driverUtilities.ConfigOptionDetailsSensor;
import com.shimmerresearch.driverUtilities.SensorDetails;
import com.shimmerresearch.driverUtilities.SensorDetailsRef;
import com.shimmerresearch.driverUtilities.UtilShimmer;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_ENDIAN;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_TYPE;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_TYPE;
import com.shimmerresearch.sensors.AbstractSensor;
import com.shimmerresearch.sensors.ActionSetting;
import com.shimmerresearch.verisense.payloaddesign.AsmBinaryFileConstants;
import com.shimmerresearch.verisense.payloaddesign.AsmBinaryFileConstants.ASM_CONFIG_BYTE_INDEX;

public class SensorLSM6DS3 extends AbstractSensor {
	
	private static final long serialVersionUID = -8572264157525359641L;
	
	private static final int DEFAULT_FIFO_BYTE_SIZE_IN_CHIP = 8112;
	private static final int DEFAULT_MAX_FIFOS_IN_PAYLOAD = 4;
	private int fifoByteSizeInChip = DEFAULT_FIFO_BYTE_SIZE_IN_CHIP;
	private int fifoSizeInChip = DEFAULT_FIFO_BYTE_SIZE_IN_CHIP/2;

	public static final String ACCEL_ID = "Accel2";
	
	protected int rangeAccel = LSM6DS3_ACCEL_RANGE_CONFIG_VALUES[1];
	protected int rangeGyro = LSM6DS3_GYRO_RANGE_CONFIG_VALUES[1];
	protected int rate = LSM6DS3_RATE_CONFIG_VALUES[3];

	public static final String[] LSM6DS3_RATE={"Power-down","12.5Hz","26.0Hz","52.0Hz","104.0Hz","208.0Hz","416.0Hz","833.0Hz","1666.0Hz"};
	public static final Integer[] LSM6DS3_RATE_CONFIG_VALUES={0,1,2,3,4,5,6,7,8};

	public static final String[] LSM6DS3_ACCEL_RANGE={
			UtilShimmer.UNICODE_PLUS_MINUS + " 2g",
			UtilShimmer.UNICODE_PLUS_MINUS + " 4g",
			UtilShimmer.UNICODE_PLUS_MINUS + " 8g",
			UtilShimmer.UNICODE_PLUS_MINUS + " 16g"};  
	public static final Integer[] LSM6DS3_ACCEL_RANGE_CONFIG_VALUES={0,2,3,1};  // Config values order might change
	
	public static final String[] LSM6DS3_GYRO_RANGE={
			UtilShimmer.UNICODE_PLUS_MINUS + " 250dps",
			UtilShimmer.UNICODE_PLUS_MINUS + " 500dps",
			UtilShimmer.UNICODE_PLUS_MINUS + " 1000dps",
			UtilShimmer.UNICODE_PLUS_MINUS + " 2000dps"};  
	public static final Integer[] LSM6DS3_GYRO_RANGE_CONFIG_VALUES={0,1,2,3};  // Config values order might change

	// --------------- Configuration options start ----------------

	public class GuiLabelSensors{
		public static final String ACCEL2 = "Accelerometer2"; 
		public static final String GYRO = "Gyroscope"; 
	}

	public static class DatabaseChannelHandles{
		public static final String LSM6DS3_ACC_X = "LSM6DS3_ACC_X";
		public static final String LSM6DS3_ACC_Y = "LSM6DS3_ACC_Y";
		public static final String LSM6DS3_ACC_Z = "LSM6DS3_ACC_Z";
		public static final String LSM6DS3_GYRO_X = "LSM6DS3_GYRO_X";
		public static final String LSM6DS3_GYRO_Y = "LSM6DS3_GYRO_Y";
		public static final String LSM6DS3_GYRO_Z = "LSM6DS3_GYRO_Z";
	}

	public class GuiLabelConfig{
		public static final String LSM6DS3_RATE = "Accel_Gyro_Rate";
		public static final String LSM6DS3_ACCEL_RANGE = "Accel_Range";
		public static final String LSM6DS3_GYRO_RANGE = "Gyro_Range";
	}

	public static class ObjectClusterSensorName{
		public static  String LSM6DS3_ACC_X = ACCEL_ID + "_X";
		public static  String LSM6DS3_ACC_Y = ACCEL_ID + "_Y";
		public static  String LSM6DS3_ACC_Z= ACCEL_ID + "_Z";
		public static  String LSM6DS3_GYRO_X = "Gyro_X";
		public static  String LSM6DS3_GYRO_Y = "Gyro_Y";
		public static  String LSM6DS3_GYRO_Z= "Gyro_Z";
	}

	public static final class DatabaseConfigHandle{
		public static final String LSM6DS3_RANGE = "LSM6DS3_Range";
		public static final String LSM6DS3_RATE = "LSM6DS3_Rate";
	}

	public static final ConfigOptionDetailsSensor CONFIG_OPTION_ACCEL_RANGE = new ConfigOptionDetailsSensor (
			SensorLSM6DS3.GuiLabelConfig.LSM6DS3_ACCEL_RANGE,
			SensorLSM6DS3.DatabaseConfigHandle.LSM6DS3_RANGE,
			LSM6DS3_ACCEL_RANGE, 
			LSM6DS3_ACCEL_RANGE_CONFIG_VALUES, 
			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoLSM6DS3);

	// Rate is same for both accel and gyro
	public static final ConfigOptionDetailsSensor CONFIG_OPTION_RATE = new ConfigOptionDetailsSensor (
			SensorLSM6DS3.GuiLabelConfig.LSM6DS3_RATE,
			SensorLSM6DS3.DatabaseConfigHandle.LSM6DS3_RATE,
			LSM6DS3_RATE, 
			LSM6DS3_RATE_CONFIG_VALUES, 
			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoLSM6DS3);
	
	public static final ConfigOptionDetailsSensor CONFIG_OPTION_GYRO_RANGE = new ConfigOptionDetailsSensor (
			SensorLSM6DS3.GuiLabelConfig.LSM6DS3_GYRO_RANGE,
			SensorLSM6DS3.DatabaseConfigHandle.LSM6DS3_RANGE,
			LSM6DS3_GYRO_RANGE, 
			LSM6DS3_GYRO_RANGE_CONFIG_VALUES, 
			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoLSM6DS3);

	// --------------- Configuration options end ----------------
	
	// ----------------- Calibration Start -----------------------

	public static final double[][] DEFAULT_OFFSET_VECTOR_LSM6DS3 = {{0},{0},{0}};	
	public static final double[][] DEFAULT_ALIGNMENT_MATRIX_LSM6DS3 = {{0,0,1},{-1,0,0},{0,-1,0}};
	
	public static final double[][] DEFAULT_SENSITIVITY_MATRIX_LSM6DS3_2G = {{1671.665922915,0,0},{0,1671.665922915,0},{0,0,1671.665922915}};
	public static final double[][] DEFAULT_SENSITIVITY_MATRIX_LSM6DS3_4G = {{835.832961457,0,0},{0,835.832961457,0},{0,0,835.832961457}};
	public static final double[][] DEFAULT_SENSITIVITY_MATRIX_LSM6DS3_8G = {{417.916480729,0,0},{0,417.916480729,0},{0,0,417.916480729}};
	public static final double[][] DEFAULT_SENSITIVITY_MATRIX_LSM6DS3_16G = {{208.958240364,0,0},{0,208.958240364,0},{0,0,208.958240364}};
	
//	public static final double[][] DEFAULT_SENSITIVITY_MATRIX_LSM6DS3_125DPS = {{228.571428571,0,0},{0,228.571428571,0},{0,0,228.571428571}};
	public static final double[][] DEFAULT_SENSITIVITY_MATRIX_LSM6DS3_250DPS = {{114.285714286,0,0},{0,114.285714286,0},{0,0,114.285714286}};
	public static final double[][] DEFAULT_SENSITIVITY_MATRIX_LSM6DS3_500DPS = {{57.142857143,0,0},{0,57.142857143,0},{0,0,57.142857143}};
	public static final double[][] DEFAULT_SENSITIVITY_MATRIX_LSM6DS3_1000DPS = {{28.571428571,0,0},{0,28.571428571,0},{0,0,28.571428571}};
	public static final double[][] DEFAULT_SENSITIVITY_MATRIX_LSM6DS3_2000DPS = {{14.285714286,0,0},{0,14.285714286,0},{0,0,14.285714286}};

	public CalibDetailsKinematic calibDetailsAccel2g = new CalibDetailsKinematic(
			LSM6DS3_ACCEL_RANGE_CONFIG_VALUES[0],
			LSM6DS3_ACCEL_RANGE[0],
			DEFAULT_ALIGNMENT_MATRIX_LSM6DS3, 
			DEFAULT_SENSITIVITY_MATRIX_LSM6DS3_2G, 
			DEFAULT_OFFSET_VECTOR_LSM6DS3);
	public CalibDetailsKinematic calibDetailsAccel4g = new CalibDetailsKinematic(
			LSM6DS3_ACCEL_RANGE_CONFIG_VALUES[1], 
			LSM6DS3_ACCEL_RANGE[1],
			DEFAULT_ALIGNMENT_MATRIX_LSM6DS3,
			DEFAULT_SENSITIVITY_MATRIX_LSM6DS3_4G, 
			DEFAULT_OFFSET_VECTOR_LSM6DS3);
	public CalibDetailsKinematic calibDetailsAccel8g = new CalibDetailsKinematic(
			LSM6DS3_ACCEL_RANGE_CONFIG_VALUES[2], 
			LSM6DS3_ACCEL_RANGE[2],
			DEFAULT_ALIGNMENT_MATRIX_LSM6DS3, 
			DEFAULT_SENSITIVITY_MATRIX_LSM6DS3_8G, 
			DEFAULT_OFFSET_VECTOR_LSM6DS3);
	public CalibDetailsKinematic calibDetailsAccel16g = new CalibDetailsKinematic(
			LSM6DS3_ACCEL_RANGE_CONFIG_VALUES[3], 
			LSM6DS3_ACCEL_RANGE[3],
			DEFAULT_ALIGNMENT_MATRIX_LSM6DS3,
			DEFAULT_SENSITIVITY_MATRIX_LSM6DS3_16G, 
			DEFAULT_OFFSET_VECTOR_LSM6DS3);
	
	public CalibDetailsKinematic calibDetailsGyro250dps = new CalibDetailsKinematic(
			LSM6DS3_GYRO_RANGE_CONFIG_VALUES[0], 
			LSM6DS3_GYRO_RANGE[0],
			DEFAULT_ALIGNMENT_MATRIX_LSM6DS3,
			DEFAULT_SENSITIVITY_MATRIX_LSM6DS3_250DPS, 
			DEFAULT_OFFSET_VECTOR_LSM6DS3);
	public CalibDetailsKinematic calibDetailsGyro500dps = new CalibDetailsKinematic(
			LSM6DS3_GYRO_RANGE_CONFIG_VALUES[1], 
			LSM6DS3_GYRO_RANGE[1],
			DEFAULT_ALIGNMENT_MATRIX_LSM6DS3, 
			DEFAULT_SENSITIVITY_MATRIX_LSM6DS3_500DPS, 
			DEFAULT_OFFSET_VECTOR_LSM6DS3);
	public CalibDetailsKinematic calibDetailsGyro1000dps = new CalibDetailsKinematic(
			LSM6DS3_GYRO_RANGE_CONFIG_VALUES[2], 
			LSM6DS3_GYRO_RANGE[2],
			DEFAULT_ALIGNMENT_MATRIX_LSM6DS3,
			DEFAULT_SENSITIVITY_MATRIX_LSM6DS3_1000DPS, 
			DEFAULT_OFFSET_VECTOR_LSM6DS3);
	public CalibDetailsKinematic calibDetailsGyro2000dps = new CalibDetailsKinematic(
			LSM6DS3_GYRO_RANGE_CONFIG_VALUES[3], 
			LSM6DS3_GYRO_RANGE[3],
			DEFAULT_ALIGNMENT_MATRIX_LSM6DS3,
			DEFAULT_SENSITIVITY_MATRIX_LSM6DS3_2000DPS, 
			DEFAULT_OFFSET_VECTOR_LSM6DS3);
	
	public CalibDetailsKinematic mCurrentCalibDetailsAccel = calibDetailsAccel2g;
	public CalibDetailsKinematic mCurrentCalibDetailsGyro = calibDetailsGyro250dps;

	// ----------------- Calibration end -----------------------

  	//--------- Sensor info start --------------
	public static final SensorDetailsRef SENSOR_LSM6DS3_ACCEL = new SensorDetailsRef(
			Configuration.Verisense.SensorBitmap.LSM6DS3_ACCEL,
			Configuration.Verisense.SensorBitmap.LSM6DS3_ACCEL,
			GuiLabelSensors.ACCEL2,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoLSM6DS3,
			Arrays.asList(GuiLabelConfig.LSM6DS3_ACCEL_RANGE,
					GuiLabelConfig.LSM6DS3_GYRO_RANGE,
					GuiLabelConfig.LSM6DS3_RATE),
			Arrays.asList(ObjectClusterSensorName.LSM6DS3_ACC_X,
					ObjectClusterSensorName.LSM6DS3_ACC_Y,
					ObjectClusterSensorName.LSM6DS3_ACC_Z));

	public static final SensorDetailsRef SENSOR_LSM6DS3_GYRO = new SensorDetailsRef(
			Configuration.Verisense.SensorBitmap.LSM6DS3_GYRO,
			Configuration.Verisense.SensorBitmap.LSM6DS3_GYRO,
			GuiLabelSensors.GYRO,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoLSM6DS3,
			Arrays.asList(GuiLabelConfig.LSM6DS3_ACCEL_RANGE,
					GuiLabelConfig.LSM6DS3_GYRO_RANGE,
					GuiLabelConfig.LSM6DS3_RATE),
			Arrays.asList(ObjectClusterSensorName.LSM6DS3_GYRO_X,
					ObjectClusterSensorName.LSM6DS3_GYRO_Y,
					ObjectClusterSensorName.LSM6DS3_GYRO_Z));
	
  	public static final Map<Integer, SensorDetailsRef> SENSOR_MAP_REF;
	static {
		Map<Integer, SensorDetailsRef> aMap = new LinkedHashMap<Integer, SensorDetailsRef>();
		aMap.put(Configuration.Verisense.SENSOR_ID.LSM6DS3_ACCEL, SensorLSM6DS3.SENSOR_LSM6DS3_ACCEL);  
		aMap.put(Configuration.Verisense.SENSOR_ID.LSM6DS3_GYRO, SensorLSM6DS3.SENSOR_LSM6DS3_GYRO);  
		SENSOR_MAP_REF = Collections.unmodifiableMap(aMap);
	}

  	//--------- Sensor info end --------------
	
	//--------- Channel info start --------------
	
	public static final ChannelDetails CHANNEL_LSM6DS3_ACCEL_X = new ChannelDetails(
			ObjectClusterSensorName.LSM6DS3_ACC_X,
			ObjectClusterSensorName.LSM6DS3_ACC_X,
			DatabaseChannelHandles.LSM6DS3_ACC_X,
			CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.LSB,
			CHANNEL_UNITS.METER_PER_SECOND_SQUARE,
			Arrays.asList(CHANNEL_TYPE.UNCAL, CHANNEL_TYPE.CAL, CHANNEL_TYPE.DERIVED));
	
	public static final ChannelDetails CHANNEL_LSM6DS3_ACCEL_Y = new ChannelDetails(
			ObjectClusterSensorName.LSM6DS3_ACC_Y,
			ObjectClusterSensorName.LSM6DS3_ACC_Y,
			DatabaseChannelHandles.LSM6DS3_ACC_Y,
			CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.LSB,
			CHANNEL_UNITS.METER_PER_SECOND_SQUARE,
			Arrays.asList(CHANNEL_TYPE.UNCAL, CHANNEL_TYPE.CAL, CHANNEL_TYPE.DERIVED));
	
	public static final ChannelDetails CHANNEL_LSM6DS3_ACCEL_Z = new ChannelDetails(
			ObjectClusterSensorName.LSM6DS3_ACC_Z,
			ObjectClusterSensorName.LSM6DS3_ACC_Z,
			DatabaseChannelHandles.LSM6DS3_ACC_Z,
			CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.LSB,
			CHANNEL_UNITS.METER_PER_SECOND_SQUARE,
			Arrays.asList(CHANNEL_TYPE.UNCAL, CHANNEL_TYPE.CAL, CHANNEL_TYPE.DERIVED));

	public static final ChannelDetails CHANNEL_LSM6DS3_GYRO_X = new ChannelDetails(
			ObjectClusterSensorName.LSM6DS3_GYRO_X,
			ObjectClusterSensorName.LSM6DS3_GYRO_X,
			DatabaseChannelHandles.LSM6DS3_GYRO_X,
			CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.LSB,
			CHANNEL_UNITS.DEGREES_PER_SECOND,
			Arrays.asList(CHANNEL_TYPE.UNCAL, CHANNEL_TYPE.CAL, CHANNEL_TYPE.DERIVED));
	
	public static final ChannelDetails CHANNEL_LSM6DS3_GYRO_Y = new ChannelDetails(
			ObjectClusterSensorName.LSM6DS3_GYRO_Y,
			ObjectClusterSensorName.LSM6DS3_GYRO_Y,
			DatabaseChannelHandles.LSM6DS3_GYRO_Y,
			CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.LSB,
			CHANNEL_UNITS.DEGREES_PER_SECOND,
			Arrays.asList(CHANNEL_TYPE.UNCAL, CHANNEL_TYPE.CAL, CHANNEL_TYPE.DERIVED));
	
	public static final ChannelDetails CHANNEL_LSM6DS3_GYRO_Z = new ChannelDetails(
			ObjectClusterSensorName.LSM6DS3_GYRO_Z,
			ObjectClusterSensorName.LSM6DS3_GYRO_Z,
			DatabaseChannelHandles.LSM6DS3_GYRO_Z,
			CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.LSB,
			CHANNEL_UNITS.DEGREES_PER_SECOND,
			Arrays.asList(CHANNEL_TYPE.UNCAL, CHANNEL_TYPE.CAL, CHANNEL_TYPE.DERIVED));
	
	public static final Map<String, ChannelDetails> CHANNEL_MAP_REF;
	static {
		Map<String, ChannelDetails> aMap = new LinkedHashMap<String, ChannelDetails>();
		aMap.put(SensorLSM6DS3.ObjectClusterSensorName.LSM6DS3_ACC_X, SensorLSM6DS3.CHANNEL_LSM6DS3_ACCEL_X);
		aMap.put(SensorLSM6DS3.ObjectClusterSensorName.LSM6DS3_ACC_Y, SensorLSM6DS3.CHANNEL_LSM6DS3_ACCEL_Y);
		aMap.put(SensorLSM6DS3.ObjectClusterSensorName.LSM6DS3_ACC_Z, SensorLSM6DS3.CHANNEL_LSM6DS3_ACCEL_Z);
		
		aMap.put(SensorLSM6DS3.ObjectClusterSensorName.LSM6DS3_GYRO_X, SensorLSM6DS3.CHANNEL_LSM6DS3_GYRO_X);
		aMap.put(SensorLSM6DS3.ObjectClusterSensorName.LSM6DS3_GYRO_Y, SensorLSM6DS3.CHANNEL_LSM6DS3_GYRO_Y);
		aMap.put(SensorLSM6DS3.ObjectClusterSensorName.LSM6DS3_GYRO_Z, SensorLSM6DS3.CHANNEL_LSM6DS3_GYRO_Z);

		CHANNEL_MAP_REF = Collections.unmodifiableMap(aMap);
	}

	//--------- Channel info end --------------

	public SensorLSM6DS3(ShimmerDevice shimmerDevice) {
		super(SENSORS.LSM6DS3, shimmerDevice);
		initialise();
	}

	@Override
	public void generateSensorMap() {
		super.createLocalSensorMapWithCustomParser(SENSOR_MAP_REF, CHANNEL_MAP_REF);
	}

	@Override
	public void generateConfigOptionsMap() {
		mConfigOptionsMap.clear();
		addConfigOption(CONFIG_OPTION_ACCEL_RANGE);
		addConfigOption(CONFIG_OPTION_GYRO_RANGE);
		addConfigOption(CONFIG_OPTION_RATE);
	}

	@Override
	public void generateSensorGroupMapping() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ObjectCluster processDataCustom(SensorDetails sensorDetails, byte[] rawData, COMMUNICATION_TYPE commType, ObjectCluster objectCluster, boolean isTimeSyncEnabled, long pcTimestamp) {
		
		// get uncalibrated data for each (sub)sensor
		if(sensorDetails.mSensorDetailsRef.mGuiFriendlyLabel.equals(GuiLabelSensors.ACCEL2) && mCurrentCalibDetailsAccel!=null){
			// ADC values
			objectCluster = sensorDetails.processDataCommon(rawData, commType, objectCluster, isTimeSyncEnabled, pcTimestamp);

			double[] unCalibratedAccel = new double[3];
			unCalibratedAccel[0] = objectCluster.getFormatClusterValue(SensorLSM6DS3.CHANNEL_LSM6DS3_ACCEL_X, CHANNEL_TYPE.UNCAL);
			unCalibratedAccel[1] = objectCluster.getFormatClusterValue(SensorLSM6DS3.CHANNEL_LSM6DS3_ACCEL_Y, CHANNEL_TYPE.UNCAL);
			unCalibratedAccel[2] = objectCluster.getFormatClusterValue(SensorLSM6DS3.CHANNEL_LSM6DS3_ACCEL_Z, CHANNEL_TYPE.UNCAL);
			
			//Add default calibrated data to Object cluster
			double[] defaultCalAccel = UtilCalibration.calibrateInertialSensorData(unCalibratedAccel, mCurrentCalibDetailsAccel.getDefaultMatrixMultipliedInverseAMSM(), mCurrentCalibDetailsAccel.getDefaultOffsetVector());
			objectCluster.addCalData(CHANNEL_LSM6DS3_ACCEL_X, defaultCalAccel[0], objectCluster.getIndexKeeper()-3);
			objectCluster.addCalData(CHANNEL_LSM6DS3_ACCEL_Y, defaultCalAccel[1], objectCluster.getIndexKeeper()-2);
			objectCluster.addCalData(CHANNEL_LSM6DS3_ACCEL_Z, defaultCalAccel[2], objectCluster.getIndexKeeper()-1);

			//Add auto-calibrated data to Object cluster - if available
			boolean isCurrentValuesSet = mCurrentCalibDetailsAccel.isCurrentValuesSet();
			if(isCurrentValuesSet) {
				double[] autoCalAccel = UtilCalibration.calibrateImuData(defaultCalAccel, mCurrentCalibDetailsAccel.getCurrentSensitivityMatrix(), mCurrentCalibDetailsAccel.getCurrentOffsetVector());
				//Add calibrated data to Object cluster
				objectCluster.addData(CHANNEL_LSM6DS3_ACCEL_X.mObjectClusterName, CHANNEL_TYPE.DERIVED, CHANNEL_LSM6DS3_ACCEL_X.mDefaultCalUnits, autoCalAccel[0], objectCluster.getIndexKeeper()-3, false);
				objectCluster.addData(CHANNEL_LSM6DS3_ACCEL_Y.mObjectClusterName, CHANNEL_TYPE.DERIVED, CHANNEL_LSM6DS3_ACCEL_Y.mDefaultCalUnits, autoCalAccel[1], objectCluster.getIndexKeeper()-2, false);
				objectCluster.addData(CHANNEL_LSM6DS3_ACCEL_Z.mObjectClusterName, CHANNEL_TYPE.DERIVED, CHANNEL_LSM6DS3_ACCEL_Z.mDefaultCalUnits, autoCalAccel[2], objectCluster.getIndexKeeper()-1, false);
			}
		}
		else if(sensorDetails.mSensorDetailsRef.mGuiFriendlyLabel.equals(GuiLabelSensors.GYRO) && mCurrentCalibDetailsGyro!=null){
			// ADC values
			objectCluster = sensorDetails.processDataCommon(rawData, commType, objectCluster, isTimeSyncEnabled, pcTimestamp);

			double[] unCalibratedGyro = new double[3];
			unCalibratedGyro[0] = objectCluster.getFormatClusterValue(SensorLSM6DS3.CHANNEL_LSM6DS3_GYRO_X, CHANNEL_TYPE.UNCAL);
			unCalibratedGyro[1] = objectCluster.getFormatClusterValue(SensorLSM6DS3.CHANNEL_LSM6DS3_GYRO_Y, CHANNEL_TYPE.UNCAL);
			unCalibratedGyro[2] = objectCluster.getFormatClusterValue(SensorLSM6DS3.CHANNEL_LSM6DS3_GYRO_Z, CHANNEL_TYPE.UNCAL);

			//Add default calibrated data to Object cluster
			double[] defaultCalGyro = UtilCalibration.calibrateInertialSensorData(unCalibratedGyro, mCurrentCalibDetailsGyro.getDefaultMatrixMultipliedInverseAMSM(), mCurrentCalibDetailsGyro.getDefaultOffsetVector());
			objectCluster.addData(CHANNEL_LSM6DS3_GYRO_X.mObjectClusterName, CHANNEL_TYPE.CAL, CHANNEL_LSM6DS3_GYRO_X.mDefaultCalUnits, defaultCalGyro[0], objectCluster.getIndexKeeper()-3, true);
			objectCluster.addData(CHANNEL_LSM6DS3_GYRO_Y.mObjectClusterName, CHANNEL_TYPE.CAL, CHANNEL_LSM6DS3_GYRO_Y.mDefaultCalUnits, defaultCalGyro[1], objectCluster.getIndexKeeper()-2, true);
			objectCluster.addData(CHANNEL_LSM6DS3_GYRO_Z.mObjectClusterName, CHANNEL_TYPE.CAL, CHANNEL_LSM6DS3_GYRO_Z.mDefaultCalUnits, defaultCalGyro[2], objectCluster.getIndexKeeper()-1, true);
			
			//Add auto-calibrated data to Object cluster
			//TODO use below if calibration CSVs contain valid Alignment values
//			double[] autoCalGyro = UtilCalibration.calibrateInertialSensorData(unCalibratedGyro, mCurrentCalibDetailsGyro);
			//TODO use below if calibration CSVs contains an identity matrix
			double[] autoCalGyro = UtilCalibration.calibrateInertialSensorData(unCalibratedGyro, mCurrentCalibDetailsGyro.getDefaultAlignmentMatrix(), mCurrentCalibDetailsGyro.getValidSensitivityMatrix(), mCurrentCalibDetailsGyro.getValidOffsetVector());
			objectCluster.addData(CHANNEL_LSM6DS3_GYRO_X.mObjectClusterName, CHANNEL_TYPE.DERIVED, CHANNEL_LSM6DS3_GYRO_X.mDefaultCalUnits, autoCalGyro[0], objectCluster.getIndexKeeper()-3, false);
			objectCluster.addData(CHANNEL_LSM6DS3_GYRO_Y.mObjectClusterName, CHANNEL_TYPE.DERIVED, CHANNEL_LSM6DS3_GYRO_Y.mDefaultCalUnits, autoCalGyro[1], objectCluster.getIndexKeeper()-2, false);
			objectCluster.addData(CHANNEL_LSM6DS3_GYRO_Z.mObjectClusterName, CHANNEL_TYPE.DERIVED, CHANNEL_LSM6DS3_GYRO_Z.mDefaultCalUnits, autoCalGyro[2], objectCluster.getIndexKeeper()-1, false);
		}
		return objectCluster;
	}

	@Override
	public void checkShimmerConfigBeforeConfiguring() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void configBytesGenerate(ShimmerDevice shimmerDevice, byte[] configBytes, COMMUNICATION_TYPE commType) {
//		if(isAnySensorChannelEnabled(VerisenseDevice.defaultCommType)) {
		if(isEitherLsm6ds3ChannelEnabled()) {
			ConfigByteLayoutLsm6ds3 configByteLayout = new ConfigByteLayoutLsm6ds3(shimmerDevice);

			if(shimmerDevice instanceof VerisenseDevice) {
				VerisenseDevice verisenseDevice = (VerisenseDevice) shimmerDevice;
				if(verisenseDevice.isPayloadDesignV8orAbove()) {
					configBytes[ASM_CONFIG_BYTE_INDEX.PAYLOAD_CONFIG3] |= (getAccelRangeConfigValue()&0x03);
				} else {
					configBytes[ASM_CONFIG_BYTE_INDEX.PAYLOAD_CONFIG0] |= (getAccelRangeConfigValue()&0x03)<<2;
				}

				configBytes[configByteLayout.idxGyroAccel2Cfg5] |= (getRateConfigValue()&0x0F)<<4;
				configBytes[configByteLayout.idxGyroAccel2Cfg5] |= (getGyroRangeConfigValue()&0x03)<<2;	
				
				configBytes[configByteLayout.idxGyroAccel2Cfg0] = (byte) (fifoSizeInChip & 0xFF);
				configBytes[configByteLayout.idxGyroAccel2Cfg1] = (byte) (((byte) (fifoSizeInChip >> 8)) & 0x0F);
			}
		}
	}

	@Override
	public void configBytesParse(ShimmerDevice shimmerDevice, byte[] configBytes, COMMUNICATION_TYPE commType) {
//		if(isAnySensorChannelEnabled(VerisenseDevice.defaultCommType)) {
		if(isEitherLsm6ds3ChannelEnabled()) {
			ConfigByteLayoutLsm6ds3 configByteLayout = new ConfigByteLayoutLsm6ds3(shimmerDevice);

			if(shimmerDevice instanceof VerisenseDevice) {
				VerisenseDevice verisenseDevice = (VerisenseDevice) shimmerDevice;
				if(verisenseDevice.isPayloadDesignV8orAbove()) {
					setAccelRangeConfigValue(configBytes[ASM_CONFIG_BYTE_INDEX.PAYLOAD_CONFIG3]&0x03);
				} else {
					setAccelRangeConfigValue((configBytes[ASM_CONFIG_BYTE_INDEX.PAYLOAD_CONFIG0]>>2)&0x03);
				}

				setRateConfigValue((configBytes[configByteLayout.idxGyroAccel2Cfg5]>>4)&0x0F);
				setGyroRangeConfigValue((configBytes[configByteLayout.idxGyroAccel2Cfg5]>>2)&0x03);
	
				int fifoSizeInChip = (configBytes[configByteLayout.idxGyroAccel2Cfg0]&0xFF) 
						| ((configBytes[configByteLayout.idxGyroAccel2Cfg1]&0x0F)<<8);
				setFifoSizeInChip(fifoSizeInChip);
			}
		}
	}

	public void setFifoSizeInChip(int fifoSizeInChip) {
		// FIFO size was added to the payload config midway through development (FW v1.02.005?) so need to check for 0 
		if(fifoSizeInChip==0) {
			this.fifoSizeInChip = DEFAULT_FIFO_BYTE_SIZE_IN_CHIP/2;
			this.fifoByteSizeInChip = DEFAULT_FIFO_BYTE_SIZE_IN_CHIP;
		} else {
			this.fifoSizeInChip = fifoSizeInChip;
			this.fifoByteSizeInChip = fifoSizeInChip*2;
		}
	}

	public int getFifoByteSizeInChip() {
		return fifoByteSizeInChip;
	}

	/** 
	 * NOTE: Only appropriate for Gen 1 to 7 of the Payload Design
	 * 
	 * @param memAvailable
	 * @return
	 */
	public int calculateMaxPayloadsInFifo(int memAvailable) {
		int maxFifosInPayload = DEFAULT_MAX_FIFOS_IN_PAYLOAD;
		if(mShimmerDevice instanceof VerisenseDevice) {
			maxFifosInPayload = (int) Math.floor(memAvailable/(AsmBinaryFileConstants.ACCEL_SPI_BUS_HEADER_BYTES+fifoByteSizeInChip));
		}
		return maxFifosInPayload;
	}
	
	@Override
	public Object setConfigValueUsingConfigLabel(Integer sensorId, String configLabel, Object valueToSet) {
		Object returnValue = null;
		
		switch(configLabel){
			case(GuiLabelConfig.LSM6DS3_RATE):
				setRateConfigValue((int)valueToSet);
				break;
			case(GuiLabelConfig.LSM6DS3_ACCEL_RANGE):
				setAccelRangeConfigValue((int)valueToSet);
				break;
			case(GuiLabelConfig.LSM6DS3_GYRO_RANGE):
				setGyroRangeConfigValue((int)valueToSet);
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
			case(GuiLabelConfig.LSM6DS3_RATE):
				returnValue = getRateConfigValue();
				break;
			case(GuiLabelConfig.LSM6DS3_ACCEL_RANGE):
				returnValue = getAccelRangeConfigValue();
				break;
			case(GuiLabelConfig.LSM6DS3_GYRO_RANGE):
				returnValue = getGyroRangeConfigValue();
				break;
			case(GuiLabelConfigCommon.CALIBRATION_CURRENT_PER_SENSOR):
				if(sensorId==Configuration.Verisense.SENSOR_ID.LSM6DS3_GYRO) {
					returnValue = mCurrentCalibDetailsGyro;
				} else if(sensorId==Configuration.Verisense.SENSOR_ID.LSM6DS3_ACCEL) {
					returnValue = mCurrentCalibDetailsAccel;
				}
				break;
			case(GuiLabelConfigCommon.RATE):
				returnValue = getRateFreq();
				break;
			default:
				returnValue = super.getConfigValueUsingConfigLabelCommon(sensorId, configLabel);
				break;
		}
		return returnValue;
	}

	@Override
	public void setSensorSamplingRate(double samplingRateHz) {
		int accelRate = 0;
		
		if (samplingRateHz==0){
			accelRate = LSM6DS3_RATE_CONFIG_VALUES[0];
		} else if (samplingRateHz<=12.5){
			accelRate = LSM6DS3_RATE_CONFIG_VALUES[1]; // 12.5Hz
		} else if (samplingRateHz<=26){
			accelRate = LSM6DS3_RATE_CONFIG_VALUES[2]; // 25Hz
		} else if (samplingRateHz<=52){
			accelRate = LSM6DS3_RATE_CONFIG_VALUES[3]; // 50Hz
		} else if (samplingRateHz<=104){
			accelRate = LSM6DS3_RATE_CONFIG_VALUES[4]; // 100Hz
		} else if (samplingRateHz<=208){
			accelRate = LSM6DS3_RATE_CONFIG_VALUES[5]; // 200Hz
		} else if (samplingRateHz<=416){
			accelRate = LSM6DS3_RATE_CONFIG_VALUES[6]; // 400Hz
		} else if (samplingRateHz<=832){
			accelRate = LSM6DS3_RATE_CONFIG_VALUES[7]; // 800Hz
		} else { //if (freq<=1600){ 
			accelRate = LSM6DS3_RATE_CONFIG_VALUES[8]; // 1600Hz
		}
		setRateConfigValue(accelRate);
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

	@Override
	public LinkedHashMap<String, Object> generateConfigMap() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void parseConfigMap(LinkedHashMap<String, Object> mapOfConfigPerShimmer) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void initialise() {
		super.initialise();
		
		updateCurrentAccelCalibInUse();
	}

	@Override
	public void generateCalibMap() {
		super.generateCalibMap();
		
		TreeMap<Integer, CalibDetails> calibMapAccel = new TreeMap<Integer, CalibDetails>();
		calibMapAccel.put(calibDetailsAccel2g.mRangeValue, calibDetailsAccel2g);
		calibMapAccel.put(calibDetailsAccel4g.mRangeValue, calibDetailsAccel4g);
		calibMapAccel.put(calibDetailsAccel8g.mRangeValue, calibDetailsAccel8g);
		calibMapAccel.put(calibDetailsAccel16g.mRangeValue, calibDetailsAccel16g);
		setCalibrationMapPerSensor(Configuration.Verisense.SENSOR_ID.LSM6DS3_ACCEL, calibMapAccel);
		updateCurrentAccelCalibInUse();
		
		TreeMap<Integer, CalibDetails> calibMapGyro = new TreeMap<Integer, CalibDetails>();
		calibMapGyro.put(calibDetailsGyro250dps.mRangeValue, calibDetailsGyro250dps);
		calibMapGyro.put(calibDetailsGyro500dps.mRangeValue, calibDetailsGyro500dps);
		calibMapGyro.put(calibDetailsGyro1000dps.mRangeValue, calibDetailsGyro1000dps);
		calibMapGyro.put(calibDetailsGyro2000dps.mRangeValue, calibDetailsGyro2000dps);
		setCalibrationMapPerSensor(Configuration.Verisense.SENSOR_ID.LSM6DS3_GYRO, calibMapGyro);
		updateCurrentGyroCalibInUse();
	}

	public void updateCurrentAccelCalibInUse(){
		mCurrentCalibDetailsAccel = getCurrentCalibDetailsIfKinematic(Configuration.Verisense.SENSOR_ID.LSM6DS3_ACCEL, getAccelRangeConfigValue());
	}
	
	public void updateCurrentGyroCalibInUse(){
		mCurrentCalibDetailsGyro = getCurrentCalibDetailsIfKinematic(Configuration.Verisense.SENSOR_ID.LSM6DS3_GYRO, getGyroRangeConfigValue());
	}

	public int getAccelRangeConfigValue() {
		return rangeAccel;
	}
	
	public int getGyroRangeConfigValue() {
		return rangeGyro;
	}

	public void setAccelRangeConfigValue(int valueToSet){
		if(ArrayUtils.contains(LSM6DS3_ACCEL_RANGE_CONFIG_VALUES, valueToSet)){
			rangeAccel = valueToSet;
		}
		updateCurrentAccelCalibInUse();
	}

	public void setGyroRangeConfigValue(int valueToSet){
		if(ArrayUtils.contains(LSM6DS3_GYRO_RANGE_CONFIG_VALUES, valueToSet)){
			rangeGyro = valueToSet;
		}
		updateCurrentGyroCalibInUse();
	}
	
	public int getRateConfigValue() {
		return rate;
	}

	public double getRateFreq() {

		String accelRateString = CONFIG_OPTION_RATE.getConfigStringFromConfigValue(getRateConfigValue());
		
		double accelRate = 0;
		if(accelRateString.contains(CHANNEL_UNITS.FREQUENCY)){
			accelRate = Double.parseDouble(accelRateString.replace(CHANNEL_UNITS.FREQUENCY, ""));
		}
		
		return accelRate;
	}

	public void setRateConfigValue(int valueToSet) {
		Integer[] configValueToCheck = LSM6DS3_RATE_CONFIG_VALUES; 
		if(ArrayUtils.contains(configValueToCheck, valueToSet)){
			rate = valueToSet;
		}
	}

	public static double calibrateTemperature(long temperatureUncal) {
		double temp = ((temperatureUncal>>4)/16.0) + 25.0;
		return temp;
	}

	public boolean isEitherLsm6ds3ChannelEnabled() {
		if(isSensorEnabled(Configuration.Verisense.SENSOR_ID.LSM6DS3_ACCEL)
				|| isSensorEnabled(Configuration.Verisense.SENSOR_ID.LSM6DS3_GYRO)) {
			return true;
		}
		return false;
	}
	
	private class ConfigByteLayoutLsm6ds3 {
		public int idxGyroAccel2Cfg5 = 0, idxGyroAccel2Cfg0 = 0, idxGyroAccel2Cfg1 = 0;
		
		public ConfigByteLayoutLsm6ds3(ShimmerDevice shimmerDevice) {
			if(shimmerDevice instanceof VerisenseDevice) {
				VerisenseDevice verisenseDevice = (VerisenseDevice) shimmerDevice;
				if(verisenseDevice.isPayloadDesignV8orAbove()) {
					idxGyroAccel2Cfg5 = ASM_CONFIG_BYTE_INDEX.PAYLOAD_CONFIG3;
					idxGyroAccel2Cfg0 = ASM_CONFIG_BYTE_INDEX.PAYLOAD_CONFIG4;
					idxGyroAccel2Cfg1 = ASM_CONFIG_BYTE_INDEX.PAYLOAD_CONFIG5;
				} else if(verisenseDevice.isPayloadDesignV5orAbove()) {
					idxGyroAccel2Cfg5 = ASM_CONFIG_BYTE_INDEX.PAYLOAD_CONFIG1;
					idxGyroAccel2Cfg0 = ASM_CONFIG_BYTE_INDEX.PAYLOAD_CONFIG3;
					idxGyroAccel2Cfg1 = ASM_CONFIG_BYTE_INDEX.PAYLOAD_CONFIG4;
				} else {
					idxGyroAccel2Cfg5 = ASM_CONFIG_BYTE_INDEX.PAYLOAD_CONFIG1;
					idxGyroAccel2Cfg0 = ASM_CONFIG_BYTE_INDEX.PAYLOAD_CONFIG2;
					idxGyroAccel2Cfg1 = ASM_CONFIG_BYTE_INDEX.PAYLOAD_CONFIG3;
				}
			}
		}
	}

	public String getAccelRangeString() {
		int accelRangeConfigValue = getAccelRangeConfigValue();
		String accelRange = SensorLSM6DS3.CONFIG_OPTION_ACCEL_RANGE.getConfigStringFromConfigValue(accelRangeConfigValue);
		accelRange = accelRange.replaceAll(UtilShimmer.UNICODE_PLUS_MINUS, "+-");
		
		if(mShimmerDevice instanceof VerisenseDevice && !((VerisenseDevice)mShimmerDevice).isCsvHeaderDesignAzMarkingPoint()) {
			accelRange = accelRange.replaceAll(CHANNEL_UNITS.GRAVITY, (" " + CHANNEL_UNITS.GRAVITY));
		}

		return accelRange;
	}

	public String getGyroRangeString() {
		int gyroRangeConfigValue = getGyroRangeConfigValue();
		String gyroRange = SensorLSM6DS3.CONFIG_OPTION_GYRO_RANGE.getConfigStringFromConfigValue(gyroRangeConfigValue);
		gyroRange = gyroRange.replaceAll(UtilShimmer.UNICODE_PLUS_MINUS, "+-");
		
		if(mShimmerDevice instanceof VerisenseDevice && !((VerisenseDevice)mShimmerDevice).isCsvHeaderDesignAzMarkingPoint()) {
			gyroRange = gyroRange.replaceAll("dps", " dps");
		}

		return gyroRange;
	}

}
