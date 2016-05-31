package com.shimmerresearch.algorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.vecmath.Vector3d;

import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.FormatCluster;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.ShimmerMsg;
import com.shimmerresearch.driver.Configuration.CHANNEL_UNITS;
import com.shimmerresearch.driver.Configuration.Shimmer3;
import com.shimmerresearch.driver.ShimmerObject.BTStreamDerivedSensors;
import com.shimmerresearch.driverUtilities.ChannelDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_TYPE;

public class OrientationModule extends AbstractAlgorithm{

	private final double BETA = 1;
	private final double Q1 = 1;
	private final double Q2 = 1;
	private final double Q3 = 1;
	private final double Q4 = 1;
	
//	private double accX, accY, accZ;
//	private double magX, magY, magZ;
//	private double gyroX, gyroY, gyroZ;
	private Vector3d accValues;
	private Vector3d gyroValues;
	private Vector3d magValues;
	
//	public static final String ORIENTATION_9DOF = "9DoF Orientation"; //move to configuration??
//	public static final String ORIENTATION_6DOF = "6DoF Orientation"; //move to configuration??
	
	public static final String ORIENTATION_9DOF_LN = "LN_Acc_9DoF"; //move to configuration??
	public static final String ORIENTATION_6DOF_LN = "LN_Acc_6DoF"; //move to configuration??
	public static final String ORIENTATION_9DOF_WR = "WR_Acc_9DoF"; //move to configuration??
	public static final String ORIENTATION_6DOF_WR = "WR_Acc_6DoF"; //move to configuration??
	
	public static final String SAMPLING_RATE = "Sampling Rate";
	public static final String ACCELEROMETER = "Accelerometer";
	
//	public static AlgorithmDetails algo9DoFOrientation_LN_Acc;
//	public static AlgorithmDetails algo9DoFOrientation_WR_Acc;
//	public static AlgorithmDetails algo6DoFOrientation_LN_Acc;
//	public static AlgorithmDetails algo6DoFOrientation_WR_Acc;
	public static List<ShimmerVerObject> mListSVO = new ArrayList<ShimmerVerObject>(); 
	
	transient Object orientationAlgorithm;
	
	double sampleRate;
	String accelerometerSensor;
	ORIENTATION_ALGORTIHM algorithmType;
	
	
	public enum ORIENTATION_ALGORTIHM {
		NINE_DOF,
		SIX_DOF;
	}
	
	{
		mConfigOptionsMap.put(SAMPLING_RATE,new AlgorithmConfigOptionDetails(AlgorithmConfigOptionDetails.GUI_COMPONENT_TYPE.TEXTFIELD,mListSVO));
		String[] accSensors = new String[2];
		accSensors[0]=Shimmer3.GuiLabelSensorTiles.LOW_NOISE_ACCEL;
		accSensors[1]=Shimmer3.GuiLabelSensorTiles.WIDE_RANGE_ACCEL;
		mConfigOptionsMap.put(ACCELEROMETER, new AlgorithmConfigOptionDetails(AlgorithmConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX, mListSVO, accSensors));
		
	}
	
		
		static ChannelDetails angleADetails = new ChannelDetails(
				Shimmer3.ObjectClusterSensorName.AXIS_ANGLE_A,
				Shimmer3.ObjectClusterSensorName.AXIS_ANGLE_A,
				Shimmer3.ObjectClusterSensorName.AXIS_ANGLE_A, //database name
				CHANNEL_UNITS.LOCAL,
				Arrays.asList(CHANNEL_TYPE.CAL));
		
		static ChannelDetails angleXDetails = new ChannelDetails(
				Shimmer3.ObjectClusterSensorName.AXIS_ANGLE_X,
				Shimmer3.ObjectClusterSensorName.AXIS_ANGLE_X,
				Shimmer3.ObjectClusterSensorName.AXIS_ANGLE_X, //database name
				CHANNEL_UNITS.LOCAL,
				Arrays.asList(CHANNEL_TYPE.CAL));
		
		static ChannelDetails angleYDetails = new ChannelDetails(
				Shimmer3.ObjectClusterSensorName.AXIS_ANGLE_Y,
				Shimmer3.ObjectClusterSensorName.AXIS_ANGLE_Y,
				Shimmer3.ObjectClusterSensorName.AXIS_ANGLE_Y, //database name
				CHANNEL_UNITS.LOCAL,
				Arrays.asList(CHANNEL_TYPE.CAL));
		
		static ChannelDetails angleZDetails = new ChannelDetails(
				Shimmer3.ObjectClusterSensorName.AXIS_ANGLE_Z,
				Shimmer3.ObjectClusterSensorName.AXIS_ANGLE_Z,
				Shimmer3.ObjectClusterSensorName.AXIS_ANGLE_Z, //database name
				CHANNEL_UNITS.LOCAL,
				Arrays.asList(CHANNEL_TYPE.CAL));
		
		static ChannelDetails quatWDetails = new ChannelDetails(
				Shimmer3.ObjectClusterSensorName.QUAT_MADGE_9DOF_W,
				Shimmer3.ObjectClusterSensorName.QUAT_MADGE_9DOF_W,
				Shimmer3.ObjectClusterSensorName.QUAT_MADGE_9DOF_W, //database name
				CHANNEL_UNITS.LOCAL,
				Arrays.asList(CHANNEL_TYPE.CAL));
		
		static ChannelDetails quatXDetails = new ChannelDetails(
				Shimmer3.ObjectClusterSensorName.QUAT_MADGE_9DOF_X,
				Shimmer3.ObjectClusterSensorName.QUAT_MADGE_9DOF_X,
				Shimmer3.ObjectClusterSensorName.QUAT_MADGE_9DOF_X, //database name
				CHANNEL_UNITS.LOCAL,
				Arrays.asList(CHANNEL_TYPE.CAL));
		
		static ChannelDetails quatYDetails = new ChannelDetails(
				Shimmer3.ObjectClusterSensorName.QUAT_MADGE_9DOF_Y,
				Shimmer3.ObjectClusterSensorName.QUAT_MADGE_9DOF_Y,
				Shimmer3.ObjectClusterSensorName.QUAT_MADGE_9DOF_Y, //database name
				CHANNEL_UNITS.LOCAL,
				Arrays.asList(CHANNEL_TYPE.CAL));
		
		static ChannelDetails quatZDetails = new ChannelDetails(
				Shimmer3.ObjectClusterSensorName.QUAT_MADGE_9DOF_Z,
				Shimmer3.ObjectClusterSensorName.QUAT_MADGE_9DOF_Z,
				Shimmer3.ObjectClusterSensorName.QUAT_MADGE_9DOF_Z, //database name
				CHANNEL_UNITS.LOCAL,
				Arrays.asList(CHANNEL_TYPE.CAL));
		
		static List<ChannelDetails> listChannels = Arrays.asList(
				angleADetails, angleXDetails, angleYDetails, angleZDetails,
				quatWDetails, quatXDetails, quatYDetails, quatZDetails);
		
		public static final AlgorithmDetails algo9DoFOrientation_LN_Acc = new AlgorithmDetails(ORIENTATION_9DOF_LN, 
				listChannels, ORIENTATION_9DOF_LN, 
				Arrays.asList(Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_LN_X,
						Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_LN_Y,
						Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_LN_Z,
						Configuration.Shimmer3.ObjectClusterSensorName.MAG_X,
						Configuration.Shimmer3.ObjectClusterSensorName.MAG_Y,
						Configuration.Shimmer3.ObjectClusterSensorName.MAG_Y,
						Configuration.Shimmer3.ObjectClusterSensorName.GYRO_X,
						Configuration.Shimmer3.ObjectClusterSensorName.GYRO_Y,
						Configuration.Shimmer3.ObjectClusterSensorName.GYRO_Z),
						Configuration.Shimmer3.GuiLabelAlgorithmGrouping.ORIENTATION_9DOF,
						BTStreamDerivedSensors.ORIENTATION_9DOF, 
						Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_A_ACCEL,
								Configuration.Shimmer3.SensorMapKey.SHIMMER_LSM303DLHC_MAG,
								Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_GYRO)
						,CHANNEL_UNITS.LOCAL);
		
		public static final AlgorithmDetails algo9DoFOrientation_WR_Acc = new AlgorithmDetails(ORIENTATION_9DOF_WR, 
				listChannels, ORIENTATION_9DOF_WR, 
				Arrays.asList(Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_WR_X,
						Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_WR_Y,
						Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_WR_Z,
						Configuration.Shimmer3.ObjectClusterSensorName.MAG_X,
						Configuration.Shimmer3.ObjectClusterSensorName.MAG_Y,
						Configuration.Shimmer3.ObjectClusterSensorName.MAG_Y,
						Configuration.Shimmer3.ObjectClusterSensorName.GYRO_X,
						Configuration.Shimmer3.ObjectClusterSensorName.GYRO_Y,
						Configuration.Shimmer3.ObjectClusterSensorName.GYRO_Z),
						Configuration.Shimmer3.GuiLabelAlgorithmGrouping.ORIENTATION_9DOF,
						BTStreamDerivedSensors.ORIENTATION_9DOF, 
						Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_LSM303DLHC_ACCEL,
								Configuration.Shimmer3.SensorMapKey.SHIMMER_LSM303DLHC_MAG,
								Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_GYRO)
						,CHANNEL_UNITS.LOCAL);
		
		public static final AlgorithmDetails algo6DoFOrientation_LN_Acc = new AlgorithmDetails(ORIENTATION_6DOF_LN, 
				listChannels, ORIENTATION_6DOF_LN, 
				Arrays.asList(Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_LN_X,
						Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_LN_Y,
						Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_LN_Z,
						Configuration.Shimmer3.ObjectClusterSensorName.GYRO_X,
						Configuration.Shimmer3.ObjectClusterSensorName.GYRO_Y,
						Configuration.Shimmer3.ObjectClusterSensorName.GYRO_Z),
						Configuration.Shimmer3.GuiLabelAlgorithmGrouping.ORIENTATION_6DOF,
						BTStreamDerivedSensors.ORIENTATION_6DOF, 
						Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_A_ACCEL,
								Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_GYRO)
						,CHANNEL_UNITS.LOCAL);
		
		public static final AlgorithmDetails algo6DoFOrientation_WR_Acc = new AlgorithmDetails(ORIENTATION_6DOF_WR, 
				listChannels, ORIENTATION_6DOF_WR, 
				Arrays.asList(Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_WR_X,
						Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_WR_Y,
						Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_WR_Z,
						Configuration.Shimmer3.ObjectClusterSensorName.GYRO_X,
						Configuration.Shimmer3.ObjectClusterSensorName.GYRO_Y,
						Configuration.Shimmer3.ObjectClusterSensorName.GYRO_Z),
						Configuration.Shimmer3.GuiLabelAlgorithmGrouping.ORIENTATION_6DOF,
						BTStreamDerivedSensors.ORIENTATION_6DOF, 
						Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_LSM303DLHC_ACCEL,
								Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_GYRO)
						,CHANNEL_UNITS.LOCAL);
		
	
	public static final Map<String, AlgorithmDetails> mAlgorithmMapRef;
    static {
        Map<String, AlgorithmDetails> aMap = new LinkedHashMap<String, AlgorithmDetails>();
        aMap.put(algo9DoFOrientation_LN_Acc.mAlgorithmName, algo9DoFOrientation_LN_Acc);
        aMap.put(algo9DoFOrientation_WR_Acc.mAlgorithmName, algo9DoFOrientation_WR_Acc);
        aMap.put(algo6DoFOrientation_LN_Acc.mAlgorithmName, algo6DoFOrientation_LN_Acc);
        aMap.put(algo6DoFOrientation_WR_Acc.mAlgorithmName, algo6DoFOrientation_WR_Acc);
		mAlgorithmMapRef = Collections.unmodifiableMap(aMap);
    }
	
	
//	{
//		mAlgorithmName = "ECG to HR Algorithm";
//		mSignalName = new String[1]; // an array because you might use multiple signals for an algorithm, note for now only single signal supported but this should be fwd compatible
//		mSignalFormat = new String[1];
//		mSignalOutputNameArray = new String[1];
//		mSignalOutputNameArray[0] = ""; //temp value
//		mSignalOutputFormatArray = new String[1];
//		mSignalOutputFormatArray[0] = CHANNEL_TYPE.CAL.toString();
//		mSignalOutputUnitArray = new String[1];
//		mSignalOutputUnitArray[0] = CHANNEL_UNITS.NO_UNITS;
//		
//		mFilteringOptions = FILTERING_OPTION.NONE;
//	}
	
	public OrientationModule(AlgorithmDetails algorithmDetails, double samplingRate) {
		mAlgorithmDetails = algorithmDetails;
		mAlgorithmType = ALGORITHM_TYPE.ALGORITHM_TYPE_CONTINUOUS;
		mAlgorithmResultType = ALGORITHM_RESULT_TYPE.ALGORITHM_RESULT_TYPE_SINGLE_OBJECT_CLUSTER;
		mAlgorithmName = algorithmDetails.mAlgorithmName;
		mAlgorithmGroupingName = algorithmDetails.mAlgorithmName;
//		setSignalFormat(CHANNEL_TYPE.CAL.toString());
		
		this.sampleRate = samplingRate;
		try {
			initialize();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public Object getSettings(String componentName) {
		Object returnValue = null;
		switch(componentName){

			case(SAMPLING_RATE):
				returnValue = getSamplingRate();
				break;
			case(ACCELEROMETER):
				returnValue = getAccelerometer();
				break;
		}
		return returnValue;
	}

	@Override
	public Object getDefaultSettings(String componentName) {
		Object returnValue = null;
		switch(componentName){
			case(SAMPLING_RATE):
				returnValue = 512;
				break;
			case(ACCELEROMETER):
				returnValue = Shimmer3.GuiLabelSensorTiles.LOW_NOISE_ACCEL;
				break;
		}
		return returnValue;
	}

	@Override
	public void setSettings(String componentName, Object valueToSet)
			throws Exception {
		
		switch(componentName){
			case(SAMPLING_RATE):
				setSamplingRate(Double.parseDouble((String) valueToSet));
				break;
			case(ACCELEROMETER):
				setAccelerometer((String) valueToSet);
				break;
		}
	}

	@Override
	public AlgorithmResultObject processDataRealTime(ObjectCluster object)
			throws Exception {

		for(String associatedChannel:mAlgorithmDetails.mListOfAssociatedSensors){
			Collection<FormatCluster> dataFormatsSignal = object.getCollectionOfFormatClusters(associatedChannel);  // first retrieve all the possible formats for the current sensor device
			if(dataFormatsSignal!=null){
				FormatCluster formatClusterSignal = ((FormatCluster)ObjectCluster.returnFormatCluster(dataFormatsSignal,mAlgorithmDetails.mChannelType.toString())); // retrieve the calibrated data
				if(formatClusterSignal!=null){
					setChannelValue(associatedChannel, formatClusterSignal.mData);
				}
				else{
					return null;
				}
			}
			else{
				return null;
			}
		}
		
		Orientation3DObject orientationObject = applyOrientationAlgorithm();
		object = addQuaternionToObjectCluster(orientationObject, object);
		
		AlgorithmResultObject aro = new AlgorithmResultObject(mAlgorithmResultType, object, mTrialName);
		return aro;						
	}
	
	private void setChannelValue(String channelName, double value){
		
		if(channelName.equals(Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_WR_X) ||
				channelName.equals(Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_LN_X)){
			accValues.x = value;
		}
		else if(channelName.equals(Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_WR_Y) ||
				channelName.equals(Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_LN_Y)){
			accValues.y = value;
		}
		else if(channelName.equals(Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_WR_Z) ||
				channelName.equals(Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_LN_Z)){
			accValues.z = value;
		}
		else if(channelName.equals(Configuration.Shimmer3.ObjectClusterSensorName.MAG_X)){
			magValues.x = value;
		}
		else if(channelName.equals(Configuration.Shimmer3.ObjectClusterSensorName.MAG_Y)){
			magValues.y = value;
		}
		else if(channelName.equals(Configuration.Shimmer3.ObjectClusterSensorName.MAG_Z)){
			magValues.z = value;
		}
		else if(channelName.equals(Configuration.Shimmer3.ObjectClusterSensorName.GYRO_X)){
			//if shimmer 2 or 3, apply a tweack to the gyro value
			gyroValues.x = value*(Math.PI/180.0);
		}
		else if(channelName.equals(Configuration.Shimmer3.ObjectClusterSensorName.GYRO_Y)){
			//if shimmer 2 or 3, apply a tweack to the gyro value
			gyroValues.y = value*(Math.PI/180.0);
		}
		else if(channelName.equals(Configuration.Shimmer3.ObjectClusterSensorName.GYRO_Z)){
			//if shimmer 2 or 3, apply a tweack to the gyro value
			gyroValues.z = value*(Math.PI/180.0);
		}
	}	
	
	private ObjectCluster addQuaternionToObjectCluster(Orientation3DObject quaternion, ObjectCluster objectCluster){
		
		objectCluster.addData(Shimmer3.ObjectClusterSensorName.AXIS_ANGLE_A,CHANNEL_TYPE.CAL,CHANNEL_UNITS.LOCAL,quaternion.getTheta());
		objectCluster.addData(Shimmer3.ObjectClusterSensorName.AXIS_ANGLE_X,CHANNEL_TYPE.CAL,CHANNEL_UNITS.LOCAL,quaternion.getAngleX());
		objectCluster.addData(Shimmer3.ObjectClusterSensorName.AXIS_ANGLE_Y,CHANNEL_TYPE.CAL,CHANNEL_UNITS.LOCAL,quaternion.getAngleY());
		objectCluster.addData(Shimmer3.ObjectClusterSensorName.AXIS_ANGLE_Z,CHANNEL_TYPE.CAL,CHANNEL_UNITS.LOCAL,quaternion.getAngleZ());
		objectCluster.addData(Shimmer3.ObjectClusterSensorName.QUAT_MADGE_9DOF_W,CHANNEL_TYPE.CAL,CHANNEL_UNITS.LOCAL,quaternion.getQuaternionW());
		objectCluster.addData(Shimmer3.ObjectClusterSensorName.QUAT_MADGE_9DOF_X,CHANNEL_TYPE.CAL,CHANNEL_UNITS.LOCAL,quaternion.getQuaternionX());
		objectCluster.addData(Shimmer3.ObjectClusterSensorName.QUAT_MADGE_9DOF_Y,CHANNEL_TYPE.CAL,CHANNEL_UNITS.LOCAL,quaternion.getQuaternionY());
		objectCluster.addData(Shimmer3.ObjectClusterSensorName.QUAT_MADGE_9DOF_Z,CHANNEL_TYPE.CAL,CHANNEL_UNITS.LOCAL,quaternion.getQuaternionZ());
		
//		String[] sensorNames = objectCluster.mSensorNames;
//		if(objectCluster.mSensorNames!=null){
//			objectCluster.mSensorNames = new String[sensorNames.length + mSignalOutputNameArray.length];
//			System.arraycopy(sensorNames, 0,objectCluster.mSensorNames, 0, sensorNames.length);
//			System.arraycopy(mSignalOutputNameArray, 0,objectCluster.mSensorNames, sensorNames.length, mSignalOutputNameArray.length);

			// add uncal data
//			double[] uncalData = objectCluster.mUncalData;
//			objectCluster.mUncalData = new double[uncalData.length + mSignalOutputNameArray.length];
//			System.arraycopy(uncalData, 0,objectCluster.mUncalData, 0, uncalData.length);
//			double[] temp = new double[mSignalOutputNameArray.length];
//			Arrays.fill(temp, Double.NaN);
//			System.arraycopy(temp, 0, objectCluster.mUncalData, uncalData.length, temp.length);
//			objectCluster.mUncalData[objectCluster.mUncalData.length-1] = Double.NaN;
//
//			// add calibrated data
//			double[] calData = objectCluster.mCalData;
//			objectCluster.mCalData = new double[calData.length + mSignalOutputNameArray.length];
//			System.arraycopy(calData, 0,objectCluster.mCalData, 0, calData.length);
//			objectCluster.mCalData[calData.length] = (double)hr;
//		}
		
		return objectCluster;
	}
	
	private Orientation3DObject applyOrientationAlgorithm(){
		
		Orientation3DObject quaternion;
		if(algorithmType == ORIENTATION_ALGORTIHM.NINE_DOF){
			quaternion = ((GradDes3DOrientation) orientationAlgorithm).update(
					accValues.x, accValues.y, accValues.z, 
					gyroValues.x, gyroValues.y, gyroValues.z
					,magValues.x, magValues.y, magValues.z);
		}
		else{
			quaternion = ((GradDes3DOrientation6DoF) orientationAlgorithm).update(
					accValues.x, accValues.y, accValues.z, 
					gyroValues.x, gyroValues.y, gyroValues.z);
		}
		
		return quaternion;
	}

	@Override
	public AlgorithmResultObject processDataPostCapture(Object object)
			throws Exception {
		throw new Error(
				"Method: Object processDataPostCapture(List<?> objectList) is not valid for Orientation9DoF Module. Use: Object processDataRealTime(Object object).");
	}

	@Override
	public void reset() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void initialize() throws Exception {
		
		double samplingPeriod = 1/sampleRate;
		
		if(mAlgorithmName.contains("9")){
			algorithmType = ORIENTATION_ALGORTIHM.NINE_DOF;
			orientationAlgorithm = new GradDes3DOrientation(BETA, samplingPeriod, Q1, Q2, Q3, Q4);
			if(mAlgorithmName.contains("LN")){
				accelerometerSensor = Shimmer3.GuiLabelSensorTiles.LOW_NOISE_ACCEL;
			}
			else{
				accelerometerSensor = Shimmer3.GuiLabelSensorTiles.WIDE_RANGE_ACCEL;
			}
		}
		else{
			algorithmType = ORIENTATION_ALGORTIHM.SIX_DOF;
			orientationAlgorithm = new GradDes3DOrientation6DoF(BETA, samplingPeriod, Q1, Q2, Q3, Q4);
			if(mAlgorithmName.contains("LN")){
				accelerometerSensor = Shimmer3.GuiLabelSensorTiles.LOW_NOISE_ACCEL;
			}
			else{
				accelerometerSensor = Shimmer3.GuiLabelSensorTiles.WIDE_RANGE_ACCEL;
			}
		}
	}

	@Override
	public String printBatchMetrics() {
		return null;
	}

	@Override
	public void eventDataReceived(ShimmerMsg shimmerMSG) {
		
	}
	
	public double getSamplingRate(){
		return sampleRate;
	}
	
	public String getAccelerometer(){
		return accelerometerSensor;
	}
	
	public void setSamplingRate(double sampleRate){
		this.sampleRate = sampleRate;
	}
	
	public void setAccelerometer(String accelerometerName){
		this.accelerometerSensor = accelerometerName;
	}
	
//	public ORIENTATION_ALGORTIHM getAlgorithmType() {
//		return algorithmType;
//	}

	public void setAlgorithmType(ORIENTATION_ALGORTIHM algorithmType) {
		this.algorithmType = algorithmType;
	}
	
	public static LinkedHashMap<String, AlgorithmDetails> getMapOfSupportedAlgorithms(ShimmerVerObject mShimmerVerObject) {
		LinkedHashMap<String, AlgorithmDetails> mapOfSupportedAlgorithms = new LinkedHashMap<String, AlgorithmDetails>();
		//TODO Filter here depending on Shimmer version
		mapOfSupportedAlgorithms.putAll(mAlgorithmMapRef);
		return mapOfSupportedAlgorithms;
	}

}
