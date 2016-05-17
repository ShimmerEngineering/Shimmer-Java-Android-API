package com.shimmerresearch.sensors;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driver.ShimmerMsg;
import com.shimmerresearch.driver.ShimmerObject;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driverUtilities.ChannelDetails;
import com.shimmerresearch.driverUtilities.SensorConfigOptionDetails;
import com.shimmerresearch.driverUtilities.SensorDetails;
import com.shimmerresearch.driverUtilities.SensorGroupingDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.FW_ID;
import com.shimmerresearch.sensors.AbstractSensor.SENSORS;

/** 
 * @author Ruud Stolk
 * 
 */
public class SensorSTMICROLSM303DLHC extends AbstractSensor{

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
//	public static final String LOCAL = "local"; //used for axis-angle and madgewick quaternions 
	
	public boolean mLowPowerAccelWR = false;
	public boolean mHighResAccelWR = false;
	
	public int mAccelRange = 0;
	public int mLSM303DigitalAccelRate = 0;
	public int mMagRange = 1;
	public int mLSM303MagRate = 4;

	//XXX
//	protected byte[] mPressureCalRawParams = new byte[23];
//	protected byte[] mPressureRawParams  = new byte[23];
//	public static final int MAX_NUMBER_OF_SIGNALS = 50;
//	
//	
//	public static final String PRESSURE_TEMPERATURE = "Pressure & Temperature";
//	public static final int SHIMMER_BMP180_PRESSURE = 22;
//	
//	protected String[] mSignalNameArray=new String[MAX_NUMBER_OF_SIGNALS];	
//	public int mPressureResolution = 0;
	
	public static class ObjectClusterSensorName{
//		public static String TEMPERATURE_BMP180 = "Temperature_BMP180";
//		public static String PRESSURE_BMP180 = "Pressure_BMP180";
	}
	public class GuiLabelConfig{
		public static final String SAMPLING_RATE_DIVIDER_LSM303DLHC_ACCEL = "LSM303DLHC Divider";
		public static final String LSM303DLHC_ACCEL_RATE = "Wide Range Accel Rate";  
		public static final String LSM303DLHC_ACCEL_RANGE = "Wide Range Accel Range"; 
		public static final String LSM303DLHC_ACCEL_LPM = "Wide Range Accel Low-Power Mode"; 
		
		
	}
	public class GuiLabelSensors{
		public static final String ACCEL_WR = "Wide-Range Accelerometer"; 
		public static final String MAG = "Magnetometer"; 
	}
	public static class DatabaseChannelHandles{
//		public static final String PRESSURE = "BMP180_Pressure";
//		public static final String TEMPERATURE = "BMP180_Temperature";
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
	
	//--------- Configuration options end --------------
	
	
	//--------- Sensor info start --------------
	
	//--------- Sensor info end --------------
	
	
	//--------- Channel info start --------------
		
	//--------- Channel info end --------------
	public SensorSTMICROLSM303DLHC(ShimmerVerObject svo) {
		super(svo);
		mSensorName = SENSORS.STMICROLSM303DLHC.toString();
	}

	//--------- Abstract methods implemented start --------------
	@Override 
	public void generateSensorMap(ShimmerVerObject svo) {
		// TODO Auto-generated method stub
		
	}

	@Override 
	public void generateConfigOptionsMap(ShimmerVerObject svo) {
		// TODO Auto-generated method stub
		
	}

	@Override 
	public void generateSensorGroupMapping(ShimmerVerObject svo) {
		// TODO Auto-generated method stub
		
	}	

	{//TODO - RS: this was already here, move to generateConfigOptionsMap()
		
		
		//config options maps should be configured based on fw and hw id
		
		mConfigOptionsMap.put(Configuration.Shimmer3.GuiLabelConfig.LSM303DLHC_ACCEL_RANGE, 
				new SensorConfigOptionDetails(Configuration.Shimmer3.ListofAccelRange, 
										Configuration.Shimmer3.ListofLSM303DLHCAccelRangeConfigValues, 
										SensorConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX, COMMUNICATION_TYPE.BLUETOOTH));
		
		mConfigOptionsMap.put(Configuration.Shimmer3.GuiLabelConfig.LSM303DLHC_ACCEL_RATE, 
				new SensorConfigOptionDetails(Configuration.Shimmer3.ListofLSM303DLHCAccelRate, 
										Configuration.Shimmer3.ListofLSM303DLHCAccelRateConfigValues, 
										SensorConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX, COMMUNICATION_TYPE.BLUETOOTH));
		
		mConfigOptionsMap.put(Configuration.Shimmer3.GuiLabelConfig.LSM303DLHC_ACCEL_RANGE, 
				new SensorConfigOptionDetails(Configuration.Shimmer3.ListofAccelRange, 
										Configuration.Shimmer3.ListofLSM303DLHCAccelRangeConfigValues, 
										SensorConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX, COMMUNICATION_TYPE.SD));
		
		mConfigOptionsMap.put(Configuration.Shimmer3.GuiLabelConfig.LSM303DLHC_ACCEL_RATE, 
				new SensorConfigOptionDetails(Configuration.Shimmer3.ListofLSM303DLHCAccelRate, 
										Configuration.Shimmer3.ListofLSM303DLHCAccelRateConfigValues, 
										SensorConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX, COMMUNICATION_TYPE.SD));
		
		mConfigOptionsMap.get(Configuration.Shimmer3.GuiLabelConfig.LSM303DLHC_ACCEL_RATE).setGuiValues(SensorConfigOptionDetails.VALUE_INDEXES.LSM303DLHC_ACCEL_RATE.IS_LPM, Configuration.Shimmer3.ListofLSM303DLHCAccelRateLpm);
		mConfigOptionsMap.get(Configuration.Shimmer3.GuiLabelConfig.LSM303DLHC_ACCEL_RATE).setConfigValues(SensorConfigOptionDetails.VALUE_INDEXES.LSM303DLHC_ACCEL_RATE.IS_LPM, Configuration.Shimmer3.ListofLSM303DLHCAccelRateLpmConfigValues);
	}
	
	
	@Override 
	public ObjectCluster processDataCustom(SensorDetails sensorDetails, byte[] sensorByteArray, COMMUNICATION_TYPE commType, ObjectCluster objectCluster) {
		// TODO Auto-generated method stub
		return objectCluster;
	}

	@Override 
	public Object getSettings(String componentName, COMMUNICATION_TYPE commType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override 
	public ActionSetting setSettings(String componentName, Object valueToSet, COMMUNICATION_TYPE commType) {
		// 		Object returnValue = null;
		ActionSetting actionsetting = new ActionSetting(commType);
		
		 switch(componentName){
			case(Configuration.Shimmer3.GuiLabelConfig.LSM303DLHC_ACCEL_RANGE):
				mAccelRange = ((int)valueToSet);
				//
			if(mFirmwareType==FW_ID.BTSTREAM||mFirmwareType==FW_ID.LOGANDSTREAM){ //mcommtype
				actionsetting.mActionByteArray = new byte[]{ShimmerObject.SET_ACCEL_SENSITIVITY_COMMAND, (byte)mAccelRange};
				return actionsetting;
			} else if (mFirmwareType==FW_ID.SDLOG){
				//compatiblity check and instruction generation
			}
        	break;
		}
		
		return actionsetting;
		
	}

	
	@Override 
	public void infoMemByteArrayGenerate(ShimmerDevice shimmerDevice,
			byte[] mInfoMemBytes) {
		// TODO Auto-generated method stub
		
	}

	@Override 
	public void infoMemByteArrayParse(ShimmerDevice shimmerDevice,
			byte[] mInfoMemBytes) {
		// TODO Auto-generated method stub
		
	}

	@Override 
	public Object setConfigValueUsingConfigLabel(String componentName,
			Object valueToSet) {
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
	
	//--------- Abstract methods implemented end --------------


	//--------- Sensor specific methods start --------------
	/**
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
