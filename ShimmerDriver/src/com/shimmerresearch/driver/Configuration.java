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

import com.shimmerresearch.algorithms.AlgorithmDetails;
import com.shimmerresearch.algorithms.OrientationModule;
import com.shimmerresearch.algorithms.OrientationModule6DOF;
import com.shimmerresearch.driverUtilities.ChannelDetails;
import com.shimmerresearch.driverUtilities.ConfigOptionDetailsSensor;
import com.shimmerresearch.driverUtilities.SensorDetailsRef;
import com.shimmerresearch.driverUtilities.SensorGroupingDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_TYPE;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_ENDIAN;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_TYPE;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.FW_ID;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.HW_ID;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.HW_ID_SR_CODES;
import com.shimmerresearch.sensors.AbstractSensor;
import com.shimmerresearch.sensors.SensorADC;
//import com.shimmerresearch.pluginalgo.ECGAdaptiveModule.ObjectClusterSensorName;
import com.shimmerresearch.sensors.SensorBMP180;
import com.shimmerresearch.sensors.SensorBattVoltage;
import com.shimmerresearch.sensors.SensorBridgeAmp;
import com.shimmerresearch.sensors.SensorECGToHRFw;
import com.shimmerresearch.sensors.SensorEXG;
import com.shimmerresearch.sensors.SensorGSR;
import com.shimmerresearch.sensors.SensorKionixKXRB52042;
import com.shimmerresearch.sensors.SensorLSM303;
import com.shimmerresearch.sensors.SensorMPU9X50;
import com.shimmerresearch.sensors.SensorPPG;
import com.shimmerresearch.sensors.SensorSystemTimeStamp;
import com.shimmerresearch.sensors.ShimmerClock;

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
	
	public static final double ACCELERATION_DUE_TO_GRAVITY = 9.81;
	
	public static class CHANNEL_UNITS{
		//Sensors units
		public static final String NO_UNITS = "no_units";
		public static final String MILLISECONDS = "ms";
		public static final String SECONDS = "s";
		public static final String METER = "m";
		public static final String METER_PER_SECOND = "m/s";
		public static final String METER_PER_SECOND_SQUARE = "m/(s^2)";  //XXX-RS-LSM-SensorClass?
		public static final String METER_SQUARE = "m^2";
		public static final String MILLIMETER = "mm";
		public static final String MILLIMETER_PER_SECOND = "mm/s";
		public static final String MILLIMETER_PER_SECOND_SQUARE = "mm/(s^2)";
		public static final String MILLIMETER_SQUARE = "mm^2";
		public static final String MILLIMETER_SQUARE_PER_SECOND = "mm^2/s";
		public static final String DEGREES_PER_SECOND = "deg/s";
		public static final String LOCAL_FLUX = "local_flux";  //XXX-RS-LSM-SensorClass?
		public static final String KOHMS = "kOhms";
		public static final String KOHMS_PER_SECOND = "kOhms/s";
		public static final String KOHMS_SECONDS = "kOhm.s";
		public static final String MILLIVOLTS = "mV";
		public static final String BEATS_PER_MINUTE = "BPM";
		public static final String KPASCAL = "kPa";
		public static final String DEGREES_CELSUIS = "Degrees Celsius";
		public static final String DEGREES = "Degrees";
		public static final String U_TESLA = "uT";  //XXX-RS-LSM-SensorClass?
		public static final String U_SIEMENS = "uS";
		//TODO: should be .SSS rather then .000? .000 might be required for viewing files in Microsoft Office
		public static final String DATE_FORMAT = "yyyy/mm/dd hh:mm:ss.000";
		public static final String GRAVITY = "g"; //XXX-RS-LSM-SensorClass?
		public static final String CLOCK_UNIT = "Ticks";
		public static final String RPM = "rpm";
		public static final String FREQUENCY = "Hz";
		public static final String POWER = "dB";

		public static final String ACCEL_CAL_UNIT = METER_PER_SECOND_SQUARE; //XXX-RS-LSM-SensorClass?
		public static final String GYRO_CAL_UNIT = DEGREES_PER_SECOND;
		public static final String MAG_CAL_UNIT = LOCAL_FLUX;  //XXX-RS-LSM-SensorClass?

//		public static final String ACCEL_DEFAULT_CAL_UNIT = METER_PER_SECOND_SQUARE+"*";
//		public static final String GYRO_DEFAULT_CAL_UNIT = DEGREES_PER_SECOND+"*";
//		public static final String MAG_DEFAULT_CAL_UNIT = LOCAL_FLUX+"*";

		public static final String LOCAL = "local"; //used for axis-angle and madgewick quaternions    //XXX-RS-LSM-SensorClass?
		public static final String PERCENT = "%";
	}	
	
	public enum COMMUNICATION_TYPE{
//		ALL,
		DOCK,
		BLUETOOTH,
		IEEE802154,
		SD,
		HID,
		CLASS, //this is to read the value of the class for clones etc. e.g. if you do a getsettings(accelRange,CLASS) and getsettings(accelRange,Bluetooth), the results are different. One returns the value in the object while the other should generate a read command to be sent to the shimmer device
	}
	
	public enum COMMUNICATION_ACTION{
		READ,
		WRITE
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
			public static String EULER_6DOF_YAW = "Euler_6DOF_Yaw";
			public static String EULER_6DOF_PITCH = "Euler_6DOF_PITCH";
			public static String EULER_6DOF_ROLL = "Euler_6DOF_Roll";
			public static String EULER_9DOF_YAW = "Euler_9DOF_YAW";
			public static String EULER_9DOF_PITCH = "Euler_9DOF_PITCH";
			public static String EULER_9DOF_ROLL = "Euler_9DOF_Roll";
			public static String HEART_RATE = "Heart_Rate"; //for the heart rate strap now no longer sold
			public static String AXIS_ANGLE_6DOF_A = "Axis_Angle_A";
			public static String AXIS_ANGLE_6DOF_X = "Axis_Angle_X";
			public static String AXIS_ANGLE_6DOF_Y = "Axis_Angle_Y";
			public static String AXIS_ANGLE_6DOF_Z = "Axis_Angle_Z";
			public static String AXIS_ANGLE_9DOF_A = "Axis_Angle_A";
			public static String AXIS_ANGLE_9DOF_X = "Axis_Angle_X";
			public static String AXIS_ANGLE_9DOF_Y = "Axis_Angle_Y";
			public static String AXIS_ANGLE_9DOF_Z = "Axis_Angle_Z";
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
		
	    public static final Map<Integer, SensorDetailsRef> mSensorMapRef;
	    static {
	        Map<Integer, SensorDetailsRef> aMap = new LinkedHashMap<Integer,SensorDetailsRef>();
		
	        aMap.put(Configuration.Shimmer2.SensorMapKey.ACCEL, new SensorDetailsRef(0x80, 0, "Accelerometer"));
	        aMap.put(Configuration.Shimmer2.SensorMapKey.GYRO, new SensorDetailsRef(0x40, 0, "Gyroscope"));
			aMap.put(Configuration.Shimmer2.SensorMapKey.MAG, new SensorDetailsRef(0x20, 0, "Magnetometer"));
			aMap.put(Configuration.Shimmer2.SensorMapKey.EMG, new SensorDetailsRef(0x08, 0, "EMG"));
			aMap.put(Configuration.Shimmer2.SensorMapKey.ECG, new SensorDetailsRef(0x10, 0, "ECG"));
			aMap.put(Configuration.Shimmer2.SensorMapKey.GSR, new SensorDetailsRef(0x04, 0, "GSR"));
			aMap.put(Configuration.Shimmer2.SensorMapKey.EXP_BOARD_A7, new SensorDetailsRef(0x02, 0, "Exp Board A7"));
			aMap.put(Configuration.Shimmer2.SensorMapKey.EXP_BOARD_A0, new SensorDetailsRef(0x01, 0, "Exp Board A0"));
			aMap.put(Configuration.Shimmer2.SensorMapKey.EXP_BOARD, new SensorDetailsRef(0x02|0x01, 0, "Exp Board"));
			aMap.put(Configuration.Shimmer2.SensorMapKey.BRIDGE_AMP, new SensorDetailsRef(0x8000, 0, "Bridge Amplifier"));
			aMap.put(Configuration.Shimmer2.SensorMapKey.HEART, new SensorDetailsRef(0x4000, 0, "Heart Rate"));
			aMap.put(Configuration.Shimmer2.SensorMapKey.BATT, new SensorDetailsRef(0x2000, 0, "Battery Voltage"));
			aMap.put(Configuration.Shimmer2.SensorMapKey.EXT_ADC_A15, new SensorDetailsRef(0x0800, 0, "External ADC A15"));
			aMap.put(Configuration.Shimmer2.SensorMapKey.INT_ADC_A1, new SensorDetailsRef(0x0400, 0, "Internal ADC A1"));
			aMap.put(Configuration.Shimmer2.SensorMapKey.INT_ADC_A12, new SensorDetailsRef(0x0200, 0, "Internal ADC A12"));
			aMap.put(Configuration.Shimmer2.SensorMapKey.INT_ADC_A13, new SensorDetailsRef(0x0100, 0, "Internal ADC A13"));
			aMap.put(Configuration.Shimmer2.SensorMapKey.INT_ADC_A14, new SensorDetailsRef(0x800000, 0, "Internal ADC A14"));
			
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
		Shimmer3.ObjectClusterSensorName.EXT_EXP_ADC_A7 = "ExpBoard A7";
		Shimmer3.ObjectClusterSensorName.EXT_EXP_ADC_A6 = "ExpBoard A6";
		Shimmer3.ObjectClusterSensorName.EXT_EXP_ADC_A15 = "External ADC A15";
		Shimmer3.ObjectClusterSensorName.INT_EXP_ADC_A12 = "Internal ADC A12";
		Shimmer3.ObjectClusterSensorName.INT_EXP_ADC_A13 = "Internal ADC A13";
		Shimmer3.ObjectClusterSensorName.INT_EXP_ADC_A14 = "Internal ADC A14";
		Shimmer3.ObjectClusterSensorName.BRIDGE_AMP_HIGH = "Bridge Amplifier High";
		Shimmer3.ObjectClusterSensorName.BRIDGE_AMP_LOW = "Bridge Amplifier Low";
		Shimmer3.ObjectClusterSensorName.GSR = "GSR";
		Shimmer3.ObjectClusterSensorName.INT_EXP_ADC_A1 = "Internal ADC A1";
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
		Shimmer3.ObjectClusterSensorName.ECG_LL_LA_24BIT = "ECG LL-LA";
		Shimmer3.ObjectClusterSensorName.ECG_LL_LA_16BIT = "ECG LL-LA";
		Shimmer3.ObjectClusterSensorName.ECG_LL_RA_16BIT = "ECG LL-RA";
		Shimmer3.ObjectClusterSensorName.ECG_LA_RA_16BIT = "ECG LA-RA";
		Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP1_CH1_24BIT = "EXG1 CH1";
		Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP2_CH1_24BIT = "EXG1 CH1";
		Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP1_CH2_24BIT = "EXG1 CH2";
		Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP2_CH2_24BIT = "EXG1 CH2";
		Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP1_CH1_16BIT = "EXG1 CH1 16BIT";
		Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP2_CH1_16BIT = "EXG1 CH1 16BIT";
		Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP1_CH2_16BIT = "EXG1 CH2 16BIT";
		Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP2_CH2_16BIT = "EXG1 CH2 16BIT";
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
		Shimmer3.ObjectClusterSensorName.EULER_6DOF_YAW = "Euler_6DOF_Yaw";
		Shimmer3.ObjectClusterSensorName.EULER_6DOF_PITCH = "Euler_6DOF_Pitch";
		Shimmer3.ObjectClusterSensorName.EULER_6DOF_ROLL = "Euler_6DOF_Roll";
		Shimmer3.ObjectClusterSensorName.EULER_9DOF_YAW = "Euler_9DOF_Yaw";
		Shimmer3.ObjectClusterSensorName.EULER_9DOF_PITCH = "Euler_9DOF_Pitch";
		Shimmer3.ObjectClusterSensorName.EULER_9DOF_ROLL = "Euler_9DOF_Roll";
		Shimmer3.ObjectClusterSensorName.AXIS_ANGLE_A = "Axis Angle A";
		Shimmer3.ObjectClusterSensorName.AXIS_ANGLE_X = "Axis Angle X";
		Shimmer3.ObjectClusterSensorName.AXIS_ANGLE_Y = "Axis Angle Y";
		Shimmer3.ObjectClusterSensorName.AXIS_ANGLE_Z = "Axis Angle Z";
		SensorPPG.ObjectClusterSensorName.PPG_A12 = "PPG_A12";
		SensorPPG.ObjectClusterSensorName.PPG_A13 = "PPG_A13";
		SensorPPG.ObjectClusterSensorName.PPG1_A12 = "PPG1_A12";
		SensorPPG.ObjectClusterSensorName.PPG1_A13 = "PPG1_A13";
		SensorPPG.ObjectClusterSensorName.PPG2_A1 = "PPG2_A1";
		SensorPPG.ObjectClusterSensorName.PPG2_A14 = "PPG2_A14";
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
		Shimmer2.ObjectClusterSensorName.EULER_6DOF_YAW = "Euler_6DOF_YAW";
		Shimmer2.ObjectClusterSensorName.EULER_6DOF_PITCH = "Euler_6DOF_PITCH";
		Shimmer2.ObjectClusterSensorName.EULER_6DOF_ROLL = "Euler_6DOF_Roll";
		Shimmer2.ObjectClusterSensorName.EULER_9DOF_YAW = "Euler_9DOF_YAW";
		Shimmer2.ObjectClusterSensorName.EULER_9DOF_PITCH = "Euler_9DOF_PITCH";
		Shimmer2.ObjectClusterSensorName.EULER_9DOF_ROLL = "Euler_9DOF_Roll";
		Shimmer2.ObjectClusterSensorName.HEART_RATE = "Heart_Rate"; //for the heart rate strap now no longer sold
		Shimmer2.ObjectClusterSensorName.AXIS_ANGLE_6DOF_A = "Axis Angle A 6DOF";
		Shimmer2.ObjectClusterSensorName.AXIS_ANGLE_6DOF_X = "Axis Angle X 6DOF";
		Shimmer2.ObjectClusterSensorName.AXIS_ANGLE_6DOF_Y = "Axis Angle Y 6DOF";
		Shimmer2.ObjectClusterSensorName.AXIS_ANGLE_6DOF_Z = "Axis Angle Z 6DOF";
		Shimmer2.ObjectClusterSensorName.AXIS_ANGLE_9DOF_A = "Axis Angle A 9DOF";
		Shimmer2.ObjectClusterSensorName.AXIS_ANGLE_9DOF_X = "Axis Angle X 9DOF";
		Shimmer2.ObjectClusterSensorName.AXIS_ANGLE_9DOF_Y = "Axis Angle Y 9DOF";
		Shimmer2.ObjectClusterSensorName.AXIS_ANGLE_9DOF_Z = "Axis Angle Z 9DOF";

	;

	}
	

	
	public static class Shimmer3{
		public class Channel{
			public static final int XAAccel     			 = 0x00;//XXX-RS-AA-SensorClass?
			public static final int YAAccel    				 = 0x01;//XXX-RS-AA-SensorClass?
			public static final int ZAAccel     			 = 0x02;//XXX-RS-AA-SensorClass?
			public static final int VBatt       			 = 0x03;
			public static final int XDAccel     			 = 0x04;//XXX-RS-LSM-SensorClass?
			public static final int YDAccel     			 = 0x05;//XXX-RS-LSM-SensorClass?
			public static final int ZDAccel     			 = 0x06;//XXX-RS-LSM-SensorClass?
			public static final int XMag        			 = 0x07;//XXX-RS-LSM-SensorClass?
			public static final int YMag        			 = 0x08;//XXX-RS-LSM-SensorClass?
			public static final int ZMag        			 = 0x09;//XXX-RS-LSM-SensorClass?
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
			public static final int SENSOR_A_ACCEL			= 0x80;//XXX-RS-AA-SensorClass?
			public static final int SENSOR_GYRO			   	= 0x40;
			public static final int SENSOR_MAG				= 0x20; //XXX-RS-LSM-SensorClass?
			public static final int SENSOR_EXG1_24BIT		= 0x10;
			public static final int SENSOR_EXG2_24BIT		= 0x08;
			public static final int SENSOR_GSR				= 0x04;
			public static final int SENSOR_EXT_A7			= 0x02;
			public static final int SENSOR_EXT_A6			= 0x01;
			public static final int SENSOR_VBATT			= 0x2000;
			public static final int SENSOR_D_ACCEL			= 0x1000; //XXX-RS-LSM-SensorClass?
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

		public static final String[] ListofBluetoothBaudRates = {"115200","1200","2400","4800","9600","19200","38400","57600","230400","460800","921600"};
		public static final Integer[] ListofBluetoothBaudRatesConfigValues = {0,1,2,3,4,5,6,7,8,9,10};
		
		public static final String[] ListOfOnOff = {"On","Off"};
		public static final Integer[] ListOfOnOffConfigValues = {0x01,0x00};

		//TODO Remove the need for this, standalone strings are too hardcoded and should be based on sensor/channel maps etc.
		@Deprecated 
		public static final String[] ListofCompatibleSensors = {
			"Low Noise Accelerometer",
			"Wide Range Accelerometer",
			"Gyroscope",
			"Magnetometer",
			"Battery Voltage",
			"External ADC A7",
			"External ADC A6",
			"External ADC A15",
			"Internal ADC A1",
			"Internal ADC A12",
			"Internal ADC A13",
			"Internal ADC A14",
			"Pressure",
			"GSR",
			"EXG1",
			"EXG2",
			"EXG1 16Bit",
			"EXG2 16Bit", 
			"Bridge Amplifier"}; 
		
//		public static final String[] ListofAccelRange = 					SensorLSM303.ListofAccelRange;
//		public static final Integer[] ListofLSM303DLHCAccelRangeConfigValues = SensorLSM303.ListofLSM303DLHCAccelRangeConfigValues;
//		public static final String[] ListofGyroRange = 						SensorMPU9X50.ListofGyroRange; 
//		public static final Integer[] ListofMPU9150GyroRangeConfigValues = 	SensorMPU9X50.ListofMPU9150GyroRangeConfigValues;
//		public static final String[] ListofMagRange = 						SensorLSM303.ListofMagRange;
//		public static final Integer[] ListofMagRangeConfigValues = 			SensorLSM303.ListofMagRangeConfigValues;

//		public static final String[] ListofPressureResolution = 			SensorBMP180.ListofPressureResolution;
//		public static final Integer[] ListofPressureResolutionConfigValues = SensorBMP180.ListofPressureResolutionConfigValues;
//		
//		public static final String[] ListofGSRRange = 						SensorGSR.ListofGSRRange;
//		public static final Integer[] ListofGSRRangeConfigValues = 			SensorGSR.ListofGSRRangeConfigValues;
//
//		public static final String[] ListOfPpgAdcSelection = 				SensorPPG.ListOfPpgAdcSelection;
//		public static final Integer[] ListOfPpgAdcSelectionConfigValues = 	SensorPPG.ListOfPpgAdcSelectionConfigValues;
//		public static final String[] ListOfPpg1AdcSelection = 				SensorPPG.ListOfPpg1AdcSelection;
//		public static final Integer[] ListOfPpg1AdcSelectionConfigValues = 	SensorPPG.ListOfPpg1AdcSelectionConfigValues;
//		public static final String[] ListOfPpg2AdcSelection = 				SensorPPG.ListOfPpg2AdcSelection;
//		public static final Integer[] ListOfPpg2AdcSelectionConfigValues = 	SensorPPG.ListOfPpg2AdcSelectionConfigValues;

		public static final String[] ListofDefaultEXG = 					SensorEXG.ListofDefaultEXG;
		public static final String[] ListOfExGGain = 						SensorEXG.ListOfExGGain;
		public static final Integer[] ListOfExGGainConfigValues = 			SensorEXG.ListOfExGGainConfigValues;
		
		public static final String[] ListOfECGReferenceElectrode = 			SensorEXG.ListOfECGReferenceElectrode;
		public static final Integer[] ListOfECGReferenceElectrodeConfigValues = SensorEXG.ListOfECGReferenceElectrodeConfigValues;
		public static final String[] ListOfEMGReferenceElectrode = 			SensorEXG.ListOfEMGReferenceElectrode;
		public static final Integer[] ListOfEMGReferenceElectrodeConfigValues = SensorEXG.ListOfEMGReferenceElectrodeConfigValues;
		public static final String[] ListOfExGReferenceElectrodeAll = 		SensorEXG.ListOfExGReferenceElectrodeAll;
		public static final Integer[] ListOfExGReferenceElectrodeConfigValuesAll = SensorEXG.ListOfExGReferenceElectrodeConfigValuesAll;
		public static final String[] ListOfRespReferenceElectrode = 		SensorEXG.ListOfRespReferenceElectrode;
		public static final Integer[] ListOfRespReferenceElectrodeConfigValues = SensorEXG.ListOfRespReferenceElectrodeConfigValues;
		public static final String[] ListOfTestReferenceElectrode = 		SensorEXG.ListOfTestReferenceElectrode;
		public static final Integer[] ListOfTestReferenceElectrodeConfigValues = SensorEXG.ListOfTestReferenceElectrodeConfigValues;
		
		public static final String[] ListOfExGLeadOffDetection = 			SensorEXG.ListOfExGLeadOffDetection;
		public static final Integer[] ListOfExGLeadOffDetectionConfigValues = SensorEXG.ListOfExGLeadOffDetectionConfigValues;
		public static final String[] ListOfExGLeadOffCurrent = 				SensorEXG.ListOfExGLeadOffCurrent;
		public static final Integer[] ListOfExGLeadOffCurrentConfigValues = SensorEXG.ListOfExGLeadOffCurrentConfigValues;
		public static final String[] ListOfExGLeadOffComparator = 			SensorEXG.ListOfExGLeadOffComparator;
		public static final Integer[] ListOfExGLeadOffComparatorConfigValues = SensorEXG.ListOfExGLeadOffComparatorConfigValues;
		public static final String[] ListOfExGResolutions = 				SensorEXG.ListOfExGResolutions;
		public static final Integer[] ListOfExGResolutionsConfigValues = 	SensorEXG.ListOfExGResolutionsConfigValues;
		public static final String[] ListOfExGRespirationDetectFreq = 		SensorEXG.ListOfExGRespirationDetectFreq;
		public static final Integer[] ListOfExGRespirationDetectFreqConfigValues = SensorEXG.ListOfExGRespirationDetectFreqConfigValues;

		public static final String[] ListOfExGRespirationDetectPhase32khz = SensorEXG.ListOfExGRespirationDetectPhase32khz;
		public static final Integer[] ListOfExGRespirationDetectPhase32khzConfigValues = SensorEXG.ListOfExGRespirationDetectPhase32khzConfigValues;
		public static final String[] ListOfExGRespirationDetectPhase64khz = SensorEXG.ListOfExGRespirationDetectPhase64khz;
		public static final Integer[] ListOfExGRespirationDetectPhase64khzConfigValues = SensorEXG.ListOfExGRespirationDetectPhase64khzConfigValues;

		public static final String[] ListOfExGRate = 						SensorEXG.ListOfExGRate;
		public static final Integer[] ListOfExGRateConfigValues = 			SensorEXG.ListOfExGRateConfigValues;

//		public static final String[] ListofLSM303DLHCAccelRate = 			SensorLSM303.ListofLSM303DLHCAccelRate;
//		public static final Integer[] ListofLSM303DLHCAccelRateConfigValues = SensorLSM303.ListofLSM303DLHCAccelRateConfigValues;
//		public static final String[] ListofLSM303DLHCAccelRateLpm = 		SensorLSM303.ListofLSM303DLHCAccelRateLpm;
//		public static final Integer[] ListofLSM303DLHCAccelRateLpmConfigValues = SensorLSM303.ListofLSM303DLHCAccelRateLpmConfigValues;
//		
//		public static final String[] ListofLSM303DLHCMagRate = 				SensorLSM303.ListofLSM303DLHCMagRate;
//		public static final Integer[] ListofLSM303DLHCMagRateConfigValues = SensorLSM303.ListofLSM303DLHCMagRateConfigValues;

//		public static final String[] ListofMPU9150AccelRange = 				SensorMPU9X50.ListofMPU9150AccelRange;
//		public static final Integer[] ListofMPU9150AccelRangeConfigValues = SensorMPU9X50.ListofMPU9150AccelRangeConfigValues;
//		public static final String[] ListofMPU9150MagRate = 				SensorMPU9X50.ListofMPU9150MagRate;
//		public static final Integer[] ListofMPU9150MagRateConfigValues = 	SensorMPU9X50.ListofMPU9150MagRateConfigValues;
//
//		public static final String[] ListofMPU9150MplCalibrationOptions = 	SensorMPU9X50.ListofMPU9150MplCalibrationOptions;
//		public static final Integer[] ListofMPU9150MplCalibrationOptionsConfigValues = SensorMPU9X50.ListofMPU9150MplCalibrationOptionsConfigValues;
//		public static final String[] ListofMPU9150MplLpfOptions = 			SensorMPU9X50.ListofMPU9150MplLpfOptions;
//		public static final Integer[] ListofMPU9150MplLpfOptionsConfigValues = SensorMPU9X50.ListofMPU9150MplLpfOptionsConfigValues;
//		public static final String[] ListofMPU9150MplRate = 				SensorMPU9X50.ListofMPU9150MplRate;
//		public static final Integer[] ListofMPU9150MplRateConfigValues = 	SensorMPU9X50.ListofMPU9150MplRateConfigValues;

		
		public class SensorMapKey{
			public static final int RESERVED_ANY_SENSOR = -1;
			
			public static final int HOST_SHIMMER_STREAMING_PROPERTIES = -100;
			//TODO below should be merged with HOST_REAL_TIME_CLOCK?
			public static final int HOST_SYSTEM_TIMESTAMP = -101;
			
			//Sensors channels originating from the Shimmer
			
			//Analog channels begin
			public static final int SHIMMER_TIMESTAMP = 1;
			/** Shimmer3 Low-noise analog accelerometer */
			public static final int SHIMMER_ANALOG_ACCEL = 2;
			public static final int SHIMMER_VBATT = 3;
			
			public static final int SHIMMER_EXT_EXP_ADC_A7 = 4;
			public static final int SHIMMER_EXT_EXP_ADC_A6 = 5;
			public static final int SHIMMER_EXT_EXP_ADC_A15 = 6;
			
			public static final int SHIMMER_INT_EXP_ADC_A12 = 7;
			public static final int HOST_PPG_A12 = 8; //GSR Board
			public static final int HOST_PPG1_A12 = 9; //Proto3 Deluxe Board
			public static final int SHIMMER_INT_EXP_ADC_A13 = 10;
			public static final int HOST_PPG_A13 = 11; //GSR Board
			public static final int HOST_PPG1_A13 = 12; //Proto3 Deluxe Board
			public static final int SHIMMER_INT_EXP_ADC_A14 = 13;
			public static final int HOST_PPG2_A14 = 14; //Proto3 Deluxe Board
			
			//TODO which ADCs should these be beside?
			public static final int SHIMMER_BRIDGE_AMP = 15;
			public static final int SHIMMER_RESISTANCE_AMP = 16;
			
			public static final int SHIMMER_INT_EXP_ADC_A1 = 17;
			public static final int HOST_PPG2_A1 = 18; //Proto3 Deluxe Board
			public static final int SHIMMER_GSR = 19;// Based on ADC1

			
			//Digital channels begin
			public static final int SHIMMER_MPU9150_GYRO = 30;
			/** Shimmer3 Wide-range digital accelerometer */
			public static final int SHIMMER_LSM303DLHC_ACCEL = 31;
			public static final int SHIMMER_LSM303DLHC_MAG = 32;
			/** Shimmer3 Alternative accelerometer */
			public static final int SHIMMER_MPU9150_ACCEL = 33;
			/** Shimmer3 Alternative magnetometer */
			public static final int SHIMMER_MPU9150_MAG = 34;
			public static final int SHIMMER_MPU9150_TEMP = 35;
			public static final int SHIMMER_BMP180_PRESSURE = 36;

			
//			public static final int SHIMMER_EXG1_24BIT = 3;
//			public static final int SHIMMER_EXG2_24BIT = 4;
			//public static final int SHIMMER_HR = 9;
//			public static final int SHIMMER_EXG1_16BIT = 19;
//			public static final int SHIMMER_EXG2_16BIT = 21;
			
			//public static final int SHIMMER_BMP180_TEMPERATURE = 23; // not yet implemented
			//public static final int SHIMMER_MSP430_TEMPERATURE = 24; // not yet implemented
			//public static final int SHIMMER_LSM303DLHC_TEMPERATURE = 26; // not yet implemented
			//public static final int SHIMMER_MPU9150_MPL_TEMPERATURE = 1<<17; // same as SENSOR_SHIMMER3_MPU9150_TEMP 
			
			public static final int SHIMMER_MPU9150_MPL_QUAT_6DOF = 50;
			public static final int SHIMMER_MPU9150_MPL_QUAT_9DOF = 51;
			public static final int SHIMMER_MPU9150_MPL_EULER_6DOF = 52;
			public static final int SHIMMER_MPU9150_MPL_EULER_9DOF = 53;
			public static final int SHIMMER_MPU9150_MPL_HEADING = 54;
			public static final int SHIMMER_MPU9150_MPL_PEDOMETER = 55;
			public static final int SHIMMER_MPU9150_MPL_TAP = 56;
			public static final int SHIMMER_MPU9150_MPL_MOTION_ORIENT = 57;
			public static final int SHIMMER_MPU9150_MPL_GYRO = 58;
			public static final int SHIMMER_MPU9150_MPL_ACCEL = 59;
			public static final int SHIMMER_MPU9150_MPL_MAG = 60;
			public static final int SHIMMER_MPU9150_MPL_QUAT_6DOF_RAW = 61;
	
			//Sensors channels modified or created on the host side
			// Combination Channels
			public static final int HOST_ECG = 100;
			public static final int HOST_EMG = 101;
			public static final int HOST_EXG_TEST = 102;
			public static final int HOST_EXG_CUSTOM = 116;
			
			// Derived Channels
			public static final int HOST_EXG_RESPIRATION = 103;
			public static final int HOST_SKIN_TEMPERATURE_PROBE = 104;
	
			public static final int HOST_PPG_DUMMY = 105;
			public static final int HOST_PPG1_DUMMY = 110;
			public static final int HOST_PPG2_DUMMY = 113;
			
			public static final int HOST_TIMESTAMP_SYNC = 151;
			public static final int HOST_REAL_TIME_CLOCK = 152;
			public static final int HOST_REAL_TIME_CLOCK_SYNC = 153;
			
			
			public static final int SHIMMER_ECG_TO_HR_FW = 150;
			
			public static final int THIRD_PARTY_NONIN = 1000;
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
//
//			public static final String LSM303DLHC_ACCEL_RATE = SensorLSM303.GuiLabelConfig.LSM303DLHC_ACCEL_RATE;
//			public static final String LSM303DLHC_ACCEL_RANGE = SensorLSM303.GuiLabelConfig.LSM303DLHC_ACCEL_RANGE;
//			public static final String LSM303DLHC_MAG_RANGE = SensorLSM303.GuiLabelConfig.LSM303DLHC_MAG_RANGE;
//			public static final String LSM303DLHC_MAG_RATE = SensorLSM303.GuiLabelConfig.LSM303DLHC_MAG_RATE;
			
//			public static final String PRESSURE_RESOLUTION =  SensorBMP180.GuiLabelConfig.PRESSURE_RESOLUTION;
//			
//			public static final String GSR_RANGE = SensorGSR.GuiLabelConfig.GSR_RANGE;
			
			public static final String EXG_RESOLUTION = SensorEXG.GuiLabelConfig.EXG_RESOLUTION;
			public static final String EXG_GAIN = SensorEXG.GuiLabelConfig.EXG_GAIN;
			public static final String EXG_BYTES = SensorEXG.GuiLabelConfig.EXG_BYTES;

			public static final String EXG_RATE = SensorEXG.GuiLabelConfig.EXG_RATE;
			public static final String EXG_REFERENCE_ELECTRODE = SensorEXG.GuiLabelConfig.EXG_REFERENCE_ELECTRODE;
			public static final String EXG_LEAD_OFF_DETECTION = SensorEXG.GuiLabelConfig.EXG_LEAD_OFF_DETECTION;
			public static final String EXG_LEAD_OFF_CURRENT = SensorEXG.GuiLabelConfig.EXG_LEAD_OFF_CURRENT;
			public static final String EXG_LEAD_OFF_COMPARATOR = SensorEXG.GuiLabelConfig.EXG_LEAD_OFF_COMPARATOR;
			public static final String EXG_RESPIRATION_DETECT_FREQ = SensorEXG.GuiLabelConfig.EXG_RESPIRATION_DETECT_FREQ;
			public static final String EXG_RESPIRATION_DETECT_PHASE = SensorEXG.GuiLabelConfig.EXG_RESPIRATION_DETECT_PHASE;

//			public static final String MPU9150_GYRO_RANGE = SensorMPU9X50.GuiLabelConfig.MPU9150_GYRO_RANGE;
//			public static final String MPU9150_GYRO_RATE = SensorMPU9X50.GuiLabelConfig.MPU9150_GYRO_RATE;
//			public static final String MPU9150_GYRO_RATE_HZ = SensorMPU9X50.GuiLabelConfig.MPU9150_GYRO_RATE_HZ;
//			
//			public static final String MPU9150_ACCEL_RANGE = SensorMPU9X50.GuiLabelConfig.MPU9150_ACCEL_RANGE;
//			public static final String MPU9150_DMP_GYRO_CAL = SensorMPU9X50.GuiLabelConfig.MPU9150_DMP_GYRO_CAL;
//			public static final String MPU9150_MPL_LPF = SensorMPU9X50.GuiLabelConfig.MPU9150_MPL_LPF;
//			public static final String MPU9150_MPL_RATE = SensorMPU9X50.GuiLabelConfig.MPU9150_MPL_RATE;
//			public static final String MPU9150_MAG_RATE = SensorMPU9X50.GuiLabelConfig.MPU9150_MAG_RATE;
//
//			public static final String MPU9150_DMP = SensorMPU9X50.GuiLabelConfig.MPU9150_DMP;
//			public static final String MPU9150_MPL = SensorMPU9X50.GuiLabelConfig.MPU9150_MPL;
//			public static final String MPU9150_MPL_9DOF_SENSOR_FUSION = SensorMPU9X50.GuiLabelConfig.MPU9150_MPL_9DOF_SENSOR_FUSION;
//			public static final String MPU9150_MPL_GYRO_CAL = SensorMPU9X50.GuiLabelConfig.MPU9150_MPL_GYRO_CAL;
//			public static final String MPU9150_MPL_VECTOR_CAL = SensorMPU9X50.GuiLabelConfig.MPU9150_MPL_VECTOR_CAL;
//			public static final String MPU9150_MPL_MAG_CAL = SensorMPU9X50.GuiLabelConfig.MPU9150_MPL_MAG_CAL;
//
//			public static final String LSM303DLHC_ACCEL_LPM = SensorLSM303.GuiLabelConfig.LSM303DLHC_ACCEL_LPM;
//			
//			public static final String MPU9150_GYRO_LPM = SensorMPU9X50.GuiLabelConfig.MPU9150_GYRO_LPM;
//			
//			public static final String LSM303DLHC_MAG_LPM = SensorLSM303.GuiLabelConfig.LSM303DLHC_MAG_LPM;
			
//			public static final String PPG_ADC_SELECTION =  SensorPPG.GuiLabelConfig.PPG_ADC_SELECTION;
//			public static final String PPG1_ADC_SELECTION = SensorPPG.GuiLabelConfig.PPG1_ADC_SELECTION;
//			public static final String PPG2_ADC_SELECTION = SensorPPG.GuiLabelConfig.PPG2_ADC_SELECTION;
//			
//			public static final String MPU9150_GYRO_DEFAULT_CALIB = SensorMPU9X50.GuiLabelConfig.MPU9150_GYRO_DEFAULT_CALIB;
//			
//			public static final String LSM303DLHC_ACCEL_DEFAULT_CALIB = SensorLSM303.GuiLabelConfig.LSM303DLHC_ACCEL_DEFAULT_CALIB;
//			public static final String LSM303DLHC_MAG_DEFAULT_CALIB = SensorLSM303.GuiLabelConfig.LSM303DLHC_MAG_DEFAULT_CALIB;
//
//			public static final String KXRB8_2042_ACCEL_DEFAULT_CALIB = SensorKionixKXRB52042.GuiLabelConfig.KXRB8_2042_ACCEL_DEFAULT_CALIB;

			public static final String TCX0 = "TCX0";
			public static final String INT_EXP_BRD_POWER_BOOLEAN = "Internal Expansion Board Power";
			public static final String INT_EXP_BRD_POWER_INTEGER = "Int Exp Power";

			public static final String KINEMATIC_LPM = "Kinematic Sensors Low-Power Mode";//XXX-RS-LSM-SensorClass? What about HighResolutionMode?!
			public static final String KINEMATIC_CALIBRATION_ALL = "Kinematic Calibration all";
		}

		// GUI Sensor Tiles
		public enum GuiLabelSensorTiles{
			//XXX-RS-AA-SensorClass?
			LOW_NOISE_ACCEL(SensorKionixKXRB52042.GuiLabelSensorTiles.LOW_NOISE_ACCEL),
			GYRO(SensorMPU9X50.GuiLabelSensorTiles.GYRO),
			
			//XXX-RS-LSM-SensorClass?
			MAG(SensorLSM303.GuiLabelSensorTiles.MAG),
			
			MPU(SensorMPU9X50.GuiLabelSensorTiles.MPU),
			BATTERY_MONITORING(SensorBattVoltage.GuiLabelSensorTiles.BATTERY_MONITORING),
			
			//XXX-RS-LSM-SensorClass?
			WIDE_RANGE_ACCEL(SensorLSM303.GuiLabelSensorTiles.WIDE_RANGE_ACCEL),
			
			PRESSURE_TEMPERATURE(SensorBMP180.GuiLabelSensorTiles.PRESSURE_TEMPERATURE),
			EXTERNAL_EXPANSION_ADC(SensorADC.GuiLabelSensorTiles.EXTERNAL_EXPANSION_ADC), //"External Expansion ADCs"), //YYY
			INTERNAL_EXPANSION_ADC(SensorADC.GuiLabelSensorTiles.INTERNAL_EXPANSION_ADC), //"Internal Expansion ADCs"), //YYY
			GSR(SensorGSR.GuiLabelSensorTiles.GSR),
			EXG("ECG/EMG"),
			PROTO3_MINI("Proto Mini"),
			PROTO3_DELUXE("Proto Deluxe"),
			PROTO3_DELUXE_SUPP(SensorPPG.GuiLabelSensorTiles.PROTO3_DELUXE_SUPP),
			BRIDGE_AMPLIFIER(SensorBridgeAmp.GuiLabelSensorTiles.BRIDGE_AMPLIFIER),
			BRIDGE_AMPLIFIER_SUPP(SensorBridgeAmp.GuiLabelSensorTiles.BRIDGE_AMPLIFIER_SUPP),
			HIGH_G_ACCEL(Configuration.Shimmer3.GuiLabelSensors.HIGH_G_ACCEL),
			MPU_ACCEL_GYRO_MAG(SensorMPU9X50.GuiLabelSensorTiles.MPU_ACCEL_GYRO_MAG), // "MPU 9DoF"),
			MPU_OTHER(SensorMPU9X50.GuiLabelSensorTiles.MPU_OTHER), //"MPU Other"),
			GPS("GPS");
			
			private String tileText = "";
			
			GuiLabelSensorTiles(String text){
				this.tileText = text;
			}
			
			public String getTileText(){
				return tileText;
			}
		}
		
		//GUI SENSORS
		public class GuiLabelSensors{
//			public static final String ACCEL_LN = SensorKionixKXRB52042.GuiLabelSensors.ACCEL_LN;
			
//			public static final String BATTERY = SensorBattVoltage.GuiLabelSensors.BATTERY;
			
//			public static final String EXT_EXP_A7 = SensorADC.GuiLabelSensors.EXT_EXP_A7;
//			public static final String EXT_EXP_A6 = SensorADC.GuiLabelSensors.EXT_EXP_A6;
//			public static final String EXT_EXP_A15 = SensorADC.GuiLabelSensors.EXT_EXP_A15;
//			public static final String INT_EXP_A12 = SensorADC.GuiLabelSensors.INT_EXP_A12;
//			public static final String INT_EXP_A13 = SensorADC.GuiLabelSensors.INT_EXP_A13;
//			public static final String INT_EXP_A14 = SensorADC.GuiLabelSensors.INT_EXP_A14;
//			public static final String INT_EXP_A1 = SensorADC.GuiLabelSensors.INT_EXP_A1;
//			
			public static final String BRIDGE_AMPLIFIER = SensorBridgeAmp.GuiLabelSensors.BRIDGE_AMPLIFIER;
			public static final String RESISTANCE_AMP = SensorBridgeAmp.GuiLabelSensors.RESISTANCE_AMP;
			public static final String SKIN_TEMP_PROBE = SensorBridgeAmp.GuiLabelSensors.SKIN_TEMP_PROBE;
			public static final String BRAMP_HIGHGAIN = SensorBridgeAmp.GuiLabelSensors.BRAMP_HIGHGAIN;
			public static final String BRAMP_LOWGAIN = SensorBridgeAmp.GuiLabelSensors.BRAMP_LOWGAIN;

//			public static final String GSR = SensorGSR.GuiLabelSensors.GSR;
			
//			public static final String ACCEL_WR = SensorLSM303.GuiLabelSensors.ACCEL_WR;
			public static final String MAG = SensorLSM303.GuiLabelSensors.MAG;

//			public static final String PRESS_TEMP_BMP180 = 	SensorBMP180.GuiLabelSensors.PRESS_TEMP_BMP180;

//			public static final String GYRO = SensorMPU9X50.GuiLabelSensors.GYRO;
//			public static final String ACCEL_MPU = SensorMPU9X50.GuiLabelSensors.ACCEL_MPU;
//			public static final String MAG_MPU = SensorMPU9X50.GuiLabelSensors.MAG_MPU;

			public static final String GYRO_MPU_MPL = SensorMPU9X50.GuiLabelSensors.GYRO_MPU_MPL;
			public static final String ACCEL_MPU_MPL = SensorMPU9X50.GuiLabelSensors.ACCEL_MPU_MPL;
			public static final String MAG_MPU_MPL = SensorMPU9X50.GuiLabelSensors.MAG_MPU_MPL;
			
			public static final String QUAT_MPL_6DOF = SensorMPU9X50.GuiLabelSensors.QUAT_MPL_6DOF;
			public static final String QUAT_MPL_9DOF = SensorMPU9X50.GuiLabelSensors.QUAT_MPL_9DOF;
			public static final String EULER_MPL_6DOF = SensorMPU9X50.GuiLabelSensors.EULER_MPL_6DOF;
			public static final String EULER_MPL_9DOF = SensorMPU9X50.GuiLabelSensors.EULER_MPL_9DOF;
			public static final String QUAT_DMP_6DOF = SensorMPU9X50.GuiLabelSensors.QUAT_DMP_6DOF;

			public static final String MPL_HEADING = SensorMPU9X50.GuiLabelSensors.MPL_HEADING;
			public static final String MPL_TEMPERATURE = SensorMPU9X50.GuiLabelSensors.MPL_TEMPERATURE;
			public static final String MPL_PEDOM_CNT = SensorMPU9X50.GuiLabelSensors.MPL_PEDOM_CNT; // not currently supported
			public static final String MPL_PEDOM_TIME = SensorMPU9X50.GuiLabelSensors.MPL_PEDOM_TIME; // not currently supported
			public static final String MPL_TAPDIRANDTAPCNT = SensorMPU9X50.GuiLabelSensors.MPL_TAPDIRANDTAPCNT; //"TapDirAndTapCnt"; // not currently supported
			public static final String MPL_MOTIONANDORIENT = SensorMPU9X50.GuiLabelSensors.MPL_MOTIONANDORIENT; //"MotionAndOrient"; // not currently supported
			
			public static final String ECG_TO_HR = "ECG To HR";
			public static final String PPG_TO_HR = "PPG To HR";
			
			//TODO: refer to OrienationModule instead
			public static final String ORIENTATION_3D_6DOF = "3D Orientation (6DOF)"; 
			public static final String ORIENTATION_3D_9DOF = "3D Orientation (9DOF)"; 
			public static final String EULER_ANGLES_6DOF = "Euler Angles (6DOF)"; 
			public static final String EULER_ANGLES_9DOF = "Euler Angles (9DOF)";   // needed in both MPU and Algorithm???

			public static final String HIGH_G_ACCEL = "200g Accel";

//			public static final String PPG_DUMMY = SensorPPG.GuiLabelSensors.PPG_DUMMY;
//			public static final String PPG_A12 = SensorPPG.GuiLabelSensors.PPG_A12;
//			public static final String PPG_A13 = SensorPPG.GuiLabelSensors.PPG_A13;
//			public static final String PPG1_DUMMY = SensorPPG.GuiLabelSensors.PPG1_DUMMY;
//			public static final String PPG1_A12 = SensorPPG.GuiLabelSensors.PPG1_A12;
//			public static final String PPG1_A13 = SensorPPG.GuiLabelSensors.PPG1_A13;
//			public static final String PPG2_DUMMY = SensorPPG.GuiLabelSensors.PPG2_DUMMY;
//			public static final String PPG2_A1 = SensorPPG.GuiLabelSensors.PPG2_A1;
//			public static final String PPG2_A14 = SensorPPG.GuiLabelSensors.PPG2_A14;
			
			public static final String EMG = SensorEXG.GuiLabelSensors.EMG;
			public static final String ECG = SensorEXG.GuiLabelSensors.ECG;
			public static final String EXG_TEST = SensorEXG.GuiLabelSensors.EXG_TEST;
			public static final String EXG_RESPIRATION = SensorEXG.GuiLabelSensors.EXG_RESPIRATION;
			public static final String EXG1_24BIT = SensorEXG.GuiLabelSensors.EXG1_24BIT;
			public static final String EXG2_24BIT = SensorEXG.GuiLabelSensors.EXG2_24BIT;
			public static final String EXG1_16BIT = SensorEXG.GuiLabelSensors.EXG1_16BIT;
			public static final String EXG2_16BIT = SensorEXG.GuiLabelSensors.EXG2_16BIT;
			public static final String EXG_CUSTOM = SensorEXG.GuiLabelSensors.EXG_CUSTOM;
		}
		
		// GUI Algorithm Grouping
		public enum GuiLabelAlgorithmGrouping{
			ORIENTATION_9DOF("9DOF"),  
			ORIENTATION_6DOF("6DOF"),  
			ECG_TO_HR("ECG-to-HR"),
			PPG_TO_HR("PPG-to-HR"),
			HRV_ECG("HRV");

			private String tileText = "";
			
			GuiLabelAlgorithmGrouping(String text){
				this.tileText = text;
			}
			
			public String getTileText(){
				return tileText;
			}
		}

		//DATABASE NAMES
		public static class DatabaseChannelHandles{
			public static final String NONE = "";
			
			public static final String EXT_ADC_A7 = SensorADC.DatabaseChannelHandles.EXT_ADC_A7;
			public static final String EXT_ADC_A6 = SensorADC.DatabaseChannelHandles.EXT_ADC_A6;
			public static final String EXT_ADC_A15 = SensorADC.DatabaseChannelHandles.EXT_ADC_A15;
			public static final String INT_ADC_A1 = SensorADC.DatabaseChannelHandles.INT_ADC_A1;
			public static final String INT_ADC_A12 = SensorADC.DatabaseChannelHandles.INT_ADC_A12;
			public static final String INT_ADC_A13 = SensorADC.DatabaseChannelHandles.INT_ADC_A13;
			public static final String INT_ADC_A14 = SensorADC.DatabaseChannelHandles.INT_ADC_A14;
			public static final String BATTERY = SensorBattVoltage.DatabaseChannelHandles.BATTERY;
			public static final String GSR = SensorGSR.DatabaseChannelHandles.GSR;
			public static final String PRESSURE = SensorBMP180.DatabaseChannelHandles.PRESSURE;
			public static final String MPU_HEADING = SensorMPU9X50.DatabaseChannelHandles.MPU_HEADING; // not available but supported in FW
			public static final String MPU_TEMP = SensorMPU9X50.DatabaseChannelHandles.MPU_TEMP;

			public static final String LN_ACC_X = SensorKionixKXRB52042.DatabaseChannelHandles.LN_ACC_X;
			public static final String LN_ACC_Y = SensorKionixKXRB52042.DatabaseChannelHandles.LN_ACC_Y;
			public static final String LN_ACC_Z = SensorKionixKXRB52042.DatabaseChannelHandles.LN_ACC_Z;
			
			public static final String WR_ACC_X = SensorLSM303.DatabaseChannelHandles.WR_ACC_X;
			public static final String WR_ACC_Y = SensorLSM303.DatabaseChannelHandles.WR_ACC_Y;
			public static final String WR_ACC_Z = SensorLSM303.DatabaseChannelHandles.WR_ACC_Z;
			public static final String MAG_X = SensorLSM303.DatabaseChannelHandles.MAG_X;
			public static final String MAG_Y = SensorLSM303.DatabaseChannelHandles.MAG_Y;
			public static final String MAG_Z = SensorLSM303.DatabaseChannelHandles.MAG_Z;
			
			public static final String GYRO_X = SensorMPU9X50.DatabaseChannelHandles.GYRO_X;
			public static final String GYRO_Y = SensorMPU9X50.DatabaseChannelHandles.GYRO_Y;
			public static final String GYRO_Z = SensorMPU9X50.DatabaseChannelHandles.GYRO_Z;
	//		public static final String BATTERY = "F5437a_INT_A2_BATTERY"; --> already define for the shimmerCongig Table
	//		public static final String EXT_ADC_A7 = "F5437a_Ext_A7"; --> already define for the shimmerCongig Table
	//		public static final String EXT_ADC_A6 = "F5437a_Ext_A6"; --> already define for the shimmerCongig Table
	//		public static final String EXT_ADC_A15 = "F5437a_Ext_A15"; --> already define for the shimmerCongig Table
	//		public static final String INT_ADC_A1 = "F5437a_Int_A1"; --> already define for the shimmerCongig Table
	//		public static final String INT_ADC_A12 = "F5437a_Int_A12"; --> already define for the shimmerCongig Table
	//		public static final String INT_ADC_A13 = "F5437a_Int_A13"; --> already define for the shimmerCongig Table
	//		public static final String INT_ADC_A14 = "F5437a_Int_A14"; --> already define for the shimmerCongig Table
	//		public static final String PRESSURE = "BMP180_Pressure"; --> already define for the shimmerCongig Table
			public static final String BRIDGE_AMPLIFIER_HIGH = SensorBridgeAmp.DatabaseChannelHandles.BRIDGE_AMPLIFIER_HIGH;
			public static final String BRIDGE_AMPLIFIER_LOW = SensorBridgeAmp.DatabaseChannelHandles.BRIDGE_AMPLIFIER_LOW;
			public static final String SKIN_TEMPERATURE = SensorBridgeAmp.DatabaseChannelHandles.SKIN_TEMPERATURE;
	//		public static final String GSR = "F5437a_Int_A1_GSR";  --> already define for the shimmerCongig Table
			public static final String RESISTANCE_AMPLIFIER = SensorBridgeAmp.DatabaseChannelHandles.RESISTANCE_AMPLIFIER;
			public static final String ALTERNATIVE_ACC_X = SensorMPU9X50.DatabaseChannelHandles.ALTERNATIVE_ACC_X; // not available but supported in FW
			public static final String ALTERNATIVE_ACC_Y = SensorMPU9X50.DatabaseChannelHandles.ALTERNATIVE_ACC_Y; // not available but supported in FW
			public static final String ALTERNATIVE_ACC_Z = SensorMPU9X50.DatabaseChannelHandles.ALTERNATIVE_ACC_Z; // not available but supported in FW
			public static final String ALTERNATIVE_MAG_X = SensorMPU9X50.DatabaseChannelHandles.ALTERNATIVE_MAG_X; // not available but supported in FW
			public static final String ALTERNATIVE_MAG_Y = SensorMPU9X50.DatabaseChannelHandles.ALTERNATIVE_MAG_Y; // not available but supported in FW
			public static final String ALTERNATIVE_MAG_Z = SensorMPU9X50.DatabaseChannelHandles.ALTERNATIVE_MAG_Z; // not available but supported in FW
			public static final String TEMPERATURE = SensorBMP180.DatabaseChannelHandles.TEMPERATURE;
	//		public static final String PRESSURE = "BMP180_Pressure"; -> already define for the shimmerCongig Table
			public static final String EXG1_CH1_24BITS = SensorEXG.DatabaseChannelHandles.EXG1_CH1_24BITS;
			public static final String EXG1_CH2_24BITS = SensorEXG.DatabaseChannelHandles.EXG1_CH2_24BITS;
			public static final String EXG2_CH1_24BITS = SensorEXG.DatabaseChannelHandles.EXG2_CH1_24BITS;
			public static final String EXG2_CH2_24BITS = SensorEXG.DatabaseChannelHandles.EXG2_CH2_24BITS;
			public static final String EXG1_CH1_16BITS = SensorEXG.DatabaseChannelHandles.EXG1_CH1_16BITS;
			public static final String EXG1_CH2_16BITS = SensorEXG.DatabaseChannelHandles.EXG1_CH2_16BITS;
			public static final String EXG2_CH1_16BITS = SensorEXG.DatabaseChannelHandles.EXG2_CH1_16BITS;
			public static final String EXG2_CH2_16BITS = SensorEXG.DatabaseChannelHandles.EXG2_CH2_16BITS;
			public static final String EXG1_STATUS = SensorEXG.DatabaseChannelHandles.EXG1_STATUS;
			public static final String EXG2_STATUS = SensorEXG.DatabaseChannelHandles.EXG2_STATUS;
			public static final String MPU_QUAT_6DOF_W = SensorMPU9X50.DatabaseChannelHandles.MPU_QUAT_6DOF_W;
			public static final String MPU_QUAT_6DOF_X = SensorMPU9X50.DatabaseChannelHandles.MPU_QUAT_6DOF_X;
			public static final String MPU_QUAT_6DOF_Y = SensorMPU9X50.DatabaseChannelHandles.MPU_QUAT_6DOF_Y;
			public static final String MPU_QUAT_6DOF_Z = SensorMPU9X50.DatabaseChannelHandles.MPU_QUAT_6DOF_Z;
			public static final String MPU_QUAT_9DOF_W = SensorMPU9X50.DatabaseChannelHandles.MPU_QUAT_9DOF_W;
			public static final String MPU_QUAT_9DOF_X = SensorMPU9X50.DatabaseChannelHandles.MPU_QUAT_9DOF_X;
			public static final String MPU_QUAT_9DOF_Y = SensorMPU9X50.DatabaseChannelHandles.MPU_QUAT_9DOF_Y;
			public static final String MPU_QUAT_9DOF_Z = SensorMPU9X50.DatabaseChannelHandles.MPU_QUAT_9DOF_Z;
			public static final String MPU_EULER_6DOF_X = SensorMPU9X50.DatabaseChannelHandles.MPU_EULER_6DOF_X; // not available but supported in FW
			public static final String MPU_EULER_6DOF_Y = SensorMPU9X50.DatabaseChannelHandles.MPU_EULER_6DOF_Y; // not available but supported in FW
			public static final String MPU_EULER_6DOF_Z = SensorMPU9X50.DatabaseChannelHandles.MPU_EULER_6DOF_Z; // not available but supported in FW
			public static final String MPU_EULER_9DOF_X = SensorMPU9X50.DatabaseChannelHandles.MPU_EULER_9DOF_X; // not available but supported in FW
			public static final String MPU_EULER_9DOF_Y = SensorMPU9X50.DatabaseChannelHandles.MPU_EULER_9DOF_Y; // not available but supported in FW
			public static final String MPU_EULER_9DOF_Z = SensorMPU9X50.DatabaseChannelHandles.MPU_EULER_9DOF_Z; // not available but supported in FW
	//		public static final String MPU_HEADING = "MPU9150_MPL_HEADING"; -> already define for the shimmerCongig Table
	//		public static final String MPU_TEMP = "MPU9150_Temperature"; -> already define for the shimmerCongig Table
			public static final String PEDOMETER_CNT = SensorMPU9X50.DatabaseChannelHandles.PEDOMETER_CNT; // not available but supported in FW
			public static final String PEDOMETER_TIME = SensorMPU9X50.DatabaseChannelHandles.PEDOMETER_TIME; // not available but supported in FW
			public static final String TAP_DIR_AND_CNT = SensorMPU9X50.DatabaseChannelHandles.TAP_DIR_AND_CNT; // not available but supported in FW
			public static final String MOTION_AND_ORIENT = SensorMPU9X50.DatabaseChannelHandles.MOTION_AND_ORIENT; // not available but supported in FW
			public static final String MPU_MPL_GYRO_X = SensorMPU9X50.DatabaseChannelHandles.MPU_MPL_GYRO_X;
			public static final String MPU_MPL_GYRO_Y = SensorMPU9X50.DatabaseChannelHandles.MPU_MPL_GYRO_Y;
			public static final String MPU_MPL_GYRO_Z = SensorMPU9X50.DatabaseChannelHandles.MPU_MPL_GYRO_Z;
			public static final String MPU_MPL_ACC_X = SensorMPU9X50.DatabaseChannelHandles.MPU_MPL_ACC_X;
			public static final String MPU_MPL_ACC_Y = SensorMPU9X50.DatabaseChannelHandles.MPU_MPL_ACC_Y;
			public static final String MPU_MPL_ACC_Z = SensorMPU9X50.DatabaseChannelHandles.MPU_MPL_ACC_Z;
			public static final String MPU_MPL_MAG_X = SensorMPU9X50.DatabaseChannelHandles.MPU_MPL_MAG_X;
			public static final String MPU_MPL_MAG_Y = SensorMPU9X50.DatabaseChannelHandles.MPU_MPL_MAG_Y;
			public static final String MPU_MPL_MAG_Z = SensorMPU9X50.DatabaseChannelHandles.MPU_MPL_MAG_Z;
			public static final String MPU_QUAT_6DOF_DMP_W = SensorMPU9X50.DatabaseChannelHandles.MPU_QUAT_6DOF_DMP_W;
			public static final String MPU_QUAT_6DOF_DMP_X = SensorMPU9X50.DatabaseChannelHandles.MPU_QUAT_6DOF_DMP_X;
			public static final String MPU_QUAT_6DOF_DMP_Y = SensorMPU9X50.DatabaseChannelHandles.MPU_QUAT_6DOF_DMP_Y;
			public static final String MPU_QUAT_6DOF_DMP_Z = SensorMPU9X50.DatabaseChannelHandles.MPU_QUAT_6DOF_DMP_Z;
			public static final String TIMESTAMP = "TimeStamp";
			public static final String TIMESTAMP_EXPORT = "Timestamp";
			public static final String OFFSET_TIMESTAMP = "Offset";
	//		public static final String REAL_TIME_CLOCK = "RealTime"; --> already define for the shimmerCongig Table
//			public static final String PPG_A12 = SensorPPG.DatabaseChannelHandles.PPG_A12;
//			public static final String PPG_A13 = SensorPPG.DatabaseChannelHandles.PPG_A13;
//			public static final String PPG1_A12 = SensorPPG.DatabaseChannelHandles.PPG1_A12;
//			public static final String PPG1_A13 = SensorPPG.DatabaseChannelHandles.PPG1_A13;
//			public static final String PPG2_A1 = SensorPPG.DatabaseChannelHandles.PPG2_A1;
//			public static final String PPG2_A14 = SensorPPG.DatabaseChannelHandles.PPG2_A14;
			
			/** Calibrated Data Table = Raw Data Table + some processed signals **/
			public static final String TIMESTAMP_SYSTEM = "System_Timestamp";
			public static final String TIMESTAMP_SYNC = "TimeStampSync";
			public static final String TIMESTAMP_SYNC_EXPORT = "TimestampSync";
			public static final String REAL_TIME_CLOCK_SYNC = "RealTimeSync";
			public static final String REAL_TIME_CLOCK = "Real_Time_Clock";
			public static final String FILTERED = "_Filtered"; // to create the name of the filtered signals
			
			//TODO: refer to OrienationModule instead
			public static final String ECG_TO_HR = "ECGToHR";
			public static final String PPG_TO_HR = "PPGToHR";
			
			//TODO: refer to OrienationModule instead
			public static final String QUARTENION_W_6DOF = "QUAT_MADGE_6DOF_W"; 
			public static final String QUARTENION_X_6DOF = "QUAT_MADGE_6DOF_X";
			public static final String QUARTENION_Y_6DOF = "QUAT_MADGE_6DOF_Y";
			public static final String QUARTENION_Z_6DOF = "QUAT_MADGE_6DOF_Z";
			public static final String QUARTENION_W_9DOF = "QUAT_MADGE_9DOF_W";
			public static final String QUARTENION_X_9DOF = "QUAT_MADGE_9DOF_X";
			public static final String QUARTENION_Y_9DOF = "QUAT_MADGE_9DOF_Y";
			public static final String QUARTENION_Z_9DOF = "QUAT_MADGE_9DOF_Z";
			public static final String EULER_6DOF_A = "EULER_6DOF_A"; 
			public static final String EULER_6DOF_X = "EULER_6DOF_X"; 
			public static final String EULER_6DOF_Y = "EULER_6DOF_Y"; 
			public static final String EULER_6DOF_Z = "EULER_6DOF_Z"; 
			public static final String EULER_9DOF_A = "EULER_9DOF_A"; 
			public static final String EULER_9DOF_X = "EULER_9DOF_X"; 
			public static final String EULER_9DOF_Y = "EULER_9DOF_Y"; 
			public static final String EULER_9DOF_Z = "EULER_9DOF_Z"; 
		}
		
		//GUI AND EXPORT CHANNELS
		public static class ObjectClusterSensorName{
			public static String EULER_9DOF_Y;
			public static String EULER_9DOF_Z;
			public static final String SHIMMER = "Shimmer";
			public static final String PACKET_RECEPTION_RATE_CURRENT = "Packet_Reception_Rate_Current";
			public static final String PACKET_RECEPTION_RATE_TRIAL = "Packet_Reception_Rate_Trial";
			
			public static String TIMESTAMP = "Timestamp";
			public static String REAL_TIME_CLOCK = "RealTime";
			public static String SYSTEM_TIMESTAMP = "System_Timestamp";
			public static String REAL_TIME_CLOCK_SYNC = "RealTime_Sync";
			public static String TIMESTAMP_SYNC = "Timestamp_Sync";
			public static String SYSTEM_TIMESTAMP_PLOT = "System_Timestamp_plot";

			public static String TIMESTAMP_OFFSET = "Offset";

			public static String ACCEL_LN_X = SensorKionixKXRB52042.ObjectClusterSensorName.ACCEL_LN_X;
			public static String ACCEL_LN_Y = SensorKionixKXRB52042.ObjectClusterSensorName.ACCEL_LN_Y;
			public static String ACCEL_LN_Z = SensorKionixKXRB52042.ObjectClusterSensorName.ACCEL_LN_Z;
			
			public static String BATTERY = SensorBattVoltage.ObjectClusterSensorName.BATTERY;
			public static final String BATT_PERCENTAGE = SensorBattVoltage.ObjectClusterSensorName.BATT_PERCENTAGE;
			
			public static String EXT_EXP_ADC_A7 = SensorADC.ObjectClusterSensorName.EXT_EXP_ADC_A7;
			public static String EXT_EXP_ADC_A6 = SensorADC.ObjectClusterSensorName.EXT_EXP_ADC_A6;
			public static String EXT_EXP_ADC_A15 = SensorADC.ObjectClusterSensorName.EXT_EXP_ADC_A15;
			public static String INT_EXP_ADC_A1 = SensorADC.ObjectClusterSensorName.INT_EXP_ADC_A1;
			public static String INT_EXP_ADC_A12 = SensorADC.ObjectClusterSensorName.INT_EXP_ADC_A12;
			public static String INT_EXP_ADC_A13 = SensorADC.ObjectClusterSensorName.INT_EXP_ADC_A13;
			public static String INT_EXP_ADC_A14 = SensorADC.ObjectClusterSensorName.INT_EXP_ADC_A14;
			
			public static String BRIDGE_AMP_HIGH = SensorBridgeAmp.ObjectClusterSensorName.BRIDGE_AMP_HIGH;
			public static String BRIDGE_AMP_LOW = SensorBridgeAmp.ObjectClusterSensorName.BRIDGE_AMP_LOW;
			public static String RESISTANCE_AMP = SensorBridgeAmp.ObjectClusterSensorName.RESISTANCE_AMP;
			public static final String SKIN_TEMPERATURE_PROBE = SensorBridgeAmp.ObjectClusterSensorName.SKIN_TEMPERATURE_PROBE;

			public static String GSR = SensorGSR.ObjectClusterSensorName.GSR;
			public static String GSR_CONDUCTANCE = SensorGSR.ObjectClusterSensorName.GSR_CONDUCTANCE;

			public static String ACCEL_WR_X = SensorLSM303.ObjectClusterSensorName.ACCEL_WR_X;
			public static String ACCEL_WR_Y = SensorLSM303.ObjectClusterSensorName.ACCEL_WR_Y;
			public static String ACCEL_WR_Z= SensorLSM303.ObjectClusterSensorName.ACCEL_WR_Z;
			public static String MAG_X = SensorLSM303.ObjectClusterSensorName.MAG_X;
			public static String MAG_Y = SensorLSM303.ObjectClusterSensorName.MAG_Y;
			public static String MAG_Z = SensorLSM303.ObjectClusterSensorName.MAG_Z;
			
			public static String TEMPERATURE_BMP180 = SensorBMP180.ObjectClusterSensorName.TEMPERATURE_BMP180;
			public static String PRESSURE_BMP180 = SensorBMP180.ObjectClusterSensorName.PRESSURE_BMP180;
			
			public static String ECG_GQ = SensorEXG.ObjectClusterSensorName.ECG_GQ;
			public static String ECG_TO_HR_FW_GQ = "ECGToHR_FW";
			public static String ECG_TO_HR_SW_GQ = "ECGToHR_SW";
			public static String EXG1_STATUS = SensorEXG.ObjectClusterSensorName.EXG1_STATUS;
			public static String EXG2_STATUS = SensorEXG.ObjectClusterSensorName.EXG2_STATUS;
			public static String EXG1_CH1_24BIT = SensorEXG.ObjectClusterSensorName.EXG1_CH1_24BIT;
			public static String EXG1_CH2_24BIT = SensorEXG.ObjectClusterSensorName.EXG1_CH2_24BIT;
			public static String EXG1_CH1_16BIT = SensorEXG.ObjectClusterSensorName.EXG1_CH1_16BIT;
			public static String EXG1_CH2_16BIT = SensorEXG.ObjectClusterSensorName.EXG1_CH2_16BIT;
			public static String EXG2_CH1_24BIT = SensorEXG.ObjectClusterSensorName.EXG2_CH1_24BIT;
			public static String EXG2_CH2_24BIT = SensorEXG.ObjectClusterSensorName.EXG2_CH2_24BIT;
			public static String EXG2_CH1_16BIT = SensorEXG.ObjectClusterSensorName.EXG2_CH1_16BIT;
			public static String EXG2_CH2_16BIT = SensorEXG.ObjectClusterSensorName.EXG2_CH2_16BIT;
			public static String EMG_CH1_24BIT = SensorEXG.ObjectClusterSensorName.EMG_CH1_24BIT;
			public static String EMG_CH2_24BIT = SensorEXG.ObjectClusterSensorName.EMG_CH2_24BIT;
			public static String EMG_CH1_16BIT = SensorEXG.ObjectClusterSensorName.EMG_CH1_16BIT;
			public static String EMG_CH2_16BIT = SensorEXG.ObjectClusterSensorName.EMG_CH2_16BIT;
			public static String ECG_LA_RA_24BIT = SensorEXG.ObjectClusterSensorName.ECG_LA_RA_24BIT;
			public static String ECG_LA_RL_24BIT = SensorEXG.ObjectClusterSensorName.ECG_LA_RL_24BIT;
			public static String ECG_LL_RA_24BIT = SensorEXG.ObjectClusterSensorName.ECG_LL_RA_24BIT;
			public static String ECG_LL_LA_24BIT = SensorEXG.ObjectClusterSensorName.ECG_LL_LA_24BIT; //derived
			public static String ECG_RESP_24BIT = SensorEXG.ObjectClusterSensorName.ECG_RESP_24BIT;
			public static String ECG_VX_RL_24BIT = SensorEXG.ObjectClusterSensorName.ECG_VX_RL_24BIT;
			public static String ECG_LA_RA_16BIT = SensorEXG.ObjectClusterSensorName.ECG_LA_RA_16BIT;
			public static String ECG_LA_RL_16BIT = SensorEXG.ObjectClusterSensorName.ECG_LA_RL_16BIT;
			public static String ECG_LL_RA_16BIT = SensorEXG.ObjectClusterSensorName.ECG_LL_RA_16BIT;
			public static String ECG_LL_LA_16BIT = SensorEXG.ObjectClusterSensorName.ECG_LL_LA_16BIT; //derived
			public static String ECG_RESP_16BIT = SensorEXG.ObjectClusterSensorName.ECG_RESP_16BIT;
			public static String ECG_VX_RL_16BIT = SensorEXG.ObjectClusterSensorName.ECG_VX_RL_16BIT;
			public static String EXG_TEST_CHIP1_CH1_24BIT = SensorEXG.ObjectClusterSensorName.EXG_TEST_CHIP1_CH1_24BIT;
			public static String EXG_TEST_CHIP1_CH2_24BIT = SensorEXG.ObjectClusterSensorName.EXG_TEST_CHIP1_CH2_24BIT;
			public static String EXG_TEST_CHIP2_CH1_24BIT = SensorEXG.ObjectClusterSensorName.EXG_TEST_CHIP2_CH1_24BIT;
			public static String EXG_TEST_CHIP2_CH2_24BIT = SensorEXG.ObjectClusterSensorName.EXG_TEST_CHIP2_CH2_24BIT;
			public static String EXG_TEST_CHIP1_CH1_16BIT = SensorEXG.ObjectClusterSensorName.EXG_TEST_CHIP1_CH1_16BIT;
			public static String EXG_TEST_CHIP1_CH2_16BIT = SensorEXG.ObjectClusterSensorName.EXG_TEST_CHIP1_CH2_16BIT;
			public static String EXG_TEST_CHIP2_CH1_16BIT = SensorEXG.ObjectClusterSensorName.EXG_TEST_CHIP2_CH1_16BIT;
			public static String EXG_TEST_CHIP2_CH2_16BIT = SensorEXG.ObjectClusterSensorName.EXG_TEST_CHIP2_CH2_16BIT;
			
			public static String GYRO_X = SensorMPU9X50.ObjectClusterSensorName.GYRO_X;
			public static String GYRO_Y = SensorMPU9X50.ObjectClusterSensorName.GYRO_Y;
			public static String GYRO_Z = SensorMPU9X50.ObjectClusterSensorName.GYRO_Z;
			public static String ACCEL_MPU_X = SensorMPU9X50.ObjectClusterSensorName.ACCEL_MPU_X;
			public static String ACCEL_MPU_Y = SensorMPU9X50.ObjectClusterSensorName.ACCEL_MPU_Y;
			public static String ACCEL_MPU_Z = SensorMPU9X50.ObjectClusterSensorName.ACCEL_MPU_Z;
			public static String MAG_MPU_X = SensorMPU9X50.ObjectClusterSensorName.MAG_MPU_X;
			public static String MAG_MPU_Y = SensorMPU9X50.ObjectClusterSensorName.MAG_MPU_Y;
			public static String MAG_MPU_Z = SensorMPU9X50.ObjectClusterSensorName.MAG_MPU_Z;
			
			public static String QUAT_MPL_6DOF_W = SensorMPU9X50.ObjectClusterSensorName.QUAT_MPL_6DOF_W;
			public static String QUAT_MPL_6DOF_X = SensorMPU9X50.ObjectClusterSensorName.QUAT_MPL_6DOF_X;
			public static String QUAT_MPL_6DOF_Y = SensorMPU9X50.ObjectClusterSensorName.QUAT_MPL_6DOF_Y;
			public static String QUAT_MPL_6DOF_Z = SensorMPU9X50.ObjectClusterSensorName.QUAT_MPL_6DOF_Z;
			public static String QUAT_MPL_9DOF_W = SensorMPU9X50.ObjectClusterSensorName.QUAT_MPL_9DOF_W;
			public static String QUAT_MPL_9DOF_X = SensorMPU9X50.ObjectClusterSensorName.QUAT_MPL_9DOF_X;
			public static String QUAT_MPL_9DOF_Y = SensorMPU9X50.ObjectClusterSensorName.QUAT_MPL_9DOF_Y;
			public static String QUAT_MPL_9DOF_Z = SensorMPU9X50.ObjectClusterSensorName.QUAT_MPL_9DOF_Z;
			public static String EULER_MPL_6DOF_X = SensorMPU9X50.ObjectClusterSensorName.EULER_MPL_6DOF_X;
			public static String EULER_MPL_6DOF_Y = SensorMPU9X50.ObjectClusterSensorName.EULER_MPL_6DOF_Y;
			public static String EULER_MPL_6DOF_Z = SensorMPU9X50.ObjectClusterSensorName.EULER_MPL_6DOF_Z;
			public static String EULER_MPL_9DOF_X = SensorMPU9X50.ObjectClusterSensorName.EULER_MPL_9DOF_X;
			public static String EULER_MPL_9DOF_Y = SensorMPU9X50.ObjectClusterSensorName.EULER_MPL_9DOF_Y;
			public static String EULER_MPL_9DOF_Z = SensorMPU9X50.ObjectClusterSensorName.EULER_MPL_9DOF_Z;
			
			public static String MPL_HEADING = SensorMPU9X50.ObjectClusterSensorName.MPL_HEADING;
			public static String MPL_TEMPERATURE = SensorMPU9X50.ObjectClusterSensorName.MPL_TEMPERATURE;
			public static String MPL_PEDOM_CNT = SensorMPU9X50.ObjectClusterSensorName.MPL_PEDOM_CNT;
			public static String MPL_PEDOM_TIME = SensorMPU9X50.ObjectClusterSensorName.MPL_PEDOM_TIME;
			public static String TAPDIRANDTAPCNT = SensorMPU9X50.ObjectClusterSensorName.TAPDIRANDTAPCNT;
			public static String MOTIONANDORIENT = SensorMPU9X50.ObjectClusterSensorName.MOTIONANDORIENT;
			
			public static String GYRO_MPU_MPL_X = SensorMPU9X50.ObjectClusterSensorName.GYRO_MPU_MPL_X;
			public static String GYRO_MPU_MPL_Y = SensorMPU9X50.ObjectClusterSensorName.GYRO_MPU_MPL_Y;
			public static String GYRO_MPU_MPL_Z = SensorMPU9X50.ObjectClusterSensorName.GYRO_MPU_MPL_Z;
			public static String ACCEL_MPU_MPL_X = SensorMPU9X50.ObjectClusterSensorName.ACCEL_MPU_MPL_X;
			public static String ACCEL_MPU_MPL_Y = SensorMPU9X50.ObjectClusterSensorName.ACCEL_MPU_MPL_Y;
			public static String ACCEL_MPU_MPL_Z = SensorMPU9X50.ObjectClusterSensorName.ACCEL_MPU_MPL_Z;
			public static String MAG_MPU_MPL_X = SensorMPU9X50.ObjectClusterSensorName.MAG_MPU_MPL_X;
			public static String MAG_MPU_MPL_Y = SensorMPU9X50.ObjectClusterSensorName.MAG_MPU_MPL_Y;
			public static String MAG_MPU_MPL_Z = SensorMPU9X50.ObjectClusterSensorName.MAG_MPU_MPL_Z;
			public static String QUAT_DMP_6DOF_W = SensorMPU9X50.ObjectClusterSensorName.QUAT_DMP_6DOF_W;
			public static String QUAT_DMP_6DOF_X = SensorMPU9X50.ObjectClusterSensorName.QUAT_DMP_6DOF_X;
			public static String QUAT_DMP_6DOF_Y = SensorMPU9X50.ObjectClusterSensorName.QUAT_DMP_6DOF_Y;
			public static String QUAT_DMP_6DOF_Z = SensorMPU9X50.ObjectClusterSensorName.QUAT_DMP_6DOF_Z;

			public static String EVENT_MARKER = "Event Marker";

//			public static String PPG_A12 = SensorPPG.ObjectClusterSensorName.PPG_A12;
//			public static String PPG_A13 = SensorPPG.ObjectClusterSensorName.PPG_A13;
//			public static String PPG1_A12 = SensorPPG.ObjectClusterSensorName.PPG1_A12;
//			public static String PPG1_A13 = SensorPPG.ObjectClusterSensorName.PPG1_A13;
//			public static String PPG2_A1 = SensorPPG.ObjectClusterSensorName.PPG2_A1;
//			public static String PPG2_A14 = SensorPPG.ObjectClusterSensorName.PPG2_A14;

			//TODO: move to algorithms class (JC).
			//Algorithms
			//TODO separate entries for LN accel vs. WR accel. 
			public static String QUAT_MADGE_6DOF_W = "Quat_Madge_6DOF_W"; 
			public static String QUAT_MADGE_6DOF_X = "Quat_Madge_6DOF_X"; 
			public static String QUAT_MADGE_6DOF_Y = "Quat_Madge_6DOF_Y"; 
			public static String QUAT_MADGE_6DOF_Z = "Quat_Madge_6DOF_Z"; 
			public static String QUAT_MADGE_9DOF_W = "Quat_Madge_9DOF_W"; 
			public static String QUAT_MADGE_9DOF_X = "Quat_Madge_9DOF_X"; 
			public static String QUAT_MADGE_9DOF_Y = "Quat_Madge_9DOF_Y"; 
			public static String QUAT_MADGE_9DOF_Z = "Quat_Madge_9DOF_Z";
			
			//axis angle 
			public static String AXIS_ANGLE_6DOF_A = "Axis_Angle_6DOF_A"; 
			public static String AXIS_ANGLE_6DOF_X = "Axis_Angle_6DOF_X"; 
			public static String AXIS_ANGLE_6DOF_Y = "Axis_Angle_6DOF_Y"; 
			public static String AXIS_ANGLE_6DOF_Z = "Axis_Angle_6DOF_Z"; 
			public static String AXIS_ANGLE_9DOF_A = "Axis_Angle_9DOF_A"; 
			public static String AXIS_ANGLE_9DOF_X = "Axis_Angle_9DOF_X"; 
			public static String AXIS_ANGLE_9DOF_Y = "Axis_Angle_9DOF_Y"; 
			public static String AXIS_ANGLE_9DOF_Z = "Axis_Angle_9DOF_Z"; 
			
			//Euler
			public static String EULER_6DOF_YAW = "Euler_6DOF_Yaw"; 
			public static String EULER_6DOF_PITCH = "Euler_6DOF_Pitch"; 
			public static String EULER_6DOF_ROLL = "Euler_6DOF_Roll"; 
			public static String EULER_9DOF_YAW = "Euler_9DOF_Yaw"; 
			public static String EULER_9DOF_PITCH = "Euler_9DOF_Pitch"; 
			public static String EULER_9DOF_ROLL = "Euler_9DOF_Roll"; 
			
			//TODO: axis angle 9DOF vs 6DOF??
			public static String AXIS_ANGLE_A = "Axis_Angle_A"; 
			public static String AXIS_ANGLE_X = "Axis_Angle_X"; 
			public static String AXIS_ANGLE_Y = "Axis_Angle_Y"; 
			public static String AXIS_ANGLE_Z = "Axis_Angle_Z"; 
			
//			// Moved by JC to algorithm module
//			public static String ECG_TO_HR_LA_RA = "ECGtoHR_LA-RA";
//			public static String ECG_TO_HR_LL_RA = "ECGtoHR_LL-RA";
//			public static String ECG_TO_HR_LL_LA = "ECGtoHR_LL_LA";
//			public static String ECG_TO_HR_VX_RL = "ECGtoHR_VX-RL";

			//TODO: remove two old channels names below
			public static String ECG_TO_HR = "ECGtoHR";

			public static  String PPG_TO_HR = "PPGtoHR";
			public static  String PPG_TO_HR1 = "PPGtoHR1";
			public static  String PPG_TO_HR2 = "PPGtoHR2";

		}
		
//		//Names used for parsing the GQ configuration header file 
//		public class HeaderFileSensorName{
//			public static final String SHIMMER3 = "shimmer3";
//			public static final String VBATT = "VBATT";
//			public static final String GSR = "GSR";
//			public static final String LSM303DLHC_ACCEL = "LSM303DLHC_ACCEL";
//		}
		
		
		public static class CompatibilityInfoForMaps{
			// These can be used to enable/disble GUI options depending on what HW, FW, Expansion boards versions are present
			private static final ShimmerVerObject svoAnyIntExpBoardAndFw = 		new ShimmerVerObject(ShimmerVerDetails.ANY_VERSION,ShimmerVerDetails.ANY_VERSION,ShimmerVerDetails.ANY_VERSION,ShimmerVerDetails.ANY_VERSION,ShimmerVerDetails.ANY_VERSION,ShimmerVerDetails.ANY_VERSION);
			private static final ShimmerVerObject svoAnyIntExpBoardAndSdlog = 		new ShimmerVerObject(HW_ID.SHIMMER_3,FW_ID.SDLOG,ShimmerVerDetails.ANY_VERSION,ShimmerVerDetails.ANY_VERSION,ShimmerVerDetails.ANY_VERSION,ShimmerVerDetails.ANY_VERSION);
			private static final ShimmerVerObject svoAnyIntExpBoardAndBtStream = 	new ShimmerVerObject(HW_ID.SHIMMER_3,FW_ID.BTSTREAM,ShimmerVerDetails.ANY_VERSION,ShimmerVerDetails.ANY_VERSION,ShimmerVerDetails.ANY_VERSION,ShimmerVerDetails.ANY_VERSION);
			private static final ShimmerVerObject svoAnyIntExpBoardAndLogAndStream = new ShimmerVerObject(HW_ID.SHIMMER_3,FW_ID.LOGANDSTREAM,ShimmerVerDetails.ANY_VERSION,ShimmerVerDetails.ANY_VERSION,ShimmerVerDetails.ANY_VERSION,ShimmerVerDetails.ANY_VERSION);

			private static final ShimmerVerObject svoNoIntExpBoardSdLog = 	new ShimmerVerObject(HW_ID.SHIMMER_3,FW_ID.SDLOG,0,8,0,ShimmerVerDetails.EXP_BRD_NONE_ID);

			private static final ShimmerVerObject svoSdLog = 				new ShimmerVerObject(HW_ID.SHIMMER_3,FW_ID.SDLOG,0,8,0,ShimmerVerDetails.ANY_VERSION);
			private static final ShimmerVerObject svoSdLogMpl = 			new ShimmerVerObject(HW_ID.SHIMMER_3,FW_ID.SDLOG,0,10,1,ShimmerVerDetails.ANY_VERSION);
			private static final ShimmerVerObject svoBtStream = 			new ShimmerVerObject(HW_ID.SHIMMER_3,FW_ID.BTSTREAM,0,5,0,ShimmerVerDetails.ANY_VERSION);
			private static final ShimmerVerObject svoLogAndStream = 		new ShimmerVerObject(HW_ID.SHIMMER_3,FW_ID.LOGANDSTREAM,0,3,3,ShimmerVerDetails.ANY_VERSION);

			private static final ShimmerVerObject svoShimmerGq802154Lr = 	new ShimmerVerObject(HW_ID.SHIMMER_GQ_802154_LR,ShimmerVerDetails.ANY_VERSION,ShimmerVerDetails.ANY_VERSION,ShimmerVerDetails.ANY_VERSION,ShimmerVerDetails.ANY_VERSION,ShimmerVerDetails.ANY_VERSION);
			private static final ShimmerVerObject svoShimmerGq802154Nr = 	new ShimmerVerObject(HW_ID.SHIMMER_GQ_802154_NR,ShimmerVerDetails.ANY_VERSION,ShimmerVerDetails.ANY_VERSION,ShimmerVerDetails.ANY_VERSION,ShimmerVerDetails.ANY_VERSION,ShimmerVerDetails.ANY_VERSION);
			private static final ShimmerVerObject svoShimmer2rGq = 		new ShimmerVerObject(HW_ID.SHIMMER_2R_GQ,ShimmerVerDetails.ANY_VERSION,ShimmerVerDetails.ANY_VERSION,ShimmerVerDetails.ANY_VERSION,ShimmerVerDetails.ANY_VERSION,ShimmerVerDetails.ANY_VERSION);

			private static final ShimmerVerObject svoExgSdLog = 			new ShimmerVerObject(HW_ID.SHIMMER_3,FW_ID.SDLOG,0,8,0,HW_ID_SR_CODES.EXP_BRD_EXG); 
			private static final ShimmerVerObject svoExgUnifiedSdLog = 	new ShimmerVerObject(HW_ID.SHIMMER_3,FW_ID.SDLOG,0,8,0,HW_ID_SR_CODES.EXP_BRD_EXG_UNIFIED);
			private static final ShimmerVerObject svoExgBtStream = 		new ShimmerVerObject(HW_ID.SHIMMER_3,FW_ID.BTSTREAM,0,5,0,HW_ID_SR_CODES.EXP_BRD_EXG);
			private static final ShimmerVerObject svoExgUnifiedBtStream = 	new ShimmerVerObject(HW_ID.SHIMMER_3,FW_ID.BTSTREAM,0,5,0,HW_ID_SR_CODES.EXP_BRD_EXG_UNIFIED);
			private static final ShimmerVerObject svoExgLogAndStream = 	new ShimmerVerObject(HW_ID.SHIMMER_3,FW_ID.LOGANDSTREAM,0,3,3,HW_ID_SR_CODES.EXP_BRD_EXG);
			private static final ShimmerVerObject svoExgUnifiedLogAndStream = new ShimmerVerObject(HW_ID.SHIMMER_3,FW_ID.LOGANDSTREAM,0,3,3,HW_ID_SR_CODES.EXP_BRD_EXG_UNIFIED);
			
			private static final ShimmerVerObject svoGsrSdLog = 			new ShimmerVerObject(HW_ID.SHIMMER_3,FW_ID.SDLOG,0,8,0,HW_ID_SR_CODES.EXP_BRD_GSR);
			private static final ShimmerVerObject svoGsrUnifiedSdLog = 	new ShimmerVerObject(HW_ID.SHIMMER_3,FW_ID.SDLOG,0,8,0,HW_ID_SR_CODES.EXP_BRD_GSR_UNIFIED);
			private static final ShimmerVerObject svoGsrBtStream = 		new ShimmerVerObject(HW_ID.SHIMMER_3,FW_ID.BTSTREAM,0,5,0,HW_ID_SR_CODES.EXP_BRD_GSR);
			private static final ShimmerVerObject svoGsrUnifiedBtStream = 	new ShimmerVerObject(HW_ID.SHIMMER_3,FW_ID.BTSTREAM,0,5,0,HW_ID_SR_CODES.EXP_BRD_GSR_UNIFIED);
			private static final ShimmerVerObject svoGsrLogAndStream = 	new ShimmerVerObject(HW_ID.SHIMMER_3,FW_ID.LOGANDSTREAM,0,3,3,HW_ID_SR_CODES.EXP_BRD_GSR);
			private static final ShimmerVerObject svoGsrUnifiedLogAndStream = 	new ShimmerVerObject(HW_ID.SHIMMER_3,FW_ID.LOGANDSTREAM,0,3,3,HW_ID_SR_CODES.EXP_BRD_GSR_UNIFIED);
			private static final ShimmerVerObject svoGsrGqBle = 			new ShimmerVerObject(HW_ID.SHIMMER_3,FW_ID.GQ_BLE,ShimmerVerDetails.ANY_VERSION,ShimmerVerDetails.ANY_VERSION,ShimmerVerDetails.ANY_VERSION,HW_ID_SR_CODES.EXP_BRD_GSR);
			private static final ShimmerVerObject svoGsrUnifiedGqBle = 	new ShimmerVerObject(HW_ID.SHIMMER_3,FW_ID.GQ_BLE,ShimmerVerDetails.ANY_VERSION,ShimmerVerDetails.ANY_VERSION,ShimmerVerDetails.ANY_VERSION,HW_ID_SR_CODES.EXP_BRD_GSR_UNIFIED);

			private static final ShimmerVerObject svoBrAmpSdLog = 			new ShimmerVerObject(HW_ID.SHIMMER_3,FW_ID.SDLOG,0,8,0,HW_ID_SR_CODES.EXP_BRD_BR_AMP);
			private static final ShimmerVerObject svoBrAmpUnifiedSdLog = 	new ShimmerVerObject(HW_ID.SHIMMER_3,FW_ID.SDLOG,0,8,0,HW_ID_SR_CODES.EXP_BRD_BR_AMP_UNIFIED);
			private static final ShimmerVerObject svoBrAmpBtStream = 		new ShimmerVerObject(HW_ID.SHIMMER_3,FW_ID.BTSTREAM,0,5,0,HW_ID_SR_CODES.EXP_BRD_BR_AMP);
			private static final ShimmerVerObject svoBrAmpUnifiedBtStream = new ShimmerVerObject(HW_ID.SHIMMER_3,FW_ID.BTSTREAM,0,5,0,HW_ID_SR_CODES.EXP_BRD_BR_AMP_UNIFIED);
			private static final ShimmerVerObject svoBrAmpLogAndStream = 	new ShimmerVerObject(HW_ID.SHIMMER_3,FW_ID.LOGANDSTREAM,0,3,3,HW_ID_SR_CODES.EXP_BRD_BR_AMP);
			private static final ShimmerVerObject svoBrAmpUnifiedLogAndStream = new ShimmerVerObject(HW_ID.SHIMMER_3,FW_ID.LOGANDSTREAM,0,3,3,HW_ID_SR_CODES.EXP_BRD_BR_AMP_UNIFIED);
			
			private static final ShimmerVerObject svoProto3MiniSdLog = 		new ShimmerVerObject(HW_ID.SHIMMER_3,FW_ID.SDLOG,0,8,0,HW_ID_SR_CODES.EXP_BRD_PROTO3_MINI);
			private static final ShimmerVerObject svoProto3MiniBtStream = 		new ShimmerVerObject(HW_ID.SHIMMER_3,FW_ID.BTSTREAM,0,5,0,HW_ID_SR_CODES.EXP_BRD_PROTO3_MINI);
			private static final ShimmerVerObject svoProto3MiniLogAndStream = 	new ShimmerVerObject(HW_ID.SHIMMER_3,FW_ID.LOGANDSTREAM,0,3,3,HW_ID_SR_CODES.EXP_BRD_PROTO3_MINI);

			private static final ShimmerVerObject svoProto3DeluxeSdLog = 		new ShimmerVerObject(HW_ID.SHIMMER_3,FW_ID.SDLOG,0,8,0,HW_ID_SR_CODES.EXP_BRD_PROTO3_DELUXE);
			private static final ShimmerVerObject svoProto3DeluxeBtStream = 	new ShimmerVerObject(HW_ID.SHIMMER_3,FW_ID.BTSTREAM,0,5,0,HW_ID_SR_CODES.EXP_BRD_PROTO3_DELUXE);
			private static final ShimmerVerObject svoProto3DeluxeLogAndStream =	new ShimmerVerObject(HW_ID.SHIMMER_3,FW_ID.LOGANDSTREAM,0,3,3,HW_ID_SR_CODES.EXP_BRD_PROTO3_DELUXE);

			private static final ShimmerVerObject svoHighGAccelSdLog = 		new ShimmerVerObject(HW_ID.SHIMMER_3,FW_ID.SDLOG,0,8,0,HW_ID_SR_CODES.EXP_BRD_HIGH_G_ACCEL);
			private static final ShimmerVerObject svoHighGAccelBtStream = 		new ShimmerVerObject(HW_ID.SHIMMER_3,FW_ID.BTSTREAM,0,5,0,HW_ID_SR_CODES.EXP_BRD_HIGH_G_ACCEL);
			private static final ShimmerVerObject svoHighGAccelLogAndStream = 	new ShimmerVerObject(HW_ID.SHIMMER_3,FW_ID.LOGANDSTREAM,0,3,3,HW_ID_SR_CODES.EXP_BRD_HIGH_G_ACCEL);

			private static final ShimmerVerObject svoShimmer4Stock = 			new ShimmerVerObject(HW_ID.SHIMMER_4_SDK,FW_ID.SHIMMER4_SDK_STOCK,ShimmerVerDetails.ANY_VERSION,ShimmerVerDetails.ANY_VERSION,ShimmerVerDetails.ANY_VERSION,ShimmerVerDetails.ANY_VERSION);
			
			public static final List<ShimmerVerObject> listOfCompatibleVersionInfoExg = Arrays.asList(
					svoExgSdLog, svoExgBtStream, svoExgLogAndStream,  
					svoExgUnifiedSdLog, svoExgUnifiedBtStream, svoExgUnifiedLogAndStream,
					svoShimmerGq802154Lr, svoShimmerGq802154Nr, svoShimmer2rGq,
					svoShimmer4Stock);
			
			private static final List<ShimmerVerObject> listOfCompatibleVersionInfoSdLog = Arrays.asList(svoSdLog);
			
			private static final List<ShimmerVerObject> listOfCompatibleVersionInfoAnyExpBoardAndFw = Arrays.asList(svoAnyIntExpBoardAndFw);
			
			public static final List<ShimmerVerObject> listOfCompatibleVersionInfoAnyExpBoardStandardFW = Arrays.asList(
					svoAnyIntExpBoardAndSdlog,svoAnyIntExpBoardAndBtStream,svoAnyIntExpBoardAndLogAndStream, 
					svoShimmer4Stock); 

			public static final List<ShimmerVerObject> listOfCompatibleVersionInfoGsr = Arrays.asList(
					svoGsrSdLog, svoGsrBtStream, svoGsrLogAndStream, svoGsrGqBle,
					svoGsrUnifiedSdLog,  svoGsrUnifiedBtStream, svoGsrUnifiedLogAndStream, 
					svoGsrUnifiedGqBle, svoShimmerGq802154Lr, svoShimmerGq802154Nr, svoShimmer2rGq,
					svoShimmer4Stock);
			
			public static final List<ShimmerVerObject> listOfCompatibleVersionInfoBMP180 = Arrays.asList(
					svoShimmer4Stock);  
			
			public static final List<ShimmerVerObject> listOfCompatibleVersionInfoBrAmp = Arrays.asList(
					svoBrAmpSdLog, svoBrAmpBtStream, svoBrAmpLogAndStream,  
					svoBrAmpUnifiedSdLog,  svoBrAmpUnifiedBtStream, svoBrAmpUnifiedLogAndStream,
					svoShimmer4Stock);

			private static final List<ShimmerVerObject> listOfCompatibleVersionInfoProto3Mini = Arrays.asList(
					svoProto3MiniSdLog, svoProto3MiniBtStream, svoProto3MiniLogAndStream);
			
			public static final List<ShimmerVerObject> listOfCompatibleVersionInfoProto3Deluxe = Arrays.asList(
					svoProto3DeluxeSdLog, svoProto3DeluxeBtStream, svoProto3DeluxeLogAndStream,
					svoShimmer4Stock);
			
			public static final List<ShimmerVerObject> listOfCompatibleVersionInfoIntExpA1 = Arrays.asList(
					svoProto3MiniSdLog, svoProto3MiniBtStream, svoProto3MiniLogAndStream, 
					svoProto3DeluxeSdLog, svoProto3DeluxeBtStream, svoProto3DeluxeLogAndStream, 
					svoHighGAccelSdLog, svoHighGAccelBtStream, svoHighGAccelLogAndStream,
					svoShimmer4Stock);
			
			public static final List<ShimmerVerObject> listOfCompatibleVersionInfoIntExpA12 = Arrays.asList(
					svoProto3MiniSdLog, svoProto3MiniBtStream, svoProto3MiniLogAndStream, 
					svoProto3DeluxeSdLog, svoProto3DeluxeBtStream, svoProto3DeluxeLogAndStream, 
					svoGsrSdLog, svoGsrBtStream, svoGsrLogAndStream, 
					svoGsrUnifiedSdLog, svoGsrUnifiedBtStream, svoGsrUnifiedLogAndStream,
					svoHighGAccelSdLog, svoHighGAccelBtStream, svoHighGAccelLogAndStream,
					svoShimmer4Stock);
			
			public static final List<ShimmerVerObject> listOfCompatibleVersionInfoIntExpA13 = Arrays.asList(
					svoProto3MiniSdLog, svoProto3MiniBtStream, svoProto3MiniLogAndStream, 
					svoProto3DeluxeSdLog, svoProto3DeluxeBtStream, svoProto3DeluxeLogAndStream, 
					svoGsrSdLog, svoGsrBtStream, svoGsrLogAndStream, 
					svoGsrUnifiedSdLog, svoGsrUnifiedBtStream, svoGsrUnifiedLogAndStream, 
					svoHighGAccelSdLog, svoHighGAccelBtStream, svoHighGAccelLogAndStream, 
					svoShimmer4Stock);
			
			public static final List<ShimmerVerObject> listOfCompatibleVersionInfoIntExpA14 = Arrays.asList(
					svoProto3MiniSdLog, svoProto3MiniBtStream, svoProto3MiniLogAndStream, 
					svoProto3DeluxeSdLog, svoProto3DeluxeBtStream, svoProto3DeluxeLogAndStream, 
					svoHighGAccelSdLog, svoHighGAccelBtStream, svoHighGAccelLogAndStream, 
					svoShimmer4Stock);

			public static final List<ShimmerVerObject> listOfCompatibleVersionInfoIntAdcsGeneral = Arrays.asList(
					svoGsrSdLog, svoGsrBtStream, svoGsrLogAndStream, 
					svoGsrUnifiedSdLog, svoGsrUnifiedBtStream, svoGsrUnifiedLogAndStream,
					svoHighGAccelSdLog, svoHighGAccelBtStream, svoHighGAccelLogAndStream,
					svoShimmer4Stock);

			private static final List<ShimmerVerObject> listOfCompatibleVersionInfoStreaming = Arrays.asList(
					svoBtStream, svoLogAndStream,
					svoShimmer4Stock);

			private static final List<ShimmerVerObject> listOfCompatibleVersionInfoLogging = Arrays.asList(
					svoSdLog, svoLogAndStream,
					svoShimmer4Stock);
			
			public static final List<ShimmerVerObject> listOfCompatibleVersionInfoRespiration = Arrays.asList(
					svoExgUnifiedSdLog, svoExgUnifiedBtStream, svoExgUnifiedLogAndStream,
					svoShimmer4Stock);

			private static final List<ShimmerVerObject> listOfCompatibleVersionInfoHighGAccel = Arrays.asList(
					svoHighGAccelSdLog,svoHighGAccelBtStream,svoHighGAccelLogAndStream);
			
			public static final List<ShimmerVerObject> listOfCompatibleVersionInfoMPLSensors = Arrays.asList(svoSdLogMpl);//,baseShimmer4); //TODO Shimmer4 temp here
			
			public static final List<ShimmerVerObject> listOfCompatibleVersionInfoShimmer4 = Arrays.asList(svoShimmer4Stock);
			
		}


	    public static final Map<Integer, SensorDetailsRef> mSensorMapRef;
	    static {
	        Map<Integer, SensorDetailsRef> aMap = new LinkedHashMap<Integer, SensorDetailsRef>();

	        //TODO decide what to do about the below:
//	        aMap.put(Configuration.Shimmer3.SensorMapKey.HOST_SHIMMER_STREAMING_PROPERTIES, ShimmerClock.sensorShimmerPacketReception); //YYY
//	        aMap.put(Configuration.Shimmer3.SensorMapKey.HOST_SHIMMER_STREAMING_PROPERTIES, new SensorDetailsRef(0, 0, ShimmerClock.GuiLabelSensors.DEVICE_PROPERTIES)); //YYY
//			//TODO sort out the difference between the below and ShimmerClock.sensorShimmerPacketReception  //YYY
//			// All Information required for parsing each of the channels
//			aMap.get(Configuration.Shimmer3.SensorMapKey.HOST_SHIMMER_STREAMING_PROPERTIES).mListOfChannelsRef = Arrays.asList(  //YYY
//					Configuration.Shimmer3.ObjectClusterSensorName.TIMESTAMP,
////					Configuration.Shimmer3.ObjectClusterSensorName.TIMESTAMP_SYNC,
//					//temp only! JC: delete after db sync works
////					Configuration.Shimmer3.ObjectClusterSensorName.REAL_TIME_CLOCK,
//					Configuration.Shimmer3.ObjectClusterSensorName.REAL_TIME_CLOCK_SYNC,
//					Configuration.Shimmer3.ObjectClusterSensorName.SYSTEM_TIMESTAMP,
//					Configuration.Shimmer3.ObjectClusterSensorName.BATT_PERCENTAGE,
//					Configuration.Shimmer3.ObjectClusterSensorName.PACKET_RECEPTION_RATE_CURRENT,
//					Configuration.Shimmer3.ObjectClusterSensorName.PACKET_RECEPTION_RATE_TRIAL,
//					Configuration.Shimmer3.ObjectClusterSensorName.EVENT_MARKER
//					); //YYY
	        aMap.putAll(ShimmerClock.mSensorMapRef);
	        
			// Assemble the channel map
			// NV_SENSORS0
//	        aMap.put(Configuration.Shimmer3.SensorMapKey.SHIMMER_ANALOG_ACCEL, SensorKionixKXRB52042.sensorKionixKXRB52042); //YYY
	        aMap.putAll(SensorKionixKXRB52042.mSensorMapRef);

			aMap.putAll(SensorMPU9X50.mSensorMapRef);
			aMap.putAll(SensorLSM303.mSensorMapRef);
			aMap.putAll(SensorGSR.mSensorMapRef);
//			aMap.put(Configuration.Shimmer3.SensorMapKey.SHIMMER_EXT_EXP_ADC_A7, SensorADC.sensorADC_EXT_EXP_ADC_A7Ref); //YYY
			aMap.putAll(SensorADC.mSensorMapRef);
//			aMap.put(Configuration.Shimmer3.SensorMapKey.SHIMMER_EXT_EXP_ADC_A6, SensorADC.sensorADC_EXT_EXP_ADC_A6Ref); //YYY
			
			// NV_SENSORS1
//			aMap.put(Configuration.Shimmer3.SensorMapKey.SHIMMER_BRIDGE_AMP, SensorBridgeAmp.sensorBridgeAmplifierRef); //YYY
			aMap.putAll(SensorBridgeAmp.mSensorMapRef);
//			aMap.put(Configuration.Shimmer3.SensorMapKey.SHIMMER_VBATT, SensorBattVoltage.sensorBattVoltageRef); //YYY
			aMap.putAll(SensorBattVoltage.mSensorMapRef);
//			aMap.put(Configuration.Shimmer3.SensorMapKey.SHIMMER_EXT_EXP_ADC_A15, SensorADC.sensorADC_EXT_EXP_ADC_A15Ref); //YYY
			aMap.putAll(SensorADC.mSensorMapRef);
//			aMap.put(Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A1, SensorADC.sensorADC_INT_EXP_ADC_A1Ref); //YYY
//			aMap.put(Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A12, SensorADC.sensorADC_INT_EXP_ADC_A12Ref); //YYY
//			aMap.put(Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A12, SensorADC.sensorADC_INT_EXP_ADC_A12Ref); //YYY
//			aMap.put(Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A13, SensorADC.sensorADC_INT_EXP_ADC_A13Ref);  //YYY
//			
			// NV_SENSORS2
//			aMap.put(Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A14, SensorADC.sensorADC_INT_EXP_ADC_A14Ref); //YYY
//	        aMap.put(Configuration.Shimmer3.SensorMapKey.SHIMMER_BMP180_PRESSURE, SensorBMP180.sensorBmp180); //YYY
			aMap.putAll(SensorBMP180.mSensorMapRef);
	        
//			aMap.put(Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_TEMP, SensorMPU9X50.sensorMpu9150TempRef); //YYY

			// NV_SENSORS3				

			// Derived Channels - Bridge Amp Board
//			aMap.put(Configuration.Shimmer3.SensorMapKey.SHIMMER_RESISTANCE_AMP, SensorBridgeAmp.sensorResistanceAmpRef);  //YYY
//			aMap.put(Configuration.Shimmer3.SensorMapKey.HOST_SKIN_TEMPERATURE_PROBE, SensorBridgeAmp.sensorSkinTempProbeRef); ///YYY
			

			// Derived Channels - GSR Board
			aMap.putAll(SensorPPG.mSensorMapRef);
//			aMap.put(Configuration.Shimmer3.SensorMapKey.HOST_PPG_DUMMY, SensorPPG.sensorPpgDummy);  //YYY
//			aMap.put(Configuration.Shimmer3.SensorMapKey.HOST_PPG_A12, SensorPPG.sensorPpgHostPPG_A12);  //YYY
//			aMap.put(Configuration.Shimmer3.SensorMapKey.HOST_PPG_A13, SensorPPG.sensorPpgHostPPG_A13);  //YYY
//
//			// Derived Channels - Proto3 Board
//			aMap.put(Configuration.Shimmer3.SensorMapKey.HOST_PPG1_DUMMY, SensorPPG.sensorPpg1Dummy);  //YYY
//			aMap.put(Configuration.Shimmer3.SensorMapKey.HOST_PPG1_A12, SensorPPG.sensorPpgHostPPG1_A12);  //YYY
//			aMap.put(Configuration.Shimmer3.SensorMapKey.HOST_PPG1_A13, SensorPPG.sensorPpgHostPPG1_A13);  //YYY
//			aMap.put(Configuration.Shimmer3.SensorMapKey.HOST_PPG2_DUMMY, SensorPPG.sensorPpg2Dummy);  //YYY
//			aMap.put(Configuration.Shimmer3.SensorMapKey.HOST_PPG2_A1, SensorPPG.sensorPpgHostPPG2_A1); //YYY
//			aMap.put(Configuration.Shimmer3.SensorMapKey.HOST_PPG2_A14, SensorPPG.sensorPpgHostPPG2_A14);  //YYY
			
			aMap.putAll(SensorEXG.mSensorMapRef);

			mSensorMapRef = Collections.unmodifiableMap(aMap);
	    }
	    
	    
	    public static final Map<String, ChannelDetails> mChannelMapRef;
	    static {
	        Map<String, ChannelDetails> aMap = new LinkedHashMap<String, ChannelDetails>();
	        
			aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.BATT_PERCENTAGE, SensorBattVoltage.channelBattPercentage);
	        
			aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.PACKET_RECEPTION_RATE_CURRENT, ShimmerClock.channelReceptionRateCurrent);
			aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.PACKET_RECEPTION_RATE_TRIAL, ShimmerClock.channelReceptionRateTrial);
			
			aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.EVENT_MARKER, ShimmerClock.channelEventMarker);
			
			// All Information required for parsing each of the channels
			//TODO incorporate 3 byte timestamp change for newer firmware
			aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.TIMESTAMP, ShimmerClock.channelShimmerClock2byte);
			
			//TODO replace with ShimmerClock instance
//			aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.SYSTEM_TIMESTAMP, ShimmerClock.channelSystemTimestamp);
			aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.SYSTEM_TIMESTAMP, SensorSystemTimeStamp.cDSystemTimestamp);
//			aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.SYSTEM_TIMESTAMP,
//					new ChannelDetails(
//							Configuration.Shimmer3.ObjectClusterSensorName.SYSTEM_TIMESTAMP,
//							Configuration.Shimmer3.ObjectClusterSensorName.SYSTEM_TIMESTAMP,
//							DatabaseChannelHandles.TIMESTAMP_SYSTEM,
//							CHANNEL_UNITS.MILLISECONDS,
//							Arrays.asList(CHANNEL_TYPE.CAL), false, true));
			
			//TODO replace with ShimmerClock instance
//			aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.SYSTEM_TIMESTAMP_PLOT, ShimmerClock.channelSystemTimestampPlot);
			aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.SYSTEM_TIMESTAMP_PLOT, SensorSystemTimeStamp.cDSystemTimestampPlot);
			
			//TODO replace with ShimmerClock instance
			aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.REAL_TIME_CLOCK, ShimmerClock.channelRealTimeClock);
//			aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.REAL_TIME_CLOCK,
//					new ChannelDetails(
//							Configuration.Shimmer3.ObjectClusterSensorName.REAL_TIME_CLOCK,
//							Configuration.Shimmer3.ObjectClusterSensorName.REAL_TIME_CLOCK,
//							DatabaseChannelHandles.REAL_TIME_CLOCK,
//							CHANNEL_UNITS.MILLISECONDS,
//							Arrays.asList(CHANNEL_TYPE.CAL), false, true));

			//TODO replace with ShimmerClock instance
			aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.REAL_TIME_CLOCK, ShimmerClock.channelRealTimeClockSync);
//			aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.REAL_TIME_CLOCK_SYNC,
//					new ChannelDetails(
//							Configuration.Shimmer3.ObjectClusterSensorName.REAL_TIME_CLOCK_SYNC,
//							Configuration.Shimmer3.ObjectClusterSensorName.REAL_TIME_CLOCK_SYNC,
//							DatabaseChannelHandles.REAL_TIME_CLOCK_SYNC,
//							CHANNEL_UNITS.MILLISECONDS,
//							Arrays.asList(CHANNEL_TYPE.CAL), false, true));
			
//			TIMESTAMP_SYNC
			
			// KionixKXRB52042 - analog accel
			aMap.putAll(SensorKionixKXRB52042.mChannelMapRef);
			
//			aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.BATTERY, SensorBattVoltage.channelBattVolt);  //YYY
			aMap.putAll(SensorBattVoltage.mChannelMapRef);

			// External and Internal ADCs
			aMap.putAll(SensorADC.mChannelMapRef);

			// Bridge Amp
			// Philips Skin Temperature Probe (through Bridge Amp)
			// Resistance Amplifier
			aMap.putAll(SensorBridgeAmp.mChannelMapRef);
			
			// GSR
			aMap.putAll(SensorGSR.mChannelMapRef);

			// MPU9150
			aMap.putAll(SensorMPU9X50.mChannelMapRef);

			// LSM303
			aMap.putAll(SensorLSM303.mChannelMapRef);

			//bmp180 pressure and temperature
			aMap.putAll(SensorBMP180.mChannelMapRef);

			//Exg
			aMap.putAll(SensorEXG.mChannelMapRef);

			// PPG - Using GSR+ board
			// PPG - Using Proto3 Deluxe TRRS Socket 1
			// PPG - Using Proto3 Deluxe TRRS Socket 2
			aMap.putAll(SensorPPG.mChannelMapRef);

			// Algorithm Channels
			aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.ECG_TO_HR, SensorECGToHRFw.channelEcgToHr);
			
			aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.PPG_TO_HR,
					new ChannelDetails(
							Configuration.Shimmer3.ObjectClusterSensorName.PPG_TO_HR,
							Configuration.Shimmer3.ObjectClusterSensorName.PPG_TO_HR,
							DatabaseChannelHandles.PPG_TO_HR,
							CHANNEL_UNITS.BEATS_PER_MINUTE,
							Arrays.asList(CHANNEL_TYPE.CAL)));
			
//			//TODO move to OrientationModule?
//			aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.QUAT_MADGE_6DOF_W,
//					new ChannelDetails(
//							Configuration.Shimmer3.ObjectClusterSensorName.QUAT_MADGE_6DOF_W,
//							Configuration.Shimmer3.ObjectClusterSensorName.QUAT_MADGE_6DOF_W,
//							DatabaseChannelHandles.QUARTENION_W_6DOF,
//							CHANNEL_UNITS.NO_UNITS,
//							Arrays.asList(CHANNEL_TYPE.CAL)));
//			aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.QUAT_MADGE_6DOF_X,
//					new ChannelDetails(
//							Configuration.Shimmer3.ObjectClusterSensorName.QUAT_MADGE_6DOF_X,
//							Configuration.Shimmer3.ObjectClusterSensorName.QUAT_MADGE_6DOF_X,
//							DatabaseChannelHandles.QUARTENION_X_6DOF,
//							CHANNEL_UNITS.NO_UNITS,
//							Arrays.asList(CHANNEL_TYPE.CAL)));
//			aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.QUAT_MADGE_6DOF_Y,
//					new ChannelDetails(
//							Configuration.Shimmer3.ObjectClusterSensorName.QUAT_MADGE_6DOF_Y,
//							Configuration.Shimmer3.ObjectClusterSensorName.QUAT_MADGE_6DOF_Y,
//							DatabaseChannelHandles.QUARTENION_Y_6DOF,
//							CHANNEL_UNITS.NO_UNITS,
//							Arrays.asList(CHANNEL_TYPE.CAL)));
//			aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.QUAT_MADGE_6DOF_Z,
//					new ChannelDetails(
//							Configuration.Shimmer3.ObjectClusterSensorName.QUAT_MADGE_6DOF_Z,
//							Configuration.Shimmer3.ObjectClusterSensorName.QUAT_MADGE_6DOF_Z,
//							DatabaseChannelHandles.QUARTENION_Z_6DOF,
//							CHANNEL_UNITS.NO_UNITS,
//							Arrays.asList(CHANNEL_TYPE.CAL)));
//			aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.QUAT_MADGE_9DOF_W,
//					new ChannelDetails(
//							Configuration.Shimmer3.ObjectClusterSensorName.QUAT_MADGE_9DOF_W,
//							Configuration.Shimmer3.ObjectClusterSensorName.QUAT_MADGE_9DOF_W,
//							DatabaseChannelHandles.QUARTENION_W_9DOF,
//							CHANNEL_UNITS.NO_UNITS,
//							Arrays.asList(CHANNEL_TYPE.CAL)));
//			aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.QUAT_MADGE_9DOF_X,
//					new ChannelDetails(
//							Configuration.Shimmer3.ObjectClusterSensorName.QUAT_MADGE_9DOF_X,
//							Configuration.Shimmer3.ObjectClusterSensorName.QUAT_MADGE_9DOF_X,
//							DatabaseChannelHandles.QUARTENION_X_9DOF,
//							CHANNEL_UNITS.NO_UNITS,
//							Arrays.asList(CHANNEL_TYPE.CAL)));
//			aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.QUAT_MADGE_9DOF_Y,
//					new ChannelDetails(
//							Configuration.Shimmer3.ObjectClusterSensorName.QUAT_MADGE_9DOF_Y,
//							Configuration.Shimmer3.ObjectClusterSensorName.QUAT_MADGE_9DOF_Y,
//							DatabaseChannelHandles.QUARTENION_Y_9DOF,
//							CHANNEL_UNITS.NO_UNITS,
//							Arrays.asList(CHANNEL_TYPE.CAL)));
//			aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.QUAT_MADGE_9DOF_Z,
//					new ChannelDetails(
//							Configuration.Shimmer3.ObjectClusterSensorName.QUAT_MADGE_9DOF_Z,
//							Configuration.Shimmer3.ObjectClusterSensorName.QUAT_MADGE_9DOF_Z,
//							DatabaseChannelHandles.QUARTENION_Z_9DOF,
//							CHANNEL_UNITS.NO_UNITS,
//							Arrays.asList(CHANNEL_TYPE.CAL)));
//						
//			aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.EULER_6DOF_A,
//					new ChannelDetails(
//							Configuration.Shimmer3.ObjectClusterSensorName.EULER_6DOF_A,
//							Configuration.Shimmer3.ObjectClusterSensorName.EULER_6DOF_A,
//							DatabaseChannelHandles.EULER_6DOF_A,
//							CHANNEL_UNITS.NO_UNITS,
//							Arrays.asList(CHANNEL_TYPE.CAL)));
//			aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.EULER_6DOF_X,
//					new ChannelDetails(
//							Configuration.Shimmer3.ObjectClusterSensorName.EULER_6DOF_X,
//							Configuration.Shimmer3.ObjectClusterSensorName.EULER_6DOF_X,
//							DatabaseChannelHandles.EULER_6DOF_X,
//							CHANNEL_UNITS.NO_UNITS,
//							Arrays.asList(CHANNEL_TYPE.CAL)));
//			aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.EULER_6DOF_Y,
//					new ChannelDetails(
//							Configuration.Shimmer3.ObjectClusterSensorName.EULER_6DOF_Y,
//							Configuration.Shimmer3.ObjectClusterSensorName.EULER_6DOF_Y,
//							DatabaseChannelHandles.EULER_6DOF_Y,
//							CHANNEL_UNITS.NO_UNITS,
//							Arrays.asList(CHANNEL_TYPE.CAL)));
//			aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.EULER_6DOF_Z,
//					new ChannelDetails(
//							Configuration.Shimmer3.ObjectClusterSensorName.EULER_6DOF_Z,
//							Configuration.Shimmer3.ObjectClusterSensorName.EULER_6DOF_Z,
//							DatabaseChannelHandles.EULER_6DOF_Z,
//							CHANNEL_UNITS.NO_UNITS,
//							Arrays.asList(CHANNEL_TYPE.CAL)));
//			aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.EULER_9DOF_A,
//					new ChannelDetails(
//							Configuration.Shimmer3.ObjectClusterSensorName.EULER_9DOF_A,
//							Configuration.Shimmer3.ObjectClusterSensorName.EULER_9DOF_A,
//							DatabaseChannelHandles.EULER_9DOF_A,
//							CHANNEL_UNITS.NO_UNITS,
//							Arrays.asList(CHANNEL_TYPE.CAL)));
//			aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.EULER_9DOF_X,
//					new ChannelDetails(
//							Configuration.Shimmer3.ObjectClusterSensorName.EULER_9DOF_X,
//							Configuration.Shimmer3.ObjectClusterSensorName.EULER_9DOF_X,
//							DatabaseChannelHandles.EULER_9DOF_X,
//							CHANNEL_UNITS.NO_UNITS,
//							Arrays.asList(CHANNEL_TYPE.CAL)));
//			aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.EULER_9DOF_Y,
//					new ChannelDetails(
//							Configuration.Shimmer3.ObjectClusterSensorName.EULER_9DOF_Y,
//							Configuration.Shimmer3.ObjectClusterSensorName.EULER_9DOF_Y,
//							DatabaseChannelHandles.EULER_9DOF_Y,
//							CHANNEL_UNITS.NO_UNITS,
//							Arrays.asList(CHANNEL_TYPE.CAL)));
//			aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.EULER_9DOF_Z,
//					new ChannelDetails(
//							Configuration.Shimmer3.ObjectClusterSensorName.EULER_9DOF_Z,
//							Configuration.Shimmer3.ObjectClusterSensorName.EULER_9DOF_Z,
//							DatabaseChannelHandles.EULER_9DOF_Z,
//							CHANNEL_UNITS.NO_UNITS,
//							Arrays.asList(CHANNEL_TYPE.CAL)));

			mChannelMapRef = Collections.unmodifiableMap(aMap);
	    }


	    public static final Map<Integer, SensorGroupingDetails> mSensorGroupingMapRef;
	    static {
	        Map<Integer, SensorGroupingDetails> aMap = new TreeMap<Integer, SensorGroupingDetails>();
		
			//Sensor Grouping for Configuration Panel 'tile' generation. 
	      //XXX-RS-AA-SensorClass?
			aMap.put(Configuration.Shimmer3.GuiLabelSensorTiles.LOW_NOISE_ACCEL.ordinal(), new SensorGroupingDetails(
					SensorKionixKXRB52042.GuiLabelSensorTiles.LOW_NOISE_ACCEL,
					Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_ANALOG_ACCEL)));
			aMap.put(Configuration.Shimmer3.GuiLabelSensorTiles.WIDE_RANGE_ACCEL.ordinal(), new SensorGroupingDetails(//XXX-RS-LSM-SensorClass?
					SensorLSM303.GuiLabelSensorTiles.WIDE_RANGE_ACCEL,
					Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_LSM303DLHC_ACCEL)));
			aMap.put(Configuration.Shimmer3.GuiLabelSensorTiles.GYRO.ordinal(), new SensorGroupingDetails(
					Configuration.Shimmer3.GuiLabelSensorTiles.GYRO.tileText,
					Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_GYRO)));
			aMap.put(Configuration.Shimmer3.GuiLabelSensorTiles.MAG.ordinal(), new SensorGroupingDetails(//XXX-RS-LSM-SensorClass? 
					SensorLSM303.GuiLabelSensorTiles.MAG,
					Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_LSM303DLHC_MAG)));
			
			aMap.put(Configuration.Shimmer3.GuiLabelSensorTiles.PRESSURE_TEMPERATURE.ordinal(), new SensorGroupingDetails(
					SensorBMP180.GuiLabelSensorTiles.PRESSURE_TEMPERATURE,
					Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_BMP180_PRESSURE)));
//			aMap.putAll(SensorBMP180.mSensorGroupingMap);
			
			aMap.put(Configuration.Shimmer3.GuiLabelSensorTiles.BATTERY_MONITORING.ordinal(), new SensorGroupingDetails(
					Configuration.Shimmer3.GuiLabelSensorTiles.BATTERY_MONITORING.getTileText(),
					Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_VBATT)));
			aMap.put(Configuration.Shimmer3.GuiLabelSensorTiles.EXTERNAL_EXPANSION_ADC.ordinal(), new SensorGroupingDetails(
					SensorADC.GuiLabelSensorTiles.EXTERNAL_EXPANSION_ADC,
					Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_EXT_EXP_ADC_A6,
								Configuration.Shimmer3.SensorMapKey.SHIMMER_EXT_EXP_ADC_A7,
								Configuration.Shimmer3.SensorMapKey.SHIMMER_EXT_EXP_ADC_A15)));
			aMap.put(Configuration.Shimmer3.GuiLabelSensorTiles.GSR.ordinal(), new SensorGroupingDetails(
					SensorGSR.GuiLabelSensorTiles.GSR,
					Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_GSR,
								Configuration.Shimmer3.SensorMapKey.HOST_PPG_DUMMY)));
	//							Configuration.Shimmer3.SensorMapKey.PPG_A12,
	//							Configuration.Shimmer3.SensorMapKey.PPG_A13)));
			aMap.put(Configuration.Shimmer3.GuiLabelSensorTiles.EXG.ordinal(), new SensorGroupingDetails(
					SensorEXG.GuiLabelSensorTiles.EXG,
					Arrays.asList(Configuration.Shimmer3.SensorMapKey.HOST_ECG,
								Configuration.Shimmer3.SensorMapKey.HOST_EMG,
								Configuration.Shimmer3.SensorMapKey.HOST_EXG_TEST,
								Configuration.Shimmer3.SensorMapKey.HOST_EXG_CUSTOM,
								Configuration.Shimmer3.SensorMapKey.HOST_EXG_RESPIRATION)));
			aMap.put(Configuration.Shimmer3.GuiLabelSensorTiles.PROTO3_MINI.ordinal(), new SensorGroupingDetails(
					SensorADC.GuiLabelSensorTiles.PROTO3_MINI,
					Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A1,
								Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A12,
								Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A13,
								Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A14)));
			aMap.put(Configuration.Shimmer3.GuiLabelSensorTiles.PROTO3_DELUXE.ordinal(), new SensorGroupingDetails(
					SensorADC.GuiLabelSensorTiles.PROTO3_DELUXE,
					Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A1,
								Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A12,
								Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A13,
								Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A14)));
			aMap.put(Configuration.Shimmer3.GuiLabelSensorTiles.PROTO3_DELUXE_SUPP.ordinal(), new SensorGroupingDetails(
					Configuration.Shimmer3.GuiLabelSensorTiles.PROTO3_DELUXE_SUPP.getTileText(),
					Arrays.asList(Configuration.Shimmer3.SensorMapKey.HOST_PPG1_DUMMY,
								Configuration.Shimmer3.SensorMapKey.HOST_PPG2_DUMMY)));
			aMap.put(Configuration.Shimmer3.GuiLabelSensorTiles.BRIDGE_AMPLIFIER.ordinal(), new SensorGroupingDetails(
					Configuration.Shimmer3.GuiLabelSensorTiles.BRIDGE_AMPLIFIER.getTileText(),
					Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_BRIDGE_AMP,
								Configuration.Shimmer3.SensorMapKey.SHIMMER_RESISTANCE_AMP)));
			aMap.put(Configuration.Shimmer3.GuiLabelSensorTiles.BRIDGE_AMPLIFIER_SUPP.ordinal(), new SensorGroupingDetails(
					Configuration.Shimmer3.GuiLabelSensorTiles.BRIDGE_AMPLIFIER_SUPP.getTileText(),
					Arrays.asList(Configuration.Shimmer3.SensorMapKey.HOST_SKIN_TEMPERATURE_PROBE)));
			aMap.put(Configuration.Shimmer3.GuiLabelSensorTiles.HIGH_G_ACCEL.ordinal(), new SensorGroupingDetails(
					Configuration.Shimmer3.GuiLabelSensorTiles.HIGH_G_ACCEL.getTileText(),
					Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A12, //X-axis
								Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A13, //Y-axis
								Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A14, //Z-axis
								Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A1))); //unused but accessible
			aMap.put(Configuration.Shimmer3.GuiLabelSensorTiles.INTERNAL_EXPANSION_ADC.ordinal(), new SensorGroupingDetails(
					SensorADC.GuiLabelSensorTiles.INTERNAL_EXPANSION_ADC,
					Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A1,
								Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A12,
								Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A13,
								Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A14)));
			aMap.put(Configuration.Shimmer3.GuiLabelSensorTiles.MPU_ACCEL_GYRO_MAG.ordinal(), new SensorGroupingDetails(
					SensorMPU9X50.GuiLabelSensorTiles.MPU_ACCEL_GYRO_MAG,
					Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_MPL_ACCEL,   // RMC 22/04/2016: MPU added to sensor class, check if can delete
								Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_MPL_GYRO,
								Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_MPL_MAG)));
			aMap.put(Configuration.Shimmer3.GuiLabelSensorTiles.MPU_OTHER.ordinal(), new SensorGroupingDetails(
					SensorMPU9X50.GuiLabelSensorTiles.MPU_OTHER,
					Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_TEMP,
								Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_MPL_QUAT_6DOF)));
			
			//Not implemented: GUI_LABEL_CHANNEL_GROUPING_GPS
			
			// ChannelTiles that have compatibility considerations (used to auto generate tiles in GUI)
			aMap.get(Configuration.Shimmer3.GuiLabelSensorTiles.LOW_NOISE_ACCEL.ordinal()).mListOfCompatibleVersionInfo = CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW;
			aMap.get(Configuration.Shimmer3.GuiLabelSensorTiles.GYRO.ordinal()).mListOfCompatibleVersionInfo = CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW;
			aMap.get(Configuration.Shimmer3.GuiLabelSensorTiles.MAG.ordinal()).mListOfCompatibleVersionInfo = CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW;//XXX-RS-LSM-SensorClass? 
			aMap.get(Configuration.Shimmer3.GuiLabelSensorTiles.BATTERY_MONITORING.ordinal()).mListOfCompatibleVersionInfo = CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardAndFw;
			aMap.get(Configuration.Shimmer3.GuiLabelSensorTiles.WIDE_RANGE_ACCEL.ordinal()).mListOfCompatibleVersionInfo = CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardAndFw;//XXX-RS-LSM-SensorClass?
			aMap.get(Configuration.Shimmer3.GuiLabelSensorTiles.PRESSURE_TEMPERATURE.ordinal()).mListOfCompatibleVersionInfo = CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW;
			aMap.get(Configuration.Shimmer3.GuiLabelSensorTiles.EXTERNAL_EXPANSION_ADC.ordinal()).mListOfCompatibleVersionInfo = CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW;
			aMap.get(Configuration.Shimmer3.GuiLabelSensorTiles.INTERNAL_EXPANSION_ADC.ordinal()).mListOfCompatibleVersionInfo = CompatibilityInfoForMaps.listOfCompatibleVersionInfoIntAdcsGeneral;
			aMap.get(Configuration.Shimmer3.GuiLabelSensorTiles.GSR.ordinal()).mListOfCompatibleVersionInfo = CompatibilityInfoForMaps.listOfCompatibleVersionInfoGsr;
			aMap.get(Configuration.Shimmer3.GuiLabelSensorTiles.EXG.ordinal()).mListOfCompatibleVersionInfo = CompatibilityInfoForMaps.listOfCompatibleVersionInfoExg;
			aMap.get(Configuration.Shimmer3.GuiLabelSensorTiles.PROTO3_MINI.ordinal()).mListOfCompatibleVersionInfo = CompatibilityInfoForMaps.listOfCompatibleVersionInfoProto3Mini;
			aMap.get(Configuration.Shimmer3.GuiLabelSensorTiles.PROTO3_DELUXE.ordinal()).mListOfCompatibleVersionInfo = CompatibilityInfoForMaps.listOfCompatibleVersionInfoProto3Deluxe;
			aMap.get(Configuration.Shimmer3.GuiLabelSensorTiles.PROTO3_DELUXE_SUPP.ordinal()).mListOfCompatibleVersionInfo = CompatibilityInfoForMaps.listOfCompatibleVersionInfoProto3Deluxe;
			aMap.get(Configuration.Shimmer3.GuiLabelSensorTiles.BRIDGE_AMPLIFIER.ordinal()).mListOfCompatibleVersionInfo = CompatibilityInfoForMaps.listOfCompatibleVersionInfoBrAmp;
			aMap.get(Configuration.Shimmer3.GuiLabelSensorTiles.BRIDGE_AMPLIFIER_SUPP.ordinal()).mListOfCompatibleVersionInfo = CompatibilityInfoForMaps.listOfCompatibleVersionInfoBrAmp;
			aMap.get(Configuration.Shimmer3.GuiLabelSensorTiles.HIGH_G_ACCEL.ordinal()).mListOfCompatibleVersionInfo = CompatibilityInfoForMaps.listOfCompatibleVersionInfoHighGAccel;
//			aMap.get(Configuration.Shimmer3.GuiLabelSensorTiles.GPS.ordinal()).mListOfCompatibleVersionInfo = CompatibilityInfoForMaps.listOfCompatibleVersionInfoGps;
			aMap.get(Configuration.Shimmer3.GuiLabelSensorTiles.MPU_ACCEL_GYRO_MAG.ordinal()).mListOfCompatibleVersionInfo = CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors;
			aMap.get(Configuration.Shimmer3.GuiLabelSensorTiles.MPU_OTHER.ordinal()).mListOfCompatibleVersionInfo = CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors;
			
			// For loop to automatically inherit associated channel configuration options from mSensorMap in the aMap
			for (SensorGroupingDetails sensorGroup:aMap.values()) {
//				 Ok to clear here because variable is initiated in the class
//				sensorGroup.mListOfConfigOptionKeysAssociated.clear();
//				List<ShimmerVerObject> listOfCompatibleVersionInfo = new ArrayList<ShimmerVerObject>();
				for (Integer sensor:sensorGroup.mListOfSensorMapKeysAssociated) {
					SensorDetailsRef sensorDetails = Configuration.Shimmer3.mSensorMapRef.get(sensor);
					if(sensorDetails!=null){
						List<String> associatedConfigOptions = sensorDetails.mListOfConfigOptionKeysAssociated;
						if (associatedConfigOptions != null) {
							for (String configOption : associatedConfigOptions) {
								// do not add duplicates
								if (!(sensorGroup.mListOfConfigOptionKeysAssociated.contains(configOption))) {
									sensorGroup.mListOfConfigOptionKeysAssociated.add(configOption);
								}
							}
						}
//						if(sensorDetails.mListOfCompatibleVersionInfo!=null){
//							listOfCompatibleVersionInfo.addAll(sensorDetails.mListOfCompatibleVersionInfo);
//						}
					}
				}
//				sensorGroup.mListOfCompatibleVersionInfo = listOfCompatibleVersionInfo;
			}
			
//			aMap.get(Configuration.Shimmer3.GuiLabelSensorTiles.GSR.ordinal()).mListOfConfigOptionKeysAssociated.add(Configuration.Shimmer3.GuiLabelConfig.PPG_ADC_SELECTION);
			aMap.get(Configuration.Shimmer3.GuiLabelSensorTiles.PROTO3_DELUXE_SUPP.ordinal()).mListOfConfigOptionKeysAssociated.add(SensorPPG.GuiLabelConfig.PPG1_ADC_SELECTION);
			aMap.get(Configuration.Shimmer3.GuiLabelSensorTiles.PROTO3_DELUXE_SUPP.ordinal()).mListOfConfigOptionKeysAssociated.add(SensorPPG.GuiLabelConfig.PPG2_ADC_SELECTION);
			
	        mSensorGroupingMapRef = Collections.unmodifiableMap(aMap);
	    }

	    public static final Map<String, ConfigOptionDetailsSensor> mConfigOptionsMapRef;
	    static {
	        Map<String, ConfigOptionDetailsSensor> aMap = new LinkedHashMap<String, ConfigOptionDetailsSensor>();
	        
			// Assemble the channel configuration options map
			aMap.put(Configuration.Shimmer3.GuiLabelConfig.SHIMMER_USER_ASSIGNED_NAME, 
					new ConfigOptionDetailsSensor(ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.TEXTFIELD));
//			aMap.put(Configuration.Shimmer3.GuiLabelConfig.SHIMMER_USER_ASSIGNED_NAME, 
//					new ConfigOptionDetailsSensor(ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.TEXTFIELD,
//											CompatibilityInfoForMaps.listOfCompatibleVersionInfoLogging));
			aMap.put(Configuration.Shimmer3.GuiLabelConfig.SHIMMER_SAMPLING_RATE, 
					new ConfigOptionDetailsSensor(ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.TEXTFIELD));
			aMap.put(Configuration.Shimmer3.GuiLabelConfig.SHIMMER_MAC_FROM_INFOMEM, 
					new ConfigOptionDetailsSensor(ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.TEXTFIELD));
			aMap.put(Configuration.Shimmer3.GuiLabelConfig.BUFFER_SIZE, 
					new ConfigOptionDetailsSensor(ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.TEXTFIELD));
//			aMap.put(Configuration.Shimmer3.GuiLabelConfig.CONFIG_TIME, 
//					new ConfigOptionDetailsSensor(ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.TEXTFIELD,
//											CompatibilityInfoForMaps.listOfCompatibleVersionInfoLogging));
			aMap.put(Configuration.Shimmer3.GuiLabelConfig.CONFIG_TIME, 
					new ConfigOptionDetailsSensor(ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.TEXTFIELD));
//			aMap.put(Configuration.Shimmer3.GuiLabelConfig.TRIAL_NAME, 
//					new ConfigOptionDetailsSensor(ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.TEXTFIELD,
//											CompatibilityInfoForMaps.listOfCompatibleVersionInfoLogging));
			aMap.put(Configuration.Shimmer3.GuiLabelConfig.TRIAL_NAME, 
					new ConfigOptionDetailsSensor(ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.TEXTFIELD));
			aMap.put(Configuration.Shimmer3.GuiLabelConfig.EXPERIMENT_ID, 
					new ConfigOptionDetailsSensor(ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.TEXTFIELD,
											CompatibilityInfoForMaps.listOfCompatibleVersionInfoLogging));

			aMap.put(Configuration.Shimmer3.GuiLabelConfig.EXPERIMENT_NUMBER_OF_SHIMMERS, 
					new ConfigOptionDetailsSensor(ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.TEXTFIELD,
											CompatibilityInfoForMaps.listOfCompatibleVersionInfoSdLog));
			aMap.put(Configuration.Shimmer3.GuiLabelConfig.EXPERIMENT_DURATION_ESTIMATED, 
					new ConfigOptionDetailsSensor(ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.TEXTFIELD,
											CompatibilityInfoForMaps.listOfCompatibleVersionInfoSdLog));
			aMap.put(Configuration.Shimmer3.GuiLabelConfig.EXPERIMENT_DURATION_MAXIMUM, 
					new ConfigOptionDetailsSensor(ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.TEXTFIELD,
											CompatibilityInfoForMaps.listOfCompatibleVersionInfoSdLog));
			aMap.put(Configuration.Shimmer3.GuiLabelConfig.BROADCAST_INTERVAL, 
					new ConfigOptionDetailsSensor(ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.TEXTFIELD,
											new ArrayList(){}));
			
			aMap.put(Configuration.Shimmer3.GuiLabelConfig.BLUETOOTH_BAUD_RATE, 
					new ConfigOptionDetailsSensor(Configuration.Shimmer3.ListofBluetoothBaudRates, 
											Configuration.Shimmer3.ListofBluetoothBaudRatesConfigValues, 
											ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX,
											CompatibilityInfoForMaps.listOfCompatibleVersionInfoStreaming));
			
			//XXX-RS-LSM-SensorClass? 
//			aMap.put(SensorLSM303.GuiLabelConfig.LSM303DLHC_ACCEL_RANGE, 
//					new ConfigOptionDetailsSensor(SensorLSM303.ListofAccelRange, 
//							SensorLSM303.ListofLSM303DLHCAccelRangeConfigValues, 
//											ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX));
			aMap.put(SensorLSM303.GuiLabelConfig.LSM303DLHC_ACCEL_RANGE,SensorLSM303.configOptionAccelRange);
			aMap.put(SensorLSM303.GuiLabelConfig.LSM303DLHC_ACCEL_RATE,SensorLSM303.configOptionAccelRate);
//			aMap.put(SensorLSM303.GuiLabelConfig.LSM303DLHC_ACCEL_RATE, 
//					new ConfigOptionDetailsSensor(SensorLSM303.ListofLSM303DLHCAccelRate, 
//							SensorLSM303.ListofLSM303DLHCAccelRateConfigValues, 
//											ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX));
			//XXX-RS-LSM-SensorClass? 
//			aMap.get(SensorLSM303.GuiLabelConfig.LSM303DLHC_ACCEL_RATE).setGuiValues(ConfigOptionDetailsSensor.VALUE_INDEXES.LSM303DLHC_ACCEL_RATE.IS_LPM, SensorLSM303.ListofLSM303DLHCAccelRateLpm);
//			//XXX-RS-LSM-SensorClass? 
//			aMap.get(SensorLSM303.GuiLabelConfig.LSM303DLHC_ACCEL_RATE).setConfigValues(ConfigOptionDetailsSensor.VALUE_INDEXES.LSM303DLHC_ACCEL_RATE.IS_LPM, SensorLSM303.ListofLSM303DLHCAccelRateLpmConfigValues);

			//XXX-RS-LSM-SensorClass? 
//			if(mLowPowerAccelWR) {
//				aMap.put(Configuration.Shimmer3.GuiLabelConfig.LSM303DLHC_ACCEL_RATE, 
//						new ConfigOptionDetailsSensor(Configuration.Shimmer3.ListofLSM303DLHCAccelRateLpm, 
//												Configuration.Shimmer3.ListofLSM303DLHCAccelRateLpmConfigValues, 
//												ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX));
//			}
//			else {
//				aMap.put(Configuration.Shimmer3.GuiLabelConfig.LSM303DLHC_ACCEL_RATE, 
//						new ConfigOptionDetailsSensor(Configuration.Shimmer3.ListofLSM303DLHCAccelRate, 
//												Configuration.Shimmer3.ListofLSM303DLHCAccelRateConfigValues, 
//												ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX));
//			}
			
//			aMap.put(Configuration.Shimmer3.GuiLabelConfig.MPU9150_GYRO_RANGE, 
//					new ConfigOptionDetailsSensor(Configuration.Shimmer3.ListofGyroRange, 
//											Configuration.Shimmer3.ListofMPU9150GyroRangeConfigValues, 
//											ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX,
//											CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW));
			aMap.put(SensorMPU9X50.GuiLabelConfig.MPU9150_GYRO_RANGE, SensorMPU9X50.configOptionMpu9150GyroRange);
			//XXX-RS-LSM-SensorClass? 
//			aMap.put(SensorLSM303.GuiLabelConfig.LSM303DLHC_MAG_RANGE, 
//					new ConfigOptionDetailsSensor(SensorLSM303.ListofMagRange, 
//							SensorLSM303.ListofMagRangeConfigValues, 
//											ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX,
//											CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW));
			//XXX-RS-LSM-SensorClass? 
//			aMap.put(SensorLSM303.GuiLabelConfig.LSM303DLHC_MAG_RATE, 
//					new ConfigOptionDetailsSensor(SensorLSM303.ListofLSM303DLHCMagRate, 
//							SensorLSM303.ListofLSM303DLHCMagRateConfigValues, 
//											ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX,
//											CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW));
			aMap.put(SensorLSM303.GuiLabelConfig.LSM303DLHC_MAG_RANGE, SensorLSM303.configOptionMagRange);
			aMap.put(SensorLSM303.GuiLabelConfig.LSM303DLHC_MAG_RATE, SensorLSM303.configOptionMagRate);
			aMap.put(SensorBMP180.GuiLabelConfig.PRESSURE_RESOLUTION, SensorBMP180.configOptionPressureResolution);
//			aMap.put(Configuration.Shimmer3.GuiLabelConfig.PRESSURE_RESOLUTION, 
//					new ConfigOptionDetailsSensor(Configuration.Shimmer3.ListofPressureResolution, 
//											Configuration.Shimmer3.ListofPressureResolutionConfigValues, 
//											ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX,
//											CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW));

//			aMap.put(SensorGSR.GuiLabelConfig.GSR_RANGE, 
//					new ConfigOptionDetailsSensor(Configuration.Shimmer3.ListofGSRRange, 
//											Configuration.Shimmer3.ListofGSRRangeConfigValues, 
//											ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX,
//											CompatibilityInfoForMaps.listOfCompatibleVersionInfoGsr));
			aMap.put(SensorGSR.GuiLabelConfig.GSR_RANGE, SensorGSR.configOptionGsrRange);
			aMap.put(Configuration.Shimmer3.GuiLabelConfig.EXG_GAIN, 
					new ConfigOptionDetailsSensor(Configuration.Shimmer3.ListOfExGGain, 
											Configuration.Shimmer3.ListOfExGGainConfigValues, 
											ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX,
											CompatibilityInfoForMaps.listOfCompatibleVersionInfoExg));
			aMap.put(Configuration.Shimmer3.GuiLabelConfig.EXG_RESOLUTION, 
					new ConfigOptionDetailsSensor(Configuration.Shimmer3.ListOfExGResolutions, 
											Configuration.Shimmer3.ListOfExGResolutionsConfigValues, 
											ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX,
											CompatibilityInfoForMaps.listOfCompatibleVersionInfoExg));

			//Advanced ExG		
			aMap.put(Configuration.Shimmer3.GuiLabelConfig.EXG_REFERENCE_ELECTRODE, 
					new ConfigOptionDetailsSensor(Configuration.Shimmer3.ListOfECGReferenceElectrode, 
											Configuration.Shimmer3.ListOfECGReferenceElectrodeConfigValues, 
											ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX,
											CompatibilityInfoForMaps.listOfCompatibleVersionInfoExg));
			aMap.get(Configuration.Shimmer3.GuiLabelConfig.EXG_REFERENCE_ELECTRODE).setGuiValues(ConfigOptionDetailsSensor.VALUE_INDEXES.EXG_REFERENCE_ELECTRODE.EMG, Configuration.Shimmer3.ListOfEMGReferenceElectrode);
			aMap.get(Configuration.Shimmer3.GuiLabelConfig.EXG_REFERENCE_ELECTRODE).setConfigValues(ConfigOptionDetailsSensor.VALUE_INDEXES.EXG_REFERENCE_ELECTRODE.EMG, Configuration.Shimmer3.ListOfEMGReferenceElectrodeConfigValues);
			aMap.get(Configuration.Shimmer3.GuiLabelConfig.EXG_REFERENCE_ELECTRODE).setGuiValues(ConfigOptionDetailsSensor.VALUE_INDEXES.EXG_REFERENCE_ELECTRODE.CUSTOM, Configuration.Shimmer3.ListOfExGReferenceElectrodeAll);
			aMap.get(Configuration.Shimmer3.GuiLabelConfig.EXG_REFERENCE_ELECTRODE).setConfigValues(ConfigOptionDetailsSensor.VALUE_INDEXES.EXG_REFERENCE_ELECTRODE.CUSTOM, Configuration.Shimmer3.ListOfExGReferenceElectrodeConfigValuesAll);
			aMap.get(Configuration.Shimmer3.GuiLabelConfig.EXG_REFERENCE_ELECTRODE).setGuiValues(ConfigOptionDetailsSensor.VALUE_INDEXES.EXG_REFERENCE_ELECTRODE.RESP, Configuration.Shimmer3.ListOfRespReferenceElectrode);
			aMap.get(Configuration.Shimmer3.GuiLabelConfig.EXG_REFERENCE_ELECTRODE).setConfigValues(ConfigOptionDetailsSensor.VALUE_INDEXES.EXG_REFERENCE_ELECTRODE.RESP, Configuration.Shimmer3.ListOfRespReferenceElectrodeConfigValues);
			aMap.get(Configuration.Shimmer3.GuiLabelConfig.EXG_REFERENCE_ELECTRODE).setGuiValues(ConfigOptionDetailsSensor.VALUE_INDEXES.EXG_REFERENCE_ELECTRODE.TEST, Configuration.Shimmer3.ListOfTestReferenceElectrode);
			aMap.get(Configuration.Shimmer3.GuiLabelConfig.EXG_REFERENCE_ELECTRODE).setConfigValues(ConfigOptionDetailsSensor.VALUE_INDEXES.EXG_REFERENCE_ELECTRODE.TEST, Configuration.Shimmer3.ListOfTestReferenceElectrodeConfigValues);
			
			aMap.put(Configuration.Shimmer3.GuiLabelConfig.EXG_BYTES, 
					new ConfigOptionDetailsSensor(ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.JPANEL,
											CompatibilityInfoForMaps.listOfCompatibleVersionInfoExg));
			
			aMap.put(Configuration.Shimmer3.GuiLabelConfig.EXG_RATE, 
					new ConfigOptionDetailsSensor(Configuration.Shimmer3.ListOfExGRate, 
											Configuration.Shimmer3.ListOfExGRateConfigValues, 
											ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX,
											CompatibilityInfoForMaps.listOfCompatibleVersionInfoExg));
			aMap.put(Configuration.Shimmer3.GuiLabelConfig.EXG_LEAD_OFF_DETECTION, 
					new ConfigOptionDetailsSensor(Configuration.Shimmer3.ListOfExGLeadOffDetection, 
											Configuration.Shimmer3.ListOfExGLeadOffDetectionConfigValues, 
											ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX,
											CompatibilityInfoForMaps.listOfCompatibleVersionInfoExg));
			aMap.put(Configuration.Shimmer3.GuiLabelConfig.EXG_LEAD_OFF_CURRENT, 
					new ConfigOptionDetailsSensor(Configuration.Shimmer3.ListOfExGLeadOffCurrent, 
											Configuration.Shimmer3.ListOfExGLeadOffCurrentConfigValues, 
											ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX,
											CompatibilityInfoForMaps.listOfCompatibleVersionInfoExg));
			aMap.put(Configuration.Shimmer3.GuiLabelConfig.EXG_LEAD_OFF_COMPARATOR, 
					new ConfigOptionDetailsSensor(Configuration.Shimmer3.ListOfExGLeadOffComparator, 
											Configuration.Shimmer3.ListOfExGLeadOffComparatorConfigValues, 
											ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX,
											CompatibilityInfoForMaps.listOfCompatibleVersionInfoExg));
			aMap.put(Configuration.Shimmer3.GuiLabelConfig.EXG_RESPIRATION_DETECT_FREQ, 
					new ConfigOptionDetailsSensor(Configuration.Shimmer3.ListOfExGRespirationDetectFreq, 
											Configuration.Shimmer3.ListOfExGRespirationDetectFreqConfigValues, 
											ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX,
											CompatibilityInfoForMaps.listOfCompatibleVersionInfoRespiration));
			aMap.put(Configuration.Shimmer3.GuiLabelConfig.EXG_RESPIRATION_DETECT_PHASE, 
					new ConfigOptionDetailsSensor(Configuration.Shimmer3.ListOfExGRespirationDetectPhase32khz, 
											Configuration.Shimmer3.ListOfExGRespirationDetectPhase32khzConfigValues, 
											ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX,
											CompatibilityInfoForMaps.listOfCompatibleVersionInfoRespiration));
			aMap.get(Configuration.Shimmer3.GuiLabelConfig.EXG_RESPIRATION_DETECT_PHASE).setGuiValues(ConfigOptionDetailsSensor.VALUE_INDEXES.EXG_RESPIRATION_DETECT_PHASE.PHASE_64KHZ, Configuration.Shimmer3.ListOfExGRespirationDetectPhase64khz);
			aMap.get(Configuration.Shimmer3.GuiLabelConfig.EXG_RESPIRATION_DETECT_PHASE).setConfigValues(ConfigOptionDetailsSensor.VALUE_INDEXES.EXG_RESPIRATION_DETECT_PHASE.PHASE_64KHZ, Configuration.Shimmer3.ListOfExGRespirationDetectPhase64khzConfigValues);
			
			//MPL Options
//			aMap.put(Configuration.Shimmer3.GuiLabelConfig.MPU9150_ACCEL_RANGE, 
//					new ConfigOptionDetailsSensor(Configuration.Shimmer3.ListofMPU9150AccelRange, 
//											Configuration.Shimmer3.ListofMPU9150AccelRangeConfigValues, 
//											ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX,
//											CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors));
			
			aMap.put(SensorMPU9X50.GuiLabelConfig.MPU9150_ACCEL_RANGE,SensorMPU9X50.configOptionMpu9150AccelRange);
			aMap.put(SensorMPU9X50.GuiLabelConfig.MPU9150_DMP_GYRO_CAL, SensorMPU9X50.configOptionMpu9150DmpGyroCal);
			aMap.put(SensorMPU9X50.GuiLabelConfig.MPU9150_MPL_LPF, SensorMPU9X50.configOptionMpu9150MplLpf);
			aMap.put(SensorMPU9X50.GuiLabelConfig.MPU9150_MPL_RATE, SensorMPU9X50.configOptionMpu9150MplRate);
			aMap.put(SensorMPU9X50.GuiLabelConfig.MPU9150_MAG_RATE, SensorMPU9X50.configOptionMpu9150MagRate);
//			aMap.put(Configuration.Shimmer3.GuiLabelConfig.MPU9150_DMP_GYRO_CAL, 
//					new ConfigOptionDetailsSensor(Configuration.Shimmer3.ListofMPU9150MplCalibrationOptions, 
//											Configuration.Shimmer3.ListofMPU9150MplCalibrationOptionsConfigValues, 
//											ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX,
//											CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors));
//			aMap.put(Configuration.Shimmer3.GuiLabelConfig.MPU9150_MPL_LPF, 
//					new ConfigOptionDetailsSensor(Configuration.Shimmer3.ListofMPU9150MplLpfOptions, 
//											Configuration.Shimmer3.ListofMPU9150MplLpfOptionsConfigValues, 
//											ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX,
//											CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors));
//			aMap.put(Configuration.Shimmer3.GuiLabelConfig.MPU9150_MPL_RATE, 
//					new ConfigOptionDetailsSensor(Configuration.Shimmer3.ListofMPU9150MplRate, 
//											Configuration.Shimmer3.ListofMPU9150MplRateConfigValues, 
//											ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX,
//											CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors));
//			aMap.put(Configuration.Shimmer3.GuiLabelConfig.MPU9150_MAG_RATE, 
//					new ConfigOptionDetailsSensor(Configuration.Shimmer3.ListofMPU9150MagRate, 
//											Configuration.Shimmer3.ListofMPU9150MagRateConfigValues, 
//											ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX,
//											CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors));

		    aMap.put(Configuration.Shimmer3.GuiLabelConfig.INT_EXP_BRD_POWER_INTEGER, 
		    	      new ConfigOptionDetailsSensor(Configuration.Shimmer3.ListOfOnOff, 
				    	        Configuration.Shimmer3.ListOfOnOffConfigValues,
				    	        ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX));
			
			//MPL CheckBoxes
			aMap.put(SensorMPU9X50.GuiLabelConfig.MPU9150_DMP, SensorMPU9X50.configOptionMpu9150Dmp); 
//					new ConfigOptionDetailsSensor(ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.CHECKBOX,CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors));
			aMap.put(SensorMPU9X50.GuiLabelConfig.MPU9150_MPL, SensorMPU9X50.configOptionMpu9150Mpl);
//					new ConfigOptionDetailsSensor(ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.CHECKBOX,CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors));
			aMap.put(SensorMPU9X50.GuiLabelConfig.MPU9150_MPL_9DOF_SENSOR_FUSION, SensorMPU9X50.configOptionMpu9150Mpl9DofSensorFusion); 
//					new ConfigOptionDetailsSensor(ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.CHECKBOX,CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors));
			aMap.put(SensorMPU9X50.GuiLabelConfig.MPU9150_MPL_GYRO_CAL, SensorMPU9X50.configOptionMpu9150MplGyroCal);
//					new ConfigOptionDetailsSensor(ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.CHECKBOX,CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors));
			aMap.put(SensorMPU9X50.GuiLabelConfig.MPU9150_MPL_VECTOR_CAL, SensorMPU9X50.configOptionMpu9150MplVectorCal); 
//					new ConfigOptionDetailsSensor(ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.CHECKBOX,CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors));
			aMap.put(SensorMPU9X50.GuiLabelConfig.MPU9150_MPL_MAG_CAL, SensorMPU9X50.configOptionMpu9150MplMagCal);
//					new ConfigOptionDetailsSensor(ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.CHECKBOX,CompatibilityInfoForMaps.listOfCompatibleVersionInfoMPLSensors));
			
			//General Config
			aMap.put(SensorMPU9X50.GuiLabelConfig.MPU9150_GYRO_RATE, SensorMPU9X50.configOptionMpu9150GyroRate); 
//					new ConfigOptionDetailsSensor(ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.TEXTFIELD,CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW));
			
			aMap.put(Configuration.Shimmer3.GuiLabelConfig.USER_BUTTON_START, 
					new ConfigOptionDetailsSensor(ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.CHECKBOX,CompatibilityInfoForMaps.listOfCompatibleVersionInfoLogging));
			aMap.put(Configuration.Shimmer3.GuiLabelConfig.SINGLE_TOUCH_START, 
					new ConfigOptionDetailsSensor(ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.CHECKBOX,CompatibilityInfoForMaps.listOfCompatibleVersionInfoSdLog));
			aMap.put(Configuration.Shimmer3.GuiLabelConfig.EXPERIMENT_MASTER_SHIMMER, 
					new ConfigOptionDetailsSensor(ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.CHECKBOX,CompatibilityInfoForMaps.listOfCompatibleVersionInfoSdLog));
			aMap.put(Configuration.Shimmer3.GuiLabelConfig.EXPERIMENT_SYNC_WHEN_LOGGING, 
					new ConfigOptionDetailsSensor(ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.CHECKBOX,CompatibilityInfoForMaps.listOfCompatibleVersionInfoSdLog));

			aMap.put(Configuration.Shimmer3.GuiLabelConfig.KINEMATIC_LPM, 
					new ConfigOptionDetailsSensor(ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.CHECKBOX,CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW));
			//XXX-RS-LSM-SensorClass? 
			aMap.put(SensorLSM303.GuiLabelConfig.LSM303DLHC_ACCEL_LPM,SensorLSM303.configOptionAccelLpm); 
//					new ConfigOptionDetailsSensor(ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.CHECKBOX));
			aMap.put(SensorMPU9X50.GuiLabelConfig.MPU9150_GYRO_LPM, SensorMPU9X50.configOptionMpu9150GyroLpm);
//					new ConfigOptionDetailsSensor(ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.CHECKBOX,CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW));
			//XXX-RS-LSM-SensorClass? 
			aMap.put(SensorLSM303.GuiLabelConfig.LSM303DLHC_MAG_LPM, 
					new ConfigOptionDetailsSensor(ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.CHECKBOX,CompatibilityInfoForMaps.listOfCompatibleVersionInfoAnyExpBoardStandardFW));
			aMap.put(Configuration.Shimmer3.GuiLabelConfig.TCX0, 
					new ConfigOptionDetailsSensor(ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.CHECKBOX,CompatibilityInfoForMaps.listOfCompatibleVersionInfoSdLog));
			aMap.put(Configuration.Shimmer3.GuiLabelConfig.INT_EXP_BRD_POWER_BOOLEAN, 
					new ConfigOptionDetailsSensor(ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.CHECKBOX));

			aMap.put(SensorPPG.GuiLabelConfig.PPG_ADC_SELECTION, SensorPPG.configOptionPpgAdcSelection);
			aMap.put(SensorPPG.GuiLabelConfig.PPG1_ADC_SELECTION, SensorPPG.configOptionPpg1AdcSelection);
			aMap.put(SensorPPG.GuiLabelConfig.PPG2_ADC_SELECTION, SensorPPG.configOptionPpg2AdcSelection);
	        
	        mConfigOptionsMapRef = Collections.unmodifiableMap(aMap);
	    }
	}
	
	public static class ShimmerGq802154{
		public class SensorMapKey{
			public static final int GSR = 1;
			public static final int ECGtoHR = 2;
		}
		
		//GUI AND EXPORT CHANNELS
		public static class ObjectClusterSensorName{
			//Related to the Spectrum Analyser
			public static String POWER_CALC_85 = "SpanManager-85";//"Results-85";
			public static String POWER_CALC_15 = "SpanManager-15";//"Results-15";
			public static String FREQUENCY = "Frequency";
			public static String POWER = "Power";
			
			public static final String RADIO_RECEPTION = "Radio_Reception";
		}
	}

	public static class ShimmerGqBle{
		public class SensorMapKey{
			public static final int MPU9150_ACCEL = 17; // SensorMPU9X50.SensorMapKey.MPU9150_ACCEL;
			public static final int MPU9150_GYRO =  1; // SensorMPU9X50.SensorMapKey.MPU9150_GYRO;
			public static final int MPU9150_MAG =  18; // SensorMPU9X50.SensorMapKey.MPU9150_MAG;
			public static final int VBATT = 10;
			public static final int LSM303DLHC_ACCEL = 11;  //XXX-RS-LSM-SensorClass?
			public static final int PPG = 107;
			public static final int GSR = 5;
			public static final int BEACON = 108;
		}

		// GUI Sensor Tiles
		public enum GuiLabelSensorTiles{
			GYRO(Configuration.ShimmerGqBle.GuiLabelSensors.GYRO),
			MAG(Configuration.ShimmerGqBle.GuiLabelSensors.MAG), //XXX-RS-LSM-SensorClass?
			BATTERY_MONITORING(Configuration.ShimmerGqBle.GuiLabelSensors.BATTERY),
			WIDE_RANGE_ACCEL(Configuration.ShimmerGqBle.GuiLabelSensors.ACCEL_WR),  //XXX-RS-LSM-SensorClass?
			GSR("GSR+"),
			BEACON(Configuration.ShimmerGqBle.GuiLabelSensors.BEACON);
			
			private String tileText = "";
			
			GuiLabelSensorTiles(String text){
				this.tileText = text;
			}
			
			public String getTileText(){
				return tileText;
			}
		}
		
		//GUI SENSORS
		public class GuiLabelSensors{
			public static final String BATTERY = SensorBattVoltage.GuiLabelSensors.BATTERY;
			public static final String GSR = SensorGSR.GuiLabelSensors.GSR;
			public static final String GYRO = SensorMPU9X50.GuiLabelSensors.GYRO;
			public static final String ACCEL_WR = SensorLSM303.GuiLabelSensors.ACCEL_WR;
			public static final String MAG = SensorLSM303.GuiLabelSensors.MAG;
			public static final String ACCEL_MPU = SensorMPU9X50.GuiLabelSensors.ACCEL_MPU;
			public static final String MAG_MPU = SensorMPU9X50.GuiLabelSensors.MAG_MPU;
			public static final String PPG_TO_HR = "PPG To HR";
			public static final String PPG = SensorPPG.GuiLabelSensors.PPG;
			public static final String BEACON = "Beacon";
		}
		
		public class GuiLabelConfig{
			public static final String SAMPLING_RATE_DIVIDER_VBATT = SensorBattVoltage.GuiLabelConfig.SAMPLING_RATE_DIVIDER_VBATT; //"VBATT Divider";
			public static final String SAMPLING_RATE_DIVIDER_GSR = SensorGSR.GuiLabelConfig.SAMPLING_RATE_DIVIDER_GSR; //"GSR Divider";
			public static final String SAMPLING_RATE_DIVIDER_PPG = SensorPPG.GuiLabelConfig.SAMPLING_RATE_DIVIDER_PPG; //"PPG Divider";
			public static final String SAMPLING_RATE_DIVIDER_LSM303DLHC_ACCEL = "LSM303DLHC Divider"; //XXX-RS-LSM-SensorClass?
			public static final String LSM303DLHC_ACCEL_RATE = SensorLSM303.GuiLabelConfig.LSM303DLHC_ACCEL_RATE;
			public static final String LSM303DLHC_ACCEL_RANGE = SensorLSM303.GuiLabelConfig.LSM303DLHC_ACCEL_RANGE;
			public static final String GSR_RANGE = SensorGSR.GuiLabelConfig.GSR_RANGE; // "GSR Range";
			public static final String LSM303DLHC_ACCEL_LPM = SensorLSM303.GuiLabelConfig.LSM303DLHC_ACCEL_LPM;
			public static final String SAMPLING_RATE_DIVIDER_BEACON = "Beacon Divider";
			
			public static final String SHIMMER_USER_ASSIGNED_NAME = "Shimmer Name";
			public static final String EXPERIMENT_NAME = "Experiment Name";
			public static final String SHIMMER_SAMPLING_RATE = "Sampling Rate";
			public static final String CONFIG_TIME = "Config Time";
			public static final String SHIMMER_MAC_FROM_INFOMEM = "InfoMem MAC";
		}
		
		public static final String[] ListofSamplingRateDividers={"0.75Hz","1.5Hz","3Hz","7.5Hz","15Hz","30Hz","75Hz","220Hz"};
		public static final Integer[] ListofSamplingRateDividersValues={0,1,2,3,4,5,6,7};
		
		
		public static class CompatibilityInfoForMaps{
			// These can be used to enable/disble GUI options depending on what HW, FW, Expansion boards versions are present
			private static final ShimmerVerObject svoAnyIntExpBoardAndFw = new ShimmerVerObject(HW_ID.SHIMMER_GQ_BLE,FW_ID.GQ_BLE,ShimmerVerDetails.ANY_VERSION,ShimmerVerDetails.ANY_VERSION,ShimmerVerDetails.ANY_VERSION,ShimmerVerDetails.ANY_VERSION);
			public static final List<ShimmerVerObject> listOfCompatibleVersionInfoGq = Arrays.asList(svoAnyIntExpBoardAndFw);
		}
		
		
		// These can be used to enable/disble GUI options depending on what HW, FW, Expansion boards versions are present
	    public static final Map<Integer, SensorDetailsRef> mSensorMapRef;
	    static {
	        Map<Integer, SensorDetailsRef> aMap = new LinkedHashMap<Integer, SensorDetailsRef>();

//	        aMap.put(Configuration.ShimmerGQ.SensorMapKey.SHIMMER_STREAMING_PROPERTIES, new SensorDetails(0, 0, "Device Properties"));

			aMap.put(Configuration.ShimmerGqBle.SensorMapKey.VBATT, new SensorDetailsRef(0, 0, Configuration.ShimmerGqBle.GuiLabelSensors.BATTERY));
			aMap.put(Configuration.ShimmerGqBle.SensorMapKey.LSM303DLHC_ACCEL, new SensorDetailsRef(0, 0, Configuration.ShimmerGqBle.GuiLabelSensors.ACCEL_WR)); //XXX-RS-LSM-SensorClass?
			aMap.put(Configuration.ShimmerGqBle.SensorMapKey.GSR, new SensorDetailsRef(0, 0, Configuration.ShimmerGqBle.GuiLabelSensors.GSR));
			aMap.put(Configuration.ShimmerGqBle.SensorMapKey.BEACON, new SensorDetailsRef(0, 0, Configuration.ShimmerGqBle.GuiLabelSensors.BEACON));
			aMap.put(Configuration.ShimmerGqBle.SensorMapKey.PPG, new SensorDetailsRef(0, 0, Configuration.ShimmerGqBle.GuiLabelSensors.PPG));
			
			aMap.get(Configuration.ShimmerGqBle.SensorMapKey.VBATT).mListOfCompatibleVersionInfo = Configuration.ShimmerGqBle.CompatibilityInfoForMaps.listOfCompatibleVersionInfoGq;
			aMap.get(Configuration.ShimmerGqBle.SensorMapKey.LSM303DLHC_ACCEL).mListOfCompatibleVersionInfo = Configuration.ShimmerGqBle.CompatibilityInfoForMaps.listOfCompatibleVersionInfoGq;  //XXX-RS-LSM-SensorClass?
			aMap.get(Configuration.ShimmerGqBle.SensorMapKey.GSR).mListOfCompatibleVersionInfo = Configuration.ShimmerGqBle.CompatibilityInfoForMaps.listOfCompatibleVersionInfoGq;
			aMap.get(Configuration.ShimmerGqBle.SensorMapKey.BEACON).mListOfCompatibleVersionInfo = Configuration.ShimmerGqBle.CompatibilityInfoForMaps.listOfCompatibleVersionInfoGq;
			aMap.get(Configuration.ShimmerGqBle.SensorMapKey.PPG).mListOfCompatibleVersionInfo = Configuration.ShimmerGqBle.CompatibilityInfoForMaps.listOfCompatibleVersionInfoGq;
			
			// Associated config options for each channel (currently used for the ChannelTileMap)
			aMap.get(Configuration.ShimmerGqBle.SensorMapKey.VBATT).mListOfConfigOptionKeysAssociated = Arrays.asList(
					Configuration.ShimmerGqBle.GuiLabelConfig.SAMPLING_RATE_DIVIDER_VBATT);
			aMap.get(Configuration.ShimmerGqBle.SensorMapKey.LSM303DLHC_ACCEL).mListOfConfigOptionKeysAssociated = Arrays.asList(  //XXX-RS-LSM-SensorClass?
					Configuration.ShimmerGqBle.GuiLabelConfig.LSM303DLHC_ACCEL_RANGE,
					Configuration.ShimmerGqBle.GuiLabelConfig.LSM303DLHC_ACCEL_RATE,
					Configuration.ShimmerGqBle.GuiLabelConfig.LSM303DLHC_ACCEL_LPM,
					Configuration.ShimmerGqBle.GuiLabelConfig.SAMPLING_RATE_DIVIDER_LSM303DLHC_ACCEL);
			aMap.get(Configuration.ShimmerGqBle.SensorMapKey.GSR).mListOfConfigOptionKeysAssociated = Arrays.asList(
					Configuration.ShimmerGqBle.GuiLabelConfig.GSR_RANGE,
					Configuration.ShimmerGqBle.GuiLabelConfig.SAMPLING_RATE_DIVIDER_GSR);
			aMap.get(Configuration.ShimmerGqBle.SensorMapKey.BEACON).mListOfConfigOptionKeysAssociated = Arrays.asList(
					Configuration.ShimmerGqBle.GuiLabelConfig.SAMPLING_RATE_DIVIDER_BEACON);
			aMap.get(Configuration.ShimmerGqBle.SensorMapKey.PPG).mListOfConfigOptionKeysAssociated = Arrays.asList(
					Configuration.ShimmerGqBle.GuiLabelConfig.SAMPLING_RATE_DIVIDER_PPG);
			mSensorMapRef = Collections.unmodifiableMap(aMap);
	    }

		
	    public static final Map<Integer, SensorGroupingDetails> mSensorGroupingMapRef;
	    static {
	        Map<Integer, SensorGroupingDetails> aMap = new TreeMap<Integer, SensorGroupingDetails>();

			//Sensor Grouping for Configuration Panel 'tile' generation. 
	        aMap.put(Configuration.ShimmerGqBle.GuiLabelSensorTiles.BATTERY_MONITORING.ordinal(), new SensorGroupingDetails(
	        		Configuration.ShimmerGqBle.GuiLabelSensorTiles.BATTERY_MONITORING.getTileText(),
	        		Arrays.asList(Configuration.ShimmerGqBle.SensorMapKey.VBATT)));
	        aMap.put(Configuration.ShimmerGqBle.GuiLabelSensorTiles.WIDE_RANGE_ACCEL.ordinal(), new SensorGroupingDetails(   //XXX-RS-LSM-SensorClass?
	        		Configuration.ShimmerGqBle.GuiLabelSensorTiles.WIDE_RANGE_ACCEL.getTileText(),
	        		Arrays.asList(Configuration.ShimmerGqBle.SensorMapKey.LSM303DLHC_ACCEL)));
	        aMap.put(Configuration.ShimmerGqBle.GuiLabelSensorTiles.GSR.ordinal(), new SensorGroupingDetails(
	        		Configuration.ShimmerGqBle.GuiLabelSensorTiles.GSR.getTileText(),
					Arrays.asList(Configuration.ShimmerGqBle.SensorMapKey.GSR,
								Configuration.ShimmerGqBle.SensorMapKey.PPG)));
	        aMap.put(Configuration.ShimmerGqBle.GuiLabelSensorTiles.BEACON.ordinal(), new SensorGroupingDetails(
	        		Configuration.ShimmerGqBle.GuiLabelSensorTiles.BEACON.getTileText(), 
	        		Arrays.asList(Configuration.ShimmerGqBle.SensorMapKey.BEACON)));
		
			// ChannelTiles that have compatibility considerations (used to auto generate tiles in GUI)
			aMap.get(Configuration.ShimmerGqBle.GuiLabelSensorTiles.BATTERY_MONITORING).mListOfCompatibleVersionInfo = Configuration.ShimmerGqBle.CompatibilityInfoForMaps.listOfCompatibleVersionInfoGq;;
			aMap.get(Configuration.ShimmerGqBle.GuiLabelSensorTiles.WIDE_RANGE_ACCEL).mListOfCompatibleVersionInfo = Configuration.ShimmerGqBle.CompatibilityInfoForMaps.listOfCompatibleVersionInfoGq;;   //XXX-RS-LSM-SensorClass?
			aMap.get(Configuration.ShimmerGqBle.GuiLabelSensorTiles.GSR).mListOfCompatibleVersionInfo = Configuration.ShimmerGqBle.CompatibilityInfoForMaps.listOfCompatibleVersionInfoGq;; 
			aMap.get(Configuration.ShimmerGqBle.GuiLabelSensorTiles.BEACON).mListOfCompatibleVersionInfo = Configuration.ShimmerGqBle.CompatibilityInfoForMaps.listOfCompatibleVersionInfoGq;;
			
			// For loop to automatically inherit associated channel configuration options from ChannelMap in the ChannelTileMap
			for (SensorGroupingDetails channelGroup:aMap.values()) {
				// Ok to clear here because variable is initiated in the class
				channelGroup.mListOfConfigOptionKeysAssociated.clear();
				for (Integer channel : channelGroup.mListOfSensorMapKeysAssociated) {
					if(mSensorMapRef.containsKey(channel)){
						List<String> associatedConfigOptions = mSensorMapRef.get(channel).mListOfConfigOptionKeysAssociated;
						if (associatedConfigOptions != null) {
							for (String configOption : associatedConfigOptions) {
								// do not add duplicates
								if (!(channelGroup.mListOfConfigOptionKeysAssociated.contains(configOption))) {
									channelGroup.mListOfConfigOptionKeysAssociated.add(configOption);
								}
							}
						}
					}
				}
			}
			
	        mSensorGroupingMapRef = Collections.unmodifiableMap(aMap);
	    }

		// Assemble the channel configuration options map
	    public static final Map<String, ConfigOptionDetailsSensor> mConfigOptionsMapRef;
	    static {
	        Map<String, ConfigOptionDetailsSensor> aMap = new LinkedHashMap<String, ConfigOptionDetailsSensor>();

			aMap = new HashMap<String,ConfigOptionDetailsSensor>();
			
	//		aMap.put(Configuration.Shimmer3.GuiLabelConfig.SHIMMER_USER_ASSIGNED_NAME, 
	//				new ConfigOptionDetailsSensor(ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.TEXTFIELD,
	//						listOfCompatibleVersionInfoGq));
			aMap.put(Configuration.Shimmer3.GuiLabelConfig.SHIMMER_SAMPLING_RATE, 
					new ConfigOptionDetailsSensor(ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.TEXTFIELD));
	//		aMap.put(Configuration.Shimmer3.GuiLabelConfig.SHIMMER_MAC_FROM_INFOMEM, 
	//				new ConfigOptionDetailsSensor(ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.TEXTFIELD));
			aMap.put(Configuration.Shimmer3.GuiLabelConfig.CONFIG_TIME, 
					new ConfigOptionDetailsSensor(ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.TEXTFIELD,
							Configuration.ShimmerGqBle.CompatibilityInfoForMaps.listOfCompatibleVersionInfoGq));
			aMap.put(Configuration.Shimmer3.GuiLabelConfig.TRIAL_NAME, 
					new ConfigOptionDetailsSensor(ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.TEXTFIELD,
							Configuration.ShimmerGqBle.CompatibilityInfoForMaps.listOfCompatibleVersionInfoGq));
	//		aMap.put(Configuration.Shimmer3.GuiLabelConfig.EXPERIMENT_ID, 
	//				new ConfigOptionDetailsSensor(ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.TEXTFIELD,
	//						Configuration.ShimmerGQ.CompatibilityInfoForMaps.listOfCompatibleVersionInfoGq));
			
			aMap.put(SensorLSM303.GuiLabelConfig.LSM303DLHC_ACCEL_RANGE, SensorLSM303.configOptionAccelRange);
//			aMap.put(Configuration.Shimmer3.GuiLabelConfig.LSM303DLHC_ACCEL_RANGE,   //XXX-RS-LSM-SensorClass?
//					new ConfigOptionDetailsSensor(Configuration.Shimmer3.ListofAccelRange, 
//											Configuration.Shimmer3.ListofLSM303DLHCAccelRangeConfigValues, 
//											ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX));
			aMap.put(SensorLSM303.GuiLabelConfig.LSM303DLHC_ACCEL_RATE, SensorLSM303.configOptionAccelRate);
//			aMap.put(Configuration.Shimmer3.GuiLabelConfig.LSM303DLHC_ACCEL_RATE, //XXX-RS-LSM-SensorClass?
//					new ConfigOptionDetailsSensor(Configuration.Shimmer3.ListofLSM303DLHCAccelRate, 
//											Configuration.Shimmer3.ListofLSM303DLHCAccelRateConfigValues, 
//											ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX));
//			aMap.get(Configuration.Shimmer3.GuiLabelConfig.LSM303DLHC_ACCEL_RATE).setGuiValues(ConfigOptionDetailsSensor.VALUE_INDEXES.LSM303DLHC_ACCEL_RATE.IS_LPM, Configuration.Shimmer3.ListofLSM303DLHCAccelRateLpm);     //XXX-RS-LSM-SensorClass?
//			aMap.get(Configuration.Shimmer3.GuiLabelConfig.LSM303DLHC_ACCEL_RATE).setConfigValues(ConfigOptionDetailsSensor.VALUE_INDEXES.LSM303DLHC_ACCEL_RATE.IS_LPM, Configuration.Shimmer3.ListofLSM303DLHCAccelRateLpmConfigValues); //XXX-RS-LSM-SensorClass?
	
			
			//XXX-RS-LSM-SensorClass? 
	//		if(mLowPowerAccelWR) {
	//			aMap.put(Configuration.Shimmer3.GuiLabelConfig.LSM303DLHC_ACCEL_RATE, 
	//					new ConfigOptionDetailsSensor(Configuration.Shimmer3.ListofLSM303DLHCAccelRateLpm, 
	//											Configuration.Shimmer3.ListofLSM303DLHCAccelRateLpmConfigValues, 
	//											ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX));
	//		}
	//		else {
	//			aMap.put(Configuration.Shimmer3.GuiLabelConfig.LSM303DLHC_ACCEL_RATE, 
	//					new ConfigOptionDetailsSensor(Configuration.Shimmer3.ListofLSM303DLHCAccelRate, 
	//											Configuration.Shimmer3.ListofLSM303DLHCAccelRateConfigValues, 
	//											ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX));
	//		}
			aMap.put(SensorGSR.GuiLabelConfig.GSR_RANGE, SensorGSR.configOptionGsrRange);  
//			aMap.put(Configuration.Shimmer3.GuiLabelConfig.GSR_RANGE, 
//					new ConfigOptionDetailsSensor(Configuration.Shimmer3.ListofGSRRange, 
//											Configuration.Shimmer3.ListofGSRRangeConfigValues, 
//											ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX,
//											Configuration.ShimmerGqBle.CompatibilityInfoForMaps.listOfCompatibleVersionInfoGq));
//			
			aMap.put(Configuration.ShimmerGqBle.GuiLabelConfig.SAMPLING_RATE_DIVIDER_GSR, 
					new ConfigOptionDetailsSensor(Configuration.ShimmerGqBle.ListofSamplingRateDividers, 
											Configuration.ShimmerGqBle.ListofSamplingRateDividersValues, 
											ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX));
			aMap.put(Configuration.ShimmerGqBle.GuiLabelConfig.SAMPLING_RATE_DIVIDER_LSM303DLHC_ACCEL,   //XXX-RS-LSM-SensorClass?
					new ConfigOptionDetailsSensor(Configuration.ShimmerGqBle.ListofSamplingRateDividers, 
											Configuration.ShimmerGqBle.ListofSamplingRateDividersValues, 
											ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX));
			aMap.put(Configuration.ShimmerGqBle.GuiLabelConfig.SAMPLING_RATE_DIVIDER_PPG, 
					new ConfigOptionDetailsSensor(Configuration.ShimmerGqBle.ListofSamplingRateDividers, 
											Configuration.ShimmerGqBle.ListofSamplingRateDividersValues, 
											ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX));
			aMap.put(Configuration.ShimmerGqBle.GuiLabelConfig.SAMPLING_RATE_DIVIDER_VBATT, 
					new ConfigOptionDetailsSensor(Configuration.ShimmerGqBle.ListofSamplingRateDividers, 
											Configuration.ShimmerGqBle.ListofSamplingRateDividersValues, 
											ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX));
			aMap.put(Configuration.ShimmerGqBle.GuiLabelConfig.SAMPLING_RATE_DIVIDER_BEACON, 
					new ConfigOptionDetailsSensor(Configuration.ShimmerGqBle.ListofSamplingRateDividers, 
											Configuration.ShimmerGqBle.ListofSamplingRateDividersValues, 
											ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX));
	
		    aMap.put(Configuration.Shimmer3.GuiLabelConfig.INT_EXP_BRD_POWER_INTEGER, 
		    	      new ConfigOptionDetailsSensor(Configuration.Shimmer3.ListOfOnOff, 
				    	        Configuration.Shimmer3.ListOfOnOffConfigValues,
				    	        ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX));
			
			//General Config
			aMap.put(Configuration.Shimmer3.GuiLabelConfig.USER_BUTTON_START, 
					new ConfigOptionDetailsSensor(ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.CHECKBOX,Configuration.ShimmerGqBle.CompatibilityInfoForMaps.listOfCompatibleVersionInfoGq));
			aMap.put(Configuration.Shimmer3.GuiLabelConfig.SINGLE_TOUCH_START, 
					new ConfigOptionDetailsSensor(ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.CHECKBOX,Configuration.ShimmerGqBle.CompatibilityInfoForMaps.listOfCompatibleVersionInfoGq));
	
			aMap.put(SensorLSM303.GuiLabelConfig.LSM303DLHC_ACCEL_LPM,  //XXX-RS-LSM-SensorClass?
					new ConfigOptionDetailsSensor(ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.CHECKBOX));
			
			aMap.put(Configuration.Shimmer3.GuiLabelConfig.INT_EXP_BRD_POWER_BOOLEAN, 
					new ConfigOptionDetailsSensor(ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.CHECKBOX));
			
			
			mConfigOptionsMapRef = Collections.unmodifiableMap(aMap);
	    }
	}
	
	
	public static class Shimmer4{
		public class GuiLabelConfig{
			public static final String INT_EXP_BRD_POWER_BOOLEAN = Configuration.Shimmer3.GuiLabelConfig.INT_EXP_BRD_POWER_BOOLEAN;
			public static final String INT_EXP_BRD_POWER_INTEGER = Configuration.Shimmer3.GuiLabelConfig.INT_EXP_BRD_POWER_INTEGER;
		}
		
		public static final ConfigOptionDetailsSensor configOptionIntExpBrdPowerInteger = new ConfigOptionDetailsSensor(
				Configuration.Shimmer3.ListOfOnOff, 
				Configuration.Shimmer3.ListOfOnOffConfigValues, 
				ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX);

		public static final ConfigOptionDetailsSensor configOptionIntExpBrdPowerBoolean = new ConfigOptionDetailsSensor(
				ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.CHECKBOX);
		
		
		// Assemble the channel configuration options map
	    public static final Map<String, ConfigOptionDetailsSensor> mConfigOptionsMapRef;
	    static {
	        Map<String, ConfigOptionDetailsSensor> aMap = new LinkedHashMap<String, ConfigOptionDetailsSensor>();

			aMap = new HashMap<String,ConfigOptionDetailsSensor>();

		    aMap.put(GuiLabelConfig.INT_EXP_BRD_POWER_INTEGER, configOptionIntExpBrdPowerInteger);
		    aMap.put(GuiLabelConfig.INT_EXP_BRD_POWER_BOOLEAN, configOptionIntExpBrdPowerBoolean);
			mConfigOptionsMapRef = Collections.unmodifiableMap(aMap);
	    }
	}

	
}


