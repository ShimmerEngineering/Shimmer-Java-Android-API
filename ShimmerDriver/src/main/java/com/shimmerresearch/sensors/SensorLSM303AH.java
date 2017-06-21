package com.shimmerresearch.sensors;

import java.util.LinkedHashMap;

import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driver.calibration.CalibDetailsKinematic;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driverUtilities.SensorDetails;

public class SensorLSM303AH extends SensorLSM303 {

	//--------- Configuration options start --------------
	public static final String MAG_RANGE = "+/- 50.0Ga"; 
	
	//--------- Configuration options end --------------


	// ----------   Mag start ---------------
	public static final double[][] AlignmentMatrixMagShimmer3 = {{-1,0,0},{0,1,0},{0,0,-1}}; 				
	public static final double[][] OffsetVectorMagShimmer3 = {{0},{0},{0}};	
	public static final double[][] SensitivityMatrixMag50GaShimmer3 = {{667,0,0},{0,667,0},{0,0,667}};

	private CalibDetailsKinematic calibDetailsMag50Ga = new CalibDetailsKinematic(
			0,
			MAG_RANGE,
			AlignmentMatrixMagShimmer3,
			SensitivityMatrixMag50GaShimmer3,
			OffsetVectorMagShimmer3);
	public CalibDetailsKinematic mCurrentCalibDetailsMag = calibDetailsMag50Ga;

	// ----------   Mag end ---------------

	
    //--------- Constructors for this class start --------------
	public SensorLSM303AH() {
		super();
	}

//	public SensorLSM303AH(ShimmerVerObject svo) {
//		super(svo);
//	}

	public SensorLSM303AH(ShimmerDevice shimmerDevice) {
		super(shimmerDevice);
	}
    //--------- Constructors for this class end --------------
	

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
	public void checkShimmerConfigBeforeConfiguring() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ObjectCluster processDataCustom(SensorDetails sensorDetails, byte[] rawData, COMMUNICATION_TYPE commType,
			ObjectCluster objectCluster, boolean isTimeSyncEnabled, long pctimeStamp) {
		// TODO Auto-generated method stub
		return null;
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

}
