package com.shimmerresearch.sensors.lisxmdl;

import java.util.Arrays;
import java.util.TreeMap;

import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driver.calibration.CalibDetails;
import com.shimmerresearch.driver.calibration.CalibDetailsKinematic;
import com.shimmerresearch.driver.calibration.UtilCalibration;
import com.shimmerresearch.driver.calibration.CalibDetails.CALIB_READ_SOURCE;
import com.shimmerresearch.driver.shimmer2r3.ConfigByteLayoutShimmer3;
import com.shimmerresearch.driver.ConfigByteLayout;
import com.shimmerresearch.driver.FormatCluster;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driverUtilities.ChannelDetails;
import com.shimmerresearch.driverUtilities.ConfigOptionDetailsSensor;
import com.shimmerresearch.driverUtilities.SensorDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;
import com.shimmerresearch.driverUtilities.UtilShimmer;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_TYPE;
import com.shimmerresearch.sensors.AbstractSensor;

public abstract class SensorLISXMDL extends AbstractSensor{

	private static final long serialVersionUID = 336326050425332812L;
	
	protected int mSensorIdMag = -1;
	protected int mSensorIdWRMag = -1;
	
	public abstract int getMagRateFromFreqForSensor(boolean isEnabled, double freq);
	public abstract int getMagRateFromFreqForSensor(boolean isEnabled, double freq, int mode);
	public abstract boolean checkLowPowerMag();
	public abstract void setLISMagRange(int valueToSet);
	public abstract void setLISWRMagRange(int valueToSet);
	
	// ----------   Wide-range mag start ---------------
	
	protected int mWRMagRange = 0;
	public boolean mIsUsingDefaultWRMagParam = true;
	protected int mLISWRMagRate = 4;

	public CalibDetailsKinematic mCurrentCalibDetailsMagWr = null;

	// ----------   Wide-range mag end ---------------

	// ----------   Mag start ---------------
	
	protected int mMagRange = 0;
	protected int mLISMagRate = 4;
	public boolean mIsUsingDefaultMagParam = true;
	protected boolean mLowPowerMag = false;
	protected boolean mMedPowerMag = false;
	protected boolean mHighPowerMag = false;
	protected boolean mUltraHighPowerMag = false;
	
	public CalibDetailsKinematic mCurrentCalibDetailsMag = null;
	
	// ----------   Mag end ---------------

	public class GuiLabelSensors{
		public static final String MAG_WR = "Wide-Range Magnetometer"; 
		public static final String MAG = "Magnetometer"; 
	}
	
	public class LABEL_SENSOR_TILE{
		public static final String MAG = GuiLabelSensors.MAG;
		public static final String WIDE_RANGE_MAG = GuiLabelSensors.MAG_WR;
	}
	
	//--------- Configuration options start --------------
	
	public static final String[] ListofLISMagRange={
			UtilShimmer.UNICODE_PLUS_MINUS + " 4Gauss",
			UtilShimmer.UNICODE_PLUS_MINUS + " 8Gauss",
			UtilShimmer.UNICODE_PLUS_MINUS + " 12Gauss",
			UtilShimmer.UNICODE_PLUS_MINUS + " 16Gauss"};  
	
	//--------- Configuration options end --------------
	
	public class GuiLabelConfig{
		
		public static final String LISXMDL_WR_MAG_RANGE = "Wide Range Mag Range";
		public static final String LISXMDL_WR_MAG_RATE = "Wide Range Mag Rate";

		public static final String LISXMDL_WR_MAG_DEFAULT_CALIB = "Wide Range Mag Default Calibration";

		//NEW
		public static final String LISXMDL_WR_MAG_CALIB_PARAM = "Wide Range Mag Calibration Details";
		public static final String LISXMDL_WR_MAG_VALID_CALIB = "Wide Range Mag Valid Calibration";
		
		public static final String LISXMDL_MAG_RANGE = "Mag Range";
		public static final String LISXMDL_MAG_RATE = "Mag Rate";
		
		public static final String LISXMDL_MAG_LP = "Mag Low-Power Mode";
		public static final String LISXMDL_MAG_MP = "Mag Med-Power Mode";
		public static final String LISXMDL_MAG_HP = "Mag High-Power Mode";
		public static final String LISXMDL_MAG_UP = "Mag Ultra High-Power Mode";

		public static final String LISXMDL_MAG_DEFAULT_CALIB = "Mag Default Calibration";

		//NEW
		public static final String LISXMDL_MAG_CALIB_PARAM = "Mag Calibration Details";
		public static final String LISXMDL_MAG_VALID_CALIB = "Mag Valid Calibration";
		
	}
	
	public static class ObjectClusterSensorName{
		
		public static  String MAG_WR_X = "Mag_WR_X";
		public static  String MAG_WR_Y = "Mag_WR_Y";
		public static  String MAG_WR_Z = "Mag_WR_Z";		
		
		public static  String MAG_X = "Mag_X";
		public static  String MAG_Y = "Mag_Y";
		public static  String MAG_Z = "Mag_Z";		
	}
	
    //--------- Constructors for this class start --------------
    /** This constructor is just used for accessing calibration*/
	public SensorLISXMDL() {
		super(SENSORS.LISXMDL);
	}
	
	public SensorLISXMDL(ShimmerVerObject svo) {
		super(SENSORS.LISXMDL, svo);
	}

	public SensorLISXMDL(ShimmerDevice shimmerDevice) {
		super(SENSORS.LISXMDL, shimmerDevice);
	}
    //--------- Constructors for this class end --------------

	//--------- Abstract methods implemented start --------------
	
	@Override
	public void checkShimmerConfigBeforeConfiguring() {
		if(!isSensorEnabled(mSensorIdWRMag)) {
			setDefaultLisMagWrSensorConfig(false);
		}
		
		if(!isSensorEnabled(mSensorIdMag)) {
			setDefaultLisMagSensorConfig(false);
		}

		setLowPowerMag(false);
	}

	@Override 
	public void setSensorSamplingRate(double samplingRateHz) {
		//set sampling rate of the sensors as close to the Shimmer sampling rate as possible (sensor sampling rate >= shimmer sampling rate)
		setLowPowerMag(false);
		
		setLISWRMagRateFromFreq(samplingRateHz);
		setLISMagRateFromFreq(samplingRateHz);
		
		checkLowPowerMag();
	}

	//--------- Abstract methods implemented end --------------

	//--------- Optional methods to override in Sensor Class start --------
	@Override 
	public ObjectCluster processDataCustom(SensorDetails sensorDetails, byte[] rawData, COMMUNICATION_TYPE commType, ObjectCluster objectCluster, boolean isTimeSyncEnabled, double pcTimestampMs) {
		
		// process data originating from the Shimmer
		objectCluster = sensorDetails.processDataCommon(rawData, commType, objectCluster, isTimeSyncEnabled, pcTimestampMs);
		
		//Calibration
		if(mEnableCalibration){
			// get uncalibrated data for each (sub)sensor
			if(sensorDetails.mSensorDetailsRef.mGuiFriendlyLabel.equals(GuiLabelSensors.MAG_WR) && mCurrentCalibDetailsMagWr!=null){
				double[] unCalibratedMagWrData = new double[3];
				for (ChannelDetails channelDetails:sensorDetails.mListOfChannels){
					//Uncalibrated Mag WR data
					if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.MAG_WR_X)){
						unCalibratedMagWrData[0] = ((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(channelDetails.mObjectClusterName), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
					}
					else if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.MAG_WR_Y)){
						unCalibratedMagWrData[1]  = ((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(channelDetails.mObjectClusterName), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
					}
					else if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.MAG_WR_Z)){
						unCalibratedMagWrData[2]  = ((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(channelDetails.mObjectClusterName), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
					}
				}
				
				double[] calibratedMagWrData = UtilCalibration.calibrateInertialSensorData(unCalibratedMagWrData, mCurrentCalibDetailsMagWr);
//				double[] calibratedAccelWrData = UtilCalibration.calibrateInertialSensorData(unCalibratedAccelWrData, mAlignmentMatrixWRAccel, mSensitivityMatrixWRAccel, mOffsetVectorWRAccel);
	
				//Add calibrated data to Object cluster
				if(sensorDetails.mSensorDetailsRef.mGuiFriendlyLabel.equals(GuiLabelSensors.MAG_WR)){	
					for (ChannelDetails channelDetails:sensorDetails.mListOfChannels){
						if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.MAG_WR_X)){
							objectCluster.addCalData(channelDetails, calibratedMagWrData[0], objectCluster.getIndexKeeper()-3, isUsingDefaultMagWRParam());
						}
						else if(channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.MAG_WR_Y)){
							objectCluster.addCalData(channelDetails, calibratedMagWrData[1], objectCluster.getIndexKeeper()-2, isUsingDefaultMagWRParam());
						}
						else if(channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.MAG_WR_Z)){
							objectCluster.addCalData(channelDetails, calibratedMagWrData[2], objectCluster.getIndexKeeper()-1, isUsingDefaultMagWRParam());
						}
					}
				}
	
				//Debugging
				if(mIsDebugOutput){
					super.consolePrintChannelsCal(objectCluster, Arrays.asList(
							new String[]{ObjectClusterSensorName.MAG_WR_X, CHANNEL_TYPE.UNCAL.toString()}, 
							new String[]{ObjectClusterSensorName.MAG_WR_Y, CHANNEL_TYPE.UNCAL.toString()}, 
							new String[]{ObjectClusterSensorName.MAG_WR_Z, CHANNEL_TYPE.UNCAL.toString()}, 
							new String[]{ObjectClusterSensorName.MAG_WR_X, CHANNEL_TYPE.CAL.toString()}, 
							new String[]{ObjectClusterSensorName.MAG_WR_Y, CHANNEL_TYPE.CAL.toString()},
							new String[]{ObjectClusterSensorName.MAG_WR_Z, CHANNEL_TYPE.CAL.toString()}));
				}
	
			}
			else if(sensorDetails.mSensorDetailsRef.mGuiFriendlyLabel.equals(GuiLabelSensors.MAG) && mCurrentCalibDetailsMag!=null){
				double[] unCalibratedMagData = new double[3];
				for (ChannelDetails channelDetails:sensorDetails.mListOfChannels){
					//Uncalibrated Magnetometer data
					if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.MAG_X)){
						unCalibratedMagData[0] = ((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(channelDetails.mObjectClusterName), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
					}
					else if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.MAG_Y)){
						unCalibratedMagData[1]  = ((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(channelDetails.mObjectClusterName), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
					}
					else if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.MAG_Z)){
						unCalibratedMagData[2]  = ((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(channelDetails.mObjectClusterName), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
					}	
				}
				
//				double[] calibratedMagData = UtilCalibration.calibrateInertialSensorData(unCalibratedMagData, mAlignmentMatrixMagnetometer, mSensitivityMatrixMagnetometer, mOffsetVectorMagnetometer);
				double[] calibratedMagData = UtilCalibration.calibrateInertialSensorData(unCalibratedMagData, mCurrentCalibDetailsMag);

				if(sensorDetails.mSensorDetailsRef.mGuiFriendlyLabel.equals(GuiLabelSensors.MAG)){
					for (ChannelDetails channelDetails:sensorDetails.mListOfChannels){
						if(channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.MAG_X)){
							objectCluster.addCalData(channelDetails, calibratedMagData[0], objectCluster.getIndexKeeper()-3, isUsingDefaultMagParam());
						}
						else if(channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.MAG_Y)){
							objectCluster.addCalData(channelDetails, calibratedMagData[1], objectCluster.getIndexKeeper()-2, isUsingDefaultMagParam());
						}
						else if(channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.MAG_Z)){
							objectCluster.addCalData(channelDetails, calibratedMagData[2], objectCluster.getIndexKeeper()-1, isUsingDefaultMagParam());
						}
					}
				}
	
				//Debugging
				if(mIsDebugOutput){
					super.consolePrintChannelsCal(objectCluster, Arrays.asList(
							new String[]{ObjectClusterSensorName.MAG_X, CHANNEL_TYPE.UNCAL.toString()}, 
							new String[]{ObjectClusterSensorName.MAG_Y, CHANNEL_TYPE.UNCAL.toString()}, 
							new String[]{ObjectClusterSensorName.MAG_Z, CHANNEL_TYPE.UNCAL.toString()}, 
							new String[]{ObjectClusterSensorName.MAG_X, CHANNEL_TYPE.CAL.toString()}, 
							new String[]{ObjectClusterSensorName.MAG_Y, CHANNEL_TYPE.CAL.toString()},
							new String[]{ObjectClusterSensorName.MAG_Z, CHANNEL_TYPE.CAL.toString()}));
				}
			}
		}
		return objectCluster;
	}
	
	@Override 
	public void configBytesGenerate(ShimmerDevice shimmerDevice, byte[] configBytes, COMMUNICATION_TYPE commType) {
		//still not being implemented for wr mag sensor
		ConfigByteLayout configByteLayout = shimmerDevice.getConfigByteLayout();
		if(configByteLayout instanceof ConfigByteLayoutShimmer3){
			ConfigByteLayoutShimmer3 configByteLayoutCast = (ConfigByteLayoutShimmer3) configByteLayout;

			configBytes[configByteLayoutCast.idxConfigSetupByte2] |= (byte) ((getMagRange() & configByteLayoutCast.maskLSM303DLHCMagRange) << configByteLayoutCast.bitShiftLSM303DLHCMagRange);
			configBytes[configByteLayoutCast.idxConfigSetupByte2] |= (byte) ((getLISMagRate() & configByteLayoutCast.maskLSM303DLHCMagSamplingRate) << configByteLayoutCast.bitShiftLSM303DLHCMagSamplingRate);

			// LISM3MDL Magnetometer Calibration Parameters
			byte[] bufferCalibrationParameters = generateCalParamLIS3MDLMag();
			System.arraycopy(bufferCalibrationParameters, 0, configBytes, configByteLayoutCast.idxLSM303DLHCMagCalibration, configByteLayoutCast.lengthGeneralCalibrationBytes);
		}
		
	}
	
	@Override 
	public void configBytesParse(ShimmerDevice shimmerDevice, byte[] configBytes, COMMUNICATION_TYPE commType) { 
		//still not being implemented for wr mag sensor
		ConfigByteLayout configByteLayout = shimmerDevice.getConfigByteLayout();
		if(configByteLayout instanceof ConfigByteLayoutShimmer3){
			ConfigByteLayoutShimmer3 configByteLayoutCast = (ConfigByteLayoutShimmer3) configByteLayout;

			setLISMagRange((configBytes[configByteLayoutCast.idxConfigSetupByte2] >> configByteLayoutCast.bitShiftLSM303DLHCMagRange) & configByteLayoutCast.maskLSM303DLHCMagRange);
			setLISMagRate((configBytes[configByteLayoutCast.idxConfigSetupByte2] >> configByteLayoutCast.bitShiftLSM303DLHCMagSamplingRate) & configByteLayoutCast.maskLSM303DLHCMagSamplingRate);
			checkLowPowerMag(); // check rate to determine if Sensor is in LPM mode
			
			if (shimmerDevice.isConnected()){
				getCurrentCalibDetailsMag().mCalibReadSource=CALIB_READ_SOURCE.INFOMEM;
			}

			// LSM303DLHC Magnetometer Calibration Parameters
			byte[] bufferCalibrationParameters = new byte[configByteLayoutCast.lengthGeneralCalibrationBytes];
			System.arraycopy(configBytes, configByteLayoutCast.idxLSM303DLHCMagCalibration, bufferCalibrationParameters, 0 , configByteLayoutCast.lengthGeneralCalibrationBytes);
			parseCalibParamFromPacketMag(bufferCalibrationParameters, CALIB_READ_SOURCE.INFOMEM);
		}
	}
	
	@Override 
	public Object setConfigValueUsingConfigLabel(Integer sensorId, String configLabel, Object valueToSet) {
		Object returnValue = null;
		
		switch(configLabel){
			case(GuiLabelConfig.LISXMDL_MAG_LP):
				setLowPowerMag((boolean)valueToSet);
				break;
			case(GuiLabelConfig.LISXMDL_MAG_MP):
				setMedPowerMag((boolean)valueToSet);
				break;
			case(GuiLabelConfig.LISXMDL_MAG_HP):
				setHighPowerMag((boolean)valueToSet);
				break;
			case(GuiLabelConfig.LISXMDL_MAG_UP):
				setUltraHighPowerMag((boolean)valueToSet);
				break;
			case(GuiLabelConfig.LISXMDL_MAG_RANGE):
				setLISMagRange((int)valueToSet);
				break;
			case(GuiLabelConfig.LISXMDL_MAG_RATE):
				setLISMagRate((int)valueToSet);
				break;
				
//			case(GuiLabelConfigCommon.KINEMATIC_CALIBRATION_ALL):
//				TreeMap<Integer, TreeMap<Integer, CalibDetails>> mapOfKinematicSensorCalibration = (TreeMap<Integer, TreeMap<Integer, CalibDetails>>) valueToSet;
//				setCalibration(mapOfKinematicSensorCalibration);
//				returnValue = valueToSet;
//	    		break;
			case(GuiLabelConfigCommon.RANGE):
				if(sensorId==mSensorIdWRMag){
					this.setConfigValueUsingConfigLabel(GuiLabelConfig.LISXMDL_WR_MAG_RANGE, valueToSet);
				}
				else if(sensorId==mSensorIdMag){
					this.setConfigValueUsingConfigLabel(GuiLabelConfig.LISXMDL_MAG_RANGE, valueToSet);
				}
				break;
			case(GuiLabelConfigCommon.RATE):
				if(sensorId==mSensorIdWRMag){
					this.setConfigValueUsingConfigLabel(GuiLabelConfig.LISXMDL_WR_MAG_RATE, valueToSet);
				}
				else if(sensorId==mSensorIdMag){
					this.setConfigValueUsingConfigLabel(GuiLabelConfig.LISXMDL_MAG_RATE, valueToSet);
				}
				break;
			default:
				returnValue = super.setConfigValueUsingConfigLabelCommon(sensorId, configLabel, valueToSet);
				break;
		}	
		
        if(configLabel.equals(SensorLISXMDL.GuiLabelConfig.LISXMDL_MAG_RATE)){
        	checkConfigOptionValues(configLabel);
        }
		
		return returnValue;
	}
	
	@Override 
	public Object getConfigValueUsingConfigLabel(Integer sensorId, String configLabel) {
		Object returnValue = null;
		
		if(configLabel.equals(GuiLabelConfig.LISXMDL_MAG_RATE)){
        	checkConfigOptionValues(configLabel);
        }
		
		switch(configLabel){
			case(GuiLabelConfig.LISXMDL_MAG_LP):
				returnValue = isLowPowerMagEnabled();
	        	break;
			case(GuiLabelConfig.LISXMDL_MAG_MP):
				returnValue = isMedPowerMagEnabled();
	        	break;
			case(GuiLabelConfig.LISXMDL_MAG_HP):
				returnValue = isHighPowerMagEnabled();
	        	break;
			case(GuiLabelConfig.LISXMDL_MAG_UP):
				returnValue = isUltraHighPowerMagEnabled();
	        	break;
			case(GuiLabelConfig.LISXMDL_MAG_RATE): 
				returnValue = getLISMagRate();
		    	break;
			case(GuiLabelConfig.LISXMDL_MAG_RANGE):
				//TODO check below and commented out code (RS (20/5/2016): Same as in ShimmerObject.)
				returnValue = getMagRange();
	        	
//			case(Configuration.Shimmer3.GuiLabelConfig.KINEMATIC_CALIBRATION_ALL):
//				returnValue = getKinematicCalibration();
//				break;
			case(GuiLabelConfigCommon.RANGE):
				if(sensorId==mSensorIdWRMag){
					returnValue = this.getConfigValueUsingConfigLabel(GuiLabelConfig.LISXMDL_WR_MAG_RANGE);
				}
				else if(sensorId==mSensorIdMag){
					returnValue = this.getConfigValueUsingConfigLabel(GuiLabelConfig.LISXMDL_MAG_RANGE);
				}
				break;
			case(GuiLabelConfigCommon.RATE):
				if(sensorId==mSensorIdWRMag){
					returnValue = this.getConfigValueUsingConfigLabel(GuiLabelConfig.LISXMDL_WR_MAG_RATE);
				}
				else if(sensorId==mSensorIdMag){
					returnValue = this.getConfigValueUsingConfigLabel(GuiLabelConfig.LISXMDL_MAG_RATE);
				}
				break;
			default:
				returnValue = super.getConfigValueUsingConfigLabelCommon(sensorId, configLabel);
				break;
			
		}
		return returnValue;
	
	}
	
	@Override 
	public boolean setDefaultConfigForSensor(int sensorId, boolean isSensorEnabled) {
		if(mSensorMap.containsKey(sensorId)){
			if(sensorId==mSensorIdWRMag) {
				setDefaultLisMagWrSensorConfig(isSensorEnabled);		
			}
			else if(sensorId==mSensorIdMag) {
				setDefaultLisMagSensorConfig(isSensorEnabled);
			}
			return true;
		}
		return false;
	}
	
	@Override 
	public boolean checkConfigOptionValues(String stringKey) {		
		if(mConfigOptionsMap.containsKey(stringKey)){
			if(stringKey==GuiLabelConfig.LISXMDL_MAG_RATE){
				if(isLowPowerMagEnabled()) {
					mConfigOptionsMap.get(stringKey).setIndexOfValuesToUse(ConfigOptionDetailsSensor.VALUE_INDEXES.LISXMDL_MAG_RATE.IS_LP);
				}
				else if (isMedPowerMagEnabled()){
					mConfigOptionsMap.get(stringKey).setIndexOfValuesToUse(ConfigOptionDetailsSensor.VALUE_INDEXES.LISXMDL_MAG_RATE.IS_MP);
				}
				else if (isHighPowerMagEnabled()){
					mConfigOptionsMap.get(stringKey).setIndexOfValuesToUse(ConfigOptionDetailsSensor.VALUE_INDEXES.LISXMDL_MAG_RATE.IS_HP);
				}
				else if (isUltraHighPowerMagEnabled()){
					mConfigOptionsMap.get(stringKey).setIndexOfValuesToUse(ConfigOptionDetailsSensor.VALUE_INDEXES.LISXMDL_MAG_RATE.IS_UP);
				}
			}		
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see com.shimmerresearch.sensors.AbstractSensor#isSensorUsingDefaultCal(int)
	 */
	@Override
	public boolean isSensorUsingDefaultCal(int sensorId) {
		if(sensorId==mSensorIdWRMag){
			return isUsingDefaultMagWRParam();
		}
		else if(sensorId==mSensorIdMag){
			return isUsingDefaultMagParam();
		}
		return false;
	}

	@Override
	public void setCalibrationMapPerSensor(int sensorId, TreeMap<Integer, CalibDetails> mapOfSensorCalibration) {
		super.setCalibrationMapPerSensor(sensorId, mapOfSensorCalibration);
		updateCurrentMagWrCalibInUse();
		updateCurrentMagCalibInUse();
	}

	//--------- Optional methods to override in Sensor Class end --------
	
	//--------- Sensor specific methods start --------------
	
	public int getLowPowerMagEnabled(){
		return (isLowPowerMagEnabled()? 1:0);
	}
	
	public int getMedPowerMagEnabled(){
		return (isMedPowerMagEnabled()? 1:0);
	}
	
	public int getHighPowerMagEnabled(){
		return (isHighPowerMagEnabled()? 1:0);
	}
	
	public int getUltraHighPowerMagEnabled(){
		return (isUltraHighPowerMagEnabled()? 1:0);
	}
	
	public void	setLowPowerMag(boolean enable){
		mLowPowerMag = enable;
		if(mShimmerDevice!=null){
			setLIS3MDLMagRateFromFreq(getSamplingRateShimmer());
		}
	}
	
	public void	setMedPowerMag(boolean enable){
		mMedPowerMag = enable;
		if(mShimmerDevice!=null){
			setLIS3MDLMagRateFromFreq(getSamplingRateShimmer());
		}
	}
	
	public void	setHighPowerMag(boolean enable){
		mHighPowerMag = enable;
		if(mShimmerDevice!=null){
			setLIS3MDLMagRateFromFreq(getSamplingRateShimmer());
		}
	}
	
	public void	setUltraHighPowerMag(boolean enable){
		mUltraHighPowerMag = enable;
		if(mShimmerDevice!=null){
			setLIS3MDLMagRateFromFreq(getSamplingRateShimmer());
		}
	}

	public boolean isLowPowerMagEnabled(){
		return mLowPowerMag;
	}
	
	public boolean isMedPowerMagEnabled(){
		return mMedPowerMag;
	}
	
	public boolean isHighPowerMagEnabled(){
		return mHighPowerMag;
	}
	
	public boolean isUltraHighPowerMagEnabled(){
		return mUltraHighPowerMag;
	}

	public void setDefaultLisMagSensorConfig(boolean isSensorEnabled) {
		if(isSensorEnabled) {
			setLowPowerMag(false);
		}
		else {
			setLISMagRange(1);
			setLowPowerMag(true);
		}		
	}
	
	public void setDefaultLisMagWrSensorConfig(boolean isSensorEnabled) {
		//no wr mag range
	}
	
	public int setLISMagRateFromFreq(double freq) {
		boolean isEnabled = isSensorEnabled(mSensorIdMag);
//		System.out.println("Setting Sampling Rate: " + freq + "\tmLowPowerAccelWR:" + mLowPowerAccelWR);
		setLISMagRateInternal(getMagRateFromFreqForSensor(isEnabled, freq, 0));
		return mLISMagRate;
	}
	
	public int setLISWRMagRateFromFreq(double freq) {
		boolean isEnabled = isSensorEnabled(mSensorIdWRMag);
//		System.out.println("Setting Sampling Rate: " + freq + "\tmLowPowerAccelWR:" + mLowPowerAccelWR);
		setLISWRMagRateInternal(getMagRateFromFreqForSensor(isEnabled, freq));
		return mLISWRMagRate;
	}
	
	public CalibDetailsKinematic getCurrentCalibDetailsMag(){
//		return getCurrentCalibDetails(mSensorIdMag, getMagRange());
		if(mCurrentCalibDetailsMag==null){
			updateCurrentMagCalibInUse();
		}
		return mCurrentCalibDetailsMag;
	}
	
	public CalibDetailsKinematic getCurrentCalibDetailsMagWr(){
//		return getCurrentCalibDetails(mSensorIdMag, getMagRange());
		if(mCurrentCalibDetailsMagWr==null){
			updateCurrentMagWrCalibInUse();
		}
		return mCurrentCalibDetailsMagWr;
	}
	
	public byte[] generateCalParamLIS3MDLMag(){
		return getCurrentCalibDetailsMag().generateCalParamByteArray();
	}
	
	public byte[] generateCalParamLIS2MDLMag(){
		return getCurrentCalibDetailsMagWr().generateCalParamByteArray();
	}
	
	public void parseCalibParamFromPacketMag(byte[] bufferCalibrationParameters, CALIB_READ_SOURCE calibReadSource) {
		getCurrentCalibDetailsMag().parseCalParamByteArray(bufferCalibrationParameters, calibReadSource);
	}
	
	public void parseCalibParamFromPacketMagWr(byte[] bufferCalibrationParameters, CALIB_READ_SOURCE calibReadSource) {
		getCurrentCalibDetailsMagWr().parseCalParamByteArray(bufferCalibrationParameters, calibReadSource);
	}
	
	public void setDefaultCalibrationShimmer3Mag() {
		getCurrentCalibDetailsMag().resetToDefaultParameters();
	}
	
	public void setDefaultCalibrationShimmer3WideRangeMag() {
		getCurrentCalibDetailsMagWr().resetToDefaultParameters();
	}
	
	public boolean isUsingDefaultMagParam(){
		return getCurrentCalibDetailsMag().isUsingDefaultParameters(); 
	}
	
	public boolean isUsingDefaultMagWRParam(){
		return getCurrentCalibDetailsMagWr().isUsingDefaultParameters(); 
	}
	
	public double[][] getAlignmentMatrixWRMag(){
		return getCurrentCalibDetailsMagWr().getValidAlignmentMatrix();
	}

	
	public double[][] getSensitivityMatrixWRMag(){
		return getCurrentCalibDetailsMagWr().getValidSensitivityMatrix();
	}

	
	public double[][] getOffsetVectorMatrixWRMag(){
		return getCurrentCalibDetailsMagWr().getValidOffsetVector();
	}

	public double[][] getAlignmentMatrixMag(){
		return getCurrentCalibDetailsMag().getValidAlignmentMatrix();
	}

	public double[][] getSensitivityMatrixMag(){
		return getCurrentCalibDetailsMag().getValidSensitivityMatrix();
	}

	public double[][] getOffsetVectorMatrixMag(){
		return getCurrentCalibDetailsMag().getValidOffsetVector();
	}
	
	public void updateCurrentMagCalibInUse(){
//		mCurrentCalibDetailsMag = getCurrentCalibDetailsMag();
		mCurrentCalibDetailsMag = getCurrentCalibDetailsIfKinematic(mSensorIdMag, getMagRange());
	}
	
	public void updateCurrentMagWrCalibInUse(){
//		mCurrentCalibDetailsMag = getCurrentCalibDetailsMag();
		mCurrentCalibDetailsMagWr = getCurrentCalibDetailsIfKinematic(mSensorIdWRMag, getWRMagRange());
	}
	
	public int getMagRange() {
		return mMagRange;
	}
	
	public int getWRMagRange() {
		return mWRMagRange;
	}
	
	public void setLISMagRate(int valueToSet){
		mLISMagRate = valueToSet;
	}
	
	public void setLISWRMagRate(int valueToSet){
		mLISWRMagRate = valueToSet;
	}
	
	public int getLISMagRate() {
		return mLISMagRate;
	}
	
	public int getLISWRMagRate() {
		return mLISWRMagRate;
	}
	
	public double getCalibTimeWRMag() {
		return mCurrentCalibDetailsMagWr.getCalibTimeMs();
	}

	public double getCalibTimeMag() {
		return mCurrentCalibDetailsMag.getCalibTimeMs();
	}
	
	public boolean isUsingValidMagParam() {
		if(!UtilShimmer.isAllZeros(getAlignmentMatrixMag()) && !UtilShimmer.isAllZeros(getSensitivityMatrixMag())){
			return true;
		}else{
			return false;
		}
	}
	
	public boolean isUsingValidWRMagParam(){
		if(!UtilShimmer.isAllZeros(getAlignmentMatrixWRMag()) && !UtilShimmer.isAllZeros(getSensitivityMatrixWRMag())){
			return true;
		}else{
			return false;
		}
	}
	
	public void setLISMagRateInternal(int valueToSet) {
		//System.out.println("Accel Rate:\t" + valueToSet);
		//UtilShimmer.consolePrintCurrentStackTrace();
		mLISMagRate = valueToSet;
	}
	
	public void setLISWRMagRateInternal(int valueToSet) {
		//System.out.println("Accel Rate:\t" + valueToSet);
		//UtilShimmer.consolePrintCurrentStackTrace();
		mLISWRMagRate = valueToSet;
	}
	
	public void updateIsUsingDefaultWRMagParam() {
		mIsUsingDefaultWRMagParam = getCurrentCalibDetailsMagWr().isUsingDefaultParameters();
	}
	
	public void updateIsUsingDefaultMagParam() {
		mIsUsingDefaultMagParam = getCurrentCalibDetailsMag().isUsingDefaultParameters();
	}
	//--------- Sensor specific methods end --------------
	
	public int setLIS3MDLMagRateFromFreq(double freq) {
		boolean isEnabled = isSensorEnabled(mSensorIdMag);
		if(isLowPowerMagEnabled()) {
			mLISMagRate = getMagRateFromFreqForSensor(isEnabled, freq, 0);
		} else if(isMedPowerMagEnabled()) {
			mLISMagRate = getMagRateFromFreqForSensor(isEnabled, freq, 1);
		} else if(isHighPowerMagEnabled()) {
			mLISMagRate = getMagRateFromFreqForSensor(isEnabled, freq, 2);
		} else if(isUltraHighPowerMagEnabled()) {
			mLISMagRate = getMagRateFromFreqForSensor(isEnabled, freq, 3);
		}
		return mLISMagRate;
	}

}
