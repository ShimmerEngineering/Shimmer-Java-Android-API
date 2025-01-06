package com.shimmerresearch.sensors.bmpX80;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;

import com.shimmerresearch.driver.Configuration.CHANNEL_UNITS;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driver.Configuration.Shimmer3.CompatibilityInfoForMaps;
import com.shimmerresearch.driver.shimmer2r3.ConfigByteLayoutShimmer3;
import com.shimmerresearch.bluetooth.BtCommandDetails;
import com.shimmerresearch.driver.ConfigByteLayout;
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
import com.shimmerresearch.sensors.bmpX80.SensorBMP390.DatabaseChannelHandles;
import com.shimmerresearch.sensors.bmpX80.SensorBMP390.DatabaseConfigHandle;
import com.shimmerresearch.sensors.bmpX80.SensorBMP390.ObjectClusterSensorName;
import com.shimmerresearch.sensors.bmpX80.SensorBMPX80.GuiLabelConfig;
import com.shimmerresearch.sensors.bmpX80.SensorBMPX80.GuiLabelSensors;
import com.shimmerresearch.sensors.bmpX80.SensorBMPX80.LABEL_SENSOR_TILE;

public class SensorBMP390 extends SensorBMPX80{
	private static final long serialVersionUID = -8614398693693822030L;
	
	//--------- Sensor specific variables start --------------
	
		/** Calibration handled on chip for Shimmer4 - might change in the future */	

		private CalibDetailsBmp390 mCalibDetailsBmp390Lcl;
		public static class DatabaseChannelHandles{
			public static final String PRESSURE_BMP390 = "BMP390_Pressure";
			public static final String TEMPERATURE_BMP390 = "BMP390_Temperature";
		}
		public static final class DatabaseConfigHandle{
			public static final String PRESSURE_PRECISION_BMP390 = "BMP390_Pressure_Precision";
			public static final String PRESSURE_RATE = "BMP390_Pres_Rate";

			public static final String PAR_T1 = "BMP390_PAR_T1";
			public static final String PAR_T2 = "BMP390_PAR_T2";
			public static final String PAR_T3 = "BMP390_PAR_T3";
			public static final String PAR_P1 = "BMP390_PAR_P1";
			public static final String PAR_P2 = "BMP390_PAR_P2";
			public static final String PAR_P3 = "BMP390_PAR_P3";
			public static final String PAR_P4 = "BMP390_PAR_P4";
			public static final String PAR_P5 = "BMP390_PAR_P5";
			public static final String PAR_P6 = "BMP390_PAR_P6";
			public static final String PAR_P7 = "BMP390_PAR_P7";
			public static final String PAR_P8 = "BMP390_PAR_P8";
			public static final String PAR_P9 = "BMP390_PAR_P9";
			public static final String PAR_P10 = "BMP390_PAR_P10";
			public static final String PAR_P11 = "BMP390_PAR_P11";

			
			public static final List<String> LIST_OF_CALIB_HANDLES = Arrays.asList(
					DatabaseConfigHandle.PAR_T1, DatabaseConfigHandle.PAR_T2, DatabaseConfigHandle.PAR_T3,
					DatabaseConfigHandle.PAR_P1, DatabaseConfigHandle.PAR_P2, DatabaseConfigHandle.PAR_P3,
					DatabaseConfigHandle.PAR_P4, DatabaseConfigHandle.PAR_P5, DatabaseConfigHandle.PAR_P6,
					DatabaseConfigHandle.PAR_P7, DatabaseConfigHandle.PAR_P8, DatabaseConfigHandle.PAR_P9,
					DatabaseConfigHandle.PAR_P10, DatabaseConfigHandle.PAR_P11);
		}

	   //--------- Constructors for this class start --------------
    
		public SensorBMP390(ShimmerVerObject svo) {
			super(SENSORS.BMP390, svo);
			initialise();
		}

		public SensorBMP390(ShimmerDevice shimmerDevice) {
			super(SENSORS.BMP390, shimmerDevice);
			initialise();
		}

		public static final class ObjectClusterSensorName{
			public static final String TEMPERATURE_BMP390 = "Temperature_BMP390";
			public static final String PRESSURE_BMP390 = "Pressure_BMP390";
		}
		//--------- Sensor specific variables end --------------
		
		//--------- Bluetooth commands start --------------
		public static final byte SET_PRESSURE_OVERSAMPLING_RATIO_COMMAND 	= (byte) 0x52;
		public static final byte PRESSURE_OVERSAMPLING_RATIO_RESPONSE 		= (byte) 0x53;
		public static final byte GET_PRESSURE_OVERSAMPLING_RATIO_COMMAND 	= (byte) 0x54;
		
		public static final byte PRESSURE_CALIBRATION_COEFFICIENTS_RESPONSE 		= (byte) 0xA6;
		public static final byte GET_PRESSURE_CALIBRATION_COEFFICIENTS_COMMAND 		= (byte) 0xA7;
		
		public static final byte SET_PRESSURE_SAMPLING_RATE_COMMAND 	= (byte) 0xB5;
		public static final byte PRESSURE_SAMPLING_RATE_RESPONSE 		= (byte) 0xB6;
		public static final byte GET_PRESSURE_SAMPLING_RATE_COMMAND 	= (byte) 0xB7;
		
	    public static final Map<Byte, BtCommandDetails> mBtGetCommandMap;
	    static {
	        Map<Byte, BtCommandDetails> aMap = new LinkedHashMap<Byte, BtCommandDetails>();
	        aMap.put(GET_PRESSURE_OVERSAMPLING_RATIO_COMMAND, new BtCommandDetails(GET_PRESSURE_OVERSAMPLING_RATIO_COMMAND, "GET_PRESSURE_OVERSAMPLING_RATIO_COMMAND", PRESSURE_OVERSAMPLING_RATIO_RESPONSE));
	        aMap.put(GET_PRESSURE_CALIBRATION_COEFFICIENTS_COMMAND, new BtCommandDetails(GET_PRESSURE_CALIBRATION_COEFFICIENTS_COMMAND, "GET_PRESSURE_CALIBRATION_COEFFICIENTS_COMMAND", PRESSURE_CALIBRATION_COEFFICIENTS_RESPONSE));
	        aMap.put(GET_PRESSURE_SAMPLING_RATE_COMMAND, new BtCommandDetails(GET_PRESSURE_SAMPLING_RATE_COMMAND, "GET_PRESSURE_SAMPLING_RATE_COMMAND", PRESSURE_SAMPLING_RATE_RESPONSE));
	        mBtGetCommandMap = Collections.unmodifiableMap(aMap);
	    }
	    
	    public static final Map<Byte, BtCommandDetails> mBtSetCommandMap;
	    static {
	        Map<Byte, BtCommandDetails> aMap = new LinkedHashMap<Byte, BtCommandDetails>();
	        aMap.put(SET_PRESSURE_OVERSAMPLING_RATIO_COMMAND, new BtCommandDetails(SET_PRESSURE_OVERSAMPLING_RATIO_COMMAND, "SET_PRESSURE_OVERSAMPLING_RATIO_COMMAND"));
	        aMap.put(SET_PRESSURE_SAMPLING_RATE_COMMAND, new BtCommandDetails(SET_PRESSURE_SAMPLING_RATE_COMMAND, "SET_PRESSURE_SAMPLING_RATE_COMMAND"));
	        mBtSetCommandMap = Collections.unmodifiableMap(aMap);
	    }
	    //--------- Bluetooth commands end --------------
		
		//--------- Configuration options start --------------
		public static final String[] ListofPressureResolutionBMP390 = {"Ultra Low","Low","Standard","High","Ultra High","Highest"};
		public static final Integer[] ListofPressureResolutionConfigValuesBMP390 = {0,1,2,3,4,5};
		public static final String[] ListofPressureRateBMP390 = {"200.0Hz","100.0Hz","50.0Hz","25.0Hz", "12.5Hz", "6.25Hz", "3.1Hz", "1.5Hz", "0.78Hz", "0.39Hz", "0.2Hz", "0.1Hz", "0.05Hz", "0.02Hz", "0.01Hz", "0.006Hz", "0.003Hz", "0.0015Hz"};
		public static final Integer[] ListofPressureRateConfigValuesBMP390 = {0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17};

		public static final ConfigOptionDetailsSensor configOptionPressureResolutionBMP390 = new ConfigOptionDetailsSensor(
				SensorBMPX80.GuiLabelConfig.PRESSURE_RESOLUTION,
				SensorBMP390.DatabaseConfigHandle.PRESSURE_PRECISION_BMP390,
				ListofPressureResolutionBMP390, 
				ListofPressureResolutionConfigValuesBMP390, 
				ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX,
				CompatibilityInfoForMaps.listOfCompatibleVersionInfoBMP390);
		
		public static final ConfigOptionDetailsSensor configOptionPressureRateBMP390 = new ConfigOptionDetailsSensor(
				SensorBMPX80.GuiLabelConfig.PRESSURE_RATE,
				SensorBMP390.DatabaseConfigHandle.PRESSURE_RATE,
				ListofPressureRateBMP390, 
				ListofPressureRateConfigValuesBMP390, 
				ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX,
				CompatibilityInfoForMaps.listOfCompatibleVersionInfoBMP390);
		
		//--------- Configuration options end --------------

		//--------- Sensor info start --------------
		public static final SensorDetailsRef sensorBmp390 = new SensorDetailsRef(
				0x04<<(2*8), 
				0x04<<(2*8), 
				GuiLabelSensors.PRESS_TEMP_BMPX80,
				CompatibilityInfoForMaps.listOfCompatibleVersionInfoBMP390,
				Arrays.asList(GuiLabelConfig.PRESSURE_RESOLUTION),
				Arrays.asList(ObjectClusterSensorName.TEMPERATURE_BMP390,
						ObjectClusterSensorName.PRESSURE_BMP390));

		public static final Map<Integer, SensorDetailsRef> mSensorMapRef;
		static {
			Map<Integer, SensorDetailsRef> aMap = new LinkedHashMap<Integer, SensorDetailsRef>();
			aMap.put(Configuration.Shimmer3.SENSOR_ID.SHIMMER_BMP390_PRESSURE, SensorBMP390.sensorBmp390);
			mSensorMapRef = Collections.unmodifiableMap(aMap);
		}
		
	    public static final SensorGroupingDetails sensorGroupBmp390 = new SensorGroupingDetails(
				LABEL_SENSOR_TILE.PRESSURE_TEMPERATURE,
				Arrays.asList(Configuration.Shimmer3.SENSOR_ID.SHIMMER_BMP390_PRESSURE),
				CompatibilityInfoForMaps.listOfCompatibleVersionInfoBMP390);

		//--------- Sensor info end --------------
	    
		//--------- Channel info start --------------
		public static final ChannelDetails channelBmp390Press = new ChannelDetails(
				ObjectClusterSensorName.PRESSURE_BMP390,
				ObjectClusterSensorName.PRESSURE_BMP390,
				DatabaseChannelHandles.PRESSURE_BMP390,
				CHANNEL_DATA_TYPE.UINT24, 3, CHANNEL_DATA_ENDIAN.MSB,
				CHANNEL_UNITS.KPASCAL,
				Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
		{
			//TODO put into above constructor
//			channelBmp390Press.mChannelSource = CHANNEL_SOURCE.SHIMMER;
//			channelBmp390Press.mDefaultUncalUnit = CHANNEL_UNITS.KPASCAL;
//			channelBmp390Press.mChannelFormatDerivedFromShimmerDataPacket = CHANNEL_TYPE.UNCAL;
		}

		public static final ChannelDetails channelBmp390Temp = new ChannelDetails(
				ObjectClusterSensorName.TEMPERATURE_BMP390,
				ObjectClusterSensorName.TEMPERATURE_BMP390,
				DatabaseChannelHandles.TEMPERATURE_BMP390,
				CHANNEL_DATA_TYPE.UINT24, 2, CHANNEL_DATA_ENDIAN.MSB,
				CHANNEL_UNITS.DEGREES_CELSIUS,
				Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
		{
			//TODO put into above constructor
//			channelBmp390Temp.mChannelSource = CHANNEL_SOURCE.SHIMMER;
//			channelBmp390Temp.mDefaultUncalUnit = CHANNEL_UNITS.DEGREES_CELSUIS;
//			channelBmp390Temp.mChannelFormatDerivedFromShimmerDataPacket = CHANNEL_TYPE.UNCAL;
		}
		public static final Map<String, ChannelDetails> mChannelMapRef;
	    static {
	        Map<String, ChannelDetails> aMap = new LinkedHashMap<String, ChannelDetails>();
	        aMap.put(ObjectClusterSensorName.PRESSURE_BMP390, channelBmp390Press);
	        aMap.put(ObjectClusterSensorName.TEMPERATURE_BMP390, channelBmp390Temp);
			mChannelMapRef = Collections.unmodifiableMap(aMap);
	    }
	    
	   //--------- Constructors for this class end --------------
	/**
	 * 
	 */

	@Override
	public void setPressureResolution(int i) {
		if(ArrayUtils.contains(ListofPressureResolutionConfigValuesBMP390, i)){
			System.err.println("New resolution:\t" + ListofPressureResolutionConfigValuesBMP390[i]);
			mPressureResolution = i;
		}
		updateCurrentPressureCalibInUse();
		
	}

	@Override
	public List<Double> getPressTempConfigValuesLegacy() {
		List<Double> configValues = new ArrayList<Double>();

		CalibDetailsBmp390 calibDetailsBmp390 = ((CalibDetailsBmp390)mCalibDetailsBmpX80);
		configValues.add(calibDetailsBmp390.par_T1);
		configValues.add(calibDetailsBmp390.par_T2);
		configValues.add(calibDetailsBmp390.par_T3);
		configValues.add(calibDetailsBmp390.par_P1);
		configValues.add(calibDetailsBmp390.par_P2);
		configValues.add(calibDetailsBmp390.par_P3);
		configValues.add(calibDetailsBmp390.par_P4);
		configValues.add(calibDetailsBmp390.par_P5);
		configValues.add(calibDetailsBmp390.par_P6);
		configValues.add(calibDetailsBmp390.par_P7);
		configValues.add(calibDetailsBmp390.par_P8);
		configValues.add(calibDetailsBmp390.par_P9);
		configValues.add(calibDetailsBmp390.par_P10);
		configValues.add(calibDetailsBmp390.par_P11);

		return configValues;
	}

	@Override
	public void generateSensorMap() {
		super.createLocalSensorMapWithCustomParser(mSensorMapRef, mChannelMapRef);
	}

	@Override
	public void generateConfigOptionsMap() {
		addConfigOption(configOptionPressureResolutionBMP390);		
		addConfigOption(configOptionPressureRateBMP390);
	}

	@Override
	public void generateSensorGroupMapping() {
		mSensorGroupingMap = new LinkedHashMap<Integer, SensorGroupingDetails>();
		//TODO Extra version check here not needed because compatability info already contained in SensorGroupingDetails?
		if(mShimmerVerObject.isShimmerGen3() || mShimmerVerObject.isShimmerGen3R() || mShimmerVerObject.isShimmerGen4()){
			mSensorGroupingMap.put(Configuration.Shimmer3.LABEL_SENSOR_TILE.PRESSURE_TEMPERATURE_BMP390.ordinal(), sensorGroupBmp390);
		}
		super.updateSensorGroupingMap();
	}

	@Override
	public ObjectCluster processDataCustom(SensorDetails sensorDetails, byte[] rawData, COMMUNICATION_TYPE commType,
			ObjectCluster objectCluster, boolean isTimeSyncEnabled, double pctimeStampMs) {
		objectCluster = sensorDetails.processDataCommon(rawData, commType, objectCluster, isTimeSyncEnabled, pctimeStampMs);

		for (ChannelDetails channelDetails:sensorDetails.mListOfChannels){
			if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.PRESSURE_BMP390)){
				double calPressure = ((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(ObjectClusterSensorName.PRESSURE_BMP390), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
				calPressure = calPressure/1000;
				objectCluster.addCalData(channelDetails, calPressure, objectCluster.getIndexKeeper()-2);
				objectCluster.incrementIndexKeeper();
			}
			if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.TEMPERATURE_BMP390)){
				double calTemp = ((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(ObjectClusterSensorName.TEMPERATURE_BMP390),channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
				calTemp = calTemp/100;
				objectCluster.addCalData(channelDetails, calTemp, objectCluster.getIndexKeeper()-1);
				objectCluster.incrementIndexKeeper();
			}
		}

		//Debugging
		super.consolePrintChannelsCal(objectCluster, Arrays.asList(
				new String[]{ObjectClusterSensorName.PRESSURE_BMP390, CHANNEL_TYPE.UNCAL.toString()}, 
				new String[]{ObjectClusterSensorName.TEMPERATURE_BMP390, CHANNEL_TYPE.UNCAL.toString()}, 
				new String[]{ObjectClusterSensorName.PRESSURE_BMP390, CHANNEL_TYPE.CAL.toString()}, 
				new String[]{ObjectClusterSensorName.TEMPERATURE_BMP390, CHANNEL_TYPE.CAL.toString()}));

		return objectCluster;
	}
	
	@Override
	public void generateCalibMap() {
		mCalibDetailsBmpX80 = new CalibDetailsBmp390();
		mCalibDetailsBmp390Lcl = (CalibDetailsBmp390) mCalibDetailsBmpX80;
		super.generateCalibMap();
	}

	@Override
	public void checkShimmerConfigBeforeConfiguring() {
		if(!isSensorEnabled(Configuration.Shimmer3.SENSOR_ID.SHIMMER_BMP390_PRESSURE)) {
			setDefaultBmp390PressureSensorConfig(false);
		}
	}

	@Override
	public Object setConfigValueUsingConfigLabel(Integer sensorId, String configLabel, Object valueToSet) {
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
	public Object getConfigValueUsingConfigLabel(Integer sensorId, String configLabel) {
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
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean setDefaultConfigForSensor(int sensorId, boolean isSensorEnabled) {
		if(mSensorMap.containsKey(sensorId)){
			if(sensorId == Configuration.Shimmer3.SENSOR_ID.SHIMMER_BMP390_PRESSURE) {
				setDefaultBmp390PressureSensorConfig(isSensorEnabled);
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ActionSetting setSettings(String componentName, Object valueToSet, COMMUNICATION_TYPE commType) {

		ActionSetting actionSetting = new ActionSetting(commType);
		switch(componentName){
		case(GuiLabelConfig.PRESSURE_RESOLUTION):
			setPressureResolution((int)valueToSet);
		break;
		}
		return actionSetting;
	}

	@Override
	public boolean processResponse(int responseCommand, Object parsedResponse, COMMUNICATION_TYPE commType) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public LinkedHashMap<String, Object> generateConfigMap() {
		LinkedHashMap<String, Object> mapOfConfig = new LinkedHashMap<String, Object>();
		mapOfConfig.put(DatabaseConfigHandle.PRESSURE_PRECISION_BMP390, getPressureResolution());
		
		mapOfConfig.put(DatabaseConfigHandle.PAR_T1, mCalibDetailsBmp390Lcl.par_T1);
		mapOfConfig.put(DatabaseConfigHandle.PAR_T2, mCalibDetailsBmp390Lcl.par_T2);
		mapOfConfig.put(DatabaseConfigHandle.PAR_T3, mCalibDetailsBmp390Lcl.par_T3);
		mapOfConfig.put(DatabaseConfigHandle.PAR_P1, mCalibDetailsBmp390Lcl.par_P1);
		mapOfConfig.put(DatabaseConfigHandle.PAR_P2, mCalibDetailsBmp390Lcl.par_P2);
		mapOfConfig.put(DatabaseConfigHandle.PAR_P3, mCalibDetailsBmp390Lcl.par_P3);
		mapOfConfig.put(DatabaseConfigHandle.PAR_P4, mCalibDetailsBmp390Lcl.par_P4);
		mapOfConfig.put(DatabaseConfigHandle.PAR_P5, mCalibDetailsBmp390Lcl.par_P5);
		mapOfConfig.put(DatabaseConfigHandle.PAR_P6, mCalibDetailsBmp390Lcl.par_P6);
		mapOfConfig.put(DatabaseConfigHandle.PAR_P7, mCalibDetailsBmp390Lcl.par_P7);
		mapOfConfig.put(DatabaseConfigHandle.PAR_P8, mCalibDetailsBmp390Lcl.par_P8);
		mapOfConfig.put(DatabaseConfigHandle.PAR_P9, mCalibDetailsBmp390Lcl.par_P9);
		mapOfConfig.put(DatabaseConfigHandle.PAR_P10, mCalibDetailsBmp390Lcl.par_P10);
		mapOfConfig.put(DatabaseConfigHandle.PAR_P11, mCalibDetailsBmp390Lcl.par_P11);

		return mapOfConfig;
	}

	@Override
	public void parseConfigMap(LinkedHashMap<String, Object> mapOfConfigPerShimmer) {
		if(mapOfConfigPerShimmer.containsKey(DatabaseConfigHandle.PRESSURE_PRECISION_BMP390)){
			setPressureResolution(((Double) mapOfConfigPerShimmer.get(DatabaseConfigHandle.PRESSURE_PRECISION_BMP390)).intValue());
		}
		//PRESSURE (BMP390) CAL PARAMS
		if(mapOfConfigPerShimmer.containsKey(DatabaseConfigHandle.PAR_T1)
				&& mapOfConfigPerShimmer.containsKey(DatabaseConfigHandle.PAR_T2)
				&& mapOfConfigPerShimmer.containsKey(DatabaseConfigHandle.PAR_T3)
				&& mapOfConfigPerShimmer.containsKey(DatabaseConfigHandle.PAR_P1)
				&& mapOfConfigPerShimmer.containsKey(DatabaseConfigHandle.PAR_P2)
				&& mapOfConfigPerShimmer.containsKey(DatabaseConfigHandle.PAR_P3)
				&& mapOfConfigPerShimmer.containsKey(DatabaseConfigHandle.PAR_P4)
				&& mapOfConfigPerShimmer.containsKey(DatabaseConfigHandle.PAR_P5)
				&& mapOfConfigPerShimmer.containsKey(DatabaseConfigHandle.PAR_P6)
				&& mapOfConfigPerShimmer.containsKey(DatabaseConfigHandle.PAR_P7)
				&& mapOfConfigPerShimmer.containsKey(DatabaseConfigHandle.PAR_P8)
				&& mapOfConfigPerShimmer.containsKey(DatabaseConfigHandle.PAR_P9)
				&& mapOfConfigPerShimmer.containsKey(DatabaseConfigHandle.PAR_P10)
				&& mapOfConfigPerShimmer.containsKey(DatabaseConfigHandle.PAR_P11)
				){
			
			setPressureCalib(
					(Double) mapOfConfigPerShimmer.get(DatabaseConfigHandle.PAR_T1),
					(Double) mapOfConfigPerShimmer.get(DatabaseConfigHandle.PAR_T2),
					(Double) mapOfConfigPerShimmer.get(DatabaseConfigHandle.PAR_T3),
					(Double) mapOfConfigPerShimmer.get(DatabaseConfigHandle.PAR_P1),
					(Double) mapOfConfigPerShimmer.get(DatabaseConfigHandle.PAR_P2),
					(Double) mapOfConfigPerShimmer.get(DatabaseConfigHandle.PAR_P3),
					(Double) mapOfConfigPerShimmer.get(DatabaseConfigHandle.PAR_P4),
					(Double) mapOfConfigPerShimmer.get(DatabaseConfigHandle.PAR_P5),
					(Double) mapOfConfigPerShimmer.get(DatabaseConfigHandle.PAR_P6),
					(Double) mapOfConfigPerShimmer.get(DatabaseConfigHandle.PAR_P7),
					(Double) mapOfConfigPerShimmer.get(DatabaseConfigHandle.PAR_P8),
					(Double) mapOfConfigPerShimmer.get(DatabaseConfigHandle.PAR_P9),
					(Double) mapOfConfigPerShimmer.get(DatabaseConfigHandle.PAR_P10),
					(Double) mapOfConfigPerShimmer.get(DatabaseConfigHandle.PAR_P11));
		}		
	}
	
	//--------- Sensor specific methods start --------------	
	
	public void setPressureCalib(
			double T1, double T2, double T3,
			double P1, double P2, double P3, 
			double P4, double P5, double P6, 
			double P7, double P8, double P9,
			double P10, double P11) {
		mCalibDetailsBmp390Lcl.setPressureCalib(T1, T2, T3, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11);
	}
	
	private void setDefaultBmp390PressureSensorConfig(boolean isSensorEnabled) {
		if(isSensorEnabled) {
		}
		else{
			mPressureResolution = 0;
		}
	}
	
	public static String parseFromDBColumnToGUIChannel(String databaseChannelHandle) {
		
		return AbstractSensor.parseFromDBColumnToGUIChannel(mChannelMapRef, databaseChannelHandle);
	}
	
	public static String parseFromGUIChannelsToDBColumn(String objectClusterName) {
		
		return AbstractSensor.parseFromGUIChannelsToDBColumn(mChannelMapRef, objectClusterName);
	}
	
	//--------- Sensor specific methods end --------------
	
	@Override
	public void configBytesParse(ShimmerDevice shimmerDevice, byte[] configBytes, COMMUNICATION_TYPE commType) {
	    ConfigByteLayout configByteLayout = shimmerDevice.getConfigByteLayout();
	    if (configByteLayout instanceof ConfigByteLayoutShimmer3) {
	        ConfigByteLayoutShimmer3 configByteLayoutCast = (ConfigByteLayoutShimmer3) configByteLayout;
	        
	        int lsbPressureResolution = (configBytes[configByteLayoutCast.idxConfigSetupByte3] 
	                >> configByteLayoutCast.bitShiftBMPX80PressureResolution) 
	                & configByteLayoutCast.maskBMPX80PressureResolution;

	        int msbPressureResolution = (configBytes[configByteLayoutCast.idxConfigSetupByte4] 
	                >> configByteLayoutCast.bitShiftBMP390PressureResolution) 
	                & configByteLayoutCast.maskBMP390PressureResolution;

	        setPressureResolution(((msbPressureResolution << 2) | lsbPressureResolution));
	    }
	}
	
	public void configBytesGenerate(ShimmerDevice shimmerDevice, byte[] configBytes, COMMUNICATION_TYPE commType) {

		ConfigByteLayout configByteLayout = shimmerDevice.getConfigByteLayout();
		if(configByteLayout instanceof ConfigByteLayoutShimmer3){
			ConfigByteLayoutShimmer3 configByteLayoutCast = (ConfigByteLayoutShimmer3) configByteLayout;		
			configBytes[configByteLayoutCast.idxConfigSetupByte4] |= (byte) (((getPressureResolution() >> 2)& configByteLayoutCast.maskBMP390PressureResolution) << configByteLayoutCast.bitShiftBMP390PressureResolution);
			configBytes[configByteLayoutCast.idxConfigSetupByte3] |= (byte) ((getPressureResolution() & configByteLayoutCast.maskBMPX80PressureResolution) << configByteLayoutCast.bitShiftBMPX80PressureResolution);
		}
	}

}
