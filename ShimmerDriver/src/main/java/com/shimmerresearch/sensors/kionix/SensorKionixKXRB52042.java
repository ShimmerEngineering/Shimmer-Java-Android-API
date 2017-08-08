package com.shimmerresearch.sensors.kionix;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.Configuration.CHANNEL_UNITS;
import com.shimmerresearch.driver.Configuration.Shimmer3;
import com.shimmerresearch.driver.Configuration.Shimmer3.CompatibilityInfoForMaps;
import com.shimmerresearch.driver.calibration.CalibDetails;
import com.shimmerresearch.driver.calibration.CalibDetailsKinematic;
import com.shimmerresearch.driver.calibration.OldCalDetails;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driverUtilities.ChannelDetails;
import com.shimmerresearch.driverUtilities.SensorDetailsRef;
import com.shimmerresearch.driverUtilities.SensorGroupingDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_ENDIAN;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_TYPE;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_TYPE;
import com.shimmerresearch.sensors.AbstractSensor;

/** This is the older (pre-July 2017) low-noise accelerometer used in the 
 * Shimmer3 (i.e. Kionix KXRB5-2042)
 * @author Mark Nolan
 *
 */
public class SensorKionixKXRB52042 extends SensorKionixAccel {

	private static final long serialVersionUID = -4053257599631109173L;

	//--------- Sensor specific variables start --------------
	
	public static final double[][] AlignmentMatrixLowNoiseAccelShimmer3 = {{0,-1,0},{-1,0,0},{0,0,-1}};
	// Based on a manufacturer stated, typical zero-g offset of 1.5V per axis over an input of 0v to 3v
	// [(1.5/3)*4096 = 2047.5]
	public static final double[][] OffsetVectorLowNoiseAccelShimmer3 = {{2047},{2047},{2047}}; 
	// Based on a manufacturer stated, typical sensitivity of 600mV/g per axis over an input of 0v to 3v
	// [+-2g full-range so 4g (or 39.24 m/s2) over 2.4V -> (2.4/3*4096)/39.24 = 83.5 bits/(m/s2)]
	public static final double[][] SensitivityMatrixLowNoiseAccel2gShimmer3 = {{83,0,0},{0,83,0},{0,0,83}};  

    public static final Map<String, OldCalDetails> mOldCalRangeMap;
    static {
        Map<String, OldCalDetails> aMap = new LinkedHashMap<String, OldCalDetails>();
        aMap.put("accel_ln_2g", new OldCalDetails("accel_ln_2g", Configuration.Shimmer3.SENSOR_ID.SHIMMER_ANALOG_ACCEL, LN_ACCEL_RANGE_VALUE));
        mOldCalRangeMap = Collections.unmodifiableMap(aMap);
    }
    
	private CalibDetailsKinematic calibDetailsAccelLn2g = new CalibDetailsKinematic(
			LN_ACCEL_RANGE_VALUE, LN_ACCEL_RANGE_STRING, 
			AlignmentMatrixLowNoiseAccelShimmer3, SensitivityMatrixLowNoiseAccel2gShimmer3, OffsetVectorLowNoiseAccelShimmer3);
//	public CalibDetailsKinematic mCurrentCalibDetailsAccelLn = calibDetailsAccelLn2g;


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
			Arrays.asList(SensorKionixAccel.ObjectClusterSensorName.ACCEL_LN_X,
					SensorKionixAccel.ObjectClusterSensorName.ACCEL_LN_Y,
					SensorKionixAccel.ObjectClusterSensorName.ACCEL_LN_Z));
//	{
//		sensorKionixKXRB52042.mCalibSensorKey = 0x01;
//	}
	
	public static final Map<Integer, SensorDetailsRef> mSensorMapRef;
    static {
        Map<Integer, SensorDetailsRef> aMap = new LinkedHashMap<Integer, SensorDetailsRef>();
        aMap.put(Configuration.Shimmer3.SENSOR_ID.SHIMMER_ANALOG_ACCEL, SensorKionixKXRB52042.sensorKionixKXRB52042);

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
    		SensorKionixAccel.LABEL_SENSOR_TILE.LOW_NOISE_ACCEL,
			Arrays.asList(Configuration.Shimmer3.SENSOR_ID.SHIMMER_ANALOG_ACCEL),
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
	public LinkedHashMap<String, Object> generateConfigMap() {
		LinkedHashMap<String, Object> mapOfConfig = new LinkedHashMap<String, Object>();
		super.addCalibDetailsToDbMap(mapOfConfig, 
				getCurrentCalibDetailsAccelLn(), 
				SensorKionixKXRB52042.DatabaseConfigHandle.LIST_OF_CALIB_HANDLES_LN_ACC,
				SensorKionixKXRB52042.DatabaseConfigHandle.LN_ACC_CALIB_TIME);
		return mapOfConfig;
	}

	@Override
	public void parseConfigMap(LinkedHashMap<String, Object> mapOfConfigPerShimmer) {
		//Analog Accel Calibration Configuration
		parseCalibDetailsKinematicFromDb(mapOfConfigPerShimmer, 
				Configuration.Shimmer3.SENSOR_ID.SHIMMER_ANALOG_ACCEL, 
				0, 
				SensorKionixKXRB52042.DatabaseConfigHandle.LIST_OF_CALIB_HANDLES_LN_ACC,
				SensorKionixKXRB52042.DatabaseConfigHandle.LN_ACC_CALIB_TIME);
	}
	
	@Override
	public void generateSensorGroupMapping() {
		mSensorGroupingMap = new LinkedHashMap<Integer, SensorGroupingDetails>();
		if(mShimmerVerObject.isShimmerGen3() || mShimmerVerObject.isShimmerGen4()){
			mSensorGroupingMap.put(Configuration.Shimmer3.LABEL_SENSOR_TILE.LOW_NOISE_ACCEL.ordinal(), sensorGroupLnAccelKXRB52042);
		}
		super.updateSensorGroupingMap();	
	}

	public static String parseFromDBColumnToGUIChannel(String databaseChannelHandle) {
		//TODO Old approach, can be removed
//		String objectClusterName = "";
//		if (databaseChannelHandle.equals(SensorKionixKXRB52042.DatabaseChannelHandles.LN_ACC_X)) {
//			objectClusterName = Shimmer3.ObjectClusterSensorName.ACCEL_LN_X;
//		} else if (databaseChannelHandle.equals(SensorKionixKXRB52042.DatabaseChannelHandles.LN_ACC_Y)) {
//			objectClusterName = Shimmer3.ObjectClusterSensorName.ACCEL_LN_Y;
//		} else if (databaseChannelHandle.equals(SensorKionixKXRB52042.DatabaseChannelHandles.LN_ACC_Z)) {
//			objectClusterName = Shimmer3.ObjectClusterSensorName.ACCEL_LN_Z;
//		}
//		return objectClusterName;
		
		return AbstractSensor.parseFromDBColumnToGUIChannel(mChannelMapRef, databaseChannelHandle);
	}

	public static String parseFromGUIChannelsToDBColumn(String objectClusterName) {
		//TODO Old approach, can be removed
//		String databaseChannelHandle = "";
//		if (objectClusterName.equals(Shimmer3.ObjectClusterSensorName.ACCEL_LN_X)) {
//			databaseChannelHandle = SensorKionixKXRB52042.DatabaseChannelHandles.LN_ACC_X;
//		} else if (objectClusterName.equals(Shimmer3.ObjectClusterSensorName.ACCEL_LN_Y)) {
//			databaseChannelHandle = SensorKionixKXRB52042.DatabaseChannelHandles.LN_ACC_Y;
//		} else if (objectClusterName.equals(Shimmer3.ObjectClusterSensorName.ACCEL_LN_Z)) {
//			databaseChannelHandle = SensorKionixKXRB52042.DatabaseChannelHandles.LN_ACC_Z;
//		}
//		return databaseChannelHandle;
		
		return AbstractSensor.parseFromGUIChannelsToDBColumn(mChannelMapRef, objectClusterName);
	}

	//--------- Abstract methods implemented end --------------

	
	//--------- Optional methods to override in Sensor Class start --------
	@Override
	public void initialise() {
		super.initialise();
		
		updateCurrentAccelLnCalibInUse();
	}

	@Override
	public void generateCalibMap() {
		super.generateCalibMap();
		
		TreeMap<Integer, CalibDetails> calibMapAccelLn = new TreeMap<Integer, CalibDetails>();
		calibMapAccelLn.put(calibDetailsAccelLn2g.mRangeValue, calibDetailsAccelLn2g);
		
		setCalibrationMapPerSensor(Configuration.Shimmer3.SENSOR_ID.SHIMMER_ANALOG_ACCEL, calibMapAccelLn);
		
		updateCurrentAccelLnCalibInUse();
	}

	//--------- Optional methods to override in Sensor Class end --------

}
