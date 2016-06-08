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
import com.shimmerresearch.driver.ShimmerObject.DerivedSensorsBitMask;
import com.shimmerresearch.driverUtilities.ChannelDetails;
import com.shimmerresearch.driverUtilities.SensorGroupingDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_TYPE;
import com.shimmerresearch.driverUtilities.ConfigOptionDetails.GUI_COMPONENT_TYPE;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.HW_ID;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.HW_ID_SR_CODES;

public class OrientationModule extends AbstractAlgorithm{

	/** * */
	private static final long serialVersionUID = -4174847826978293223L;
	
	private final double BETA = 1;
	private final double Q1 = 1;
	private final double Q2 = 1;
	private final double Q3 = 1;
	private final double Q4 = 1;
	
	private Vector3d accValues;
	private Vector3d gyroValues;
	private Vector3d magValues;
	
	private static final String[] QUATERNION_OPTIONS = {"Quaternion Off", "Quaternion On"};
//	private Integer[] QUATERNION_OPTIONS_VALUES = {1, 0};
	private static final String[] EULER_OPTIONS = {"Euler Off", "Euler On"};
//	private Integer[] EULER_OPTIONS_VALUES = {1, 0};
	
	private static final ShimmerVerObject baseSh3Module = new ShimmerVerObject(
			HW_ID.SHIMMER_3,ShimmerVerDetails.ANY_VERSION,
			ShimmerVerDetails.ANY_VERSION,
			ShimmerVerDetails.ANY_VERSION,
			ShimmerVerDetails.ANY_VERSION,
			HW_ID_SR_CODES.EXP_BRD_EXG);
	
	//TODO update objectcluster name with previously aggreed name
	public static class AlgorithmName{
		public static final String ORIENTATION_9DOF_LN = "LN_Acc_9DoF";
		public static final String ORIENTATION_6DOF_LN = "LN_Acc_6DoF";
		public static final String ORIENTATION_9DOF_WR = "WR_Acc_9DoF";
		public static final String ORIENTATION_6DOF_WR = "WR_Acc_6DoF";
	}
	
	public static final String SAMPLING_RATE = "Sampling Rate";
	public static final String ACCELEROMETER = "Accelerometer";
	public static final String QUATERNION_OUTPUT = "QuaternionOutput";
	public static final String EULER_OUTPUT = "EulerOutput";
	
	public static List<ShimmerVerObject> mListSVO = new ArrayList<ShimmerVerObject>(); 
	
	
	transient Object orientationAlgorithm;
	
	double sampleRate;
	String accelerometerSensor;
	boolean quaternionOutput;
	boolean eulerOutput;
	ORIENTATION_TYPE orientationType;
	
	public enum ORIENTATION_TYPE {
		NINE_DOF,
		SIX_DOF;
	}
	
	
	static ChannelDetails channelAngleA = new ChannelDetails(
			Shimmer3.ObjectClusterSensorName.AXIS_ANGLE_A,
			Shimmer3.ObjectClusterSensorName.AXIS_ANGLE_A,
			Shimmer3.ObjectClusterSensorName.AXIS_ANGLE_A, //database name
			CHANNEL_UNITS.LOCAL,
			Arrays.asList(CHANNEL_TYPE.CAL));
	
	static ChannelDetails channelAngleX = new ChannelDetails(
			Shimmer3.ObjectClusterSensorName.AXIS_ANGLE_X,
			Shimmer3.ObjectClusterSensorName.AXIS_ANGLE_X,
			Shimmer3.ObjectClusterSensorName.AXIS_ANGLE_X, //database name
			CHANNEL_UNITS.LOCAL,
			Arrays.asList(CHANNEL_TYPE.CAL));
	
	static ChannelDetails channelAngleY = new ChannelDetails(
			Shimmer3.ObjectClusterSensorName.AXIS_ANGLE_Y,
			Shimmer3.ObjectClusterSensorName.AXIS_ANGLE_Y,
			Shimmer3.ObjectClusterSensorName.AXIS_ANGLE_Y, //database name
			CHANNEL_UNITS.LOCAL,
			Arrays.asList(CHANNEL_TYPE.CAL));
	
	static ChannelDetails channelAngleZ = new ChannelDetails(
			Shimmer3.ObjectClusterSensorName.AXIS_ANGLE_Z,
			Shimmer3.ObjectClusterSensorName.AXIS_ANGLE_Z,
			Shimmer3.ObjectClusterSensorName.AXIS_ANGLE_Z, //database name
			CHANNEL_UNITS.LOCAL,
			Arrays.asList(CHANNEL_TYPE.CAL));
	
	static ChannelDetails channelQuatW = new ChannelDetails(
			Shimmer3.ObjectClusterSensorName.QUAT_MADGE_9DOF_W,
			Shimmer3.ObjectClusterSensorName.QUAT_MADGE_9DOF_W,
			Shimmer3.ObjectClusterSensorName.QUAT_MADGE_9DOF_W, //database name
			CHANNEL_UNITS.LOCAL,
			Arrays.asList(CHANNEL_TYPE.CAL));
	
	static ChannelDetails channelQuatX = new ChannelDetails(
			Shimmer3.ObjectClusterSensorName.QUAT_MADGE_9DOF_X,
			Shimmer3.ObjectClusterSensorName.QUAT_MADGE_9DOF_X,
			Shimmer3.ObjectClusterSensorName.QUAT_MADGE_9DOF_X, //database name
			CHANNEL_UNITS.LOCAL,
			Arrays.asList(CHANNEL_TYPE.CAL));
	
	static ChannelDetails channelQuatY = new ChannelDetails(
			Shimmer3.ObjectClusterSensorName.QUAT_MADGE_9DOF_Y,
			Shimmer3.ObjectClusterSensorName.QUAT_MADGE_9DOF_Y,
			Shimmer3.ObjectClusterSensorName.QUAT_MADGE_9DOF_Y, //database name
			CHANNEL_UNITS.LOCAL,
			Arrays.asList(CHANNEL_TYPE.CAL));
	
	static ChannelDetails channelQuatZ = new ChannelDetails(
			Shimmer3.ObjectClusterSensorName.QUAT_MADGE_9DOF_Z,
			Shimmer3.ObjectClusterSensorName.QUAT_MADGE_9DOF_Z,
			Shimmer3.ObjectClusterSensorName.QUAT_MADGE_9DOF_Z, //database name
			CHANNEL_UNITS.LOCAL,
			Arrays.asList(CHANNEL_TYPE.CAL));
	
	static List<ChannelDetails> listChannels = Arrays.asList(
			channelAngleA, channelAngleX, channelAngleY, channelAngleZ,
			channelQuatW, channelQuatX, channelQuatY, channelQuatZ);
	
	public static final AlgorithmDetails algo9DoFOrientation_LN_Acc = new AlgorithmDetails(
			AlgorithmName.ORIENTATION_9DOF_LN, 
			AlgorithmName.ORIENTATION_9DOF_LN, 
			Arrays.asList(Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_LN_X,
					Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_LN_Y,
					Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_LN_Z,
					Configuration.Shimmer3.ObjectClusterSensorName.MAG_X,
					Configuration.Shimmer3.ObjectClusterSensorName.MAG_Y,
					Configuration.Shimmer3.ObjectClusterSensorName.MAG_Y,
					Configuration.Shimmer3.ObjectClusterSensorName.GYRO_X,
					Configuration.Shimmer3.ObjectClusterSensorName.GYRO_Y,
					Configuration.Shimmer3.ObjectClusterSensorName.GYRO_Z),
//					Configuration.Shimmer3.GuiLabelAlgorithmGrouping.ORIENTATION_9DOF.getTileText(),
			(DerivedSensorsBitMask.ORIENTATION_9DOF_LN_QUAT|DerivedSensorsBitMask.ORIENTATION_9DOF_LN_EULER), 
			Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_A_ACCEL,
					Configuration.Shimmer3.SensorMapKey.SHIMMER_LSM303DLHC_MAG,
					Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_GYRO),
			CHANNEL_UNITS.LOCAL,
			listChannels);
	
	public static final AlgorithmDetails algo9DoFOrientation_WR_Acc = new AlgorithmDetails(
			AlgorithmName.ORIENTATION_9DOF_WR, 
			AlgorithmName.ORIENTATION_9DOF_WR, 
			Arrays.asList(Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_WR_X,
					Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_WR_Y,
					Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_WR_Z,
					Configuration.Shimmer3.ObjectClusterSensorName.MAG_X,
					Configuration.Shimmer3.ObjectClusterSensorName.MAG_Y,
					Configuration.Shimmer3.ObjectClusterSensorName.MAG_Y,
					Configuration.Shimmer3.ObjectClusterSensorName.GYRO_X,
					Configuration.Shimmer3.ObjectClusterSensorName.GYRO_Y,
					Configuration.Shimmer3.ObjectClusterSensorName.GYRO_Z),
//					Configuration.Shimmer3.GuiLabelAlgorithmGrouping.ORIENTATION_9DOF.getTileText(),
			(DerivedSensorsBitMask.ORIENTATION_9DOF_WR_QUAT|DerivedSensorsBitMask.ORIENTATION_9DOF_WR_EULER), 
			Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_LSM303DLHC_ACCEL,
					Configuration.Shimmer3.SensorMapKey.SHIMMER_LSM303DLHC_MAG,
					Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_GYRO),
			CHANNEL_UNITS.LOCAL,
			listChannels);
	
	public static final AlgorithmDetails algo6DoFOrientation_LN_Acc = new AlgorithmDetails(
			AlgorithmName.ORIENTATION_6DOF_LN, 
			AlgorithmName.ORIENTATION_6DOF_LN, 
			Arrays.asList(Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_LN_X,
					Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_LN_Y,
					Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_LN_Z,
					Configuration.Shimmer3.ObjectClusterSensorName.GYRO_X,
					Configuration.Shimmer3.ObjectClusterSensorName.GYRO_Y,
					Configuration.Shimmer3.ObjectClusterSensorName.GYRO_Z),
//					Configuration.Shimmer3.GuiLabelAlgorithmGrouping.ORIENTATION_6DOF.getTileText(),
			(DerivedSensorsBitMask.ORIENTATION_6DOF_LN_QUAT|DerivedSensorsBitMask.ORIENTATION_6DOF_LN_EULER), 
			Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_A_ACCEL,
					Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_GYRO),
			CHANNEL_UNITS.LOCAL,
			listChannels);
	
	public static final AlgorithmDetails algo6DoFOrientation_WR_Acc = new AlgorithmDetails(
			AlgorithmName.ORIENTATION_6DOF_WR, 
			AlgorithmName.ORIENTATION_6DOF_WR, 
			Arrays.asList(Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_WR_X,
					Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_WR_Y,
					Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_WR_Z,
					Configuration.Shimmer3.ObjectClusterSensorName.GYRO_X,
					Configuration.Shimmer3.ObjectClusterSensorName.GYRO_Y,
					Configuration.Shimmer3.ObjectClusterSensorName.GYRO_Z),
//					Configuration.Shimmer3.GuiLabelAlgorithmGrouping.ORIENTATION_6DOF.getTileText(),
			(DerivedSensorsBitMask.ORIENTATION_6DOF_WR_QUAT|DerivedSensorsBitMask.ORIENTATION_6DOF_WR_EULER), 
			Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_LSM303DLHC_ACCEL,
					Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_GYRO),
			CHANNEL_UNITS.LOCAL,
			listChannels);
		
	
	public static final Map<String, AlgorithmDetails> mAlgorithmMapRef;
    static {
        Map<String, AlgorithmDetails> aMap = new LinkedHashMap<String, AlgorithmDetails>();
        aMap.put(algo9DoFOrientation_LN_Acc.mAlgorithmName, algo9DoFOrientation_LN_Acc);
        aMap.put(algo9DoFOrientation_WR_Acc.mAlgorithmName, algo9DoFOrientation_WR_Acc);
        aMap.put(algo6DoFOrientation_LN_Acc.mAlgorithmName, algo6DoFOrientation_LN_Acc);
        aMap.put(algo6DoFOrientation_WR_Acc.mAlgorithmName, algo6DoFOrientation_WR_Acc);
		mAlgorithmMapRef = Collections.unmodifiableMap(aMap);
    }
    
	// ------------------- Algorithms grouping map start -----------------------
	private static final SensorGroupingDetails sGD9Dof = new SensorGroupingDetails(
			Configuration.Shimmer3.GuiLabelAlgorithmGrouping.ORIENTATION_9DOF.getTileText(), 
			Arrays.asList(OrientationModule.algo9DoFOrientation_LN_Acc,
					OrientationModule.algo9DoFOrientation_WR_Acc),
			Arrays.asList(OrientationModule.QUATERNION_OUTPUT,
					OrientationModule.EULER_OUTPUT),
			0);
	public static final SensorGroupingDetails sGD6Dof = new SensorGroupingDetails(
			Configuration.Shimmer3.GuiLabelAlgorithmGrouping.ORIENTATION_6DOF.getTileText(), 
			Arrays.asList(OrientationModule.algo6DoFOrientation_LN_Acc,
					OrientationModule.algo6DoFOrientation_WR_Acc),
			Arrays.asList(OrientationModule.QUATERNION_OUTPUT,
					OrientationModule.EULER_OUTPUT),
			0);
	// ------------------- Algorithms grouping map end -----------------------

	
	public static final ConfigOptionDetailsAlgorithm configOptionQuatOutput = new ConfigOptionDetailsAlgorithm(
			QUATERNION_OPTIONS, 
			GUI_COMPONENT_TYPE.COMBOBOX,
			null);

	public static final ConfigOptionDetailsAlgorithm configOptionEulerOutput = new ConfigOptionDetailsAlgorithm(
			EULER_OPTIONS, 
			GUI_COMPONENT_TYPE.COMBOBOX,
			null);

	{
		mListSVO.add(baseSh3Module);
		
//		mConfigOptionsMap.put(SAMPLING_RATE,new AlgorithmConfigOptionDetails(AlgorithmConfigOptionDetails.GUI_COMPONENT_TYPE.TEXTFIELD,mListSVO));
//		String[] accSensors = new String[2];
//		accSensors[0]=Shimmer3.GuiLabelSensorTiles.LOW_NOISE_ACCEL;
//		accSensors[1]=Shimmer3.GuiLabelSensorTiles.WIDE_RANGE_ACCEL;
//		mConfigOptionsMap.put(ACCELEROMETER, new AlgorithmConfigOptionDetails(AlgorithmConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX, mListSVO, accSensors));
		
		mConfigOptionsMap.put(QUATERNION_OUTPUT, configOptionQuatOutput);
		mConfigOptionsMap.put(EULER_OUTPUT, configOptionEulerOutput);
		
		mAlgorithmGroupingMap.put(Configuration.Shimmer3.GuiLabelAlgorithmGrouping.ORIENTATION_9DOF.ordinal(), sGD9Dof);
		mAlgorithmGroupingMap.put(Configuration.Shimmer3.GuiLabelAlgorithmGrouping.ORIENTATION_6DOF.ordinal(), sGD6Dof);

	}
	
	public OrientationModule(AlgorithmDetails algorithmDetails, double samplingRate) {
		mAlgorithmDetails = algorithmDetails;
		mAlgorithmType = ALGORITHM_TYPE.ALGORITHM_TYPE_CONTINUOUS;
		mAlgorithmResultType = ALGORITHM_RESULT_TYPE.ALGORITHM_RESULT_TYPE_SINGLE_OBJECT_CLUSTER;
		mAlgorithmName = algorithmDetails.mAlgorithmName;
		mAlgorithmGroupingName = algorithmDetails.mAlgorithmName;
		
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
			case(QUATERNION_OUTPUT):
				returnValue = isQuaternionOutput();
			break;
			case(EULER_OUTPUT):
				returnValue = isEulerOutput();
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
			case(QUATERNION_OUTPUT):
				returnValue = true;
			break;
			case(EULER_OUTPUT):
				returnValue = false;
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
			case(QUATERNION_OUTPUT):
				setQuaternionOutput((boolean) valueToSet);;
			break;
			case(EULER_OUTPUT):
				setEulerOutput((boolean) valueToSet);
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
			//if shimmer 2 or 3, apply a tweak to the gyro value
			gyroValues.x = value*(Math.PI/180.0);
		}
		else if(channelName.equals(Configuration.Shimmer3.ObjectClusterSensorName.GYRO_Y)){
			//if shimmer 2 or 3, apply a tweak to the gyro value
			gyroValues.y = value*(Math.PI/180.0);
		}
		else if(channelName.equals(Configuration.Shimmer3.ObjectClusterSensorName.GYRO_Z)){
			//if shimmer 2 or 3, apply a tweak to the gyro value
			gyroValues.z = value*(Math.PI/180.0);
		}
	}	
	
	private ObjectCluster addQuaternionToObjectCluster(Orientation3DObject quaternion, ObjectCluster objectCluster){
		
//		if(algorithmOutput == ORIENTATION_OUTPUT.BOTH){
//			objectCluster.addData(Shimmer3.ObjectClusterSensorName.AXIS_ANGLE_A,CHANNEL_TYPE.CAL,CHANNEL_UNITS.LOCAL,quaternion.getTheta());
//			objectCluster.addData(Shimmer3.ObjectClusterSensorName.AXIS_ANGLE_X,CHANNEL_TYPE.CAL,CHANNEL_UNITS.LOCAL,quaternion.getAngleX());
//			objectCluster.addData(Shimmer3.ObjectClusterSensorName.AXIS_ANGLE_Y,CHANNEL_TYPE.CAL,CHANNEL_UNITS.LOCAL,quaternion.getAngleY());
//			objectCluster.addData(Shimmer3.ObjectClusterSensorName.AXIS_ANGLE_Z,CHANNEL_TYPE.CAL,CHANNEL_UNITS.LOCAL,quaternion.getAngleZ());
//			objectCluster.addData(Shimmer3.ObjectClusterSensorName.QUAT_MADGE_9DOF_W,CHANNEL_TYPE.CAL,CHANNEL_UNITS.LOCAL,quaternion.getQuaternionW());
//			objectCluster.addData(Shimmer3.ObjectClusterSensorName.QUAT_MADGE_9DOF_X,CHANNEL_TYPE.CAL,CHANNEL_UNITS.LOCAL,quaternion.getQuaternionX());
//			objectCluster.addData(Shimmer3.ObjectClusterSensorName.QUAT_MADGE_9DOF_Y,CHANNEL_TYPE.CAL,CHANNEL_UNITS.LOCAL,quaternion.getQuaternionY());
//			objectCluster.addData(Shimmer3.ObjectClusterSensorName.QUAT_MADGE_9DOF_Z,CHANNEL_TYPE.CAL,CHANNEL_UNITS.LOCAL,quaternion.getQuaternionZ());
//		}
		if(eulerOutput){
			objectCluster.addData(Shimmer3.ObjectClusterSensorName.AXIS_ANGLE_A,CHANNEL_TYPE.CAL,CHANNEL_UNITS.LOCAL,quaternion.getTheta());
			objectCluster.addData(Shimmer3.ObjectClusterSensorName.AXIS_ANGLE_X,CHANNEL_TYPE.CAL,CHANNEL_UNITS.LOCAL,quaternion.getAngleX());
			objectCluster.addData(Shimmer3.ObjectClusterSensorName.AXIS_ANGLE_Y,CHANNEL_TYPE.CAL,CHANNEL_UNITS.LOCAL,quaternion.getAngleY());
			objectCluster.addData(Shimmer3.ObjectClusterSensorName.AXIS_ANGLE_Z,CHANNEL_TYPE.CAL,CHANNEL_UNITS.LOCAL,quaternion.getAngleZ());
		}
		
		if(quaternionOutput){
			objectCluster.addData(Shimmer3.ObjectClusterSensorName.QUAT_MADGE_9DOF_W,CHANNEL_TYPE.CAL,CHANNEL_UNITS.LOCAL,quaternion.getQuaternionW());
			objectCluster.addData(Shimmer3.ObjectClusterSensorName.QUAT_MADGE_9DOF_X,CHANNEL_TYPE.CAL,CHANNEL_UNITS.LOCAL,quaternion.getQuaternionX());
			objectCluster.addData(Shimmer3.ObjectClusterSensorName.QUAT_MADGE_9DOF_Y,CHANNEL_TYPE.CAL,CHANNEL_UNITS.LOCAL,quaternion.getQuaternionY());
			objectCluster.addData(Shimmer3.ObjectClusterSensorName.QUAT_MADGE_9DOF_Z,CHANNEL_TYPE.CAL,CHANNEL_UNITS.LOCAL,quaternion.getQuaternionZ());
		}
		
		return objectCluster;
	}
	
	private Orientation3DObject applyOrientationAlgorithm(){
		
		Orientation3DObject quaternion;
		if(orientationType == ORIENTATION_TYPE.NINE_DOF){
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
		
	}

	@Override
	public void initialize() throws Exception {
		
		double samplingPeriod = 1/sampleRate;
		
		if(mAlgorithmName.equals(AlgorithmName.ORIENTATION_9DOF_LN)
				||mAlgorithmName.equals(AlgorithmName.ORIENTATION_9DOF_WR)){
			orientationType = ORIENTATION_TYPE.NINE_DOF;
			orientationAlgorithm = new GradDes3DOrientation(BETA, samplingPeriod, Q1, Q2, Q3, Q4);
		}
		else if(mAlgorithmName.equals(AlgorithmName.ORIENTATION_6DOF_LN)
				||mAlgorithmName.equals(AlgorithmName.ORIENTATION_6DOF_WR)){
			orientationType = ORIENTATION_TYPE.SIX_DOF;
			orientationAlgorithm = new GradDes3DOrientation6DoF(BETA, samplingPeriod, Q1, Q2, Q3, Q4);
		}
		
		if(mAlgorithmName.equals(AlgorithmName.ORIENTATION_9DOF_LN)
				||mAlgorithmName.equals(AlgorithmName.ORIENTATION_6DOF_LN)){
			accelerometerSensor = Shimmer3.GuiLabelSensors.ACCEL_LN;
		}
		else{
			accelerometerSensor = Shimmer3.GuiLabelSensors.ACCEL_WR;
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
	
	public boolean isEulerOutput() {
		return eulerOutput;
	}

	public void setEulerOutput(boolean eulerOutput) {
		this.eulerOutput = eulerOutput;
	}
	
	public boolean isQuaternionOutput() {
		return quaternionOutput;
	}

	public void setQuaternionOutput(boolean quaternionOutput) {
		this.quaternionOutput = quaternionOutput;
	}
	
	public ORIENTATION_TYPE getOrientationType(){
		return orientationType;
	}

	public void setOrientationType(ORIENTATION_TYPE algorithmType) {
		this.orientationType = algorithmType;
	}
	
	public static LinkedHashMap<String, AlgorithmDetails> getMapOfSupportedAlgorithms(ShimmerVerObject shimmerVerObject) {
		LinkedHashMap<String, AlgorithmDetails> mapOfSupportedAlgorithms = new LinkedHashMap<String, AlgorithmDetails>();
		//TODO Filter here depending on Shimmer version
		mapOfSupportedAlgorithms.putAll(mAlgorithmMapRef);
		return mapOfSupportedAlgorithms;
	}

	@Override
	public void algorithmMapUpdateFromEnabledSensorsVars(long derivedSensorBitmapID) {
		updateModuleIsEnabled(derivedSensorBitmapID);
//		updateModuleOutput(derivedSensorBitmapID);
	}
	
	private void updateModuleIsEnabled(long derivedSensorBitmapID){
		boolean isEnabled = false;
		quaternionOutput = false;
		eulerOutput = false;
		if(mAlgorithmDetails.mAlgorithmName.equals(AlgorithmName.ORIENTATION_9DOF_LN)){
			if((derivedSensorBitmapID&DerivedSensorsBitMask.ORIENTATION_9DOF_LN_QUAT)>0
				||(derivedSensorBitmapID&DerivedSensorsBitMask.ORIENTATION_9DOF_LN_EULER)>0){
				isEnabled = true;
				if((derivedSensorBitmapID&DerivedSensorsBitMask.ORIENTATION_9DOF_LN_QUAT)>0){
					quaternionOutput = true;
				}
				if((derivedSensorBitmapID&DerivedSensorsBitMask.ORIENTATION_9DOF_LN_EULER)>0){
					eulerOutput = true;
				}
			}
		}
		else if(mAlgorithmDetails.mAlgorithmName.equals(AlgorithmName.ORIENTATION_9DOF_WR)){
			if((derivedSensorBitmapID&DerivedSensorsBitMask.ORIENTATION_9DOF_WR_QUAT)>0
				||(derivedSensorBitmapID&DerivedSensorsBitMask.ORIENTATION_9DOF_WR_EULER)>0){
				isEnabled = true;
				if((derivedSensorBitmapID&DerivedSensorsBitMask.ORIENTATION_9DOF_WR_QUAT)>0){
					quaternionOutput = true;
				}
				if((derivedSensorBitmapID&DerivedSensorsBitMask.ORIENTATION_9DOF_WR_EULER)>0){
					eulerOutput = true;
				}
			}
		}
		else if(mAlgorithmDetails.mAlgorithmName.equals(AlgorithmName.ORIENTATION_6DOF_WR)){
			if((derivedSensorBitmapID&DerivedSensorsBitMask.ORIENTATION_6DOF_WR_QUAT)>0
				||(derivedSensorBitmapID&DerivedSensorsBitMask.ORIENTATION_6DOF_WR_EULER)>0){
				isEnabled = true;
				if((derivedSensorBitmapID&DerivedSensorsBitMask.ORIENTATION_6DOF_WR_QUAT)>0){
					quaternionOutput = true;
				}
				if((derivedSensorBitmapID&DerivedSensorsBitMask.ORIENTATION_6DOF_WR_EULER)>0){
					eulerOutput = true;
				}
			}
		}
		else if(mAlgorithmDetails.mAlgorithmName.equals(AlgorithmName.ORIENTATION_6DOF_LN)){
			if((derivedSensorBitmapID&DerivedSensorsBitMask.ORIENTATION_6DOF_LN_QUAT)>0
				||(derivedSensorBitmapID&DerivedSensorsBitMask.ORIENTATION_6DOF_LN_EULER)>0){
				isEnabled = true;
				if((derivedSensorBitmapID&DerivedSensorsBitMask.ORIENTATION_6DOF_LN_QUAT)>0){
					quaternionOutput = true;
				}
				if((derivedSensorBitmapID&DerivedSensorsBitMask.ORIENTATION_6DOF_LN_EULER)>0){
					eulerOutput = true;
				}
			}
		}
		setIsEnabled(isEnabled);
	}

//	private void updateModuleOutput(long derivedSensorBitmapID){
//		if(mIsEnabled){
//			if(mAlgorithmDetails.mAlgorithmName.equals(AlgorithmName.ORIENTATION_9DOF_LN)){
//				if((derivedSensorBitmapID&DerivedSensorsBitMask.ORIENTATION_9DOF_LN_QUAT)>0
//						||(derivedSensorBitmapID&DerivedSensorsBitMask.ORIENTATION_6DOF_LN_QUAT)>0
//						||(derivedSensorBitmapID&DerivedSensorsBitMask.ORIENTATION_9DOF_WR_QUAT)>0
//						||(derivedSensorBitmapID&DerivedSensorsBitMask.ORIENTATION_6DOF_WR_QUAT)>0){
//					quaternionOutput = true;
//				}
//			}
//			else if(mAlgorithmDetails.mAlgorithmName.equals(AlgorithmName.ORIENTATION_9DOF_WR)){
//				if((derivedSensorBitmapID&DerivedSensorsBitMask.ORIENTATION_9DOF_LN_QUAT)>0
//						||(derivedSensorBitmapID&DerivedSensorsBitMask.ORIENTATION_6DOF_LN_QUAT)>0
//						||(derivedSensorBitmapID&DerivedSensorsBitMask.ORIENTATION_9DOF_WR_QUAT)>0
//						||(derivedSensorBitmapID&DerivedSensorsBitMask.ORIENTATION_6DOF_WR_QUAT)>0){
//					quaternionOutput = true;
//				}
//				
//			}
//			
//			if((derivedSensorBitmapID&DerivedSensorsBitMask.ORIENTATION_9DOF_LN_EULER)>0
//					||(derivedSensorBitmapID&DerivedSensorsBitMask.ORIENTATION_6DOF_LN_EULER)>0
//					||(derivedSensorBitmapID&DerivedSensorsBitMask.ORIENTATION_9DOF_WR_EULER)>0
//					||(derivedSensorBitmapID&DerivedSensorsBitMask.ORIENTATION_6DOF_WR_EULER)>0){
//				eulerOutput = true;
//			}
//		}
//		else{
//			quaternionOutput = false;
//			eulerOutput = false;
//		}
//	}
	
	@Override
	public long getDerivedSensorBitmapID() {
		long bitmask = 0;
		if(mAlgorithmDetails!=null && isEnabled()){
			if(mAlgorithmDetails.mAlgorithmName.equals(AlgorithmName.ORIENTATION_9DOF_LN)){
				if(quaternionOutput){
					bitmask |= DerivedSensorsBitMask.ORIENTATION_9DOF_LN_QUAT;
				}
				if(eulerOutput){
					bitmask |= DerivedSensorsBitMask.ORIENTATION_9DOF_LN_EULER;
				}
			}
			else if(mAlgorithmDetails.mAlgorithmName.equals(AlgorithmName.ORIENTATION_9DOF_WR)){
				if(quaternionOutput){
					bitmask |= DerivedSensorsBitMask.ORIENTATION_9DOF_WR_QUAT;
				}
				if(eulerOutput){
					bitmask |= DerivedSensorsBitMask.ORIENTATION_9DOF_WR_EULER;
				}
			}
			else if(mAlgorithmDetails.mAlgorithmName.equals(AlgorithmName.ORIENTATION_6DOF_WR)){
				if(quaternionOutput){
					bitmask |= DerivedSensorsBitMask.ORIENTATION_6DOF_WR_QUAT;
				}
				if(eulerOutput){
					bitmask |= DerivedSensorsBitMask.ORIENTATION_6DOF_WR_EULER;
				}
			}
			else if(mAlgorithmDetails.mAlgorithmName.equals(AlgorithmName.ORIENTATION_6DOF_LN)){
				if(quaternionOutput){
					bitmask |= DerivedSensorsBitMask.ORIENTATION_6DOF_LN_QUAT;
				}
				if(eulerOutput){
					bitmask |= DerivedSensorsBitMask.ORIENTATION_6DOF_LN_EULER;
				}
			}
		}
		return bitmask;
	}

}
