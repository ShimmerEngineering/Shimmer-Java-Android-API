package com.shimmerresearch.sensors;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driver.ShimmerObject;
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
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.FW_ID;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.HW_ID;
import com.shimmerresearch.sensors.SensorBMP180.GuiLabelConfig;

/** 
 * @author Ruud Stolk
 * 
 */
public class SensorLSM303 extends AbstractSensor{

	//TODO - RS: remove these comments that were already in here:
	// list of compatible shimmer hw/fw for sensor not sensor options (see ShimmerVerObject class	
	//list/map of shimmer ver objects  to specify what config options should be generated based on hw/fw id	
	//COMTYPE should have dummy for no action setting
	//map infomem to fw, index, value
	
	/** * */
	private static final long serialVersionUID = -2119834127313796684L;

	//--------- Sensor specific variables start --------------
	public static final String METER_PER_SECOND_SQUARE = "m/(s^2)";  
	public static final String LOCAL_FLUX = "local_flux";  
//	public static final String U_TESLA = "uT";
//	public static final String GRAVITY = "g";
	public static final String ACCEL_CAL_UNIT = METER_PER_SECOND_SQUARE;
	public static final String MAG_CAL_UNIT = LOCAL_FLUX;
	
	public boolean mLowPowerAccelWR = false;
	public boolean mHighResAccelWR = false;
	
	public int mAccelRange = 0;
	public int mLSM303DigitalAccelRate = 0;
	public int mMagRange = 1;
	public int mLSM303MagRate = 4;

	/*XXX have not found equivalents for these variables from BMP180 class yet
	protected byte[] mPressureCalRawParams = new byte[23];
	protected byte[] mPressureRawParams  = new byte[23];
	public static final int MAX_NUMBER_OF_SIGNALS = 50;
		protected String[] mSignalNameArray=new String[MAX_NUMBER_OF_SIGNALS];	
	
	public static final String PRESSURE_TEMPERATURE = "Pressure & Temperature";
	XXX*/
	
	//XXX this method is not in BMP180 class
	public class Channel{
		public static final int XDAccel     			 = 0x04;
		public static final int YDAccel     			 = 0x05;
		public static final int ZDAccel     			 = 0x06;
		public static final int XMag        			 = 0x07;
		public static final int YMag        			 = 0x08;
		public static final int ZMag        			 = 0x09;
	}
	
	//XXX this method is not in BMP180 class
	public class SensorBitmap{
		public static final int SENSOR_MAG = 0x20;
		public static final int SENSOR_D_ACCEL = 0x1000;
	}
	
	//XXX this method is not in put inside a class environment in the BMP180 class
	public class SensorMapKey{
		public static final int SHIMMER_LSM303DLHC_MAG = 2;
		public static final int SHIMMER_LSM303DLHC_ACCEL = 11;
	}
	
	public class GuiLabelConfig{
		public static final String LSM303DLHC_ACCEL_RATE = "Wide Range Accel Rate";  
		public static final String LSM303DLHC_ACCEL_RANGE = "Wide Range Accel Range"; 
				
		public static final String LSM303DLHC_MAG_RANGE = "Mag Range";
		public static final String LSM303DLHC_MAG_RATE = "Mag Rate";
		
		public static final String LSM303DLHC_ACCEL_LPM = "Wide Range Accel Low-Power Mode"; 
		public static final String LSM303DLHC_MAG_LPM = "Mag Low-Power Mode";
		
		public static final String LSM303DLHC_ACCEL_DEFAULT_CALIB = "Wide Range Accel Default Calibration";
		public static final String LSM303DLHC_MAG_DEFAULT_CALIB = "Mag Default Calibration";
		
		//XXX are these needed here?
		public static final String KINEMATIC_LPM = "Kinematic Sensors Low-Power Mode";
	}
	
	//XXX this method is not in BMP180 class
	public class GuiLabelSensorTiles{
		public static final String MAG = GuiLabelSensors.MAG;
		public static final String ACCEL_WR = GuiLabelSensors.ACCEL_WR;
	}
		
	public class GuiLabelSensors{
		public static final String ACCEL_WR = "Wide-Range Accelerometer"; 
		public static final String MAG = "Magnetometer"; 
	}
	
	//XXX are these needed here?
	public class GuiLabelAlgorithmGrouping{
		public static final String ORIENTATION_9DOF = "9DOF"; 
		public static final String ORIENTATION_6DOF = "6DOF"; 
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
	
	/*XXX - Done in like it is done for BMP180 currently, but:
	 *  - Use CompatibilityInfoForMaps.listOfCompatibleVersionInfoLSM303 instead of CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW?
	 *  - Use other SensorConfigOptionDetails constructor, the one that includes connection type?
	 */
	public static final SensorConfigOptionDetails configOptionAccelRange = new SensorConfigOptionDetails(
			ListofAccelRange, 
			ListofLSM303DLHCAccelRangeConfigValues, 
			SensorConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX,
//			CompatibilityInfoForMaps.listOfCompatibleVersionInfoBMP180
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW);
	
	public static final SensorConfigOptionDetails configOptionMagRange = new SensorConfigOptionDetails(
			ListofMagRange, 
			ListofMagRangeConfigValues, 
			SensorConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX,
//			CompatibilityInfoForMaps.listOfCompatibleVersionInfoBMP180
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW);
	
	public static final SensorConfigOptionDetails configOptionAccelRate = new SensorConfigOptionDetails(
			ListofLSM303DLHCAccelRate, 
			ListofLSM303DLHCAccelRateConfigValues, 
			SensorConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX,
//			CompatibilityInfoForMaps.listOfCompatibleVersionInfoBMP180
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW);
	
	public static final SensorConfigOptionDetails configOptionMagRate = new SensorConfigOptionDetails(
			ListofLSM303DLHCMagRate, 
			ListofLSM303DLHCMagRateConfigValues, 
			SensorConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX,
//			CompatibilityInfoForMaps.listOfCompatibleVersionInfoBMP180
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW);
	
	public static final SensorConfigOptionDetails configOptionAccelRateLpm = new SensorConfigOptionDetails(
			ListofLSM303DLHCAccelRateLpm, 
			ListofLSM303DLHCAccelRateLpmConfigValues, 
			SensorConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX,
//			CompatibilityInfoForMaps.listOfCompatibleVersionInfoBMP180
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW);
	
	
	//--------- Configuration options end --------------
	
	
	//--------- Sensor info start --------------
	public static final SensorDetailsRef sensorLSM303DLHCAccel = new SensorDetailsRef(
			SensorBitmap.SENSOR_D_ACCEL, 
			SensorBitmap.SENSOR_D_ACCEL, 
			GuiLabelSensors.ACCEL_WR,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW,
			Arrays.asList(GuiLabelConfig.LSM303DLHC_ACCEL_RANGE,GuiLabelConfig.LSM303DLHC_ACCEL_RATE),
			Arrays.asList(ObjectClusterSensorName.ACCEL_WR_X,ObjectClusterSensorName.ACCEL_WR_Y,ObjectClusterSensorName.ACCEL_WR_Z));
	
	public static final SensorDetailsRef sensorLSM303DLHCMag = new SensorDetailsRef(
			SensorBitmap.SENSOR_MAG, 
			SensorBitmap.SENSOR_MAG, 
			GuiLabelSensors.MAG,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW,
			Arrays.asList(GuiLabelConfig.LSM303DLHC_MAG_RANGE,GuiLabelConfig.LSM303DLHC_MAG_RATE),
			Arrays.asList(ObjectClusterSensorName.MAG_X,ObjectClusterSensorName.MAG_Y,ObjectClusterSensorName.MAG_Z));
	
	public static final Map<Integer, SensorDetailsRef> mSensorMapRef;
    static {
        Map<Integer, SensorDetailsRef> aMap = new LinkedHashMap<Integer, SensorDetailsRef>();
        aMap.put(SensorLSM303.SensorMapKey.SHIMMER_LSM303DLHC_ACCEL, SensorLSM303.sensorLSM303DLHCAccel);
        aMap.put(SensorLSM303.SensorMapKey.SHIMMER_LSM303DLHC_MAG, SensorLSM303.sensorLSM303DLHCMag);	

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
        aMap.put(SensorLSM303.ObjectClusterSensorName.MAG_Y, SensorLSM303.channelLSM303MagY);
        aMap.put(SensorLSM303.ObjectClusterSensorName.MAG_Z, SensorLSM303.channelLSM303MagZ);
		mChannelMapRef = Collections.unmodifiableMap(aMap);
    }
	//--------- Channel info end --------------
    
    /**--------- Constructor for this sensor --------------
    * @param svo
	*/
	public SensorLSM303(ShimmerVerObject svo) {
		super(svo);
		mSensorName = SENSORS.LSM303.toString();
	}

	//--------- Abstract methods implemented start --------------
	@Override 
	public void generateSensorMap(ShimmerVerObject svo) {
		super.createLocalSensorMap(mSensorMapRef, mChannelMapRef);
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
			mSensorGroupingMap.put(GuiLabelSensorTiles.ACCEL_WR, new SensorGroupingDetails(
					Arrays.asList(SensorMapKey.SHIMMER_LSM303DLHC_ACCEL),
					CompatibilityInfoForMaps.listOfCompatibleVersionInfoLSM303));
			mSensorGroupingMap.put(GuiLabelSensorTiles.MAG, new SensorGroupingDetails(
					Arrays.asList(SensorMapKey.SHIMMER_LSM303DLHC_MAG),
					CompatibilityInfoForMaps.listOfCompatibleVersionInfoLSM303));
		}
		super.updateSensorGroupingMap();	
	}	

//	{//TODO - RS: this was already here, move to generateConfigOptionsMap()
//		
//		
//		//config options maps should be configured based on fw and hw id
//		
//		mConfigOptionsMap.put(GuiLabelConfig.LSM303DLHC_ACCEL_RANGE, 
//				new SensorConfigOptionDetails(ListofAccelRange, 
//										ListofLSM303DLHCAccelRangeConfigValues, 
//										SensorConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX, COMMUNICATION_TYPE.BLUETOOTH));
//		
//		mConfigOptionsMap.put(GuiLabelConfig.LSM303DLHC_ACCEL_RATE, 
//				new SensorConfigOptionDetails(ListofLSM303DLHCAccelRate, 
//										ListofLSM303DLHCAccelRateConfigValues, 
//										SensorConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX, COMMUNICATION_TYPE.BLUETOOTH));
//		
//		mConfigOptionsMap.put(GuiLabelConfig.LSM303DLHC_ACCEL_RANGE, 
//				new SensorConfigOptionDetails(ListofAccelRange, 
//										ListofLSM303DLHCAccelRangeConfigValues, 
//										SensorConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX, COMMUNICATION_TYPE.SD));
//		
//		mConfigOptionsMap.put(GuiLabelConfig.LSM303DLHC_ACCEL_RATE, 
//				new SensorConfigOptionDetails(ListofLSM303DLHCAccelRate, 
//										ListofLSM303DLHCAccelRateConfigValues, 
//										SensorConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX, COMMUNICATION_TYPE.SD));
//		
//		mConfigOptionsMap.get(GuiLabelConfig.LSM303DLHC_ACCEL_RATE).setGuiValues(SensorConfigOptionDetails.VALUE_INDEXES.LSM303DLHC_ACCEL_RATE.IS_LPM, Configuration.Shimmer3.ListofLSM303DLHCAccelRateLpm);
//		mConfigOptionsMap.get(GuiLabelConfig.LSM303DLHC_ACCEL_RATE).setConfigValues(SensorConfigOptionDetails.VALUE_INDEXES.LSM303DLHC_ACCEL_RATE.IS_LPM, Configuration.Shimmer3.ListofLSM303DLHCAccelRateLpmConfigValues);
//	}
//	
	
	@Override 
	public ObjectCluster processDataCustom(SensorDetails sensorDetails, byte[] sensorByteArray, COMMUNICATION_TYPE commType, ObjectCluster objectCluster) {
		// TODO Auto-generated method stub
		return objectCluster;
	}

	@Override 
	public void infoMemByteArrayGenerate(ShimmerDevice shimmerDevice, byte[] mInfoMemBytes) {
		// TODO Auto-generated method stub
		
	}

	@Override 
	public void infoMemByteArrayParse(ShimmerDevice shimmerDevice, byte[] mInfoMemBytes) {
		// TODO Auto-generated method stub
		
	}

	@Override 
	public Object setConfigValueUsingConfigLabel(String componentName, Object valueToSet) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override 
	public Object getConfigValueUsingConfigLabel(String componentName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override 
	public void setSamplingRateFromFreq() {
		// TODO Auto-generated method stub
		
	}

	@Override 
	public boolean setDefaultConfiguration(int sensorMapKey, boolean state) {
		if(mSensorMap.containsKey(sensorMapKey)){
			//TODO set defaults for particular sensor
			return true;
		}
		return false;
	}
	
	
	@Override 
	public Object getSettings(String componentName, COMMUNICATION_TYPE commType) {
		// TODO Auto-generated method stub
		//RS: Also returning null in BMP180 and GSR sensors classes.
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
//			//TODO these settings to be included here as well?
//			/*
//			case(GuiLabelConfig.LSM303DLHC_ACCEL_DEFAULT_CALIB):
//			case(GuiLabelConfig.LSM303DLHC_MAG_DEFAULT_CALIB):
//			*/
//		}
		
		return actionsetting;
		
	}

	
	//--------- Abstract methods implemented end --------------


	//--------- Sensor specific methods start --------------
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
	 * 
	 * @param enable
	 */
	protected void setLowPowerAccelWR(boolean enable){
		
	}

	public String getSensorName(){
		return mSensorName;
	}

	//--------- Sensor specific methods end --------------

	
	//--------- Abstract methods not implemented start --------------
	
	@Override 
	public boolean checkConfigOptionValues(String stringKey) {
		// TODO Auto-generated method stub
		return false;
	}

	//--------- Abstract methods not implemented end --------------



}
