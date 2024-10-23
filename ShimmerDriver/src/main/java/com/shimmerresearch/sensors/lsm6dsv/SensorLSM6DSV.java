package com.shimmerresearch.sensors.lsm6dsv;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.shimmerresearch.bluetooth.BtCommandDetails;
import com.shimmerresearch.driver.Configuration.CHANNEL_UNITS;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driver.Configuration.Shimmer3.CompatibilityInfoForMaps;
import com.shimmerresearch.driver.calibration.CalibDetails;
import com.shimmerresearch.driver.calibration.CalibDetailsKinematic;
import com.shimmerresearch.driver.calibration.CalibDetails.CALIB_READ_SOURCE;
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
import com.shimmerresearch.sensors.ActionSetting;

public class SensorLSM6DSV extends AbstractSensor{
	
	
	//--------- Sensor specific variables start --------------
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

	public static class DatabaseChannelHandles{
		public static final String LN_ACC_X = "LSM6DSV_X";
		public static final String LN_ACC_Y = "LSM6DSV_X";
		public static final String LN_ACC_Z = "LSM6DSV_X";
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
	}
	
	public static class ObjectClusterSensorName{
		public static  String ACCEL_LN_X = "Accel_LN_X";
		public static  String ACCEL_LN_Y = "Accel_LN_Y";
		public static  String ACCEL_LN_Z = "Accel_LN_Z";
	}	
	
	public CalibDetailsKinematic mCurrentCalibDetailsAccelLn = null;
	
	public class GuiLabelSensors{
		public static final String ACCEL_LN = "Low-Noise Accelerometer";
	}
	
	public class LABEL_SENSOR_TILE{
		public static final String LOW_NOISE_ACCEL = GuiLabelSensors.ACCEL_LN;
	}
	//--------- Sensor specific variables end --------------
	
	
	
	//--------- Configuration options start --------------
	public static final Integer[] ListofLSM6DSVAccelRangeConfigValues={0,1,2,3};  

	public static final String[] ListofLSM6DSVGyroRate={"Power-down","1.875Hz","7.5Hz","12.0Hz","30.0Hz","60.0Hz","120.0Hz","240.0Hz","480.0Hz","960.0Hz","1920.0Hz","3840.0Hz","7680.0Hz"};
	public static final Integer[] ListofLSM6DSVGyroRateConfigValues={0,1,2,3,4,5,6,7,8,9,10,11,12,13};

//	public static final String[] ListofLSM303AHAccelRateLpm={"Power-down","1.0Hz","12.5Hz","25.0Hz","50.0Hz","100.0Hz","200.0Hz","400.0Hz","800.0Hz"};
//	public static final Integer[] ListofLSM303AHAccelRateLpmConfigValues={0,8,9,10,11,12,13,14,15};
//
//	public static final ConfigOptionDetailsSensor configOptionAccelRange = new ConfigOptionDetailsSensor(
//			SensorLSM303.GuiLabelConfig.LSM303_ACCEL_RANGE,
//			SensorLSM303AH.DatabaseConfigHandle.WR_ACC_RANGE,
//			ListofLSM303AccelRange, 
//			ListofLSM303AccelRangeConfigValues, 
//			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX,
//			CompatibilityInfoForMaps.listOfCompatibleVersionInfoLSM303AH);
//
//	public static final ConfigOptionDetailsSensor configOptionAccelRate = new ConfigOptionDetailsSensor(
//			SensorLSM303.GuiLabelConfig.LSM303_ACCEL_RATE,
//			SensorLSM303AH.DatabaseConfigHandle.WR_ACC_RATE,
//			ListofLSM303AHAccelRateHr, 
//			ListofLSM303AHAccelRateHrConfigValues, 
//			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX,
//			CompatibilityInfoForMaps.listOfCompatibleVersionInfoLSM303AH,
//			Arrays.asList(
//				new ConfigOptionObject(ConfigOptionDetailsSensor.VALUE_INDEXES.LSM303_ACCEL_RATE.IS_LPM, 
//						ListofLSM303AHAccelRateLpm, 
//						ListofLSM303AHAccelRateLpmConfigValues)));
//
//	public static final String[] ListofLSM303AHMagRate={"10.0Hz","20.0Hz","50.0Hz","100.0Hz"};
//	public static final Integer[] ListofLSM303AHMagRateConfigValues={0,1,2,3};
//
//	public static final String[] ListofLSM303AHMagRange={"+/- 49.152Ga"}; 
//	public static final Integer[] ListofLSM303AHMagRangeConfigValues={0};  
//
//	public static final ConfigOptionDetailsSensor configOptionMagRange = new ConfigOptionDetailsSensor(
//			SensorLSM303.GuiLabelConfig.LSM303_MAG_RANGE,
//			SensorLSM303AH.DatabaseConfigHandle.MAG_RANGE,
//			ListofLSM303AHMagRange, 
//			ListofLSM303AHMagRangeConfigValues, 
//			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX,
//			CompatibilityInfoForMaps.listOfCompatibleVersionInfoLSM303AH);
//	
//	public static final ConfigOptionDetailsSensor configOptionMagRate = new ConfigOptionDetailsSensor(
//			SensorLSM303.GuiLabelConfig.LSM303_MAG_RATE,
//			SensorLSM303AH.DatabaseConfigHandle.MAG_RATE,
//			ListofLSM303AHMagRate, 
//			ListofLSM303AHMagRateConfigValues, 
//			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX,
//			CompatibilityInfoForMaps.listOfCompatibleVersionInfoLSM303AH);
//
//	public static final ConfigOptionDetailsSensor configOptionAccelLpm = new ConfigOptionDetailsSensor(
//			SensorLSM303.GuiLabelConfig.LSM303_ACCEL_LPM,
//			SensorLSM303AH.DatabaseConfigHandle.WR_ACC_LPM,
//			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.CHECKBOX);
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
	
	public static final Map<Integer, SensorDetailsRef> mSensorMapRef;
    static {
        Map<Integer, SensorDetailsRef> aMap = new LinkedHashMap<Integer, SensorDetailsRef>();
        aMap.put(Configuration.Shimmer3.SENSOR_ID.SHIMMER_ANALOG_ACCEL, SensorLSM6DSV.sensorLSM6DSV);

		mSensorMapRef = Collections.unmodifiableMap(aMap);
    }
	//--------- Sensor info end --------------
    
    
    
	//--------- Channel info start --------------  // To Be Changed
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
    
    
    public static final Map<String, ChannelDetails> mChannelMapRef;
    static {
        Map<String, ChannelDetails> aMap = new LinkedHashMap<String, ChannelDetails>();
        aMap.put(SensorLSM6DSV.ObjectClusterSensorName.ACCEL_LN_X, SensorLSM6DSV.channelAccelX);
        aMap.put(SensorLSM6DSV.ObjectClusterSensorName.ACCEL_LN_Y, SensorLSM6DSV.channelAccelY);
        aMap.put(SensorLSM6DSV.ObjectClusterSensorName.ACCEL_LN_Z, SensorLSM6DSV.channelAccelZ);
		mChannelMapRef = Collections.unmodifiableMap(aMap);
    }
	//--------- Channel info end --------------
    
    public static final SensorGroupingDetails sensorGroupLnAccelLSM6DSV = new SensorGroupingDetails(
    		SensorLSM6DSV.LABEL_SENSOR_TILE.LOW_NOISE_ACCEL,
			Arrays.asList(Configuration.Shimmer3.SENSOR_ID.SHIMMER_ANALOG_ACCEL),
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoKionixKXTC92050);
	
    
    
	//--------- Bluetooth commands start --------------
	public static final byte SET_LN_ACCEL_CALIBRATION_COMMAND			= (byte) 0x11;
	public static final byte LN_ACCEL_CALIBRATION_RESPONSE       		= (byte) 0x12;
	public static final byte GET_LN_ACCEL_CALIBRATION_COMMAND    		= (byte) 0x13;
	
	public static final byte SET_ALT_ACCEL_RANGE_COMMAND				= (byte) 0x4F;
	public static final byte ALT_ACCEL_RANGE_RESPONSE					= (byte) 0x50;
	public static final byte GET_ALT_ACCEL_RANGE_COMMAND				= (byte) 0x51;

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
		CalibDetails calibPerSensor = getCalibForSensor(Configuration.Shimmer3.SENSOR_ID.SHIMMER_ANALOG_ACCEL, LN_ACCEL_RANGE_VALUE_2G);
		if(calibPerSensor!=null){
			return (CalibDetailsKinematic) calibPerSensor;
		}
		return null;
	}
//	
//	/**
//	 * Converts the Analog Accel calibration variables from Shimmer Object
//	 * into a byte array for sending to the Shimmer.
//	 * 
//	 * @return the bytes array containing the Analog Accel Calibration
//	 */
//	public byte[] generateCalParamByteArrayAccelLn(){
//		return getCurrentCalibDetailsAccelLn().generateCalParamByteArray();
//	}

	//--------- Sensor specific methods end --------------
	
	
	
	// Constructors for Class START ------------------------------------------
//	public SensorLSM6DSV(SENSORS sensorType) {
//		super(sensorType);
//		// TODO Auto-generated constructor stub
//	}
//
//	public SensorLSM6DSV(SENSORS sensorType, ShimmerVerObject svo) {
//		super(sensorType, svo);
//	}
//	
//	public SensorLSM6DSV(SENSORS sensorType, ShimmerDevice shimmerDevice) {
//		super(sensorType, shimmerDevice);
//	}
	
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

	
	
	@Override
	public void generateSensorMap() {
		// TODO Auto-generated method stub
		super.createLocalSensorMapWithCustomParser(mSensorMapRef, mChannelMapRef);
	}
	
	@Override
	public LinkedHashMap<String, Object> generateConfigMap() {
		LinkedHashMap<String, Object> mapOfConfig = new LinkedHashMap<String, Object>();
		super.addCalibDetailsToDbMap(mapOfConfig, 
				getCurrentCalibDetailsAccelLn(), 
				SensorLSM6DSV.DatabaseConfigHandle.LIST_OF_CALIB_HANDLES_LN_ACC,
				SensorLSM6DSV.DatabaseConfigHandle.LN_ACC_CALIB_TIME);
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
	}

	@Override
	public void generateConfigOptionsMap() {
		// TODO Auto-generated method stub

	}

	@Override
	public void generateSensorGroupMapping() {
		mSensorGroupingMap = new LinkedHashMap<Integer, SensorGroupingDetails>();
		mSensorGroupingMap.put(Configuration.Shimmer3.LABEL_SENSOR_TILE.LOW_NOISE_ACCEL.ordinal(), sensorGroupLnAccelLSM6DSV);
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
		super.initialise();
		
		updateCurrentAccelLnCalibInUse();
	}
	
	@Override
	public void generateCalibMap() {
		super.generateCalibMap();
		
		TreeMap<Integer, CalibDetails> calibMapAccelLn = new TreeMap<Integer, CalibDetails>();
		calibMapAccelLn.put(calibDetailsAccelLn2g.mRangeValue, calibDetailsAccelLn2g);
		
		setCalibrationMapPerSensor(Configuration.Shimmer3.SENSOR_ID.SHIMMER_ANALOG_ACCEL, calibMapAccelLn);
		
		updateCurrentAccelLnCalibInUse();
	}
	//--------- Optional methods to override in Sensor Class end --------
	
}