package com.shimmerresearch.sensors;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;

import com.shimmerresearch.bluetooth.BtCommandDetails;
import com.shimmerresearch.comms.radioProtocol.ShimmerLiteProtocolInstructionSet.LiteProtocolInstructionSet;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.Configuration.CHANNEL_UNITS;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driver.Configuration.Shimmer3.CompatibilityInfoForMaps;
import com.shimmerresearch.driver.calibration.CalibDetailsBmp180;
import com.shimmerresearch.driver.calibration.CalibDetailsKinematic;
import com.shimmerresearch.driver.calibration.CalibDetails.CALIB_READ_SOURCE;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.FormatCluster;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driverUtilities.SensorDetailsRef;
import com.shimmerresearch.driverUtilities.ChannelDetails;
import com.shimmerresearch.driverUtilities.ConfigOptionDetailsSensor;
import com.shimmerresearch.driverUtilities.SensorDetails;
import com.shimmerresearch.driverUtilities.SensorGroupingDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;
import com.shimmerresearch.driverUtilities.UtilParseData;
import com.shimmerresearch.driverUtilities.UtilShimmer;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_ENDIAN;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_TYPE;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_SOURCE;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_TYPE;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.HW_ID;

//TODO add calibdetails from ShimmerObject (updateCurrentPressureCalibInUse() + generateCalibMap() + isSensorUsingDefaultCal(), refer to SensorKionix)

/**
 * @author Ronan McCormack
 * @author Mark Nolan
 *
 */
public class SensorBMP180 extends AbstractSensor {
	
	/** * */
	private static final long serialVersionUID = 4559709230029277863L;
	
	private CalibDetailsBmp180 mCalibDetailsBmp180 = new CalibDetailsBmp180();
	
	private byte[] mPressureCalRawParams = new byte[23];
	private byte[] mPressureRawParams  = new byte[23];
	
//	public static final int SHIMMER_BMP180_PRESSURE = 22;
	private int mPressureResolution = 0;
	

	public class GuiLabelConfig{
		public static final String PRESSURE_RESOLUTION = "Pressure Resolution";
	}
	
	public class GuiLabelSensors{
		public static final String PRESS_TEMP_BMP180 = "Pressure & Temperature";
	}
	
	// GUI Sensor Tiles
	public class GuiLabelSensorTiles{
		public static final String PRESSURE_TEMPERATURE = GuiLabelSensors.PRESS_TEMP_BMP180;
	}
	
	public static class DatabaseChannelHandles{
		public static final String PRESSURE = "BMP180_Pressure";
		public static final String TEMPERATURE = "BMP180_Temperature";
	}
	public static final class DatabaseConfigHandle{
		public static final String PRESSURE_PRECISION = "BMP180_Pressure_Precision";
		
		public static final String TEMP_PRES_AC1 = "BMP180_AC1";
		public static final String TEMP_PRES_AC2 = "BMP180_AC2";
		public static final String TEMP_PRES_AC3 = "BMP180_AC3";
		public static final String TEMP_PRES_AC4 = "BMP180_AC4";
		public static final String TEMP_PRES_AC5 = "BMP180_AC5";
		public static final String TEMP_PRES_AC6 = "BMP180_AC6";
		public static final String TEMP_PRES_B1 = "BMP180_B1";
		public static final String TEMP_PRES_B2 = "BMP180_B2";
		public static final String TEMP_PRES_MB = "BMP180_MB";
		public static final String TEMP_PRES_MC = "BMP180_MC";
		public static final String TEMP_PRES_MD = "BMP180_MD";
		
	}
	
	public static class ObjectClusterSensorName{
		public static String TEMPERATURE_BMP180 = "Temperature_BMP180";
		public static String PRESSURE_BMP180 = "Pressure_BMP180";
	}
	//--------- Sensor specific variables end --------------

	//--------- Bluetooth commands start --------------
	public static final byte SET_BMP180_PRES_RESOLUTION_COMMAND 	= (byte) 0x52;
	public static final byte BMP180_PRES_RESOLUTION_RESPONSE 		= (byte) 0x53;
	public static final byte GET_BMP180_PRES_RESOLUTION_COMMAND 	= (byte) 0x54;
	public static final byte SET_BMP180_PRES_CALIBRATION_COMMAND	= (byte) 0x55;
	public static final byte BMP180_PRES_CALIBRATION_RESPONSE 		= (byte) 0x56;
	public static final byte GET_BMP180_PRES_CALIBRATION_COMMAND 	= (byte) 0x57;
	public static final byte BMP180_CALIBRATION_COEFFICIENTS_RESPONSE = (byte) 0x58;
	public static final byte GET_BMP180_CALIBRATION_COEFFICIENTS_COMMAND = (byte) 0x59;
	
    public static final Map<Byte, BtCommandDetails> mBtGetCommandMap;
    static {
        Map<Byte, BtCommandDetails> aMap = new LinkedHashMap<Byte, BtCommandDetails>();
        aMap.put(GET_BMP180_PRES_RESOLUTION_COMMAND, new BtCommandDetails(GET_BMP180_PRES_RESOLUTION_COMMAND, "GET_BMP180_PRES_RESOLUTION_COMMAND", BMP180_PRES_RESOLUTION_RESPONSE));
        aMap.put(GET_BMP180_PRES_CALIBRATION_COMMAND, new BtCommandDetails(GET_BMP180_PRES_CALIBRATION_COMMAND, "GET_BMP180_PRES_CALIBRATION_COMMAND", BMP180_PRES_CALIBRATION_RESPONSE));
        aMap.put(GET_BMP180_CALIBRATION_COEFFICIENTS_COMMAND, new BtCommandDetails(GET_BMP180_CALIBRATION_COEFFICIENTS_COMMAND, "GET_BMP180_CALIBRATION_COEFFICIENTS_COMMAND", BMP180_CALIBRATION_COEFFICIENTS_RESPONSE));
        mBtGetCommandMap = Collections.unmodifiableMap(aMap);
    }

    public static final Map<Byte, BtCommandDetails> mBtSetCommandMap;
    static {
        Map<Byte, BtCommandDetails> aMap = new LinkedHashMap<Byte, BtCommandDetails>();
        aMap.put(SET_BMP180_PRES_RESOLUTION_COMMAND, new BtCommandDetails(SET_BMP180_PRES_RESOLUTION_COMMAND, "SET_BMP180_PRES_RESOLUTION_COMMAND"));
        aMap.put(SET_BMP180_PRES_CALIBRATION_COMMAND, new BtCommandDetails(SET_BMP180_PRES_CALIBRATION_COMMAND, "SET_BMP180_PRES_CALIBRATION_COMMAND"));
        mBtSetCommandMap = Collections.unmodifiableMap(aMap);
    }
	//--------- Bluetooth commands end --------------
	
	//--------- Configuration options start --------------
	public static final String[] ListofPressureResolution = {"Low","Standard","High","Very High"};
	public static final Integer[] ListofPressureResolutionConfigValues = {0,1,2,3};

	public static final ConfigOptionDetailsSensor configOptionPressureResolution = new ConfigOptionDetailsSensor(
			ListofPressureResolution, 
			ListofPressureResolutionConfigValues, 
			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX,
//			CompatibilityInfoForMaps.listOfCompatibleVersionInfoBMP180
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW);
	//--------- Configuration options end --------------

	//--------- Sensor info start --------------
	public static final SensorDetailsRef sensorBmp180 = new SensorDetailsRef(
			0x04<<(2*8), 
			0x04<<(2*8), 
			GuiLabelSensors.PRESS_TEMP_BMP180,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW,
			Arrays.asList(GuiLabelConfig.PRESSURE_RESOLUTION),
			Arrays.asList(ObjectClusterSensorName.TEMPERATURE_BMP180,
					ObjectClusterSensorName.PRESSURE_BMP180));
	
    public static final Map<Integer, SensorDetailsRef> mSensorMapRef;
    static {
        Map<Integer, SensorDetailsRef> aMap = new LinkedHashMap<Integer, SensorDetailsRef>();
        aMap.put(Configuration.Shimmer3.SensorMapKey.SHIMMER_BMP180_PRESSURE, SensorBMP180.sensorBmp180);
		mSensorMapRef = Collections.unmodifiableMap(aMap);
    }
	//--------- Sensor info end --------------
    
	//--------- Channel info start --------------
	public static final ChannelDetails channelBmp180Press = new ChannelDetails(
			ObjectClusterSensorName.PRESSURE_BMP180,
			ObjectClusterSensorName.PRESSURE_BMP180,
			DatabaseChannelHandles.PRESSURE,
			CHANNEL_DATA_TYPE.UINT24, 3, CHANNEL_DATA_ENDIAN.MSB,
			CHANNEL_UNITS.KPASCAL,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL),
			0x1B);
	{
		//TODO put into above constructor
//		channelBmp180Press.mChannelSource = CHANNEL_SOURCE.SHIMMER;
//		channelBmp180Press.mDefaultUncalUnit = CHANNEL_UNITS.KPASCAL;
//		channelBmp180Press.mChannelFormatDerivedFromShimmerDataPacket = CHANNEL_TYPE.UNCAL;
	}
	
	public static final ChannelDetails channelBmp180Temp = new ChannelDetails(
			ObjectClusterSensorName.TEMPERATURE_BMP180,
			ObjectClusterSensorName.TEMPERATURE_BMP180,
			DatabaseChannelHandles.TEMPERATURE,
			CHANNEL_DATA_TYPE.UINT16, 2, CHANNEL_DATA_ENDIAN.MSB,
			CHANNEL_UNITS.DEGREES_CELSUIS,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL),
			0x1A);
	{
		//TODO put into above constructor
//		channelBmp180Temp.mChannelSource = CHANNEL_SOURCE.SHIMMER;
//		channelBmp180Temp.mDefaultUncalUnit = CHANNEL_UNITS.DEGREES_CELSUIS;
//		channelBmp180Temp.mChannelFormatDerivedFromShimmerDataPacket = CHANNEL_TYPE.UNCAL;
	}

	public static final Map<String, ChannelDetails> mChannelMapRef;
    static {
        Map<String, ChannelDetails> aMap = new LinkedHashMap<String, ChannelDetails>();
        aMap.put(ObjectClusterSensorName.PRESSURE_BMP180, SensorBMP180.channelBmp180Press);
        aMap.put(ObjectClusterSensorName.TEMPERATURE_BMP180, SensorBMP180.channelBmp180Temp);
		mChannelMapRef = Collections.unmodifiableMap(aMap);
    }
	//--------- Channel info end --------------
    
	//--------- Constructors for this class start --------------
	/** Constructor for this Sensor
	 * @param svo
	 */
	public SensorBMP180(ShimmerVerObject svo) {
		super(SENSORS.BMP180, svo);
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
		mConfigOptionsMap.put(GuiLabelConfig.PRESSURE_RESOLUTION, configOptionPressureResolution); 
	}
	
	
	@Override
	public void generateSensorGroupMapping() {
		mSensorGroupingMap = new LinkedHashMap<Integer, SensorGroupingDetails>();
		if(mShimmerVerObject.isShimmerGen3() || mShimmerVerObject.isShimmerGen4()){
			int groupIndex = Configuration.Shimmer3.GuiLabelSensorTiles.PRESSURE_TEMPERATURE.ordinal();
			mSensorGroupingMap.put(groupIndex, new SensorGroupingDetails(
					GuiLabelSensorTiles.PRESSURE_TEMPERATURE,
					Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_BMP180_PRESSURE),
					CompatibilityInfoForMaps.listOfCompatibleVersionInfoBMP180));
		}
		super.updateSensorGroupingMap();
	}
	
	
	@Override
	public ObjectCluster processDataCustom(SensorDetails sensorDetails, byte[] sensorByteArray, COMMUNICATION_TYPE commType, ObjectCluster objectCluster, boolean isTimeSyncEnabled, long pcTimestamp) {
		
		double rawDataUP = 0;
		double rawDataUT = 0;
		int index = 0;
		for (ChannelDetails channelDetails:sensorDetails.mListOfChannels){
			//first process the data originating from the Shimmer sensor
			byte[] channelByteArray = new byte[channelDetails.mDefaultNumBytes];
			System.arraycopy(sensorByteArray, index, channelByteArray, 0, channelDetails.mDefaultNumBytes);
			
//			System.out.println("BMP180 bytes\t" + channelDetails.mObjectClusterName + "\t" + UtilShimmer.bytesToHexStringWithSpacesFormatted(channelByteArray));

			objectCluster = SensorDetails.processShimmerChannelData(channelByteArray, channelDetails, objectCluster);
			objectCluster.incrementIndexKeeper();
			index = index + channelDetails.mDefaultNumBytes;

			if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.PRESSURE_BMP180)){
				rawDataUP = ((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(ObjectClusterSensorName.PRESSURE_BMP180), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
				rawDataUP = rawDataUP/Math.pow(2,8-mPressureResolution);
			}
			if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.TEMPERATURE_BMP180)){
				rawDataUT = ((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(ObjectClusterSensorName.TEMPERATURE_BMP180), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
			}
		}

		//Calibration
		double[] bmp180caldata = calibratePressureSensorData(rawDataUP, rawDataUT, mCalibDetailsBmp180);
		bmp180caldata[0] = bmp180caldata[0]/1000;
		
		for (ChannelDetails channelDetails:sensorDetails.mListOfChannels){
			if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.PRESSURE_BMP180)){
				objectCluster.addCalData(channelDetails, bmp180caldata[0], objectCluster.getIndexKeeper()-2);
				objectCluster.incrementIndexKeeper();
			}
			else if(channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.TEMPERATURE_BMP180)){
				objectCluster.addCalData(channelDetails, bmp180caldata[1], objectCluster.getIndexKeeper()-1);
				objectCluster.incrementIndexKeeper();
			}
		}
		//Debugging
//		super.consolePrintChannelsCal(objectCluster, Arrays.asList(
//				new String[]{ObjectClusterSensorName.PRESSURE_BMP180, CHANNEL_TYPE.UNCAL.toString()}, 
//				new String[]{ObjectClusterSensorName.TEMPERATURE_BMP180, CHANNEL_TYPE.UNCAL.toString()}, 
//				new String[]{ObjectClusterSensorName.PRESSURE_BMP180, CHANNEL_TYPE.CAL.toString()}, 
//				new String[]{ObjectClusterSensorName.TEMPERATURE_BMP180, CHANNEL_TYPE.CAL.toString()}));
		
		return objectCluster;
	}

	@Override
	public void infoMemByteArrayGenerate(ShimmerDevice shimmerDevice, byte[] mInfoMemBytes) {
		int idxConfigSetupByte3 = 9;
		int bitShiftBMP180PressureResolution = 4;
		int maskBMP180PressureResolution = 0x03;
		mInfoMemBytes[idxConfigSetupByte3] |= (byte) ((mPressureResolution & maskBMP180PressureResolution) << bitShiftBMP180PressureResolution);
	}

	@Override
	public void infoMemByteArrayParse(ShimmerDevice shimmerDevice, byte[] mInfoMemBytes) {
		int idxConfigSetupByte3 = 9;
		int bitShiftBMP180PressureResolution = 4;
		int maskBMP180PressureResolution = 0x03;
		setPressureResolution((mInfoMemBytes[idxConfigSetupByte3] >> bitShiftBMP180PressureResolution) & maskBMP180PressureResolution);
		mPressureResolution = getPressureResolution();
//		System.out.println("Info Mem Pressure resolution:\t" + mPressureResolution);
//		System.out.println("Check");
	}

	@Override
	public Object setConfigValueUsingConfigLabel(Integer sensorMapKey, String configLabel, Object valueToSet) {
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
	public Object getConfigValueUsingConfigLabel(Integer sensorMapKey, String configLabel){
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
	public boolean setDefaultConfigForSensor(int sensorMapKey, boolean isSensorEnabled) {
		if(mSensorMap.containsKey(sensorMapKey)){
			if(sensorMapKey == Configuration.Shimmer3.SensorMapKey.SHIMMER_BMP180_PRESSURE) {
				setDefaultBmp180PressureSensorConfig(isSensorEnabled);
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
	public LinkedHashMap<String, Object> getConfigMapForDb() {
		LinkedHashMap<String, Object> mapOfConfig = new LinkedHashMap<String, Object>();
		mapOfConfig.put(DatabaseConfigHandle.PRESSURE_PRECISION, getPressureResolution());


		mapOfConfig.put(DatabaseConfigHandle.TEMP_PRES_AC1, getPressTempAC1());
		mapOfConfig.put(DatabaseConfigHandle.TEMP_PRES_AC2, getPressTempAC2());
		mapOfConfig.put(DatabaseConfigHandle.TEMP_PRES_AC3, getPressTempAC3());
		mapOfConfig.put(DatabaseConfigHandle.TEMP_PRES_AC4, getPressTempAC4());
		mapOfConfig.put(DatabaseConfigHandle.TEMP_PRES_AC5, getPressTempAC5());
		mapOfConfig.put(DatabaseConfigHandle.TEMP_PRES_AC6, getPressTempAC6());
		mapOfConfig.put(DatabaseConfigHandle.TEMP_PRES_B1, getPressTempB1());
		mapOfConfig.put(DatabaseConfigHandle.TEMP_PRES_B2, getPressTempB2());
		mapOfConfig.put(DatabaseConfigHandle.TEMP_PRES_MB, getPressTempMB());
		mapOfConfig.put(DatabaseConfigHandle.TEMP_PRES_MC, getPressTempMC());
		mapOfConfig.put(DatabaseConfigHandle.TEMP_PRES_MD, getPressTempMD());

		return mapOfConfig;
	}

	@Override
	public void parseConfigMapFromDb(LinkedHashMap<String, Object> mapOfConfigPerShimmer) {
		if(mapOfConfigPerShimmer.containsKey(DatabaseConfigHandle.PRESSURE_PRECISION)){
			setPressureResolution(((Double) mapOfConfigPerShimmer.get(DatabaseConfigHandle.PRESSURE_PRECISION)).intValue());
		}
		//PRESSURE (BMP180) CAL PARAMS
		if(mapOfConfigPerShimmer.containsKey(DatabaseConfigHandle.TEMP_PRES_AC1)
				&& mapOfConfigPerShimmer.containsKey(DatabaseConfigHandle.TEMP_PRES_AC2)
				&& mapOfConfigPerShimmer.containsKey(DatabaseConfigHandle.TEMP_PRES_AC3)
				&& mapOfConfigPerShimmer.containsKey(DatabaseConfigHandle.TEMP_PRES_AC4)
				&& mapOfConfigPerShimmer.containsKey(DatabaseConfigHandle.TEMP_PRES_AC5)
				&& mapOfConfigPerShimmer.containsKey(DatabaseConfigHandle.TEMP_PRES_AC6)
				&& mapOfConfigPerShimmer.containsKey(DatabaseConfigHandle.TEMP_PRES_B1)
				&& mapOfConfigPerShimmer.containsKey(DatabaseConfigHandle.TEMP_PRES_B2)
				&& mapOfConfigPerShimmer.containsKey(DatabaseConfigHandle.TEMP_PRES_MB)
				&& mapOfConfigPerShimmer.containsKey(DatabaseConfigHandle.TEMP_PRES_MC)
				&& mapOfConfigPerShimmer.containsKey(DatabaseConfigHandle.TEMP_PRES_MD)){
			
			setPressureCalib(
					(Double) mapOfConfigPerShimmer.get(DatabaseConfigHandle.TEMP_PRES_AC1),
					(Double) mapOfConfigPerShimmer.get(DatabaseConfigHandle.TEMP_PRES_AC2),
					(Double) mapOfConfigPerShimmer.get(DatabaseConfigHandle.TEMP_PRES_AC3),
					(Double) mapOfConfigPerShimmer.get(DatabaseConfigHandle.TEMP_PRES_AC4),
					(Double) mapOfConfigPerShimmer.get(DatabaseConfigHandle.TEMP_PRES_AC5),
					(Double) mapOfConfigPerShimmer.get(DatabaseConfigHandle.TEMP_PRES_AC6),
					(Double) mapOfConfigPerShimmer.get(DatabaseConfigHandle.TEMP_PRES_B1),
					(Double) mapOfConfigPerShimmer.get(DatabaseConfigHandle.TEMP_PRES_B2),
					(Double) mapOfConfigPerShimmer.get(DatabaseConfigHandle.TEMP_PRES_MB),
					(Double) mapOfConfigPerShimmer.get(DatabaseConfigHandle.TEMP_PRES_MC),
					(Double) mapOfConfigPerShimmer.get(DatabaseConfigHandle.TEMP_PRES_MD));
		}
	}

	@Override
	public void processResponse(Object obj, COMMUNICATION_TYPE commType) {
		// TODO Auto-generated method stub
		if (commType==COMMUNICATION_TYPE.BLUETOOTH){
			byte[] responseBytes = (byte[])obj;
			if(responseBytes[0]!=LiteProtocolInstructionSet.InstructionsGet.GET_BMP180_CALIBRATION_COEFFICIENTS_COMMAND_VALUE){
				byte[] pressureResoRes = new byte[22]; 
				System.arraycopy(responseBytes, 1, pressureResoRes, 0, 22);
				retrievePressureCalibrationParametersFromPacket(pressureResoRes, CALIB_READ_SOURCE.LEGACY_BT_COMMAND);
			}
		}
	}
	

	@Override
	public void checkShimmerConfigBeforeConfiguring() {
//		// If Shimmer name is default, update with MAC ID if available.
//		if(mShimmerUserAssignedName.equals(DEFAULT_SHIMMER_NAME)){
//			setDefaultShimmerName();
//		}	
		if(!isSensorEnabled(Configuration.Shimmer3.SensorMapKey.SHIMMER_BMP180_PRESSURE)) {
			setDefaultBmp180PressureSensorConfig(false);
		}
	}
	
//	@Override
//	public List<String> generateListOfConfigOptionKeysAssociated(ShimmerVerObject svo) {
//		return mListOfConfigOptionKeysAssociated = Arrays.asList(
//				Configuration.Shimmer3.GuiLabelConfig.PRESSURE_RESOLUTION);
//	}


	//--------- Abstract methods implemented end --------------


	//--------- Sensor specific methods start --------------
	public static double[] calibratePressureSensorData(double UP, double UT, CalibDetailsBmp180 calibDetailsBmp180){
		//Calculate the true temperature
		double X1 = (UT - calibDetailsBmp180.AC6) * (calibDetailsBmp180.AC5 / 32768);
		double X2 = (calibDetailsBmp180.MC * 2048 / (X1 + calibDetailsBmp180.MD));
		double B5 = X1 + X2;
		double T = (B5 + 8) / 16;
		
		double B6 = B5 - 4000;
		X1 = (calibDetailsBmp180.B2 * (Math.pow(B6,2)/ 4096)) / 2048;
		X2 = calibDetailsBmp180.AC2 * B6 / 2048;
		double X3 = X1 + X2;
		double B3 = (((calibDetailsBmp180.AC1 * 4 + X3)*(1<<calibDetailsBmp180.mRangeValue) + 2)) / 4;
		X1 = calibDetailsBmp180.AC3 * B6 / 8192;
		X2 = (calibDetailsBmp180.B1 * (Math.pow(B6,2)/ 4096)) / 65536;
		X3 = ((X1 + X2) + 2) / 4;
		double B4 = calibDetailsBmp180.AC4 * (X3 + 32768) / 32768;
		double B7 = (UP - B3) * (50000>>calibDetailsBmp180.mRangeValue);
		double p=0;
		if (B7 < 2147483648L ){ //0x80000000
			p = (B7 * 2) / B4;
		}
		else{
			p = (B7 / B4) * 2;
		}
		X1 = ((p / 256.0) * (p / 256.0) * 3038) / 65536;
		X2 = (-7357 * p) / 65536;
		p = p +( (X1 + X2 + 3791) / 16);

		double[] caldata = new double[2];
		caldata[0]=p;
		caldata[1]=T/10;
		return caldata;
	}
	
	public void retrievePressureCalibrationParametersFromPacket(byte[] pressureResoRes, CALIB_READ_SOURCE calibReadSource) {
		mCalibDetailsBmp180.parseCalParamByteArray(pressureResoRes, calibReadSource);
		mCalibDetailsBmp180.mRangeValue = getPressureResolution();
	}
	

	public byte[] getRawCalibrationParameters(ShimmerVerObject svo){        
		byte[] rawcal=new byte[1];
		if(mShimmerVerObject.isShimmerGen3() || mShimmerVerObject.isShimmerGen4()){
			// Mag + Pressure
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
			try {
				outputStream.write(5); // write the number of different calibration parameters

				outputStream.write( mPressureCalRawParams.length);
				outputStream.write( mPressureCalRawParams );
				rawcal = outputStream.toByteArray( );
			} catch (IOException e) {
				e.printStackTrace();
			}			

		} 
		else {
			rawcal[0]=0;
		}
		return rawcal;
	}
	
	private void setPressureResolution(int i){
		if(ArrayUtils.contains(SensorBMP180.ListofPressureResolutionConfigValues, i)){
//			System.out.println("New resolution:\t" + ListofPressureResolution[i]);
			mPressureResolution = i;
		}
		updateCurrentPressureCalibInUse();
	}
	
	private int getPressureResolution(){
		return mPressureResolution;
	}
	
	public void updateCurrentPressureCalibInUse(){
		mCalibDetailsBmp180.mRangeValue = getPressureResolution();
	}

	private void setDefaultBmp180PressureSensorConfig(boolean isSensorEnabled) {
		//RS (30/5/2016) - from ShimmerObject:
		if(isSensorEnabled) {
		}
		else{
			mPressureResolution = 0;
		}
	}
	
	public void setPressureCalib(
			double AC1, double AC2, double AC3, 
			double AC4, double AC5, double AC6,
			double B1, double B2, 
			double MB, double MC, double MD){
		mCalibDetailsBmp180.setPressureCalib(AC1, AC2, AC3, AC4, AC5, AC6, B1, B2, MB, MC, MD);
	}
	public double getPressTempAC1(){
		return mCalibDetailsBmp180.AC1;
	}
	
	public double getPressTempAC2(){
		return mCalibDetailsBmp180.AC2;
	}
	
	public double getPressTempAC3(){
		return mCalibDetailsBmp180.AC3;
	}
	
	public double getPressTempAC4(){
		return mCalibDetailsBmp180.AC4;
	}
	
	public double getPressTempAC5(){
		return mCalibDetailsBmp180.AC5;
	}
	
	public double getPressTempAC6(){
		return mCalibDetailsBmp180.AC6;
	}
	
	public double getPressTempB1(){
		return mCalibDetailsBmp180.B1;
	}
	
	public double getPressTempB2(){
		return mCalibDetailsBmp180.B2;
	}
	
	public double getPressTempMB(){
		return mCalibDetailsBmp180.MB;
	}
	
	public double getPressTempMC(){
		return mCalibDetailsBmp180.MC;
	}
	
	public double getPressTempMD(){
		return mCalibDetailsBmp180.MD;
	}
	
	

	
	//--------- Sensor specific methods end --------------


}