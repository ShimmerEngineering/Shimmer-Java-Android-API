package com.shimmerresearch.sensors.lis2dw12;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driver.calibration.CalibDetails;
import com.shimmerresearch.driver.calibration.CalibDetailsKinematic;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driverUtilities.SensorDetails;
import com.shimmerresearch.driverUtilities.SensorDetailsRef;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;
import com.shimmerresearch.sensors.AbstractSensor;
import com.shimmerresearch.sensors.ActionSetting;
import com.shimmerresearch.sensors.lsm303.SensorLSM303AH;

public class SensorLIS2DW12 extends AbstractSensor {

	
	// ----------   Wide-range accel start ---------------
	protected int mSensorIdAccel = -1;
	protected int mAccelRange = 0;
	public CalibDetailsKinematic mCurrentCalibDetailsAccelWr = null;
	// ----------   Wide-range accel end ---------------
	
	
	//--------- Sensor info start --------------
	public static final Map<Integer, SensorDetailsRef> mSensorMapRef;
    static {
        Map<Integer, SensorDetailsRef> aMap = new LinkedHashMap<Integer, SensorDetailsRef>();
        aMap.put(Configuration.Shimmer3.SENSOR_ID.SHIMMER_LSM303_ACCEL, SensorLSM303AH.sensorLSM303AHAccel);  
        aMap.put(Configuration.Shimmer3.SENSOR_ID.SHIMMER_LSM303_MAG, SensorLSM303AH.sensorLSM303AHMag);	
		mSensorMapRef = Collections.unmodifiableMap(aMap);
    }
	//--------- Sensor info end --------------
	
	
	
    //--------- Constructors for this class start --------------
	public SensorLIS2DW12() {
		super(SENSORS.LIS2DW12);
		initialise();
	}
	
	public SensorLIS2DW12(ShimmerVerObject svo) {
		super(SENSORS.LIS2DW12, svo);
		initialise();
	}

	public SensorLIS2DW12(ShimmerDevice shimmerDevice) {
		super(SENSORS.LIS2DW12, shimmerDevice);
		initialise();
	}
   //--------- Constructors for this class end --------------

	

	@Override
	public void generateSensorMap() {
		// TODO Auto-generated method stub
		super.createLocalSensorMapWithCustomParser(mSensorMapRef, mChannelMapRef);
		
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getConfigValueUsingConfigLabel(Integer sensorId, String configLabel) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setSensorSamplingRate(double samplingRateHz) {
		// TODO Auto-generated method stub
		
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
	
	
	
	//--------- Optional methods to override in Sensor Class start --------
	@Override
	public void initialise() {
		mSensorIdAccel = Configuration.Shimmer3.SENSOR_ID.SHIMMER_LIS2DW12_ACCEL_WR;
		super.initialise();
			
		updateCurrentAccelWrCalibInUse();
	}
	//--------- Optional methods to override in Sensor Class end --------
	
	
	
	//--------- Sensor specific methods start --------------
	public void updateCurrentAccelWrCalibInUse(){
		mCurrentCalibDetailsAccelWr = getCurrentCalibDetailsIfKinematic(mSensorIdAccel, getAccelRange());
	}
	
	public int getAccelRange() {
		return mAccelRange;
	}
	//--------- Sensor specific methods end --------------
}