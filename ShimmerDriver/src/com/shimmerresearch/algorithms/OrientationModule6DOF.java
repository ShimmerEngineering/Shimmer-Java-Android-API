package com.shimmerresearch.algorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.vecmath.Vector3d;

import com.shimmerresearch.algorithms.AbstractAlgorithm.ALGORITHM_RESULT_TYPE;
import com.shimmerresearch.algorithms.AbstractAlgorithm.ALGORITHM_TYPE;
import com.shimmerresearch.algorithms.AbstractAlgorithm.GuiLabelConfigCommon;
import com.shimmerresearch.algorithms.OrientationModule.AlgorithmName;
import com.shimmerresearch.algorithms.OrientationModule.GuiFriendlyLabelConfig;
//import com.shimmerresearch.algorithms.OrientationModule.GuiLabelConfig;
import com.shimmerresearch.algorithms.OrientationModule.ORIENTATION_TYPE;
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

public class OrientationModule6DOF extends OrientationModule{


	/** * */
	private static final long serialVersionUID = -4174847826978293223L;

	//TODO update object cluster name with previously agreed name
	public static class AlgorithmName{
		public static final String ORIENTATION_6DOF_LN = "LN_Acc_6DoF";
		public static final String ORIENTATION_6DOF_WR = "WR_Acc_6DoF";
	}

	public class GuiLabelConfig{
		public static final String ACCELEROMETER = "Accelerometer";
		public static final String QUATERNION_OUTPUT_6DOF = "Quaternion_6DOF";
		public static final String EULER_OUTPUT_6DOF = "Euler_6DOF";
	}


	transient Object orientationAlgorithm;

	protected static String WR = "_WR";
	protected static String LN = "_LN";

	public static class ObjectClusterSensorName{
		public static String QUAT_MADGE_6DOF_W = Configuration.Shimmer3.ObjectClusterSensorName.QUAT_MADGE_6DOF_W; 
		public static String QUAT_MADGE_6DOF_W_LN = QUAT_MADGE_6DOF_W + LN; 
		public static String QUAT_MADGE_6DOF_W_WR = QUAT_MADGE_6DOF_W + WR; 
		public static String QUAT_MADGE_6DOF_X = Configuration.Shimmer3.ObjectClusterSensorName.QUAT_MADGE_6DOF_X; 
		public static String QUAT_MADGE_6DOF_X_LN = QUAT_MADGE_6DOF_X + LN; 
		public static String QUAT_MADGE_6DOF_X_WR = QUAT_MADGE_6DOF_X + WR; 
		public static String QUAT_MADGE_6DOF_Y = Configuration.Shimmer3.ObjectClusterSensorName.QUAT_MADGE_6DOF_Y; 
		public static String QUAT_MADGE_6DOF_Y_LN = QUAT_MADGE_6DOF_Y + LN; 
		public static String QUAT_MADGE_6DOF_Y_WR = QUAT_MADGE_6DOF_Y + WR; 
		public static String QUAT_MADGE_6DOF_Z = Configuration.Shimmer3.ObjectClusterSensorName.QUAT_MADGE_6DOF_Z; 
		public static String QUAT_MADGE_6DOF_Z_LN = QUAT_MADGE_6DOF_Z + LN; 
		public static String QUAT_MADGE_6DOF_Z_WR = QUAT_MADGE_6DOF_Z + WR; 

		public static String EULER_6DOF_A = Configuration.Shimmer3.ObjectClusterSensorName.EULER_6DOF_A; 
		public static String EULER_6DOF_A_LN = EULER_6DOF_A + LN; 
		public static String EULER_6DOF_A_WR = EULER_6DOF_A + WR; 
		public static String EULER_6DOF_X = Configuration.Shimmer3.ObjectClusterSensorName.EULER_6DOF_X; 
		public static String EULER_6DOF_X_LN = EULER_6DOF_X + LN; 
		public static String EULER_6DOF_X_WR = EULER_6DOF_X + WR; 
		public static String EULER_6DOF_Y = Configuration.Shimmer3.ObjectClusterSensorName.EULER_6DOF_Y; 
		public static String EULER_6DOF_Y_LN = EULER_6DOF_Y + LN; 
		public static String EULER_6DOF_Y_WR = EULER_6DOF_Y + WR; 
		public static String EULER_6DOF_Z = Configuration.Shimmer3.ObjectClusterSensorName.EULER_6DOF_Z; 
		public static String EULER_6DOF_Z_LN = EULER_6DOF_Z + LN; 
		public static String EULER_6DOF_Z_WR = EULER_6DOF_Z + WR; 

		@Deprecated //need to describe axis angle 9DOF vs 6DOF
		public static String AXIS_ANGLE_A = Configuration.Shimmer3.ObjectClusterSensorName.AXIS_ANGLE_A; 
		@Deprecated //need to describe axis angle 9DOF vs 6DOF
		public static String AXIS_ANGLE_X = Configuration.Shimmer3.ObjectClusterSensorName.AXIS_ANGLE_X; 
		@Deprecated //need to describe axis angle 9DOF vs 6DOF
		public static String AXIS_ANGLE_Y = Configuration.Shimmer3.ObjectClusterSensorName.AXIS_ANGLE_Y; 
		@Deprecated //need to describe axis angle 9DOF vs 6DOF
		public static String AXIS_ANGLE_Z = Configuration.Shimmer3.ObjectClusterSensorName.AXIS_ANGLE_Z; 
	}

	//TODO 6DOF channal details for low noise 
	private static final ChannelDetails channelAngleA_6DOF_LN = new ChannelDetails(
			ObjectClusterSensorName.EULER_6DOF_A_LN, //ObjectClusterName
			ObjectClusterSensorName.EULER_6DOF_A_LN, //GUI friendly text to display
			ObjectClusterSensorName.EULER_6DOF_A_LN, //database name
			CHANNEL_UNITS.NO_UNITS,
			Arrays.asList(CHANNEL_TYPE.CAL));

	private static final ChannelDetails channelAngleX_6DOF_LN = new ChannelDetails(
			ObjectClusterSensorName.EULER_6DOF_X_LN, //ObjectClusterName
			ObjectClusterSensorName.EULER_6DOF_X_LN, //GUI friendly text to display
			ObjectClusterSensorName.EULER_6DOF_X_LN, //database name
			CHANNEL_UNITS.NO_UNITS,
			Arrays.asList(CHANNEL_TYPE.CAL));

	private static final ChannelDetails channelAngleY_6DOF_LN = new ChannelDetails(
			ObjectClusterSensorName.EULER_6DOF_Y_LN, //ObjectClusterName
			ObjectClusterSensorName.EULER_6DOF_Y_LN, //GUI friendly text to display
			ObjectClusterSensorName.EULER_6DOF_Y_LN, //database name
			CHANNEL_UNITS.NO_UNITS,
			Arrays.asList(CHANNEL_TYPE.CAL));

	private static final ChannelDetails channelAngleZ_6DOF_LN = new ChannelDetails(
			ObjectClusterSensorName.EULER_6DOF_Z_LN, //ObjectClusterName
			ObjectClusterSensorName.EULER_6DOF_Z_LN, //GUI friendly text to display
			ObjectClusterSensorName.EULER_6DOF_Z_LN, //database name
			CHANNEL_UNITS.NO_UNITS,
			Arrays.asList(CHANNEL_TYPE.CAL));

	private static final ChannelDetails channelQuatW_6DOF_LN = new ChannelDetails(
			ObjectClusterSensorName.QUAT_MADGE_6DOF_W_LN,
			ObjectClusterSensorName.QUAT_MADGE_6DOF_W_LN,
			ObjectClusterSensorName.QUAT_MADGE_6DOF_W_LN, //database name
			CHANNEL_UNITS.NO_UNITS,
			Arrays.asList(CHANNEL_TYPE.CAL));

	private static final ChannelDetails channelQuatX_6DOF_LN = new ChannelDetails(
			ObjectClusterSensorName.QUAT_MADGE_6DOF_X_LN,
			ObjectClusterSensorName.QUAT_MADGE_6DOF_X_LN,
			ObjectClusterSensorName.QUAT_MADGE_6DOF_X_LN, //database name
			CHANNEL_UNITS.NO_UNITS,
			Arrays.asList(CHANNEL_TYPE.CAL));

	private static final ChannelDetails channelQuatY_6DOF_LN = new ChannelDetails(
			ObjectClusterSensorName.QUAT_MADGE_6DOF_Y_LN,
			ObjectClusterSensorName.QUAT_MADGE_6DOF_Y_LN,
			ObjectClusterSensorName.QUAT_MADGE_6DOF_Y_LN, //database name
			CHANNEL_UNITS.NO_UNITS,
			Arrays.asList(CHANNEL_TYPE.CAL));

	private static final ChannelDetails channelQuatZ_6DOF_LN = new ChannelDetails(
			ObjectClusterSensorName.QUAT_MADGE_6DOF_Z_LN,
			ObjectClusterSensorName.QUAT_MADGE_6DOF_Z_LN,
			ObjectClusterSensorName.QUAT_MADGE_6DOF_Z_LN, //database name
			CHANNEL_UNITS.NO_UNITS,
			Arrays.asList(CHANNEL_TYPE.CAL));


	//6DOF wide range 
	private static final ChannelDetails channelAngleA_6DOF_WR = new ChannelDetails(
			ObjectClusterSensorName.EULER_6DOF_A_WR, //ObjectClusterName
			ObjectClusterSensorName.EULER_6DOF_A_WR, //GUI friendly text to display
			ObjectClusterSensorName.EULER_6DOF_A_WR, //database name
			CHANNEL_UNITS.NO_UNITS,
			Arrays.asList(CHANNEL_TYPE.CAL));

	private static final ChannelDetails channelAngleX_6DOF_WR = new ChannelDetails(
			ObjectClusterSensorName.EULER_6DOF_X_WR, //ObjectClusterName
			ObjectClusterSensorName.EULER_6DOF_X_WR, //GUI friendly text to display
			ObjectClusterSensorName.EULER_6DOF_X_WR, //database name
			CHANNEL_UNITS.NO_UNITS,
			Arrays.asList(CHANNEL_TYPE.CAL));

	private static final ChannelDetails channelAngleY_6DOF_WR = new ChannelDetails(
			ObjectClusterSensorName.EULER_6DOF_Y_WR, //ObjectClusterName
			ObjectClusterSensorName.EULER_6DOF_Y_WR, //GUI friendly text to display
			ObjectClusterSensorName.EULER_6DOF_Y_WR, //database name
			CHANNEL_UNITS.NO_UNITS,
			Arrays.asList(CHANNEL_TYPE.CAL));

	private static final ChannelDetails channelAngleZ_6DOF_WR = new ChannelDetails(
			ObjectClusterSensorName.EULER_6DOF_Z_WR, //ObjectClusterName
			ObjectClusterSensorName.EULER_6DOF_Z_WR, //GUI friendly text to display
			ObjectClusterSensorName.EULER_6DOF_Z_WR, //database name
			CHANNEL_UNITS.NO_UNITS,
			Arrays.asList(CHANNEL_TYPE.CAL));

	private static final ChannelDetails channelQuatW_6DOF_WR = new ChannelDetails(
			ObjectClusterSensorName.QUAT_MADGE_6DOF_W_WR,
			ObjectClusterSensorName.QUAT_MADGE_6DOF_W_WR,
			ObjectClusterSensorName.QUAT_MADGE_6DOF_W_WR, //database name
			CHANNEL_UNITS.NO_UNITS,
			Arrays.asList(CHANNEL_TYPE.CAL));

	private static final ChannelDetails channelQuatX_6DOF_WR = new ChannelDetails(
			ObjectClusterSensorName.QUAT_MADGE_6DOF_X_WR,
			ObjectClusterSensorName.QUAT_MADGE_6DOF_X_WR,
			ObjectClusterSensorName.QUAT_MADGE_6DOF_X_WR, //database name
			CHANNEL_UNITS.NO_UNITS,
			Arrays.asList(CHANNEL_TYPE.CAL));

	private static final ChannelDetails channelQuatY_6DOF_WR = new ChannelDetails(
			ObjectClusterSensorName.QUAT_MADGE_6DOF_Y_WR,
			ObjectClusterSensorName.QUAT_MADGE_6DOF_Y_WR,
			ObjectClusterSensorName.QUAT_MADGE_6DOF_Y_WR, //database name
			CHANNEL_UNITS.NO_UNITS,
			Arrays.asList(CHANNEL_TYPE.CAL));

	private static final ChannelDetails channelQuatZ_6DOF_WR = new ChannelDetails(
			ObjectClusterSensorName.QUAT_MADGE_6DOF_Z_WR,
			ObjectClusterSensorName.QUAT_MADGE_6DOF_Z_WR,
			ObjectClusterSensorName.QUAT_MADGE_6DOF_Z_WR, //database name
			CHANNEL_UNITS.NO_UNITS,
			Arrays.asList(CHANNEL_TYPE.CAL));

	//6DOF channel groups
	public static List<ChannelDetails> listChannelsQuat6DOF_WR = Arrays.asList(
			channelQuatW_6DOF_WR, channelQuatX_6DOF_WR, channelQuatY_6DOF_WR, channelQuatZ_6DOF_WR);

	public static List<ChannelDetails> listChannelsEuler6DOF_LN = Arrays.asList(
			channelAngleA_6DOF_LN, channelAngleX_6DOF_LN, channelAngleY_6DOF_LN, channelAngleZ_6DOF_LN);

	public static List<ChannelDetails> listChannelsQuat6DOF_LN = Arrays.asList(
			channelQuatW_6DOF_LN, channelQuatX_6DOF_LN, channelQuatY_6DOF_LN, channelQuatZ_6DOF_LN);

	public static List<ChannelDetails> listChannelsEuler6DOF_WR = Arrays.asList(
			channelAngleA_6DOF_WR, channelAngleX_6DOF_WR, channelAngleY_6DOF_WR, channelAngleZ_6DOF_WR);

	public static final AlgorithmDetails algo6DoFOrientation_LN_Acc = new AlgorithmDetails(
			AlgorithmName.ORIENTATION_6DOF_LN, 
			OrientationModule.GuiFriendlyLabelConfig.ORIENTATAION_LN, 
			Arrays.asList(Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_LN_X,
					Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_LN_Y,
					Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_LN_Z,
					Configuration.Shimmer3.ObjectClusterSensorName.GYRO_X,
					Configuration.Shimmer3.ObjectClusterSensorName.GYRO_Y,
					Configuration.Shimmer3.ObjectClusterSensorName.GYRO_Z),
			(DerivedSensorsBitMask.ORIENTATION_6DOF_LN_QUAT|DerivedSensorsBitMask.ORIENTATION_6DOF_LN_EULER), 
			Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_ANALOG_ACCEL,
					Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_GYRO),
					CHANNEL_UNITS.NO_UNITS,
					listChannelsEuler6DOF_LN);

	public static final AlgorithmDetails algo6DoFOrientation_WR_Acc = new AlgorithmDetails(
			AlgorithmName.ORIENTATION_6DOF_WR, 
			OrientationModule.GuiFriendlyLabelConfig.ORIENTATAION_WR, 
			Arrays.asList(Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_WR_X,
					Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_WR_Y,
					Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_WR_Z,
					Configuration.Shimmer3.ObjectClusterSensorName.GYRO_X,
					Configuration.Shimmer3.ObjectClusterSensorName.GYRO_Y,
					Configuration.Shimmer3.ObjectClusterSensorName.GYRO_Z),
			(DerivedSensorsBitMask.ORIENTATION_6DOF_WR_QUAT|DerivedSensorsBitMask.ORIENTATION_6DOF_WR_EULER), 
			Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_LSM303DLHC_ACCEL,
					Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_GYRO),
					CHANNEL_UNITS.NO_UNITS,
					listChannelsQuat6DOF_WR);


	public static final Map<String, AlgorithmDetails> mAlgorithmMapRef;
	static {
		Map<String, AlgorithmDetails> aMap = new LinkedHashMap<String, AlgorithmDetails>();
		aMap.put(algo6DoFOrientation_LN_Acc.mAlgorithmName, algo6DoFOrientation_LN_Acc);
		aMap.put(algo6DoFOrientation_WR_Acc.mAlgorithmName, algo6DoFOrientation_WR_Acc);
		mAlgorithmMapRef = Collections.unmodifiableMap(aMap);
	}

	public static LinkedHashMap<String, AlgorithmDetails> getMapOfSupportedAlgorithms(ShimmerVerObject shimmerVerObject) {
		LinkedHashMap<String, AlgorithmDetails> mapOfSupportedAlgorithms = new LinkedHashMap<String, AlgorithmDetails>();
		//TODO Filter here depending on Shimmer version
		mapOfSupportedAlgorithms.putAll(mAlgorithmMapRef);
		return mapOfSupportedAlgorithms;
	}


	// ------------------- Algorithms grouping map start -----------------------
	public static final SensorGroupingDetails sGD6Dof = new SensorGroupingDetails(
			Configuration.Shimmer3.GuiLabelAlgorithmGrouping.ORIENTATION_6DOF.getTileText(), 
			Arrays.asList(OrientationModule6DOF.algo6DoFOrientation_LN_Acc,
					OrientationModule6DOF.algo6DoFOrientation_WR_Acc),
					Arrays.asList(GuiLabelConfig.QUATERNION_OUTPUT_6DOF,
							GuiLabelConfig.EULER_OUTPUT_6DOF),
							0);
	// ------------------- Algorithms grouping map end -----------------------

	{
//		mConfigOptionsMap.put(SAMPLING_RATE,new AlgorithmConfigOptionDetails(AlgorithmConfigOptionDetails.GUI_COMPONENT_TYPE.TEXTFIELD,mListSVO));
//		String[] accSensors = new String[2];
//		accSensors[0]=Shimmer3.GuiLabelSensorTiles.LOW_NOISE_ACCEL;
//		accSensors[1]=Shimmer3.GuiLabelSensorTiles.WIDE_RANGE_ACCEL;
//		mConfigOptionsMap.put(ACCELEROMETER, new AlgorithmConfigOptionDetails(AlgorithmConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX, mListSVO, accSensors));

		final ConfigOptionDetailsAlgorithm configOptionQuatOutput = new ConfigOptionDetailsAlgorithm(
				QUATERNION_OPTIONS, 
				GuiFriendlyLabelConfig.QUATERNION_OUTPUT,
				GUI_COMPONENT_TYPE.COMBOBOX,
				null);

		final ConfigOptionDetailsAlgorithm configOptionEulerOutput = new ConfigOptionDetailsAlgorithm(
				EULER_OPTIONS, 
				GuiFriendlyLabelConfig.EULER_OUTPUT,
				GUI_COMPONENT_TYPE.COMBOBOX,
				null);


		mConfigOptionsMap.put(GuiLabelConfig.QUATERNION_OUTPUT_6DOF, configOptionQuatOutput);
		mConfigOptionsMap.put(GuiLabelConfig.EULER_OUTPUT_6DOF, configOptionEulerOutput);

		mMapOfAlgorithmGrouping.put(Configuration.Shimmer3.GuiLabelAlgorithmGrouping.ORIENTATION_6DOF.ordinal(), sGD6Dof);

	}

	public ObjectCluster addQuaternionToObjectCluster(Orientation3DObject quaternion, ObjectCluster objectCluster){


		if(mAlgorithmName.equals(AlgorithmName.ORIENTATION_6DOF_LN)){
			if(isEulerOutput()){
				objectCluster.addData(ObjectClusterSensorName.EULER_6DOF_A_LN,CHANNEL_TYPE.CAL,CHANNEL_UNITS.NO_UNITS,quaternion.getTheta());
				objectCluster.addData(ObjectClusterSensorName.EULER_6DOF_X_LN,CHANNEL_TYPE.CAL,CHANNEL_UNITS.NO_UNITS,quaternion.getAngleX());
				objectCluster.addData(ObjectClusterSensorName.EULER_6DOF_Y_LN,CHANNEL_TYPE.CAL,CHANNEL_UNITS.NO_UNITS,quaternion.getAngleY());
				objectCluster.addData(ObjectClusterSensorName.EULER_6DOF_Z_LN,CHANNEL_TYPE.CAL,CHANNEL_UNITS.NO_UNITS,quaternion.getAngleZ());
			}
			if(isQuaternionOutput()){
				objectCluster.addData(ObjectClusterSensorName.QUAT_MADGE_6DOF_W_LN,CHANNEL_TYPE.CAL,CHANNEL_UNITS.NO_UNITS,quaternion.getQuaternionW());
				objectCluster.addData(ObjectClusterSensorName.QUAT_MADGE_6DOF_X_LN,CHANNEL_TYPE.CAL,CHANNEL_UNITS.NO_UNITS,quaternion.getQuaternionX());
				objectCluster.addData(ObjectClusterSensorName.QUAT_MADGE_6DOF_Y_LN,CHANNEL_TYPE.CAL,CHANNEL_UNITS.NO_UNITS,quaternion.getQuaternionY());
				objectCluster.addData(ObjectClusterSensorName.QUAT_MADGE_6DOF_Z_LN,CHANNEL_TYPE.CAL,CHANNEL_UNITS.NO_UNITS,quaternion.getQuaternionZ());
			}
		}
		else if(mAlgorithmName.equals(AlgorithmName.ORIENTATION_6DOF_WR)){
			if(isEulerOutput()){
				objectCluster.addData(ObjectClusterSensorName.EULER_6DOF_A_WR,CHANNEL_TYPE.CAL,CHANNEL_UNITS.NO_UNITS,quaternion.getTheta());
				objectCluster.addData(ObjectClusterSensorName.EULER_6DOF_X_WR,CHANNEL_TYPE.CAL,CHANNEL_UNITS.NO_UNITS,quaternion.getAngleX());
				objectCluster.addData(ObjectClusterSensorName.EULER_6DOF_Y_WR,CHANNEL_TYPE.CAL,CHANNEL_UNITS.NO_UNITS,quaternion.getAngleY());
				objectCluster.addData(ObjectClusterSensorName.EULER_6DOF_Z_WR,CHANNEL_TYPE.CAL,CHANNEL_UNITS.NO_UNITS,quaternion.getAngleZ());
			}
			if(isQuaternionOutput()){
				objectCluster.addData(ObjectClusterSensorName.QUAT_MADGE_6DOF_W_WR,CHANNEL_TYPE.CAL,CHANNEL_UNITS.NO_UNITS,quaternion.getQuaternionW());
				objectCluster.addData(ObjectClusterSensorName.QUAT_MADGE_6DOF_X_WR,CHANNEL_TYPE.CAL,CHANNEL_UNITS.NO_UNITS,quaternion.getQuaternionX());
				objectCluster.addData(ObjectClusterSensorName.QUAT_MADGE_6DOF_Y_WR,CHANNEL_TYPE.CAL,CHANNEL_UNITS.NO_UNITS,quaternion.getQuaternionY());
				objectCluster.addData(ObjectClusterSensorName.QUAT_MADGE_6DOF_Z_WR,CHANNEL_TYPE.CAL,CHANNEL_UNITS.NO_UNITS,quaternion.getQuaternionZ());
			}
		}

		return objectCluster;
	}


	public OrientationModule6DOF(AlgorithmDetails algorithmDetails, double samplingRate) {
		// TODO Auto-generated constructor stub
		mAlgorithmDetails = algorithmDetails;
		mAlgorithmType = ALGORITHM_TYPE.ALGORITHM_TYPE_CONTINUOUS;
		mAlgorithmResultType = ALGORITHM_RESULT_TYPE.ALGORITHM_RESULT_TYPE_SINGLE_OBJECT_CLUSTER;
		mAlgorithmName = algorithmDetails.mAlgorithmName;
//		mAlgorithmGroupingName = "6DOF";

		this.samplingRate = samplingRate;

		try {
			initialize();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public void initialize() throws Exception {

		double samplingPeriod = 1/samplingRate;

		if(mAlgorithmName.equals(AlgorithmName.ORIENTATION_6DOF_LN)
				||mAlgorithmName.equals(AlgorithmName.ORIENTATION_6DOF_WR)){
			orientationType = ORIENTATION_TYPE.SIX_DOF;
			orientationAlgorithm = new GradDes3DOrientation6DoF(BETA, samplingPeriod, Q1, Q2, Q3, Q4);
		}

		if(mAlgorithmName.equals(AlgorithmName.ORIENTATION_6DOF_LN)){
			accelerometerSensor = GuiFriendlyLabelConfig.ORIENTATAION_LN;
		}
		else{
			accelerometerSensor = GuiFriendlyLabelConfig.ORIENTATAION_WR;
		}
	}


	public Orientation3DObject applyOrientationAlgorithm(){

		Orientation3DObject quaternion;

		quaternion = ((GradDes3DOrientation6DoF) orientationAlgorithm).update(
				accValues.x, accValues.y, accValues.z, 
				gyroValues.x, gyroValues.y, gyroValues.z);

		return quaternion;
	}


	@Override
	public List<ChannelDetails> getChannelDetails() {

		List<ChannelDetails> listOfChannelDetails = new ArrayList<ChannelDetails>();

		if(mAlgorithmDetails.mAlgorithmName.equals(AlgorithmName.ORIENTATION_6DOF_WR)){
			if(isQuaternionOutput()){
				listOfChannelDetails.addAll(listChannelsQuat6DOF_WR);
			}
			if(isEulerOutput()){
				listOfChannelDetails.addAll(listChannelsEuler6DOF_WR);
			}
		}
		else if(mAlgorithmDetails.mAlgorithmName.equals(AlgorithmName.ORIENTATION_6DOF_LN)){
			if(isQuaternionOutput()){
				listOfChannelDetails.addAll(listChannelsQuat6DOF_LN);
			}
			if(isEulerOutput()){
				listOfChannelDetails.addAll(listChannelsEuler6DOF_LN);
			}
		}

		//		return super.getChannelDetails();
		return listOfChannelDetails;
	}

	public void setAccelerometer(String accelerometerName){
		this.accelerometerSensor = accelerometerName;
	}

	@Override
	public Object getSettings(String componentName) {
		Object returnValue = null;
		switch(componentName){

		case(GuiLabelConfigCommon.SAMPLING_RATE):
			returnValue = getSamplingRate();
		break;
//		case(GuiLabelConfig.ACCELEROMETER):
//			returnValue = getAccelerometer();
//		break;
		case(GuiLabelConfig.QUATERNION_OUTPUT_6DOF):
			returnValue = isQuaternionOutput();
		break;
		case(GuiLabelConfig.EULER_OUTPUT_6DOF):
			returnValue = isEulerOutput();
		break;
		}
		return returnValue;
	}


	@Override
	public Object getDefaultSettings(String componentName) {
		Object returnValue = null;
		switch(componentName){
		case(GuiLabelConfigCommon.SAMPLING_RATE):
			returnValue = 512;
		break;
//		case(GuiLabelConfig.ACCELEROMETER):
//			returnValue = Shimmer3.GuiLabelSensorTiles.LOW_NOISE_ACCEL;
//			break;
		case(GuiLabelConfig.QUATERNION_OUTPUT_6DOF):
			returnValue = true;
		break;
		case(GuiLabelConfig.EULER_OUTPUT_6DOF):
			returnValue = false;
		break;
		}
		return returnValue;
	}

	@Override
	public void setSettings(String componentName, Object valueToSet){

		switch(componentName){
		case(GuiLabelConfigCommon.SAMPLING_RATE):
			if(valueToSet instanceof String){
				if(!((String) valueToSet).isEmpty()){
					setSamplingRate(Double.parseDouble((String) valueToSet));
				}
			}
			else if(valueToSet instanceof Double){
				setSamplingRate((Double) valueToSet);
			}
		break;
//		case(GuiLabelConfig.ACCELEROMETER):
//			setAccelerometer((String) valueToSet);
//			break;
		case(GuiLabelConfig.QUATERNION_OUTPUT_6DOF):
			if(valueToSet instanceof Boolean){
				setQuaternionOutput((boolean) valueToSet);
			}
			else if(valueToSet instanceof Integer){
				setQuaternionOutput(((Integer) valueToSet)>0? true:false);
			}
		break;
		case(GuiLabelConfig.EULER_OUTPUT_6DOF):
			if(valueToSet instanceof Boolean){
				setEulerOutput((boolean) valueToSet);
			}
			else if(valueToSet instanceof Integer){
				setEulerOutput(((Integer) valueToSet)>0? true:false);
			}
		break;
		}
	}


}


