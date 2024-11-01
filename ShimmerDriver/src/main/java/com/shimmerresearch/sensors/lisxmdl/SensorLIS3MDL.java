package com.shimmerresearch.sensors.lisxmdl;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.ArrayUtils;

import com.shimmerresearch.driver.Configuration.CHANNEL_UNITS;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driver.Configuration.Shimmer3.CompatibilityInfoForMaps;
import com.shimmerresearch.driver.calibration.CalibDetails;
import com.shimmerresearch.driver.calibration.CalibDetailsKinematic;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driver.ShimmerObject;
import com.shimmerresearch.driverUtilities.ChannelDetails;
import com.shimmerresearch.driverUtilities.ConfigOptionDetailsSensor;
import com.shimmerresearch.driverUtilities.ConfigOptionObject;
import com.shimmerresearch.driverUtilities.SensorDetailsRef;
import com.shimmerresearch.driverUtilities.SensorGroupingDetails;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_ENDIAN;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_TYPE;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_TYPE;
import com.shimmerresearch.sensors.AbstractSensor;
import com.shimmerresearch.sensors.ActionSetting;

public class SensorLIS3MDL extends SensorLISXMDL{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2366590050010873738L;
	
	//--------- Sensor specific variables start --------------	
	
	public static final double[][] DefaultAlignmentLIS3MDL = {{1,0,0},{0,-1,0},{0,0,-1}};	

	// ----------   Mag start ---------------
	public static final double[][] DefaultAlignmentMatrixMagShimmer3 = DefaultAlignmentLIS3MDL; 				
	public static final double[][] DefaultOffsetVectorMagShimmer3 = {{0},{0},{0}};	
	// Manufacturer stated: X any Y any Z axis @ 6842 LSB/gauss
	public static final double[][] DefaultSensitivityMatrixMag4GaShimmer3 = {{6842,0,0},{0,6842,0},{0,0,6842}};
	// Manufacturer stated: X any Y any Z axis @ 3421  LSB/gauss
	public static final double[][] DefaultSensitivityMatrixMag8GaShimmer3 = {{3421 ,0,0},{0,3421 ,0},{0,0,3421 }};
	// Manufacturer stated: X any Y any Z axis @ 2281  LSB/gauss
	public static final double[][] DefaultSensitivityMatrixMag12GaShimmer3 = {{2281 ,0,0},{0,2281 ,0},{0,0,2281 }};
	// Manufacturer stated: X any Y any Z axis @ 1711  LSB/gauss
	public static final double[][] DefaultSensitivityMatrixMag16GaShimmer3 = {{1711 ,0,0},{0,1711 ,0},{0,0,1711 }};

	private CalibDetailsKinematic calibDetailsMag4 = new CalibDetailsKinematic(
			ListofLIS3MDLMagRangeConfigValues[0],
			ListofLIS3MDLMagRange[0],
			DefaultAlignmentMatrixMagShimmer3,
			DefaultSensitivityMatrixMag4GaShimmer3,
			DefaultOffsetVectorMagShimmer3);
	private CalibDetailsKinematic calibDetailsMag8 = new CalibDetailsKinematic(
			ListofLIS3MDLMagRangeConfigValues[1],
			ListofLIS3MDLMagRange[1],
			DefaultAlignmentMatrixMagShimmer3, 
			DefaultSensitivityMatrixMag8GaShimmer3,
			DefaultOffsetVectorMagShimmer3);
	private CalibDetailsKinematic calibDetailsMag12 = new CalibDetailsKinematic(
			ListofLIS3MDLMagRangeConfigValues[2], 
			ListofLIS3MDLMagRange[2],
			DefaultAlignmentMatrixMagShimmer3,
			DefaultSensitivityMatrixMag12GaShimmer3, 
			DefaultOffsetVectorMagShimmer3);
	private CalibDetailsKinematic calibDetailsMag16 = new CalibDetailsKinematic(
			ListofLIS3MDLMagRangeConfigValues[3],
			ListofLIS3MDLMagRange[3],
			DefaultAlignmentMatrixMagShimmer3,
			DefaultSensitivityMatrixMag16GaShimmer3,
			DefaultOffsetVectorMagShimmer3);

	// ----------   Mag end ---------------
	
	public static class DatabaseChannelHandles{
		public static final String MAG_X = "LIS3MDL_MAG_X";
		public static final String MAG_Y = "LIS3MDL_MAG_Y";
		public static final String MAG_Z = "LIS3MDL_MAG_Z";
	}
	
	public static final class DatabaseConfigHandle{
		public static final String MAG_RANGE = "LIS3MDL_Mag_Range";
		public static final String MAG_RATE = "LIS3MDL_Mag_Rate";
//		public static final String MAG = "LIS3MDL_Mag";
		
		public static final String MAG_LPM = "LIS3MDL_Mag_LPM";
		public static final String MAG_MPM = "LIS3MDL_Mag_MPM";
		public static final String MAG_HPM = "LIS3MDL_Mag_HPM";
		public static final String MAG_UPM = "LIS3MDL_Mag_UPM";
		
		public static final String MAG_CALIB_TIME = "LIS3MDL_Mag_Calib_Time";
		public static final String MAG_OFFSET_X = "LIS3MDL_Mag_Offset_X";
		public static final String MAG_OFFSET_Y = "LIS3MDL_Mag_Offset_Y";
		public static final String MAG_OFFSET_Z = "LIS3MDL_Mag_Offset_Z";
		public static final String MAG_GAIN_X = "LIS3MDL_Mag_Gain_X";
		public static final String MAG_GAIN_Y = "LIS3MDL_Mag_Gain_Y";
		public static final String MAG_GAIN_Z = "LIS3MDL_Mag_Gain_Z";
		public static final String MAG_ALIGN_XX = "LIS3MDL_Mag_Align_XX";
		public static final String MAG_ALIGN_XY = "LIS3MDL_Mag_Align_XY";
		public static final String MAG_ALIGN_XZ = "LIS3MDL_Mag_Align_XZ";
		public static final String MAG_ALIGN_YX = "LIS3MDL_Mag_Align_YX";
		public static final String MAG_ALIGN_YY = "LIS3MDL_Mag_Align_YY";
		public static final String MAG_ALIGN_YZ = "LIS3MDL_Mag_Align_YZ";
		public static final String MAG_ALIGN_ZX = "LIS3MDL_Mag_Align_ZX";
		public static final String MAG_ALIGN_ZY = "LIS3MDL_Mag_Align_ZY";
		public static final String MAG_ALIGN_ZZ = "LIS3MDL_Mag_Align_ZZ";

		public static final List<String> LIST_OF_CALIB_HANDLES_MAG = Arrays.asList(
				DatabaseConfigHandle.MAG_OFFSET_X, DatabaseConfigHandle.MAG_OFFSET_Y, DatabaseConfigHandle.MAG_OFFSET_Z,
				DatabaseConfigHandle.MAG_GAIN_X, DatabaseConfigHandle.MAG_GAIN_Y, DatabaseConfigHandle.MAG_GAIN_Z,
				DatabaseConfigHandle.MAG_ALIGN_XX, DatabaseConfigHandle.MAG_ALIGN_XY, DatabaseConfigHandle.MAG_ALIGN_XZ,
				DatabaseConfigHandle.MAG_ALIGN_YX, DatabaseConfigHandle.MAG_ALIGN_YY, DatabaseConfigHandle.MAG_ALIGN_YZ,
				DatabaseConfigHandle.MAG_ALIGN_ZX, DatabaseConfigHandle.MAG_ALIGN_ZY, DatabaseConfigHandle.MAG_ALIGN_ZZ);
	}
	
	//--------- Sensor specific variables end --------------	
	
	//--------- Configuration options start --------------
	
	public static final String[] ListofLIS3MDLMagRateLp={"0.625Hz","1.25Hz","2.5Hz","5Hz","10Hz","20Hz","40Hz","80Hz","1000Hz"};
	public static final String[] ListofLIS3MDLMagRateMp={"1.25Hz","2.5Hz","5Hz","10Hz","20Hz","40Hz","80Hz","560Hz"};
	public static final String[] ListofLIS3MDLMagRateHp={"1.25Hz","2.5Hz","5Hz","10Hz","20Hz","40Hz","80Hz","300Hz"};
	public static final String[] ListofLIS3MDLMagRateUp={"1.25Hz","2.5Hz","5Hz","10Hz","20Hz","40Hz","80Hz","155Hz"};
	public static final Integer[] ListofLIS3MDLMagRateLpConfigValues = {0x00, 0x02, 0x04, 0x06, 0x08, 0x0A, 0x0C, 0x0E, 0x01};
	public static final Integer[] ListofLIS3MDLMagRateMpConfigValues = {0x12, 0x14, 0x16, 0x18, 0x1A, 0x1C, 0x1E, 0x11};
	public static final Integer[] ListofLIS3MDLMagRateHpConfigValues = {0x22, 0x24, 0x26, 0x28, 0x2A, 0x2C, 0x2E, 0x21};
	public static final Integer[] ListofLIS3MDLMagRateUpConfigValues = {0x32, 0x34, 0x36, 0x38, 0x3A, 0x3C, 0x3E, 0x31};

	public static final String[] ListofLIS3MDLMagRange={"+/- 4Ga","+/- 8Ga","+/- 12Ga","+/- 16Ga"}; 
	public static final Integer[] ListofLIS3MDLMagRangeConfigValues={0,1,2,3};

	public static final ConfigOptionDetailsSensor configOptionMagRange = new ConfigOptionDetailsSensor(
			SensorLIS3MDL.GuiLabelConfig.LISXMDL_MAG_RANGE,
			SensorLIS3MDL.DatabaseConfigHandle.MAG_RANGE,
			ListofLIS3MDLMagRange, 
			ListofLIS3MDLMagRangeConfigValues, 
			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoLISM3MDL);
	
	public static final ConfigOptionDetailsSensor configOptionMagRate = new ConfigOptionDetailsSensor(
			SensorLIS3MDL.GuiLabelConfig.LISXMDL_MAG_RATE,
			SensorLIS3MDL.DatabaseConfigHandle.MAG_RATE,
			ListofLIS3MDLMagRateLp, 
			ListofLIS3MDLMagRateLpConfigValues,
			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoLISM3MDL,
		    Arrays.asList(
		            new ConfigOptionObject(ConfigOptionDetailsSensor.VALUE_INDEXES.LIS3MDL_MAG_RATE.IS_MP, 
		                    SensorLIS3MDL.ListofLIS3MDLMagRateMp, 
		                    SensorLIS3MDL.ListofLIS3MDLMagRateMpConfigValues),
		            new ConfigOptionObject(ConfigOptionDetailsSensor.VALUE_INDEXES.LIS3MDL_MAG_RATE.IS_HP, 
		                    SensorLIS3MDL.ListofLIS3MDLMagRateHp, 
		                    SensorLIS3MDL.ListofLIS3MDLMagRateHpConfigValues),
		            new ConfigOptionObject(ConfigOptionDetailsSensor.VALUE_INDEXES.LIS3MDL_MAG_RATE.IS_UP, 
		                    SensorLIS3MDL.ListofLIS3MDLMagRateUp, 
		                    SensorLIS3MDL.ListofLIS3MDLMagRateUpConfigValues)
		        ));

	//--------- Configuration options end --------------
	
	//--------- Sensor info start --------------
	
	public static final SensorDetailsRef sensorLIS3MDLMag = new SensorDetailsRef(
			0x20, //== Configuration.Shimmer3.SensorBitmap.SENSOR_MAG will be: SensorBitmap.SENSOR_MAG, 
			0x20, //== Configuration.Shimmer3.SensorBitmap.SENSOR_MAG will be: SensorBitmap.SENSOR_MAG, 
			GuiLabelSensors.MAG,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoLISM3MDL,
			Arrays.asList(GuiLabelConfig.LISXMDL_MAG_RANGE,
					GuiLabelConfig.LISXMDL_MAG_RATE),
			//MAG channel parsing order is XZY instead of XYZ but it would be better to represent it on the GUI in XYZ
			Arrays.asList(ObjectClusterSensorName.MAG_X,
					ObjectClusterSensorName.MAG_Y,
					ObjectClusterSensorName.MAG_Z));
	
	public static final Map<Integer, SensorDetailsRef> mSensorMapRef;
    static {
        Map<Integer, SensorDetailsRef> aMap = new LinkedHashMap<Integer, SensorDetailsRef>();
        aMap.put(Configuration.Shimmer3.SENSOR_ID.SHIMMER_LIS3MDL_MAG, SensorLIS3MDL.sensorLIS3MDLMag);	
		mSensorMapRef = Collections.unmodifiableMap(aMap);
    }
	
	//--------- Sensor info end --------------
    
  //--------- Channel info start --------------
    
    public static final ChannelDetails channelLIS3MDLMagX = new ChannelDetails(
			ObjectClusterSensorName.MAG_X,
			ObjectClusterSensorName.MAG_X,
			DatabaseChannelHandles.MAG_X,
			CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.MSB,
			CHANNEL_UNITS.LOCAL_FLUX,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL),
			0x07);
	
	public static final ChannelDetails channelLIS3MDLMagY = new ChannelDetails(
			ObjectClusterSensorName.MAG_Y,
			ObjectClusterSensorName.MAG_Y,
			DatabaseChannelHandles.MAG_Y,
			CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.MSB,
			CHANNEL_UNITS.LOCAL_FLUX,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL),
			0x08);
	
	public static final ChannelDetails channelLIS3MDLMagZ = new ChannelDetails(
			ObjectClusterSensorName.MAG_Z,
			ObjectClusterSensorName.MAG_Z,
			DatabaseChannelHandles.MAG_Z,
			CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.MSB,
			CHANNEL_UNITS.LOCAL_FLUX,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL),
			0x09);
    
    public static final Map<String, ChannelDetails> mChannelMapRef;
    static {
        Map<String, ChannelDetails> aMap = new LinkedHashMap<String, ChannelDetails>();
        aMap.put(SensorLIS3MDL.ObjectClusterSensorName.MAG_X, SensorLIS3MDL.channelLIS3MDLMagX);
        aMap.put(SensorLIS3MDL.ObjectClusterSensorName.MAG_Z, SensorLIS3MDL.channelLIS3MDLMagZ);
        aMap.put(SensorLIS3MDL.ObjectClusterSensorName.MAG_Y, SensorLIS3MDL.channelLIS3MDLMagY);
		mChannelMapRef = Collections.unmodifiableMap(aMap);
    }
    
    //--------- Channel info end --------------
    
    public static final SensorGroupingDetails sensorGroupLisMag = new SensorGroupingDetails(
			LABEL_SENSOR_TILE.MAG,
			Arrays.asList(Configuration.Shimmer3.SENSOR_ID.SHIMMER_LIS3MDL_MAG),
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoLISM3MDL);
    
  //--------- Constructors for this class start --------------
    
	public SensorLIS3MDL() {
		super();
		initialise();
	}
	
	public SensorLIS3MDL(ShimmerObject obj) {
		super(obj);
		initialise();
	}
	
	public SensorLIS3MDL(ShimmerDevice shimmerDevice) {
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
		if(mShimmerVerObject.isShimmerGen3() || mShimmerVerObject.isShimmerGen4()){
			mSensorGroupingMap.put(Configuration.Shimmer3.LABEL_SENSOR_TILE.MAG.ordinal(), sensorGroupLisMag);
		}
		super.updateSensorGroupingMap();	
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
		
		return actionsetting;
		
	}
	
	@Override
	public LinkedHashMap<String, Object> generateConfigMap() {
LinkedHashMap<String, Object> mapOfConfig = new LinkedHashMap<String, Object>();
		
		mapOfConfig.put(SensorLIS3MDL.DatabaseConfigHandle.MAG_RATE, getLIS3MDLMagRate());
		mapOfConfig.put(SensorLIS3MDL.DatabaseConfigHandle.MAG_RANGE, getMagRange());
		mapOfConfig.put(SensorLIS3MDL.DatabaseConfigHandle.MAG_LPM, getLowPowerMagEnabled());
		mapOfConfig.put(SensorLIS3MDL.DatabaseConfigHandle.MAG_MPM, getMedPowerMagEnabled());
		mapOfConfig.put(SensorLIS3MDL.DatabaseConfigHandle.MAG_HPM, getHighPowerMagEnabled());
		mapOfConfig.put(SensorLIS3MDL.DatabaseConfigHandle.MAG_UPM, getUltraHighPowerMagEnabled());
		
		super.addCalibDetailsToDbMap(mapOfConfig, 
				getCurrentCalibDetailsMag(), 
				SensorLIS3MDL.DatabaseConfigHandle.LIST_OF_CALIB_HANDLES_MAG,
				SensorLIS3MDL.DatabaseConfigHandle.MAG_CALIB_TIME);

		return mapOfConfig;
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
		if(mapOfConfigPerShimmer.containsKey(SensorLIS3MDL.DatabaseConfigHandle.MAG_RANGE)){
			setLIS3MDLMagRate(((Double) mapOfConfigPerShimmer.get(SensorLIS3MDL.DatabaseConfigHandle.MAG_RANGE)).intValue());
		}
		if(mapOfConfigPerShimmer.containsKey(SensorLIS3MDL.DatabaseConfigHandle.MAG_RATE)){
			setLIS3MDLMagRange(((Double) mapOfConfigPerShimmer.get(SensorLIS3MDL.DatabaseConfigHandle.MAG_RATE)).intValue());
		}
		
		//Magnetometer Calibration Configuration
		parseCalibDetailsKinematicFromDb(mapOfConfigPerShimmer, 
				Configuration.Shimmer3.SENSOR_ID.SHIMMER_LIS3MDL_MAG, 
				getMagRange(), 
				SensorLIS3MDL.DatabaseConfigHandle.LIST_OF_CALIB_HANDLES_MAG,
				SensorLIS3MDL.DatabaseConfigHandle.MAG_CALIB_TIME);
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
		mSensorIdMag = Configuration.Shimmer3.SENSOR_ID.SHIMMER_LIS3MDL_MAG;
		super.initialise();
		
		mMagRange = ListofLIS3MDLMagRangeConfigValues[0];
		
		updateCurrentMagCalibInUse();
	}

	@Override
	public void generateCalibMap() {
		super.generateCalibMap();

		TreeMap<Integer, CalibDetails> calibMapMag = new TreeMap<Integer, CalibDetails>();
		calibMapMag.put(calibDetailsMag4.mRangeValue, calibDetailsMag4);
		calibMapMag.put(calibDetailsMag8.mRangeValue, calibDetailsMag8);
		calibMapMag.put(calibDetailsMag12.mRangeValue, calibDetailsMag12);
		calibMapMag.put(calibDetailsMag16.mRangeValue, calibDetailsMag16);
		setCalibrationMapPerSensor(Configuration.Shimmer3.SENSOR_ID.SHIMMER_LIS3MDL_MAG, calibMapMag);
		
		updateCurrentMagCalibInUse();
	}
	
	//--------- Optional methods to override in Sensor Class end --------
	
	//--------- Sensor specific methods start --------------
	
	public void setLIS3MDLMagRange(int valueToSet){
		if(ArrayUtils.contains(ListofLIS3MDLMagRangeConfigValues, valueToSet)){
			mMagRange = valueToSet;
			updateCurrentMagCalibInUse();
		}
	}
	
	public boolean checkLowPowerMag() {
		setLowPowerMag((getLIS3MDLMagRate() <= 14)? true:false);
		return isLowPowerMagEnabled();
	}
	
	public boolean checkMedPowerMag() {
		setMedPowerMag((getLIS3MDLMagRate() >= 17 && getLIS3MDLMagRate() <= 30)? true:false);
		return isMedPowerMagEnabled();
	}
	
	public boolean checkHighPowerMag() {
		setHighPowerMag((getLIS3MDLMagRate() >= 33 && getLIS3MDLMagRate() <= 46)? true:false);
		return isHighPowerMagEnabled();
	}
	
	public boolean checkUltraHighPowerMag() {
		setUltraHighPowerMag((getLIS3MDLMagRate() >= 49 && getLIS3MDLMagRate() <= 62)? true:false); 
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
			magRate = 0;
		} else if (freq<=2.5) {
			magRate = 2;
		} else if (freq<=5) {
			magRate = 4;
		} else if (freq<=10) {
			magRate = 6;
		} else if (freq<=20) {
			magRate = 8;
		} else if (freq<=40) {
			magRate = 10;
		} else if (freq<=80) {
			magRate = 12;
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
	public int getMagRateFromFreqForSensor(boolean isEnabled, double freq) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	//--------- Sensor specific methods end --------------

}
