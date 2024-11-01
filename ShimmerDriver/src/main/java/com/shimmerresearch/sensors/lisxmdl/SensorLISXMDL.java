package com.shimmerresearch.sensors.lisxmdl;

import java.util.LinkedHashMap;

import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driver.calibration.CalibDetailsKinematic;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driverUtilities.SensorDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;
import com.shimmerresearch.sensors.AbstractSensor;
import com.shimmerresearch.sensors.ActionSetting;
import com.shimmerresearch.sensors.AbstractSensor.SENSORS;

public abstract class SensorLISXMDL extends AbstractSensor{

	/**
	 * 
	 */
	private static final long serialVersionUID = 336326050425332812L;
	
	protected int mMagRange = 0;
	protected int mSensorIdMag = -1;
	
	protected int mLIS2MDLMagRate = 4;
	
	public CalibDetailsKinematic mCurrentCalibDetailsMag = null;//calibDetailsMag1p3;
	
	public abstract int getMagRateFromFreqForSensor(boolean isEnabled, double freq);
	
	public SensorLISXMDL() {
		super(SENSORS.LISXMDL);
	}
	
	public SensorLISXMDL(ShimmerVerObject svo) {
		super(SENSORS.LISXMDL, svo);
	}

	public SensorLISXMDL(ShimmerDevice shimmerDevice) {
		super(SENSORS.LISXMDL, shimmerDevice);
	}
	
	public class GuiLabelSensors{
		public static final String MAG_WR = "Wide-Range Magnetometer"; 
		public static final String MAG = "Magnetometer"; 
	}
	
	public class GuiLabelConfig{
				
		public static final String LISXMDL_WR_MAG_RANGE = "Wide Range Mag Range";
		public static final String LISXMDL_WR_MAG_RATE = "Wide Range Mag Rate";

		public static final String LISXMDL_WR_MAG_DEFAULT_CALIB = "Wide Range Mag Default Calibration";

		//NEW
		public static final String LISXMDL_WR_MAG_CALIB_PARAM = "Wide Range Mag Calibration Details";
		public static final String LISXMDL_WR_MAG_VALID_CALIB = "Wide Range Mag Valid Calibration";
	}
	
	public static class ObjectClusterSensorName{
		
		public static  String MAG_WR_X = "Mag_WR_X";
		public static  String MAG_WR_Y = "Mag_WR_Y";
		public static  String MAG_WR_Z = "Mag_WR_Z";		
	}
	
	public class LABEL_SENSOR_TILE{
		public static final String MAG = GuiLabelSensors.MAG;
		public static final String WIDE_RANGE_MAG = GuiLabelSensors.MAG_WR;
	}
	
	public int getMagRange() {
		return mMagRange;
	}
	
	public int getLIS2MDLMagRate() {
		return mLIS2MDLMagRate;
	}
	
	public CalibDetailsKinematic getCurrentCalibDetailsMag(){
//		return getCurrentCalibDetails(mSensorIdMag, getMagRange());
		if(mCurrentCalibDetailsMag==null){
			updateCurrentMagCalibInUse();
		}
		return mCurrentCalibDetailsMag;
	}
	
	public void setLIS2MDLMagRange(int valueToSet) {
		//Not needed for LIS2MDL as it only has one range
	}
	
	public void setLIS2MDLMagRate(int valueToSet){
		mLIS2MDLMagRate = valueToSet;
	}
	
	public void updateCurrentMagCalibInUse(){
//		mCurrentCalibDetailsMag = getCurrentCalibDetailsMag();
		mCurrentCalibDetailsMag = getCurrentCalibDetailsIfKinematic(mSensorIdMag, getMagRange());
	}

	public SensorLISXMDL(SENSORS sensorType) {
		super(sensorType);
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

}
