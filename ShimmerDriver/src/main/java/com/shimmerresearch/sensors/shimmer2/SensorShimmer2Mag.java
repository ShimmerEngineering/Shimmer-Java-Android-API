package com.shimmerresearch.sensors.shimmer2;

import java.util.LinkedHashMap;
import java.util.TreeMap;

import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driver.calibration.CalibDetails;
import com.shimmerresearch.driver.calibration.CalibDetailsKinematic;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driverUtilities.SensorDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.HW_ID;
import com.shimmerresearch.sensors.AbstractSensor;
import com.shimmerresearch.sensors.ActionSetting;
import com.shimmerresearch.sensors.AbstractSensor.GuiLabelConfigCommon;

public class SensorShimmer2Mag extends AbstractSensor {

	private static final long serialVersionUID = -1017796687253609998L;
	
	//--------- Sensor specific variables start --------------	

	protected int mShimmer2MagRate=0;
	protected int mMagRange = 1;
	protected boolean mLowPowerMag = false;	
	
	public boolean mIsUsingDefaultMagParam = true;

	//Shimmer2/2r Calibration - Default values
	protected static final double[][] AlignmentMatrixMagShimmer2 = {{1,0,0},{0,1,0},{0,0,-1}};
	protected static final double[][] SensitivityMatrixMagShimmer2 = {{580,0,0},{0,580,0},{0,0,580}}; 		
	protected static final double[][] OffsetVectorMagShimmer2 = {{0},{0},{0}};				

	protected static final double[][] SensitivityMatrixMag0p8GaShimmer2 = {{1370,0,0},{0,1370,0},{0,0,1370}};
	protected static final double[][] SensitivityMatrixMag1p3GaShimmer2 = {{1090,0,0},{0,1090,0},{0,0,1090}};
	protected static final double[][] SensitivityMatrixMag1p9GaShimmer2 = {{820,0,0},{0,820,0},{0,0,820}};
	protected static final double[][] SensitivityMatrixMag2p5GaShimmer2 = {{660,0,0},{0,660,0},{0,0,660}};
	protected static final double[][] SensitivityMatrixMag4p0GaShimmer2 = {{440,0,0},{0,440,0},{0,0,440}};
	protected static final double[][] SensitivityMatrixMag4p7GaShimmer2 = {{390,0,0},{0,390,0},{0,0,390}};
	protected static final double[][] SensitivityMatrixMag5p6GaShimmer2 = {{330,0,0},{0,330,0},{0,0,330}};
	protected static final double[][] SensitivityMatrixMag8p1GaShimmer2 = {{230,0,0},{0,230,0},{0,0,230}};

	private CalibDetailsKinematic calibDetailsShimmer2rMag0p8 = new CalibDetailsKinematic(
			0,
			Configuration.Shimmer2.ListofMagRange[0],
			AlignmentMatrixMagShimmer2,
			SensitivityMatrixMag0p8GaShimmer2,
			OffsetVectorMagShimmer2);
	private CalibDetailsKinematic calibDetailsShimmer2rMag1p3 = new CalibDetailsKinematic(
			1,
			Configuration.Shimmer2.ListofMagRange[1],
			AlignmentMatrixMagShimmer2,
			SensitivityMatrixMag1p3GaShimmer2,
			OffsetVectorMagShimmer2);
	private CalibDetailsKinematic calibDetailsShimmer2rMag1p9 = new CalibDetailsKinematic(
			2,
			Configuration.Shimmer2.ListofMagRange[2],
			AlignmentMatrixMagShimmer2,
			SensitivityMatrixMag1p9GaShimmer2,
			OffsetVectorMagShimmer2);
	private CalibDetailsKinematic calibDetailsShimmer2rMag2p5 = new CalibDetailsKinematic(
			3,
			Configuration.Shimmer2.ListofMagRange[3],
			AlignmentMatrixMagShimmer2,
			SensitivityMatrixMag2p5GaShimmer2,
			OffsetVectorMagShimmer2);
	private CalibDetailsKinematic calibDetailsShimmer2rMag4p0 = new CalibDetailsKinematic(
			4,
			Configuration.Shimmer2.ListofMagRange[4],
			AlignmentMatrixMagShimmer2,
			SensitivityMatrixMag4p0GaShimmer2,
			OffsetVectorMagShimmer2);
	private CalibDetailsKinematic calibDetailsShimmer2rMag4p7 = new CalibDetailsKinematic(
			5,
			Configuration.Shimmer2.ListofMagRange[5],
			AlignmentMatrixMagShimmer2,
			SensitivityMatrixMag4p7GaShimmer2,
			OffsetVectorMagShimmer2);
	private CalibDetailsKinematic calibDetailsShimmer2rMag5p6 = new CalibDetailsKinematic(
			6,
			Configuration.Shimmer2.ListofMagRange[6],
			AlignmentMatrixMagShimmer2,
			SensitivityMatrixMag5p6GaShimmer2,
			OffsetVectorMagShimmer2);
	private CalibDetailsKinematic calibDetailsShimmer2rMag8p1 = new CalibDetailsKinematic(
			7,
			Configuration.Shimmer2.ListofMagRange[7],
			AlignmentMatrixMagShimmer2,
			SensitivityMatrixMag8p1GaShimmer2,
			OffsetVectorMagShimmer2);

	protected TreeMap<Integer, CalibDetails> mCalibMapMagShimmer2r = new TreeMap<Integer, CalibDetails>(); 
	{
		mCalibMapMagShimmer2r.put(calibDetailsShimmer2rMag0p8.mRangeValue, calibDetailsShimmer2rMag0p8);
		mCalibMapMagShimmer2r.put(calibDetailsShimmer2rMag1p3.mRangeValue, calibDetailsShimmer2rMag1p3);
		mCalibMapMagShimmer2r.put(calibDetailsShimmer2rMag1p9.mRangeValue, calibDetailsShimmer2rMag1p9);
		mCalibMapMagShimmer2r.put(calibDetailsShimmer2rMag2p5.mRangeValue, calibDetailsShimmer2rMag2p5);
		mCalibMapMagShimmer2r.put(calibDetailsShimmer2rMag4p0.mRangeValue, calibDetailsShimmer2rMag4p0);
		mCalibMapMagShimmer2r.put(calibDetailsShimmer2rMag4p7.mRangeValue, calibDetailsShimmer2rMag4p7);
		mCalibMapMagShimmer2r.put(calibDetailsShimmer2rMag5p6.mRangeValue, calibDetailsShimmer2rMag5p6);
		mCalibMapMagShimmer2r.put(calibDetailsShimmer2rMag8p1.mRangeValue, calibDetailsShimmer2rMag8p1);
	}
	
	
	public class GuiLabelConfig{
		public static final String MAG_LOW_POWER_MODE = "Mag Low Power Mode";
		public static final String MAG_RANGE = "Mag Range";
	}
	
	public CalibDetailsKinematic mCurrentCalibDetailsMag = null;

	//--------- Sensor specific variables end --------------	

    //--------- Constructors for this class end --------------

	public SensorShimmer2Mag(ShimmerDevice shimmerDevice) {
		super(SENSORS.SHIMMER2R_MAG, shimmerDevice);
		initialise();
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
			case(GuiLabelConfig.MAG_RANGE):
				setMagRange((int)valueToSet);
	    		break;
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
		setShimmer2rMagRateFromFreq(samplingRateHz);
		checkLowPowerMag();
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
	public void generateCalibMap() {
		super.generateCalibMap();
		
		TreeMap<Integer, CalibDetails> calibMapMag = null;
		if(mShimmerDevice.getHardwareVersion()==HW_ID.SHIMMER_2){
			calibMapMag = mCalibMapMagShimmer2r;
		} else {
			calibMapMag = mCalibMapMagShimmer2r;
		}
		if(calibMapMag!=null){
			setCalibrationMapPerSensor(Configuration.Shimmer2.SENSOR_ID.MAG, calibMapMag);
		}

		updateCurrentCalibInUse();
	}
	
	@Override
	public void setCalibrationMapPerSensor(int sensorId, TreeMap<Integer, CalibDetails> mapOfSensorCalibration) {
		super.setCalibrationMapPerSensor(sensorId, mapOfSensorCalibration);
		updateCurrentCalibInUse();
	}
	
	//--------- Optional methods to override in Sensor Class end --------

	
	//--------- Sensor specific methods start --------------
	/**
	 * This enables the low power mag option. When not enabled the sampling rate
	 * of the mag is set to the closest supported value to the actual sampling
	 * rate that it can achieve. In low power mode it defaults to 10Hz
	 * 
	 * @param enable
	 */
	public void setLowPowerMag(boolean enable){
		mLowPowerMag = enable;
		setShimmer2rMagRateFromFreq(mShimmerDevice.getSamplingRateShimmer());
	}

	public boolean checkLowPowerMag() {
		setLowPowerMag((getMagRate() <= 4)? true:false);
		return isLowPowerMagEnabled();
	}

	public void setShimmer2rMagRateFromFreq(double samplingRateShimmer) {
		if (!isLowPowerMagEnabled()){
			if (samplingRateShimmer>=50){
				setShimmer2rMagRate(6);
			} else if (samplingRateShimmer>=20) {
				setShimmer2rMagRate(5);
			} else if (samplingRateShimmer>=10) {
				setShimmer2rMagRate(4);
			} else {
				setShimmer2rMagRate(3);
			}
		} else {
			setShimmer2rMagRate(4);
		}
		
//		if(!isLowPowerMagEnabled()){
//			if(samplingRateShimmer<=10) {
//				setShimmer2rMagRate(4);
//			} 
//			else if (samplingRateShimmer<=20) {
//				setShimmer2rMagRate(5);
//			} 
//			else {
//				setShimmer2rMagRate(6);
//			}
//		} 
//		else {
//			setShimmer2rMagRate(4);
//		}
	}
	
	public void setShimmer2rMagRate(int magRate) {
		mShimmer2MagRate = magRate;
	}

	
    public boolean isLowPowerMagEnabled(){
		return mLowPowerMag;
	} 
    
	public int getMagRate() {
		return mShimmer2MagRate;
	}
	
	public int getMagRange() {
		return mMagRange;
	}

	public void setMagRange(int range){
		mMagRange = range;
	}
	
	public void updateCurrentCalibInUse(){
		mCurrentCalibDetailsMag = getCurrentCalibDetailsIfKinematic(Configuration.Shimmer2.SENSOR_ID.MAG, getMagRange());
	}

	public CalibDetailsKinematic getCurrentCalibDetailsMag(){
		if(mCurrentCalibDetailsMag==null){
			updateCurrentCalibInUse();
		}
		return mCurrentCalibDetailsMag;
	}

	
	//--------- Sensor specific methods end --------------
	
	public void updateIsUsingDefaultMagParam() {
		mIsUsingDefaultMagParam = getCurrentCalibDetailsMag().isUsingDefaultParameters();
	}
	
}
