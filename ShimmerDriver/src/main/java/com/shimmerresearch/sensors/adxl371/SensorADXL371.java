package com.shimmerresearch.sensors.adxl371;

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
import com.shimmerresearch.driver.calibration.OldCalDetails;
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

public class SensorADXL371 extends AbstractSensor{

	/**
	 * 
	 */
	private static final long serialVersionUID = -841122434330904985L;
	
	protected int mSensorIdAccel = -1;
	protected int mADXL371AnalogAccelRate = 0;
	public CalibDetailsKinematic mCurrentCalibDetailsAccelHighG = null;
	
	public class GuiLabelSensors{
		public static final String ACCEL_HighG = "High-G Accelerometer"; 
	}

	public class LABEL_SENSOR_TILE{
		public static final String HIGH_G_ACCEL = GuiLabelSensors.ACCEL_HighG;
	}
	
	public class GuiLabelConfig{
		public static final String ADXL371_ACCEL_RATE = "High G Accel Rate";  
		public static final String ADXL371_ACCEL_RANGE = "High G Accel Range"; 

		public static final String ADXL371_ACCEL_DEFAULT_CALIB = "High G Accel Default Calibration";

		//NEW
		public static final String ADXL371_ACCEL_CALIB_PARAM = "High G Accel Calibration Details";
		public static final String ADXL371_ACCEL_VALID_CALIB = "High G Accel Valid Calibration";
	}
	
	public static class ObjectClusterSensorName{
		public static  String ACCEL_HighG_X = "Accel_HighG_X";
		public static  String ACCEL_HighG_Y = "Accel_HighG_Y";
		public static  String ACCEL_HighG_Z= "Accel_HighG_Z";
	}
	
	//--------- Sensor specific variables start --------------	
	
		// ----------   High-g accel start ---------------

		public static final double[][] DefaultAlignmentADXL377 = {{0,1,0},{1,0,0},{0,0,-1}};	

		public static final double[][] DefaultAlignmentMatrixHighGAccelShimmer3 = DefaultAlignmentADXL377;	
		public static final double[][] DefaultOffsetVectorHighGAccelShimmer3 = {{0},{0},{0}};	
		public static final double[][] DefaultSensitivityMatrixHighGAccelShimmer3 = {{16,0,0},{0,16,0},{0,0,16}};

		private CalibDetailsKinematic calibDetailsAccelHighG = new CalibDetailsKinematic(
				0,
				"0",
				DefaultAlignmentMatrixHighGAccelShimmer3, 
				DefaultSensitivityMatrixHighGAccelShimmer3, 
				DefaultOffsetVectorHighGAccelShimmer3);

		// ----------   High-g accel end ---------------
		
		public static class DatabaseChannelHandles{
			public static final String HighG_ACC_X = "ADXL371_ACC_X";
			public static final String HighG_ACC_Y = "ADXL371_ACC_Y";
			public static final String HighG_ACC_Z = "ADXL371_ACC_Z";
		}
		
		public static final class DatabaseConfigHandle{

			public static final String HighG_ACC_RATE = "ADXL371_Acc_Rate";
			public static final String HighG_ACC_RANGE = "ADXL371_Acc_Range";
			
			public static final String HighG_ACC_LPM = "ADXL371_Acc_LPM";
			public static final String HighG_ACC_HRM = "ADXL371_Acc_HRM";
			
			public static final String HighG_ACC_CALIB_TIME = "ADXL371_Acc_Calib_Time";
			public static final String HighG_ACC_OFFSET_X = "ADXL371_Acc_Offset_X";
			public static final String HighG_ACC_OFFSET_Y = "ADXL371_Acc_Offset_Y";
			public static final String HighG_ACC_OFFSET_Z = "ADXL371_Acc_Offset_Z";
			public static final String HighG_ACC_GAIN_X = "ADXL371_Acc_Gain_X";
			public static final String HighG_ACC_GAIN_Y = "ADXL371_Acc_Gain_Y";
			public static final String HighG_ACC_GAIN_Z = "ADXL371_Acc_Gain_Z";
			public static final String HighG_ACC_ALIGN_XX = "ADXL371_Acc_Align_XX";
			public static final String HighG_ACC_ALIGN_XY = "ADXL371_Acc_Align_XY";
			public static final String HighG_ACC_ALIGN_XZ = "ADXL371_Acc_Align_XZ";
			public static final String HighG_ACC_ALIGN_YX = "ADXL371_Acc_Align_YX";
			public static final String HighG_ACC_ALIGN_YY = "ADXL371_Acc_Align_YY";
			public static final String HighG_ACC_ALIGN_YZ = "ADXL371_Acc_Align_YZ";
			public static final String HighG_ACC_ALIGN_ZX = "ADXL371_Acc_Align_ZX";
			public static final String HighG_ACC_ALIGN_ZY = "ADXL371_Acc_Align_ZY";
			public static final String HighG_ACC_ALIGN_ZZ = "ADXL371_Acc_Align_ZZ";

			public static final List<String> LIST_OF_CALIB_HANDLES_HighG_ACCEL = Arrays.asList(
					DatabaseConfigHandle.HighG_ACC_OFFSET_X, DatabaseConfigHandle.HighG_ACC_OFFSET_Y, DatabaseConfigHandle.HighG_ACC_OFFSET_Z,
					DatabaseConfigHandle.HighG_ACC_GAIN_X, DatabaseConfigHandle.HighG_ACC_GAIN_Y, DatabaseConfigHandle.HighG_ACC_GAIN_Z,
					DatabaseConfigHandle.HighG_ACC_ALIGN_XX, DatabaseConfigHandle.HighG_ACC_ALIGN_XY, DatabaseConfigHandle.HighG_ACC_ALIGN_XZ,
					DatabaseConfigHandle.HighG_ACC_ALIGN_YX, DatabaseConfigHandle.HighG_ACC_ALIGN_YY, DatabaseConfigHandle.HighG_ACC_ALIGN_YZ,
					DatabaseConfigHandle.HighG_ACC_ALIGN_ZX, DatabaseConfigHandle.HighG_ACC_ALIGN_ZY, DatabaseConfigHandle.HighG_ACC_ALIGN_ZZ);
		}
		
		//--------- Sensor specific variables end --------------

		//--------- Configuration options start --------------
		
		public static final Integer[] ListofADXL371AccelRangeConfigValues={0};  

		public static final String[] ListofADXL371AccelRate={"Power-down","1.0Hz","10.0Hz","25.0Hz","50.0Hz","100.0Hz","200.0Hz","400.0Hz","1344.0Hz"};
		public static final Integer[] ListofADXL371AccelRateConfigValues={0,1,2,3,4};

		public static final ConfigOptionDetailsSensor configOptionAccelRate = new ConfigOptionDetailsSensor(
				SensorADXL371.GuiLabelConfig.ADXL371_ACCEL_RATE,
				SensorADXL371.DatabaseConfigHandle.HighG_ACC_RATE,
				ListofADXL371AccelRate, 
				ListofADXL371AccelRateConfigValues, 
				ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX,
				CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW);
		
		//--------- Configuration options end --------------
		
		//--------- Sensor info start --------------
		public static final SensorDetailsRef sensorADXL371Accel = new SensorDetailsRef(
				0x10<<8, //== Configuration.Shimmer3.SensorBitmap.SENSOR_D_ACCEL will be: SensorBitmap.SENSOR_D_ACCEL
				0x10<<8, //== Configuration.Shimmer3.SensorBitmap.SENSOR_D_ACCEL will be: SensorBitmap.SENSOR_D_ACCEL
				GuiLabelSensors.ACCEL_HighG,
				CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW,
				Arrays.asList(GuiLabelConfig.ADXL371_ACCEL_RANGE,
					GuiLabelConfig.ADXL371_ACCEL_RATE),
				Arrays.asList(ObjectClusterSensorName.ACCEL_HighG_X,
						ObjectClusterSensorName.ACCEL_HighG_Y,
						ObjectClusterSensorName.ACCEL_HighG_Z));
		
		public static final Map<Integer, SensorDetailsRef> mSensorMapRef;
	    static {
	        Map<Integer, SensorDetailsRef> aMap = new LinkedHashMap<Integer, SensorDetailsRef>();
	        aMap.put(Configuration.Shimmer3.SENSOR_ID.SHIMMER_ADXL371_ACCEL, SensorADXL371.sensorADXL371Accel);  
			mSensorMapRef = Collections.unmodifiableMap(aMap);
	    }
		//--------- Sensor info end --------------
	    
	  //--------- Channel info start --------------
	    public static final ChannelDetails channelADXL371AccelX = new ChannelDetails(
				ObjectClusterSensorName.ACCEL_HighG_X,
				ObjectClusterSensorName.ACCEL_HighG_X,
				DatabaseChannelHandles.HighG_ACC_X,
				CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.LSB,
				CHANNEL_UNITS.METER_PER_SECOND_SQUARE,
				Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL),
				0x04);
	    
	    public static final ChannelDetails channelADXL371AccelY = new ChannelDetails(
				ObjectClusterSensorName.ACCEL_HighG_Y,
				ObjectClusterSensorName.ACCEL_HighG_Y,
				DatabaseChannelHandles.HighG_ACC_Y,
				CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.LSB,
				CHANNEL_UNITS.METER_PER_SECOND_SQUARE,
				Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL),
				0x05);
	    
	    public static final ChannelDetails channelADXL371AccelZ = new ChannelDetails(
				ObjectClusterSensorName.ACCEL_HighG_Z,
				ObjectClusterSensorName.ACCEL_HighG_Z,
				DatabaseChannelHandles.HighG_ACC_Z,
				CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.LSB,
				CHANNEL_UNITS.METER_PER_SECOND_SQUARE,
				Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL),
				0x06);
	    
	    public static final Map<String, ChannelDetails> mChannelMapRef;
	    static {
	        Map<String, ChannelDetails> aMap = new LinkedHashMap<String, ChannelDetails>();
	        aMap.put(SensorADXL371.ObjectClusterSensorName.ACCEL_HighG_X, SensorADXL371.channelADXL371AccelX);
	        aMap.put(SensorADXL371.ObjectClusterSensorName.ACCEL_HighG_Y, SensorADXL371.channelADXL371AccelY);
	        aMap.put(SensorADXL371.ObjectClusterSensorName.ACCEL_HighG_Z, SensorADXL371.channelADXL371AccelZ);
			mChannelMapRef = Collections.unmodifiableMap(aMap);
	    }
		//--------- Channel info end --------------
		
	    public static final SensorGroupingDetails sensorGroupAdxlAccel = new SensorGroupingDetails(
				LABEL_SENSOR_TILE.HIGH_G_ACCEL,
				Arrays.asList(Configuration.Shimmer3.SENSOR_ID.SHIMMER_ADXL371_ACCEL),
				CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW);

	  //--------- Constructors for this class start --------------
    
		public SensorADXL371() {
			super(SENSORS.ADXL371);
			initialise();
		}
		
		public SensorADXL371(ShimmerObject obj) {
			super(SENSORS.ADXL371, obj);
			initialise();
		}
		
		public SensorADXL371(ShimmerDevice shimmerDevice) {
			super(SENSORS.ADXL371, shimmerDevice);
			initialise();
		}
	    
	  //--------- Constructors for this class end --------------
		
	//--------- Abstract methods implemented start --------------
	@Override 
	public void generateSensorMap() {
		super.createLocalSensorMapWithCustomParser(mSensorMapRef, mChannelMapRef);
	}

	
	@Override 
	public void generateConfigOptionsMap() {
		addConfigOption(configOptionAccelRate);
	}
	
	@Override 
	public void generateSensorGroupMapping() {
		mSensorGroupingMap = new LinkedHashMap<Integer, SensorGroupingDetails>();
		if(mShimmerVerObject.isShimmerGen3() || mShimmerVerObject.isShimmerGen4()){
			mSensorGroupingMap.put(Configuration.Shimmer3.LABEL_SENSOR_TILE.HIGH_G_ACCEL.ordinal(), sensorGroupAdxlAccel);
		}
		super.updateSensorGroupingMap();	
	}	

	
	@Override 
	public Object getSettings(String componentName, COMMUNICATION_TYPE commType) {
		// TODO Auto-generated method stub
		//XXX - RS: Also returning null in BMP180 and GSR sensors classes 
		return null;
	}

	
	@Override 
	public ActionSetting setSettings(String componentName, Object valueToSet, COMMUNICATION_TYPE commType) {
		// 		Object returnValue = null;
		ActionSetting actionsetting = new ActionSetting(commType);

		return actionsetting;
		
	}
	
	@Override
	public LinkedHashMap<String, Object> generateConfigMap() {
		LinkedHashMap<String, Object> mapOfConfig = new LinkedHashMap<String, Object>();
		
		mapOfConfig.put(SensorADXL371.DatabaseConfigHandle.HighG_ACC_RATE, getADXL371AnalogAccelRate());
		
		super.addCalibDetailsToDbMap(mapOfConfig, 
				getCurrentCalibDetailsAccelHighG(), 
				SensorADXL371.DatabaseConfigHandle.LIST_OF_CALIB_HANDLES_HighG_ACCEL,
				SensorADXL371.DatabaseConfigHandle.HighG_ACC_CALIB_TIME);

		return mapOfConfig;
	}	
	
	@Override
	public void parseConfigMap(LinkedHashMap<String, Object> mapOfConfigPerShimmer) {
		
		if(mapOfConfigPerShimmer.containsKey(SensorADXL371.DatabaseConfigHandle.HighG_ACC_RATE)){
			setADXL371AnalogAccelRate(((Double) mapOfConfigPerShimmer.get(SensorADXL371.DatabaseConfigHandle.HighG_ACC_RATE)).intValue());
		}
		
		//Analog Accel Calibration Configuration
		parseCalibDetailsKinematicFromDb(mapOfConfigPerShimmer, 
				Configuration.Shimmer3.SENSOR_ID.SHIMMER_ADXL371_ACCEL, 
				0, 
				SensorADXL371.DatabaseConfigHandle.LIST_OF_CALIB_HANDLES_HighG_ACCEL,
				SensorADXL371.DatabaseConfigHandle.HighG_ACC_CALIB_TIME);
	}


	@Override
	public boolean processResponse(int responseCommand, Object parsedResponse, COMMUNICATION_TYPE commType) {
		// TODO Auto-generated method stub
		return false;
	}
	
	//--------- Abstract methods implemented end --------------

	//--------- Optional methods to override in Sensor Class start --------
		@Override
		public void initialise() {
			mSensorIdAccel = Configuration.Shimmer3.SENSOR_ID.SHIMMER_ADXL371_ACCEL;
			super.initialise();

			updateCurrentAccelHighGCalibInUse();
		}

		@Override
		public void generateCalibMap() {
			super.generateCalibMap();
			
			TreeMap<Integer, CalibDetails> calibMapAccelHighG= new TreeMap<Integer, CalibDetails>();
			calibMapAccelHighG.put(calibDetailsAccelHighG.mRangeValue, calibDetailsAccelHighG);
			setCalibrationMapPerSensor(Configuration.Shimmer3.SENSOR_ID.SHIMMER_ADXL371_ACCEL, calibMapAccelHighG);

			updateCurrentAccelHighGCalibInUse();
		}
		
		//--------- Optional methods to override in Sensor Class end --------

		//--------- Sensor specific methods start --------------
	
		public int getAccelRateFromFreqForSensor(boolean isEnabled, double freq) {
			return SensorADXL371.getAccelRateFromFreq(isEnabled, freq);
		}

		public static int getAccelRateFromFreq(boolean isEnabled, double freq) {
			int accelRate = 0; // Power down
			
			if(isEnabled){
				if (freq<=320){
					accelRate = 0; // 320Hz
				} else if (freq<=640){
					accelRate = 1; // 640Hz
				} else if (freq<=1280){
					accelRate = 2; // 1280Hz
				} else if (freq<=2560){
					accelRate = 3; // 2560Hz
				} else {
					accelRate = 4; // 5120Hz
				}
			}
			return accelRate;
		}
		
		public static String parseFromDBColumnToGUIChannel(String databaseChannelHandle) {
			return AbstractSensor.parseFromDBColumnToGUIChannel(mChannelMapRef, databaseChannelHandle);
		}

		public static String parseFromGUIChannelsToDBColumn(String objectClusterName) {
			return AbstractSensor.parseFromGUIChannelsToDBColumn(mChannelMapRef, objectClusterName);
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
		
		public int getADXL371AnalogAccelRate() {
			return mADXL371AnalogAccelRate;
		}
		
		public void setADXL371AnalogAccelRate(int valueToSet) {
			setADXL371AnalogAccelRateInternal(valueToSet);
		}
		
		public void setADXL371AnalogAccelRateInternal(int valueToSet) {
			//System.out.println("Accel Rate:\t" + valueToSet);
			//UtilShimmer.consolePrintCurrentStackTrace();
			mADXL371AnalogAccelRate = valueToSet;
		}

		public CalibDetailsKinematic getCurrentCalibDetailsAccelHighG(){
//			return getCurrentCalibDetails(mSensorIdAccel, getAccelRange());
			return mCurrentCalibDetailsAccelHighG;
		}
		
		public void updateCurrentAccelHighGCalibInUse(){
//			mCurrentCalibDetailsAccelWr = getCurrentCalibDetailsAccelWr();
			mCurrentCalibDetailsAccelHighG = getCurrentCalibDetailsIfKinematic(mSensorIdAccel, 0);
		}
		//--------- Sensor specific methods end --------------
	
}
