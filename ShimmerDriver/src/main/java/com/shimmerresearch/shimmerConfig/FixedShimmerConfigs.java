package com.shimmerresearch.shimmerConfig;

import com.shimmerresearch.driver.ShimmerDevice;

import java.util.LinkedHashMap;
import java.util.Map.Entry;

import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driver.Configuration.Shimmer3;
import com.shimmerresearch.driver.Configuration.Sweatch;
import com.shimmerresearch.driverUtilities.AssembleShimmerConfig;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.HW_ID;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.HW_ID_SR_CODES;
import com.shimmerresearch.sensors.SensorEXG;
import com.shimmerresearch.sensors.lsm303.SensorLSM303;

/**This class is used for setting up the same hardware with default configurations but for different applications.
 * 
 * @author Mark Nolan
 */
public class FixedShimmerConfigs {

	//TODO change to code names for release
    public enum FIXED_SHIMMER_CONFIG_MODE {
    	NONE,
    	CADENCE,
    	CIMIT,
    	CALIBRATION_IMU,
    	USER,
    	//TODO might need to update this approach as SWEATCH is inaccessible to this class and shares no commonality with Shimmer3/4. 
    	SWEATCH_ANDROID,
    	NEUHOME,
    	NEUHOMEGSRONLY
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
				if(expId==HW_ID_SR_CODES.EXP_BRD_EXG 
						|| expId==HW_ID_SR_CODES.EXP_BRD_EXG_UNIFIED 
						|| expId==HW_ID_SR_CODES.SHIMMER_ECG_MD){
					triggerConfiguration = true;
					setFixedConfig1(shimmerDevice);
				}
			}
			else if(fixedConfig==FIXED_SHIMMER_CONFIG_MODE.NEUHOME){
				if(expId==HW_ID_SR_CODES.EXP_BRD_EXG 
						|| expId==HW_ID_SR_CODES.EXP_BRD_EXG_UNIFIED 
						){
					//only for the future if required
				} else if(expId==HW_ID_SR_CODES.EXP_BRD_GSR 
						|| expId==HW_ID_SR_CODES.EXP_BRD_GSR_UNIFIED 
						){
					setFixedConfig4(shimmerDevice);
					triggerConfiguration = true;
				}
			}
			else if(fixedConfig==FIXED_SHIMMER_CONFIG_MODE.NEUHOMEGSRONLY){
				if(expId==HW_ID_SR_CODES.EXP_BRD_EXG 
						|| expId==HW_ID_SR_CODES.EXP_BRD_EXG_UNIFIED 
						){
					//only for the future if required
				} else if(expId==HW_ID_SR_CODES.EXP_BRD_GSR 
						|| expId==HW_ID_SR_CODES.EXP_BRD_GSR_UNIFIED 
						){
					setFixedConfig5(shimmerDevice);
					triggerConfiguration = true;
				}
			}
			else if(fixedConfig==FIXED_SHIMMER_CONFIG_MODE.CALIBRATION_IMU){
				triggerConfiguration = true;
				setFixedConfig2(shimmerDevice);
			}
//			else if(fixedConfig==FIXED_SHIMMER_CONFIG_MODE.SWEATCH_ANDROID){
//				setFixedConfig3(shimmerDevice);
//			}
			
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

	public static void setFixedConfig4(ShimmerDevice shimmerDevice) {
		int expId = shimmerDevice.getExpansionBoardId();
		if(expId==HW_ID_SR_CODES.EXP_BRD_GSR 
				|| expId==HW_ID_SR_CODES.EXP_BRD_GSR_UNIFIED ){
			shimmerDevice.setShimmerAndSensorsSamplingRate(256);
			shimmerDevice.setSensorEnabledState(Shimmer3.SENSOR_ID.HOST_PPG_A13, true); 
			shimmerDevice.setSensorEnabledState(Shimmer3.SENSOR_ID.SHIMMER_GSR, true);
			
			//setting sampling rate 1024Hz hardcoded -> ExG data rate should update automatically in the driver
			
		}
	}

	public static void setFixedConfig5(ShimmerDevice shimmerDevice) {
		int expId = shimmerDevice.getExpansionBoardId();
		if(expId==HW_ID_SR_CODES.EXP_BRD_GSR 
				|| expId==HW_ID_SR_CODES.EXP_BRD_GSR_UNIFIED ){
			shimmerDevice.setShimmerAndSensorsSamplingRate(256);
			shimmerDevice.setSensorEnabledState(Shimmer3.SENSOR_ID.SHIMMER_GSR, true);
			
			//setting sampling rate 1024Hz hardcoded -> ExG data rate should update automatically in the driver
			
		}
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

	//TODO 
	public static void setFixedConfig2(ShimmerDevice shimmerDevice) {
		
	}
	
	//TODO
//	public static void setFixedConfig3(ShimmerDevice shimmerDevice) {
//		int hwId = shimmerDevice.getHardwareVersion();
//		if(hwId==HW_ID.SWEATCH){
//			shimmerDevice.setDefaultShimmerConfiguration();
////			//ADC RATE
////			shimmerDevice.setConfigValueUsingConfigLabel(
////					Sweatch.SENSOR_ID.SWEATCH_ADC, 
////					SensorSweatch.GuiLabelConfig.EXG_GAIN,
////					0);
////			//PGA GAIN
////			shimmerDevice.setConfigValueUsingConfigLabel(
////					Sweatch.SENSOR_ID.SWEATCH_ADC, 
////					SensorEXG.GuiLabelConfig.EXG_GAIN,
////					0);
////			//Device Sampling rate
////			shimmerDevice.setConfigValueUsingConfigLabel(
////					Sweatch.SENSOR_ID.SWEATCH_ADC, 
////					SensorEXG.GuiLabelConfig.EXG_GAIN,
////					0);
//		}
//	}

	
}
