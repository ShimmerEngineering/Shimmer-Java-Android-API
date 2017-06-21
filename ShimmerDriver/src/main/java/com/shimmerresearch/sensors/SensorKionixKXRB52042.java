package com.shimmerresearch.sensors;

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
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.FormatCluster;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driverUtilities.ChannelDetails;
import com.shimmerresearch.driverUtilities.SensorDetails;
import com.shimmerresearch.driverUtilities.SensorDetailsRef;
import com.shimmerresearch.driverUtilities.SensorGroupingDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_ENDIAN;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_TYPE;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_TYPE;


/** Sensorclass for KionixKXRB52042 - analog/low-noise accelerometer
 * 
 * @author Ruud Stolk
 * @author Mark Nolan
 */
public class SensorKionixKXRB52042 extends AbstractSensor{

	/** * */
	private static final long serialVersionUID = -5027305280613145453L;
	
	//--------- Sensor specific variables start --------------
	public static final double[][] AlignmentMatrixLowNoiseAccelShimmer3 = {{0,-1,0},{-1,0,0},{0,0,-1}};
	public static final double[][] OffsetVectorLowNoiseAccelShimmer3 = {{2047},{2047},{2047}};
	public static final double[][] SensitivityMatrixLowNoiseAccel2gShimmer3 = {{83,0,0},{0,83,0},{0,0,83}};

	public static final int LN_ACCEL_RANGE_CONSTANT = 0;
	public static final String OldCalRangeLN2g = "accel_ln_2g";
	
    public static final Map<String, OldCalDetails> mOldCalRangeMap;
    static {
        Map<String, OldCalDetails> aMap = new LinkedHashMap<String, OldCalDetails>();
        aMap.put("accel_ln_2g", new OldCalDetails("accel_ln_2g", Configuration.Shimmer3.SensorMapKey.SHIMMER_ANALOG_ACCEL, LN_ACCEL_RANGE_CONSTANT));
        mOldCalRangeMap = Collections.unmodifiableMap(aMap);
    }

	private CalibDetailsKinematic calibDetailsAccelLn2g = new CalibDetailsKinematic(
			LN_ACCEL_RANGE_CONSTANT, "+/- 2g", 
			AlignmentMatrixLowNoiseAccelShimmer3, SensitivityMatrixLowNoiseAccel2gShimmer3, OffsetVectorLowNoiseAccelShimmer3);
	public CalibDetailsKinematic mCurrentCalibDetailsAccelLn = calibDetailsAccelLn2g;


	public class GuiLabelConfig{
		public static final String KXRB8_2042_ACCEL_DEFAULT_CALIB = "Low Noise Accel Default Calibration";
		
		//NEW
		public static final String KXRB8_2042_ACCEL_VALID_CALIB = "Low Noise Accel Valid Calibration";
		public static final String KXRB8_2042_ACCEL_CALIB_PARAM = "Low Noise Accel Calibration Details";
	}
	
	public class GuiLabelSensors{
		public static final String ACCEL_LN = "Low-Noise Accelerometer";
	}
	
	public class GuiLabelSensorTiles{
		public static final String LOW_NOISE_ACCEL = GuiLabelSensors.ACCEL_LN;
	}
	
	public static class DatabaseChannelHandles{
		public static final String LN_ACC_X = "KXRB8_2042_X";
		public static final String LN_ACC_Y = "KXRB8_2042_Y";
		public static final String LN_ACC_Z = "KXRB8_2042_Z";
	}
	public static final class DatabaseConfigHandle{
//		public static final String LN_ACC = "KXRB8_2042_Acc";
		
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

	//--------- Sensor info start --------------
	public static final SensorDetailsRef sensorKionixKXRB52042 = new SensorDetailsRef(
			0x80, //== Configuration.Shimmer3.SensorBitmap.SENSOR_A_ACCEL will be: SensorBitmap.SENSOR_A_ACCEL, 
			0x80, //== Configuration.Shimmer3.SensorBitmap.SENSOR_A_ACCEL will be: SensorBitmap.SENSOR_A_ACCEL, 
			GuiLabelSensors.ACCEL_LN,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW,
			null,
			Arrays.asList(ObjectClusterSensorName.ACCEL_LN_X,ObjectClusterSensorName.ACCEL_LN_Y,ObjectClusterSensorName.ACCEL_LN_Z));
//	{
//		sensorKionixKXRB52042.mCalibSensorKey = 0x01;
//	}
	
	public static final Map<Integer, SensorDetailsRef> mSensorMapRef;
    static {
        Map<Integer, SensorDetailsRef> aMap = new LinkedHashMap<Integer, SensorDetailsRef>();
        aMap.put(Configuration.Shimmer3.SensorMapKey.SHIMMER_ANALOG_ACCEL, SensorKionixKXRB52042.sensorKionixKXRB52042);

		mSensorMapRef = Collections.unmodifiableMap(aMap);
    }
	//--------- Sensor info end --------------
    
    
	//--------- Channel info start --------------
    public static final ChannelDetails channelLSM303AccelX = new ChannelDetails(
			ObjectClusterSensorName.ACCEL_LN_X,
			ObjectClusterSensorName.ACCEL_LN_X,
			DatabaseChannelHandles.LN_ACC_X,
//			CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.LSB,
			CHANNEL_DATA_TYPE.UINT12, 2, CHANNEL_DATA_ENDIAN.LSB,
			CHANNEL_UNITS.METER_PER_SECOND_SQUARE,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL),
			0x00);
    
    
    public static final ChannelDetails channelLSM303AccelY = new ChannelDetails(
			ObjectClusterSensorName.ACCEL_LN_Y,
			ObjectClusterSensorName.ACCEL_LN_Y,
			DatabaseChannelHandles.LN_ACC_Y,
//			CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.LSB,
			CHANNEL_DATA_TYPE.UINT12, 2, CHANNEL_DATA_ENDIAN.LSB,
			CHANNEL_UNITS.METER_PER_SECOND_SQUARE,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL),
			0x01);
    
    
    public static final ChannelDetails channelLSM303AccelZ = new ChannelDetails(
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
        aMap.put(SensorKionixKXRB52042.ObjectClusterSensorName.ACCEL_LN_X, SensorKionixKXRB52042.channelLSM303AccelX);
        aMap.put(SensorKionixKXRB52042.ObjectClusterSensorName.ACCEL_LN_Y, SensorKionixKXRB52042.channelLSM303AccelY);
        aMap.put(SensorKionixKXRB52042.ObjectClusterSensorName.ACCEL_LN_Z, SensorKionixKXRB52042.channelLSM303AccelZ);
		mChannelMapRef = Collections.unmodifiableMap(aMap);
    }
	//--------- Channel info end --------------

	
    //--------- Constructors for this class start --------------
    /**Just used for accessing calibration*/
	public SensorKionixKXRB52042() {
		super(SENSORS.KIONIXKXRB52042);
		initialise();
	}
	
	public SensorKionixKXRB52042(ShimmerVerObject svo) {
		super(SENSORS.KIONIXKXRB52042, svo);
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
		//No configuration options.
	}
	
	@Override
	public void generateSensorGroupMapping() {
		mSensorGroupingMap = new LinkedHashMap<Integer, SensorGroupingDetails>();
		if(mShimmerVerObject.isShimmerGen3() || mShimmerVerObject.isShimmerGen4()){
			int groupIndex = Configuration.Shimmer3.GuiLabelSensorTiles.LOW_NOISE_ACCEL.ordinal();
			mSensorGroupingMap.put(groupIndex, new SensorGroupingDetails(
					GuiLabelSensorTiles.LOW_NOISE_ACCEL,
					Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_ANALOG_ACCEL),
					CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW));
		}
		super.updateSensorGroupingMap();	
	}

	@Override
	public ObjectCluster processDataCustom(SensorDetails sensorDetails, byte[] rawData, COMMUNICATION_TYPE commType, ObjectCluster objectCluster, boolean isTimeSyncEnabled, long pcTimestamp) {
	
		objectCluster = sensorDetails.processDataCommon(rawData, commType, objectCluster, isTimeSyncEnabled, pcTimestamp);

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
	public void configByteArrayGenerate(ShimmerDevice shimmerDevice,byte[] mInfoMemBytes) {
//		int idxAnalogAccelCalibration = 31;
		//fix for newer firmware -> see InfomemLayoutShimmer3
		int idxAnalogAccelCalibration =		34;
		int lengthGeneralCalibrationBytes = 21;
		
		//Accel Calibration Parameters
		byte[] bufferCalibrationParameters = generateCalParamAnalogAccel();
		System.arraycopy(bufferCalibrationParameters, 0, mInfoMemBytes, idxAnalogAccelCalibration, lengthGeneralCalibrationBytes);
	}
	
	
	@Override
	public void configByteArrayParse(ShimmerDevice shimmerDevice, byte[] mInfoMemBytes) {
//		int idxAnalogAccelCalibration = 31;
		//fix for newer firmware -> see InfomemLayoutShimmer3
		int idxAnalogAccelCalibration =		34;
		int lengthGeneralCalibrationBytes = 21;

		//Accel Calibration Parameters
		byte[] bufferCalibrationParameters = new byte[lengthGeneralCalibrationBytes];
		System.arraycopy(mInfoMemBytes, idxAnalogAccelCalibration, bufferCalibrationParameters, 0 , lengthGeneralCalibrationBytes);
		parseCalibParamFromPacketAccelAnalog(bufferCalibrationParameters, CALIB_READ_SOURCE.INFOMEM);	
	}


	@Override
	public Object setConfigValueUsingConfigLabel(Integer sensorMapKey, String configLabel, Object valueToSet) {
		Object returnValue = null;
		switch(configLabel){
//			case(GuiLabelConfigCommon.KINEMATIC_CALIBRATION_PER_SENSOR):
//				if(sensorMapKey==Configuration.Shimmer3.SensorMapKey.SHIMMER_ANALOG_ACCEL){
//					TreeMap<Integer, CalibDetailsKinematic> mapOfKinematicSensorCalibration = (TreeMap<Integer, CalibDetailsKinematic>) valueToSet;
//					setKinematicCalibration(mapOfKinematicSensorCalibration);
//					returnValue = valueToSet;
//				}
//	    		break;
	        default:
	        	returnValue = super.setConfigValueUsingConfigLabelCommon(sensorMapKey, configLabel, valueToSet);
	        	break;
		}
		return returnValue;
	}

	@Override
	public Object getConfigValueUsingConfigLabel(Integer sensorMapKey, String configLabel) {
		Object returnValue = null;
		switch(configLabel){
			case(GuiLabelConfigCommon.RANGE):
				if(sensorMapKey==Configuration.Shimmer3.SensorMapKey.SHIMMER_ANALOG_ACCEL){
					returnValue = 0;
				}
				break;
			default:
				returnValue = super.getConfigValueUsingConfigLabelCommon(sensorMapKey, configLabel);
				break;
		}
		return returnValue;
	}

	
	@Override
	public void setSensorSamplingRate(double samplingRateHz) {
		// No data rate setting.
	}
	
	
	@Override
	public boolean setDefaultConfigForSensor(int sensorMapKey, boolean isSensorEnabled) {
		if(mSensorMap.containsKey(sensorMapKey)){
			updateCurrentAccelLnCalibInUse();
			return true;
		}
		return false;
	}
	
	
	@Override
	public boolean checkConfigOptionValues(String stringKey) {
		if(mConfigOptionsMap.containsKey(stringKey)){
			//XXX Return true if mSensorMap contains sensorMapKey regardless of the fact there a no configuration options?
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
	public LinkedHashMap<String, Object> getConfigMapForDb() {
		LinkedHashMap<String, Object> mapOfConfig = new LinkedHashMap<String, Object>();
		super.addCalibDetailsToDbMap(mapOfConfig, 
				getCurrentCalibDetailsAccelLn(), 
				SensorKionixKXRB52042.DatabaseConfigHandle.LIST_OF_CALIB_HANDLES_LN_ACC,
				SensorKionixKXRB52042.DatabaseConfigHandle.LN_ACC_CALIB_TIME);
		return mapOfConfig;
	}

	@Override
	public void parseConfigMapFromDb(LinkedHashMap<String, Object> mapOfConfigPerShimmer) {
		//Analog Accel Calibration Configuration
		parseCalibDetailsKinematicFromDb(mapOfConfigPerShimmer, 
				Configuration.Shimmer3.SensorMapKey.SHIMMER_ANALOG_ACCEL, 
				0, 
				SensorKionixKXRB52042.DatabaseConfigHandle.LIST_OF_CALIB_HANDLES_LN_ACC,
				SensorKionixKXRB52042.DatabaseConfigHandle.LN_ACC_CALIB_TIME);
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
	
	public CalibDetailsKinematic getCurrentCalibDetails(int sensorMapKey, int range){
		CalibDetailsKinematic calibPerSensor = (CalibDetailsKinematic) super.getCalibForSensor(sensorMapKey, range);
		return calibPerSensor;
	}
	
	public void updateCurrentAccelLnCalibInUse(){
		mCurrentCalibDetailsAccelLn = getCurrentCalibDetailsAccelLn();
	}
	
	public CalibDetailsKinematic getCurrentCalibDetailsAccelLn(){
		CalibDetails calibPerSensor = getCalibForSensor(Configuration.Shimmer3.SensorMapKey.SHIMMER_ANALOG_ACCEL, LN_ACCEL_RANGE_CONSTANT);
		if(calibPerSensor!=null){
			return (CalibDetailsKinematic) calibPerSensor;
		}
		return null;
	}
	
	//--------- Sensor specific methods end --------------



	
	//--------- Optional methods to override in Sensor Class start --------
	
	@Override
	public void generateCalibMap() {
		super.generateCalibMap();
		
		TreeMap<Integer, CalibDetails> calibMapAccelLn = new TreeMap<Integer, CalibDetails>();
		calibMapAccelLn.put(calibDetailsAccelLn2g.mRangeValue, calibDetailsAccelLn2g);
		
		addCalibrationPerSensor(Configuration.Shimmer3.SensorMapKey.SHIMMER_ANALOG_ACCEL, calibMapAccelLn);
		updateCurrentAccelLnCalibInUse();
	}
	
	/* (non-Javadoc)
	 * @see com.shimmerresearch.sensors.AbstractSensor#isSensorUsingDefaultCal(int)
	 */
	@Override
	public boolean isSensorUsingDefaultCal(int sensorMapKey) {
		if(sensorMapKey==Configuration.Shimmer3.SensorMapKey.SHIMMER_ANALOG_ACCEL){
			return isUsingDefaultLNAccelParam();
		}
		return false;
	}
	
	
	//--------- Optional methods to override in Sensor Class end --------

}
