package com.shimmerresearch.sensors.lis3mdl;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.shimmerresearch.driver.Configuration.CHANNEL_UNITS;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driver.Configuration.Shimmer3.CompatibilityInfoForMaps;
import com.shimmerresearch.driver.calibration.CalibDetails;
import com.shimmerresearch.driver.calibration.CalibDetailsKinematic;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driver.ShimmerObject;
import com.shimmerresearch.driverUtilities.ChannelDetails;
import com.shimmerresearch.driverUtilities.ConfigOptionDetailsSensor;
import com.shimmerresearch.driverUtilities.SensorDetails;
import com.shimmerresearch.driverUtilities.SensorDetailsRef;
import com.shimmerresearch.driverUtilities.SensorGroupingDetails;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_ENDIAN;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_TYPE;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_TYPE;
import com.shimmerresearch.sensors.AbstractSensor;
import com.shimmerresearch.sensors.ActionSetting;
import com.shimmerresearch.sensors.AbstractSensor.SENSORS;

public class SensorLIS3MDL extends AbstractSensor{
	
	protected int mSensorIdMag = -1;
	protected int mMagRange = 0;
	public CalibDetailsKinematic mCurrentCalibDetailsMag = null;
	
	//--------- Sensor specific variables start --------------	
	
	public static final double[][] DefaultAlignmentLIS3MDL = {{1,0,0},{0,-1,0},{0,0,-1}};	

	// ----------   Mag start ---------------
	public static final double[][] DefaultAlignmentMatrixMagShimmer3 = DefaultAlignmentLIS3MDL; 				
	public static final double[][] DefaultOffsetVectorMagShimmer3 = {{0},{0},{0}};	
	// Manufacturer stated: X any Y any Z axis @ 6842 LSB/gauss
	public static final double[][] DefaultSensitivityMatrixMag4GaShimmer3 = {{6842,0,0},{0,6842,0},{0,0,6842}};
	// Manufacturer stated: X any Y any Z axis @ 3421  LSB/gauss
	public static final double[][] DefaultSensitivityMatrixMag8GaShimmer3 = {{3421 ,0,0},{0,3421 ,0},{0,0,3421 }};
	// Manufacturer stated: X any Y any Z axis @ 2281  LSB/gauss
	public static final double[][] DefaultSensitivityMatrixMag12GaShimmer3 = {{2281 ,0,0},{0,2281 ,0},{0,0,2281 }};
	// Manufacturer stated: X any Y any Z axis @ 1711  LSB/gauss
	public static final double[][] DefaultSensitivityMatrixMag16GaShimmer3 = {{1711 ,0,0},{0,1711 ,0},{0,0,1711 }};

	private CalibDetailsKinematic calibDetailsMag4 = new CalibDetailsKinematic(
			ListofLIS3MDLMagRangeConfigValues[0],
			ListofLIS3MDLMagRange[0],
			DefaultAlignmentMatrixMagShimmer3,
			DefaultSensitivityMatrixMag4GaShimmer3,
			DefaultOffsetVectorMagShimmer3);
	private CalibDetailsKinematic calibDetailsMag8 = new CalibDetailsKinematic(
			ListofLIS3MDLMagRangeConfigValues[1],
			ListofLIS3MDLMagRange[1],
			DefaultAlignmentMatrixMagShimmer3, 
			DefaultSensitivityMatrixMag8GaShimmer3,
			DefaultOffsetVectorMagShimmer3);
	private CalibDetailsKinematic calibDetailsMag12 = new CalibDetailsKinematic(
			ListofLIS3MDLMagRangeConfigValues[2], 
			ListofLIS3MDLMagRange[2],
			DefaultAlignmentMatrixMagShimmer3,
			DefaultSensitivityMatrixMag12GaShimmer3, 
			DefaultOffsetVectorMagShimmer3);
	private CalibDetailsKinematic calibDetailsMag16 = new CalibDetailsKinematic(
			ListofLIS3MDLMagRangeConfigValues[3],
			ListofLIS3MDLMagRange[3],
			DefaultAlignmentMatrixMagShimmer3,
			DefaultSensitivityMatrixMag16GaShimmer3,
			DefaultOffsetVectorMagShimmer3);

	// ----------   Mag end ---------------
	
	public static class DatabaseChannelHandles{
		public static final String MAG_X = "LIS3MDL_MAG_X";
		public static final String MAG_Y = "LIS3MDL_MAG_Y";
		public static final String MAG_Z = "LIS3MDL_MAG_Z";
	}
	
	public static final class DatabaseConfigHandle{
		public static final String MAG_RANGE = "LIS3MDL_Mag_Range";
		public static final String MAG_RATE = "LIS3MDL_Mag_Rate";
//		public static final String MAG = "LIS3MDL_Mag";
		
		public static final String MAG_CALIB_TIME = "LIS3MDL_Mag_Calib_Time";
		public static final String MAG_OFFSET_X = "LIS3MDL_Mag_Offset_X";
		public static final String MAG_OFFSET_Y = "LIS3MDL_Mag_Offset_Y";
		public static final String MAG_OFFSET_Z = "LIS3MDL_Mag_Offset_Z";
		public static final String MAG_GAIN_X = "LIS3MDL_Mag_Gain_X";
		public static final String MAG_GAIN_Y = "LIS3MDL_Mag_Gain_Y";
		public static final String MAG_GAIN_Z = "LIS3MDL_Mag_Gain_Z";
		public static final String MAG_ALIGN_XX = "LIS3MDL_Mag_Align_XX";
		public static final String MAG_ALIGN_XY = "LIS3MDL_Mag_Align_XY";
		public static final String MAG_ALIGN_XZ = "LIS3MDL_Mag_Align_XZ";
		public static final String MAG_ALIGN_YX = "LIS3MDL_Mag_Align_YX";
		public static final String MAG_ALIGN_YY = "LIS3MDL_Mag_Align_YY";
		public static final String MAG_ALIGN_YZ = "LIS3MDL_Mag_Align_YZ";
		public static final String MAG_ALIGN_ZX = "LIS3MDL_Mag_Align_ZX";
		public static final String MAG_ALIGN_ZY = "LIS3MDL_Mag_Align_ZY";
		public static final String MAG_ALIGN_ZZ = "LIS3MDL_Mag_Align_ZZ";

		public static final List<String> LIST_OF_CALIB_HANDLES_MAG = Arrays.asList(
				DatabaseConfigHandle.MAG_OFFSET_X, DatabaseConfigHandle.MAG_OFFSET_Y, DatabaseConfigHandle.MAG_OFFSET_Z,
				DatabaseConfigHandle.MAG_GAIN_X, DatabaseConfigHandle.MAG_GAIN_Y, DatabaseConfigHandle.MAG_GAIN_Z,
				DatabaseConfigHandle.MAG_ALIGN_XX, DatabaseConfigHandle.MAG_ALIGN_XY, DatabaseConfigHandle.MAG_ALIGN_XZ,
				DatabaseConfigHandle.MAG_ALIGN_YX, DatabaseConfigHandle.MAG_ALIGN_YY, DatabaseConfigHandle.MAG_ALIGN_YZ,
				DatabaseConfigHandle.MAG_ALIGN_ZX, DatabaseConfigHandle.MAG_ALIGN_ZY, DatabaseConfigHandle.MAG_ALIGN_ZZ);
	}
	
	//--------- Sensor specific variables end --------------	
	
	//--------- Configuration options start --------------
	
	public static final String[] ListofLIS3MDLMagRate={"0.75Hz","1.5Hz","3.0Hz","7.5Hz","15.0Hz","30.0Hz","75.0Hz","220.0Hz"};
	public static final Integer[] ListofLIS3MDLMagRateConfigValues={0,1,2,3,4,5,6,7}; //not yet implemented

	public static final String[] ListofLIS3MDLMagRange={"+/- 4Ga","+/- 8Ga","+/- 12Ga","+/- 16Ga"}; 
	public static final Integer[] ListofLIS3MDLMagRangeConfigValues={0,1,2,3};

	
	public static final ConfigOptionDetailsSensor configOptionMagRange = new ConfigOptionDetailsSensor(
			SensorLIS3MDL.GuiLabelConfig.LIS3MDL_MAG_RANGE,
			SensorLIS3MDL.DatabaseConfigHandle.MAG_RANGE,
			ListofLIS3MDLMagRange, 
			ListofLIS3MDLMagRangeConfigValues, 
			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW);
	
	public static final ConfigOptionDetailsSensor configOptionMagRate = new ConfigOptionDetailsSensor(
			SensorLIS3MDL.GuiLabelConfig.LIS3MDL_MAG_RATE,
			SensorLIS3MDL.DatabaseConfigHandle.MAG_RATE,
			ListofLIS3MDLMagRate, 
			ListofLIS3MDLMagRateConfigValues, 
			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW);

	//--------- Configuration options end --------------
	
	//--------- Sensor info start --------------
	
	public static final SensorDetailsRef sensorLIS3MDLMag = new SensorDetailsRef(
			0x20, //== Configuration.Shimmer3.SensorBitmap.SENSOR_MAG will be: SensorBitmap.SENSOR_MAG, 
			0x20, //== Configuration.Shimmer3.SensorBitmap.SENSOR_MAG will be: SensorBitmap.SENSOR_MAG, 
			GuiLabelSensors.MAG,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW,
			Arrays.asList(GuiLabelConfig.LIS3MDL_MAG_RANGE,
					GuiLabelConfig.LIS3MDL_MAG_RATE),
			//MAG channel parsing order is XZY instead of XYZ but it would be better to represent it on the GUI in XYZ
			Arrays.asList(ObjectClusterSensorName.MAG_X,
					ObjectClusterSensorName.MAG_Y,
					ObjectClusterSensorName.MAG_Z));
	
	public static final Map<Integer, SensorDetailsRef> mSensorMapRef;
    static {
        Map<Integer, SensorDetailsRef> aMap = new LinkedHashMap<Integer, SensorDetailsRef>();
        aMap.put(Configuration.Shimmer3.SENSOR_ID.SHIMMER_LIS3MDL_MAG, SensorLIS3MDL.sensorLIS3MDLMag);	
		mSensorMapRef = Collections.unmodifiableMap(aMap);
    }
	
	//--------- Sensor info end --------------
    
  //--------- Channel info start --------------
    
    public static final ChannelDetails channelLIS3MDLMagX = new ChannelDetails(
			ObjectClusterSensorName.MAG_X,
			ObjectClusterSensorName.MAG_X,
			DatabaseChannelHandles.MAG_X,
			CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.MSB,
			CHANNEL_UNITS.LOCAL_FLUX,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL),
			0x07);
	
	public static final ChannelDetails channelLIS3MDLMagY = new ChannelDetails(
			ObjectClusterSensorName.MAG_Y,
			ObjectClusterSensorName.MAG_Y,
			DatabaseChannelHandles.MAG_Y,
			CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.MSB,
			CHANNEL_UNITS.LOCAL_FLUX,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL),
			0x08);
	
	public static final ChannelDetails channelLIS3MDLMagZ = new ChannelDetails(
			ObjectClusterSensorName.MAG_Z,
			ObjectClusterSensorName.MAG_Z,
			DatabaseChannelHandles.MAG_Z,
			CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.MSB,
			CHANNEL_UNITS.LOCAL_FLUX,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL),
			0x09);
    
    public static final Map<String, ChannelDetails> mChannelMapRef;
    static {
        Map<String, ChannelDetails> aMap = new LinkedHashMap<String, ChannelDetails>();
        aMap.put(SensorLIS3MDL.ObjectClusterSensorName.MAG_X, SensorLIS3MDL.channelLIS3MDLMagX);
        aMap.put(SensorLIS3MDL.ObjectClusterSensorName.MAG_Z, SensorLIS3MDL.channelLIS3MDLMagZ);
        aMap.put(SensorLIS3MDL.ObjectClusterSensorName.MAG_Y, SensorLIS3MDL.channelLIS3MDLMagY);
		mChannelMapRef = Collections.unmodifiableMap(aMap);
    }
    
    //--------- Channel info end --------------
    
    public static final SensorGroupingDetails sensorGroupLisMag = new SensorGroupingDetails(
			LABEL_SENSOR_TILE.MAG,
			Arrays.asList(Configuration.Shimmer3.SENSOR_ID.SHIMMER_LIS3MDL_MAG),
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW);
    
  //--------- Constructors for this class start --------------
    
	public SensorLIS3MDL() {
		super(SENSORS.LIS3MDL);
		initialise();
	}
	
	public SensorLIS3MDL(ShimmerObject obj) {
		super(SENSORS.LIS3MDL, obj);
		initialise();
	}
	
	public SensorLIS3MDL(ShimmerDevice shimmerDevice) {
		super(SENSORS.LIS3MDL, shimmerDevice);
		initialise();
	}
    
  //--------- Constructors for this class end --------------
	
	//--------- Optional methods to override in Sensor Class start --------
	@Override
	public void initialise() {
		mSensorIdMag = Configuration.Shimmer3.SENSOR_ID.SHIMMER_LIS3MDL_MAG;
		super.initialise();
		
		mMagRange = ListofLIS3MDLMagRangeConfigValues[0];
		
		updateCurrentMagCalibInUse();
	}

	@Override
	public void generateCalibMap() {
		super.generateCalibMap();

		TreeMap<Integer, CalibDetails> calibMapMag = new TreeMap<Integer, CalibDetails>();
		calibMapMag.put(calibDetailsMag4.mRangeValue, calibDetailsMag4);
		calibMapMag.put(calibDetailsMag8.mRangeValue, calibDetailsMag8);
		calibMapMag.put(calibDetailsMag12.mRangeValue, calibDetailsMag12);
		calibMapMag.put(calibDetailsMag16.mRangeValue, calibDetailsMag16);
		setCalibrationMapPerSensor(Configuration.Shimmer3.SENSOR_ID.SHIMMER_LIS3MDL_MAG, calibMapMag);
		
		updateCurrentMagCalibInUse();
	}
	
	//--------- Optional methods to override in Sensor Class end --------
	
	public class GuiLabelSensors{
		public static final String MAG = "Magnetometer"; 
	}
	
	public class LABEL_SENSOR_TILE{
		public static final String MAG = GuiLabelSensors.MAG;
	}
	
	public class GuiLabelConfig{
		public static final String LIS3MDL_MAG_RANGE = "Mag Range";
		public static final String LIS3MDL_MAG_RATE = "Mag Rate";
	}
	   
	public static class ObjectClusterSensorName{
		public static  String MAG_X = "Mag_X";
		public static  String MAG_Y = "Mag_Y";
		public static  String MAG_Z = "Mag_Z";		
	}
    
	@Override
	public void generateSensorMap() {
		super.createLocalSensorMapWithCustomParser(mSensorMapRef, mChannelMapRef);
	}

	@Override
	public void generateConfigOptionsMap() {
		addConfigOption(configOptionMagRange);
		addConfigOption(configOptionMagRate);
	}

	@Override
	public void generateSensorGroupMapping() {
		mSensorGroupingMap = new LinkedHashMap<Integer, SensorGroupingDetails>();
		if(mShimmerVerObject.isShimmerGen3() || mShimmerVerObject.isShimmerGen4()){
			mSensorGroupingMap.put(Configuration.Shimmer3.LABEL_SENSOR_TILE.MAG.ordinal(), sensorGroupLisMag);
		}
		super.updateSensorGroupingMap();	
	}	

	@Override
	public ObjectCluster processDataCustom(SensorDetails sensorDetails, byte[] rawData, COMMUNICATION_TYPE commType, ObjectCluster objectCluster, boolean isTimeSyncEnabled, double pcTimestampMs) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isUsingDefaultMagParam(){
		return getCurrentCalibDetailsMag().isUsingDefaultParameters(); 
	}
	
	public CalibDetailsKinematic getCurrentCalibDetailsMag(){
//		return getCurrentCalibDetails(mSensorIdMag, getMagRange());
		if(mCurrentCalibDetailsMag==null){
			updateCurrentMagCalibInUse();
		}
		return mCurrentCalibDetailsMag;
	}
	
	public void updateCurrentMagCalibInUse(){
//		mCurrentCalibDetailsMag = getCurrentCalibDetailsMag();
		mCurrentCalibDetailsMag = getCurrentCalibDetailsIfKinematic(mSensorIdMag, getMagRange());
	}
	
	public int getMagRange() {
		return mMagRange;
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

	@Override
	public LinkedHashMap<String, Object> generateConfigMap() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void parseConfigMap(LinkedHashMap<String, Object> mapOfConfigPerShimmer) {
		// TODO Auto-generated method stub
		
	}

}
