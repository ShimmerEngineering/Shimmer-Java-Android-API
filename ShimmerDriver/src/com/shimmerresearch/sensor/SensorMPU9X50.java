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

	public int mMPU9X50GyroAccelRate=0;
	public int mMPU9X50AccelRange=0;   // This stores the current MPU9150 Accel Range. 0 = 2g, 1 = 4g, 2 = 8g, 4 = 16g
	public int mMagXRange=1;			   // mMagRange changed to mMagXRange
	public int mGyroXRange=1;			   // mGyroRange changed to mGyroXRange		
	public int mMPU9X50DMP = 0;
	public int mMPU9X50LPF = 0;
	public int mMPU9X50MotCalCfg = 0;
	public int mMPU9X50MPLSamplingRate = 0;
	public int mMPU9X50MagSamplingRate = 0;	

	public int mMPLXSensorFusion = 0;    // mMPLSensorFusion changed to mMPLXSensorFusion
	public int mMPLXGyroCalTC = 0;
	public int mMPLXVectCompCal = 0;
	public int mMPLXMagDistCal = 0;
	public int mMPLXEnable = 0;
	
	public boolean mDefaultCalibrationParametersGyro = true;
	public double[][] mAlignmentMatrixGyroscope = {{0,-1,0},{-1,0,0},{0,0,-1}}; 				
	public double[][] mSensitivityMatrixGyroscope = {{2.73,0,0},{0,2.73,0},{0,0,2.73}}; 		
	public double[][] mOffsetVectorGyroscope = {{1843},{1843},{1843}};
	
	//Default values Shimmer2
		public static final double[][] AlignmentMatrixGyroShimmer2 = {{0,-1,0},{-1,0,0},{0,0,-1}}; 				
		public static final double[][] SensitivityMatrixGyroShimmer2 = {{2.73,0,0},{0,2.73,0},{0,0,2.73}}; 		
		public static final double[][] OffsetVectorGyroShimmer2 = {{1843},{1843},{1843}};
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

		//Default values Shimmer2 and Shimmer3
		public static final double[][] AlignmentMatrixMagShimmer2 = {{1,0,0},{0,1,0},{0,0,-1}};
		public static final double[][] SensitivityMatrixMagShimmer2 = {{580,0,0},{0,580,0},{0,0,580}}; 		
		public static final double[][] OffsetVectorMagShimmer2 = {{0},{0},{0}};				
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
		
	
	public static final int ANY_VERSION = -1;
	
	//These can be used to enable/disable GUI options depending on what HW, FW, Expansion boards versions are present
	private static final ShimmerVerObject mpu9x50 =new ShimmerVerObject(HW_ID.SHIMMER_4,ANY_VERSION,ANY_VERSION,ANY_VERSION,ANY_VERSION,ANY_VERSION);	
	private static final List<ShimmerVerObject> listOfCompatibleVersionMPU9X50 = Arrays.asList(mpu9x50);
	
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
		
		
		if(svo.mHardwareVersion==HW_ID.SHIMMER_3){
			mSensorGroupingMap.put(Configuration.Shimmer3.GuiLabelSensorTiles.MPU_ACCEL_GYRO_MAG, new SensorGroupingDetails(
					Arrays.asList(Configuration.Shimmer3.SensorMapKey.MPU9150_MPL_ACCEL,
							Configuration.Shimmer3.SensorMapKey.MPU9150_MPL_GYRO,
							Configuration.Shimmer3.SensorMapKey.MPU9150_MPL_MAG)));
			mSensorGroupingMap.get(Configuration.Shimmer3.GuiLabelSensorTiles.MPU_ACCEL_GYRO_MAG).mListOfCompatibleVersionInfo = listOfCompatibleVersionMPU9X50;
			
//			mSensorGroupingMap.put(Configuration.Shimmer3.GuiLabelSensorTiles.MPU_OTHER, new SensorGroupingDetails(
//					Arrays.asList(Configuration.Shimmer3.SensorMapKey.MPU9150_TEMP,
//							Configuration.Shimmer3.SensorMapKey.MPU9150_MPL_QUAT_6DOF)));
//			mSensorGroupingMap.get(Configuration.Shimmer3.GuiLabelSensorTiles.MPU_ACCEL_GYRO_MAG).mListOfCompatibleVersionInfo = listOfCompatibleVersionMPU9X50;
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
				if (commType == COMMUNICATION_TYPE.BLUETOOTH){
					
				} else if (commType == COMMUNICATION_TYPE.DOCK){
					
				} else if (commType == COMMUNICATION_TYPE.CLASS){
					
				}
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
		
		if (channelDetails.mObjectClusterName.equals(Configuration.Shimmer3.ObjectClusterSensorName.GSR)){
//			ObjectCluster objectCluster = (ObjectCluster) object;
			double rawXData = ((FormatCluster)ObjectCluster.returnFormatCluster(object.mPropertyCluster.get(channelDetails.mObjectClusterName), ChannelDetails.channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
			  
		}
		return object;
	}

	@Override
	public void infoMemByteArrayGenerate(ShimmerDevice shimmerDevice,
			byte[] mInfoMemBytes) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void infoMemByteArrayParse(ShimmerDevice shimmerDevice,
			byte[] mInfoMemBytes) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Map<String, SensorGroupingDetails> getSensorGroupingMap() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object setConfigValueUsingConfigLabel(String componentName,
			Object valueToSet) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getConfigValueUsingConfigLabel(String componentName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HashMap<COMMUNICATION_TYPE, LinkedHashMap<Integer, ChannelDetails>> generateChannelDetailsMap(
			ShimmerVerObject svo) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HashMap<String, SensorConfigOptionDetails> generateConfigOptionsMap(
			ShimmerVerObject svo) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void generateListOfConfigOptionKeysAssociated() {
		mListOfConfigOptionKeysAssociated = Arrays.asList(
				Configuration.Shimmer3.GuiLabelConfig.MPU9150_ACCEL_RANGE);
	}
	
	
	
	
	
	
}
