package com.shimmerresearch.tools;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class EnabledSensorParser {

    private static final Map<Integer, String> SENSOR_MAP = new LinkedHashMap<>();

    static {
        // Motion
        SENSOR_MAP.put(0x80, "SENSOR_ACCEL");
        SENSOR_MAP.put(0x1000, "SENSOR_DACCEL");
        SENSOR_MAP.put(0x40, "SENSOR_GYRO");
        SENSOR_MAP.put(0x20, "SENSOR_MAG");
        SENSOR_MAP.put(0x400000, "SENSOR_ALT_ACCEL");
        SENSOR_MAP.put(0x200000, "SENSOR_ALT_MAG");

        // Biopotential / EXG
        SENSOR_MAP.put(0x10, "SENSOR_ECG / EXG1_24BIT");
        SENSOR_MAP.put(0x08, "SENSOR_EMG / EXG2_24BIT");
        SENSOR_MAP.put(0x100000, "SENSOR_EXG1_16BIT");
        SENSOR_MAP.put(0x080000, "SENSOR_EXG2_16BIT");

        // Analog / ADC
        SENSOR_MAP.put(0x02, "SENSOR_EXT_ADC_A7");
        SENSOR_MAP.put(0x01, "SENSOR_EXT_ADC_A6");
        SENSOR_MAP.put(0x0800, "SENSOR_EXT_ADC_A15");
        SENSOR_MAP.put(0x0400, "SENSOR_INT_ADC_A1");
        SENSOR_MAP.put(0x0200, "SENSOR_INT_ADC_A12");
        SENSOR_MAP.put(0x0100, "SENSOR_INT_ADC_A13");
        SENSOR_MAP.put(0x800000, "SENSOR_INT_ADC_A14");

        // Other
        SENSOR_MAP.put(0x04, "SENSOR_GSR");
        SENSOR_MAP.put(0x40000, "SENSOR_BMPX80");
        SENSOR_MAP.put(0x8000, "SENSOR_BRIDGE_AMP");
        SENSOR_MAP.put(0x4000, "SENSOR_HEART");
        SENSOR_MAP.put(0x2000, "SENSOR_BATT");
    }

    public static List<String> parseHex(String hexInput) {
        String cleanHex = hexInput.replace("hx:", "");
        long bitmask = Long.parseUnsignedLong(cleanHex, 16);

        List<String> enabledSensors = new ArrayList<>();

        for (Map.Entry<Integer, String> entry : SENSOR_MAP.entrySet()) {
            if ((bitmask & entry.getKey()) != 0) {
                enabledSensors.add(entry.getValue());
            }
        }
        return enabledSensors;
    }

    public static void main(String[] args) {

        String csvInput ="hx:0000000000000104,"
        		+ "hx:0000000000000004,"
        		+ "hx:00000000000031E4,"
        		+ "hx:00000000000439E7,"
        		+ "hx:00000000000010E0,"
        		+ "hx:0000000000000010,"
        		+ "hx:0000000000001104,"
        		+ "hx:0000000000000018,"
        		+ "hx:0000000000000098,"
        		+ "hx:00000000000020E0";

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
