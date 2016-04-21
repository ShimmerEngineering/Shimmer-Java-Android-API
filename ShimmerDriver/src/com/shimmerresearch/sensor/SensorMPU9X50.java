package com.shimmerresearch.sensor;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
	
public int mMPU9X50DMP = 0;
public int mMPU9X50LPF = 0;
public int mMPU9X50MotCalCfg = 0;
public int mMPU9X50MPLSamplingRate = 0;
public int mMPU9X50MagSamplingRate = 0;	

public static final int ANY_VERSION = -1;

//These can be used to enable/disable GUI options depending on what HW, FW, Expansion boards versions are present
private static final ShimmerVerObject mpu9x50 =new ShimmerVerObject(HW_ID.SHIMMER_4,ANY_VERSION,ANY_VERSION,ANY_VERSION,ANY_VERSION,ANY_VERSION);	
private static final List<ShimmerVerObject> listOfCompatibleVersionMPU9X50 = Arrays.asList(mpu9x50);

	public SensorMPU9X50(ShimmerVerObject svo){
		super(svo);
		
		mSensorName = SENSORS.MPU9X50.toString();
		mGuiFriendlyLabel = Shimmer3.GuiLabelSensors.ACCEL_MPU;
		// Check if needed and settings
	    mIntExpBoardPowerRequired = true;   
		//mSensorBitmapIDStreaming = 0x04<<(0*8);
		//mSensorBitmapIDSDLogHeader =  0x04<<(0*8);    
		
		List<Integer> mListOfMplChannels = Arrays.asList(
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
		
		mListOfConfigOptionKeysAssociated = Arrays.asList(
				Configuration.Shimmer3.GuiLabelConfig.MPU9150_ACCEL_RANGE);
		
		if(svo.mHardwareVersion==HW_ID.SHIMMER_3){
			mSensorGroupingMap.put(Configuration.Shimmer3.GuiLabelSensorTiles.MPU_ACCEL_GYRO_MAG, new SensorGroupingDetails(
					Arrays.asList(Configuration.Shimmer3.SensorMapKey.MPU9150_ACCEL)));
			mSensorGroupingMap.get(Configuration.Shimmer3.GuiLabelSensorTiles.MPU_ACCEL_GYRO_MAG).mListOfCompatibleVersionInfo = listOfCompatibleVersionMPU9X50;
		}
		
		
	}

	@Override
	public String getSensorName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getSettings(String componentName, COMMUNICATION_TYPE commType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ActionSetting setSettings(String componentName, Object valueToSet,
			COMMUNICATION_TYPE commType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ObjectCluster processData(byte[] rawData,
			COMMUNICATION_TYPE commType, ObjectCluster object) {
		// TODO Auto-generated method stub
		return null;
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
	
	
	
	
	
	
}
