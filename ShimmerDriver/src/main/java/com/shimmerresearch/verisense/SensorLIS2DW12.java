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
import com.shimmerresearch.verisense.communication.OpConfigPayload.OP_CONFIG_BYTE_INDEX;
import com.shimmerresearch.verisense.payloaddesign.AsmBinaryFileConstants.PAYLOAD_CONFIG_BYTE_INDEX;

public class SensorLIS2DW12 extends AbstractSensor {
	
	private static final long serialVersionUID = -3219602356151087121L;
	
	public static final int FIFO_SIZE_IN_CHIP = 192;
	public static final int MAX_FIFOS_IN_PAYLOAD = 169;
	
	public static final String ACCEL_ID = "Accel1";
	
	protected int range = LIS2DW12_ACCEL_RANGE_CONFIG_VALUES[1];
	protected int rate = LIS2DW12_ACCEL_RATE_HP_CONFIG_VALUES[3];
	protected int mode = LIS2DW12_MODE_CONFIG_VALUES[1];
	protected int lpMode = LIS2DW12_LP_MODE_CONFIG_VALUES[0];

	public static final String[] LIS2DW12_ACCEL_RATE_HP={"Power-down","12.5Hz","12.5Hz","25.0Hz","50.0Hz","100.0Hz","200.0Hz","400.0Hz","800.0Hz","1600.0Hz"};
	public static final Integer[] LIS2DW12_ACCEL_RATE_HP_CONFIG_VALUES={0,1,2,3,4,5,6,7,8,9};

	public static final String[] LIS2DW12_ACCEL_RATE_LP={"Power-down","1.6Hz","12.5Hz","25.0Hz","50.0Hz","100.0Hz","200.0Hz","200.0Hz","200.0Hz","200.0Hz"};
	public static final Integer[] LIS2DW12_ACCEL_RATE_LP_CONFIG_VALUES={0,1,2,3,4,5,6,7,8,9};
	
	public static final String[] LIS2DW12_ACCEL_RANGE={
			UtilShimmer.UNICODE_PLUS_MINUS + " 2g",
			UtilShimmer.UNICODE_PLUS_MINUS + " 4g",
			UtilShimmer.UNICODE_PLUS_MINUS + " 8g",
			UtilShimmer.UNICODE_PLUS_MINUS + " 16g"};  
	public static final Integer[] LIS2DW12_ACCEL_RANGE_CONFIG_VALUES={0,1,2,3};  

	public static final String[] LIS2DW12_MODE={
			"Low-Power Mode (12/14-bit resolution)",
			"High-Performance Mode (14-bit resolution)"};
	public static final Integer[] LIS2DW12_MODE_CONFIG_VALUES={0,1};

	public static final String[] LIS2DW12_LP_MODE={
			"LP1: 12-bit resolution, Noise=4.5mg(RMS)",
			"LP2: 14-bit resolution, Noise=2.4mg(RMS)",
			"LP3: 14-bit resolution, Noise=1.8mg(RMS)",
			"LP4: 14-bit resolution, Noise=1.3mg(RMS)"};
	public static final Integer[] LIS2DW12_LP_MODE_CONFIG_VALUES={0,1,2,3};

	
	public static final String[] LIS2DW12_RESOLUTION={
			"12-bit",
			"14-bit"};

	public static final String[] LIS2DW12_MODE_MERGED={
			"High-Performance Mode",
			"Low-Power Mode 1, RMS Noise = 4.5 mg",
			"Low-Power Mode 2, RMS Noise = 2.4 mg",
			"Low-Power Mode 3, RMS Noise = 1.8 mg",
			"Low-Power Mode 4, RMS Noise = 1.3 mg"};
	
	
	// --------------- Configuration options start ----------------

	public class GuiLabelSensors{
		public static final String ACCEL1 = "Accelerometer1"; 
	}

	public static class ObjectClusterSensorName{
		public static  String LIS2DW12_ACC_X = ACCEL_ID + "_X";
		public static  String LIS2DW12_ACC_Y = ACCEL_ID + "_Y";
		public static  String LIS2DW12_ACC_Z= ACCEL_ID + "_Z";
	}

	public static class DatabaseChannelHandles{
		public static final String LIS2DW12_ACC_X = "LIS2DW12_ACC_X";
		public static final String LIS2DW12_ACC_Y = "LIS2DW12_ACC_Y";
		public static final String LIS2DW12_ACC_Z = "LIS2DW12_ACC_Z";
	}

	public class GuiLabelConfig{
		public static final String LIS2DW12_RANGE = "Range";
		public static final String LIS2DW12_RATE = "Accel_Rate";
		public static final String LIS2DW12_MODE = "Mode";
		public static final String LIS2DW12_LP_MODE = "LP Mode";
	}
	
	public static final class DatabaseConfigHandle{
		public static final String LIS2DW12_RANGE = "LIS2DW12_Mag_Range";
		public static final String LIS2DW12_RATE = "LIS2DW12_Mag_Rate";
		public static final String LIS2DW12_MODE = "LIS2DW12_Mode";
		public static final String LIS2DW12_LP_MODE = "LIS2DW12_LpMode";
	}

	public static final ConfigOptionDetailsSensor CONFIG_OPTION_ACCEL_RANGE = new ConfigOptionDetailsSensor (
			SensorLIS2DW12.GuiLabelConfig.LIS2DW12_RANGE,
			SensorLIS2DW12.DatabaseConfigHandle.LIS2DW12_RANGE,
			LIS2DW12_ACCEL_RANGE, 
			LIS2DW12_ACCEL_RANGE_CONFIG_VALUES, 
			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoLIS2DW12);

	public static final ConfigOptionDetailsSensor CONFIG_OPTION_ACCEL_RATE_LP = new ConfigOptionDetailsSensor (
			SensorLIS2DW12.GuiLabelConfig.LIS2DW12_RATE,
			SensorLIS2DW12.DatabaseConfigHandle.LIS2DW12_RATE,
			LIS2DW12_ACCEL_RATE_LP, 
			LIS2DW12_ACCEL_RATE_LP_CONFIG_VALUES, 
			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoLIS2DW12);

	public static final ConfigOptionDetailsSensor CONFIG_OPTION_ACCEL_RATE_HP = new ConfigOptionDetailsSensor (
			SensorLIS2DW12.GuiLabelConfig.LIS2DW12_RATE,
			SensorLIS2DW12.DatabaseConfigHandle.LIS2DW12_RATE,
			LIS2DW12_ACCEL_RATE_HP, 
			LIS2DW12_ACCEL_RATE_HP_CONFIG_VALUES, 
			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoLIS2DW12);

	public static final ConfigOptionDetailsSensor CONFIG_OPTION_ACCEL_MODE = new ConfigOptionDetailsSensor (
			SensorLIS2DW12.GuiLabelConfig.LIS2DW12_MODE,
			SensorLIS2DW12.DatabaseConfigHandle.LIS2DW12_MODE,
			LIS2DW12_MODE, 
			LIS2DW12_MODE_CONFIG_VALUES, 
			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoLIS2DW12);
	
	public static final ConfigOptionDetailsSensor CONFIG_OPTION_ACCEL_LP_MODE = new ConfigOptionDetailsSensor (
			SensorLIS2DW12.GuiLabelConfig.LIS2DW12_LP_MODE,
			SensorLIS2DW12.DatabaseConfigHandle.LIS2DW12_LP_MODE,
			LIS2DW12_LP_MODE, 
			LIS2DW12_LP_MODE_CONFIG_VALUES, 
			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoLIS2DW12);

	// --------------- Configuration options end ----------------
	
	// ----------------- Calibration Start -----------------------

	public static final double[][] DEFAULT_OFFSET_VECTOR_LIS2DW12 = {{0},{0},{0}};	
	public static final double[][] DEFAULT_ALIGNMENT_MATRIX_LIS2DW12 = {{0,0,1},{1,0,0},{0,1,0}};
	
	public static final double[][] DEFAULT_SENSITIVITY_MATRIX_LIS2DW12_2G = {{1671.665922915,0,0},{0,1671.665922915,0},{0,0,1671.665922915}};
	public static final double[][] DEFAULT_SENSITIVITY_MATRIX_LIS2DW12_4G = {{835.832961457,0,0},{0,835.832961457,0},{0,0,835.832961457}};
	public static final double[][] DEFAULT_SENSITIVITY_MATRIX_LIS2DW12_8G = {{417.916480729,0,0},{0,417.916480729,0},{0,0,417.916480729}};
	public static final double[][] DEFAULT_SENSITIVITY_MATRIX_LIS2DW12_16G = {{208.958240364,0,0},{0,208.958240364,0},{0,0,208.958240364}};

	public CalibDetailsKinematic calibDetailsAccel2g = new CalibDetailsKinematic(
			LIS2DW12_ACCEL_RANGE_CONFIG_VALUES[0],
			LIS2DW12_ACCEL_RANGE[0],
			DEFAULT_ALIGNMENT_MATRIX_LIS2DW12, 
			DEFAULT_SENSITIVITY_MATRIX_LIS2DW12_2G, 
			DEFAULT_OFFSET_VECTOR_LIS2DW12);
	public CalibDetailsKinematic calibDetailsAccel4g = new CalibDetailsKinematic(
			LIS2DW12_ACCEL_RANGE_CONFIG_VALUES[1], 
			LIS2DW12_ACCEL_RANGE[1],
			DEFAULT_ALIGNMENT_MATRIX_LIS2DW12,
			DEFAULT_SENSITIVITY_MATRIX_LIS2DW12_4G, 
			DEFAULT_OFFSET_VECTOR_LIS2DW12);
	public CalibDetailsKinematic calibDetailsAccel8g = new CalibDetailsKinematic(
			LIS2DW12_ACCEL_RANGE_CONFIG_VALUES[2], 
			LIS2DW12_ACCEL_RANGE[2],
			DEFAULT_ALIGNMENT_MATRIX_LIS2DW12, 
			DEFAULT_SENSITIVITY_MATRIX_LIS2DW12_8G, 
			DEFAULT_OFFSET_VECTOR_LIS2DW12);
	public CalibDetailsKinematic calibDetailsAccel16g = new CalibDetailsKinematic(
			LIS2DW12_ACCEL_RANGE_CONFIG_VALUES[3], 
			LIS2DW12_ACCEL_RANGE[3],
			DEFAULT_ALIGNMENT_MATRIX_LIS2DW12,
			DEFAULT_SENSITIVITY_MATRIX_LIS2DW12_16G, 
			DEFAULT_OFFSET_VECTOR_LIS2DW12);

	public CalibDetailsKinematic mCurrentCalibDetailsAccel = calibDetailsAccel2g;
	
	// ----------------- Calibration end -----------------------

  	//--------- Sensor info start --------------
	public static final SensorDetailsRef SENOSR_LIS2DW12_ACCEL = new SensorDetailsRef(
			Configuration.Verisense.SensorBitmap.LIS2DW12_ACCEL,
			Configuration.Verisense.SensorBitmap.LIS2DW12_ACCEL,
			GuiLabelSensors.ACCEL1,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoLIS2DW12,
			Arrays.asList(GuiLabelConfig.LIS2DW12_RANGE,
					GuiLabelConfig.LIS2DW12_RATE),
			Arrays.asList(ObjectClusterSensorName.LIS2DW12_ACC_X,
					ObjectClusterSensorName.LIS2DW12_ACC_Y,
					ObjectClusterSensorName.LIS2DW12_ACC_Z));

  	public static final Map<Integer, SensorDetailsRef> SENSOR_MAP_REF;
	static {
		Map<Integer, SensorDetailsRef> aMap = new LinkedHashMap<Integer, SensorDetailsRef>();
		aMap.put(Configuration.Verisense.SENSOR_ID.LIS2DW12_ACCEL, SensorLIS2DW12.SENOSR_LIS2DW12_ACCEL);  
		SENSOR_MAP_REF = Collections.unmodifiableMap(aMap);
	}

  	//--------- Sensor info end --------------
	
	//--------- Channel info start --------------
	
	public static final ChannelDetails CHANNEL_LISDW12_ACCEL_X = new ChannelDetails(
			ObjectClusterSensorName.LIS2DW12_ACC_X,
			ObjectClusterSensorName.LIS2DW12_ACC_X,
			DatabaseChannelHandles.LIS2DW12_ACC_X,
			CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.LSB,
			CHANNEL_UNITS.METER_PER_SECOND_SQUARE,
			Arrays.asList(CHANNEL_TYPE.UNCAL, CHANNEL_TYPE.CAL, CHANNEL_TYPE.DERIVED));
	
	public static final ChannelDetails CHANNEL_LISDW12_ACCEL_Y = new ChannelDetails(
			ObjectClusterSensorName.LIS2DW12_ACC_Y,
			ObjectClusterSensorName.LIS2DW12_ACC_Y,
			DatabaseChannelHandles.LIS2DW12_ACC_Y,
			CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.LSB,
			CHANNEL_UNITS.METER_PER_SECOND_SQUARE,
			Arrays.asList(CHANNEL_TYPE.UNCAL, CHANNEL_TYPE.CAL, CHANNEL_TYPE.DERIVED));
	
	public static final ChannelDetails CHANNEL_LISDW12_ACCEL_Z = new ChannelDetails(
			ObjectClusterSensorName.LIS2DW12_ACC_Z,
			ObjectClusterSensorName.LIS2DW12_ACC_Z,
			DatabaseChannelHandles.LIS2DW12_ACC_Z,
			CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.LSB,
			CHANNEL_UNITS.METER_PER_SECOND_SQUARE,
			Arrays.asList(CHANNEL_TYPE.UNCAL, CHANNEL_TYPE.CAL, CHANNEL_TYPE.DERIVED));
	 
	public static final Map<String, ChannelDetails> CHANNEL_MAP_REF;
	static {
		Map<String, ChannelDetails> aMap = new LinkedHashMap<String, ChannelDetails>();
		aMap.put(SensorLIS2DW12.ObjectClusterSensorName.LIS2DW12_ACC_X, SensorLIS2DW12.CHANNEL_LISDW12_ACCEL_X);
		aMap.put(SensorLIS2DW12.ObjectClusterSensorName.LIS2DW12_ACC_Y, SensorLIS2DW12.CHANNEL_LISDW12_ACCEL_Y);
		aMap.put(SensorLIS2DW12.ObjectClusterSensorName.LIS2DW12_ACC_Z, SensorLIS2DW12.CHANNEL_LISDW12_ACCEL_Z);

		CHANNEL_MAP_REF = Collections.unmodifiableMap(aMap);
	}

	//--------- Channel info end --------------
	
	
	public SensorLIS2DW12(VerisenseDevice verisenseDevice) {
		super(SENSORS.LIS2DW12, verisenseDevice);
		initialise();
	}

	@Override
	public void generateSensorMap() {
		super.createLocalSensorMapWithCustomParser(SENSOR_MAP_REF, CHANNEL_MAP_REF);
//		super.createLocalSensorMap(mSensorMapRef, mChannelMapRef);
	}

	@Override
	public void generateConfigOptionsMap() {
		mConfigOptionsMap.clear();
		addConfigOption(CONFIG_OPTION_ACCEL_RANGE);
		addConfigOption(CONFIG_OPTION_ACCEL_LP_MODE);
		addConfigOption(CONFIG_OPTION_ACCEL_MODE);
		addConfigOption(CONFIG_OPTION_ACCEL_RATE_HP);
		addConfigOption(CONFIG_OPTION_ACCEL_RATE_LP);
	}

	@Override
	public void generateSensorGroupMapping() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ObjectCluster processDataCustom(SensorDetails sensorDetails, byte[] rawData, COMMUNICATION_TYPE commType, ObjectCluster objectCluster, boolean isTimeSyncEnabled, long pcTimestamp) {

		// ADC values
		objectCluster = sensorDetails.processDataCommon(rawData, commType, objectCluster, isTimeSyncEnabled, pcTimestamp);

		double[] unCalibratedAccel = new double[3];
		unCalibratedAccel[0] = objectCluster.getFormatClusterValue(SensorLIS2DW12.CHANNEL_LISDW12_ACCEL_X, CHANNEL_TYPE.UNCAL);
		unCalibratedAccel[1] = objectCluster.getFormatClusterValue(SensorLIS2DW12.CHANNEL_LISDW12_ACCEL_Y, CHANNEL_TYPE.UNCAL);
		unCalibratedAccel[2] = objectCluster.getFormatClusterValue(SensorLIS2DW12.CHANNEL_LISDW12_ACCEL_Z, CHANNEL_TYPE.UNCAL);
		if(Double.isFinite(unCalibratedAccel[0]) && Double.isFinite(unCalibratedAccel[1]) && Double.isFinite(unCalibratedAccel[2])) {
			//Add default calibrated data to Object cluster
			double[] defaultCalAccel = UtilCalibration.calibrateInertialSensorData(unCalibratedAccel, mCurrentCalibDetailsAccel.getDefaultMatrixMultipliedInverseAMSM(), mCurrentCalibDetailsAccel.getDefaultOffsetVector());
			objectCluster.addCalData(CHANNEL_LISDW12_ACCEL_X, defaultCalAccel[0], objectCluster.getIndexKeeper()-3);
			objectCluster.addCalData(CHANNEL_LISDW12_ACCEL_Y, defaultCalAccel[1], objectCluster.getIndexKeeper()-2);
			objectCluster.addCalData(CHANNEL_LISDW12_ACCEL_Z, defaultCalAccel[2], objectCluster.getIndexKeeper()-1);

			//Add auto-calibrated data to Object cluster - if available
			boolean isCurrentValuesSet = mCurrentCalibDetailsAccel.isCurrentValuesSet();
			if(isCurrentValuesSet) {
				double[] autoCalAccel = UtilCalibration.calibrateImuData(defaultCalAccel, mCurrentCalibDetailsAccel.getCurrentSensitivityMatrix(), mCurrentCalibDetailsAccel.getCurrentOffsetVector());
				objectCluster.addData(CHANNEL_LISDW12_ACCEL_X.mObjectClusterName, CHANNEL_TYPE.DERIVED, CHANNEL_LISDW12_ACCEL_X.mDefaultCalUnits, autoCalAccel[0], objectCluster.getIndexKeeper()-3, false);
				objectCluster.addData(CHANNEL_LISDW12_ACCEL_Y.mObjectClusterName, CHANNEL_TYPE.DERIVED, CHANNEL_LISDW12_ACCEL_Y.mDefaultCalUnits, autoCalAccel[1], objectCluster.getIndexKeeper()-2, false);
				objectCluster.addData(CHANNEL_LISDW12_ACCEL_Z.mObjectClusterName, CHANNEL_TYPE.DERIVED, CHANNEL_LISDW12_ACCEL_Z.mDefaultCalUnits, autoCalAccel[2], objectCluster.getIndexKeeper()-1, false);
			}
		}

		return objectCluster;
	}

	@Override
	public void checkShimmerConfigBeforeConfiguring() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void configBytesGenerate(ShimmerDevice shimmerDevice, byte[] configBytes, COMMUNICATION_TYPE commType) {
		if(isSensorEnabled(Configuration.Verisense.SENSOR_ID.LIS2DW12_ACCEL)) {
			ConfigByteLayoutLis2dw12 configByteLayoutLis2dw12 = new ConfigByteLayoutLis2dw12(commType);
			
			configBytes[configByteLayoutLis2dw12.idxFsAccel1] |= (getAccelRangeConfigValue()&0x03)<<configByteLayoutLis2dw12.bitShiftFsAccel1;
			
			configBytes[configByteLayoutLis2dw12.idxAccel1Cfg0] |= (getAccelRateConfigValue()&0x0F)<<4;
			configBytes[configByteLayoutLis2dw12.idxAccel1Cfg0] |= (getAccelModeConfigValue()&0x03)<<2;
			configBytes[configByteLayoutLis2dw12.idxAccel1Cfg0] |= (getAccelLpModeConfigValue()&0x03)<<0;
		}
	}

	@Override
	public void configBytesParse(ShimmerDevice shimmerDevice, byte[] configBytes, COMMUNICATION_TYPE commType) {
		if(isSensorEnabled(Configuration.Verisense.SENSOR_ID.LIS2DW12_ACCEL)) {
			ConfigByteLayoutLis2dw12 configByteLayoutLis2dw12 = new ConfigByteLayoutLis2dw12(commType);
			
			setAccelRangeConfigValue((configBytes[configByteLayoutLis2dw12.idxFsAccel1]>>configByteLayoutLis2dw12.bitShiftFsAccel1)&0x03);
			
			byte accel1Cfg0 = configBytes[configByteLayoutLis2dw12.idxAccel1Cfg0];
			setAccelModeConfigValue((accel1Cfg0>>2)&0x03);
			setAccelLpModeConfigValue((accel1Cfg0>>0)&0x03);
			//Need to parse rate after mode
			setAccelRateConfigValue((accel1Cfg0>>4)&0x0F);
		}
	}

	@Override
	public Object setConfigValueUsingConfigLabel(Integer sensorId, String configLabel, Object valueToSet) {
		Object returnValue = null;
		
		switch(configLabel){
			case(GuiLabelConfig.LIS2DW12_RATE):
				setAccelRateConfigValue((int)valueToSet);
				break;
			case(GuiLabelConfig.LIS2DW12_RANGE):
				setAccelRangeConfigValue((int)valueToSet);
				break;
			case(GuiLabelConfig.LIS2DW12_MODE):
				setAccelModeConfigValue((int)valueToSet);
				break;
			case(GuiLabelConfig.LIS2DW12_LP_MODE):
				setAccelLpModeConfigValue((int)valueToSet);
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
			case(GuiLabelConfig.LIS2DW12_RATE):
				returnValue = getAccelRateConfigValue();
				break;
			case(GuiLabelConfig.LIS2DW12_RANGE):
				returnValue = getAccelRangeConfigValue();
				break;
			case(GuiLabelConfig.LIS2DW12_MODE):
				returnValue = getAccelModeConfigValue();
				break;
			case(GuiLabelConfig.LIS2DW12_LP_MODE):
				returnValue = getAccelLpModeConfigValue();
				break;
			case(GuiLabelConfigCommon.CALIBRATION_CURRENT_PER_SENSOR):
				if(sensorId==Configuration.Verisense.SENSOR_ID.LIS2DW12_ACCEL) {
					returnValue = mCurrentCalibDetailsAccel;
				}
				break;
			case(GuiLabelConfigCommon.RATE):
				returnValue = getAccelRateFreq();
				break;
			default:
				returnValue = super.getConfigValueUsingConfigLabelCommon(sensorId, configLabel);
				break;
		}
		return returnValue;
	}

	@Override
	public void setSensorSamplingRate(double samplingRateHz) {
		int accelRate = 0; // Power down
		
		int accelMode = getAccelModeConfigValue();
		
		if (samplingRateHz==0){
			accelRate = 0;
		} else if (samplingRateHz<=1.6 && accelMode==0){ // LP mode
			accelRate = 1; // 1.6Hz
		} else if (samplingRateHz<=12.5){
			accelRate = 2; // 12.5Hz
		} else if (samplingRateHz<=25){
			accelRate = 3; // 25Hz
		} else if (samplingRateHz<=50){
			accelRate = 4; // 50Hz
		} else if (samplingRateHz<=100){
			accelRate = 5; // 100Hz
		} else if (samplingRateHz<=200 || accelMode==0){ // LP mode cut-off
			accelRate = 6; // 200Hz
		} else if (samplingRateHz<=400){ //HP mode
			accelRate = 7; // 400Hz
		} else if (samplingRateHz<=800){ //HP mode
			accelRate = 8; // 800Hz
		} else { //if (freq<=1600){ //HP mode
			accelRate = 9; // 1600Hz
		}
		setAccelRateConfigValue(accelRate);
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
		setCalibrationMapPerSensor(Configuration.Verisense.SENSOR_ID.LIS2DW12_ACCEL, calibMapAccel);

		updateCurrentAccelCalibInUse();
	}

	public void updateCurrentAccelCalibInUse(){
		mCurrentCalibDetailsAccel = getCurrentCalibDetailsIfKinematic(Configuration.Verisense.SENSOR_ID.LIS2DW12_ACCEL, getAccelRangeConfigValue());
	}

	public int getAccelRangeConfigValue() {
		return range;
	}

	public void setAccelRangeConfigValue(int valueToSet){
		if(ArrayUtils.contains(LIS2DW12_ACCEL_RANGE_CONFIG_VALUES, valueToSet)){
			range = valueToSet;
		}
		updateCurrentAccelCalibInUse();
	}

	public int getAccelRateConfigValue() {
		return rate;
	}

	public double getAccelRateFreq() {
		ConfigOptionDetailsSensor configOptionAccelRate = CONFIG_OPTION_ACCEL_RATE_HP; 
		if(mode==0) {
			configOptionAccelRate = CONFIG_OPTION_ACCEL_RATE_LP;
		}

		String accelRateString = configOptionAccelRate.getConfigStringFromConfigValue(getAccelRateConfigValue());
		
		double accelRate = 0;
		if(accelRateString.contains(CHANNEL_UNITS.FREQUENCY)){
			accelRate = Double.parseDouble(accelRateString.replace(CHANNEL_UNITS.FREQUENCY, ""));
		}
		
		return accelRate;
	}

	public void setAccelRateConfigValue(int valueToSet) {
		Integer[] configValueToCheck = LIS2DW12_ACCEL_RATE_HP_CONFIG_VALUES; 
		if(mode==0) {
			configValueToCheck = LIS2DW12_ACCEL_RATE_LP_CONFIG_VALUES;
		}
		if(ArrayUtils.contains(configValueToCheck, valueToSet)){
			rate = valueToSet;
		}
	}

	public int getAccelModeConfigValue() {
		return mode;
	}

	public void setAccelModeConfigValue(int valueToSet) {
		if(ArrayUtils.contains(LIS2DW12_MODE_CONFIG_VALUES, valueToSet)){
			mode = valueToSet;
		}
	}

	public int getAccelLpModeConfigValue() {
		return lpMode;
	}

	public void setAccelLpModeConfigValue(int valueToSet) {
		if(ArrayUtils.contains(LIS2DW12_LP_MODE_CONFIG_VALUES, valueToSet)){
			lpMode = valueToSet;
		}
	}

	public static double calibrateTemperature(long temperatureUncal) {
		double temp = ((temperatureUncal>>4)/16.0) + 25.0;
		return temp;
	}

	public String getAccelLpModeString() {
		if(mode==0) {
			String accelLpModeStr = SensorLIS2DW12.CONFIG_OPTION_ACCEL_LP_MODE.getConfigStringFromConfigValue(getAccelLpModeConfigValue());
			return accelLpModeStr;
		} else {
			return UtilVerisenseDriver.FEATURE_NOT_AVAILABLE;
		}
	}

	public String getAccelModeString() {
		return SensorLIS2DW12.CONFIG_OPTION_ACCEL_MODE.getConfigStringFromConfigValue(getAccelModeConfigValue());
	}

	public String getAccelRangeString() {
		String accel1Range = SensorLIS2DW12.CONFIG_OPTION_ACCEL_RANGE.getConfigStringFromConfigValue(getAccelRangeConfigValue());
		accel1Range = accel1Range.replaceAll(UtilShimmer.UNICODE_PLUS_MINUS, "+-");
		if(mShimmerDevice instanceof VerisenseDevice && !((VerisenseDevice)mShimmerDevice).isCsvHeaderDesignAzMarkingPoint()) {
			accel1Range = accel1Range.replaceAll(CHANNEL_UNITS.GRAVITY, (" " + CHANNEL_UNITS.GRAVITY));
		}
		return accel1Range;
	}

	public String getAccelModeMergedString() {
		if(mode==LIS2DW12_MODE_CONFIG_VALUES[1]) {
			// High-performance Mode
			return LIS2DW12_MODE_MERGED[0];
		} else {
			// Low-Power Mode X
			return LIS2DW12_MODE_MERGED[lpMode+1];
		}
	}

	public String getAccelResolutionString() {
		if(mode==LIS2DW12_MODE_CONFIG_VALUES[0] && lpMode==LIS2DW12_LP_MODE_CONFIG_VALUES[0]) {
			// 12-bit
			return LIS2DW12_RESOLUTION[0];
		} else {
			// 14-bit
			return LIS2DW12_RESOLUTION[1];
		}
	}
	
	private class ConfigByteLayoutLis2dw12 {
		public int idxAccel1Cfg0 = 0, idxFsAccel1 = 0, bitShiftFsAccel1 = 0;
		
		public ConfigByteLayoutLis2dw12(COMMUNICATION_TYPE commType) {
			if(commType==COMMUNICATION_TYPE.SD) {
				idxFsAccel1 = PAYLOAD_CONFIG_BYTE_INDEX.PAYLOAD_CONFIG0;
				bitShiftFsAccel1 = 2;
				idxAccel1Cfg0 = PAYLOAD_CONFIG_BYTE_INDEX.PAYLOAD_CONFIG1;
			} else {
				idxFsAccel1 = OP_CONFIG_BYTE_INDEX.ACCEL1_CFG_1;
				idxAccel1Cfg0 = OP_CONFIG_BYTE_INDEX.ACCEL1_CFG_0;
				bitShiftFsAccel1 = 4;
			}
		}
	}

}