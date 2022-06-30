package com.shimmerresearch.verisense.sensors;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.shimmerresearch.driver.Configuration.CHANNEL_UNITS;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driver.Configuration.Verisense.CompatibilityInfoForMaps;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driver.calibration.CalibDetails;
import com.shimmerresearch.driver.calibration.CalibDetailsKinematic;
import com.shimmerresearch.driver.calibration.UtilCalibration;
import com.shimmerresearch.driverUtilities.ChannelDetails;
import com.shimmerresearch.driverUtilities.ConfigOptionDetailsSensor;
import com.shimmerresearch.driverUtilities.SensorDetails;
import com.shimmerresearch.driverUtilities.SensorDetailsRef;
import com.shimmerresearch.driverUtilities.SensorGroupingDetails;
import com.shimmerresearch.driverUtilities.UtilShimmer;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_ENDIAN;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_TYPE;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_TYPE;
import com.shimmerresearch.sensors.AbstractSensor;
import com.shimmerresearch.sensors.ActionSetting;
import com.shimmerresearch.verisense.UtilVerisenseDriver;
import com.shimmerresearch.verisense.VerisenseDevice;
import com.shimmerresearch.verisense.communication.payloads.OperationalConfigPayload.OP_CONFIG_BYTE_INDEX;
import com.shimmerresearch.verisense.payloaddesign.AsmBinaryFileConstants.PAYLOAD_CONFIG_BYTE_INDEX;

/** Sensor chip that contains an accelerometer. 
 * 
 * This sensor is typically refereed to as Accel1 in the Verisense hardware.
 *  
 * @author Mark Nolan
 *
 */
public class SensorLIS2DW12 extends AbstractSensor {
	
	private static final long serialVersionUID = -3219602356151087121L;
	
	public static final int FIFO_SIZE_IN_CHIP = 192;
	public static final int MAX_FIFOS_IN_PAYLOAD = 169;
	
	public static final String ACCEL_ID = "Accel1";
	
	protected LIS2DW12_ACCEL_RANGE range = LIS2DW12_ACCEL_RANGE.RANGE_4G;
	protected LIS2DW12_ACCEL_RATE rate = LIS2DW12_ACCEL_RATE.LOW_POWER_25_0_HZ;
	protected LIS2DW12_MODE mode = LIS2DW12_MODE.HIGH_PERFORMANCE;
	protected LIS2DW12_LP_MODE lpMode = LIS2DW12_LP_MODE.LOW_POWER1_12BIT_4_5_MG_NOISE;
	protected LIS2DW12_BW_FILT bwFilt = LIS2DW12_BW_FILT.ODR_DIVIDED_BY_2;
	protected LIS2DW12_FILTERED_DATA_TYPE_SELECTION fds = LIS2DW12_FILTERED_DATA_TYPE_SELECTION.LOW_PASS_FILTER_PATH_SELECTED;
	protected LIS2DW12_LOW_NOISE lowNoise = LIS2DW12_LOW_NOISE.DISABLED;
	protected LIS2DW12_HP_REF_MODE hpFilterMode = LIS2DW12_HP_REF_MODE.DISABLED;
	protected LIS2DW12_FIFO_MODE fifoMode = LIS2DW12_FIFO_MODE.CONTINUOUS_TO_FIFO_MODE;
	protected LIS2DW12_FIFO_THRESHOLD fifoThreshold = LIS2DW12_FIFO_THRESHOLD.SAMPLE_31;

	public static enum LIS2DW12_FILTERED_DATA_TYPE_SELECTION implements ISensorConfig {
		LOW_PASS_FILTER_PATH_SELECTED("Low-pass filter path selected", 0),
		HIGH_PASS_FILTER_PATH_SELECTED("High-pass filter path selected", 1);
		
		String label;
		Integer configValue;
		
		static Map<String, Integer> REF_MAP = new HashMap<>();
		static {
			for (LIS2DW12_FILTERED_DATA_TYPE_SELECTION e : values()) {
				REF_MAP.put(e.label, e.configValue);
			}
		}

		static Map<Integer, LIS2DW12_FILTERED_DATA_TYPE_SELECTION> BY_CONFIG_VALUE = new HashMap<>();
		static {
			for (LIS2DW12_FILTERED_DATA_TYPE_SELECTION e : values()) {
				BY_CONFIG_VALUE.put(e.configValue, e);
			}
		}

		private LIS2DW12_FILTERED_DATA_TYPE_SELECTION(String label, int configValue) {
			this.label = label;
			this.configValue = configValue;
		}
		
		public static String[] getLabels() {
			return REF_MAP.keySet().toArray(new String[REF_MAP.keySet().size()]);
		}
		
		public static Integer[] getConfigValues() {
			return REF_MAP.values().toArray(new Integer[REF_MAP.values().size()]);
		}
		
		public static LIS2DW12_FILTERED_DATA_TYPE_SELECTION getForConfigValue(int configValue) {
			return BY_CONFIG_VALUE.get(UtilShimmer.nudgeInteger(configValue, LOW_PASS_FILTER_PATH_SELECTED.configValue, HIGH_PASS_FILTER_PATH_SELECTED.configValue));
		}
	}
	
	public static enum LIS2DW12_LOW_NOISE implements ISensorConfig {
		DISABLED("Disabled", 0),
		ENABLED("Enabled", 1);
		
		String label;
		Integer configValue;
		
		static Map<String, Integer> REF_MAP = new HashMap<>();
		static {
			for (LIS2DW12_LOW_NOISE e : values()) {
				REF_MAP.put(e.label, e.configValue);
			}
		}

		static Map<Integer, LIS2DW12_LOW_NOISE> BY_CONFIG_VALUE = new HashMap<>();
		static {
			for (LIS2DW12_LOW_NOISE e : values()) {
				BY_CONFIG_VALUE.put(e.configValue, e);
			}
		}

		private LIS2DW12_LOW_NOISE(String label, int configValue) {
			this.label = label;
			this.configValue = configValue;
		}
		
		public static String[] getLabels() {
			return REF_MAP.keySet().toArray(new String[REF_MAP.keySet().size()]);
		}
		
		public static Integer[] getConfigValues() {
			return REF_MAP.values().toArray(new Integer[REF_MAP.values().size()]);
		}
		
		public static LIS2DW12_LOW_NOISE getForConfigValue(int configValue) {
			return BY_CONFIG_VALUE.get(UtilShimmer.nudgeInteger(configValue, DISABLED.configValue, ENABLED.configValue));
		}
	}
	
	public static enum LIS2DW12_BW_FILT implements ISensorConfig {
		ODR_DIVIDED_BY_2("ODR/2 (up to ODR = 800 Hz, 400 Hz when ODR = 1600 Hz)", 0),
		ODR_DIVIDED_BY_4("ODR/4 (HP/LP)", 1),
		ODR_DIVIDED_BY_10("ODR/10 (HP/LP)", 2),
		ODR_DIVIDED_BY_20("ODR/20 (HP/LP)", 3);
		
		String label;
		Integer configValue;
		
		static Map<String, Integer> REF_MAP = new HashMap<>();
		static {
			for (LIS2DW12_BW_FILT e : values()) {
				REF_MAP.put(e.label, e.configValue);
			}
		}

		static Map<Integer, LIS2DW12_BW_FILT> BY_CONFIG_VALUE = new HashMap<>();
		static {
			for (LIS2DW12_BW_FILT e : values()) {
				BY_CONFIG_VALUE.put(e.configValue, e);
			}
		}

		private LIS2DW12_BW_FILT(String label, int configValue) {
			this.label = label;
			this.configValue = configValue;
		}
		
		public static String[] getLabels() {
			return REF_MAP.keySet().toArray(new String[REF_MAP.keySet().size()]);
		}
		
		public static Integer[] getConfigValues() {
			return REF_MAP.values().toArray(new Integer[REF_MAP.values().size()]);
		}
		
		public static LIS2DW12_BW_FILT getForConfigValue(int configValue) {
			return BY_CONFIG_VALUE.get(UtilShimmer.nudgeInteger(configValue, ODR_DIVIDED_BY_2.configValue, ODR_DIVIDED_BY_20.configValue));
		}
	}

	public static enum LIS2DW12_MODE implements ISensorConfig {
		LOW_POWER("Low-Power Mode (12/14-bit resolution)", 0),
		HIGH_PERFORMANCE("High-Performance Mode (14-bit resolution)", 1);
		
		String label;
		Integer configValue;
		
		static Map<String, Integer> REF_MAP = new HashMap<>();
		static {
			for (LIS2DW12_MODE e : values()) {
				REF_MAP.put(e.label, e.configValue);
			}
		}
		
		static Map<Integer, LIS2DW12_MODE> BY_CONFIG_VALUE = new HashMap<>();
		static {
			for (LIS2DW12_MODE e : values()) {
				BY_CONFIG_VALUE.put(e.configValue, e);
			}
		}

		private LIS2DW12_MODE(String label, int configValue) {
			this.label = label;
			this.configValue = configValue;
		}
		
		public static String[] getLabels() {
			return REF_MAP.keySet().toArray(new String[REF_MAP.keySet().size()]);
		}
		
		public static Integer[] getConfigValues() {
			return REF_MAP.values().toArray(new Integer[REF_MAP.values().size()]);
		}
		
		public static LIS2DW12_MODE getForConfigValue(int configValue) {
			return BY_CONFIG_VALUE.get(UtilShimmer.nudgeInteger(configValue, LOW_POWER.configValue, HIGH_PERFORMANCE.configValue));
		}
	}

	public static enum LIS2DW12_ACCEL_RATE implements ISensorConfig {
		POWER_DOWN("Power-down", 0, 0.0, LIS2DW12_MODE.LOW_POWER),
		HIGH_PERFORMANCE_12_5_HZ("12.5Hz", 1, 12.5, LIS2DW12_MODE.HIGH_PERFORMANCE),
		HIGH_PERFORMANCE_25_0_HZ("25.0Hz", 3, 25.0, LIS2DW12_MODE.HIGH_PERFORMANCE),
		HIGH_PERFORMANCE_50_0_HZ("50.0Hz", 4, 50.0, LIS2DW12_MODE.HIGH_PERFORMANCE),
		HIGH_PERFORMANCE_100_0_HZ("100.0Hz", 5, 100.0, LIS2DW12_MODE.HIGH_PERFORMANCE),
		HIGH_PERFORMANCE_200_0_HZ("200.0Hz", 6, 200.0, LIS2DW12_MODE.HIGH_PERFORMANCE),
		HIGH_PERFORMANCE_400_0_HZ("400.0Hz", 7, 400.0, LIS2DW12_MODE.HIGH_PERFORMANCE),
		HIGH_PERFORMANCE_800_0_HZ("800.0Hz", 8, 800.0, LIS2DW12_MODE.HIGH_PERFORMANCE),
		HIGH_PERFORMANCE_1600_0_HZ("1600.0Hz", 9, 1600.0, LIS2DW12_MODE.HIGH_PERFORMANCE),
		LOW_POWER_1_6_HZ("1.6Hz", 1, 1.6, LIS2DW12_MODE.LOW_POWER),
		LOW_POWER_12_5_HZ("12.5Hz", 2, 12.5, LIS2DW12_MODE.LOW_POWER),
		LOW_POWER_25_0_HZ("25.0Hz", 3, 25.0, LIS2DW12_MODE.LOW_POWER),
		LOW_POWER_50_0_HZ("50.0Hz", 4, 50.0, LIS2DW12_MODE.LOW_POWER),
		LOW_POWER_100_0_HZ("100.0Hz", 5, 100.0, LIS2DW12_MODE.LOW_POWER),
		LOW_POWER_200_0_HZ("200.0Hz", 6, 200.0, LIS2DW12_MODE.LOW_POWER),
		LOW_POWER_200_0_HZ_ALT1("200.0Hz", 7, 200.0, LIS2DW12_MODE.LOW_POWER),
		LOW_POWER_200_0_HZ_ALT2("200.0Hz", 8, 200.0, LIS2DW12_MODE.LOW_POWER),
		LOW_POWER_200_0_HZ_ALT3("200.0Hz", 9, 200.0, LIS2DW12_MODE.LOW_POWER);
		
		public String label;
		public Integer configValue;
		public double freqHz;
		public LIS2DW12_MODE mode;

		public static Map<String, Integer> REF_MAP_HP = new HashMap<>();
		public static Map<String, Integer> REF_MAP_LP = new HashMap<>();
		static {
			for (LIS2DW12_ACCEL_RATE e : values()) {
				if(e.mode==LIS2DW12_MODE.HIGH_PERFORMANCE || e==LIS2DW12_ACCEL_RATE.POWER_DOWN) {
					REF_MAP_HP.put(e.label, e.configValue);
				}
				if(e.mode==LIS2DW12_MODE.LOW_POWER || e==LIS2DW12_ACCEL_RATE.POWER_DOWN) {
					REF_MAP_LP.put(e.label, e.configValue);
				}
			}
		}

		private LIS2DW12_ACCEL_RATE(String label, int configValue, double freqHz, LIS2DW12_MODE mode) {
			this.label = label;
			this.configValue = configValue;
			this.freqHz = freqHz;
			this.mode = mode;
		}

		public static String[] getLabelsHp() {
			return REF_MAP_HP.keySet().toArray(new String[REF_MAP_HP.keySet().size()]);
		}
		
		public static Integer[] getConfigValuesHp() {
			return REF_MAP_HP.values().toArray(new Integer[REF_MAP_HP.values().size()]);
		}

		public static String[] getLabelsLp() {
			return REF_MAP_LP.keySet().toArray(new String[REF_MAP_LP.keySet().size()]);
		}
		
		public static Integer[] getConfigValuesLp() {
			return REF_MAP_LP.values().toArray(new Integer[REF_MAP_LP.values().size()]);
		}
		
		public static LIS2DW12_ACCEL_RATE getForConfigValue(int configValue, LIS2DW12_MODE list2dw12Mode) {
			int valueToSet = UtilShimmer.nudgeInteger(configValue, POWER_DOWN.configValue, HIGH_PERFORMANCE_1600_0_HZ.configValue);
			if(valueToSet==LIS2DW12_ACCEL_RATE.POWER_DOWN.configValue) {
				return LIS2DW12_ACCEL_RATE.POWER_DOWN;
			} else {
				for(LIS2DW12_ACCEL_RATE lis2dw12AccelRate : LIS2DW12_ACCEL_RATE.values()) {
					if(list2dw12Mode==lis2dw12AccelRate.mode && lis2dw12AccelRate.configValue==valueToSet) {
						return lis2dw12AccelRate;
					}
				}
			}
			return LIS2DW12_ACCEL_RATE.POWER_DOWN;
		}
	}
	
	public static enum LIS2DW12_ACCEL_RANGE implements ISensorConfig {
		RANGE_2G(UtilShimmer.UNICODE_PLUS_MINUS + " 2g", 0),
		RANGE_4G(UtilShimmer.UNICODE_PLUS_MINUS + " 4g", 1),
		RANGE_8G(UtilShimmer.UNICODE_PLUS_MINUS + " 8g", 2),
		RANGE_16G(UtilShimmer.UNICODE_PLUS_MINUS + " 16g", 3);
		
		String label;
		Integer configValue;

		static Map<String, Integer> REF_MAP = new HashMap<>();
		static {
			for (LIS2DW12_ACCEL_RANGE e : values()) {
				REF_MAP.put(e.label, e.configValue);
			}
		}
		
		static Map<Integer, LIS2DW12_ACCEL_RANGE> BY_CONFIG_VALUE = new HashMap<>();
		static {
			for (LIS2DW12_ACCEL_RANGE e : values()) {
				BY_CONFIG_VALUE.put(e.configValue, e);
			}
		}

		private LIS2DW12_ACCEL_RANGE(String label, Integer configValue) {
			this.label = label;
			this.configValue = configValue;
		}
		
		public static String[] getLabels() {
			return REF_MAP.keySet().toArray(new String[REF_MAP.keySet().size()]);
		}
		
		public static Integer[] getConfigValues() {
			return REF_MAP.values().toArray(new Integer[REF_MAP.values().size()]);
		}
		
		public static LIS2DW12_ACCEL_RANGE getForConfigValue(int configValue) {
			return BY_CONFIG_VALUE.get(UtilShimmer.nudgeInteger(configValue, RANGE_2G.configValue, RANGE_16G.configValue));
		}
	}

	public static enum LIS2DW12_LP_MODE implements ISensorConfig {
		LOW_POWER1_12BIT_4_5_MG_NOISE("LP1: 12-bit resolution, Noise=4.5mg(RMS)", 0),
		LOW_POWER2_14BIT_2_4_MG_NOISE("LP2: 14-bit resolution, Noise=2.4mg(RMS)", 1),
		LOW_POWER3_14BIT_1_8_MG_NOISE("LP3: 14-bit resolution, Noise=1.8mg(RMS)", 2),
		LOW_POWER4_14BIT_1_3_MG_NOISE("LP4: 14-bit resolution, Noise=1.3mg(RMS)", 3);
		
		String label;
		Integer configValue;

		static Map<String, Integer> REF_MAP = new HashMap<>();
		static {
			for (LIS2DW12_LP_MODE e : values()) {
				REF_MAP.put(e.label, e.configValue);
			}
		}

		static Map<Integer, LIS2DW12_LP_MODE> BY_CONFIG_VALUE = new HashMap<>();
		static {
			for (LIS2DW12_LP_MODE e : values()) {
				BY_CONFIG_VALUE.put(e.configValue, e);
			}
		}

		private LIS2DW12_LP_MODE(String label, Integer configValue) {
			this.label = label;
			this.configValue = configValue;
		}
		
		public static String[] getLabels() {
			return REF_MAP.keySet().toArray(new String[REF_MAP.keySet().size()]);
		}
		
		public static Integer[] getConfigValues() {
			return REF_MAP.values().toArray(new Integer[REF_MAP.values().size()]);
		}
		
		public static LIS2DW12_LP_MODE getForConfigValue(int configValue) {
			return BY_CONFIG_VALUE.get(UtilShimmer.nudgeInteger(configValue, LOW_POWER1_12BIT_4_5_MG_NOISE.configValue, LOW_POWER4_14BIT_1_3_MG_NOISE.configValue));
		}
	}

	public static enum LIS2DW12_HP_REF_MODE implements ISensorConfig {
		DISABLED("High-pass filter reference mode disabled", 0),
		ENABLED("High-pass filter reference mode enabled", 1);
		
		String label;
		Integer configValue;
		
		static Map<String, Integer> REF_MAP = new HashMap<>();
		static {
			for (LIS2DW12_HP_REF_MODE e : values()) {
				REF_MAP.put(e.label, e.configValue);
			}
		}

		static Map<Integer, LIS2DW12_HP_REF_MODE> BY_CONFIG_VALUE = new HashMap<>();
		static {
			for (LIS2DW12_HP_REF_MODE e : values()) {
				BY_CONFIG_VALUE.put(e.configValue, e);
			}
		}

		private LIS2DW12_HP_REF_MODE(String label, int configValue) {
			this.label = label;
			this.configValue = configValue;
		}
		
		public static String[] getLabels() {
			return REF_MAP.keySet().toArray(new String[REF_MAP.keySet().size()]);
		}
		
		public static Integer[] getConfigValues() {
			return REF_MAP.values().toArray(new Integer[REF_MAP.values().size()]);
		}
		
		public static LIS2DW12_HP_REF_MODE getForConfigValue(int configValue) {
			return BY_CONFIG_VALUE.get(UtilShimmer.nudgeInteger(configValue, DISABLED.configValue, ENABLED.configValue));
		}
	}

	public static enum LIS2DW12_FIFO_MODE implements ISensorConfig {
		BYPASS_MODE("Bypass mode: FIFO turned off", 0b000),
		FIFO_MODE("FIFO mode: Stops collecting data when FIFO is full", 0b001),
		CONTINUOUS_TO_FIFO_MODE("Continuous-to-FIFO: Stream mode until trigger is deasserted, then FIFO mode", 0b011),
		BYPASS_TO_FIFO_MODE("Bypass-to-Continuous: Bypass mode until trigger is deasserted, then FIFO mode", 0b100),
		CONTINUOUS_MODE("Continuous mode: If the FIFO is full, the new sample overwrites the older sample", 0b110);
		
		String label;
		Integer configValue;

		static Map<String, Integer> REF_MAP = new HashMap<>();
		static {
			for (LIS2DW12_FIFO_MODE e : values()) {
				REF_MAP.put(e.label, e.configValue);
			}
		}

		static Map<Integer, LIS2DW12_FIFO_MODE> BY_CONFIG_VALUE = new HashMap<>();
		static {
			for (LIS2DW12_FIFO_MODE e : values()) {
				BY_CONFIG_VALUE.put(e.configValue, e);
			}
		}

		private LIS2DW12_FIFO_MODE(String label, Integer configValue) {
			this.label = label;
			this.configValue = configValue;
		}
		
		public static String[] getLabels() {
			return REF_MAP.keySet().toArray(new String[REF_MAP.keySet().size()]);
		}
		
		public static Integer[] getConfigValues() {
			return REF_MAP.values().toArray(new Integer[REF_MAP.values().size()]);
		}
		
		public static LIS2DW12_FIFO_MODE getForConfigValue(int configValue) {
			//Different approach here as supported config values are not a continuous range
			for(LIS2DW12_FIFO_MODE fifoMode:LIS2DW12_FIFO_MODE.values()) {
				if(configValue==fifoMode.configValue) {
					return fifoMode;
				}
			}
			return LIS2DW12_FIFO_MODE.CONTINUOUS_TO_FIFO_MODE;
		}
	}

	public static enum LIS2DW12_FIFO_THRESHOLD implements ISensorConfig {
		SAMPLE_0("0", 0),
		SAMPLE_1("1", 1),
		SAMPLE_2("2", 2),
		SAMPLE_3("3", 3),
		SAMPLE_4("4", 4),
		SAMPLE_5("5", 5),
		SAMPLE_6("6", 6),
		SAMPLE_7("7", 7),
		SAMPLE_8("8", 8),
		SAMPLE_9("9", 9),
		SAMPLE_10("10", 10),
		SAMPLE_11("11", 11),
		SAMPLE_12("12", 12),
		SAMPLE_13("13", 13),
		SAMPLE_14("14", 14),
		SAMPLE_15("15", 15),
		SAMPLE_16("16", 16),
		SAMPLE_17("17", 17),
		SAMPLE_18("18", 18),
		SAMPLE_19("19", 19),
		SAMPLE_20("20", 20),
		SAMPLE_21("21", 21),
		SAMPLE_22("22", 22),
		SAMPLE_23("23", 23),
		SAMPLE_24("24", 24),
		SAMPLE_25("25", 25),
		SAMPLE_26("26", 26),
		SAMPLE_27("27", 27),
		SAMPLE_28("28", 28),
		SAMPLE_29("29", 29),
		SAMPLE_30("30", 30),
		SAMPLE_31("31", 31);
		
		String label;
		Integer configValue;

		static Map<String, Integer> REF_MAP = new HashMap<>();
		static {
			for (LIS2DW12_FIFO_THRESHOLD e : values()) {
				REF_MAP.put(e.label, e.configValue);
			}
		}

		static Map<Integer, LIS2DW12_FIFO_THRESHOLD> BY_CONFIG_VALUE = new HashMap<>();
		static {
			for (LIS2DW12_FIFO_THRESHOLD e : values()) {
				BY_CONFIG_VALUE.put(e.configValue, e);
			}
		}

		private LIS2DW12_FIFO_THRESHOLD(String label, Integer configValue) {
			this.label = label;
			this.configValue = configValue;
		}
		
		public static String[] getLabels() {
			return REF_MAP.keySet().toArray(new String[REF_MAP.keySet().size()]);
		}
		
		public static Integer[] getConfigValues() {
			return REF_MAP.values().toArray(new Integer[REF_MAP.values().size()]);
		}
		
		public static LIS2DW12_FIFO_THRESHOLD getForConfigValue(int configValue) {
			return BY_CONFIG_VALUE.get(UtilShimmer.nudgeInteger(configValue, SAMPLE_0.configValue, SAMPLE_31.configValue));
		}
	}

	public static final String[] LIS2DW12_RESOLUTION={
			"12-bit",
			"14-bit"};

	public static final String[] LIS2DW12_MODE_MERGED={
			"High-Performance Mode",
			"Low-Power Mode 1, RMS Noise = 4.5 mg",
			"Low-Power Mode 2, RMS Noise = 2.4 mg",
			"Low-Power Mode 3, RMS Noise = 1.8 mg",
			"Low-Power Mode 4, RMS Noise = 1.3 mg"};
	
	// --------------- Configuration options start ----------------

	public class GuiLabelSensors{
		public static final String ACCEL1 = "Accelerometer1"; 
	}

	public static class ObjectClusterSensorName{
		public static  String LIS2DW12_ACC_X = ACCEL_ID + "_X";
		public static  String LIS2DW12_ACC_Y = ACCEL_ID + "_Y";
		public static  String LIS2DW12_ACC_Z= ACCEL_ID + "_Z";
	}
	
	public static class LABEL_SENSOR_TILE{
		public static final String ACCEL = "ACCEL";
	}

	public static class DatabaseChannelHandles{
		public static final String LIS2DW12_ACC_X = "LIS2DW12_ACC_X";
		public static final String LIS2DW12_ACC_Y = "LIS2DW12_ACC_Y";
		public static final String LIS2DW12_ACC_Z = "LIS2DW12_ACC_Z";
	}

	public class GuiLabelConfig{
		public static final String LIS2DW12_RANGE = "Range";
		public static final String LIS2DW12_RATE = "Accel_Rate";
		public static final String LIS2DW12_MODE = "Mode";
		public static final String LIS2DW12_LP_MODE = "LP Mode";
	}
	
	public static final class DatabaseConfigHandle{
		public static final String LIS2DW12_RANGE = "LIS2DW12_Mag_Range";
		public static final String LIS2DW12_RATE = "LIS2DW12_Mag_Rate";
		public static final String LIS2DW12_MODE = "LIS2DW12_Mode";
		public static final String LIS2DW12_LP_MODE = "LIS2DW12_LpMode";
	}

	public static final ConfigOptionDetailsSensor CONFIG_OPTION_ACCEL_RANGE = new ConfigOptionDetailsSensor (
			SensorLIS2DW12.GuiLabelConfig.LIS2DW12_RANGE,
			SensorLIS2DW12.DatabaseConfigHandle.LIS2DW12_RANGE,
			LIS2DW12_ACCEL_RANGE.getLabels(), 
			LIS2DW12_ACCEL_RANGE.getConfigValues(), 
			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoLIS2DW12);

	public static final ConfigOptionDetailsSensor CONFIG_OPTION_ACCEL_RATE_LP = new ConfigOptionDetailsSensor (
			SensorLIS2DW12.GuiLabelConfig.LIS2DW12_RATE,
			SensorLIS2DW12.DatabaseConfigHandle.LIS2DW12_RATE,
			LIS2DW12_ACCEL_RATE.getLabelsLp(), 
			LIS2DW12_ACCEL_RATE.getConfigValuesLp(), 
			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoLIS2DW12);

	public static final ConfigOptionDetailsSensor CONFIG_OPTION_ACCEL_RATE_HP = new ConfigOptionDetailsSensor (
			SensorLIS2DW12.GuiLabelConfig.LIS2DW12_RATE,
			SensorLIS2DW12.DatabaseConfigHandle.LIS2DW12_RATE,
			LIS2DW12_ACCEL_RATE.getLabelsHp(), 
			LIS2DW12_ACCEL_RATE.getConfigValuesLp(), 
			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoLIS2DW12);

	public static final ConfigOptionDetailsSensor CONFIG_OPTION_ACCEL_MODE = new ConfigOptionDetailsSensor (
			SensorLIS2DW12.GuiLabelConfig.LIS2DW12_MODE,
			SensorLIS2DW12.DatabaseConfigHandle.LIS2DW12_MODE,
			LIS2DW12_MODE.getLabels(), 
			LIS2DW12_MODE.getConfigValues(), 
			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoLIS2DW12);
	
	public static final ConfigOptionDetailsSensor CONFIG_OPTION_ACCEL_LP_MODE = new ConfigOptionDetailsSensor (
			SensorLIS2DW12.GuiLabelConfig.LIS2DW12_LP_MODE,
			SensorLIS2DW12.DatabaseConfigHandle.LIS2DW12_LP_MODE,
			LIS2DW12_LP_MODE.getLabels(), 
			LIS2DW12_LP_MODE.getConfigValues(), 
			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoLIS2DW12);

	// --------------- Configuration options end ----------------
	
	// ----------------- Calibration Start -----------------------

	public static final double[][] DEFAULT_OFFSET_VECTOR_LIS2DW12 = {{0},{0},{0}};	
	public static final double[][] DEFAULT_ALIGNMENT_MATRIX_LIS2DW12 = {{0,0,1},{1,0,0},{0,1,0}};
	
	public static final double[][] DEFAULT_SENSITIVITY_MATRIX_LIS2DW12_2G = {{1671.665922915,0,0},{0,1671.665922915,0},{0,0,1671.665922915}};
	public static final double[][] DEFAULT_SENSITIVITY_MATRIX_LIS2DW12_4G = {{835.832961457,0,0},{0,835.832961457,0},{0,0,835.832961457}};
	public static final double[][] DEFAULT_SENSITIVITY_MATRIX_LIS2DW12_8G = {{417.916480729,0,0},{0,417.916480729,0},{0,0,417.916480729}};
	public static final double[][] DEFAULT_SENSITIVITY_MATRIX_LIS2DW12_16G = {{208.958240364,0,0},{0,208.958240364,0},{0,0,208.958240364}};

	public CalibDetailsKinematic calibDetailsAccel2g = new CalibDetailsKinematic(
			LIS2DW12_ACCEL_RANGE.RANGE_2G.configValue,
			LIS2DW12_ACCEL_RANGE.RANGE_2G.label,
			DEFAULT_ALIGNMENT_MATRIX_LIS2DW12, 
			DEFAULT_SENSITIVITY_MATRIX_LIS2DW12_2G, 
			DEFAULT_OFFSET_VECTOR_LIS2DW12);
	public CalibDetailsKinematic calibDetailsAccel4g = new CalibDetailsKinematic(
			LIS2DW12_ACCEL_RANGE.RANGE_4G.configValue,
			LIS2DW12_ACCEL_RANGE.RANGE_4G.label,
			DEFAULT_ALIGNMENT_MATRIX_LIS2DW12,
			DEFAULT_SENSITIVITY_MATRIX_LIS2DW12_4G, 
			DEFAULT_OFFSET_VECTOR_LIS2DW12);
	public CalibDetailsKinematic calibDetailsAccel8g = new CalibDetailsKinematic(
			LIS2DW12_ACCEL_RANGE.RANGE_8G.configValue,
			LIS2DW12_ACCEL_RANGE.RANGE_8G.label,
			DEFAULT_ALIGNMENT_MATRIX_LIS2DW12, 
			DEFAULT_SENSITIVITY_MATRIX_LIS2DW12_8G, 
			DEFAULT_OFFSET_VECTOR_LIS2DW12);
	public CalibDetailsKinematic calibDetailsAccel16g = new CalibDetailsKinematic(
			LIS2DW12_ACCEL_RANGE.RANGE_16G.configValue,
			LIS2DW12_ACCEL_RANGE.RANGE_16G.label,
			DEFAULT_ALIGNMENT_MATRIX_LIS2DW12,
			DEFAULT_SENSITIVITY_MATRIX_LIS2DW12_16G, 
			DEFAULT_OFFSET_VECTOR_LIS2DW12);

	public CalibDetailsKinematic mCurrentCalibDetailsAccel = calibDetailsAccel2g;
	
	// ----------------- Calibration end -----------------------

  	//--------- Sensor info start --------------
	public static final SensorDetailsRef SENOSR_LIS2DW12_ACCEL = new SensorDetailsRef(
			Configuration.Verisense.SensorBitmap.LIS2DW12_ACCEL,
			Configuration.Verisense.SensorBitmap.LIS2DW12_ACCEL,
			GuiLabelSensors.ACCEL1,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoLIS2DW12,
			Arrays.asList(Configuration.Verisense.SENSOR_ID.LSM6DS3_ACCEL,
					Configuration.Verisense.SENSOR_ID.LSM6DS3_GYRO),
			Arrays.asList(GuiLabelConfig.LIS2DW12_RANGE,
					GuiLabelConfig.LIS2DW12_RATE),
			Arrays.asList(ObjectClusterSensorName.LIS2DW12_ACC_X,
					ObjectClusterSensorName.LIS2DW12_ACC_Y,
					ObjectClusterSensorName.LIS2DW12_ACC_Z),
			false);

  	public static final Map<Integer, SensorDetailsRef> SENSOR_MAP_REF;
	static {
		Map<Integer, SensorDetailsRef> aMap = new LinkedHashMap<Integer, SensorDetailsRef>();
		aMap.put(Configuration.Verisense.SENSOR_ID.LIS2DW12_ACCEL, SensorLIS2DW12.SENOSR_LIS2DW12_ACCEL);  
		SENSOR_MAP_REF = Collections.unmodifiableMap(aMap);
	}

  	//--------- Sensor info end --------------
	
	//--------- Channel info start --------------
	
	public static final ChannelDetails CHANNEL_LISDW12_ACCEL_X = new ChannelDetails(
			ObjectClusterSensorName.LIS2DW12_ACC_X,
			ObjectClusterSensorName.LIS2DW12_ACC_X,
			DatabaseChannelHandles.LIS2DW12_ACC_X,
			CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.LSB,
			CHANNEL_UNITS.METER_PER_SECOND_SQUARE,
			Arrays.asList(CHANNEL_TYPE.UNCAL, CHANNEL_TYPE.CAL, CHANNEL_TYPE.DERIVED));
	
	public static final ChannelDetails CHANNEL_LISDW12_ACCEL_Y = new ChannelDetails(
			ObjectClusterSensorName.LIS2DW12_ACC_Y,
			ObjectClusterSensorName.LIS2DW12_ACC_Y,
			DatabaseChannelHandles.LIS2DW12_ACC_Y,
			CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.LSB,
			CHANNEL_UNITS.METER_PER_SECOND_SQUARE,
			Arrays.asList(CHANNEL_TYPE.UNCAL, CHANNEL_TYPE.CAL, CHANNEL_TYPE.DERIVED));
	
	public static final ChannelDetails CHANNEL_LISDW12_ACCEL_Z = new ChannelDetails(
			ObjectClusterSensorName.LIS2DW12_ACC_Z,
			ObjectClusterSensorName.LIS2DW12_ACC_Z,
			DatabaseChannelHandles.LIS2DW12_ACC_Z,
			CHANNEL_DATA_TYPE.INT16, 2, CHANNEL_DATA_ENDIAN.LSB,
			CHANNEL_UNITS.METER_PER_SECOND_SQUARE,
			Arrays.asList(CHANNEL_TYPE.UNCAL, CHANNEL_TYPE.CAL, CHANNEL_TYPE.DERIVED));
	 
	public static final Map<String, ChannelDetails> CHANNEL_MAP_REF;
	static {
		Map<String, ChannelDetails> aMap = new LinkedHashMap<String, ChannelDetails>();
		aMap.put(SensorLIS2DW12.ObjectClusterSensorName.LIS2DW12_ACC_X, SensorLIS2DW12.CHANNEL_LISDW12_ACCEL_X);
		aMap.put(SensorLIS2DW12.ObjectClusterSensorName.LIS2DW12_ACC_Y, SensorLIS2DW12.CHANNEL_LISDW12_ACCEL_Y);
		aMap.put(SensorLIS2DW12.ObjectClusterSensorName.LIS2DW12_ACC_Z, SensorLIS2DW12.CHANNEL_LISDW12_ACCEL_Z);

		CHANNEL_MAP_REF = Collections.unmodifiableMap(aMap);
	}

	//--------- Channel info end --------------
	
	
	public SensorLIS2DW12(VerisenseDevice verisenseDevice) {
		super(SENSORS.LIS2DW12, verisenseDevice);
		initialise();
	}

	@Override
	public void generateSensorMap() {
		super.createLocalSensorMapWithCustomParser(SENSOR_MAP_REF, CHANNEL_MAP_REF);
//		super.createLocalSensorMap(mSensorMapRef, mChannelMapRef);
	}

	@Override
	public void generateConfigOptionsMap() {
		mConfigOptionsMap.clear();
		addConfigOption(CONFIG_OPTION_ACCEL_RANGE);
		addConfigOption(CONFIG_OPTION_ACCEL_LP_MODE);
		addConfigOption(CONFIG_OPTION_ACCEL_MODE);
		addConfigOption(CONFIG_OPTION_ACCEL_RATE_HP);
		addConfigOption(CONFIG_OPTION_ACCEL_RATE_LP);
	}

	@Override 
	public void generateSensorGroupMapping() {
		
		int groupIndex = Configuration.Shimmer3.LABEL_SENSOR_TILE.BRIDGE_AMPLIFIER.ordinal();
		
		if(mShimmerVerObject.isShimmerGenVerisense()){
			mSensorGroupingMap.put(groupIndex, new SensorGroupingDetails(
					LABEL_SENSOR_TILE.ACCEL,
					Arrays.asList(
							Configuration.Verisense.SENSOR_ID.LIS2DW12_ACCEL),
					CompatibilityInfoForMaps.listOfCompatibleVersionInfoLIS2DW12));
		}
		super.updateSensorGroupingMap();
	}	

	@Override
	public ObjectCluster processDataCustom(SensorDetails sensorDetails, byte[] rawData, COMMUNICATION_TYPE commType, ObjectCluster objectCluster, boolean isTimeSyncEnabled, double pcTimestampMs) {

		// ADC values
		objectCluster = sensorDetails.processDataCommon(rawData, commType, objectCluster, isTimeSyncEnabled, pcTimestampMs);

		double[] unCalibratedAccel = new double[3];
		unCalibratedAccel[0] = objectCluster.getFormatClusterValue(SensorLIS2DW12.CHANNEL_LISDW12_ACCEL_X, CHANNEL_TYPE.UNCAL);
		unCalibratedAccel[1] = objectCluster.getFormatClusterValue(SensorLIS2DW12.CHANNEL_LISDW12_ACCEL_Y, CHANNEL_TYPE.UNCAL);
		unCalibratedAccel[2] = objectCluster.getFormatClusterValue(SensorLIS2DW12.CHANNEL_LISDW12_ACCEL_Z, CHANNEL_TYPE.UNCAL);
		if(Double.isFinite(unCalibratedAccel[0]) && Double.isFinite(unCalibratedAccel[1]) && Double.isFinite(unCalibratedAccel[2])) {
			//Add default calibrated data to Object cluster
			double[] defaultCalAccel = UtilCalibration.calibrateInertialSensorData(unCalibratedAccel, mCurrentCalibDetailsAccel.getDefaultMatrixMultipliedInverseAMSM(), mCurrentCalibDetailsAccel.getDefaultOffsetVector());
			objectCluster.addCalData(CHANNEL_LISDW12_ACCEL_X, defaultCalAccel[0], objectCluster.getIndexKeeper()-3);
			objectCluster.addCalData(CHANNEL_LISDW12_ACCEL_Y, defaultCalAccel[1], objectCluster.getIndexKeeper()-2);
			objectCluster.addCalData(CHANNEL_LISDW12_ACCEL_Z, defaultCalAccel[2], objectCluster.getIndexKeeper()-1);

			//Add auto-calibrated data to Object cluster - if available
			boolean isCurrentValuesSet = mCurrentCalibDetailsAccel.isCurrentValuesSet();
			if(isCurrentValuesSet) {
				double[] autoCalAccel = UtilCalibration.calibrateImuData(defaultCalAccel, mCurrentCalibDetailsAccel.getCurrentSensitivityMatrix(), mCurrentCalibDetailsAccel.getCurrentOffsetVector());
				objectCluster.addData(CHANNEL_LISDW12_ACCEL_X.mObjectClusterName, CHANNEL_TYPE.DERIVED, CHANNEL_LISDW12_ACCEL_X.mDefaultCalUnits, autoCalAccel[0], objectCluster.getIndexKeeper()-3, false);
				objectCluster.addData(CHANNEL_LISDW12_ACCEL_Y.mObjectClusterName, CHANNEL_TYPE.DERIVED, CHANNEL_LISDW12_ACCEL_Y.mDefaultCalUnits, autoCalAccel[1], objectCluster.getIndexKeeper()-2, false);
				objectCluster.addData(CHANNEL_LISDW12_ACCEL_Z.mObjectClusterName, CHANNEL_TYPE.DERIVED, CHANNEL_LISDW12_ACCEL_Z.mDefaultCalUnits, autoCalAccel[2], objectCluster.getIndexKeeper()-1, false);
			}
		}

		return objectCluster;
	}

	@Override
	public void checkShimmerConfigBeforeConfiguring() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void configBytesGenerate(ShimmerDevice shimmerDevice, byte[] configBytes, COMMUNICATION_TYPE commType) {
		if(isSensorEnabled(Configuration.Verisense.SENSOR_ID.LIS2DW12_ACCEL)) {
			ConfigByteLayoutLis2dw12 configByteLayout = new ConfigByteLayoutLis2dw12(commType);
			
			configBytes[configByteLayout.idxFsAccel1] &= ~(configByteLayout.maskbitFsAccel1<<configByteLayout.bitShiftFsAccel1);
			configBytes[configByteLayout.idxFsAccel1] |= (getAccelRangeConfigValue()&configByteLayout.maskbitFsAccel1)<<configByteLayout.bitShiftFsAccel1;
			
			configBytes[configByteLayout.idxAccel1Cfg0] = 0x00;
			configBytes[configByteLayout.idxAccel1Cfg0] |= (getAccelRateConfigValue()&configByteLayout.maskAccelRate)<<configByteLayout.bitShiftAccelRate;
			configBytes[configByteLayout.idxAccel1Cfg0] |= (getAccelModeConfigValue()&configByteLayout.maskMode)<<configByteLayout.bitShiftMode;
			configBytes[configByteLayout.idxAccel1Cfg0] |= (getAccelLpModeConfigValue()&configByteLayout.maskLpMode)<<configByteLayout.bitShiftLpMode;
			
			if(configByteLayout.idxAccel1Cfg1>=0) {
				// Clear all bits except the range bits as these have already been set above
				configBytes[configByteLayout.idxAccel1Cfg1] &= (configByteLayout.maskbitFsAccel1<<configByteLayout.bitShiftFsAccel1);
				configBytes[configByteLayout.idxAccel1Cfg1] |= (getBwFilt().configValue & configByteLayout.maskBwFilt) << configByteLayout.bitShiftBwFilt;
				configBytes[configByteLayout.idxAccel1Cfg1] |= (getFilteredDataTypeSelection().configValue & configByteLayout.maskFds) << configByteLayout.bitShiftFds;
				configBytes[configByteLayout.idxAccel1Cfg1] |= (getLowNoise().configValue & configByteLayout.maskLowNoise) << configByteLayout.bitShiftLowNoise;
			}
			
			if(configByteLayout.idxAccel1Cfg2>=0) {
				configBytes[configByteLayout.idxAccel1Cfg2] &= ~(configByteLayout.maskHpRefMode<<configByteLayout.bitShiftHpRefMode);
				configBytes[configByteLayout.idxAccel1Cfg2] |= (getHpFilterMode().configValue & configByteLayout.maskHpRefMode) << configByteLayout.bitShiftHpRefMode;
			}
			if(configByteLayout.idxAccel1Cfg3>=0) {
				configBytes[configByteLayout.idxAccel1Cfg3] = 0;
				configBytes[configByteLayout.idxAccel1Cfg3] |= (getFifoMode().configValue & configByteLayout.maskFifoMode) << configByteLayout.bitShiftFifoMode;
				configBytes[configByteLayout.idxAccel1Cfg3] |= (getFifoThreshold().configValue & configByteLayout.maskFifoThreshold) << configByteLayout.bitShiftFifoThreshold;
				
			}
		}
	}

	@Override
	public void configBytesParse(ShimmerDevice shimmerDevice, byte[] configBytes, COMMUNICATION_TYPE commType) {
		if(isSensorEnabled(Configuration.Verisense.SENSOR_ID.LIS2DW12_ACCEL)) {
			ConfigByteLayoutLis2dw12 configByteLayout = new ConfigByteLayoutLis2dw12(commType);
			
			setAccelRangeConfigValue((configBytes[configByteLayout.idxFsAccel1]>>configByteLayout.bitShiftFsAccel1)&configByteLayout.maskbitFsAccel1);
			
			byte accel1Cfg0 = configBytes[configByteLayout.idxAccel1Cfg0];
			setAccelModeConfigValue((accel1Cfg0>>configByteLayout.bitShiftMode)&configByteLayout.maskMode);
			setAccelLpModeConfigValue((accel1Cfg0>>0)&0x03);
			//Need to parse rate after mode
			setAccelRateConfigValue((accel1Cfg0>>configByteLayout.bitShiftAccelRate)&configByteLayout.maskAccelRate);
			
			if(configByteLayout.idxAccel1Cfg1>=0) {
				setBwFilt(LIS2DW12_BW_FILT.getForConfigValue((configBytes[configByteLayout.idxAccel1Cfg1]>>configByteLayout.bitShiftBwFilt) & configByteLayout.maskBwFilt));
				setFds(LIS2DW12_FILTERED_DATA_TYPE_SELECTION.getForConfigValue((configBytes[configByteLayout.idxAccel1Cfg1]>>configByteLayout.bitShiftFds) & configByteLayout.maskFds));
				setLowNoise(LIS2DW12_LOW_NOISE.getForConfigValue((configBytes[configByteLayout.idxAccel1Cfg1]>>configByteLayout.bitShiftLowNoise) & configByteLayout.maskLowNoise));
			}
			if(configByteLayout.idxAccel1Cfg2>=0) {
				setHpFilterMode(LIS2DW12_HP_REF_MODE.getForConfigValue((configBytes[configByteLayout.idxAccel1Cfg2]>>configByteLayout.bitShiftHpRefMode) & configByteLayout.maskHpRefMode));
			}
			if(configByteLayout.idxAccel1Cfg3>=0) {
				setFifoMode(LIS2DW12_FIFO_MODE.getForConfigValue((configBytes[configByteLayout.idxAccel1Cfg3]>>configByteLayout.bitShiftFifoMode) & configByteLayout.maskFifoMode));
				setFifoThreshold(LIS2DW12_FIFO_THRESHOLD.getForConfigValue((configBytes[configByteLayout.idxAccel1Cfg3]>>configByteLayout.bitShiftFifoThreshold) & configByteLayout.maskFifoThreshold));
			}
		}
	}

	@Override
	public Object setConfigValueUsingConfigLabel(Integer sensorId, String configLabel, Object valueToSet) {
		Object returnValue = null;
		
		switch(configLabel){
			case(GuiLabelConfig.LIS2DW12_RATE):
				setAccelRateConfigValue((int)valueToSet);
				break;
			case(GuiLabelConfig.LIS2DW12_RANGE):
				setAccelRangeConfigValue((int)valueToSet);
				break;
			case(GuiLabelConfig.LIS2DW12_MODE):
				setAccelModeConfigValue((int)valueToSet);
				break;
			case(GuiLabelConfig.LIS2DW12_LP_MODE):
				setAccelLpModeConfigValue((int)valueToSet);
				break;
			default:
				returnValue = super.setConfigValueUsingConfigLabelCommon(sensorId, configLabel, valueToSet);
				break;
		}	
		return returnValue;
	}

	@Override
	public Object getConfigValueUsingConfigLabel(Integer sensorId, String configLabel) {
		Object returnValue = null;

		switch(configLabel){
			case(GuiLabelConfig.LIS2DW12_RATE):
				returnValue = getAccelRateConfigValue();
				break;
			case(GuiLabelConfig.LIS2DW12_RANGE):
				returnValue = getAccelRangeConfigValue();
				break;
			case(GuiLabelConfig.LIS2DW12_MODE):
				returnValue = getAccelModeConfigValue();
				break;
			case(GuiLabelConfig.LIS2DW12_LP_MODE):
				returnValue = getAccelLpModeConfigValue();
				break;
			case(GuiLabelConfigCommon.CALIBRATION_CURRENT_PER_SENSOR):
				if(sensorId==Configuration.Verisense.SENSOR_ID.LIS2DW12_ACCEL) {
					returnValue = mCurrentCalibDetailsAccel;
				}
				break;
			case(GuiLabelConfigCommon.RATE):
				returnValue = getAccelRateFreq();
				break;
			default:
				returnValue = super.getConfigValueUsingConfigLabelCommon(sensorId, configLabel);
				break;
		}
		return returnValue;
	}

	@Override
	public void setSensorSamplingRate(double samplingRateHz) {
		int accelRate = 0; // Power down
		
		int accelMode = getAccelModeConfigValue();
		
		if (samplingRateHz==0){
			accelRate = 0;
		} else if (samplingRateHz<=1.6 && accelMode==0){ // LP mode
			accelRate = 1; // 1.6Hz
		} else if (samplingRateHz<=12.5){
			accelRate = 2; // 12.5Hz
		} else if (samplingRateHz<=25){
			accelRate = 3; // 25Hz
		} else if (samplingRateHz<=50){
			accelRate = 4; // 50Hz
		} else if (samplingRateHz<=100){
			accelRate = 5; // 100Hz
		} else if (samplingRateHz<=200 || accelMode==0){ // LP mode cut-off
			accelRate = 6; // 200Hz
		} else if (samplingRateHz<=400){ //HP mode
			accelRate = 7; // 400Hz
		} else if (samplingRateHz<=800){ //HP mode
			accelRate = 8; // 800Hz
		} else { //if (freq<=1600){ //HP mode
			accelRate = 9; // 1600Hz
		}
		setAccelRateConfigValue(accelRate);
	}

	@Override
	public boolean setDefaultConfigForSensor(int sensorId, boolean isSensorEnabled) {
		if(mSensorMap.containsKey(sensorId)){
			//TODO handle isSensorEnabled = true and false

			setAccelRange(LIS2DW12_ACCEL_RANGE.RANGE_4G);
			setAccelRate(LIS2DW12_ACCEL_RATE.LOW_POWER_25_0_HZ);
			setAccelMode(LIS2DW12_MODE.HIGH_PERFORMANCE);
			setAccelLpMode(LIS2DW12_LP_MODE.LOW_POWER1_12BIT_4_5_MG_NOISE);
			setBwFilt(LIS2DW12_BW_FILT.ODR_DIVIDED_BY_2);
			setFds(LIS2DW12_FILTERED_DATA_TYPE_SELECTION.LOW_PASS_FILTER_PATH_SELECTED);
			setLowNoise(LIS2DW12_LOW_NOISE.DISABLED);
			setHpFilterMode(LIS2DW12_HP_REF_MODE.DISABLED);
			setFifoMode(LIS2DW12_FIFO_MODE.CONTINUOUS_TO_FIFO_MODE);
			setFifoThreshold(LIS2DW12_FIFO_THRESHOLD.SAMPLE_31);

			return true;
		}
		return false;
	}

	@Override
	public boolean checkConfigOptionValues(String stringKey) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Object getSettings(String componentName, COMMUNICATION_TYPE commType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ActionSetting setSettings(String componentName, Object valueToSet, COMMUNICATION_TYPE commType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean processResponse(int responseCommand, Object parsedResponse, COMMUNICATION_TYPE commType) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public LinkedHashMap<String, Object> generateConfigMap() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void parseConfigMap(LinkedHashMap<String, Object> mapOfConfigPerShimmer) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void initialise() {
		super.initialise();
		
		updateCurrentAccelCalibInUse();
	}

	@Override
	public void generateCalibMap() {
		super.generateCalibMap();
		
		TreeMap<Integer, CalibDetails> calibMapAccel = new TreeMap<Integer, CalibDetails>();
		calibMapAccel.put(calibDetailsAccel2g.mRangeValue, calibDetailsAccel2g);
		calibMapAccel.put(calibDetailsAccel4g.mRangeValue, calibDetailsAccel4g);
		calibMapAccel.put(calibDetailsAccel8g.mRangeValue, calibDetailsAccel8g);
		calibMapAccel.put(calibDetailsAccel16g.mRangeValue, calibDetailsAccel16g);
		setCalibrationMapPerSensor(Configuration.Verisense.SENSOR_ID.LIS2DW12_ACCEL, calibMapAccel);

		updateCurrentAccelCalibInUse();
	}

	public void updateCurrentAccelCalibInUse(){
		mCurrentCalibDetailsAccel = getCurrentCalibDetailsIfKinematic(Configuration.Verisense.SENSOR_ID.LIS2DW12_ACCEL, getAccelRangeConfigValue());
	}

	public void setAccelRate(LIS2DW12_ACCEL_RATE lis2dw12AccelRate) {
		rate = lis2dw12AccelRate;
		setAccelMode(lis2dw12AccelRate.mode);
	}
	
	public LIS2DW12_ACCEL_RATE getAccelRate() {
		return rate;
	}

	public int getAccelRangeConfigValue() {
		return range.configValue;
	}

	public void setAccelRangeConfigValue(int valueToSet){
		setAccelRange(LIS2DW12_ACCEL_RANGE.getForConfigValue(valueToSet));
	}

	public void setAccelRange(LIS2DW12_ACCEL_RANGE lis2dw12AccelRange){
		range = lis2dw12AccelRange;
		updateCurrentAccelCalibInUse();
	}

	public LIS2DW12_ACCEL_RANGE getAccelRange(){
		return range;
	}

	public int getAccelRateConfigValue() {
		return getAccelRate().configValue;
	}

	public double getAccelRateFreq() {
		return getAccelRate().freqHz;
	}

	public void setAccelRateConfigValue(int valueToSet) {
		setAccelRate(LIS2DW12_ACCEL_RATE.getForConfigValue(valueToSet, mode));
	}

	public int getAccelModeConfigValue() {
		return mode.configValue;
	}

	public void setAccelModeConfigValue(int valueToSet) {
		setAccelMode(LIS2DW12_MODE.getForConfigValue(valueToSet));
	}

	public void setAccelMode(LIS2DW12_MODE valueToSet) {
		mode = valueToSet;
	}
	
	public LIS2DW12_MODE getAccelMode() {
		return mode;
	}

	public int getAccelLpModeConfigValue() {
		return lpMode.configValue;
	}

	public void setAccelLpModeConfigValue(int valueToSet) {
		setAccelLpMode(LIS2DW12_LP_MODE.getForConfigValue(valueToSet));
	}

	public void setAccelLpMode(LIS2DW12_LP_MODE valueToSet) {
		lpMode = valueToSet;
	}

	public LIS2DW12_LP_MODE getAccelLpMode() {
		return lpMode;
	}

	public static double calibrateTemperature(long temperatureUncal) {
		double temp = ((temperatureUncal>>4)/16.0) + 25.0;
		return temp;
	}

	public String getAccelLpModeString() {
		if(mode==LIS2DW12_MODE.LOW_POWER) {
			String accelLpModeStr = SensorLIS2DW12.CONFIG_OPTION_ACCEL_LP_MODE.getConfigStringFromConfigValue(getAccelLpModeConfigValue());
			return accelLpModeStr;
		} else {
			return UtilVerisenseDriver.FEATURE_NOT_AVAILABLE;
		}
	}

	public String getAccelModeString() {
		return SensorLIS2DW12.CONFIG_OPTION_ACCEL_MODE.getConfigStringFromConfigValue(getAccelModeConfigValue());
	}

	public String getAccelRangeString() {
		String accel1Range = SensorLIS2DW12.CONFIG_OPTION_ACCEL_RANGE.getConfigStringFromConfigValue(getAccelRangeConfigValue());
		accel1Range = accel1Range.replaceAll(UtilShimmer.UNICODE_PLUS_MINUS, "+-");
		if(mShimmerDevice instanceof VerisenseDevice && !((VerisenseDevice)mShimmerDevice).isCsvHeaderDesignAzMarkingPoint()) {
			accel1Range = accel1Range.replaceAll(CHANNEL_UNITS.GRAVITY, (" " + CHANNEL_UNITS.GRAVITY));
		}
		return accel1Range;
	}

	public String getAccelModeMergedString() {
		if(mode==LIS2DW12_MODE.HIGH_PERFORMANCE) {
			// High-performance Mode
			return LIS2DW12_MODE_MERGED[0];
		} else {
			// Low-Power Mode X
			return LIS2DW12_MODE_MERGED[lpMode.configValue+1];
		}
	}

	public String getAccelResolutionString() {
		if(mode==LIS2DW12_MODE.LOW_POWER && lpMode==LIS2DW12_LP_MODE.LOW_POWER1_12BIT_4_5_MG_NOISE) {
			// 12-bit
			return LIS2DW12_RESOLUTION[0];
		} else {
			// 14-bit
			return LIS2DW12_RESOLUTION[1];
		}
	}

	public LIS2DW12_BW_FILT getBwFilt() {
		return bwFilt;
	}

	public void setBwFilt(LIS2DW12_BW_FILT bwFilt) {
		this.bwFilt = bwFilt;
	}

	public LIS2DW12_FILTERED_DATA_TYPE_SELECTION getFilteredDataTypeSelection() {
		return fds;
	}

	public void setFds(LIS2DW12_FILTERED_DATA_TYPE_SELECTION lis2dw12_FILTERED_DATA_TYPE_SELECTION) {
		this.fds = lis2dw12_FILTERED_DATA_TYPE_SELECTION;
	}

	public LIS2DW12_LOW_NOISE getLowNoise() {
		return lowNoise;
	}

	public void setLowNoise(LIS2DW12_LOW_NOISE lis2dw12LowNoise) {
		this.lowNoise = lis2dw12LowNoise;
	}

	public LIS2DW12_HP_REF_MODE getHpFilterMode() {
		return hpFilterMode;
	}

	public void setHpFilterMode(LIS2DW12_HP_REF_MODE hpFilterMode) {
		this.hpFilterMode = hpFilterMode;
	}
	
	public LIS2DW12_FIFO_MODE getFifoMode() {
		return fifoMode;
	}

	public void setFifoMode(LIS2DW12_FIFO_MODE fMode) {
		this.fifoMode = fMode;
	}

	public LIS2DW12_FIFO_THRESHOLD getFifoThreshold() {
		return fifoThreshold;
	}

	public void setFifoThreshold(LIS2DW12_FIFO_THRESHOLD fifoThreshold) {
		this.fifoThreshold = fifoThreshold;
	}

	private class ConfigByteLayoutLis2dw12 {
		public int idxAccel1Cfg0 = -1, idxAccel1Cfg1 = -1, idxAccel1Cfg2 = -1, idxAccel1Cfg3 = -1, idxFsAccel1 = -1; 
		public int maskbitFsAccel1 = 0x03, bitShiftFsAccel1 = 0;
		public int maskAccelRate = 0x0F, bitShiftAccelRate = 4;
		public int maskMode = 0x03, bitShiftMode = 2;
		public int maskLpMode = 0x03, bitShiftLpMode = 0;
		public int maskBwFilt = 0x03, bitShiftBwFilt = 6;
		public int maskFds = 0x01, bitShiftFds = 3;
		public int maskHpRefMode = 0x01, bitShiftHpRefMode = 1;
		public int maskLowNoise = 0x01, bitShiftLowNoise = 2;
		public int maskFifoMode = 0x07, bitShiftFifoMode = 5;
		public int maskFifoThreshold = 0x1F, bitShiftFifoThreshold = 0;
		
		public ConfigByteLayoutLis2dw12(COMMUNICATION_TYPE commType) {
			if(commType==COMMUNICATION_TYPE.SD) {
				idxFsAccel1 = PAYLOAD_CONFIG_BYTE_INDEX.PAYLOAD_CONFIG0;
				bitShiftFsAccel1 = 2;
				idxAccel1Cfg0 = PAYLOAD_CONFIG_BYTE_INDEX.PAYLOAD_CONFIG1;
			} else {
				idxAccel1Cfg0 = OP_CONFIG_BYTE_INDEX.ACCEL1_CFG_0;
				idxAccel1Cfg1 = OP_CONFIG_BYTE_INDEX.ACCEL1_CFG_1;
				idxAccel1Cfg2 = OP_CONFIG_BYTE_INDEX.ACCEL1_CFG_2;
				idxAccel1Cfg3 = OP_CONFIG_BYTE_INDEX.ACCEL1_CFG_3;

				idxFsAccel1 = OP_CONFIG_BYTE_INDEX.ACCEL1_CFG_1;
				bitShiftFsAccel1 = 4;
			}
		}
	}

	@Override
	public void setSensorConfig(ISensorConfig sensorConfig) {
		if(sensorConfig instanceof LIS2DW12_ACCEL_RATE) {
			setAccelRate((LIS2DW12_ACCEL_RATE)sensorConfig);
		} else if(sensorConfig instanceof LIS2DW12_LP_MODE) {
			setAccelLpMode((LIS2DW12_LP_MODE)sensorConfig);
		} else if (sensorConfig instanceof LIS2DW12_ACCEL_RANGE) {
			setAccelRange((LIS2DW12_ACCEL_RANGE)sensorConfig);
		} else if (sensorConfig instanceof LIS2DW12_FILTERED_DATA_TYPE_SELECTION) {
			setFds((LIS2DW12_FILTERED_DATA_TYPE_SELECTION)sensorConfig);
		} else if (sensorConfig instanceof LIS2DW12_LOW_NOISE) {
			setLowNoise((LIS2DW12_LOW_NOISE)sensorConfig);
		} else if (sensorConfig instanceof LIS2DW12_MODE) {
			setAccelMode((LIS2DW12_MODE)sensorConfig);
		} else if (sensorConfig instanceof LIS2DW12_ACCEL_RATE) {
			setAccelRate((LIS2DW12_ACCEL_RATE)sensorConfig);
		} else if (sensorConfig instanceof LIS2DW12_BW_FILT) {
			setBwFilt((LIS2DW12_BW_FILT)sensorConfig);
		} else if (sensorConfig instanceof LIS2DW12_HP_REF_MODE) {
			setHpFilterMode((LIS2DW12_HP_REF_MODE)sensorConfig);
		} else if (sensorConfig instanceof LIS2DW12_FIFO_MODE) {
			setFifoMode((LIS2DW12_FIFO_MODE)sensorConfig);
		} else if (sensorConfig instanceof LIS2DW12_FIFO_THRESHOLD) {
			setFifoThreshold((LIS2DW12_FIFO_THRESHOLD)sensorConfig);
		} else {
			super.setSensorConfig(sensorConfig);
		}
	}

	@Override
	public List<ISensorConfig> getSensorConfig() {
		List<ISensorConfig> listOfSensorConfig = super.getSensorConfig();
		listOfSensorConfig.add(getFilteredDataTypeSelection());
		listOfSensorConfig.add(getAccelLpMode());
		listOfSensorConfig.add(getAccelMode());
		listOfSensorConfig.add(getAccelRange());
		listOfSensorConfig.add(getAccelRate());
		listOfSensorConfig.add(getBwFilt());
		listOfSensorConfig.add(getLowNoise());
		listOfSensorConfig.add(getHpFilterMode());
		listOfSensorConfig.add(getFifoMode());
		listOfSensorConfig.add(getFifoThreshold());
		return listOfSensorConfig;
	}

}
