package com.shimmerresearch.sensors.bmpX80;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;

import com.shimmerresearch.bluetooth.BtCommandDetails;
import com.shimmerresearch.driver.Configuration.CHANNEL_UNITS;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driver.Configuration.Shimmer3.CompatibilityInfoForMaps;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.FormatCluster;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driverUtilities.ChannelDetails;
import com.shimmerresearch.driverUtilities.ConfigOptionDetailsSensor;
import com.shimmerresearch.driverUtilities.SensorDetails;
import com.shimmerresearch.driverUtilities.SensorDetailsRef;
import com.shimmerresearch.driverUtilities.SensorGroupingDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_ENDIAN;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_TYPE;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_TYPE;
import com.shimmerresearch.sensors.AbstractSensor;
import com.shimmerresearch.sensors.ActionSetting;
import com.shimmerresearch.sensors.AbstractSensor.SENSORS;
import com.shimmerresearch.sensors.bmpX80.SensorBMP180.DatabaseConfigHandle;

/**
 * @author Ronan McCormack
 * @author Mark Nolan
 *
 */
//TODO update compatibility maps
public class SensorBMP280 extends SensorBMPX80 {

	/** * */
	private static final long serialVersionUID = 5173164657730440965L;
	
	//--------- Sensor specific variables start --------------
	
	/** Calibration handled on chip for Shimmer4 - might change in the future */	

	private CalibDetailsBmp280 mCalibDetailsBmp280Lcl;
	
	public static class DatabaseChannelHandles{
		public static final String PRESSURE_BMP280 = "BMP280_Pressure";
		public static final String TEMPERATURE_BMP280 = "BMP280_Temperature";
	}
	public static final class DatabaseConfigHandle{
		public static final String PRESSURE_PRECISION_BMP280 = "BMP280_Pressure_Precision";
		
		public static final String DIG_T1 = "BMP280_DIG_T1";
		public static final String DIG_T2 = "BMP280_DIG_T2";
		public static final String DIG_T3 = "BMP280_DIG_T3";
		public static final String DIG_P1 = "BMP280_DIG_P1";
		public static final String DIG_P2 = "BMP280_DIG_P2";
		public static final String DIG_P3 = "BMP280_DIG_P3";
		public static final String DIG_P4 = "BMP280_DIG_P4";
		public static final String DIG_P5 = "BMP280_DIG_P5";
		public static final String DIG_P6 = "BMP280_DIG_P6";
		public static final String DIG_P7 = "BMP280_DIG_P7";
		public static final String DIG_P8 = "BMP280_DIG_P8";
		public static final String DIG_P9 = "BMP280_DIG_P9";
		
		public static final List<String> LIST_OF_CALIB_HANDLES = Arrays.asList(
				DatabaseConfigHandle.DIG_T1, DatabaseConfigHandle.DIG_T2, DatabaseConfigHandle.DIG_T3,
				DatabaseConfigHandle.DIG_P1, DatabaseConfigHandle.DIG_P2, DatabaseConfigHandle.DIG_P3,
				DatabaseConfigHandle.DIG_P4, DatabaseConfigHandle.DIG_P5, DatabaseConfigHandle.DIG_P6,
				DatabaseConfigHandle.DIG_P7, DatabaseConfigHandle.DIG_P8, DatabaseConfigHandle.DIG_P9);
	}
	
	public static final class ObjectClusterSensorName{
		public static final String TEMPERATURE_BMP280 = "Temperature_BMP280";
		public static final String PRESSURE_BMP280 = "Pressure_BMP280";
	}
	//--------- Sensor specific variables end --------------
	
	//--------- Bluetooth commands start --------------
//	public static final byte SET_BMP280_PRES_RESOLUTION_COMMAND 	= (byte) 0x52;
//	public static final byte BMP280_PRES_RESOLUTION_RESPONSE 		= (byte) 0x53;
//	public static final byte GET_BMP280_PRES_RESOLUTION_COMMAND 	= (byte) 0x54;
//	public static final byte SET_BMP280_PRES_CALIBRATION_COMMAND	= (byte) 0x55;
//	public static final byte BMP280_PRES_CALIBRATION_RESPONSE 		= (byte) 0x56;
//	public static final byte GET_BMP280_PRES_CALIBRATION_COMMAND 	= (byte) 0x57;
//	public static final byte BMP280_CALIBRATION_COEFFICIENTS_RESPONSE = (byte) 0x58;
//	public static final byte GET_BMP280_CALIBRATION_COEFFICIENTS_COMMAND = (byte) 0x59;
//
//	public static final Map<Byte, BtCommandDetails> mBtGetCommandMap;
//	static {
//		Map<Byte, BtCommandDetails> aMap = new LinkedHashMap<Byte, BtCommandDetails>();
//		aMap.put(GET_BMP280_PRES_RESOLUTION_COMMAND, new BtCommandDetails(GET_BMP280_PRES_RESOLUTION_COMMAND, "GET_BMP280_PRES_RESOLUTION_COMMAND", BMP280_PRES_RESOLUTION_RESPONSE));
//		aMap.put(GET_BMP280_PRES_CALIBRATION_COMMAND, new BtCommandDetails(GET_BMP280_PRES_CALIBRATION_COMMAND, "GET_BMP280_PRES_CALIBRATION_COMMAND", BMP280_PRES_CALIBRATION_RESPONSE));
//		aMap.put(GET_BMP280_CALIBRATION_COEFFICIENTS_COMMAND, new BtCommandDetails(GET_BMP280_CALIBRATION_COEFFICIENTS_COMMAND, "GET_BMP280_CALIBRATION_COEFFICIENTS_COMMAND", BMP280_CALIBRATION_COEFFICIENTS_RESPONSE));
//		mBtGetCommandMap = Collections.unmodifiableMap(aMap);
//	}
//
//	public static final Map<Byte, BtCommandDetails> mBtSetCommandMap;
//	static {
//		Map<Byte, BtCommandDetails> aMap = new LinkedHashMap<Byte, BtCommandDetails>();
//		aMap.put(SET_BMP280_PRES_RESOLUTION_COMMAND, new BtCommandDetails(SET_BMP280_PRES_RESOLUTION_COMMAND, "SET_BMP280_PRES_RESOLUTION_COMMAND"));
//		aMap.put(SET_BMP280_PRES_CALIBRATION_COMMAND, new BtCommandDetails(SET_BMP280_PRES_CALIBRATION_COMMAND, "SET_BMP280_PRES_CALIBRATION_COMMAND"));
//		mBtSetCommandMap = Collections.unmodifiableMap(aMap);
//	}
	
	//--------- Configuration options start --------------
	public static final String[] ListofPressureResolutionBMP280 = {"Low","Standard","High","Ultra High"};
	public static final Integer[] ListofPressureResolutionConfigValuesBMP280 = {0,1,2,3};

	public static final ConfigOptionDetailsSensor configOptionPressureResolutionBMP280 = new ConfigOptionDetailsSensor(
			SensorBMPX80.GuiLabelConfig.PRESSURE_RESOLUTION,
			SensorBMP280.DatabaseConfigHandle.PRESSURE_PRECISION_BMP280,
			ListofPressureResolutionBMP280, 
			ListofPressureResolutionConfigValuesBMP280, 
			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoBMP280);
	//--------- Configuration options end --------------

	//--------- Sensor info start --------------
	public static final SensorDetailsRef sensorBmp280 = new SensorDetailsRef(
			0x04<<(2*8), 
			0x04<<(2*8), 
			GuiLabelSensors.PRESS_TEMP_BMPX80,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoBMP280,
			Arrays.asList(GuiLabelConfig.PRESSURE_RESOLUTION),
			Arrays.asList(ObjectClusterSensorName.TEMPERATURE_BMP280,
					ObjectClusterSensorName.PRESSURE_BMP280));

	public static final Map<Integer, SensorDetailsRef> mSensorMapRef;
	static {
		Map<Integer, SensorDetailsRef> aMap = new LinkedHashMap<Integer, SensorDetailsRef>();
		aMap.put(Configuration.Shimmer3.SENSOR_ID.SHIMMER_BMPX80_PRESSURE, SensorBMP280.sensorBmp280);
		mSensorMapRef = Collections.unmodifiableMap(aMap);
	}
	
    public static final SensorGroupingDetails sensorGroupBmp280 = new SensorGroupingDetails(
			LABEL_SENSOR_TILE.PRESSURE_TEMPERATURE,
			Arrays.asList(Configuration.Shimmer3.SENSOR_ID.SHIMMER_BMPX80_PRESSURE),
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoBMP280);

	//--------- Sensor info end --------------
    
	//--------- Channel info start --------------
	public static final ChannelDetails channelBmp280Press = new ChannelDetails(
			ObjectClusterSensorName.PRESSURE_BMP280,
			ObjectClusterSensorName.PRESSURE_BMP280,
			DatabaseChannelHandles.PRESSURE_BMP280,
			CHANNEL_DATA_TYPE.UINT24, 3, CHANNEL_DATA_ENDIAN.MSB,
			CHANNEL_UNITS.KPASCAL,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
	{
		//TODO put into above constructor
//		channelBmp280Press.mChannelSource = CHANNEL_SOURCE.SHIMMER;
//		channelBmp280Press.mDefaultUncalUnit = CHANNEL_UNITS.KPASCAL;
//		channelBmp280Press.mChannelFormatDerivedFromShimmerDataPacket = CHANNEL_TYPE.UNCAL;
	}

	public static final ChannelDetails channelBmp280Temp = new ChannelDetails(
			ObjectClusterSensorName.TEMPERATURE_BMP280,
			ObjectClusterSensorName.TEMPERATURE_BMP280,
			DatabaseChannelHandles.TEMPERATURE_BMP280,
			CHANNEL_DATA_TYPE.UINT16, 2, CHANNEL_DATA_ENDIAN.MSB,
			CHANNEL_UNITS.DEGREES_CELSIUS,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
	{
		//TODO put into above constructor
//		channelBmp280Temp.mChannelSource = CHANNEL_SOURCE.SHIMMER;
//		channelBmp280Temp.mDefaultUncalUnit = CHANNEL_UNITS.DEGREES_CELSUIS;
//		channelBmp280Temp.mChannelFormatDerivedFromShimmerDataPacket = CHANNEL_TYPE.UNCAL;
	}
	public static final Map<String, ChannelDetails> mChannelMapRef;
    static {
        Map<String, ChannelDetails> aMap = new LinkedHashMap<String, ChannelDetails>();
        aMap.put(ObjectClusterSensorName.PRESSURE_BMP280, channelBmp280Press);
        aMap.put(ObjectClusterSensorName.TEMPERATURE_BMP280, channelBmp280Temp);
		mChannelMapRef = Collections.unmodifiableMap(aMap);
    }
    
   //--------- Constructors for this class start --------------
    
	public SensorBMP280(ShimmerVerObject svo) {
		super(SENSORS.BMP280, svo);
		initialise();
	}

	public SensorBMP280(ShimmerDevice shimmerDevice) {
		super(SENSORS.BMP280, shimmerDevice);
		initialise();
	}

   //--------- Constructors for this class end --------------


	@Override
	public void generateSensorMap() {
		super.createLocalSensorMapWithCustomParser(mSensorMapRef, mChannelMapRef);
	}

	@Override
	public void generateConfigOptionsMap() {
		addConfigOption(configOptionPressureResolutionBMP280);
	}

	@Override
	public void generateSensorGroupMapping() {
		mSensorGroupingMap = new LinkedHashMap<Integer, SensorGroupingDetails>();
		//TODO Extra version check here not needed because compatability info already contained in SensorGroupingDetails?
		if(mShimmerVerObject.isShimmerGen3() || mShimmerVerObject.isShimmerGen4()){
			mSensorGroupingMap.put(Configuration.Shimmer3.LABEL_SENSOR_TILE.PRESSURE_TEMPERATURE_BMP280.ordinal(), sensorGroupBmp280);
		}
		super.updateSensorGroupingMap();
	}

	@Override
	public void generateCalibMap() {
		mCalibDetailsBmpX80 = new CalibDetailsBmp280();
		mCalibDetailsBmp280Lcl = (CalibDetailsBmp280) mCalibDetailsBmpX80;
		super.generateCalibMap();
	}

	@Override
	public void checkShimmerConfigBeforeConfiguring() {
		if(!isSensorEnabled(Configuration.Shimmer3.SENSOR_ID.SHIMMER_BMPX80_PRESSURE)) {
			setDefaultBmp280PressureSensorConfig(false);
		}
	}

	@Override
	public ObjectCluster processDataCustom(SensorDetails sensorDetails, byte[] rawData, COMMUNICATION_TYPE commType, ObjectCluster objectCluster, boolean isTimeSyncEnabled, double pcTimestampMs) {
		objectCluster = sensorDetails.processDataCommon(rawData, commType, objectCluster, isTimeSyncEnabled, pcTimestampMs);

		for (ChannelDetails channelDetails:sensorDetails.mListOfChannels){
			if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.PRESSURE_BMP280)){
				double calPressure = ((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(ObjectClusterSensorName.PRESSURE_BMP280), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
				calPressure = calPressure/1000;
				objectCluster.addCalData(channelDetails, calPressure, objectCluster.getIndexKeeper()-2);
				objectCluster.incrementIndexKeeper();
			}
			if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.TEMPERATURE_BMP280)){
				double calTemp = ((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(ObjectClusterSensorName.TEMPERATURE_BMP280),channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
				calTemp = calTemp/100;
				objectCluster.addCalData(channelDetails, calTemp, objectCluster.getIndexKeeper()-1);
				objectCluster.incrementIndexKeeper();
			}
		}

		//Debugging
		super.consolePrintChannelsCal(objectCluster, Arrays.asList(
				new String[]{ObjectClusterSensorName.PRESSURE_BMP280, CHANNEL_TYPE.UNCAL.toString()}, 
				new String[]{ObjectClusterSensorName.TEMPERATURE_BMP280, CHANNEL_TYPE.UNCAL.toString()}, 
				new String[]{ObjectClusterSensorName.PRESSURE_BMP280, CHANNEL_TYPE.CAL.toString()}, 
				new String[]{ObjectClusterSensorName.TEMPERATURE_BMP280, CHANNEL_TYPE.CAL.toString()}));

		return objectCluster;
	}

	@Override
	public Object setConfigValueUsingConfigLabel(Integer sensorId,String configLabel, Object valueToSet) {
		Object returnValue = null;
		switch(configLabel){
			case(GuiLabelConfig.PRESSURE_RESOLUTION):
				setPressureResolution((int)valueToSet);
				returnValue = valueToSet;
		 		break;
		}
		return returnValue;
	}

	@Override
	public Object getConfigValueUsingConfigLabel(Integer sensorId,String configLabel) {
		Object returnValue = null;
		switch(configLabel){
		case(GuiLabelConfig.PRESSURE_RESOLUTION):
			returnValue = getPressureResolution();
	 		break;
		  }
		return returnValue;
	}

	@Override
	public void setSensorSamplingRate(double samplingRateHz) {
		// Not in this class
		
	}

	@Override
	public boolean setDefaultConfigForSensor(int sensorId, boolean isSensorEnabled) {
		if(mSensorMap.containsKey(sensorId)){
			if(sensorId == Configuration.Shimmer3.SENSOR_ID.SHIMMER_BMPX80_PRESSURE) {
				setDefaultBmp280PressureSensorConfig(isSensorEnabled);
				return true;
				}
		  }
	  return false;
	}

	@Override
	public boolean checkConfigOptionValues(String stringKey) {
		if(mConfigOptionsMap.containsKey(stringKey)){
			return true;
		}
		return false;
	}

	@Override
	public Object getSettings(String componentName, COMMUNICATION_TYPE commType) {
		return null;
	}

	@Override
	public ActionSetting setSettings(String componentName, Object valueToSet,COMMUNICATION_TYPE commType) {

		ActionSetting actionSetting = new ActionSetting(commType);
		switch(componentName){
		case(GuiLabelConfig.PRESSURE_RESOLUTION):
			setPressureResolution((int)valueToSet);
		break;
		}
		return actionSetting;
	}
	
	@Override
	public LinkedHashMap<String, Object> generateConfigMap() {
		LinkedHashMap<String, Object> mapOfConfig = new LinkedHashMap<String, Object>();
		mapOfConfig.put(DatabaseConfigHandle.PRESSURE_PRECISION_BMP280, getPressureResolution());
		
		mapOfConfig.put(DatabaseConfigHandle.DIG_T1, mCalibDetailsBmp280Lcl.dig_T1);
		mapOfConfig.put(DatabaseConfigHandle.DIG_T2, mCalibDetailsBmp280Lcl.dig_T2);
		mapOfConfig.put(DatabaseConfigHandle.DIG_T3, mCalibDetailsBmp280Lcl.dig_T3);
		mapOfConfig.put(DatabaseConfigHandle.DIG_P1, mCalibDetailsBmp280Lcl.dig_P1);
		mapOfConfig.put(DatabaseConfigHandle.DIG_P2, mCalibDetailsBmp280Lcl.dig_P2);
		mapOfConfig.put(DatabaseConfigHandle.DIG_P3, mCalibDetailsBmp280Lcl.dig_P3);
		mapOfConfig.put(DatabaseConfigHandle.DIG_P4, mCalibDetailsBmp280Lcl.dig_P4);
		mapOfConfig.put(DatabaseConfigHandle.DIG_P5, mCalibDetailsBmp280Lcl.dig_P5);
		mapOfConfig.put(DatabaseConfigHandle.DIG_P6, mCalibDetailsBmp280Lcl.dig_P6);
		mapOfConfig.put(DatabaseConfigHandle.DIG_P7, mCalibDetailsBmp280Lcl.dig_P7);
		mapOfConfig.put(DatabaseConfigHandle.DIG_P8, mCalibDetailsBmp280Lcl.dig_P8);
		mapOfConfig.put(DatabaseConfigHandle.DIG_P9, mCalibDetailsBmp280Lcl.dig_P9);

		return mapOfConfig;
	}
	
	@Override
	public void parseConfigMap(LinkedHashMap<String, Object> mapOfConfigPerShimmer) {
		if(mapOfConfigPerShimmer.containsKey(DatabaseConfigHandle.PRESSURE_PRECISION_BMP280)){
			setPressureResolution(((Double) mapOfConfigPerShimmer.get(DatabaseConfigHandle.PRESSURE_PRECISION_BMP280)).intValue());
		}
		//PRESSURE (BMP180) CAL PARAMS
		if(mapOfConfigPerShimmer.containsKey(DatabaseConfigHandle.DIG_T1)
				&& mapOfConfigPerShimmer.containsKey(DatabaseConfigHandle.DIG_T2)
				&& mapOfConfigPerShimmer.containsKey(DatabaseConfigHandle.DIG_T3)
				&& mapOfConfigPerShimmer.containsKey(DatabaseConfigHandle.DIG_P1)
				&& mapOfConfigPerShimmer.containsKey(DatabaseConfigHandle.DIG_P2)
				&& mapOfConfigPerShimmer.containsKey(DatabaseConfigHandle.DIG_P3)
				&& mapOfConfigPerShimmer.containsKey(DatabaseConfigHandle.DIG_P4)
				&& mapOfConfigPerShimmer.containsKey(DatabaseConfigHandle.DIG_P5)
				&& mapOfConfigPerShimmer.containsKey(DatabaseConfigHandle.DIG_P6)
				&& mapOfConfigPerShimmer.containsKey(DatabaseConfigHandle.DIG_P7)
				&& mapOfConfigPerShimmer.containsKey(DatabaseConfigHandle.DIG_P8)
				&& mapOfConfigPerShimmer.containsKey(DatabaseConfigHandle.DIG_P9)){
			
			setPressureCalib(
					(Double) mapOfConfigPerShimmer.get(DatabaseConfigHandle.DIG_T1),
					(Double) mapOfConfigPerShimmer.get(DatabaseConfigHandle.DIG_T2),
					(Double) mapOfConfigPerShimmer.get(DatabaseConfigHandle.DIG_T3),
					(Double) mapOfConfigPerShimmer.get(DatabaseConfigHandle.DIG_P1),
					(Double) mapOfConfigPerShimmer.get(DatabaseConfigHandle.DIG_P2),
					(Double) mapOfConfigPerShimmer.get(DatabaseConfigHandle.DIG_P3),
					(Double) mapOfConfigPerShimmer.get(DatabaseConfigHandle.DIG_P4),
					(Double) mapOfConfigPerShimmer.get(DatabaseConfigHandle.DIG_P5),
					(Double) mapOfConfigPerShimmer.get(DatabaseConfigHandle.DIG_P6),
					(Double) mapOfConfigPerShimmer.get(DatabaseConfigHandle.DIG_P7),
					(Double) mapOfConfigPerShimmer.get(DatabaseConfigHandle.DIG_P8),
					(Double) mapOfConfigPerShimmer.get(DatabaseConfigHandle.DIG_P9));
		}

	}

	@Override
	public boolean processResponse(int responseCommand, Object parsedResponse, COMMUNICATION_TYPE commType) {
		// TODO Auto-generated method stub
		return false;
	}
	
	
	//--------- Abstract methods implemented end --------------


	//--------- Sensor specific methods start --------------	
	public void setPressureCalib(
			double T1, double T2, double T3,
			double P1, double P2, double P3, 
			double P4, double P5, double P6, 
			double P7, double P8, double P9) {
		mCalibDetailsBmp280Lcl.setPressureCalib(T1, T2, T3, P1, P2, P3, P4, P5, P6, P7, P8, P9);
	}

	@Override
	public void setPressureResolution(int i){
		if(ArrayUtils.contains(ListofPressureResolutionConfigValuesBMP280, i)){
//			System.err.println("New resolution:\t" + ListofPressureResolution[i]);
			mPressureResolution = i;
		}
		updateCurrentPressureCalibInUse();
	}
	
	@Override
	public double[] calibratePressureSensorData(double UP, double UT) {
		UT = UT * Math.pow(2, 4);
		UP=UP/Math.pow(2,4);
		return super.calibratePressureSensorData(UP, UT);
	}

	@Override
	public List<Double> getPressTempConfigValuesLegacy() {
		List<Double> configValues = new ArrayList<Double>();

		CalibDetailsBmp280 calibDetailsBmp280 = ((CalibDetailsBmp280)mCalibDetailsBmpX80);
		configValues.add(calibDetailsBmp280.dig_T1);
		configValues.add(calibDetailsBmp280.dig_T2);
		configValues.add(calibDetailsBmp280.dig_T3);
		configValues.add(calibDetailsBmp280.dig_P1);
		configValues.add(calibDetailsBmp280.dig_P2);
		configValues.add(calibDetailsBmp280.dig_P3);
		configValues.add(calibDetailsBmp280.dig_P4);
		configValues.add(calibDetailsBmp280.dig_P5);
		configValues.add(calibDetailsBmp280.dig_P6);
		configValues.add(calibDetailsBmp280.dig_P7);
		configValues.add(calibDetailsBmp280.dig_P8);
		configValues.add(calibDetailsBmp280.dig_P9);
		
		return configValues;
	}
	
	private void setDefaultBmp280PressureSensorConfig(boolean isSensorEnabled) {
		if(isSensorEnabled) {
		}
		else{
			mPressureResolution = 0;
		}
	}

	public static String parseFromDBColumnToGUIChannel(String databaseChannelHandle) {
		//TODO Old approach, can be removed
//		String objectClusterName = "";
//		if (databaseChannelHandle.equals(SensorBMP280.DatabaseChannelHandles.TEMPERATURE_BMP280)) {
//			objectClusterName = SensorBMP280.ObjectClusterSensorName.TEMPERATURE_BMP280;
//		} else if (databaseChannelHandle.equals(SensorBMP280.DatabaseChannelHandles.PRESSURE_BMP280)) {
//			objectClusterName = SensorBMP280.ObjectClusterSensorName.PRESSURE_BMP280;
//		}
//		return objectClusterName;
		
		return AbstractSensor.parseFromDBColumnToGUIChannel(mChannelMapRef, databaseChannelHandle);
	}

	public static String parseFromGUIChannelsToDBColumn(String objectClusterName) {
		//TODO Old approach, can be removed
//		String databaseChannelHandle = "";
//		if (objectClusterName.equals(SensorBMP280.ObjectClusterSensorName.TEMPERATURE_BMP280)) {
//			databaseChannelHandle = SensorBMP280.DatabaseChannelHandles.TEMPERATURE_BMP280;
//		} else if (objectClusterName.equals(SensorBMP280.ObjectClusterSensorName.PRESSURE_BMP280)) {
//			databaseChannelHandle = SensorBMP280.DatabaseChannelHandles.PRESSURE_BMP280;
//		}
//		return databaseChannelHandle;
		
		return AbstractSensor.parseFromGUIChannelsToDBColumn(mChannelMapRef, objectClusterName);
	}
	

	
	//--------- Sensor specific methods end --------------


	
}
