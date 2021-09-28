package com.shimmerresearch.verisense;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.FixMethodOrder;
import org.junit.Test;

import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driver.Configuration.Verisense;
import com.shimmerresearch.driverUtilities.ExpansionBoardDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.HW_ID;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;
import com.shimmerresearch.verisense.VerisenseDevice.FW_CHANGES;
import com.shimmerresearch.verisense.sensors.ISensorConfig;
import com.shimmerresearch.verisense.sensors.SensorGSRVerisense;
import com.shimmerresearch.verisense.sensors.SensorLIS2DW12;
import com.shimmerresearch.verisense.sensors.SensorLSM6DS3;
import com.shimmerresearch.verisense.sensors.SensorMAX86916;
import com.shimmerresearch.verisense.sensors.SensorMAX86XXX;

/**
 * Tests Verisense config byte parsing and generation
 * @author Mark Nolan
 *
 */
@FixMethodOrder
public class API_00004_VerisenseConfigByteParsingAndGeneration {

	@Test
	public void test001_verisenseDevice() {
		List<ISensorConfig> listOfSensorConfig = new ArrayList<ISensorConfig>();
		listOfSensorConfig.addAll(Arrays.asList(VerisenseDevice.BLE_TX_POWER.values()));

		ShimmerVerObject svo = FW_CHANGES.CCF21_010_3;
		ExpansionBoardDetails ebd = new ExpansionBoardDetails(HW_ID.VERISENSE_IMU, 1, 0);

		runTest(svo, ebd, new int[] {}, listOfSensorConfig);
	}

	@Test
	public void test002_sensorLis2dw12() {
		List<ISensorConfig> listOfSensorConfig = new ArrayList<ISensorConfig>();
		listOfSensorConfig.addAll(Arrays.asList(SensorLIS2DW12.LIS2DW12_ACCEL_RATE.values()));
		listOfSensorConfig.addAll(Arrays.asList(SensorLIS2DW12.LIS2DW12_BW_FILT.values()));
		listOfSensorConfig.addAll(Arrays.asList(SensorLIS2DW12.LIS2DW12_ACCEL_RANGE.values()));
		listOfSensorConfig.addAll(Arrays.asList(SensorLIS2DW12.LIS2DW12_LP_MODE.values()));
		listOfSensorConfig.addAll(Arrays.asList(SensorLIS2DW12.LIS2DW12_MODE.values()));
		listOfSensorConfig.addAll(Arrays.asList(SensorLIS2DW12.LIS2DW12_FILTERED_DATA_TYPE_SELECTION.values()));
		listOfSensorConfig.addAll(Arrays.asList(SensorLIS2DW12.LIS2DW12_LOW_NOISE.values()));
		listOfSensorConfig.addAll(Arrays.asList(SensorLIS2DW12.LIS2DW12_HP_REF_MODE.values()));
		listOfSensorConfig.addAll(Arrays.asList(SensorLIS2DW12.LIS2DW12_FIFO_MODE.values()));
		listOfSensorConfig.addAll(Arrays.asList(SensorLIS2DW12.LIS2DW12_FIFO_THRESHOLD.values()));
		
		ShimmerVerObject svo = FW_CHANGES.CCF21_010_3;
		ExpansionBoardDetails ebd = new ExpansionBoardDetails(HW_ID.VERISENSE_IMU, 1, 0);

		runTest(svo, ebd, new int[] {Verisense.SENSOR_ID.LIS2DW12_ACCEL}, listOfSensorConfig);
	}

	@Test
	public void test003_sensorLsm6ds3() {
		List<ISensorConfig> listOfSensorConfig = new ArrayList<ISensorConfig>();
		listOfSensorConfig.addAll(Arrays.asList(SensorLSM6DS3.LSM6DS3_ACCEL_RANGE.values()));
		listOfSensorConfig.addAll(Arrays.asList(SensorLSM6DS3.LSM6DS3_GYRO_RANGE.values()));
		listOfSensorConfig.addAll(Arrays.asList(SensorLSM6DS3.LSM6DS3_RATE.values()));
		listOfSensorConfig.addAll(Arrays.asList(SensorLSM6DS3.FIFO_DECIMATION_GYRO.values()));
		listOfSensorConfig.addAll(Arrays.asList(SensorLSM6DS3.FIFO_DECIMATION_ACCEL.values()));
		listOfSensorConfig.addAll(Arrays.asList(SensorLSM6DS3.FIFO_MODE.values()));
		listOfSensorConfig.addAll(Arrays.asList(SensorLSM6DS3.ACCEL_ANTI_ALIASING_BANDWIDTH_FILTER.values()));
		listOfSensorConfig.addAll(Arrays.asList(SensorLSM6DS3.HIGH_PASS_FILTER_CUT_OFF_FREQ_GYRO.values()));
		
		ShimmerVerObject svo = FW_CHANGES.CCF21_010_3;
		ExpansionBoardDetails ebd = new ExpansionBoardDetails(HW_ID.VERISENSE_IMU, 1, 0);
		
		runTest(svo, ebd, new int[] {Verisense.SENSOR_ID.LSM6DS3_ACCEL, Verisense.SENSOR_ID.LSM6DS3_GYRO}, listOfSensorConfig);
	}

	@Test
	public void test004_sensorBattVoltage() {
		List<ISensorConfig> listOfSensorConfig = new ArrayList<ISensorConfig>();
//		listOfSensorConfig.addAll(Arrays.asList(SensorBattVoltageVerisense..values()));

		ShimmerVerObject svo = FW_CHANGES.CCF21_010_3;
		ExpansionBoardDetails ebd = new ExpansionBoardDetails(HW_ID.VERISENSE_IMU, 1, 0);

		runTest(svo, ebd, new int[] {Verisense.SENSOR_ID.VBATT}, listOfSensorConfig);
	}

	@Test
	public void test005_sensorGsr() {
		List<ISensorConfig> listOfSensorConfig = new ArrayList<ISensorConfig>();
		listOfSensorConfig.addAll(Arrays.asList(SensorGSRVerisense.GSR_RANGE.values()));
		
		ShimmerVerObject svo = FW_CHANGES.CCF21_010_3;
		ExpansionBoardDetails ebd = new ExpansionBoardDetails(HW_ID.VERISENSE_GSR_PLUS, 7, 0);
		
		runTest(svo, ebd, new int[] {Verisense.SENSOR_ID.GSR}, listOfSensorConfig);
	}

	@Test
	public void test006_sensorMax86xxxx() {
		List<ISensorConfig> listOfSensorConfig = new ArrayList<ISensorConfig>();
		listOfSensorConfig.addAll(Arrays.asList(SensorMAX86XXX.MAX86XXX_ADC_RESOLUTION.values()));
		listOfSensorConfig.addAll(Arrays.asList(SensorMAX86XXX.MAX86XXX_PULSE_WIDTH.values()));
		listOfSensorConfig.addAll(Arrays.asList(SensorMAX86XXX.MAX86XXX_SAMPLE_AVG.values()));
		listOfSensorConfig.addAll(Arrays.asList(SensorMAX86916.MAX86916_SAMPLE_RATE.values()));
		listOfSensorConfig.addAll(Arrays.asList(SensorMAX86916.PROX_DETECTION_MODE.values()));
		
		ShimmerVerObject svo = FW_CHANGES.CCF21_010_3;
		ExpansionBoardDetails ebd = new ExpansionBoardDetails(HW_ID.VERISENSE_PULSE_PLUS, 7, 0);
		
		runTest(svo, ebd, new int[] {Verisense.SENSOR_ID.MAX86916_PPG_BLUE, Verisense.SENSOR_ID.MAX86916_PPG_GREEN, Verisense.SENSOR_ID.MAX86XXX_PPG_IR, Verisense.SENSOR_ID.MAX86XXX_PPG_RED}, listOfSensorConfig);
	}

	private VerisenseDevice setupVerisenseDevice(ShimmerVerObject svo, ExpansionBoardDetails ebd) {
		VerisenseDevice verisenseDeviceSrc = new VerisenseDevice();
		
		verisenseDeviceSrc.setShimmerVersionObject(svo);
		verisenseDeviceSrc.setHardwareVersion(ebd.getExpansionBoardId());
		verisenseDeviceSrc.setExpansionBoardDetails(ebd);
		verisenseDeviceSrc.sensorAndConfigMapsCreate();
		
		return verisenseDeviceSrc;
	}

	private void runTest(ShimmerVerObject svo, ExpansionBoardDetails ebd, int[] sensorIds, List<ISensorConfig> listOfSensorConfig) {
		
		COMMUNICATION_TYPE commType = COMMUNICATION_TYPE.BLUETOOTH;
		
		VerisenseDevice verisenseDeviceSrc = setupVerisenseDevice(svo, ebd);

		if(sensorIds.length>0) {
			for(int sensorId:sensorIds) {
				verisenseDeviceSrc.setSensorEnabledState(sensorId, true);
			}
		}
		
		for(ISensorConfig sensorConfig:listOfSensorConfig) {
			verisenseDeviceSrc.setSensorConfig(sensorConfig);

			byte[] configBytes = verisenseDeviceSrc.configBytesGenerate(true, commType);
			
			VerisenseDevice verisenseDeviceTarget = setupVerisenseDevice(svo, ebd);
			verisenseDeviceTarget.configBytesParse(configBytes, commType);
			List<ISensorConfig> listOfSensorConfigTarget = verisenseDeviceTarget.getSensorConfig();

			String res = sensorConfig.getClass().getSimpleName() + "." + sensorConfig.toString() + " -> " + (listOfSensorConfigTarget.contains(sensorConfig)? "PASS":"FAIL");
			System.out.println(res);
			assertTrue(res, listOfSensorConfigTarget.contains(sensorConfig));
		}
	}

}
