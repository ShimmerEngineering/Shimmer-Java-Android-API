package com.shimmerresearch.sensors.lisxmdl;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.ArrayUtils;

import com.shimmerresearch.bluetooth.BtCommandDetails;
import com.shimmerresearch.driver.ConfigByteLayout;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.FormatCluster;
import com.shimmerresearch.driver.Configuration.CHANNEL_UNITS;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driver.Configuration.Shimmer3.CompatibilityInfoForMaps;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driver.ShimmerObject;
import com.shimmerresearch.driver.calibration.CalibDetails;
import com.shimmerresearch.driver.calibration.CalibDetailsKinematic;
import com.shimmerresearch.driver.calibration.UtilCalibration;
import com.shimmerresearch.driver.calibration.CalibDetails.CALIB_READ_SOURCE;
import com.shimmerresearch.driver.shimmer2r3.ConfigByteLayoutShimmer3;
import com.shimmerresearch.driverUtilities.ChannelDetails;
import com.shimmerresearch.driverUtilities.ConfigOptionDetailsSensor;
import com.shimmerresearch.driverUtilities.ConfigOptionObject;
import com.shimmerresearch.driverUtilities.SensorDetails;
import com.shimmerresearch.driverUtilities.SensorDetailsRef;
import com.shimmerresearch.driverUtilities.SensorGroupingDetails;
import com.shimmerresearch.driverUtilities.UtilShimmer;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_ENDIAN;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_TYPE;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_TYPE;
import com.shimmerresearch.sensors.AbstractSensor;
import com.shimmerresearch.sensors.ActionSetting;

public class SensorLIS3MDL extends AbstractSensor{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4028368641088628178L;

	protected int mWRMagRange = 0;
	public boolean mIsUsingDefaultWRMagParam = true;
	protected boolean mLowPowerMag = false;
	protected boolean mMedPowerMag = false;
	protected boolean mHighPowerMag = false;
	protected boolean mUltraHighPowerMag = false;
	protected int mLISWRMagRate = 4;
	protected int mSensorIdWRMag = -1;
	
	
	//--------- Sensor specific variables start --------------	
	
		public static final double[][] DefaultAlignmentLIS3MDL = {{1,0,0},{0,-1,0},{0,0,-1}};	

		// ----------   AltMag start ---------------
		public static final double[][] DefaultAlignmentMatrixWrMagShimmer3 = DefaultAlignmentLIS3MDL; 				
		public static final double[][] DefaultOffsetVectorWrMagShimmer3 = {{0},{0},{0}};	
		// Manufacturer stated: X any Y any Z axis @ 6842 LSB/gauss
		public static final double[][] DefaultSensitivityMatrixWrMag4GaShimmer3 = {{6842,0,0},{0,6842,0},{0,0,6842}};
		// Manufacturer stated: X any Y any Z axis @ 3421  LSB/gauss
		public static final double[][] DefaultSensitivityMatrixWrMag8GaShimmer3 = {{3421 ,0,0},{0,3421 ,0},{0,0,3421 }};
		// Manufacturer stated: X any Y any Z axis @ 2281  LSB/gauss
		public static final double[][] DefaultSensitivityMatrixWrMag12GaShimmer3 = {{2281 ,0,0},{0,2281 ,0},{0,0,2281 }};
		// Manufacturer stated: X any Y any Z axis @ 1711  LSB/gauss
		public static final double[][] DefaultSensitivityMatrixWrMag16GaShimmer3 = {{1711 ,0,0},{0,1711 ,0},{0,0,1711 }};

		private CalibDetailsKinematic calibDetailsMag4 = new CalibDetailsKinematic(
				ListofLIS3MDLWrMagRangeConfigValues[0],
				ListofLIS3MDLWrMagRange[0],
				DefaultAlignmentMatrixWrMagShimmer3,
				DefaultSensitivityMatrixWrMag4GaShimmer3,
				DefaultOffsetVectorWrMagShimmer3);
		
		private CalibDetailsKinematic calibDetailsMag8 = new CalibDetailsKinematic(
				ListofLIS3MDLWrMagRangeConfigValues[1],
				ListofLIS3MDLWrMagRange[1],
				DefaultAlignmentMatrixWrMagShimmer3, 
				DefaultSensitivityMatrixWrMag8GaShimmer3,
				DefaultOffsetVectorWrMagShimmer3);
		
		private CalibDetailsKinematic calibDetailsMag12 = new CalibDetailsKinematic(
				ListofLIS3MDLWrMagRangeConfigValues[2], 
				ListofLIS3MDLWrMagRange[2],
				DefaultAlignmentMatrixWrMagShimmer3,
				DefaultSensitivityMatrixWrMag12GaShimmer3, 
				DefaultOffsetVectorWrMagShimmer3);
		
		private CalibDetailsKinematic calibDetailsMag16 = new CalibDetailsKinematic(
				ListofLIS3MDLWrMagRangeConfigValues[3],
				ListofLIS3MDLWrMagRange[3],
				DefaultAlignmentMatrixWrMagShimmer3,
				DefaultSensitivityMatrixWrMag16GaShimmer3,
				DefaultOffsetVectorWrMagShimmer3);
		
		public CalibDetailsKinematic mCurrentCalibDetailsMagWr = calibDetailsMag4;

		// ----------   AltMag end ---------------
	
	public static class DatabaseChannelHandles{
		public static final String WR_MAG_X = "LIS3MDL_MAG_X";
		public static final String WR_MAG_Y = "LIS3MDL_MAG_Y";
		public static final String WR_MAG_Z = "LIS3MDL_MAG_Z";
	}
	
	public static final class DatabaseConfigHandle{
		public static final String WR_MAG_RATE = "LIS3MDL_WR_Mag_Rate";
		public static final String WR_MAG_RANGE = "LIS3MDL_WR_Mag_Range";

		public static final String MAG_LPM = "LIS3MDL_Mag_LPM";
		public static final String MAG_MPM = "LIS3MDL_Mag_MPM";
		public static final String MAG_HPM = "LIS3MDL_Mag_HPM";
		public static final String MAG_UPM = "LIS3MDL_Mag_UPM";
		public static final String MAG_WR_CALIB_TIME = "LIS3MDL_Mag_WR_Calib_Time";
		public static final String MAG_WR_OFFSET_X = "LIS3MDL_Mag_WR_Offset_X";
		public static final String MAG_WR_OFFSET_Y = "LIS3MDL_Mag_WR_Offset_Y";
		public static final String MAG_WR_OFFSET_Z = "LIS3MDL_Mag_WR_Offset_Z";
		public static final String MAG_WR_GAIN_X = "LIS3MDL_Mag_WR_Gain_X";
		public static final String MAG_WR_GAIN_Y = "LIS3MDL_Mag_WR_Gain_Y";
		public static final String MAG_WR_GAIN_Z = "LIS3MDL_Mag_WR_Gain_Z";
		public static final String MAG_WR_ALIGN_XX = "LIS3MDL_Mag_WR_Align_XX";
		public static final String MAG_WR_ALIGN_XY = "LIS3MDL_Mag_WR_Align_XY";
		public static final String MAG_WR_ALIGN_XZ = "LIS3MDL_Mag_WR_Align_XZ";
		public static final String MAG_WR_ALIGN_YX = "LIS3MDL_Mag_WR_Align_YX";
		public static final String MAG_WR_ALIGN_YY = "LIS3MDL_Mag_WR_Align_YY";
		public static final String MAG_WR_ALIGN_YZ = "LIS3MDL_Mag_WR_Align_YZ";
		public static final String MAG_WR_ALIGN_ZX = "LIS3MDL_Mag_WR_Align_ZX";
		public static final String MAG_WR_ALIGN_ZY = "LIS3MDL_Mag_WR_Align_ZY";
		public static final String MAG_WR_ALIGN_ZZ = "LIS3MDL_Mag_WR_Align_ZZ";

		public static final List<String> LIST_OF_CALIB_HANDLES_MAG = Arrays.asList(
				DatabaseConfigHandle.MAG_WR_OFFSET_X, DatabaseConfigHandle.MAG_WR_OFFSET_Y, DatabaseConfigHandle.MAG_WR_OFFSET_Z,
				DatabaseConfigHandle.MAG_WR_GAIN_X, DatabaseConfigHandle.MAG_WR_GAIN_Y, DatabaseConfigHandle.MAG_WR_GAIN_Z,
				DatabaseConfigHandle.MAG_WR_ALIGN_XX, DatabaseConfigHandle.MAG_WR_ALIGN_XY, DatabaseConfigHandle.MAG_WR_ALIGN_XZ,
				DatabaseConfigHandle.MAG_WR_ALIGN_YX, DatabaseConfigHandle.MAG_WR_ALIGN_YY, DatabaseConfigHandle.MAG_WR_ALIGN_YZ,
				DatabaseConfigHandle.MAG_WR_ALIGN_ZX, DatabaseConfigHandle.MAG_WR_ALIGN_ZY, DatabaseConfigHandle.MAG_WR_ALIGN_ZZ);
		
	}
	
	public class GuiLabelConfig{
		
		public static final String LIS3MDL_WR_MAG_RANGE = "Wide Range Mag Range";
		public static final String LIS3MDL_WR_MAG_RATE = "Wide Range Mag Rate";
		public static final String LIS3MDL_MAG_LP = "Mag Low-Power Mode";
		public static final String LIS3MDL_MAG_MP = "Mag Med-Power Mode";
		public static final String LIS3MDL_MAG_HP = "Mag High-Power Mode";
		public static final String LIS3MDL_MAG_UP = "Mag Ultra High-Power Mode";
		public static final String LIS3MDL_WR_MAG_DEFAULT_CALIB = "Wide Range Mag Default Calibration";

		//NEW
		public static final String LIS3MDL_WR_MAG_CALIB_PARAM = "Wide Range Mag Calibration Details";
		public static final String LIS3MDL_WR_MAG_VALID_CALIB = "Wide Range Mag Valid Calibration";
	}
	
	public static class ObjectClusterSensorName{
		
		public static  String MAG_WR_X = "Mag_WR_X";
		public static  String MAG_WR_Y = "Mag_WR_Y";
		public static  String MAG_WR_Z = "Mag_WR_Z";				
	}
	
	public class GuiLabelSensors{
		public static final String MAG_WR = "Wide-Range Magnetometer"; 
	}
	
	public class LABEL_SENSOR_TILE{
		public static final String WIDE_RANGE_MAG = GuiLabelSensors.MAG_WR;
	}

	//--------- Sensor specific variables end --------------	
	
	//--------- Configuration options start --------------
	
		public static final String[] ListofLIS3MDLWrMagRateLp={"0.625Hz","1.25Hz","2.5Hz","5Hz","10Hz","20Hz","40Hz","80Hz","1000Hz"};
		public static final String[] ListofLIS3MDLWrMagRateMp={"1.25Hz","2.5Hz","5Hz","10Hz","20Hz","40Hz","80Hz","560Hz"};
		public static final String[] ListofLIS3MDLWrMagRateHp={"1.25Hz","2.5Hz","5Hz","10Hz","20Hz","40Hz","80Hz","300Hz"};
		public static final String[] ListofLIS3MDLWrMagRateUp={"1.25Hz","2.5Hz","5Hz","10Hz","20Hz","40Hz","80Hz","155Hz"};
		public static final Integer[] ListofLIS3MDLWrMagRateLpConfigValues = {0x00, 0x02, 0x04, 0x06, 0x08, 0x0A, 0x0C, 0x0E, 0x01};
		public static final Integer[] ListofLIS3MDLWrMagRateMpConfigValues = {0x12, 0x14, 0x16, 0x18, 0x1A, 0x1C, 0x1E, 0x11};
		public static final Integer[] ListofLIS3MDLWrMagRateHpConfigValues = {0x22, 0x24, 0x26, 0x28, 0x2A, 0x2C, 0x2E, 0x21};
		public static final Integer[] ListofLIS3MDLWrMagRateUpConfigValues = {0x32, 0x34, 0x36, 0x38, 0x3A, 0x3C, 0x3E, 0x31};

		public static final String[] ListofLIS3MDLWrMagRange={"+/- 4Ga","+/- 8Ga","+/- 12Ga","+/- 16Ga"}; 
		public static final Integer[] ListofLIS3MDLWrMagRangeConfigValues={0,1,2,3};

		public static final ConfigOptionDetailsSensor configOptionMagRange = new ConfigOptionDetailsSensor(
				SensorLIS3MDL.GuiLabelConfig.LIS3MDL_WR_MAG_RANGE,
				SensorLIS3MDL.DatabaseConfigHandle.WR_MAG_RANGE,
				ListofLIS3MDLWrMagRange, 
				ListofLIS3MDLWrMagRangeConfigValues, 
				ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX,
				CompatibilityInfoForMaps.listOfCompatibleVersionInfoLIS3MDL);
		
		public static final ConfigOptionDetailsSensor configOptionMagRate = new ConfigOptionDetailsSensor(
				SensorLIS3MDL.GuiLabelConfig.LIS3MDL_WR_MAG_RATE,
				SensorLIS3MDL.DatabaseConfigHandle.WR_MAG_RATE,
				ListofLIS3MDLWrMagRateLp, 
				ListofLIS3MDLWrMagRateLpConfigValues,
				ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX,
				CompatibilityInfoForMaps.listOfCompatibleVersionInfoLIS3MDL,
			    Arrays.asList(
			            new ConfigOptionObject(ConfigOptionDetailsSensor.VALUE_INDEXES.LIS3MDL_WR_MAG_RATE.IS_MP, 
			                    SensorLIS3MDL.ListofLIS3MDLWrMagRateMp, 
			                    SensorLIS3MDL.ListofLIS3MDLWrMagRateMpConfigValues),
			            new ConfigOptionObject(ConfigOptionDetailsSensor.VALUE_INDEXES.LIS3MDL_WR_MAG_RATE.IS_HP, 
			                    SensorLIS3MDL.ListofLIS3MDLWrMagRateHp, 
			                    SensorLIS3MDL.ListofLIS3MDLWrMagRateHpConfigValues),
			            new ConfigOptionObject(ConfigOptionDetailsSensor.VALUE_INDEXES.LIS3MDL_WR_MAG_RATE.IS_UP, 
			                    SensorLIS3MDL.ListofLIS3MDLWrMagRateUp, 
			                    SensorLIS3MDL.ListofLIS3MDLWrMagRateUpConfigValues)
			        ));

		//--------- Configuration options end --------------
	
	//--------- Sensor info start --------------
	
	public static final SensorDetailsRef sensorLIS3MDLMag = new SensorDetailsRef(
			0x200000, //== Configuration.Shimmer3.SensorBitmap.SENSOR_MAG will be: SensorBitmap.SENSOR_MAG, 
			0x200000, //== Configuration.Shimmer3.SensorBitmap.SENSOR_MAG will be: SensorBitmap.SENSOR_MAG, 
			GuiLabelSensors.MAG_WR,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoLIS3MDL,
			Arrays.asList(GuiLabelConfig.LIS3MDL_WR_MAG_RANGE,
					GuiLabelConfig.LIS3MDL_WR_MAG_RATE),
			Arrays.asList(ObjectClusterSensorName.MAG_WR_X,
					ObjectClusterSensorName.MAG_WR_Y,
					ObjectClusterSensorName.MAG_WR_Z));
	
	public static final Map<Integer, SensorDetailsRef> mSensorMapRef;
    static {
        Map<Integer, SensorDetailsRef> aMap = new LinkedHashMap<Integer, SensorDetailsRef>(); 
        aMap.put(Configuration.Shimmer3.SENSOR_ID.SHIMMER_LIS3MDL_MAG_ALT, SensorLIS3MDL.sensorLIS3MDLMag);	
		mSensorMapRef = Collections.unmodifiableMap(aMap);
    }
	//--------- Sensor info end --------------
    
    //--------- Channel info start --------------
    
    public static final ChannelDetails channelLIS3MDLMagX = new ChannelDetails(
			ObjectClusterSensorName.MAG_WR_X,
			ObjectClusterSensorName.MAG_WR_X,
			DatabaseChannelHandles.WR_MAG_X,
			CHANNEL_DATA_TYPE.INT12, 2, CHANNEL_DATA_ENDIAN.LSB,
			CHANNEL_UNITS.LOCAL_FLUX,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL),
			0x17);
	
	public static final ChannelDetails channelLIS3MDLMagY = new ChannelDetails(
			ObjectClusterSensorName.MAG_WR_Y,
			ObjectClusterSensorName.MAG_WR_Y,
			DatabaseChannelHandles.WR_MAG_Y,
			CHANNEL_DATA_TYPE.INT12, 2, CHANNEL_DATA_ENDIAN.LSB,
			CHANNEL_UNITS.LOCAL_FLUX,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL),
			0x18);
	
	public static final ChannelDetails channelLIS3MDLMagZ = new ChannelDetails(
			ObjectClusterSensorName.MAG_WR_Z,
			ObjectClusterSensorName.MAG_WR_Z,
			DatabaseChannelHandles.WR_MAG_Z,
			CHANNEL_DATA_TYPE.INT12, 2, CHANNEL_DATA_ENDIAN.LSB,
			CHANNEL_UNITS.LOCAL_FLUX,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL),
			0x19);
    
    public static final Map<String, ChannelDetails> mChannelMapRef;
    static {
        Map<String, ChannelDetails> aMap = new LinkedHashMap<String, ChannelDetails>();
        aMap.put(SensorLIS3MDL.ObjectClusterSensorName.MAG_WR_X, SensorLIS3MDL.channelLIS3MDLMagX);
        aMap.put(SensorLIS3MDL.ObjectClusterSensorName.MAG_WR_Z, SensorLIS3MDL.channelLIS3MDLMagZ);
        aMap.put(SensorLIS3MDL.ObjectClusterSensorName.MAG_WR_Y, SensorLIS3MDL.channelLIS3MDLMagY);
		mChannelMapRef = Collections.unmodifiableMap(aMap);
    }
	//--------- Channel info end --------------

    public static final SensorGroupingDetails sensorGroupLisMag = new SensorGroupingDetails(
			LABEL_SENSOR_TILE.WIDE_RANGE_MAG,
			Arrays.asList(Configuration.Shimmer3.SENSOR_ID.SHIMMER_LIS3MDL_MAG_ALT),
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoLIS3MDL);
    
	//--------- Bluetooth commands start --------------
	//still not being implemented for wr mag sensor due to unavailability in docs
    public static final byte SET_MAG_GAIN_COMMAND             		= (byte) 0x37;
  	public static final byte MAG_GAIN_RESPONSE                		= (byte) 0x38;
  	public static final byte GET_MAG_GAIN_COMMAND             		= (byte) 0x39;
  	
	public static final byte SET_ALT_MAG_CALIBRATION_COMMAND      		= (byte) 0xAF; 
	public static final byte ALT_MAG_CALIBRATION_RESPONSE         		= (byte) 0xB0; 
	public static final byte GET_ALT_MAG_CALIBRATION_COMMAND      		= (byte) 0xB1; 

	public static final byte SET_ALT_MAG_SAMPLING_RATE_COMMAND    		= (byte) 0xB2; 
	public static final byte ALT_MAG_SAMPLING_RATE_RESPONSE       		= (byte) 0xB3; 
	public static final byte GET_ALT_MAG_SAMPLING_RATE_COMMAND    		= (byte) 0xB4; 
	
    public static final Map<Byte, BtCommandDetails> mBtGetCommandMap;
    static {
        Map<Byte, BtCommandDetails> aMap = new LinkedHashMap<Byte, BtCommandDetails>();
	    aMap.put(GET_MAG_GAIN_COMMAND, new BtCommandDetails(GET_MAG_GAIN_COMMAND, "GET_MAG_GAIN_COMMAND", MAG_GAIN_RESPONSE));
        aMap.put(GET_ALT_MAG_CALIBRATION_COMMAND, new BtCommandDetails(GET_ALT_MAG_CALIBRATION_COMMAND, "GET_ALT_MAG_CALIBRATION_COMMAND", ALT_MAG_CALIBRATION_RESPONSE));
        aMap.put(GET_ALT_MAG_SAMPLING_RATE_COMMAND, new BtCommandDetails(GET_ALT_MAG_SAMPLING_RATE_COMMAND, "GET_ALT_MAG_SAMPLING_RATE_COMMAND", ALT_MAG_SAMPLING_RATE_RESPONSE));
        mBtGetCommandMap = Collections.unmodifiableMap(aMap);
    }
    
    public static final Map<Byte, BtCommandDetails> mBtSetCommandMap;
    static {
        Map<Byte, BtCommandDetails> aMap = new LinkedHashMap<Byte, BtCommandDetails>();
        aMap.put(SET_MAG_GAIN_COMMAND, new BtCommandDetails(SET_MAG_GAIN_COMMAND, "SET_MAG_GAIN_COMMAND"));
        aMap.put(SET_ALT_MAG_CALIBRATION_COMMAND, new BtCommandDetails(SET_ALT_MAG_CALIBRATION_COMMAND, "SET_ALT_MAG_CALIBRATION_COMMAND"));
        aMap.put(SET_ALT_MAG_SAMPLING_RATE_COMMAND, new BtCommandDetails(SET_ALT_MAG_SAMPLING_RATE_COMMAND, "SET_ALT_MAG_SAMPLING_RATE_COMMAND"));
        mBtSetCommandMap = Collections.unmodifiableMap(aMap);
    }
	//--------- Bluetooth commands end --------------
    
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

		mapOfConfig.put(SensorLIS3MDL.DatabaseConfigHandle.WR_MAG_RANGE, getWRMagRange());
		mapOfConfig.put(SensorLIS3MDL.DatabaseConfigHandle.WR_MAG_RATE, getLIS3MDLWRMagRate());
		mapOfConfig.put(SensorLIS3MDL.DatabaseConfigHandle.MAG_LPM, getLowPowerMagEnabled());
		mapOfConfig.put(SensorLIS3MDL.DatabaseConfigHandle.MAG_MPM, getMedPowerMagEnabled());
		mapOfConfig.put(SensorLIS3MDL.DatabaseConfigHandle.MAG_HPM, getHighPowerMagEnabled());
		mapOfConfig.put(SensorLIS3MDL.DatabaseConfigHandle.MAG_UPM, getUltraHighPowerMagEnabled());

		super.addCalibDetailsToDbMap(mapOfConfig, 
				getCurrentCalibDetailsMagWr(), 
				SensorLIS3MDL.DatabaseConfigHandle.LIST_OF_CALIB_HANDLES_MAG,
				SensorLIS3MDL.DatabaseConfigHandle.MAG_WR_CALIB_TIME);

		return mapOfConfig;
	}
	
	public CalibDetailsKinematic getCurrentCalibDetailsMagWr(){
		if(mCurrentCalibDetailsMagWr==null){
			updateCurrentMagWrCalibInUse();
		}
		return mCurrentCalibDetailsMagWr;
	}
	
	public void updateCurrentMagWrCalibInUse(){
//		mCurrentCalibDetailsMag = getCurrentCalibDetailsMag();
		mCurrentCalibDetailsMagWr = getCurrentCalibDetailsIfKinematic(mSensorIdWRMag, getWRMagRange());
	}
	
	public int getWRMagRange() {
		return mWRMagRange;
	}
	
	public int getLIS3MDLWRMagRate() {
		return mLISWRMagRate;
	}
	
	public void setLIS3MDLWRMagRate(int valueToSet){
		mLISWRMagRate = valueToSet;
	}
	
	public void setWRMagRange(int valueToSet) {
		setLIS3MDLWRMagRange(valueToSet);
	}
	
	public void setLIS3MDLWRMagRange(int i) {
		if(ArrayUtils.contains(ListofLIS3MDLWrMagRangeConfigValues, i)){
			mWRMagRange = i;
			updateCurrentMagWrCalibInUse();
		}
	}
	
	
	public int getLowPowerMagEnabled(){
		return (isLowPowerMagEnabled()? 1:0);
	}
	
	public int getMedPowerMagEnabled(){
		return (isMedPowerMagEnabled()? 1:0);
	}
	
	public int getHighPowerMagEnabled(){
		return (isHighPowerMagEnabled()? 1:0);
	}
	
	public int getUltraHighPowerMagEnabled(){
		return (isUltraHighPowerMagEnabled()? 1:0);
	}
	
	public void	setLowPowerMag(boolean enable){
		mLowPowerMag = enable;
		if(mShimmerDevice!=null){
			setLISWRMagRateFromFreq(getSamplingRateShimmer());
		}
	}
	
	public void	setMedPowerMag(boolean enable){
		mMedPowerMag = enable;
		if(mShimmerDevice!=null){
			setLISWRMagRateFromFreq(getSamplingRateShimmer());
		}
	}
	
	public void	setHighPowerMag(boolean enable){
		mHighPowerMag = enable;
		if(mShimmerDevice!=null){
			setLISWRMagRateFromFreq(getSamplingRateShimmer());
		}
	}
	
	public void	setUltraHighPowerMag(boolean enable){
		mUltraHighPowerMag = enable;
		if(mShimmerDevice!=null){
			setLISWRMagRateFromFreq(getSamplingRateShimmer());
		}
	}
	
	public boolean isLowPowerMagEnabled(){
		return mLowPowerMag;
	}
	
	public boolean isMedPowerMagEnabled(){
		return mMedPowerMag;
	}
	
	public boolean isHighPowerMagEnabled(){
		return mHighPowerMag;
	}
	
	public boolean isUltraHighPowerMagEnabled(){
		return mUltraHighPowerMag;
	}


	@Override
	public void parseConfigMap(LinkedHashMap<String, Object> mapOfConfigPerShimmer) {
		if(mapOfConfigPerShimmer.containsKey(SensorLIS3MDL.DatabaseConfigHandle.MAG_LPM)){
			setLowPowerMag(((Double) mapOfConfigPerShimmer.get(SensorLIS3MDL.DatabaseConfigHandle.MAG_LPM))>0? true:false);
		}
		if(mapOfConfigPerShimmer.containsKey(SensorLIS3MDL.DatabaseConfigHandle.MAG_MPM)){
			setMedPowerMag(((Double) mapOfConfigPerShimmer.get(SensorLIS3MDL.DatabaseConfigHandle.MAG_MPM))>0? true:false);
		}
		if(mapOfConfigPerShimmer.containsKey(SensorLIS3MDL.DatabaseConfigHandle.MAG_HPM)){
			setHighPowerMag(((Double) mapOfConfigPerShimmer.get(SensorLIS3MDL.DatabaseConfigHandle.MAG_HPM))>0? true:false);
		}
		if(mapOfConfigPerShimmer.containsKey(SensorLIS3MDL.DatabaseConfigHandle.MAG_UPM)){
			setUltraHighPowerMag(((Double) mapOfConfigPerShimmer.get(SensorLIS3MDL.DatabaseConfigHandle.MAG_UPM))>0? true:false);
		}
		if(mapOfConfigPerShimmer.containsKey(SensorLIS3MDL.DatabaseConfigHandle.WR_MAG_RANGE)){
			setWRMagRange(((Double) mapOfConfigPerShimmer.get(SensorLIS3MDL.DatabaseConfigHandle.WR_MAG_RANGE)).intValue());
		}
		if(mapOfConfigPerShimmer.containsKey(SensorLIS3MDL.DatabaseConfigHandle.WR_MAG_RATE)){
			setLIS3MDLWRMagRate(((Double) mapOfConfigPerShimmer.get(SensorLIS3MDL.DatabaseConfigHandle.WR_MAG_RATE)).intValue());
		}
		
		//Magnetometer Calibration Configuration
		parseCalibDetailsKinematicFromDb(mapOfConfigPerShimmer, 
				Configuration.Shimmer3.SENSOR_ID.SHIMMER_LIS3MDL_MAG_ALT, 
				getWRMagRange(), 
				SensorLIS3MDL.DatabaseConfigHandle.LIST_OF_CALIB_HANDLES_MAG,
				SensorLIS3MDL.DatabaseConfigHandle.MAG_WR_CALIB_TIME);
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
		mSensorIdWRMag = Configuration.Shimmer3.SENSOR_ID.SHIMMER_LIS3MDL_MAG_ALT;
		super.initialise();
		
		mWRMagRange = ListofLIS3MDLWrMagRangeConfigValues[0];
		
		updateCurrentMagWrCalibInUse();
	}

	@Override
	public void generateCalibMap() {
		super.generateCalibMap();

		TreeMap<Integer, CalibDetails> calibMapMagWr = new TreeMap<Integer, CalibDetails>();
		calibMapMagWr.put(calibDetailsMag4.mRangeValue, calibDetailsMag4);
		calibMapMagWr.put(calibDetailsMag8.mRangeValue, calibDetailsMag8);
		calibMapMagWr.put(calibDetailsMag12.mRangeValue, calibDetailsMag12);
		calibMapMagWr.put(calibDetailsMag16.mRangeValue, calibDetailsMag16);
		setCalibrationMapPerSensor(Configuration.Shimmer3.SENSOR_ID.SHIMMER_LIS3MDL_MAG_ALT, calibMapMagWr);
		
		updateCurrentMagWrCalibInUse();
	}
	
	//--------- Optional methods to override in Sensor Class end --------
	
	//--------- Sensor specific methods start --------------
	
	public double getCalibTimeMagWR() {
		return mCurrentCalibDetailsMagWr.getCalibTimeMs();
	}
	
	public boolean isUsingValidMagWRParam() {
		if(!UtilShimmer.isAllZeros(getAlignmentMatrixMagWr()) && !UtilShimmer.isAllZeros(getSensitivityMatrixMagWr())){
			return true;
		}else{
			return false;
		}
	}
	
	public void updateIsUsingDefaultWRMagParam() {
		mIsUsingDefaultWRMagParam = getCurrentCalibDetailsMagWr().isUsingDefaultParameters();
	}
	
	public boolean checkLowPowerMag() {
		setLowPowerMag((getLIS3MDLWRMagRate() <= 14)? true:false);
		return isLowPowerMagEnabled();
	}
	
	public boolean checkMedPowerMag() {
		setMedPowerMag((getLIS3MDLWRMagRate() >= 17 && getLIS3MDLWRMagRate() <= 30)? true:false);
		return isMedPowerMagEnabled();
	}
	
	public boolean checkHighPowerMag() {
		setHighPowerMag((getLIS3MDLWRMagRate() >= 33 && getLIS3MDLWRMagRate() <= 46)? true:false);
		return isHighPowerMagEnabled();
	}
	
	public boolean checkUltraHighPowerMag() {
		setUltraHighPowerMag((getLIS3MDLWRMagRate() >= 49 && getLIS3MDLWRMagRate() <= 62)? true:false); 
		return isUltraHighPowerMagEnabled();
	}
	
	public int getMagRateFromFreqForSensor(boolean isEnabled, double freq, int powerMode) {
		return SensorLIS3MDL.getMagRateFromFreq(isEnabled, freq, powerMode);
	}
	
	public static int getMagRateFromFreq(boolean isEnabled, double freq, int powerMode) {
		int magRate = 0; // 0.625Hz
		
		if(isEnabled){
			switch(powerMode) {
			case 0:
				magRate = lowPowerMode(freq);
				break;
			case 1:
				magRate = medPowerMode(freq);
				break;
			case 2:
				magRate = highPowerMode(freq);
				break;
			case 3:
				magRate = ultraHighPowerMode(freq);
				break;
			}
		}
		return magRate;
	}
	
	public static int lowPowerMode(double freq) {
		int magRate = 0;
		
		if (freq<=0.625){
		magRate = 0; 
		} else if (freq<=1.25){
			magRate = 2;
		} else if (freq<=2.5) {
			magRate = 4;
		} else if (freq<=5) {
			magRate = 6;
		} else if (freq<=10) {
			magRate = 8;
		} else if (freq<=20) {
			magRate = 10;
		} else if (freq<=40) {
			magRate = 12;
		} else if (freq<=80) {
			magRate = 14;
		} else if (freq<=1000) {
			magRate = 1;
		}
		return magRate;
	}
	
	public static int medPowerMode(double freq) {
		int magRate = 18;
		
		if (freq<=1.25){
			magRate = 18;
		} else if (freq<=2.5) {
			magRate = 20;
		} else if (freq<=5) {
			magRate = 22;
		} else if (freq<=10) {
			magRate = 24;
		} else if (freq<=20) {
			magRate = 26;
		} else if (freq<=40) {
			magRate = 28;
		} else if (freq<=80) {
			magRate = 30;
		} else if (freq<=560) {
			magRate = 17;
		}
		return magRate;
	}
	
	public static int highPowerMode(double freq) {
		int magRate = 34;
		
		if (freq<=1.25){
			magRate = 34;
		} else if (freq<=2.5) {
			magRate = 36;
		} else if (freq<=5) {
			magRate = 38;
		} else if (freq<=10) {
			magRate = 40;
		} else if (freq<=20) {
			magRate = 42;
		} else if (freq<=40) {
			magRate = 44;
		} else if (freq<=80) {
			magRate = 46;
		} else if (freq<=300) {
			magRate = 33;
		}
		return magRate;
	}
	
	public static int ultraHighPowerMode(double freq) {
		int magRate = 50;
		
		if (freq<=1.25){
			magRate = 50;
		} else if (freq<=2.5) {
			magRate = 52;
		} else if (freq<=5) {
			magRate = 54;
		} else if (freq<=10) {
			magRate = 56;
		} else if (freq<=20) {
			magRate = 58;
		} else if (freq<=40) {
			magRate = 60;
		} else if (freq<=80) {
			magRate = 62;
		} else if (freq<=155) {
			magRate = 49;
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
	public ObjectCluster processDataCustom(SensorDetails sensorDetails, byte[] rawData, COMMUNICATION_TYPE commType,
			ObjectCluster objectCluster, boolean isTimeSyncEnabled, double pcTimestampMs) {

		// process data originating from the Shimmer
		objectCluster = sensorDetails.processDataCommon(rawData, commType, objectCluster, isTimeSyncEnabled, pcTimestampMs);
		
		//Calibration
		if(mEnableCalibration){
			// get uncalibrated data for each (sub)sensor
			if(sensorDetails.mSensorDetailsRef.mGuiFriendlyLabel.equals(GuiLabelSensors.MAG_WR) && mCurrentCalibDetailsMagWr!=null){
				double[] unCalibratedMagWrData = new double[3];
				for (ChannelDetails channelDetails:sensorDetails.mListOfChannels){
					//Uncalibrated Mag WR data
					if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.MAG_WR_X)){
						unCalibratedMagWrData[0] = ((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(channelDetails.mObjectClusterName), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
					}
					else if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.MAG_WR_Y)){
						unCalibratedMagWrData[1]  = ((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(channelDetails.mObjectClusterName), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
					}
					else if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.MAG_WR_Z)){
						unCalibratedMagWrData[2]  = ((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(channelDetails.mObjectClusterName), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
					}
				}
				
				double[] calibratedMagWrData = UtilCalibration.calibrateInertialSensorData(unCalibratedMagWrData, mCurrentCalibDetailsMagWr);
//				double[] calibratedAccelWrData = UtilCalibration.calibrateInertialSensorData(unCalibratedAccelWrData, mAlignmentMatrixWRAccel, mSensitivityMatrixWRAccel, mOffsetVectorWRAccel);
	
				//Add calibrated data to Object cluster
				if(sensorDetails.mSensorDetailsRef.mGuiFriendlyLabel.equals(GuiLabelSensors.MAG_WR)){	
					for (ChannelDetails channelDetails:sensorDetails.mListOfChannels){
						if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.MAG_WR_X)){
							objectCluster.addCalData(channelDetails, calibratedMagWrData[0], objectCluster.getIndexKeeper()-3, isUsingDefaultMagWRParam());
						}
						else if(channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.MAG_WR_Y)){
							objectCluster.addCalData(channelDetails, calibratedMagWrData[1], objectCluster.getIndexKeeper()-2, isUsingDefaultMagWRParam());
						}
						else if(channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.MAG_WR_Z)){
							objectCluster.addCalData(channelDetails, calibratedMagWrData[2], objectCluster.getIndexKeeper()-1, isUsingDefaultMagWRParam());
						}
					}
				}
	
				//Debugging
				if(mIsDebugOutput){
					super.consolePrintChannelsCal(objectCluster, Arrays.asList(
							new String[]{ObjectClusterSensorName.MAG_WR_X, CHANNEL_TYPE.UNCAL.toString()}, 
							new String[]{ObjectClusterSensorName.MAG_WR_Y, CHANNEL_TYPE.UNCAL.toString()}, 
							new String[]{ObjectClusterSensorName.MAG_WR_Z, CHANNEL_TYPE.UNCAL.toString()}, 
							new String[]{ObjectClusterSensorName.MAG_WR_X, CHANNEL_TYPE.CAL.toString()}, 
							new String[]{ObjectClusterSensorName.MAG_WR_Y, CHANNEL_TYPE.CAL.toString()},
							new String[]{ObjectClusterSensorName.MAG_WR_Z, CHANNEL_TYPE.CAL.toString()}));
				}
	
			}
		}
		return objectCluster;
	}
	
	public boolean isUsingDefaultMagWRParam(){
		return getCurrentCalibDetailsMagWr().isUsingDefaultParameters(); 
	}

	@Override
	public void checkShimmerConfigBeforeConfiguring() {
		if(!isSensorEnabled(mSensorIdWRMag)) {
			setDefaultLisMagWrSensorConfig(false);
		}
		setLowPowerMag(false);
	}

	@Override
	public void configBytesGenerate(ShimmerDevice shimmerDevice, byte[] configBytes, COMMUNICATION_TYPE commType) {
		ConfigByteLayout configByteLayout = shimmerDevice.getConfigByteLayout();
		if(configByteLayout instanceof ConfigByteLayoutShimmer3){
			ConfigByteLayoutShimmer3 configByteLayoutCast = (ConfigByteLayoutShimmer3) configByteLayout;
			
			configBytes[configByteLayoutCast.idxConfigSetupByte2] |= (byte) ((getWRMagRange() & configByteLayoutCast.maskLSM303DLHCMagRange) << configByteLayoutCast.bitShiftLSM303DLHCMagRange);
			configBytes[configByteLayoutCast.idxConfigSetupByte4] |= (byte) ((getLIS3MDLWRMagRate() & configByteLayoutCast.maskLIS3MDLAltMagSamplingRate) << configByteLayoutCast.bitShiftLIS3MDLAltMagSamplingRate);

			byte[] bufferCalibrationParameters = generateCalParamLIS3MDLMag();
			System.arraycopy(bufferCalibrationParameters, 0, configBytes, configByteLayoutCast.idxLIS3MDLAltMagCalibration, configByteLayoutCast.lengthGeneralCalibrationBytes);
		}
	}

	public byte[] generateCalParamLIS3MDLMag(){
		return mCurrentCalibDetailsMagWr.generateCalParamByteArray();
	}

	@Override
	public void configBytesParse(ShimmerDevice shimmerDevice, byte[] configBytes, COMMUNICATION_TYPE commType) {
		//still not being implemented for wr mag sensor
		ConfigByteLayout configByteLayout = shimmerDevice.getConfigByteLayout();
		if(configByteLayout instanceof ConfigByteLayoutShimmer3){
			ConfigByteLayoutShimmer3 configByteLayoutCast = (ConfigByteLayoutShimmer3) configByteLayout;
			
			setLIS3MDLWRMagRange((configBytes[configByteLayoutCast.idxConfigSetupByte2] >> configByteLayoutCast.bitShiftLSM303DLHCMagRange) & configByteLayoutCast.maskLSM303DLHCMagRange);
			setLIS3MDLWRMagRate((configBytes[configByteLayoutCast.idxConfigSetupByte4] >> configByteLayoutCast.bitShiftLIS3MDLAltMagSamplingRate) & configByteLayoutCast.maskLIS3MDLAltMagSamplingRate);
			checkLowPowerMag(); // check rate to determine if Sensor is in LPM mode

			if (shimmerDevice.isConnected()){
				getCurrentCalibDetailsMagWr().mCalibReadSource=CALIB_READ_SOURCE.INFOMEM;
			}

			byte[] bufferCalibrationParameters = new byte[configByteLayoutCast.lengthGeneralCalibrationBytes];
			System.arraycopy(configBytes, configByteLayoutCast.idxLIS3MDLAltMagCalibration, bufferCalibrationParameters, 0 , configByteLayoutCast.lengthGeneralCalibrationBytes);
			parseCalibParamFromPacketMagWr(bufferCalibrationParameters, CALIB_READ_SOURCE.INFOMEM);
		}
	}
	
	
	public void parseCalibParamFromPacketMagWr(byte[] bufferCalibrationParameters, CALIB_READ_SOURCE calibReadSource) {
		mCurrentCalibDetailsMagWr.parseCalParamByteArray(bufferCalibrationParameters, calibReadSource);
	}

	@Override
	public Object setConfigValueUsingConfigLabel(Integer sensorId, String configLabel, Object valueToSet) {
		Object returnValue = null;
		
		switch(configLabel){
		case(GuiLabelConfig.LIS3MDL_MAG_LP):
			setLowPowerMag((boolean)valueToSet);
			break;
		case(GuiLabelConfig.LIS3MDL_MAG_MP):
			setMedPowerMag((boolean)valueToSet);
			break;
		case(GuiLabelConfig.LIS3MDL_MAG_HP):
			setHighPowerMag((boolean)valueToSet);
			break;
		case(GuiLabelConfig.LIS3MDL_MAG_UP):
			setUltraHighPowerMag((boolean)valueToSet);
			break;
		case(GuiLabelConfig.LIS3MDL_WR_MAG_RANGE):
			setLIS3MDLWRMagRange((int)valueToSet);
			break;
		case(GuiLabelConfig.LIS3MDL_WR_MAG_RATE):
			setLIS3MDLWRMagRate((int)valueToSet);
			break;
			case(GuiLabelConfigCommon.RANGE):
				if(sensorId==mSensorIdWRMag){
					this.setConfigValueUsingConfigLabel(GuiLabelConfig.LIS3MDL_WR_MAG_RANGE, valueToSet);
					break;
				}
			case(GuiLabelConfigCommon.RATE):
				if(sensorId==mSensorIdWRMag){
					this.setConfigValueUsingConfigLabel(GuiLabelConfig.LIS3MDL_WR_MAG_RATE, valueToSet);
					break;
				}
			default:
				returnValue = super.setConfigValueUsingConfigLabelCommon(sensorId, configLabel, valueToSet);
				break;
		}	
		
        if(configLabel.equals(SensorLIS3MDL.GuiLabelConfig.LIS3MDL_WR_MAG_RATE)){
        	checkConfigOptionValues(configLabel);
        }
		
		return returnValue;
	}

	@Override
	public Object getConfigValueUsingConfigLabel(Integer sensorId, String configLabel) {
		Object returnValue = null;
		
		if(configLabel.equals(GuiLabelConfig.LIS3MDL_WR_MAG_RATE)){
        	checkConfigOptionValues(configLabel);
        }
		
		switch(configLabel){
			case(GuiLabelConfig.LIS3MDL_MAG_LP):
				returnValue = isLowPowerMagEnabled();
        		break;
			case(GuiLabelConfig.LIS3MDL_MAG_MP):
				returnValue = isMedPowerMagEnabled();
				break;
			case(GuiLabelConfig.LIS3MDL_MAG_HP):
				returnValue = isHighPowerMagEnabled();
				break;
			case(GuiLabelConfig.LIS3MDL_MAG_UP):
				returnValue = isUltraHighPowerMagEnabled();
        		break;
			case(GuiLabelConfig.LIS3MDL_WR_MAG_RANGE): 
				returnValue = getWRMagRange();
	    		break;
			case(GuiLabelConfig.LIS3MDL_WR_MAG_RATE): 
				int configValue = getLIS3MDLWRMagRate(); 
				returnValue = configValue;
				break;
			case(GuiLabelConfigCommon.RANGE):
				if(sensorId==mSensorIdWRMag){
					returnValue = this.getConfigValueUsingConfigLabel(GuiLabelConfig.LIS3MDL_WR_MAG_RANGE);
//					returnValue = 0;
					break;
				}
			case(GuiLabelConfigCommon.RATE):
				if(sensorId==mSensorIdWRMag){
					returnValue = this.getConfigValueUsingConfigLabel(GuiLabelConfig.LIS3MDL_WR_MAG_RATE);
					break;
				}
			default:
				returnValue = super.getConfigValueUsingConfigLabelCommon(sensorId, configLabel);
				break;
			
		}
		return returnValue;
	}

	@Override
	public void setSensorSamplingRate(double samplingRateHz) {
		setLowPowerMag(false);
		setLISWRMagRateFromFreq(samplingRateHz);
		checkLowPowerMag();
	}
	
	public int setLISWRMagRateFromFreq(double freq) {
		boolean isEnabled = isSensorEnabled(mSensorIdWRMag);
		if(isLowPowerMagEnabled()) {
			mLISWRMagRate = getMagRateFromFreqForSensor(isEnabled, freq, 0);
		} else if(isMedPowerMagEnabled()) {
			mLISWRMagRate = getMagRateFromFreqForSensor(isEnabled, freq, 1);
		} else if(isHighPowerMagEnabled()) {
			mLISWRMagRate = getMagRateFromFreqForSensor(isEnabled, freq, 2);
		} else if(isUltraHighPowerMagEnabled()) {
			mLISWRMagRate = getMagRateFromFreqForSensor(isEnabled, freq, 3);
		}
		return mLISWRMagRate;
	}
	
	@Override
	public boolean setDefaultConfigForSensor(int sensorId, boolean isSensorEnabled) {
		if(mSensorMap.containsKey(sensorId)){
			if(sensorId==mSensorIdWRMag) {
				setDefaultLisMagWrSensorConfig(isSensorEnabled);		
			}
			return true;
		}
		return false;
	}
	
	public void setDefaultLisMagWrSensorConfig(boolean isSensorEnabled) {
		if(isSensorEnabled) {
			setLowPowerMag(false);
		}
		else {
			setLIS3MDLWRMagRange(1);
			setLowPowerMag(true);
		}	
	}

	@Override
	public boolean checkConfigOptionValues(String stringKey) {
		// TODO Auto-generated method stub
		return false;
	}
	
	public double[][] getAlignmentMatrixMagWr(){
		return mCurrentCalibDetailsMagWr.getValidAlignmentMatrix();
	}
	
	public double[][] getSensitivityMatrixMagWr(){
		return mCurrentCalibDetailsMagWr.getValidSensitivityMatrix();
	}
	
	public double[][] getOffsetVectorMatrixMagWr(){
		return mCurrentCalibDetailsMagWr.getValidOffsetVector();
	}

	//--------- Sensor specific methods end --------------

}
