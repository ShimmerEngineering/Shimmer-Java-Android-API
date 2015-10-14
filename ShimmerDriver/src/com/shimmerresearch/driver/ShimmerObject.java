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

//v0.1.0 consensys

package com.shimmerresearch.driver;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.vecmath.Quat4d;
import javax.vecmath.Vector3d;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.shimmerresearch.algorithms.AlgorithmDetailsNew;
import com.shimmerresearch.algorithms.GradDes3DOrientation;
import com.shimmerresearch.driver.ChannelDetails.CHANNEL_TYPE;
import com.shimmerresearch.driver.Configuration.CHANNEL_UNITS;
import com.shimmerresearch.driver.Configuration.Shimmer2;
import com.shimmerresearch.driver.ShimmerVerDetails.FW_ID;
import com.shimmerresearch.driver.ShimmerVerDetails.HW_ID;
import com.shimmerresearch.driver.SensorDetails;
import com.shimmerresearch.driver.Configuration.Shimmer3;
import com.shimmerresearch.driver.ShimmerVerDetails.HW_ID_SR_CODES;
import com.shimmerresearch.exgConfig.ExGConfigBytesDetails;
import com.shimmerresearch.exgConfig.ExGConfigBytesDetails.EXG_SETTINGS;
import com.shimmerresearch.exgConfig.ExGConfigOption;
import com.shimmerresearch.exgConfig.ExGConfigBytesDetails.EXG_SETTING_OPTIONS;
import com.shimmerresearch.exgConfig.ExGConfigOptionDetails.EXG_CHIP_INDEX;
import com.shimmerresearch.sensor.AbstractSensor;
import com.shimmerresearch.algorithms.AlgorithmDetailsNew.SENSOR_CHECK_METHOD;
import com.shimmerresearch.algorithms.GradDes3DOrientation.Quaternion;

public abstract class ShimmerObject extends ShimmerDevice implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1364568867018921219L;
	
	public final static String DEFAULT_SHIMMER_NAME = "Shimmer";
	public final static String DEFAULT_EXPERIMENT_NAME = "Trial";

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
	
	public class SDLogHeaderDerivedSensors{
		public final static int PPG2_1_14 = 1<<4;
		public final static int PPG1_12_13 = 1<<3;
		public final static int PPG_12_13 = 1<<2;
		public final static int SKIN_TEMP = 1<<1;
		public final static int RES_AMP = 1<<0;
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
	
	protected Map<Integer,SensorEnabledDetails> mSensorMap = new LinkedHashMap<Integer,SensorEnabledDetails>();
	protected Map<String, ChannelDetails> mChannelMap = new LinkedHashMap<String, ChannelDetails>();
	protected Map<String, AlgorithmDetailsNew> mAlgorithmChannelsMap = new LinkedHashMap<String, AlgorithmDetailsNew>();
	protected Map<String, List<String>> mAlgorithmGroupingMap = new LinkedHashMap<String, List<String>>();
	
	protected Map<String,SensorGroupingDetails> mSensorGroupingMap = new LinkedHashMap<String,SensorGroupingDetails>();
	protected Map<String, SensorConfigOptionDetails> mConfigOptionsMap = new HashMap<String,SensorConfigOptionDetails>();
	
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
	public static final byte SET_LSM303DLHC_ACCEL_CALIBRATION_COMMAND = (byte) 0x1A;
	public static final byte LSM303DLHC_ACCEL_CALIBRATION_RESPONSE 	= (byte) 0x1B;
	public static final byte GET_LSM303DLHC_ACCEL_CALIBRATION_COMMAND = (byte) 0x1C;
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
	public static final byte SET_DERIVED_CHANNEL_BYTES				= (byte) 0x6D; 
	public static final byte DERIVED_CHANNEL_BYTES_RESPONSE			= (byte) 0x6E; 
	public static final byte GET_DERIVED_CHANNEL_BYTES				= (byte) 0x6F; 
	public static final byte START_SDBT_COMMAND 					= (byte) 0x70;
	public static final byte STATUS_RESPONSE	 					= (byte) 0x71;
	public static final byte GET_STATUS_COMMAND 					= (byte) 0x72;
	public static final byte SET_TRIAL_CONFIG_COMMAND				= (byte) 0x73; 
	public static final byte TRIAL_CONFIG_RESPONSE					= (byte) 0x74; 
	public static final byte GET_TRIAL_CONFIG_COMMAND				= (byte) 0x75; 
	public static final byte SET_CENTER_COMMAND						= (byte) 0x76; 
	public static final byte CENTER_RESPONSE						= (byte) 0x77; 
	public static final byte GET_CENTER_COMMAND						= (byte) 0x78; 
	public static final byte SET_SHIMMERNAME_COMMAND				= (byte) 0x79; //Shimmer Name
	public static final byte SHIMMERNAME_RESPONSE					= (byte) 0x7a; 
	public static final byte GET_SHIMMERNAME_COMMAND				= (byte) 0x7b; 
	public static final byte SET_EXPID_COMMAND						= (byte) 0x7c; //Experiment Name
	public static final byte EXPID_RESPONSE							= (byte) 0x7d; 
	public static final byte GET_EXPID_COMMAND						= (byte) 0x7e; 
	public static final byte SET_MYID_COMMAND						= (byte) 0x7F; //Shimmer ID in trial
	public static final byte MYID_RESPONSE							= (byte) 0x80; 
	public static final byte GET_MYID_COMMAND						= (byte) 0x81; 
	public static final byte SET_NSHIMMER_COMMAND					= (byte) 0x82; 
	public static final byte NSHIMMER_RESPONSE						= (byte) 0x83; 
	public static final byte GET_NSHIMMER_COMMAND					= (byte) 0x84; 
	public static final byte SET_CONFIGTIME_COMMAND					= (byte) 0x85; 
	public static final byte CONFIGTIME_RESPONSE					= (byte) 0x86; 
	public static final byte GET_CONFIGTIME_COMMAND					= (byte) 0x87; 
	public static final byte DIR_RESPONSE		 					= (byte) 0x88;
	public static final byte GET_DIR_COMMAND 						= (byte) 0x89;
	public static final byte INSTREAM_CMD_RESPONSE 					= (byte) 0x8A;
	
	public static final byte SET_INFOMEM_COMMAND   					= (byte) 0x8C;
	public static final byte INFOMEM_RESPONSE      					= (byte) 0x8D;
	public static final byte GET_INFOMEM_COMMAND   					= (byte) 0x8E;
	
	public static final byte SET_CRC_COMMAND						= (byte) 0x8B; 
	public static final byte SET_RWC_COMMAND                        = (byte) 0x8F;
	public static final byte RWC_RESPONSE                           = (byte) 0x90;
	public static final byte GET_RWC_COMMAND                        = (byte) 0x91;
	
	public static final byte ROUTINE_COMMUNICATION					= (byte) 0xE0;
	public static final byte ACK_COMMAND_PROCESSED            		= (byte) 0xFF;
	
	public static final byte START_LOGGING_ONLY_COMMAND				= (byte) 0x92;
	public static final byte STOP_LOGGING_ONLY_COMMAND				= (byte) 0x93;
	public static final byte VBATT_RESPONSE                         = (byte) 0x94;
	public static final byte GET_VBATT_COMMAND                      = (byte) 0x95;
	public static final byte TEST_CONNECTION_COMMAND            	= (byte) 0x96;
	public static final byte STOP_SDBT_COMMAND 						= (byte) 0x97;

	public static final int MAX_NUMBER_OF_SIGNALS = 50; //used to be 11 but now 13 because of the SR30 + 8 for 3d orientation
	public static final int MAX_INQUIRY_PACKET_SIZE = 47;

	//TODO switch to just using ShimmerVerObject rather then individual variables 
	protected ShimmerVerObject mShimmerVerObject = new ShimmerVerObject();
	protected int mHardwareVersion=HW_ID.UNKNOWN;
	public String mHardwareVersionParsed = "";
	public int mFirmwareVersionCode = 0;
	public int mFirmwareIdentifier = 0;
	public int mFirmwareVersionMajor = 0;
	public int mFirmwareVersionMinor = 0;
	public int mFirmwareVersionInternal = 0;
	public String mFirmwareVersionParsed = "";
	
	public InfoMemLayout mInfoMemLayout = new InfoMemLayout();
	
	//TODO switch to just using ExpansionBoardDetails rather then individual variables 
	protected ExpansionBoardDetails mExpansionBoardDetails = new ExpansionBoardDetails();
	public int mExpansionBoardId = HW_ID_SR_CODES.UNKNOWN; 
	public int mExpansionBoardRev = -1;
	public int mExpansionBoardRevSpecial = -1;
	public String mExpansionBoardParsed = "";  
	public String mExpansionBoardParsedWithVer = "";  
	protected byte[] mExpBoardArray = null; // Array where the expansion board response is stored
	
	public static final int ANY_VERSION = -1;
	
	protected String mClassName="Shimmer";
	protected double mLastReceivedTimeStamp=0;
	protected double mCurrentTimeStampCycle=0;
	protected double mShimmerSamplingRate; 	                                        	// 51.2Hz is the default sampling rate 
	protected long mEnabledSensors = (long)0;												// This stores the enabled sensors
	protected long mDerivedSensors = (long)0;												// This stores the sensors channels derived in SW
	protected int mBluetoothBaudRate=9; //460800

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
	protected int mPastGSRRange=4; // this is to fix a bug with SDLog v0.9
	protected int mPastGSRUncalibratedValue=4; // this is to fix a bug with SDLog v0.9
	protected boolean mPastGSRFirstTime=true; // this is to fix a bug with SDLog v0.9
	protected int mInternalExpPower=-1;													// This shows whether the internal exp power is enabled.
	protected long mConfigByte0;
	protected int mNChannels=0;	                                                // Default number of sensor channels set to three because of the on board accelerometer 
	protected int mBufferSize;                   							

	protected String[] mSignalNameArray=new String[MAX_NUMBER_OF_SIGNALS];							// 19 is the maximum number of signal thus far
	protected String[] mSignalDataTypeArray=new String[MAX_NUMBER_OF_SIGNALS];						// 19 is the maximum number of signal thus far
	protected boolean mDefaultCalibrationParametersECG = true;
	protected boolean mDefaultCalibrationParametersEMG = true;
	protected boolean mDefaultCalibrationParametersAccel = true;
	protected boolean mDefaultCalibrationParametersDigitalAccel = true; //Also known as the wide range accelerometer
	protected int mPressureResolution = 0;
	/** 0 = 16 bit, 1 = 24 bit */
	protected int mExGResolution = 1;
	private boolean mIsExg1_24bitEnabled = false;
	private boolean mIsExg2_24bitEnabled = false;
	private boolean mIsExg1_16bitEnabled = false;
	private boolean mIsExg2_16bitEnabled = false;
	
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
	protected int mShowRtcErrorLeds = 0;
	protected int mMasterShimmer = 0;
	protected int mSingleTouch = 0;
	protected int mTCXO = 0;
	protected long mConfigTime; //this is in milliseconds, utc
	protected long mRTCOffset; //this is in ticks
	protected int mSyncWhenLogging = 0;
	protected int mSyncBroadcastInterval = 0;
	protected byte[] mInfoMemBytes = createEmptyInfoMemByteArray(512);
	protected boolean mShimmerUsingConfigFromInfoMem = false;
	protected boolean mIsCrcEnabled = false;
	protected String mCenter = "";
	
	protected long mInitialTimeStamp;

	protected final static int FW_TYPE_BT=0;
	protected final static int FW_TYPE_SD=1;
	
	protected String mTrialName = "";
	protected int mTrialId = 0;
	protected int mTrialNumberOfShimmers = 0;

	protected int mTrialDurationEstimated = 0;
	protected int mTrialDurationMaximum = 0;
	
	protected String mMyBluetoothAddress="";
	protected String mMacIdFromUart = "";
	protected String mMacIdFromInfoMem = "";
	
	protected String mShimmerUserAssignedName=""; // This stores the user assigned name

	protected boolean mConfigFileCreationFlag = true;
	protected boolean mCalibFileCreationFlag = false;
	
	protected List<String> syncNodesList = new ArrayList<String>();
	
	protected int mPpgAdcSelectionGsrBoard = 0;
	protected int mPpg1AdcSelectionProto3DeluxeBoard = 0;
	protected int mPpg2AdcSelectionProto3DeluxeBoard = 0;
	

	//
	protected double[][] mAlignmentMatrixAnalogAccel = {{-1,0,0},{0,-1,0},{0,0,1}}; 			
	protected double[][] mSensitivityMatrixAnalogAccel = {{38,0,0},{0,38,0},{0,0,38}}; 	
	protected double[][] mOffsetVectorAnalogAccel = {{2048},{2048},{2048}};				
	protected abstract void checkBattery();
	protected double[][] mAlignmentMatrixWRAccel = {{-1,0,0},{0,1,0},{0,0,-1}}; 			
	protected double[][] mSensitivityMatrixWRAccel = {{1631,0,0},{0,1631,0},{0,0,1631}}; 	
	protected double[][] mOffsetVectorWRAccel = {{0},{0},{0}};	

	//Default values Shimmer2 
	protected static final double[][] SensitivityMatrixAccel1p5gShimmer2 = {{101,0,0},{0,101,0},{0,0,101}};
	protected static final double[][] SensitivityMatrixAccel2gShimmer2 = {{76,0,0},{0,76,0},{0,0,76}};
	protected static final double[][] SensitivityMatrixAccel4gShimmer2 = {{38,0,0},{0,38,0},{0,0,38}};
	protected static final double[][] SensitivityMatrixAccel6gShimmer2 = {{25,0,0},{0,25,0},{0,0,25}};
	protected static final double[][] AlignmentMatrixAccelShimmer2 =  {{-1,0,0},{0,-1,0},{0,0,1}}; 			
	protected static final double[][] OffsetVectorAccelShimmer2 = {{2048},{2048},{2048}};			
	//Shimmer3
	public static final double[][] SensitivityMatrixLowNoiseAccel2gShimmer3 = {{83,0,0},{0,83,0},{0,0,83}};
	protected static final double[][] AlignmentMatrixLowNoiseAccelShimmer3 = {{0,-1,0},{-1,0,0},{0,0,-1}};
	
	public static final double[][] SensitivityMatrixWideRangeAccel2gShimmer3 = {{1631,0,0},{0,1631,0},{0,0,1631}};
	public static final double[][] SensitivityMatrixWideRangeAccel4gShimmer3 = {{815,0,0},{0,815,0},{0,0,815}};
	public static final double[][] SensitivityMatrixWideRangeAccel8gShimmer3 = {{408,0,0},{0,408,0},{0,0,408}};
	public static final double[][] SensitivityMatrixWideRangeAccel16gShimmer3 = {{135,0,0},{0,135,0},{0,0,135}};

	protected static final double[][] AlignmentMatrixWideRangeAccelShimmer3 = {{-1,0,0},{0,1,0},{0,0,-1}}; 	
	protected static final double[][] AlignmentMatrixAccelShimmer3 = {{0,-1,0},{-1,0,0},{0,0,-1}};
	protected static final double[][] OffsetVectorLowNoiseAccelShimmer3 = {{2047},{2047},{2047}};
	protected static final double[][] OffsetVectorWideRangeAccelShimmer3 = {{0},{0},{0}};

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
	protected static final double[][] AlignmentMatrixGyroShimmer2 = {{0,-1,0},{-1,0,0},{0,0,-1}}; 				
	protected static final double[][] SensitivityMatrixGyroShimmer2 = {{2.73,0,0},{0,2.73,0},{0,0,2.73}}; 		
	protected static final double[][] OffsetVectorGyroShimmer2 = {{1843},{1843},{1843}};
	//Shimmer3
	public static final double[][] SensitivityMatrixGyro250dpsShimmer3 = {{131,0,0},{0,131,0},{0,0,131}};
	public static final double[][] SensitivityMatrixGyro500dpsShimmer3 = {{65.5,0,0},{0,65.5,0},{0,0,65.5}};
	public static final double[][] SensitivityMatrixGyro1000dpsShimmer3 = {{32.8,0,0},{0,32.8,0},{0,0,32.8}};
	public static final double[][] SensitivityMatrixGyro2000dpsShimmer3 = {{16.4,0,0},{0,16.4,0},{0,0,16.4}};
	protected static final double[][] AlignmentMatrixGyroShimmer3 = {{0,-1,0},{-1,0,0},{0,0,-1}}; 				
	protected static final double[][] OffsetVectorGyroShimmer3 = {{0},{0},{0}};		

	protected int mCurrentLEDStatus=0;
	protected boolean mDefaultCalibrationParametersMag = true;
	protected double[][] mAlignmentMatrixMagnetometer = {{1,0,0},{0,1,0},{0,0,-1}}; 				
	protected double[][] mSensitivityMatrixMagnetometer = {{580,0,0},{0,580,0},{0,0,580}}; 		
	protected double[][] mOffsetVectorMagnetometer = {{0},{0},{0}};								

	//Default values Shimmer2 and Shimmer3
	protected static final double[][] AlignmentMatrixMagShimmer2 = {{1,0,0},{0,1,0},{0,0,-1}};
	protected static final double[][] SensitivityMatrixMagShimmer2 = {{580,0,0},{0,580,0},{0,0,580}}; 		
	protected static final double[][] OffsetVectorMagShimmer2 = {{0},{0},{0}};				
	//Shimmer3
	protected static final double[][] AlignmentMatrixMagShimmer3 = {{-1,0,0},{0,1,0},{0,0,-1}}; 				
	protected static final double[][] SensitivityMatrixMagShimmer3 = {{1100,0,0},{0,1100,0},{0,0,980}}; 		
	protected static final double[][] OffsetVectorMagShimmer3 = {{0},{0},{0}};		

	
	protected double[][] AlignmentMatrixMPLAccel = {{-1,0,0},{0,1,0},{0,0,-1}}; 			
	protected double[][] SensitivityMatrixMPLAccel = {{1631,0,0},{0,1631,0},{0,0,1631}}; 	
	protected double[][] OffsetVectorMPLAccel = {{0},{0},{0}};
	
	protected double[][] AlignmentMatrixMPLMag = {{-1,0,0},{0,1,0},{0,0,-1}}; 			
	protected double[][] SensitivityMatrixMPLMag = {{1631,0,0},{0,1631,0},{0,0,1631}}; 	
	protected double[][] OffsetVectorMPLMag = {{0},{0},{0}};
	
	protected double[][] AlignmentMatrixMPLGyro = {{-1,0,0},{0,1,0},{0,0,-1}}; 			
	protected double[][] SensitivityMatrixMPLGyro = {{1631,0,0},{0,1631,0},{0,0,1631}}; 	
	protected double[][] OffsetVectorMPLGyro = {{0},{0},{0}};
	

	public static final double[][] SensitivityMatrixMag1p3GaShimmer3 = {{1100,0,0},{0,1100,0},{0,0,980}};
	public static final double[][] SensitivityMatrixMag1p9GaShimmer3 = {{855,0,0},{0,855,0},{0,0,760}};
	public static final double[][] SensitivityMatrixMag2p5GaShimmer3 = {{670,0,0},{0,670,0},{0,0,600}};
	public static final double[][] SensitivityMatrixMag4GaShimmer3 = {{450,0,0},{0,450,0},{0,0,400}};
	public static final double[][] SensitivityMatrixMag4p7GaShimmer3 = {{400,0,0},{0,400,0},{0,0,355}};
	public static final double[][] SensitivityMatrixMag5p6GaShimmer3 = {{330,0,0},{0,330,0},{0,0,295}};
	public static final double[][] SensitivityMatrixMag8p1GaShimmer3 = {{230,0,0},{0,230,0},{0,0,205}};

	protected static final double[][] SensitivityMatrixMag0p8GaShimmer2 = {{1370,0,0},{0,1370,0},{0,0,1370}};
	protected static final double[][] SensitivityMatrixMag1p3GaShimmer2 = {{1090,0,0},{0,1090,0},{0,0,1090}};
	protected static final double[][] SensitivityMatrixMag1p9GaShimmer2 = {{820,0,0},{0,820,0},{0,0,820}};
	protected static final double[][] SensitivityMatrixMag2p5GaShimmer2 = {{660,0,0},{0,660,0},{0,0,660}};
	protected static final double[][] SensitivityMatrixMag4p0GaShimmer2 = {{440,0,0},{0,440,0},{0,0,440}};
	protected static final double[][] SensitivityMatrixMag4p7GaShimmer2 = {{390,0,0},{0,390,0},{0,0,390}};
	protected static final double[][] SensitivityMatrixMag5p6GaShimmer2 = {{330,0,0},{0,330,0},{0,0,330}};
	protected static final double[][] SensitivityMatrixMag8p1GaShimmer2 = {{230,0,0},{0,230,0},{0,0,230}};


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
	protected double mPacketReceptionRateCurrent=100;
	protected double mLastReceivedCalibratedTimeStamp=-1; 
	protected boolean mFirstTimeCalTime=true;
	protected double mCalTimeStart;
	private double mLastKnownHeartRate=0;
	protected DescriptiveStatistics mVSenseBattMA= new DescriptiveStatistics(1024);
	Quat4d mQ = new Quat4d();
	transient GradDes3DOrientation mOrientationAlgo;
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
	protected boolean mIsStreaming =false;											// This is used to monitor whether the device is in streaming mode
	protected boolean mIsSDLogging =false;											// This is used to monitor whether the device is in sd log mode
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
	protected ExGConfigBytesDetails mExGConfigBytesDetails = new ExGConfigBytesDetails(); 

	protected byte[] mEXG1RegisterArray = new byte[10];
	protected byte[] mEXG2RegisterArray = new byte[10];
	protected int mEXG1RateSetting; //setting not value
	protected int mEXG1CH1GainSetting; // this is the setting not to be confused with the actual value
	protected int mEXG1CH1GainValue; // this is the value
	protected int mEXG1CH2GainSetting; // this is the setting not to be confused with the actual value
	protected int mEXG1CH2GainValue; // this is the value
	protected int mEXG2RateSetting; //setting not value
	protected int mEXG2CH1GainSetting; // this is the setting not to be confused with the actual value
	protected int mEXG2CH1GainValue; // this is the value
	protected int mEXG2CH2PowerDown;//Not used in ShimmerBluetooth
	protected int mEXG2CH2GainSetting; // this is the setting not to be confused with the actual value
	protected int mEXG2CH2GainValue; // this is the value
	
	//EXG ADVANCED
	protected int mEXGReferenceElectrode=-1;
	protected int mLeadOffDetectionMode;
	protected int mEXG1LeadOffCurrentMode;
	protected int mEXG2LeadOffCurrentMode;
	protected int mEXG1Comparators;
	protected int mEXG2Comparators;
	protected int mEXGRLDSense;
	protected int mEXG1LeadOffSenseSelection;
	protected int mEXG2LeadOffSenseSelection;
	protected int mEXGLeadOffDetectionCurrent;
	protected int mEXGLeadOffComparatorTreshold;
	protected int mEXG2RespirationDetectState;//Not used in ShimmerBluetooth
	protected int mEXG2RespirationDetectFreq;//Not used in ShimmerBluetooth
	protected int mEXG2RespirationDetectPhase;//Not used in ShimmerBluetooth
	
	
	//This features are only used in LogAndStream FW 
	protected String mDirectoryName;
	protected int mDirectoryNameLength;
	protected boolean mIsSensing;
	private List<String[]> mExtraSignalProperties = null;
	
	protected ShimmerBattStatusDetails mShimmerBattStatusDetails = new ShimmerBattStatusDetails();
//	protected String mChargingState = "";
//	protected int mChargingStatus = 0;
//	protected String mBattVoltage = "";
//	protected Double mEstimatedChargePercentage = 0.0;
//	protected String mEstimatedChargePercentageParsed = "";

	protected boolean mIsInitialised = false;
	protected boolean mIsDocked = false;
	protected boolean mHaveAttemptedToReadConfig = false;
	
	List<Integer> mListOfMplChannels = Arrays.asList(
			Configuration.Shimmer3.SensorMapKey.MPU9150_TEMP,
			Configuration.Shimmer3.SensorMapKey.MPU9150_MPL_QUAT_6DOF,
			Configuration.Shimmer3.SensorMapKey.MPU9150_MPL_QUAT_9DOF,
			Configuration.Shimmer3.SensorMapKey.MPU9150_MPL_EULER_6DOF,
			Configuration.Shimmer3.SensorMapKey.MPU9150_MPL_EULER_9DOF,
			Configuration.Shimmer3.SensorMapKey.MPU9150_MPL_HEADING,
			Configuration.Shimmer3.SensorMapKey.MPU9150_MPL_PEDOMETER,
			Configuration.Shimmer3.SensorMapKey.MPU9150_MPL_TAP,
			Configuration.Shimmer3.SensorMapKey.MPU9150_MPL_MOTION_ORIENT,
			Configuration.Shimmer3.SensorMapKey.MPU9150_MPL_GYRO,
			Configuration.Shimmer3.SensorMapKey.MPU9150_MPL_ACCEL,
			Configuration.Shimmer3.SensorMapKey.MPU9150_MPL_MAG,
			Configuration.Shimmer3.SensorMapKey.MPU9150_MPL_QUAT_6DOF_RAW);

	public static final double ACCELERATION_DUE_TO_GRAVITY = 9.81;
	
	//Testing for GQ
	protected int mGqPacketNumHeaderBytes = 0;
	protected int mSamplingDividerVBatt = 0;
	protected int mSamplingDividerGsr = 0;
	protected int mSamplingDividerPpg = 0;
	protected int mSamplingDividerLsm303dlhcAccel = 0;
	protected int mSamplingDividerBeacon = 0;

	/** A shimmer device will have multiple sensors, depending on HW type and revision, 
	 * these type of sensors can change, this holds a list of all the sensors for different versions.
	 * This only works with classes which implements the ShimmerHardwareSensors interface. E.g. ShimmerGQ
	 * 
	 */
	protected List<AbstractSensor> mListOfSensors = new ArrayList<AbstractSensor>();
	
	
	protected int mTimeStampPacketByteSize = 2;
//	protected byte[] mSetRWC;
//	protected byte[] mGetRWC;
	public long mShimmerRealWorldClockConFigTime = 0;
	public long mShimmerLastReadRealTimeClockValue = 0;
	public String mShimmerLastReadRtcValueParsed = "";
	
	protected int mTimeStampPacketRawMaxValue = 65536;// 16777216 or 65536 
	
	private boolean isOverrideShowRwcErrorLeds = true;


	/** This method will be deprecated for future Shimmer hardware revisions. The last hardware this will be used for is Shimmer3. 
	 *  It should work with all FW associated with Shimmer3 and Shimmer2 devices.
	 *  
	 *  Future hardware which WON'T be using this will start with ShimmerEmotionalGQ HW. 
	 * 
	 * @param newPacket
	 * @param fwIdentifier
	 * @param timeSync
	 * @param pctimestamp this is only used by shimmerbluetooth, set to -1 if not using
	 * @return
	 * @throws Exception
	 */
	protected ObjectCluster buildMsg(byte[] newPacket, int fwIdentifier, int timeSync, long pctimestamp) throws Exception {
		ObjectCluster objectCluster = new ObjectCluster();
		
		objectCluster.mMyName = mShimmerUserAssignedName;
		objectCluster.mBluetoothAddress = mMyBluetoothAddress;
		objectCluster.mRawData = newPacket;
		long systemTime = System.currentTimeMillis();
		if(fwIdentifier == FW_TYPE_BT){
			systemTime = pctimestamp;
		}
		objectCluster.mSystemTimeStamp=ByteBuffer.allocate(8).putLong(systemTime).array();
		objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.SYSTEM_TIMESTAMP,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLISECONDS,systemTime));
		
		if(fwIdentifier != FW_TYPE_BT && fwIdentifier != FW_TYPE_SD){
			throw new Exception("The Firmware is not compatible");
		}
		
		int numCalibratedData = mNChannels;
		int numUncalibratedData = mNChannels;
		int numUncalibratedDataUnits = mNChannels;
		int numCalibratedDataUnits = mNChannels;
		int numSensorNames = mNChannels;
		int numAdditionalChannels = 0;
		
		if (fwIdentifier == FW_TYPE_BT){
			objectCluster.mSystemTimeStamp=ByteBuffer.allocate(8).putLong(systemTime).array();
			//plus 1 because of: timestamp
			numAdditionalChannels += 1;
			
			//plus 4 because of: batt percent, PRR Current, PRR Trial, system timestamp
			numAdditionalChannels += 4;
		} 
		else {
			if (mRTCOffset == 0){
				//sd log time stamp already included in mnChannels
			} 
			else {
				//for RTC
				numAdditionalChannels += 1;
			}
		}
		
		if (fwIdentifier == FW_TYPE_BT){ // Here so as not to mess with ShimmerSDLog
			//for ECG LL-LA Channel added&calculated in API
			if(isEXGUsingDefaultECGConfiguration()){
				numAdditionalChannels += 1;
			}
		}
		
		double [] calibratedData = new double[numCalibratedData+numAdditionalChannels];
		double [] uncalibratedData = new double[numUncalibratedData+numAdditionalChannels];
		String [] uncalibratedDataUnits = new String[numUncalibratedDataUnits+numAdditionalChannels];
		String [] calibratedDataUnits = new String[numCalibratedDataUnits+numAdditionalChannels];
		String [] sensorNames = new String[numSensorNames+numAdditionalChannels];
		
		System.arraycopy(mSignalNameArray, 0, sensorNames, 0, sensorNames.length);
		
		//PARSE DATA
		long[] newPacketInt=parsedData(newPacket,mSignalDataTypeArray);
		
		double[] tempData=new double[3];
		Vector3d accelerometer = new Vector3d();
		Vector3d magnetometer = new Vector3d();
		Vector3d gyroscope = new Vector3d();
		
		
		if (mHardwareVersion==HW_ID.SHIMMER_SR30 || mHardwareVersion==HW_ID.SHIMMER_3){
			
			int iTimeStamp=getSignalIndex(Configuration.Shimmer3.ObjectClusterSensorName.TIMESTAMP); //find index
			if(mFirstTime && fwIdentifier == FW_TYPE_SD){
				//this is to make sure the Raw starts from zero
				mFirstRawTS = (double)newPacketInt[iTimeStamp];
				mFirstTime = false;
			}
			double calibratedTS = calibrateTimeStamp((double)newPacketInt[iTimeStamp]);

			//TIMESTAMP
			if (fwIdentifier == FW_TYPE_SD){
				// RTC timestamp uncal. (shimmer timestamp + RTC offset from header); unit = ticks
				double unwrappedrawtimestamp = calibratedTS*32768/1000;
				unwrappedrawtimestamp = unwrappedrawtimestamp - mFirstRawTS; //deduct this so it will start from 0
				long sdlograwtimestamp = (long)mInitialTimeStamp + (long)unwrappedrawtimestamp;
				objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.TIMESTAMP,new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.CLOCK_UNIT,(double)sdlograwtimestamp));
				uncalibratedData[iTimeStamp] = (double)sdlograwtimestamp;
				uncalibratedDataUnits[iTimeStamp] = CHANNEL_UNITS.CLOCK_UNIT;

				if (mEnableCalibration){
					double sdlogcaltimestamp = (double)sdlograwtimestamp/32768*1000;
					objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.TIMESTAMP,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLISECONDS,sdlogcaltimestamp));
					calibratedData[iTimeStamp] = sdlogcaltimestamp;
					calibratedDataUnits[iTimeStamp] = CHANNEL_UNITS.MILLISECONDS;
				}
			} 
			else if (fwIdentifier == FW_TYPE_BT){
				objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.TIMESTAMP,new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iTimeStamp]));
				uncalibratedData[iTimeStamp] = (double)newPacketInt[iTimeStamp];
				uncalibratedDataUnits[iTimeStamp] = CHANNEL_UNITS.NO_UNITS;
				if (mEnableCalibration){
					objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.TIMESTAMP,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLISECONDS,calibratedTS));
					calibratedData[iTimeStamp] = calibratedTS;
					calibratedDataUnits[iTimeStamp] = CHANNEL_UNITS.MILLISECONDS;
					objectCluster.mShimmerCalibratedTimeStamp = calibratedTS;
				}
			}

			//RAW RTC
			if ((fwIdentifier == FW_TYPE_SD) && mRTCOffset!=0)
			{
				double unwrappedrawtimestamp = calibratedTS*32768/1000;
				unwrappedrawtimestamp = unwrappedrawtimestamp - mFirstRawTS; //deduct this so it will start from 0
				long rtctimestamp = (long)mInitialTimeStamp + (long)unwrappedrawtimestamp + mRTCOffset;
				objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.REAL_TIME_CLOCK,new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.CLOCK_UNIT,(double)rtctimestamp));
				uncalibratedData[sensorNames.length-1] = (double)rtctimestamp;
				uncalibratedDataUnits[sensorNames.length-1] = CHANNEL_UNITS.CLOCK_UNIT;
				sensorNames[sensorNames.length-1]= Shimmer3.ObjectClusterSensorName.REAL_TIME_CLOCK;
				if (mEnableCalibration){
					double rtctimestampcal = ((double)mInitialTimeStamp/32768.0*1000.0) + calibratedTS + ((double)mRTCOffset/32768.0*1000.0) - (mFirstRawTS/32768.0*1000.0);
					objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.REAL_TIME_CLOCK,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLISECONDS,rtctimestampcal));
					calibratedData[sensorNames.length-1] = rtctimestampcal;
					calibratedDataUnits[sensorNames.length-1] = CHANNEL_UNITS.MILLISECONDS;
				}
			}

			//OFFSET
			if(timeSync==1 && (fwIdentifier == FW_TYPE_SD)){
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
					if(newPacketInt[iOffset] == 4294967295L){ //this is 4 bytes
						offsetValue=Double.NaN;
					} else {
						offsetValue=(double)newPacketInt[iOffset];
					}
				}

				objectCluster.mPropertyCluster.put("Offset",new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,offsetValue));
				uncalibratedData[iOffset] = offsetValue;
				calibratedData[iOffset] = Double.NaN;
				uncalibratedDataUnits[iOffset] = CHANNEL_UNITS.NO_UNITS;
				calibratedDataUnits[iOffset] = CHANNEL_UNITS.NO_UNITS;
			} 


			objectCluster = callAdditionalServices(objectCluster);


			//first get raw and calibrated data, this is data derived from the Shimmer device and involves no involvement from the API


			if (((fwIdentifier == FW_TYPE_BT) && (mEnabledSensors & BTStream.ACCEL_LN) > 0) 
					|| ((fwIdentifier == FW_TYPE_SD) && (mEnabledSensors & SDLogHeader.ACCEL_LN) > 0)){
				int iAccelX=getSignalIndex(Shimmer3.ObjectClusterSensorName.ACCEL_LN_X); //find index
				int iAccelY=getSignalIndex(Shimmer3.ObjectClusterSensorName.ACCEL_LN_Y); //find index
				int iAccelZ=getSignalIndex(Shimmer3.ObjectClusterSensorName.ACCEL_LN_Z); //find index
				tempData[0]=(double)newPacketInt[iAccelX];
				tempData[1]=(double)newPacketInt[iAccelY];
				tempData[2]=(double)newPacketInt[iAccelZ];

				objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.ACCEL_LN_X,new FormatCluster(CHANNEL_TYPE.UNCAL.toString().toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iAccelX]));
				objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.ACCEL_LN_Y,new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iAccelY]));
				objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.ACCEL_LN_Z,new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iAccelZ]));
				uncalibratedData[iAccelX]=(double)newPacketInt[iAccelX];
				uncalibratedData[iAccelY]=(double)newPacketInt[iAccelY];
				uncalibratedData[iAccelZ]=(double)newPacketInt[iAccelZ];
				uncalibratedDataUnits[iAccelX]=CHANNEL_UNITS.NO_UNITS;
				uncalibratedDataUnits[iAccelY]=CHANNEL_UNITS.NO_UNITS;
				uncalibratedDataUnits[iAccelZ]=CHANNEL_UNITS.NO_UNITS;


				if (mEnableCalibration){
					double[] accelCalibratedData;
					accelCalibratedData=calibrateInertialSensorData(tempData, mAlignmentMatrixAnalogAccel, mSensitivityMatrixAnalogAccel, mOffsetVectorAnalogAccel);
					calibratedData[iAccelX]=accelCalibratedData[0];
					calibratedData[iAccelY]=accelCalibratedData[1];
					calibratedData[iAccelZ]=accelCalibratedData[2];

					objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.ACCEL_LN_X,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.ACCEL_CAL_UNIT,accelCalibratedData[0],mDefaultCalibrationParametersAccel));
					objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.ACCEL_LN_Y,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.ACCEL_CAL_UNIT,accelCalibratedData[1],mDefaultCalibrationParametersAccel));
					objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.ACCEL_LN_Z,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.ACCEL_CAL_UNIT,accelCalibratedData[2],mDefaultCalibrationParametersAccel));
					calibratedDataUnits[iAccelX] = CHANNEL_UNITS.ACCEL_CAL_UNIT;
					calibratedDataUnits[iAccelY] = CHANNEL_UNITS.ACCEL_CAL_UNIT;
					calibratedDataUnits[iAccelZ] = CHANNEL_UNITS.ACCEL_CAL_UNIT;
					accelerometer.x=accelCalibratedData[0];
					accelerometer.y=accelCalibratedData[1];
					accelerometer.z=accelCalibratedData[2];

//					if (mDefaultCalibrationParametersAccel == true) {
//						objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.ACCEL_LN_X,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.ACCEL_DEFAULT_CAL_UNIT,accelCalibratedData[0]));
//						objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.ACCEL_LN_Y,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.ACCEL_DEFAULT_CAL_UNIT,accelCalibratedData[1]));
//						objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.ACCEL_LN_Z,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.ACCEL_DEFAULT_CAL_UNIT,accelCalibratedData[2]));
//						calibratedDataUnits[iAccelX]=CHANNEL_UNITS.ACCEL_DEFAULT_CAL_UNIT;
//						calibratedDataUnits[iAccelY]=CHANNEL_UNITS.ACCEL_DEFAULT_CAL_UNIT;
//						calibratedDataUnits[iAccelZ]=CHANNEL_UNITS.ACCEL_DEFAULT_CAL_UNIT;
//						accelerometer.x=accelCalibratedData[0];
//						accelerometer.y=accelCalibratedData[1];
//						accelerometer.z=accelCalibratedData[2];
//
//					} else {
//						objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.ACCEL_LN_X,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.ACCEL_CAL_UNIT,accelCalibratedData[0]));
//						objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.ACCEL_LN_Y,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.ACCEL_CAL_UNIT,accelCalibratedData[1]));
//						objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.ACCEL_LN_Z,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.ACCEL_CAL_UNIT,accelCalibratedData[2]));
//						calibratedDataUnits[iAccelX] = CHANNEL_UNITS.ACCEL_CAL_UNIT;
//						calibratedDataUnits[iAccelY] = CHANNEL_UNITS.ACCEL_CAL_UNIT;
//						calibratedDataUnits[iAccelZ] = CHANNEL_UNITS.ACCEL_CAL_UNIT;
//						accelerometer.x=accelCalibratedData[0];
//						accelerometer.y=accelCalibratedData[1];
//						accelerometer.z=accelCalibratedData[2];
//
//					}
				}
			}
			if (((fwIdentifier == FW_TYPE_BT) && (mEnabledSensors & BTStream.ACCEL_WR) > 0) 
					|| ((fwIdentifier == FW_TYPE_SD) && (mEnabledSensors & SDLogHeader.ACCEL_WR) > 0)
					){
				int iAccelX=getSignalIndex(Shimmer3.ObjectClusterSensorName.ACCEL_WR_X); //find index
				int iAccelY=getSignalIndex(Shimmer3.ObjectClusterSensorName.ACCEL_WR_Y); //find index
				int iAccelZ=getSignalIndex(Shimmer3.ObjectClusterSensorName.ACCEL_WR_Z); //find index
				//check range

				tempData[0]=(double)newPacketInt[iAccelX];
				tempData[1]=(double)newPacketInt[iAccelY];
				tempData[2]=(double)newPacketInt[iAccelZ];
				objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.ACCEL_WR_X,new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iAccelX]));
				objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.ACCEL_WR_Y,new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iAccelY]));
				objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.ACCEL_WR_Z,new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iAccelZ]));
				uncalibratedData[iAccelX]=(double)newPacketInt[iAccelX];
				uncalibratedData[iAccelY]=(double)newPacketInt[iAccelY];
				uncalibratedData[iAccelZ]=(double)newPacketInt[iAccelZ];
				uncalibratedDataUnits[iAccelX]=CHANNEL_UNITS.NO_UNITS;
				uncalibratedDataUnits[iAccelY]=CHANNEL_UNITS.NO_UNITS;
				uncalibratedDataUnits[iAccelZ]=CHANNEL_UNITS.NO_UNITS;

				if (mEnableCalibration){
					double[] accelCalibratedData;
					accelCalibratedData=calibrateInertialSensorData(tempData, mAlignmentMatrixWRAccel, mSensitivityMatrixWRAccel, mOffsetVectorWRAccel);
					calibratedData[iAccelX]=accelCalibratedData[0];
					calibratedData[iAccelY]=accelCalibratedData[1];
					calibratedData[iAccelZ]=accelCalibratedData[2];
					
					objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.ACCEL_WR_X,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.ACCEL_CAL_UNIT,calibratedData[iAccelX],mDefaultCalibrationParametersDigitalAccel));
					objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.ACCEL_WR_Y,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.ACCEL_CAL_UNIT,calibratedData[iAccelY],mDefaultCalibrationParametersDigitalAccel));
					objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.ACCEL_WR_Z,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.ACCEL_CAL_UNIT,calibratedData[iAccelZ],mDefaultCalibrationParametersDigitalAccel));
					calibratedDataUnits[iAccelX]=CHANNEL_UNITS.ACCEL_CAL_UNIT;
					calibratedDataUnits[iAccelY]=CHANNEL_UNITS.ACCEL_CAL_UNIT;
					calibratedDataUnits[iAccelZ]=CHANNEL_UNITS.ACCEL_CAL_UNIT;
					if (((mEnabledSensors & SENSOR_ACCEL) == 0)){
						accelerometer.x=accelCalibratedData[0];
						accelerometer.y=accelCalibratedData[1];
						accelerometer.z=accelCalibratedData[2];
					}

//					if (mDefaultCalibrationParametersDigitalAccel == true) {
//						objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.ACCEL_WR_X,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.ACCEL_DEFAULT_CAL_UNIT,calibratedData[iAccelX]));
//						objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.ACCEL_WR_Y,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.ACCEL_DEFAULT_CAL_UNIT,calibratedData[iAccelY]));
//						objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.ACCEL_WR_Z,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.ACCEL_DEFAULT_CAL_UNIT,calibratedData[iAccelZ]));
//						calibratedDataUnits[iAccelX]=CHANNEL_UNITS.ACCEL_DEFAULT_CAL_UNIT;
//						calibratedDataUnits[iAccelY]=CHANNEL_UNITS.ACCEL_DEFAULT_CAL_UNIT;
//						calibratedDataUnits[iAccelZ]=CHANNEL_UNITS.ACCEL_DEFAULT_CAL_UNIT;
//						if (((mEnabledSensors & SENSOR_ACCEL) == 0)){
//							accelerometer.x=accelCalibratedData[0]; //this is used to calculate quaternions // skip if Low noise is enabled
//							accelerometer.y=accelCalibratedData[1];
//							accelerometer.z=accelCalibratedData[2];
//						}
//					} else {
//						objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.ACCEL_WR_X,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.ACCEL_CAL_UNIT,calibratedData[iAccelX]));
//						objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.ACCEL_WR_Y,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.ACCEL_CAL_UNIT,calibratedData[iAccelY]));
//						objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.ACCEL_WR_Z,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.ACCEL_CAL_UNIT,calibratedData[iAccelZ]));
//						calibratedDataUnits[iAccelX]=CHANNEL_UNITS.ACCEL_CAL_UNIT;
//						calibratedDataUnits[iAccelY]=CHANNEL_UNITS.ACCEL_CAL_UNIT;
//						calibratedDataUnits[iAccelZ]=CHANNEL_UNITS.ACCEL_CAL_UNIT;
//						if (((mEnabledSensors & SENSOR_ACCEL) == 0)){
//							accelerometer.x=accelCalibratedData[0];
//							accelerometer.y=accelCalibratedData[1];
//							accelerometer.z=accelCalibratedData[2];
//						}
//
//					}	
				}
			}
			if (((fwIdentifier == FW_TYPE_BT) && (mEnabledSensors & BTStream.GYRO) > 0) 
					|| ((fwIdentifier == FW_TYPE_SD) && (mEnabledSensors & SDLogHeader.GYRO) > 0)
					) {
				int iGyroX=getSignalIndex(Shimmer3.ObjectClusterSensorName.GYRO_X);
				int iGyroY=getSignalIndex(Shimmer3.ObjectClusterSensorName.GYRO_Y);
				int iGyroZ=getSignalIndex(Shimmer3.ObjectClusterSensorName.GYRO_Z);
				tempData[0]=(double)newPacketInt[iGyroX];
				tempData[1]=(double)newPacketInt[iGyroY];
				tempData[2]=(double)newPacketInt[iGyroZ];


				objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.GYRO_X,new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iGyroX]));
				objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.GYRO_Y,new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iGyroY]));
				objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.GYRO_Z,new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iGyroZ]));
				uncalibratedData[iGyroX]=(double)newPacketInt[iGyroX];
				uncalibratedData[iGyroY]=(double)newPacketInt[iGyroY];
				uncalibratedData[iGyroZ]=(double)newPacketInt[iGyroZ];
				uncalibratedDataUnits[iGyroX]=CHANNEL_UNITS.NO_UNITS;
				uncalibratedDataUnits[iGyroY]=CHANNEL_UNITS.NO_UNITS;
				uncalibratedDataUnits[iGyroZ]=CHANNEL_UNITS.NO_UNITS;
				if (mEnableCalibration){
					double[] gyroCalibratedData=calibrateInertialSensorData(tempData, mAlignmentMatrixGyroscope, mSensitivityMatrixGyroscope, mOffsetVectorGyroscope);
					calibratedData[iGyroX]=gyroCalibratedData[0];
					calibratedData[iGyroY]=gyroCalibratedData[1];
					calibratedData[iGyroZ]=gyroCalibratedData[2];
					
					objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.GYRO_X,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.GYRO_CAL_UNIT,gyroCalibratedData[0],mDefaultCalibrationParametersGyro));
					objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.GYRO_Y,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.GYRO_CAL_UNIT,gyroCalibratedData[1],mDefaultCalibrationParametersGyro));
					objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.GYRO_Z,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.GYRO_CAL_UNIT,gyroCalibratedData[2],mDefaultCalibrationParametersGyro));
					gyroscope.x=gyroCalibratedData[0]*Math.PI/180;
					gyroscope.y=gyroCalibratedData[1]*Math.PI/180;
					gyroscope.z=gyroCalibratedData[2]*Math.PI/180;
					calibratedDataUnits[iGyroX]=CHANNEL_UNITS.GYRO_CAL_UNIT;
					calibratedDataUnits[iGyroY]=CHANNEL_UNITS.GYRO_CAL_UNIT;
					calibratedDataUnits[iGyroZ]=CHANNEL_UNITS.GYRO_CAL_UNIT;

//					if (mDefaultCalibrationParametersGyro == true) {
//						objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.GYRO_X,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.GYRO_DEFAULT_CAL_UNIT,gyroCalibratedData[0]));
//						objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.GYRO_Y,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.GYRO_DEFAULT_CAL_UNIT,gyroCalibratedData[1]));
//						objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.GYRO_Z,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.GYRO_DEFAULT_CAL_UNIT,gyroCalibratedData[2]));
//						gyroscope.x=gyroCalibratedData[0]*Math.PI/180;
//						gyroscope.y=gyroCalibratedData[1]*Math.PI/180;
//						gyroscope.z=gyroCalibratedData[2]*Math.PI/180;
//						calibratedDataUnits[iGyroX]=CHANNEL_UNITS.GYRO_DEFAULT_CAL_UNIT;
//						calibratedDataUnits[iGyroY]=CHANNEL_UNITS.GYRO_DEFAULT_CAL_UNIT;
//						calibratedDataUnits[iGyroZ]=CHANNEL_UNITS.GYRO_DEFAULT_CAL_UNIT;
//					} else {
//						objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.GYRO_X,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.GYRO_CAL_UNIT,gyroCalibratedData[0]));
//						objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.GYRO_Y,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.GYRO_CAL_UNIT,gyroCalibratedData[1]));
//						objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.GYRO_Z,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.GYRO_CAL_UNIT,gyroCalibratedData[2]));
//						gyroscope.x=gyroCalibratedData[0]*Math.PI/180;
//						gyroscope.y=gyroCalibratedData[1]*Math.PI/180;
//						gyroscope.z=gyroCalibratedData[2]*Math.PI/180;
//						calibratedDataUnits[iGyroX]=CHANNEL_UNITS.GYRO_CAL_UNIT;
//						calibratedDataUnits[iGyroY]=CHANNEL_UNITS.GYRO_CAL_UNIT;
//						calibratedDataUnits[iGyroZ]=CHANNEL_UNITS.GYRO_CAL_UNIT;
//					} 
					
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
			if (((fwIdentifier == FW_TYPE_BT) && (mEnabledSensors & BTStream.MAG) > 0) 
					|| ((fwIdentifier == FW_TYPE_SD) && (mEnabledSensors & SDLogHeader.MAG) > 0)
					) {
				int iMagX=getSignalIndex(Shimmer3.ObjectClusterSensorName.MAG_X);
				int iMagY=getSignalIndex(Shimmer3.ObjectClusterSensorName.MAG_Y);
				int iMagZ=getSignalIndex(Shimmer3.ObjectClusterSensorName.MAG_Z);
				tempData[0]=(double)newPacketInt[iMagX];
				tempData[1]=(double)newPacketInt[iMagY];
				tempData[2]=(double)newPacketInt[iMagZ];
				objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.MAG_X,new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iMagX]));
				objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.MAG_Y,new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iMagY]));
				objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.MAG_Z,new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iMagZ]));
				uncalibratedData[iMagX]=(double)newPacketInt[iMagX];
				uncalibratedData[iMagY]=(double)newPacketInt[iMagY];
				uncalibratedData[iMagZ]=(double)newPacketInt[iMagZ];
				uncalibratedDataUnits[iMagX]=CHANNEL_UNITS.NO_UNITS;
				uncalibratedDataUnits[iMagY]=CHANNEL_UNITS.NO_UNITS;
				uncalibratedDataUnits[iMagZ]=CHANNEL_UNITS.NO_UNITS;
				if (mEnableCalibration){
					double[] magCalibratedData;
					magCalibratedData=calibrateInertialSensorData(tempData, mAlignmentMatrixMagnetometer, mSensitivityMatrixMagnetometer, mOffsetVectorMagnetometer);
					calibratedData[iMagX]=magCalibratedData[0];
					calibratedData[iMagY]=magCalibratedData[1];
					calibratedData[iMagZ]=magCalibratedData[2];
					
					objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.MAG_X,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MAG_CAL_UNIT,magCalibratedData[0],mDefaultCalibrationParametersMag));
					objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.MAG_Y,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MAG_CAL_UNIT,magCalibratedData[1],mDefaultCalibrationParametersMag));
					objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.MAG_Z,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MAG_CAL_UNIT,magCalibratedData[2],mDefaultCalibrationParametersMag));
					magnetometer.x=magCalibratedData[0];
					magnetometer.y=magCalibratedData[1];
					magnetometer.z=magCalibratedData[2];
					calibratedDataUnits[iMagX]=CHANNEL_UNITS.MAG_CAL_UNIT;
					calibratedDataUnits[iMagY]=CHANNEL_UNITS.MAG_CAL_UNIT;
					calibratedDataUnits[iMagZ]=CHANNEL_UNITS.MAG_CAL_UNIT;

//					if (mDefaultCalibrationParametersMag == true) {
//						objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.MAG_X,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MAG_DEFAULT_CAL_UNIT,magCalibratedData[0]));
//						objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.MAG_Y,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MAG_DEFAULT_CAL_UNIT,magCalibratedData[1]));
//						objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.MAG_Z,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MAG_DEFAULT_CAL_UNIT,magCalibratedData[2]));
//						magnetometer.x=magCalibratedData[0];
//						magnetometer.y=magCalibratedData[1];
//						magnetometer.z=magCalibratedData[2];
//						calibratedDataUnits[iMagX]=CHANNEL_UNITS.MAG_DEFAULT_CAL_UNIT;
//						calibratedDataUnits[iMagY]=CHANNEL_UNITS.MAG_DEFAULT_CAL_UNIT;
//						calibratedDataUnits[iMagZ]=CHANNEL_UNITS.MAG_DEFAULT_CAL_UNIT;
//					} else {
//						objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.MAG_X,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MAG_CAL_UNIT,magCalibratedData[0]));
//						objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.MAG_Y,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MAG_CAL_UNIT,magCalibratedData[1]));
//						objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.MAG_Z,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MAG_CAL_UNIT,magCalibratedData[2]));
//						magnetometer.x=magCalibratedData[0];
//						magnetometer.y=magCalibratedData[1];
//						magnetometer.z=magCalibratedData[2];
//						calibratedDataUnits[iMagX]=CHANNEL_UNITS.MAG_CAL_UNIT;
//						calibratedDataUnits[iMagY]=CHANNEL_UNITS.MAG_CAL_UNIT;
//						calibratedDataUnits[iMagZ]=CHANNEL_UNITS.MAG_CAL_UNIT;
//					}
				}
			}

			if (((fwIdentifier == FW_TYPE_BT) && (mEnabledSensors & BTStream.BATTERY) > 0) 
					|| ((fwIdentifier == FW_TYPE_SD) && (mEnabledSensors & SDLogHeader.BATTERY) > 0)
					) {
				int iBatt = getSignalIndex(Shimmer3.ObjectClusterSensorName.BATTERY);
				tempData[0] = (double)newPacketInt[iBatt];
				objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.BATTERY,new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iBatt]));
				uncalibratedData[iBatt]=(double)newPacketInt[iBatt];
				uncalibratedDataUnits[iBatt]=CHANNEL_UNITS.NO_UNITS;
				if (mEnableCalibration){
					calibratedData[iBatt]=calibrateU12AdcValue(tempData[0],0,3,1)*2;
					calibratedDataUnits[iBatt] = CHANNEL_UNITS.MILLIVOLTS;
					objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.BATTERY,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calibratedData[iBatt]));
					mVSenseBattMA.addValue(calibratedData[iBatt]);
					checkBattery();
				}
			}
			if (((fwIdentifier == FW_TYPE_BT) && (mEnabledSensors & BTStream.EXT_EXP_A7) > 0) 
					|| ((fwIdentifier == FW_TYPE_SD) && (mEnabledSensors & SDLogHeader.EXT_EXP_A7) > 0)
					) {
				int iA7 = getSignalIndex(Shimmer3.ObjectClusterSensorName.EXT_EXP_ADC_A7);
				tempData[0] = (double)newPacketInt[iA7];
				objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.EXT_EXP_ADC_A7,new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iA7]));
				uncalibratedData[iA7]=(double)newPacketInt[iA7];
				uncalibratedDataUnits[iA7]=CHANNEL_UNITS.NO_UNITS;
				if (mEnableCalibration){
					calibratedData[iA7]=calibrateU12AdcValue(tempData[0],0,3,1);
					calibratedDataUnits[iA7] = CHANNEL_UNITS.MILLIVOLTS;
					objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.EXT_EXP_ADC_A7,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calibratedData[iA7]));
				}
			}
			if (((fwIdentifier == FW_TYPE_BT) && (mEnabledSensors & BTStream.EXT_EXP_A6) > 0) 
					|| ((fwIdentifier == FW_TYPE_SD) && (mEnabledSensors & SDLogHeader.EXT_EXP_A6) > 0)
					) {
				int iA6 = getSignalIndex(Shimmer3.ObjectClusterSensorName.EXT_EXP_ADC_A6);
				tempData[0] = (double)newPacketInt[iA6];
				objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.EXT_EXP_ADC_A6,new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iA6]));
				uncalibratedData[iA6]=(double)newPacketInt[iA6];
				uncalibratedDataUnits[iA6]=CHANNEL_UNITS.NO_UNITS;
				if (mEnableCalibration){
					calibratedData[iA6]=calibrateU12AdcValue(tempData[0],0,3,1);
					calibratedDataUnits[iA6] = CHANNEL_UNITS.MILLIVOLTS;
					objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.EXT_EXP_ADC_A6,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calibratedData[iA6]));
				}
			}
			if (((fwIdentifier == FW_TYPE_BT) && (mEnabledSensors & BTStream.EXT_EXP_A15) > 0) 
					|| ((fwIdentifier == FW_TYPE_SD) && (mEnabledSensors & SDLogHeader.EXT_EXP_A15) > 0)
					) {
				int iA15 = getSignalIndex(Shimmer3.ObjectClusterSensorName.EXT_EXP_ADC_A15);
				tempData[0] = (double)newPacketInt[iA15];
				objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.EXT_EXP_ADC_A15,new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iA15]));
				uncalibratedData[iA15]=(double)newPacketInt[iA15];
				uncalibratedDataUnits[iA15]=CHANNEL_UNITS.NO_UNITS;

				if (mEnableCalibration){
					calibratedData[iA15]=calibrateU12AdcValue(tempData[0],0,3,1);
					calibratedDataUnits[iA15] = CHANNEL_UNITS.MILLIVOLTS;
					objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.EXT_EXP_ADC_A15,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calibratedData[iA15]));
				}
			}
			if (((fwIdentifier == FW_TYPE_BT) && (mEnabledSensors & BTStream.INT_EXP_A1) > 0) 
					|| ((fwIdentifier == FW_TYPE_SD) && (mEnabledSensors & SDLogHeader.INT_EXP_A1) > 0)
					) {
					int iA1 = getSignalIndex(Shimmer3.ObjectClusterSensorName.INT_EXP_ADC_A1);
					tempData[0] = (double)newPacketInt[iA1];
					String sensorName = Shimmer3.ObjectClusterSensorName.INT_EXP_ADC_A1;
					
					//to Support derived sensor renaming
					if (isDerivedSensorsSupported()){
						//change name based on derived sensor value
						if ((mDerivedSensors & SDLogHeaderDerivedSensors.PPG2_1_14)>0){
							sensorName = Shimmer3.ObjectClusterSensorName.PPG2_A1;
						}else if ((mDerivedSensors & SDLogHeaderDerivedSensors.RES_AMP)>0){
							sensorName = Shimmer3.ObjectClusterSensorName.RESISTANCE_AMP;
						}else if((mDerivedSensors & SDLogHeaderDerivedSensors.SKIN_TEMP)>0){
							sensorName = Shimmer3.ObjectClusterSensorName.SKIN_TEMPERATURE_PROBE;
						}
						
					}
					sensorNames[iA1]=sensorName;
					objectCluster.mPropertyCluster.put(sensorName,new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iA1]));
					uncalibratedData[iA1]=(double)newPacketInt[iA1];
					uncalibratedDataUnits[iA1]=CHANNEL_UNITS.NO_UNITS;
					if (mEnableCalibration){
						if((mDerivedSensors & SDLogHeaderDerivedSensors.SKIN_TEMP)>0){
							calibratedData[iA1]=calibratePhillipsSkinTemperatureData(tempData[0]);
							calibratedDataUnits[iA1] = CHANNEL_UNITS.DEGREES_CELSUIS;
							objectCluster.mPropertyCluster.put(sensorName,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.DEGREES_CELSUIS,calibratedData[iA1]));
						}
						else{
							calibratedData[iA1]=calibrateU12AdcValue(tempData[0],0,3,1);
							calibratedDataUnits[iA1] = CHANNEL_UNITS.MILLIVOLTS;
							objectCluster.mPropertyCluster.put(sensorName,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calibratedData[iA1]));
						}
					}
			}
			if (((fwIdentifier == FW_TYPE_BT) && (mEnabledSensors & BTStream.INT_EXP_A12) > 0) 
					|| ((fwIdentifier == FW_TYPE_SD) && (mEnabledSensors & SDLogHeader.INT_EXP_A12) > 0)
					) {
				int iA12 = getSignalIndex(Shimmer3.ObjectClusterSensorName.INT_EXP_ADC_A12);
				tempData[0] = (double)newPacketInt[iA12];
				String sensorName = Shimmer3.ObjectClusterSensorName.INT_EXP_ADC_A12;
				//to Support derived sensor renaming
				if (isDerivedSensorsSupported()){
					//change name based on derived sensor value
					if ((mDerivedSensors & SDLogHeaderDerivedSensors.PPG_12_13)>0){
						sensorName = Shimmer3.ObjectClusterSensorName.PPG_A12;
					} else if ((mDerivedSensors & SDLogHeaderDerivedSensors.PPG1_12_13)>0){
						sensorName = Shimmer3.ObjectClusterSensorName.PPG1_A12;
					}
				}
				
				sensorNames[iA12]=sensorName;
				objectCluster.mPropertyCluster.put(sensorName,new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iA12]));
				uncalibratedData[iA12]=(double)newPacketInt[iA12];
				uncalibratedDataUnits[iA12]=CHANNEL_UNITS.NO_UNITS;
				if (mEnableCalibration){
					calibratedData[iA12]=calibrateU12AdcValue(tempData[0],0,3,1);
					objectCluster.mPropertyCluster.put(sensorName,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calibratedData[iA12]));
					calibratedDataUnits[iA12] = CHANNEL_UNITS.MILLIVOLTS;

				}
			}
			if (((fwIdentifier == FW_TYPE_BT) && (mEnabledSensors & BTStream.INT_EXP_A13) > 0) 
					|| ((fwIdentifier == FW_TYPE_SD) && (mEnabledSensors & SDLogHeader.INT_EXP_A13) > 0)
					) {
				int iA13 = getSignalIndex(Shimmer3.ObjectClusterSensorName.INT_EXP_ADC_A13);
				tempData[0] = (double)newPacketInt[iA13];
				String sensorName = Shimmer3.ObjectClusterSensorName.INT_EXP_ADC_A13;
				//to Support derived sensor renaming
				if (isDerivedSensorsSupported()){
					//change name based on derived sensor value
					if ((mDerivedSensors & SDLogHeaderDerivedSensors.PPG_12_13)>0){
						sensorName = Shimmer3.ObjectClusterSensorName.PPG_A13;
					} else if ((mDerivedSensors & SDLogHeaderDerivedSensors.PPG1_12_13)>0){
						sensorName = Shimmer3.ObjectClusterSensorName.PPG1_A13;
					}
				}
				
				sensorNames[iA13]=sensorName;
				objectCluster.mPropertyCluster.put(sensorName,new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iA13]));
				uncalibratedData[iA13]=(double)newPacketInt[iA13];
				uncalibratedDataUnits[iA13]=CHANNEL_UNITS.NO_UNITS;
				if (mEnableCalibration){
					calibratedData[iA13]=calibrateU12AdcValue(tempData[0],0,3,1);
					objectCluster.mPropertyCluster.put(sensorName,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calibratedData[iA13]));
					calibratedDataUnits[iA13] = CHANNEL_UNITS.MILLIVOLTS;
				}
			}
			if (((fwIdentifier == FW_TYPE_BT) && (mEnabledSensors & BTStream.INT_EXP_A14) > 0) 
					|| ((fwIdentifier == FW_TYPE_SD) && (mEnabledSensors & SDLogHeader.INT_EXP_A14) > 0)
					) {
				int iA14 = getSignalIndex(Shimmer3.ObjectClusterSensorName.INT_EXP_ADC_A14);
				tempData[0] = (double)newPacketInt[iA14];
				String sensorName = Shimmer3.ObjectClusterSensorName.INT_EXP_ADC_A14;
				//to Support derived sensor renaming
				if (isDerivedSensorsSupported()){
					//change name based on derived sensor value
					if ((mDerivedSensors & SDLogHeaderDerivedSensors.PPG2_1_14)>0){
						sensorName = Shimmer3.ObjectClusterSensorName.PPG2_A14;
					}
				}
				sensorNames[iA14]=sensorName;
				objectCluster.mPropertyCluster.put(sensorName,new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iA14]));
				uncalibratedData[iA14]=(double)newPacketInt[iA14];
				uncalibratedDataUnits[iA14]=CHANNEL_UNITS.NO_UNITS;
				if (mEnableCalibration){
					calibratedData[iA14]=calibrateU12AdcValue(tempData[0],0,3,1);
					objectCluster.mPropertyCluster.put(sensorName,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calibratedData[iA14]));
					calibratedDataUnits[iA14] = CHANNEL_UNITS.MILLIVOLTS;
				}
			}
			//((fwIdentifier == FW_IDEN_BTSTREAM) && (mEnabledSensors & BTStream.ACCEL_WR) > 0) 
			//|| ((fwIdentifier == FW_IDEN_SD) && (mEnabledSensors & SDLogHeader.ACCEL_WR) > 0)




			if (((fwIdentifier == FW_TYPE_BT)&&((mEnabledSensors & BTStream.ACCEL_LN) > 0 || (mEnabledSensors & BTStream.ACCEL_WR) > 0) && ((mEnabledSensors & BTStream.GYRO) > 0) && ((mEnabledSensors & BTStream.MAG) > 0) && mOrientationEnabled )
					||((fwIdentifier == FW_TYPE_SD)&&((mEnabledSensors & SDLogHeader.ACCEL_LN) > 0 || (mEnabledSensors & SDLogHeader.ACCEL_WR) > 0) && ((mEnabledSensors & SDLogHeader.GYRO) > 0) && ((mEnabledSensors & SDLogHeader.MAG) > 0) && mOrientationEnabled )){
				if (mEnableCalibration){
					if (mOrientationAlgo==null){
						mOrientationAlgo = new GradDes3DOrientation(0.4, (double)1/mShimmerSamplingRate, 1, 0, 0,0);
					}
					Quaternion q = mOrientationAlgo.update(accelerometer.x,accelerometer.y,accelerometer.z, gyroscope.x,gyroscope.y,gyroscope.z, magnetometer.x,magnetometer.y,magnetometer.z);					double theta, Rx, Ry, Rz, rho;
					rho = Math.acos(q.q1);
					theta = rho * 2;
					Rx = q.q2 / Math.sin(rho);
					Ry = q.q3 / Math.sin(rho);
					Rz = q.q4 / Math.sin(rho);
					objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.AXIS_ANGLE_A,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL,theta));
					objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.AXIS_ANGLE_X,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL,Rx));
					objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.AXIS_ANGLE_Y,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL,Ry));
					objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.AXIS_ANGLE_Z,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL,Rz));
					objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.QUAT_MADGE_9DOF_W,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL,q.q1));
					objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.QUAT_MADGE_9DOF_X,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL,q.q2));
					objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.QUAT_MADGE_9DOF_Y,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL,q.q3));
					objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.QUAT_MADGE_9DOF_Z,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL,q.q4));
				}
			}

			if (((fwIdentifier == FW_TYPE_BT) && (mEnabledSensors & BTStream.EXG1_24BIT) > 0) 
					|| ((fwIdentifier == FW_TYPE_SD) && (mEnabledSensors & SDLogHeader.EXG1_24BIT) > 0)
					){
				int iexg1ch1 = getSignalIndex(Shimmer3.ObjectClusterSensorName.EXG1_CH1_24BIT);
				int iexg1ch2 = getSignalIndex(Shimmer3.ObjectClusterSensorName.EXG1_CH2_24BIT);
				int iexg1sta = getSignalIndex(Shimmer3.ObjectClusterSensorName.EXG1_STATUS);
				double exg1ch1 = (double)newPacketInt[iexg1ch1];
				double exg1ch2 = (double)newPacketInt[iexg1ch2];
				double exg1sta = (double)newPacketInt[iexg1sta];
				objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.EXG1_STATUS,new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg1sta));
				uncalibratedData[iexg1sta]=(double)newPacketInt[iexg1sta];
				uncalibratedData[iexg1ch1]=(double)newPacketInt[iexg1ch1];
				uncalibratedData[iexg1ch2]=(double)newPacketInt[iexg1ch2];
				uncalibratedDataUnits[iexg1sta]=CHANNEL_UNITS.NO_UNITS;
				uncalibratedDataUnits[iexg1ch1]=CHANNEL_UNITS.NO_UNITS;
				uncalibratedDataUnits[iexg1ch2]=CHANNEL_UNITS.NO_UNITS;
				if (mEnableCalibration){
					double calexg1ch1 = exg1ch1 *(((2.42*1000)/mEXG1CH1GainValue)/(Math.pow(2,23)-1));
					double calexg1ch2 = exg1ch2 *(((2.42*1000)/mEXG1CH2GainValue)/(Math.pow(2,23)-1));
					calibratedData[iexg1ch1]=calexg1ch1;
					calibratedData[iexg1ch2]=calexg1ch2;
					calibratedData[iexg1sta]=(double)newPacketInt[iexg1sta];
					calibratedDataUnits[iexg1sta]=CHANNEL_UNITS.NO_UNITS;
					calibratedDataUnits[iexg1ch1]=CHANNEL_UNITS.MILLIVOLTS;
					calibratedDataUnits[iexg1ch2]=CHANNEL_UNITS.MILLIVOLTS;
					if (isEXGUsingDefaultECGConfiguration()||isEXGUsingDefaultRespirationConfiguration()){
						sensorNames[iexg1ch1]=Shimmer3.ObjectClusterSensorName.ECG_LL_RA_24BIT;
						sensorNames[iexg1ch2]=Shimmer3.ObjectClusterSensorName.ECG_LA_RA_24BIT;
						objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.ECG_LL_RA_24BIT,new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg1ch1));
						objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.ECG_LA_RA_24BIT,new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg1ch2));
						objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.ECG_LL_RA_24BIT,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calexg1ch1));
						objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.ECG_LA_RA_24BIT,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calexg1ch2));
					} 
					else if (isEXGUsingDefaultEMGConfiguration()){
						sensorNames[iexg1ch1]=Shimmer3.ObjectClusterSensorName.EMG_CH1_24BIT;
						sensorNames[iexg1ch2]=Shimmer3.ObjectClusterSensorName.EMG_CH2_24BIT;
						objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.EMG_CH1_24BIT,new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg1ch1));
						objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.EMG_CH2_24BIT,new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg1ch2));
						objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.EMG_CH1_24BIT,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calexg1ch1));
						objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.EMG_CH2_24BIT,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calexg1ch2));
					} 
					else if (isEXGUsingDefaultTestSignalConfiguration()){
						sensorNames[iexg1ch1]=Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP1_CH1_24BIT;
						sensorNames[iexg1ch2]=Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP1_CH2_24BIT;
						objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP1_CH1_24BIT,new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg1ch1));
						objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP1_CH2_24BIT,new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg1ch2));
						objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP1_CH1_24BIT,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calexg1ch1));
						objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP1_CH2_24BIT,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calexg1ch2));
					} 
					else {
						sensorNames[iexg1ch1]=Shimmer3.ObjectClusterSensorName.EXG1_CH1_24BIT;
						sensorNames[iexg1ch2]=Shimmer3.ObjectClusterSensorName.EXG1_CH2_24BIT;
						objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.EXG1_CH1_24BIT,new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg1ch1));
						objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.EXG1_CH2_24BIT,new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg1ch2));
						objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.EXG1_CH1_24BIT,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calexg1ch1));
						objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.EXG1_CH2_24BIT,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calexg1ch2));
					}
				}
			}
			if (((fwIdentifier == FW_TYPE_BT) && (mEnabledSensors & BTStream.EXG2_24BIT) > 0) 
					|| ((fwIdentifier == FW_TYPE_SD) && (mEnabledSensors & SDLogHeader.EXG2_24BIT) > 0)
					){
				int iexg2ch1 = getSignalIndex(Shimmer3.ObjectClusterSensorName.EXG2_CH1_24BIT);
				int iexg2ch2 = getSignalIndex(Shimmer3.ObjectClusterSensorName.EXG2_CH2_24BIT);
				int iexg2sta = getSignalIndex(Shimmer3.ObjectClusterSensorName.EXG2_STATUS);
				double exg2ch1 = (double)newPacketInt[iexg2ch1];
				double exg2ch2 = (double)newPacketInt[iexg2ch2];
				double exg2sta = (double)newPacketInt[iexg2sta];
				objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.EXG2_STATUS,new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg2sta));
				uncalibratedData[iexg2sta]=(double)newPacketInt[iexg2sta];
				uncalibratedData[iexg2ch1]=(double)newPacketInt[iexg2ch1];
				uncalibratedData[iexg2ch2]=(double)newPacketInt[iexg2ch2];
				uncalibratedDataUnits[iexg2sta]=CHANNEL_UNITS.NO_UNITS;
				uncalibratedDataUnits[iexg2ch1]=CHANNEL_UNITS.NO_UNITS;
				uncalibratedDataUnits[iexg2ch2]=CHANNEL_UNITS.NO_UNITS;
				if (mEnableCalibration){
					double calexg2ch1 = exg2ch1 *(((2.42*1000)/mEXG2CH1GainValue)/(Math.pow(2,23)-1));
					double calexg2ch2 = exg2ch2 *(((2.42*1000)/mEXG2CH2GainValue)/(Math.pow(2,23)-1));
					calibratedData[iexg2ch1]=calexg2ch1;
					calibratedData[iexg2ch2]=calexg2ch2;
					calibratedData[iexg2sta]=(double)newPacketInt[iexg2sta];
					calibratedDataUnits[iexg2sta]=CHANNEL_UNITS.NO_UNITS;
					calibratedDataUnits[iexg2ch1]=CHANNEL_UNITS.MILLIVOLTS;
					calibratedDataUnits[iexg2ch2]=CHANNEL_UNITS.MILLIVOLTS;
					if (isEXGUsingDefaultECGConfiguration()||isEXGUsingDefaultRespirationConfiguration()){
						sensorNames[iexg2ch1]=Shimmer3.ObjectClusterSensorName.EXG2_CH1_24BIT;
						sensorNames[iexg2ch2]=Shimmer3.ObjectClusterSensorName.ECG_VX_RL_24BIT;
						objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.EXG2_CH1_24BIT,new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg2ch1));
						objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.ECG_VX_RL_24BIT,new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg2ch2));
						objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.EXG2_CH1_24BIT,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calexg2ch1));
						objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.ECG_VX_RL_24BIT,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calexg2ch2));
					}
					else if (isEXGUsingDefaultEMGConfiguration()){
						sensorNames[iexg2ch1]=Shimmer3.ObjectClusterSensorName.EXG2_CH1_24BIT;
						sensorNames[iexg2ch2]=Shimmer3.ObjectClusterSensorName.EXG2_CH2_24BIT;
						objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.EXG2_CH1_24BIT,new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,0));
						objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.EXG2_CH2_24BIT,new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,0));
						objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.EXG2_CH1_24BIT,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,0));
						objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.EXG2_CH2_24BIT,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,0));
					} 
					else if (isEXGUsingDefaultTestSignalConfiguration()){
						sensorNames[iexg2ch1]=Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP2_CH1_24BIT;
						sensorNames[iexg2ch2]=Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP2_CH2_24BIT;
						objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP2_CH1_24BIT,new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg2ch1));
						objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP2_CH2_24BIT,new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg2ch2));
						objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP2_CH1_24BIT,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calexg2ch1));
						objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP2_CH2_24BIT,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calexg2ch2));
					} 
					else {
						sensorNames[iexg2ch1]=Shimmer3.ObjectClusterSensorName.EXG2_CH1_24BIT;
						sensorNames[iexg2ch2]=Shimmer3.ObjectClusterSensorName.EXG2_CH2_24BIT;
						objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.EXG2_CH1_24BIT,new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg2ch1));
						objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.EXG2_CH2_24BIT,new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg2ch2));
						objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.EXG2_CH1_24BIT,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calexg2ch1));
						objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.EXG2_CH2_24BIT,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calexg2ch2));	
					}
				}
			}
			
			if (((fwIdentifier == FW_TYPE_BT) && (mEnabledSensors & BTStream.EXG1_16BIT) > 0) 
					|| ((fwIdentifier == FW_TYPE_SD) && (mEnabledSensors & SDLogHeader.EXG1_16BIT) > 0)
					){
				int iexg1ch1 = getSignalIndex(Shimmer3.ObjectClusterSensorName.EXG1_CH1_16BIT);
				int iexg1ch2 = getSignalIndex(Shimmer3.ObjectClusterSensorName.EXG1_CH2_16BIT);
				int iexg1sta = getSignalIndex(Shimmer3.ObjectClusterSensorName.EXG1_STATUS);
				double exg1ch1 = (double)newPacketInt[iexg1ch1];
				double exg1ch2 = (double)newPacketInt[iexg1ch2];
				double exg1sta = (double)newPacketInt[iexg1sta];
				objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.EXG1_STATUS,new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg1sta));
				uncalibratedData[iexg1sta]=(double)newPacketInt[iexg1sta];
				uncalibratedData[iexg1ch1]=(double)newPacketInt[iexg1ch1];
				uncalibratedData[iexg1ch2]=(double)newPacketInt[iexg1ch2];
				uncalibratedDataUnits[iexg1sta]=CHANNEL_UNITS.NO_UNITS;
				uncalibratedDataUnits[iexg1ch1]=CHANNEL_UNITS.NO_UNITS;
				uncalibratedDataUnits[iexg1ch2]=CHANNEL_UNITS.NO_UNITS;
				if (mEnableCalibration){
					double calexg1ch1 = exg1ch1 *(((2.42*1000)/(mEXG1CH1GainValue*2))/(Math.pow(2,15)-1));
					double calexg1ch2 = exg1ch2 *(((2.42*1000)/(mEXG1CH2GainValue*2))/(Math.pow(2,15)-1));
					calibratedData[iexg1ch1]=calexg1ch1;
					calibratedData[iexg1ch2]=calexg1ch2;
					calibratedData[iexg1sta]=(double)newPacketInt[iexg1sta];
					calibratedDataUnits[iexg1sta]=CHANNEL_UNITS.NO_UNITS;
					calibratedDataUnits[iexg1ch1]=CHANNEL_UNITS.MILLIVOLTS;
					calibratedDataUnits[iexg1ch2]=CHANNEL_UNITS.MILLIVOLTS;
					if (isEXGUsingDefaultECGConfiguration()||isEXGUsingDefaultRespirationConfiguration()){
						sensorNames[iexg1ch1]=Shimmer3.ObjectClusterSensorName.ECG_LL_RA_16BIT;
						sensorNames[iexg1ch2]=Shimmer3.ObjectClusterSensorName.ECG_LA_RA_16BIT;
						objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.ECG_LL_RA_16BIT,new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg1ch1));
						objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.ECG_LA_RA_16BIT,new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg1ch2));
						objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.ECG_LL_RA_16BIT,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calexg1ch1));
						objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.ECG_LA_RA_16BIT,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calexg1ch2));
					} 
					else if (isEXGUsingDefaultEMGConfiguration()){
						sensorNames[iexg1ch1]=Shimmer3.ObjectClusterSensorName.EMG_CH1_16BIT;
						sensorNames[iexg1ch2]=Shimmer3.ObjectClusterSensorName.EMG_CH2_16BIT;
						objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.EMG_CH1_16BIT,new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg1ch1));
						objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.EMG_CH2_16BIT,new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg1ch2));
						objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.EMG_CH1_16BIT,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calexg1ch1));
						objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.EMG_CH2_16BIT,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calexg1ch2));
					} 
					else if (isEXGUsingDefaultTestSignalConfiguration()){
						sensorNames[iexg1ch1]=Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP1_CH1_16BIT;
						sensorNames[iexg1ch2]=Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP1_CH2_16BIT;
						objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP1_CH1_16BIT,new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg1ch1));
						objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP1_CH2_16BIT,new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg1ch2));
						objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP1_CH1_16BIT,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calexg1ch1));
						objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP1_CH2_16BIT,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calexg1ch2));
					} 
					else {
						sensorNames[iexg1ch1]=Shimmer3.ObjectClusterSensorName.EXG1_CH1_16BIT;
						sensorNames[iexg1ch2]=Shimmer3.ObjectClusterSensorName.EXG1_CH2_16BIT;
						objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.EXG1_CH1_16BIT,new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg1ch1));
						objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.EXG1_CH2_16BIT,new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg1ch2));
						objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.EXG1_CH1_16BIT,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calexg1ch1));
						objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.EXG1_CH2_16BIT,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calexg1ch2));
					}
				}
			}
			if (((fwIdentifier == FW_TYPE_BT) && (mEnabledSensors & BTStream.EXG2_16BIT) > 0) 
					|| ((fwIdentifier == FW_TYPE_SD) && (mEnabledSensors & SDLogHeader.EXG2_16BIT) > 0)
					){
				int iexg2ch1 = getSignalIndex(Shimmer3.ObjectClusterSensorName.EXG2_CH1_16BIT);
				int iexg2ch2 = getSignalIndex(Shimmer3.ObjectClusterSensorName.EXG2_CH2_16BIT);
				int iexg2sta = getSignalIndex(Shimmer3.ObjectClusterSensorName.EXG2_STATUS);
				double exg2ch1 = (double)newPacketInt[iexg2ch1];
				double exg2ch2 = (double)newPacketInt[iexg2ch2];
				double exg2sta = (double)newPacketInt[iexg2sta];
				objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.EXG2_STATUS,new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg2sta));
				uncalibratedData[iexg2sta]=(double)newPacketInt[iexg2sta];
				uncalibratedData[iexg2ch1]=(double)newPacketInt[iexg2ch1];
				uncalibratedData[iexg2ch2]=(double)newPacketInt[iexg2ch2];
				uncalibratedDataUnits[iexg2sta]=CHANNEL_UNITS.NO_UNITS;
				uncalibratedDataUnits[iexg2ch1]=CHANNEL_UNITS.NO_UNITS;
				uncalibratedDataUnits[iexg2ch2]=CHANNEL_UNITS.NO_UNITS;
				if (mEnableCalibration){
					double calexg2ch1 = ((exg2ch1)) *(((2.42*1000)/(mEXG2CH1GainValue*2))/(Math.pow(2,15)-1));
					double calexg2ch2 = ((exg2ch2)) *(((2.42*1000)/(mEXG2CH2GainValue*2))/(Math.pow(2,15)-1));
					calibratedData[iexg2ch1]=calexg2ch1;
					calibratedData[iexg2ch2]=calexg2ch2;
					calibratedData[iexg2sta]=(double)newPacketInt[iexg2sta];
					calibratedDataUnits[iexg2sta]=CHANNEL_UNITS.NO_UNITS;
					calibratedDataUnits[iexg2ch1]=CHANNEL_UNITS.MILLIVOLTS;
					calibratedDataUnits[iexg2ch2]=CHANNEL_UNITS.MILLIVOLTS;
					if (isEXGUsingDefaultECGConfiguration()){
//						sensorNames[iexg2ch1]=Shimmer3.ObjectClusterSensorName.EXG2_CH1_16BIT;
						sensorNames[iexg2ch2]=Shimmer3.ObjectClusterSensorName.ECG_VX_RL_16BIT;
//						objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.EXG2_CH1_16BIT,new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg2ch1));
						objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.ECG_VX_RL_16BIT,new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg2ch2));
//						objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.EXG2_CH1_16BIT,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calexg2ch1));
						objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.ECG_VX_RL_16BIT,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calexg2ch2));
					}
					else if (isEXGUsingDefaultRespirationConfiguration()){
						sensorNames[iexg2ch1]=Shimmer3.ObjectClusterSensorName.ECG_RESP_16BIT;
						sensorNames[iexg2ch2]=Shimmer3.ObjectClusterSensorName.ECG_VX_RL_16BIT;
						objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.ECG_RESP_16BIT,new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg2ch1));
						objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.ECG_VX_RL_16BIT,new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg2ch2));
						objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.ECG_RESP_16BIT,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calexg2ch1));
						objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.ECG_VX_RL_16BIT,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calexg2ch2));
					}
					else if (isEXGUsingDefaultEMGConfiguration()){
						sensorNames[iexg2ch1]=Shimmer3.ObjectClusterSensorName.EXG2_CH1_16BIT;
						sensorNames[iexg2ch2]=Shimmer3.ObjectClusterSensorName.EXG2_CH2_16BIT;
						objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.EXG2_CH1_16BIT,new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,0));
						objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.EXG2_CH2_16BIT,new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,0));
						objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.EXG2_CH1_16BIT,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,0));
						objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.EXG2_CH2_16BIT,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,0));
					} 
					else if (isEXGUsingDefaultTestSignalConfiguration()){
						sensorNames[iexg2ch1]=Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP2_CH1_16BIT;
						sensorNames[iexg2ch2]=Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP2_CH2_16BIT;
						objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP2_CH1_16BIT,new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg2ch1));
						objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP2_CH2_16BIT,new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg2ch2));
						objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP2_CH1_16BIT,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calexg2ch1));
						objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP2_CH2_16BIT,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calexg2ch2));
					} 
					else {
						sensorNames[iexg2ch1]=Shimmer3.ObjectClusterSensorName.EXG2_CH1_16BIT;
						sensorNames[iexg2ch2]=Shimmer3.ObjectClusterSensorName.EXG2_CH2_16BIT;
						objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.EXG2_CH1_16BIT,new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg2ch1));
						objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.EXG2_CH2_16BIT,new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg2ch1));
						objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.EXG2_CH1_16BIT,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calexg2ch1));
						objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.EXG2_CH2_16BIT,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calexg2ch1));	
					}
				}
			}

			if (((fwIdentifier == FW_TYPE_BT) && (mEnabledSensors & BTStream.BMP180) > 0) 
					|| ((fwIdentifier == FW_TYPE_SD) && (mEnabledSensors & SDLogHeader.BMP180) > 0)
					){
				int iUT = getSignalIndex(Shimmer3.ObjectClusterSensorName.TEMPERATURE_BMP180);
				int iUP = getSignalIndex(Shimmer3.ObjectClusterSensorName.PRESSURE_BMP180);
				double UT = (double)newPacketInt[iUT];
				double UP = (double)newPacketInt[iUP];
				UP=UP/Math.pow(2,8-mPressureResolution);
				objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.PRESSURE_BMP180,new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,UP));
				objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.TEMPERATURE_BMP180,new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,UT));
				uncalibratedData[iUT]=(double)newPacketInt[iUT];
				uncalibratedData[iUP]=(double)newPacketInt[iUP];
				uncalibratedDataUnits[iUT]=CHANNEL_UNITS.NO_UNITS;
				uncalibratedDataUnits[iUP]=CHANNEL_UNITS.NO_UNITS;
				if (mEnableCalibration){
					double[] bmp180caldata= calibratePressureSensorData(UP,UT);
					objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.PRESSURE_BMP180,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.KPASCAL,bmp180caldata[0]/1000));
					objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.TEMPERATURE_BMP180,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.DEGREES_CELSUIS,bmp180caldata[1]));
					calibratedData[iUT]=bmp180caldata[1];
					calibratedData[iUP]=bmp180caldata[0]/1000;
					calibratedDataUnits[iUT]=CHANNEL_UNITS.DEGREES_CELSUIS;
					calibratedDataUnits[iUP]=CHANNEL_UNITS.KPASCAL;
				}
			}

			if (((fwIdentifier == FW_TYPE_BT) && (mEnabledSensors & BTStream.BRIDGE_AMP) > 0) 
					|| ((fwIdentifier == FW_TYPE_SD) && (mEnabledSensors & SDLogHeader.BRIDGE_AMP) > 0)
					) {
				int iBAHigh = getSignalIndex(Shimmer3.ObjectClusterSensorName.BRIDGE_AMP_HIGH);
				int iBALow = getSignalIndex(Shimmer3.ObjectClusterSensorName.BRIDGE_AMP_LOW);
				objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.BRIDGE_AMP_HIGH,new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iBAHigh]));
				objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.BRIDGE_AMP_LOW,new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iBALow]));
				uncalibratedData[iBAHigh]=(double)newPacketInt[iBAHigh];
				uncalibratedData[iBALow]=(double)newPacketInt[iBALow];
				uncalibratedDataUnits[iBAHigh]=CHANNEL_UNITS.NO_UNITS;
				uncalibratedDataUnits[iBALow]=CHANNEL_UNITS.NO_UNITS;
				if (mEnableCalibration){
					tempData[0] = (double)newPacketInt[iBAHigh];
					tempData[1] = (double)newPacketInt[iBALow];
					calibratedData[iBAHigh]=calibrateU12AdcValue(tempData[0],60,3,551);
					calibratedData[iBALow]=calibrateU12AdcValue(tempData[1],1950,3,183.7);
					calibratedDataUnits[iBAHigh]=CHANNEL_UNITS.MILLIVOLTS;
					calibratedDataUnits[iBALow]=CHANNEL_UNITS.MILLIVOLTS;
					objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.BRIDGE_AMP_HIGH,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calibratedData[iBAHigh]));
					objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.BRIDGE_AMP_LOW,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calibratedData[iBALow]));	
				}
			}

			if (((fwIdentifier == FW_TYPE_BT) && (mEnabledSensors & BTStream.GSR) > 0) 
					|| ((fwIdentifier == FW_TYPE_SD) && (mEnabledSensors & SDLogHeader.GSR) > 0)
					) {
				int iGSR = getSignalIndex(Shimmer3.ObjectClusterSensorName.GSR);
				double p1=0,p2=0;//,p3=0,p4=0,p5=0;
				if (fwIdentifier == FW_TYPE_SD && mFirmwareVersionMajor ==0 && mFirmwareVersionMinor==9){
					tempData[0] = (double)newPacketInt[iGSR];
					int gsrUncalibratedData = ((int)tempData[0] & 4095); 
					int newGSRRange = -1; // initialized to -1 so it will only come into play if mGSRRange = 4  

					/*
					 * 	for i = 2:length(range)
    				 *		if range(i-1)~=range(i)
        			 *			if abs(gsrValue(i-1)-gsrValue(i))< 300
            		 *				range(i) = range(i-1);
        			 *			end
    				 *		end
					 *	end
					 */
					
					if (mGSRRange==4){
						newGSRRange=(49152 & (int)tempData[0])>>14;
						if (mPastGSRFirstTime){
							mPastGSRRange = newGSRRange;
							mPastGSRFirstTime = false;
						}
						if (newGSRRange != mPastGSRRange)
						{
							
							if (Math.abs(mPastGSRUncalibratedValue-gsrUncalibratedData)<300){
								newGSRRange = mPastGSRRange;
							} else {
								mPastGSRRange = newGSRRange;
							}
							mPastGSRUncalibratedValue = gsrUncalibratedData;
						}
						
					}
					if (mGSRRange==0 || newGSRRange==0) { //Note that from FW 1.0 onwards the MSB of the GSR data contains the range
						// the polynomial function used for calibration has been deprecated, it is replaced with a linear function
						if (mHardwareVersion!=HW_ID.SHIMMER_3){
							p1 = 0.0373;
							p2 = -24.9915;
						} else {
							//p1 = 0.0363;
							//p2 = -24.8617;
							p1 = 0.0373;
							p2 = -24.9915;
						}
					} else if (mGSRRange==1 || newGSRRange==1) {
						if (mHardwareVersion!=HW_ID.SHIMMER_3){
							p1 = 0.0054;
							p2 = -3.5194;
						} else {
							//p1 = 0.0051;
							//p2 = -3.8357;
							p1 = 0.0054;
							p2 = -3.5194;
						}
					} else if (mGSRRange==2 || newGSRRange==2) {
						if (mHardwareVersion!=HW_ID.SHIMMER_3){
							p1 = 0.0015;
							p2 = -1.0163;
						} else {
							//p1 = 0.0015;
							//p2 = -1.0067;
							p1 = 0.0015;
							p2 = -1.0163;
						}
					} else if (mGSRRange==3  || newGSRRange==3) {
						if (mHardwareVersion!=HW_ID.SHIMMER_3){
							p1 = 4.5580e-04;
							p2 = -0.3014;
						} else {
							//p1 = 4.4513e-04;
							//p2 = -0.3193;
							p1 = 4.5580e-04;
							p2 = -0.3014;
						}
					}
				} else {
					tempData[0] = (double)newPacketInt[iGSR];
					int newGSRRange = -1; // initialized to -1 so it will only come into play if mGSRRange = 4  

					if (mGSRRange==4){
						newGSRRange=(49152 & (int)tempData[0])>>14; 
					}
					if (mGSRRange==0 || newGSRRange==0) { //Note that from FW 1.0 onwards the MSB of the GSR data contains the range
						// the polynomial function used for calibration has been deprecated, it is replaced with a linear function
						if (mHardwareVersion!=HW_ID.SHIMMER_3){
							p1 = 0.0373;
							p2 = -24.9915;

						} else { //Values have been reverted to 2r values
							//p1 = 0.0363;
							//p2 = -24.8617;
							p1 = 0.0373;
							p2 = -24.9915;
						}
					} else if (mGSRRange==1 || newGSRRange==1) {
						if (mHardwareVersion!=HW_ID.SHIMMER_3){
							p1 = 0.0054;
							p2 = -3.5194;
						} else {
							//p1 = 0.0051;
							//p2 = -3.8357;
							p1 = 0.0054;
							p2 = -3.5194;
						}
					} else if (mGSRRange==2 || newGSRRange==2) {
						if (mHardwareVersion!=HW_ID.SHIMMER_3){
							p1 = 0.0015;
							p2 = -1.0163;
						} else {
							//p1 = 0.0015;
							//p2 = -1.0067;
							p1 = 0.0015;
							p2 = -1.0163;
						}
					} else if (mGSRRange==3  || newGSRRange==3) {
						if (mHardwareVersion!=HW_ID.SHIMMER_3){
							p1 = 4.5580e-04;
							p2 = -0.3014;
						} else {
							//p1 = 4.4513e-04;
							//p2 = -0.3193;
							p1 = 4.5580e-04;
							p2 = -0.3014;
						}
					}
				}
				objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.GSR,new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iGSR]));
				uncalibratedData[iGSR]=(double)newPacketInt[iGSR];
				uncalibratedDataUnits[iGSR]=CHANNEL_UNITS.NO_UNITS;
				if (mEnableCalibration){
					calibratedData[iGSR] = calibrateGsrData(tempData[0],p1,p2);
					calibratedDataUnits[iGSR]=CHANNEL_UNITS.KOHMS;
					objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.GSR,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.KOHMS,calibratedData[iGSR]));
//					calibratedData[iGSR] = calibrateGsrDataToSiemens(tempData[0],p1,p2);
//					calibratedDataUnits[iGSR]=CHANNEL_UNITS.MICROSIEMENS;
//					objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.GSR,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MICROSIEMENS,calibratedData[iGSR]));
				}
			}

			if (((fwIdentifier == FW_TYPE_SD) && (mEnabledSensors & SDLogHeader.GYRO_MPU_MPL) > 0)){
				int iGyroX = getSignalIndex(Shimmer3.ObjectClusterSensorName.GYRO_MPU_MPL_X);
				int iGyroY = getSignalIndex(Shimmer3.ObjectClusterSensorName.GYRO_MPU_MPL_Y);
				int iGyroZ = getSignalIndex(Shimmer3.ObjectClusterSensorName.GYRO_MPU_MPL_Z);
				calibratedData[iGyroX] = (double)newPacketInt[iGyroX]/Math.pow(2, 16);
				calibratedData[iGyroY] = (double)newPacketInt[iGyroY]/Math.pow(2, 16);
				calibratedData[iGyroZ] = (double)newPacketInt[iGyroZ]/Math.pow(2, 16);
				calibratedDataUnits[iGyroX] = CHANNEL_UNITS.GYRO_CAL_UNIT;
				calibratedDataUnits[iGyroY] = CHANNEL_UNITS.GYRO_CAL_UNIT;
				calibratedDataUnits[iGyroZ] = CHANNEL_UNITS.GYRO_CAL_UNIT;
				uncalibratedData[iGyroX] = (double)newPacketInt[iGyroX];
				uncalibratedData[iGyroY] = (double)newPacketInt[iGyroY];
				uncalibratedData[iGyroZ] = (double)newPacketInt[iGyroZ];
				uncalibratedDataUnits[iGyroX] = CHANNEL_UNITS.NO_UNITS;
				uncalibratedDataUnits[iGyroY] = CHANNEL_UNITS.NO_UNITS;
				uncalibratedDataUnits[iGyroZ] = CHANNEL_UNITS.NO_UNITS;
			}

			if (((fwIdentifier == FW_TYPE_SD) && (mEnabledSensors & SDLogHeader.ACCEL_MPU_MPL) > 0)){
				int iAccelX = getSignalIndex(Shimmer3.ObjectClusterSensorName.ACCEL_MPU_MPL_X);
				int iAccelY = getSignalIndex(Shimmer3.ObjectClusterSensorName.ACCEL_MPU_MPL_Y);
				int iAccelZ = getSignalIndex(Shimmer3.ObjectClusterSensorName.ACCEL_MPU_MPL_Z);
//				calibratedData[iAccelX] = ((double)newPacketInt[iAccelX]/Math.pow(2, 16))*ACCELERATION_DUE_TO_GRAVITY;
//				calibratedData[iAccelY] = ((double)newPacketInt[iAccelY]/Math.pow(2, 16))*ACCELERATION_DUE_TO_GRAVITY;
//				calibratedData[iAccelZ] = ((double)newPacketInt[iAccelZ]/Math.pow(2, 16))*ACCELERATION_DUE_TO_GRAVITY;
//				calibratedDataUnits[iAccelX] = CHANNEL_UNITS.ACCEL_CAL_UNIT;
//				calibratedDataUnits[iAccelY] = CHANNEL_UNITS.ACCEL_CAL_UNIT;
//				calibratedDataUnits[iAccelZ] = CHANNEL_UNITS.ACCEL_CAL_UNIT;
				calibratedData[iAccelX] = (double)newPacketInt[iAccelX]/Math.pow(2, 16);
				calibratedData[iAccelY] = (double)newPacketInt[iAccelY]/Math.pow(2, 16);
				calibratedData[iAccelZ] = (double)newPacketInt[iAccelZ]/Math.pow(2, 16);
				calibratedDataUnits[iAccelX] = CHANNEL_UNITS.GRAVITY;
				calibratedDataUnits[iAccelY] = CHANNEL_UNITS.GRAVITY;
				calibratedDataUnits[iAccelZ] = CHANNEL_UNITS.GRAVITY;
				uncalibratedData[iAccelX] = (double)newPacketInt[iAccelX];
				uncalibratedData[iAccelY] = (double)newPacketInt[iAccelX];
				uncalibratedData[iAccelZ] = (double)newPacketInt[iAccelX];
				uncalibratedDataUnits[iAccelX] = CHANNEL_UNITS.NO_UNITS;
				uncalibratedDataUnits[iAccelY] = CHANNEL_UNITS.NO_UNITS;
				uncalibratedDataUnits[iAccelZ] = CHANNEL_UNITS.NO_UNITS;
			}

			if (((fwIdentifier == FW_TYPE_SD) && (mEnabledSensors & SDLogHeader.MAG_MPU_MPL) > 0)){
				int iMagX = getSignalIndex(Shimmer3.ObjectClusterSensorName.MAG_MPU_MPL_X);
				int iMagY = getSignalIndex(Shimmer3.ObjectClusterSensorName.MAG_MPU_MPL_Y);
				int iMagZ = getSignalIndex(Shimmer3.ObjectClusterSensorName.MAG_MPU_MPL_Z);
				calibratedData[iMagX] = (double)newPacketInt[iMagX]/Math.pow(2, 16);
				calibratedData[iMagY] = (double)newPacketInt[iMagY]/Math.pow(2, 16);
				calibratedData[iMagZ] = (double)newPacketInt[iMagZ]/Math.pow(2, 16);
				calibratedDataUnits[iMagX] = CHANNEL_UNITS.MAG_CAL_UNIT;
				calibratedDataUnits[iMagY] = CHANNEL_UNITS.MAG_CAL_UNIT;
				calibratedDataUnits[iMagZ] = CHANNEL_UNITS.MAG_CAL_UNIT;
				uncalibratedData[iMagX] = (double)newPacketInt[iMagX];
				uncalibratedData[iMagY] = (double)newPacketInt[iMagY];
				uncalibratedData[iMagZ] = (double)newPacketInt[iMagZ];
				uncalibratedDataUnits[iMagX] = CHANNEL_UNITS.NO_UNITS;
				uncalibratedDataUnits[iMagY] = CHANNEL_UNITS.NO_UNITS;
				uncalibratedDataUnits[iMagZ] = CHANNEL_UNITS.NO_UNITS;
			}

			if (((fwIdentifier == FW_TYPE_SD) && (mEnabledSensors & SDLogHeader.MPL_QUAT_6DOF) > 0)){
				int iQW = getSignalIndex(Shimmer3.ObjectClusterSensorName.QUAT_MPL_6DOF_W);
				int iQX = getSignalIndex(Shimmer3.ObjectClusterSensorName.QUAT_MPL_6DOF_X);
				int iQY = getSignalIndex(Shimmer3.ObjectClusterSensorName.QUAT_MPL_6DOF_Y);
				int iQZ = getSignalIndex(Shimmer3.ObjectClusterSensorName.QUAT_MPL_6DOF_Z);
				calibratedData[iQW] = (double)newPacketInt[iQW]/Math.pow(2, 30);
				calibratedData[iQX] = (double)newPacketInt[iQX]/Math.pow(2, 30);
				calibratedData[iQY] = (double)newPacketInt[iQY]/Math.pow(2, 30);
				calibratedData[iQZ] = (double)newPacketInt[iQZ]/Math.pow(2, 30);
				calibratedDataUnits[iQW] = CHANNEL_UNITS.NO_UNITS;
				calibratedDataUnits[iQX] = CHANNEL_UNITS.NO_UNITS;
				calibratedDataUnits[iQY] = CHANNEL_UNITS.NO_UNITS;
				calibratedDataUnits[iQZ] = CHANNEL_UNITS.NO_UNITS;
				uncalibratedData[iQW] = (double)newPacketInt[iQW];
				uncalibratedData[iQX] = (double)newPacketInt[iQX];
				uncalibratedData[iQY] = (double)newPacketInt[iQY];
				uncalibratedData[iQZ] = (double)newPacketInt[iQZ];
				uncalibratedDataUnits[iQW] = CHANNEL_UNITS.NO_UNITS;
				uncalibratedDataUnits[iQX] = CHANNEL_UNITS.NO_UNITS;
				uncalibratedDataUnits[iQY] = CHANNEL_UNITS.NO_UNITS;
				calibratedDataUnits[iQZ] = CHANNEL_UNITS.NO_UNITS;
			}
			if (((fwIdentifier == FW_TYPE_SD) && (mEnabledSensors & SDLogHeader.MPL_TEMPERATURE) > 0)){
				int iT = getSignalIndex(Shimmer3.ObjectClusterSensorName.MPL_TEMPERATURE);
				calibratedData[iT] = (double)newPacketInt[iT]/Math.pow(2, 16);
				calibratedDataUnits[iT] = CHANNEL_UNITS.DEGREES_CELSUIS;
				uncalibratedData[iT] = (double)newPacketInt[iT];
				uncalibratedDataUnits[iT] = CHANNEL_UNITS.NO_UNITS;
			}
			
			if (((fwIdentifier == FW_TYPE_SD) && (mEnabledSensors & SDLogHeader.MPL_PEDOMETER) > 0)){
				int iPedoCnt = getSignalIndex(Shimmer3.ObjectClusterSensorName.MPL_PEDOM_CNT);
				int iPedoTime = getSignalIndex(Shimmer3.ObjectClusterSensorName.MPL_PEDOM_TIME);
				calibratedData[iPedoCnt] = (double)newPacketInt[iPedoCnt];
				calibratedData[iPedoTime] = (double)newPacketInt[iPedoTime];
				calibratedDataUnits[iPedoCnt] = CHANNEL_UNITS.NO_UNITS;
				calibratedDataUnits[iPedoTime] = CHANNEL_UNITS.MILLISECONDS;
				uncalibratedData[iPedoCnt] = (double)newPacketInt[iPedoCnt];
				uncalibratedData[iPedoTime] = (double)newPacketInt[iPedoTime];
				uncalibratedDataUnits[iPedoCnt] = CHANNEL_UNITS.NO_UNITS;
				uncalibratedDataUnits[iPedoTime] = CHANNEL_UNITS.NO_UNITS;
			}
			
			if (((fwIdentifier == FW_TYPE_SD) && (mEnabledSensors & SDLogHeader.MPL_HEADING) > 0)){
				int iH = getSignalIndex(Shimmer3.ObjectClusterSensorName.MPL_HEADING);
				calibratedData[iH] = (double)newPacketInt[iH]/Math.pow(2, 16);
				calibratedDataUnits[iH] = CHANNEL_UNITS.DEGREES;
				uncalibratedData[iH] = (double)newPacketInt[iH];
				uncalibratedDataUnits[iH] = CHANNEL_UNITS.NO_UNITS;
			}

			//TODO: separate out tap dir and cnt to two channels 
			//Bits 7-5 - Direction,	Bits 4-0 - Count
			if (((fwIdentifier == FW_TYPE_SD) && (mEnabledSensors & SDLogHeader.MPL_TAP) > 0)){
				int iTap = getSignalIndex(Shimmer3.ObjectClusterSensorName.TAPDIRANDTAPCNT);
//				calibratedData[iTapCnt] = (double)(newPacketInt[iPedoCnt]&0x1F);
//				calibratedData[iTapDir] = (double)((newPacketInt[iPedoTime]>>5)&0x07);
				calibratedData[iTap] = (double)newPacketInt[iTap];
				calibratedDataUnits[iTap] = CHANNEL_UNITS.NO_UNITS;
				uncalibratedData[iTap] = (double)newPacketInt[iTap];
				uncalibratedDataUnits[iTap] = CHANNEL_UNITS.NO_UNITS;
			}
			
			//TODO: separate out motion and orientation to two channels
			//Bit 7 - Motion/No motion,	Bits 5-4 - Display Orientation,	Bits 3-1 - Orientation,	Bit 0 - Flip indicator
			if (((fwIdentifier == FW_TYPE_SD) && (mEnabledSensors & SDLogHeader.MPL_MOTION_ORIENT) > 0)){
				int iMotOrient = getSignalIndex(Shimmer3.ObjectClusterSensorName.MOTIONANDORIENT);
				calibratedData[iMotOrient] = (double)newPacketInt[iMotOrient];
				calibratedDataUnits[iMotOrient] = CHANNEL_UNITS.NO_UNITS;
				uncalibratedData[iMotOrient] = (double)newPacketInt[iMotOrient];
				uncalibratedDataUnits[iMotOrient] = CHANNEL_UNITS.NO_UNITS;
			}
			
			//Additional Channels Offset
			int additionalChannelsOffset = calibratedData.length-numAdditionalChannels+1; //+1 because timestamp channel appears at the start
			
			if (fwIdentifier == FW_TYPE_BT){ // Here so as not to mess with ShimmerSDLog
				if(isEXGUsingDefaultECGConfiguration()){
					if (((fwIdentifier == FW_TYPE_BT) && (mEnabledSensors & BTStream.EXG1_16BIT) > 0) 
							|| ((fwIdentifier == FW_TYPE_SD) && (mEnabledSensors & SDLogHeader.EXG1_16BIT) > 0)){
						
						int iexg1ch1 = getSignalIndex(Shimmer3.ObjectClusterSensorName.EXG1_CH1_16BIT);
						int iexg1ch2 = getSignalIndex(Shimmer3.ObjectClusterSensorName.EXG1_CH2_16BIT);
	
						sensorNames[additionalChannelsOffset] = Shimmer3.ObjectClusterSensorName.ECG_LL_LA_16BIT;
						
						uncalibratedData[additionalChannelsOffset]= uncalibratedData[iexg1ch1] - uncalibratedData[iexg1ch2];
						uncalibratedDataUnits[additionalChannelsOffset]=CHANNEL_UNITS.NO_UNITS;
						objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.ECG_LL_LA_16BIT,new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,uncalibratedData[additionalChannelsOffset]));
						
						if(mEnableCalibration){
							calibratedData[additionalChannelsOffset]= calibratedData[iexg1ch1] - calibratedData[iexg1ch2];
							calibratedDataUnits[additionalChannelsOffset]=CHANNEL_UNITS.MILLIVOLTS;
							objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.ECG_LL_LA_16BIT,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calibratedData[additionalChannelsOffset]));
						}
						additionalChannelsOffset += 1;
	
						
					}
					else if (((fwIdentifier == FW_TYPE_BT) && (mEnabledSensors & BTStream.EXG1_24BIT) > 0) 
							|| ((fwIdentifier == FW_TYPE_SD) && (mEnabledSensors & SDLogHeader.EXG1_24BIT) > 0)){
						
						int iexg1ch1 = getSignalIndex(Shimmer3.ObjectClusterSensorName.EXG1_CH1_24BIT);
						int iexg1ch2 = getSignalIndex(Shimmer3.ObjectClusterSensorName.EXG1_CH2_24BIT);
	
						sensorNames[additionalChannelsOffset] = Shimmer3.ObjectClusterSensorName.ECG_LL_LA_24BIT;
						
						uncalibratedData[additionalChannelsOffset]= uncalibratedData[iexg1ch1] - uncalibratedData[iexg1ch2];
						uncalibratedDataUnits[additionalChannelsOffset]=CHANNEL_UNITS.NO_UNITS;
						objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.ECG_LL_LA_24BIT,new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,uncalibratedData[additionalChannelsOffset]));
						
						if(mEnableCalibration){
							calibratedData[additionalChannelsOffset]= calibratedData[iexg1ch1] - calibratedData[iexg1ch2];
							calibratedDataUnits[additionalChannelsOffset]=CHANNEL_UNITS.MILLIVOLTS;
							objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.ECG_LL_LA_24BIT,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calibratedData[additionalChannelsOffset]));
						}
						additionalChannelsOffset += 1;
					}
				}
			}
			else if(fwIdentifier == FW_TYPE_SD){
				if(isEXGUsingDefaultECGConfigurationForSDFW()){
					//calculate the ECG Derived sensor for SD (LL-LA) and replace it for the ECG Respiration
					if (((mEnabledSensors & BTStream.EXG1_16BIT) > 0)){
						
						int iexg1ch1 = getSignalIndex(Shimmer3.ObjectClusterSensorName.EXG1_CH1_16BIT);
						int iexg1ch2 = getSignalIndex(Shimmer3.ObjectClusterSensorName.EXG1_CH2_16BIT);
						int iexg2ch1 = getSignalIndex(Shimmer3.ObjectClusterSensorName.EXG2_CH1_16BIT);
						
						sensorNames[iexg2ch1] = Shimmer3.ObjectClusterSensorName.ECG_LL_LA_16BIT;
						objectCluster.mPropertyCluster.removeAll(Shimmer3.ObjectClusterSensorName.EXG2_CH1_16BIT);
						
						uncalibratedData[iexg2ch1]= uncalibratedData[iexg1ch1] - uncalibratedData[iexg1ch2];
						uncalibratedDataUnits[iexg2ch1]=CHANNEL_UNITS.NO_UNITS;
						objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.ECG_LL_LA_16BIT,new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,uncalibratedData[iexg2ch1]));
						
						if(mEnableCalibration){
							calibratedData[iexg2ch1]= calibratedData[iexg1ch1] - calibratedData[iexg1ch2];
							calibratedDataUnits[iexg2ch1]=CHANNEL_UNITS.MILLIVOLTS;
							objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.ECG_LL_LA_16BIT,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calibratedData[iexg2ch1]));
						}

					}
					else if (((mEnabledSensors & BTStream.EXG1_24BIT) > 0)){
						
						int iexg1ch1 = getSignalIndex(Shimmer3.ObjectClusterSensorName.EXG1_CH1_24BIT);
						int iexg1ch2 = getSignalIndex(Shimmer3.ObjectClusterSensorName.EXG1_CH2_24BIT);
						int iexg2ch1 = getSignalIndex(Shimmer3.ObjectClusterSensorName.EXG2_CH1_24BIT);
						
						sensorNames[iexg2ch1] = Shimmer3.ObjectClusterSensorName.ECG_LL_LA_24BIT;
						objectCluster.mPropertyCluster.removeAll(Shimmer3.ObjectClusterSensorName.EXG1_CH2_24BIT);
						
						uncalibratedData[iexg2ch1]= uncalibratedData[iexg1ch1] - uncalibratedData[iexg1ch2];
						uncalibratedDataUnits[iexg2ch1]=CHANNEL_UNITS.NO_UNITS;
						objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.ECG_LL_LA_24BIT,new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,uncalibratedData[iexg2ch1]));
						
						if(mEnableCalibration){
							calibratedData[iexg2ch1]= calibratedData[iexg1ch1] - calibratedData[iexg1ch2];
							calibratedDataUnits[iexg2ch1]=CHANNEL_UNITS.MILLIVOLTS;
							objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.ECG_LL_LA_24BIT,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calibratedData[iexg2ch1]));
						}
						
					}
				}
			}
			
			
			if(fwIdentifier == FW_TYPE_BT){
				objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.BATT_PERCENTAGE,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.PERCENT,(double)mShimmerBattStatusDetails.mEstimatedChargePercentage));
				calibratedData[additionalChannelsOffset] = (double)mShimmerBattStatusDetails.mEstimatedChargePercentage;
				calibratedDataUnits[additionalChannelsOffset] = CHANNEL_UNITS.PERCENT;
				uncalibratedData[additionalChannelsOffset] = Double.NaN;
				uncalibratedDataUnits[additionalChannelsOffset] = "";
				sensorNames[additionalChannelsOffset] = Shimmer3.ObjectClusterSensorName.BATT_PERCENTAGE;
				additionalChannelsOffset+=1;

				objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.PACKET_RECEPTION_RATE_CURRENT,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.PERCENT,(double)mPacketReceptionRateCurrent));
				calibratedData[additionalChannelsOffset] = (double)mPacketReceptionRateCurrent;
				calibratedDataUnits[additionalChannelsOffset] = CHANNEL_UNITS.PERCENT;
				uncalibratedData[additionalChannelsOffset] = Double.NaN;
				uncalibratedDataUnits[additionalChannelsOffset] = "";
				sensorNames[additionalChannelsOffset] = Shimmer3.ObjectClusterSensorName.PACKET_RECEPTION_RATE_CURRENT;
				additionalChannelsOffset+=1;

				objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.PACKET_RECEPTION_RATE_TRIAL,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.PERCENT,(double)mPacketReceptionRate));
				calibratedData[additionalChannelsOffset] = (double)mPacketReceptionRate;
				calibratedDataUnits[additionalChannelsOffset] = CHANNEL_UNITS.PERCENT;
				uncalibratedData[additionalChannelsOffset] = Double.NaN;
				uncalibratedDataUnits[additionalChannelsOffset] = "";
				sensorNames[additionalChannelsOffset] = Shimmer3.ObjectClusterSensorName.PACKET_RECEPTION_RATE_TRIAL;
				additionalChannelsOffset+=1;
				
				calibratedData[additionalChannelsOffset] = (double)systemTime;
				calibratedDataUnits[additionalChannelsOffset] = CHANNEL_UNITS.MILLISECONDS;
				uncalibratedData[additionalChannelsOffset] = Double.NaN;
				uncalibratedDataUnits[additionalChannelsOffset] = "";
				sensorNames[additionalChannelsOffset] = Shimmer3.ObjectClusterSensorName.SYSTEM_TIMESTAMP;
				additionalChannelsOffset+=1;
			}
			
			objectCluster.mCalData = calibratedData;
			objectCluster.mUncalData = uncalibratedData;
			objectCluster.mUnitCal = calibratedDataUnits;
			objectCluster.mUnitUncal = uncalibratedDataUnits;
			objectCluster.mSensorNames = sensorNames;

		} 
		else if (mHardwareVersion==HW_ID.SHIMMER_2 || mHardwareVersion==HW_ID.SHIMMER_2R){
			 //start of Shimmer2

			int iTimeStamp=getSignalIndex(Configuration.Shimmer2.ObjectClusterSensorName.TIMESTAMP); //find index
			double calibratedTS = calibrateTimeStamp((double)newPacketInt[iTimeStamp]);

			//TIMESTAMP
			if (fwIdentifier == FW_TYPE_BT){
				objectCluster.mPropertyCluster.put(Configuration.Shimmer2.ObjectClusterSensorName.TIMESTAMP,new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iTimeStamp]));
				uncalibratedData[iTimeStamp] = (double)newPacketInt[iTimeStamp];
				uncalibratedDataUnits[iTimeStamp] = CHANNEL_UNITS.NO_UNITS;
				if (mEnableCalibration){
					objectCluster.mPropertyCluster.put(Configuration.Shimmer2.ObjectClusterSensorName.TIMESTAMP,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLISECONDS,calibratedTS));
					calibratedData[iTimeStamp] = calibratedTS;
					calibratedDataUnits[iTimeStamp] = CHANNEL_UNITS.MILLISECONDS;
				}
			}
			
			if ((mEnabledSensors & SENSOR_ACCEL) > 0){
				int iAccelX=getSignalIndex(Shimmer2.ObjectClusterSensorName.ACCEL_X); //find index
				int iAccelY=getSignalIndex(Shimmer2.ObjectClusterSensorName.ACCEL_Y); //find index
				int iAccelZ=getSignalIndex(Shimmer2.ObjectClusterSensorName.ACCEL_Z); //find index
				objectCluster.mPropertyCluster.put(Shimmer2.ObjectClusterSensorName.ACCEL_X,new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iAccelX]));
				objectCluster.mPropertyCluster.put(Shimmer2.ObjectClusterSensorName.ACCEL_Y,new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iAccelY]));
				objectCluster.mPropertyCluster.put(Shimmer2.ObjectClusterSensorName.ACCEL_Z,new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iAccelZ]));
				if (mEnableCalibration){
					tempData[0]=(double)newPacketInt[iAccelX];
					tempData[1]=(double)newPacketInt[iAccelY];
					tempData[2]=(double)newPacketInt[iAccelZ];
					double[] accelCalibratedData=calibrateInertialSensorData(tempData, mAlignmentMatrixAnalogAccel, mSensitivityMatrixAnalogAccel, mOffsetVectorAnalogAccel);
					calibratedData[iAccelX]=accelCalibratedData[0];
					calibratedData[iAccelY]=accelCalibratedData[1];
					calibratedData[iAccelZ]=accelCalibratedData[2];
					if (mDefaultCalibrationParametersAccel == true) {
						objectCluster.mPropertyCluster.put(Shimmer2.ObjectClusterSensorName.ACCEL_X,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.METER_PER_SECOND_SQUARE+"*",accelCalibratedData[0]));
						objectCluster.mPropertyCluster.put(Shimmer2.ObjectClusterSensorName.ACCEL_Y,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.METER_PER_SECOND_SQUARE+"*",accelCalibratedData[1]));
						objectCluster.mPropertyCluster.put(Shimmer2.ObjectClusterSensorName.ACCEL_Z,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.METER_PER_SECOND_SQUARE+"*",accelCalibratedData[2]));
						accelerometer.x=accelCalibratedData[0];
						accelerometer.y=accelCalibratedData[1];
						accelerometer.z=accelCalibratedData[2];
					} else {
						objectCluster.mPropertyCluster.put(Shimmer2.ObjectClusterSensorName.ACCEL_X,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.METER_PER_SECOND_SQUARE,accelCalibratedData[0]));
						objectCluster.mPropertyCluster.put(Shimmer2.ObjectClusterSensorName.ACCEL_Y,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.METER_PER_SECOND_SQUARE,accelCalibratedData[1]));
						objectCluster.mPropertyCluster.put(Shimmer2.ObjectClusterSensorName.ACCEL_Z,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.METER_PER_SECOND_SQUARE,accelCalibratedData[2]));
						accelerometer.x=accelCalibratedData[0];
						accelerometer.y=accelCalibratedData[1];
						accelerometer.z=accelCalibratedData[2];
					}
				}

			}

			if ((mEnabledSensors & SENSOR_GYRO) > 0) {
				int iGyroX=getSignalIndex(Shimmer2.ObjectClusterSensorName.GYRO_X);
				int iGyroY=getSignalIndex(Shimmer2.ObjectClusterSensorName.GYRO_Y);
				int iGyroZ=getSignalIndex(Shimmer2.ObjectClusterSensorName.GYRO_Z);					
				objectCluster.mPropertyCluster.put(Shimmer2.ObjectClusterSensorName.GYRO_X,new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iGyroX]));
				objectCluster.mPropertyCluster.put(Shimmer2.ObjectClusterSensorName.GYRO_Y,new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iGyroY]));
				objectCluster.mPropertyCluster.put(Shimmer2.ObjectClusterSensorName.GYRO_Z,new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iGyroZ]));
				if (mEnableCalibration){
					tempData[0]=(double)newPacketInt[iGyroX];
					tempData[1]=(double)newPacketInt[iGyroY];
					tempData[2]=(double)newPacketInt[iGyroZ];
					double[] gyroCalibratedData=calibrateInertialSensorData(tempData, mAlignmentMatrixGyroscope, mSensitivityMatrixGyroscope, mOffsetVectorGyroscope);
					calibratedData[iGyroX]=gyroCalibratedData[0];
					calibratedData[iGyroY]=gyroCalibratedData[1];
					calibratedData[iGyroZ]=gyroCalibratedData[2];
					if (mDefaultCalibrationParametersGyro == true) {
						objectCluster.mPropertyCluster.put(Shimmer2.ObjectClusterSensorName.GYRO_X,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.DEGREES_PER_SECOND+"*",gyroCalibratedData[0]));
						objectCluster.mPropertyCluster.put(Shimmer2.ObjectClusterSensorName.GYRO_Y,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.DEGREES_PER_SECOND+"*",gyroCalibratedData[1]));
						objectCluster.mPropertyCluster.put(Shimmer2.ObjectClusterSensorName.GYRO_Z,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.DEGREES_PER_SECOND+"*",gyroCalibratedData[2]));
						gyroscope.x=gyroCalibratedData[0]*Math.PI/180;
						gyroscope.y=gyroCalibratedData[1]*Math.PI/180;
						gyroscope.z=gyroCalibratedData[2]*Math.PI/180;
					} else {
						objectCluster.mPropertyCluster.put(Shimmer2.ObjectClusterSensorName.GYRO_X,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.DEGREES_PER_SECOND,gyroCalibratedData[0]));
						objectCluster.mPropertyCluster.put(Shimmer2.ObjectClusterSensorName.GYRO_Y,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.DEGREES_PER_SECOND,gyroCalibratedData[1]));
						objectCluster.mPropertyCluster.put(Shimmer2.ObjectClusterSensorName.GYRO_Z,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.DEGREES_PER_SECOND,gyroCalibratedData[2]));
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
				int iMagX=getSignalIndex(Shimmer2.ObjectClusterSensorName.MAG_X);
				int iMagY=getSignalIndex(Shimmer2.ObjectClusterSensorName.MAG_Y);
				int iMagZ=getSignalIndex(Shimmer2.ObjectClusterSensorName.MAG_Z);
				objectCluster.mPropertyCluster.put(Shimmer2.ObjectClusterSensorName.MAG_X,new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iMagX]));
				objectCluster.mPropertyCluster.put(Shimmer2.ObjectClusterSensorName.MAG_Y,new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iMagY]));
				objectCluster.mPropertyCluster.put(Shimmer2.ObjectClusterSensorName.MAG_Z,new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iMagZ]));
				if (mEnableCalibration){
					tempData[0]=(double)newPacketInt[iMagX];
					tempData[1]=(double)newPacketInt[iMagY];
					tempData[2]=(double)newPacketInt[iMagZ];
					double[] magCalibratedData=calibrateInertialSensorData(tempData, mAlignmentMatrixMagnetometer, mSensitivityMatrixMagnetometer, mOffsetVectorMagnetometer);
					calibratedData[iMagX]=magCalibratedData[0];
					calibratedData[iMagY]=magCalibratedData[1];
					calibratedData[iMagZ]=magCalibratedData[2];
					if (mDefaultCalibrationParametersMag == true) {
						objectCluster.mPropertyCluster.put(Shimmer2.ObjectClusterSensorName.MAG_X,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL_FLUX+"*",magCalibratedData[0]));
						objectCluster.mPropertyCluster.put(Shimmer2.ObjectClusterSensorName.MAG_Y,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL_FLUX+"*",magCalibratedData[1]));
						objectCluster.mPropertyCluster.put(Shimmer2.ObjectClusterSensorName.MAG_Z,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL_FLUX+"*",magCalibratedData[2]));
						magnetometer.x=magCalibratedData[0];
						magnetometer.y=magCalibratedData[1];
						magnetometer.z=magCalibratedData[2];
					} else {
						objectCluster.mPropertyCluster.put(Shimmer2.ObjectClusterSensorName.MAG_X,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL_FLUX,magCalibratedData[0]));
						objectCluster.mPropertyCluster.put(Shimmer2.ObjectClusterSensorName.MAG_Y,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL_FLUX,magCalibratedData[1]));
						objectCluster.mPropertyCluster.put(Shimmer2.ObjectClusterSensorName.MAG_Z,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL_FLUX,magCalibratedData[2]));
						magnetometer.x=magCalibratedData[0];
						magnetometer.y=magCalibratedData[1];
						magnetometer.z=magCalibratedData[2];
					}
				}
			}


			if ((mEnabledSensors & SENSOR_ACCEL) > 0 && (mEnabledSensors & SENSOR_GYRO) > 0 && (mEnabledSensors & SENSOR_MAG) > 0 && mOrientationEnabled ){
				if (mEnableCalibration){
					if (mOrientationAlgo==null){
						mOrientationAlgo = new GradDes3DOrientation(0.4, (double)1/mShimmerSamplingRate, 1, 0, 0,0);
					}
					Quaternion q = mOrientationAlgo.update(accelerometer.x,accelerometer.y,accelerometer.z, gyroscope.x,gyroscope.y,gyroscope.z, magnetometer.x,magnetometer.y,magnetometer.z);
					double theta, Rx, Ry, Rz, rho;
					rho = Math.acos(q.q1);
					theta = rho * 2;
					Rx = q.q2 / Math.sin(rho);
					Ry = q.q3 / Math.sin(rho);
					Rz = q.q4 / Math.sin(rho);
					objectCluster.mPropertyCluster.put(Shimmer2.ObjectClusterSensorName.AXIS_ANGLE_A,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL,theta));
					objectCluster.mPropertyCluster.put(Shimmer2.ObjectClusterSensorName.AXIS_ANGLE_X,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL,Rx));
					objectCluster.mPropertyCluster.put(Shimmer2.ObjectClusterSensorName.AXIS_ANGLE_Y,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL,Ry));
					objectCluster.mPropertyCluster.put(Shimmer2.ObjectClusterSensorName.AXIS_ANGLE_Z,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL,Rz));
					objectCluster.mPropertyCluster.put(Shimmer2.ObjectClusterSensorName.QUAT_MADGE_9DOF_W,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL,q.q1));
					objectCluster.mPropertyCluster.put(Shimmer2.ObjectClusterSensorName.QUAT_MADGE_9DOF_X,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL,q.q2));
					objectCluster.mPropertyCluster.put(Shimmer2.ObjectClusterSensorName.QUAT_MADGE_9DOF_Y,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL,q.q3));
					objectCluster.mPropertyCluster.put(Shimmer2.ObjectClusterSensorName.QUAT_MADGE_9DOF_Z,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL,q.q4));
				}
			}

			if ((mEnabledSensors & SENSOR_GSR) > 0) {
				int iGSR = getSignalIndex(Shimmer2.ObjectClusterSensorName.GSR);
				tempData[0] = (double)newPacketInt[iGSR];
				int newGSRRange = -1; // initialized to -1 so it will only come into play if mGSRRange = 4  

				double p1=0,p2=0;//,p3=0,p4=0,p5=0;
				if (mGSRRange==4){
					newGSRRange=(49152 & (int)tempData[0])>>14; 
				}
				if (mGSRRange==0 || newGSRRange==0) { //Note that from FW 1.0 onwards the MSB of the GSR data contains the range
					// the polynomial function used for calibration has been deprecated, it is replaced with a linear function
					
					p1 = 0.0373;
					p2 = -24.9915;
				} else if (mGSRRange==1 || newGSRRange==1) {
					p1 = 0.0054;
					p2 = -3.5194;
				} else if (mGSRRange==2 || newGSRRange==2) {
					p1 = 0.0015;
					p2 = -1.0163;
				} else if (mGSRRange==3  || newGSRRange==3) {
					p1 = 4.5580e-04;
					p2 = -0.3014;
				}
				objectCluster.mPropertyCluster.put(Shimmer2.ObjectClusterSensorName.GSR,new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iGSR]));
				if (mEnableCalibration){
					tempData[0] = (double)newPacketInt[iGSR];
					calibratedData[iGSR] = calibrateGsrData(tempData[0],p1,p2);
					objectCluster.mPropertyCluster.put(Shimmer2.ObjectClusterSensorName.GSR,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.KOHMS,calibratedData[iGSR]));
				}
			}
			if ((mEnabledSensors & SENSOR_ECG) > 0) {
				int iECGRALL = getSignalIndex(Shimmer2.ObjectClusterSensorName.ECG_RA_LL);
				int iECGLALL = getSignalIndex(Shimmer2.ObjectClusterSensorName.ECG_LA_LL);
				objectCluster.mPropertyCluster.put(Shimmer2.ObjectClusterSensorName.ECG_RA_LL,new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iECGRALL]));
				objectCluster.mPropertyCluster.put(Shimmer2.ObjectClusterSensorName.ECG_LA_LL,new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iECGLALL]));
				if (mEnableCalibration){
					tempData[0] = (double)newPacketInt[iECGRALL];
					tempData[1] = (double)newPacketInt[iECGLALL];
					calibratedData[iECGRALL]=calibrateU12AdcValue(tempData[0],OffsetECGRALL,3,GainECGRALL);
					calibratedData[iECGLALL]=calibrateU12AdcValue(tempData[1],OffsetECGLALL,3,GainECGLALL);
					if (mDefaultCalibrationParametersECG == true) {
						objectCluster.mPropertyCluster.put(Shimmer2.ObjectClusterSensorName.ECG_RA_LL,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS+"*",calibratedData[iECGRALL]));
						objectCluster.mPropertyCluster.put(Shimmer2.ObjectClusterSensorName.ECG_LA_LL,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS+"*",calibratedData[iECGLALL]));
					} else {
						objectCluster.mPropertyCluster.put(Shimmer2.ObjectClusterSensorName.ECG_RA_LL,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calibratedData[iECGRALL]));
						objectCluster.mPropertyCluster.put(Shimmer2.ObjectClusterSensorName.ECG_LA_LL,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calibratedData[iECGLALL]));
					}
				}
			}
			if ((mEnabledSensors & SENSOR_EMG) > 0) {
				int iEMG = getSignalIndex(Shimmer2.ObjectClusterSensorName.EMG);
				objectCluster.mPropertyCluster.put(Shimmer2.ObjectClusterSensorName.EMG,new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iEMG]));
				if (mEnableCalibration){
					tempData[0] = (double)newPacketInt[iEMG];
					calibratedData[iEMG]=calibrateU12AdcValue(tempData[0],OffsetEMG,3,GainEMG);
					if (mDefaultCalibrationParametersEMG == true){
						objectCluster.mPropertyCluster.put(Shimmer2.ObjectClusterSensorName.EMG,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS+"*",calibratedData[iEMG]));
					} else {
						objectCluster.mPropertyCluster.put(Shimmer2.ObjectClusterSensorName.EMG,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calibratedData[iEMG]));
					}
				}
			}
			if ((mEnabledSensors & SENSOR_BRIDGE_AMP) > 0) {
				int iBAHigh = getSignalIndex(Shimmer2.ObjectClusterSensorName.BRIDGE_AMP_HIGH);
				int iBALow = getSignalIndex(Shimmer2.ObjectClusterSensorName.BRIDGE_AMP_LOW);
				objectCluster.mPropertyCluster.put(Shimmer2.ObjectClusterSensorName.BRIDGE_AMP_HIGH,new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iBAHigh]));
				objectCluster.mPropertyCluster.put(Shimmer2.ObjectClusterSensorName.BRIDGE_AMP_LOW,new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iBALow]));
				if (mEnableCalibration){
					tempData[0] = (double)newPacketInt[iBAHigh];
					tempData[1] = (double)newPacketInt[iBALow];
					calibratedData[iBAHigh]=calibrateU12AdcValue(tempData[0],60,3,551);
					calibratedData[iBALow]=calibrateU12AdcValue(tempData[1],1950,3,183.7);
					objectCluster.mPropertyCluster.put(Shimmer2.ObjectClusterSensorName.BRIDGE_AMP_HIGH,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calibratedData[iBAHigh]));
					objectCluster.mPropertyCluster.put(Shimmer2.ObjectClusterSensorName.BRIDGE_AMP_LOW,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calibratedData[iBALow]));	
				}
			}
			if ((mEnabledSensors & SENSOR_HEART) > 0) {
				int iHeartRate = getSignalIndex(Shimmer2.ObjectClusterSensorName.HEART_RATE);
				tempData[0] = (double)newPacketInt[iHeartRate];
				objectCluster.mPropertyCluster.put(Shimmer2.ObjectClusterSensorName.HEART_RATE,new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,tempData[0]));
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
					objectCluster.mPropertyCluster.put(Shimmer2.ObjectClusterSensorName.HEART_RATE,new FormatCluster(CHANNEL_TYPE.CAL.toString(),"BPM",calibratedData[iHeartRate]));	
				}
			}
			if ((mEnabledSensors& SENSOR_EXP_BOARD_A0) > 0) {
				int iA0 = getSignalIndex(Shimmer2.ObjectClusterSensorName.EXP_BOARD_A0);
				tempData[0] = (double)newPacketInt[iA0];
				if (getPMux()==0){
					objectCluster.mPropertyCluster.put(Shimmer2.ObjectClusterSensorName.EXP_BOARD_A0,new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iA0]));
					if (mEnableCalibration){
						calibratedData[iA0]=calibrateU12AdcValue(tempData[0],0,3,1);
						objectCluster.mPropertyCluster.put(Shimmer2.ObjectClusterSensorName.EXP_BOARD_A0,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calibratedData[iA0]));
					}
				} else {
					objectCluster.mPropertyCluster.put(Shimmer2.ObjectClusterSensorName.VOLT_REG,new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iA0]));
					if (mEnableCalibration){
						calibratedData[iA0]=calibrateU12AdcValue(tempData[0],0,3,1)*1.988;
						objectCluster.mPropertyCluster.put(Shimmer2.ObjectClusterSensorName.VOLT_REG,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calibratedData[iA0]));
					}

				}
			}					
			if ((mEnabledSensors & SENSOR_EXP_BOARD_A7) > 0) {
				int iA7 = getSignalIndex(Shimmer2.ObjectClusterSensorName.EXP_BOARD_A7);
				tempData[0] = (double)newPacketInt[iA7];
				if (getPMux()==0){
					objectCluster.mPropertyCluster.put(Shimmer2.ObjectClusterSensorName.EXP_BOARD_A7,new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iA7]));
					if (mEnableCalibration){
						calibratedData[iA7]=calibrateU12AdcValue(tempData[0],0,3,1);
						objectCluster.mPropertyCluster.put(Shimmer2.ObjectClusterSensorName.EXP_BOARD_A7,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calibratedData[iA7]));
					} else {
						objectCluster.mPropertyCluster.put(Shimmer2.ObjectClusterSensorName.BATTERY,new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iA7]));
						if (mEnableCalibration){
							calibratedData[iA7]=calibrateU12AdcValue(tempData[0],0,3,1)*2;
							objectCluster.mPropertyCluster.put(Shimmer2.ObjectClusterSensorName.BATTERY,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calibratedData[iA7]));
						}

					}
				} 
			}
			if  ((mEnabledSensors & SENSOR_BATT) > 0) {
				int iA0 = getSignalIndex(Shimmer2.ObjectClusterSensorName.EXP_BOARD_A0);
				objectCluster.mPropertyCluster.put(Shimmer2.ObjectClusterSensorName.VOLT_REG,new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iA0]));

				int iA7 = getSignalIndex(Shimmer2.ObjectClusterSensorName.EXP_BOARD_A7);
				objectCluster.mPropertyCluster.put(Shimmer2.ObjectClusterSensorName.BATTERY,new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iA7]));


				if (mEnableCalibration){
					tempData[0] = (double)newPacketInt[iA0];
					calibratedData[iA0]=calibrateU12AdcValue(tempData[0],0,3,1)*1.988;
					objectCluster.mPropertyCluster.put(Shimmer2.ObjectClusterSensorName.VOLT_REG,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calibratedData[iA0]));

					tempData[0] = (double)newPacketInt[iA7];
					calibratedData[iA7]=calibrateU12AdcValue(tempData[0],0,3,1)*2;
					objectCluster.mPropertyCluster.put(Shimmer2.ObjectClusterSensorName.BATTERY,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calibratedData[iA7]));	
					mVSenseBattMA.addValue(calibratedData[iA7]);
					checkBattery();
				}
			}
			
		}
		else{
			throw new Exception("The Hardware version is not compatible");
		}
		
		return objectCluster;
	}
	
	private boolean isDerivedSensorsSupported(){
		if((isThisVerCompatibleWith(HW_ID.SHIMMER_3, FW_ID.SHIMMER3.BTSTREAM, 0, 7, 0))
		||(isThisVerCompatibleWith(HW_ID.SHIMMER_3, FW_ID.SHIMMER3.SDLOG, 0, 8, 69))
		||(isThisVerCompatibleWith(HW_ID.SHIMMER_3, FW_ID.SHIMMER3.LOGANDSTREAM, 0, 3, 17))){
			return true;
		}
		return false;
	}
	
	public boolean isThisVerCompatibleWith(int hardwareVersion, int firmwareIdentifier, int firmwareVersionMajor, int firmwareVersionMinor, int firmwareVersionInternal){
		return UtilShimmer.compareVersions(mHardwareVersion, mFirmwareIdentifier, mFirmwareVersionMajor, mFirmwareVersionMinor, mFirmwareVersionInternal,
				hardwareVersion, firmwareIdentifier, firmwareVersionMajor, firmwareVersionMinor, firmwareVersionInternal);
	}
	
	@Deprecated
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

		int iTimeStamp=getSignalIndex(Configuration.Shimmer2.ObjectClusterSensorName.TIMESTAMP); //find index
		tempData[0]=(double)newPacketInt[1];
		objectCluster.mPropertyCluster.put("Timestamp",new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iTimeStamp]));
		if (mEnableCalibration){
			objectCluster.mPropertyCluster.put("Timestamp",new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLISECONDS,calibrateTimeStamp((double)newPacketInt[iTimeStamp])));
		}
		objectCluster = callAdditionalServices(objectCluster);


		if (mHardwareVersion==HW_ID.SHIMMER_SR30 || mHardwareVersion==HW_ID.SHIMMER_3){
			if (((mEnabledSensors & SENSOR_ACCEL) > 0)){
				int iAccelX=getSignalIndex("Low Noise Accelerometer X"); //find index
				int iAccelY=getSignalIndex("Low Noise Accelerometer Y"); //find index
				int iAccelZ=getSignalIndex("Low Noise Accelerometer Z"); //find index
				tempData[0]=(double)newPacketInt[iAccelX];
				tempData[1]=(double)newPacketInt[iAccelY];
				tempData[2]=(double)newPacketInt[iAccelZ];

				objectCluster.mPropertyCluster.put("Low Noise Accelerometer X",new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iAccelX]));
				objectCluster.mPropertyCluster.put("Low Noise Accelerometer Y",new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iAccelY]));
				objectCluster.mPropertyCluster.put("Low Noise Accelerometer Z",new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iAccelZ]));

				if (mEnableCalibration){
					double[] accelCalibratedData;
					accelCalibratedData=calibrateInertialSensorData(tempData, mAlignmentMatrixAnalogAccel, mSensitivityMatrixAnalogAccel, mOffsetVectorAnalogAccel);
					calibratedData[iAccelX]=accelCalibratedData[0];
					calibratedData[iAccelY]=accelCalibratedData[1];
					calibratedData[iAccelZ]=accelCalibratedData[2];
					if (mDefaultCalibrationParametersAccel == true) {
						objectCluster.mPropertyCluster.put("Low Noise Accelerometer X",new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.METER_PER_SECOND_SQUARE+"*",accelCalibratedData[0]));
						objectCluster.mPropertyCluster.put("Low Noise Accelerometer Y",new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.METER_PER_SECOND_SQUARE+"*",accelCalibratedData[1]));
						objectCluster.mPropertyCluster.put("Low Noise Accelerometer Z",new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.METER_PER_SECOND_SQUARE+"*",accelCalibratedData[2]));
						accelerometer.x=accelCalibratedData[0];
						accelerometer.y=accelCalibratedData[1];
						accelerometer.z=accelCalibratedData[2];

					} else {
						objectCluster.mPropertyCluster.put("Low Noise Accelerometer X",new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.METER_PER_SECOND_SQUARE,accelCalibratedData[0]));
						objectCluster.mPropertyCluster.put("Low Noise Accelerometer Y",new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.METER_PER_SECOND_SQUARE,accelCalibratedData[1]));
						objectCluster.mPropertyCluster.put("Low Noise Accelerometer Z",new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.METER_PER_SECOND_SQUARE,accelCalibratedData[2]));
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
				objectCluster.mPropertyCluster.put("Wide Range Accelerometer X",new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iAccelX]));
				objectCluster.mPropertyCluster.put("Wide Range Accelerometer Y",new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iAccelY]));
				objectCluster.mPropertyCluster.put("Wide Range Accelerometer Z",new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iAccelZ]));


				if (mEnableCalibration){
					double[] accelCalibratedData;
					accelCalibratedData=calibrateInertialSensorData(tempData, mAlignmentMatrixWRAccel, mSensitivityMatrixWRAccel, mOffsetVectorWRAccel);
					calibratedData[iAccelX]=accelCalibratedData[0];
					calibratedData[iAccelY]=accelCalibratedData[1];
					calibratedData[iAccelZ]=accelCalibratedData[2];
					if (mDefaultCalibrationParametersDigitalAccel == true) {
						objectCluster.mPropertyCluster.put("Wide Range Accelerometer X",new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.METER_PER_SECOND_SQUARE+"*",calibratedData[iAccelX]));
						objectCluster.mPropertyCluster.put("Wide Range Accelerometer Y",new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.METER_PER_SECOND_SQUARE+"*",calibratedData[iAccelY]));
						objectCluster.mPropertyCluster.put("Wide Range Accelerometer Z",new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.METER_PER_SECOND_SQUARE+"*",calibratedData[iAccelZ]));
						if (((mEnabledSensors & SENSOR_ACCEL) == 0)){
							accelerometer.x=accelCalibratedData[0]; //this is used to calculate quaternions // skip if Low noise is enabled
							accelerometer.y=accelCalibratedData[1];
							accelerometer.z=accelCalibratedData[2];
						}
					} else {
						objectCluster.mPropertyCluster.put("Wide Range Accelerometer X",new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.METER_PER_SECOND_SQUARE,calibratedData[iAccelX]));
						objectCluster.mPropertyCluster.put("Wide Range Accelerometer Y",new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.METER_PER_SECOND_SQUARE,calibratedData[iAccelY]));
						objectCluster.mPropertyCluster.put("Wide Range Accelerometer Z",new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.METER_PER_SECOND_SQUARE,calibratedData[iAccelZ]));
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


				objectCluster.mPropertyCluster.put("Gyroscope X",new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iGyroX]));
				objectCluster.mPropertyCluster.put("Gyroscope Y",new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iGyroY]));
				objectCluster.mPropertyCluster.put("Gyroscope Z",new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iGyroZ]));

				if (mEnableCalibration){
					double[] gyroCalibratedData=calibrateInertialSensorData(tempData, mAlignmentMatrixGyroscope, mSensitivityMatrixGyroscope, mOffsetVectorGyroscope);
					calibratedData[iGyroX]=gyroCalibratedData[0];
					calibratedData[iGyroY]=gyroCalibratedData[1];
					calibratedData[iGyroZ]=gyroCalibratedData[2];
					if (mDefaultCalibrationParametersGyro == true) {
						objectCluster.mPropertyCluster.put("Gyroscope X",new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.DEGREES_PER_SECOND+"*",gyroCalibratedData[0]));
						objectCluster.mPropertyCluster.put("Gyroscope Y",new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.DEGREES_PER_SECOND+"*",gyroCalibratedData[1]));
						objectCluster.mPropertyCluster.put("Gyroscope Z",new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.DEGREES_PER_SECOND+"*",gyroCalibratedData[2]));
						gyroscope.x=gyroCalibratedData[0]*Math.PI/180;
						gyroscope.y=gyroCalibratedData[1]*Math.PI/180;
						gyroscope.z=gyroCalibratedData[2]*Math.PI/180;
					} else {
						objectCluster.mPropertyCluster.put("Gyroscope X",new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.DEGREES_PER_SECOND,gyroCalibratedData[0]));
						objectCluster.mPropertyCluster.put("Gyroscope Y",new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.DEGREES_PER_SECOND,gyroCalibratedData[1]));
						objectCluster.mPropertyCluster.put("Gyroscope Z",new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.DEGREES_PER_SECOND,gyroCalibratedData[2]));
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
				objectCluster.mPropertyCluster.put("Magnetometer X",new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iMagX]));
				objectCluster.mPropertyCluster.put("Magnetometer Y",new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iMagY]));
				objectCluster.mPropertyCluster.put("Magnetometer Z",new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iMagZ]));
				if (mEnableCalibration){
					double[] magCalibratedData=calibrateInertialSensorData(tempData, mAlignmentMatrixMagnetometer, mSensitivityMatrixMagnetometer, mOffsetVectorMagnetometer);
					calibratedData[iMagX]=magCalibratedData[0];
					calibratedData[iMagY]=magCalibratedData[1];
					calibratedData[iMagZ]=magCalibratedData[2];
					if (mDefaultCalibrationParametersMag == true) {
						objectCluster.mPropertyCluster.put("Magnetometer X",new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL_FLUX+"*",magCalibratedData[0]));
						objectCluster.mPropertyCluster.put("Magnetometer Y",new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL_FLUX+"*",magCalibratedData[1]));
						objectCluster.mPropertyCluster.put("Magnetometer Z",new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL_FLUX+"*",magCalibratedData[2]));
						magnetometer.x=magCalibratedData[0];
						magnetometer.y=magCalibratedData[1];
						magnetometer.z=magCalibratedData[2];
					} else {
						objectCluster.mPropertyCluster.put("Magnetometer X",new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL_FLUX,magCalibratedData[0]));
						objectCluster.mPropertyCluster.put("Magnetometer Y",new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL_FLUX,magCalibratedData[1]));
						objectCluster.mPropertyCluster.put("Magnetometer Z",new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL_FLUX,magCalibratedData[2]));
						magnetometer.x=magCalibratedData[0];
						magnetometer.y=magCalibratedData[1];
						magnetometer.z=magCalibratedData[2];
					}
				}
			}

			if ((mEnabledSensors & SENSOR_BATT) > 0) {
				int iA0 = getSignalIndex("VSenseBatt");
				tempData[0] = (double)newPacketInt[iA0];
				objectCluster.mPropertyCluster.put("VSenseBatt",new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iA0]));
				if (mEnableCalibration){
					calibratedData[iA0]=calibrateU12AdcValue(tempData[0],0,3,1)*1.988;
					objectCluster.mPropertyCluster.put("VSenseBatt",new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calibratedData[iA0]));
					mVSenseBattMA.addValue(calibratedData[iA0]);
					checkBattery();
				}
			}
			if ((mEnabledSensors & SENSOR_EXT_ADC_A7) > 0) {
				int iA0 = getSignalIndex("External ADC A7");
				tempData[0] = (double)newPacketInt[iA0];
				objectCluster.mPropertyCluster.put("External ADC A7",new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iA0]));
				if (mEnableCalibration){
					calibratedData[iA0]=calibrateU12AdcValue(tempData[0],0,3,1);
					objectCluster.mPropertyCluster.put("External ADC A7",new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calibratedData[iA0]));
				}
			}
			if ((mEnabledSensors & SENSOR_EXT_ADC_A6) > 0) {
				int iA0 = getSignalIndex("External ADC A6");
				tempData[0] = (double)newPacketInt[iA0];
				objectCluster.mPropertyCluster.put("External ADC A6",new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iA0]));
				if (mEnableCalibration){
					calibratedData[iA0]=calibrateU12AdcValue(tempData[0],0,3,1);
					objectCluster.mPropertyCluster.put("External ADC A6",new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calibratedData[iA0]));
				}
			}
			if ((mEnabledSensors & SENSOR_EXT_ADC_A15) > 0) {
				int iA0 = getSignalIndex("External ADC A15");
				tempData[0] = (double)newPacketInt[iA0];
				objectCluster.mPropertyCluster.put("External ADC A15",new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iA0]));
				if (mEnableCalibration){
					calibratedData[iA0]=calibrateU12AdcValue(tempData[0],0,3,1);
					objectCluster.mPropertyCluster.put("External ADC A15",new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calibratedData[iA0]));
				}
			}
			if ((mEnabledSensors & SENSOR_INT_ADC_A1) > 0) {
				int iA0 = getSignalIndex("Internal ADC A1");
				tempData[0] = (double)newPacketInt[iA0];
				objectCluster.mPropertyCluster.put("Internal ADC A1",new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iA0]));
				if (mEnableCalibration){
					calibratedData[iA0]=calibrateU12AdcValue(tempData[0],0,3,1);
					objectCluster.mPropertyCluster.put("Internal ADC A1",new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calibratedData[iA0]));
				}
			}
			if ((mEnabledSensors & SENSOR_INT_ADC_A12) > 0) {
				int iA0 = getSignalIndex("Internal ADC A12");
				tempData[0] = (double)newPacketInt[iA0];
				objectCluster.mPropertyCluster.put("Internal ADC A12",new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iA0]));
				if (mEnableCalibration){
					calibratedData[iA0]=calibrateU12AdcValue(tempData[0],0,3,1);
					objectCluster.mPropertyCluster.put("Internal ADC A12",new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calibratedData[iA0]));
				}
			}
			if ((mEnabledSensors & SENSOR_INT_ADC_A13) > 0) {
				int iA0 = getSignalIndex("Internal ADC A13");
				tempData[0] = (double)newPacketInt[iA0];
				objectCluster.mPropertyCluster.put("Internal ADC A13",new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iA0]));
				if (mEnableCalibration){
					calibratedData[iA0]=calibrateU12AdcValue(tempData[0],0,3,1);
					objectCluster.mPropertyCluster.put("Internal ADC A13",new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calibratedData[iA0]));
				}
			}
			if ((mEnabledSensors & SENSOR_INT_ADC_A14) > 0) {
				int iA0 = getSignalIndex("Internal ADC A14");
				tempData[0] = (double)newPacketInt[iA0];
				objectCluster.mPropertyCluster.put("Internal ADC A14",new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iA0]));
				if (mEnableCalibration){
					calibratedData[iA0]=calibrateU12AdcValue(tempData[0],0,3,1);
					objectCluster.mPropertyCluster.put("Internal ADC A14",new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calibratedData[iA0]));
				}
			}
			if (((mEnabledSensors & SENSOR_ACCEL) > 0 || (mEnabledSensors & SENSOR_DACCEL) > 0) && ((mEnabledSensors & 0xFF)& SENSOR_GYRO) > 0 && ((mEnabledSensors & 0xFF)& SENSOR_MAG) > 0 && mOrientationEnabled ){
				if (mEnableCalibration){
					if (mOrientationAlgo==null){
						mOrientationAlgo = new GradDes3DOrientation(0.4, (double)1/mShimmerSamplingRate, 1, 0, 0,0);
					}
					Quaternion q = mOrientationAlgo.update(accelerometer.x,accelerometer.y,accelerometer.z, gyroscope.x,gyroscope.y,gyroscope.z, magnetometer.x,magnetometer.y,magnetometer.z);					double theta, Rx, Ry, Rz, rho;
					rho = Math.acos(q.q1);
					theta = rho * 2;
					Rx = q.q2 / Math.sin(rho);
					Ry = q.q3 / Math.sin(rho);
					Rz = q.q4 / Math.sin(rho);
					objectCluster.mPropertyCluster.put("Axis Angle A",new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL,theta));
					objectCluster.mPropertyCluster.put("Axis Angle X",new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL,Rx));
					objectCluster.mPropertyCluster.put("Axis Angle Y",new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL,Ry));
					objectCluster.mPropertyCluster.put("Axis Angle Z",new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL,Rz));
					objectCluster.mPropertyCluster.put("Quaternion 0",new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL,q.q1));
					objectCluster.mPropertyCluster.put("Quaternion 1",new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL,q.q2));
					objectCluster.mPropertyCluster.put("Quaternion 2",new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL,q.q3));
					objectCluster.mPropertyCluster.put("Quaternion 3",new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL,q.q4));
				}
			}

			if ((mEnabledSensors & SENSOR_EXG1_24BIT) >0){
				int iexg1ch1 = getSignalIndex("EXG1 24Bit CH1");
				int iexg1ch2 = getSignalIndex("EXG1 24Bit CH2");
				int iexg1sta = getSignalIndex("EXG1 STATUS");
				double exg1ch1 = (double)newPacketInt[iexg1ch1];
				double exg1ch2 = (double)newPacketInt[iexg1ch2];
				double exg1sta = (double)newPacketInt[iexg1sta];
				objectCluster.mPropertyCluster.put("EXG1 STATUS",new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg1sta));
				if (mEnableCalibration){
					double calexg1ch1 = exg1ch1 *(((2.42*1000)/mEXG1CH1GainValue)/(Math.pow(2,23)-1));
					double calexg1ch2 = exg1ch2 *(((2.42*1000)/mEXG1CH2GainValue)/(Math.pow(2,23)-1));
					if (isEXGUsingDefaultECGConfiguration()){
						objectCluster.mPropertyCluster.put("ECG LL-RA",new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg1ch1));
						objectCluster.mPropertyCluster.put("ECG LA-RA",new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg1ch2));
						objectCluster.mPropertyCluster.put("ECG LL-RA",new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calexg1ch1));
						objectCluster.mPropertyCluster.put("ECG LA-RA",new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calexg1ch2));
					} else if (isEXGUsingDefaultEMGConfiguration()){
						objectCluster.mPropertyCluster.put("EMG CH1",new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg1ch1));
						objectCluster.mPropertyCluster.put("EMG CH2",new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg1ch2));
						objectCluster.mPropertyCluster.put("EMG CH1",new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calexg1ch1));
						objectCluster.mPropertyCluster.put("EMG CH2",new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calexg1ch2));
					} else {
						objectCluster.mPropertyCluster.put("EXG1 CH1",new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg1ch1));
						objectCluster.mPropertyCluster.put("EXG1 CH2",new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg1ch2));
						objectCluster.mPropertyCluster.put("EXG1 CH1",new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calexg1ch1));
						objectCluster.mPropertyCluster.put("EXG1 CH2",new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calexg1ch2));
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

				objectCluster.mPropertyCluster.put("EXG2 STATUS",new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg2sta));
				if (mEnableCalibration){
					double calexg2ch1 = exg2ch1 *(((2.42*1000)/mEXG2CH1GainValue)/(Math.pow(2,23)-1));
					double calexg2ch2 = exg2ch2 *(((2.42*1000)/mEXG2CH2GainValue)/(Math.pow(2,23)-1));
					if (isEXGUsingDefaultECGConfiguration()){
						objectCluster.mPropertyCluster.put("EXG2 CH1",new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg2ch1));
						objectCluster.mPropertyCluster.put("ECG Vx-RL",new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg2ch2));
						objectCluster.mPropertyCluster.put("EXG2 CH1",new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calexg2ch1));
						objectCluster.mPropertyCluster.put("ECG Vx-RL",new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calexg2ch2));
					}
					else if (isEXGUsingDefaultEMGConfiguration()){
						objectCluster.mPropertyCluster.put("EXG2 CH1",new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,0));
						objectCluster.mPropertyCluster.put("EXG2 CH2",new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,0));
						objectCluster.mPropertyCluster.put("EXG2 CH1",new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,0));
						objectCluster.mPropertyCluster.put("EXG2 CH2",new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,0));
					} else {
						objectCluster.mPropertyCluster.put("EXG2 CH1",new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg2ch1));
						objectCluster.mPropertyCluster.put("EXG2 CH2",new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg2ch2));
						objectCluster.mPropertyCluster.put("EXG2 CH1",new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calexg2ch1));
						objectCluster.mPropertyCluster.put("EXG2 CH2",new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calexg2ch2));	
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
				objectCluster.mPropertyCluster.put("EXG1 STATUS",new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg1sta));
				if (mEnableCalibration){
					double calexg1ch1 = exg1ch1 *(((2.42*1000)/(mEXG1CH1GainValue*2))/(Math.pow(2,15)-1));
					double calexg1ch2 = exg1ch2 *(((2.42*1000)/(mEXG1CH2GainValue*2))/(Math.pow(2,15)-1));
					if (isEXGUsingDefaultECGConfiguration()){
						objectCluster.mPropertyCluster.put("ECG LL-RA",new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg1ch1));
						objectCluster.mPropertyCluster.put("ECG LA-RA",new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg1ch2));
						objectCluster.mPropertyCluster.put("ECG LL-RA",new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calexg1ch1));
						objectCluster.mPropertyCluster.put("ECG LA-RA",new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calexg1ch2));
					} else if (isEXGUsingDefaultEMGConfiguration()){
						objectCluster.mPropertyCluster.put("EMG CH1",new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg1ch1));
						objectCluster.mPropertyCluster.put("EMG CH2",new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg1ch2));
						objectCluster.mPropertyCluster.put("EMG CH1",new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calexg1ch1));
						objectCluster.mPropertyCluster.put("EMG CH2",new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calexg1ch2));
					} else {
						objectCluster.mPropertyCluster.put("EXG1 CH1 16Bit",new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg1ch1));
						objectCluster.mPropertyCluster.put("EXG1 CH2 16Bit",new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg1ch2));
						objectCluster.mPropertyCluster.put("EXG1 CH1 16Bit",new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calexg1ch1));
						objectCluster.mPropertyCluster.put("EXG1 CH2 16Bit",new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calexg1ch2));
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

				objectCluster.mPropertyCluster.put("EXG2 STATUS",new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg2sta));
				if (mEnableCalibration){
					double calexg2ch1 = ((exg2ch1)) *(((2.42*1000)/(mEXG2CH1GainValue*2))/(Math.pow(2,15)-1));
					double calexg2ch2 = ((exg2ch2)) *(((2.42*1000)/(mEXG2CH2GainValue*2))/(Math.pow(2,15)-1));
					if (isEXGUsingDefaultECGConfiguration()){
						objectCluster.mPropertyCluster.put("EXG2 CH1",new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg2ch1));
						objectCluster.mPropertyCluster.put("ECG Vx-RL",new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg2ch2));
						objectCluster.mPropertyCluster.put("EXG2 CH1",new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calexg2ch1));
						objectCluster.mPropertyCluster.put("ECG Vx-RL",new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calexg2ch2));
					}
					else if (isEXGUsingDefaultEMGConfiguration()){
						objectCluster.mPropertyCluster.put("EXG2 CH1",new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,0));
						objectCluster.mPropertyCluster.put("EXG2 CH2",new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,0));
						objectCluster.mPropertyCluster.put("EXG2 CH1",new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,0));
						objectCluster.mPropertyCluster.put("EXG2 CH2",new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,0));
					} else {
						objectCluster.mPropertyCluster.put("EXG2 CH1 16Bit",new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg2ch1));
						objectCluster.mPropertyCluster.put("EXG2 CH2 16Bit",new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg2ch2));
						objectCluster.mPropertyCluster.put("EXG2 CH1 16Bit",new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calexg2ch1));
						objectCluster.mPropertyCluster.put("EXG2 CH2 16Bit",new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calexg2ch2));
					}
				}
			}

			if ((mEnabledSensors & SENSOR_BMP180) >0){
				int iUT = getSignalIndex("Temperature");
				int iUP = getSignalIndex("Pressure");
				double UT = (double)newPacketInt[iUT];
				double UP = (double)newPacketInt[iUP];
				UP=UP/Math.pow(2,8-mPressureResolution);
				objectCluster.mPropertyCluster.put("Pressure",new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,UP));
				objectCluster.mPropertyCluster.put("Temperature",new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,UT));
				if (mEnableCalibration){
					double[] bmp180caldata= calibratePressureSensorData(UP,UT);
					objectCluster.mPropertyCluster.put("Pressure",new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.KPASCAL,bmp180caldata[0]/1000));
					objectCluster.mPropertyCluster.put("Temperature",new FormatCluster(CHANNEL_TYPE.CAL.toString(),"Celsius",bmp180caldata[1]));
				}
			}

			if ((mEnabledSensors & SENSOR_BRIDGE_AMP) > 0) {
				int iBAHigh = getSignalIndex("Bridge Amplifier High");
				int iBALow = getSignalIndex("Bridge Amplifier Low");
				objectCluster.mPropertyCluster.put("Bridge Amplifier High",new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iBAHigh]));
				objectCluster.mPropertyCluster.put("Bridge Amplifier Low",new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iBALow]));
				if (mEnableCalibration){
					tempData[0] = (double)newPacketInt[iBAHigh];
					tempData[1] = (double)newPacketInt[iBALow];
					calibratedData[iBAHigh]=calibrateU12AdcValue(tempData[0],60,3,551);
					calibratedData[iBALow]=calibrateU12AdcValue(tempData[1],1950,3,183.7);
					objectCluster.mPropertyCluster.put("Bridge Amplifier High",new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calibratedData[iBAHigh]));
					objectCluster.mPropertyCluster.put("Bridge Amplifier Low",new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calibratedData[iBALow]));	
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
					if (mHardwareVersion!=HW_ID.SHIMMER_3){
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
					if (mHardwareVersion!=HW_ID.SHIMMER_3){
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
					if (mHardwareVersion!=HW_ID.SHIMMER_3){
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
					if (mHardwareVersion!=HW_ID.SHIMMER_3){
						p1 = 4.5580e-04;
						p2 = -0.3014;
					} else {
						p1 = 4.4513e-04;
						p2 = -0.3193;
					}
				}
				objectCluster.mPropertyCluster.put("GSR",new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iGSR]));
				if (mEnableCalibration){
					calibratedData[iGSR] = calibrateGsrData(tempData[0],p1,p2);
					objectCluster.mPropertyCluster.put("GSR",new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.KOHMS,calibratedData[iGSR]));
				}
			}
		} else { //start of Shimmer2

			if ((mEnabledSensors & SENSOR_ACCEL) > 0){
				int iAccelX=getSignalIndex("Accelerometer X"); //find index
				int iAccelY=getSignalIndex("Accelerometer Y"); //find index
				int iAccelZ=getSignalIndex("Accelerometer Z"); //find index
				objectCluster.mPropertyCluster.put("Accelerometer X",new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iAccelX]));
				objectCluster.mPropertyCluster.put("Accelerometer Y",new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iAccelY]));
				objectCluster.mPropertyCluster.put("Accelerometer Z",new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iAccelZ]));
				if (mEnableCalibration){
					tempData[0]=(double)newPacketInt[iAccelX];
					tempData[1]=(double)newPacketInt[iAccelY];
					tempData[2]=(double)newPacketInt[iAccelZ];
					double[] accelCalibratedData=calibrateInertialSensorData(tempData, mAlignmentMatrixAnalogAccel, mSensitivityMatrixAnalogAccel, mOffsetVectorAnalogAccel);
					calibratedData[iAccelX]=accelCalibratedData[0];
					calibratedData[iAccelY]=accelCalibratedData[1];
					calibratedData[iAccelZ]=accelCalibratedData[2];
					if (mDefaultCalibrationParametersAccel == true) {
						objectCluster.mPropertyCluster.put("Accelerometer X",new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.METER_PER_SECOND_SQUARE+"*",accelCalibratedData[0]));
						objectCluster.mPropertyCluster.put("Accelerometer Y",new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.METER_PER_SECOND_SQUARE+"*",accelCalibratedData[1]));
						objectCluster.mPropertyCluster.put("Accelerometer Z",new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.METER_PER_SECOND_SQUARE+"*",accelCalibratedData[2]));
						accelerometer.x=accelCalibratedData[0];
						accelerometer.y=accelCalibratedData[1];
						accelerometer.z=accelCalibratedData[2];
					} else {
						objectCluster.mPropertyCluster.put("Accelerometer X",new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.METER_PER_SECOND_SQUARE,accelCalibratedData[0]));
						objectCluster.mPropertyCluster.put("Accelerometer Y",new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.METER_PER_SECOND_SQUARE,accelCalibratedData[1]));
						objectCluster.mPropertyCluster.put("Accelerometer Z",new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.METER_PER_SECOND_SQUARE,accelCalibratedData[2]));
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
				objectCluster.mPropertyCluster.put("Gyroscope X",new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iGyroX]));
				objectCluster.mPropertyCluster.put("Gyroscope Y",new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iGyroY]));
				objectCluster.mPropertyCluster.put("Gyroscope Z",new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iGyroZ]));
				if (mEnableCalibration){
					tempData[0]=(double)newPacketInt[iGyroX];
					tempData[1]=(double)newPacketInt[iGyroY];
					tempData[2]=(double)newPacketInt[iGyroZ];
					double[] gyroCalibratedData=calibrateInertialSensorData(tempData, mAlignmentMatrixGyroscope, mSensitivityMatrixGyroscope, mOffsetVectorGyroscope);
					calibratedData[iGyroX]=gyroCalibratedData[0];
					calibratedData[iGyroY]=gyroCalibratedData[1];
					calibratedData[iGyroZ]=gyroCalibratedData[2];
					if (mDefaultCalibrationParametersGyro == true) {
						objectCluster.mPropertyCluster.put("Gyroscope X",new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.DEGREES_PER_SECOND+"*",gyroCalibratedData[0]));
						objectCluster.mPropertyCluster.put("Gyroscope Y",new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.DEGREES_PER_SECOND+"*",gyroCalibratedData[1]));
						objectCluster.mPropertyCluster.put("Gyroscope Z",new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.DEGREES_PER_SECOND+"*",gyroCalibratedData[2]));
						gyroscope.x=gyroCalibratedData[0]*Math.PI/180;
						gyroscope.y=gyroCalibratedData[1]*Math.PI/180;
						gyroscope.z=gyroCalibratedData[2]*Math.PI/180;
					} else {
						objectCluster.mPropertyCluster.put("Gyroscope X",new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.DEGREES_PER_SECOND,gyroCalibratedData[0]));
						objectCluster.mPropertyCluster.put("Gyroscope Y",new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.DEGREES_PER_SECOND,gyroCalibratedData[1]));
						objectCluster.mPropertyCluster.put("Gyroscope Z",new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.DEGREES_PER_SECOND,gyroCalibratedData[2]));
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
				objectCluster.mPropertyCluster.put("Magnetometer X",new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iMagX]));
				objectCluster.mPropertyCluster.put("Magnetometer Y",new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iMagY]));
				objectCluster.mPropertyCluster.put("Magnetometer Z",new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iMagZ]));
				if (mEnableCalibration){
					tempData[0]=(double)newPacketInt[iMagX];
					tempData[1]=(double)newPacketInt[iMagY];
					tempData[2]=(double)newPacketInt[iMagZ];
					double[] magCalibratedData=calibrateInertialSensorData(tempData, mAlignmentMatrixMagnetometer, mSensitivityMatrixMagnetometer, mOffsetVectorMagnetometer);
					calibratedData[iMagX]=magCalibratedData[0];
					calibratedData[iMagY]=magCalibratedData[1];
					calibratedData[iMagZ]=magCalibratedData[2];
					if (mDefaultCalibrationParametersMag == true) {
						objectCluster.mPropertyCluster.put("Magnetometer X",new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL_FLUX+"*",magCalibratedData[0]));
						objectCluster.mPropertyCluster.put("Magnetometer Y",new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL_FLUX+"*",magCalibratedData[1]));
						objectCluster.mPropertyCluster.put("Magnetometer Z",new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL_FLUX+"*",magCalibratedData[2]));
						magnetometer.x=magCalibratedData[0];
						magnetometer.y=magCalibratedData[1];
						magnetometer.z=magCalibratedData[2];
					} else {
						objectCluster.mPropertyCluster.put("Magnetometer X",new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL_FLUX,magCalibratedData[0]));
						objectCluster.mPropertyCluster.put("Magnetometer Y",new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL_FLUX,magCalibratedData[1]));
						objectCluster.mPropertyCluster.put("Magnetometer Z",new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL_FLUX,magCalibratedData[2]));
						magnetometer.x=magCalibratedData[0];
						magnetometer.y=magCalibratedData[1];
						magnetometer.z=magCalibratedData[2];
					}
				}
			}


			if ((mEnabledSensors & SENSOR_ACCEL) > 0 && (mEnabledSensors & SENSOR_GYRO) > 0 && (mEnabledSensors & SENSOR_MAG) > 0 && mOrientationEnabled ){
				if (mEnableCalibration){
					if (mOrientationAlgo==null){
						mOrientationAlgo = new GradDes3DOrientation(0.4, (double)1/mShimmerSamplingRate, 1, 0, 0,0);
					}
					Quaternion q = mOrientationAlgo.update(accelerometer.x,accelerometer.y,accelerometer.z, gyroscope.x,gyroscope.y,gyroscope.z, magnetometer.x,magnetometer.y,magnetometer.z);
					double theta, Rx, Ry, Rz, rho;
					rho = Math.acos(q.q1);
					theta = rho * 2;
					Rx = q.q2 / Math.sin(rho);
					Ry = q.q3 / Math.sin(rho);
					Rz = q.q4 / Math.sin(rho);
					objectCluster.mPropertyCluster.put("Axis Angle A",new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL,theta));
					objectCluster.mPropertyCluster.put("Axis Angle X",new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL,Rx));
					objectCluster.mPropertyCluster.put("Axis Angle Y",new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL,Ry));
					objectCluster.mPropertyCluster.put("Axis Angle Z",new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL,Rz));
					objectCluster.mPropertyCluster.put("Quaternion 0",new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL,q.q1));
					objectCluster.mPropertyCluster.put("Quaternion 1",new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL,q.q2));
					objectCluster.mPropertyCluster.put("Quaternion 2",new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL,q.q3));
					objectCluster.mPropertyCluster.put("Quaternion 3",new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL,q.q4));
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
				objectCluster.mPropertyCluster.put("GSR",new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iGSR]));
				if (mEnableCalibration){
					tempData[0] = (double)newPacketInt[iGSR];
					calibratedData[iGSR] = calibrateGsrData(tempData[0],p1,p2);
					objectCluster.mPropertyCluster.put("GSR",new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.KOHMS,calibratedData[iGSR]));
				}
			}
			if ((mEnabledSensors & SENSOR_ECG) > 0) {
				int iECGRALL = getSignalIndex("ECG RA LL");
				int iECGLALL = getSignalIndex("ECG LA LL");
				objectCluster.mPropertyCluster.put("ECG RA-LL",new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iECGRALL]));
				objectCluster.mPropertyCluster.put("ECG LA-LL",new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iECGLALL]));
				if (mEnableCalibration){
					tempData[0] = (double)newPacketInt[iECGRALL];
					tempData[1] = (double)newPacketInt[iECGLALL];
					calibratedData[iECGRALL]=calibrateU12AdcValue(tempData[0],OffsetECGRALL,3,GainECGRALL);
					calibratedData[iECGLALL]=calibrateU12AdcValue(tempData[1],OffsetECGLALL,3,GainECGLALL);
					if (mDefaultCalibrationParametersECG == true) {
						objectCluster.mPropertyCluster.put("ECG RA-LL",new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS+"*",calibratedData[iECGRALL]));
						objectCluster.mPropertyCluster.put("ECG LA-LL",new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS+"*",calibratedData[iECGLALL]));
					} else {
						objectCluster.mPropertyCluster.put("ECG RA-LL",new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calibratedData[iECGRALL]));
						objectCluster.mPropertyCluster.put("ECG LA-LL",new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calibratedData[iECGLALL]));
					}
				}
			}
			if ((mEnabledSensors & SENSOR_EMG) > 0) {
				int iEMG = getSignalIndex("EMG");
				objectCluster.mPropertyCluster.put("EMG",new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iEMG]));
				if (mEnableCalibration){
					tempData[0] = (double)newPacketInt[iEMG];
					calibratedData[iEMG]=calibrateU12AdcValue(tempData[0],OffsetEMG,3,GainEMG);
					if (mDefaultCalibrationParametersEMG == true){
						objectCluster.mPropertyCluster.put("EMG",new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS+"*",calibratedData[iEMG]));
					} else {
						objectCluster.mPropertyCluster.put("EMG",new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calibratedData[iEMG]));
					}
				}
			}
			if ((mEnabledSensors & SENSOR_BRIDGE_AMP) > 0) {
				int iBAHigh = getSignalIndex("Bridge Amplifier High");
				int iBALow = getSignalIndex("Bridge Amplifier Low");
				objectCluster.mPropertyCluster.put("Bridge Amplifier High",new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iBAHigh]));
				objectCluster.mPropertyCluster.put("Bridge Amplifier Low",new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iBALow]));
				if (mEnableCalibration){
					tempData[0] = (double)newPacketInt[iBAHigh];
					tempData[1] = (double)newPacketInt[iBALow];
					calibratedData[iBAHigh]=calibrateU12AdcValue(tempData[0],60,3,551);
					calibratedData[iBALow]=calibrateU12AdcValue(tempData[1],1950,3,183.7);
					objectCluster.mPropertyCluster.put("Bridge Amplifier High",new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calibratedData[iBAHigh]));
					objectCluster.mPropertyCluster.put("Bridge Amplifier Low",new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calibratedData[iBALow]));	
				}
			}
			if ((mEnabledSensors & SENSOR_HEART) > 0) {
				int iHeartRate = getSignalIndex("Heart Rate");
				tempData[0] = (double)newPacketInt[iHeartRate];
				objectCluster.mPropertyCluster.put("Heart Rate",new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,tempData[0]));
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
					objectCluster.mPropertyCluster.put("Heart Rate",new FormatCluster(CHANNEL_TYPE.CAL.toString(),"BPM",calibratedData[iHeartRate]));	
				}
			}
			if ((mEnabledSensors& SENSOR_EXP_BOARD_A0) > 0) {
				int iA0 = getSignalIndex("Exp Board A0");
				tempData[0] = (double)newPacketInt[iA0];
				if (getPMux()==0){
					objectCluster.mPropertyCluster.put("ExpBoard A0",new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iA0]));
					if (mEnableCalibration){
						calibratedData[iA0]=calibrateU12AdcValue(tempData[0],0,3,1);
						objectCluster.mPropertyCluster.put("ExpBoard A0",new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calibratedData[iA0]));
					}
				} else {
					objectCluster.mPropertyCluster.put("VSenseReg",new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iA0]));
					if (mEnableCalibration){
						calibratedData[iA0]=calibrateU12AdcValue(tempData[0],0,3,1)*1.988;
						objectCluster.mPropertyCluster.put("VSenseReg",new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calibratedData[iA0]));
					}

				}
			}					
			if ((mEnabledSensors & SENSOR_EXP_BOARD_A7) > 0) {
				int iA7 = getSignalIndex("Exp Board A7");
				tempData[0] = (double)newPacketInt[iA7];
				if (getPMux()==0){
					objectCluster.mPropertyCluster.put("ExpBoard A7",new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iA7]));
					if (mEnableCalibration){
						calibratedData[iA7]=calibrateU12AdcValue(tempData[0],0,3,1);
						objectCluster.mPropertyCluster.put("ExpBoard A7",new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calibratedData[iA7]));
					}
				} 
			}
			if  ((mEnabledSensors & SENSOR_BATT) > 0) {
				int iA0 = getSignalIndex("Exp Board A0");
				objectCluster.mPropertyCluster.put("VSenseReg",new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iA0]));

				int iA7 = getSignalIndex("Exp Board A7");
				objectCluster.mPropertyCluster.put("VSenseBatt",new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iA7]));


				if (mEnableCalibration){
					tempData[0] = (double)newPacketInt[iA0];
					calibratedData[iA0]=calibrateU12AdcValue(tempData[0],0,3,1)*1.988;
					objectCluster.mPropertyCluster.put("VSenseReg",new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calibratedData[iA0]));

					tempData[0] = (double)newPacketInt[iA7];
					calibratedData[iA7]=calibrateU12AdcValue(tempData[0],0,3,1)*2;
					objectCluster.mPropertyCluster.put("VSenseBatt",new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calibratedData[iA7]));	
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
	protected long[] parsedData(byte[] data,String[] dataType){
		
		int iData=0;
		long[] formattedData=new long[dataType.length];

		for (int i=0;i<dataType.length;i++)
			if (dataType[i]=="u8") {
				formattedData[i]=(int)0xFF & data[iData];
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
			}  else if (dataType[i]=="u24") {				
				long xmsb =((long)(data[iData+2] & 0xFF) << 16);
				long msb =((long)(data[iData+1] & 0xFF) << 8);
				long lsb =((long)(data[iData+0] & 0xFF));
				formattedData[i]=xmsb + msb + lsb;
				iData=iData+3;
			} else if (dataType[i]=="i24r") {
				long xmsb =((long)(data[iData+0] & 0xFF) << 16);
				long msb =((long)(data[iData+1] & 0xFF) << 8);
				long lsb =((long)(data[iData+2] & 0xFF));
				formattedData[i]=calculatetwoscomplement((int)(xmsb + msb + lsb),24);
				iData=iData+3;
			} else if (dataType[i]=="u32signed") {
				//TODO: should this be called i32?
				//TODO: are the indexes incorrect, current '+1' to '+4', should this be '+0' to '+3' the the others listed here?
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
			//TODO: Newly added below up to u72 - check
			} else if (dataType[i]=="u32") {
				long forthmsb =(((long)data[iData+3] & 0xFF) << 24);
				long thirdmsb =(((long)data[iData+2] & 0xFF) << 16);
				long msb =(((long)data[iData+1] & 0xFF) << 8);
				long lsb =(((long)data[iData+0] & 0xFF) << 0);
				formattedData[i]=forthmsb + thirdmsb + msb + lsb;
				iData=iData+4;
			} else if (dataType[i]=="u32r") {
				long forthmsb =(((long)data[iData+0] & 0xFF) << 24);
				long thirdmsb =(((long)data[iData+1] & 0xFF) << 16);
				long msb =(((long)data[iData+2] & 0xFF) << 8);
				long lsb =(((long)data[iData+3] & 0xFF) << 0);
				formattedData[i]=forthmsb + thirdmsb + msb + lsb;
				iData=iData+4;
			} else if (dataType[i]=="i32") {
				long xxmsb =((long)(data[iData+3] & 0xFF) << 24);
				long xmsb =((long)(data[iData+2] & 0xFF) << 16);
				long msb =((long)(data[iData+1] & 0xFF) << 8);
				long lsb =((long)(data[iData+0] & 0xFF) << 0);
				formattedData[i]=calculatetwoscomplement((long)(xxmsb + xmsb + msb + lsb),32);
				iData=iData+4;
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
	
	/**
	 * Data Methods
	 */  
	protected int[] formatDataPacketReverse(byte[] data,String[] dataType){
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

	private int calculatetwoscomplement(int signedData, int bitLength){
		int newData=signedData;
		if (signedData>=(1<<(bitLength-1))) {
			newData=-((signedData^(int)(Math.pow(2, bitLength)-1))+1);
		}

		return newData;
	}

	private long calculatetwoscomplement(long signedData, int bitLength){
		long newData=signedData;
		if (signedData>=(1L<<(bitLength-1))) {
			newData=-((signedData^(long)(Math.pow(2, bitLength)-1))+1);
		}

		return newData;
	}
	
	protected int getSignalIndex(String signalName) {
		int iSignal=-1; //better to fail //used to be -1, putting to zero ensure it works eventhough it might be wrong SR30
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
	protected void interpretDataPacketFormat(int nC, byte[] signalid)
	{
		String [] signalNameArray=new String[MAX_NUMBER_OF_SIGNALS];
		String [] signalDataTypeArray=new String[MAX_NUMBER_OF_SIGNALS];
		
		if (mHardwareVersion==HW_ID.SHIMMER_SR30 || mHardwareVersion==HW_ID.SHIMMER_3){
			signalNameArray[0]=Configuration.Shimmer3.ObjectClusterSensorName.TIMESTAMP;
		}
		else{
			signalNameArray[0]=Configuration.Shimmer2.ObjectClusterSensorName.TIMESTAMP;
		}
		
		int packetSize=mTimeStampPacketByteSize; // Time stamp
		if (mTimeStampPacketByteSize==2){
			signalDataTypeArray[0]="u16";
		} else if (mTimeStampPacketByteSize==3) {
			signalDataTypeArray[0]="u24";
		}
		
		int enabledSensors= 0x00;
		for (int i=0;i<nC;i++) {
			if ((byte)signalid[i]==(byte)0x00){
				if (mHardwareVersion==HW_ID.SHIMMER_SR30 || mHardwareVersion==HW_ID.SHIMMER_3){
					signalNameArray[i+1]=Shimmer3.ObjectClusterSensorName.ACCEL_LN_X;
					signalDataTypeArray[i+1] = "i16";
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|Configuration.Shimmer3.SensorBitmap.SENSOR_A_ACCEL);
				} 
				else {
					signalNameArray[i+1]=Shimmer2.ObjectClusterSensorName.ACCEL_X;
					signalDataTypeArray[i+1] = "u12";
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|SENSOR_ACCEL);
				}
			}
			else if ((byte)signalid[i]==(byte)0x01){
				if (mHardwareVersion==HW_ID.SHIMMER_SR30 || mHardwareVersion==HW_ID.SHIMMER_3){
					signalNameArray[i+1]=Shimmer3.ObjectClusterSensorName.ACCEL_LN_Y;
					signalDataTypeArray[i+1] = "i16";
					packetSize=packetSize+2; 
					enabledSensors= (enabledSensors|Configuration.Shimmer3.SensorBitmap.SENSOR_A_ACCEL);
				} 
				else {
					signalNameArray[i+1]=Shimmer2.ObjectClusterSensorName.ACCEL_Y;
					signalDataTypeArray[i+1] = "u12";
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|SENSOR_ACCEL);
				}
			}
			else if ((byte)signalid[i]==(byte)0x02){
				if (mHardwareVersion==HW_ID.SHIMMER_SR30 || mHardwareVersion==HW_ID.SHIMMER_3){
					signalNameArray[i+1]=Shimmer3.ObjectClusterSensorName.ACCEL_LN_Z;
					signalDataTypeArray[i+1] = "i16";
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|Configuration.Shimmer3.SensorBitmap.SENSOR_A_ACCEL);
				} 
				else {
					signalNameArray[i+1]=Shimmer2.ObjectClusterSensorName.ACCEL_Z;
					signalDataTypeArray[i+1] = "u12";
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|SENSOR_ACCEL);
				}
			}
			else if ((byte)signalid[i]==(byte)0x03){

				if (mHardwareVersion==HW_ID.SHIMMER_SR30){
					signalNameArray[i+1]=Shimmer3.ObjectClusterSensorName.GYRO_X;
					signalDataTypeArray[i+1] = "i16";
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|Configuration.Shimmer3.SensorBitmap.SENSOR_GYRO);
				} 
				else if (mHardwareVersion==HW_ID.SHIMMER_3){
					signalNameArray[i+1]=Shimmer3.ObjectClusterSensorName.BATTERY; //should be the battery but this will do for now
					signalDataTypeArray[i+1] = "i16";
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|Configuration.Shimmer3.SensorBitmap.SENSOR_VBATT);	
				} 
				else {
					signalNameArray[i+1]=Shimmer2.ObjectClusterSensorName.GYRO_X;
					signalDataTypeArray[i+1] = "u12";
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|SENSOR_GYRO);
				}
			}
			else if ((byte)signalid[i]==(byte)0x04){

				if (mHardwareVersion==HW_ID.SHIMMER_SR30){
					signalNameArray[i+1]=Shimmer3.ObjectClusterSensorName.GYRO_Y;
					signalDataTypeArray[i+1] = "i16";
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|Configuration.Shimmer3.SensorBitmap.SENSOR_GYRO);
				} 
				else if (mHardwareVersion==HW_ID.SHIMMER_3){
					signalDataTypeArray[i+1] = "i16";
					packetSize=packetSize+2;
					signalNameArray[i+1]=Shimmer3.ObjectClusterSensorName.ACCEL_WR_X;
					enabledSensors= (enabledSensors|Configuration.Shimmer3.SensorBitmap.SENSOR_D_ACCEL);
				} 
				else {
					signalNameArray[i+1]=Shimmer2.ObjectClusterSensorName.GYRO_Y;
					signalDataTypeArray[i+1] = "u12";
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|SENSOR_GYRO);
				}
			}
			else if ((byte)signalid[i]==(byte)0x05){
				if (mHardwareVersion==HW_ID.SHIMMER_SR30){
					signalNameArray[i+1]=Shimmer3.ObjectClusterSensorName.GYRO_Z;
					signalDataTypeArray[i+1] = "i16";
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|Configuration.Shimmer3.SensorBitmap.SENSOR_GYRO);
				}
				else if (mHardwareVersion==HW_ID.SHIMMER_3){
					signalDataTypeArray[i+1] = "i16";
					packetSize=packetSize+2;
					signalNameArray[i+1]=Shimmer3.ObjectClusterSensorName.ACCEL_WR_Y;
					enabledSensors= (enabledSensors|Configuration.Shimmer3.SensorBitmap.SENSOR_D_ACCEL);
				}
				else {
					signalNameArray[i+1]=Shimmer2.ObjectClusterSensorName.GYRO_Z;
					signalDataTypeArray[i+1] = "u12";
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|SENSOR_GYRO);
				}
			}
			else if ((byte)signalid[i]==(byte)0x06){
				if(mHardwareVersion==HW_ID.SHIMMER_SR30){
					signalNameArray[i+1]=Shimmer3.ObjectClusterSensorName.BATTERY; //should be the battery but this will do for now
					signalDataTypeArray[i+1] = "i16";
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|Configuration.Shimmer3.SensorBitmap.SENSOR_VBATT);	
				} 
				else if (mHardwareVersion==HW_ID.SHIMMER_3){
					signalDataTypeArray[i+1] = "i16";
					packetSize=packetSize+2;
					signalNameArray[i+1]=Shimmer3.ObjectClusterSensorName.ACCEL_WR_Z;
					enabledSensors= (enabledSensors|Configuration.Shimmer3.SensorBitmap.SENSOR_D_ACCEL);
				} 
				else {
					signalNameArray[i+1]=Shimmer2.ObjectClusterSensorName.MAG_X;
					signalDataTypeArray[i+1] = "i16";
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|SENSOR_MAG);
				}
			}
			else if ((byte)signalid[i]==(byte)0x07){
				if(mHardwareVersion==HW_ID.SHIMMER_SR30){
					signalDataTypeArray[i+1] = "i16";
					packetSize=packetSize+2;
					signalNameArray[i+1]=Shimmer3.ObjectClusterSensorName.ACCEL_WR_X;
					enabledSensors= (enabledSensors|Configuration.Shimmer3.SensorBitmap.SENSOR_D_ACCEL);
				} 
				else if(mHardwareVersion==HW_ID.SHIMMER_3){
					signalNameArray[i+1]=Shimmer3.ObjectClusterSensorName.MAG_X;
					signalDataTypeArray[i+1] = "i16r";			
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|Configuration.Shimmer3.SensorBitmap.SENSOR_MAG);
				} 
				else {
					signalDataTypeArray[i+1] = "i16";
					packetSize=packetSize+2;
					signalNameArray[i+1]=Shimmer2.ObjectClusterSensorName.MAG_Y;
					enabledSensors= (enabledSensors|SENSOR_MAG);
				}
			}
			else if ((byte)signalid[i]==(byte)0x08)
			{	
				if(mHardwareVersion==HW_ID.SHIMMER_SR30){
					signalDataTypeArray[i+1] = "i16";
					packetSize=packetSize+2;
					signalNameArray[i+1]=Shimmer3.ObjectClusterSensorName.ACCEL_WR_Y;
					enabledSensors= (enabledSensors|Configuration.Shimmer3.SensorBitmap.SENSOR_D_ACCEL);
				} 
				else if(mHardwareVersion==HW_ID.SHIMMER_3){
					signalNameArray[i+1]=Shimmer3.ObjectClusterSensorName.MAG_Y;
					signalDataTypeArray[i+1] = "i16r";			
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|Configuration.Shimmer3.SensorBitmap.SENSOR_MAG);
				} 
				else {
					signalDataTypeArray[i+1] = "i16";
					packetSize=packetSize+2;
					signalNameArray[i+1]=Shimmer2.ObjectClusterSensorName.MAG_Z;
					enabledSensors= (enabledSensors|SENSOR_MAG);
				}
			}
			else if ((byte)signalid[i]==(byte)0x09){
				if(mHardwareVersion==HW_ID.SHIMMER_SR30){
					signalNameArray[i+1]=Shimmer3.ObjectClusterSensorName.ACCEL_WR_Z;
					signalDataTypeArray[i+1] = "i16";
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|Configuration.Shimmer3.SensorBitmap.SENSOR_D_ACCEL);
				} 
				else if(mHardwareVersion==HW_ID.SHIMMER_3){
					signalNameArray[i+1]=Shimmer3.ObjectClusterSensorName.MAG_Z;
					signalDataTypeArray[i+1] = "i16r";			
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|Configuration.Shimmer3.SensorBitmap.SENSOR_MAG);
				} 
				else {
					signalNameArray[i+1]=Shimmer2.ObjectClusterSensorName.ECG_RA_LL;
					signalDataTypeArray[i+1] = "u12";
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|SENSOR_ECG);
				}
			}
			else if ((byte)signalid[i]==(byte)0x0A){
				if(mHardwareVersion==HW_ID.SHIMMER_SR30){
					signalNameArray[i+1]=Shimmer3.ObjectClusterSensorName.MAG_X;
					signalDataTypeArray[i+1] = "i16";			
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|Configuration.Shimmer3.SensorBitmap.SENSOR_MAG);
				}
				else if (mHardwareVersion==HW_ID.SHIMMER_3) {
					signalNameArray[i+1]=Shimmer3.ObjectClusterSensorName.GYRO_X;
					signalDataTypeArray[i+1] = "i16r";
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|Configuration.Shimmer3.SensorBitmap.SENSOR_GYRO);
				}
				else {
					signalNameArray[i+1]=Shimmer2.ObjectClusterSensorName.ECG_LA_LL;
					signalDataTypeArray[i+1] = "u12";
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|SENSOR_ECG);
				}
			}
			else if ((byte)signalid[i]==(byte)0x0B){
				if(mHardwareVersion==HW_ID.SHIMMER_SR30){
					signalNameArray[i+1]=Shimmer3.ObjectClusterSensorName.MAG_Y;
					signalDataTypeArray[i+1] = "i16";			
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|Configuration.Shimmer3.SensorBitmap.SENSOR_MAG);
				}
				else if (mHardwareVersion==HW_ID.SHIMMER_3) {
					signalNameArray[i+1]=Shimmer3.ObjectClusterSensorName.GYRO_Y;
					signalDataTypeArray[i+1] = "i16r";
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|Configuration.Shimmer3.SensorBitmap.SENSOR_GYRO);
				}
				else {
					signalNameArray[i+1]=Shimmer2.ObjectClusterSensorName.GSR;
					signalDataTypeArray[i+1] = "u16";
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|SENSOR_GSR);
				}
			}
			else if ((byte)signalid[i]==(byte)0x0C){
				if(mHardwareVersion==HW_ID.SHIMMER_SR30){
					signalNameArray[i+1]=Shimmer3.ObjectClusterSensorName.MAG_Z;
					signalDataTypeArray[i+1] = "i16";			
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|Configuration.Shimmer3.SensorBitmap.SENSOR_MAG);
				}
				else if (mHardwareVersion==HW_ID.SHIMMER_3) {
					signalNameArray[i+1]=Shimmer3.ObjectClusterSensorName.GYRO_Z;
					signalDataTypeArray[i+1] = "i16r";
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|Configuration.Shimmer3.SensorBitmap.SENSOR_GYRO);
				}
				else {
					signalNameArray[i+1]=Shimmer2.ObjectClusterSensorName.GSR_RES;
					signalDataTypeArray[i+1] = "u16";
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|SENSOR_GSR);
				}
			}
			else if ((byte)signalid[i]==(byte)0x0D){
				if (mHardwareVersion==HW_ID.SHIMMER_3){
					signalNameArray[i+1]=Shimmer3.ObjectClusterSensorName.EXT_EXP_ADC_A7;
					signalDataTypeArray[i+1] = "u12";
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|SENSOR_EXT_ADC_A7);
				} 
				else {
					signalNameArray[i+1]=Shimmer2.ObjectClusterSensorName.EMG;
					signalDataTypeArray[i+1] = "u12";
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|SENSOR_EMG);
				}
			}
			else if ((byte)signalid[i]==(byte)0x0E){
				if (mHardwareVersion==HW_ID.SHIMMER_3){
					signalNameArray[i+1]=Shimmer3.ObjectClusterSensorName.EXT_EXP_ADC_A6;
					signalDataTypeArray[i+1] = "u12";
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|SENSOR_EXT_ADC_A6);
				} 
				else{
					signalNameArray[i+1]=Shimmer2.ObjectClusterSensorName.EXP_BOARD_A0;
					signalDataTypeArray[i+1] = "u12";
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|SENSOR_EXP_BOARD_A0);
				}
			}
			else if ((byte)signalid[i]==(byte)0x0F){
				if (mHardwareVersion==HW_ID.SHIMMER_3){
					signalNameArray[i+1]=Shimmer3.ObjectClusterSensorName.EXT_EXP_ADC_A15;
					signalDataTypeArray[i+1] = "u12";
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|SENSOR_EXT_ADC_A15);
				} else{
					signalNameArray[i+1]=Shimmer2.ObjectClusterSensorName.EXP_BOARD_A7;
					signalDataTypeArray[i+1] = "u12";
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|SENSOR_EXP_BOARD_A7);
				}
			}
			else if ((byte)signalid[i]==(byte)0x10){
				if (mHardwareVersion==HW_ID.SHIMMER_3){
					signalNameArray[i+1]=Shimmer3.ObjectClusterSensorName.INT_EXP_ADC_A1;
					signalDataTypeArray[i+1] = "u12";
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|SENSOR_INT_ADC_A1);
				} 
				else {
					signalNameArray[i+1]=Shimmer2.ObjectClusterSensorName.BRIDGE_AMP_HIGH;
					signalDataTypeArray[i+1] = "u12";
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|SENSOR_BRIDGE_AMP);
				}
			}
			else if ((byte)signalid[i]==(byte)0x11){
				if (mHardwareVersion==HW_ID.SHIMMER_3){
					signalNameArray[i+1]=Shimmer3.ObjectClusterSensorName.INT_EXP_ADC_A12;
					signalDataTypeArray[i+1] = "u12";
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|SENSOR_INT_ADC_A12);
				} 
				else {
					signalNameArray[i+1]=Shimmer2.ObjectClusterSensorName.BRIDGE_AMP_LOW;
					signalDataTypeArray[i+1] = "u12";
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|SENSOR_BRIDGE_AMP);
				}
			}
			else if ((byte)signalid[i]==(byte)0x12){
				if (mHardwareVersion==HW_ID.SHIMMER_3){
					signalNameArray[i+1]=Shimmer3.ObjectClusterSensorName.INT_EXP_ADC_A13;
					signalDataTypeArray[i+1] = "u12";
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|SENSOR_INT_ADC_A13);
				} 
				else {
					signalNameArray[i+1]=Shimmer2.ObjectClusterSensorName.HEART_RATE;
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
			else if ((byte)signalid[i]==(byte)0x13){
				if (mHardwareVersion==HW_ID.SHIMMER_3){
					signalNameArray[i+1]=Shimmer3.ObjectClusterSensorName.INT_EXP_ADC_A14;
					signalDataTypeArray[i+1] = "u12";
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|SENSOR_INT_ADC_A14);
				}
			}
			else if ((byte)signalid[i]==(byte)0x1A){
				if (mHardwareVersion==HW_ID.SHIMMER_3){
					signalNameArray[i+1]=Shimmer3.ObjectClusterSensorName.TEMPERATURE_BMP180;
					signalDataTypeArray[i+1] = "u16r";
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|SENSOR_BMP180);
				}
			}
			else if ((byte)signalid[i]==(byte)0x1B){
				if (mHardwareVersion==HW_ID.SHIMMER_3){
					signalNameArray[i+1]=Shimmer3.ObjectClusterSensorName.PRESSURE_BMP180;
					signalDataTypeArray[i+1] = "u24r";
					packetSize=packetSize+3;
					enabledSensors= (enabledSensors|SENSOR_BMP180);
				}
			}
			else if ((byte)signalid[i]==(byte)0x1C){
				if (mHardwareVersion==HW_ID.SHIMMER_3){
					signalNameArray[i+1]=Shimmer3.ObjectClusterSensorName.GSR;
					signalDataTypeArray[i+1] = "u16";
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|SENSOR_GSR);
				}
			}
			else if ((byte)signalid[i]==(byte)0x1D){
				if (mHardwareVersion==HW_ID.SHIMMER_3){
					signalNameArray[i+1]=Shimmer3.ObjectClusterSensorName.EXG1_STATUS;
					signalDataTypeArray[i+1] = "u8";
					packetSize=packetSize+1;
				}
			}
			else if ((byte)signalid[i]==(byte)0x1E){
				if (mHardwareVersion==HW_ID.SHIMMER_3){
					signalNameArray[i+1]=Shimmer3.ObjectClusterSensorName.EXG1_CH1_24BIT;
					signalDataTypeArray[i+1] = "i24r";
					packetSize=packetSize+3;
					enabledSensors= (enabledSensors|SENSOR_EXG1_24BIT);
				}
			}
			else if ((byte)signalid[i]==(byte)0x1F){
				if (mHardwareVersion==HW_ID.SHIMMER_3){
					signalNameArray[i+1]=Shimmer3.ObjectClusterSensorName.EXG1_CH2_24BIT;
					signalDataTypeArray[i+1] = "i24r";
					packetSize=packetSize+3;
					enabledSensors= (enabledSensors|SENSOR_EXG1_24BIT);
				}
			}
			else if ((byte)signalid[i]==(byte)0x20){
				if (mHardwareVersion==HW_ID.SHIMMER_3){
					signalNameArray[i+1]=Shimmer3.ObjectClusterSensorName.EXG2_STATUS;
					signalDataTypeArray[i+1] = "u8";
					packetSize=packetSize+1;
				}
			}
			else if ((byte)signalid[i]==(byte)0x21){
				if (mHardwareVersion==HW_ID.SHIMMER_3){
					signalNameArray[i+1]=Shimmer3.ObjectClusterSensorName.EXG2_CH1_24BIT;
					signalDataTypeArray[i+1] = "i24r";
					packetSize=packetSize+3;
					enabledSensors= (enabledSensors|SENSOR_EXG2_24BIT);
				}
			}
			else if ((byte)signalid[i]==(byte)0x22){
				if (mHardwareVersion==HW_ID.SHIMMER_3){
					signalNameArray[i+1]=Shimmer3.ObjectClusterSensorName.EXG2_CH2_24BIT;
					signalDataTypeArray[i+1] = "i24r";
					packetSize=packetSize+3;
					enabledSensors= (enabledSensors|SENSOR_EXG2_24BIT);
				}
			}
			else if ((byte)signalid[i]==(byte)0x23){
				if (mHardwareVersion==HW_ID.SHIMMER_3){
					signalNameArray[i+1]=Shimmer3.ObjectClusterSensorName.EXG1_CH1_16BIT;
					signalDataTypeArray[i+1] = "i16r";
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|SENSOR_EXG1_16BIT);
				}
			}
			else if ((byte)signalid[i]==(byte)0x24){
				if (mHardwareVersion==HW_ID.SHIMMER_3){
					signalNameArray[i+1]=Shimmer3.ObjectClusterSensorName.EXG1_CH2_16BIT;
					signalDataTypeArray[i+1] = "i16r";
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|SENSOR_EXG1_16BIT);
				}
			}
			else if ((byte)signalid[i]==(byte)0x25){
				if (mHardwareVersion==HW_ID.SHIMMER_3){
					signalNameArray[i+1]=Shimmer3.ObjectClusterSensorName.EXG2_CH1_16BIT;
					signalDataTypeArray[i+1] = "i16r";
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|SENSOR_EXG2_16BIT);
				}
			}
			else if ((byte)signalid[i]==(byte)0x26){
				if (mHardwareVersion==HW_ID.SHIMMER_3){
					signalNameArray[i+1]=Shimmer3.ObjectClusterSensorName.EXG2_CH2_16BIT;
					signalDataTypeArray[i+1] = "i16r";
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|SENSOR_EXG2_16BIT);
				}
			}
			else if ((byte)signalid[i]==(byte)0x27){
				if (mHardwareVersion==HW_ID.SHIMMER_3){
					signalNameArray[i+1]=Shimmer3.ObjectClusterSensorName.BRIDGE_AMP_HIGH;
					signalDataTypeArray[i+1] = "u12";
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|SENSOR_BRIDGE_AMP);
				} 
			}
			else if ((byte)signalid[i]==(byte)0x28){
				if (mHardwareVersion==HW_ID.SHIMMER_3){
					signalNameArray[i+1]=Shimmer3.ObjectClusterSensorName.BRIDGE_AMP_LOW;
					signalDataTypeArray[i+1] = "u12";
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|SENSOR_BRIDGE_AMP);
				} 
			}
			else{
				signalNameArray[i+1]=Byte.toString(signalid[i]);
				signalDataTypeArray[i+1] = "u12";
				packetSize=packetSize+2;
			}
		}
		
//		int offset = nC + 2;
//		signalNameArray[offset]=Shimmer3.ObjectClusterSensorName.BATT_PERCENTAGE;
//		signalNameArray[offset+1]=Shimmer3.ObjectClusterSensorName.PACKET_RECEPTION_RATE_CURRENT;
//		signalNameArray[offset+2]=Shimmer3.ObjectClusterSensorName.PACKET_RECEPTION_RATE_TRIAL;
//		signalNameArray[offset+3]=Shimmer3.ObjectClusterSensorName.ECG_LL_LA_16BIT;
		
		mSignalNameArray=signalNameArray;
		mSignalDataTypeArray=signalDataTypeArray;
		mPacketSize=packetSize;
	}

	protected void retrievePressureCalibrationParametersFromPacket(byte[] pressureResoRes, int packetType) {
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
	@Deprecated
	public static BiMap<String,String> generateBiMapSensorIDtoSensorName(int shimmerVersion){
		BiMap<String, String> sensorBitmaptoName =null;  
		if (shimmerVersion != HW_ID.SHIMMER_2R){
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
	@Deprecated
	public void generateBiMapSensorIDtoSensorName(){
		if (mHardwareVersion != -1){
			if (mHardwareVersion != HW_ID.SHIMMER_2R){
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


	//TODO: update with sensorMap entries
	public String[] getListofEnabledSensorSignals(){
		List<String> listofSignals = new ArrayList<String>();
		String[] enabledSignals; 
		if (mHardwareVersion==HW_ID.SHIMMER_2 || mHardwareVersion==HW_ID.SHIMMER_2R){
			listofSignals.add("Timestamp");
			if (((mEnabledSensors & 0xFF)& SENSOR_ACCEL) > 0){
				listofSignals.add(Shimmer2.ObjectClusterSensorName.ACCEL_X);
				listofSignals.add(Shimmer2.ObjectClusterSensorName.ACCEL_Y);
				listofSignals.add(Shimmer2.ObjectClusterSensorName.ACCEL_Z);
			}
			if (((mEnabledSensors & 0xFF)& SENSOR_GYRO) > 0) {
				listofSignals.add(Shimmer2.ObjectClusterSensorName.GYRO_X);
				listofSignals.add(Shimmer2.ObjectClusterSensorName.GYRO_Y);
				listofSignals.add(Shimmer2.ObjectClusterSensorName.GYRO_Z);
			}
			if (((mEnabledSensors & 0xFF)& SENSOR_MAG) > 0) {
				listofSignals.add(Shimmer2.ObjectClusterSensorName.MAG_X);
				listofSignals.add(Shimmer2.ObjectClusterSensorName.MAG_Y);
				listofSignals.add(Shimmer2.ObjectClusterSensorName.MAG_Z);
			}
			if (((mEnabledSensors & 0xFF) & SENSOR_GSR) > 0) {
				listofSignals.add(Shimmer2.ObjectClusterSensorName.GSR);
				//listofSignals.add(Shimmer2.ObjectClusterSensorName.GSR_RES);
			}
			if (((mEnabledSensors & 0xFF) & SENSOR_ECG) > 0) {
				listofSignals.add(Shimmer2.ObjectClusterSensorName.ECG_RA_LL);
				listofSignals.add(Shimmer2.ObjectClusterSensorName.ECG_LA_LL);
			}
			if (((mEnabledSensors & 0xFF) & SENSOR_EMG) > 0) {
				listofSignals.add(Shimmer2.ObjectClusterSensorName.EMG);
			}
			if (((mEnabledSensors & 0xFF00) & SENSOR_BRIDGE_AMP) > 0) {
				listofSignals.add(Shimmer2.ObjectClusterSensorName.BRIDGE_AMP_HIGH);
				listofSignals.add(Shimmer2.ObjectClusterSensorName.BRIDGE_AMP_LOW);
			}
			if (((mEnabledSensors & 0xFF00) & SENSOR_HEART) > 0) {
				listofSignals.add(Shimmer2.ObjectClusterSensorName.HEART_RATE);
			}
			if ((((mEnabledSensors & 0xFF) & SENSOR_EXP_BOARD_A0) > 0) && getPMux() == 0) {
				listofSignals.add(Shimmer2.ObjectClusterSensorName.EXP_BOARD_A0);
			}
			if ((((mEnabledSensors & 0xFF) & SENSOR_EXP_BOARD_A7) > 0 && getPMux() == 0)) {
				listofSignals.add(Shimmer2.ObjectClusterSensorName.EXP_BOARD_A7);
			}
			if (((mEnabledSensors & 0xFFFF) & SENSOR_BATT) > 0) {
				listofSignals.add(Shimmer2.ObjectClusterSensorName.BATTERY);
				listofSignals.add(Shimmer2.ObjectClusterSensorName.REG);
			}
			if (((mEnabledSensors & 0xFF)& SENSOR_ACCEL) > 0 && ((mEnabledSensors & 0xFF)& SENSOR_GYRO) > 0 && ((mEnabledSensors & 0xFF)& SENSOR_MAG) > 0 && mOrientationEnabled){
				listofSignals.add(Shimmer2.ObjectClusterSensorName.AXIS_ANGLE_A);
				listofSignals.add(Shimmer2.ObjectClusterSensorName.AXIS_ANGLE_X);
				listofSignals.add(Shimmer2.ObjectClusterSensorName.AXIS_ANGLE_Y);
				listofSignals.add(Shimmer2.ObjectClusterSensorName.AXIS_ANGLE_Z);
			}
			if (((mEnabledSensors & 0xFF)& SENSOR_ACCEL) > 0 && ((mEnabledSensors & 0xFF)& SENSOR_GYRO) > 0 && ((mEnabledSensors & 0xFF)& SENSOR_MAG) > 0 && mOrientationEnabled){
				listofSignals.add(Shimmer2.ObjectClusterSensorName.QUAT_MADGE_9DOF_W);
				listofSignals.add(Shimmer2.ObjectClusterSensorName.QUAT_MADGE_9DOF_X);
				listofSignals.add(Shimmer2.ObjectClusterSensorName.QUAT_MADGE_9DOF_Y);
				listofSignals.add(Shimmer2.ObjectClusterSensorName.QUAT_MADGE_9DOF_Z);
			}

		} else {
			listofSignals.add("Timestamp");
			if ((mEnabledSensors & SENSOR_ACCEL) >0){
				listofSignals.add(Shimmer3.ObjectClusterSensorName.ACCEL_LN_X);
				listofSignals.add(Shimmer3.ObjectClusterSensorName.ACCEL_LN_Y);
				listofSignals.add(Shimmer3.ObjectClusterSensorName.ACCEL_LN_Z);
			}
			if ((mEnabledSensors& SENSOR_DACCEL) >0){
				listofSignals.add(Shimmer3.ObjectClusterSensorName.ACCEL_WR_X);
				listofSignals.add(Shimmer3.ObjectClusterSensorName.ACCEL_WR_Y);
				listofSignals.add(Shimmer3.ObjectClusterSensorName.ACCEL_WR_Z);
			}
			if (((mEnabledSensors & 0xFF)& SENSOR_GYRO) > 0) {
				listofSignals.add(Shimmer3.ObjectClusterSensorName.GYRO_X);
				listofSignals.add(Shimmer3.ObjectClusterSensorName.GYRO_Y);
				listofSignals.add(Shimmer3.ObjectClusterSensorName.GYRO_Z);
			}
			if (((mEnabledSensors & 0xFF)& SENSOR_MAG) > 0) {
				listofSignals.add(Shimmer3.ObjectClusterSensorName.MAG_X);
				listofSignals.add(Shimmer3.ObjectClusterSensorName.MAG_Y);
				listofSignals.add(Shimmer3.ObjectClusterSensorName.MAG_Z);
			} 
			if (((mEnabledSensors & 0xFFFF) & SENSOR_BATT) > 0) {
				listofSignals.add(Shimmer3.ObjectClusterSensorName.BATTERY);
			}
			if (((mEnabledSensors & 0xFFFFFF)& SENSOR_EXT_ADC_A15) > 0) {
				listofSignals.add(Shimmer3.ObjectClusterSensorName.EXT_EXP_ADC_A15);
			}
			if (((mEnabledSensors & 0xFFFFFF)& SENSOR_EXT_ADC_A7) > 0) {
				listofSignals.add(Shimmer3.ObjectClusterSensorName.EXT_EXP_ADC_A7);
			}
			if (((mEnabledSensors & 0xFFFFFF)& SENSOR_EXT_ADC_A6) > 0) {
				listofSignals.add(Shimmer3.ObjectClusterSensorName.EXT_EXP_ADC_A6);
			}
			if (((mEnabledSensors & 0xFFFFFF)& SENSOR_INT_ADC_A1) > 0) {
				listofSignals.add(Shimmer3.ObjectClusterSensorName.INT_EXP_ADC_A1);
			}
			if (((mEnabledSensors & 0xFFFFFF)& SENSOR_INT_ADC_A12) > 0) {
				listofSignals.add(Shimmer3.ObjectClusterSensorName.INT_EXP_ADC_A12);
			}
			if (((mEnabledSensors & 0xFFFFFF)& SENSOR_INT_ADC_A13) > 0) {
				listofSignals.add(Shimmer3.ObjectClusterSensorName.INT_EXP_ADC_A13);
			}
			if (((mEnabledSensors & 0xFFFFFF)& SENSOR_INT_ADC_A14) > 0) {
				listofSignals.add(Shimmer3.ObjectClusterSensorName.INT_EXP_ADC_A14);
			}
			if ((((mEnabledSensors & 0xFF)& SENSOR_ACCEL) > 0 || ((mEnabledSensors & 0xFFFF)& SENSOR_DACCEL) > 0)&& ((mEnabledSensors & 0xFF)& SENSOR_GYRO) > 0 && ((mEnabledSensors & 0xFF)& SENSOR_MAG) > 0 && mOrientationEnabled){
				listofSignals.add(Shimmer3.ObjectClusterSensorName.AXIS_ANGLE_A);
				listofSignals.add(Shimmer3.ObjectClusterSensorName.AXIS_ANGLE_X);
				listofSignals.add(Shimmer3.ObjectClusterSensorName.AXIS_ANGLE_Y);
				listofSignals.add(Shimmer3.ObjectClusterSensorName.AXIS_ANGLE_Z);
			}
			if ((((mEnabledSensors & 0xFF)& SENSOR_ACCEL) > 0 || ((mEnabledSensors & 0xFFFF)& SENSOR_DACCEL) > 0) && ((mEnabledSensors & 0xFF)& SENSOR_GYRO) > 0 && ((mEnabledSensors & 0xFF)& SENSOR_MAG) > 0 && mOrientationEnabled){
				listofSignals.add(Shimmer3.ObjectClusterSensorName.QUAT_MADGE_9DOF_W);
				listofSignals.add(Shimmer3.ObjectClusterSensorName.QUAT_MADGE_9DOF_X);
				listofSignals.add(Shimmer3.ObjectClusterSensorName.QUAT_MADGE_9DOF_Y);
				listofSignals.add(Shimmer3.ObjectClusterSensorName.QUAT_MADGE_9DOF_Z);
			}
			if ((mEnabledSensors & SENSOR_BMP180)>0){
				listofSignals.add(Shimmer3.ObjectClusterSensorName.PRESSURE_BMP180);
				listofSignals.add(Shimmer3.ObjectClusterSensorName.TEMPERATURE_BMP180);
			}
			if ((mEnabledSensors & SENSOR_GSR)>0){
				listofSignals.add(Shimmer3.ObjectClusterSensorName.GSR);
			}
			if (((mEnabledSensors & SENSOR_EXG1_24BIT)>0)|| ((mEnabledSensors & SENSOR_EXG1_16BIT)>0)){
				listofSignals.add(Shimmer3.ObjectClusterSensorName.EXG1_STATUS);
			}
			if (((mEnabledSensors & SENSOR_EXG2_24BIT)>0)|| ((mEnabledSensors & SENSOR_EXG2_16BIT)>0)){
				listofSignals.add(Shimmer3.ObjectClusterSensorName.EXG2_STATUS);
			}
			if ((mEnabledSensors & SENSOR_EXG1_24BIT)>0){
				if (isEXGUsingDefaultECGConfiguration()){
					listofSignals.add(Shimmer3.ObjectClusterSensorName.ECG_LL_RA_24BIT);
					listofSignals.add(Shimmer3.ObjectClusterSensorName.ECG_LA_RA_24BIT);
				}
				else if (isEXGUsingDefaultEMGConfiguration()){
					listofSignals.add(Shimmer3.ObjectClusterSensorName.EMG_CH1_24BIT);
					listofSignals.add(Shimmer3.ObjectClusterSensorName.EMG_CH2_24BIT);
				} else {
					listofSignals.add(Shimmer3.ObjectClusterSensorName.EXG1_CH1_24BIT);
					listofSignals.add(Shimmer3.ObjectClusterSensorName.EXG1_CH2_24BIT);
				}

			}
			if ((mEnabledSensors & SENSOR_EXG2_24BIT)>0){
				if (isEXGUsingDefaultECGConfiguration()){
					listofSignals.add(Shimmer3.ObjectClusterSensorName.EXG2_CH1_24BIT);
					listofSignals.add(Shimmer3.ObjectClusterSensorName.ECG_VX_RL_24BIT);
				}
				else if (isEXGUsingDefaultEMGConfiguration()){
					listofSignals.add(Shimmer3.ObjectClusterSensorName.EXG2_CH1_24BIT);
					listofSignals.add(Shimmer3.ObjectClusterSensorName.EXG2_CH2_24BIT);
				} else {
					listofSignals.add(Shimmer3.ObjectClusterSensorName.EXG2_CH1_24BIT);
					listofSignals.add(Shimmer3.ObjectClusterSensorName.EXG2_CH2_24BIT);
				}
			}
			if ((mEnabledSensors & SENSOR_EXG1_16BIT)>0){
				if (isEXGUsingDefaultECGConfiguration()){
					listofSignals.add(Shimmer3.ObjectClusterSensorName.ECG_LL_RA_16BIT);
					listofSignals.add(Shimmer3.ObjectClusterSensorName.ECG_LA_RA_16BIT);
				}
				else if (isEXGUsingDefaultEMGConfiguration()){
					listofSignals.add(Shimmer3.ObjectClusterSensorName.EMG_CH1_16BIT);
					listofSignals.add(Shimmer3.ObjectClusterSensorName.EMG_CH2_16BIT);
				} else {
					listofSignals.add(Shimmer3.ObjectClusterSensorName.EXG1_CH1_16BIT);
					listofSignals.add(Shimmer3.ObjectClusterSensorName.EXG1_CH2_16BIT);
				}
			}
			if ((mEnabledSensors & SENSOR_EXG2_16BIT)>0){
				if (isEXGUsingDefaultECGConfiguration()){
					listofSignals.add(Shimmer3.ObjectClusterSensorName.EXG2_CH1_16BIT);
					listofSignals.add(Shimmer3.ObjectClusterSensorName.ECG_VX_RL_16BIT);
				}
				else if (isEXGUsingDefaultEMGConfiguration()){
					listofSignals.add(Shimmer3.ObjectClusterSensorName.EXG2_CH1_16BIT);
					listofSignals.add(Shimmer3.ObjectClusterSensorName.EXG2_CH2_16BIT);
				} else {
					listofSignals.add(Shimmer3.ObjectClusterSensorName.EXG2_CH1_16BIT);
					listofSignals.add(Shimmer3.ObjectClusterSensorName.EXG2_CH2_16BIT);
				}
			}
			if (((mEnabledSensors & 0xFF00) & SENSOR_BRIDGE_AMP) > 0) {
				listofSignals.add(Shimmer3.ObjectClusterSensorName.BRIDGE_AMP_HIGH);
				listofSignals.add(Shimmer3.ObjectClusterSensorName.BRIDGE_AMP_LOW);
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

	public void retrieveKinematicCalibrationParametersFromPacket(byte[] bufferCalibrationParameters, int packetType) {
		
		if (packetType==ACCEL_CALIBRATION_RESPONSE || packetType==LSM303DLHC_ACCEL_CALIBRATION_RESPONSE || packetType==GYRO_CALIBRATION_RESPONSE || packetType==MAG_CALIBRATION_RESPONSE ){
			String[] dataType={"i16","i16","i16","i16","i16","i16","i8","i8","i8","i8","i8","i8","i8","i8","i8"}; 
			int[] formattedPacket=formatDataPacketReverse(bufferCalibrationParameters,dataType); // using the datatype the calibration parameters are converted
			double[] AM=new double[9];
			for (int i=0;i<9;i++){
				AM[i]=((double)formattedPacket[6+i])/100;
			}

			double[][] AlignmentMatrix = {{AM[0],AM[1],AM[2]},{AM[3],AM[4],AM[5]},{AM[6],AM[7],AM[8]}}; 				
			double[][] SensitivityMatrix = {{formattedPacket[3],0,0},{0,formattedPacket[4],0},{0,0,formattedPacket[5]}}; 
			double[][] OffsetVector = {{formattedPacket[0]},{formattedPacket[1]},{formattedPacket[2]}};

			if(packetType==ACCEL_CALIBRATION_RESPONSE && checkIfDefaultAccelCal(OffsetVector, SensitivityMatrix, AlignmentMatrix)){
				mDefaultCalibrationParametersAccel = true;
				mAlignmentMatrixAnalogAccel = AlignmentMatrix;
				mOffsetVectorAnalogAccel = OffsetVector;
				mSensitivityMatrixAnalogAccel = SensitivityMatrix;
			}
			else if (packetType==ACCEL_CALIBRATION_RESPONSE && SensitivityMatrix[0][0]!=-1) {   //used to be 65535 but changed to -1 as we are now using i16
				mDefaultCalibrationParametersAccel = false;
				mAlignmentMatrixAnalogAccel = AlignmentMatrix;
				mOffsetVectorAnalogAccel = OffsetVector;
				mSensitivityMatrixAnalogAccel = SensitivityMatrix;
			} 
			else if(packetType==ACCEL_CALIBRATION_RESPONSE && SensitivityMatrix[0][0]==-1){
				if (mHardwareVersion!=3){
					mDefaultCalibrationParametersAccel = true;
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
				} 
				else {
					setDefaultCalibrationShimmer3LowNoiseAccel();
				}
			}

			if(packetType==LSM303DLHC_ACCEL_CALIBRATION_RESPONSE && checkIfDefaultWideRangeAccelCal(OffsetVector, SensitivityMatrix, AlignmentMatrix)){
				mDefaultCalibrationParametersDigitalAccel = true;
			}
			else if (packetType==LSM303DLHC_ACCEL_CALIBRATION_RESPONSE && SensitivityMatrix[0][0]!=-1) {   //used to be 65535 but changed to -1 as we are now using i16
				mDefaultCalibrationParametersDigitalAccel = false;
				mAlignmentMatrixWRAccel = AlignmentMatrix;
				mOffsetVectorWRAccel = OffsetVector;
				mSensitivityMatrixWRAccel = SensitivityMatrix;
			}
			else if(packetType==LSM303DLHC_ACCEL_CALIBRATION_RESPONSE  && SensitivityMatrix[0][0]==-1){
				setDefaultCalibrationShimmer3WideRangeAccel();
			}
			
			if(packetType==GYRO_CALIBRATION_RESPONSE && checkIfDefaulGyroCal(OffsetVector, SensitivityMatrix, AlignmentMatrix)){
				mDefaultCalibrationParametersGyro = true;
				mAlignmentMatrixGyroscope = AlignmentMatrix;
				mOffsetVectorGyroscope = OffsetVector;
				mSensitivityMatrixGyroscope = SensitivityMatrix;
				mSensitivityMatrixGyroscope[0][0] = mSensitivityMatrixGyroscope[0][0]/100;
				mSensitivityMatrixGyroscope[1][1] = mSensitivityMatrixGyroscope[1][1]/100;
				mSensitivityMatrixGyroscope[2][2] = mSensitivityMatrixGyroscope[2][2]/100;
			}
			else if (packetType==GYRO_CALIBRATION_RESPONSE && SensitivityMatrix[0][0]!=-1) {
				mDefaultCalibrationParametersGyro = false;
				mAlignmentMatrixGyroscope = AlignmentMatrix;
				mOffsetVectorGyroscope = OffsetVector;
				mSensitivityMatrixGyroscope = SensitivityMatrix;
				mSensitivityMatrixGyroscope[0][0] = mSensitivityMatrixGyroscope[0][0]/100;
				mSensitivityMatrixGyroscope[1][1] = mSensitivityMatrixGyroscope[1][1]/100;
				mSensitivityMatrixGyroscope[2][2] = mSensitivityMatrixGyroscope[2][2]/100;
			} 
			else if(packetType==GYRO_CALIBRATION_RESPONSE && SensitivityMatrix[0][0]==-1){
				if(mHardwareVersion!=3){
					mDefaultCalibrationParametersGyro = true;
					mAlignmentMatrixGyroscope = AlignmentMatrixGyroShimmer2;
					mOffsetVectorGyroscope = OffsetVectorGyroShimmer2;
					mSensitivityMatrixGyroscope = SensitivityMatrixGyroShimmer2;	
				} 
				else {
					setDefaultCalibrationShimmer3Gyro();
				}
			} 
			
			if(packetType==MAG_CALIBRATION_RESPONSE && checkIfDefaulMagCal(OffsetVector, SensitivityMatrix, AlignmentMatrix)){
				mDefaultCalibrationParametersMag = true;
				mAlignmentMatrixMagnetometer = AlignmentMatrix;
				mOffsetVectorMagnetometer = OffsetVector;
				mSensitivityMatrixMagnetometer = SensitivityMatrix;
			}
			else if (packetType==MAG_CALIBRATION_RESPONSE && SensitivityMatrix[0][0]!=-1) {
				mDefaultCalibrationParametersMag = false;
				mAlignmentMatrixMagnetometer = AlignmentMatrix;
				mOffsetVectorMagnetometer = OffsetVector;
				mSensitivityMatrixMagnetometer = SensitivityMatrix;
			}
			else if(packetType==MAG_CALIBRATION_RESPONSE && SensitivityMatrix[0][0]==-1){
				mDefaultCalibrationParametersMag = true;
				if (mHardwareVersion!=3){
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
					setDefaultCalibrationShimmer3Mag();
				}
			}
		}
	}

	private boolean checkIfDefaultAccelCal(double[][] offsetVectorToTest, double[][] sensitivityMatrixToTest, double[][] alignmentMatrixToTest) {
		
		double[][] offsetVectorToCompare = OffsetVectorLowNoiseAccelShimmer3;
		double[][] sensitivityVectorToCompare = SensitivityMatrixLowNoiseAccel2gShimmer3;
		double[][] alignmentVectorToCompare = AlignmentMatrixLowNoiseAccelShimmer3;
		
		if (mHardwareVersion!=HW_ID.SHIMMER_3){
			alignmentVectorToCompare = AlignmentMatrixAccelShimmer2;
			offsetVectorToCompare = OffsetVectorAccelShimmer2;
			if (getAccelRange()==0){
				sensitivityVectorToCompare = SensitivityMatrixAccel1p5gShimmer2; 
			} else if (getAccelRange()==1){
				sensitivityVectorToCompare = SensitivityMatrixAccel2gShimmer2; 
			} else if (getAccelRange()==2){
				sensitivityVectorToCompare = SensitivityMatrixAccel4gShimmer2; 
			} else if (getAccelRange()==3){
				sensitivityVectorToCompare = SensitivityMatrixAccel6gShimmer2; 
			}
		} 
		
		boolean alignmentPass = Arrays.deepEquals(alignmentVectorToCompare, alignmentMatrixToTest);
		boolean offsetPass = Arrays.deepEquals(offsetVectorToCompare, offsetVectorToTest);
		boolean sensitivityPass = Arrays.deepEquals(sensitivityVectorToCompare, sensitivityMatrixToTest);
		
		if(alignmentPass&&offsetPass&&sensitivityPass){
			return true;
		}
		return false;
	}

	private boolean checkIfDefaultWideRangeAccelCal(double[][] offsetVectorToTest, double[][] sensitivityMatrixToTest, double[][] alignmentMatrixToTest) {
		
		double[][] offsetVectorToCompare = OffsetVectorWideRangeAccelShimmer3;
		double[][] sensitivityVectorToCompare = SensitivityMatrixWideRangeAccel2gShimmer3;
		double[][] alignmentVectorToCompare = AlignmentMatrixWideRangeAccelShimmer3;
		
		if (getAccelRange()==0){
			sensitivityVectorToCompare = SensitivityMatrixWideRangeAccel2gShimmer3;
			alignmentVectorToCompare = AlignmentMatrixWideRangeAccelShimmer3;
			offsetVectorToCompare = OffsetVectorWideRangeAccelShimmer3;
		} else if (getAccelRange()==1){
			sensitivityVectorToCompare = SensitivityMatrixWideRangeAccel4gShimmer3;
			alignmentVectorToCompare = AlignmentMatrixWideRangeAccelShimmer3;
			offsetVectorToCompare = OffsetVectorWideRangeAccelShimmer3;
		} else if (getAccelRange()==2){
			sensitivityVectorToCompare = SensitivityMatrixWideRangeAccel8gShimmer3;
			alignmentVectorToCompare = AlignmentMatrixWideRangeAccelShimmer3;
			offsetVectorToCompare = OffsetVectorWideRangeAccelShimmer3;
		} else if (getAccelRange()==3){
			sensitivityVectorToCompare = SensitivityMatrixWideRangeAccel16gShimmer3;
			alignmentVectorToCompare = AlignmentMatrixWideRangeAccelShimmer3;
			offsetVectorToCompare = OffsetVectorWideRangeAccelShimmer3;
		}
		
		boolean alignmentPass = Arrays.deepEquals(alignmentVectorToCompare, alignmentMatrixToTest);
		boolean offsetPass = Arrays.deepEquals(offsetVectorToCompare, offsetVectorToTest);
		boolean sensitivityPass = Arrays.deepEquals(sensitivityVectorToCompare, sensitivityMatrixToTest);
		
		if(alignmentPass&&offsetPass&&sensitivityPass){
			return true;
		}
		return false;
	}

	private boolean checkIfDefaulGyroCal(double[][] offsetVectorToTest, double[][] sensitivityMatrixToTest, double[][] alignmentMatrixToTest) {
		
		double[][] offsetVectorToCompare = OffsetVectorGyroShimmer2;
		double[][] sensitivityVectorToCompare = SensitivityMatrixGyroShimmer2;
		double[][] alignmentVectorToCompare = AlignmentMatrixGyroShimmer2;
		
		if(mHardwareVersion==HW_ID.SHIMMER_3){
			if (mGyroRange==0){
				sensitivityVectorToCompare = SensitivityMatrixGyro250dpsShimmer3;

			} else if (mGyroRange==1){
				sensitivityVectorToCompare = SensitivityMatrixGyro500dpsShimmer3;

			} else if (mGyroRange==2){
				sensitivityVectorToCompare = SensitivityMatrixGyro1000dpsShimmer3;

			} else if (mGyroRange==3){
				sensitivityVectorToCompare = SensitivityMatrixGyro2000dpsShimmer3;
			}
			alignmentVectorToCompare = AlignmentMatrixGyroShimmer3;
			offsetVectorToCompare = OffsetVectorGyroShimmer3;
		}
		
		sensitivityVectorToCompare[0][0] = sensitivityVectorToCompare[0][0]*100;
		sensitivityVectorToCompare[1][1] = sensitivityVectorToCompare[1][1]*100;
		sensitivityVectorToCompare[2][2] = sensitivityVectorToCompare[2][2]*100;
		
		boolean alignmentPass = Arrays.deepEquals(alignmentVectorToCompare, alignmentMatrixToTest);
		boolean offsetPass = Arrays.deepEquals(offsetVectorToCompare, offsetVectorToTest);
		boolean sensitivityPass = Arrays.deepEquals(sensitivityVectorToCompare, sensitivityMatrixToTest);
		
		if(alignmentPass&&offsetPass&&sensitivityPass){
			return true;
		}
		return false;
	}
	
	private boolean checkIfDefaulMagCal(double[][] offsetVectorToTest, double[][] sensitivityMatrixToTest, double[][] alignmentMatrixToTest) {
		
		double[][] offsetVectorToCompare = new double[][]{};
		double[][] sensitivityVectorToCompare = new double[][]{};
		double[][] alignmentVectorToCompare = new double[][]{};
		
		if(mHardwareVersion==HW_ID.SHIMMER_3){
			alignmentVectorToCompare = AlignmentMatrixMagShimmer3;
			offsetVectorToCompare = OffsetVectorMagShimmer3;
			if (mMagRange==1){
				sensitivityVectorToCompare = SensitivityMatrixMag1p3GaShimmer3;
			} else if (mMagRange==2){
				sensitivityVectorToCompare = SensitivityMatrixMag1p9GaShimmer3;
			} else if (mMagRange==3){
				sensitivityVectorToCompare = SensitivityMatrixMag2p5GaShimmer3;
			} else if (mMagRange==4){
				sensitivityVectorToCompare = SensitivityMatrixMag4GaShimmer3;
			} else if (mMagRange==5){
				sensitivityVectorToCompare = SensitivityMatrixMag4p7GaShimmer3;
			} else if (mMagRange==6){
				sensitivityVectorToCompare = SensitivityMatrixMag5p6GaShimmer3;
			} else if (mMagRange==7){
				sensitivityVectorToCompare = SensitivityMatrixMag8p1GaShimmer3;
			}
		}
		else{
			alignmentVectorToCompare = AlignmentMatrixMagShimmer2;
			offsetVectorToCompare = OffsetVectorMagShimmer2;
			if (mMagRange==0){
				sensitivityVectorToCompare = SensitivityMatrixMag0p8GaShimmer2;
			} else if (mMagRange==1){
				sensitivityVectorToCompare = SensitivityMatrixMag1p3GaShimmer2;
			} else if (mMagRange==2){
				sensitivityVectorToCompare = SensitivityMatrixMag1p9GaShimmer2;
			} else if (mMagRange==3){
				sensitivityVectorToCompare = SensitivityMatrixMag2p5GaShimmer2;
			} else if (mMagRange==4){
				sensitivityVectorToCompare = SensitivityMatrixMag4p0GaShimmer2;
			} else if (mMagRange==5){
				sensitivityVectorToCompare = SensitivityMatrixMag4p7GaShimmer2;
			} else if (mMagRange==6){
				sensitivityVectorToCompare = SensitivityMatrixMag5p6GaShimmer2;
			} else if (mMagRange==7){
				sensitivityVectorToCompare = SensitivityMatrixMag8p1GaShimmer2;
			}			
		}
		
		boolean alignmentPass = Arrays.deepEquals(alignmentVectorToCompare, alignmentMatrixToTest);
		boolean offsetPass = Arrays.deepEquals(offsetVectorToCompare, offsetVectorToTest);
		boolean sensitivityPass = Arrays.deepEquals(sensitivityVectorToCompare, sensitivityMatrixToTest);
		
		if(alignmentPass&&offsetPass&&sensitivityPass){
			return true;
		}
		return false;
	}
	

	
	protected void setDefaultCalibrationShimmer3StandardImus(){
		setDefaultCalibrationShimmer3LowNoiseAccel();
		setDefaultCalibrationShimmer3WideRangeAccel();
		setDefaultCalibrationShimmer3Gyro();
		setDefaultCalibrationShimmer3Mag();
	}

	private void setDefaultCalibrationShimmer3LowNoiseAccel() {
		mDefaultCalibrationParametersAccel = true;
		mSensitivityMatrixAnalogAccel = SensitivityMatrixLowNoiseAccel2gShimmer3;
		mAlignmentMatrixAnalogAccel = AlignmentMatrixLowNoiseAccelShimmer3;
		mOffsetVectorAnalogAccel = OffsetVectorLowNoiseAccelShimmer3;
	}
	
	private void setDefaultCalibrationShimmer3WideRangeAccel() {
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

	private void setDefaultCalibrationShimmer3Gyro() {
		mDefaultCalibrationParametersGyro = true;
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
	
	private void setDefaultCalibrationShimmer3Mag() {
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
		if (mLastReceivedTimeStamp>(timeStamp+(mTimeStampPacketRawMaxValue*mCurrentTimeStampCycle))){ 
			mCurrentTimeStampCycle=mCurrentTimeStampCycle+1;
		}

		mLastReceivedTimeStamp=(timeStamp+(mTimeStampPacketRawMaxValue*mCurrentTimeStampCycle));
		calibratedTimeStamp=mLastReceivedTimeStamp/32768*1000;   // to convert into mS
		if (mFirstTimeCalTime){
			mFirstTimeCalTime=false;
			mCalTimeStart = calibratedTimeStamp;
		}
		if (mLastReceivedCalibratedTimeStamp!=-1){
			double timeDifference=calibratedTimeStamp-mLastReceivedCalibratedTimeStamp;
			double expectedTimeDifference = (1/mShimmerSamplingRate)*1000;
			double expectedTimeDifferenceLimit = expectedTimeDifference + (expectedTimeDifference*0.1); 
			//if (timeDifference>(1/(mShimmerSamplingRate-1))*1000){
			if (timeDifference>expectedTimeDifferenceLimit){
//				mPacketLossCount=mPacketLossCount+1;
				mPacketLossCount+= (long) (timeDifference/expectedTimeDifferenceLimit);
				Long mTotalNumberofPackets=(long) ((calibratedTimeStamp-mCalTimeStart)/(1/mShimmerSamplingRate*1000));

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
	
	protected double calibratePhillipsSkinTemperatureData(double uncalibratedData){
		
		/* 
		 * SEE Bridge Amplifer+ User Manual for Details
		 * 
		 * y = -27.42ln(x) + 56.502 
		 * where y = temperature in degC
		 * where x = (200*Vo)/((10.1)Pv-Vo)
		 * where Pv = 3000mV
		 * where Vo = Uncalibrated output of the resistance amplifier channel
		 * 
		*/
		
		double x = (200.0*uncalibratedData)/((10.1)*3000-uncalibratedData);
		double y = -27.42*Math.log(x) + 56.502;
		
		return y;
	}

	protected double calibrateGsrData(double gsrUncalibratedData,double p1, double p2){
		gsrUncalibratedData = (double)((int)gsrUncalibratedData & 4095); 
		//the following polynomial is deprecated and has been replaced with a more accurate linear one, see GSR user guide for further details
		//double gsrCalibratedData = (p1*Math.pow(gsrUncalibratedData,4)+p2*Math.pow(gsrUncalibratedData,3)+p3*Math.pow(gsrUncalibratedData,2)+p4*gsrUncalibratedData+p5)/1000;
		//the following is the new linear method see user GSR user guide for further details
		double gsrCalibratedData = (1/((p1*gsrUncalibratedData)+p2)*1000); //kohms 
		return gsrCalibratedData;  
	}

	protected double calibrateGsrDataToSiemens(double gsrUncalibratedData,double p1, double p2){
		gsrUncalibratedData = (double)((int)gsrUncalibratedData & 4095); 
		//the following polynomial is deprecated and has been replaced with a more accurate linear one, see GSR user guide for further details
		//double gsrCalibratedData = (p1*Math.pow(gsrUncalibratedData,4)+p2*Math.pow(gsrUncalibratedData,3)+p3*Math.pow(gsrUncalibratedData,2)+p4*gsrUncalibratedData+p5)/1000;
		//the following is the new linear method see user GSR user guide for further details
		double gsrCalibratedData = (((p1*gsrUncalibratedData)+p2)); //microsiemens 
		return gsrCalibratedData;  
	}

	public double getSamplingRate(){
		return mShimmerSamplingRate;
	}

	/** 0 = +/-2g, 1 = +/-4g, 2 = +/-8g, 3 = +/- 16g */
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
		if (mHardwareVersion==HW_ID.SHIMMER_2 || mHardwareVersion==HW_ID.SHIMMER_2R){

			
			mPacketSize = mTimeStampPacketByteSize +bufferInquiry[3]*2; 
			mShimmerSamplingRate = (double)1024/bufferInquiry[0];
			if (mLSM303MagRate==3 && mShimmerSamplingRate>10){
				mLowPowerMag = true;
			}
			mAccelRange = bufferInquiry[1];
			mConfigByte0 = bufferInquiry[2] & 0xFF; //convert the byte to unsigned integer
			mNChannels = bufferInquiry[3];
			mBufferSize = bufferInquiry[4];
			byte[] signalIdArray = new byte[mNChannels];
			System.arraycopy(bufferInquiry, 5, signalIdArray, 0, mNChannels);
			updateEnabledSensorsFromChannels(signalIdArray);
			interpretDataPacketFormat(mNChannels,signalIdArray);
			mInquiryResponseBytes = new byte[5+mNChannels];
			System.arraycopy(bufferInquiry, 0, mInquiryResponseBytes , 0, mInquiryResponseBytes.length);
		} 
		else if (mHardwareVersion==HW_ID.SHIMMER_3) {
			mPacketSize = mTimeStampPacketByteSize+bufferInquiry[6]*2; 
			mShimmerSamplingRate = (32768/(double)((int)(bufferInquiry[0] & 0xFF) + ((int)(bufferInquiry[1] & 0xFF) << 8)));
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
			if ((mLSM303DigitalAccelRate==2 && mShimmerSamplingRate>10)){
				mLowPowerAccelWR = true;
			}
			if ((mMPU9150GyroAccelRate==0xFF && mShimmerSamplingRate>10)){
				mLowPowerGyro = true;
			}
			if ((mLSM303MagRate==4 && mShimmerSamplingRate>10)){
				mLowPowerMag = true;
			}
			byte[] signalIdArray = new byte[mNChannels];
			System.arraycopy(bufferInquiry, 8, signalIdArray, 0, mNChannels);
			updateEnabledSensorsFromChannels(signalIdArray);
			interpretDataPacketFormat(mNChannels,signalIdArray);
		} 
		else if (mHardwareVersion==HW_ID.SHIMMER_SR30) {
			mPacketSize = mTimeStampPacketByteSize+bufferInquiry[2]*2; 
			mShimmerSamplingRate = (double)1024/bufferInquiry[0];
			mAccelRange = bufferInquiry[1];
			mNChannels = bufferInquiry[2];
			mBufferSize = bufferInquiry[3];
			byte[] signalIdArray = new byte[mNChannels];
			System.arraycopy(bufferInquiry, 4, signalIdArray, 0, mNChannels); // this is 4 because there is no config byte
			interpretDataPacketFormat(mNChannels,signalIdArray);

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
			if (mHardwareVersion==HW_ID.SHIMMER_3){
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

			} else if(mHardwareVersion==HW_ID.SHIMMER_2R){
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
	/** replaced with getShimmerUserAssignedName()
	 * @return
	 */
	@Deprecated 
	public String getDeviceName(){
		return mShimmerUserAssignedName;
	}
	public String getBluetoothAddress(){
		return  mMyBluetoothAddress;
	}
	/** replaced with setShimmerUserAssignedName()
	 * @return
	 */
	@Deprecated 
	public void setDeviceName(String deviceName) {
		mShimmerUserAssignedName = deviceName;
	}
	public byte[] getRawInquiryResponse(){
		return mInquiryResponseBytes;
	}

	public byte[] getRawCalibrationParameters(){

		byte[] rawcal=new byte[1];
		if (mHardwareVersion==HW_ID.SHIMMER_3)
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

		} else if (mHardwareVersion==HW_ID.SHIMMER_2 ||mHardwareVersion==HW_ID.SHIMMER_2R)
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
		if (mHardwareVersion==HW_ID.SHIMMER_3){
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
		if (mHardwareVersion==HW_ID.SHIMMER_2 || mHardwareVersion==HW_ID.SHIMMER_2R){
			if (((mEnabledSensors & 0xFF) & SENSOR_ECG) > 0) {
				listofSensors.add("ECG");
			}
			if (((mEnabledSensors & 0xFF) & SENSOR_EMG) > 0) {
				listofSensors.add("EMG");
			}
		}
			if (((mEnabledSensors & 0xFF00) & SENSOR_BRIDGE_AMP) > 0) {
				listofSensors.add("Bridge Amplifier");
			}
		
		if (((mEnabledSensors & 0xFF00) & SENSOR_HEART) > 0) {
			listofSensors.add("Heart Rate");
		}
		if (((mEnabledSensors & 0xFF) & SENSOR_EXP_BOARD_A0) > 0 && (mEnabledSensors & SENSOR_BATT) == 0 && mHardwareVersion != HW_ID.SHIMMER_3) {
			listofSensors.add("ExpBoard A0");
		}
		if (((mEnabledSensors & 0xFF) & SENSOR_EXP_BOARD_A7) > 0  && (mEnabledSensors & SENSOR_BATT) == 0 && mHardwareVersion != HW_ID.SHIMMER_3) {
			listofSensors.add("ExpBoard A7");
		}
		if ((mEnabledSensors & SENSOR_BATT) > 0) {
			listofSensors.add("Battery Voltage");
		}
		if (((mEnabledSensors & 0xFF) & SENSOR_EXT_ADC_A7) > 0  && mHardwareVersion == HW_ID.SHIMMER_3) {
			listofSensors.add("External ADC A7");
		}
		if (((mEnabledSensors & 0xFF) & SENSOR_EXT_ADC_A6) > 0  && mHardwareVersion == HW_ID.SHIMMER_3) {
			listofSensors.add("External ADC A6");
		}
		if (((mEnabledSensors & 0xFFFF) & SENSOR_EXT_ADC_A15) > 0  && mHardwareVersion == HW_ID.SHIMMER_3) {
			listofSensors.add("External ADC A15");
		}
		if (((mEnabledSensors & 0xFFFF) & SENSOR_INT_ADC_A1) > 0  && mHardwareVersion == HW_ID.SHIMMER_3) {
			listofSensors.add("Internal ADC A1");
		}
		if (((mEnabledSensors & 0xFFFF) & SENSOR_INT_ADC_A12) > 0  && mHardwareVersion == HW_ID.SHIMMER_3) {
			listofSensors.add("Internal ADC A12");
		}
		if (((mEnabledSensors & 0xFFFFFF) & SENSOR_INT_ADC_A13) > 0  && mHardwareVersion == HW_ID.SHIMMER_3) {
			listofSensors.add("Internal ADC A13");
		}
		if (((mEnabledSensors & 0xFFFFFF) & SENSOR_INT_ADC_A14) > 0  && mHardwareVersion == HW_ID.SHIMMER_3) {
			listofSensors.add("Internal ADC A14");
		}
		if ((mEnabledSensors & SENSOR_BMP180) > 0  && mHardwareVersion == HW_ID.SHIMMER_3) {
			listofSensors.add("Pressure");
		}
		if ((mEnabledSensors & SENSOR_EXG1_24BIT) > 0  && mHardwareVersion == HW_ID.SHIMMER_3) {
			listofSensors.add("EXG1");
		}
		if ((mEnabledSensors & SENSOR_EXG2_24BIT) > 0  && mHardwareVersion == HW_ID.SHIMMER_3) {
			listofSensors.add("EXG2");
		}
		if ((mEnabledSensors & SENSOR_EXG1_16BIT) > 0  && mHardwareVersion == HW_ID.SHIMMER_3) {
			listofSensors.add("EXG1 16Bit");
		}
		if ((mEnabledSensors & SENSOR_EXG2_16BIT) > 0  && mHardwareVersion == HW_ID.SHIMMER_3) {
			listofSensors.add("EXG2 16Bit");
		}

		return listofSensors;
	}
	
	/** Returns a list of string[] of the four properties. 1) Shimmer Name - 2) Property/Signal Name - 3) Format Name - 4) Unit Name
	 * @return list string array of properties
	 */
	public List<String[]> getListofEnabledSensorSignalsandFormats(){
		List<String[]> listofSignals = new ArrayList<String[]>();
		 
		if (mHardwareVersion==HW_ID.SHIMMER_2 || mHardwareVersion==HW_ID.SHIMMER_2R){
			String[] channel = new String[]{mShimmerUserAssignedName,Shimmer2.ObjectClusterSensorName.TIMESTAMP,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLISECONDS};
			listofSignals.add(channel);
			channel = new String[]{mShimmerUserAssignedName,Shimmer2.ObjectClusterSensorName.TIMESTAMP,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS};
			listofSignals.add(channel);
			if (((mEnabledSensors & 0xFF)& SENSOR_ACCEL) > 0){
				String unit = CHANNEL_UNITS.METER_PER_SECOND_SQUARE;
				if (mDefaultCalibrationParametersAccel == true) {
					unit += "*";
				}
				
				channel = new String[]{mShimmerUserAssignedName,Shimmer2.ObjectClusterSensorName.ACCEL_X,CHANNEL_TYPE.CAL.toString(),unit};
				listofSignals.add(channel);
				channel = new String[]{mShimmerUserAssignedName,Shimmer2.ObjectClusterSensorName.ACCEL_X,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS};
				listofSignals.add(channel);
				
				channel = new String[]{mShimmerUserAssignedName,Shimmer2.ObjectClusterSensorName.ACCEL_Y,CHANNEL_TYPE.CAL.toString(),unit};
				listofSignals.add(channel);
				channel = new String[]{mShimmerUserAssignedName,Shimmer2.ObjectClusterSensorName.ACCEL_Y,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS};
				listofSignals.add(channel);
				
				channel = new String[]{mShimmerUserAssignedName,Shimmer2.ObjectClusterSensorName.ACCEL_Z,CHANNEL_TYPE.CAL.toString(),unit};
				listofSignals.add(channel);
				channel = new String[]{mShimmerUserAssignedName,Shimmer2.ObjectClusterSensorName.ACCEL_Z,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS};
				listofSignals.add(channel);
				
			}
			if (((mEnabledSensors & 0xFF)& SENSOR_GYRO) > 0) {
				String unit = CHANNEL_UNITS.DEGREES_PER_SECOND;
				if (mDefaultCalibrationParametersGyro == true) {
					unit += "*";
				} 
				channel = new String[]{mShimmerUserAssignedName,Shimmer2.ObjectClusterSensorName.GYRO_X,CHANNEL_TYPE.CAL.toString(),unit};
				listofSignals.add(channel);
				channel = new String[]{mShimmerUserAssignedName,Shimmer2.ObjectClusterSensorName.GYRO_X,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS};
				listofSignals.add(channel);
				
				channel = new String[]{mShimmerUserAssignedName,Shimmer2.ObjectClusterSensorName.GYRO_Y,CHANNEL_TYPE.CAL.toString(),unit};
				listofSignals.add(channel);
				channel = new String[]{mShimmerUserAssignedName,Shimmer2.ObjectClusterSensorName.GYRO_Y,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS};
				listofSignals.add(channel);
				
				channel = new String[]{mShimmerUserAssignedName,Shimmer2.ObjectClusterSensorName.GYRO_Z,CHANNEL_TYPE.CAL.toString(),unit};
				listofSignals.add(channel);
				channel = new String[]{mShimmerUserAssignedName,Shimmer2.ObjectClusterSensorName.GYRO_Z,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS};
				listofSignals.add(channel);
			}
			if (((mEnabledSensors & 0xFF)& SENSOR_MAG) > 0) {
				String unit = CHANNEL_UNITS.LOCAL_FLUX;
				if (mDefaultCalibrationParametersGyro == true) {
					unit += "*";
				} 
				channel = new String[]{mShimmerUserAssignedName,Shimmer2.ObjectClusterSensorName.MAG_X,CHANNEL_TYPE.CAL.toString(),unit};
				listofSignals.add(channel);
				channel = new String[]{mShimmerUserAssignedName,Shimmer2.ObjectClusterSensorName.MAG_X,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS};
				listofSignals.add(channel);
				
				channel = new String[]{mShimmerUserAssignedName,Shimmer2.ObjectClusterSensorName.MAG_Y,CHANNEL_TYPE.CAL.toString(),unit};
				listofSignals.add(channel);
				channel = new String[]{mShimmerUserAssignedName,Shimmer2.ObjectClusterSensorName.MAG_Y,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS};
				listofSignals.add(channel);
				
				channel = new String[]{mShimmerUserAssignedName,Shimmer2.ObjectClusterSensorName.MAG_Z,CHANNEL_TYPE.CAL.toString(),unit};
				listofSignals.add(channel);
				channel = new String[]{mShimmerUserAssignedName,Shimmer2.ObjectClusterSensorName.MAG_Z,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS};
				listofSignals.add(channel);
			}
			if (((mEnabledSensors & 0xFF) & SENSOR_GSR) > 0) {
				channel = new String[]{mShimmerUserAssignedName,Shimmer2.ObjectClusterSensorName.GSR,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.KOHMS};
				listofSignals.add(channel);
				channel = new String[]{mShimmerUserAssignedName,Shimmer2.ObjectClusterSensorName.GSR,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS};
				listofSignals.add(channel);
				
				//channel = new String[]{mMyName,Shimmer2.ObjectClusterSensorName.GSR_RES,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS};
				//listofSignals.add(channel);
			}
			if (((mEnabledSensors & 0xFF) & SENSOR_ECG) > 0) {
				
				String unit = CHANNEL_UNITS.MILLIVOLTS;
				if (mDefaultCalibrationParametersECG == true) {
					unit += "*";
				}
				
				channel = new String[]{mShimmerUserAssignedName,Shimmer2.ObjectClusterSensorName.ECG_RA_LL,CHANNEL_TYPE.CAL.toString(),unit};
				listofSignals.add(channel);
				channel = new String[]{mShimmerUserAssignedName,Shimmer2.ObjectClusterSensorName.ECG_RA_LL,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS};
				listofSignals.add(channel);
				
				channel = new String[]{mShimmerUserAssignedName,Shimmer2.ObjectClusterSensorName.ECG_LA_LL,CHANNEL_TYPE.CAL.toString(),unit};
				listofSignals.add(channel);
				channel = new String[]{mShimmerUserAssignedName,Shimmer2.ObjectClusterSensorName.ECG_LA_LL,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS};
				listofSignals.add(channel);
				
			}
			if (((mEnabledSensors & 0xFF) & SENSOR_EMG) > 0) {
				String unit = CHANNEL_UNITS.MILLIVOLTS;
				if (mDefaultCalibrationParametersECG == true) {
					unit += "*";
				}
				
				channel = new String[]{mShimmerUserAssignedName,Shimmer2.ObjectClusterSensorName.EMG,CHANNEL_TYPE.CAL.toString(),unit};
				listofSignals.add(channel);
				channel = new String[]{mShimmerUserAssignedName,Shimmer2.ObjectClusterSensorName.EMG,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS};
				listofSignals.add(channel);
			}
			if (((mEnabledSensors & 0xFF00) & SENSOR_BRIDGE_AMP) > 0) {
				String unit = CHANNEL_UNITS.MILLIVOLTS;
				channel = new String[]{mShimmerUserAssignedName,Shimmer2.ObjectClusterSensorName.BRIDGE_AMP_HIGH,CHANNEL_TYPE.CAL.toString(),unit};
				listofSignals.add(channel);
				channel = new String[]{mShimmerUserAssignedName,Shimmer2.ObjectClusterSensorName.BRIDGE_AMP_HIGH,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS};
				listofSignals.add(channel);
				
				channel = new String[]{mShimmerUserAssignedName,Shimmer2.ObjectClusterSensorName.BRIDGE_AMP_LOW,CHANNEL_TYPE.CAL.toString(),unit};
				listofSignals.add(channel);
				channel = new String[]{mShimmerUserAssignedName,Shimmer2.ObjectClusterSensorName.BRIDGE_AMP_LOW,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS};
				listofSignals.add(channel);
			}
			if (((mEnabledSensors & 0xFF00) & SENSOR_HEART) > 0) {
				String unit = CHANNEL_UNITS.BEATS_PER_MINUTE;
				channel = new String[]{mShimmerUserAssignedName,Shimmer2.ObjectClusterSensorName.HEART_RATE,CHANNEL_TYPE.CAL.toString(),unit};
				listofSignals.add(channel);
				channel = new String[]{mShimmerUserAssignedName,Shimmer2.ObjectClusterSensorName.HEART_RATE,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS};
				listofSignals.add(channel);
			}
			if ((((mEnabledSensors & 0xFF) & SENSOR_EXP_BOARD_A0) > 0) && getPMux() == 0) {
				String unit = CHANNEL_UNITS.MILLIVOLTS;
				channel = new String[]{mShimmerUserAssignedName,Shimmer2.ObjectClusterSensorName.EXP_BOARD_A0,CHANNEL_TYPE.CAL.toString(),unit};
				listofSignals.add(channel);
				channel = new String[]{mShimmerUserAssignedName,Shimmer2.ObjectClusterSensorName.EXP_BOARD_A0,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS};
				listofSignals.add(channel);
			}
			if ((((mEnabledSensors & 0xFF) & SENSOR_EXP_BOARD_A7) > 0 && getPMux() == 0)) {
				String unit = CHANNEL_UNITS.MILLIVOLTS;
				channel = new String[]{mShimmerUserAssignedName,Shimmer2.ObjectClusterSensorName.EXP_BOARD_A7,CHANNEL_TYPE.CAL.toString(),unit};
				listofSignals.add(channel);
				channel = new String[]{mShimmerUserAssignedName,Shimmer2.ObjectClusterSensorName.EXP_BOARD_A7,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS};
				listofSignals.add(channel);
			}
			if (((mEnabledSensors & 0xFFFF) & SENSOR_BATT) > 0) {
				channel = new String[]{mShimmerUserAssignedName,Shimmer2.ObjectClusterSensorName.REG,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS};
				listofSignals.add(channel);
				channel = new String[]{mShimmerUserAssignedName,Shimmer2.ObjectClusterSensorName.REG,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS};
				listofSignals.add(channel);
				channel = new String[]{mShimmerUserAssignedName,Shimmer2.ObjectClusterSensorName.BATTERY,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS};
				listofSignals.add(channel);
				channel = new String[]{mShimmerUserAssignedName,Shimmer2.ObjectClusterSensorName.BATTERY,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS};
				listofSignals.add(channel);
			}
			if (((mEnabledSensors & 0xFF)& SENSOR_ACCEL) > 0 && ((mEnabledSensors & 0xFF)& SENSOR_GYRO) > 0 && ((mEnabledSensors & 0xFF)& SENSOR_MAG) > 0 && mOrientationEnabled){
				channel = new String[]{mShimmerUserAssignedName,Shimmer2.ObjectClusterSensorName.AXIS_ANGLE_A,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL};
				listofSignals.add(channel);
				channel = new String[]{mShimmerUserAssignedName,Shimmer2.ObjectClusterSensorName.AXIS_ANGLE_X,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL};
				listofSignals.add(channel);
				channel = new String[]{mShimmerUserAssignedName,Shimmer2.ObjectClusterSensorName.AXIS_ANGLE_Y,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL};
				listofSignals.add(channel);
				channel = new String[]{mShimmerUserAssignedName,Shimmer2.ObjectClusterSensorName.AXIS_ANGLE_Z,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL};
				listofSignals.add(channel);
				channel = new String[]{mShimmerUserAssignedName,Shimmer2.ObjectClusterSensorName.QUAT_MADGE_9DOF_W,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL};
				listofSignals.add(channel);
				channel = new String[]{mShimmerUserAssignedName,Shimmer2.ObjectClusterSensorName.QUAT_MADGE_9DOF_X,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL};
				listofSignals.add(channel);
				channel = new String[]{mShimmerUserAssignedName,Shimmer2.ObjectClusterSensorName.QUAT_MADGE_9DOF_Y,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL};
				listofSignals.add(channel);
				channel = new String[]{mShimmerUserAssignedName,Shimmer2.ObjectClusterSensorName.QUAT_MADGE_9DOF_Z,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL};
				listofSignals.add(channel);
			}
//			if (((mEnabledSensors & 0xFF)& SENSOR_ACCEL) > 0 && ((mEnabledSensors & 0xFF)& SENSOR_GYRO) > 0 && ((mEnabledSensors & 0xFF)& SENSOR_MAG) > 0 && mOrientationEnabled){
//				String unit = "local";
//				channel = new String[]{mMyName,"Quaternion 0",CHANNEL_TYPE.CALIBRATED,unit};
//				listofSignals.add(channel);
//				channel = new String[]{mMyName,"Quaternion 1",CHANNEL_TYPE.CALIBRATED,unit};
//				listofSignals.add(channel);
//				channel = new String[]{mMyName,"Quaternion 2",CHANNEL_TYPE.CALIBRATED,unit};
//				listofSignals.add(channel);
//				channel = new String[]{mMyName,"Quaternion 3",CHANNEL_TYPE.CALIBRATED,unit};
//				listofSignals.add(channel);
//			}

		} else if (mHardwareVersion==HW_ID.SHIMMER_3) {

			String[] channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.TIMESTAMP,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLISECONDS};
			listofSignals.add(channel);
			channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.TIMESTAMP,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS};
			listofSignals.add(channel);
			if ((mEnabledSensors & SENSOR_ACCEL) >0){

				String unit = CHANNEL_UNITS.METER_PER_SECOND_SQUARE;
				if (mDefaultCalibrationParametersAccel == true) {
					unit += "*";
				}
				
				channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.ACCEL_LN_X,CHANNEL_TYPE.CAL.toString(),unit};
				listofSignals.add(channel);
				channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.ACCEL_LN_X,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS};
				listofSignals.add(channel);
				
				channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.ACCEL_LN_Y,CHANNEL_TYPE.CAL.toString(),unit};
				listofSignals.add(channel);
				channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.ACCEL_LN_Y,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS};
				listofSignals.add(channel);
				
				channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.ACCEL_LN_Z,CHANNEL_TYPE.CAL.toString(),unit};
				listofSignals.add(channel);
				channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.ACCEL_LN_Z,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS};
				listofSignals.add(channel);
				
			}
			if ((mEnabledSensors& SENSOR_DACCEL) >0){


				String unit = CHANNEL_UNITS.METER_PER_SECOND_SQUARE;
				if (mDefaultCalibrationParametersDigitalAccel == true) {
					unit += "*";
				}
				
				channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.ACCEL_WR_X,CHANNEL_TYPE.CAL.toString(),unit};
				listofSignals.add(channel);
				channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.ACCEL_WR_X,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS};
				listofSignals.add(channel);
				
				channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.ACCEL_WR_Y,CHANNEL_TYPE.CAL.toString(),unit};
				listofSignals.add(channel);
				channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.ACCEL_WR_Y,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS};
				listofSignals.add(channel);
				
				channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.ACCEL_WR_Z,CHANNEL_TYPE.CAL.toString(),unit};
				listofSignals.add(channel);
				channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.ACCEL_WR_Z,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS};
				listofSignals.add(channel);
				
			
			}
			if (((mEnabledSensors & 0xFF)& SENSOR_GYRO) > 0) {
				String unit = CHANNEL_UNITS.DEGREES_PER_SECOND;
				if (mDefaultCalibrationParametersGyro == true) {
					unit += "*";
				} 
				channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.GYRO_X,CHANNEL_TYPE.CAL.toString(),unit};
				listofSignals.add(channel);
				channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.GYRO_X,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS};
				listofSignals.add(channel);
				
				channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.GYRO_Y,CHANNEL_TYPE.CAL.toString(),unit};
				listofSignals.add(channel);
				channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.GYRO_Y,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS};
				listofSignals.add(channel);
				
				channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.GYRO_Z,CHANNEL_TYPE.CAL.toString(),unit};
				listofSignals.add(channel);
				channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.GYRO_Z,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS};
				listofSignals.add(channel);
			}
			if (((mEnabledSensors & 0xFF)& SENSOR_MAG) > 0) {
				String unit = CHANNEL_UNITS.LOCAL_FLUX;
				if (mDefaultCalibrationParametersGyro == true) {
					unit += "*";
				} 
				channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.MAG_X,CHANNEL_TYPE.CAL.toString(),unit};
				listofSignals.add(channel);
				channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.MAG_X,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS};
				listofSignals.add(channel);
				
				channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.MAG_Y,CHANNEL_TYPE.CAL.toString(),unit};
				listofSignals.add(channel);
				channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.MAG_Y,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS};
				listofSignals.add(channel);
				
				channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.MAG_Z,CHANNEL_TYPE.CAL.toString(),unit};
				listofSignals.add(channel);
				channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.MAG_Z,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS};
				listofSignals.add(channel);
			} 
			if (((mEnabledSensors & 0xFFFF) & SENSOR_BATT) > 0) {
				channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.BATTERY,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS};
				listofSignals.add(channel);
				channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.BATTERY,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS};
				listofSignals.add(channel);
				
			}
			if (((mEnabledSensors & 0xFFFFFF)& SENSOR_EXT_ADC_A15) > 0) {
				channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.EXT_EXP_ADC_A15,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS};
				listofSignals.add(channel);
				channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.EXT_EXP_ADC_A15,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS};
				listofSignals.add(channel);
				
			}
			if (((mEnabledSensors & 0xFFFFFF)& SENSOR_EXT_ADC_A7) > 0) {
				channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.EXT_EXP_ADC_A7,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS};
				listofSignals.add(channel);
				channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.EXT_EXP_ADC_A7,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS};
				listofSignals.add(channel);
			}
			if (((mEnabledSensors & 0xFFFFFF)& SENSOR_EXT_ADC_A6) > 0) {
				channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.EXT_EXP_ADC_A6,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS};
				listofSignals.add(channel);
				channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.EXT_EXP_ADC_A6,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS};
				listofSignals.add(channel);
			}
			if (((mEnabledSensors & 0xFFFFFF)& SENSOR_INT_ADC_A1) > 0) {
				channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.INT_EXP_ADC_A1,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS};
				listofSignals.add(channel);
				channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.INT_EXP_ADC_A1,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS};
				listofSignals.add(channel);
			}
			if (((mEnabledSensors & 0xFFFFFF)& SENSOR_INT_ADC_A12) > 0) {
				channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.INT_EXP_ADC_A12,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS};
				listofSignals.add(channel);
				channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.INT_EXP_ADC_A12,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS};
				listofSignals.add(channel);
			}
			if (((mEnabledSensors & 0xFFFFFF)& SENSOR_INT_ADC_A13) > 0) {
				channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.INT_EXP_ADC_A13,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS};
				listofSignals.add(channel);
				channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.INT_EXP_ADC_A13,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS};
				listofSignals.add(channel);
			}
			if (((mEnabledSensors & 0xFFFFFF)& SENSOR_INT_ADC_A14) > 0) {
				channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.INT_EXP_ADC_A14,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS};
				listofSignals.add(channel);
				channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.INT_EXP_ADC_A14,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS};
				listofSignals.add(channel);
			}
			if ((((mEnabledSensors & 0xFF)& SENSOR_ACCEL) > 0 || ((mEnabledSensors & 0xFFFF)& SENSOR_DACCEL) > 0)&& ((mEnabledSensors & 0xFF)& SENSOR_GYRO) > 0 && ((mEnabledSensors & 0xFF)& SENSOR_MAG) > 0 && mOrientationEnabled){
				channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.AXIS_ANGLE_A,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL};
				listofSignals.add(channel);
				channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.AXIS_ANGLE_X,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL};
				listofSignals.add(channel);
				channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.AXIS_ANGLE_Y,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL};
				listofSignals.add(channel);
				channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.AXIS_ANGLE_Z,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL};
				listofSignals.add(channel);
				channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.QUAT_MADGE_9DOF_W,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL};
				listofSignals.add(channel);
				channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.QUAT_MADGE_9DOF_X,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL};
				listofSignals.add(channel);
				channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.QUAT_MADGE_9DOF_Y,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL};
				listofSignals.add(channel);
				channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.QUAT_MADGE_9DOF_Z,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL};
				listofSignals.add(channel);
			}
//			if ((((mEnabledSensors & 0xFF)& SENSOR_ACCEL) > 0 || ((mEnabledSensors & 0xFFFF)& SENSOR_DACCEL) > 0) && ((mEnabledSensors & 0xFF)& SENSOR_GYRO) > 0 && ((mEnabledSensors & 0xFF)& SENSOR_MAG) > 0 && mOrientationEnabled){
//				String unit = "local";
//				channel = new String[]{mMyName,"Quaternion 0",CHANNEL_TYPE.CALIBRATED,unit};
//				listofSignals.add(channel);
//				channel = new String[]{mMyName,"Quaternion 1",CHANNEL_TYPE.CALIBRATED,unit};
//				listofSignals.add(channel);
//				channel = new String[]{mMyName,"Quaternion 2",CHANNEL_TYPE.CALIBRATED,unit};
//				listofSignals.add(channel);
//				channel = new String[]{mMyName,"Quaternion 3",CHANNEL_TYPE.CALIBRATED,unit};
//				listofSignals.add(channel);
//			}
			if ((mEnabledSensors & SENSOR_BMP180)>0){
				channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.PRESSURE_BMP180,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.KPASCAL};
				listofSignals.add(channel);
				channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.PRESSURE_BMP180,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS};
				listofSignals.add(channel);
				channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.TEMPERATURE_BMP180,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.DEGREES_CELSUIS};
				listofSignals.add(channel);
				channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.TEMPERATURE_BMP180,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS};
				listofSignals.add(channel);
			}
			if ((mEnabledSensors & SENSOR_GSR)>0){
				channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.GSR,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.KOHMS};
				listofSignals.add(channel);
//				channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.GSR,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MICROSIEMENS};
//				listofSignals.add(channel);
				//channel = new String[]{mMyName,"Magnetometer X",CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS};
				//listofSignals.add(channel);
			}
			if (((mEnabledSensors & SENSOR_EXG1_24BIT)>0)|| ((mEnabledSensors & SENSOR_EXG1_16BIT)>0)){
				channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.EXG1_STATUS,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS};
				listofSignals.add(channel);
			}
			if (((mEnabledSensors & SENSOR_EXG2_24BIT)>0)|| ((mEnabledSensors & SENSOR_EXG2_16BIT)>0)){
				channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.EXG2_STATUS,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS};
				listofSignals.add(channel);
			}
			if ((mEnabledSensors & SENSOR_EXG1_24BIT)>0){
				if (isEXGUsingDefaultECGConfiguration()){
					channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.ECG_LL_RA_24BIT,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS};
					listofSignals.add(channel);
					channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.ECG_LL_RA_24BIT,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS};
					listofSignals.add(channel);
					
					channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.ECG_LA_RA_24BIT,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS};
					listofSignals.add(channel);
					channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.ECG_LA_RA_24BIT,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS};
					listofSignals.add(channel);
				}
				else if (isEXGUsingDefaultEMGConfiguration()){
					channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.EMG_CH1_24BIT,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS};
					listofSignals.add(channel);
					channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.EMG_CH1_24BIT,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS};
					listofSignals.add(channel);
					
					channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.EMG_CH2_24BIT,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS};
					listofSignals.add(channel);
					channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.EMG_CH2_24BIT,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS};
					listofSignals.add(channel);
				} else {
					channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.EXG1_CH1_24BIT,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS};
					listofSignals.add(channel);
					channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.EXG1_CH1_24BIT,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS};
					listofSignals.add(channel);
					
					channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.EXG1_CH2_24BIT,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS};
					listofSignals.add(channel);
					channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.EXG1_CH2_24BIT,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS};
					listofSignals.add(channel);
				}

			}
			if ((mEnabledSensors & SENSOR_EXG2_24BIT)>0){
				if (isEXGUsingDefaultECGConfiguration()){
					channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.EXG2_CH1_24BIT,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS};
					listofSignals.add(channel);
					channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.EXG2_CH1_24BIT,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS};
					listofSignals.add(channel);
					
					channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.ECG_VX_RL_24BIT,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS};
					listofSignals.add(channel);
					channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.ECG_VX_RL_24BIT,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS};
					listofSignals.add(channel);
				}
				else if (isEXGUsingDefaultEMGConfiguration()){
					channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.EXG2_CH1_24BIT,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS};
					listofSignals.add(channel);
					channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.EXG2_CH1_24BIT,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS};
					listofSignals.add(channel);
					
					channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.EXG2_CH2_24BIT,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS};
					listofSignals.add(channel);
					channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.EXG2_CH2_24BIT,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS};
					listofSignals.add(channel);
				}
				else {
					channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.EXG2_CH1_24BIT,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS};
					listofSignals.add(channel);
					channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.EXG2_CH1_24BIT,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS};
					listofSignals.add(channel);

					channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.EXG2_CH2_24BIT,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS};
					listofSignals.add(channel);
					channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.EXG2_CH2_24BIT,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS};
					listofSignals.add(channel);

				}
			}
			if ((mEnabledSensors & SENSOR_EXG1_16BIT)>0){
				if (isEXGUsingDefaultECGConfiguration()){
					channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.ECG_LL_RA_16BIT,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS};
					listofSignals.add(channel);
					channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.ECG_LL_RA_16BIT,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS};
					listofSignals.add(channel);
					
					channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.ECG_LA_RA_16BIT,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS};
					listofSignals.add(channel);
					channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.ECG_LA_RA_16BIT,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS};
					listofSignals.add(channel);
				}
				else if (isEXGUsingDefaultEMGConfiguration()){
					channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.EMG_CH1_16BIT,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS};
					listofSignals.add(channel);
					channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.EMG_CH1_16BIT,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS};
					listofSignals.add(channel);
					
					channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.EMG_CH2_16BIT,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS};
					listofSignals.add(channel);
					channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.EMG_CH2_16BIT,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS};
					listofSignals.add(channel);
				} else {
					channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.EXG1_CH1_16BIT,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS};
					listofSignals.add(channel);
					channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.EXG1_CH1_16BIT,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS};
					listofSignals.add(channel);
					
					channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.EXG1_CH2_16BIT,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS};
					listofSignals.add(channel);
					channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.EXG1_CH2_16BIT,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS};
					listofSignals.add(channel);
				}

			}
			if ((mEnabledSensors & SENSOR_EXG2_16BIT)>0){
				if (isEXGUsingDefaultECGConfiguration()){
					channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.EXG2_CH1_16BIT,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS};
					listofSignals.add(channel);
					channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.EXG2_CH1_16BIT,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS};
					listofSignals.add(channel);
					
					channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.ECG_VX_RL_16BIT,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS};
					listofSignals.add(channel);
					channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.ECG_VX_RL_16BIT,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS};
					listofSignals.add(channel);
				}
				else if (isEXGUsingDefaultEMGConfiguration()){
					channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.EXG2_CH1_16BIT,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS};
					listofSignals.add(channel);
					channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.EXG2_CH1_16BIT,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS};
					listofSignals.add(channel);
					
					channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.EXG2_CH2_16BIT,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS};
					listofSignals.add(channel);
					channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.EXG2_CH2_16BIT,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS};
					listofSignals.add(channel);
				}
				else {
					channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.EXG2_CH1_16BIT,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS};
					listofSignals.add(channel);
					channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.EXG2_CH1_16BIT,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS};
					listofSignals.add(channel);

					channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.EXG2_CH2_16BIT,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS};
					listofSignals.add(channel);
					channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.EXG2_CH2_16BIT,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS};
					listofSignals.add(channel);

				}
			}
			if (((mEnabledSensors & 0xFF00) & SENSOR_BRIDGE_AMP) > 0) {
				channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.BRIDGE_AMP_HIGH,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS};
				listofSignals.add(channel);
				channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.BRIDGE_AMP_HIGH,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS};
				listofSignals.add(channel);
				
				channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.BRIDGE_AMP_LOW,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS};
				listofSignals.add(channel);
				channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.BRIDGE_AMP_LOW,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS};
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
	
	public double[][] getOffsetVectorMPLAccel(){
		return OffsetVectorMPLAccel;
	}
	
	public double[][] getSensitivityMatrixMPLAccel(){
		return SensitivityMatrixMPLAccel;
	}
	
	public double[][] getAlignmentMatrixMPLAccel(){
		return AlignmentMatrixMPLAccel;
	}
	
	public double[][] getOffsetVectorMPLMag(){
		return OffsetVectorMPLMag;
	}
	
	public double[][] getSensitivityMatrixMPLMag(){
		return SensitivityMatrixMPLMag;
	}
	
	public double[][] getAlignmentMatrixMPLMag(){
		return AlignmentMatrixMPLMag;
	}
	
	public double[][] getOffsetVectorMPLGyro(){
		return OffsetVectorMPLGyro;
	}
	
	public double[][] getSensitivityMatrixMPLGyro(){
		return SensitivityMatrixMPLGyro;
	}
	
	public double[][] getAlignmentMatrixMPLGyro(){
		return AlignmentMatrixMPLGyro;
	}


	public long getEnabledSensors() {
		return mEnabledSensors;
	}
	
	public long getRTCOffset(){
		return mRTCOffset;
	}

	public String[] getListofSupportedSensors(){
		String[] sensorNames = null;
		if (mHardwareVersion==HW_ID.SHIMMER_2R || mHardwareVersion==HW_ID.SHIMMER_2){
			sensorNames = Configuration.Shimmer2.ListofCompatibleSensors;
		} else if  (mHardwareVersion==HW_ID.SHIMMER_3){
			sensorNames = Configuration.Shimmer3.ListofCompatibleSensors;
		}
		return sensorNames;
	}

	public static String[] getListofSupportedSensors(int shimmerVersion){
		String[] sensorNames = null;
		if (shimmerVersion==HW_ID.SHIMMER_2R || shimmerVersion==HW_ID.SHIMMER_2){
			sensorNames = Configuration.Shimmer2.ListofCompatibleSensors;
		} else if  (shimmerVersion==HW_ID.SHIMMER_3){
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

	
	public boolean isEXGUsingDefaultECGConfigurationForSDFW(){
//		if((mIsExg1_16bitEnabled&&mIsExg2_16bitEnabled)||(mIsExg1_24bitEnabled&&mIsExg2_24bitEnabled)){
			if(((mEXG1RegisterArray[3] & 0x0F)==0)&&((mEXG1RegisterArray[4] & 0x0F)==0)&& ((mEXG2RegisterArray[3] & 0x0F)==0)&&((mEXG2RegisterArray[4] & 0x0F)==7)){
				return true;
			}
//		}
		return false;

	}

	public boolean isEXGUsingDefaultECGConfiguration(){
		if((mIsExg1_16bitEnabled&&mIsExg2_16bitEnabled)||(mIsExg1_24bitEnabled&&mIsExg2_24bitEnabled)){
			if(((mEXG1RegisterArray[3] & 0x0F)==0)&&((mEXG1RegisterArray[4] & 0x0F)==0)&& ((mEXG2RegisterArray[3] & 0x0F)==0)&&((mEXG2RegisterArray[4] & 0x0F)==7)){
				return true;
			}
		}
		return false;
	}

	public boolean isEXGUsingDefaultEMGConfiguration(){
		if((mIsExg1_16bitEnabled&&!mIsExg2_16bitEnabled)||(mIsExg1_24bitEnabled&&!mIsExg2_24bitEnabled)){
			if(((mEXG1RegisterArray[3] & 0x0F)==9)&&((mEXG1RegisterArray[4] & 0x0F)==0)&& ((mEXG2RegisterArray[3] & 0x0F)==1)&&((mEXG2RegisterArray[4] & 0x0F)==1)){
				return true;
			}
		}
		return false;
	}
	
	public boolean isEXGUsingDefaultTestSignalConfiguration(){
		if((mIsExg1_16bitEnabled&&mIsExg2_16bitEnabled)||(mIsExg1_24bitEnabled&&mIsExg2_24bitEnabled)){
			if(((mEXG1RegisterArray[3] & 0x0F)==5)&&((mEXG1RegisterArray[4] & 0x0F)==5)&& ((mEXG2RegisterArray[3] & 0x0F)==5)&&((mEXG2RegisterArray[4] & 0x0F)==5)){
				return true;
			}
		}
		return false;
	}

	public boolean isEXGUsingDefaultRespirationConfiguration(){
		if((mIsExg1_16bitEnabled&&mIsExg2_16bitEnabled)||(mIsExg1_24bitEnabled&&mIsExg2_24bitEnabled)){
			if((mEXG2RegisterArray[8] & 0xC0)==0xC0){
	//		if(isEXGUsingDefaultECGConfiguration()&&((mEXG2RegisterArray[8] & 0xC0)==0xC0)){
				return true;
			}
		}
		return false;
	}
	
	public boolean isEXGUsingCustomSignalConfiguration(){
		if(mIsExg1_16bitEnabled||mIsExg2_16bitEnabled||mIsExg1_24bitEnabled||mIsExg2_24bitEnabled){
			return true;
		}
		return false;
	}
	

	public byte[] getPressureRawCoefficients(){
		return mPressureCalRawParams;
	}
	
	public int getExg1CH1GainValue(){
		return mEXG1CH1GainValue;
	}
	
	public int getExg1CH2GainValue(){
		return mEXG1CH2GainValue;
	}
	
	public int getExg2CH1GainValue(){
		return mEXG2CH1GainValue;
	}
	
	public int getExg2CH2GainValue(){
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
	
	
	/**
	 * Computes the closest compatible sampling rate for the Shimmer based on
	 * the passed in 'rate' variable. Also computes the next highest available
	 * sampling rate for the Shimmer's sensors (dependent on pre-set low-power
	 * modes).
	 * 
	 * @param rate
	 */
	public void setShimmerSamplingRate(double rate){
		
		double maxGUISamplingRate = 2048.0;
		double maxShimmerSamplingRate = 32768.0;
		
		if (mHardwareVersion==HW_ID.SHIMMER_2 || mHardwareVersion==HW_ID.SHIMMER_2R) {
			maxGUISamplingRate = 1024.0;
		} else if (mHardwareVersion==HW_ID.SHIMMER_3 || mHardwareVersion==HW_ID.SHIMMER_GQ) {
			//check if an MPL channel is enabled - limit rate to 51.2Hz
			if(checkIfAnyMplChannelEnabled() && mFirmwareIdentifier == ShimmerVerDetails.FW_ID.SHIMMER3.SDLOG){
				rate = 51.2;
			}
		}		
    	// don't let sampling rate < 0 OR > maxRate
    	if(rate < 1) {
    		rate = 1.0;
    	}
    	else if (rate > maxGUISamplingRate) {
    		rate = maxGUISamplingRate;
    	}
    	
    	 // get Shimmer compatible sampling rate
    	Double actualSamplingRate = maxShimmerSamplingRate/Math.floor(maxShimmerSamplingRate/rate);
    	 // round sampling rate to two decimal places
    	actualSamplingRate = (double)Math.round(actualSamplingRate * 100) / 100;
		mShimmerSamplingRate = actualSamplingRate;
		
		if(mHardwareVersion==HW_ID.SHIMMER_2 || mHardwareVersion==HW_ID.SHIMMER_2R) {
			if(!mLowPowerMag){
				if(rate<=10) {
					mShimmer2MagRate = 4;
				} 
				else if (rate<=20) {
					mShimmer2MagRate = 5;
				} 
				else {
					mShimmer2MagRate = 6;
				}
			} 
			else {
				mShimmer2MagRate = 4;
			}
		} 
		else if (mHardwareVersion==HW_ID.SHIMMER_3 || mHardwareVersion==HW_ID.SHIMMER_GQ) {
			setLSM303MagRateFromFreq(mShimmerSamplingRate);
			setLSM303AccelRateFromFreq(mShimmerSamplingRate);
			setMPU9150GyroAccelRateFromFreq(mShimmerSamplingRate);
			setExGRateFromFreq(mShimmerSamplingRate);
			
			if(mFirmwareIdentifier==FW_ID.SHIMMER3.SDLOG){
				setMPU9150MagRateFromFreq(mShimmerSamplingRate);
				setMPU9150MplRateFromFreq(mShimmerSamplingRate);
			}

		}
	}
	
	/**
	 * Computes next higher available sensor sampling rate setting based on
	 * passed in "freq" variable and dependent on whether low-power mode is set.
	 * 
	 * @param freq
	 * @return int the rate configuration setting for the respective sensor
	 */
	private int setLSM303AccelRateFromFreq(double freq) {
		// Unused: 8 = 1.620kHz (only low-power mode), 9 = 1.344kHz (normal-mode) / 5.376kHz (low-power mode)
		
		// Check if channel is enabled 
		if (!isSensorEnabled(Configuration.Shimmer3.SensorMapKey.LSM303DLHC_ACCEL)) {
			mLSM303DigitalAccelRate = 0; // Power down
			return mLSM303DigitalAccelRate;
		}
		
		if (!mLowPowerAccelWR){
			if (freq<=1){
				mLSM303DigitalAccelRate = 1; // 1Hz
			} else if (freq<=10){
				mLSM303DigitalAccelRate = 2; // 10Hz
			} else if (freq<=25){
				mLSM303DigitalAccelRate = 3; // 25Hz
			} else if (freq<=50){
				mLSM303DigitalAccelRate = 4; // 50Hz
			} else if (freq<=100){
				mLSM303DigitalAccelRate = 5; // 100Hz
			} else if (freq<=200){
				mLSM303DigitalAccelRate = 6; // 200Hz
			} else if (freq<=400){
				mLSM303DigitalAccelRate = 7; // 400Hz
			} else {
				mLSM303DigitalAccelRate = 9; // 1344Hz
			}
		}
		else {
			if (freq>=10){
				mLSM303DigitalAccelRate = 2; // 10Hz
			} else {
				mLSM303DigitalAccelRate = 1; // 1Hz
			}
		}
		return mLSM303DigitalAccelRate;
	}
	
	/**
	 * Computes next higher available sensor sampling rate setting based on
	 * passed in "freq" variable and dependent on whether low-power mode is set.
	 * 
	 * @param freq
	 * @return int the rate configuration setting for the respective sensor
	 */
	private int setLSM303MagRateFromFreq(double freq) {
		// Check if channel is enabled 
		if (!isSensorEnabled(Configuration.Shimmer3.SensorMapKey.LSM303DLHC_MAG)) {
			mLSM303MagRate = 0; // 0.75Hz
			return mLSM303MagRate;
		}
		
		if (!mLowPowerMag){
			if (freq<=0.75){
				mLSM303MagRate = 0; // 0.75Hz
			} else if (freq<=1){
				mLSM303MagRate = 1; // 1.5Hz
			} else if (freq<=3) {
				mLSM303MagRate = 2; // 3Hz
			} else if (freq<=7.5) {
				mLSM303MagRate = 3; // 7.5Hz
			} else if (freq<=15) {
				mLSM303MagRate = 4; // 15Hz
			} else if (freq<=30) {
				mLSM303MagRate = 5; // 30Hz
			} else if (freq<=75) {
				mLSM303MagRate = 6; // 75Hz
			} else {
				mLSM303MagRate = 7; // 220Hz
			}
		} else {
			if (freq>=10){
				mLSM303MagRate = 4; // 15Hz
			} else {
				mLSM303MagRate = 1; // 1.5Hz
			}
		}		
		return mLSM303MagRate;
	}
	
	/**
	 * Computes next higher available sensor sampling rate setting based on
	 * passed in "freq" variable and dependent on whether low-power mode is set.
	 * 
	 * @param freq
	 * @return int the rate configuration setting for the respective sensor
	 */
	public int setMPU9150GyroAccelRateFromFreq(double freq) {
		boolean setFreq = false;
		// Check if channel is enabled 
		if(checkIfAnyMplChannelEnabled()){
			setFreq = true;
		}
		else if(checkIfAMpuGyroOrAccelEnabled()){
			setFreq = true;
		}
		
		if(setFreq){
			// Gyroscope Output Rate = 8kHz when the DLPF (Digital Low-pass filter) is disabled (DLPF_CFG = 0 or 7), and 1kHz when the DLPF is enabled
			double numerator = 1000;
			if(mMPU9150LPF == 0) {
				numerator = 8000;
			}
	
			if (!mLowPowerGyro){
				if(freq<4) {
					freq = 4;
				}
				else if(freq>numerator) {
					freq = numerator;
				}
				int result = (int) Math.floor(((numerator / freq) - 1));
				if(result>255) {
					result = 255;
				}
				mMPU9150GyroAccelRate = result;
	
			}
			else {
				mMPU9150GyroAccelRate = 0xFF; // Dec. = 255, Freq. = 31.25Hz (or 3.92Hz when LPF enabled)
			}
		}
		else {
			mMPU9150GyroAccelRate = 0xFF; // Dec. = 255, Freq. = 31.25Hz (or 3.92Hz when LPF enabled)
		}
		return mMPU9150GyroAccelRate;
	}
	
	/**
	 * Computes next higher available sensor sampling rate setting based on
	 * passed in "freq" variable and dependent on whether low-power mode is set.
	 * 
	 * @param freq
	 * @return int the rate configuration setting for the respective sensor
	 */
	public int setExGRateFromFreq(double freq) {
		
		int valueToSet = 0x00; // 125Hz
		if (freq<=125) {
			valueToSet = 0x00; // 125Hz
		} else if (freq<=250) {
			valueToSet = 0x01; // 250Hz
		} else if (freq<=500) {
			valueToSet = 0x02; // 500Hz
		} else if (freq<=1000) {
			valueToSet = 0x03; // 1000Hz
		} else if (freq<=2000) {
			valueToSet = 0x04; // 2000Hz
		} else if (freq<=4000) {
			valueToSet = 0x05; // 4000Hz
		} else if (freq<=8000) {
			valueToSet = 0x06; // 8000Hz
		} else {
			valueToSet = 0x02; // 500Hz
		}
		setEXGRateSetting(valueToSet);
		return mEXG1RateSetting;
	}
	
	/**
	 * Computes next higher available sensor sampling rate setting based on
	 * passed in "freq" variable and dependent on whether low-power mode is set.
	 * 
	 * @param freq
	 * @return int the rate configuration setting for the respective sensor
	 */
	private int setMPU9150MagRateFromFreq(double freq) {
		boolean setFreq = false;
		// Check if channel is enabled 
		if(checkIfAnyMplChannelEnabled()){
			setFreq = true;
		}
		else if (isSensorEnabled(Configuration.Shimmer3.SensorMapKey.MPU9150_MAG)) {
			setFreq = true;
		}
		
		if(setFreq){
			if (freq<=10){
				mMPU9150MagSamplingRate = 0; // 10Hz
			} else if (freq<=20){
				mMPU9150MagSamplingRate = 1; // 20Hz
			} else if (freq<=40) {
				mMPU9150MagSamplingRate = 2; // 40Hz
			} else if (freq<=50) {
				mMPU9150MagSamplingRate = 3; // 50Hz
			} else {
				mMPU9150MagSamplingRate = 4; // 100Hz
			}
		}
		else {
			mMPU9150MagSamplingRate = 0; // 10 Hz
		}
		return mMPU9150MagSamplingRate;
	}
	
	/**
	 * Computes next higher available sensor sampling rate setting based on
	 * passed in "freq" variable and dependent on whether low-power mode is set.
	 * 
	 * @param freq
	 * @return int the rate configuration setting for the respective sensor
	 */
	private int setMPU9150MplRateFromFreq(double freq) {
		// Check if channel is enabled 
		if(!checkIfAnyMplChannelEnabled()){
			mMPU9150MPLSamplingRate = 0; // 10 Hz
			return mMPU9150MPLSamplingRate;
		}
		
		if (freq<=10){
			mMPU9150MPLSamplingRate = 0; // 10Hz
		} else if (freq<=20){
			mMPU9150MPLSamplingRate = 1; // 20Hz
		} else if (freq<=40) {
			mMPU9150MPLSamplingRate = 2; // 40Hz
		} else if (freq<=50) {
			mMPU9150MPLSamplingRate = 3; // 50Hz
		} else {
			mMPU9150MPLSamplingRate = 4; // 100Hz
		}
		return mMPU9150MPLSamplingRate;
	}
	
	
	/**
	 * Checks to see if the MPU9150 gyro is in low power mode. As determined by
	 * the sensor's sampling rate being set to the lowest possible value and not
	 * related to any specific configuration bytes sent to the Shimmer/MPU9150.
	 * 
	 * @return boolean, true if low-power mode enabled
	 */
	public boolean checkLowPowerGyro() {
		if(mMPU9150GyroAccelRate == 0xFF) {
			mLowPowerGyro = true;
		}
		else {
			mLowPowerGyro = false;
		}
		return mLowPowerGyro;
	}

	/**
	 * Checks to see if the LSM303DLHC Mag is in low power mode. As determined by
	 * the sensor's sampling rate being set to the lowest possible value and not
	 * related to any specific configuration bytes sent to the Shimmer/MPU9150.
	 * 
	 * @return boolean, true if low-power mode enabled
	 */
	public boolean checkLowPowerMag() {
		if(mLSM303MagRate <= 4) {
			mLowPowerMag = true;
		}
		else {
			mLowPowerMag = false;
		}
		return mLowPowerMag;
	}
	
	
	/**
	 * Populates the individual ExG related variables in ShimmerObject per ExG
	 * chip based on the ExG configuration byte arrays
	 * 
	 * @param chipIndex
	 *            indicates whether the bytes are specific to chip 1 or chip 2
	 *            on the ExG expansion board.
	 * @param byteArray
	 *            the configuration byte array for an individual chip (10-bytes
	 *            long)
	 */
	public void exgBytesGetConfigFrom(int chipIndex, byte[] byteArray) {
		// to overcome possible backward compatability issues (where
		// bufferAns.length was 11 or 12 using all of the ExG config bytes)
		int index = 1;
		if(byteArray.length == 10) {
			index = 0;
		}
		
		if (chipIndex==1){
			System.arraycopy(byteArray, index, mEXG1RegisterArray, 0, 10);
			// retrieve the gain and rate from the the registers
			mEXG1RateSetting = mEXG1RegisterArray[0] & 7;
			mEXGLeadOffDetectionCurrent = (mEXG1RegisterArray[2] >> 2) & 3;
			mEXGLeadOffComparatorTreshold = (mEXG1RegisterArray[2] >> 5) & 7;
			mEXG1CH1GainSetting = (mEXG1RegisterArray[3] >> 4) & 7;
			mEXG1CH1GainValue = convertEXGGainSettingToValue(mEXG1CH1GainSetting);
			mEXG1CH2GainSetting = (mEXG1RegisterArray[4] >> 4) & 7;
			mEXG1CH2GainValue = convertEXGGainSettingToValue(mEXG1CH2GainSetting);
			mEXGReferenceElectrode = mEXG1RegisterArray[5] & 0x0F;
			mEXG1LeadOffCurrentMode = mEXG1RegisterArray[2] & 1;
			mEXG1Comparators = mEXG1RegisterArray[1] & 0x40;								
			mEXGRLDSense = mEXG1RegisterArray[5] & 0x10;
			mEXG1LeadOffSenseSelection = mEXG1RegisterArray[6] & 0x0f; //2P1N1P
			
			mExGConfigBytesDetails.updateFromRegisterArray(EXG_CHIP_INDEX.CHIP1, mEXG1RegisterArray);
		} 
		
		else if (chipIndex==2){
			System.arraycopy(byteArray, index, mEXG2RegisterArray, 0, 10);
			mEXG2RateSetting = mEXG2RegisterArray[0] & 7;
			mEXG2CH1GainSetting = (mEXG2RegisterArray[3] >> 4) & 7;
			mEXG2CH2PowerDown = (mEXG2RegisterArray[3] >> 7) & 1;
			mEXG2CH1GainValue = convertEXGGainSettingToValue(mEXG2CH1GainSetting);
			mEXG2CH2GainSetting = (mEXG2RegisterArray[4] >> 4) & 7;
			mEXG2CH2GainValue = convertEXGGainSettingToValue(mEXG2CH2GainSetting);
			mEXG2LeadOffCurrentMode = mEXG2RegisterArray[2] & 1;
			mEXG2Comparators = mEXG2RegisterArray[1] & 0x40;
			mEXG2LeadOffSenseSelection = mEXG2RegisterArray[6] & 0x0f; //2P
			
			mEXG2RespirationDetectState = (mEXG2RegisterArray[8] >> 6) & 0x03;
			mEXG2RespirationDetectPhase = (mEXG2RegisterArray[8] >> 2) & 0x0F;
			mEXG2RespirationDetectFreq = (mEXG2RegisterArray[9] >> 2) & 0x01;
			
			mExGConfigBytesDetails.updateFromRegisterArray(EXG_CHIP_INDEX.CHIP2, mEXG2RegisterArray);
		}
		
	}

	public void exgBytesGetConfigFrom(byte[] mEXG1RegisterArray, byte[] mEXG2RegisterArray){
		exgBytesGetConfigFrom(1, mEXG1RegisterArray);
		exgBytesGetConfigFrom(2, mEXG2RegisterArray);
		internalCheckExgModeAndUpdateSensorMap();
	}
	
//	//TODO:2015-06-16 remove the need for this by using map
//	/**
//	 * Generates the ExG configuration byte arrays based on the individual ExG
//	 * related variables stored in ShimmerObject. The resulting arrays are
//	 * stored in the global variables mEXG1RegisterArray and mEXG2RegisterArray.
//	 * 
//	 */
	public void exgBytesGetFromConfig() {
		mEXG1RegisterArray = mExGConfigBytesDetails.getEXG1RegisterArray();
		mEXG2RegisterArray = mExGConfigBytesDetails.getEXG2RegisterArray();
		exgBytesGetConfigFrom(mEXG1RegisterArray, mEXG2RegisterArray);
	}
	
	
	/**
	 * Checks if 16 bit ECG configuration is set on the Shimmer device. Do not
	 * use this command right after setting an EXG setting, as due to the
	 * execution model, the old settings might be returned, if this command is
	 * executed before an ack is received.
	 * 
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
	 * Checks if 24 bit ECG configuration is set on the Shimmer device. Do not
	 * use this command right after setting an EXG setting, as due to the
	 * execution model, the old settings might be returned, if this command is
	 * executed before an ack is received.
	 * 
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
	 * Checks if 16 bit EMG configuration is set on the Shimmer device. Do not
	 * use this command right after setting an EXG setting, as due to the
	 * execution model, the old settings might be returned, if this command is
	 * executed before an ack is received.
	 * 
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
	 * Checks if 24 bit EMG configuration is set on the Shimmer device. Do not
	 * use this command right after setting an EXG setting, as due to the
	 * execution model, the old settings might be returned, if this command is
	 * executed before an ack is received.
	 * 
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
	 * Checks if 16 bit test signal configuration is set on the Shimmer device.
	 * Do not use this command right after setting an EXG setting, as due to the
	 * execution model, the old settings might be returned, if this command is
	 * executed before an ack is received.
	 * 
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
	
	/**
	 * This enables the calculation of 3D orientation through the use of the
	 * gradient descent algorithm, note that the user will have to ensure that
	 * mEnableCalibration has been set to true (see enableCalibration), and that
	 * the accel, gyro and mag has been enabled
	 * 
	 * @param enable
	 */
	protected void set3DOrientation(boolean enable){
		//enable the sensors if they have not been enabled 
		mOrientationEnabled = enable;
	}	
	
	/**
	 * This enables the low power accel option. When not enabled the sampling
	 * rate of the accel is set to the closest value to the actual sampling rate
	 * that it can achieve. In low power mode it defaults to 10Hz. Also and
	 * additional low power mode is used for the LSM303DLHC. This command will
	 * only supports the following Accel range +4g, +8g , +16g
	 * 
	 * @param enable
	 */
	protected void setLowPowerAccelWR(boolean enable){
		mLowPowerAccelWR = enable;
		mHighResAccelWR = !enable;

		setLSM303AccelRateFromFreq(mShimmerSamplingRate);
	}
	
	/**
	 * This enables the low-power gyro option. When not enabled the sampling
	 * rate of the gyro is set to the closest supported value to the actual
	 * sampling rate that it can achieve. For the Shimmer2, in low power mode it
	 * defaults to 10Hz.
	 * 
	 * @param enable
	 */
	protected void setLowPowerGyro(boolean enable){
		if(!checkIfAnyMplChannelEnabled()) {
			mLowPowerGyro = enable;
			setMPU9150GyroAccelRateFromFreq(mShimmerSamplingRate);
		}
		else{
			mLowPowerGyro = false;
			setMPU9150GyroAccelRateFromFreq(mShimmerSamplingRate);
		}
	}
	
	/**
	 * This enables the low power mag option. When not enabled the sampling rate
	 * of the mag is set to the closest supported value to the actual sampling
	 * rate that it can achieve. In low power mode it defaults to 10Hz
	 * 
	 * @param enable
	 */
	protected void setLowPowerMag(boolean enable){
		mLowPowerMag = enable;
		if((mHardwareVersion==HW_ID.SHIMMER_2)||(mHardwareVersion==HW_ID.SHIMMER_2R)){
			if (!mLowPowerMag){
				if (mShimmerSamplingRate>=50){
					mShimmer2MagRate = 6;
				} else if (mShimmerSamplingRate>=20) {
					mShimmer2MagRate = 5;
				} else if (mShimmerSamplingRate>=10) {
					mShimmer2MagRate = 4;
				} else {
					mShimmer2MagRate = 3;
				}
			} else {
				mShimmer2MagRate = 4;
			}
		} else {
			setLSM303MagRateFromFreq(mShimmerSamplingRate);
		}
	}
	
	/**
	 * This can only be used for Shimmer3 devices (EXG) When a enable
	 * configuration is load, the advanced exg configuration is removed, so it
	 * needs to be set again
	 * 
	 */
	 protected void setDefaultECGConfiguration() {
		 if (mHardwareVersion==HW_ID.SHIMMER_3){
//			mEXG1RegisterArray = new byte[]{(byte) 2,(byte) 160,(byte) 16,(byte) 64,(byte) 64,(byte) 45,(byte) 0,(byte) 0,(byte) 2,(byte) 3};
//			mEXG2RegisterArray = new byte[]{(byte) 2,(byte) 160,(byte) 16,(byte) 64,(byte) 71,(byte) 0,(byte) 0,(byte) 0,(byte) 2,(byte) 1};

			clearExgConfig();
			setExgChannelBitsPerMode(Configuration.Shimmer3.SensorMapKey.ECG);
			
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.CONVERSION_MODES.CONTINUOUS);
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.REFERENCE_BUFFER.ON);
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.VOLTAGE_REFERENCE.VREF_2_42V);

			setExgPropertyBothChips(EXG_SETTING_OPTIONS.GAIN_PGA_CH1.GAIN_4);
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.GAIN_PGA_CH2.GAIN_4);

			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP2,EXG_SETTING_OPTIONS.INPUT_SELECTION_CH2.RLDIN_CONNECTED_TO_NEG_INPUT);
			
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1,EXG_SETTING_OPTIONS.RLD_BUFFER_POWER.ENABLED);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1,EXG_SETTING_OPTIONS.RLD_NEG_INPUTS_CH2.RLD_CONNECTED_TO_IN2N);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1,EXG_SETTING_OPTIONS.RLD_POS_INPUTS_CH2.RLD_CONNECTED_TO_IN2P);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1,EXG_SETTING_OPTIONS.RLD_POS_INPUTS_CH1.RLD_CONNECTED_TO_IN1P);
			
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1,EXG_SETTING_OPTIONS.RLD_REFERENCE_SIGNAL.HALF_OF_SUPPLY);

			setExGRateFromFreq(mShimmerSamplingRate);
			exgBytesGetConfigFrom(mEXG1RegisterArray, mEXG2RegisterArray);
		 }
	}

	/**
	 * This can only be used for Shimmer3 devices (EXG) When a enable
	 * configuration is load, the advanced exg configuration is removed, so it
	 * needs to be set again
	 */
	 protected void setDefaultEMGConfiguration(){
		if (mHardwareVersion==HW_ID.SHIMMER_3){
//			mEXG1RegisterArray = new byte[]{(byte) 2,(byte) 160,(byte) 16,(byte) 105,(byte) 96,(byte) 32,(byte) 0,(byte) 0,(byte) 2,(byte) 3};
//			mEXG2RegisterArray = new byte[]{(byte) 2,(byte) 160,(byte) 16,(byte) 129,(byte) 129,(byte) 0,(byte) 0,(byte) 0,(byte) 2,(byte) 1};
			
			clearExgConfig();
			setExgChannelBitsPerMode(Configuration.Shimmer3.SensorMapKey.EMG);
			
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.CONVERSION_MODES.CONTINUOUS);
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.REFERENCE_BUFFER.ON);
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.VOLTAGE_REFERENCE.VREF_2_42V);
			
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1,EXG_SETTING_OPTIONS.GAIN_PGA_CH1.GAIN_12);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1,EXG_SETTING_OPTIONS.GAIN_PGA_CH2.GAIN_12);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1,EXG_SETTING_OPTIONS.INPUT_SELECTION_CH1.ROUTE_CH3_TO_CH1);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1,EXG_SETTING_OPTIONS.INPUT_SELECTION_CH2.NORMAL);
			
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP2,EXG_SETTING_OPTIONS.POWER_DOWN_CH1.POWER_DOWN);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP2,EXG_SETTING_OPTIONS.INPUT_SELECTION_CH1.SHORTED);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP2,EXG_SETTING_OPTIONS.POWER_DOWN_CH2.POWER_DOWN);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP2,EXG_SETTING_OPTIONS.INPUT_SELECTION_CH2.SHORTED);
			
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1,EXG_SETTING_OPTIONS.RLD_BUFFER_POWER.ENABLED);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1,EXG_SETTING_OPTIONS.RLD_REFERENCE_SIGNAL.HALF_OF_SUPPLY);
			
			setExGRateFromFreq(mShimmerSamplingRate);
			exgBytesGetConfigFrom(mEXG1RegisterArray, mEXG2RegisterArray);
		}
		
	}

	/**
	 * This can only be used for Shimmer3 devices (EXG). Enables the test signal
	 * (square wave) of both EXG chips, to use, both EXG1 and EXG2 have to be
	 * enabled
	 */
	 protected void setEXGTestSignal(){
		if (mHardwareVersion==HW_ID.SHIMMER_3){
//			mEXG1RegisterArray = new byte[]{(byte) 2,(byte) 163,(byte) 16,(byte) 5,(byte) 5,(byte) 0,(byte) 0,(byte) 0,(byte) 2,(byte) 1};
//			mEXG2RegisterArray = new byte[]{(byte) 2,(byte) 163,(byte) 16,(byte) 5,(byte) 5,(byte) 0,(byte) 0,(byte) 0,(byte) 2,(byte) 1};
			
			clearExgConfig();
			setExgChannelBitsPerMode(Configuration.Shimmer3.SensorMapKey.EXG_TEST);
			
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.CONVERSION_MODES.CONTINUOUS);
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.REFERENCE_BUFFER.ON);
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.VOLTAGE_REFERENCE.VREF_2_42V);

			setExgPropertyBothChips(EXG_SETTING_OPTIONS.TEST_SIGNAL_SELECTION.ON);
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.TEST_SIGNAL_FREQUENCY.SQUARE_WAVE_1KHZ);

			setExgPropertyBothChips(EXG_SETTING_OPTIONS.INPUT_SELECTION_CH1.TEST_SIGNAL);
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.INPUT_SELECTION_CH2.TEST_SIGNAL);
			
			setExGRateFromFreq(mShimmerSamplingRate);
			exgBytesGetConfigFrom(mEXG1RegisterArray, mEXG2RegisterArray);
		}
	}
	 
		
	/**
	 * This can only be used for Shimmer3 devices (EXG) When a enable
	 * configuration is load, the advanced exg configuration is removed, so it
	 * needs to be set again
	 * 
	 */
	 protected void setDefaultRespirationConfiguration() {
		 if (mHardwareVersion==HW_ID.SHIMMER_3){
//			mEXG1RegisterArray = new byte[]{(byte) 2,(byte) 160,(byte) 16,(byte) 64,(byte) 64,(byte) 32,(byte) 0,(byte) 0,(byte) 2,(byte) 3};
//			mEXG2RegisterArray = new byte[]{(byte) 2,(byte) 160,(byte) 16,(byte) 64,(byte) 71,(byte) 0,(byte) 0,(byte) 0,(byte) 234,(byte) 1};
			
			clearExgConfig();
			setExgChannelBitsPerMode(Configuration.Shimmer3.SensorMapKey.EXG_RESPIRATION);

			setExgPropertyBothChips(EXG_SETTING_OPTIONS.CONVERSION_MODES.CONTINUOUS);
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.REFERENCE_BUFFER.ON);
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.VOLTAGE_REFERENCE.VREF_2_42V);

			setExgPropertyBothChips(EXG_SETTING_OPTIONS.GAIN_PGA_CH1.GAIN_4);
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.GAIN_PGA_CH2.GAIN_4);

//			setExgPropertySingleChip(CHIP_INDEX.CHIP2,EXG_SETTING_OPTIONS.INPUT_SELECTION_CH2.RLDIN_CONNECTED_TO_NEG_INPUT); //TODO:2015-06 check!!
			
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1,EXG_SETTING_OPTIONS.RLD_BUFFER_POWER.ENABLED);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1,EXG_SETTING_OPTIONS.RLD_REFERENCE_SIGNAL.HALF_OF_SUPPLY);

			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP2,EXG_SETTING_OPTIONS.RESPIRATION_DEMOD_CIRCUITRY.ON);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP2,EXG_SETTING_OPTIONS.RESPIRATION_MOD_CIRCUITRY.ON);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP2,EXG_SETTING_OPTIONS.RESPIRATION_PHASE_AT_32KHZ.PHASE_112_5);
			
			setExGRateFromFreq(mShimmerSamplingRate);
			exgBytesGetConfigFrom(mEXG1RegisterArray, mEXG2RegisterArray);
		 }
	}	 

	/**
	 * This can only be used for Shimmer3 devices (EXG). Enables the default
	 * setting for the 'custom' channel.
	 * 
	 * Ben: I believe ExG can do 3 channels single ended maybe 4. For 3, The INN
	 * channels are set to "RLD" and then tie RL to ground (Won't be perfect
	 * because of R4 but should be reasonable). The RLD buffer needs to be
	 * disabled and inputs to the buffer likely set to not connected... For the
	 * 4th, it seems like we could use the RESPMOD input to get RLD - RA (and
	 * then invert in SW?). But this may always be zero if RLD is ground... We
	 * would end up with:
	 * <p>
	 * <li>Chip1 Ch1: LL - RLD 
	 * <li>Chip1 Ch2: LA - RLD 
	 * <li>Chip2 Ch1: Nothing? 
	 * <li>Chip2 Ch2: V1 - RLD
	 * <p>
	 * However there may be an advanced configuration where we use VDD/2 as
	 * input to a channel of Chip1 and then buffer that via RLD and then tie
	 * that buffered 1.5V via RLD to the ground of a sensor and the various
	 * inputs. That config would be best for AC signals (giving a Vdd/2
	 * reference) but limits peak amplitude of the incoming signal.
	 * 
	 * 
	 */
	 protected void setEXGCustom(){
		if (mHardwareVersion==HW_ID.SHIMMER_3){
//			mEXG1RegisterArray = new byte[]{(byte) 2,(byte) 163,(byte) 16,(byte) 7,(byte) 7,(byte) 0,(byte) 0,(byte) 0,(byte) 2,(byte) 1};
//			mEXG2RegisterArray = new byte[]{(byte) 2,(byte) 163,(byte) 16,(byte) 7,(byte) 7,(byte) 0,(byte) 0,(byte) 0,(byte) 2,(byte) 1};
			
			clearExgConfig();
			setExgChannelBitsPerMode(Configuration.Shimmer3.SensorMapKey.EXG_CUSTOM);
			
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.CONVERSION_MODES.CONTINUOUS);
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.REFERENCE_BUFFER.ON);
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.VOLTAGE_REFERENCE.VREF_2_42V);

			setExgPropertyBothChips(EXG_SETTING_OPTIONS.TEST_SIGNAL_SELECTION.ON);
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.TEST_SIGNAL_FREQUENCY.SQUARE_WAVE_1KHZ);

			setExgPropertyBothChips(EXG_SETTING_OPTIONS.INPUT_SELECTION_CH1.RLDIN_CONNECTED_TO_NEG_INPUT);
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.INPUT_SELECTION_CH2.RLDIN_CONNECTED_TO_NEG_INPUT);
			
			setExGRateFromFreq(mShimmerSamplingRate);
			exgBytesGetConfigFrom(mEXG1RegisterArray, mEXG2RegisterArray);
		}
	}
	 
	protected void clearExgConfig(){
		setExgChannelBitsPerMode(-1);
		mExGConfigBytesDetails.startNewExGConig();

		mEXG1RegisterArray = mExGConfigBytesDetails.getEXG1RegisterArray();
		mEXG2RegisterArray = mExGConfigBytesDetails.getEXG2RegisterArray();
		exgBytesGetConfigFrom(mEXG1RegisterArray, mEXG2RegisterArray);
	}
	 
	/** Note: Doesn't update the Sensor Map
	 * @param chipIndex
	 * @param option
	 */
	protected void setExgPropertySingleChip(EXG_CHIP_INDEX chipIndex, ExGConfigOption option){
		mExGConfigBytesDetails.setExgPropertySingleChip(chipIndex,option);
		if(chipIndex==EXG_CHIP_INDEX.CHIP1){
			mEXG1RegisterArray = mExGConfigBytesDetails.getEXG1RegisterArray();
			exgBytesGetConfigFrom(1, mEXG1RegisterArray);
		}
		else if(chipIndex==EXG_CHIP_INDEX.CHIP2){
			mEXG2RegisterArray = mExGConfigBytesDetails.getEXG2RegisterArray();
			exgBytesGetConfigFrom(2, mEXG2RegisterArray);
		}
	}
	
	protected boolean isExgPropertyEnabled(EXG_CHIP_INDEX chipIndex, ExGConfigOption option){
		return mExGConfigBytesDetails.isExgPropertyEnabled(chipIndex, option);
	}
	 
	protected void setExgPropertyBothChips(ExGConfigOption option){
		mExGConfigBytesDetails.setExgPropertyBothChips(option);
		mEXG1RegisterArray = mExGConfigBytesDetails.getEXG1RegisterArray();
		mEXG2RegisterArray = mExGConfigBytesDetails.getEXG2RegisterArray();
		
		exgBytesGetConfigFrom(mEXG1RegisterArray, mEXG2RegisterArray);
	}
	
	/** Note: Doesn't update the Sensor Map
	 * @param chipIndex
	 * @param propertyName
	 * @param value
	 */
	public void setExgPropertySingleChipValue(EXG_CHIP_INDEX chipIndex, String propertyName, int value){
		mExGConfigBytesDetails.setExgPropertyValue(chipIndex,propertyName,value);
		if(chipIndex==EXG_CHIP_INDEX.CHIP1){
			mEXG1RegisterArray = mExGConfigBytesDetails.getEXG1RegisterArray();
			exgBytesGetConfigFrom(1, mEXG1RegisterArray);
		}
		else if(chipIndex==EXG_CHIP_INDEX.CHIP2){
			mEXG2RegisterArray = mExGConfigBytesDetails.getEXG2RegisterArray();
			exgBytesGetConfigFrom(2, mEXG2RegisterArray);
		}
	}
	
	public int getExgPropertySingleChip(EXG_CHIP_INDEX chipIndex, String propertyName){
		return mExGConfigBytesDetails.getExgPropertySingleChip(chipIndex, propertyName);
	}
	
	
	public HashMap<String, Integer> getMapOfExGSettingsChip1(){
		return mExGConfigBytesDetails.mMapOfExGSettingsChip1ThisShimmer;
	}

	public HashMap<String, Integer> getMapOfExGSettingsChip2(){
		return mExGConfigBytesDetails.mMapOfExGSettingsChip2ThisShimmer;
	}

	/**Sets all default Shimmer settings in ShimmerObject.
	 * 
	 */
	public void setDefaultShimmerConfiguration() {
		if (mHardwareVersion != -1){
			
			mShimmerUserAssignedName = DEFAULT_SHIMMER_NAME;
			mTrialName = DEFAULT_EXPERIMENT_NAME;
			
			mTrialNumberOfShimmers = 1;
			mTrialId = 0;
			mButtonStart = 1;
			mShowRtcErrorLeds = 1;
			
			mBluetoothBaudRate=9; //460800

			mInternalExpPower=0;

			mExGResolution = 1;
			mShimmer2MagRate=0;
			
			mMasterShimmer = 0;
			mSingleTouch = 0;
			mTCXO = 0;
			
			mPacketSize=0; 
			mConfigByte0=0;
			mNChannels=0; 
			mBufferSize=0;                   							
			mSyncBroadcastInterval = 0;
			mInitialTimeStamp = 0;
			
			setShimmerSamplingRate(51.2);
			setDefaultECGConfiguration(); 
			
			syncNodesList.clear();
			
			sensorAndConfigMapsCreate();
			if (mHardwareVersion == HW_ID.SHIMMER_3){
				setSensorEnabledState(Configuration.Shimmer3.SensorMapKey.A_ACCEL, true);
				setSensorEnabledState(Configuration.Shimmer3.SensorMapKey.MPU9150_GYRO, true);
				setSensorEnabledState(Configuration.Shimmer3.SensorMapKey.LSM303DLHC_MAG, true);
				setSensorEnabledState(Configuration.Shimmer3.SensorMapKey.VBATT, true);
			}
		}
	}
	
	
	public boolean checkInfoMemValid(byte[] infoMemContents){
		// Check first 6 bytes of InfoMem for 0xFF to determine if contents are valid 
		byte[] comparisonBuffer = new byte[]{-1,-1,-1,-1,-1,-1};
		byte[] detectionBuffer = new byte[comparisonBuffer.length];
		System.arraycopy(infoMemContents, 0, detectionBuffer, 0, detectionBuffer.length);
		if(Arrays.equals(comparisonBuffer, detectionBuffer)) {
			return false;
		}
		return true;
	}
	
	
	/**
	 * Parse the Shimmer's Information Memory when read through the Shimmer
	 * Dock/Consensys Base. The Information Memory is a region of the Shimmer's
	 * inbuilt RAM where all configuration information is stored.
	 * 
	 * @param infoMemContents
	 *            the array of InfoMem bytes.
	 */
	protected void infoMemByteArrayParse(byte[] infoMemContents) {
		String shimmerName = "";

		if(!checkInfoMemValid(infoMemContents)){
			// InfoMem not valid
			setDefaultShimmerConfiguration();
			mShimmerUsingConfigFromInfoMem = false;

//			mShimmerInfoMemBytes = infoMemByteArrayGenerate();
//			mShimmerInfoMemBytes = new byte[infoMemContents.length];
			mInfoMemBytes = infoMemContents;
		}
		else {
			mShimmerUsingConfigFromInfoMem = true;

			// InfoMem valid
			
			mInfoMemBytes = infoMemContents;
			createInfoMemLayoutObjectIfNeeded();

			// InfoMem D - Start - used by BtStream, SdLog and LogAndStream
			// Sampling Rate
			mShimmerSamplingRate = (32768/(double)((int)(infoMemContents[mInfoMemLayout.idxShimmerSamplingRate] & mInfoMemLayout.maskShimmerSamplingRate) + ((int)(infoMemContents[mInfoMemLayout.idxShimmerSamplingRate+1] & mInfoMemLayout.maskShimmerSamplingRate) << 8)));
	
			mBufferSize = (int)(infoMemContents[mInfoMemLayout.idxBufferSize] & mInfoMemLayout.maskBufferSize);
			
			// Sensors
			mEnabledSensors = ((long)infoMemContents[mInfoMemLayout.idxSensors0] & mInfoMemLayout.maskSensors) << mInfoMemLayout.byteShiftSensors0;
			mEnabledSensors += ((long)infoMemContents[mInfoMemLayout.idxSensors1] & mInfoMemLayout.maskSensors) << mInfoMemLayout.byteShiftSensors1;
			mEnabledSensors += ((long)infoMemContents[mInfoMemLayout.idxSensors2] & mInfoMemLayout.maskSensors) << mInfoMemLayout.byteShiftSensors2;

			checkExgResolutionFromEnabledSensorsVar();

			// Configuration
			mLSM303DigitalAccelRate = (infoMemContents[mInfoMemLayout.idxConfigSetupByte0] >> mInfoMemLayout.bitShiftLSM303DLHCAccelSamplingRate) & mInfoMemLayout.maskLSM303DLHCAccelSamplingRate; 
			mAccelRange = (infoMemContents[mInfoMemLayout.idxConfigSetupByte0] >> mInfoMemLayout.bitShiftLSM303DLHCAccelRange) & mInfoMemLayout.maskLSM303DLHCAccelRange;
			if(((infoMemContents[mInfoMemLayout.idxConfigSetupByte0] >> mInfoMemLayout.bitShiftLSM303DLHCAccelLPM) & mInfoMemLayout.maskLSM303DLHCAccelLPM) == mInfoMemLayout.maskLSM303DLHCAccelLPM) {
				mLowPowerAccelWR = true;
			}
			else {
				mLowPowerAccelWR = false;
			}
			if(((infoMemContents[mInfoMemLayout.idxConfigSetupByte0] >> mInfoMemLayout.bitShiftLSM303DLHCAccelHRM) & mInfoMemLayout.maskLSM303DLHCAccelHRM) == mInfoMemLayout.maskLSM303DLHCAccelHRM) {
				mHighResAccelWR = true;
			}
			else {
				mHighResAccelWR = false;
			}
			mMPU9150GyroAccelRate = (infoMemContents[mInfoMemLayout.idxConfigSetupByte1] >> mInfoMemLayout.bitShiftMPU9150AccelGyroSamplingRate) & mInfoMemLayout.maskMPU9150AccelGyroSamplingRate;
			checkLowPowerGyro(); // check rate to determine if Sensor is in LPM mode
			
			mMagRange = (infoMemContents[mInfoMemLayout.idxConfigSetupByte2] >> mInfoMemLayout.bitShiftLSM303DLHCMagRange) & mInfoMemLayout.maskLSM303DLHCMagRange;
			mLSM303MagRate = (infoMemContents[mInfoMemLayout.idxConfigSetupByte2] >> mInfoMemLayout.bitShiftLSM303DLHCMagSamplingRate) & mInfoMemLayout.maskLSM303DLHCMagSamplingRate;
			checkLowPowerMag(); // check rate to determine if Sensor is in LPM mode

			mGyroRange = (infoMemContents[mInfoMemLayout.idxConfigSetupByte2] >> mInfoMemLayout.bitShiftMPU9150GyroRange) & mInfoMemLayout.maskMPU9150GyroRange;
			
			mMPU9150AccelRange = (infoMemContents[mInfoMemLayout.idxConfigSetupByte3] >> mInfoMemLayout.bitShiftMPU9150AccelRange) & mInfoMemLayout.maskMPU9150AccelRange;
			mPressureResolution = (infoMemContents[mInfoMemLayout.idxConfigSetupByte3] >> mInfoMemLayout.bitShiftBMP180PressureResolution) & mInfoMemLayout.maskBMP180PressureResolution;
			mGSRRange = (infoMemContents[mInfoMemLayout.idxConfigSetupByte3] >> mInfoMemLayout.bitShiftGSRRange) & mInfoMemLayout.maskGSRRange;
			mInternalExpPower = (infoMemContents[mInfoMemLayout.idxConfigSetupByte3] >> mInfoMemLayout.bitShiftEXPPowerEnable) & mInfoMemLayout.maskEXPPowerEnable;
			
			//EXG Configuration
			System.arraycopy(infoMemContents, mInfoMemLayout.idxEXGADS1292RChip1Config1, mEXG1RegisterArray, 0, 10);
			System.arraycopy(infoMemContents, mInfoMemLayout.idxEXGADS1292RChip2Config1, mEXG2RegisterArray, 0, 10);
			exgBytesGetConfigFrom(mEXG1RegisterArray, mEXG2RegisterArray);
			
			mBluetoothBaudRate = infoMemContents[mInfoMemLayout.idxBtCommBaudRate] & mInfoMemLayout.maskBaudRate;
			//TODO: hack below -> fix
//			if(!(mBluetoothBaudRate>=0 && mBluetoothBaudRate<=10)){
//				mBluetoothBaudRate = 0; 
//			}
			
			byte[] bufferCalibrationParameters = new byte[mInfoMemLayout.lengthGeneralCalibrationBytes];
			// Analog Accel Calibration Parameters
			System.arraycopy(infoMemContents, mInfoMemLayout.idxAnalogAccelCalibration, bufferCalibrationParameters, 0 , mInfoMemLayout.lengthGeneralCalibrationBytes);
			retrieveKinematicCalibrationParametersFromPacket(bufferCalibrationParameters, ACCEL_CALIBRATION_RESPONSE);
			
			// MPU9150 Gyroscope Calibration Parameters
			bufferCalibrationParameters = new byte[mInfoMemLayout.lengthGeneralCalibrationBytes];
			System.arraycopy(infoMemContents, mInfoMemLayout.idxMPU9150GyroCalibration, bufferCalibrationParameters, 0 , mInfoMemLayout.lengthGeneralCalibrationBytes);
			retrieveKinematicCalibrationParametersFromPacket(bufferCalibrationParameters, GYRO_CALIBRATION_RESPONSE);
			
			// LSM303DLHC Magnetometer Calibration Parameters
			bufferCalibrationParameters = new byte[mInfoMemLayout.lengthGeneralCalibrationBytes];
			System.arraycopy(infoMemContents, mInfoMemLayout.idxLSM303DLHCMagCalibration, bufferCalibrationParameters, 0 , mInfoMemLayout.lengthGeneralCalibrationBytes);
			retrieveKinematicCalibrationParametersFromPacket(bufferCalibrationParameters, MAG_CALIBRATION_RESPONSE);

			// LSM303DLHC Digital Accel Calibration Parameters
			bufferCalibrationParameters = new byte[mInfoMemLayout.lengthGeneralCalibrationBytes];
			System.arraycopy(infoMemContents, mInfoMemLayout.idxLSM303DLHCAccelCalibration, bufferCalibrationParameters, 0 , mInfoMemLayout.lengthGeneralCalibrationBytes);
			retrieveKinematicCalibrationParametersFromPacket(bufferCalibrationParameters, LSM303DLHC_ACCEL_CALIBRATION_RESPONSE);

			//TODO: decide what to do
			// BMP180 Pressure Calibration Parameters

//			mDerivedSensors = (long)0;
//			if((mInfoMemLayout.idxDerivedSensors0>=0)&&(mInfoMemLayout.idxDerivedSensors1>=0)) { // Check if compatible
//				if((infoMemContents[mInfoMemLayout.idxDerivedSensors0] != (byte)mInfoMemLayout.maskDerivedChannelsByte)
//						&&(infoMemContents[mInfoMemLayout.idxDerivedSensors1] != (byte)mInfoMemLayout.maskDerivedChannelsByte)){
//					mDerivedSensors = ((long)infoMemContents[mInfoMemLayout.idxDerivedSensors0] & mInfoMemLayout.maskDerivedChannelsByte) << mInfoMemLayout.byteShiftDerivedSensors0;
//					mDerivedSensors |= ((long)infoMemContents[mInfoMemLayout.idxDerivedSensors1] & mInfoMemLayout.maskDerivedChannelsByte) << mInfoMemLayout.byteShiftDerivedSensors1;
//					if(mInfoMemLayout.idxDerivedSensors2>=0) { // Check if compatible
//						mDerivedSensors |= ((long)infoMemContents[mInfoMemLayout.idxDerivedSensors2] & mInfoMemLayout.maskDerivedChannelsByte) << mInfoMemLayout.byteShiftDerivedSensors2;
//					}
//				}
//			}
			
			mDerivedSensors = (long)0;
			// Check if compatible and not equal to 0xFF
			if((mInfoMemLayout.idxDerivedSensors0>0) && (infoMemContents[mInfoMemLayout.idxDerivedSensors0]!=(byte)mInfoMemLayout.maskDerivedChannelsByte)
					&& (mInfoMemLayout.idxDerivedSensors1>0) && (infoMemContents[mInfoMemLayout.idxDerivedSensors1]!=(byte)mInfoMemLayout.maskDerivedChannelsByte)) { 
				
				mDerivedSensors |= ((long)infoMemContents[mInfoMemLayout.idxDerivedSensors0] & mInfoMemLayout.maskDerivedChannelsByte) << mInfoMemLayout.byteShiftDerivedSensors0;
				mDerivedSensors |= ((long)infoMemContents[mInfoMemLayout.idxDerivedSensors1] & mInfoMemLayout.maskDerivedChannelsByte) << mInfoMemLayout.byteShiftDerivedSensors1;
				
				// Check if compatible and not equal to 0xFF
				if((mInfoMemLayout.idxDerivedSensors2>0) && (infoMemContents[mInfoMemLayout.idxDerivedSensors2]!=(byte)mInfoMemLayout.maskDerivedChannelsByte)){ 
					mDerivedSensors |= ((long)infoMemContents[mInfoMemLayout.idxDerivedSensors2] & mInfoMemLayout.maskDerivedChannelsByte) << mInfoMemLayout.byteShiftDerivedSensors2;
				}
			}

			// InfoMem D - End

//			//SDLog and LogAndStream
//			if(((mFirmwareIdentifier==FW_ID.SHIMMER3.LOGANDSTREAM)||(mFirmwareIdentifier==FW_ID.SHIMMER3.SDLOG))&&(mInfoMemBytes.length >=384)) {
				
				// InfoMem C - Start - used by SdLog and LogAndStream
				if(mFirmwareIdentifier==FW_ID.SHIMMER3.SDLOG) {
					mMPU9150DMP = (infoMemContents[mInfoMemLayout.idxConfigSetupByte4] >> mInfoMemLayout.bitShiftMPU9150DMP) & mInfoMemLayout.maskMPU9150DMP;
					mMPU9150LPF = (infoMemContents[mInfoMemLayout.idxConfigSetupByte4] >> mInfoMemLayout.bitShiftMPU9150LPF) & mInfoMemLayout.maskMPU9150LPF;
					mMPU9150MotCalCfg =  (infoMemContents[mInfoMemLayout.idxConfigSetupByte4] >> mInfoMemLayout.bitShiftMPU9150MotCalCfg) & mInfoMemLayout.maskMPU9150MotCalCfg;
					
					mMPU9150MPLSamplingRate = (infoMemContents[mInfoMemLayout.idxConfigSetupByte5] >> mInfoMemLayout.bitShiftMPU9150MPLSamplingRate) & mInfoMemLayout.maskMPU9150MPLSamplingRate;
					mMPU9150MagSamplingRate = (infoMemContents[mInfoMemLayout.idxConfigSetupByte5] >> mInfoMemLayout.bitShiftMPU9150MagSamplingRate) & mInfoMemLayout.maskMPU9150MagSamplingRate;
					
					mEnabledSensors += ((long)infoMemContents[mInfoMemLayout.idxSensors3] & 0xFF) << mInfoMemLayout.bitShiftSensors3;
					mEnabledSensors += ((long)infoMemContents[mInfoMemLayout.idxSensors4] & 0xFF) << mInfoMemLayout.bitShiftSensors4;
					
					mMPLSensorFusion = (infoMemContents[mInfoMemLayout.idxConfigSetupByte6] >> mInfoMemLayout.bitShiftMPLSensorFusion) & mInfoMemLayout.maskMPLSensorFusion;
					mMPLGyroCalTC = (infoMemContents[mInfoMemLayout.idxConfigSetupByte6] >> mInfoMemLayout.bitShiftMPLGyroCalTC) & mInfoMemLayout.maskMPLGyroCalTC;
					mMPLVectCompCal = (infoMemContents[mInfoMemLayout.idxConfigSetupByte6] >> mInfoMemLayout.bitShiftMPLVectCompCal) & mInfoMemLayout.maskMPLVectCompCal;
					mMPLMagDistCal = (infoMemContents[mInfoMemLayout.idxConfigSetupByte6] >> mInfoMemLayout.bitShiftMPLMagDistCal) & mInfoMemLayout.maskMPLMagDistCal;
					mMPLEnable = (infoMemContents[mInfoMemLayout.idxConfigSetupByte6] >> mInfoMemLayout.bitShiftMPLEnable) & mInfoMemLayout.maskMPLEnable;
					
					String[] dataType={"i16","i16","i16","i16","i16","i16","i8","i8","i8","i8","i8","i8","i8","i8","i8"};
					//MPL Accel Calibration Parameters
					bufferCalibrationParameters = new byte[mInfoMemLayout.lengthGeneralCalibrationBytes];
					System.arraycopy(infoMemContents, mInfoMemLayout.idxMPLAccelCalibration, bufferCalibrationParameters, 0 , mInfoMemLayout.lengthGeneralCalibrationBytes);
					int[] formattedPacket = formatDataPacketReverse(bufferCalibrationParameters,dataType);
					double[] AM=new double[9];
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
					bufferCalibrationParameters = new byte[mInfoMemLayout.lengthGeneralCalibrationBytes];
					System.arraycopy(infoMemContents, mInfoMemLayout.idxMPLMagCalibration, bufferCalibrationParameters, 0 , mInfoMemLayout.lengthGeneralCalibrationBytes);
					formattedPacket = formatDataPacketReverse(bufferCalibrationParameters,dataType);
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
					bufferCalibrationParameters = new byte[mInfoMemLayout.lengthGeneralCalibrationBytes];
					System.arraycopy(infoMemContents, mInfoMemLayout.idxMPLGyroCalibration, bufferCalibrationParameters, 0 , mInfoMemLayout.lengthGeneralCalibrationBytes);
					formattedPacket = formatDataPacketReverse(bufferCalibrationParameters,dataType);
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
				}
				
				// Shimmer Name
				byte[] shimmerNameBuffer = new byte[mInfoMemLayout.lengthShimmerName];
				System.arraycopy(infoMemContents, mInfoMemLayout.idxSDShimmerName, shimmerNameBuffer, 0 , mInfoMemLayout.lengthShimmerName);
				for(byte b : shimmerNameBuffer) {
					if(!UtilShimmer.isAsciiPrintable((char)b)) {
						break;
					}
					shimmerName += (char)b;
				}
				
				// Experiment Name
				byte[] experimentNameBuffer = new byte[mInfoMemLayout.lengthExperimentName];
				System.arraycopy(infoMemContents, mInfoMemLayout.idxSDEXPIDName, experimentNameBuffer, 0 , mInfoMemLayout.lengthExperimentName);
				String experimentName = "";
				for(byte b : experimentNameBuffer) {
					if(!UtilShimmer.isAsciiPrintable((char)b)) {
						break;
					}
					experimentName += (char)b;
				}
				mTrialName = new String(experimentName);
	
				//Configuration Time
				int bitShift = (mInfoMemLayout.lengthConfigTimeBytes-1) * 8;
				mConfigTime = 0;
				for(int x=0; x<mInfoMemLayout.lengthConfigTimeBytes; x++ ) {
					mConfigTime += (((long)(infoMemContents[mInfoMemLayout.idxSDConfigTime0+x] & 0xFF)) << bitShift);
					bitShift -= 8;
				}
//				//TODO can be replaced by more efficient implementation
//				long value = 0;
//				for (int i = 0; i < by.length; i++){
//				   value = (value << 8) + (by[i] & 0xff);
//				}
//				//if ConfigTime is all F's, reset the time to 0 
//				if((mConfigTime&(2^32)) == (2^32)) {
//					mConfigTime = 0;
//				}

				if(mFirmwareIdentifier==FW_ID.SHIMMER3.SDLOG) {
					mTrialId = infoMemContents[mInfoMemLayout.idxSDMyTrialID] & 0xFF;
					mTrialNumberOfShimmers = infoMemContents[mInfoMemLayout.idxSDNumOfShimmers] & 0xFF;
				}
				
				if((mFirmwareIdentifier==FW_ID.SHIMMER3.SDLOG)||(mFirmwareIdentifier==FW_ID.SHIMMER3.LOGANDSTREAM)) {
					mButtonStart = (infoMemContents[mInfoMemLayout.idxSDExperimentConfig0] >> mInfoMemLayout.bitShiftButtonStart) & mInfoMemLayout.maskButtonStart;
					mShowRtcErrorLeds = (infoMemContents[mInfoMemLayout.idxSDExperimentConfig0] >> mInfoMemLayout.bitShiftShowRwcErrorLeds) & mInfoMemLayout.maskShowRwcErrorLeds;
				}
				
				if(mFirmwareIdentifier==FW_ID.SHIMMER3.SDLOG) {
					mSyncWhenLogging = (infoMemContents[mInfoMemLayout.idxSDExperimentConfig0] >> mInfoMemLayout.bitShiftTimeSyncWhenLogging) & mInfoMemLayout.maskTimeSyncWhenLogging;
					mMasterShimmer = (infoMemContents[mInfoMemLayout.idxSDExperimentConfig0] >> mInfoMemLayout.bitShiftMasterShimmer) & mInfoMemLayout.maskTimeMasterShimmer;
					mSingleTouch = (infoMemContents[mInfoMemLayout.idxSDExperimentConfig1] >> mInfoMemLayout.bitShiftSingleTouch) & mInfoMemLayout.maskTimeSingleTouch;
					mTCXO = (infoMemContents[mInfoMemLayout.idxSDExperimentConfig1] >> mInfoMemLayout.bitShiftTCX0) & mInfoMemLayout.maskTimeTCX0;
					
					mSyncBroadcastInterval = (int)(infoMemContents[mInfoMemLayout.idxSDBTInterval] & 0xFF);
					
					// Maximum and Estimated Length in minutes
					mTrialDurationEstimated =  ((int)(infoMemContents[mInfoMemLayout.idxEstimatedExpLengthLsb] & 0xFF) + (((int)(infoMemContents[mInfoMemLayout.idxEstimatedExpLengthMsb] & 0xFF)) << 8));
					mTrialDurationMaximum =  ((int)(infoMemContents[mInfoMemLayout.idxMaxExpLengthLsb] & 0xFF) + (((int)(infoMemContents[mInfoMemLayout.idxMaxExpLengthMsb] & 0xFF)) << 8));
				}
					
				byte[] macIdBytes = new byte[mInfoMemLayout.lengthMacIdBytes];
				System.arraycopy(infoMemContents, mInfoMemLayout.idxMacAddress, macIdBytes, 0 , mInfoMemLayout.lengthMacIdBytes);
				mMacIdFromInfoMem = UtilShimmer.bytesToHexString(macIdBytes);
				

				if(((infoMemContents[mInfoMemLayout.idxSDConfigDelayFlag]>>mInfoMemLayout.bitShiftSDCfgFileWriteFlag)&mInfoMemLayout.maskSDCfgFileWriteFlag) == mInfoMemLayout.maskSDCfgFileWriteFlag) {
					mConfigFileCreationFlag = true;
				}
				else {
					mConfigFileCreationFlag = false;
				}
				if(((infoMemContents[mInfoMemLayout.idxSDConfigDelayFlag]>>mInfoMemLayout.bitShiftSDCalibFileWriteFlag)&mInfoMemLayout.maskSDCalibFileWriteFlag) == mInfoMemLayout.maskSDCalibFileWriteFlag) {
					mCalibFileCreationFlag = true;
				}
				else {
					mCalibFileCreationFlag = false;
				}

				// InfoMem C - End
					
				if(mFirmwareIdentifier==FW_ID.SHIMMER3.SDLOG) {
					// InfoMem B Start -> Slave MAC ID for Multi-Shimmer Syncronisation
					syncNodesList.clear();
					for (int i = 0; i < mInfoMemLayout.maxNumOfExperimentNodes; i++) {
						System.arraycopy(infoMemContents, mInfoMemLayout.idxNode0 + (i*mInfoMemLayout.lengthMacIdBytes), macIdBytes, 0 , mInfoMemLayout.lengthMacIdBytes);
						if((Arrays.equals(macIdBytes, mInfoMemLayout.invalidMacId))||(Arrays.equals(macIdBytes, mInfoMemLayout.invalidMacId))) {
//						if(Arrays.equals(macIdBytes, new byte[]{-1,-1,-1,-1,-1,-1})) {
							break;
						}
						else {
							syncNodesList.add(UtilShimmer.bytesToHexString(macIdBytes));
						}
					}
					// InfoMem B End
				}
//			}
			
			//TODO Complete and tidy below
			sensorAndConfigMapsCreate();
			sensorMapUpdateFromEnabledSensorsVars();
			
//			sensorMapCheckandCorrectSensorDependencies();
		}
		
		// Set name if nothing was read from InfoMem
		if(!shimmerName.isEmpty()) {
			mShimmerUserAssignedName = new String(shimmerName);
			
		}
		else {
			mShimmerUserAssignedName = DEFAULT_SHIMMER_NAME + "_" + getMacIdFromUartParsed();
			
//			if(!mMacIdFromUartParsed.isEmpty()) {
//				mShimmerUserAssignedName += "_" + getMacIdFromUartParsed();
//			}
		}
		
	}
	
	/**
	 * Generate the Shimmer's Information Memory byte array based on the
	 * settings stored in ShimmerObject. These bytes can then be written to the
	 * Shimmer via the Shimmer Dock/Consensys Base. The Information Memory is is
	 * a region of the Shimmer's inbuilt RAM where all configuration information
	 * is stored.
	 * 
	 * @param generateForWritingToShimmer
	 * @return
	 */
	protected byte[] infoMemByteArrayGenerate(boolean generateForWritingToShimmer) {

		InfoMemLayout mInfoMemLayout = new InfoMemLayout(mFirmwareIdentifier, mFirmwareVersionMajor, mFirmwareVersionMinor, mFirmwareVersionInternal);
		
		byte[] infoMemBackup = mInfoMemBytes.clone();
		
		mInfoMemBytes = new byte[mInfoMemLayout.mInfoMemSize];
		
		// InfoMem defaults to 0xFF on firmware flash
		mInfoMemBytes = createEmptyInfoMemByteArray(mInfoMemBytes.length);
//		for(int i =0; i < mInfoMemBytes.length; i++) {
//			mInfoMemBytes[i] = (byte) 0xFF;
//		}
		
		// If not being generated from scratch then copy across exisiting InfoMem contents
		if(!generateForWritingToShimmer) {
			System.arraycopy(infoMemBackup, 0, mInfoMemBytes, 0, (infoMemBackup.length > mInfoMemBytes.length) ? mInfoMemBytes.length:infoMemBackup.length);
		}	
		
		// InfoMem D - Start - used by BtStream, SdLog and LogAndStream
		// Sampling Rate
		int samplingRate = (int)(32768 / mShimmerSamplingRate);
		mInfoMemBytes[mInfoMemLayout.idxShimmerSamplingRate] = (byte) (samplingRate & mInfoMemLayout.maskShimmerSamplingRate); 
		mInfoMemBytes[mInfoMemLayout.idxShimmerSamplingRate+1] = (byte) ((samplingRate >> 8) & mInfoMemLayout.maskShimmerSamplingRate); 

		//FW not using this feature and BtStream will reject infomem if this isn't set to '1'
		mInfoMemBytes[mInfoMemLayout.idxBufferSize] = (byte) 1;//(byte) (mBufferSize & mInfoMemLayout.maskBufferSize); 
		
		// Sensors
		//JC: The updateEnabledSensorsFromExgResolution(), seems to be working incorrectly because of the boolean values of mIsExg1_24bitEnabled, so updating this values first 
		checkExgResolutionFromEnabledSensorsVar();
		refreshEnabledSensorsFromSensorMap();
		mInfoMemBytes[mInfoMemLayout.idxSensors0] = (byte) ((mEnabledSensors >> mInfoMemLayout.byteShiftSensors0) & mInfoMemLayout.maskSensors);
		mInfoMemBytes[mInfoMemLayout.idxSensors1] = (byte) ((mEnabledSensors >> mInfoMemLayout.byteShiftSensors1) & mInfoMemLayout.maskSensors);
		mInfoMemBytes[mInfoMemLayout.idxSensors2] = (byte) ((mEnabledSensors >> mInfoMemLayout.byteShiftSensors2) & mInfoMemLayout.maskSensors);
//		setDefaultConfigForDisabledSensors();
		
		// Configuration
		mInfoMemBytes[mInfoMemLayout.idxConfigSetupByte0] = (byte) ((mLSM303DigitalAccelRate & mInfoMemLayout.maskLSM303DLHCAccelSamplingRate) << mInfoMemLayout.bitShiftLSM303DLHCAccelSamplingRate);
		mInfoMemBytes[mInfoMemLayout.idxConfigSetupByte0] |= (byte) ((mAccelRange & mInfoMemLayout.maskLSM303DLHCAccelRange) << mInfoMemLayout.bitShiftLSM303DLHCAccelRange);
		if(mLowPowerAccelWR) {
			mInfoMemBytes[mInfoMemLayout.idxConfigSetupByte0] |= (mInfoMemLayout.maskLSM303DLHCAccelLPM << mInfoMemLayout.bitShiftLSM303DLHCAccelLPM);
		}
		if(mHighResAccelWR) {
			mInfoMemBytes[mInfoMemLayout.idxConfigSetupByte0] |= (mInfoMemLayout.maskLSM303DLHCAccelHRM << mInfoMemLayout.bitShiftLSM303DLHCAccelHRM);
		}

		mInfoMemBytes[mInfoMemLayout.idxConfigSetupByte1] = (byte) ((mMPU9150GyroAccelRate & mInfoMemLayout.maskMPU9150AccelGyroSamplingRate) << mInfoMemLayout.bitShiftMPU9150AccelGyroSamplingRate);

		mInfoMemBytes[mInfoMemLayout.idxConfigSetupByte2] = (byte) ((mMagRange & mInfoMemLayout.maskLSM303DLHCMagRange) << mInfoMemLayout.bitShiftLSM303DLHCMagRange);
		mInfoMemBytes[mInfoMemLayout.idxConfigSetupByte2] |= (byte) ((mLSM303MagRate & mInfoMemLayout.maskLSM303DLHCMagSamplingRate) << mInfoMemLayout.bitShiftLSM303DLHCMagSamplingRate);
		mInfoMemBytes[mInfoMemLayout.idxConfigSetupByte2] |= (byte) ((mGyroRange & mInfoMemLayout.maskMPU9150GyroRange) << mInfoMemLayout.bitShiftMPU9150GyroRange);
		
		mInfoMemBytes[mInfoMemLayout.idxConfigSetupByte3] = (byte) ((mMPU9150AccelRange & mInfoMemLayout.maskMPU9150AccelRange) << mInfoMemLayout.bitShiftMPU9150AccelRange);
		mInfoMemBytes[mInfoMemLayout.idxConfigSetupByte3] |= (byte) ((mPressureResolution & mInfoMemLayout.maskBMP180PressureResolution) << mInfoMemLayout.bitShiftBMP180PressureResolution);
		mInfoMemBytes[mInfoMemLayout.idxConfigSetupByte3] |= (byte) ((mGSRRange & mInfoMemLayout.maskGSRRange) << mInfoMemLayout.bitShiftGSRRange);
		mInfoMemBytes[mInfoMemLayout.idxConfigSetupByte3] |= (byte) ((mInternalExpPower & mInfoMemLayout.maskEXPPowerEnable) << mInfoMemLayout.bitShiftEXPPowerEnable);
		
		//EXG Configuration
		exgBytesGetFromConfig(); //update mEXG1Register and mEXG2Register
		System.arraycopy(mEXG1RegisterArray, 0, mInfoMemBytes, mInfoMemLayout.idxEXGADS1292RChip1Config1, 10);
		System.arraycopy(mEXG2RegisterArray, 0, mInfoMemBytes, mInfoMemLayout.idxEXGADS1292RChip2Config1, 10);
		
		mInfoMemBytes[mInfoMemLayout.idxBtCommBaudRate] = (byte) (mBluetoothBaudRate & mInfoMemLayout.maskBaudRate);

		// Analog Accel Calibration Parameters
		byte[] bufferCalibrationParameters = new byte[mInfoMemLayout.lengthGeneralCalibrationBytes];
		// offsetVector -> buffer offset = 0
		for (int i=0; i<3; i++) {
			bufferCalibrationParameters[0+(i*2)] = (byte) ((((int)mOffsetVectorAnalogAccel[i][0]) >> 8) & 0xFF);
			bufferCalibrationParameters[0+(i*2)+1] = (byte) ((((int)mOffsetVectorAnalogAccel[i][0]) >> 0) & 0xFF);
		}
		// sensitivityMatrix -> buffer offset = 6
		for (int i=0; i<3; i++) {
			bufferCalibrationParameters[6+(i*2)] = (byte) ((((int)mSensitivityMatrixAnalogAccel[i][i]) >> 8) & 0xFF);
			bufferCalibrationParameters[6+(i*2)+1] = (byte) ((((int)mSensitivityMatrixAnalogAccel[i][i]) >> 0) & 0xFF);
		}
		// alignmentMatrix -> buffer offset = 12
		for (int i=0; i<3; i++) {
			bufferCalibrationParameters[12+(i*3)] = (byte) (((int)(mAlignmentMatrixAnalogAccel[i][0]*100)) & 0xFF);
			bufferCalibrationParameters[12+(i*3)+1] = (byte) (((int)(mAlignmentMatrixAnalogAccel[i][1]*100)) & 0xFF);
			bufferCalibrationParameters[12+(i*3)+2] = (byte) (((int)(mAlignmentMatrixAnalogAccel[i][2]*100)) & 0xFF);
		}
		System.arraycopy(bufferCalibrationParameters, 0, mInfoMemBytes, mInfoMemLayout.idxAnalogAccelCalibration, mInfoMemLayout.lengthGeneralCalibrationBytes);

		// MPU9150 Gyroscope Calibration Parameters
		bufferCalibrationParameters = new byte[mInfoMemLayout.lengthGeneralCalibrationBytes];
		// offsetVector -> buffer offset = 0
		for (int i=0; i<3; i++) {
			bufferCalibrationParameters[0+(i*2)] = (byte) ((((int)mOffsetVectorGyroscope[i][0]) >> 8) & 0xFF);
			bufferCalibrationParameters[0+(i*2)+1] = (byte) ((((int)mOffsetVectorGyroscope[i][0]) >> 0) & 0xFF);
		}
		// sensitivityMatrix -> buffer offset = 6
		for (int i=0; i<3; i++) {
			bufferCalibrationParameters[6+(i*2)] = (byte) ((((int)mSensitivityMatrixGyroscope[i][i]) >> 8) & 0xFF);
			bufferCalibrationParameters[6+(i*2)+1] = (byte) ((((int)mSensitivityMatrixGyroscope[i][i]) >> 0) & 0xFF);
		}
		// alignmentMatrix -> buffer offset = 12
		for (int i=0; i<3; i++) {
			bufferCalibrationParameters[12+(i*3)] = (byte) (((int)(mAlignmentMatrixGyroscope[i][0]*100)) & 0xFF);
			bufferCalibrationParameters[12+(i*3)+1] = (byte) (((int)(mAlignmentMatrixGyroscope[i][1]*100)) & 0xFF);
			bufferCalibrationParameters[12+(i*3)+2] = (byte) (((int)(mAlignmentMatrixGyroscope[i][2]*100)) & 0xFF);
		}
		System.arraycopy(bufferCalibrationParameters, 0, mInfoMemBytes, mInfoMemLayout.idxMPU9150GyroCalibration, mInfoMemLayout.lengthGeneralCalibrationBytes);

		// LSM303DLHC Magnetometer Calibration Parameters
		bufferCalibrationParameters = new byte[mInfoMemLayout.lengthGeneralCalibrationBytes];
		// offsetVector -> buffer offset = 0
		for (int i=0; i<3; i++) {
			bufferCalibrationParameters[0+(i*2)] = (byte) ((((int)mOffsetVectorMagnetometer[i][0]) >> 8) & 0xFF);
			bufferCalibrationParameters[0+(i*2)+1] = (byte) ((((int)mOffsetVectorMagnetometer[i][0]) >> 0) & 0xFF);
		}
		// sensitivityMatrix -> buffer offset = 6
		for (int i=0; i<3; i++) {
			bufferCalibrationParameters[6+(i*2)] = (byte) ((((int)mSensitivityMatrixMagnetometer[i][i]) >> 8) & 0xFF);
			bufferCalibrationParameters[6+(i*2)+1] = (byte) ((((int)mSensitivityMatrixMagnetometer[i][i]) >> 0) & 0xFF);
		}
		// alignmentMatrix -> buffer offset = 12
		for (int i=0; i<3; i++) {
			bufferCalibrationParameters[12+(i*3)] = (byte) (((int)(mAlignmentMatrixMagnetometer[i][0]*100)) & 0xFF);
			bufferCalibrationParameters[12+(i*3)+1] = (byte) (((int)(mAlignmentMatrixMagnetometer[i][1]*100)) & 0xFF);
			bufferCalibrationParameters[12+(i*3)+2] = (byte) (((int)(mAlignmentMatrixMagnetometer[i][2]*100)) & 0xFF);
		}
		System.arraycopy(bufferCalibrationParameters, 0, mInfoMemBytes, mInfoMemLayout.idxLSM303DLHCMagCalibration, mInfoMemLayout.lengthGeneralCalibrationBytes);

		// LSM303DLHC Digital Accel Calibration Parameters
		bufferCalibrationParameters = generateCalParamLSM303DLHCAccel();
		System.arraycopy(bufferCalibrationParameters, 0, mInfoMemBytes, mInfoMemLayout.idxLSM303DLHCAccelCalibration, mInfoMemLayout.lengthGeneralCalibrationBytes);
		
		//TODO: decide what to do
		// BMP180 Pressure Calibration Parameters

		// Derived Sensors
		if((mInfoMemLayout.idxDerivedSensors0>0)&&(mInfoMemLayout.idxDerivedSensors1>0)) { // Check if compatible
			mInfoMemBytes[mInfoMemLayout.idxDerivedSensors0] = (byte) ((mDerivedSensors >> mInfoMemLayout.byteShiftDerivedSensors0) & mInfoMemLayout.maskDerivedChannelsByte);
			mInfoMemBytes[mInfoMemLayout.idxDerivedSensors1] = (byte) ((mDerivedSensors >> mInfoMemLayout.byteShiftDerivedSensors1) & mInfoMemLayout.maskDerivedChannelsByte);
			if(mInfoMemLayout.idxDerivedSensors2>0) { // Check if compatible
				mInfoMemBytes[mInfoMemLayout.idxDerivedSensors2] = (byte) ((mDerivedSensors >> mInfoMemLayout.byteShiftDerivedSensors2) & mInfoMemLayout.maskDerivedChannelsByte);
			}
		}
		
//		// Derived Sensors
//		mDerivedSensors = (long)0;
//		// Check if compatible
//		if((mInfoMemLayout.idxDerivedSensors0>0)&&(mInfoMemLayout.idxDerivedSensors1>0)) {
//			for(Integer key:mSensorMap.keySet()) {
//				if(mSensorMap.get(key).mIsEnabled) {
//					mDerivedSensors |= mSensorMap.get(key).mDerivedSensorBitmapID;
//				}
//			}
//			
//			/*
//			 * Infomem layout and values for derived channels (added by RM for debug)
//			 * 
//			mInfoMemLayout.idxDerivedSensors0 = 31
//			mInfoMemLayout.idxDerivedSensors1 = 32	
//			mInfoMemLayout.idxDerivedSensors2 = 33	
//			
//			mDerivedSensors = 1 (Resistance Amp - BAMP)
//			mDerivedSensors = 2 (Skin Temp - BAMP)
//			mDerivedSensors = 4 (PPG - GSR+)
//			mDerivedSensors = 8 (PPG1 - P3D)
//			mDerivedSensors = 16 (PPG2 - P3D)
//			
//			byteShiftDerivedSensors0 = 0
//			byteShiftDerivedSensors1 = 8
//			byteShiftDerivedSensors2 = 16
//			
// 			mInfoMemLayout.maskDerivedChannelsByte = 0xFF
// 			
// 			*/
//			
//			mInfoMemBytes[mInfoMemLayout.idxDerivedSensors0] = (byte) ((mDerivedSensors >> mInfoMemLayout.byteShiftDerivedSensors0) & mInfoMemLayout.maskDerivedChannelsByte);
//			mInfoMemBytes[mInfoMemLayout.idxDerivedSensors1] = (byte) ((mDerivedSensors >> mInfoMemLayout.byteShiftDerivedSensors1) & mInfoMemLayout.maskDerivedChannelsByte);
//			if(mInfoMemLayout.idxDerivedSensors2>=0) { // Check if compatible
//				mInfoMemBytes[mInfoMemLayout.idxDerivedSensors2] = (byte) ((mDerivedSensors >> mInfoMemLayout.byteShiftDerivedSensors2) & mInfoMemLayout.maskDerivedChannelsByte);
//			}
//
//		}
		
		// InfoMem D - End

		
		//TODO: Add full FW version checking here to support future changes to FW
		//SDLog and LogAndStream
//		if(((mFirmwareIdentifier==FW_ID.SHIMMER3.LOGANDSTREAM)||(mFirmwareIdentifier==FW_ID.SHIMMER3.SDLOG))&&(mInfoMemBytes.length >=384)) {

			// InfoMem C - Start - used by SdLog and LogAndStream
			if(mFirmwareIdentifier==FW_ID.SHIMMER3.SDLOG) {
				mInfoMemBytes[mInfoMemLayout.idxConfigSetupByte4] = (byte) ((mMPU9150DMP & mInfoMemLayout.maskMPU9150DMP) << mInfoMemLayout.bitShiftMPU9150DMP);
				mInfoMemBytes[mInfoMemLayout.idxConfigSetupByte4] |= (byte) ((mMPU9150LPF & mInfoMemLayout.maskMPU9150LPF) << mInfoMemLayout.bitShiftMPU9150LPF);
				mInfoMemBytes[mInfoMemLayout.idxConfigSetupByte4] |= (byte) ((mMPU9150MotCalCfg & mInfoMemLayout.maskMPU9150MotCalCfg) << mInfoMemLayout.bitShiftMPU9150MotCalCfg);

				mInfoMemBytes[mInfoMemLayout.idxConfigSetupByte5] = (byte) ((mMPU9150MPLSamplingRate & mInfoMemLayout.maskMPU9150MPLSamplingRate) << mInfoMemLayout.bitShiftMPU9150MPLSamplingRate);
				mInfoMemBytes[mInfoMemLayout.idxConfigSetupByte5] |= (byte) ((mMPU9150MagSamplingRate & mInfoMemLayout.maskMPU9150MPLSamplingRate) << mInfoMemLayout.bitShiftMPU9150MagSamplingRate);

				mInfoMemBytes[mInfoMemLayout.idxSensors3] = (byte) ((mEnabledSensors >> mInfoMemLayout.bitShiftSensors3) & 0xFF);
				mInfoMemBytes[mInfoMemLayout.idxSensors4] = (byte) ((mEnabledSensors >> mInfoMemLayout.bitShiftSensors4) & 0xFF);

				mInfoMemBytes[mInfoMemLayout.idxConfigSetupByte6] = (byte) ((mMPLSensorFusion & mInfoMemLayout.maskMPLSensorFusion) << mInfoMemLayout.bitShiftMPLSensorFusion);
				mInfoMemBytes[mInfoMemLayout.idxConfigSetupByte6] |= (byte) ((mMPLGyroCalTC & mInfoMemLayout.maskMPLGyroCalTC) << mInfoMemLayout.bitShiftMPLGyroCalTC);
				mInfoMemBytes[mInfoMemLayout.idxConfigSetupByte6] |= (byte) ((mMPLVectCompCal & mInfoMemLayout.maskMPLVectCompCal) << mInfoMemLayout.bitShiftMPLVectCompCal);
				mInfoMemBytes[mInfoMemLayout.idxConfigSetupByte6] |= (byte) ((mMPLMagDistCal & mInfoMemLayout.maskMPLMagDistCal) << mInfoMemLayout.bitShiftMPLMagDistCal);
				mInfoMemBytes[mInfoMemLayout.idxConfigSetupByte6] |= (byte) ((mMPLEnable & mInfoMemLayout.maskMPLEnable) << mInfoMemLayout.bitShiftMPLEnable);
				
				//TODO: decide what to do
				//MPL Accel Calibration Parameters
				//MPL Mag Calibration Configuration
				//MPL Gyro Calibration Configuration
			}

			// Shimmer Name
			for (int i = 0; i < mInfoMemLayout.lengthShimmerName; i++) {
				if (i < mShimmerUserAssignedName.length()) {
					mInfoMemBytes[mInfoMemLayout.idxSDShimmerName + i] = (byte) mShimmerUserAssignedName.charAt(i);
				}
				else {
					mInfoMemBytes[mInfoMemLayout.idxSDShimmerName + i] = (byte) 0xFF;
				}
			}
			
			// Experiment Name
			for (int i = 0; i < mInfoMemLayout.lengthExperimentName; i++) {
				if (i < mTrialName.length()) {
					mInfoMemBytes[mInfoMemLayout.idxSDEXPIDName + i] = (byte) mTrialName.charAt(i);
				}
				else {
					mInfoMemBytes[mInfoMemLayout.idxSDEXPIDName + i] = (byte) 0xFF;
				}
			}

			//Configuration Time
			mInfoMemBytes[mInfoMemLayout.idxSDConfigTime0] = (byte) ((mConfigTime >> mInfoMemLayout.bitShiftSDConfigTime0) & 0xFF);
			mInfoMemBytes[mInfoMemLayout.idxSDConfigTime1] = (byte) ((mConfigTime >> mInfoMemLayout.bitShiftSDConfigTime1) & 0xFF);
			mInfoMemBytes[mInfoMemLayout.idxSDConfigTime2] = (byte) ((mConfigTime >> mInfoMemLayout.bitShiftSDConfigTime2) & 0xFF);
			mInfoMemBytes[mInfoMemLayout.idxSDConfigTime3] = (byte) ((mConfigTime >> mInfoMemLayout.bitShiftSDConfigTime3) & 0xFF);
			
			if(mFirmwareIdentifier==FW_ID.SHIMMER3.SDLOG) {
				mInfoMemBytes[mInfoMemLayout.idxSDMyTrialID] = (byte) (mTrialId & 0xFF);
	
				mInfoMemBytes[mInfoMemLayout.idxSDNumOfShimmers] = (byte) (mTrialNumberOfShimmers & 0xFF);
			}
			
			if((mFirmwareIdentifier==FW_ID.SHIMMER3.SDLOG)||(mFirmwareIdentifier==FW_ID.SHIMMER3.LOGANDSTREAM)) {
				mInfoMemBytes[mInfoMemLayout.idxSDExperimentConfig0] = (byte) ((mButtonStart & mInfoMemLayout.maskButtonStart) << mInfoMemLayout.bitShiftButtonStart);
				if(this.isOverrideShowRwcErrorLeds){
					mInfoMemBytes[mInfoMemLayout.idxSDExperimentConfig0] |= (byte) ((mInfoMemLayout.maskShowRwcErrorLeds) << mInfoMemLayout.bitShiftShowRwcErrorLeds);
				}
				else {
					mInfoMemBytes[mInfoMemLayout.idxSDExperimentConfig0] |= (byte) ((mShowRtcErrorLeds & mInfoMemLayout.maskShowRwcErrorLeds) << mInfoMemLayout.bitShiftShowRwcErrorLeds);
				}
			}
			
			if(mFirmwareIdentifier==FW_ID.SHIMMER3.SDLOG) {
				mInfoMemBytes[mInfoMemLayout.idxSDExperimentConfig0] |= (byte) ((mSyncWhenLogging & mInfoMemLayout.maskTimeSyncWhenLogging) << mInfoMemLayout.bitShiftTimeSyncWhenLogging);
				mInfoMemBytes[mInfoMemLayout.idxSDExperimentConfig0] |= (byte) ((mMasterShimmer & mInfoMemLayout.maskTimeMasterShimmer) << mInfoMemLayout.bitShiftMasterShimmer);
				
				mInfoMemBytes[mInfoMemLayout.idxSDExperimentConfig1] = (byte) ((mSingleTouch & mInfoMemLayout.maskTimeSingleTouch) << mInfoMemLayout.bitShiftSingleTouch);
				mInfoMemBytes[mInfoMemLayout.idxSDExperimentConfig1] |= (byte) ((mTCXO & mInfoMemLayout.maskTimeTCX0) << mInfoMemLayout.bitShiftTCX0);
			
				mInfoMemBytes[mInfoMemLayout.idxSDBTInterval] = (byte) (mSyncBroadcastInterval & 0xFF);
			
				// Maximum and Estimated Length in minutes
				mInfoMemBytes[mInfoMemLayout.idxEstimatedExpLengthLsb] = (byte) ((mTrialDurationEstimated >> 0) & 0xFF);
				mInfoMemBytes[mInfoMemLayout.idxEstimatedExpLengthMsb] = (byte) ((mTrialDurationEstimated >> 8) & 0xFF);
				mInfoMemBytes[mInfoMemLayout.idxMaxExpLengthLsb] = (byte) ((mTrialDurationMaximum >> 0) & 0xFF);
				mInfoMemBytes[mInfoMemLayout.idxMaxExpLengthMsb] = (byte) ((mTrialDurationMaximum >> 8) & 0xFF);
			}
			
			if(((mFirmwareIdentifier==FW_ID.SHIMMER3.LOGANDSTREAM)||(mFirmwareIdentifier==FW_ID.SHIMMER3.SDLOG))) {
				if(generateForWritingToShimmer) {
					// MAC address - set to all 0xFF (i.e. invalid MAC) so that Firmware will know to check for MAC from Bluetooth transceiver
					// (already set to 0xFF at start of method but just incase)
					System.arraycopy(mInfoMemLayout.invalidMacId, 0, mInfoMemBytes, mInfoMemLayout.idxMacAddress, mInfoMemLayout.lengthMacIdBytes);
	
					mInfoMemBytes[mInfoMemLayout.idxSDConfigDelayFlag] = 0;
					// Tells the Shimmer to create a new config file on undock/power cycle
					//TODO RM enabled the two lines below (MN had the below two lines commented out.. but need them to write config successfully over UART)
					byte configFileWriteBit = (byte) (mConfigFileCreationFlag? (mInfoMemLayout.maskSDCfgFileWriteFlag << mInfoMemLayout.bitShiftSDCfgFileWriteFlag):0x00);
					mInfoMemBytes[mInfoMemLayout.idxSDConfigDelayFlag] |= configFileWriteBit;

					mInfoMemBytes[mInfoMemLayout.idxSDConfigDelayFlag] |= mInfoMemLayout.bitShiftSDCfgFileWriteFlag;

					 // Tells the Shimmer to create a new calibration files on undock/power cycle
					byte calibFileWriteBit = (byte) (mCalibFileCreationFlag? (mInfoMemLayout.maskSDCalibFileWriteFlag << mInfoMemLayout.bitShiftSDCalibFileWriteFlag):0x00);
					mInfoMemBytes[mInfoMemLayout.idxSDConfigDelayFlag] |= calibFileWriteBit;
				}
			}
			// InfoMem C - End
				
			if(mFirmwareIdentifier==FW_ID.SHIMMER3.SDLOG) {
				// InfoMem B Start -> Slave MAC ID for Multi-Shimmer Syncronisation
				for (int i = 0; i < mInfoMemLayout.maxNumOfExperimentNodes; i++) { // Limit of 21 nodes
					byte[] macIdArray;
					if((syncNodesList.size()>0) && (i<syncNodesList.size()) && (mSyncWhenLogging>0)) {
						macIdArray = UtilShimmer.hexStringToByteArray(syncNodesList.get(i));
					}
					else {
						macIdArray = mInfoMemLayout.invalidMacId;
					}
					System.arraycopy(macIdArray, 0, mInfoMemBytes, mInfoMemLayout.idxNode0 + (i*mInfoMemLayout.lengthMacIdBytes), mInfoMemLayout.lengthMacIdBytes);
				}
				// InfoMem B End
			}
			
//		}
		return mInfoMemBytes;
	}
	
	/**
	 * Creates an empty byte array for the purposes of generating the
	 * configuration bytes to write to the Shimmer (default all bytes = 0xFF).
	 * 
	 * @param size the size of the byte array to create.
	 * @return byte array
	 */
	public byte[] createEmptyInfoMemByteArray(int size) {
		byte[] newArray = new byte[size];
		for(byte b:newArray) {
			b = (byte)0xFF;
		}
		return newArray;
	}
	

	/**
	 * @return a refreshed version of the current mShimmerInfoMemBytes
	 */
	public byte[] refreshShimmerInfoMemBytes() {
//		System.out.println("SlotDetails:" + this.mUniqueIdentifier + " " + mShimmerInfoMemBytes[3]);
		return infoMemByteArrayGenerate(false);
	}
	
	public void setCalibFileCreationFlag(boolean state) {
		mCalibFileCreationFlag = state;
	}
	
	public void setConfigFileCreationFlag(boolean state) {
		mConfigFileCreationFlag = state;
	}
	
	public void refreshEnabledSensorsFromSensorMap(){
		if(mSensorMap!=null) {
			if (mHardwareVersion == HW_ID.SHIMMER_3){
				mEnabledSensors = (long)0;
				mDerivedSensors = (long)0;
				sensorMapCheckandCorrectHwDependencies();
				for(SensorEnabledDetails sED:mSensorMap.values()) {
					if(sED.mIsEnabled) {
						mEnabledSensors |= sED.mSensorDetails.mSensorBitmapIDSDLogHeader;
						
						if(sED.isDerivedChannel()){
							mDerivedSensors |= sED.mDerivedSensorBitmapID;
						}
					}
				}
				updateEnabledSensorsFromExgResolution();
			}
		}
	}
	

	/**
	 * Converts the LSM303DLHC Accel calibration variables from Shimmer Object
	 * into a byte array for sending to the Shimmer.
	 * 
	 * @return the bytes array containing the LSM303DLHC Accel calibration
	 */
	public byte[] generateCalParamLSM303DLHCAccel(){
		byte[] bufferCalibrationParameters = new byte[21];
		// offsetVector -> buffer offset = 0
		for (int i=0; i<3; i++) {
			bufferCalibrationParameters[0+(i*2)] = (byte) ((((int)mOffsetVectorWRAccel[i][0]) >> 8) & 0xFF);
			bufferCalibrationParameters[0+(i*2)+1] = (byte) ((((int)mOffsetVectorWRAccel[i][0]) >> 0) & 0xFF);
		}
		// sensitivityMatrix -> buffer offset = 6
		for (int i=0; i<3; i++) {
			bufferCalibrationParameters[6+(i*2)] = (byte) ((((int)mSensitivityMatrixWRAccel[i][i]) >> 8) & 0xFF);
			bufferCalibrationParameters[6+(i*2)+1] = (byte) ((((int)mSensitivityMatrixWRAccel[i][i]) >> 0) & 0xFF);
		}
		// alignmentMatrix -> buffer offset = 12
		for (int i=0; i<3; i++) {
			bufferCalibrationParameters[12+(i*3)] = (byte) (((int)(mAlignmentMatrixWRAccel[i][0]*100)) & 0xFF);
			bufferCalibrationParameters[12+(i*3)+1] = (byte) (((int)(mAlignmentMatrixWRAccel[i][1]*100)) & 0xFF);
			bufferCalibrationParameters[12+(i*3)+2] = (byte) (((int)(mAlignmentMatrixWRAccel[i][2]*100)) & 0xFF);
		}
		return bufferCalibrationParameters;
	}

	/**
	 * This method is used as the basis for configuring a Shimmer through the
	 * Consensys application. All sensor and configuration information is stored
	 * in dynamically created and controlled Maps.
	 * 
	 * Should only be used after the Shimmer HW and FW version information is
	 * set
	 */
	public void sensorAndConfigMapsCreate() {
		
		mSensorMap = new LinkedHashMap<Integer, SensorEnabledDetails>();
		mChannelMap = new LinkedHashMap<String, ChannelDetails>();
		mAlgorithmChannelsMap = new LinkedHashMap<String, AlgorithmDetailsNew>();
		mAlgorithmGroupingMap = new LinkedHashMap<String, List<String>>();
		mSensorGroupingMap = new LinkedHashMap<String,SensorGroupingDetails>();
		mConfigOptionsMap = new HashMap<String,SensorConfigOptionDetails>();
		
		if (mHardwareVersion != -1){
			if (mHardwareVersion == HW_ID.SHIMMER_2R){
				Map<Integer,SensorDetails> sensorMapRef = Configuration.Shimmer2.mSensorMapRef;
				for(Integer key:sensorMapRef.keySet()){
					mSensorMap.put(key, new SensorEnabledDetails(false, 0, sensorMapRef.get(key)));
				}
			} 
			else if (mHardwareVersion == HW_ID.SHIMMER_3) {
				createInfoMemLayoutObjectIfNeeded();

				Map<Integer,SensorDetails> sensorMapRef = Configuration.Shimmer3.mSensorMapRef;
				for(Integer key:sensorMapRef.keySet()){
					
					int derivedChannelBitmapID = 0;
					if(key==Configuration.Shimmer3.SensorMapKey.RESISTANCE_AMP){
						derivedChannelBitmapID = mInfoMemLayout.maskDerivedChannelResAmp;
					}
					else if(key==Configuration.Shimmer3.SensorMapKey.SKIN_TEMPERATURE_PROBE){
						derivedChannelBitmapID = mInfoMemLayout.maskDerivedChannelSkinTemp;
					}
					else if(key==Configuration.Shimmer3.SensorMapKey.PPG_A12){
						derivedChannelBitmapID = mInfoMemLayout.maskDerivedChannelPpg_ADC12ADC13;
					}
					else if(key==Configuration.Shimmer3.SensorMapKey.PPG_A13){
						derivedChannelBitmapID = mInfoMemLayout.maskDerivedChannelPpg_ADC12ADC13;
					}
					else if(key==Configuration.Shimmer3.SensorMapKey.PPG1_A12){
						derivedChannelBitmapID = mInfoMemLayout.maskDerivedChannelPpg1_ADC12ADC13;
					}
					else if(key==Configuration.Shimmer3.SensorMapKey.PPG1_A13){
						derivedChannelBitmapID = mInfoMemLayout.maskDerivedChannelPpg1_ADC12ADC13;
					}
					else if(key==Configuration.Shimmer3.SensorMapKey.PPG2_A1){
						derivedChannelBitmapID = mInfoMemLayout.maskDerivedChannelPpg2_ADC1ADC14;
					}
					else if(key==Configuration.Shimmer3.SensorMapKey.PPG2_A14){
						derivedChannelBitmapID = mInfoMemLayout.maskDerivedChannelPpg2_ADC1ADC14;
					}
						
					mSensorMap.put(key, new SensorEnabledDetails(false, derivedChannelBitmapID, sensorMapRef.get(key)));
					
					
    				//Special cases for ExG 24-bit vs. 16-bit
					if((key==Configuration.Shimmer3.SensorMapKey.ECG)
							||(key==Configuration.Shimmer3.SensorMapKey.EMG)
							||(key==Configuration.Shimmer3.SensorMapKey.EXG_RESPIRATION)
							||(key==Configuration.Shimmer3.SensorMapKey.EXG_CUSTOM)
							||(key==Configuration.Shimmer3.SensorMapKey.EXG_TEST)){

//						List<Object> objs;
						Iterator<String> i = mSensorMap.get(key).mListOfChannels.iterator();
						while (i.hasNext()) {
						   String channelName = i.next();
						   
						   		//System.out.println("getExGResolution(): " +getExGResolution());
						   		
			    				if((getExGResolution()==1)
			    						&&((channelName.equals(Configuration.Shimmer3.ObjectClusterSensorName.ECG_LA_RA_16BIT))
			    					||(channelName.equals(Configuration.Shimmer3.ObjectClusterSensorName.ECG_LL_RA_16BIT))
			    					||(channelName.equals(Configuration.Shimmer3.ObjectClusterSensorName.ECG_VX_RL_16BIT))
				    				||(channelName.equals(Configuration.Shimmer3.ObjectClusterSensorName.ECG_RESP_16BIT))
			    					||(channelName.equals(Configuration.Shimmer3.ObjectClusterSensorName.EMG_CH1_16BIT))
			    					||(channelName.equals(Configuration.Shimmer3.ObjectClusterSensorName.EMG_CH2_16BIT))
			    					||(channelName.equals(Configuration.Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP1_CH1_16BIT))
			    					||(channelName.equals(Configuration.Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP1_CH2_16BIT))
			    					||(channelName.equals(Configuration.Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP2_CH1_16BIT))
			    					||(channelName.equals(Configuration.Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP2_CH2_16BIT))
			    					||(channelName.equals(Configuration.Shimmer3.ObjectClusterSensorName.ECG_LL_LA_16BIT)))){
									    i.remove();
								}
			    				else if((getExGResolution()==0)
			    						&&((channelName.equals(Configuration.Shimmer3.ObjectClusterSensorName.ECG_LA_RA_24BIT))
				    					||(channelName.equals(Configuration.Shimmer3.ObjectClusterSensorName.ECG_LL_RA_24BIT))
				    					||(channelName.equals(Configuration.Shimmer3.ObjectClusterSensorName.ECG_VX_RL_24BIT))
				    					||(channelName.equals(Configuration.Shimmer3.ObjectClusterSensorName.ECG_RESP_24BIT))
				    					||(channelName.equals(Configuration.Shimmer3.ObjectClusterSensorName.EMG_CH1_24BIT))
				    					||(channelName.equals(Configuration.Shimmer3.ObjectClusterSensorName.EMG_CH2_24BIT))
				    					||(channelName.equals(Configuration.Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP1_CH1_24BIT))
				    					||(channelName.equals(Configuration.Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP1_CH2_24BIT))
				    					||(channelName.equals(Configuration.Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP2_CH1_24BIT))
				    					||(channelName.equals(Configuration.Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP2_CH2_24BIT))
				    					||(channelName.equals(Configuration.Shimmer3.ObjectClusterSensorName.ECG_LL_LA_24BIT)))){
								    i.remove();
								}

					   	}
					}
					
				}
				
				mChannelMap = Configuration.Shimmer3.mChannelMapRef;
				mAlgorithmChannelsMap = Configuration.Shimmer3.mAlgorithmChannelsMap;
				mAlgorithmGroupingMap = Configuration.Shimmer3.mAlgorithmGroupingMap;
				mSensorGroupingMap = Configuration.Shimmer3.mSensorGroupingMap;
				mConfigOptionsMap = Configuration.Shimmer3.mConfigOptionsMap;
				
				
				
//				// For loop to automatically inherit associated channel configuration options from mSensorMap in the aMap
//				for (String channelGroup : mSensorGroupingMap.keySet()) {
//					// Ok to clear here because variable is initiated in the class
//					mSensorGroupingMap.get(channelGroup).mListOfConfigOptionKeysAssociated.clear();
//					for (Integer sensor:mSensorGroupingMap.get(channelGroup).mListOfSensorMapKeysAssociated) {
//						if(mSensorMap.containsKey(sensor)){
//							List<String> associatedConfigOptions = mSensorMap.get(sensor).mSensorDetails.mListOfConfigOptionKeysAssociated;
//							if (associatedConfigOptions != null) {
//								for (String configOption : associatedConfigOptions) {
//									// do not add duplicates
//									if (!(mSensorGroupingMap.get(channelGroup).mListOfConfigOptionKeysAssociated.contains(configOption))) {
//										mSensorGroupingMap.get(channelGroup).mListOfConfigOptionKeysAssociated.add(configOption);
//									}
//								}
//							}
//						}
//					}
//				}
				

			}
			else if (mHardwareVersion == HW_ID.SHIMMER_GQ) {

//				// These can be used to enable/disble GUI options depending on what HW, FW, Expansion boards versions are present
//				ShimmerVerObject baseAnyIntExpBoardAndFw = new ShimmerVerObject(HW_ID.SHIMMER_GQ,FW_ID.SHIMMER3.GQ_GSR,ANY_VERSION,ANY_VERSION,ANY_VERSION,ANY_VERSION);
//				List<ShimmerVerObject> listOfCompatibleVersionInfoGq = Arrays.asList(baseAnyIntExpBoardAndFw);
//				
//				mSensorMapRef.put(Configuration.ShimmerGQ.SensorMapKey.VBATT, new SensorDetails(false, Configuration.ShimmerGQ.GuiLabelSensors.BATTERY));
//				mSensorMapRef.put(Configuration.ShimmerGQ.SensorMapKey.LSM303DLHC_ACCEL, new SensorDetails(false, Configuration.ShimmerGQ.GuiLabelSensors.ACCEL_WR));
//				mSensorMapRef.put(Configuration.ShimmerGQ.SensorMapKey.GSR, new SensorDetails(false, Configuration.ShimmerGQ.GuiLabelSensors.GSR));
//				mSensorMapRef.put(Configuration.ShimmerGQ.SensorMapKey.BEACON, new SensorDetails(false, Configuration.ShimmerGQ.GuiLabelSensors.BEACON));
//				mSensorMapRef.put(Configuration.ShimmerGQ.SensorMapKey.PPG, new SensorDetails(false, Configuration.ShimmerGQ.GuiLabelSensors.PPG));
//				
//				mSensorMapRef.get(Configuration.ShimmerGQ.SensorMapKey.VBATT).mListOfCompatibleVersionInfo = listOfCompatibleVersionInfoGq;
//				mSensorMapRef.get(Configuration.ShimmerGQ.SensorMapKey.LSM303DLHC_ACCEL).mListOfCompatibleVersionInfo = listOfCompatibleVersionInfoGq;
//				mSensorMapRef.get(Configuration.ShimmerGQ.SensorMapKey.GSR).mListOfCompatibleVersionInfo = listOfCompatibleVersionInfoGq;
//				mSensorMapRef.get(Configuration.ShimmerGQ.SensorMapKey.BEACON).mListOfCompatibleVersionInfo = listOfCompatibleVersionInfoGq;
//				mSensorMapRef.get(Configuration.ShimmerGQ.SensorMapKey.PPG).mListOfCompatibleVersionInfo = listOfCompatibleVersionInfoGq;
//
//				// Associated config options for each channel (currently used for the ChannelTileMap)
//				mSensorMapRef.get(Configuration.ShimmerGQ.SensorMapKey.VBATT).mListOfConfigOptionKeysAssociated = Arrays.asList(
//						Configuration.ShimmerGQ.GuiLabelConfig.SAMPLING_RATE_DIVIDER_VBATT);
//				mSensorMapRef.get(Configuration.ShimmerGQ.SensorMapKey.LSM303DLHC_ACCEL).mListOfConfigOptionKeysAssociated = Arrays.asList(
//						Configuration.ShimmerGQ.GuiLabelConfig.LSM303DLHC_ACCEL_RANGE,
//						Configuration.ShimmerGQ.GuiLabelConfig.LSM303DLHC_ACCEL_RATE,
//						Configuration.ShimmerGQ.GuiLabelConfig.LSM303DLHC_ACCEL_LPM,
//						Configuration.ShimmerGQ.GuiLabelConfig.SAMPLING_RATE_DIVIDER_LSM303DLHC_ACCEL);
//				mSensorMapRef.get(Configuration.ShimmerGQ.SensorMapKey.GSR).mListOfConfigOptionKeysAssociated = Arrays.asList(
//						Configuration.ShimmerGQ.GuiLabelConfig.GSR_RANGE,
//						Configuration.ShimmerGQ.GuiLabelConfig.SAMPLING_RATE_DIVIDER_GSR);
//				mSensorMapRef.get(Configuration.ShimmerGQ.SensorMapKey.BEACON).mListOfConfigOptionKeysAssociated = Arrays.asList(
//						Configuration.ShimmerGQ.GuiLabelConfig.SAMPLING_RATE_DIVIDER_BEACON);
//				mSensorMapRef.get(Configuration.ShimmerGQ.SensorMapKey.PPG).mListOfConfigOptionKeysAssociated = Arrays.asList(
//						Configuration.ShimmerGQ.GuiLabelConfig.SAMPLING_RATE_DIVIDER_PPG);
//
//				
//				//Sensor Grouping for Configuration Panel 'tile' generation. 
//				mSensorTileMap.put(Configuration.ShimmerGQ.GuiLabelSensorTiles.BATTERY_MONITORING, new SensorTileDetails(
//						Arrays.asList(Configuration.ShimmerGQ.SensorMapKey.VBATT)));
//				mSensorTileMap.put(Configuration.ShimmerGQ.GuiLabelSensorTiles.WIDE_RANGE_ACCEL, new SensorTileDetails(
//						Arrays.asList(Configuration.ShimmerGQ.SensorMapKey.LSM303DLHC_ACCEL)));
//				mSensorTileMap.put(Configuration.ShimmerGQ.GuiLabelSensorTiles.GSR, new SensorTileDetails(
//						Arrays.asList(Configuration.ShimmerGQ.SensorMapKey.GSR,
//									Configuration.ShimmerGQ.SensorMapKey.PPG)));
//				mSensorTileMap.put(Configuration.ShimmerGQ.GuiLabelSensorTiles.BEACON, new SensorTileDetails(
//						Arrays.asList(Configuration.ShimmerGQ.SensorMapKey.BEACON)));
//				
//				// ChannelTiles that have compatibility considerations (used to auto generate tiles in GUI)
//				mSensorTileMap.get(Configuration.ShimmerGQ.GuiLabelSensorTiles.BATTERY_MONITORING).mListOfCompatibleVersionInfo = listOfCompatibleVersionInfoGq;
//				mSensorTileMap.get(Configuration.ShimmerGQ.GuiLabelSensorTiles.WIDE_RANGE_ACCEL).mListOfCompatibleVersionInfo = listOfCompatibleVersionInfoGq;
//				mSensorTileMap.get(Configuration.ShimmerGQ.GuiLabelSensorTiles.GSR).mListOfCompatibleVersionInfo = listOfCompatibleVersionInfoGq;
//				mSensorTileMap.get(Configuration.ShimmerGQ.GuiLabelSensorTiles.BEACON).mListOfCompatibleVersionInfo = listOfCompatibleVersionInfoGq;
//				
//				// For loop to automatically inherit associated channel configuration options from ChannelMap in the ChannelTileMap
//				for (String channelGroup : mSensorTileMap.keySet()) {
//					// Ok to clear here because variable is initiated in the class
//					mSensorTileMap.get(channelGroup).mListOfConfigOptionKeysAssociated.clear();
//					for (Integer channel : mSensorTileMap.get(channelGroup).mListOfSensorMapKeysAssociated) {
//						if(mSensorMapRef.containsKey(channel)){
//							List<String> associatedConfigOptions = mSensorMapRef.get(channel).mListOfConfigOptionKeysAssociated;
//							if (associatedConfigOptions != null) {
//								for (String configOption : associatedConfigOptions) {
//									// do not add duplicates
//									if (!(mSensorTileMap.get(channelGroup).mListOfConfigOptionKeysAssociated.contains(configOption))) {
//										mSensorTileMap.get(channelGroup).mListOfConfigOptionKeysAssociated.add(configOption);
//									}
//								}
//							}
//						}
//					}
//				}
//				
//				// Assemble the channel configuration options map
//				mConfigOptionsMap = new HashMap<String,SensorConfigOptionDetails>();
//				
////				mConfigOptionsMap.put(Configuration.Shimmer3.GuiLabelConfig.SHIMMER_USER_ASSIGNED_NAME, 
////						new SensorConfigOptionDetails(SensorConfigOptionDetails.GUI_COMPONENT_TYPE.TEXTFIELD,
////								listOfCompatibleVersionInfoGq));
//				mConfigOptionsMap.put(Configuration.Shimmer3.GuiLabelConfig.SHIMMER_SAMPLING_RATE, 
//						new SensorConfigOptionDetails(SensorConfigOptionDetails.GUI_COMPONENT_TYPE.TEXTFIELD));
////				mConfigOptionsMap.put(Configuration.Shimmer3.GuiLabelConfig.SHIMMER_MAC_FROM_INFOMEM, 
////						new SensorConfigOptionDetails(SensorConfigOptionDetails.GUI_COMPONENT_TYPE.TEXTFIELD));
//				mConfigOptionsMap.put(Configuration.Shimmer3.GuiLabelConfig.CONFIG_TIME, 
//						new SensorConfigOptionDetails(SensorConfigOptionDetails.GUI_COMPONENT_TYPE.TEXTFIELD,
//								listOfCompatibleVersionInfoGq));
//				mConfigOptionsMap.put(Configuration.Shimmer3.GuiLabelConfig.TRIAL_NAME, 
//						new SensorConfigOptionDetails(SensorConfigOptionDetails.GUI_COMPONENT_TYPE.TEXTFIELD,
//								listOfCompatibleVersionInfoGq));
////				mConfigOptionsMap.put(Configuration.Shimmer3.GuiLabelConfig.EXPERIMENT_ID, 
////						new SensorConfigOptionDetails(SensorConfigOptionDetails.GUI_COMPONENT_TYPE.TEXTFIELD,
////								listOfCompatibleVersionInfoGq));
//
//				mConfigOptionsMap.put(Configuration.Shimmer3.GuiLabelConfig.LSM303DLHC_ACCEL_RANGE, 
//						new SensorConfigOptionDetails(Configuration.Shimmer3.ListofAccelRange, 
//												Configuration.Shimmer3.ListofLSM303DLHCAccelRangeConfigValues, 
//												SensorConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX));
//				
//				mConfigOptionsMap.put(Configuration.Shimmer3.GuiLabelConfig.LSM303DLHC_ACCEL_RATE, 
//						new SensorConfigOptionDetails(Configuration.Shimmer3.ListofLSM303DLHCAccelRate, 
//												Configuration.Shimmer3.ListofLSM303DLHCAccelRateConfigValues, 
//												SensorConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX));
//				mConfigOptionsMap.get(Configuration.Shimmer3.GuiLabelConfig.LSM303DLHC_ACCEL_RATE).setGuiValues(SensorConfigOptionDetails.VALUE_INDEXES.LSM303DLHC_ACCEL_RATE.IS_LPM, Configuration.Shimmer3.ListofLSM303DLHCAccelRateLpm);
//				mConfigOptionsMap.get(Configuration.Shimmer3.GuiLabelConfig.LSM303DLHC_ACCEL_RATE).setConfigValues(SensorConfigOptionDetails.VALUE_INDEXES.LSM303DLHC_ACCEL_RATE.IS_LPM, Configuration.Shimmer3.ListofLSM303DLHCAccelRateLpmConfigValues);
//
////				if(mLowPowerAccelWR) {
////					mConfigOptionsMap.put(Configuration.Shimmer3.GuiLabelConfig.LSM303DLHC_ACCEL_RATE, 
////							new SensorConfigOptionDetails(Configuration.Shimmer3.ListofLSM303DLHCAccelRateLpm, 
////													Configuration.Shimmer3.ListofLSM303DLHCAccelRateLpmConfigValues, 
////													SensorConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX));
////				}
////				else {
////					mConfigOptionsMap.put(Configuration.Shimmer3.GuiLabelConfig.LSM303DLHC_ACCEL_RATE, 
////							new SensorConfigOptionDetails(Configuration.Shimmer3.ListofLSM303DLHCAccelRate, 
////													Configuration.Shimmer3.ListofLSM303DLHCAccelRateConfigValues, 
////													SensorConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX));
////				}
//				
//				mConfigOptionsMap.put(Configuration.Shimmer3.GuiLabelConfig.GSR_RANGE, 
//						new SensorConfigOptionDetails(Configuration.Shimmer3.ListofGSRRange, 
//												Configuration.Shimmer3.ListofGSRRangeConfigValues, 
//												SensorConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX,
//												listOfCompatibleVersionInfoGq));
//				
//				mConfigOptionsMap.put(Configuration.ShimmerGQ.GuiLabelConfig.SAMPLING_RATE_DIVIDER_GSR, 
//						new SensorConfigOptionDetails(Configuration.ShimmerGQ.ListofSamplingRateDividers, 
//												Configuration.ShimmerGQ.ListofSamplingRateDividersValues, 
//												SensorConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX));
//				mConfigOptionsMap.put(Configuration.ShimmerGQ.GuiLabelConfig.SAMPLING_RATE_DIVIDER_LSM303DLHC_ACCEL, 
//						new SensorConfigOptionDetails(Configuration.ShimmerGQ.ListofSamplingRateDividers, 
//												Configuration.ShimmerGQ.ListofSamplingRateDividersValues, 
//												SensorConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX));
//				mConfigOptionsMap.put(Configuration.ShimmerGQ.GuiLabelConfig.SAMPLING_RATE_DIVIDER_PPG, 
//						new SensorConfigOptionDetails(Configuration.ShimmerGQ.ListofSamplingRateDividers, 
//												Configuration.ShimmerGQ.ListofSamplingRateDividersValues, 
//												SensorConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX));
//				mConfigOptionsMap.put(Configuration.ShimmerGQ.GuiLabelConfig.SAMPLING_RATE_DIVIDER_VBATT, 
//						new SensorConfigOptionDetails(Configuration.ShimmerGQ.ListofSamplingRateDividers, 
//												Configuration.ShimmerGQ.ListofSamplingRateDividersValues, 
//												SensorConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX));
//				mConfigOptionsMap.put(Configuration.ShimmerGQ.GuiLabelConfig.SAMPLING_RATE_DIVIDER_BEACON, 
//						new SensorConfigOptionDetails(Configuration.ShimmerGQ.ListofSamplingRateDividers, 
//												Configuration.ShimmerGQ.ListofSamplingRateDividersValues, 
//												SensorConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX));
//
//			    mConfigOptionsMap.put(Configuration.Shimmer3.GuiLabelConfig.INT_EXP_BRD_POWER_INTEGER, 
//			    	      new SensorConfigOptionDetails(Configuration.Shimmer3.ListOfOnOff, 
//					    	        Configuration.Shimmer3.ListOfOnOffConfigValues,
//					    	        SensorConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX));
//				
//				//General Config
//				mConfigOptionsMap.put(Configuration.Shimmer3.GuiLabelConfig.USER_BUTTON_START, 
//						new SensorConfigOptionDetails(SensorConfigOptionDetails.GUI_COMPONENT_TYPE.CHECKBOX,listOfCompatibleVersionInfoGq));
//				mConfigOptionsMap.put(Configuration.Shimmer3.GuiLabelConfig.SINGLE_TOUCH_START, 
//						new SensorConfigOptionDetails(SensorConfigOptionDetails.GUI_COMPONENT_TYPE.CHECKBOX,listOfCompatibleVersionInfoGq));
//
//				mConfigOptionsMap.put(Configuration.Shimmer3.GuiLabelConfig.LSM303DLHC_ACCEL_LPM, 
//						new SensorConfigOptionDetails(SensorConfigOptionDetails.GUI_COMPONENT_TYPE.CHECKBOX));
//				
//				mConfigOptionsMap.put(Configuration.Shimmer3.GuiLabelConfig.INT_EXP_BRD_POWER_BOOLEAN, 
//						new SensorConfigOptionDetails(SensorConfigOptionDetails.GUI_COMPONENT_TYPE.CHECKBOX));
			}
		}
	}
	
	
	public void checkConfigOptionValues(String stringKey) {
		if(mConfigOptionsMap!=null){
			if(mConfigOptionsMap.containsKey(stringKey)){
				if(mHardwareVersion==HW_ID.SHIMMER_3){
			        if(stringKey.equals(Configuration.Shimmer3.GuiLabelConfig.LSM303DLHC_ACCEL_RATE)) {
			        	if(isLSM303DigitalAccelLPM()) {
							mConfigOptionsMap.get(stringKey).setIndexOfValuesToUse(SensorConfigOptionDetails.VALUE_INDEXES.LSM303DLHC_ACCEL_RATE.IS_LPM);
			        	}
			        	else {
							mConfigOptionsMap.get(stringKey).setIndexOfValuesToUse(SensorConfigOptionDetails.VALUE_INDEXES.LSM303DLHC_ACCEL_RATE.NOT_LPM);
		        			// double check that rate is compatible with LPM (8 not compatible so to higher rate)
			        		setLSM303DigitalAccelRate(mLSM303DigitalAccelRate);
			        	}
			        }
			        else if(stringKey.equals(Configuration.Shimmer3.GuiLabelConfig.EXG_RESPIRATION_DETECT_PHASE)) {
			        	checkWhichExgRespPhaseValuesToUse();
			        }
			        else if(stringKey.equals(Configuration.Shimmer3.GuiLabelConfig.EXG_REFERENCE_ELECTRODE)) {
			        	checkWhichExgRefElectrodeValuesToUse();
			        }
				}
				else if(mHardwareVersion==HW_ID.SHIMMER_GQ){
				}
			}
		}
	}
	
	private void checkWhichExgRespPhaseValuesToUse(){
		String stringKey = Configuration.Shimmer3.GuiLabelConfig.EXG_RESPIRATION_DETECT_PHASE;
		if(mConfigOptionsMap!=null){
			if(mConfigOptionsMap.containsKey(stringKey)){
				if(mHardwareVersion==HW_ID.SHIMMER_3){
					if(isExgRespirationDetectFreq32kHz()) {
						mConfigOptionsMap.get(stringKey).setIndexOfValuesToUse(SensorConfigOptionDetails.VALUE_INDEXES.EXG_RESPIRATION_DETECT_PHASE.PHASE_32KHZ);
					}
					else {
						mConfigOptionsMap.get(stringKey).setIndexOfValuesToUse(SensorConfigOptionDetails.VALUE_INDEXES.EXG_RESPIRATION_DETECT_PHASE.PHASE_64KHZ);
					}
					
					// change config val if not appropriate
					Integer[] configvalues = mConfigOptionsMap.get(stringKey).getConfigValues();
					if(!Arrays.asList(configvalues).contains(getEXG2RespirationDetectPhase())){
						setEXG2RespirationDetectPhase(configvalues[0]);
					}
					
				}
			}
		}
	}
	
	private void checkWhichExgRefElectrodeValuesToUse(){
		String stringKey = Configuration.Shimmer3.GuiLabelConfig.EXG_REFERENCE_ELECTRODE;
		if(mConfigOptionsMap!=null){
			if(mConfigOptionsMap.containsKey(stringKey)){
				if(mHardwareVersion==HW_ID.SHIMMER_3){
					if(isEXGUsingDefaultRespirationConfiguration()) { // Do Respiration check first
						mConfigOptionsMap.get(stringKey).setIndexOfValuesToUse(SensorConfigOptionDetails.VALUE_INDEXES.EXG_REFERENCE_ELECTRODE.RESP);
					}
					else if(isEXGUsingDefaultECGConfiguration()) {
						mConfigOptionsMap.get(stringKey).setIndexOfValuesToUse(SensorConfigOptionDetails.VALUE_INDEXES.EXG_REFERENCE_ELECTRODE.ECG);
					}
					else if(isEXGUsingDefaultEMGConfiguration()) {
						mConfigOptionsMap.get(stringKey).setIndexOfValuesToUse(SensorConfigOptionDetails.VALUE_INDEXES.EXG_REFERENCE_ELECTRODE.EMG);
					}
					else if(isEXGUsingDefaultTestSignalConfiguration()) {
						mConfigOptionsMap.get(stringKey).setIndexOfValuesToUse(SensorConfigOptionDetails.VALUE_INDEXES.EXG_REFERENCE_ELECTRODE.TEST);
					}
					else {
						mConfigOptionsMap.get(stringKey).setIndexOfValuesToUse(SensorConfigOptionDetails.VALUE_INDEXES.EXG_REFERENCE_ELECTRODE.CUSTOM);
					}
					
					// change config val if not appropriate
					Integer[] configvalues = mConfigOptionsMap.get(stringKey).getConfigValues();
					if(!Arrays.asList(configvalues).contains(getEXGReferenceElectrode())){
						setEXGReferenceElectrode(configvalues[0]);
					}

				}
			}
		}
	}

	
	/**
	 * Used to convert from the enabledSensors long variable read from the
	 * Shimmer to the set enabled status of the relative entries in the Sensor
	 * Map. Used in Consensys for dynamic GUI generation to configure a Shimmer.
	 * 
	 */
	public void sensorMapUpdateFromEnabledSensorsVars() {

		checkExgResolutionFromEnabledSensorsVar();

		if(mSensorMap==null){
			sensorAndConfigMapsCreate();
		}
		
		if(mSensorMap!=null) {

			if (mHardwareVersion == HW_ID.SHIMMER_3) {
				
				for(Integer sensorMapKey:mSensorMap.keySet()) {
					boolean skipKey = false;

					// Skip if ExG channels here -> handle them after for loop.
					if((sensorMapKey==Configuration.Shimmer3.SensorMapKey.ECG)
							||(sensorMapKey==Configuration.Shimmer3.SensorMapKey.EMG)
							||(sensorMapKey==Configuration.Shimmer3.SensorMapKey.EXG_TEST)
							||(sensorMapKey==Configuration.Shimmer3.SensorMapKey.EXG_CUSTOM)
							||(sensorMapKey==Configuration.Shimmer3.SensorMapKey.EXG_RESPIRATION)) {
						mSensorMap.get(sensorMapKey).mIsEnabled = false;
						skipKey = true;
					}
					// Handle derived sensors based on int adc channels (e.g. PPG vs. A12/A13)
					else if(((sensorMapKey==Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A12)
						||(sensorMapKey==Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A13)
						||(sensorMapKey==Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A1)
						||(sensorMapKey==Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A14))){

						//Check if a derived channel is enabled, if it is ignore disable and skip 
						innerloop:
						for(Integer conflictKey:mSensorMap.get(sensorMapKey).mSensorDetails.mListOfSensorMapKeysConflicting) {
							if(mSensorMap.get(conflictKey).isDerivedChannel()) {
								if((mDerivedSensors&mSensorMap.get(conflictKey).mDerivedSensorBitmapID) == mSensorMap.get(conflictKey).mDerivedSensorBitmapID) {
									mSensorMap.get(sensorMapKey).mIsEnabled = false;
									skipKey = true;
									break innerloop;
								}
							}
						}
					}
//					else if(sensorMapKey == Configuration.Shimmer3.SensorMapKey.TIMESTAMP_SYNC 
////							|| sensorMapKey == Configuration.Shimmer3.SensorMapKey.TIMESTAMP
////							|| sensorMapKey == Configuration.Shimmer3.SensorMapKey.REAL_TIME_CLOCK
//							|| sensorMapKey == Configuration.Shimmer3.SensorMapKey.REAL_TIME_CLOCK_SYNC){
//						mSensorMap.get(sensorMapKey).mIsEnabled = false;
//						skipKey = true;
//					}
					else if(sensorMapKey == Configuration.Shimmer3.SensorMapKey.SHIMMER_STREAMING_PROPERTIES){
						mSensorMap.get(sensorMapKey).mIsEnabled = true;
						skipKey = true;
					}


					// Process remaining channels
					if(!skipKey) {
						mSensorMap.get(sensorMapKey).mIsEnabled = false;
						// Check if this sensor is a derived sensor
						if(mSensorMap.get(sensorMapKey).isDerivedChannel()) {
							//Check if associated derived channels are enabled 
							if((mDerivedSensors&mSensorMap.get(sensorMapKey).mDerivedSensorBitmapID) == mSensorMap.get(sensorMapKey).mDerivedSensorBitmapID) {
								//TODO add comment
								if((mEnabledSensors&mSensorMap.get(sensorMapKey).mSensorDetails.mSensorBitmapIDSDLogHeader) == mSensorMap.get(sensorMapKey).mSensorDetails.mSensorBitmapIDSDLogHeader) {
									mSensorMap.get(sensorMapKey).mIsEnabled = true;
								}
							}
						}
						// This is not a derived sensor
						else {
							//Check if sensor's bit in sensor bitmap is enabled
							if((mEnabledSensors&mSensorMap.get(sensorMapKey).mSensorDetails.mSensorBitmapIDSDLogHeader) == mSensorMap.get(sensorMapKey).mSensorDetails.mSensorBitmapIDSDLogHeader) {
								mSensorMap.get(sensorMapKey).mIsEnabled = true;
							}
						}
					}
				}
				
				// Now that all main sensor channels have been parsed, deal with
				// sensor channels that have special conditions. E.g. deciding
				// what type of signal the ExG is configured for or what derived
				// channel is enabled like whether PPG is on ADC12 or ADC13
				
				//Handle ExG sensors
				internalCheckExgModeAndUpdateSensorMap();

				// Handle PPG sensors so that it appears in Consensys as a
				// single PPG channel with a selectable ADC based on different
				// hardware versions.
				
				//Used for Shimmer GSR hardware
				if((mSensorMap.get(Configuration.Shimmer3.SensorMapKey.PPG_A12).mIsEnabled)||(mSensorMap.get(Configuration.Shimmer3.SensorMapKey.PPG_A13).mIsEnabled)) {
					mSensorMap.get(Configuration.Shimmer3.SensorMapKey.PPG_DUMMY).mIsEnabled = true;
					if(mSensorMap.get(Configuration.Shimmer3.SensorMapKey.PPG_A12).mIsEnabled) {
						mPpgAdcSelectionGsrBoard = Configuration.Shimmer3.ListOfPpgAdcSelectionConfigValues[1]; // PPG_A12
					}
					else if(mSensorMap.get(Configuration.Shimmer3.SensorMapKey.PPG_A13).mIsEnabled) {
						mPpgAdcSelectionGsrBoard = Configuration.Shimmer3.ListOfPpgAdcSelectionConfigValues[0]; // PPG_A13
					}
				}
				else {
					mSensorMap.get(Configuration.Shimmer3.SensorMapKey.PPG_DUMMY).mIsEnabled = false;
				}
				//Used for Shimmer Proto3 Deluxe hardware
				if((mSensorMap.get(Configuration.Shimmer3.SensorMapKey.PPG1_A12).mIsEnabled)||(mSensorMap.get(Configuration.Shimmer3.SensorMapKey.PPG1_A13).mIsEnabled)) {
					mSensorMap.get(Configuration.Shimmer3.SensorMapKey.PPG1_DUMMY).mIsEnabled = true;
					if(mSensorMap.get(Configuration.Shimmer3.SensorMapKey.PPG1_A12).mIsEnabled) {
						mPpg1AdcSelectionProto3DeluxeBoard = Configuration.Shimmer3.ListOfPpg1AdcSelectionConfigValues[1]; // PPG1_A12
					}
					else if(mSensorMap.get(Configuration.Shimmer3.SensorMapKey.PPG1_A13).mIsEnabled) {
						mPpg1AdcSelectionProto3DeluxeBoard = Configuration.Shimmer3.ListOfPpg1AdcSelectionConfigValues[0]; // PPG1_A13
					}
				}
				else {
					mSensorMap.get(Configuration.Shimmer3.SensorMapKey.PPG1_DUMMY).mIsEnabled = false;
				}
				//Used for Shimmer Proto3 Deluxe hardware
				if((mSensorMap.get(Configuration.Shimmer3.SensorMapKey.PPG2_A1).mIsEnabled)||(mSensorMap.get(Configuration.Shimmer3.SensorMapKey.PPG2_A14).mIsEnabled)) {
					mSensorMap.get(Configuration.Shimmer3.SensorMapKey.PPG2_DUMMY).mIsEnabled = true;
					if(mSensorMap.get(Configuration.Shimmer3.SensorMapKey.PPG2_A1).mIsEnabled) {
						mPpg2AdcSelectionProto3DeluxeBoard = Configuration.Shimmer3.ListOfPpg2AdcSelectionConfigValues[0]; // PPG2_A1
					}
					else if(mSensorMap.get(Configuration.Shimmer3.SensorMapKey.PPG2_A14).mIsEnabled) {
						mPpg2AdcSelectionProto3DeluxeBoard = Configuration.Shimmer3.ListOfPpg2AdcSelectionConfigValues[1]; // PPG2_A14
					}
				}
				else {
					mSensorMap.get(Configuration.Shimmer3.SensorMapKey.PPG2_DUMMY).mIsEnabled = false;
				}
			}
			else if (mHardwareVersion == HW_ID.SHIMMER_GQ) {
				
			}
			
		}
		
	}
	
	private void checkExgResolutionFromEnabledSensorsVar(){
		mIsExg1_24bitEnabled = ((mEnabledSensors & mInfoMemLayout.maskExg1_24bitFlag)==mInfoMemLayout.maskExg1_24bitFlag)? true:false;
		mIsExg2_24bitEnabled = ((mEnabledSensors & mInfoMemLayout.maskExg2_24bitFlag)==mInfoMemLayout.maskExg2_24bitFlag)? true:false;
		mIsExg1_16bitEnabled = ((mEnabledSensors & mInfoMemLayout.maskExg1_16bitFlag)==mInfoMemLayout.maskExg1_16bitFlag)? true:false;
		mIsExg2_16bitEnabled = ((mEnabledSensors & mInfoMemLayout.maskExg2_16bitFlag)==mInfoMemLayout.maskExg2_16bitFlag)? true:false;
		
		if(mIsExg1_16bitEnabled||mIsExg2_16bitEnabled){
			mExGResolution = 0;
		}
		else if(mIsExg1_24bitEnabled||mIsExg2_24bitEnabled){
			mExGResolution = 1;
		}
	}

	private void updateEnabledSensorsFromExgResolution(){
		//JC: should this be here -> checkExgResolutionFromEnabledSensorsVar()
		
		mEnabledSensors &= ~mInfoMemLayout.maskExg1_24bitFlag;
		mEnabledSensors |= (mIsExg1_24bitEnabled? mInfoMemLayout.maskExg1_24bitFlag:0);
		
		mEnabledSensors &= ~mInfoMemLayout.maskExg2_24bitFlag;
		mEnabledSensors |= (mIsExg2_24bitEnabled? mInfoMemLayout.maskExg2_24bitFlag:0);
		
		mEnabledSensors &= ~mInfoMemLayout.maskExg1_16bitFlag;
		mEnabledSensors |= (mIsExg1_16bitEnabled? mInfoMemLayout.maskExg1_16bitFlag:0);
		
		mEnabledSensors &= ~mInfoMemLayout.maskExg2_16bitFlag;
		mEnabledSensors |= (mIsExg2_16bitEnabled? mInfoMemLayout.maskExg2_16bitFlag:0);
	}
	
	private void setExgChannelBitsPerMode(int sensorMapKey){
		mIsExg1_24bitEnabled = false;
		mIsExg2_24bitEnabled = false;
		mIsExg1_16bitEnabled = false;
		mIsExg2_16bitEnabled = false;
		
		boolean chip1Enabled = false;
		boolean chip2Enabled = false;
		if(sensorMapKey==-1){
			chip1Enabled = false;
			chip2Enabled = false;
		}
		else if(sensorMapKey==Configuration.Shimmer3.SensorMapKey.ECG){
			chip1Enabled = true;
			chip2Enabled = true;
		}
		else if(sensorMapKey==Configuration.Shimmer3.SensorMapKey.EMG){
			chip1Enabled = true;
			chip2Enabled = false;
		}
		else if(sensorMapKey==Configuration.Shimmer3.SensorMapKey.EXG_RESPIRATION){
			chip1Enabled = true;
			chip2Enabled = true;
		}
		else if(sensorMapKey==Configuration.Shimmer3.SensorMapKey.EXG_CUSTOM){
			chip1Enabled = true;
			chip2Enabled = true;
		}
		else if(sensorMapKey==Configuration.Shimmer3.SensorMapKey.EXG_TEST){
			chip1Enabled = true;
			chip2Enabled = true;
		}
		
		if(mExGResolution==1){
			mIsExg1_24bitEnabled = chip1Enabled;
			mIsExg2_24bitEnabled = chip2Enabled;
		}
		else {
			mIsExg1_16bitEnabled = chip1Enabled;
			mIsExg2_16bitEnabled = chip2Enabled;
		}
	}

	public boolean checkIfSensorEnabled(int sensorMapKey){
		if (mHardwareVersion == HW_ID.SHIMMER_3) {
			//Used for Shimmer GSR hardware
			if(sensorMapKey==Configuration.Shimmer3.SensorMapKey.PPG_DUMMY){
				if((isSensorEnabled(Configuration.Shimmer3.SensorMapKey.PPG_A12))||(isSensorEnabled(Configuration.Shimmer3.SensorMapKey.PPG_A13)))
					return true;
				else
					return false;
			}
			else if(sensorMapKey==Configuration.Shimmer3.SensorMapKey.PPG1_DUMMY){
				if((isSensorEnabled(Configuration.Shimmer3.SensorMapKey.PPG1_A12))||(isSensorEnabled(Configuration.Shimmer3.SensorMapKey.PPG1_A13)))
					return true;
				else
					return false;
			}
			else if(sensorMapKey==Configuration.Shimmer3.SensorMapKey.PPG2_DUMMY){
				if((isSensorEnabled(Configuration.Shimmer3.SensorMapKey.PPG2_A1))||(isSensorEnabled(Configuration.Shimmer3.SensorMapKey.PPG2_A14)))
					return true;
				else
					return false;
			}
			
			return isSensorEnabled(sensorMapKey);
		}
		return false;
	}
	
	private boolean isSensorEnabled(int sensorMapKey){
		if((mSensorMap!=null)&&(mSensorMap!=null)) {
			if(mSensorMap.containsKey(sensorMapKey)){
				return mSensorMap.get(sensorMapKey).mIsEnabled;
			}
		}		
		return false;
	}
	
	
	
	/**
	 * Used to changed the enabled state of a sensor in the sensormap. This is
	 * only used in Consensys for dynamic configuration of a Shimmer. This
	 * method deals with everything assciated with enabling a sensor such as:
	 * 1) dealing with conflicting sensors
	 * 2) dealing with other required sensors for the chosen sensor
	 * 3) determining whether expansion board power is required
	 * 4) setting default settings for disabled sensors 
	 * 5) etc.
	 * 
	 * @param sensorMapKey the sensormap key of the sensor to be enabled/disabled
	 * @param state the sensor state to set 
	 * @return a boolean indicating if the sensors state was successfully changed
	 */
	public boolean setSensorEnabledState(int sensorMapKey, boolean state) {
		
		if(mSensorMap!=null) {
			
			if (mHardwareVersion == HW_ID.SHIMMER_3){
				
				// Special case for Dummy entries in the Sensor Map
				if(sensorMapKey == Configuration.Shimmer3.SensorMapKey.PPG_DUMMY) {
					mSensorMap.get(sensorMapKey).mIsEnabled = state;
					if(Configuration.Shimmer3.ListOfPpgAdcSelection[mPpgAdcSelectionGsrBoard].contains("A12")) {
						sensorMapKey = Configuration.Shimmer3.SensorMapKey.PPG_A12;
					}
					else {
						sensorMapKey = Configuration.Shimmer3.SensorMapKey.PPG_A13;
					}
				}		
				else if(sensorMapKey == Configuration.Shimmer3.SensorMapKey.PPG1_DUMMY) {
					mSensorMap.get(sensorMapKey).mIsEnabled = state;
					if(Configuration.Shimmer3.ListOfPpg1AdcSelection[mPpg1AdcSelectionProto3DeluxeBoard].contains("A12")) {
						sensorMapKey = Configuration.Shimmer3.SensorMapKey.PPG1_A12;
					}
					else {
						sensorMapKey = Configuration.Shimmer3.SensorMapKey.PPG1_A13;
					}
				}		
				else if(sensorMapKey == Configuration.Shimmer3.SensorMapKey.PPG2_DUMMY) {
					mSensorMap.get(sensorMapKey).mIsEnabled = state;
					if(Configuration.Shimmer3.ListOfPpg2AdcSelection[mPpg2AdcSelectionProto3DeluxeBoard].contains("A14")) {
						sensorMapKey = Configuration.Shimmer3.SensorMapKey.PPG2_A14;
					}
					else {
						sensorMapKey = Configuration.Shimmer3.SensorMapKey.PPG2_A1;
					}
				}		
				
				// Automatically handle required channels for each sensor
				if(mSensorMap.get(sensorMapKey).mSensorDetails.mListOfSensorMapKeysRequired != null) {
					for(Integer i:mSensorMap.get(sensorMapKey).mSensorDetails.mListOfSensorMapKeysRequired) {
						mSensorMap.get(i).mIsEnabled = state;
					}
				}
				
			}
			else if (mHardwareVersion == HW_ID.SHIMMER_GQ) {
				
			}
			
			//Set sensor state
			mSensorMap.get(sensorMapKey).mIsEnabled = state;

			sensorMapConflictCheckandCorrect(sensorMapKey);
			setDefaultConfigForSensor(sensorMapKey, mSensorMap.get(sensorMapKey).mIsEnabled);

			// Automatically control internal expansion board power
			checkIfInternalExpBrdPowerIsNeeded();
			
			refreshEnabledSensorsFromSensorMap();

			return ((mSensorMap.get(sensorMapKey).mIsEnabled==state)? true:false);
			
		}
		else {
			return false;
		}
	}
	
	/**
	 * Method to set force defaults for disabled sensors. Need to ensure
	 * consistency across all configured Shimmers. Without the below, if a
	 * Shimmer is read from and then configured without changing any of the
	 * configuration, the configuration will not be checked. Another application
	 * could have saved incorrect configuration to the Shimmer.
	 * 
	 */
	public void checkShimmerConfigBeforeConfiguring() {
		
		if (mHardwareVersion == HW_ID.SHIMMER_3){
			
			if(!isSensorEnabled(Configuration.Shimmer3.SensorMapKey.LSM303DLHC_ACCEL)) {
				setDefaultLsm303dlhcAccelSensorConfig(false);
			}
			if(!isSensorEnabled(Configuration.Shimmer3.SensorMapKey.LSM303DLHC_MAG)) {
				setDefaultLsm303dlhcMagSensorConfig(false);
			}
			if(!isSensorEnabled(Configuration.Shimmer3.SensorMapKey.MPU9150_GYRO)){
				setDefaultMpu9150GyroSensorConfig(false);
			}
			if(!isSensorEnabled(Configuration.Shimmer3.SensorMapKey.MPU9150_ACCEL)){
				setDefaultMpu9150AccelSensorConfig(false);
			}
			if(!isSensorEnabled(Configuration.Shimmer3.SensorMapKey.MPU9150_MAG)){
				setMPU9150MagRateFromFreq(mShimmerSamplingRate);
			}
			if(!isSensorEnabled(Configuration.Shimmer3.SensorMapKey.BMP180_PRESSURE)) {
				setDefaultBmp180PressureSensorConfig(false);
			}
			if(!isSensorEnabled(Configuration.Shimmer3.SensorMapKey.GSR)) {
				setDefaultGsrSensorConfig(false);
			}
			if((!isSensorEnabled(Configuration.Shimmer3.SensorMapKey.ECG))
					&&(!isSensorEnabled(Configuration.Shimmer3.SensorMapKey.EXG_TEST))
					&&(!isSensorEnabled(Configuration.Shimmer3.SensorMapKey.EMG))
					&&(!isSensorEnabled(Configuration.Shimmer3.SensorMapKey.EXG_CUSTOM))
					&&(!isSensorEnabled(Configuration.Shimmer3.SensorMapKey.EXG_RESPIRATION)) ){
//					&&(!isSensorEnabled(Configuration.Shimmer3.SensorMapKey.EXG1_16BIT))
//					&&(!isSensorEnabled(Configuration.Shimmer3.SensorMapKey.EXG2_16BIT))
//					&&(!isSensorEnabled(Configuration.Shimmer3.SensorMapKey.EXG1_24BIT))
//					&&(!isSensorEnabled(Configuration.Shimmer3.SensorMapKey.EXG2_24BIT))) {
//				if(!checkIfOtherExgChannelEnabled()) {
					setDefaultECGConfiguration();
//				}
			}
			if(!checkIfAnyMplChannelEnabled()) {
				setDefaultMpu9150MplSensorConfig(false);
			}
			
			checkIfInternalExpBrdPowerIsNeeded();
//			checkIfMPLandDMPIsNeeded();
		}
		
	}

	/**Automatically control internal expansion board power based on sensor map
	 * 
	 */
	private void checkIfInternalExpBrdPowerIsNeeded(){

		if (mHardwareVersion == HW_ID.SHIMMER_3){
			for(Integer channelKey:mSensorMap.keySet()) {
				if(mSensorMap.get(channelKey).mIsEnabled && mSensorMap.get(channelKey).mSensorDetails.mIntExpBoardPowerRequired) {
					mInternalExpPower = 1;
					break;
				}
				else {
					// Exception for Int ADC sensors 
					//TODO need to check HW version??
					if(isSensorEnabled(Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A1)
						||isSensorEnabled(Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A12)
						||isSensorEnabled(Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A13)
						||isSensorEnabled(Configuration.Shimmer3.SensorMapKey.INT_EXP_ADC_A14)){
						
					}
					else {
						mInternalExpPower = 0;
					}
				}
			}
		}
	}
	
//	private void copySensorMapSensorDetails(int keyFrom, int keyTo) {
//		if(mSensorMap!=null) {
//			if(mSensorMapRef.containsKey(keyFrom)&&mSensorMapRef.containsKey(keyTo)) {
//				mSensorMapRef.get(keyTo).mListOfCompatibleVersionInfo = mSensorMapRef.get(keyFrom).mListOfCompatibleVersionInfo;
//				mSensorMapRef.get(keyTo).mListOfSensorMapKeysRequired = mSensorMapRef.get(keyFrom).mListOfSensorMapKeysRequired;
//				mSensorMapRef.get(keyTo).mListOfSensorMapKeysConflicting = mSensorMapRef.get(keyFrom).mListOfSensorMapKeysConflicting;
////				mSensorMap.get(keyTo).mIntExpBoardPowerRequired = mSensorMap.get(keyFrom).mIntExpBoardPowerRequired;
//				mSensorMapRef.get(keyTo).mListOfConfigOptionKeysAssociated = mSensorMapRef.get(keyFrom).mListOfConfigOptionKeysAssociated;
//				
////				mSensorMap.get(keyTo).mSensorBitmapIDStreaming = mSensorMap.get(keyFrom).mSensorBitmapIDStreaming;
////				mSensorMap.get(keyTo).mSensorBitmapIDSDLogHeader = mSensorMap.get(keyFrom).mSensorBitmapIDSDLogHeader;
////				mSensorMap.get(keyTo).mDerivedChannelBitmapBitShift = mSensorMap.get(keyFrom).mDerivedChannelBitmapBitShift;
////				mSensorMap.get(keyTo).mDerivedChannelBitmapMask = mSensorMap.get(keyFrom).mDerivedChannelBitmapMask;
//			}
//		}		
//	}
	
	//TODO improve this method - was changed at the last minute and is not fully operational
	public boolean checkIfVersionCompatible(List<ShimmerVerObject> listOfCompatibleVersionInfo) {
		if(listOfCompatibleVersionInfo == null) {
			return true;
		}
		
		for(ShimmerVerObject compatibleVersionInfo:listOfCompatibleVersionInfo) {

			boolean compatible = true;
			
			boolean checkHardwareVersion = false;
			boolean checkExpansionBoardId = false;
			boolean checkFirmwareIdentifier = false;
			boolean checkFirmwareVersionMajor = false;
			boolean checkFirmwareVersionMinor = false;
			boolean checkFirmwareVersionInternal = false;
			
			if(compatibleVersionInfo.mHardwareVersion!=ANY_VERSION) {
				checkHardwareVersion = true;
			}
			if(compatibleVersionInfo.mShimmerExpansionBoardId!=ANY_VERSION) {
				checkExpansionBoardId = true;
			}
			if(compatibleVersionInfo.mFirmwareIdentifier!=ANY_VERSION) {
				checkFirmwareIdentifier = true;
			}
			if(compatibleVersionInfo.mFirmwareVersionMajor!=ANY_VERSION) {
				checkFirmwareVersionMajor = true;
			}
			if(compatibleVersionInfo.mFirmwareVersionMinor!=ANY_VERSION) {
				checkFirmwareVersionMinor = true;
			}
			if(compatibleVersionInfo.mFirmwareVersionInternal!=ANY_VERSION) {
				checkFirmwareVersionInternal = true;
			}
			
			if((compatible)&&(checkHardwareVersion)) {
				if(mHardwareVersion != compatibleVersionInfo.mHardwareVersion) {
					compatible = false;
				}
			}
			if((compatible)&&(checkExpansionBoardId)) {
				if(mExpansionBoardId != compatibleVersionInfo.mShimmerExpansionBoardId) {
					compatible = false;
				}
			}
//			if((compatible)&&(checkFirmwareIdentifier)) {
//				if(mFirmwareIdentifier != compatibleVersionInfo.mFirmwareIdentifier) {
//					compatible = false;
//				}
//			}
			
//			if((compatible)&&(checkFirmwareVersionMajor)) {
//				if(mFirmwareVersionMajor < compatibleVersionInfo.mFirmwareVersionMajor) {
//					compatible = false;
//				}
//				if((compatible)&&(checkFirmwareVersionMinor)) {
//					if(mFirmwareVersionMinor < compatibleVersionInfo.mFirmwareVersionMinor) {
//						compatible = false;
//					}
//				}
//				if((compatible)&&(checkFirmwareVersionInternal)) {
//					if(mFirmwareVersionInternal < compatibleVersionInfo.mFirmwareVersionInternal) {
//						compatible = false;
//					}
//				}
//			}
//			else if((compatible)&&(checkFirmwareVersionMinor)) {
//				if(mFirmwareVersionMinor < compatibleVersionInfo.mFirmwareVersionMinor) {
//					compatible = false;
//				}
//				if((compatible)&&(checkFirmwareVersionInternal)) {
//					if(mFirmwareVersionInternal < compatibleVersionInfo.mFirmwareVersionInternal) {
//						compatible = false;
//					}
//				}
//			}
//			else if((compatible)&&(checkFirmwareVersionInternal)) {
//				if(mFirmwareVersionInternal < compatibleVersionInfo.mFirmwareVersionInternal) {
//					compatible = false;
//				}
//			}
			
			if(checkFirmwareVersionMajor){
				// Using the tree structure below each of the FW Major, Minor or Internal Release variables can be ignored
				if((compatible)&&(!UtilShimmer.compareVersions(mFirmwareIdentifier, 
						mFirmwareVersionMajor, 
						mFirmwareVersionMinor, 
						mFirmwareVersionInternal, 
						compatibleVersionInfo.mFirmwareIdentifier, 
						compatibleVersionInfo.mFirmwareVersionMajor, 
						compatibleVersionInfo.mFirmwareVersionMinor, 
						compatibleVersionInfo.mFirmwareVersionInternal))){
					compatible = false;
				}
			}
			
			if(compatible) {
				return true;
			}
		}
		return false;
	}

	
	public List<Integer> sensorMapConflictCheck(Integer key){
		List<Integer> listOfChannelConflicts = new ArrayList<Integer>();
		
		//TODO: handle Shimmer2/r exceptions which involve get5VReg(), getPMux() and writePMux()
		
		if (mHardwareVersion == HW_ID.SHIMMER_3){
			if(mSensorMap.get(key).mSensorDetails.mListOfSensorMapKeysConflicting != null) {
				for(Integer sensorMapKey:mSensorMap.get(key).mSensorDetails.mListOfSensorMapKeysConflicting) {
					if(isSensorEnabled(sensorMapKey)) {
						listOfChannelConflicts.add(sensorMapKey);
					}
				}
			}
		}
		
		if(listOfChannelConflicts.isEmpty()) {
			return null;
		}
		else {
			return listOfChannelConflicts;
		}
	}
	
	/**
	 * @param key This takes in a single sensor map key to check for conflicts and correct
	 * @return enabledSensors This returns the new set of enabled sensors, where any sensors which conflicts with sensorToCheck is disabled on the bitmap, so sensorToCheck can be accomodated (e.g. for Shimmer2 using ECG will disable EMG,GSR,..basically any daughter board)
	 * @return boolean 
	 *  
	 */
	public void sensorMapConflictCheckandCorrect(int key){
		
		if(mSensorMap.get(key) != null) {
			if(mSensorMap.get(key).mSensorDetails.mListOfSensorMapKeysConflicting != null) {
				for(Integer sensorMapKey:mSensorMap.get(key).mSensorDetails.mListOfSensorMapKeysConflicting) {
					if(mSensorMap.get(sensorMapKey) != null) {
						mSensorMap.get(sensorMapKey).mIsEnabled = false;
						if(mSensorMap.get(sensorMapKey).isDerivedChannel()) {
							mDerivedSensors &= ~mSensorMap.get(sensorMapKey).mDerivedSensorBitmapID;
						}
						setDefaultConfigForSensor(sensorMapKey, mSensorMap.get(sensorMapKey).mIsEnabled);
					}
				}
			}
		}
		
		sensorMapCheckandCorrectSensorDependencies();
		sensorMapCheckandCorrectHwDependencies();
	}

	
	private void sensorMapCheckandCorrectSensorDependencies() {
		//Cycle through any required sensors and update sensorMap channel enable values
		for(Integer sensorMapKey:mSensorMap.keySet()) {
			if(mSensorMap.get(sensorMapKey).mSensorDetails.mListOfSensorMapKeysRequired != null) {
				for(Integer requiredSensorKey:mSensorMap.get(sensorMapKey).mSensorDetails.mListOfSensorMapKeysRequired) {
					if(mSensorMap.get(requiredSensorKey).mIsEnabled == false) {
						mSensorMap.get(sensorMapKey).mIsEnabled = false;
						if(mSensorMap.get(sensorMapKey).isDerivedChannel()) {
							mDerivedSensors &= ~mSensorMap.get(sensorMapKey).mDerivedSensorBitmapID;
						}
						setDefaultConfigForSensor(sensorMapKey, mSensorMap.get(sensorMapKey).mIsEnabled);
						break;
					}
				}
				
			}
			
//			if (mHardwareVersion == HW_ID.SHIMMER_3){
//				//Exceptions for Shimmer3 ExG
//				
//				//TODO tidy below
//				// If ECG or ExG_Test (i.e., two ExG chips)
//				if(((sensorMapKey==Configuration.Shimmer3.SensorMapKey.ECG)
//						||(sensorMapKey==Configuration.Shimmer3.SensorMapKey.EXG_TEST)
//						||(sensorMapKey==Configuration.Shimmer3.SensorMapKey.EXG_CUSTOM)
//						||(sensorMapKey==Configuration.Shimmer3.SensorMapKey.EXG_RESPIRATION))&&(isSensorEnabled(sensorMapKey))) {
//					if(!(((isSensorEnabled(Configuration.Shimmer3.SensorMapKey.EXG1_16BIT))&&(isSensorEnabled(Configuration.Shimmer3.SensorMapKey.EXG2_16BIT)))
//							||((isSensorEnabled(Configuration.Shimmer3.SensorMapKey.EXG1_24BIT))&&(isSensorEnabled(Configuration.Shimmer3.SensorMapKey.EXG2_24BIT))))){
//						mSensorMap.get(sensorMapKey).mIsEnabled = false;
//					}
//					else {
//						
//					}
//				}
//				// Else if EMG (i.e., one ExG chip)
//				else if((sensorMapKey==Configuration.Shimmer3.SensorMapKey.EMG)&&(isSensorEnabled(sensorMapKey))) {
//					if(!((isSensorEnabled(Configuration.Shimmer3.SensorMapKey.EXG1_16BIT))||(isSensorEnabled(Configuration.Shimmer3.SensorMapKey.EXG1_24BIT)))){
//						mSensorMap.get(sensorMapKey).mIsEnabled = false;
//					}
//				}	
//			}

		}
	}
	

	private void sensorMapCheckandCorrectHwDependencies() {
		for(Integer sensorMapKey:mSensorMap.keySet()) {
			if(mSensorMap.get(sensorMapKey).mSensorDetails.mListOfCompatibleVersionInfo != null) {
				if(!checkIfVersionCompatible(mSensorMap.get(sensorMapKey).mSensorDetails.mListOfCompatibleVersionInfo)) {
					mSensorMap.get(sensorMapKey).mIsEnabled = false;
					if(mSensorMap.get(sensorMapKey).isDerivedChannel()) {
						mDerivedSensors &= ~mSensorMap.get(sensorMapKey).mDerivedSensorBitmapID;
					}
					setDefaultConfigForSensor(sensorMapKey, mSensorMap.get(sensorMapKey).mIsEnabled);
				}
			}
		}
	}
	

	//TODO set defaults when ").mIsEnabled = false)" is set manually in the code
	private void setDefaultConfigForSensor(int sensorMapKey, boolean state) {
		if(sensorMapKey==Configuration.Shimmer3.SensorMapKey.LSM303DLHC_ACCEL) {
			setDefaultLsm303dlhcAccelSensorConfig(state);
		}
		else if(sensorMapKey==Configuration.Shimmer3.SensorMapKey.LSM303DLHC_MAG) {
			setDefaultLsm303dlhcMagSensorConfig(state);
		}
		else if(sensorMapKey == Configuration.Shimmer3.SensorMapKey.MPU9150_GYRO){
			setDefaultMpu9150GyroSensorConfig(state);
		}
		else if(sensorMapKey == Configuration.Shimmer3.SensorMapKey.MPU9150_ACCEL){
			setDefaultMpu9150AccelSensorConfig(state);
		}
		else if(sensorMapKey == Configuration.Shimmer3.SensorMapKey.MPU9150_MAG){
			setMPU9150MagRateFromFreq(mShimmerSamplingRate);
		}
		
		else if(sensorMapKey==Configuration.Shimmer3.SensorMapKey.BMP180_PRESSURE) {
			setDefaultBmp180PressureSensorConfig(state);
		}
		else if(sensorMapKey==Configuration.Shimmer3.SensorMapKey.GSR) {
			setDefaultGsrSensorConfig(state);
		}
		else if((sensorMapKey == Configuration.Shimmer3.SensorMapKey.EXG_RESPIRATION)
				|| (sensorMapKey == Configuration.Shimmer3.SensorMapKey.ECG)
				|| (sensorMapKey == Configuration.Shimmer3.SensorMapKey.EMG)
				|| (sensorMapKey == Configuration.Shimmer3.SensorMapKey.EXG_TEST)
				|| (sensorMapKey == Configuration.Shimmer3.SensorMapKey.EXG_CUSTOM)) {
			
			// If ExG sensor enabled, set default settings for that
			// sensor. Otherwise set the default ECG configuration.
			if(state) { 
				if(sensorMapKey == Configuration.Shimmer3.SensorMapKey.EXG_RESPIRATION) {
					setDefaultRespirationConfiguration();
				}
				else if(sensorMapKey == Configuration.Shimmer3.SensorMapKey.ECG) {
//					System.err.println("SET ECG CHANNEL");
					setDefaultECGConfiguration();
				}
				else if(sensorMapKey == Configuration.Shimmer3.SensorMapKey.EMG) {
					setDefaultEMGConfiguration();
				}
				else if(sensorMapKey == Configuration.Shimmer3.SensorMapKey.EXG_TEST) {
					setEXGTestSignal();
				}
				else if(sensorMapKey == Configuration.Shimmer3.SensorMapKey.EXG_CUSTOM) {
					setEXGCustom();
				}
			}
			else {
				if(!checkIfOtherExgChannelEnabled()) {
//					System.err.println("CLEAR EXG CHANNEL");
					clearExgConfig();
				}
			}
		}
		else if(mListOfMplChannels.contains(sensorMapKey)){
			if(!checkIfAnyOtherMplChannelEnabled(sensorMapKey)) {
				setDefaultMpu9150MplSensorConfig(state);
			}
			else {
//				setMPU9150GyroAccelRateFromFreq(mShimmerSamplingRate);
//				if (mSensorMap.get(Configuration.Shimmer3.SensorMapKey.MPU9150_GYRO) != null) {
//					if(mSensorMap.get(Configuration.Shimmer3.SensorMapKey.MPU9150_GYRO).mIsEnabled) {
//						return true;
//					}
//				}
//				if(mSensorMap.get(Configuration.Shimmer3.SensorMapKey.MPU9150_ACCEL) != null) {
//					if(mSensorMap.get(Configuration.Shimmer3.SensorMapKey.MPU9150_ACCEL).mIsEnabled) {
//						return true;
//					}
//				}
			}
		}
		
	}
	
	private void setDefaultBmp180PressureSensorConfig(boolean state) {
		if(state) {
		}
		else {
			mPressureResolution = 0;
		}
	}
	
	private void setDefaultLsm303dlhcAccelSensorConfig(boolean state) {
		if(state) {
			setLowPowerAccelWR(false);
		}
		else {
			mAccelRange = 0;
			setLowPowerAccelWR(true);
//			setLSM303AccelRateFromFreq(mShimmerSamplingRate);
		}
	}

	private void setDefaultLsm303dlhcMagSensorConfig(boolean state) {
		if(state) {
			setLowPowerMag(false);
		}
		else {
			mMagRange=1;
			setLowPowerMag(true);
//			setLSM303MagRateFromFreq(mShimmerSamplingRate);
		}
	}

	private void setDefaultGsrSensorConfig(boolean state) {
		if(state) {
		}
		else {
			mGSRRange=4;
		}
	}

	private void setDefaultMpu9150GyroSensorConfig(boolean state) {
		if(!checkIfAnyMplChannelEnabled()) {
			if(!isSensorEnabled(Configuration.Shimmer3.SensorMapKey.MPU9150_ACCEL)) {
				if(state) {
					setLowPowerGyro(false);
				}
				else {
					setLowPowerGyro(true);
				}
			}
			
			if(!state){
				mGyroRange=1;
			}
		}
		else {
			mGyroRange=3; // 2000dps
		}
	}
	
	private void setDefaultMpu9150AccelSensorConfig(boolean state) {
		if(!checkIfAnyMplChannelEnabled()) {
			if(!isSensorEnabled(Configuration.Shimmer3.SensorMapKey.MPU9150_GYRO)) {
				if(state) {
					setLowPowerGyro(false);
				}
				else {
					setLowPowerGyro(true);
				}
			}
			
			if(!state){
				mMPU9150AccelRange = 0; //=2g
			}
		}
		else {
			mMPU9150AccelRange = 0; //=2g
		}
	}
	
	private void setDefaultMpu9150MplSensorConfig(boolean state) {
		if(state){
			mMPU9150DMP = 1;
			mMPLEnable = 1;
			mMPU9150LPF = 1; // 188Hz
			mMPU9150MotCalCfg = 1; // Fast Calibration
			mMPLGyroCalTC = 1;
			mMPLVectCompCal = 1;
			mMPLMagDistCal = 1;
			mMPLSensorFusion = 0;
			
//			//Gyro rate can not be set to 250dps when DMP is on
//			if(mGyroRange==0){
//				mGyroRange=1;
//			}
			
			//force gyro range to be 2000dps and accel range to be +-2g - others untested
			mGyroRange=3; // 2000dps
			mMPU9150AccelRange= 0; // 2g
			
			setLowPowerGyro(false);
			setMPU9150MagRateFromFreq(mShimmerSamplingRate);
			setMPU9150MplRateFromFreq(mShimmerSamplingRate);
		}
		else {
			mMPU9150DMP = 0;
			mMPLEnable = 0;
			mMPU9150LPF = 0;
			mMPU9150MotCalCfg = 0;
			mMPLGyroCalTC = 0;
			mMPLVectCompCal = 0;
			mMPLMagDistCal = 0;
			mMPLSensorFusion = 0;
			
			if(checkIfAMpuGyroOrAccelEnabled()){
				setMPU9150GyroAccelRateFromFreq(mShimmerSamplingRate);
			}
			else {
				setLowPowerGyro(true);
			}
			
			setMPU9150MagRateFromFreq(mShimmerSamplingRate);
			setMPU9150MplRateFromFreq(mShimmerSamplingRate);
		}
	}
	
	private boolean checkIfAMpuGyroOrAccelEnabled(){
		if(isSensorEnabled(Configuration.Shimmer3.SensorMapKey.MPU9150_GYRO)) {
			return true;
		}
		if(isSensorEnabled(Configuration.Shimmer3.SensorMapKey.MPU9150_ACCEL)) {
			return true;
		}
//		if(mSensorMap.get(Configuration.Shimmer3.SensorMapKey.MPU9150_MAG) != null) {
//			if(mSensorMap.get(Configuration.Shimmer3.SensorMapKey.MPU9150_MAG).mIsEnabled) {
//				return true;
//			}
//		}
		return false;
	}	
	
	private boolean checkIfAnyOtherMplChannelEnabled(int sensorMapKey){
		if (mHardwareVersion==HW_ID.SHIMMER_3 || mHardwareVersion==HW_ID.SHIMMER_GQ) {
			if(mSensorMap.keySet().size()>0){
				
				for(int key:mListOfMplChannels){
					if(key==sensorMapKey){
						continue;
					}
					if(isSensorEnabled(key)) {
						return true;
					}
				}
			}
		}
		return false;
	}
			
	protected boolean checkIfAnyMplChannelEnabled(){
		if (mHardwareVersion==HW_ID.SHIMMER_3 || mHardwareVersion==HW_ID.SHIMMER_GQ) {
			if(mSensorMap.keySet().size()>0){
				
				for(int key:mListOfMplChannels){
					if(isSensorEnabled(key)) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	private boolean checkIfOtherExgChannelEnabled() {
		for(Integer sensorMapKey:mSensorMap.keySet()) {
			if(mSensorMap.get(sensorMapKey).mIsEnabled) {
				if((sensorMapKey==Configuration.Shimmer3.SensorMapKey.ECG)
					||(sensorMapKey==Configuration.Shimmer3.SensorMapKey.EMG)
					||(sensorMapKey==Configuration.Shimmer3.SensorMapKey.EXG_TEST)
					||(sensorMapKey==Configuration.Shimmer3.SensorMapKey.EXG_CUSTOM)
					||(sensorMapKey==Configuration.Shimmer3.SensorMapKey.EXG_RESPIRATION) ){
//					||(sensorMapKey==Configuration.Shimmer3.SensorMapKey.EXG1_16BIT)
//					||(sensorMapKey==Configuration.Shimmer3.SensorMapKey.EXG2_16BIT)
//					||(sensorMapKey==Configuration.Shimmer3.SensorMapKey.EXG1_24BIT)
//					||(sensorMapKey==Configuration.Shimmer3.SensorMapKey.EXG2_24BIT)) {
					return true;
				}
			}
		}
		return false;
	}
	
	private void internalCheckExgModeAndUpdateSensorMap(){
		if(mSensorMap!=null){
			if(mHardwareVersion==HW_ID.SHIMMER_3){
//				if((mIsExg1_24bitEnabled||mIsExg2_24bitEnabled||mIsExg1_16bitEnabled||mIsExg2_16bitEnabled)){
//				if((isSensorEnabled(Configuration.Shimmer3.SensorMapKey.EXG1_16BIT))
//						||(isSensorEnabled(Configuration.Shimmer3.SensorMapKey.EXG2_16BIT))
//						||(isSensorEnabled(Configuration.Shimmer3.SensorMapKey.EXG1_24BIT))
//						||(isSensorEnabled(Configuration.Shimmer3.SensorMapKey.EXG2_24BIT))) {
					if(isEXGUsingDefaultRespirationConfiguration()) { // Do Respiration check first
						mSensorMap.get(Configuration.Shimmer3.SensorMapKey.ECG).mIsEnabled = false;
						mSensorMap.get(Configuration.Shimmer3.SensorMapKey.EMG).mIsEnabled = false;
						mSensorMap.get(Configuration.Shimmer3.SensorMapKey.EXG_TEST).mIsEnabled = false;
						mSensorMap.get(Configuration.Shimmer3.SensorMapKey.EXG_CUSTOM).mIsEnabled = false;
						mSensorMap.get(Configuration.Shimmer3.SensorMapKey.EXG_RESPIRATION).mIsEnabled = true;
					}
					else if(isEXGUsingDefaultECGConfiguration()) {
						mSensorMap.get(Configuration.Shimmer3.SensorMapKey.ECG).mIsEnabled = true;
						mSensorMap.get(Configuration.Shimmer3.SensorMapKey.EMG).mIsEnabled = false;
						mSensorMap.get(Configuration.Shimmer3.SensorMapKey.EXG_TEST).mIsEnabled = false;
						mSensorMap.get(Configuration.Shimmer3.SensorMapKey.EXG_CUSTOM).mIsEnabled = false;
						mSensorMap.get(Configuration.Shimmer3.SensorMapKey.EXG_RESPIRATION).mIsEnabled = false;
					}
					else if(isEXGUsingDefaultEMGConfiguration()) {
						mSensorMap.get(Configuration.Shimmer3.SensorMapKey.ECG).mIsEnabled = false;
						mSensorMap.get(Configuration.Shimmer3.SensorMapKey.EMG).mIsEnabled = true;
						mSensorMap.get(Configuration.Shimmer3.SensorMapKey.EXG_TEST).mIsEnabled = false;
						mSensorMap.get(Configuration.Shimmer3.SensorMapKey.EXG_CUSTOM).mIsEnabled = false;
						mSensorMap.get(Configuration.Shimmer3.SensorMapKey.EXG_RESPIRATION).mIsEnabled = false;
					}
					else if(isEXGUsingDefaultTestSignalConfiguration()){
						mSensorMap.get(Configuration.Shimmer3.SensorMapKey.ECG).mIsEnabled = false;
						mSensorMap.get(Configuration.Shimmer3.SensorMapKey.EMG).mIsEnabled = false;
						mSensorMap.get(Configuration.Shimmer3.SensorMapKey.EXG_TEST).mIsEnabled = true;
						mSensorMap.get(Configuration.Shimmer3.SensorMapKey.EXG_CUSTOM).mIsEnabled = false;
						mSensorMap.get(Configuration.Shimmer3.SensorMapKey.EXG_RESPIRATION).mIsEnabled = false;
					}
					else if(isEXGUsingCustomSignalConfiguration()){
						mSensorMap.get(Configuration.Shimmer3.SensorMapKey.ECG).mIsEnabled = false;
						mSensorMap.get(Configuration.Shimmer3.SensorMapKey.EMG).mIsEnabled = false;
						mSensorMap.get(Configuration.Shimmer3.SensorMapKey.EXG_TEST).mIsEnabled = false;
						mSensorMap.get(Configuration.Shimmer3.SensorMapKey.EXG_CUSTOM).mIsEnabled = true;
						mSensorMap.get(Configuration.Shimmer3.SensorMapKey.EXG_RESPIRATION).mIsEnabled = false;
					}
					else {
//						setSensorEnabledState(Configuration.Shimmer3.SensorMapKey.ECG, false);
//						setSensorEnabledState(Configuration.Shimmer3.SensorMapKey.EMG, false);
//						setSensorEnabledState(Configuration.Shimmer3.SensorMapKey.EXG_TEST, false);
//						setSensorEnabledState(Configuration.Shimmer3.SensorMapKey.EXG_CUSTOM, false);
//						setSensorEnabledState(Configuration.Shimmer3.SensorMapKey.EXG_RESPIRATION, false);
						mSensorMap.get(Configuration.Shimmer3.SensorMapKey.ECG).mIsEnabled = false;
						mSensorMap.get(Configuration.Shimmer3.SensorMapKey.EMG).mIsEnabled = false;
						mSensorMap.get(Configuration.Shimmer3.SensorMapKey.EXG_TEST).mIsEnabled = false;
						mSensorMap.get(Configuration.Shimmer3.SensorMapKey.EXG_CUSTOM).mIsEnabled = false;
						mSensorMap.get(Configuration.Shimmer3.SensorMapKey.EXG_RESPIRATION).mIsEnabled = false;
					}
				}
//			}
		}
	}
	
	 /**
	 * @return the InfoMem byte size. HW and FW version needs to be set first for this to operate correctly.
	 */
	public int getExpectedInfoMemByteLength() {
		createInfoMemLayoutObjectIfNeeded();
		return mInfoMemLayout.mInfoMemSize;
	}
	
	public void createInfoMemLayoutObjectIfNeeded(){
		boolean create = false;
		if(mInfoMemLayout==null){
			create = true;
		}
		else if(mInfoMemLayout.isDifferent(mFirmwareIdentifier, mFirmwareVersionMajor, mFirmwareVersionMinor, mFirmwareVersionInternal)){
			create = true;
		}
		
		if(create){
			mInfoMemLayout = new InfoMemLayout(mFirmwareIdentifier, mFirmwareVersionMajor, mFirmwareVersionMinor, mFirmwareVersionInternal);
		}
	}
	
	 /**
	 * @return the mConfigTime
	 */
	public long getConfigTime() {
		return mConfigTime;
	}

	 /**
	 * @return the mConfigTime in a parsed String format (yyyy-MM-dd hh:MM:ss)
	 */
	public String getConfigTimeParsed() {
		return UtilShimmer.convertSecondsToDateString(mConfigTime);
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
	
	public int getMPU9150DMP() {
		return mMPU9150DMP;
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
	 * @return the mTrialName
	 */
	public String getTrialName() {
		return mTrialName;
	}

	/**
	 * @return the mTrialNumberOfShimmers
	 */
	public int getTrialNumberOfShimmers() {
		return mTrialNumberOfShimmers;
	}
	
	/**
	 * @return the mTrialDurationEstimated
	 */
	public int getTrialDurationEstimated() {
		return mTrialDurationEstimated;
	}

	/**
	 * @return the mTrialDurationMaximum
	 */
	public int getTrialDurationMaximum() {
		return mTrialDurationMaximum;
	}

	/**
	 * @return the mLSM303DigitalAccelLPM
	 */
	public boolean isLSM303DigitalAccelLPM() {
		return mLowPowerAccelWR;
	}
	
	/**
	 * @return true if ExG respiration detection frequency is 32kHz and false if 64kHz
	 */
	public boolean isExgRespirationDetectFreq32kHz() {
		if(getEXG2RespirationDetectFreq()==0)
//		if(mEXG2RespirationDetectFreq==0)
			return true;
		else
			return false;
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
	public int getLSM303MagRate() {
		return mLSM303MagRate;
	}

	/**
	 * @return the mLSM303DigitalAccelRate
	 */
	public int getLSM303DigitalAccelRate() {
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
		return mTrialId;
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
	 * @return the mSyncWhenLogging
	 */
	public int getSyncWhenLogging() {
		return mSyncWhenLogging;
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
		//System.out.println("mExGResolution: " +mExGResolution);
		return mExGResolution;
	}

	/**
	 * @return the mShimmerVersion
	 */
	public int getHardwareVersion() {
		return mHardwareVersion;
	}
	
	
	//TODO: MN -> move gets to ShimmerObject (could be duplicated there already)

	public int getLowPowerAccelEnabled(){
		if(mLowPowerAccelWR)
			return 1;
		else
			return 0;
	}

	public int getLowPowerGyroEnabled() {
		if(mLowPowerGyro)
			return 1;
		else
			return 0;
	}

	public int getLowPowerMagEnabled() {
		if(mLowPowerMag)
			return 1;
		else
			return 0;
	}
	
	
	public boolean getInitialized(){
		return mIsInitialised;
	}

	
	/**
	 * Get the 5V Reg. Only supported on Shimmer2/2R.
	 * @return 0 in case the 5V Reg is disableb, 1 in case the 5V Reg is enabled, and -1 in case the device doesn't support this feature
	 */
	public int get5VReg(){
		if(mHardwareVersion!=HW_ID.SHIMMER_3){
			if((mConfigByte0 & (byte)128)!=0) {
				//then set ConfigByte0 at bit position 7
				return 1;
			} 
			else {
				return 0;
			}
		}
		else{
			return -1;
		}
	}
	
	public int getCurrentLEDStatus() {
		return mCurrentLEDStatus;
	}

	public int getBaudRate(){
		return mBluetoothBaudRate;
	}
	
	public int getReferenceElectrode(){
		return mEXGReferenceElectrode;
	}
	
	public int getLeadOffDetectionMode(){
		return mLeadOffDetectionMode;
	}
	
	public int getLeadOffDetectionCurrent(){
		return mEXGLeadOffDetectionCurrent;
	}
	
	public int getLeadOffComparatorTreshold(){
		return mEXGLeadOffComparatorTreshold;
	}
	
	public int getExGComparatorsChip1(){
		return mEXG1Comparators;
	}
	
	public int getExGComparatorsChip2(){
		return mEXG2Comparators;
	}
	
	public int getShimmerVersion(){
		return mHardwareVersion;
	}

    /** Returns true if device is streaming (Bluetooth)
     * @return
     */
    public boolean isStreaming(){
    	return mIsStreaming;
    }
    
    //region --------- IS+something FUNCTIONS --------- 
    
    public boolean isLowPowerMagEnabled(){
		return mLowPowerMag;
	}
    
    public boolean isGyroOnTheFlyCalEnabled(){
		return mEnableOntheFlyGyroOVCal;
	}

	public boolean is3DOrientatioEnabled(){
		return mOrientationEnabled;
	}
    
	/**Only used for LogAndStream
	 * @return
	 */
	public boolean isSensing(){
		return mIsSensing;
	}
	
	public boolean isLowPowerAccelEnabled() {
		return mLowPowerAccelWR;
	}

	public boolean isLowPowerGyroEnabled() {
		return mLowPowerGyro;
	}
	
	public boolean isUsingDefaultLNAccelParam(){
		return mDefaultCalibrationParametersAccel;
	}
	
	public boolean isUsingDefaultAccelParam(){
		return mDefaultCalibrationParametersAccel;
	}
	
	public boolean isUsingDefaultWRAccelParam(){
		return mDefaultCalibrationParametersDigitalAccel; 
	}

	public boolean isUsingDefaultGyroParam(){
		return mDefaultCalibrationParametersGyro;
	}
	
	public boolean isUsingDefaultMagParam(){
		return mDefaultCalibrationParametersMag;
	}
	
	public boolean isUsingDefaultECGParam(){
		return mDefaultCalibrationParametersECG;
	}
	
	public boolean isUsingDefaultEMGParam(){
		return mDefaultCalibrationParametersEMG;
	}
	
    

	/**
	 * @return the mSensorMap
	 */
	public Map<Integer, SensorEnabledDetails> getSensorMap() {
		return mSensorMap;
	}
	
	/**
	 * @return the mChannelMap
	 */
	public Map<String, ChannelDetails> getChannelMap() {
		return mChannelMap;
	}

	/**
	 * @return the mAlgorithmChannelsMap
	 */
	public Map<String, AlgorithmDetailsNew> getAlgorithmChannelsMap() {
		return mAlgorithmChannelsMap;
	}

	/**
	 * @return the mConfigOptionsMap
	 */
	public Map<String, SensorConfigOptionDetails> getConfigOptionsMap() {
		return mConfigOptionsMap;
	}

	/**
	 * @return the mSensorGroupingMap
	 */
	public Map<String, SensorGroupingDetails> getSensorGroupingMap() {
		return mSensorGroupingMap;
	}

	public List<AlgorithmDetailsNew> getListOfSupportedAlgorithmChannels() {
		
		List<AlgorithmDetailsNew> listOfSupportAlgorihmChannels = new ArrayList<AlgorithmDetailsNew>();
		parentLoop:
    	for(AlgorithmDetailsNew algorithmDetails:mAlgorithmChannelsMap.values()) {
    		if(algorithmDetails.mSensorCheckMethod == SENSOR_CHECK_METHOD.ANY){
        		for(Integer sensorMapKey:algorithmDetails.mListOfRequiredSensors){
        			if(mSensorMap.containsKey(sensorMapKey)){
        				if(mSensorMap.get(sensorMapKey).mIsEnabled){
        					listOfSupportAlgorihmChannels.add(algorithmDetails);
        					continue parentLoop;
        				}
        			}
        		}
    		}
    		else if(algorithmDetails.mSensorCheckMethod == SENSOR_CHECK_METHOD.ALL){
        		for(Integer sensorMapKey:algorithmDetails.mListOfRequiredSensors){
        			if(!mSensorMap.containsKey(sensorMapKey)){
    					continue parentLoop;
        			}
        			else{
        				if(!mSensorMap.get(sensorMapKey).mIsEnabled){
        					continue parentLoop;
        				}
        			}
        			
        			//made it to past the last sensor
        			if(sensorMapKey==algorithmDetails.mListOfRequiredSensors.get(algorithmDetails.mListOfRequiredSensors.size()-1)){
    					listOfSupportAlgorihmChannels.add(algorithmDetails);
        			}
        		}
    		}
    		
    	}

		// TODO Auto-generated method stub
		return listOfSupportAlgorihmChannels;
	}

	/**
	 * @return the mAlgorithmGroupingMap
	 */
	public Map<String, List<String>> getAlgorithmGroupingMap() {
		return mAlgorithmGroupingMap;
	}
	
	//TODO: finish. Similar approach to above in getListOfSupportedAlgorithmChannels()
	public List<String> getListOfSupportedAlgorithmGroups() {
		return new ArrayList<String>();
	}
	
	/**
	 * @return the mEXG1RateSetting
	 */
	public int getEXG1RateSetting() {
		return getExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1, EXG_SETTINGS.REG1_DATA_RATE);
	}

	/**
	 * @return the mEXGReferenceElectrode
	 */
	public int getEXGReferenceElectrode() {
		return mExGConfigBytesDetails.getEXGReferenceElectrode();
	}
	
	/**
	 * @return the mEXGLeadOffDetectionCurrent
	 */
	public int getEXGLeadOffDetectionCurrent() {
		return getExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1, EXG_SETTINGS.REG3_LEAD_OFF_CURRENT);
	}


	/**
	 * @return the mEXGLeadOffComparatorTreshold
	 */
	public int getEXGLeadOffComparatorTreshold() {
		return getExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1, EXG_SETTINGS.REG3_COMPARATOR_THRESHOLD);
	}

	/**
	 * @return the mEXG2RespirationDetectFreq
	 */
	public int getEXG2RespirationDetectFreq() {
		return getExgPropertySingleChip(EXG_CHIP_INDEX.CHIP2, EXG_SETTINGS.REG10_RESPIRATION_CONTROL_FREQUENCY);
	}

	/**
	 * @return the mEXG2RespirationDetectPhase
	 */
	public int getEXG2RespirationDetectPhase() {
		return getExgPropertySingleChip(EXG_CHIP_INDEX.CHIP2, EXG_SETTINGS.REG9_RESPIRATION_PHASE);
	}
	
	public double getShimmerSamplingRate(){
		return mShimmerSamplingRate; 
	}

	/**
	 * @return the mEXG1RegisterArray
	 */
	public byte[] getEXG1RegisterArray() {
		return mEXG1RegisterArray;
	}

	/**
	 * @return the mEXG2RegisterArray
	 */
	public byte[] getEXG2RegisterArray() {
		return mEXG2RegisterArray;
	}

	protected void setExGGainSetting(EXG_CHIP_INDEX chipID,  int channel, int value){
		if(chipID==EXG_CHIP_INDEX.CHIP1){
			if(channel==1){
				setExgPropertySingleChipValue(EXG_CHIP_INDEX.CHIP1,EXG_SETTINGS.REG4_CHANNEL_1_PGA_GAIN,(int)value);
			}
			else if(channel==2){
				setExgPropertySingleChipValue(EXG_CHIP_INDEX.CHIP1,EXG_SETTINGS.REG5_CHANNEL_2_PGA_GAIN,(int)value);
			}
		}
		else if(chipID==EXG_CHIP_INDEX.CHIP2){
			if(channel==1){
				setExgPropertySingleChipValue(EXG_CHIP_INDEX.CHIP2,EXG_SETTINGS.REG4_CHANNEL_1_PGA_GAIN,(int)value);
			}
			else if(channel==2){
				setExgPropertySingleChipValue(EXG_CHIP_INDEX.CHIP2,EXG_SETTINGS.REG5_CHANNEL_2_PGA_GAIN,(int)value);
			}
		}
	}

	protected void setExGGainSetting(int value){
		setExgPropertySingleChipValue(EXG_CHIP_INDEX.CHIP1,EXG_SETTINGS.REG4_CHANNEL_1_PGA_GAIN,(int)value);
		setExgPropertySingleChipValue(EXG_CHIP_INDEX.CHIP1,EXG_SETTINGS.REG5_CHANNEL_2_PGA_GAIN,(int)value);
		setExgPropertySingleChipValue(EXG_CHIP_INDEX.CHIP2,EXG_SETTINGS.REG4_CHANNEL_1_PGA_GAIN,(int)value);
		setExgPropertySingleChipValue(EXG_CHIP_INDEX.CHIP2,EXG_SETTINGS.REG5_CHANNEL_2_PGA_GAIN,(int)value);
	}
	
	protected void setExGResolution(int i){
		mExGResolution = i;
		
		if(i==0) { // 16-bit
			//this is needed so the BT can write the correct sensor
			/*if ((mEnabledSensors & SENSOR_EXG1_16BIT)>0){
				mEnabledSensors=mEnabledSensors^SENSOR_EXG1_16BIT;
			}
			if ((mEnabledSensors & SENSOR_EXG2_16BIT)>0){
				mEnabledSensors=mEnabledSensors^SENSOR_EXG2_16BIT;
			}
			mEnabledSensors = SENSOR_EXG1_16BIT|SENSOR_EXG2_16BIT;
			*/
			//
			
			if(mIsExg1_24bitEnabled){
				mIsExg1_24bitEnabled = false;
				mIsExg1_16bitEnabled = true;
			}
			if(mIsExg2_24bitEnabled){
				mIsExg2_24bitEnabled = false;
				mIsExg2_16bitEnabled = true;
			}
		}
		else if(i==1) { // 24-bit
			/*if ((mEnabledSensors & SENSOR_EXG1_24BIT)>0){
				mEnabledSensors=mEnabledSensors^SENSOR_EXG1_24BIT;
			}
			if ((mEnabledSensors & SENSOR_EXG2_24BIT)>0){
				mEnabledSensors=mEnabledSensors^SENSOR_EXG2_24BIT;
			}
			mEnabledSensors = SENSOR_EXG1_24BIT|SENSOR_EXG2_24BIT;
			*/
			if(mIsExg1_16bitEnabled){
				mIsExg1_24bitEnabled = true;
				mIsExg1_16bitEnabled = false;
			}
			if(mIsExg2_16bitEnabled){
				mIsExg2_24bitEnabled = true;
				mIsExg2_16bitEnabled = false;
			}
		}
		
		updateEnabledSensorsFromExgResolution();
		
//		if(mSensorMap != null) {
//			if(i==0) { // 16-bit
//				if(isSensorEnabled(Configuration.Shimmer3.SensorMapKey.EXG1_24BIT)) {
//					mSensorMap.get(Configuration.Shimmer3.SensorMapKey.EXG1_24BIT).mIsEnabled = false;
//					mSensorMap.get(Configuration.Shimmer3.SensorMapKey.EXG1_16BIT).mIsEnabled = true;
//				}
//				if(isSensorEnabled(Configuration.Shimmer3.SensorMapKey.EXG2_24BIT)) {
//					mSensorMap.get(Configuration.Shimmer3.SensorMapKey.EXG2_24BIT).mIsEnabled = false;
//					mSensorMap.get(Configuration.Shimmer3.SensorMapKey.EXG2_16BIT).mIsEnabled = true;
//				}
//			}
//			else if(i==1) { // 24-bit
//				if(isSensorEnabled(Configuration.Shimmer3.SensorMapKey.EXG1_16BIT)) {
//					mSensorMap.get(Configuration.Shimmer3.SensorMapKey.EXG1_16BIT).mIsEnabled = false;
//					mSensorMap.get(Configuration.Shimmer3.SensorMapKey.EXG1_24BIT).mIsEnabled = true;
//				}
//				if(isSensorEnabled(Configuration.Shimmer3.SensorMapKey.EXG2_16BIT)) {
//					mSensorMap.get(Configuration.Shimmer3.SensorMapKey.EXG2_16BIT).mIsEnabled = false;
//					mSensorMap.get(Configuration.Shimmer3.SensorMapKey.EXG2_24BIT).mIsEnabled = true;
//				}
//			}
//		}
	}
	
	public int getExGGainSetting(){
//		mEXG1CH1GainSetting = i;
//		mEXG1CH2GainSetting = i;
//		mEXG2CH1GainSetting = i;
//		mEXG2CH2GainSetting = i;
//		System.out.println("SlotDetails: getExGGain - Setting: = " + mEXG1CH1GainSetting + " - Value = " + mEXG1CH1GainValue);
		return this.mEXG1CH1GainSetting;
	}
	
	/** Note: Doesn't update the Sensor Map
	 * @param mEXG1RegisterArray the mEXG1RegisterArray to set
	 */
	protected void setEXG1RegisterArray(byte[] EXG1RegisterArray) {
		this.mEXG1RegisterArray = EXG1RegisterArray;
		exgBytesGetConfigFrom(1, EXG1RegisterArray);
	}

	/** Note: Doesn't update the Sensor Map
	 * @param mEXG2RegisterArray the mEXG2RegisterArray to set
	 */
	protected void setEXG2RegisterArray(byte[] EXG2RegisterArray) {
		this.mEXG2RegisterArray = EXG2RegisterArray;
		exgBytesGetConfigFrom(2, EXG2RegisterArray);
	}


	/**
	 *This can only be used for Shimmer3 devices (EXG) 
	 *When a enable configuration is loaded, the advanced ExG configuration is removed, so it needs to be set again
	 */
	 protected void enableDefaultECGConfiguration() {
		 if (mHardwareVersion==HW_ID.SHIMMER_3){
			setDefaultECGConfiguration();
		 }
	}

	/**
	 * This can only be used for Shimmer3 devices (EXG)
	 * When a enable configuration is loaded, the advanced ExG configuration is removed, so it needs to be set again
	 */
	protected void enableDefaultEMGConfiguration(){
		if (mHardwareVersion==HW_ID.SHIMMER_3){
			setDefaultEMGConfiguration();
		}
	}

	/**
	 * This can only be used for Shimmer3 devices (EXG). Enables the test signal (square wave) of both EXG chips, to use, both EXG1 and EXG2 have to be enabled
	 */
	protected void enableEXGTestSignal(){
		if (mHardwareVersion==HW_ID.SHIMMER_3){
			setEXGTestSignal();
		}
	}

	/**
	 * @param valueToSet the valueToSet to set
	 */
	protected void setEXGRateSetting(int valueToSet) {
		setExgPropertySingleChipValue(EXG_CHIP_INDEX.CHIP1,EXG_SETTINGS.REG1_DATA_RATE,(int)valueToSet);
		setExgPropertySingleChipValue(EXG_CHIP_INDEX.CHIP2,EXG_SETTINGS.REG1_DATA_RATE,(int)valueToSet);
	}
	
	/**
	 * @param valueToSet the valueToSet to set
	 */
	protected void setEXGRateSetting(EXG_CHIP_INDEX chipID, int valueToSet) {
		if(chipID==EXG_CHIP_INDEX.CHIP1){
			setExgPropertySingleChipValue(EXG_CHIP_INDEX.CHIP1,EXG_SETTINGS.REG1_DATA_RATE,(int)valueToSet);
		}
		else if(chipID==EXG_CHIP_INDEX.CHIP2){
			setExgPropertySingleChipValue(EXG_CHIP_INDEX.CHIP2,EXG_SETTINGS.REG1_DATA_RATE,(int)valueToSet);
		}
	}

	/**
	 * @param mEXGReferenceElectrode the mEXGReferenceElectrode to set
	 */
	protected void setEXGReferenceElectrode(int valueToSet) {
		setExgPropertySingleChipValue(EXG_CHIP_INDEX.CHIP1,EXG_SETTINGS.REG6_CH2_RLD_NEG_INPUTS,((valueToSet&0x08) == 0x08)? 1:0);
		setExgPropertySingleChipValue(EXG_CHIP_INDEX.CHIP1,EXG_SETTINGS.REG6_CH2_RLD_POS_INPUTS,((valueToSet&0x04) == 0x04)? 1:0);
		setExgPropertySingleChipValue(EXG_CHIP_INDEX.CHIP1,EXG_SETTINGS.REG6_CH1_RLD_NEG_INPUTS,((valueToSet&0x02) == 0x02)? 1:0);
		setExgPropertySingleChipValue(EXG_CHIP_INDEX.CHIP1,EXG_SETTINGS.REG6_CH1_RLD_POS_INPUTS,((valueToSet&0x01) == 0x01)? 1:0);
	}

	protected void setEXGLeadOffCurrentMode(int mode){
		if(mode==0){//Off
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.LEAD_OFF_FREQUENCY.DC);
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.LEAD_OFF_COMPARATORS.OFF);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1, EXG_SETTING_OPTIONS.RLD_LEAD_OFF_SENSE_FUNCTION.OFF);
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.LEAD_OFF_DETECT_NEG_INPUTS_CH2.OFF);
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.LEAD_OFF_DETECT_POS_INPUTS_CH2.OFF);
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.LEAD_OFF_DETECT_NEG_INPUTS_CH1.OFF);
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.LEAD_OFF_DETECT_POS_INPUTS_CH1.OFF);
			if(isEXGUsingDefaultEMGConfiguration()){
				setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP2, EXG_SETTING_OPTIONS.POWER_DOWN_CH2.POWER_DOWN);
			}
		}
		else if(mode==1){//DC Current
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.LEAD_OFF_FREQUENCY.DC);
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.LEAD_OFF_COMPARATORS.ON);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1, EXG_SETTING_OPTIONS.RLD_LEAD_OFF_SENSE_FUNCTION.ON);
			
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1, EXG_SETTING_OPTIONS.LEAD_OFF_DETECT_NEG_INPUTS_CH2.OFF);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1, EXG_SETTING_OPTIONS.LEAD_OFF_DETECT_POS_INPUTS_CH2.ON);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1, EXG_SETTING_OPTIONS.LEAD_OFF_DETECT_NEG_INPUTS_CH1.ON);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1, EXG_SETTING_OPTIONS.LEAD_OFF_DETECT_POS_INPUTS_CH1.ON);

			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP2, EXG_SETTING_OPTIONS.LEAD_OFF_DETECT_NEG_INPUTS_CH2.OFF);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP2, EXG_SETTING_OPTIONS.LEAD_OFF_DETECT_POS_INPUTS_CH2.ON);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP2, EXG_SETTING_OPTIONS.LEAD_OFF_DETECT_NEG_INPUTS_CH1.OFF);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP2, EXG_SETTING_OPTIONS.LEAD_OFF_DETECT_POS_INPUTS_CH1.OFF);

			if(isEXGUsingDefaultEMGConfiguration()){
				setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP2, EXG_SETTING_OPTIONS.POWER_DOWN_CH2.NORMAL_OPERATION);
			}
		}
		else if(mode==2){//AC Current
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.LEAD_OFF_FREQUENCY.AC);
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.LEAD_OFF_COMPARATORS.ON);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1, EXG_SETTING_OPTIONS.RLD_LEAD_OFF_SENSE_FUNCTION.ON);
			
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1, EXG_SETTING_OPTIONS.LEAD_OFF_DETECT_NEG_INPUTS_CH2.OFF);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1, EXG_SETTING_OPTIONS.LEAD_OFF_DETECT_POS_INPUTS_CH2.ON);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1, EXG_SETTING_OPTIONS.LEAD_OFF_DETECT_NEG_INPUTS_CH1.ON);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1, EXG_SETTING_OPTIONS.LEAD_OFF_DETECT_POS_INPUTS_CH1.ON);

			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP2, EXG_SETTING_OPTIONS.LEAD_OFF_DETECT_NEG_INPUTS_CH2.OFF);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP2, EXG_SETTING_OPTIONS.LEAD_OFF_DETECT_POS_INPUTS_CH2.ON);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP2, EXG_SETTING_OPTIONS.LEAD_OFF_DETECT_NEG_INPUTS_CH1.OFF);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP2, EXG_SETTING_OPTIONS.LEAD_OFF_DETECT_POS_INPUTS_CH1.OFF);

			if(isEXGUsingDefaultEMGConfiguration()){
				setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP2, EXG_SETTING_OPTIONS.POWER_DOWN_CH2.NORMAL_OPERATION);
			}
		}
	}

	protected int getEXGLeadOffCurrentMode(){
		if(isExgPropertyEnabled(EXG_CHIP_INDEX.CHIP1, EXG_SETTING_OPTIONS.RLD_LEAD_OFF_SENSE_FUNCTION.ON)
				||isExgPropertyEnabled(EXG_CHIP_INDEX.CHIP1, EXG_SETTING_OPTIONS.LEAD_OFF_DETECT_NEG_INPUTS_CH2.ON)
				||isExgPropertyEnabled(EXG_CHIP_INDEX.CHIP1, EXG_SETTING_OPTIONS.LEAD_OFF_DETECT_POS_INPUTS_CH2.ON)
				||isExgPropertyEnabled(EXG_CHIP_INDEX.CHIP1, EXG_SETTING_OPTIONS.LEAD_OFF_DETECT_NEG_INPUTS_CH1.ON)
				||isExgPropertyEnabled(EXG_CHIP_INDEX.CHIP1, EXG_SETTING_OPTIONS.LEAD_OFF_DETECT_POS_INPUTS_CH1.ON)
				||isExgPropertyEnabled(EXG_CHIP_INDEX.CHIP2, EXG_SETTING_OPTIONS.LEAD_OFF_DETECT_NEG_INPUTS_CH2.ON)
				||isExgPropertyEnabled(EXG_CHIP_INDEX.CHIP2, EXG_SETTING_OPTIONS.LEAD_OFF_DETECT_POS_INPUTS_CH2.ON)
				||isExgPropertyEnabled(EXG_CHIP_INDEX.CHIP2, EXG_SETTING_OPTIONS.LEAD_OFF_DETECT_NEG_INPUTS_CH1.ON)
				||isExgPropertyEnabled(EXG_CHIP_INDEX.CHIP2, EXG_SETTING_OPTIONS.LEAD_OFF_DETECT_POS_INPUTS_CH1.ON)
				){
			if(isExgPropertyEnabled(EXG_CHIP_INDEX.CHIP1, EXG_SETTING_OPTIONS.LEAD_OFF_FREQUENCY.DC)){
				return 1;//DC Current
			}
			else if(isExgPropertyEnabled(EXG_CHIP_INDEX.CHIP1, EXG_SETTING_OPTIONS.LEAD_OFF_FREQUENCY.AC)){
				return 2;//AC Current
			}
		}
		return 0;//Off
	}

	/**
	 * @param mEXGLeadOffDetectionCurrent the mEXGLeadOffDetectionCurrent to set
	 */
	protected void setEXGLeadOffDetectionCurrent(int mEXGLeadOffDetectionCurrent) {
		setExgPropertySingleChipValue(EXG_CHIP_INDEX.CHIP1, EXG_SETTINGS.REG3_LEAD_OFF_CURRENT, mEXGLeadOffDetectionCurrent);
		setExgPropertySingleChipValue(EXG_CHIP_INDEX.CHIP2, EXG_SETTINGS.REG3_LEAD_OFF_CURRENT, mEXGLeadOffDetectionCurrent);
	}


	/**
	 * @param mEXGLeadOffComparatorTreshold the mEXGLeadOffComparatorTreshold to set
	 */
	protected void setEXGLeadOffComparatorTreshold(int mEXGLeadOffComparatorTreshold) {
		setExgPropertySingleChipValue(EXG_CHIP_INDEX.CHIP1, EXG_SETTINGS.REG3_COMPARATOR_THRESHOLD, mEXGLeadOffComparatorTreshold);
		setExgPropertySingleChipValue(EXG_CHIP_INDEX.CHIP2, EXG_SETTINGS.REG3_COMPARATOR_THRESHOLD, mEXGLeadOffComparatorTreshold);
	}

	/**
	 * @param mEXG2RespirationDetectFreq the mEXG2RespirationDetectFreq to set
	 */
	protected void setEXG2RespirationDetectFreq(int mEXG2RespirationDetectFreq) {
		setExgPropertySingleChipValue(EXG_CHIP_INDEX.CHIP2, EXG_SETTINGS.REG10_RESPIRATION_CONTROL_FREQUENCY, mEXG2RespirationDetectFreq);
		checkWhichExgRespPhaseValuesToUse();
		
		if(isExgRespirationDetectFreq32kHz()) {
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP2, EXG_SETTING_OPTIONS.RESPIRATION_PHASE_AT_32KHZ.PHASE_112_5);
		}
		else {
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP2, EXG_SETTING_OPTIONS.RESPIRATION_PHASE_AT_64KHZ.PHASE_157_5);
		}
	}

	/**
	 * @param mEXG2RespirationDetectPhase the mEXG2RespirationDetectPhase to set
	 */
	protected void setEXG2RespirationDetectPhase(int mEXG2RespirationDetectPhase) {
		setExgPropertySingleChipValue(EXG_CHIP_INDEX.CHIP2, EXG_SETTINGS.REG9_RESPIRATION_PHASE, mEXG2RespirationDetectPhase);
	}
	
	
	/**
	 * @return the mFirmwareIdentifier
	 */
	public int getFirmwareIdentifier() {
		return mFirmwareIdentifier;
	}
	
	public int getFirmwareMajorVersion(){
		return mFirmwareVersionMajor;
	}
	
	public int getFirmwareMinorVersion(){
		return mFirmwareVersionMinor;
	}

	public int getFirmwareInternalVersion(){
		return mFirmwareVersionInternal;
	}
	
	
	public int getFirmwareCode(){
		return mFirmwareVersionCode;
	}
	
	public String getFWVersionName(){
		return mFirmwareVersionParsed;
	}
	
	/**
	 * Get the FW Identifier. It is equal to 3 when LogAndStream, and equal to 4 when BTStream. 
	 * @return The FW identifier
	 */
	public int getFWIdentifier(){
		return (int) mFirmwareIdentifier;
	}
	
	public double getPressTempAC1(){
		return AC1;
	}
	
	public double getPressTempAC2(){
		return AC2;
	}
	
	public double getPressTempAC3(){
		return AC3;
	}
	
	public double getPressTempAC4(){
		return AC4;
	}
	
	public double getPressTempAC5(){
		return AC5;
	}
	
	public double getPressTempAC6(){
		return AC6;
	}
	
	public double getPressTempB1(){
		return B1;
	}
	
	public double getPressTempB2(){
		return B2;
	}
	
	public double getPressTempMB(){
		return MB;
	}
	
	public double getPressTempMC(){
		return MC;
	}
	
	public double getPressTempMD(){
		return MD;
	}


	/**
	 * @return the mDerivedSensors
	 */
	public long getDerivedSensors() {
		return mDerivedSensors;
	}

	public boolean isUsingConfigFromInfoMem() {
		return mShimmerUsingConfigFromInfoMem;
	}

	/**
	 * @return the mPpgAdcSelectionGsrBoard
	 */
	public int getPpgAdcSelectionGsrBoard() {
		return mPpgAdcSelectionGsrBoard;
	}

	/**
	 * @return the mPpg1AdcSelectionProto3DeluxeBoard
	 */
	public int getPpg1AdcSelectionProto3DeluxeBoard() {
		return mPpg1AdcSelectionProto3DeluxeBoard;
	}

	/**
	 * @return the mPpg2AdcSelectionProto3DeluxeBoard
	 */
	public int getPpg2AdcSelectionProto3DeluxeBoard() {
		return mPpg2AdcSelectionProto3DeluxeBoard;
	}
	
	
	/**
	 * @return the mMacIdFromUart
	 */
	public String getMacIdFromUart() {
		return mMacIdFromUart;
	}

	/**
	 * @return the mMacIdFromUartParsed
	 */
	public String getMacIdFromUartParsed() {
		if(this.mMacIdFromUart.length()>=12) {
			return this.mMacIdFromUart.substring(8, 12);
		}		return "0000";
	}

	/**
	 * @return the mMacIdFromBtParsed
	 */
	public String getMacIdFromBtParsed() {
		if(this.mMyBluetoothAddress.length()>=12) {
			return this.mMyBluetoothAddress.substring(8, 12);
		}
		return "0000";	}

	/**
	 * @return the mSamplingDividerVBatt
	 */
	public int getSamplingDividerVBatt() {
		return mSamplingDividerVBatt;
	}

	/**
	 * @return the mSamplingDividerGsr
	 */
	public int getSamplingDividerGsr() {
		return mSamplingDividerGsr;
	}

	/**
	 * @return the mMPU9150GyroAccelRate in Hz
	 */
	public double getMPU9150GyroAccelRateInHz() {
		// Gyroscope Output Rate = 8kHz when the DLPF is disabled (DLPF_CFG = 0 or 7), and 1kHz when the DLPF is enabled
		double numerator = 1000.0;
		if(mMPU9150LPF == 0) {
			numerator = 8000.0;
		}
		
		if(mMPU9150GyroAccelRate == 0) {
			return numerator;
		}
		else {
			return (numerator / mMPU9150GyroAccelRate);
		}
	}
	
	
	/**
	 * @return the mSamplingDividerPpg
	 */
	public int getSamplingDividerPpg() {
		return mSamplingDividerPpg;
	}

	/**
	 * @return the mSamplingDividerLsm303dlhcAccel
	 */
	public int getSamplingDividerLsm303dlhcAccel() {
		return mSamplingDividerLsm303dlhcAccel;
	}

	public long getInitialTimeStamp(){
		return mInitialTimeStamp;
	}
	/**
	 * @return the mHighResAccelWR
	 */
	public boolean isHighResAccelWR() {
		return mHighResAccelWR;
	}

	/**
	 * @return the mLowPowerAccelWR
	 */
	public boolean isLowPowerAccelWR() {
		return mLowPowerAccelWR;
	}


	protected void setDigitalAccelRange(int i){
		mAccelRange = i;
	}
	
	/**
	 * @param mMPU9150AccelRange the mMPU9150AccelRange to set
	 */
	protected void setMPU9150AccelRange(int i) {
		if(checkIfAnyMplChannelEnabled()){
			i=0; // 2g
		}
		
		mMPU9150AccelRange = i;
	}
	
	protected void setMPU9150GyroRange(int i){
//		//Gyro rate can not be set to 250dps when DMP is on
//		if((checkIfAnyMplChannelEnabled()) && (i==0)){
//			i=1;
//		}
		
		if(checkIfAnyMplChannelEnabled()){
			i=3; // 2000dps
		}
		
		mGyroRange = i;
	}
	
	/**
	 * @param state the mInternalExpPower state to set
	 */
	protected void setInternalExpPower(boolean state) {
		if(state) 
			mInternalExpPower = 0x01;
		else 
			mInternalExpPower = 0x00;
	}
	
	protected void setLSM303MagRange(int i){
		mMagRange = i;
	}
	
	protected void setPressureResolution(int i){
		mPressureResolution = i;
	}
	
	protected void setGSRRange(int i){
		mGSRRange = i;
	}
	

	/**
	 * @param mLSM303DigitalAccelRate the mLSM303DigitalAccelRate to set
	 */
	protected void setLSM303DigitalAccelRate(int mLSM303DigitalAccelRate) {
		// double check that rate is compatible with LPM (8 not compatible so to higher rate)
		if((!isLSM303DigitalAccelLPM()) && (mLSM303DigitalAccelRate==8)) {
			mLSM303DigitalAccelRate = 9;
		}
		this.mLSM303DigitalAccelRate = mLSM303DigitalAccelRate;
	}
	
	/**
	 * @param mLSM303MagRate the mLSM303MagRate to set
	 */
	protected void setLSM303MagRate(int mLSM303MagRate) {
		this.mLSM303MagRate = mLSM303MagRate;
	}

	/**
	 * @param shimmerInfoMemBytes the shimmerInfoMemBytes to set
	 */
	protected void setShimmerInfoMemBytes(byte[] shimmerInfoMemBytes) {
		this.infoMemByteArrayParse(shimmerInfoMemBytes);
	}
	
	/**
	 * @return the mShimmerInfoMemBytes
	 */
	public byte[] getShimmerInfoMemBytes() {
		return mInfoMemBytes;
	}
	
//	protected void checkShimmerConfigurationBeforeConfiguring() {
//		this.checkShimmerConfigurationBeforeConfiguring();
//	}
	
	/**
	 * @return the mShimmerInfoMemBytes generated from an empty byte array. This is called to generate the InfoMem bytes for writing to the Shimmer.
	 */
	protected byte[] generateShimmerInfoMemBytes() {
//		System.out.println("SlotDetails:" + this.mUniqueIdentifier + " " + mShimmerInfoMemBytes[3]);
		return infoMemByteArrayGenerate(true);
	}


	
//	protected void clearShimmerInfoMemBytes() {
//		this.mShimmerInfoMemBytes = createEmptyInfoMemByteArray(512);
//	}
	
	
	/**
	 * @param mConfigTime the mConfigTime to set
	 */
	public void setConfigTime(long mConfigTime) {
		this.mConfigTime = mConfigTime;
	}

	
	/**
	 * @param mBufferSize the mBufferSize to set
	 */
	protected void setBufferSize(int mBufferSize) {
		this.mBufferSize = mBufferSize;
	}

	/**
	 * @param mMPU9150MPLSamplingRate the mMPU9150MPLSamplingRate to set
	 */
	protected void setMPU9150MPLSamplingRate(int mMPU9150MPLSamplingRate) {
		this.mMPU9150MPLSamplingRate = mMPU9150MPLSamplingRate;
	}

	/**
	 * @param mMPU9150MagSamplingRate the mMPU9150MagSamplingRate to set
	 */
	protected void setMPU9150MagSamplingRate(int mMPU9150MagSamplingRate) {
		this.mMPU9150MagSamplingRate = mMPU9150MagSamplingRate;
	}

	/**
	 * @param mExperimentName the mExperimentName to set
	 */
	public void setExperimentName(String mExperimentName) {
		if(mExperimentName.length()>12)
			this.mTrialName = mExperimentName.substring(0, 11);
		else
			this.mTrialName = mExperimentName;
	}

	/**
	 * @param mExperimentNumberOfShimmers the mExperimentNumberOfShimmers to set
	 */
	public void setExperimentNumberOfShimmers(int mExperimentNumberOfShimmers) {
    	int maxValue = (int) ((Math.pow(2, 8))-1); 
    	if(mExperimentNumberOfShimmers>maxValue) {
    		mExperimentNumberOfShimmers = maxValue;
    	}
    	else if(mExperimentNumberOfShimmers<=0) {
    		mExperimentNumberOfShimmers = 1;
    	}
    	this.mTrialNumberOfShimmers = mExperimentNumberOfShimmers;
	}

	/**
	 * @param mExperimentDurationEstimated the mExperimentDurationEstimated to set.  Min value is 1.
	 */
	public void setExperimentDurationEstimated(int mExperimentDurationEstimated) {
    	int maxValue = (int) ((Math.pow(2, 16))-1); 
    	if(mExperimentDurationEstimated>maxValue) {
    		mExperimentDurationEstimated = maxValue;
    	}
    	else if(mExperimentDurationEstimated<=0) {
    		mExperimentDurationEstimated = 1;
    	}
    	this.mTrialDurationEstimated = mExperimentDurationEstimated;
	}

	/**
	 * @param mExperimentDurationMaximum the mExperimentDurationMaximum to set. Min value is 0.
	 */
	public void setExperimentDurationMaximum(int mExperimentDurationMaximum) {
    	int maxValue = (int) ((Math.pow(2, 16))-1); 
    	if(mExperimentDurationMaximum>maxValue) {
    		mExperimentDurationMaximum = maxValue;
    	}
    	else if(mExperimentDurationMaximum<0) {
    		mExperimentDurationMaximum = 1;
    	}
    	this.mTrialDurationMaximum = mExperimentDurationMaximum;
	}

	/**
	 * @param mShimmerUserAssignedName the mShimmerUserAssignedName to set
	 */
	public void setShimmerUserAssignedName(String mShimmerUserAssignedName) {
		if(mShimmerUserAssignedName.length()>12) {
			this.mShimmerUserAssignedName = mShimmerUserAssignedName.substring(0, 12);
		}
		else { 
			this.mShimmerUserAssignedName = mShimmerUserAssignedName;
		}
	}
	
	/**
	 * @param state the mInternalExpPower state to set
	 */
	protected void setInternalExpPower(int state) {
		this.mInternalExpPower = state;
	}
	
	/**
	 * @return the mInternalExpPower
	 */
	public boolean isInternalExpPower() {
		if(mInternalExpPower > 0)
			return true;
		else
			return false;
	}
	
	/**
	 * @param state the mMasterShimmer state to set
	 */
	public void setMasterShimmer(boolean state) {
		if(state) 
			this.mMasterShimmer = 0x01;
		else 
			this.mMasterShimmer = 0x00;
	}
	
	/**
	 * @return the mMasterShimmer
	 */
	public boolean isMasterShimmer() {
		if(this.mMasterShimmer > 0)
			return true;
		else
			return false;
	}

	/**
	 * @param state the mSingleTouch state to set
	 */
	public void setSingleTouch(boolean state) {
		if(state) 
			this.mSingleTouch = 0x01;
		else 
			this.mSingleTouch = 0x00;
	}

	/**
	 * @return the mSingleTouch
	 */
	public boolean isSingleTouch() {
		if(this.mSingleTouch > 0)
			return true;
		else
			return false;
	}
	
	/**
	 * @param state  the mTCXO state to set
	 */
	protected void setTCXO(boolean state) {
		if(state) 
			this.mTCXO = 0x01;
		else 
			this.mTCXO = 0x00;
	}

	/**
	 * @return the mTCXO
	 */
	public boolean isTCXO() {
		if(mTCXO > 0)
			return true;
		else
			return false;
	}
	
	/**
	 * @return the mSyncWhenLogging
	 */
	public boolean isSyncWhenLogging() {
		if(mSyncWhenLogging > 0)
			return true;
		else
			return false;
	}

	/**
	 * @param state the mSyncWhenLogging state to set
	 */
	public void setSyncWhenLogging(boolean state) {
		if(state) 
			this.mSyncWhenLogging = 0x01;
		else 
			this.mSyncWhenLogging = 0x00;
	}


	/**
	 * @return the mButtonStart
	 */
	public boolean isButtonStart() {
		if(mButtonStart > 0)
			return true;
		else
			return false;
	}


	/**
	 * @param state the mButtonStart state to set
	 */
	public void setButtonStart(boolean state) {
		if(state) 
			this.mButtonStart = 0x01;
		else 
			this.mButtonStart = 0x00;
	}

	
	// MPL options
	/**
	 * @return the mMPU9150DMP
	 */
	public boolean isMPU9150DMP() {
		if(mMPU9150DMP > 0)
			return true;
		else
			return false;
	}


	/**
	 * @param state the mMPU9150DMP state to set
	 */
	protected void setMPU9150DMP(boolean state) {
		if(state) 
			this.mMPU9150DMP = 0x01;
		else 
			this.mMPU9150DMP = 0x00;
	}
	
	/**
	 * @return the mMPLEnable
	 */
	public boolean isMPLEnable() {
		if(mMPLEnable > 0)
			return true;
		else
			return false;
	}
	
	/**
	 * @param state the mMPLEnable state to set
	 */
	protected void setMPLEnable(boolean state) {
		if(state) 
			this.mMPLEnable = 0x01;
		else 
			this.mMPLEnable = 0x00;
	}

	/**
	 * @return the mMPLSensorFusion
	 */
	public boolean isMPLSensorFusion() {
		if(mMPLSensorFusion > 0)
			return true;
		else
			return false;
	}

	/**
	 * @param state the mMPLSensorFusion state to set
	 */
	protected void setMPLSensorFusion(boolean state) {
		if(state) 
			this.mMPLSensorFusion = 0x01;
		else 
			this.mMPLSensorFusion = 0x00;
	}

	/**
	 * @return the mMPLGyroCalTC
	 */
	public boolean isMPLGyroCalTC() {
		if(mMPLGyroCalTC > 0)
			return true;
		else
			return false;
	}
	
	/**
	 * @param state the mMPLGyroCalTC state to set
	 */
	protected void setMPLGyroCalTC(boolean state) {
		if(state) 
			this.mMPLGyroCalTC = 0x01;
		else 
			this.mMPLGyroCalTC = 0x00;
	}

	/**
	 * @return the mMPLVectCompCal
	 */
	public boolean isMPLVectCompCal() {
		if(mMPLVectCompCal > 0)
			return true;
		else
			return false;
	}

	/**
	 * @param state the mMPLVectCompCal state to set
	 */
	protected void setMPLVectCompCal(boolean state) {
		if(state) 
			this.mMPLVectCompCal = 0x01;
		else 
			this.mMPLVectCompCal = 0x00;
	}

	/**
	 * @return the mMPLMagDistCal
	 */
	public boolean isMPLMagDistCal() {
		if(mMPLMagDistCal > 0)
			return true;
		else
			return false;
	}
	
	/**
	 * @param state the mMPLMagDistCal state to set
	 */
	protected void setMPLMagDistCal(boolean state) {
		if(state) 
			this.mMPLMagDistCal = 0x01;
		else 
			this.mMPLMagDistCal = 0x00;
	}
	
	/**
	 * @return the mMPLSensorFusion
	 */
	public boolean getmMPLSensorFusion() {
		if(mMPLSensorFusion > 0)
			return true;
		else
			return false;
	}

	/**
	 * @param state the mMPLSensorFusion state to set
	 */
	protected void setmMPLSensorFusion(boolean state) {
		if(state) 
			this.mMPLSensorFusion = 0x01;
		else 
			this.mMPLSensorFusion = 0x00;
	}


	/**
	 * @param mMPU9150MotCalCfg the mMPU9150MotCalCfg to set
	 */
	protected void setMPU9150MotCalCfg(int mMPU9150MotCalCfg) {
		this.mMPU9150MotCalCfg = mMPU9150MotCalCfg;
	}

	/**
	 * @param mMPU9150LPF the mMPU9150LPF to set
	 */
	protected void setMPU9150LPF(int mMPU9150LPF) {
		this.mMPU9150LPF = mMPU9150LPF;
	}

	/**
	 * @param mExperimentId the mExperimentId to set
	 */
	public void setExperimentId(int mExperimentId) {
    	int maxValue = (int) ((Math.pow(2, 8))-1); 
    	if(mExperimentId>maxValue) {
    		mExperimentId = maxValue;
    	}
    	else if(mExperimentId<0) {
    		mExperimentId = 1;
    	}
		this.mTrialId = mExperimentId;
	}

	
	/**
	 * @param syncNodesList the syncNodesList to set
	 */
	public void setSyncNodesList(List<String> syncNodesList) {
		this.syncNodesList = syncNodesList;
	}

	/**
	 * @param mSyncBroadcastInterval the mSyncBroadcastInterval to set
	 */
	protected void setSyncBroadcastInterval(int mSyncBroadcastInterval) {
    	int maxValue = (int) ((Math.pow(2, 8))-1); 
    	if(mSyncBroadcastInterval>maxValue) {
    		mSyncBroadcastInterval = maxValue;
    	}
    	else if(mSyncBroadcastInterval<=0) {
    		mSyncBroadcastInterval = 1;
    	}
		this.mSyncBroadcastInterval = mSyncBroadcastInterval;
	}
	
	
	/**
	 * @param mBluetoothBaudRate the mBluetoothBaudRate to set
	 */
	protected void setBluetoothBaudRate(int mBluetoothBaudRate) {
		this.mBluetoothBaudRate = mBluetoothBaudRate;
	}

	/**
	 * @param ppgAdcSelectionGsrBoard the mPpgAdcSelectionGsrBoard to set
	 */
	protected void setPpgAdcSelectionGsrBoard(int ppgAdcSelectionGsrBoard) {
		this.mPpgAdcSelectionGsrBoard = ppgAdcSelectionGsrBoard;
		int key = Configuration.Shimmer3.SensorMapKey.PPG_DUMMY;
		this.setSensorEnabledState(key, mSensorMap.get(key).mIsEnabled);
	}

	/**
	 * @param ppg1AdcSelectionProto3DeluxeBoard the mPpg1AdcSelectionProto3DeluxeBoard to set
	 */
	protected void setPpg1AdcSelectionProto3DeluxeBoard(int ppg1AdcSelectionProto3DeluxeBoard) {
		this.mPpg1AdcSelectionProto3DeluxeBoard = ppg1AdcSelectionProto3DeluxeBoard;
		int key = Configuration.Shimmer3.SensorMapKey.PPG1_DUMMY;
		this.setSensorEnabledState(key, mSensorMap.get(key).mIsEnabled);
	}

	/**
	 * @param ppg2AdcSelectionProto3DeluxeBoard the mPpg2AdcSelectionProto3DeluxeBoard to set
	 */
	protected void setPpg2AdcSelectionProto3DeluxeBoard(int ppg2AdcSelectionProto3DeluxeBoard) {
		this.mPpg2AdcSelectionProto3DeluxeBoard = ppg2AdcSelectionProto3DeluxeBoard;
		int key = Configuration.Shimmer3.SensorMapKey.PPG2_DUMMY;
		this.setSensorEnabledState(key, mSensorMap.get(key).mIsEnabled);
	}

	/**
	 * @param macIdFromUart the mMacIdFromUart to set
	 */
	protected void setMacIdFromUart(String macIdFromUart) {
		this.mMacIdFromUart = macIdFromUart;
	}

	/**
	 * @param myBluetoothAddress the myBluetoothAddress to set
	 */
	protected void setMacIdFromBt(String myBluetoothAddress){
		this.mMyBluetoothAddress = myBluetoothAddress;
	}
	
	/**
	 * @param mSamplingDividerVBatt the mSamplingDividerVBatt to set
	 */
	protected void setSamplingDividerVBatt(int mSamplingDividerVBatt) {
		this.mSamplingDividerVBatt = mSamplingDividerVBatt;
	}

	/**
	 * @param mSamplingDividerGsr the mSamplingDividerGsr to set
	 */
	protected void setSamplingDividerGsr(int mSamplingDividerGsr) {
		this.mSamplingDividerGsr = mSamplingDividerGsr;
	}

	/**
	 * @param mSamplingDividerPpg the mSamplingDividerPpg to set
	 */
	protected void setSamplingDividerPpg(int mSamplingDividerPpg) {
		this.mSamplingDividerPpg = mSamplingDividerPpg;
	}

	/**
	 * @param mSamplingDividerLsm303dlhcAccel the mSamplingDividerLsm303dlhcAccel to set
	 */
	protected void setSamplingDividerLsm303dlhcAccel(int mSamplingDividerLsm303dlhcAccel) {
		this.mSamplingDividerLsm303dlhcAccel = mSamplingDividerLsm303dlhcAccel;
	}

	/**
	 * @param mSamplingDividerBeacon the mSamplingDividerBeacon to set
	 */
	protected void setSamplingDividerBeacon(int mSamplingDividerBeacon) {
		this.mSamplingDividerBeacon = mSamplingDividerBeacon;
	}
	
	
	public int getExpansionBoardId(){
		return mExpansionBoardId;
	}
	
	public int getExpansionBoardRev(){
		return mExpansionBoardRev;
	}
	
	public int getExpansionBoardRevSpecial(){
		return mExpansionBoardRevSpecial;
	}
	
	public String getExpansionBoardIdParsed(){
		return mExpansionBoardParsed;
	}

	protected void setExpansionBoardId(int expansionBoardId){
		mExpansionBoardId = expansionBoardId;
	}

	protected void clearExpansionBoardDetails(){
		mExpansionBoardId = -1;
		mExpansionBoardRev = -1;
		mExpansionBoardRevSpecial = -1;
		mExpansionBoardParsed = "";
		mExpansionBoardParsedWithVer = "";
		mExpBoardArray = null;
	}
	
	
	public Object getValueUsingGuiComponent(String componentName) {
		Object returnValue = null;
		
        if((componentName.equals(Configuration.Shimmer3.GuiLabelConfig.LSM303DLHC_ACCEL_RATE))
        		||(componentName.equals(Configuration.Shimmer3.GuiLabelConfig.EXG_RESPIRATION_DETECT_PHASE))
        		||(componentName.equals(Configuration.Shimmer3.GuiLabelConfig.EXG_REFERENCE_ELECTRODE))){
        	checkConfigOptionValues(componentName);
        }
		
		switch(componentName){
//Booleans
			case(Configuration.Shimmer3.GuiLabelConfig.USER_BUTTON_START):
				returnValue = isButtonStart();
				break;
			case(Configuration.Shimmer3.GuiLabelConfig.SINGLE_TOUCH_START):
				returnValue = isSingleTouch();
				break;
			case(Configuration.Shimmer3.GuiLabelConfig.EXPERIMENT_MASTER_SHIMMER):
				returnValue = isMasterShimmer();
	        	break;
			case(Configuration.Shimmer3.GuiLabelConfig.EXPERIMENT_SYNC_WHEN_LOGGING):
				returnValue = isSyncWhenLogging();
	        	break;
			
			case(Configuration.Shimmer3.GuiLabelConfig.KINEMATIC_LPM):
				if(isLSM303DigitalAccelLPM()&&checkLowPowerGyro()&&checkLowPowerMag()) {
					returnValue = true;
				}
				else {
					returnValue = false;
				}
	        	break;
			case(Configuration.Shimmer3.GuiLabelConfig.LSM303DLHC_ACCEL_LPM):
				returnValue = isLSM303DigitalAccelLPM();
	        	break;
			case(Configuration.Shimmer3.GuiLabelConfig.MPU9150_GYRO_LPM):
				returnValue = checkLowPowerGyro();
	        	break;
			case(Configuration.Shimmer3.GuiLabelConfig.LSM303DLHC_MAG_LPM):
				returnValue = checkLowPowerMag();
	        	break;
			case(Configuration.Shimmer3.GuiLabelConfig.TCX0):
				returnValue = isTCXO();
	        	break;
			case(Configuration.Shimmer3.GuiLabelConfig.INT_EXP_BRD_POWER_BOOLEAN):
				returnValue = isInternalExpPower();
	        	break;
			case(Configuration.Shimmer3.GuiLabelConfig.MPU9150_DMP):
				returnValue = isMPU9150DMP();
	        	break;
			case(Configuration.Shimmer3.GuiLabelConfig.MPU9150_MPL):
				returnValue = isMPLEnable();
	        	break;
			case(Configuration.Shimmer3.GuiLabelConfig.MPU9150_MPL_9DOF_SENSOR_FUSION):
				returnValue = isMPLSensorFusion();
	        	break;
			case(Configuration.Shimmer3.GuiLabelConfig.MPU9150_MPL_GYRO_CAL):
				returnValue = isMPLGyroCalTC();
	        	break;
			case(Configuration.Shimmer3.GuiLabelConfig.MPU9150_MPL_VECTOR_CAL):
				returnValue = isMPLVectCompCal();
	        	break;
			case(Configuration.Shimmer3.GuiLabelConfig.MPU9150_MPL_MAG_CAL):
				returnValue = isMPLMagDistCal();
	        	break;

//Integers
			case(Configuration.Shimmer3.GuiLabelConfig.BLUETOOTH_BAUD_RATE):
				returnValue = getBluetoothBaudRate();
	        	break;
    	
			case(Configuration.Shimmer3.GuiLabelConfig.LSM303DLHC_ACCEL_RANGE):
				returnValue = getAccelRange();
	        	break;
	        
			case(Configuration.Shimmer3.GuiLabelConfig.MPU9150_GYRO_RANGE):
				returnValue = getGyroRange();
	        	break;
	
			case(Configuration.Shimmer3.GuiLabelConfig.LSM303DLHC_MAG_RANGE):
				//TODO check below and commented out code
				returnValue = getMagRange();
			
//					// firmware sets mag range to 7 (i.e. index 6 in combobox) if user set mag range to 0 in config file
//					if(getMagRange() == 0) cmBx.setSelectedIndex(6);
//					else cmBx.setSelectedIndex(getMagRange()-1);
	    		break;
			
			case(Configuration.Shimmer3.GuiLabelConfig.PRESSURE_RESOLUTION):
				returnValue = getPressureResolution();
	    		break;
	    		
			case(Configuration.Shimmer3.GuiLabelConfig.GSR_RANGE):
				returnValue = getGSRRange(); //TODO: check with RM re firmware bug??
	        	break;
	        	
			case(Configuration.Shimmer3.GuiLabelConfig.EXG_RESOLUTION):
				returnValue = getExGResolution();
	    		break;
	        	
			case(Configuration.Shimmer3.GuiLabelConfig.EXG_GAIN):
				returnValue = getExGGainSetting();
				//consolePrintLn("Get " + configValue);
	        	break;
	        	
			case(Configuration.Shimmer3.GuiLabelConfig.LSM303DLHC_ACCEL_RATE):
				int configValue = getLSM303DigitalAccelRate(); 
				 
	        	if(!isLSM303DigitalAccelLPM()) {
		        	if(configValue==8) {
		        		configValue = 9;
		        	}
	        	}
				returnValue = configValue;
	    		break;
	    		
			case(Configuration.Shimmer3.GuiLabelConfig.LSM303DLHC_MAG_RATE):
				returnValue = getLSM303MagRate();
	        	break;

			case(Configuration.Shimmer3.GuiLabelConfig.MPU9150_ACCEL_RANGE):
				returnValue = getMPU9150AccelRange();
            	break;
			case(Configuration.Shimmer3.GuiLabelConfig.MPU9150_DMP_GYRO_CAL):
				returnValue = getMPU9150MotCalCfg();
            	break;
			case(Configuration.Shimmer3.GuiLabelConfig.MPU9150_LPF):
				returnValue = getMPU9150LPF();
            	break;
			case(Configuration.Shimmer3.GuiLabelConfig.MPU9150_MPL_RATE):
				returnValue = getMPU9150MPLSamplingRate();
        		break;
			case(Configuration.Shimmer3.GuiLabelConfig.MPU9150_MAG_RATE):
				returnValue = getMPU9150MagSamplingRate();
            	break;
            	
			case(Configuration.Shimmer3.GuiLabelConfig.EXG_RATE):
				returnValue = getEXG1RateSetting();
				//returnValue = getEXG2RateSetting();
            	break;
			case(Configuration.Shimmer3.GuiLabelConfig.EXG_REFERENCE_ELECTRODE):
				returnValue = getEXGReferenceElectrode();
            	break;
			case(Configuration.Shimmer3.GuiLabelConfig.EXG_LEAD_OFF_DETECTION):
				returnValue = getEXGLeadOffCurrentMode();
            	break;
			case(Configuration.Shimmer3.GuiLabelConfig.EXG_LEAD_OFF_CURRENT):
				returnValue = getEXGLeadOffDetectionCurrent();
            	break;
			case(Configuration.Shimmer3.GuiLabelConfig.EXG_LEAD_OFF_COMPARATOR):
				returnValue = getEXGLeadOffComparatorTreshold();
            	break;
			case(Configuration.Shimmer3.GuiLabelConfig.EXG_RESPIRATION_DETECT_FREQ):
				returnValue = getEXG2RespirationDetectFreq();
            	break;
			case(Configuration.Shimmer3.GuiLabelConfig.EXG_RESPIRATION_DETECT_PHASE):
				returnValue = getEXG2RespirationDetectPhase();
            	break;
			case(Configuration.Shimmer3.GuiLabelConfig.INT_EXP_BRD_POWER_INTEGER):
				returnValue = getInternalExpPower();
            	break;
			case(Configuration.Shimmer3.GuiLabelConfig.PPG_ADC_SELECTION):
				returnValue = getPpgAdcSelectionGsrBoard();
	    		break;
			case(Configuration.Shimmer3.GuiLabelConfig.PPG1_ADC_SELECTION):
				returnValue = getPpg1AdcSelectionProto3DeluxeBoard();
	    		break;
			case(Configuration.Shimmer3.GuiLabelConfig.PPG2_ADC_SELECTION):
				returnValue = getPpg2AdcSelectionProto3DeluxeBoard();
	    		break;
            	

			case(Configuration.ShimmerGQ.GuiLabelConfig.SAMPLING_RATE_DIVIDER_GSR):
				returnValue = getSamplingDividerGsr();
	    		break;
			case(Configuration.ShimmerGQ.GuiLabelConfig.SAMPLING_RATE_DIVIDER_LSM303DLHC_ACCEL):
				returnValue = getSamplingDividerLsm303dlhcAccel();
	    		break;
			case(Configuration.ShimmerGQ.GuiLabelConfig.SAMPLING_RATE_DIVIDER_PPG):
				returnValue = getSamplingDividerPpg();
	    		break;
			case(Configuration.ShimmerGQ.GuiLabelConfig.SAMPLING_RATE_DIVIDER_VBATT):
				returnValue = getSamplingDividerVBatt();
	    		break;
	    		
	    		
//Strings
			case(Configuration.Shimmer3.GuiLabelConfig.SHIMMER_USER_ASSIGNED_NAME):
				returnValue = getShimmerUserAssignedName();
	        	break;
			case(Configuration.Shimmer3.GuiLabelConfig.TRIAL_NAME):
				returnValue = getTrialName();
	        	break;
			case(Configuration.Shimmer3.GuiLabelConfig.SHIMMER_SAMPLING_RATE):
		        Double readSamplingRate = getShimmerSamplingRate();
		    	Double actualSamplingRate = 32768/Math.floor(32768/readSamplingRate); // get Shimmer compatible sampling rate
		    	actualSamplingRate = (double)Math.round(actualSamplingRate * 100) / 100; // round sampling rate to two decimal places
//			    	consolePrintLn("GET SAMPLING RATE: " + componentName);
		    	returnValue = actualSamplingRate.toString();
	        	break;
			case(Configuration.Shimmer3.GuiLabelConfig.BUFFER_SIZE):
				returnValue = Integer.toString(getBufferSize());
	        	break;
			case(Configuration.Shimmer3.GuiLabelConfig.CONFIG_TIME):
	        	returnValue = getConfigTimeParsed();
	        	break;
			case(Configuration.Shimmer3.GuiLabelConfig.SHIMMER_MAC_FROM_INFOMEM):
	        	returnValue = getMacIdFromInfoMem();
	        	break;
			case(Configuration.Shimmer3.GuiLabelConfig.EXPERIMENT_ID):
	        	returnValue = Integer.toString(getExperimentId());
	        	break;
			case(Configuration.Shimmer3.GuiLabelConfig.EXPERIMENT_NUMBER_OF_SHIMMERS):
	        	returnValue = Integer.toString(getTrialNumberOfShimmers());
	        	break;
			case(Configuration.Shimmer3.GuiLabelConfig.EXPERIMENT_DURATION_ESTIMATED):
	        	returnValue = Integer.toString(getTrialDurationEstimated());
	        	break;
			case(Configuration.Shimmer3.GuiLabelConfig.EXPERIMENT_DURATION_MAXIMUM):
	        	returnValue = Integer.toString(getTrialDurationMaximum());
	        	break;
			case(Configuration.Shimmer3.GuiLabelConfig.BROADCAST_INTERVAL):
	        	returnValue = Integer.toString(getSyncBroadcastInterval());
	        	break;
			case(Configuration.Shimmer3.GuiLabelConfig.MPU9150_GYRO_RATE):
				returnValue = Double.toString((double)Math.round(getMPU9150GyroAccelRateInHz() * 100) / 100); // round sampling rate to two decimal places
//    		System.out.println("Gyro Sampling rate: " + getMPU9150GyroAccelRateInHz() + " " + returnValue);
	        	break;
	        	
////List<Byte[]>
//			case(Configuration.Shimmer3.GuiLabelConfig.EXG_BYTES):
//				List<byte[]> listOFExGBytes = new ArrayList<byte[]>();
//				listOFExGBytes.add(getEXG1RegisterArray());
//				listOFExGBytes.add(getEXG2RegisterArray());
//				returnValue = listOFExGBytes;
//	        	break;
	        	
	        default:
	        	break;
		}
		
		return returnValue;
	}		
	
	protected Object setValueUsingGuiComponent(String componentName, Object valueToSet) {

		Object returnValue = null;
		int buf = 0;

		switch(componentName){
//Booleans
			case(Configuration.Shimmer3.GuiLabelConfig.USER_BUTTON_START):
				setButtonStart((boolean)valueToSet);
				break;
			case(Configuration.Shimmer3.GuiLabelConfig.SINGLE_TOUCH_START):
				setSingleTouch((boolean)valueToSet);
				break;
			case(Configuration.Shimmer3.GuiLabelConfig.EXPERIMENT_MASTER_SHIMMER):
				setMasterShimmer((boolean)valueToSet);
	        	break;
			case(Configuration.Shimmer3.GuiLabelConfig.EXPERIMENT_SYNC_WHEN_LOGGING):
				setSyncWhenLogging((boolean)valueToSet);
	        	break;

			case(Configuration.Shimmer3.GuiLabelConfig.KINEMATIC_LPM):
				setLowPowerAccelWR((boolean)valueToSet);
				setLowPowerGyro((boolean)valueToSet);
				setLowPowerMag((boolean)valueToSet);
	        	break;
			case(Configuration.Shimmer3.GuiLabelConfig.LSM303DLHC_ACCEL_LPM):
				setLowPowerAccelWR((boolean)valueToSet);
	        	break;
			case(Configuration.Shimmer3.GuiLabelConfig.MPU9150_GYRO_LPM):
				setLowPowerGyro((boolean)valueToSet);
	        	break;
			case(Configuration.Shimmer3.GuiLabelConfig.LSM303DLHC_MAG_LPM):
				setLowPowerMag((boolean)valueToSet);
	        	break;
			case(Configuration.Shimmer3.GuiLabelConfig.TCX0):
            	setTCXO((boolean)valueToSet);
	        	break;
			case(Configuration.Shimmer3.GuiLabelConfig.INT_EXP_BRD_POWER_BOOLEAN):
            	setInternalExpPower((boolean)valueToSet);
	        	break;
        	
			case(Configuration.Shimmer3.GuiLabelConfig.MPU9150_DMP):
            	setMPU9150DMP((boolean)valueToSet);
	        	break;
			case(Configuration.Shimmer3.GuiLabelConfig.MPU9150_MPL):
            	setMPLEnable((boolean)valueToSet);
	        	break;
			case(Configuration.Shimmer3.GuiLabelConfig.MPU9150_MPL_9DOF_SENSOR_FUSION):
            	setMPLSensorFusion((boolean)valueToSet);
	        	break;
			case(Configuration.Shimmer3.GuiLabelConfig.MPU9150_MPL_GYRO_CAL):
            	setMPLGyroCalTC((boolean)valueToSet);
	        	break;
			case(Configuration.Shimmer3.GuiLabelConfig.MPU9150_MPL_VECTOR_CAL):
            	setMPLVectCompCal((boolean)valueToSet);
	        	break;
			case(Configuration.Shimmer3.GuiLabelConfig.MPU9150_MPL_MAG_CAL):
            	setMPLMagDistCal((boolean)valueToSet);
	        	break;

//Integers
			case(Configuration.Shimmer3.GuiLabelConfig.BLUETOOTH_BAUD_RATE):
				setBluetoothBaudRate((int)valueToSet);
	        	break;
		        	
    		case(Configuration.Shimmer3.GuiLabelConfig.LSM303DLHC_ACCEL_RANGE):
				setDigitalAccelRange((int)valueToSet);
	        	break;
	        
			case(Configuration.Shimmer3.GuiLabelConfig.MPU9150_GYRO_RANGE):
	        	setMPU9150GyroRange((int)valueToSet);
	        	break;
	
			case(Configuration.Shimmer3.GuiLabelConfig.LSM303DLHC_MAG_RANGE):
				setLSM303MagRange((int)valueToSet);
	    		break;
			
			case(Configuration.Shimmer3.GuiLabelConfig.PRESSURE_RESOLUTION):
				setPressureResolution((int)valueToSet);
	    		break;
	    		
			case(Configuration.Shimmer3.GuiLabelConfig.GSR_RANGE):
	    		setGSRRange((int)valueToSet);
	        	break;
	        	
			case(Configuration.Shimmer3.GuiLabelConfig.EXG_RESOLUTION):
				setExGResolution((int)valueToSet);
	    		break;
	    		
			case(Configuration.Shimmer3.GuiLabelConfig.EXG_GAIN):
				//consolePrintLn("before set " + getExGGain());
				setExGGainSetting((int)valueToSet);
				//consolePrintLn("after set " + getExGGain());
	        	break;
				
			case(Configuration.Shimmer3.GuiLabelConfig.LSM303DLHC_ACCEL_RATE):
				setLSM303DigitalAccelRate((int)valueToSet);
	    		break;
	    		
			case(Configuration.Shimmer3.GuiLabelConfig.LSM303DLHC_MAG_RATE):
				setLSM303MagRate((int)valueToSet);
	        	break;
	        	
			case(Configuration.Shimmer3.GuiLabelConfig.MPU9150_ACCEL_RANGE):
				setMPU9150AccelRange((int)valueToSet);
	        	break;
			case(Configuration.Shimmer3.GuiLabelConfig.MPU9150_DMP_GYRO_CAL):
				setMPU9150MotCalCfg((int)valueToSet);
	        	break;
			case(Configuration.Shimmer3.GuiLabelConfig.MPU9150_LPF):
				setMPU9150LPF((int)valueToSet);
	        	break;
			case(Configuration.Shimmer3.GuiLabelConfig.MPU9150_MPL_RATE):
				setMPU9150MPLSamplingRate((int)valueToSet);
	        	break;
			case(Configuration.Shimmer3.GuiLabelConfig.MPU9150_MAG_RATE):
				setMPU9150MagSamplingRate((int)valueToSet);
	        	break;
	        
	        //TODO: regenerate EXG register bytes on each change (just in case)
	        	
			case(Configuration.Shimmer3.GuiLabelConfig.EXG_RATE):
//				setEXG1RateSetting((int)valueToSet);
//				setEXG2RateSetting((int)valueToSet);
				
				setEXGRateSetting((int)valueToSet);

            	break;
			case(Configuration.Shimmer3.GuiLabelConfig.EXG_REFERENCE_ELECTRODE):
				setEXGReferenceElectrode((int)valueToSet);
            	break;
			case(Configuration.Shimmer3.GuiLabelConfig.EXG_LEAD_OFF_DETECTION):
				setEXGLeadOffCurrentMode((int)valueToSet);
            	break;
			case(Configuration.Shimmer3.GuiLabelConfig.EXG_LEAD_OFF_CURRENT):
				setEXGLeadOffDetectionCurrent((int)valueToSet);
            	break;
			case(Configuration.Shimmer3.GuiLabelConfig.EXG_LEAD_OFF_COMPARATOR):
				setEXGLeadOffComparatorTreshold((int)valueToSet);
            	break;
			case(Configuration.Shimmer3.GuiLabelConfig.EXG_RESPIRATION_DETECT_FREQ):
				setEXG2RespirationDetectFreq((int)valueToSet);
            	break;
			case(Configuration.Shimmer3.GuiLabelConfig.EXG_RESPIRATION_DETECT_PHASE):
				setEXG2RespirationDetectPhase((int)valueToSet);
            	break;	        	
			case(Configuration.Shimmer3.GuiLabelConfig.INT_EXP_BRD_POWER_INTEGER):
				setInternalExpPower((int)valueToSet);
            	break;
			case(Configuration.Shimmer3.GuiLabelConfig.PPG_ADC_SELECTION):
				setPpgAdcSelectionGsrBoard((int)valueToSet);
	    		break;
			case(Configuration.Shimmer3.GuiLabelConfig.PPG1_ADC_SELECTION):
				setPpg1AdcSelectionProto3DeluxeBoard((int)valueToSet);
	    		break;
			case(Configuration.Shimmer3.GuiLabelConfig.PPG2_ADC_SELECTION):
				setPpg2AdcSelectionProto3DeluxeBoard((int)valueToSet);
	    		break;
	    	//GQ
			case(Configuration.ShimmerGQ.GuiLabelConfig.SAMPLING_RATE_DIVIDER_GSR):
				setSamplingDividerGsr((int)valueToSet);
	    		break;
			case(Configuration.ShimmerGQ.GuiLabelConfig.SAMPLING_RATE_DIVIDER_LSM303DLHC_ACCEL):
				setSamplingDividerLsm303dlhcAccel((int)valueToSet);
	    		break;
			case(Configuration.ShimmerGQ.GuiLabelConfig.SAMPLING_RATE_DIVIDER_PPG):
				setSamplingDividerPpg((int)valueToSet);
	    		break;
			case(Configuration.ShimmerGQ.GuiLabelConfig.SAMPLING_RATE_DIVIDER_VBATT):
				setSamplingDividerVBatt((int)valueToSet);
	    		break;
	
//Strings
			case(Configuration.Shimmer3.GuiLabelConfig.SHIMMER_USER_ASSIGNED_NAME):
        		setShimmerUserAssignedName((String)valueToSet);
	        	break;
			case(Configuration.Shimmer3.GuiLabelConfig.TRIAL_NAME):
        		setExperimentName((String)valueToSet);
	        	break;
			case(Configuration.Shimmer3.GuiLabelConfig.SHIMMER_SAMPLING_RATE):
	          	// don't let sampling rate be empty
	          	Double enteredSamplingRate;
	          	if(((String)valueToSet).isEmpty()) {
	          		enteredSamplingRate = 1.0;
	          	}            	
	          	else {
	          		enteredSamplingRate = Double.parseDouble((String)valueToSet);
	          	}
	      		setShimmerSamplingRate(enteredSamplingRate);
	      		
	      		returnValue = Double.toString(getShimmerSamplingRate());
	        	break;
//			case(Configuration.Shimmer3.GuiLabelConfig.BUFFER_SIZE):
//	        	break;
//			case(Configuration.Shimmer3.GuiLabelConfig.CONFIG_TIME):
//	        	break;
//			case(Configuration.Shimmer3.GuiLabelConfig.SHIMMER_MAC_FROM_INFOMEM):
//	        	break;
			case(Configuration.Shimmer3.GuiLabelConfig.EXPERIMENT_ID):
            	if(!(((String)valueToSet).isEmpty())) {
                	buf = Integer.parseInt((String)valueToSet);
            	}
        		setExperimentId(buf);
        		
        		returnValue = Integer.toString(getExperimentId());
	        	break;
			case(Configuration.Shimmer3.GuiLabelConfig.EXPERIMENT_NUMBER_OF_SHIMMERS):
            	if(((String)valueToSet).isEmpty()) {
            		buf = 1;
            	}
            	else {
                	buf = Integer.parseInt((String)valueToSet);
            	}
        		setExperimentNumberOfShimmers(buf);
        		
        		returnValue = Integer.toString(getTrialNumberOfShimmers());
	        	break;
			case(Configuration.Shimmer3.GuiLabelConfig.EXPERIMENT_DURATION_ESTIMATED):
            	if(((String)valueToSet).isEmpty()) {
            		buf = 1;
            	}
            	else {
                	buf = Integer.parseInt((String)valueToSet);
            	}
        		setExperimentDurationEstimated(buf);
        		
        		returnValue = Integer.toString(getTrialDurationEstimated());
	        	break;
			case(Configuration.Shimmer3.GuiLabelConfig.EXPERIMENT_DURATION_MAXIMUM):
            	//leave max_exp_len = 0 to not automatically stop logging.
            	if(((String)valueToSet).isEmpty()) {
            		buf = 1;
            	}
            	else {
                	buf = Integer.parseInt((String)valueToSet);
            	}
        		setExperimentDurationMaximum(buf);
        		
        		returnValue = Integer.toString(getTrialDurationMaximum());
	        	break;
			case(Configuration.Shimmer3.GuiLabelConfig.BROADCAST_INTERVAL):
            	buf = 1; // Minimum = 1
            	if(!(((String)valueToSet).isEmpty())) {
                	buf = Integer.parseInt((String)valueToSet);
            	}
        		setSyncBroadcastInterval(buf);
        		
        		returnValue = Integer.toString(getSyncBroadcastInterval());
	        	break;
			case(Configuration.Shimmer3.GuiLabelConfig.MPU9150_GYRO_RATE):
            	double bufDouble = 4.0; // Minimum = 4Hz
            	if(((String)valueToSet).isEmpty()) {
            		bufDouble = 4.0;
            	}
            	else {
            		bufDouble = Double.parseDouble((String)valueToSet);
            	}
            	
            	// Since user is manually entering a freq., clear low-power mode so that their chosen rate will be set correctly. Tick box will be re-enabled automatically if they enter LPM freq. 
            	setLowPowerGyro(false); 
        		setMPU9150GyroAccelRateFromFreq(bufDouble);

        		returnValue = Double.toString((double)Math.round(getMPU9150GyroAccelRateInHz() * 100) / 100); // round sampling rate to two decimal places
//        		System.out.println("Gyro Sampling rate: " + getMPU9150GyroAccelRateInHz() + " " + returnValue);

	        	break;
	        	
////List<Byte[]>
//			case(Configuration.Shimmer3.GuiLabelConfig.EXG_BYTES):
////				if(valueToSet instanceof List<?>){
//					setEXG1RegisterArray(((List<byte[]>)valueToSet).get(0));
//					setEXG2RegisterArray(((List<byte[]>)valueToSet).get(1));
////				}
//	        	break;

	        	
	        default:
	        	break;
		}
		
        if((componentName.equals(Configuration.Shimmer3.GuiLabelConfig.LSM303DLHC_ACCEL_RATE))
        		||(componentName.equals(Configuration.Shimmer3.GuiLabelConfig.EXG_RESPIRATION_DETECT_PHASE))
        		||(componentName.equals(Configuration.Shimmer3.GuiLabelConfig.EXG_REFERENCE_ELECTRODE))){
        	checkConfigOptionValues(componentName);
        }
			
		return returnValue;

	}
	
	public Boolean isStreaming(ShimmerObject obj){
		return obj.mIsStreaming;
	}
	
	/**
	 * @return the mChargingState
	 */
	public String getChargingStateParsed() {
		return mShimmerBattStatusDetails.getChargingStatusParsed();
	}

	/**
	 * @return the mBattVoltage
	 */
	public String getBattVoltage() {
		return mShimmerBattStatusDetails.mBattVoltageParsed;
	}

	/**
	 * @return the mEstimatedChargePercentageParsed
	 */
	public String getEstimatedChargePercentageParsed() {
		return mShimmerBattStatusDetails.mEstimatedChargePercentageParsed;
	}
	
	/**
	 * @return the mEstimatedChargePercentage
	 */
	public Double getEstimatedChargePercentage() {
		return mShimmerBattStatusDetails.mEstimatedChargePercentage;
	}
	
	/**
	 * @return the mLastReceivedTimeStamp
	 */
	public double getLastReceivedTimeStamp(){
		return mLastReceivedTimeStamp;
	}
	
	public String getCenter(){
		return mCenter;
	}
	
	public void setCenter(String value){
		mCenter = value;
	}

	/**
	 * @return the mHaveAttemptedToRead
	 */
	public boolean isHaveAttemptedToRead() {
		return haveAttemptedToRead();
	}

	/**
	 * @return the mDocked
	 */
	public boolean isDocked() {
		return mIsDocked;
	}

	/**
	 * @return the mIsInitialized
	 */
	public boolean isInitialized() {
		return mIsInitialised;
	}

	/**
	 * @return the mHaveAttemptedToRead
	 */
	public boolean haveAttemptedToRead() {
		return mHaveAttemptedToReadConfig;
	}

	public ExpansionBoardDetails getExpansionBoardDetails() {
		return mExpansionBoardDetails;
	}

	public ShimmerVerObject getShimmerVerDetails() {
		return mShimmerVerObject;
	}

	public void setShimmerVersionInfo(ShimmerVerObject hwfw) {
		mShimmerVerObject = hwfw;
		mHardwareVersion = hwfw.mHardwareVersion;
		mHardwareVersionParsed = hwfw.mHardwareVersionParsed;
		mFirmwareIdentifier = hwfw.mFirmwareIdentifier;
		mFirmwareVersionMajor = hwfw.mFirmwareVersionMajor;
		mFirmwareVersionMinor = hwfw.mFirmwareVersionMinor;
		mFirmwareVersionInternal = hwfw.mFirmwareVersionInternal;
		mFirmwareVersionParsed = hwfw.mFirmwareVersionParsed;
		mFirmwareVersionCode = hwfw.mFirmwareVersionCode;
		
		createInfoMemLayoutObjectIfNeeded();
		
		//Once the version is known update settings accordingly 
		if (mFirmwareVersionCode>=6){
			mTimeStampPacketByteSize =3;
			mTimeStampPacketRawMaxValue = 16777216;
		} 
		else if (mFirmwareVersionCode<6){
			mTimeStampPacketByteSize =2;
			mTimeStampPacketRawMaxValue = 65536;
		}
	}

	public void setLastReadRealTimeClockValue(long time) {
		mShimmerLastReadRealTimeClockValue = time;
		mShimmerLastReadRtcValueParsed = UtilShimmer.fromMilToDateExcelCompatible(Long.toString(time), false);
	}

}
