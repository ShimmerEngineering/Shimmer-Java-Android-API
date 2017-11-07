package com.shimmerresearch.shimmerConfig;

import com.shimmerresearch.driver.ShimmerDevice;

import java.util.LinkedHashMap;
import java.util.Map.Entry;

import com.shimmerresearch.driver.Configuration.Shimmer3;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.HW_ID_SR_CODES;
import com.shimmerresearch.sensors.SensorEXG;
import com.shimmerresearch.sensors.lsm303.SensorLSM303;

public class FixedShimmerConfigs {

	//TODO change to code names for release
    public enum FIXED_SHIMMER_CONFIG_MODE {
    	NONE,
    	CADENCE,
    	CIMIT,
    	CALIBRATION_IMU,
    	USER
    }
    
	public static boolean setFixedConfigWhenConnecting(ShimmerDevice shimmerDevice, FIXED_SHIMMER_CONFIG_MODE fixedConfig, LinkedHashMap<String, Object> fixedConfigMap) {
		boolean triggerConfiguration = false;
		
		if(fixedConfig!=FIXED_SHIMMER_CONFIG_MODE.NONE){
	
			int expId = shimmerDevice.getExpansionBoardId();
	
			shimmerDevice.setDefaultShimmerConfiguration();
			shimmerDevice.disableAllAlgorithms();
			shimmerDevice.disableAllSensors();
	
			if(fixedConfig==FIXED_SHIMMER_CONFIG_MODE.USER && fixedConfigMap!=null){
				for(Entry<String, Object> configEntry:fixedConfigMap.entrySet()){
					shimmerDevice.setConfigValueUsingConfigLabel(configEntry.getKey(), configEntry.getValue());
				}
				triggerConfiguration = true;
			}
			else if(fixedConfig==FIXED_SHIMMER_CONFIG_MODE.CADENCE){
				triggerConfiguration = true;
				setFixedConfig0(shimmerDevice);
			}
			else if(fixedConfig==FIXED_SHIMMER_CONFIG_MODE.CIMIT){
				if(expId==HW_ID_SR_CODES.SHIMMER_ECG_MD){
					triggerConfiguration = true;
					setFixedConfig1(shimmerDevice);
				}
			}
			else if(fixedConfig==FIXED_SHIMMER_CONFIG_MODE.CALIBRATION_IMU){
				triggerConfiguration = true;
				setFixedConfig2(shimmerDevice);
			}
			
//			if(triggerConfiguration){
//				if(shimmerDevice instanceof ShimmerBluetooth){
//					ShimmerBluetooth shimmerBluetooth = (ShimmerBluetooth)shimmerDevice;
//					shimmerBluetooth.setSetupDeviceDuringConnection(true);
//				}
//			}
		}
		
		return triggerConfiguration;
	}

    
	public static void setFixedConfig0(ShimmerDevice shimmerDevice) {
		if(shimmerDevice.getSensorIdsSet().contains(Shimmer3.SENSOR_ID.SHIMMER_LSM303_ACCEL)){
			//- setting wide range accel as only sensor
			shimmerDevice.setSensorEnabledState(Shimmer3.SENSOR_ID.SHIMMER_LSM303_ACCEL, true); 

			//setting accel range +/- 4g
			shimmerDevice.setConfigValueUsingConfigLabel(
					Shimmer3.SENSOR_ID.SHIMMER_LSM303_ACCEL, 
					SensorLSM303.GuiLabelConfig.LSM303_ACCEL_RANGE,
					1);
		}

		//setting sampling rate 51.2 hardcoded
		shimmerDevice.setShimmerAndSensorsSamplingRate(51.20);
	}

	public static void setFixedConfig1(ShimmerDevice shimmerDevice) {
		
		int expId = shimmerDevice.getExpansionBoardId();
		if(expId==HW_ID_SR_CODES.EXP_BRD_EXG 
				|| expId==HW_ID_SR_CODES.EXP_BRD_EXG_UNIFIED 
				|| expId==HW_ID_SR_CODES.SHIMMER_ECG_MD){
			if(shimmerDevice.getSensorIdsSet().contains(Shimmer3.SENSOR_ID.HOST_ECG)){
				//- setting ECG as only sensor
				shimmerDevice.setSensorEnabledState(Shimmer3.SENSOR_ID.HOST_ECG, true); 
	
				//setting ECG Gain to 6
				shimmerDevice.setConfigValueUsingConfigLabel(
						Shimmer3.SENSOR_ID.HOST_ECG, 
						SensorEXG.GuiLabelConfig.EXG_GAIN,
						0);
				
				//setting ECG Resolution to 24bit
				shimmerDevice.setConfigValueUsingConfigLabel(
						Shimmer3.SENSOR_ID.HOST_ECG, 
						SensorEXG.GuiLabelConfig.EXG_RESOLUTION,
						1);
				
				//setting ECG Reference Electrode to Inverse Wilson CT
				shimmerDevice.setConfigValueUsingConfigLabel(
						Shimmer3.SENSOR_ID.HOST_ECG, 
						SensorEXG.GuiLabelConfig.EXG_REFERENCE_ELECTRODE,
						13);
			}
	
			//setting sampling rate 1024Hz hardcoded -> ExG data rate should update automatically in the driver
			shimmerDevice.setShimmerAndSensorsSamplingRate(1024.0);
		}
	}

	public static void setFixedConfig2(ShimmerDevice shimmerDevice) {
		//TODO
	}

	
}
