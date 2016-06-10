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
	
	private static final String[] QUATERNION_OPTIONS = {"Off", "On"};
	private static final String[] EULER_OPTIONS = {"Off", "On"};
	
	private static final ShimmerVerObject baseSh3Module = new ShimmerVerObject(
			HW_ID.SHIMMER_3,ShimmerVerDetails.ANY_VERSION,
			ShimmerVerDetails.ANY_VERSION,
			ShimmerVerDetails.ANY_VERSION,
			ShimmerVerDetails.ANY_VERSION,
			HW_ID_SR_CODES.EXP_BRD_EXG);
	
	//TODO update objectcluster name with previously aggreed name
	public static class AlgorithmName{
		public static final String ORIENTATION_9DOF_LN = "LN_Acc_9DoF";
		public static final String ORIENTATION_9DOF_WR = "WR_Acc_9DoF";
	}
	
	public class GuiLabelConfig{
		public static final String ACCELEROMETER = "Accelerometer";
		public static final String QUATERNION_OUTPUT = "Quaternion";
		public static final String EULER_OUTPUT = "Euler";
	}
	
	
	public static List<ShimmerVerObject> mListSVO = new ArrayList<ShimmerVerObject>(); 
	
	
	transient Object orientationAlgorithm;
	
	private double samplingRate;
	private String accelerometerSensor;
	private boolean quaternionOutput = true;
	private boolean eulerOutput = false;
	
	protected static String WR = "_WR";
	protected static String LN = "_LN";
	
	public static class ObjectClusterSensorName{
	
		public static String QUAT_MADGE_9DOF_W = Configuration.Shimmer3.ObjectClusterSensorName.QUAT_MADGE_9DOF_W; 
		public static String QUAT_MADGE_9DOF_W_LN = QUAT_MADGE_9DOF_W + LN; 
		public static String QUAT_MADGE_9DOF_W_WR = QUAT_MADGE_9DOF_W + WR; 
		public static String QUAT_MADGE_9DOF_X = Configuration.Shimmer3.ObjectClusterSensorName.QUAT_MADGE_9DOF_X; 
		public static String QUAT_MADGE_9DOF_X_LN = QUAT_MADGE_9DOF_X + LN; 
		public static String QUAT_MADGE_9DOF_X_WR = QUAT_MADGE_9DOF_X + WR; 
		public static String QUAT_MADGE_9DOF_Y = Configuration.Shimmer3.ObjectClusterSensorName.QUAT_MADGE_9DOF_Y; 
		public static String QUAT_MADGE_9DOF_Y_LN = QUAT_MADGE_9DOF_Y + LN; 
		public static String QUAT_MADGE_9DOF_Y_WR = QUAT_MADGE_9DOF_Y + WR; 
		public static String QUAT_MADGE_9DOF_Z = Configuration.Shimmer3.ObjectClusterSensorName.QUAT_MADGE_9DOF_Z; 
		public static String QUAT_MADGE_9DOF_Z_LN = QUAT_MADGE_9DOF_Z + LN; 
		public static String QUAT_MADGE_9DOF_Z_WR = QUAT_MADGE_9DOF_Z + WR; 
		
		public static String EULER_9DOF_A = Configuration.Shimmer3.ObjectClusterSensorName.EULER_9DOF_A; 
		public static String EULER_9DOF_A_LN = EULER_9DOF_A + LN; 
		public static String EULER_9DOF_A_WR = EULER_9DOF_A; 
		public static String EULER_9DOF_X = Configuration.Shimmer3.ObjectClusterSensorName.EULER_9DOF_X; 
		public static String EULER_9DOF_X_LN = EULER_9DOF_X + LN; 
		public static String EULER_9DOF_X_WR = EULER_9DOF_X; 
		public static String EULER_9DOF_Y = Configuration.Shimmer3.ObjectClusterSensorName.EULER_9DOF_Y; 
		public static String EULER_9DOF_Y_LN = EULER_9DOF_Y + LN; 
		public static String EULER_9DOF_Y_WR = EULER_9DOF_Y; 
		public static String EULER_9DOF_Z = Configuration.Shimmer3.ObjectClusterSensorName.EULER_9DOF_Z; 
		public static String EULER_9DOF_Z_LN = EULER_9DOF_Z + LN; 
		public static String EULER_9DOF_Z_WR = EULER_9DOF_Z; 

		@Deprecated //need to describe axis angle 9DOF vs 6DOF
		public static String AXIS_ANGLE_A = Configuration.Shimmer3.ObjectClusterSensorName.AXIS_ANGLE_A; 
		@Deprecated //need to describe axis angle 9DOF vs 6DOF
		public static String AXIS_ANGLE_X = Configuration.Shimmer3.ObjectClusterSensorName.AXIS_ANGLE_X; 
		@Deprecated //need to describe axis angle 9DOF vs 6DOF
		public static String AXIS_ANGLE_Y = Configuration.Shimmer3.ObjectClusterSensorName.AXIS_ANGLE_Y; 
		@Deprecated //need to describe axis angle 9DOF vs 6DOF
		public static String AXIS_ANGLE_Z = Configuration.Shimmer3.ObjectClusterSensorName.AXIS_ANGLE_Z; 
	}
	
	//9DOF
	
	//TODO 9DOF channal details for low noise 
	private static final ChannelDetails channelAngleA_9DOF_LN = new ChannelDetails(
			ObjectClusterSensorName.EULER_9DOF_A_LN, //ObjectClusterName
			ObjectClusterSensorName.EULER_9DOF_A_LN, //GUI friendly text to display
			ObjectClusterSensorName.EULER_9DOF_A_LN, //database name
			CHANNEL_UNITS.NO_UNITS,
			Arrays.asList(CHANNEL_TYPE.CAL));
	
	private static final ChannelDetails channelAngleX_9DOF_LN = new ChannelDetails(
			ObjectClusterSensorName.EULER_9DOF_X_LN, //ObjectClusterName
			ObjectClusterSensorName.EULER_9DOF_X_LN, //GUI friendly text to display
			ObjectClusterSensorName.EULER_9DOF_X_LN, //database name
			CHANNEL_UNITS.NO_UNITS,
			Arrays.asList(CHANNEL_TYPE.CAL));
	
	private static final ChannelDetails channelAngleY_9DOF_LN = new ChannelDetails(
			ObjectClusterSensorName.EULER_9DOF_Y_LN, //ObjectClusterName
			ObjectClusterSensorName.EULER_9DOF_Y_LN, //GUI friendly text to display
			ObjectClusterSensorName.EULER_9DOF_Y_LN, //database name
			CHANNEL_UNITS.NO_UNITS,
			Arrays.asList(CHANNEL_TYPE.CAL));
	
	private static final ChannelDetails channelAngleZ_9DOF_LN = new ChannelDetails(
			ObjectClusterSensorName.EULER_9DOF_Z_LN, //ObjectClusterName
			ObjectClusterSensorName.EULER_9DOF_Z_LN, //GUI friendly text to display
			ObjectClusterSensorName.EULER_9DOF_Z_LN, //database name
			CHANNEL_UNITS.NO_UNITS,
			Arrays.asList(CHANNEL_TYPE.CAL));
	
	private static final ChannelDetails channelQuatW_9DOF_LN = new ChannelDetails(
			ObjectClusterSensorName.QUAT_MADGE_9DOF_W_LN,
			ObjectClusterSensorName.QUAT_MADGE_9DOF_W_LN,
			ObjectClusterSensorName.QUAT_MADGE_9DOF_W_LN, //database name
			CHANNEL_UNITS.NO_UNITS,
			Arrays.asList(CHANNEL_TYPE.CAL));
	
	private static final ChannelDetails channelQuatX_9DOF_LN = new ChannelDetails(
			ObjectClusterSensorName.QUAT_MADGE_9DOF_X_LN,
			ObjectClusterSensorName.QUAT_MADGE_9DOF_X_LN,
			ObjectClusterSensorName.QUAT_MADGE_9DOF_X_LN, //database name
			CHANNEL_UNITS.NO_UNITS,
			Arrays.asList(CHANNEL_TYPE.CAL));
	
	private static final ChannelDetails channelQuatY_9DOF_LN = new ChannelDetails(
			ObjectClusterSensorName.QUAT_MADGE_9DOF_Y_LN,
			ObjectClusterSensorName.QUAT_MADGE_9DOF_Y_LN,
			ObjectClusterSensorName.QUAT_MADGE_9DOF_Y_LN, //database name
			CHANNEL_UNITS.NO_UNITS,
			Arrays.asList(CHANNEL_TYPE.CAL));
	
	private static final ChannelDetails channelQuatZ_9DOF_LN = new ChannelDetails(
			ObjectClusterSensorName.QUAT_MADGE_9DOF_Z_LN,
			ObjectClusterSensorName.QUAT_MADGE_9DOF_Z_LN,
			ObjectClusterSensorName.QUAT_MADGE_9DOF_Z_LN, //database name
			CHANNEL_UNITS.NO_UNITS,
			Arrays.asList(CHANNEL_TYPE.CAL));
	
	
	//9DOF wide range 
	private static final ChannelDetails channelAngleA_9DOF_WR = new ChannelDetails(
			ObjectClusterSensorName.EULER_9DOF_A_WR, //ObjectClusterName
			ObjectClusterSensorName.EULER_9DOF_A_WR, //GUI friendly text to display
			ObjectClusterSensorName.EULER_9DOF_A_WR, //database name
			CHANNEL_UNITS.NO_UNITS,
			Arrays.asList(CHANNEL_TYPE.CAL));
	
	private static final ChannelDetails channelAngleX_9DOF_WR = new ChannelDetails(
			ObjectClusterSensorName.EULER_9DOF_X_WR, //ObjectClusterName
			ObjectClusterSensorName.EULER_9DOF_X_WR, //GUI friendly text to display
			ObjectClusterSensorName.EULER_9DOF_X_WR, //database name
			CHANNEL_UNITS.NO_UNITS,
			Arrays.asList(CHANNEL_TYPE.CAL));
	
	private static final ChannelDetails channelAngleY_9DOF_WR = new ChannelDetails(
			ObjectClusterSensorName.EULER_9DOF_Y_WR, //ObjectClusterName
			ObjectClusterSensorName.EULER_9DOF_Y_WR, //GUI friendly text to display
			ObjectClusterSensorName.EULER_9DOF_Y_WR, //database name
			CHANNEL_UNITS.NO_UNITS,
			Arrays.asList(CHANNEL_TYPE.CAL));
	
	private static final ChannelDetails channelAngleZ_9DOF_WR = new ChannelDetails(
			ObjectClusterSensorName.EULER_9DOF_Z_WR, //ObjectClusterName
			ObjectClusterSensorName.EULER_9DOF_Z_WR, //GUI friendly text to display
			ObjectClusterSensorName.EULER_9DOF_Z_WR, //database name
			CHANNEL_UNITS.NO_UNITS,
			Arrays.asList(CHANNEL_TYPE.CAL));
	
	private static final ChannelDetails channelQuatW_9DOF_WR = new ChannelDetails(
			ObjectClusterSensorName.QUAT_MADGE_9DOF_W_WR,
			ObjectClusterSensorName.QUAT_MADGE_9DOF_W_WR,
			ObjectClusterSensorName.QUAT_MADGE_9DOF_W_WR, //database name
			CHANNEL_UNITS.NO_UNITS,
			Arrays.asList(CHANNEL_TYPE.CAL));
	
	private static final ChannelDetails channelQuatX_9DOF_WR = new ChannelDetails(
			ObjectClusterSensorName.QUAT_MADGE_9DOF_X_WR,
			ObjectClusterSensorName.QUAT_MADGE_9DOF_X_WR,
			ObjectClusterSensorName.QUAT_MADGE_9DOF_X_WR, //database name
			CHANNEL_UNITS.NO_UNITS,
			Arrays.asList(CHANNEL_TYPE.CAL));
	
	private static final ChannelDetails channelQuatY_9DOF_WR = new ChannelDetails(
			ObjectClusterSensorName.QUAT_MADGE_9DOF_Y_WR,
			ObjectClusterSensorName.QUAT_MADGE_9DOF_Y_WR,
			ObjectClusterSensorName.QUAT_MADGE_9DOF_Y_WR, //database name
			CHANNEL_UNITS.NO_UNITS,
			Arrays.asList(CHANNEL_TYPE.CAL));
	
	private static final ChannelDetails channelQuatZ_9DOF_WR = new ChannelDetails(
			ObjectClusterSensorName.QUAT_MADGE_9DOF_Z_WR,
			ObjectClusterSensorName.QUAT_MADGE_9DOF_Z_WR,
			ObjectClusterSensorName.QUAT_MADGE_9DOF_Z_WR, //database name
			CHANNEL_UNITS.NO_UNITS,
			Arrays.asList(CHANNEL_TYPE.CAL));
	
	//9 DOF channel groups
	 public static List<ChannelDetails> listChannelsQuat9DOF_WR = Arrays.asList(
			 channelQuatW_9DOF_WR, channelQuatX_9DOF_WR, channelQuatY_9DOF_WR, channelQuatZ_9DOF_WR);
	 
	 public static List<ChannelDetails> listChannelsEuler9DOF_WR = Arrays.asList(
			 channelAngleA_9DOF_WR, channelAngleX_9DOF_WR, channelAngleY_9DOF_WR, channelAngleZ_9DOF_WR);

	 public static List<ChannelDetails> listChannelsEuler9DOF_LN = Arrays.asList(
			 channelAngleA_9DOF_LN, channelAngleX_9DOF_LN, channelAngleY_9DOF_LN, channelAngleZ_9DOF_LN);
	 
	 public static List<ChannelDetails> listChannelsQuat9DOF_LN = Arrays.asList(
			 channelQuatW_9DOF_LN, channelQuatX_9DOF_LN, channelQuatY_9DOF_LN, channelQuatZ_9DOF_LN);

	 
	 
	public static final AlgorithmDetails algo9DoFOrientation_LN_Acc = new AlgorithmDetails(
			AlgorithmName.ORIENTATION_9DOF_LN, 
			"Low-Noise Accel", 
			Arrays.asList(Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_LN_X,
					Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_LN_Y,
					Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_LN_Z,
					Configuration.Shimmer3.ObjectClusterSensorName.MAG_X,
					Configuration.Shimmer3.ObjectClusterSensorName.MAG_Y,
					Configuration.Shimmer3.ObjectClusterSensorName.MAG_Z,
					Configuration.Shimmer3.ObjectClusterSensorName.GYRO_X,
					Configuration.Shimmer3.ObjectClusterSensorName.GYRO_Y,
					Configuration.Shimmer3.ObjectClusterSensorName.GYRO_Z),
//					Configuration.Shimmer3.GuiLabelAlgorithmGrouping.ORIENTATION_9DOF.getTileText(),
			(DerivedSensorsBitMask.ORIENTATION_9DOF_LN_QUAT|DerivedSensorsBitMask.ORIENTATION_9DOF_LN_EULER), 
			Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_A_ACCEL,
					Configuration.Shimmer3.SensorMapKey.SHIMMER_LSM303DLHC_MAG,
					Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_GYRO),
			CHANNEL_UNITS.NO_UNITS,
			listChannelsEuler9DOF_LN);
	
	public static final AlgorithmDetails algo9DoFOrientation_WR_Acc = new AlgorithmDetails(
			AlgorithmName.ORIENTATION_9DOF_WR, 
			"Wide-Noise Accel", 
			Arrays.asList(Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_WR_X,
					Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_WR_Y,
					Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_WR_Z,
					Configuration.Shimmer3.ObjectClusterSensorName.MAG_X,
					Configuration.Shimmer3.ObjectClusterSensorName.MAG_Y,
					Configuration.Shimmer3.ObjectClusterSensorName.MAG_Z,
					Configuration.Shimmer3.ObjectClusterSensorName.GYRO_X,
					Configuration.Shimmer3.ObjectClusterSensorName.GYRO_Y,
					Configuration.Shimmer3.ObjectClusterSensorName.GYRO_Z),
//					Configuration.Shimmer3.GuiLabelAlgorithmGrouping.ORIENTATION_9DOF.getTileText(),
			(DerivedSensorsBitMask.ORIENTATION_9DOF_WR_QUAT|DerivedSensorsBitMask.ORIENTATION_9DOF_WR_EULER), 
			Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_LSM303DLHC_ACCEL,
					Configuration.Shimmer3.SensorMapKey.SHIMMER_LSM303DLHC_MAG,
					Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_GYRO),
			CHANNEL_UNITS.NO_UNITS,
			listChannelsQuat9DOF_WR);
		
	
	public static final Map<String, AlgorithmDetails> mAlgorithmMapRef;
    static {
        Map<String, AlgorithmDetails> aMap = new LinkedHashMap<String, AlgorithmDetails>();
        aMap.put(algo9DoFOrientation_LN_Acc.mAlgorithmName, algo9DoFOrientation_LN_Acc);
        aMap.put(algo9DoFOrientation_WR_Acc.mAlgorithmName, algo9DoFOrientation_WR_Acc);
		mAlgorithmMapRef = Collections.unmodifiableMap(aMap);
    }
    
	// ------------------- Algorithms grouping map start -----------------------
	private static final SensorGroupingDetails sGD9Dof = new SensorGroupingDetails(
			Configuration.Shimmer3.GuiLabelAlgorithmGrouping.ORIENTATION_9DOF.getTileText(), 
			Arrays.asList(OrientationModule.algo9DoFOrientation_LN_Acc,
					OrientationModule.algo9DoFOrientation_WR_Acc),
			Arrays.asList(GuiLabelConfig.QUATERNION_OUTPUT,
					GuiLabelConfig.EULER_OUTPUT),
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
		
		mConfigOptionsMap.put(GuiLabelConfig.QUATERNION_OUTPUT, configOptionQuatOutput);
		mConfigOptionsMap.put(GuiLabelConfig.EULER_OUTPUT, configOptionEulerOutput);
		
		mAlgorithmGroupingMap.put(Configuration.Shimmer3.GuiLabelAlgorithmGrouping.ORIENTATION_9DOF.ordinal(), sGD9Dof);
	}
	
	public OrientationModule(AlgorithmDetails algorithmDetails, double samplingRate) {
		mAlgorithmDetails = algorithmDetails;
		mAlgorithmType = ALGORITHM_TYPE.ALGORITHM_TYPE_CONTINUOUS;
		mAlgorithmResultType = ALGORITHM_RESULT_TYPE.ALGORITHM_RESULT_TYPE_SINGLE_OBJECT_CLUSTER;
		mAlgorithmName = algorithmDetails.mAlgorithmName;
		mAlgorithmGroupingName = algorithmDetails.mAlgorithmName;
		
		this.samplingRate = samplingRate;
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

			case(GuiLabelConfigCommon.SAMPLING_RATE):
				returnValue = getSamplingRate();
			break;
			case(GuiLabelConfig.ACCELEROMETER):
				returnValue = getAccelerometer();
			break;
			case(GuiLabelConfig.QUATERNION_OUTPUT):
				returnValue = isQuaternionOutput();
			break;
			case(GuiLabelConfig.EULER_OUTPUT):
				returnValue = isEulerOutput();
			break;
		}
		return returnValue;
	}
	
	
	@Override
	public void setIsEnabled(boolean isEnabled) {
		mIsEnabled = isEnabled;
		if(mIsEnabled){
			if(!isQuaternionOutput() && !isEulerOutput()){
				setQuaternionOutput(true);
//				setEulerOutput(false);
			}
		}
		else {
			setQuaternionOutput(false);
			setEulerOutput(false);
		}
	}

	@Override
	public Object getDefaultSettings(String componentName) {
		Object returnValue = null;
		switch(componentName){
			case(GuiLabelConfigCommon.SAMPLING_RATE):
				returnValue = 512;
				break;
			case(GuiLabelConfig.ACCELEROMETER):
				returnValue = Shimmer3.GuiLabelSensorTiles.LOW_NOISE_ACCEL;
				break;
			case(GuiLabelConfig.QUATERNION_OUTPUT):
				returnValue = true;
				break;
			case(GuiLabelConfig.EULER_OUTPUT):
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
					setSamplingRate(Double.parseDouble((String) valueToSet));
				}
				else if(valueToSet instanceof Double){
					setSamplingRate((Double) valueToSet);
				}
				break;
			case(GuiLabelConfig.ACCELEROMETER):
				setAccelerometer((String) valueToSet);
				break;
			case(GuiLabelConfig.QUATERNION_OUTPUT):
				if(valueToSet instanceof Boolean){
					setQuaternionOutput((boolean) valueToSet);
				}
				else if(valueToSet instanceof Integer){
					setQuaternionOutput(((Integer) valueToSet)>0? true:false);
				}
				break;
			case(GuiLabelConfig.EULER_OUTPUT):
				if(valueToSet instanceof Boolean){
					setEulerOutput((boolean) valueToSet);
				}
				else if(valueToSet instanceof Integer){
					setEulerOutput(((Integer) valueToSet)>0? true:false);
				}
				break;
		}
	}

	@Override
	public AlgorithmResultObject processDataRealTime(ObjectCluster object) throws Exception {

		accValues = new Vector3d();
		magValues = new Vector3d();
		gyroValues = new Vector3d();

		for(String associatedChannel:mAlgorithmDetails.mListOfAssociatedSensors){
			Collection<FormatCluster> dataFormatsSignal = object.getCollectionOfFormatClusters(associatedChannel);  // first retrieve all the possible formats for the current sensor device
			if(dataFormatsSignal!=null){
				FormatCluster formatClusterSignal = ((FormatCluster)ObjectCluster.returnFormatCluster(dataFormatsSignal,mAlgorithmDetails.mChannelType.toString())); // retrieve the calibrated data
				double value = formatClusterSignal.mData;
				if(!Double.isNaN(value)){	
					setChannelValue(associatedChannel, value);
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
		
		if(channelName.equals(Configuration.Shimmer3.ObjectClusterSensorName.MAG_X)){
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
		else {
			if(mAlgorithmName.equals(AlgorithmName.ORIENTATION_9DOF_LN)){
				if(channelName.equals(Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_LN_X)){
					accValues.x = value;
				}
				else if(channelName.equals(Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_LN_Y)){
					accValues.y = value;
				}
				else if(channelName.equals(Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_LN_Z)){
					accValues.z = value;
				}
			}
			else if(mAlgorithmName.equals(AlgorithmName.ORIENTATION_9DOF_WR)){
				if(channelName.equals(Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_WR_X)){
					accValues.x = value;
				}
				else if(channelName.equals(Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_WR_Y)){
					accValues.y = value;
				}
				else if(channelName.equals(Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_WR_Z)){
					accValues.z = value;
				}
			}			
		}

	}	
	
	private ObjectCluster addQuaternionToObjectCluster(Orientation3DObject quaternion, ObjectCluster objectCluster){
		
		if(mAlgorithmName.equals(AlgorithmName.ORIENTATION_9DOF_LN)){
			if(eulerOutput){
				objectCluster.addData(ObjectClusterSensorName.EULER_9DOF_A_LN,CHANNEL_TYPE.CAL,CHANNEL_UNITS.NO_UNITS,quaternion.getTheta());
				objectCluster.addData(ObjectClusterSensorName.EULER_9DOF_X_LN,CHANNEL_TYPE.CAL,CHANNEL_UNITS.NO_UNITS,quaternion.getAngleX());
				objectCluster.addData(ObjectClusterSensorName.EULER_9DOF_Y_LN,CHANNEL_TYPE.CAL,CHANNEL_UNITS.NO_UNITS,quaternion.getAngleY());
				objectCluster.addData(ObjectClusterSensorName.EULER_9DOF_Z_LN,CHANNEL_TYPE.CAL,CHANNEL_UNITS.NO_UNITS,quaternion.getAngleZ());
			}
			if(quaternionOutput){
				objectCluster.addData(ObjectClusterSensorName.QUAT_MADGE_9DOF_W_LN,CHANNEL_TYPE.CAL,CHANNEL_UNITS.NO_UNITS,quaternion.getQuaternionW());
				objectCluster.addData(ObjectClusterSensorName.QUAT_MADGE_9DOF_X_LN,CHANNEL_TYPE.CAL,CHANNEL_UNITS.NO_UNITS,quaternion.getQuaternionX());
				objectCluster.addData(ObjectClusterSensorName.QUAT_MADGE_9DOF_Y_LN,CHANNEL_TYPE.CAL,CHANNEL_UNITS.NO_UNITS,quaternion.getQuaternionY());
				objectCluster.addData(ObjectClusterSensorName.QUAT_MADGE_9DOF_Z_LN,CHANNEL_TYPE.CAL,CHANNEL_UNITS.NO_UNITS,quaternion.getQuaternionZ());
			}
		}
		else if(mAlgorithmName.equals(AlgorithmName.ORIENTATION_9DOF_WR)){
			if(eulerOutput){
				objectCluster.addData(ObjectClusterSensorName.EULER_9DOF_A_WR,CHANNEL_TYPE.CAL,CHANNEL_UNITS.NO_UNITS,quaternion.getTheta());
				objectCluster.addData(ObjectClusterSensorName.EULER_9DOF_X_WR,CHANNEL_TYPE.CAL,CHANNEL_UNITS.NO_UNITS,quaternion.getAngleX());
				objectCluster.addData(ObjectClusterSensorName.EULER_9DOF_Y_WR,CHANNEL_TYPE.CAL,CHANNEL_UNITS.NO_UNITS,quaternion.getAngleY());
				objectCluster.addData(ObjectClusterSensorName.EULER_9DOF_Z_WR,CHANNEL_TYPE.CAL,CHANNEL_UNITS.NO_UNITS,quaternion.getAngleZ());
			}
			if(quaternionOutput){
				objectCluster.addData(ObjectClusterSensorName.QUAT_MADGE_9DOF_W_WR,CHANNEL_TYPE.CAL,CHANNEL_UNITS.NO_UNITS,quaternion.getQuaternionW());
				objectCluster.addData(ObjectClusterSensorName.QUAT_MADGE_9DOF_X_WR,CHANNEL_TYPE.CAL,CHANNEL_UNITS.NO_UNITS,quaternion.getQuaternionX());
				objectCluster.addData(ObjectClusterSensorName.QUAT_MADGE_9DOF_Y_WR,CHANNEL_TYPE.CAL,CHANNEL_UNITS.NO_UNITS,quaternion.getQuaternionY());
				objectCluster.addData(ObjectClusterSensorName.QUAT_MADGE_9DOF_Z_WR,CHANNEL_TYPE.CAL,CHANNEL_UNITS.NO_UNITS,quaternion.getQuaternionZ());
			}
		}
		
		return objectCluster;
	}
	
	private Orientation3DObject applyOrientationAlgorithm(){
		
		Orientation3DObject quaternion;
			quaternion = ((GradDes3DOrientation) orientationAlgorithm).update(
					accValues.x, accValues.y, accValues.z, 
					gyroValues.x, gyroValues.y, gyroValues.z
					,magValues.x, magValues.y, magValues.z);
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
		
		double samplingPeriod = 1/samplingRate;
		
		if(mAlgorithmName.equals(AlgorithmName.ORIENTATION_9DOF_LN)
				||mAlgorithmName.equals(AlgorithmName.ORIENTATION_9DOF_WR)){
			orientationAlgorithm = new GradDes3DOrientation(BETA, samplingPeriod, Q1, Q2, Q3, Q4);
		}
		
		if(mAlgorithmName.equals(AlgorithmName.ORIENTATION_9DOF_LN)){
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
		return samplingRate;
	}
	
	public String getAccelerometer(){
		return accelerometerSensor;
	}
	
	public void setSamplingRate(double sampleRate){
		this.samplingRate = sampleRate;
		try {
			initialize();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
		setQuaternionOutput(false);
		setEulerOutput(false);
		
		if(mAlgorithmDetails.mAlgorithmName.equals(AlgorithmName.ORIENTATION_9DOF_LN)){
			if((derivedSensorBitmapID&DerivedSensorsBitMask.ORIENTATION_9DOF_LN_QUAT)>0){
				setQuaternionOutput(true);
			}
			if((derivedSensorBitmapID&DerivedSensorsBitMask.ORIENTATION_9DOF_LN_EULER)>0){
				setEulerOutput(true);
			}
		}
		else if(mAlgorithmDetails.mAlgorithmName.equals(AlgorithmName.ORIENTATION_9DOF_WR)){
			if((derivedSensorBitmapID&DerivedSensorsBitMask.ORIENTATION_9DOF_WR_QUAT)>0){
				setQuaternionOutput(true);
			}
			if((derivedSensorBitmapID&DerivedSensorsBitMask.ORIENTATION_9DOF_WR_EULER)>0){
				setEulerOutput(true);
			}
		}
		
		setIsEnabled(isEulerOutput()||isQuaternionOutput());
	}
	
	@Override
	public long getDerivedSensorBitmapID() {
		long bitmask = 0;
		if(mAlgorithmDetails!=null && isEnabled()){
			if(mAlgorithmDetails.mAlgorithmName.equals(AlgorithmName.ORIENTATION_9DOF_LN)){
				if(isQuaternionOutput()){
					bitmask |= DerivedSensorsBitMask.ORIENTATION_9DOF_LN_QUAT;
				}
				if(isEulerOutput()){
					bitmask |= DerivedSensorsBitMask.ORIENTATION_9DOF_LN_EULER;
				}
			}
			else if(mAlgorithmDetails.mAlgorithmName.equals(AlgorithmName.ORIENTATION_9DOF_WR)){
				if(isQuaternionOutput()){
					bitmask |= DerivedSensorsBitMask.ORIENTATION_9DOF_WR_QUAT;
				}
				if(isEulerOutput()){
					bitmask |= DerivedSensorsBitMask.ORIENTATION_9DOF_WR_EULER;
				}
			}
		}
		
		//bitmask = DerivedSensorsBitMask.ORIENTATION_6DOF_LN_QUAT | DerivedSensorsBitMask.ORIENTATION_9DOF_LN_EULER ;
		
		return bitmask;
	}
	
	
	@Override
	public List<ChannelDetails> getChannelDetails() {
		
		List<ChannelDetails> listOfChannelDetails = new ArrayList<ChannelDetails>();
		
		if(mAlgorithmDetails.mAlgorithmName.equals(AlgorithmName.ORIENTATION_9DOF_LN)){
			if(isQuaternionOutput()){
				listOfChannelDetails.addAll(listChannelsQuat9DOF_LN);
			}
			if(isEulerOutput()){
				listOfChannelDetails.addAll(listChannelsEuler9DOF_LN);
			}
		}
		else if(mAlgorithmDetails.mAlgorithmName.equals(AlgorithmName.ORIENTATION_9DOF_WR)){
			if(isQuaternionOutput()){
				listOfChannelDetails.addAll(listChannelsQuat9DOF_WR);
			}
			if(isEulerOutput()){
				listOfChannelDetails.addAll(listChannelsEuler9DOF_WR);
			}
		}
//		return super.getChannelDetails();
		return listOfChannelDetails;
	}
	


}
