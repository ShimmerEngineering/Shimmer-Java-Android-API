package com.shimmerresearch.sensors.lisxmdl;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.shimmerresearch.bluetooth.BtCommandDetails;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.Configuration.CHANNEL_UNITS;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driver.Configuration.Shimmer3.CompatibilityInfoForMaps;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driver.ShimmerObject;
import com.shimmerresearch.driver.calibration.CalibDetails;
import com.shimmerresearch.driver.calibration.CalibDetailsKinematic;
import com.shimmerresearch.driverUtilities.ChannelDetails;
import com.shimmerresearch.driverUtilities.ConfigOptionDetailsSensor;
import com.shimmerresearch.driverUtilities.SensorDetailsRef;
import com.shimmerresearch.driverUtilities.SensorGroupingDetails;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_ENDIAN;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_TYPE;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_TYPE;
import com.shimmerresearch.sensors.AbstractSensor;
import com.shimmerresearch.sensors.ActionSetting;

public class SensorLIS2MDL extends SensorLISXMDL{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4028368641088628178L;

	//--------- Sensor specific variables start --------------	
	
	public static final double[][] DefaultAlignmentLIS2MDL = {{-1,0,0},{0,-1,0},{0,0,-1}};	
	
// ----------   Mag start ---------------
	public static final double[][] DefaultAlignmentMatrixMagShimmer3 = DefaultAlignmentLIS2MDL; 				
	public static final double[][] DefaultOffsetVectorMagShimmer3 = {{0},{0},{0}};	

	public static final double[][] DefaultSensitivityMatrixMagShimmer3 = {{667,0,0},{0,667,0},{0,0,667}};

	private CalibDetailsKinematic calibDetailsMag = new CalibDetailsKinematic(
			ListofLIS2MDLWRMagRangeConfigValues[0],
			ListofLIS2MDLWRMagRange[0],
			DefaultAlignmentMatrixMagShimmer3,
			DefaultSensitivityMatrixMagShimmer3,
			DefaultOffsetVectorMagShimmer3);
	public CalibDetailsKinematic mCurrentCalibDetailsMag = calibDetailsMag;

	// ----------   Mag end ---------------
	
	public static class DatabaseChannelHandles{
		public static final String WR_MAG_X = "LIS2MDL_MAG_X";
		public static final String WR_MAG_Y = "LIS2MDL_MAG_Y";
		public static final String WR_MAG_Z = "LIS2MDL_MAG_Z";
	}
	
	public static final class DatabaseConfigHandle{
		public static final String WR_MAG_RATE = "LIS2MDL_WR_Mag_Rate";
		public static final String WR_MAG_RANGE = "LIS2MDL_WR_Mag_Range";
		
		public static final String MAG_CALIB_TIME = "LIS2MDL_Mag_Calib_Time";
		public static final String MAG_OFFSET_X = "LIS2MDL_Mag_Offset_X";
		public static final String MAG_OFFSET_Y = "LIS2MDL_Mag_Offset_Y";
		public static final String MAG_OFFSET_Z = "LIS2MDL_Mag_Offset_Z";
		public static final String MAG_GAIN_X = "LIS2MDL_Mag_Gain_X";
		public static final String MAG_GAIN_Y = "LIS2MDL_Mag_Gain_Y";
		public static final String MAG_GAIN_Z = "LIS2MDL_Mag_Gain_Z";
		public static final String MAG_ALIGN_XX = "LIS2MDL_Mag_Align_XX";
		public static final String MAG_ALIGN_XY = "LIS2MDL_Mag_Align_XY";
		public static final String MAG_ALIGN_XZ = "LIS2MDL_Mag_Align_XZ";
		public static final String MAG_ALIGN_YX = "LIS2MDL_Mag_Align_YX";
		public static final String MAG_ALIGN_YY = "LIS2MDL_Mag_Align_YY";
		public static final String MAG_ALIGN_YZ = "LIS2MDL_Mag_Align_YZ";
		public static final String MAG_ALIGN_ZX = "LIS2MDL_Mag_Align_ZX";
		public static final String MAG_ALIGN_ZY = "LIS2MDL_Mag_Align_ZY";
		public static final String MAG_ALIGN_ZZ = "LIS2MDL_Mag_Align_ZZ";

		public static final List<String> LIST_OF_CALIB_HANDLES_MAG = Arrays.asList(
				DatabaseConfigHandle.MAG_OFFSET_X, DatabaseConfigHandle.MAG_OFFSET_Y, DatabaseConfigHandle.MAG_OFFSET_Z,
				DatabaseConfigHandle.MAG_GAIN_X, DatabaseConfigHandle.MAG_GAIN_Y, DatabaseConfigHandle.MAG_GAIN_Z,
				DatabaseConfigHandle.MAG_ALIGN_XX, DatabaseConfigHandle.MAG_ALIGN_XY, DatabaseConfigHandle.MAG_ALIGN_XZ,
				DatabaseConfigHandle.MAG_ALIGN_YX, DatabaseConfigHandle.MAG_ALIGN_YY, DatabaseConfigHandle.MAG_ALIGN_YZ,
				DatabaseConfigHandle.MAG_ALIGN_ZX, DatabaseConfigHandle.MAG_ALIGN_ZY, DatabaseConfigHandle.MAG_ALIGN_ZZ);
		
	}

	//--------- Sensor specific variables end --------------	
	
	//--------- Bluetooth commands start --------------
	//still not being implemented for wr mag sensor due to unavailability in docs
	public static final byte SET_ALT_MAG_CALIBRATION_COMMAND      		= (byte) 0x17;//tbd
	public static final byte ALT_MAG_CALIBRATION_RESPONSE         		= (byte) 0x18;//tbd
	public static final byte GET_ALT_MAG_CALIBRATION_COMMAND      		= (byte) 0x19;//tbd

	public static final byte SET_ALT_MAG_SAMPLING_RATE_COMMAND    		= (byte) 0x3A;//tbd
	public static final byte ALT_MAG_SAMPLING_RATE_RESPONSE       		= (byte) 0x3B;//tbd
	public static final byte GET_ALT_MAG_SAMPLING_RATE_COMMAND    		= (byte) 0x3C;//tbd
	
    public static final Map<Byte, BtCommandDetails> mBtGetCommandMap;
    static {
        Map<Byte, BtCommandDetails> aMap = new LinkedHashMap<Byte, BtCommandDetails>();
        aMap.put(GET_ALT_MAG_CALIBRATION_COMMAND, new BtCommandDetails(GET_ALT_MAG_CALIBRATION_COMMAND, "GET_ALT_MAG_CALIBRATION_COMMAND", ALT_MAG_CALIBRATION_RESPONSE));
        aMap.put(GET_ALT_MAG_SAMPLING_RATE_COMMAND, new BtCommandDetails(GET_ALT_MAG_SAMPLING_RATE_COMMAND, "GET_ALT_MAG_SAMPLING_RATE_COMMAND", ALT_MAG_SAMPLING_RATE_RESPONSE));
        mBtGetCommandMap = Collections.unmodifiableMap(aMap);
    }
    
    public static final Map<Byte, BtCommandDetails> mBtSetCommandMap;
    static {
        Map<Byte, BtCommandDetails> aMap = new LinkedHashMap<Byte, BtCommandDetails>();
        aMap.put(SET_ALT_MAG_CALIBRATION_COMMAND, new BtCommandDetails(SET_ALT_MAG_CALIBRATION_COMMAND, "SET_ALT_MAG_CALIBRATION_COMMAND"));
        aMap.put(SET_ALT_MAG_SAMPLING_RATE_COMMAND, new BtCommandDetails(SET_ALT_MAG_SAMPLING_RATE_COMMAND, "SET_ALT_MAG_SAMPLING_RATE_COMMAND"));
        mBtSetCommandMap = Collections.unmodifiableMap(aMap);
    }
	//--------- Bluetooth commands end --------------
	
	//--------- Configuration options start --------------
	
	public static final String[] ListofLIS2MDLWRMagRate={"10.0Hz","20.0Hz","50.0Hz","100.0Hz"};
	public static final Integer[] ListofLIS2MDLWRMagRateConfigValues={0,1,2,3};
	
	public static final String[] ListofLIS2MDLWRMagRange={"+/- 49.152Ga"}; 
	public static final Integer[] ListofLIS2MDLWRMagRangeConfigValues={0};  
	
	public static final ConfigOptionDetailsSensor configOptionMagRange = new ConfigOptionDetailsSensor(
			SensorLISXMDL.GuiLabelConfig.LISXMDL_WR_MAG_RANGE,
			SensorLIS2MDL.DatabaseConfigHandle.WR_MAG_RANGE,
			ListofLIS2MDLWRMagRange, 
			ListofLIS2MDLWRMagRangeConfigValues, 
			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoLIS2MDL);

	public static final ConfigOptionDetailsSensor configOptionMagRate = new ConfigOptionDetailsSensor(
			SensorLISXMDL.GuiLabelConfig.LISXMDL_WR_MAG_RATE,
			SensorLIS2MDL.DatabaseConfigHandle.WR_MAG_RATE,
			ListofLIS2MDLWRMagRate, 
			ListofLIS2MDLWRMagRateConfigValues, 
			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoLIS2MDL);

	//--------- Configuration options end --------------
	
	//--------- Sensor info start --------------
	
	public static final SensorDetailsRef sensorLIS2MDLMag = new SensorDetailsRef(
			0x20, //== Configuration.Shimmer3.SensorBitmap.SENSOR_MAG will be: SensorBitmap.SENSOR_MAG, 
			0x20, //== Configuration.Shimmer3.SensorBitmap.SENSOR_MAG will be: SensorBitmap.SENSOR_MAG, 
			GuiLabelSensors.MAG,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoLIS2MDL,
			Arrays.asList(GuiLabelConfig.LISXMDL_WR_MAG_RATE),

			Arrays.asList(ObjectClusterSensorName.MAG_WR_X,
					ObjectClusterSensorName.MAG_WR_Y,
					ObjectClusterSensorName.MAG_WR_Z));
	
	public static final Map<Integer, SensorDetailsRef> mSensorMapRef;
    static {
        Map<Integer, SensorDetailsRef> aMap = new LinkedHashMap<Integer, SensorDetailsRef>(); 
        aMap.put(Configuration.Shimmer3.SENSOR_ID.SHIMMER_LIS2MDL_MAG_WR, SensorLIS2MDL.sensorLIS2MDLMag);	
		mSensorMapRef = Collections.unmodifiableMap(aMap);
    }
	//--------- Sensor info end --------------
    
    //--------- Channel info start --------------
    
    public static final ChannelDetails channelLIS2MDLMagX = new ChannelDetails(
			ObjectClusterSensorName.MAG_WR_X,
			ObjectClusterSensorName.MAG_WR_X,
			DatabaseChannelHandles.WR_MAG_X,
			CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.MSB,
			CHANNEL_UNITS.LOCAL_FLUX,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL),
			0x07);
	
	public static final ChannelDetails channelLIS2MDLMagY = new ChannelDetails(
			ObjectClusterSensorName.MAG_WR_Y,
			ObjectClusterSensorName.MAG_WR_Y,
			DatabaseChannelHandles.WR_MAG_Y,
			CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.MSB,
			CHANNEL_UNITS.LOCAL_FLUX,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL),
			0x08);
	
	public static final ChannelDetails channelLIS2MDLMagZ = new ChannelDetails(
			ObjectClusterSensorName.MAG_WR_Z,
			ObjectClusterSensorName.MAG_WR_Z,
			DatabaseChannelHandles.WR_MAG_Z,
			CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.MSB,
			CHANNEL_UNITS.LOCAL_FLUX,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL),
			0x09);
    
    public static final Map<String, ChannelDetails> mChannelMapRef;
    static {
        Map<String, ChannelDetails> aMap = new LinkedHashMap<String, ChannelDetails>();
        aMap.put(SensorLIS2MDL.ObjectClusterSensorName.MAG_WR_X, SensorLIS2MDL.channelLIS2MDLMagX);
        aMap.put(SensorLIS2MDL.ObjectClusterSensorName.MAG_WR_Z, SensorLIS2MDL.channelLIS2MDLMagZ);
        aMap.put(SensorLIS2MDL.ObjectClusterSensorName.MAG_WR_Y, SensorLIS2MDL.channelLIS2MDLMagY);
		mChannelMapRef = Collections.unmodifiableMap(aMap);
    }
	//--------- Channel info end --------------

    public static final SensorGroupingDetails sensorGroupLisMag = new SensorGroupingDetails(
			LABEL_SENSOR_TILE.WIDE_RANGE_MAG,
			Arrays.asList(Configuration.Shimmer3.SENSOR_ID.SHIMMER_LIS2MDL_MAG_WR),
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoLIS2MDL);
    
    //--------- Constructors for this class start --------------
	public SensorLIS2MDL() {
		super();
		initialise();
	}
	
	public SensorLIS2MDL(ShimmerObject obj) {
		super(obj);
		initialise();
	}
	
	public SensorLIS2MDL(ShimmerDevice shimmerDevice) {
		super(shimmerDevice);
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
		addConfigOption(configOptionMagRange);
		addConfigOption(configOptionMagRate);
	}

	@Override
	public void generateSensorGroupMapping() {
		mSensorGroupingMap = new LinkedHashMap<Integer, SensorGroupingDetails>();
		mSensorGroupingMap.put(Configuration.Shimmer3.LABEL_SENSOR_TILE.WIDE_RANGE_MAG_3R.ordinal(), sensorGroupLisMag);
		super.updateSensorGroupingMap();	
	}

	@Override
	public Object getSettings(String componentName, COMMUNICATION_TYPE commType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ActionSetting setSettings(String componentName, Object valueToSet, COMMUNICATION_TYPE commType) {
		ActionSetting actionsetting = new ActionSetting(commType);

		return actionsetting;
	}

	@Override
	public LinkedHashMap<String, Object> generateConfigMap() {
		LinkedHashMap<String, Object> mapOfConfig = new LinkedHashMap<String, Object>();

		mapOfConfig.put(SensorLIS2MDL.DatabaseConfigHandle.WR_MAG_RANGE, getWRMagRange());
		mapOfConfig.put(SensorLIS2MDL.DatabaseConfigHandle.WR_MAG_RATE, getLISWRMagRate());

		super.addCalibDetailsToDbMap(mapOfConfig, 
				getCurrentCalibDetailsMag(), 
				SensorLIS2MDL.DatabaseConfigHandle.LIST_OF_CALIB_HANDLES_MAG,
				SensorLIS2MDL.DatabaseConfigHandle.MAG_CALIB_TIME);

		return mapOfConfig;
	}

	@Override
	public void parseConfigMap(LinkedHashMap<String, Object> mapOfConfigPerShimmer) {

		if(mapOfConfigPerShimmer.containsKey(SensorLIS2MDL.DatabaseConfigHandle.WR_MAG_RANGE)){
			setLISWRMagRange(((Double) mapOfConfigPerShimmer.get(SensorLIS2MDL.DatabaseConfigHandle.WR_MAG_RANGE)).intValue());
		}
		if(mapOfConfigPerShimmer.containsKey(SensorLIS2MDL.DatabaseConfigHandle.WR_MAG_RATE)){
			setLISWRMagRate(((Double) mapOfConfigPerShimmer.get(SensorLIS2MDL.DatabaseConfigHandle.WR_MAG_RATE)).intValue());
		}
		
		//Magnetometer Calibration Configuration
		parseCalibDetailsKinematicFromDb(mapOfConfigPerShimmer, 
				Configuration.Shimmer3.SENSOR_ID.SHIMMER_LIS2MDL_MAG_WR, 
				getMagRange(), 
				SensorLIS2MDL.DatabaseConfigHandle.LIST_OF_CALIB_HANDLES_MAG,
				SensorLIS2MDL.DatabaseConfigHandle.MAG_CALIB_TIME);
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
		mSensorIdWRMag = Configuration.Shimmer3.SENSOR_ID.SHIMMER_LIS2MDL_MAG_WR;
		super.initialise();
		
		mWRMagRange = ListofLIS2MDLWRMagRangeConfigValues[0];
		
		updateCurrentMagWrCalibInUse();
	}

	@Override
	public void generateCalibMap() {
		super.generateCalibMap();

		TreeMap<Integer, CalibDetails> calibMapMag = new TreeMap<Integer, CalibDetails>();
		calibMapMag.put(calibDetailsMag.mRangeValue, calibDetailsMag);
		setCalibrationMapPerSensor(Configuration.Shimmer3.SENSOR_ID.SHIMMER_LIS2MDL_MAG_WR, calibMapMag);
		
		updateCurrentMagWrCalibInUse();
	}
	
	//--------- Optional methods to override in Sensor Class end --------
	
	//--------- Sensor specific methods start --------------

	@Override
	public int getMagRateFromFreqForSensor(boolean isEnabled, double freq) {
		int magRate = 0; // 10Hz

		if (freq<10.0){
			magRate = 0; // 10Hz
		} else if (freq<20.0){
			magRate = 1; // 20Hz
		} else if (freq<50.0) {
			magRate = 2; // 50Hz
		} else {
			magRate = 3; // 100Hz
		}
		return magRate;
	}

	public static String parseFromDBColumnToGUIChannel(String databaseChannelHandle) {
		return AbstractSensor.parseFromDBColumnToGUIChannel(mChannelMapRef, databaseChannelHandle);
	}

	public static String parseFromGUIChannelsToDBColumn(String objectClusterName) {
		return AbstractSensor.parseFromGUIChannelsToDBColumn(mChannelMapRef, objectClusterName);
	}

	@Override
	public int getMagRateFromFreqForSensor(boolean isEnabled, double freq, int mode) {
		return 0;
	}

	@Override
	public boolean checkLowPowerMag() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setLISMagRange(int valueToSet) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setLISWRMagRange(int valueToSet) {
		// TODO Auto-generated method stub
		
	}

	//--------- Sensor specific methods end --------------

}
