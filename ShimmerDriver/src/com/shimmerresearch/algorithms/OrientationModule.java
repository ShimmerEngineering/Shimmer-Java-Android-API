package com.shimmerresearch.algorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.vecmath.Vector3d;

import com.shimmerresearch.algorithms.AbstractAlgorithm.GuiLabelConfigCommon;
import com.shimmerresearch.algorithms.OrientationModule6DOF.GuiLabelConfig;
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

public abstract class OrientationModule extends AbstractAlgorithm{

	/** * */
	private static final long serialVersionUID = -4174847826978293223L;
	
	protected final double BETA = 1;
	protected final double Q1 = 1;
	protected final double Q2 = 1;
	protected final double Q3 = 1;
	protected final double Q4 = 1;
	
	protected Vector3d accValues;
	protected Vector3d gyroValues;
	protected Vector3d magValues;
		
	protected static final String[] QUATERNION_OPTIONS = {"Off", "On"};
	protected static final String[] EULER_OPTIONS = {"Off", "On"};
	
	private static final ShimmerVerObject baseSh3Module = new ShimmerVerObject(
			HW_ID.SHIMMER_3,ShimmerVerDetails.ANY_VERSION,
			ShimmerVerDetails.ANY_VERSION,
			ShimmerVerDetails.ANY_VERSION,
			ShimmerVerDetails.ANY_VERSION,
			HW_ID_SR_CODES.EXP_BRD_EXG);
	
	//TODO update object cluster name with previously agreed name
	public static class AlgorithmName{
		public static final String ORIENTATION_9DOF_LN = "LN_Acc_9DoF";
		public static final String ORIENTATION_6DOF_LN = "LN_Acc_6DoF";
		public static final String ORIENTATION_9DOF_WR = "WR_Acc_9DoF";
		public static final String ORIENTATION_6DOF_WR = "WR_Acc_6DoF";
	}
	
	public class GuiLabelConfig{
		public static final String ACCELEROMETER = "Accelerometer";
		public static final String QUATERNION_OUTPUT = "Quaternion";
		public static final String EULER_OUTPUT = "Euler";
	}
	
	public static List<ShimmerVerObject> mListSVO = new ArrayList<ShimmerVerObject>(); 
	
	
	transient Object orientationAlgorithm;
	public boolean quaternionOutput= true;
	public boolean eulerOutput = false;
	protected double samplingRate;
	protected String accelerometerSensor;
	ORIENTATION_TYPE orientationType;
	
	public enum ORIENTATION_TYPE {
		NINE_DOF,
		SIX_DOF;
	}
	
	// ------------------- Algorithms grouping map end -----------------------

	{
		mListSVO.add(baseSh3Module);
		
//		mConfigOptionsMap.put(SAMPLING_RATE,new AlgorithmConfigOptionDetails(AlgorithmConfigOptionDetails.GUI_COMPONENT_TYPE.TEXTFIELD,mListSVO));
//		String[] accSensors = new String[2];
//		accSensors[0]=Shimmer3.GuiLabelSensorTiles.LOW_NOISE_ACCEL;
//		accSensors[1]=Shimmer3.GuiLabelSensorTiles.WIDE_RANGE_ACCEL;
//		mConfigOptionsMap.put(ACCELEROMETER, new AlgorithmConfigOptionDetails(AlgorithmConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX, mListSVO, accSensors));
		
	}
	
//	public OrientationModule(AlgorithmDetails algorithmDetails, double samplingRate) {
//		mAlgorithmDetails = algorithmDetails;
//		mAlgorithmType = ALGORITHM_TYPE.ALGORITHM_TYPE_CONTINUOUS;
//		mAlgorithmResultType = ALGORITHM_RESULT_TYPE.ALGORITHM_RESULT_TYPE_SINGLE_OBJECT_CLUSTER;
//		mAlgorithmName = algorithmDetails.mAlgorithmName;
//		mAlgorithmGroupingName = algorithmDetails.mAlgorithmName;
//		
//		this.samplingRate = samplingRate;
//		try {
//			initialize();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
	


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
			if(mAlgorithmName.equals(AlgorithmName.ORIENTATION_9DOF_LN)
					||mAlgorithmName.equals(AlgorithmName.ORIENTATION_6DOF_LN)){
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
			else if(mAlgorithmName.equals(AlgorithmName.ORIENTATION_9DOF_WR)
					||mAlgorithmName.equals(AlgorithmName.ORIENTATION_6DOF_WR)){
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
	
	public abstract ObjectCluster addQuaternionToObjectCluster(Orientation3DObject quaternion, ObjectCluster objectCluster);
	
	public abstract Orientation3DObject applyOrientationAlgorithm();

	@Override
	public AlgorithmResultObject processDataPostCapture(Object object)
			throws Exception {
		throw new Error(
				"Method: Object processDataPostCapture(List<?> objectList) is not valid for Orientation9DoF Module. Use: Object processDataRealTime(Object object).");
	}

	@Override
	public void reset() throws Exception {
		
	}

//	@Override
//	public void initialize() throws Exception {
//		
//		double samplingPeriod = 1/samplingRate;
//		
//		if(mAlgorithmName.equals(AlgorithmName.ORIENTATION_9DOF_LN)
//				||mAlgorithmName.equals(AlgorithmName.ORIENTATION_9DOF_WR)){
//			orientationType = ORIENTATION_TYPE.NINE_DOF;
//			orientationAlgorithm = new GradDes3DOrientation(BETA, samplingPeriod, Q1, Q2, Q3, Q4);
//		}
//		else if(mAlgorithmName.equals(AlgorithmName.ORIENTATION_6DOF_LN)
//				||mAlgorithmName.equals(AlgorithmName.ORIENTATION_6DOF_WR)){
//			orientationType = ORIENTATION_TYPE.SIX_DOF;
//			orientationAlgorithm = new GradDes3DOrientation6DoF(BETA, samplingPeriod, Q1, Q2, Q3, Q4);
//		}
//		
//		if(mAlgorithmName.equals(AlgorithmName.ORIENTATION_9DOF_LN)
//				||mAlgorithmName.equals(AlgorithmName.ORIENTATION_6DOF_LN)){
//			accelerometerSensor = Shimmer3.GuiLabelSensors.ACCEL_LN;
//		}
//		else{
//			accelerometerSensor = Shimmer3.GuiLabelSensors.ACCEL_WR;
//		}
//	}

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
	
	//trying to split combo box connection
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
		else if(mAlgorithmDetails.mAlgorithmName.equals(AlgorithmName.ORIENTATION_6DOF_LN)){
			if((derivedSensorBitmapID&DerivedSensorsBitMask.ORIENTATION_6DOF_LN_QUAT)>0){
				setQuaternionOutput(true);
			}
			if((derivedSensorBitmapID&DerivedSensorsBitMask.ORIENTATION_6DOF_LN_EULER)>0){
				setEulerOutput(true);
			}
		}
		else if(mAlgorithmDetails.mAlgorithmName.equals(AlgorithmName.ORIENTATION_6DOF_WR)){
			if((derivedSensorBitmapID&DerivedSensorsBitMask.ORIENTATION_6DOF_WR_QUAT)>0){
				setQuaternionOutput(true);
			}
			if((derivedSensorBitmapID&DerivedSensorsBitMask.ORIENTATION_6DOF_WR_EULER)>0){
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
			else if(mAlgorithmDetails.mAlgorithmName.equals(AlgorithmName.ORIENTATION_6DOF_WR)){
				if(isQuaternionOutput()){
					bitmask |= DerivedSensorsBitMask.ORIENTATION_6DOF_WR_QUAT;
				}
				if(isEulerOutput()){
					bitmask |= DerivedSensorsBitMask.ORIENTATION_6DOF_WR_EULER;
				}
			}
			else if(mAlgorithmDetails.mAlgorithmName.equals(AlgorithmName.ORIENTATION_6DOF_LN)){
				if(isQuaternionOutput()){
					bitmask |= DerivedSensorsBitMask.ORIENTATION_6DOF_LN_QUAT;
				}
				if(isEulerOutput()){
					bitmask |= DerivedSensorsBitMask.ORIENTATION_6DOF_LN_EULER;
				}
			}
		}
				
		return bitmask;
	}
	
	

	


}