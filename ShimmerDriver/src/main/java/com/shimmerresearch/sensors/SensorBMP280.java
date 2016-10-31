package com.shimmerresearch.sensors;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
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
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_SOURCE;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_TYPE;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.HW_ID;
import com.shimmerresearch.sensors.AbstractSensor.SENSORS;
import com.shimmerresearch.sensors.SensorBMP180.DatabaseConfigHandle;


/**
 * @author Ronan McCormack
 *
 */
public class SensorBMP280 extends AbstractSensor{

	/** * */
	private static final long serialVersionUID = 5173164657730440965L;
	
	//--------- Sensor specific variables start --------------
	
// ---------Calibration handled on chip-------------	
	
//	public double pressTempAC1 = 408;
//	public double pressTempAC2 = -72;
//	public double pressTempAC3 = -14383;
//	public double pressTempAC4 = 332741;
//	public double pressTempAC5 = 32757;
//	public double pressTempAC6 = 23153;
//	public double pressTempB1 = 6190;
//	public double pressTempB2 = 4;
//	public double pressTempMB = -32767;
//	public double pressTempMC = -8711;
//	public double pressTempMD = 2868;
//	
//    protected byte[] mPressureCalRawParams = new byte[23];
//	protected byte[] mPressureRawParams  = new byte[23];
	

	private int mPressureResolution_BMP280 = 0;
	

	public class GuiLabelConfig{
		public static final String PRESSURE_RESOLUTION_BMP280 = "Pressure Resolution";
	}
	
	public class GuiLabelSensors{
		public static final String PRESS_TEMP_BMP280 = "Pressure & Temperature";
	}
	
	// GUI Sensor Tiles
	public class GuiLabelSensorTiles{
		public static final String PRESSURE_TEMPERATURE_BMP280 = GuiLabelSensors.PRESS_TEMP_BMP280;
	}
	
	public static class DatabaseChannelHandles{
		public static final String PRESSURE_BMP280 = "BMP280_Pressure";
		public static final String TEMPERATURE_BMP280 = "BMP280_Temperature";
	}
	public static final class DatabaseConfigHandle{
		public static final String PRESSURE_PRECISION_BMP280 = "BMP280_Pressure_Precision";
	}
	
	public static class ObjectClusterSensorName{
		public static String TEMPERATURE_BMP280 = "Temperature_BMP280";
		public static String PRESSURE_BMP280 = "Pressure_BMP280";
	}
	//--------- Sensor specific variables end --------------
	
	//--------- Bluetooth commands start --------------
	public static final byte SET_BMP280_PRES_RESOLUTION_COMMAND 	= (byte) 0x52;
	public static final byte BMP280_PRES_RESOLUTION_RESPONSE 		= (byte) 0x53;
	public static final byte GET_BMP280_PRES_RESOLUTION_COMMAND 	= (byte) 0x54;
	public static final byte SET_BMP280_PRES_CALIBRATION_COMMAND	= (byte) 0x55;
	public static final byte BMP280_PRES_CALIBRATION_RESPONSE 		= (byte) 0x56;
	public static final byte GET_BMP280_PRES_CALIBRATION_COMMAND 	= (byte) 0x57;
	public static final byte BMP280_CALIBRATION_COEFFICIENTS_RESPONSE = (byte) 0x58;
	public static final byte GET_BMP280_CALIBRATION_COEFFICIENTS_COMMAND = (byte) 0x59;

	public static final Map<Byte, BtCommandDetails> mBtGetCommandMap;
	static {
		Map<Byte, BtCommandDetails> aMap = new LinkedHashMap<Byte, BtCommandDetails>();
		aMap.put(GET_BMP280_PRES_RESOLUTION_COMMAND, new BtCommandDetails(GET_BMP280_PRES_RESOLUTION_COMMAND, "GET_BMP280_PRES_RESOLUTION_COMMAND", BMP280_PRES_RESOLUTION_RESPONSE));
		aMap.put(GET_BMP280_PRES_CALIBRATION_COMMAND, new BtCommandDetails(GET_BMP280_PRES_CALIBRATION_COMMAND, "GET_BMP280_PRES_CALIBRATION_COMMAND", BMP280_PRES_CALIBRATION_RESPONSE));
		aMap.put(GET_BMP280_CALIBRATION_COEFFICIENTS_COMMAND, new BtCommandDetails(GET_BMP280_CALIBRATION_COEFFICIENTS_COMMAND, "GET_BMP280_CALIBRATION_COEFFICIENTS_COMMAND", BMP280_CALIBRATION_COEFFICIENTS_RESPONSE));
		mBtGetCommandMap = Collections.unmodifiableMap(aMap);
	}

	public static final Map<Byte, BtCommandDetails> mBtSetCommandMap;
	static {
		Map<Byte, BtCommandDetails> aMap = new LinkedHashMap<Byte, BtCommandDetails>();
		aMap.put(SET_BMP280_PRES_RESOLUTION_COMMAND, new BtCommandDetails(SET_BMP280_PRES_RESOLUTION_COMMAND, "SET_BMP280_PRES_RESOLUTION_COMMAND"));
		aMap.put(SET_BMP280_PRES_CALIBRATION_COMMAND, new BtCommandDetails(SET_BMP280_PRES_CALIBRATION_COMMAND, "SET_BMP280_PRES_CALIBRATION_COMMAND"));
		mBtSetCommandMap = Collections.unmodifiableMap(aMap);
	}
	
	//--------- Configuration options start --------------
	public static final String[] ListofPressureResolutionBMP280 = {"Low","Standard","High","Very High"};
	public static final Integer[] ListofPressureResolutionConfigValuesBMP280 = {0,1,2,3};

	public static final ConfigOptionDetailsSensor configOptionPressureResolutionBMP280 = new ConfigOptionDetailsSensor(
			ListofPressureResolutionBMP280, 
			ListofPressureResolutionConfigValuesBMP280, 
			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW);
	//--------- Configuration options end --------------

	//--------- Sensor info start --------------
	public static final SensorDetailsRef sensorBmp280 = new SensorDetailsRef(
			0x04<<(2*8), 
			0x04<<(2*8), 
			GuiLabelSensors.PRESS_TEMP_BMP280,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW,
			Arrays.asList(GuiLabelConfig.PRESSURE_RESOLUTION_BMP280),
			Arrays.asList(ObjectClusterSensorName.TEMPERATURE_BMP280,
					ObjectClusterSensorName.PRESSURE_BMP280));

	public static final Map<Integer, SensorDetailsRef> mSensorMapRef;
	static {
		Map<Integer, SensorDetailsRef> aMap = new LinkedHashMap<Integer, SensorDetailsRef>();
		aMap.put(Configuration.Shimmer3.SensorMapKey.SHIMMER_BMP280_PRESSURE, SensorBMP280.sensorBmp280);
		mSensorMapRef = Collections.unmodifiableMap(aMap);
	}
	
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
			CHANNEL_UNITS.DEGREES_CELSUIS,
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
	
   //--------- Constructors for this class end --------------


	@Override
	public void generateSensorMap() {
		super.createLocalSensorMapWithCustomParser(mSensorMapRef, mChannelMapRef);
	}

	@Override
	public void generateConfigOptionsMap() {
		mConfigOptionsMap.put(GuiLabelConfig.PRESSURE_RESOLUTION_BMP280, configOptionPressureResolutionBMP280);
	}

	@Override
	public void generateSensorGroupMapping() {
		mSensorGroupingMap = new LinkedHashMap<Integer, SensorGroupingDetails>();
		if(mShimmerVerObject.isShimmerGen3() || mShimmerVerObject.isShimmerGen4()){
			int groupIndex = Configuration.Shimmer3.GuiLabelSensorTiles.PRESSURE_TEMPERATURE_BMP280.ordinal();
			mSensorGroupingMap.put(groupIndex, new SensorGroupingDetails(
					GuiLabelSensorTiles.PRESSURE_TEMPERATURE_BMP280,
					Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_BMP280_PRESSURE),
					CompatibilityInfoForMaps.listOfCompatibleVersionInfoBMP280));
		}
		super.updateSensorGroupingMap();
	}

	@Override
	public void checkShimmerConfigBeforeConfiguring() {
		if(!isSensorEnabled(Configuration.Shimmer3.SensorMapKey.SHIMMER_BMP280_PRESSURE)) {
			setDefaultBmp280PressureSensorConfig(false);
		}
	}

	@Override
	public ObjectCluster processDataCustom(SensorDetails sensorDetails,byte[] rawData, COMMUNICATION_TYPE commType,ObjectCluster objectCluster, boolean isTimeSyncEnabled,long pcTimestamp) {
		objectCluster = sensorDetails.processDataCommon(rawData, commType, objectCluster, isTimeSyncEnabled, pcTimestamp);

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
	public void configByteArrayGenerate(ShimmerDevice shimmerDevice,byte[] mInfoMemBytes) {
		int idxConfigSetupByte3 = 9;
		int bitShiftBMP280PressureResolution = 4;
		int maskBMP280PressureResolution = 0x03;
		mInfoMemBytes[idxConfigSetupByte3] |= (byte) ((mPressureResolution_BMP280 & maskBMP280PressureResolution) << bitShiftBMP280PressureResolution);
		mPressureResolution_BMP280 = getPressureResolution();
//		System.out.println("Info Mem Pressure resolution:\t" + mPressureResolution_BMP280);
//		System.out.println("Check");
	}

	@Override
	public void configByteArrayParse(ShimmerDevice shimmerDevice,byte[] mInfoMemBytes) {
		int idxConfigSetupByte3 = 9;
		int bitShiftBMP280PressureResolution = 4;
		int maskBMP280PressureResolution = 0x03;
		setPressureResolution((mInfoMemBytes[idxConfigSetupByte3] >> bitShiftBMP280PressureResolution) & maskBMP280PressureResolution);
//		System.out.println("Pressure resolution:" + mPressureResolution);
		
	}

	@Override
	public Object setConfigValueUsingConfigLabel(Integer sensorMapKey,String configLabel, Object valueToSet) {
		Object returnValue = null;
		switch(configLabel){
			case(GuiLabelConfig.PRESSURE_RESOLUTION_BMP280):
				setPressureResolution((int)valueToSet);
				returnValue = valueToSet;
		 		break;
		}
		return returnValue;
	}

	@Override
	public Object getConfigValueUsingConfigLabel(Integer sensorMapKey,String configLabel) {
		Object returnValue = null;
		switch(configLabel){
		case(GuiLabelConfig.PRESSURE_RESOLUTION_BMP280):
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
	public boolean setDefaultConfigForSensor(int sensorMapKey, boolean isSensorEnabled) {
		if(mSensorMap.containsKey(sensorMapKey)){
			if(sensorMapKey == Configuration.Shimmer3.SensorMapKey.SHIMMER_BMP280_PRESSURE) {
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
		case(GuiLabelConfig.PRESSURE_RESOLUTION_BMP280):
			setPressureResolution((int)valueToSet);
		break;
		}
		return actionSetting;
	}
	
	@Override
	public LinkedHashMap<String, Object> getConfigMapForDb() {
		LinkedHashMap<String, Object> mapOfConfig = new LinkedHashMap<String, Object>();
		mapOfConfig.put(DatabaseConfigHandle.PRESSURE_PRECISION_BMP280, getPressureResolution());
		return mapOfConfig;
	}
	
	@Override
	public void parseConfigMapFromDb(LinkedHashMap<String, Object> mapOfConfigPerShimmer) {
		if(mapOfConfigPerShimmer.containsKey(DatabaseConfigHandle.PRESSURE_PRECISION_BMP280)){
			setPressureResolution(((Double) mapOfConfigPerShimmer.get(DatabaseConfigHandle.PRESSURE_PRECISION_BMP280)).intValue());
		}
	}


	@Override
	public boolean processResponse(int responseCommand, Object parsedResponse, COMMUNICATION_TYPE commType) {
		// TODO Auto-generated method stub
		return false;
	}
	
	
	
	
	//--------- Abstract methods implemented end --------------


	//--------- Sensor specific methods start --------------	
	
	private void setDefaultBmp280PressureSensorConfig(boolean isSensorEnabled) {

		if(isSensorEnabled) {
		}
		else{
			mPressureResolution_BMP280 = 0;
		}
	}
	
	
	private void setPressureResolution(int i){
		if(ArrayUtils.contains(ListofPressureResolutionConfigValuesBMP280, i)){
//			System.err.println("New resolution:\t" + ListofPressureResolution[i]);
			mPressureResolution_BMP280 = i;
		}
	}
	
	private int getPressureResolution() {
		return mPressureResolution_BMP280;
	}


	
}
