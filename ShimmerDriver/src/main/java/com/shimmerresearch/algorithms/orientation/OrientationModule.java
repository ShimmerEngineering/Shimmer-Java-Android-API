package com.shimmerresearch.algorithms.orientation;

import java.util.Collection;

import javax.vecmath.Vector3d;

import com.shimmerresearch.algorithms.AbstractAlgorithm;
import com.shimmerresearch.algorithms.AlgorithmDetails;
import com.shimmerresearch.algorithms.AlgorithmResultObject;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.Configuration.Shimmer3.DerivedSensorsBitMask;
import com.shimmerresearch.driver.FormatCluster;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driver.ShimmerMsg;
import com.shimmerresearch.driverUtilities.ConfigOptionDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;
import com.shimmerresearch.driverUtilities.ConfigOptionDetails.GUI_COMPONENT_TYPE;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.FW_ID;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.HW_ID;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.HW_ID_SR_CODES;

public abstract class OrientationModule extends AbstractAlgorithm{

	private static final long serialVersionUID = 2501565989909361523L;
	
	//--------- Algorithm specific variables start --------------	
	protected Vector3d accValues;
	protected Vector3d gyroValues;
	protected Vector3d magValues;
		
	protected static final String[] QUATERNION_OPTIONS = {"Off", "On"};
	protected static final String[] EULER_OPTIONS = {"Off", "On"};
	
	protected static final ShimmerVerObject svoSh3Module = new ShimmerVerObject(
			HW_ID.SHIMMER_3,ShimmerVerDetails.ANY_VERSION,
			ShimmerVerDetails.ANY_VERSION,
			ShimmerVerDetails.ANY_VERSION,
			ShimmerVerDetails.ANY_VERSION,
			HW_ID_SR_CODES.EXP_BRD_EXG);
	
	public static class AlgorithmName{
		public static final String ORIENTATION_9DOF_LN = "LN_Acc_9DoF";
		public static final String ORIENTATION_6DOF_LN = "LN_Acc_6DoF";
		public static final String ORIENTATION_9DOF_WR = "WR_Acc_9DoF";
		public static final String ORIENTATION_6DOF_WR = "WR_Acc_6DoF";
	}
	
	public class GuiLabelConfig{
		public static final String ORIENTATAION_LN = "Low-Noise Accel";
		public static final String ORIENTATAION_WR = "Wide-Range Accel";
		public static final String QUATERNION_OUTPUT = "Quaternion";
		public static final String EULER_OUTPUT = "Euler";
	}
	
	transient GradDes3DOrientation orientationAlgorithm;
	public boolean quaternionOutput= true;
	public boolean eulerOutput = false;
	public boolean axisAngleOutput = false;
	protected double samplingRate;
	protected String accelerometerSensor;
	ORIENTATION_TYPE orientationType;
	
	public enum ORIENTATION_TYPE {
		NINE_DOF,
		SIX_DOF;
	}
	//--------- Algorithm specific variables end --------------	
	
	// --------- Configuration options start --------------
	public static final ConfigOptionDetails configOptionQuatOutput = new ConfigOptionDetails(
			OrientationModule.GuiLabelConfig.QUATERNION_OUTPUT,
			null,
			QUATERNION_OPTIONS, 
			GUI_COMPONENT_TYPE.COMBOBOX,
			null);

	public static final ConfigOptionDetails configOptionEulerOutput = new ConfigOptionDetails(
			OrientationModule.GuiLabelConfig.EULER_OUTPUT,
			null,
			EULER_OPTIONS, 
			GUI_COMPONENT_TYPE.COMBOBOX,
			null);
	// --------- Configuration options end --------------

	public abstract ObjectCluster addQuaternionToObjectCluster(Orientation3DObject quaternion, ObjectCluster objectCluster);
	public abstract Orientation3DObject applyOrientationAlgorithm();

	
    //--------- Constructors for this class start --------------
	
	public OrientationModule(ShimmerDevice shimmerDevice, AlgorithmDetails algorithmDetails) {
		super(shimmerDevice, algorithmDetails);
	}
    //--------- Constructors for this class end --------------

	public GradDes3DOrientation getOrientationAlgorithm() {
		return orientationAlgorithm;
	}
	
	@Override
	public AlgorithmResultObject processDataRealTime(ObjectCluster object) throws Exception {

		accValues = new Vector3d();
		magValues = new Vector3d();
		gyroValues = new Vector3d();

		for(String associatedChannel: mAlgorithmDetails.mListOfAssociatedSensorChannels){
			Collection<FormatCluster> dataFormatsSignal = object.getCollectionOfFormatClusters(associatedChannel);  // first retrieve all the possible formats for the current sensor device
			if(dataFormatsSignal!=null){
				FormatCluster formatClusterSignal = ((FormatCluster)ObjectCluster.returnFormatCluster(dataFormatsSignal, mAlgorithmDetails.mChannelType.toString())); // retrieve the calibrated data
				if(formatClusterSignal != null){
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
			else{
				return null;
			}
		}
		
		Orientation3DObject orientationObject = applyOrientationAlgorithm();
		object = addQuaternionToObjectCluster(orientationObject, object);
		
		AlgorithmResultObject aro = new AlgorithmResultObject(mAlgorithmResultType, object, getTrialName());
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
	
	@Override
	public AlgorithmResultObject processDataPostCapture(Object object)
			throws Exception {
		throw new Error(
				"Method: Object processDataPostCapture(List<?> objectList) is not valid for Orientation9DoF Module. Use: Object processDataRealTime(Object object).");
	}

	@Override
	public void resetAlgorithm() throws Exception {
		resetAlgorithmBuffers();
	}
	
	@Override
	public void resetAlgorithmBuffers() {
		if(orientationAlgorithm!=null) {
			orientationAlgorithm.resetInitialConditions();
		}
	}

	@Override
	public String printBatchMetrics() {
		return null;
	}

	@Override
	public void eventDataReceived(ShimmerMsg shimmerMSG) {
		
	}
	
	public double getShimmerSamplingRate(){
		return samplingRate;
	}
	
	public String getAccelerometer(){
		return accelerometerSensor;
	}
	
	public void setShimmerSamplingRate(double sampleRate){
		//Hack because the principle Sampling rate of the StroKare device for EMG is 2048Hz whereas the IMU sample at 51.2Hz 
		if(mShimmerDevice!=null && mShimmerDevice.getFirmwareIdentifier()==FW_ID.STROKARE){
			sampleRate = 51.2;
		}
		
		this.samplingRate = sampleRate;
		
		//TODO this is being initialized repeatedly in a number of different places.
		try {
			initialize();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void setIsEnabled(boolean isEnabled) {
		mIsEnabled = isEnabled;
	}
	
	protected void checkIfToDisable(){
		if(isEnabled() && !isQuaternionOutput() && !isEulerOutput()){
			setIsEnabled(false);
		}
	}
	
	public boolean isAxisAngleOutput() {
		return axisAngleOutput;
	}

	public void setAxisAngleOutput(boolean axisAngleOutput) {
		this.axisAngleOutput = axisAngleOutput;
		checkIfToDisable();
	}
	
	
	public boolean isEulerOutput() {
		return eulerOutput;
	}

	public void setEulerOutput(boolean eulerOutput) {
		this.eulerOutput = eulerOutput;
		checkIfToDisable();
	}
	
	public boolean isQuaternionOutput() {
		return quaternionOutput;
	}

	public void setQuaternionOutput(boolean quaternionOutput) {
		this.quaternionOutput = quaternionOutput;
		checkIfToDisable();
	}
	

	
	public ORIENTATION_TYPE getOrientationType(){
		return orientationType;
	}

	public void setOrientationType(ORIENTATION_TYPE algorithmType) {
		this.orientationType = algorithmType;
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
	
	
	@Override
	public void setDefaultSetting() {
		super.setDefaultSetting();
		if(isEnabled()){
			if(!isQuaternionOutput() && !isEulerOutput()){
				setQuaternionOutput(true);
	//			setEulerOutput(false);
			}
		}
		else {
			setQuaternionOutput(false);
			setEulerOutput(false);
		}
	}

	
	@Override
	public void loadAlgorithmVariables(AbstractAlgorithm abstractAlgorithmSource) {
//		super.loadAlgorithmVariables(value);
		
		if(abstractAlgorithmSource instanceof OrientationModule){
			OrientationModule orientationModuleSource = (OrientationModule) abstractAlgorithmSource;
			GradDes3DOrientation gradDes3DOrientation = orientationModuleSource.orientationAlgorithm;
			orientationAlgorithm.setInitialConditions(gradDes3DOrientation.mBeta, 
					gradDes3DOrientation.q0, 
					gradDes3DOrientation.q1, 
					gradDes3DOrientation.q2, 
					gradDes3DOrientation.q3);
		}
	}
	
}
