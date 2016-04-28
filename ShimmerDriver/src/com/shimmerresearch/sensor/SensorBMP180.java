package com.shimmerresearch.sensor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.shimmerresearch.driver.Configuration.CHANNEL_UNITS;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driver.Configuration.Shimmer3;
import com.shimmerresearch.driver.Configuration.Shimmer3.CompatibilityInfoForMaps;
import com.shimmerresearch.driver.Configuration.Shimmer3.DatabaseChannelHandles;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.FormatCluster;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driverUtilities.SensorDetails;
import com.shimmerresearch.driverUtilities.ChannelDetails;
import com.shimmerresearch.driverUtilities.SensorConfigOptionDetails;
import com.shimmerresearch.driverUtilities.SensorGroupingDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_ENDIAN;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_TYPE;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_TYPE;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.HW_ID;
import com.shimmerresearch.sensor.AbstractSensor.SENSORS;

public class SensorBMP180 extends AbstractSensor implements Serializable {
	
	/** * */
	private static final long serialVersionUID = 4559709230029277863L;
	
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
	
	public static final byte GET_BMP180_PRES_CALIBRATION_COMMAND 	= (byte) 0x57;
	public static final byte BMP180_CALIBRATION_COEFFICIENTS_RESPONSE = (byte) 0x58;
	public static final byte GET_BMP180_CALIBRATION_COEFFICIENTS_COMMAND = (byte) 0x59;
	
	public int mPressureResolution = 0;
	
    public static final Map<Integer, SensorDetails> mSensorMapRef;
    static {
        Map<Integer, SensorDetails> aMap = new LinkedHashMap<Integer, SensorDetails>();
        long streamingByteIndex = 2;
		long logHeaderByteIndex = 2;
		aMap.put(Configuration.Shimmer3.SensorMapKey.BMP180_PRESSURE, new SensorDetails(0x04<<(streamingByteIndex*8), 0x04<<(logHeaderByteIndex*8), Shimmer3.GuiLabelSensors.PRESS_TEMP_BMP180));
		aMap.get(Configuration.Shimmer3.SensorMapKey.BMP180_PRESSURE).mListOfCompatibleVersionInfo = CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW;
		aMap.get(Configuration.Shimmer3.SensorMapKey.BMP180_PRESSURE).mListOfConfigOptionKeysAssociated = Arrays.asList(
				Configuration.Shimmer3.GuiLabelConfig.PRESSURE_RESOLUTION);
		aMap.get(Configuration.Shimmer3.SensorMapKey.BMP180_PRESSURE).mListOfChannelsRef = Arrays.asList(
				Configuration.Shimmer3.ObjectClusterSensorName.PRESSURE_BMP180,
				Configuration.Shimmer3.ObjectClusterSensorName.TEMPERATURE_BMP180);

		mSensorMapRef = Collections.unmodifiableMap(aMap);
    }
	
	public SensorBMP180(ShimmerVerObject svo) {
		super(svo);
		mSensorName = SENSORS.BMP180.toString();
		mGuiFriendlyLabel = Shimmer3.GuiLabelSensors.BMP_180;
		mIntExpBoardPowerRequired = false; 
	}

	@Override
	public String getSensorName() {
		// TODO Auto-generated method stub
		return mSensorName;
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
			case(Configuration.Shimmer3.GuiLabelConfig.PRESSURE_RESOLUTION):
				setPressureResolution((int)valueToSet);
		 		break;
		}
		return actionSetting;
	}

	
	@Override
	public ObjectCluster processData(byte[] rawData,COMMUNICATION_TYPE commType, ObjectCluster object) {
		int index = 0;
		for (ChannelDetails channelDetails:mMapOfCommTypetoChannel.get(commType).values()){
			//first process the data originating from the Shimmer sensor
			byte[] channelByteArray = new byte[channelDetails.mDefaultNumBytes];
			System.arraycopy(rawData, index, channelByteArray, 0, channelDetails.mDefaultNumBytes);
			object = processShimmerChannelData(rawData, channelDetails, object);
		}
		return object;
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
	
	// --------------- Check if needed --------------------
	
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
	
	@Override
	public void infoMemByteArrayGenerate(ShimmerDevice shimmerDevice, byte[] mInfoMemBytes) {
		int idxConfigSetupByte3 =	9;
		int bitShiftBMP180PressureResolution = 4;
		int maskBMP180PressureResolution = 0x03;
		mInfoMemBytes[idxConfigSetupByte3] |= (byte) ((mPressureResolution & maskBMP180PressureResolution) << bitShiftBMP180PressureResolution);
		
	}

	@Override
	public void infoMemByteArrayParse(ShimmerDevice shimmerDevice, byte[] mInfoMemBytes) {
		int idxConfigSetupByte3 =	9;
		int bitShiftBMP180PressureResolution = 4;
		int maskBMP180PressureResolution = 0x03;
		mPressureResolution = (mInfoMemBytes[idxConfigSetupByte3] >> bitShiftBMP180PressureResolution) & maskBMP180PressureResolution;
		
	}

	@Override
	public Map<String, SensorGroupingDetails> getSensorGroupingMap() {
		super.updateSensorGroupingMap();
		return mSensorGroupingMap;
	}

	@Override
	public Object setConfigValueUsingConfigLabel(String componentName,
			Object valueToSet) {
		Object returnValue = null;
		switch(componentName){
			case(Configuration.Shimmer3.GuiLabelConfig.PRESSURE_RESOLUTION):
				setPressureResolution((int)valueToSet);
				returnValue = valueToSet;
		 		break;
	}
		return returnValue;
	}
	
	public void setPressureResolution(int i){
		mPressureResolution = i;
	}
	
	@Override
	public Object getConfigValueUsingConfigLabel(String componentName) {
		Object returnValue = null;
		
		switch(componentName){
		case(Configuration.Shimmer3.GuiLabelConfig.PRESSURE_RESOLUTION):
			returnValue = getPressureResolution();
	 		break;
		  }
		return returnValue;
	}

	public int getPressureResolution(){
		return mPressureResolution;
	}
	
	@Override
	public void setSamplingRateFromFreq() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDefaultConfiguration() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public HashMap<COMMUNICATION_TYPE, LinkedHashMap<Integer, ChannelDetails>> generateChannelDetailsMap(ShimmerVerObject svo) {
		LinkedHashMap<Integer, ChannelDetails> mapOfChannelDetails = new LinkedHashMap<Integer, ChannelDetails>();
		ChannelDetails channelDetails = null;
		int index = 0;

		channelDetails = new ChannelDetails(
				Configuration.Shimmer3.ObjectClusterSensorName.TEMPERATURE_BMP180,
				Configuration.Shimmer3.ObjectClusterSensorName.TEMPERATURE_BMP180,
				DatabaseChannelHandles.TEMPERATURE,
				CHANNEL_DATA_TYPE.UINT16, 2, CHANNEL_DATA_ENDIAN.MSB,
				CHANNEL_UNITS.DEGREES_CELSUIS,
				Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
		mapOfChannelDetails.put(index++, channelDetails);

		channelDetails = new ChannelDetails(
				Configuration.Shimmer3.ObjectClusterSensorName.PRESSURE_BMP180,
				Configuration.Shimmer3.ObjectClusterSensorName.PRESSURE_BMP180,
				DatabaseChannelHandles.PRESSURE,
				CHANNEL_DATA_TYPE.UINT24, 3, CHANNEL_DATA_ENDIAN.MSB,
				CHANNEL_UNITS.KPASCAL,
				Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
		mapOfChannelDetails.put(index++, channelDetails);					  

		mMapOfCommTypetoChannel.put(COMMUNICATION_TYPE.SD, mapOfChannelDetails);
		mMapOfCommTypetoChannel.put(COMMUNICATION_TYPE.BLUETOOTH, mapOfChannelDetails);

		return mMapOfCommTypetoChannel;
	}

	@Override
	public HashMap<String, SensorConfigOptionDetails> generateConfigOptionsMap(ShimmerVerObject svo) {
		mConfigOptionsMap.clear();
		
		mConfigOptionsMap.put(Configuration.Shimmer3.GuiLabelConfig.PRESSURE_RESOLUTION , 
				new SensorConfigOptionDetails(Configuration.Shimmer3.ListofPressureResolution, 
										Configuration.Shimmer3.ListofPressureResolutionConfigValues, 
										SensorConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX,
										CompatibilityInfoForMaps.listOfCompatibleVersionInfoBMP180));
		return mConfigOptionsMap;
	}

	@Override
	public List<Integer> generateListOfSensorMapKeysConflicting(ShimmerVerObject svo) {
		
		return null;
	}

	@Override
	public List<String> generateListOfConfigOptionKeysAssociated(ShimmerVerObject svo) {
		return mListOfConfigOptionKeysAssociated = Arrays.asList(
				Configuration.Shimmer3.GuiLabelConfig.PRESSURE_RESOLUTION);
	}

	@Override
	public Map<String, SensorGroupingDetails> generateSensorGroupMapping(ShimmerVerObject svo) {
		if(svo.mHardwareVersion==HW_ID.SHIMMER_3 || svo.mHardwareVersion==HW_ID.SHIMMER_4_SDK){
			mSensorGroupingMap.put(Configuration.Shimmer3.GuiLabelSensorTiles.PRESSURE_TEMPERATURE, new SensorGroupingDetails(
					Arrays.asList(Configuration.Shimmer3.SensorMapKey.BMP180_PRESSURE)));
			mSensorGroupingMap.get(Configuration.Shimmer3.GuiLabelSensorTiles.PRESSURE_TEMPERATURE).mListOfCompatibleVersionInfo = CompatibilityInfoForMaps.listOfCompatibleVersionInfoBMP180;
		}
		return mSensorGroupingMap;
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

}
