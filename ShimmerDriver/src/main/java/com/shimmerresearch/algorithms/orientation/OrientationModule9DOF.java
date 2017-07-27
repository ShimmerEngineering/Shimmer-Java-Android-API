package com.shimmerresearch.algorithms.orientation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.shimmerresearch.algorithms.AlgorithmDetails;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.Configuration.CHANNEL_UNITS;
import com.shimmerresearch.driver.Configuration.Shimmer3.DerivedSensorsBitMask;
import com.shimmerresearch.driverUtilities.ChannelDetails;
import com.shimmerresearch.driverUtilities.SensorGroupingDetails;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_TYPE;
import com.shimmerresearch.driverUtilities.ConfigOptionDetails;
import com.shimmerresearch.driverUtilities.ConfigOptionDetails.GUI_COMPONENT_TYPE;

public class OrientationModule9DOF extends OrientationModule {

	/** * */
	private static final long serialVersionUID = -4174847829978293223L;

	//TODO update objectcluster name with previously aggreed name
	public static class AlgorithmName{
		public static final String ORIENTATION_9DOF_LN = "LN_Acc_9DoF";
		public static final String ORIENTATION_9DOF_WR = "WR_Acc_9DoF";
	}	

	public class GuiLabelConfig{
		public static final String ACCELEROMETER_9DOF = "Accelerometer_9DOF";
		public static final String QUATERNION_OUTPUT_9DOF = "Quaternion_9DOF";
		public static final String EULER_OUTPUT_9DOF = "Euler_9DOF";
	}

	transient Object orientationAlgorithm;

	public static final String WR = "_WR";
	public static final String LN = "_LN";

	public static final class ObjectClusterSensorName{
		public static final String QUAT_MADGE_9DOF_W = "Quat_Madge_9DOF_W";//Configuration.Shimmer3.ObjectClusterSensorName.QUAT_MADGE_9DOF_W; 
		public static final String QUAT_MADGE_9DOF_W_LN = QUAT_MADGE_9DOF_W + LN; 
		public static final String QUAT_MADGE_9DOF_W_WR = QUAT_MADGE_9DOF_W + WR; 
		public static final String QUAT_MADGE_9DOF_X = "Quat_Madge_9DOF_X";//Configuration.Shimmer3.ObjectClusterSensorName.QUAT_MADGE_9DOF_X; 
		public static final String QUAT_MADGE_9DOF_X_LN = QUAT_MADGE_9DOF_X + LN; 
		public static final String QUAT_MADGE_9DOF_X_WR = QUAT_MADGE_9DOF_X + WR; 
		public static final String QUAT_MADGE_9DOF_Y = "Quat_Madge_9DOF_Y";//Configuration.Shimmer3.ObjectClusterSensorName.QUAT_MADGE_9DOF_Y; 
		public static final String QUAT_MADGE_9DOF_Y_LN = QUAT_MADGE_9DOF_Y + LN; 
		public static final String QUAT_MADGE_9DOF_Y_WR = QUAT_MADGE_9DOF_Y + WR; 
		public static final String QUAT_MADGE_9DOF_Z = "Quat_Madge_9DOF_Z";//Configuration.Shimmer3.ObjectClusterSensorName.QUAT_MADGE_9DOF_Z; 
		public static final String QUAT_MADGE_9DOF_Z_LN = QUAT_MADGE_9DOF_Z + LN; 
		public static final String QUAT_MADGE_9DOF_Z_WR = QUAT_MADGE_9DOF_Z + WR; 

		public static final String AXIS_ANGLE_9DOF_A = "Axis_Angle_9DOF_A";//Configuration.Shimmer3.ObjectClusterSensorName.AXIS_ANGLE_9DOF_A; 
		public static final String AXIS_ANGLE_9DOF_A_LN = AXIS_ANGLE_9DOF_A + LN; 
		public static final String AXIS_ANGLE_9DOF_A_WR = AXIS_ANGLE_9DOF_A + WR; 
		public static final String AXIS_ANGLE_9DOF_X = "Axis_Angle_9DOF_X";//Configuration.Shimmer3.ObjectClusterSensorName.AXIS_ANGLE_9DOF_X; 
		public static final String AXIS_ANGLE_9DOF_X_LN = AXIS_ANGLE_9DOF_X + LN; 
		public static final String AXIS_ANGLE_9DOF_X_WR = AXIS_ANGLE_9DOF_X + WR; 
		public static final String AXIS_ANGLE_9DOF_Y = "Axis_Angle_9DOF_Y";//Configuration.Shimmer3.ObjectClusterSensorName.AXIS_ANGLE_9DOF_Y; 
		public static final String AXIS_ANGLE_9DOF_Y_LN = AXIS_ANGLE_9DOF_Y + LN; 
		public static final String AXIS_ANGLE_9DOF_Y_WR = AXIS_ANGLE_9DOF_Y + WR; 
		public static final String AXIS_ANGLE_9DOF_Z = "Axis_Angle_9DOF_Z";//Configuration.Shimmer3.ObjectClusterSensorName.AXIS_ANGLE_9DOF_Z; 
		public static final String AXIS_ANGLE_9DOF_Z_LN = AXIS_ANGLE_9DOF_Z + LN; 
		public static final String AXIS_ANGLE_9DOF_Z_WR = AXIS_ANGLE_9DOF_Z + WR; 
		
		public static final String EULER_9DOF_YAW = "Euler_9DOF_Yaw";//Configuration.Shimmer3.ObjectClusterSensorName.EULER_9DOF_YAW; 
		public static final String EULER_9DOF_YAW_LN = EULER_9DOF_YAW + LN; 
		public static final String EULER_9DOF_YAW_WR = EULER_9DOF_YAW + WR; 
		public static final String EULER_9DOF_PITCH = "Euler_9DOF_Pitch";//Configuration.Shimmer3.ObjectClusterSensorName.EULER_9DOF_PITCH; 
		public static final String EULER_9DOF_PITCH_LN = EULER_9DOF_PITCH + LN; 
		public static final String EULER_9DOF_PITCH_WR = EULER_9DOF_PITCH + WR; 
		public static final String EULER_9DOF_ROLL = "Euler_9DOF_Roll";//Configuration.Shimmer3.ObjectClusterSensorName.EULER_9DOF_ROLL; 
		public static final String EULER_9DOF_ROLL_LN = EULER_9DOF_ROLL + LN; 
		public static final String EULER_9DOF_ROLL_WR = EULER_9DOF_ROLL + WR; 


//		@Deprecated //need to describe axis angle 9DOF vs 9DOF
//		public static final String AXIS_ANGLE_A = Configuration.Shimmer3.ObjectClusterSensorName.AXIS_ANGLE_A; 
//		@Deprecated //need to describe axis angle 9DOF vs 9DOF
//		public static final String AXIS_ANGLE_X = Configuration.Shimmer3.ObjectClusterSensorName.AXIS_ANGLE_X; 
//		@Deprecated //need to describe axis angle 9DOF vs 9DOF
//		public static final String AXIS_ANGLE_Y = Configuration.Shimmer3.ObjectClusterSensorName.AXIS_ANGLE_Y; 
//		@Deprecated //need to describe axis angle 9DOF vs 9DOF
//		public static final String AXIS_ANGLE_Z = Configuration.Shimmer3.ObjectClusterSensorName.AXIS_ANGLE_Z; 
	}
	
	public static class DatabaseChannelHandles{
		public static final String QUARTENION_W_9DOF = "QUAT_MADGE_9DOF_W";
		public static final String QUARTENION_X_9DOF = "QUAT_MADGE_9DOF_X";
		public static final String QUARTENION_Y_9DOF = "QUAT_MADGE_9DOF_Y";
		public static final String QUARTENION_Z_9DOF = "QUAT_MADGE_9DOF_Z";

		public static final String EULER_9DOF_YAW = "EULER_9DOF_YAW";
		public static final String EULER_9DOF_PITCH = "EULER_9DOF_PITCH";
		public static final String EULER_9DOF_ROLL = "EULER_9DOF_ROLL";
	}

	//TODO 9DOF channal details for low noise 
	private static final ChannelDetails channel_Euler_Yaw_9DOF_LN = new ChannelDetails(
			ObjectClusterSensorName.EULER_9DOF_YAW_LN, //ObjectClusterName
			ObjectClusterSensorName.EULER_9DOF_YAW_LN, //GUI friendly text to display
			ObjectClusterSensorName.EULER_9DOF_YAW_LN, //database name
			CHANNEL_UNITS.NO_UNITS,
			Arrays.asList(CHANNEL_TYPE.CAL));

	private static final ChannelDetails channel_Euler_Pitch_9DOF_LN = new ChannelDetails(
			ObjectClusterSensorName.EULER_9DOF_PITCH_LN, //ObjectClusterName
			ObjectClusterSensorName.EULER_9DOF_PITCH_LN, //GUI friendly text to display
			ObjectClusterSensorName.EULER_9DOF_PITCH_LN, //database name
			CHANNEL_UNITS.NO_UNITS,
			Arrays.asList(CHANNEL_TYPE.CAL));

	private static final ChannelDetails channel_Euler_Roll_9DOF_LN = new ChannelDetails(
			ObjectClusterSensorName.EULER_9DOF_ROLL_LN, //ObjectClusterName
			ObjectClusterSensorName.EULER_9DOF_ROLL_LN, //GUI friendly text to display
			ObjectClusterSensorName.EULER_9DOF_ROLL_LN, //database name
			CHANNEL_UNITS.NO_UNITS,
			Arrays.asList(CHANNEL_TYPE.CAL));
	
	private static final ChannelDetails channelAngleA_9DOF_LN = new ChannelDetails(
			ObjectClusterSensorName.AXIS_ANGLE_9DOF_A_LN, //ObjectClusterName
			ObjectClusterSensorName.AXIS_ANGLE_9DOF_A_LN, //GUI friendly text to display
			ObjectClusterSensorName.AXIS_ANGLE_9DOF_A_LN, //database name
			CHANNEL_UNITS.NO_UNITS,
			Arrays.asList(CHANNEL_TYPE.CAL));

	private static final ChannelDetails channelAngleX_9DOF_LN = new ChannelDetails(
			ObjectClusterSensorName.AXIS_ANGLE_9DOF_X_LN, //ObjectClusterName
			ObjectClusterSensorName.AXIS_ANGLE_9DOF_X_LN, //GUI friendly text to display
			ObjectClusterSensorName.AXIS_ANGLE_9DOF_X_LN, //database name
			CHANNEL_UNITS.NO_UNITS,
			Arrays.asList(CHANNEL_TYPE.CAL));

	private static final ChannelDetails channelAngleY_9DOF_LN = new ChannelDetails(
			ObjectClusterSensorName.AXIS_ANGLE_9DOF_Y_LN, //ObjectClusterName
			ObjectClusterSensorName.AXIS_ANGLE_9DOF_Y_LN, //GUI friendly text to display
			ObjectClusterSensorName.AXIS_ANGLE_9DOF_Y_LN, //database name
			CHANNEL_UNITS.NO_UNITS,
			Arrays.asList(CHANNEL_TYPE.CAL));

	private static final ChannelDetails channelAngleZ_9DOF_LN = new ChannelDetails(
			ObjectClusterSensorName.AXIS_ANGLE_9DOF_Z_LN, //ObjectClusterName
			ObjectClusterSensorName.AXIS_ANGLE_9DOF_Z_LN, //GUI friendly text to display
			ObjectClusterSensorName.AXIS_ANGLE_9DOF_Z_LN, //database name
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
	
	private static final ChannelDetails channel_Euler_Yaw_9DOF_WR = new ChannelDetails(
			ObjectClusterSensorName.EULER_9DOF_YAW_WR, //ObjectClusterName
			ObjectClusterSensorName.EULER_9DOF_YAW_WR, //GUI friendly text to display
			ObjectClusterSensorName.EULER_9DOF_YAW_WR, //database name
			CHANNEL_UNITS.NO_UNITS,
			Arrays.asList(CHANNEL_TYPE.CAL));

	private static final ChannelDetails channel_Euler_Pitch_9DOF_WR = new ChannelDetails(
			ObjectClusterSensorName.EULER_9DOF_PITCH_WR, //ObjectClusterName
			ObjectClusterSensorName.EULER_9DOF_PITCH_WR, //GUI friendly text to display
			ObjectClusterSensorName.EULER_9DOF_PITCH_WR, //database name
			CHANNEL_UNITS.NO_UNITS,
			Arrays.asList(CHANNEL_TYPE.CAL));

	private static final ChannelDetails channel_Euler_Roll_9DOF_WR = new ChannelDetails(
			ObjectClusterSensorName.EULER_9DOF_ROLL_WR, //ObjectClusterName
			ObjectClusterSensorName.EULER_9DOF_ROLL_WR, //GUI friendly text to display
			ObjectClusterSensorName.EULER_9DOF_ROLL_WR, //database name
			CHANNEL_UNITS.NO_UNITS,
			Arrays.asList(CHANNEL_TYPE.CAL));
	
	private static final ChannelDetails channelAngleA_9DOF_WR = new ChannelDetails(
			ObjectClusterSensorName.AXIS_ANGLE_9DOF_A_WR, //ObjectClusterName
			ObjectClusterSensorName.AXIS_ANGLE_9DOF_A_WR, //GUI friendly text to display
			ObjectClusterSensorName.AXIS_ANGLE_9DOF_A_WR, //database name
			CHANNEL_UNITS.NO_UNITS,
			Arrays.asList(CHANNEL_TYPE.CAL));

	private static final ChannelDetails channelAngleX_9DOF_WR = new ChannelDetails(
			ObjectClusterSensorName.AXIS_ANGLE_9DOF_X_WR, //ObjectClusterName
			ObjectClusterSensorName.AXIS_ANGLE_9DOF_X_WR, //GUI friendly text to display
			ObjectClusterSensorName.AXIS_ANGLE_9DOF_X_WR, //database name
			CHANNEL_UNITS.NO_UNITS,
			Arrays.asList(CHANNEL_TYPE.CAL));

	private static final ChannelDetails channelAngleY_9DOF_WR = new ChannelDetails(
			ObjectClusterSensorName.AXIS_ANGLE_9DOF_Y_WR, //ObjectClusterName
			ObjectClusterSensorName.AXIS_ANGLE_9DOF_Y_WR, //GUI friendly text to display
			ObjectClusterSensorName.AXIS_ANGLE_9DOF_Y_WR, //database name
			CHANNEL_UNITS.NO_UNITS,
			Arrays.asList(CHANNEL_TYPE.CAL));

	private static final ChannelDetails channelAngleZ_9DOF_WR = new ChannelDetails(
			ObjectClusterSensorName.AXIS_ANGLE_9DOF_Z_WR, //ObjectClusterName
			ObjectClusterSensorName.AXIS_ANGLE_9DOF_Z_WR, //GUI friendly text to display
			ObjectClusterSensorName.AXIS_ANGLE_9DOF_Z_WR, //database name
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


	//9DOF channel groups
	public static final List<ChannelDetails> listChannelsQuat9DOF_WR = Arrays.asList(
			channelQuatW_9DOF_WR, channelQuatX_9DOF_WR, channelQuatY_9DOF_WR, channelQuatZ_9DOF_WR);

	public static final List<ChannelDetails> listChannelsAxisAngle9DOF_LN = Arrays.asList(
			channelAngleA_9DOF_LN, channelAngleX_9DOF_LN, channelAngleY_9DOF_LN, channelAngleZ_9DOF_LN);

	public static final List<ChannelDetails> listChannelsQuat9DOF_LN = Arrays.asList(
			channelQuatW_9DOF_LN, channelQuatX_9DOF_LN, channelQuatY_9DOF_LN, channelQuatZ_9DOF_LN);

	public static final List<ChannelDetails> listChannelsAxisAngle9DOF_WR = Arrays.asList(
			channelAngleA_9DOF_WR, channelAngleX_9DOF_WR, channelAngleY_9DOF_WR, channelAngleZ_9DOF_WR);
	
	public static final List<ChannelDetails> listChannelsEuler9DOF_LN = Arrays.asList(
			channel_Euler_Yaw_9DOF_LN, channel_Euler_Pitch_9DOF_LN, channel_Euler_Roll_9DOF_LN);
	
	public static final List<ChannelDetails> listChannelsEuler9DOF_WR = Arrays.asList(
			channel_Euler_Yaw_9DOF_WR, channel_Euler_Pitch_9DOF_WR, channel_Euler_Roll_9DOF_WR);
	
	
	public static final AlgorithmDetails algo9DoFOrientation_LN_Acc = new AlgorithmDetails(
			AlgorithmName.ORIENTATION_9DOF_LN, 
			OrientationModule.GuiLabelConfig.ORIENTATAION_LN, 
			Arrays.asList(Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_LN_X,
					Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_LN_Y,
					Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_LN_Z,
					Configuration.Shimmer3.ObjectClusterSensorName.GYRO_X,
					Configuration.Shimmer3.ObjectClusterSensorName.GYRO_Y,
					Configuration.Shimmer3.ObjectClusterSensorName.GYRO_Z,
					Configuration.Shimmer3.ObjectClusterSensorName.MAG_X,
					Configuration.Shimmer3.ObjectClusterSensorName.MAG_Y,
					Configuration.Shimmer3.ObjectClusterSensorName.MAG_Z),
			(DerivedSensorsBitMask.ORIENTATION_9DOF_LN_QUAT|DerivedSensorsBitMask.ORIENTATION_9DOF_LN_EULER), 
			Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_ANALOG_ACCEL,
					Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9X50_GYRO,
					Configuration.Shimmer3.SensorMapKey.SHIMMER_LSM303_MAG),
					CHANNEL_UNITS.NO_UNITS,
					listChannelsEuler9DOF_LN);

	public static final AlgorithmDetails algo9DoFOrientation_WR_Acc = new AlgorithmDetails(
			AlgorithmName.ORIENTATION_9DOF_WR, 
			OrientationModule.GuiLabelConfig.ORIENTATAION_WR, 
			Arrays.asList(Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_WR_X,
					Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_WR_Y,
					Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_WR_Z,
					Configuration.Shimmer3.ObjectClusterSensorName.GYRO_X,
					Configuration.Shimmer3.ObjectClusterSensorName.GYRO_Y,
					Configuration.Shimmer3.ObjectClusterSensorName.GYRO_Z,
					Configuration.Shimmer3.ObjectClusterSensorName.MAG_X,
					Configuration.Shimmer3.ObjectClusterSensorName.MAG_Y,
					Configuration.Shimmer3.ObjectClusterSensorName.MAG_Z),
			(DerivedSensorsBitMask.ORIENTATION_9DOF_WR_QUAT|DerivedSensorsBitMask.ORIENTATION_9DOF_WR_EULER), 
			Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_LSM303_ACCEL,
					Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9X50_GYRO,
					Configuration.Shimmer3.SensorMapKey.SHIMMER_LSM303_MAG),
					CHANNEL_UNITS.NO_UNITS,
					listChannelsQuat9DOF_WR);


	public static final Map<String, AlgorithmDetails> mAlgorithmMapRef;
	static {
		Map<String, AlgorithmDetails> aMap = new LinkedHashMap<String, AlgorithmDetails>();
		aMap.put(algo9DoFOrientation_LN_Acc.mAlgorithmName, algo9DoFOrientation_LN_Acc);
		aMap.put(algo9DoFOrientation_WR_Acc.mAlgorithmName, algo9DoFOrientation_WR_Acc);
		mAlgorithmMapRef = Collections.unmodifiableMap(aMap);
	}

//	public static LinkedHashMap<String, AlgorithmDetails> getMapOfSupportedAlgorithms(ShimmerVerObject shimmerVerObject) {
//		LinkedHashMap<String, AlgorithmDetails> mapOfSupportedAlgorithms = new LinkedHashMap<String, AlgorithmDetails>();
//		//TODO Filter here depending on Shimmer version
//		mapOfSupportedAlgorithms.putAll(mAlgorithmMapRef);
//		return mapOfSupportedAlgorithms;
//	}


	// ------------------- Algorithms grouping map start -----------------------
	public static final SensorGroupingDetails sGD9Dof = new SensorGroupingDetails(
			Configuration.Shimmer3.GuiLabelAlgorithmGrouping.ORIENTATION_9DOF.getTileText(),
			Arrays.asList(OrientationModule9DOF.algo9DoFOrientation_LN_Acc,
					OrientationModule9DOF.algo9DoFOrientation_WR_Acc),
					Arrays.asList(GuiLabelConfig.QUATERNION_OUTPUT_9DOF,
							GuiLabelConfig.EULER_OUTPUT_9DOF),
							0);
	// ------------------- Algorithms grouping map end -----------------------

	{

		//			mConfigOptionsMap.put(SAMPLING_RATE,new AlgorithmConfigOptionDetails(AlgorithmConfigOptionDetails.GUI_COMPONENT_TYPE.TEXTFIELD,mListSVO));
		//			String[] accSensors = new String[2];
		//			accSensors[0]=Shimmer3.GuiLabelSensorTiles.LOW_NOISE_ACCEL;
		//			accSensors[1]=Shimmer3.GuiLabelSensorTiles.WIDE_RANGE_ACCEL;
		//			mConfigOptionsMap.put(ACCELEROMETER, new AlgorithmConfigOptionDetails(AlgorithmConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX, mListSVO, accSensors));
		final ConfigOptionDetails configOptionQuatOutput9DOF = new ConfigOptionDetails(
				OrientationModule.GuiLabelConfig.QUATERNION_OUTPUT,
				null,
				QUATERNION_OPTIONS, 
				GUI_COMPONENT_TYPE.COMBOBOX,
				null);

		final ConfigOptionDetails configOptionEulerOutput9DOF = new ConfigOptionDetails(
				OrientationModule.GuiLabelConfig.EULER_OUTPUT,
				null,
				EULER_OPTIONS, 
				GUI_COMPONENT_TYPE.COMBOBOX,
				null);


		mConfigOptionsMap.put(GuiLabelConfig.QUATERNION_OUTPUT_9DOF, configOptionQuatOutput9DOF);
		mConfigOptionsMap.put(GuiLabelConfig.EULER_OUTPUT_9DOF, configOptionEulerOutput9DOF);

		mMapOfAlgorithmGrouping.put(Configuration.Shimmer3.GuiLabelAlgorithmGrouping.ORIENTATION_9DOF.ordinal(), sGD9Dof);

	}

	public OrientationModule9DOF(AlgorithmDetails algorithmDetails, double samplingRate) {
		super(algorithmDetails);
		setupAlgorithm();

		this.samplingRate = samplingRate;
	}
	
	@Override
	public void setupAlgorithm() {
		super.setupAlgorithm();
		
		mAlgorithmType = ALGORITHM_TYPE.ALGORITHM_TYPE_CONTINUOUS;
		mAlgorithmResultType = ALGORITHM_RESULT_TYPE.ALGORITHM_RESULT_TYPE_SINGLE_OBJECT_CLUSTER;
		//TODO EN replace hardcoded
		//			mAlgorithmGroupingName = "9DOF";

		try {
			initialize();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	@Override
	public void setGeneralAlgorithmName() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setFilteringOption() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setMinSamplingRateForAlgorithm() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setSupportedVerInfo() {
		mListOfCompatibleSVO.add(svoSh3Module);
	}

	@Override
	public void generateConfigOptionsMap() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void generateAlgorithmGroupingMap() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void initialize() throws Exception {

		double samplingPeriod = 1/samplingRate;

		if(mAlgorithmName.equals(AlgorithmName.ORIENTATION_9DOF_LN)
				||mAlgorithmName.equals(AlgorithmName.ORIENTATION_9DOF_WR)){
			orientationType = ORIENTATION_TYPE.NINE_DOF;
//			orientationAlgorithm = new GradDes3DOrientation(BETA, samplingPeriod, Q1, Q2, Q3, Q4);
			orientationAlgorithm = new GradDes3DOrientation9DoF(samplingPeriod);
		}

		if(mAlgorithmName.equals(AlgorithmName.ORIENTATION_9DOF_LN)){
			setAccelerometer(OrientationModule.GuiLabelConfig.ORIENTATAION_LN);
		}
		else{
			setAccelerometer(OrientationModule.GuiLabelConfig.ORIENTATAION_WR);
		}
	}

	public Orientation3DObject applyOrientationAlgorithm(){

		Orientation3DObject quaternion;
		quaternion = ((GradDes3DOrientation9DoF) orientationAlgorithm).update(
				accValues.x, accValues.y, accValues.z, 
				gyroValues.x, gyroValues.y, gyroValues.z
				,magValues.x, magValues.y, magValues.z);

		return quaternion;
	}


	@Override
	public List<ChannelDetails> getChannelDetails(boolean showDisabledChannels) {

		List<ChannelDetails> listOfChannelDetails = new ArrayList<ChannelDetails>();

		if(mAlgorithmDetails.mAlgorithmName.equals(AlgorithmName.ORIENTATION_9DOF_WR)){
			if(showDisabledChannels || isQuaternionOutput()){
				listOfChannelDetails.addAll(listChannelsQuat9DOF_WR);
			}
			if(showDisabledChannels || isEulerOutput()){
				listOfChannelDetails.addAll(listChannelsEuler9DOF_WR);
			}
			if(showDisabledChannels || isAxisAngleOutput()){
				listOfChannelDetails.addAll(listChannelsAxisAngle9DOF_WR);
			}
		}
		else if(mAlgorithmDetails.mAlgorithmName.equals(AlgorithmName.ORIENTATION_9DOF_LN)){
			if(showDisabledChannels || isQuaternionOutput()){
				listOfChannelDetails.addAll(listChannelsQuat9DOF_LN);
			}
			if(showDisabledChannels || isEulerOutput()){
				listOfChannelDetails.addAll(listChannelsEuler9DOF_LN);
			}
			if(showDisabledChannels || isAxisAngleOutput()){
				listOfChannelDetails.addAll(listChannelsAxisAngle9DOF_LN);
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

		case(GuiLabelConfigCommon.SHIMMER_SAMPLING_RATE):
			returnValue = getShimmerSamplingRate();
		break;
		//				case(GuiLabelConfig.ACCELEROMETER_9DOF):
		//					returnValue = getAccelerometer();
		//				break;
		case(GuiLabelConfig.QUATERNION_OUTPUT_9DOF):
			returnValue = isQuaternionOutput();
		break;
		case(GuiLabelConfig.EULER_OUTPUT_9DOF):
			returnValue = isEulerOutput();
		break;
		}
		return returnValue;
	}

	@Override
	public Object getDefaultSettings(String componentName) {
		Object returnValue = null;
		switch(componentName){
		case(GuiLabelConfigCommon.SHIMMER_SAMPLING_RATE):
			returnValue = 512;
		break;
		//				case(GuiLabelConfig.ACCELEROMETER_9DOF):
		//					returnValue = Shimmer3.GuiLabelSensorTiles.LOW_NOISE_ACCEL;
		//					break;
		case(GuiLabelConfig.QUATERNION_OUTPUT_9DOF):
			returnValue = true;
		break;
		case(GuiLabelConfig.EULER_OUTPUT_9DOF):
			returnValue = false;
		break;
		}
		return returnValue;
	}

	@Override
	public void setSettings(String componentName, Object valueToSet){

		switch(componentName){
		case(GuiLabelConfigCommon.SHIMMER_SAMPLING_RATE):
			if(valueToSet instanceof String){
				if(!((String) valueToSet).isEmpty()){
					setShimmerSamplingRate(Double.parseDouble((String) valueToSet));
				}
			}
			else if(valueToSet instanceof Double){
				setShimmerSamplingRate((Double) valueToSet);
			}
		break;
		//				case(GuiLabelConfig.ACCELEROMETER_9DOF):
		//					setAccelerometer((String) valueToSet);
		//					break;
		case(GuiLabelConfig.QUATERNION_OUTPUT_9DOF):
			if(valueToSet instanceof Boolean){
				setQuaternionOutput((boolean) valueToSet);
			}
			else if(valueToSet instanceof Integer){
				setQuaternionOutput(((Integer) valueToSet)>0? true:false);
			}
		break;
		case(GuiLabelConfig.EULER_OUTPUT_9DOF):
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
	public void algorithmMapUpdateFromEnabledSensorsVars(long derivedSensorBitmapID) {
		setQuaternionOutput(false);
		setEulerOutput(false);
		setAxisAngleOutput(false);
		setIsEnabled(false);
		
		// This is necessary because all 9DOF modules need to have synced
		// booleans for enabling Quaternion/Euler outputs for the Consensys implementation. 
		if(((derivedSensorBitmapID&DerivedSensorsBitMask.ORIENTATION_9DOF_LN_QUAT)>0)
			||(derivedSensorBitmapID&DerivedSensorsBitMask.ORIENTATION_9DOF_WR_QUAT)>0){
			setQuaternionOutput(true);
		}
		if(((derivedSensorBitmapID&DerivedSensorsBitMask.ORIENTATION_9DOF_LN_EULER)>0)
			||(derivedSensorBitmapID&DerivedSensorsBitMask.ORIENTATION_9DOF_WR_EULER)>0){
			setEulerOutput(true);
		}
		
		if(mAlgorithmDetails.mAlgorithmName.equals(AlgorithmName.ORIENTATION_9DOF_LN)){
			if(((derivedSensorBitmapID&DerivedSensorsBitMask.ORIENTATION_9DOF_LN_QUAT)>0)
				||(derivedSensorBitmapID&DerivedSensorsBitMask.ORIENTATION_9DOF_LN_EULER)>0){
				setIsEnabled(true);
			}
		}
		else if(mAlgorithmDetails.mAlgorithmName.equals(AlgorithmName.ORIENTATION_9DOF_WR)){
			if(((derivedSensorBitmapID&DerivedSensorsBitMask.ORIENTATION_9DOF_WR_QUAT)>0)
				||(derivedSensorBitmapID&DerivedSensorsBitMask.ORIENTATION_9DOF_WR_EULER)>0){
				setIsEnabled(true);
			}
		}
	}

	@Override
	public LinkedHashMap<String, Object> getConfigMapForDb() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void parseConfigMapFromDb(LinkedHashMap<String, Object> mapOfConfigPerShimmer) {
		// TODO Auto-generated method stub
		
	}

	public ObjectCluster addQuaternionToObjectCluster(Orientation3DObject quaternion, ObjectCluster objectCluster){

		if(mAlgorithmName.equals(AlgorithmName.ORIENTATION_9DOF_LN)){
			if(isEulerOutput()){
				objectCluster.addData(ObjectClusterSensorName.EULER_9DOF_YAW_LN,CHANNEL_TYPE.CAL,CHANNEL_UNITS.NO_UNITS,quaternion.getYaw());
				objectCluster.addData(ObjectClusterSensorName.EULER_9DOF_PITCH_LN,CHANNEL_TYPE.CAL,CHANNEL_UNITS.NO_UNITS,quaternion.getPitch());
				objectCluster.addData(ObjectClusterSensorName.EULER_9DOF_ROLL_LN,CHANNEL_TYPE.CAL,CHANNEL_UNITS.NO_UNITS,quaternion.getRoll());
			}
			if(isQuaternionOutput()){
				objectCluster.addData(ObjectClusterSensorName.QUAT_MADGE_9DOF_W_LN,CHANNEL_TYPE.CAL,CHANNEL_UNITS.NO_UNITS,quaternion.getQuaternionW());
				objectCluster.addData(ObjectClusterSensorName.QUAT_MADGE_9DOF_X_LN,CHANNEL_TYPE.CAL,CHANNEL_UNITS.NO_UNITS,quaternion.getQuaternionX());
				objectCluster.addData(ObjectClusterSensorName.QUAT_MADGE_9DOF_Y_LN,CHANNEL_TYPE.CAL,CHANNEL_UNITS.NO_UNITS,quaternion.getQuaternionY());
				objectCluster.addData(ObjectClusterSensorName.QUAT_MADGE_9DOF_Z_LN,CHANNEL_TYPE.CAL,CHANNEL_UNITS.NO_UNITS,quaternion.getQuaternionZ());
			}
			if(isAxisAngleOutput()){
				objectCluster.addData(ObjectClusterSensorName.AXIS_ANGLE_9DOF_A_LN,CHANNEL_TYPE.CAL,CHANNEL_UNITS.NO_UNITS,quaternion.getTheta());
				objectCluster.addData(ObjectClusterSensorName.AXIS_ANGLE_9DOF_X_LN,CHANNEL_TYPE.CAL,CHANNEL_UNITS.NO_UNITS,quaternion.getAngleX());
				objectCluster.addData(ObjectClusterSensorName.AXIS_ANGLE_9DOF_Y_LN,CHANNEL_TYPE.CAL,CHANNEL_UNITS.NO_UNITS,quaternion.getAngleY());
				objectCluster.addData(ObjectClusterSensorName.AXIS_ANGLE_9DOF_Z_LN,CHANNEL_TYPE.CAL,CHANNEL_UNITS.NO_UNITS,quaternion.getAngleZ());
			}
			
		}
		else if(mAlgorithmName.equals(AlgorithmName.ORIENTATION_9DOF_WR)){
			if(isEulerOutput()){
				objectCluster.addData(ObjectClusterSensorName.EULER_9DOF_YAW_WR,CHANNEL_TYPE.CAL,CHANNEL_UNITS.NO_UNITS,quaternion.getYaw());
				objectCluster.addData(ObjectClusterSensorName.EULER_9DOF_PITCH_WR,CHANNEL_TYPE.CAL,CHANNEL_UNITS.NO_UNITS,quaternion.getPitch());
				objectCluster.addData(ObjectClusterSensorName.EULER_9DOF_ROLL_WR,CHANNEL_TYPE.CAL,CHANNEL_UNITS.NO_UNITS,quaternion.getRoll());
			}
			if(isQuaternionOutput()){
				objectCluster.addData(ObjectClusterSensorName.QUAT_MADGE_9DOF_W_WR,CHANNEL_TYPE.CAL,CHANNEL_UNITS.NO_UNITS,quaternion.getQuaternionW());
				objectCluster.addData(ObjectClusterSensorName.QUAT_MADGE_9DOF_X_WR,CHANNEL_TYPE.CAL,CHANNEL_UNITS.NO_UNITS,quaternion.getQuaternionX());
				objectCluster.addData(ObjectClusterSensorName.QUAT_MADGE_9DOF_Y_WR,CHANNEL_TYPE.CAL,CHANNEL_UNITS.NO_UNITS,quaternion.getQuaternionY());
				objectCluster.addData(ObjectClusterSensorName.QUAT_MADGE_9DOF_Z_WR,CHANNEL_TYPE.CAL,CHANNEL_UNITS.NO_UNITS,quaternion.getQuaternionZ());
			}
			if(isAxisAngleOutput()){
				objectCluster.addData(ObjectClusterSensorName.AXIS_ANGLE_9DOF_A_WR,CHANNEL_TYPE.CAL,CHANNEL_UNITS.NO_UNITS,quaternion.getTheta());
				objectCluster.addData(ObjectClusterSensorName.AXIS_ANGLE_9DOF_X_WR,CHANNEL_TYPE.CAL,CHANNEL_UNITS.NO_UNITS,quaternion.getAngleX());
				objectCluster.addData(ObjectClusterSensorName.AXIS_ANGLE_9DOF_Y_WR,CHANNEL_TYPE.CAL,CHANNEL_UNITS.NO_UNITS,quaternion.getAngleY());
				objectCluster.addData(ObjectClusterSensorName.AXIS_ANGLE_9DOF_Z_WR,CHANNEL_TYPE.CAL,CHANNEL_UNITS.NO_UNITS,quaternion.getAngleZ());
			}
			
		}

		return objectCluster;
	}


}
