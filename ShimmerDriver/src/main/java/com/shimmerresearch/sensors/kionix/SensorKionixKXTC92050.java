package com.shimmerresearch.sensors.kionix;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.Configuration.CHANNEL_UNITS;
import com.shimmerresearch.driver.Configuration.Shimmer3;
import com.shimmerresearch.driver.Configuration.Shimmer3.CompatibilityInfoForMaps;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driverUtilities.ChannelDetails;
import com.shimmerresearch.driverUtilities.SensorDetailsRef;
import com.shimmerresearch.driverUtilities.SensorGroupingDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_ENDIAN;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_TYPE;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_TYPE;

/** This is the newer (post-July 2017) low-noise accelerometer used in the Shimmer3
 * @author Mark Nolan
 *
 */
public class SensorKionixKXTC92050 extends SensorKionixAccel {

	private static final long serialVersionUID = -4547873490496111518L;

	//--------- Sensor specific variables start --------------

	public static class DatabaseChannelHandles{
		public static final String LN_ACC_X = "KXTC9_2050_X";
		public static final String LN_ACC_Y = "KXTC9_2050_Y";
		public static final String LN_ACC_Z = "KXTC9_2050_Z";
	}
	public static final class DatabaseConfigHandle{
		public static final String LN_ACC_CALIB_TIME = "KXTC9_2050_Acc_Calib_Time";
		public static final String LN_ACC_OFFSET_X = "KXTC9_2050_Acc_Offset_X";
		public static final String LN_ACC_OFFSET_Y = "KXTC9_2050_Acc_Offset_Y";
		public static final String LN_ACC_OFFSET_Z = "KXTC9_2050_Acc_Offset_Z";
		public static final String LN_ACC_GAIN_X = "KXTC9_2050_Acc_Gain_X";
		public static final String LN_ACC_GAIN_Y = "KXTC9_2050_Acc_Gain_Y";
		public static final String LN_ACC_GAIN_Z = "KXTC9_2050_Acc_Gain_Z";
		public static final String LN_ACC_ALIGN_XX = "KXTC9_2050_Acc_Align_XX";
		public static final String LN_ACC_ALIGN_XY = "KXTC9_2050_Acc_Align_XY";
		public static final String LN_ACC_ALIGN_XZ = "KXTC9_2050_Acc_Align_XZ";
		public static final String LN_ACC_ALIGN_YX = "KXTC9_2050_Acc_Align_YX";
		public static final String LN_ACC_ALIGN_YY = "KXTC9_2050_Acc_Align_YY";
		public static final String LN_ACC_ALIGN_YZ = "KXTC9_2050_Acc_Align_YZ";
		public static final String LN_ACC_ALIGN_ZX = "KXTC9_2050_Acc_Align_ZX";
		public static final String LN_ACC_ALIGN_ZY = "KXTC9_2050_Acc_Align_ZY";
		public static final String LN_ACC_ALIGN_ZZ = "KXTC9_2050_Acc_Align_ZZ";
		
		public static final List<String> LIST_OF_CALIB_HANDLES_LN_ACC = Arrays.asList(
				DatabaseConfigHandle.LN_ACC_OFFSET_X, DatabaseConfigHandle.LN_ACC_OFFSET_Y, DatabaseConfigHandle.LN_ACC_OFFSET_Z,
				DatabaseConfigHandle.LN_ACC_GAIN_X, DatabaseConfigHandle.LN_ACC_GAIN_Y, DatabaseConfigHandle.LN_ACC_GAIN_Z,
				DatabaseConfigHandle.LN_ACC_ALIGN_XX, DatabaseConfigHandle.LN_ACC_ALIGN_XY, DatabaseConfigHandle.LN_ACC_ALIGN_XZ,
				DatabaseConfigHandle.LN_ACC_ALIGN_YX, DatabaseConfigHandle.LN_ACC_ALIGN_YY, DatabaseConfigHandle.LN_ACC_ALIGN_YZ,
				DatabaseConfigHandle.LN_ACC_ALIGN_ZX, DatabaseConfigHandle.LN_ACC_ALIGN_ZY, DatabaseConfigHandle.LN_ACC_ALIGN_ZZ);
	}
	//--------- Sensor specific variables end --------------

	//--------- Sensor info start --------------
	public static final SensorDetailsRef sensorKionixKXTC92050 = new SensorDetailsRef(
			0x80, //== Configuration.Shimmer3.SensorBitmap.SENSOR_A_ACCEL will be: SensorBitmap.SENSOR_A_ACCEL, 
			0x80, //== Configuration.Shimmer3.SensorBitmap.SENSOR_A_ACCEL will be: SensorBitmap.SENSOR_A_ACCEL, 
			GuiLabelSensors.ACCEL_LN,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoKionixKXTC92050,
			null,
			Arrays.asList(ObjectClusterSensorName.ACCEL_LN_X,ObjectClusterSensorName.ACCEL_LN_Y,ObjectClusterSensorName.ACCEL_LN_Z));
//	{
//		sensorKionixKXTC92050.mCalibSensorKey = 0x01;
//	}
	
	public static final Map<Integer, SensorDetailsRef> mSensorMapRef;
    static {
        Map<Integer, SensorDetailsRef> aMap = new LinkedHashMap<Integer, SensorDetailsRef>();
        aMap.put(Configuration.Shimmer3.SensorMapKey.SHIMMER_ANALOG_ACCEL, SensorKionixKXTC92050.sensorKionixKXTC92050);

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
        aMap.put(SensorKionixAccel.ObjectClusterSensorName.ACCEL_LN_X, SensorKionixKXTC92050.channelAccelX);
        aMap.put(SensorKionixAccel.ObjectClusterSensorName.ACCEL_LN_Y, SensorKionixKXTC92050.channelAccelY);
        aMap.put(SensorKionixAccel.ObjectClusterSensorName.ACCEL_LN_Z, SensorKionixKXTC92050.channelAccelZ);
		mChannelMapRef = Collections.unmodifiableMap(aMap);
    }
	//--------- Channel info end --------------
	
    public static final SensorGroupingDetails sensorGroupLnAccelKXTC92050 = new SensorGroupingDetails(
    		SensorKionixAccel.GuiLabelSensorTiles.LOW_NOISE_ACCEL,
			Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_ANALOG_ACCEL),
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoKionixKXTC92050);

    
    //--------- Constructors for this class start --------------
	public SensorKionixKXTC92050() {
		super(SENSORS.KIONIXKXTC92050);
		initialise();
	}

	public SensorKionixKXTC92050(ShimmerVerObject shimmerVerObject) {
		super(SENSORS.KIONIXKXTC92050, shimmerVerObject);
		initialise();
	}
	
	public SensorKionixKXTC92050(ShimmerDevice shimmerDevice) {
		super(SENSORS.KIONIXKXTC92050, shimmerDevice);
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
				SensorKionixKXTC92050.DatabaseConfigHandle.LIST_OF_CALIB_HANDLES_LN_ACC,
				SensorKionixKXTC92050.DatabaseConfigHandle.LN_ACC_CALIB_TIME);
		return mapOfConfig;
	}

	@Override
	public void parseConfigMapFromDb(LinkedHashMap<String, Object> mapOfConfigPerShimmer) {
		//Analog Accel Calibration Configuration
		parseCalibDetailsKinematicFromDb(mapOfConfigPerShimmer, 
				Configuration.Shimmer3.SensorMapKey.SHIMMER_ANALOG_ACCEL, 
				0, 
				SensorKionixKXTC92050.DatabaseConfigHandle.LIST_OF_CALIB_HANDLES_LN_ACC,
				SensorKionixKXTC92050.DatabaseConfigHandle.LN_ACC_CALIB_TIME);
	}

	@Override
	public void generateSensorGroupMapping() {
		mSensorGroupingMap = new LinkedHashMap<Integer, SensorGroupingDetails>();
		mSensorGroupingMap.put(Configuration.Shimmer3.GuiLabelSensorTiles.LOW_NOISE_ACCEL.ordinal(), sensorGroupLnAccelKXTC92050);
		super.updateSensorGroupingMap();	
	}

	public static String parseFromDBColumnToGUIChannel(String dbColumn) {
		String channel = "";
		if (dbColumn.equals(SensorKionixKXTC92050.DatabaseChannelHandles.LN_ACC_X)) {
			channel = Shimmer3.ObjectClusterSensorName.ACCEL_LN_X;
		} else if (dbColumn.equals(SensorKionixKXTC92050.DatabaseChannelHandles.LN_ACC_Y)) {
			channel = Shimmer3.ObjectClusterSensorName.ACCEL_LN_Y;
		} else if (dbColumn.equals(SensorKionixKXTC92050.DatabaseChannelHandles.LN_ACC_Z)) {
			channel = Shimmer3.ObjectClusterSensorName.ACCEL_LN_Z;
		}
		return channel;
	}

	public static String parseFromGUIChannelsToDBColumn(String channel) {
		String dbColumn = "";
		if (channel.equals(Shimmer3.ObjectClusterSensorName.ACCEL_LN_X)) {
			dbColumn = SensorKionixKXTC92050.DatabaseChannelHandles.LN_ACC_X;
		} else if (channel.equals(Shimmer3.ObjectClusterSensorName.ACCEL_LN_Y)) {
			dbColumn = SensorKionixKXTC92050.DatabaseChannelHandles.LN_ACC_Y;
		} else if (channel.equals(Shimmer3.ObjectClusterSensorName.ACCEL_LN_Z)) {
			dbColumn = SensorKionixKXTC92050.DatabaseChannelHandles.LN_ACC_Z;
		}
		return dbColumn;
	}


	//--------- Abstract methods implemented end --------------

}
