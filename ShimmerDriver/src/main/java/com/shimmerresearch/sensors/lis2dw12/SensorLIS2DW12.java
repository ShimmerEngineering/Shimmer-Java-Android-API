package com.shimmerresearch.sensors.lis2dw12;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import com.shimmerresearch.driver.Configuration.CHANNEL_UNITS;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driver.Configuration.Shimmer3.CompatibilityInfoForMaps;
import com.shimmerresearch.driver.calibration.CalibDetails;
import com.shimmerresearch.driver.calibration.CalibDetailsKinematic;
import com.shimmerresearch.bluetooth.BtCommandDetails;
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
import com.shimmerresearch.sensors.ActionSetting;
import com.shimmerresearch.sensors.lsm303.SensorLSM303;
import com.shimmerresearch.sensors.lsm303.SensorLSM303AH;
import com.shimmerresearch.sensors.lsm303.SensorLSM303.GuiLabelConfig;
import com.shimmerresearch.sensors.lsm303.SensorLSM303.GuiLabelSensors;
import com.shimmerresearch.sensors.lsm303.SensorLSM303.LABEL_SENSOR_TILE;
import com.shimmerresearch.sensors.lsm303.SensorLSM303.ObjectClusterSensorName;
import com.shimmerresearch.sensors.lsm303.SensorLSM303AH.DatabaseChannelHandles;

public class SensorLIS2DW12 extends AbstractSensor {

	
	// ----------   Wide-range accel start ---------------
	protected int mSensorIdAccel = -1;
	protected int mAccelRange = 0;
	public CalibDetailsKinematic mCurrentCalibDetailsAccelWr = null;
	
	public class GuiLabelConfig{
		public static final String LIS2DW12_ACCEL_RANGE = "Wide Range Accel Range"; 
		public static final String LIS2DW12_ACCEL_RATE = "Wide Range Accel Rate";  
		public static final String LIS2DW12_ACCEL_LPM = "Wide Range Accel Low-Power Mode"; 
	}
	
	public class GuiLabelSensors{
		public static final String ACCEL_WR = "Wide-Range Accelerometer"; 
	}
	
	public class LABEL_SENSOR_TILE{
		public static final String WIDE_RANGE_ACCEL = GuiLabelSensors.ACCEL_WR;
		public static final String ACCEL = "ACCEL";
	}
	
	public static class ObjectClusterSensorName{
		public static  String ACCEL_WR_X = "Accel_WR_X";
		public static  String ACCEL_WR_Y = "Accel_WR_Y";
		public static  String ACCEL_WR_Z= "Accel_WR_Z";		
	}
	
	public static class DatabaseChannelHandles{
		public static final String WR_ACC_X = "LIS2DW12_ACC_X";
		public static final String WR_ACC_Y = "LIS2DW12_ACC_Y";
		public static final String WR_ACC_Z = "LIS2DW12_ACC_Z";
	}
	
	public static final class DatabaseConfigHandle{
		public static final String WR_ACC = "LIS2DW12_Acc";
		public static final String WR_ACC_RATE = "LIS2DW12_Acc_Rate";
		public static final String WR_ACC_RANGE = "LIS2DW12_Acc_Range";
		
		public static final String WR_ACC_LPM = "LIS2DW12_Acc_LPM";
		public static final String WR_ACC_HPM = "LIS2DW12_Acc_HPM";
	}
	
	public static final String[] ListofLIS2DW12AccelRange={
			UtilShimmer.UNICODE_PLUS_MINUS + " 2g",
			UtilShimmer.UNICODE_PLUS_MINUS + " 4g",
			UtilShimmer.UNICODE_PLUS_MINUS + " 8g",
			UtilShimmer.UNICODE_PLUS_MINUS + " 16g"
	};  
	
	public static final double[][] DefaultAlignmentLIS2DW12 = {{0,-1,0},{1,0,0},{0,0,-1}};	
	public static final double[][] DefaultAlignmentMatrixWideRangeAccelShimmer3R = DefaultAlignmentLIS2DW12;
	
	public static final double[][] DefaultOffsetVectorWideRangeAccelShimmer3R = {{0},{0},{0}};
	
	public static final double[][] DefaultSensitivityMatrixWideRangeAccel2gShimmer3R = {{1671,0,0},{0,1671,0},{0,0,1671}};
	public static final double[][] DefaultSensitivityMatrixWideRangeAccel4gShimmer3R = {{836,0,0},{0,836,0},{0,0,836}};
	public static final double[][] DefaultSensitivityMatrixWideRangeAccel8gShimmer3R = {{418,0,0},{0,418,0},{0,0,418}};
	public static final double[][] DefaultSensitivityMatrixWideRangeAccel16gShimmer3R = {{209,0,0},{0,209,0},{0,0,209}};

	private CalibDetailsKinematic calibDetailsAccelWr2g = new CalibDetailsKinematic(
			ListofLIS2DW12AccelRangeConfigValues[0],
			ListofLIS2DW12AccelRange[0],
			DefaultAlignmentMatrixWideRangeAccelShimmer3R, 
			DefaultSensitivityMatrixWideRangeAccel2gShimmer3R, 
			DefaultOffsetVectorWideRangeAccelShimmer3R);
	
	private CalibDetailsKinematic calibDetailsAccelWr4g = new CalibDetailsKinematic(
			ListofLIS2DW12AccelRangeConfigValues[1], 
			ListofLIS2DW12AccelRange[1],
			DefaultAlignmentMatrixWideRangeAccelShimmer3R,
			DefaultSensitivityMatrixWideRangeAccel4gShimmer3R, 
			DefaultOffsetVectorWideRangeAccelShimmer3R);
	
	private CalibDetailsKinematic calibDetailsAccelWr8g = new CalibDetailsKinematic(
			ListofLIS2DW12AccelRangeConfigValues[2], 
			ListofLIS2DW12AccelRange[2],
			DefaultAlignmentMatrixWideRangeAccelShimmer3R, 
			DefaultSensitivityMatrixWideRangeAccel8gShimmer3R, 
			DefaultOffsetVectorWideRangeAccelShimmer3R);
	
	private CalibDetailsKinematic calibDetailsAccelWr16g = new CalibDetailsKinematic(
			ListofLIS2DW12AccelRangeConfigValues[3], 
			ListofLIS2DW12AccelRange[3],
			DefaultAlignmentMatrixWideRangeAccelShimmer3R,
			DefaultSensitivityMatrixWideRangeAccel16gShimmer3R, 
			DefaultOffsetVectorWideRangeAccelShimmer3R);
	// ----------   Wide-range accel end ---------------
	
	
	
	//--------- Sensor info start --------------
	public static final SensorDetailsRef sensorLIS2DW12Accel = new SensorDetailsRef(
			0x10<<8, //== Configuration.Shimmer3.SensorBitmap.SENSOR_D_ACCEL will be: SensorBitmap.SENSOR_D_ACCEL
			0x10<<8, //== Configuration.Shimmer3.SensorBitmap.SENSOR_D_ACCEL will be: SensorBitmap.SENSOR_D_ACCEL
			GuiLabelSensors.ACCEL_WR,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoLSM303AH,  // To add for LIS2DW12
			Arrays.asList(GuiLabelConfig.LIS2DW12_ACCEL_RANGE,
					GuiLabelConfig.LIS2DW12_ACCEL_RATE),
			Arrays.asList(ObjectClusterSensorName.ACCEL_WR_X,
					ObjectClusterSensorName.ACCEL_WR_Y,
					ObjectClusterSensorName.ACCEL_WR_Z));
	
	public static final Map<Integer, SensorDetailsRef> mSensorMapRef;
    static {
        Map<Integer, SensorDetailsRef> aMap = new LinkedHashMap<Integer, SensorDetailsRef>();
        aMap.put(Configuration.Shimmer3.SENSOR_ID.SHIMMER_LIS2DW12_ACCEL_WR, SensorLIS2DW12.sensorLIS2DW12Accel);  	
		mSensorMapRef = Collections.unmodifiableMap(aMap);
    }
	//--------- Sensor info end --------------
	
    
    
	//--------- Bluetooth commands start --------------
	public static final byte SET_WR_ACCEL_RANGE_COMMAND    		= (byte) 0x09;
	public static final byte WR_ACCEL_RANGE_RESPONSE       		= (byte) 0x0A;
	public static final byte GET_WR_ACCEL_RANGE_COMMAND    		= (byte) 0x0B;
	
	public static final byte SET_WR_ACCEL_CALIBRATION_COMMAND 	= (byte) 0x1A;
	public static final byte WR_ACCEL_CALIBRATION_RESPONSE	 	= (byte) 0x1B;
	public static final byte GET_WR_ACCEL_CALIBRATION_COMMAND  	= (byte) 0x1C;
	
	public static final byte SET_WR_ACCEL_SAMPLING_RATE_COMMAND  	= (byte) 0x40;
	public static final byte WR_ACCEL_SAMPLING_RATE_RESPONSE  		= (byte) 0x41;
	public static final byte GET_WR_ACCEL_SAMPLING_RATE_COMMAND  	= (byte) 0x42;
	
	public static final byte SET_WR_ACCEL_LPMODE_COMMAND 	= (byte) 0x43;
	public static final byte WR_ACCEL_LPMODE_RESPONSE		= (byte) 0x44;
	public static final byte GET_WR_ACCEL_LPMODE_COMMAND  	= (byte) 0x45;
	
	public static final byte SET_WR_ACCEL_HRMODE_COMMAND 	= (byte) 0x46;
	public static final byte WR_ACCEL_HRMODE_RESPONSE		= (byte) 0x47;
	public static final byte GET_WR_ACCEL_HRMODE_COMMAND 	= (byte) 0x48;
	
    public static final Map<Byte, BtCommandDetails> mBtGetCommandMap;
    static {
        Map<Byte, BtCommandDetails> aMap = new LinkedHashMap<Byte, BtCommandDetails>();
        aMap.put(GET_WR_ACCEL_RANGE_COMMAND, new BtCommandDetails(GET_WR_ACCEL_RANGE_COMMAND, "GET_WR_ACCEL_RANGE_COMMAND", WR_ACCEL_RANGE_RESPONSE));
        aMap.put(GET_WR_ACCEL_CALIBRATION_COMMAND, new BtCommandDetails(GET_WR_ACCEL_CALIBRATION_COMMAND, "GET_WR_ACCEL_CALIBRATION_COMMAND", WR_ACCEL_CALIBRATION_RESPONSE));
        aMap.put(GET_WR_ACCEL_SAMPLING_RATE_COMMAND, new BtCommandDetails(GET_WR_ACCEL_SAMPLING_RATE_COMMAND, "GET_WR_ACCEL_SAMPLING_RATE_COMMAND", WR_ACCEL_SAMPLING_RATE_RESPONSE));
        aMap.put(GET_WR_ACCEL_LPMODE_COMMAND, new BtCommandDetails(GET_WR_ACCEL_LPMODE_COMMAND, "GET_WR_ACCEL_LPMODE_COMMAND", WR_ACCEL_LPMODE_RESPONSE));
        aMap.put(GET_WR_ACCEL_HRMODE_COMMAND, new BtCommandDetails(GET_WR_ACCEL_HRMODE_COMMAND, "GET_WR_ACCEL_HRMODE_COMMAND", WR_ACCEL_HRMODE_RESPONSE));
        mBtGetCommandMap = Collections.unmodifiableMap(aMap);
    }
    
    public static final Map<Byte, BtCommandDetails> mBtSetCommandMap;
    static {
        Map<Byte, BtCommandDetails> aMap = new LinkedHashMap<Byte, BtCommandDetails>();
        aMap.put(SET_WR_ACCEL_RANGE_COMMAND, new BtCommandDetails(SET_WR_ACCEL_RANGE_COMMAND, "SET_WR_ACCEL_RANGE_COMMAND"));
        aMap.put(SET_WR_ACCEL_CALIBRATION_COMMAND, new BtCommandDetails(SET_WR_ACCEL_CALIBRATION_COMMAND, "SET_WR_ACCEL_CALIBRATION_COMMAND"));
        aMap.put(SET_WR_ACCEL_SAMPLING_RATE_COMMAND, new BtCommandDetails(SET_WR_ACCEL_SAMPLING_RATE_COMMAND, "SET_WR_ACCEL_SAMPLING_RATE_COMMAND"));
        aMap.put(SET_WR_ACCEL_LPMODE_COMMAND, new BtCommandDetails(SET_WR_ACCEL_LPMODE_COMMAND, "SET_WR_ACCEL_LPMODE_COMMAND"));
        aMap.put(SET_WR_ACCEL_HRMODE_COMMAND, new BtCommandDetails(SET_WR_ACCEL_HRMODE_COMMAND, "SET_WR_ACCEL_HRMODE_COMMAND"));
        mBtSetCommandMap = Collections.unmodifiableMap(aMap);
    }
	//--------- Bluetooth commands end --------------
    
    
    
	//--------- Configuration options start --------------
	public static final Integer[] ListofLIS2DW12AccelRangeConfigValues={0,1,2,3};  

	public static final String[] ListofLIS2DW12AccelRateHpm={"Power-down","12.5Hz","12.5Hz","25.0Hz","50.0Hz","100.0Hz","200.0Hz","400.0Hz","800.0Hz","1600.0Hz"};
	public static final Integer[] ListofLIS2DW12AccelRateHpmConfigValues={0,1,2,3,4,5,6,7,8,9};

	public static final String[] ListofLIS2DW12AccelRateLpm={"Power-down","1.6Hz","12.5Hz","25.0Hz","50.0Hz","100.0Hz","200.0Hz","200.0Hz","200.0Hz","200.0Hz"};
	public static final Integer[] ListofLIS2DW12AccelRateLpmConfigValues={0,1,2,3,4,5,6,7,8,9};

	public static final ConfigOptionDetailsSensor configOptionAccelRange = new ConfigOptionDetailsSensor(
			SensorLIS2DW12.GuiLabelConfig.LIS2DW12_ACCEL_RANGE,
			SensorLIS2DW12.DatabaseConfigHandle.WR_ACC_RANGE,
			ListofLIS2DW12AccelRange, 
			ListofLIS2DW12AccelRangeConfigValues, 
			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoLSM303AH); // To Be Changed

	public static final ConfigOptionDetailsSensor configOptionAccelRate = new ConfigOptionDetailsSensor(
			SensorLIS2DW12.GuiLabelConfig.LIS2DW12_ACCEL_RATE,
			SensorLIS2DW12.DatabaseConfigHandle.WR_ACC_RATE,
			ListofLIS2DW12AccelRateHpm, 
			ListofLIS2DW12AccelRateHpmConfigValues, 
			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoLSM303AH, // To Be Changed
			Arrays.asList(
				new ConfigOptionObject(ConfigOptionDetailsSensor.VALUE_INDEXES.LIS2DW12_ACCEL_RATE.IS_LPM, 
						ListofLIS2DW12AccelRateLpm, 
						ListofLIS2DW12AccelRateLpmConfigValues)));
	
	public static final ConfigOptionDetailsSensor configOptionAccelLpm = new ConfigOptionDetailsSensor(
			SensorLIS2DW12.GuiLabelConfig.LIS2DW12_ACCEL_LPM,
			SensorLIS2DW12.DatabaseConfigHandle.WR_ACC_LPM,
			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.CHECKBOX);
	//--------- Configuration options end --------------
	
	
	
	//--------- Channel info start --------------
    public static final ChannelDetails channelLIS2DW12AccelX = new ChannelDetails(
			ObjectClusterSensorName.ACCEL_WR_X,
			ObjectClusterSensorName.ACCEL_WR_X,
			DatabaseChannelHandles.WR_ACC_X,
			CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.LSB,
			CHANNEL_UNITS.METER_PER_SECOND_SQUARE,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL),
			0x04);
    
    public static final ChannelDetails channelLIS2DW12AccelY = new ChannelDetails(
			ObjectClusterSensorName.ACCEL_WR_Y,
			ObjectClusterSensorName.ACCEL_WR_Y,
			DatabaseChannelHandles.WR_ACC_Y,
			CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.LSB,
			CHANNEL_UNITS.METER_PER_SECOND_SQUARE,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL),
			0x05);
    
    public static final ChannelDetails channelLIS2DW12AccelZ = new ChannelDetails(
			ObjectClusterSensorName.ACCEL_WR_Z,
			ObjectClusterSensorName.ACCEL_WR_Z,
			DatabaseChannelHandles.WR_ACC_Z,
			CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.LSB,
			CHANNEL_UNITS.METER_PER_SECOND_SQUARE,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL),
			0x06);
    
    public static final Map<String, ChannelDetails> mChannelMapRef;
    static {
        Map<String, ChannelDetails> aMap = new LinkedHashMap<String, ChannelDetails>();
        aMap.put(SensorLIS2DW12.ObjectClusterSensorName.ACCEL_WR_X, SensorLIS2DW12.channelLIS2DW12AccelX);
        aMap.put(SensorLIS2DW12.ObjectClusterSensorName.ACCEL_WR_Y, SensorLIS2DW12.channelLIS2DW12AccelY);
        aMap.put(SensorLIS2DW12.ObjectClusterSensorName.ACCEL_WR_Z, SensorLIS2DW12.channelLIS2DW12AccelZ);
		mChannelMapRef = Collections.unmodifiableMap(aMap);
    }
	//--------- Channel info end --------------
    
    public static final SensorGroupingDetails sensorGroupLpmAccel = new SensorGroupingDetails(
			LABEL_SENSOR_TILE.WIDE_RANGE_ACCEL,
			Arrays.asList(Configuration.Shimmer3.SENSOR_ID.SHIMMER_LIS2DW12_ACCEL_WR),
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoLSM303AH); // To Be Changed
	
    
	
    //--------- Constructors for this class start --------------
	public SensorLIS2DW12() {
		super(SENSORS.LIS2DW12);
		initialise();
	}
	
	public SensorLIS2DW12(ShimmerVerObject svo) {
		super(SENSORS.LIS2DW12, svo);
		initialise();
	}

	public SensorLIS2DW12(ShimmerDevice shimmerDevice) {
		super(SENSORS.LIS2DW12, shimmerDevice);
		initialise();
	}
   //--------- Constructors for this class end --------------

	

	@Override
	public void generateSensorMap() {
		// TODO Auto-generated method stub
		super.createLocalSensorMapWithCustomParser(mSensorMapRef, mChannelMapRef);
	}

	@Override
	public void generateConfigOptionsMap() {
		// TODO Auto-generated method stub
		addConfigOption(configOptionAccelRange);
		addConfigOption(configOptionAccelRate);
		addConfigOption(configOptionAccelLpm);
	}

	@Override
	public void generateSensorGroupMapping() {
		// TODO Auto-generated method stub
		mSensorGroupingMap = new LinkedHashMap<Integer, SensorGroupingDetails>();
		mSensorGroupingMap.put(Configuration.Shimmer3.LABEL_SENSOR_TILE.WIDE_RANGE_ACCEL.ordinal(), sensorGroupLpmAccel);
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

	@Override
	public LinkedHashMap<String, Object> generateConfigMap() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void parseConfigMap(LinkedHashMap<String, Object> mapOfConfigPerShimmer) {
		// TODO Auto-generated method stub
		
	}
	
	
	
	//--------- Optional methods to override in Sensor Class start --------
	@Override
	public void initialise() {
		mSensorIdAccel = Configuration.Shimmer3.SENSOR_ID.SHIMMER_LIS2DW12_ACCEL_WR;
		super.initialise();
		
		updateCurrentAccelWrCalibInUse();
	}
	
	@Override
	public void generateCalibMap() {
		super.generateCalibMap();
		
		TreeMap<Integer, CalibDetails> calibMapAccelWr = new TreeMap<Integer, CalibDetails>();
		calibMapAccelWr.put(calibDetailsAccelWr2g.mRangeValue, calibDetailsAccelWr2g);
		calibMapAccelWr.put(calibDetailsAccelWr4g.mRangeValue, calibDetailsAccelWr4g);
		calibMapAccelWr.put(calibDetailsAccelWr8g.mRangeValue, calibDetailsAccelWr8g);
		calibMapAccelWr.put(calibDetailsAccelWr16g.mRangeValue, calibDetailsAccelWr16g);
		setCalibrationMapPerSensor(mSensorIdAccel, calibMapAccelWr);

		updateCurrentAccelWrCalibInUse();
	}
	//--------- Optional methods to override in Sensor Class end --------
	
	
	
	//--------- Sensor specific methods start --------------
	public void updateCurrentAccelWrCalibInUse(){
		mCurrentCalibDetailsAccelWr = getCurrentCalibDetailsIfKinematic(mSensorIdAccel, getAccelRange());
	}
	
	public int getAccelRange() {
		return mAccelRange;
	}
	//--------- Sensor specific methods end --------------
}