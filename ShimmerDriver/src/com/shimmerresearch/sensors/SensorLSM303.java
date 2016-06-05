package com.shimmerresearch.sensors;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.shimmerresearch.bluetooth.BtCommandDetails;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.FormatCluster;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driver.UtilShimmer;
import com.shimmerresearch.driver.Configuration.CHANNEL_UNITS;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driver.Configuration.Shimmer3.CompatibilityInfoForMaps;
import com.shimmerresearch.driverUtilities.ChannelDetails;
import com.shimmerresearch.driverUtilities.SensorConfigOptionDetails;
import com.shimmerresearch.driverUtilities.SensorDetails;
import com.shimmerresearch.driverUtilities.SensorDetailsRef;
import com.shimmerresearch.driverUtilities.SensorGroupingDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_ENDIAN;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_TYPE;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_TYPE;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.HW_ID;

/**
 * Sensor class for the LSM303DLHC combined Accelerometer and Magnetometer 
 * (commonly referred to as the wide-range accel in Shimmer literature)
 * 
 * Accelerometer: one 12-bit reading (left-justified) per axis, LSB. 
 * Magnetometer: one 12-bit reading (right-justified) per axis, MSB.
 * 
 * @author Ruud Stolk
 * @author Mark Nolan
 * 
 */
public class SensorLSM303 extends AbstractSensor{	
	/**
	 * Sensorclass for LSM303 - digital/wide-range accelerometer + magnetometer 
	 *  
	 *  @param svo
	 * * */  
	private static final long serialVersionUID = -2119834127313796684L;

	//--------- Sensor specific variables start --------------	
	public boolean mLowPowerAccelWR = false;
	public boolean mHighResAccelWR = true;
	public boolean mLowPowerMag = false;
	
	public int mAccelRange = 0;
	public int mLSM303DigitalAccelRate = 0;
	public int mMagRange = 1;
	public int mLSM303MagRate = 4;
	
	protected boolean mDefaultCalibrationParametersDigitalAccel = true;
	protected double[][] mAlignmentMatrixWRAccel = {{-1,0,0},{0,1,0},{0,0,-1}}; 			
	protected double[][] mSensitivityMatrixWRAccel = {{1631,0,0},{0,1631,0},{0,0,1631}};	 	
	protected double[][] mOffsetVectorWRAccel = {{0},{0},{0}};	
	
	protected boolean mDefaultCalibrationParametersMag = true;	
	protected double[][] mAlignmentMatrixMagnetometer = {{1,0,0},{0,1,0},{0,0,-1}};				
	protected double[][] mSensitivityMatrixMagnetometer = {{580,0,0},{0,580,0},{0,0,580}};	
	protected double[][] mOffsetVectorMagnetometer = {{0},{0},{0}};
	
	public static final double[][] SensitivityMatrixWideRangeAccel2gShimmer3 = {{1631,0,0},{0,1631,0},{0,0,1631}};
	public static final double[][] SensitivityMatrixWideRangeAccel4gShimmer3 = {{815,0,0},{0,815,0},{0,0,815}};
	public static final double[][] SensitivityMatrixWideRangeAccel8gShimmer3 = {{408,0,0},{0,408,0},{0,0,408}};
	public static final double[][] SensitivityMatrixWideRangeAccel16gShimmer3 = {{135,0,0},{0,135,0},{0,0,135}};

	protected static final double[][] AlignmentMatrixWideRangeAccelShimmer3 = {{-1,0,0},{0,1,0},{0,0,-1}};	
	protected static final double[][] OffsetVectorWideRangeAccelShimmer3 = {{0},{0},{0}};	

	public static final double[][] SensitivityMatrixMag1p3GaShimmer3 = {{1100,0,0},{0,1100,0},{0,0,980}};
	public static final double[][] SensitivityMatrixMag1p9GaShimmer3 = {{855,0,0},{0,855,0},{0,0,760}};
	public static final double[][] SensitivityMatrixMag2p5GaShimmer3 = {{670,0,0},{0,670,0},{0,0,600}};
	public static final double[][] SensitivityMatrixMag4GaShimmer3 = {{450,0,0},{0,450,0},{0,0,400}};
	public static final double[][] SensitivityMatrixMag4p7GaShimmer3 = {{400,0,0},{0,400,0},{0,0,355}};
	public static final double[][] SensitivityMatrixMag5p6GaShimmer3 = {{330,0,0},{0,330,0},{0,0,295}};
	public static final double[][] SensitivityMatrixMag8p1GaShimmer3 = {{230,0,0},{0,230,0},{0,0,205}};
	
	protected static final double[][] AlignmentMatrixMagShimmer3 = {{-1,0,0},{0,1,0},{0,0,-1}}; 				
	protected static final double[][] OffsetVectorMagShimmer3 = {{0},{0},{0}};	

	//TODO commented - RS (20/5/2016): Keep in Configuration.java for now. 
//	public class SensorBitmap{
//		public static final int SENSOR_MAG = 0x20;
//		public static final int SENSOR_D_ACCEL = 0x1000;
//	}
		
	public class GuiLabelConfig{
		public static final String LSM303DLHC_ACCEL_RATE = "Wide Range Accel Rate";  
		public static final String LSM303DLHC_ACCEL_RANGE = "Wide Range Accel Range"; 
				
		public static final String LSM303DLHC_MAG_RANGE = "Mag Range";
		public static final String LSM303DLHC_MAG_RATE = "Mag Rate";
		
		public static final String LSM303DLHC_ACCEL_LPM = "Wide Range Accel Low-Power Mode"; 
		public static final String LSM303DLHC_MAG_LPM = "Mag Low-Power Mode";
		
		public static final String LSM303DLHC_ACCEL_DEFAULT_CALIB = "Wide Range Accel Default Calibration";
		public static final String LSM303DLHC_MAG_DEFAULT_CALIB = "Mag Default Calibration";
	}
	

	public class GuiLabelSensors{
		public static final String ACCEL_WR = "Wide-Range Accelerometer"; 
		public static final String MAG = "Magnetometer"; 
	}

	
	public class GuiLabelSensorTiles{
		public static final String MAG = GuiLabelSensors.MAG;
		public static final String WIDE_RANGE_ACCEL = GuiLabelSensors.ACCEL_WR;
	}
	
	
	public static class DatabaseChannelHandles{
		public static final String WR_ACC_X = "LSM303DLHC_ACC_X";
		public static final String WR_ACC_Y = "LSM303DLHC_ACC_Y";
		public static final String WR_ACC_Z = "LSM303DLHC_ACC_Z";
		public static final String MAG_X = "LSM303DLHC_MAG_X";
		public static final String MAG_Y = "LSM303DLHC_MAG_Y";
		public static final String MAG_Z = "LSM303DLHC_MAG_Z";
	}
	
	
	public static class ObjectClusterSensorName{
		public static  String ACCEL_WR_X = "Accel_WR_X";
		public static  String ACCEL_WR_Y = "Accel_WR_Y";
		public static  String ACCEL_WR_Z= "Accel_WR_Z";
		
		public static  String MAG_X = "Mag_X";
		public static  String MAG_Y = "Mag_Y";
		public static  String MAG_Z = "Mag_Z";		
	}
	//--------- Sensor specific variables end --------------
	
	
	//--------- Bluetooth commands start --------------
	public static final byte SET_ACCEL_SENSITIVITY_COMMAND    		= (byte) 0x09;
	public static final byte ACCEL_SENSITIVITY_RESPONSE       		= (byte) 0x0A;
	public static final byte GET_ACCEL_SENSITIVITY_COMMAND    		= (byte) 0x0B;
	
	public static final byte SET_MAG_CALIBRATION_COMMAND      		= (byte) 0x17;
	public static final byte MAG_CALIBRATION_RESPONSE         		= (byte) 0x18;
	public static final byte GET_MAG_CALIBRATION_COMMAND      		= (byte) 0x19;
	
	public static final byte SET_LSM303DLHC_ACCEL_CALIBRATION_COMMAND = (byte) 0x1A;
	public static final byte LSM303DLHC_ACCEL_CALIBRATION_RESPONSE 	= (byte) 0x1B;
	public static final byte GET_LSM303DLHC_ACCEL_CALIBRATION_COMMAND = (byte) 0x1C;
	
	public static final byte SET_MAG_GAIN_COMMAND             		= (byte) 0x37;
	public static final byte MAG_GAIN_RESPONSE                		= (byte) 0x38;
	public static final byte GET_MAG_GAIN_COMMAND             		= (byte) 0x39;
	
	public static final byte SET_MAG_SAMPLING_RATE_COMMAND    		= (byte) 0x3A;
	public static final byte MAG_SAMPLING_RATE_RESPONSE       		= (byte) 0x3B;
	public static final byte GET_MAG_SAMPLING_RATE_COMMAND    		= (byte) 0x3C;
	
	public static final byte SET_ACCEL_SAMPLING_RATE_COMMAND  		= (byte) 0x40;
	public static final byte ACCEL_SAMPLING_RATE_RESPONSE  			= (byte) 0x41;
	public static final byte GET_ACCEL_SAMPLING_RATE_COMMAND  		= (byte) 0x42;
	
	public static final byte SET_LSM303DLHC_ACCEL_LPMODE_COMMAND 	= (byte) 0x43;
	public static final byte LSM303DLHC_ACCEL_LPMODE_RESPONSE		= (byte) 0x44;
	public static final byte GET_LSM303DLHC_ACCEL_LPMODE_COMMAND 	= (byte) 0x45;
	
	public static final byte SET_LSM303DLHC_ACCEL_HRMODE_COMMAND	= (byte) 0x46;
	public static final byte LSM303DLHC_ACCEL_HRMODE_RESPONSE		= (byte) 0x47;
	public static final byte GET_LSM303DLHC_ACCEL_HRMODE_COMMAND 	= (byte) 0x48;
	
    public static final Map<Byte, BtCommandDetails> mBtGetCommandMap;
    static {
        Map<Byte, BtCommandDetails> aMap = new LinkedHashMap<Byte, BtCommandDetails>();
        aMap.put(GET_ACCEL_SENSITIVITY_COMMAND, new BtCommandDetails(GET_ACCEL_SENSITIVITY_COMMAND, "GET_ACCEL_SENSITIVITY_COMMAND", ACCEL_SENSITIVITY_RESPONSE));
        aMap.put(GET_MAG_CALIBRATION_COMMAND, new BtCommandDetails(GET_MAG_CALIBRATION_COMMAND, "GET_MAG_CALIBRATION_COMMAND", MAG_CALIBRATION_RESPONSE));
        aMap.put(GET_LSM303DLHC_ACCEL_CALIBRATION_COMMAND, new BtCommandDetails(GET_LSM303DLHC_ACCEL_CALIBRATION_COMMAND, "GET_LSM303DLHC_ACCEL_CALIBRATION_COMMAND", LSM303DLHC_ACCEL_CALIBRATION_RESPONSE));
        aMap.put(GET_MAG_GAIN_COMMAND, new BtCommandDetails(GET_MAG_GAIN_COMMAND, "GET_MAG_GAIN_COMMAND", MAG_GAIN_RESPONSE));
        aMap.put(GET_MAG_SAMPLING_RATE_COMMAND, new BtCommandDetails(GET_MAG_SAMPLING_RATE_COMMAND, "GET_MAG_SAMPLING_RATE_COMMAND", MAG_SAMPLING_RATE_RESPONSE));
        aMap.put(GET_ACCEL_SAMPLING_RATE_COMMAND, new BtCommandDetails(GET_ACCEL_SAMPLING_RATE_COMMAND, "GET_ACCEL_SAMPLING_RATE_COMMAND", ACCEL_SAMPLING_RATE_RESPONSE));
        aMap.put(GET_LSM303DLHC_ACCEL_LPMODE_COMMAND, new BtCommandDetails(GET_LSM303DLHC_ACCEL_LPMODE_COMMAND, "GET_LSM303DLHC_ACCEL_LPMODE_COMMAND", LSM303DLHC_ACCEL_LPMODE_RESPONSE));
        aMap.put(GET_LSM303DLHC_ACCEL_HRMODE_COMMAND, new BtCommandDetails(GET_LSM303DLHC_ACCEL_HRMODE_COMMAND, "GET_LSM303DLHC_ACCEL_HRMODE_COMMAND", LSM303DLHC_ACCEL_HRMODE_RESPONSE));
        mBtGetCommandMap = Collections.unmodifiableMap(aMap);
    }
    
    public static final Map<Byte, BtCommandDetails> mBtSetCommandMap;
    static {
        Map<Byte, BtCommandDetails> aMap = new LinkedHashMap<Byte, BtCommandDetails>();
        aMap.put(SET_ACCEL_SENSITIVITY_COMMAND, new BtCommandDetails(SET_ACCEL_SENSITIVITY_COMMAND, "SET_ACCEL_SENSITIVITY_COMMAND"));
        aMap.put(SET_MAG_CALIBRATION_COMMAND, new BtCommandDetails(SET_MAG_CALIBRATION_COMMAND, "SET_MAG_CALIBRATION_COMMAND"));
        aMap.put(SET_LSM303DLHC_ACCEL_CALIBRATION_COMMAND, new BtCommandDetails(SET_LSM303DLHC_ACCEL_CALIBRATION_COMMAND, "SET_LSM303DLHC_ACCEL_CALIBRATION_COMMAND"));
        aMap.put(SET_MAG_GAIN_COMMAND, new BtCommandDetails(SET_MAG_GAIN_COMMAND, "SET_MAG_GAIN_COMMAND"));
        aMap.put(SET_MAG_SAMPLING_RATE_COMMAND, new BtCommandDetails(SET_MAG_SAMPLING_RATE_COMMAND, "SET_MAG_SAMPLING_RATE_COMMAND"));
        aMap.put(SET_ACCEL_SAMPLING_RATE_COMMAND, new BtCommandDetails(SET_ACCEL_SAMPLING_RATE_COMMAND, "SET_ACCEL_SAMPLING_RATE_COMMAND"));
        aMap.put(SET_LSM303DLHC_ACCEL_LPMODE_COMMAND, new BtCommandDetails(SET_LSM303DLHC_ACCEL_LPMODE_COMMAND, "SET_LSM303DLHC_ACCEL_LPMODE_COMMAND"));
        aMap.put(SET_LSM303DLHC_ACCEL_HRMODE_COMMAND, new BtCommandDetails(SET_LSM303DLHC_ACCEL_HRMODE_COMMAND, "SET_LSM303DLHC_ACCEL_HRMODE_COMMAND"));
        mBtSetCommandMap = Collections.unmodifiableMap(aMap);
    }
	//--------- Bluetooth commands end --------------
	

	//--------- Configuration options start --------------
	public static final String[] ListofAccelRange={"+/- 2g","+/- 4g","+/- 8g","+/- 16g"};  
	public static final Integer[] ListofLSM303DLHCAccelRangeConfigValues={0,1,2,3};  
	
	public static final String[] ListofMagRange={"+/- 1.3Ga","+/- 1.9Ga","+/- 2.5Ga","+/- 4.0Ga","+/- 4.7Ga","+/- 5.6Ga","+/- 8.1Ga"}; 
	public static final Integer[] ListofMagRangeConfigValues={1,2,3,4,5,6,7}; // no '0' option  
	
	public static final String[] ListofLSM303DLHCAccelRate={"Power-down","1.0Hz","10.0Hz","25.0Hz","50.0Hz","100.0Hz","200.0Hz","400.0Hz","1344.0Hz"};
	public static final Integer[] ListofLSM303DLHCAccelRateConfigValues={0,1,2,3,4,5,6,7,9};
	
	public static final String[] ListofLSM303DLHCMagRate={"0.75Hz","1.5Hz","3.0Hz","7.5Hz","15.0Hz","30.0Hz","75.0Hz","220.0Hz"};
	public static final Integer[] ListofLSM303DLHCMagRateConfigValues={0,1,2,3,4,5,6,7};
	
	public static final String[] ListofLSM303DLHCAccelRateLpm={"Power-down","1Hz","10Hz","25Hz","50Hz","100Hz","200Hz","400Hz","1620Hz","5376Hz"}; // 1620Hz and 5376Hz are only available in low-power mode
	public static final Integer[] ListofLSM303DLHCAccelRateLpmConfigValues={0,1,2,3,4,5,6,7,8,9};
	
	public static final SensorConfigOptionDetails configOptionAccelRange = new SensorConfigOptionDetails(
			ListofAccelRange, 
			ListofLSM303DLHCAccelRangeConfigValues, 
			SensorConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW);
	
	public static final SensorConfigOptionDetails configOptionMagRange = new SensorConfigOptionDetails(
			ListofMagRange, 
			ListofMagRangeConfigValues, 
			SensorConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW);
	
	public static final SensorConfigOptionDetails configOptionAccelRate = new SensorConfigOptionDetails(
			ListofLSM303DLHCAccelRate, 
			ListofLSM303DLHCAccelRateConfigValues, 
			SensorConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW);
	
	public static final SensorConfigOptionDetails configOptionMagRate = new SensorConfigOptionDetails(
			ListofLSM303DLHCMagRate, 
			ListofLSM303DLHCMagRateConfigValues, 
			SensorConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW);
	
	public static final SensorConfigOptionDetails configOptionAccelRateLpm = new SensorConfigOptionDetails(
			ListofLSM303DLHCAccelRateLpm, 
			ListofLSM303DLHCAccelRateLpmConfigValues, 
			SensorConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW);
	//--------- Configuration options end --------------
	
	
	//--------- Sensor info start --------------
	public static final SensorDetailsRef sensorLSM303DLHCAccel = new SensorDetailsRef(
			0x1000, //== Configuration.Shimmer3.SensorBitmap.SENSOR_D_ACCEL will be: SensorBitmap.SENSOR_D_ACCEL
			0x1000, //== Configuration.Shimmer3.SensorBitmap.SENSOR_D_ACCEL will be: SensorBitmap.SENSOR_D_ACCEL
			GuiLabelSensors.ACCEL_WR,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW,
			Arrays.asList(GuiLabelConfig.LSM303DLHC_ACCEL_RANGE,GuiLabelConfig.LSM303DLHC_ACCEL_RATE),
			Arrays.asList(ObjectClusterSensorName.ACCEL_WR_X,
					ObjectClusterSensorName.ACCEL_WR_Y,
					ObjectClusterSensorName.ACCEL_WR_Z));
	
	public static final SensorDetailsRef sensorLSM303DLHCMag = new SensorDetailsRef(
			0x20, //== Configuration.Shimmer3.SensorBitmap.SENSOR_MAG will be: SensorBitmap.SENSOR_MAG, 
			0x20, //== Configuration.Shimmer3.SensorBitmap.SENSOR_MAG will be: SensorBitmap.SENSOR_MAG, 
			GuiLabelSensors.MAG,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW,
			Arrays.asList(GuiLabelConfig.LSM303DLHC_MAG_RANGE,GuiLabelConfig.LSM303DLHC_MAG_RATE),
			//MAG channel order is XZY instead of XYZ
			Arrays.asList(ObjectClusterSensorName.MAG_X,
					ObjectClusterSensorName.MAG_Z,
					ObjectClusterSensorName.MAG_Y));
	
	public static final Map<Integer, SensorDetailsRef> mSensorMapRef;
    static {
        Map<Integer, SensorDetailsRef> aMap = new LinkedHashMap<Integer, SensorDetailsRef>();
        aMap.put(Configuration.Shimmer3.SensorMapKey.SHIMMER_LSM303DLHC_ACCEL, SensorLSM303.sensorLSM303DLHCAccel);  
        aMap.put(Configuration.Shimmer3.SensorMapKey.SHIMMER_LSM303DLHC_MAG, SensorLSM303.sensorLSM303DLHCMag);	
		mSensorMapRef = Collections.unmodifiableMap(aMap);
    }
	//--------- Sensor info end --------------
	
	
	//--------- Channel info start --------------
    public static final ChannelDetails channelLSM303AccelX = new ChannelDetails(
			ObjectClusterSensorName.ACCEL_WR_X,
			ObjectClusterSensorName.ACCEL_WR_X,
			DatabaseChannelHandles.WR_ACC_X,
			CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.LSB,
			CHANNEL_UNITS.METER_PER_SECOND_SQUARE,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
    
    public static final ChannelDetails channelLSM303AccelY = new ChannelDetails(
			ObjectClusterSensorName.ACCEL_WR_Y,
			ObjectClusterSensorName.ACCEL_WR_Y,
			DatabaseChannelHandles.WR_ACC_Y,
			CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.LSB,
			CHANNEL_UNITS.METER_PER_SECOND_SQUARE,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
    
    public static final ChannelDetails channelLSM303AccelZ = new ChannelDetails(
			ObjectClusterSensorName.ACCEL_WR_Z,
			ObjectClusterSensorName.ACCEL_WR_Z,
			DatabaseChannelHandles.WR_ACC_Z,
			CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.LSB,
			CHANNEL_UNITS.METER_PER_SECOND_SQUARE,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
    
    public static final ChannelDetails channelLSM303MagX = new ChannelDetails(
			ObjectClusterSensorName.MAG_X,
			ObjectClusterSensorName.MAG_X,
			DatabaseChannelHandles.MAG_X,
			CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.MSB,
			CHANNEL_UNITS.LOCAL_FLUX,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
	
	public static final ChannelDetails channelLSM303MagY = new ChannelDetails(
			ObjectClusterSensorName.MAG_Y,
			ObjectClusterSensorName.MAG_Y,
			DatabaseChannelHandles.MAG_Y,
			CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.MSB,
			CHANNEL_UNITS.LOCAL_FLUX,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
	
	public static final ChannelDetails channelLSM303MagZ = new ChannelDetails(
			ObjectClusterSensorName.MAG_Z,
			ObjectClusterSensorName.MAG_Z,
			DatabaseChannelHandles.MAG_Z,
			CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.MSB,
			CHANNEL_UNITS.LOCAL_FLUX,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
    
    public static final Map<String, ChannelDetails> mChannelMapRef;
    static {
        Map<String, ChannelDetails> aMap = new LinkedHashMap<String, ChannelDetails>();
        aMap.put(SensorLSM303.ObjectClusterSensorName.ACCEL_WR_X, SensorLSM303.channelLSM303AccelX);
        aMap.put(SensorLSM303.ObjectClusterSensorName.ACCEL_WR_Y, SensorLSM303.channelLSM303AccelY);
        aMap.put(SensorLSM303.ObjectClusterSensorName.ACCEL_WR_Z, SensorLSM303.channelLSM303AccelZ);
        aMap.put(SensorLSM303.ObjectClusterSensorName.MAG_X, SensorLSM303.channelLSM303MagX);
        aMap.put(SensorLSM303.ObjectClusterSensorName.MAG_Z, SensorLSM303.channelLSM303MagZ);
        aMap.put(SensorLSM303.ObjectClusterSensorName.MAG_Y, SensorLSM303.channelLSM303MagY);
		mChannelMapRef = Collections.unmodifiableMap(aMap);
    }
	//--------- Channel info end --------------
    
    
    //--------- Constructors for this class start --------------
    public SensorLSM303(ShimmerVerObject svo) {
		super(svo);
		setSensorName(SENSORS.LSM303.toString());
	}
   //--------- Constructors for this class end --------------

	
	//--------- Abstract methods implemented start --------------
	@Override 
	public void generateSensorMap(ShimmerVerObject svo) {
		super.createLocalSensorMapWithCustomParser(mSensorMapRef, mChannelMapRef);
	}

	
	@Override 
	public void generateConfigOptionsMap(ShimmerVerObject svo) {
		mConfigOptionsMap.put(GuiLabelConfig.LSM303DLHC_ACCEL_RANGE, configOptionAccelRange);
		mConfigOptionsMap.put(GuiLabelConfig.LSM303DLHC_MAG_RANGE, configOptionMagRange);
		mConfigOptionsMap.put(GuiLabelConfig.LSM303DLHC_ACCEL_RATE, configOptionAccelRate);
		mConfigOptionsMap.put(GuiLabelConfig.LSM303DLHC_MAG_RATE, configOptionMagRate);
		mConfigOptionsMap.put(GuiLabelConfig.LSM303DLHC_ACCEL_LPM, configOptionAccelRateLpm);
	}
	
	@Override 
	public void generateSensorGroupMapping(ShimmerVerObject svo) {
		mSensorGroupingMap = new LinkedHashMap<String, SensorGroupingDetails>();
		if(svo.mHardwareVersion==HW_ID.SHIMMER_3 || svo.mHardwareVersion==HW_ID.SHIMMER_4_SDK){
			mSensorGroupingMap.put(GuiLabelSensorTiles.WIDE_RANGE_ACCEL, new SensorGroupingDetails(
					GuiLabelSensorTiles.WIDE_RANGE_ACCEL,
					Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_LSM303DLHC_ACCEL),
					CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW));
			mSensorGroupingMap.put(GuiLabelSensorTiles.MAG, new SensorGroupingDetails(
					GuiLabelSensorTiles.MAG,
					Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_LSM303DLHC_MAG),
					CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW));
		}
		super.updateSensorGroupingMap();	
	}	

	
	@Override 
	public ObjectCluster processDataCustom(SensorDetails sensorDetails, byte[] rawData, COMMUNICATION_TYPE commType, ObjectCluster objectCluster, boolean isTimeSyncEnabled, long pcTimestamp) {
		
		// process data originating from the Shimmer
		objectCluster = sensorDetails.processDataCommon(rawData, commType, objectCluster, isTimeSyncEnabled, pcTimestamp);
		
		//Calibration
		if(mEnableCalibration){
			// get uncalibrated data for each (sub)sensor
			if(sensorDetails.mSensorDetailsRef.mGuiFriendlyLabel.equals(GuiLabelSensors.ACCEL_WR)){
				double[] unCalibratedAccelWrData = new double[3];
				for (ChannelDetails channelDetails:sensorDetails.mListOfChannels){
					//Uncalibrated Accelerometer data
					if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.ACCEL_WR_X)){
						unCalibratedAccelWrData[0] = ((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(channelDetails.mObjectClusterName), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
					}
					else if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.ACCEL_WR_Y)){
						unCalibratedAccelWrData[1]  = ((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(channelDetails.mObjectClusterName), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
					}
					else if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.ACCEL_WR_Z)){
						unCalibratedAccelWrData[2]  = ((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(channelDetails.mObjectClusterName), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
					}
				}
				
				double[] calibratedAccelWrData = UtilCalibration.calibrateInertialSensorData(unCalibratedAccelWrData, mAlignmentMatrixWRAccel, mSensitivityMatrixWRAccel, mOffsetVectorWRAccel);
	
				//Add calibrated data to Object cluster
				if(sensorDetails.mSensorDetailsRef.mGuiFriendlyLabel.equals(GuiLabelSensors.ACCEL_WR)){	
					for (ChannelDetails channelDetails:sensorDetails.mListOfChannels){
						if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.ACCEL_WR_X)){
							objectCluster.addCalData(channelDetails, calibratedAccelWrData[0], objectCluster.getIndexKeeper()-3);
						}
						else if(channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.ACCEL_WR_Y)){
							objectCluster.addCalData(channelDetails, calibratedAccelWrData[1], objectCluster.getIndexKeeper()-2);
						}
						else if(channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.ACCEL_WR_Z)){
							objectCluster.addCalData(channelDetails, calibratedAccelWrData[2], objectCluster.getIndexKeeper()-1);
						}
					}
				}
	
				//Debugging
				super.consolePrintChannelsCal(objectCluster, Arrays.asList(
						new String[]{ObjectClusterSensorName.ACCEL_WR_X, CHANNEL_TYPE.UNCAL.toString()}, 
						new String[]{ObjectClusterSensorName.ACCEL_WR_Y, CHANNEL_TYPE.UNCAL.toString()}, 
						new String[]{ObjectClusterSensorName.ACCEL_WR_Z, CHANNEL_TYPE.UNCAL.toString()}, 
						new String[]{ObjectClusterSensorName.ACCEL_WR_X, CHANNEL_TYPE.CAL.toString()}, 
						new String[]{ObjectClusterSensorName.ACCEL_WR_Y, CHANNEL_TYPE.CAL.toString()},
						new String[]{ObjectClusterSensorName.ACCEL_WR_Z, CHANNEL_TYPE.CAL.toString()}));
	
			}
			else if(sensorDetails.mSensorDetailsRef.mGuiFriendlyLabel.equals(GuiLabelSensors.MAG)){
				double[] unCalibratedMagData = new double[3];
				for (ChannelDetails channelDetails:sensorDetails.mListOfChannels){
					//Uncalibrated Magnetometer data
					if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.MAG_X)){
						unCalibratedMagData[0] = ((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(channelDetails.mObjectClusterName), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
					}
					else if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.MAG_Y)){
						unCalibratedMagData[1]  = ((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(channelDetails.mObjectClusterName), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
					}
					else if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.MAG_Z)){
						unCalibratedMagData[2]  = ((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(channelDetails.mObjectClusterName), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
					}	
				}
				
				double[] calibratedMagData = UtilCalibration.calibrateInertialSensorData(unCalibratedMagData, mAlignmentMatrixMagnetometer, mSensitivityMatrixMagnetometer, mOffsetVectorMagnetometer);
	
				if(sensorDetails.mSensorDetailsRef.mGuiFriendlyLabel.equals(GuiLabelSensors.MAG)){
					for (ChannelDetails channelDetails:sensorDetails.mListOfChannels){
						if(channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.MAG_X)){
							objectCluster.addCalData(channelDetails, calibratedMagData[0], objectCluster.getIndexKeeper()-3);
						}
						else if(channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.MAG_Y)){
							objectCluster.addCalData(channelDetails, calibratedMagData[1], objectCluster.getIndexKeeper()-2);
						}
						else if(channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.MAG_Z)){
							objectCluster.addCalData(channelDetails, calibratedMagData[2], objectCluster.getIndexKeeper()-1);
						}
					}
				}
				
				//Debugging
				super.consolePrintChannelsCal(objectCluster, Arrays.asList(
						new String[]{ObjectClusterSensorName.MAG_X, CHANNEL_TYPE.UNCAL.toString()}, 
						new String[]{ObjectClusterSensorName.MAG_Y, CHANNEL_TYPE.UNCAL.toString()}, 
						new String[]{ObjectClusterSensorName.MAG_Z, CHANNEL_TYPE.UNCAL.toString()}, 
						new String[]{ObjectClusterSensorName.MAG_X, CHANNEL_TYPE.CAL.toString()}, 
						new String[]{ObjectClusterSensorName.MAG_Y, CHANNEL_TYPE.CAL.toString()},
						new String[]{ObjectClusterSensorName.MAG_Z, CHANNEL_TYPE.CAL.toString()}));
			}
		}
		return objectCluster;
	}

	
	@Override 
	public void infoMemByteArrayGenerate(ShimmerDevice shimmerDevice, byte[] mInfoMemBytes) {//XXX - What is "ShimmerDevice shimmerDevice" doing here? 
		int idxConfigSetupByte0 =              		6; 
		int idxConfigSetupByte2 =              		8;
//		int idxLSM303DLHCAccelCalibration =    	   94; 
//		int idxLSM303DLHCMagCalibration =          73;
		//fix for newer firmware -> see InfomemLayoutShimmer3
		int idxLSM303DLHCMagCalibration =   76;
		int idxLSM303DLHCAccelCalibration = 97;
		int bitShiftLSM303DLHCAccelSamplingRate =   4;
		int bitShiftLSM303DLHCAccelRange =          2;
		int bitShiftLSM303DLHCAccelLPM =            1;
		int bitShiftLSM303DLHCAccelHRM =            0;
		int bitShiftLSM303DLHCMagRange =            5;
		int bitShiftLSM303DLHCMagSamplingRate =     2;
		int maskLSM303DLHCAccelSamplingRate =    0x0F;   
		int maskLSM303DLHCAccelRange =           0x03;
		int maskLSM303DLHCAccelLPM =             0x01;
		int maskLSM303DLHCAccelHRM =             0x01;
		int maskLSM303DLHCMagRange =             0x07;
		int maskLSM303DLHCMagSamplingRate =      0x07;
		int lengthGeneralCalibrationBytes =        21;
		
		//idxConfigSetupByte0 
		mInfoMemBytes[idxConfigSetupByte0] = (byte) ((mLSM303DigitalAccelRate & maskLSM303DLHCAccelSamplingRate) << bitShiftLSM303DLHCAccelSamplingRate);
		mInfoMemBytes[idxConfigSetupByte0] |= (byte) ((mAccelRange & maskLSM303DLHCAccelRange) << bitShiftLSM303DLHCAccelRange);
		if(mLowPowerAccelWR) {
			mInfoMemBytes[idxConfigSetupByte0] |= (maskLSM303DLHCAccelLPM << bitShiftLSM303DLHCAccelLPM);
		}
		if(mHighResAccelWR) {
			mInfoMemBytes[idxConfigSetupByte0] |= (maskLSM303DLHCAccelHRM << bitShiftLSM303DLHCAccelHRM);
		}
		
		//idxConfigSetupByte2
		mInfoMemBytes[idxConfigSetupByte2] = (byte) ((mMagRange & maskLSM303DLHCMagRange) << bitShiftLSM303DLHCMagRange);
		mInfoMemBytes[idxConfigSetupByte2] |= (byte) ((mLSM303MagRate & maskLSM303DLHCMagSamplingRate) << bitShiftLSM303DLHCMagSamplingRate);
		
		// LSM303DLHC Digital Accel Calibration Parameters
		byte[] bufferCalibrationParameters = generateCalParamLSM303DLHCAccel();
		System.arraycopy(bufferCalibrationParameters, 0, mInfoMemBytes, idxLSM303DLHCAccelCalibration, lengthGeneralCalibrationBytes);
		
		// LSM303DLHC Magnetometer Calibration Parameters
		bufferCalibrationParameters = generateCalParamLSM303DLHCMag();
		System.arraycopy(bufferCalibrationParameters, 0, mInfoMemBytes, idxLSM303DLHCMagCalibration, lengthGeneralCalibrationBytes);
	}

	
	@Override 
	public void infoMemByteArrayParse(ShimmerDevice shimmerDevice, byte[] mInfoMemBytes) {//XXX - What is "ShimmerDevice shimmerDevice" doing here? 
		int idxConfigSetupByte0 =              		6; 
		int idxConfigSetupByte2 =              		8;
//		int idxLSM303DLHCAccelCalibration =    	   94; 
//		int idxLSM303DLHCMagCalibration =          73;
		//fix for newer firmware -> see InfomemLayoutShimmer3
		int idxLSM303DLHCMagCalibration =   76;
		int idxLSM303DLHCAccelCalibration = 97;
		int bitShiftLSM303DLHCAccelSamplingRate =   4;
		int bitShiftLSM303DLHCAccelRange =          2;
		int bitShiftLSM303DLHCAccelLPM =            1;
		int bitShiftLSM303DLHCAccelHRM =            0;
		int bitShiftLSM303DLHCMagRange =            5;
		int bitShiftLSM303DLHCMagSamplingRate =     2;
		int maskLSM303DLHCAccelSamplingRate =    0x0F;   
		int maskLSM303DLHCAccelRange =           0x03;
		int maskLSM303DLHCAccelLPM =             0x01;
		int maskLSM303DLHCAccelHRM =             0x01;
		int maskLSM303DLHCMagRange =             0x07;
		int maskLSM303DLHCMagSamplingRate =      0x07;
		int lengthGeneralCalibrationBytes =        21;
		
		//idxConfigSetupByte0 
		mLSM303DigitalAccelRate = (mInfoMemBytes[idxConfigSetupByte0] >> bitShiftLSM303DLHCAccelSamplingRate) & maskLSM303DLHCAccelSamplingRate; 
		mAccelRange = (mInfoMemBytes[idxConfigSetupByte0] >> bitShiftLSM303DLHCAccelRange) & maskLSM303DLHCAccelRange;
		if(((mInfoMemBytes[idxConfigSetupByte0] >> bitShiftLSM303DLHCAccelLPM) & maskLSM303DLHCAccelLPM) == maskLSM303DLHCAccelLPM) {
			mLowPowerAccelWR = true;
		}
		else {
			mLowPowerAccelWR = false;
		}
		if(((mInfoMemBytes[idxConfigSetupByte0] >> bitShiftLSM303DLHCAccelHRM) & maskLSM303DLHCAccelHRM) == maskLSM303DLHCAccelHRM) {
			mHighResAccelWR = true;
		}
		else {
			mHighResAccelWR = false;
		}
		
		//idxConfigSetupByte2
		mMagRange = (mInfoMemBytes[idxConfigSetupByte2] >> bitShiftLSM303DLHCMagRange) & maskLSM303DLHCMagRange;
		mLSM303MagRate = (mInfoMemBytes[idxConfigSetupByte2] >> bitShiftLSM303DLHCMagSamplingRate) & maskLSM303DLHCMagSamplingRate;
		checkLowPowerMag(); // check rate to determine if Sensor is in LPM mode
		
		// LSM303DLHC Digital Accel Calibration Parameters
		byte[] bufferCalibrationParameters = new byte[lengthGeneralCalibrationBytes];
		System.arraycopy(mInfoMemBytes, idxLSM303DLHCAccelCalibration, bufferCalibrationParameters, 0 , lengthGeneralCalibrationBytes);
		retrieveKinematicCalibrationParametersFromPacket(bufferCalibrationParameters, LSM303DLHC_ACCEL_CALIBRATION_RESPONSE);
		
		// LSM303DLHC Magnetometer Calibration Parameters
		bufferCalibrationParameters = new byte[lengthGeneralCalibrationBytes];
		System.arraycopy(mInfoMemBytes, idxLSM303DLHCMagCalibration, bufferCalibrationParameters, 0 , lengthGeneralCalibrationBytes);
		retrieveKinematicCalibrationParametersFromPacket(bufferCalibrationParameters, MAG_CALIBRATION_RESPONSE);
	}

	
	@Override 
	public Object setConfigValueUsingConfigLabel(String componentName, Object valueToSet) {
		Object returnValue = null;
		
		switch(componentName){
		
			case(GuiLabelConfig.LSM303DLHC_ACCEL_LPM):
				setLowPowerAccelWR((boolean)valueToSet);
				break;
				
			case(GuiLabelConfig.LSM303DLHC_MAG_LPM):
				setLowPowerMag((boolean)valueToSet);
				break;
				
			case(GuiLabelConfig.LSM303DLHC_ACCEL_RANGE):
				setDigitalAccelRange((int)valueToSet);
				break;
				
			case(GuiLabelConfig.LSM303DLHC_MAG_RANGE):
				setLSM303MagRange((int)valueToSet);
				break;
				
			case(GuiLabelConfig.LSM303DLHC_ACCEL_RATE):
				setLSM303DigitalAccelRate((int)valueToSet);
				break;
				
			case(GuiLabelConfig.LSM303DLHC_MAG_RATE):
				setLSM303MagRate((int)valueToSet);
				break;
		}		
		return returnValue;
	}

	
	@Override 
	public Object getConfigValueUsingConfigLabel(String componentName) {
		Object returnValue = null;
		
		if(componentName.equals(GuiLabelConfig.LSM303DLHC_ACCEL_RATE)){
        	checkConfigOptionValues(componentName);
        }
		
		switch(componentName){
		
			case(GuiLabelConfig.LSM303DLHC_ACCEL_LPM):
				returnValue = isLSM303DigitalAccelLPM();
	        	break;
	        	
			case(GuiLabelConfig.LSM303DLHC_MAG_LPM):
				returnValue = checkLowPowerMag();
	        	break;
	        	
			case(GuiLabelConfig.LSM303DLHC_ACCEL_RANGE): 
				returnValue = getAccelRange();
		    	break;
		    	
			case(GuiLabelConfig.LSM303DLHC_MAG_RANGE):
				//TODO check below and commented out code (RS (20/5/2016): Same as in ShimmerObject.)
				returnValue = getMagRange();
			
		//						// firmware sets mag range to 7 (i.e. index 6 in combobox) if user set mag range to 0 in config file
		//						if(getMagRange() == 0) cmBx.setSelectedIndex(6);
		//						else cmBx.setSelectedIndex(getMagRange()-1);
				break;
			
			case(GuiLabelConfig.LSM303DLHC_ACCEL_RATE): 
				int configValue = getLSM303DigitalAccelRate(); 
				 
		    	if(!isLSM303DigitalAccelLPM()) {
		        	if(configValue==8) {
		        		//TODO:
		        		/*RS (20/5/2016): Why returning a different value?
		        		 * In the Set-method the compatibility-check for Accel Rates supported for Low Power Mode is made.
		        		 * In this get-method the it should just read/get the value, not manipulating it.
		        		 * */
		        		configValue = 9;
		        	}
		    	}
				returnValue = configValue;
				break;
		
			case(GuiLabelConfig.LSM303DLHC_MAG_RATE):
				returnValue = getLSM303MagRate();
	        	break;
			
		}
		return returnValue;
	}

	
	@Override 
	public void setSensorSamplingRate(double samplingRateHz) {
		//set sampling rate of the sensors as close to the Shimmer sampling rate as possible (sensor sampling rate >= shimmer sampling rate) 
		setLSM303AccelRateFromFreq(samplingRateHz);
		setLSM303MagRateFromFreq(samplingRateHz);
	}
	
	@Override 
	public boolean setDefaultConfigForSensor(int sensorMapKey, boolean isSensorEnabled) {
		if(mSensorMap.containsKey(sensorMapKey)){
			if(sensorMapKey==Configuration.Shimmer3.SensorMapKey.SHIMMER_LSM303DLHC_ACCEL) {
				setDefaultLsm303dlhcAccelSensorConfig(isSensorEnabled);		
			}
			else if(sensorMapKey==Configuration.Shimmer3.SensorMapKey.SHIMMER_LSM303DLHC_MAG) {
				setDefaultLsm303dlhcMagSensorConfig(isSensorEnabled);
			}
			return true;
		}
		return false;
	}
	
	
	@Override 
	public boolean checkConfigOptionValues(String stringKey) {		
		if(mConfigOptionsMap.containsKey(stringKey)){
			if(stringKey==GuiLabelConfig.LSM303DLHC_ACCEL_RATE){
				if(isLSM303DigitalAccelLPM()) {
					mConfigOptionsMap.get(stringKey).setIndexOfValuesToUse(SensorConfigOptionDetails.VALUE_INDEXES.LSM303DLHC_ACCEL_RATE.IS_LPM);
				}
				else {
					mConfigOptionsMap.get(stringKey).setIndexOfValuesToUse(SensorConfigOptionDetails.VALUE_INDEXES.LSM303DLHC_ACCEL_RATE.NOT_LPM);
					// double check that rate is compatible with LPM (8 not compatible so set to higher rate) 
					setLSM303DigitalAccelRate(mLSM303DigitalAccelRate);
				}
			}		
			return true;
		}
		return false;
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
		
//		 switch(componentName){
//			case(Configuration.Shimmer3.GuiLabelConfig.LSM303DLHC_ACCEL_RANGE):
//				mAccelRange = ((int)valueToSet);
//				//
//			if(mFirmwareType==FW_ID.BTSTREAM||mFirmwareType==FW_ID.LOGANDSTREAM){ //mcommtype
//				actionsetting.mActionByteArray = new byte[]{ShimmerObject.SET_ACCEL_SENSITIVITY_COMMAND, (byte)mAccelRange};
//				return actionsetting;
//			} else if (mFirmwareType==FW_ID.SDLOG){
//				//compatiblity check and instruction generation
//			}
//        	break;
		
//		public static final byte SET_ACCEL_SENSITIVITY_COMMAND    		= (byte) 0x09;
//		public static final byte SET_MAG_CALIBRATION_COMMAND      		= (byte) 0x17;		
//		public static final byte SET_LSM303DLHC_ACCEL_CALIBRATION_COMMAND = (byte) 0x1A;
//		public static final byte SET_MAG_GAIN_COMMAND             		= (byte) 0x37;	
//		public static final byte SET_MAG_SAMPLING_RATE_COMMAND    		= (byte) 0x3A;		
//		public static final byte SET_ACCEL_SAMPLING_RATE_COMMAND  		= (byte) 0x40;	
//		public static final byte SET_LSM303DLHC_ACCEL_LPMODE_COMMAND 	= (byte) 0x43;	
//		public static final byte SET_LSM303DLHC_ACCEL_HRMODE_COMMAND	= (byte) 0x46;
//		public boolean mLowPowerAccelWR = false;
//		public boolean mHighResAccelWR = false;
//		
//		public int mAccelRange = 0;
//		public int mLSM303DigitalAccelRate = 0;
//		public int mMagRange = 1;
//		public int mLSM303MagRate = 4;
		
		//Might be used like this - RS 
//		switch(componentName){
//			case(GuiLabelConfig.LSM303DLHC_ACCEL_RANGE):
//				mAccelRange = ((int)valueToSet);
//				if(mFirmwareType==FW_ID.BTSTREAM||mFirmwareType==FW_ID.LOGANDSTREAM){ //mcommtype
//					actionsetting.mActionByteArray = new byte[]{SET_ACCEL_SENSITIVITY_COMMAND, (byte)mAccelRange};
//					return actionsetting;
//				} else if (mFirmwareType==FW_ID.SDLOG){
//					//compatiblity check and instruction generation
//				}
//				break;
//			case(GuiLabelConfig.LSM303DLHC_MAG_RANGE):
//				mMagRange = ((int)valueToSet);
//				if(mFirmwareType==FW_ID.BTSTREAM||mFirmwareType==FW_ID.LOGANDSTREAM){ //mcommtype
//					actionsetting.mActionByteArray = new byte[]{SET_MAG_GAIN_COMMAND, (byte)mMagRange};
//					return actionsetting;
//				} else if (mFirmwareType==FW_ID.SDLOG){
//					//compatiblity check and instruction generation
//				}
//				break;
//			case(GuiLabelConfig.LSM303DLHC_ACCEL_RATE):
//				mLSM303DigitalAccelRate = ((int)valueToSet);
//				if(mFirmwareType==FW_ID.BTSTREAM||mFirmwareType==FW_ID.LOGANDSTREAM){ //mcommtype
//					actionsetting.mActionByteArray = new byte[]{SET_ACCEL_SAMPLING_RATE_COMMAND, (byte)mLSM303DigitalAccelRate};
//					return actionsetting;
//				} else if (mFirmwareType==FW_ID.SDLOG){
//					//compatiblity check and instruction generation
//				}
//				break;
//			case(GuiLabelConfig.LSM303DLHC_MAG_RATE):
//				mLSM303MagRate = ((int)valueToSet);
//				if(mFirmwareType==FW_ID.BTSTREAM||mFirmwareType==FW_ID.LOGANDSTREAM){ //mcommtype
//					actionsetting.mActionByteArray = new byte[]{SET_MAG_SAMPLING_RATE_COMMAND, (byte)mLSM303MagRate};
//					return actionsetting;
//				} else if (mFirmwareType==FW_ID.SDLOG){
//					//compatiblity check and instruction generation
//				}
//				break;
//			case(GuiLabelConfig.LSM303DLHC_ACCEL_LPM):
//				mLowPowerAccelWR = ((boolean)valueToSet);
//			if(mFirmwareType==FW_ID.BTSTREAM||mFirmwareType==FW_ID.LOGANDSTREAM){ //mcommtype
//				actionsetting.mActionByteArray = new byte[]{SET_MAG_SAMPLING_RATE_COMMAND, (byte)mLowPowerAccelWR};
//				return actionsetting;
//			} else if (mFirmwareType==FW_ID.SDLOG){
//				//compatiblity check and instruction generation
//			}
//			break;
//			case(GuiLabelConfig.LSM303DLHC_MAG_LPM):
//				
//				
//			//TODO Above: Do LPM for Accel and Mag as is done in ShimmerObject. Below: Should these settings to be included in here as well? 
//			/*
//			case(GuiLabelConfig.LSM303DLHC_ACCEL_DEFAULT_CALIB):
//			case(GuiLabelConfig.LSM303DLHC_MAG_DEFAULT_CALIB):
//			*/
//		}
		
		return actionsetting;
		
	}
	
	
	@Override
	public void processResponse(Object obj, COMMUNICATION_TYPE commType) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void checkShimmerConfigBeforeConfiguring() {
//		// If Shimmer name is default, update with MAC ID if available.
//		if(mShimmerUserAssignedName.equals(DEFAULT_SHIMMER_NAME)){
//			setDefaultShimmerName();
//		}

		if(!isSensorEnabled(Configuration.Shimmer3.SensorMapKey.SHIMMER_LSM303DLHC_ACCEL)) {
			setDefaultLsm303dlhcAccelSensorConfig(false);
		}
		
		if(!isSensorEnabled(Configuration.Shimmer3.SensorMapKey.SHIMMER_LSM303DLHC_MAG)) {
			setDefaultLsm303dlhcMagSensorConfig(false);
		}
		
	}
	//--------- Abstract methods implemented end --------------


	//--------- Sensor specific methods start --------------
	private byte[] generateCalParamLSM303DLHCAccel(){
		byte[] bufferCalibrationParameters = new byte[21];
		// offsetVector -> buffer offset = 0
		for (int i=0; i<3; i++) {
			bufferCalibrationParameters[0+(i*2)] = (byte) ((((int)mOffsetVectorWRAccel[i][0]) >> 8) & 0xFF);
			bufferCalibrationParameters[0+(i*2)+1] = (byte) ((((int)mOffsetVectorWRAccel[i][0]) >> 0) & 0xFF);
		}
		// sensitivityMatrix -> buffer offset = 6
		for (int i=0; i<3; i++) {
			bufferCalibrationParameters[6+(i*2)] = (byte) ((((int)mSensitivityMatrixWRAccel[i][i]) >> 8) & 0xFF);
			bufferCalibrationParameters[6+(i*2)+1] = (byte) ((((int)mSensitivityMatrixWRAccel[i][i]) >> 0) & 0xFF);
		}
		// alignmentMatrix -> buffer offset = 12
		for (int i=0; i<3; i++) {
			bufferCalibrationParameters[12+(i*3)] = (byte) (((int)(mAlignmentMatrixWRAccel[i][0]*100)) & 0xFF);
			bufferCalibrationParameters[12+(i*3)+1] = (byte) (((int)(mAlignmentMatrixWRAccel[i][1]*100)) & 0xFF);
			bufferCalibrationParameters[12+(i*3)+2] = (byte) (((int)(mAlignmentMatrixWRAccel[i][2]*100)) & 0xFF);
		}
		return bufferCalibrationParameters;
	}
	
	
	private byte[] generateCalParamLSM303DLHCMag(){
		byte[] bufferCalibrationParameters = new byte[21];
		// offsetVector -> buffer offset = 0
		for (int i=0; i<3; i++) {
			bufferCalibrationParameters[0+(i*2)] = (byte) ((((int)mOffsetVectorMagnetometer[i][0]) >> 8) & 0xFF);
			bufferCalibrationParameters[0+(i*2)+1] = (byte) ((((int)mOffsetVectorMagnetometer[i][0]) >> 0) & 0xFF);
		}
		// sensitivityMatrix -> buffer offset = 6
		for (int i=0; i<3; i++) {
			bufferCalibrationParameters[6+(i*2)] = (byte) ((((int)mSensitivityMatrixMagnetometer[i][i]) >> 8) & 0xFF);
			bufferCalibrationParameters[6+(i*2)+1] = (byte) ((((int)mSensitivityMatrixMagnetometer[i][i]) >> 0) & 0xFF);
		}
		// alignmentMatrix -> buffer offset = 12
		for (int i=0; i<3; i++) {
			bufferCalibrationParameters[12+(i*3)] = (byte) (((int)(mAlignmentMatrixMagnetometer[i][0]*100)) & 0xFF);
			bufferCalibrationParameters[12+(i*3)+1] = (byte) (((int)(mAlignmentMatrixMagnetometer[i][1]*100)) & 0xFF);
			bufferCalibrationParameters[12+(i*3)+2] = (byte) (((int)(mAlignmentMatrixMagnetometer[i][2]*100)) & 0xFF);
		}
		return bufferCalibrationParameters;
	}
	
	
	private void retrieveKinematicCalibrationParametersFromPacket(byte[] bufferCalibrationParameters, int packetType) {
		String[] dataType={"i16","i16","i16","i16","i16","i16","i8","i8","i8","i8","i8","i8","i8","i8","i8"}; 
		int[] formattedPacket = UtilParseData.formatDataPacketReverse(bufferCalibrationParameters,dataType); // using the datatype the calibration parameters are converted
		double[] AM=new double[9];
		for (int i=0;i<9;i++){
			AM[i]=((double)formattedPacket[6+i])/100;
		}

		double[][] AlignmentMatrix = {{AM[0],AM[1],AM[2]},{AM[3],AM[4],AM[5]},{AM[6],AM[7],AM[8]}}; 				
		double[][] SensitivityMatrix = {{formattedPacket[3],0,0},{0,formattedPacket[4],0},{0,0,formattedPacket[5]}}; 
		double[][] OffsetVector = {{formattedPacket[0]},{formattedPacket[1]},{formattedPacket[2]}};
		
		//Accelerometer
		if(packetType==LSM303DLHC_ACCEL_CALIBRATION_RESPONSE && checkIfDefaultWideRangeAccelCal(OffsetVector, SensitivityMatrix, AlignmentMatrix)){
			mDefaultCalibrationParametersDigitalAccel = true;
		}
		else if (packetType==LSM303DLHC_ACCEL_CALIBRATION_RESPONSE && SensitivityMatrix[0][0]!=-1) {   //used to be 65535 but changed to -1 as we are now using i16
			mDefaultCalibrationParametersDigitalAccel = false;
			mAlignmentMatrixWRAccel = AlignmentMatrix;
			mOffsetVectorWRAccel = OffsetVector;
			mSensitivityMatrixWRAccel = SensitivityMatrix;
		}
		else if(packetType==LSM303DLHC_ACCEL_CALIBRATION_RESPONSE  && SensitivityMatrix[0][0]==-1){
			//TODO - Use Shimmer3 values or something different? 

			setDefaultCalibrationShimmer3WideRangeAccel();
		}
		
		//Magnetometer
		if(packetType==MAG_CALIBRATION_RESPONSE && checkIfDefaulMagCal(OffsetVector, SensitivityMatrix, AlignmentMatrix)){
			mDefaultCalibrationParametersMag = true;
			mAlignmentMatrixMagnetometer = AlignmentMatrix;
			mOffsetVectorMagnetometer = OffsetVector;
			mSensitivityMatrixMagnetometer = SensitivityMatrix;
		}
		else if (packetType==MAG_CALIBRATION_RESPONSE && SensitivityMatrix[0][0]!=-1) {
			mDefaultCalibrationParametersMag = false;
			mAlignmentMatrixMagnetometer = AlignmentMatrix;
			mOffsetVectorMagnetometer = OffsetVector;
			mSensitivityMatrixMagnetometer = SensitivityMatrix;
		}
		else if(packetType==MAG_CALIBRATION_RESPONSE && SensitivityMatrix[0][0]==-1){
			//TODO - Use Shimmer3 values or something different? 

			setDefaultCalibrationShimmer3Mag();
		}
	}
	
	
	private void setDefaultCalibrationShimmer3WideRangeAccel() {
		//TODO - Use Shimmer3 values or something different? 
	
		mDefaultCalibrationParametersDigitalAccel = true;
		
		if (mAccelRange==0){
			mSensitivityMatrixWRAccel = UtilShimmer.deepCopyDoubleMatrix(SensitivityMatrixWideRangeAccel2gShimmer3);
		} else if (mAccelRange==1){
			mSensitivityMatrixWRAccel = UtilShimmer.deepCopyDoubleMatrix(SensitivityMatrixWideRangeAccel4gShimmer3);
		} else if (mAccelRange==2){
			mSensitivityMatrixWRAccel = UtilShimmer.deepCopyDoubleMatrix(SensitivityMatrixWideRangeAccel8gShimmer3);
		} else if (mAccelRange==3){
			mSensitivityMatrixWRAccel = UtilShimmer.deepCopyDoubleMatrix(SensitivityMatrixWideRangeAccel16gShimmer3);
		}
		
		mAlignmentMatrixWRAccel = UtilShimmer.deepCopyDoubleMatrix(AlignmentMatrixWideRangeAccelShimmer3);
		mOffsetVectorWRAccel = UtilShimmer.deepCopyDoubleMatrix(OffsetVectorWideRangeAccelShimmer3);	
	}

	
	private void setDefaultCalibrationShimmer3Mag() {
		//TODO - Use Shimmer3 values or something different? 
		
		mDefaultCalibrationParametersMag = true;
		
		if (mMagRange==1){
			mSensitivityMatrixMagnetometer = UtilShimmer.deepCopyDoubleMatrix(SensitivityMatrixMag1p3GaShimmer3);
		} else if (mMagRange==2){
			mSensitivityMatrixMagnetometer = UtilShimmer.deepCopyDoubleMatrix(SensitivityMatrixMag1p9GaShimmer3);
		} else if (mMagRange==3){
			mSensitivityMatrixMagnetometer = UtilShimmer.deepCopyDoubleMatrix(SensitivityMatrixMag2p5GaShimmer3);
		} else if (mMagRange==4){
			mSensitivityMatrixMagnetometer = UtilShimmer.deepCopyDoubleMatrix(SensitivityMatrixMag4GaShimmer3);
		} else if (mMagRange==5){
			mSensitivityMatrixMagnetometer = UtilShimmer.deepCopyDoubleMatrix(SensitivityMatrixMag4p7GaShimmer3);
		} else if (mMagRange==6){
			mSensitivityMatrixMagnetometer = UtilShimmer.deepCopyDoubleMatrix(SensitivityMatrixMag5p6GaShimmer3);
		} else if (mMagRange==7){
			mSensitivityMatrixMagnetometer = UtilShimmer.deepCopyDoubleMatrix(SensitivityMatrixMag8p1GaShimmer3);
		}
		
		mAlignmentMatrixMagnetometer = UtilShimmer.deepCopyDoubleMatrix(AlignmentMatrixMagShimmer3);
		mOffsetVectorMagnetometer = UtilShimmer.deepCopyDoubleMatrix(OffsetVectorMagShimmer3);
	}
	
	
	private boolean checkIfDefaultWideRangeAccelCal(double[][] offsetVectorToTest, double[][] sensitivityMatrixToTest, double[][] alignmentMatrixToTest) {
		//TODO - Use Shimmer3 defaults or something different? 

		double[][] offsetVectorToCompare = OffsetVectorWideRangeAccelShimmer3;
		double[][] sensitivityVectorToCompare = SensitivityMatrixWideRangeAccel2gShimmer3;
		double[][] alignmentVectorToCompare = AlignmentMatrixWideRangeAccelShimmer3;
		
		if (mAccelRange==0){
			sensitivityVectorToCompare = SensitivityMatrixWideRangeAccel2gShimmer3;
		} else if (mAccelRange==1){
			sensitivityVectorToCompare = SensitivityMatrixWideRangeAccel4gShimmer3;
		} else if (mAccelRange==2){
			sensitivityVectorToCompare = SensitivityMatrixWideRangeAccel8gShimmer3;
		} else if (mAccelRange==3){
			sensitivityVectorToCompare = SensitivityMatrixWideRangeAccel16gShimmer3;
		}
		
		boolean alignmentPass = Arrays.deepEquals(alignmentVectorToCompare, alignmentMatrixToTest);
		boolean offsetPass = Arrays.deepEquals(offsetVectorToCompare, offsetVectorToTest);
		boolean sensitivityPass = Arrays.deepEquals(sensitivityVectorToCompare, sensitivityMatrixToTest);
		
		if(alignmentPass&&offsetPass&&sensitivityPass){
			return true;
		}
		return false;
	}
	
	
	private boolean checkIfDefaulMagCal(double[][] offsetVectorToTest, double[][] sensitivityMatrixToTest, double[][] alignmentMatrixToTest) {
		//TODO - Use Shimmer3 defaults or something different? 
		
		double[][] offsetVectorToCompare = new double[][]{};
		double[][] sensitivityVectorToCompare = new double[][]{};
		double[][] alignmentVectorToCompare = new double[][]{};


		alignmentVectorToCompare = AlignmentMatrixMagShimmer3;
		offsetVectorToCompare = OffsetVectorMagShimmer3;
		if (mMagRange==1){
			sensitivityVectorToCompare = SensitivityMatrixMag1p3GaShimmer3;
		} else if (mMagRange==2){
			sensitivityVectorToCompare = SensitivityMatrixMag1p9GaShimmer3;
		} else if (mMagRange==3){
			sensitivityVectorToCompare = SensitivityMatrixMag2p5GaShimmer3;
		} else if (mMagRange==4){
			sensitivityVectorToCompare = SensitivityMatrixMag4GaShimmer3;
		} else if (mMagRange==5){
			sensitivityVectorToCompare = SensitivityMatrixMag4p7GaShimmer3;
		} else if (mMagRange==6){
			sensitivityVectorToCompare = SensitivityMatrixMag5p6GaShimmer3;
		} else if (mMagRange==7){
			sensitivityVectorToCompare = SensitivityMatrixMag8p1GaShimmer3;
		}

		boolean alignmentPass = Arrays.deepEquals(alignmentVectorToCompare, alignmentMatrixToTest);
		boolean offsetPass = Arrays.deepEquals(offsetVectorToCompare, offsetVectorToTest);
		boolean sensitivityPass = Arrays.deepEquals(sensitivityVectorToCompare, sensitivityMatrixToTest);

		if(alignmentPass&&offsetPass&&sensitivityPass){
			return true;
		}
		return false;
	}


	private boolean checkLowPowerMag() {
		if(mLSM303MagRate <= 4) {
			mLowPowerMag = true;
		}
		else {
			mLowPowerMag = false;
		}
		return mLowPowerMag;
	}
	
	
	/**XXX
	 * RS (17/05/2016): Two questions with regards to the information below the questions:
	 * 
	 * 		What additional lower power mode is used?
	 * 		Why would the '2g' range not be support by this low power mode -> where is this mentioned in the datasheet?
	 *  
	 * This enables the low power accel option. When not enabled the sampling
	 * rate of the accel is set to the closest value to the actual sampling rate
	 * that it can achieve. In low power mode it defaults to 10Hz. Also and
	 * additional low power mode is used for the LSM303DLHC. This command will
	 * only supports the following Accel range +4g, +8g , +16g
	 */
	public void setHighResAccelWR(boolean enable) {
		mHighResAccelWR = enable;
	}
	
	public void setLowPowerAccelWR(boolean enable){
		mLowPowerAccelWR = enable;
		mHighResAccelWR = !enable;
		setLSM303AccelRateFromFreq(mMaxSetShimmerSamplingRate);
	}
	
	
	public void	setLowPowerMag(boolean enable){
		mLowPowerMag = enable;
		setLSM303MagRateFromFreq(mMaxSetShimmerSamplingRate);
	}
		
	
	public void setDigitalAccelRange(int valueToSet){
		mAccelRange = valueToSet;
	}


	public void setLSM303DigitalAccelRate(int valueToSet) {
		mLSM303DigitalAccelRate = valueToSet;
		//LPM is not compatible with mLSM303DigitalAccelRate == 8, set to next higher rate
		if(mLowPowerAccelWR && (valueToSet==8)) {
			mLSM303DigitalAccelRate = 9;
		}
	}
	

	public void setLSM303MagRange(int valueToSet){
		mMagRange = valueToSet;
	}
	
	
	public void setLSM303MagRate(int valueToSet){
		mLSM303MagRate = valueToSet;
	}
	
	
	private int setLSM303AccelRateFromFreq(double freq) {
		// Unused: 8 = 1.620kHz (only low-power mode), 9 = 1.344kHz (normal-mode) / 5.376kHz (low-power mode)
		
		// Check if channel is enabled 
		if (!isSensorEnabled(Configuration.Shimmer3.SensorMapKey.SHIMMER_LSM303DLHC_ACCEL)) {
			mLSM303DigitalAccelRate = 0; // Power down
			return mLSM303DigitalAccelRate;
		}
		
		if (!mLowPowerAccelWR){
			if (freq<=1){
				mLSM303DigitalAccelRate = 1; // 1Hz
			} else if (freq<=10){
				mLSM303DigitalAccelRate = 2; // 10Hz
			} else if (freq<=25){
				mLSM303DigitalAccelRate = 3; // 25Hz
			} else if (freq<=50){
				mLSM303DigitalAccelRate = 4; // 50Hz
			} else if (freq<=100){
				mLSM303DigitalAccelRate = 5; // 100Hz
			} else if (freq<=200){
				mLSM303DigitalAccelRate = 6; // 200Hz
			} else if (freq<=400){
				mLSM303DigitalAccelRate = 7; // 400Hz
			} else {
				mLSM303DigitalAccelRate = 9; // 1344Hz
			}
		}
		else {
			if (freq>=10){
				mLSM303DigitalAccelRate = 2; // 10Hz
			} else {
				mLSM303DigitalAccelRate = 1; // 1Hz
			}
		}
		return mLSM303DigitalAccelRate;
	}
	
	
	private int setLSM303MagRateFromFreq(double freq) {
		// Check if channel is enabled 
		if (!isSensorEnabled(Configuration.Shimmer3.SensorMapKey.SHIMMER_LSM303DLHC_MAG)) {
			mLSM303MagRate = 0; // 0.75Hz
			return mLSM303MagRate;
		}
		
		if (!mLowPowerMag){
			if (freq<=0.75){
				mLSM303MagRate = 0; // 0.75Hz
			} else if (freq<=1){
				mLSM303MagRate = 1; // 1.5Hz
			} else if (freq<=3) {
				mLSM303MagRate = 2; // 3Hz
			} else if (freq<=7.5) {
				mLSM303MagRate = 3; // 7.5Hz
			} else if (freq<=15) {
				mLSM303MagRate = 4; // 15Hz
			} else if (freq<=30) {
				mLSM303MagRate = 5; // 30Hz
			} else if (freq<=75) {
				mLSM303MagRate = 6; // 75Hz
			} else {
				mLSM303MagRate = 7; // 220Hz
			}
		} else {
			if (freq>=10){
				mLSM303MagRate = 4; // 15Hz
			} else {
				mLSM303MagRate = 1; // 1.5Hz
			}
		}		
		return mLSM303MagRate;
	}
	
	
	private void setDefaultLsm303dlhcMagSensorConfig(boolean isSensorEnabled) {
		if(isSensorEnabled) {
			setLowPowerMag(false);
		}
		else {
			mMagRange=1;
			setLowPowerMag(true);
		}		
	}

	
	private void setDefaultLsm303dlhcAccelSensorConfig(boolean isSensorEnabled) {
		if(isSensorEnabled) {
			setLowPowerAccelWR(false);
		}
		else {
			mAccelRange = 0;
			setLowPowerAccelWR(true);
		}
	}
	
	
	public boolean isHighResAccelWr(){
		return mHighResAccelWR;
	}
	

	//TODO Returning same variable as isHighResAccelWr() -> remove one method?
	public boolean isLSM303DigitalAccelHRM() {
		return mHighResAccelWR;
	}
	
	
	public boolean isLowPowerAccelWr(){
		return mLowPowerAccelWR;
	}
	
	
	//TODO Returning same variable as isLowPowerAccelWr() -> remove one method?
	public boolean isLSM303DigitalAccelLPM() {
		return mLowPowerAccelWR;
	}
	
	//TODO Returning same variable as isLowPowerAccelWr() -> remove one method?
	public boolean isLowPowerAccelEnabled() {
		return mLowPowerAccelWR;
	}
	
	
	public boolean isLowPowerMagEnabled(){
		return mLowPowerMag;
	}
	
	
	public boolean isUsingDefaultWRAccelParam(){
		return mDefaultCalibrationParametersDigitalAccel; 
	}
	
	
	public boolean isUsingDefaultMagParam(){
		return mDefaultCalibrationParametersMag;
	}
	
	
	public int getLowPowerAccelEnabled(){
		if(mLowPowerAccelWR)
			return 1;
		else
			return 0;
	}
	
	
	public int getLowPowerMagEnabled() {
		if(mLowPowerMag)
			return 1;
		else
			return 0;
	}
	
	
	public int getAccelRange() {
		return mAccelRange;
	}
	
	
	public int getMagRange() {
		return mMagRange;
	}
	
	
	public int getLSM303MagRate() {
		return mLSM303MagRate;
	}

	
	public int getLSM303DigitalAccelRate() {
		return mLSM303DigitalAccelRate;
	}

	
	public double[][] getAlignmentMatrixWRAccel(){
		return mAlignmentMatrixWRAccel;
	}

	
	public double[][] getSensitivityMatrixWRAccel(){
		return mSensitivityMatrixWRAccel;
	}

	
	public double[][] getOffsetVectorMatrixWRAccel(){
		return mOffsetVectorWRAccel;
	}

	
	public double[][] getAlignmentMatrixMag(){
		return mAlignmentMatrixMagnetometer;
	}

	public double[][] getSensitivityMatrixMag(){
		return mSensitivityMatrixMagnetometer;
	}

	public double[][] getOffsetVectorMatrixMag(){
		return mOffsetVectorMagnetometer;
	}
	//--------- Sensor specific methods end --------------


	

	//--------- Optional methods to override in Sensor Class start --------
	/* (non-Javadoc)
	 * @see com.shimmerresearch.sensors.AbstractSensor#isSensorUsingDefaultCal(int)
	 */
	@Override
	public boolean isSensorUsingDefaultCal(int sensorMapKey) {
		if(sensorMapKey==Configuration.Shimmer3.SensorMapKey.SHIMMER_LSM303DLHC_ACCEL){
			return isUsingDefaultWRAccelParam();
		}
		else if(sensorMapKey==Configuration.Shimmer3.SensorMapKey.SHIMMER_LSM303DLHC_MAG){
			return isUsingDefaultMagParam();
		}
		return false;
	}	
	//--------- Optional methods to override in Sensor Class end --------



	
	//--------- Abstract methods not implemented start --------------
	//--------- Abstract methods not implemented end --------------
}
