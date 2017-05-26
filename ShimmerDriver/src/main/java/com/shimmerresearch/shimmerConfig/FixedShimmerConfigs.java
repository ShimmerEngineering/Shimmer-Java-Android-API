package com.shimmerresearch.shimmerConfig;

import com.shimmerresearch.bluetooth.ShimmerBluetooth;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driver.Configuration.Shimmer3;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.HW_ID_SR_CODES;
import com.shimmerresearch.sensors.SensorEXG;
import com.shimmerresearch.sensors.SensorLSM303;
import com.shimmerresearch.shimmerConfig.FixedShimmerConfigs.FIXED_SHIMMER_CONFIG;

public class FixedShimmerConfigs {

	//TODO change to code names for release
    public enum FIXED_SHIMMER_CONFIG {
    	NONE,
    	CADENCE,
    	CIMIT,
    	CALIBRATION_IMU
    }
    
	public static boolean setFixedConfigWhenConnecting(ShimmerDevice shimmerDevice, FIXED_SHIMMER_CONFIG fixedConfig) {
		boolean triggerConfiguration = false;
		
		if(fixedConfig!=FIXED_SHIMMER_CONFIG.NONE){
	
			int expId = shimmerDevice.getExpansionBoardId();
	
			shimmerDevice.setDefaultShimmerConfiguration();
			shimmerDevice.disableAllAlgorithms();
			shimmerDevice.disableAllSensors();
	
			if(fixedConfig==FIXED_SHIMMER_CONFIG.CADENCE){
				triggerConfiguration = true;
				setFixedConfig0(shimmerDevice);
			}
			else if(fixedConfig==FIXED_SHIMMER_CONFIG.CIMIT){
				if(expId==HW_ID_SR_CODES.EXP_BRD_EXG || expId==HW_ID_SR_CODES.EXP_BRD_EXG_UNIFIED){
					triggerConfiguration = true;
					setFixedConfig1(shimmerDevice);
				}
			}
			else if(fixedConfig==FIXED_SHIMMER_CONFIG.CALIBRATION_IMU){
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
		if(shimmerDevice.getSensorMapKeySet().contains(Shimmer3.SensorMapKey.SHIMMER_LSM303DLHC_ACCEL)){
			//- setting wide range accel as only sensor
			shimmerDevice.setSensorEnabledState(Shimmer3.SensorMapKey.SHIMMER_LSM303DLHC_ACCEL, true); 

			//setting accel range +/- 4g
			shimmerDevice.setConfigValueUsingConfigLabel(
					Shimmer3.SensorMapKey.SHIMMER_LSM303DLHC_ACCEL, 
					SensorLSM303.GuiLabelConfig.LSM303DLHC_ACCEL_RANGE,
					1);
		}

		//setting sampling rate 51.2 hardcoded
		shimmerDevice.setShimmerAndSensorsSamplingRate(51.20);
	}

	public static void setFixedConfig1(ShimmerDevice shimmerDevice) {
		
		int expId = shimmerDevice.getExpansionBoardId();
		if(expId==HW_ID_SR_CODES.EXP_BRD_EXG || expId==HW_ID_SR_CODES.EXP_BRD_EXG_UNIFIED){
			if(shimmerDevice.getSensorMapKeySet().contains(Shimmer3.SensorMapKey.HOST_ECG)){
				//- setting ECG as only sensor
				shimmerDevice.setSensorEnabledState(Shimmer3.SensorMapKey.HOST_ECG, true); 
	
				//setting ECG Gain to 6
				shimmerDevice.setConfigValueUsingConfigLabel(
						Shimmer3.SensorMapKey.HOST_ECG, 
						SensorEXG.GuiLabelConfig.EXG_GAIN,
						0);
				
				//setting ECG Resolution to 24bit
				shimmerDevice.setConfigValueUsingConfigLabel(
						Shimmer3.SensorMapKey.HOST_ECG, 
						SensorEXG.GuiLabelConfig.EXG_RESOLUTION,
						1);
			}
	
			//setting sampling rate 1024Hz hardcoded -> ExG data rate should update automatically in the driver
			shimmerDevice.setShimmerAndSensorsSamplingRate(1024.0);
		}
	}

	public static void setFixedConfig2(ShimmerDevice shimmerDevice) {
		//TODO
	}

	
}
