package com.shimmerresearch.algorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.shimmerresearch.algorithms.AbstractAlgorithm.ALGORITHM_RESULT_TYPE;
import com.shimmerresearch.algorithms.AbstractAlgorithm.ALGORITHM_TYPE;
import com.shimmerresearch.algorithms.AbstractAlgorithm.GuiLabelConfigCommon;
//import com.shimmerresearch.algorithms.OrientationModule.GuiLabelConfig;
import com.shimmerresearch.algorithms.OrientationModule.ORIENTATION_TYPE;
import com.shimmerresearch.algorithms.OrientationModule6DOF.AlgorithmName;
import com.shimmerresearch.algorithms.OrientationModule6DOF.ObjectClusterSensorName;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.Configuration.CHANNEL_UNITS;
import com.shimmerresearch.driver.Configuration.Shimmer3;
import com.shimmerresearch.driver.ShimmerObject.DerivedSensorsBitMask;
import com.shimmerresearch.driverUtilities.ChannelDetails;
import com.shimmerresearch.driverUtilities.SensorGroupingDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_TYPE;
import com.shimmerresearch.driverUtilities.ConfigOptionDetails.GUI_COMPONENT_TYPE;

public class OrientationModule9DOF extends OrientationModule {

		/** * */
		private static final long serialVersionUID = -4174847826978293223L;

		//TODO update objectcluster name with previously aggreed name
		public static class AlgorithmName{
			public static final String ORIENTATION_9DOF_LN = "LN_Acc_9DoF";
			public static final String ORIENTATION_9DOF_WR = "WR_Acc_9DoF";
		}	
		
		transient Object orientationAlgorithm;
			
		protected static String WR = "_WR";
		protected static String LN = "_LN";
		
		public static class ObjectClusterSensorName{
			//QUATERNIONS
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
			public static String EULER_9DOF_A_WR = EULER_9DOF_A + WR; 
			public static String EULER_9DOF_X = Configuration.Shimmer3.ObjectClusterSensorName.EULER_9DOF_X; 
			public static String EULER_9DOF_X_LN = EULER_9DOF_X + LN; 
			public static String EULER_9DOF_X_WR = EULER_9DOF_X + WR; 
			public static String EULER_9DOF_Y = Configuration.Shimmer3.ObjectClusterSensorName.EULER_9DOF_Y; 
			public static String EULER_9DOF_Y_LN = EULER_9DOF_Y + LN; 
			public static String EULER_9DOF_Y_WR = EULER_9DOF_Y + WR; 
			public static String EULER_9DOF_Z = Configuration.Shimmer3.ObjectClusterSensorName.EULER_9DOF_Z; 
			public static String EULER_9DOF_Z_LN = EULER_9DOF_Z + LN; 
			public static String EULER_9DOF_Z_WR = EULER_9DOF_Z + WR; 
		
			@Deprecated //need to describe axis angle 9DOF vs 9DOF
			public static String AXIS_ANGLE_A = Configuration.Shimmer3.ObjectClusterSensorName.AXIS_ANGLE_A; 
			@Deprecated //need to describe axis angle 9DOF vs 9DOF
			public static String AXIS_ANGLE_X = Configuration.Shimmer3.ObjectClusterSensorName.AXIS_ANGLE_X; 
			@Deprecated //need to describe axis angle 9DOF vs 9DOF
			public static String AXIS_ANGLE_Y = Configuration.Shimmer3.ObjectClusterSensorName.AXIS_ANGLE_Y; 
			@Deprecated //need to describe axis angle 9DOF vs 9DOF
			public static String AXIS_ANGLE_Z = Configuration.Shimmer3.ObjectClusterSensorName.AXIS_ANGLE_Z; 
		}
		
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

		 //9DOF channel groups
		 public static List<ChannelDetails> listChannelsQuat9DOF_WR = Arrays.asList(
				 channelQuatW_9DOF_WR, channelQuatX_9DOF_WR, channelQuatY_9DOF_WR, channelQuatZ_9DOF_WR);

		 public static List<ChannelDetails> listChannelsEuler9DOF_LN = Arrays.asList(
				 channelAngleA_9DOF_LN, channelAngleX_9DOF_LN, channelAngleY_9DOF_LN, channelAngleZ_9DOF_LN);
		 
		 public static List<ChannelDetails> listChannelsQuat9DOF_LN = Arrays.asList(
				 channelQuatW_9DOF_LN, channelQuatX_9DOF_LN, channelQuatY_9DOF_LN, channelQuatZ_9DOF_LN);

		 public static List<ChannelDetails> listChannelsEuler9DOF_WR = Arrays.asList(
				 channelAngleA_9DOF_WR, channelAngleX_9DOF_WR, channelAngleY_9DOF_WR, channelAngleZ_9DOF_WR);
		
		public static final AlgorithmDetails algo9DoFOrientation_LN_Acc = new AlgorithmDetails(
				AlgorithmName.ORIENTATION_9DOF_LN, 
				"Low-Noise Accel", 
				Arrays.asList(Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_LN_X,
						Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_LN_Y,
						Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_LN_Z,
						Configuration.Shimmer3.ObjectClusterSensorName.GYRO_X,
						Configuration.Shimmer3.ObjectClusterSensorName.GYRO_Y,
						Configuration.Shimmer3.ObjectClusterSensorName.GYRO_Z),
//						Configuration.Shimmer3.GuiLabelAlgorithmGrouping.ORIENTATION_9DOF.getTileText(),
				(DerivedSensorsBitMask.ORIENTATION_9DOF_LN_QUAT|DerivedSensorsBitMask.ORIENTATION_9DOF_LN_EULER), 
				Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_ANALOG_ACCEL,
						Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_GYRO),
				CHANNEL_UNITS.NO_UNITS,
				listChannelsEuler9DOF_LN);
		
		public static final AlgorithmDetails algo9DoFOrientation_WR_Acc = new AlgorithmDetails(
				AlgorithmName.ORIENTATION_9DOF_WR, 
				"Wide-Range Accel", 
				Arrays.asList(Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_WR_X,
						Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_WR_Y,
						Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_WR_Z,
						Configuration.Shimmer3.ObjectClusterSensorName.GYRO_X,
						Configuration.Shimmer3.ObjectClusterSensorName.GYRO_Y,
						Configuration.Shimmer3.ObjectClusterSensorName.GYRO_Z),
//						Configuration.Shimmer3.GuiLabelAlgorithmGrouping.ORIENTATION_9DOF.getTileText(),
				(DerivedSensorsBitMask.ORIENTATION_9DOF_WR_QUAT|DerivedSensorsBitMask.ORIENTATION_9DOF_WR_EULER), 
				Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_LSM303DLHC_ACCEL,
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
	    
		public static LinkedHashMap<String, AlgorithmDetails> getMapOfSupportedAlgorithms(ShimmerVerObject shimmerVerObject) {
			LinkedHashMap<String, AlgorithmDetails> mapOfSupportedAlgorithms = new LinkedHashMap<String, AlgorithmDetails>();
			//TODO Filter here depending on Shimmer version
			mapOfSupportedAlgorithms.putAll(mAlgorithmMapRef);
			return mapOfSupportedAlgorithms;
		}

	    
		// ------------------- Algorithms grouping map start -----------------------
		public static final SensorGroupingDetails sGD9Dof = new SensorGroupingDetails(
				Configuration.Shimmer3.GuiLabelAlgorithmGrouping.ORIENTATION_9DOF.getTileText(), 
				Arrays.asList(OrientationModule9DOF.algo9DoFOrientation_LN_Acc,
						OrientationModule9DOF.algo9DoFOrientation_WR_Acc),
				Arrays.asList(GuiLabelConfig.QUATERNION_OUTPUT,
						GuiLabelConfig.EULER_OUTPUT),
				0);
		// ------------------- Algorithms grouping map end -----------------------

		{
			
//			mConfigOptionsMap.put(SAMPLING_RATE,new AlgorithmConfigOptionDetails(AlgorithmConfigOptionDetails.GUI_COMPONENT_TYPE.TEXTFIELD,mListSVO));
//			String[] accSensors = new String[2];
//			accSensors[0]=Shimmer3.GuiLabelSensorTiles.LOW_NOISE_ACCEL;
//			accSensors[1]=Shimmer3.GuiLabelSensorTiles.WIDE_RANGE_ACCEL;
//			mConfigOptionsMap.put(ACCELEROMETER, new AlgorithmConfigOptionDetails(AlgorithmConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX, mListSVO, accSensors));
			final ConfigOptionDetailsAlgorithm configOptionQuatOutput9DOF = new ConfigOptionDetailsAlgorithm(
					QUATERNION_OPTIONS, 
					GUI_COMPONENT_TYPE.COMBOBOX,
					null);

			final ConfigOptionDetailsAlgorithm configOptionEulerOutput9DOF = new ConfigOptionDetailsAlgorithm(
					EULER_OPTIONS, 
					GUI_COMPONENT_TYPE.COMBOBOX,
					null);
			
			
			mConfigOptionsMap.put(GuiLabelConfig.QUATERNION_OUTPUT, configOptionQuatOutput9DOF);
			mConfigOptionsMap.put(GuiLabelConfig.EULER_OUTPUT, configOptionEulerOutput9DOF);
			
			mMapOfAlgorithmGrouping.put(Configuration.Shimmer3.GuiLabelAlgorithmGrouping.ORIENTATION_9DOF.ordinal(), sGD9Dof);

		}
		
		public ObjectCluster addQuaternionToObjectCluster(Orientation3DObject quaternion, ObjectCluster objectCluster){
		

		 if(mAlgorithmName.equals(AlgorithmName.ORIENTATION_9DOF_LN)){
			if(isEulerOutput()){
				objectCluster.addData(ObjectClusterSensorName.EULER_9DOF_A_LN,CHANNEL_TYPE.CAL,CHANNEL_UNITS.NO_UNITS,quaternion.getTheta());
				objectCluster.addData(ObjectClusterSensorName.EULER_9DOF_X_LN,CHANNEL_TYPE.CAL,CHANNEL_UNITS.NO_UNITS,quaternion.getAngleX());
				objectCluster.addData(ObjectClusterSensorName.EULER_9DOF_Y_LN,CHANNEL_TYPE.CAL,CHANNEL_UNITS.NO_UNITS,quaternion.getAngleY());
				objectCluster.addData(ObjectClusterSensorName.EULER_9DOF_Z_LN,CHANNEL_TYPE.CAL,CHANNEL_UNITS.NO_UNITS,quaternion.getAngleZ());
			}
			if(isQuaternionOutput()){
				objectCluster.addData(ObjectClusterSensorName.QUAT_MADGE_9DOF_W_LN,CHANNEL_TYPE.CAL,CHANNEL_UNITS.NO_UNITS,quaternion.getQuaternionW());
				objectCluster.addData(ObjectClusterSensorName.QUAT_MADGE_9DOF_X_LN,CHANNEL_TYPE.CAL,CHANNEL_UNITS.NO_UNITS,quaternion.getQuaternionX());
				objectCluster.addData(ObjectClusterSensorName.QUAT_MADGE_9DOF_Y_LN,CHANNEL_TYPE.CAL,CHANNEL_UNITS.NO_UNITS,quaternion.getQuaternionY());
				objectCluster.addData(ObjectClusterSensorName.QUAT_MADGE_9DOF_Z_LN,CHANNEL_TYPE.CAL,CHANNEL_UNITS.NO_UNITS,quaternion.getQuaternionZ());
			}
		}
		else if(mAlgorithmName.equals(AlgorithmName.ORIENTATION_9DOF_WR)){
			if(isEulerOutput()){
				objectCluster.addData(ObjectClusterSensorName.EULER_9DOF_A_WR,CHANNEL_TYPE.CAL,CHANNEL_UNITS.NO_UNITS,quaternion.getTheta());
				objectCluster.addData(ObjectClusterSensorName.EULER_9DOF_X_WR,CHANNEL_TYPE.CAL,CHANNEL_UNITS.NO_UNITS,quaternion.getAngleX());
				objectCluster.addData(ObjectClusterSensorName.EULER_9DOF_Y_WR,CHANNEL_TYPE.CAL,CHANNEL_UNITS.NO_UNITS,quaternion.getAngleY());
				objectCluster.addData(ObjectClusterSensorName.EULER_9DOF_Z_WR,CHANNEL_TYPE.CAL,CHANNEL_UNITS.NO_UNITS,quaternion.getAngleZ());
			}
			if(isQuaternionOutput()){
				objectCluster.addData(ObjectClusterSensorName.QUAT_MADGE_9DOF_W_WR,CHANNEL_TYPE.CAL,CHANNEL_UNITS.NO_UNITS,quaternion.getQuaternionW());
				objectCluster.addData(ObjectClusterSensorName.QUAT_MADGE_9DOF_X_WR,CHANNEL_TYPE.CAL,CHANNEL_UNITS.NO_UNITS,quaternion.getQuaternionX());
				objectCluster.addData(ObjectClusterSensorName.QUAT_MADGE_9DOF_Y_WR,CHANNEL_TYPE.CAL,CHANNEL_UNITS.NO_UNITS,quaternion.getQuaternionY());
				objectCluster.addData(ObjectClusterSensorName.QUAT_MADGE_9DOF_Z_WR,CHANNEL_TYPE.CAL,CHANNEL_UNITS.NO_UNITS,quaternion.getQuaternionZ());
			}
		}
		
		return objectCluster;
	}	
		
		public OrientationModule9DOF(AlgorithmDetails algorithmDetails, double samplingRate) {
			// TODO Auto-generated constructor stub
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
		public void initialize() throws Exception {
			
			double samplingPeriod = 1/samplingRate;
			
			if(mAlgorithmName.equals(AlgorithmName.ORIENTATION_9DOF_LN)
					||mAlgorithmName.equals(AlgorithmName.ORIENTATION_9DOF_WR)){
				orientationType = ORIENTATION_TYPE.NINE_DOF;
				orientationAlgorithm = new GradDes3DOrientation(BETA, samplingPeriod, Q1, Q2, Q3, Q4);
			}
			
			if(mAlgorithmName.equals(AlgorithmName.ORIENTATION_9DOF_LN)){
				accelerometerSensor = Shimmer3.GuiLabelSensors.ACCEL_LN;
			}
			else{
				accelerometerSensor = Shimmer3.GuiLabelSensors.ACCEL_WR;
			}
		}
		
		public Orientation3DObject applyOrientationAlgorithm(){
			
			Orientation3DObject quaternion;
				quaternion = ((GradDes3DOrientation) orientationAlgorithm).update(
						accValues.x, accValues.y, accValues.z, 
						gyroValues.x, gyroValues.y, gyroValues.z
						,magValues.x, magValues.y, magValues.z);

			return quaternion;
		}
		

		@Override
		public List<ChannelDetails> getChannelDetails() {
			
			List<ChannelDetails> listOfChannelDetails = new ArrayList<ChannelDetails>();

			if(mAlgorithmDetails.mAlgorithmName.equals(AlgorithmName.ORIENTATION_9DOF_WR)){
				if(isQuaternionOutput()){
					listOfChannelDetails.addAll(listChannelsQuat9DOF_WR);
				}
				if(isEulerOutput()){
					listOfChannelDetails.addAll(listChannelsEuler9DOF_WR);
				}
			}
			else if(mAlgorithmDetails.mAlgorithmName.equals(AlgorithmName.ORIENTATION_9DOF_LN)){
				if(isQuaternionOutput()){
					listOfChannelDetails.addAll(listChannelsQuat9DOF_LN);
				}
				if(isEulerOutput()){
					listOfChannelDetails.addAll(listChannelsEuler9DOF_LN);
				}
			}
			
//			return super.getChannelDetails();
			return listOfChannelDetails;
		}
		

		
	}



