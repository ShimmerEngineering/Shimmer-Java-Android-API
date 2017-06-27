package com.shimmerresearch.sensors.kionix;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.Configuration.CHANNEL_UNITS;
import com.shimmerresearch.driver.Configuration.Shimmer3.CompatibilityInfoForMaps;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driverUtilities.ChannelDetails;
import com.shimmerresearch.driverUtilities.SensorDetailsRef;
import com.shimmerresearch.driverUtilities.SensorGroupingDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_ENDIAN;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_TYPE;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_TYPE;

public class SensorKionixKXRB52042 extends SensorKionixAccel {

	private static final long serialVersionUID = -4053257599631109173L;

	//--------- Sensor specific variables start --------------

	public static class DatabaseChannelHandles{
		public static final String LN_ACC_X = "KXRB8_2042_X";
		public static final String LN_ACC_Y = "KXRB8_2042_Y";
		public static final String LN_ACC_Z = "KXRB8_2042_Z";
	}
	public static final class DatabaseConfigHandle{
		public static final String LN_ACC_CALIB_TIME = "KXRB8_2042_Acc_Calib_Time";
		public static final String LN_ACC_OFFSET_X = "KXRB8_2042_Acc_Offset_X";
		public static final String LN_ACC_OFFSET_Y = "KXRB8_2042_Acc_Offset_Y";
		public static final String LN_ACC_OFFSET_Z = "KXRB8_2042_Acc_Offset_Z";
		public static final String LN_ACC_GAIN_X = "KXRB8_2042_Acc_Gain_X";
		public static final String LN_ACC_GAIN_Y = "KXRB8_2042_Acc_Gain_Y";
		public static final String LN_ACC_GAIN_Z = "KXRB8_2042_Acc_Gain_Z";
		public static final String LN_ACC_ALIGN_XX = "KXRB8_2042_Acc_Align_XX";
		public static final String LN_ACC_ALIGN_XY = "KXRB8_2042_Acc_Align_XY";
		public static final String LN_ACC_ALIGN_XZ = "KXRB8_2042_Acc_Align_XZ";
		public static final String LN_ACC_ALIGN_YX = "KXRB8_2042_Acc_Align_YX";
		public static final String LN_ACC_ALIGN_YY = "KXRB8_2042_Acc_Align_YY";
		public static final String LN_ACC_ALIGN_YZ = "KXRB8_2042_Acc_Align_YZ";
		public static final String LN_ACC_ALIGN_ZX = "KXRB8_2042_Acc_Align_ZX";
		public static final String LN_ACC_ALIGN_ZY = "KXRB8_2042_Acc_Align_ZY";
		public static final String LN_ACC_ALIGN_ZZ = "KXRB8_2042_Acc_Align_ZZ";
		
		public static final List<String> LIST_OF_CALIB_HANDLES_LN_ACC = Arrays.asList(
				DatabaseConfigHandle.LN_ACC_OFFSET_X, DatabaseConfigHandle.LN_ACC_OFFSET_Y, DatabaseConfigHandle.LN_ACC_OFFSET_Z,
				DatabaseConfigHandle.LN_ACC_GAIN_X, DatabaseConfigHandle.LN_ACC_GAIN_Y, DatabaseConfigHandle.LN_ACC_GAIN_Z,
				DatabaseConfigHandle.LN_ACC_ALIGN_XX, DatabaseConfigHandle.LN_ACC_ALIGN_XY, DatabaseConfigHandle.LN_ACC_ALIGN_XZ,
				DatabaseConfigHandle.LN_ACC_ALIGN_YX, DatabaseConfigHandle.LN_ACC_ALIGN_YY, DatabaseConfigHandle.LN_ACC_ALIGN_YZ,
				DatabaseConfigHandle.LN_ACC_ALIGN_ZX, DatabaseConfigHandle.LN_ACC_ALIGN_ZY, DatabaseConfigHandle.LN_ACC_ALIGN_ZZ);
	}
	//--------- Sensor specific variables end --------------

	//--------- Sensor info start --------------
	public static final SensorDetailsRef sensorKionixKXRB52042 = new SensorDetailsRef(
			0x80, //== Configuration.Shimmer3.SensorBitmap.SENSOR_A_ACCEL will be: SensorBitmap.SENSOR_A_ACCEL, 
			0x80, //== Configuration.Shimmer3.SensorBitmap.SENSOR_A_ACCEL will be: SensorBitmap.SENSOR_A_ACCEL, 
			GuiLabelSensors.ACCEL_LN,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW,
			null,
			Arrays.asList(ObjectClusterSensorName.ACCEL_LN_X,ObjectClusterSensorName.ACCEL_LN_Y,ObjectClusterSensorName.ACCEL_LN_Z));
//	{
//		sensorKionixKXRB52042.mCalibSensorKey = 0x01;
//	}
	
	public static final Map<Integer, SensorDetailsRef> mSensorMapRef;
    static {
        Map<Integer, SensorDetailsRef> aMap = new LinkedHashMap<Integer, SensorDetailsRef>();
        aMap.put(Configuration.Shimmer3.SensorMapKey.SHIMMER_ANALOG_ACCEL, SensorKionixKXRB52042.sensorKionixKXRB52042);

		mSensorMapRef = Collections.unmodifiableMap(aMap);
    }
	//--------- Sensor info end --------------

	//--------- Channel info start --------------
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
        aMap.put(SensorKionixAccel.ObjectClusterSensorName.ACCEL_LN_X, SensorKionixKXRB52042.channelAccelX);
        aMap.put(SensorKionixAccel.ObjectClusterSensorName.ACCEL_LN_Y, SensorKionixKXRB52042.channelAccelY);
        aMap.put(SensorKionixAccel.ObjectClusterSensorName.ACCEL_LN_Z, SensorKionixKXRB52042.channelAccelZ);
		mChannelMapRef = Collections.unmodifiableMap(aMap);
    }
	//--------- Channel info end --------------
    
    public static final SensorGroupingDetails sensorGroupLnAccelKXRB52042 = new SensorGroupingDetails(
    		SensorKionixAccel.GuiLabelSensorTiles.LOW_NOISE_ACCEL,
			Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_ANALOG_ACCEL),
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW);
    
    //--------- Constructors for this class start --------------
	public SensorKionixKXRB52042() {
		super(SENSORS.KIONIXKXRB52042);
		initialise();
	}

	public SensorKionixKXRB52042(ShimmerVerObject shimmerVerObject) {
		super(SENSORS.KIONIXKXRB52042, shimmerVerObject);
		initialise();
	}
	
	public SensorKionixKXRB52042(ShimmerDevice shimmerDevice) {
		super(SENSORS.KIONIXKXRB52042, shimmerDevice);
		initialise();
	}
    //--------- Constructors for this class end --------------

	//--------- Abstract methods implemented start --------------


	@Override
	public void generateSensorMap() {
		super.createLocalSensorMapWithCustomParser(mSensorMapRef, mChannelMapRef);		
	}
	
	@Override
	public LinkedHashMap<String, Object> getConfigMapForDb() {
		LinkedHashMap<String, Object> mapOfConfig = new LinkedHashMap<String, Object>();
		super.addCalibDetailsToDbMap(mapOfConfig, 
				getCurrentCalibDetailsAccelLn(), 
				SensorKionixKXRB52042.DatabaseConfigHandle.LIST_OF_CALIB_HANDLES_LN_ACC,
				SensorKionixKXRB52042.DatabaseConfigHandle.LN_ACC_CALIB_TIME);
		return mapOfConfig;
	}

	@Override
	public void parseConfigMapFromDb(LinkedHashMap<String, Object> mapOfConfigPerShimmer) {
		//Analog Accel Calibration Configuration
		parseCalibDetailsKinematicFromDb(mapOfConfigPerShimmer, 
				Configuration.Shimmer3.SensorMapKey.SHIMMER_ANALOG_ACCEL, 
				0, 
				SensorKionixKXRB52042.DatabaseConfigHandle.LIST_OF_CALIB_HANDLES_LN_ACC,
				SensorKionixKXRB52042.DatabaseConfigHandle.LN_ACC_CALIB_TIME);
	}
	
	@Override
	public void generateSensorGroupMapping() {
		mSensorGroupingMap = new LinkedHashMap<Integer, SensorGroupingDetails>();
		if(mShimmerVerObject.isShimmerGen3() || mShimmerVerObject.isShimmerGen4()){
			mSensorGroupingMap.put(Configuration.Shimmer3.GuiLabelSensorTiles.LOW_NOISE_ACCEL.ordinal(), sensorGroupLnAccelKXRB52042);
		}
		super.updateSensorGroupingMap();	
	}


	//--------- Abstract methods implemented end --------------

}
