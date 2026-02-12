package com.shimmerresearch.tools;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.shimmerresearch.driver.Configuration.Shimmer3.DerivedSensorsBitMask;

public class SensorParser {
    private static final Map<Integer, String> SENSOR_MAP = new LinkedHashMap<>();

    static {
        SENSOR_MAP.put(DerivedSensorsBitMask.RES_AMP, "RES_AMP");
        SENSOR_MAP.put(DerivedSensorsBitMask.SKIN_TEMP, "SKIN_TEMP");
        SENSOR_MAP.put(DerivedSensorsBitMask.PPG_12_13, "PPG_12_13");
        SENSOR_MAP.put(DerivedSensorsBitMask.PPG1_12_13, "PPG1_12_13");
        SENSOR_MAP.put(DerivedSensorsBitMask.PPG2_1_14, "PPG2_1_14");
        SENSOR_MAP.put(DerivedSensorsBitMask.PPG_TO_HR_12_13, "PPG_TO_HR_12_13");
        SENSOR_MAP.put(DerivedSensorsBitMask.PPG_TO_HR1_12_13, "PPG_TO_HR1_12_13");
        SENSOR_MAP.put(DerivedSensorsBitMask.PPG_TO_HR2_1_14, "PPG_TO_HR2_1_14");
        SENSOR_MAP.put(DerivedSensorsBitMask.ACTIVITY_MODULE, "ACTIVITY_MODULE");
        SENSOR_MAP.put(DerivedSensorsBitMask.GSR_METRICS_GENERAL, "GSR_METRICS_GENERAL");
        SENSOR_MAP.put(DerivedSensorsBitMask.ECG2HR_HRV_FREQ_DOMAIN, "ECG2HR_HRV_FREQ_DOMAIN");
        SENSOR_MAP.put(DerivedSensorsBitMask.ECG2HR_HRV_TIME_DOMAIN, "ECG2HR_HRV_TIME_DOMAIN");
        SENSOR_MAP.put(DerivedSensorsBitMask.ECG2HR_CHIP2_CH2, "ECG2HR_CHIP2_CH2");
        SENSOR_MAP.put(DerivedSensorsBitMask.ECG2HR_CHIP2_CH1, "ECG2HR_CHIP2_CH1");
        SENSOR_MAP.put(DerivedSensorsBitMask.ECG2HR_CHIP1_CH2, "ECG2HR_CHIP1_CH2");
        SENSOR_MAP.put(DerivedSensorsBitMask.ECG2HR_CHIP1_CH1, "ECG2HR_CHIP1_CH1");
        SENSOR_MAP.put(DerivedSensorsBitMask.ORIENTATION_9DOF_WR_QUAT, "ORIENTATION_9DOF_WR_QUAT");
        SENSOR_MAP.put(DerivedSensorsBitMask.ORIENTATION_9DOF_WR_EULER, "ORIENTATION_9DOF_WR_EULER");
        SENSOR_MAP.put(DerivedSensorsBitMask.ORIENTATION_6DOF_WR_QUAT, "ORIENTATION_6DOF_WR_QUAT");
        SENSOR_MAP.put(DerivedSensorsBitMask.ORIENTATION_6DOF_WR_EULER, "ORIENTATION_6DOF_WR_EULER");
        SENSOR_MAP.put(DerivedSensorsBitMask.ORIENTATION_9DOF_LN_QUAT, "ORIENTATION_9DOF_LN_QUAT");
        SENSOR_MAP.put(DerivedSensorsBitMask.ORIENTATION_9DOF_LN_EULER, "ORIENTATION_9DOF_LN_EULER");
        SENSOR_MAP.put(DerivedSensorsBitMask.ORIENTATION_6DOF_LN_QUAT, "ORIENTATION_6DOF_LN_QUAT");
        SENSOR_MAP.put(DerivedSensorsBitMask.ORIENTATION_6DOF_LN_EULER, "ORIENTATION_6DOF_LN_EULER");
        SENSOR_MAP.put(DerivedSensorsBitMask.EMG_PROCESSING_CHAN2, "EMG_PROCESSING_CHAN2");
        SENSOR_MAP.put(DerivedSensorsBitMask.EMG_PROCESSING_CHAN1, "EMG_PROCESSING_CHAN1");
        SENSOR_MAP.put(DerivedSensorsBitMask.GSR_BASELINE, "GSR_BASELINE");
        SENSOR_MAP.put(DerivedSensorsBitMask.GSR_METRICS_TREND_PEAK, "GSR_METRICS_TREND_PEAK");
        SENSOR_MAP.put(DerivedSensorsBitMask.GAIT_MODULE, "GAIT_MODULE");
        SENSOR_MAP.put(DerivedSensorsBitMask.GYRO_ON_THE_FLY_CAL, "GYRO_ON_THE_FLY_CAL");
    }

    /**
     * Parses the hex string and returns a list of enabled sensor names.
     * @param hexInput String format "hx:0000000020440000"
     */
    public static List<String> parseHex(String hexInput) {
        String cleanHex = hexInput.replace("hx:", "");
        // Using Long because the input is 64-bit, even though constants are 32-bit
        long bitmask = Long.parseUnsignedLong(cleanHex, 16);
        
        List<String> enabledSensors = new ArrayList<>();
        
        for (Map.Entry<Integer, String> entry : SENSOR_MAP.entrySet()) {
            // Check if the specific bit is set
            if ((bitmask & entry.getKey()) != 0) {
                enabledSensors.add(entry.getValue());
            }
        }
        return enabledSensors;
    }

    public static void main(String[] args) {
    	// String containing multiple CSV hex values
        String csvInput = "hx:0000000000000000,"
        		+ "hx:0000000000000004,"
        		+ "hx:0000000020440000,"
        		+ "hx:0000000020000000,"
        		+ "hx:0000000020000024,"
        		+ "hx:0000000000000024,"
        		+ "hx:000000000000F000,"
        		+ "hx:0000000000008000,"
        		+ "hx:0000000020000004,"
        		+ "hx:0000000008000224,"
        		+ "hx:0000000001000000,"
        		+ "hx:0000000020110000";

        // Split by comma
        String[] hexValues = csvInput.split(",");

        System.out.println(String.format("%-25s | %s", "Hex Value", "Enabled Sensors"));
        System.out.println("------------------------------------------------------------");

        for (String hex : hexValues) {
            List<String> results = parseHex(hex);
            String sensors = results.isEmpty() ? "None" : String.join(", ", results);
            
            System.out.println(String.format("%-25s | %s", hex.trim(), sensors));
        }
    }
}