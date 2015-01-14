//Rev_1.9
/*
 * Copyright (c) 2010 - 2014, Shimmer Research, Ltd.
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
 * @author Jong Chern Lim, Ruaidhri Molloy, Alejandro Saez, Mark Nolan 
 * 
 * Changes since 1.8
 * - updated to work with SDLOG
 * - change method formatdatapacketreverse to protected
 * - added i32r to parsed data
 * - mSignalNameArray changed to protected
 * - fix twos complement for long numbers (32bit>=)
 * - fix 6dof calculation ~ long 
 * - fix intiali time stamp ~ long
 * 
 * 
 * Changes since 1.7
 * - remove multiplying factor (2.8) from the gain in the calculation of the Bridge Amplifier calibrated data
 * 
 * @date   September, 2014
 * 
 * Changes since 1.6
 * - Added functionality for plotmanager see MSSAPI , addExtraSignalProperty, removeExtraSignalProperty ,getListofEnabledSensorSignalsandFormats()
 * - various exg advance updates
 * 
 * @date   July, 2014
 * 
 * Changes since 1.5
 * - Bridge Amplifier gauge support for Shimmer3
 * - Bug fix for strain gauge calibration for Shimmer2r
 * - Enable 3D orientation for wide range accel, orientation algorithm defaults to low noise even if wide range is enabled
 * - Fixed quaternion naming typo
 * - Commented out initialisation mSensorBitmaptoName
 * - add method getPressureRawCoefficients
 *  
 * @date   October, 2013
 * 
 * Changes since 1.4
 * - fix getListofEnabledSensors, which was not returning accel shimmer2r
 * - fix null pointer graddes algo when using Shimmer2
 * - converted to abstract class , and added checkbattery abstract method for the Shimmer2r
 * - updated gsr calibrate command parameters for Shimmer3
 * - removed mShimmerSamplingRate decimal formatter, decimal formatter should be done on the UI
 * - fixed a GSR Shimmer2 problem when using autorange
 * - added VSense Batt and VSense Reg and Timestamp to getListOfEnabledSensorSignals
 * - added getSamplingRate()
 * - added get methods for calibration parameters accel,gyro,mag,accel2
 * - updated getoffsetaccel
 * - added exg configurations
 * - added i24r for exg
 * - add get exg configurations
 * - renamed i16* to i16r for consistency
 * - added EXG_CHIP1 = 0 and EXG_CHIP2=1
 * - updated Mag Default Range
 * 
 */

package com.shimmerresearch.driver;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.vecmath.Quat4d;
import javax.vecmath.Vector3d;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import sun.util.calendar.BaseCalendar.Date;
import sun.util.calendar.CalendarDate;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.shimmerresearch.algorithms.GradDes3DOrientation;
import com.shimmerresearch.driver.ChannelDetails;
import com.shimmerresearch.algorithms.GradDes3DOrientation.Quaternion;
import com.sun.org.apache.bcel.internal.generic.ISUB;

public abstract class ShimmerObject implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1364568867018921219L;
	
	public static final String ACCEL_CAL_UNIT = "m/s^2";
	public static final String ACCEL_DEFAULT_CAL_UNIT = "m/s^2";
	public static final String ADC_CAL_UNIT = "mV";
	public static final String GSR_CAL_UNIT = "kOhms";
	public static final String GYRO_CAL_UNIT = "deg/s";
	public static final String GYRO_DEFAULT_CAL_UNIT = "deg/s";
	public static final String MAG_CAL_UNIT = "local flux";
	public static final String MAG_DEFAULT_CAL_UNIT = "local flux";
	public static final String TEMP_CAL_UNIT = "Degrees Celsius";
	public static final String PRESSURE_CAL_UNIT = "kPa";
	public static final String HEARTRATE_CAL_UNIT = "bpm";
	
	public static final String NO_UNIT = "No Units";
	public static final String CLOCK_UNIT = "Ticks";
	
//	public static final int SHIMMER_1=0;
//	public static final int SHIMMER_2=1;
//	public static final int SHIMMER_2R=2;
//	public static final int SHIMMER_3=3;
//	public static final int SHIMMER_SR30=4;
	
	public final static int HW_ID_SHIMMER_1 = 0;
	public final static int HW_ID_SHIMMER_2 = 1;
	public final static int HW_ID_SHIMMER_2R = 2;
	public final static int HW_ID_SHIMMER_3 = 3;
	public final static int HW_ID_SHIMMER_SR30 = 4;
	
	public final static int FW_ID_SHIMMER3_BOILER_PLATE = 0;
	public final static int FW_ID_SHIMMER3_BTSTREAM = 1;
	public final static int FW_ID_SHIMMER3_SDLOG = 2;
	public final static int FW_ID_SHIMMER3_LOGANDSTREAM = 3;

	protected boolean mFirstTime = true;
	double mFirstRawTS = 0;
	public int OFFSET_LENGTH = 9;
	
	//Sensor Bitmap for ID ; for the purpose of forward compatibility the sensor bitmap and the ID and the sensor bitmap for the Shimmer firmware has been kept separate, 
	public static final int SENSOR_ACCEL				   = 0x80; 
	public static final int SENSOR_DACCEL				   = 0x1000; //only 
	public static final int SENSOR_GYRO				   	   = 0x40;
	public static final int SENSOR_MAG					   = 0x20;
	public static final int SENSOR_ECG					   = 0x10;
	public static final int SENSOR_EMG					   = 0x08;
	public static final int SENSOR_EXG1_24BIT			   = 0x10; //only applicable for Shimmer3
	public static final int SENSOR_EXG2_24BIT			   = 0x08; //only applicable for Shimmer3
	public static final int SHIMMER3_SENSOR_ECG			   = SENSOR_EXG1_24BIT + SENSOR_EXG2_24BIT;
	public static final int SHIMMER3_SENSOR_EMG			   = SENSOR_EXG1_24BIT;
	public static final int SENSOR_GSR					   = 0x04;
	public static final int SENSOR_EXP_BOARD_A7		       = 0x02; //only Applicable for Shimmer2
	public static final int SENSOR_EXP_BOARD_A0		       = 0x01; //only Applicable for Shimmer2
	public static final int SENSOR_EXP_BOARD		       = SENSOR_EXP_BOARD_A7+SENSOR_EXP_BOARD_A0;
	public static final int SENSOR_BRIDGE_AMP			   = 0x8000;
	public static final int SENSOR_HEART				   = 0x4000;
	public static final int SENSOR_BATT	  			       = 0x2000; //THIS IS A DUMMY VALUE
	public static final int SENSOR_EXT_ADC_A7              = 0x02; //only Applicable for Shimmer3
	public static final int SENSOR_EXT_ADC_A6              = 0x01; //only Applicable for Shimmer3
	public static final int SENSOR_EXT_ADC_A15             = 0x0800;
	public static final int SENSOR_INT_ADC_A1              = 0x0400;
	public static final int SENSOR_INT_ADC_A12             = 0x0200;
	public static final int SENSOR_INT_ADC_A13             = 0x0100;
	public static final int SENSOR_INT_ADC_A14             = 0x800000;
	public static final int SENSOR_ALL_ADC_SHIMMER3        = SENSOR_INT_ADC_A14 | SENSOR_INT_ADC_A13 | SENSOR_INT_ADC_A12 | SENSOR_INT_ADC_A1 | SENSOR_EXT_ADC_A7 | SENSOR_EXT_ADC_A6 | SENSOR_EXT_ADC_A15; 
	public static final int SENSOR_BMP180              	   = 0x40000;
	public static final int SENSOR_EXG1_16BIT			   = 0x100000; //only applicable for Shimmer3
	public static final int SENSOR_EXG2_16BIT			   = 0x080000; //only applicable for Shimmer3
	public BiMap<String, String> mSensorBitmaptoName;  
	
	public class SDLogHeader {
		public final static int ACCEL_LN = 1<<7;
		public final static int GYRO = 1<<6;
		public final static int MAG = 1<<5;
		public final static int EXG1_24BIT = 1<<4;
		public final static int EXG2_24BIT = 1<<3;
		public final static int GSR = 1<<2;
		public final static int EXT_EXP_A7 = 1<<1;
		public final static int EXT_EXP_A6 = 1<<0;
		public final static int BRIDGE_AMP = 1<<15;
		// 1<<9 NONE 
		public final static int BATTERY = 1<<13;
		public final static int ACCEL_WR = 1<<12;
		public final static int EXT_EXP_A15 = 1<<11;
		public final static int INT_EXP_A1 = 1<<10;
		public final static int INT_EXP_A12 = 1<<9;
		public final static int INT_EXP_A13 = 1<<8;
		public final static int INT_EXP_A14 = 1<<23;
		public final static int ACCEL_MPU = 1<<22;
		public final static int MAG_MPU = 1<<21;
		public final static int EXG1_16BIT = 1<<20;
		public final static int EXG2_16BIT = 1<<19;
		public final static int BMP180 = 1<<18;
		public final static int MPL_TEMPERATURE = 1<<17;
		// 1<<23
		public final static int MPL_QUAT_6DOF = 1<<31;
		public final static int MPL_QUAT_9DOF = 1<<30;
		public final static int MPL_EULER_6DOF = 1<<29;
		public final static int MPL_EULER_9DOF = 1<<28;
		public final static int MPL_HEADING = 1<<27;
		public final static int MPL_PEDOMETER = 1<<26;
		public final static int MPL_TAP = 1<<25;
		public final static int MPL_MOTION_ORIENT = 1<<24;
		public final static long GYRO_MPU_MPL = (long)1<<39;
		public final static long ACCEL_MPU_MPL = (long)1<<38;
		public final static long MAG_MPU_MPL = (long)1<<37;
		public final static long SD_SENSOR_MPL_QUAT_6DOF_RAW = (long)1<<36;
	}
	public class BTStream {
		public final static int ACCEL_LN = 0x80; 
		public final static int GYRO = 0x40;
		public final static int MAG = 0x20;
		public final static int EXG1_24BIT = 0x10;
		public final static int EXG2_24BIT = 0x08;
		public final static int GSR = 0x04;
		public final static int EXT_EXP_A7 = 0x02;
		public final static int EXT_EXP_A6 = 0x01;
		public final static int BRIDGE_AMP = 0x8000;
		// 1<<9 NONE 
		public final static int BATTERY = 0x2000;
		public final static int ACCEL_WR = 0x1000;
		public final static int EXT_EXP_A15 = 0x0800;
		public final static int INT_EXP_A1 = 0x0400;
		public final static int INT_EXP_A12 = 0x0200;
		public final static int INT_EXP_A13 = 0x0100;
		public final static int INT_EXP_A14 = 0x800000;
		public final static int ACCEL_MPU = 0x400000;
		public final static int MAG_MPU = 0x200000;
		public final static int EXG1_16BIT = 0x100000;
		public final static int EXG2_16BIT = 0x080000;
		public final static int BMP180 = 0x40000;
		public final static int MPL_TEMPERATURE = 1<<22;
		// 1<<23
		//public final static int MPL_QUAT_6DOF = 1<<24;
		//public final static int MPL_QUAT_9DOF = 1<<25;
		//public final static int MPL_EULER_6DOF = 1<<26;
		//public final static int MPL_EULER_9DOF = 1<<27;
		//public final static int MPL_HEADING = 1<<28;
		//public final static int MPL_PEDOMETER = 1<<29;
		//public final static int MPL_TAP = 1<<30;
		//public final static int MPL_MOTION_ORIENT = 1<<31;
		//public final static long GYRO_MPU_MPL = 1<<32;
		//public final static long ACCEL_MPU_MPL = 1<<33;
		//public final static long MAG_MPU_MPL = 1<<34;
		//public final static long SD_SENSOR_MPL_QUAT_6DOF_RAW = 1<<35;
	}
	
	
	/*
		{  
		final Map<String, String> tempSensorBMtoName = new HashMap<String, String>();  
		tempSensorBMtoName.put(Integer.toString(SENSOR_ACCEL), "Accelerometer");  
		tempSensorBMtoName.put(Integer.toString(SENSOR_GYRO), "Gyroscope");  
		tempSensorBMtoName.put(Integer.toString(SENSOR_MAG), "Magnetometer");  
		tempSensorBMtoName.put(Integer.toString(SENSOR_ECG), "ECG");  
		tempSensorBMtoName.put(Integer.toString(SENSOR_EMG), "EMG");  
		tempSensorBMtoName.put(Integer.toString(SENSOR_GSR), "GSR");  
		tempSensorBMtoName.put(Integer.toString(SENSOR_EXP_BOARD_A7), "Exp Board A7");
		tempSensorBMtoName.put(Integer.toString(SENSOR_EXP_BOARD_A0), "Exp Board A0");
		tempSensorBMtoName.put(Integer.toString(SENSOR_EXP_BOARD), "Exp Board");
		tempSensorBMtoName.put(Integer.toString(SENSOR_BRIDGE_AMP), "Bridge Amplifier");
		tempSensorBMtoName.put(Integer.toString(SENSOR_HEART), "Heart Rate");  
		tempSensorBMtoName.put(Integer.toString(SENSOR_BATT), "Battery Voltage");
		tempSensorBMtoName.put(Integer.toString(SENSOR_EXT_ADC_A7), "External ADC A7");  
		tempSensorBMtoName.put(Integer.toString(SENSOR_EXT_ADC_A6), "External ADC A6");  
		tempSensorBMtoName.put(Integer.toString(SENSOR_EXT_ADC_A15), "External ADC A15");
		tempSensorBMtoName.put(Integer.toString(SENSOR_INT_ADC_A1), "Internal ADC A1");
		tempSensorBMtoName.put(Integer.toString(SENSOR_INT_ADC_A12), "Internal ADC A12");
		tempSensorBMtoName.put(Integer.toString(SENSOR_INT_ADC_A13), "Internal ADC A13");
		tempSensorBMtoName.put(Integer.toString(SENSOR_INT_ADC_A14), "Internal ADC A14");
		mSensorBitmaptoName = ImmutableBiMap.copyOf(Collections.unmodifiableMap(tempSensorBMtoName));
		}  
	 */
	
	protected TreeMap<Integer,ChannelDetails> mShimmerSensorsMap = new TreeMap<Integer,ChannelDetails>();
	protected TreeMap<Integer,ChannelOptionDetails> mShimmerSensorsOptionsMap = new TreeMap<Integer,ChannelOptionDetails>();

	//TODO: move sensor map keys to Configuration.Shimmer3
	//TODO: create sensor map keys for Shimmer2 in Configuration.Shimmer2
	public final static int SENSORMAPKEY_SHIMMER2R_ACCEL = 0;
	public final static int SENSORMAPKEY_SHIMMER2R_GYRO = 1;
	public final static int SENSORMAPKEY_SHIMMER2R_MAG = 2;
	public final static int SENSORMAPKEY_SHIMMER2R_EMG = 3;
	public final static int SENSORMAPKEY_SHIMMER2R_ECG = 4;
	public final static int SENSORMAPKEY_SHIMMER2R_GSR = 5;
	public final static int SENSORMAPKEY_SHIMMER2R_EXP_BOARD_A7 = 6;
	public final static int SENSORMAPKEY_SHIMMER2R_EXP_BOARD_A0 = 7;
	
	public final static int SENSORMAPKEY_SHIMMER2R_EXP_BOARD = 8;
	public final static int SENSORMAPKEY_SHIMMER2R_BRIDGE_AMP = 9;
	public final static int SENSORMAPKEY_SHIMMER2R_HEART = 10;
	public final static int SENSORMAPKEY_SHIMMER2R_BATT = 11;
	public final static int SENSORMAPKEY_SHIMMER2R_EXT_ADC_A15 = 12;
	public final static int SENSORMAPKEY_SHIMMER2R_INT_ADC_A1 = 13;
	public final static int SENSORMAPKEY_SHIMMER2R_INT_ADC_A12 = 14;
	public final static int SENSORMAPKEY_SHIMMER2R_INT_ADC_A13 = 15;
	public final static int SENSORMAPKEY_SHIMMER2R_INT_ADC_A14 = 16;

	/**
	 * Shimmer3 Low-noise analog accelerometer
	 */
	public final static int SENSORMAPKEY_SHIMMER3_A_ACCEL = 0;
	/**
	 * Shimmer3 Gyroscope
	 */
	public final static int SENSORMAPKEY_SHIMMER3_MPU9150_GYRO = 1;
	/**
	 * Shimmer3 Primary magnetometer
	 */
	public final static int SENSORMAPKEY_SHIMMER3_LSM303DLHC_MAG = 2;
	public final static int SENSORMAPKEY_SHIMMER3_EXG1_24BIT = 3;
	public final static int SENSORMAPKEY_SHIMMER3_EXG2_24BIT = 4;
	public final static int SENSORMAPKEY_SHIMMER3_GSR = 5;
	public final static int SENSORMAPKEY_SHIMMER3_EXT_ADC_A7 = 6;
	public final static int SENSORMAPKEY_SHIMMER3_EXT_ADC_A6 = 7;
	public final static int SENSORMAPKEY_SHIMMER3_BRIDGE_AMP = 8;
	public final static int SENSORMAPKEY_SHIMMER3_RESISTANCE_AMP = 9;
	//public final static int SENSORMAPKEY_SHIMMER3_HR = 9;
	public final static int SENSORMAPKEY_SHIMMER3_VBATT = 10;
	/**
	 * Shimmer3 Wide-range digital accelerometer
	 */
	public final static int SENSORMAPKEY_SHIMMER3_LSM303DLHC_ACCEL = 11;
	public final static int SENSORMAPKEY_SHIMMER3_EXT_ADC_A15 = 12;
	public final static int SENSORMAPKEY_SHIMMER3_INT_ADC_A1 = 13;
	public final static int SENSORMAPKEY_SHIMMER3_INT_ADC_A12 = 14;
	public final static int SENSORMAPKEY_SHIMMER3_INT_ADC_A13 = 15;
	public final static int SENSORMAPKEY_SHIMMER3_INT_ADC_A14 = 16;
	/**
	 * Shimmer3 Alternative accelerometer
	 */
	public final static int SENSORMAPKEY_SHIMMER3_MPU9150_ACCEL = 17;
	/**
	 * Shimmer3 Alternative magnetometer
	 */
	public final static int SENSORMAPKEY_SHIMMER3_MPU9150_MAG = 18;
	public final static int SENSORMAPKEY_SHIMMER3_EXG1_16BIT = 19;
	public final static int SENSORMAPKEY_SHIMMER3_EXG2_16BIT = 21;
	public final static int SENSORMAPKEY_SHIMMER3_BMP180_PRESSURE = 22;
	//public final static int SENSORMAPKEY_SHIMMER3_BMP180_TEMPERATURE = 23; // not yet implemented
	//public final static int SENSORMAPKEY_SHIMMER3_MSP430_TEMPERATURE = 24; // not yet implemented
	public final static int SENSORMAPKEY_SHIMMER3_MPU9150_TEMP = 25;
	//public final static int SENSORMAPKEY_SHIMMER3_LSM303DLHC_TEMPERATURE = 26; // not yet implemented
	//public final static int SENSORMAPKEY_SHIMMER3_MPL_TEMPERATURE = 1<<17; // same as SENSOR_SHIMMER3_MPU9150_TEMP 
	public final static int SENSORMAPKEY_SHIMMER3_MPL_QUAT_6DOF = 27;
	public final static int SENSORMAPKEY_SHIMMER3_MPL_QUAT_9DOF = 28;
	public final static int SENSORMAPKEY_SHIMMER3_MPL_EULER_6DOF = 29;
	public final static int SENSORMAPKEY_SHIMMER3_MPL_EULER_9DOF = 30;
	public final static int SENSORMAPKEY_SHIMMER3_MPL_HEADING = 31;
	public final static int SENSORMAPKEY_SHIMMER3_MPL_PEDOMETER = 32;
	public final static int SENSORMAPKEY_SHIMMER3_MPL_TAP = 33;
	public final static int SENSORMAPKEY_SHIMMER3_MPL_MOTION_ORIENT = 34;
	public final static int SENSORMAPKEY_SHIMMER3_GYRO_MPU_MPL = 35;
	public final static int SENSORMAPKEY_SHIMMER3_ACCEL_MPU_MPL = 36;
	public final static int SENSORMAPKEY_SHIMMER3_MAG_MPU_MPL = 37;
	public final static int SENSORMAPKEY_SHIMMER3_MPL_QUAT_6DOF_RAW = 38;
	
	// Combination Channels
	public final static int SENSORMAPKEY_SHIMMER3_ECG = 100;
	public final static int SENSORMAPKEY_SHIMMER3_EMG = 101;
	public final static int SENSORMAPKEY_SHIMMER3_EXG_TEST = 102;
	public final static int SENSORMAPKEY_SHIMMER3_ALL_ADC = 103;
	





	
	//Constants describing the packet type
	public static final byte DATA_PACKET                      		= (byte) 0x00;
	public static final byte INQUIRY_COMMAND                  		= (byte) 0x01;
	public static final byte INQUIRY_RESPONSE                 		= (byte) 0x02;
	public static final byte GET_SAMPLING_RATE_COMMAND 	   			= (byte) 0x03;
	public static final byte SAMPLING_RATE_RESPONSE           		= (byte) 0x04;
	public static final byte SET_SAMPLING_RATE_COMMAND        		= (byte) 0x05;
	public static final byte TOGGLE_LED_COMMAND              		= (byte) 0x06;
	public static final byte START_STREAMING_COMMAND          		= (byte) 0x07;
	public static final byte SET_SENSORS_COMMAND              		= (byte) 0x08;
	public static final byte SET_ACCEL_SENSITIVITY_COMMAND    		= (byte) 0x09;
	public static final byte ACCEL_SENSITIVITY_RESPONSE       		= (byte) 0x0A;
	public static final byte GET_ACCEL_SENSITIVITY_COMMAND    		= (byte) 0x0B;
	public static final byte SET_5V_REGULATOR_COMMAND         		= (byte) 0x0C; // only Shimmer 2
	public static final byte SET_PMUX_COMMAND                 		= (byte) 0x0D; // only Shimmer 2
	public static final byte SET_CONFIG_BYTE0_COMMAND   	   		= (byte) 0x0E;
	public static final byte CONFIG_BYTE0_RESPONSE      	   		= (byte) 0x0F;
	public static final byte GET_CONFIG_BYTE0_COMMAND   	   		= (byte) 0x10;
	public static final byte STOP_STREAMING_COMMAND           		= (byte) 0x20;
	public static final byte SET_ACCEL_CALIBRATION_COMMAND			= (byte) 0x11;
	public static final byte ACCEL_CALIBRATION_RESPONSE       		= (byte) 0x12;
	public static final byte LSM303DLHC_ACCEL_CALIBRATION_RESPONSE 	= (byte) 0x1B;
	public static final byte SET_LSM303DLHC_ACCEL_CALIBRATION_COMMAND = (byte) 0x1A;
	public static final byte GET_ACCEL_CALIBRATION_COMMAND    		= (byte) 0x13;
	public static final byte SET_GYRO_CALIBRATION_COMMAND 	  		= (byte) 0x14;
	public static final byte GYRO_CALIBRATION_RESPONSE        		= (byte) 0x15;
	public static final byte GET_GYRO_CALIBRATION_COMMAND     		= (byte) 0x16;
	public static final byte SET_MAG_CALIBRATION_COMMAND      		= (byte) 0x17;
	public static final byte MAG_CALIBRATION_RESPONSE         		= (byte) 0x18;
	public static final byte GET_MAG_CALIBRATION_COMMAND      		= (byte) 0x19;
	public static final byte SET_GSR_RANGE_COMMAND			   		= (byte) 0x21;
	public static final byte GSR_RANGE_RESPONSE			   			= (byte) 0x22;
	public static final byte GET_GSR_RANGE_COMMAND			   		= (byte) 0x23;
	public static final byte GET_SHIMMER_VERSION_COMMAND      		= (byte) 0x24;
	public static final byte GET_SHIMMER_VERSION_COMMAND_NEW      	= (byte) 0x3F; //this is to avoid the $ char which is used by rn42
	public static final byte GET_SHIMMER_VERSION_RESPONSE     		= (byte) 0x25;
	public static final byte SET_EMG_CALIBRATION_COMMAND      		= (byte) 0x26;
	public static final byte EMG_CALIBRATION_RESPONSE         		= (byte) 0x27;
	public static final byte GET_EMG_CALIBRATION_COMMAND      		= (byte) 0x28;
	public static final byte SET_ECG_CALIBRATION_COMMAND      		= (byte) 0x29;
	public static final byte ECG_CALIBRATION_RESPONSE         		= (byte) 0x2A;
	public static final byte GET_ECG_CALIBRATION_COMMAND      		= (byte) 0x2B;
	public static final byte GET_ALL_CALIBRATION_COMMAND      		= (byte) 0x2C;
	public static final byte ALL_CALIBRATION_RESPONSE         		= (byte) 0x2D; 
	public static final byte GET_FW_VERSION_COMMAND          		= (byte) 0x2E;
	public static final byte FW_VERSION_RESPONSE             	 	= (byte) 0x2F;
	public static final byte SET_BLINK_LED                    		= (byte) 0x30;
	public static final byte BLINK_LED_RESPONSE               		= (byte) 0x31;
	public static final byte GET_BLINK_LED                    		= (byte) 0x32;
	public static final byte SET_GYRO_TEMP_VREF_COMMAND       		= (byte) 0x33;
	public static final byte SET_BUFFER_SIZE_COMMAND          		= (byte) 0x34;
	public static final byte BUFFER_SIZE_RESPONSE             		= (byte) 0x35;
	public static final byte GET_BUFFER_SIZE_COMMAND          		= (byte) 0x36;
	public static final byte SET_MAG_GAIN_COMMAND             		= (byte) 0x37;
	public static final byte MAG_GAIN_RESPONSE                		= (byte) 0x38;
	public static final byte GET_MAG_GAIN_COMMAND             		= (byte) 0x39;
	public static final byte SET_MAG_SAMPLING_RATE_COMMAND    		= (byte) 0x3A;
	public static final byte MAG_SAMPLING_RATE_RESPONSE       		= (byte) 0x3B;
	public static final byte GET_MAG_SAMPLING_RATE_COMMAND    		= (byte) 0x3C;
	public static final byte SET_ACCEL_SAMPLING_RATE_COMMAND  		= (byte) 0x40;
	public static final byte ACCEL_SAMPLING_RATE_RESPONSE  			= (byte) 0x41;
	public static final byte GET_ACCEL_SAMPLING_RATE_COMMAND  		= (byte) 0x42;
	public static final byte SET_LSM303DLHC_ACCEL_LPMODE_COMMAND 	= (byte) 0x43;
	public static final byte LSM303DLHC_ACCEL_LPMODE_RESPONSE		= (byte) 0x44;
	public static final byte GET_LSM303DLHC_ACCEL_LPMODE_COMMAND 	= (byte) 0x45;
	public static final byte SET_LSM303DLHC_ACCEL_HRMODE_COMMAND	= (byte) 0x46;
	public static final byte LSM303DLHC_ACCEL_HRMODE_RESPONSE		= (byte) 0x47;
	public static final byte GET_LSM303DLHC_ACCEL_HRMODE_COMMAND 	= (byte) 0x48;
	public static final byte SET_MPU9150_GYRO_RANGE_COMMAND 		= (byte) 0x49;
	public static final byte MPU9150_GYRO_RANGE_RESPONSE 			= (byte) 0x4A;
	public static final byte GET_MPU9150_GYRO_RANGE_COMMAND 		= (byte) 0x4B;
	public static final byte SET_MPU9150_SAMPLING_RATE_COMMAND 		= (byte) 0x4C;
	public static final byte MPU9150_SAMPLING_RATE_RESPONSE 		= (byte) 0x4D;
	public static final byte GET_MPU9150_SAMPLING_RATE_COMMAND 		= (byte) 0x4E;
	public static final byte SET_BMP180_PRES_RESOLUTION_COMMAND 	= (byte) 0x52;
	public static final byte BMP180_PRES_RESOLUTION_RESPONSE 		= (byte) 0x53;
	public static final byte GET_BMP180_PRES_RESOLUTION_COMMAND 	= (byte) 0x54;
	public static final byte SET_BMP180_PRES_CALIBRATION_COMMAND	= (byte) 0x55;
	public static final byte BMP180_PRES_CALIBRATION_RESPONSE 		= (byte) 0x56;
	public static final byte GET_BMP180_PRES_CALIBRATION_COMMAND 	= (byte) 0x57;
	public static final byte BMP180_CALIBRATION_COEFFICIENTS_RESPONSE = (byte) 0x58;
	public static final byte GET_BMP180_CALIBRATION_COEFFICIENTS_COMMAND = (byte) 0x59;
	public static final byte RESET_TO_DEFAULT_CONFIGURATION_COMMAND = (byte) 0x5A;
	public static final byte RESET_CALIBRATION_VALUE_COMMAND 		= (byte) 0x5B;
	public static final byte MPU9150_MAG_SENS_ADJ_VALS_RESPONSE 	= (byte) 0x5C;
	public static final byte GET_MPU9150_MAG_SENS_ADJ_VALS_COMMAND 	= (byte) 0x5D;
	public static final byte SET_INTERNAL_EXP_POWER_ENABLE_COMMAND 	= (byte) 0x5E;
	public static final byte INTERNAL_EXP_POWER_ENABLE_RESPONSE 	= (byte) 0x5F;
	public static final byte GET_INTERNAL_EXP_POWER_ENABLE_COMMAND 	= (byte) 0x60;
	public static final byte SET_EXG_REGS_COMMAND 					= (byte) 0x61;
	public static final byte EXG_REGS_RESPONSE 						= (byte) 0x62;
	public static final byte GET_EXG_REGS_COMMAND 					= (byte) 0x63;
	public static final byte DAUGHTER_CARD_ID_RESPONSE 				= (byte) 0x65;
	public static final byte GET_DAUGHTER_CARD_ID_COMMAND			= (byte) 0x66;
	public static final byte SET_BAUD_RATE_COMMAND 					= (byte) 0x6A;
	public static final byte BAUD_RATE_RESPONSE 					= (byte) 0x6B;
	public static final byte GET_BAUD_RATE_COMMAND 					= (byte) 0x6C;
	public static final byte START_SDBT_COMMAND 					= (byte) 0x70;
	public static final byte STATUS_RESPONSE	 					= (byte) 0x71;
	public static final byte GET_STATUS_COMMAND 					= (byte) 0x72;
	public static final byte DIR_RESPONSE		 					= (byte) 0x88;
	public static final byte GET_DIR_COMMAND 						= (byte) 0x89;
	public static final byte INSTREAM_CMD_RESPONSE 					= (byte) 0x8A;
	public static final byte ACK_COMMAND_PROCESSED            		= (byte) 0xFF;
	protected String mMyName="";														// This stores the user assigned name
	protected String mMyBluetoothAddress="";
	public static final int MAX_NUMBER_OF_SIGNALS = 40; //used to be 11 but now 13 because of the SR30 + 8 for 3d orientation
	public static final int MAX_INQUIRY_PACKET_SIZE = 47;
//	protected int mFWCode=0;

	public final static int MSP430_5XX_INFOMEM_D_ADDRESS = 0x001800; 
	public final static int MSP430_5XX_INFOMEM_C_ADDRESS = 0x001880; 
	public final static int MSP430_5XX_INFOMEM_B_ADDRESS = 0x001900;
	public final static int MSP430_5XX_INFOMEM_A_ADDRESS = 0x001980; 
	public final static int MSP430_5XX_INFOMEM_LAST_ADDRESS = 0x0019FF;
//	public final static int MSP430_5XX_PROGRAM_START_ADDRESS = 0x00FFFE; 
	
	public int mFirmwareVersionCode = 0;
	public int mFirmwareIndentifier = 0;
	public int mFirmwareVersionMajor = 0;
	public int mFirmwareVersionMinor = 0;
	public int mFirmwareVersionRelease = 0;
	public String mFirmwareVersionParsed = "";
	//		protected double mFWVersion;
//	protected int mFWMajorVersion;
//	protected int mFWMinorVersion;
//	protected int mFWInternal;
//	protected double mFWIdentifier;
//	protected String mFWVersionFullName="";
	
	protected String mClassName="Shimmer";
	private double mLastReceivedTimeStamp=0;
	protected double mCurrentTimeStampCycle=0;
	protected double mSamplingRate; 	                                        	// 51.2Hz is the default sampling rate 
	protected long mEnabledSensors = (long)0;												// This stores the enabled sensors


	protected int mPacketSize=0; 													// Default 2 bytes for time stamp and 6 bytes for accelerometer 
	protected int mAccelRange=0;													// This stores the current accelerometer range being used. The accelerometer range is stored during two instances, once an ack packet is received after a writeAccelRange(), and after a response packet has been received after readAccelRange()  	
//	protected boolean mLowPowerAccelWR = false; //TODO: add comment and set from BT command
	protected boolean mHighResAccelWR = false; //TODO: add comment and set from BT command
	protected int mLSM303MagRate=4;												// This stores the current Mag Sampling rate, it is a value between 0 and 6; 0 = 0.5 Hz; 1 = 1.0 Hz; 2 = 2.0 Hz; 3 = 5.0 Hz; 4 = 10.0 Hz; 5 = 20.0 Hz; 6 = 50.0 Hz
	protected int mLSM303DigitalAccelRate=0;
	protected int mMPU9150GyroAccelRate=0;
	protected int mMagRange=1;														// Currently not supported on Shimmer2. This stores the current Mag Range, it is a value between 0 and 6; 0 = 0.7 Ga; 1 = 1.0 Ga; 2 = 1.5 Ga; 3 = 2.0 Ga; 4 = 3.2 Ga; 5 = 3.8 Ga; 6 = 4.5 Ga
	protected int mGyroRange=1;													// This stores the current Gyro Range, it is a value between 0 and 3; 0 = +/- 250dps,1 = 500dps, 2 = 1000dps, 3 = 2000dps 
	protected int mMPU9150AccelRange=0;											// This stores the current MPU9150 Accel Range. 0 = 2g, 1 = 4g, 2 = 8g, 4 = 16g
	protected int mGSRRange=4;													// This stores the current GSR range being used.
	protected int mInternalExpPower=-1;													// This shows whether the internal exp power is enabled.
	protected long mConfigByte0;
	protected int mNChannels=0;	                                                // Default number of sensor channels set to three because of the on board accelerometer 
	protected int mBufferSize;                   							
	protected int mShimmerVersion=-1;
	public String mShimmerVersionParsed = "";

	protected String[] mSignalNameArray=new String[MAX_NUMBER_OF_SIGNALS];							// 19 is the maximum number of signal thus far
	protected String[] mSignalDataTypeArray=new String[MAX_NUMBER_OF_SIGNALS];						// 19 is the maximum number of signal thus far
	protected boolean mDefaultCalibrationParametersECG = true;
	protected boolean mDefaultCalibrationParametersEMG = true;
	protected boolean mDefaultCalibrationParametersAccel = true;
	protected boolean mDefaultCalibrationParametersDigitalAccel = true; //Also known as the wide range accelerometer
	protected int mPressureResolution = 0;
	protected int mExGResolution = 0;
	protected int mShimmer2MagRate=0;
	
	//TODO add comments and default values
	//protected int mDigitalAccelRange = 0;
	//protected int mAccelRange = 0;
//	protected int mAccelLPM = 0;
//	protected int mAccelHPM = 0;
//	protected int mMPU9150GyroRate = 0;
//	protected int mMPUAccelRange = 0;
	protected int mMPU9150DMP = 0;
	protected int mMPU9150LPF = 0;
	protected int mMPU9150MotCalCfg = 0;
	protected int mMPU9150MPLSamplingRate = 0;
	protected int mMPU9150MagSamplingRate = 0;
	protected int mMPLSensorFusion = 0;
	protected int mMPLGyroCalTC = 0;
	protected int mMPLVectCompCal = 0;
	protected int mMPLMagDistCal = 0;
	protected int mMPLEnable = 0;
	protected int mButtonStart = 0;
	protected int mMasterShimmer = 0;
	protected int mSingleTouch = 0;
	protected int mTCXO = 0;
	protected long mConfigTime; //this is in milliseconds, utc
	protected long mRTCDifference; //this is in ticks
	protected int mSyncTimeWhenLogging = 0;
	protected int mSyncBroadcastInterval = 0;

	protected long mInitialTimeStamp;

	
	//TODO: ASK JC ABOUT BELOW, indexes wrong? Can we just use new ones above like (FW_ID_SHIMMER3_BTSTREAM)
	protected final static int FW_IDEN_BTSTREAM=0;
	protected final static int FW_IDEN_SD=1;

	
	protected String mShimmerUserAssignedName = "";  //TODO: this seems to be a duplicate of mMyName and ?devicename?
	protected String mExperimentName = ""; //TODO: this seems to be a duplicate of mMyTrial
	protected int mExperimentId = 0;
	protected int mExperimentNumberOfShimmers = 0;
	protected int mExperimentDurationEstimated = 0;
	protected int mExperimentDurationMaximum = 0;
	protected String mMacIdFromInfoMem = "";
	protected boolean mConfigFileCreationFlag = false;
	protected List<String> syncNodesList = new ArrayList<String>();
	
//	protected byte[] mShimmerInfoMemBytes = new byte[512];
	protected byte[] mShimmerInfoMemBytes = createEmptyInfoMemByteArray(512);
	


	//
	protected double[][] mAlignmentMatrixAnalogAccel = {{-1,0,0},{0,-1,0},{0,0,1}}; 			
	protected double[][] mSensitivityMatrixAnalogAccel = {{38,0,0},{0,38,0},{0,0,38}}; 	
	protected double[][] mOffsetVectorAnalogAccel = {{2048},{2048},{2048}};				
	protected abstract void checkBattery();
	protected double[][] mAlignmentMatrixWRAccel = {{-1,0,0},{0,1,0},{0,0,-1}}; 			
	protected double[][] mSensitivityMatrixWRAccel = {{1631,0,0},{0,1631,0},{0,0,1631}}; 	
	protected double[][] mOffsetVectorWRAccel = {{0},{0},{0}};	

	//Default values Shimmer2 
	protected static double[][] SensitivityMatrixAccel1p5gShimmer2 = {{101,0,0},{0,101,0},{0,0,101}};
	protected static double[][] SensitivityMatrixAccel2gShimmer2 = {{76,0,0},{0,76,0},{0,0,76}};
	protected static double[][] SensitivityMatrixAccel4gShimmer2 = {{38,0,0},{0,38,0},{0,0,38}};
	protected static double[][] SensitivityMatrixAccel6gShimmer2 = {{25,0,0},{0,25,0},{0,0,25}};
	protected static double[][] AlignmentMatrixAccelShimmer2 =  {{-1,0,0},{0,-1,0},{0,0,1}}; 			
	protected static double[][] OffsetVectorAccelShimmer2 = {{2048},{2048},{2048}};			
	//Shimmer3
	protected static double[][] SensitivityMatrixLowNoiseAccel2gShimmer3 = {{83,0,0},{0,83,0},{0,0,83}};
	protected static double[][] SensitivityMatrixWideRangeAccel2gShimmer3 = {{1631,0,0},{0,1631,0},{0,0,1631}};
	protected static double[][] SensitivityMatrixWideRangeAccel4gShimmer3 = {{815,0,0},{0,815,0},{0,0,815}};
	protected static double[][] SensitivityMatrixWideRangeAccel8gShimmer3 = {{408,0,0},{0,408,0},{0,0,408}};
	protected static double[][] SensitivityMatrixWideRangeAccel16gShimmer3 = {{135,0,0},{0,135,0},{0,0,135}};
	protected static double[][] AlignmentMatrixLowNoiseAccelShimmer3 = {{0,-1,0},{-1,0,0},{0,0,-1}};

	protected static double[][] AlignmentMatrixWideRangeAccelShimmer3 = {{-1,0,0},{0,1,0},{0,0,-1}}; 	
	protected static double[][] AlignmentMatrixAccelShimmer3 = {{0,-1,0},{-1,0,0},{0,0,-1}};
	protected static double[][] OffsetVectorLowNoiseAccelShimmer3 = {{2047},{2047},{2047}};
	protected static double[][] OffsetVectorWideRangeAccelShimmer3 = {{0},{0},{0}};

	protected double OffsetECGRALL=2060;
	protected double GainECGRALL=175;
	protected double OffsetECGLALL=2060;
	protected double GainECGLALL=175;
	protected double OffsetEMG=2060;
	protected double GainEMG=750;

	protected boolean mDefaultCalibrationParametersGyro = true;
	protected double[][] mAlignmentMatrixGyroscope = {{0,-1,0},{-1,0,0},{0,0,-1}}; 				
	protected double[][] mSensitivityMatrixGyroscope = {{2.73,0,0},{0,2.73,0},{0,0,2.73}}; 		
	protected double[][] mOffsetVectorGyroscope = {{1843},{1843},{1843}};						

	//Default values Shimmer2
	protected static double[][] AlignmentMatrixGyroShimmer2 = {{0,-1,0},{-1,0,0},{0,0,-1}}; 				
	protected static double[][] SensitivityMatrixGyroShimmer2 = {{2.73,0,0},{0,2.73,0},{0,0,2.73}}; 		
	protected static double[][] OffsetVectorGyroShimmer2 = {{1843},{1843},{1843}};
	//Shimmer3
	protected static double[][] SensitivityMatrixGyro250dpsShimmer3 = {{131,0,0},{0,131,0},{0,0,131}};
	protected static double[][] SensitivityMatrixGyro500dpsShimmer3 = {{65.5,0,0},{0,65.5,0},{0,0,65.5}};
	protected static double[][] SensitivityMatrixGyro1000dpsShimmer3 = {{32.8,0,0},{0,32.8,0},{0,0,32.8}};
	protected static double[][] SensitivityMatrixGyro2000dpsShimmer3 = {{16.4,0,0},{0,16.4,0},{0,0,16.4}};
	protected static double[][] AlignmentMatrixGyroShimmer3 = {{0,-1,0},{-1,0,0},{0,0,-1}}; 				
	protected static double[][] OffsetVectorGyroShimmer3 = {{0},{0},{0}};		

	protected int mCurrentLEDStatus=0;
	protected boolean mDefaultCalibrationParametersMag = true;
	protected double[][] mAlignmentMatrixMagnetometer = {{1,0,0},{0,1,0},{0,0,-1}}; 				
	protected double[][] mSensitivityMatrixMagnetometer = {{580,0,0},{0,580,0},{0,0,580}}; 		
	protected double[][] mOffsetVectorMagnetometer = {{0},{0},{0}};								

	//Default values Shimmer2 and Shimmer3
	protected static double[][] AlignmentMatrixMagShimmer2 = {{1,0,0},{0,1,0},{0,0,-1}};
	protected static double[][] SensitivityMatrixMagShimmer2 = {{580,0,0},{0,580,0},{0,0,580}}; 		
	protected static double[][] OffsetVectorMagShimmer2 = {{0},{0},{0}};				
	//Shimmer3
	protected static double[][] AlignmentMatrixMagShimmer3 = {{-1,0,0},{0,1,0},{0,0,-1}}; 				
	protected static double[][] SensitivityMatrixMagShimmer3 = {{1100,0,0},{0,1100,0},{0,0,980}}; 		
	protected static double[][] OffsetVectorMagShimmer3 = {{0},{0},{0}};		

	
	protected double[][] AlignmentMatrixMPLAccel = {{-1,0,0},{0,1,0},{0,0,-1}}; 			
	protected double[][] SensitivityMatrixMPLAccel = {{1631,0,0},{0,1631,0},{0,0,1631}}; 	
	protected double[][] OffsetVectorMPLAccel = {{0},{0},{0}};
	
	protected double[][] AlignmentMatrixMPLMag = {{-1,0,0},{0,1,0},{0,0,-1}}; 			
	protected double[][] SensitivityMatrixMPLMag = {{1631,0,0},{0,1631,0},{0,0,1631}}; 	
	protected double[][] OffsetVectorMPLMag = {{0},{0},{0}};
	
	protected double[][] AlignmentMatrixMPLGyro = {{-1,0,0},{0,1,0},{0,0,-1}}; 			
	protected double[][] SensitivityMatrixMPLGyro = {{1631,0,0},{0,1631,0},{0,0,1631}}; 	
	protected double[][] OffsetVectorMPLGyro = {{0},{0},{0}};
	
	
	
	
	protected static double[][] SensitivityMatrixMag1p3GaShimmer3 = {{1100,0,0},{0,1100,0},{0,0,980}};
	protected static double[][] SensitivityMatrixMag1p9GaShimmer3 = {{855,0,0},{0,855,0},{0,0,760}};
	protected static double[][] SensitivityMatrixMag2p5GaShimmer3 = {{670,0,0},{0,670,0},{0,0,600}};
	protected static double[][] SensitivityMatrixMag4GaShimmer3 = {{450,0,0},{0,450,0},{0,0,400}};
	protected static double[][] SensitivityMatrixMag4p7GaShimmer3 = {{400,0,0},{0,400,0},{0,0,355}};
	protected static double[][] SensitivityMatrixMag5p6GaShimmer3 = {{330,0,0},{0,330,0},{0,0,295}};
	protected static double[][] SensitivityMatrixMag8p1GaShimmer3 = {{230,0,0},{0,230,0},{0,0,205}};

	protected static double[][] SensitivityMatrixMag0p8GaShimmer2 = {{1370,0,0},{0,1370,0},{0,0,1370}};
	protected static double[][] SensitivityMatrixMag1p3GaShimmer2 = {{1090,0,0},{0,1090,0},{0,0,1090}};
	protected static double[][] SensitivityMatrixMag1p9GaShimmer2 = {{820,0,0},{0,820,0},{0,0,820}};
	protected static double[][] SensitivityMatrixMag2p5GaShimmer2 = {{660,0,0},{0,660,0},{0,0,660}};
	protected static double[][] SensitivityMatrixMag4p0GaShimmer2 = {{440,0,0},{0,440,0},{0,0,440}};
	protected static double[][] SensitivityMatrixMag4p7GaShimmer2 = {{390,0,0},{0,390,0},{0,0,390}};
	protected static double[][] SensitivityMatrixMag5p6GaShimmer2 = {{330,0,0},{0,330,0},{0,0,330}};
	protected static double[][] SensitivityMatrixMag8p1GaShimmer2 = {{230,0,0},{0,230,0},{0,0,230}};


	protected double AC1 = 408;
	protected double AC2 = -72;
	protected double AC3 = -14383;
	protected double AC4 = 332741;
	protected double AC5 = 32757;
	protected double AC6 = 23153;
	protected double B1 = 6190;
	protected double B2 = 4;
	protected double MB = -32767;
	protected double MC = -8711;
	protected double MD = 2868;

	protected boolean mLowPowerMag = false;
	protected boolean mLowPowerAccelWR = false;
	protected boolean mLowPowerGyro = false;
	
	protected long mPacketLossCount=0;
	protected double mPacketReceptionRate=100;
	protected double mLastReceivedCalibratedTimeStamp=-1; 
	protected boolean mFirstTimeCalTime=true;
	protected double mCalTimeStart;
	private double mLastKnownHeartRate=0;
	protected DescriptiveStatistics mVSenseBattMA= new DescriptiveStatistics(1024);
	Quat4d mQ = new Quat4d();
	GradDes3DOrientation mOrientationAlgo;
	protected boolean mOrientationEnabled = false;
	protected boolean mEnableOntheFlyGyroOVCal = false;

	protected double mGyroOVCalThreshold = 1.2;
	DescriptiveStatistics mGyroX;
	DescriptiveStatistics mGyroY;
	DescriptiveStatistics mGyroZ;
	DescriptiveStatistics mGyroXRaw;
	DescriptiveStatistics mGyroYRaw;
	DescriptiveStatistics mGyroZRaw;
	protected boolean mEnableCalibration = true;
	protected byte[] mInquiryResponseBytes;
	protected boolean mStreaming =false;											// This is used to monitor whether the device is in streaming mode
	//all raw params should start with a 1 byte identifier in position [0]
	protected byte[] mAccelCalRawParams = new byte[22];
	protected byte[] mDigiAccelCalRawParams  = new byte[22];
	protected byte[] mGyroCalRawParams  = new byte[22];
	protected byte[] mMagCalRawParams  = new byte[22];
	protected byte[] mPressureRawParams  = new byte[23];
	protected byte[] mEMGCalRawParams  = new byte[13];
	protected byte[] mECGCalRawParams = new byte[13];
	protected byte[] mPressureCalRawParams = new byte[23];

	//EXG
	protected byte[] mEXG1Register = new byte[10];
	protected byte[] mEXG2Register = new byte[10];
	protected int mEXG1RateSetting; //setting not value
	protected int mEXG1CH1GainSetting; // this is the setting not to be confused with the actual value
	protected int mEXG1CH1GainValue; // this is the value
	protected int mEXG1CH2GainSetting; // this is the setting not to be confused with the actual value
	protected int mEXG1CH2GainValue; // this is the value
	protected int mEXG2RateSetting; //setting not value
	protected int mEXG2CH1GainSetting; // this is the setting not to be confused with the actual value
	protected int mEXG2CH1GainValue; // this is the value
	protected int mEXG2CH2GainSetting; // this is the setting not to be confused with the actual value
	protected int mEXG2CH2GainValue; // this is the value
	protected static final int EXG_CHIP1 = 0;
	protected static final int EXG_CHIP2 = 1;
	//EXG ADVANCED
	protected int mRefenceElectrode=-1;
	protected int mLeadOffDetectionMode;
	protected int mLeadOffCurrentModeChip1;
	protected int mLeadOffCurrentModeChip2;
	protected int mComparatorsChip1;
	protected int mComparatorsChip2;
	protected int mRLDSense;
	protected int m2P1N1P;
	protected int m2P;
	protected int mLeadOffDetectionCurrent;
	protected int mLeadOffComparatorTreshold;	
	
	protected int mBluetoothBaudRate=0;
	protected byte[] mExpBoardArray; // Array where the expansion board response is stored
	protected String mExpBoardName; // Name of the expansion board. ONLY SHIMMER 3
	
	//This features are only used in LogAndStream FW 
	protected String mDirectoryName;
	protected int mDirectoryNameLength;
	protected boolean mSensingStatus;
	protected boolean mDockedStatus;
	private List<String[]> mExtraSignalProperties = null;

	protected ObjectCluster buildMsg(byte[] newPacket, int fwIdentifier, int timeSync) {
		ObjectCluster objectCluster = new ObjectCluster();
		objectCluster.mMyName = mMyName;
		objectCluster.mBluetoothAddress = mMyBluetoothAddress;
		double [] calibratedData;
		double [] uncalibratedData;
		String [] uncalibratedDataUnits;
		String [] calibratedDataUnits;
		String [] sensorNames;
		if (fwIdentifier == FW_IDEN_BTSTREAM){
			objectCluster.mSystemTimeStamp=ByteBuffer.allocate(8).putLong(System.currentTimeMillis()).array();
			calibratedData=new double[mNChannels + 1]; //plus 1 because of the time stamp
			uncalibratedData=new double[mNChannels + 1]; //plus 1 because of the time stamp	
			uncalibratedDataUnits = new String[mNChannels + 1];
			calibratedDataUnits = new String[mNChannels + 1];
			sensorNames = new String[mNChannels + 1];
		} else {
			if (mRTCDifference == 0){
				calibratedData=new double[mNChannels]; //sd log time stamp already included in mnChannels
				uncalibratedData=new double[mNChannels];
				uncalibratedDataUnits = new String[mNChannels];
				calibratedDataUnits = new String[mNChannels];
				sensorNames = new String[mNChannels];
			} else {
				calibratedData=new double[mNChannels+1]; //for RTC
				uncalibratedData=new double[mNChannels+1];
				uncalibratedDataUnits = new String[mNChannels+1];
				calibratedDataUnits = new String[mNChannels+1];
				sensorNames = new String[mNChannels+1];
			}
		}
		
		System.arraycopy(mSignalNameArray, 0, sensorNames, 0, sensorNames.length);
		
		//PARSE DATA
		long[] newPacketInt=parsedData(newPacket,mSignalDataTypeArray);
		
		double[] tempData=new double[3];
		Vector3d accelerometer = new Vector3d();
		Vector3d magnetometer = new Vector3d();
		Vector3d gyroscope = new Vector3d();
		
		
		
		int iTimeStamp=getSignalIndex("TimeStamp"); //find index
		if(mFirstTime && fwIdentifier == FW_IDEN_SD){
			//this is to make sure the Raw starts from zero
			mFirstRawTS = (double)newPacketInt[iTimeStamp];
			mFirstTime = false;
		}
		double calibratedTS = calibrateTimeStamp((double)newPacketInt[iTimeStamp]);
		
		//TIMESTAMP
		if (fwIdentifier == FW_IDEN_SD){
			// RTC timestamp uncal. (shimmer timestamp + RTC offset from header); unit = ticks
			double unwrappedrawtimestamp = calibratedTS*32768/1000;
			unwrappedrawtimestamp = unwrappedrawtimestamp - mFirstRawTS; //deduct this so it will start from 0
			long sdlograwtimestamp = (long)mInitialTimeStamp + (long)unwrappedrawtimestamp;
			objectCluster.mPropertyCluster.put("Timestamp",new FormatCluster("RAW",CLOCK_UNIT,(double)sdlograwtimestamp));
			uncalibratedData[iTimeStamp] = (double)sdlograwtimestamp;
			uncalibratedDataUnits[iTimeStamp] = CLOCK_UNIT;
			
			if (mEnableCalibration){
				double sdlogcaltimestamp = (double)sdlograwtimestamp/32768*1000;
				objectCluster.mPropertyCluster.put("Timestamp",new FormatCluster("CAL","mSecs",sdlogcaltimestamp));
				calibratedData[iTimeStamp] = sdlogcaltimestamp;
				calibratedDataUnits[iTimeStamp] = "mSecs";
			}
		} else if (fwIdentifier == FW_IDEN_BTSTREAM){
			objectCluster.mPropertyCluster.put("Timestamp",new FormatCluster("RAW",NO_UNIT,(double)newPacketInt[iTimeStamp]));
			uncalibratedData[iTimeStamp] = (double)newPacketInt[iTimeStamp];
			uncalibratedDataUnits[iTimeStamp] = NO_UNIT;
			if (mEnableCalibration){
				objectCluster.mPropertyCluster.put("Timestamp",new FormatCluster("CAL","mSecs",calibratedTS));
				calibratedData[iTimeStamp] = calibratedTS;
				calibratedDataUnits[iTimeStamp] = "mSecs";
			}
		}
		
		//RAW RTC
		if ((fwIdentifier == FW_IDEN_SD) && mRTCDifference!=0)
		{
			double unwrappedrawtimestamp = calibratedTS*32768/1000;
			unwrappedrawtimestamp = unwrappedrawtimestamp - mFirstRawTS; //deduct this so it will start from 0
			long rtctimestamp = (long)mInitialTimeStamp + (long)unwrappedrawtimestamp + mRTCDifference;
			objectCluster.mPropertyCluster.put(Shimmer3Configuration.REAL_TIME_CLOCK_OBJECTCLUSTER_SENSORNAME,new FormatCluster("RAW",CLOCK_UNIT,(double)rtctimestamp));
			uncalibratedData[sensorNames.length-1] = (double)rtctimestamp;
			uncalibratedDataUnits[sensorNames.length-1] = CLOCK_UNIT;
			sensorNames[sensorNames.length-1]= Shimmer3Configuration.REAL_TIME_CLOCK_OBJECTCLUSTER_SENSORNAME;
			if (mEnableCalibration){
				double rtctimestampcal = (mInitialTimeStamp/32768*1000) + calibratedTS + (mRTCDifference/32768*1000) - (mFirstRawTS/32768*1000);
				objectCluster.mPropertyCluster.put(Shimmer3Configuration.REAL_TIME_CLOCK_OBJECTCLUSTER_SENSORNAME,new FormatCluster("CAL","mSecs",rtctimestampcal));
				calibratedData[sensorNames.length-1] = rtctimestampcal;
				calibratedDataUnits[sensorNames.length-1] = "mSecs";
			}
		}
		
		//OFFSET
		if(timeSync==1 && (fwIdentifier == FW_IDEN_SD)){
			int iOffset=getSignalIndex("Offset"); //find index
			double offsetValue = Double.NaN;
			if (OFFSET_LENGTH==9){
				if(newPacketInt[iOffset] == 1152921504606846975L){
					offsetValue=Double.NaN;
				} else {
					offsetValue=(double)newPacketInt[iOffset];
				}	
			}
			else{
				if(newPacketInt[iOffset] == 4294967295L){
					offsetValue=Double.NaN;
				} else {
					offsetValue=(double)newPacketInt[iOffset];
				}
			}
			
			objectCluster.mPropertyCluster.put("Offset",new FormatCluster("RAW",NO_UNIT,offsetValue));
			uncalibratedData[iOffset] = offsetValue;
			calibratedData[iOffset] = Double.NaN;
			uncalibratedDataUnits[iOffset] = NO_UNIT;
			calibratedDataUnits[iOffset] = NO_UNIT;
		} 
		
		
		objectCluster = callAdditionalServices(objectCluster);
		
		
		//first get raw and calibrated data, this is data derived from the Shimmer device and involves no involvement from the API
		

		if (((fwIdentifier == FW_IDEN_BTSTREAM) && (mEnabledSensors & BTStream.ACCEL_LN) > 0) 
				|| ((fwIdentifier == FW_IDEN_SD) && (mEnabledSensors & SDLogHeader.ACCEL_LN) > 0)
				){
			int iAccelX=getSignalIndex(Shimmer3Configuration.ACCEL_LN_X_OBJECTCLUSTER_SENSORNAME); //find index
			int iAccelY=getSignalIndex(Shimmer3Configuration.ACCEL_LN_Y_OBJECTCLUSTER_SENSORNAME); //find index
			int iAccelZ=getSignalIndex(Shimmer3Configuration.ACCEL_LN_Z_OBJECTCLUSTER_SENSORNAME); //find index
			tempData[0]=(double)newPacketInt[iAccelX];
			tempData[1]=(double)newPacketInt[iAccelY];
			tempData[2]=(double)newPacketInt[iAccelZ];

			objectCluster.mPropertyCluster.put(Shimmer3Configuration.ACCEL_LN_X_OBJECTCLUSTER_SENSORNAME,new FormatCluster("RAW",NO_UNIT,(double)newPacketInt[iAccelX]));
			objectCluster.mPropertyCluster.put(Shimmer3Configuration.ACCEL_LN_Y_OBJECTCLUSTER_SENSORNAME,new FormatCluster("RAW",NO_UNIT,(double)newPacketInt[iAccelY]));
			objectCluster.mPropertyCluster.put(Shimmer3Configuration.ACCEL_LN_Z_OBJECTCLUSTER_SENSORNAME,new FormatCluster("RAW",NO_UNIT,(double)newPacketInt[iAccelZ]));
			uncalibratedData[iAccelX]=(double)newPacketInt[iAccelX];
			uncalibratedData[iAccelY]=(double)newPacketInt[iAccelY];
			uncalibratedData[iAccelZ]=(double)newPacketInt[iAccelZ];
			uncalibratedDataUnits[iAccelX]=NO_UNIT;
			uncalibratedDataUnits[iAccelY]=NO_UNIT;
			uncalibratedDataUnits[iAccelZ]=NO_UNIT;
			
			
			if (mEnableCalibration){
				double[] accelCalibratedData;
				accelCalibratedData=calibrateInertialSensorData(tempData, mAlignmentMatrixAnalogAccel, mSensitivityMatrixAnalogAccel, mOffsetVectorAnalogAccel);
				calibratedData[iAccelX]=accelCalibratedData[0];
				calibratedData[iAccelY]=accelCalibratedData[1];
				calibratedData[iAccelZ]=accelCalibratedData[2];
				
				if (mDefaultCalibrationParametersAccel == true) {
					objectCluster.mPropertyCluster.put(Shimmer3Configuration.ACCEL_LN_X_OBJECTCLUSTER_SENSORNAME,new FormatCluster("CAL",ACCEL_DEFAULT_CAL_UNIT,accelCalibratedData[0]));
					objectCluster.mPropertyCluster.put(Shimmer3Configuration.ACCEL_LN_Y_OBJECTCLUSTER_SENSORNAME,new FormatCluster("CAL",ACCEL_DEFAULT_CAL_UNIT,accelCalibratedData[1]));
					objectCluster.mPropertyCluster.put(Shimmer3Configuration.ACCEL_LN_Z_OBJECTCLUSTER_SENSORNAME,new FormatCluster("CAL",ACCEL_DEFAULT_CAL_UNIT,accelCalibratedData[2]));
					calibratedDataUnits[iAccelX]=ACCEL_DEFAULT_CAL_UNIT;
					calibratedDataUnits[iAccelY]=ACCEL_DEFAULT_CAL_UNIT;
					calibratedDataUnits[iAccelZ]=ACCEL_DEFAULT_CAL_UNIT;
					accelerometer.x=accelCalibratedData[0];
					accelerometer.y=accelCalibratedData[1];
					accelerometer.z=accelCalibratedData[2];

				} else {
					objectCluster.mPropertyCluster.put(Shimmer3Configuration.ACCEL_LN_X_OBJECTCLUSTER_SENSORNAME,new FormatCluster("CAL",ACCEL_CAL_UNIT,accelCalibratedData[0]));
					objectCluster.mPropertyCluster.put(Shimmer3Configuration.ACCEL_LN_Y_OBJECTCLUSTER_SENSORNAME,new FormatCluster("CAL",ACCEL_CAL_UNIT,accelCalibratedData[1]));
					objectCluster.mPropertyCluster.put(Shimmer3Configuration.ACCEL_LN_Z_OBJECTCLUSTER_SENSORNAME,new FormatCluster("CAL",ACCEL_CAL_UNIT,accelCalibratedData[2]));
					calibratedDataUnits[iAccelX] = ACCEL_CAL_UNIT;
					calibratedDataUnits[iAccelY] = ACCEL_CAL_UNIT;
					calibratedDataUnits[iAccelZ] = ACCEL_CAL_UNIT;
					accelerometer.x=accelCalibratedData[0];
					accelerometer.y=accelCalibratedData[1];
					accelerometer.z=accelCalibratedData[2];

				}
			}
		}
		if (((fwIdentifier == FW_IDEN_BTSTREAM) && (mEnabledSensors & BTStream.ACCEL_WR) > 0) 
				|| ((fwIdentifier == FW_IDEN_SD) && (mEnabledSensors & SDLogHeader.ACCEL_WR) > 0)
				){
			int iAccelX=getSignalIndex(Shimmer3Configuration.ACCEL_WR_X_OBJECTCLUSTER_SENSORNAME); //find index
			int iAccelY=getSignalIndex(Shimmer3Configuration.ACCEL_WR_Y_OBJECTCLUSTER_SENSORNAME); //find index
			int iAccelZ=getSignalIndex(Shimmer3Configuration.ACCEL_WR_Z_OBJECTCLUSTER_SENSORNAME); //find index
			//check range

			tempData[0]=(double)newPacketInt[iAccelX];
			tempData[1]=(double)newPacketInt[iAccelY];
			tempData[2]=(double)newPacketInt[iAccelZ];
			objectCluster.mPropertyCluster.put(Shimmer3Configuration.ACCEL_WR_X_OBJECTCLUSTER_SENSORNAME,new FormatCluster("RAW",NO_UNIT,(double)newPacketInt[iAccelX]));
			objectCluster.mPropertyCluster.put(Shimmer3Configuration.ACCEL_WR_Y_OBJECTCLUSTER_SENSORNAME,new FormatCluster("RAW",NO_UNIT,(double)newPacketInt[iAccelY]));
			objectCluster.mPropertyCluster.put(Shimmer3Configuration.ACCEL_WR_Z_OBJECTCLUSTER_SENSORNAME,new FormatCluster("RAW",NO_UNIT,(double)newPacketInt[iAccelZ]));
			uncalibratedData[iAccelX]=(double)newPacketInt[iAccelX];
			uncalibratedData[iAccelY]=(double)newPacketInt[iAccelY];
			uncalibratedData[iAccelZ]=(double)newPacketInt[iAccelZ];
			uncalibratedDataUnits[iAccelX]=NO_UNIT;
			uncalibratedDataUnits[iAccelY]=NO_UNIT;
			uncalibratedDataUnits[iAccelZ]=NO_UNIT;

			if (mEnableCalibration){
				double[] accelCalibratedData;
				accelCalibratedData=calibrateInertialSensorData(tempData, mAlignmentMatrixWRAccel, mSensitivityMatrixWRAccel, mOffsetVectorWRAccel);
				calibratedData[iAccelX]=accelCalibratedData[0];
				calibratedData[iAccelY]=accelCalibratedData[1];
				calibratedData[iAccelZ]=accelCalibratedData[2];
				if (mDefaultCalibrationParametersDigitalAccel == true) {
					objectCluster.mPropertyCluster.put(Shimmer3Configuration.ACCEL_WR_X_OBJECTCLUSTER_SENSORNAME,new FormatCluster("CAL",ACCEL_DEFAULT_CAL_UNIT,calibratedData[iAccelX]));
					objectCluster.mPropertyCluster.put(Shimmer3Configuration.ACCEL_WR_Y_OBJECTCLUSTER_SENSORNAME,new FormatCluster("CAL",ACCEL_DEFAULT_CAL_UNIT,calibratedData[iAccelY]));
					objectCluster.mPropertyCluster.put(Shimmer3Configuration.ACCEL_WR_Z_OBJECTCLUSTER_SENSORNAME,new FormatCluster("CAL",ACCEL_DEFAULT_CAL_UNIT,calibratedData[iAccelZ]));
					calibratedDataUnits[iAccelX]=ACCEL_DEFAULT_CAL_UNIT;
					calibratedDataUnits[iAccelY]=ACCEL_DEFAULT_CAL_UNIT;
					calibratedDataUnits[iAccelZ]=ACCEL_DEFAULT_CAL_UNIT;
					if (((mEnabledSensors & SENSOR_ACCEL) == 0)){
						accelerometer.x=accelCalibratedData[0]; //this is used to calculate quaternions // skip if Low noise is enabled
						accelerometer.y=accelCalibratedData[1];
						accelerometer.z=accelCalibratedData[2];
					}
				} else {
					objectCluster.mPropertyCluster.put(Shimmer3Configuration.ACCEL_WR_X_OBJECTCLUSTER_SENSORNAME,new FormatCluster("CAL",ACCEL_CAL_UNIT,calibratedData[iAccelX]));
					objectCluster.mPropertyCluster.put(Shimmer3Configuration.ACCEL_WR_Y_OBJECTCLUSTER_SENSORNAME,new FormatCluster("CAL",ACCEL_CAL_UNIT,calibratedData[iAccelY]));
					objectCluster.mPropertyCluster.put(Shimmer3Configuration.ACCEL_WR_Z_OBJECTCLUSTER_SENSORNAME,new FormatCluster("CAL",ACCEL_CAL_UNIT,calibratedData[iAccelZ]));
					calibratedDataUnits[iAccelX]=ACCEL_CAL_UNIT;
					calibratedDataUnits[iAccelY]=ACCEL_CAL_UNIT;
					calibratedDataUnits[iAccelZ]=ACCEL_CAL_UNIT;
					if (((mEnabledSensors & SENSOR_ACCEL) == 0)){
						accelerometer.x=accelCalibratedData[0];
						accelerometer.y=accelCalibratedData[1];
						accelerometer.z=accelCalibratedData[2];
					}

				}	
			}
		}
		if (((fwIdentifier == FW_IDEN_BTSTREAM) && (mEnabledSensors & BTStream.GYRO) > 0) 
				|| ((fwIdentifier == FW_IDEN_SD) && (mEnabledSensors & SDLogHeader.GYRO) > 0)
				) {
			int iGyroX=getSignalIndex(Shimmer3Configuration.GYRO_X_OBJECTCLUSTER_SENSORNAME);
			int iGyroY=getSignalIndex(Shimmer3Configuration.GYRO_Y_OBJECTCLUSTER_SENSORNAME);
			int iGyroZ=getSignalIndex(Shimmer3Configuration.GYRO_Z_OBJECTCLUSTER_SENSORNAME);
			tempData[0]=(double)newPacketInt[iGyroX];
			tempData[1]=(double)newPacketInt[iGyroY];
			tempData[2]=(double)newPacketInt[iGyroZ];


			objectCluster.mPropertyCluster.put(Shimmer3Configuration.GYRO_X_OBJECTCLUSTER_SENSORNAME,new FormatCluster("RAW",NO_UNIT,(double)newPacketInt[iGyroX]));
			objectCluster.mPropertyCluster.put(Shimmer3Configuration.GYRO_Y_OBJECTCLUSTER_SENSORNAME,new FormatCluster("RAW",NO_UNIT,(double)newPacketInt[iGyroY]));
			objectCluster.mPropertyCluster.put(Shimmer3Configuration.GYRO_Z_OBJECTCLUSTER_SENSORNAME,new FormatCluster("RAW",NO_UNIT,(double)newPacketInt[iGyroZ]));
			uncalibratedData[iGyroX]=(double)newPacketInt[iGyroX];
			uncalibratedData[iGyroY]=(double)newPacketInt[iGyroY];
			uncalibratedData[iGyroZ]=(double)newPacketInt[iGyroZ];
			uncalibratedDataUnits[iGyroX]=NO_UNIT;
			uncalibratedDataUnits[iGyroY]=NO_UNIT;
			uncalibratedDataUnits[iGyroZ]=NO_UNIT;
			if (mEnableCalibration){
				double[] gyroCalibratedData=calibrateInertialSensorData(tempData, mAlignmentMatrixGyroscope, mSensitivityMatrixGyroscope, mOffsetVectorGyroscope);
				calibratedData[iGyroX]=gyroCalibratedData[0];
				calibratedData[iGyroY]=gyroCalibratedData[1];
				calibratedData[iGyroZ]=gyroCalibratedData[2];
				if (mDefaultCalibrationParametersGyro == true) {
					objectCluster.mPropertyCluster.put(Shimmer3Configuration.GYRO_X_OBJECTCLUSTER_SENSORNAME,new FormatCluster("CAL",GYRO_DEFAULT_CAL_UNIT,gyroCalibratedData[0]));
					objectCluster.mPropertyCluster.put(Shimmer3Configuration.GYRO_Y_OBJECTCLUSTER_SENSORNAME,new FormatCluster("CAL",GYRO_DEFAULT_CAL_UNIT,gyroCalibratedData[1]));
					objectCluster.mPropertyCluster.put(Shimmer3Configuration.GYRO_Z_OBJECTCLUSTER_SENSORNAME,new FormatCluster("CAL",GYRO_DEFAULT_CAL_UNIT,gyroCalibratedData[2]));
					gyroscope.x=gyroCalibratedData[0]*Math.PI/180;
					gyroscope.y=gyroCalibratedData[1]*Math.PI/180;
					gyroscope.z=gyroCalibratedData[2]*Math.PI/180;
					calibratedDataUnits[iGyroX]=GYRO_DEFAULT_CAL_UNIT;
					calibratedDataUnits[iGyroY]=GYRO_DEFAULT_CAL_UNIT;
					calibratedDataUnits[iGyroZ]=GYRO_DEFAULT_CAL_UNIT;
				} else {
					objectCluster.mPropertyCluster.put(Shimmer3Configuration.GYRO_X_OBJECTCLUSTER_SENSORNAME,new FormatCluster("CAL",GYRO_CAL_UNIT,gyroCalibratedData[0]));
					objectCluster.mPropertyCluster.put(Shimmer3Configuration.GYRO_Y_OBJECTCLUSTER_SENSORNAME,new FormatCluster("CAL",GYRO_CAL_UNIT,gyroCalibratedData[1]));
					objectCluster.mPropertyCluster.put(Shimmer3Configuration.GYRO_Z_OBJECTCLUSTER_SENSORNAME,new FormatCluster("CAL",GYRO_CAL_UNIT,gyroCalibratedData[2]));
					gyroscope.x=gyroCalibratedData[0]*Math.PI/180;
					gyroscope.y=gyroCalibratedData[1]*Math.PI/180;
					gyroscope.z=gyroCalibratedData[2]*Math.PI/180;
					calibratedDataUnits[iGyroX]=GYRO_CAL_UNIT;
					calibratedDataUnits[iGyroY]=GYRO_CAL_UNIT;
					calibratedDataUnits[iGyroZ]=GYRO_CAL_UNIT;
					
				} 
				if (mEnableOntheFlyGyroOVCal){
					mGyroX.addValue(gyroCalibratedData[0]);
					mGyroY.addValue(gyroCalibratedData[1]);
					mGyroZ.addValue(gyroCalibratedData[2]);
					mGyroXRaw.addValue((double)newPacketInt[iGyroX]);
					mGyroYRaw.addValue((double)newPacketInt[iGyroY]);
					mGyroZRaw.addValue((double)newPacketInt[iGyroZ]);
					if (mGyroX.getStandardDeviation()<mGyroOVCalThreshold && mGyroY.getStandardDeviation()<mGyroOVCalThreshold && mGyroZ.getStandardDeviation()<mGyroOVCalThreshold){
						mOffsetVectorGyroscope[0][0]=mGyroXRaw.getMean();
						mOffsetVectorGyroscope[1][0]=mGyroYRaw.getMean();
						mOffsetVectorGyroscope[2][0]=mGyroZRaw.getMean();
					}
				}
			}

		}
		if (((fwIdentifier == FW_IDEN_BTSTREAM) && (mEnabledSensors & BTStream.MAG) > 0) 
				|| ((fwIdentifier == FW_IDEN_SD) && (mEnabledSensors & SDLogHeader.MAG) > 0)
				) {
			int iMagX=getSignalIndex(Shimmer3Configuration.MAG_X_OBJECTCLUSTER_SENSORNAME);
			int iMagY=getSignalIndex(Shimmer3Configuration.MAG_Y_OBJECTCLUSTER_SENSORNAME);
			int iMagZ=getSignalIndex(Shimmer3Configuration.MAG_Z_OBJECTCLUSTER_SENSORNAME);
			tempData[0]=(double)newPacketInt[iMagX];
			tempData[1]=(double)newPacketInt[iMagY];
			tempData[2]=(double)newPacketInt[iMagZ];
			objectCluster.mPropertyCluster.put(Shimmer3Configuration.MAG_X_OBJECTCLUSTER_SENSORNAME,new FormatCluster("RAW",NO_UNIT,(double)newPacketInt[iMagX]));
			objectCluster.mPropertyCluster.put(Shimmer3Configuration.MAG_Y_OBJECTCLUSTER_SENSORNAME,new FormatCluster("RAW",NO_UNIT,(double)newPacketInt[iMagY]));
			objectCluster.mPropertyCluster.put(Shimmer3Configuration.MAG_Z_OBJECTCLUSTER_SENSORNAME,new FormatCluster("RAW",NO_UNIT,(double)newPacketInt[iMagZ]));
			uncalibratedData[iMagX]=(double)newPacketInt[iMagX];
			uncalibratedData[iMagY]=(double)newPacketInt[iMagY];
			uncalibratedData[iMagZ]=(double)newPacketInt[iMagZ];
			uncalibratedDataUnits[iMagX]=NO_UNIT;
			uncalibratedDataUnits[iMagY]=NO_UNIT;
			uncalibratedDataUnits[iMagZ]=NO_UNIT;
			if (mEnableCalibration){
				double[] magCalibratedData;
				magCalibratedData=calibrateInertialSensorData(tempData, mAlignmentMatrixMagnetometer, mSensitivityMatrixMagnetometer, mOffsetVectorMagnetometer);
				calibratedData[iMagX]=magCalibratedData[0];
				calibratedData[iMagY]=magCalibratedData[1];
				calibratedData[iMagZ]=magCalibratedData[2];
				if (mDefaultCalibrationParametersMag == true) {
					objectCluster.mPropertyCluster.put(Shimmer3Configuration.MAG_X_OBJECTCLUSTER_SENSORNAME,new FormatCluster("CAL",MAG_DEFAULT_CAL_UNIT,magCalibratedData[0]));
					objectCluster.mPropertyCluster.put(Shimmer3Configuration.MAG_Y_OBJECTCLUSTER_SENSORNAME,new FormatCluster("CAL",MAG_DEFAULT_CAL_UNIT,magCalibratedData[1]));
					objectCluster.mPropertyCluster.put(Shimmer3Configuration.MAG_Z_OBJECTCLUSTER_SENSORNAME,new FormatCluster("CAL",MAG_DEFAULT_CAL_UNIT,magCalibratedData[2]));
					magnetometer.x=magCalibratedData[0];
					magnetometer.y=magCalibratedData[1];
					magnetometer.z=magCalibratedData[2];
					calibratedDataUnits[iMagX]=MAG_DEFAULT_CAL_UNIT;
					calibratedDataUnits[iMagY]=MAG_DEFAULT_CAL_UNIT;
					calibratedDataUnits[iMagZ]=MAG_DEFAULT_CAL_UNIT;
				} else {
					objectCluster.mPropertyCluster.put(Shimmer3Configuration.MAG_X_OBJECTCLUSTER_SENSORNAME,new FormatCluster("CAL",MAG_CAL_UNIT,magCalibratedData[0]));
					objectCluster.mPropertyCluster.put(Shimmer3Configuration.MAG_Y_OBJECTCLUSTER_SENSORNAME,new FormatCluster("CAL",MAG_CAL_UNIT,magCalibratedData[1]));
					objectCluster.mPropertyCluster.put(Shimmer3Configuration.MAG_Z_OBJECTCLUSTER_SENSORNAME,new FormatCluster("CAL",MAG_CAL_UNIT,magCalibratedData[2]));
					magnetometer.x=magCalibratedData[0];
					magnetometer.y=magCalibratedData[1];
					magnetometer.z=magCalibratedData[2];
					calibratedDataUnits[iMagX]=MAG_CAL_UNIT;
					calibratedDataUnits[iMagY]=MAG_CAL_UNIT;
					calibratedDataUnits[iMagZ]=MAG_CAL_UNIT;
				}
			}
		}

		if (((fwIdentifier == FW_IDEN_BTSTREAM) && (mEnabledSensors & BTStream.BATTERY) > 0) 
				|| ((fwIdentifier == FW_IDEN_SD) && (mEnabledSensors & SDLogHeader.BATTERY) > 0)
				) {
			int iBatt = getSignalIndex(Shimmer3Configuration.BATTERY_OBJECTCLUSTER_SENSORNAME);
			tempData[0] = (double)newPacketInt[iBatt];
			objectCluster.mPropertyCluster.put(Shimmer3Configuration.BATTERY_OBJECTCLUSTER_SENSORNAME,new FormatCluster("RAW",NO_UNIT,(double)newPacketInt[iBatt]));
			uncalibratedData[iBatt]=(double)newPacketInt[iBatt];
			uncalibratedDataUnits[iBatt]=NO_UNIT;
			if (mEnableCalibration){
				calibratedData[iBatt]=calibrateU12AdcValue(tempData[0],0,3,1)*1.988;
				calibratedDataUnits[iBatt] = ADC_CAL_UNIT;
				objectCluster.mPropertyCluster.put(Shimmer3Configuration.BATTERY_OBJECTCLUSTER_SENSORNAME,new FormatCluster("CAL",ADC_CAL_UNIT,calibratedData[iBatt]));
				mVSenseBattMA.addValue(calibratedData[iBatt]);
				checkBattery();
			}
		}
		if (((fwIdentifier == FW_IDEN_BTSTREAM) && (mEnabledSensors & BTStream.EXT_EXP_A7) > 0) 
				|| ((fwIdentifier == FW_IDEN_SD) && (mEnabledSensors & SDLogHeader.EXT_EXP_A7) > 0)
				) {
			int iA7 = getSignalIndex(Shimmer3Configuration.EXT_EXP_A7_OBJECTCLUSTER_SENSORNAME);
			tempData[0] = (double)newPacketInt[iA7];
			objectCluster.mPropertyCluster.put(Shimmer3Configuration.EXT_EXP_A7_OBJECTCLUSTER_SENSORNAME,new FormatCluster("RAW",NO_UNIT,(double)newPacketInt[iA7]));
			uncalibratedData[iA7]=(double)newPacketInt[iA7];
			uncalibratedDataUnits[iA7]=NO_UNIT;
			if (mEnableCalibration){
				calibratedData[iA7]=calibrateU12AdcValue(tempData[0],0,3,1);
				calibratedDataUnits[iA7] = ADC_CAL_UNIT;
				objectCluster.mPropertyCluster.put(Shimmer3Configuration.EXT_EXP_A7_OBJECTCLUSTER_SENSORNAME,new FormatCluster("CAL",ADC_CAL_UNIT,calibratedData[iA7]));
			}
		}
		if (((fwIdentifier == FW_IDEN_BTSTREAM) && (mEnabledSensors & BTStream.EXT_EXP_A6) > 0) 
				|| ((fwIdentifier == FW_IDEN_SD) && (mEnabledSensors & SDLogHeader.EXT_EXP_A6) > 0)
				) {
			int iA6 = getSignalIndex(Shimmer3Configuration.EXT_EXP_A6_OBJECTCLUSTER_SENSORNAME);
			tempData[0] = (double)newPacketInt[iA6];
			objectCluster.mPropertyCluster.put(Shimmer3Configuration.EXT_EXP_A6_OBJECTCLUSTER_SENSORNAME,new FormatCluster("RAW",NO_UNIT,(double)newPacketInt[iA6]));
			uncalibratedData[iA6]=(double)newPacketInt[iA6];
			uncalibratedDataUnits[iA6]=NO_UNIT;
			if (mEnableCalibration){
				calibratedData[iA6]=calibrateU12AdcValue(tempData[0],0,3,1);
				calibratedDataUnits[iA6] = ADC_CAL_UNIT;
				objectCluster.mPropertyCluster.put(Shimmer3Configuration.EXT_EXP_A6_OBJECTCLUSTER_SENSORNAME,new FormatCluster("CAL",ADC_CAL_UNIT,calibratedData[iA6]));
			}
		}
		if (((fwIdentifier == FW_IDEN_BTSTREAM) && (mEnabledSensors & BTStream.EXT_EXP_A15) > 0) 
				|| ((fwIdentifier == FW_IDEN_SD) && (mEnabledSensors & SDLogHeader.EXT_EXP_A15) > 0)
				) {
			int iA15 = getSignalIndex(Shimmer3Configuration.EXT_EXP_A15_OBJECTCLUSTER_SENSORNAME);
			tempData[0] = (double)newPacketInt[iA15];
			objectCluster.mPropertyCluster.put(Shimmer3Configuration.EXT_EXP_A15_OBJECTCLUSTER_SENSORNAME,new FormatCluster("RAW",NO_UNIT,(double)newPacketInt[iA15]));
			uncalibratedData[iA15]=(double)newPacketInt[iA15];
			uncalibratedDataUnits[iA15]=NO_UNIT;
			
			if (mEnableCalibration){
				calibratedData[iA15]=calibrateU12AdcValue(tempData[0],0,3,1);
				calibratedDataUnits[iA15] = ADC_CAL_UNIT;
				objectCluster.mPropertyCluster.put(Shimmer3Configuration.EXT_EXP_A15_OBJECTCLUSTER_SENSORNAME,new FormatCluster("CAL",ADC_CAL_UNIT,calibratedData[iA15]));
			}
		}
		if (((fwIdentifier == FW_IDEN_BTSTREAM) && (mEnabledSensors & BTStream.INT_EXP_A1) > 0) 
				|| ((fwIdentifier == FW_IDEN_SD) && (mEnabledSensors & SDLogHeader.INT_EXP_A1) > 0)
				) {
			int iA1 = getSignalIndex(Shimmer3Configuration.INT_EXP_A1_OBJECTCLUSTER_SENSORNAME);
			tempData[0] = (double)newPacketInt[iA1];
			objectCluster.mPropertyCluster.put(Shimmer3Configuration.INT_EXP_A1_OBJECTCLUSTER_SENSORNAME,new FormatCluster("RAW",NO_UNIT,(double)newPacketInt[iA1]));
			uncalibratedData[iA1]=(double)newPacketInt[iA1];
			uncalibratedDataUnits[iA1]=NO_UNIT;
			if (mEnableCalibration){
				calibratedData[iA1]=calibrateU12AdcValue(tempData[0],0,3,1);
				calibratedDataUnits[iA1] = ADC_CAL_UNIT;
				objectCluster.mPropertyCluster.put(Shimmer3Configuration.INT_EXP_A1_OBJECTCLUSTER_SENSORNAME,new FormatCluster("CAL",ADC_CAL_UNIT,calibratedData[iA1]));
			}
		}
		if (((fwIdentifier == FW_IDEN_BTSTREAM) && (mEnabledSensors & BTStream.INT_EXP_A12) > 0) 
				|| ((fwIdentifier == FW_IDEN_SD) && (mEnabledSensors & SDLogHeader.INT_EXP_A12) > 0)
				) {
			int iA12 = getSignalIndex(Shimmer3Configuration.INT_EXP_A1_OBJECTCLUSTER_SENSORNAME);
			tempData[0] = (double)newPacketInt[iA12];
			objectCluster.mPropertyCluster.put(Shimmer3Configuration.INT_EXP_A12_OBJECTCLUSTER_SENSORNAME,new FormatCluster("RAW",NO_UNIT,(double)newPacketInt[iA12]));
			uncalibratedData[iA12]=(double)newPacketInt[iA12];
			uncalibratedDataUnits[iA12]=NO_UNIT;
			if (mEnableCalibration){
				calibratedData[iA12]=calibrateU12AdcValue(tempData[0],0,3,1);
				objectCluster.mPropertyCluster.put(Shimmer3Configuration.INT_EXP_A12_OBJECTCLUSTER_SENSORNAME,new FormatCluster("CAL",ADC_CAL_UNIT,calibratedData[iA12]));
				calibratedDataUnits[iA12] = ADC_CAL_UNIT;
				
			}
		}
		if (((fwIdentifier == FW_IDEN_BTSTREAM) && (mEnabledSensors & BTStream.INT_EXP_A13) > 0) 
				|| ((fwIdentifier == FW_IDEN_SD) && (mEnabledSensors & SDLogHeader.INT_EXP_A13) > 0)
				) {
			int iA13 = getSignalIndex(Shimmer3Configuration.INT_EXP_A13_OBJECTCLUSTER_SENSORNAME);
			tempData[0] = (double)newPacketInt[iA13];
			objectCluster.mPropertyCluster.put(Shimmer3Configuration.INT_EXP_A13_OBJECTCLUSTER_SENSORNAME,new FormatCluster("RAW",NO_UNIT,(double)newPacketInt[iA13]));
			uncalibratedData[iA13]=(double)newPacketInt[iA13];
			uncalibratedDataUnits[iA13]=NO_UNIT;
			if (mEnableCalibration){
				calibratedData[iA13]=calibrateU12AdcValue(tempData[0],0,3,1);
				objectCluster.mPropertyCluster.put(Shimmer3Configuration.INT_EXP_A13_OBJECTCLUSTER_SENSORNAME,new FormatCluster("CAL",ADC_CAL_UNIT,calibratedData[iA13]));
				calibratedDataUnits[iA13] = ADC_CAL_UNIT;
			}
		}
		if (((fwIdentifier == FW_IDEN_BTSTREAM) && (mEnabledSensors & BTStream.INT_EXP_A14) > 0) 
				|| ((fwIdentifier == FW_IDEN_SD) && (mEnabledSensors & SDLogHeader.INT_EXP_A14) > 0)
				) {
			int iA14 = getSignalIndex(Shimmer3Configuration.INT_EXP_A14_OBJECTCLUSTER_SENSORNAME);
			tempData[0] = (double)newPacketInt[iA14];
			objectCluster.mPropertyCluster.put(Shimmer3Configuration.INT_EXP_A14_OBJECTCLUSTER_SENSORNAME,new FormatCluster("RAW",NO_UNIT,(double)newPacketInt[iA14]));
			uncalibratedData[iA14]=(double)newPacketInt[iA14];
			uncalibratedDataUnits[iA14]=NO_UNIT;
			if (mEnableCalibration){
				calibratedData[iA14]=calibrateU12AdcValue(tempData[0],0,3,1);
				objectCluster.mPropertyCluster.put(Shimmer3Configuration.INT_EXP_A14_OBJECTCLUSTER_SENSORNAME,new FormatCluster("CAL",ADC_CAL_UNIT,calibratedData[iA14]));
				calibratedDataUnits[iA14] = ADC_CAL_UNIT;
			}
		}
		//((fwIdentifier == FW_IDEN_BTSTREAM) && (mEnabledSensors & BTStream.ACCEL_WR) > 0) 
		//|| ((fwIdentifier == FW_IDEN_SD) && (mEnabledSensors & SDLogHeader.ACCEL_WR) > 0)
		
		
		
		
		if (((fwIdentifier == FW_IDEN_BTSTREAM)&&((mEnabledSensors & BTStream.ACCEL_LN) > 0 || (mEnabledSensors & BTStream.ACCEL_WR) > 0) && ((mEnabledSensors & BTStream.GYRO) > 0) && ((mEnabledSensors & BTStream.MAG) > 0) && mOrientationEnabled )
				||((fwIdentifier == FW_IDEN_SD)&&((mEnabledSensors & SDLogHeader.ACCEL_LN) > 0 || (mEnabledSensors & SDLogHeader.ACCEL_WR) > 0) && ((mEnabledSensors & SDLogHeader.GYRO) > 0) && ((mEnabledSensors & SDLogHeader.MAG) > 0) && mOrientationEnabled )){
			if (mEnableCalibration){
				if (mOrientationAlgo==null){
					mOrientationAlgo = new GradDes3DOrientation(0.4, (double)1/mSamplingRate, 1, 0, 0,0);
				}
				Quaternion q = mOrientationAlgo.update(accelerometer.x,accelerometer.y,accelerometer.z, gyroscope.x,gyroscope.y,gyroscope.z, magnetometer.x,magnetometer.y,magnetometer.z);					double theta, Rx, Ry, Rz, rho;
				rho = Math.acos(q.q1);
				theta = rho * 2;
				Rx = q.q2 / Math.sin(rho);
				Ry = q.q3 / Math.sin(rho);
				Rz = q.q4 / Math.sin(rho);
				objectCluster.mPropertyCluster.put("Axis Angle A",new FormatCluster("CAL","local",theta));
				objectCluster.mPropertyCluster.put("Axis Angle X",new FormatCluster("CAL","local",Rx));
				objectCluster.mPropertyCluster.put("Axis Angle Y",new FormatCluster("CAL","local",Ry));
				objectCluster.mPropertyCluster.put("Axis Angle Z",new FormatCluster("CAL","local",Rz));
				objectCluster.mPropertyCluster.put(Shimmer3Configuration.QUAT_MADGE_9DOF_W_OBJECTCLUSTER_SENSORNAME,new FormatCluster("CAL","local",q.q1));
				objectCluster.mPropertyCluster.put(Shimmer3Configuration.QUAT_MADGE_9DOF_X_OBJECTCLUSTER_SENSORNAME,new FormatCluster("CAL","local",q.q2));
				objectCluster.mPropertyCluster.put(Shimmer3Configuration.QUAT_MADGE_9DOF_Y_OBJECTCLUSTER_SENSORNAME,new FormatCluster("CAL","local",q.q3));
				objectCluster.mPropertyCluster.put(Shimmer3Configuration.QUAT_MADGE_9DOF_Z_OBJECTCLUSTER_SENSORNAME,new FormatCluster("CAL","local",q.q4));
			}
		}

		if (((fwIdentifier == FW_IDEN_BTSTREAM) && (mEnabledSensors & BTStream.EXG1_24BIT) > 0) 
				|| ((fwIdentifier == FW_IDEN_SD) && (mEnabledSensors & SDLogHeader.EXG1_24BIT) > 0)
				){
			int iexg1ch1 = getSignalIndex(Shimmer3Configuration.EXG1_CH1_24BIT_OBJECTCLUSTER_SENSORNAME);
			int iexg1ch2 = getSignalIndex(Shimmer3Configuration.EXG1_CH2_24BIT_OBJECTCLUSTER_SENSORNAME);
			int iexg1sta = getSignalIndex(Shimmer3Configuration.EXG1_STATUS_OBJECTCLUSTER_SENSORNAME);
			double exg1ch1 = (double)newPacketInt[iexg1ch1];
			double exg1ch2 = (double)newPacketInt[iexg1ch2];
			double exg1sta = (double)newPacketInt[iexg1sta];
			objectCluster.mPropertyCluster.put(Shimmer3Configuration.EXG1_STATUS_OBJECTCLUSTER_SENSORNAME,new FormatCluster("RAW",NO_UNIT,exg1sta));
			uncalibratedData[iexg1sta]=(double)newPacketInt[iexg1sta];
			uncalibratedData[iexg1ch1]=(double)newPacketInt[iexg1ch1];
			uncalibratedData[iexg1ch2]=(double)newPacketInt[iexg1ch2];
			uncalibratedDataUnits[iexg1sta]=NO_UNIT;
			uncalibratedDataUnits[iexg1ch1]=NO_UNIT;
			uncalibratedDataUnits[iexg1ch2]=NO_UNIT;
			if (mEnableCalibration){
				double calexg1ch1 = exg1ch1 *(((2.42*1000)/mEXG1CH1GainValue)/(Math.pow(2,23)-1));
				double calexg1ch2 = exg1ch2 *(((2.42*1000)/mEXG1CH2GainValue)/(Math.pow(2,23)-1));
				calibratedData[iexg1ch1]=calexg1ch1;
				calibratedData[iexg1ch2]=calexg1ch2;
				calibratedData[iexg1sta]=(double)newPacketInt[iexg1sta];
				calibratedDataUnits[iexg1sta]=NO_UNIT;
				calibratedDataUnits[iexg1ch1]=ADC_CAL_UNIT;
				calibratedDataUnits[iexg1ch2]=ADC_CAL_UNIT;
				if (isEXGUsingDefaultECGConfiguration()){
					sensorNames[iexg1ch1]=Shimmer3Configuration.ECG_LL_RA_24BIT_OBJECTCLUSTER_SENSORNAME;
					sensorNames[iexg1ch2]=Shimmer3Configuration.ECG_LA_RA_24BIT_OBJECTCLUSTER_SENSORNAME;
					objectCluster.mPropertyCluster.put(Shimmer3Configuration.ECG_LL_RA_24BIT_OBJECTCLUSTER_SENSORNAME,new FormatCluster("RAW",NO_UNIT,exg1ch1));
					objectCluster.mPropertyCluster.put(Shimmer3Configuration.ECG_LA_RA_24BIT_OBJECTCLUSTER_SENSORNAME,new FormatCluster("RAW",NO_UNIT,exg1ch2));
					objectCluster.mPropertyCluster.put(Shimmer3Configuration.ECG_LL_RA_24BIT_OBJECTCLUSTER_SENSORNAME,new FormatCluster("CAL",ADC_CAL_UNIT,calexg1ch1));
					objectCluster.mPropertyCluster.put(Shimmer3Configuration.ECG_LA_RA_24BIT_OBJECTCLUSTER_SENSORNAME,new FormatCluster("CAL",ADC_CAL_UNIT,calexg1ch2));
				} else if (isEXGUsingDefaultEMGConfiguration()){
					sensorNames[iexg1ch1]=Shimmer3Configuration.EMG_CH1_24BIT_OBJECTCLUSTER_SENSORNAME;
					sensorNames[iexg1ch2]=Shimmer3Configuration.EMG_CH2_24BIT_OBJECTCLUSTER_SENSORNAME;
					objectCluster.mPropertyCluster.put(Shimmer3Configuration.EMG_CH1_24BIT_OBJECTCLUSTER_SENSORNAME,new FormatCluster("RAW",NO_UNIT,exg1ch1));
					objectCluster.mPropertyCluster.put(Shimmer3Configuration.EMG_CH2_24BIT_OBJECTCLUSTER_SENSORNAME,new FormatCluster("RAW",NO_UNIT,exg1ch2));
					objectCluster.mPropertyCluster.put(Shimmer3Configuration.EMG_CH1_24BIT_OBJECTCLUSTER_SENSORNAME,new FormatCluster("CAL",ADC_CAL_UNIT,calexg1ch1));
					objectCluster.mPropertyCluster.put(Shimmer3Configuration.EMG_CH2_24BIT_OBJECTCLUSTER_SENSORNAME,new FormatCluster("CAL",ADC_CAL_UNIT,calexg1ch2));
				} else {
					sensorNames[iexg1ch1]=Shimmer3Configuration.EXG1_CH1_24BIT_OBJECTCLUSTER_SENSORNAME;
					sensorNames[iexg1ch2]=Shimmer3Configuration.EXG1_CH2_24BIT_OBJECTCLUSTER_SENSORNAME;
					objectCluster.mPropertyCluster.put(Shimmer3Configuration.EXG1_CH1_24BIT_OBJECTCLUSTER_SENSORNAME,new FormatCluster("RAW",NO_UNIT,exg1ch1));
					objectCluster.mPropertyCluster.put(Shimmer3Configuration.EXG1_CH2_24BIT_OBJECTCLUSTER_SENSORNAME,new FormatCluster("RAW",NO_UNIT,exg1ch2));
					objectCluster.mPropertyCluster.put(Shimmer3Configuration.EXG1_CH1_24BIT_OBJECTCLUSTER_SENSORNAME,new FormatCluster("CAL",ADC_CAL_UNIT,calexg1ch1));
					objectCluster.mPropertyCluster.put(Shimmer3Configuration.EXG1_CH2_24BIT_OBJECTCLUSTER_SENSORNAME,new FormatCluster("CAL",ADC_CAL_UNIT,calexg1ch2));
				}
			}
		}
		if (((fwIdentifier == FW_IDEN_BTSTREAM) && (mEnabledSensors & BTStream.EXG2_24BIT) > 0) 
				|| ((fwIdentifier == FW_IDEN_SD) && (mEnabledSensors & SDLogHeader.EXG2_24BIT) > 0)
				){
			int iexg2ch1 = getSignalIndex(Shimmer3Configuration.EXG2_CH1_24BIT_OBJECTCLUSTER_SENSORNAME);
			int iexg2ch2 = getSignalIndex(Shimmer3Configuration.EXG2_CH2_24BIT_OBJECTCLUSTER_SENSORNAME);
			int iexg2sta = getSignalIndex(Shimmer3Configuration.EXG2_STATUS_OBJECTCLUSTER_SENSORNAME);
			double exg2ch1 = (double)newPacketInt[iexg2ch1];
			double exg2ch2 = (double)newPacketInt[iexg2ch2];
			double exg2sta = (double)newPacketInt[iexg2sta];
			objectCluster.mPropertyCluster.put(Shimmer3Configuration.EXG2_STATUS_OBJECTCLUSTER_SENSORNAME,new FormatCluster("RAW",NO_UNIT,exg2sta));
			uncalibratedData[iexg2sta]=(double)newPacketInt[iexg2sta];
			uncalibratedData[iexg2ch1]=(double)newPacketInt[iexg2ch1];
			uncalibratedData[iexg2ch2]=(double)newPacketInt[iexg2ch2];
			uncalibratedDataUnits[iexg2sta]=NO_UNIT;
			uncalibratedDataUnits[iexg2ch1]=NO_UNIT;
			uncalibratedDataUnits[iexg2ch2]=NO_UNIT;
			if (mEnableCalibration){
				double calexg2ch1 = exg2ch1 *(((2.42*1000)/mEXG2CH1GainValue)/(Math.pow(2,23)-1));
				double calexg2ch2 = exg2ch2 *(((2.42*1000)/mEXG2CH2GainValue)/(Math.pow(2,23)-1));
				calibratedData[iexg2ch1]=calexg2ch1;
				calibratedData[iexg2ch2]=calexg2ch2;
				calibratedData[iexg2sta]=(double)newPacketInt[iexg2sta];
				calibratedDataUnits[iexg2sta]=NO_UNIT;
				calibratedDataUnits[iexg2ch1]=ADC_CAL_UNIT;
				calibratedDataUnits[iexg2ch2]=ADC_CAL_UNIT;
				if (isEXGUsingDefaultECGConfiguration()){
					sensorNames[iexg2ch1]=Shimmer3Configuration.EXG2_CH1_24BIT_OBJECTCLUSTER_SENSORNAME;
					sensorNames[iexg2ch2]=Shimmer3Configuration.ECG_VX_RL_24BIT_OBJECTCLUSTER_SENSORNAME;
					objectCluster.mPropertyCluster.put(Shimmer3Configuration.EXG2_CH1_24BIT_OBJECTCLUSTER_SENSORNAME,new FormatCluster("RAW",NO_UNIT,exg2ch1));
					objectCluster.mPropertyCluster.put(Shimmer3Configuration.ECG_VX_RL_24BIT_OBJECTCLUSTER_SENSORNAME,new FormatCluster("RAW",NO_UNIT,exg2ch2));
					objectCluster.mPropertyCluster.put(Shimmer3Configuration.EXG2_CH1_24BIT_OBJECTCLUSTER_SENSORNAME,new FormatCluster("CAL",ADC_CAL_UNIT,calexg2ch1));
					objectCluster.mPropertyCluster.put(Shimmer3Configuration.ECG_VX_RL_24BIT_OBJECTCLUSTER_SENSORNAME,new FormatCluster("CAL",ADC_CAL_UNIT,calexg2ch2));
				}
				else if (isEXGUsingDefaultEMGConfiguration()){
					sensorNames[iexg2ch1]=Shimmer3Configuration.EXG2_CH1_24BIT_OBJECTCLUSTER_SENSORNAME;
					sensorNames[iexg2ch2]=Shimmer3Configuration.EXG2_CH2_24BIT_OBJECTCLUSTER_SENSORNAME;
					objectCluster.mPropertyCluster.put(Shimmer3Configuration.EXG2_CH1_24BIT_OBJECTCLUSTER_SENSORNAME,new FormatCluster("RAW",NO_UNIT,0));
					objectCluster.mPropertyCluster.put(Shimmer3Configuration.EXG2_CH2_24BIT_OBJECTCLUSTER_SENSORNAME,new FormatCluster("RAW",NO_UNIT,0));
					objectCluster.mPropertyCluster.put(Shimmer3Configuration.EXG2_CH1_24BIT_OBJECTCLUSTER_SENSORNAME,new FormatCluster("CAL",ADC_CAL_UNIT,0));
					objectCluster.mPropertyCluster.put(Shimmer3Configuration.EXG2_CH2_24BIT_OBJECTCLUSTER_SENSORNAME,new FormatCluster("CAL",ADC_CAL_UNIT,0));
				} else {
					sensorNames[iexg2ch1]=Shimmer3Configuration.EXG2_CH1_24BIT_OBJECTCLUSTER_SENSORNAME;
					sensorNames[iexg2ch2]=Shimmer3Configuration.EXG2_CH2_24BIT_OBJECTCLUSTER_SENSORNAME;
					objectCluster.mPropertyCluster.put(Shimmer3Configuration.EXG2_CH1_24BIT_OBJECTCLUSTER_SENSORNAME,new FormatCluster("RAW",NO_UNIT,0));
					objectCluster.mPropertyCluster.put(Shimmer3Configuration.EXG2_CH2_24BIT_OBJECTCLUSTER_SENSORNAME,new FormatCluster("RAW",NO_UNIT,0));
					objectCluster.mPropertyCluster.put(Shimmer3Configuration.EXG2_CH1_24BIT_OBJECTCLUSTER_SENSORNAME,new FormatCluster("CAL",ADC_CAL_UNIT,0));
					objectCluster.mPropertyCluster.put(Shimmer3Configuration.EXG2_CH2_24BIT_OBJECTCLUSTER_SENSORNAME,new FormatCluster("CAL",ADC_CAL_UNIT,0));	
				}
			}
		}

		if (((fwIdentifier == FW_IDEN_BTSTREAM) && (mEnabledSensors & BTStream.EXG1_16BIT) > 0) 
				|| ((fwIdentifier == FW_IDEN_SD) && (mEnabledSensors & SDLogHeader.EXG1_16BIT) > 0)
				){
			int iexg1ch1 = getSignalIndex(Shimmer3Configuration.EXG1_CH1_16BIT_OBJECTCLUSTER_SENSORNAME);
			int iexg1ch2 = getSignalIndex(Shimmer3Configuration.EXG1_CH2_16BIT_OBJECTCLUSTER_SENSORNAME);
			int iexg1sta = getSignalIndex(Shimmer3Configuration.EXG1_STATUS_OBJECTCLUSTER_SENSORNAME);
			double exg1ch1 = (double)newPacketInt[iexg1ch1];
			double exg1ch2 = (double)newPacketInt[iexg1ch2];
			double exg1sta = (double)newPacketInt[iexg1sta];
			objectCluster.mPropertyCluster.put(Shimmer3Configuration.EXG1_STATUS_OBJECTCLUSTER_SENSORNAME,new FormatCluster("RAW",NO_UNIT,exg1sta));
			uncalibratedData[iexg1sta]=(double)newPacketInt[iexg1sta];
			uncalibratedData[iexg1ch1]=(double)newPacketInt[iexg1ch1];
			uncalibratedData[iexg1ch2]=(double)newPacketInt[iexg1ch2];
			uncalibratedDataUnits[iexg1sta]=NO_UNIT;
			uncalibratedDataUnits[iexg1ch1]=NO_UNIT;
			uncalibratedDataUnits[iexg1ch2]=NO_UNIT;
			if (mEnableCalibration){
				double calexg1ch1 = exg1ch1 *(((2.42*1000)/(mEXG1CH1GainValue*2))/(Math.pow(2,15)-1));
				double calexg1ch2 = exg1ch2 *(((2.42*1000)/(mEXG1CH2GainValue*2))/(Math.pow(2,15)-1));
				calibratedData[iexg1ch1]=calexg1ch1;
				calibratedData[iexg1ch2]=calexg1ch2;
				calibratedData[iexg1sta]=(double)newPacketInt[iexg1sta];
				calibratedDataUnits[iexg1sta]=NO_UNIT;
				calibratedDataUnits[iexg1ch1]=ADC_CAL_UNIT;
				calibratedDataUnits[iexg1ch2]=ADC_CAL_UNIT;
				if (isEXGUsingDefaultECGConfiguration()){
					sensorNames[iexg1ch1]=Shimmer3Configuration.ECG_LL_RA_16BIT_OBJECTCLUSTER_SENSORNAME;
					sensorNames[iexg1ch2]=Shimmer3Configuration.ECG_LA_RA_16BIT_OBJECTCLUSTER_SENSORNAME;
					objectCluster.mPropertyCluster.put(Shimmer3Configuration.ECG_LL_RA_16BIT_OBJECTCLUSTER_SENSORNAME,new FormatCluster("RAW",NO_UNIT,exg1ch1));
					objectCluster.mPropertyCluster.put(Shimmer3Configuration.ECG_LA_RA_16BIT_OBJECTCLUSTER_SENSORNAME,new FormatCluster("RAW",NO_UNIT,exg1ch2));
					objectCluster.mPropertyCluster.put(Shimmer3Configuration.ECG_LL_RA_16BIT_OBJECTCLUSTER_SENSORNAME,new FormatCluster("CAL",ADC_CAL_UNIT,calexg1ch1));
					objectCluster.mPropertyCluster.put(Shimmer3Configuration.ECG_LA_RA_16BIT_OBJECTCLUSTER_SENSORNAME,new FormatCluster("CAL",ADC_CAL_UNIT,calexg1ch2));
				} else if (isEXGUsingDefaultEMGConfiguration()){
					sensorNames[iexg1ch1]=Shimmer3Configuration.EMG_CH1_16BIT_OBJECTCLUSTER_SENSORNAME;
					sensorNames[iexg1ch2]=Shimmer3Configuration.EMG_CH2_16BIT_OBJECTCLUSTER_SENSORNAME;
					objectCluster.mPropertyCluster.put(Shimmer3Configuration.EMG_CH1_16BIT_OBJECTCLUSTER_SENSORNAME,new FormatCluster("RAW",NO_UNIT,exg1ch1));
					objectCluster.mPropertyCluster.put(Shimmer3Configuration.EMG_CH2_16BIT_OBJECTCLUSTER_SENSORNAME,new FormatCluster("RAW",NO_UNIT,exg1ch2));
					objectCluster.mPropertyCluster.put(Shimmer3Configuration.EMG_CH1_16BIT_OBJECTCLUSTER_SENSORNAME,new FormatCluster("CAL",ADC_CAL_UNIT,calexg1ch1));
					objectCluster.mPropertyCluster.put(Shimmer3Configuration.EMG_CH2_16BIT_OBJECTCLUSTER_SENSORNAME,new FormatCluster("CAL",ADC_CAL_UNIT,calexg1ch2));
				} else {
					sensorNames[iexg1ch1]=Shimmer3Configuration.EXG1_CH1_16BIT_OBJECTCLUSTER_SENSORNAME;
					sensorNames[iexg1ch2]=Shimmer3Configuration.EXG1_CH2_16BIT_OBJECTCLUSTER_SENSORNAME;
					objectCluster.mPropertyCluster.put(Shimmer3Configuration.EXG1_CH1_16BIT_OBJECTCLUSTER_SENSORNAME,new FormatCluster("RAW",NO_UNIT,exg1ch1));
					objectCluster.mPropertyCluster.put(Shimmer3Configuration.EXG1_CH2_16BIT_OBJECTCLUSTER_SENSORNAME,new FormatCluster("RAW",NO_UNIT,exg1ch2));
					objectCluster.mPropertyCluster.put(Shimmer3Configuration.EXG1_CH1_16BIT_OBJECTCLUSTER_SENSORNAME,new FormatCluster("CAL",ADC_CAL_UNIT,calexg1ch1));
					objectCluster.mPropertyCluster.put(Shimmer3Configuration.EXG1_CH2_16BIT_OBJECTCLUSTER_SENSORNAME,new FormatCluster("CAL",ADC_CAL_UNIT,calexg1ch2));
				}
			}
		}
		if (((fwIdentifier == FW_IDEN_BTSTREAM) && (mEnabledSensors & BTStream.EXG2_16BIT) > 0) 
				|| ((fwIdentifier == FW_IDEN_SD) && (mEnabledSensors & SDLogHeader.EXG2_16BIT) > 0)
				){
			int iexg2ch1 = getSignalIndex(Shimmer3Configuration.EXG2_CH1_16BIT_OBJECTCLUSTER_SENSORNAME);
			int iexg2ch2 = getSignalIndex(Shimmer3Configuration.EXG2_CH2_16BIT_OBJECTCLUSTER_SENSORNAME);
			int iexg2sta = getSignalIndex(Shimmer3Configuration.EXG2_STATUS_OBJECTCLUSTER_SENSORNAME);
			double exg2ch1 = (double)newPacketInt[iexg2ch1];
			double exg2ch2 = (double)newPacketInt[iexg2ch2];
			double exg2sta = (double)newPacketInt[iexg2sta];
			objectCluster.mPropertyCluster.put(Shimmer3Configuration.EXG2_STATUS_OBJECTCLUSTER_SENSORNAME,new FormatCluster("RAW",NO_UNIT,exg2sta));
			uncalibratedData[iexg2sta]=(double)newPacketInt[iexg2sta];
			uncalibratedData[iexg2ch1]=(double)newPacketInt[iexg2ch1];
			uncalibratedData[iexg2ch2]=(double)newPacketInt[iexg2ch2];
			uncalibratedDataUnits[iexg2sta]=NO_UNIT;
			uncalibratedDataUnits[iexg2ch1]=NO_UNIT;
			uncalibratedDataUnits[iexg2ch2]=NO_UNIT;
			if (mEnableCalibration){
				double calexg2ch1 = ((exg2ch1)) *(((2.42*1000)/(mEXG2CH1GainValue*2))/(Math.pow(2,15)-1));
				double calexg2ch2 = ((exg2ch2)) *(((2.42*1000)/(mEXG2CH2GainValue*2))/(Math.pow(2,15)-1));
				calibratedData[iexg2ch1]=calexg2ch1;
				calibratedData[iexg2ch2]=calexg2ch2;
				calibratedData[iexg2sta]=(double)newPacketInt[iexg2sta];
				calibratedDataUnits[iexg2sta]=NO_UNIT;
				calibratedDataUnits[iexg2ch1]=ADC_CAL_UNIT;
				calibratedDataUnits[iexg2ch2]=ADC_CAL_UNIT;
				if (isEXGUsingDefaultECGConfiguration()){
					sensorNames[iexg2ch1]=Shimmer3Configuration.EXG2_CH1_16BIT_OBJECTCLUSTER_SENSORNAME;
					sensorNames[iexg2ch2]=Shimmer3Configuration.ECG_VX_RL_16BIT_OBJECTCLUSTER_SENSORNAME;
					objectCluster.mPropertyCluster.put(Shimmer3Configuration.EXG2_CH1_16BIT_OBJECTCLUSTER_SENSORNAME,new FormatCluster("RAW",NO_UNIT,exg2ch1));
					objectCluster.mPropertyCluster.put(Shimmer3Configuration.ECG_VX_RL_16BIT_OBJECTCLUSTER_SENSORNAME,new FormatCluster("RAW",NO_UNIT,exg2ch2));
					objectCluster.mPropertyCluster.put(Shimmer3Configuration.EXG2_CH1_16BIT_OBJECTCLUSTER_SENSORNAME,new FormatCluster("CAL",ADC_CAL_UNIT,calexg2ch1));
					objectCluster.mPropertyCluster.put(Shimmer3Configuration.ECG_VX_RL_16BIT_OBJECTCLUSTER_SENSORNAME,new FormatCluster("CAL",ADC_CAL_UNIT,calexg2ch2));
				}
				else if (isEXGUsingDefaultEMGConfiguration()){
					sensorNames[iexg2ch1]=Shimmer3Configuration.EXG2_CH1_16BIT_OBJECTCLUSTER_SENSORNAME;
					sensorNames[iexg2ch2]=Shimmer3Configuration.EXG2_CH2_16BIT_OBJECTCLUSTER_SENSORNAME;
					objectCluster.mPropertyCluster.put(Shimmer3Configuration.EXG2_CH1_16BIT_OBJECTCLUSTER_SENSORNAME,new FormatCluster("RAW",NO_UNIT,0));
					objectCluster.mPropertyCluster.put(Shimmer3Configuration.EXG2_CH2_16BIT_OBJECTCLUSTER_SENSORNAME,new FormatCluster("RAW",NO_UNIT,0));
					objectCluster.mPropertyCluster.put(Shimmer3Configuration.EXG2_CH1_16BIT_OBJECTCLUSTER_SENSORNAME,new FormatCluster("CAL",ADC_CAL_UNIT,0));
					objectCluster.mPropertyCluster.put(Shimmer3Configuration.EXG2_CH2_16BIT_OBJECTCLUSTER_SENSORNAME,new FormatCluster("CAL",ADC_CAL_UNIT,0));
				} else {
					sensorNames[iexg2ch1]=Shimmer3Configuration.EXG2_CH1_16BIT_OBJECTCLUSTER_SENSORNAME;
					sensorNames[iexg2ch2]=Shimmer3Configuration.EXG2_CH2_16BIT_OBJECTCLUSTER_SENSORNAME;
					objectCluster.mPropertyCluster.put(Shimmer3Configuration.EXG2_CH1_16BIT_OBJECTCLUSTER_SENSORNAME,new FormatCluster("RAW",NO_UNIT,0));
					objectCluster.mPropertyCluster.put(Shimmer3Configuration.EXG2_CH2_16BIT_OBJECTCLUSTER_SENSORNAME,new FormatCluster("RAW",NO_UNIT,0));
					objectCluster.mPropertyCluster.put(Shimmer3Configuration.EXG2_CH1_16BIT_OBJECTCLUSTER_SENSORNAME,new FormatCluster("CAL",ADC_CAL_UNIT,0));
					objectCluster.mPropertyCluster.put(Shimmer3Configuration.EXG2_CH2_16BIT_OBJECTCLUSTER_SENSORNAME,new FormatCluster("CAL",ADC_CAL_UNIT,0));	
				}
			}
		}

		if (((fwIdentifier == FW_IDEN_BTSTREAM) && (mEnabledSensors & BTStream.BMP180) > 0) 
				|| ((fwIdentifier == FW_IDEN_SD) && (mEnabledSensors & SDLogHeader.BMP180) > 0)
				){
			int iUT = getSignalIndex(Shimmer3Configuration.TEMPERATURE_BMP180_OBJECTCLUSTER_SENSORNAME);
			int iUP = getSignalIndex(Shimmer3Configuration.PRESSURE_BMP180_OBJECTCLUSTER_SENSORNAME);
			double UT = (double)newPacketInt[iUT];
			double UP = (double)newPacketInt[iUP];
			UP=UP/Math.pow(2,8-mPressureResolution);
			objectCluster.mPropertyCluster.put(Shimmer3Configuration.PRESSURE_BMP180_OBJECTCLUSTER_SENSORNAME,new FormatCluster("RAW",NO_UNIT,UP));
			objectCluster.mPropertyCluster.put(Shimmer3Configuration.TEMPERATURE_BMP180_OBJECTCLUSTER_SENSORNAME,new FormatCluster("RAW",NO_UNIT,UT));
			uncalibratedData[iUT]=(double)newPacketInt[iUT];
			uncalibratedData[iUP]=(double)newPacketInt[iUP];
			uncalibratedDataUnits[iUT]=NO_UNIT;
			uncalibratedDataUnits[iUP]=NO_UNIT;
			if (mEnableCalibration){
				double[] bmp180caldata= calibratePressureSensorData(UP,UT);
				objectCluster.mPropertyCluster.put("Pressure",new FormatCluster("CAL",PRESSURE_CAL_UNIT,bmp180caldata[0]/1000));
				objectCluster.mPropertyCluster.put("Temperature",new FormatCluster("CAL",TEMP_CAL_UNIT,bmp180caldata[1]));
				calibratedData[iUT]=bmp180caldata[1];
				calibratedData[iUP]=bmp180caldata[0]/1000;
				calibratedDataUnits[iUT]=TEMP_CAL_UNIT;
				calibratedDataUnits[iUP]=PRESSURE_CAL_UNIT;
			}
		}

		if (((fwIdentifier == FW_IDEN_BTSTREAM) && (mEnabledSensors & BTStream.BRIDGE_AMP) > 0) 
				|| ((fwIdentifier == FW_IDEN_SD) && (mEnabledSensors & SDLogHeader.BRIDGE_AMP) > 0)
				) {
			int iBAHigh = getSignalIndex(Shimmer3Configuration.BRIDGE_AMP_HIGH_OBJECTCLUSTER_SENSORNAME);
			int iBALow = getSignalIndex(Shimmer3Configuration.BRIDGE_AMP_LOW_OBJECTCLUSTER_SENSORNAME);
			objectCluster.mPropertyCluster.put(Shimmer3Configuration.BRIDGE_AMP_HIGH_OBJECTCLUSTER_SENSORNAME,new FormatCluster("RAW",NO_UNIT,(double)newPacketInt[iBAHigh]));
			objectCluster.mPropertyCluster.put(Shimmer3Configuration.BRIDGE_AMP_LOW_OBJECTCLUSTER_SENSORNAME,new FormatCluster("RAW",NO_UNIT,(double)newPacketInt[iBALow]));
			uncalibratedData[iBAHigh]=(double)newPacketInt[iBAHigh];
			uncalibratedData[iBALow]=(double)newPacketInt[iBALow];
			uncalibratedDataUnits[iBAHigh]=NO_UNIT;
			uncalibratedDataUnits[iBALow]=NO_UNIT;
			if (mEnableCalibration){
				tempData[0] = (double)newPacketInt[iBAHigh];
				tempData[1] = (double)newPacketInt[iBALow];
				calibratedData[iBAHigh]=calibrateU12AdcValue(tempData[0],60,3,551);
				calibratedData[iBALow]=calibrateU12AdcValue(tempData[1],1950,3,183.7);
				calibratedDataUnits[iBAHigh]=ADC_CAL_UNIT;
				calibratedDataUnits[iBALow]=ADC_CAL_UNIT;
				objectCluster.mPropertyCluster.put("Bridge Amplifier High",new FormatCluster("CAL",ADC_CAL_UNIT,calibratedData[iBAHigh]));
				objectCluster.mPropertyCluster.put("Bridge Amplifier Low",new FormatCluster("CAL",ADC_CAL_UNIT,calibratedData[iBALow]));	
			}
		}

		if (((fwIdentifier == FW_IDEN_BTSTREAM) && (mEnabledSensors & BTStream.GSR) > 0) 
				|| ((fwIdentifier == FW_IDEN_SD) && (mEnabledSensors & SDLogHeader.GSR) > 0)
				) {
			int iGSR = getSignalIndex(Shimmer3Configuration.GSR_OBJECTCLUSTER_SENSORNAME);
			tempData[0] = (double)newPacketInt[iGSR];
			int newGSRRange = -1; // initialized to -1 so it will only come into play if mGSRRange = 4  

			double p1=0,p2=0;//,p3=0,p4=0,p5=0;
			if (mGSRRange==4){
				newGSRRange=(49152 & (int)tempData[0])>>14; 
			}
			if (mGSRRange==0 || newGSRRange==0) { //Note that from FW 1.0 onwards the MSB of the GSR data contains the range
				// the polynomial function used for calibration has been deprecated, it is replaced with a linear function
				/* p1 = 6.5995E-9;
	                    p2 = -6.895E-5;
	                    p3 = 2.699E-1;
	                    p4 = -4.769835E+2;
	                    p5 = 3.403513341E+5;*/
				if (mShimmerVersion!=HW_ID_SHIMMER_3){
					p1 = 0.0373;
					p2 = -24.9915;
				} else {
					p1 = 0.0363;
					p2 = -24.8617;
				}
			} else if (mGSRRange==1 || newGSRRange==1) {
				/*p1 = 1.3569627E-8;
	                    p2 = -1.650399E-4;
	                    p3 = 7.54199E-1;
	                    p4 = -1.5726287856E+3;
	                    p5 = 1.367507927E+6;*/
				if (mShimmerVersion!=HW_ID_SHIMMER_3){
					p1 = 0.0054;
					p2 = -3.5194;
				} else {
					p1 = 0.0051;
					p2 = -3.8357;
				}
			} else if (mGSRRange==2 || newGSRRange==2) {
				/*p1 = 2.550036498E-8;
	                    p2 = -3.3136E-4;
	                    p3 = 1.6509426597E+0;
	                    p4 = -3.833348044E+3;
	                    p5 = 3.8063176947E+6;*/
				if (mShimmerVersion!=HW_ID_SHIMMER_3){
					p1 = 0.0015;
					p2 = -1.0163;
				} else {
					p1 = 0.0015;
					p2 = -1.0067;
				}
			} else if (mGSRRange==3  || newGSRRange==3) {
				/*p1 = 3.7153627E-7;
	                    p2 = -4.239437E-3;
	                    p3 = 1.7905709E+1;
	                    p4 = -3.37238657E+4;
	                    p5 = 2.53680446279E+7;*/
				if (mShimmerVersion!=HW_ID_SHIMMER_3){
					p1 = 4.5580e-04;
					p2 = -0.3014;
				} else {
					p1 = 4.4513e-04;
					p2 = -0.3193;
				}
			}
			objectCluster.mPropertyCluster.put("GSR",new FormatCluster("RAW",NO_UNIT,(double)newPacketInt[iGSR]));
			uncalibratedData[iGSR]=(double)newPacketInt[iGSR];
			uncalibratedDataUnits[iGSR]=NO_UNIT;
			if (mEnableCalibration){
				calibratedData[iGSR] = calibrateGsrData(tempData[0],p1,p2);
				calibratedDataUnits[iGSR]=GSR_CAL_UNIT;
				objectCluster.mPropertyCluster.put("GSR",new FormatCluster("CAL",GSR_CAL_UNIT,calibratedData[iGSR]));
			}
		}
		
		if (((fwIdentifier == FW_IDEN_SD) && (mEnabledSensors & SDLogHeader.GYRO_MPU_MPL) > 0)){
			int iGyroX = getSignalIndex(Shimmer3Configuration.GYRO_MPU_MPL_X_OBJECTCLUSTER_SENSORNAME);
			int iGyroY = getSignalIndex(Shimmer3Configuration.GYRO_MPU_MPL_Y_OBJECTCLUSTER_SENSORNAME);
			int iGyroZ = getSignalIndex(Shimmer3Configuration.GYRO_MPU_MPL_Z_OBJECTCLUSTER_SENSORNAME);
			calibratedData[iGyroX] = (double)newPacketInt[iGyroX]/Math.pow(2, 16);
			calibratedData[iGyroY] = (double)newPacketInt[iGyroY]/Math.pow(2, 16);
			calibratedData[iGyroZ] = (double)newPacketInt[iGyroZ]/Math.pow(2, 16);
			calibratedDataUnits[iGyroX] = GYRO_CAL_UNIT;
			calibratedDataUnits[iGyroY] = GYRO_CAL_UNIT;
			calibratedDataUnits[iGyroZ] = GYRO_CAL_UNIT;
			uncalibratedData[iGyroX] = (double)newPacketInt[iGyroX];
			uncalibratedData[iGyroY] = (double)newPacketInt[iGyroY];
			uncalibratedData[iGyroZ] = (double)newPacketInt[iGyroZ];
			uncalibratedDataUnits[iGyroX] = NO_UNIT;
			uncalibratedDataUnits[iGyroY] = NO_UNIT;
			uncalibratedDataUnits[iGyroZ] = NO_UNIT;
		}
		
		if (((fwIdentifier == FW_IDEN_SD) && (mEnabledSensors & SDLogHeader.ACCEL_MPU_MPL) > 0)){
			int iAccelX = getSignalIndex(Shimmer3Configuration.ACCEL_MPU_MPL_X_OBJECTCLUSTER_SENSORNAME);
			int iAccelY = getSignalIndex(Shimmer3Configuration.ACCEL_MPU_MPL_Y_OBJECTCLUSTER_SENSORNAME);
			int iAccelZ = getSignalIndex(Shimmer3Configuration.ACCEL_MPU_MPL_Z_OBJECTCLUSTER_SENSORNAME);
			calibratedData[iAccelX] = (double)newPacketInt[iAccelX]/Math.pow(2, 16);
			calibratedData[iAccelY] = (double)newPacketInt[iAccelY]/Math.pow(2, 16);
			calibratedData[iAccelZ] = (double)newPacketInt[iAccelZ]/Math.pow(2, 16);
			calibratedDataUnits[iAccelX] = ACCEL_CAL_UNIT;
			calibratedDataUnits[iAccelY] = ACCEL_CAL_UNIT;
			calibratedDataUnits[iAccelZ] = ACCEL_CAL_UNIT;
			uncalibratedData[iAccelX] = (double)newPacketInt[iAccelX];
			uncalibratedData[iAccelY] = (double)newPacketInt[iAccelX];
			uncalibratedData[iAccelZ] = (double)newPacketInt[iAccelX];
			uncalibratedDataUnits[iAccelX] = NO_UNIT;
			uncalibratedDataUnits[iAccelY] = NO_UNIT;
			uncalibratedDataUnits[iAccelZ] = NO_UNIT;
		}
		
		if (((fwIdentifier == FW_IDEN_SD) && (mEnabledSensors & SDLogHeader.MAG_MPU_MPL) > 0)){
			int iMagX = getSignalIndex(Shimmer3Configuration.MAG_MPU_MPL_X_OBJECTCLUSTER_SENSORNAME);
			int iMagY = getSignalIndex(Shimmer3Configuration.MAG_MPU_MPL_Y_OBJECTCLUSTER_SENSORNAME);
			int iMagZ = getSignalIndex(Shimmer3Configuration.MAG_MPU_MPL_Z_OBJECTCLUSTER_SENSORNAME);
			calibratedData[iMagX] = (double)newPacketInt[iMagX]/Math.pow(2, 16);
			calibratedData[iMagY] = (double)newPacketInt[iMagY]/Math.pow(2, 16);
			calibratedData[iMagZ] = (double)newPacketInt[iMagZ]/Math.pow(2, 16);
			calibratedDataUnits[iMagX] = MAG_CAL_UNIT;
			calibratedDataUnits[iMagY] = MAG_CAL_UNIT;
			calibratedDataUnits[iMagZ] = MAG_CAL_UNIT;
			uncalibratedData[iMagX] = (double)newPacketInt[iMagX];
			uncalibratedData[iMagY] = (double)newPacketInt[iMagY];
			uncalibratedData[iMagZ] = (double)newPacketInt[iMagZ];
			uncalibratedDataUnits[iMagX] = NO_UNIT;
			uncalibratedDataUnits[iMagY] = NO_UNIT;
			uncalibratedDataUnits[iMagZ] = NO_UNIT;
		}
		
		if (((fwIdentifier == FW_IDEN_SD) && (mEnabledSensors & SDLogHeader.MPL_QUAT_6DOF) > 0)){
			int iQW = getSignalIndex(Shimmer3Configuration.QUAT_MPL_6DOF_W_OBJECTCLUSTER_SENSORNAME);
			int iQX = getSignalIndex(Shimmer3Configuration.QUAT_MPL_6DOF_X_OBJECTCLUSTER_SENSORNAME);
			int iQY = getSignalIndex(Shimmer3Configuration.QUAT_MPL_6DOF_Y_OBJECTCLUSTER_SENSORNAME);
			int iQZ = getSignalIndex(Shimmer3Configuration.QUAT_MPL_6DOF_Z_OBJECTCLUSTER_SENSORNAME);
			calibratedData[iQW] = (double)newPacketInt[iQW]/Math.pow(2, 30);
			calibratedData[iQX] = (double)newPacketInt[iQX]/Math.pow(2, 30);
			calibratedData[iQY] = (double)newPacketInt[iQY]/Math.pow(2, 30);
			calibratedData[iQZ] = (double)newPacketInt[iQZ]/Math.pow(2, 30);
			calibratedDataUnits[iQW] = NO_UNIT;
			calibratedDataUnits[iQX] = NO_UNIT;
			calibratedDataUnits[iQY] = NO_UNIT;
			calibratedDataUnits[iQZ] = NO_UNIT;
			uncalibratedData[iQW] = (double)newPacketInt[iQW];
			uncalibratedData[iQX] = (double)newPacketInt[iQX];
			uncalibratedData[iQY] = (double)newPacketInt[iQY];
			uncalibratedData[iQZ] = (double)newPacketInt[iQZ];
			uncalibratedDataUnits[iQW] = NO_UNIT;
			uncalibratedDataUnits[iQX] = NO_UNIT;
			uncalibratedDataUnits[iQY] = NO_UNIT;
			calibratedDataUnits[iQZ] = NO_UNIT;
		}
		if (((fwIdentifier == FW_IDEN_SD) && (mEnabledSensors & SDLogHeader.MPL_TEMPERATURE) > 0)){
			int iT = getSignalIndex(Shimmer3Configuration.MPL_TEMPERATURE_OBJECTCLUSTER_SENSORNAME);
			calibratedData[iT] = (double)newPacketInt[iT]/Math.pow(2, 16);
			calibratedDataUnits[iT] = TEMP_CAL_UNIT;
			uncalibratedData[iT] = (double)newPacketInt[iT];
			uncalibratedDataUnits[iT] = NO_UNIT;
		}
		objectCluster.mCalData = calibratedData;
		objectCluster.mUncalData = uncalibratedData;
		objectCluster.mUnitCal = calibratedDataUnits;
		objectCluster.mUnitUncal = uncalibratedDataUnits;
		objectCluster.mSensorNames = sensorNames;
		return objectCluster; 
	}
	
	
	protected Object buildMsg(byte[] newPacket, Object object) {
		ObjectCluster objectCluster = (ObjectCluster) object;
		objectCluster.mRawData = newPacket;
		objectCluster.mSystemTimeStamp=ByteBuffer.allocate(8).putLong(System.currentTimeMillis()).array();
		double [] calibratedData=new double[mNChannels + 1]; //plus 1 because of the time stamp
		long[] newPacketInt=parsedData(newPacket,mSignalDataTypeArray);
		double[] tempData=new double[3];
		Vector3d accelerometer = new Vector3d();
		Vector3d magnetometer = new Vector3d();
		Vector3d gyroscope = new Vector3d();

		int iTimeStamp=getSignalIndex("TimeStamp"); //find index
		tempData[0]=(double)newPacketInt[1];
		objectCluster.mPropertyCluster.put("Timestamp",new FormatCluster("RAW",NO_UNIT,(double)newPacketInt[iTimeStamp]));
		if (mEnableCalibration){
			objectCluster.mPropertyCluster.put("Timestamp",new FormatCluster("CAL","mSecs",calibrateTimeStamp((double)newPacketInt[iTimeStamp])));
		}
		objectCluster = callAdditionalServices(objectCluster);


		if (mShimmerVersion==HW_ID_SHIMMER_SR30 || mShimmerVersion==HW_ID_SHIMMER_3){
			if (((mEnabledSensors & SENSOR_ACCEL) > 0)){
				int iAccelX=getSignalIndex("Low Noise Accelerometer X"); //find index
				int iAccelY=getSignalIndex("Low Noise Accelerometer Y"); //find index
				int iAccelZ=getSignalIndex("Low Noise Accelerometer Z"); //find index
				tempData[0]=(double)newPacketInt[iAccelX];
				tempData[1]=(double)newPacketInt[iAccelY];
				tempData[2]=(double)newPacketInt[iAccelZ];

				objectCluster.mPropertyCluster.put("Low Noise Accelerometer X",new FormatCluster("RAW",NO_UNIT,(double)newPacketInt[iAccelX]));
				objectCluster.mPropertyCluster.put("Low Noise Accelerometer Y",new FormatCluster("RAW",NO_UNIT,(double)newPacketInt[iAccelY]));
				objectCluster.mPropertyCluster.put("Low Noise Accelerometer Z",new FormatCluster("RAW",NO_UNIT,(double)newPacketInt[iAccelZ]));

				if (mEnableCalibration){
					double[] accelCalibratedData;
					accelCalibratedData=calibrateInertialSensorData(tempData, mAlignmentMatrixAnalogAccel, mSensitivityMatrixAnalogAccel, mOffsetVectorAnalogAccel);
					calibratedData[iAccelX]=accelCalibratedData[0];
					calibratedData[iAccelY]=accelCalibratedData[1];
					calibratedData[iAccelZ]=accelCalibratedData[2];
					if (mDefaultCalibrationParametersAccel == true) {
						objectCluster.mPropertyCluster.put("Low Noise Accelerometer X",new FormatCluster("CAL","m/(sec^2)*",accelCalibratedData[0]));
						objectCluster.mPropertyCluster.put("Low Noise Accelerometer Y",new FormatCluster("CAL","m/(sec^2)*",accelCalibratedData[1]));
						objectCluster.mPropertyCluster.put("Low Noise Accelerometer Z",new FormatCluster("CAL","m/(sec^2)*",accelCalibratedData[2]));
						accelerometer.x=accelCalibratedData[0];
						accelerometer.y=accelCalibratedData[1];
						accelerometer.z=accelCalibratedData[2];

					} else {
						objectCluster.mPropertyCluster.put("Low Noise Accelerometer X",new FormatCluster("CAL","m/(sec^2)",accelCalibratedData[0]));
						objectCluster.mPropertyCluster.put("Low Noise Accelerometer Y",new FormatCluster("CAL","m/(sec^2)",accelCalibratedData[1]));
						objectCluster.mPropertyCluster.put("Low Noise Accelerometer Z",new FormatCluster("CAL","m/(sec^2)",accelCalibratedData[2]));
						accelerometer.x=accelCalibratedData[0];
						accelerometer.y=accelCalibratedData[1];
						accelerometer.z=accelCalibratedData[2];

					}
				}
			}
			if (((mEnabledSensors & SENSOR_DACCEL) > 0)){
				int iAccelX=getSignalIndex("Wide Range Accelerometer X"); //find index
				int iAccelY=getSignalIndex("Wide Range Accelerometer Y"); //find index
				int iAccelZ=getSignalIndex("Wide Range Accelerometer Z"); //find index
				//check range

				tempData[0]=(double)newPacketInt[iAccelX];
				tempData[1]=(double)newPacketInt[iAccelY];
				tempData[2]=(double)newPacketInt[iAccelZ];
				objectCluster.mPropertyCluster.put("Wide Range Accelerometer X",new FormatCluster("RAW",NO_UNIT,(double)newPacketInt[iAccelX]));
				objectCluster.mPropertyCluster.put("Wide Range Accelerometer Y",new FormatCluster("RAW",NO_UNIT,(double)newPacketInt[iAccelY]));
				objectCluster.mPropertyCluster.put("Wide Range Accelerometer Z",new FormatCluster("RAW",NO_UNIT,(double)newPacketInt[iAccelZ]));


				if (mEnableCalibration){
					double[] accelCalibratedData;
					accelCalibratedData=calibrateInertialSensorData(tempData, mAlignmentMatrixWRAccel, mSensitivityMatrixWRAccel, mOffsetVectorWRAccel);
					calibratedData[iAccelX]=accelCalibratedData[0];
					calibratedData[iAccelY]=accelCalibratedData[1];
					calibratedData[iAccelZ]=accelCalibratedData[2];
					if (mDefaultCalibrationParametersDigitalAccel == true) {
						objectCluster.mPropertyCluster.put("Wide Range Accelerometer X",new FormatCluster("CAL","m/(sec^2)*",calibratedData[iAccelX]));
						objectCluster.mPropertyCluster.put("Wide Range Accelerometer Y",new FormatCluster("CAL","m/(sec^2)*",calibratedData[iAccelY]));
						objectCluster.mPropertyCluster.put("Wide Range Accelerometer Z",new FormatCluster("CAL","m/(sec^2)*",calibratedData[iAccelZ]));
						if (((mEnabledSensors & SENSOR_ACCEL) == 0)){
							accelerometer.x=accelCalibratedData[0]; //this is used to calculate quaternions // skip if Low noise is enabled
							accelerometer.y=accelCalibratedData[1];
							accelerometer.z=accelCalibratedData[2];
						}
					} else {
						objectCluster.mPropertyCluster.put("Wide Range Accelerometer X",new FormatCluster("CAL","m/(sec^2)",calibratedData[iAccelX]));
						objectCluster.mPropertyCluster.put("Wide Range Accelerometer Y",new FormatCluster("CAL","m/(sec^2)",calibratedData[iAccelY]));
						objectCluster.mPropertyCluster.put("Wide Range Accelerometer Z",new FormatCluster("CAL","m/(sec^2)",calibratedData[iAccelZ]));
						if (((mEnabledSensors & SENSOR_ACCEL) == 0)){
							accelerometer.x=accelCalibratedData[0];
							accelerometer.y=accelCalibratedData[1];
							accelerometer.z=accelCalibratedData[2];
						}

					}	
				}
			}
			if (((mEnabledSensors & 0xFF)& SENSOR_GYRO) > 0) {
				int iGyroX=getSignalIndex("Gyroscope X");
				int iGyroY=getSignalIndex("Gyroscope Y");
				int iGyroZ=getSignalIndex("Gyroscope Z");
				tempData[0]=(double)newPacketInt[iGyroX];
				tempData[1]=(double)newPacketInt[iGyroY];
				tempData[2]=(double)newPacketInt[iGyroZ];


				objectCluster.mPropertyCluster.put("Gyroscope X",new FormatCluster("RAW",NO_UNIT,(double)newPacketInt[iGyroX]));
				objectCluster.mPropertyCluster.put("Gyroscope Y",new FormatCluster("RAW",NO_UNIT,(double)newPacketInt[iGyroY]));
				objectCluster.mPropertyCluster.put("Gyroscope Z",new FormatCluster("RAW",NO_UNIT,(double)newPacketInt[iGyroZ]));

				if (mEnableCalibration){
					double[] gyroCalibratedData=calibrateInertialSensorData(tempData, mAlignmentMatrixGyroscope, mSensitivityMatrixGyroscope, mOffsetVectorGyroscope);
					calibratedData[iGyroX]=gyroCalibratedData[0];
					calibratedData[iGyroY]=gyroCalibratedData[1];
					calibratedData[iGyroZ]=gyroCalibratedData[2];
					if (mDefaultCalibrationParametersGyro == true) {
						objectCluster.mPropertyCluster.put("Gyroscope X",new FormatCluster("CAL","deg/sec*",gyroCalibratedData[0]));
						objectCluster.mPropertyCluster.put("Gyroscope Y",new FormatCluster("CAL","deg/sec*",gyroCalibratedData[1]));
						objectCluster.mPropertyCluster.put("Gyroscope Z",new FormatCluster("CAL","deg/sec*",gyroCalibratedData[2]));
						gyroscope.x=gyroCalibratedData[0]*Math.PI/180;
						gyroscope.y=gyroCalibratedData[1]*Math.PI/180;
						gyroscope.z=gyroCalibratedData[2]*Math.PI/180;
					} else {
						objectCluster.mPropertyCluster.put("Gyroscope X",new FormatCluster("CAL","deg/sec",gyroCalibratedData[0]));
						objectCluster.mPropertyCluster.put("Gyroscope Y",new FormatCluster("CAL","deg/sec",gyroCalibratedData[1]));
						objectCluster.mPropertyCluster.put("Gyroscope Z",new FormatCluster("CAL","deg/sec",gyroCalibratedData[2]));
						gyroscope.x=gyroCalibratedData[0]*Math.PI/180;
						gyroscope.y=gyroCalibratedData[1]*Math.PI/180;
						gyroscope.z=gyroCalibratedData[2]*Math.PI/180;
						if (mEnableOntheFlyGyroOVCal){
							mGyroX.addValue(gyroCalibratedData[0]);
							mGyroY.addValue(gyroCalibratedData[1]);
							mGyroZ.addValue(gyroCalibratedData[2]);
							mGyroXRaw.addValue((double)newPacketInt[iGyroX]);
							mGyroYRaw.addValue((double)newPacketInt[iGyroY]);
							mGyroZRaw.addValue((double)newPacketInt[iGyroZ]);
							if (mGyroX.getStandardDeviation()<mGyroOVCalThreshold && mGyroY.getStandardDeviation()<mGyroOVCalThreshold && mGyroZ.getStandardDeviation()<mGyroOVCalThreshold){
								mOffsetVectorGyroscope[0][0]=mGyroXRaw.getMean();
								mOffsetVectorGyroscope[1][0]=mGyroYRaw.getMean();
								mOffsetVectorGyroscope[2][0]=mGyroZRaw.getMean();
							}
						}
					} 
				}

			}
			if (((mEnabledSensors & 0xFF)& SENSOR_MAG) > 0) {
				int iMagX=getSignalIndex("Magnetometer X");
				int iMagY=getSignalIndex("Magnetometer Y");
				int iMagZ=getSignalIndex("Magnetometer Z");
				tempData[0]=(double)newPacketInt[iMagX];
				tempData[1]=(double)newPacketInt[iMagY];
				tempData[2]=(double)newPacketInt[iMagZ];
				objectCluster.mPropertyCluster.put("Magnetometer X",new FormatCluster("RAW",NO_UNIT,(double)newPacketInt[iMagX]));
				objectCluster.mPropertyCluster.put("Magnetometer Y",new FormatCluster("RAW",NO_UNIT,(double)newPacketInt[iMagY]));
				objectCluster.mPropertyCluster.put("Magnetometer Z",new FormatCluster("RAW",NO_UNIT,(double)newPacketInt[iMagZ]));
				if (mEnableCalibration){
					double[] magCalibratedData=calibrateInertialSensorData(tempData, mAlignmentMatrixMagnetometer, mSensitivityMatrixMagnetometer, mOffsetVectorMagnetometer);
					calibratedData[iMagX]=magCalibratedData[0];
					calibratedData[iMagY]=magCalibratedData[1];
					calibratedData[iMagZ]=magCalibratedData[2];
					if (mDefaultCalibrationParametersMag == true) {
						objectCluster.mPropertyCluster.put("Magnetometer X",new FormatCluster("CAL","local*",magCalibratedData[0]));
						objectCluster.mPropertyCluster.put("Magnetometer Y",new FormatCluster("CAL","local*",magCalibratedData[1]));
						objectCluster.mPropertyCluster.put("Magnetometer Z",new FormatCluster("CAL","local*",magCalibratedData[2]));
						magnetometer.x=magCalibratedData[0];
						magnetometer.y=magCalibratedData[1];
						magnetometer.z=magCalibratedData[2];
					} else {
						objectCluster.mPropertyCluster.put("Magnetometer X",new FormatCluster("CAL","local",magCalibratedData[0]));
						objectCluster.mPropertyCluster.put("Magnetometer Y",new FormatCluster("CAL","local",magCalibratedData[1]));
						objectCluster.mPropertyCluster.put("Magnetometer Z",new FormatCluster("CAL","local",magCalibratedData[2]));
						magnetometer.x=magCalibratedData[0];
						magnetometer.y=magCalibratedData[1];
						magnetometer.z=magCalibratedData[2];
					}
				}
			}

			if ((mEnabledSensors & SENSOR_BATT) > 0) {
				int iA0 = getSignalIndex("VSenseBatt");
				tempData[0] = (double)newPacketInt[iA0];
				objectCluster.mPropertyCluster.put("VSenseBatt",new FormatCluster("RAW",NO_UNIT,(double)newPacketInt[iA0]));
				if (mEnableCalibration){
					calibratedData[iA0]=calibrateU12AdcValue(tempData[0],0,3,1)*1.988;
					objectCluster.mPropertyCluster.put("VSenseBatt",new FormatCluster("CAL","mVolts",calibratedData[iA0]));
					mVSenseBattMA.addValue(calibratedData[iA0]);
					checkBattery();
				}
			}
			if ((mEnabledSensors & SENSOR_EXT_ADC_A7) > 0) {
				int iA0 = getSignalIndex("External ADC A7");
				tempData[0] = (double)newPacketInt[iA0];
				objectCluster.mPropertyCluster.put("External ADC A7",new FormatCluster("RAW",NO_UNIT,(double)newPacketInt[iA0]));
				if (mEnableCalibration){
					calibratedData[iA0]=calibrateU12AdcValue(tempData[0],0,3,1);
					objectCluster.mPropertyCluster.put("External ADC A7",new FormatCluster("CAL","mVolts",calibratedData[iA0]));
				}
			}
			if ((mEnabledSensors & SENSOR_EXT_ADC_A6) > 0) {
				int iA0 = getSignalIndex("External ADC A6");
				tempData[0] = (double)newPacketInt[iA0];
				objectCluster.mPropertyCluster.put("External ADC A6",new FormatCluster("RAW",NO_UNIT,(double)newPacketInt[iA0]));
				if (mEnableCalibration){
					calibratedData[iA0]=calibrateU12AdcValue(tempData[0],0,3,1);
					objectCluster.mPropertyCluster.put("External ADC A6",new FormatCluster("CAL","mVolts",calibratedData[iA0]));
				}
			}
			if ((mEnabledSensors & SENSOR_EXT_ADC_A15) > 0) {
				int iA0 = getSignalIndex("External ADC A15");
				tempData[0] = (double)newPacketInt[iA0];
				objectCluster.mPropertyCluster.put("External ADC A15",new FormatCluster("RAW",NO_UNIT,(double)newPacketInt[iA0]));
				if (mEnableCalibration){
					calibratedData[iA0]=calibrateU12AdcValue(tempData[0],0,3,1);
					objectCluster.mPropertyCluster.put("External ADC A15",new FormatCluster("CAL","mVolts",calibratedData[iA0]));
				}
			}
			if ((mEnabledSensors & SENSOR_INT_ADC_A1) > 0) {
				int iA0 = getSignalIndex("Internal ADC A1");
				tempData[0] = (double)newPacketInt[iA0];
				objectCluster.mPropertyCluster.put("Internal ADC A1",new FormatCluster("RAW",NO_UNIT,(double)newPacketInt[iA0]));
				if (mEnableCalibration){
					calibratedData[iA0]=calibrateU12AdcValue(tempData[0],0,3,1);
					objectCluster.mPropertyCluster.put("Internal ADC A1",new FormatCluster("CAL","mVolts",calibratedData[iA0]));
				}
			}
			if ((mEnabledSensors & SENSOR_INT_ADC_A12) > 0) {
				int iA0 = getSignalIndex("Internal ADC A12");
				tempData[0] = (double)newPacketInt[iA0];
				objectCluster.mPropertyCluster.put("Internal ADC A12",new FormatCluster("RAW",NO_UNIT,(double)newPacketInt[iA0]));
				if (mEnableCalibration){
					calibratedData[iA0]=calibrateU12AdcValue(tempData[0],0,3,1);
					objectCluster.mPropertyCluster.put("Internal ADC A12",new FormatCluster("CAL","mVolts",calibratedData[iA0]));
				}
			}
			if ((mEnabledSensors & SENSOR_INT_ADC_A13) > 0) {
				int iA0 = getSignalIndex("Internal ADC A13");
				tempData[0] = (double)newPacketInt[iA0];
				objectCluster.mPropertyCluster.put("Internal ADC A13",new FormatCluster("RAW",NO_UNIT,(double)newPacketInt[iA0]));
				if (mEnableCalibration){
					calibratedData[iA0]=calibrateU12AdcValue(tempData[0],0,3,1);
					objectCluster.mPropertyCluster.put("Internal ADC A13",new FormatCluster("CAL","mVolts",calibratedData[iA0]));
				}
			}
			if ((mEnabledSensors & SENSOR_INT_ADC_A14) > 0) {
				int iA0 = getSignalIndex("Internal ADC A14");
				tempData[0] = (double)newPacketInt[iA0];
				objectCluster.mPropertyCluster.put("Internal ADC A14",new FormatCluster("RAW",NO_UNIT,(double)newPacketInt[iA0]));
				if (mEnableCalibration){
					calibratedData[iA0]=calibrateU12AdcValue(tempData[0],0,3,1);
					objectCluster.mPropertyCluster.put("Internal ADC A14",new FormatCluster("CAL","mVolts",calibratedData[iA0]));
				}
			}
			if (((mEnabledSensors & SENSOR_ACCEL) > 0 || (mEnabledSensors & SENSOR_DACCEL) > 0) && ((mEnabledSensors & 0xFF)& SENSOR_GYRO) > 0 && ((mEnabledSensors & 0xFF)& SENSOR_MAG) > 0 && mOrientationEnabled ){
				if (mEnableCalibration){
					if (mOrientationAlgo==null){
						mOrientationAlgo = new GradDes3DOrientation(0.4, (double)1/mSamplingRate, 1, 0, 0,0);
					}
					Quaternion q = mOrientationAlgo.update(accelerometer.x,accelerometer.y,accelerometer.z, gyroscope.x,gyroscope.y,gyroscope.z, magnetometer.x,magnetometer.y,magnetometer.z);					double theta, Rx, Ry, Rz, rho;
					rho = Math.acos(q.q1);
					theta = rho * 2;
					Rx = q.q2 / Math.sin(rho);
					Ry = q.q3 / Math.sin(rho);
					Rz = q.q4 / Math.sin(rho);
					objectCluster.mPropertyCluster.put("Axis Angle A",new FormatCluster("CAL","local",theta));
					objectCluster.mPropertyCluster.put("Axis Angle X",new FormatCluster("CAL","local",Rx));
					objectCluster.mPropertyCluster.put("Axis Angle Y",new FormatCluster("CAL","local",Ry));
					objectCluster.mPropertyCluster.put("Axis Angle Z",new FormatCluster("CAL","local",Rz));
					objectCluster.mPropertyCluster.put("Quaternion 0",new FormatCluster("CAL","local",q.q1));
					objectCluster.mPropertyCluster.put("Quaternion 1",new FormatCluster("CAL","local",q.q2));
					objectCluster.mPropertyCluster.put("Quaternion 2",new FormatCluster("CAL","local",q.q3));
					objectCluster.mPropertyCluster.put("Quaternion 3",new FormatCluster("CAL","local",q.q4));
				}
			}

			if ((mEnabledSensors & SENSOR_EXG1_24BIT) >0){
				int iexg1ch1 = getSignalIndex("EXG1 24Bit CH1");
				int iexg1ch2 = getSignalIndex("EXG1 24Bit CH2");
				int iexg1sta = getSignalIndex("EXG1 STATUS");
				double exg1ch1 = (double)newPacketInt[iexg1ch1];
				double exg1ch2 = (double)newPacketInt[iexg1ch2];
				double exg1sta = (double)newPacketInt[iexg1sta];
				objectCluster.mPropertyCluster.put("EXG1 STATUS",new FormatCluster("RAW",NO_UNIT,exg1sta));
				if (mEnableCalibration){
					double calexg1ch1 = exg1ch1 *(((2.42*1000)/mEXG1CH1GainValue)/(Math.pow(2,23)-1));
					double calexg1ch2 = exg1ch2 *(((2.42*1000)/mEXG1CH2GainValue)/(Math.pow(2,23)-1));
					if (isEXGUsingDefaultECGConfiguration()){
						objectCluster.mPropertyCluster.put("ECG LL-RA",new FormatCluster("RAW",NO_UNIT,exg1ch1));
						objectCluster.mPropertyCluster.put("ECG LA-RA",new FormatCluster("RAW",NO_UNIT,exg1ch2));
						objectCluster.mPropertyCluster.put("ECG LL-RA",new FormatCluster("CAL","mVolts",calexg1ch1));
						objectCluster.mPropertyCluster.put("ECG LA-RA",new FormatCluster("CAL","mVolts",calexg1ch2));
					} else if (isEXGUsingDefaultEMGConfiguration()){
						objectCluster.mPropertyCluster.put("EMG CH1",new FormatCluster("RAW",NO_UNIT,exg1ch1));
						objectCluster.mPropertyCluster.put("EMG CH2",new FormatCluster("RAW",NO_UNIT,exg1ch2));
						objectCluster.mPropertyCluster.put("EMG CH1",new FormatCluster("CAL","mVolts",calexg1ch1));
						objectCluster.mPropertyCluster.put("EMG CH2",new FormatCluster("CAL","mVolts",calexg1ch2));
					} else {
						objectCluster.mPropertyCluster.put("EXG1 CH1",new FormatCluster("RAW",NO_UNIT,exg1ch1));
						objectCluster.mPropertyCluster.put("EXG1 CH2",new FormatCluster("RAW",NO_UNIT,exg1ch2));
						objectCluster.mPropertyCluster.put("EXG1 CH1",new FormatCluster("CAL","mVolts",calexg1ch1));
						objectCluster.mPropertyCluster.put("EXG1 CH2",new FormatCluster("CAL","mVolts",calexg1ch2));
					}
				}
			}
			if ((mEnabledSensors & SENSOR_EXG2_24BIT) >0){
				int iexg2ch1 = getSignalIndex("EXG2 24Bit CH1");
				int iexg2ch2 = getSignalIndex("EXG2 24Bit CH2");
				int iexg2sta = getSignalIndex("EXG2 STATUS");
				double exg2ch1 = (double)newPacketInt[iexg2ch1];
				double exg2ch2 = (double)newPacketInt[iexg2ch2];
				double exg2sta = (double)newPacketInt[iexg2sta];

				objectCluster.mPropertyCluster.put("EXG2 STATUS",new FormatCluster("RAW",NO_UNIT,exg2sta));
				if (mEnableCalibration){
					double calexg2ch1 = exg2ch1 *(((2.42*1000)/mEXG2CH1GainValue)/(Math.pow(2,23)-1));
					double calexg2ch2 = exg2ch2 *(((2.42*1000)/mEXG2CH2GainValue)/(Math.pow(2,23)-1));
					if (isEXGUsingDefaultECGConfiguration()){
						objectCluster.mPropertyCluster.put("EXG2 CH1",new FormatCluster("RAW",NO_UNIT,exg2ch1));
						objectCluster.mPropertyCluster.put("ECG Vx-RL",new FormatCluster("RAW",NO_UNIT,exg2ch2));
						objectCluster.mPropertyCluster.put("EXG2 CH1",new FormatCluster("CAL","mVolts",calexg2ch1));
						objectCluster.mPropertyCluster.put("ECG Vx-RL",new FormatCluster("CAL","mVolts",calexg2ch2));
					}
					else if (isEXGUsingDefaultEMGConfiguration()){
						objectCluster.mPropertyCluster.put("EXG2 CH1",new FormatCluster("RAW",NO_UNIT,0));
						objectCluster.mPropertyCluster.put("EXG2 CH2",new FormatCluster("RAW",NO_UNIT,0));
						objectCluster.mPropertyCluster.put("EXG2 CH1",new FormatCluster("CAL","mVolts",0));
						objectCluster.mPropertyCluster.put("EXG2 CH2",new FormatCluster("CAL","mVolts",0));
					} else {
						objectCluster.mPropertyCluster.put("EXG2 CH1",new FormatCluster("RAW",NO_UNIT,exg2ch1));
						objectCluster.mPropertyCluster.put("EXG2 CH2",new FormatCluster("RAW",NO_UNIT,exg2ch2));
						objectCluster.mPropertyCluster.put("EXG2 CH1",new FormatCluster("CAL","mVolts",calexg2ch1));
						objectCluster.mPropertyCluster.put("EXG2 CH2",new FormatCluster("CAL","mVolts",calexg2ch2));	
					}
				}
			}

			if ((mEnabledSensors & SENSOR_EXG1_16BIT) >0){
				int iexg1ch1 = getSignalIndex("EXG1 16Bit CH1");
				int iexg1ch2 = getSignalIndex("EXG1 16Bit CH2");
				int iexg1sta = getSignalIndex("EXG1 STATUS");
				double exg1ch1 = (double)newPacketInt[iexg1ch1];
				double exg1ch2 = (double)newPacketInt[iexg1ch2];
				double exg1sta = (double)newPacketInt[iexg1sta];
				objectCluster.mPropertyCluster.put("EXG1 STATUS",new FormatCluster("RAW",NO_UNIT,exg1sta));
				if (mEnableCalibration){
					double calexg1ch1 = exg1ch1 *(((2.42*1000)/(mEXG1CH1GainValue*2))/(Math.pow(2,15)-1));
					double calexg1ch2 = exg1ch2 *(((2.42*1000)/(mEXG1CH2GainValue*2))/(Math.pow(2,15)-1));
					if (isEXGUsingDefaultECGConfiguration()){
						objectCluster.mPropertyCluster.put("ECG LL-RA",new FormatCluster("RAW",NO_UNIT,exg1ch1));
						objectCluster.mPropertyCluster.put("ECG LA-RA",new FormatCluster("RAW",NO_UNIT,exg1ch2));
						objectCluster.mPropertyCluster.put("ECG LL-RA",new FormatCluster("CAL","mVolts",calexg1ch1));
						objectCluster.mPropertyCluster.put("ECG LA-RA",new FormatCluster("CAL","mVolts",calexg1ch2));
					} else if (isEXGUsingDefaultEMGConfiguration()){
						objectCluster.mPropertyCluster.put("EMG CH1",new FormatCluster("RAW",NO_UNIT,exg1ch1));
						objectCluster.mPropertyCluster.put("EMG CH2",new FormatCluster("RAW",NO_UNIT,exg1ch2));
						objectCluster.mPropertyCluster.put("EMG CH1",new FormatCluster("CAL","mVolts",calexg1ch1));
						objectCluster.mPropertyCluster.put("EMG CH2",new FormatCluster("CAL","mVolts",calexg1ch2));
					} else {
						objectCluster.mPropertyCluster.put("EXG1 CH1 16Bit",new FormatCluster("RAW",NO_UNIT,exg1ch1));
						objectCluster.mPropertyCluster.put("EXG1 CH2 16Bit",new FormatCluster("RAW",NO_UNIT,exg1ch2));
						objectCluster.mPropertyCluster.put("EXG1 CH1 16Bit",new FormatCluster("CAL","mVolts",calexg1ch1));
						objectCluster.mPropertyCluster.put("EXG1 CH2 16Bit",new FormatCluster("CAL","mVolts",calexg1ch2));
					}
				}
			}
			if ((mEnabledSensors & SENSOR_EXG2_16BIT) >0){
				int iexg2ch1 = getSignalIndex("EXG2 16Bit CH1");
				int iexg2ch2 = getSignalIndex("EXG2 16Bit CH2");
				int iexg2sta = getSignalIndex("EXG2 STATUS");
				double exg2ch1 = (double)newPacketInt[iexg2ch1];
				double exg2ch2 = (double)newPacketInt[iexg2ch2];
				double exg2sta = (double)newPacketInt[iexg2sta];

				objectCluster.mPropertyCluster.put("EXG2 STATUS",new FormatCluster("RAW",NO_UNIT,exg2sta));
				if (mEnableCalibration){
					double calexg2ch1 = ((exg2ch1)) *(((2.42*1000)/(mEXG2CH1GainValue*2))/(Math.pow(2,15)-1));
					double calexg2ch2 = ((exg2ch2)) *(((2.42*1000)/(mEXG2CH2GainValue*2))/(Math.pow(2,15)-1));
					if (isEXGUsingDefaultECGConfiguration()){
						objectCluster.mPropertyCluster.put("EXG2 CH1",new FormatCluster("RAW",NO_UNIT,exg2ch1));
						objectCluster.mPropertyCluster.put("ECG Vx-RL",new FormatCluster("RAW",NO_UNIT,exg2ch2));
						objectCluster.mPropertyCluster.put("EXG2 CH1",new FormatCluster("CAL","mVolts",calexg2ch1));
						objectCluster.mPropertyCluster.put("ECG Vx-RL",new FormatCluster("CAL","mVolts",calexg2ch2));
					}
					else if (isEXGUsingDefaultEMGConfiguration()){
						objectCluster.mPropertyCluster.put("EXG2 CH1",new FormatCluster("RAW",NO_UNIT,0));
						objectCluster.mPropertyCluster.put("EXG2 CH2",new FormatCluster("RAW",NO_UNIT,0));
						objectCluster.mPropertyCluster.put("EXG2 CH1",new FormatCluster("CAL","mVolts",0));
						objectCluster.mPropertyCluster.put("EXG2 CH2",new FormatCluster("CAL","mVolts",0));
					} else {
						objectCluster.mPropertyCluster.put("EXG2 CH1 16Bit",new FormatCluster("RAW",NO_UNIT,exg2ch1));
						objectCluster.mPropertyCluster.put("EXG2 CH2 16Bit",new FormatCluster("RAW",NO_UNIT,exg2ch2));
						objectCluster.mPropertyCluster.put("EXG2 CH1 16Bit",new FormatCluster("CAL","mVolts",calexg2ch1));
						objectCluster.mPropertyCluster.put("EXG2 CH2 16Bit",new FormatCluster("CAL","mVolts",calexg2ch2));
					}
				}
			}

			if ((mEnabledSensors & SENSOR_BMP180) >0){
				int iUT = getSignalIndex("Temperature");
				int iUP = getSignalIndex("Pressure");
				double UT = (double)newPacketInt[iUT];
				double UP = (double)newPacketInt[iUP];
				UP=UP/Math.pow(2,8-mPressureResolution);
				objectCluster.mPropertyCluster.put("Pressure",new FormatCluster("RAW",NO_UNIT,UP));
				objectCluster.mPropertyCluster.put("Temperature",new FormatCluster("RAW",NO_UNIT,UT));
				if (mEnableCalibration){
					double[] bmp180caldata= calibratePressureSensorData(UP,UT);
					objectCluster.mPropertyCluster.put("Pressure",new FormatCluster("CAL","kPa",bmp180caldata[0]/1000));
					objectCluster.mPropertyCluster.put("Temperature",new FormatCluster("CAL","Celsius",bmp180caldata[1]));
				}
			}

			if ((mEnabledSensors & SENSOR_BRIDGE_AMP) > 0) {
				int iBAHigh = getSignalIndex("Bridge Amplifier High");
				int iBALow = getSignalIndex("Bridge Amplifier Low");
				objectCluster.mPropertyCluster.put("Bridge Amplifier High",new FormatCluster("RAW",NO_UNIT,(double)newPacketInt[iBAHigh]));
				objectCluster.mPropertyCluster.put("Bridge Amplifier Low",new FormatCluster("RAW",NO_UNIT,(double)newPacketInt[iBALow]));
				if (mEnableCalibration){
					tempData[0] = (double)newPacketInt[iBAHigh];
					tempData[1] = (double)newPacketInt[iBALow];
					calibratedData[iBAHigh]=calibrateU12AdcValue(tempData[0],60,3,551);
					calibratedData[iBALow]=calibrateU12AdcValue(tempData[1],1950,3,183.7);
					objectCluster.mPropertyCluster.put("Bridge Amplifier High",new FormatCluster("CAL","mVolts",calibratedData[iBAHigh]));
					objectCluster.mPropertyCluster.put("Bridge Amplifier Low",new FormatCluster("CAL","mVolts",calibratedData[iBALow]));	
				}
			}

			if ((mEnabledSensors & SENSOR_GSR) > 0) {
				int iGSR = getSignalIndex("GSR Raw");
				tempData[0] = (double)newPacketInt[iGSR];
				int newGSRRange = -1; // initialized to -1 so it will only come into play if mGSRRange = 4  

				double p1=0,p2=0;//,p3=0,p4=0,p5=0;
				if (mGSRRange==4){
					newGSRRange=(49152 & (int)tempData[0])>>14; 
				}
				if (mGSRRange==0 || newGSRRange==0) { //Note that from FW 1.0 onwards the MSB of the GSR data contains the range
					// the polynomial function used for calibration has been deprecated, it is replaced with a linear function
					/* p1 = 6.5995E-9;
		                    p2 = -6.895E-5;
		                    p3 = 2.699E-1;
		                    p4 = -4.769835E+2;
		                    p5 = 3.403513341E+5;*/
					if (mShimmerVersion!=HW_ID_SHIMMER_3){
						p1 = 0.0373;
						p2 = -24.9915;
					} else {
						p1 = 0.0363;
						p2 = -24.8617;
					}
				} else if (mGSRRange==1 || newGSRRange==1) {
					/*p1 = 1.3569627E-8;
		                    p2 = -1.650399E-4;
		                    p3 = 7.54199E-1;
		                    p4 = -1.5726287856E+3;
		                    p5 = 1.367507927E+6;*/
					if (mShimmerVersion!=HW_ID_SHIMMER_3){
						p1 = 0.0054;
						p2 = -3.5194;
					} else {
						p1 = 0.0051;
						p2 = -3.8357;
					}
				} else if (mGSRRange==2 || newGSRRange==2) {
					/*p1 = 2.550036498E-8;
		                    p2 = -3.3136E-4;
		                    p3 = 1.6509426597E+0;
		                    p4 = -3.833348044E+3;
		                    p5 = 3.8063176947E+6;*/
					if (mShimmerVersion!=HW_ID_SHIMMER_3){
						p1 = 0.0015;
						p2 = -1.0163;
					} else {
						p1 = 0.0015;
						p2 = -1.0067;
					}
				} else if (mGSRRange==3  || newGSRRange==3) {
					/*p1 = 3.7153627E-7;
		                    p2 = -4.239437E-3;
		                    p3 = 1.7905709E+1;
		                    p4 = -3.37238657E+4;
		                    p5 = 2.53680446279E+7;*/
					if (mShimmerVersion!=HW_ID_SHIMMER_3){
						p1 = 4.5580e-04;
						p2 = -0.3014;
					} else {
						p1 = 4.4513e-04;
						p2 = -0.3193;
					}
				}
				objectCluster.mPropertyCluster.put("GSR",new FormatCluster("RAW",NO_UNIT,(double)newPacketInt[iGSR]));
				if (mEnableCalibration){
					calibratedData[iGSR] = calibrateGsrData(tempData[0],p1,p2);
					objectCluster.mPropertyCluster.put("GSR",new FormatCluster("CAL","kOhms",calibratedData[iGSR]));
				}
			}
		} else { //start of Shimmer2

			if ((mEnabledSensors & SENSOR_ACCEL) > 0){
				int iAccelX=getSignalIndex("Accelerometer X"); //find index
				int iAccelY=getSignalIndex("Accelerometer Y"); //find index
				int iAccelZ=getSignalIndex("Accelerometer Z"); //find index
				objectCluster.mPropertyCluster.put("Accelerometer X",new FormatCluster("RAW",NO_UNIT,(double)newPacketInt[iAccelX]));
				objectCluster.mPropertyCluster.put("Accelerometer Y",new FormatCluster("RAW",NO_UNIT,(double)newPacketInt[iAccelY]));
				objectCluster.mPropertyCluster.put("Accelerometer Z",new FormatCluster("RAW",NO_UNIT,(double)newPacketInt[iAccelZ]));
				if (mEnableCalibration){
					tempData[0]=(double)newPacketInt[iAccelX];
					tempData[1]=(double)newPacketInt[iAccelY];
					tempData[2]=(double)newPacketInt[iAccelZ];
					double[] accelCalibratedData=calibrateInertialSensorData(tempData, mAlignmentMatrixAnalogAccel, mSensitivityMatrixAnalogAccel, mOffsetVectorAnalogAccel);
					calibratedData[iAccelX]=accelCalibratedData[0];
					calibratedData[iAccelY]=accelCalibratedData[1];
					calibratedData[iAccelZ]=accelCalibratedData[2];
					if (mDefaultCalibrationParametersAccel == true) {
						objectCluster.mPropertyCluster.put("Accelerometer X",new FormatCluster("CAL","m/(sec^2)*",accelCalibratedData[0]));
						objectCluster.mPropertyCluster.put("Accelerometer Y",new FormatCluster("CAL","m/(sec^2)*",accelCalibratedData[1]));
						objectCluster.mPropertyCluster.put("Accelerometer Z",new FormatCluster("CAL","m/(sec^2)*",accelCalibratedData[2]));
						accelerometer.x=accelCalibratedData[0];
						accelerometer.y=accelCalibratedData[1];
						accelerometer.z=accelCalibratedData[2];
					} else {
						objectCluster.mPropertyCluster.put("Accelerometer X",new FormatCluster("CAL","m/(sec^2)",accelCalibratedData[0]));
						objectCluster.mPropertyCluster.put("Accelerometer Y",new FormatCluster("CAL","m/(sec^2)",accelCalibratedData[1]));
						objectCluster.mPropertyCluster.put("Accelerometer Z",new FormatCluster("CAL","m/(sec^2)",accelCalibratedData[2]));
						accelerometer.x=accelCalibratedData[0];
						accelerometer.y=accelCalibratedData[1];
						accelerometer.z=accelCalibratedData[2];
					}
				}

			}

			if ((mEnabledSensors & SENSOR_GYRO) > 0) {
				int iGyroX=getSignalIndex("Gyroscope X");
				int iGyroY=getSignalIndex("Gyroscope Y");
				int iGyroZ=getSignalIndex("Gyroscope Z");					
				objectCluster.mPropertyCluster.put("Gyroscope X",new FormatCluster("RAW",NO_UNIT,(double)newPacketInt[iGyroX]));
				objectCluster.mPropertyCluster.put("Gyroscope Y",new FormatCluster("RAW",NO_UNIT,(double)newPacketInt[iGyroY]));
				objectCluster.mPropertyCluster.put("Gyroscope Z",new FormatCluster("RAW",NO_UNIT,(double)newPacketInt[iGyroZ]));
				if (mEnableCalibration){
					tempData[0]=(double)newPacketInt[iGyroX];
					tempData[1]=(double)newPacketInt[iGyroY];
					tempData[2]=(double)newPacketInt[iGyroZ];
					double[] gyroCalibratedData=calibrateInertialSensorData(tempData, mAlignmentMatrixGyroscope, mSensitivityMatrixGyroscope, mOffsetVectorGyroscope);
					calibratedData[iGyroX]=gyroCalibratedData[0];
					calibratedData[iGyroY]=gyroCalibratedData[1];
					calibratedData[iGyroZ]=gyroCalibratedData[2];
					if (mDefaultCalibrationParametersGyro == true) {
						objectCluster.mPropertyCluster.put("Gyroscope X",new FormatCluster("CAL","deg/sec*",gyroCalibratedData[0]));
						objectCluster.mPropertyCluster.put("Gyroscope Y",new FormatCluster("CAL","deg/sec*",gyroCalibratedData[1]));
						objectCluster.mPropertyCluster.put("Gyroscope Z",new FormatCluster("CAL","deg/sec*",gyroCalibratedData[2]));
						gyroscope.x=gyroCalibratedData[0]*Math.PI/180;
						gyroscope.y=gyroCalibratedData[1]*Math.PI/180;
						gyroscope.z=gyroCalibratedData[2]*Math.PI/180;
					} else {
						objectCluster.mPropertyCluster.put("Gyroscope X",new FormatCluster("CAL","deg/sec",gyroCalibratedData[0]));
						objectCluster.mPropertyCluster.put("Gyroscope Y",new FormatCluster("CAL","deg/sec",gyroCalibratedData[1]));
						objectCluster.mPropertyCluster.put("Gyroscope Z",new FormatCluster("CAL","deg/sec",gyroCalibratedData[2]));
						gyroscope.x=gyroCalibratedData[0]*Math.PI/180;
						gyroscope.y=gyroCalibratedData[1]*Math.PI/180;
						gyroscope.z=gyroCalibratedData[2]*Math.PI/180;
						if (mEnableOntheFlyGyroOVCal){
							mGyroX.addValue(gyroCalibratedData[0]);
							mGyroY.addValue(gyroCalibratedData[1]);
							mGyroZ.addValue(gyroCalibratedData[2]);
							mGyroXRaw.addValue((double)newPacketInt[iGyroX]);
							mGyroYRaw.addValue((double)newPacketInt[iGyroY]);
							mGyroZRaw.addValue((double)newPacketInt[iGyroZ]);
							if (mGyroX.getStandardDeviation()<mGyroOVCalThreshold && mGyroY.getStandardDeviation()<mGyroOVCalThreshold && mGyroZ.getStandardDeviation()<mGyroOVCalThreshold){
								mOffsetVectorGyroscope[0][0]=mGyroXRaw.getMean();
								mOffsetVectorGyroscope[1][0]=mGyroYRaw.getMean();
								mOffsetVectorGyroscope[2][0]=mGyroZRaw.getMean();
							}
						}
					} 
				}
			}
			if ((mEnabledSensors & SENSOR_MAG) > 0) {
				int iMagX=getSignalIndex("Magnetometer X");
				int iMagY=getSignalIndex("Magnetometer Y");
				int iMagZ=getSignalIndex("Magnetometer Z");
				objectCluster.mPropertyCluster.put("Magnetometer X",new FormatCluster("RAW",NO_UNIT,(double)newPacketInt[iMagX]));
				objectCluster.mPropertyCluster.put("Magnetometer Y",new FormatCluster("RAW",NO_UNIT,(double)newPacketInt[iMagY]));
				objectCluster.mPropertyCluster.put("Magnetometer Z",new FormatCluster("RAW",NO_UNIT,(double)newPacketInt[iMagZ]));
				if (mEnableCalibration){
					tempData[0]=(double)newPacketInt[iMagX];
					tempData[1]=(double)newPacketInt[iMagY];
					tempData[2]=(double)newPacketInt[iMagZ];
					double[] magCalibratedData=calibrateInertialSensorData(tempData, mAlignmentMatrixMagnetometer, mSensitivityMatrixMagnetometer, mOffsetVectorMagnetometer);
					calibratedData[iMagX]=magCalibratedData[0];
					calibratedData[iMagY]=magCalibratedData[1];
					calibratedData[iMagZ]=magCalibratedData[2];
					if (mDefaultCalibrationParametersMag == true) {
						objectCluster.mPropertyCluster.put("Magnetometer X",new FormatCluster("CAL","local*",magCalibratedData[0]));
						objectCluster.mPropertyCluster.put("Magnetometer Y",new FormatCluster("CAL","local*",magCalibratedData[1]));
						objectCluster.mPropertyCluster.put("Magnetometer Z",new FormatCluster("CAL","local*",magCalibratedData[2]));
						magnetometer.x=magCalibratedData[0];
						magnetometer.y=magCalibratedData[1];
						magnetometer.z=magCalibratedData[2];
					} else {
						objectCluster.mPropertyCluster.put("Magnetometer X",new FormatCluster("CAL","local",magCalibratedData[0]));
						objectCluster.mPropertyCluster.put("Magnetometer Y",new FormatCluster("CAL","local",magCalibratedData[1]));
						objectCluster.mPropertyCluster.put("Magnetometer Z",new FormatCluster("CAL","local",magCalibratedData[2]));
						magnetometer.x=magCalibratedData[0];
						magnetometer.y=magCalibratedData[1];
						magnetometer.z=magCalibratedData[2];
					}
				}
			}


			if ((mEnabledSensors & SENSOR_ACCEL) > 0 && (mEnabledSensors & SENSOR_GYRO) > 0 && (mEnabledSensors & SENSOR_MAG) > 0 && mOrientationEnabled ){
				if (mEnableCalibration){
					if (mOrientationAlgo==null){
						mOrientationAlgo = new GradDes3DOrientation(0.4, (double)1/mSamplingRate, 1, 0, 0,0);
					}
					Quaternion q = mOrientationAlgo.update(accelerometer.x,accelerometer.y,accelerometer.z, gyroscope.x,gyroscope.y,gyroscope.z, magnetometer.x,magnetometer.y,magnetometer.z);
					double theta, Rx, Ry, Rz, rho;
					rho = Math.acos(q.q1);
					theta = rho * 2;
					Rx = q.q2 / Math.sin(rho);
					Ry = q.q3 / Math.sin(rho);
					Rz = q.q4 / Math.sin(rho);
					objectCluster.mPropertyCluster.put("Axis Angle A",new FormatCluster("CAL","local",theta));
					objectCluster.mPropertyCluster.put("Axis Angle X",new FormatCluster("CAL","local",Rx));
					objectCluster.mPropertyCluster.put("Axis Angle Y",new FormatCluster("CAL","local",Ry));
					objectCluster.mPropertyCluster.put("Axis Angle Z",new FormatCluster("CAL","local",Rz));
					objectCluster.mPropertyCluster.put("Quaternion 0",new FormatCluster("CAL","local",q.q1));
					objectCluster.mPropertyCluster.put("Quaternion 1",new FormatCluster("CAL","local",q.q2));
					objectCluster.mPropertyCluster.put("Quaternion 2",new FormatCluster("CAL","local",q.q3));
					objectCluster.mPropertyCluster.put("Quaternion 3",new FormatCluster("CAL","local",q.q4));
				}
			}

			if ((mEnabledSensors & SENSOR_GSR) > 0) {
				int iGSR = getSignalIndex("GSR Raw");
				tempData[0] = (double)newPacketInt[iGSR];
				int newGSRRange = -1; // initialized to -1 so it will only come into play if mGSRRange = 4  

				double p1=0,p2=0;//,p3=0,p4=0,p5=0;
				if (mGSRRange==4){
					newGSRRange=(49152 & (int)tempData[0])>>14; 
				}
				if (mGSRRange==0 || newGSRRange==0) { //Note that from FW 1.0 onwards the MSB of the GSR data contains the range
					// the polynomial function used for calibration has been deprecated, it is replaced with a linear function
					/* p1 = 6.5995E-9;
		                    p2 = -6.895E-5;
		                    p3 = 2.699E-1;
		                    p4 = -4.769835E+2;
		                    p5 = 3.403513341E+5;*/
					p1 = 0.0373;
					p2 = -24.9915;
				} else if (mGSRRange==1 || newGSRRange==1) {
					/*p1 = 1.3569627E-8;
		                    p2 = -1.650399E-4;
		                    p3 = 7.54199E-1;
		                    p4 = -1.5726287856E+3;
		                    p5 = 1.367507927E+6;*/
					p1 = 0.0054;
					p2 = -3.5194;
				} else if (mGSRRange==2 || newGSRRange==2) {
					/*p1 = 2.550036498E-8;
		                    p2 = -3.3136E-4;
		                    p3 = 1.6509426597E+0;
		                    p4 = -3.833348044E+3;
		                    p5 = 3.8063176947E+6;*/
					p1 = 0.0015;
					p2 = -1.0163;
				} else if (mGSRRange==3  || newGSRRange==3) {
					/*p1 = 3.7153627E-7;
		                    p2 = -4.239437E-3;
		                    p3 = 1.7905709E+1;
		                    p4 = -3.37238657E+4;
		                    p5 = 2.53680446279E+7;*/
					p1 = 4.5580e-04;
					p2 = -0.3014;
				}
				objectCluster.mPropertyCluster.put("GSR",new FormatCluster("RAW",NO_UNIT,(double)newPacketInt[iGSR]));
				if (mEnableCalibration){
					tempData[0] = (double)newPacketInt[iGSR];
					calibratedData[iGSR] = calibrateGsrData(tempData[0],p1,p2);
					objectCluster.mPropertyCluster.put("GSR",new FormatCluster("CAL","kOhms",calibratedData[iGSR]));
				}
			}
			if ((mEnabledSensors & SENSOR_ECG) > 0) {
				int iECGRALL = getSignalIndex("ECG RA LL");
				int iECGLALL = getSignalIndex("ECG LA LL");
				objectCluster.mPropertyCluster.put("ECG RA-LL",new FormatCluster("RAW",NO_UNIT,(double)newPacketInt[iECGRALL]));
				objectCluster.mPropertyCluster.put("ECG LA-LL",new FormatCluster("RAW",NO_UNIT,(double)newPacketInt[iECGLALL]));
				if (mEnableCalibration){
					tempData[0] = (double)newPacketInt[iECGRALL];
					tempData[1] = (double)newPacketInt[iECGLALL];
					calibratedData[iECGRALL]=calibrateU12AdcValue(tempData[0],OffsetECGRALL,3,GainECGRALL);
					calibratedData[iECGLALL]=calibrateU12AdcValue(tempData[1],OffsetECGLALL,3,GainECGLALL);
					if (mDefaultCalibrationParametersECG == true) {
						objectCluster.mPropertyCluster.put("ECG RA-LL",new FormatCluster("CAL","mVolts*",calibratedData[iECGRALL]));
						objectCluster.mPropertyCluster.put("ECG LA-LL",new FormatCluster("CAL","mVolts*",calibratedData[iECGLALL]));
					} else {
						objectCluster.mPropertyCluster.put("ECG RA-LL",new FormatCluster("CAL","mVolts",calibratedData[iECGRALL]));
						objectCluster.mPropertyCluster.put("ECG LA-LL",new FormatCluster("CAL","mVolts",calibratedData[iECGLALL]));
					}
				}
			}
			if ((mEnabledSensors & SENSOR_EMG) > 0) {
				int iEMG = getSignalIndex("EMG");
				objectCluster.mPropertyCluster.put("EMG",new FormatCluster("RAW",NO_UNIT,(double)newPacketInt[iEMG]));
				if (mEnableCalibration){
					tempData[0] = (double)newPacketInt[iEMG];
					calibratedData[iEMG]=calibrateU12AdcValue(tempData[0],OffsetEMG,3,GainEMG);
					if (mDefaultCalibrationParametersEMG == true){
						objectCluster.mPropertyCluster.put("EMG",new FormatCluster("CAL","mVolts*",calibratedData[iEMG]));
					} else {
						objectCluster.mPropertyCluster.put("EMG",new FormatCluster("CAL","mVolts",calibratedData[iEMG]));
					}
				}
			}
			if ((mEnabledSensors & SENSOR_BRIDGE_AMP) > 0) {
				int iBAHigh = getSignalIndex("Bridge Amplifier High");
				int iBALow = getSignalIndex("Bridge Amplifier Low");
				objectCluster.mPropertyCluster.put("Bridge Amplifier High",new FormatCluster("RAW",NO_UNIT,(double)newPacketInt[iBAHigh]));
				objectCluster.mPropertyCluster.put("Bridge Amplifier Low",new FormatCluster("RAW",NO_UNIT,(double)newPacketInt[iBALow]));
				if (mEnableCalibration){
					tempData[0] = (double)newPacketInt[iBAHigh];
					tempData[1] = (double)newPacketInt[iBALow];
					calibratedData[iBAHigh]=calibrateU12AdcValue(tempData[0],60,3,551);
					calibratedData[iBALow]=calibrateU12AdcValue(tempData[1],1950,3,183.7);
					objectCluster.mPropertyCluster.put("Bridge Amplifier High",new FormatCluster("CAL","mVolts",calibratedData[iBAHigh]));
					objectCluster.mPropertyCluster.put("Bridge Amplifier Low",new FormatCluster("CAL","mVolts",calibratedData[iBALow]));	
				}
			}
			if ((mEnabledSensors & SENSOR_HEART) > 0) {
				int iHeartRate = getSignalIndex("Heart Rate");
				tempData[0] = (double)newPacketInt[iHeartRate];
				objectCluster.mPropertyCluster.put("Heart Rate",new FormatCluster("RAW",NO_UNIT,tempData[0]));
				if (mEnableCalibration){
					calibratedData[iHeartRate]=tempData[0];
					if (mFirmwareVersionMajor==0 && mFirmwareVersionMinor==1){

					} else {
						if (tempData[0]==0){
							calibratedData[iHeartRate]=	mLastKnownHeartRate;
						} else {
							calibratedData[iHeartRate]=(int)(1024/tempData[0]*60);
							mLastKnownHeartRate=calibratedData[iHeartRate];
						}
					}
					objectCluster.mPropertyCluster.put("Heart Rate",new FormatCluster("CAL","BPM",calibratedData[iHeartRate]));	
				}
			}
			if ((mEnabledSensors& SENSOR_EXP_BOARD_A0) > 0) {
				int iA0 = getSignalIndex("Exp Board A0");
				tempData[0] = (double)newPacketInt[iA0];
				if (getPMux()==0){
					objectCluster.mPropertyCluster.put("ExpBoard A0",new FormatCluster("RAW",NO_UNIT,(double)newPacketInt[iA0]));
					if (mEnableCalibration){
						calibratedData[iA0]=calibrateU12AdcValue(tempData[0],0,3,1);
						objectCluster.mPropertyCluster.put("ExpBoard A0",new FormatCluster("CAL","mVolts",calibratedData[iA0]));
					}
				} else {
					objectCluster.mPropertyCluster.put("VSenseReg",new FormatCluster("RAW",NO_UNIT,(double)newPacketInt[iA0]));
					if (mEnableCalibration){
						calibratedData[iA0]=calibrateU12AdcValue(tempData[0],0,3,1)*1.988;
						objectCluster.mPropertyCluster.put("VSenseReg",new FormatCluster("CAL","mVolts",calibratedData[iA0]));
					}

				}
			}					
			if ((mEnabledSensors & SENSOR_EXP_BOARD_A7) > 0) {
				int iA7 = getSignalIndex("Exp Board A7");
				tempData[0] = (double)newPacketInt[iA7];
				if (getPMux()==0){
					objectCluster.mPropertyCluster.put("ExpBoard A7",new FormatCluster("RAW",NO_UNIT,(double)newPacketInt[iA7]));
					if (mEnableCalibration){
						calibratedData[iA7]=calibrateU12AdcValue(tempData[0],0,3,1);
						objectCluster.mPropertyCluster.put("ExpBoard A7",new FormatCluster("CAL","mVolts",calibratedData[iA7]));
					}
				} 
			}
			if  ((mEnabledSensors & SENSOR_BATT) > 0) {
				int iA0 = getSignalIndex("Exp Board A0");
				objectCluster.mPropertyCluster.put("VSenseReg",new FormatCluster("RAW",NO_UNIT,(double)newPacketInt[iA0]));

				int iA7 = getSignalIndex("Exp Board A7");
				objectCluster.mPropertyCluster.put("VSenseBatt",new FormatCluster("RAW",NO_UNIT,(double)newPacketInt[iA7]));


				if (mEnableCalibration){
					tempData[0] = (double)newPacketInt[iA0];
					calibratedData[iA0]=calibrateU12AdcValue(tempData[0],0,3,1)*1.988;
					objectCluster.mPropertyCluster.put("VSenseReg",new FormatCluster("CAL","mVolts",calibratedData[iA0]));

					tempData[0] = (double)newPacketInt[iA7];
					calibratedData[iA7]=calibrateU12AdcValue(tempData[0],0,3,1)*2;
					objectCluster.mPropertyCluster.put("VSenseBatt",new FormatCluster("CAL","mVolts",calibratedData[iA7]));	
					mVSenseBattMA.addValue(calibratedData[iA7]);
					checkBattery();
				}
			}
		}
		
		
		
		return objectCluster;
	}




	//protected abstract void writeLEDCommand(int i);

	/**
	 * Converts the raw packet byte values, into the corresponding calibrated and uncalibrated sensor values, the Instruction String determines the output 
	 * @param newPacket a byte array containing the current received packet
	 * @param Instructions an array string containing the commands to execute. It is currently not fully supported
	 * @return
	 */

	protected long[] parsedData(byte[] data,String[] dataType)
	{
		int iData=0;
		long[] formattedData=new long[dataType.length];

		for (int i=0;i<dataType.length;i++)
			if (dataType[i]=="u8") {
				formattedData[i]=(int)data[iData];
				iData=iData+1;
			} else if (dataType[i]=="i8") {
				formattedData[i]=calculatetwoscomplement((int)((int)0xFF & data[iData]),8);
				iData=iData+1;
			} else if (dataType[i]=="u12") {

				formattedData[i]=(int)((int)(data[iData] & 0xFF) + ((int)(data[iData+1] & 0xFF) << 8));
				iData=iData+2;
			} else if (dataType[i]=="i12>") {
				formattedData[i]=calculatetwoscomplement((int)((int)(data[iData] & 0xFF) + ((int)(data[iData+1] & 0xFF) << 8)),16);
				formattedData[i]=formattedData[i]>>4; // shift right by 4 bits
				iData=iData+2;
			} else if (dataType[i]=="u16") {				
				formattedData[i]=(int)((int)(data[iData] & 0xFF) + ((int)(data[iData+1] & 0xFF) << 8));
				iData=iData+2;
			} else if (dataType[i]=="u16r") {				
				formattedData[i]=(int)((int)(data[iData+1] & 0xFF) + ((int)(data[iData+0] & 0xFF) << 8));
				iData=iData+2;
			} else if (dataType[i]=="i16") {
				formattedData[i]=calculatetwoscomplement((int)((int)(data[iData] & 0xFF) + ((int)(data[iData+1] & 0xFF) << 8)),16);
				//formattedData[i]=ByteBuffer.wrap(arrayb).order(ByteOrder.LITTLE_ENDIAN).getShort();
				iData=iData+2;
			} else if (dataType[i]=="i16r"){
				formattedData[i]=calculatetwoscomplement((int)((int)(data[iData+1] & 0xFF) + ((int)(data[iData] & 0xFF) << 8)),16);
				//formattedData[i]=ByteBuffer.wrap(arrayb).order(ByteOrder.LITTLE_ENDIAN).getShort();
				iData=iData+2;
			} else if (dataType[i]=="u24r") {
				long xmsb =((long)(data[iData+0] & 0xFF) << 16);
				long msb =((long)(data[iData+1] & 0xFF) << 8);
				long lsb =((long)(data[iData+2] & 0xFF));
				formattedData[i]=xmsb + msb + lsb;
				iData=iData+3;
			} else if (dataType[i]=="i24r") {
				long xmsb =((long)(data[iData+0] & 0xFF) << 16);
				long msb =((long)(data[iData+1] & 0xFF) << 8);
				long lsb =((long)(data[iData+2] & 0xFF));
				formattedData[i]=calculatetwoscomplement((int)(xmsb + msb + lsb),24);
				iData=iData+3;
			} else if (dataType[i]=="u32signed") {
				long offset = (((long)data[iData] & 0xFF));
				if (offset == 255){
					offset = 0;
				}
				long xxmsb =(((long)data[iData+4] & 0xFF) << 24);
				long xmsb =(((long)data[iData+3] & 0xFF) << 16);
				long msb =(((long)data[iData+2] & 0xFF) << 8);
				long lsb =(((long)data[iData+1] & 0xFF));
				formattedData[i]=(1-2*offset)*(xxmsb + xmsb + msb + lsb);
				iData=iData+5;
			} else if (dataType[i]=="i32r") {
				long xxmsb =((long)(data[iData+0] & 0xFF) << 24);
				long xmsb =((long)(data[iData+1] & 0xFF) << 16);
				long msb =((long)(data[iData+2] & 0xFF) << 8);
				long lsb =((long)(data[iData+3] & 0xFF) << 0);
				formattedData[i]=calculatetwoscomplement((long)(xxmsb + xmsb + msb + lsb),32);
				iData=iData+4;
			} else if (dataType[i]=="u72"){
				// do something to parse the 9 byte data
				long offset = (((long)data[iData] & 0xFF));
				if (offset == 255){
					offset = 0;
				}
				
				long eigthmsb =(((long)data[iData+8] & 0x0FL) << 56);
				long seventhmsb =(((long)data[iData+7] & 0xFFL) << 48);
				long sixthmsb =(((long)data[iData+6] & 0xFFL) << 40);
				long fifthmsb =(((long)data[iData+5] & 0xFFL) << 32);
				long forthmsb =(((long)data[iData+4] & 0xFFL) << 24);
				long thirdmsb =(((long)data[iData+3] & 0xFFL) << 16);
				long msb =(((long)data[iData+2] & 0xFF) << 8);
				long lsb =(((long)data[iData+1] & 0xFF));
				formattedData[i]=(1-2*offset)*(eigthmsb + seventhmsb + sixthmsb + fifthmsb+ forthmsb+ thirdmsb + msb + lsb);
				iData=iData+9;
			}
		return formattedData;
	}
	/*
	 * Data Methods
	 * */  


	protected int[] formatdatapacketreverse(byte[] data,String[] dataType)
	{
		int iData=0;
		int[] formattedData=new int[dataType.length];

		for (int i=0;i<dataType.length;i++)
			if (dataType[i]=="u8") {
				formattedData[i]=(int)data[iData];
				iData=iData+1;
			}
			else if (dataType[i]=="i8") {
				formattedData[i]=calculatetwoscomplement((int)((int)0xFF & data[iData]),8);
				iData=iData+1;
			}
			else if (dataType[i]=="u12") {

				formattedData[i]=(int)((int)(data[iData+1] & 0xFF) + ((int)(data[iData] & 0xFF) << 8));
				iData=iData+2;
			}
			else if (dataType[i]=="u16") {

				formattedData[i]=(int)((int)(data[iData+1] & 0xFF) + ((int)(data[iData] & 0xFF) << 8));
				iData=iData+2;
			}
			else if (dataType[i]=="i16") {

				formattedData[i]=calculatetwoscomplement((int)((int)(data[iData+1] & 0xFF) + ((int)(data[iData] & 0xFF) << 8)),16);
				iData=iData+2;
			}
		return formattedData;
	}

	private int calculatetwoscomplement(int signedData, int bitLength)
	{
		int newData=signedData;
		if (signedData>=(1<<(bitLength-1))) {
			newData=-((signedData^(int)(Math.pow(2, bitLength)-1))+1);
		}

		return newData;
	}

	private long calculatetwoscomplement(long signedData, int bitLength)
	{
		long newData=signedData;
		if (signedData>=(1L<<(bitLength-1))) {
			newData=-((signedData^(long)(Math.pow(2, bitLength)-1))+1);
		}

		return newData;
	}
	
	protected int getSignalIndex(String signalName) {
		int iSignal=0; //used to be -1, putting to zero ensure it works eventhough it might be wrong SR30
		for (int i=0;i<mSignalNameArray.length;i++) {
			if (signalName==mSignalNameArray[i]) {
				iSignal=i;
			}
		}

		return iSignal;
	}

	
	/** Only for Bluetooth
	 * @param nC
	 * @param signalid
	 */
	protected void interpretdatapacketformat(int nC, byte[] signalid)
	{
		String [] signalNameArray=new String[MAX_NUMBER_OF_SIGNALS];
		String [] signalDataTypeArray=new String[MAX_NUMBER_OF_SIGNALS];
		signalNameArray[0]="TimeStamp";
		signalDataTypeArray[0]="u16";
		int packetSize=2; // Time stamp
		int enabledSensors= 0x00;
		for (int i=0;i<nC;i++) {
			if ((byte)signalid[i]==(byte)0x00)
			{
				if (mShimmerVersion==HW_ID_SHIMMER_SR30 || mShimmerVersion==HW_ID_SHIMMER_3){
					signalNameArray[i+1]="Low Noise Accelerometer X";
					signalDataTypeArray[i+1] = "i16";
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|Configuration.Shimmer3.SensorBitmap.SENSOR_A_ACCEL_S3);
				} else {
					signalNameArray[i+1]="Accelerometer X";
					signalDataTypeArray[i+1] = "u12";
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|SENSOR_ACCEL);
				}
			}
			else if ((byte)signalid[i]==(byte)0x01)
			{
				if (mShimmerVersion==HW_ID_SHIMMER_SR30 || mShimmerVersion==HW_ID_SHIMMER_3){
					signalNameArray[i+1]="Low Noise Accelerometer Y";
					signalDataTypeArray[i+1] = "i16";
					packetSize=packetSize+2; 
					enabledSensors= (enabledSensors|Configuration.Shimmer3.SensorBitmap.SENSOR_A_ACCEL_S3);
				} else {
					signalNameArray[i+1]="Accelerometer Y";
					signalDataTypeArray[i+1] = "u12";
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|SENSOR_ACCEL);
				}
			}
			else if ((byte)signalid[i]==(byte)0x02)
			{
				if (mShimmerVersion==HW_ID_SHIMMER_SR30 || mShimmerVersion==HW_ID_SHIMMER_3){
					signalNameArray[i+1]="Low Noise Accelerometer Z";
					signalDataTypeArray[i+1] = "i16";
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|Configuration.Shimmer3.SensorBitmap.SENSOR_A_ACCEL_S3);
				} else {
					signalNameArray[i+1]="Accelerometer Z";
					signalDataTypeArray[i+1] = "u12";
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|SENSOR_ACCEL);
				}
			}
			else if ((byte)signalid[i]==(byte)0x03)
			{

				if (mShimmerVersion==HW_ID_SHIMMER_SR30){
					signalNameArray[i+1]="Gyroscope X";
					signalDataTypeArray[i+1] = "i16";
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|Configuration.Shimmer3.SensorBitmap.SENSOR_GYRO_S3);
				} else if (mShimmerVersion==HW_ID_SHIMMER_3){
					signalNameArray[i+1]="VSenseBatt"; //should be the battery but this will do for now
					signalDataTypeArray[i+1] = "i16";
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|Configuration.Shimmer3.SensorBitmap.SENSOR_VBATT_S3);	
				} else {
					signalNameArray[i+1]="Gyroscope X";
					signalDataTypeArray[i+1] = "u12";
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|SENSOR_GYRO);
				}
			}
			else if ((byte)signalid[i]==(byte)0x04)
			{

				if (mShimmerVersion==HW_ID_SHIMMER_SR30){
					signalNameArray[i+1]="Gyroscope Y";
					signalDataTypeArray[i+1] = "i16";
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|Configuration.Shimmer3.SensorBitmap.SENSOR_GYRO_S3);
				} else if (mShimmerVersion==HW_ID_SHIMMER_3){
					signalDataTypeArray[i+1] = "i16";
					packetSize=packetSize+2;
					signalNameArray[i+1]="Wide Range Accelerometer X";
					enabledSensors= (enabledSensors|Configuration.Shimmer3.SensorBitmap.SENSOR_D_ACCEL_S3);
				} else {
					signalNameArray[i+1]="Gyroscope Y";
					signalDataTypeArray[i+1] = "u12";
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|SENSOR_GYRO);
				}
			}
			else if ((byte)signalid[i]==(byte)0x05)
			{
				if (mShimmerVersion==HW_ID_SHIMMER_SR30){
					signalNameArray[i+1]="Gyroscope Z";
					signalDataTypeArray[i+1] = "i16";
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|Configuration.Shimmer3.SensorBitmap.SENSOR_GYRO_S3);
				} else if (mShimmerVersion==HW_ID_SHIMMER_3){
					signalDataTypeArray[i+1] = "i16";
					packetSize=packetSize+2;
					signalNameArray[i+1]="Wide Range Accelerometer Y";
					enabledSensors= (enabledSensors|Configuration.Shimmer3.SensorBitmap.SENSOR_D_ACCEL_S3);
				} else {
					signalNameArray[i+1]="Gyroscope Z";
					signalDataTypeArray[i+1] = "u12";
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|SENSOR_GYRO);
				}
			}
			else if ((byte)signalid[i]==(byte)0x06)
			{
				if(mShimmerVersion==HW_ID_SHIMMER_SR30){
					signalNameArray[i+1]="VSenseBatt"; //should be the battery but this will do for now
					signalDataTypeArray[i+1] = "i16";
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|Configuration.Shimmer3.SensorBitmap.SENSOR_VBATT_S3);	
				} else if (mShimmerVersion==HW_ID_SHIMMER_3){
					signalDataTypeArray[i+1] = "i16";
					packetSize=packetSize+2;
					signalNameArray[i+1]="Wide Range Accelerometer Z";
					enabledSensors= (enabledSensors|Configuration.Shimmer3.SensorBitmap.SENSOR_D_ACCEL_S3);
				} else {
					signalNameArray[i+1]="Magnetometer X";
					signalDataTypeArray[i+1] = "i16";
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|SENSOR_MAG);
				}


			}
			else if ((byte)signalid[i]==(byte)0x07)
			{
				if(mShimmerVersion==HW_ID_SHIMMER_SR30){
					signalDataTypeArray[i+1] = "i16";
					packetSize=packetSize+2;
					signalNameArray[i+1]="Wide Range Accelerometer X";
					enabledSensors= (enabledSensors|Configuration.Shimmer3.SensorBitmap.SENSOR_D_ACCEL_S3);
				} else if(mShimmerVersion==HW_ID_SHIMMER_3){
					signalNameArray[i+1]="Magnetometer X";
					signalDataTypeArray[i+1] = "i16r";			
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|Configuration.Shimmer3.SensorBitmap.SENSOR_MAG_S3);
				} else {
					signalDataTypeArray[i+1] = "i16";
					packetSize=packetSize+2;
					signalNameArray[i+1]="Magnetometer Y";
					enabledSensors= (enabledSensors|SENSOR_MAG);
				}


			}
			else if ((byte)signalid[i]==(byte)0x08)
			{	
				if(mShimmerVersion==HW_ID_SHIMMER_SR30){
					signalDataTypeArray[i+1] = "i16";
					packetSize=packetSize+2;
					signalNameArray[i+1]="Wide Range Accelerometer Y";
					enabledSensors= (enabledSensors|Configuration.Shimmer3.SensorBitmap.SENSOR_D_ACCEL_S3);
				} else if(mShimmerVersion==HW_ID_SHIMMER_3){
					signalNameArray[i+1]="Magnetometer Y";
					signalDataTypeArray[i+1] = "i16r";			
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|Configuration.Shimmer3.SensorBitmap.SENSOR_MAG_S3);
				} else {
					signalDataTypeArray[i+1] = "i16";
					packetSize=packetSize+2;
					signalNameArray[i+1]="Magnetometer Z";
					enabledSensors= (enabledSensors|SENSOR_MAG);
				}

			}
			else if ((byte)signalid[i]==(byte)0x09)
			{
				if(mShimmerVersion==HW_ID_SHIMMER_SR30){
					signalNameArray[i+1]="Wide Range Accelerometer Z";
					signalDataTypeArray[i+1] = "i16";
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|Configuration.Shimmer3.SensorBitmap.SENSOR_D_ACCEL_S3);
				} else if(mShimmerVersion==HW_ID_SHIMMER_3){
					signalNameArray[i+1]="Magnetometer Z";
					signalDataTypeArray[i+1] = "i16r";			
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|Configuration.Shimmer3.SensorBitmap.SENSOR_MAG_S3);
				} else {
					signalNameArray[i+1]="ECG RA LL";
					signalDataTypeArray[i+1] = "u12";
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|SENSOR_ECG);
				}


			}
			else if ((byte)signalid[i]==(byte)0x0A)
			{
				if(mShimmerVersion==HW_ID_SHIMMER_SR30){
					signalNameArray[i+1]="Magnetometer X";
					signalDataTypeArray[i+1] = "i16";			
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|Configuration.Shimmer3.SensorBitmap.SENSOR_MAG_S3);
				} else if (mShimmerVersion==HW_ID_SHIMMER_3) {
					signalNameArray[i+1]="Gyroscope X";
					signalDataTypeArray[i+1] = "i16r";
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|Configuration.Shimmer3.SensorBitmap.SENSOR_GYRO_S3);
				} else {

					signalNameArray[i+1]="ECG LA LL";
					signalDataTypeArray[i+1] = "u12";
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|SENSOR_ECG);
				}
			}
			else if ((byte)signalid[i]==(byte)0x0B)
			{
				if(mShimmerVersion==HW_ID_SHIMMER_SR30){
					signalNameArray[i+1]="Magnetometer Y";
					signalDataTypeArray[i+1] = "i16";			
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|Configuration.Shimmer3.SensorBitmap.SENSOR_MAG_S3);
				}  else if (mShimmerVersion==HW_ID_SHIMMER_3) {
					signalNameArray[i+1]="Gyroscope Y";
					signalDataTypeArray[i+1] = "i16r";
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|Configuration.Shimmer3.SensorBitmap.SENSOR_GYRO_S3);
				} else {
					signalNameArray[i+1]="GSR Raw";
					signalDataTypeArray[i+1] = "u16";
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|SENSOR_GSR);
				}
			}
			else if ((byte)signalid[i]==(byte)0x0C)
			{
				if(mShimmerVersion==HW_ID_SHIMMER_SR30){
					signalNameArray[i+1]="Magnetometer Z";
					signalDataTypeArray[i+1] = "i16";			
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|Configuration.Shimmer3.SensorBitmap.SENSOR_MAG_S3);
				} else if (mShimmerVersion==HW_ID_SHIMMER_3) {
					signalNameArray[i+1]="Gyroscope Z";
					signalDataTypeArray[i+1] = "i16r";
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|Configuration.Shimmer3.SensorBitmap.SENSOR_GYRO_S3);
				} else {
					signalNameArray[i+1]="GSR Res";
					signalDataTypeArray[i+1] = "u16";
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|SENSOR_GSR);
				}
			}
			else if ((byte)signalid[i]==(byte)0x0D)
			{
				if (mShimmerVersion==HW_ID_SHIMMER_3){
					signalNameArray[i+1]="External ADC A7";
					signalDataTypeArray[i+1] = "u12";
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|SENSOR_EXT_ADC_A7);
				} else{
					signalNameArray[i+1]="EMG";
					signalDataTypeArray[i+1] = "u12";
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|SENSOR_EMG);
				}
			}
			else if ((byte)signalid[i]==(byte)0x0E)
			{
				if (mShimmerVersion==HW_ID_SHIMMER_3){
					signalNameArray[i+1]="External ADC A6";
					signalDataTypeArray[i+1] = "u12";
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|SENSOR_EXT_ADC_A6);
				} else{
					signalNameArray[i+1]="Exp Board A0";
					signalDataTypeArray[i+1] = "u12";
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|SENSOR_EXP_BOARD_A0);
				}
			}
			else if ((byte)signalid[i]==(byte)0x0F)
			{
				if (mShimmerVersion==HW_ID_SHIMMER_3){
					signalNameArray[i+1]="External ADC A15";
					signalDataTypeArray[i+1] = "u12";
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|SENSOR_EXT_ADC_A15);
				} else{
					signalNameArray[i+1]="Exp Board A7";
					signalDataTypeArray[i+1] = "u12";
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|SENSOR_EXP_BOARD_A7);
				}
			}
			else if ((byte)signalid[i]==(byte)0x10)
			{
				if (mShimmerVersion==HW_ID_SHIMMER_3){
					signalNameArray[i+1]="Internal ADC A1";
					signalDataTypeArray[i+1] = "u12";
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|SENSOR_INT_ADC_A1);
				} else {
					signalNameArray[i+1]="Bridge Amplifier High";
					signalDataTypeArray[i+1] = "u12";
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|SENSOR_BRIDGE_AMP);
				}
			}

			else if ((byte)signalid[i]==(byte)0x11)
			{
				if (mShimmerVersion==HW_ID_SHIMMER_3){
					signalNameArray[i+1]="Internal ADC A12";
					signalDataTypeArray[i+1] = "u12";
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|SENSOR_INT_ADC_A12);
				} else {
					signalNameArray[i+1]="Bridge Amplifier Low";
					signalDataTypeArray[i+1] = "u12";
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|SENSOR_BRIDGE_AMP);
				}
			}
			else if ((byte)signalid[i]==(byte)0x12)
			{
				if (mShimmerVersion==HW_ID_SHIMMER_3){
					signalNameArray[i+1]="Internal ADC A13";
					signalDataTypeArray[i+1] = "u12";
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|SENSOR_INT_ADC_A13);
				} else {
					signalNameArray[i+1]="Heart Rate";
					if (mFirmwareVersionMajor==0 && mFirmwareVersionMinor==1){
						signalDataTypeArray[i+1] = "u8";
						packetSize=packetSize+1;
					} else {
						signalDataTypeArray[i+1] = "u16"; 
						packetSize=packetSize+2;
					}
					enabledSensors= (enabledSensors|SENSOR_HEART);
				}
			}
			else if ((byte)signalid[i]==(byte)0x13)
			{
				if (mShimmerVersion==HW_ID_SHIMMER_3){
					signalNameArray[i+1]="Internal ADC A14";
					signalDataTypeArray[i+1] = "u12";
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|SENSOR_INT_ADC_A14);
				}
			}
			else if ((byte)signalid[i]==(byte)0x1A){
				if (mShimmerVersion==HW_ID_SHIMMER_3){
					signalNameArray[i+1]="Temperature";
					signalDataTypeArray[i+1] = "u16r";
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|SENSOR_BMP180);
				}
			}
			else if ((byte)signalid[i]==(byte)0x1B){
				if (mShimmerVersion==HW_ID_SHIMMER_3){
					signalNameArray[i+1]="Pressure";
					signalDataTypeArray[i+1] = "u24r";
					packetSize=packetSize+3;
					enabledSensors= (enabledSensors|SENSOR_BMP180);
				}
			}
			else if ((byte)signalid[i]==(byte)0x1C){
				if (mShimmerVersion==HW_ID_SHIMMER_3){
					signalNameArray[i+1]="GSR Raw";
					signalDataTypeArray[i+1] = "u16";
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|SENSOR_GSR);
				}
			}
			else if ((byte)signalid[i]==(byte)0x1D){
				if (mShimmerVersion==HW_ID_SHIMMER_3){
					signalNameArray[i+1]="EXG1 STATUS";
					signalDataTypeArray[i+1] = "u8";
					packetSize=packetSize+1;

				}
			}
			else if ((byte)signalid[i]==(byte)0x1E){
				if (mShimmerVersion==HW_ID_SHIMMER_3){
					signalNameArray[i+1]="EXG1 24Bit CH1";
					signalDataTypeArray[i+1] = "i24r";
					packetSize=packetSize+3;
					enabledSensors= (enabledSensors|SENSOR_EXG1_24BIT);
				}
			}
			else if ((byte)signalid[i]==(byte)0x1F){
				if (mShimmerVersion==HW_ID_SHIMMER_3){
					signalNameArray[i+1]="EXG1 24Bit CH2";
					signalDataTypeArray[i+1] = "i24r";
					packetSize=packetSize+3;
					enabledSensors= (enabledSensors|SENSOR_EXG1_24BIT);
				}
			}

			else if ((byte)signalid[i]==(byte)0x20){
				if (mShimmerVersion==HW_ID_SHIMMER_3){
					signalNameArray[i+1]="EXG2 STATUS";
					signalDataTypeArray[i+1] = "u8";
					packetSize=packetSize+1;

				}
			}
			else if ((byte)signalid[i]==(byte)0x21){
				if (mShimmerVersion==HW_ID_SHIMMER_3){
					signalNameArray[i+1]="EXG2 24Bit CH1";
					signalDataTypeArray[i+1] = "i24r";
					packetSize=packetSize+3;
					enabledSensors= (enabledSensors|SENSOR_EXG2_24BIT);
				}
			}
			else if ((byte)signalid[i]==(byte)0x22){
				if (mShimmerVersion==HW_ID_SHIMMER_3){
					signalNameArray[i+1]="EXG2 24Bit CH2";
					signalDataTypeArray[i+1] = "i24r";
					packetSize=packetSize+3;
					enabledSensors= (enabledSensors|SENSOR_EXG2_24BIT);
				}
			}

			else if ((byte)signalid[i]==(byte)0x23){
				if (mShimmerVersion==HW_ID_SHIMMER_3){
					signalNameArray[i+1]="EXG1 16Bit CH1";
					signalDataTypeArray[i+1] = "i16r";
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|SENSOR_EXG1_16BIT);
				}
			}
			else if ((byte)signalid[i]==(byte)0x24){
				if (mShimmerVersion==HW_ID_SHIMMER_3){
					signalNameArray[i+1]="EXG1 16Bit CH2";
					signalDataTypeArray[i+1] = "i16r";
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|SENSOR_EXG1_16BIT);
				}
			}

			else if ((byte)signalid[i]==(byte)0x25){
				if (mShimmerVersion==HW_ID_SHIMMER_3){
					signalNameArray[i+1]="EXG2 16Bit CH1";
					signalDataTypeArray[i+1] = "i16r";
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|SENSOR_EXG2_16BIT);
				}
			}
			else if ((byte)signalid[i]==(byte)0x26){
				if (mShimmerVersion==HW_ID_SHIMMER_3){
					signalNameArray[i+1]="EXG2 16Bit CH2";
					signalDataTypeArray[i+1] = "i16r";
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|SENSOR_EXG2_16BIT);
				}
			}
			else if ((byte)signalid[i]==(byte)0x27)
			{
				if (mShimmerVersion==HW_ID_SHIMMER_3){
					signalNameArray[i+1]="Bridge Amplifier High";
					signalDataTypeArray[i+1] = "u12";
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|SENSOR_BRIDGE_AMP);
				} 
			}
			else if ((byte)signalid[i]==(byte)0x28)
			{
				if (mShimmerVersion==HW_ID_SHIMMER_3){
					signalNameArray[i+1]="Bridge Amplifier Low";
					signalDataTypeArray[i+1] = "u12";
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|SENSOR_BRIDGE_AMP);
				} 
			}
			else
			{
				signalNameArray[i+1]=Byte.toString(signalid[i]);
				signalDataTypeArray[i+1] = "u12";
				packetSize=packetSize+2;
			}

		}
		mSignalNameArray=signalNameArray;
		mSignalDataTypeArray=signalDataTypeArray;
		mPacketSize=packetSize;
	}

	protected void retrievepressurecalibrationparametersfrompacket(byte[] pressureResoRes, int packetType)
	{
		if (packetType == BMP180_CALIBRATION_COEFFICIENTS_RESPONSE){
			AC1 = calculatetwoscomplement((int)((int)(pressureResoRes[1] & 0xFF) + ((int)(pressureResoRes[0] & 0xFF) << 8)),16);
			AC2 = calculatetwoscomplement((int)((int)(pressureResoRes[3] & 0xFF) + ((int)(pressureResoRes[2] & 0xFF) << 8)),16);
			AC3 = calculatetwoscomplement((int)((int)(pressureResoRes[5] & 0xFF) + ((int)(pressureResoRes[4] & 0xFF) << 8)),16);
			AC4 = (int)((int)(pressureResoRes[7] & 0xFF) + ((int)(pressureResoRes[6] & 0xFF) << 8));
			AC5 = (int)((int)(pressureResoRes[9] & 0xFF) + ((int)(pressureResoRes[8] & 0xFF) << 8));
			AC6 = (int)((int)(pressureResoRes[11] & 0xFF) + ((int)(pressureResoRes[10] & 0xFF) << 8));
			B1 = calculatetwoscomplement((int)((int)(pressureResoRes[13] & 0xFF) + ((int)(pressureResoRes[12] & 0xFF) << 8)),16);
			B2 = calculatetwoscomplement((int)((int)(pressureResoRes[15] & 0xFF) + ((int)(pressureResoRes[14] & 0xFF) << 8)),16);
			MB = calculatetwoscomplement((int)((int)(pressureResoRes[17] & 0xFF) + ((int)(pressureResoRes[16] & 0xFF) << 8)),16);
			MC = calculatetwoscomplement((int)((int)(pressureResoRes[19] & 0xFF) + ((int)(pressureResoRes[18] & 0xFF) << 8)),16);
			MD = calculatetwoscomplement((int)((int)(pressureResoRes[21] & 0xFF) + ((int)(pressureResoRes[20] & 0xFF) << 8)),16);
		}



	}
	
	/**
	 * Should only be used when Shimmer is Connected and Initialized
	 */
	public static BiMap<String,String> generateBiMapSensorIDtoSensorName(int shimmerVersion){
		BiMap<String, String> sensorBitmaptoName =null;  
		if (shimmerVersion != HW_ID_SHIMMER_2R){
			final Map<String, String> tempSensorBMtoName = new HashMap<String, String>();  
			tempSensorBMtoName.put(Integer.toString(SENSOR_GYRO), "Gyroscope");  
			tempSensorBMtoName.put(Integer.toString(SENSOR_MAG), "Magnetometer");  
			tempSensorBMtoName.put(Integer.toString(SENSOR_GSR), "GSR");  
			tempSensorBMtoName.put(Integer.toString(SENSOR_EXP_BOARD_A7), "Exp Board A7");
			tempSensorBMtoName.put(Integer.toString(SENSOR_EXP_BOARD_A0), "Exp Board A0");
			tempSensorBMtoName.put(Integer.toString(SENSOR_EXP_BOARD), "Exp Board");
			tempSensorBMtoName.put(Integer.toString(SENSOR_BRIDGE_AMP), "Bridge Amplifier");
			tempSensorBMtoName.put(Integer.toString(SENSOR_HEART), "Heart Rate");  
			tempSensorBMtoName.put(Integer.toString(SENSOR_BATT), "Battery Voltage");
			tempSensorBMtoName.put(Integer.toString(SENSOR_EXT_ADC_A7), "External ADC A7");  
			tempSensorBMtoName.put(Integer.toString(SENSOR_EXT_ADC_A6), "External ADC A6");  
			tempSensorBMtoName.put(Integer.toString(SENSOR_EXT_ADC_A15), "External ADC A15");
			tempSensorBMtoName.put(Integer.toString(SENSOR_INT_ADC_A1), "Internal ADC A1");
			tempSensorBMtoName.put(Integer.toString(SENSOR_INT_ADC_A12), "Internal ADC A12");
			tempSensorBMtoName.put(Integer.toString(SENSOR_INT_ADC_A13), "Internal ADC A13");
			tempSensorBMtoName.put(Integer.toString(SENSOR_INT_ADC_A14), "Internal ADC A14");
			tempSensorBMtoName.put(Integer.toString(SENSOR_BMP180), "Pressure");
			tempSensorBMtoName.put(Integer.toString(SENSOR_ACCEL), "Low Noise Accelerometer");  
			tempSensorBMtoName.put(Integer.toString(SENSOR_DACCEL), "Wide Range Accelerometer");
			tempSensorBMtoName.put(Integer.toString(SENSOR_EXG1_24BIT), "EXG1");  
			tempSensorBMtoName.put(Integer.toString(SENSOR_EXG2_24BIT), "EXG2");
			tempSensorBMtoName.put(Integer.toString(SENSOR_EXG1_16BIT), "EXG1 16Bit");  
			tempSensorBMtoName.put(Integer.toString(SENSOR_EXG2_16BIT), "EXG2 16Bit");
			sensorBitmaptoName = ImmutableBiMap.copyOf(Collections.unmodifiableMap(tempSensorBMtoName));
		} else {
			final Map<String, String> tempSensorBMtoName = new HashMap<String, String>();  
			tempSensorBMtoName.put(Integer.toString(SENSOR_ACCEL), "Accelerometer");
			tempSensorBMtoName.put(Integer.toString(SENSOR_GYRO), "Gyroscope");  
			tempSensorBMtoName.put(Integer.toString(SENSOR_MAG), "Magnetometer");  
			tempSensorBMtoName.put(Integer.toString(SENSOR_EMG), "EMG");  
			tempSensorBMtoName.put(Integer.toString(SENSOR_ECG), "ECG");  
			tempSensorBMtoName.put(Integer.toString(SENSOR_GSR), "GSR");  
			tempSensorBMtoName.put(Integer.toString(SENSOR_EXP_BOARD_A7), "Exp Board A7");
			tempSensorBMtoName.put(Integer.toString(SENSOR_EXP_BOARD_A0), "Exp Board A0");
			tempSensorBMtoName.put(Integer.toString(SENSOR_EXP_BOARD), "Exp Board");
			tempSensorBMtoName.put(Integer.toString(SENSOR_BRIDGE_AMP), "Bridge Amplifier");
			tempSensorBMtoName.put(Integer.toString(SENSOR_HEART), "Heart Rate");  
			tempSensorBMtoName.put(Integer.toString(SENSOR_BATT), "Battery Voltage");
			tempSensorBMtoName.put(Integer.toString(SENSOR_EXT_ADC_A7), "External ADC A7");  
			tempSensorBMtoName.put(Integer.toString(SENSOR_EXT_ADC_A6), "External ADC A6");  
			tempSensorBMtoName.put(Integer.toString(SENSOR_EXT_ADC_A15), "External ADC A15");
			tempSensorBMtoName.put(Integer.toString(SENSOR_INT_ADC_A1), "Internal ADC A1");
			tempSensorBMtoName.put(Integer.toString(SENSOR_INT_ADC_A12), "Internal ADC A12");
			tempSensorBMtoName.put(Integer.toString(SENSOR_INT_ADC_A13), "Internal ADC A13");
			tempSensorBMtoName.put(Integer.toString(SENSOR_INT_ADC_A14), "Internal ADC A14");
			sensorBitmaptoName = ImmutableBiMap.copyOf(Collections.unmodifiableMap(tempSensorBMtoName));
		}
		return sensorBitmaptoName;
	}

	/**
	 * Should only be used when Shimmer is Connected and Initialized
	 */
	public void generateBiMapSensorIDtoSensorName(){
		if (mShimmerVersion != -1){
			if (mShimmerVersion != HW_ID_SHIMMER_2R){
				final Map<String, String> tempSensorBMtoName = new HashMap<String, String>();  
				tempSensorBMtoName.put(Integer.toString(SENSOR_BMP180), "Pressure");
				tempSensorBMtoName.put(Integer.toString(SENSOR_GYRO), "Gyroscope");  
				tempSensorBMtoName.put(Integer.toString(SENSOR_MAG), "Magnetometer");  
				tempSensorBMtoName.put(Integer.toString(SHIMMER3_SENSOR_ECG), "ECG");  
				tempSensorBMtoName.put(Integer.toString(SHIMMER3_SENSOR_EMG), "EMG");  
				tempSensorBMtoName.put(Integer.toString(SENSOR_GSR), "GSR");  
				tempSensorBMtoName.put(Integer.toString(SENSOR_EXP_BOARD_A7), "Exp Board A7");
				tempSensorBMtoName.put(Integer.toString(SENSOR_EXP_BOARD_A0), "Exp Board A0");
				tempSensorBMtoName.put(Integer.toString(SENSOR_EXP_BOARD), "Exp Board");
				tempSensorBMtoName.put(Integer.toString(SENSOR_BRIDGE_AMP), "Bridge Amplifier");
				tempSensorBMtoName.put(Integer.toString(SENSOR_HEART), "Heart Rate");  
				tempSensorBMtoName.put(Integer.toString(SENSOR_BATT), "Battery Voltage");
				tempSensorBMtoName.put(Integer.toString(SENSOR_EXT_ADC_A7), "External ADC A7");  
				tempSensorBMtoName.put(Integer.toString(SENSOR_EXT_ADC_A6), "External ADC A6");  
				tempSensorBMtoName.put(Integer.toString(SENSOR_EXT_ADC_A15), "External ADC A15");
				tempSensorBMtoName.put(Integer.toString(SENSOR_INT_ADC_A1), "Internal ADC A1");
				tempSensorBMtoName.put(Integer.toString(SENSOR_INT_ADC_A12), "Internal ADC A12");
				tempSensorBMtoName.put(Integer.toString(SENSOR_INT_ADC_A13), "Internal ADC A13");
				tempSensorBMtoName.put(Integer.toString(SENSOR_INT_ADC_A14), "Internal ADC A14");
				tempSensorBMtoName.put(Integer.toString(SENSOR_ACCEL), "Low Noise Accelerometer");
				tempSensorBMtoName.put(Integer.toString(SENSOR_DACCEL), "Wide Range Accelerometer");
				mSensorBitmaptoName = ImmutableBiMap.copyOf(Collections.unmodifiableMap(tempSensorBMtoName));


			} else {
				final Map<String, String> tempSensorBMtoName = new HashMap<String, String>();  
				tempSensorBMtoName.put(Integer.toString(SENSOR_ACCEL), "Accelerometer");
				tempSensorBMtoName.put(Integer.toString(SENSOR_GYRO), "Gyroscope");  
				tempSensorBMtoName.put(Integer.toString(SENSOR_MAG), "Magnetometer");
				tempSensorBMtoName.put(Integer.toString(SENSOR_ECG), "ECG");  
				tempSensorBMtoName.put(Integer.toString(SENSOR_EMG), "EMG");  
				tempSensorBMtoName.put(Integer.toString(SENSOR_GSR), "GSR");  
				tempSensorBMtoName.put(Integer.toString(SENSOR_EXP_BOARD_A7), "Exp Board A7");
				tempSensorBMtoName.put(Integer.toString(SENSOR_EXP_BOARD_A0), "Exp Board A0");
				tempSensorBMtoName.put(Integer.toString(SENSOR_EXP_BOARD), "Exp Board");
				tempSensorBMtoName.put(Integer.toString(SENSOR_BRIDGE_AMP), "Bridge Amplifier");
				tempSensorBMtoName.put(Integer.toString(SENSOR_HEART), "Heart Rate");  
				tempSensorBMtoName.put(Integer.toString(SENSOR_BATT), "Battery Voltage");
				tempSensorBMtoName.put(Integer.toString(SENSOR_EXT_ADC_A7), "External ADC A7");  
				tempSensorBMtoName.put(Integer.toString(SENSOR_EXT_ADC_A6), "External ADC A6");  
				tempSensorBMtoName.put(Integer.toString(SENSOR_EXT_ADC_A15), "External ADC A15");
				tempSensorBMtoName.put(Integer.toString(SENSOR_INT_ADC_A1), "Internal ADC A1");
				tempSensorBMtoName.put(Integer.toString(SENSOR_INT_ADC_A12), "Internal ADC A12");
				tempSensorBMtoName.put(Integer.toString(SENSOR_INT_ADC_A13), "Internal ADC A13");
				tempSensorBMtoName.put(Integer.toString(SENSOR_INT_ADC_A14), "Internal ADC A14");
				mSensorBitmaptoName = ImmutableBiMap.copyOf(Collections.unmodifiableMap(tempSensorBMtoName));
			}

		}
	}



	public String[] getListofEnabledSensorSignals(){
		List<String> listofSignals = new ArrayList<String>();
		String[] enabledSignals; 
		if (mShimmerVersion!=HW_ID_SHIMMER_3){
			listofSignals.add("Timestamp");
			if (((mEnabledSensors & 0xFF)& SENSOR_ACCEL) > 0){
				listofSignals.add("Accelerometer X");
				listofSignals.add("Accelerometer Y");
				listofSignals.add("Accelerometer Z");
			}
			if (((mEnabledSensors & 0xFF)& SENSOR_GYRO) > 0) {
				listofSignals.add("Gyroscope X");
				listofSignals.add("Gyroscope Y");
				listofSignals.add("Gyroscope Z");
			}
			if (((mEnabledSensors & 0xFF)& SENSOR_MAG) > 0) {
				listofSignals.add("Magnetometer X");
				listofSignals.add("Magnetometer Y");
				listofSignals.add("Magnetometer Z");
			}
			if (((mEnabledSensors & 0xFF) & SENSOR_GSR) > 0) {
				listofSignals.add("GSR");
			}
			if (((mEnabledSensors & 0xFF) & SENSOR_ECG) > 0) {
				listofSignals.add("ECG RA-LL");
				listofSignals.add("ECG LA-LL");
			}
			if (((mEnabledSensors & 0xFF) & SENSOR_EMG) > 0) {
				listofSignals.add("EMG");
			}
			if (((mEnabledSensors & 0xFF00) & SENSOR_BRIDGE_AMP) > 0) {
				listofSignals.add("Bridge Amplifier High");
				listofSignals.add("Bridge Amplifier Low");
			}
			if (((mEnabledSensors & 0xFF00) & SENSOR_HEART) > 0) {
				listofSignals.add("Heart Rate");
			}
			if ((((mEnabledSensors & 0xFF) & SENSOR_EXP_BOARD_A0) > 0) && getPMux() == 0) {
				listofSignals.add("ExpBoard A0");
			}
			if ((((mEnabledSensors & 0xFF) & SENSOR_EXP_BOARD_A7) > 0 && getPMux() == 0)) {
				listofSignals.add("ExpBoard A7");
			}
			if (((mEnabledSensors & 0xFFFF) & SENSOR_BATT) > 0) {
				listofSignals.add("VSenseBatt");
				listofSignals.add("VSenseReg");
			}
			if (((mEnabledSensors & 0xFF)& SENSOR_ACCEL) > 0 && ((mEnabledSensors & 0xFF)& SENSOR_GYRO) > 0 && ((mEnabledSensors & 0xFF)& SENSOR_MAG) > 0 && mOrientationEnabled){
				listofSignals.add("Axis Angle A");
				listofSignals.add("Axis Angle X");
				listofSignals.add("Axis Angle Y");
				listofSignals.add("Axis Angle Z");
			}
			if (((mEnabledSensors & 0xFF)& SENSOR_ACCEL) > 0 && ((mEnabledSensors & 0xFF)& SENSOR_GYRO) > 0 && ((mEnabledSensors & 0xFF)& SENSOR_MAG) > 0 && mOrientationEnabled){
				listofSignals.add("Quaternion 0");
				listofSignals.add("Quaternion 1");
				listofSignals.add("Quaternion 2");
				listofSignals.add("Quaternion 3");
			}

		} else {
			listofSignals.add("Timestamp");
			if ((mEnabledSensors & SENSOR_ACCEL) >0){
				listofSignals.add("Low Noise Accelerometer X");
				listofSignals.add("Low Noise Accelerometer Y");
				listofSignals.add("Low Noise Accelerometer Z");
			}
			if ((mEnabledSensors& SENSOR_DACCEL) >0){
				listofSignals.add("Wide Range Accelerometer X");
				listofSignals.add("Wide Range Accelerometer Y");
				listofSignals.add("Wide Range Accelerometer Z");
			}
			if (((mEnabledSensors & 0xFF)& SENSOR_GYRO) > 0) {
				listofSignals.add("Gyroscope X");
				listofSignals.add("Gyroscope Y");
				listofSignals.add("Gyroscope Z");
			}
			if (((mEnabledSensors & 0xFF)& SENSOR_MAG) > 0) {
				listofSignals.add("Magnetometer X");
				listofSignals.add("Magnetometer Y");
				listofSignals.add("Magnetometer Z");
			} 
			if (((mEnabledSensors & 0xFFFF) & SENSOR_BATT) > 0) {
				listofSignals.add("VSenseBatt");
			}
			if (((mEnabledSensors & 0xFFFFFF)& SENSOR_EXT_ADC_A15) > 0) {
				listofSignals.add("External ADC A15");
			}
			if (((mEnabledSensors & 0xFFFFFF)& SENSOR_EXT_ADC_A7) > 0) {
				listofSignals.add("External ADC A7");
			}
			if (((mEnabledSensors & 0xFFFFFF)& SENSOR_EXT_ADC_A6) > 0) {
				listofSignals.add("External ADC A6");
			}
			if (((mEnabledSensors & 0xFFFFFF)& SENSOR_INT_ADC_A1) > 0) {
				listofSignals.add("Internal ADC A1");
			}
			if (((mEnabledSensors & 0xFFFFFF)& SENSOR_INT_ADC_A12) > 0) {
				listofSignals.add("Internal ADC A12");
			}
			if (((mEnabledSensors & 0xFFFFFF)& SENSOR_INT_ADC_A13) > 0) {
				listofSignals.add("Internal ADC A13");
			}
			if (((mEnabledSensors & 0xFFFFFF)& SENSOR_INT_ADC_A14) > 0) {
				listofSignals.add("Internal ADC A14");
			}
			if ((((mEnabledSensors & 0xFF)& SENSOR_ACCEL) > 0 || ((mEnabledSensors & 0xFFFF)& SENSOR_DACCEL) > 0)&& ((mEnabledSensors & 0xFF)& SENSOR_GYRO) > 0 && ((mEnabledSensors & 0xFF)& SENSOR_MAG) > 0 && mOrientationEnabled){
				listofSignals.add("Axis Angle A");
				listofSignals.add("Axis Angle X");
				listofSignals.add("Axis Angle Y");
				listofSignals.add("Axis Angle Z");
			}
			if ((((mEnabledSensors & 0xFF)& SENSOR_ACCEL) > 0 || ((mEnabledSensors & 0xFFFF)& SENSOR_DACCEL) > 0) && ((mEnabledSensors & 0xFF)& SENSOR_GYRO) > 0 && ((mEnabledSensors & 0xFF)& SENSOR_MAG) > 0 && mOrientationEnabled){
				listofSignals.add("Quaternion 0");
				listofSignals.add("Quaternion 1");
				listofSignals.add("Quaternion 2");
				listofSignals.add("Quaternion 3");
			}
			if ((mEnabledSensors & SENSOR_BMP180)>0){
				listofSignals.add("Pressure");
				listofSignals.add("Temperature");
			}
			if ((mEnabledSensors & SENSOR_GSR)>0){
				listofSignals.add("GSR");
			}
			if (((mEnabledSensors & SENSOR_EXG1_24BIT)>0)|| ((mEnabledSensors & SENSOR_EXG1_16BIT)>0)){
				listofSignals.add("EXG1 STATUS");
			}
			if (((mEnabledSensors & SENSOR_EXG2_24BIT)>0)|| ((mEnabledSensors & SENSOR_EXG2_16BIT)>0)){
				listofSignals.add("EXG2 STATUS");
			}
			if ((mEnabledSensors & SENSOR_EXG1_24BIT)>0){
				if (isEXGUsingDefaultECGConfiguration()){
					listofSignals.add("ECG LL-RA");
					listofSignals.add("ECG LA-RA");
				}
				else if (isEXGUsingDefaultEMGConfiguration()){
					listofSignals.add("EMG CH1");
					listofSignals.add("EMG CH2");
				} else {
					listofSignals.add("EXG1 CH1");
					listofSignals.add("EXG1 CH2");
				}

			}
			if ((mEnabledSensors & SENSOR_EXG2_24BIT)>0){
				if (isEXGUsingDefaultECGConfiguration()){
					listofSignals.add("EXG2 CH1");
					listofSignals.add("ECG Vx-RL");
				}
				else if (isEXGUsingDefaultEMGConfiguration()){
					listofSignals.add("EXG2 CH1");
					listofSignals.add("EXG2 CH2");
				} else {
					listofSignals.add("EXG2 CH1");
					listofSignals.add("EXG2 CH2");
				}
			}
			if ((mEnabledSensors & SENSOR_EXG1_16BIT)>0){
				if (isEXGUsingDefaultECGConfiguration()){
					listofSignals.add("ECG LL-RA");
					listofSignals.add("ECG LA-RA");
				}
				else if (isEXGUsingDefaultEMGConfiguration()){
					listofSignals.add("EMG CH1");
					listofSignals.add("EMG CH2");
				} else {
					listofSignals.add("EXG1 CH1 16Bit");
					listofSignals.add("EXG1 CH2 16Bit");
				}
			}
			if ((mEnabledSensors & SENSOR_EXG2_16BIT)>0){
				if (isEXGUsingDefaultECGConfiguration()){
					listofSignals.add("EXG2 CH1");
					listofSignals.add("ECG Vx-RL");
				}
				else if (isEXGUsingDefaultEMGConfiguration()){
					listofSignals.add("EXG2 CH1 16Bit");
					listofSignals.add("EXG2 CH2 16Bit");
				} else {
					listofSignals.add("EXG2 CH1 16Bit");
					listofSignals.add("EXG2 CH2 16Bit");
				}
			}
			if (((mEnabledSensors & 0xFF00) & SENSOR_BRIDGE_AMP) > 0) {
				listofSignals.add("Bridge Amplifier High");
				listofSignals.add("Bridge Amplifier Low");
			}
		}
		enabledSignals = listofSignals.toArray(new String[listofSignals.size()]);
		return enabledSignals;
	}	

	protected void retrievebiophysicalcalibrationparametersfrompacket(byte[] bufferCalibrationParameters, int packetType)
	{
		if (packetType == ECG_CALIBRATION_RESPONSE){
			if (bufferCalibrationParameters[0]==-1 && bufferCalibrationParameters[1] == -1 && bufferCalibrationParameters[2] == -1 && bufferCalibrationParameters[3]==-1){
				mDefaultCalibrationParametersECG = true;
			} else {
				mDefaultCalibrationParametersECG = false;
				OffsetECGLALL=(double)((bufferCalibrationParameters[0]&0xFF)<<8)+(bufferCalibrationParameters[1]&0xFF);
				GainECGLALL=(double)((bufferCalibrationParameters[2]&0xFF)<<8)+(bufferCalibrationParameters[3]&0xFF);
				OffsetECGRALL=(double)((bufferCalibrationParameters[4]&0xFF)<<8)+(bufferCalibrationParameters[5]&0xFF);
				GainECGRALL=(double)((bufferCalibrationParameters[6]&0xFF)<<8)+(bufferCalibrationParameters[7]&0xFF);
			}	
		}

		if (packetType == EMG_CALIBRATION_RESPONSE){

			if (bufferCalibrationParameters[0]==-1 && bufferCalibrationParameters[1] == -1 && bufferCalibrationParameters[2] == -1 && bufferCalibrationParameters[3]==-1){
				mDefaultCalibrationParametersEMG = true;
			} else {
				mDefaultCalibrationParametersEMG = false;
				OffsetEMG=(double)((bufferCalibrationParameters[0]&0xFF)<<8)+(bufferCalibrationParameters[1]&0xFF);
				GainEMG=(double)((bufferCalibrationParameters[2]&0xFF)<<8)+(bufferCalibrationParameters[3]&0xFF);
			}
		}

	}

	protected void retrievekinematiccalibrationparametersfrompacket(byte[] bufferCalibrationParameters, int packetType)
	{
		if (packetType==ACCEL_CALIBRATION_RESPONSE || packetType==LSM303DLHC_ACCEL_CALIBRATION_RESPONSE || packetType==GYRO_CALIBRATION_RESPONSE || packetType==MAG_CALIBRATION_RESPONSE ){
			String[] dataType={"i16","i16","i16","i16","i16","i16","i8","i8","i8","i8","i8","i8","i8","i8","i8"}; 
			int[] formattedPacket=formatdatapacketreverse(bufferCalibrationParameters,dataType); // using the datatype the calibration parameters are converted
			double[] AM=new double[9];
			for (int i=0;i<9;i++)
			{
				AM[i]=((double)formattedPacket[6+i])/100;
			}

			double[][] AlignmentMatrix = {{AM[0],AM[1],AM[2]},{AM[3],AM[4],AM[5]},{AM[6],AM[7],AM[8]}}; 				
			double[][] SensitivityMatrix = {{formattedPacket[3],0,0},{0,formattedPacket[4],0},{0,0,formattedPacket[5]}}; 
			double[][] OffsetVector = {{formattedPacket[0]},{formattedPacket[1]},{formattedPacket[2]}};


			if (packetType==ACCEL_CALIBRATION_RESPONSE && SensitivityMatrix[0][0]!=-1) {   //used to be 65535 but changed to -1 as we are now using i16
				mDefaultCalibrationParametersAccel = false;
				mAlignmentMatrixAnalogAccel = AlignmentMatrix;
				mOffsetVectorAnalogAccel = OffsetVector;
				mSensitivityMatrixAnalogAccel = SensitivityMatrix;
			} else if(packetType==ACCEL_CALIBRATION_RESPONSE && SensitivityMatrix[0][0]==-1){
				mDefaultCalibrationParametersAccel = true;
				if (mShimmerVersion!=3){
					mAlignmentMatrixAnalogAccel = AlignmentMatrixAccelShimmer2;
					mOffsetVectorAnalogAccel = OffsetVectorAccelShimmer2;
					if (getAccelRange()==0){
						mSensitivityMatrixAnalogAccel = SensitivityMatrixAccel1p5gShimmer2; 
					} else if (getAccelRange()==1){
						mSensitivityMatrixAnalogAccel = SensitivityMatrixAccel2gShimmer2; 
					} else if (getAccelRange()==2){
						mSensitivityMatrixAnalogAccel = SensitivityMatrixAccel4gShimmer2; 
					} else if (getAccelRange()==3){
						mSensitivityMatrixAnalogAccel = SensitivityMatrixAccel6gShimmer2; 
					}
				} else {
					if (getAccelRange()==0){
						mSensitivityMatrixAnalogAccel = SensitivityMatrixLowNoiseAccel2gShimmer3;
						mAlignmentMatrixAnalogAccel = AlignmentMatrixLowNoiseAccelShimmer3;
						mOffsetVectorAnalogAccel = OffsetVectorLowNoiseAccelShimmer3;
					} else if (getAccelRange()==1){
						mSensitivityMatrixAnalogAccel = SensitivityMatrixWideRangeAccel4gShimmer3;
						mAlignmentMatrixAnalogAccel = AlignmentMatrixWideRangeAccelShimmer3;
						mOffsetVectorAnalogAccel = OffsetVectorWideRangeAccelShimmer3;
					} else if (getAccelRange()==2){
						mSensitivityMatrixAnalogAccel = SensitivityMatrixWideRangeAccel8gShimmer3;
						mAlignmentMatrixAnalogAccel = AlignmentMatrixWideRangeAccelShimmer3;
						mOffsetVectorAnalogAccel = OffsetVectorWideRangeAccelShimmer3;
					} else if (getAccelRange()==3){
						mSensitivityMatrixAnalogAccel = SensitivityMatrixWideRangeAccel16gShimmer3;
						mAlignmentMatrixAnalogAccel = AlignmentMatrixWideRangeAccelShimmer3;
						mOffsetVectorAnalogAccel = OffsetVectorWideRangeAccelShimmer3;
					}


				}
			}

			if (packetType==LSM303DLHC_ACCEL_CALIBRATION_RESPONSE && SensitivityMatrix[0][0]!=-1) {   //used to be 65535 but changed to -1 as we are now using i16
				mDefaultCalibrationParametersDigitalAccel = false;
				mAlignmentMatrixWRAccel = AlignmentMatrix;
				mOffsetVectorWRAccel = OffsetVector;
				mSensitivityMatrixWRAccel = SensitivityMatrix;
			} else if(packetType==LSM303DLHC_ACCEL_CALIBRATION_RESPONSE  && SensitivityMatrix[0][0]==-1){
				mDefaultCalibrationParametersDigitalAccel = true;
				if (getAccelRange()==0){
					mSensitivityMatrixWRAccel = SensitivityMatrixWideRangeAccel2gShimmer3;
					mAlignmentMatrixWRAccel = AlignmentMatrixWideRangeAccelShimmer3;
					mOffsetVectorWRAccel = OffsetVectorWideRangeAccelShimmer3;
				} else if (getAccelRange()==1){
					mSensitivityMatrixWRAccel = SensitivityMatrixWideRangeAccel4gShimmer3;
					mAlignmentMatrixWRAccel = AlignmentMatrixWideRangeAccelShimmer3;
					mOffsetVectorWRAccel = OffsetVectorWideRangeAccelShimmer3;
				} else if (getAccelRange()==2){
					mSensitivityMatrixWRAccel = SensitivityMatrixWideRangeAccel8gShimmer3;
					mAlignmentMatrixWRAccel = AlignmentMatrixWideRangeAccelShimmer3;
					mOffsetVectorWRAccel = OffsetVectorWideRangeAccelShimmer3;
				} else if (getAccelRange()==3){
					mSensitivityMatrixWRAccel = SensitivityMatrixWideRangeAccel16gShimmer3;
					mAlignmentMatrixWRAccel = AlignmentMatrixWideRangeAccelShimmer3;
					mOffsetVectorWRAccel = OffsetVectorWideRangeAccelShimmer3;
				}
			}
			if (packetType==GYRO_CALIBRATION_RESPONSE && SensitivityMatrix[0][0]!=-1) {
				mDefaultCalibrationParametersGyro = false;
				mAlignmentMatrixGyroscope = AlignmentMatrix;
				mOffsetVectorGyroscope = OffsetVector;
				mSensitivityMatrixGyroscope = SensitivityMatrix;
				mSensitivityMatrixGyroscope[0][0] = mSensitivityMatrixGyroscope[0][0]/100;
				mSensitivityMatrixGyroscope[1][1] = mSensitivityMatrixGyroscope[1][1]/100;
				mSensitivityMatrixGyroscope[2][2] = mSensitivityMatrixGyroscope[2][2]/100;

			} else if(packetType==GYRO_CALIBRATION_RESPONSE && SensitivityMatrix[0][0]==-1){
				mDefaultCalibrationParametersGyro = true;
				if (mShimmerVersion!=3){
					mAlignmentMatrixGyroscope = AlignmentMatrixGyroShimmer2;
					mOffsetVectorGyroscope = OffsetVectorGyroShimmer2;
					mSensitivityMatrixGyroscope = SensitivityMatrixGyroShimmer2;	
				} else {
					if (mGyroRange==0){
						mSensitivityMatrixGyroscope = SensitivityMatrixGyro250dpsShimmer3;

					} else if (mGyroRange==1){
						mSensitivityMatrixGyroscope = SensitivityMatrixGyro500dpsShimmer3;

					} else if (mGyroRange==2){
						mSensitivityMatrixGyroscope = SensitivityMatrixGyro1000dpsShimmer3;

					} else if (mGyroRange==3){
						mSensitivityMatrixGyroscope = SensitivityMatrixGyro2000dpsShimmer3;
					}
					mAlignmentMatrixGyroscope = AlignmentMatrixGyroShimmer3;
					mOffsetVectorGyroscope = OffsetVectorGyroShimmer3;
				}
			} 
			if (packetType==MAG_CALIBRATION_RESPONSE && SensitivityMatrix[0][0]!=-1) {
				mDefaultCalibrationParametersMag = false;
				mAlignmentMatrixMagnetometer = AlignmentMatrix;
				mOffsetVectorMagnetometer = OffsetVector;
				mSensitivityMatrixMagnetometer = SensitivityMatrix;

			} else if(packetType==MAG_CALIBRATION_RESPONSE && SensitivityMatrix[0][0]==-1){
				mDefaultCalibrationParametersMag = true;
				if (mShimmerVersion!=3){
					mAlignmentMatrixMagnetometer = AlignmentMatrixMagShimmer2;
					mOffsetVectorMagnetometer = OffsetVectorMagShimmer2;
					if (mMagRange==0){
						mSensitivityMatrixMagnetometer = SensitivityMatrixMag0p8GaShimmer2;
					} else if (mMagRange==1){
						mSensitivityMatrixMagnetometer = SensitivityMatrixMag1p3GaShimmer2;
					} else if (mMagRange==2){
						mSensitivityMatrixMagnetometer = SensitivityMatrixMag1p9GaShimmer2;
					} else if (mMagRange==3){
						mSensitivityMatrixMagnetometer = SensitivityMatrixMag2p5GaShimmer2;
					} else if (mMagRange==4){
						mSensitivityMatrixMagnetometer = SensitivityMatrixMag4p0GaShimmer2;
					} else if (mMagRange==5){
						mSensitivityMatrixMagnetometer = SensitivityMatrixMag4p7GaShimmer2;
					} else if (mMagRange==6){
						mSensitivityMatrixMagnetometer = SensitivityMatrixMag5p6GaShimmer2;
					} else if (mMagRange==7){
						mSensitivityMatrixMagnetometer = SensitivityMatrixMag8p1GaShimmer2;
					}
				} else {
					mAlignmentMatrixMagnetometer = AlignmentMatrixMagShimmer3;
					mOffsetVectorMagnetometer = OffsetVectorMagShimmer3;
					if (mMagRange==1){
						mSensitivityMatrixMagnetometer = SensitivityMatrixMag1p3GaShimmer3;
					} else if (mMagRange==2){
						mSensitivityMatrixMagnetometer = SensitivityMatrixMag1p9GaShimmer3;
					} else if (mMagRange==3){
						mSensitivityMatrixMagnetometer = SensitivityMatrixMag2p5GaShimmer3;
					} else if (mMagRange==4){
						mSensitivityMatrixMagnetometer = SensitivityMatrixMag4GaShimmer3;
					} else if (mMagRange==5){
						mSensitivityMatrixMagnetometer = SensitivityMatrixMag4p7GaShimmer3;
					} else if (mMagRange==6){
						mSensitivityMatrixMagnetometer = SensitivityMatrixMag5p6GaShimmer3;
					} else if (mMagRange==7){
						mSensitivityMatrixMagnetometer = SensitivityMatrixMag8p1GaShimmer3;
					}
				}
			}
		}
	}

	private double[][] matrixinverse3x3(double[][] data) {
		double a,b,c,d,e,f,g,h,i;
		a=data[0][0];
		b=data[0][1];
		c=data[0][2];
		d=data[1][0];
		e=data[1][1];
		f=data[1][2];
		g=data[2][0];
		h=data[2][1];
		i=data[2][2];
		//
		double deter=a*e*i+b*f*g+c*d*h-c*e*g-b*d*i-a*f*h;
		double[][] answer=new double[3][3];
		answer[0][0]=(1/deter)*(e*i-f*h);

		answer[0][1]=(1/deter)*(c*h-b*i);
		answer[0][2]=(1/deter)*(b*f-c*e);
		answer[1][0]=(1/deter)*(f*g-d*i);
		answer[1][1]=(1/deter)*(a*i-c*g);
		answer[1][2]=(1/deter)*(c*d-a*f);
		answer[2][0]=(1/deter)*(d*h-e*g);
		answer[2][1]=(1/deter)*(g*b-a*h);
		answer[2][2]=(1/deter)*(a*e-b*d);
		return answer;
	}
	private double[][] matrixminus(double[][] a ,double[][] b) {
		int aRows = a.length,
				aColumns = a[0].length,
				bRows = b.length,
				bColumns = b[0].length;
		if (( aColumns != bColumns )&&( aRows != bRows )) {
			throw new IllegalArgumentException(" Matrix did not match");
		}
		double[][] resultant = new double[aRows][bColumns];
		for(int i = 0; i < aRows; i++) { // aRow
			for(int k = 0; k < aColumns; k++) { // aColumn

				resultant[i][k]=a[i][k]-b[i][k];

			}
		}
		return resultant;
	}

	private double[][] matrixmultiplication(double[][] a,double[][] b) {

		int aRows = a.length,
				aColumns = a[0].length,
				bRows = b.length,
				bColumns = b[0].length;

		if ( aColumns != bRows ) {
			throw new IllegalArgumentException("A:Rows: " + aColumns + " did not match B:Columns " + bRows + ".");
		}

		double[][] resultant = new double[aRows][bColumns];

		for(int i = 0; i < aRows; i++) { // aRow
			for(int j = 0; j < bColumns; j++) { // bColumn
				for(int k = 0; k < aColumns; k++) { // aColumn
					resultant[i][j] += a[i][k] * b[k][j];
				}
			}
		}

		return resultant;
	}

	protected double calibrateTimeStamp(double timeStamp){
		//first convert to continuous time stamp
		double calibratedTimeStamp=0;
		if (mLastReceivedTimeStamp>(timeStamp+(65536*mCurrentTimeStampCycle))){ 
			mCurrentTimeStampCycle=mCurrentTimeStampCycle+1;
		}

		mLastReceivedTimeStamp=(timeStamp+(65536*mCurrentTimeStampCycle));
		calibratedTimeStamp=mLastReceivedTimeStamp/32768*1000;   // to convert into mS
		if (mFirstTimeCalTime){
			mFirstTimeCalTime=false;
			mCalTimeStart = calibratedTimeStamp;
		}
		if (mLastReceivedCalibratedTimeStamp!=-1){
			double timeDifference=calibratedTimeStamp-mLastReceivedCalibratedTimeStamp;
			if (timeDifference>(1/(mSamplingRate-1))*1000){
				mPacketLossCount=mPacketLossCount+1;
				Long mTotalNumberofPackets=(long) ((calibratedTimeStamp-mCalTimeStart)/(1/mSamplingRate*1000));

				mPacketReceptionRate = (double)((mTotalNumberofPackets-mPacketLossCount)/(double)mTotalNumberofPackets)*100;
				sendStatusMsgPacketLossDetected();
			}
		}	
		mLastReceivedCalibratedTimeStamp=calibratedTimeStamp;
		return calibratedTimeStamp;
	}

	//protected abstract void sendStatusMsgPacketLossDetected();
	protected void sendStatusMsgPacketLossDetected() {
	}

	protected double[] calibrateInertialSensorData(double[] data, double[][] AM, double[][] SM, double[][] OV) {
		/*  Based on the theory outlined by Ferraris F, Grimaldi U, and Parvis M.  
           in "Procedure for effortless in-field calibration of three-axis rate gyros and accelerometers" Sens. Mater. 1995; 7: 311-30.            
           C = [R^(-1)] .[K^(-1)] .([U]-[B])
			where.....
			[C] -> [3 x n] Calibrated Data Matrix 
			[U] -> [3 x n] Uncalibrated Data Matrix
			[B] ->  [3 x n] Replicated Sensor Offset Vector Matrix 
			[R^(-1)] -> [3 x 3] Inverse Alignment Matrix
			[K^(-1)] -> [3 x 3] Inverse Sensitivity Matrix
			n = Number of Samples
		 */
		double [][] data2d=new double [3][1];
		data2d[0][0]=data[0];
		data2d[1][0]=data[1];
		data2d[2][0]=data[2];
		data2d= matrixmultiplication(matrixmultiplication(matrixinverse3x3(AM),matrixinverse3x3(SM)),matrixminus(data2d,OV));
		double[] ansdata=new double[3];
		ansdata[0]=data2d[0][0];
		ansdata[1]=data2d[1][0];
		ansdata[2]=data2d[2][0];
		return ansdata;
	}

	protected double[] calibratePressureSensorData(double UP, double UT){
		double X1 = (UT - AC6) * AC5 / 32768;
		double X2 = (MC * 2048 / (X1 + MD));
		double B5 = X1 + X2;
		double T = (B5 + 8) / 16;

		double B6 = B5 - 4000;
		X1 = (B2 * (Math.pow(B6,2)/ 4096)) / 2048;
		X2 = AC2 * B6 / 2048;
		double X3 = X1 + X2;
		double B3 = (((AC1 * 4 + X3)*(1<<mPressureResolution) + 2)) / 4;
		X1 = AC3 * B6 / 8192;
		X2 = (B1 * (Math.pow(B6,2)/ 4096)) / 65536;
		X3 = ((X1 + X2) + 2) / 4;
		double B4 = AC4 * (X3 + 32768) / 32768;
		double B7 = (UP - B3) * (50000>>mPressureResolution);
		double p=0;
		if (B7 < 2147483648L ){ //0x80000000
			p = (B7 * 2) / B4;
		}
		else{
			p = (B7 / B4) * 2;
		}
		X1 = ((p / 256.0) * (p / 256.0) * 3038) / 65536;
		X2 = (-7357 * p) / 65536;
		p = p +( (X1 + X2 + 3791) / 16);

		double[] caldata = new double[2];
		caldata[0]=p;
		caldata[1]=T/10;
		return caldata;
	}


	protected double calibrateU12AdcValue(double uncalibratedData,double offset,double vRefP,double gain){
		double calibratedData=(uncalibratedData-offset)*(((vRefP*1000)/gain)/4095);
		return calibratedData;
	}

	protected double calibrateGsrData(double gsrUncalibratedData,double p1, double p2){
		gsrUncalibratedData = (double)((int)gsrUncalibratedData & 4095); 
		//the following polynomial is deprecated and has been replaced with a more accurate linear one, see GSR user guide for further details
		//double gsrCalibratedData = (p1*Math.pow(gsrUncalibratedData,4)+p2*Math.pow(gsrUncalibratedData,3)+p3*Math.pow(gsrUncalibratedData,2)+p4*gsrUncalibratedData+p5)/1000;
		//the following is the new linear method see user GSR user guide for further details
		double gsrCalibratedData = (1/((p1*gsrUncalibratedData)+p2)*1000); //kohms 
		return gsrCalibratedData;  
	}

	public double getSamplingRate(){
		return mSamplingRate;
	}

	public int getAccelRange(){
		return mAccelRange;
	}

	public int getPressureResolution(){
		return mPressureResolution;
	}

	public int getMagRange(){
		return mMagRange;
	}

	public int getGyroRange(){
		return mGyroRange;
	}

	public int getGSRRange(){
		return mGSRRange;
	}

	public int getInternalExpPower(){
		return mInternalExpPower;
	}

	public int getPMux(){
		if ((mConfigByte0 & (byte)64)!=0) {
			//then set ConfigByte0 at bit position 7
			return 1;
		} else{
			return 0;
		}
	}

	protected ObjectCluster callAdditionalServices(ObjectCluster objectCluster) {
		return objectCluster;
	}

	protected void interpretInqResponse(byte[] bufferInquiry){
		if (mShimmerVersion==HW_ID_SHIMMER_2 || mShimmerVersion==HW_ID_SHIMMER_2R){

			mPacketSize = 2+bufferInquiry[3]*2; 
			mSamplingRate = (double)1024/bufferInquiry[0];
			if (mLSM303MagRate==3 && mSamplingRate>10){
				mLowPowerMag = true;
			}
			mAccelRange = bufferInquiry[1];
			mConfigByte0 = bufferInquiry[2] & 0xFF; //convert the byte to unsigned integer
			mNChannels = bufferInquiry[3];
			mBufferSize = bufferInquiry[4];
			byte[] signalIdArray = new byte[mNChannels];
			System.arraycopy(bufferInquiry, 5, signalIdArray, 0, mNChannels);
			updateEnabledSensorsFromChannels(signalIdArray);
			interpretdatapacketformat(mNChannels,signalIdArray);
			mInquiryResponseBytes = new byte[5+mNChannels];
			System.arraycopy(bufferInquiry, 0, mInquiryResponseBytes , 0, mInquiryResponseBytes.length);
		} else if (mShimmerVersion==HW_ID_SHIMMER_3) {
			mPacketSize = 2+bufferInquiry[6]*2; 
			mSamplingRate = (32768/(double)((int)(bufferInquiry[0] & 0xFF) + ((int)(bufferInquiry[1] & 0xFF) << 8)));
			mNChannels = bufferInquiry[6];
			mBufferSize = bufferInquiry[7];
			mConfigByte0 = ((long)(bufferInquiry[2] & 0xFF) +((long)(bufferInquiry[3] & 0xFF) << 8)+((long)(bufferInquiry[4] & 0xFF) << 16) +((long)(bufferInquiry[5] & 0xFF) << 24));
			mAccelRange = ((int)(mConfigByte0 & 0xC))>>2;
			mGyroRange = ((int)(mConfigByte0 & 196608))>>16;
			mMagRange = ((int)(mConfigByte0 & 14680064))>>21;
			mLSM303DigitalAccelRate = ((int)(mConfigByte0 & 0xF0))>>4;
			mMPU9150GyroAccelRate = ((int)(mConfigByte0 & 65280))>>8;
			mLSM303MagRate = ((int)(mConfigByte0 & 1835008))>>18; 
			mPressureResolution = (((int)(mConfigByte0 >>28)) & 3);
			mGSRRange  = (((int)(mConfigByte0 >>25)) & 7);
			mInternalExpPower = (((int)(mConfigByte0 >>24)) & 1);
			mInquiryResponseBytes = new byte[8+mNChannels];
			System.arraycopy(bufferInquiry, 0, mInquiryResponseBytes , 0, mInquiryResponseBytes.length);
			if ((mLSM303DigitalAccelRate==2 && mSamplingRate>10)){
				mLowPowerAccelWR = true;
			}
			if ((mMPU9150GyroAccelRate==0xFF && mSamplingRate>10)){
				mLowPowerGyro = true;
			}
			if ((mLSM303MagRate==4 && mSamplingRate>10)){
				mLowPowerMag = true;
			}
			byte[] signalIdArray = new byte[mNChannels];
			System.arraycopy(bufferInquiry, 8, signalIdArray, 0, mNChannels);
			updateEnabledSensorsFromChannels(signalIdArray);
			interpretdatapacketformat(mNChannels,signalIdArray);
		} else if (mShimmerVersion==HW_ID_SHIMMER_SR30) {
			mPacketSize = 2+bufferInquiry[2]*2; 
			mSamplingRate = (double)1024/bufferInquiry[0];
			mAccelRange = bufferInquiry[1];
			mNChannels = bufferInquiry[2];
			mBufferSize = bufferInquiry[3];
			byte[] signalIdArray = new byte[mNChannels];
			System.arraycopy(bufferInquiry, 4, signalIdArray, 0, mNChannels); // this is 4 because there is no config byte
			interpretdatapacketformat(mNChannels,signalIdArray);

		}
	}


	protected void updateEnabledSensorsFromChannels(byte[] channels){
		// set the sensors value
		// crude way of getting this value, but allows for more customised firmware
		// to still work with this application
		// e.g. if any axis of the accelerometer is being transmitted, then it will
		// recognise that the accelerometer is being sampled
		int enabledSensors = 0;
		for (int i=0;i<channels.length;i++)
		{
			if (mShimmerVersion==HW_ID_SHIMMER_3){
				if (channels[i]==Configuration.Shimmer3.Channel.XAAccel || channels[i]==Configuration.Shimmer3.Channel.YAAccel || channels[i]==Configuration.Shimmer3.Channel.ZAAccel){
					enabledSensors = enabledSensors | SENSOR_ACCEL;
				}
				if (channels[i]==Configuration.Shimmer3.Channel.XDAccel || channels[i]==Configuration.Shimmer3.Channel.YDAccel||channels[i]==Configuration.Shimmer3.Channel.ZDAccel){
					enabledSensors = enabledSensors | SENSOR_DACCEL;
				}
				if (channels[i]==Configuration.Shimmer3.Channel.XGyro || channels[i]==Configuration.Shimmer3.Channel.YGyro||channels[i]==Configuration.Shimmer3.Channel.ZGyro){
					enabledSensors = enabledSensors | SENSOR_GYRO;
				}
				if (channels[i]==Configuration.Shimmer3.Channel.XMag || channels[i]==Configuration.Shimmer3.Channel.YMag||channels[i]==Configuration.Shimmer3.Channel.ZMag){
					enabledSensors = enabledSensors | SENSOR_MAG;
				}
				if (channels[i]==Configuration.Shimmer3.Channel.VBatt){
					enabledSensors = enabledSensors | SENSOR_BATT;
				}
				if (channels[i]==Configuration.Shimmer3.Channel.ExtAdc7){
					enabledSensors = enabledSensors | SENSOR_EXT_ADC_A7;
				}
				if (channels[i]==Configuration.Shimmer3.Channel.ExtAdc6){
					enabledSensors = enabledSensors | SENSOR_EXT_ADC_A6;
				}
				if (channels[i]==Configuration.Shimmer3.Channel.ExtAdc15){
					enabledSensors = enabledSensors | SENSOR_EXT_ADC_A15;
				}
				if (channels[i]==Configuration.Shimmer3.Channel.IntAdc1){
					enabledSensors = enabledSensors | SENSOR_INT_ADC_A1;
				}
				if (channels[i]==Configuration.Shimmer3.Channel.IntAdc12){
					enabledSensors = enabledSensors | SENSOR_INT_ADC_A12;
				}
				if (channels[i]==Configuration.Shimmer3.Channel.IntAdc13){
					enabledSensors = enabledSensors | SENSOR_INT_ADC_A13;
				}
				if (channels[i]==Configuration.Shimmer3.Channel.IntAdc14){
					enabledSensors = enabledSensors | SENSOR_INT_ADC_A14;
				}
				if (channels[i]==Configuration.Shimmer3.Channel.Pressure){
					enabledSensors = enabledSensors | SENSOR_BMP180;
				}
				if (channels[i]==Configuration.Shimmer3.Channel.Temperature){
					enabledSensors = enabledSensors | SENSOR_BMP180;
				}
				if (channels[i]==Configuration.Shimmer3.Channel.GsrRaw){
					enabledSensors = enabledSensors | SENSOR_GSR;
				}
				if (channels[i]==Configuration.Shimmer3.Channel.EXG_ADS1292R_1_STATUS){
					//enabledSensors = enabledSensors | SENSOR_EXG1_24BIT;
				}
				if (channels[i]==Configuration.Shimmer3.Channel.EXG_ADS1292R_1_CH1_24BIT){
					enabledSensors = enabledSensors | SENSOR_EXG1_24BIT;
				}
				if (channels[i]==Configuration.Shimmer3.Channel.EXG_ADS1292R_1_CH2_24BIT){
					enabledSensors = enabledSensors | SENSOR_EXG1_24BIT;
				}
				if (channels[i]==Configuration.Shimmer3.Channel.EXG_ADS1292R_1_CH1_16BIT){
					enabledSensors = enabledSensors | SENSOR_EXG1_16BIT;
				}
				if (channels[i]==Configuration.Shimmer3.Channel.EXG_ADS1292R_1_CH2_16BIT){
					enabledSensors = enabledSensors | SENSOR_EXG1_16BIT;
				}
				if (channels[i]==Configuration.Shimmer3.Channel.EXG_ADS1292R_2_STATUS){
					//enabledSensors = enabledSensors | SENSOR_EXG2_24BIT;
				}
				if (channels[i]==Configuration.Shimmer3.Channel.EXG_ADS1292R_2_CH1_24BIT){
					enabledSensors = enabledSensors | SENSOR_EXG2_24BIT;
				}
				if (channels[i]==Configuration.Shimmer3.Channel.EXG_ADS1292R_2_CH2_24BIT){
					enabledSensors = enabledSensors | SENSOR_EXG2_24BIT;
				}
				if (channels[i]==Configuration.Shimmer3.Channel.EXG_ADS1292R_2_CH1_16BIT){
					enabledSensors = enabledSensors | SENSOR_EXG2_16BIT;
				}
				if (channels[i]==Configuration.Shimmer3.Channel.EXG_ADS1292R_2_CH2_16BIT){
					enabledSensors = enabledSensors | SENSOR_EXG2_16BIT;
				}
				if ((channels[i] == Configuration.Shimmer3.Channel.BridgeAmpHigh) || (channels[i] == Configuration.Shimmer3.Channel.BridgeAmpLow))
				{
					enabledSensors = enabledSensors | SENSOR_BRIDGE_AMP;
				}

			} else if(mShimmerVersion==HW_ID_SHIMMER_2R){
				if (channels[i]==Configuration.Shimmer2.Channel.XAccel || channels[i]==Configuration.Shimmer2.Channel.YAccel||channels[i]==Configuration.Shimmer2.Channel.ZAccel){
					enabledSensors = enabledSensors | SENSOR_ACCEL;
				}
				if (channels[i]==Configuration.Shimmer2.Channel.XGyro || channels[i]==Configuration.Shimmer2.Channel.YGyro ||channels[i]==Configuration.Shimmer2.Channel.ZGyro){
					enabledSensors = enabledSensors | SENSOR_GYRO;
				}
				if (channels[i]==Configuration.Shimmer2.Channel.XMag || channels[i]==Configuration.Shimmer2.Channel.XMag ||channels[i]==Configuration.Shimmer2.Channel.XMag){
					enabledSensors = enabledSensors | SENSOR_MAG;
				}        	
				if ((channels[i] == Configuration.Shimmer2.Channel.EcgLaLl) || (channels[i] == Configuration.Shimmer2.Channel.EcgRaLl))
				{
					enabledSensors = enabledSensors | SENSOR_ECG;
				}
				else if (channels[i] == Configuration.Shimmer2.Channel.Emg)
				{
					enabledSensors = enabledSensors | SENSOR_EMG;
				}
				else if (channels[i] == Configuration.Shimmer2.Channel.AnExA0 && getPMux()==0)
				{
					enabledSensors = enabledSensors | SENSOR_EXP_BOARD_A0;
				}
				else if (channels[i] == Configuration.Shimmer2.Channel.AnExA7 && getPMux()==0)
				{
					enabledSensors = enabledSensors | SENSOR_EXP_BOARD_A7;
				}
				else if ((channels[i] == Configuration.Shimmer2.Channel.BridgeAmpHigh) || (channels[i] == Configuration.Shimmer2.Channel.BridgeAmpLow))
				{
					enabledSensors = enabledSensors | SENSOR_BRIDGE_AMP;
				}
				else if ((channels[i] == Configuration.Shimmer2.Channel.GsrRaw) || (channels[i] == Configuration.Shimmer2.Channel.GsrRes))
				{
					enabledSensors = enabledSensors | SENSOR_GSR;
				}
				else if (channels[i] == Configuration.Shimmer2.Channel.HeartRate)
				{
					enabledSensors = enabledSensors | SENSOR_HEART;
				}   else if (channels[i] == Configuration.Shimmer2.Channel.AnExA0 && getPMux()==1)
				{
					enabledSensors = enabledSensors | SENSOR_BATT;
				}
				else if (channels[i] == Configuration.Shimmer2.Channel.AnExA7 && getPMux()==1)
				{
					enabledSensors = enabledSensors | SENSOR_BATT;
				}
			} 
		}
		mEnabledSensors=enabledSensors;	
	}
	public String getDeviceName(){
		return mMyName;
	}
	public String getBluetoothAddress(){
		return  mMyBluetoothAddress;
	}
	public void setDeviceName(String deviceName) {
		mMyName = deviceName;
	}
	public byte[] getRawInquiryResponse(){
		return mInquiryResponseBytes;
	}

	public byte[] getRawCalibrationParameters(){

		byte[] rawcal=new byte[1];
		if (mShimmerVersion==HW_ID_SHIMMER_3)
		{
			//Accel + Digi Accel + Gyro + Mag + Pressure
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
			try {
				outputStream.write(5); // write the number of different calibration parameters
				outputStream.write( mAccelCalRawParams.length);
				outputStream.write( mAccelCalRawParams);
				outputStream.write( mDigiAccelCalRawParams.length);
				outputStream.write( mDigiAccelCalRawParams );
				outputStream.write( mGyroCalRawParams.length );
				outputStream.write( mGyroCalRawParams );
				outputStream.write( mMagCalRawParams.length );
				outputStream.write( mMagCalRawParams );
				outputStream.write( mPressureCalRawParams.length);
				outputStream.write( mPressureCalRawParams );
				rawcal = outputStream.toByteArray( );
			} catch (IOException e) {
				e.printStackTrace();
			}			

		} else if (mShimmerVersion==HW_ID_SHIMMER_2 ||mShimmerVersion==HW_ID_SHIMMER_2R)
		{
			//Accel + Digi Accel + Gyro + Mag + Pressure
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
			try 
			{
				outputStream.write(5); // write the number of different calibration parameters
				outputStream.write( mAccelCalRawParams.length);
				outputStream.write( mAccelCalRawParams);
				outputStream.write( mGyroCalRawParams.length );
				outputStream.write( mGyroCalRawParams );
				outputStream.write( mMagCalRawParams.length );
				outputStream.write( mMagCalRawParams );
				outputStream.write( mECGCalRawParams.length );
				outputStream.write( mECGCalRawParams );
				outputStream.write( mEMGCalRawParams.length );
				outputStream.write( mEMGCalRawParams );
				rawcal = outputStream.toByteArray( );
			} catch (IOException e) {
				e.printStackTrace();
			}		

		} else {
			rawcal[0]=0;
		}
		return rawcal;

	}



	public List<String> getListofEnabledSensors(){
		List<String> listofSensors = new ArrayList<String>();
		if (mShimmerVersion==HW_ID_SHIMMER_3){
			if (((mEnabledSensors & 0xFF)& SENSOR_ACCEL) > 0){
				listofSensors.add("Low Noise Accelerometer");
			}
			if ((mEnabledSensors & SENSOR_DACCEL) > 0){
				listofSensors.add("Wide Range Accelerometer");
			}
		} else {
			if (((mEnabledSensors & 0xFF)& SENSOR_ACCEL) > 0){
				listofSensors.add("Accelerometer");
			}
		}
		if (((mEnabledSensors & 0xFF)& SENSOR_GYRO) > 0) {
			listofSensors.add("Gyroscope");
		}
		if (((mEnabledSensors & 0xFF)& SENSOR_MAG) > 0) {
			listofSensors.add("Magnetometer");
		}
		if (((mEnabledSensors & 0xFF) & SENSOR_GSR) > 0) {
			listofSensors.add("GSR");
		}
		if (((mEnabledSensors & 0xFF) & SENSOR_ECG) > 0) {
			listofSensors.add("ECG");
		}
		if (((mEnabledSensors & 0xFF) & SENSOR_EMG) > 0) {
			listofSensors.add("EMG");
		}
		if (((mEnabledSensors & 0xFF00) & SENSOR_BRIDGE_AMP) > 0) {
			listofSensors.add("Bridge Amplifier");
		}
		if (((mEnabledSensors & 0xFF00) & SENSOR_HEART) > 0) {
			listofSensors.add("Heart Rate");
		}
		if (((mEnabledSensors & 0xFF) & SENSOR_EXP_BOARD_A0) > 0 && (mEnabledSensors & SENSOR_BATT) == 0 && mShimmerVersion != HW_ID_SHIMMER_3) {
			listofSensors.add("ExpBoard A0");
		}
		if (((mEnabledSensors & 0xFF) & SENSOR_EXP_BOARD_A7) > 0  && (mEnabledSensors & SENSOR_BATT) == 0 && mShimmerVersion != HW_ID_SHIMMER_3) {
			listofSensors.add("ExpBoard A7");
		}
		if ((mEnabledSensors & SENSOR_BATT) > 0) {
			listofSensors.add("Battery Voltage");
		}
		if (((mEnabledSensors & 0xFF) & SENSOR_EXT_ADC_A7) > 0  && mShimmerVersion == HW_ID_SHIMMER_3) {
			listofSensors.add("External ADC A7");
		}
		if (((mEnabledSensors & 0xFF) & SENSOR_EXT_ADC_A6) > 0  && mShimmerVersion == HW_ID_SHIMMER_3) {
			listofSensors.add("External ADC A6");
		}
		if (((mEnabledSensors & 0xFFFF) & SENSOR_EXT_ADC_A15) > 0  && mShimmerVersion == HW_ID_SHIMMER_3) {
			listofSensors.add("External ADC A15");
		}
		if (((mEnabledSensors & 0xFFFF) & SENSOR_INT_ADC_A1) > 0  && mShimmerVersion == HW_ID_SHIMMER_3) {
			listofSensors.add("Internal ADC A1");
		}
		if (((mEnabledSensors & 0xFFFF) & SENSOR_INT_ADC_A12) > 0  && mShimmerVersion == HW_ID_SHIMMER_3) {
			listofSensors.add("Internal ADC A12");
		}
		if (((mEnabledSensors & 0xFFFFFF) & SENSOR_INT_ADC_A13) > 0  && mShimmerVersion == HW_ID_SHIMMER_3) {
			listofSensors.add("Internal ADC A13");
		}
		if (((mEnabledSensors & 0xFFFFFF) & SENSOR_INT_ADC_A14) > 0  && mShimmerVersion == HW_ID_SHIMMER_3) {
			listofSensors.add("Internal ADC A14");
		}
		if ((mEnabledSensors & SENSOR_BMP180) > 0  && mShimmerVersion == HW_ID_SHIMMER_3) {
			listofSensors.add("Pressure");
		}
		if ((mEnabledSensors & SENSOR_EXG1_24BIT) > 0  && mShimmerVersion == HW_ID_SHIMMER_3) {
			listofSensors.add("EXG1");
		}
		if ((mEnabledSensors & SENSOR_EXG2_24BIT) > 0  && mShimmerVersion == HW_ID_SHIMMER_3) {
			listofSensors.add("EXG2");
		}
		if ((mEnabledSensors & SENSOR_EXG1_16BIT) > 0  && mShimmerVersion == HW_ID_SHIMMER_3) {
			listofSensors.add("EXG1 16Bit");
		}
		if ((mEnabledSensors & SENSOR_EXG2_16BIT) > 0  && mShimmerVersion == HW_ID_SHIMMER_3) {
			listofSensors.add("EXG2 16Bit");
		}

		return listofSensors;
	}
	
	/** Returns a list of string[] of the four properties. 1) Shimmer Name - 2) Property/Signal Name - 3) Format Name - 4) Unit Name
	 * @return list string array of properties
	 */
	public List<String[]> getListofEnabledSensorSignalsandFormats(){
		List<String[]> listofSignals = new ArrayList<String[]>();
		 
		if (mShimmerVersion!=HW_ID_SHIMMER_3){
			String[] channel = new String[]{mMyName,"Timestamp","CAL","mSecs"};
			listofSignals.add(channel);
			channel = new String[]{mMyName,"Timestamp","RAW",NO_UNIT};
			listofSignals.add(channel);
			if (((mEnabledSensors & 0xFF)& SENSOR_ACCEL) > 0){
				String unit = "m/(sec^2)";
				if (mDefaultCalibrationParametersAccel == true) {
					unit = "m/(sec^2)*";
				}
				
				channel = new String[]{mMyName,"Accelerometer X","CAL",unit};
				listofSignals.add(channel);
				channel = new String[]{mMyName,"Accelerometer X","RAW",NO_UNIT};
				listofSignals.add(channel);
				
				channel = new String[]{mMyName,"Accelerometer Y","CAL",unit};
				listofSignals.add(channel);
				channel = new String[]{mMyName,"Accelerometer Y","RAW",NO_UNIT};
				listofSignals.add(channel);
				
				channel = new String[]{mMyName,"Accelerometer Z","CAL",unit};
				listofSignals.add(channel);
				channel = new String[]{mMyName,"Accelerometer Z","RAW",NO_UNIT};
				listofSignals.add(channel);
				
			}
			if (((mEnabledSensors & 0xFF)& SENSOR_GYRO) > 0) {
				String unit = "deg/sec";
				if (mDefaultCalibrationParametersGyro == true) {
					unit = "deg/sec*";
				} 
				channel = new String[]{mMyName,"Gyroscope X","CAL",unit};
				listofSignals.add(channel);
				channel = new String[]{mMyName,"Gyroscope X","RAW",NO_UNIT};
				listofSignals.add(channel);
				
				channel = new String[]{mMyName,"Gyroscope Y","CAL",unit};
				listofSignals.add(channel);
				channel = new String[]{mMyName,"Gyroscope Y","RAW",NO_UNIT};
				listofSignals.add(channel);
				
				channel = new String[]{mMyName,"Gyroscope Z","CAL",unit};
				listofSignals.add(channel);
				channel = new String[]{mMyName,"Gyroscope Z","RAW",NO_UNIT};
				listofSignals.add(channel);
			}
			if (((mEnabledSensors & 0xFF)& SENSOR_MAG) > 0) {
				String unit = "local";
				if (mDefaultCalibrationParametersGyro == true) {
					unit = "local*";
				} 
				channel = new String[]{mMyName,"Magnetometer X","CAL",unit};
				listofSignals.add(channel);
				channel = new String[]{mMyName,"Magnetometer X","RAW",NO_UNIT};
				listofSignals.add(channel);
				
				channel = new String[]{mMyName,"Magnetometer Y","CAL",unit};
				listofSignals.add(channel);
				channel = new String[]{mMyName,"Magnetometer Y","RAW",NO_UNIT};
				listofSignals.add(channel);
				
				channel = new String[]{mMyName,"Magnetometer Z","CAL",unit};
				listofSignals.add(channel);
				channel = new String[]{mMyName,"Magnetometer Z","RAW",NO_UNIT};
				listofSignals.add(channel);
			}
			if (((mEnabledSensors & 0xFF) & SENSOR_GSR) > 0) {
				channel = new String[]{mMyName,"GSR","CAL","kOhms"};
				listofSignals.add(channel);
				channel = new String[]{mMyName,"Magnetometer X","RAW",NO_UNIT};
				listofSignals.add(channel);
			}
			if (((mEnabledSensors & 0xFF) & SENSOR_ECG) > 0) {
				
				String unit = "mVolts";
				if (mDefaultCalibrationParametersECG == true) {
					unit = "mVolts*";
				}
				
				channel = new String[]{mMyName,"ECG RA-LL","CAL",unit};
				listofSignals.add(channel);
				channel = new String[]{mMyName,"ECG RA-LL","RAW",NO_UNIT};
				listofSignals.add(channel);
				
				channel = new String[]{mMyName,"ECG LA-LL","CAL",unit};
				listofSignals.add(channel);
				channel = new String[]{mMyName,"ECG LA-LL","RAW",NO_UNIT};
				listofSignals.add(channel);
				
			}
			if (((mEnabledSensors & 0xFF) & SENSOR_EMG) > 0) {
				String unit = "mVolts";
				if (mDefaultCalibrationParametersECG == true) {
					unit = "mVolts*";
				}
				
				channel = new String[]{mMyName,"EMG","CAL",unit};
				listofSignals.add(channel);
				channel = new String[]{mMyName,"EMG","RAW",NO_UNIT};
				listofSignals.add(channel);
			}
			if (((mEnabledSensors & 0xFF00) & SENSOR_BRIDGE_AMP) > 0) {
				String unit = "mVolts";
				channel = new String[]{mMyName,"Bridge Amplifier High","CAL",unit};
				listofSignals.add(channel);
				channel = new String[]{mMyName,"Bridge Amplifier High","RAW",NO_UNIT};
				listofSignals.add(channel);
				
				channel = new String[]{mMyName,"Bridge Amplifier Low","CAL",unit};
				listofSignals.add(channel);
				channel = new String[]{mMyName,"Bridge Amplifier Low","RAW",NO_UNIT};
				listofSignals.add(channel);
			}
			if (((mEnabledSensors & 0xFF00) & SENSOR_HEART) > 0) {
				String unit = "BPM";
				channel = new String[]{mMyName,"Heart Rate","CAL",unit};
				listofSignals.add(channel);
				channel = new String[]{mMyName,"Heart Rate","RAW",NO_UNIT};
				listofSignals.add(channel);
			}
			if ((((mEnabledSensors & 0xFF) & SENSOR_EXP_BOARD_A0) > 0) && getPMux() == 0) {
				String unit = "mVolts";
				channel = new String[]{mMyName,"ExpBoard A0","CAL",unit};
				listofSignals.add(channel);
				channel = new String[]{mMyName,"ExpBoard A0","RAW",NO_UNIT};
				listofSignals.add(channel);
			}
			if ((((mEnabledSensors & 0xFF) & SENSOR_EXP_BOARD_A7) > 0 && getPMux() == 0)) {
				String unit = "mVolts";
				channel = new String[]{mMyName,"ExpBoard A7","CAL",unit};
				listofSignals.add(channel);
				channel = new String[]{mMyName,"ExpBoard A7","RAW",NO_UNIT};
				listofSignals.add(channel);
			}
			if (((mEnabledSensors & 0xFFFF) & SENSOR_BATT) > 0) {
				String unit = "mVolts";
				channel = new String[]{mMyName,"VSenseReg","CAL",unit};
				listofSignals.add(channel);
				channel = new String[]{mMyName,"VSenseReg","RAW",NO_UNIT};
				listofSignals.add(channel);
				channel = new String[]{mMyName,"VSenseBatt","CAL",unit};
				listofSignals.add(channel);
				channel = new String[]{mMyName,"VSenseBatt","RAW",NO_UNIT};
				listofSignals.add(channel);
			}
			if (((mEnabledSensors & 0xFF)& SENSOR_ACCEL) > 0 && ((mEnabledSensors & 0xFF)& SENSOR_GYRO) > 0 && ((mEnabledSensors & 0xFF)& SENSOR_MAG) > 0 && mOrientationEnabled){
				String unit = "local";
				channel = new String[]{mMyName,"Axis Angle A","CAL",unit};
				listofSignals.add(channel);
				channel = new String[]{mMyName,"Axis Angle X","CAL",unit};
				listofSignals.add(channel);
				channel = new String[]{mMyName,"Axis Angle Y","CAL",unit};
				listofSignals.add(channel);
				channel = new String[]{mMyName,"Axis Angle Z","CAL",unit};
				listofSignals.add(channel);
				channel = new String[]{mMyName,"Quaternion 0","CAL",unit};
				listofSignals.add(channel);
				channel = new String[]{mMyName,"Quaternion 1","CAL",unit};
				listofSignals.add(channel);
				channel = new String[]{mMyName,"Quaternion 2","CAL",unit};
				listofSignals.add(channel);
				channel = new String[]{mMyName,"Quaternion 3","CAL",unit};
				listofSignals.add(channel);
			}
//			if (((mEnabledSensors & 0xFF)& SENSOR_ACCEL) > 0 && ((mEnabledSensors & 0xFF)& SENSOR_GYRO) > 0 && ((mEnabledSensors & 0xFF)& SENSOR_MAG) > 0 && mOrientationEnabled){
//				String unit = "local";
//				channel = new String[]{mMyName,"Quaternion 0","CAL",unit};
//				listofSignals.add(channel);
//				channel = new String[]{mMyName,"Quaternion 1","CAL",unit};
//				listofSignals.add(channel);
//				channel = new String[]{mMyName,"Quaternion 2","CAL",unit};
//				listofSignals.add(channel);
//				channel = new String[]{mMyName,"Quaternion 3","CAL",unit};
//				listofSignals.add(channel);
//			}

		} else {

			String[] channel = new String[]{mMyName,"Timestamp","CAL","mSecs"};
			listofSignals.add(channel);
			channel = new String[]{mMyName,"Timestamp","RAW",NO_UNIT};
			listofSignals.add(channel);
			if ((mEnabledSensors & SENSOR_ACCEL) >0){

				String unit = "m/(sec^2)";
				if (mDefaultCalibrationParametersAccel == true) {
					unit = "m/(sec^2)*";
				}
				
				channel = new String[]{mMyName,"Low Noise Accelerometer X","CAL",unit};
				listofSignals.add(channel);
				channel = new String[]{mMyName,"Low Noise Accelerometer X","RAW",NO_UNIT};
				listofSignals.add(channel);
				
				channel = new String[]{mMyName,"Low Noise Accelerometer Y","CAL",unit};
				listofSignals.add(channel);
				channel = new String[]{mMyName,"Low Noise Accelerometer Y","RAW",NO_UNIT};
				listofSignals.add(channel);
				
				channel = new String[]{mMyName,"Low Noise Accelerometer Z","CAL",unit};
				listofSignals.add(channel);
				channel = new String[]{mMyName,"Low Noise Accelerometer Z","RAW",NO_UNIT};
				listofSignals.add(channel);
				
			}
			if ((mEnabledSensors& SENSOR_DACCEL) >0){


				String unit = "m/(sec^2)";
				if (mDefaultCalibrationParametersDigitalAccel == true) {
					unit = "m/(sec^2)*";
				}
				
				channel = new String[]{mMyName,"Wide Range Accelerometer X","CAL",unit};
				listofSignals.add(channel);
				channel = new String[]{mMyName,"Wide Range Accelerometer X","RAW",NO_UNIT};
				listofSignals.add(channel);
				
				channel = new String[]{mMyName,"Wide Range Accelerometer Y","CAL",unit};
				listofSignals.add(channel);
				channel = new String[]{mMyName,"Wide Range Accelerometer Y","RAW",NO_UNIT};
				listofSignals.add(channel);
				
				channel = new String[]{mMyName,"Wide Range Accelerometer Z","CAL",unit};
				listofSignals.add(channel);
				channel = new String[]{mMyName,"Wide Range Accelerometer Z","RAW",NO_UNIT};
				listofSignals.add(channel);
				
			
			}
			if (((mEnabledSensors & 0xFF)& SENSOR_GYRO) > 0) {
				String unit = "deg/sec";
				if (mDefaultCalibrationParametersGyro == true) {
					unit = "deg/sec*";
				} 
				channel = new String[]{mMyName,"Gyroscope X","CAL",unit};
				listofSignals.add(channel);
				channel = new String[]{mMyName,"Gyroscope X","RAW",NO_UNIT};
				listofSignals.add(channel);
				
				channel = new String[]{mMyName,"Gyroscope Y","CAL",unit};
				listofSignals.add(channel);
				channel = new String[]{mMyName,"Gyroscope Y","RAW",NO_UNIT};
				listofSignals.add(channel);
				
				channel = new String[]{mMyName,"Gyroscope Z","CAL",unit};
				listofSignals.add(channel);
				channel = new String[]{mMyName,"Gyroscope Z","RAW",NO_UNIT};
				listofSignals.add(channel);
			}
			if (((mEnabledSensors & 0xFF)& SENSOR_MAG) > 0) {
				String unit = "local";
				if (mDefaultCalibrationParametersGyro == true) {
					unit = "local*";
				} 
				channel = new String[]{mMyName,"Magnetometer X","CAL",unit};
				listofSignals.add(channel);
				channel = new String[]{mMyName,"Magnetometer X","RAW",NO_UNIT};
				listofSignals.add(channel);
				
				channel = new String[]{mMyName,"Magnetometer Y","CAL",unit};
				listofSignals.add(channel);
				channel = new String[]{mMyName,"Magnetometer Y","RAW",NO_UNIT};
				listofSignals.add(channel);
				
				channel = new String[]{mMyName,"Magnetometer Z","CAL",unit};
				listofSignals.add(channel);
				channel = new String[]{mMyName,"Magnetometer Z","RAW",NO_UNIT};
				listofSignals.add(channel);
			} 
			if (((mEnabledSensors & 0xFFFF) & SENSOR_BATT) > 0) {
				String unit = "mVolts";
				channel = new String[]{mMyName,"VSenseBatt","CAL",unit};
				listofSignals.add(channel);
				channel = new String[]{mMyName,"VSenseBatt","RAW",NO_UNIT};
				listofSignals.add(channel);
				
			}
			if (((mEnabledSensors & 0xFFFFFF)& SENSOR_EXT_ADC_A15) > 0) {
				String unit = "mVolts";
				channel = new String[]{mMyName,"External ADC A15","CAL",unit};
				listofSignals.add(channel);
				channel = new String[]{mMyName,"External ADC A15","RAW",NO_UNIT};
				listofSignals.add(channel);
				
			}
			if (((mEnabledSensors & 0xFFFFFF)& SENSOR_EXT_ADC_A7) > 0) {
				String unit = "mVolts";
				channel = new String[]{mMyName,"External ADC A7","CAL",unit};
				listofSignals.add(channel);
				channel = new String[]{mMyName,"External ADC A7","RAW",NO_UNIT};
				listofSignals.add(channel);
			}
			if (((mEnabledSensors & 0xFFFFFF)& SENSOR_EXT_ADC_A6) > 0) {
				String unit = "mVolts";
				channel = new String[]{mMyName,"External ADC A6","CAL",unit};
				listofSignals.add(channel);
				channel = new String[]{mMyName,"External ADC A6","RAW",NO_UNIT};
				listofSignals.add(channel);
			}
			if (((mEnabledSensors & 0xFFFFFF)& SENSOR_INT_ADC_A1) > 0) {
				String unit = "mVolts";
				channel = new String[]{mMyName,"Internal ADC A1","CAL",unit};
				listofSignals.add(channel);
				channel = new String[]{mMyName,"Internal ADC A1","RAW",NO_UNIT};
				listofSignals.add(channel);
			}
			if (((mEnabledSensors & 0xFFFFFF)& SENSOR_INT_ADC_A12) > 0) {
				String unit = "mVolts";
				channel = new String[]{mMyName,"Internal ADC A12","CAL",unit};
				listofSignals.add(channel);
				channel = new String[]{mMyName,"Internal ADC A12","RAW",NO_UNIT};
				listofSignals.add(channel);
			}
			if (((mEnabledSensors & 0xFFFFFF)& SENSOR_INT_ADC_A13) > 0) {
				String unit = "mVolts";
				channel = new String[]{mMyName,"Internal ADC A13","CAL",unit};
				listofSignals.add(channel);
				channel = new String[]{mMyName,"Internal ADC A13","RAW",NO_UNIT};
				listofSignals.add(channel);
			}
			if (((mEnabledSensors & 0xFFFFFF)& SENSOR_INT_ADC_A14) > 0) {
				String unit = "mVolts";
				channel = new String[]{mMyName,"Internal ADC A14","CAL",unit};
				listofSignals.add(channel);
				channel = new String[]{mMyName,"Internal ADC A14","RAW",NO_UNIT};
				listofSignals.add(channel);
			}
			if ((((mEnabledSensors & 0xFF)& SENSOR_ACCEL) > 0 || ((mEnabledSensors & 0xFFFF)& SENSOR_DACCEL) > 0)&& ((mEnabledSensors & 0xFF)& SENSOR_GYRO) > 0 && ((mEnabledSensors & 0xFF)& SENSOR_MAG) > 0 && mOrientationEnabled){
				String unit = "local";
				channel = new String[]{mMyName,"Axis Angle A","CAL",unit};
				listofSignals.add(channel);
				channel = new String[]{mMyName,"Axis Angle X","CAL",unit};
				listofSignals.add(channel);
				channel = new String[]{mMyName,"Axis Angle Y","CAL",unit};
				listofSignals.add(channel);
				channel = new String[]{mMyName,"Axis Angle Z","CAL",unit};
				listofSignals.add(channel);
				channel = new String[]{mMyName,"Quaternion 0","CAL",unit};
				listofSignals.add(channel);
				channel = new String[]{mMyName,"Quaternion 1","CAL",unit};
				listofSignals.add(channel);
				channel = new String[]{mMyName,"Quaternion 2","CAL",unit};
				listofSignals.add(channel);
				channel = new String[]{mMyName,"Quaternion 3","CAL",unit};
				listofSignals.add(channel);
			}
//			if ((((mEnabledSensors & 0xFF)& SENSOR_ACCEL) > 0 || ((mEnabledSensors & 0xFFFF)& SENSOR_DACCEL) > 0) && ((mEnabledSensors & 0xFF)& SENSOR_GYRO) > 0 && ((mEnabledSensors & 0xFF)& SENSOR_MAG) > 0 && mOrientationEnabled){
//				String unit = "local";
//				channel = new String[]{mMyName,"Quaternion 0","CAL",unit};
//				listofSignals.add(channel);
//				channel = new String[]{mMyName,"Quaternion 1","CAL",unit};
//				listofSignals.add(channel);
//				channel = new String[]{mMyName,"Quaternion 2","CAL",unit};
//				listofSignals.add(channel);
//				channel = new String[]{mMyName,"Quaternion 3","CAL",unit};
//				listofSignals.add(channel);
//			}
			if ((mEnabledSensors & SENSOR_BMP180)>0){
				channel = new String[]{mMyName,"Pressure","CAL","kPa"};
				listofSignals.add(channel);
				channel = new String[]{mMyName,"Pressure","RAW",NO_UNIT};
				listofSignals.add(channel);
				channel = new String[]{mMyName,"Temperature","CAL","Celsius"};
				listofSignals.add(channel);
				channel = new String[]{mMyName,"Temperature","RAW",NO_UNIT};
				listofSignals.add(channel);
			}
			if ((mEnabledSensors & SENSOR_GSR)>0){
				channel = new String[]{mMyName,"GSR","CAL","kOhms"};
				listofSignals.add(channel);
				channel = new String[]{mMyName,"Magnetometer X","RAW",NO_UNIT};
				listofSignals.add(channel);
			}
			if (((mEnabledSensors & SENSOR_EXG1_24BIT)>0)|| ((mEnabledSensors & SENSOR_EXG1_16BIT)>0)){
				channel = new String[]{mMyName,"EXG1 STATUS","RAW",NO_UNIT};
				listofSignals.add(channel);
			}
			if (((mEnabledSensors & SENSOR_EXG2_24BIT)>0)|| ((mEnabledSensors & SENSOR_EXG2_16BIT)>0)){
				channel = new String[]{mMyName,"EXG2 STATUS","RAW",NO_UNIT};
				listofSignals.add(channel);
			}
			if ((mEnabledSensors & SENSOR_EXG1_24BIT)>0){
				String unit = "mVolts";
				if (isEXGUsingDefaultECGConfiguration()){
					channel = new String[]{mMyName,"ECG LL-RA","CAL",unit};
					listofSignals.add(channel);
					channel = new String[]{mMyName,"ECG LL-RA","RAW",NO_UNIT};
					listofSignals.add(channel);
					
					channel = new String[]{mMyName,"ECG LL-RA","CAL",unit};
					listofSignals.add(channel);
					channel = new String[]{mMyName,"ECG LL-RA","RAW",NO_UNIT};
					listofSignals.add(channel);
				}
				else if (isEXGUsingDefaultEMGConfiguration()){
					channel = new String[]{mMyName,"EMG CH1","CAL",unit};
					listofSignals.add(channel);
					channel = new String[]{mMyName,"EMG CH1","RAW",NO_UNIT};
					listofSignals.add(channel);
					
					channel = new String[]{mMyName,"EMG CH2","CAL",unit};
					listofSignals.add(channel);
					channel = new String[]{mMyName,"EMG CH2","RAW",NO_UNIT};
					listofSignals.add(channel);
				} else {
					channel = new String[]{mMyName,"EXG1 CH1","CAL",unit};
					listofSignals.add(channel);
					channel = new String[]{mMyName,"EXG1 CH1","RAW",NO_UNIT};
					listofSignals.add(channel);
					
					channel = new String[]{mMyName,"EXG1 CH2","CAL",unit};
					listofSignals.add(channel);
					channel = new String[]{mMyName,"EXG1 CH2","RAW",NO_UNIT};
					listofSignals.add(channel);
				}

			}
			if ((mEnabledSensors & SENSOR_EXG2_24BIT)>0){
				String unit = "mVolts";
				if (isEXGUsingDefaultECGConfiguration()){
					channel = new String[]{mMyName,"EXG2 CH1","CAL",unit};
					listofSignals.add(channel);
					channel = new String[]{mMyName,"EXG2 CH1","RAW",NO_UNIT};
					listofSignals.add(channel);
					
					channel = new String[]{mMyName,"ECG Vx-RL","CAL",unit};
					listofSignals.add(channel);
					channel = new String[]{mMyName,"ECG Vx-RL","RAW",NO_UNIT};
					listofSignals.add(channel);
				}
				else if (isEXGUsingDefaultEMGConfiguration()){
					channel = new String[]{mMyName,"EXG2 CH1","CAL",unit};
					listofSignals.add(channel);
					channel = new String[]{mMyName,"EXG2 CH1","RAW",NO_UNIT};
					listofSignals.add(channel);
					
					channel = new String[]{mMyName,"EXG2 CH2","CAL",unit};
					listofSignals.add(channel);
					channel = new String[]{mMyName,"EXG2 CH2","RAW",NO_UNIT};
					listofSignals.add(channel);
				}
				else {
					channel = new String[]{mMyName,"EXG2 CH1","CAL",unit};
					listofSignals.add(channel);
					channel = new String[]{mMyName,"EXG2 CH1","RAW",NO_UNIT};
					listofSignals.add(channel);
					
					channel = new String[]{mMyName,"EXG2 CH2","CAL",unit};
					listofSignals.add(channel);
					channel = new String[]{mMyName,"EXG2 CH2","RAW",NO_UNIT};
					listofSignals.add(channel);
				
				}
			}
			if ((mEnabledSensors & SENSOR_EXG1_16BIT)>0){
				String unit = "mVolts";
				if (isEXGUsingDefaultECGConfiguration()){
					channel = new String[]{mMyName,"ECG LL-RA","CAL",unit};
					listofSignals.add(channel);
					channel = new String[]{mMyName,"ECG LL-RA","RAW",NO_UNIT};
					listofSignals.add(channel);
					
					channel = new String[]{mMyName,"ECG LL-RA","CAL",unit};
					listofSignals.add(channel);
					channel = new String[]{mMyName,"ECG LL-RA","RAW",NO_UNIT};
					listofSignals.add(channel);
				}
				else if (isEXGUsingDefaultEMGConfiguration()){
					channel = new String[]{mMyName,"EMG CH1","CAL",unit};
					listofSignals.add(channel);
					channel = new String[]{mMyName,"EMG CH1","RAW",NO_UNIT};
					listofSignals.add(channel);
					
					channel = new String[]{mMyName,"EMG CH2","CAL",unit};
					listofSignals.add(channel);
					channel = new String[]{mMyName,"EMG CH2","RAW",NO_UNIT};
					listofSignals.add(channel);
				} else {
					channel = new String[]{mMyName,"EXG1 CH1 16Bit","CAL",unit};
					listofSignals.add(channel);
					channel = new String[]{mMyName,"EXG1 CH1 16Bit","RAW",NO_UNIT};
					listofSignals.add(channel);
					
					channel = new String[]{mMyName,"EXG1 CH2 16Bit","CAL",unit};
					listofSignals.add(channel);
					channel = new String[]{mMyName,"EXG1 CH2 16Bit","RAW",NO_UNIT};
					listofSignals.add(channel);
				}

			}
			if ((mEnabledSensors & SENSOR_EXG2_16BIT)>0){
				String unit = "mVolts";
				if (isEXGUsingDefaultECGConfiguration()){
					channel = new String[]{mMyName,"EXG2 CH1","CAL",unit};
					listofSignals.add(channel);
					channel = new String[]{mMyName,"EXG2 CH1","RAW",NO_UNIT};
					listofSignals.add(channel);
					
					channel = new String[]{mMyName,"ECG Vx-RL","CAL",unit};
					listofSignals.add(channel);
					channel = new String[]{mMyName,"ECG Vx-RL","RAW",NO_UNIT};
					listofSignals.add(channel);
				}
				else if (isEXGUsingDefaultEMGConfiguration()){
					channel = new String[]{mMyName,"EXG2 CH1","CAL",unit};
					listofSignals.add(channel);
					channel = new String[]{mMyName,"EXG2 CH1","RAW",NO_UNIT};
					listofSignals.add(channel);
					
					channel = new String[]{mMyName,"EXG2 CH2","CAL",unit};
					listofSignals.add(channel);
					channel = new String[]{mMyName,"EXG2 CH2","RAW",NO_UNIT};
					listofSignals.add(channel);
				}
				else {
					channel = new String[]{mMyName,"EXG2 CH1 16Bit","CAL",unit};
					listofSignals.add(channel);
					channel = new String[]{mMyName,"EXG2 CH1 16Bit","RAW",NO_UNIT};
					listofSignals.add(channel);
					
					channel = new String[]{mMyName,"EXG2 CH2 16Bit","CAL",unit};
					listofSignals.add(channel);
					channel = new String[]{mMyName,"EXG2 CH2 16Bit","RAW",NO_UNIT};
					listofSignals.add(channel);
				
				}
			}
			if (((mEnabledSensors & 0xFF00) & SENSOR_BRIDGE_AMP) > 0) {
				String unit = "mVolts";
				channel = new String[]{mMyName,"Bridge Amplifier High","CAL",unit};
				listofSignals.add(channel);
				channel = new String[]{mMyName,"Bridge Amplifier High","RAW",NO_UNIT};
				listofSignals.add(channel);
				
				channel = new String[]{mMyName,"Bridge Amplifier Low","CAL",unit};
				listofSignals.add(channel);
				channel = new String[]{mMyName,"Bridge Amplifier Low","RAW",NO_UNIT};
				listofSignals.add(channel);
			}
		}
		
		if (mExtraSignalProperties != null){
			listofSignals.addAll(mExtraSignalProperties);
		}
		
		return listofSignals;
	}	
	
	public void addExtraSignalProperty(String [] property){
		if (mExtraSignalProperties==null){
			mExtraSignalProperties = new ArrayList<String[]>();
		}
		mExtraSignalProperties.add(property);
	}
	
	public void removeExtraSignalProperty(String [] property){
		for (int i=mExtraSignalProperties.size()-1;i>-1;i--){
			String[]p = mExtraSignalProperties.get(i);
			if (p[0].equals(property[0]) && p[1].equals(property[1]) && p[2].equals(property[2]) && p[3].equals(property[3])){
				mExtraSignalProperties.remove(i);
			}
			
		}
	}
	
	//AlignmentMatrixMag, SensitivityMatrixMag, OffsetVectorMag

	public double[][] getAlignmentMatrixMag(){
		return mAlignmentMatrixMagnetometer;
	}

	public double[][] getSensitivityMatrixMag(){
		return mSensitivityMatrixMagnetometer;
	}

	public double[][] getOffsetVectorMatrixMag(){
		return mOffsetVectorMagnetometer;
	}

	public double[][] getAlighmentMatrixGyro(){
		return mAlignmentMatrixGyroscope;
	}

	public double[][] getSensitivityMatrixGyro(){
		return mSensitivityMatrixGyroscope;
	}

	public double[][] getOffsetVectorMatrixGyro(){
		return mOffsetVectorGyroscope;
	}

	public double[][] getAlighmentMatrixAccel(){
		return mAlignmentMatrixAnalogAccel;
	}

	public double[][] getSensitivityMatrixAccel(){
		return mSensitivityMatrixAnalogAccel;
	}

	public double[][] getOffsetVectorMatrixAccel(){
		return mOffsetVectorAnalogAccel;
	}

	public double[][] getAlighmentMatrixWRAccel(){
		return mAlignmentMatrixWRAccel;
	}

	public double[][] getSensitivityMatrixWRAccel(){
		return mSensitivityMatrixWRAccel;
	}

	public double[][] getOffsetVectorMatrixWRAccel(){
		return mOffsetVectorWRAccel;
	}


	public long getEnabledSensors() {
		return mEnabledSensors;
	}

	public String[] getListofSupportedSensors(){
		String[] sensorNames = null;
		if (mShimmerVersion==HW_ID_SHIMMER_2R || mShimmerVersion==HW_ID_SHIMMER_2){
			sensorNames = Configuration.Shimmer2.ListofCompatibleSensors;
		} else if  (mShimmerVersion==HW_ID_SHIMMER_3){
			sensorNames = Configuration.Shimmer3.ListofCompatibleSensors;
		}
		return sensorNames;
	}

	public static String[] getListofSupportedSensors(int shimmerVersion){
		String[] sensorNames = null;
		if (shimmerVersion==HW_ID_SHIMMER_2R || shimmerVersion==HW_ID_SHIMMER_2){
			sensorNames = Configuration.Shimmer2.ListofCompatibleSensors;
		} else if  (shimmerVersion==HW_ID_SHIMMER_3){
			sensorNames = Configuration.Shimmer3.ListofCompatibleSensors;
		}
		return sensorNames;
	}

	/**
	 * @param enable this enables the calibration of the gyroscope while streaming
	 * @param bufferSize sets the buffersize of the window used to determine the new calibration parameters, see implementation for more details
	 * @param threshold sets the threshold of when to use the incoming data to recalibrate gyroscope offset, this is in degrees, and the default value is 1.2
	 */
	public void enableOnTheFlyGyroCal(boolean enable,int bufferSize,double threshold){
		if (enable){
			mGyroOVCalThreshold=threshold;
			mGyroX=new DescriptiveStatistics(bufferSize);
			mGyroY=new DescriptiveStatistics(bufferSize);
			mGyroZ=new DescriptiveStatistics(bufferSize);
			mGyroXRaw=new DescriptiveStatistics(bufferSize);
			mGyroYRaw=new DescriptiveStatistics(bufferSize);
			mGyroZRaw=new DescriptiveStatistics(bufferSize);
			mEnableOntheFlyGyroOVCal = enable;
		}
	}

	protected int convertEXGGainSettingToValue(int setting){

		if (setting==0){
			return 6;
		} else if (setting==1){
			return 1;
		} else if (setting==2){
			return 2;
		} else if (setting==3){
			return 3;
		} else if (setting==4){
			return 4;
		} else if (setting==5){
			return 8;
		} else if (setting==6){
			return 12;
		}
		else {
			return -1; // -1 means invalid value
		}

	}

	/**
	 * Checks the EXG register bytes, and determines whether default ecg/emg are being used. 
	 * @return 0 for ECG, 1 for EMG, 3 for test signal and 4 for custom
	 */
	public int checkEXGConfiguration(){
		return -1;
	}

	public byte[] getEXG1RegisterContents(){
		return mEXG1Register;
	}

	public byte[] getEXG2RegisterContents(){
		return mEXG2Register;
	}

	protected boolean isEXGUsingDefaultECGConfiguration(){
		boolean using = false;
		if(((mEXG1Register[3] & 15)==0)&&((mEXG1Register[4] & 15)==0)&& ((mEXG2Register[3] & 15)==0)&&((mEXG2Register[4] & 15)==7)){
			using = true;
		}
		return using;
	}

	protected boolean isEXGUsingDefaultTestSignalConfiguration(){
		boolean using = false;
		if(((mEXG1Register[3] & 15)==5)&&((mEXG1Register[4] & 15)==5)&& ((mEXG2Register[3] & 15)==5)&&((mEXG2Register[4] & 15)==5)){
			using = true;
		}
		return using;
	}

	protected boolean isEXGUsingDefaultEMGConfiguration(){
		boolean using = false;
		if(((mEXG1Register[3] & 15)==9)&&((mEXG1Register[4] & 15)==0)&& ((mEXG2Register[3] & 15)==1)&&((mEXG2Register[4] & 15)==1)){
			using = true;
		}
		return using;
	}

	public byte[] getPressureRawCoefficients(){
		return mPressureCalRawParams;
	}
	
	protected int getExg1CH1GainValue(){
		return mEXG1CH1GainValue;
	}
	
	protected int getExg1CH2GainValue(){
		return mEXG1CH2GainValue;
	}
	
	protected int getExg2CH1GainValue(){
		return mEXG2CH1GainValue;
	}
	
	protected int getExg2CH2GainValue(){
		return mEXG2CH2GainValue;
	}
	
	public String parseReferenceElectrodeTotring(int referenceElectrode){
		String refElectrode = "Unknown";
		
		if(referenceElectrode==0 && (isEXGUsingDefaultECGConfiguration() || isEXGUsingDefaultEMGConfiguration()))
			refElectrode = "Fixed Potential";
		else if(referenceElectrode==13 && isEXGUsingDefaultECGConfiguration())
			refElectrode = "Inverse Wilson CT";
		else if(referenceElectrode==3 && isEXGUsingDefaultEMGConfiguration())
			refElectrode = "Inverse of Ch1";
		
		return refElectrode;
	}
	
	public String parseLeadOffComparatorTresholdToString(int treshold){
		
		String tresholdString="";
		switch(treshold){
			case 0:
				tresholdString = "Pos:95% - Neg:5%";
			break;
			case 1:
				tresholdString = "Pos:92.5% - Neg:7.5%";
			break;
			case 2:
				tresholdString = "Pos:90% - Neg:10%";
			break;
			case 3:
				tresholdString = "Pos:87.5% - Neg:12.5%";
			break;
			case 4:
				tresholdString = "Pos:85% - Neg:15%";
			break;
			case 5:
				tresholdString = "Pos:80% - Neg:20%";
			break;
			case 6:
				tresholdString = "Pos:75% - Neg:25%";
				break;
			case 7:
				tresholdString = "Pos:70% - Neg:30%";
			break;
			default:
				tresholdString = "Treshold unread";
			break;
		}
		
		return tresholdString;
	}
	
	public String parseLeadOffModeToString(int leadOffMode){
		
		String modeString="";
		switch(leadOffMode){
			case 0:
				modeString +="Off";
			break;
			case 1:
				modeString +="DC Current";
			break;
			case 2:
				modeString +="AC Current";
			break;
			default:
				modeString +="No mode selected";
			break;
		}
		
		return modeString;
	}
	
	public String parseLeadOffDetectionCurrentToString(int current){
		
		String currentString="";
		switch(current){
			case 0:
				currentString +="6 nA";
			break;
			case 1:
				currentString +="22 nA";
			break;
			case 2:
				currentString +="6 uA";
			break;
			default:
				currentString +="22 uA";
			break;
		}
		
		return currentString;
	}
	
	public void setShimmerSamplingRate(double rate){
		
		double maxRate = 0.0;
		if (mShimmerVersion==HW_ID_SHIMMER_2 || mShimmerVersion==HW_ID_SHIMMER_2R){
			maxRate = 1024.0;
		} else if (mShimmerVersion==HW_ID_SHIMMER_3) {
			maxRate = 32768.0;
		}		
    	// don't let sampling rate < 0 OR > maxRate
    	if(rate <= 0) rate = 1.0;
    	else if (rate > maxRate) rate = maxRate;
    	
    	 // get Shimmer compatible sampling rate
    	Double actualSamplingRate = maxRate/Math.floor(maxRate/rate);
    	 // round sampling rate to two decimal places
    	actualSamplingRate = (double)Math.round(actualSamplingRate * 100) / 100;
		mSamplingRate = actualSamplingRate;
		
		if (mShimmerVersion==HW_ID_SHIMMER_2 || mShimmerVersion==HW_ID_SHIMMER_2R){
			if (!mLowPowerMag){
				if (rate<=10) {
					mShimmer2MagRate = 4;
				} else if (rate<=20) {
					mShimmer2MagRate = 5;
				} else {
					mShimmer2MagRate = 6;
				}
			} else {
				mShimmer2MagRate = 4;
			}
//			rate=1024/rate; //the equivalent hex setting
//			mListofInstructions.add(new byte[]{SET_SAMPLING_RATE_COMMAND, (byte)Math.rint(rate), 0x00});
			
		} else if (mShimmerVersion==HW_ID_SHIMMER_3) {
			setLSM303MagRateFromFreq(mSamplingRate);
			setLSM303AccelRateFromFreq(mSamplingRate);
			setMPU9150GyroAccelRateFromFreq(mSamplingRate);

//			int samplingByteValue = (int) (32768/rate);
//			mListofInstructions.add(new byte[]{SET_SAMPLING_RATE_COMMAND, (byte)(samplingByteValue&0xFF), (byte)((samplingByteValue>>8)&0xFF)});

		}
	}
	
	private int setLSM303AccelRateFromFreq(double freq) {
		// Unused: 8 = 1.620kHz (only low-power mode), 9 = 1.344kHz (normal-mode) / 5.376kHz (low-power mode) 
		if (!mLowPowerAccelWR){
			if (mSamplingRate<=1){
				mLSM303DigitalAccelRate = 1; // 1Hz
			} else if (mSamplingRate<=10){
				mLSM303DigitalAccelRate = 2; // 10Hz
			} else if (mSamplingRate<=25){
				mLSM303DigitalAccelRate = 3; // 25Hz
			} else if (mSamplingRate<=50){
				mLSM303DigitalAccelRate = 4; // 50Hz
			} else if (mSamplingRate<=100){
				mLSM303DigitalAccelRate = 5; // 100Hz
			} else if (mSamplingRate<=200){
				mLSM303DigitalAccelRate = 6; // 200Hz
			} else if (mSamplingRate<=400){
				mLSM303DigitalAccelRate = 7; // 400Hz
			} else {
				mLSM303DigitalAccelRate = 9; // 1344Hz
			}
		}
		else {
			if (mSamplingRate>=10){
				mLSM303DigitalAccelRate = 2; // 10Hz
			} else {
				mLSM303DigitalAccelRate = 1; // 1Hz
			}
		}
		return mLSM303DigitalAccelRate;
	}
	
	private int setLSM303MagRateFromFreq(double freq) {
		if (!mLowPowerMag){
			if (mSamplingRate<=0.75){
				mLSM303MagRate = 0; // 0.75Hz
			} else if (mSamplingRate<=1){
				mLSM303MagRate = 1; // 1.5Hz
			} else if (mSamplingRate<=3) {
				mLSM303MagRate = 2; // 3Hz
			} else if (mSamplingRate<=7.5) {
				mLSM303MagRate = 3; // 7.5Hz
			} else if (mSamplingRate<=15) {
				mLSM303MagRate = 4; // 15Hz
			} else if (mSamplingRate<=30) {
				mLSM303MagRate = 5; // 30Hz
			} else if (mSamplingRate<=75) {
				mLSM303MagRate = 6; // 75Hz
			} else {
				mLSM303MagRate = 7; // 220Hz
			}
		} else {
			if (mSamplingRate>=10){
				mLSM303MagRate = 4; // 15Hz
			} else {
				mLSM303MagRate = 1; // 1.5Hz
			}
		}		
		return mLSM303MagRate;
	}
	
	public int setMPU9150GyroAccelRateFromFreq(double freq) {
		if (!mLowPowerGyro){
			// Gyroscope Output Rate = 8kHz when the DLPF is disabled (DLPF_CFG = 0 or 7), and 1kHz when the DLPF is enabled
			double numerator = 1000;
			if(mMPU9150LPF == 0) {
				numerator = 8000;
			}

			if(freq<4) {
				freq = 4;
			}
			else if(freq>numerator) {
				freq = numerator;
			}
			int result = (int) (numerator / freq);
			if(result>255) result = 255;
			mMPU9150GyroAccelRate = result;

//			if (mSamplingRate<=51.28) {
//				mMPU9150GyroAccelRate = 0x9B; // Dec. = 155, Freq. = 51.28Hz 
//			} else if (mSamplingRate<=102.56) {
//				mMPU9150GyroAccelRate = 0x4D; // Dec. = 77, Freq. = 102.56Hz
//			} else if (mSamplingRate<=129.03) {
//				mMPU9150GyroAccelRate = 0x3D; // Dec. = 61, Freq. = 129.03Hz
//			} else if (mSamplingRate<=173.91) {
//				mMPU9150GyroAccelRate = 0x2D; // Dec. = 45, Freq. = 173.91Hz
//			} else if (mSamplingRate<=205.13) {
//				mMPU9150GyroAccelRate = 0x26; // Dec. = 38, Freq. = 205.13Hz
//			} else if (mSamplingRate<=258.06) {
//				mMPU9150GyroAccelRate = 0x1E; // Dec. = 30, Freq. = 258.06Hz
//			} else if (mSamplingRate<=533.33) {
//				mMPU9150GyroAccelRate = 0x0E; // Dec. = 14, Freq. = 533.33Hz
//			} else {
//				mMPU9150GyroAccelRate = 0x06; // Dec. = 6, Freq. = 1142.86Hz
//			}
		}
		else {
			mMPU9150GyroAccelRate = 0xFF; // Dec. = 255, Freq. = 31.25Hz
		}
		return mMPU9150GyroAccelRate;
	}
	
	public boolean isLowPowerGyro() {
		if(mMPU9150GyroAccelRate == 0xFF) {
			mLowPowerGyro = true;
		}
		else {
			mLowPowerGyro = false;
		}
		return mLowPowerGyro;
	}

	public boolean isLowPowerMag() {
		if(mLSM303MagRate <= 4) {
//		if((mLSM303MagRate == 4)||(mLSM303MagRate == 1)) {
			mLowPowerMag = true;
		}
		else {
			mLowPowerMag = false;
		}
		return mLowPowerMag;
	}
	
	
	public void exgBytesReadFrom(int mTempChipID, byte[] bufferAns) {
		// MN hack to overcome possible backward compatability issues
		int index = 1;
		if(bufferAns.length == 10) {
			index = 0;
		}
		
		if (mTempChipID==1){
			System.arraycopy(bufferAns, index, mEXG1Register, 0, 10);
//			System.arraycopy(bufferAns, 1, mEXG1Register, 0, 10);// MN removed
			// retrieve the gain and rate from the the registers
			mEXG1RateSetting = mEXG1Register[0] & 7;
			mEXG1CH1GainSetting = (mEXG1Register[3] >> 4) & 7;
			mEXG1CH1GainValue = convertEXGGainSettingToValue(mEXG1CH1GainSetting);
			mEXG1CH2GainSetting = (mEXG1Register[4] >> 4) & 7;
			mEXG1CH2GainValue = convertEXGGainSettingToValue(mEXG1CH2GainSetting);
			mRefenceElectrode = mEXG1Register[5] & 0x0F;
			mLeadOffCurrentModeChip1 = mEXG1Register[2] & 1;
			mComparatorsChip1 = mEXG1Register[1] & 0x40;								
			mRLDSense = mEXG1Register[5] & 0x10;
			m2P1N1P = mEXG1Register[6] & 0x0f;
			mLeadOffDetectionCurrent = (mEXG1Register[2] >> 2) & 3;
			mLeadOffComparatorTreshold = (mEXG1Register[2] >> 5) & 7;
		} else if (mTempChipID==2){
			System.arraycopy(bufferAns, index, mEXG2Register, 0, 10);
			//System.arraycopy(bufferAns, 1, mEXG2Register, 0, 10); // MN removed
			mEXG2RateSetting = mEXG2Register[0] & 7;
			mEXG2CH1GainSetting = (mEXG2Register[3] >> 4) & 7;
			mEXG2CH1GainValue = convertEXGGainSettingToValue(mEXG2CH1GainSetting);
			mEXG2CH2GainSetting = (mEXG2Register[4] >> 4) & 7;
			mEXG2CH2GainValue = convertEXGGainSettingToValue(mEXG2CH2GainSetting);
			mLeadOffCurrentModeChip2 = mEXG2Register[2] & 1;
			mComparatorsChip2 = mEXG2Register[1] & 0x40;
			m2P = mEXG2Register[6] & 0x0f;
		}
	}

//	public void exgBytesWriteTo() {
////		System.arraycopy(bufferAns, 1, mEXG1Register, 0, 10);
//		// retrieve the gain and rate from the the registers
//		mEXG1RateSetting = mEXG1Register[0] & 7;
//		mEXG1CH1GainSetting = (mEXG1Register[3] >> 4) & 7;
//		mEXG1CH1GainValue = convertEXGGainSettingToValue(mEXG1CH1GainSetting);
//		mEXG1CH2GainSetting = (mEXG1Register[4] >> 4) & 7;
//		mEXG1CH2GainValue = convertEXGGainSettingToValue(mEXG1CH2GainSetting);
//		mRefenceElectrode = mEXG1Register[5] & 0x0F;
//		mLeadOffCurrentModeChip1 = mEXG1Register[2] & 1;
//		mComparatorsChip1 = mEXG1Register[1] & 0x40;								
//		mRLDSense = mEXG1Register[5] & 0x10;
//		m2P1N1P = mEXG1Register[6] & 0x0f;
//		mLeadOffDetectionCurrent = (mEXG1Register[2] >> 2) & 3;
//		mLeadOffComparatorTreshold = (mEXG1Register[2] >> 5) & 7;
//
////		System.arraycopy(bufferAns, 1, mEXG2Register, 0, 10);						
//		mEXG2RateSetting = mEXG2Register[0] & 7;
//		mEXG2CH1GainSetting = (mEXG2Register[3] >> 4) & 7;
//		mEXG2CH1GainValue = convertEXGGainSettingToValue(mEXG2CH1GainSetting);
//		mEXG2CH2GainSetting = (mEXG2Register[4] >> 4) & 7;
//		mEXG2CH2GainValue = convertEXGGainSettingToValue(mEXG2CH2GainSetting);
//		mLeadOffCurrentModeChip2 = mEXG2Register[2] & 1;
//		mComparatorsChip2 = mEXG2Register[1] & 0x40;
//		m2P = mEXG2Register[6] & 0x0f;
//	}
	
	
	/**
	 * Checks if 16 bit ECG configuration is set on the Shimmer device. Do not use this command right after setting an EXG setting, as due to the execution model, the old settings might be returned, if this command is executed before an ack is received.
	 * @return true if 16 bit ECG is set
	 */
	public boolean isEXGUsingECG16Configuration(){
		boolean using = false;
		if ((mEnabledSensors & SENSOR_EXG1_16BIT)>0 && (mEnabledSensors & SENSOR_EXG2_16BIT)>0){
			if(isEXGUsingDefaultECGConfiguration()){
				using = true;
			}
		}
		return using;
	}
	
	/**
	 * Checks if 24 bit ECG configuration is set on the Shimmer device. Do not use this command right after setting an EXG setting, as due to the execution model, the old settings might be returned, if this command is executed before an ack is received.
	 * @return true if 24 bit ECG is set
	 */
	public boolean isEXGUsingECG24Configuration(){
		boolean using = false;
		if ((mEnabledSensors & SENSOR_EXG1_24BIT)>0 && (mEnabledSensors & SENSOR_EXG2_24BIT)>0){
			if(isEXGUsingDefaultECGConfiguration()){
				using = true;
			}
		}
		return using;
	}
	
	/**
	 * Checks if 16 bit EMG configuration is set on the Shimmer device.  Do not use this command right after setting an EXG setting, as due to the execution model, the old settings might be returned, if this command is executed before an ack is received. 
	 * @return true if 16 bit EMG is set
	 */
	public boolean isEXGUsingEMG16Configuration(){
		boolean using = false;
		if ((mEnabledSensors & SENSOR_EXG1_16BIT)>0 && (mEnabledSensors & SENSOR_EXG2_16BIT)>0){
			if(isEXGUsingDefaultEMGConfiguration()){
				using = true;
			}
		}
		return using;
	}
	
	/**
	 * Checks if 24 bit EMG configuration is set on the Shimmer device.  Do not use this command right after setting an EXG setting, as due to the execution model, the old settings might be returned, if this command is executed before an ack is received.
	 * @return true if 24 bit EMG is set
	 */
	public boolean isEXGUsingEMG24Configuration(){
		boolean using = false;
		if ((mEnabledSensors & SENSOR_EXG1_24BIT)>0 && (mEnabledSensors & SENSOR_EXG2_24BIT)>0){
			if(isEXGUsingDefaultEMGConfiguration()){
				using = true;
			}
		}
		return using;
	}
	
	/**
	 * Checks if 16 bit test signal configuration is set on the Shimmer device. Do not use this command right after setting an EXG setting, as due to the execution model, the old settings might be returned, if this command is executed before an ack is received.
	 * @return true if 24 bit test signal is set
	 */
	public boolean isEXGUsingTestSignal16Configuration(){
		boolean using = false;
		if ((mEnabledSensors & SENSOR_EXG1_16BIT)>0 && (mEnabledSensors & SENSOR_EXG2_16BIT)>0){
			if(isEXGUsingDefaultTestSignalConfiguration()){
				using = true;
			}
		}
		return using;
	}
	
	/**
	 * Checks if 24 bit test signal configuration is set on the Shimmer device.
	 * @return true if 24 bit test signal is set
	 */
	public boolean isEXGUsingTestSignal24Configuration(){
		boolean using = false;
		if ((mEnabledSensors & SENSOR_EXG1_24BIT)>0 && (mEnabledSensors & SENSOR_EXG2_24BIT)>0){
			if(isEXGUsingDefaultTestSignalConfiguration()){
				using = true;
			}
		}
		return using;
	}	
	
	
	//region --------- ENABLE/DISABLE FUNCTIONS --------- 
	/**** ENABLE FUNCTIONS *****/
	
	/**
	 * This enables the calculation of 3D orientation through the use of the gradient descent algorithm, note that the user will have to ensure that mEnableCalibration has been set to true (see enableCalibration), and that the accel, gyro and mag has been enabled
	 * @param enable
	 */
	protected void set3DOrientation(boolean enable){
		//enable the sensors if they have not been enabled 
		mOrientationEnabled = enable;
	}	
	
	/**
	 * This enables the low power accel option. When not enabled the sampling rate of the accel is set to the closest value to the actual sampling rate that it can achieve. In low power mode it defaults to 10Hz. Also and additional low power mode is used for the LSM303DLHC. This command will only supports the following Accel range +4g, +8g , +16g 
	 * @param enable
	 */
	protected void setLowPowerAccelWR(boolean enable){
		mLowPowerAccelWR = enable;
		mHighResAccelWR = !enable;

		setLSM303AccelRateFromFreq(mSamplingRate);
	}
	
	
	/**
	 * This enables the low power accel option. When not enabled the sampling rate of the accel is set to the closest value to the actual sampling rate that it can achieve. In low power mode it defaults to 10Hz. Also and additional low power mode is used for the LSM303DLHC. This command will only supports the following Accel range +4g, +8g , +16g 
	 * @param enable
	 */
	protected void setLowPowerGyro(boolean enable){
		mLowPowerGyro = enable;
		setMPU9150GyroAccelRateFromFreq(mSamplingRate);
	}
	
	/**
	 * This enables the low power mag option. When not enabled the sampling rate of the mag is set to the closest value to the actual sampling rate that it can achieve. In low power mode it defaults to 10Hz
	 * @param enable
	 */
	protected void setLowPowerMag(boolean enable){
		mLowPowerMag = enable;
		if (mShimmerVersion!=HW_ID_SHIMMER_3){
			if (!mLowPowerMag){
				if (mSamplingRate>=50){
					mShimmer2MagRate = 6;
				} else if (mSamplingRate>=20) {
					mShimmer2MagRate = 5;
				} else if (mSamplingRate>=10) {
					mShimmer2MagRate = 4;
				} else {
					mShimmer2MagRate = 3;
				}
			} else {
				mShimmer2MagRate = 4;
			}
		} else {
			setLSM303MagRateFromFreq(mSamplingRate);
		}
	}
	
	
	/**
	 *This can only be used for Shimmer3 devices (EXG) 
	 *When a enable configuration is load, the advanced exg configuration is removed, so it needs to be set again
	 * 
	 */
	 protected void setDefaultECGConfiguration() {
		 if (mShimmerVersion==3){
			byte[] mEXG1Register = {(byte) 2,(byte) 160,(byte) 16,(byte) 64,(byte) 64,(byte) 45,(byte) 0,(byte) 0,(byte) 2,(byte) 3};
			byte[] mEXG2Register = {(byte) 2,(byte) 160,(byte) 16,(byte) 64,(byte) 71,(byte) 0,(byte) 0,(byte) 0,(byte) 2,(byte) 1};
			if (mSamplingRate<=128){
				mEXG1Register[0]=0;
				mEXG2Register[0]=0;
			} else if (mSamplingRate<=256){
				mEXG1Register[0]=1;
				mEXG2Register[0]=1;
			}
			else if (mSamplingRate<=512){
				mEXG1Register[0]=2;
				mEXG2Register[0]=2;
			}
			exgBytesReadFrom(1, mEXG1Register);
			exgBytesReadFrom(2, mEXG2Register);
		 }
	}

	/**
	 * This can only be used for Shimmer3 devices (EXG)
	 * When a enable configuration is load, the advanced exg configuration is removed, so it needs to be set again
	 */
	 protected void setDefaultEMGConfiguration(){
		if (mShimmerVersion==3){
			mEXG1Register = new byte[]{(byte) 2,(byte) 160,(byte) 16,(byte) 105,(byte) 96,(byte) 32,(byte) 0,(byte) 0,(byte) 2,(byte) 3};
			mEXG2Register = new byte[]{(byte) 2,(byte) 160,(byte) 16,(byte) 129,(byte) 129,(byte) 0,(byte) 0,(byte) 0,(byte) 2,(byte) 1};
			if (mSamplingRate<=128){
				mEXG1Register[0]=0;
				mEXG2Register[0]=0;
			} else if (mSamplingRate<=256){
				mEXG1Register[0]=1;
				mEXG2Register[0]=1;
			}
			else if (mSamplingRate<=512){
				mEXG1Register[0]=2;
				mEXG2Register[0]=2;
			}
			exgBytesReadFrom(1, mEXG1Register);
			exgBytesReadFrom(2, mEXG2Register);
		}
		
	}

	/**
	 * This can only be used for Shimmer3 devices (EXG). Enables the test signal (square wave) of both EXG chips, to use, both EXG1 and EXG2 have to be enabled
	 */
	 protected void setEXGTestSignal(){
		if (mShimmerVersion==3){
			byte[] mEXG1Register = {(byte) 2,(byte) 163,(byte) 16,(byte) 5,(byte) 5,(byte) 0,(byte) 0,(byte) 0,(byte) 2,(byte) 1};
			byte[] mEXG2Register = {(byte) 2,(byte) 163,(byte) 16,(byte) 5,(byte) 5,(byte) 0,(byte) 0,(byte) 0,(byte) 2,(byte) 1};
			if (mSamplingRate<=128){
				mEXG1Register[0]=0;
				mEXG2Register[0]=0;
			} else if (mSamplingRate<=256){
				mEXG1Register[0]=1;
				mEXG2Register[0]=1;
			}
			else if (mSamplingRate<=512){
				mEXG1Register[0]=2;
				mEXG2Register[0]=2;
			}
			exgBytesReadFrom(1, mEXG1Register);
			exgBytesReadFrom(2, mEXG2Register);
		}
	}

	//endregion
	
	//TODO set all defaults
	protected void setDefaultShimmerConfiguration() {
		if (mShimmerVersion != -1){
			mShimmerUserAssignedName = "Default";
			mExperimentName = "Trial001";
			mSamplingRate = 51.20;
			
			mExperimentNumberOfShimmers = 1;
			mButtonStart = 1;
			
			createMapOfChannels();
			if (mShimmerVersion == HW_ID_SHIMMER_2R){
				
			}
			else {
				mShimmerSensorsMap.get(SENSORMAPKEY_SHIMMER3_A_ACCEL).mIsEnabled = true;
				mShimmerSensorsMap.get(SENSORMAPKEY_SHIMMER3_MPU9150_GYRO).mIsEnabled = true;
				mShimmerSensorsMap.get(SENSORMAPKEY_SHIMMER3_LSM303DLHC_MAG).mIsEnabled = true;
				mShimmerSensorsMap.get(SENSORMAPKEY_SHIMMER3_VBATT).mIsEnabled = true;
			}
		}
	}
	
	protected void infoMemByteArrayParse(byte[] infoMemContents) {

		// Check first 6 bytes of InfoMem for 0xFF to determine if contents are valid 
		byte[] comparisonBuffer = new byte[]{-1,-1,-1,-1,-1,-1};
		byte[] detectionBuffer = new byte[comparisonBuffer.length];
		System.arraycopy(infoMemContents, 0, detectionBuffer, 0, detectionBuffer.length);
		if(Arrays.equals(comparisonBuffer, detectionBuffer)) {
			// InfoMem not valid
			setDefaultShimmerConfiguration();
//			mShimmerInfoMemBytes = infoMemByteArrayGenerate();
//			mShimmerInfoMemBytes = new byte[infoMemContents.length];
			mShimmerInfoMemBytes = infoMemContents;
		}
		else {
			// InfoMem valid
			
			mShimmerInfoMemBytes = infoMemContents;
			InfoMemLayout iM = new InfoMemLayout(mFirmwareVersionCode, mFirmwareVersionMajor, mFirmwareVersionMinor, mFirmwareVersionRelease);
			
	//		mRawSamplingRate = ((int)(infoMemContents[NV_SAMPLING_RATE] & 0xFF) + (((int)(infoMemContents[NV_SAMPLING_RATE+1] & 0xFF)) << 8));
			
			// InfoMem D - Start - used by BtStream, SdLog and LogAndStream
			// Sampling Rate
	//		double samplingRate = (32768/(double)((int)(infoMemContents[NV_SAMPLING_RATE] & 0xFF) + ((int)(infoMemContents[NV_SAMPLING_RATE+1] & 0xFF) << 8)));
			mSamplingRate = (32768/(double)((int)(infoMemContents[iM.idxShimmerSamplingRate] & iM.maskShimmerSamplingRate) + ((int)(infoMemContents[iM.idxShimmerSamplingRate+1] & iM.maskShimmerSamplingRate) << 8)));
	
			mBufferSize = (int)(infoMemContents[iM.idxBufferSize] & iM.maskBufferSize);
			
			// Sensors
			mEnabledSensors = ((long)infoMemContents[iM.idxSensors0] & iM.maskSensors) << iM.bitShiftSensors0;
			mEnabledSensors += ((long)infoMemContents[iM.idxSensors1] & iM.maskSensors) << iM.bitShiftSensors1;
			mEnabledSensors += ((long)infoMemContents[iM.idxSensors2] & iM.maskSensors) << iM.bitShiftSensors2;
			
			// Configuration
			mLSM303DigitalAccelRate = (infoMemContents[iM.idxConfigSetupByte0] >> iM.bitShiftLSM303DLHCAccelSamplingRate) & iM.maskLSM303DLHCAccelSamplingRate; 
			mAccelRange = (infoMemContents[iM.idxConfigSetupByte0] >> iM.bitShiftLSM303DLHCAccelRange) & iM.maskLSM303DLHCAccelRange;
			if(((infoMemContents[iM.idxConfigSetupByte0] >> iM.bitShiftLSM303DLHCAccelLPM) & iM.maskLSM303DLHCAccelLPM) == iM.maskLSM303DLHCAccelLPM) {
				mLowPowerAccelWR = true;
			}
			else {
				mLowPowerAccelWR = false;
			}
			if(((infoMemContents[iM.idxConfigSetupByte0] >> iM.bitShiftLSM303DLHCAccelHRM) & iM.maskLSM303DLHCAccelHRM) == iM.maskLSM303DLHCAccelHRM) {
				mHighResAccelWR = true;
			}
			else {
				mHighResAccelWR = false;
			}
			mMPU9150GyroAccelRate = (infoMemContents[iM.idxConfigSetupByte1] >> iM.bitShiftMPU9150AccelGyroSamplingRate) & iM.maskMPU9150AccelGyroSamplingRate;
			isLowPowerGyro(); // check rate to determine if Sensor is in LPM mode
			
			mMagRange = (infoMemContents[iM.idxConfigSetupByte2] >> iM.bitShiftLSM303DLHCMagRange) & iM.maskLSM303DLHCMagRange;
			mLSM303MagRate = (infoMemContents[iM.idxConfigSetupByte2] >> iM.bitShiftLSM303DLHCMagSamplingRate) & iM.maskLSM303DLHCMagSamplingRate;
			isLowPowerMag(); // check rate to determine if Sensor is in LPM mode

			mGyroRange = (infoMemContents[iM.idxConfigSetupByte2] >> iM.bitShiftMPU9150GyroRange) & iM.maskMPU9150GyroRange;
			
			mMPU9150AccelRange = (infoMemContents[iM.idxConfigSetupByte3] >> iM.bitShiftMPU9150AccelRange) & iM.maskMPU9150AccelRange;
			mPressureResolution = (infoMemContents[iM.idxConfigSetupByte3] >> iM.bitShiftBMP180PressureResolution) & iM.maskBMP180PressureResolution;
			mGSRRange = (infoMemContents[iM.idxConfigSetupByte3] >> iM.bitShiftGSRRange) & iM.maskGSRRange;
			mInternalExpPower = (infoMemContents[iM.idxConfigSetupByte3] >> iM.bitShiftEXPPowerEnable) & iM.maskEXPPowerEnable;
			
			//EXG Configuration
			System.arraycopy(infoMemContents, iM.idxEXGADS1292RChip1Config1, mEXG1Register, 0, 10);
			mEXG1RateSetting = (mEXG1Register[iM.idxEXGADS1292RConfig1] >> iM.bitShiftEXGRateSetting) & iM.maskEXGRateSetting;
			mEXG1CH1GainSetting = (mEXG1Register[iM.idxEXGADS1292RCH1Set] >> iM.bitShiftEXGGainSetting) & iM.maskEXGGainSetting;
			mEXG1CH1GainValue = convertEXGGainSettingToValue(mEXG1CH1GainSetting);
			mEXG1CH2GainSetting = (mEXG1Register[iM.idxEXGADS1292RCH2Set] >> iM.bitShiftEXGGainSetting) & iM.maskEXGGainSetting;
			mEXG1CH2GainValue = convertEXGGainSettingToValue(mEXG1CH2GainSetting);
			mRefenceElectrode = mEXG1Register[iM.idxEXGADS1292RRLDSens] & 0x0F;
	
			System.arraycopy(infoMemContents, iM.idxEXGADS1292RChip2Config1, mEXG2Register, 0, 10);
			mEXG2RateSetting = (mEXG2Register[iM.idxEXGADS1292RConfig1] >> iM.bitShiftEXGRateSetting) & iM.maskEXGRateSetting;
			mEXG2CH1GainSetting = (mEXG2Register[iM.idxEXGADS1292RCH1Set] >> iM.bitShiftEXGGainSetting) & iM.maskEXGGainSetting;
			mEXG2CH1GainValue = convertEXGGainSettingToValue(mEXG2CH1GainSetting);
			mEXG2CH2GainSetting = (mEXG2Register[iM.idxEXGADS1292RCH2Set] >> iM.bitShiftEXGGainSetting) & iM.maskEXGGainSetting;
			mEXG2CH2GainValue = convertEXGGainSettingToValue(mEXG2CH2GainSetting);
			
			mBluetoothBaudRate = infoMemContents[iM.idxBtCommBaudRate] & iM.maskBaudRate;
			
			//TODO: hack below -> fix
			if(!(mBluetoothBaudRate>=0 && mBluetoothBaudRate<=10)){
				mBluetoothBaudRate = 0; 
			}
			
			String[] dataType={"i16","i16","i16","i16","i16","i16","i8","i8","i8","i8","i8","i8","i8","i8","i8"};
			// Analog Accel Calibration Parameters
			byte[] bufferCalibrationParameters = new byte[iM.lengthGeneralCalibrationBytes];
			System.arraycopy(infoMemContents, iM.idxAnalogAccelCalibration, bufferCalibrationParameters, 0 , iM.lengthGeneralCalibrationBytes);
			int[] formattedPacket = formatdatapacketreverse(bufferCalibrationParameters,dataType);
			double[] AM=new double[9];
			for (int i=0;i<9;i++)
			{
				AM[i]=((double)formattedPacket[6+i])/100;
			}
			double[][] alignmentMatrixAA = {{AM[0],AM[1],AM[2]},{AM[3],AM[4],AM[5]},{AM[6],AM[7],AM[8]}}; 				
			double[][] sensitivityMatrixAA = {{formattedPacket[3],0,0},{0,formattedPacket[4],0},{0,0,formattedPacket[5]}}; 
			double[][] offsetVectorAA = {{formattedPacket[0]},{formattedPacket[1]},{formattedPacket[2]}};
			mAlignmentMatrixAnalogAccel = alignmentMatrixAA; 			
			mSensitivityMatrixAnalogAccel = sensitivityMatrixAA; 	
			mOffsetVectorAnalogAccel = offsetVectorAA;
			
			// MPU9150 Gyroscope Calibration Parameters
			bufferCalibrationParameters = new byte[iM.lengthGeneralCalibrationBytes];
			System.arraycopy(infoMemContents, iM.idxMPU9150GyroCalibration, bufferCalibrationParameters, 0 , iM.lengthGeneralCalibrationBytes);
			formattedPacket = formatdatapacketreverse(bufferCalibrationParameters,dataType);
			AM=new double[9];
			for (int i=0;i<9;i++)
			{
				AM[i]=((double)formattedPacket[6+i])/100;
			}
			double[][] alignmentMatrixG = {{AM[0],AM[1],AM[2]},{AM[3],AM[4],AM[5]},{AM[6],AM[7],AM[8]}}; 				
			double[][] sensitivityMatrixG = {{formattedPacket[3],0,0},{0,formattedPacket[4],0},{0,0,formattedPacket[5]}}; 
			double[][] offsetVectorG = {{formattedPacket[0]},{formattedPacket[1]},{formattedPacket[2]}};
			mAlignmentMatrixGyroscope = alignmentMatrixG; 			
			mSensitivityMatrixGyroscope = sensitivityMatrixG; 	
			mSensitivityMatrixGyroscope[0][0] = mSensitivityMatrixGyroscope[0][0]/100;
			mSensitivityMatrixGyroscope[1][1] = mSensitivityMatrixGyroscope[1][1]/100;
			mSensitivityMatrixGyroscope[2][2] = mSensitivityMatrixGyroscope[2][2]/100;
			mOffsetVectorGyroscope = offsetVectorG;
	
			// LSM303DLHC Magnetometer Calibration Parameters
			bufferCalibrationParameters = new byte[iM.lengthGeneralCalibrationBytes];
			System.arraycopy(infoMemContents, iM.idxLSM303DLHCMagCalibration, bufferCalibrationParameters, 0 , iM.lengthGeneralCalibrationBytes);
			formattedPacket = formatdatapacketreverse(bufferCalibrationParameters,dataType);
			AM=new double[9];
			for (int i=0;i<9;i++)
			{
				AM[i]=((double)formattedPacket[6+i])/100;
			}
			double[][] alignmentMatrixM = {{AM[0],AM[1],AM[2]},{AM[3],AM[4],AM[5]},{AM[6],AM[7],AM[8]}}; 				
			double[][] sensitivityMatrixM = {{formattedPacket[3],0,0},{0,formattedPacket[4],0},{0,0,formattedPacket[5]}}; 
			double[][] offsetVectorM = {{formattedPacket[0]},{formattedPacket[1]},{formattedPacket[2]}};
			mAlignmentMatrixMagnetometer = alignmentMatrixM; 			
			mSensitivityMatrixMagnetometer = sensitivityMatrixM; 	
			mOffsetVectorMagnetometer = offsetVectorM;
	
			// LSM303DLHC Digital Accel Calibration Parameters
			bufferCalibrationParameters = new byte[iM.lengthGeneralCalibrationBytes];
			System.arraycopy(infoMemContents, iM.idxLSM303DLHCAccelCalibration, bufferCalibrationParameters, 0 , iM.lengthGeneralCalibrationBytes);
			formattedPacket = formatdatapacketreverse(bufferCalibrationParameters,dataType);
			AM=new double[9];
			for (int i=0;i<9;i++)
			{
				AM[i]=((double)formattedPacket[6+i])/100;
			}
			double[][] alignmentMatrixDA = {{AM[0],AM[1],AM[2]},{AM[3],AM[4],AM[5]},{AM[6],AM[7],AM[8]}}; 				
			double[][] sensitivityMatrixDA = {{formattedPacket[3],0,0},{0,formattedPacket[4],0},{0,0,formattedPacket[5]}}; 
			double[][] offsetVectorDA = {{formattedPacket[0]},{formattedPacket[1]},{formattedPacket[2]}};
			mAlignmentMatrixWRAccel = alignmentMatrixDA; 			
			mSensitivityMatrixWRAccel = sensitivityMatrixDA; 	
			mOffsetVectorWRAccel = offsetVectorDA;
			
			//TODO: decide what to do
			// BMP180 Pressure Calibration Parameters
	
			// InfoMem D - End
			
			
			//SDLog and LogAndStream
			if(((mFirmwareIndentifier == FW_ID_SHIMMER3_SDLOG) || (mFirmwareIndentifier == FW_ID_SHIMMER3_LOGANDSTREAM))
					&& infoMemContents.length >= 384) {
				
				// InfoMem C - Start - used by SdLog and LogAndStream
				mMPU9150DMP = (infoMemContents[iM.idxConfigSetupByte4] >> iM.bitShiftMPU9150DMP) & iM.maskMPU9150DMP;
				mMPU9150LPF = (infoMemContents[iM.idxConfigSetupByte4] >> iM.bitShiftMPU9150LPF) & iM.maskMPU9150LPF;
				mMPU9150MotCalCfg =  (infoMemContents[iM.idxConfigSetupByte4] >> iM.bitShiftMPU9150MotCalCfg) & iM.maskMPU9150MotCalCfg;
				
				mMPU9150MPLSamplingRate = (infoMemContents[iM.idxConfigSetupByte5] >> iM.bitShiftMPU9150MPLSamplingRate) & iM.maskMPU9150MPLSamplingRate;
				mMPU9150MagSamplingRate = (infoMemContents[iM.idxConfigSetupByte5] >> iM.bitShiftMPU9150MagSamplingRate) & iM.maskMPU9150MagSamplingRate;
				
				mEnabledSensors += ((long)infoMemContents[iM.idxSensors3] & 0xFF) << iM.bitShiftSensors3;
				mEnabledSensors += ((long)infoMemContents[iM.idxSensors4] & 0xFF) << iM.bitShiftSensors4;
				
				mMPLSensorFusion = (infoMemContents[iM.idxConfigSetupByte6] >> iM.bitShiftMPLSensorFusion) & iM.maskMPLSensorFusion;
				mMPLGyroCalTC = (infoMemContents[iM.idxConfigSetupByte6] >> iM.bitShiftMPLGyroCalTC) & iM.maskMPLGyroCalTC;
				mMPLVectCompCal = (infoMemContents[iM.idxConfigSetupByte6] >> iM.bitShiftMPLVectCompCal) & iM.maskMPLVectCompCal;
				mMPLMagDistCal = (infoMemContents[iM.idxConfigSetupByte6] >> iM.bitShiftMPLMagDistCal) & iM.maskMPLMagDistCal;
				mMPLEnable = (infoMemContents[iM.idxConfigSetupByte6] >> iM.bitShiftMPLEnable) & iM.maskMPLEnable;
				
				//MPL Accel Calibration Parameters
				bufferCalibrationParameters = new byte[iM.lengthGeneralCalibrationBytes];
				System.arraycopy(infoMemContents, iM.idxMPLAccelCalibration, bufferCalibrationParameters, 0 , iM.lengthGeneralCalibrationBytes);
				formattedPacket = formatdatapacketreverse(bufferCalibrationParameters,dataType);
				AM=new double[9];
				for (int i=0;i<9;i++)
				{
					AM[i]=((double)formattedPacket[6+i])/100;
				}
				double[][] alignmentMatrixMPLA = {{AM[0],AM[1],AM[2]},{AM[3],AM[4],AM[5]},{AM[6],AM[7],AM[8]}}; 				
				double[][] sensitivityMatrixMPLA = {{formattedPacket[3],0,0},{0,formattedPacket[4],0},{0,0,formattedPacket[5]}}; 
				double[][] offsetVectorMPLA = {{formattedPacket[0]},{formattedPacket[1]},{formattedPacket[2]}};
				AlignmentMatrixMPLAccel = alignmentMatrixMPLA; 			
				SensitivityMatrixMPLAccel = sensitivityMatrixMPLA; 	
				OffsetVectorMPLAccel = offsetVectorMPLA;
		
				//MPL Mag Calibration Configuration
				bufferCalibrationParameters = new byte[iM.lengthGeneralCalibrationBytes];
				System.arraycopy(infoMemContents, iM.idxMPLMagCalibration, bufferCalibrationParameters, 0 , iM.lengthGeneralCalibrationBytes);
				formattedPacket = formatdatapacketreverse(bufferCalibrationParameters,dataType);
				AM=new double[9];
				for (int i=0;i<9;i++)
				{
					AM[i]=((double)formattedPacket[6+i])/100;
				}
				double[][] alignmentMatrixMPLMag = {{AM[0],AM[1],AM[2]},{AM[3],AM[4],AM[5]},{AM[6],AM[7],AM[8]}}; 				
				double[][] sensitivityMatrixMPLMag = {{formattedPacket[3],0,0},{0,formattedPacket[4],0},{0,0,formattedPacket[5]}}; 
				double[][] offsetVectorMPLMag = {{formattedPacket[0]},{formattedPacket[1]},{formattedPacket[2]}};
				AlignmentMatrixMPLMag = alignmentMatrixMPLMag; 			
				SensitivityMatrixMPLMag = sensitivityMatrixMPLMag; 	
				OffsetVectorMPLMag = offsetVectorMPLMag;
		
				//MPL Gyro Calibration Configuration
				bufferCalibrationParameters = new byte[iM.lengthGeneralCalibrationBytes];
				System.arraycopy(infoMemContents, iM.idxMPLGyroCalibration, bufferCalibrationParameters, 0 , iM.lengthGeneralCalibrationBytes);
				formattedPacket = formatdatapacketreverse(bufferCalibrationParameters,dataType);
				AM=new double[9];
				for (int i=0;i<9;i++)
				{
					AM[i]=((double)formattedPacket[6+i])/100;
				}
				double[][] alignmentMatrixMPLGyro = {{AM[0],AM[1],AM[2]},{AM[3],AM[4],AM[5]},{AM[6],AM[7],AM[8]}}; 				
				double[][] sensitivityMatrixMPLGyro = {{formattedPacket[3],0,0},{0,formattedPacket[4],0},{0,0,formattedPacket[5]}}; 
				double[][] offsetVectorMPLGyro = {{formattedPacket[0]},{formattedPacket[1]},{formattedPacket[2]}};
				AlignmentMatrixMPLGyro = alignmentMatrixMPLGyro; 			
				SensitivityMatrixMPLGyro = sensitivityMatrixMPLGyro; 	
				OffsetVectorMPLGyro = offsetVectorMPLGyro;
				
				// Shimmer Name
				byte[] shimmerNameBuffer = new byte[iM.lengthShimmerName];
				System.arraycopy(infoMemContents, iM.idxSDShimmerName, shimmerNameBuffer, 0 , iM.lengthShimmerName);
				String shimmerName = "";
				for(byte b : shimmerNameBuffer) {
					if(!isAsciiPrintable((char)b)) {
						break;
					}
					shimmerName += (char)b;
				}
				mShimmerUserAssignedName = new String(shimmerName);
				
				// Experiment Name
				byte[] experimentNameBuffer = new byte[iM.lengthExperimentName];
				System.arraycopy(infoMemContents, iM.idxSDEXPIDName, experimentNameBuffer, 0 , iM.lengthExperimentName);
				String experimentName = "";
				for(byte b : experimentNameBuffer) {
					if(!isAsciiPrintable((char)b)) {
						break;
					}
					experimentName += (char)b;
				}
				mExperimentName = new String(experimentName);
	
				//Configuration Time
//				mConfigTime = (((long)(infoMemContents[iM.idxSDConfigTime] & 0xFF))<<24)
//								+((long)(infoMemContents[iM.idxSDConfigTime+1] & 0xFF)<<16)
//								+((long)(infoMemContents[iM.idxSDConfigTime+2] & 0xFF)<<8)
//								+((long)(infoMemContents[iM.idxSDConfigTime+3] & 0xFF));
				
				//Configuration Time
				int bitShift = (iM.lengthConfigTimeBytes-1) * 8;
				mConfigTime = 0;
				for(int x=0; x<iM.lengthConfigTimeBytes; x++ ) {
					mConfigTime += (((long)(infoMemContents[iM.idxSDConfigTime+x] & 0xFF)) << bitShift);
					bitShift -= 8;
				}
				
				mExperimentId = infoMemContents[iM.idxSDMyTrialID] & 0xFF;
				mExperimentNumberOfShimmers = infoMemContents[iM.idxSDNumOfShimmers] & 0xFF;
				
				mButtonStart = (infoMemContents[iM.idxSDExperimentConfig0] >> iM.bitShiftButtonStart) & iM.maskButtonStart;
				mSyncTimeWhenLogging = (infoMemContents[iM.idxSDExperimentConfig0] >> iM.bitShiftTimeSyncWhenLogging) & iM.maskTimeSyncWhenLogging;
				mMasterShimmer = (infoMemContents[iM.idxSDExperimentConfig0] >> iM.bitShiftMasterShimmer) & iM.maskTimeMasterShimmer;
				
				mSingleTouch = (infoMemContents[iM.idxSDExperimentConfig1] >> iM.bitShiftSingleTouch) & iM.maskTimeSingleTouch;
				mTCXO = (infoMemContents[iM.idxSDExperimentConfig1] >> iM.bitShiftTCX0) & iM.maskTimeTCX0;
				
				mSyncBroadcastInterval = (int)(infoMemContents[iM.idxSDBTInterval] & 0xFF);
				
				// Maximum and Estimated Length in minutes
				mExperimentDurationEstimated =  ((int)(infoMemContents[iM.idxEstimatedExpLengthLsb] & 0xFF) + (((int)(infoMemContents[iM.idxEstimatedExpLengthMsb] & 0xFF)) << 8));
				mExperimentDurationMaximum =  ((int)(infoMemContents[iM.idxMaxExpLengthLsb] & 0xFF) + (((int)(infoMemContents[iM.idxMaxExpLengthMsb] & 0xFF)) << 8));
	
				byte[] macIdBytes = new byte[iM.lengthMacIdBytes];
//				System.out.println(iM.idxMacAddress);
				System.arraycopy(infoMemContents, iM.idxMacAddress, macIdBytes, 0 , iM.lengthMacIdBytes);
				mMacIdFromInfoMem = bytesToHex(macIdBytes);
//				System.out.println(mMacIdFromInfoMem);
				
				if(((infoMemContents[iM.idxSDConfigDelayFlag]>>1)&0x01) == 0x01) {
					mConfigFileCreationFlag = true;
				}
				else {
					mConfigFileCreationFlag = false;
				}
				// InfoMem C - End
				
				// InfoMem B Start -> Slave MAC ID for Multi-Shimmer Syncronisation
				syncNodesList.clear();
				for (int i = 0; i < 21; i++) {
					System.arraycopy(infoMemContents, iM.idxNode0 + (i*iM.lengthMacIdBytes), macIdBytes, 0 , iM.lengthMacIdBytes);
					if(Arrays.equals(macIdBytes, new byte[]{-1,-1,-1,-1,-1,-1})) {
						break;
					}
					else {
						syncNodesList.add(bytesToHex(macIdBytes));
					}
				}
				// InfoMem B End
				
			}
			
			createMapOfChannels();
			//Set sensormap channel enable values
			for(Integer key:mShimmerSensorsMap.keySet()) {
				if((mEnabledSensors & mShimmerSensorsMap.get(key).mSensorBitmapIDSDLogHeader) > 0) {
					mShimmerSensorsMap.get(key).mIsEnabled = true;
				}
				else {
					mShimmerSensorsMap.get(key).mIsEnabled = false;
				}
			}
			
			if((mShimmerSensorsMap.get(SENSORMAPKEY_SHIMMER3_EXG1_16BIT).mIsEnabled)||(mShimmerSensorsMap.get(SENSORMAPKEY_SHIMMER3_EXG2_16BIT).mIsEnabled)) {
				mExGResolution = 0;
			}
			else if((mShimmerSensorsMap.get(SENSORMAPKEY_SHIMMER3_EXG1_24BIT).mIsEnabled)||(mShimmerSensorsMap.get(SENSORMAPKEY_SHIMMER3_EXG2_24BIT).mIsEnabled)) {
				mExGResolution = 1;
			}
			
			sensorMapCheckandCorrectSensorDependencies();
			
		}
	}

	protected byte[] infoMemByteArrayGenerate(boolean generateForWritingToShimmer) {
		
		byte[] infoMemBackup = mShimmerInfoMemBytes.clone();
		
		if((mFirmwareIndentifier == FW_ID_SHIMMER3_SDLOG) || (mFirmwareIndentifier == FW_ID_SHIMMER3_LOGANDSTREAM)) {
			mShimmerInfoMemBytes = new byte[384];
//			mShimmerInfoMemBytes = createEmptyInfoMemByteArray(384);
		}
		else if(mFirmwareIndentifier == FW_ID_SHIMMER3_BTSTREAM) {
			mShimmerInfoMemBytes = new byte[128];
//			mShimmerInfoMemBytes = createEmptyInfoMemByteArray(128);
		}
		else {
			mShimmerInfoMemBytes = new byte[512]; 
//			mShimmerInfoMemBytes = createEmptyInfoMemByteArray(512);
		}
		
		// InfoMem defaults to 0xFF on firmware flash
		for(int i =0; i < mShimmerInfoMemBytes.length; i++) {
			mShimmerInfoMemBytes[i] = (byte) 0xFF;
		}
		
		// If not being generated from scratch then copy across exisiting InfoMem contents
		if(!generateForWritingToShimmer) {
			System.arraycopy(infoMemBackup, 0, mShimmerInfoMemBytes, 0, (infoMemBackup.length > mShimmerInfoMemBytes.length) ? mShimmerInfoMemBytes.length:infoMemBackup.length);
		}	
		
		InfoMemLayout infoMemMap = new InfoMemLayout(mFirmwareVersionCode, mFirmwareVersionMajor, mFirmwareVersionMinor, mFirmwareVersionRelease);
		
		// InfoMem D - Start - used by BtStream, SdLog and LogAndStream
		// Sampling Rate
		int samplingRate = (int)(32768 / mSamplingRate);
		mShimmerInfoMemBytes[infoMemMap.idxShimmerSamplingRate] = (byte) (samplingRate & 0xFF); 
		mShimmerInfoMemBytes[infoMemMap.idxShimmerSamplingRate+1] = (byte) ((samplingRate >> 8 ) & 0xFF); 

		mShimmerInfoMemBytes[infoMemMap.idxBufferSize] = (byte) (mBufferSize & 0xFF); 
		
		// Sensors
		mEnabledSensors = (long)0;
		for(Integer key:mShimmerSensorsMap.keySet()) {
			if(mShimmerSensorsMap.get(key).mIsEnabled) {
				mEnabledSensors |= mShimmerSensorsMap.get(key).mSensorBitmapIDSDLogHeader;
			}
		}
		mShimmerInfoMemBytes[infoMemMap.idxSensors0] = (byte) ((mEnabledSensors >> 0) & 0xFF);
		mShimmerInfoMemBytes[infoMemMap.idxSensors1] = (byte) ((mEnabledSensors >> 8) & 0xFF);
		mShimmerInfoMemBytes[infoMemMap.idxSensors2] = (byte) ((mEnabledSensors >> 16) & 0xFF);
		
		// Configuration
		mShimmerInfoMemBytes[infoMemMap.idxConfigSetupByte0] = (byte) ((mLSM303DigitalAccelRate & 0xF) << 4);
		mShimmerInfoMemBytes[infoMemMap.idxConfigSetupByte0] |= (byte) ((mAccelRange & 0x03) << 2);
		if(mLowPowerAccelWR) {
			mShimmerInfoMemBytes[infoMemMap.idxConfigSetupByte0] |= 0x02;
		}
		if(mHighResAccelWR) {
			mShimmerInfoMemBytes[infoMemMap.idxConfigSetupByte0] |= 0x01;
		}

		mShimmerInfoMemBytes[infoMemMap.idxConfigSetupByte1] = (byte) ((mMPU9150GyroAccelRate & 0xFF) << 0);

		mShimmerInfoMemBytes[infoMemMap.idxConfigSetupByte2] = (byte) ((mMagRange & 0x07) << 5);
		mShimmerInfoMemBytes[infoMemMap.idxConfigSetupByte2] |= (byte) ((mLSM303MagRate & 0x07) << 2);
		mShimmerInfoMemBytes[infoMemMap.idxConfigSetupByte2] |= (byte) ((mGyroRange & 0x03) << 0);
		
		mShimmerInfoMemBytes[infoMemMap.idxConfigSetupByte3] = (byte) ((mMPU9150AccelRange & 0x03) << 6);
		mShimmerInfoMemBytes[infoMemMap.idxConfigSetupByte3] |= (byte) ((mPressureResolution & 0x03) << 4);
		mShimmerInfoMemBytes[infoMemMap.idxConfigSetupByte3] |= (byte) ((mGSRRange & 0x07) << 1);
		mShimmerInfoMemBytes[infoMemMap.idxConfigSetupByte3] |= (byte) ((mInternalExpPower & 0x01) << 0);
		
		//EXG Configuration
		System.arraycopy(mEXG1Register, 0, mShimmerInfoMemBytes, infoMemMap.idxEXGADS1292RChip1Config1, 10);
		exgBytesReadFrom(1, mEXG1Register);
		System.arraycopy(mEXG2Register, 0, mShimmerInfoMemBytes, infoMemMap.idxEXGADS1292RChip2Config1, 10);
		exgBytesReadFrom(2, mEXG2Register);
		
		mShimmerInfoMemBytes[infoMemMap.idxBtCommBaudRate] = (byte) ((mBluetoothBaudRate & 0xFF) << 0);

		// Analog Accel Calibration Parameters
		byte[] bufferCalibrationParameters = new byte[21];
		// offsetVector -> buffer offset = 0
		for (int i=0; i<3; i++) {
			bufferCalibrationParameters[0+(i*2)] = (byte) ((((int)mOffsetVectorAnalogAccel[i][0]) >> 0) & 0xFF);
			bufferCalibrationParameters[0+(i*2)+1] = (byte) ((((int)mOffsetVectorAnalogAccel[i][0]) >> 8) & 0xFF);
		}
		// sensitivityMatrix -> buffer offset = 6
		for (int i=0; i<3; i++) {
			bufferCalibrationParameters[6+(i*2)] = (byte) ((((int)mSensitivityMatrixAnalogAccel[i][i]) >> 0) & 0xFF);
			bufferCalibrationParameters[6+(i*2)+1] = (byte) ((((int)mSensitivityMatrixAnalogAccel[i][i]) >> 8) & 0xFF);
		}
		// alignmentMatrix -> buffer offset = 12
		for (int i=0; i<3; i++) {
			bufferCalibrationParameters[12+(i*3)] = (byte) (((int)(mAlignmentMatrixAnalogAccel[i][0]*100)) & 0xFF);
			bufferCalibrationParameters[12+(i*3)+1] = (byte) (((int)(mAlignmentMatrixAnalogAccel[i][1]*100)) & 0xFF);
			bufferCalibrationParameters[12+(i*3)+2] = (byte) (((int)(mAlignmentMatrixAnalogAccel[i][2]*100)) & 0xFF);
		}
		System.arraycopy(bufferCalibrationParameters, 0, mShimmerInfoMemBytes, infoMemMap.idxAnalogAccelCalibration, 21);

		// MPU9150 Gyroscope Calibration Parameters
		bufferCalibrationParameters = new byte[21];
		// offsetVector -> buffer offset = 0
		for (int i=0; i<3; i++) {
			bufferCalibrationParameters[0+(i*2)] = (byte) ((((int)mOffsetVectorGyroscope[i][0]) >> 0) & 0xFF);
			bufferCalibrationParameters[0+(i*2)+1] = (byte) ((((int)mOffsetVectorGyroscope[i][0]) >> 8) & 0xFF);
		}
		// sensitivityMatrix -> buffer offset = 6
		for (int i=0; i<3; i++) {
			bufferCalibrationParameters[6+(i*2)] = (byte) ((((int)mSensitivityMatrixGyroscope[i][i]) >> 0) & 0xFF);
			bufferCalibrationParameters[6+(i*2)+1] = (byte) ((((int)mSensitivityMatrixGyroscope[i][i]) >> 8) & 0xFF);
		}
		// alignmentMatrix -> buffer offset = 12
		for (int i=0; i<3; i++) {
			bufferCalibrationParameters[12+(i*3)] = (byte) (((int)(mAlignmentMatrixAnalogAccel[i][0]*100)) & 0xFF);
			bufferCalibrationParameters[12+(i*3)+1] = (byte) (((int)(mAlignmentMatrixAnalogAccel[i][1]*100)) & 0xFF);
			bufferCalibrationParameters[12+(i*3)+2] = (byte) (((int)(mAlignmentMatrixAnalogAccel[i][2]*100)) & 0xFF);
		}
		System.arraycopy(bufferCalibrationParameters, 0, mShimmerInfoMemBytes, infoMemMap.idxMPU9150GyroCalibration, 21);

		// LSM303DLHC Magnetometer Calibration Parameters
		bufferCalibrationParameters = new byte[21];
		// offsetVector -> buffer offset = 0
		for (int i=0; i<3; i++) {
			bufferCalibrationParameters[0+(i*2)] = (byte) ((((int)mOffsetVectorMagnetometer[i][0]) >> 0) & 0xFF);
			bufferCalibrationParameters[0+(i*2)+1] = (byte) ((((int)mOffsetVectorMagnetometer[i][0]) >> 8) & 0xFF);
		}
		// sensitivityMatrix -> buffer offset = 6
		for (int i=0; i<3; i++) {
			bufferCalibrationParameters[6+(i*2)] = (byte) ((((int)mSensitivityMatrixMagnetometer[i][i]) >> 0) & 0xFF);
			bufferCalibrationParameters[6+(i*2)+1] = (byte) ((((int)mSensitivityMatrixMagnetometer[i][i]) >> 8) & 0xFF);
		}
		// alignmentMatrix -> buffer offset = 12
		for (int i=0; i<3; i++) {
			bufferCalibrationParameters[12+(i*3)] = (byte) (((int)(mAlignmentMatrixMagnetometer[i][0]*100)) & 0xFF);
			bufferCalibrationParameters[12+(i*3)+1] = (byte) (((int)(mAlignmentMatrixMagnetometer[i][1]*100)) & 0xFF);
			bufferCalibrationParameters[12+(i*3)+2] = (byte) (((int)(mAlignmentMatrixMagnetometer[i][2]*100)) & 0xFF);
		}
		System.arraycopy(bufferCalibrationParameters, 0, mShimmerInfoMemBytes, infoMemMap.idxLSM303DLHCMagCalibration, 21);

		// LSM303DLHC Digital Accel Calibration Parameters
		bufferCalibrationParameters = new byte[21];
		// offsetVector -> buffer offset = 0
		for (int i=0; i<3; i++) {
			bufferCalibrationParameters[0+(i*2)] = (byte) ((((int)mOffsetVectorWRAccel[i][0]) >> 0) & 0xFF);
			bufferCalibrationParameters[0+(i*2)+1] = (byte) ((((int)mOffsetVectorWRAccel[i][0]) >> 8) & 0xFF);
		}
		// sensitivityMatrix -> buffer offset = 6
		for (int i=0; i<3; i++) {
			bufferCalibrationParameters[6+(i*2)] = (byte) ((((int)mSensitivityMatrixWRAccel[i][i]) >> 0) & 0xFF);
			bufferCalibrationParameters[6+(i*2)+1] = (byte) ((((int)mSensitivityMatrixWRAccel[i][i]) >> 8) & 0xFF);
		}
		// alignmentMatrix -> buffer offset = 12
		for (int i=0; i<3; i++) {
			bufferCalibrationParameters[12+(i*3)] = (byte) (((int)(mAlignmentMatrixWRAccel[i][0]*100)) & 0xFF);
			bufferCalibrationParameters[12+(i*3)+1] = (byte) (((int)(mAlignmentMatrixWRAccel[i][1]*100)) & 0xFF);
			bufferCalibrationParameters[12+(i*3)+2] = (byte) (((int)(mAlignmentMatrixWRAccel[i][2]*100)) & 0xFF);
		}
		System.arraycopy(bufferCalibrationParameters, 0, mShimmerInfoMemBytes, infoMemMap.idxLSM303DLHCAccelCalibration, 21);
		
		//TODO: decide what to do
		// BMP180 Pressure Calibration Parameters

		// InfoMem D - End
		
		
		//SDLog and LogAndStream
		if((mFirmwareIndentifier == FW_ID_SHIMMER3_SDLOG) || (mFirmwareIndentifier == FW_ID_SHIMMER3_LOGANDSTREAM)) {

			// InfoMem C - Start - used by SdLog and LogAndStream
			mShimmerInfoMemBytes[infoMemMap.idxConfigSetupByte4] = (byte) ((mMPU9150DMP & 0x01) << 7);
			mShimmerInfoMemBytes[infoMemMap.idxConfigSetupByte4] |= (byte) ((mMPU9150LPF & 0x07) << 3);
			mShimmerInfoMemBytes[infoMemMap.idxConfigSetupByte4] |= (byte) ((mMPU9150MotCalCfg & 0x07) << 0);

			mShimmerInfoMemBytes[infoMemMap.idxConfigSetupByte5] = (byte) ((mMPU9150MPLSamplingRate & 0x07) << 5);
			mShimmerInfoMemBytes[infoMemMap.idxConfigSetupByte5] |= (byte) ((mMPU9150MagSamplingRate & 0x07) << 2);

			mShimmerInfoMemBytes[infoMemMap.idxSensors3] = (byte) ((mEnabledSensors >> 24) & 0xFF);
			mShimmerInfoMemBytes[infoMemMap.idxSensors4] = (byte) ((mEnabledSensors >> 32) & 0xFF);

			mShimmerInfoMemBytes[infoMemMap.idxConfigSetupByte6] = (byte) ((mMPLSensorFusion & 0x01) << 7);
			mShimmerInfoMemBytes[infoMemMap.idxConfigSetupByte6] |= (byte) ((mMPLGyroCalTC & 0x01) << 6);
			mShimmerInfoMemBytes[infoMemMap.idxConfigSetupByte6] |= (byte) ((mMPLVectCompCal & 0x01) << 5);
			mShimmerInfoMemBytes[infoMemMap.idxConfigSetupByte6] |= (byte) ((mMPLMagDistCal & 0x01) << 4);
			mShimmerInfoMemBytes[infoMemMap.idxConfigSetupByte6] |= (byte) ((mMPLEnable & 0x01) << 3);
			
			//TODO: decide what to do
			//MPL Accel Calibration Parameters
			//MPL Mag Calibration Configuration
			//MPL Gyro Calibration Configuration

			// Shimmer Name
			for (int i = 0; i < 12; i++) {
				if (i < mShimmerUserAssignedName.length()) {
					mShimmerInfoMemBytes[infoMemMap.idxSDShimmerName + i] = (byte) mShimmerUserAssignedName.charAt(i);
				}
				else {
					mShimmerInfoMemBytes[infoMemMap.idxSDShimmerName + i] = (byte) 0xFF;
				}
			}
			
			// Experiment Name
			for (int i = 0; i < 12; i++) {
				if (i < mExperimentName.length()) {
					mShimmerInfoMemBytes[infoMemMap.idxSDEXPIDName + i] = (byte) mExperimentName.charAt(i);
				}
				else {
					mShimmerInfoMemBytes[infoMemMap.idxSDEXPIDName + i] = (byte) 0xFF;
				}
			}

			//Configuration Time
			mShimmerInfoMemBytes[infoMemMap.idxSDConfigTime] = (byte) ((mConfigTime >> 24) & 0xFF);
			mShimmerInfoMemBytes[infoMemMap.idxSDConfigTime+1] = (byte) ((mConfigTime >> 16) & 0xFF);
			mShimmerInfoMemBytes[infoMemMap.idxSDConfigTime+2] = (byte) ((mConfigTime >> 8) & 0xFF);
			mShimmerInfoMemBytes[infoMemMap.idxSDConfigTime+3] = (byte) ((mConfigTime >> 0) & 0xFF);
			
			mShimmerInfoMemBytes[infoMemMap.idxSDMyTrialID] = (byte) (mExperimentId & 0xFF);

			mShimmerInfoMemBytes[infoMemMap.idxSDNumOfShimmers] = (byte) (mExperimentNumberOfShimmers & 0xFF);
			
			mShimmerInfoMemBytes[infoMemMap.idxSDExperimentConfig0] = (byte) ((mButtonStart & 0x01) << 5);
			mShimmerInfoMemBytes[infoMemMap.idxSDExperimentConfig0] |= (byte) ((mSyncTimeWhenLogging & 0x01) << 2);
			mShimmerInfoMemBytes[infoMemMap.idxSDExperimentConfig0] |= (byte) ((mMasterShimmer & 0x01) << 1);
			
			mShimmerInfoMemBytes[infoMemMap.idxSDExperimentConfig1] = (byte) ((mSingleTouch & 0x01) << 7);
			mShimmerInfoMemBytes[infoMemMap.idxSDExperimentConfig1] |= (byte) ((mTCXO & 0x01) << 4);
			
			mShimmerInfoMemBytes[infoMemMap.idxSDBTInterval] = (byte) (mSyncBroadcastInterval & 0xFF);
			
			// Maximum and Estimated Length in minutes
			mShimmerInfoMemBytes[infoMemMap.idxEstimatedExpLengthLsb] = (byte) ((mExperimentDurationEstimated >> 0) & 0xFF);
			mShimmerInfoMemBytes[infoMemMap.idxEstimatedExpLengthMsb] = (byte) ((mExperimentDurationEstimated >> 8) & 0xFF);
			mShimmerInfoMemBytes[infoMemMap.idxMaxExpLengthLsb] = (byte) ((mExperimentDurationMaximum >> 0) & 0xFF);
			mShimmerInfoMemBytes[infoMemMap.idxMaxExpLengthMsb] = (byte) ((mExperimentDurationMaximum >> 8) & 0xFF);

			byte[] invalidMacId = new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
			if(generateForWritingToShimmer) {
				// MAC address - set to all 0xFF (i.e. invalid MAC) so that Firmware will know to check for MAC from Bluetooth transceiver
				// (already set to 0xFF at start of method but just incase)
				System.arraycopy(invalidMacId, 0, mShimmerInfoMemBytes, infoMemMap.idxMacAddress, 6);
				
				 // Tells the Shimmer to create a new config file on undock/power cycle
				mShimmerInfoMemBytes[infoMemMap.idxSDConfigDelayFlag] = (byte) 0x01;
			}
			// InfoMem C - End
			
			// InfoMem B Start -> Slave MAC ID for Multi-Shimmer Syncronisation
			for (int i = 0; i < 21; i++) { // Limit of 21 nodes
				byte[] macIdArray;
				if(i < syncNodesList.size()) {
					macIdArray = hexStringToByteArray(syncNodesList.get(i));
				}
				else {
					macIdArray = invalidMacId;
				}
				System.arraycopy(macIdArray, 0, mShimmerInfoMemBytes, infoMemMap.idxNode0 + (i*6), 6);
			}
			// InfoMem B End
		}
		
		return mShimmerInfoMemBytes;
	}
	
	public byte[] createEmptyInfoMemByteArray(int size) {
		byte[] newArray = new byte[size];
		for(byte b:newArray) {
			b = (byte)0xFF;
		}
		return newArray;
	}

	public static boolean isAsciiPrintable(char ch) {
	      return ch >= 32 && ch < 127;
	  }

	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
	public static String bytesToHex(byte[] bytes) {
	    char[] hexChars = new char[bytes.length * 2];
	    for ( int j = 0; j < bytes.length; j++ ) {
	        int v = bytes[j] & 0xFF;
	        hexChars[j * 2] = hexArray[v >>> 4];
	        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
	    }
	    return new String(hexChars);
	}
	
	public static byte[] hexStringToByteArray(String s) {
	    int len = s.length();
	    byte[] data = new byte[len / 2];
	    for (int i = 0; i < len; i += 2) {
	        data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
	                             + Character.digit(s.charAt(i+1), 16));
	    }
	    return data;
	}
	
	
	/**
	 * Should only be used after the Shimmer HW and FW version information is set
	 */
	public void createMapOfChannels() {
		//TODO: move bitshift values and masks to dedicated classes for infomem, btstream sensor enable and SD file header mapping (for version control)
		
		mShimmerSensorsMap = new TreeMap<Integer,ChannelDetails>();

		if (mShimmerVersion != -1){
			if (mShimmerVersion == HW_ID_SHIMMER_2R){
				mShimmerSensorsMap.put(SENSORMAPKEY_SHIMMER2R_ACCEL, new ChannelDetails(false, 0x80, 0, "Accelerometer"));
				mShimmerSensorsMap.put(SENSORMAPKEY_SHIMMER2R_GYRO, new ChannelDetails(false, 0x40, 0, "Gyroscope"));
				mShimmerSensorsMap.get(SENSORMAPKEY_SHIMMER2R_GYRO).mSensorMapKeysConflicting = new Integer[]{
					SENSORMAPKEY_SHIMMER2R_ECG,
					SENSORMAPKEY_SHIMMER2R_EMG,
					SENSORMAPKEY_SHIMMER2R_GSR,
					SENSORMAPKEY_SHIMMER2R_BRIDGE_AMP};
				mShimmerSensorsMap.put(SENSORMAPKEY_SHIMMER2R_MAG, new ChannelDetails(false, 0x20, 0, "Magnetometer"));
				mShimmerSensorsMap.get(SENSORMAPKEY_SHIMMER2R_MAG).mSensorMapKeysConflicting = new Integer[]{
					SENSORMAPKEY_SHIMMER2R_ECG,
					SENSORMAPKEY_SHIMMER2R_EMG,
					SENSORMAPKEY_SHIMMER2R_GSR,
					SENSORMAPKEY_SHIMMER2R_BRIDGE_AMP};
				mShimmerSensorsMap.put(SENSORMAPKEY_SHIMMER2R_EMG, new ChannelDetails(false, 0x08, 0, "EMG"));
				mShimmerSensorsMap.get(SENSORMAPKEY_SHIMMER2R_EMG).mSensorMapKeysConflicting = new Integer[]{
					SENSORMAPKEY_SHIMMER2R_GSR,
					SENSORMAPKEY_SHIMMER2R_ECG,
					SENSORMAPKEY_SHIMMER2R_BRIDGE_AMP,
					SENSORMAPKEY_SHIMMER2R_GYRO,
					SENSORMAPKEY_SHIMMER2R_MAG};
				mShimmerSensorsMap.put(SENSORMAPKEY_SHIMMER2R_ECG, new ChannelDetails(false, 0x10, 0, "ECG"));
				mShimmerSensorsMap.get(SENSORMAPKEY_SHIMMER2R_ECG).mSensorMapKeysConflicting = new Integer[]{
					SENSORMAPKEY_SHIMMER2R_GSR,
					SENSORMAPKEY_SHIMMER2R_EMG,
					SENSORMAPKEY_SHIMMER2R_BRIDGE_AMP,
					SENSORMAPKEY_SHIMMER2R_GYRO,
					SENSORMAPKEY_SHIMMER2R_MAG};
				mShimmerSensorsMap.put(SENSORMAPKEY_SHIMMER2R_GSR, new ChannelDetails(false, 0x04, 0, "GSR"));
				mShimmerSensorsMap.get(SENSORMAPKEY_SHIMMER2R_GSR).mSensorMapKeysConflicting = new Integer[]{
					SENSORMAPKEY_SHIMMER2R_ECG,
					SENSORMAPKEY_SHIMMER2R_EMG,
					SENSORMAPKEY_SHIMMER2R_BRIDGE_AMP,
					SENSORMAPKEY_SHIMMER2R_GYRO,
					SENSORMAPKEY_SHIMMER2R_MAG};
				mShimmerSensorsMap.put(SENSORMAPKEY_SHIMMER2R_EXP_BOARD_A7, new ChannelDetails(false, 0x02, 0, "Exp Board A7"));
				mShimmerSensorsMap.get(SENSORMAPKEY_SHIMMER2R_EXP_BOARD_A7).mSensorMapKeysConflicting = new Integer[]{
					SENSORMAPKEY_SHIMMER2R_HEART,
					SENSORMAPKEY_SHIMMER2R_BATT};
				mShimmerSensorsMap.put(SENSORMAPKEY_SHIMMER2R_EXP_BOARD_A0, new ChannelDetails(false, 0x01, 0, "Exp Board A0"));
				mShimmerSensorsMap.get(SENSORMAPKEY_SHIMMER2R_EXP_BOARD_A0).mSensorMapKeysConflicting = new Integer[]{
					SENSORMAPKEY_SHIMMER2R_HEART,
					SENSORMAPKEY_SHIMMER2R_BATT};
				mShimmerSensorsMap.put(SENSORMAPKEY_SHIMMER2R_EXP_BOARD, new ChannelDetails(false, 0x02|0x01, 0, "Exp Board"));
				mShimmerSensorsMap.put(SENSORMAPKEY_SHIMMER2R_BRIDGE_AMP, new ChannelDetails(false, 0x8000, 0, "Bridge Amplifier"));
				mShimmerSensorsMap.get(SENSORMAPKEY_SHIMMER2R_BRIDGE_AMP).mSensorMapKeysConflicting = new Integer[]{
					SENSORMAPKEY_SHIMMER2R_ECG,
					SENSORMAPKEY_SHIMMER2R_EMG,
					SENSORMAPKEY_SHIMMER2R_GSR,
					SENSORMAPKEY_SHIMMER2R_GYRO,
					SENSORMAPKEY_SHIMMER2R_MAG};
				mShimmerSensorsMap.put(SENSORMAPKEY_SHIMMER2R_HEART, new ChannelDetails(false, 0x4000, 0, "Heart Rate"));
				mShimmerSensorsMap.get(SENSORMAPKEY_SHIMMER2R_HEART).mSensorMapKeysConflicting = new Integer[]{
					SENSORMAPKEY_SHIMMER2R_EXP_BOARD_A0,
					SENSORMAPKEY_SHIMMER2R_EXP_BOARD_A7};
				mShimmerSensorsMap.put(SENSORMAPKEY_SHIMMER2R_BATT, new ChannelDetails(false, 0x2000, 0, "Battery Voltage"));
				mShimmerSensorsMap.get(SENSORMAPKEY_SHIMMER2R_BATT).mSensorMapKeysConflicting = new Integer[]{
					SENSORMAPKEY_SHIMMER2R_EXP_BOARD_A0,
					SENSORMAPKEY_SHIMMER2R_EXP_BOARD_A7};
				mShimmerSensorsMap.put(SENSORMAPKEY_SHIMMER2R_EXT_ADC_A15, new ChannelDetails(false, 0x0800, 0, "External ADC A15"));
				mShimmerSensorsMap.put(SENSORMAPKEY_SHIMMER2R_INT_ADC_A1, new ChannelDetails(false, 0x0400, 0, "External ADC A1"));
				mShimmerSensorsMap.put(SENSORMAPKEY_SHIMMER2R_INT_ADC_A12, new ChannelDetails(false, 0x0200, 0, "External ADC A12"));
				mShimmerSensorsMap.put(SENSORMAPKEY_SHIMMER2R_INT_ADC_A13, new ChannelDetails(false, 0x0100, 0, "External ADC A13"));
				mShimmerSensorsMap.put(SENSORMAPKEY_SHIMMER2R_INT_ADC_A14, new ChannelDetails(false, 0x800000, 0, "External ADC A14"));

			} else {
				long streamingByteIndex = 0;		// NV_SENSORS0
				long logHeaderByteIndex = 0;
				mShimmerSensorsMap.put(SENSORMAPKEY_SHIMMER3_A_ACCEL, new ChannelDetails(false, 0x80<<(streamingByteIndex*8), 0x80<<(logHeaderByteIndex*8), Shimmer3Configuration.ACCEL_LN_GUI));
				mShimmerSensorsMap.put(SENSORMAPKEY_SHIMMER3_MPU9150_GYRO, new ChannelDetails(false, 0x40<<(streamingByteIndex*8), 0x40<<(logHeaderByteIndex*8), Shimmer3Configuration.GYRO_GUI));
				mShimmerSensorsMap.put(SENSORMAPKEY_SHIMMER3_LSM303DLHC_MAG, new ChannelDetails(false, 0x20<<(streamingByteIndex*8), 0x20<<(logHeaderByteIndex*8), Shimmer3Configuration.MAG_GUI));
				mShimmerSensorsMap.put(SENSORMAPKEY_SHIMMER3_EXG1_24BIT, new ChannelDetails(false, 0x10<<(streamingByteIndex*8), 0x10<<(logHeaderByteIndex*8), "EXG1 24BIT", true));
				mShimmerSensorsMap.get(SENSORMAPKEY_SHIMMER3_EXG1_24BIT).mSensorMapKeysConflicting = new Integer[]{
					SENSORMAPKEY_SHIMMER3_INT_ADC_A1,
					SENSORMAPKEY_SHIMMER3_INT_ADC_A12,
					SENSORMAPKEY_SHIMMER3_INT_ADC_A13,
					SENSORMAPKEY_SHIMMER3_INT_ADC_A14,
					SENSORMAPKEY_SHIMMER3_GSR,
					SENSORMAPKEY_SHIMMER3_EXG1_16BIT,
					SENSORMAPKEY_SHIMMER3_EXG2_16BIT,
					SENSORMAPKEY_SHIMMER3_RESISTANCE_AMP,
					SENSORMAPKEY_SHIMMER3_BRIDGE_AMP};
				mShimmerSensorsMap.put(SENSORMAPKEY_SHIMMER3_EXG2_24BIT, new ChannelDetails(false, 0x08<<(streamingByteIndex*8), 0x08<<(logHeaderByteIndex*8), "EXG2 24BIT", true));
				mShimmerSensorsMap.get(SENSORMAPKEY_SHIMMER3_EXG2_24BIT).mSensorMapKeysConflicting = new Integer[]{
					SENSORMAPKEY_SHIMMER3_INT_ADC_A1,
					SENSORMAPKEY_SHIMMER3_INT_ADC_A12,
					SENSORMAPKEY_SHIMMER3_INT_ADC_A13,
					SENSORMAPKEY_SHIMMER3_INT_ADC_A14,
					SENSORMAPKEY_SHIMMER3_GSR,
					SENSORMAPKEY_SHIMMER3_EMG,
					SENSORMAPKEY_SHIMMER3_EXG1_16BIT,
					SENSORMAPKEY_SHIMMER3_EXG2_16BIT,
					SENSORMAPKEY_SHIMMER3_RESISTANCE_AMP,
					SENSORMAPKEY_SHIMMER3_BRIDGE_AMP};
				mShimmerSensorsMap.put(SENSORMAPKEY_SHIMMER3_GSR, new ChannelDetails(false, 0x04<<(streamingByteIndex*8), 0x04<<(logHeaderByteIndex*8), Shimmer3Configuration.GSR_GUI, true));
				mShimmerSensorsMap.get(SENSORMAPKEY_SHIMMER3_GSR).mSensorMapKeysConflicting = new Integer[]{
					SENSORMAPKEY_SHIMMER3_INT_ADC_A1,
					SENSORMAPKEY_SHIMMER3_INT_ADC_A14,
					SENSORMAPKEY_SHIMMER3_ECG,
					SENSORMAPKEY_SHIMMER3_EMG,
					SENSORMAPKEY_SHIMMER3_EXG_TEST,
					SENSORMAPKEY_SHIMMER3_EXG1_16BIT,
					SENSORMAPKEY_SHIMMER3_EXG2_16BIT,
					SENSORMAPKEY_SHIMMER3_EXG1_24BIT,
					SENSORMAPKEY_SHIMMER3_EXG2_24BIT,
					SENSORMAPKEY_SHIMMER3_RESISTANCE_AMP,
					SENSORMAPKEY_SHIMMER3_BRIDGE_AMP};
				mShimmerSensorsMap.get(SENSORMAPKEY_SHIMMER3_GSR).mSensorMapKeysRequired = new Integer[]{
					SENSORMAPKEY_SHIMMER3_INT_ADC_A12,
					SENSORMAPKEY_SHIMMER3_INT_ADC_A13};

				mShimmerSensorsMap.put(SENSORMAPKEY_SHIMMER3_EXT_ADC_A7, new ChannelDetails(false, 0x02<<(streamingByteIndex*8), 0x02<<(logHeaderByteIndex*8), Shimmer3Configuration.EXT_EXP_A7_GUI));
				mShimmerSensorsMap.put(SENSORMAPKEY_SHIMMER3_EXT_ADC_A6, new ChannelDetails(false, 0x01<<(streamingByteIndex*8), 0x01<<(logHeaderByteIndex*8), Shimmer3Configuration.EXT_EXP_A6_GUI));

				streamingByteIndex = 1;			// NV_SENSORS1
				logHeaderByteIndex = 1;
				mShimmerSensorsMap.put(SENSORMAPKEY_SHIMMER3_BRIDGE_AMP, new ChannelDetails(false, 0x80<<(streamingByteIndex*8), 0x80<<(logHeaderByteIndex*8), Shimmer3Configuration.BRIDGE_AMPLIFIER_GUI, true));
				mShimmerSensorsMap.get(SENSORMAPKEY_SHIMMER3_BRIDGE_AMP).mSensorMapKeysConflicting = new Integer[]{
					SENSORMAPKEY_SHIMMER3_INT_ADC_A12,
//					SENSORMAPKEY_SHIMMER3_INT_ADC_A13,
//					SENSORMAPKEY_SHIMMER3_INT_ADC_A14,
					SENSORMAPKEY_SHIMMER3_GSR,
					SENSORMAPKEY_SHIMMER3_ECG,
					SENSORMAPKEY_SHIMMER3_EMG,
					SENSORMAPKEY_SHIMMER3_EXG_TEST,
					SENSORMAPKEY_SHIMMER3_EXG1_16BIT,
					SENSORMAPKEY_SHIMMER3_EXG2_16BIT,
					SENSORMAPKEY_SHIMMER3_EXG1_24BIT,
					SENSORMAPKEY_SHIMMER3_EXG2_24BIT};
				mShimmerSensorsMap.get(SENSORMAPKEY_SHIMMER3_BRIDGE_AMP).mSensorMapKeysRequired = new Integer[]{
					SENSORMAPKEY_SHIMMER3_INT_ADC_A13, //Bridge Amplifier High Gain
					SENSORMAPKEY_SHIMMER3_INT_ADC_A14}; //Bridge Amplifier Low Gain

				//shimmerChannels.put(, new ChannelDetails(false, 0x40<<(btStreamByteIndex*8), 0x40<<(sDHeaderByteIndex*8), "")); // unused
				mShimmerSensorsMap.put(SENSORMAPKEY_SHIMMER3_VBATT, new ChannelDetails(false, 0x20<<(streamingByteIndex*8), 0x20<<(logHeaderByteIndex*8), Shimmer3Configuration.BATTERY_GUI));
				mShimmerSensorsMap.put(SENSORMAPKEY_SHIMMER3_LSM303DLHC_ACCEL, new ChannelDetails(false, 0x10<<(streamingByteIndex*8), 0x10<<(logHeaderByteIndex*8), Shimmer3Configuration.ACCEL_WR_GUI));
				mShimmerSensorsMap.put(SENSORMAPKEY_SHIMMER3_EXT_ADC_A15, new ChannelDetails(false, 0x08<<(streamingByteIndex*8), 0x08<<(logHeaderByteIndex*8), Shimmer3Configuration.EXT_EXP_A15_GUI));
				
				mShimmerSensorsMap.put(SENSORMAPKEY_SHIMMER3_RESISTANCE_AMP, new ChannelDetails(false, 0x80<<(streamingByteIndex*8), 0x80<<(logHeaderByteIndex*8), Shimmer3Configuration.RESISTANCE_AMP_GUI,true));
				mShimmerSensorsMap.get(SENSORMAPKEY_SHIMMER3_RESISTANCE_AMP).mSensorMapKeysConflicting = new Integer[]{
//					SENSORMAPKEY_SHIMMER3_INT_ADC_A1,
//					SENSORMAPKEY_SHIMMER3_INT_ADC_A14,
					SENSORMAPKEY_SHIMMER3_INT_ADC_A12,
					SENSORMAPKEY_SHIMMER3_GSR,
					SENSORMAPKEY_SHIMMER3_ECG,
					SENSORMAPKEY_SHIMMER3_EMG,
					SENSORMAPKEY_SHIMMER3_EXG_TEST,
					SENSORMAPKEY_SHIMMER3_EXG1_16BIT,
					SENSORMAPKEY_SHIMMER3_EXG2_16BIT,
					SENSORMAPKEY_SHIMMER3_EXG1_24BIT,
					SENSORMAPKEY_SHIMMER3_EXG2_24BIT};
				mShimmerSensorsMap.get(SENSORMAPKEY_SHIMMER3_RESISTANCE_AMP).mSensorMapKeysRequired = new Integer[]{
					SENSORMAPKEY_SHIMMER3_INT_ADC_A1};
				
				mShimmerSensorsMap.put(SENSORMAPKEY_SHIMMER3_INT_ADC_A1, new ChannelDetails(false, 0x04<<(streamingByteIndex*8), 0x04<<(logHeaderByteIndex*8), Shimmer3Configuration.INT_EXP_A1_GUI));
				mShimmerSensorsMap.get(SENSORMAPKEY_SHIMMER3_INT_ADC_A1).mSensorMapKeysConflicting = new Integer[]{
					SENSORMAPKEY_SHIMMER3_GSR,
					SENSORMAPKEY_SHIMMER3_ECG,
					SENSORMAPKEY_SHIMMER3_EMG,
					SENSORMAPKEY_SHIMMER3_EXG_TEST,
					SENSORMAPKEY_SHIMMER3_EXG1_16BIT,
					SENSORMAPKEY_SHIMMER3_EXG2_16BIT,
					SENSORMAPKEY_SHIMMER3_EXG1_24BIT,
					SENSORMAPKEY_SHIMMER3_EXG2_24BIT
				};

				mShimmerSensorsMap.put(SENSORMAPKEY_SHIMMER3_INT_ADC_A12, new ChannelDetails(false, 0x02<<(streamingByteIndex*8), 0x02<<(logHeaderByteIndex*8), Shimmer3Configuration.INT_EXP_A12_GUI));
				mShimmerSensorsMap.get(SENSORMAPKEY_SHIMMER3_INT_ADC_A12).mSensorMapKeysConflicting = new Integer[]{
					SENSORMAPKEY_SHIMMER3_ECG,
					SENSORMAPKEY_SHIMMER3_EMG,
					SENSORMAPKEY_SHIMMER3_EXG_TEST,
					SENSORMAPKEY_SHIMMER3_EXG1_16BIT,
					SENSORMAPKEY_SHIMMER3_EXG2_16BIT,
					SENSORMAPKEY_SHIMMER3_EXG1_24BIT,
					SENSORMAPKEY_SHIMMER3_EXG2_24BIT,
					SENSORMAPKEY_SHIMMER3_RESISTANCE_AMP,
					SENSORMAPKEY_SHIMMER3_BRIDGE_AMP};

				mShimmerSensorsMap.put(SENSORMAPKEY_SHIMMER3_INT_ADC_A13, new ChannelDetails(false, 0x01<<(streamingByteIndex*8), 0x01<<(logHeaderByteIndex*8), Shimmer3Configuration.INT_EXP_A13_GUI));
				mShimmerSensorsMap.get(SENSORMAPKEY_SHIMMER3_INT_ADC_A13).mSensorMapKeysConflicting = new Integer[]{
					SENSORMAPKEY_SHIMMER3_ECG,
					SENSORMAPKEY_SHIMMER3_EMG,
					SENSORMAPKEY_SHIMMER3_EXG_TEST,
					SENSORMAPKEY_SHIMMER3_EXG1_16BIT,
					SENSORMAPKEY_SHIMMER3_EXG2_16BIT,
					SENSORMAPKEY_SHIMMER3_EXG1_24BIT,
					SENSORMAPKEY_SHIMMER3_EXG2_24BIT,
					SENSORMAPKEY_SHIMMER3_RESISTANCE_AMP,
					SENSORMAPKEY_SHIMMER3_BRIDGE_AMP};
				
				streamingByteIndex = 2;			// NV_SENSORS2
				logHeaderByteIndex = 2;
				mShimmerSensorsMap.put(SENSORMAPKEY_SHIMMER3_INT_ADC_A14, new ChannelDetails(false, 0x80<<(streamingByteIndex*8), 0x80<<(logHeaderByteIndex*8), Shimmer3Configuration.INT_EXP_A14_GUI));
				mShimmerSensorsMap.get(SENSORMAPKEY_SHIMMER3_INT_ADC_A14).mSensorMapKeysConflicting = new Integer[]{
					SENSORMAPKEY_SHIMMER3_GSR,
					SENSORMAPKEY_SHIMMER3_ECG,
					SENSORMAPKEY_SHIMMER3_EMG,
					SENSORMAPKEY_SHIMMER3_EXG_TEST,
					SENSORMAPKEY_SHIMMER3_EXG1_16BIT,
					SENSORMAPKEY_SHIMMER3_EXG2_16BIT,
					SENSORMAPKEY_SHIMMER3_EXG1_24BIT,
					SENSORMAPKEY_SHIMMER3_EXG2_24BIT,
					SENSORMAPKEY_SHIMMER3_RESISTANCE_AMP,
					SENSORMAPKEY_SHIMMER3_BRIDGE_AMP};
				mShimmerSensorsMap.put(SENSORMAPKEY_SHIMMER3_MPU9150_ACCEL, new ChannelDetails(false, 0x40<<(streamingByteIndex*8), 0x40<<(logHeaderByteIndex*8), Shimmer3Configuration.ACCEL_MPU_GUI));
				mShimmerSensorsMap.put(SENSORMAPKEY_SHIMMER3_MPU9150_MAG, new ChannelDetails(false, 0x20<<(streamingByteIndex*8), 0x20<<(logHeaderByteIndex*8), Shimmer3Configuration.MAG_MPU_GUI));
				mShimmerSensorsMap.put(SENSORMAPKEY_SHIMMER3_EXG1_16BIT, new ChannelDetails(false, 0x10<<(streamingByteIndex*8), 0x10<<(logHeaderByteIndex*8), "EXG1 16BIT", true));
				mShimmerSensorsMap.get(SENSORMAPKEY_SHIMMER3_EXG1_16BIT).mSensorMapKeysConflicting = new Integer[]{
					SENSORMAPKEY_SHIMMER3_INT_ADC_A1,
					SENSORMAPKEY_SHIMMER3_INT_ADC_A12,
					SENSORMAPKEY_SHIMMER3_INT_ADC_A13,
					SENSORMAPKEY_SHIMMER3_INT_ADC_A14,
					SENSORMAPKEY_SHIMMER3_GSR,
					SENSORMAPKEY_SHIMMER3_EXG1_24BIT,
					SENSORMAPKEY_SHIMMER3_EXG2_24BIT,
					SENSORMAPKEY_SHIMMER3_RESISTANCE_AMP,
					SENSORMAPKEY_SHIMMER3_BRIDGE_AMP};
				mShimmerSensorsMap.put(SENSORMAPKEY_SHIMMER3_EXG2_16BIT, new ChannelDetails(false, 0x08<<(streamingByteIndex*8), 0x08<<(logHeaderByteIndex*8), "EXG2 16BIT", true));
				mShimmerSensorsMap.get(SENSORMAPKEY_SHIMMER3_EXG2_16BIT).mSensorMapKeysConflicting = new Integer[]{
					SENSORMAPKEY_SHIMMER3_INT_ADC_A1,
					SENSORMAPKEY_SHIMMER3_INT_ADC_A12,
					SENSORMAPKEY_SHIMMER3_INT_ADC_A13,
					SENSORMAPKEY_SHIMMER3_INT_ADC_A14,
					SENSORMAPKEY_SHIMMER3_GSR,
					SENSORMAPKEY_SHIMMER3_EMG,
					SENSORMAPKEY_SHIMMER3_EXG1_24BIT,
					SENSORMAPKEY_SHIMMER3_EXG2_24BIT,
					SENSORMAPKEY_SHIMMER3_RESISTANCE_AMP,
					SENSORMAPKEY_SHIMMER3_BRIDGE_AMP};
				
				mShimmerSensorsMap.put(SENSORMAPKEY_SHIMMER3_BMP180_PRESSURE, new ChannelDetails(false, 0x04<<(streamingByteIndex*8), 0x04<<(logHeaderByteIndex*8), Shimmer3Configuration.PRESS_TEMP_BMP180_GUI));
				mShimmerSensorsMap.put(SENSORMAPKEY_SHIMMER3_MPU9150_TEMP, new ChannelDetails(false, 0x20<<(streamingByteIndex*8), 0x20<<(logHeaderByteIndex*8), Shimmer3Configuration.MPL_TEMPERATURE_GUI));

				//shimmerChannels.put(SENSOR_SHIMMER3_BMP180_TEMPERATURE, new ChannelDetails(false, 0x02<<(btStreamByteIndex*8), 0x02<<(sDHeaderByteIndex*8), "")); // not yet implemented
				//shimmerChannels.put(SENSOR_SHIMMER3_MSP430_TEMPERATURE, new ChannelDetails(false, 0x01<<(btStreamByteIndex*8), 0x01<<(sDHeaderByteIndex*8), "")); // not yet implemented
				//shimmerChannels.put(SENSOR_SHIMMER3_LSM303DLHC_TEMPERATURE, new ChannelDetails(false, 0x01<<(btStreamByteIndex*8), 0x01<<(sDHeaderByteIndex*8), "")); // not yet implemented
				
				streamingByteIndex = 3;			// NV_SENSORS3
				logHeaderByteIndex = 3;
				mShimmerSensorsMap.put(SENSORMAPKEY_SHIMMER3_MPL_QUAT_6DOF, new ChannelDetails(false, (long)0, (long)0x80<<(logHeaderByteIndex*8), Shimmer3Configuration.QUAT_MPL_6DOF_GUI));
				mShimmerSensorsMap.put(SENSORMAPKEY_SHIMMER3_MPL_QUAT_9DOF, new ChannelDetails(false, (long)0, (long)0x40<<(logHeaderByteIndex*8), Shimmer3Configuration.QUAT_MPL_9DOF_GUI));
				mShimmerSensorsMap.put(SENSORMAPKEY_SHIMMER3_MPL_EULER_6DOF, new ChannelDetails(false, (long)0, (long)0x20<<(logHeaderByteIndex*8), Shimmer3Configuration.EULER_ANGLES_6DOF_GUI));
				mShimmerSensorsMap.put(SENSORMAPKEY_SHIMMER3_MPL_EULER_9DOF, new ChannelDetails(false, (long)0, (long)0x10<<(logHeaderByteIndex*8), Shimmer3Configuration.EULER_ANGLES_9DOF_GUI));
				mShimmerSensorsMap.put(SENSORMAPKEY_SHIMMER3_MPL_HEADING, new ChannelDetails(false, (long)0, (long)0x08<<(logHeaderByteIndex*8), Shimmer3Configuration.MPL_HEADING_GUI));
				mShimmerSensorsMap.put(SENSORMAPKEY_SHIMMER3_MPL_PEDOMETER, new ChannelDetails(false, (long)0, (long)0x04<<(logHeaderByteIndex*8), Shimmer3Configuration.MPL_PEDOM_CNT_GUI));
				mShimmerSensorsMap.put(SENSORMAPKEY_SHIMMER3_MPL_TAP, new ChannelDetails(false, (long)0, (long)0x02<<(logHeaderByteIndex*8), Shimmer3Configuration.MPL_TAPDIRANDTAPCNT_GUI));
				mShimmerSensorsMap.put(SENSORMAPKEY_SHIMMER3_MPL_MOTION_ORIENT, new ChannelDetails(false, (long)0, (long)0x01<<(logHeaderByteIndex*8), Shimmer3Configuration.MPL_MOTIONANDORIENT_GUI));

				streamingByteIndex = 4;			// NV_SENSORS4
				logHeaderByteIndex = 4;
				mShimmerSensorsMap.put(SENSORMAPKEY_SHIMMER3_GYRO_MPU_MPL, new ChannelDetails(false, (long)0, (long)0x80<<(logHeaderByteIndex*8), Shimmer3Configuration.GYRO_MPU_MPL_GUI));
				mShimmerSensorsMap.put(SENSORMAPKEY_SHIMMER3_ACCEL_MPU_MPL, new ChannelDetails(false, (long)0, (long)0x40<<(logHeaderByteIndex*8), Shimmer3Configuration.ACCEL_MPU_MPL_GUI));
				mShimmerSensorsMap.put(SENSORMAPKEY_SHIMMER3_MAG_MPU_MPL, new ChannelDetails(false, (long)0, (long)0x20<<(logHeaderByteIndex*8), Shimmer3Configuration.MAG_MPU_MPL_GUI));
				mShimmerSensorsMap.put(SENSORMAPKEY_SHIMMER3_MPL_QUAT_6DOF_RAW, new ChannelDetails(false, (long)0, (long)0x10<<(logHeaderByteIndex*8), Shimmer3Configuration.QUAT_DMP_6DOF_GUI));
				//shimmerChannels.put(, new ChannelDetails(false, 0, 0x08<<(loggingHeaderByteIndex*8), "")); // unused
				//shimmerChannels.put(, new ChannelDetails(false, 0, 0x04<<(loggingHeaderByteIndex*8), "")); // unused
				//shimmerChannels.put(, new ChannelDetails(false, 0, 0x02<<(loggingHeaderByteIndex*8), "")); // unused
				//shimmerChannels.put(, new ChannelDetails(false, 0, 0x01<<(loggingHeaderByteIndex*8), "")); // unused
				
				// Combination Sensors
				long shimmer3Ecg = mShimmerSensorsMap.get(SENSORMAPKEY_SHIMMER3_EXG1_24BIT).mSensorBitmapIDStreaming | mShimmerSensorsMap.get(SENSORMAPKEY_SHIMMER3_EXG2_24BIT).mSensorBitmapIDStreaming;
				mShimmerSensorsMap.put(SENSORMAPKEY_SHIMMER3_ECG, new ChannelDetails(false, shimmer3Ecg, shimmer3Ecg, Shimmer3Configuration.ECG_GUI, true)); // SENSOR_EXG1_24BIT + SENSOR_EXG2_24BIT
				mShimmerSensorsMap.get(SENSORMAPKEY_SHIMMER3_ECG).mSensorMapKeysConflicting = new Integer[]{
					SENSORMAPKEY_SHIMMER3_INT_ADC_A1,
					SENSORMAPKEY_SHIMMER3_INT_ADC_A12,
					SENSORMAPKEY_SHIMMER3_INT_ADC_A13,
					SENSORMAPKEY_SHIMMER3_INT_ADC_A14,
					SENSORMAPKEY_SHIMMER3_GSR,
					SENSORMAPKEY_SHIMMER3_RESISTANCE_AMP,
					SENSORMAPKEY_SHIMMER3_BRIDGE_AMP,
					SENSORMAPKEY_SHIMMER3_EMG,
					SENSORMAPKEY_SHIMMER3_EXG_TEST};

				long shimmer3Emg = mShimmerSensorsMap.get(SENSORMAPKEY_SHIMMER3_EXG1_24BIT).mSensorBitmapIDStreaming;
				mShimmerSensorsMap.put(SENSORMAPKEY_SHIMMER3_EMG, new ChannelDetails(false, shimmer3Emg, shimmer3Emg, Shimmer3Configuration.EMG_GUI, true)); // SENSOR_EXG1_24BIT
				mShimmerSensorsMap.get(SENSORMAPKEY_SHIMMER3_EMG).mSensorMapKeysConflicting = new Integer[]{
					SENSORMAPKEY_SHIMMER3_INT_ADC_A1,
					SENSORMAPKEY_SHIMMER3_INT_ADC_A12,
					SENSORMAPKEY_SHIMMER3_INT_ADC_A13,
					SENSORMAPKEY_SHIMMER3_INT_ADC_A14,
					SENSORMAPKEY_SHIMMER3_GSR,
					SENSORMAPKEY_SHIMMER3_RESISTANCE_AMP,
					SENSORMAPKEY_SHIMMER3_BRIDGE_AMP,
					SENSORMAPKEY_SHIMMER3_EXG2_16BIT,
					SENSORMAPKEY_SHIMMER3_EXG2_24BIT,
					SENSORMAPKEY_SHIMMER3_ECG,
					SENSORMAPKEY_SHIMMER3_EXG_TEST};
				
				mShimmerSensorsMap.put(SENSORMAPKEY_SHIMMER3_EXG_TEST, new ChannelDetails(false, shimmer3Ecg, shimmer3Ecg, Shimmer3Configuration.EXG_TEST_GUI, true)); // SENSOR_EXG1_24BIT
				mShimmerSensorsMap.get(SENSORMAPKEY_SHIMMER3_EXG_TEST).mSensorMapKeysConflicting = new Integer[]{
					SENSORMAPKEY_SHIMMER3_INT_ADC_A1,
					SENSORMAPKEY_SHIMMER3_INT_ADC_A12,
					SENSORMAPKEY_SHIMMER3_INT_ADC_A13,
					SENSORMAPKEY_SHIMMER3_INT_ADC_A14,
					SENSORMAPKEY_SHIMMER3_GSR,
					SENSORMAPKEY_SHIMMER3_RESISTANCE_AMP,
					SENSORMAPKEY_SHIMMER3_BRIDGE_AMP,
					SENSORMAPKEY_SHIMMER3_ECG,
					SENSORMAPKEY_SHIMMER3_EMG};
				
//				long shimmer3AllAdc = mShimmerSensorsMap.get(SENSOR_SHIMMER3_EXT_ADC_A7).mSensorBitmapIDStreaming
//						| mShimmerSensorsMap.get(SENSOR_SHIMMER3_EXT_ADC_A6).mSensorBitmapIDStreaming
//						| mShimmerSensorsMap.get(SENSOR_SHIMMER3_EXT_ADC_A15).mSensorBitmapIDStreaming
//						| mShimmerSensorsMap.get(SENSOR_SHIMMER3_INT_ADC_A1).mSensorBitmapIDStreaming
//						| mShimmerSensorsMap.get(SENSOR_SHIMMER3_INT_ADC_A12).mSensorBitmapIDStreaming
//						| mShimmerSensorsMap.get(SENSOR_SHIMMER3_INT_ADC_A13).mSensorBitmapIDStreaming;
//				mShimmerSensorsMap.put(SENSOR_SHIMMER3_ALL_ADC, new ChannelDetails(false, shimmer3AllAdc, shimmer3AllAdc, Shimmer3Configuration.ADC_ALL)); // SENSOR_ALL_ADC_SHIMMER3

				
			}
			
			// Sensor Options Map
			mShimmerSensorsOptionsMap = new TreeMap<Integer,ChannelOptionDetails>();
			
			//Standard Shimmer3 Options
			mShimmerSensorsOptionsMap.put(Configuration.Shimmer3.SENSOROPTIONSMAPKEY_LSM303DLHC_ACCEL_RANGE, 
					new ChannelOptionDetails(Shimmer3Configuration.GUI_LABEL_CONFIG_LSM303DLHC_ACCEL_RANGE, 
											Configuration.Shimmer3.ListofAccelRange, 
											Configuration.Shimmer3.ListofLSM303DLHCAccelRangeConfigValues, 
											ChannelOptionDetails.COMBOBOX));
			
			mShimmerSensorsOptionsMap.put(Configuration.Shimmer3.SENSOROPTIONSMAPKEY_LSM303DLHC_ACCEL_RATE, 
					new ChannelOptionDetails(Shimmer3Configuration.GUI_LABEL_CONFIG_LSM303DLHC_ACCEL_RATE, 
											Configuration.Shimmer3.ListofLSM303DLHCAccelRate, 
											Configuration.Shimmer3.ListofLSM303DLHCAccelRateConfigValues, 
											ChannelOptionDetails.COMBOBOX));
			
			mShimmerSensorsOptionsMap.put(Configuration.Shimmer3.SENSOROPTIONSMAPKEY_MPU9150_GYRO_RANGE, 
					new ChannelOptionDetails(Shimmer3Configuration.GUI_LABEL_CONFIG_MPU9150_GYRO_RANGE, 
											Configuration.Shimmer3.ListofGyroRange, 
											Configuration.Shimmer3.ListofMPU9150GyroRangeConfigValues, 
											ChannelOptionDetails.COMBOBOX));
			
			mShimmerSensorsOptionsMap.put(Configuration.Shimmer3.SENSOROPTIONSMAPKEY_LSM303DLHC_MAG_RANGE, 
					new ChannelOptionDetails(Shimmer3Configuration.GUI_LABEL_CONFIG_LSM303DLHC_MAG_RANGE, 
											Configuration.Shimmer3.ListofMagRange, 
											Configuration.Shimmer3.ListofMagRangeConfigValues, 
											ChannelOptionDetails.COMBOBOX));
			
			mShimmerSensorsOptionsMap.put(Configuration.Shimmer3.SENSOROPTIONSMAPKEY_LSM303DLHC_MAG_RATE, 
					new ChannelOptionDetails(Shimmer3Configuration.GUI_LABEL_CONFIG_LSM303DLHC_MAG_RATE, 
											Configuration.Shimmer3.ListofLSM303DLHCMagRate, 
											Configuration.Shimmer3.ListofLSM303DLHCMagRateConfigValues, 
											ChannelOptionDetails.COMBOBOX));

			mShimmerSensorsOptionsMap.put(Configuration.Shimmer3.SENSOROPTIONSMAPKEY_PRESSURE_RESOLUTION, 
					new ChannelOptionDetails(Shimmer3Configuration.GUI_LABEL_CONFIG_PRESSURE_RESOLUTION, 
											Configuration.Shimmer3.ListofPressureResolution, 
											Configuration.Shimmer3.ListofPressureResolutionConfigValues, 
											ChannelOptionDetails.COMBOBOX));

			mShimmerSensorsOptionsMap.put(Configuration.Shimmer3.SENSOROPTIONSMAPKEY_GSR_RANGE, 
					new ChannelOptionDetails(Shimmer3Configuration.GUI_LABEL_CONFIG_GSR_RANGE, 
											Configuration.Shimmer3.ListofGSRRange, 
											Configuration.Shimmer3.ListofGSRRangeConfigValues, 
											ChannelOptionDetails.COMBOBOX));
			
			
			mShimmerSensorsOptionsMap.put(Configuration.Shimmer3.SENSOROPTIONSMAPKEY_EXG_GAIN, 
					new ChannelOptionDetails(Shimmer3Configuration.GUI_LABEL_CONFIG_EXG_GAIN, 
											Configuration.Shimmer3.ListOfExGGain, 
											Configuration.Shimmer3.ListOfExGGainConfigValues, 
											ChannelOptionDetails.COMBOBOX));
			
			mShimmerSensorsOptionsMap.put(Configuration.Shimmer3.SENSOROPTIONSMAPKEY_EXG_RESOLUTION, 
					new ChannelOptionDetails(Shimmer3Configuration.GUI_LABEL_CONFIG_EXG_RESOLUTION, 
											Configuration.Shimmer3.ListOfExGResolutions, 
											Configuration.Shimmer3.ListOfExGResolutionsConfigValues, 
											ChannelOptionDetails.COMBOBOX));

			//Advanced ExG		
			mShimmerSensorsOptionsMap.put(Configuration.Shimmer3.SENSOROPTIONSMAPKEY_EXG_ECG_REFERENCE_ELECTRODE, 
					new ChannelOptionDetails(Shimmer3Configuration.GUI_LABEL_CONFIG_EXG_REFERENCE_ELECTRODE, 
											Configuration.Shimmer3.ListOfECGReferenceElectrode, 
											Configuration.Shimmer3.ListOfECGReferenceElectrodeConfigValues, 
											ChannelOptionDetails.COMBOBOX));
			
//			mShimmerSensorsOptionsMap.put(Configuration.Shimmer3.SENSOROPTIONSMAPKEY_EXG_EMG_REFERENCE_ELECTRODE, 
//					new ChannelOptionDetails(Shimmer3Configuration.GUI_LABEL_CONFIG_EXG_REFERENCE_ELECTRODE, 
//											Configuration.Shimmer3.ListOfEMGReferenceElectrode, 
//											Configuration.Shimmer3.ListOfEMGReferenceElectrodeConfigValues, 
//											ChannelOptionDetails.COMBOBOX));
			
			mShimmerSensorsOptionsMap.put(Configuration.Shimmer3.SENSOROPTIONSMAPKEY_EXG_LEAD_OFF_DETECTION, 
					new ChannelOptionDetails(Shimmer3Configuration.GUI_LABEL_CONFIG_EXG_LEAD_OFF_DETECTION, 
											Configuration.Shimmer3.ListOfExGLeadOffDetection, 
											Configuration.Shimmer3.ListOfExGLeadOffDetectionConfigValues, 
											ChannelOptionDetails.COMBOBOX));
			
			mShimmerSensorsOptionsMap.put(Configuration.Shimmer3.SENSOROPTIONSMAPKEY_EXG_LEAD_OFF_CURRENT, 
					new ChannelOptionDetails(Shimmer3Configuration.GUI_LABEL_CONFIG_EXG_LEAD_OFF_CURRENT, 
											Configuration.Shimmer3.ListOfExGLeadOffCurrent, 
											Configuration.Shimmer3.ListOfExGLeadOffCurrentConfigValues, 
											ChannelOptionDetails.COMBOBOX));
			
			mShimmerSensorsOptionsMap.put(Configuration.Shimmer3.SENSOROPTIONSMAPKEY_EXG_LEAD_OFF_COMPARATOR, 
					new ChannelOptionDetails(Shimmer3Configuration.GUI_LABEL_CONFIG_EXG_LEAD_OFF_COMPARATOR, 
											Configuration.Shimmer3.ListOfExGLeadOffComparator, 
											Configuration.Shimmer3.ListOfExGLeadOffComparatorConfigValues, 
											ChannelOptionDetails.COMBOBOX));
			
			mShimmerSensorsOptionsMap.put(Configuration.Shimmer3.SENSOROPTIONSMAPKEY_EXG_RESPIRATION_DETECT_FREQ, 
					new ChannelOptionDetails(Shimmer3Configuration.GUI_LABEL_CONFIG_EXG_RESPIRATION_DETECT_FREQ, 
											Configuration.Shimmer3.ListOfExGRespirationDetectFreq, 
											Configuration.Shimmer3.ListOfExGRespirationDetectFreqConfigValues, 
											ChannelOptionDetails.COMBOBOX));
			
			mShimmerSensorsOptionsMap.put(Configuration.Shimmer3.SENSOROPTIONSMAPKEY_EXG_RESPIRATION_DETECT_PHASE, 
					new ChannelOptionDetails(Shimmer3Configuration.GUI_LABEL_CONFIG_EXG_RESPIRATION_DETECT_PHASE, 
											Configuration.Shimmer3.ListOfExGRespirationDetectPhase32khz, 
											Configuration.Shimmer3.ListOfExGRespirationDetectPhase32khzConfigValues, 
											ChannelOptionDetails.COMBOBOX));
			
			//MPL Options
			mShimmerSensorsOptionsMap.put(Configuration.Shimmer3.SENSOROPTIONSMAPKEY_MPU9150_ACCEL_RANGE, 
					new ChannelOptionDetails(Shimmer3Configuration.GUI_LABEL_CONFIG_MPU9150_ACCEL_RANGE, 
											Configuration.Shimmer3.ListofMPU9150AccelRange, 
											Configuration.Shimmer3.ListofMPU9150AccelRangeConfigValues, 
											ChannelOptionDetails.COMBOBOX));
			
			mShimmerSensorsOptionsMap.put(Configuration.Shimmer3.SENSOROPTIONSMAPKEY_MPU9150_GYRO_CAL, 
					new ChannelOptionDetails(Shimmer3Configuration.GUI_LABEL_CONFIG_MPU9150_GYRO_CAL, 
											Configuration.Shimmer3.ListofMPU9150MplCalibrationOptions, 
											Configuration.Shimmer3.ListofMPU9150MplCalibrationOptionsConfigValues, 
											ChannelOptionDetails.COMBOBOX));

			mShimmerSensorsOptionsMap.put(Configuration.Shimmer3.SENSOROPTIONSMAPKEY_MPU9150_LPF, 
					new ChannelOptionDetails(Shimmer3Configuration.GUI_LABEL_CONFIG_MPU9150_LPF, 
											Configuration.Shimmer3.ListofMPU9150MplLpfOptions, 
											Configuration.Shimmer3.ListofMPU9150MplLpfOptionsConfigValues, 
											ChannelOptionDetails.COMBOBOX));

			mShimmerSensorsOptionsMap.put(Configuration.Shimmer3.SENSOROPTIONSMAPKEY_MPU9150_MPL_RATE, 
					new ChannelOptionDetails(Shimmer3Configuration.GUI_LABEL_CONFIG_MPL_RATE, 
											Configuration.Shimmer3.ListofMPU9150MplRate, 
											Configuration.Shimmer3.ListofMPU9150MplSamplingRatesConfigValues, 
											ChannelOptionDetails.COMBOBOX));

			mShimmerSensorsOptionsMap.put(Configuration.Shimmer3.SENSOROPTIONSMAPKEY_MPU9150_MAG_RATE, 
					new ChannelOptionDetails(Shimmer3Configuration.GUI_LABEL_CONFIG_MPU9150_MAG_RATE, 
											Configuration.Shimmer3.ListofMPU9150MagRate, 
											Configuration.Shimmer3.ListofMPU9150MagSamplingRatesConfigValues, 
											ChannelOptionDetails.COMBOBOX));


		}
	}
	
	public boolean setSensorEnabledState(int key, boolean state) {
		if(mShimmerSensorsMap!=null) {
			
			if (mShimmerVersion == HW_ID_SHIMMER_3){
				
				// Automatically handle required channels for each sensor
				if(mShimmerSensorsMap.get(key).mSensorMapKeysRequired != null) {
					for(Integer i:mShimmerSensorsMap.get(key).mSensorMapKeysRequired) {
						mShimmerSensorsMap.get(i).mIsEnabled = state;
					}
				}
				
				// Unique cases for Shimmer3 ExG
				if((key == SENSORMAPKEY_SHIMMER3_EXG1_16BIT) || (key == SENSORMAPKEY_SHIMMER3_EXG2_16BIT)) {
					mExGResolution = 0;
				}
				else if((key == SENSORMAPKEY_SHIMMER3_EXG1_24BIT) || (key == SENSORMAPKEY_SHIMMER3_EXG2_24BIT)) {
					mExGResolution = 1;
				}
				
				if((key == SENSORMAPKEY_SHIMMER3_ECG) || (key == SENSORMAPKEY_SHIMMER3_EMG) || (key == SENSORMAPKEY_SHIMMER3_EXG_TEST)) {
					if(mExGResolution == 0) {// 16-bit
						if((key == SENSORMAPKEY_SHIMMER3_ECG)||(key == SENSORMAPKEY_SHIMMER3_EXG_TEST)) {
							mShimmerSensorsMap.get(SENSORMAPKEY_SHIMMER3_EXG1_16BIT).mIsEnabled = state;
							mShimmerSensorsMap.get(SENSORMAPKEY_SHIMMER3_EXG2_16BIT).mIsEnabled = state;
						}
						else if(key == SENSORMAPKEY_SHIMMER3_EMG) {
							mShimmerSensorsMap.get(SENSORMAPKEY_SHIMMER3_EXG1_16BIT).mIsEnabled = state;
						}
					}
					else { // 24-bit
						if((key == SENSORMAPKEY_SHIMMER3_ECG)||(key == SENSORMAPKEY_SHIMMER3_EXG_TEST)) {
							mShimmerSensorsMap.get(SENSORMAPKEY_SHIMMER3_EXG1_24BIT).mIsEnabled = state;
							mShimmerSensorsMap.get(SENSORMAPKEY_SHIMMER3_EXG2_24BIT).mIsEnabled = state;
						}
						else if(key == SENSORMAPKEY_SHIMMER3_EMG) {
							mShimmerSensorsMap.get(SENSORMAPKEY_SHIMMER3_EXG1_24BIT).mIsEnabled = state;
						}
					}
				}
				

			}
			
			mShimmerSensorsMap.get(key).mIsEnabled = state;
			sensorMapConflictCheckandCorrect(key);
			
			//TODO handle ECG, EMG, ExG Test being turned off
			
			if (mShimmerVersion == HW_ID_SHIMMER_3){
				// Automatically control internal expansion board power
				for(Integer i:mShimmerSensorsMap.keySet()) {
					if(mShimmerSensorsMap.get(i).mIsEnabled && mShimmerSensorsMap.get(i).mIntExpBoardPowerRequired) {
						mInternalExpPower = 1;
						break;
					}
					else {
						mInternalExpPower = 0;
					}
				}
			}
			
		}
		else {
			return false;
		}
		
		if(mShimmerSensorsMap.get(key).mIsEnabled == state) {
			return true;
		}
		else {
			return false;
		}
	}
	
	
	/**
	 * @param key This takes in a single sensor map key to check for conflicts and correct
	 * @return enabledSensors This returns the new set of enabled sensors, where any sensors which conflicts with sensorToCheck is disabled on the bitmap, so sensorToCheck can be accomodated (e.g. for Shimmer2 using ECG will disable EMG,GSR,..basically any daughter board)
	 * @return boolean 
	 *  
	 */
	public void sensorMapConflictCheckandCorrect(int key){
		if(mShimmerSensorsMap.get(key) != null) {
			if(mShimmerSensorsMap.get(key).mSensorMapKeysConflicting != null) {
				for(Integer i:mShimmerSensorsMap.get(key).mSensorMapKeysConflicting) {
					if(mShimmerSensorsMap.get(i) != null) {
						mShimmerSensorsMap.get(i).mIsEnabled = false;
					}
				}
			}
		}
		sensorMapCheckandCorrectSensorDependencies();
	}

	public List<Integer> sensorMapConflictCheck(Integer key){
		List<Integer> listOfSensorConflicts = new ArrayList<Integer>();
		
//		boolean pass=true;
		
		if (mShimmerVersion != HW_ID_SHIMMER_3){
			//TODO: handle Shimmer2/r exceptions which involve get5VReg(), getPMux() and writePMux()
			
//			if (mShimmerSensorsMap.get(SENSOR_GYRO).mIsEnabled == true){
//				if (mShimmerSensorsMap.get(SENSOR_EMG).mIsEnabled == true){
//					pass=false;
//				} else if (mShimmerSensorsMap.get(SENSOR_ECG).mIsEnabled == true){
//					pass=false;
//				} else if (mShimmerSensorsMap.get(SENSOR_GSR).mIsEnabled == true){
//					pass=false;
//				} else if (mShimmerSensorsMap.get(SENSOR_BRIDGE_AMP).mIsEnabled == true){
//					pass=false;
//				}
//			}
//
//			if (mShimmerSensorsMap.get(SENSOR_MAG).mIsEnabled == true){
//				if (mShimmerSensorsMap.get(SENSOR_EMG).mIsEnabled == true){
//					pass=false;
//				} else if (mShimmerSensorsMap.get(SENSOR_ECG).mIsEnabled == true){
//					pass=false;
//				} else if (mShimmerSensorsMap.get(SENSOR_GSR).mIsEnabled == true){
//					pass=false;
//				} else if (mShimmerSensorsMap.get(SENSOR_BRIDGE_AMP).mIsEnabled == true){
//					pass=false;
//				}
//			}
//
//			if (mShimmerSensorsMap.get(SENSOR_EMG).mIsEnabled == true){
//				if (mShimmerSensorsMap.get(SENSOR_GYRO).mIsEnabled == true){
//					pass=false;
//				} else if (mShimmerSensorsMap.get(SENSOR_MAG).mIsEnabled == true){
//					pass=false;
//				} else if (mShimmerSensorsMap.get(SENSOR_ECG).mIsEnabled == true){
//					pass=false;
//				} else if (mShimmerSensorsMap.get(SENSOR_GSR).mIsEnabled == true){
//					pass=false;
//				} else if (mShimmerSensorsMap.get(SENSOR_BRIDGE_AMP).mIsEnabled == true){
//					pass=false;
//				}
//			}
//
//			if (mShimmerSensorsMap.get(SENSOR_ECG).mIsEnabled == true){
//				if (mShimmerSensorsMap.get(SENSOR_GYRO).mIsEnabled == true){
//					pass=false;
//				} else if (mShimmerSensorsMap.get(SENSOR_MAG).mIsEnabled == true){
//					pass=false;
//				} else if (mShimmerSensorsMap.get(SENSOR_EMG).mIsEnabled == true){
//					pass=false;
//				} else if (mShimmerSensorsMap.get(SENSOR_GSR).mIsEnabled == true){
//					pass=false;
//				} else if (mShimmerSensorsMap.get(SENSOR_BRIDGE_AMP).mIsEnabled == true){
//					pass=false;
//				}
//			}
//
//			if (mShimmerSensorsMap.get(SENSOR_GSR).mIsEnabled == true){
//				if (mShimmerSensorsMap.get(SENSOR_GYRO).mIsEnabled == true){
//					pass=false;
//				} else if (mShimmerSensorsMap.get(SENSOR_MAG).mIsEnabled == true){
//					pass=false;
//				} else if (mShimmerSensorsMap.get(SENSOR_EMG).mIsEnabled == true){
//					pass=false;
//				} else if (mShimmerSensorsMap.get(SENSOR_ECG).mIsEnabled == true){
//					pass=false;
//				} else if (mShimmerSensorsMap.get(SENSOR_BRIDGE_AMP).mIsEnabled == true){
//					pass=false;
//				}
//			}
//
//			if (mShimmerSensorsMap.get(SENSOR_BRIDGE_AMP).mIsEnabled == true){
//				if (mShimmerSensorsMap.get(SENSOR_GYRO).mIsEnabled == true){
//					pass=false;
//				} else if (mShimmerSensorsMap.get(SENSOR_MAG).mIsEnabled == true){
//					pass=false;
//				} else if (mShimmerSensorsMap.get(SENSOR_EMG).mIsEnabled == true){
//					pass=false;
//				} else if (mShimmerSensorsMap.get(SENSOR_ECG).mIsEnabled == true){
//					pass=false;
//				} else if (mShimmerSensorsMap.get(SENSOR_GSR).mIsEnabled == true){
//					pass=false;
//				} else if (get5VReg()==1){ // if the 5volt reg is set 
//					pass=false;
//				}
//			}
//
//			if (mShimmerSensorsMap.get(SENSOR_EXP_BOARD_A0).mIsEnabled == true){
//				if (mShimmerSensorsMap.get(SENSOR_BATT).mIsEnabled == true){
//					pass=false;
//				} else if (getPMux()==1){
//					writePMux(0);
//				}
//			}
//
//			if (mShimmerSensorsMap.get(SENSOR_EXP_BOARD_A7).mIsEnabled == true){
//				if (mShimmerSensorsMap.get(SENSOR_BATT).mIsEnabled == true){
//					pass=false;
//				}else if (getPMux()==1){
//					writePMux(0);
//				}
//			}
//
//			if (mShimmerSensorsMap.get(SENSOR_BATT).mIsEnabled == true){
//				if (mShimmerSensorsMap.get(SENSOR_EXP_BOARD_A7).mIsEnabled == true){
//					pass=false;
//				} 
//				if (mShimmerSensorsMap.get(SENSOR_EXP_BOARD_A0).mIsEnabled == true){
//					pass=false;
//				}
//				if (mShimmerSensorsMap.get(SENSOR_BATT).mIsEnabled == true){
//					if (getPMux()==0){
//						writePMux(1);
//					}
//				}
//			}
//			if (!pass){
//				
//			}
		}
		
		else{ // Shimmer3
			if(mShimmerSensorsMap.get(key).mSensorMapKeysConflicting != null) {
				for(Integer i:mShimmerSensorsMap.get(key).mSensorMapKeysConflicting) {
					if(mShimmerSensorsMap.get(i) != null) {
						if(mShimmerSensorsMap.get(i).mIsEnabled == true) {
							listOfSensorConflicts.add(i);
						}
					}
				}
			}
		}
		
		if(listOfSensorConflicts.isEmpty()) {
			return null;
		}
		else {
			return listOfSensorConflicts;
		}
	}

	
	private void sensorMapCheckandCorrectSensorDependencies() {
		//Cycle through any required sensors and update sensormap channel enable values
		for(Integer sensorMapKey:mShimmerSensorsMap.keySet()) {
			if(mShimmerSensorsMap.get(sensorMapKey).mSensorMapKeysRequired != null) {
				for(Integer requiredSensorKey:mShimmerSensorsMap.get(sensorMapKey).mSensorMapKeysRequired) {
					if(mShimmerSensorsMap.get(requiredSensorKey).mIsEnabled == false) {
						mShimmerSensorsMap.get(sensorMapKey).mIsEnabled = false;
						break;
					}
				}
			}
			
			if (mShimmerVersion == HW_ID_SHIMMER_3){
				//Exceptions for Shimmer3 ExG
				
				//TODO: Check if default ECG/EMG/ExG Test
				//TODO: Distinguish between ECG/EMG/ExG Test
				
				if(((sensorMapKey==SENSORMAPKEY_SHIMMER3_ECG)||(sensorMapKey==SENSORMAPKEY_SHIMMER3_EXG_TEST))&&(mShimmerSensorsMap.get(sensorMapKey).mIsEnabled)) {
					if(!(((mShimmerSensorsMap.get(SENSORMAPKEY_SHIMMER3_EXG1_16BIT).mIsEnabled)&&(mShimmerSensorsMap.get(SENSORMAPKEY_SHIMMER3_EXG2_16BIT).mIsEnabled))
							||((mShimmerSensorsMap.get(SENSORMAPKEY_SHIMMER3_EXG1_24BIT).mIsEnabled)&&(mShimmerSensorsMap.get(SENSORMAPKEY_SHIMMER3_EXG2_24BIT).mIsEnabled)))){
						mShimmerSensorsMap.get(sensorMapKey).mIsEnabled = false;
					}
					else {
						
					}
				}
				else if((sensorMapKey==SENSORMAPKEY_SHIMMER3_EMG)&&(mShimmerSensorsMap.get(sensorMapKey).mIsEnabled == true)) {
					if(!((mShimmerSensorsMap.get(SENSORMAPKEY_SHIMMER3_EXG1_16BIT).mIsEnabled)||(mShimmerSensorsMap.get(SENSORMAPKEY_SHIMMER3_EXG1_24BIT).mIsEnabled))){
						mShimmerSensorsMap.get(sensorMapKey).mIsEnabled = false;
					}
				}	
			}

		}
	}
	
	
	 /**
	 * @return the mConfigTime
	 */
	public long getConfigTime() {
		return mConfigTime;
	}

	/**
	 * @return the mBufferSize
	 */
	public int getBufferSize() {
		return mBufferSize;
	}
	
	/**
	 * @return the mMPU9150AccelRange
	 */
	public int getMPU9150AccelRange() {
		return mMPU9150AccelRange;
	}

	/**
	 * @return the mMPU9150GyroAccelRate
	 */
	public int getMPU9150GyroAccelRate() {
		return mMPU9150GyroAccelRate;
	}

	/**
	 * @return the mMPU9150MotCalCfg
	 */
	public int getMPU9150MotCalCfg() {
		return mMPU9150MotCalCfg;
	}
	/**
	 * @return the mMPU9150LPF
	 */
	public int getMPU9150LPF() {
		return mMPU9150LPF;
	}

	/**
	 * @return the mMPU9150MPLSamplingRate
	 */
	public int getMPU9150MPLSamplingRate() {
		return mMPU9150MPLSamplingRate;
	}

	/**
	 * @return the mMPU9150MagSamplingRate
	 */
	public int getMPU9150MagSamplingRate() {
		return mMPU9150MagSamplingRate;
	}

	/**
	 * @return the mMasterShimmer
	 */
	public int getMasterShimmer() {
		return mMasterShimmer;
	}

	/**
	 * @return the mSingleTouch
	 */
	public int getSingleTouch() {
		return mSingleTouch;
	}

	/**
	 * @return the mTCXO
	 */
	public int getTCXO() {
		return mTCXO;
	}

	/**
	 * @return the mExperimentName
	 */
	public String getExperimentName() {
		return mExperimentName;
	}

	/**
	 * @return the mExperimentNumberOfShimmers
	 */
	public int getExperimentNumberOfShimmers() {
		return mExperimentNumberOfShimmers;
	}
	
	/**
	 * @return the mExperimentDurationEstimated
	 */
	public int getExperimentDurationEstimated() {
		return mExperimentDurationEstimated;
	}

	/**
	 * @return the mExperimentDurationMaximum
	 */
	public int getExperimentDurationMaximum() {
		return mExperimentDurationMaximum;
	}

	/**
	 * @return the mLSM303DigitalAccelLPM
	 */
	public boolean isLSM303DigitalAccelLPM() {
		return mLowPowerAccelWR;
	}

	/**
	 * @return the mLSM303DigitalAccelHRM
	 */
	public boolean isLSM303DigitalAccelHRM() {
		return mHighResAccelWR;
	}
	
//	/**
//	 * @return the mLowPowerMag
//	 */
//	public boolean ismLowPowerMag() {
//		return mLowPowerMag;
//	}
//
//	/**
//	 * @return the mLowPowerGyro
//	 */
//	public boolean ismLowPowerGyro() {
//		return mLowPowerGyro;
//	}

	/**
	 * @return the mShimmerUserAssignedName
	 */
	public String getShimmerUserAssignedName() {
		return mShimmerUserAssignedName;
	}

	/**
	 * @return the mLSM303MagRate
	 */
	public int getmLSM303MagRate() {
		return mLSM303MagRate;
	}

	/**
	 * @return the mLSM303DigitalAccelRate
	 */
	public int getmLSM303DigitalAccelRate() {
		return mLSM303DigitalAccelRate;
	}

	/**
	 * @return the mMacIdFromInfoMem
	 */
	public String getMacIdFromInfoMem() {
		return mMacIdFromInfoMem;
	}

	/**
	 * @return the mExperimentId
	 */
	public int getExperimentId() {
		return mExperimentId;
	}

	/**
	 * @return the syncNodesList
	 */
	public List<String> getSyncNodesList() {
		return syncNodesList;
	}


	/**
	 * @return the mSyncBroadcastInterval
	 */
	public int getSyncBroadcastInterval() {
		return mSyncBroadcastInterval;
	}


	/**
	 * @return the mSyncTimeWhenLogging
	 */
	public int getSyncTimeWhenLogging() {
		return mSyncTimeWhenLogging;
	}


	/**
	 * @return the mBluetoothBaudRate
	 */
	public int getBluetoothBaudRate() {
		return mBluetoothBaudRate;
	}


	/**
	 * @return the mExGResolution
	 */
	public int getExGResolution() {
		return mExGResolution;
	}




}
