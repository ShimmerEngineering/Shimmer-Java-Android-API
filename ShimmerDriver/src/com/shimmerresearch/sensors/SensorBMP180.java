package com.shimmerresearch.sensors;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.shimmerresearch.bluetooth.BtCommandDetails;
import com.shimmerresearch.driver.Configuration.CHANNEL_UNITS;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driver.Configuration.Shimmer3.CompatibilityInfoForMaps;
import com.shimmerresearch.driver.FormatCluster;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driver.UtilShimmer;
import com.shimmerresearch.driverUtilities.SensorDetailsRef;
import com.shimmerresearch.driverUtilities.ChannelDetails;
import com.shimmerresearch.driverUtilities.SensorConfigOptionDetails;
import com.shimmerresearch.driverUtilities.SensorDetails;
import com.shimmerresearch.driverUtilities.SensorGroupingDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_ENDIAN;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_TYPE;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_SOURCE;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_TYPE;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.HW_ID;

/**
 * @author Ronan McCormack
 * @author Mark Nolan
 *
 */
public class SensorBMP180 extends AbstractSensor {
	
	/** * */
	private static final long serialVersionUID = 4559709230029277863L;
	
	//--------- Sensor specific variables start --------------
	public double pressTempAC1 = 408;
	public double pressTempAC2 = -72;
	public double pressTempAC3 = -14383;
	public double pressTempAC4 = 332741;
	public double pressTempAC5 = 32757;
	public double pressTempAC6 = 23153;
	public double pressTempB1 = 6190;
	public double pressTempB2 = 4;
	public double pressTempMB = -32767;
	public double pressTempMC = -8711;
	public double pressTempMD = 2868;
	
	protected byte[] mPressureCalRawParams = new byte[23];
	protected byte[] mPressureRawParams  = new byte[23];
	public static final String PRESSURE_TEMPERATURE = "Pressure & Temperature";
	public static final int SHIMMER_BMP180_PRESSURE = 22;
	public int mPressureResolution = 0;
	
	public static class ObjectClusterSensorName{
		public static String TEMPERATURE_BMP180 = "Temperature_BMP180";
		public static String PRESSURE_BMP180 = "Pressure_BMP180";
	}
	public class GuiLabelConfig{
		public static final String PRESSURE_RESOLUTION = "Pressure Resolution";
	}
	public class GuiLabelSensors{
		public static final String PRESS_TEMP_BMP180 = "Pressure & Temperature";
	}
	public static class DatabaseChannelHandles{
		public static final String PRESSURE = "BMP180_Pressure";
		public static final String TEMPERATURE = "BMP180_Temperature";
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

	public static final SensorConfigOptionDetails configOptionPressureResolution = new SensorConfigOptionDetails(
			ListofPressureResolution, 
			ListofPressureResolutionConfigValues, 
			SensorConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX,
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
			Arrays.asList(ObjectClusterSensorName.PRESSURE_BMP180,
					ObjectClusterSensorName.TEMPERATURE_BMP180));
	
    public static final Map<Integer, SensorDetailsRef> mSensorMapRef;
    static {
        Map<Integer, SensorDetailsRef> aMap = new LinkedHashMap<Integer, SensorDetailsRef>();
        aMap.put(SensorBMP180.SHIMMER_BMP180_PRESSURE, SensorBMP180.sensorBmp180);
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
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
	{
		//TODO put into above constructor
		channelBmp180Press.mChannelSource = CHANNEL_SOURCE.SHIMMER;
		channelBmp180Press.mDefaultUnit = CHANNEL_UNITS.KPASCAL;
		channelBmp180Press.mChannelFormatDerivedFromShimmerDataPacket = CHANNEL_TYPE.UNCAL;
	}
	
	public static final ChannelDetails channelBmp180Temp = new ChannelDetails(
			ObjectClusterSensorName.TEMPERATURE_BMP180,
			ObjectClusterSensorName.TEMPERATURE_BMP180,
			DatabaseChannelHandles.TEMPERATURE,
			CHANNEL_DATA_TYPE.UINT16, 2, CHANNEL_DATA_ENDIAN.MSB,
			CHANNEL_UNITS.DEGREES_CELSUIS,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
	{
		//TODO put into above constructor
		channelBmp180Temp.mChannelSource = CHANNEL_SOURCE.SHIMMER;
		channelBmp180Temp.mDefaultUnit = CHANNEL_UNITS.DEGREES_CELSUIS;
		channelBmp180Temp.mChannelFormatDerivedFromShimmerDataPacket = CHANNEL_TYPE.UNCAL;
	}

	public static final Map<String, ChannelDetails> mChannelMapRef;
    static {
        Map<String, ChannelDetails> aMap = new LinkedHashMap<String, ChannelDetails>();
        aMap.put(ObjectClusterSensorName.PRESSURE_BMP180, SensorBMP180.channelBmp180Press);
        aMap.put(ObjectClusterSensorName.TEMPERATURE_BMP180, SensorBMP180.channelBmp180Temp);
		mChannelMapRef = Collections.unmodifiableMap(aMap);
    }
	//--------- Channel info end --------------
    
	
	/** Constructor for this Sensor
	 * @param svo
	 */
	public SensorBMP180(ShimmerVerObject svo) {
		super(svo);
		setSensorName(SENSORS.BMP180.toString());
	}
	
	@Override
	public void generateSensorMap(ShimmerVerObject svo) {
		super.createLocalSensorMapWithCustomParser(mSensorMapRef, mChannelMapRef);
	}

	//--------- Abstract methods implemented start --------------
	@Override
	public ObjectCluster processDataCustom(SensorDetails sensorDetails, byte[] rawData, COMMUNICATION_TYPE commType, ObjectCluster objectCluster, boolean isTimeSyncEnabled, long pcTimestamp) {
		
		System.out.print("BMP180 bytes\t" + UtilShimmer.bytesToHexStringWithSpacesFormatted(rawData));
		
		double rawDataUP = 0;
		double rawDataUT = 0;
		int index = 0;
		for (ChannelDetails channelDetails:sensorDetails.mListOfChannels){
			//first process the data originating from the Shimmer sensor
			byte[] channelByteArray = new byte[channelDetails.mDefaultNumBytes];
			System.arraycopy(rawData, index, channelByteArray, 0, channelDetails.mDefaultNumBytes);
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

		double[] bmp180caldata= calibratePressureSensorData(rawDataUP,rawDataUT);

		for (ChannelDetails channelDetails:sensorDetails.mListOfChannels){
			if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.PRESSURE_BMP180)){
				objectCluster.addCalData(channelDetails, bmp180caldata[0], objectCluster.getIndexKeeper()-2);
			}
			else if(channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.TEMPERATURE_BMP180)){
				objectCluster.addCalData(channelDetails, bmp180caldata[1], objectCluster.getIndexKeeper()-1);
			}
		}
		
		//Debugging
		super.consolePrintChannelsCal(objectCluster, Arrays.asList(
				new String[]{ObjectClusterSensorName.PRESSURE_BMP180, CHANNEL_TYPE.UNCAL.toString()}, 
				new String[]{ObjectClusterSensorName.TEMPERATURE_BMP180, CHANNEL_TYPE.UNCAL.toString()}, 
				new String[]{ObjectClusterSensorName.PRESSURE_BMP180, CHANNEL_TYPE.CAL.toString()}, 
				new String[]{ObjectClusterSensorName.TEMPERATURE_BMP180, CHANNEL_TYPE.CAL.toString()}));
		
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
		mPressureResolution = (mInfoMemBytes[idxConfigSetupByte3] >> bitShiftBMP180PressureResolution) & maskBMP180PressureResolution;
	}

	@Override
	public Object setConfigValueUsingConfigLabel(String componentName, Object valueToSet) {
		Object returnValue = null;
		switch(componentName){
			case(GuiLabelConfig.PRESSURE_RESOLUTION):
				setPressureResolution((int)valueToSet);
				returnValue = valueToSet;
		 		break;
		}
		return returnValue;
	}
	
	@Override
	public Object getConfigValueUsingConfigLabel(String componentName){
		Object returnValue = null;
		
		switch(componentName){
		case(GuiLabelConfig.PRESSURE_RESOLUTION):
			returnValue = getPressureResolution();
	 		break;
		  }
		return returnValue;
	}

	@Override
	public void setSensorSamplingRate() {
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
	public ActionSetting setSettings(String componentName, Object valueToSet,COMMUNICATION_TYPE commType) {
		
		ActionSetting actionSetting = new ActionSetting(commType);
		switch(componentName){
			case(GuiLabelConfig.PRESSURE_RESOLUTION):
				setPressureResolution((int)valueToSet);
		 		break;
		}
		return actionSetting;
	}

//	@Override
//	public LinkedHashMap<Integer, ChannelDetails> generateChannelDetailsMap(ShimmerVerObject svo) {
//		LinkedHashMap<Integer, ChannelDetails> mapOfChannelDetails = new LinkedHashMap<Integer, ChannelDetails>();
//		int index = 0;
//		mapOfChannelDetails.put(index++, cdBmp180Temp);
//		mapOfChannelDetails.put(index++, cdBmp180Press);					  
//
////		mMapOfCommTypetoChannel.put(COMMUNICATION_TYPE.SD, mapOfChannelDetails);
////		mMapOfCommTypetoChannel.put(COMMUNICATION_TYPE.BLUETOOTH, mapOfChannelDetails);
//		mMapOfChannelDetails = mapOfChannelDetails;
//		
//		return mMapOfChannelDetails;
//	}

	@Override
	public void generateConfigOptionsMap(ShimmerVerObject svo) {
		mConfigOptionsMap.put(GuiLabelConfig.PRESSURE_RESOLUTION, configOptionPressureResolution); 
	}

//	@Override
//	public List<Integer> generateListOfSensorMapKeysConflicting(ShimmerVerObject svo) {
//		
//		return null;
//	}
//
//	@Override
//	public List<String> generateListOfConfigOptionKeysAssociated(ShimmerVerObject svo) {
//		return mListOfConfigOptionKeysAssociated = Arrays.asList(
//				Configuration.Shimmer3.GuiLabelConfig.PRESSURE_RESOLUTION);
//	}


//	@Override
//	public void generateSensorGroupMapping(ShimmerVerObject svo) {
//		mSensorGroupingMap = new LinkedHashMap<String, SensorGroupingDetails>();
//		if(svo.mHardwareVersion==HW_ID.SHIMMER_3 || svo.mHardwareVersion==HW_ID.SHIMMER_4_SDK){
//			mSensorGroupingMap.put(Configuration.Shimmer3.GuiLabelSensorTiles.PRESSURE_TEMPERATURE, new SensorGroupingDetails(
//					Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_BMP180_PRESSURE),
//					CompatibilityInfoForMaps.listOfCompatibleVersionInfoBMP180));
//		}
//		super.updateSensorGroupingMap();
//	}
	@Override
	public void generateSensorGroupMapping(ShimmerVerObject svo) {
		mSensorGroupingMap = new LinkedHashMap<String, SensorGroupingDetails>();
		if(svo.mHardwareVersion==HW_ID.SHIMMER_3 || svo.mHardwareVersion==HW_ID.SHIMMER_4_SDK){
			mSensorGroupingMap.put(PRESSURE_TEMPERATURE, new SensorGroupingDetails(
					Arrays.asList(SHIMMER_BMP180_PRESSURE),
					CompatibilityInfoForMaps.listOfCompatibleVersionInfoBMP180));
		}
		super.updateSensorGroupingMap();
	}
	//--------- Abstract methods implemented end --------------


	//--------- Sensor specific methods start --------------
	public void setPressureResolution(int i){
		mPressureResolution = i;
	}
	
	public int getPressureResolution(){
		return mPressureResolution;
	}
	
	public double[] calibratePressureSensorData(double UP, double UT){
		double X1 = (UT - pressTempAC6) * pressTempAC5 / 32768;
		double X2 = (pressTempMC * 2048 / (X1 + pressTempMD));
		double B5 = X1 + X2;
		double T = (B5 + 8) / 16;

		double B6 = B5 - 4000;
		X1 = (pressTempB2 * (Math.pow(B6,2)/ 4096)) / 2048;
		X2 = pressTempAC2 * B6 / 2048;
		double X3 = X1 + X2;
		double B3 = (((pressTempAC1 * 4 + X3)*(1<<mPressureResolution) + 2)) / 4;
		X1 = pressTempAC3 * B6 / 8192;
		X2 = (pressTempB1 * (Math.pow(B6,2)/ 4096)) / 65536;
		X3 = ((X1 + X2) + 2) / 4;
		double B4 = pressTempAC4 * (X3 + 32768) / 32768;
		double B7 = (UP - B3) * (50000>>mPressureResolution);
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
	
	public void retrievePressureCalibrationParametersFromPacket(byte[] pressureResoRes, int packetType) {
		if (packetType == BMP180_CALIBRATION_COEFFICIENTS_RESPONSE){
			pressTempAC1 = UtilParseData.calculatetwoscomplement((int)((int)(pressureResoRes[1] & 0xFF) + ((int)(pressureResoRes[0] & 0xFF) << 8)),16);
			pressTempAC2 = UtilParseData.calculatetwoscomplement((int)((int)(pressureResoRes[3] & 0xFF) + ((int)(pressureResoRes[2] & 0xFF) << 8)),16);
			pressTempAC3 = UtilParseData.calculatetwoscomplement((int)((int)(pressureResoRes[5] & 0xFF) + ((int)(pressureResoRes[4] & 0xFF) << 8)),16);
			pressTempAC4 = (int)((int)(pressureResoRes[7] & 0xFF) + ((int)(pressureResoRes[6] & 0xFF) << 8));
			pressTempAC5 = (int)((int)(pressureResoRes[9] & 0xFF) + ((int)(pressureResoRes[8] & 0xFF) << 8));
			pressTempAC6 = (int)((int)(pressureResoRes[11] & 0xFF) + ((int)(pressureResoRes[10] & 0xFF) << 8));
			pressTempB1 = UtilParseData.calculatetwoscomplement((int)((int)(pressureResoRes[13] & 0xFF) + ((int)(pressureResoRes[12] & 0xFF) << 8)),16);
			pressTempB2 = UtilParseData.calculatetwoscomplement((int)((int)(pressureResoRes[15] & 0xFF) + ((int)(pressureResoRes[14] & 0xFF) << 8)),16);
			pressTempMB = UtilParseData.calculatetwoscomplement((int)((int)(pressureResoRes[17] & 0xFF) + ((int)(pressureResoRes[16] & 0xFF) << 8)),16);
			pressTempMC = UtilParseData.calculatetwoscomplement((int)((int)(pressureResoRes[19] & 0xFF) + ((int)(pressureResoRes[18] & 0xFF) << 8)),16);
			pressTempMD = UtilParseData.calculatetwoscomplement((int)((int)(pressureResoRes[21] & 0xFF) + ((int)(pressureResoRes[20] & 0xFF) << 8)),16);
		}
	}
	
	//TODO Check if needed 
	public byte[] getRawCalibrationParameters(ShimmerVerObject svo){        
		byte[] rawcal=new byte[1];
		if (svo.mHardwareVersion==HW_ID.SHIMMER_3 || svo.mHardwareVersion==HW_ID.SHIMMER_4_SDK){
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
	
	public double getPressTempAC1(){
		return pressTempAC1;
	}
	
	public double getPressTempAC2(){
		return pressTempAC2;
	}
	
	public double getPressTempAC3(){
		return pressTempAC3;
	}
	
	public double getPressTempAC4(){
		return pressTempAC4;
	}
	
	public double getPressTempAC5(){
		return pressTempAC5;
	}
	
	public double getPressTempAC6(){
		return pressTempAC6;
	}
	
	public double getPressTempB1(){
		return pressTempB1;
	}
	
	public double getPressTempB2(){
		return pressTempB2;
	}
	
	public double getPressTempMB(){
		return pressTempMB;
	}
	
	public double getPressTempMC(){
		return pressTempMC;
	}
	
	public double getPressTempMD(){
		return pressTempMD;
	}
	//--------- Sensor specific methods end --------------

	@Override
	public boolean checkConfigOptionValues(String stringKey) {
		// TODO Auto-generated method stub
		return false;
	}


}
