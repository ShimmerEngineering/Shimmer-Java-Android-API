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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.shimmerresearch.driver.ChannelDetails.ChannelDataEndian;
import com.shimmerresearch.driver.ChannelDetails.ChannelDataType;
import com.shimmerresearch.driver.ShimmerVerDetails.FW_ID;
import com.shimmerresearch.driver.ShimmerVerDetails.HW_ID;
import com.shimmerresearch.driver.ShimmerVerDetails.HW_ID_SR_CODES;

/**
 * The purpose of this code is to maintain the configurations constants for a
 * Shimmer Device
 * 
 * @author Jong Chern Lim,
 * @author Ruaidhri Molloy
 * @author Mark Nolan
 * @author Alex Saez
 *
 */
public class Configuration {
	
	public static class CHANNEL_UNITS{
		//Sensors units
		public static final String NO_UNITS = "no units";
		public static final String MILLISECONDS = "ms";
		public static final String METER_PER_SECOND_SQUARE = "m/(s^2)";
		public static final String DEGREES_PER_SECOND = "deg/s";
		public static final String LOCAL_FLUX = "local_flux";
		public static final String KOHMS = "kOhms";
		public static final String MILLIVOLTS = "mV";
		public static final String BEATS_PER_MINUTE = "BPM";
		public static final String KPASCAL = "kPa";
		public static final String DEGREES_CELSUIS = "Degrees Celsius";
		public static final String DEGREES = "Degrees";
		public static final String U_TESLA = "uT";
		public static final String DATE_FORMAT = "yyyy/mm/dd hh:mm:ss.000";
		public static final String GRAVITY = "g";
		public static final String CLOCK_UNIT = "Ticks";

		public static final String ACCEL_CAL_UNIT = METER_PER_SECOND_SQUARE;
		public static final String ACCEL_DEFAULT_CAL_UNIT = METER_PER_SECOND_SQUARE+"*";
		public static final String GYRO_CAL_UNIT = DEGREES_PER_SECOND;
		public static final String GYRO_DEFAULT_CAL_UNIT = DEGREES_PER_SECOND+"*";
		public static final String MAG_CAL_UNIT = LOCAL_FLUX;
		public static final String MAG_DEFAULT_CAL_UNIT = LOCAL_FLUX+"*";
		
		public static final String LOCAL = "local"; //used for axis-angle and madgewick quaternions
	}	
	
	public static class CHANNEL_TYPE{
		public static final String CAL = "CAL";
		public static final String UNCAL = "UNCAL";
		public static final String RAW = "RAW";
	}
	
	//Channel Contents
	public static class ShimmerGQ{
		public class SensorMapKey{
			public static final int MPU9150_ACCEL = 17;
			public static final int MPU9150_GYRO = 1;
			public static final int MPU9150_MAG = 18;
			public static final int VBATT = 10;
			public static final int LSM303DLHC_ACCEL = 11;
			public static final int PPG = 107;
			public static final int GSR = 5;
			public static final int BEACON = 108;
		}

		// GUI Sensor Tiles
		public class GuiLabelSensorTiles{
			public static final String GYRO = Configuration.ShimmerGQ.GuiLabelSensors.GYRO;
			public static final String MAG = Configuration.ShimmerGQ.GuiLabelSensors.MAG;
			public static final String BATTERY_MONITORING = Configuration.ShimmerGQ.GuiLabelSensors.BATTERY;
			public static final String WIDE_RANGE_ACCEL = Configuration.ShimmerGQ.GuiLabelSensors.ACCEL_WR;
			public static final String GSR = "GSR+";
			public static final String BEACON = Configuration.ShimmerGQ.GuiLabelSensors.BEACON;
		}
		
		//GUI SENSORS
		public class GuiLabelSensors{
			public static final String BATTERY = "Battery Voltage";
			public static final String GSR = "GSR";
			public static final String GYRO = "Gyroscope";
			public static final String ACCEL_WR = "Wide-Range Accelerometer";
			public static final String MAG = "Magnetometer";
			public static final String ACCEL_MPU = "Alternative Accel";
			public static final String MAG_MPU = "Alternative Mag";
			public static final String PPG_TO_HR = "PPG To HR";
			public static final String PPG = "PPG";
			public static final String BEACON = "Beacon";
		}
		
		public class GuiLabelConfig{
			public static final String SAMPLING_RATE_DIVIDER_VBATT = "VBATT Divider";
			public static final String SAMPLING_RATE_DIVIDER_GSR = "GSR Divider";
			public static final String SAMPLING_RATE_DIVIDER_PPG = "PPG Divider";
			public static final String SAMPLING_RATE_DIVIDER_LSM303DLHC_ACCEL = "LSM3030DLHC Divider";
			public static final String LSM303DLHC_ACCEL_RATE = "Wide Range Accel Rate";
			public static final String LSM303DLHC_ACCEL_RANGE = "Wide Range Accel Range";
			public static final String GSR_RANGE = "GSR Range";
			public static final String LSM303DLHC_ACCEL_LPM = "Wide Range Accel Low-Power Mode";
			public static final String SAMPLING_RATE_DIVIDER_BEACON = "Beacon Divider";
			
			public static final String SHIMMER_USER_ASSIGNED_NAME = "Shimmer Name";
			public static final String EXPERIMENT_NAME = "Experiment Name";
			public static final String SHIMMER_SAMPLING_RATE = "Sampling Rate";
			public static final String CONFIG_TIME = "Config Time";
			public static final String SHIMMER_MAC_FROM_INFOMEM = "InfoMem MAC";

		}
		
		public static final String[] ListofSamplingRateDividers={"0.75Hz","1.5Hz","3Hz","7.5Hz","15Hz","30Hz","75Hz","220Hz"};
		public static final Integer[] ListofSamplingRateDividersValues={0,1,2,3,4,5,6,7};

		
	}
	
	public static class Shimmer3{
		public class Channel{
			public static final int XAAccel     			 = 0x00;
			public static final int YAAccel    				 = 0x01;
			public static final int ZAAccel     			 = 0x02;
			public static final int VBatt       			 = 0x03;
			public static final int XDAccel     			 = 0x04;
			public static final int YDAccel     			 = 0x05;
			public static final int ZDAccel     			 = 0x06;
			public static final int XMag        			 = 0x07;
			public static final int YMag        			 = 0x08;
			public static final int ZMag        			 = 0x09;
			public static final int XGyro       			 = 0x0A;
			public static final int YGyro       			 = 0x0B;
			public static final int ZGyro       			 = 0x0C;
			public static final int ExtAdc7					 = 0x0D;
			public static final int ExtAdc6					 = 0x0E;
			public static final int ExtAdc15 				 = 0x0F;
			public static final int IntAdc1					 = 0x10;
			public static final int IntAdc12 				 = 0x11;
			public static final int IntAdc13 				 = 0x12;
			public static final int IntAdc14 				 = 0x13;
			public static final int XAlterAccel      		 = 0x14; //Alternative Accelerometer
			public static final int YAlterAccel     		 = 0x15;
			public static final int ZAlterAccel     		 = 0x16;
			public static final int XAlterMag        		 = 0x17; //Alternative Magnetometer
			public static final int YAlterMag        		 = 0x18;
			public static final int ZAlterMag        		 = 0x19;
			public static final int Temperature 			 = 0x1A;
			public static final int Pressure 				 = 0x1B;
			public static final int GsrRaw 					 = 0x1C;
			public static final int EXG_ADS1292R_1_STATUS 	 = 0x1D;
			public static final int EXG_ADS1292R_1_CH1_24BIT = 0x1E;
			public static final int EXG_ADS1292R_1_CH2_24BIT = 0x1F;
			public static final int EXG_ADS1292R_2_STATUS 	 = 0x20;
			public static final int EXG_ADS1292R_2_CH1_24BIT = 0x21;
			public static final int EXG_ADS1292R_2_CH2_24BIT = 0x22;
			public static final int EXG_ADS1292R_1_CH1_16BIT = 0x23;
			public static final int EXG_ADS1292R_1_CH2_16BIT = 0x24;
			public static final int EXG_ADS1292R_2_CH1_16BIT = 0x25;
			public static final int EXG_ADS1292R_2_CH2_16BIT = 0x26;
			public static final int BridgeAmpHigh  			 = 0x27;
			public static final int BridgeAmpLow   			 = 0x28;
		}

		public class SensorBitmap{
			//Sensor Bitmap for Shimmer 3
			public static final int SENSOR_A_ACCEL			= 0x80;
			public static final int SENSOR_GYRO			   	= 0x40;
			public static final int SENSOR_MAG				= 0x20;
			public static final int SENSOR_EXG1_24BIT		= 0x10;
			public static final int SENSOR_EXG2_24BIT		= 0x08;
			public static final int SENSOR_GSR				= 0x04;
			public static final int SENSOR_EXT_A7			= 0x02;
			public static final int SENSOR_EXT_A6			= 0x01;
			public static final int SENSOR_VBATT			= 0x2000;
			public static final int SENSOR_D_ACCEL			= 0x1000;
			public static final int SENSOR_EXT_A15			= 0x0800;
			public static final int SENSOR_INT_A1			= 0x0400;
			public static final int SENSOR_INT_A12			= 0x0200;
			public static final int SENSOR_INT_A13			= 0x0100;
			public static final int SENSOR_INT_A14			= 0x800000;
			public static final int SENSOR_BMP180			= 0x40000;
			public static final int SENSOR_EXG1_16BIT		= 0x100000;
			public static final int SENSOR_EXG2_16BIT		= 0x080000;
			public static final int SENSOR_BRIDGE_AMP		= 0x8000;
		}

		public static final String[] ListofCompatibleSensors={"Low Noise Accelerometer","Wide Range Accelerometer","Gyroscope","Magnetometer","Battery Voltage","External ADC A7","External ADC A6","External ADC A15","Internal ADC A1","Internal ADC A12","Internal ADC A13","Internal ADC A14","Pressure","GSR","EXG1","EXG2","EXG1 16Bit","EXG2 16Bit", "Bridge Amplifier"}; 
		public static final String[] ListofAccelRange={"+/- 2g","+/- 4g","+/- 8g","+/- 16g"};
		public static final Integer[] ListofLSM303DLHCAccelRangeConfigValues={0,1,2,3};
		public static final String[] ListofGyroRange={"+/- 250 dps","+/- 500 dps","+/- 1000 dps","+/- 2000 dps"}; 
		public static final Integer[] ListofMPU9150GyroRangeConfigValues={0,1,2,3};
		public static final String[] ListofMagRange={"+/- 1.3 Ga","+/- 1.9 Ga","+/- 2.5 Ga","+/- 4.0 Ga","+/- 4.7 Ga","+/- 5.6 Ga","+/- 8.1 Ga"}; 
		public static final Integer[] ListofMagRangeConfigValues={1,2,3,4,5,6,7}; // no '0' option
		public static final String[] ListofPressureResolution={"Low","Standard","High","Very High"};
		public static final Integer[] ListofPressureResolutionConfigValues={0,1,2,3};
		
		public static final String[] ListofGSRRange={"10k\u2126 to 56k\u2126","56k\u2126 to 220k\u2126","220k\u2126 to 680k\u2126","680k\u2126 to 4.7M\u2126","Auto"};
		public static final Integer[] ListofGSRRangeConfigValues={0,1,2,3,4};
		public static final String[] ListOfPpgAdcSelection={"Int A13","Int A12"};
		public static final Integer[] ListOfPpgAdcSelectionConfigValues={0,1};
		public static final String[] ListOfPpg1AdcSelection={"Int A13","Int A12"};
		public static final Integer[] ListOfPpg1AdcSelectionConfigValues={0,1};
		public static final String[] ListOfPpg2AdcSelection={"Int A1","Int A14"};
		public static final Integer[] ListOfPpg2AdcSelectionConfigValues={0,1};

		//TODO: base ExG config in ExGConfigBytesDetails rather then here
		public static final String[] ListofDefaultEXG={"ECG","EMG","Test Signal"};
		public static final String[] ListOfExGGain={"6","1","2","3","4","8","12"};
		public static final Integer[] ListOfExGGainConfigValues={0,1,2,3,4,5,6};
		
		public static final String[] ListOfECGReferenceElectrode={"Inverse Wilson CT","Fixed Potential"};
		public static final Integer[] ListOfECGReferenceElectrodeConfigValues={13,0};
		public static final String[] ListOfEMGReferenceElectrode={"Fixed Potential", "Inverse of Ch1"};
		public static final Integer[] ListOfEMGReferenceElectrodeConfigValues={0,3};
		public static final String[] ListOfExGReferenceElectrodeAll={"Fixed Potential","Inverse of Ch1","Inverse Wilson CT","3-Ch Single-ended"};//,"Inputs Shorted"
		public static final Integer[] ListOfExGReferenceElectrodeConfigValuesAll={0,3,13,7};
		public static final String[] ListOfRespReferenceElectrode={"Fixed Potential"};
		public static final Integer[] ListOfRespReferenceElectrodeConfigValues={0};
		public static final String[] ListOfTestReferenceElectrode={"Test Signal"};
		public static final Integer[] ListOfTestReferenceElectrodeConfigValues={0};
		
		public static final String[] ListOfExGLeadOffDetection={"Off","DC Current"};
		public static final Integer[] ListOfExGLeadOffDetectionConfigValues={0,1};
		public static final String[] ListOfExGLeadOffCurrent={"6 nA","22 nA", "6 uA", "22 uA"};
		public static final Integer[] ListOfExGLeadOffCurrentConfigValues={0,1,2,3};
		public static final String[] ListOfExGLeadOffComparator={"Pos:95%-Neg:5%","Pos:92.5%-Neg:7.5%","Pos:90%-Neg:10%","Pos:87.5%-Neg:12.5%","Pos:85%-Neg:15%","Pos:80%-Neg:20%","Pos:75%-Neg:25%","Pos:70%-Neg:30%"};
		public static final Integer[] ListOfExGLeadOffComparatorConfigValues={0,1,2,3,4,5,6,7};
		public static final String[] ListOfExGResolutions={"16-bit","24-bit"};
		public static final Integer[] ListOfExGResolutionsConfigValues={0,1};
		public static final String[] ListOfExGRespirationDetectFreq={"32 kHz","64 kHz"};
		public static final Integer[] ListOfExGRespirationDetectFreqConfigValues={0,1};

		public static final String[] ListOfExGRespirationDetectPhase32khz={"0�","11.25�","22.5�","33.75�","45�","56.25�","67.5�","78.75�","90�","101.25�","112.5�","123.75�","135�","146.25�","157.5�","168.75�"};
		public static final Integer[] ListOfExGRespirationDetectPhase32khzConfigValues={0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15};
		public static final String[] ListOfExGRespirationDetectPhase64khz={"0�","22.5�","45�","67.5�","90�","112.5�","135�","157.5�"};
		public static final Integer[] ListOfExGRespirationDetectPhase64khzConfigValues={0,1,2,3,4,5,6,7};

		public static final String[] ListOfExGRate={"125 Hz","250 Hz","500 Hz","1 kHz","2 kHz","4 kHz","8 kHz"};
		public static final Integer[] ListOfExGRateConfigValues={0,1,2,3,4,5,6};

		public static final String[] ListofBluetoothBaudRates={"115200","1200","2400","4800","9600","19200","38400","57600","230400","460800","921600"};
		public static final Integer[] ListofBluetoothBaudRatesConfigValues={0,1,2,3,4,5,6,7,8,9,10};

		//TODO
		public static final String[] ListofLSM303DLHCAccelRate={"Power-down","1Hz","10Hz","25Hz","50Hz","100Hz","200Hz","400Hz","1344Hz"};
		public static final Integer[] ListofLSM303DLHCAccelRateConfigValues={0,1,2,3,4,5,6,7,9};
		public static final String[] ListofLSM303DLHCAccelRateLpm={"Power-down","1Hz","10Hz","25Hz","50Hz","100Hz","200Hz","400Hz","1620Hz","5376Hz"}; // 1620Hz and 5376Hz are only available in low-power mode
		public static final Integer[] ListofLSM303DLHCAccelRateLpmConfigValues={0,1,2,3,4,5,6,7,8,9};
		
		public static final String[] ListofLSM303DLHCMagRate={"0.75Hz","1.5Hz","3Hz","7.5Hz","15Hz","30Hz","75Hz","220Hz"};
		public static final Integer[] ListofLSM303DLHCMagRateConfigValues={0,1,2,3,4,5,6,7};

		public static final String[] ListofMPU9150AccelRange={"+/- 2g","+/- 4g","+/- 8g","+/- 16g"};
		public static final Integer[] ListofMPU9150AccelRangeConfigValues={0,1,2,3};
		public static final String[] ListofMPU9150MagRate={"10Hz","20Hz","40Hz","50Hz","100Hz"};
		public static final Integer[] ListofMPU9150MagRateConfigValues={0,1,2,3,4};

		public static final String[] ListofMPU9150MplCalibrationOptions={"No Cal","Fast Cal","1s no motion","2s no motion","5s no motion","10s no motion","30s no motion","60s no motion"};
		public static final Integer[] ListofMPU9150MplCalibrationOptionsConfigValues={0,1,2,3,4,5,6,7};
		public static final String[] ListofMPU9150MplLpfOptions={"No LPF","188Hz","98Hz","42Hz","20Hz","10Hz","5Hz"};
		public static final Integer[] ListofMPU9150MplLpfOptionsConfigValues={0,1,2,3,4,5,6};
		public static final String[] ListofMPU9150MplRate={"10Hz","20Hz","40Hz","50Hz","100Hz"};
		public static final Integer[] ListofMPU9150MplRateConfigValues={0,1,2,3,4};

		public static final String[] ListOfOnOff={"On","Off"};
		public static final Integer[] ListOfOnOffConfigValues={0x01,0x00};
		
		public class SensorMapKey{
			/**
			 * Shimmer3 Low-noise analog accelerometer
			 */
			public static final int A_ACCEL = 0;
			/**
			 * Shimmer3 Gyroscope
			 */
			public static final int MPU9150_GYRO = 1;
			/**
			 * Shimmer3 Primary magnetometer
			 */
			public static final int LSM303DLHC_MAG = 2;
			public static final int EXG1_24BIT = 3;
			public static final int EXG2_24BIT = 4;
			public static final int GSR = 5;
			public static final int EXT_EXP_ADC_A6 = 6;
			public static final int EXT_EXP_ADC_A7 = 7;
			public static final int BRIDGE_AMP = 8;
			public static final int RESISTANCE_AMP = 9;
			//public static final int HR = 9;
			public static final int VBATT = 10;
			/**
			 * Shimmer3 Wide-range digital accelerometer
			 */
			public static final int LSM303DLHC_ACCEL = 11;
			public static final int EXT_EXP_ADC_A15 = 12;
			public static final int INT_EXP_ADC_A1 = 13;
			public static final int INT_EXP_ADC_A12 = 14;
			public static final int INT_EXP_ADC_A13 = 15;
			public static final int INT_EXP_ADC_A14 = 16;
			/**
			 * Shimmer3 Alternative accelerometer
			 */
			public static final int MPU9150_ACCEL = 17;
			/**
			 * Shimmer3 Alternative magnetometer
			 */
			public static final int MPU9150_MAG = 18;
			public static final int EXG1_16BIT = 19;
			public static final int EXG2_16BIT = 21;
			public static final int BMP180_PRESSURE = 22;
			//public static final int BMP180_TEMPERATURE = 23; // not yet implemented
			//public static final int MSP430_TEMPERATURE = 24; // not yet implemented
			public static final int MPU9150_TEMP = 25;
			//public static final int LSM303DLHC_TEMPERATURE = 26; // not yet implemented
			//public static final int MPU9150_MPL_TEMPERATURE = 1<<17; // same as SENSOR_SHIMMER3_MPU9150_TEMP 
			public static final int MPU9150_MPL_QUAT_6DOF = 27;
			public static final int MPU9150_MPL_QUAT_9DOF = 28;
			public static final int MPU9150_MPL_EULER_6DOF = 29;
			public static final int MPU9150_MPL_EULER_9DOF = 30;
			public static final int MPU9150_MPL_HEADING = 31;
			public static final int MPU9150_MPL_PEDOMETER = 32;
			public static final int MPU9150_MPL_TAP = 33;
			public static final int MPU9150_MPL_MOTION_ORIENT = 34;
			public static final int MPU9150_MPL_GYRO = 35;
			public static final int MPU9150_MPL_ACCEL = 36;
			public static final int MPU9150_MPL_MAG = 37;
			public static final int MPU9150_MPL_QUAT_6DOF_RAW = 38;
	
			// Combination Channels
			public static final int ECG = 100;
			public static final int EMG = 101;
			public static final int EXG_TEST = 102;
			
			// Derived Channels
			public static final int EXG_RESPIRATION = 103;
			public static final int SKIN_TEMP_PROBE = 104;
	
			// Derived Channels - GSR Board
			public static final int PPG_A12 = 106;
			public static final int PPG_A13 = 107;
			
			// Derived Channels - Proto3 Deluxe Board
			
			public static final int PPG1_A12 = 111;
			public static final int PPG1_A13 = 112;
			public static final int PPG2_A1 = 114;
			public static final int PPG2_A14 = 115;
			public static final int EXG_CUSTOM = 116;
			
			public static final int TIMESTAMP = 150;

			public static final int PPG_DUMMY = 105;
			public static final int PPG1_DUMMY = 110;
			public static final int PPG2_DUMMY = 113;

		}

		// Sensor Options Map
		public class GuiLabelConfig{
			public static final String SHIMMER_USER_ASSIGNED_NAME = "Shimmer Name";
			public static final String TRIAL_NAME = "Trial Name";
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

			public static final String USER_BUTTON_START = "User Button";
			public static final String UNDOCK_START = "Undock/Dock";
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
			public static final String EXG_BYTES = "Bytes";

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
			
			public static final String LSM303DLHC_ACCEL_DEFAULT_CALIB = "Wide Range Accel Default Calibration";
			public static final String MPU9150_GYRO_DEFAULT_CALIB = "Gyro Default Calibration";
			public static final String LSM303DLHC_MAG_DEFAULT_CALIB = "Mag Default Calibration";
			public static final String KXRB8_2042_ACCEL_DEFAULT_CALIB = "Low Noise Accel Default Calibration";

		}

		// GUI Sensor Tiles
		public class GuiLabelSensorTiles{
			public static final String LOW_NOISE_ACCEL = Configuration.Shimmer3.GuiLabelSensors.ACCEL_LN;
			public static final String GYRO = Configuration.Shimmer3.GuiLabelSensors.GYRO;
			public static final String MAG = Configuration.Shimmer3.GuiLabelSensors.MAG;
			public static final String BATTERY_MONITORING = Configuration.Shimmer3.GuiLabelSensors.BATTERY;
			public static final String WIDE_RANGE_ACCEL = Configuration.Shimmer3.GuiLabelSensors.ACCEL_WR;
			public static final String PRESSURE_TEMPERATURE = Configuration.Shimmer3.GuiLabelSensors.PRESS_TEMP_BMP180;
			public static final String EXTERNAL_EXPANSION_ADC = "External Expansion ADCs";
			public static final String GSR = "GSR+";
			public static final String EXG = "ECG/EMG";
			public static final String PROTO3_MINI = "Proto Mini";
			public static final String PROTO3_DELUXE = "Proto Deluxe";
			public static final String PROTO3_DELUXE_SUPP = "PPG";
			public static final String BRIDGE_AMPLIFIER = "Bridge Amplifier+";
			public static final String BRIDGE_AMPLIFIER_SUPP = Configuration.Shimmer3.GuiLabelSensors.SKIN_TEMP_PROBE;
			public static final String HIGH_G_ACCEL = Configuration.Shimmer3.GuiLabelSensors.HIGH_G_ACCEL;
			public static final String INTERNAL_EXPANSION_ADC = "Internal Expansion ADCs";
			public static final String MPU_ACCEL_GYRO_MAG = "MPU 9DoF";
			public static final String MPU_OTHER = "MPU Other";
			//public static final String GPS = "GPS";
		}
		
		//GUI SENSORS
		public class GuiLabelSensors{
			public static final String ACCEL_LN = "Low-Noise Accelerometer";
			public static final String BATTERY = "Battery Voltage";
			public static final String EXT_EXP_A7 = "Ext A7";
			public static final String EXT_EXP_A6 = "Ext A6";
			public static final String EXT_EXP_A15 = "Ext A15";
			public static final String INT_EXP_A12 = "Int A12";
			public static final String INT_EXP_A13 = "Int A13";
			public static final String INT_EXP_A14 = "Int A14";
			public static final String BRIDGE_AMPLIFIER = "Bridge Amp";
			public static final String GSR = "GSR";
			public static final String INT_EXP_A1 = "Int A1";
			public static final String RESISTANCE_AMP = "Resistance Amp";
			public static final String GYRO = "Gyroscope";
			public static final String ACCEL_WR = "Wide-Range Accelerometer";
			public static final String MAG = "Magnetometer";
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
			public static final String EXG_CUSTOM = "Custom";
		}

		//DATABASE NAMES
		//GUI AND EXPORT CHANNELS
		public static class ObjectClusterSensorName{
			public static  String TIMESTAMP = "Timestamp";
			public static  String REAL_TIME_CLOCK = "RealTime";
			public static  String ACCEL_LN_X = "Accel_LN_X";
			public static  String ACCEL_LN_Y = "Accel_LN_Y";
			public static  String ACCEL_LN_Z = "Accel_LN_Z";
			public static  String BATTERY = "Battery";
			public static  String EXT_EXP_A7 = "Ext_Exp_A7";
			public static  String EXT_EXP_A6 = "Ext_Exp_A6";
			public static  String EXT_EXP_A15 = "Ext_Exp_A15";
			public static  String INT_EXP_A12 = "Int_Exp_A12";
			public static  String INT_EXP_A13 = "Int_Exp_A13";
			public static  String INT_EXP_A14 = "Int_Exp_A14";
			public static  String BRIDGE_AMP_HIGH = "Bridge_Amp_High";
			public static  String BRIDGE_AMP_LOW = "Bridge_Amp_Low";
			public static  String GSR = "GSR";
			public static  String INT_EXP_A1 = "Int_Exp_A1";
			public static  String RESISTANCE_AMP = "Resistance_Amp";
			public static  String GYRO_X = "Gyro_X";
			public static  String GYRO_Y = "Gyro_Y";
			public static  String GYRO_Z = "Gyro_Z";
			public static  String ACCEL_WR_X = "Accel_WR_X";
			public static  String ACCEL_WR_Y = "Accel_WR_Y";
			public static  String ACCEL_WR_Z= "Accel_WR_Z";
			public static  String MAG_X = "Mag_X";
			public static  String MAG_Y = "Mag_Y";
			public static  String MAG_Z = "Mag_Z";
			public static  String ACCEL_MPU_X = "Accel_MPU_X";
			public static  String ACCEL_MPU_Y = "Accel_MPU_Y";
			public static  String ACCEL_MPU_Z = "Accel_MPU_Z";
			public static  String MAG_MPU_X = "Mag_MPU_X";
			public static  String MAG_MPU_Y = "Mag_MPU_Y";
			public static  String MAG_MPU_Z = "Mag_MPU_Z";
			public static  String TEMPERATURE_BMP180 = "Temperature_BMP180";
			public static  String PRESSURE_BMP180 = "Pressure_BMP180";
			public static  String EMG_CH1_24BIT = "EMG_CH1_24BIT";
			public static  String EMG_CH2_24BIT = "EMG_CH2_24BIT";
			public static  String EMG_CH1_16BIT = "EMG_CH1_16BIT";
			public static  String EMG_CH2_16BIT = "EMG_CH2_16BIT";
			public static  String ECG_LL_RA_24BIT = "ECG_LL-RA_24BIT";
			public static  String ECG_LA_RA_24BIT = "ECG_LA-RA_24BIT";
			public static  String ECG_LL_RA_16BIT = "ECG_LL-RA_16BIT";
			public static  String ECG_LA_RA_16BIT = "ECG_LA-RA_16BIT";
			public static  String TEST_CHIP1_CH1_24BIT = "Test_CHIP1_CH1_24BIT";
			public static  String TEST_CHIP1_CH2_24BIT = "Test_CHIP1_CH2_24BIT";
			public static  String TEST_CHIP2_CH1_24BIT = "Test_CHIP2_CH1_24BIT";
			public static  String TEST_CHIP2_CH2_24BIT = "Test_CHIP2_CH2_24BIT";
			public static  String TEST_CHIP1_CH1_16BIT = "Test_CHIP1_CH1_16BIT";
			public static  String TEST_CHIP1_CH2_16BIT = "Test_CHIP1_CH2_16BIT";
			public static  String TEST_CHIP2_CH1_16BIT = "Test_CHIP2_CH1_16BIT";
			public static  String TEST_CHIP2_CH2_16BIT = "Test_CHIP2_CH2_16BIT";
			public static  String EXG1_STATUS = "ECG_EMG_Status1";
			public static  String ECG_RESP_24BIT = "ECG_RESP_24BIT";
			public static  String ECG_VX_RL_24BIT = "ECG_Vx-RL_24BIT";
			public static  String ECG_RESP_16BIT = "ECG_RESP_16BIT";
			public static  String ECG_VX_RL_16BIT = "ECG_Vx-RL_16BIT";
			public static  String EXG1_CH1_24BIT = "ExG1_CH1_24BIT";
			public static  String EXG1_CH2_24BIT = "ExG1_CH2_24BIT";
			public static  String EXG1_CH1_16BIT = "ExG1_CH1_16BIT";
			public static  String EXG1_CH2_16BIT = "ExG1_CH2_16BIT";
			public static  String EXG2_CH1_24BIT = "ExG2_CH1_24BIT";
			public static  String EXG2_CH2_24BIT = "ExG2_CH2_24BIT";
			public static  String EXG2_CH1_16BIT = "ExG2_CH1_16BIT";
			public static  String EXG2_CH2_16BIT = "ExG2_CH2_16BIT";
			public static  String EXG2_STATUS = "ECG_EMG_Status2";
			public static  String QUAT_MPL_6DOF_W = "Quat_MPL_6DOF_W";
			public static  String QUAT_MPL_6DOF_X = "Quat_MPL_6DOF_X";
			public static  String QUAT_MPL_6DOF_Y = "Quat_MPL_6DOF_Y";
			public static  String QUAT_MPL_6DOF_Z = "Quat_MPL_6DOF_Z";
			public static  String QUAT_MPL_9DOF_W = "Quat_MPL_9DOF_W";
			public static  String QUAT_MPL_9DOF_X = "Quat_MPL_9DOF_X";
			public static  String QUAT_MPL_9DOF_Y = "Quat_MPL_9DOF_Y";
			public static  String QUAT_MPL_9DOF_Z = "Quat_MPL_9DOF_Z";
			public static  String EULER_MPL_6DOF_X = "Euler_MPL_6DOF_X";
			public static  String EULER_MPL_6DOF_Y = "Euler_MPL_6DOF_Y";
			public static  String EULER_MPL_6DOF_Z = "Euler_MPL_6DOF_Z";
			public static  String EULER_MPL_9DOF_X = "Euler_MPL_9DOF_X";
			public static  String EULER_MPL_9DOF_Y = "Euler_MPL_9DOF_Y";
			public static  String EULER_MPL_9DOF_Z = "Euler_MPL_9DOF_Z";
			public static  String MPL_HEADING = "MPL_heading";
			public static  String MPL_TEMPERATURE = "MPL_Temperature";
			public static  String MPL_PEDOM_CNT = "MPL_Pedom_cnt";
			public static  String MPL_PEDOM_TIME = "MPL_Pedom_Time";
			public static  String TAPDIRANDTAPCNT = "TapDirAndTapCnt";
			public static  String MOTIONANDORIENT = "MotionAndOrient";
			public static  String GYRO_MPU_MPL_X = "Gyro_MPU_MPL_X";
			public static  String GYRO_MPU_MPL_Y = "Gyro_MPU_MPL_Y";
			public static  String GYRO_MPU_MPL_Z = "Gyro_MPU_MPL_Z";
			public static  String ACCEL_MPU_MPL_X = "Accel_MPU_MPL_X";
			public static  String ACCEL_MPU_MPL_Y = "Accel_MPU_MPL_Y";
			public static  String ACCEL_MPU_MPL_Z = "Accel_MPU_MPL_Z";
			public static  String MAG_MPU_MPL_X = "Mag_MPU_MPL_X";
			public static  String MAG_MPU_MPL_Y = "Mag_MPU_MPL_Y";
			public static  String MAG_MPU_MPL_Z = "Mag_MPU_MPL_Z";
			public static  String QUAT_DMP_6DOF_W = "Quat_DMP_6DOF_W";
			public static  String QUAT_DMP_6DOF_X = "Quat_DMP_6DOF_X";
			public static  String QUAT_DMP_6DOF_Y = "Quat_DMP_6DOF_Y";
			public static  String QUAT_DMP_6DOF_Z = "Quat_DMP_6DOF_Z";
			public static  String ECG_TO_HR = "ECGtoHR";
			public static  String PPG_TO_HR = "PPGtoHR";
			public static  String QUAT_MADGE_6DOF_W = "Quat_Madge_6DOF_W";
			public static  String QUAT_MADGE_6DOF_X = "Quat_Madge_6DOF_X";
			public static  String QUAT_MADGE_6DOF_Y = "Quat_Madge_6DOF_Y";
			public static  String QUAT_MADGE_6DOF_Z = "Quat_Madge_6DOF_Z";
			public static  String QUAT_MADGE_9DOF_W = "Quat_Madge_9DOF_W";
			public static  String QUAT_MADGE_9DOF_X = "Quat_Madge_9DOF_X";
			public static  String QUAT_MADGE_9DOF_Y = "Quat_Madge_9DOF_Y";
			public static  String QUAT_MADGE_9DOF_Z = "Quat_Madge_9DOF_Z";
			public static  String EULER_6DOF_A = "Euler_6DOF_A";
			public static  String EULER_6DOF_X = "Euler_6DOF_X";
			public static  String EULER_6DOF_Y = "Euler_6DOF_Y";
			public static  String EULER_6DOF_Z = "Euler_6DOF_Z";
			public static  String EULER_9DOF_A = "Euler_9DOF_A";
			public static  String EULER_9DOF_X = "Euler_9DOF_X";
			public static  String EULER_9DOF_Y = "Euler_9DOF_Y";
			public static  String EULER_9DOF_Z = "Euler_9DOF_Z";
			public static  String AXIS_ANGLE_A = "Axis_Angle_A";
			public static  String AXIS_ANGLE_X = "Axis_Angle_X";
			public static  String AXIS_ANGLE_Y = "Axis_Angle_Y";
			public static  String AXIS_ANGLE_Z = "Axis_Angle_Z";
			public static  String PPG_A12 = "PPG_A12";
			public static  String PPG_A13 = "PPG_A13";
			public static  String PPG1_A12 = "PPG1_A12";
			public static  String PPG1_A13 = "PPG1_A13";
			public static  String PPG2_A1 = "PPG2_A1";
			public static  String PPG2_A14 = "PPG2_A14";
			public static  String REAL_TIME_CLOCK_SYNC = "RealTime_Sync";
			public static  String TIMESTAMP_SYNC = "Timestamp_Sync";
		}
		
		//Names used for parsing the GQ configuration header file 
		public class HeaderFileSensorName{
			public static final String SHIMMER3 = "shimmer3";
			public static final String VBATT = "VBATT";
			public static final String GSR = "GSR";
			public static final String LSM303DLHC_ACCEL = "LSM303DLHC_ACCEL";
		}
		
		
		private static class CompatibilityInfoForMaps{
			public static final int ANY_VERSION = -1;

			// These can be used to enable/disble GUI options depending on what HW, FW, Expansion boards versions are present
			private static final ShimmerVerObject baseAnyIntExpBoardAndFw = 			new ShimmerVerObject(HW_ID.SHIMMER_3,ANY_VERSION,ANY_VERSION,ANY_VERSION,ANY_VERSION,ANY_VERSION);
			private static final ShimmerVerObject baseAnyIntExpBoardAndSdlog = 		new ShimmerVerObject(HW_ID.SHIMMER_3,FW_ID.SHIMMER3.SDLOG,ANY_VERSION,ANY_VERSION,ANY_VERSION,ANY_VERSION);
			private static final ShimmerVerObject baseAnyIntExpBoardAndBtStream = 	new ShimmerVerObject(HW_ID.SHIMMER_3,FW_ID.SHIMMER3.BTSTREAM,ANY_VERSION,ANY_VERSION,ANY_VERSION,ANY_VERSION);
			private static final ShimmerVerObject baseAnyIntExpBoardAndLogAndStream = new ShimmerVerObject(HW_ID.SHIMMER_3,FW_ID.SHIMMER3.LOGANDSTREAM,ANY_VERSION,ANY_VERSION,ANY_VERSION,ANY_VERSION);

			private static final ShimmerVerObject baseNoIntExpBoardSdLog = 	new ShimmerVerObject(HW_ID.SHIMMER_3,FW_ID.SHIMMER3.SDLOG,0,8,0,ShimmerVerDetails.EXP_BRD_NONE_ID);

			private static final ShimmerVerObject baseSdLog = 				new ShimmerVerObject(HW_ID.SHIMMER_3,FW_ID.SHIMMER3.SDLOG,0,8,0,ANY_VERSION);
			private static final ShimmerVerObject baseSdLogMpl = 				new ShimmerVerObject(HW_ID.SHIMMER_3,FW_ID.SHIMMER3.SDLOG,0,10,1,ANY_VERSION);
			private static final ShimmerVerObject baseBtStream = 			new ShimmerVerObject(HW_ID.SHIMMER_3,FW_ID.SHIMMER3.BTSTREAM,0,5,0,ANY_VERSION);
			private static final ShimmerVerObject baseLogAndStream = 		new ShimmerVerObject(HW_ID.SHIMMER_3,FW_ID.SHIMMER3.LOGANDSTREAM,0,3,3,ANY_VERSION);
			
			private static final ShimmerVerObject baseExgSdLog = 			new ShimmerVerObject(HW_ID.SHIMMER_3,FW_ID.SHIMMER3.SDLOG,0,8,0,HW_ID_SR_CODES.EXP_BRD_EXG); 
			private static final ShimmerVerObject baseExgUnifiedSdLog = 		new ShimmerVerObject(HW_ID.SHIMMER_3,FW_ID.SHIMMER3.SDLOG,0,8,0,HW_ID_SR_CODES.EXP_BRD_EXG_UNIFIED);
			private static final ShimmerVerObject baseExgBtStream = 			new ShimmerVerObject(HW_ID.SHIMMER_3,FW_ID.SHIMMER3.BTSTREAM,0,5,0,HW_ID_SR_CODES.EXP_BRD_EXG);
			private static final ShimmerVerObject baseExgUnifiedBtStream = 	new ShimmerVerObject(HW_ID.SHIMMER_3,FW_ID.SHIMMER3.BTSTREAM,0,5,0,HW_ID_SR_CODES.EXP_BRD_EXG_UNIFIED);
			private static final ShimmerVerObject baseExgLogAndStream = 		new ShimmerVerObject(HW_ID.SHIMMER_3,FW_ID.SHIMMER3.LOGANDSTREAM,0,3,3,HW_ID_SR_CODES.EXP_BRD_EXG);
			private static final ShimmerVerObject baseExgUnifiedLogAndStream = new ShimmerVerObject(HW_ID.SHIMMER_3,FW_ID.SHIMMER3.LOGANDSTREAM,0,3,3,HW_ID_SR_CODES.EXP_BRD_EXG_UNIFIED);
			
			private static final ShimmerVerObject baseGsrSdLog = 			new ShimmerVerObject(HW_ID.SHIMMER_3,FW_ID.SHIMMER3.SDLOG,0,8,0,HW_ID_SR_CODES.EXP_BRD_GSR);
			private static final ShimmerVerObject baseGsrUnifiedSdLog = 		new ShimmerVerObject(HW_ID.SHIMMER_3,FW_ID.SHIMMER3.SDLOG,0,8,0,HW_ID_SR_CODES.EXP_BRD_GSR_UNIFIED);
			private static final ShimmerVerObject baseGsrBtStream = 			new ShimmerVerObject(HW_ID.SHIMMER_3,FW_ID.SHIMMER3.BTSTREAM,0,5,0,HW_ID_SR_CODES.EXP_BRD_GSR);
			private static final ShimmerVerObject baseGsrUnifiedBtStream = 	new ShimmerVerObject(HW_ID.SHIMMER_3,FW_ID.SHIMMER3.BTSTREAM,0,5,0,HW_ID_SR_CODES.EXP_BRD_GSR_UNIFIED);
			private static final ShimmerVerObject baseGsrLogAndStream = 		new ShimmerVerObject(HW_ID.SHIMMER_3,FW_ID.SHIMMER3.LOGANDSTREAM,0,3,3,HW_ID_SR_CODES.EXP_BRD_GSR);
			private static final ShimmerVerObject baseGsrUnifiedLogAndStream = 	new ShimmerVerObject(HW_ID.SHIMMER_3,FW_ID.SHIMMER3.LOGANDSTREAM,0,3,3,HW_ID_SR_CODES.EXP_BRD_GSR_UNIFIED);
			private static final ShimmerVerObject baseGsrGq = 				new ShimmerVerObject(HW_ID.SHIMMER_3,FW_ID.SHIMMER3.GQ_GSR,ANY_VERSION,ANY_VERSION,ANY_VERSION,HW_ID_SR_CODES.EXP_BRD_GSR);
			private static final ShimmerVerObject baseGsrUnifiedGq = 		new ShimmerVerObject(HW_ID.SHIMMER_3,FW_ID.SHIMMER3.GQ_GSR,ANY_VERSION,ANY_VERSION,ANY_VERSION,HW_ID_SR_CODES.EXP_BRD_GSR_UNIFIED);

			private static final ShimmerVerObject baseBrAmpSdLog = 			new ShimmerVerObject(HW_ID.SHIMMER_3,FW_ID.SHIMMER3.SDLOG,0,8,0,HW_ID_SR_CODES.EXP_BRD_BR_AMP);
			private static final ShimmerVerObject baseBrAmpUnifiedSdLog = 	new ShimmerVerObject(HW_ID.SHIMMER_3,FW_ID.SHIMMER3.SDLOG,0,8,0,HW_ID_SR_CODES.EXP_BRD_BR_AMP_UNIFIED);
			private static final ShimmerVerObject baseBrAmpBtStream = 		new ShimmerVerObject(HW_ID.SHIMMER_3,FW_ID.SHIMMER3.BTSTREAM,0,5,0,HW_ID_SR_CODES.EXP_BRD_BR_AMP);
			private static final ShimmerVerObject baseBrAmpUnifiedBtStream = new ShimmerVerObject(HW_ID.SHIMMER_3,FW_ID.SHIMMER3.BTSTREAM,0,5,0,HW_ID_SR_CODES.EXP_BRD_BR_AMP_UNIFIED);
			private static final ShimmerVerObject baseBrAmpLogAndStream = 	new ShimmerVerObject(HW_ID.SHIMMER_3,FW_ID.SHIMMER3.LOGANDSTREAM,0,3,3,HW_ID_SR_CODES.EXP_BRD_BR_AMP);
			private static final ShimmerVerObject baseBrAmpUnifiedLogAndStream = new ShimmerVerObject(HW_ID.SHIMMER_3,FW_ID.SHIMMER3.LOGANDSTREAM,0,3,3,HW_ID_SR_CODES.EXP_BRD_BR_AMP_UNIFIED);
			
			private static final ShimmerVerObject baseProto3MiniSdLog = 			new ShimmerVerObject(HW_ID.SHIMMER_3,FW_ID.SHIMMER3.SDLOG,0,8,0,HW_ID_SR_CODES.EXP_BRD_PROTO3_MINI);
			private static final ShimmerVerObject baseProto3MiniBtStream = 		new ShimmerVerObject(HW_ID.SHIMMER_3,FW_ID.SHIMMER3.BTSTREAM,0,5,0,HW_ID_SR_CODES.EXP_BRD_PROTO3_MINI);
			private static final ShimmerVerObject baseProto3MiniLogAndStream = 	new ShimmerVerObject(HW_ID.SHIMMER_3,FW_ID.SHIMMER3.LOGANDSTREAM,0,3,3,HW_ID_SR_CODES.EXP_BRD_PROTO3_MINI);

			private static final ShimmerVerObject baseProto3DeluxeSdLog = 		new ShimmerVerObject(HW_ID.SHIMMER_3,FW_ID.SHIMMER3.SDLOG,0,8,0,HW_ID_SR_CODES.EXP_BRD_PROTO3_DELUXE);
			private static final ShimmerVerObject baseProto3DeluxeBtStream = 	new ShimmerVerObject(HW_ID.SHIMMER_3,FW_ID.SHIMMER3.BTSTREAM,0,5,0,HW_ID_SR_CODES.EXP_BRD_PROTO3_DELUXE);
			private static final ShimmerVerObject baseProto3DeluxeLogAndStream =	new ShimmerVerObject(HW_ID.SHIMMER_3,FW_ID.SHIMMER3.LOGANDSTREAM,0,3,3,HW_ID_SR_CODES.EXP_BRD_PROTO3_DELUXE);

			private static final ShimmerVerObject baseHighGAccelSdLog = 			new ShimmerVerObject(HW_ID.SHIMMER_3,FW_ID.SHIMMER3.SDLOG,0,8,0,HW_ID_SR_CODES.EXP_BRD_HIGH_G_ACCEL);
			private static final ShimmerVerObject baseHighGAccelBtStream = 		new ShimmerVerObject(HW_ID.SHIMMER_3,FW_ID.SHIMMER3.BTSTREAM,0,5,0,HW_ID_SR_CODES.EXP_BRD_HIGH_G_ACCEL);
			private static final ShimmerVerObject baseHighGAccelLogAndStream = 	new ShimmerVerObject(HW_ID.SHIMMER_3,FW_ID.SHIMMER3.LOGANDSTREAM,0,3,3,HW_ID_SR_CODES.EXP_BRD_HIGH_G_ACCEL);

			private static final List<ShimmerVerObject> listOfCompatibleVersionInfoExg = Arrays.asList(
					baseExgSdLog, baseExgBtStream, baseExgLogAndStream,  
					baseExgUnifiedSdLog, baseExgUnifiedBtStream, baseExgUnifiedLogAndStream);
			
			private static final List<ShimmerVerObject> listOfCompatibleVersionInfoSdLog = Arrays.asList(baseSdLog);
			private static final List<ShimmerVerObject> listOfCompatibleVersionInfoAnyExpBoardAndFw = Arrays.asList(baseAnyIntExpBoardAndFw);
			private static final List<ShimmerVerObject> listOfCompatibleVersionInfoAnyExpBoardStandardFW = Arrays.asList(
					baseAnyIntExpBoardAndSdlog,baseAnyIntExpBoardAndBtStream,baseAnyIntExpBoardAndLogAndStream);

			private static final List<ShimmerVerObject> listOfCompatibleVersionInfoGsr = Arrays.asList(
					baseGsrSdLog, baseGsrBtStream, baseGsrLogAndStream, baseGsrGq,
					baseGsrUnifiedSdLog,  baseGsrUnifiedBtStream, baseGsrUnifiedLogAndStream, baseGsrUnifiedGq);
			
			private static final List<ShimmerVerObject> listOfCompatibleVersionInfoBrAmp = Arrays.asList(
					baseBrAmpSdLog, baseBrAmpBtStream, baseBrAmpLogAndStream,  
					baseBrAmpUnifiedSdLog,  baseBrAmpUnifiedBtStream, baseBrAmpUnifiedLogAndStream);

			private static final List<ShimmerVerObject> listOfCompatibleVersionInfoProto3Mini = Arrays.asList(
					baseProto3MiniSdLog, baseProto3MiniBtStream, baseProto3MiniLogAndStream);
			
			private static final List<ShimmerVerObject> listOfCompatibleVersionInfoProto3Deluxe = Arrays.asList(
					baseProto3DeluxeSdLog, baseProto3DeluxeBtStream, baseProto3DeluxeLogAndStream);
			
			private static final List<ShimmerVerObject> listOfCompatibleVersionInfoIntExpA1 = Arrays.asList(
					baseProto3MiniSdLog, baseProto3MiniBtStream, baseProto3MiniLogAndStream, 
					baseProto3DeluxeSdLog, baseProto3DeluxeBtStream, baseProto3DeluxeLogAndStream, 
					baseHighGAccelSdLog, baseHighGAccelBtStream, baseHighGAccelLogAndStream);
			
			private static final List<ShimmerVerObject> listOfCompatibleVersionInfoIntExpA12 = Arrays.asList(
					baseProto3MiniSdLog, baseProto3MiniBtStream, baseProto3MiniLogAndStream, 
					baseProto3DeluxeSdLog, baseProto3DeluxeBtStream, baseProto3DeluxeLogAndStream, 
					baseGsrSdLog, baseGsrBtStream, baseGsrLogAndStream, 
					baseGsrUnifiedSdLog, baseGsrUnifiedBtStream, baseGsrUnifiedLogAndStream,
					baseHighGAccelSdLog, baseHighGAccelBtStream, baseHighGAccelLogAndStream);
			
			private static final List<ShimmerVerObject> listOfCompatibleVersionInfoIntExpA13 = Arrays.asList(
					baseProto3MiniSdLog, baseProto3MiniBtStream, baseProto3MiniLogAndStream, 
					baseProto3DeluxeSdLog, baseProto3DeluxeBtStream, baseProto3DeluxeLogAndStream, 
					baseHighGAccelSdLog, baseHighGAccelBtStream, baseHighGAccelLogAndStream, 
					baseGsrSdLog, baseGsrBtStream, baseGsrLogAndStream, 
					baseGsrUnifiedSdLog, baseGsrUnifiedBtStream, baseGsrUnifiedLogAndStream 
					);

			private static final List<ShimmerVerObject> listOfCompatibleVersionInfoIntExpA14 = Arrays.asList(
					baseProto3MiniSdLog, baseProto3MiniBtStream, baseProto3MiniLogAndStream, 
					baseProto3DeluxeSdLog, baseProto3DeluxeBtStream, baseProto3DeluxeLogAndStream, 
					baseHighGAccelSdLog, baseHighGAccelBtStream, baseHighGAccelLogAndStream 
					);
			
			private static final List<ShimmerVerObject> listOfCompatibleVersionInfoStreaming = Arrays.asList(
					baseBtStream, baseLogAndStream);

			private static final List<ShimmerVerObject> listOfCompatibleVersionInfoLogging = Arrays.asList(
					baseSdLog, baseLogAndStream);
			
			private static final List<ShimmerVerObject> listOfCompatibleVersionInfoRespiration = Arrays.asList(
					baseExgUnifiedSdLog, baseExgUnifiedBtStream, baseExgUnifiedLogAndStream);

			private static final List<ShimmerVerObject> listOfCompatibleVersionInfoHighGAccel = Arrays.asList(
					baseHighGAccelSdLog,baseHighGAccelBtStream,baseHighGAccelLogAndStream);
			
			private static final List<ShimmerVerObject> listOfCompatibleVersionInfoMPLSensors = Arrays.asList(baseSdLogMpl);
		}


		
	    public static final Map<Integer, SensorDetails> mSensorMapRef;
	    static {
	        Map<Integer, SensorDetails> aMap = new LinkedHashMap<Integer, SensorDetails>();

//			mSensorMap.put(Configuration.Shimmer3.SensorMapKey.TIMESTAMP, new SensorDetails(true, 0, 0, Shimmer3.ObjectClusterSensorName.TIMESTAMP));
	        
			// Assemble the channel map
			// NV_SENSORS0
			long streamingByteIndex = 0;
			long logHeaderByteIndex = 0;
			aMap.put(Configuration.Shimmer3.SensorMapKey.A_ACCEL, new SensorDetails(0x80<<(streamingByteIndex*8), 0x80<<(logHeaderByteIndex*8), Shimmer3.GuiLabelSensors.ACCEL_LN));
			aMap.put(Configuration.Shimmer3.SensorMapKey.MPU9150_GYRO, new SensorDetails(0x40<<(streamingByteIndex*8), 0x40<<(logHeaderByteIndex*8), Shimmer3.GuiLabelSensors.GYRO));
			aMap.put(Configuration.Shimmer3.SensorMapKey.LSM303DLHC_MAG, new SensorDetails(0x20<<(streamingByteIndex*8), 0x20<<(logHeaderByteIndex*8), Shimmer3.GuiLabelSensors.MAG));
			aMap.put(Configuration.Shimmer3.SensorMapKey.EXG1_24BIT, new SensorDetails(0x10<<(streamingByteIndex*8), 0x10<<(logHeaderByteIndex*8), Configuration.Shimmer3.GuiLabelSensors.EXG1_24BIT));
			aMap.put(Configuration.Shimmer3.SensorMapKey.EXG2_24BIT, new SensorDetails(0x08<<(streamingByteIndex*8), 0x08<<(logHeaderByteIndex*8), Configuration.Shimmer3.GuiLabelSensors.EXG2_24BIT));
			aMap.put(Configuration.Shimmer3.SensorMapKey.GSR, new SensorDetails(0x04<<(streamingByteIndex*8), 0x04<<(logHeaderByteIndex*8), Shimmer3.GuiLabelSensors.GSR));
			aMap.put(Configuration.Shimmer3.SensorMapKey.EXT_EXP_ADC_A7, new SensorDetails(0x02<<(streamingByteIndex*8), 0x02<<(logHeaderByteIndex*8), Shimmer3.GuiLabelSensors.EXT_EXP_A7));
			aMap.put(Configuration.Shimmer3.SensorMapKey.EXT_EXP_ADC_A6, new SensorDetails(0x01<<(streamingByteIndex*8), 0x01<<(logHeaderByteIndex*8), Shimmer3.GuiLabelSensors.EXT_EXP_A6));
			
			// NV_SENSORS1
			streamingByteIndex = 1;
			logHeaderByteIndex = 1;
			aMap.put(Configuration.Shimmer3.SensorMapKey.BRIDGE_AMP, new SensorDetails(0x80<<(streamingByteIndex*8), 0x80<<(logHeaderByteIndex*8), Shimmer3.GuiLabelSensors.BRIDGE_AMPLIFIER));
			//shimmerChannels.put(, new ChannelDetails(false, 0x40<<(streamingByteIndex*8), 0x40<<(logHeaderByteIndex*8), "")); // unused? - new PPG bit might be here now
			aMap.put(Configuration.Shimmer3.SensorMapKey.VBATT, new SensorDetails(0x20<<(streamingByteIndex*8), 0x20<<(logHeaderByteIndex*8), Shimmer3.GuiLabelSensors.BATTERY));
			aMap.put(Configuration.Shimmer3.SensorMapKey.LSM303DLHC_ACCEL, new SensorDetails(0x10<<(streamingByteIndex*8), 0x10<<(logHeaderByteIndex*8), Shimmer3.GuiLabelSensors.ACCEL_WR));
			aMap.put(Configuration.Shimmer3.SensorMapKey.EXT_EXP_ADC_A15, new SensorDetails(0x08<<(streamingByteIndex*8), 0x08<<(logHeaderByteIndex*8), Shimmer3.GuiLabelSensors.EXT_EXP_A15));
			aMap.put(Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A1, new SensorDetails(0x04<<(streamingByteIndex*8), 0x04<<(logHeaderByteIndex*8), Shimmer3.GuiLabelSensors.INT_EXP_A1));
			aMap.put(Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A12, new SensorDetails(0x02<<(streamingByteIndex*8), 0x02<<(logHeaderByteIndex*8), Shimmer3.GuiLabelSensors.INT_EXP_A12));
			aMap.put(Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A13, new SensorDetails(0x01<<(streamingByteIndex*8), 0x01<<(logHeaderByteIndex*8), Shimmer3.GuiLabelSensors.INT_EXP_A13));

			// NV_SENSORS2
			streamingByteIndex = 2;
			logHeaderByteIndex = 2;
			aMap.put(Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A14, new SensorDetails(0x80<<(streamingByteIndex*8), 0x80<<(logHeaderByteIndex*8), Shimmer3.GuiLabelSensors.INT_EXP_A14));
			aMap.put(Configuration.Shimmer3.SensorMapKey.MPU9150_ACCEL, new SensorDetails(0x40<<(streamingByteIndex*8), 0x40<<(logHeaderByteIndex*8), Shimmer3.GuiLabelSensors.ACCEL_MPU));
			aMap.put(Configuration.Shimmer3.SensorMapKey.MPU9150_MAG, new SensorDetails(0x20<<(streamingByteIndex*8), 0x20<<(logHeaderByteIndex*8), Shimmer3.GuiLabelSensors.MAG_MPU));
			aMap.put(Configuration.Shimmer3.SensorMapKey.EXG1_16BIT, new SensorDetails(0x10<<(streamingByteIndex*8), 0x10<<(logHeaderByteIndex*8), Configuration.Shimmer3.GuiLabelSensors.EXG1_16BIT));
			aMap.put(Configuration.Shimmer3.SensorMapKey.EXG2_16BIT, new SensorDetails(0x08<<(streamingByteIndex*8), 0x08<<(logHeaderByteIndex*8), Configuration.Shimmer3.GuiLabelSensors.EXG2_16BIT));
			aMap.put(Configuration.Shimmer3.SensorMapKey.BMP180_PRESSURE, new SensorDetails(0x04<<(streamingByteIndex*8), 0x04<<(logHeaderByteIndex*8), Shimmer3.GuiLabelSensors.PRESS_TEMP_BMP180));
			aMap.put(Configuration.Shimmer3.SensorMapKey.MPU9150_TEMP, new SensorDetails(0x02<<(streamingByteIndex*8), 0x02<<(logHeaderByteIndex*8), Shimmer3.GuiLabelSensors.MPL_TEMPERATURE));
			//shimmerChannels.put(SENSOR_SHIMMER3_MSP430_TEMPERATURE, new ChannelDetails(false, 0x01<<(btStreamByteIndex*8), 0x01<<(sDHeaderByteIndex*8), "")); // not yet implemented
			//shimmerChannels.put(SENSOR_SHIMMER3_LSM303DLHC_TEMPERATURE, new ChannelDetails(false, 0x01<<(btStreamByteIndex*8), 0x01<<(sDHeaderByteIndex*8), "")); // not yet implemented

			// NV_SENSORS3				
			streamingByteIndex = 3;
			logHeaderByteIndex = 3;
			aMap.put(Configuration.Shimmer3.SensorMapKey.MPU9150_MPL_QUAT_6DOF, new SensorDetails((long)0, (long)0x80<<(logHeaderByteIndex*8), Shimmer3.GuiLabelSensors.QUAT_MPL_6DOF));
			aMap.put(Configuration.Shimmer3.SensorMapKey.MPU9150_MPL_QUAT_9DOF, new SensorDetails((long)0, (long)0x40<<(logHeaderByteIndex*8), Shimmer3.GuiLabelSensors.QUAT_MPL_9DOF));
			aMap.put(Configuration.Shimmer3.SensorMapKey.MPU9150_MPL_EULER_6DOF, new SensorDetails((long)0, (long)0x20<<(logHeaderByteIndex*8), Shimmer3.GuiLabelSensors.EULER_ANGLES_6DOF));
			aMap.put(Configuration.Shimmer3.SensorMapKey.MPU9150_MPL_EULER_9DOF, new SensorDetails((long)0, (long)0x10<<(logHeaderByteIndex*8), Shimmer3.GuiLabelSensors.EULER_ANGLES_9DOF));
			aMap.put(Configuration.Shimmer3.SensorMapKey.MPU9150_MPL_HEADING, new SensorDetails((long)0, (long)0x08<<(logHeaderByteIndex*8), Shimmer3.GuiLabelSensors.MPL_HEADING));
			aMap.put(Configuration.Shimmer3.SensorMapKey.MPU9150_MPL_PEDOMETER, new SensorDetails((long)0, (long)0x04<<(logHeaderByteIndex*8), Shimmer3.GuiLabelSensors.MPL_PEDOM_CNT));
			aMap.put(Configuration.Shimmer3.SensorMapKey.MPU9150_MPL_TAP, new SensorDetails((long)0, (long)0x02<<(logHeaderByteIndex*8), Shimmer3.GuiLabelSensors.MPL_TAPDIRANDTAPCNT));
			aMap.put(Configuration.Shimmer3.SensorMapKey.MPU9150_MPL_MOTION_ORIENT, new SensorDetails((long)0, (long)0x01<<(logHeaderByteIndex*8), Shimmer3.GuiLabelSensors.MPL_MOTIONANDORIENT));

			// NV_SENSORS4
			streamingByteIndex = 4;
			logHeaderByteIndex = 4;
			aMap.put(Configuration.Shimmer3.SensorMapKey.MPU9150_MPL_GYRO, new SensorDetails((long)0, (long)0x80<<(logHeaderByteIndex*8), Shimmer3.GuiLabelSensors.GYRO_MPU_MPL));
			aMap.put(Configuration.Shimmer3.SensorMapKey.MPU9150_MPL_ACCEL, new SensorDetails((long)0, (long)0x40<<(logHeaderByteIndex*8), Shimmer3.GuiLabelSensors.ACCEL_MPU_MPL));
			aMap.put(Configuration.Shimmer3.SensorMapKey.MPU9150_MPL_MAG, new SensorDetails((long)0, (long)0x20<<(logHeaderByteIndex*8), Shimmer3.GuiLabelSensors.MAG_MPU_MPL));
			aMap.put(Configuration.Shimmer3.SensorMapKey.MPU9150_MPL_QUAT_6DOF_RAW, new SensorDetails((long)0, (long)0x10<<(logHeaderByteIndex*8), Shimmer3.GuiLabelSensors.QUAT_DMP_6DOF));
			//shimmerChannels.put(, new ChannelDetails(false, 0, 0x08<<(loggingHeaderByteIndex*8), "")); // unused
			//shimmerChannels.put(, new ChannelDetails(false, 0, 0x04<<(loggingHeaderByteIndex*8), "")); // unused
			//shimmerChannels.put(, new ChannelDetails(false, 0, 0x02<<(loggingHeaderByteIndex*8), "")); // unused
			//shimmerChannels.put(, new ChannelDetails(false, 0, 0x01<<(loggingHeaderByteIndex*8), "")); // unused
			
			aMap.put(Configuration.Shimmer3.SensorMapKey.ECG, new SensorDetails(0, 0, Shimmer3.GuiLabelSensors.ECG));
			aMap.put(Configuration.Shimmer3.SensorMapKey.EXG_TEST, new SensorDetails(0, 0, Shimmer3.GuiLabelSensors.EXG_TEST));
			aMap.put(Configuration.Shimmer3.SensorMapKey.EXG_RESPIRATION, new SensorDetails(0, 0, Configuration.Shimmer3.GuiLabelSensors.EXG_RESPIRATION));
			aMap.put(Configuration.Shimmer3.SensorMapKey.EMG, new SensorDetails(0, 0, Shimmer3.GuiLabelSensors.EMG));
			aMap.put(Configuration.Shimmer3.SensorMapKey.EXG_CUSTOM, new SensorDetails(0, 0, Shimmer3.GuiLabelSensors.EXG_CUSTOM));

			// Derived Channels - Bridge Amp Board
			aMap.put(Configuration.Shimmer3.SensorMapKey.RESISTANCE_AMP, new SensorDetails(0, 0, Shimmer3.GuiLabelSensors.RESISTANCE_AMP));
			aMap.get(Configuration.Shimmer3.SensorMapKey.RESISTANCE_AMP).mSensorBitmapIDSDLogHeader = aMap.get(Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A1).mSensorBitmapIDSDLogHeader;
			aMap.get(Configuration.Shimmer3.SensorMapKey.RESISTANCE_AMP).mSensorBitmapIDStreaming = aMap.get(Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A1).mSensorBitmapIDStreaming;
//			aMap.get(Configuration.Shimmer3.SensorMapKey.RESISTANCE_AMP).mDerivedSensorBitmapID = infoMemMap.maskDerivedChannelResAmp;
			aMap.put(Configuration.Shimmer3.SensorMapKey.SKIN_TEMP_PROBE, new SensorDetails(0, 0, Shimmer3.GuiLabelSensors.SKIN_TEMP_PROBE));
			aMap.get(Configuration.Shimmer3.SensorMapKey.SKIN_TEMP_PROBE).mSensorBitmapIDSDLogHeader = aMap.get(Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A1).mSensorBitmapIDSDLogHeader;
			aMap.get(Configuration.Shimmer3.SensorMapKey.SKIN_TEMP_PROBE).mSensorBitmapIDStreaming = aMap.get(Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A1).mSensorBitmapIDStreaming;
//			aMap.get(Configuration.Shimmer3.SensorMapKey.SKIN_TEMP_PROBE).mDerivedSensorBitmapID = infoMemMap.maskDerivedChannelSkinTemp;


//			// Derived Channels - GSR Board
//			aMap.put(Configuration.Shimmer3.SensorMapKey.PPG_DUMMY, new SensorDetails(0, 0, Configuration.Shimmer3.GuiLabelSensors.PPG_DUMMY));
//			aMap.get(Configuration.Shimmer3.SensorMapKey.PPG_DUMMY).mMapOfChildSensorDetails.put(Configuration.Shimmer3.GuiLabelSensors.PPG_A12 ,new SensorDetails(0, 0, Configuration.Shimmer3.GuiLabelSensors.PPG_A12));
//			aMap.get(Configuration.Shimmer3.SensorMapKey.PPG_DUMMY).mMapOfChildSensorDetails.get(Configuration.Shimmer3.GuiLabelSensors.PPG_A12).mSensorBitmapIDStreaming = aMap.get(Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A12).mSensorBitmapIDStreaming;
//			aMap.get(Configuration.Shimmer3.SensorMapKey.PPG_DUMMY).mMapOfChildSensorDetails.get(Configuration.Shimmer3.GuiLabelSensors.PPG_A12).mSensorBitmapIDSDLogHeader = aMap.get(Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A12).mSensorBitmapIDSDLogHeader;
//			aMap.get(Configuration.Shimmer3.SensorMapKey.PPG_DUMMY).mMapOfChildSensorDetails.put(Configuration.Shimmer3.GuiLabelSensors.PPG_A13 ,new SensorDetails(0, 0, Configuration.Shimmer3.GuiLabelSensors.PPG_A13));
//			aMap.get(Configuration.Shimmer3.SensorMapKey.PPG_DUMMY).mMapOfChildSensorDetails.get(Configuration.Shimmer3.GuiLabelSensors.PPG_A13).mSensorBitmapIDStreaming = aMap.get(Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A13).mSensorBitmapIDStreaming;
//			aMap.get(Configuration.Shimmer3.SensorMapKey.PPG_DUMMY).mMapOfChildSensorDetails.get(Configuration.Shimmer3.GuiLabelSensors.PPG_A13).mSensorBitmapIDSDLogHeader = aMap.get(Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A13).mSensorBitmapIDSDLogHeader;
//			
//			// Derived Channels - Proto3 Board
//			aMap.put(Configuration.Shimmer3.SensorMapKey.PPG1_DUMMY, new SensorDetails(0, 0, Configuration.Shimmer3.GuiLabelSensors.PPG1_DUMMY));
//			aMap.get(Configuration.Shimmer3.SensorMapKey.PPG1_DUMMY).mMapOfChildSensorDetails.put(Configuration.Shimmer3.GuiLabelSensors.PPG1_A12 ,new SensorDetails(0, 0, Configuration.Shimmer3.GuiLabelSensors.PPG1_A12));
//			aMap.get(Configuration.Shimmer3.SensorMapKey.PPG1_DUMMY).mMapOfChildSensorDetails.get(Configuration.Shimmer3.GuiLabelSensors.PPG1_A12).mSensorBitmapIDStreaming = aMap.get(Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A12).mSensorBitmapIDStreaming;
//			aMap.get(Configuration.Shimmer3.SensorMapKey.PPG1_DUMMY).mMapOfChildSensorDetails.get(Configuration.Shimmer3.GuiLabelSensors.PPG1_A12).mSensorBitmapIDSDLogHeader = aMap.get(Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A12).mSensorBitmapIDSDLogHeader;
//			aMap.get(Configuration.Shimmer3.SensorMapKey.PPG1_DUMMY).mMapOfChildSensorDetails.put(Configuration.Shimmer3.GuiLabelSensors.PPG1_A13 ,new SensorDetails(0, 0, Configuration.Shimmer3.GuiLabelSensors.PPG1_A13));
//			aMap.get(Configuration.Shimmer3.SensorMapKey.PPG1_DUMMY).mMapOfChildSensorDetails.get(Configuration.Shimmer3.GuiLabelSensors.PPG1_A13).mSensorBitmapIDStreaming = aMap.get(Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A13).mSensorBitmapIDStreaming;
//			aMap.get(Configuration.Shimmer3.SensorMapKey.PPG1_DUMMY).mMapOfChildSensorDetails.get(Configuration.Shimmer3.GuiLabelSensors.PPG1_A13).mSensorBitmapIDSDLogHeader = aMap.get(Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A13).mSensorBitmapIDSDLogHeader;
//
//			aMap.put(Configuration.Shimmer3.SensorMapKey.PPG2_DUMMY, new SensorDetails(0, 0, Configuration.Shimmer3.GuiLabelSensors.PPG2_DUMMY));
//			aMap.get(Configuration.Shimmer3.SensorMapKey.PPG2_DUMMY).mMapOfChildSensorDetails.put(Configuration.Shimmer3.GuiLabelSensors.PPG2_A1 ,new SensorDetails(0, 0, Configuration.Shimmer3.GuiLabelSensors.PPG2_A1));
//			aMap.get(Configuration.Shimmer3.SensorMapKey.PPG2_DUMMY).mMapOfChildSensorDetails.get(Configuration.Shimmer3.GuiLabelSensors.PPG2_A1).mSensorBitmapIDStreaming = aMap.get(Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A1).mSensorBitmapIDStreaming;
//			aMap.get(Configuration.Shimmer3.SensorMapKey.PPG2_DUMMY).mMapOfChildSensorDetails.get(Configuration.Shimmer3.GuiLabelSensors.PPG2_A1).mSensorBitmapIDSDLogHeader = aMap.get(Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A1).mSensorBitmapIDSDLogHeader;
//			aMap.get(Configuration.Shimmer3.SensorMapKey.PPG2_DUMMY).mMapOfChildSensorDetails.put(Configuration.Shimmer3.GuiLabelSensors.PPG2_A14 ,new SensorDetails(0, 0, Configuration.Shimmer3.GuiLabelSensors.PPG2_A14));
//			aMap.get(Configuration.Shimmer3.SensorMapKey.PPG2_DUMMY).mMapOfChildSensorDetails.get(Configuration.Shimmer3.GuiLabelSensors.PPG2_A14).mSensorBitmapIDStreaming = aMap.get(Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A14).mSensorBitmapIDStreaming;
//			aMap.get(Configuration.Shimmer3.SensorMapKey.PPG2_DUMMY).mMapOfChildSensorDetails.get(Configuration.Shimmer3.GuiLabelSensors.PPG2_A14).mSensorBitmapIDSDLogHeader = aMap.get(Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A14).mSensorBitmapIDSDLogHeader;

			// Derived Channels - GSR Board
			aMap.put(Configuration.Shimmer3.SensorMapKey.PPG_A12, new SensorDetails(0, 0, Configuration.Shimmer3.GuiLabelSensors.PPG_A12));
			aMap.get(Configuration.Shimmer3.SensorMapKey.PPG_A12).mSensorBitmapIDStreaming = aMap.get(Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A12).mSensorBitmapIDStreaming;
			aMap.get(Configuration.Shimmer3.SensorMapKey.PPG_A12).mSensorBitmapIDSDLogHeader = aMap.get(Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A12).mSensorBitmapIDSDLogHeader;
//			aMap.get(Configuration.Shimmer3.SensorMapKey.PPG_A12).mDerivedSensorBitmapID = infoMemMap.maskDerivedChannelPpg_ADC12ADC13;
			aMap.put(Configuration.Shimmer3.SensorMapKey.PPG_A13, new SensorDetails(0, 0, Configuration.Shimmer3.GuiLabelSensors.PPG_A13));
			aMap.get(Configuration.Shimmer3.SensorMapKey.PPG_A13).mSensorBitmapIDStreaming = aMap.get(Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A13).mSensorBitmapIDStreaming;
			aMap.get(Configuration.Shimmer3.SensorMapKey.PPG_A13).mSensorBitmapIDSDLogHeader = aMap.get(Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A13).mSensorBitmapIDSDLogHeader;
//			aMap.get(Configuration.Shimmer3.SensorMapKey.PPG_A13).mDerivedSensorBitmapID = infoMemMap.maskDerivedChannelPpg_ADC12ADC13;
			
			// Derived Channels - Proto3 Board
			aMap.put(Configuration.Shimmer3.SensorMapKey.PPG1_A12, new SensorDetails(0, 0, Configuration.Shimmer3.GuiLabelSensors.PPG1_A12));
			aMap.get(Configuration.Shimmer3.SensorMapKey.PPG1_A12).mSensorBitmapIDStreaming = aMap.get(Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A12).mSensorBitmapIDStreaming;
			aMap.get(Configuration.Shimmer3.SensorMapKey.PPG1_A12).mSensorBitmapIDSDLogHeader = aMap.get(Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A12).mSensorBitmapIDSDLogHeader;
//			aMap.get(Configuration.Shimmer3.SensorMapKey.PPG1_A12).mDerivedSensorBitmapID = infoMemMap.maskDerivedChannelPpg1_ADC12ADC13;
			aMap.put(Configuration.Shimmer3.SensorMapKey.PPG1_A13, new SensorDetails(0, 0, Configuration.Shimmer3.GuiLabelSensors.PPG1_A13));
			aMap.get(Configuration.Shimmer3.SensorMapKey.PPG1_A13).mSensorBitmapIDStreaming = aMap.get(Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A13).mSensorBitmapIDStreaming;
			aMap.get(Configuration.Shimmer3.SensorMapKey.PPG1_A13).mSensorBitmapIDSDLogHeader = aMap.get(Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A13).mSensorBitmapIDSDLogHeader;
//			aMap.get(Configuration.Shimmer3.SensorMapKey.PPG1_A13).mDerivedSensorBitmapID = infoMemMap.maskDerivedChannelPpg1_ADC12ADC13;
			aMap.put(Configuration.Shimmer3.SensorMapKey.PPG2_A1, new SensorDetails(0, 0, Configuration.Shimmer3.GuiLabelSensors.PPG2_A1));
			aMap.get(Configuration.Shimmer3.SensorMapKey.PPG2_A1).mSensorBitmapIDStreaming = aMap.get(Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A1).mSensorBitmapIDStreaming;
			aMap.get(Configuration.Shimmer3.SensorMapKey.PPG2_A1).mSensorBitmapIDSDLogHeader = aMap.get(Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A1).mSensorBitmapIDSDLogHeader;
//			aMap.get(Configuration.Shimmer3.SensorMapKey.PPG2_A1).mDerivedSensorBitmapID = infoMemMap.maskDerivedChannelPpg2_ADC1ADC14;
			aMap.put(Configuration.Shimmer3.SensorMapKey.PPG2_A14, new SensorDetails(0, 0, Configuration.Shimmer3.GuiLabelSensors.PPG2_A14));
			aMap.get(Configuration.Shimmer3.SensorMapKey.PPG2_A14).mSensorBitmapIDStreaming = aMap.get(Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A14).mSensorBitmapIDStreaming;
			aMap.get(Configuration.Shimmer3.SensorMapKey.PPG2_A14).mSensorBitmapIDSDLogHeader = aMap.get(Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A14).mSensorBitmapIDSDLogHeader;
//			aMap.get(Configuration.Shimmer3.SensorMapKey.PPG2_A14).mDerivedSensorBitmapID = infoMemMap.maskDerivedChannelPpg2_ADC1ADC14;
			
//			// Derived Channels - GSR Board
//			aMap.put(Configuration.Shimmer3.SensorMapKey.PPG_DUMMY, new SensorDetails(0, 0, Configuration.Shimmer3.GuiLabelSensors.PPG_DUMMY));
//			aMap.put(Configuration.Shimmer3.SensorMapKey.PPG_A12, new SensorDetails(0, 0, Configuration.Shimmer3.GuiLabelSensors.PPG_A12));
//			aMap.get(Configuration.Shimmer3.SensorMapKey.PPG_A12).mSensorBitmapIDStreaming = aMap.get(Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A12).mSensorBitmapIDStreaming;
//			aMap.get(Configuration.Shimmer3.SensorMapKey.PPG_A12).mSensorBitmapIDSDLogHeader = aMap.get(Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A12).mSensorBitmapIDSDLogHeader;
////			aMap.get(Configuration.Shimmer3.SensorMapKey.PPG_A12).mDerivedSensorBitmapID = infoMemMap.maskDerivedChannelPpg_ADC12ADC13;
//			aMap.put(Configuration.Shimmer3.SensorMapKey.PPG_A13, new SensorDetails(0, 0, Configuration.Shimmer3.GuiLabelSensors.PPG_A13));
//			aMap.get(Configuration.Shimmer3.SensorMapKey.PPG_A13).mSensorBitmapIDStreaming = aMap.get(Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A13).mSensorBitmapIDStreaming;
//			aMap.get(Configuration.Shimmer3.SensorMapKey.PPG_A13).mSensorBitmapIDSDLogHeader = aMap.get(Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A13).mSensorBitmapIDSDLogHeader;
////			aMap.get(Configuration.Shimmer3.SensorMapKey.PPG_A13).mDerivedSensorBitmapID = infoMemMap.maskDerivedChannelPpg_ADC12ADC13;
//			
//			// Derived Channels - Proto3 Board
//			aMap.put(Configuration.Shimmer3.SensorMapKey.PPG1_DUMMY, new SensorDetails(0, 0, Configuration.Shimmer3.GuiLabelSensors.PPG1_DUMMY));
//			aMap.put(Configuration.Shimmer3.SensorMapKey.PPG1_A12, new SensorDetails(0, 0, Configuration.Shimmer3.GuiLabelSensors.PPG1_A12));
//			aMap.get(Configuration.Shimmer3.SensorMapKey.PPG1_A12).mSensorBitmapIDStreaming = aMap.get(Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A12).mSensorBitmapIDStreaming;
//			aMap.get(Configuration.Shimmer3.SensorMapKey.PPG1_A12).mSensorBitmapIDSDLogHeader = aMap.get(Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A12).mSensorBitmapIDSDLogHeader;
////			aMap.get(Configuration.Shimmer3.SensorMapKey.PPG1_A12).mDerivedSensorBitmapID = infoMemMap.maskDerivedChannelPpg1_ADC12ADC13;
//			aMap.put(Configuration.Shimmer3.SensorMapKey.PPG1_A13, new SensorDetails(0, 0, Configuration.Shimmer3.GuiLabelSensors.PPG1_A13));
//			aMap.get(Configuration.Shimmer3.SensorMapKey.PPG1_A13).mSensorBitmapIDStreaming = aMap.get(Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A13).mSensorBitmapIDStreaming;
//			aMap.get(Configuration.Shimmer3.SensorMapKey.PPG1_A13).mSensorBitmapIDSDLogHeader = aMap.get(Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A13).mSensorBitmapIDSDLogHeader;
////			aMap.get(Configuration.Shimmer3.SensorMapKey.PPG1_A13).mDerivedSensorBitmapID = infoMemMap.maskDerivedChannelPpg1_ADC12ADC13;
//			aMap.put(Configuration.Shimmer3.SensorMapKey.PPG2_DUMMY, new SensorDetails(0, 0, Configuration.Shimmer3.GuiLabelSensors.PPG2_DUMMY));
//			aMap.put(Configuration.Shimmer3.SensorMapKey.PPG2_A1, new SensorDetails(0, 0, Configuration.Shimmer3.GuiLabelSensors.PPG2_A1));
//			aMap.get(Configuration.Shimmer3.SensorMapKey.PPG2_A1).mSensorBitmapIDStreaming = aMap.get(Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A1).mSensorBitmapIDStreaming;
//			aMap.get(Configuration.Shimmer3.SensorMapKey.PPG2_A1).mSensorBitmapIDSDLogHeader = aMap.get(Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A1).mSensorBitmapIDSDLogHeader;
////			aMap.get(Configuration.Shimmer3.SensorMapKey.PPG2_A1).mDerivedSensorBitmapID = infoMemMap.maskDerivedChannelPpg2_ADC1ADC14;
//			aMap.put(Configuration.Shimmer3.SensorMapKey.PPG2_A14, new SensorDetails(0, 0, Configuration.Shimmer3.GuiLabelSensors.PPG2_A14));
//			aMap.get(Configuration.Shimmer3.SensorMapKey.PPG2_A14).mSensorBitmapIDStreaming = aMap.get(Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A14).mSensorBitmapIDStreaming;
//			aMap.get(Configuration.Shimmer3.SensorMapKey.PPG2_A14).mSensorBitmapIDSDLogHeader = aMap.get(Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A14).mSensorBitmapIDSDLogHeader;
////			aMap.get(Configuration.Shimmer3.SensorMapKey.PPG2_A14).mDerivedSensorBitmapID = infoMemMap.maskDerivedChannelPpg2_ADC1ADC14;

			
			// Now that channel map is assembled we can add compatiblity
			// information, internal expansion board power requirements,
			// associated required channels, conflicting channels and
			// associated configuration options.
			
			// Channels that have compatibility considerations (used to auto-hide/disable channels/config options in GUI)
			aMap.get(Configuration.Shimmer3.SensorMapKey.EXG1_24BIT).mListOfCompatibleVersionInfo = CompatibilityInfoForMaps.listOfCompatibleVersionInfoExg;
			aMap.get(Configuration.Shimmer3.SensorMapKey.EXG2_24BIT).mListOfCompatibleVersionInfo = CompatibilityInfoForMaps.listOfCompatibleVersionInfoExg;
			aMap.get(Configuration.Shimmer3.SensorMapKey.GSR).mListOfCompatibleVersionInfo = CompatibilityInfoForMaps.listOfCompatibleVersionInfoGsr;
			aMap.get(Configuration.Shimmer3.SensorMapKey.BRIDGE_AMP).mListOfCompatibleVersionInfo = CompatibilityInfoForMaps.listOfCompatibleVersionInfoBrAmp;
			aMap.get(Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A1).mListOfCompatibleVersionInfo = CompatibilityInfoForMaps.listOfCompatibleVersionInfoIntExpA1;
			aMap.get(Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A12).mListOfCompatibleVersionInfo = CompatibilityInfoForMaps.listOfCompatibleVersionInfoIntExpA12;
			aMap.get(Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A13).mListOfCompatibleVersionInfo = CompatibilityInfoForMaps.listOfCompatibleVersionInfoIntExpA13;
//			aMap.get(Configuration.Shimmer3.SensorMapKey.PPG_DUMMY).mListOfCompatibleVersionInfo = CompatibilityInfoForMaps.listOfCompatibleVersionInfoGsr;
			aMap.get(Configuration.Shimmer3.SensorMapKey.PPG_A12).mListOfCompatibleVersionInfo = CompatibilityInfoForMaps.listOfCompatibleVersionInfoGsr;
			aMap.get(Configuration.Shimmer3.SensorMapKey.PPG_A13).mListOfCompatibleVersionInfo = CompatibilityInfoForMaps.listOfCompatibleVersionInfoGsr;
//			aMap.get(Configuration.Shimmer3.SensorMapKey.PPG1_DUMMY).mListOfCompatibleVersionInfo = CompatibilityInfoForMaps.listOfCompatibleVersionInfoProto3Deluxe;
			aMap.get(Configuration.Shimmer3.SensorMapKey.PPG1_A12).mListOfCompatibleVersionInfo = CompatibilityInfoForMaps.listOfCompatibleVersionInfoProto3Deluxe;
			aMap.get(Configuration.Shimmer3.SensorMapKey.PPG1_A13).mListOfCompatibleVersionInfo = CompatibilityInfoForMaps.listOfCompatibleVersionInfoProto3Deluxe;
//			aMap.get(Configuration.Shimmer3.SensorMapKey.PPG2_DUMMY).mListOfCompatibleVersionInfo = CompatibilityInfoForMaps.listOfCompatibleVersionInfoProto3Deluxe;
			aMap.get(Configuration.Shimmer3.SensorMapKey.PPG2_A1).mListOfCompatibleVersionInfo = CompatibilityInfoForMaps.listOfCompatibleVersionInfoProto3Deluxe;
			aMap.get(Configuration.Shimmer3.SensorMapKey.PPG2_A14).mListOfCompatibleVersionInfo = CompatibilityInfoForMaps.listOfCompatibleVersionInfoProto3Deluxe;
			aMap.get(Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A14).mListOfCompatibleVersionInfo = CompatibilityInfoForMaps.listOfCompatibleVersionInfoIntExpA14;
			aMap.get(Configuration.Shimmer3.SensorMapKey.EXG1_16BIT).mListOfCompatibleVersionInfo = CompatibilityInfoForMaps.listOfCompatibleVersionInfoExg;
			aMap.get(Configuration.Shimmer3.SensorMapKey.EXG2_16BIT).mListOfCompatibleVersionInfo = CompatibilityInfoForMaps.listOfCompatibleVersionInfoExg;
			aMap.get(Configuration.Shimmer3.SensorMapKey.MPU9150_TEMP).mListOfCompatibleVersionInfo = CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors;
			aMap.get(Configuration.Shimmer3.SensorMapKey.MPU9150_MPL_QUAT_6DOF).mListOfCompatibleVersionInfo = CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors;
			aMap.get(Configuration.Shimmer3.SensorMapKey.MPU9150_MPL_QUAT_9DOF).mListOfCompatibleVersionInfo = CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors;
			aMap.get(Configuration.Shimmer3.SensorMapKey.MPU9150_MPL_EULER_6DOF).mListOfCompatibleVersionInfo = CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors;
			aMap.get(Configuration.Shimmer3.SensorMapKey.MPU9150_MPL_EULER_9DOF).mListOfCompatibleVersionInfo = CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors;
			aMap.get(Configuration.Shimmer3.SensorMapKey.MPU9150_MPL_HEADING).mListOfCompatibleVersionInfo = CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors;
			aMap.get(Configuration.Shimmer3.SensorMapKey.MPU9150_MPL_PEDOMETER).mListOfCompatibleVersionInfo = CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors;
			aMap.get(Configuration.Shimmer3.SensorMapKey.MPU9150_MPL_TAP).mListOfCompatibleVersionInfo = CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors;
			aMap.get(Configuration.Shimmer3.SensorMapKey.MPU9150_MPL_MOTION_ORIENT).mListOfCompatibleVersionInfo = CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors;
			aMap.get(Configuration.Shimmer3.SensorMapKey.MPU9150_MPL_GYRO).mListOfCompatibleVersionInfo = CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors;
			aMap.get(Configuration.Shimmer3.SensorMapKey.MPU9150_MPL_ACCEL).mListOfCompatibleVersionInfo = CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors;
			aMap.get(Configuration.Shimmer3.SensorMapKey.MPU9150_MPL_MAG).mListOfCompatibleVersionInfo = CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors;
			aMap.get(Configuration.Shimmer3.SensorMapKey.MPU9150_MPL_QUAT_6DOF_RAW).mListOfCompatibleVersionInfo = CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors;
			aMap.get(Configuration.Shimmer3.SensorMapKey.ECG).mListOfCompatibleVersionInfo = CompatibilityInfoForMaps.listOfCompatibleVersionInfoExg;
			aMap.get(Configuration.Shimmer3.SensorMapKey.EMG).mListOfCompatibleVersionInfo = CompatibilityInfoForMaps.listOfCompatibleVersionInfoExg;
			aMap.get(Configuration.Shimmer3.SensorMapKey.EXG_TEST).mListOfCompatibleVersionInfo = CompatibilityInfoForMaps.listOfCompatibleVersionInfoExg;
			aMap.get(Configuration.Shimmer3.SensorMapKey.EXG_CUSTOM).mListOfCompatibleVersionInfo = CompatibilityInfoForMaps.listOfCompatibleVersionInfoExg;
			aMap.get(Configuration.Shimmer3.SensorMapKey.EXG_RESPIRATION).mListOfCompatibleVersionInfo = CompatibilityInfoForMaps.listOfCompatibleVersionInfoRespiration;
			aMap.get(Configuration.Shimmer3.SensorMapKey.RESISTANCE_AMP).mListOfCompatibleVersionInfo = CompatibilityInfoForMaps.listOfCompatibleVersionInfoBrAmp;
			aMap.get(Configuration.Shimmer3.SensorMapKey.SKIN_TEMP_PROBE).mListOfCompatibleVersionInfo = CompatibilityInfoForMaps.listOfCompatibleVersionInfoBrAmp;
			aMap.get(Configuration.Shimmer3.SensorMapKey.A_ACCEL).mListOfCompatibleVersionInfo = CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW;
			aMap.get(Configuration.Shimmer3.SensorMapKey.LSM303DLHC_MAG).mListOfCompatibleVersionInfo = CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW;
			aMap.get(Configuration.Shimmer3.SensorMapKey.MPU9150_GYRO).mListOfCompatibleVersionInfo = CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW;
			aMap.get(Configuration.Shimmer3.SensorMapKey.LSM303DLHC_ACCEL).mListOfCompatibleVersionInfo = CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW;
			aMap.get(Configuration.Shimmer3.SensorMapKey.BMP180_PRESSURE).mListOfCompatibleVersionInfo = CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW;
			aMap.get(Configuration.Shimmer3.SensorMapKey.EXT_EXP_ADC_A6).mListOfCompatibleVersionInfo = CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW;
			aMap.get(Configuration.Shimmer3.SensorMapKey.EXT_EXP_ADC_A7).mListOfCompatibleVersionInfo = CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW;
			aMap.get(Configuration.Shimmer3.SensorMapKey.EXT_EXP_ADC_A15).mListOfCompatibleVersionInfo = CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW;
			
			// Channels that require Internal Expansion board power (automatically enabled internal expansion board power when each Channel is enabled)
			aMap.get(Configuration.Shimmer3.SensorMapKey.BRIDGE_AMP).mIntExpBoardPowerRequired = true;
			aMap.get(Configuration.Shimmer3.SensorMapKey.RESISTANCE_AMP).mIntExpBoardPowerRequired = true;
			aMap.get(Configuration.Shimmer3.SensorMapKey.SKIN_TEMP_PROBE).mIntExpBoardPowerRequired = true;
			aMap.get(Configuration.Shimmer3.SensorMapKey.PPG_A12).mIntExpBoardPowerRequired = true;
			aMap.get(Configuration.Shimmer3.SensorMapKey.PPG_A13).mIntExpBoardPowerRequired = true;
			aMap.get(Configuration.Shimmer3.SensorMapKey.PPG1_A12).mIntExpBoardPowerRequired = true;
			aMap.get(Configuration.Shimmer3.SensorMapKey.PPG1_A13).mIntExpBoardPowerRequired = true;
			aMap.get(Configuration.Shimmer3.SensorMapKey.PPG2_A1).mIntExpBoardPowerRequired = true;
			aMap.get(Configuration.Shimmer3.SensorMapKey.PPG2_A14).mIntExpBoardPowerRequired = true;
			aMap.get(Configuration.Shimmer3.SensorMapKey.EXG1_24BIT).mIntExpBoardPowerRequired = true;
			aMap.get(Configuration.Shimmer3.SensorMapKey.EXG2_24BIT).mIntExpBoardPowerRequired = true;
			aMap.get(Configuration.Shimmer3.SensorMapKey.EXG1_16BIT).mIntExpBoardPowerRequired = true;
			aMap.get(Configuration.Shimmer3.SensorMapKey.EXG2_16BIT).mIntExpBoardPowerRequired = true;
			aMap.get(Configuration.Shimmer3.SensorMapKey.ECG).mIntExpBoardPowerRequired = true;
			aMap.get(Configuration.Shimmer3.SensorMapKey.EMG).mIntExpBoardPowerRequired = true;
			aMap.get(Configuration.Shimmer3.SensorMapKey.EXG_TEST).mIntExpBoardPowerRequired = true;
			aMap.get(Configuration.Shimmer3.SensorMapKey.EXG_CUSTOM).mIntExpBoardPowerRequired = true;
			aMap.get(Configuration.Shimmer3.SensorMapKey.EXG_RESPIRATION).mIntExpBoardPowerRequired = true;
			
			// Required Channels - these get auto enabled/disabled when the parent channel is enabled/disabled
			//mSensorMap.get(Configuration.Shimmer3.SensorMapKey.RESISTANCE_AMP).mListOfSensorMapKeysRequired = Arrays.asList(
			//		Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A1);
			//mSensorMap.get(Configuration.Shimmer3.SensorMapKey.SKIN_TEMP_PROBE).mListOfSensorMapKeysRequired = Arrays.asList(
			//		Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A1);
			
			// Conflicting Channels (stop two conflicting channels from being enabled and the same time)
			aMap.get(Configuration.Shimmer3.SensorMapKey.EXG1_24BIT).mListOfSensorMapKeysConflicting = Arrays.asList(
					Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A1,
					Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A12,
					Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A13,
					Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A14,
					Configuration.Shimmer3.SensorMapKey.GSR,
					Configuration.Shimmer3.SensorMapKey.EXG1_16BIT,
					Configuration.Shimmer3.SensorMapKey.EXG2_16BIT,
					Configuration.Shimmer3.SensorMapKey.RESISTANCE_AMP,
					Configuration.Shimmer3.SensorMapKey.BRIDGE_AMP);
			aMap.get(Configuration.Shimmer3.SensorMapKey.EXG2_24BIT).mListOfSensorMapKeysConflicting = Arrays.asList(
					Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A1,
					Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A12,
					Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A13,
					Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A14,
					Configuration.Shimmer3.SensorMapKey.GSR,
					Configuration.Shimmer3.SensorMapKey.EMG,
					Configuration.Shimmer3.SensorMapKey.EXG1_16BIT,
					Configuration.Shimmer3.SensorMapKey.EXG2_16BIT,
					Configuration.Shimmer3.SensorMapKey.RESISTANCE_AMP,
					Configuration.Shimmer3.SensorMapKey.BRIDGE_AMP);
			aMap.get(Configuration.Shimmer3.SensorMapKey.GSR).mListOfSensorMapKeysConflicting = Arrays.asList(
					Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A1,
					Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A14,
					Configuration.Shimmer3.SensorMapKey.ECG,
					Configuration.Shimmer3.SensorMapKey.EMG,
					Configuration.Shimmer3.SensorMapKey.EXG_TEST,
					Configuration.Shimmer3.SensorMapKey.EXG_CUSTOM,
					Configuration.Shimmer3.SensorMapKey.EXG_RESPIRATION,
					Configuration.Shimmer3.SensorMapKey.EXG1_16BIT,
					Configuration.Shimmer3.SensorMapKey.EXG2_16BIT,
					Configuration.Shimmer3.SensorMapKey.EXG1_24BIT,
					Configuration.Shimmer3.SensorMapKey.EXG2_24BIT,
					Configuration.Shimmer3.SensorMapKey.RESISTANCE_AMP,
					Configuration.Shimmer3.SensorMapKey.BRIDGE_AMP);
			aMap.get(Configuration.Shimmer3.SensorMapKey.RESISTANCE_AMP).mListOfSensorMapKeysConflicting = Arrays.asList(
					Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A1,
					Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A12,
					Configuration.Shimmer3.SensorMapKey.SKIN_TEMP_PROBE,
					Configuration.Shimmer3.SensorMapKey.GSR,
					Configuration.Shimmer3.SensorMapKey.ECG,
					Configuration.Shimmer3.SensorMapKey.EMG,
					Configuration.Shimmer3.SensorMapKey.EXG_TEST,
					Configuration.Shimmer3.SensorMapKey.EXG_CUSTOM,
					Configuration.Shimmer3.SensorMapKey.EXG_RESPIRATION,
					Configuration.Shimmer3.SensorMapKey.EXG1_16BIT,
					Configuration.Shimmer3.SensorMapKey.EXG2_16BIT,
					Configuration.Shimmer3.SensorMapKey.EXG1_24BIT,
					Configuration.Shimmer3.SensorMapKey.EXG2_24BIT);
			aMap.get(Configuration.Shimmer3.SensorMapKey.SKIN_TEMP_PROBE).mListOfSensorMapKeysConflicting = Arrays.asList(
					Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A1,
					Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A12,
					Configuration.Shimmer3.SensorMapKey.RESISTANCE_AMP,
					Configuration.Shimmer3.SensorMapKey.GSR,
					Configuration.Shimmer3.SensorMapKey.ECG,
					Configuration.Shimmer3.SensorMapKey.EMG,
					Configuration.Shimmer3.SensorMapKey.EXG_TEST,
					Configuration.Shimmer3.SensorMapKey.EXG_CUSTOM,
					Configuration.Shimmer3.SensorMapKey.EXG_RESPIRATION,
					Configuration.Shimmer3.SensorMapKey.EXG1_16BIT,
					Configuration.Shimmer3.SensorMapKey.EXG2_16BIT,
					Configuration.Shimmer3.SensorMapKey.EXG1_24BIT,
					Configuration.Shimmer3.SensorMapKey.EXG2_24BIT);
			aMap.get(Configuration.Shimmer3.SensorMapKey.BRIDGE_AMP).mListOfSensorMapKeysConflicting = Arrays.asList(
					Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A12,
					Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A13,
					Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A14,
					Configuration.Shimmer3.SensorMapKey.GSR,
					Configuration.Shimmer3.SensorMapKey.ECG,
					Configuration.Shimmer3.SensorMapKey.EMG,
					Configuration.Shimmer3.SensorMapKey.EXG_TEST,
					Configuration.Shimmer3.SensorMapKey.EXG_CUSTOM,
					Configuration.Shimmer3.SensorMapKey.EXG_RESPIRATION,
					Configuration.Shimmer3.SensorMapKey.EXG1_16BIT,
					Configuration.Shimmer3.SensorMapKey.EXG2_16BIT,
					Configuration.Shimmer3.SensorMapKey.EXG1_24BIT,
					Configuration.Shimmer3.SensorMapKey.EXG2_24BIT);
			aMap.get(Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A1).mListOfSensorMapKeysConflicting = Arrays.asList(
//					Configuration.Shimmer3.SensorMapKey.PPG2_DUMMY,
					Configuration.Shimmer3.SensorMapKey.PPG2_A1,
					Configuration.Shimmer3.SensorMapKey.PPG2_A14,
					Configuration.Shimmer3.SensorMapKey.RESISTANCE_AMP,
					Configuration.Shimmer3.SensorMapKey.SKIN_TEMP_PROBE,
					Configuration.Shimmer3.SensorMapKey.GSR,
					Configuration.Shimmer3.SensorMapKey.ECG,
					Configuration.Shimmer3.SensorMapKey.EMG,
					Configuration.Shimmer3.SensorMapKey.EXG_TEST,
					Configuration.Shimmer3.SensorMapKey.EXG_CUSTOM,
					Configuration.Shimmer3.SensorMapKey.EXG_RESPIRATION,
					Configuration.Shimmer3.SensorMapKey.EXG1_16BIT,
					Configuration.Shimmer3.SensorMapKey.EXG2_16BIT,
					Configuration.Shimmer3.SensorMapKey.EXG1_24BIT,
					Configuration.Shimmer3.SensorMapKey.EXG2_24BIT);
			aMap.get(Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A12).mListOfSensorMapKeysConflicting = Arrays.asList(
//					Configuration.Shimmer3.SensorMapKey.PPG_DUMMY,
					Configuration.Shimmer3.SensorMapKey.PPG_A12,
					Configuration.Shimmer3.SensorMapKey.PPG_A13,
//					Configuration.Shimmer3.SensorMapKey.PPG1_DUMMY,
					Configuration.Shimmer3.SensorMapKey.PPG1_A12,
					Configuration.Shimmer3.SensorMapKey.PPG1_A13,
					Configuration.Shimmer3.SensorMapKey.ECG,
					Configuration.Shimmer3.SensorMapKey.EMG,
					Configuration.Shimmer3.SensorMapKey.EXG_TEST,
					Configuration.Shimmer3.SensorMapKey.EXG_CUSTOM,
					Configuration.Shimmer3.SensorMapKey.EXG_RESPIRATION,
					Configuration.Shimmer3.SensorMapKey.EXG1_16BIT,
					Configuration.Shimmer3.SensorMapKey.EXG2_16BIT,
					Configuration.Shimmer3.SensorMapKey.EXG1_24BIT,
					Configuration.Shimmer3.SensorMapKey.EXG2_24BIT,
					Configuration.Shimmer3.SensorMapKey.RESISTANCE_AMP,
					Configuration.Shimmer3.SensorMapKey.BRIDGE_AMP);
			aMap.get(Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A13).mListOfSensorMapKeysConflicting = Arrays.asList(
//					Configuration.Shimmer3.SensorMapKey.PPG_DUMMY,
					Configuration.Shimmer3.SensorMapKey.PPG_A12,
					Configuration.Shimmer3.SensorMapKey.PPG_A13,
//					Configuration.Shimmer3.SensorMapKey.PPG1_DUMMY,
					Configuration.Shimmer3.SensorMapKey.PPG1_A12,
					Configuration.Shimmer3.SensorMapKey.PPG1_A13,
					Configuration.Shimmer3.SensorMapKey.ECG,
					Configuration.Shimmer3.SensorMapKey.EMG,
					Configuration.Shimmer3.SensorMapKey.EXG_TEST,
					Configuration.Shimmer3.SensorMapKey.EXG_CUSTOM,
					Configuration.Shimmer3.SensorMapKey.EXG_RESPIRATION,
					Configuration.Shimmer3.SensorMapKey.EXG1_16BIT,
					Configuration.Shimmer3.SensorMapKey.EXG2_16BIT,
					Configuration.Shimmer3.SensorMapKey.EXG1_24BIT,
					Configuration.Shimmer3.SensorMapKey.EXG2_24BIT,
					Configuration.Shimmer3.SensorMapKey.RESISTANCE_AMP,
					Configuration.Shimmer3.SensorMapKey.BRIDGE_AMP);
			aMap.get(Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A14).mListOfSensorMapKeysConflicting = Arrays.asList(
//					Configuration.Shimmer3.SensorMapKey.PPG2_DUMMY,
					Configuration.Shimmer3.SensorMapKey.PPG2_A1,
					Configuration.Shimmer3.SensorMapKey.PPG2_A14,
					Configuration.Shimmer3.SensorMapKey.GSR,
					Configuration.Shimmer3.SensorMapKey.ECG,
					Configuration.Shimmer3.SensorMapKey.EMG,
					Configuration.Shimmer3.SensorMapKey.EXG_TEST,
					Configuration.Shimmer3.SensorMapKey.EXG_CUSTOM,
					Configuration.Shimmer3.SensorMapKey.EXG_RESPIRATION,
					Configuration.Shimmer3.SensorMapKey.EXG1_16BIT,
					Configuration.Shimmer3.SensorMapKey.EXG2_16BIT,
					Configuration.Shimmer3.SensorMapKey.EXG1_24BIT,
					Configuration.Shimmer3.SensorMapKey.EXG2_24BIT,
					Configuration.Shimmer3.SensorMapKey.RESISTANCE_AMP,
					Configuration.Shimmer3.SensorMapKey.BRIDGE_AMP);
			aMap.get(Configuration.Shimmer3.SensorMapKey.EXG1_16BIT).mListOfSensorMapKeysConflicting = Arrays.asList(
					Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A1,
					Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A12,
					Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A13,
					Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A14,
					Configuration.Shimmer3.SensorMapKey.GSR,
					Configuration.Shimmer3.SensorMapKey.EXG1_24BIT,
					Configuration.Shimmer3.SensorMapKey.EXG2_24BIT,
					Configuration.Shimmer3.SensorMapKey.RESISTANCE_AMP,
					Configuration.Shimmer3.SensorMapKey.BRIDGE_AMP);
			aMap.get(Configuration.Shimmer3.SensorMapKey.EXG2_16BIT).mListOfSensorMapKeysConflicting = Arrays.asList(
					Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A1,
					Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A12,
					Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A13,
					Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A14,
					Configuration.Shimmer3.SensorMapKey.GSR,
					Configuration.Shimmer3.SensorMapKey.EMG,
					Configuration.Shimmer3.SensorMapKey.EXG1_24BIT,
					Configuration.Shimmer3.SensorMapKey.EXG2_24BIT,
					Configuration.Shimmer3.SensorMapKey.RESISTANCE_AMP,
					Configuration.Shimmer3.SensorMapKey.BRIDGE_AMP);
			aMap.get(Configuration.Shimmer3.SensorMapKey.ECG).mListOfSensorMapKeysConflicting = Arrays.asList(
					Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A1,
					Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A12,
					Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A13,
					Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A14,
					Configuration.Shimmer3.SensorMapKey.GSR,
					Configuration.Shimmer3.SensorMapKey.RESISTANCE_AMP,
					Configuration.Shimmer3.SensorMapKey.BRIDGE_AMP,
					Configuration.Shimmer3.SensorMapKey.EMG,
					Configuration.Shimmer3.SensorMapKey.EXG_TEST,
					Configuration.Shimmer3.SensorMapKey.EXG_CUSTOM,
					Configuration.Shimmer3.SensorMapKey.EXG_RESPIRATION);
			aMap.get(Configuration.Shimmer3.SensorMapKey.EMG).mListOfSensorMapKeysConflicting = Arrays.asList(
					Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A1,
					Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A12,
					Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A13,
					Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A14,
					Configuration.Shimmer3.SensorMapKey.GSR,
					Configuration.Shimmer3.SensorMapKey.RESISTANCE_AMP,
					Configuration.Shimmer3.SensorMapKey.BRIDGE_AMP,
					Configuration.Shimmer3.SensorMapKey.EXG2_16BIT,
					Configuration.Shimmer3.SensorMapKey.EXG2_24BIT,
					Configuration.Shimmer3.SensorMapKey.ECG,
					Configuration.Shimmer3.SensorMapKey.EXG_TEST,
					Configuration.Shimmer3.SensorMapKey.EXG_CUSTOM,
					Configuration.Shimmer3.SensorMapKey.EXG_RESPIRATION);
			aMap.get(Configuration.Shimmer3.SensorMapKey.EXG_TEST).mListOfSensorMapKeysConflicting = Arrays.asList(
					Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A1,
					Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A12,
					Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A13,
					Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A14,
					Configuration.Shimmer3.SensorMapKey.GSR,
					Configuration.Shimmer3.SensorMapKey.RESISTANCE_AMP,
					Configuration.Shimmer3.SensorMapKey.BRIDGE_AMP,
					Configuration.Shimmer3.SensorMapKey.ECG,
					Configuration.Shimmer3.SensorMapKey.EMG,
					Configuration.Shimmer3.SensorMapKey.EXG_CUSTOM,
					Configuration.Shimmer3.SensorMapKey.EXG_RESPIRATION);
			aMap.get(Configuration.Shimmer3.SensorMapKey.EXG_CUSTOM).mListOfSensorMapKeysConflicting = Arrays.asList(
					Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A1,
					Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A12,
					Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A13,
					Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A14,
					Configuration.Shimmer3.SensorMapKey.GSR,
					Configuration.Shimmer3.SensorMapKey.RESISTANCE_AMP,
					Configuration.Shimmer3.SensorMapKey.BRIDGE_AMP,
					Configuration.Shimmer3.SensorMapKey.ECG,
					Configuration.Shimmer3.SensorMapKey.EMG,
					Configuration.Shimmer3.SensorMapKey.EXG_TEST,
					Configuration.Shimmer3.SensorMapKey.EXG_RESPIRATION);
			aMap.get(Configuration.Shimmer3.SensorMapKey.EXG_RESPIRATION).mListOfSensorMapKeysConflicting = Arrays.asList(
					Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A1,
					Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A12,
					Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A13,
					Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A14,
					Configuration.Shimmer3.SensorMapKey.GSR,
					Configuration.Shimmer3.SensorMapKey.RESISTANCE_AMP,
					Configuration.Shimmer3.SensorMapKey.BRIDGE_AMP,
					Configuration.Shimmer3.SensorMapKey.ECG,
					Configuration.Shimmer3.SensorMapKey.EMG,
					Configuration.Shimmer3.SensorMapKey.EXG_CUSTOM,
					Configuration.Shimmer3.SensorMapKey.EXG_TEST);
			aMap.get(Configuration.Shimmer3.SensorMapKey.MPU9150_MPL_GYRO).mListOfSensorMapKeysConflicting = Arrays.asList(
					Configuration.Shimmer3.SensorMapKey.MPU9150_GYRO);
			aMap.get(Configuration.Shimmer3.SensorMapKey.MPU9150_GYRO).mListOfSensorMapKeysConflicting = Arrays.asList(
					Configuration.Shimmer3.SensorMapKey.MPU9150_MPL_GYRO);
			
			//The A12 and A13 based PPG channels have the same channel exceptions as GSR with the addition of their counterpart channel 
			aMap.get(Configuration.Shimmer3.SensorMapKey.PPG_A12).mListOfSensorMapKeysConflicting = new ArrayList<Integer>(aMap.get(Configuration.Shimmer3.SensorMapKey.GSR).mListOfSensorMapKeysConflicting);
			aMap.get(Configuration.Shimmer3.SensorMapKey.PPG_A12).mListOfSensorMapKeysConflicting.add(Configuration.Shimmer3.SensorMapKey.PPG_A13);
			aMap.get(Configuration.Shimmer3.SensorMapKey.PPG_A12).mListOfSensorMapKeysConflicting.add(Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A12);
			aMap.get(Configuration.Shimmer3.SensorMapKey.PPG_A12).mListOfSensorMapKeysConflicting.add(Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A13);
			aMap.get(Configuration.Shimmer3.SensorMapKey.PPG_A13).mListOfSensorMapKeysConflicting = new ArrayList<Integer>(aMap.get(Configuration.Shimmer3.SensorMapKey.GSR).mListOfSensorMapKeysConflicting);
			aMap.get(Configuration.Shimmer3.SensorMapKey.PPG_A13).mListOfSensorMapKeysConflicting.add(Configuration.Shimmer3.SensorMapKey.PPG_A12);
			aMap.get(Configuration.Shimmer3.SensorMapKey.PPG_A13).mListOfSensorMapKeysConflicting.add(Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A12);
			aMap.get(Configuration.Shimmer3.SensorMapKey.PPG_A13).mListOfSensorMapKeysConflicting.add(Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A13);

			aMap.get(Configuration.Shimmer3.SensorMapKey.PPG1_A12).mListOfSensorMapKeysConflicting = Arrays.asList(
					Configuration.Shimmer3.SensorMapKey.ECG,
					Configuration.Shimmer3.SensorMapKey.GSR,
					Configuration.Shimmer3.SensorMapKey.ECG,
					Configuration.Shimmer3.SensorMapKey.EMG,
					Configuration.Shimmer3.SensorMapKey.EXG_TEST,
					Configuration.Shimmer3.SensorMapKey.EXG_CUSTOM,
					Configuration.Shimmer3.SensorMapKey.EXG_RESPIRATION,
					Configuration.Shimmer3.SensorMapKey.EXG1_16BIT,
					Configuration.Shimmer3.SensorMapKey.EXG2_16BIT,
					Configuration.Shimmer3.SensorMapKey.EXG1_24BIT,
					Configuration.Shimmer3.SensorMapKey.EXG2_24BIT,
					Configuration.Shimmer3.SensorMapKey.RESISTANCE_AMP,
					Configuration.Shimmer3.SensorMapKey.BRIDGE_AMP,
					Configuration.Shimmer3.SensorMapKey.PPG1_A13,
					Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A12,
					Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A13);
			
			aMap.get(Configuration.Shimmer3.SensorMapKey.PPG1_A13).mListOfSensorMapKeysConflicting = Arrays.asList(
					Configuration.Shimmer3.SensorMapKey.ECG,
					Configuration.Shimmer3.SensorMapKey.GSR,
					Configuration.Shimmer3.SensorMapKey.ECG,
					Configuration.Shimmer3.SensorMapKey.EMG,
					Configuration.Shimmer3.SensorMapKey.EXG_TEST,
					Configuration.Shimmer3.SensorMapKey.EXG_RESPIRATION,
					Configuration.Shimmer3.SensorMapKey.EXG1_16BIT,
					Configuration.Shimmer3.SensorMapKey.EXG2_16BIT,
					Configuration.Shimmer3.SensorMapKey.EXG1_24BIT,
					Configuration.Shimmer3.SensorMapKey.EXG2_24BIT,
					Configuration.Shimmer3.SensorMapKey.RESISTANCE_AMP,
					Configuration.Shimmer3.SensorMapKey.BRIDGE_AMP,
					Configuration.Shimmer3.SensorMapKey.PPG1_A12,
					Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A12,
					Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A13);
					
			aMap.get(Configuration.Shimmer3.SensorMapKey.PPG2_A1).mListOfSensorMapKeysConflicting = Arrays.asList(
					Configuration.Shimmer3.SensorMapKey.ECG,
					Configuration.Shimmer3.SensorMapKey.GSR,
					Configuration.Shimmer3.SensorMapKey.ECG,
					Configuration.Shimmer3.SensorMapKey.EMG,
					Configuration.Shimmer3.SensorMapKey.EXG_TEST,
					Configuration.Shimmer3.SensorMapKey.EXG_RESPIRATION,
					Configuration.Shimmer3.SensorMapKey.EXG1_16BIT,
					Configuration.Shimmer3.SensorMapKey.EXG2_16BIT,
					Configuration.Shimmer3.SensorMapKey.EXG1_24BIT,
					Configuration.Shimmer3.SensorMapKey.EXG2_24BIT,
					Configuration.Shimmer3.SensorMapKey.RESISTANCE_AMP,
					Configuration.Shimmer3.SensorMapKey.BRIDGE_AMP,
					Configuration.Shimmer3.SensorMapKey.PPG2_A14,
					Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A1,
					Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A14);
					
			aMap.get(Configuration.Shimmer3.SensorMapKey.PPG2_A14).mListOfSensorMapKeysConflicting = Arrays.asList(
					Configuration.Shimmer3.SensorMapKey.ECG,
					Configuration.Shimmer3.SensorMapKey.GSR,
					Configuration.Shimmer3.SensorMapKey.ECG,
					Configuration.Shimmer3.SensorMapKey.EMG,
					Configuration.Shimmer3.SensorMapKey.EXG_TEST,
					Configuration.Shimmer3.SensorMapKey.EXG_CUSTOM,
					Configuration.Shimmer3.SensorMapKey.EXG_RESPIRATION,
					Configuration.Shimmer3.SensorMapKey.EXG1_16BIT,
					Configuration.Shimmer3.SensorMapKey.EXG2_16BIT,
					Configuration.Shimmer3.SensorMapKey.EXG1_24BIT,
					Configuration.Shimmer3.SensorMapKey.EXG2_24BIT,
					Configuration.Shimmer3.SensorMapKey.RESISTANCE_AMP,
					Configuration.Shimmer3.SensorMapKey.BRIDGE_AMP,
					Configuration.Shimmer3.SensorMapKey.PPG2_A1,
					Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A1,
					Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A14);
			
			// Associated config options for each channel (currently used for the ChannelTileMap)
			aMap.get(Configuration.Shimmer3.SensorMapKey.LSM303DLHC_ACCEL).mListOfConfigOptionKeysAssociated = Arrays.asList(
					Configuration.Shimmer3.GuiLabelConfig.LSM303DLHC_ACCEL_RANGE,
					Configuration.Shimmer3.GuiLabelConfig.LSM303DLHC_ACCEL_RATE);
			aMap.get(Configuration.Shimmer3.SensorMapKey.LSM303DLHC_MAG).mListOfConfigOptionKeysAssociated = Arrays.asList(
					Configuration.Shimmer3.GuiLabelConfig.LSM303DLHC_MAG_RANGE,
					Configuration.Shimmer3.GuiLabelConfig.LSM303DLHC_MAG_RATE);
			aMap.get(Configuration.Shimmer3.SensorMapKey.MPU9150_GYRO).mListOfConfigOptionKeysAssociated = Arrays.asList(
					Configuration.Shimmer3.GuiLabelConfig.MPU9150_GYRO_RANGE,
					Configuration.Shimmer3.GuiLabelConfig.MPU9150_GYRO_RATE);
			aMap.get(Configuration.Shimmer3.SensorMapKey.BMP180_PRESSURE).mListOfConfigOptionKeysAssociated = Arrays.asList(
					Configuration.Shimmer3.GuiLabelConfig.PRESSURE_RESOLUTION);
			aMap.get(Configuration.Shimmer3.SensorMapKey.GSR).mListOfConfigOptionKeysAssociated = Arrays.asList(
					Configuration.Shimmer3.GuiLabelConfig.GSR_RANGE);
			aMap.get(Configuration.Shimmer3.SensorMapKey.ECG).mListOfConfigOptionKeysAssociated = Arrays.asList(
					Configuration.Shimmer3.GuiLabelConfig.EXG_GAIN,
					Configuration.Shimmer3.GuiLabelConfig.EXG_RESOLUTION,
					Configuration.Shimmer3.GuiLabelConfig.EXG_REFERENCE_ELECTRODE,
					Configuration.Shimmer3.GuiLabelConfig.EXG_LEAD_OFF_DETECTION,
					Configuration.Shimmer3.GuiLabelConfig.EXG_LEAD_OFF_CURRENT,
					Configuration.Shimmer3.GuiLabelConfig.EXG_LEAD_OFF_COMPARATOR,
					Configuration.Shimmer3.GuiLabelConfig.EXG_RESPIRATION_DETECT_FREQ,
					Configuration.Shimmer3.GuiLabelConfig.EXG_RESPIRATION_DETECT_PHASE);
			aMap.get(Configuration.Shimmer3.SensorMapKey.EMG).mListOfConfigOptionKeysAssociated = Arrays.asList(
					Configuration.Shimmer3.GuiLabelConfig.EXG_GAIN,
					Configuration.Shimmer3.GuiLabelConfig.EXG_RESOLUTION,
					Configuration.Shimmer3.GuiLabelConfig.EXG_REFERENCE_ELECTRODE,
					Configuration.Shimmer3.GuiLabelConfig.EXG_LEAD_OFF_DETECTION,
					Configuration.Shimmer3.GuiLabelConfig.EXG_LEAD_OFF_CURRENT,
					Configuration.Shimmer3.GuiLabelConfig.EXG_LEAD_OFF_COMPARATOR);
			aMap.get(Configuration.Shimmer3.SensorMapKey.EXG_TEST).mListOfConfigOptionKeysAssociated = Arrays.asList(
					Configuration.Shimmer3.GuiLabelConfig.EXG_GAIN,
					Configuration.Shimmer3.GuiLabelConfig.EXG_RESOLUTION);
			aMap.get(Configuration.Shimmer3.SensorMapKey.EXG_CUSTOM).mListOfConfigOptionKeysAssociated = Arrays.asList(
					Configuration.Shimmer3.GuiLabelConfig.EXG_GAIN,
					Configuration.Shimmer3.GuiLabelConfig.EXG_RESOLUTION,
					Configuration.Shimmer3.GuiLabelConfig.EXG_REFERENCE_ELECTRODE,
					Configuration.Shimmer3.GuiLabelConfig.EXG_LEAD_OFF_DETECTION,
					Configuration.Shimmer3.GuiLabelConfig.EXG_LEAD_OFF_CURRENT,
					Configuration.Shimmer3.GuiLabelConfig.EXG_LEAD_OFF_COMPARATOR,
					Configuration.Shimmer3.GuiLabelConfig.EXG_RESPIRATION_DETECT_FREQ,
					Configuration.Shimmer3.GuiLabelConfig.EXG_RESPIRATION_DETECT_PHASE);
			aMap.get(Configuration.Shimmer3.SensorMapKey.EXG_RESPIRATION).mListOfConfigOptionKeysAssociated = Arrays.asList(
					Configuration.Shimmer3.GuiLabelConfig.EXG_GAIN,
					Configuration.Shimmer3.GuiLabelConfig.EXG_RESOLUTION,
					Configuration.Shimmer3.GuiLabelConfig.EXG_RESPIRATION_DETECT_FREQ,
					Configuration.Shimmer3.GuiLabelConfig.EXG_RESPIRATION_DETECT_PHASE);
//			aMap.get(Configuration.Shimmer3.SensorMapKey.PPG_DUMMY).mListOfConfigOptionKeysAssociated = Arrays.asList(
//					Configuration.Shimmer3.GuiLabelConfig.PPG_ADC_SELECTION);
//			aMap.get(Configuration.Shimmer3.SensorMapKey.PPG1_DUMMY).mListOfConfigOptionKeysAssociated = Arrays.asList(
//					Configuration.Shimmer3.GuiLabelConfig.PPG1_ADC_SELECTION);
//			aMap.get(Configuration.Shimmer3.SensorMapKey.PPG2_DUMMY).mListOfConfigOptionKeysAssociated = Arrays.asList(
//					Configuration.Shimmer3.GuiLabelConfig.PPG2_ADC_SELECTION);
			aMap.get(Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A1).mListOfConfigOptionKeysAssociated = Arrays.asList(
					Configuration.Shimmer3.GuiLabelConfig.INT_EXP_BRD_POWER_INTEGER);
			aMap.get(Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A12).mListOfConfigOptionKeysAssociated = Arrays.asList(
					Configuration.Shimmer3.GuiLabelConfig.INT_EXP_BRD_POWER_INTEGER);
			aMap.get(Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A13).mListOfConfigOptionKeysAssociated = Arrays.asList(
					Configuration.Shimmer3.GuiLabelConfig.INT_EXP_BRD_POWER_INTEGER);
			aMap.get(Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A14).mListOfConfigOptionKeysAssociated = Arrays.asList(
					Configuration.Shimmer3.GuiLabelConfig.INT_EXP_BRD_POWER_INTEGER);
			aMap.get(Configuration.Shimmer3.SensorMapKey.MPU9150_MPL_ACCEL).mListOfConfigOptionKeysAssociated = Arrays.asList(
					Configuration.Shimmer3.GuiLabelConfig.MPU9150_ACCEL_RANGE,
					Configuration.Shimmer3.GuiLabelConfig.MPU9150_LPF);
			aMap.get(Configuration.Shimmer3.SensorMapKey.MPU9150_MPL_GYRO).mListOfConfigOptionKeysAssociated = Arrays.asList(
					Configuration.Shimmer3.GuiLabelConfig.MPU9150_GYRO_RANGE,
					Configuration.Shimmer3.GuiLabelConfig.MPU9150_LPF,
					Configuration.Shimmer3.GuiLabelConfig.MPU9150_GYRO_RATE,
					Configuration.Shimmer3.GuiLabelConfig.MPU9150_MPL_GYRO_CAL);
			aMap.get(Configuration.Shimmer3.SensorMapKey.MPU9150_MPL_MAG).mListOfConfigOptionKeysAssociated = Arrays.asList(
					Configuration.Shimmer3.GuiLabelConfig.MPU9150_LPF);
			
			
			
			// All Information required for parsing each of the channels
//			mSensorMap.get(Configuration.Shimmer3.SensorMapKey.TIMESTAMP).mListOfChannels.add(new ChannelDetails(Configuration.Shimmer3.ObjectClusterSensorName.TIMESTAMP,ChannelDataType.UINT16,2,ChannelDataEndian.LSB, CHANNEL_UNITS.MILLISECONDS));
			
			aMap.get(Configuration.Shimmer3.SensorMapKey.A_ACCEL).mListOfChannels.add(new ChannelDetails(Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_LN_X,ChannelDataType.UINT12,2,ChannelDataEndian.LSB, CHANNEL_UNITS.METER_PER_SECOND_SQUARE));
			aMap.get(Configuration.Shimmer3.SensorMapKey.A_ACCEL).mListOfChannels.add(new ChannelDetails(Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_LN_Y,ChannelDataType.UINT12,2,ChannelDataEndian.LSB, CHANNEL_UNITS.METER_PER_SECOND_SQUARE));
			aMap.get(Configuration.Shimmer3.SensorMapKey.A_ACCEL).mListOfChannels.add(new ChannelDetails(Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_LN_Z,ChannelDataType.UINT12,2,ChannelDataEndian.LSB, CHANNEL_UNITS.METER_PER_SECOND_SQUARE));
			
			aMap.get(Configuration.Shimmer3.SensorMapKey.VBATT).mListOfChannels.add(new ChannelDetails(Configuration.Shimmer3.ObjectClusterSensorName.BATTERY,ChannelDataType.UINT12,2,ChannelDataEndian.LSB, CHANNEL_UNITS.MILLIVOLTS));

			//ADC
			aMap.get(Configuration.Shimmer3.SensorMapKey.EXT_EXP_ADC_A7).mListOfChannels.add(new ChannelDetails(Configuration.Shimmer3.ObjectClusterSensorName.EXT_EXP_A15,ChannelDataType.UINT12,2,ChannelDataEndian.LSB, CHANNEL_UNITS.MILLIVOLTS));
			aMap.get(Configuration.Shimmer3.SensorMapKey.EXT_EXP_ADC_A6).mListOfChannels.add(new ChannelDetails(Configuration.Shimmer3.ObjectClusterSensorName.EXT_EXP_A6,ChannelDataType.UINT12,2,ChannelDataEndian.LSB, CHANNEL_UNITS.MILLIVOLTS));
			aMap.get(Configuration.Shimmer3.SensorMapKey.EXT_EXP_ADC_A15).mListOfChannels.add(new ChannelDetails(Configuration.Shimmer3.ObjectClusterSensorName.EXT_EXP_A15,ChannelDataType.UINT12,2,ChannelDataEndian.LSB, CHANNEL_UNITS.MILLIVOLTS));

			aMap.get(Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A1).mListOfChannels.add(new ChannelDetails(Configuration.Shimmer3.ObjectClusterSensorName.INT_EXP_A1,ChannelDataType.UINT12,2,ChannelDataEndian.LSB, CHANNEL_UNITS.MILLIVOLTS));
			aMap.get(Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A12).mListOfChannels.add(new ChannelDetails(Configuration.Shimmer3.ObjectClusterSensorName.INT_EXP_A12,ChannelDataType.UINT12,2,ChannelDataEndian.LSB, CHANNEL_UNITS.MILLIVOLTS));
			aMap.get(Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A13).mListOfChannels.add(new ChannelDetails(Configuration.Shimmer3.ObjectClusterSensorName.INT_EXP_A13,ChannelDataType.UINT12,2,ChannelDataEndian.LSB, CHANNEL_UNITS.MILLIVOLTS));
			aMap.get(Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A14).mListOfChannels.add(new ChannelDetails(Configuration.Shimmer3.ObjectClusterSensorName.INT_EXP_A14,ChannelDataType.UINT12,2,ChannelDataEndian.LSB, CHANNEL_UNITS.MILLIVOLTS));

			//Bridge Amp High
			aMap.get(Configuration.Shimmer3.SensorMapKey.BRIDGE_AMP).mListOfChannels.add(new ChannelDetails(Configuration.Shimmer3.ObjectClusterSensorName.BRIDGE_AMP_HIGH,ChannelDataType.UINT12,2,ChannelDataEndian.LSB, CHANNEL_UNITS.MILLIVOLTS));
			aMap.get(Configuration.Shimmer3.SensorMapKey.BRIDGE_AMP).mListOfChannels.add(new ChannelDetails(Configuration.Shimmer3.ObjectClusterSensorName.BRIDGE_AMP_LOW,ChannelDataType.UINT12,2,ChannelDataEndian.LSB, CHANNEL_UNITS.MILLIVOLTS));
			
			//GSR
			aMap.get(Configuration.Shimmer3.SensorMapKey.GSR).mListOfChannels.add(new ChannelDetails(Configuration.Shimmer3.ObjectClusterSensorName.GSR,ChannelDataType.UINT16,2,ChannelDataEndian.LSB, CHANNEL_UNITS.KOHMS));

			//GyroMPU
			aMap.get(Configuration.Shimmer3.SensorMapKey.MPU9150_GYRO).mListOfChannels.add(new ChannelDetails(Configuration.Shimmer3.ObjectClusterSensorName.GYRO_X,ChannelDataType.INT16,2,ChannelDataEndian.MSB, CHANNEL_UNITS.DEGREES_PER_SECOND));
			aMap.get(Configuration.Shimmer3.SensorMapKey.MPU9150_GYRO).mListOfChannels.add(new ChannelDetails(Configuration.Shimmer3.ObjectClusterSensorName.GYRO_Y,ChannelDataType.INT16,2,ChannelDataEndian.MSB, CHANNEL_UNITS.DEGREES_PER_SECOND));
			aMap.get(Configuration.Shimmer3.SensorMapKey.MPU9150_GYRO).mListOfChannels.add(new ChannelDetails(Configuration.Shimmer3.ObjectClusterSensorName.GYRO_Z,ChannelDataType.INT16,2,ChannelDataEndian.MSB, CHANNEL_UNITS.DEGREES_PER_SECOND));
			
			aMap.get(Configuration.Shimmer3.SensorMapKey.LSM303DLHC_ACCEL).mListOfChannels.add(new ChannelDetails(Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_WR_X,ChannelDataType.INT16,2,ChannelDataEndian.LSB, CHANNEL_UNITS.METER_PER_SECOND_SQUARE));
			aMap.get(Configuration.Shimmer3.SensorMapKey.LSM303DLHC_ACCEL).mListOfChannels.add(new ChannelDetails(Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_WR_Y,ChannelDataType.INT16,2,ChannelDataEndian.LSB, CHANNEL_UNITS.METER_PER_SECOND_SQUARE));
			aMap.get(Configuration.Shimmer3.SensorMapKey.LSM303DLHC_ACCEL).mListOfChannels.add(new ChannelDetails(Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_WR_Z,ChannelDataType.INT16,2,ChannelDataEndian.LSB, CHANNEL_UNITS.METER_PER_SECOND_SQUARE));
			
			aMap.get(Configuration.Shimmer3.SensorMapKey.LSM303DLHC_MAG).mListOfChannels.add(new ChannelDetails(Configuration.Shimmer3.ObjectClusterSensorName.MAG_X,ChannelDataType.INT16,2,ChannelDataEndian.MSB, CHANNEL_UNITS.LOCAL_FLUX));
			aMap.get(Configuration.Shimmer3.SensorMapKey.LSM303DLHC_MAG).mListOfChannels.add(new ChannelDetails(Configuration.Shimmer3.ObjectClusterSensorName.MAG_Y,ChannelDataType.INT16,2,ChannelDataEndian.MSB, CHANNEL_UNITS.LOCAL_FLUX));
			aMap.get(Configuration.Shimmer3.SensorMapKey.LSM303DLHC_MAG).mListOfChannels.add(new ChannelDetails(Configuration.Shimmer3.ObjectClusterSensorName.MAG_Z,ChannelDataType.INT16,2,ChannelDataEndian.MSB, CHANNEL_UNITS.LOCAL_FLUX));

			aMap.get(Configuration.Shimmer3.SensorMapKey.MPU9150_ACCEL).mListOfChannels.add(new ChannelDetails(Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_MPU_MPL_X,ChannelDataType.INT16,2,ChannelDataEndian.MSB, CHANNEL_UNITS.METER_PER_SECOND_SQUARE));
			aMap.get(Configuration.Shimmer3.SensorMapKey.MPU9150_ACCEL).mListOfChannels.add(new ChannelDetails(Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_MPU_MPL_Y,ChannelDataType.INT16,2,ChannelDataEndian.MSB, CHANNEL_UNITS.METER_PER_SECOND_SQUARE));
			aMap.get(Configuration.Shimmer3.SensorMapKey.MPU9150_ACCEL).mListOfChannels.add(new ChannelDetails(Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_MPU_MPL_Z,ChannelDataType.INT16,2,ChannelDataEndian.MSB, CHANNEL_UNITS.METER_PER_SECOND_SQUARE));

			aMap.get(Configuration.Shimmer3.SensorMapKey.MPU9150_MAG).mListOfChannels.add(new ChannelDetails(Configuration.Shimmer3.ObjectClusterSensorName.MAG_MPU_X,ChannelDataType.INT16,2,ChannelDataEndian.LSB, CHANNEL_UNITS.LOCAL_FLUX));
			aMap.get(Configuration.Shimmer3.SensorMapKey.MPU9150_MAG).mListOfChannels.add(new ChannelDetails(Configuration.Shimmer3.ObjectClusterSensorName.MAG_MPU_Y,ChannelDataType.INT16,2,ChannelDataEndian.LSB, CHANNEL_UNITS.LOCAL_FLUX));
			aMap.get(Configuration.Shimmer3.SensorMapKey.MPU9150_MAG).mListOfChannels.add(new ChannelDetails(Configuration.Shimmer3.ObjectClusterSensorName.MAG_MPU_Z,ChannelDataType.INT16,2,ChannelDataEndian.LSB, CHANNEL_UNITS.LOCAL_FLUX));

			//bmp180 - pressure
			aMap.get(Configuration.Shimmer3.SensorMapKey.BMP180_PRESSURE).mListOfChannels.add(new ChannelDetails(Configuration.Shimmer3.ObjectClusterSensorName.PRESSURE_BMP180,ChannelDataType.UINT24,3,ChannelDataEndian.MSB, CHANNEL_UNITS.KPASCAL));
			//bmp180 - temp
			aMap.get(Configuration.Shimmer3.SensorMapKey.BMP180_PRESSURE).mListOfChannels.add(new ChannelDetails(Configuration.Shimmer3.ObjectClusterSensorName.TEMPERATURE_BMP180,ChannelDataType.UINT16,2,ChannelDataEndian.MSB, CHANNEL_UNITS.DEGREES_CELSUIS));
			
			//exg
			aMap.get(Configuration.Shimmer3.SensorMapKey.EXG1_24BIT).mListOfChannels.add(new ChannelDetails(Configuration.Shimmer3.ObjectClusterSensorName.EXG1_STATUS,ChannelDataType.UINT8,1,ChannelDataEndian.LSB, CHANNEL_UNITS.NO_UNITS));
			aMap.get(Configuration.Shimmer3.SensorMapKey.EXG1_24BIT).mListOfChannels.add(new ChannelDetails(Configuration.Shimmer3.ObjectClusterSensorName.EXG1_CH1_24BIT,ChannelDataType.INT24,3,ChannelDataEndian.MSB, CHANNEL_UNITS.MILLIVOLTS));
			aMap.get(Configuration.Shimmer3.SensorMapKey.EXG1_24BIT).mListOfChannels.add(new ChannelDetails(Configuration.Shimmer3.ObjectClusterSensorName.EXG1_CH2_24BIT,ChannelDataType.INT24,3,ChannelDataEndian.MSB, CHANNEL_UNITS.MILLIVOLTS));

			aMap.get(Configuration.Shimmer3.SensorMapKey.EXG2_24BIT).mListOfChannels.add(new ChannelDetails(Configuration.Shimmer3.ObjectClusterSensorName.EXG2_STATUS,ChannelDataType.UINT8,1,ChannelDataEndian.LSB, CHANNEL_UNITS.NO_UNITS));
			aMap.get(Configuration.Shimmer3.SensorMapKey.EXG2_24BIT).mListOfChannels.add(new ChannelDetails(Configuration.Shimmer3.ObjectClusterSensorName.EXG2_CH1_24BIT,ChannelDataType.INT24,3,ChannelDataEndian.MSB, CHANNEL_UNITS.MILLIVOLTS));
			aMap.get(Configuration.Shimmer3.SensorMapKey.EXG2_24BIT).mListOfChannels.add(new ChannelDetails(Configuration.Shimmer3.ObjectClusterSensorName.EXG2_CH2_24BIT,ChannelDataType.INT24,3,ChannelDataEndian.MSB, CHANNEL_UNITS.MILLIVOLTS));

			aMap.get(Configuration.Shimmer3.SensorMapKey.EXG1_16BIT).mListOfChannels.add(new ChannelDetails(Configuration.Shimmer3.ObjectClusterSensorName.EXG1_STATUS,ChannelDataType.UINT8,1,ChannelDataEndian.LSB, CHANNEL_UNITS.NO_UNITS));
			aMap.get(Configuration.Shimmer3.SensorMapKey.EXG1_16BIT).mListOfChannels.add(new ChannelDetails(Configuration.Shimmer3.ObjectClusterSensorName.EXG1_CH1_16BIT,ChannelDataType.INT16,2,ChannelDataEndian.MSB, CHANNEL_UNITS.MILLIVOLTS));
			aMap.get(Configuration.Shimmer3.SensorMapKey.EXG1_16BIT).mListOfChannels.add(new ChannelDetails(Configuration.Shimmer3.ObjectClusterSensorName.EXG1_CH2_16BIT,ChannelDataType.INT16,2,ChannelDataEndian.MSB, CHANNEL_UNITS.MILLIVOLTS));

			aMap.get(Configuration.Shimmer3.SensorMapKey.EXG2_16BIT).mListOfChannels.add(new ChannelDetails(Configuration.Shimmer3.ObjectClusterSensorName.EXG2_STATUS,ChannelDataType.UINT8,1,ChannelDataEndian.LSB, CHANNEL_UNITS.NO_UNITS));
			aMap.get(Configuration.Shimmer3.SensorMapKey.EXG2_16BIT).mListOfChannels.add(new ChannelDetails(Configuration.Shimmer3.ObjectClusterSensorName.EXG2_CH1_16BIT,ChannelDataType.INT16,2,ChannelDataEndian.MSB, CHANNEL_UNITS.MILLIVOLTS));
			aMap.get(Configuration.Shimmer3.SensorMapKey.EXG2_16BIT).mListOfChannels.add(new ChannelDetails(Configuration.Shimmer3.ObjectClusterSensorName.EXG2_CH2_16BIT,ChannelDataType.INT16,2,ChannelDataEndian.MSB, CHANNEL_UNITS.MILLIVOLTS));
			

			aMap.get(Configuration.Shimmer3.SensorMapKey.MPU9150_MPL_QUAT_6DOF).mListOfChannels.add(new ChannelDetails(Configuration.Shimmer3.ObjectClusterSensorName.QUAT_MPL_6DOF_W,ChannelDataType.INT32,4,ChannelDataEndian.MSB, CHANNEL_UNITS.NO_UNITS));
			aMap.get(Configuration.Shimmer3.SensorMapKey.MPU9150_MPL_QUAT_6DOF).mListOfChannels.add(new ChannelDetails(Configuration.Shimmer3.ObjectClusterSensorName.QUAT_MPL_6DOF_X,ChannelDataType.INT32,4,ChannelDataEndian.MSB, CHANNEL_UNITS.NO_UNITS));
			aMap.get(Configuration.Shimmer3.SensorMapKey.MPU9150_MPL_QUAT_6DOF).mListOfChannels.add(new ChannelDetails(Configuration.Shimmer3.ObjectClusterSensorName.QUAT_MPL_6DOF_Y,ChannelDataType.INT32,4,ChannelDataEndian.MSB, CHANNEL_UNITS.NO_UNITS));
			aMap.get(Configuration.Shimmer3.SensorMapKey.MPU9150_MPL_QUAT_6DOF).mListOfChannels.add(new ChannelDetails(Configuration.Shimmer3.ObjectClusterSensorName.QUAT_MPL_6DOF_Z,ChannelDataType.INT32,4,ChannelDataEndian.MSB, CHANNEL_UNITS.NO_UNITS));
			
			aMap.get(Configuration.Shimmer3.SensorMapKey.MPU9150_MPL_QUAT_9DOF).mListOfChannels.add(new ChannelDetails(Configuration.Shimmer3.ObjectClusterSensorName.QUAT_MPL_9DOF_W,ChannelDataType.INT32,4,ChannelDataEndian.MSB, CHANNEL_UNITS.NO_UNITS));
			aMap.get(Configuration.Shimmer3.SensorMapKey.MPU9150_MPL_QUAT_9DOF).mListOfChannels.add(new ChannelDetails(Configuration.Shimmer3.ObjectClusterSensorName.QUAT_MPL_9DOF_X,ChannelDataType.INT32,4,ChannelDataEndian.MSB, CHANNEL_UNITS.NO_UNITS));
			aMap.get(Configuration.Shimmer3.SensorMapKey.MPU9150_MPL_QUAT_9DOF).mListOfChannels.add(new ChannelDetails(Configuration.Shimmer3.ObjectClusterSensorName.QUAT_MPL_9DOF_Y,ChannelDataType.INT32,4,ChannelDataEndian.MSB, CHANNEL_UNITS.NO_UNITS));
			aMap.get(Configuration.Shimmer3.SensorMapKey.MPU9150_MPL_QUAT_9DOF).mListOfChannels.add(new ChannelDetails(Configuration.Shimmer3.ObjectClusterSensorName.QUAT_MPL_9DOF_Z,ChannelDataType.INT32,4,ChannelDataEndian.MSB, CHANNEL_UNITS.NO_UNITS));
			
			//euler
			aMap.get(Configuration.Shimmer3.SensorMapKey.MPU9150_MPL_EULER_6DOF).mListOfChannels.add(new ChannelDetails(Configuration.Shimmer3.ObjectClusterSensorName.EULER_MPL_6DOF_X,ChannelDataType.INT32,4,ChannelDataEndian.MSB, CHANNEL_UNITS.NO_UNITS));
			aMap.get(Configuration.Shimmer3.SensorMapKey.MPU9150_MPL_EULER_6DOF).mListOfChannels.add(new ChannelDetails(Configuration.Shimmer3.ObjectClusterSensorName.EULER_MPL_6DOF_Y,ChannelDataType.INT32,4,ChannelDataEndian.MSB, CHANNEL_UNITS.NO_UNITS));
			aMap.get(Configuration.Shimmer3.SensorMapKey.MPU9150_MPL_EULER_6DOF).mListOfChannels.add(new ChannelDetails(Configuration.Shimmer3.ObjectClusterSensorName.EULER_MPL_6DOF_Z,ChannelDataType.INT32,4,ChannelDataEndian.MSB, CHANNEL_UNITS.NO_UNITS));

			aMap.get(Configuration.Shimmer3.SensorMapKey.MPU9150_MPL_EULER_9DOF).mListOfChannels.add(new ChannelDetails(Configuration.Shimmer3.ObjectClusterSensorName.EULER_MPL_9DOF_X,ChannelDataType.INT32,4,ChannelDataEndian.MSB, CHANNEL_UNITS.NO_UNITS));
			aMap.get(Configuration.Shimmer3.SensorMapKey.MPU9150_MPL_EULER_9DOF).mListOfChannels.add(new ChannelDetails(Configuration.Shimmer3.ObjectClusterSensorName.EULER_MPL_9DOF_Y,ChannelDataType.INT32,4,ChannelDataEndian.MSB, CHANNEL_UNITS.NO_UNITS));
			aMap.get(Configuration.Shimmer3.SensorMapKey.MPU9150_MPL_EULER_9DOF).mListOfChannels.add(new ChannelDetails(Configuration.Shimmer3.ObjectClusterSensorName.EULER_MPL_9DOF_Z,ChannelDataType.INT32,4,ChannelDataEndian.MSB, CHANNEL_UNITS.NO_UNITS));

			//heading
			aMap.get(Configuration.Shimmer3.SensorMapKey.MPU9150_MPL_HEADING).mListOfChannels.add(new ChannelDetails(Configuration.Shimmer3.ObjectClusterSensorName.MPL_HEADING,ChannelDataType.INT32,4,ChannelDataEndian.MSB, CHANNEL_UNITS.DEGREES));

			//mpu temp
			aMap.get(Configuration.Shimmer3.SensorMapKey.MPU9150_TEMP).mListOfChannels.add(new ChannelDetails(Configuration.Shimmer3.ObjectClusterSensorName.MPL_TEMPERATURE,ChannelDataType.INT32,4,ChannelDataEndian.MSB, CHANNEL_UNITS.DEGREES_CELSUIS));

			//mpl pedom
			aMap.get(Configuration.Shimmer3.SensorMapKey.MPU9150_MPL_PEDOMETER).mListOfChannels.add(new ChannelDetails(Configuration.Shimmer3.ObjectClusterSensorName.MPL_PEDOM_CNT,ChannelDataType.UINT32,4,ChannelDataEndian.MSB, CHANNEL_UNITS.NO_UNITS));
			aMap.get(Configuration.Shimmer3.SensorMapKey.MPU9150_MPL_PEDOMETER).mListOfChannels.add(new ChannelDetails(Configuration.Shimmer3.ObjectClusterSensorName.MPL_PEDOM_TIME,ChannelDataType.UINT32,4,ChannelDataEndian.MSB, CHANNEL_UNITS.NO_UNITS));

			//mpl tap
			aMap.get(Configuration.Shimmer3.SensorMapKey.MPU9150_MPL_TAP).mListOfChannels.add(new ChannelDetails(Configuration.Shimmer3.ObjectClusterSensorName.TAPDIRANDTAPCNT,ChannelDataType.UINT8,1,ChannelDataEndian.MSB, CHANNEL_UNITS.NO_UNITS));

			//mpl motion orient
			aMap.get(Configuration.Shimmer3.SensorMapKey.MPU9150_MPL_MOTION_ORIENT).mListOfChannels.add(new ChannelDetails(Configuration.Shimmer3.ObjectClusterSensorName.TAPDIRANDTAPCNT,ChannelDataType.UINT8,1,ChannelDataEndian.MSB, CHANNEL_UNITS.NO_UNITS));

			//mpl gyro cal
			aMap.get(Configuration.Shimmer3.SensorMapKey.MPU9150_MPL_GYRO).mListOfChannels.add(new ChannelDetails(Configuration.Shimmer3.ObjectClusterSensorName.GYRO_MPU_MPL_X,ChannelDataType.INT32,4,ChannelDataEndian.MSB, CHANNEL_UNITS.DEGREES_PER_SECOND));
			aMap.get(Configuration.Shimmer3.SensorMapKey.MPU9150_MPL_GYRO).mListOfChannels.add(new ChannelDetails(Configuration.Shimmer3.ObjectClusterSensorName.GYRO_MPU_MPL_Y,ChannelDataType.INT32,4,ChannelDataEndian.MSB, CHANNEL_UNITS.DEGREES_PER_SECOND));
			aMap.get(Configuration.Shimmer3.SensorMapKey.MPU9150_MPL_GYRO).mListOfChannels.add(new ChannelDetails(Configuration.Shimmer3.ObjectClusterSensorName.GYRO_MPU_MPL_Z,ChannelDataType.INT32,4,ChannelDataEndian.MSB, CHANNEL_UNITS.DEGREES_PER_SECOND));

			//mpl accel cal
			aMap.get(Configuration.Shimmer3.SensorMapKey.MPU9150_MPL_ACCEL).mListOfChannels.add(new ChannelDetails(Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_MPU_MPL_X,ChannelDataType.INT32,4,ChannelDataEndian.MSB, CHANNEL_UNITS.GRAVITY));
			aMap.get(Configuration.Shimmer3.SensorMapKey.MPU9150_MPL_ACCEL).mListOfChannels.add(new ChannelDetails(Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_MPU_MPL_Y,ChannelDataType.INT32,4,ChannelDataEndian.MSB, CHANNEL_UNITS.GRAVITY));
			aMap.get(Configuration.Shimmer3.SensorMapKey.MPU9150_MPL_ACCEL).mListOfChannels.add(new ChannelDetails(Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_MPU_MPL_Z,ChannelDataType.INT32,4,ChannelDataEndian.MSB, CHANNEL_UNITS.GRAVITY));

			//mpl mag cal
			aMap.get(Configuration.Shimmer3.SensorMapKey.MPU9150_MPL_MAG).mListOfChannels.add(new ChannelDetails(Configuration.Shimmer3.ObjectClusterSensorName.MAG_MPU_MPL_X,ChannelDataType.INT32,4,ChannelDataEndian.MSB, CHANNEL_UNITS.U_TESLA));
			aMap.get(Configuration.Shimmer3.SensorMapKey.MPU9150_MPL_MAG).mListOfChannels.add(new ChannelDetails(Configuration.Shimmer3.ObjectClusterSensorName.MAG_MPU_MPL_Y,ChannelDataType.INT32,4,ChannelDataEndian.MSB, CHANNEL_UNITS.U_TESLA));
			aMap.get(Configuration.Shimmer3.SensorMapKey.MPU9150_MPL_MAG).mListOfChannels.add(new ChannelDetails(Configuration.Shimmer3.ObjectClusterSensorName.MAG_MPU_MPL_Z,ChannelDataType.INT32,4,ChannelDataEndian.MSB, CHANNEL_UNITS.U_TESLA));
			
			// Raw 6DOF quaterians from the DMP hardware module of the MPU9150
			aMap.get(Configuration.Shimmer3.SensorMapKey.MPU9150_MPL_QUAT_6DOF_RAW).mListOfChannels.add(new ChannelDetails(Configuration.Shimmer3.ObjectClusterSensorName.QUAT_DMP_6DOF_W,ChannelDataType.INT32,4,ChannelDataEndian.MSB, CHANNEL_UNITS.NO_UNITS));
			aMap.get(Configuration.Shimmer3.SensorMapKey.MPU9150_MPL_QUAT_6DOF_RAW).mListOfChannels.add(new ChannelDetails(Configuration.Shimmer3.ObjectClusterSensorName.QUAT_DMP_6DOF_X,ChannelDataType.INT32,4,ChannelDataEndian.MSB, CHANNEL_UNITS.NO_UNITS));
			aMap.get(Configuration.Shimmer3.SensorMapKey.MPU9150_MPL_QUAT_6DOF_RAW).mListOfChannels.add(new ChannelDetails(Configuration.Shimmer3.ObjectClusterSensorName.QUAT_DMP_6DOF_Y,ChannelDataType.INT32,4,ChannelDataEndian.MSB, CHANNEL_UNITS.NO_UNITS));
			aMap.get(Configuration.Shimmer3.SensorMapKey.MPU9150_MPL_QUAT_6DOF_RAW).mListOfChannels.add(new ChannelDetails(Configuration.Shimmer3.ObjectClusterSensorName.QUAT_DMP_6DOF_Z,ChannelDataType.INT32,4,ChannelDataEndian.MSB, CHANNEL_UNITS.NO_UNITS));
			
			
			mSensorMapRef = Collections.unmodifiableMap(aMap);
	    }
	    
	    public static final Map<String, SensorGroupingDetails> mSensorGroupingMap;
	    static {
	        Map<String, SensorGroupingDetails> aMap = new LinkedHashMap<String, SensorGroupingDetails>();
		
			//Sensor Grouping for Configuration Panel 'tile' generation. 
			aMap.put(Configuration.Shimmer3.GuiLabelSensorTiles.LOW_NOISE_ACCEL, new SensorGroupingDetails(
					Arrays.asList(Configuration.Shimmer3.SensorMapKey.A_ACCEL)));
			aMap.put(Configuration.Shimmer3.GuiLabelSensorTiles.WIDE_RANGE_ACCEL, new SensorGroupingDetails(
					Arrays.asList(Configuration.Shimmer3.SensorMapKey.LSM303DLHC_ACCEL)));
			aMap.put(Configuration.Shimmer3.GuiLabelSensorTiles.GYRO, new SensorGroupingDetails(
					Arrays.asList(Configuration.Shimmer3.SensorMapKey.MPU9150_GYRO)));
			aMap.put(Configuration.Shimmer3.GuiLabelSensorTiles.MAG, new SensorGroupingDetails(
					Arrays.asList(Configuration.Shimmer3.SensorMapKey.LSM303DLHC_MAG)));
			aMap.put(Configuration.Shimmer3.GuiLabelSensorTiles.PRESSURE_TEMPERATURE, new SensorGroupingDetails(
					Arrays.asList(Configuration.Shimmer3.SensorMapKey.BMP180_PRESSURE)));
			aMap.put(Configuration.Shimmer3.GuiLabelSensorTiles.BATTERY_MONITORING, new SensorGroupingDetails(
					Arrays.asList(Configuration.Shimmer3.SensorMapKey.VBATT)));
			aMap.put(Configuration.Shimmer3.GuiLabelSensorTiles.EXTERNAL_EXPANSION_ADC, new SensorGroupingDetails(
					Arrays.asList(Configuration.Shimmer3.SensorMapKey.EXT_EXP_ADC_A6,
								Configuration.Shimmer3.SensorMapKey.EXT_EXP_ADC_A7,
								Configuration.Shimmer3.SensorMapKey.EXT_EXP_ADC_A15)));
			aMap.put(Configuration.Shimmer3.GuiLabelSensorTiles.GSR, new SensorGroupingDetails(
					Arrays.asList(Configuration.Shimmer3.SensorMapKey.GSR,
								Configuration.Shimmer3.SensorMapKey.PPG_DUMMY)));
	//							Configuration.Shimmer3.SensorMapKey.PPG_A12,
	//							Configuration.Shimmer3.SensorMapKey.PPG_A13)));
			aMap.put(Configuration.Shimmer3.GuiLabelSensorTiles.EXG, new SensorGroupingDetails(
					Arrays.asList(Configuration.Shimmer3.SensorMapKey.ECG,
								Configuration.Shimmer3.SensorMapKey.EMG,
								Configuration.Shimmer3.SensorMapKey.EXG_TEST,
								Configuration.Shimmer3.SensorMapKey.EXG_CUSTOM,
								Configuration.Shimmer3.SensorMapKey.EXG_RESPIRATION)));
			aMap.put(Configuration.Shimmer3.GuiLabelSensorTiles.PROTO3_MINI, new SensorGroupingDetails(
					Arrays.asList(Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A12,
								Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A1,
								Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A13,
								Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A14)));
			aMap.put(Configuration.Shimmer3.GuiLabelSensorTiles.PROTO3_DELUXE, new SensorGroupingDetails(
					Arrays.asList(Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A12,
								Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A1,
								Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A13,
								Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A14)));
			aMap.put(Configuration.Shimmer3.GuiLabelSensorTiles.PROTO3_DELUXE_SUPP, new SensorGroupingDetails(
					Arrays.asList(Configuration.Shimmer3.SensorMapKey.PPG1_DUMMY,
								Configuration.Shimmer3.SensorMapKey.PPG2_DUMMY)));
			aMap.put(Configuration.Shimmer3.GuiLabelSensorTiles.BRIDGE_AMPLIFIER, new SensorGroupingDetails(
					Arrays.asList(Configuration.Shimmer3.SensorMapKey.BRIDGE_AMP,
								Configuration.Shimmer3.SensorMapKey.RESISTANCE_AMP)));
			//aMap.put(Configuration.Shimmer3.GuiLabelSensorTiles.BRIDGE_AMPLIFIER_SUPP, new SensorTileDetails(
			//		Arrays.asList(Configuration.Shimmer3.SensorMapKey.SKIN_TEMP_PROBE)));
			aMap.put(Configuration.Shimmer3.GuiLabelSensorTiles.HIGH_G_ACCEL, new SensorGroupingDetails(
					Arrays.asList(Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A12, //X-axis
								Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A13, //Y-axis
								Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A14, //Z-axis
								Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A1))); //unused but accessible
			aMap.put(Configuration.Shimmer3.GuiLabelSensorTiles.INTERNAL_EXPANSION_ADC, new SensorGroupingDetails(
					Arrays.asList(Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A1,
								Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A12,
								Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A13,
								Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A14)));
			aMap.put(Configuration.Shimmer3.GuiLabelSensorTiles.MPU_ACCEL_GYRO_MAG, new SensorGroupingDetails(
					Arrays.asList(Configuration.Shimmer3.SensorMapKey.MPU9150_MPL_ACCEL,
								Configuration.Shimmer3.SensorMapKey.MPU9150_MPL_GYRO,
								Configuration.Shimmer3.SensorMapKey.MPU9150_MPL_MAG)));
			aMap.put(Configuration.Shimmer3.GuiLabelSensorTiles.MPU_OTHER, new SensorGroupingDetails(
					Arrays.asList(Configuration.Shimmer3.SensorMapKey.MPU9150_TEMP,
								Configuration.Shimmer3.SensorMapKey.MPU9150_MPL_QUAT_6DOF)));
			
			//Not implemented: GUI_LABEL_CHANNEL_GROUPING_GPS
			
			// ChannelTiles that have compatibility considerations (used to auto generate tiles in GUI)
			aMap.get(Configuration.Shimmer3.GuiLabelSensorTiles.LOW_NOISE_ACCEL).mListOfCompatibleVersionInfo = CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW;
			aMap.get(Configuration.Shimmer3.GuiLabelSensorTiles.GYRO).mListOfCompatibleVersionInfo = CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW;
			aMap.get(Configuration.Shimmer3.GuiLabelSensorTiles.MAG).mListOfCompatibleVersionInfo = CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW;
			aMap.get(Configuration.Shimmer3.GuiLabelSensorTiles.BATTERY_MONITORING).mListOfCompatibleVersionInfo = CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardAndFw;
			aMap.get(Configuration.Shimmer3.GuiLabelSensorTiles.WIDE_RANGE_ACCEL).mListOfCompatibleVersionInfo = CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardAndFw;
			aMap.get(Configuration.Shimmer3.GuiLabelSensorTiles.PRESSURE_TEMPERATURE).mListOfCompatibleVersionInfo = CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW;
			aMap.get(Configuration.Shimmer3.GuiLabelSensorTiles.EXTERNAL_EXPANSION_ADC).mListOfCompatibleVersionInfo = CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW;
			//aMap.get(Configuration.Shimmer3.GuiLabelSensorTiles.INTERNAL_EXPANSION_ADC).mListOfCompatibleVersionInfo = CompatibilityInfoForMaps.listOfCompatibleVersionInfoGsr;
			aMap.get(Configuration.Shimmer3.GuiLabelSensorTiles.GSR).mListOfCompatibleVersionInfo = CompatibilityInfoForMaps.listOfCompatibleVersionInfoGsr;
			aMap.get(Configuration.Shimmer3.GuiLabelSensorTiles.EXG).mListOfCompatibleVersionInfo = CompatibilityInfoForMaps.listOfCompatibleVersionInfoExg;
			aMap.get(Configuration.Shimmer3.GuiLabelSensorTiles.PROTO3_MINI).mListOfCompatibleVersionInfo = CompatibilityInfoForMaps.listOfCompatibleVersionInfoProto3Mini;
			aMap.get(Configuration.Shimmer3.GuiLabelSensorTiles.PROTO3_DELUXE).mListOfCompatibleVersionInfo = CompatibilityInfoForMaps.listOfCompatibleVersionInfoProto3Deluxe;
			aMap.get(Configuration.Shimmer3.GuiLabelSensorTiles.PROTO3_DELUXE_SUPP).mListOfCompatibleVersionInfo = CompatibilityInfoForMaps.listOfCompatibleVersionInfoProto3Deluxe;
			aMap.get(Configuration.Shimmer3.GuiLabelSensorTiles.BRIDGE_AMPLIFIER).mListOfCompatibleVersionInfo = CompatibilityInfoForMaps.listOfCompatibleVersionInfoBrAmp;
			//aMap.get(Configuration.Shimmer3.GuiLabelSensorTiles.BRIDGE_AMPLIFIER_SUPP).mListOfCompatibleVersionInfo = CompatibilityInfoForMaps.listOfCompatibleVersionInfoBrAmp;
			aMap.get(Configuration.Shimmer3.GuiLabelSensorTiles.HIGH_G_ACCEL).mListOfCompatibleVersionInfo = CompatibilityInfoForMaps.listOfCompatibleVersionInfoHighGAccel;
			//mShimmerChannelGroupingMap.get(Configuration.Shimmer3.GUI_LABEL_CHANNEL_GROUPING_GPS).mCompatibleVersionInfo = CompatibilityInfoForMaps.listOfCompatibleVersionInfoGps;
			aMap.get(Configuration.Shimmer3.GuiLabelSensorTiles.MPU_ACCEL_GYRO_MAG).mListOfCompatibleVersionInfo = CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors;
			aMap.get(Configuration.Shimmer3.GuiLabelSensorTiles.MPU_OTHER).mListOfCompatibleVersionInfo = CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors;
			
//			// For loop to automatically inherit associated channel configuration options from mSensorMap in the aMap
//			for (String channelGroup : aMap.keySet()) {
//				// Ok to clear here because variable is initiated in the class
//				aMap.get(channelGroup).mListOfConfigOptionKeysAssociated.clear();
//				for (Integer channel : aMap.get(channelGroup).mListOfSensorMapKeysAssociated) {
//					List<String> associatedConfigOptions = Configuration.Shimmer3.mSensorMapRef.get(channel).mListOfConfigOptionKeysAssociated;
//					if (associatedConfigOptions != null) {
//						for (String configOption : associatedConfigOptions) {
//							// do not add duplicates
//							if (!(aMap.get(channelGroup).mListOfConfigOptionKeysAssociated.contains(configOption))) {
//								aMap.get(channelGroup).mListOfConfigOptionKeysAssociated.add(configOption);
//							}
//						}
//					}
//				}
//			}
			
			aMap.get(Configuration.Shimmer3.GuiLabelSensorTiles.GSR).mListOfConfigOptionKeysAssociated.add(Configuration.Shimmer3.GuiLabelConfig.PPG_ADC_SELECTION);
			aMap.get(Configuration.Shimmer3.GuiLabelSensorTiles.PROTO3_DELUXE_SUPP).mListOfConfigOptionKeysAssociated.add(Configuration.Shimmer3.GuiLabelConfig.PPG1_ADC_SELECTION);
			aMap.get(Configuration.Shimmer3.GuiLabelSensorTiles.PROTO3_DELUXE_SUPP).mListOfConfigOptionKeysAssociated.add(Configuration.Shimmer3.GuiLabelConfig.PPG2_ADC_SELECTION);
			
	        mSensorGroupingMap = Collections.unmodifiableMap(aMap);
	    }

	    public static final Map<String, SensorConfigOptionDetails> mConfigOptionsMap;
	    static {
	        Map<String, SensorConfigOptionDetails> aMap = new LinkedHashMap<String, SensorConfigOptionDetails>();
	        
			// Assemble the channel configuration options map
			aMap.put(Configuration.Shimmer3.GuiLabelConfig.SHIMMER_USER_ASSIGNED_NAME, 
					new SensorConfigOptionDetails(SensorConfigOptionDetails.GUI_COMPONENT_TYPE.TEXTFIELD,
											CompatibilityInfoForMaps.listOfCompatibleVersionInfoLogging));
			aMap.put(Configuration.Shimmer3.GuiLabelConfig.SHIMMER_SAMPLING_RATE, 
					new SensorConfigOptionDetails(SensorConfigOptionDetails.GUI_COMPONENT_TYPE.TEXTFIELD));
			aMap.put(Configuration.Shimmer3.GuiLabelConfig.SHIMMER_MAC_FROM_INFOMEM, 
					new SensorConfigOptionDetails(SensorConfigOptionDetails.GUI_COMPONENT_TYPE.TEXTFIELD));
			aMap.put(Configuration.Shimmer3.GuiLabelConfig.BUFFER_SIZE, 
					new SensorConfigOptionDetails(SensorConfigOptionDetails.GUI_COMPONENT_TYPE.TEXTFIELD));
			aMap.put(Configuration.Shimmer3.GuiLabelConfig.CONFIG_TIME, 
					new SensorConfigOptionDetails(SensorConfigOptionDetails.GUI_COMPONENT_TYPE.TEXTFIELD,
											CompatibilityInfoForMaps.listOfCompatibleVersionInfoLogging));
			aMap.put(Configuration.Shimmer3.GuiLabelConfig.TRIAL_NAME, 
					new SensorConfigOptionDetails(SensorConfigOptionDetails.GUI_COMPONENT_TYPE.TEXTFIELD,
											CompatibilityInfoForMaps.listOfCompatibleVersionInfoLogging));
			aMap.put(Configuration.Shimmer3.GuiLabelConfig.EXPERIMENT_ID, 
					new SensorConfigOptionDetails(SensorConfigOptionDetails.GUI_COMPONENT_TYPE.TEXTFIELD,
											CompatibilityInfoForMaps.listOfCompatibleVersionInfoLogging));

			aMap.put(Configuration.Shimmer3.GuiLabelConfig.EXPERIMENT_NUMBER_OF_SHIMMERS, 
					new SensorConfigOptionDetails(SensorConfigOptionDetails.GUI_COMPONENT_TYPE.TEXTFIELD,
											CompatibilityInfoForMaps.listOfCompatibleVersionInfoSdLog));
			aMap.put(Configuration.Shimmer3.GuiLabelConfig.EXPERIMENT_DURATION_ESTIMATED, 
					new SensorConfigOptionDetails(SensorConfigOptionDetails.GUI_COMPONENT_TYPE.TEXTFIELD,
											CompatibilityInfoForMaps.listOfCompatibleVersionInfoSdLog));
			aMap.put(Configuration.Shimmer3.GuiLabelConfig.EXPERIMENT_DURATION_MAXIMUM, 
					new SensorConfigOptionDetails(SensorConfigOptionDetails.GUI_COMPONENT_TYPE.TEXTFIELD,
											CompatibilityInfoForMaps.listOfCompatibleVersionInfoSdLog));
			aMap.put(Configuration.Shimmer3.GuiLabelConfig.BROADCAST_INTERVAL, 
					new SensorConfigOptionDetails(SensorConfigOptionDetails.GUI_COMPONENT_TYPE.TEXTFIELD,
											new ArrayList(){}));
			
			aMap.put(Configuration.Shimmer3.GuiLabelConfig.BLUETOOTH_BAUD_RATE, 
					new SensorConfigOptionDetails(Configuration.Shimmer3.ListofBluetoothBaudRates, 
											Configuration.Shimmer3.ListofBluetoothBaudRatesConfigValues, 
											SensorConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX,
											CompatibilityInfoForMaps.listOfCompatibleVersionInfoStreaming));
			
			aMap.put(Configuration.Shimmer3.GuiLabelConfig.LSM303DLHC_ACCEL_RANGE, 
					new SensorConfigOptionDetails(Configuration.Shimmer3.ListofAccelRange, 
											Configuration.Shimmer3.ListofLSM303DLHCAccelRangeConfigValues, 
											SensorConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX));
			
			aMap.put(Configuration.Shimmer3.GuiLabelConfig.LSM303DLHC_ACCEL_RATE, 
					new SensorConfigOptionDetails(Configuration.Shimmer3.ListofLSM303DLHCAccelRate, 
											Configuration.Shimmer3.ListofLSM303DLHCAccelRateConfigValues, 
											SensorConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX));
			aMap.get(Configuration.Shimmer3.GuiLabelConfig.LSM303DLHC_ACCEL_RATE).setGuiValues(SensorConfigOptionDetails.VALUE_INDEXES.LSM303DLHC_ACCEL_RATE.IS_LPM, Configuration.Shimmer3.ListofLSM303DLHCAccelRateLpm);
			aMap.get(Configuration.Shimmer3.GuiLabelConfig.LSM303DLHC_ACCEL_RATE).setConfigValues(SensorConfigOptionDetails.VALUE_INDEXES.LSM303DLHC_ACCEL_RATE.IS_LPM, Configuration.Shimmer3.ListofLSM303DLHCAccelRateLpmConfigValues);

//			if(mLowPowerAccelWR) {
//				aMap.put(Configuration.Shimmer3.GuiLabelConfig.LSM303DLHC_ACCEL_RATE, 
//						new SensorConfigOptionDetails(Configuration.Shimmer3.ListofLSM303DLHCAccelRateLpm, 
//												Configuration.Shimmer3.ListofLSM303DLHCAccelRateLpmConfigValues, 
//												SensorConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX));
//			}
//			else {
//				aMap.put(Configuration.Shimmer3.GuiLabelConfig.LSM303DLHC_ACCEL_RATE, 
//						new SensorConfigOptionDetails(Configuration.Shimmer3.ListofLSM303DLHCAccelRate, 
//												Configuration.Shimmer3.ListofLSM303DLHCAccelRateConfigValues, 
//												SensorConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX));
//			}
			
			aMap.put(Configuration.Shimmer3.GuiLabelConfig.MPU9150_GYRO_RANGE, 
					new SensorConfigOptionDetails(Configuration.Shimmer3.ListofGyroRange, 
											Configuration.Shimmer3.ListofMPU9150GyroRangeConfigValues, 
											SensorConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX,
											CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW));
			aMap.put(Configuration.Shimmer3.GuiLabelConfig.LSM303DLHC_MAG_RANGE, 
					new SensorConfigOptionDetails(Configuration.Shimmer3.ListofMagRange, 
											Configuration.Shimmer3.ListofMagRangeConfigValues, 
											SensorConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX,
											CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW));
			aMap.put(Configuration.Shimmer3.GuiLabelConfig.LSM303DLHC_MAG_RATE, 
					new SensorConfigOptionDetails(Configuration.Shimmer3.ListofLSM303DLHCMagRate, 
											Configuration.Shimmer3.ListofLSM303DLHCMagRateConfigValues, 
											SensorConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX,
											CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW));
			aMap.put(Configuration.Shimmer3.GuiLabelConfig.PRESSURE_RESOLUTION, 
					new SensorConfigOptionDetails(Configuration.Shimmer3.ListofPressureResolution, 
											Configuration.Shimmer3.ListofPressureResolutionConfigValues, 
											SensorConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX,
											CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW));

			aMap.put(Configuration.Shimmer3.GuiLabelConfig.GSR_RANGE, 
					new SensorConfigOptionDetails(Configuration.Shimmer3.ListofGSRRange, 
											Configuration.Shimmer3.ListofGSRRangeConfigValues, 
											SensorConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX,
											CompatibilityInfoForMaps.listOfCompatibleVersionInfoGsr));
			
			aMap.put(Configuration.Shimmer3.GuiLabelConfig.EXG_GAIN, 
					new SensorConfigOptionDetails(Configuration.Shimmer3.ListOfExGGain, 
											Configuration.Shimmer3.ListOfExGGainConfigValues, 
											SensorConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX,
											CompatibilityInfoForMaps.listOfCompatibleVersionInfoExg));
			aMap.put(Configuration.Shimmer3.GuiLabelConfig.EXG_RESOLUTION, 
					new SensorConfigOptionDetails(Configuration.Shimmer3.ListOfExGResolutions, 
											Configuration.Shimmer3.ListOfExGResolutionsConfigValues, 
											SensorConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX,
											CompatibilityInfoForMaps.listOfCompatibleVersionInfoExg));

			//Advanced ExG		
			aMap.put(Configuration.Shimmer3.GuiLabelConfig.EXG_REFERENCE_ELECTRODE, 
					new SensorConfigOptionDetails(Configuration.Shimmer3.ListOfECGReferenceElectrode, 
											Configuration.Shimmer3.ListOfECGReferenceElectrodeConfigValues, 
											SensorConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX,
											CompatibilityInfoForMaps.listOfCompatibleVersionInfoExg));
			aMap.get(Configuration.Shimmer3.GuiLabelConfig.EXG_REFERENCE_ELECTRODE).setGuiValues(SensorConfigOptionDetails.VALUE_INDEXES.EXG_REFERENCE_ELECTRODE.EMG, Configuration.Shimmer3.ListOfEMGReferenceElectrode);
			aMap.get(Configuration.Shimmer3.GuiLabelConfig.EXG_REFERENCE_ELECTRODE).setConfigValues(SensorConfigOptionDetails.VALUE_INDEXES.EXG_REFERENCE_ELECTRODE.EMG, Configuration.Shimmer3.ListOfEMGReferenceElectrodeConfigValues);
			aMap.get(Configuration.Shimmer3.GuiLabelConfig.EXG_REFERENCE_ELECTRODE).setGuiValues(SensorConfigOptionDetails.VALUE_INDEXES.EXG_REFERENCE_ELECTRODE.CUSTOM, Configuration.Shimmer3.ListOfExGReferenceElectrodeAll);
			aMap.get(Configuration.Shimmer3.GuiLabelConfig.EXG_REFERENCE_ELECTRODE).setConfigValues(SensorConfigOptionDetails.VALUE_INDEXES.EXG_REFERENCE_ELECTRODE.CUSTOM, Configuration.Shimmer3.ListOfExGReferenceElectrodeConfigValuesAll);
			aMap.get(Configuration.Shimmer3.GuiLabelConfig.EXG_REFERENCE_ELECTRODE).setGuiValues(SensorConfigOptionDetails.VALUE_INDEXES.EXG_REFERENCE_ELECTRODE.RESP, Configuration.Shimmer3.ListOfRespReferenceElectrode);
			aMap.get(Configuration.Shimmer3.GuiLabelConfig.EXG_REFERENCE_ELECTRODE).setConfigValues(SensorConfigOptionDetails.VALUE_INDEXES.EXG_REFERENCE_ELECTRODE.RESP, Configuration.Shimmer3.ListOfRespReferenceElectrodeConfigValues);
			aMap.get(Configuration.Shimmer3.GuiLabelConfig.EXG_REFERENCE_ELECTRODE).setGuiValues(SensorConfigOptionDetails.VALUE_INDEXES.EXG_REFERENCE_ELECTRODE.TEST, Configuration.Shimmer3.ListOfTestReferenceElectrode);
			aMap.get(Configuration.Shimmer3.GuiLabelConfig.EXG_REFERENCE_ELECTRODE).setConfigValues(SensorConfigOptionDetails.VALUE_INDEXES.EXG_REFERENCE_ELECTRODE.TEST, Configuration.Shimmer3.ListOfTestReferenceElectrodeConfigValues);
			
			aMap.put(Configuration.Shimmer3.GuiLabelConfig.EXG_BYTES, 
					new SensorConfigOptionDetails(SensorConfigOptionDetails.GUI_COMPONENT_TYPE.JPANEL,
											CompatibilityInfoForMaps.listOfCompatibleVersionInfoExg));
			
			aMap.put(Configuration.Shimmer3.GuiLabelConfig.EXG_RATE, 
					new SensorConfigOptionDetails(Configuration.Shimmer3.ListOfExGRate, 
											Configuration.Shimmer3.ListOfExGRateConfigValues, 
											SensorConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX,
											CompatibilityInfoForMaps.listOfCompatibleVersionInfoExg));
			aMap.put(Configuration.Shimmer3.GuiLabelConfig.EXG_LEAD_OFF_DETECTION, 
					new SensorConfigOptionDetails(Configuration.Shimmer3.ListOfExGLeadOffDetection, 
											Configuration.Shimmer3.ListOfExGLeadOffDetectionConfigValues, 
											SensorConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX,
											CompatibilityInfoForMaps.listOfCompatibleVersionInfoExg));
			aMap.put(Configuration.Shimmer3.GuiLabelConfig.EXG_LEAD_OFF_CURRENT, 
					new SensorConfigOptionDetails(Configuration.Shimmer3.ListOfExGLeadOffCurrent, 
											Configuration.Shimmer3.ListOfExGLeadOffCurrentConfigValues, 
											SensorConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX,
											CompatibilityInfoForMaps.listOfCompatibleVersionInfoExg));
			aMap.put(Configuration.Shimmer3.GuiLabelConfig.EXG_LEAD_OFF_COMPARATOR, 
					new SensorConfigOptionDetails(Configuration.Shimmer3.ListOfExGLeadOffComparator, 
											Configuration.Shimmer3.ListOfExGLeadOffComparatorConfigValues, 
											SensorConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX,
											CompatibilityInfoForMaps.listOfCompatibleVersionInfoExg));
			aMap.put(Configuration.Shimmer3.GuiLabelConfig.EXG_RESPIRATION_DETECT_FREQ, 
					new SensorConfigOptionDetails(Configuration.Shimmer3.ListOfExGRespirationDetectFreq, 
											Configuration.Shimmer3.ListOfExGRespirationDetectFreqConfigValues, 
											SensorConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX,
											CompatibilityInfoForMaps.listOfCompatibleVersionInfoRespiration));
			aMap.put(Configuration.Shimmer3.GuiLabelConfig.EXG_RESPIRATION_DETECT_PHASE, 
					new SensorConfigOptionDetails(Configuration.Shimmer3.ListOfExGRespirationDetectPhase32khz, 
											Configuration.Shimmer3.ListOfExGRespirationDetectPhase32khzConfigValues, 
											SensorConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX,
											CompatibilityInfoForMaps.listOfCompatibleVersionInfoRespiration));
			aMap.get(Configuration.Shimmer3.GuiLabelConfig.EXG_RESPIRATION_DETECT_PHASE).setGuiValues(SensorConfigOptionDetails.VALUE_INDEXES.EXG_RESPIRATION_DETECT_PHASE.PHASE_64KHZ, Configuration.Shimmer3.ListOfExGRespirationDetectPhase64khz);
			aMap.get(Configuration.Shimmer3.GuiLabelConfig.EXG_RESPIRATION_DETECT_PHASE).setConfigValues(SensorConfigOptionDetails.VALUE_INDEXES.EXG_RESPIRATION_DETECT_PHASE.PHASE_64KHZ, Configuration.Shimmer3.ListOfExGRespirationDetectPhase64khzConfigValues);
			
			//MPL Options
			aMap.put(Configuration.Shimmer3.GuiLabelConfig.MPU9150_ACCEL_RANGE, 
					new SensorConfigOptionDetails(Configuration.Shimmer3.ListofMPU9150AccelRange, 
											Configuration.Shimmer3.ListofMPU9150AccelRangeConfigValues, 
											SensorConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX,
											CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors));
			aMap.put(Configuration.Shimmer3.GuiLabelConfig.MPU9150_DMP_GYRO_CAL, 
					new SensorConfigOptionDetails(Configuration.Shimmer3.ListofMPU9150MplCalibrationOptions, 
											Configuration.Shimmer3.ListofMPU9150MplCalibrationOptionsConfigValues, 
											SensorConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX,
											CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors));
			aMap.put(Configuration.Shimmer3.GuiLabelConfig.MPU9150_LPF, 
					new SensorConfigOptionDetails(Configuration.Shimmer3.ListofMPU9150MplLpfOptions, 
											Configuration.Shimmer3.ListofMPU9150MplLpfOptionsConfigValues, 
											SensorConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX,
											CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors));
			aMap.put(Configuration.Shimmer3.GuiLabelConfig.MPU9150_MPL_RATE, 
					new SensorConfigOptionDetails(Configuration.Shimmer3.ListofMPU9150MplRate, 
											Configuration.Shimmer3.ListofMPU9150MplRateConfigValues, 
											SensorConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX,
											CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors));
			aMap.put(Configuration.Shimmer3.GuiLabelConfig.MPU9150_MAG_RATE, 
					new SensorConfigOptionDetails(Configuration.Shimmer3.ListofMPU9150MagRate, 
											Configuration.Shimmer3.ListofMPU9150MagRateConfigValues, 
											SensorConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX,
											CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors));

		    aMap.put(Configuration.Shimmer3.GuiLabelConfig.INT_EXP_BRD_POWER_INTEGER, 
		    	      new SensorConfigOptionDetails(Configuration.Shimmer3.ListOfOnOff, 
				    	        Configuration.Shimmer3.ListOfOnOffConfigValues,
				    	        SensorConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX));
			
			//MPL CheckBoxes
			aMap.put(Configuration.Shimmer3.GuiLabelConfig.MPU9150_DMP, 
					new SensorConfigOptionDetails(SensorConfigOptionDetails.GUI_COMPONENT_TYPE.CHECKBOX,CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors));
			aMap.put(Configuration.Shimmer3.GuiLabelConfig.MPU9150_MPL, 
					new SensorConfigOptionDetails(SensorConfigOptionDetails.GUI_COMPONENT_TYPE.CHECKBOX,CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors));
			aMap.put(Configuration.Shimmer3.GuiLabelConfig.MPU9150_MPL_9DOF_SENSOR_FUSION, 
					new SensorConfigOptionDetails(SensorConfigOptionDetails.GUI_COMPONENT_TYPE.CHECKBOX,CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors));
			aMap.put(Configuration.Shimmer3.GuiLabelConfig.MPU9150_MPL_GYRO_CAL, 
					new SensorConfigOptionDetails(SensorConfigOptionDetails.GUI_COMPONENT_TYPE.CHECKBOX,CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors));
			aMap.put(Configuration.Shimmer3.GuiLabelConfig.MPU9150_MPL_VECTOR_CAL, 
					new SensorConfigOptionDetails(SensorConfigOptionDetails.GUI_COMPONENT_TYPE.CHECKBOX,CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors));
			aMap.put(Configuration.Shimmer3.GuiLabelConfig.MPU9150_MPL_MAG_CAL, 
					new SensorConfigOptionDetails(SensorConfigOptionDetails.GUI_COMPONENT_TYPE.CHECKBOX,CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors));
			
			//General Config
			aMap.put(Configuration.Shimmer3.GuiLabelConfig.MPU9150_GYRO_RATE, 
					new SensorConfigOptionDetails(SensorConfigOptionDetails.GUI_COMPONENT_TYPE.TEXTFIELD,CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW));
			
			aMap.put(Configuration.Shimmer3.GuiLabelConfig.USER_BUTTON_START, 
					new SensorConfigOptionDetails(SensorConfigOptionDetails.GUI_COMPONENT_TYPE.CHECKBOX,CompatibilityInfoForMaps.listOfCompatibleVersionInfoLogging));
			aMap.put(Configuration.Shimmer3.GuiLabelConfig.SINGLE_TOUCH_START, 
					new SensorConfigOptionDetails(SensorConfigOptionDetails.GUI_COMPONENT_TYPE.CHECKBOX,CompatibilityInfoForMaps.listOfCompatibleVersionInfoSdLog));
			aMap.put(Configuration.Shimmer3.GuiLabelConfig.EXPERIMENT_MASTER_SHIMMER, 
					new SensorConfigOptionDetails(SensorConfigOptionDetails.GUI_COMPONENT_TYPE.CHECKBOX,CompatibilityInfoForMaps.listOfCompatibleVersionInfoSdLog));
			aMap.put(Configuration.Shimmer3.GuiLabelConfig.EXPERIMENT_SYNC_WHEN_LOGGING, 
					new SensorConfigOptionDetails(SensorConfigOptionDetails.GUI_COMPONENT_TYPE.CHECKBOX,CompatibilityInfoForMaps.listOfCompatibleVersionInfoSdLog));

			aMap.put(Configuration.Shimmer3.GuiLabelConfig.KINEMATIC_LPM, 
					new SensorConfigOptionDetails(SensorConfigOptionDetails.GUI_COMPONENT_TYPE.CHECKBOX,CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW));
			aMap.put(Configuration.Shimmer3.GuiLabelConfig.LSM303DLHC_ACCEL_LPM, 
					new SensorConfigOptionDetails(SensorConfigOptionDetails.GUI_COMPONENT_TYPE.CHECKBOX));
			aMap.put(Configuration.Shimmer3.GuiLabelConfig.MPU9150_GYRO_LPM, 
					new SensorConfigOptionDetails(SensorConfigOptionDetails.GUI_COMPONENT_TYPE.CHECKBOX,CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW));
			aMap.put(Configuration.Shimmer3.GuiLabelConfig.LSM303DLHC_MAG_LPM, 
					new SensorConfigOptionDetails(SensorConfigOptionDetails.GUI_COMPONENT_TYPE.CHECKBOX,CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW));
			aMap.put(Configuration.Shimmer3.GuiLabelConfig.TCX0, 
					new SensorConfigOptionDetails(SensorConfigOptionDetails.GUI_COMPONENT_TYPE.CHECKBOX,CompatibilityInfoForMaps.listOfCompatibleVersionInfoSdLog));
			aMap.put(Configuration.Shimmer3.GuiLabelConfig.INT_EXP_BRD_POWER_BOOLEAN, 
					new SensorConfigOptionDetails(SensorConfigOptionDetails.GUI_COMPONENT_TYPE.CHECKBOX));
			
			aMap.put(Configuration.Shimmer3.GuiLabelConfig.PPG_ADC_SELECTION, 
					new SensorConfigOptionDetails(Configuration.Shimmer3.ListOfPpgAdcSelection, 
											Configuration.Shimmer3.ListOfPpgAdcSelectionConfigValues, 
											SensorConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX,
											CompatibilityInfoForMaps.listOfCompatibleVersionInfoGsr));
			aMap.put(Configuration.Shimmer3.GuiLabelConfig.PPG1_ADC_SELECTION, 
					new SensorConfigOptionDetails(Configuration.Shimmer3.ListOfPpg1AdcSelection, 
											Configuration.Shimmer3.ListOfPpg1AdcSelectionConfigValues, 
											SensorConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX,
											CompatibilityInfoForMaps.listOfCompatibleVersionInfoProto3Deluxe));
			aMap.put(Configuration.Shimmer3.GuiLabelConfig.PPG2_ADC_SELECTION, 
					new SensorConfigOptionDetails(Configuration.Shimmer3.ListOfPpg2AdcSelection, 
											Configuration.Shimmer3.ListOfPpg2AdcSelectionConfigValues, 
											SensorConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX,
											CompatibilityInfoForMaps.listOfCompatibleVersionInfoProto3Deluxe));
	        
	        mConfigOptionsMap = Collections.unmodifiableMap(aMap);
	    }





		
	}

	public static class Shimmer2{
		public class Channel{
			public static final int XAccel      = 0x00;
			public static final int YAccel      = 0x01;
			public static final int ZAccel      = 0x02;
			public static final int XGyro       = 0x03;
			public static final int YGyro       = 0x04;
			public static final int ZGyro       = 0x05;
			public static final int XMag        = 0x06;
			public static final int YMag        = 0x07;
			public static final int ZMag        = 0x08;
			public static final int EcgRaLl     = 0x09;
			public static final int EcgLaLl     = 0x0A;
			public static final int GsrRaw      = 0x0B;
			public static final int GsrRes      = 0x0C;
			public static final int Emg         = 0x0D;
			public static final int AnExA0      = 0x0E;
			public static final int AnExA7      = 0x0F;
			public static final int BridgeAmpHigh  = 0x10;
			public static final int BridgeAmpLow   = 0x11;
			public static final int HeartRate   = 0x12;
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
		public static class ObjectClusterSensorName{
			public static String TIMESTAMP = "Timestamp";
			public static String REAL_TIME_CLOCK = "RealTime";
			public static String ACCEL_X = "Accel_X";
			public static String ACCEL_Y = "Accel_Y";
			public static String ACCEL_Z = "Accel_Z";
			public static String BATTERY = "Battery";
			public static String REG = "Reg";
			public static String EXT_EXP_A7 = "Ext_Exp_A7";
			public static String EXT_EXP_A6 = "Ext_Exp_A6";
			public static String EXT_EXP_A15 = "Ext_Exp_A15";
			public static String INT_EXP_A12 = "Int_Exp_A12";
			public static String INT_EXP_A13 = "Int_Exp_A13";
			public static String INT_EXP_A14 = "Int_Exp_A14";
			public static String BRIDGE_AMP_HIGH = "Bridge_Amp_High";
			public static String BRIDGE_AMP_LOW = "Bridge_Amp_Low";
			public static String GSR = "GSR";
			//public static String GSR_RAW = "GSR Raw";
			public static String GSR_RES = "GSR Res";
			public static String INT_EXP_A1 = "Int_Exp_A1";
			public static String EXP_BOARD_A0 = "Exp_Board_A0";
			public static String EXP_BOARD_A7 = "Exp_Board_A7";
			public static String GYRO_X = "Gyro_X";
			public static String GYRO_Y = "Gyro_Y";
			public static String GYRO_Z = "Gyro_Z";
			public static String MAG_X = "Mag_X";
			public static String MAG_Y = "Mag_Y";
			public static String MAG_Z = "Mag_Z";
			public static String EMG = "EMG";
			public static String ECG_RA_LL = "ECG_RA-LL";
			public static String ECG_LA_LL = "ECG_LA-LL";
			public static String ECG_TO_HR = "ECGtoHR";
			public static String QUAT_MADGE_6DOF_W = "Quat_Madge_6DOF_W";
			public static String QUAT_MADGE_6DOF_X = "Quat_Madge_6DOF_X";
			public static String QUAT_MADGE_6DOF_Y = "Quat_Madge_6DOF_Y";
			public static String QUAT_MADGE_6DOF_Z = "Quat_Madge_6DOF_Z";
			public static String QUAT_MADGE_9DOF_W = "Quat_Madge_9DOF_W";
			public static String QUAT_MADGE_9DOF_X = "Quat_Madge_9DOF_X";
			public static String QUAT_MADGE_9DOF_Y = "Quat_Madge_9DOF_Y";
			public static String QUAT_MADGE_9DOF_Z = "Quat_Madge_9DOF_Z";
			public static String EULER_6DOF_A = "Euler_6DOF_A";
			public static String EULER_6DOF_X = "Euler_6DOF_X";
			public static String EULER_6DOF_Y = "Euler_6DOF_Y";
			public static String EULER_6DOF_Z = "Euler_6DOF_Z";
			public static String EULER_9DOF_A = "Euler_9DOF_A";
			public static String EULER_9DOF_X = "Euler_9DOF_X";
			public static String EULER_9DOF_Y = "Euler_9DOF_Y";
			public static String EULER_9DOF_Z = "Euler_9DOF_Z";
			public static String HEART_RATE = "Heart_Rate"; //for the heart rate strap now no longer sold
			public static String AXIS_ANGLE_A = "Axis_Angle_A";
			public static String AXIS_ANGLE_X = "Axis_Angle_X";
			public static String AXIS_ANGLE_Y = "Axis_Angle_Y";
			public static String AXIS_ANGLE_Z = "Axis_Angle_Z";
			public static String VOLT_REG = "VSenseReg";
		}
		
		public static final String[] ListofCompatibleSensors={"Accelerometer","Gyroscope","Magnetometer","Battery Voltage","ECG","EMG","GSR","Exp Board","Bridge Amplifier","Heart Rate"};
		public static final String[] ListofAccelRange={"+/- 1.5g","+/- 6g"};
		public static final String[] ListofMagRange={"+/- 0.8Ga","+/- 1.3Ga","+/- 1.9Ga","+/- 2.5Ga","+/- 4.0Ga","+/- 4.7Ga","+/- 5.6Ga","+/- 8.1Ga"};
		public static final String[] ListofGSRRange={"10kOhm to 56kOhm","56kOhm to 220kOhm","220kOhm to 680kOhm","680kOhm to 4.7MOhm","Auto Range"};

		public class SensorMapKey{
			public static final int ACCEL = 0;
			public static final int GYRO = 1;
			public static final int MAG = 2;
			public static final int EMG = 3;
			public static final int ECG = 4;
			public static final int GSR = 5;
			public static final int EXP_BOARD_A7 = 6;
			public static final int EXP_BOARD_A0 = 7;
			public static final int EXP_BOARD = 8;
			public static final int BRIDGE_AMP = 9;
			public static final int HEART = 10;
			public static final int BATT = 11;
			public static final int EXT_ADC_A15 = 12;
			public static final int INT_ADC_A1 = 13;
			public static final int INT_ADC_A12 = 14;
			public static final int INT_ADC_A13 = 15;
			public static final int INT_ADC_A14 = 16;
		}
		
	    public static final Map<Integer, SensorDetails> mSensorMapRef;
	    static {
	        Map<Integer, SensorDetails> aMap = new LinkedHashMap<Integer,SensorDetails>();
		
	        aMap.put(Configuration.Shimmer2.SensorMapKey.ACCEL, new SensorDetails(0x80, 0, "Accelerometer"));
	        aMap.put(Configuration.Shimmer2.SensorMapKey.GYRO, new SensorDetails(0x40, 0, "Gyroscope"));
			aMap.put(Configuration.Shimmer2.SensorMapKey.MAG, new SensorDetails(0x20, 0, "Magnetometer"));
			aMap.put(Configuration.Shimmer2.SensorMapKey.EMG, new SensorDetails(0x08, 0, "EMG"));
			aMap.put(Configuration.Shimmer2.SensorMapKey.ECG, new SensorDetails(0x10, 0, "ECG"));
			aMap.put(Configuration.Shimmer2.SensorMapKey.GSR, new SensorDetails(0x04, 0, "GSR"));
			aMap.put(Configuration.Shimmer2.SensorMapKey.EXP_BOARD_A7, new SensorDetails(0x02, 0, "Exp Board A7"));
			aMap.put(Configuration.Shimmer2.SensorMapKey.EXP_BOARD_A0, new SensorDetails(0x01, 0, "Exp Board A0"));
			aMap.put(Configuration.Shimmer2.SensorMapKey.EXP_BOARD, new SensorDetails(0x02|0x01, 0, "Exp Board"));
			aMap.put(Configuration.Shimmer2.SensorMapKey.BRIDGE_AMP, new SensorDetails(0x8000, 0, "Bridge Amplifier"));
			aMap.put(Configuration.Shimmer2.SensorMapKey.HEART, new SensorDetails(0x4000, 0, "Heart Rate"));
			aMap.put(Configuration.Shimmer2.SensorMapKey.BATT, new SensorDetails(0x2000, 0, "Battery Voltage"));
			aMap.put(Configuration.Shimmer2.SensorMapKey.EXT_ADC_A15, new SensorDetails(0x0800, 0, "External ADC A15"));
			aMap.put(Configuration.Shimmer2.SensorMapKey.INT_ADC_A1, new SensorDetails(0x0400, 0, "Internal ADC A1"));
			aMap.put(Configuration.Shimmer2.SensorMapKey.INT_ADC_A12, new SensorDetails(0x0200, 0, "Internal ADC A12"));
			aMap.put(Configuration.Shimmer2.SensorMapKey.INT_ADC_A13, new SensorDetails(0x0100, 0, "Internal ADC A13"));
			aMap.put(Configuration.Shimmer2.SensorMapKey.INT_ADC_A14, new SensorDetails(0x800000, 0, "Internal ADC A14"));
			
			// Conflicting Channels
			aMap.get(Configuration.Shimmer2.SensorMapKey.GYRO).mListOfSensorMapKeysConflicting = Arrays.asList(
				Configuration.Shimmer2.SensorMapKey.ECG,
				Configuration.Shimmer2.SensorMapKey.EMG,
				Configuration.Shimmer2.SensorMapKey.GSR,
				Configuration.Shimmer2.SensorMapKey.BRIDGE_AMP);
			aMap.get(Configuration.Shimmer2.SensorMapKey.MAG).mListOfSensorMapKeysConflicting = Arrays.asList(
				Configuration.Shimmer2.SensorMapKey.ECG,
				Configuration.Shimmer2.SensorMapKey.EMG,
				Configuration.Shimmer2.SensorMapKey.GSR,
				Configuration.Shimmer2.SensorMapKey.BRIDGE_AMP);
			aMap.get(Configuration.Shimmer2.SensorMapKey.EMG).mListOfSensorMapKeysConflicting = Arrays.asList(
				Configuration.Shimmer2.SensorMapKey.GSR,
				Configuration.Shimmer2.SensorMapKey.ECG,
				Configuration.Shimmer2.SensorMapKey.BRIDGE_AMP,
				Configuration.Shimmer2.SensorMapKey.GYRO,
				Configuration.Shimmer2.SensorMapKey.MAG);
			aMap.get(Configuration.Shimmer2.SensorMapKey.ECG).mListOfSensorMapKeysConflicting = Arrays.asList(
				Configuration.Shimmer2.SensorMapKey.GSR,
				Configuration.Shimmer2.SensorMapKey.EMG,
				Configuration.Shimmer2.SensorMapKey.BRIDGE_AMP,
				Configuration.Shimmer2.SensorMapKey.GYRO,
				Configuration.Shimmer2.SensorMapKey.MAG);
			aMap.get(Configuration.Shimmer2.SensorMapKey.GSR).mListOfSensorMapKeysConflicting = Arrays.asList(
				Configuration.Shimmer2.SensorMapKey.ECG,
				Configuration.Shimmer2.SensorMapKey.EMG,
				Configuration.Shimmer2.SensorMapKey.BRIDGE_AMP,
				Configuration.Shimmer2.SensorMapKey.GYRO,
				Configuration.Shimmer2.SensorMapKey.MAG);
			aMap.get(Configuration.Shimmer2.SensorMapKey.EXP_BOARD_A7).mListOfSensorMapKeysConflicting = Arrays.asList(
				Configuration.Shimmer2.SensorMapKey.HEART,
				Configuration.Shimmer2.SensorMapKey.BATT);
			aMap.get(Configuration.Shimmer2.SensorMapKey.EXP_BOARD_A0).mListOfSensorMapKeysConflicting = Arrays.asList(
				Configuration.Shimmer2.SensorMapKey.HEART,
				Configuration.Shimmer2.SensorMapKey.BATT);
			aMap.get(Configuration.Shimmer2.SensorMapKey.BRIDGE_AMP).mListOfSensorMapKeysConflicting = Arrays.asList(
				Configuration.Shimmer2.SensorMapKey.ECG,
				Configuration.Shimmer2.SensorMapKey.EMG,
				Configuration.Shimmer2.SensorMapKey.GSR,
				Configuration.Shimmer2.SensorMapKey.GYRO,
				Configuration.Shimmer2.SensorMapKey.MAG);
			aMap.get(Configuration.Shimmer2.SensorMapKey.HEART).mListOfSensorMapKeysConflicting = Arrays.asList(
				Configuration.Shimmer2.SensorMapKey.EXP_BOARD_A0,
				Configuration.Shimmer2.SensorMapKey.EXP_BOARD_A7);
			aMap.get(Configuration.Shimmer2.SensorMapKey.BATT).mListOfSensorMapKeysConflicting = Arrays.asList(
				Configuration.Shimmer2.SensorMapKey.EXP_BOARD_A0,
				Configuration.Shimmer2.SensorMapKey.EXP_BOARD_A7);
			mSensorMapRef = Collections.unmodifiableMap(aMap);
	    }
				
		

	}

	
	public static void setTooLegacyObjectClusterSensorNames(){

		Shimmer3.ObjectClusterSensorName.TIMESTAMP = "Timestamp";
		Shimmer3.ObjectClusterSensorName.REAL_TIME_CLOCK = "RealTime";
		Shimmer3.ObjectClusterSensorName.ACCEL_LN_X = "Low Noise Accelerometer X";
		Shimmer3.ObjectClusterSensorName.ACCEL_LN_Y = "Low Noise Accelerometer Y";
		Shimmer3.ObjectClusterSensorName.ACCEL_LN_Z = "Low Noise Accelerometer Z";
		Shimmer3.ObjectClusterSensorName.BATTERY = "VSenseBatt";
		Shimmer3.ObjectClusterSensorName.EXT_EXP_A7 = "ExpBoard A7";
		Shimmer3.ObjectClusterSensorName.EXT_EXP_A6 = "ExpBoard A6";
		Shimmer3.ObjectClusterSensorName.EXT_EXP_A15 = "External ADC A15";
		Shimmer3.ObjectClusterSensorName.INT_EXP_A12 = "Internal ADC A12";
		Shimmer3.ObjectClusterSensorName.INT_EXP_A13 = "Internal ADC A13";
		Shimmer3.ObjectClusterSensorName.INT_EXP_A14 = "Internal ADC A14";
		Shimmer3.ObjectClusterSensorName.BRIDGE_AMP_HIGH = "Bridge Amplifier High";
		Shimmer3.ObjectClusterSensorName.BRIDGE_AMP_LOW = "Bridge Amplifier Low";
		Shimmer3.ObjectClusterSensorName.GSR = "GSR";
		Shimmer3.ObjectClusterSensorName.INT_EXP_A1 = "Internal ADC A1";
		Shimmer3.ObjectClusterSensorName.RESISTANCE_AMP = "Resistance Amp";
		Shimmer3.ObjectClusterSensorName.GYRO_X = "Gyroscope X";
		Shimmer3.ObjectClusterSensorName.GYRO_Y = "Gyroscope Y";
		Shimmer3.ObjectClusterSensorName.GYRO_Z = "Gyroscope Z";
		Shimmer3.ObjectClusterSensorName.ACCEL_WR_X = "Wide Range Accelerometer X";
		Shimmer3.ObjectClusterSensorName.ACCEL_WR_Y = "Wide Range Accelerometer Y";
		Shimmer3.ObjectClusterSensorName.ACCEL_WR_Z= "Wide Range Accelerometer Z";
		Shimmer3.ObjectClusterSensorName.MAG_X = "Magnetometer X";
		Shimmer3.ObjectClusterSensorName.MAG_Y = "Magnetometer Y";
		Shimmer3.ObjectClusterSensorName.MAG_Z = "Magnetometer Z";
		Shimmer3.ObjectClusterSensorName.ACCEL_MPU_X = "Accel_MPU_X";
		Shimmer3.ObjectClusterSensorName.ACCEL_MPU_Y = "Accel_MPU_Y";
		Shimmer3.ObjectClusterSensorName.ACCEL_MPU_Z = "Accel_MPU_Z";
		Shimmer3.ObjectClusterSensorName.MAG_MPU_X = "Mag_MPU_X";
		Shimmer3.ObjectClusterSensorName.MAG_MPU_Y = "Mag_MPU_Y";
		Shimmer3.ObjectClusterSensorName.MAG_MPU_Z = "Mag_MPU_Z";
		Shimmer3.ObjectClusterSensorName.TEMPERATURE_BMP180 = "Temperature";
		Shimmer3.ObjectClusterSensorName.PRESSURE_BMP180 = "Pressure";
		Shimmer3.ObjectClusterSensorName.EMG_CH1_24BIT = "EMG CH1";
		Shimmer3.ObjectClusterSensorName.EMG_CH2_24BIT = "EMG CH2";
		Shimmer3.ObjectClusterSensorName.EMG_CH1_16BIT = "EMG CH1";
		Shimmer3.ObjectClusterSensorName.EMG_CH2_16BIT = "EMG CH2";
		Shimmer3.ObjectClusterSensorName.ECG_LL_RA_24BIT = "ECG LL-RA";
		Shimmer3.ObjectClusterSensorName.ECG_LA_RA_24BIT = "ECG LA-RA";
		Shimmer3.ObjectClusterSensorName.ECG_LL_RA_16BIT = "ECG LL-RA";
		Shimmer3.ObjectClusterSensorName.ECG_LA_RA_16BIT = "ECG LA-RA";
		Shimmer3.ObjectClusterSensorName.TEST_CHIP1_CH1_24BIT = "EXG1 CH1";
		Shimmer3.ObjectClusterSensorName.TEST_CHIP2_CH1_24BIT = "EXG1 CH1";
		Shimmer3.ObjectClusterSensorName.TEST_CHIP1_CH2_24BIT = "EXG1 CH2";
		Shimmer3.ObjectClusterSensorName.TEST_CHIP2_CH2_24BIT = "EXG1 CH2";
		Shimmer3.ObjectClusterSensorName.TEST_CHIP1_CH1_16BIT = "EXG1 CH1 16BIT";
		Shimmer3.ObjectClusterSensorName.TEST_CHIP2_CH1_16BIT = "EXG1 CH1 16BIT";
		Shimmer3.ObjectClusterSensorName.TEST_CHIP1_CH2_16BIT = "EXG1 CH2 16BIT";
		Shimmer3.ObjectClusterSensorName.TEST_CHIP2_CH2_16BIT = "EXG1 CH2 16BIT";
		Shimmer3.ObjectClusterSensorName.EXG1_STATUS = "EXG1 Status";
		Shimmer3.ObjectClusterSensorName.ECG_RESP_24BIT = "ECG RESP";
		Shimmer3.ObjectClusterSensorName.ECG_VX_RL_24BIT = "ECG Vx-RL";
		Shimmer3.ObjectClusterSensorName.ECG_RESP_16BIT = "ECG RESP";
		Shimmer3.ObjectClusterSensorName.ECG_VX_RL_16BIT = "ECG Vx-RL";
		Shimmer3.ObjectClusterSensorName.EXG1_CH1_24BIT = "ExG1 CH1";
		Shimmer3.ObjectClusterSensorName.EXG1_CH2_24BIT = "ExG1 CH2";
		Shimmer3.ObjectClusterSensorName.EXG1_CH1_16BIT = "ExG1 CH1 16Bit";
		Shimmer3.ObjectClusterSensorName.EXG1_CH2_16BIT = "ExG1 CH2 16Bit";
		Shimmer3.ObjectClusterSensorName.EXG2_CH1_24BIT = "ExG2 CH1";
		Shimmer3.ObjectClusterSensorName.EXG2_CH2_24BIT = "ExG2 CH2";
		Shimmer3.ObjectClusterSensorName.EXG2_CH1_16BIT = "ExG2 CH1 16Bit";
		Shimmer3.ObjectClusterSensorName.EXG2_CH2_16BIT = "ExG2 CH2 16Bit";
		Shimmer3.ObjectClusterSensorName.EXG2_STATUS = "EXG2 Status";
		Shimmer3.ObjectClusterSensorName.QUAT_MPL_6DOF_W = "Quat_MPL_6DOF_W";
		Shimmer3.ObjectClusterSensorName.QUAT_MPL_6DOF_X = "Quat_MPL_6DOF_X";
		Shimmer3.ObjectClusterSensorName.QUAT_MPL_6DOF_Y = "Quat_MPL_6DOF_Y";
		Shimmer3.ObjectClusterSensorName.QUAT_MPL_6DOF_Z = "Quat_MPL_6DOF_Z";
		Shimmer3.ObjectClusterSensorName.QUAT_MPL_9DOF_W = "Quat_MPL_9DOF_W";
		Shimmer3.ObjectClusterSensorName.QUAT_MPL_9DOF_X = "Quat_MPL_9DOF_X";
		Shimmer3.ObjectClusterSensorName.QUAT_MPL_9DOF_Y = "Quat_MPL_9DOF_Y";
		Shimmer3.ObjectClusterSensorName.QUAT_MPL_9DOF_Z = "Quat_MPL_9DOF_Z";
		Shimmer3.ObjectClusterSensorName.EULER_MPL_6DOF_X = "Euler_MPL_6DOF_X";
		Shimmer3.ObjectClusterSensorName.EULER_MPL_6DOF_Y = "Euler_MPL_6DOF_Y";
		Shimmer3.ObjectClusterSensorName.EULER_MPL_6DOF_Z = "Euler_MPL_6DOF_Z";
		Shimmer3.ObjectClusterSensorName.EULER_MPL_9DOF_X = "Euler_MPL_9DOF_X";
		Shimmer3.ObjectClusterSensorName.EULER_MPL_9DOF_Y = "Euler_MPL_9DOF_Y";
		Shimmer3.ObjectClusterSensorName.EULER_MPL_9DOF_Z = "Euler_MPL_9DOF_Z";
		Shimmer3.ObjectClusterSensorName.MPL_HEADING = "MPL_heading";
		Shimmer3.ObjectClusterSensorName.MPL_TEMPERATURE = "MPL_Temperature";
		Shimmer3.ObjectClusterSensorName.MPL_PEDOM_CNT = "MPL_Pedom_cnt";
		Shimmer3.ObjectClusterSensorName.MPL_PEDOM_TIME = "MPL_Pedom_Time";
		Shimmer3.ObjectClusterSensorName.TAPDIRANDTAPCNT = "TapDirAndTapCnt";
		Shimmer3.ObjectClusterSensorName.MOTIONANDORIENT = "MotionAndOrient";
		Shimmer3.ObjectClusterSensorName.GYRO_MPU_MPL_X = "Gyro_MPU_MPL_X";
		Shimmer3.ObjectClusterSensorName.GYRO_MPU_MPL_Y = "Gyro_MPU_MPL_Y";
		Shimmer3.ObjectClusterSensorName.GYRO_MPU_MPL_Z = "Gyro_MPU_MPL_Z";
		Shimmer3.ObjectClusterSensorName.ACCEL_MPU_MPL_X = "Accel_MPU_MPL_X";
		Shimmer3.ObjectClusterSensorName.ACCEL_MPU_MPL_Y = "Accel_MPU_MPL_Y";
		Shimmer3.ObjectClusterSensorName.ACCEL_MPU_MPL_Z = "Accel_MPU_MPL_Z";
		Shimmer3.ObjectClusterSensorName.MAG_MPU_MPL_X = "Mag_MPU_MPL_X";
		Shimmer3.ObjectClusterSensorName.MAG_MPU_MPL_Y = "Mag_MPU_MPL_Y";
		Shimmer3.ObjectClusterSensorName.MAG_MPU_MPL_Z = "Mag_MPU_MPL_Z";
		Shimmer3.ObjectClusterSensorName.QUAT_DMP_6DOF_W = "Quat_DMP_6DOF_W";
		Shimmer3.ObjectClusterSensorName.QUAT_DMP_6DOF_X = "Quat_DMP_6DOF_X";
		Shimmer3.ObjectClusterSensorName.QUAT_DMP_6DOF_Y = "Quat_DMP_6DOF_Y";
		Shimmer3.ObjectClusterSensorName.QUAT_DMP_6DOF_Z = "Quat_DMP_6DOF_Z";
		Shimmer3.ObjectClusterSensorName.ECG_TO_HR = "ECGtoHR";
		Shimmer3.ObjectClusterSensorName.PPG_TO_HR = "PPGtoHR";
		Shimmer3.ObjectClusterSensorName.QUAT_MADGE_6DOF_W = "Quaternion 0";
		Shimmer3.ObjectClusterSensorName.QUAT_MADGE_6DOF_X = "Quaternion 1";
		Shimmer3.ObjectClusterSensorName.QUAT_MADGE_6DOF_Y = "Quaternion 2";
		Shimmer3.ObjectClusterSensorName.QUAT_MADGE_6DOF_Z = "Quaternion 3";
		Shimmer3.ObjectClusterSensorName.QUAT_MADGE_9DOF_W = "Quaternion 0";
		Shimmer3.ObjectClusterSensorName.QUAT_MADGE_9DOF_X = "Quaternion 1";
		Shimmer3.ObjectClusterSensorName.QUAT_MADGE_9DOF_Y = "Quaternion 2";
		Shimmer3.ObjectClusterSensorName.QUAT_MADGE_9DOF_Z = "Quaternion 3";
		Shimmer3.ObjectClusterSensorName.EULER_6DOF_A = "Euler_6DOF_A";
		Shimmer3.ObjectClusterSensorName.EULER_6DOF_X = "Euler_6DOF_X";
		Shimmer3.ObjectClusterSensorName.EULER_6DOF_Y = "Euler_6DOF_Y";
		Shimmer3.ObjectClusterSensorName.EULER_6DOF_Z = "Euler_6DOF_Z";
		Shimmer3.ObjectClusterSensorName.EULER_9DOF_A = "Euler_9DOF_A";
		Shimmer3.ObjectClusterSensorName.EULER_9DOF_X = "Euler_9DOF_X";
		Shimmer3.ObjectClusterSensorName.EULER_9DOF_Y = "Euler_9DOF_Y";
		Shimmer3.ObjectClusterSensorName.EULER_9DOF_Z = "Euler_9DOF_Z";
		Shimmer3.ObjectClusterSensorName.AXIS_ANGLE_A = "Axis Angle A";
		Shimmer3.ObjectClusterSensorName.AXIS_ANGLE_X = "Axis Angle X";
		Shimmer3.ObjectClusterSensorName.AXIS_ANGLE_Y = "Axis Angle Y";
		Shimmer3.ObjectClusterSensorName.AXIS_ANGLE_Z = "Axis Angle Z";
		Shimmer3.ObjectClusterSensorName.PPG_A12 = "PPG_A12";
		Shimmer3.ObjectClusterSensorName.PPG_A13 = "PPG_A13";
		Shimmer3.ObjectClusterSensorName.PPG1_A12 = "PPG1_A12";
		Shimmer3.ObjectClusterSensorName.PPG1_A13 = "PPG1_A13";
		Shimmer3.ObjectClusterSensorName.PPG2_A1 = "PPG2_A1";
		Shimmer3.ObjectClusterSensorName.PPG2_A14 = "PPG2_A14";
		Shimmer3.ObjectClusterSensorName.REAL_TIME_CLOCK_SYNC = "RealTime_Sync";
		Shimmer3.ObjectClusterSensorName.TIMESTAMP_SYNC = "Timestamp_Sync";

		Shimmer2.ObjectClusterSensorName.TIMESTAMP = "Timestamp";
		Shimmer2.ObjectClusterSensorName.REAL_TIME_CLOCK = "RealTime";
		Shimmer2.ObjectClusterSensorName.ACCEL_X = "Accelerometer X";
		Shimmer2.ObjectClusterSensorName.ACCEL_Y = "Accelerometer Y";
		Shimmer2.ObjectClusterSensorName.ACCEL_Z = "Accelerometer Z";
		Shimmer2.ObjectClusterSensorName.BATTERY = "VSenseBatt";
		Shimmer2.ObjectClusterSensorName.VOLT_REG = "VSenseReg";
		Shimmer2.ObjectClusterSensorName.BRIDGE_AMP_HIGH = "Bridge Amplifier High";
		Shimmer2.ObjectClusterSensorName.BRIDGE_AMP_LOW = "Bridge Amplifier Low";
		Shimmer2.ObjectClusterSensorName.GSR = "GSR";
		//Shimmer2.ObjectClusterSensorName.GSR_RAW = "GSR Raw";
		Shimmer2.ObjectClusterSensorName.GSR_RES = "GSR Res";
		Shimmer2.ObjectClusterSensorName.EXP_BOARD_A0 = "ExpBoard A0";
		Shimmer2.ObjectClusterSensorName.EXP_BOARD_A7 = "ExpBoard A7";
		Shimmer2.ObjectClusterSensorName.GYRO_X = "Gyroscope X";
		Shimmer2.ObjectClusterSensorName.GYRO_Y = "Gyroscope Y";
		Shimmer2.ObjectClusterSensorName.GYRO_Z = "Gyroscope Z";
		Shimmer2.ObjectClusterSensorName.MAG_X = "Magnetometer X";
		Shimmer2.ObjectClusterSensorName.MAG_Y = "Magnetometer Y";
		Shimmer2.ObjectClusterSensorName.MAG_Z = "Magnetometer Z";
		Shimmer2.ObjectClusterSensorName.EMG = "EMG";
		Shimmer2.ObjectClusterSensorName.ECG_RA_LL = "ECG RA-LL";
		Shimmer2.ObjectClusterSensorName.ECG_LA_LL = "ECG LA-LL";
		Shimmer2.ObjectClusterSensorName.ECG_TO_HR = "ECGtoHR";
		Shimmer2.ObjectClusterSensorName.QUAT_MADGE_6DOF_W = "Quaternion 0";
		Shimmer2.ObjectClusterSensorName.QUAT_MADGE_6DOF_X = "Quaternion 1";
		Shimmer2.ObjectClusterSensorName.QUAT_MADGE_6DOF_Y = "Quaternion 2";
		Shimmer2.ObjectClusterSensorName.QUAT_MADGE_6DOF_Z = "Quaternion 3";
		Shimmer2.ObjectClusterSensorName.QUAT_MADGE_9DOF_W = "Quaternion 0";
		Shimmer2.ObjectClusterSensorName.QUAT_MADGE_9DOF_X = "Quaternion 1";
		Shimmer2.ObjectClusterSensorName.QUAT_MADGE_9DOF_Y = "Quaternion 2";
		Shimmer2.ObjectClusterSensorName.QUAT_MADGE_9DOF_Z = "Quaternion 3";
		Shimmer2.ObjectClusterSensorName.EULER_6DOF_A = "Euler_6DOF_A";
		Shimmer2.ObjectClusterSensorName.EULER_6DOF_X = "Euler_6DOF_X";
		Shimmer2.ObjectClusterSensorName.EULER_6DOF_Y = "Euler_6DOF_Y";
		Shimmer2.ObjectClusterSensorName.EULER_6DOF_Z = "Euler_6DOF_Z";
		Shimmer2.ObjectClusterSensorName.EULER_9DOF_A = "Euler_9DOF_A";
		Shimmer2.ObjectClusterSensorName.EULER_9DOF_X = "Euler_9DOF_X";
		Shimmer2.ObjectClusterSensorName.EULER_9DOF_Y = "Euler_9DOF_Y";
		Shimmer2.ObjectClusterSensorName.EULER_9DOF_Z = "Euler_9DOF_Z";
		Shimmer2.ObjectClusterSensorName.HEART_RATE = "Heart_Rate"; //for the heart rate strap now no longer sold
		Shimmer2.ObjectClusterSensorName.AXIS_ANGLE_A = "Axis Angle A";
		Shimmer2.ObjectClusterSensorName.AXIS_ANGLE_X = "Axis Angle X";
		Shimmer2.ObjectClusterSensorName.AXIS_ANGLE_Y = "Axis Angle Y";
		Shimmer2.ObjectClusterSensorName.AXIS_ANGLE_Z = "Axis Angle Z";
		
	
		
		
	}
	
}


