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
			public static final int SENSOR_A_ACCEL_S3			   = 0x80;
			public static final int SENSOR_GYRO_S3			   	   = 0x40;
			public static final int SENSOR_MAG_S3				   = 0x20;
			public static final int SENSOR_EXG1_24BIT			   = 0x10;
			public static final int SENSOR_EXG2_24BIT			   = 0x08;
			public static final int SENSOR_GSR					   = 0x04;
			public static final int SENSOR_EXT_A7				   = 0x02;
			public static final int SENSOR_EXT_A6				   = 0x01;
			public static final int SENSOR_VBATT_S3				   = 0x2000;
			public static final int SENSOR_D_ACCEL_S3			   = 0x1000;
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
		public final static String[] ListofGyroRange={"+/- 250dps","+/- 500dps","+/- 1000dps","+/- 2000dps"}; 
		public final static String[] ListofMagRange={"+/- 1.3Ga","+/- 1.9Ga","+/- 2.5Ga","+/- 4.0Ga","+/- 4.7Ga","+/- 5.6Ga","+/- 8.1Ga"}; 
		public final static String[] ListofPressureResolution={"Low","Standard","High","Very High"};
		public final static String[] ListofGSRRange={"10kOhm to 56kOhm","56kOhm to 220kOhm","220kOhm to 680kOhm","680kOhm to 4.7MOhm","Auto Range"};
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
		public final static String[] ListOfExGRespirationDetectPhase32khz={"0�","11.25�","22.5�","33.75�","45�","56.25�","67.5�","78.75�","90�","101.25�","112.5�","123.75�","135�","146.25�","157.5�","168.75�"};
		public final static Integer[] ListOfExGRespirationDetectPhase32khzConfigValues={0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15};
		public final static String[] ListOfExGRespirationDetectPhase64khz={"0�","22.5�","45�","67.5�","90�","112.5�","135�","157.5�"};
		public final static Integer[] ListOfExGRespirationDetectPhase64khzConfigValues={0,1,2,3,4,5,6,7};
		
		public final static String[] ListOfExGRate={"125Hz","250Hz","500Hz","1kHz","2kHz","4kHz","8kHz"};
		public final static Integer[] ListOfExGRateConfigValues={0,1,2,3,4,5,6};
		

		/**
		 * Shimmer3 Low-noise analog accelerometer
		 */
		public final static int CHANNELMAPKEY_A_ACCEL = 0;
		/**
		 * Shimmer3 Gyroscope
		 */
		public final static int CHANNELMAPKEY_MPU9150_GYRO = 1;
		/**
		 * Shimmer3 Primary magnetometer
		 */
		public final static int CHANNELMAPKEY_LSM303DLHC_MAG = 2;
		public final static int CHANNELMAPKEY_EXG1_24BIT = 3;
		public final static int CHANNELMAPKEY_EXG2_24BIT = 4;
		public final static int CHANNELMAPKEY_GSR = 5;
		public final static int CHANNELMAPKEY_EXT_EXP_ADC_A6 = 6;
		public final static int CHANNELMAPKEY_EXT_EXP_ADC_A7 = 7;
		public final static int CHANNELMAPKEY_BRIDGE_AMP = 8;
		public final static int CHANNELMAPKEY_RESISTANCE_AMP = 9;
		//public final static int CHANNELMAPKEY_HR = 9;
		public final static int CHANNELMAPKEY_VBATT = 10;
		/**
		 * Shimmer3 Wide-range digital accelerometer
		 */
		public final static int CHANNELMAPKEY_LSM303DLHC_ACCEL = 11;
		public final static int CHANNELMAPKEY_EXT_EXP_ADC_A15 = 12;
		public final static int CHANNELMAPKEY_INT_EXP_ADC_A1 = 13;
		public final static int CHANNELMAPKEY_INT_EXP_ADC_A12 = 14;
		public final static int CHANNELMAPKEY_INT_EXP_ADC_A13 = 15;
		public final static int CHANNELMAPKEY_INT_EXP_ADC_A14 = 16;
		/**
		 * Shimmer3 Alternative accelerometer
		 */
		public final static int CHANNELMAPKEY_MPU9150_ACCEL = 17;
		/**
		 * Shimmer3 Alternative magnetometer
		 */
		public final static int CHANNELMAPKEY_MPU9150_MAG = 18;
		public final static int CHANNELMAPKEY_EXG1_16BIT = 19;
		public final static int CHANNELMAPKEY_EXG2_16BIT = 21;
		public final static int CHANNELMAPKEY_BMP180_PRESSURE = 22;
		//public final static int CHANNELMAPKEY_BMP180_TEMPERATURE = 23; // not yet implemented
		//public final static int CHANNELMAPKEY_MSP430_TEMPERATURE = 24; // not yet implemented
		public final static int CHANNELMAPKEY_MPU9150_TEMP = 25;
		//public final static int CHANNELMAPKEY_LSM303DLHC_TEMPERATURE = 26; // not yet implemented
		//public final static int CHANNELMAPKEY_MPU9150_MPL_TEMPERATURE = 1<<17; // same as SENSOR_SHIMMER3_MPU9150_TEMP 
		public final static int CHANNELMAPKEY_MPU9150_MPL_QUAT_6DOF = 27;
		public final static int CHANNELMAPKEY_MPU9150_MPL_QUAT_9DOF = 28;
		public final static int CHANNELMAPKEY_MPU9150_MPL_EULER_6DOF = 29;
		public final static int CHANNELMAPKEY_MPU9150_MPL_EULER_9DOF = 30;
		public final static int CHANNELMAPKEY_MPU9150_MPL_HEADING = 31;
		public final static int CHANNELMAPKEY_MPU9150_MPL_PEDOMETER = 32;
		public final static int CHANNELMAPKEY_MPU9150_MPL_TAP = 33;
		public final static int CHANNELMAPKEY_MPU9150_MPL_MOTION_ORIENT = 34;
		public final static int CHANNELMAPKEY_MPU9150_MPL_GYRO = 35;
		public final static int CHANNELMAPKEY_MPU9150_MPL_ACCEL = 36;
		public final static int CHANNELMAPKEY_MPU9150_MPL_MAG = 37;
		public final static int CHANNELMAPKEY_MPU9150_MPL_QUAT_6DOF_RAW = 38;
		
		// Combination Channels
		public final static int CHANNELMAPKEY_ECG = 100;
		public final static int CHANNELMAPKEY_EMG = 101;
		public final static int CHANNELMAPKEY_EXG_TEST = 102;

		public final static int CHANNELMAPKEY_PPG_A12 = 105;
		public final static int CHANNELMAPKEY_PPG_A13 = 106;
		public final static int CHANNELMAPKEY_EXG_RESPIRATION = 107;

		
		// Sensor Options Map
		public static final String GUI_LABEL_CONFIG_SHIMMER_USER_ASSIGNED_NAME = "Shimmer Name";
		public static final String GUI_LABEL_CONFIG_EXPERIMENT_NAME = "Experiment Name";
		public static final String GUI_LABEL_CONFIG_SHIMMER_SAMPLING_RATE = "Sampling Rate";
		public static final String GUI_LABEL_CONFIG_BUFFER_SIZE = "Buffer Size";
		public static final String GUI_LABEL_CONFIG_CONFIG_TIME = "Config Time";
		public static final String GUI_LABEL_CONFIG_EXPERIMENT_NUMBER_OF_SHIMMERS = "Number Of Shimmers";
		public static final String GUI_LABEL_CONFIG_SHIMMER_MAC_FROM_INFOMEM = "InfoMem MAC";
		public static final String GUI_LABEL_CONFIG_EXPERIMENT_ID = "Experiment ID";
		public static final String GUI_LABEL_CONFIG_EXPERIMENT_DURATION_ESTIMATED = "Estimated Duration";
		public static final String GUI_LABEL_CONFIG_EXPERIMENT_DURATION_MAXIMUM = "Maximum Duration";
		public static final String GUI_LABEL_CONFIG_BROADCAST_INTERVAL = "Broadcast Interval";
		public static final String GUI_LABEL_CONFIG_BLUETOOTH_BAUD_RATE = "Bluetooth Baud Rate";

		public static final String GUI_LABEL_CONFIG_USER_BUTTON_START = "User Button Start";
		public static final String GUI_LABEL_CONFIG_SINGLE_TOUCH_START = "Single Touch Start";
		public static final String GUI_LABEL_CONFIG_EXPERIMENT_MASTER_SHIMMER = "Master Shimmer";
		public static final String GUI_LABEL_CONFIG_EXPERIMENT_SYNC_WHEN_LOGGING = "Sync When Logging";
			
		public static final String GUI_LABEL_CONFIG_LSM303DLHC_ACCEL_RATE = "Wide Range Accel Rate";
		public static final String GUI_LABEL_CONFIG_LSM303DLHC_ACCEL_RANGE = "Wide Range Accel Range";
		public static final String GUI_LABEL_CONFIG_MPU9150_GYRO_RANGE = "Gyro Range";
		public static final String GUI_LABEL_CONFIG_MPU9150_GYRO_RATE = "Gyro Sampling Rate";
		public static final String GUI_LABEL_CONFIG_LSM303DLHC_MAG_RANGE = "Mag Range";
		public static final String GUI_LABEL_CONFIG_LSM303DLHC_MAG_RATE = "Mag Rate";
		public static final String GUI_LABEL_CONFIG_PRESSURE_RESOLUTION = "Pressure Resolution";
		public static final String GUI_LABEL_CONFIG_GSR_RANGE = "GSR Range";
		public static final String GUI_LABEL_CONFIG_EXG_RESOLUTION = "ExG Resolution";
		public static final String GUI_LABEL_CONFIG_EXG_GAIN = "ExG Gain";

		public static final String GUI_LABEL_CONFIG_EXG_RATE = "ExG Rate";
		public static final String GUI_LABEL_CONFIG_EXG_REFERENCE_ELECTRODE = "ExG Reference Electrode";
		public static final String GUI_LABEL_CONFIG_EXG_LEAD_OFF_DETECTION = "ExG Lead-Off Detection";
		public static final String GUI_LABEL_CONFIG_EXG_LEAD_OFF_CURRENT = "ExG Lead-Off Current";
		public static final String GUI_LABEL_CONFIG_EXG_LEAD_OFF_COMPARATOR = "ExG Lead-Off Compartor Threshold";
		public static final String GUI_LABEL_CONFIG_EXG_RESPIRATION_DETECT_FREQ = "ExG Respiration Detection Freq.";
		public static final String GUI_LABEL_CONFIG_EXG_RESPIRATION_DETECT_PHASE = "ExG Respiration Detection Phase";

		public static final String GUI_LABEL_CONFIG_MPU9150_ACCEL_RANGE = "MPU Accel Range";
		public static final String GUI_LABEL_CONFIG_MPU9150_DMP_GYRO_CAL = "MPU Gyro Cal";
		public static final String GUI_LABEL_CONFIG_MPU9150_LPF = "MPU LPF";
		public static final String GUI_LABEL_CONFIG_MPU9150_MPL_RATE = "MPL Rate";
		public static final String GUI_LABEL_CONFIG_MPU9150_MAG_RATE = "MPU Mag Rate";
		
		public static final String GUI_LABEL_CONFIG_MPU9150_DMP = "DMP";
		public static final String GUI_LABEL_CONFIG_MPU9150_MPL = "MPL";
		public static final String GUI_LABEL_CONFIG_MPU9150_MPL_9DOF_SENSOR_FUSION = "9DOF Sensor Fusion";
		public static final String GUI_LABEL_CONFIG_MPU9150_MPL_GYRO_CAL = "Gyro Calibration";
		public static final String GUI_LABEL_CONFIG_MPU9150_MPL_VECTOR_CAL = "Vector Compensation Calibration";
		public static final String GUI_LABEL_CONFIG_MPU9150_MPL_MAG_CAL = "Magnetic Disturbance Calibration";

		public static final String GUI_LABEL_CONFIG_KINEMATIC_LPM = "Kinematic Sensors Low-Power Mode";
		public static final String GUI_LABEL_CONFIG_LSM303DLHC_ACCEL_LPM = "Wide Range Accel Low-Power Mode";
		public static final String GUI_LABEL_CONFIG_MPU9150_GYRO_LPM = "Gyro Low-Power Mode";
		public static final String GUI_LABEL_CONFIG_LSM303DLHC_MAG_LPM = "Mag Low-Power Mode";
		public static final String GUI_LABEL_CONFIG_TCX0 = "TCX0";
		public static final String GUI_LABEL_CONFIG_INT_EXP_BRD_POW = "Internal Expansion Board Power";
		
		
		//Channels
		public static final String GUI_LABEL_CHANNEL_PPG_A12 = "PPG A12";
		public static final String GUI_LABEL_CHANNEL_PPG_A13 = "PPG A13";
		public static final String GUI_LABEL_CHANNEL_EXG_RESPIRATION = "Respiration";
		public static final String GUI_LABEL_CHANNEL_BRAMP_HIGHGAIN = "High Gain";
		public static final String GUI_LABEL_CHANNEL_BRAMP_LOWGAIN = "Low Gain";

		public static final String GUI_LABEL_CHANNEL_EXG1_24BIT = "EXG1 24BIT";
		public static final String GUI_LABEL_CHANNEL_EXG2_24BIT = "EXG2 24BIT";
		public static final String GUI_LABEL_CHANNEL_EXG1_16BIT = "EXG1 16BIT";
		public static final String GUI_LABEL_CHANNEL_EXG2_16BIT = "EXG2 16BIT";

		//ChannelTiles
		public static final String GUI_LABEL_CHANNELTILE_LOW_NOISE_ACCEL = "Low Noise Accel";
		public static final String GUI_LABEL_CHANNELTILE_GYRO = "Gyro";
		public static final String GUI_LABEL_CHANNELTILE_MAG = "Mag";
		public static final String GUI_LABEL_CHANNELTILE_BATTERY_MONITORING = "Battery Monitoring";
		public static final String GUI_LABEL_CHANNELTILE_WIDE_RANGE_ACCEL = "Wide Range Accel";
		public static final String GUI_LABEL_CHANNELTILE_PRESSURE_TEMPERATURE = "Pressure/Temperature";
		public static final String GUI_LABEL_CHANNELTILE_EXTERNAL_EXPANSION_ADC = "External Expansion";
		public static final String GUI_LABEL_CHANNELTILE_GSR = "GSR+";
		public static final String GUI_LABEL_CHANNELTILE_EXG = "ExG";
		public static final String GUI_LABEL_CHANNELTILE_PROTO_MINI = "Proto Mini";
		public static final String GUI_LABEL_CHANNELTILE_PROTO_DELUXE = "Proto Deluxe";
		public static final String GUI_LABEL_CHANNELTILE_BRIDGE_AMPLIFIER = "Bridge Amplifier+";
		public static final String GUI_LABEL_CHANNELTILE_HIGH_G_ACCEL = "200g Accel";
		public static final String GUI_LABEL_CHANNELTILE_INTERNAL_EXPANSION_ADC = "Internal Expansion";
		//public static final String GUI_LABEL_CHANNELTILE_GPS = "GPS";
		
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
		public final static String[] ListofCompatibleSensors={"Accelerometer","Gyroscope","Magnetometer","Battery Voltage","ECG","EMG","GSR","Exp Board","Bridge Amplifier","Heart Rate"};
		public final static String[] ListofAccelRange={"+/- 1.5g","+/- 6g"};
		public final static String[] ListofMagRange={"+/- 0.8Ga","+/- 1.3Ga","+/- 1.9Ga","+/- 2.5Ga","+/- 4.0Ga","+/- 4.7Ga","+/- 5.6Ga","+/- 8.1Ga"};
		public final static String[] ListofGSRRange={"10kOhm to 56kOhm","56kOhm to 220kOhm","220kOhm to 680kOhm","680kOhm to 4.7MOhm","Auto Range"};
		
		
		public final static int CHANNELMAPKEY_ACCEL = 0;
		public final static int CHANNELMAPKEY_GYRO = 1;
		public final static int CHANNELMAPKEY_MAG = 2;
		public final static int CHANNELMAPKEY_EMG = 3;
		public final static int CHANNELMAPKEY_ECG = 4;
		public final static int CHANNELMAPKEY_GSR = 5;
		public final static int CHANNELMAPKEY_EXP_BOARD_A7 = 6;
		public final static int CHANNELMAPKEY_EXP_BOARD_A0 = 7;
		public final static int CHANNELMAPKEY_EXP_BOARD = 8;
		public final static int CHANNELMAPKEY_BRIDGE_AMP = 9;
		public final static int CHANNELMAPKEY_HEART = 10;
		public final static int CHANNELMAPKEY_BATT = 11;
		public final static int CHANNELMAPKEY_EXT_ADC_A15 = 12;
		public final static int CHANNELMAPKEY_INT_ADC_A1 = 13;
		public final static int CHANNELMAPKEY_INT_ADC_A12 = 14;
		public final static int CHANNELMAPKEY_INT_ADC_A13 = 15;
		public final static int CHANNELMAPKEY_INT_ADC_A14 = 16;

	}

}


