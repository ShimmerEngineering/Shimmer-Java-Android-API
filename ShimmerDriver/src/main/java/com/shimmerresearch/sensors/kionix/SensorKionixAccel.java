package com.shimmerresearch.sensors.kionix;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.shimmerresearch.bluetooth.BtCommandDetails;
import com.shimmerresearch.driver.Configuration.CHANNEL_UNITS;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driver.Configuration.Shimmer3.CompatibilityInfoForMaps;
import com.shimmerresearch.driver.calibration.CalibDetails;
import com.shimmerresearch.driver.calibration.CalibDetailsKinematic;
import com.shimmerresearch.driver.calibration.OldCalDetails;
import com.shimmerresearch.driver.calibration.UtilCalibration;
import com.shimmerresearch.driver.calibration.CalibDetails.CALIB_READ_SOURCE;
import com.shimmerresearch.driver.shimmer2r3.ConfigByteLayoutShimmer3;
import com.shimmerresearch.driver.ConfigByteLayout;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.FormatCluster;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driverUtilities.ChannelDetails;
import com.shimmerresearch.driverUtilities.SensorDetails;
import com.shimmerresearch.driverUtilities.SensorDetailsRef;
import com.shimmerresearch.driverUtilities.SensorGroupingDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;
import com.shimmerresearch.driverUtilities.UtilShimmer;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_ENDIAN;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_TYPE;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_TYPE;
import com.shimmerresearch.sensors.AbstractSensor;
import com.shimmerresearch.sensors.ActionSetting;
import com.shimmerresearch.sensors.AbstractSensor.GuiLabelConfigCommon;
import com.shimmerresearch.sensors.AbstractSensor.SENSORS;


/** Parent Sensor Class for the Kionix analog/low-noise accelerometers
 * 
 * @author Ruud Stolk
 * @author Mark Nolan
 */
public abstract class SensorKionixAccel extends AbstractSensor{

	/** * */
	private static final long serialVersionUID = -5027305280613145453L;
	
	//--------- Sensor specific variables start --------------

	public static final int LN_ACCEL_RANGE_VALUE = 0;
	public static final String LN_ACCEL_RANGE_STRING = UtilShimmer.UNICODE_PLUS_MINUS + " 2g" ;//"+/- 2g";
	public static final String OldCalRangeLN2g = "accel_ln_2g";
	
	public CalibDetailsKinematic mCurrentCalibDetailsAccelLn = null;
	
	public boolean mIsUsingDefaultLNAccelParam = true;

	public class GuiLabelConfig{
		public static final String KIONIX_ACCEL_DEFAULT_CALIB = "Low Noise Accel Default Calibration";
		
		//NEW
		public static final String KIONIX_ACCEL_VALID_CALIB = "Low Noise Accel Valid Calibration";
		public static final String KIONIX_ACCEL_CALIB_PARAM = "Low Noise Accel Calibration Details";
	}
	
	public class GuiLabelSensors{
		public static final String ACCEL_LN = "Low-Noise Accelerometer";
	}
	
	public class LABEL_SENSOR_TILE{
		public static final String LOW_NOISE_ACCEL = GuiLabelSensors.ACCEL_LN;
	}
	
	public static class ObjectClusterSensorName{
		public static  String ACCEL_LN_X = "Accel_LN_X";
		public static  String ACCEL_LN_Y = "Accel_LN_Y";
		public static  String ACCEL_LN_Z = "Accel_LN_Z";
	}	
	//--------- Sensor specific variables end --------------
	

	//--------- Bluetooth commands start --------------
	public static final byte SET_ACCEL_CALIBRATION_COMMAND			= (byte) 0x11;
	public static final byte ACCEL_CALIBRATION_RESPONSE       		= (byte) 0x12;
	public static final byte GET_ACCEL_CALIBRATION_COMMAND    		= (byte) 0x13;

	public static final Map<Byte, BtCommandDetails> mBtGetCommandMap;
	static {
		Map<Byte, BtCommandDetails> aMap = new LinkedHashMap<Byte, BtCommandDetails>();
		aMap.put(GET_ACCEL_CALIBRATION_COMMAND, new BtCommandDetails(GET_ACCEL_CALIBRATION_COMMAND,"GET_ACCEL_CALIBRATION_COMMAND",ACCEL_CALIBRATION_RESPONSE));
		mBtGetCommandMap = Collections.unmodifiableMap(aMap);
	}

	public static final Map<Byte, BtCommandDetails> mBtSetCommandMap;
	static {
		Map<Byte, BtCommandDetails> aMap = new LinkedHashMap<Byte, BtCommandDetails>();
		aMap.put(SET_ACCEL_CALIBRATION_COMMAND, new BtCommandDetails(SET_ACCEL_CALIBRATION_COMMAND, "SET_ACCEL_CALIBRATION_COMMAND"));
		mBtSetCommandMap = Collections.unmodifiableMap(aMap);
	}
	//--------- Bluetooth commands end --------------

	
	//--------- Configuration options start --------------
	// No configuration options.
	//--------- Configuration options end --------------

    
    //--------- Constructors for this class start --------------
    /**Just used for accessing calibration*/
	public SensorKionixAccel(SENSORS sensor) {
		super(sensor);
	}
	
	public SensorKionixAccel(SENSORS sensor, ShimmerVerObject svo) {
		super(sensor, svo);
	}
	
	public SensorKionixAccel(SENSORS sensor, ShimmerDevice shimmerDevice) {
		super(sensor, shimmerDevice);
	}

	//--------- Constructors for this class end --------------
	
	
	//--------- Abstract methods implemented start --------------


	@Override
	public void generateConfigOptionsMap() {
		//No configuration options.
	}
	
	@Override
	public ObjectCluster processDataCustom(SensorDetails sensorDetails, byte[] rawData, COMMUNICATION_TYPE commType, ObjectCluster objectCluster, boolean isTimeSyncEnabled, double pcTimestampMs) {
	
		objectCluster = sensorDetails.processDataCommon(rawData, commType, objectCluster, isTimeSyncEnabled, pcTimestampMs);

//		int index = 0;
//		for (ChannelDetails channelDetails:sensorDetails.mListOfChannels){
//			//first process the data originating from the Shimmer sensor
//			byte[] channelByteArray = new byte[channelDetails.mDefaultNumBytes];
//			System.arraycopy(rawData, index, channelByteArray, 0, channelDetails.mDefaultNumBytes);
//			objectCluster = SensorDetails.processShimmerChannelData(channelByteArray, channelDetails, objectCluster);
//			index = index + channelDetails.mDefaultNumBytes;
//			objectCluster.incrementIndexKeeper();
//		}
		
		if(mEnableCalibration && mCurrentCalibDetailsAccelLn!=null){
			//Uncalibrated Accelerometer data
			double[] unCalibratedAccelData = new double[3];
			for (ChannelDetails channelDetails:sensorDetails.mListOfChannels){
				if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.ACCEL_LN_X)){
					unCalibratedAccelData[0] = ((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(channelDetails.mObjectClusterName), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
				}
				else if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.ACCEL_LN_Y)){
					unCalibratedAccelData[1]  = ((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(channelDetails.mObjectClusterName), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
				}
				else if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.ACCEL_LN_Z)){
					unCalibratedAccelData[2]  = ((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(channelDetails.mObjectClusterName), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
				}	
			}
				
			//Calibration
			double[] calibratedAccelData = UtilCalibration.calibrateInertialSensorData(unCalibratedAccelData, mCurrentCalibDetailsAccelLn);
//			double[] calibratedAccelData = UtilCalibration.calibrateInertialSensorData(unCalibratedAccelData, mAlignmentMatrixAnalogAccel, mSensitivityMatrixAnalogAccel, mOffsetVectorAnalogAccel);

			//Add calibrated data to Object cluster
			for (ChannelDetails channelDetails:sensorDetails.mListOfChannels){
				if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.ACCEL_LN_X)){
					objectCluster.addCalData(channelDetails, calibratedAccelData[0], objectCluster.getIndexKeeper()-3, isUsingDefaultLNAccelParam());
				}
				else if(channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.ACCEL_LN_Y)){
					objectCluster.addCalData(channelDetails, calibratedAccelData[1], objectCluster.getIndexKeeper()-2, isUsingDefaultLNAccelParam());
				}
				else if(channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.ACCEL_LN_Z)){
					objectCluster.addCalData(channelDetails, calibratedAccelData[2], objectCluster.getIndexKeeper()-1, isUsingDefaultLNAccelParam());
				}
			}			
		}
		
		//Debugging
		if(mIsDebugOutput){
			super.consolePrintChannelsCal(objectCluster, Arrays.asList(
					new String[]{ObjectClusterSensorName.ACCEL_LN_X, CHANNEL_TYPE.UNCAL.toString()}, 
					new String[]{ObjectClusterSensorName.ACCEL_LN_Y, CHANNEL_TYPE.UNCAL.toString()}, 
					new String[]{ObjectClusterSensorName.ACCEL_LN_Z, CHANNEL_TYPE.UNCAL.toString()}, 
					new String[]{ObjectClusterSensorName.ACCEL_LN_X, CHANNEL_TYPE.CAL.toString()}, 
					new String[]{ObjectClusterSensorName.ACCEL_LN_Y, CHANNEL_TYPE.CAL.toString()},
					new String[]{ObjectClusterSensorName.ACCEL_LN_Z, CHANNEL_TYPE.CAL.toString()}));
		}

		return objectCluster;
	}
	
	
	@Override
	public void configBytesGenerate(ShimmerDevice shimmerDevice, byte[] configBytes, COMMUNICATION_TYPE commType) {
		
		ConfigByteLayout configByteLayout = shimmerDevice.getConfigByteLayout();
		if(configByteLayout instanceof ConfigByteLayoutShimmer3){
			ConfigByteLayoutShimmer3 configByteLayoutCast = (ConfigByteLayoutShimmer3) configByteLayout;
			
			// Analog Accel Calibration Parameters
			byte[] bufferCalibrationParameters = generateCalParamByteArrayAccelLn();
			System.arraycopy(bufferCalibrationParameters, 0, configBytes, configByteLayoutCast.idxAnalogAccelCalibration, configByteLayoutCast.lengthGeneralCalibrationBytes);
		}
	}
	
	
	@Override
	public void configBytesParse(ShimmerDevice shimmerDevice, byte[] configBytes, COMMUNICATION_TYPE commType) {
		ConfigByteLayout configByteLayout = shimmerDevice.getConfigByteLayout();
		if(configByteLayout instanceof ConfigByteLayoutShimmer3){
			ConfigByteLayoutShimmer3 configByteLayoutCast = (ConfigByteLayoutShimmer3) configByteLayout;
			
			if (shimmerDevice.isConnected()){
				getCurrentCalibDetailsAccelLn().mCalibReadSource=CALIB_READ_SOURCE.INFOMEM;
			}
			
			// Analog Accel Calibration Parameters
			byte[] bufferCalibrationParameters = new byte[configByteLayoutCast.lengthGeneralCalibrationBytes];
			System.arraycopy(configBytes, configByteLayoutCast.idxAnalogAccelCalibration, bufferCalibrationParameters, 0 , configByteLayoutCast.lengthGeneralCalibrationBytes);
			parseCalibParamFromPacketAccelAnalog(bufferCalibrationParameters, CALIB_READ_SOURCE.INFOMEM);
		}
	}


	@Override
	public Object setConfigValueUsingConfigLabel(Integer sensorId, String configLabel, Object valueToSet) {
		Object returnValue = null;
		switch(configLabel){
//			case(GuiLabelConfigCommon.KINEMATIC_CALIBRATION_PER_SENSOR):
//				if(sensorId==Configuration.Shimmer3.SENSOR_ID.SHIMMER_ANALOG_ACCEL){
//					TreeMap<Integer, CalibDetailsKinematic> mapOfKinematicSensorCalibration = (TreeMap<Integer, CalibDetailsKinematic>) valueToSet;
//					setKinematicCalibration(mapOfKinematicSensorCalibration);
//					returnValue = valueToSet;
//				}
//	    		break;
	        default:
	        	returnValue = super.setConfigValueUsingConfigLabelCommon(sensorId, configLabel, valueToSet);
	        	break;
		}
		return returnValue;
	}

	@Override
	public Object getConfigValueUsingConfigLabel(Integer sensorId, String configLabel) {
		Object returnValue = null;
		switch(configLabel){
			case(GuiLabelConfigCommon.RANGE):
				if(sensorId==Configuration.Shimmer3.SENSOR_ID.SHIMMER_ANALOG_ACCEL){
					returnValue = 0;
				}
				break;
			default:
				returnValue = super.getConfigValueUsingConfigLabelCommon(sensorId, configLabel);
				break;
		}
		return returnValue;
	}

	
	@Override
	public void setSensorSamplingRate(double samplingRateHz) {
		// No data rate setting.
	}
	
	
	@Override
	public boolean setDefaultConfigForSensor(int sensorId, boolean isSensorEnabled) {
		if(mSensorMap.containsKey(sensorId)){
			updateCurrentAccelLnCalibInUse();
			return true;
		}
		return false;
	}
	
	
	@Override
	public boolean checkConfigOptionValues(String stringKey) {
		if(mConfigOptionsMap.containsKey(stringKey)){
			//XXX Return true if mSensorMap contains sensorId regardless of the fact there a no configuration options?
			return true;
		}
		return false;
	}
	
	
	@Override
	public Object getSettings(String componentName, COMMUNICATION_TYPE commType) {
		//TODO RS - Implement rest of this method.
		return null;
	}

	
	@Override
	public ActionSetting setSettings(String componentName, Object valueToSet, COMMUNICATION_TYPE commType) {
		ActionSetting actionsetting = new ActionSetting(commType);
		//TODO RS - Implement rest of this method.		
		return actionsetting;
	}
	
	@Override
	public boolean processResponse(int responseCommand, Object parsedResponse, COMMUNICATION_TYPE commType) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public void checkShimmerConfigBeforeConfiguring() {
		// TODO Auto-generated method stub
		
	}
	//--------- Abstract methods implemented end --------------


	//--------- Sensor specific methods start --------------
	private byte[] generateCalParamAnalogAccel(){
		return mCurrentCalibDetailsAccelLn.generateCalParamByteArray();
	}
	
	public void parseCalibParamFromPacketAccelAnalog(byte[] bufferCalibrationParameters, CALIB_READ_SOURCE calibReadSource) {
		mCurrentCalibDetailsAccelLn.parseCalParamByteArray(bufferCalibrationParameters, calibReadSource);
	}
	
	private void setDefaultCalibrationShimmer3LowNoiseAccel() {
		mCurrentCalibDetailsAccelLn.resetToDefaultParameters();
	}

	public String getSensorName(){
		return mSensorName;
	}
	
	
	public boolean isUsingDefaultLNAccelParam(){
		return mCurrentCalibDetailsAccelLn.isUsingDefaultParameters();
	}
	
	public double[][] getAlignmentMatrixAccel(){
		return mCurrentCalibDetailsAccelLn.getValidAlignmentMatrix();
	}

	public double[][] getSensitivityMatrixAccel(){
		return mCurrentCalibDetailsAccelLn.getValidSensitivityMatrix();
	}


	public double[][] getOffsetVectorMatrixAccel(){
		return mCurrentCalibDetailsAccelLn.getValidOffsetVector();
	}
	
	public void updateCurrentAccelLnCalibInUse(){
		mCurrentCalibDetailsAccelLn = getCurrentCalibDetailsAccelLn();
	}
	
	public CalibDetailsKinematic getCurrentCalibDetailsAccelLn(){
		CalibDetails calibPerSensor = getCalibForSensor(Configuration.Shimmer3.SENSOR_ID.SHIMMER_ANALOG_ACCEL, LN_ACCEL_RANGE_VALUE);
		if(calibPerSensor!=null){
			return (CalibDetailsKinematic) calibPerSensor;
		}
		return null;
	}
	
	/**
	 * Converts the Analog Accel calibration variables from Shimmer Object
	 * into a byte array for sending to the Shimmer.
	 * 
	 * @return the bytes array containing the Analog Accel Calibration
	 */
	public byte[] generateCalParamByteArrayAccelLn(){
		return getCurrentCalibDetailsAccelLn().generateCalParamByteArray();
	}

	//--------- Sensor specific methods end --------------



	
	//--------- Optional methods to override in Sensor Class start --------
	
	/* (non-Javadoc)
	 * @see com.shimmerresearch.sensors.AbstractSensor#isSensorUsingDefaultCal(int)
	 */
	@Override
	public boolean isSensorUsingDefaultCal(int sensorId) {
		if(sensorId==Configuration.Shimmer3.SENSOR_ID.SHIMMER_ANALOG_ACCEL){
			return isUsingDefaultLNAccelParam();
		}
		return false;
	}
	
	@Override
	public void setCalibrationMapPerSensor(int sensorId, TreeMap<Integer, CalibDetails> mapOfSensorCalibration) {
		super.setCalibrationMapPerSensor(sensorId, mapOfSensorCalibration);
		updateCurrentAccelLnCalibInUse();
	}

	//--------- Optional methods to override in Sensor Class end --------
	
	public void updateIsUsingDefaultLNAccelParam() {
		mIsUsingDefaultLNAccelParam = getCurrentCalibDetailsAccelLn().isUsingDefaultParameters();
	}

}
