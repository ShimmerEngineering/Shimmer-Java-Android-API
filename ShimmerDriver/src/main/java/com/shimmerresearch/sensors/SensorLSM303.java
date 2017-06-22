package com.shimmerresearch.sensors;

import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;

import com.shimmerresearch.driver.FormatCluster;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driver.calibration.CalibDetails;
import com.shimmerresearch.driver.calibration.CalibDetailsKinematic;
import com.shimmerresearch.driver.calibration.UtilCalibration;
import com.shimmerresearch.driver.calibration.CalibDetails.CALIB_READ_SOURCE;
import com.shimmerresearch.driverUtilities.ChannelDetails;
import com.shimmerresearch.driverUtilities.ConfigOptionDetailsSensor;
import com.shimmerresearch.driverUtilities.SensorDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_TYPE;

public abstract class SensorLSM303 extends AbstractSensor {

	private static final long serialVersionUID = -4885535001690922548L;

	
	protected int mSensorMapKeyAccel = -1;
	protected int mSensorMapKeyMag = -1;
	
	public abstract int getAccelRateFromFreqForSensor(boolean isEnabled, double freq, boolean isLowPowerMode);
	public abstract int getMagRateFromFreqForSensor(boolean isEnabled, double freq, boolean isLowPowerMode);
	public abstract void setLSM303MagRange(int valueToSet);
	
	// ----------   Wide-range accel start ---------------
	
	protected int mAccelRange = 0;
	protected boolean mLowPowerAccelWR = false;
	protected boolean mHighResAccelWR = true;
	
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

	protected int mMagRange = 1;

	public CalibDetailsKinematic mCurrentCalibDetailsMag = null;//calibDetailsMag1p3;
	// ----------   Mag end ---------------

	
	public class GuiLabelSensors{
		public static final String ACCEL_WR = "Wide-Range Accelerometer"; 
		public static final String MAG = "Magnetometer"; 
	}

	public class GuiLabelSensorTiles{
		public static final String MAG = GuiLabelSensors.MAG;
		public static final String WIDE_RANGE_ACCEL = GuiLabelSensors.ACCEL_WR;
	}

	//--------- Configuration options start --------------
	public static final String[] ListofLSM303AccelRange={"+/- 2g","+/- 4g","+/- 8g","+/- 16g"};  
	public static final Integer[] ListofLSM303AccelRangeConfigValues={0,1,2,3};  
	
	public static final ConfigOptionDetailsSensor configOptionAccelLpm = new ConfigOptionDetailsSensor(
			 ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.CHECKBOX);

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
		setLowPowerAccelWR(false);
		setLowPowerMag(false);
		
		if(!isSensorEnabled(mSensorMapKeyAccel)) {
			setDefaultLsm303AccelSensorConfig(false);
		}
		
		if(!isSensorEnabled(mSensorMapKeyMag)) {
			setDefaultLsm303MagSensorConfig(false);
		}
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
	public ObjectCluster processDataCustom(SensorDetails sensorDetails, byte[] rawData, COMMUNICATION_TYPE commType, ObjectCluster objectCluster, boolean isTimeSyncEnabled, long pcTimestamp) {
		
		// process data originating from the Shimmer
		objectCluster = sensorDetails.processDataCommon(rawData, commType, objectCluster, isTimeSyncEnabled, pcTimestamp);
		
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
	public void configByteArrayGenerate(ShimmerDevice shimmerDevice, byte[] mInfoMemBytes) {//XXX - What is "ShimmerDevice shimmerDevice" doing here? 
		int idxConfigSetupByte0 =              		6; 
		int idxConfigSetupByte2 =              		8;
//		int idxLSM303DLHCAccelCalibration =    	   94; 
//		int idxLSM303DLHCMagCalibration =          73;
		//fix for newer firmware -> see InfomemLayoutShimmer3
		int idxLSM303DLHCMagCalibration =   76;
		int idxLSM303DLHCAccelCalibration = 97;
		int bitShiftLSM303DLHCAccelSamplingRate =   4;
		int bitShiftLSM303DLHCAccelRange =          2;
		int bitShiftLSM303DLHCAccelLPM =            1;
		int bitShiftLSM303DLHCAccelHRM =            0;
		int bitShiftLSM303DLHCMagRange =            5;
		int bitShiftLSM303DLHCMagSamplingRate =     2;
		int maskLSM303DLHCAccelSamplingRate =    0x0F;   
		int maskLSM303DLHCAccelRange =           0x03;
		int maskLSM303DLHCAccelLPM =             0x01;
		int maskLSM303DLHCAccelHRM =             0x01;
		int maskLSM303DLHCMagRange =             0x07;
		int maskLSM303DLHCMagSamplingRate =      0x07;
		int lengthGeneralCalibrationBytes =        21;
		
		//idxConfigSetupByte0 
		mInfoMemBytes[idxConfigSetupByte0] |= (byte) ((mLSM303DigitalAccelRate & maskLSM303DLHCAccelSamplingRate) << bitShiftLSM303DLHCAccelSamplingRate);
		mInfoMemBytes[idxConfigSetupByte0] |= (byte) ((getAccelRange() & maskLSM303DLHCAccelRange) << bitShiftLSM303DLHCAccelRange);
		if(mLowPowerAccelWR) {
			mInfoMemBytes[idxConfigSetupByte0] |= (maskLSM303DLHCAccelLPM << bitShiftLSM303DLHCAccelLPM);
		}
		if(mHighResAccelWR) {
			mInfoMemBytes[idxConfigSetupByte0] |= (maskLSM303DLHCAccelHRM << bitShiftLSM303DLHCAccelHRM);
		}
		
		//idxConfigSetupByte2
		mInfoMemBytes[idxConfigSetupByte2] |= (byte) ((getMagRange() & maskLSM303DLHCMagRange) << bitShiftLSM303DLHCMagRange);
		mInfoMemBytes[idxConfigSetupByte2] |= (byte) ((getLSM303MagRate() & maskLSM303DLHCMagSamplingRate) << bitShiftLSM303DLHCMagSamplingRate);
		
		// LSM303DLHC Digital Accel Calibration Parameters
		byte[] bufferCalibrationParameters = generateCalParamLSM303DLHCAccel();
		System.arraycopy(bufferCalibrationParameters, 0, mInfoMemBytes, idxLSM303DLHCAccelCalibration, lengthGeneralCalibrationBytes);
		
		// LSM303DLHC Magnetometer Calibration Parameters
		bufferCalibrationParameters = generateCalParamLSM303DLHCMag();
		System.arraycopy(bufferCalibrationParameters, 0, mInfoMemBytes, idxLSM303DLHCMagCalibration, lengthGeneralCalibrationBytes);
	}

	
	@Override 
	public void configByteArrayParse(ShimmerDevice shimmerDevice, byte[] mInfoMemBytes) {//XXX - What is "ShimmerDevice shimmerDevice" doing here? 
		int idxConfigSetupByte0 =              		6; 
		int idxConfigSetupByte2 =              		8;
//		int idxLSM303DLHCAccelCalibration =    	   94; 
//		int idxLSM303DLHCMagCalibration =          73;
		//fix for newer firmware -> see InfomemLayoutShimmer3
		int idxLSM303DLHCMagCalibration =   76;
		int idxLSM303DLHCAccelCalibration = 97;
		int bitShiftLSM303DLHCAccelSamplingRate =   4;
		int bitShiftLSM303DLHCAccelRange =          2;
		int bitShiftLSM303DLHCAccelLPM =            1;
		int bitShiftLSM303DLHCAccelHRM =            0;
		int bitShiftLSM303DLHCMagRange =            5;
		int bitShiftLSM303DLHCMagSamplingRate =     2;
		int maskLSM303DLHCAccelSamplingRate =    0x0F;   
		int maskLSM303DLHCAccelRange =           0x03;
		int maskLSM303DLHCAccelLPM =             0x01;
		int maskLSM303DLHCAccelHRM =             0x01;
		int maskLSM303DLHCMagRange =             0x07;
		int maskLSM303DLHCMagSamplingRate =      0x07;
		int lengthGeneralCalibrationBytes =        21;
		
		//idxConfigSetupByte0 
		mLSM303DigitalAccelRate = (mInfoMemBytes[idxConfigSetupByte0] >> bitShiftLSM303DLHCAccelSamplingRate) & maskLSM303DLHCAccelSamplingRate; 
		setLSM303AccelRange((mInfoMemBytes[idxConfigSetupByte0] >> bitShiftLSM303DLHCAccelRange) & maskLSM303DLHCAccelRange);
		if(((mInfoMemBytes[idxConfigSetupByte0] >> bitShiftLSM303DLHCAccelLPM) & maskLSM303DLHCAccelLPM) == maskLSM303DLHCAccelLPM) {
			mLowPowerAccelWR = true;
		}
		else {
			mLowPowerAccelWR = false;
		}
		if(((mInfoMemBytes[idxConfigSetupByte0] >> bitShiftLSM303DLHCAccelHRM) & maskLSM303DLHCAccelHRM) == maskLSM303DLHCAccelHRM) {
			mHighResAccelWR = true;
		}
		else {
			mHighResAccelWR = false;
		}
		
		//idxConfigSetupByte2
		setLSM303MagRange((mInfoMemBytes[idxConfigSetupByte2] >> bitShiftLSM303DLHCMagRange) & maskLSM303DLHCMagRange);
		setLSM303MagRate((mInfoMemBytes[idxConfigSetupByte2] >> bitShiftLSM303DLHCMagSamplingRate) & maskLSM303DLHCMagSamplingRate);
		checkLowPowerMag(); // check rate to determine if Sensor is in LPM mode
		
		// LSM303DLHC Digital Accel Calibration Parameters
		byte[] bufferCalibrationParameters = new byte[lengthGeneralCalibrationBytes];
		System.arraycopy(mInfoMemBytes, idxLSM303DLHCAccelCalibration, bufferCalibrationParameters, 0 , lengthGeneralCalibrationBytes);
//		retrieveKinematicCalibrationParametersFromPacket(bufferCalibrationParameters, LSM303DLHC_ACCEL_CALIBRATION_RESPONSE);
		parseCalibParamFromPacketAccelLsm(bufferCalibrationParameters, CALIB_READ_SOURCE.INFOMEM);
		
		// LSM303DLHC Magnetometer Calibration Parameters
		bufferCalibrationParameters = new byte[lengthGeneralCalibrationBytes];
		System.arraycopy(mInfoMemBytes, idxLSM303DLHCMagCalibration, bufferCalibrationParameters, 0 , lengthGeneralCalibrationBytes);
//		retrieveKinematicCalibrationParametersFromPacket(bufferCalibrationParameters, MAG_CALIBRATION_RESPONSE);
		parseCalibParamFromPacketMag(bufferCalibrationParameters, CALIB_READ_SOURCE.INFOMEM);
	}

	
	@Override 
	public Object setConfigValueUsingConfigLabel(Integer sensorMapKey, String configLabel, Object valueToSet) {
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
				if(sensorMapKey==mSensorMapKeyAccel){
					this.setConfigValueUsingConfigLabel(GuiLabelConfig.LSM303_ACCEL_RANGE, valueToSet);
				}
				else if(sensorMapKey==mSensorMapKeyMag){
					this.setConfigValueUsingConfigLabel(GuiLabelConfig.LSM303_MAG_RANGE, valueToSet);
				}
				break;
			case(GuiLabelConfigCommon.RATE):
				if(sensorMapKey==mSensorMapKeyAccel){
					this.setConfigValueUsingConfigLabel(GuiLabelConfig.LSM303_ACCEL_RATE, valueToSet);
				}
				else if(sensorMapKey==mSensorMapKeyMag){
					this.setConfigValueUsingConfigLabel(GuiLabelConfig.LSM303_MAG_RANGE, valueToSet);
				}
				break;
			default:
				returnValue = super.setConfigValueUsingConfigLabelCommon(sensorMapKey, configLabel, valueToSet);
				break;
		}		
		return returnValue;
	}

	
	@Override 
	public Object getConfigValueUsingConfigLabel(Integer sensorMapKey, String configLabel) {
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
				 
		    	if(!isLSM303DigitalAccelLPM()) {
		        	if(configValue==8) {
		        		//TODO:
		        		/*RS (20/5/2016): Why returning a different value?
		        		 * In the Set-method the compatibility-check for Accel Rates supported for Low Power Mode is made.
		        		 * In this get-method the it should just read/get the value, not manipulating it.
		        		 * */
		        		configValue = 9;
		        	}
		    	}
				returnValue = configValue;
				break;
		
			case(GuiLabelConfig.LSM303_MAG_RATE):
				returnValue = getLSM303MagRate();
	        	break;
	        	
//			case(Configuration.Shimmer3.GuiLabelConfig.KINEMATIC_CALIBRATION_ALL):
//				returnValue = getKinematicCalibration();
//				break;
			case(GuiLabelConfigCommon.RANGE):
				if(sensorMapKey==mSensorMapKeyAccel){
					returnValue = this.getConfigValueUsingConfigLabel(GuiLabelConfig.LSM303_ACCEL_RANGE);
				}
				else if(sensorMapKey==mSensorMapKeyMag){
					returnValue = this.getConfigValueUsingConfigLabel(GuiLabelConfig.LSM303_MAG_RANGE);
				}
				break;
			case(GuiLabelConfigCommon.RATE):
				if(sensorMapKey==mSensorMapKeyAccel){
					returnValue = this.getConfigValueUsingConfigLabel(GuiLabelConfig.LSM303_ACCEL_RATE);
				}
				else if(sensorMapKey==mSensorMapKeyMag){
					returnValue = this.getConfigValueUsingConfigLabel(GuiLabelConfig.LSM303_MAG_RATE);
				}
				break;
			default:
				returnValue = super.getConfigValueUsingConfigLabelCommon(sensorMapKey, configLabel);
				break;
			
		}
		return returnValue;
	
	}

	@Override 
	public boolean setDefaultConfigForSensor(int sensorMapKey, boolean isSensorEnabled) {
		if(mSensorMap.containsKey(sensorMapKey)){
			if(sensorMapKey==mSensorMapKeyAccel) {
				setDefaultLsm303AccelSensorConfig(isSensorEnabled);		
			}
			else if(sensorMapKey==mSensorMapKeyMag) {
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
					setLSM303DigitalAccelRate(mLSM303DigitalAccelRate);
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
	public boolean isSensorUsingDefaultCal(int sensorMapKey) {
		if(sensorMapKey==mSensorMapKeyAccel){
			return isUsingDefaultWRAccelParam();
		}
		else if(sensorMapKey==mSensorMapKeyMag){
			return isUsingDefaultMagParam();
		}
		return false;
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
	
	public void setLowPowerAccelWR(boolean enable){
		mLowPowerAccelWR = enable;
		mHighResAccelWR = !enable;
		setLSM303AccelRateFromFreq(mMaxSetShimmerSamplingRate);
	}
	
	public void setLSM303DigitalAccelRate(int valueToSet) {
		mLSM303DigitalAccelRate = valueToSet;
		//LPM is not compatible with mLSM303DigitalAccelRate == 8, set to next higher rate
		if(mLowPowerAccelWR && (valueToSet==8)) {
			mLSM303DigitalAccelRate = 9;
		}
	}
	
	protected boolean checkLowPowerMag() {
		setLowPowerMag((getLSM303MagRate() <= 4)? true:false);
		return isLowPowerMagEnabled();
	}
	
	public void	setLowPowerMag(boolean enable){
		mLowPowerMag = enable;
		setLSM303MagRateFromFreq(mMaxSetShimmerSamplingRate);
	}

	public boolean isLowPowerMagEnabled(){
		return mLowPowerMag;
	}

	public int getLowPowerMagEnabled() {
		return (isLowPowerMagEnabled()? 1:0);
	}

	
	protected void setDefaultLsm303MagSensorConfig(boolean isSensorEnabled) {
		if(isSensorEnabled) {
			setLowPowerMag(false);
		}
		else {
			setLSM303MagRange(1);
			setLowPowerMag(true);
		}		
	}

	
	protected void setDefaultLsm303AccelSensorConfig(boolean isSensorEnabled) {
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
		boolean isEnabled = isSensorEnabled(mSensorMapKeyAccel);
		mLSM303DigitalAccelRate = getAccelRateFromFreqForSensor(isEnabled, freq, mLowPowerAccelWR);
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
		boolean isEnabled = isSensorEnabled(mSensorMapKeyMag);
		mLSM303MagRate = getMagRateFromFreqForSensor(isEnabled, freq, isLowPowerMagEnabled());
		return mLSM303MagRate;
	}



	public CalibDetailsKinematic getCurrentCalibDetailsMag(){
		return getCurrentCalibDetails(mSensorMapKeyMag, getMagRange());
	}

	public CalibDetailsKinematic getCurrentCalibDetailsAccelWr(){
		return getCurrentCalibDetails(mSensorMapKeyAccel, getAccelRange());
	}

	protected byte[] generateCalParamLSM303DLHCAccel(){
		return mCurrentCalibDetailsAccelWr.generateCalParamByteArray();
	}
	
	
	protected byte[] generateCalParamLSM303DLHCMag(){
		return mCurrentCalibDetailsMag.generateCalParamByteArray();
	}
	
	public void parseCalibParamFromPacketAccelLsm(byte[] bufferCalibrationParameters, CALIB_READ_SOURCE calibReadSource) {
		mCurrentCalibDetailsAccelWr.parseCalParamByteArray(bufferCalibrationParameters, calibReadSource);
	}

	public void parseCalibParamFromPacketMag(byte[] bufferCalibrationParameters, CALIB_READ_SOURCE calibReadSource) {
		mCurrentCalibDetailsMag.parseCalParamByteArray(bufferCalibrationParameters, calibReadSource);
	}
	
	
	private void setDefaultCalibrationShimmer3WideRangeAccel() {
		mCurrentCalibDetailsAccelWr.resetToDefaultParameters();
	}

	
	private void setDefaultCalibrationShimmer3Mag() {
		mCurrentCalibDetailsMag.resetToDefaultParameters();
	}

	public boolean isUsingDefaultWRAccelParam(){
		return mCurrentCalibDetailsAccelWr.isUsingDefaultParameters(); 
	}
	
	public boolean isUsingDefaultMagParam(){
		return mCurrentCalibDetailsMag.isUsingDefaultParameters(); 
	}

	public CalibDetailsKinematic getCurrentCalibDetails(int sensorMapKey, int range){
		CalibDetails calibPerSensor = getCalibForSensor(sensorMapKey, range);
		if(calibPerSensor!=null){
			return (CalibDetailsKinematic) calibPerSensor;
		}
		return null;
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
		return mCurrentCalibDetailsMag.getValidAlignmentMatrix();
	}

	public double[][] getSensitivityMatrixMag(){
		return mCurrentCalibDetailsMag.getValidSensitivityMatrix();
	}

	public double[][] getOffsetVectorMatrixMag(){
		return mCurrentCalibDetailsMag.getValidOffsetVector();
	}


	public void updateCurrentMagCalibInUse(){
		mCurrentCalibDetailsMag = getCurrentCalibDetailsMag();
	}
	
	public void updateCurrentAccelWrCalibInUse(){
		mCurrentCalibDetailsAccelWr = getCurrentCalibDetailsAccelWr();
	}

	
	public void setLSM303AccelRange(int valueToSet){
		if(ArrayUtils.contains(ListofLSM303AccelRangeConfigValues, valueToSet)){
			mAccelRange = valueToSet;
			updateCurrentAccelWrCalibInUse();
		}
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
	
	public boolean isHighResAccelWr(){
		return isLSM303DigitalAccelHRM();
	}
	

	//TODO Returning same variable as isHighResAccelWr() -> remove one method?
	public boolean isLSM303DigitalAccelHRM() {
		return mHighResAccelWR;
	}
	

	//TODO Returning same variable as isLowPowerAccelWr() -> remove one method?
	public boolean isLSM303DigitalAccelLPM() {
		return mLowPowerAccelWR;
	}

	public int getLowPowerAccelEnabled(){
		return (isLSM303DigitalAccelLPM()? 1:0);
	}

	public boolean isLowPowerAccelWr(){
		return isLSM303DigitalAccelLPM();
	}
	
	//TODO Returning same variable as isLowPowerAccelWr() -> remove one method?
	public boolean isLowPowerAccelEnabled() {
		return isLSM303DigitalAccelLPM();
	}

	public int getLSM303DigitalAccelRate() {
		return mLSM303DigitalAccelRate;
	}
	
	//--------- Sensor specific methods end --------------


}
