package com.shimmerresearch.sensors;


import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.shimmerresearch.driver.Configuration.CHANNEL_UNITS;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driver.Configuration.Shimmer3.CompatibilityInfoForMaps;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driverUtilities.ChannelDetails;
import com.shimmerresearch.driverUtilities.SensorDetails;
import com.shimmerresearch.driverUtilities.SensorDetailsRef;
import com.shimmerresearch.driverUtilities.SensorGroupingDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_ENDIAN;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_TYPE;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_TYPE;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.HW_ID;


/** 
 * @author Ruud Stolk
 * 
 */
public class SensorKionixKXRB52042 extends AbstractSensor{

	/** Sensorclass for KionixKXRB52042 - analog/low-noise accelerometer
	 *
	 *  @param svo
	 * */
	private static final long serialVersionUID = -5027305280613145453L;

	
	//--------- Sensor specific variables start --------------
	protected boolean mDefaultCalibrationParametersAccel = true;
	protected double[][] mAlignmentMatrixAnalogAccel = {{-1,0,0},{0,-1,0},{0,0,1}}; 			
	protected double[][] mSensitivityMatrixAnalogAccel = {{38,0,0},{0,38,0},{0,0,38}}; 	
	protected double[][] mOffsetVectorAnalogAccel = {{2048},{2048},{2048}};
	
	public static final double[][] SensitivityMatrixLowNoiseAccel2gShimmer3 = {{83,0,0},{0,83,0},{0,0,83}};
	protected static final double[][] AlignmentMatrixLowNoiseAccelShimmer3 = {{0,-1,0},{-1,0,0},{0,0,-1}};
	protected static final double[][] OffsetVectorLowNoiseAccelShimmer3 = {{2047},{2047},{2047}};

	
	public class Channel{
		public static final int XAAccel     			 = 0x00;
		public static final int YAAccel    				 = 0x01;
		public static final int ZAAccel     			 = 0x02;
	}
	
	
	public class SensorBitmap{
		public static final int SENSOR_A_ACCEL			= 0x80;
	}
	
	
	public class SensorMapKey{
		public static final int SHIMMER_A_ACCEL = 0;
	}
	
	
	public class GuiLabelConfig{
		public static final String KXRB8_2042_ACCEL_DEFAULT_CALIB = "Low Noise Accel Default Calibration";
	}
	
	
	public class GuiLabelSensorTiles{
		public static final String LOW_NOISE_ACCEL = Configuration.Shimmer3.GuiLabelSensors.ACCEL_LN;
	}
	
	
	public class GuiLabelSensors{
		public static final String ACCEL_LN = "Low-Noise Accelerometer";
	}
	
	
	public static class DatabaseChannelHandles{
		public static final String LN_ACC_X = "KXRB8_2042_X";
		public static final String LN_ACC_Y = "KXRB8_2042_Y";
		public static final String LN_ACC_Z = "KXRB8_2042_Z";
	}
	
	
	public static class ObjectClusterSensorName{
		public static  String ACCEL_LN_X = "Accel_LN_X";
		public static  String ACCEL_LN_Y = "Accel_LN_Y";
		public static  String ACCEL_LN_Z = "Accel_LN_Z";
	}	
	//--------- Sensor specific variables end --------------
	
	
	//--------- Bluetooth commands start --------------
	public static final byte SET_ACCEL_CALIBRATION_COMMAND			= (byte) 0x11;
	public static final byte ACCEL_CALIBRATION_RESPONSE       		= (byte) 0x12;
	public static final byte GET_ACCEL_CALIBRATION_COMMAND    		= (byte) 0x13;
	//--------- Bluetooth commands end --------------

	
	//--------- Configuration options start --------------
	//--------- Configuration options end --------------

	//--------- Sensor info start --------------
	public static final SensorDetailsRef sensorKionixKXRB52042 = new SensorDetailsRef(
			SensorBitmap.SENSOR_A_ACCEL, 
			SensorBitmap.SENSOR_A_ACCEL, 
			GuiLabelSensors.ACCEL_LN,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW,
			null,
			Arrays.asList(ObjectClusterSensorName.ACCEL_LN_X,ObjectClusterSensorName.ACCEL_LN_Y,ObjectClusterSensorName.ACCEL_LN_Z));
	
	public static final Map<Integer, SensorDetailsRef> mSensorMapRef;
    static {
        Map<Integer, SensorDetailsRef> aMap = new LinkedHashMap<Integer, SensorDetailsRef>();
        aMap.put(SensorKionixKXRB52042.SensorMapKey.SHIMMER_A_ACCEL, SensorKionixKXRB52042.sensorKionixKXRB52042);

		mSensorMapRef = Collections.unmodifiableMap(aMap);
    }
	//--------- Sensor info end --------------
    
    
	//--------- Channel info start --------------
    public static final ChannelDetails channelLSM303AccelX = new ChannelDetails(
			ObjectClusterSensorName.ACCEL_LN_X,
			ObjectClusterSensorName.ACCEL_LN_X,
			DatabaseChannelHandles.LN_ACC_X,
			CHANNEL_DATA_TYPE.UINT12, 2, CHANNEL_DATA_ENDIAN.LSB,
			CHANNEL_UNITS.METER_PER_SECOND_SQUARE,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
    
    public static final ChannelDetails channelLSM303AccelY = new ChannelDetails(
			ObjectClusterSensorName.ACCEL_LN_Y,
			ObjectClusterSensorName.ACCEL_LN_Y,
			DatabaseChannelHandles.LN_ACC_Y,
			CHANNEL_DATA_TYPE.UINT12, 2, CHANNEL_DATA_ENDIAN.LSB,
			CHANNEL_UNITS.METER_PER_SECOND_SQUARE,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
    
    public static final ChannelDetails channelLSM303AccelZ = new ChannelDetails(
			ObjectClusterSensorName.ACCEL_LN_Z,
			ObjectClusterSensorName.ACCEL_LN_Z,
			DatabaseChannelHandles.LN_ACC_Z,
			CHANNEL_DATA_TYPE.UINT12, 2, CHANNEL_DATA_ENDIAN.LSB,
			CHANNEL_UNITS.METER_PER_SECOND_SQUARE,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
    
    public static final Map<String, ChannelDetails> mChannelMapRef;
    static {
        Map<String, ChannelDetails> aMap = new LinkedHashMap<String, ChannelDetails>();
        aMap.put(SensorKionixKXRB52042.ObjectClusterSensorName.ACCEL_LN_X, SensorKionixKXRB52042.channelLSM303AccelX);
        aMap.put(SensorKionixKXRB52042.ObjectClusterSensorName.ACCEL_LN_Y, SensorKionixKXRB52042.channelLSM303AccelY);
        aMap.put(SensorKionixKXRB52042.ObjectClusterSensorName.ACCEL_LN_Z, SensorKionixKXRB52042.channelLSM303AccelZ);
		mChannelMapRef = Collections.unmodifiableMap(aMap);
    }
	//--------- Channel info end --------------

	
    //--------- Constructors for this class start --------------
	public SensorKionixKXRB52042(ShimmerVerObject svo) {
		super(svo);
		mSensorName = SENSORS.KIONIXKXRB52042.toString();
	}
	//--------- Constructors for this class end --------------
	
	
	//--------- Abstract methods implemented start --------------
	@Override
	public void generateSensorMap(ShimmerVerObject svo) {
		super.createLocalSensorMap(mSensorMapRef, mChannelMapRef);		
	}

	
	@Override
	public void generateConfigOptionsMap(ShimmerVerObject svo) {
		//Nothing to configure.
	}
	
	
	@Override
	public void generateSensorGroupMapping(ShimmerVerObject svo) {
		mSensorGroupingMap = new LinkedHashMap<String, SensorGroupingDetails>();
		if(svo.mHardwareVersion==HW_ID.SHIMMER_3 || svo.mHardwareVersion==HW_ID.SHIMMER_4_SDK){
			mSensorGroupingMap.put(GuiLabelSensorTiles.LOW_NOISE_ACCEL, new SensorGroupingDetails(
					Arrays.asList(SensorMapKey.SHIMMER_A_ACCEL),
					CompatibilityInfoForMaps.listOfCompatibleVersionInfoKionixKXRB52042));
		}
		super.updateSensorGroupingMap();	
	}

	
	@Override
	public ObjectCluster processDataCustom(SensorDetails sensotDetails, byte[] rawData, COMMUNICATION_TYPE commType, ObjectCluster objectCluster) {
		// TODO Auto-generated method stub
		return objectCluster;
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
	public boolean setDefaultConfigForSensor(int sensorMapKey, boolean state) {
		if(mSensorMap.containsKey(sensorMapKey)){
			//TODO set defaults for particular sensor
			return true;
		}
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
	public boolean checkConfigOptionValues(String stringKey) {
		// TODO Auto-generated method stub
		return false;
	}
	//--------- Abstract methods implemented end --------------







	
}
