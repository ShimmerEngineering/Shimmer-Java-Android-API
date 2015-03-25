/*Rev 2.6
 * 
 * 
 *  Copyright (c) 2010, Shimmer Research, Ltd.
 * All rights reserved
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:

 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above
 *       copyright notice, this list of conditions and the following
 *       disclaimer in the documentation and/or other materials provided
 *       with the distribution.
 *     * Neither the name of Shimmer Research, Ltd. nor the names of its
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.

 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * @author Jong Chern Lim, Ruaidhri Molloy
 * @date   May, 2014
 * 
 * The purpose of this code is to maintain the configurations of BTSTREAM
 * 
 * 
 * Changes since 2.5 (RM first revision)
 * - Addition of Strain Gauge for Shimmer3
 * 
 * Changes since 2.2
 * - Changed list of compatible sensors to public
 * 
 */


package com.shimmerresearch.driver;

public class Configuration {
	//Channel Contents
	public static class Shimmer3{
		public class Channel{
			public final static int XAAccel     			 = 0x00;
			public final static int YAAccel    				 = 0x01;
			public final static int ZAAccel     			 = 0x02;
			public final static int VBatt       			 = 0x03;
			public final static int XDAccel     			 = 0x04;
			public final static int YDAccel     			 = 0x05;
			public final static int ZDAccel     			 = 0x06;
			public final static int XMag        			 = 0x07;
			public final static int YMag        			 = 0x08;
			public final static int ZMag        			 = 0x09;
			public final static int XGyro       			 = 0x0A;
			public final static int YGyro       			 = 0x0B;
			public final static int ZGyro       			 = 0x0C;
			public final static int ExtAdc7					 = 0x0D;
			public final static int ExtAdc6					 = 0x0E;
			public final static int ExtAdc15 				 = 0x0F;
			public final static int IntAdc1					 = 0x10;
			public final static int IntAdc12 				 = 0x11;
			public final static int IntAdc13 				 = 0x12;
			public final static int IntAdc14 				 = 0x13;
			public final static int XAlterAccel      		 = 0x14; //Alternative Accelerometer
			public final static int YAlterAccel     		 = 0x15;
			public final static int ZAlterAccel     		 = 0x16;
			public final static int XAlterMag        		 = 0x17; //Alternative Magnetometer
			public final static int YAlterMag        		 = 0x18;
			public final static int ZAlterMag        		 = 0x19;
			public final static int Temperature 			 = 0x1A;
			public final static int Pressure 				 = 0x1B;
			public final static int GsrRaw 					 = 0x1C;
			public final static int EXG_ADS1292R_1_STATUS 	 = 0x1D;
			public final static int EXG_ADS1292R_1_CH1_24BIT = 0x1E;
			public final static int EXG_ADS1292R_1_CH2_24BIT = 0x1F;
			public final static int EXG_ADS1292R_2_STATUS 	 = 0x20;
			public final static int EXG_ADS1292R_2_CH1_24BIT = 0x21;
			public final static int EXG_ADS1292R_2_CH2_24BIT = 0x22;
			public final static int EXG_ADS1292R_1_CH1_16BIT = 0x23;
			public final static int EXG_ADS1292R_1_CH2_16BIT = 0x24;
			public final static int EXG_ADS1292R_2_CH1_16BIT = 0x25;
			public final static int EXG_ADS1292R_2_CH2_16BIT = 0x26;
			public final static int BridgeAmpHigh  			 = 0x27;
			public final static int BridgeAmpLow   			 = 0x28;
		}

		public class SensorBitmap{
			//Sensor Bitmap for Shimmer 3
			public static final int SENSOR_A_ACCEL			   = 0x80;
			public static final int SENSOR_GYRO			   	   = 0x40;
			public static final int SENSOR_MAG				   = 0x20;
			public static final int SENSOR_EXG1_24BIT			   = 0x10;
			public static final int SENSOR_EXG2_24BIT			   = 0x08;
			public static final int SENSOR_GSR					   = 0x04;
			public static final int SENSOR_EXT_A7				   = 0x02;
			public static final int SENSOR_EXT_A6				   = 0x01;
			public static final int SENSOR_VBATT				   = 0x2000;
			public static final int SENSOR_D_ACCEL			   = 0x1000;
			public static final int SENSOR_EXT_A15				   = 0x0800;
			public static final int SENSOR_INT_A1				   = 0x0400;
			public static final int SENSOR_INT_A12				   = 0x0200;
			public static final int SENSOR_INT_A13				   = 0x0100;
			public static final int SENSOR_INT_A14				   = 0x800000;
			public static final int SENSOR_BMP180				   = 0x40000;
			public static final int SENSOR_EXG1_16BIT			   = 0x100000;
			public static final int SENSOR_EXG2_16BIT			   = 0x080000;
			public static final int SENSOR_BRIDGE_AMP			   = 0x8000;
		}

		public final static String[] ListofCompatibleSensors={"Low Noise Accelerometer","Wide Range Accelerometer","Gyroscope","Magnetometer","Battery Voltage","External ADC A7","External ADC A6","External ADC A15","Internal ADC A1","Internal ADC A12","Internal ADC A13","Internal ADC A14","Pressure","GSR","EXG1","EXG2","EXG1 16Bit","EXG2 16Bit", "Bridge Amplifier"}; 
		public final static String[] ListofAccelRange={"+/- 2g","+/- 4g","+/- 8g","+/- 16g"};
		public final static String[] ListofGyroRange={"+/- 250 dps","+/- 500 dps","+/- 1000 dps","+/- 2000 dps"}; 
		public final static String[] ListofMagRange={"+/- 1.3 Ga","+/- 1.9 Ga","+/- 2.5 Ga","+/- 4.0 Ga","+/- 4.7 Ga","+/- 5.6 Ga","+/- 8.1 Ga"}; 
		public final static String[] ListofPressureResolution={"Low","Standard","High","Very High"};
		public final static String[] ListofGSRRange={"10k\u2126 to 56k\u2126","56k\u2126 to 220k\u2126","220k\u2126 to 680k\u2126","680k\u2126 to 4.7M\u2126","Auto"};
		public final static String[] ListofDefaultEXG={"ECG","EMG","Test Signal"};
		public final static String[] ListOfExGGain={"6","1","2","3","4","8","12"};
		public final static String[] ListOfECGReferenceElectrode={"Inverse Wilson CT","Fixed Potential"};
		public final static String[] ListOfEMGReferenceElectrode={"Fixed Potential", "Inverse of Ch1"};
		public final static String[] ListOfExGLeadOffDetection={"Off","DC Current"};
		public final static String[] ListOfExGLeadOffCurrent={"6 nA","22 nA", "6 uA", "22 uA"};
		public final static String[] ListOfExGLeadOffComparator={"Pos:95%-Neg:5%","Pos:92.5%-Neg:7.5%","Pos:90%-Neg:10%","Pos:87.5%-Neg:12.5%","Pos:85%-Neg:15%","Pos:80%-Neg:20%","Pos:75%-Neg:25%","Pos:70%-Neg:30%"};
		public final static String[] ListofMPU9150AccelRange={"+/- 2g","+/- 4g","+/- 8g","+/- 16g"};
		public final static String[] ListofBluetoothBaudRates={"115200","1200","2400","4800","9600","19200","38400","57600","230400","460800","921600"};


		//TODO: From here onwards is is Mark TESTING - not finished
		//TODO: check all indexes below
		public final static Integer[] ListofBluetoothBaudRatesConfigValues={0,1,2,3,4,5,6,7,8,9,10};

		public final static String[] ListofMPU9150MplCalibrationOptions={"No Cal","Fast Cal","1s no motion","2s no motion","5s no motion","10s no motion","30s no motion","60s no motion"};
		public final static String[] ListofMPU9150MplLpfOptions={"No LPF","188Hz","98Hz","42Hz","20Hz","10Hz","5Hz"};

		//		public final static String[] ListofLSM303DLHCAccelRate={"Power-down","1Hz","10Hz","25Hz","50Hz","100Hz","200Hz","400Hz","1620Hz","1344Hz","5376Hz"}; // 1620Hz and 5376Hz are only available in low-power mode, 1344Hz only available in full power mode
		//		public final static Integer[] ListofLSM303DLHCAccelRateConfigValues={0,1,2,3,4,5,6,7,8,9,9};
		public final static String[] ListofLSM303DLHCAccelRate={"Power-down","1Hz","10Hz","25Hz","50Hz","100Hz","200Hz","400Hz","1344Hz"};
		public final static Integer[] ListofLSM303DLHCAccelRateConfigValues={0,1,2,3,4,5,6,7,9};
		public final static String[] ListofLSM303DLHCAccelRateLpm={"Power-down","1Hz","10Hz","25Hz","50Hz","100Hz","200Hz","400Hz","1620Hz","5376Hz"}; // 1620Hz and 5376Hz are only available in low-power mode
		public final static Integer[] ListofLSM303DLHCAccelRateLpmConfigValues={0,1,2,3,4,5,6,7,8,9};

		public final static String[] ListofLSM303DLHCMagRate={"0.75Hz","1.5Hz","3Hz","7.5Hz","15Hz","30Hz","75Hz","220Hz"};
		public final static Integer[] ListofLSM303DLHCMagRateConfigValues={0,1,2,3,4,5,6,7};
		public final static String[] ListofMPU9150MplRate={"10Hz","20Hz","40Hz","50Hz","100Hz"};
		public final static Integer[] ListofMPU9150MplRateConfigValues={0,1,2,3,4};
		public final static String[] ListofMPU9150MagRate={"10Hz","20Hz","40Hz","50Hz","100Hz"};
		public final static Integer[] ListofMPU9150MagRateConfigValues={0,1,2,3,4};

		public final static Integer[] ListofLSM303DLHCAccelRangeConfigValues={0,1,2,3};
		public final static Integer[] ListofMPU9150GyroRangeConfigValues={0,1,2,3};

		public final static Integer[] ListofPressureResolutionConfigValues={0,1,2,3};
		public final static Integer[] ListofGSRRangeConfigValues={0,1,2,3,4};
		public final static Integer[] ListofMagRangeConfigValues={1,2,3,4,5,6,7}; // no '0' option

		public final static Integer[] ListofMPU9150AccelRangeConfigValues={0,1,2,3};
		public final static Integer[] ListofMPU9150MplCalibrationOptionsConfigValues={0,1,2,3,4,5,6,7};
		public final static Integer[] ListofMPU9150MplLpfOptionsConfigValues={0,1,2,3,4,5,6};

		public final static Integer[] ListOfExGGainConfigValues={0,1,2,3,4,5,6};
		public final static String[] ListOfExGResolutions={"16-bit","24-bit"};
		public final static Integer[] ListOfExGResolutionsConfigValues={0,1};

		public final static Integer[] ListOfECGReferenceElectrodeConfigValues={13,0};
		public final static Integer[] ListOfEMGReferenceElectrodeConfigValues={0,3};
		public final static Integer[] ListOfExGLeadOffDetectionConfigValues={-1,0};
		public final static Integer[] ListOfExGLeadOffCurrentConfigValues={0,1,2,3};
		public final static Integer[] ListOfExGLeadOffComparatorConfigValues={0,1,2,3,4,5,6,7};

		public final static String[] ListOfExGRespirationDetectFreq={"32 kHz","64 kHz"};
		public final static Integer[] ListOfExGRespirationDetectFreqConfigValues={0,1};
		public final static String[] ListOfExGRespirationDetectPhase32khz={"0°","11.25°","22.5°","33.75°","45°","56.25°","67.5°","78.75°","90°","101.25°","112.5°","123.75°","135°","146.25°","157.5°","168.75°"};
		public final static Integer[] ListOfExGRespirationDetectPhase32khzConfigValues={0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15};
		public final static String[] ListOfExGRespirationDetectPhase64khz={"0°","22.5°","45°","67.5°","90°","112.5°","135°","157.5°"};
		public final static Integer[] ListOfExGRespirationDetectPhase64khzConfigValues={0,1,2,3,4,5,6,7};

		public final static String[] ListOfExGRate={"125 Hz","250 Hz","500 Hz","1 kHz","2 kHz","4 kHz","8 kHz"};
		public final static Integer[] ListOfExGRateConfigValues={0,1,2,3,4,5,6};

		public final static String[] ListOfOnOff={"On","Off"};
		public final static Integer[] ListOfOnOffConfigValues={0x01,0x00};
		
		public final static String[] ListOfPpgAdcSelection={"Int A13","Int A12"};
		public final static Integer[] ListOfPpgAdcSelectionConfigValues={0,1};
		public final static String[] ListOfPpg1AdcSelection={"Int A13","Int A12"};
		public final static Integer[] ListOfPpg1AdcSelectionConfigValues={0,1};
		public final static String[] ListOfPpg2AdcSelection={"Int A1","Int A14"};
		public final static Integer[] ListOfPpg2AdcSelectionConfigValues={0,1};

		
		public class SensorMapKey{
			/**
			 * Shimmer3 Low-noise analog accelerometer
			 */
			public final static int A_ACCEL = 0;
			/**
			 * Shimmer3 Gyroscope
			 */
			public final static int MPU9150_GYRO = 1;
			/**
			 * Shimmer3 Primary magnetometer
			 */
			public final static int LSM303DLHC_MAG = 2;
			public final static int EXG1_24BIT = 3;
			public final static int EXG2_24BIT = 4;
			public final static int GSR = 5;
			public final static int EXT_EXP_ADC_A6 = 6;
			public final static int EXT_EXP_ADC_A7 = 7;
			public final static int BRIDGE_AMP = 8;
			public final static int RESISTANCE_AMP = 9;
			//public final static int HR = 9;
			public final static int VBATT = 10;
			/**
			 * Shimmer3 Wide-range digital accelerometer
			 */
			public final static int LSM303DLHC_ACCEL = 11;
			public final static int EXT_EXP_ADC_A15 = 12;
			public final static int INT_EXP_ADC_A1 = 13;
			public final static int INT_EXP_ADC_A12 = 14;
			public final static int INT_EXP_ADC_A13 = 15;
			public final static int INT_EXP_ADC_A14 = 16;
			/**
			 * Shimmer3 Alternative accelerometer
			 */
			public final static int MPU9150_ACCEL = 17;
			/**
			 * Shimmer3 Alternative magnetometer
			 */
			public final static int MPU9150_MAG = 18;
			public final static int EXG1_16BIT = 19;
			public final static int EXG2_16BIT = 21;
			public final static int BMP180_PRESSURE = 22;
			//public final static int BMP180_TEMPERATURE = 23; // not yet implemented
			//public final static int MSP430_TEMPERATURE = 24; // not yet implemented
			public final static int MPU9150_TEMP = 25;
			//public final static int LSM303DLHC_TEMPERATURE = 26; // not yet implemented
			//public final static int MPU9150_MPL_TEMPERATURE = 1<<17; // same as SENSOR_SHIMMER3_MPU9150_TEMP 
			public final static int MPU9150_MPL_QUAT_6DOF = 27;
			public final static int MPU9150_MPL_QUAT_9DOF = 28;
			public final static int MPU9150_MPL_EULER_6DOF = 29;
			public final static int MPU9150_MPL_EULER_9DOF = 30;
			public final static int MPU9150_MPL_HEADING = 31;
			public final static int MPU9150_MPL_PEDOMETER = 32;
			public final static int MPU9150_MPL_TAP = 33;
			public final static int MPU9150_MPL_MOTION_ORIENT = 34;
			public final static int MPU9150_MPL_GYRO = 35;
			public final static int MPU9150_MPL_ACCEL = 36;
			public final static int MPU9150_MPL_MAG = 37;
			public final static int MPU9150_MPL_QUAT_6DOF_RAW = 38;
	
			// Combination Channels
			public final static int ECG = 100;
			public final static int EMG = 101;
			public final static int EXG_TEST = 102;
			
			// Derived Channels
			public final static int EXG_RESPIRATION = 103;
			public final static int SKIN_TEMP_PROBE = 104;
	
			// Derived Channels - GSR Board
			public final static int PPG_DUMMY = 105;
			public final static int PPG_A12 = 106;
			public final static int PPG_A13 = 107;
			
			// Derived Channels - Proto3 Deluxe Board
			
			public final static int PPG1_DUMMY = 110;
			public final static int PPG1_A12 = 111;
			public final static int PPG1_A13 = 112;
			public final static int PPG2_DUMMY = 113;
			public final static int PPG2_A1 = 114;
			public final static int PPG2_A14 = 115;
		}

		// Sensor Options Map
		public class GuiLabelConfig{
			public static final String SHIMMER_USER_ASSIGNED_NAME = "Shimmer Name";
			public static final String EXPERIMENT_NAME = "Experiment Name";
			public static final String SHIMMER_SAMPLING_RATE = "Sampling Rate";
			public static final String BUFFER_SIZE = "Buffer Size";
			public static final String CONFIG_TIME = "Config Time";
			public static final String EXPERIMENT_NUMBER_OF_SHIMMERS = "Number Of Shimmers";
			public static final String SHIMMER_MAC_FROM_INFOMEM = "InfoMem MAC";
			public static final String EXPERIMENT_ID = "Experiment ID";
			public static final String EXPERIMENT_DURATION_ESTIMATED = "Estimated Duration";
			public static final String EXPERIMENT_DURATION_MAXIMUM = "Maximum Duration";
			public static final String BROADCAST_INTERVAL = "Broadcast Interval";
			public static final String BLUETOOTH_BAUD_RATE = "Bluetooth Baud Rate";

			public static final String USER_BUTTON_START = "User Button Start";
			public static final String SINGLE_TOUCH_START = "Single Touch Start";
			public static final String EXPERIMENT_MASTER_SHIMMER = "Master Shimmer";
			public static final String EXPERIMENT_SYNC_WHEN_LOGGING = "Sync When Logging";

			public static final String LSM303DLHC_ACCEL_RATE = "Wide Range Accel Rate";
			public static final String LSM303DLHC_ACCEL_RANGE = "Wide Range Accel Range";
			public static final String MPU9150_GYRO_RANGE = "Gyro Range";
			public static final String MPU9150_GYRO_RATE = "Gyro Sampling Rate";
			public static final String LSM303DLHC_MAG_RANGE = "Mag Range";
			public static final String LSM303DLHC_MAG_RATE = "Mag Rate";
			public static final String PRESSURE_RESOLUTION = "Pressure Resolution";
			public static final String GSR_RANGE = "GSR Range";
			public static final String EXG_RESOLUTION = "Resolution";
			public static final String EXG_GAIN = "Gain";

			public static final String EXG_RATE = "Rate";
			public static final String EXG_REFERENCE_ELECTRODE = "Reference Electrode";
			public static final String EXG_LEAD_OFF_DETECTION = "Lead-Off Detection";
			public static final String EXG_LEAD_OFF_CURRENT = "Lead-Off Current";
			public static final String EXG_LEAD_OFF_COMPARATOR = "Lead-Off Compartor Threshold";
			public static final String EXG_RESPIRATION_DETECT_FREQ = "Respiration Detection Freq.";
			public static final String EXG_RESPIRATION_DETECT_PHASE = "Respiration Detection Phase";

			public static final String MPU9150_ACCEL_RANGE = "MPU Accel Range";
			public static final String MPU9150_DMP_GYRO_CAL = "MPU Gyro Cal";
			public static final String MPU9150_LPF = "MPU LPF";
			public static final String MPU9150_MPL_RATE = "MPL Rate";
			public static final String MPU9150_MAG_RATE = "MPU Mag Rate";

			public static final String MPU9150_DMP = "DMP";
			public static final String MPU9150_MPL = "MPL";
			public static final String MPU9150_MPL_9DOF_SENSOR_FUSION = "9DOF Sensor Fusion";
			public static final String MPU9150_MPL_GYRO_CAL = "Gyro Calibration";
			public static final String MPU9150_MPL_VECTOR_CAL = "Vector Compensation Calibration";
			public static final String MPU9150_MPL_MAG_CAL = "Magnetic Disturbance Calibration";

			public static final String KINEMATIC_LPM = "Kinematic Sensors Low-Power Mode";
			public static final String LSM303DLHC_ACCEL_LPM = "Wide Range Accel Low-Power Mode";
			public static final String MPU9150_GYRO_LPM = "Gyro Low-Power Mode";
			public static final String LSM303DLHC_MAG_LPM = "Mag Low-Power Mode";
			public static final String TCX0 = "TCX0";
			public static final String INT_EXP_BRD_POWER_BOOLEAN = "Internal Expansion Board Power";
			public static final String INT_EXP_BRD_POWER_INTEGER = "Int Exp Power";
			
			public static final String PPG_ADC_SELECTION = "PPG Channel";
			public static final String PPG1_ADC_SELECTION = "PPG1 Channel";
			public static final String PPG2_ADC_SELECTION = "PPG2 Channel";

		}

		// GUI Sensor Tiles
		public class GuiLabelSensorTiles{
			public static final String LOW_NOISE_ACCEL = Configuration.Shimmer3.GuiLabelSensors.ACCEL_LN;
			public static final String GYRO = Configuration.Shimmer3.GuiLabelSensors.GYRO;
			public static final String MAG = Configuration.Shimmer3.GuiLabelSensors.MAG;
			public static final String BATTERY_MONITORING = Configuration.Shimmer3.GuiLabelSensors.BATTERY;
			public static final String WIDE_RANGE_ACCEL = Configuration.Shimmer3.GuiLabelSensors.ACCEL_WR;
			public static final String PRESSURE_TEMPERATURE = Configuration.Shimmer3.GuiLabelSensors.PRESS_TEMP_BMP180;
			public static final String EXTERNAL_EXPANSION_ADC = "External Expansion";
			public static final String GSR = "GSR+";
			public static final String EXG = "ECG/EMG";
			public static final String PROTO3_MINI = "Proto Mini";
			public static final String PROTO3_DELUXE = "Proto Deluxe";
			public static final String PROTO3_DELUXE_SUPP = "PPG";
			public static final String BRIDGE_AMPLIFIER = "Bridge Amplifier+";
			public static final String BRIDGE_AMPLIFIER_SUPP = Configuration.Shimmer3.GuiLabelSensors.SKIN_TEMP_PROBE;
			public static final String HIGH_G_ACCEL = Configuration.Shimmer3.GuiLabelSensors.HIGH_G_ACCEL;
			public static final String INTERNAL_EXPANSION_ADC = "Internal Expansion";
			//public static final String GPS = "GPS";
		}
		
		//GUI SENSORS
		public class GuiLabelSensors{
			public static final String ACCEL_LN = "Low Noise Accel";
			public static final String BATTERY = "Battery Monitoring";
			public static final String EXT_EXP_A7 = "Ext A7";
			public static final String EXT_EXP_A6 = "Ext A6";
			public static final String EXT_EXP_A15 = "Ext A15";
			public static final String INT_EXP_A12 = "Int A12";
			public static final String INT_EXP_A13 = "Int A13";
			public static final String INT_EXP_A14 = "Int A14";
			public static final String BRIDGE_AMPLIFIER = "Bridge Amplifier";
			public static final String GSR = "GSR";
			public static final String INT_EXP_A1 = "Int A1";
			public static final String RESISTANCE_AMP = "Resistance Amplifier";
			public static final String GYRO = "Gyro";
			public static final String ACCEL_WR = "Wide Range Accel";
			public static final String MAG = "Mag";
			public static final String ACCEL_MPU = "Alternative Accel";
			public static final String MAG_MPU = "Alternative Mag";
			public static final String PRESS_TEMP_BMP180 = "Pressure & Temperature";
			public static final String EMG = "EMG";
			public static final String ECG = "ECG";
			public static final String EXG_TEST = "Test";
			public static final String EXT_EXP_ADC = "External Expansion";
			public static final String QUAT_MPL_6DOF = "MPU Quat 6DOF";
			public static final String QUAT_MPL_9DOF = "MPU Quat 9DOF";
			public static final String EULER_MPL_6DOF = "MPU Euler 6DOF";
			public static final String EULER_MPL_9DOF = "MPU Euler 9DOF";
			public static final String MPL_HEADING = "MPU Heading";
			public static final String MPL_TEMPERATURE = "MPU Temp";
			public static final String MPL_PEDOM_CNT = "MPL_Pedom_cnt"; // not currently supported
			public static final String MPL_PEDOM_TIME = "MPL_Pedom_Time"; // not currently supported
			public static final String MPL_TAPDIRANDTAPCNT = "TapDirAndTapCnt"; // not currently supported
			public static final String MPL_MOTIONANDORIENT = "MotionAndOrient"; // not currently supported
			public static final String GYRO_MPU_MPL = "MPU Gyro";
			public static final String ACCEL_MPU_MPL = "MPU Accel";
			public static final String MAG_MPU_MPL = "MPU Mag";
			public static final String QUAT_DMP_6DOF = "MPU Quat 6DOF (from DMP)";
			public static final String ECG_TO_HR = "ECG To HR";
			public static final String PPG_TO_HR = "PPG To HR";
			public static final String ORIENTATION_3D_6DOF = "3D Orientation (6DOF)";
			public static final String ORIENTATION_3D_9DOF = "3D Orientation (9DOF)";
			public static final String EULER_ANGLES_6DOF = "Euler Angles (6DOF)";
			public static final String EULER_ANGLES_9DOF = "Euler Angles (9DOF)";

			public static final String HIGH_G_ACCEL = "200g Accel";

			public static final String PPG_DUMMY = "PPG";
			public static final String PPG_A12 = "PPG A12";
			public static final String PPG_A13 = "PPG A13";
			public static final String PPG1_DUMMY = "PPG1";
			public static final String PPG1_A12 = "PPG1 A12";
			public static final String PPG1_A13 = "PPG1 A13";
			public static final String PPG2_DUMMY = "PPG2";
			public static final String PPG2_A1 = "PPG2 A1";
			public static final String PPG2_A14 = "PPG2 A14";
			public static final String EXG_RESPIRATION = "Respiration";
			public static final String SKIN_TEMP_PROBE = "Temperature Probe";
			public static final String BRAMP_HIGHGAIN = "High Gain";
			public static final String BRAMP_LOWGAIN = "Low Gain";
	
			public static final String EXG1_24BIT = "EXG1 24BIT";
			public static final String EXG2_24BIT = "EXG2 24BIT";
			public static final String EXG1_16BIT = "EXG1 16BIT";
			public static final String EXG2_16BIT = "EXG2 16BIT";
		}

		//DATABASE NAMES
		//GUI AND EXPORT CHANNELS
		public class ObjectClusterSensorName{
			public static final String TIMESTAMP = "Timestamp";
			public static final String REAL_TIME_CLOCK = "RealTime";
			public static final String ACCEL_LN_X = "Accel_LN_X";
			public static final String ACCEL_LN_Y = "Accel_LN_Y";
			public static final String ACCEL_LN_Z = "Accel_LN_Z";
			public static final String BATTERY = "Battery";
			public static final String EXT_EXP_A7 = "Ext_Exp_A7";
			public static final String EXT_EXP_A6 = "Ext_Exp_A6";
			public static final String EXT_EXP_A15 = "Ext_Exp_A15";
			public static final String INT_EXP_A12 = "Int_Exp_A12";
			public static final String INT_EXP_A13 = "Int_Exp_A13";
			public static final String INT_EXP_A14 = "Int_Exp_A14";
			public static final String BRIDGE_AMP_HIGH = "Bridge_Amp_High";
			public static final String BRIDGE_AMP_LOW = "Bridge_Amp_Low";
			public static final String GSR = "GSR";
			public static final String INT_EXP_A1 = "Int_Exp_A1";
			public static final String RESISTANCE_AMP = "Resistance_Amp";
			public static final String GYRO_X = "Gyro_X";
			public static final String GYRO_Y = "Gyro_Y";
			public static final String GYRO_Z = "Gyro_Z";
			public static final String ACCEL_WR_X = "Accel_WR_X";
			public static final String ACCEL_WR_Y = "Accel_WR_Y";
			public static final String ACCEL_WR_Z= "Accel_WR_Z";
			public static final String MAG_X = "Mag_X";
			public static final String MAG_Y = "Mag_Y";
			public static final String MAG_Z = "Mag_Z";
			public static final String ACCEL_MPU_X = "Accel_MPU_X";
			public static final String ACCEL_MPU_Y = "Accel_MPU_Y";
			public static final String ACCEL_MPU_Z = "Accel_MPU_Z";
			public static final String MAG_MPU_X = "Mag_MPU_X";
			public static final String MAG_MPU_Y = "Mag_MPU_Y";
			public static final String MAG_MPU_Z = "Mag_MPU_Z";
			public static final String TEMPERATURE_BMP180 = "Temperature_BMP180";
			public static final String PRESSURE_BMP180 = "Pressure_BMP180";
			public static final String EMG_CH1_24BIT = "EMG_CH1_24BIT";
			public static final String EMG_CH2_24BIT = "EMG_CH2_24BIT";
			public static final String EMG_CH1_16BIT = "EMG_CH1_16BIT";
			public static final String EMG_CH2_16BIT = "EMG_CH2_16BIT";
			public static final String ECG_LL_RA_24BIT = "ECG_LL-RA_24BIT";
			public static final String ECG_LA_RA_24BIT = "ECG_LA-RA_24BIT";
			public static final String ECG_LL_RA_16BIT = "ECG_LL-RA_16BIT";
			public static final String ECG_LA_RA_16BIT = "ECG_LA-RA_16BIT";
			public static final String TEST_CH1_24BIT = "Test_CH1_24BIT";
			public static final String TEST_CH2_24BIT = "Test_CH2_24BIT";
			public static final String TEST_CH1_16BIT = "Test_CH1_16BIT";
			public static final String TEST_CH2_16BIT = "Test_CH2_16BIT";
			public static final String EXG1_STATUS = "ECG_EMG_Status1";
			public static final String ECG_RESP_24BIT = "ECG_RESP_24BIT";
			public static final String ECG_VX_RL_24BIT = "ECG_Vx-RL_24BIT";
			public static final String ECG_RESP_16BIT = "ECG_RESP_16BIT";
			public static final String ECG_VX_RL_16BIT = "ECG_Vx-RL_16BIT";
			public static final String EXG1_CH1_24BIT = "ExG1_CH1_24BIT";
			public static final String EXG1_CH2_24BIT = "ExG1_CH2_24BIT";
			public static final String EXG1_CH1_16BIT = "ExG1_CH1_16BIT";
			public static final String EXG1_CH2_16BIT = "ExG1_CH2_16BIT";
			public static final String EXG2_CH1_24BIT = "ExG2_CH1_24BIT";
			public static final String EXG2_CH2_24BIT = "ExG2_CH2_24BIT";
			public static final String EXG2_CH1_16BIT = "ExG2_CH1_16BIT";
			public static final String EXG2_CH2_16BIT = "ExG2_CH2_16BIT";
			public static final String EXG2_STATUS = "ECG_EMG_Status2";
			public static final String QUAT_MPL_6DOF_W = "Quat_MPL_6DOF_W";
			public static final String QUAT_MPL_6DOF_X = "Quat_MPL_6DOF_X";
			public static final String QUAT_MPL_6DOF_Y = "Quat_MPL_6DOF_Y";
			public static final String QUAT_MPL_6DOF_Z = "Quat_MPL_6DOF_Z";
			public static final String QUAT_MPL_9DOF_W = "Quat_MPL_9DOF_W";
			public static final String QUAT_MPL_9DOF_X = "Quat_MPL_9DOF_X";
			public static final String QUAT_MPL_9DOF_Y = "Quat_MPL_9DOF_Y";
			public static final String QUAT_MPL_9DOF_Z = "Quat_MPL_9DOF_Z";
			public static final String EULER_MPL_6DOF_X = "Euler_MPL_6DOF_X";
			public static final String EULER_MPL_6DOF_Y = "Euler_MPL_6DOF_Y";
			public static final String EULER_MPL_6DOF_Z = "Euler_MPL_6DOF_Z";
			public static final String EULER_MPL_9DOF_X = "Euler_MPL_9DOF_X";
			public static final String EULER_MPL_9DOF_Y = "Euler_MPL_9DOF_Y";
			public static final String EULER_MPL_9DOF_Z = "Euler_MPL_9DOF_Z";
			public static final String MPL_HEADING = "MPL_heading";
			public static final String MPL_TEMPERATURE = "MPL_Temperature";
			public static final String MPL_PEDOM_CNT = "MPL_Pedom_cnt";
			public static final String MPL_PEDOM_TIME = "MPL_Pedom_Time";
			public static final String TAPDIRANDTAPCNT = "TapDirAndTapCnt";
			public static final String MOTIONANDORIENT = "MotionAndOrient";
			public static final String GYRO_MPU_MPL_X = "Gyro_MPU_MPL_X";
			public static final String GYRO_MPU_MPL_Y = "Gyro_MPU_MPL_Y";
			public static final String GYRO_MPU_MPL_Z = "Gyro_MPU_MPL_Z";
			public static final String ACCEL_MPU_MPL_X = "Accel_MPU_MPL_X";
			public static final String ACCEL_MPU_MPL_Y = "Accel_MPU_MPL_Y";
			public static final String ACCEL_MPU_MPL_Z = "Accel_MPU_MPL_Z";
			public static final String MAG_MPU_MPL_X = "Mag_MPU_MPL_X";
			public static final String MAG_MPU_MPL_Y = "Mag_MPU_MPL_Y";
			public static final String MAG_MPU_MPL_Z = "Mag_MPU_MPL_Z";
			public static final String QUAT_DMP_6DOF_W = "Quat_DMP_6DOF_W";
			public static final String QUAT_DMP_6DOF_X = "Quat_DMP_6DOF_X";
			public static final String QUAT_DMP_6DOF_Y = "Quat_DMP_6DOF_Y";
			public static final String QUAT_DMP_6DOF_Z = "Quat_DMP_6DOF_Z";
			public static final String ECG_TO_HR = "ECGtoHR";
			public static final String PPG_TO_HR = "PPGtoHR";
			public static final String QUAT_MADGE_6DOF_W = "Quat_Madge_6DOF_W";
			public static final String QUAT_MADGE_6DOF_X = "Quat_Madge_6DOF_X";
			public static final String QUAT_MADGE_6DOF_Y = "Quat_Madge_6DOF_Y";
			public static final String QUAT_MADGE_6DOF_Z = "Quat_Madge_6DOF_Z";
			public static final String QUAT_MADGE_9DOF_W = "Quat_Madge_9DOF_W";
			public static final String QUAT_MADGE_9DOF_X = "Quat_Madge_9DOF_X";
			public static final String QUAT_MADGE_9DOF_Y = "Quat_Madge_9DOF_Y";
			public static final String QUAT_MADGE_9DOF_Z = "Quat_Madge_9DOF_Z";
			public static final String EULER_6DOF_A = "Euler_6DOF_A";
			public static final String EULER_6DOF_X = "Euler_6DOF_X";
			public static final String EULER_6DOF_Y = "Euler_6DOF_Y";
			public static final String EULER_6DOF_Z = "Euler_6DOF_Z";
			public static final String EULER_9DOF_A = "Euler_9DOF_A";
			public static final String EULER_9DOF_X = "Euler_9DOF_X";
			public static final String EULER_9DOF_Y = "Euler_9DOF_Y";
			public static final String EULER_9DOF_Z = "Euler_9DOF_Z";
			public static final String AXIS_ANGLE_A = "Axis Angle A";
			public static final String AXIS_ANGLE_X = "Axis Angle X";
			public static final String AXIS_ANGLE_Y = "Axis Angle Y";
			public static final String AXIS_ANGLE_Z = "Axis Angle Z";
		}
	}

	public static class Shimmer2{
		public class Channel{
			public final static int XAccel      = 0x00;
			public final static int YAccel      = 0x01;
			public final static int ZAccel      = 0x02;
			public final static int XGyro       = 0x03;
			public final static int YGyro       = 0x04;
			public final static int ZGyro       = 0x05;
			public final static int XMag        = 0x06;
			public final static int YMag        = 0x07;
			public final static int ZMag        = 0x08;
			public final static int EcgRaLl     = 0x09;
			public final static int EcgLaLl     = 0x0A;
			public final static int GsrRaw      = 0x0B;
			public final static int GsrRes      = 0x0C;
			public final static int Emg         = 0x0D;
			public final static int AnExA0      = 0x0E;
			public final static int AnExA7      = 0x0F;
			public final static int BridgeAmpHigh  = 0x10;
			public final static int BridgeAmpLow   = 0x11;
			public final static int HeartRate   = 0x12;
		}
		public class SensorBitmap{
			public static final int SENSOR_ACCEL				   = 0x80;
			public static final int SENSOR_GYRO				   	   = 0x40;
			public static final int SENSOR_MAG					   = 0x20;
			public static final int SENSOR_ECG					   = 0x10;
			public static final int SENSOR_EMG					   = 0x08;
			public static final int SENSOR_GSR					   = 0x04;
			public static final int SENSOR_EXP_BOARD_A7		       = 0x02;
			public static final int SENSOR_EXP_BOARD_A0		       = 0x01;
			public static final int SENSOR_BRIDGE_AMP			   = 0x8000;
			public static final int SENSOR_HEART				   = 0x4000;
		}
		
		//DATABASE NAMES
				//GUI AND EXPORT CHANNELS
				public class ObjectClusterSensorName{
					public static final String TIMESTAMP = "Timestamp";
					public static final String REAL_TIME_CLOCK = "RealTime";
					public static final String ACCEL_X = "Accel_X";
					public static final String ACCEL_Y = "Accel_Y";
					public static final String ACCEL_Z = "Accel_Z";
					public static final String BATTERY = "Battery";
					public static final String REG = "Reg";
					public static final String EXT_EXP_A7 = "Ext_Exp_A7";
					public static final String EXT_EXP_A6 = "Ext_Exp_A6";
					public static final String EXT_EXP_A15 = "Ext_Exp_A15";
					public static final String INT_EXP_A12 = "Int_Exp_A12";
					public static final String INT_EXP_A13 = "Int_Exp_A13";
					public static final String INT_EXP_A14 = "Int_Exp_A14";
					public static final String BRIDGE_AMP_HIGH = "Bridge_Amp_High";
					public static final String BRIDGE_AMP_LOW = "Bridge_Amp_Low";
					public static final String GSR = "GSR";
					//public static final String GSR_RAW = "GSR Raw";
					public static final String GSR_RES = "GSR Res";
					public static final String INT_EXP_A1 = "Int_Exp_A1";
					public static final String EXP_BOARD_A0 = "Exp Board A0";
					public static final String EXP_BOARD_A7 = "Exp Board A7";
					public static final String GYRO_X = "Gyro_X";
					public static final String GYRO_Y = "Gyro_Y";
					public static final String GYRO_Z = "Gyro_Z";
					public static final String MAG_X = "Mag_X";
					public static final String MAG_Y = "Mag_Y";
					public static final String MAG_Z = "Mag_Z";
					public static final String EMG = "EMG";
					public static final String ECG_RA_LL = "ECG_RA-LL";
					public static final String ECG_LA_LL = "ECG_LA-LL";
					public static final String ECG_TO_HR = "ECGtoHR";
					public static final String QUAT_MADGE_6DOF_W = "Quat_Madge_6DOF_W";
					public static final String QUAT_MADGE_6DOF_X = "Quat_Madge_6DOF_X";
					public static final String QUAT_MADGE_6DOF_Y = "Quat_Madge_6DOF_Y";
					public static final String QUAT_MADGE_6DOF_Z = "Quat_Madge_6DOF_Z";
					public static final String QUAT_MADGE_9DOF_W = "Quat_Madge_9DOF_W";
					public static final String QUAT_MADGE_9DOF_X = "Quat_Madge_9DOF_X";
					public static final String QUAT_MADGE_9DOF_Y = "Quat_Madge_9DOF_Y";
					public static final String QUAT_MADGE_9DOF_Z = "Quat_Madge_9DOF_Z";
					public static final String EULER_6DOF_A = "Euler_6DOF_A";
					public static final String EULER_6DOF_X = "Euler_6DOF_X";
					public static final String EULER_6DOF_Y = "Euler_6DOF_Y";
					public static final String EULER_6DOF_Z = "Euler_6DOF_Z";
					public static final String EULER_9DOF_A = "Euler_9DOF_A";
					public static final String EULER_9DOF_X = "Euler_9DOF_X";
					public static final String EULER_9DOF_Y = "Euler_9DOF_Y";
					public static final String EULER_9DOF_Z = "Euler_9DOF_Z";
					public static final String HEART_RATE = "Heart Rate"; //for the heart rate strap now no longer sold
					public static final String AXIS_ANGLE_A = "Axis Angle A";
					public static final String AXIS_ANGLE_X = "Axis Angle X";
					public static final String AXIS_ANGLE_Y = "Axis Angle Y";
					public static final String AXIS_ANGLE_Z = "Axis Angle Z";
					public static final String VOLT_REG = "VSenseReg";
				}
		
		public final static String[] ListofCompatibleSensors={"Accelerometer","Gyroscope","Magnetometer","Battery Voltage","ECG","EMG","GSR","Exp Board","Bridge Amplifier","Heart Rate"};
		public final static String[] ListofAccelRange={"+/- 1.5g","+/- 6g"};
		public final static String[] ListofMagRange={"+/- 0.8Ga","+/- 1.3Ga","+/- 1.9Ga","+/- 2.5Ga","+/- 4.0Ga","+/- 4.7Ga","+/- 5.6Ga","+/- 8.1Ga"};
		public final static String[] ListofGSRRange={"10kOhm to 56kOhm","56kOhm to 220kOhm","220kOhm to 680kOhm","680kOhm to 4.7MOhm","Auto Range"};

		public class SensorMapKey{
			public final static int ACCEL = 0;
			public final static int GYRO = 1;
			public final static int MAG = 2;
			public final static int EMG = 3;
			public final static int ECG = 4;
			public final static int GSR = 5;
			public final static int EXP_BOARD_A7 = 6;
			public final static int EXP_BOARD_A0 = 7;
			public final static int EXP_BOARD = 8;
			public final static int BRIDGE_AMP = 9;
			public final static int HEART = 10;
			public final static int BATT = 11;
			public final static int EXT_ADC_A15 = 12;
			public final static int INT_ADC_A1 = 13;
			public final static int INT_ADC_A12 = 14;
			public final static int INT_ADC_A13 = 15;
			public final static int INT_ADC_A14 = 16;
		}
	}

}


