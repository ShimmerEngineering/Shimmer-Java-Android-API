package com.shimmerresearch.sensor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import com.shimmerresearch.driver.Configuration.CHANNEL_UNITS;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driver.Configuration.Shimmer3;
import com.shimmerresearch.driver.Configuration.Shimmer3.CompatibilityInfoForMaps;
import com.shimmerresearch.driver.Configuration.Shimmer3.DatabaseChannelHandles;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driverUtilities.ChannelDetails;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_SOURCE;
import com.shimmerresearch.driverUtilities.SensorConfigOptionDetails;
import com.shimmerresearch.driverUtilities.SensorDetails;
import com.shimmerresearch.driverUtilities.SensorEnabledDetails;
import com.shimmerresearch.driverUtilities.SensorGroupingDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_TYPE;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_ENDIAN;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_TYPE;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.HW_ID;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.FormatCluster;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driver.ShimmerObject;
import com.shimmerresearch.sensor.AbstractSensor.SENSORS;

public class SensorMPU9X50 extends AbstractSensor implements Serializable {

	/** * */
	private static final long serialVersionUID = -1137540822708521997L;
	
	/** This stores the current Gyro Range, it is a value between 0 and 3; 0 = +/- 250dps,1 = 500dps, 2 = 1000dps, 3 = 2000dps */
	protected int mGyroRange=1;													 

	// ----------- MPU9X50 options start -------------------------
//	protected int mMPU9150GyroRate = 0;

	protected int mMPU9150AccelRange=0;											// This stores the current MPU9150 Accel Range. 0 = 2g, 1 = 4g, 2 = 8g, 4 = 16g
	protected int mMPU9150GyroAccelRate=0;

	protected int mMPU9150DMP = 0;
	protected int mMPU9150LPF = 0;
	protected int mMPU9150MotCalCfg = 0;
	protected int mMPU9150MPLSamplingRate = 0;
	protected int mMPU9150MagSamplingRate = 0;
	protected int mMPLSensorFusion = 0;
	protected int mMPLGyroCalTC = 0;
	protected int mMPLVectCompCal = 0;
	protected int mMPLMagDistCal = 0;
	protected int mMPLEnable = 0;

	protected double[][] AlignmentMatrixMPLAccel = {{-1,0,0},{0,1,0},{0,0,-1}}; 			
	protected double[][] SensitivityMatrixMPLAccel = {{1631,0,0},{0,1631,0},{0,0,1631}}; 	
	protected double[][] OffsetVectorMPLAccel = {{0},{0},{0}};
	
	protected double[][] AlignmentMatrixMPLMag = {{-1,0,0},{0,1,0},{0,0,-1}}; 			
	protected double[][] SensitivityMatrixMPLMag = {{1631,0,0},{0,1631,0},{0,0,1631}}; 	
	protected double[][] OffsetVectorMPLMag = {{0},{0},{0}};
	
	protected double[][] AlignmentMatrixMPLGyro = {{-1,0,0},{0,1,0},{0,0,-1}}; 			
	protected double[][] SensitivityMatrixMPLGyro = {{1631,0,0},{0,1631,0},{0,0,1631}}; 	
	protected double[][] OffsetVectorMPLGyro = {{0},{0},{0}};

	// ----------- MPU9X50 options end -------------------------	

	
	public boolean mDefaultCalibrationParametersGyro = true;
	public double[][] mAlignmentMatrixGyroscope = {{0,-1,0},{-1,0,0},{0,0,-1}}; 				
	public double[][] mSensitivityMatrixGyroscope = {{2.73,0,0},{0,2.73,0},{0,0,2.73}}; 		
	public double[][] mOffsetVectorGyroscope = {{1843},{1843},{1843}};
	
	//Shimmer3
	public static final double[][] SensitivityMatrixGyro250dpsShimmer3 = {{131,0,0},{0,131,0},{0,0,131}};
	public static final double[][] SensitivityMatrixGyro500dpsShimmer3 = {{65.5,0,0},{0,65.5,0},{0,0,65.5}};
	public static final double[][] SensitivityMatrixGyro1000dpsShimmer3 = {{32.8,0,0},{0,32.8,0},{0,0,32.8}};
	public static final double[][] SensitivityMatrixGyro2000dpsShimmer3 = {{16.4,0,0},{0,16.4,0},{0,0,16.4}};
	public static final double[][] AlignmentMatrixGyroShimmer3 = {{0,-1,0},{-1,0,0},{0,0,-1}}; 				
	public static final double[][] OffsetVectorGyroShimmer3 = {{0},{0},{0}};		

	// variable names changed from ShimmerObject
	public boolean mDefaultCalibrationParametersXMag = true;
	public double[][] mAlignmentMatrixXMagnetometer = {{1,0,0},{0,1,0},{0,0,-1}}; 				
	public double[][] mSensitivityMatrixXMagnetometer = {{580,0,0},{0,580,0},{0,0,580}}; 		
	public double[][] mOffsetVectorXMagnetometer = {{0},{0},{0}};								

	//Shimmer3
	public static final double[][] AlignmentMatrixMagShimmer3 = {{-1,0,0},{0,1,0},{0,0,-1}}; 				
	public static final double[][] SensitivityMatrixMagShimmer3 = {{1100,0,0},{0,1100,0},{0,0,980}}; 		
	public static final double[][] OffsetVectorMagShimmer3 = {{0},{0},{0}};		

	
	public double[][] AlignmentMatrixMPLXAccel = {{-1,0,0},{0,1,0},{0,0,-1}}; 			
	public double[][] SensitivityMatrixMPLXAccel = {{1631,0,0},{0,1631,0},{0,0,1631}}; 	
	public double[][] OffsetVectorMPLXAccel = {{0},{0},{0}};
	
	public double[][] AlignmentMatrixMPLXMag = {{-1,0,0},{0,1,0},{0,0,-1}}; 			
	public double[][] SensitivityMatrixMPLXMag = {{1631,0,0},{0,1631,0},{0,0,1631}}; 	
	protected double[][] OffsetVectorMPLXMag = {{0},{0},{0}};
	
	public double[][] AlignmentMatrixMPLXGyro = {{-1,0,0},{0,1,0},{0,0,-1}}; 			
	public double[][] SensitivityMatrixMPLXGyro = {{1631,0,0},{0,1631,0},{0,0,1631}}; 	
	public double[][] OffsetVectorMPLXGyro = {{0},{0},{0}};
	
	public boolean mLowPowerXMag = false;
	public boolean mLowPowerAccelXWR = false;
	public boolean mLowPowerXGyro = false;
	
	public boolean mEnableOntheFlyGyroOVCal = false;

	public double mGyroXOVCalThreshold = 1.2;
	DescriptiveStatistics mGyroXX;
	DescriptiveStatistics mGyroXY;
	DescriptiveStatistics mGyroXZ;
	DescriptiveStatistics mGyroXRaw;
	DescriptiveStatistics mGyroXYRaw;
	DescriptiveStatistics mGyroXZRaw;
	public boolean mEnableXCalibration = true;
	public byte[] mInquiryResponseXBytes;
	
	public byte[] mGyroCalRawXParams  = new byte[22];
	public byte[] mMagCalRawXParams  = new byte[22];
		
	
	//These can be used to enable/disable GUI options depending on what HW, FW, Expansion boards versions are present
//	private static final ShimmerVerObject MPU9150 =new ShimmerVerObject(
//			HW_ID.SHIMMER_4,
//			ShimmerVerDetails.ANY_VERSION,
//			ShimmerVerDetails.ANY_VERSION,
//			ShimmerVerDetails.ANY_VERSION,
//			ShimmerVerDetails.ANY_VERSION,
//			ShimmerVerDetails.ANY_VERSION);
//	
//	private static final List<ShimmerVerObject> listOfCompatibleVersionMPU9150 = Arrays.asList(MPU9150);
	
	public static final List<Integer> mListOfMplChannels = Arrays.asList(
			Configuration.Shimmer3.SensorMapKey.MPU9150_TEMP,
			Configuration.Shimmer3.SensorMapKey.MPU9150_MPL_QUAT_6DOF,
			Configuration.Shimmer3.SensorMapKey.MPU9150_MPL_QUAT_9DOF,
			Configuration.Shimmer3.SensorMapKey.MPU9150_MPL_EULER_6DOF,
			Configuration.Shimmer3.SensorMapKey.MPU9150_MPL_EULER_9DOF,
			Configuration.Shimmer3.SensorMapKey.MPU9150_MPL_HEADING,
			Configuration.Shimmer3.SensorMapKey.MPU9150_MPL_PEDOMETER,
			Configuration.Shimmer3.SensorMapKey.MPU9150_MPL_TAP,
			Configuration.Shimmer3.SensorMapKey.MPU9150_MPL_MOTION_ORIENT,
			Configuration.Shimmer3.SensorMapKey.MPU9150_MPL_GYRO,
			Configuration.Shimmer3.SensorMapKey.MPU9150_MPL_ACCEL,
			Configuration.Shimmer3.SensorMapKey.MPU9150_MPL_MAG,
			Configuration.Shimmer3.SensorMapKey.MPU9150_MPL_QUAT_6DOF_RAW);

	public SensorMPU9X50(ShimmerVerObject svo){
		super(svo);
		
		mSensorName = SENSORS.MPU9X50.toString();
		mGuiFriendlyLabel = Shimmer3.GuiLabelSensors.ACCEL_MPU;
		
	    mIntExpBoardPowerRequired = false;   
	    // Check if needed and settings
		//mSensorBitmapIDStreaming = 0x04<<(0*8);
		//mSensorBitmapIDSDLogHeader =  0x04<<(0*8);    
		
		
//		aMap.get(Configuration.Shimmer3.SensorMapKey.MPU9150_MPL_ACCEL).mListOfConfigOptionKeysAssociated = Arrays.asList(
//				Configuration.Shimmer3.GuiLabelConfig.MPU9150_ACCEL_RANGE,
//				Configuration.Shimmer3.GuiLabelConfig.MPU9150_MPL_LPF);
//		aMap.get(Configuration.Shimmer3.SensorMapKey.MPU9150_MPL_GYRO).mListOfConfigOptionKeysAssociated = Arrays.asList(
//				Configuration.Shimmer3.GuiLabelConfig.MPU9150_GYRO_RANGE,
//				Configuration.Shimmer3.GuiLabelConfig.MPU9150_MPL_LPF,
//				Configuration.Shimmer3.GuiLabelConfig.MPU9150_GYRO_RATE,
//				Configuration.Shimmer3.GuiLabelConfig.MPU9150_MPL_GYRO_CAL);
//		aMap.get(Configuration.Shimmer3.SensorMapKey.MPU9150_MPL_MAG).mListOfConfigOptionKeysAssociated = Arrays.asList(
//				Configuration.Shimmer3.GuiLabelConfig.MPU9150_MPL_LPF);

		
		if(svo.mHardwareVersion==HW_ID.SHIMMER_3 || svo.mHardwareVersion==HW_ID.SHIMMER_4){
			mSensorGroupingMap.put(Configuration.Shimmer3.GuiLabelSensorTiles.MPU_ACCEL_GYRO_MAG, new SensorGroupingDetails(
					Arrays.asList(Configuration.Shimmer3.SensorMapKey.MPU9150_MPL_ACCEL,
							Configuration.Shimmer3.SensorMapKey.MPU9150_MPL_GYRO,
							Configuration.Shimmer3.SensorMapKey.MPU9150_MPL_MAG)));
			mSensorGroupingMap.get(Configuration.Shimmer3.GuiLabelSensorTiles.MPU_ACCEL_GYRO_MAG).mListOfCompatibleVersionInfo = CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors;
			
			mSensorGroupingMap.put(Configuration.Shimmer3.GuiLabelSensorTiles.MPU_OTHER, new SensorGroupingDetails(
					Arrays.asList(Configuration.Shimmer3.SensorMapKey.MPU9150_TEMP,
								Configuration.Shimmer3.SensorMapKey.MPU9150_MPL_QUAT_6DOF)));
			mSensorGroupingMap.get(Configuration.Shimmer3.GuiLabelSensorTiles.MPU_ACCEL_GYRO_MAG).mListOfCompatibleVersionInfo = CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors;
		}
		
		
	}

	@Override
	public String getSensorName() {
		return mSensorName;
	}

	@Override
	public Object getSettings(String componentName, COMMUNICATION_TYPE commType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ActionSetting setSettings(String componentName, Object valueToSet, COMMUNICATION_TYPE commType) {
		
		ActionSetting actionSetting = new ActionSetting(commType);
		switch(componentName){
			case(Configuration.Shimmer3.GuiLabelConfig.MPU9150_ACCEL_RANGE):
//				if (commType == COMMUNICATION_TYPE.BLUETOOTH){
//					
//				} else if (commType == COMMUNICATION_TYPE.DOCK){
//					
//				} else if (commType == COMMUNICATION_TYPE.CLASS){
//					
//				}
			break;
		}
		return actionSetting;
	}

	@Override
	public ObjectCluster processData(byte[] rawData, COMMUNICATION_TYPE commType, ObjectCluster object) {
		
		int index = 0;
		for (ChannelDetails channelDetails:mMapOfCommTypetoChannel.get(commType).values()){
			//first process the data originating from the Shimmer sensor
			byte[] channelByteArray = new byte[channelDetails.mDefaultNumBytes];
			System.arraycopy(rawData, index, channelByteArray, 0, channelDetails.mDefaultNumBytes);
			object = processShimmerChannelData(rawData, channelDetails, object);
		}
		
//		if (channelDetails.mObjectClusterName.equals(Configuration.Shimmer3.ObjectClusterSensorName.GSR)){
////			ObjectCluster objectCluster = (ObjectCluster) object;
//			double rawXData = ((FormatCluster)ObjectCluster.returnFormatCluster(object.mPropertyCluster.get(channelDetails.mObjectClusterName), ChannelDetails.channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
//			  
//		}
		return object;
	}

	@Override
	public void infoMemByteArrayGenerate(ShimmerDevice shimmerDevice, byte[] mInfoMemBytes) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void infoMemByteArrayParse(ShimmerDevice shimmerDevice, byte[] mInfoMemBytes) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Map<String, SensorGroupingDetails> getSensorGroupingMap() {
		super.updateSensorGroupingMap();
		return mSensorGroupingMap;
	}

	@Override
	public Object setConfigValueUsingConfigLabel(String componentName, Object valueToSet) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getConfigValueUsingConfigLabel(String componentName) {
		Object returnValue = null;
		switch(componentName){
			case(Configuration.Shimmer3.GuiLabelConfig.MPU9150_DMP):
				returnValue = isMPU9150DMP();
	        	break;
			case(Configuration.Shimmer3.GuiLabelConfig.MPU9150_MPL):
				returnValue = isMPLEnable();
	        	break;
			case(Configuration.Shimmer3.GuiLabelConfig.MPU9150_MPL_9DOF_SENSOR_FUSION):
				returnValue = isMPLSensorFusion();
	        	break;
			case(Configuration.Shimmer3.GuiLabelConfig.MPU9150_MPL_GYRO_CAL):
				returnValue = isMPLGyroCalTC();
	        	break;
			case(Configuration.Shimmer3.GuiLabelConfig.MPU9150_MPL_VECTOR_CAL):
				returnValue = isMPLVectCompCal();
	        	break;
			case(Configuration.Shimmer3.GuiLabelConfig.MPU9150_MPL_MAG_CAL):
				returnValue = isMPLMagDistCal();
	        	break;
		
			case(Configuration.Shimmer3.GuiLabelConfig.MPU9150_ACCEL_RANGE):
				returnValue = getMPU9150AccelRange();
		    	break;
			case(Configuration.Shimmer3.GuiLabelConfig.MPU9150_DMP_GYRO_CAL):
				returnValue = getMPU9150MotCalCfg();
		    	break;
			case(Configuration.Shimmer3.GuiLabelConfig.MPU9150_MPL_LPF):
				returnValue = getMPU9150LPF();
		    	break;
			case(Configuration.Shimmer3.GuiLabelConfig.MPU9150_MPL_RATE):
				returnValue = getMPU9150MPLSamplingRate();
				break;
			case(Configuration.Shimmer3.GuiLabelConfig.MPU9150_MAG_RATE):
				returnValue = getMPU9150MagSamplingRate();
		    	break;
		    	
			case(Configuration.Shimmer3.GuiLabelConfig.MPU9150_GYRO_RATE):
				returnValue = Double.toString((double)Math.round(getMPU9150GyroAccelRateInHz() * 100) / 100); // round sampling rate to two decimal places
//    		    		System.out.println("Gyro Sampling rate: " + getMPU9150GyroAccelRateInHz() + " " + returnValue);
	        	break;
		    default:
	        	break;
		}

		return returnValue;
	}

	@Override
	public HashMap<COMMUNICATION_TYPE, LinkedHashMap<Integer, ChannelDetails>> generateChannelDetailsMap(ShimmerVerObject svo) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HashMap<String, SensorConfigOptionDetails> generateConfigOptionsMap(ShimmerVerObject svo) {
		mConfigOptionsMap.clear();
		
//		if (svo.mFirmwareIdentifier == ShimmerVerDetails.FW_ID.BTSTREAM 
//				|| svo.mFirmwareIdentifier == ShimmerVerDetails.FW_ID.SDLOG
//				|| svo.mFirmwareIdentifier == ShimmerVerDetails.FW_ID.GQ_802154) {
			
			mConfigOptionsMap.put(Configuration.Shimmer3.GuiLabelConfig.GSR_RANGE, 
					new SensorConfigOptionDetails(Configuration.Shimmer3.ListofGSRRange, 
											Configuration.Shimmer3.ListofGSRRangeConfigValues, 
											SensorConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX,
											CompatibilityInfoForMaps.listOfCompatibleVersionInfoGsr));
			
			
			mConfigOptionsMap.put(Configuration.Shimmer3.GuiLabelConfig.MPU9150_GYRO_RANGE, 
					new SensorConfigOptionDetails(Configuration.Shimmer3.ListofGyroRange, 
											Configuration.Shimmer3.ListofMPU9150GyroRangeConfigValues, 
											SensorConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX,
											CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW));
			//MPL Options
			mConfigOptionsMap.put(Configuration.Shimmer3.GuiLabelConfig.MPU9150_ACCEL_RANGE, 
					new SensorConfigOptionDetails(Configuration.Shimmer3.ListofMPU9150AccelRange, 
											Configuration.Shimmer3.ListofMPU9150AccelRangeConfigValues, 
											SensorConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX,
											CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors));
			mConfigOptionsMap.put(Configuration.Shimmer3.GuiLabelConfig.MPU9150_DMP_GYRO_CAL, 
					new SensorConfigOptionDetails(Configuration.Shimmer3.ListofMPU9150MplCalibrationOptions, 
											Configuration.Shimmer3.ListofMPU9150MplCalibrationOptionsConfigValues, 
											SensorConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX,
											CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors));
			mConfigOptionsMap.put(Configuration.Shimmer3.GuiLabelConfig.MPU9150_MPL_LPF, 
					new SensorConfigOptionDetails(Configuration.Shimmer3.ListofMPU9150MplLpfOptions, 
											Configuration.Shimmer3.ListofMPU9150MplLpfOptionsConfigValues, 
											SensorConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX,
											CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors));
			mConfigOptionsMap.put(Configuration.Shimmer3.GuiLabelConfig.MPU9150_MPL_RATE, 
					new SensorConfigOptionDetails(Configuration.Shimmer3.ListofMPU9150MplRate, 
											Configuration.Shimmer3.ListofMPU9150MplRateConfigValues, 
											SensorConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX,
											CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors));
			mConfigOptionsMap.put(Configuration.Shimmer3.GuiLabelConfig.MPU9150_MAG_RATE, 
					new SensorConfigOptionDetails(Configuration.Shimmer3.ListofMPU9150MagRate, 
											Configuration.Shimmer3.ListofMPU9150MagRateConfigValues, 
											SensorConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX,
											CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors));
			
			//MPL CheckBoxes
			mConfigOptionsMap.put(Configuration.Shimmer3.GuiLabelConfig.MPU9150_DMP, 
					new SensorConfigOptionDetails(SensorConfigOptionDetails.GUI_COMPONENT_TYPE.CHECKBOX,CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors));
			mConfigOptionsMap.put(Configuration.Shimmer3.GuiLabelConfig.MPU9150_MPL, 
					new SensorConfigOptionDetails(SensorConfigOptionDetails.GUI_COMPONENT_TYPE.CHECKBOX,CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors));
			mConfigOptionsMap.put(Configuration.Shimmer3.GuiLabelConfig.MPU9150_MPL_9DOF_SENSOR_FUSION, 
					new SensorConfigOptionDetails(SensorConfigOptionDetails.GUI_COMPONENT_TYPE.CHECKBOX,CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors));
			mConfigOptionsMap.put(Configuration.Shimmer3.GuiLabelConfig.MPU9150_MPL_GYRO_CAL, 
					new SensorConfigOptionDetails(SensorConfigOptionDetails.GUI_COMPONENT_TYPE.CHECKBOX,CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors));
			mConfigOptionsMap.put(Configuration.Shimmer3.GuiLabelConfig.MPU9150_MPL_VECTOR_CAL, 
					new SensorConfigOptionDetails(SensorConfigOptionDetails.GUI_COMPONENT_TYPE.CHECKBOX,CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors));
			mConfigOptionsMap.put(Configuration.Shimmer3.GuiLabelConfig.MPU9150_MPL_MAG_CAL, 
					new SensorConfigOptionDetails(SensorConfigOptionDetails.GUI_COMPONENT_TYPE.CHECKBOX,CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors));
			
			//General Config
			mConfigOptionsMap.put(Configuration.Shimmer3.GuiLabelConfig.MPU9150_GYRO_RATE, 
					new SensorConfigOptionDetails(SensorConfigOptionDetails.GUI_COMPONENT_TYPE.TEXTFIELD,CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW));
			mConfigOptionsMap.put(Configuration.Shimmer3.GuiLabelConfig.MPU9150_GYRO_LPM, 
					new SensorConfigOptionDetails(SensorConfigOptionDetails.GUI_COMPONENT_TYPE.CHECKBOX,CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW));


//		}
				
		return mConfigOptionsMap;
	}

	@Override
	public void generateListOfConfigOptionKeysAssociated() {
		mListOfConfigOptionKeysAssociated = Arrays.asList(
				Configuration.Shimmer3.GuiLabelConfig.MPU9150_ACCEL_RANGE);
	}

	@Override
	public void generateListOfSensorMapKeysConflicting() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void generateSensorGroupMapping(ShimmerVerObject svo) {
		// TODO Auto-generated method stub
		
	}
	
	
	
	
	// ----------- MPU9X50 options start -------------------------

//	/**
//	 * Computes next higher available sensor sampling rate setting based on
//	 * passed in "freq" variable and dependent on whether low-power mode is set.
//	 * 
//	 * @param freq
//	 * @return int the rate configuration setting for the respective sensor
//	 */
//	public int setMPU9150GyroAccelRateFromFreq(double freq) {
//		boolean setFreq = false;
//		// Check if channel is enabled 
//		if(checkIfAnyMplChannelEnabled()){
//			setFreq = true;
//		}
//		else if(checkIfAMpuGyroOrAccelEnabled()){
//			setFreq = true;
//		}
//		
//		if(setFreq){
//			// Gyroscope Output Rate = 8kHz when the DLPF (Digital Low-pass filter) is disabled (DLPF_CFG = 0 or 7), and 1kHz when the DLPF is enabled
//			double numerator = 1000;
//			if(mMPU9150LPF == 0) {
//				numerator = 8000;
//			}
//	
//			if (!mLowPowerGyro){
//				if(freq<4) {
//					freq = 4;
//				}
//				else if(freq>numerator) {
//					freq = numerator;
//				}
//				int result = (int) Math.floor(((numerator / freq) - 1));
//				if(result>255) {
//					result = 255;
//				}
//				mMPU9150GyroAccelRate = result;
//	
//			}
//			else {
//				mMPU9150GyroAccelRate = 0xFF; // Dec. = 255, Freq. = 31.25Hz (or 3.92Hz when LPF enabled)
//			}
//		}
//		else {
//			mMPU9150GyroAccelRate = 0xFF; // Dec. = 255, Freq. = 31.25Hz (or 3.92Hz when LPF enabled)
//		}
//		return mMPU9150GyroAccelRate;
//	}
//	
//	/**
//	 * Computes next higher available sensor sampling rate setting based on
//	 * passed in "freq" variable and dependent on whether low-power mode is set.
//	 * 
//	 * @param freq
//	 * @return int the rate configuration setting for the respective sensor
//	 */
//	private int setMPU9150MagRateFromFreq(double freq) {
//		boolean setFreq = false;
//		// Check if channel is enabled 
//		if(checkIfAnyMplChannelEnabled()){
//			setFreq = true;
//		}
//		else if (isSensorEnabled(Configuration.Shimmer3.SensorMapKey.MPU9150_MAG)) {
//			setFreq = true;
//		}
//		
//		if(setFreq){
//			if (freq<=10){
//				mMPU9150MagSamplingRate = 0; // 10Hz
//			} else if (freq<=20){
//				mMPU9150MagSamplingRate = 1; // 20Hz
//			} else if (freq<=40) {
//				mMPU9150MagSamplingRate = 2; // 40Hz
//			} else if (freq<=50) {
//				mMPU9150MagSamplingRate = 3; // 50Hz
//			} else {
//				mMPU9150MagSamplingRate = 4; // 100Hz
//			}
//		}
//		else {
//			mMPU9150MagSamplingRate = 0; // 10 Hz
//		}
//		return mMPU9150MagSamplingRate;
//	}
//	
//	/**
//	 * Computes next higher available sensor sampling rate setting based on
//	 * passed in "freq" variable and dependent on whether low-power mode is set.
//	 * 
//	 * @param freq
//	 * @return int the rate configuration setting for the respective sensor
//	 */
//	private int setMPU9150MplRateFromFreq(double freq) {
//		// Check if channel is enabled 
//		if(!checkIfAnyMplChannelEnabled()){
//			mMPU9150MPLSamplingRate = 0; // 10 Hz
//			return mMPU9150MPLSamplingRate;
//		}
//		
//		if (freq<=10){
//			mMPU9150MPLSamplingRate = 0; // 10Hz
//		} else if (freq<=20){
//			mMPU9150MPLSamplingRate = 1; // 20Hz
//		} else if (freq<=40) {
//			mMPU9150MPLSamplingRate = 2; // 40Hz
//		} else if (freq<=50) {
//			mMPU9150MPLSamplingRate = 3; // 50Hz
//		} else {
//			mMPU9150MPLSamplingRate = 4; // 100Hz
//		}
//		return mMPU9150MPLSamplingRate;
//	}
//	
//	private void setDefaultMpu9150GyroSensorConfig(boolean state) {
//		if(!checkIfAnyMplChannelEnabled()) {
//			if(!isSensorEnabled(Configuration.Shimmer3.SensorMapKey.MPU9150_ACCEL)) {
//				if(state) {
//					setLowPowerGyro(false);
//				}
//				else {
//					setLowPowerGyro(true);
//				}
//			}
//			
//			mGyroRange=1;
////			if(!state){
////				mGyroRange=1; // 500dps
////			}
//		}
//		else {
//			mGyroRange=3; // 2000dps
//		}
//	}
//	
//	private void setDefaultMpu9150AccelSensorConfig(boolean state) {
//		if(!checkIfAnyMplChannelEnabled()) {
//			if(!isSensorEnabled(Configuration.Shimmer3.SensorMapKey.MPU9150_GYRO)) {
//				if(state) {
//					setLowPowerGyro(false);
//				}
//				else {
//					setLowPowerGyro(true);
//				}
//			}
//			
//			if(!state){
//				mMPU9150AccelRange = 0; //=2g
//			}
//		}
//		else {
//			mMPU9150AccelRange = 0; //=2g
//		}
//	}
//	
//	private void setDefaultMpu9150MplSensorConfig(boolean state) {
//		if(state){
//			mMPU9150DMP = 1;
//			mMPLEnable = 1;
//			mMPU9150LPF = 1; // 188Hz
//			mMPU9150MotCalCfg = 1; // Fast Calibration
//			mMPLGyroCalTC = 1;
//			mMPLVectCompCal = 1;
//			mMPLMagDistCal = 1;
//			mMPLSensorFusion = 0;
//			
////			//Gyro rate can not be set to 250dps when DMP is on
////			if(mGyroRange==0){
////				mGyroRange=1;
////			}
//			
//			//force gyro range to be 2000dps and accel range to be +-2g - others untested
//			mGyroRange=3; // 2000dps
//			mMPU9150AccelRange= 0; // 2g
//			
//			setLowPowerGyro(false);
//			setMPU9150MagRateFromFreq(getSamplingRateShimmer());
//			setMPU9150MplRateFromFreq(getSamplingRateShimmer());
//		}
//		else {
//			mMPU9150DMP = 0;
//			mMPLEnable = 0;
//			mMPU9150LPF = 0;
//			mMPU9150MotCalCfg = 0;
//			mMPLGyroCalTC = 0;
//			mMPLVectCompCal = 0;
//			mMPLMagDistCal = 0;
//			mMPLSensorFusion = 0;
//			
//			if(checkIfAMpuGyroOrAccelEnabled()){
//				setMPU9150GyroAccelRateFromFreq(getSamplingRateShimmer());
//			}
//			else {
//				setLowPowerGyro(true);
//			}
//			
//			setMPU9150MagRateFromFreq(getSamplingRateShimmer());
//			setMPU9150MplRateFromFreq(getSamplingRateShimmer());
//		}
//	}
//	
//	private boolean checkIfAMpuGyroOrAccelEnabled(){
//		if(isSensorEnabled(Configuration.Shimmer3.SensorMapKey.MPU9150_GYRO)) {
//			return true;
//		}
//		if(isSensorEnabled(Configuration.Shimmer3.SensorMapKey.MPU9150_ACCEL)) {
//			return true;
//		}
////		if(mSensorMap.get(Configuration.Shimmer3.SensorMapKey.MPU9150_MAG) != null) {
////			if(mSensorMap.get(Configuration.Shimmer3.SensorMapKey.MPU9150_MAG).mIsEnabled) {
////				return true;
////			}
////		}
//		return false;
//	}	
//	
//	private boolean checkIfAnyOtherMplChannelEnabled(int sensorMapKey){
//		if (mShimmerVerObject.getHardwareVersion()==HW_ID.SHIMMER_3 || mShimmerVerObject.getHardwareVersion()==HW_ID.SHIMMER_GQ_BLE) {
//			if(mSensorEnabledMap.keySet().size()>0){
//				
//				for(int key:SensorMPU9X50.mListOfMplChannels){
////				for(int key:mListOfMplChannels){
//					if(key==sensorMapKey){
//						continue;
//					}
//					if(isSensorEnabled(key)) {
//						return true;
//					}
//				}
//			}
//		}
//		return false;
//	}
//			
//	protected boolean checkIfAnyMplChannelEnabled(){
//		if (mShimmerVerObject.getHardwareVersion()==HW_ID.SHIMMER_3 || mShimmerVerObject.getHardwareVersion()==HW_ID.SHIMMER_GQ_BLE) {
//			if(mSensorEnabledMap.keySet().size()>0){
//				
//				for(int key:SensorMPU9X50.mListOfMplChannels){
////					for(int key:mListOfMplChannels){
//					if(isSensorEnabled(key)) {
//						return true;
//					}
//				}
//			}
//		}
//		return false;
//	}
	
	/**
	 * @return the mMPU9150AccelRange
	 */
	public int getMPU9150AccelRange() {
		return mMPU9150AccelRange;
	}

	/**
	 * @return the mMPU9150GyroAccelRate
	 */
	public int getMPU9150GyroAccelRate() {
		return mMPU9150GyroAccelRate;
	}

	/**
	 * @return the mMPU9150MotCalCfg
	 */
	public int getMPU9150MotCalCfg() {
		return mMPU9150MotCalCfg;
	}
	/**
	 * @return the mMPU9150LPF
	 */
	public int getMPU9150LPF() {
		return mMPU9150LPF;
	}
	
	public int getMPU9150DMP() {
		return mMPU9150DMP;
	}

	/**
	 * @return the mMPU9150MPLSamplingRate
	 */
	public int getMPU9150MPLSamplingRate() {
		return mMPU9150MPLSamplingRate;
	}

	/**
	 * @return the mMPU9150MagSamplingRate
	 */
	public int getMPU9150MagSamplingRate() {
		return mMPU9150MagSamplingRate;
	}
	
	/**
	 * @return the mMPU9150GyroAccelRate in Hz
	 */
	public double getMPU9150GyroAccelRateInHz() {
		// Gyroscope Output Rate = 8kHz when the DLPF is disabled (DLPF_CFG = 0 or 7), and 1kHz when the DLPF is enabled
		double numerator = 1000.0;
		if(mMPU9150LPF == 0) {
			numerator = 8000.0;
		}
		
		if(mMPU9150GyroAccelRate == 0) {
			return numerator;
		}
		else {
			return (numerator / mMPU9150GyroAccelRate);
		}
	}
	
//	/**
//	 * @param mMPU9150AccelRange the mMPU9150AccelRange to set
//	 */
//	protected void setMPU9150AccelRange(int i) {
//		if(checkIfAnyMplChannelEnabled()){
//			i=0; // 2g
//		}
//		
//		mMPU9150AccelRange = i;
//	}
//	
//	protected void setMPU9150GyroRange(int i){
////		//Gyro rate can not be set to 250dps when DMP is on
////		if((checkIfAnyMplChannelEnabled()) && (i==0)){
////			i=1;
////		}
//		
//		if(checkIfAnyMplChannelEnabled()){
//			i=3; // 2000dps
//		}
//		
//		mGyroRange = i;
//	}

	/**
	 * @param mMPU9150MPLSamplingRate the mMPU9150MPLSamplingRate to set
	 */
	protected void setMPU9150MPLSamplingRate(int mMPU9150MPLSamplingRate) {
		this.mMPU9150MPLSamplingRate = mMPU9150MPLSamplingRate;
	}

	/**
	 * @param mMPU9150MagSamplingRate the mMPU9150MagSamplingRate to set
	 */
	protected void setMPU9150MagSamplingRate(int mMPU9150MagSamplingRate) {
		this.mMPU9150MagSamplingRate = mMPU9150MagSamplingRate;
	}
	
	// MPL options
	/**
	 * @return the mMPU9150DMP
	 */
	public boolean isMPU9150DMP() {
		return (mMPU9150DMP>0)? true:false;
	}


	/**
	 * @param state the mMPU9150DMP state to set
	 */
	protected void setMPU9150DMP(boolean state) {
		if(state) 
			this.mMPU9150DMP = 0x01;
		else 
			this.mMPU9150DMP = 0x00;
	}
	
	/**
	 * @return the mMPLEnable
	 */
	public boolean isMPLEnable() {
		return (mMPLEnable>0)? true:false;
	}
	
	/**
	 * @param state the mMPLEnable state to set
	 */
	protected void setMPLEnable(boolean state) {
		if(state) 
			this.mMPLEnable = 0x01;
		else 
			this.mMPLEnable = 0x00;
	}

	/**
	 * @return the mMPLSensorFusion
	 */
	public boolean isMPLSensorFusion() {
		return (mMPLSensorFusion>0)? true:false;
	}

	/**
	 * @param state the mMPLSensorFusion state to set
	 */
	protected void setMPLSensorFusion(boolean state) {
		if(state) 
			this.mMPLSensorFusion = 0x01;
		else 
			this.mMPLSensorFusion = 0x00;
	}

	/**
	 * @return the mMPLGyroCalTC
	 */
	public boolean isMPLGyroCalTC() {
		return (mMPLGyroCalTC>0)? true:false;
	}
	
	/**
	 * @param state the mMPLGyroCalTC state to set
	 */
	protected void setMPLGyroCalTC(boolean state) {
		if(state) 
			this.mMPLGyroCalTC = 0x01;
		else 
			this.mMPLGyroCalTC = 0x00;
	}

	/**
	 * @return the mMPLVectCompCal
	 */
	public boolean isMPLVectCompCal() {
		return (mMPLVectCompCal>0)? true:false;
	}

	/**
	 * @param state the mMPLVectCompCal state to set
	 */
	protected void setMPLVectCompCal(boolean state) {
		if(state) 
			this.mMPLVectCompCal = 0x01;
		else 
			this.mMPLVectCompCal = 0x00;
	}

	/**
	 * @return the mMPLMagDistCal
	 */
	public boolean isMPLMagDistCal() {
		return (mMPLMagDistCal>0)? true:false;
	}
	
	/**
	 * @param state the mMPLMagDistCal state to set
	 */
	protected void setMPLMagDistCal(boolean state) {
		if(state) 
			this.mMPLMagDistCal = 0x01;
		else 
			this.mMPLMagDistCal = 0x00;
	}
	
	/**
	 * @return the mMPLSensorFusion
	 */
	public boolean getmMPLSensorFusion() {
		return (mMPLSensorFusion>0)? true:false;
	}

	/**
	 * @param state the mMPLSensorFusion state to set
	 */
	protected void setmMPLSensorFusion(boolean state) {
		if(state) 
			this.mMPLSensorFusion = 0x01;
		else 
			this.mMPLSensorFusion = 0x00;
	}


	/**
	 * @param mMPU9150MotCalCfg the mMPU9150MotCalCfg to set
	 */
	protected void setMPU9150MotCalCfg(int mMPU9150MotCalCfg) {
		this.mMPU9150MotCalCfg = mMPU9150MotCalCfg;
	}

	/**
	 * @param mMPU9150LPF the mMPU9150LPF to set
	 */
	protected void setMPU9150LPF(int mMPU9150LPF) {
		this.mMPU9150LPF = mMPU9150LPF;
	}

	// ----------- MPU9X50 options end -------------------------
	
	@Override
	public void setSamplingRateFromFreq() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDefaultConfiguration() {
		// TODO Auto-generated method stub
		
	}


	
}
