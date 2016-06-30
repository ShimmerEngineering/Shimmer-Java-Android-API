package com.shimmerresearch.sensors;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import com.shimmerresearch.bluetooth.BtCommandDetails;
import com.shimmerresearch.driver.Configuration.CHANNEL_UNITS;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driver.Configuration.Shimmer3.CompatibilityInfoForMaps;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.FormatCluster;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driverUtilities.CalibDetailsKinematic;
import com.shimmerresearch.driverUtilities.ChannelDetails;
import com.shimmerresearch.driverUtilities.SensorDetails;
import com.shimmerresearch.driverUtilities.SensorDetailsRef;
import com.shimmerresearch.driverUtilities.SensorGroupingDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;
import com.shimmerresearch.driverUtilities.UtilCalibration;
import com.shimmerresearch.driverUtilities.UtilParseData;
import com.shimmerresearch.driverUtilities.UtilShimmer;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_ENDIAN;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_TYPE;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_TYPE;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.HW_ID;

/** Sensorclass for KionixKXRB52042 - analog/low-noise accelerometer
 * 
 * @author Ruud Stolk
 */
public class SensorKionixKXRB52042 extends AbstractSensor{

	/** * */
	private static final long serialVersionUID = -5027305280613145453L;
	
	//--------- Sensor specific variables start --------------
	/**TODO use calibration map instead*/
	@Deprecated
	protected boolean mDefaultCalibrationParametersAccel = true;
	/**TODO use calibration map instead*/
	@Deprecated
	protected double[][] mAlignmentMatrixAnalogAccel = {{-1,0,0},{0,-1,0},{0,0,1}}; 			
	/**TODO use calibration map instead*/
	@Deprecated
	protected double[][] mSensitivityMatrixAnalogAccel = {{38,0,0},{0,38,0},{0,0,38}}; 	
	/**TODO use calibration map instead*/
	@Deprecated
	protected double[][] mOffsetVectorAnalogAccel = {{2048},{2048},{2048}};

	public static final double[][] AlignmentMatrixLowNoiseAccelShimmer3 = {{0,-1,0},{-1,0,0},{0,0,-1}};
	public static final double[][] OffsetVectorLowNoiseAccelShimmer3 = {{2047},{2047},{2047}};
	public static final double[][] SensitivityMatrixLowNoiseAccel2gShimmer3 = {{83,0,0},{0,83,0},{0,0,83}};

	protected TreeMap<Integer, CalibDetailsKinematic> mCalibMapAccelAnalogShimmer3 = new TreeMap<Integer, CalibDetailsKinematic>(); 
	{
		mCalibMapAccelAnalogShimmer3.put(0, new CalibDetailsKinematic(0, "+/- 2g",
						AlignmentMatrixLowNoiseAccelShimmer3, SensitivityMatrixLowNoiseAccel2gShimmer3, OffsetVectorLowNoiseAccelShimmer3));
	}

	//TODO commented - RS (20/5/2016): Keep in Configuration.java for now. 
//	public class SensorBitmap{
//		public static final int SENSOR_A_ACCEL			= 0x80;
//	}
//	
	
	public class GuiLabelConfig{
		public static final String KXRB8_2042_ACCEL_DEFAULT_CALIB = "Low Noise Accel Default Calibration";
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
			CHANNEL_DATA_TYPE.UINT12, 2, CHANNEL_DATA_ENDIAN.LSB,
			CHANNEL_UNITS.METER_PER_SECOND_SQUARE,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
    
    
    public static final ChannelDetails channelLSM303AccelY = new ChannelDetails(
			ObjectClusterSensorName.ACCEL_LN_Y,
			ObjectClusterSensorName.ACCEL_LN_Y,
			DatabaseChannelHandles.LN_ACC_Y,
			CHANNEL_DATA_TYPE.UINT12, 2, CHANNEL_DATA_ENDIAN.LSB,
			CHANNEL_UNITS.METER_PER_SECOND_SQUARE,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
    
    
    public static final ChannelDetails channelLSM303AccelZ = new ChannelDetails(
			ObjectClusterSensorName.ACCEL_LN_Z,
			ObjectClusterSensorName.ACCEL_LN_Z,
			DatabaseChannelHandles.LN_ACC_Z,
			CHANNEL_DATA_TYPE.UINT12, 2, CHANNEL_DATA_ENDIAN.LSB,
			CHANNEL_UNITS.METER_PER_SECOND_SQUARE,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
    
    
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
	public SensorKionixKXRB52042(ShimmerVerObject svo) {
		super(svo);
		setSensorName(SENSORS.KIONIXKXRB52042.toString());
	}
	//--------- Constructors for this class end --------------
	
	
	//--------- Abstract methods implemented start --------------
	@Override
	public void generateSensorMap(ShimmerVerObject svo) {
		super.createLocalSensorMapWithCustomParser(mSensorMapRef, mChannelMapRef);		
	}

	
	@Override
	public void generateConfigOptionsMap(ShimmerVerObject svo) {
		//No configuration options.
	}
	
	
	@Override
	public void generateSensorGroupMapping(ShimmerVerObject svo) {
		mSensorGroupingMap = new LinkedHashMap<Integer, SensorGroupingDetails>();
		if(svo.mHardwareVersion==HW_ID.SHIMMER_3 || svo.mHardwareVersion==HW_ID.SHIMMER_4_SDK){
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
		
		if(mEnableCalibration){
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
			double[] calibratedAccelData = UtilCalibration.calibrateInertialSensorData(unCalibratedAccelData, mAlignmentMatrixAnalogAccel, mSensitivityMatrixAnalogAccel, mOffsetVectorAnalogAccel);

			//Add calibrated data to Object cluster
			for (ChannelDetails channelDetails:sensorDetails.mListOfChannels){
				if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.ACCEL_LN_X)){
					objectCluster.addCalData(channelDetails, calibratedAccelData[0], objectCluster.getIndexKeeper()-3);
				}
				else if(channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.ACCEL_LN_Y)){
					objectCluster.addCalData(channelDetails, calibratedAccelData[1], objectCluster.getIndexKeeper()-2);
				}
				else if(channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.ACCEL_LN_Z)){
					objectCluster.addCalData(channelDetails, calibratedAccelData[2], objectCluster.getIndexKeeper()-1);
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
	public void infoMemByteArrayGenerate(ShimmerDevice shimmerDevice,byte[] mInfoMemBytes) {
//		int idxAnalogAccelCalibration = 31;
		//fix for newer firmware -> see InfomemLayoutShimmer3
		int idxAnalogAccelCalibration =		34;
		int lengthGeneralCalibrationBytes = 21;
		
		//Accel Calibration Parameters
		byte[] bufferCalibrationParameters = generateCalParamAnalogAccel();
		System.arraycopy(bufferCalibrationParameters, 0, mInfoMemBytes, idxAnalogAccelCalibration, lengthGeneralCalibrationBytes);
	}
	
	
	@Override
	public void infoMemByteArrayParse(ShimmerDevice shimmerDevice,byte[] mInfoMemBytes) {
//		int idxAnalogAccelCalibration = 31;
		//fix for newer firmware -> see InfomemLayoutShimmer3
		int idxAnalogAccelCalibration =		34;
		int lengthGeneralCalibrationBytes = 21;

		
		//Accel Calibration Parameters
		byte[] bufferCalibrationParameters = new byte[lengthGeneralCalibrationBytes];
		System.arraycopy(mInfoMemBytes, idxAnalogAccelCalibration, bufferCalibrationParameters, 0 , lengthGeneralCalibrationBytes);
		retrieveKinematicCalibrationParametersFromPacket(bufferCalibrationParameters, ACCEL_CALIBRATION_RESPONSE);	
	}


	@Override
	public Object setConfigValueUsingConfigLabel(Integer sensorMapKey, String configLabel, Object valueToSet) {
		Object returnValue = null;
		switch(configLabel){
			case(GuiLabelConfigCommon.KINEMATIC_CALIBRATION):
				if((sensorMapKey==Configuration.Shimmer3.SensorMapKey.RESERVED_ANY_SENSOR)
						||(sensorMapKey==Configuration.Shimmer3.SensorMapKey.SHIMMER_ANALOG_ACCEL)){
					TreeMap<Integer, CalibDetailsKinematic> mapOfKinematicSensorCalibration = (TreeMap<Integer, CalibDetailsKinematic>) valueToSet;
					setKinematicCalibration(mapOfKinematicSensorCalibration);
					returnValue = valueToSet;
				}
	    		break;
	        default:
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
			case(GuiLabelConfigCommon.KINEMATIC_CALIBRATION):
				if((sensorMapKey==Configuration.Shimmer3.SensorMapKey.RESERVED_ANY_SENSOR)
						||(sensorMapKey==Configuration.Shimmer3.SensorMapKey.SHIMMER_ANALOG_ACCEL)){
					returnValue = getKinematicCalibration();
				}
				break;
	        default:
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
	public void processResponse(Object obj, COMMUNICATION_TYPE commType) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void checkShimmerConfigBeforeConfiguring() {
		// TODO Auto-generated method stub
		
	}
	//--------- Abstract methods implemented end --------------


	//--------- Sensor specific methods start --------------
	private void setKinematicCalibration(TreeMap<Integer, CalibDetailsKinematic> mapOfKinematicSensorCalibration) {
		mCalibMapAccelAnalogShimmer3.putAll(mapOfKinematicSensorCalibration);
	}

	private TreeMap<Integer, TreeMap<Integer, CalibDetailsKinematic>> getKinematicCalibration() {
		TreeMap<Integer, TreeMap<Integer, CalibDetailsKinematic>> mapOfKinematicSensorCalibration = new TreeMap<Integer, TreeMap<Integer, CalibDetailsKinematic>>();
		mapOfKinematicSensorCalibration.put(Configuration.Shimmer3.SensorMapKey.SHIMMER_ANALOG_ACCEL, mCalibMapAccelAnalogShimmer3);
		return mapOfKinematicSensorCalibration;
	}
	
	private void updateCalibMapAccelLn() {
		int rangeValue = 0;
		CalibDetailsKinematic calDetails = mCalibMapAccelAnalogShimmer3.get(rangeValue);
		if(calDetails==null){
			String rangeString = "+/- 2g";
			calDetails = new CalibDetailsKinematic(rangeValue, rangeString);
		}
		calDetails.setCurrentValues(mAlignmentMatrixAnalogAccel, mSensitivityMatrixAnalogAccel, mOffsetVectorAnalogAccel);
		mCalibMapAccelAnalogShimmer3.put(rangeValue, calDetails);
	}

	private byte[] generateCalParamAnalogAccel(){
		// Analog Accel Calibration Parameters
		byte[] bufferCalibrationParameters = new byte[21];
		// offsetVector -> buffer offset = 0
		for (int i=0; i<3; i++) {
			bufferCalibrationParameters[0+(i*2)] = (byte) ((((int)mOffsetVectorAnalogAccel[i][0]) >> 8) & 0xFF);
			bufferCalibrationParameters[0+(i*2)+1] = (byte) ((((int)mOffsetVectorAnalogAccel[i][0]) >> 0) & 0xFF);
		}
		// sensitivityMatrix -> buffer offset = 6
		for (int i=0; i<3; i++) {
			bufferCalibrationParameters[6+(i*2)] = (byte) ((((int)mSensitivityMatrixAnalogAccel[i][i]) >> 8) & 0xFF);
			bufferCalibrationParameters[6+(i*2)+1] = (byte) ((((int)mSensitivityMatrixAnalogAccel[i][i]) >> 0) & 0xFF);
		}
		// alignmentMatrix -> buffer offset = 12
		for (int i=0; i<3; i++) {
			bufferCalibrationParameters[12+(i*3)] = (byte) (((int)(mAlignmentMatrixAnalogAccel[i][0]*100)) & 0xFF);
			bufferCalibrationParameters[12+(i*3)+1] = (byte) (((int)(mAlignmentMatrixAnalogAccel[i][1]*100)) & 0xFF);
			bufferCalibrationParameters[12+(i*3)+2] = (byte) (((int)(mAlignmentMatrixAnalogAccel[i][2]*100)) & 0xFF);
		}
		return bufferCalibrationParameters;
	}
	
	
	private void retrieveKinematicCalibrationParametersFromPacket(byte[] bufferCalibrationParameters,  int packetType) {
		String[] dataType={"i16","i16","i16","i16","i16","i16","i8","i8","i8","i8","i8","i8","i8","i8","i8"}; 
		int[] formattedPacket = UtilParseData.formatDataPacketReverse(bufferCalibrationParameters,dataType); // using the datatype the calibration parameters are converted
		double[] AM=new double[9];
		for (int i=0;i<9;i++){
			AM[i]=((double)formattedPacket[6+i])/100;
		}

		double[][] AlignmentMatrix = {{AM[0],AM[1],AM[2]},{AM[3],AM[4],AM[5]},{AM[6],AM[7],AM[8]}}; 				
		double[][] SensitivityMatrix = {{formattedPacket[3],0,0},{0,formattedPacket[4],0},{0,0,formattedPacket[5]}}; 
		double[][] OffsetVector = {{formattedPacket[0]},{formattedPacket[1]},{formattedPacket[2]}};
		
		//Accel Calibration Parameters
		if(packetType==ACCEL_CALIBRATION_RESPONSE && checkIfDefaultAccelCal(OffsetVector, SensitivityMatrix, AlignmentMatrix)){
			mDefaultCalibrationParametersAccel = true;
			mAlignmentMatrixAnalogAccel = AlignmentMatrix;
			mOffsetVectorAnalogAccel = OffsetVector;
			mSensitivityMatrixAnalogAccel = SensitivityMatrix;
		}
		else if (packetType==ACCEL_CALIBRATION_RESPONSE && SensitivityMatrix[0][0]!=-1) {   //used to be 65535 but changed to -1 as we are now using i16
			mDefaultCalibrationParametersAccel = false;
			mAlignmentMatrixAnalogAccel = AlignmentMatrix;
			mOffsetVectorAnalogAccel = OffsetVector;
			mSensitivityMatrixAnalogAccel = SensitivityMatrix;
		} 
		else if(packetType==ACCEL_CALIBRATION_RESPONSE && SensitivityMatrix[0][0]==-1){
			//TODO - Use Shimmer3 values or something different? 

			setDefaultCalibrationShimmer3LowNoiseAccel();
		}
		
		updateCalibMapAccelLn();
	}

	
	private void setDefaultCalibrationShimmer3LowNoiseAccel() {
		//TODO - Use Shimmer3 values or something different? 
		
		mDefaultCalibrationParametersAccel = true;
		mSensitivityMatrixAnalogAccel = UtilShimmer.deepCopyDoubleMatrix(SensitivityMatrixLowNoiseAccel2gShimmer3);
		mAlignmentMatrixAnalogAccel = UtilShimmer.deepCopyDoubleMatrix(AlignmentMatrixLowNoiseAccelShimmer3);
		mOffsetVectorAnalogAccel = UtilShimmer.deepCopyDoubleMatrix(OffsetVectorLowNoiseAccelShimmer3);	
	}


	private boolean checkIfDefaultAccelCal(double[][] offsetVectorToTest, double[][] sensitivityMatrixToTest, double[][] alignmentMatrixToTest) {
		//TODO - Use Shimmer3 defaults or something different?
	
		double[][] offsetVectorToCompare = OffsetVectorLowNoiseAccelShimmer3;
		double[][] sensitivityVectorToCompare = SensitivityMatrixLowNoiseAccel2gShimmer3;
		double[][] alignmentVectorToCompare = AlignmentMatrixLowNoiseAccelShimmer3;
		
		boolean alignmentPass = Arrays.deepEquals(alignmentVectorToCompare, alignmentMatrixToTest);
		boolean offsetPass = Arrays.deepEquals(offsetVectorToCompare, offsetVectorToTest);
		boolean sensitivityPass = Arrays.deepEquals(sensitivityVectorToCompare, sensitivityMatrixToTest);
		
		if(alignmentPass&&offsetPass&&sensitivityPass){
			return true;
		}
		return false;
	}
	
	
	public String getSensorName(){
		return mSensorName;
	}
	
	
	public boolean isUsingDefaultAccelParam(){
		return mDefaultCalibrationParametersAccel; 
	}
	
	
	//XXX returning same variable as isUsingDefaultAccelParam() -> keep one of the two methods?
	public boolean isUsingDefaultLNAccelParam(){
		return mDefaultCalibrationParametersAccel;
	}
	
	
	public double[][] getAlignmentMatrixAccel(){
		return mAlignmentMatrixAnalogAccel;
	}


	public double[][] getSensitivityMatrixAccel(){
		return mSensitivityMatrixAnalogAccel;
	}


	public double[][] getOffsetVectorMatrixAccel(){
		return mOffsetVectorAnalogAccel;
	}
	//--------- Sensor specific methods end --------------



	
	//--------- Optional methods to override in Sensor Class start --------
	
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
