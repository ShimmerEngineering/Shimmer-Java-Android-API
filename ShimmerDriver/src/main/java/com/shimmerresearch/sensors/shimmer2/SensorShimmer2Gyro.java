package com.shimmerresearch.sensors.shimmer2;

import java.util.LinkedHashMap;
import java.util.TreeMap;

import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driver.calibration.CalibDetails;
import com.shimmerresearch.driver.calibration.CalibDetailsKinematic;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driverUtilities.OnTheFlyCalGyro;
import com.shimmerresearch.driverUtilities.SensorDetails;
import com.shimmerresearch.sensors.AbstractSensor;
import com.shimmerresearch.sensors.ActionSetting;

public class SensorShimmer2Gyro extends AbstractSensor {

	private static final long serialVersionUID = -8600361157087801458L;

	
	/** This stores the current Gyro Range, it is a value between 0 and 3; 0 = +/- 250dps,1 = 500dps, 2 = 1000dps, 3 = 2000dps */
	private int mGyroRange = 1;													 

	/** all raw params should start with a 1 byte identifier in position [0] */
	protected byte[] mGyroCalRawParams  = new byte[22];

	transient protected OnTheFlyCalGyro mOnTheFlyCalGyro = new OnTheFlyCalGyro(); 

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
			ObjectCluster objectCluster, boolean isTimeSyncEnabled, long pctimeStamp) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void checkShimmerConfigBeforeConfiguring() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void configByteArrayGenerate(ShimmerDevice shimmerDevice, byte[] configBytes) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void configByteArrayParse(ShimmerDevice shimmerDevice, byte[] configBytes) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Object setConfigValueUsingConfigLabel(Integer sensorMapKey, String configLabel, Object valueToSet) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getConfigValueUsingConfigLabel(Integer sensorMapKey, String configLabel) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setSensorSamplingRate(double samplingRateHz) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean setDefaultConfigForSensor(int sensorMapKey, boolean isSensorEnabled) {
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
	public LinkedHashMap<String, Object> getConfigMapForDb() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void parseConfigMapFromDb(LinkedHashMap<String, Object> mapOfConfigPerShimmer) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void setCalibrationMapPerSensor(int sensorMapKey, TreeMap<Integer, CalibDetails> mapOfSensorCalibration) {
		super.setCalibrationMapPerSensor(sensorMapKey, mapOfSensorCalibration);
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
		mCurrentCalibDetailsGyro = getCurrentCalibDetailsIfKinematic(Configuration.Shimmer2.SensorMapKey.GYRO, getGyroRange());
	}

	public CalibDetailsKinematic getCurrentCalibDetailsGyro(){
		return mCurrentCalibDetailsGyro;
	}

	/**
	 * @param enable this enables the calibration of the gyroscope while streaming
	 * @param bufferSize sets the buffersize of the window used to determine the new calibration parameters, see implementation for more details
	 * @param threshold sets the threshold of when to use the incoming data to recalibrate gyroscope offset, this is in degrees, and the default value is 1.2
	 */
	public void enableOnTheFlyGyroCal(boolean state, int bufferSize, double threshold){
		mOnTheFlyCalGyro.enableOnTheFlyGyroCal(state, bufferSize, threshold);
	}
	
	public void setOnTheFlyGyroCal(boolean state){
		mOnTheFlyCalGyro.setOnTheFlyGyroCal(state);
	}

    public boolean isGyroOnTheFlyCalEnabled(){
    	return mOnTheFlyCalGyro.isGyroOnTheFlyCalEnabled();
	}

    public OnTheFlyCalGyro getOnTheFlyCalGyro(){
    	return mOnTheFlyCalGyro;
    }


}
