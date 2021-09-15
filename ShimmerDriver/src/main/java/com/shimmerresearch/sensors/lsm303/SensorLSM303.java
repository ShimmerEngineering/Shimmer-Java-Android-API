package com.shimmerresearch.sensors.lsm303;

import java.util.Arrays;
import java.util.TreeMap;

import com.shimmerresearch.driver.FormatCluster;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driver.ConfigByteLayout;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driver.calibration.CalibDetails;
import com.shimmerresearch.driver.calibration.CalibDetailsKinematic;
import com.shimmerresearch.driver.calibration.UtilCalibration;
import com.shimmerresearch.driver.calibration.CalibDetails.CALIB_READ_SOURCE;
import com.shimmerresearch.driver.shimmer2r3.ConfigByteLayoutShimmer3;
import com.shimmerresearch.driverUtilities.ChannelDetails;
import com.shimmerresearch.driverUtilities.ConfigOptionDetailsSensor;
import com.shimmerresearch.driverUtilities.SensorDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;
import com.shimmerresearch.driverUtilities.UtilShimmer;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_TYPE;
import com.shimmerresearch.sensors.AbstractSensor;

public abstract class SensorLSM303 extends AbstractSensor {

	private static final long serialVersionUID = -4885535001690922548L;

	protected int mSensorIdAccel = -1;
	protected int mSensorIdMag = -1;
	
	public abstract int getAccelRateFromFreqForSensor(boolean isEnabled, double freq, boolean isLowPowerMode);
	public abstract int getMagRateFromFreqForSensor(boolean isEnabled, double freq, boolean isLowPowerMode);
	public abstract void setLSM303MagRange(int valueToSet);
	public abstract void setLSM303DigitalAccelRate(int valueToSet);
	public abstract boolean checkLowPowerMag();
	public abstract void setLSM303AccelRange(int valueToSet);

	
	// ----------   Wide-range accel start ---------------
	
	protected int mAccelRange = 0;
	protected boolean mLowPowerAccelWR = false;
	protected boolean mHighResAccelWR = true;
	
	public boolean mIsUsingDefaultWRAccelParam = true;
	
	protected int mLSM303DigitalAccelRate = 0;

	public CalibDetailsKinematic mCurrentCalibDetailsAccelWr = null;//calibDetailsAccelWr2g;

	// ----------   Wide-range accel end ---------------

	// ----------   Mag start ---------------
	protected int mLSM303MagRate = 4;
	
	/**
	 * Just used to create a software limit to the sampling rate for the Mag
	 * (set at 15Hz for LSM303DLHC and 20Hz for LSM303AHTR). This is a legacy
	 * variable that doesn't need to be used.
	 */
	protected boolean mLowPowerMag = false;
	
	public boolean mIsUsingDefaultMagParam = true;

	protected int mMagRange = 0;

	public CalibDetailsKinematic mCurrentCalibDetailsMag = null;//calibDetailsMag1p3;
	// ----------   Mag end ---------------

	
	public class GuiLabelSensors{
		public static final String ACCEL_WR = "Wide-Range Accelerometer"; 
		public static final String MAG = "Magnetometer"; 
	}

	public class LABEL_SENSOR_TILE{
		public static final String MAG = GuiLabelSensors.MAG;
		public static final String WIDE_RANGE_ACCEL = GuiLabelSensors.ACCEL_WR;
	}

	//--------- Configuration options start --------------
//	public static final String[] ListofLSM303AccelRange={"+/- 2g","+/- 4g","+/- 8g","+/- 16g"};  
	public static final String[] ListofLSM303AccelRange={
			UtilShimmer.UNICODE_PLUS_MINUS + " 2g",
			UtilShimmer.UNICODE_PLUS_MINUS + " 4g",
			UtilShimmer.UNICODE_PLUS_MINUS + " 8g",
			UtilShimmer.UNICODE_PLUS_MINUS + " 16g"};  
	

	//--------- Configuration options end --------------

	
	public class GuiLabelConfig{
		public static final String LSM303_ACCEL_RATE = "Wide Range Accel Rate";  
		public static final String LSM303_ACCEL_RANGE = "Wide Range Accel Range"; 
				
		public static final String LSM303_MAG_RANGE = "Mag Range";
		public static final String LSM303_MAG_RATE = "Mag Rate";
		
		public static final String LSM303_ACCEL_LPM = "Wide Range Accel Low-Power Mode"; 
		public static final String LSM303_MAG_LPM = "Mag Low-Power Mode";

		public static final String LSM303_ACCEL_DEFAULT_CALIB = "Wide Range Accel Default Calibration";
		public static final String LSM303_MAG_DEFAULT_CALIB = "Mag Default Calibration";

		//NEW
		public static final String LSM303_ACCEL_CALIB_PARAM = "Wide Range Accel Calibration Details";
		public static final String LSM303_ACCEL_VALID_CALIB = "Wide Range Accel Valid Calibration";
		public static final String LSM303_MAG_CALIB_PARAM = "Mag Calibration Details";
		public static final String LSM303_MAG_VALID_CALIB = "Mag Valid Calibration";
	}
	
	public static class ObjectClusterSensorName{
		public static  String ACCEL_WR_X = "Accel_WR_X";
		public static  String ACCEL_WR_Y = "Accel_WR_Y";
		public static  String ACCEL_WR_Z= "Accel_WR_Z";
		
		public static  String MAG_X = "Mag_X";
		public static  String MAG_Y = "Mag_Y";
		public static  String MAG_Z = "Mag_Z";		
	}


	
    //--------- Constructors for this class start --------------
    /** This constructor is just used for accessing calibration*/
	public SensorLSM303() {
		super(SENSORS.LSM303);
	}
	
	public SensorLSM303(ShimmerVerObject svo) {
		super(SENSORS.LSM303, svo);
	}

	public SensorLSM303(ShimmerDevice shimmerDevice) {
		super(SENSORS.LSM303, shimmerDevice);
	}
    //--------- Constructors for this class end --------------

	//--------- Abstract methods implemented start --------------

	@Override
	public void checkShimmerConfigBeforeConfiguring() {
		if(!isSensorEnabled(mSensorIdAccel)) {
			setDefaultLsm303AccelSensorConfig(false);
		}
		
		if(!isSensorEnabled(mSensorIdMag)) {
			setDefaultLsm303MagSensorConfig(false);
		}
		
		//Added this for Conensys 1.0.0 release - assumes individual sampling rates of each sensor matches the Shimmer sampling
		setLowPowerAccelWR(false);
		setLowPowerMag(false);
	}
	
	@Override 
	public void setSensorSamplingRate(double samplingRateHz) {
		//set sampling rate of the sensors as close to the Shimmer sampling rate as possible (sensor sampling rate >= shimmer sampling rate)
		setLowPowerAccelWR(false);
		setLowPowerMag(false);
		
		setLSM303AccelRateFromFreq(samplingRateHz);
		setLSM303MagRateFromFreq(samplingRateHz);
		
		//TODO
	//	checkLowPowerAccelWR();
		checkLowPowerMag();
	}

	//--------- Abstract methods implemented end --------------

	//--------- Optional methods to override in Sensor Class start --------
	@Override 
	public ObjectCluster processDataCustom(SensorDetails sensorDetails, byte[] rawData, COMMUNICATION_TYPE commType, ObjectCluster objectCluster, boolean isTimeSyncEnabled, double pcTimestampMs) {
		
		// process data originating from the Shimmer
		objectCluster = sensorDetails.processDataCommon(rawData, commType, objectCluster, isTimeSyncEnabled, pcTimestampMs);
		
		if(this instanceof SensorLSM303DLHC){
			//TODO swap Y and Z channels 
		}
		
		//Calibration
		if(mEnableCalibration){
			// get uncalibrated data for each (sub)sensor
			if(sensorDetails.mSensorDetailsRef.mGuiFriendlyLabel.equals(GuiLabelSensors.ACCEL_WR) && mCurrentCalibDetailsAccelWr!=null){
				double[] unCalibratedAccelWrData = new double[3];
				for (ChannelDetails channelDetails:sensorDetails.mListOfChannels){
					//Uncalibrated Accelerometer data
					if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.ACCEL_WR_X)){
						unCalibratedAccelWrData[0] = ((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(channelDetails.mObjectClusterName), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
					}
					else if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.ACCEL_WR_Y)){
						unCalibratedAccelWrData[1]  = ((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(channelDetails.mObjectClusterName), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
					}
					else if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.ACCEL_WR_Z)){
						unCalibratedAccelWrData[2]  = ((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(channelDetails.mObjectClusterName), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
					}
				}
				
				double[] calibratedAccelWrData = UtilCalibration.calibrateInertialSensorData(unCalibratedAccelWrData, mCurrentCalibDetailsAccelWr);
//				double[] calibratedAccelWrData = UtilCalibration.calibrateInertialSensorData(unCalibratedAccelWrData, mAlignmentMatrixWRAccel, mSensitivityMatrixWRAccel, mOffsetVectorWRAccel);
	
				//Add calibrated data to Object cluster
				if(sensorDetails.mSensorDetailsRef.mGuiFriendlyLabel.equals(GuiLabelSensors.ACCEL_WR)){	
					for (ChannelDetails channelDetails:sensorDetails.mListOfChannels){
						if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.ACCEL_WR_X)){
							objectCluster.addCalData(channelDetails, calibratedAccelWrData[0], objectCluster.getIndexKeeper()-3, isUsingDefaultWRAccelParam());
						}
						else if(channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.ACCEL_WR_Y)){
							objectCluster.addCalData(channelDetails, calibratedAccelWrData[1], objectCluster.getIndexKeeper()-2, isUsingDefaultWRAccelParam());
						}
						else if(channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.ACCEL_WR_Z)){
							objectCluster.addCalData(channelDetails, calibratedAccelWrData[2], objectCluster.getIndexKeeper()-1, isUsingDefaultWRAccelParam());
						}
					}
				}
	
				//Debugging
				if(mIsDebugOutput){
					super.consolePrintChannelsCal(objectCluster, Arrays.asList(
							new String[]{ObjectClusterSensorName.ACCEL_WR_X, CHANNEL_TYPE.UNCAL.toString()}, 
							new String[]{ObjectClusterSensorName.ACCEL_WR_Y, CHANNEL_TYPE.UNCAL.toString()}, 
							new String[]{ObjectClusterSensorName.ACCEL_WR_Z, CHANNEL_TYPE.UNCAL.toString()}, 
							new String[]{ObjectClusterSensorName.ACCEL_WR_X, CHANNEL_TYPE.CAL.toString()}, 
							new String[]{ObjectClusterSensorName.ACCEL_WR_Y, CHANNEL_TYPE.CAL.toString()},
							new String[]{ObjectClusterSensorName.ACCEL_WR_Z, CHANNEL_TYPE.CAL.toString()}));
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
		
		ConfigByteLayout configByteLayout = shimmerDevice.getConfigByteLayout();
		if(configByteLayout instanceof ConfigByteLayoutShimmer3){
			ConfigByteLayoutShimmer3 configByteLayoutCast = (ConfigByteLayoutShimmer3) configByteLayout;
			
			configBytes[configByteLayoutCast.idxConfigSetupByte0] |= (byte) ((getLSM303DigitalAccelRate() & configByteLayoutCast.maskLSM303DLHCAccelSamplingRate) << configByteLayoutCast.bitShiftLSM303DLHCAccelSamplingRate);
			configBytes[configByteLayoutCast.idxConfigSetupByte0] |= (byte) ((getAccelRange() & configByteLayoutCast.maskLSM303DLHCAccelRange) << configByteLayoutCast.bitShiftLSM303DLHCAccelRange);
			if(isLowPowerAccelWR()) {
				configBytes[configByteLayoutCast.idxConfigSetupByte0] |= (configByteLayoutCast.maskLSM303DLHCAccelLPM << configByteLayoutCast.bitShiftLSM303DLHCAccelLPM);
			}
			if(isHighResAccelWR()) {
				configBytes[configByteLayoutCast.idxConfigSetupByte0] |= (configByteLayoutCast.maskLSM303DLHCAccelHRM << configByteLayoutCast.bitShiftLSM303DLHCAccelHRM);
			}

			configBytes[configByteLayoutCast.idxConfigSetupByte2] |= (byte) ((getMagRange() & configByteLayoutCast.maskLSM303DLHCMagRange) << configByteLayoutCast.bitShiftLSM303DLHCMagRange);
			configBytes[configByteLayoutCast.idxConfigSetupByte2] |= (byte) ((getLSM303MagRate() & configByteLayoutCast.maskLSM303DLHCMagSamplingRate) << configByteLayoutCast.bitShiftLSM303DLHCMagSamplingRate);

			// LSM303DLHC Magnetometer Calibration Parameters
			byte[] bufferCalibrationParameters = generateCalParamLSM303DLHCMag();
			System.arraycopy(bufferCalibrationParameters, 0, configBytes, configByteLayoutCast.idxLSM303DLHCMagCalibration, configByteLayoutCast.lengthGeneralCalibrationBytes);

			// LSM303DLHC Digital Accel Calibration Parameters
			bufferCalibrationParameters = generateCalParamLSM303DLHCAccel();
			System.arraycopy(bufferCalibrationParameters, 0, configBytes, configByteLayoutCast.idxLSM303DLHCAccelCalibration, configByteLayoutCast.lengthGeneralCalibrationBytes);

		}
		
	}

	
	@Override 
	public void configBytesParse(ShimmerDevice shimmerDevice, byte[] configBytes, COMMUNICATION_TYPE commType) { 
		
		ConfigByteLayout configByteLayout = shimmerDevice.getConfigByteLayout();
		if(configByteLayout instanceof ConfigByteLayoutShimmer3){
			ConfigByteLayoutShimmer3 configByteLayoutCast = (ConfigByteLayoutShimmer3) configByteLayout;

			setLSM303DigitalAccelRate((configBytes[configByteLayoutCast.idxConfigSetupByte0] >> configByteLayoutCast.bitShiftLSM303DLHCAccelSamplingRate) & configByteLayoutCast.maskLSM303DLHCAccelSamplingRate); 
			setLSM303AccelRange((configBytes[configByteLayoutCast.idxConfigSetupByte0] >> configByteLayoutCast.bitShiftLSM303DLHCAccelRange) & configByteLayoutCast.maskLSM303DLHCAccelRange);
			if(((configBytes[configByteLayoutCast.idxConfigSetupByte0] >> configByteLayoutCast.bitShiftLSM303DLHCAccelLPM) & configByteLayoutCast.maskLSM303DLHCAccelLPM) == configByteLayoutCast.maskLSM303DLHCAccelLPM) {
				setLowPowerAccelWR(true);
			}
			else {
				setLowPowerAccelWR(false);
			}
			if(((configBytes[configByteLayoutCast.idxConfigSetupByte0] >> configByteLayoutCast.bitShiftLSM303DLHCAccelHRM) & configByteLayoutCast.maskLSM303DLHCAccelHRM) == configByteLayoutCast.maskLSM303DLHCAccelHRM) {
				setHighResAccelWR(true);
			}
			else {
				setHighResAccelWR(false);
			}

			setLSM303MagRange((configBytes[configByteLayoutCast.idxConfigSetupByte2] >> configByteLayoutCast.bitShiftLSM303DLHCMagRange) & configByteLayoutCast.maskLSM303DLHCMagRange);
			setLSM303MagRate((configBytes[configByteLayoutCast.idxConfigSetupByte2] >> configByteLayoutCast.bitShiftLSM303DLHCMagSamplingRate) & configByteLayoutCast.maskLSM303DLHCMagSamplingRate);
			checkLowPowerMag(); // check rate to determine if Sensor is in LPM mode

			if (shimmerDevice.isConnected()){
				getCurrentCalibDetailsMag().mCalibReadSource=CALIB_READ_SOURCE.INFOMEM;
				getCurrentCalibDetailsAccelWr().mCalibReadSource=CALIB_READ_SOURCE.INFOMEM;
			}

			// LSM303DLHC Magnetometer Calibration Parameters
			byte[] bufferCalibrationParameters = new byte[configByteLayoutCast.lengthGeneralCalibrationBytes];
			System.arraycopy(configBytes, configByteLayoutCast.idxLSM303DLHCMagCalibration, bufferCalibrationParameters, 0 , configByteLayoutCast.lengthGeneralCalibrationBytes);
			parseCalibParamFromPacketMag(bufferCalibrationParameters, CALIB_READ_SOURCE.INFOMEM);

			// LSM303DLHC Digital Accel Calibration Parameters
			bufferCalibrationParameters = new byte[configByteLayoutCast.lengthGeneralCalibrationBytes];
			System.arraycopy(configBytes, configByteLayoutCast.idxLSM303DLHCAccelCalibration, bufferCalibrationParameters, 0 , configByteLayoutCast.lengthGeneralCalibrationBytes);
			parseCalibParamFromPacketAccelLsm(bufferCalibrationParameters, CALIB_READ_SOURCE.INFOMEM);
		}
	}

	
	@Override 
	public Object setConfigValueUsingConfigLabel(Integer sensorId, String configLabel, Object valueToSet) {
		Object returnValue = null;
		
		switch(configLabel){
			case(GuiLabelConfig.LSM303_ACCEL_LPM):
				setLowPowerAccelWR((boolean)valueToSet);
				break;
			case(GuiLabelConfig.LSM303_MAG_LPM):
				setLowPowerMag((boolean)valueToSet);
				break;
			case(GuiLabelConfig.LSM303_ACCEL_RANGE):
				setLSM303AccelRange((int)valueToSet);
				break;
			case(GuiLabelConfig.LSM303_MAG_RANGE):
				setLSM303MagRange((int)valueToSet);
				break;
			case(GuiLabelConfig.LSM303_ACCEL_RATE):
				setLSM303DigitalAccelRate((int)valueToSet);
				break;
			case(GuiLabelConfig.LSM303_MAG_RATE):
				setLSM303MagRate((int)valueToSet);
				break;
				
//			case(GuiLabelConfigCommon.KINEMATIC_CALIBRATION_ALL):
//				TreeMap<Integer, TreeMap<Integer, CalibDetails>> mapOfKinematicSensorCalibration = (TreeMap<Integer, TreeMap<Integer, CalibDetails>>) valueToSet;
//				setCalibration(mapOfKinematicSensorCalibration);
//				returnValue = valueToSet;
//	    		break;
			case(GuiLabelConfigCommon.RANGE):
				if(sensorId==mSensorIdAccel){
					this.setConfigValueUsingConfigLabel(GuiLabelConfig.LSM303_ACCEL_RANGE, valueToSet);
				}
				else if(sensorId==mSensorIdMag){
					this.setConfigValueUsingConfigLabel(GuiLabelConfig.LSM303_MAG_RANGE, valueToSet);
				}
				break;
			case(GuiLabelConfigCommon.RATE):
				if(sensorId==mSensorIdAccel){
					this.setConfigValueUsingConfigLabel(GuiLabelConfig.LSM303_ACCEL_RATE, valueToSet);
				}
				else if(sensorId==mSensorIdMag){
					this.setConfigValueUsingConfigLabel(GuiLabelConfig.LSM303_MAG_RANGE, valueToSet);
				}
				break;
			default:
				returnValue = super.setConfigValueUsingConfigLabelCommon(sensorId, configLabel, valueToSet);
				break;
		}	
		
        if(configLabel.equals(SensorLSM303.GuiLabelConfig.LSM303_ACCEL_RATE)){
        	checkConfigOptionValues(configLabel);
        }
		
		return returnValue;
	}

	
	@Override 
	public Object getConfigValueUsingConfigLabel(Integer sensorId, String configLabel) {
		Object returnValue = null;
		
		if(configLabel.equals(GuiLabelConfig.LSM303_ACCEL_RATE)){
        	checkConfigOptionValues(configLabel);
        }
		
		switch(configLabel){
			case(GuiLabelConfig.LSM303_ACCEL_LPM):
				returnValue = isLSM303DigitalAccelLPM();
	        	break;
			case(GuiLabelConfig.LSM303_MAG_LPM):
				returnValue = checkLowPowerMag();
	        	break;
			case(GuiLabelConfig.LSM303_ACCEL_RANGE): 
				returnValue = getAccelRange();
		    	break;
			case(GuiLabelConfig.LSM303_MAG_RANGE):
				//TODO check below and commented out code (RS (20/5/2016): Same as in ShimmerObject.)
				returnValue = getMagRange();
			
		//						// firmware sets mag range to 7 (i.e. index 6 in combobox) if user set mag range to 0 in config file
		//						if(getMagRange() == 0) cmBx.setSelectedIndex(6);
		//						else cmBx.setSelectedIndex(getMagRange()-1);
				break;
			case(GuiLabelConfig.LSM303_ACCEL_RATE): 
				int configValue = getLSM303DigitalAccelRate(); 
				 
//		    	if(!isLSM303DigitalAccelLPM()) {
//		        	if(configValue==8) {
//		        		//TODO:
//		        		/*RS (20/5/2016): Why returning a different value?
//		        		 * In the Set-method the compatibility-check for Accel Rates supported for Low Power Mode is made.
//		        		 * In this get-method the it should just read/get the value, not manipulating it.
//		        		 * */
//		        		configValue = 9;
//		        	}
//		    	}
				returnValue = configValue;
				break;
			case(GuiLabelConfig.LSM303_MAG_RATE):
				returnValue = getLSM303MagRate();
	        	break;
	        	
//			case(Configuration.Shimmer3.GuiLabelConfig.KINEMATIC_CALIBRATION_ALL):
//				returnValue = getKinematicCalibration();
//				break;
			case(GuiLabelConfigCommon.RANGE):
				if(sensorId==mSensorIdAccel){
					returnValue = this.getConfigValueUsingConfigLabel(GuiLabelConfig.LSM303_ACCEL_RANGE);
				}
				else if(sensorId==mSensorIdMag){
					returnValue = this.getConfigValueUsingConfigLabel(GuiLabelConfig.LSM303_MAG_RANGE);
				}
				break;
			case(GuiLabelConfigCommon.RATE):
				if(sensorId==mSensorIdAccel){
					returnValue = this.getConfigValueUsingConfigLabel(GuiLabelConfig.LSM303_ACCEL_RATE);
				}
				else if(sensorId==mSensorIdMag){
					returnValue = this.getConfigValueUsingConfigLabel(GuiLabelConfig.LSM303_MAG_RATE);
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
			if(sensorId==mSensorIdAccel) {
				setDefaultLsm303AccelSensorConfig(isSensorEnabled);		
			}
			else if(sensorId==mSensorIdMag) {
				setDefaultLsm303MagSensorConfig(isSensorEnabled);
			}
			return true;
		}
		return false;
	}
	
	@Override 
	public boolean checkConfigOptionValues(String stringKey) {		
		if(mConfigOptionsMap.containsKey(stringKey)){
			if(stringKey==GuiLabelConfig.LSM303_ACCEL_RATE){
				if(isLSM303DigitalAccelLPM()) {
					mConfigOptionsMap.get(stringKey).setIndexOfValuesToUse(ConfigOptionDetailsSensor.VALUE_INDEXES.LSM303_ACCEL_RATE.IS_LPM);
				}
				else {
					mConfigOptionsMap.get(stringKey).setIndexOfValuesToUse(ConfigOptionDetailsSensor.VALUE_INDEXES.LSM303_ACCEL_RATE.NOT_LPM);
					// double check that rate is compatible with LPM (8 not compatible so set to higher rate) 
//					setLSM303DigitalAccelRate(mLSM303DigitalAccelRate);
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
		if(sensorId==mSensorIdAccel){
			return isUsingDefaultWRAccelParam();
		}
		else if(sensorId==mSensorIdMag){
			return isUsingDefaultMagParam();
		}
		return false;
	}

	@Override
	public void setCalibrationMapPerSensor(int sensorId, TreeMap<Integer, CalibDetails> mapOfSensorCalibration) {
		super.setCalibrationMapPerSensor(sensorId, mapOfSensorCalibration);
		updateCurrentAccelWrCalibInUse();
		updateCurrentMagCalibInUse();
	}

	//--------- Optional methods to override in Sensor Class end --------
	
	
	//--------- Sensor specific methods start --------------

	/**XXX
	 * RS (17/05/2016): Two questions with regards to the information below the questions:
	 * 
	 * 		What additional lower power mode is used?
	 * 		Why would the '2g' range not be support by this low power mode -> where is this mentioned in the datasheet?
	 *  
	 * This enables the low power accel option. When not enabled the sampling
	 * rate of the accel is set to the closest value to the actual sampling rate
	 * that it can achieve. In low power mode it defaults to 10Hz. Also and
	 * additional low power mode is used for the LSM303DLHC. This command will
	 * only supports the following Accel range +4g, +8g , +16g
	 */
	public void setHighResAccelWR(boolean enable) {
		mHighResAccelWR = enable;
	}
	
	public void setHighResAccelWR(int i){
		mHighResAccelWR = (i>0)? true:false;
	}
	
	public void setLowPowerAccelWR(boolean enable){
		mLowPowerAccelWR = enable;
		mHighResAccelWR = !enable;
		if(mShimmerDevice!=null){
			setLSM303AccelRateFromFreq(getSamplingRateShimmer());
		}
	}
	
	public void	setLowPowerMag(boolean enable){
		mLowPowerMag = enable;
		if(mShimmerDevice!=null){
			setLSM303MagRateFromFreq(getSamplingRateShimmer());
		}
	}

	public boolean isLowPowerMagEnabled(){
		return mLowPowerMag;
	}

	public int getLowPowerMagEnabled() {
		return (isLowPowerMagEnabled()? 1:0);
	}

	
	public void setDefaultLsm303MagSensorConfig(boolean isSensorEnabled) {
		if(isSensorEnabled) {
			setLowPowerMag(false);
		}
		else {
			setLSM303MagRange(1);
			setLowPowerMag(true);
		}		
	}

	
	public void setDefaultLsm303AccelSensorConfig(boolean isSensorEnabled) {
		if(isSensorEnabled) {
			setLowPowerAccelWR(false);
		}
		else {
			setLSM303AccelRange(0);
			setLowPowerAccelWR(true);
		}
	}
	
	
	/**
	 * Computes next higher available sensor sampling rate setting based on
	 * passed in "freq" variable and dependent on whether low-power mode is set.
	 * 
	 * @param freq
	 * @return int the rate configuration setting for the respective sensor
	 */
	public int setLSM303AccelRateFromFreq(double freq) {
		boolean isEnabled = isSensorEnabled(mSensorIdAccel);
//		System.out.println("Setting Sampling Rate: " + freq + "\tmLowPowerAccelWR:" + mLowPowerAccelWR);
		setLSM303DigitalAccelRateInternal(getAccelRateFromFreqForSensor(isEnabled, freq, mLowPowerAccelWR));
		return mLSM303DigitalAccelRate;
	}

	/**
	 * Computes next higher available sensor sampling rate setting based on
	 * passed in "freq" variable and dependent on whether low-power mode is set.
	 * 
	 * @param freq
	 * @return int the rate configuration setting for the respective sensor
	 */
	public int setLSM303MagRateFromFreq(double freq) {
		boolean isEnabled = isSensorEnabled(mSensorIdMag);
		mLSM303MagRate = getMagRateFromFreqForSensor(isEnabled, freq, isLowPowerMagEnabled());
		return mLSM303MagRate;
	}



	public CalibDetailsKinematic getCurrentCalibDetailsMag(){
//		return getCurrentCalibDetails(mSensorIdMag, getMagRange());
		if(mCurrentCalibDetailsMag==null){
			updateCurrentMagCalibInUse();
		}
		return mCurrentCalibDetailsMag;
	}

	public CalibDetailsKinematic getCurrentCalibDetailsAccelWr(){
//		return getCurrentCalibDetails(mSensorIdAccel, getAccelRange());
		return mCurrentCalibDetailsAccelWr;
	}

	public byte[] generateCalParamLSM303DLHCAccel(){
		return mCurrentCalibDetailsAccelWr.generateCalParamByteArray();
	}
	
	
	public byte[] generateCalParamLSM303DLHCMag(){
		return getCurrentCalibDetailsMag().generateCalParamByteArray();
	}
	
	public void parseCalibParamFromPacketAccelLsm(byte[] bufferCalibrationParameters, CALIB_READ_SOURCE calibReadSource) {
		mCurrentCalibDetailsAccelWr.parseCalParamByteArray(bufferCalibrationParameters, calibReadSource);
	}

	public void parseCalibParamFromPacketMag(byte[] bufferCalibrationParameters, CALIB_READ_SOURCE calibReadSource) {
		getCurrentCalibDetailsMag().parseCalParamByteArray(bufferCalibrationParameters, calibReadSource);
	}
	
	
	public void setDefaultCalibrationShimmer3WideRangeAccel() {
		mCurrentCalibDetailsAccelWr.resetToDefaultParameters();
	}

	
	public void setDefaultCalibrationShimmer3Mag() {
		getCurrentCalibDetailsMag().resetToDefaultParameters();
	}

	public boolean isUsingDefaultWRAccelParam(){
		return mCurrentCalibDetailsAccelWr.isUsingDefaultParameters(); 
	}
	
	public boolean isUsingDefaultMagParam(){
		return getCurrentCalibDetailsMag().isUsingDefaultParameters(); 
	}

	public double[][] getAlignmentMatrixWRAccel(){
		return mCurrentCalibDetailsAccelWr.getValidAlignmentMatrix();
	}

	
	public double[][] getSensitivityMatrixWRAccel(){
		return mCurrentCalibDetailsAccelWr.getValidSensitivityMatrix();
	}

	
	public double[][] getOffsetVectorMatrixWRAccel(){
		return mCurrentCalibDetailsAccelWr.getValidOffsetVector();
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
	
	public void updateCurrentAccelWrCalibInUse(){
//		mCurrentCalibDetailsAccelWr = getCurrentCalibDetailsAccelWr();
		mCurrentCalibDetailsAccelWr = getCurrentCalibDetailsIfKinematic(mSensorIdAccel, getAccelRange());
	}

	
	public int getAccelRange() {
		return mAccelRange;
	}
	
	public int getMagRange() {
		return mMagRange;
	}

	public void setLSM303MagRate(int valueToSet){
		mLSM303MagRate = valueToSet;
	}
	
	public int getLSM303MagRate() {
		return mLSM303MagRate;
	}
	
	public boolean isHighResAccelWR(){
		return isLSM303DigitalAccelHRM();
	}
	

	//TODO Returning same variable as isHighResAccelWr() -> remove one method?
	public boolean isLSM303DigitalAccelHRM() {
		return mHighResAccelWR;
	}
	
	public int getHighResAccelWREnabled(){
		return (mHighResAccelWR? 1:0);
	}


	//TODO Returning same variable as isLowPowerAccelWr() -> remove one method?
	public boolean isLSM303DigitalAccelLPM() {
		return mLowPowerAccelWR;
	}

	public void setLowPowerAccelEnabled(int i){
		mLowPowerAccelWR = (i>0)? true:false;
	}

	public int getLowPowerAccelEnabled(){
		return (isLSM303DigitalAccelLPM()? 1:0);
	}

	public boolean isLowPowerAccelWR(){
		return isLSM303DigitalAccelLPM();
	}
	
	//TODO Returning same variable as isLowPowerAccelWr() -> remove one method?
	public boolean isLowPowerAccelEnabled() {
		return isLSM303DigitalAccelLPM();
	}

	public int getLSM303DigitalAccelRate() {
		return mLSM303DigitalAccelRate;
	}
	
	public double getCalibTimeWRAccel() {
		return mCurrentCalibDetailsAccelWr.getCalibTimeMs();
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
	
	public boolean isUsingValidWRAccelParam(){
		if(!UtilShimmer.isAllZeros(getAlignmentMatrixWRAccel()) && !UtilShimmer.isAllZeros(getSensitivityMatrixWRAccel())){
			return true;
		}else{
			return false;
		}
	}
	
	public void setLSM303DigitalAccelRateInternal(int valueToSet) {
		//System.out.println("Accel Rate:\t" + valueToSet);
		//UtilShimmer.consolePrintCurrentStackTrace();
		mLSM303DigitalAccelRate = valueToSet;
	}
	
	public void updateIsUsingDefaultWRAccelParam() {
		mIsUsingDefaultWRAccelParam = getCurrentCalibDetailsAccelWr().isUsingDefaultParameters();
	}
	
	public void updateIsUsingDefaultMagParam() {
		mIsUsingDefaultMagParam = getCurrentCalibDetailsMag().isUsingDefaultParameters();
	}
	//--------- Sensor specific methods end --------------


}
