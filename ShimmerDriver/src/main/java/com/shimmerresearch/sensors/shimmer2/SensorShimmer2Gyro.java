package com.shimmerresearch.sensors.shimmer2;

import java.util.LinkedHashMap;
import java.util.TreeMap;

import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driver.calibration.CalibDetails;
import com.shimmerresearch.driver.calibration.CalibDetailsKinematic;
import com.shimmerresearch.algorithms.gyroOnTheFlyCal.OnTheFlyGyroOffsetCal;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driverUtilities.SensorDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.HW_ID;
import com.shimmerresearch.sensors.AbstractSensor;
import com.shimmerresearch.sensors.ActionSetting;

public class SensorShimmer2Gyro extends AbstractSensor {

	private static final long serialVersionUID = -8600361157087801458L;

	private int mGyroRange = 0;													 

	protected boolean mLowPowerGyro = false;
	
	public boolean mIsUsingDefaultGyroParam = true;
	
	/** all raw params should start with a 1 byte identifier in position [0] */
	protected byte[] mGyroCalRawParams  = new byte[22];

	transient protected OnTheFlyGyroOffsetCal mOnTheFlyGyroOffsetCal = new OnTheFlyGyroOffsetCal(); 

	//Shimmer2/2r Calibration - Default values (LPR450AL = X+Y axes, LPY450AL = X axis)
	protected static final double[][] AlignmentMatrixGyroShimmer2 = {{0,-1,0},{-1,0,0},{0,0,-1}}; 				
	protected static final double[][] OffsetVectorGyroShimmer2 = {{1843},{1843},{1843}};
	protected static final double[][] SensitivityMatrixGyroShimmer2 = {{2.73,0,0},{0,2.73,0},{0,0,2.73}}; 		
	
	private CalibDetailsKinematic calibDetailsShimmer2rGyro = new CalibDetailsKinematic(
			0, 
			"Default",
			AlignmentMatrixGyroShimmer2,
			SensitivityMatrixGyroShimmer2,
			OffsetVectorGyroShimmer2);
	
	public TreeMap<Integer, CalibDetails> mCalibMapGyroShimmer2r = new TreeMap<Integer, CalibDetails>(); 
	{
		mCalibMapGyroShimmer2r.put(calibDetailsShimmer2rGyro.mRangeValue, calibDetailsShimmer2rGyro);
	}
	public CalibDetailsKinematic mCurrentCalibDetailsGyro = null;

	
	public SensorShimmer2Gyro(ShimmerDevice shimmerDevice) {
		super(SENSORS.SHIMMER2R_GYRO, shimmerDevice);
		initialise();
	}

	@Override
	public void generateSensorMap() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void generateConfigOptionsMap() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void generateSensorGroupMapping() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ObjectCluster processDataCustom(SensorDetails sensorDetails, byte[] rawData, COMMUNICATION_TYPE commType,
			ObjectCluster objectCluster, boolean isTimeSyncEnabled, double pctimeStampMs) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void checkShimmerConfigBeforeConfiguring() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void configBytesGenerate(ShimmerDevice shimmerDevice, byte[] configBytes, COMMUNICATION_TYPE commType) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void configBytesParse(ShimmerDevice shimmerDevice, byte[] configBytes, COMMUNICATION_TYPE commType) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Object setConfigValueUsingConfigLabel(Integer sensorId, String configLabel, Object valueToSet) {
		Object returnValue = null;
		switch(configLabel){
//			case(GuiLabelConfigCommon.RANGE):
//	    		break;
	        default:
	        	returnValue = super.setConfigValueUsingConfigLabelCommon(sensorId, configLabel, valueToSet);
	        	break;
		}
		return returnValue;
	}

	@Override
	public Object getConfigValueUsingConfigLabel(Integer sensorId, String configLabel) {
		Object returnValue = null;
		switch(configLabel){
			case(GuiLabelConfigCommon.RANGE):
//				if(sensorId==Configuration.Shimmer3.SENSOR_ID.SHIMMER_ANALOG_ACCEL){
//					returnValue = 0;
//				}
				break;
			default:
				returnValue = super.getConfigValueUsingConfigLabelCommon(sensorId, configLabel);
				break;
		}
		return returnValue;
	}

	@Override
	public void setSensorSamplingRate(double samplingRateHz) {
		setOnTheFlyCalGyroBufferSize(samplingRateHz);
	}

	@Override
	public boolean setDefaultConfigForSensor(int sensorId, boolean isSensorEnabled) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean checkConfigOptionValues(String stringKey) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Object getSettings(String componentName, COMMUNICATION_TYPE commType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ActionSetting setSettings(String componentName, Object valueToSet, COMMUNICATION_TYPE commType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean processResponse(int responseCommand, Object parsedResponse, COMMUNICATION_TYPE commType) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public LinkedHashMap<String, Object> generateConfigMap() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void parseConfigMap(LinkedHashMap<String, Object> mapOfConfigPerShimmer) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void setCalibrationMapPerSensor(int sensorId, TreeMap<Integer, CalibDetails> mapOfSensorCalibration) {
		super.setCalibrationMapPerSensor(sensorId, mapOfSensorCalibration);
		updateCurrentCalibInUse();
	}

	@Override
	public void generateCalibMap() {
		super.generateCalibMap();
		
		TreeMap<Integer, CalibDetails> calibMapGyro = null;
		if(mShimmerDevice.getHardwareVersion()==HW_ID.SHIMMER_2){
			calibMapGyro = mCalibMapGyroShimmer2r;
		} else {
			calibMapGyro = mCalibMapGyroShimmer2r;
		}
		if(calibMapGyro!=null){
			setCalibrationMapPerSensor(Configuration.Shimmer2.SENSOR_ID.GYRO, calibMapGyro);
		}

		updateCurrentCalibInUse();
	}
	
	
	public int getGyroRange(){
		return mGyroRange;
	}
	
	public void setGyroRange(int newRange) {
		mGyroRange = newRange;
		updateCurrentCalibInUse();
	}
	

	public void updateCurrentCalibInUse(){
		mCurrentCalibDetailsGyro = getCurrentCalibDetailsIfKinematic(Configuration.Shimmer2.SENSOR_ID.GYRO, getGyroRange());
	}

	public CalibDetailsKinematic getCurrentCalibDetailsGyro(){
		CalibDetails calibPerSensor = getCalibForSensor(Configuration.Shimmer2.SENSOR_ID.GYRO, getGyroRange());
		if(calibPerSensor!=null){
			return (CalibDetailsKinematic) calibPerSensor;
		}
		return null;
	}

	/**
	 * @param enable this enables the calibration of the gyroscope while streaming
	 * @param bufferSize sets the buffersize of the window used to determine the new calibration parameters, see implementation for more details
	 * @param threshold sets the threshold of when to use the incoming data to recalibrate gyroscope offset, this is in degrees, and the default value is 1.2
	 */
	public void enableOnTheFlyGyroCal(boolean state, int bufferSize, double threshold){
		mOnTheFlyGyroOffsetCal.setIsEnabled(state, bufferSize, threshold);
	}
	
	public void setOnTheFlyGyroCal(boolean state){
		mOnTheFlyGyroOffsetCal.setIsEnabled(state);
	}

    public boolean isGyroOnTheFlyCalEnabled(){
    	return mOnTheFlyGyroOffsetCal.isEnabled();
	}

    public OnTheFlyGyroOffsetCal getOnTheFlyCalGyro(){
    	return mOnTheFlyGyroOffsetCal;
    }
    
    public void setOnTheFlyCalGyroBufferSize(double samplingRate) {
    	mOnTheFlyGyroOffsetCal.setBufferSizeFromSamplingRate(samplingRate);
    }
    
    public void setOnTheFlyCalGyroThreshold(int threshold) {
    	mOnTheFlyGyroOffsetCal.setOffsetThreshold(threshold);
    }

	public void setLowPowerGyro(boolean enable) {
		mLowPowerGyro = enable;
	}

	public int getLowPowerGyroEnabled() {
		return mLowPowerGyro? 1:0;
	}

	public boolean checkLowPowerGyro() {
		return mLowPowerGyro;
	}
	
	public void updateIsUsingDefaultGyroParam() {
		mIsUsingDefaultGyroParam = getCurrentCalibDetailsGyro().isUsingDefaultParameters();
	}


}
