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
import java.util.TreeMap;

import javax.vecmath.Quat4d;
import javax.vecmath.Vector3d;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.shimmerresearch.algorithms.AbstractAlgorithm;
import com.shimmerresearch.algorithms.ConfigOptionDetailsAlgorithm;
import com.shimmerresearch.algorithms.GradDes3DOrientation;
import com.shimmerresearch.comms.wiredProtocol.UartComponentPropertyDetails;
import com.shimmerresearch.comms.wiredProtocol.UartPacketDetails.UART_COMPONENT_PROPERTY;
import com.shimmerresearch.driver.Configuration.CHANNEL_UNITS;
import com.shimmerresearch.driver.Configuration.Shimmer2;
import com.shimmerresearch.driver.Configuration.Shimmer3;
import com.shimmerresearch.driverUtilities.CalibDetailsKinematic;
import com.shimmerresearch.driverUtilities.ChannelDetails;
import com.shimmerresearch.driverUtilities.ConfigOptionDetails;
import com.shimmerresearch.driverUtilities.ConfigOptionDetailsSensor;
import com.shimmerresearch.driverUtilities.SensorDetailsRef;
import com.shimmerresearch.driverUtilities.SensorDetails;
import com.shimmerresearch.driverUtilities.SensorGroupingDetails;
import com.shimmerresearch.driverUtilities.ShimmerSDCardDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_TYPE;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.FW_ID;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.HW_ID;
import com.shimmerresearch.exgConfig.ExGConfigBytesDetails;
import com.shimmerresearch.exgConfig.ExGConfigBytesDetails.EXG_SETTINGS;
import com.shimmerresearch.exgConfig.ExGConfigOption;
import com.shimmerresearch.exgConfig.ExGConfigBytesDetails.EXG_SETTING_OPTIONS;
import com.shimmerresearch.exgConfig.ExGConfigOptionDetails.EXG_CHIP_INDEX;
import com.shimmerresearch.sensors.SensorEXG;
import com.shimmerresearch.sensors.SensorGSR;
import com.shimmerresearch.sensors.SensorMPU9X50;
import com.shimmerresearch.sensors.UtilCalibration;
import com.shimmerresearch.sensors.UtilParseData;
import com.shimmerresearch.algorithms.Orientation3DObject;

/**
 * @author mnolan
 *
 */
public abstract class ShimmerObject extends ShimmerDevice implements Serializable {

	/** * */
	private static final long serialVersionUID = -1364568867018921219L;
	
	protected boolean mFirstTime = true;
	double mFirstRawTS = 0;
	public int OFFSET_LENGTH = 9;
	
	//Sensor Bitmap for ID ; for the purpose of forward compatibility the sensor bitmap and the ID and the sensor bitmap for the Shimmer firmware has been kept separate, 
	//XXX-RS-AA-SensorClass?
	public static final int SENSOR_ACCEL				   = 0x80; 
	public static final int SENSOR_DACCEL				   = 0x1000; //only             //XXX-RS-LSM-SensorClass? "only" -> Only what?
	public static final int SENSOR_GYRO				   	   = 0x40;
	public static final int SENSOR_MAG					   = 0x20;//XXX-RS-LSM-SensorClass? 
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
		//XXX-RS-AA-SensorClass?
		public final static int ACCEL_LN 		= 1<<7;
		public final static int GYRO 			= 1<<6;
		public final static int MAG 			= 1<<5;//XXX-RS-LSM-SensorClass?
		public final static int EXG1_24BIT 		= 1<<4;
		public final static int EXG2_24BIT 		= 1<<3;
		public final static int GSR 			= 1<<2;
		public final static int EXT_EXP_A7 		= 1<<1;
		public final static int EXT_EXP_A6 		= 1<<0;
		public final static int BRIDGE_AMP 		= 1<<15;
		public final static int ECG_TO_HR_FW	= 1<<14;
		public final static int BATTERY 		= 1<<13;
		public final static int ACCEL_WR 		= 1<<12;//XXX-RS-LSM-SensorClass?
		public final static int EXT_EXP_A15 	= 1<<11;
		public final static int INT_EXP_A1 		= 1<<10;
		public final static int INT_EXP_A12 	= 1<<9;
		public final static int INT_EXP_A13 	= 1<<8;
		public final static int INT_EXP_A14 	= 1<<23;
		public final static int ACCEL_MPU 		= 1<<22;
		public final static int MAG_MPU 		= 1<<21;
		public final static int EXG1_16BIT 		= 1<<20;
		public final static int EXG2_16BIT 		= 1<<19;
		public final static int BMP180 			= 1<<18;
		public final static int MPL_TEMPERATURE = 1<<17;
		// 1<<16
		public final static int MPL_QUAT_6DOF 	= 1<<31;
		public final static int MPL_QUAT_9DOF 	= 1<<30;
		public final static int MPL_EULER_6DOF 	= 1<<29;
		public final static int MPL_EULER_9DOF 	= 1<<28;
		public final static int MPL_HEADING 	= 1<<27;
		public final static int MPL_PEDOMETER 	= 1<<26;
		public final static int MPL_TAP 		= 1<<25;
		public final static int MPL_MOTION_ORIENT = 1<<24;
		public final static long GYRO_MPU_MPL 	= (long)1<<39;
		public final static long ACCEL_MPU_MPL 	= (long)1<<38;
		public final static long MAG_MPU_MPL 	= (long)1<<37;
		public final static long SD_SENSOR_MPL_QUAT_6DOF_RAW = (long)1<<36;
		// 1<<35
		// 1<<34
		// 1<<33
		// 1<<32
	}
	
	public class DerivedSensorsBitMask{
		public final static int ORIENTATION_6DOF_LN_EULER = 1<<23;
		public final static int ORIENTATION_6DOF_LN_QUAT = 1<<22;
		public final static int ORIENTATION_9DOF_LN_EULER = 1<<21;
		public final static int ORIENTATION_9DOF_LN_QUAT = 1<<20;
		public final static int ORIENTATION_6DOF_WR_EULER = 1<<19;
		public final static int ORIENTATION_6DOF_WR_QUAT = 1<<18;
		public final static int ORIENTATION_9DOF_WR_EULER = 1<<17;
		public final static int ORIENTATION_9DOF_WR_QUAT = 1<<16;
// ------------------------------------------------------------------
		public final static int ECG2HR_CHIP1_CH1 = 1<<15;
		public final static int ECG2HR_CHIP1_CH2 = 1<<14;
		public final static int ECG2HR_CHIP2_CH1 = 1<<13;
		public final static int ECG2HR_CHIP2_CH2 = 1<<12;
		public final static int ECG2HR_HRV_TIME_DOMAIN = 1<<11;
		public final static int ECG2HR_HRV_FREQ_DOMAIN = 1<<10;
// ----------- Now implemented in SensorPPG -------------------------		
		public final static int PPG_TO_HR2_1_14 = 1<<7;
		public final static int PPG_TO_HR1_12_13 = 1<<6;
		public final static int PPG_TO_HR_12_13 = 1<<5;
		public final static int PPG2_1_14 = 1<<4;
		public final static int PPG1_12_13 = 1<<3;
		public final static int PPG_12_13 = 1<<2;
// ------------------------------------------------------------------			
		public final static int SKIN_TEMP = 1<<1;
		public final static int RES_AMP = 1<<0;
	}
	
	public class BTStream {
		//XXX-RS-AA-SensorClass?
		public final static int ACCEL_LN = 0x80; 
		public final static int GYRO = 0x40;
		public final static int MAG = 0x20;//XXX-RS-LSM-SensorClass?
		public final static int EXG1_24BIT = 0x10;
		public final static int EXG2_24BIT = 0x08;
		public final static int GSR = 0x04;
		public final static int EXT_EXP_A7 = 0x02;
		public final static int EXT_EXP_A6 = 0x01;
		public final static int BRIDGE_AMP = 0x8000;
		// 1<<9 NONE 
		public final static int BATTERY = 0x2000;
		public final static int ACCEL_WR = 0x1000;//XXX-RS-LSM-SensorClass?
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
	
	protected Map<String, ChannelDetails> mChannelMap = new LinkedHashMap<String, ChannelDetails>(); 
	
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
	public static final byte SET_ACCEL_SENSITIVITY_COMMAND    		= (byte) 0x09;//XXX-RS-LSM-SensorClass?
	public static final byte ACCEL_SENSITIVITY_RESPONSE       		= (byte) 0x0A;//XXX-RS-LSM-SensorClass?
	public static final byte GET_ACCEL_SENSITIVITY_COMMAND    		= (byte) 0x0B;//XXX-RS-LSM-SensorClass?
	public static final byte SET_5V_REGULATOR_COMMAND         		= (byte) 0x0C; // only Shimmer 2
	public static final byte SET_PMUX_COMMAND                 		= (byte) 0x0D; // only Shimmer 2
	public static final byte SET_CONFIG_BYTE0_COMMAND   	   		= (byte) 0x0E;
	public static final byte CONFIG_BYTE0_RESPONSE      	   		= (byte) 0x0F;
	public static final byte GET_CONFIG_BYTE0_COMMAND   	   		= (byte) 0x10;
	public static final byte STOP_STREAMING_COMMAND           		= (byte) 0x20;
	//XXX-RS-AA-SensorClass?
	public static final byte SET_ACCEL_CALIBRATION_COMMAND			= (byte) 0x11;
	public static final byte ACCEL_CALIBRATION_RESPONSE       		= (byte) 0x12;
	
	public static final byte SET_LSM303DLHC_ACCEL_CALIBRATION_COMMAND = (byte) 0x1A;//XXX-RS-LSM-SensorClass?
	public static final byte LSM303DLHC_ACCEL_CALIBRATION_RESPONSE 	= (byte) 0x1B;//XXX-RS-LSM-SensorClass?
	public static final byte GET_LSM303DLHC_ACCEL_CALIBRATION_COMMAND = (byte) 0x1C;//XXX-RS-LSM-SensorClass?
	//XXX-RS-AA-SensorClass?
	public static final byte GET_ACCEL_CALIBRATION_COMMAND    		= (byte) 0x13;
	
	public static final byte SET_GYRO_CALIBRATION_COMMAND 	  		= (byte) 0x14;//YYY-RMC-MPU-SensorClass
	public static final byte GYRO_CALIBRATION_RESPONSE        		= (byte) 0x15;//YYY-RMC-MPU-SensorClass
	public static final byte GET_GYRO_CALIBRATION_COMMAND     		= (byte) 0x16;//YYY-RMC-MPU-SensorClass
	public static final byte SET_MAG_CALIBRATION_COMMAND      		= (byte) 0x17;//XXX-RS-LSM-SensorClass?
	public static final byte MAG_CALIBRATION_RESPONSE         		= (byte) 0x18;//XXX-RS-LSM-SensorClass?
	public static final byte GET_MAG_CALIBRATION_COMMAND      		= (byte) 0x19;//XXX-RS-LSM-SensorClass?
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
	public static final byte SET_GYRO_TEMP_VREF_COMMAND       		= (byte) 0x33;//YYY-RMC-MPU-SensorClass
	public static final byte SET_BUFFER_SIZE_COMMAND          		= (byte) 0x34;
	public static final byte BUFFER_SIZE_RESPONSE             		= (byte) 0x35;
	public static final byte GET_BUFFER_SIZE_COMMAND          		= (byte) 0x36;
	public static final byte SET_MAG_GAIN_COMMAND             		= (byte) 0x37;//XXX-RS-LSM-SensorClass?
	public static final byte MAG_GAIN_RESPONSE                		= (byte) 0x38;//XXX-RS-LSM-SensorClass?
	public static final byte GET_MAG_GAIN_COMMAND             		= (byte) 0x39;//XXX-RS-LSM-SensorClass?
	public static final byte SET_MAG_SAMPLING_RATE_COMMAND    		= (byte) 0x3A;//XXX-RS-LSM-SensorClass?
	public static final byte MAG_SAMPLING_RATE_RESPONSE       		= (byte) 0x3B;//XXX-RS-LSM-SensorClass?
	public static final byte GET_MAG_SAMPLING_RATE_COMMAND    		= (byte) 0x3C;//XXX-RS-LSM-SensorClass?
	public static final byte SET_ACCEL_SAMPLING_RATE_COMMAND  		= (byte) 0x40;//XXX-RS-LSM-SensorClass?
	public static final byte ACCEL_SAMPLING_RATE_RESPONSE  			= (byte) 0x41;//XXX-RS-LSM-SensorClass?
	public static final byte GET_ACCEL_SAMPLING_RATE_COMMAND  		= (byte) 0x42;//XXX-RS-LSM-SensorClass?
	public static final byte SET_LSM303DLHC_ACCEL_LPMODE_COMMAND 	= (byte) 0x43;//XXX-RS-LSM-SensorClass?
	public static final byte LSM303DLHC_ACCEL_LPMODE_RESPONSE		= (byte) 0x44;//XXX-RS-LSM-SensorClass?
	public static final byte GET_LSM303DLHC_ACCEL_LPMODE_COMMAND 	= (byte) 0x45;//XXX-RS-LSM-SensorClass?
	public static final byte SET_LSM303DLHC_ACCEL_HRMODE_COMMAND	= (byte) 0x46;//XXX-RS-LSM-SensorClass?
	public static final byte LSM303DLHC_ACCEL_HRMODE_RESPONSE		= (byte) 0x47;//XXX-RS-LSM-SensorClass?
	public static final byte GET_LSM303DLHC_ACCEL_HRMODE_COMMAND 	= (byte) 0x48;//XXX-RS-LSM-SensorClass?
	public static final byte SET_MPU9150_GYRO_RANGE_COMMAND 		= (byte) 0x49;//YYY-RMC-MPU-SensorClass
	public static final byte MPU9150_GYRO_RANGE_RESPONSE 			= (byte) 0x4A;//YYY-RMC-MPU-SensorClass
	public static final byte GET_MPU9150_GYRO_RANGE_COMMAND 		= (byte) 0x4B;//YYY-RMC-MPU-SensorClass;
	public static final byte SET_MPU9150_SAMPLING_RATE_COMMAND 		= (byte) 0x4C;//YYY-RMC-MPU-SensorClass
	public static final byte MPU9150_SAMPLING_RATE_RESPONSE 		= (byte) 0x4D;//YYY-RMC-MPU-SensorClass
	public static final byte GET_MPU9150_SAMPLING_RATE_COMMAND 		= (byte) 0x4E;//YYY-RMC-MPU-SensorClass
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
	public static final byte MPU9150_MAG_SENS_ADJ_VALS_RESPONSE 	= (byte) 0x5C;//YYY-RMC-MPU-SensorClass
	public static final byte GET_MPU9150_MAG_SENS_ADJ_VALS_COMMAND 	= (byte) 0x5D;//YYY-RMC-MPU-SensorClass
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

	public static final int MAX_NUMBER_OF_SIGNALS = 70;//50; //used to be 11 but now 13 because of the SR30 + 8 for 3d orientation
	public static final int MAX_INQUIRY_PACKET_SIZE = 47;

	protected String mClassName="Shimmer";
	protected double mLastReceivedTimeStamp=0;
	protected double mCurrentTimeStampCycle=0;
	protected int mBluetoothBaudRate=9; //460800

	protected int mPacketSize=0; // Default 2 bytes for time stamp and 6 bytes for accelerometer 
	protected int mAccelRange=0;// This stores the current accelerometer range being used. The accelerometer range is stored during two instances, once an ack packet is received after a writeAccelRange(), and after a response packet has been received after readAccelRange() //XXX-RS-LSM-SensorClass?	 	
//	protected boolean mLowPowerAccelWR = false;//TODO: add comment and set from BT command //XXX-RS-LSM-SensorClass?	
	protected boolean mHighResAccelWR = false; //TODO: add comment and set from BT command //XXX-RS-LSM-SensorClass?	
	protected int mLSM303MagRate=4;	// This stores the current Mag Sampling rate, it is a value between 0 and 6; 0 = 0.5 Hz; 1 = 1.0 Hz; 2 = 2.0 Hz; 3 = 5.0 Hz; 4 = 10.0 Hz; 5 = 20.0 Hz; 6 = 50.0 Hz //XXX-RS-LSM-SensorClass?	
	protected int mLSM303DigitalAccelRate=0;//XXX-RS-LSM-SensorClass?
	protected int mMagRange=1;// Currently not supported on Shimmer2. This stores the current Mag Range, it is a value between 0 and 6; 0 = 0.7 Ga; 1 = 1.0 Ga; 2 = 1.5 Ga; 3 = 2.0 Ga; 4 = 3.2 Ga; 5 = 3.8 Ga; 6 = 4.5 Ga //XXX-RS-LSM-SensorClass?	
	/** This stores the current Gyro Range, it is a value between 0 and 3; 0 = +/- 250dps,1 = 500dps, 2 = 1000dps, 3 = 2000dps */
	protected int mGyroRange=1;													 
	protected int mGSRRange=4;													// This stores the current GSR range being used.
	protected int mPastGSRRange=4; // this is to fix a bug with SDLog v0.9
	protected int mPastGSRUncalibratedValue=4; // this is to fix a bug with SDLog v0.9
	protected boolean mPastGSRFirstTime=true; // this is to fix a bug with SDLog v0.9
//	protected int mInternalExpPower=-1;													// This shows whether the internal exp power is enabled.
	protected long mConfigByte0;//XXX-RS-LSM-SensorClass?	
	protected int mNChannels=0;	                                                // Default number of sensor channels set to three because of the on board accelerometer 
	protected int mBufferSize;                   							

	protected String[] mSignalNameArray=new String[MAX_NUMBER_OF_SIGNALS];							// 19 is the maximum number of signal thus far
	protected String[] mSignalDataTypeArray=new String[MAX_NUMBER_OF_SIGNALS];						// 19 is the maximum number of signal thus far
	protected boolean mDefaultCalibrationParametersECG = true;
	protected boolean mDefaultCalibrationParametersEMG = true;
	protected boolean mDefaultCalibrationParametersAccel = true;//XXX-RS-AA-SensorClass?
	protected boolean mDefaultCalibrationParametersDigitalAccel = true; //Also known as the wide range accelerometer //XXX-RS-LSM-SensorClass?
	protected int mPressureResolution = 0;
	/** 0 = 16 bit, 1 = 24 bit */
	protected int mExGResolution = 1;
	private boolean mIsExg1_24bitEnabled = false;
	private boolean mIsExg2_24bitEnabled = false;
	private boolean mIsExg1_16bitEnabled = false;
	private boolean mIsExg2_16bitEnabled = false;
	
	protected int mShimmer2MagRate=0;
	
	protected int mButtonStart = 0;
	protected int mShowRtcErrorLeds = 0;
	protected int mMasterShimmer = 0;
	protected int mSingleTouch = 0;
	protected int mTCXO = 0;
	protected long mRTCOffset = 0; //this is in ticks
	protected int mSyncWhenLogging = 0;
	protected int mSyncBroadcastInterval = 0;
//	protected byte[] mInfoMemBytes = createEmptyInfoMemByteArray(512);
	protected boolean mShimmerUsingConfigFromInfoMem = false;
	protected boolean mIsCrcEnabled = false;
	protected String mCenter = "";
	
	protected long mInitialTimeStamp = 0;

	protected final static int FW_TYPE_BT=0;
	protected final static int FW_TYPE_SD=1;
	
	protected int mTrialId = 0;
	protected int mTrialNumberOfShimmers = 0;

	protected int mTrialDurationEstimated = 0;
	protected int mTrialDurationMaximum = 0;
	
	/** Used in BT communication */
	protected String mMyBluetoothAddress="";
//	/** Used in UART command through the base/dock*/
//	protected String mMacIdFromUart = "";
	/** Read from the InfoMem from UART command through the base/dock*/
	protected String mMacIdFromInfoMem = "";
	
//	protected String mShimmerUserAssignedName=""; // This stores the user assigned name

	protected boolean mConfigFileCreationFlag = true;
	protected boolean mCalibFileCreationFlag = false;
	
	protected List<String> syncNodesList = new ArrayList<String>();
// ----------- Now implemented in SensorPPG -------------------------		
	protected int mPpgAdcSelectionGsrBoard = 0;
	protected int mPpg1AdcSelectionProto3DeluxeBoard = 0;
	protected int mPpg2AdcSelectionProto3DeluxeBoard = 0;
// ------------------------------------------------------------------	

	//
	//XXX-RS-AA-SensorClass?
	protected TreeMap<Integer, CalibDetailsKinematic> mCalibAccelAnalog = new TreeMap<Integer, CalibDetailsKinematic>(); 
	protected double[][] mAlignmentMatrixAnalogAccel = {{-1,0,0},{0,-1,0},{0,0,1}}; 			
	protected double[][] mSensitivityMatrixAnalogAccel = {{38,0,0},{0,38,0},{0,0,38}}; 	
	protected double[][] mOffsetVectorAnalogAccel = {{2048},{2048},{2048}};
	
	protected TreeMap<Integer, CalibDetailsKinematic> mCalibAccelWideRange = new TreeMap<Integer, CalibDetailsKinematic>(); 
	protected double[][] mAlignmentMatrixWRAccel = {{-1,0,0},{0,1,0},{0,0,-1}};//XXX-RS-LSM-SensorClass?	 			
	protected double[][] mSensitivityMatrixWRAccel = {{1631,0,0},{0,1631,0},{0,0,1631}};//XXX-RS-LSM-SensorClass?	 	
	protected double[][] mOffsetVectorWRAccel = {{0},{0},{0}};//XXX-RS-LSM-SensorClass?		

	//Default values Shimmer2 
	protected static final double[][] SensitivityMatrixAccel1p5gShimmer2 = {{101,0,0},{0,101,0},{0,0,101}};
	protected static final double[][] SensitivityMatrixAccel2gShimmer2 = {{76,0,0},{0,76,0},{0,0,76}};
	protected static final double[][] SensitivityMatrixAccel4gShimmer2 = {{38,0,0},{0,38,0},{0,0,38}};
	protected static final double[][] SensitivityMatrixAccel6gShimmer2 = {{25,0,0},{0,25,0},{0,0,25}};
	protected static final double[][] AlignmentMatrixAccelShimmer2 =  {{-1,0,0},{0,-1,0},{0,0,1}}; 			
	protected static final double[][] OffsetVectorAccelShimmer2 = {{2048},{2048},{2048}};			
	//Shimmer3
	//XXX-RS-AA-SensorClass?
	public static final double[][] SensitivityMatrixLowNoiseAccel2gShimmer3 = {{83,0,0},{0,83,0},{0,0,83}};
	protected static final double[][] AlignmentMatrixLowNoiseAccelShimmer3 = {{0,-1,0},{-1,0,0},{0,0,-1}};
	
	public static final double[][] SensitivityMatrixWideRangeAccel2gShimmer3 = {{1631,0,0},{0,1631,0},{0,0,1631}};//XXX-RS-LSM-SensorClass?	
	public static final double[][] SensitivityMatrixWideRangeAccel4gShimmer3 = {{815,0,0},{0,815,0},{0,0,815}};//XXX-RS-LSM-SensorClass?	
	public static final double[][] SensitivityMatrixWideRangeAccel8gShimmer3 = {{408,0,0},{0,408,0},{0,0,408}};//XXX-RS-LSM-SensorClass?	
	public static final double[][] SensitivityMatrixWideRangeAccel16gShimmer3 = {{135,0,0},{0,135,0},{0,0,135}};//XXX-RS-LSM-SensorClass?	

	protected static final double[][] AlignmentMatrixWideRangeAccelShimmer3 = {{-1,0,0},{0,1,0},{0,0,-1}};//XXX-RS-LSM-SensorClass?	 	
	protected static final double[][] AlignmentMatrixAccelShimmer3 = {{0,-1,0},{-1,0,0},{0,0,-1}};
	//XXX-RS-AA-SensorClass?
	protected static final double[][] OffsetVectorLowNoiseAccelShimmer3 = {{2047},{2047},{2047}};
	
	protected static final double[][] OffsetVectorWideRangeAccelShimmer3 = {{0},{0},{0}};//XXX-RS-LSM-SensorClass?	

	protected double OffsetECGRALL=2060;
	protected double GainECGRALL=175;
	protected double OffsetECGLALL=2060;
	protected double GainECGLALL=175;
	protected double OffsetEMG=2060;
	protected double GainEMG=750;

	protected TreeMap<Integer, CalibDetailsKinematic> mCalibGyro = new TreeMap<Integer, CalibDetailsKinematic>(); 
	{
		mCalibGyro.put(Shimmer3.ListofMPU9150GyroRangeConfigValues[0], 
				new CalibDetailsKinematic(Shimmer3.ListofMPU9150GyroRangeConfigValues[0], Shimmer3.ListofGyroRange[0],
						AlignmentMatrixGyroShimmer3, SensitivityMatrixGyro250dpsShimmer3, OffsetVectorGyroShimmer3));
		mCalibGyro.put(Shimmer3.ListofMPU9150GyroRangeConfigValues[1], 
				new CalibDetailsKinematic(Shimmer3.ListofMPU9150GyroRangeConfigValues[1], Shimmer3.ListofGyroRange[1],
						AlignmentMatrixGyroShimmer3, SensitivityMatrixGyro500dpsShimmer3, OffsetVectorGyroShimmer3));
		mCalibGyro.put(Shimmer3.ListofMPU9150GyroRangeConfigValues[2], 
				new CalibDetailsKinematic(Shimmer3.ListofMPU9150GyroRangeConfigValues[2], Shimmer3.ListofGyroRange[2],
						AlignmentMatrixGyroShimmer3, SensitivityMatrixGyro1000dpsShimmer3, OffsetVectorGyroShimmer3));
		mCalibGyro.put(Shimmer3.ListofMPU9150GyroRangeConfigValues[3], 
				new CalibDetailsKinematic(Shimmer3.ListofMPU9150GyroRangeConfigValues[3], Shimmer3.ListofGyroRange[3],
						AlignmentMatrixGyroShimmer3, SensitivityMatrixGyro2000dpsShimmer3, OffsetVectorGyroShimmer3));
	}
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
	//XXX-RS-LSM-SensorClass?	
	protected TreeMap<Integer, CalibDetailsKinematic> mCalibMag = new TreeMap<Integer, CalibDetailsKinematic>(); 
	protected boolean mDefaultCalibrationParametersMag = true;	
	protected double[][] mAlignmentMatrixMagnetometer = {{1,0,0},{0,1,0},{0,0,-1}};				
	protected double[][] mSensitivityMatrixMagnetometer = {{580,0,0},{0,580,0},{0,0,580}};	
	protected double[][] mOffsetVectorMagnetometer = {{0},{0},{0}};

	//Default values Shimmer2 and Shimmer3
	protected static final double[][] AlignmentMatrixMagShimmer2 = {{1,0,0},{0,1,0},{0,0,-1}};
	protected static final double[][] SensitivityMatrixMagShimmer2 = {{580,0,0},{0,580,0},{0,0,580}}; 		
	protected static final double[][] OffsetVectorMagShimmer2 = {{0},{0},{0}};				
	//Shimmer3 
	//XXX-RS-LSM-SensorClass?	
	protected static final double[][] AlignmentMatrixMagShimmer3 = {{-1,0,0},{0,1,0},{0,0,-1}}; 				
	protected static final double[][] SensitivityMatrixMagShimmer3 = {{1100,0,0},{0,1100,0},{0,0,980}}; 		
	protected static final double[][] OffsetVectorMagShimmer3 = {{0},{0},{0}};		

	//XXX-RS-LSM-SensorClass?	
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

	//YYY ----------- Now implemented in SensorBMP180 -------------------------
	protected double pressTempAC1 = 408;          
	protected double pressTempAC2 = -72;
	protected double pressTempAC3 = -14383;
	protected double pressTempAC4 = 332741;
	protected double pressTempAC5 = 32757;
	protected double pressTempAC6 = 23153;
	protected double pressTempB1 = 6190;
	protected double pressTempB2 = 4;
	protected double pressTempMB = -32767;
	protected double pressTempMC = -8711;
	protected double pressTempMD = 2868;
	// ---------------------------------------------------------------------
	protected boolean mLowPowerMag = false;//XXX-RS-LSM-SensorClass?	
	protected boolean mLowPowerAccelWR = false;//XXX-RS-LSM-SensorClass?	
	protected boolean mLowPowerGyro = false;
	
	protected double mLastReceivedCalibratedTimeStamp=-1; 
	protected boolean mFirstTimeCalTime=true;//XXX-RS-LSM-SensorClass?	
	protected double mCalTimeStart;//XXX-RS-LSM-SensorClass?	
	private double mLastKnownHeartRate=0;
	protected DescriptiveStatistics mVSenseBattMA= new DescriptiveStatistics(1024); //YYY -BattVolt-SensorClass
	Quat4d mQ = new Quat4d();//XXX-RS-LSM-SensorClass?	
	transient GradDes3DOrientation mOrientationAlgo;//XXX-RS-LSM-SensorClass?	
	protected boolean mOrientationEnabled = false;//XXX-RS-LSM-SensorClass?	
	protected boolean mEnableOntheFlyGyroOVCal = false;

	protected double mGyroOVCalThreshold = 1.2;
	DescriptiveStatistics mGyroX;
	DescriptiveStatistics mGyroY;
	DescriptiveStatistics mGyroZ;
	DescriptiveStatistics mGyroXRaw;
	DescriptiveStatistics mGyroYRaw;
	DescriptiveStatistics mGyroZRaw;
	
	protected boolean mEnableCalibration = true;//XXX-RS-LSM-SensorClass?	
	protected byte[] mInquiryResponseBytes;//XXX-RS-LSM-SensorClass?	
//	protected boolean mIsStreaming =false;											// This is used to monitor whether the device is in streaming mode
//	protected boolean mIsSDLogging =false;											// This is used to monitor whether the device is in sd log mode
	//all raw params should start with a 1 byte identifier in position [0]
	//XXX-RS-AA-SensorClass?
	protected byte[] mAccelCalRawParams = new byte[22];
	
	protected byte[] mDigiAccelCalRawParams  = new byte[22];//XXX-RS-LSM-SensorClass?	
	protected byte[] mGyroCalRawParams  = new byte[22];
	protected byte[] mMagCalRawParams  = new byte[22];//XXX-RS-LSM-SensorClass?	
	
	    // ----------- Now implemented in SensorBMP180 -------------------------
		protected byte[] mPressureCalRawParams = new byte[23];
		protected byte[] mPressureRawParams  = new byte[23];
		// ---------------------------------------------------------------------
	
	protected byte[] mEMGCalRawParams  = new byte[13];
	protected byte[] mECGCalRawParams = new byte[13];
	
	

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
	
	//YYY ----------- MPU9X50 options start -------------------------
//	protected int mMPU9150GyroRate = 0;
//	protected int mMPUAccelRange = 0;

	protected int mMPU9150AccelRange=0;											// This stores the current MPU9150 Accel Range. 0 = 2g, 1 = 4g, 2 = 8g, 4 = 16g
	protected int mMPU9150GyroAccelRate=0;

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

	protected double[][] AlignmentMatrixMPLAccel = {{-1,0,0},{0,1,0},{0,0,-1}}; 			
	protected double[][] SensitivityMatrixMPLAccel = {{1631,0,0},{0,1631,0},{0,0,1631}}; 	
	protected double[][] OffsetVectorMPLAccel = {{0},{0},{0}};
	
	protected double[][] AlignmentMatrixMPLMag = {{-1,0,0},{0,1,0},{0,0,-1}}; 			
	protected double[][] SensitivityMatrixMPLMag = {{1631,0,0},{0,1631,0},{0,0,1631}}; 	
	protected double[][] OffsetVectorMPLMag = {{0},{0},{0}};
	
	protected double[][] AlignmentMatrixMPLGyro = {{-1,0,0},{0,1,0},{0,0,-1}}; 			
	protected double[][] SensitivityMatrixMPLGyro = {{1631,0,0},{0,1631,0},{0,0,1631}}; 	
	protected double[][] OffsetVectorMPLGyro = {{0},{0},{0}};

	// ----------- MPU9X50 options end -------------------------	

	
	//This features are only used in LogAndStream FW 
	protected String mDirectoryName;
	protected int mDirectoryNameLength;
	@Deprecated
	private List<String[]> mExtraSignalProperties = null;
	
	public static final double ACCELERATION_DUE_TO_GRAVITY = 9.81;//XXX-RS-LSM-SensorClass?
	
	/** GQ BLE */
	protected int mGqPacketNumHeaderBytes = 0;
	/** GQ BLE */
	protected int mSamplingDividerVBatt = 0;
	/** GQ BLE */
	protected int mSamplingDividerGsr = 0;
// ----------- Now implemented in SensorPPG -------------------------		
	/** GQ BLE */
	protected int mSamplingDividerPpg = 0;
// ------------------------------------------------------------------	
	/** GQ BLE */
	protected int mSamplingDividerLsm303dlhcAccel = 0;//XXX-RS-LSM-SensorClass?
	/** GQ BLE */
	protected int mSamplingDividerBeacon = 0;

	
	protected int mTimeStampPacketByteSize = 2;
//	protected byte[] mSetRWC;
//	protected byte[] mGetRWC;
//	public long mShimmerRealTimeClockConFigTime = 0;
//	public long mShimmerLastReadRealTimeClockValue = 0;
//	public String mShimmerLastReadRtcValueParsed = "";
	
	protected int mTimeStampPacketRawMaxValue = 65536;// 16777216 or 65536 
	
	private boolean isOverrideShowRwcErrorLeds = true;

	protected abstract void checkBattery();
	
	/** This method will be deprecated for future Shimmer hardware revisions. The last hardware this will be used for is Shimmer3. 
	 *  It should work with all FW associated with Shimmer3 and Shimmer2 devices.
	 *  
	 *  Future hardware which WON'T be using this will start with ShimmerGQ HW. 
	 * 
	 * @param newPacket
	 * @param fwType
	 * @param isTimeSyncEnabled
	 * @param pcTimestamp this is only used by shimmerbluetooth, set to -1 if not using
	 * @return
	 * @throws Exception
	 */
//	@Override //TODO replace "int fwType" with "COMMUNICATION_TYPE commType" so that the method can override the one in ShimmerDevice
	protected ObjectCluster buildMsg(byte[] newPacket, int fwType, boolean isTimeSyncEnabled, long pcTimestamp) throws Exception {
		ObjectCluster objectCluster = new ObjectCluster();
		objectCluster.setShimmerName(mShimmerUserAssignedName);
		objectCluster.setMacAddress(mMyBluetoothAddress);
		objectCluster.mRawData = newPacket;
		
		long systemTime = System.currentTimeMillis();
		if(fwType == FW_TYPE_BT){
			systemTime = pcTimestamp;
			objectCluster.mSystemTimeStamp=ByteBuffer.allocate(8).putLong(systemTime).array();
			objectCluster.addData(Shimmer3.ObjectClusterSensorName.SYSTEM_TIMESTAMP,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLISECONDS,systemTime);
		}
		
		
		if(fwType != FW_TYPE_BT && fwType != FW_TYPE_SD){
			throw new Exception("The Firmware is not compatible");
		}
		
		int numCalibratedData = mNChannels;
		int numUncalibratedData = mNChannels;
		int numUncalibratedDataUnits = mNChannels;
		int numCalibratedDataUnits = mNChannels; 
		int numSensorNames = mNChannels;
		int numAdditionalChannels = 0;
		
		if (fwType == FW_TYPE_BT){
			objectCluster.mSystemTimeStamp=ByteBuffer.allocate(8).putLong(systemTime).array();
			//plus 1 because of: timestamp
			numAdditionalChannels += 1;
			
			//plus 4 because of: batt percent, PRR Current, PRR Trial, system timestamp
			numAdditionalChannels += 4;
			
			//Event Markers
			numAdditionalChannels += 1;
			
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
		
		if (fwType == FW_TYPE_BT){ // Here so as not to mess with ShimmerSDLog
			//for ECG LL-LA Channel added&calculated in API
			if(isEXGUsingDefaultECGConfiguration()){
				numAdditionalChannels += 1;
			}
		}
		
		// adding channels from enabled algorithms details
		//
		for(AbstractAlgorithm aA: getListOfEnabledAlgorithmModules()){
			numAdditionalChannels += aA.getNumberOfEnabledChannels();
		}
		
		//numAdditionalChannels += 15;
		
		//XXX-RS-LSM-SensorClass? //XXX-RS-AA-SensorClass?
		double [] calibratedData = new double[numCalibratedData+numAdditionalChannels];
		double [] uncalibratedData = new double[numUncalibratedData+numAdditionalChannels];
		String [] uncalibratedDataUnits = new String[numUncalibratedDataUnits+numAdditionalChannels];
		String [] calibratedDataUnits = new String[numCalibratedDataUnits+numAdditionalChannels];
		String [] sensorNames = new String[numSensorNames+numAdditionalChannels];
		
		System.arraycopy(mSignalNameArray, 0, sensorNames, 0, sensorNames.length);
		// the above is throwing an error
		//sensorNames = Arrays.copyOf(mSignalNameArray, mSignalNameArray.length);
		
		//PARSE DATA
		long[] newPacketInt = UtilParseData.parseData(newPacket, mSignalDataTypeArray);
		
		double[] tempData=new double[3];
		Vector3d accelerometer = new Vector3d();//XXX-RS-LSM-SensorClass? //XXX-RS-AA-SensorClass?
		Vector3d magnetometer = new Vector3d();//XXX-RS-LSM-SensorClass? //XXX-RS-AA-SensorClass?
		Vector3d gyroscope = new Vector3d();
		
		if (getHardwareVersion()==HW_ID.SHIMMER_SR30 || getHardwareVersion()==HW_ID.SHIMMER_3  
				|| getHardwareVersion()==HW_ID.SHIMMER_GQ_802154_LR || getHardwareVersion()==HW_ID.SHIMMER_GQ_802154_NR || getHardwareVersion()==HW_ID.SHIMMER_2R_GQ){
			
			int iTimeStamp=getSignalIndex(Configuration.Shimmer3.ObjectClusterSensorName.TIMESTAMP); //find index
			if(mFirstTime && fwType == FW_TYPE_SD){
				//this is to make sure the Raw starts from zero
				mFirstRawTS = (double)newPacketInt[iTimeStamp];
				mFirstTime = false;
			}
			double calibratedTS = calibrateTimeStamp((double)newPacketInt[iTimeStamp]);

			//TIMESTAMP
			if (fwType == FW_TYPE_SD){
				// RTC timestamp uncal. (shimmer timestamp + RTC offset from header); unit = ticks
				double unwrappedrawtimestamp = calibratedTS*32768/1000;
				if (getFirmwareVersionMajor() ==0 && getFirmwareVersionMinor()==5){
					
				} else {
					unwrappedrawtimestamp = unwrappedrawtimestamp - mFirstRawTS; //deduct this so it will start from 0
				}
				long sdlograwtimestamp = (long)mInitialTimeStamp + (long)unwrappedrawtimestamp;
				objectCluster.addData(Shimmer3.ObjectClusterSensorName.TIMESTAMP,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.CLOCK_UNIT,(double)sdlograwtimestamp);
				
				uncalibratedData[iTimeStamp] = (double)sdlograwtimestamp;
				if (getFirmwareVersionMajor() ==0 && getFirmwareVersionMinor()==5){
					uncalibratedData[iTimeStamp] = (double)newPacketInt[iTimeStamp];
				}
				uncalibratedDataUnits[iTimeStamp] = CHANNEL_UNITS.CLOCK_UNIT;

				if (mEnableCalibration){
					double sdlogcaltimestamp = (double)sdlograwtimestamp/32768*1000;
					objectCluster.addData(Shimmer3.ObjectClusterSensorName.TIMESTAMP,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLISECONDS,sdlogcaltimestamp);
					calibratedData[iTimeStamp] = sdlogcaltimestamp;
					calibratedDataUnits[iTimeStamp] = CHANNEL_UNITS.MILLISECONDS;
				}
			} 
			else if (fwType == FW_TYPE_BT){
				objectCluster.addData(Shimmer3.ObjectClusterSensorName.TIMESTAMP,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iTimeStamp]);
				uncalibratedData[iTimeStamp] = (double)newPacketInt[iTimeStamp];
				uncalibratedDataUnits[iTimeStamp] = CHANNEL_UNITS.NO_UNITS;
				if (mEnableCalibration){
					objectCluster.addData(Shimmer3.ObjectClusterSensorName.TIMESTAMP,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLISECONDS,calibratedTS);
					calibratedData[iTimeStamp] = calibratedTS;
					calibratedDataUnits[iTimeStamp] = CHANNEL_UNITS.MILLISECONDS;
					objectCluster.mShimmerCalibratedTimeStamp = calibratedTS;
				}
			}

			//RAW RTC
			if ((fwType == FW_TYPE_SD) && mRTCOffset!=0) {
//			if (fwIdentifier == FW_TYPE_SD) {
				double unwrappedrawtimestamp = calibratedTS*32768/1000;
				unwrappedrawtimestamp = unwrappedrawtimestamp - mFirstRawTS; //deduct this so it will start from 0
				long rtctimestamp = (long)mInitialTimeStamp + (long)unwrappedrawtimestamp + mRTCOffset;
				objectCluster.addData(Shimmer3.ObjectClusterSensorName.REAL_TIME_CLOCK,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.CLOCK_UNIT,(double)rtctimestamp);
				uncalibratedData[sensorNames.length-1] = (double)rtctimestamp;
				uncalibratedDataUnits[sensorNames.length-1] = CHANNEL_UNITS.CLOCK_UNIT;
				sensorNames[sensorNames.length-1]= Shimmer3.ObjectClusterSensorName.REAL_TIME_CLOCK;
				if (mEnableCalibration){
					double rtctimestampcal = calibratedTS;
					if(mInitialTimeStamp!=0){
						rtctimestampcal += ((double)mInitialTimeStamp/32768.0*1000.0);
					}
					if(mRTCOffset!=0){
						rtctimestampcal += ((double)mRTCOffset/32768.0*1000.0);
					}
					if(mFirstRawTS!=0){
						rtctimestampcal -= (mFirstRawTS/32768.0*1000.0);
					}
					objectCluster.addData(Shimmer3.ObjectClusterSensorName.REAL_TIME_CLOCK,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLISECONDS,rtctimestampcal);
					calibratedData[sensorNames.length-1] = rtctimestampcal;
					calibratedDataUnits[sensorNames.length-1] = CHANNEL_UNITS.MILLISECONDS;
				}
			}

			//OFFSET
			if(isTimeSyncEnabled && (fwType == FW_TYPE_SD)){
				int iOffset=getSignalIndex(Shimmer3.ObjectClusterSensorName.TIMESTAMP_OFFSET); //find index
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

				objectCluster.addData(Shimmer3.ObjectClusterSensorName.TIMESTAMP_OFFSET,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,offsetValue);
				uncalibratedData[iOffset] = offsetValue;
				calibratedData[iOffset] = Double.NaN;
				uncalibratedDataUnits[iOffset] = CHANNEL_UNITS.NO_UNITS;
				calibratedDataUnits[iOffset] = CHANNEL_UNITS.NO_UNITS;
			} 


			objectCluster = callAdditionalServices(objectCluster);


			//first get raw and calibrated data, this is data derived from the Shimmer device and involves no involvement from the API

			//XXX-RS-AA-SensorClass?
			if (((fwType == FW_TYPE_BT) && (mEnabledSensors & BTStream.ACCEL_LN) > 0) 
					|| ((fwType == FW_TYPE_SD) && (mEnabledSensors & SDLogHeader.ACCEL_LN) > 0)){
				int iAccelX=getSignalIndex(Shimmer3.ObjectClusterSensorName.ACCEL_LN_X); //find index
				int iAccelY=getSignalIndex(Shimmer3.ObjectClusterSensorName.ACCEL_LN_Y); //find index
				int iAccelZ=getSignalIndex(Shimmer3.ObjectClusterSensorName.ACCEL_LN_Z); //find index
				tempData[0]=(double)newPacketInt[iAccelX];
				tempData[1]=(double)newPacketInt[iAccelY];
				tempData[2]=(double)newPacketInt[iAccelZ];

				objectCluster.addData(Shimmer3.ObjectClusterSensorName.ACCEL_LN_X,CHANNEL_TYPE.UNCAL.toString().toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iAccelX]);
				objectCluster.addData(Shimmer3.ObjectClusterSensorName.ACCEL_LN_Y,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iAccelY]);
				objectCluster.addData(Shimmer3.ObjectClusterSensorName.ACCEL_LN_Z,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iAccelZ]);
				uncalibratedData[iAccelX]=(double)newPacketInt[iAccelX];
				uncalibratedData[iAccelY]=(double)newPacketInt[iAccelY];
				uncalibratedData[iAccelZ]=(double)newPacketInt[iAccelZ];
				uncalibratedDataUnits[iAccelX]=CHANNEL_UNITS.NO_UNITS;
				uncalibratedDataUnits[iAccelY]=CHANNEL_UNITS.NO_UNITS;
				uncalibratedDataUnits[iAccelZ]=CHANNEL_UNITS.NO_UNITS;


				if (mEnableCalibration){
					double[] accelCalibratedData;
					accelCalibratedData=UtilCalibration.calibrateInertialSensorData(tempData, mAlignmentMatrixAnalogAccel, mSensitivityMatrixAnalogAccel, mOffsetVectorAnalogAccel);
					calibratedData[iAccelX]=accelCalibratedData[0];
					calibratedData[iAccelY]=accelCalibratedData[1];
					calibratedData[iAccelZ]=accelCalibratedData[2];

					objectCluster.addData(Shimmer3.ObjectClusterSensorName.ACCEL_LN_X,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.ACCEL_CAL_UNIT,accelCalibratedData[0],mDefaultCalibrationParametersAccel);
					objectCluster.addData(Shimmer3.ObjectClusterSensorName.ACCEL_LN_Y,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.ACCEL_CAL_UNIT,accelCalibratedData[1],mDefaultCalibrationParametersAccel);
					objectCluster.addData(Shimmer3.ObjectClusterSensorName.ACCEL_LN_Z,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.ACCEL_CAL_UNIT,accelCalibratedData[2],mDefaultCalibrationParametersAccel);
					calibratedDataUnits[iAccelX] = CHANNEL_UNITS.ACCEL_CAL_UNIT;
					calibratedDataUnits[iAccelY] = CHANNEL_UNITS.ACCEL_CAL_UNIT;
					calibratedDataUnits[iAccelZ] = CHANNEL_UNITS.ACCEL_CAL_UNIT;
					accelerometer.x=accelCalibratedData[0];
					accelerometer.y=accelCalibratedData[1];
					accelerometer.z=accelCalibratedData[2];

//					if (mDefaultCalibrationParametersAccel == true) {
//						objectCluster.addData(Shimmer3.ObjectClusterSensorName.ACCEL_LN_X,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.ACCEL_DEFAULT_CAL_UNIT,accelCalibratedData[0]));
//						objectCluster.addData(Shimmer3.ObjectClusterSensorName.ACCEL_LN_Y,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.ACCEL_DEFAULT_CAL_UNIT,accelCalibratedData[1]));
//						objectCluster.addData(Shimmer3.ObjectClusterSensorName.ACCEL_LN_Z,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.ACCEL_DEFAULT_CAL_UNIT,accelCalibratedData[2]));
//						calibratedDataUnits[iAccelX]=CHANNEL_UNITS.ACCEL_DEFAULT_CAL_UNIT;
//						calibratedDataUnits[iAccelY]=CHANNEL_UNITS.ACCEL_DEFAULT_CAL_UNIT;
//						calibratedDataUnits[iAccelZ]=CHANNEL_UNITS.ACCEL_DEFAULT_CAL_UNIT;
//						accelerometer.x=accelCalibratedData[0];
//						accelerometer.y=accelCalibratedData[1];
//						accelerometer.z=accelCalibratedData[2];
//
//					} else {
//						objectCluster.addData(Shimmer3.ObjectClusterSensorName.ACCEL_LN_X,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.ACCEL_CAL_UNIT,accelCalibratedData[0]));
//						objectCluster.addData(Shimmer3.ObjectClusterSensorName.ACCEL_LN_Y,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.ACCEL_CAL_UNIT,accelCalibratedData[1]));
//						objectCluster.addData(Shimmer3.ObjectClusterSensorName.ACCEL_LN_Z,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.ACCEL_CAL_UNIT,accelCalibratedData[2]));
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
			//XXX-RS-LSM-SensorClass?
			if (((fwType == FW_TYPE_BT) && (mEnabledSensors & BTStream.ACCEL_WR) > 0) 
					|| ((fwType == FW_TYPE_SD) && (mEnabledSensors & SDLogHeader.ACCEL_WR) > 0)
					){
				int iAccelX=getSignalIndex(Shimmer3.ObjectClusterSensorName.ACCEL_WR_X); //find index
				int iAccelY=getSignalIndex(Shimmer3.ObjectClusterSensorName.ACCEL_WR_Y); //find index
				int iAccelZ=getSignalIndex(Shimmer3.ObjectClusterSensorName.ACCEL_WR_Z); //find index
				//check range

				tempData[0]=(double)newPacketInt[iAccelX];
				tempData[1]=(double)newPacketInt[iAccelY];
				tempData[2]=(double)newPacketInt[iAccelZ];
				objectCluster.addData(Shimmer3.ObjectClusterSensorName.ACCEL_WR_X,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iAccelX]);
				objectCluster.addData(Shimmer3.ObjectClusterSensorName.ACCEL_WR_Y,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iAccelY]);
				objectCluster.addData(Shimmer3.ObjectClusterSensorName.ACCEL_WR_Z,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iAccelZ]);
				uncalibratedData[iAccelX]=(double)newPacketInt[iAccelX];
				uncalibratedData[iAccelY]=(double)newPacketInt[iAccelY];
				uncalibratedData[iAccelZ]=(double)newPacketInt[iAccelZ];
				uncalibratedDataUnits[iAccelX]=CHANNEL_UNITS.NO_UNITS;
				uncalibratedDataUnits[iAccelY]=CHANNEL_UNITS.NO_UNITS;
				uncalibratedDataUnits[iAccelZ]=CHANNEL_UNITS.NO_UNITS;

				if (mEnableCalibration){
					double[] accelCalibratedData;
					accelCalibratedData=UtilCalibration.calibrateInertialSensorData(tempData, mAlignmentMatrixWRAccel, mSensitivityMatrixWRAccel, mOffsetVectorWRAccel);
					calibratedData[iAccelX]=accelCalibratedData[0];
					calibratedData[iAccelY]=accelCalibratedData[1];
					calibratedData[iAccelZ]=accelCalibratedData[2];
					
					objectCluster.addData(Shimmer3.ObjectClusterSensorName.ACCEL_WR_X,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.ACCEL_CAL_UNIT,calibratedData[iAccelX],mDefaultCalibrationParametersDigitalAccel);
					objectCluster.addData(Shimmer3.ObjectClusterSensorName.ACCEL_WR_Y,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.ACCEL_CAL_UNIT,calibratedData[iAccelY],mDefaultCalibrationParametersDigitalAccel);
					objectCluster.addData(Shimmer3.ObjectClusterSensorName.ACCEL_WR_Z,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.ACCEL_CAL_UNIT,calibratedData[iAccelZ],mDefaultCalibrationParametersDigitalAccel);
					calibratedDataUnits[iAccelX]=CHANNEL_UNITS.ACCEL_CAL_UNIT;
					calibratedDataUnits[iAccelY]=CHANNEL_UNITS.ACCEL_CAL_UNIT;
					calibratedDataUnits[iAccelZ]=CHANNEL_UNITS.ACCEL_CAL_UNIT;
					if (((mEnabledSensors & SENSOR_ACCEL) == 0)){
						accelerometer.x=accelCalibratedData[0];
						accelerometer.y=accelCalibratedData[1];
						accelerometer.z=accelCalibratedData[2];
					}

//					if (mDefaultCalibrationParametersDigitalAccel == true) {
//						objectCluster.addData(Shimmer3.ObjectClusterSensorName.ACCEL_WR_X,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.ACCEL_DEFAULT_CAL_UNIT,calibratedData[iAccelX]));
//						objectCluster.addData(Shimmer3.ObjectClusterSensorName.ACCEL_WR_Y,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.ACCEL_DEFAULT_CAL_UNIT,calibratedData[iAccelY]));
//						objectCluster.addData(Shimmer3.ObjectClusterSensorName.ACCEL_WR_Z,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.ACCEL_DEFAULT_CAL_UNIT,calibratedData[iAccelZ]));
//						calibratedDataUnits[iAccelX]=CHANNEL_UNITS.ACCEL_DEFAULT_CAL_UNIT;
//						calibratedDataUnits[iAccelY]=CHANNEL_UNITS.ACCEL_DEFAULT_CAL_UNIT;
//						calibratedDataUnits[iAccelZ]=CHANNEL_UNITS.ACCEL_DEFAULT_CAL_UNIT;
//						if (((mEnabledSensors & SENSOR_ACCEL) == 0)){
//							accelerometer.x=accelCalibratedData[0]; //this is used to calculate quaternions // skip if Low noise is enabled
//							accelerometer.y=accelCalibratedData[1];
//							accelerometer.z=accelCalibratedData[2];
//						}
//					} else {
//						objectCluster.addData(Shimmer3.ObjectClusterSensorName.ACCEL_WR_X,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.ACCEL_CAL_UNIT,calibratedData[iAccelX]));
//						objectCluster.addData(Shimmer3.ObjectClusterSensorName.ACCEL_WR_Y,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.ACCEL_CAL_UNIT,calibratedData[iAccelY]));
//						objectCluster.addData(Shimmer3.ObjectClusterSensorName.ACCEL_WR_Z,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.ACCEL_CAL_UNIT,calibratedData[iAccelZ]));
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
			if (((fwType == FW_TYPE_BT) && (mEnabledSensors & BTStream.GYRO) > 0) 
					|| ((fwType == FW_TYPE_SD) && (mEnabledSensors & SDLogHeader.GYRO) > 0)
					) {
				int iGyroX=getSignalIndex(Shimmer3.ObjectClusterSensorName.GYRO_X);
				int iGyroY=getSignalIndex(Shimmer3.ObjectClusterSensorName.GYRO_Y);
				int iGyroZ=getSignalIndex(Shimmer3.ObjectClusterSensorName.GYRO_Z);
				tempData[0]=(double)newPacketInt[iGyroX];
				tempData[1]=(double)newPacketInt[iGyroY];
				tempData[2]=(double)newPacketInt[iGyroZ];


				objectCluster.addData(Shimmer3.ObjectClusterSensorName.GYRO_X,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iGyroX]);
				objectCluster.addData(Shimmer3.ObjectClusterSensorName.GYRO_Y,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iGyroY]);
				objectCluster.addData(Shimmer3.ObjectClusterSensorName.GYRO_Z,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iGyroZ]);
				uncalibratedData[iGyroX]=(double)newPacketInt[iGyroX];
				uncalibratedData[iGyroY]=(double)newPacketInt[iGyroY];
				uncalibratedData[iGyroZ]=(double)newPacketInt[iGyroZ];
				uncalibratedDataUnits[iGyroX]=CHANNEL_UNITS.NO_UNITS;
				uncalibratedDataUnits[iGyroY]=CHANNEL_UNITS.NO_UNITS;
				uncalibratedDataUnits[iGyroZ]=CHANNEL_UNITS.NO_UNITS;
				if (mEnableCalibration){
					double[] gyroCalibratedData=UtilCalibration.calibrateInertialSensorData(tempData, mAlignmentMatrixGyroscope, mSensitivityMatrixGyroscope, mOffsetVectorGyroscope);
					calibratedData[iGyroX]=gyroCalibratedData[0];
					calibratedData[iGyroY]=gyroCalibratedData[1];
					calibratedData[iGyroZ]=gyroCalibratedData[2];
					
					objectCluster.addData(Shimmer3.ObjectClusterSensorName.GYRO_X,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.GYRO_CAL_UNIT,gyroCalibratedData[0],mDefaultCalibrationParametersGyro);
					objectCluster.addData(Shimmer3.ObjectClusterSensorName.GYRO_Y,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.GYRO_CAL_UNIT,gyroCalibratedData[1],mDefaultCalibrationParametersGyro);
					objectCluster.addData(Shimmer3.ObjectClusterSensorName.GYRO_Z,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.GYRO_CAL_UNIT,gyroCalibratedData[2],mDefaultCalibrationParametersGyro);
					gyroscope.x=gyroCalibratedData[0]*Math.PI/180;
					gyroscope.y=gyroCalibratedData[1]*Math.PI/180;
					gyroscope.z=gyroCalibratedData[2]*Math.PI/180;
					calibratedDataUnits[iGyroX]=CHANNEL_UNITS.GYRO_CAL_UNIT;
					calibratedDataUnits[iGyroY]=CHANNEL_UNITS.GYRO_CAL_UNIT;
					calibratedDataUnits[iGyroZ]=CHANNEL_UNITS.GYRO_CAL_UNIT;

//					if (mDefaultCalibrationParametersGyro == true) {
//						objectCluster.addData(Shimmer3.ObjectClusterSensorName.GYRO_X,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.GYRO_DEFAULT_CAL_UNIT,gyroCalibratedData[0]));
//						objectCluster.addData(Shimmer3.ObjectClusterSensorName.GYRO_Y,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.GYRO_DEFAULT_CAL_UNIT,gyroCalibratedData[1]));
//						objectCluster.addData(Shimmer3.ObjectClusterSensorName.GYRO_Z,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.GYRO_DEFAULT_CAL_UNIT,gyroCalibratedData[2]));
//						gyroscope.x=gyroCalibratedData[0]*Math.PI/180;
//						gyroscope.y=gyroCalibratedData[1]*Math.PI/180;
//						gyroscope.z=gyroCalibratedData[2]*Math.PI/180;
//						calibratedDataUnits[iGyroX]=CHANNEL_UNITS.GYRO_DEFAULT_CAL_UNIT;
//						calibratedDataUnits[iGyroY]=CHANNEL_UNITS.GYRO_DEFAULT_CAL_UNIT;
//						calibratedDataUnits[iGyroZ]=CHANNEL_UNITS.GYRO_DEFAULT_CAL_UNIT;
//					} else {
//						objectCluster.addData(Shimmer3.ObjectClusterSensorName.GYRO_X,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.GYRO_CAL_UNIT,gyroCalibratedData[0]));
//						objectCluster.addData(Shimmer3.ObjectClusterSensorName.GYRO_Y,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.GYRO_CAL_UNIT,gyroCalibratedData[1]));
//						objectCluster.addData(Shimmer3.ObjectClusterSensorName.GYRO_Z,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.GYRO_CAL_UNIT,gyroCalibratedData[2]));
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
			//XXX-RS-LSM-SensorClass?
			if (((fwType == FW_TYPE_BT) && (mEnabledSensors & BTStream.MAG) > 0) 
					|| ((fwType == FW_TYPE_SD) && (mEnabledSensors & SDLogHeader.MAG) > 0)
					) {
				int iMagX=getSignalIndex(Shimmer3.ObjectClusterSensorName.MAG_X);
				int iMagY=getSignalIndex(Shimmer3.ObjectClusterSensorName.MAG_Y);
				int iMagZ=getSignalIndex(Shimmer3.ObjectClusterSensorName.MAG_Z);
				tempData[0]=(double)newPacketInt[iMagX];
				tempData[1]=(double)newPacketInt[iMagY];
				tempData[2]=(double)newPacketInt[iMagZ];
				objectCluster.addData(Shimmer3.ObjectClusterSensorName.MAG_X,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iMagX]);
				objectCluster.addData(Shimmer3.ObjectClusterSensorName.MAG_Y,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iMagY]);
				objectCluster.addData(Shimmer3.ObjectClusterSensorName.MAG_Z,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iMagZ]);
				uncalibratedData[iMagX]=(double)newPacketInt[iMagX];
				uncalibratedData[iMagY]=(double)newPacketInt[iMagY];
				uncalibratedData[iMagZ]=(double)newPacketInt[iMagZ];
				uncalibratedDataUnits[iMagX]=CHANNEL_UNITS.NO_UNITS;
				uncalibratedDataUnits[iMagY]=CHANNEL_UNITS.NO_UNITS;
				uncalibratedDataUnits[iMagZ]=CHANNEL_UNITS.NO_UNITS;
				if (mEnableCalibration){
					double[] magCalibratedData;
					magCalibratedData=UtilCalibration.calibrateInertialSensorData(tempData, mAlignmentMatrixMagnetometer, mSensitivityMatrixMagnetometer, mOffsetVectorMagnetometer);
					calibratedData[iMagX]=magCalibratedData[0];
					calibratedData[iMagY]=magCalibratedData[1];
					calibratedData[iMagZ]=magCalibratedData[2];
					
					objectCluster.addData(Shimmer3.ObjectClusterSensorName.MAG_X,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MAG_CAL_UNIT,magCalibratedData[0],mDefaultCalibrationParametersMag);
					objectCluster.addData(Shimmer3.ObjectClusterSensorName.MAG_Y,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MAG_CAL_UNIT,magCalibratedData[1],mDefaultCalibrationParametersMag);
					objectCluster.addData(Shimmer3.ObjectClusterSensorName.MAG_Z,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MAG_CAL_UNIT,magCalibratedData[2],mDefaultCalibrationParametersMag);
					magnetometer.x=magCalibratedData[0];
					magnetometer.y=magCalibratedData[1];
					magnetometer.z=magCalibratedData[2];
					calibratedDataUnits[iMagX]=CHANNEL_UNITS.MAG_CAL_UNIT;
					calibratedDataUnits[iMagY]=CHANNEL_UNITS.MAG_CAL_UNIT;
					calibratedDataUnits[iMagZ]=CHANNEL_UNITS.MAG_CAL_UNIT;

//					if (mDefaultCalibrationParametersMag == true) {
//						objectCluster.addData(Shimmer3.ObjectClusterSensorName.MAG_X,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MAG_DEFAULT_CAL_UNIT,magCalibratedData[0]));
//						objectCluster.addData(Shimmer3.ObjectClusterSensorName.MAG_Y,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MAG_DEFAULT_CAL_UNIT,magCalibratedData[1]));
//						objectCluster.addData(Shimmer3.ObjectClusterSensorName.MAG_Z,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MAG_DEFAULT_CAL_UNIT,magCalibratedData[2]));
//						magnetometer.x=magCalibratedData[0];
//						magnetometer.y=magCalibratedData[1];
//						magnetometer.z=magCalibratedData[2];
//						calibratedDataUnits[iMagX]=CHANNEL_UNITS.MAG_DEFAULT_CAL_UNIT;
//						calibratedDataUnits[iMagY]=CHANNEL_UNITS.MAG_DEFAULT_CAL_UNIT;
//						calibratedDataUnits[iMagZ]=CHANNEL_UNITS.MAG_DEFAULT_CAL_UNIT;
//					} else {
//						objectCluster.addData(Shimmer3.ObjectClusterSensorName.MAG_X,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MAG_CAL_UNIT,magCalibratedData[0]));
//						objectCluster.addData(Shimmer3.ObjectClusterSensorName.MAG_Y,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MAG_CAL_UNIT,magCalibratedData[1]));
//						objectCluster.addData(Shimmer3.ObjectClusterSensorName.MAG_Z,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MAG_CAL_UNIT,magCalibratedData[2]));
//						magnetometer.x=magCalibratedData[0];
//						magnetometer.y=magCalibratedData[1];
//						magnetometer.z=magCalibratedData[2];
//						calibratedDataUnits[iMagX]=CHANNEL_UNITS.MAG_CAL_UNIT;
//						calibratedDataUnits[iMagY]=CHANNEL_UNITS.MAG_CAL_UNIT;
//						calibratedDataUnits[iMagZ]=CHANNEL_UNITS.MAG_CAL_UNIT;
//					}
				}
			}

			if (((fwType == FW_TYPE_BT) && (mEnabledSensors & BTStream.BATTERY) > 0) 
					|| ((fwType == FW_TYPE_SD) && (mEnabledSensors & SDLogHeader.BATTERY) > 0)
					) {
				int iBatt = getSignalIndex(Shimmer3.ObjectClusterSensorName.BATTERY);
				tempData[0] = (double)newPacketInt[iBatt];
				objectCluster.addData(Shimmer3.ObjectClusterSensorName.BATTERY,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iBatt]);
				uncalibratedData[iBatt]=(double)newPacketInt[iBatt];
				uncalibratedDataUnits[iBatt]=CHANNEL_UNITS.NO_UNITS;
				if (mEnableCalibration){
					calibratedData[iBatt]=calibrateU12AdcValue(tempData[0],0,3,1)*2;
					calibratedDataUnits[iBatt] = CHANNEL_UNITS.MILLIVOLTS;
					objectCluster.addData(Shimmer3.ObjectClusterSensorName.BATTERY,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calibratedData[iBatt]);
					mVSenseBattMA.addValue(calibratedData[iBatt]);
					checkBattery();
				}
			}
			if (((fwType == FW_TYPE_BT) && (mEnabledSensors & BTStream.EXT_EXP_A7) > 0) 
					|| ((fwType == FW_TYPE_SD) && (mEnabledSensors & SDLogHeader.EXT_EXP_A7) > 0)
					) {
				int iA7 = getSignalIndex(Shimmer3.ObjectClusterSensorName.EXT_EXP_ADC_A7);
				tempData[0] = (double)newPacketInt[iA7];
				objectCluster.addData(Shimmer3.ObjectClusterSensorName.EXT_EXP_ADC_A7,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iA7]);
				uncalibratedData[iA7]=(double)newPacketInt[iA7];
				uncalibratedDataUnits[iA7]=CHANNEL_UNITS.NO_UNITS;
				if (mEnableCalibration){
					calibratedData[iA7]=calibrateU12AdcValue(tempData[0],0,3,1);
					calibratedDataUnits[iA7] = CHANNEL_UNITS.MILLIVOLTS;
					objectCluster.addData(Shimmer3.ObjectClusterSensorName.EXT_EXP_ADC_A7,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calibratedData[iA7]);
				}
			}
			if (((fwType == FW_TYPE_BT) && (mEnabledSensors & BTStream.EXT_EXP_A6) > 0) 
					|| ((fwType == FW_TYPE_SD) && (mEnabledSensors & SDLogHeader.EXT_EXP_A6) > 0)
					) {
				int iA6 = getSignalIndex(Shimmer3.ObjectClusterSensorName.EXT_EXP_ADC_A6);
				tempData[0] = (double)newPacketInt[iA6];
				objectCluster.addData(Shimmer3.ObjectClusterSensorName.EXT_EXP_ADC_A6,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iA6]);
				uncalibratedData[iA6]=(double)newPacketInt[iA6];
				uncalibratedDataUnits[iA6]=CHANNEL_UNITS.NO_UNITS;
				if (mEnableCalibration){
					calibratedData[iA6]=calibrateU12AdcValue(tempData[0],0,3,1);
					calibratedDataUnits[iA6] = CHANNEL_UNITS.MILLIVOLTS;
					objectCluster.addData(Shimmer3.ObjectClusterSensorName.EXT_EXP_ADC_A6,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calibratedData[iA6]);
				}
			}
			if (((fwType == FW_TYPE_BT) && (mEnabledSensors & BTStream.EXT_EXP_A15) > 0) 
					|| ((fwType == FW_TYPE_SD) && (mEnabledSensors & SDLogHeader.EXT_EXP_A15) > 0)
					) {
				int iA15 = getSignalIndex(Shimmer3.ObjectClusterSensorName.EXT_EXP_ADC_A15);
				tempData[0] = (double)newPacketInt[iA15];
				objectCluster.addData(Shimmer3.ObjectClusterSensorName.EXT_EXP_ADC_A15,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iA15]);
				uncalibratedData[iA15]=(double)newPacketInt[iA15];
				uncalibratedDataUnits[iA15]=CHANNEL_UNITS.NO_UNITS;

				if (mEnableCalibration){
					calibratedData[iA15]=calibrateU12AdcValue(tempData[0],0,3,1);
					calibratedDataUnits[iA15] = CHANNEL_UNITS.MILLIVOLTS;
					objectCluster.addData(Shimmer3.ObjectClusterSensorName.EXT_EXP_ADC_A15,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calibratedData[iA15]);
				}
			}
			if (((fwType == FW_TYPE_BT) && (mEnabledSensors & BTStream.INT_EXP_A1) > 0) 
					|| ((fwType == FW_TYPE_SD) && (mEnabledSensors & SDLogHeader.INT_EXP_A1) > 0)
					) {
					int iA1 = getSignalIndex(Shimmer3.ObjectClusterSensorName.INT_EXP_ADC_A1);
					tempData[0] = (double)newPacketInt[iA1];
					String sensorName = Shimmer3.ObjectClusterSensorName.INT_EXP_ADC_A1;
					
					//to Support derived sensor renaming
					if (isDerivedSensorsSupported()){
						//change name based on derived sensor value
						if ((mDerivedSensors & DerivedSensorsBitMask.PPG2_1_14)>0){
							sensorName = Shimmer3.ObjectClusterSensorName.PPG2_A1;
						}else if ((mDerivedSensors & DerivedSensorsBitMask.RES_AMP)>0){
							sensorName = Shimmer3.ObjectClusterSensorName.RESISTANCE_AMP;
						}else if((mDerivedSensors & DerivedSensorsBitMask.SKIN_TEMP)>0){
							sensorName = Shimmer3.ObjectClusterSensorName.SKIN_TEMPERATURE_PROBE;
						}
						
					}
					sensorNames[iA1]=sensorName;
					objectCluster.addData(sensorName,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iA1]);
					uncalibratedData[iA1]=(double)newPacketInt[iA1];
					uncalibratedDataUnits[iA1]=CHANNEL_UNITS.NO_UNITS;
					if (mEnableCalibration){
						if((mDerivedSensors & DerivedSensorsBitMask.SKIN_TEMP)>0){
							calibratedData[iA1]=calibratePhillipsSkinTemperatureData(tempData[0]);
							calibratedDataUnits[iA1] = CHANNEL_UNITS.DEGREES_CELSUIS;
							objectCluster.addData(sensorName,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.DEGREES_CELSUIS,calibratedData[iA1]);
						}
						else{
							calibratedData[iA1]=calibrateU12AdcValue(tempData[0],0,3,1);
							calibratedDataUnits[iA1] = CHANNEL_UNITS.MILLIVOLTS;
							objectCluster.addData(sensorName,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calibratedData[iA1]);
						}
					}
			}
			if (((fwType == FW_TYPE_BT) && (mEnabledSensors & BTStream.INT_EXP_A12) > 0) 
					|| ((fwType == FW_TYPE_SD) && (mEnabledSensors & SDLogHeader.INT_EXP_A12) > 0)
					) {
				int iA12 = getSignalIndex(Shimmer3.ObjectClusterSensorName.INT_EXP_ADC_A12);
				tempData[0] = (double)newPacketInt[iA12];
				String sensorName = Shimmer3.ObjectClusterSensorName.INT_EXP_ADC_A12;
				//to Support derived sensor renaming
				if (isDerivedSensorsSupported()){
					//change name based on derived sensor value
					if ((mDerivedSensors & DerivedSensorsBitMask.PPG_12_13)>0){
						sensorName = Shimmer3.ObjectClusterSensorName.PPG_A12;
					} else if ((mDerivedSensors & DerivedSensorsBitMask.PPG1_12_13)>0){
						sensorName = Shimmer3.ObjectClusterSensorName.PPG1_A12;
					}
				}
				
				sensorNames[iA12]=sensorName;
				objectCluster.addData(sensorName,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iA12]);
				uncalibratedData[iA12]=(double)newPacketInt[iA12];
				uncalibratedDataUnits[iA12]=CHANNEL_UNITS.NO_UNITS;
				if (mEnableCalibration){
					calibratedData[iA12]=calibrateU12AdcValue(tempData[0],0,3,1);
					objectCluster.addData(sensorName,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calibratedData[iA12]);
					calibratedDataUnits[iA12] = CHANNEL_UNITS.MILLIVOLTS;

				}
			}
			if (((fwType == FW_TYPE_BT) && (mEnabledSensors & BTStream.INT_EXP_A13) > 0) 
					|| ((fwType == FW_TYPE_SD) && (mEnabledSensors & SDLogHeader.INT_EXP_A13) > 0)
					) {
				int iA13 = getSignalIndex(Shimmer3.ObjectClusterSensorName.INT_EXP_ADC_A13);
				tempData[0] = (double)newPacketInt[iA13];
				String sensorName = Shimmer3.ObjectClusterSensorName.INT_EXP_ADC_A13;
				//to Support derived sensor renaming
				if (isDerivedSensorsSupported()){
					//change name based on derived sensor value
					if ((mDerivedSensors & DerivedSensorsBitMask.PPG_12_13)>0){
						sensorName = Shimmer3.ObjectClusterSensorName.PPG_A13;
					} else if ((mDerivedSensors & DerivedSensorsBitMask.PPG1_12_13)>0){
						sensorName = Shimmer3.ObjectClusterSensorName.PPG1_A13;
					}
				}
				
				sensorNames[iA13]=sensorName;
				objectCluster.addData(sensorName,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iA13]);
				uncalibratedData[iA13]=(double)newPacketInt[iA13];
				uncalibratedDataUnits[iA13]=CHANNEL_UNITS.NO_UNITS;
				if (mEnableCalibration){
					calibratedData[iA13]=calibrateU12AdcValue(tempData[0],0,3,1);
					objectCluster.addData(sensorName,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calibratedData[iA13]);
					calibratedDataUnits[iA13] = CHANNEL_UNITS.MILLIVOLTS;
				}
			}
			if (((fwType == FW_TYPE_BT) && (mEnabledSensors & BTStream.INT_EXP_A14) > 0) 
					|| ((fwType == FW_TYPE_SD) && (mEnabledSensors & SDLogHeader.INT_EXP_A14) > 0)
					) {
				int iA14 = getSignalIndex(Shimmer3.ObjectClusterSensorName.INT_EXP_ADC_A14);
				tempData[0] = (double)newPacketInt[iA14];
				String sensorName = Shimmer3.ObjectClusterSensorName.INT_EXP_ADC_A14;
				//to Support derived sensor renaming
				if (isDerivedSensorsSupported()){
					//change name based on derived sensor value
					if ((mDerivedSensors & DerivedSensorsBitMask.PPG2_1_14)>0){
						sensorName = Shimmer3.ObjectClusterSensorName.PPG2_A14;
					}
				}
				sensorNames[iA14]=sensorName;
				objectCluster.addData(sensorName,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iA14]);
				uncalibratedData[iA14]=(double)newPacketInt[iA14];
				uncalibratedDataUnits[iA14]=CHANNEL_UNITS.NO_UNITS;
				if (mEnableCalibration){
					calibratedData[iA14]=calibrateU12AdcValue(tempData[0],0,3,1);
					objectCluster.addData(sensorName,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calibratedData[iA14]);
					calibratedDataUnits[iA14] = CHANNEL_UNITS.MILLIVOLTS;
				}
			}
			//((fwIdentifier == FW_IDEN_BTSTREAM) && (mEnabledSensors & BTStream.ACCEL_WR) > 0) 
			//|| ((fwIdentifier == FW_IDEN_SD) && (mEnabledSensors & SDLogHeader.ACCEL_WR) > 0)
			if (((fwType == FW_TYPE_BT)&&((mEnabledSensors & BTStream.ACCEL_LN) > 0 || (mEnabledSensors & BTStream.ACCEL_WR) > 0) && ((mEnabledSensors & BTStream.GYRO) > 0) && ((mEnabledSensors & BTStream.MAG) > 0) && mOrientationEnabled )
					||((fwType == FW_TYPE_SD)&&((mEnabledSensors & SDLogHeader.ACCEL_LN) > 0 || (mEnabledSensors & SDLogHeader.ACCEL_WR) > 0) && ((mEnabledSensors & SDLogHeader.GYRO) > 0) && ((mEnabledSensors & SDLogHeader.MAG) > 0) && mOrientationEnabled )){
				if (mEnableCalibration){
					if (mOrientationAlgo==null){
						mOrientationAlgo = new GradDes3DOrientation(0.4, (double)1/getSamplingRateShimmer(), 1, 0, 0,0);
					}
					//New 2016-05-31
					Orientation3DObject q = mOrientationAlgo.update(accelerometer.x,accelerometer.y,accelerometer.z, gyroscope.x,gyroscope.y,gyroscope.z, magnetometer.x,magnetometer.y,magnetometer.z);
					//Old 2016-05-31
//					Quaternion q = mOrientationAlgo.update(accelerometer.x,accelerometer.y,accelerometer.z, gyroscope.x,gyroscope.y,gyroscope.z, magnetometer.x,magnetometer.y,magnetometer.z);
					double theta, Rx, Ry, Rz, rho;
//					rho = Math.acos(q.q1);
//					theta = rho * 2;
//					Rx = q.q2 / Math.sin(rho);
//					Ry = q.q3 / Math.sin(rho);
//					Rz = q.q4 / Math.sin(rho);
					objectCluster.addData(Shimmer3.ObjectClusterSensorName.AXIS_ANGLE_A,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL,q.getTheta());
					objectCluster.addData(Shimmer3.ObjectClusterSensorName.AXIS_ANGLE_X,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL,q.getAngleX());
					objectCluster.addData(Shimmer3.ObjectClusterSensorName.AXIS_ANGLE_Y,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL,q.getAngleY());
					objectCluster.addData(Shimmer3.ObjectClusterSensorName.AXIS_ANGLE_Z,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL,q.getAngleZ());
					objectCluster.addData(Shimmer3.ObjectClusterSensorName.QUAT_MADGE_9DOF_W,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL,q.getQuaternionW());
					objectCluster.addData(Shimmer3.ObjectClusterSensorName.QUAT_MADGE_9DOF_X,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL,q.getQuaternionX());
					objectCluster.addData(Shimmer3.ObjectClusterSensorName.QUAT_MADGE_9DOF_Y,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL,q.getQuaternionY());
					objectCluster.addData(Shimmer3.ObjectClusterSensorName.QUAT_MADGE_9DOF_Z,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL,q.getQuaternionZ());
				}
			}

			if (((fwType == FW_TYPE_BT) && (mEnabledSensors & BTStream.EXG1_24BIT) > 0) 
					|| ((fwType == FW_TYPE_SD) && (mEnabledSensors & SDLogHeader.EXG1_24BIT) > 0)
					){
				int iexg1ch1 = getSignalIndex(Shimmer3.ObjectClusterSensorName.EXG1_CH1_24BIT);
				int iexg1ch2 = getSignalIndex(Shimmer3.ObjectClusterSensorName.EXG1_CH2_24BIT);
				int iexg1sta = getSignalIndex(Shimmer3.ObjectClusterSensorName.EXG1_STATUS);
				double exg1ch1 = (double)newPacketInt[iexg1ch1];
				double exg1ch2 = (double)newPacketInt[iexg1ch2];
				double exg1sta = (double)newPacketInt[iexg1sta];
				objectCluster.addData(Shimmer3.ObjectClusterSensorName.EXG1_STATUS,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg1sta);
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
					
					objectCluster.addData(Shimmer3.ObjectClusterSensorName.EXG1_STATUS,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.NO_UNITS,exg1sta);
					
					if (isEXGUsingDefaultECGConfiguration()||isEXGUsingDefaultRespirationConfiguration()){
						sensorNames[iexg1ch1]=Shimmer3.ObjectClusterSensorName.ECG_LL_RA_24BIT;
						objectCluster.addData(Shimmer3.ObjectClusterSensorName.ECG_LL_RA_24BIT,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg1ch1);
						sensorNames[iexg1ch2]=Shimmer3.ObjectClusterSensorName.ECG_LA_RA_24BIT;
						objectCluster.addData(Shimmer3.ObjectClusterSensorName.ECG_LA_RA_24BIT,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg1ch2);
						
						if(getFirmwareIdentifier()==FW_ID.GQ_802154){
							objectCluster.addData(Shimmer3.ObjectClusterSensorName.ECG_LA_RA_24BIT,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calexg1ch1);
							objectCluster.addData(Shimmer3.ObjectClusterSensorName.ECG_LA_RL_24BIT,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calexg1ch2);
						} else {
							objectCluster.addData(Shimmer3.ObjectClusterSensorName.ECG_LL_RA_24BIT,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calexg1ch1);
							objectCluster.addData(Shimmer3.ObjectClusterSensorName.ECG_LA_RA_24BIT,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calexg1ch2);
						}
					} 
					else if (isEXGUsingDefaultEMGConfiguration()){
						sensorNames[iexg1ch1]=Shimmer3.ObjectClusterSensorName.EMG_CH1_24BIT;
						sensorNames[iexg1ch2]=Shimmer3.ObjectClusterSensorName.EMG_CH2_24BIT;
						objectCluster.addData(Shimmer3.ObjectClusterSensorName.EMG_CH1_24BIT,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg1ch1);
						objectCluster.addData(Shimmer3.ObjectClusterSensorName.EMG_CH2_24BIT,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg1ch2);
						objectCluster.addData(Shimmer3.ObjectClusterSensorName.EMG_CH1_24BIT,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calexg1ch1);
						objectCluster.addData(Shimmer3.ObjectClusterSensorName.EMG_CH2_24BIT,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calexg1ch2);
					} 
					else if (isEXGUsingDefaultTestSignalConfiguration()){
						sensorNames[iexg1ch1]=Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP1_CH1_24BIT;
						sensorNames[iexg1ch2]=Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP1_CH2_24BIT;
						objectCluster.addData(Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP1_CH1_24BIT,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg1ch1);
						objectCluster.addData(Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP1_CH2_24BIT,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg1ch2);
						objectCluster.addData(Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP1_CH1_24BIT,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calexg1ch1);
						objectCluster.addData(Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP1_CH2_24BIT,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calexg1ch2);
					} 
					else {
						sensorNames[iexg1ch1]=Shimmer3.ObjectClusterSensorName.EXG1_CH1_24BIT;
						sensorNames[iexg1ch2]=Shimmer3.ObjectClusterSensorName.EXG1_CH2_24BIT;
						objectCluster.addData(Shimmer3.ObjectClusterSensorName.EXG1_CH1_24BIT,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg1ch1);
						objectCluster.addData(Shimmer3.ObjectClusterSensorName.EXG1_CH2_24BIT,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg1ch2);
						objectCluster.addData(Shimmer3.ObjectClusterSensorName.EXG1_CH1_24BIT,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calexg1ch1);
						objectCluster.addData(Shimmer3.ObjectClusterSensorName.EXG1_CH2_24BIT,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calexg1ch2);
					}
				}
			}
			if (((fwType == FW_TYPE_BT) && (mEnabledSensors & BTStream.EXG2_24BIT) > 0) 
					|| ((fwType == FW_TYPE_SD) && (mEnabledSensors & SDLogHeader.EXG2_24BIT) > 0)
					){
				int iexg2ch1 = getSignalIndex(Shimmer3.ObjectClusterSensorName.EXG2_CH1_24BIT);
				int iexg2ch2 = getSignalIndex(Shimmer3.ObjectClusterSensorName.EXG2_CH2_24BIT);
				int iexg2sta = getSignalIndex(Shimmer3.ObjectClusterSensorName.EXG2_STATUS);
				double exg2ch1 = (double)newPacketInt[iexg2ch1];
				double exg2ch2 = (double)newPacketInt[iexg2ch2];
				double exg2sta = (double)newPacketInt[iexg2sta];
				objectCluster.addData(Shimmer3.ObjectClusterSensorName.EXG2_STATUS,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg2sta);
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
					
					objectCluster.addData(Shimmer3.ObjectClusterSensorName.EXG2_STATUS,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.NO_UNITS,exg2sta);
					
					if (isEXGUsingDefaultECGConfiguration()||isEXGUsingDefaultRespirationConfiguration()){
						sensorNames[iexg2ch2]=Shimmer3.ObjectClusterSensorName.ECG_VX_RL_24BIT;
						objectCluster.addData(Shimmer3.ObjectClusterSensorName.ECG_VX_RL_24BIT,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg2ch2);
						objectCluster.addData(Shimmer3.ObjectClusterSensorName.ECG_VX_RL_24BIT,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calexg2ch2);
//						sensorNames[iexg2ch1]=Shimmer3.ObjectClusterSensorName.EXG2_CH1_24BIT;
//						objectCluster.addData(Shimmer3.ObjectClusterSensorName.EXG2_CH1_24BIT,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg2ch1));
//						objectCluster.addData(Shimmer3.ObjectClusterSensorName.EXG2_CH1_24BIT,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calexg2ch1));
						if (isEXGUsingDefaultRespirationConfiguration()){
							sensorNames[iexg2ch1]=Shimmer3.ObjectClusterSensorName.ECG_RESP_24BIT;
							objectCluster.addData(Shimmer3.ObjectClusterSensorName.ECG_RESP_24BIT,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg2ch1);
							objectCluster.addData(Shimmer3.ObjectClusterSensorName.ECG_RESP_24BIT,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calexg2ch1);
						}
					}
					else if (isEXGUsingDefaultEMGConfiguration()){
						sensorNames[iexg2ch1]=Shimmer3.ObjectClusterSensorName.EXG2_CH1_24BIT;
						sensorNames[iexg2ch2]=Shimmer3.ObjectClusterSensorName.EXG2_CH2_24BIT;
						objectCluster.addData(Shimmer3.ObjectClusterSensorName.EXG2_CH1_24BIT,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,0);
						objectCluster.addData(Shimmer3.ObjectClusterSensorName.EXG2_CH2_24BIT,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,0);
						objectCluster.addData(Shimmer3.ObjectClusterSensorName.EXG2_CH1_24BIT,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,0);
						objectCluster.addData(Shimmer3.ObjectClusterSensorName.EXG2_CH2_24BIT,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,0);
					} 
					else if (isEXGUsingDefaultTestSignalConfiguration()){
						sensorNames[iexg2ch1]=Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP2_CH1_24BIT;
						sensorNames[iexg2ch2]=Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP2_CH2_24BIT;
						objectCluster.addData(Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP2_CH1_24BIT,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg2ch1);
						objectCluster.addData(Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP2_CH2_24BIT,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg2ch2);
						objectCluster.addData(Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP2_CH1_24BIT,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calexg2ch1);
						objectCluster.addData(Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP2_CH2_24BIT,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calexg2ch2);
					} 
					else {
						sensorNames[iexg2ch1]=Shimmer3.ObjectClusterSensorName.EXG2_CH1_24BIT;
						sensorNames[iexg2ch2]=Shimmer3.ObjectClusterSensorName.EXG2_CH2_24BIT;
						objectCluster.addData(Shimmer3.ObjectClusterSensorName.EXG2_CH1_24BIT,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg2ch1);
						objectCluster.addData(Shimmer3.ObjectClusterSensorName.EXG2_CH2_24BIT,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg2ch2);
						objectCluster.addData(Shimmer3.ObjectClusterSensorName.EXG2_CH1_24BIT,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calexg2ch1);
						objectCluster.addData(Shimmer3.ObjectClusterSensorName.EXG2_CH2_24BIT,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calexg2ch2);	
					}
				}
			}
			
			if (((fwType == FW_TYPE_BT) && (mEnabledSensors & BTStream.EXG1_16BIT) > 0) 
					|| ((fwType == FW_TYPE_SD) && (mEnabledSensors & SDLogHeader.EXG1_16BIT) > 0)
					){
				int iexg1ch1 = getSignalIndex(Shimmer3.ObjectClusterSensorName.EXG1_CH1_16BIT);
				int iexg1ch2 = getSignalIndex(Shimmer3.ObjectClusterSensorName.EXG1_CH2_16BIT);
				int iexg1sta = getSignalIndex(Shimmer3.ObjectClusterSensorName.EXG1_STATUS);
				double exg1ch1 = (double)newPacketInt[iexg1ch1];
				double exg1ch2 = (double)newPacketInt[iexg1ch2];
				double exg1sta = (double)newPacketInt[iexg1sta];
				objectCluster.addData(Shimmer3.ObjectClusterSensorName.EXG1_STATUS,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg1sta);
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
						objectCluster.addData(Shimmer3.ObjectClusterSensorName.ECG_LL_RA_16BIT,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg1ch1);
						sensorNames[iexg1ch2]=Shimmer3.ObjectClusterSensorName.ECG_LA_RA_16BIT;
						objectCluster.addData(Shimmer3.ObjectClusterSensorName.ECG_LA_RA_16BIT,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg1ch2);
						if(getFirmwareIdentifier()==FW_ID.GQ_802154){
							objectCluster.addData(Shimmer3.ObjectClusterSensorName.ECG_LA_RA_16BIT,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calexg1ch1);
							objectCluster.addData(Shimmer3.ObjectClusterSensorName.ECG_LA_RL_16BIT,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calexg1ch2);
						} else {
							objectCluster.addData(Shimmer3.ObjectClusterSensorName.ECG_LL_RA_16BIT,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calexg1ch1);
							objectCluster.addData(Shimmer3.ObjectClusterSensorName.ECG_LA_RA_16BIT,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calexg1ch2);
						}
					} 
					else if (isEXGUsingDefaultEMGConfiguration()){
						sensorNames[iexg1ch1]=Shimmer3.ObjectClusterSensorName.EMG_CH1_16BIT;
						sensorNames[iexg1ch2]=Shimmer3.ObjectClusterSensorName.EMG_CH2_16BIT;
						objectCluster.addData(Shimmer3.ObjectClusterSensorName.EMG_CH1_16BIT,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg1ch1);
						objectCluster.addData(Shimmer3.ObjectClusterSensorName.EMG_CH2_16BIT,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg1ch2);
						objectCluster.addData(Shimmer3.ObjectClusterSensorName.EMG_CH1_16BIT,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calexg1ch1);
						objectCluster.addData(Shimmer3.ObjectClusterSensorName.EMG_CH2_16BIT,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calexg1ch2);
					} 
					else if (isEXGUsingDefaultTestSignalConfiguration()){
						sensorNames[iexg1ch1]=Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP1_CH1_16BIT;
						sensorNames[iexg1ch2]=Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP1_CH2_16BIT;
						objectCluster.addData(Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP1_CH1_16BIT,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg1ch1);
						objectCluster.addData(Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP1_CH2_16BIT,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg1ch2);
						objectCluster.addData(Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP1_CH1_16BIT,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calexg1ch1);
						objectCluster.addData(Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP1_CH2_16BIT,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calexg1ch2);
					} 
					else {
						sensorNames[iexg1ch1]=Shimmer3.ObjectClusterSensorName.EXG1_CH1_16BIT;
						sensorNames[iexg1ch2]=Shimmer3.ObjectClusterSensorName.EXG1_CH2_16BIT;
						objectCluster.addData(Shimmer3.ObjectClusterSensorName.EXG1_CH1_16BIT,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg1ch1);
						objectCluster.addData(Shimmer3.ObjectClusterSensorName.EXG1_CH2_16BIT,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg1ch2);
						objectCluster.addData(Shimmer3.ObjectClusterSensorName.EXG1_CH1_16BIT,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calexg1ch1);
						objectCluster.addData(Shimmer3.ObjectClusterSensorName.EXG1_CH2_16BIT,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calexg1ch2);
					}
				}
			}
			if (((fwType == FW_TYPE_BT) && (mEnabledSensors & BTStream.EXG2_16BIT) > 0) 
					|| ((fwType == FW_TYPE_SD) && (mEnabledSensors & SDLogHeader.EXG2_16BIT) > 0)
					){
				int iexg2ch1 = getSignalIndex(Shimmer3.ObjectClusterSensorName.EXG2_CH1_16BIT);
				int iexg2ch2 = getSignalIndex(Shimmer3.ObjectClusterSensorName.EXG2_CH2_16BIT);
				int iexg2sta = getSignalIndex(Shimmer3.ObjectClusterSensorName.EXG2_STATUS);
				double exg2ch1 = (double)newPacketInt[iexg2ch1];
				double exg2ch2 = (double)newPacketInt[iexg2ch2];
				double exg2sta = (double)newPacketInt[iexg2sta];
				objectCluster.addData(Shimmer3.ObjectClusterSensorName.EXG2_STATUS,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg2sta);
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
					if (isEXGUsingDefaultECGConfiguration()||isEXGUsingDefaultRespirationConfiguration()){
						sensorNames[iexg2ch2]=Shimmer3.ObjectClusterSensorName.ECG_VX_RL_16BIT;
						objectCluster.addData(Shimmer3.ObjectClusterSensorName.ECG_VX_RL_16BIT,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg2ch2);
						objectCluster.addData(Shimmer3.ObjectClusterSensorName.ECG_VX_RL_16BIT,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calexg2ch2);
//						sensorNames[iexg2ch1]=Shimmer3.ObjectClusterSensorName.EXG2_CH1_16BIT;
//						objectCluster.addData(Shimmer3.ObjectClusterSensorName.EXG2_CH1_16BIT,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg2ch1));
//						objectCluster.addData(Shimmer3.ObjectClusterSensorName.EXG2_CH1_16BIT,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calexg2ch1));
						if (isEXGUsingDefaultRespirationConfiguration()){
							sensorNames[iexg2ch1]=Shimmer3.ObjectClusterSensorName.ECG_RESP_16BIT;
							objectCluster.addData(Shimmer3.ObjectClusterSensorName.ECG_RESP_16BIT,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg2ch1);
							objectCluster.addData(Shimmer3.ObjectClusterSensorName.ECG_RESP_16BIT,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calexg2ch1);
						}
					}
					else if (isEXGUsingDefaultEMGConfiguration()){
						sensorNames[iexg2ch1]=Shimmer3.ObjectClusterSensorName.EXG2_CH1_16BIT;
						sensorNames[iexg2ch2]=Shimmer3.ObjectClusterSensorName.EXG2_CH2_16BIT;
						objectCluster.addData(Shimmer3.ObjectClusterSensorName.EXG2_CH1_16BIT,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,0);
						objectCluster.addData(Shimmer3.ObjectClusterSensorName.EXG2_CH2_16BIT,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,0);
						objectCluster.addData(Shimmer3.ObjectClusterSensorName.EXG2_CH1_16BIT,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,0);
						objectCluster.addData(Shimmer3.ObjectClusterSensorName.EXG2_CH2_16BIT,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,0);
					} 
					else if (isEXGUsingDefaultTestSignalConfiguration()){
						sensorNames[iexg2ch1]=Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP2_CH1_16BIT;
						sensorNames[iexg2ch2]=Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP2_CH2_16BIT;
						objectCluster.addData(Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP2_CH1_16BIT,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg2ch1);
						objectCluster.addData(Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP2_CH2_16BIT,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg2ch2);
						objectCluster.addData(Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP2_CH1_16BIT,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calexg2ch1);
						objectCluster.addData(Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP2_CH2_16BIT,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calexg2ch2);
					} 
					else {
						sensorNames[iexg2ch1]=Shimmer3.ObjectClusterSensorName.EXG2_CH1_16BIT;
						sensorNames[iexg2ch2]=Shimmer3.ObjectClusterSensorName.EXG2_CH2_16BIT;
						objectCluster.addData(Shimmer3.ObjectClusterSensorName.EXG2_CH1_16BIT,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg2ch1);
						objectCluster.addData(Shimmer3.ObjectClusterSensorName.EXG2_CH2_16BIT,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg2ch1);
						objectCluster.addData(Shimmer3.ObjectClusterSensorName.EXG2_CH1_16BIT,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calexg2ch1);
						objectCluster.addData(Shimmer3.ObjectClusterSensorName.EXG2_CH2_16BIT,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calexg2ch1);	
					}
				}
			}

			if (((fwType == FW_TYPE_BT) && (mEnabledSensors & BTStream.BMP180) > 0) 
					|| ((fwType == FW_TYPE_SD) && (mEnabledSensors & SDLogHeader.BMP180) > 0)
					){
				int iUT = getSignalIndex(Shimmer3.ObjectClusterSensorName.TEMPERATURE_BMP180);
				int iUP = getSignalIndex(Shimmer3.ObjectClusterSensorName.PRESSURE_BMP180);
				double UT = (double)newPacketInt[iUT];
				double UP = (double)newPacketInt[iUP];
				UP=UP/Math.pow(2,8-mPressureResolution);
				objectCluster.addData(Shimmer3.ObjectClusterSensorName.PRESSURE_BMP180,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,UP);
				objectCluster.addData(Shimmer3.ObjectClusterSensorName.TEMPERATURE_BMP180,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,UT);
				uncalibratedData[iUT]=(double)newPacketInt[iUT];
				uncalibratedData[iUP]=(double)newPacketInt[iUP];
				uncalibratedDataUnits[iUT]=CHANNEL_UNITS.NO_UNITS;
				uncalibratedDataUnits[iUP]=CHANNEL_UNITS.NO_UNITS;
				if (mEnableCalibration){
					double[] bmp180caldata= calibratePressureSensorData(UP,UT);
					objectCluster.addData(Shimmer3.ObjectClusterSensorName.PRESSURE_BMP180,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.KPASCAL,bmp180caldata[0]/1000);
					objectCluster.addData(Shimmer3.ObjectClusterSensorName.TEMPERATURE_BMP180,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.DEGREES_CELSUIS,bmp180caldata[1]);
					calibratedData[iUT]=bmp180caldata[1];
					calibratedData[iUP]=bmp180caldata[0]/1000;
					calibratedDataUnits[iUT]=CHANNEL_UNITS.DEGREES_CELSUIS;
					calibratedDataUnits[iUP]=CHANNEL_UNITS.KPASCAL;
				}
			}

			if (((fwType == FW_TYPE_BT) && (mEnabledSensors & BTStream.BRIDGE_AMP) > 0) 
					|| ((fwType == FW_TYPE_SD) && (mEnabledSensors & SDLogHeader.BRIDGE_AMP) > 0)
					) {
				int iBAHigh = getSignalIndex(Shimmer3.ObjectClusterSensorName.BRIDGE_AMP_HIGH);
				int iBALow = getSignalIndex(Shimmer3.ObjectClusterSensorName.BRIDGE_AMP_LOW);
				objectCluster.addData(Shimmer3.ObjectClusterSensorName.BRIDGE_AMP_HIGH,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iBAHigh]);
				objectCluster.addData(Shimmer3.ObjectClusterSensorName.BRIDGE_AMP_LOW,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iBALow]);
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
					objectCluster.addData(Shimmer3.ObjectClusterSensorName.BRIDGE_AMP_HIGH,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calibratedData[iBAHigh]);
					objectCluster.addData(Shimmer3.ObjectClusterSensorName.BRIDGE_AMP_LOW,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calibratedData[iBALow]);	
				}
			}
			
			if (((fwType == FW_TYPE_BT) && (mEnabledSensors & BTStream.GSR) > 0) 
					|| ((fwType == FW_TYPE_SD) && (mEnabledSensors & SDLogHeader.GSR) > 0)
					) {
				int iGSR = getSignalIndex(Shimmer3.ObjectClusterSensorName.GSR);
				double p1=0,p2=0;//,p3=0,p4=0,p5=0;
				if (fwType == FW_TYPE_SD && getFirmwareVersionMajor() ==0 && getFirmwareVersionMinor()==9){
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
						if (getHardwareVersion()!=HW_ID.SHIMMER_3){
							p1 = 0.0373;
							p2 = -24.9915;
						} else {
							//p1 = 0.0363;
							//p2 = -24.8617;
							p1 = 0.0373;
							p2 = -24.9915;
						}
					} else if (mGSRRange==1 || newGSRRange==1) {
						if (getHardwareVersion()!=HW_ID.SHIMMER_3){
							p1 = 0.0054;
							p2 = -3.5194;
						} else {
							//p1 = 0.0051;
							//p2 = -3.8357;
							p1 = 0.0054;
							p2 = -3.5194;
						}
					} else if (mGSRRange==2 || newGSRRange==2) {
						if (getHardwareVersion()!=HW_ID.SHIMMER_3){
							p1 = 0.0015;
							p2 = -1.0163;
						} else {
							//p1 = 0.0015;
							//p2 = -1.0067;
							p1 = 0.0015;
							p2 = -1.0163;
						}
					} else if (mGSRRange==3  || newGSRRange==3) {
						if (getHardwareVersion()!=HW_ID.SHIMMER_3){
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
						if (getHardwareVersion()!=HW_ID.SHIMMER_3){
							p1 = 0.0373;
							p2 = -24.9915;

						} else { //Values have been reverted to 2r values
							//p1 = 0.0363;
							//p2 = -24.8617;
							p1 = 0.0373;
							p2 = -24.9915;
						}
					} else if (mGSRRange==1 || newGSRRange==1) {
						if (getHardwareVersion()!=HW_ID.SHIMMER_3){
							p1 = 0.0054;
							p2 = -3.5194;
						} else {
							//p1 = 0.0051;
							//p2 = -3.8357;
							p1 = 0.0054;
							p2 = -3.5194;
						}
					} else if (mGSRRange==2 || newGSRRange==2) {
						if (getHardwareVersion()!=HW_ID.SHIMMER_3){
							p1 = 0.0015;
							p2 = -1.0163;
						} else {
							//p1 = 0.0015;
							//p2 = -1.0067;
							p1 = 0.0015;
							p2 = -1.0163;
						}
					} else if (mGSRRange==3  || newGSRRange==3) {
						if (getHardwareVersion()!=HW_ID.SHIMMER_3){
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
				objectCluster.addData(Shimmer3.ObjectClusterSensorName.GSR,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iGSR]);
				uncalibratedData[iGSR]=(double)newPacketInt[iGSR];
				uncalibratedDataUnits[iGSR]=CHANNEL_UNITS.NO_UNITS;
				if (mEnableCalibration){
					if(getFirmwareIdentifier()==FW_ID.GQ_802154){
						calibratedData[iGSR] = SensorGSR.calibrateGsrDataToSiemens(tempData[0],p1,p2);
						calibratedDataUnits[iGSR]=CHANNEL_UNITS.U_SIEMENS;
						objectCluster.addData(Shimmer3.ObjectClusterSensorName.GSR,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.U_SIEMENS,calibratedData[iGSR]);
					}
					else {
						calibratedData[iGSR] = SensorGSR.calibrateGsrData(tempData[0],p1,p2);
						calibratedDataUnits[iGSR]=CHANNEL_UNITS.KOHMS;
						objectCluster.addData(Shimmer3.ObjectClusterSensorName.GSR,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.KOHMS,calibratedData[iGSR]);
					}
				}
			}

			if (((fwType == FW_TYPE_SD) && (mEnabledSensors & SDLogHeader.GYRO_MPU_MPL) > 0)){
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

			if (((fwType == FW_TYPE_SD) && (mEnabledSensors & SDLogHeader.ACCEL_MPU_MPL) > 0)){
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

			if (((fwType == FW_TYPE_SD) && (mEnabledSensors & SDLogHeader.MAG_MPU_MPL) > 0)){
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

			if (((fwType == FW_TYPE_SD) && (mEnabledSensors & SDLogHeader.MPL_QUAT_6DOF) > 0)){
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
			if (((fwType == FW_TYPE_SD) && (mEnabledSensors & SDLogHeader.MPL_TEMPERATURE) > 0)){
				int iT = getSignalIndex(Shimmer3.ObjectClusterSensorName.MPL_TEMPERATURE);
				calibratedData[iT] = (double)newPacketInt[iT]/Math.pow(2, 16);
				calibratedDataUnits[iT] = CHANNEL_UNITS.DEGREES_CELSUIS;
				uncalibratedData[iT] = (double)newPacketInt[iT];
				uncalibratedDataUnits[iT] = CHANNEL_UNITS.NO_UNITS;
			}
			
			if (((fwType == FW_TYPE_SD) && (mEnabledSensors & SDLogHeader.MPL_PEDOMETER) > 0)){
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
			
			if (((fwType == FW_TYPE_SD) && (mEnabledSensors & SDLogHeader.MPL_HEADING) > 0)){
				int iH = getSignalIndex(Shimmer3.ObjectClusterSensorName.MPL_HEADING);
				calibratedData[iH] = (double)newPacketInt[iH]/Math.pow(2, 16);
				calibratedDataUnits[iH] = CHANNEL_UNITS.DEGREES;
				uncalibratedData[iH] = (double)newPacketInt[iH];
				uncalibratedDataUnits[iH] = CHANNEL_UNITS.NO_UNITS;
			}

			//TODO: separate out tap dir and cnt to two channels 
			//Bits 7-5 - Direction,	Bits 4-0 - Count
			if (((fwType == FW_TYPE_SD) && (mEnabledSensors & SDLogHeader.MPL_TAP) > 0)){
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
			if (((fwType == FW_TYPE_SD) && (mEnabledSensors & SDLogHeader.MPL_MOTION_ORIENT) > 0)){
				int iMotOrient = getSignalIndex(Shimmer3.ObjectClusterSensorName.MOTIONANDORIENT);
				calibratedData[iMotOrient] = (double)newPacketInt[iMotOrient];
				calibratedDataUnits[iMotOrient] = CHANNEL_UNITS.NO_UNITS;
				uncalibratedData[iMotOrient] = (double)newPacketInt[iMotOrient];
				uncalibratedDataUnits[iMotOrient] = CHANNEL_UNITS.NO_UNITS;
			}
			
			if ((fwType == FW_TYPE_SD) && (mEnabledSensors & SDLogHeader.ECG_TO_HR_FW) > 0){
				int sigIndex = getSignalIndex(Shimmer3.ObjectClusterSensorName.ECG_TO_HR);
				objectCluster.addData(Shimmer3.ObjectClusterSensorName.ECG_TO_HR,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.BEATS_PER_MINUTE,(double)newPacketInt[sigIndex]);
//				uncalibratedData[sigIndex]=(double)newPacketInt[sigIndex];
//				uncalibratedDataUnits[sigIndex]=CHANNEL_UNITS.BEATS_PER_MINUTE;
				calibratedData[sigIndex]=(double)newPacketInt[sigIndex];
				calibratedDataUnits[sigIndex]=CHANNEL_UNITS.BEATS_PER_MINUTE;
			}

			//Additional Channels Offset
			int additionalChannelsOffset = calibratedData.length-numAdditionalChannels+1; //+1 because timestamp channel appears at the start
			
			if (fwType == FW_TYPE_BT){ // Here so as not to mess with ShimmerSDLog
				if(isEXGUsingDefaultECGConfiguration()||isEXGUsingDefaultRespirationConfiguration()){
					if (((fwType == FW_TYPE_BT) && (mEnabledSensors & BTStream.EXG1_16BIT) > 0) 
							|| ((fwType == FW_TYPE_SD) && (mEnabledSensors & SDLogHeader.EXG1_16BIT) > 0)){
						
						int iexg1ch1 = getSignalIndex(Shimmer3.ObjectClusterSensorName.EXG1_CH1_16BIT);
						int iexg1ch2 = getSignalIndex(Shimmer3.ObjectClusterSensorName.EXG1_CH2_16BIT);
	
						sensorNames[additionalChannelsOffset] = Shimmer3.ObjectClusterSensorName.ECG_LL_LA_16BIT;
						
						uncalibratedData[additionalChannelsOffset]= uncalibratedData[iexg1ch1] - uncalibratedData[iexg1ch2];
						uncalibratedDataUnits[additionalChannelsOffset]=CHANNEL_UNITS.NO_UNITS;
						objectCluster.addData(Shimmer3.ObjectClusterSensorName.ECG_LL_LA_16BIT,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,uncalibratedData[additionalChannelsOffset]);
						
						if(mEnableCalibration){
							calibratedData[additionalChannelsOffset]= calibratedData[iexg1ch1] - calibratedData[iexg1ch2];
							calibratedDataUnits[additionalChannelsOffset]=CHANNEL_UNITS.MILLIVOLTS;
							objectCluster.addData(Shimmer3.ObjectClusterSensorName.ECG_LL_LA_16BIT,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calibratedData[additionalChannelsOffset]);
						}
						additionalChannelsOffset += 1;
	
						
					}
					else if (((fwType == FW_TYPE_BT) && (mEnabledSensors & BTStream.EXG1_24BIT) > 0) 
							|| ((fwType == FW_TYPE_SD) && (mEnabledSensors & SDLogHeader.EXG1_24BIT) > 0)){
						
						int iexg1ch1 = getSignalIndex(Shimmer3.ObjectClusterSensorName.EXG1_CH1_24BIT);
						int iexg1ch2 = getSignalIndex(Shimmer3.ObjectClusterSensorName.EXG1_CH2_24BIT);
	
						sensorNames[additionalChannelsOffset] = Shimmer3.ObjectClusterSensorName.ECG_LL_LA_24BIT;
						
						uncalibratedData[additionalChannelsOffset]= uncalibratedData[iexg1ch1] - uncalibratedData[iexg1ch2];
						uncalibratedDataUnits[additionalChannelsOffset]=CHANNEL_UNITS.NO_UNITS;
						objectCluster.addData(Shimmer3.ObjectClusterSensorName.ECG_LL_LA_24BIT,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,uncalibratedData[additionalChannelsOffset]);
						
						if(mEnableCalibration){
							calibratedData[additionalChannelsOffset]= calibratedData[iexg1ch1] - calibratedData[iexg1ch2];
							calibratedDataUnits[additionalChannelsOffset]=CHANNEL_UNITS.MILLIVOLTS;
							objectCluster.addData(Shimmer3.ObjectClusterSensorName.ECG_LL_LA_24BIT,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calibratedData[additionalChannelsOffset]);
						}
						additionalChannelsOffset += 1;
					}
				}
			}
			else if(fwType == FW_TYPE_SD){
				if (getHardwareVersion() == HW_ID.SHIMMER_3){
					if(isEXGUsingDefaultECGConfigurationForSDFW()){
						//calculate the ECG Derived sensor for SD (LL-LA) and replace it for the ECG Respiration
						if (((mEnabledSensors & BTStream.EXG1_16BIT) > 0)){
							
							int iexg1ch1 = getSignalIndex(Shimmer3.ObjectClusterSensorName.EXG1_CH1_16BIT);
							int iexg1ch2 = getSignalIndex(Shimmer3.ObjectClusterSensorName.EXG1_CH2_16BIT);
							int iexg2ch1 = getSignalIndex(Shimmer3.ObjectClusterSensorName.EXG2_CH1_16BIT);
							
							sensorNames[iexg2ch1] = Shimmer3.ObjectClusterSensorName.ECG_LL_LA_16BIT;
							objectCluster.removeAll(Shimmer3.ObjectClusterSensorName.EXG2_CH1_16BIT);
							
							uncalibratedData[iexg2ch1]= uncalibratedData[iexg1ch1] - uncalibratedData[iexg1ch2];
							uncalibratedDataUnits[iexg2ch1]=CHANNEL_UNITS.NO_UNITS;
							objectCluster.addData(Shimmer3.ObjectClusterSensorName.ECG_LL_LA_16BIT,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,uncalibratedData[iexg2ch1]);
							
							if(mEnableCalibration){
								calibratedData[iexg2ch1]= calibratedData[iexg1ch1] - calibratedData[iexg1ch2];
								calibratedDataUnits[iexg2ch1]=CHANNEL_UNITS.MILLIVOLTS;
								objectCluster.addData(Shimmer3.ObjectClusterSensorName.ECG_LL_LA_16BIT,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calibratedData[iexg2ch1]);
							}
	
						}
						else if (((mEnabledSensors & BTStream.EXG1_24BIT) > 0)){
							
							int iexg1ch1 = getSignalIndex(Shimmer3.ObjectClusterSensorName.EXG1_CH1_24BIT);
							int iexg1ch2 = getSignalIndex(Shimmer3.ObjectClusterSensorName.EXG1_CH2_24BIT);
							int iexg2ch1 = getSignalIndex(Shimmer3.ObjectClusterSensorName.EXG2_CH1_24BIT);
							
							sensorNames[iexg2ch1] = Shimmer3.ObjectClusterSensorName.ECG_LL_LA_24BIT;
							objectCluster.removeAll(Shimmer3.ObjectClusterSensorName.EXG1_CH2_24BIT);
							
							uncalibratedData[iexg2ch1]= uncalibratedData[iexg1ch1] - uncalibratedData[iexg1ch2];
							uncalibratedDataUnits[iexg2ch1]=CHANNEL_UNITS.NO_UNITS;
							objectCluster.addData(Shimmer3.ObjectClusterSensorName.ECG_LL_LA_24BIT,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,uncalibratedData[iexg2ch1]);
							
							if(mEnableCalibration){
								calibratedData[iexg2ch1]= calibratedData[iexg1ch1] - calibratedData[iexg1ch2];
								calibratedDataUnits[iexg2ch1]=CHANNEL_UNITS.MILLIVOLTS;
								objectCluster.addData(Shimmer3.ObjectClusterSensorName.ECG_LL_LA_24BIT,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calibratedData[iexg2ch1]);
							}
							
						}
					}
				}
			}
			
			if(fwType == FW_TYPE_BT){
				objectCluster.addData(Shimmer3.ObjectClusterSensorName.BATT_PERCENTAGE,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.PERCENT,(double)mShimmerBattStatusDetails.mEstimatedChargePercentage);
				calibratedData[additionalChannelsOffset] = (double)mShimmerBattStatusDetails.mEstimatedChargePercentage;
				calibratedDataUnits[additionalChannelsOffset] = CHANNEL_UNITS.PERCENT;
				uncalibratedData[additionalChannelsOffset] = Double.NaN;
				uncalibratedDataUnits[additionalChannelsOffset] = "";
				sensorNames[additionalChannelsOffset] = Shimmer3.ObjectClusterSensorName.BATT_PERCENTAGE;
				additionalChannelsOffset+=1;

				objectCluster.addData(Shimmer3.ObjectClusterSensorName.PACKET_RECEPTION_RATE_CURRENT,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.PERCENT,(double)mPacketReceptionRateCurrent);
				calibratedData[additionalChannelsOffset] = (double)mPacketReceptionRateCurrent;
				calibratedDataUnits[additionalChannelsOffset] = CHANNEL_UNITS.PERCENT;
				uncalibratedData[additionalChannelsOffset] = Double.NaN;
				uncalibratedDataUnits[additionalChannelsOffset] = "";
				sensorNames[additionalChannelsOffset] = Shimmer3.ObjectClusterSensorName.PACKET_RECEPTION_RATE_CURRENT;
				additionalChannelsOffset+=1;

				objectCluster.addData(Shimmer3.ObjectClusterSensorName.PACKET_RECEPTION_RATE_TRIAL,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.PERCENT,(double)mPacketReceptionRate);
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
				
				//event
				objectCluster.addData(Shimmer3.ObjectClusterSensorName.EVENT_MARKER,CHANNEL_TYPE.CAL.toString(), CHANNEL_UNITS.NO_UNITS, mEventMarkers);
				untriggerEventIfLastOneWasPulse();
//				if(mEventMarkersIsPulse){
//					mEventMarkersIsPulse = false;
//					setEventUntrigger(mEventMarkersCodeLast);
//				}
				
//				calibratedData[additionalChannelsOffset] = (double)mPacketReceptionRate;
//				calibratedDataUnits[additionalChannelsOffset] = CHANNEL_UNITS.PERCENT;
//				uncalibratedData[additionalChannelsOffset] = Double.NaN;
//				uncalibratedDataUnits[additionalChannelsOffset] = "";
//				sensorNames[additionalChannelsOffset] = Shimmer3.ObjectClusterSensorName.PACKET_RECEPTION_RATE_TRIAL;
//				additionalChannelsOffset+=1;
			}
			
			objectCluster.mCalData = calibratedData;
			objectCluster.mUncalData = uncalibratedData;
			objectCluster.mUnitCal = calibratedDataUnits;
			objectCluster.mUnitUncal = uncalibratedDataUnits;
			objectCluster.mSensorNames = sensorNames;

//			 processAlgorithmData(objectCluster);	

		} 
		else if (getHardwareVersion()==HW_ID.SHIMMER_2 || getHardwareVersion()==HW_ID.SHIMMER_2R){
			 //start of Shimmer2

			int iTimeStamp=getSignalIndex(Configuration.Shimmer2.ObjectClusterSensorName.TIMESTAMP); //find index
			double calibratedTS = calibrateTimeStamp((double)newPacketInt[iTimeStamp]);

			//TIMESTAMP
			if (fwType == FW_TYPE_BT){
				objectCluster.addData(Configuration.Shimmer2.ObjectClusterSensorName.TIMESTAMP,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iTimeStamp]);
				uncalibratedData[iTimeStamp] = (double)newPacketInt[iTimeStamp];
				uncalibratedDataUnits[iTimeStamp] = CHANNEL_UNITS.NO_UNITS;
				if (mEnableCalibration){
					objectCluster.addData(Configuration.Shimmer2.ObjectClusterSensorName.TIMESTAMP,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLISECONDS,calibratedTS);
					calibratedData[iTimeStamp] = calibratedTS;
					calibratedDataUnits[iTimeStamp] = CHANNEL_UNITS.MILLISECONDS;
				}
			}
			
			if ((mEnabledSensors & SENSOR_ACCEL) > 0){
				int iAccelX=getSignalIndex(Shimmer2.ObjectClusterSensorName.ACCEL_X); //find index
				int iAccelY=getSignalIndex(Shimmer2.ObjectClusterSensorName.ACCEL_Y); //find index
				int iAccelZ=getSignalIndex(Shimmer2.ObjectClusterSensorName.ACCEL_Z); //find index
				objectCluster.addData(Shimmer2.ObjectClusterSensorName.ACCEL_X,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iAccelX]);
				objectCluster.addData(Shimmer2.ObjectClusterSensorName.ACCEL_Y,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iAccelY]);
				objectCluster.addData(Shimmer2.ObjectClusterSensorName.ACCEL_Z,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iAccelZ]);
				if (mEnableCalibration){
					tempData[0]=(double)newPacketInt[iAccelX];
					tempData[1]=(double)newPacketInt[iAccelY];
					tempData[2]=(double)newPacketInt[iAccelZ];
					double[] accelCalibratedData=UtilCalibration.calibrateInertialSensorData(tempData, mAlignmentMatrixAnalogAccel, mSensitivityMatrixAnalogAccel, mOffsetVectorAnalogAccel);
					calibratedData[iAccelX]=accelCalibratedData[0];
					calibratedData[iAccelY]=accelCalibratedData[1];
					calibratedData[iAccelZ]=accelCalibratedData[2];
					if (mDefaultCalibrationParametersAccel == true) {
						objectCluster.addData(Shimmer2.ObjectClusterSensorName.ACCEL_X,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.METER_PER_SECOND_SQUARE+"*",accelCalibratedData[0]);
						objectCluster.addData(Shimmer2.ObjectClusterSensorName.ACCEL_Y,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.METER_PER_SECOND_SQUARE+"*",accelCalibratedData[1]);
						objectCluster.addData(Shimmer2.ObjectClusterSensorName.ACCEL_Z,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.METER_PER_SECOND_SQUARE+"*",accelCalibratedData[2]);
						accelerometer.x=accelCalibratedData[0];
						accelerometer.y=accelCalibratedData[1];
						accelerometer.z=accelCalibratedData[2];
					} else {
						objectCluster.addData(Shimmer2.ObjectClusterSensorName.ACCEL_X,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.METER_PER_SECOND_SQUARE,accelCalibratedData[0]);
						objectCluster.addData(Shimmer2.ObjectClusterSensorName.ACCEL_Y,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.METER_PER_SECOND_SQUARE,accelCalibratedData[1]);
						objectCluster.addData(Shimmer2.ObjectClusterSensorName.ACCEL_Z,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.METER_PER_SECOND_SQUARE,accelCalibratedData[2]);
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
				objectCluster.addData(Shimmer2.ObjectClusterSensorName.GYRO_X,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iGyroX]);
				objectCluster.addData(Shimmer2.ObjectClusterSensorName.GYRO_Y,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iGyroY]);
				objectCluster.addData(Shimmer2.ObjectClusterSensorName.GYRO_Z,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iGyroZ]);
				if (mEnableCalibration){
					tempData[0]=(double)newPacketInt[iGyroX];
					tempData[1]=(double)newPacketInt[iGyroY];
					tempData[2]=(double)newPacketInt[iGyroZ];
					double[] gyroCalibratedData=UtilCalibration.calibrateInertialSensorData(tempData, mAlignmentMatrixGyroscope, mSensitivityMatrixGyroscope, mOffsetVectorGyroscope);
					calibratedData[iGyroX]=gyroCalibratedData[0];
					calibratedData[iGyroY]=gyroCalibratedData[1];
					calibratedData[iGyroZ]=gyroCalibratedData[2];
					if (mDefaultCalibrationParametersGyro == true) {
						objectCluster.addData(Shimmer2.ObjectClusterSensorName.GYRO_X,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.DEGREES_PER_SECOND+"*",gyroCalibratedData[0]);
						objectCluster.addData(Shimmer2.ObjectClusterSensorName.GYRO_Y,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.DEGREES_PER_SECOND+"*",gyroCalibratedData[1]);
						objectCluster.addData(Shimmer2.ObjectClusterSensorName.GYRO_Z,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.DEGREES_PER_SECOND+"*",gyroCalibratedData[2]);
						gyroscope.x=gyroCalibratedData[0]*Math.PI/180;
						gyroscope.y=gyroCalibratedData[1]*Math.PI/180;
						gyroscope.z=gyroCalibratedData[2]*Math.PI/180;
					} else {
						objectCluster.addData(Shimmer2.ObjectClusterSensorName.GYRO_X,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.DEGREES_PER_SECOND,gyroCalibratedData[0]);
						objectCluster.addData(Shimmer2.ObjectClusterSensorName.GYRO_Y,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.DEGREES_PER_SECOND,gyroCalibratedData[1]);
						objectCluster.addData(Shimmer2.ObjectClusterSensorName.GYRO_Z,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.DEGREES_PER_SECOND,gyroCalibratedData[2]);
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
				objectCluster.addData(Shimmer2.ObjectClusterSensorName.MAG_X,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iMagX]);
				objectCluster.addData(Shimmer2.ObjectClusterSensorName.MAG_Y,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iMagY]);
				objectCluster.addData(Shimmer2.ObjectClusterSensorName.MAG_Z,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iMagZ]);
				if (mEnableCalibration){
					tempData[0]=(double)newPacketInt[iMagX];
					tempData[1]=(double)newPacketInt[iMagY];
					tempData[2]=(double)newPacketInt[iMagZ];
					double[] magCalibratedData=UtilCalibration.calibrateInertialSensorData(tempData, mAlignmentMatrixMagnetometer, mSensitivityMatrixMagnetometer, mOffsetVectorMagnetometer);
					calibratedData[iMagX]=magCalibratedData[0];
					calibratedData[iMagY]=magCalibratedData[1];
					calibratedData[iMagZ]=magCalibratedData[2];
					if (mDefaultCalibrationParametersMag == true) {
						objectCluster.addData(Shimmer2.ObjectClusterSensorName.MAG_X,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL_FLUX+"*",magCalibratedData[0]);
						objectCluster.addData(Shimmer2.ObjectClusterSensorName.MAG_Y,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL_FLUX+"*",magCalibratedData[1]);
						objectCluster.addData(Shimmer2.ObjectClusterSensorName.MAG_Z,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL_FLUX+"*",magCalibratedData[2]);
						magnetometer.x=magCalibratedData[0];
						magnetometer.y=magCalibratedData[1];
						magnetometer.z=magCalibratedData[2];
					} else {
						objectCluster.addData(Shimmer2.ObjectClusterSensorName.MAG_X,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL_FLUX,magCalibratedData[0]);
						objectCluster.addData(Shimmer2.ObjectClusterSensorName.MAG_Y,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL_FLUX,magCalibratedData[1]);
						objectCluster.addData(Shimmer2.ObjectClusterSensorName.MAG_Z,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL_FLUX,magCalibratedData[2]);
						magnetometer.x=magCalibratedData[0];
						magnetometer.y=magCalibratedData[1];
						magnetometer.z=magCalibratedData[2];
					}
				}
			}


			if ((mEnabledSensors & SENSOR_ACCEL) > 0 && (mEnabledSensors & SENSOR_GYRO) > 0 && (mEnabledSensors & SENSOR_MAG) > 0 && mOrientationEnabled ){
				if (mEnableCalibration){
					if (mOrientationAlgo==null){
						mOrientationAlgo = new GradDes3DOrientation(0.4, (double)1/getSamplingRateShimmer(), 1, 0, 0,0);
					}
					Orientation3DObject q = mOrientationAlgo.update(accelerometer.x,accelerometer.y,accelerometer.z, gyroscope.x,gyroscope.y,gyroscope.z, magnetometer.x,magnetometer.y,magnetometer.z);
					objectCluster.addData(Shimmer2.ObjectClusterSensorName.AXIS_ANGLE_A,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL,q.getTheta());
					objectCluster.addData(Shimmer2.ObjectClusterSensorName.AXIS_ANGLE_X,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL,q.getAngleX());
					objectCluster.addData(Shimmer2.ObjectClusterSensorName.AXIS_ANGLE_Y,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL,q.getAngleY());
					objectCluster.addData(Shimmer2.ObjectClusterSensorName.AXIS_ANGLE_Z,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL,q.getAngleZ());
					objectCluster.addData(Shimmer2.ObjectClusterSensorName.QUAT_MADGE_9DOF_W,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL,q.getQuaternionW());
					objectCluster.addData(Shimmer2.ObjectClusterSensorName.QUAT_MADGE_9DOF_X,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL,q.getQuaternionX());
					objectCluster.addData(Shimmer2.ObjectClusterSensorName.QUAT_MADGE_9DOF_Y,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL,q.getQuaternionY());
					objectCluster.addData(Shimmer2.ObjectClusterSensorName.QUAT_MADGE_9DOF_Z,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL,q.getQuaternionZ());
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
				objectCluster.addData(Shimmer2.ObjectClusterSensorName.GSR,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iGSR]);
				if (mEnableCalibration){
					tempData[0] = (double)newPacketInt[iGSR];
					calibratedData[iGSR] = SensorGSR.calibrateGsrData(tempData[0],p1,p2);
					objectCluster.addData(Shimmer2.ObjectClusterSensorName.GSR,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.KOHMS,calibratedData[iGSR]);
				}
			}
			if ((mEnabledSensors & SENSOR_ECG) > 0) {
				int iECGRALL = getSignalIndex(Shimmer2.ObjectClusterSensorName.ECG_RA_LL);
				int iECGLALL = getSignalIndex(Shimmer2.ObjectClusterSensorName.ECG_LA_LL);
				objectCluster.addData(Shimmer2.ObjectClusterSensorName.ECG_RA_LL,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iECGRALL]);
				objectCluster.addData(Shimmer2.ObjectClusterSensorName.ECG_LA_LL,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iECGLALL]);
				if (mEnableCalibration){
					tempData[0] = (double)newPacketInt[iECGRALL];
					tempData[1] = (double)newPacketInt[iECGLALL];
					calibratedData[iECGRALL]=calibrateU12AdcValue(tempData[0],OffsetECGRALL,3,GainECGRALL);
					calibratedData[iECGLALL]=calibrateU12AdcValue(tempData[1],OffsetECGLALL,3,GainECGLALL);
					if (mDefaultCalibrationParametersECG == true) {
						objectCluster.addData(Shimmer2.ObjectClusterSensorName.ECG_RA_LL,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS+"*",calibratedData[iECGRALL]);
						objectCluster.addData(Shimmer2.ObjectClusterSensorName.ECG_LA_LL,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS+"*",calibratedData[iECGLALL]);
					} else {
						objectCluster.addData(Shimmer2.ObjectClusterSensorName.ECG_RA_LL,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calibratedData[iECGRALL]);
						objectCluster.addData(Shimmer2.ObjectClusterSensorName.ECG_LA_LL,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calibratedData[iECGLALL]);
					}
				}
			}
			if ((mEnabledSensors & SENSOR_EMG) > 0) {
				int iEMG = getSignalIndex(Shimmer2.ObjectClusterSensorName.EMG);
				objectCluster.addData(Shimmer2.ObjectClusterSensorName.EMG,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iEMG]);
				if (mEnableCalibration){
					tempData[0] = (double)newPacketInt[iEMG];
					calibratedData[iEMG]=calibrateU12AdcValue(tempData[0],OffsetEMG,3,GainEMG);
					if (mDefaultCalibrationParametersEMG == true){
						objectCluster.addData(Shimmer2.ObjectClusterSensorName.EMG,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS+"*",calibratedData[iEMG]);
					} else {
						objectCluster.addData(Shimmer2.ObjectClusterSensorName.EMG,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calibratedData[iEMG]);
					}
				}
			}
			if ((mEnabledSensors & SENSOR_BRIDGE_AMP) > 0) {
				int iBAHigh = getSignalIndex(Shimmer2.ObjectClusterSensorName.BRIDGE_AMP_HIGH);
				int iBALow = getSignalIndex(Shimmer2.ObjectClusterSensorName.BRIDGE_AMP_LOW);
				objectCluster.addData(Shimmer2.ObjectClusterSensorName.BRIDGE_AMP_HIGH,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iBAHigh]);
				objectCluster.addData(Shimmer2.ObjectClusterSensorName.BRIDGE_AMP_LOW,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iBALow]);
				if (mEnableCalibration){
					tempData[0] = (double)newPacketInt[iBAHigh];
					tempData[1] = (double)newPacketInt[iBALow];
					calibratedData[iBAHigh]=calibrateU12AdcValue(tempData[0],60,3,551);
					calibratedData[iBALow]=calibrateU12AdcValue(tempData[1],1950,3,183.7);
					objectCluster.addData(Shimmer2.ObjectClusterSensorName.BRIDGE_AMP_HIGH,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calibratedData[iBAHigh]);
					objectCluster.addData(Shimmer2.ObjectClusterSensorName.BRIDGE_AMP_LOW,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calibratedData[iBALow]);	
				}
			}
			if ((mEnabledSensors & SENSOR_HEART) > 0) {
				int iHeartRate = getSignalIndex(Shimmer2.ObjectClusterSensorName.HEART_RATE);
				tempData[0] = (double)newPacketInt[iHeartRate];
				objectCluster.addData(Shimmer2.ObjectClusterSensorName.HEART_RATE,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,tempData[0]);
				if (mEnableCalibration){
					calibratedData[iHeartRate]=tempData[0];
					if (getFirmwareVersionMajor()==0 && getFirmwareVersionMinor()==1){

					} else {
						if (tempData[0]==0){
							calibratedData[iHeartRate]=	mLastKnownHeartRate;
						} else {
							calibratedData[iHeartRate]=(int)(1024/tempData[0]*60);
							mLastKnownHeartRate=calibratedData[iHeartRate];
						}
					}
					objectCluster.addData(Shimmer2.ObjectClusterSensorName.HEART_RATE,CHANNEL_TYPE.CAL.toString(),"BPM",calibratedData[iHeartRate]);	
				}
			}
			if ((mEnabledSensors& SENSOR_EXP_BOARD_A0) > 0) {
				int iA0 = getSignalIndex(Shimmer2.ObjectClusterSensorName.EXP_BOARD_A0);
				tempData[0] = (double)newPacketInt[iA0];
				if (getPMux()==0){
					objectCluster.addData(Shimmer2.ObjectClusterSensorName.EXP_BOARD_A0,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iA0]);
					if (mEnableCalibration){
						calibratedData[iA0]=calibrateU12AdcValue(tempData[0],0,3,1);
						objectCluster.addData(Shimmer2.ObjectClusterSensorName.EXP_BOARD_A0,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calibratedData[iA0]);
					}
				} else {
					objectCluster.addData(Shimmer2.ObjectClusterSensorName.VOLT_REG,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iA0]);
					if (mEnableCalibration){
						calibratedData[iA0]=calibrateU12AdcValue(tempData[0],0,3,1)*1.988;
						objectCluster.addData(Shimmer2.ObjectClusterSensorName.VOLT_REG,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calibratedData[iA0]);
					}

				}
			}					
			if ((mEnabledSensors & SENSOR_EXP_BOARD_A7) > 0) {
				int iA7 = getSignalIndex(Shimmer2.ObjectClusterSensorName.EXP_BOARD_A7);
				tempData[0] = (double)newPacketInt[iA7];
				if (getPMux()==0){
					objectCluster.addData(Shimmer2.ObjectClusterSensorName.EXP_BOARD_A7,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iA7]);
					if (mEnableCalibration){
						calibratedData[iA7]=calibrateU12AdcValue(tempData[0],0,3,1);
						objectCluster.addData(Shimmer2.ObjectClusterSensorName.EXP_BOARD_A7,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calibratedData[iA7]);
					} else {
						objectCluster.addData(Shimmer2.ObjectClusterSensorName.BATTERY,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iA7]);
						if (mEnableCalibration){
							calibratedData[iA7]=calibrateU12AdcValue(tempData[0],0,3,1)*2;
							objectCluster.addData(Shimmer2.ObjectClusterSensorName.BATTERY,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calibratedData[iA7]);
						}

					}
				} 
			}
			if  ((mEnabledSensors & SENSOR_BATT) > 0) {
				int iA0 = getSignalIndex(Shimmer2.ObjectClusterSensorName.EXP_BOARD_A0);
				objectCluster.addData(Shimmer2.ObjectClusterSensorName.VOLT_REG,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iA0]);

				int iA7 = getSignalIndex(Shimmer2.ObjectClusterSensorName.EXP_BOARD_A7);
				objectCluster.addData(Shimmer2.ObjectClusterSensorName.BATTERY,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iA7]);


				if (mEnableCalibration){
					tempData[0] = (double)newPacketInt[iA0];
					calibratedData[iA0]=calibrateU12AdcValue(tempData[0],0,3,1)*1.988;
					objectCluster.addData(Shimmer2.ObjectClusterSensorName.VOLT_REG,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calibratedData[iA0]);

					tempData[0] = (double)newPacketInt[iA7];
					calibratedData[iA7]=calibrateU12AdcValue(tempData[0],0,3,1)*2;
					objectCluster.addData(Shimmer2.ObjectClusterSensorName.BATTERY,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calibratedData[iA7]);	
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
	
	
	@Deprecated
	protected Object buildMsg(byte[] newPacket, Object object) {
		ObjectCluster objectCluster = (ObjectCluster) object;
		objectCluster.mRawData = newPacket;
		objectCluster.mSystemTimeStamp=ByteBuffer.allocate(8).putLong(System.currentTimeMillis()).array();
		double [] calibratedData=new double[mNChannels + 1]; //plus 1 because of the time stamp
		long[] newPacketInt = UtilParseData.parseData(newPacket,mSignalDataTypeArray);
		double[] tempData=new double[3];
		Vector3d accelerometer = new Vector3d();
		Vector3d magnetometer = new Vector3d();
		Vector3d gyroscope = new Vector3d();

		int iTimeStamp=getSignalIndex(Configuration.Shimmer2.ObjectClusterSensorName.TIMESTAMP); //find index
		tempData[0]=(double)newPacketInt[1];
		objectCluster.addData("Timestamp",CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iTimeStamp]);
		if (mEnableCalibration){
			objectCluster.addData("Timestamp",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLISECONDS,calibrateTimeStamp((double)newPacketInt[iTimeStamp]));
		}
		objectCluster = callAdditionalServices(objectCluster);


		if (getHardwareVersion()==HW_ID.SHIMMER_SR30 || getHardwareVersion()==HW_ID.SHIMMER_3){
			if (((mEnabledSensors & SENSOR_ACCEL) > 0)){
				int iAccelX=getSignalIndex("Low Noise Accelerometer X"); //find index
				int iAccelY=getSignalIndex("Low Noise Accelerometer Y"); //find index
				int iAccelZ=getSignalIndex("Low Noise Accelerometer Z"); //find index
				tempData[0]=(double)newPacketInt[iAccelX];
				tempData[1]=(double)newPacketInt[iAccelY];
				tempData[2]=(double)newPacketInt[iAccelZ];

				objectCluster.addData("Low Noise Accelerometer X",CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iAccelX]);
				objectCluster.addData("Low Noise Accelerometer Y",CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iAccelY]);
				objectCluster.addData("Low Noise Accelerometer Z",CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iAccelZ]);

				if (mEnableCalibration){
					double[] accelCalibratedData;
					accelCalibratedData=UtilCalibration.calibrateInertialSensorData(tempData, mAlignmentMatrixAnalogAccel, mSensitivityMatrixAnalogAccel, mOffsetVectorAnalogAccel);
					calibratedData[iAccelX]=accelCalibratedData[0];
					calibratedData[iAccelY]=accelCalibratedData[1];
					calibratedData[iAccelZ]=accelCalibratedData[2];
					if (mDefaultCalibrationParametersAccel == true) {
						objectCluster.addData("Low Noise Accelerometer X",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.METER_PER_SECOND_SQUARE+"*",accelCalibratedData[0]);
						objectCluster.addData("Low Noise Accelerometer Y",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.METER_PER_SECOND_SQUARE+"*",accelCalibratedData[1]);
						objectCluster.addData("Low Noise Accelerometer Z",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.METER_PER_SECOND_SQUARE+"*",accelCalibratedData[2]);
						accelerometer.x=accelCalibratedData[0];
						accelerometer.y=accelCalibratedData[1];
						accelerometer.z=accelCalibratedData[2];

					} else {
						objectCluster.addData("Low Noise Accelerometer X",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.METER_PER_SECOND_SQUARE,accelCalibratedData[0]);
						objectCluster.addData("Low Noise Accelerometer Y",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.METER_PER_SECOND_SQUARE,accelCalibratedData[1]);
						objectCluster.addData("Low Noise Accelerometer Z",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.METER_PER_SECOND_SQUARE,accelCalibratedData[2]);
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
				objectCluster.addData("Wide Range Accelerometer X",CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iAccelX]);
				objectCluster.addData("Wide Range Accelerometer Y",CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iAccelY]);
				objectCluster.addData("Wide Range Accelerometer Z",CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iAccelZ]);


				if (mEnableCalibration){
					double[] accelCalibratedData;
					accelCalibratedData=UtilCalibration.calibrateInertialSensorData(tempData, mAlignmentMatrixWRAccel, mSensitivityMatrixWRAccel, mOffsetVectorWRAccel);
					calibratedData[iAccelX]=accelCalibratedData[0];
					calibratedData[iAccelY]=accelCalibratedData[1];
					calibratedData[iAccelZ]=accelCalibratedData[2];
					if (mDefaultCalibrationParametersDigitalAccel == true) {
						objectCluster.addData("Wide Range Accelerometer X",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.METER_PER_SECOND_SQUARE+"*",calibratedData[iAccelX]);
						objectCluster.addData("Wide Range Accelerometer Y",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.METER_PER_SECOND_SQUARE+"*",calibratedData[iAccelY]);
						objectCluster.addData("Wide Range Accelerometer Z",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.METER_PER_SECOND_SQUARE+"*",calibratedData[iAccelZ]);
						if (((mEnabledSensors & SENSOR_ACCEL) == 0)){
							accelerometer.x=accelCalibratedData[0]; //this is used to calculate quaternions // skip if Low noise is enabled
							accelerometer.y=accelCalibratedData[1];
							accelerometer.z=accelCalibratedData[2];
						}
					} else {
						objectCluster.addData("Wide Range Accelerometer X",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.METER_PER_SECOND_SQUARE,calibratedData[iAccelX]);
						objectCluster.addData("Wide Range Accelerometer Y",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.METER_PER_SECOND_SQUARE,calibratedData[iAccelY]);
						objectCluster.addData("Wide Range Accelerometer Z",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.METER_PER_SECOND_SQUARE,calibratedData[iAccelZ]);
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


				objectCluster.addData("Gyroscope X",CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iGyroX]);
				objectCluster.addData("Gyroscope Y",CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iGyroY]);
				objectCluster.addData("Gyroscope Z",CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iGyroZ]);

				if (mEnableCalibration){
					double[] gyroCalibratedData=UtilCalibration.calibrateInertialSensorData(tempData, mAlignmentMatrixGyroscope, mSensitivityMatrixGyroscope, mOffsetVectorGyroscope);
					calibratedData[iGyroX]=gyroCalibratedData[0];
					calibratedData[iGyroY]=gyroCalibratedData[1];
					calibratedData[iGyroZ]=gyroCalibratedData[2];
					if (mDefaultCalibrationParametersGyro == true) {
						objectCluster.addData("Gyroscope X",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.DEGREES_PER_SECOND+"*",gyroCalibratedData[0]);
						objectCluster.addData("Gyroscope Y",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.DEGREES_PER_SECOND+"*",gyroCalibratedData[1]);
						objectCluster.addData("Gyroscope Z",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.DEGREES_PER_SECOND+"*",gyroCalibratedData[2]);
						gyroscope.x=gyroCalibratedData[0]*Math.PI/180;
						gyroscope.y=gyroCalibratedData[1]*Math.PI/180;
						gyroscope.z=gyroCalibratedData[2]*Math.PI/180;
					} else {
						objectCluster.addData("Gyroscope X",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.DEGREES_PER_SECOND,gyroCalibratedData[0]);
						objectCluster.addData("Gyroscope Y",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.DEGREES_PER_SECOND,gyroCalibratedData[1]);
						objectCluster.addData("Gyroscope Z",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.DEGREES_PER_SECOND,gyroCalibratedData[2]);
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
				objectCluster.addData("Magnetometer X",CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iMagX]);
				objectCluster.addData("Magnetometer Y",CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iMagY]);
				objectCluster.addData("Magnetometer Z",CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iMagZ]);
				if (mEnableCalibration){
					double[] magCalibratedData=UtilCalibration.calibrateInertialSensorData(tempData, mAlignmentMatrixMagnetometer, mSensitivityMatrixMagnetometer, mOffsetVectorMagnetometer);
					calibratedData[iMagX]=magCalibratedData[0];
					calibratedData[iMagY]=magCalibratedData[1];
					calibratedData[iMagZ]=magCalibratedData[2];
					if (mDefaultCalibrationParametersMag == true) {
						objectCluster.addData("Magnetometer X",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL_FLUX+"*",magCalibratedData[0]);
						objectCluster.addData("Magnetometer Y",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL_FLUX+"*",magCalibratedData[1]);
						objectCluster.addData("Magnetometer Z",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL_FLUX+"*",magCalibratedData[2]);
						magnetometer.x=magCalibratedData[0];
						magnetometer.y=magCalibratedData[1];
						magnetometer.z=magCalibratedData[2];
					} else {
						objectCluster.addData("Magnetometer X",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL_FLUX,magCalibratedData[0]);
						objectCluster.addData("Magnetometer Y",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL_FLUX,magCalibratedData[1]);
						objectCluster.addData("Magnetometer Z",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL_FLUX,magCalibratedData[2]);
						magnetometer.x=magCalibratedData[0];
						magnetometer.y=magCalibratedData[1];
						magnetometer.z=magCalibratedData[2];
					}
				}
			}

			if ((mEnabledSensors & SENSOR_BATT) > 0) {
				int iA0 = getSignalIndex("VSenseBatt");
				tempData[0] = (double)newPacketInt[iA0];
				objectCluster.addData("VSenseBatt",CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iA0]);
				if (mEnableCalibration){
					calibratedData[iA0]=calibrateU12AdcValue(tempData[0],0,3,1)*1.988;
					objectCluster.addData("VSenseBatt",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calibratedData[iA0]);
					mVSenseBattMA.addValue(calibratedData[iA0]);
					checkBattery();
				}
			}
			if ((mEnabledSensors & SENSOR_EXT_ADC_A7) > 0) {
				int iA0 = getSignalIndex("External ADC A7");
				tempData[0] = (double)newPacketInt[iA0];
				objectCluster.addData("External ADC A7",CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iA0]);
				if (mEnableCalibration){
					calibratedData[iA0]=calibrateU12AdcValue(tempData[0],0,3,1);
					objectCluster.addData("External ADC A7",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calibratedData[iA0]);
				}
			}
			if ((mEnabledSensors & SENSOR_EXT_ADC_A6) > 0) {
				int iA0 = getSignalIndex("External ADC A6");
				tempData[0] = (double)newPacketInt[iA0];
				objectCluster.addData("External ADC A6",CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iA0]);
				if (mEnableCalibration){
					calibratedData[iA0]=calibrateU12AdcValue(tempData[0],0,3,1);
					objectCluster.addData("External ADC A6",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calibratedData[iA0]);
				}
			}
			if ((mEnabledSensors & SENSOR_EXT_ADC_A15) > 0) {
				int iA0 = getSignalIndex("External ADC A15");
				tempData[0] = (double)newPacketInt[iA0];
				objectCluster.addData("External ADC A15",CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iA0]);
				if (mEnableCalibration){
					calibratedData[iA0]=calibrateU12AdcValue(tempData[0],0,3,1);
					objectCluster.addData("External ADC A15",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calibratedData[iA0]);
				}
			}
			if ((mEnabledSensors & SENSOR_INT_ADC_A1) > 0) {
				int iA0 = getSignalIndex("Internal ADC A1");
				tempData[0] = (double)newPacketInt[iA0];
				objectCluster.addData("Internal ADC A1",CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iA0]);
				if (mEnableCalibration){
					calibratedData[iA0]=calibrateU12AdcValue(tempData[0],0,3,1);
					objectCluster.addData("Internal ADC A1",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calibratedData[iA0]);
				}
			}
			if ((mEnabledSensors & SENSOR_INT_ADC_A12) > 0) {
				int iA0 = getSignalIndex("Internal ADC A12");
				tempData[0] = (double)newPacketInt[iA0];
				objectCluster.addData("Internal ADC A12",CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iA0]);
				if (mEnableCalibration){
					calibratedData[iA0]=calibrateU12AdcValue(tempData[0],0,3,1);
					objectCluster.addData("Internal ADC A12",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calibratedData[iA0]);
				}
			}
			if ((mEnabledSensors & SENSOR_INT_ADC_A13) > 0) {
				int iA0 = getSignalIndex("Internal ADC A13");
				tempData[0] = (double)newPacketInt[iA0];
				objectCluster.addData("Internal ADC A13",CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iA0]);
				if (mEnableCalibration){
					calibratedData[iA0]=calibrateU12AdcValue(tempData[0],0,3,1);
					objectCluster.addData("Internal ADC A13",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calibratedData[iA0]);
				}
			}
			if ((mEnabledSensors & SENSOR_INT_ADC_A14) > 0) {
				int iA0 = getSignalIndex("Internal ADC A14");
				tempData[0] = (double)newPacketInt[iA0];
				objectCluster.addData("Internal ADC A14",CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iA0]);
				if (mEnableCalibration){
					calibratedData[iA0]=calibrateU12AdcValue(tempData[0],0,3,1);
					objectCluster.addData("Internal ADC A14",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calibratedData[iA0]);
				}
			}
			if (((mEnabledSensors & SENSOR_ACCEL) > 0 || (mEnabledSensors & SENSOR_DACCEL) > 0) && ((mEnabledSensors & 0xFF)& SENSOR_GYRO) > 0 && ((mEnabledSensors & 0xFF)& SENSOR_MAG) > 0 && mOrientationEnabled ){
				if (mEnableCalibration){
					if (mOrientationAlgo==null){
						mOrientationAlgo = new GradDes3DOrientation(0.4, (double)1/getSamplingRateShimmer(), 1, 0, 0,0);
					}
					Orientation3DObject q = mOrientationAlgo.update(accelerometer.x,accelerometer.y,accelerometer.z, gyroscope.x,gyroscope.y,gyroscope.z, magnetometer.x,magnetometer.y,magnetometer.z);					double theta, Rx, Ry, Rz, rho;
					objectCluster.addData("Axis Angle A",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL,q.getTheta());
					objectCluster.addData("Axis Angle X",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL,q.getAngleX());
					objectCluster.addData("Axis Angle Y",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL,q.getAngleY());
					objectCluster.addData("Axis Angle Z",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL,q.getAngleZ());
					objectCluster.addData("Quaternion 0",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL,q.getQuaternionW());
					objectCluster.addData("Quaternion 1",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL,q.getQuaternionX());
					objectCluster.addData("Quaternion 2",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL,q.getQuaternionY());
					objectCluster.addData("Quaternion 3",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL,q.getQuaternionZ());
				}
			}

			if ((mEnabledSensors & SENSOR_EXG1_24BIT) >0){
				int iexg1ch1 = getSignalIndex("EXG1 24Bit CH1");
				int iexg1ch2 = getSignalIndex("EXG1 24Bit CH2");
				int iexg1sta = getSignalIndex("EXG1 STATUS");
				double exg1ch1 = (double)newPacketInt[iexg1ch1];
				double exg1ch2 = (double)newPacketInt[iexg1ch2];
				double exg1sta = (double)newPacketInt[iexg1sta];
				objectCluster.addData("EXG1 STATUS",CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg1sta);
				if (mEnableCalibration){
					double calexg1ch1 = exg1ch1 *(((2.42*1000)/mEXG1CH1GainValue)/(Math.pow(2,23)-1));
					double calexg1ch2 = exg1ch2 *(((2.42*1000)/mEXG1CH2GainValue)/(Math.pow(2,23)-1));
					if (isEXGUsingDefaultECGConfiguration()){
						objectCluster.addData("ECG LL-RA",CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg1ch1);
						objectCluster.addData("ECG LA-RA",CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg1ch2);
						objectCluster.addData("ECG LL-RA",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calexg1ch1);
						objectCluster.addData("ECG LA-RA",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calexg1ch2);
					} else if (isEXGUsingDefaultEMGConfiguration()){
						objectCluster.addData("EMG CH1",CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg1ch1);
						objectCluster.addData("EMG CH2",CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg1ch2);
						objectCluster.addData("EMG CH1",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calexg1ch1);
						objectCluster.addData("EMG CH2",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calexg1ch2);
					} else {
						objectCluster.addData("EXG1 CH1",CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg1ch1);
						objectCluster.addData("EXG1 CH2",CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg1ch2);
						objectCluster.addData("EXG1 CH1",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calexg1ch1);
						objectCluster.addData("EXG1 CH2",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calexg1ch2);
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

				objectCluster.addData("EXG2 STATUS",CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg2sta);
				if (mEnableCalibration){
					double calexg2ch1 = exg2ch1 *(((2.42*1000)/mEXG2CH1GainValue)/(Math.pow(2,23)-1));
					double calexg2ch2 = exg2ch2 *(((2.42*1000)/mEXG2CH2GainValue)/(Math.pow(2,23)-1));
					if (isEXGUsingDefaultECGConfiguration()){
						objectCluster.addData("EXG2 CH1",CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg2ch1);
						objectCluster.addData("ECG Vx-RL",CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg2ch2);
						objectCluster.addData("EXG2 CH1",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calexg2ch1);
						objectCluster.addData("ECG Vx-RL",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calexg2ch2);
					}
					else if (isEXGUsingDefaultEMGConfiguration()){
						objectCluster.addData("EXG2 CH1",CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,0);
						objectCluster.addData("EXG2 CH2",CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,0);
						objectCluster.addData("EXG2 CH1",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,0);
						objectCluster.addData("EXG2 CH2",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,0);
					} else {
						objectCluster.addData("EXG2 CH1",CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg2ch1);
						objectCluster.addData("EXG2 CH2",CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg2ch2);
						objectCluster.addData("EXG2 CH1",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calexg2ch1);
						objectCluster.addData("EXG2 CH2",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calexg2ch2);	
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
				objectCluster.addData("EXG1 STATUS",CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg1sta);
				if (mEnableCalibration){
					double calexg1ch1 = exg1ch1 *(((2.42*1000)/(mEXG1CH1GainValue*2))/(Math.pow(2,15)-1));
					double calexg1ch2 = exg1ch2 *(((2.42*1000)/(mEXG1CH2GainValue*2))/(Math.pow(2,15)-1));
					if (isEXGUsingDefaultECGConfiguration()){
						objectCluster.addData("ECG LL-RA",CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg1ch1);
						objectCluster.addData("ECG LA-RA",CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg1ch2);
						objectCluster.addData("ECG LL-RA",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calexg1ch1);
						objectCluster.addData("ECG LA-RA",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calexg1ch2);
					} else if (isEXGUsingDefaultEMGConfiguration()){
						objectCluster.addData("EMG CH1",CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg1ch1);
						objectCluster.addData("EMG CH2",CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg1ch2);
						objectCluster.addData("EMG CH1",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calexg1ch1);
						objectCluster.addData("EMG CH2",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calexg1ch2);
					} else {
						objectCluster.addData("EXG1 CH1 16Bit",CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg1ch1);
						objectCluster.addData("EXG1 CH2 16Bit",CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg1ch2);
						objectCluster.addData("EXG1 CH1 16Bit",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calexg1ch1);
						objectCluster.addData("EXG1 CH2 16Bit",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calexg1ch2);
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

				objectCluster.addData("EXG2 STATUS",CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg2sta);
				if (mEnableCalibration){
					double calexg2ch1 = ((exg2ch1)) *(((2.42*1000)/(mEXG2CH1GainValue*2))/(Math.pow(2,15)-1));
					double calexg2ch2 = ((exg2ch2)) *(((2.42*1000)/(mEXG2CH2GainValue*2))/(Math.pow(2,15)-1));
					if (isEXGUsingDefaultECGConfiguration()){
						objectCluster.addData("EXG2 CH1",CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg2ch1);
						objectCluster.addData("ECG Vx-RL",CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg2ch2);
						objectCluster.addData("EXG2 CH1",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calexg2ch1);
						objectCluster.addData("ECG Vx-RL",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calexg2ch2);
					}
					else if (isEXGUsingDefaultEMGConfiguration()){
						objectCluster.addData("EXG2 CH1",CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,0);
						objectCluster.addData("EXG2 CH2",CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,0);
						objectCluster.addData("EXG2 CH1",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,0);
						objectCluster.addData("EXG2 CH2",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,0);
					} else {
						objectCluster.addData("EXG2 CH1 16Bit",CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg2ch1);
						objectCluster.addData("EXG2 CH2 16Bit",CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,exg2ch2);
						objectCluster.addData("EXG2 CH1 16Bit",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calexg2ch1);
						objectCluster.addData("EXG2 CH2 16Bit",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calexg2ch2);
					}
				}
			}

			if ((mEnabledSensors & SENSOR_BMP180) >0){
				int iUT = getSignalIndex("Temperature");
				int iUP = getSignalIndex("Pressure");
				double UT = (double)newPacketInt[iUT];
				double UP = (double)newPacketInt[iUP];
				UP=UP/Math.pow(2,8-mPressureResolution);
				objectCluster.addData("Pressure",CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,UP);
				objectCluster.addData("Temperature",CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,UT);
				if (mEnableCalibration){
					double[] bmp180caldata= calibratePressureSensorData(UP,UT);
					objectCluster.addData("Pressure",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.KPASCAL,bmp180caldata[0]/1000);
					objectCluster.addData("Temperature",CHANNEL_TYPE.CAL.toString(),"Celsius",bmp180caldata[1]);
				}
			}

			if ((mEnabledSensors & SENSOR_BRIDGE_AMP) > 0) {
				int iBAHigh = getSignalIndex("Bridge Amplifier High");
				int iBALow = getSignalIndex("Bridge Amplifier Low");
				objectCluster.addData("Bridge Amplifier High",CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iBAHigh]);
				objectCluster.addData("Bridge Amplifier Low",CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iBALow]);
				if (mEnableCalibration){
					tempData[0] = (double)newPacketInt[iBAHigh];
					tempData[1] = (double)newPacketInt[iBALow];
					calibratedData[iBAHigh]=calibrateU12AdcValue(tempData[0],60,3,551);
					calibratedData[iBALow]=calibrateU12AdcValue(tempData[1],1950,3,183.7);
					objectCluster.addData("Bridge Amplifier High",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calibratedData[iBAHigh]);
					objectCluster.addData("Bridge Amplifier Low",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calibratedData[iBALow]);	
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
					if (getHardwareVersion()!=HW_ID.SHIMMER_3){
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
					if (getHardwareVersion()!=HW_ID.SHIMMER_3){
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
					if (getHardwareVersion()!=HW_ID.SHIMMER_3){
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
					if (getHardwareVersion()!=HW_ID.SHIMMER_3){
						p1 = 4.5580e-04;
						p2 = -0.3014;
					} else {
						p1 = 4.4513e-04;
						p2 = -0.3193;
					}
				}
				objectCluster.addData("GSR",CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iGSR]);
				if (mEnableCalibration){
					calibratedData[iGSR] = SensorGSR.calibrateGsrData(tempData[0],p1,p2);
					objectCluster.addData("GSR",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.KOHMS,calibratedData[iGSR]);
				}
			}
		} else { //start of Shimmer2

			if ((mEnabledSensors & SENSOR_ACCEL) > 0){
				int iAccelX=getSignalIndex("Accelerometer X"); //find index
				int iAccelY=getSignalIndex("Accelerometer Y"); //find index
				int iAccelZ=getSignalIndex("Accelerometer Z"); //find index
				objectCluster.addData("Accelerometer X",CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iAccelX]);
				objectCluster.addData("Accelerometer Y",CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iAccelY]);
				objectCluster.addData("Accelerometer Z",CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iAccelZ]);
				if (mEnableCalibration){
					tempData[0]=(double)newPacketInt[iAccelX];
					tempData[1]=(double)newPacketInt[iAccelY];
					tempData[2]=(double)newPacketInt[iAccelZ];
					double[] accelCalibratedData=UtilCalibration.calibrateInertialSensorData(tempData, mAlignmentMatrixAnalogAccel, mSensitivityMatrixAnalogAccel, mOffsetVectorAnalogAccel);
					calibratedData[iAccelX]=accelCalibratedData[0];
					calibratedData[iAccelY]=accelCalibratedData[1];
					calibratedData[iAccelZ]=accelCalibratedData[2];
					if (mDefaultCalibrationParametersAccel == true) {
						objectCluster.addData("Accelerometer X",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.METER_PER_SECOND_SQUARE+"*",accelCalibratedData[0]);
						objectCluster.addData("Accelerometer Y",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.METER_PER_SECOND_SQUARE+"*",accelCalibratedData[1]);
						objectCluster.addData("Accelerometer Z",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.METER_PER_SECOND_SQUARE+"*",accelCalibratedData[2]);
						accelerometer.x=accelCalibratedData[0];
						accelerometer.y=accelCalibratedData[1];
						accelerometer.z=accelCalibratedData[2];
					} else {
						objectCluster.addData("Accelerometer X",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.METER_PER_SECOND_SQUARE,accelCalibratedData[0]);
						objectCluster.addData("Accelerometer Y",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.METER_PER_SECOND_SQUARE,accelCalibratedData[1]);
						objectCluster.addData("Accelerometer Z",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.METER_PER_SECOND_SQUARE,accelCalibratedData[2]);
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
				objectCluster.addData("Gyroscope X",CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iGyroX]);
				objectCluster.addData("Gyroscope Y",CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iGyroY]);
				objectCluster.addData("Gyroscope Z",CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iGyroZ]);
				if (mEnableCalibration){
					tempData[0]=(double)newPacketInt[iGyroX];
					tempData[1]=(double)newPacketInt[iGyroY];
					tempData[2]=(double)newPacketInt[iGyroZ];
					double[] gyroCalibratedData=UtilCalibration.calibrateInertialSensorData(tempData, mAlignmentMatrixGyroscope, mSensitivityMatrixGyroscope, mOffsetVectorGyroscope);
					calibratedData[iGyroX]=gyroCalibratedData[0];
					calibratedData[iGyroY]=gyroCalibratedData[1];
					calibratedData[iGyroZ]=gyroCalibratedData[2];
					if (mDefaultCalibrationParametersGyro == true) {
						objectCluster.addData("Gyroscope X",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.DEGREES_PER_SECOND+"*",gyroCalibratedData[0]);
						objectCluster.addData("Gyroscope Y",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.DEGREES_PER_SECOND+"*",gyroCalibratedData[1]);
						objectCluster.addData("Gyroscope Z",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.DEGREES_PER_SECOND+"*",gyroCalibratedData[2]);
						gyroscope.x=gyroCalibratedData[0]*Math.PI/180;
						gyroscope.y=gyroCalibratedData[1]*Math.PI/180;
						gyroscope.z=gyroCalibratedData[2]*Math.PI/180;
					} else {
						objectCluster.addData("Gyroscope X",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.DEGREES_PER_SECOND,gyroCalibratedData[0]);
						objectCluster.addData("Gyroscope Y",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.DEGREES_PER_SECOND,gyroCalibratedData[1]);
						objectCluster.addData("Gyroscope Z",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.DEGREES_PER_SECOND,gyroCalibratedData[2]);
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
				objectCluster.addData("Magnetometer X",CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iMagX]);
				objectCluster.addData("Magnetometer Y",CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iMagY]);
				objectCluster.addData("Magnetometer Z",CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iMagZ]);
				if (mEnableCalibration){
					tempData[0]=(double)newPacketInt[iMagX];
					tempData[1]=(double)newPacketInt[iMagY];
					tempData[2]=(double)newPacketInt[iMagZ];
					double[] magCalibratedData=UtilCalibration.calibrateInertialSensorData(tempData, mAlignmentMatrixMagnetometer, mSensitivityMatrixMagnetometer, mOffsetVectorMagnetometer);
					calibratedData[iMagX]=magCalibratedData[0];
					calibratedData[iMagY]=magCalibratedData[1];
					calibratedData[iMagZ]=magCalibratedData[2];
					if (mDefaultCalibrationParametersMag == true) {
						objectCluster.addData("Magnetometer X",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL_FLUX+"*",magCalibratedData[0]);
						objectCluster.addData("Magnetometer Y",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL_FLUX+"*",magCalibratedData[1]);
						objectCluster.addData("Magnetometer Z",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL_FLUX+"*",magCalibratedData[2]);
						magnetometer.x=magCalibratedData[0];
						magnetometer.y=magCalibratedData[1];
						magnetometer.z=magCalibratedData[2];
					} else {
						objectCluster.addData("Magnetometer X",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL_FLUX,magCalibratedData[0]);
						objectCluster.addData("Magnetometer Y",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL_FLUX,magCalibratedData[1]);
						objectCluster.addData("Magnetometer Z",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL_FLUX,magCalibratedData[2]);
						magnetometer.x=magCalibratedData[0];
						magnetometer.y=magCalibratedData[1];
						magnetometer.z=magCalibratedData[2];
					}
				}
			}


			if ((mEnabledSensors & SENSOR_ACCEL) > 0 && (mEnabledSensors & SENSOR_GYRO) > 0 && (mEnabledSensors & SENSOR_MAG) > 0 && mOrientationEnabled ){
				if (mEnableCalibration){
					if (mOrientationAlgo==null){
						mOrientationAlgo = new GradDes3DOrientation(0.4, (double)1/getSamplingRateShimmer(), 1, 0, 0,0);
					}
					Orientation3DObject q = mOrientationAlgo.update(accelerometer.x,accelerometer.y,accelerometer.z, gyroscope.x,gyroscope.y,gyroscope.z, magnetometer.x,magnetometer.y,magnetometer.z);
					objectCluster.addData("Axis Angle A",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL,q.getTheta());
					objectCluster.addData("Axis Angle X",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL,q.getAngleX());
					objectCluster.addData("Axis Angle Y",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL,q.getAngleY());
					objectCluster.addData("Axis Angle Z",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL,q.getAngleZ());
					objectCluster.addData("Quaternion 0",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL,q.getQuaternionW());
					objectCluster.addData("Quaternion 1",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL,q.getQuaternionX());
					objectCluster.addData("Quaternion 2",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL,q.getQuaternionY());
					objectCluster.addData("Quaternion 3",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.LOCAL,q.getQuaternionZ());
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
				objectCluster.addData("GSR",CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iGSR]);
				if (mEnableCalibration){
					tempData[0] = (double)newPacketInt[iGSR];
					calibratedData[iGSR] = SensorGSR.calibrateGsrData(tempData[0],p1,p2);
					objectCluster.addData("GSR",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.KOHMS,calibratedData[iGSR]);
				}
			}
			if ((mEnabledSensors & SENSOR_ECG) > 0) {
				int iECGRALL = getSignalIndex("ECG RA LL");
				int iECGLALL = getSignalIndex("ECG LA LL");
				objectCluster.addData("ECG RA-LL",CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iECGRALL]);
				objectCluster.addData("ECG LA-LL",CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iECGLALL]);
				if (mEnableCalibration){
					tempData[0] = (double)newPacketInt[iECGRALL];
					tempData[1] = (double)newPacketInt[iECGLALL];
					calibratedData[iECGRALL]=calibrateU12AdcValue(tempData[0],OffsetECGRALL,3,GainECGRALL);
					calibratedData[iECGLALL]=calibrateU12AdcValue(tempData[1],OffsetECGLALL,3,GainECGLALL);
					if (mDefaultCalibrationParametersECG == true) {
						objectCluster.addData("ECG RA-LL",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS+"*",calibratedData[iECGRALL]);
						objectCluster.addData("ECG LA-LL",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS+"*",calibratedData[iECGLALL]);
					} else {
						objectCluster.addData("ECG RA-LL",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calibratedData[iECGRALL]);
						objectCluster.addData("ECG LA-LL",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calibratedData[iECGLALL]);
					}
				}
			}
			if ((mEnabledSensors & SENSOR_EMG) > 0) {
				int iEMG = getSignalIndex("EMG");
				objectCluster.addData("EMG",CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iEMG]);
				if (mEnableCalibration){
					tempData[0] = (double)newPacketInt[iEMG];
					calibratedData[iEMG]=calibrateU12AdcValue(tempData[0],OffsetEMG,3,GainEMG);
					if (mDefaultCalibrationParametersEMG == true){
						objectCluster.addData("EMG",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS+"*",calibratedData[iEMG]);
					} else {
						objectCluster.addData("EMG",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calibratedData[iEMG]);
					}
				}
			}
			if ((mEnabledSensors & SENSOR_BRIDGE_AMP) > 0) {
				int iBAHigh = getSignalIndex("Bridge Amplifier High");
				int iBALow = getSignalIndex("Bridge Amplifier Low");
				objectCluster.addData("Bridge Amplifier High",CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iBAHigh]);
				objectCluster.addData("Bridge Amplifier Low",CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iBALow]);
				if (mEnableCalibration){
					tempData[0] = (double)newPacketInt[iBAHigh];
					tempData[1] = (double)newPacketInt[iBALow];
					calibratedData[iBAHigh]=calibrateU12AdcValue(tempData[0],60,3,551);
					calibratedData[iBALow]=calibrateU12AdcValue(tempData[1],1950,3,183.7);
					objectCluster.addData("Bridge Amplifier High",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calibratedData[iBAHigh]);
					objectCluster.addData("Bridge Amplifier Low",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calibratedData[iBALow]);	
				}
			}
			if ((mEnabledSensors & SENSOR_HEART) > 0) {
				int iHeartRate = getSignalIndex("Heart Rate");
				tempData[0] = (double)newPacketInt[iHeartRate];
				objectCluster.addData("Heart Rate",CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,tempData[0]);
				if (mEnableCalibration){
					calibratedData[iHeartRate]=tempData[0];
					if (getFirmwareVersionMajor()==0 && getFirmwareVersionMinor()==1){

					} else {
						if (tempData[0]==0){
							calibratedData[iHeartRate]=	mLastKnownHeartRate;
						} else {
							calibratedData[iHeartRate]=(int)(1024/tempData[0]*60);
							mLastKnownHeartRate=calibratedData[iHeartRate];
						}
					}
					objectCluster.addData("Heart Rate",CHANNEL_TYPE.CAL.toString(),"BPM",calibratedData[iHeartRate]);	
				}
			}
			if ((mEnabledSensors& SENSOR_EXP_BOARD_A0) > 0) {
				int iA0 = getSignalIndex("Exp Board A0");
				tempData[0] = (double)newPacketInt[iA0];
				if (getPMux()==0){
					objectCluster.addData("ExpBoard A0",CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iA0]);
					if (mEnableCalibration){
						calibratedData[iA0]=calibrateU12AdcValue(tempData[0],0,3,1);
						objectCluster.addData("ExpBoard A0",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calibratedData[iA0]);
					}
				} else {
					objectCluster.addData("VSenseReg",CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iA0]);
					if (mEnableCalibration){
						calibratedData[iA0]=calibrateU12AdcValue(tempData[0],0,3,1)*1.988;
						objectCluster.addData("VSenseReg",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calibratedData[iA0]);
					}

				}
			}					
			if ((mEnabledSensors & SENSOR_EXP_BOARD_A7) > 0) {
				int iA7 = getSignalIndex("Exp Board A7");
				tempData[0] = (double)newPacketInt[iA7];
				if (getPMux()==0){
					objectCluster.addData("ExpBoard A7",CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iA7]);
					if (mEnableCalibration){
						calibratedData[iA7]=calibrateU12AdcValue(tempData[0],0,3,1);
						objectCluster.addData("ExpBoard A7",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calibratedData[iA7]);
					}
				} 
			}
			if  ((mEnabledSensors & SENSOR_BATT) > 0) {
				int iA0 = getSignalIndex("Exp Board A0");
				objectCluster.addData("VSenseReg",CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iA0]);

				int iA7 = getSignalIndex("Exp Board A7");
				objectCluster.addData("VSenseBatt",CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iA7]);


				if (mEnableCalibration){
					tempData[0] = (double)newPacketInt[iA0];
					calibratedData[iA0]=calibrateU12AdcValue(tempData[0],0,3,1)*1.988;
					objectCluster.addData("VSenseReg",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calibratedData[iA0]);

					tempData[0] = (double)newPacketInt[iA7];
					calibratedData[iA7]=calibrateU12AdcValue(tempData[0],0,3,1)*2;
					objectCluster.addData("VSenseBatt",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS,calibratedData[iA7]);	
					mVSenseBattMA.addValue(calibratedData[iA7]);
					checkBattery();
				}
			}
		}
		
		return objectCluster;
	}

	protected int getSignalIndex(String signalName) {
		int iSignal=-1; //better to fail //used to be -1, putting to zero ensure it works eventhough it might be wrong SR30
		for (int i=0;i<mSignalNameArray.length;i++) {
			if (signalName==mSignalNameArray[i]) {
//				iSignal=i;
				return i;
			}
		}

		return iSignal;
	}

	/** Only for Bluetooth
	 * @param numChannels
	 * @param signalid
	 */
	protected void interpretDataPacketFormat(int numChannels, byte[] signalid){
		String [] signalNameArray=new String[MAX_NUMBER_OF_SIGNALS];
		String [] signalDataTypeArray=new String[MAX_NUMBER_OF_SIGNALS];
		
		if (getHardwareVersion()==HW_ID.SHIMMER_SR30 || getHardwareVersion()==HW_ID.SHIMMER_3){
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
		for (int i=0;i<numChannels;i++) {
			if ((byte)signalid[i]==(byte)0x00){
				if (getHardwareVersion()==HW_ID.SHIMMER_SR30 || getHardwareVersion()==HW_ID.SHIMMER_3){//XXX-RS-AA-SensorClass?
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
				if (getHardwareVersion()==HW_ID.SHIMMER_SR30 || getHardwareVersion()==HW_ID.SHIMMER_3){//XXX-RS-AA-SensorClass?
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
				if (getHardwareVersion()==HW_ID.SHIMMER_SR30 || getHardwareVersion()==HW_ID.SHIMMER_3){//XXX-RS-AA-SensorClass?
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

				if (getHardwareVersion()==HW_ID.SHIMMER_SR30){
					signalNameArray[i+1]=Shimmer3.ObjectClusterSensorName.GYRO_X;
					signalDataTypeArray[i+1] = "i16";
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|Configuration.Shimmer3.SensorBitmap.SENSOR_GYRO);
				} 
				else if (getHardwareVersion()==HW_ID.SHIMMER_3){
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

				if (getHardwareVersion()==HW_ID.SHIMMER_SR30){
					signalNameArray[i+1]=Shimmer3.ObjectClusterSensorName.GYRO_Y;
					signalDataTypeArray[i+1] = "i16";
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|Configuration.Shimmer3.SensorBitmap.SENSOR_GYRO);
				} 
				else if (getHardwareVersion()==HW_ID.SHIMMER_3){//XXX-RS-LSM-SensorClass?
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
				if (getHardwareVersion()==HW_ID.SHIMMER_SR30){
					signalNameArray[i+1]=Shimmer3.ObjectClusterSensorName.GYRO_Z;
					signalDataTypeArray[i+1] = "i16";
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|Configuration.Shimmer3.SensorBitmap.SENSOR_GYRO);
				}
				else if (getHardwareVersion()==HW_ID.SHIMMER_3){//XXX-RS-LSM-SensorClass?
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
				if(getHardwareVersion()==HW_ID.SHIMMER_SR30){
					signalNameArray[i+1]=Shimmer3.ObjectClusterSensorName.BATTERY; //should be the battery but this will do for now
					signalDataTypeArray[i+1] = "i16";
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|Configuration.Shimmer3.SensorBitmap.SENSOR_VBATT);	
				} 
				else if (getHardwareVersion()==HW_ID.SHIMMER_3){//XXX-RS-LSM-SensorClass?
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
				if(getHardwareVersion()==HW_ID.SHIMMER_SR30){//XXX-RS-LSM-SensorClass?
					signalDataTypeArray[i+1] = "i16";
					packetSize=packetSize+2;
					signalNameArray[i+1]=Shimmer3.ObjectClusterSensorName.ACCEL_WR_X;
					enabledSensors= (enabledSensors|Configuration.Shimmer3.SensorBitmap.SENSOR_D_ACCEL);
				} 
				else if(getHardwareVersion()==HW_ID.SHIMMER_3){//XXX-RS-LSM-SensorClass?
					signalNameArray[i+1]=Shimmer3.ObjectClusterSensorName.MAG_X;
					signalDataTypeArray[i+1] = "i16r";			
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|Configuration.Shimmer3.SensorBitmap.SENSOR_MAG);
				} 
				else {//XXX-RS-LSM-SensorClass?
					signalDataTypeArray[i+1] = "i16";
					packetSize=packetSize+2;
					signalNameArray[i+1]=Shimmer2.ObjectClusterSensorName.MAG_Y;
					enabledSensors= (enabledSensors|SENSOR_MAG);
				}
			}
			else if ((byte)signalid[i]==(byte)0x08)
			{	
				if(getHardwareVersion()==HW_ID.SHIMMER_SR30){//XXX-RS-LSM-SensorClass?
					signalDataTypeArray[i+1] = "i16";
					packetSize=packetSize+2;
					signalNameArray[i+1]=Shimmer3.ObjectClusterSensorName.ACCEL_WR_Y;
					enabledSensors= (enabledSensors|Configuration.Shimmer3.SensorBitmap.SENSOR_D_ACCEL);
				} 
				else if(getHardwareVersion()==HW_ID.SHIMMER_3){//XXX-RS-LSM-SensorClass?
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
				if(getHardwareVersion()==HW_ID.SHIMMER_SR30){//XXX-RS-LSM-SensorClass?
					signalNameArray[i+1]=Shimmer3.ObjectClusterSensorName.ACCEL_WR_Z;
					signalDataTypeArray[i+1] = "i16";
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|Configuration.Shimmer3.SensorBitmap.SENSOR_D_ACCEL);
				} 
				else if(getHardwareVersion()==HW_ID.SHIMMER_3){//XXX-RS-LSM-SensorClass?
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
				if(getHardwareVersion()==HW_ID.SHIMMER_SR30){//XXX-RS-LSM-SensorClass?
					signalNameArray[i+1]=Shimmer3.ObjectClusterSensorName.MAG_X;
					signalDataTypeArray[i+1] = "i16";			
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|Configuration.Shimmer3.SensorBitmap.SENSOR_MAG);
				}
				else if (getHardwareVersion()==HW_ID.SHIMMER_3) {
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
				if(getHardwareVersion()==HW_ID.SHIMMER_SR30){//XXX-RS-LSM-SensorClass?
					signalNameArray[i+1]=Shimmer3.ObjectClusterSensorName.MAG_Y;
					signalDataTypeArray[i+1] = "i16";			
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|Configuration.Shimmer3.SensorBitmap.SENSOR_MAG);
				}
				else if (getHardwareVersion()==HW_ID.SHIMMER_3) {
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
				if(getHardwareVersion()==HW_ID.SHIMMER_SR30){//XXX-RS-LSM-SensorClass?
					signalNameArray[i+1]=Shimmer3.ObjectClusterSensorName.MAG_Z;
					signalDataTypeArray[i+1] = "i16";			
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|Configuration.Shimmer3.SensorBitmap.SENSOR_MAG);
				}
				else if (getHardwareVersion()==HW_ID.SHIMMER_3) {
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
				if (getHardwareVersion()==HW_ID.SHIMMER_3){
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
				if (getHardwareVersion()==HW_ID.SHIMMER_3){
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
				if (getHardwareVersion()==HW_ID.SHIMMER_3){
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
				if (getHardwareVersion()==HW_ID.SHIMMER_3){
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
				if (getHardwareVersion()==HW_ID.SHIMMER_3){
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
				if (getHardwareVersion()==HW_ID.SHIMMER_3){
					signalNameArray[i+1]=Shimmer3.ObjectClusterSensorName.INT_EXP_ADC_A13;
					signalDataTypeArray[i+1] = "u12";
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|SENSOR_INT_ADC_A13);
				} 
				else {
					signalNameArray[i+1]=Shimmer2.ObjectClusterSensorName.HEART_RATE;
					if (getFirmwareVersionMajor()==0 && getFirmwareVersionMinor()==1){
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
				if (getHardwareVersion()==HW_ID.SHIMMER_3){
					signalNameArray[i+1]=Shimmer3.ObjectClusterSensorName.INT_EXP_ADC_A14;
					signalDataTypeArray[i+1] = "u12";
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|SENSOR_INT_ADC_A14);
				}
			}
			else if ((byte)signalid[i]==(byte)0x1A){
				if (getHardwareVersion()==HW_ID.SHIMMER_3){
					signalNameArray[i+1]=Shimmer3.ObjectClusterSensorName.TEMPERATURE_BMP180;
					signalDataTypeArray[i+1] = "u16r";
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|SENSOR_BMP180);
				}
			}
			else if ((byte)signalid[i]==(byte)0x1B){
				if (getHardwareVersion()==HW_ID.SHIMMER_3){
					signalNameArray[i+1]=Shimmer3.ObjectClusterSensorName.PRESSURE_BMP180;
					signalDataTypeArray[i+1] = "u24r";
					packetSize=packetSize+3;
					enabledSensors= (enabledSensors|SENSOR_BMP180);
				}
			}
			else if ((byte)signalid[i]==(byte)0x1C){
				if (getHardwareVersion()==HW_ID.SHIMMER_3){
					signalNameArray[i+1]=Shimmer3.ObjectClusterSensorName.GSR;
					signalDataTypeArray[i+1] = "u16";
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|SENSOR_GSR);
				}
			}
			else if ((byte)signalid[i]==(byte)0x1D){
				if (getHardwareVersion()==HW_ID.SHIMMER_3){
					signalNameArray[i+1]=Shimmer3.ObjectClusterSensorName.EXG1_STATUS;
					signalDataTypeArray[i+1] = "u8";
					packetSize=packetSize+1;
				}
			}
			else if ((byte)signalid[i]==(byte)0x1E){
				if (getHardwareVersion()==HW_ID.SHIMMER_3){
					signalNameArray[i+1]=Shimmer3.ObjectClusterSensorName.EXG1_CH1_24BIT;
					signalDataTypeArray[i+1] = "i24r";
					packetSize=packetSize+3;
					enabledSensors= (enabledSensors|SENSOR_EXG1_24BIT);
				}
			}
			else if ((byte)signalid[i]==(byte)0x1F){
				if (getHardwareVersion()==HW_ID.SHIMMER_3){
					signalNameArray[i+1]=Shimmer3.ObjectClusterSensorName.EXG1_CH2_24BIT;
					signalDataTypeArray[i+1] = "i24r";
					packetSize=packetSize+3;
					enabledSensors= (enabledSensors|SENSOR_EXG1_24BIT);
				}
			}
			else if ((byte)signalid[i]==(byte)0x20){
				if (getHardwareVersion()==HW_ID.SHIMMER_3){
					signalNameArray[i+1]=Shimmer3.ObjectClusterSensorName.EXG2_STATUS;
					signalDataTypeArray[i+1] = "u8";
					packetSize=packetSize+1;
				}
			}
			else if ((byte)signalid[i]==(byte)0x21){
				if (getHardwareVersion()==HW_ID.SHIMMER_3){
					signalNameArray[i+1]=Shimmer3.ObjectClusterSensorName.EXG2_CH1_24BIT;
					signalDataTypeArray[i+1] = "i24r";
					packetSize=packetSize+3;
					enabledSensors= (enabledSensors|SENSOR_EXG2_24BIT);
				}
			}
			else if ((byte)signalid[i]==(byte)0x22){
				if (getHardwareVersion()==HW_ID.SHIMMER_3){
					signalNameArray[i+1]=Shimmer3.ObjectClusterSensorName.EXG2_CH2_24BIT;
					signalDataTypeArray[i+1] = "i24r";
					packetSize=packetSize+3;
					enabledSensors= (enabledSensors|SENSOR_EXG2_24BIT);
				}
			}
			else if ((byte)signalid[i]==(byte)0x23){
				if (getHardwareVersion()==HW_ID.SHIMMER_3){
					signalNameArray[i+1]=Shimmer3.ObjectClusterSensorName.EXG1_CH1_16BIT;
					signalDataTypeArray[i+1] = "i16r";
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|SENSOR_EXG1_16BIT);
				}
			}
			else if ((byte)signalid[i]==(byte)0x24){
				if (getHardwareVersion()==HW_ID.SHIMMER_3){
					signalNameArray[i+1]=Shimmer3.ObjectClusterSensorName.EXG1_CH2_16BIT;
					signalDataTypeArray[i+1] = "i16r";
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|SENSOR_EXG1_16BIT);
				}
			}
			else if ((byte)signalid[i]==(byte)0x25){
				if (getHardwareVersion()==HW_ID.SHIMMER_3){
					signalNameArray[i+1]=Shimmer3.ObjectClusterSensorName.EXG2_CH1_16BIT;
					signalDataTypeArray[i+1] = "i16r";
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|SENSOR_EXG2_16BIT);
				}
			}
			else if ((byte)signalid[i]==(byte)0x26){
				if (getHardwareVersion()==HW_ID.SHIMMER_3){
					signalNameArray[i+1]=Shimmer3.ObjectClusterSensorName.EXG2_CH2_16BIT;
					signalDataTypeArray[i+1] = "i16r";
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|SENSOR_EXG2_16BIT);
				}
			}
			else if ((byte)signalid[i]==(byte)0x27){
				if (getHardwareVersion()==HW_ID.SHIMMER_3){
					signalNameArray[i+1]=Shimmer3.ObjectClusterSensorName.BRIDGE_AMP_HIGH;
					signalDataTypeArray[i+1] = "u12";
					packetSize=packetSize+2;
					enabledSensors= (enabledSensors|SENSOR_BRIDGE_AMP);
				} 
			}
			else if ((byte)signalid[i]==(byte)0x28){
				if (getHardwareVersion()==HW_ID.SHIMMER_3){
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
			pressTempAC1 = UtilParseData.calculatetwoscomplement((int)((int)(pressureResoRes[1] & 0xFF) + ((int)(pressureResoRes[0] & 0xFF) << 8)),16);
			pressTempAC2 = UtilParseData.calculatetwoscomplement((int)((int)(pressureResoRes[3] & 0xFF) + ((int)(pressureResoRes[2] & 0xFF) << 8)),16);
			pressTempAC3 = UtilParseData.calculatetwoscomplement((int)((int)(pressureResoRes[5] & 0xFF) + ((int)(pressureResoRes[4] & 0xFF) << 8)),16);
			pressTempAC4 = (int)((int)(pressureResoRes[7] & 0xFF) + ((int)(pressureResoRes[6] & 0xFF) << 8));
			pressTempAC5 = (int)((int)(pressureResoRes[9] & 0xFF) + ((int)(pressureResoRes[8] & 0xFF) << 8));
			pressTempAC6 = (int)((int)(pressureResoRes[11] & 0xFF) + ((int)(pressureResoRes[10] & 0xFF) << 8));
			pressTempB1 = UtilParseData.calculatetwoscomplement((int)((int)(pressureResoRes[13] & 0xFF) + ((int)(pressureResoRes[12] & 0xFF) << 8)),16);
			pressTempB2 = UtilParseData.calculatetwoscomplement((int)((int)(pressureResoRes[15] & 0xFF) + ((int)(pressureResoRes[14] & 0xFF) << 8)),16);
			pressTempMB = UtilParseData.calculatetwoscomplement((int)((int)(pressureResoRes[17] & 0xFF) + ((int)(pressureResoRes[16] & 0xFF) << 8)),16);
			pressTempMC = UtilParseData.calculatetwoscomplement((int)((int)(pressureResoRes[19] & 0xFF) + ((int)(pressureResoRes[18] & 0xFF) << 8)),16);
			pressTempMD = UtilParseData.calculatetwoscomplement((int)((int)(pressureResoRes[21] & 0xFF) + ((int)(pressureResoRes[20] & 0xFF) << 8)),16);
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
		if (getHardwareVersion() != -1){
			if (getHardwareVersion() != HW_ID.SHIMMER_2R){
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
		if (getHardwareVersion()==HW_ID.SHIMMER_2 || getHardwareVersion()==HW_ID.SHIMMER_2R){
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
			if ((mEnabledSensors & SENSOR_ACCEL) >0){//XXX-RS-AA-SensorClass?
				listofSignals.add(Shimmer3.ObjectClusterSensorName.ACCEL_LN_X);
				listofSignals.add(Shimmer3.ObjectClusterSensorName.ACCEL_LN_Y);
				listofSignals.add(Shimmer3.ObjectClusterSensorName.ACCEL_LN_Z);
			}
			if ((mEnabledSensors& SENSOR_DACCEL) >0){//XXX-RS-LSM-SensorClass?
				listofSignals.add(Shimmer3.ObjectClusterSensorName.ACCEL_WR_X);
				listofSignals.add(Shimmer3.ObjectClusterSensorName.ACCEL_WR_Y);
				listofSignals.add(Shimmer3.ObjectClusterSensorName.ACCEL_WR_Z);
			}
			if (((mEnabledSensors & 0xFF)& SENSOR_GYRO) > 0) {
				listofSignals.add(Shimmer3.ObjectClusterSensorName.GYRO_X);
				listofSignals.add(Shimmer3.ObjectClusterSensorName.GYRO_Y);
				listofSignals.add(Shimmer3.ObjectClusterSensorName.GYRO_Z);
			}
			if (((mEnabledSensors & 0xFF)& SENSOR_MAG) > 0) {//XXX-RS-LSM-SensorClass?
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
			//XXX-RS-LSM-SensorClass? //XXX-RS-AA-SensorClass?
			if ((((mEnabledSensors & 0xFF)& SENSOR_ACCEL) > 0 || ((mEnabledSensors & 0xFFFF)& SENSOR_DACCEL) > 0)&& ((mEnabledSensors & 0xFF)& SENSOR_GYRO) > 0 && ((mEnabledSensors & 0xFF)& SENSOR_MAG) > 0 && mOrientationEnabled){
				listofSignals.add(Shimmer3.ObjectClusterSensorName.AXIS_ANGLE_A);
				listofSignals.add(Shimmer3.ObjectClusterSensorName.AXIS_ANGLE_X);
				listofSignals.add(Shimmer3.ObjectClusterSensorName.AXIS_ANGLE_Y);
				listofSignals.add(Shimmer3.ObjectClusterSensorName.AXIS_ANGLE_Z);
			}
			//XXX-RS-LSM-SensorClass? //XXX-RS-AA-SensorClass?
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
				} else if (isEXGUsingDefaultTestSignalConfiguration()){
					listofSignals.add(Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP1_CH1_24BIT);
					listofSignals.add(Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP1_CH2_24BIT);
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
				} 
				else if (isEXGUsingDefaultTestSignalConfiguration()){
					listofSignals.add(Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP2_CH1_24BIT);
					listofSignals.add(Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP2_CH2_24BIT);
				}
				else {
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
				} else if (isEXGUsingDefaultTestSignalConfiguration()){
					listofSignals.add(Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP1_CH1_16BIT);
					listofSignals.add(Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP1_CH2_16BIT);
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
				} else if (isEXGUsingDefaultTestSignalConfiguration()){
					listofSignals.add(Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP2_CH1_16BIT);
					listofSignals.add(Shimmer3.ObjectClusterSensorName.EXG_TEST_CHIP2_CH2_16BIT);
				}  else {
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

	
	/** Shimmer2r only
	 * @param bufferCalibrationParameters
	 * @param packetType
	 */
	protected void retrieveBiophysicalCalibrationParametersFromPacket(byte[] bufferCalibrationParameters, int packetType){
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

	/**
	 * @param bufferCalibrationParameters
	 * @param packetType
	 */
	public void retrieveKinematicCalibrationParametersFromPacket(byte[] bufferCalibrationParameters, int packetType) {
		
		if (packetType==ACCEL_CALIBRATION_RESPONSE || packetType==LSM303DLHC_ACCEL_CALIBRATION_RESPONSE || packetType==GYRO_CALIBRATION_RESPONSE || packetType==MAG_CALIBRATION_RESPONSE ){
			String[] dataType={"i16","i16","i16","i16","i16","i16","i8","i8","i8","i8","i8","i8","i8","i8","i8"}; 
			int[] formattedPacket = UtilParseData.formatDataPacketReverse(bufferCalibrationParameters,dataType); // using the datatype the calibration parameters are converted
			double[] AM=new double[9];
			for (int i=0;i<9;i++){
				AM[i]=((double)formattedPacket[6+i])/100;
			}

			double[][] AlignmentMatrix = {{AM[0],AM[1],AM[2]},{AM[3],AM[4],AM[5]},{AM[6],AM[7],AM[8]}}; 				
			double[][] SensitivityMatrix = {{formattedPacket[3],0,0},{0,formattedPacket[4],0},{0,0,formattedPacket[5]}}; 
			double[][] OffsetVector = {{formattedPacket[0]},{formattedPacket[1]},{formattedPacket[2]}};
			
			//XXX-RS-AA-SensorClass?
			if(packetType==ACCEL_CALIBRATION_RESPONSE){
				if(checkIfDefaultAccelCal(OffsetVector, SensitivityMatrix, AlignmentMatrix)){
					mDefaultCalibrationParametersAccel = true;
					mAlignmentMatrixAnalogAccel = AlignmentMatrix;
					mOffsetVectorAnalogAccel = OffsetVector;
					mSensitivityMatrixAnalogAccel = SensitivityMatrix;
				}
				else if (SensitivityMatrix[0][0]!=-1) {   //used to be 65535 but changed to -1 as we are now using i16
					mDefaultCalibrationParametersAccel = false;
					mAlignmentMatrixAnalogAccel = AlignmentMatrix;
					mOffsetVectorAnalogAccel = OffsetVector;
					mSensitivityMatrixAnalogAccel = SensitivityMatrix;
				} 
				else if(SensitivityMatrix[0][0]==-1){
					if (getHardwareVersion()!=HW_ID.SHIMMER_3){
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
				
				int rangeValue = 0;
				String rangeString = "+-2g";
				CalibDetailsKinematic calDetails = new CalibDetailsKinematic(rangeValue, rangeString, 
						mAlignmentMatrixAnalogAccel, mSensitivityMatrixAnalogAccel, mOffsetVectorAnalogAccel,
						AlignmentMatrixLowNoiseAccelShimmer3, SensitivityMatrixLowNoiseAccel2gShimmer3, OffsetVectorLowNoiseAccelShimmer3);
//				calDetails.mIsDefaultCal = mDefaultCalibrationParametersAccel;
				mCalibAccelAnalog.put(rangeValue, calDetails);
			}
			
			//XXX-RS-LSM-SensorClass?
			if(packetType==LSM303DLHC_ACCEL_CALIBRATION_RESPONSE){
				if(checkIfDefaultWideRangeAccelCal(OffsetVector, SensitivityMatrix, AlignmentMatrix)){
					mDefaultCalibrationParametersDigitalAccel = true;
					mAlignmentMatrixWRAccel = AlignmentMatrix;
					mOffsetVectorWRAccel = OffsetVector;
					mSensitivityMatrixWRAccel = SensitivityMatrix;
				}
				else if (SensitivityMatrix[0][0]!=-1) {   //used to be 65535 but changed to -1 as we are now using i16
					mDefaultCalibrationParametersDigitalAccel = false;
					mAlignmentMatrixWRAccel = AlignmentMatrix;
					mOffsetVectorWRAccel = OffsetVector;
					mSensitivityMatrixWRAccel = SensitivityMatrix;
				}
				else if(SensitivityMatrix[0][0]==-1){
					setDefaultCalibrationShimmer3WideRangeAccel();
				}
				
				int rangeValue = getAccelRange();
				String rangeString = "TEMP"; //TODO update with getSensorRangeFromConfigValue()
				CalibDetailsKinematic calDetails = new CalibDetailsKinematic(rangeValue, rangeString, mAlignmentMatrixWRAccel, mSensitivityMatrixWRAccel, mOffsetVectorWRAccel);
//				calDetails.mIsDefaultCal = mDefaultCalibrationParametersDigitalAccel;
				mCalibAccelWideRange.put(rangeValue, calDetails);
			}
			
			if(packetType==GYRO_CALIBRATION_RESPONSE){
				if(checkIfDefaulGyroCal(OffsetVector, SensitivityMatrix, AlignmentMatrix)){
					mDefaultCalibrationParametersGyro = true;
					mAlignmentMatrixGyroscope = AlignmentMatrix;
					mOffsetVectorGyroscope = OffsetVector;
					mSensitivityMatrixGyroscope = SensitivityMatrix;
					for(int i=0;i<=2;i++){
						mSensitivityMatrixGyroscope[i][i] = mSensitivityMatrixGyroscope[i][i]/100;
					}
				}
				else if (SensitivityMatrix[0][0]!=-1) {
					mDefaultCalibrationParametersGyro = false;
					mAlignmentMatrixGyroscope = AlignmentMatrix;
					mOffsetVectorGyroscope = OffsetVector;
					mSensitivityMatrixGyroscope = SensitivityMatrix;
					for(int i=0;i<=2;i++){
						mSensitivityMatrixGyroscope[i][i] = mSensitivityMatrixGyroscope[i][i]/100;
					}
				} 
				else if(SensitivityMatrix[0][0]==-1){
					if(getHardwareVersion()!=HW_ID.SHIMMER_3){
						mDefaultCalibrationParametersGyro = true;
						mAlignmentMatrixGyroscope = AlignmentMatrixGyroShimmer2;
						mOffsetVectorGyroscope = OffsetVectorGyroShimmer2;
						mSensitivityMatrixGyroscope = SensitivityMatrixGyroShimmer2;	
					} 
					else {
						setDefaultCalibrationShimmer3Gyro();
					}
				} 
				
				int rangeValue = getGyroRange();
				CalibDetailsKinematic calDetails = mCalibGyro.get(rangeValue);
				if(calDetails==null){
					String rangeString = "TEMP"; //TODO update with getSensorRangeFromConfigValue()
					calDetails = new CalibDetailsKinematic(rangeValue, rangeString);
				}
				calDetails.setCurrentValues(mAlignmentMatrixGyroscope, mSensitivityMatrixGyroscope, mOffsetVectorGyroscope);
//				calDetails.mIsDefaultCal = mDefaultCalibrationParametersDigitalAccel;
				mCalibGyro.put(rangeValue, calDetails);
			}
			
			if(packetType==MAG_CALIBRATION_RESPONSE){
				if(checkIfDefaulMagCal(OffsetVector, SensitivityMatrix, AlignmentMatrix)){
					mDefaultCalibrationParametersMag = true;
					mAlignmentMatrixMagnetometer = AlignmentMatrix;
					mOffsetVectorMagnetometer = OffsetVector;
					mSensitivityMatrixMagnetometer = SensitivityMatrix;
				}
				else if (SensitivityMatrix[0][0]!=-1) {
					mDefaultCalibrationParametersMag = false;
					mAlignmentMatrixMagnetometer = AlignmentMatrix;
					mOffsetVectorMagnetometer = OffsetVector;
					mSensitivityMatrixMagnetometer = SensitivityMatrix;
				}
				else if(SensitivityMatrix[0][0]==-1){
					mDefaultCalibrationParametersMag = true;
					if (getHardwareVersion()!=HW_ID.SHIMMER_3){
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
				
				int rangeValue = mMagRange;
				String rangeString = "TEMP"; //TODO update with getSensorRangeFromConfigValue()
				CalibDetailsKinematic calDetails = new CalibDetailsKinematic(rangeValue, rangeString, mAlignmentMatrixMagnetometer, mSensitivityMatrixMagnetometer, mOffsetVectorMagnetometer);
//				calDetails.mIsDefaultCal = mDefaultCalibrationParametersDigitalAccel;
				mCalibGyro.put(rangeValue, calDetails);
			}
		}
	}
	
	//TODO: MN 22-06-2016
	public String getSensorRangeFromConfigValue(){
//		int index = Arrays.asList(sensorCalDetOb.getSensorRangesConfigValues()).indexOf(sensorCalDetOb.getCurrentConfigValue());
//		return sensorCalDetOb.getSensorRanges()[index];
		
		return "";
	}

	//XXX-RS-AA-SensorClass?
	private boolean checkIfDefaultAccelCal(double[][] offsetVectorToTest, double[][] sensitivityMatrixToTest, double[][] alignmentMatrixToTest) {
		
		double[][] offsetVectorToCompare = OffsetVectorLowNoiseAccelShimmer3;
		double[][] sensitivityVectorToCompare = SensitivityMatrixLowNoiseAccel2gShimmer3;
		double[][] alignmentVectorToCompare = AlignmentMatrixLowNoiseAccelShimmer3;
		
		if (getHardwareVersion()!=HW_ID.SHIMMER_3){
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

	//XXX-RS-LSM-SensorClass?
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
		
		if(getHardwareVersion()==HW_ID.SHIMMER_3){
			if (mGyroRange==0){
				sensitivityVectorToCompare = UtilShimmer.deepCopyDoubleMatrix(SensitivityMatrixGyro250dpsShimmer3);

			} else if (mGyroRange==1){
				sensitivityVectorToCompare = UtilShimmer.deepCopyDoubleMatrix(SensitivityMatrixGyro500dpsShimmer3);

			} else if (mGyroRange==2){
				sensitivityVectorToCompare = UtilShimmer.deepCopyDoubleMatrix(SensitivityMatrixGyro1000dpsShimmer3);

			} else if (mGyroRange==3){
				sensitivityVectorToCompare = UtilShimmer.deepCopyDoubleMatrix(SensitivityMatrixGyro2000dpsShimmer3);
			}
			alignmentVectorToCompare = AlignmentMatrixGyroShimmer3;
			offsetVectorToCompare = OffsetVectorGyroShimmer3;
		}
		
		for(int i=0;i<=2;i++){
			sensitivityVectorToCompare[i][i] = sensitivityVectorToCompare[i][i]*100;
		}
		
		boolean alignmentPass = Arrays.deepEquals(alignmentVectorToCompare, alignmentMatrixToTest);
		boolean offsetPass = Arrays.deepEquals(offsetVectorToCompare, offsetVectorToTest);
		boolean sensitivityPass = Arrays.deepEquals(sensitivityVectorToCompare, sensitivityMatrixToTest);
		
		if(alignmentPass&&offsetPass&&sensitivityPass){
			return true;
		}
		return false;
	}
	
	//XXX-RS-LSM-SensorClass?
	private boolean checkIfDefaulMagCal(double[][] offsetVectorToTest, double[][] sensitivityMatrixToTest, double[][] alignmentMatrixToTest) {
		
		double[][] offsetVectorToCompare = new double[][]{};
		double[][] sensitivityVectorToCompare = new double[][]{};
		double[][] alignmentVectorToCompare = new double[][]{};
		
		if(getHardwareVersion()==HW_ID.SHIMMER_3){
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
	

	
	public void setDefaultCalibrationShimmer3StandardImus(){
		setDefaultCalibrationShimmer3LowNoiseAccel();
		setDefaultCalibrationShimmer3WideRangeAccel();//XXX-RS-LSM-SensorClass?
		setDefaultCalibrationShimmer3Gyro();
		setDefaultCalibrationShimmer3Mag();
	}

	//XXX-RS-AA-SensorClass?
	private void setDefaultCalibrationShimmer3LowNoiseAccel() {
		mDefaultCalibrationParametersAccel = true;
		mSensitivityMatrixAnalogAccel = UtilShimmer.deepCopyDoubleMatrix(SensitivityMatrixLowNoiseAccel2gShimmer3);
		mAlignmentMatrixAnalogAccel = UtilShimmer.deepCopyDoubleMatrix(AlignmentMatrixLowNoiseAccelShimmer3);
		mOffsetVectorAnalogAccel = UtilShimmer.deepCopyDoubleMatrix(OffsetVectorLowNoiseAccelShimmer3);
	}
	
	//XXX-RS-LSM-SensorClass?
	private void setDefaultCalibrationShimmer3WideRangeAccel() {
		mDefaultCalibrationParametersDigitalAccel = true;
		if (getAccelRange()==0){
			mSensitivityMatrixWRAccel = UtilShimmer.deepCopyDoubleMatrix(SensitivityMatrixWideRangeAccel2gShimmer3);
		} else if (getAccelRange()==1){
			mSensitivityMatrixWRAccel = UtilShimmer.deepCopyDoubleMatrix(SensitivityMatrixWideRangeAccel4gShimmer3);
		} else if (getAccelRange()==2){
			mSensitivityMatrixWRAccel = UtilShimmer.deepCopyDoubleMatrix(SensitivityMatrixWideRangeAccel8gShimmer3);
		} else if (getAccelRange()==3){
			mSensitivityMatrixWRAccel = UtilShimmer.deepCopyDoubleMatrix(SensitivityMatrixWideRangeAccel16gShimmer3);
		}
		mAlignmentMatrixWRAccel = UtilShimmer.deepCopyDoubleMatrix(AlignmentMatrixWideRangeAccelShimmer3);
		mOffsetVectorWRAccel = UtilShimmer.deepCopyDoubleMatrix(OffsetVectorWideRangeAccelShimmer3);
	}

	private void setDefaultCalibrationShimmer3Gyro() {
		mDefaultCalibrationParametersGyro = true;
		if (mGyroRange==0){
			mSensitivityMatrixGyroscope = UtilShimmer.deepCopyDoubleMatrix(SensitivityMatrixGyro250dpsShimmer3);

		} else if (mGyroRange==1){
			mSensitivityMatrixGyroscope = UtilShimmer.deepCopyDoubleMatrix(SensitivityMatrixGyro500dpsShimmer3);

		} else if (mGyroRange==2){
			mSensitivityMatrixGyroscope = UtilShimmer.deepCopyDoubleMatrix(SensitivityMatrixGyro1000dpsShimmer3);

		} else if (mGyroRange==3){
			mSensitivityMatrixGyroscope = UtilShimmer.deepCopyDoubleMatrix(SensitivityMatrixGyro2000dpsShimmer3);
		}
		mAlignmentMatrixGyroscope = UtilShimmer.deepCopyDoubleMatrix(AlignmentMatrixGyroShimmer3);
		mOffsetVectorGyroscope = UtilShimmer.deepCopyDoubleMatrix(OffsetVectorGyroShimmer3);
	}
	//XXX-RS-LSM-SensorClass?
	private void setDefaultCalibrationShimmer3Mag() {
		mAlignmentMatrixMagnetometer = UtilShimmer.deepCopyDoubleMatrix(AlignmentMatrixMagShimmer3);
		mOffsetVectorMagnetometer = UtilShimmer.deepCopyDoubleMatrix(OffsetVectorMagShimmer3);
		if (mMagRange==1){
			mSensitivityMatrixMagnetometer = UtilShimmer.deepCopyDoubleMatrix(SensitivityMatrixMag1p3GaShimmer3);
		} else if (mMagRange==2){
			mSensitivityMatrixMagnetometer = UtilShimmer.deepCopyDoubleMatrix(SensitivityMatrixMag1p9GaShimmer3);
		} else if (mMagRange==3){
			mSensitivityMatrixMagnetometer = UtilShimmer.deepCopyDoubleMatrix(SensitivityMatrixMag2p5GaShimmer3);
		} else if (mMagRange==4){
			mSensitivityMatrixMagnetometer = UtilShimmer.deepCopyDoubleMatrix(SensitivityMatrixMag4GaShimmer3);
		} else if (mMagRange==5){
			mSensitivityMatrixMagnetometer = UtilShimmer.deepCopyDoubleMatrix(SensitivityMatrixMag4p7GaShimmer3);
		} else if (mMagRange==6){
			mSensitivityMatrixMagnetometer = UtilShimmer.deepCopyDoubleMatrix(SensitivityMatrixMag5p6GaShimmer3);
		} else if (mMagRange==7){
			mSensitivityMatrixMagnetometer = UtilShimmer.deepCopyDoubleMatrix(SensitivityMatrixMag8p1GaShimmer3);
		}
	}
	
//	//XXX-RS-LSM-SensorClass? //XXX-RS-AA-SensorClass?
//	protected static double[] calibrateInertialSensorData(double[] data, double[][] AM, double[][] SM, double[][] OV) {
//		/*  Based on the theory outlined by Ferraris F, Grimaldi U, and Parvis M.  
//           in "Procedure for effortless in-field calibration of three-axis rate gyros and accelerometers" Sens. Mater. 1995; 7: 311-30.            
//           C = [R^(-1)] .[K^(-1)] .([U]-[B])
//			where.....
//			[C] -> [3 x n] Calibrated Data Matrix 
//			[U] -> [3 x n] Uncalibrated Data Matrix
//			[B] ->  [3 x n] Replicated Sensor Offset Vector Matrix 
//			[R^(-1)] -> [3 x 3] Inverse Alignment Matrix
//			[K^(-1)] -> [3 x 3] Inverse Sensitivity Matrix
//			n = Number of Samples
//		 */
//		double [][] data2d=new double [3][1];
//		data2d[0][0]=data[0];
//		data2d[1][0]=data[1];
//		data2d[2][0]=data[2];
//		data2d= matrixmultiplication(matrixmultiplication(matrixinverse3x3(AM),matrixinverse3x3(SM)),matrixminus(data2d,OV));
//		double[] ansdata=new double[3];
//		ansdata[0]=data2d[0][0];
//		ansdata[1]=data2d[1][0];
//		ansdata[2]=data2d[2][0];
//		return ansdata;
//	}
//
//	private static double[][] matrixinverse3x3(double[][] data) {
//		double a,b,c,d,e,f,g,h,i;
//		a=data[0][0];
//		b=data[0][1];
//		c=data[0][2];
//		d=data[1][0];
//		e=data[1][1];
//		f=data[1][2];
//		g=data[2][0];
//		h=data[2][1];
//		i=data[2][2];
//		//
//		double deter=a*e*i+b*f*g+c*d*h-c*e*g-b*d*i-a*f*h;
//		double[][] answer=new double[3][3];
//		answer[0][0]=(1/deter)*(e*i-f*h);
//
//		answer[0][1]=(1/deter)*(c*h-b*i);
//		answer[0][2]=(1/deter)*(b*f-c*e);
//		answer[1][0]=(1/deter)*(f*g-d*i);
//		answer[1][1]=(1/deter)*(a*i-c*g);
//		answer[1][2]=(1/deter)*(c*d-a*f);
//		answer[2][0]=(1/deter)*(d*h-e*g);
//		answer[2][1]=(1/deter)*(g*b-a*h);
//		answer[2][2]=(1/deter)*(a*e-b*d);
//		return answer;
//	}
//	private static double[][] matrixminus(double[][] a ,double[][] b) {
//		int aRows = a.length,
//				aColumns = a[0].length,
//				bRows = b.length,
//				bColumns = b[0].length;
//		if (( aColumns != bColumns )&&( aRows != bRows )) {
//			throw new IllegalArgumentException(" Matrix did not match");
//		}
//		double[][] resultant = new double[aRows][bColumns];
//		for(int i = 0; i < aRows; i++) { // aRow
//			for(int k = 0; k < aColumns; k++) { // aColumn
//
//				resultant[i][k]=a[i][k]-b[i][k];
//
//			}
//		}
//		return resultant;
//	}
//
//	private static double[][] matrixmultiplication(double[][] a, double[][] b) {
//
//		int aRows = a.length,
//				aColumns = a[0].length,
//				bRows = b.length,
//				bColumns = b[0].length;
//
//		if ( aColumns != bRows ) {
//			throw new IllegalArgumentException("A:Rows: " + aColumns + " did not match B:Columns " + bRows + ".");
//		}
//
//		double[][] resultant = new double[aRows][bColumns];
//
//		for(int i = 0; i < aRows; i++) { // aRow
//			for(int j = 0; j < bColumns; j++) { // bColumn
//				for(int k = 0; k < aColumns; k++) { // aColumn
//					resultant[i][j] += a[i][k] * b[k][j];
//				}
//			}
//		}
//
//		return resultant;
//	}

	
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
			double expectedTimeDifference = (1/getSamplingRateShimmer())*1000;
			double expectedTimeDifferenceLimit = expectedTimeDifference + (expectedTimeDifference*0.1); 
			//if (timeDifference>(1/(mShimmerSamplingRate-1))*1000){
			if (timeDifference>expectedTimeDifferenceLimit){
//				mPacketLossCount=mPacketLossCount+1;
				mPacketLossCount+= (long) (timeDifference/expectedTimeDifferenceLimit);
				Long mTotalNumberofPackets=(long) ((calibratedTimeStamp-mCalTimeStart)/(1/getSamplingRateShimmer()*1000));

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

	protected double[] calibratePressureSensorData(double UP, double UT){
		double X1 = (UT - pressTempAC6) * pressTempAC5 / 32768;
		double X2 = (pressTempMC * 2048 / (X1 + pressTempMD));
		double B5 = X1 + X2;
		double T = (B5 + 8) / 16;

		double B6 = B5 - 4000;
		X1 = (pressTempB2 * (Math.pow(B6,2)/ 4096)) / 2048;
		X2 = pressTempAC2 * B6 / 2048;
		double X3 = X1 + X2;
		double B3 = (((pressTempAC1 * 4 + X3)*(1<<mPressureResolution) + 2)) / 4;
		X1 = pressTempAC3 * B6 / 8192;
		X2 = (pressTempB1 * (Math.pow(B6,2)/ 4096)) / 65536;
		X3 = ((X1 + X2) + 2) / 4;
		double B4 = pressTempAC4 * (X3 + 32768) / 32768;
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


	protected static double calibrateU12AdcValue(double uncalibratedData,double offset,double vRefP,double gain){
		double calibratedData=(uncalibratedData-offset)*(((vRefP*1000)/gain)/4095);
		return calibratedData;
	}
	
	/** 
	 * SEE Bridge Amplifer+ User Manual for Details
	 * 
	 * y = -27.42ln(x) + 56.502 
	 * where y = temperature in degC
	 * where x = (200*Vo)/((10.1)Pv-Vo)
	 * where Pv = 3000mV
	 * where Vo = Uncalibrated output of the resistance amplifier channel
	 * 
	*/
	
	// YYY implemented in SensorBridgeAmp
	protected static double calibratePhillipsSkinTemperatureData(double uncalibratedData){
		double x = (200.0*uncalibratedData)/((10.1)*3000-uncalibratedData);
		double y = -27.42*Math.log(x) + 56.502;
		return y;
	}

//	protected static double calibrateGsrData(double gsrUncalibratedData,double p1, double p2){
//		gsrUncalibratedData = (double)((int)gsrUncalibratedData & 4095); 
//		//the following polynomial is deprecated and has been replaced with a more accurate linear one, see GSR user guide for further details
//		//double gsrCalibratedData = (p1*Math.pow(gsrUncalibratedData,4)+p2*Math.pow(gsrUncalibratedData,3)+p3*Math.pow(gsrUncalibratedData,2)+p4*gsrUncalibratedData+p5)/1000;
//		//the following is the new linear method see user GSR user guide for further details
//		double gsrCalibratedData = (1/((p1*gsrUncalibratedData)+p2)*1000); //kohms 
//		return gsrCalibratedData;  
//	}
// 
//	protected static double calibrateGsrDataToSiemens(double gsrUncalibratedData,double p1, double p2){
//		gsrUncalibratedData = (double)((int)gsrUncalibratedData & 4095); 
//		//the following polynomial is deprecated and has been replaced with a more accurate linear one, see GSR user guide for further details
//		//double gsrCalibratedData = (p1*Math.pow(gsrUncalibratedData,4)+p2*Math.pow(gsrUncalibratedData,3)+p3*Math.pow(gsrUncalibratedData,2)+p4*gsrUncalibratedData+p5)/1000;
//		//the following is the new linear method see user GSR user guide for further details
//		double gsrCalibratedData = (((p1*gsrUncalibratedData)+p2)); //microsiemens 
//		return gsrCalibratedData;  
//	}

//	public double getSamplingRateShimmer(){
//		return getSamplingRateShimmer(COMMUNICATION_TYPE.SD); 
//	}
//	
//	public void setSamplingRateShimmer(double samplingRate){
//		//In Shimmer3 the SD and BT have the same sampling rate 
//		setSamplingRateShimmer(COMMUNICATION_TYPE.SD, samplingRate);
//		setSamplingRateShimmer(COMMUNICATION_TYPE.BLUETOOTH, samplingRate);
//	}


	public int getPressureResolution(){
		return mPressureResolution;
	}


	public int getGSRRange(){
		return mGSRRange;
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
		if (getHardwareVersion()==HW_ID.SHIMMER_2 || getHardwareVersion()==HW_ID.SHIMMER_2R){
			mPacketSize = mTimeStampPacketByteSize +bufferInquiry[3]*2; 
			setSamplingRateShimmer((double)1024/bufferInquiry[0]);
			if (mLSM303MagRate==3 && getSamplingRateShimmer()>10){
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
		else if (getHardwareVersion()==HW_ID.SHIMMER_3) {
			mPacketSize = mTimeStampPacketByteSize+bufferInquiry[6]*2; 
			setSamplingRateShimmer((32768/(double)((int)(bufferInquiry[0] & 0xFF) + ((int)(bufferInquiry[1] & 0xFF) << 8))));
			mNChannels = bufferInquiry[6];
			mBufferSize = bufferInquiry[7];
			mConfigByte0 = ((long)(bufferInquiry[2] & 0xFF) +((long)(bufferInquiry[3] & 0xFF) << 8)+((long)(bufferInquiry[4] & 0xFF) << 16) +((long)(bufferInquiry[5] & 0xFF) << 24));
			mAccelRange = ((int)(mConfigByte0 & 0xC))>>2;//XXX-RS-LSM-SensorClass?
			mGyroRange = ((int)(mConfigByte0 & 196608))>>16;
			mMagRange = ((int)(mConfigByte0 & 14680064))>>21;//XXX-RS-LSM-SensorClass?
			mLSM303DigitalAccelRate = ((int)(mConfigByte0 & 0xF0))>>4;//XXX-RS-LSM-SensorClass?
			mMPU9150GyroAccelRate = ((int)(mConfigByte0 & 65280))>>8;
			mLSM303MagRate = ((int)(mConfigByte0 & 1835008))>>18; //XXX-RS-LSM-SensorClass?
			mPressureResolution = (((int)(mConfigByte0 >>28)) & 3);
			mGSRRange  = (((int)(mConfigByte0 >>25)) & 7);
			mInternalExpPower = (((int)(mConfigByte0 >>24)) & 1);
			mInquiryResponseBytes = new byte[8+mNChannels];
			System.arraycopy(bufferInquiry, 0, mInquiryResponseBytes , 0, mInquiryResponseBytes.length);
			if ((mLSM303DigitalAccelRate==2 && getSamplingRateShimmer()>10)){
				mLowPowerAccelWR = true;
			}
			if ((mMPU9150GyroAccelRate==0xFF && getSamplingRateShimmer()>10)){
				mLowPowerGyro = true;
			}
			if ((mLSM303MagRate==4 && getSamplingRateShimmer()>10)){
				mLowPowerMag = true;
			}
			byte[] signalIdArray = new byte[mNChannels];
			System.arraycopy(bufferInquiry, 8, signalIdArray, 0, mNChannels);
			updateEnabledSensorsFromChannels(signalIdArray);
			interpretDataPacketFormat(mNChannels,signalIdArray);
			checkExgResolutionFromEnabledSensorsVar();
		} 
		else if (getHardwareVersion()==HW_ID.SHIMMER_SR30) {
			mPacketSize = mTimeStampPacketByteSize+bufferInquiry[2]*2; 
			setSamplingRateShimmer((double)1024/bufferInquiry[0]);
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
			if (getHardwareVersion()==HW_ID.SHIMMER_3){
				//XXX-RS-AA-SensorClass?
				if (channels[i]==Configuration.Shimmer3.Channel.XAAccel || channels[i]==Configuration.Shimmer3.Channel.YAAccel || channels[i]==Configuration.Shimmer3.Channel.ZAAccel){
					enabledSensors = enabledSensors | SENSOR_ACCEL;
				}
				//XXX-RS-LSM-SensorClass?
				if (channels[i]==Configuration.Shimmer3.Channel.XDAccel || channels[i]==Configuration.Shimmer3.Channel.YDAccel||channels[i]==Configuration.Shimmer3.Channel.ZDAccel){
					enabledSensors = enabledSensors | SENSOR_DACCEL;
				}
				if (channels[i]==Configuration.Shimmer3.Channel.XGyro || channels[i]==Configuration.Shimmer3.Channel.YGyro||channels[i]==Configuration.Shimmer3.Channel.ZGyro){
					enabledSensors = enabledSensors | SENSOR_GYRO;
				}
				//XXX-RS-LSM-SensorClass?
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

			} else if(getHardwareVersion()==HW_ID.SHIMMER_2R){
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
		if (getHardwareVersion()==HW_ID.SHIMMER_3)
		{
			//Accel + Digi Accel + Gyro + Mag + Pressure
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
			try {
				outputStream.write(5); // write the number of different calibration parameters
				//XXX-RS-AA-SensorClass?
				outputStream.write( mAccelCalRawParams.length);
				outputStream.write( mAccelCalRawParams);
				
				outputStream.write( mDigiAccelCalRawParams.length);//XXX-RS-LSM-SensorClass?
				outputStream.write( mDigiAccelCalRawParams );//XXX-RS-LSM-SensorClass?
				outputStream.write( mGyroCalRawParams.length );
				outputStream.write( mGyroCalRawParams );
				outputStream.write( mMagCalRawParams.length );//XXX-RS-LSM-SensorClass?
				outputStream.write( mMagCalRawParams );//XXX-RS-LSM-SensorClass?
				outputStream.write( mPressureCalRawParams.length);
				outputStream.write( mPressureCalRawParams );
				rawcal = outputStream.toByteArray( );
			} catch (IOException e) {
				e.printStackTrace();
			}			

		} else if (getHardwareVersion()==HW_ID.SHIMMER_2 ||getHardwareVersion()==HW_ID.SHIMMER_2R)
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
		if (getHardwareVersion()==HW_ID.SHIMMER_3){
			//XXX-RS-AA-SensorClass?
			if (((mEnabledSensors & 0xFF)& SENSOR_ACCEL) > 0){
				listofSensors.add("Low Noise Accelerometer");
			}
			
			if ((mEnabledSensors & SENSOR_DACCEL) > 0){//XXX-RS-LSM-SensorClass?
				listofSensors.add("Wide Range Accelerometer");
			}
			//XXX-RS-AA-SensorClass?
		} else {
			if (((mEnabledSensors & 0xFF)& SENSOR_ACCEL) > 0){
				listofSensors.add("Accelerometer");
			}
		}
		
		if (((mEnabledSensors & 0xFF)& SENSOR_GYRO) > 0) {
			listofSensors.add("Gyroscope");
		}
		if (((mEnabledSensors & 0xFF)& SENSOR_MAG) > 0) {//XXX-RS-LSM-SensorClass?
			listofSensors.add("Magnetometer");
		}
		if (((mEnabledSensors & 0xFF) & SENSOR_GSR) > 0) {
			listofSensors.add("GSR");
		}
		if (getHardwareVersion()==HW_ID.SHIMMER_2 || getHardwareVersion()==HW_ID.SHIMMER_2R){
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
		if (((mEnabledSensors & 0xFF) & SENSOR_EXP_BOARD_A0) > 0 && (mEnabledSensors & SENSOR_BATT) == 0 && getHardwareVersion() != HW_ID.SHIMMER_3) {
			listofSensors.add("ExpBoard A0");
		}
		if (((mEnabledSensors & 0xFF) & SENSOR_EXP_BOARD_A7) > 0  && (mEnabledSensors & SENSOR_BATT) == 0 && getHardwareVersion() != HW_ID.SHIMMER_3) {
			listofSensors.add("ExpBoard A7");
		}
		if ((mEnabledSensors & SENSOR_BATT) > 0) {
			listofSensors.add("Battery Voltage");
		}
		if (((mEnabledSensors & 0xFF) & SENSOR_EXT_ADC_A7) > 0  && getHardwareVersion() == HW_ID.SHIMMER_3) {
			listofSensors.add("External ADC A7");
		}
		if (((mEnabledSensors & 0xFF) & SENSOR_EXT_ADC_A6) > 0  && getHardwareVersion() == HW_ID.SHIMMER_3) {
			listofSensors.add("External ADC A6");
		}
		if (((mEnabledSensors & 0xFFFF) & SENSOR_EXT_ADC_A15) > 0  && getHardwareVersion() == HW_ID.SHIMMER_3) {
			listofSensors.add("External ADC A15");
		}
		if (((mEnabledSensors & 0xFFFF) & SENSOR_INT_ADC_A1) > 0  && getHardwareVersion() == HW_ID.SHIMMER_3) {
			listofSensors.add("Internal ADC A1");
		}
		if (((mEnabledSensors & 0xFFFF) & SENSOR_INT_ADC_A12) > 0  && getHardwareVersion() == HW_ID.SHIMMER_3) {
			listofSensors.add("Internal ADC A12");
		}
		if (((mEnabledSensors & 0xFFFFFF) & SENSOR_INT_ADC_A13) > 0  && getHardwareVersion() == HW_ID.SHIMMER_3) {
			listofSensors.add("Internal ADC A13");
		}
		if (((mEnabledSensors & 0xFFFFFF) & SENSOR_INT_ADC_A14) > 0  && getHardwareVersion() == HW_ID.SHIMMER_3) {
			listofSensors.add("Internal ADC A14");
		}
		if ((mEnabledSensors & SENSOR_BMP180) > 0  && getHardwareVersion() == HW_ID.SHIMMER_3) {
			listofSensors.add("Pressure");
		}
		if ((mEnabledSensors & SENSOR_EXG1_24BIT) > 0  && getHardwareVersion() == HW_ID.SHIMMER_3) {
			listofSensors.add("EXG1");
		}
		if ((mEnabledSensors & SENSOR_EXG2_24BIT) > 0  && getHardwareVersion() == HW_ID.SHIMMER_3) {
			listofSensors.add("EXG2");
		}
		if ((mEnabledSensors & SENSOR_EXG1_16BIT) > 0  && getHardwareVersion() == HW_ID.SHIMMER_3) {
			listofSensors.add("EXG1 16Bit");
		}
		if ((mEnabledSensors & SENSOR_EXG2_16BIT) > 0  && getHardwareVersion() == HW_ID.SHIMMER_3) {
			listofSensors.add("EXG2 16Bit");
		}

		return listofSensors;
	}
	
	/** Returns a list of string[] of the four properties. 1) Shimmer Name - 2) Property/Signal Name - 3) Format Name - 4) Unit Name
	 * @return list string array of properties
	 */
	public List<String[]> getListofEnabledSensorSignalsandFormats(){
		List<String[]> listofSignals = new ArrayList<String[]>();
		 
		if (getHardwareVersion()==HW_ID.SHIMMER_2 || getHardwareVersion()==HW_ID.SHIMMER_2R){
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

		} else if (getHardwareVersion()==HW_ID.SHIMMER_3) {

			String[] channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.TIMESTAMP,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLISECONDS};
			listofSignals.add(channel);
			channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.TIMESTAMP,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS};
			listofSignals.add(channel);
			//XXX-RS-AA-SensorClass?
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
			if ((mEnabledSensors& SENSOR_DACCEL) >0){//XXX-RS-LSM-SensorClass?


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
			if (((mEnabledSensors & 0xFF)& SENSOR_MAG) > 0) {//XXX-RS-LSM-SensorClass?
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
				String sensorName = Shimmer3.ObjectClusterSensorName.INT_EXP_ADC_A12;
				if ((mDerivedSensors & DerivedSensorsBitMask.PPG_12_13)>0){
					sensorName = Shimmer3.ObjectClusterSensorName.PPG_A12;
				} else if ((mDerivedSensors & DerivedSensorsBitMask.PPG1_12_13)>0){
					sensorName = Shimmer3.ObjectClusterSensorName.PPG1_A12;
				}
				channel = new String[]{mShimmerUserAssignedName,sensorName,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS};
				listofSignals.add(channel);
				channel = new String[]{mShimmerUserAssignedName,sensorName,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS};
				listofSignals.add(channel);
			}
			if (((mEnabledSensors & 0xFFFFFF)& SENSOR_INT_ADC_A13) > 0) {
				String sensorName = Shimmer3.ObjectClusterSensorName.INT_EXP_ADC_A13;
				if ((mDerivedSensors & DerivedSensorsBitMask.PPG_12_13)>0){
					sensorName = Shimmer3.ObjectClusterSensorName.PPG_A13;
				} else if ((mDerivedSensors & DerivedSensorsBitMask.PPG1_12_13)>0){
					sensorName = Shimmer3.ObjectClusterSensorName.PPG1_A13;
				}
				
				channel = new String[]{mShimmerUserAssignedName,sensorName,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS};
				listofSignals.add(channel);
				channel = new String[]{mShimmerUserAssignedName,sensorName,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS};
				listofSignals.add(channel);
				
				
			}
			if (((mEnabledSensors & 0xFFFFFF)& SENSOR_INT_ADC_A14) > 0) {
				channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.INT_EXP_ADC_A14,CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS};
				listofSignals.add(channel);
				channel = new String[]{mShimmerUserAssignedName,Shimmer3.ObjectClusterSensorName.INT_EXP_ADC_A14,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS};
				listofSignals.add(channel);
			}
			//XXX-RS-LSM-SensorClass? //XXX-RS-AA-SensorClass?
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
			//XXX-RS-LSM-SensorClass?
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
		
		
		//Currently just used by the REAL_TIME_CLOCK channel
		if (mExtraSignalProperties != null){
			listofSignals.addAll(mExtraSignalProperties);
		}
		
		//Process enabled algorithms
		listofSignals.addAll(getListofEnabledAlgorithmsSignalsandFormats());
		
		return listofSignals;
	}
	
	@Deprecated
	public void addAlgorithm(String key,AbstractAlgorithm aobj){
		if (!doesAlgorithmAlreadyExist(aobj)){
			mMapOfAlgorithmModules.put(key,aobj);
			String[] outputNameArray = aobj.getSignalOutputNameArray();
			String[] outputFormatArray = aobj.getSignalOutputFormatArray();
			String[] outputUnitArray = aobj.getSignalOutputUnitArray();

			for (int i=0;i<outputNameArray.length;i++){
				String[] prop= new String[4];
				prop[0] = mShimmerUserAssignedName;
				prop[1] = outputNameArray[i];
				prop[2] = outputFormatArray[i];
				prop[3] = outputUnitArray[i];
				addExtraSignalProperty(prop);
			}
		}
	}
	
	@Deprecated
	public void addExtraSignalProperty(String [] property){
		if (mExtraSignalProperties==null){
			mExtraSignalProperties = new ArrayList<String[]>();
		}
		mExtraSignalProperties.add(property);
	}
	
	@Deprecated
	public void clearExtraSignalProperties(){
		if (mExtraSignalProperties!=null){
			mExtraSignalProperties.clear();
		}
	}
	
	@Deprecated
	public void removeExtraSignalProperty(String [] property){
		if(mExtraSignalProperties!=null){//JC: fix for consensys 4.3
			for (int i=mExtraSignalProperties.size()-1;i>-1;i--){
				String[]p = mExtraSignalProperties.get(i);
				if (p[0].equals(property[0]) && p[1].equals(property[1]) && p[2].equals(property[2]) && p[3].equals(property[3])){
					mExtraSignalProperties.remove(i);
				}

			}
		}
	}
	
	//AlignmentMatrixMag, SensitivityMatrixMag, OffsetVectorMag



	public double[][] getAlignmentMatrixGyro(){
		return mAlignmentMatrixGyroscope;
	}

	public double[][] getSensitivityMatrixGyro(){
		return mSensitivityMatrixGyroscope;
	}

	public double[][] getOffsetVectorMatrixGyro(){
		return mOffsetVectorGyroscope;
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


	public long getRTCOffset(){
		return mRTCOffset;
	}

	public String[] getListofSupportedSensors(){
		String[] sensorNames = null;
		if (getHardwareVersion()==HW_ID.SHIMMER_2R || getHardwareVersion()==HW_ID.SHIMMER_2){
			sensorNames = Configuration.Shimmer2.ListofCompatibleSensors;
		} else if  (getHardwareVersion()==HW_ID.SHIMMER_3){
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

	public byte[] getPressureRawCoefficients(){
		return mPressureCalRawParams;
	}
	
	
	/* (non-Javadoc)
	 * @see com.shimmerresearch.driver.ShimmerDevice#calcMaxSamplingRate()
	 */
	@Override
	protected double calcMaxSamplingRate() {
		double maxGUISamplingRate = 2048.0;
		
		if (getHardwareVersion()==HW_ID.SHIMMER_2 || getHardwareVersion()==HW_ID.SHIMMER_2R) {
			maxGUISamplingRate = 1024.0;
		} else if (getHardwareVersion()==HW_ID.SHIMMER_3 || getHardwareVersion()==HW_ID.SHIMMER_GQ_BLE) {
			//check if an MPL channel is enabled - limit rate to 51.2Hz
			if(checkIfAnyMplChannelEnabled() && getFirmwareIdentifier()==ShimmerVerDetails.FW_ID.SDLOG){
//				rate = 51.2;
				maxGUISamplingRate = 51.2;
			}
		}	
		return maxGUISamplingRate;
	}

	/* (non-Javadoc)
	 * @see com.shimmerresearch.driver.ShimmerDevice#setSamplingRateSensors(double)
	 */
	@Override
	protected void setSamplingRateSensors(double samplingRateShimmer) {
		if(getHardwareVersion()==HW_ID.SHIMMER_2 || getHardwareVersion()==HW_ID.SHIMMER_2R) {
			if(!mLowPowerMag){
				if(samplingRateShimmer<=10) {
					mShimmer2MagRate = 4;
				} 
				else if (samplingRateShimmer<=20) {
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
		else if (getHardwareVersion()==HW_ID.SHIMMER_3 || getHardwareVersion()==HW_ID.SHIMMER_GQ_BLE) {
			setLSM303MagRateFromFreq(samplingRateShimmer);
			setLSM303AccelRateFromFreq(samplingRateShimmer);
			setMPU9150GyroAccelRateFromFreq(samplingRateShimmer);
			setExGRateFromFreq(samplingRateShimmer);
			
			if(getFirmwareIdentifier()==FW_ID.SDLOG){
				setMPU9150MagRateFromFreq(samplingRateShimmer);
				setMPU9150MplRateFromFreq(samplingRateShimmer);
			}
		}
	}

	/**
	 * Checks to see if the MPU9150 gyro is in low power mode. As determined by
	 * the sensor's sampling rate being set to the lowest possible value and not
	 * related to any specific configuration bytes sent to the Shimmer/MPU9150.
	 * 
	 * @return boolean, true if low-power mode enabled
	 */
	
//	------------Implemented in MPU----------------
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
	//XXX-RS-LSM-SensorClass?
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
	 * This enables the calculation of 3D orientation through the use of the
	 * gradient descent algorithm, note that the user will have to ensure that
	 * mEnableCalibration has been set to true (see enableCalibration), and that
	 * the accel, gyro and mag has been enabled
	 * 
	 * @param enable
	 */
	//XXX-RS-LSM-SensorClass? //XXX-RS-AA-SensorClass?
	protected void set3DOrientation(boolean enable){
		//enable the sensors if they have not been enabled 
		mOrientationEnabled = enable;
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
		if(getHardwareVersion()==HW_ID.SHIMMER_3 || getHardwareVersion()==HW_ID.SHIMMER_4_SDK){
			if(!checkIfAnyMplChannelEnabled()) {
				mLowPowerGyro = enable;
				setMPU9150GyroAccelRateFromFreq(getSamplingRateShimmer());
			}
			else{
				mLowPowerGyro = false;
				setMPU9150GyroAccelRateFromFreq(getSamplingRateShimmer());
			}
		}
	}
	

	
	/* Need to override here because ShimmerDevice uses a different sensormap
	 * (non-Javadoc)
	 * @see com.shimmerresearch.driver.ShimmerDevice#setDefaultShimmerConfiguration()
	 */
	@Override
	public void setDefaultShimmerConfiguration() {
		if (getHardwareVersion() != -1){
			
//			mShimmerUserAssignedName = DEFAULT_SHIMMER_NAME;
			setDefaultShimmerName();
			
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
			
			setShimmerAndSensorsSamplingRate(51.2);
			setDefaultECGConfiguration(getSamplingRateShimmer()); 
			
			syncNodesList.clear();
			
			sensorAndConfigMapsCreate();
			if (getHardwareVersion() == HW_ID.SHIMMER_3){
				//XXX-RS-AA-SensorClass?
				setSensorEnabledState(Configuration.Shimmer3.SensorMapKey.SHIMMER_ANALOG_ACCEL, true);
				
				setSensorEnabledState(Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_GYRO, true);
				setSensorEnabledState(Configuration.Shimmer3.SensorMapKey.SHIMMER_LSM303DLHC_MAG, true);//XXX-RS-LSM-SensorClass? Where is the LSM303DLHC_ACCEL?
				setSensorEnabledState(Configuration.Shimmer3.SensorMapKey.SHIMMER_VBATT, true);
			}
		}
	}
	
	private void setDefaultShimmerName(){
		mShimmerUserAssignedName = DEFAULT_SHIMMER_NAME;

		String macParsed = getMacIdParsed();
		if(macParsed.isEmpty()){
			return;
		}
		
		if(mShimmerUserAssignedName.length()>7) { // 12 char max minus "_XXXX"
			mShimmerUserAssignedName = mShimmerUserAssignedName.substring(0, 7);
		}
		mShimmerUserAssignedName += "_" + macParsed; 
		return;
	}
	
	/**
	 * Parse the Shimmer's Information Memory when read through the Shimmer
	 * Dock/Consensys Base. The Information Memory is a region of the Shimmer's
	 * inbuilt RAM where all configuration information is stored.
	 * 
	 * @param infoMemBytes
	 *            the array of InfoMem bytes.
	 */
	@Override
	public void infoMemByteArrayParse(byte[] infoMemBytes) {
		String shimmerName = "";

		if(!InfoMemLayout.checkInfoMemValid(infoMemBytes)){
			// InfoMem not valid
			setDefaultShimmerConfiguration();
			mShimmerUsingConfigFromInfoMem = false;

//			mShimmerInfoMemBytes = infoMemByteArrayGenerate();
//			mShimmerInfoMemBytes = new byte[infoMemContents.length];
			mInfoMemBytes = infoMemBytes;
		}
		else {
			InfoMemLayoutShimmer3 infoMemLayoutCast = (InfoMemLayoutShimmer3) mInfoMemLayout;

			mShimmerUsingConfigFromInfoMem = true;

			// InfoMem valid
			
			mInfoMemBytes = infoMemBytes;
			createInfoMemLayoutObjectIfNeeded();

			// InfoMem D - Start - used by BtStream, SdLog and LogAndStream
			// Sampling Rate
			setSamplingRateShimmer((32768/(double)((int)(infoMemBytes[infoMemLayoutCast.idxShimmerSamplingRate] & infoMemLayoutCast.maskShimmerSamplingRate) 
					+ ((int)(infoMemBytes[infoMemLayoutCast.idxShimmerSamplingRate+1] & infoMemLayoutCast.maskShimmerSamplingRate) << 8))));
	
			mBufferSize = (int)(infoMemBytes[infoMemLayoutCast.idxBufferSize] & infoMemLayoutCast.maskBufferSize);
			
			// Sensors
			mEnabledSensors = ((long)infoMemBytes[infoMemLayoutCast.idxSensors0] & infoMemLayoutCast.maskSensors) << infoMemLayoutCast.byteShiftSensors0;
			mEnabledSensors += ((long)infoMemBytes[infoMemLayoutCast.idxSensors1] & infoMemLayoutCast.maskSensors) << infoMemLayoutCast.byteShiftSensors1;
			mEnabledSensors += ((long)infoMemBytes[infoMemLayoutCast.idxSensors2] & infoMemLayoutCast.maskSensors) << infoMemLayoutCast.byteShiftSensors2;

			checkExgResolutionFromEnabledSensorsVar();

			// Configuration
			//XXX-RS-LSM-SensorClass?
			mLSM303DigitalAccelRate = (infoMemBytes[infoMemLayoutCast.idxConfigSetupByte0] >> infoMemLayoutCast.bitShiftLSM303DLHCAccelSamplingRate) & infoMemLayoutCast.maskLSM303DLHCAccelSamplingRate; 
			mAccelRange = (infoMemBytes[infoMemLayoutCast.idxConfigSetupByte0] >> infoMemLayoutCast.bitShiftLSM303DLHCAccelRange) & infoMemLayoutCast.maskLSM303DLHCAccelRange;
			if(((infoMemBytes[infoMemLayoutCast.idxConfigSetupByte0] >> infoMemLayoutCast.bitShiftLSM303DLHCAccelLPM) & infoMemLayoutCast.maskLSM303DLHCAccelLPM) == infoMemLayoutCast.maskLSM303DLHCAccelLPM) {
				mLowPowerAccelWR = true;
			}
			else {
				mLowPowerAccelWR = false;
			}
			if(((infoMemBytes[infoMemLayoutCast.idxConfigSetupByte0] >> infoMemLayoutCast.bitShiftLSM303DLHCAccelHRM) & infoMemLayoutCast.maskLSM303DLHCAccelHRM) == infoMemLayoutCast.maskLSM303DLHCAccelHRM) {
				mHighResAccelWR = true;
			}
			else {
				mHighResAccelWR = false;
			}
			
			mMPU9150GyroAccelRate = (infoMemBytes[infoMemLayoutCast.idxConfigSetupByte1] >> infoMemLayoutCast.bitShiftMPU9150AccelGyroSamplingRate) & infoMemLayoutCast.maskMPU9150AccelGyroSamplingRate;
			checkLowPowerGyro(); // check rate to determine if Sensor is in LPM mode
			
			//XXX-RS-LSM-SensorClass?
			mMagRange = (infoMemBytes[infoMemLayoutCast.idxConfigSetupByte2] >> infoMemLayoutCast.bitShiftLSM303DLHCMagRange) & infoMemLayoutCast.maskLSM303DLHCMagRange;
			mLSM303MagRate = (infoMemBytes[infoMemLayoutCast.idxConfigSetupByte2] >> infoMemLayoutCast.bitShiftLSM303DLHCMagSamplingRate) & infoMemLayoutCast.maskLSM303DLHCMagSamplingRate;
			checkLowPowerMag(); // check rate to determine if Sensor is in LPM mode

			mGyroRange = (infoMemBytes[infoMemLayoutCast.idxConfigSetupByte2] >> infoMemLayoutCast.bitShiftMPU9150GyroRange) & infoMemLayoutCast.maskMPU9150GyroRange;
			
			mMPU9150AccelRange = (infoMemBytes[infoMemLayoutCast.idxConfigSetupByte3] >> infoMemLayoutCast.bitShiftMPU9150AccelRange) & infoMemLayoutCast.maskMPU9150AccelRange;
			mPressureResolution = (infoMemBytes[infoMemLayoutCast.idxConfigSetupByte3] >> infoMemLayoutCast.bitShiftBMP180PressureResolution) & infoMemLayoutCast.maskBMP180PressureResolution;
			mGSRRange = (infoMemBytes[infoMemLayoutCast.idxConfigSetupByte3] >> infoMemLayoutCast.bitShiftGSRRange) & infoMemLayoutCast.maskGSRRange;
			mInternalExpPower = (infoMemBytes[infoMemLayoutCast.idxConfigSetupByte3] >> infoMemLayoutCast.bitShiftEXPPowerEnable) & infoMemLayoutCast.maskEXPPowerEnable;
			
			//EXG Configuration
			System.arraycopy(infoMemBytes, infoMemLayoutCast.idxEXGADS1292RChip1Config1, mEXG1RegisterArray, 0, 10);
			System.arraycopy(infoMemBytes, infoMemLayoutCast.idxEXGADS1292RChip2Config1, mEXG2RegisterArray, 0, 10);
			exgBytesGetConfigFrom(mEXG1RegisterArray, mEXG2RegisterArray);
			
			mBluetoothBaudRate = infoMemBytes[infoMemLayoutCast.idxBtCommBaudRate] & infoMemLayoutCast.maskBaudRate;
			//TODO: hack below -> fix
//			if(!(mBluetoothBaudRate>=0 && mBluetoothBaudRate<=10)){
//				mBluetoothBaudRate = 0; 
//			}
			
			byte[] bufferCalibrationParameters = new byte[infoMemLayoutCast.lengthGeneralCalibrationBytes];
			//XXX-RS-AA-SensorClass?
			// Analog Accel Calibration Parameters
			System.arraycopy(infoMemBytes, infoMemLayoutCast.idxAnalogAccelCalibration, bufferCalibrationParameters, 0 , infoMemLayoutCast.lengthGeneralCalibrationBytes);
			retrieveKinematicCalibrationParametersFromPacket(bufferCalibrationParameters, ACCEL_CALIBRATION_RESPONSE);
			
			// MPU9150 Gyroscope Calibration Parameters
			bufferCalibrationParameters = new byte[infoMemLayoutCast.lengthGeneralCalibrationBytes];
			System.arraycopy(infoMemBytes, infoMemLayoutCast.idxMPU9150GyroCalibration, bufferCalibrationParameters, 0 , infoMemLayoutCast.lengthGeneralCalibrationBytes);
			retrieveKinematicCalibrationParametersFromPacket(bufferCalibrationParameters, GYRO_CALIBRATION_RESPONSE);
			
			//XXX-RS-LSM-SensorClass?
			// LSM303DLHC Magnetometer Calibration Parameters
			bufferCalibrationParameters = new byte[infoMemLayoutCast.lengthGeneralCalibrationBytes];
			System.arraycopy(infoMemBytes, infoMemLayoutCast.idxLSM303DLHCMagCalibration, bufferCalibrationParameters, 0 , infoMemLayoutCast.lengthGeneralCalibrationBytes);
			retrieveKinematicCalibrationParametersFromPacket(bufferCalibrationParameters, MAG_CALIBRATION_RESPONSE);

			//XXX-RS-LSM-SensorClass?
			// LSM303DLHC Digital Accel Calibration Parameters
			bufferCalibrationParameters = new byte[infoMemLayoutCast.lengthGeneralCalibrationBytes];
			System.arraycopy(infoMemBytes, infoMemLayoutCast.idxLSM303DLHCAccelCalibration, bufferCalibrationParameters, 0 , infoMemLayoutCast.lengthGeneralCalibrationBytes);
			retrieveKinematicCalibrationParametersFromPacket(bufferCalibrationParameters, LSM303DLHC_ACCEL_CALIBRATION_RESPONSE);

			//TODO: decide what to do
			// BMP180 Pressure Calibration Parameters

//			mDerivedSensors = (long)0;
//			if((infoMemLayoutCast.idxDerivedSensors0>=0)&&(infoMemLayoutCast.idxDerivedSensors1>=0)) { // Check if compatible
//				if((infoMemContents[infoMemLayoutCast.idxDerivedSensors0] != (byte)infoMemLayoutCast.maskDerivedChannelsByte)
//						&&(infoMemContents[infoMemLayoutCast.idxDerivedSensors1] != (byte)infoMemLayoutCast.maskDerivedChannelsByte)){
//					mDerivedSensors = ((long)infoMemContents[infoMemLayoutCast.idxDerivedSensors0] & infoMemLayoutCast.maskDerivedChannelsByte) << infoMemLayoutCast.byteShiftDerivedSensors0;
//					mDerivedSensors |= ((long)infoMemContents[infoMemLayoutCast.idxDerivedSensors1] & infoMemLayoutCast.maskDerivedChannelsByte) << infoMemLayoutCast.byteShiftDerivedSensors1;
//					if(infoMemLayoutCast.idxDerivedSensors2>=0) { // Check if compatible
//						mDerivedSensors |= ((long)infoMemContents[infoMemLayoutCast.idxDerivedSensors2] & infoMemLayoutCast.maskDerivedChannelsByte) << infoMemLayoutCast.byteShiftDerivedSensors2;
//					}
//				}
//			}
			
			mDerivedSensors = (long)0;
			// Check if compatible and not equal to 0xFF
			if((infoMemLayoutCast.idxDerivedSensors0>0) && (infoMemBytes[infoMemLayoutCast.idxDerivedSensors0]!=(byte)infoMemLayoutCast.maskDerivedChannelsByte)
					&& (infoMemLayoutCast.idxDerivedSensors1>0) && (infoMemBytes[infoMemLayoutCast.idxDerivedSensors1]!=(byte)infoMemLayoutCast.maskDerivedChannelsByte)) { 
				
				mDerivedSensors |= ((long)infoMemBytes[infoMemLayoutCast.idxDerivedSensors0] & infoMemLayoutCast.maskDerivedChannelsByte) << infoMemLayoutCast.byteShiftDerivedSensors0;
				mDerivedSensors |= ((long)infoMemBytes[infoMemLayoutCast.idxDerivedSensors1] & infoMemLayoutCast.maskDerivedChannelsByte) << infoMemLayoutCast.byteShiftDerivedSensors1;
				
				// Check if compatible and not equal to 0xFF
				if((infoMemLayoutCast.idxDerivedSensors2>0) && (infoMemBytes[infoMemLayoutCast.idxDerivedSensors2]!=(byte)infoMemLayoutCast.maskDerivedChannelsByte)){ 
					mDerivedSensors |= ((long)infoMemBytes[infoMemLayoutCast.idxDerivedSensors2] & infoMemLayoutCast.maskDerivedChannelsByte) << infoMemLayoutCast.byteShiftDerivedSensors2;
				}
			}

			// InfoMem D - End

//			//SDLog and LogAndStream
//			if(((getFirmwareIdentifier()==FW_ID.LOGANDSTREAM)||(getFirmwareIdentifier()==FW_ID.SDLOG))&&(mInfoMemBytes.length >=384)) {
				
				// InfoMem C - Start - used by SdLog and LogAndStream
				if(getFirmwareIdentifier()==FW_ID.SDLOG) {
					mMPU9150DMP = (infoMemBytes[infoMemLayoutCast.idxConfigSetupByte4] >> infoMemLayoutCast.bitShiftMPU9150DMP) & infoMemLayoutCast.maskMPU9150DMP;
					mMPU9150LPF = (infoMemBytes[infoMemLayoutCast.idxConfigSetupByte4] >> infoMemLayoutCast.bitShiftMPU9150LPF) & infoMemLayoutCast.maskMPU9150LPF;
					mMPU9150MotCalCfg =  (infoMemBytes[infoMemLayoutCast.idxConfigSetupByte4] >> infoMemLayoutCast.bitShiftMPU9150MotCalCfg) & infoMemLayoutCast.maskMPU9150MotCalCfg;
					
					mMPU9150MPLSamplingRate = (infoMemBytes[infoMemLayoutCast.idxConfigSetupByte5] >> infoMemLayoutCast.bitShiftMPU9150MPLSamplingRate) & infoMemLayoutCast.maskMPU9150MPLSamplingRate;
					mMPU9150MagSamplingRate = (infoMemBytes[infoMemLayoutCast.idxConfigSetupByte5] >> infoMemLayoutCast.bitShiftMPU9150MagSamplingRate) & infoMemLayoutCast.maskMPU9150MagSamplingRate;
					
					mEnabledSensors += ((long)infoMemBytes[infoMemLayoutCast.idxSensors3] & 0xFF) << infoMemLayoutCast.bitShiftSensors3;
					mEnabledSensors += ((long)infoMemBytes[infoMemLayoutCast.idxSensors4] & 0xFF) << infoMemLayoutCast.bitShiftSensors4;
					
					mMPLSensorFusion = (infoMemBytes[infoMemLayoutCast.idxConfigSetupByte6] >> infoMemLayoutCast.bitShiftMPLSensorFusion) & infoMemLayoutCast.maskMPLSensorFusion;
					mMPLGyroCalTC = (infoMemBytes[infoMemLayoutCast.idxConfigSetupByte6] >> infoMemLayoutCast.bitShiftMPLGyroCalTC) & infoMemLayoutCast.maskMPLGyroCalTC;
					mMPLVectCompCal = (infoMemBytes[infoMemLayoutCast.idxConfigSetupByte6] >> infoMemLayoutCast.bitShiftMPLVectCompCal) & infoMemLayoutCast.maskMPLVectCompCal;
					mMPLMagDistCal = (infoMemBytes[infoMemLayoutCast.idxConfigSetupByte6] >> infoMemLayoutCast.bitShiftMPLMagDistCal) & infoMemLayoutCast.maskMPLMagDistCal;
					mMPLEnable = (infoMemBytes[infoMemLayoutCast.idxConfigSetupByte6] >> infoMemLayoutCast.bitShiftMPLEnable) & infoMemLayoutCast.maskMPLEnable;
					
					String[] dataType={"i16","i16","i16","i16","i16","i16","i8","i8","i8","i8","i8","i8","i8","i8","i8"};
					//MPL Accel Calibration Parameters
					bufferCalibrationParameters = new byte[infoMemLayoutCast.lengthGeneralCalibrationBytes];
					System.arraycopy(infoMemBytes, infoMemLayoutCast.idxMPLAccelCalibration, bufferCalibrationParameters, 0 , infoMemLayoutCast.lengthGeneralCalibrationBytes);
					int[] formattedPacket = UtilParseData.formatDataPacketReverse(bufferCalibrationParameters,dataType);
					double[] AM=new double[9];
					for (int i=0;i<9;i++) {
						AM[i]=((double)formattedPacket[6+i])/100;
					}
					double[][] alignmentMatrixMPLA = {{AM[0],AM[1],AM[2]},{AM[3],AM[4],AM[5]},{AM[6],AM[7],AM[8]}}; 				
					double[][] sensitivityMatrixMPLA = {{formattedPacket[3],0,0},{0,formattedPacket[4],0},{0,0,formattedPacket[5]}}; 
					double[][] offsetVectorMPLA = {{formattedPacket[0]},{formattedPacket[1]},{formattedPacket[2]}};
					AlignmentMatrixMPLAccel = alignmentMatrixMPLA; 			
					SensitivityMatrixMPLAccel = sensitivityMatrixMPLA; 	
					OffsetVectorMPLAccel = offsetVectorMPLA;
			
					//MPL Mag Calibration Configuration
					bufferCalibrationParameters = new byte[infoMemLayoutCast.lengthGeneralCalibrationBytes];
					System.arraycopy(infoMemBytes, infoMemLayoutCast.idxMPLMagCalibration, bufferCalibrationParameters, 0 , infoMemLayoutCast.lengthGeneralCalibrationBytes);
					formattedPacket = UtilParseData.formatDataPacketReverse(bufferCalibrationParameters,dataType);
					AM=new double[9];
					for (int i=0;i<9;i++) {
						AM[i]=((double)formattedPacket[6+i])/100;
					}
					double[][] alignmentMatrixMPLMag = {{AM[0],AM[1],AM[2]},{AM[3],AM[4],AM[5]},{AM[6],AM[7],AM[8]}}; 				
					double[][] sensitivityMatrixMPLMag = {{formattedPacket[3],0,0},{0,formattedPacket[4],0},{0,0,formattedPacket[5]}}; 
					double[][] offsetVectorMPLMag = {{formattedPacket[0]},{formattedPacket[1]},{formattedPacket[2]}};
					AlignmentMatrixMPLMag = alignmentMatrixMPLMag; 			
					SensitivityMatrixMPLMag = sensitivityMatrixMPLMag; 	
					OffsetVectorMPLMag = offsetVectorMPLMag;
			
					//MPL Gyro Calibration Configuration
					bufferCalibrationParameters = new byte[infoMemLayoutCast.lengthGeneralCalibrationBytes];
					System.arraycopy(infoMemBytes, infoMemLayoutCast.idxMPLGyroCalibration, bufferCalibrationParameters, 0 , infoMemLayoutCast.lengthGeneralCalibrationBytes);
					formattedPacket = UtilParseData.formatDataPacketReverse(bufferCalibrationParameters,dataType);
					AM=new double[9];
					for (int i=0;i<9;i++) {
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
				byte[] shimmerNameBuffer = new byte[infoMemLayoutCast.lengthShimmerName];
				System.arraycopy(infoMemBytes, infoMemLayoutCast.idxSDShimmerName, shimmerNameBuffer, 0 , infoMemLayoutCast.lengthShimmerName);
				for(byte b : shimmerNameBuffer) {
					if(!UtilShimmer.isAsciiPrintable((char)b)) {
						break;
					}
					shimmerName += (char)b;
				}
				
				// Experiment Name
				byte[] experimentNameBuffer = new byte[infoMemLayoutCast.lengthExperimentName];
				System.arraycopy(infoMemBytes, infoMemLayoutCast.idxSDEXPIDName, experimentNameBuffer, 0 , infoMemLayoutCast.lengthExperimentName);
				String experimentName = "";
				for(byte b : experimentNameBuffer) {
					if(!UtilShimmer.isAsciiPrintable((char)b)) {
						break;
					}
					experimentName += (char)b;
				}
				mTrialName = new String(experimentName);
	
				//Configuration Time
				int bitShift = (infoMemLayoutCast.lengthConfigTimeBytes-1) * 8;
				mConfigTime = 0;
				for(int x=0; x<infoMemLayoutCast.lengthConfigTimeBytes; x++ ) {
					mConfigTime += (((long)(infoMemBytes[infoMemLayoutCast.idxSDConfigTime0+x] & 0xFF)) << bitShift);
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

				if(getFirmwareIdentifier()==FW_ID.SDLOG) {
					mTrialId = infoMemBytes[infoMemLayoutCast.idxSDMyTrialID] & 0xFF;
					mTrialNumberOfShimmers = infoMemBytes[infoMemLayoutCast.idxSDNumOfShimmers] & 0xFF;
				}
				
				if((getFirmwareIdentifier()==FW_ID.SDLOG)||(getFirmwareIdentifier()==FW_ID.LOGANDSTREAM)) {
					mButtonStart = (infoMemBytes[infoMemLayoutCast.idxSDExperimentConfig0] >> infoMemLayoutCast.bitShiftButtonStart) & infoMemLayoutCast.maskButtonStart;
					mShowRtcErrorLeds = (infoMemBytes[infoMemLayoutCast.idxSDExperimentConfig0] >> infoMemLayoutCast.bitShiftShowRwcErrorLeds) & infoMemLayoutCast.maskShowRwcErrorLeds;
				}
				
				if(getFirmwareIdentifier()==FW_ID.SDLOG) {
					mSyncWhenLogging = (infoMemBytes[infoMemLayoutCast.idxSDExperimentConfig0] >> infoMemLayoutCast.bitShiftTimeSyncWhenLogging) & infoMemLayoutCast.maskTimeSyncWhenLogging;
					mMasterShimmer = (infoMemBytes[infoMemLayoutCast.idxSDExperimentConfig0] >> infoMemLayoutCast.bitShiftMasterShimmer) & infoMemLayoutCast.maskTimeMasterShimmer;
					mSingleTouch = (infoMemBytes[infoMemLayoutCast.idxSDExperimentConfig1] >> infoMemLayoutCast.bitShiftSingleTouch) & infoMemLayoutCast.maskTimeSingleTouch;
					mTCXO = (infoMemBytes[infoMemLayoutCast.idxSDExperimentConfig1] >> infoMemLayoutCast.bitShiftTCX0) & infoMemLayoutCast.maskTimeTCX0;
					
					mSyncBroadcastInterval = (int)(infoMemBytes[infoMemLayoutCast.idxSDBTInterval] & 0xFF);
					
					// Maximum and Estimated Length in minutes
					mTrialDurationEstimated =  ((int)(infoMemBytes[infoMemLayoutCast.idxEstimatedExpLengthLsb] & 0xFF) + (((int)(infoMemBytes[infoMemLayoutCast.idxEstimatedExpLengthMsb] & 0xFF)) << 8));
					mTrialDurationMaximum =  ((int)(infoMemBytes[infoMemLayoutCast.idxMaxExpLengthLsb] & 0xFF) + (((int)(infoMemBytes[infoMemLayoutCast.idxMaxExpLengthMsb] & 0xFF)) << 8));
				}
					
				byte[] macIdBytes = new byte[infoMemLayoutCast.lengthMacIdBytes];
				System.arraycopy(infoMemBytes, infoMemLayoutCast.idxMacAddress, macIdBytes, 0 , infoMemLayoutCast.lengthMacIdBytes);
				mMacIdFromInfoMem = UtilShimmer.bytesToHexString(macIdBytes);
				

				if(((infoMemBytes[infoMemLayoutCast.idxSDConfigDelayFlag]>>infoMemLayoutCast.bitShiftSDCfgFileWriteFlag)&infoMemLayoutCast.maskSDCfgFileWriteFlag) == infoMemLayoutCast.maskSDCfgFileWriteFlag) {
					mConfigFileCreationFlag = true;
				}
				else {
					mConfigFileCreationFlag = false;
				}
				if(((infoMemBytes[infoMemLayoutCast.idxSDConfigDelayFlag]>>infoMemLayoutCast.bitShiftSDCalibFileWriteFlag)&infoMemLayoutCast.maskSDCalibFileWriteFlag) == infoMemLayoutCast.maskSDCalibFileWriteFlag) {
					mCalibFileCreationFlag = true;
				}
				else {
					mCalibFileCreationFlag = false;
				}

				// InfoMem C - End
					
				if(getFirmwareIdentifier()==FW_ID.SDLOG) {
					// InfoMem B Start -> Slave MAC ID for Multi-Shimmer Syncronisation
					syncNodesList.clear();
					for (int i = 0; i < infoMemLayoutCast.maxNumOfExperimentNodes; i++) {
						System.arraycopy(infoMemBytes, infoMemLayoutCast.idxNode0 + (i*infoMemLayoutCast.lengthMacIdBytes), macIdBytes, 0 , infoMemLayoutCast.lengthMacIdBytes);
						if((Arrays.equals(macIdBytes, infoMemLayoutCast.invalidMacId))||(Arrays.equals(macIdBytes, infoMemLayoutCast.invalidMacId))) {
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
			
			prepareAllAfterConfigRead();
		}
		
		checkAndCorrectShimmerName(shimmerName);
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
	@Override
	public byte[] infoMemByteArrayGenerate(boolean generateForWritingToShimmer) {

		InfoMemLayoutShimmer3 infoMemLayout = new InfoMemLayoutShimmer3(getFirmwareIdentifier(), getFirmwareVersionMajor(), getFirmwareVersionMinor(), getFirmwareVersionInternal());
		
		byte[] infoMemBackup = mInfoMemBytes.clone();
		
		// InfoMem defaults to 0xFF on firmware flash
		mInfoMemBytes = infoMemLayout.createEmptyInfoMemByteArray(mInfoMemBytes.length);
//		mInfoMemBytes = infoMemLayout.createEmptyInfoMemByteArray();
		
		if(getHardwareVersion()==HW_ID.SHIMMER_3){
		
			// If not being generated from scratch then copy across exisiting InfoMem contents
			if(!generateForWritingToShimmer) {
				System.arraycopy(infoMemBackup, 0, mInfoMemBytes, 0, (infoMemBackup.length > mInfoMemBytes.length) ? mInfoMemBytes.length:infoMemBackup.length);
			}	
			
			// InfoMem D - Start - used by BtStream, SdLog and LogAndStream
			// Sampling Rate
			int samplingRate = (int)(32768 / getSamplingRateShimmer());
			mInfoMemBytes[infoMemLayout.idxShimmerSamplingRate] = (byte) (samplingRate & infoMemLayout.maskShimmerSamplingRate); 
			mInfoMemBytes[infoMemLayout.idxShimmerSamplingRate+1] = (byte) ((samplingRate >> 8) & infoMemLayout.maskShimmerSamplingRate); 
	
			//FW not using this feature and BtStream will reject infomem if this isn't set to '1'
			mInfoMemBytes[infoMemLayout.idxBufferSize] = (byte) 1;//(byte) (mBufferSize & mInfoMemLayout.maskBufferSize); 
			
			// Sensors
			//JC: The updateEnabledSensorsFromExgResolution(), seems to be working incorrectly because of the boolean values of mIsExg1_24bitEnabled, so updating this values first 
			checkExgResolutionFromEnabledSensorsVar();
			refreshEnabledSensorsFromSensorMap();
			mInfoMemBytes[infoMemLayout.idxSensors0] = (byte) ((mEnabledSensors >> infoMemLayout.byteShiftSensors0) & infoMemLayout.maskSensors);
			mInfoMemBytes[infoMemLayout.idxSensors1] = (byte) ((mEnabledSensors >> infoMemLayout.byteShiftSensors1) & infoMemLayout.maskSensors);
			mInfoMemBytes[infoMemLayout.idxSensors2] = (byte) ((mEnabledSensors >> infoMemLayout.byteShiftSensors2) & infoMemLayout.maskSensors);
			
			//XXX-RS-LSM-SensorClass?
			// Configuration
			mInfoMemBytes[infoMemLayout.idxConfigSetupByte0] = (byte) ((mLSM303DigitalAccelRate & infoMemLayout.maskLSM303DLHCAccelSamplingRate) << infoMemLayout.bitShiftLSM303DLHCAccelSamplingRate);
			mInfoMemBytes[infoMemLayout.idxConfigSetupByte0] |= (byte) ((mAccelRange & infoMemLayout.maskLSM303DLHCAccelRange) << infoMemLayout.bitShiftLSM303DLHCAccelRange);
			if(mLowPowerAccelWR) {
				mInfoMemBytes[infoMemLayout.idxConfigSetupByte0] |= (infoMemLayout.maskLSM303DLHCAccelLPM << infoMemLayout.bitShiftLSM303DLHCAccelLPM);
			}
			if(mHighResAccelWR) {
				mInfoMemBytes[infoMemLayout.idxConfigSetupByte0] |= (infoMemLayout.maskLSM303DLHCAccelHRM << infoMemLayout.bitShiftLSM303DLHCAccelHRM);
			}
	
			mInfoMemBytes[infoMemLayout.idxConfigSetupByte1] = (byte) ((mMPU9150GyroAccelRate & infoMemLayout.maskMPU9150AccelGyroSamplingRate) << infoMemLayout.bitShiftMPU9150AccelGyroSamplingRate);
	
			//XXX-RS-LSM-SensorClass?
			mInfoMemBytes[infoMemLayout.idxConfigSetupByte2] = (byte) ((mMagRange & infoMemLayout.maskLSM303DLHCMagRange) << infoMemLayout.bitShiftLSM303DLHCMagRange);
			mInfoMemBytes[infoMemLayout.idxConfigSetupByte2] |= (byte) ((mLSM303MagRate & infoMemLayout.maskLSM303DLHCMagSamplingRate) << infoMemLayout.bitShiftLSM303DLHCMagSamplingRate);
			
			mInfoMemBytes[infoMemLayout.idxConfigSetupByte2] |= (byte) ((mGyroRange & infoMemLayout.maskMPU9150GyroRange) << infoMemLayout.bitShiftMPU9150GyroRange);
			
			mInfoMemBytes[infoMemLayout.idxConfigSetupByte3] = (byte) ((mMPU9150AccelRange & infoMemLayout.maskMPU9150AccelRange) << infoMemLayout.bitShiftMPU9150AccelRange);
			mInfoMemBytes[infoMemLayout.idxConfigSetupByte3] |= (byte) ((mPressureResolution & infoMemLayout.maskBMP180PressureResolution) << infoMemLayout.bitShiftBMP180PressureResolution);
			mInfoMemBytes[infoMemLayout.idxConfigSetupByte3] |= (byte) ((mGSRRange & infoMemLayout.maskGSRRange) << infoMemLayout.bitShiftGSRRange);
			mInfoMemBytes[infoMemLayout.idxConfigSetupByte3] |= (byte) ((mInternalExpPower & infoMemLayout.maskEXPPowerEnable) << infoMemLayout.bitShiftEXPPowerEnable);
			
			//EXG Configuration
			exgBytesGetFromConfig(); //update mEXG1Register and mEXG2Register
			System.arraycopy(mEXG1RegisterArray, 0, mInfoMemBytes, infoMemLayout.idxEXGADS1292RChip1Config1, 10);
			System.arraycopy(mEXG2RegisterArray, 0, mInfoMemBytes, infoMemLayout.idxEXGADS1292RChip2Config1, 10);
			
			mInfoMemBytes[infoMemLayout.idxBtCommBaudRate] = (byte) (mBluetoothBaudRate & infoMemLayout.maskBaudRate);
	
			//XXX-RS-AA-SensorClass?
			// Analog Accel Calibration Parameters
			byte[] bufferCalibrationParameters = generateCalParamAnalogAccel();
			System.arraycopy(bufferCalibrationParameters, 0, mInfoMemBytes, infoMemLayout.idxAnalogAccelCalibration, infoMemLayout.lengthGeneralCalibrationBytes);
	
			// MPU9150 Gyroscope Calibration Parameters
			bufferCalibrationParameters = generateCalParamGyroscope();
			System.arraycopy(bufferCalibrationParameters, 0, mInfoMemBytes, infoMemLayout.idxMPU9150GyroCalibration, infoMemLayout.lengthGeneralCalibrationBytes);
			
			//XXX-RS-LSM-SensorClass?
			// LSM303DLHC Magnetometer Calibration Parameters
			bufferCalibrationParameters = generateCalParamLSM303DLHCMag();
			System.arraycopy(bufferCalibrationParameters, 0, mInfoMemBytes, infoMemLayout.idxLSM303DLHCMagCalibration, infoMemLayout.lengthGeneralCalibrationBytes);

			//XXX-RS-LSM-SensorClass?
			// LSM303DLHC Digital Accel Calibration Parameters
			bufferCalibrationParameters = generateCalParamLSM303DLHCAccel();
			System.arraycopy(bufferCalibrationParameters, 0, mInfoMemBytes, infoMemLayout.idxLSM303DLHCAccelCalibration, infoMemLayout.lengthGeneralCalibrationBytes);
			
			//TODO: decide what to do
			// BMP180 Pressure Calibration Parameters
	
			// Derived Sensors
			if((infoMemLayout.idxDerivedSensors0>0)&&(infoMemLayout.idxDerivedSensors1>0)) { // Check if compatible
				mInfoMemBytes[infoMemLayout.idxDerivedSensors0] = (byte) ((mDerivedSensors >> infoMemLayout.byteShiftDerivedSensors0) & infoMemLayout.maskDerivedChannelsByte);
				mInfoMemBytes[infoMemLayout.idxDerivedSensors1] = (byte) ((mDerivedSensors >> infoMemLayout.byteShiftDerivedSensors1) & infoMemLayout.maskDerivedChannelsByte);
				if(infoMemLayout.idxDerivedSensors2>0) { // Check if compatible
					mInfoMemBytes[infoMemLayout.idxDerivedSensors2] = (byte) ((mDerivedSensors >> infoMemLayout.byteShiftDerivedSensors2) & infoMemLayout.maskDerivedChannelsByte);
				}
			}
			
			// InfoMem D - End
	
			
			//TODO: Add full FW version checking here to support future changes to FW
			//SDLog and LogAndStream
	//		if(((getFirmwareIdentifier()==FW_ID.LOGANDSTREAM)||(getFirmwareIdentifier()==FW_ID.SDLOG))&&(mInfoMemBytes.length >=384)) {
	
				// InfoMem C - Start - used by SdLog and LogAndStream
				if(getFirmwareIdentifier()==FW_ID.SDLOG) {
					mInfoMemBytes[infoMemLayout.idxConfigSetupByte4] = (byte) ((mMPU9150DMP & infoMemLayout.maskMPU9150DMP) << infoMemLayout.bitShiftMPU9150DMP);
					mInfoMemBytes[infoMemLayout.idxConfigSetupByte4] |= (byte) ((mMPU9150LPF & infoMemLayout.maskMPU9150LPF) << infoMemLayout.bitShiftMPU9150LPF);
					mInfoMemBytes[infoMemLayout.idxConfigSetupByte4] |= (byte) ((mMPU9150MotCalCfg & infoMemLayout.maskMPU9150MotCalCfg) << infoMemLayout.bitShiftMPU9150MotCalCfg);
	
					mInfoMemBytes[infoMemLayout.idxConfigSetupByte5] = (byte) ((mMPU9150MPLSamplingRate & infoMemLayout.maskMPU9150MPLSamplingRate) << infoMemLayout.bitShiftMPU9150MPLSamplingRate);
					mInfoMemBytes[infoMemLayout.idxConfigSetupByte5] |= (byte) ((mMPU9150MagSamplingRate & infoMemLayout.maskMPU9150MPLSamplingRate) << infoMemLayout.bitShiftMPU9150MagSamplingRate);
	
					mInfoMemBytes[infoMemLayout.idxSensors3] = (byte) ((mEnabledSensors >> infoMemLayout.bitShiftSensors3) & 0xFF);
					mInfoMemBytes[infoMemLayout.idxSensors4] = (byte) ((mEnabledSensors >> infoMemLayout.bitShiftSensors4) & 0xFF);
	
					mInfoMemBytes[infoMemLayout.idxConfigSetupByte6] = (byte) ((mMPLSensorFusion & infoMemLayout.maskMPLSensorFusion) << infoMemLayout.bitShiftMPLSensorFusion);
					mInfoMemBytes[infoMemLayout.idxConfigSetupByte6] |= (byte) ((mMPLGyroCalTC & infoMemLayout.maskMPLGyroCalTC) << infoMemLayout.bitShiftMPLGyroCalTC);
					mInfoMemBytes[infoMemLayout.idxConfigSetupByte6] |= (byte) ((mMPLVectCompCal & infoMemLayout.maskMPLVectCompCal) << infoMemLayout.bitShiftMPLVectCompCal);
					mInfoMemBytes[infoMemLayout.idxConfigSetupByte6] |= (byte) ((mMPLMagDistCal & infoMemLayout.maskMPLMagDistCal) << infoMemLayout.bitShiftMPLMagDistCal);
					mInfoMemBytes[infoMemLayout.idxConfigSetupByte6] |= (byte) ((mMPLEnable & infoMemLayout.maskMPLEnable) << infoMemLayout.bitShiftMPLEnable);
					
					//TODO: decide what to do
					//MPL Accel Calibration Parameters
					//MPL Mag Calibration Configuration
					//MPL Gyro Calibration Configuration
				}
	
				// Shimmer Name
				for (int i = 0; i < infoMemLayout.lengthShimmerName; i++) {
					if (i < mShimmerUserAssignedName.length()) {
						mInfoMemBytes[infoMemLayout.idxSDShimmerName + i] = (byte) mShimmerUserAssignedName.charAt(i);
					}
					else {
						mInfoMemBytes[infoMemLayout.idxSDShimmerName + i] = (byte) 0xFF;
					}
				}
				
				// Experiment Name
				for (int i = 0; i < infoMemLayout.lengthExperimentName; i++) {
					if (i < mTrialName.length()) {
						mInfoMemBytes[infoMemLayout.idxSDEXPIDName + i] = (byte) mTrialName.charAt(i);
					}
					else {
						mInfoMemBytes[infoMemLayout.idxSDEXPIDName + i] = (byte) 0xFF;
					}
				}
	
				//Configuration Time
				mInfoMemBytes[infoMemLayout.idxSDConfigTime0] = (byte) ((mConfigTime >> infoMemLayout.bitShiftSDConfigTime0) & 0xFF);
				mInfoMemBytes[infoMemLayout.idxSDConfigTime1] = (byte) ((mConfigTime >> infoMemLayout.bitShiftSDConfigTime1) & 0xFF);
				mInfoMemBytes[infoMemLayout.idxSDConfigTime2] = (byte) ((mConfigTime >> infoMemLayout.bitShiftSDConfigTime2) & 0xFF);
				mInfoMemBytes[infoMemLayout.idxSDConfigTime3] = (byte) ((mConfigTime >> infoMemLayout.bitShiftSDConfigTime3) & 0xFF);
				
				if(getFirmwareIdentifier()==FW_ID.SDLOG) {
					mInfoMemBytes[infoMemLayout.idxSDMyTrialID] = (byte) (mTrialId & 0xFF);
		
					mInfoMemBytes[infoMemLayout.idxSDNumOfShimmers] = (byte) (mTrialNumberOfShimmers & 0xFF);
				}
				
				if((getFirmwareIdentifier()==FW_ID.SDLOG)||(getFirmwareIdentifier()==FW_ID.LOGANDSTREAM)) {
					mInfoMemBytes[infoMemLayout.idxSDExperimentConfig0] = (byte) ((mButtonStart & infoMemLayout.maskButtonStart) << infoMemLayout.bitShiftButtonStart);
					if(this.isOverrideShowRwcErrorLeds){
						mInfoMemBytes[infoMemLayout.idxSDExperimentConfig0] |= (byte) ((infoMemLayout.maskShowRwcErrorLeds) << infoMemLayout.bitShiftShowRwcErrorLeds);
					}
					else {
						mInfoMemBytes[infoMemLayout.idxSDExperimentConfig0] |= (byte) ((mShowRtcErrorLeds & infoMemLayout.maskShowRwcErrorLeds) << infoMemLayout.bitShiftShowRwcErrorLeds);
					}
				}
				
				if(getFirmwareIdentifier()==FW_ID.SDLOG) {
					mInfoMemBytes[infoMemLayout.idxSDExperimentConfig0] |= (byte) ((mSyncWhenLogging & infoMemLayout.maskTimeSyncWhenLogging) << infoMemLayout.bitShiftTimeSyncWhenLogging);
					mInfoMemBytes[infoMemLayout.idxSDExperimentConfig0] |= (byte) ((mMasterShimmer & infoMemLayout.maskTimeMasterShimmer) << infoMemLayout.bitShiftMasterShimmer);
					
					mInfoMemBytes[infoMemLayout.idxSDExperimentConfig1] = (byte) ((mSingleTouch & infoMemLayout.maskTimeSingleTouch) << infoMemLayout.bitShiftSingleTouch);
					mInfoMemBytes[infoMemLayout.idxSDExperimentConfig1] |= (byte) ((mTCXO & infoMemLayout.maskTimeTCX0) << infoMemLayout.bitShiftTCX0);
				
					mInfoMemBytes[infoMemLayout.idxSDBTInterval] = (byte) (mSyncBroadcastInterval & 0xFF);
				
					// Maximum and Estimated Length in minutes
					mInfoMemBytes[infoMemLayout.idxEstimatedExpLengthLsb] = (byte) ((mTrialDurationEstimated >> 0) & 0xFF);
					mInfoMemBytes[infoMemLayout.idxEstimatedExpLengthMsb] = (byte) ((mTrialDurationEstimated >> 8) & 0xFF);
					mInfoMemBytes[infoMemLayout.idxMaxExpLengthLsb] = (byte) ((mTrialDurationMaximum >> 0) & 0xFF);
					mInfoMemBytes[infoMemLayout.idxMaxExpLengthMsb] = (byte) ((mTrialDurationMaximum >> 8) & 0xFF);
				}
				
				if(((getFirmwareIdentifier()==FW_ID.LOGANDSTREAM)||(getFirmwareIdentifier()==FW_ID.SDLOG))) {
					if(generateForWritingToShimmer) {
						// MAC address - set to all 0xFF (i.e. invalid MAC) so that Firmware will know to check for MAC from Bluetooth transceiver
						// (already set to 0xFF at start of method but just incase)
						System.arraycopy(infoMemLayout.invalidMacId, 0, mInfoMemBytes, infoMemLayout.idxMacAddress, infoMemLayout.lengthMacIdBytes);
		
						mInfoMemBytes[infoMemLayout.idxSDConfigDelayFlag] = 0;
						// Tells the Shimmer to create a new config file on undock/power cycle
						//TODO RM enabled the two lines below (MN had the below two lines commented out.. but need them to write config successfully over UART)
						byte configFileWriteBit = (byte) (mConfigFileCreationFlag? (infoMemLayout.maskSDCfgFileWriteFlag << infoMemLayout.bitShiftSDCfgFileWriteFlag):0x00);
						mInfoMemBytes[infoMemLayout.idxSDConfigDelayFlag] |= configFileWriteBit;
	
						mInfoMemBytes[infoMemLayout.idxSDConfigDelayFlag] |= infoMemLayout.bitShiftSDCfgFileWriteFlag;
	
						 // Tells the Shimmer to create a new calibration files on undock/power cycle
						byte calibFileWriteBit = (byte) (mCalibFileCreationFlag? (infoMemLayout.maskSDCalibFileWriteFlag << infoMemLayout.bitShiftSDCalibFileWriteFlag):0x00);
						mInfoMemBytes[infoMemLayout.idxSDConfigDelayFlag] |= calibFileWriteBit;
					}
				}
				// InfoMem C - End
					
				if(getFirmwareIdentifier()==FW_ID.SDLOG) {
					// InfoMem B Start -> Slave MAC ID for Multi-Shimmer Syncronisation
					for (int i = 0; i < infoMemLayout.maxNumOfExperimentNodes; i++) { // Limit of 21 nodes
						byte[] macIdArray;
						if((syncNodesList.size()>0) && (i<syncNodesList.size()) && (mSyncWhenLogging>0)) {
							macIdArray = UtilShimmer.hexStringToByteArray(syncNodesList.get(i));
						}
						else {
							macIdArray = infoMemLayout.invalidMacId;
						}
						System.arraycopy(macIdArray, 0, mInfoMemBytes, infoMemLayout.idxNode0 + (i*infoMemLayout.lengthMacIdBytes), infoMemLayout.lengthMacIdBytes);
					}
					// InfoMem B End
				}
		}
//		}
		return mInfoMemBytes;
	}
	
	public void setCalibFileCreationFlag(boolean state) {
		mCalibFileCreationFlag = state;
	}
	
	public void setConfigFileCreationFlag(boolean state) {
		mConfigFileCreationFlag = state;
	}
	
//	@Override
//	public void refreshEnabledSensorsFromSensorMap(){
//		if(mSensorMap!=null) {
//			if (getHardwareVersion()==HW_ID.SHIMMER_3 || getHardwareVersion()==HW_ID.SHIMMER_4_SDK) {
//				mEnabledSensors = (long)0;
//				mDerivedSensors = (long)0;
//				sensorMapCheckandCorrectHwDependencies();
//				for (SensorDetails sED : mSensorMap.values()) {
//					if (sED.isEnabled()) {
//						mEnabledSensors |= sED.mSensorDetailsRef.mSensorBitmapIDSDLogHeader;
//
//						if (sED.isDerivedChannel()) {
//							mDerivedSensors |= sED.mDerivedSensorBitmapID;
//						}
//					}
//				}
//				// add in algorithm map compatible with device
//				updateDerivedSensors();
//				
//				updateEnabledSensorsFromExgResolution();
//			}
//		}
//	}
	
	@Override
	public void handleSpecCasesUpdateEnabledSensors() {
		updateEnabledSensorsFromExgResolution();
	}
	
//	public void refreshEnabledSensorsFromSensorMap(){
//		if(mSensorMap!=null) {
//			if (getHardwareVersion() == HW_ID.SHIMMER_3){
//				mEnabledSensors = (long)0;
//				mDerivedSensors = (long)0;
//				sensorMapCheckandCorrectHwDependencies();
//				for(SensorDetails sED:mSensorMap.values()) {
//					if(sED.isEnabled()) {
//						mEnabledSensors |= sED.mSensorDetailsRef.mSensorBitmapIDSDLogHeader;
//						
//						if(sED.isDerivedChannel()){
//							mDerivedSensors |= sED.mDerivedSensorBitmapID;
//						}
//					}
//				}
//				updateEnabledSensorsFromExgResolution();
////				//add in algorithm map compatible with device
////				configDerivedSensor(getListOfSupportedAlgorithmChannels());
//				mDerivedSensors =getDerivedSensors();
//			}
////			interpretDataPacketFormat();
//		}
//	}
	
	/**
	 * Converts the LSM303DLHC Accel calibration variables from Shimmer Object
	 * into a byte array for sending to the Shimmer.
	 * 
	 * @return the bytes array containing the LSM303DLHC Accel calibration
	 */
	//XXX-RS-LSM-SensorClass?
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
	 * Converts the Analog Accel calibration variables from Shimmer Object
	 * into a byte array for sending to the Shimmer.
	 * 
	 * @return the bytes array containing the Analog Accel Calibration
	 */
	//XXX-RS-AA-SensorClass?
	public byte[] generateCalParamAnalogAccel(){
		// Analog Accel Calibration Parameters
		byte[] bufferCalibrationParameters = new byte[21];
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
		return bufferCalibrationParameters;
	}
	
	/**
	 * Converts the MPU9150 Gyroscope calibration variables from Shimmer Object
	 * into a byte array for sending to the Shimmer.
	 * 
	 * @return the bytes array containing the Gyroscope Calibration
	 */
	public byte[] generateCalParamGyroscope(){
		// MPU9150 Gyroscope Calibration Parameters
		byte[] bufferCalibrationParameters = new byte[21];
		// offsetVector -> buffer offset = 0
		for (int i=0; i<3; i++) {
			bufferCalibrationParameters[0+(i*2)] = (byte) ((((int)mOffsetVectorGyroscope[i][0]) >> 8) & 0xFF);
			bufferCalibrationParameters[0+(i*2)+1] = (byte) ((((int)mOffsetVectorGyroscope[i][0]) >> 0) & 0xFF);
		}
		// sensitivityMatrix -> buffer offset = 6
		for (int i=0; i<3; i++) {
			bufferCalibrationParameters[6+(i*2)] = (byte) ((((int)mSensitivityMatrixGyroscope[i][i]*100) >> 8) & 0xFF);
			bufferCalibrationParameters[6+(i*2)+1] = (byte) ((((int)mSensitivityMatrixGyroscope[i][i]*100) >> 0) & 0xFF);
		}
		// alignmentMatrix -> buffer offset = 12
		for (int i=0; i<3; i++) {
			bufferCalibrationParameters[12+(i*3)] = (byte) (((int)(mAlignmentMatrixGyroscope[i][0]*100)) & 0xFF);
			bufferCalibrationParameters[12+(i*3)+1] = (byte) (((int)(mAlignmentMatrixGyroscope[i][1]*100)) & 0xFF);
			bufferCalibrationParameters[12+(i*3)+2] = (byte) (((int)(mAlignmentMatrixGyroscope[i][2]*100)) & 0xFF);
		}
		return bufferCalibrationParameters;
	}
	
	/**
	 * Converts the MPU9150 Gyroscope calibration variables from Shimmer Object
	 * into a byte array for sending to the Shimmer.
	 * 
	 * @return the bytes array containing the Gyroscope Calibration
	 */
	//XXX-RS-LSM-SensorClass?
	public byte[] generateCalParamLSM303DLHCMag(){
		// LSM303DLHC Magnetometer Calibration Parameters
		byte[] bufferCalibrationParameters = new byte[21];
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
	@Override
	public void sensorAndConfigMapsCreate() {
		// Clear all here because they won't necessarily be cleared depending on
		// hardware version
		mSensorMap = new LinkedHashMap<Integer, SensorDetails>();
		mChannelMap = new LinkedHashMap<String, ChannelDetails>();
		mMapOfAlgorithmModules = new LinkedHashMap<String, AbstractAlgorithm>();
		mMapOfAlgorithmGrouping = new TreeMap<Integer, SensorGroupingDetails>();
		mConfigOptionsMapAlgorithms = new HashMap<String, ConfigOptionDetailsAlgorithm>();
		mSensorGroupingMap = new TreeMap<Integer,SensorGroupingDetails>();
		mConfigOptionsMap = new HashMap<String, ConfigOptionDetailsSensor>();

		if (getHardwareVersion() != -1){
			if (getHardwareVersion() == HW_ID.SHIMMER_2R){
				Map<Integer,SensorDetailsRef> sensorMapRef = Configuration.Shimmer2.mSensorMapRef;
				for(Integer key:sensorMapRef.keySet()){
					mSensorMap.put(key, new SensorDetails(false, 0, sensorMapRef.get(key)));
				}
			} 
			else if (getHardwareVersion() == HW_ID.SHIMMER_3) {
				createSensorMapShimmer3();
				
				mChannelMap = Configuration.Shimmer3.mChannelMapRef;
				mSensorGroupingMap.putAll(Configuration.Shimmer3.mSensorGroupingMapRef);
				mConfigOptionsMap.putAll(Configuration.Shimmer3.mConfigOptionsMapRef);
				
				generateMapOfAlgorithmModules();
				generateMapOfAlgorithmConfigOptions();
				generateMapOfAlgorithmGroupingMap();
			}
			else if (getHardwareVersion() == HW_ID.SHIMMER_GQ_BLE) {
				
				Map<Integer,SensorDetailsRef> sensorMapRef = Configuration.ShimmerGqBle.mSensorMapRef;
				for(Integer key:sensorMapRef.keySet()){
					mSensorMap.put(key, new SensorDetails(false, 0, sensorMapRef.get(key)));
				}

				mSensorGroupingMap.putAll(Configuration.ShimmerGqBle.mSensorGroupingMapRef);
				mConfigOptionsMap.putAll(Configuration.ShimmerGqBle.mConfigOptionsMapRef);
			}
		}
		
		//Update ChannelDetails in the mSensorMap
		Iterator<SensorDetails> iterator = mSensorMap.values().iterator();
		while(iterator.hasNext()){
			SensorDetails sensorDetails = iterator.next();
			for(String channelMapKey:sensorDetails.mSensorDetailsRef.mListOfChannelsRef){
				ChannelDetails channelDetails = mChannelMap.get(channelMapKey);
				if(channelDetails!=null){
					sensorDetails.mListOfChannels.add(channelDetails);
				}
			}
		}
		
		handleSpecialCasesAfterSensorMapCreate();
	}
	
	@Override
	protected void handleSpecialCasesAfterSensorMapCreate() {
		
		//Special cases for ExG 24-bit vs. 16-bit
		List<Integer> listOfSensorMapKeys = Arrays.asList(
				Configuration.Shimmer3.SensorMapKey.HOST_ECG,
				Configuration.Shimmer3.SensorMapKey.HOST_EMG,
				Configuration.Shimmer3.SensorMapKey.HOST_EXG_RESPIRATION,
				Configuration.Shimmer3.SensorMapKey.HOST_EXG_CUSTOM,
				Configuration.Shimmer3.SensorMapKey.HOST_EXG_TEST);
		
		for(Integer sensorMapKey:listOfSensorMapKeys){
			SensorDetails sensorDetails = mSensorMap.get(sensorMapKey);
			if(sensorDetails!=null){
				Iterator<ChannelDetails> iterator = sensorDetails.mListOfChannels.iterator();
				while (iterator.hasNext()) {
					ChannelDetails channelDetails = iterator.next();
					String channelName = channelDetails.mObjectClusterName;
//			   		System.out.println("getExGResolution(): " +getExGResolution());
			   		
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
						    iterator.remove();
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
					    iterator.remove();
					}
			   	}
			}
		}
	}

	
	private void createSensorMapShimmer3(){
		mSensorMap = new LinkedHashMap<Integer, SensorDetails>();
		
//		createInfoMemLayoutObjectIfNeeded();
		InfoMemLayout infoMemLayout = getInfoMemLayout();
		if(infoMemLayout!=null){
			InfoMemLayoutShimmer3 infoMemLayoutCast = (InfoMemLayoutShimmer3) infoMemLayout;

			Map<Integer,SensorDetailsRef> sensorMapRef = Configuration.Shimmer3.mSensorMapRef;
			for(Integer key:sensorMapRef.keySet()){
				
				//Special cases for derived sensor bitmap ID
				int derivedChannelBitmapID = 0;
				if(key==Configuration.Shimmer3.SensorMapKey.SHIMMER_RESISTANCE_AMP){
					derivedChannelBitmapID = infoMemLayoutCast.maskDerivedChannelResAmp;
				}
				else if(key==Configuration.Shimmer3.SensorMapKey.HOST_SKIN_TEMPERATURE_PROBE){
					derivedChannelBitmapID = infoMemLayoutCast.maskDerivedChannelSkinTemp;
				}
				else if(key==Configuration.Shimmer3.SensorMapKey.HOST_PPG_A12){
					derivedChannelBitmapID = infoMemLayoutCast.maskDerivedChannelPpg_ADC12ADC13;
				}
				else if(key==Configuration.Shimmer3.SensorMapKey.HOST_PPG_A13){
					derivedChannelBitmapID = infoMemLayoutCast.maskDerivedChannelPpg_ADC12ADC13;
				}
				else if(key==Configuration.Shimmer3.SensorMapKey.HOST_PPG1_A12){
					derivedChannelBitmapID = infoMemLayoutCast.maskDerivedChannelPpg1_ADC12ADC13;
				}
				else if(key==Configuration.Shimmer3.SensorMapKey.HOST_PPG1_A13){
					derivedChannelBitmapID = infoMemLayoutCast.maskDerivedChannelPpg1_ADC12ADC13;
				}
				else if(key==Configuration.Shimmer3.SensorMapKey.HOST_PPG2_A1){
					derivedChannelBitmapID = infoMemLayoutCast.maskDerivedChannelPpg2_ADC1ADC14;
				}
				else if(key==Configuration.Shimmer3.SensorMapKey.HOST_PPG2_A14){
					derivedChannelBitmapID = infoMemLayoutCast.maskDerivedChannelPpg2_ADC1ADC14;
				}
					
				mSensorMap.put(key, new SensorDetails(false, derivedChannelBitmapID, sensorMapRef.get(key)));
			}
			
		}
	}
	
	//TODO 2016-05-18 feed below into sensor map classes
	@Override
	public void checkConfigOptionValues(String stringKey) {
		if(mConfigOptionsMap!=null){
			ConfigOptionDetails configOptions = mConfigOptionsMap.get(stringKey);
			if(configOptions!=null){
				if(getHardwareVersion()==HW_ID.SHIMMER_3){
					//XXX-RS-LSM-SensorClass?
					int nonStandardIndex = -1;
			        if(stringKey.equals(Configuration.Shimmer3.GuiLabelConfig.LSM303DLHC_ACCEL_RATE)) {
			        	if(isLSM303DigitalAccelLPM()) {
							nonStandardIndex = ConfigOptionDetailsSensor.VALUE_INDEXES.LSM303DLHC_ACCEL_RATE.IS_LPM;
			        	}
			        	else {
							nonStandardIndex = ConfigOptionDetailsSensor.VALUE_INDEXES.LSM303DLHC_ACCEL_RATE.NOT_LPM;
		        			// double check that rate is compatible with LPM (8 not compatible so set to higher rate) 
			        		setLSM303DigitalAccelRate(mLSM303DigitalAccelRate);
			        	}
			        	
						if(nonStandardIndex!=-1 && configOptions instanceof ConfigOptionDetailsSensor){
							((ConfigOptionDetailsSensor) configOptions).setIndexOfValuesToUse(nonStandardIndex);
						}

			        }
			        else if(stringKey.equals(Configuration.Shimmer3.GuiLabelConfig.EXG_RESPIRATION_DETECT_PHASE)) {
			        	checkWhichExgRespPhaseValuesToUse();
			        }
			        else if(stringKey.equals(Configuration.Shimmer3.GuiLabelConfig.EXG_REFERENCE_ELECTRODE)) {
			        	checkWhichExgRefElectrodeValuesToUse();
			        }
				}
				else if(getHardwareVersion()==HW_ID.SHIMMER_GQ_BLE){
				}
			}
		}
	}
	

// -------------Copied to Sensor PPG + EXG + ShimmerDevice----------------------------------	
//	/**
//	 * Used to convert from the enabledSensors long variable read from the
//	 * Shimmer to the set enabled status of the relative entries in the Sensor
//	 * Map. Used in Consensys for dynamic GUI generation to configure a Shimmer.
//	 * 
//	 */
//	@Override
//	//TODO tidy the below. Almost exact same in ShimmerDevice and not sure if this needs to be there 
//	public void sensorMapUpdateFromEnabledSensorsVars() {
//
//		checkExgResolutionFromEnabledSensorsVar();
//
//		if(mSensorMap==null){
//			sensorAndConfigMapsCreate();
//		}
//		
//		if(mSensorMap!=null) {
//			if (getHardwareVersion() == HW_ID.SHIMMER_3) {
//				mapLoop:
//				for(Integer sensorMapKey:mSensorMap.keySet()) {
//					if(handleSpecCasesBeforeSensorMapUpdate(sensorMapKey)){
//						continue mapLoop;
//					}
//
//					// Process remaining channels
//					SensorDetails sensorDetails = mSensorMap.get(sensorMapKey);
//					sensorDetails.updateFromEnabledSensorsVars(mEnabledSensors, mDerivedSensors);
//				}
//				
//				// Now that all main sensor channels have been parsed, deal with
//				// sensor channels that have special conditions. E.g. deciding
//				// what type of signal the ExG is configured for or what derived
//				// channel is enabled like whether PPG is on ADC12 or ADC13
//				handleSpecCasesAfterSensorMapUpdate();
//			}
//			else if (getHardwareVersion() == HW_ID.SHIMMER_GQ_BLE) {
//				
//			}
//		}
//		
////		//Debugging
////		printSensorAndParserMaps();
//
//	}
	
	@Override
	public void handleSpecCasesBeforeSensorMapUpdateGeneral() {
		checkExgResolutionFromEnabledSensorsVar();
	}
	
	@Override
	public boolean handleSpecCasesBeforeSensorMapUpdatePerSensor(Integer sensorMapKey) {
		// Skip if ExG channels here -> handle them after for loop.
		if((sensorMapKey==Configuration.Shimmer3.SensorMapKey.HOST_ECG)
				||(sensorMapKey==Configuration.Shimmer3.SensorMapKey.HOST_EMG)
				||(sensorMapKey==Configuration.Shimmer3.SensorMapKey.HOST_EXG_TEST)
				||(sensorMapKey==Configuration.Shimmer3.SensorMapKey.HOST_EXG_CUSTOM)
				||(sensorMapKey==Configuration.Shimmer3.SensorMapKey.HOST_EXG_RESPIRATION)) {
			mSensorMap.get(sensorMapKey).setIsEnabled(false);
			return true;
		}
		// Handle derived sensors based on int adc channels (e.g. PPG vs. A12/A13)
		else if(((sensorMapKey==Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A12)
			||(sensorMapKey==Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A13)
			||(sensorMapKey==Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A1)
			||(sensorMapKey==Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A14))){

			//Check if a derived channel is enabled, if it is ignore disable and skip 
			innerloop:
			for(Integer conflictKey:mSensorMap.get(sensorMapKey).mSensorDetailsRef.mListOfSensorMapKeysConflicting) {
				if(mSensorMap.get(conflictKey).isDerivedChannel()) {
					if((mDerivedSensors&mSensorMap.get(conflictKey).mDerivedSensorBitmapID) == mSensorMap.get(conflictKey).mDerivedSensorBitmapID) {
						mSensorMap.get(sensorMapKey).setIsEnabled(false);
						return true;
					}
				}
			}
		}
//		else if(sensorMapKey == Configuration.Shimmer3.SensorMapKey.TIMESTAMP_SYNC 
////				|| sensorMapKey == Configuration.Shimmer3.SensorMapKey.TIMESTAMP
////				|| sensorMapKey == Configuration.Shimmer3.SensorMapKey.REAL_TIME_CLOCK
//				|| sensorMapKey == Configuration.Shimmer3.SensorMapKey.REAL_TIME_CLOCK_SYNC){
//			mSensorMap.get(sensorMapKey).setIsEnabled(false);
//			skipKey = true;
//		}
		else if(sensorMapKey == Configuration.Shimmer3.SensorMapKey.HOST_SHIMMER_STREAMING_PROPERTIES){
			mSensorMap.get(sensorMapKey).setIsEnabled(true);
			return true;
		}
		return false;
	}
	
	@Override
	public void handleSpecCasesAfterSensorMapUpdateFromEnabledSensors() {
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
		if (mSensorMap.get(Configuration.Shimmer3.SensorMapKey.HOST_PPG_A12)!=null){
			if((mSensorMap.get(Configuration.Shimmer3.SensorMapKey.HOST_PPG_A12).isEnabled())||(mSensorMap.get(Configuration.Shimmer3.SensorMapKey.HOST_PPG_A13).isEnabled())) {
				mSensorMap.get(Configuration.Shimmer3.SensorMapKey.HOST_PPG_DUMMY).setIsEnabled(true);
				if(mSensorMap.get(Configuration.Shimmer3.SensorMapKey.HOST_PPG_A12).isEnabled()) {
					mPpgAdcSelectionGsrBoard = Configuration.Shimmer3.ListOfPpgAdcSelectionConfigValues[1]; // PPG_A12
				}
				else if(mSensorMap.get(Configuration.Shimmer3.SensorMapKey.HOST_PPG_A13).isEnabled()) {
					mPpgAdcSelectionGsrBoard = Configuration.Shimmer3.ListOfPpgAdcSelectionConfigValues[0]; // PPG_A13
	
				}
			}
			else {
				mSensorMap.get(Configuration.Shimmer3.SensorMapKey.HOST_PPG_DUMMY).setIsEnabled(false);
	
			}
		}
		//Used for Shimmer Proto3 Deluxe hardware
		if (mSensorMap.get(Configuration.Shimmer3.SensorMapKey.HOST_PPG1_A12)!=null){
			if((mSensorMap.get(Configuration.Shimmer3.SensorMapKey.HOST_PPG1_A12).isEnabled())||(mSensorMap.get(Configuration.Shimmer3.SensorMapKey.HOST_PPG1_A13).isEnabled())) {
				mSensorMap.get(Configuration.Shimmer3.SensorMapKey.HOST_PPG1_DUMMY).setIsEnabled(true);
				if(mSensorMap.get(Configuration.Shimmer3.SensorMapKey.HOST_PPG1_A12).isEnabled()) {
					mPpg1AdcSelectionProto3DeluxeBoard = Configuration.Shimmer3.ListOfPpg1AdcSelectionConfigValues[1]; // PPG1_A12
				}
				else if(mSensorMap.get(Configuration.Shimmer3.SensorMapKey.HOST_PPG1_A13).isEnabled()) {
					mPpg1AdcSelectionProto3DeluxeBoard = Configuration.Shimmer3.ListOfPpg1AdcSelectionConfigValues[0]; // PPG1_A13
				}
			}
			else {
				mSensorMap.get(Configuration.Shimmer3.SensorMapKey.HOST_PPG1_DUMMY).setIsEnabled(false);
			}
		}
		//Used for Shimmer Proto3 Deluxe hardware
		if (mSensorMap.get(Configuration.Shimmer3.SensorMapKey.HOST_PPG2_A1)!=null){
			if((mSensorMap.get(Configuration.Shimmer3.SensorMapKey.HOST_PPG2_A1).isEnabled())||(mSensorMap.get(Configuration.Shimmer3.SensorMapKey.HOST_PPG2_A14).isEnabled())) {
				mSensorMap.get(Configuration.Shimmer3.SensorMapKey.HOST_PPG2_DUMMY).setIsEnabled(true);
				if(mSensorMap.get(Configuration.Shimmer3.SensorMapKey.HOST_PPG2_A1).isEnabled()) {
					mPpg2AdcSelectionProto3DeluxeBoard = Configuration.Shimmer3.ListOfPpg2AdcSelectionConfigValues[0]; // PPG2_A1
				}
				else if(mSensorMap.get(Configuration.Shimmer3.SensorMapKey.HOST_PPG2_A14).isEnabled()) {
					mPpg2AdcSelectionProto3DeluxeBoard = Configuration.Shimmer3.ListOfPpg2AdcSelectionConfigValues[1]; // PPG2_A14
				}
			}
			else {
				mSensorMap.get(Configuration.Shimmer3.SensorMapKey.HOST_PPG2_DUMMY).setIsEnabled(false);
			}
		}
	}
	
	//TODO 2016-05-18 feed below into sensor map classes
	
// --------------------Implemented in SensorPPG-----------------------	
	public boolean checkIfSensorEnabled(int sensorMapKey){
		if (getHardwareVersion() == HW_ID.SHIMMER_3) {
			//Used for Shimmer GSR hardware
			if(sensorMapKey==Configuration.Shimmer3.SensorMapKey.HOST_PPG_DUMMY){
				if((super.isSensorEnabled(Configuration.Shimmer3.SensorMapKey.HOST_PPG_A12))||(super.isSensorEnabled(Configuration.Shimmer3.SensorMapKey.HOST_PPG_A13)))
					return true;
				else
					return false;
			}
			else if(sensorMapKey==Configuration.Shimmer3.SensorMapKey.HOST_PPG1_DUMMY){
				if((super.isSensorEnabled(Configuration.Shimmer3.SensorMapKey.HOST_PPG1_A12))||(super.isSensorEnabled(Configuration.Shimmer3.SensorMapKey.HOST_PPG1_A13)))
					return true;
				else
					return false;
			}
			else if(sensorMapKey==Configuration.Shimmer3.SensorMapKey.HOST_PPG2_DUMMY){
				if((super.isSensorEnabled(Configuration.Shimmer3.SensorMapKey.HOST_PPG2_A1))||(super.isSensorEnabled(Configuration.Shimmer3.SensorMapKey.HOST_PPG2_A14)))
					return true;
				else
					return false;
			}
		}
		
		return super.isSensorEnabled(sensorMapKey);
	}
// -------------------------------------------------------------------------------------------------------------------	

	
	//TODO 2016-05-18 feed below into sensor map classes
	
//-----------------Copied to PPG Sensor Class-----------------
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
//	@Override
//	public boolean setSensorEnabledState(int sensorMapKey, boolean state) {
//		
//		if(mSensorMap!=null) {
//			SensorDetails sensorDetails = mSensorMap.get(sensorMapKey);
//			
//			if (getHardwareVersion() == HW_ID.SHIMMER_3){
//
//				// Special case for Dummy entries in the Sensor Map
//				if(sensorMapKey == Configuration.Shimmer3.SensorMapKey.HOST_PPG_DUMMY) {
//					SensorDetails sDDummy = mSensorMap.get(sensorMapKey);
//					sDDummy.setIsEnabled(state);
//					if(Configuration.Shimmer3.ListOfPpgAdcSelection[mPpgAdcSelectionGsrBoard].contains("A12")) {
//						sensorMapKey = Configuration.Shimmer3.SensorMapKey.HOST_PPG_A12;
//					}
//					else {
//						sensorMapKey = Configuration.Shimmer3.SensorMapKey.HOST_PPG_A13;
//					}
//				}		
//				else if(sensorMapKey == Configuration.Shimmer3.SensorMapKey.HOST_PPG1_DUMMY) {
//					SensorDetails sDDummy = mSensorMap.get(sensorMapKey);
//					sDDummy.setIsEnabled(state);
//					if(Configuration.Shimmer3.ListOfPpg1AdcSelection[mPpg1AdcSelectionProto3DeluxeBoard].contains("A12")) {
//						sensorMapKey = Configuration.Shimmer3.SensorMapKey.HOST_PPG1_A12;
//					}
//					else {
//						sensorMapKey = Configuration.Shimmer3.SensorMapKey.HOST_PPG1_A13;
//					}
//				}		
//				else if(sensorMapKey == Configuration.Shimmer3.SensorMapKey.HOST_PPG2_DUMMY) {
//					SensorDetails sDDummy = mSensorMap.get(sensorMapKey);
//					sDDummy.setIsEnabled(state);
//					if(Configuration.Shimmer3.ListOfPpg2AdcSelection[mPpg2AdcSelectionProto3DeluxeBoard].contains("A14")) {
//						sensorMapKey = Configuration.Shimmer3.SensorMapKey.HOST_PPG2_A14;
//					}
//					else {
//						sensorMapKey = Configuration.Shimmer3.SensorMapKey.HOST_PPG2_A1;
//					}
//				}		
//				
//				// Automatically handle required channels for each sensor
//				sensorDetails = mSensorMap.get(sensorMapKey);
//				List<Integer> listOfRequiredKeys = sensorDetails.mSensorDetailsRef.mListOfSensorMapKeysRequired;
//				if(listOfRequiredKeys != null && listOfRequiredKeys.size()>0) {
//					for(Integer sensorKey:listOfRequiredKeys) {
//						mSensorMap.get(sensorKey).setIsEnabled(state);
//					}
//				}
//				
//			}
//			else if (getHardwareVersion() == HW_ID.SHIMMER_GQ_BLE) {
//				
//			}
//			
//			//Set sensor state
//			sensorDetails.setIsEnabled(state);
//
//			sensorMapConflictCheckandCorrect(sensorMapKey);
//			setDefaultConfigForSensor(sensorMapKey, sensorDetails.isEnabled());
//
//			// Automatically control internal expansion board power
//			checkIfInternalExpBrdPowerIsNeeded();
//			
//			refreshEnabledSensorsFromSensorMap();
//
//			boolean result = sensorDetails.isEnabled();
//			
//			return (result==state? true:false);
//			
//		}
//		else {
//			return false;
//		}
//	}
	
//	@Override
//	public boolean setSensorEnabledState(int sensorMapKey, boolean state) {
//		if(mSensorMap!=null) {
//			
//			if (getHardwareVersion() == HW_ID.SHIMMER_3){
//				
//				// Special case for Dummy entries in the Sensor Map
//				if(sensorMapKey == Configuration.Shimmer3.SensorMapKey.HOST_PPG_DUMMY) {
//					mSensorMap.get(sensorMapKey).setIsEnabled(state);
//					if(Configuration.Shimmer3.ListOfPpgAdcSelection[mPpgAdcSelectionGsrBoard].contains("A12")) {
//						sensorMapKey = Configuration.Shimmer3.SensorMapKey.HOST_PPG_A12;
//					}
//					else {
//						sensorMapKey = Configuration.Shimmer3.SensorMapKey.HOST_PPG_A13;
//					}
//				}		
//				else if(sensorMapKey == Configuration.Shimmer3.SensorMapKey.HOST_PPG1_DUMMY) {
//					mSensorMap.get(sensorMapKey).setIsEnabled(state);
//					if(Configuration.Shimmer3.ListOfPpg1AdcSelection[mPpg1AdcSelectionProto3DeluxeBoard].contains("A12")) {
//						sensorMapKey = Configuration.Shimmer3.SensorMapKey.HOST_PPG1_A12;
//					}
//					else {
//						sensorMapKey = Configuration.Shimmer3.SensorMapKey.HOST_PPG1_A13;
//					}
//				}		
//				else if(sensorMapKey == Configuration.Shimmer3.SensorMapKey.HOST_PPG2_DUMMY) {
//					mSensorMap.get(sensorMapKey).setIsEnabled(state);
//					if(Configuration.Shimmer3.ListOfPpg2AdcSelection[mPpg2AdcSelectionProto3DeluxeBoard].contains("A14")) {
//						sensorMapKey = Configuration.Shimmer3.SensorMapKey.HOST_PPG2_A14;
//					}
//					else {
//						sensorMapKey = Configuration.Shimmer3.SensorMapKey.HOST_PPG2_A1;
//					}
//				}		
//				
//				// Automatically handle required channels for each sensor
//				if(mSensorMap.get(sensorMapKey).mSensorDetailsRef.mListOfSensorMapKeysRequired != null) {
//					for(Integer i:mSensorMap.get(sensorMapKey).mSensorDetailsRef.mListOfSensorMapKeysRequired) {
//						mSensorMap.get(i).setIsEnabled(state);
//					}
//				}
//				
//			}
//			else if (getHardwareVersion() == HW_ID.SHIMMER_GQ_BLE) {
//				
//			}
//			
//			//Set sensor state
//			mSensorMap.get(sensorMapKey).setIsEnabled(state);
//
//			sensorMapConflictCheckandCorrect(sensorMapKey);
//			setDefaultConfigForSensor(sensorMapKey, mSensorMap.get(sensorMapKey).isEnabled());
//
//			// Automatically control internal expansion board power
//			checkIfInternalExpBrdPowerIsNeeded();
//			
//			refreshEnabledSensorsFromSensorMap();
//
//			printSensorAndParserMaps();
//
//			boolean result = mSensorMap.get(sensorMapKey).isEnabled();
//			return (result==state? true:false);
//		}
//		else {
//			return false;
//		}
//	}
	
	@Override
	public int handleSpecCasesBeforeSetSensorState(int sensorMapKey, boolean state) {
		// Special case for Dummy entries in the Sensor Map
		if(sensorMapKey == Configuration.Shimmer3.SensorMapKey.HOST_PPG_DUMMY) {
			mSensorMap.get(sensorMapKey).setIsEnabled(state);
			if(Configuration.Shimmer3.ListOfPpgAdcSelection[mPpgAdcSelectionGsrBoard].contains("A12")) {
				return Configuration.Shimmer3.SensorMapKey.HOST_PPG_A12;
			}
			else {
				return Configuration.Shimmer3.SensorMapKey.HOST_PPG_A13;
			}
		}		
		else if(sensorMapKey == Configuration.Shimmer3.SensorMapKey.HOST_PPG1_DUMMY) {
			mSensorMap.get(sensorMapKey).setIsEnabled(state);
			if(Configuration.Shimmer3.ListOfPpg1AdcSelection[mPpg1AdcSelectionProto3DeluxeBoard].contains("A12")) {
				return Configuration.Shimmer3.SensorMapKey.HOST_PPG1_A12;
			}
			else {
				return Configuration.Shimmer3.SensorMapKey.HOST_PPG1_A13;
			}
		}		
		else if(sensorMapKey == Configuration.Shimmer3.SensorMapKey.HOST_PPG2_DUMMY) {
			mSensorMap.get(sensorMapKey).setIsEnabled(state);
			if(Configuration.Shimmer3.ListOfPpg2AdcSelection[mPpg2AdcSelectionProto3DeluxeBoard].contains("A14")) {
				return Configuration.Shimmer3.SensorMapKey.HOST_PPG2_A14;
			}
			else {
				return Configuration.Shimmer3.SensorMapKey.HOST_PPG2_A1;
			}
		}	
		return sensorMapKey;
	}
//--------------------------------------------------------------------------------
	
	//TODO 2016-05-18 feed below into sensor map classes
	/* (non-Javadoc)
	 * @see com.shimmerresearch.driver.ShimmerDevice#checkShimmerConfigBeforeConfiguring()
	 */
	@Override
	public void checkShimmerConfigBeforeConfiguring() {

		if (getHardwareVersion() == HW_ID.SHIMMER_3){

			// If Shimmer name is default, update with MAC ID if available.
			if(mShimmerUserAssignedName.equals(DEFAULT_SHIMMER_NAME)){
				setDefaultShimmerName();
			}

			//XXX-RS-LSM-SensorClass
			if(!isSensorEnabled(Configuration.Shimmer3.SensorMapKey.SHIMMER_LSM303DLHC_ACCEL)) {
				setDefaultLsm303dlhcAccelSensorConfig(false);

				//XXX-RS-LSM-SensorClass
				if(!isSensorEnabled(Configuration.Shimmer3.SensorMapKey.SHIMMER_LSM303DLHC_MAG)) {
					setDefaultLsm303dlhcMagSensorConfig(false);
				}
				//YYY-MPU-Class
				if(!isSensorEnabled(Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_GYRO)){ 
					setDefaultMpu9150GyroSensorConfig(false);
				}
				//YYY-MPU-Class
				if(!isSensorEnabled(Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_ACCEL)){
					setDefaultMpu9150AccelSensorConfig(false);
				}
				//YYY-MPU-Class
				if(!isSensorEnabled(Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_MAG)){
					setMPU9150MagRateFromFreq(getSamplingRateShimmer());
				}
				//YYY-BMP-Class
				if(!isSensorEnabled(Configuration.Shimmer3.SensorMapKey.SHIMMER_BMP180_PRESSURE)) {
					setDefaultBmp180PressureSensorConfig(false);
				}
				//YYY-GSR-Class
				if(!isSensorEnabled(Configuration.Shimmer3.SensorMapKey.SHIMMER_GSR)) {
					setDefaultGsrSensorConfig(false);
				}
				//YYY-EXG-Class
				if((!isSensorEnabled(Configuration.Shimmer3.SensorMapKey.HOST_ECG))
						&&(!isSensorEnabled(Configuration.Shimmer3.SensorMapKey.HOST_EXG_TEST))
						&&(!isSensorEnabled(Configuration.Shimmer3.SensorMapKey.HOST_EMG))
						&&(!isSensorEnabled(Configuration.Shimmer3.SensorMapKey.HOST_EXG_CUSTOM))
						&&(!isSensorEnabled(Configuration.Shimmer3.SensorMapKey.HOST_EXG_RESPIRATION)) ){
					//					&&(!isSensorEnabled(Configuration.Shimmer3.SensorMapKey.EXG1_16BIT))
					//					&&(!isSensorEnabled(Configuration.Shimmer3.SensorMapKey.EXG2_16BIT))
					//					&&(!isSensorEnabled(Configuration.Shimmer3.SensorMapKey.EXG1_24BIT))
					//					&&(!isSensorEnabled(Configuration.Shimmer3.SensorMapKey.EXG2_24BIT))) {
					//				if(!checkIfOtherExgChannelEnabled()) {
					setDefaultECGConfiguration(getSamplingRateShimmer());
					//				}
				}
				if(!checkIfAnyMplChannelEnabled()) {
					setDefaultMpu9150MplSensorConfig(false);
				}

				checkIfInternalExpBrdPowerIsNeeded();
				//			checkIfMPLandDMPIsNeeded();
			}
		}
		
	}

	//TODO 2016-05-18 feed below into sensor map classes
	
// --------------Copied to PPG sensor class----------------------
	
	/**Automatically control internal expansion board power based on sensor map
	 */
	@Override
	protected void checkIfInternalExpBrdPowerIsNeeded(){

		if (getHardwareVersion() == HW_ID.SHIMMER_3){
			for(SensorDetails sensorDetails:mSensorMap.values()) {
				if(sensorDetails.isInternalExpBrdPowerRequired()){
					mInternalExpPower = 1;
					break;
				}
				else {
					// Exception for Int ADC sensors 
					//TODO need to check HW version??
					if(isSensorEnabled(Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A1)
						||isSensorEnabled(Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A12)
						||isSensorEnabled(Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A13)
						||isSensorEnabled(Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A14)){
						
					}
					else {
						mInternalExpPower = 0;
					}
				}
			}
		}
	}
	
// --------------------------------------------------------------------------------------	
	
	//TODO 2016-05-18 feed below into sensor map classes
	//TODO set defaults when ").setIsEnabled(false))" is set manually in the code
	@Override
	protected void setDefaultConfigForSensor(int sensorMapKey, boolean state) {
	//RS (30/5/2016) - fed into SensorLSM303
		if(sensorMapKey==Configuration.Shimmer3.SensorMapKey.SHIMMER_LSM303DLHC_ACCEL) {//XXX-RS-LSM-SensorClass?
			setDefaultLsm303dlhcAccelSensorConfig(state);
		}
		else if(sensorMapKey==Configuration.Shimmer3.SensorMapKey.SHIMMER_LSM303DLHC_MAG) {//XXX-RS-LSM-SensorClass?
			setDefaultLsm303dlhcMagSensorConfig(state);
		}
		
	//RS (30/5/2016) - fed into SensorMPU9X50
		else if(sensorMapKey == Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_GYRO){
			setDefaultMpu9150GyroSensorConfig(state);
		}
		else if(sensorMapKey == Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_ACCEL){
			setDefaultMpu9150AccelSensorConfig(state);
		}
		else if(sensorMapKey == Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_MAG){
			setMPU9150MagRateFromFreq(getSamplingRateShimmer());
		}
		
		else if(sensorMapKey==Configuration.Shimmer3.SensorMapKey.SHIMMER_BMP180_PRESSURE) {
			setDefaultBmp180PressureSensorConfig(state);
		}
		else if(sensorMapKey==Configuration.Shimmer3.SensorMapKey.SHIMMER_GSR) {
			setDefaultGsrSensorConfig(state);
		}
		//RS (30/5/2016) - fed into SensorEXG
		else if((sensorMapKey == Configuration.Shimmer3.SensorMapKey.HOST_EXG_RESPIRATION)
				|| (sensorMapKey == Configuration.Shimmer3.SensorMapKey.HOST_ECG)
				|| (sensorMapKey == Configuration.Shimmer3.SensorMapKey.HOST_EMG)
				|| (sensorMapKey == Configuration.Shimmer3.SensorMapKey.HOST_EXG_TEST)
				|| (sensorMapKey == Configuration.Shimmer3.SensorMapKey.HOST_EXG_CUSTOM)) {
			
			// If ExG sensor enabled, set default settings for that
			// sensor. Otherwise set the default ECG configuration.
			if(state) { 
				if(sensorMapKey == Configuration.Shimmer3.SensorMapKey.HOST_EXG_RESPIRATION) {
					setDefaultRespirationConfiguration(getSamplingRateShimmer());
				}
				else if(sensorMapKey == Configuration.Shimmer3.SensorMapKey.HOST_ECG) {
//					System.err.println("SET ECG CHANNEL");
					setDefaultECGConfiguration(getSamplingRateShimmer());
				}
				else if(sensorMapKey == Configuration.Shimmer3.SensorMapKey.HOST_EMG) {
					setDefaultEMGConfiguration(getSamplingRateShimmer());
				}
				else if(sensorMapKey == Configuration.Shimmer3.SensorMapKey.HOST_EXG_TEST) {
					setEXGTestSignal(getSamplingRateShimmer());
				}
				else if(sensorMapKey == Configuration.Shimmer3.SensorMapKey.HOST_EXG_CUSTOM) {
					setEXGCustom(getSamplingRateShimmer());
				}
			}
			else {
				if(!SensorEXG.checkIfOtherExgChannelEnabled(mSensorMap)) {
//					System.err.println("CLEAR EXG CHANNEL");
					clearExgConfig();
				}
			}
		}
	//RS (30/5/2016) - fed into SensorMPU9X50
		else if(SensorMPU9X50.mListOfMplChannels.contains(sensorMapKey)){
//		else if(mListOfMplChannels.contains(sensorMapKey)){
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
	
	private void setDefaultGsrSensorConfig(boolean state) {
		if(state) {
		}
		else {
			mGSRRange=4;
		}
	}

	/**
	 * @return the mBufferSize
	 */
	public int getBufferSize() {
		return mBufferSize;
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

	public int getLowPowerGyroEnabled() {
		if(mLowPowerGyro)
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
		if(getHardwareVersion()!=HW_ID.SHIMMER_3){
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
	
	public int getShimmerVersion(){
		return getHardwareVersion();
	}

    //region --------- IS+something FUNCTIONS --------- 
    
	/* (non-Javadoc)
	 * @see com.shimmerresearch.driver.ShimmerDevice#isSensorUsingDefaultCal(int)
	 */
	@Override
	public boolean isSensorUsingDefaultCal(int sensorMapKey) {
		if(sensorMapKey==Configuration.Shimmer3.SensorMapKey.SHIMMER_ANALOG_ACCEL){
			return isUsingDefaultLNAccelParam();
		}
		else if(sensorMapKey==Configuration.Shimmer3.SensorMapKey.SHIMMER_LSM303DLHC_ACCEL){
			return isUsingDefaultWRAccelParam();
		}
		else if(sensorMapKey==Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_GYRO){
			return isUsingDefaultGyroParam();
		}
		else if(sensorMapKey==Configuration.Shimmer3.SensorMapKey.SHIMMER_LSM303DLHC_MAG){
			return isUsingDefaultMagParam();
		}
		return false;
	}
	
    public boolean isGyroOnTheFlyCalEnabled(){
		return mEnableOntheFlyGyroOVCal;
	}

	public boolean isLowPowerGyroEnabled() {
		return mLowPowerGyro;
	}
	
	public boolean isUsingDefaultGyroParam(){
		return mDefaultCalibrationParametersGyro;
	}
	
	public boolean isUsingValidGyroParam(){
		if(!isAllZeros(getAlignmentMatrixGyro()) && !isAllZeros(getSensitivityMatrixGyro())){
			return true;
		}else{
			return false;
		}
	}
	

	public boolean isUsingDefaultECGParam(){
		return mDefaultCalibrationParametersECG;
	}
	
	public boolean isUsingDefaultEMGParam(){
		return mDefaultCalibrationParametersEMG;
	}
	
    
	/**
	 * @return the mChannelMap
	 */
	public Map<String, ChannelDetails> getChannelMap() {
		return mChannelMap;
	}
	
	public double getPressTempAC1(){
		return pressTempAC1;
	}
	
	public double getPressTempAC2(){
		return pressTempAC2;
	}
	
	public double getPressTempAC3(){
		return pressTempAC3;
	}
	
	public double getPressTempAC4(){
		return pressTempAC4;
	}
	
	public double getPressTempAC5(){
		return pressTempAC5;
	}
	
	public double getPressTempAC6(){
		return pressTempAC6;
	}
	
	public double getPressTempB1(){
		return pressTempB1;
	}
	
	public double getPressTempB2(){
		return pressTempB2;
	}
	
	public double getPressTempMB(){
		return pressTempMB;
	}
	
	public double getPressTempMC(){
		return pressTempMC;
	}
	
	public double getPressTempMD(){
		return pressTempMD;
	}


	public boolean isUsingConfigFromInfoMem() {
		return mShimmerUsingConfigFromInfoMem;
	}

	/**
	 * @return the mMacIdFromBtParsed
	 */
	public String getMacIdFromBtParsed() {
		if(this.mMyBluetoothAddress.length()>=12) {
			return this.mMyBluetoothAddress.substring(8, 12);
		}
		return "0000";
	}
	
	/**
	 * @return the MAC address from any source available
	 */
	@Override
	public String getMacId() {
		if(!mMacIdFromUart.isEmpty()){
			return mMacIdFromUart; 
		}
		else {
			if(!mMacIdFromInfoMem.isEmpty()){
				return mMacIdFromInfoMem; 
			}
			else {
				if(!mMacIdFromInfoMem.isEmpty()){
					return mMacIdFromInfoMem; 
				}
				else {
					return mMyBluetoothAddress; 
				}
			}
		}
	}	
// YYY - Implemented in SensorBattVoltage
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

	public long getInitialTimeStamp(){
		return mInitialTimeStamp;
	}

	public void setPressureResolution(int i){
		mPressureResolution = i;
	}
	
	public void setGSRRange(int i){
		mGSRRange = i;
	}
	
	/**
	 * @return the mShimmerInfoMemBytes generated from an empty byte array. This is called to generate the InfoMem bytes for writing to the Shimmer.
	 */
	protected byte[] generateShimmerInfoMemBytes() {
//		System.out.println("SlotDetails:" + this.mUniqueIdentifier + " " + mShimmerInfoMemBytes[3]);
		return infoMemByteArrayGenerate(true);
	}
	
	
//	/**
//	 * @param mConfigTime the mConfigTime to set
//	 */
//	public void setConfigTime(long mConfigTime) {
//		this.mConfigTime = mConfigTime;
//	}

	
	/**
	 * @param mBufferSize the mBufferSize to set
	 */
	protected void setBufferSize(int mBufferSize) {
		this.mBufferSize = mBufferSize;
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
	
//	/**
//	 * @param state the mInternalExpPower state to set
//	 */
//	protected void setInternalExpPower(int state) {
//		this.mInternalExpPower = state;
//	}
//	
//	/**
//	 * @return the mInternalExpPower
//	 */
//	public boolean isInternalExpPower() {
//		if(mInternalExpPower > 0)
//			return true;
//		else
//			return false;
//	}
//	
//	/**
//	 * @param state the mInternalExpPower state to set
//	 */
//	protected void setInternalExpPower(boolean state) {
//		if(state) 
//			mInternalExpPower = 0x01;
//		else 
//			mInternalExpPower = 0x00;
//	}
//	
//	public int getInternalExpPower(){
//		return mInternalExpPower;
//	}

	
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
	public void setBluetoothBaudRate(int mBluetoothBaudRate) {
		this.mBluetoothBaudRate = mBluetoothBaudRate;
	}

	/**
	 * @param myBluetoothAddress the myBluetoothAddress to set
	 */
	protected void setMacIdFromBt(String myBluetoothAddress){
		this.mMyBluetoothAddress = myBluetoothAddress;
	}
	
	@Override
	public TreeMap<Integer, TreeMap<Integer, CalibDetailsKinematic>> getMapOfKinematicSensorCalibration(){
		TreeMap<Integer, TreeMap<Integer, CalibDetailsKinematic>> mapOfKinematicSensorCalibration = new TreeMap<Integer, TreeMap<Integer, CalibDetailsKinematic>>();
		mapOfKinematicSensorCalibration.put(Shimmer3.SensorMapKey.SHIMMER_ANALOG_ACCEL, mCalibAccelAnalog);
		mapOfKinematicSensorCalibration.put(Shimmer3.SensorMapKey.SHIMMER_MPU9150_GYRO, mCalibGyro);
		mapOfKinematicSensorCalibration.put(Shimmer3.SensorMapKey.SHIMMER_LSM303DLHC_ACCEL, mCalibAccelWideRange);
		mapOfKinematicSensorCalibration.put(Shimmer3.SensorMapKey.SHIMMER_LSM303DLHC_MAG, mCalibMag);
		return mapOfKinematicSensorCalibration;
	}

	//-------------------- PPG Start -----------------------------------

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
	 * @param ppgAdcSelectionGsrBoard the mPpgAdcSelectionGsrBoard to set
	 */
	protected void setPpgAdcSelectionGsrBoard(int ppgAdcSelectionGsrBoard) {
		this.mPpgAdcSelectionGsrBoard = ppgAdcSelectionGsrBoard;
		int key = Configuration.Shimmer3.SensorMapKey.HOST_PPG_DUMMY;
		this.setSensorEnabledState(key, mSensorMap.get(key).isEnabled());
	}

	/**
	 * @param ppg1AdcSelectionProto3DeluxeBoard the mPpg1AdcSelectionProto3DeluxeBoard to set
	 */
	protected void setPpg1AdcSelectionProto3DeluxeBoard(int ppg1AdcSelectionProto3DeluxeBoard) {
		this.mPpg1AdcSelectionProto3DeluxeBoard = ppg1AdcSelectionProto3DeluxeBoard;
		int key = Configuration.Shimmer3.SensorMapKey.HOST_PPG1_DUMMY;
		this.setSensorEnabledState(key, mSensorMap.get(key).isEnabled());
	}

	/**
	 * @param ppg2AdcSelectionProto3DeluxeBoard the mPpg2AdcSelectionProto3DeluxeBoard to set
	 */
	protected void setPpg2AdcSelectionProto3DeluxeBoard(int ppg2AdcSelectionProto3DeluxeBoard) {
		this.mPpg2AdcSelectionProto3DeluxeBoard = ppg2AdcSelectionProto3DeluxeBoard;
		int key = Configuration.Shimmer3.SensorMapKey.HOST_PPG2_DUMMY;
		this.setSensorEnabledState(key, mSensorMap.get(key).isEnabled());
	}
	
	/** ShimmerGQBle
	 * @param mSamplingDividerPpg the mSamplingDividerPpg to set
	 */
	public void setSamplingDividerPpg(int mSamplingDividerPpg) {
		this.mSamplingDividerPpg = mSamplingDividerPpg;
	}

	/**
	 * @return the mSamplingDividerPpg
	 */
	public int getSamplingDividerPpg() {
		return mSamplingDividerPpg;
	}


	//-------------------- PPG End -----------------------------------

	//-------------------- ExG Start -----------------------------------
	
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
			mEXG1CH1GainValue = SensorEXG.convertEXGGainSettingToValue(mEXG1CH1GainSetting);
			mEXG1CH2GainSetting = (mEXG1RegisterArray[4] >> 4) & 7;
			mEXG1CH2GainValue = SensorEXG.convertEXGGainSettingToValue(mEXG1CH2GainSetting);
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
			mEXG2CH1GainValue = SensorEXG.convertEXGGainSettingToValue(mEXG2CH1GainSetting);
			mEXG2CH2GainSetting = (mEXG2RegisterArray[4] >> 4) & 7;
			mEXG2CH2GainValue = SensorEXG.convertEXGGainSettingToValue(mEXG2CH2GainSetting);
			mEXG2LeadOffCurrentMode = mEXG2RegisterArray[2] & 1;
			mEXG2Comparators = mEXG2RegisterArray[1] & 0x40;
			mEXG2LeadOffSenseSelection = mEXG2RegisterArray[6] & 0x0f; //2P
			
			mEXG2RespirationDetectState = (mEXG2RegisterArray[8] >> 6) & 0x03;
			mEXG2RespirationDetectPhase = (mEXG2RegisterArray[8] >> 2) & 0x0F;
			mEXG2RespirationDetectFreq = (mEXG2RegisterArray[9] >> 2) & 0x01;
			
			mExGConfigBytesDetails.updateFromRegisterArray(EXG_CHIP_INDEX.CHIP2, mEXG2RegisterArray);
		}
		
	}

	public void exgBytesGetConfigFrom(byte[] eXG1RegisterArray, byte[] eXG2RegisterArray){
		exgBytesGetConfigFrom(1, eXG1RegisterArray);
		exgBytesGetConfigFrom(2, eXG2RegisterArray);
		internalCheckExgModeAndUpdateSensorMap();
	}
	
	//TODO:2015-06-16 remove the need for this by using map
	/**
	 * Generates the ExG configuration byte arrays based on the individual ExG
	 * related variables stored in ShimmerObject. The resulting arrays are
	 * stored in the global variables mEXG1RegisterArray and mEXG2RegisterArray.
	 * 
	 */
	public void exgBytesGetFromConfig() {
		mEXG1RegisterArray = mExGConfigBytesDetails.getEXG1RegisterArray();
		mEXG2RegisterArray = mExGConfigBytesDetails.getEXG2RegisterArray();
		exgBytesGetConfigFrom(mEXG1RegisterArray, mEXG2RegisterArray);
	}
	
	/**
	 * This can only be used for Shimmer3 devices (EXG) When a enable
	 * configuration is load, the advanced exg configuration is removed, so it
	 * needs to be set again
	 * 
	 */
	 public void setDefaultECGConfiguration(double shimmerSamplingRate) {
		 if (getHardwareVersion()==HW_ID.SHIMMER_3){
//			mEXG1RegisterArray = new byte[]{(byte) 2,(byte) 160,(byte) 16,(byte) 64,(byte) 64,(byte) 45,(byte) 0,(byte) 0,(byte) 2,(byte) 3};
//			mEXG2RegisterArray = new byte[]{(byte) 2,(byte) 160,(byte) 16,(byte) 64,(byte) 71,(byte) 0,(byte) 0,(byte) 0,(byte) 2,(byte) 1};

			clearExgConfig();
			setExgChannelBitsPerMode(Configuration.Shimmer3.SensorMapKey.HOST_ECG);
			
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.REG1.CONVERSION_MODES.CONTINUOUS);
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.REG2.REFERENCE_BUFFER.ON);
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.REG2.VOLTAGE_REFERENCE.VREF_2_42V);

			setExgPropertyBothChips(EXG_SETTING_OPTIONS.REG4.CH1_PGA_GAIN.GAIN_4);
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.REG5.CH2_PGA_GAIN.GAIN_4);

			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP2,EXG_SETTING_OPTIONS.REG5.CH2_INPUT_SELECTION.RLDIN_CONNECTED_TO_NEG_INPUT);
			
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1,EXG_SETTING_OPTIONS.REG6.RLD_BUFFER_POWER.ENABLED);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1,EXG_SETTING_OPTIONS.REG6.RLD_NEG_INPUTS_CH2.RLD_CONNECTED_TO_IN2N);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1,EXG_SETTING_OPTIONS.REG6.RLD_POS_INPUTS_CH2.RLD_CONNECTED_TO_IN2P);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1,EXG_SETTING_OPTIONS.REG6.RLD_POS_INPUTS_CH1.RLD_CONNECTED_TO_IN1P);
			
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1,EXG_SETTING_OPTIONS.REG10.RLD_REFERENCE_SIGNAL.HALF_OF_SUPPLY);

			setExGRateFromFreq(shimmerSamplingRate);
			exgBytesGetConfigFrom(mEXG1RegisterArray, mEXG2RegisterArray);
		 }
	}

	/**
	 * This can only be used for Shimmer3 devices (EXG) When a enable
	 * configuration is load, the advanced exg configuration is removed, so it
	 * needs to be set again
	 */
	 protected void setDefaultEMGConfiguration(double shimmerSamplingRate){
		if (getHardwareVersion()==HW_ID.SHIMMER_3){
//			mEXG1RegisterArray = new byte[]{(byte) 2,(byte) 160,(byte) 16,(byte) 105,(byte) 96,(byte) 32,(byte) 0,(byte) 0,(byte) 2,(byte) 3};
//			mEXG2RegisterArray = new byte[]{(byte) 2,(byte) 160,(byte) 16,(byte) 129,(byte) 129,(byte) 0,(byte) 0,(byte) 0,(byte) 2,(byte) 1};
			
			clearExgConfig();
			setExgChannelBitsPerMode(Configuration.Shimmer3.SensorMapKey.HOST_EMG);
			
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.REG1.CONVERSION_MODES.CONTINUOUS);
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.REG2.REFERENCE_BUFFER.ON);
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.REG2.VOLTAGE_REFERENCE.VREF_2_42V);
			
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1,EXG_SETTING_OPTIONS.REG4.CH1_PGA_GAIN.GAIN_12);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1,EXG_SETTING_OPTIONS.REG5.CH2_PGA_GAIN.GAIN_12);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1,EXG_SETTING_OPTIONS.REG4.CH1_INPUT_SELECTION.ROUTE_CH3_TO_CH1);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1,EXG_SETTING_OPTIONS.REG5.CH2_INPUT_SELECTION.NORMAL);
			
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP2,EXG_SETTING_OPTIONS.REG4.CH1_POWER_DOWN.POWER_DOWN);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP2,EXG_SETTING_OPTIONS.REG4.CH1_INPUT_SELECTION.SHORTED);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP2,EXG_SETTING_OPTIONS.REG5.CH2_POWER_DOWN.POWER_DOWN);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP2,EXG_SETTING_OPTIONS.REG5.CH2_INPUT_SELECTION.SHORTED);
			
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1,EXG_SETTING_OPTIONS.REG6.RLD_BUFFER_POWER.ENABLED);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1,EXG_SETTING_OPTIONS.REG10.RLD_REFERENCE_SIGNAL.HALF_OF_SUPPLY);
			
			setExGRateFromFreq(shimmerSamplingRate);
			exgBytesGetConfigFrom(mEXG1RegisterArray, mEXG2RegisterArray);
		}
		
	}

	/**
	 * This can only be used for Shimmer3 devices (EXG). Enables the test signal
	 * (square wave) of both EXG chips, to use, both EXG1 and EXG2 have to be
	 * enabled
	 */
	 protected void setEXGTestSignal(double shimmerSamplingRate){
		if (getHardwareVersion()==HW_ID.SHIMMER_3){
//			mEXG1RegisterArray = new byte[]{(byte) 2,(byte) 163,(byte) 16,(byte) 5,(byte) 5,(byte) 0,(byte) 0,(byte) 0,(byte) 2,(byte) 1};
//			mEXG2RegisterArray = new byte[]{(byte) 2,(byte) 163,(byte) 16,(byte) 5,(byte) 5,(byte) 0,(byte) 0,(byte) 0,(byte) 2,(byte) 1};
			
			clearExgConfig();
			setExgChannelBitsPerMode(Configuration.Shimmer3.SensorMapKey.HOST_EXG_TEST);
			
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.REG1.CONVERSION_MODES.CONTINUOUS);
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.REG2.REFERENCE_BUFFER.ON);
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.REG2.VOLTAGE_REFERENCE.VREF_2_42V);

			setExgPropertyBothChips(EXG_SETTING_OPTIONS.REG2.TEST_SIGNAL_SELECTION.ON);
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.REG2.TEST_SIGNAL_FREQUENCY.SQUARE_WAVE_1KHZ);

			setExgPropertyBothChips(EXG_SETTING_OPTIONS.REG4.CH1_INPUT_SELECTION.TEST_SIGNAL);
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.REG5.CH2_INPUT_SELECTION.TEST_SIGNAL);
			
			setExGRateFromFreq(shimmerSamplingRate);
			exgBytesGetConfigFrom(mEXG1RegisterArray, mEXG2RegisterArray);
		}
	}
	 
		
	/**
	 * This can only be used for Shimmer3 devices (EXG) When a enable
	 * configuration is load, the advanced exg configuration is removed, so it
	 * needs to be set again
	 * @param shimmerSamplingRate 
	 * 
	 */
	 protected void setDefaultRespirationConfiguration(double shimmerSamplingRate) {
		 if (getHardwareVersion()==HW_ID.SHIMMER_3){
//			mEXG1RegisterArray = new byte[]{(byte) 2,(byte) 160,(byte) 16,(byte) 64,(byte) 64,(byte) 32,(byte) 0,(byte) 0,(byte) 2,(byte) 3};
//			mEXG2RegisterArray = new byte[]{(byte) 2,(byte) 160,(byte) 16,(byte) 64,(byte) 71,(byte) 0,(byte) 0,(byte) 0,(byte) 234,(byte) 1};
			
			clearExgConfig();
			setExgChannelBitsPerMode(Configuration.Shimmer3.SensorMapKey.HOST_EXG_RESPIRATION);

			setExgPropertyBothChips(EXG_SETTING_OPTIONS.REG1.CONVERSION_MODES.CONTINUOUS);
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.REG2.REFERENCE_BUFFER.ON);
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.REG2.VOLTAGE_REFERENCE.VREF_2_42V);

			setExgPropertyBothChips(EXG_SETTING_OPTIONS.REG4.CH1_PGA_GAIN.GAIN_4);
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.REG5.CH2_PGA_GAIN.GAIN_4);

//			setExgPropertySingleChip(CHIP_INDEX.CHIP2,EXG_SETTING_OPTIONS.INPUT_SELECTION_CH2.RLDIN_CONNECTED_TO_NEG_INPUT); //TODO:2015-06 check!!
			
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1,EXG_SETTING_OPTIONS.REG6.RLD_BUFFER_POWER.ENABLED);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1,EXG_SETTING_OPTIONS.REG10.RLD_REFERENCE_SIGNAL.HALF_OF_SUPPLY);

			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP2,EXG_SETTING_OPTIONS.REG9.RESPIRATION_DEMOD_CIRCUITRY.ON);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP2,EXG_SETTING_OPTIONS.REG9.RESPIRATION_MOD_CIRCUITRY.ON);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP2,EXG_SETTING_OPTIONS.REG9.RESPIRATION_PHASE_AT_32KHZ.PHASE_112_5);
			
			setExGRateFromFreq(shimmerSamplingRate);
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
	 */
	 protected void setEXGCustom(double shimmerSamplingRate){
		if (getHardwareVersion()==HW_ID.SHIMMER_3){
//			mEXG1RegisterArray = new byte[]{(byte) 2,(byte) 163,(byte) 16,(byte) 7,(byte) 7,(byte) 0,(byte) 0,(byte) 0,(byte) 2,(byte) 1};
//			mEXG2RegisterArray = new byte[]{(byte) 2,(byte) 163,(byte) 16,(byte) 7,(byte) 7,(byte) 0,(byte) 0,(byte) 0,(byte) 2,(byte) 1};
			
			clearExgConfig();
			setExgChannelBitsPerMode(Configuration.Shimmer3.SensorMapKey.HOST_EXG_CUSTOM);
			
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.REG1.CONVERSION_MODES.CONTINUOUS);
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.REG2.REFERENCE_BUFFER.ON);
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.REG2.VOLTAGE_REFERENCE.VREF_2_42V);

			setExgPropertyBothChips(EXG_SETTING_OPTIONS.REG2.TEST_SIGNAL_SELECTION.ON);
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.REG2.TEST_SIGNAL_FREQUENCY.SQUARE_WAVE_1KHZ);

			setExgPropertyBothChips(EXG_SETTING_OPTIONS.REG4.CH1_INPUT_SELECTION.RLDIN_CONNECTED_TO_NEG_INPUT);
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.REG5.CH2_INPUT_SELECTION.RLDIN_CONNECTED_TO_NEG_INPUT);
			
			setExGRateFromFreq(shimmerSamplingRate);
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
	
	protected void checkExgResolutionFromEnabledSensorsVar(){
		InfoMemLayoutShimmer3 infoMemLayoutCast = (InfoMemLayoutShimmer3)mInfoMemLayout;
		if (infoMemLayoutCast!=null){
			mIsExg1_24bitEnabled = ((mEnabledSensors & infoMemLayoutCast.maskExg1_24bitFlag)>0)? true:false;
			mIsExg2_24bitEnabled = ((mEnabledSensors & infoMemLayoutCast.maskExg2_24bitFlag)>0)? true:false;
			mIsExg1_16bitEnabled = ((mEnabledSensors & infoMemLayoutCast.maskExg1_16bitFlag)>0)? true:false;
			mIsExg2_16bitEnabled = ((mEnabledSensors & infoMemLayoutCast.maskExg2_16bitFlag)>0)? true:false;

			if(mIsExg1_16bitEnabled||mIsExg2_16bitEnabled){
				mExGResolution = 0;
			}
			else if(mIsExg1_24bitEnabled||mIsExg2_24bitEnabled){
				mExGResolution = 1;
			}
		}
	}

	private void updateEnabledSensorsFromExgResolution(){
		InfoMemLayoutShimmer3 infoMemLayoutCast = (InfoMemLayoutShimmer3)getInfoMemLayout();
		long enabledSensors = getEnabledSensors();

		//JC: should this be here -> checkExgResolutionFromEnabledSensorsVar()
		
		enabledSensors &= ~infoMemLayoutCast.maskExg1_24bitFlag;
		enabledSensors |= (mIsExg1_24bitEnabled? infoMemLayoutCast.maskExg1_24bitFlag:0);
		
		enabledSensors &= ~infoMemLayoutCast.maskExg2_24bitFlag;
		enabledSensors |= (mIsExg2_24bitEnabled? infoMemLayoutCast.maskExg2_24bitFlag:0);
		
		enabledSensors &= ~infoMemLayoutCast.maskExg1_16bitFlag;
		enabledSensors |= (mIsExg1_16bitEnabled? infoMemLayoutCast.maskExg1_16bitFlag:0);
		
		enabledSensors &= ~infoMemLayoutCast.maskExg2_16bitFlag;
		enabledSensors |= (mIsExg2_16bitEnabled? infoMemLayoutCast.maskExg2_16bitFlag:0);
		
		setEnabledSensors(enabledSensors);
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
		else if(sensorMapKey==Configuration.Shimmer3.SensorMapKey.HOST_ECG){
			chip1Enabled = true;
			chip2Enabled = true;
		}
		else if(sensorMapKey==Configuration.Shimmer3.SensorMapKey.HOST_EMG){
			chip1Enabled = true;
			chip2Enabled = false;
		}
		else if(sensorMapKey==Configuration.Shimmer3.SensorMapKey.HOST_EXG_RESPIRATION){
			chip1Enabled = true;
			chip2Enabled = true;
		}
		else if(sensorMapKey==Configuration.Shimmer3.SensorMapKey.HOST_EXG_CUSTOM){
			chip1Enabled = true;
			chip2Enabled = true;
		}
		else if(sensorMapKey==Configuration.Shimmer3.SensorMapKey.HOST_EXG_TEST){
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
	
//	private static boolean checkIfOtherExgChannelEnabled(Map<Integer,SensorDetails> sensorEnabledMap) {
//		for(Integer sensorMapKey:sensorEnabledMap.keySet()) {
//			if(sensorEnabledMap.get(sensorMapKey).isEnabled()) {
//				if((sensorMapKey==Configuration.Shimmer3.SensorMapKey.HOST_ECG)
//					||(sensorMapKey==Configuration.Shimmer3.SensorMapKey.HOST_EMG)
//					||(sensorMapKey==Configuration.Shimmer3.SensorMapKey.HOST_EXG_TEST)
//					||(sensorMapKey==Configuration.Shimmer3.SensorMapKey.HOST_EXG_CUSTOM)
//					||(sensorMapKey==Configuration.Shimmer3.SensorMapKey.HOST_EXG_RESPIRATION) ){
////					||(sensorMapKey==Configuration.Shimmer3.SensorMapKey.EXG1_16BIT)
////					||(sensorMapKey==Configuration.Shimmer3.SensorMapKey.EXG2_16BIT)
////					||(sensorMapKey==Configuration.Shimmer3.SensorMapKey.EXG1_24BIT)
////					||(sensorMapKey==Configuration.Shimmer3.SensorMapKey.EXG2_24BIT)) {
//					return true;
//				}
//			}
//		}
//		return false;
//	}
	
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
	
	/**
	 * @return the mExGResolution
	 */
	public int getExGResolution() {
		//System.out.println("mExGResolution: " +mExGResolution);
		return mExGResolution;
	}
	
	public int getExg1CH1GainValue(){
//		return getExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1, EXG_SETTINGS.REG4_CHANNEL_1_PGA_GAIN);
		return mEXG1CH1GainValue;
	}
	
	public int getExg1CH2GainValue(){
//		return getExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1, EXG_SETTINGS.REG5_CHANNEL_2_PGA_GAIN);
		return mEXG1CH2GainValue;
	}
	
	public int getExg2CH1GainValue(){
//		return getExgPropertySingleChip(EXG_CHIP_INDEX.CHIP2, EXG_SETTINGS.REG4_CHANNEL_1_PGA_GAIN);
		return mEXG2CH1GainValue;
	}
	
	public int getExg2CH2GainValue(){
		return getExgPropertySingleChip(EXG_CHIP_INDEX.CHIP2, EXG_SETTINGS.REG5_CHANNEL_2_PGA_GAIN);
//		return mEXG2CH2GainValue;
	}
	
	public boolean areExgChannelGainsEqual(List<EXG_CHIP_INDEX> listOfChipsToCheck){
		boolean areEqual = true;
		if(listOfChipsToCheck.contains(EXG_CHIP_INDEX.CHIP1)){
			if(getExg1CH1GainValue() != getExg1CH2GainValue()) {
				areEqual = false;
			}
		}

		if(listOfChipsToCheck.contains(EXG_CHIP_INDEX.CHIP2)){
			if(getExg2CH1GainValue() != getExg2CH2GainValue()) {
				areEqual = false;
			}
		}

		if(listOfChipsToCheck.contains(EXG_CHIP_INDEX.CHIP1) && listOfChipsToCheck.contains(EXG_CHIP_INDEX.CHIP2)){
			if(getExg1CH1GainValue() != getExg2CH1GainValue()) {
				areEqual = false;
			}
		}
		return areEqual;
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
//					mSensorMap.get(Configuration.Shimmer3.SensorMapKey.EXG1_24BIT).setIsEnabled(false);
//					mSensorMap.get(Configuration.Shimmer3.SensorMapKey.EXG1_16BIT).setIsEnabled(true);
//				}
//				if(isSensorEnabled(Configuration.Shimmer3.SensorMapKey.EXG2_24BIT)) {
//					mSensorMap.get(Configuration.Shimmer3.SensorMapKey.EXG2_24BIT).setIsEnabled(false);
//					mSensorMap.get(Configuration.Shimmer3.SensorMapKey.EXG2_16BIT).setIsEnabled(true);
//				}
//			}
//			else if(i==1) { // 24-bit
//				if(isSensorEnabled(Configuration.Shimmer3.SensorMapKey.EXG1_16BIT)) {
//					mSensorMap.get(Configuration.Shimmer3.SensorMapKey.EXG1_16BIT).setIsEnabled(false);
//					mSensorMap.get(Configuration.Shimmer3.SensorMapKey.EXG1_24BIT).setIsEnabled(true);
//				}
//				if(isSensorEnabled(Configuration.Shimmer3.SensorMapKey.EXG2_16BIT)) {
//					mSensorMap.get(Configuration.Shimmer3.SensorMapKey.EXG2_16BIT).setIsEnabled(false);
//					mSensorMap.get(Configuration.Shimmer3.SensorMapKey.EXG2_24BIT).setIsEnabled(true);
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
		 if (getHardwareVersion()==HW_ID.SHIMMER_3){
			setDefaultECGConfiguration(getSamplingRateShimmer());
		 }
	}

	/**
	 * This can only be used for Shimmer3 devices (EXG)
	 * When a enable configuration is loaded, the advanced ExG configuration is removed, so it needs to be set again
	 */
	protected void enableDefaultEMGConfiguration(){
		if (getHardwareVersion()==HW_ID.SHIMMER_3){
			setDefaultEMGConfiguration(getSamplingRateShimmer());
		}
	}

	/**
	 * This can only be used for Shimmer3 devices (EXG). Enables the test signal (square wave) of both EXG chips, to use, both EXG1 and EXG2 have to be enabled
	 */
	protected void enableEXGTestSignal(){
		if (getHardwareVersion()==HW_ID.SHIMMER_3){
			setEXGTestSignal(getSamplingRateShimmer());
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
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.REG3.LEAD_OFF_FREQUENCY.DC);
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.REG2.LEAD_OFF_COMPARATORS.OFF);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1, EXG_SETTING_OPTIONS.REG6.RLD_LEAD_OFF_SENSE_FUNCTION.OFF);
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.REG7.LEAD_OFF_DETECT_NEG_INPUTS_CH2.OFF);
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.REG7.LEAD_OFF_DETECT_POS_INPUTS_CH2.OFF);
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.REG7.LEAD_OFF_DETECT_NEG_INPUTS_CH1.OFF);
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.REG7.LEAD_OFF_DETECT_POS_INPUTS_CH1.OFF);
			if(isEXGUsingDefaultEMGConfiguration()){
				setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP2, EXG_SETTING_OPTIONS.REG5.CH2_POWER_DOWN.POWER_DOWN);
			}
		}
		else if(mode==1){//DC Current
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.REG3.LEAD_OFF_FREQUENCY.DC);
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.REG2.LEAD_OFF_COMPARATORS.ON);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1, EXG_SETTING_OPTIONS.REG6.RLD_LEAD_OFF_SENSE_FUNCTION.ON);
			
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1, EXG_SETTING_OPTIONS.REG7.LEAD_OFF_DETECT_NEG_INPUTS_CH2.OFF);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1, EXG_SETTING_OPTIONS.REG7.LEAD_OFF_DETECT_POS_INPUTS_CH2.ON);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1, EXG_SETTING_OPTIONS.REG7.LEAD_OFF_DETECT_NEG_INPUTS_CH1.ON);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1, EXG_SETTING_OPTIONS.REG7.LEAD_OFF_DETECT_POS_INPUTS_CH1.ON);

			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP2, EXG_SETTING_OPTIONS.REG7.LEAD_OFF_DETECT_NEG_INPUTS_CH2.OFF);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP2, EXG_SETTING_OPTIONS.REG7.LEAD_OFF_DETECT_POS_INPUTS_CH2.ON);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP2, EXG_SETTING_OPTIONS.REG7.LEAD_OFF_DETECT_NEG_INPUTS_CH1.OFF);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP2, EXG_SETTING_OPTIONS.REG7.LEAD_OFF_DETECT_POS_INPUTS_CH1.OFF);

			if(isEXGUsingDefaultEMGConfiguration()){
				setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP2, EXG_SETTING_OPTIONS.REG5.CH2_POWER_DOWN.NORMAL_OPERATION);
			}
		}
		else if(mode==2){//AC Current
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.REG3.LEAD_OFF_FREQUENCY.AC);
			setExgPropertyBothChips(EXG_SETTING_OPTIONS.REG2.LEAD_OFF_COMPARATORS.ON);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1, EXG_SETTING_OPTIONS.REG6.RLD_LEAD_OFF_SENSE_FUNCTION.ON);
			
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1, EXG_SETTING_OPTIONS.REG7.LEAD_OFF_DETECT_NEG_INPUTS_CH2.OFF);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1, EXG_SETTING_OPTIONS.REG7.LEAD_OFF_DETECT_POS_INPUTS_CH2.ON);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1, EXG_SETTING_OPTIONS.REG7.LEAD_OFF_DETECT_NEG_INPUTS_CH1.ON);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1, EXG_SETTING_OPTIONS.REG7.LEAD_OFF_DETECT_POS_INPUTS_CH1.ON);

			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP2, EXG_SETTING_OPTIONS.REG7.LEAD_OFF_DETECT_NEG_INPUTS_CH2.OFF);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP2, EXG_SETTING_OPTIONS.REG7.LEAD_OFF_DETECT_POS_INPUTS_CH2.ON);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP2, EXG_SETTING_OPTIONS.REG7.LEAD_OFF_DETECT_NEG_INPUTS_CH1.OFF);
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP2, EXG_SETTING_OPTIONS.REG7.LEAD_OFF_DETECT_POS_INPUTS_CH1.OFF);

			if(isEXGUsingDefaultEMGConfiguration()){
				setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP2, EXG_SETTING_OPTIONS.REG5.CH2_POWER_DOWN.NORMAL_OPERATION);
			}
		}
	}

	protected int getEXGLeadOffCurrentMode(){
		if(isExgPropertyEnabled(EXG_CHIP_INDEX.CHIP1, EXG_SETTING_OPTIONS.REG6.RLD_LEAD_OFF_SENSE_FUNCTION.ON)
				||isExgPropertyEnabled(EXG_CHIP_INDEX.CHIP1, EXG_SETTING_OPTIONS.REG7.LEAD_OFF_DETECT_NEG_INPUTS_CH2.ON)
				||isExgPropertyEnabled(EXG_CHIP_INDEX.CHIP1, EXG_SETTING_OPTIONS.REG7.LEAD_OFF_DETECT_POS_INPUTS_CH2.ON)
				||isExgPropertyEnabled(EXG_CHIP_INDEX.CHIP1, EXG_SETTING_OPTIONS.REG7.LEAD_OFF_DETECT_NEG_INPUTS_CH1.ON)
				||isExgPropertyEnabled(EXG_CHIP_INDEX.CHIP1, EXG_SETTING_OPTIONS.REG7.LEAD_OFF_DETECT_POS_INPUTS_CH1.ON)
				||isExgPropertyEnabled(EXG_CHIP_INDEX.CHIP2, EXG_SETTING_OPTIONS.REG7.LEAD_OFF_DETECT_NEG_INPUTS_CH2.ON)
				||isExgPropertyEnabled(EXG_CHIP_INDEX.CHIP2, EXG_SETTING_OPTIONS.REG7.LEAD_OFF_DETECT_POS_INPUTS_CH2.ON)
				||isExgPropertyEnabled(EXG_CHIP_INDEX.CHIP2, EXG_SETTING_OPTIONS.REG7.LEAD_OFF_DETECT_NEG_INPUTS_CH1.ON)
				||isExgPropertyEnabled(EXG_CHIP_INDEX.CHIP2, EXG_SETTING_OPTIONS.REG7.LEAD_OFF_DETECT_POS_INPUTS_CH1.ON)
				){
			if(isExgPropertyEnabled(EXG_CHIP_INDEX.CHIP1, EXG_SETTING_OPTIONS.REG3.LEAD_OFF_FREQUENCY.DC)){
				return 1;//DC Current
			}
			else if(isExgPropertyEnabled(EXG_CHIP_INDEX.CHIP1, EXG_SETTING_OPTIONS.REG3.LEAD_OFF_FREQUENCY.AC)){
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
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP2, EXG_SETTING_OPTIONS.REG9.RESPIRATION_PHASE_AT_32KHZ.PHASE_112_5);
		}
		else {
			setExgPropertySingleChip(EXG_CHIP_INDEX.CHIP2, EXG_SETTING_OPTIONS.REG9.RESPIRATION_PHASE_AT_64KHZ.PHASE_157_5);
		}
	}

	/**
	 * @param mEXG2RespirationDetectPhase the mEXG2RespirationDetectPhase to set
	 */
	protected void setEXG2RespirationDetectPhase(int mEXG2RespirationDetectPhase) {
		setExgPropertySingleChipValue(EXG_CHIP_INDEX.CHIP2, EXG_SETTINGS.REG9_RESPIRATION_PHASE, mEXG2RespirationDetectPhase);
	}
	
	//Moved to SensorEXG
//	protected static int convertEXGGainSettingToValue(int setting){
//
//		if (setting==0){
//			return 6;
//		} else if (setting==1){
//			return 1;
//		} else if (setting==2){
//			return 2;
//		} else if (setting==3){
//			return 3;
//		} else if (setting==4){
//			return 4;
//		} else if (setting==5){
//			return 8;
//		} else if (setting==6){
//			return 12;
//		}
//		else {
//			return -1; // -1 means invalid value
//		}
//
//	}

	/** TODO USE SENSOR MAPS and isEXGUsingDefaultECGConfiguration() instead */
	@Deprecated 
	public boolean isEXGUsingDefaultECGConfigurationForSDFW(){
		if (getFirmwareIdentifier() == FW_ID.GQ_802154){
			if(((mEXG1RegisterArray[3] & 0x0F)==0)&&((mEXG1RegisterArray[4] & 0x0F)==0)){
				return true;
			}
		}
		else {
//			if((mIsExg1_16bitEnabled&&mIsExg2_16bitEnabled)||(mIsExg1_24bitEnabled&&mIsExg2_24bitEnabled)){
				if(((mEXG1RegisterArray[3] & 0x0F)==0)&&((mEXG1RegisterArray[4] & 0x0F)==0)&& ((mEXG2RegisterArray[3] & 0x0F)==0)&&((mEXG2RegisterArray[4] & 0x0F)==7)){
					return true;
				}
//			}
		}
		return false;

	}

	public boolean isEXGUsingDefaultECGConfiguration(){
		if (getFirmwareIdentifier() == FW_ID.GQ_802154){
			if(((mEXG1RegisterArray[3] & 0x0F)==0)&&((mEXG1RegisterArray[4] & 0x0F)==0)){
				return true;
			}
		}
		else {
			if((mIsExg1_16bitEnabled&&mIsExg2_16bitEnabled)||(mIsExg1_24bitEnabled&&mIsExg2_24bitEnabled)){
				if(((mEXG1RegisterArray[3] & 0x0F)==0)&&((mEXG1RegisterArray[4] & 0x0F)==0)&& ((mEXG2RegisterArray[3] & 0x0F)==0)&&((mEXG2RegisterArray[4] & 0x0F)==7)){
					return true;
				}
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
	
	private void internalCheckExgModeAndUpdateSensorMap(){
		if(mSensorMap!=null){
			if(getHardwareVersion()==HW_ID.SHIMMER_3){
//				if((mIsExg1_24bitEnabled||mIsExg2_24bitEnabled||mIsExg1_16bitEnabled||mIsExg2_16bitEnabled)){
//				if((isSensorEnabled(Configuration.Shimmer3.SensorMapKey.EXG1_16BIT))
//						||(isSensorEnabled(Configuration.Shimmer3.SensorMapKey.EXG2_16BIT))
//						||(isSensorEnabled(Configuration.Shimmer3.SensorMapKey.EXG1_24BIT))
//						||(isSensorEnabled(Configuration.Shimmer3.SensorMapKey.EXG2_24BIT))) {
					if(isEXGUsingDefaultRespirationConfiguration()) { // Do Respiration check first
						mSensorMap.get(Configuration.Shimmer3.SensorMapKey.HOST_ECG).setIsEnabled(false);
						mSensorMap.get(Configuration.Shimmer3.SensorMapKey.HOST_EMG).setIsEnabled(false);
						mSensorMap.get(Configuration.Shimmer3.SensorMapKey.HOST_EXG_TEST).setIsEnabled(false);
						mSensorMap.get(Configuration.Shimmer3.SensorMapKey.HOST_EXG_CUSTOM).setIsEnabled(false);
						mSensorMap.get(Configuration.Shimmer3.SensorMapKey.HOST_EXG_RESPIRATION).setIsEnabled(true);
					}
					else if(isEXGUsingDefaultECGConfiguration()) {
						mSensorMap.get(Configuration.Shimmer3.SensorMapKey.HOST_ECG).setIsEnabled(true);
						mSensorMap.get(Configuration.Shimmer3.SensorMapKey.HOST_EMG).setIsEnabled(false);
						mSensorMap.get(Configuration.Shimmer3.SensorMapKey.HOST_EXG_TEST).setIsEnabled(false);
						mSensorMap.get(Configuration.Shimmer3.SensorMapKey.HOST_EXG_CUSTOM).setIsEnabled(false);
						mSensorMap.get(Configuration.Shimmer3.SensorMapKey.HOST_EXG_RESPIRATION).setIsEnabled(false);
					}
					else if(isEXGUsingDefaultEMGConfiguration()) {
						mSensorMap.get(Configuration.Shimmer3.SensorMapKey.HOST_ECG).setIsEnabled(false);
						mSensorMap.get(Configuration.Shimmer3.SensorMapKey.HOST_EMG).setIsEnabled(true);
						mSensorMap.get(Configuration.Shimmer3.SensorMapKey.HOST_EXG_TEST).setIsEnabled(false);
						mSensorMap.get(Configuration.Shimmer3.SensorMapKey.HOST_EXG_CUSTOM).setIsEnabled(false);
						mSensorMap.get(Configuration.Shimmer3.SensorMapKey.HOST_EXG_RESPIRATION).setIsEnabled(false);
					}
					else if(isEXGUsingDefaultTestSignalConfiguration()){
						mSensorMap.get(Configuration.Shimmer3.SensorMapKey.HOST_ECG).setIsEnabled(false);
						mSensorMap.get(Configuration.Shimmer3.SensorMapKey.HOST_EMG).setIsEnabled(false);
						mSensorMap.get(Configuration.Shimmer3.SensorMapKey.HOST_EXG_TEST).setIsEnabled(true);
						mSensorMap.get(Configuration.Shimmer3.SensorMapKey.HOST_EXG_CUSTOM).setIsEnabled(false);
						mSensorMap.get(Configuration.Shimmer3.SensorMapKey.HOST_EXG_RESPIRATION).setIsEnabled(false);
					}
					else if(isEXGUsingCustomSignalConfiguration()){
						mSensorMap.get(Configuration.Shimmer3.SensorMapKey.HOST_ECG).setIsEnabled(false);
						mSensorMap.get(Configuration.Shimmer3.SensorMapKey.HOST_EMG).setIsEnabled(false);
						mSensorMap.get(Configuration.Shimmer3.SensorMapKey.HOST_EXG_TEST).setIsEnabled(false);
						mSensorMap.get(Configuration.Shimmer3.SensorMapKey.HOST_EXG_CUSTOM).setIsEnabled(true);
						mSensorMap.get(Configuration.Shimmer3.SensorMapKey.HOST_EXG_RESPIRATION).setIsEnabled(false);
					}
					else {
						if (mSensorMap.get(Configuration.Shimmer3.SensorMapKey.HOST_ECG)!=null){
							mSensorMap.get(Configuration.Shimmer3.SensorMapKey.HOST_ECG).setIsEnabled(false);
							mSensorMap.get(Configuration.Shimmer3.SensorMapKey.HOST_EMG).setIsEnabled(false);
							mSensorMap.get(Configuration.Shimmer3.SensorMapKey.HOST_EXG_TEST).setIsEnabled(false);
							mSensorMap.get(Configuration.Shimmer3.SensorMapKey.HOST_EXG_CUSTOM).setIsEnabled(false);
							mSensorMap.get(Configuration.Shimmer3.SensorMapKey.HOST_EXG_RESPIRATION).setIsEnabled(false);
						}
					}
				}
//			}
		}
	}
	
	private void checkWhichExgRespPhaseValuesToUse(){
		String stringKey = Configuration.Shimmer3.GuiLabelConfig.EXG_RESPIRATION_DETECT_PHASE;
		if(mConfigOptionsMap!=null){
			ConfigOptionDetails configOptions = mConfigOptionsMap.get(stringKey);
			if(configOptions!=null){
				if(getHardwareVersion()==HW_ID.SHIMMER_3){
					int nonStandardIndex = -1;
					if(isExgRespirationDetectFreq32kHz()) {
						nonStandardIndex = ConfigOptionDetailsSensor.VALUE_INDEXES.EXG_RESPIRATION_DETECT_PHASE.PHASE_32KHZ;
					}
					else {
						nonStandardIndex = ConfigOptionDetailsSensor.VALUE_INDEXES.EXG_RESPIRATION_DETECT_PHASE.PHASE_64KHZ;
					}
					
					if(nonStandardIndex!=-1 && configOptions instanceof ConfigOptionDetailsSensor){
						((ConfigOptionDetailsSensor) configOptions).setIndexOfValuesToUse(nonStandardIndex);
					}
					
					// change config val if not appropriate
					Integer[] configvalues = configOptions.getConfigValues();
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
			ConfigOptionDetails configOptions = mConfigOptionsMap.get(stringKey);
			if(configOptions!=null){
				if(getHardwareVersion()==HW_ID.SHIMMER_3){
					int nonStandardIndex = -1;
					if(isEXGUsingDefaultRespirationConfiguration()) { // Do Respiration check first
						nonStandardIndex = ConfigOptionDetailsSensor.VALUE_INDEXES.EXG_REFERENCE_ELECTRODE.RESP;
					}
					else if(isEXGUsingDefaultECGConfiguration()) {
						nonStandardIndex = ConfigOptionDetailsSensor.VALUE_INDEXES.EXG_REFERENCE_ELECTRODE.ECG;
					}
					else if(isEXGUsingDefaultEMGConfiguration()) {
						nonStandardIndex = ConfigOptionDetailsSensor.VALUE_INDEXES.EXG_REFERENCE_ELECTRODE.EMG;
					}
					else if(isEXGUsingDefaultTestSignalConfiguration()) {
						nonStandardIndex = ConfigOptionDetailsSensor.VALUE_INDEXES.EXG_REFERENCE_ELECTRODE.TEST;
					}
					else {
						nonStandardIndex = ConfigOptionDetailsSensor.VALUE_INDEXES.EXG_REFERENCE_ELECTRODE.CUSTOM;
					}
					
					if(nonStandardIndex!=-1 && configOptions instanceof ConfigOptionDetailsSensor){
						((ConfigOptionDetailsSensor) configOptions).setIndexOfValuesToUse(nonStandardIndex);
					}

					
					// change config val if not appropriate
					Integer[] configvalues = configOptions.getConfigValues();
					if(!Arrays.asList(configvalues).contains(getEXGReferenceElectrode())){
						setEXGReferenceElectrode(configvalues[0]);
					}

				}
			}
		}
	}
	
	/**
	 * Checks if 16 bit ECG configuration is set on the Shimmer device. Do not
	 * use this command right after setting an EXG setting, as due to the
	 * execution model, the old settings might be returned, if this command is
	 * executed before an ack is received.
	 * 
	 * @return true if 16 bit ECG is set
	 */
	@Deprecated
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
	@Deprecated
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
	@Deprecated
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
	@Deprecated
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
	@Deprecated
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
	@Deprecated
	public boolean isEXGUsingTestSignal24Configuration(){
		boolean using = false;
		if ((mEnabledSensors & SENSOR_EXG1_24BIT)>0 && (mEnabledSensors & SENSOR_EXG2_24BIT)>0){
			if(isEXGUsingDefaultTestSignalConfiguration()){
				using = true;
			}
		}
		return using;
	}	
	
	@Deprecated
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

	@Deprecated
	public static String parseLeadOffComparatorTresholdToString(int treshold){

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

	@Deprecated
	public static String parseLeadOffModeToString(int leadOffMode){

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

	@Deprecated
	public static String parseLeadOffDetectionCurrentToString(int current){

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
	
	@Deprecated
	public int getReferenceElectrode(){
		return mEXGReferenceElectrode;
	}
	
	@Deprecated
	public int getLeadOffDetectionMode(){
		return mLeadOffDetectionMode;
	}
	
	@Deprecated
	public int getLeadOffDetectionCurrent(){
		return mEXGLeadOffDetectionCurrent;
	}
	
	@Deprecated
	public int getLeadOffComparatorTreshold(){
		return mEXGLeadOffComparatorTreshold;
	}
	
	@Deprecated
	public int getExGComparatorsChip1(){
		return mEXG1Comparators;
	}
	
	@Deprecated
	public int getExGComparatorsChip2(){
		return mEXG2Comparators;
	}

	//-------------------- ExG End -----------------------------------


	// ----------- KionixKXRB52042 - Analog Accelerometer start -----------------------------------
	//XXX-RS-AA-SensorClass?
	public boolean isUsingDefaultLNAccelParam(){
		return mDefaultCalibrationParametersAccel;
	}

	public boolean isUsingValidLNAccelParam(){
		if(!isAllZeros(getAlignmentMatrixAccel()) && !isAllZeros(getSensitivityMatrixAccel())){
			return true;
		}else{
			return false;
		}
	}
	
	//XXX-RS-AA-SensorClass?
	public boolean isUsingDefaultAccelParam(){
		return mDefaultCalibrationParametersAccel;
	}

	//XXX-RS-AA-SensorClass?
	public double[][] getAlignmentMatrixAccel(){
		return mAlignmentMatrixAnalogAccel;
	}

	//XXX-RS-AA-SensorClass?
	public double[][] getSensitivityMatrixAccel(){
		return mSensitivityMatrixAnalogAccel;
	}

	//XXX-RS-AA-SensorClass?
	public double[][] getOffsetVectorMatrixAccel(){
		return mOffsetVectorAnalogAccel;
	}
	// ----------- KionixKXRB52042 - Analog Accelerometer end -----------------------------------

	// ----------- LSM303 start -----------------------------------
	/**
	 * @param mHighResAccelWR the mHighResAccelWR to set
	 */
	//XXX-RS-LSM-SensorClass?
	public void setHighResAccelWR(boolean mHighResAccelWR) {
		this.mHighResAccelWR = mHighResAccelWR;
	}

	/**
	 * This enables the low power accel option. When not enabled the sampling
	 * rate of the accel is set to the closest value to the actual sampling rate
	 * that it can achieve. In low power mode it defaults to 10Hz. Also an
	 * additional low power mode is used for the LSM303DLHC. This command will
	 * only supports the following Accel range +4g, +8g , +16g   //XXX-RS-LSM-SensorClass? Where in the datasheet is this mentioned?
	 * 
	 * @param enable
	 */
	//XXX-RS-LSM-SensorClass?
	public void setLowPowerAccelWR(boolean enable){
		mLowPowerAccelWR = enable;
		mHighResAccelWR = !enable;

		setLSM303AccelRateFromFreq(getSamplingRateShimmer());
	}


	/**
	 * This enables the low power mag option. When not enabled the sampling rate
	 * of the mag is set to the closest supported value to the actual sampling
	 * rate that it can achieve. In low power mode it defaults to 10Hz
	 * 
	 * @param enable
	 */
	//XXX-RS-LSM-SensorClass?
	protected void setLowPowerMag(boolean enable){
		mLowPowerMag = enable;
		if((getHardwareVersion()==HW_ID.SHIMMER_2)||(getHardwareVersion()==HW_ID.SHIMMER_2R)){
			if (!mLowPowerMag){
				if (getSamplingRateShimmer()>=50){
					mShimmer2MagRate = 6;
				} else if (getSamplingRateShimmer()>=20) {
					mShimmer2MagRate = 5;
				} else if (getSamplingRateShimmer()>=10) {
					mShimmer2MagRate = 4;
				} else {
					mShimmer2MagRate = 3;
				}
			} else {
				mShimmer2MagRate = 4;
			}
		} else {
			setLSM303MagRateFromFreq(getSamplingRateShimmer());
		}
	}
	
	
	//XXX-RS-LSM-SensorClass?
	public void setDigitalAccelRange(int i){
		mAccelRange = i;
	}

	
	/**
	 * @param mLSM303DigitalAccelRate the mLSM303DigitalAccelRate to set
	 */
	//XXX-RS-LSM-SensorClass?
	public void setLSM303DigitalAccelRate(int mLSM303DigitalAccelRate) {
		// double check that rate is compatible with LPM (8 not compatible so set to higher rate)
		if((!isLSM303DigitalAccelLPM()) && (mLSM303DigitalAccelRate==8)) {
			mLSM303DigitalAccelRate = 9;
		}
		this.mLSM303DigitalAccelRate = mLSM303DigitalAccelRate;
	}
	
	
	//XXX-RS-LSM-SensorClass?
	protected void setLSM303MagRange(int i){
		mMagRange = i;
	}
	

	/**
	 * @param mLSM303MagRate the mLSM303MagRate to set
	 */
	//XXX-RS-LSM-SensorClass?  
	protected void setLSM303MagRate(int mLSM303MagRate) {
		this.mLSM303MagRate = mLSM303MagRate;
	}
	
	
	/**
	 * Computes next higher available sensor sampling rate setting based on
	 * passed in "freq" variable and dependent on whether low-power mode is set.
	 * 
	 * @param freq
	 * @return int the rate configuration setting for the respective sensor
	 */
	//XXX-RS-LSM-SensorClass?
	private int setLSM303AccelRateFromFreq(double freq) {
		// Unused: 8 = 1.620kHz (only low-power mode), 9 = 1.344kHz (normal-mode) / 5.376kHz (low-power mode)
		
		// Check if channel is enabled 
		if (!isSensorEnabled(Configuration.Shimmer3.SensorMapKey.SHIMMER_LSM303DLHC_ACCEL)) {
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
	//XXX-RS-LSM-SensorClass?
	private int setLSM303MagRateFromFreq(double freq) {
		// Check if channel is enabled 
		if (!isSensorEnabled(Configuration.Shimmer3.SensorMapKey.SHIMMER_LSM303DLHC_MAG)) {
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
	
	//XXX-RS-LSM-SensorClass?
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


	//XXX-RS-LSM-SensorClass?
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


	/**
	 * @return the mHighResAccelWR
	 */
	//XXX-RS-LSM-SensorClass?
	public boolean isHighResAccelWR() {
		return mHighResAccelWR;
	}

	/**
	 * @return the mLSM303DigitalAccelHRM
	 */
	//XXX-RS-LSM-SensorClass?
	public boolean isLSM303DigitalAccelHRM() {
		return mHighResAccelWR;
	}

	/**
	 * @return the mLowPowerAccelWR
	 */
	//XXX-RS-LSM-SensorClass?
	public boolean isLowPowerAccelWR() {
		return mLowPowerAccelWR;
	}

	/**
	 * @return the mLSM303DigitalAccelLPM
	 */
	//XXX-RS-LSM-SensorClass?
	public boolean isLSM303DigitalAccelLPM() {
		return mLowPowerAccelWR;
	}


	//XXX-RS-LSM-SensorClass?
	public boolean isLowPowerAccelEnabled() {
		return mLowPowerAccelWR;
	}


	//XXX-RS-LSM-SensorClass?
    public boolean isLowPowerMagEnabled(){
		return mLowPowerMag;
	} 

	
	//XXX-RS-LSM-SensorClass?
	public boolean isUsingDefaultWRAccelParam(){
		return mDefaultCalibrationParametersDigitalAccel; 
	}

	//XXX-RS-LSM-SensorClass?
	public boolean isUsingValidWRAccelParam(){
		if(!isAllZeros(getAlignmentMatrixAccel()) && !isAllZeros(getSensitivityMatrixAccel())){
			return true;
		}else{
			return false;
		}
	}
	
	public boolean isAllZeros(double[][] matrix){
		boolean allZeros = true;
		
		for(int j = 0; j < matrix[1].length; j++){
			for(int i = 0; i < matrix.length; i++){
		//		ty
			}
			
		}
		
		return allZeros;
	}

	//XXX-RS-LSM-SensorClass?
	public boolean isUsingDefaultMagParam(){
		return mDefaultCalibrationParametersMag;
	}
	
	//XXX-RS-LSM-SensorClass?
	public boolean isUsingValidMagParam(){
		if(!isAllZeros(getAlignmentMatrixMag()) && !isAllZeros(getSensitivityMatrixMag())){
			return true;
		}else{
			return false;
		}
	}
	

	//XXX-RS-LSM-SensorClass?  Not unique for LSM303
	public boolean is3DOrientatioEnabled(){
		return mOrientationEnabled;
	}


	public int getLowPowerAccelEnabled(){//XXX-RS-LSM-SensorClass?
		if(mLowPowerAccelWR)
			return 1;
		else
			return 0;
	}

	public int getLowPowerMagEnabled() {//XXX-RS-LSM-SensorClass?
		if(mLowPowerMag)
			return 1;
		else
			return 0;
	}

	
	/** 0 = +/-2g, 1 = +/-4g, 2 = +/-8g, 3 = +/- 16g */
	public int getAccelRange(){//XXX-RS-LSM-SensorClass?
		return mAccelRange;
	}

	
	public int getMagRange(){//XXX-RS-LSM-SensorClass?
		return mMagRange;
	}


	/**
	 * @return the mLSM303MagRate
	 */
	//XXX-RS-LSM-SensorClass?
	public int getLSM303MagRate() {
		return mLSM303MagRate;
	}

	/**
	 * @return the mLSM303DigitalAccelRate
	 */
	//XXX-RS-LSM-SensorClass?
	public int getLSM303DigitalAccelRate() {
		return mLSM303DigitalAccelRate;
	}


	public double[][] getAlignmentMatrixWRAccel(){//XXX-RS-LSM-SensorClass?
		return mAlignmentMatrixWRAccel;
	}

	public double[][] getSensitivityMatrixWRAccel(){//XXX-RS-LSM-SensorClass?
		return mSensitivityMatrixWRAccel;
	}

	public double[][] getOffsetVectorMatrixWRAccel(){//XXX-RS-LSM-SensorClass?
		return mOffsetVectorWRAccel;
	}

	public double[][] getAlignmentMatrixMag(){//XXX-RS-LSM-SensorClass?
		return mAlignmentMatrixMagnetometer;
	}

	public double[][] getSensitivityMatrixMag(){//XXX-RS-LSM-SensorClass?
		return mSensitivityMatrixMagnetometer;
	}

	public double[][] getOffsetVectorMatrixMag(){//XXX-RS-LSM-SensorClass?
		return mOffsetVectorMagnetometer;
	}

	/**
	 * @return the mSamplingDividerLsm303dlhcAccel
	 */
	//XXX-RS-LSM-SensorClass?  (Only GQ BLE?)
	public int getSamplingDividerLsm303dlhcAccel() {
		return mSamplingDividerLsm303dlhcAccel;
	}




	// ----------- LSM303 end -----------------------------------
	
	// ----------- MPU9X50 options start -------------------------
	
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
	private int setMPU9150MagRateFromFreq(double freq) {
		boolean setFreq = false;
		// Check if channel is enabled 
		if(checkIfAnyMplChannelEnabled()){
			setFreq = true;
		}
		else if (isSensorEnabled(Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_MAG)) {
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

	private void setDefaultMpu9150GyroSensorConfig(boolean state) {
		if(!checkIfAnyMplChannelEnabled()) {
			if(!isSensorEnabled(Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_ACCEL)) {
				if(state) {
					setLowPowerGyro(false);
				}
				else {
					setLowPowerGyro(true);
				}
			}
			
			mGyroRange=1;
//			if(!state){
//				mGyroRange=1; // 500dps
//			}
		}
		else {
			mGyroRange=3; // 2000dps
		}
	}
	
	private void setDefaultMpu9150AccelSensorConfig(boolean state) {
		if(!checkIfAnyMplChannelEnabled()) {
			if(!isSensorEnabled(Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_GYRO)) {
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
			setMPU9150MagRateFromFreq(getSamplingRateShimmer());
			setMPU9150MplRateFromFreq(getSamplingRateShimmer());
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
				setMPU9150GyroAccelRateFromFreq(getSamplingRateShimmer());
			}
			else {
				setLowPowerGyro(true);
			}
			
			setMPU9150MagRateFromFreq(getSamplingRateShimmer());
			setMPU9150MplRateFromFreq(getSamplingRateShimmer());
		}
	}
	
	private boolean checkIfAMpuGyroOrAccelEnabled(){
		if(isSensorEnabled(Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_GYRO)) {
			return true;
		}
		if(isSensorEnabled(Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_ACCEL)) {
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
		if (mShimmerVerObject.getHardwareVersion()==HW_ID.SHIMMER_3 || mShimmerVerObject.getHardwareVersion()==HW_ID.SHIMMER_GQ_BLE) {
			if(mSensorMap.keySet().size()>0){
				
				for(int key:SensorMPU9X50.mListOfMplChannels){
//				for(int key:mListOfMplChannels){
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
			
	public boolean checkIfAnyMplChannelEnabled(){
		if (mShimmerVerObject.getHardwareVersion()==HW_ID.SHIMMER_3 || mShimmerVerObject.getHardwareVersion()==HW_ID.SHIMMER_GQ_BLE) {
			if(mSensorMap.keySet().size()>0){
				
				for(int key:SensorMPU9X50.mListOfMplChannels){
//					for(int key:mListOfMplChannels){
					if(isSensorEnabled(key)) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public int getGyroRange(){
		return mGyroRange;
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
	
	// MPL options
	/**
	 * @return the mMPU9150DMP
	 */
	public boolean isMPU9150DMP() {
		return (mMPU9150DMP>0)? true:false;
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
		return (mMPLEnable>0)? true:false;
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
		return (mMPLSensorFusion>0)? true:false;
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
		return (mMPLGyroCalTC>0)? true:false;
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
		return (mMPLVectCompCal>0)? true:false;
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
		return (mMPLMagDistCal>0)? true:false;
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
		return (mMPLSensorFusion>0)? true:false;
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

	// ----------- MPU9X50 options end -------------------------
	
	@Override
	public Object getConfigValueUsingConfigLabel(String componentName) {
		Object returnValue = null;
		
        if((componentName.equals(Configuration.Shimmer3.GuiLabelConfig.LSM303DLHC_ACCEL_RATE))//XXX-RS-LSM-SensorClass? 
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
			
			case(Configuration.Shimmer3.GuiLabelConfig.KINEMATIC_LPM)://XXX-RS-LSM-SensorClass? 
				if(isLSM303DigitalAccelLPM()&&checkLowPowerGyro()&&checkLowPowerMag()) {
					returnValue = true;
				}
				else {
					returnValue = false;
				}
	        	break;
			case(Configuration.Shimmer3.GuiLabelConfig.LSM303DLHC_ACCEL_LPM)://XXX-RS-LSM-SensorClass? 
				returnValue = isLSM303DigitalAccelLPM();
	        	break;
			case(Configuration.Shimmer3.GuiLabelConfig.MPU9150_GYRO_LPM):
				returnValue = checkLowPowerGyro();
	        	break;
			case(Configuration.Shimmer3.GuiLabelConfig.LSM303DLHC_MAG_LPM)://XXX-RS-LSM-SensorClass? 
				returnValue = checkLowPowerMag();
	        	break;
			case(Configuration.Shimmer3.GuiLabelConfig.TCX0):
				returnValue = isTCXO();
	        	break;
//			case(Configuration.Shimmer3.GuiLabelConfig.INT_EXP_BRD_POWER_BOOLEAN):
//				returnValue = isInternalExpPower();
//	        	break;
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
    	
			case(Configuration.Shimmer3.GuiLabelConfig.LSM303DLHC_ACCEL_RANGE)://XXX-RS-LSM-SensorClass? 
				returnValue = getAccelRange();
	        	break;
	        
			case(Configuration.Shimmer3.GuiLabelConfig.MPU9150_GYRO_RANGE):
				returnValue = getGyroRange();
	        	break;
	
			case(Configuration.Shimmer3.GuiLabelConfig.LSM303DLHC_MAG_RANGE)://XXX-RS-LSM-SensorClass? 
				//TODO check below and commented out code
				returnValue = getMagRange();
			
//    							// firmware sets mag range to 7 (i.e. index 6 in combobox) if user set mag range to 0 in config file
//    							if(getMagRange() == 0) cmBx.setSelectedIndex(6);
//    							else cmBx.setSelectedIndex(getMagRange()-1);
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
	        	
			case(Configuration.Shimmer3.GuiLabelConfig.LSM303DLHC_ACCEL_RATE)://XXX-RS-LSM-SensorClass? 
				int configValue = getLSM303DigitalAccelRate(); 
				 
	        	if(!isLSM303DigitalAccelLPM()) {
		        	if(configValue==8) {
		        		//TODO:
		        		/* RS (20/5/2016): Why returning a different value?
		        		 * In the Set-method the compatibility-check for Accel Rates supported for Low Power Mode is made.
		        		 * In this get-method the it should just read/get the value, not manipulating it.
		        		 * */
		        		configValue = 9;
		        	}
	        	}
				returnValue = configValue;
	    		break;
	    		
			case(Configuration.Shimmer3.GuiLabelConfig.LSM303DLHC_MAG_RATE)://XXX-RS-LSM-SensorClass? 
				returnValue = getLSM303MagRate();
	        	break;

			case(Configuration.Shimmer3.GuiLabelConfig.MPU9150_ACCEL_RANGE):
				returnValue = getMPU9150AccelRange();
            	break;
			case(Configuration.Shimmer3.GuiLabelConfig.MPU9150_DMP_GYRO_CAL):
				returnValue = getMPU9150MotCalCfg();
            	break;
			case(Configuration.Shimmer3.GuiLabelConfig.MPU9150_MPL_LPF):
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
//			case(Configuration.Shimmer3.GuiLabelConfig.INT_EXP_BRD_POWER_INTEGER):
//				returnValue = getInternalExpPower();
//            	break;
			case(Configuration.Shimmer3.GuiLabelConfig.PPG_ADC_SELECTION):
				returnValue = getPpgAdcSelectionGsrBoard();
	    		break;
			case(Configuration.Shimmer3.GuiLabelConfig.PPG1_ADC_SELECTION):
				returnValue = getPpg1AdcSelectionProto3DeluxeBoard();
	    		break;
			case(Configuration.Shimmer3.GuiLabelConfig.PPG2_ADC_SELECTION):
				returnValue = getPpg2AdcSelectionProto3DeluxeBoard();
	    		break;
            	

			case(Configuration.ShimmerGqBle.GuiLabelConfig.SAMPLING_RATE_DIVIDER_GSR):
				returnValue = getSamplingDividerGsr();
	    		break;
			case(Configuration.ShimmerGqBle.GuiLabelConfig.SAMPLING_RATE_DIVIDER_LSM303DLHC_ACCEL): //XXX-RS-LSM-SensorClass? 
				returnValue = getSamplingDividerLsm303dlhcAccel();
	    		break;
			case(Configuration.ShimmerGqBle.GuiLabelConfig.SAMPLING_RATE_DIVIDER_PPG):
				returnValue = getSamplingDividerPpg();
	    		break;
			case(Configuration.ShimmerGqBle.GuiLabelConfig.SAMPLING_RATE_DIVIDER_VBATT):
				returnValue = getSamplingDividerVBatt();
	    		break;
	    		
	    		
//Strings
//    					case(Configuration.Shimmer3.GuiLabelConfig.SHIMMER_USER_ASSIGNED_NAME):
//    						returnValue = getShimmerUserAssignedName();
//    			        	break;
//    					case(Configuration.Shimmer3.GuiLabelConfig.TRIAL_NAME):
//    						returnValue = getTrialName();
//    			        	break;
			case(Configuration.Shimmer3.GuiLabelConfig.SHIMMER_SAMPLING_RATE):
		        Double readSamplingRate = getSamplingRateShimmer();
		    	Double actualSamplingRate = 32768/Math.floor(32768/readSamplingRate); // get Shimmer compatible sampling rate
		    	actualSamplingRate = (double)Math.round(actualSamplingRate * 100) / 100; // round sampling rate to two decimal places
//    					    	consolePrintLn("GET SAMPLING RATE: " + componentName);
		    	returnValue = actualSamplingRate.toString();
	        	break;
			case(Configuration.Shimmer3.GuiLabelConfig.BUFFER_SIZE):
				returnValue = Integer.toString(getBufferSize());
	        	break;
//    					case(Configuration.Shimmer3.GuiLabelConfig.CONFIG_TIME):
//    			        	returnValue = getConfigTimeParsed();
//    			        	break;
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
//    		    		System.out.println("Gyro Sampling rate: " + getMPU9150GyroAccelRateInHz() + " " + returnValue);
	        	break;
			case(Configuration.Shimmer3.GuiLabelConfig.MPU9150_GYRO_RATE_HZ):
				returnValue = getMPU9150GyroAccelRateInHz();
				break;
	        	
////List<Byte[]>
//    					case(Configuration.Shimmer3.GuiLabelConfig.EXG_BYTES):
//    						List<byte[]> listOFExGBytes = new ArrayList<byte[]>();
//    						listOFExGBytes.add(getEXG1RegisterArray());
//    						listOFExGBytes.add(getEXG2RegisterArray());
//    						returnValue = listOFExGBytes;
//    			        	break;
	        	
	        default:
	        	returnValue = super.getConfigValueUsingConfigLabel(componentName);
	        	break;
		}
		
		return returnValue;
	}		
	
	@Override
	public Object setConfigValueUsingConfigLabel(String groupName, String componentName, Object valueToSet) {

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
				setLowPowerAccelWR((boolean)valueToSet);//XXX-RS-LSM-SensorClass? 
				setLowPowerGyro((boolean)valueToSet);
				setLowPowerMag((boolean)valueToSet);
	        	break;
			case(Configuration.Shimmer3.GuiLabelConfig.LSM303DLHC_ACCEL_LPM)://XXX-RS-LSM-SensorClass? 
				setLowPowerAccelWR((boolean)valueToSet);
	        	break;
			case(Configuration.Shimmer3.GuiLabelConfig.MPU9150_GYRO_LPM):
				setLowPowerGyro((boolean)valueToSet);
	        	break;
			case(Configuration.Shimmer3.GuiLabelConfig.LSM303DLHC_MAG_LPM)://XXX-RS-LSM-SensorClass? 
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
		        	
    		case(Configuration.Shimmer3.GuiLabelConfig.LSM303DLHC_ACCEL_RANGE)://XXX-RS-LSM-SensorClass? 
				setDigitalAccelRange((int)valueToSet);
	        	break;
	        
			case(Configuration.Shimmer3.GuiLabelConfig.MPU9150_GYRO_RANGE):
	        	setMPU9150GyroRange((int)valueToSet);
	        	break;
	
			case(Configuration.Shimmer3.GuiLabelConfig.LSM303DLHC_MAG_RANGE)://XXX-RS-LSM-SensorClass? 
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
				
			case(Configuration.Shimmer3.GuiLabelConfig.LSM303DLHC_ACCEL_RATE)://XXX-RS-LSM-SensorClass? 
				setLSM303DigitalAccelRate((int)valueToSet);
	    		break;
	    		
			case(Configuration.Shimmer3.GuiLabelConfig.LSM303DLHC_MAG_RATE)://XXX-RS-LSM-SensorClass? 
				setLSM303MagRate((int)valueToSet);
	        	break;
	        	
			case(Configuration.Shimmer3.GuiLabelConfig.MPU9150_ACCEL_RANGE):
				setMPU9150AccelRange((int)valueToSet);
	        	break;
			case(Configuration.Shimmer3.GuiLabelConfig.MPU9150_DMP_GYRO_CAL):
				setMPU9150MotCalCfg((int)valueToSet);
	        	break;
			case(Configuration.Shimmer3.GuiLabelConfig.MPU9150_MPL_LPF):
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
			case(Configuration.ShimmerGqBle.GuiLabelConfig.SAMPLING_RATE_DIVIDER_GSR):
				setSamplingDividerGsr((int)valueToSet);
	    		break;
			case(Configuration.ShimmerGqBle.GuiLabelConfig.SAMPLING_RATE_DIVIDER_LSM303DLHC_ACCEL)://XXX-RS-LSM-SensorClass? Is there a sample rate divider LSM303DLHC_MAG
				setSamplingDividerLsm303dlhcAccel((int)valueToSet);
	    		break;
			case(Configuration.ShimmerGqBle.GuiLabelConfig.SAMPLING_RATE_DIVIDER_PPG):
				setSamplingDividerPpg((int)valueToSet);
	    		break;
			case(Configuration.ShimmerGqBle.GuiLabelConfig.SAMPLING_RATE_DIVIDER_VBATT):
				setSamplingDividerVBatt((int)valueToSet);
	    		break;
	
//Strings
//			case(Configuration.Shimmer3.GuiLabelConfig.SHIMMER_USER_ASSIGNED_NAME):
//        		setShimmerUserAssignedName((String)valueToSet);
//	        	break;
//			case(Configuration.Shimmer3.GuiLabelConfig.TRIAL_NAME):
//				setTrialName((String)valueToSet);
//	        	break;
//			case(Configuration.Shimmer3.GuiLabelConfig.SHIMMER_SAMPLING_RATE):
//	          	// don't let sampling rate be empty
//	          	Double enteredSamplingRate;
//	          	if(((String)valueToSet).isEmpty()) {
//	          		enteredSamplingRate = 1.0;
//	          	}            	
//	          	else {
//	          		enteredSamplingRate = Double.parseDouble((String)valueToSet);
//	          	}
//	      		setShimmerAndSensorsSamplingRate(enteredSamplingRate);
//	      		
//	      		returnValue = Double.toString(getSamplingRateShimmer());
//	        	break;
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
	        	returnValue = super.setConfigValueUsingConfigLabel(groupName, componentName, valueToSet);
	        	break;
		}
		
        if((componentName.equals(Configuration.Shimmer3.GuiLabelConfig.LSM303DLHC_ACCEL_RATE))//XXX-RS-LSM-SensorClass?
        		||(componentName.equals(Configuration.Shimmer3.GuiLabelConfig.EXG_RESPIRATION_DETECT_PHASE))
        		||(componentName.equals(Configuration.Shimmer3.GuiLabelConfig.EXG_REFERENCE_ELECTRODE))){
        	checkConfigOptionValues(componentName);
        }
			
		return returnValue;

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

	@Override
	public void setShimmerVersionObject(ShimmerVerObject hwfw) {
		super.setShimmerVersionObject(hwfw);
		
//		createInfoMemLayoutObjectIfNeeded();
		
		updateTimestampByteLength();
	}
	
	public void updateTimestampByteLength(){
		//Once the version is known update settings accordingly 
		if (getFirmwareVersionCode()>=6){
			mTimeStampPacketByteSize = 3;
			mTimeStampPacketRawMaxValue = 16777216;
		} 
		else {//if (getFirmwareVersionCode()<6){
			mTimeStampPacketByteSize = 2;
			mTimeStampPacketRawMaxValue = 65536;
		}
	}
	

	// --------------- Set Methods Start --------------------------

	public void setUniqueID(String uniqueID){
		mUniqueID = uniqueID;
		String[] idSplit = mUniqueID.split(".");
		if(idSplit.length>=3) {
			mDockID = idSplit[0] + "." + idSplit[1];
			try {
				mSlotNumber = Integer.parseInt(idSplit[2]);
			}
			catch (NumberFormatException nFE) {
			}
		}
	}

	public void initialise(int hardwareVersion) {
		this.setHardwareVersion(hardwareVersion);
		sensorAndConfigMapsCreate();
	}

	public void updateShimmerDriveInfo(ShimmerSDCardDetails shimmerSDCardDetails) {
		this.mShimmerSDCardDetails = shimmerSDCardDetails;
	}
	
	/** setShimmerVerionObject should be used instead
	 * @param firmwareId the firmwareId to set
	 */
	public void setFirmwareIdentifier(int firmwareId) {
		ShimmerVerObject sVOFwId = new ShimmerVerObject(getHardwareVersion(), firmwareId, getFirmwareVersionMajor(), getFirmwareVersionMinor(), getFirmwareVersionInternal());
		setShimmerVersionObject(sVOFwId);
	}

	/** setShimmerVerionObject should be used instead
	 * @param firmwareId the firmwareId to set
	 */
	public void setFirmwareVersion(int firmwareVersionMajor, int firmwareVersionMinor, int firmwareVersionInternal) {
		ShimmerVerObject sVOFwId = new ShimmerVerObject(getHardwareVersion(), getFirmwareIdentifier(), firmwareVersionMajor, firmwareVersionMinor, firmwareVersionInternal);
		setShimmerVersionObject(sVOFwId);
	}



	public void setShimmerVersionInfoAndCreateSensorMap(ShimmerVerObject hwfw) {
		setShimmerVersionObject(hwfw);
		sensorAndConfigMapsCreate();
	}

	public void clearShimmerVersionInfo() {
		setShimmerVersionInfoAndCreateSensorMap(new ShimmerVerObject());
	}
	


	// --------------- Set Methods End --------------------------

	
	// --------------- ShimmerGQ BLE related start --------------------------
	
	/** ShimmerGQBle
	 * @param mSamplingDividerVBatt the mSamplingDividerVBatt to set
	 */
	public void setSamplingDividerVBatt(int mSamplingDividerVBatt) {
		this.mSamplingDividerVBatt = mSamplingDividerVBatt;
	}

	/** ShimmerGQBle
	 * @param mSamplingDividerGsr the mSamplingDividerGsr to set
	 */
	public void setSamplingDividerGsr(int mSamplingDividerGsr) {
		this.mSamplingDividerGsr = mSamplingDividerGsr;
	}

	/** ShimmerGQBle
	 * @param mSamplingDividerLsm303dlhcAccel the mSamplingDividerLsm303dlhcAccel to set
	 */
	//XXX-RS-LSM-SensorClass?
	public void setSamplingDividerLsm303dlhcAccel(int mSamplingDividerLsm303dlhcAccel) {
		this.mSamplingDividerLsm303dlhcAccel = mSamplingDividerLsm303dlhcAccel;
	}

	/** ShimmerGQBle
	 * @param mSamplingDividerBeacon the mSamplingDividerBeacon to set
	 */
	public void setSamplingDividerBeacon(int mSamplingDividerBeacon) {
		this.mSamplingDividerBeacon = mSamplingDividerBeacon;
	}

	public void setExpansionBoardId(int expansionBoardId) {
		mExpansionBoardDetails.mExpansionBoardId = expansionBoardId;
	}

	
	// --------------- ShimmerGQ BLE related end --------------------------


	@Override
	public void parseUartConfigResponse(UartComponentPropertyDetails cPD, byte[] response){
		// Parse response string
		if(cPD==UART_COMPONENT_PROPERTY.BAT.ENABLE){
			//TODO Shimmer3 vs. ShimmerGQ
			if(getHardwareVersion()==HW_ID.SHIMMER_3){
				getSensorMap().get(Configuration.Shimmer3.SensorMapKey.SHIMMER_VBATT).setIsEnabled((response[0]==0)? false:true);
			}
			else if(getHardwareVersion()==HW_ID.SHIMMER_GQ_BLE){
				getSensorMap().get(Configuration.ShimmerGqBle.SensorMapKey.VBATT).setIsEnabled((response[0]==0)? false:true);
			}
		}
		else if(cPD==UART_COMPONENT_PROPERTY.BAT.FREQ_DIVIDER){
			setSamplingDividerVBatt(response[0]);
		}

		else if(cPD==UART_COMPONENT_PROPERTY.LSM303DLHC_ACCEL.ENABLE){//XXX-RS-LSM-SensorClass?
			//TODO Shimmer3 vs. ShimmerGQ
			if(getHardwareVersion()==HW_ID.SHIMMER_3){
				getSensorMap().get(Configuration.Shimmer3.SensorMapKey.SHIMMER_LSM303DLHC_ACCEL).setIsEnabled((response[0]==0)? false:true);
			}
			else if(getHardwareVersion()==HW_ID.SHIMMER_GQ_BLE){
				getSensorMap().get(Configuration.ShimmerGqBle.SensorMapKey.LSM303DLHC_ACCEL).setIsEnabled((response[0]==0)? false:true);
			}
		}
		else if(cPD==UART_COMPONENT_PROPERTY.LSM303DLHC_ACCEL.DATA_RATE){//XXX-RS-LSM-SensorClass?
			setLSM303DigitalAccelRate(response[0]);
		}
		else if(cPD==UART_COMPONENT_PROPERTY.LSM303DLHC_ACCEL.RANGE){//XXX-RS-LSM-SensorClass?
			setDigitalAccelRange(response[0]);
		}
		else if(cPD==UART_COMPONENT_PROPERTY.LSM303DLHC_ACCEL.LP_MODE){//XXX-RS-LSM-SensorClass?
			setLowPowerAccelWR((response[0]==0)? false:true);
		}
		else if(cPD==UART_COMPONENT_PROPERTY.LSM303DLHC_ACCEL.HR_MODE){//XXX-RS-LSM-SensorClass?
			setHighResAccelWR((response[0]==0)? false:true);
		}
		else if(cPD==UART_COMPONENT_PROPERTY.LSM303DLHC_ACCEL.FREQ_DIVIDER){//XXX-RS-LSM-SensorClass?
			setSamplingDividerLsm303dlhcAccel(response[0]);
		}
		else if(cPD==UART_COMPONENT_PROPERTY.LSM303DLHC_ACCEL.CALIBRATION){//XXX-RS-LSM-SensorClass?
//			parseCalParamLSM303DLHCAccel(response);
			retrieveKinematicCalibrationParametersFromPacket(response, ShimmerObject.LSM303DLHC_ACCEL_CALIBRATION_RESPONSE);//XXX-RS-LSM-SensorClass?
		}
		else if(cPD==UART_COMPONENT_PROPERTY.GSR.ENABLE){
			//TODO Shimmer3 vs. ShimmerGQ
			if(getHardwareVersion()==HW_ID.SHIMMER_3){
				getSensorMap().get(Configuration.Shimmer3.SensorMapKey.SHIMMER_GSR).setIsEnabled((response[0]==0)? false:true);
			}
			else if(getHardwareVersion()==HW_ID.SHIMMER_GQ_BLE){
				getSensorMap().get(Configuration.ShimmerGqBle.SensorMapKey.GSR).setIsEnabled((response[0]==0)? false:true);
			}
		}
		else if(cPD==UART_COMPONENT_PROPERTY.GSR.RANGE){
			setGSRRange(response[0]);
		}
		else if(cPD==UART_COMPONENT_PROPERTY.GSR.FREQ_DIVIDER){
			setSamplingDividerGsr(response[0]);
		}
		else if(cPD==UART_COMPONENT_PROPERTY.BEACON.ENABLE){
			//TODO Shimmer3 vs. ShimmerGQ
			if(getHardwareVersion()==HW_ID.SHIMMER_3){
				getSensorMap().get(Configuration.ShimmerGqBle.SensorMapKey.BEACON).setIsEnabled((response[0]==0)? false:true);
			}
			else if(getHardwareVersion()==HW_ID.SHIMMER_GQ_BLE){
				getSensorMap().get(Configuration.ShimmerGqBle.SensorMapKey.BEACON).setIsEnabled((response[0]==0)? false:true);
			}
		}
		else if(cPD==UART_COMPONENT_PROPERTY.BEACON.FREQ_DIVIDER){
			setSamplingDividerBeacon(response[0]);
		}
		else {
			super.parseUartConfigResponse(cPD, response);
		}
	}

	@Override
	public byte[] generateUartConfigMessage(UartComponentPropertyDetails cPD){
		
//		System.out.println("Component:" + cPD.component + " Property:" + cPD.property + " ByteArray:" + cPD.byteArray.length);
		
		if(cPD==UART_COMPONENT_PROPERTY.LSM303DLHC_ACCEL.ENABLE){//XXX-RS-LSM-SensorClass?
			byte[] response = new byte[1]; 
			response[0] = (byte)(getSensorMap().get(Configuration.ShimmerGqBle.SensorMapKey.LSM303DLHC_ACCEL).isEnabled()? 1:0);
			return response;
		}
		else if(cPD==UART_COMPONENT_PROPERTY.LSM303DLHC_ACCEL.DATA_RATE){//XXX-RS-LSM-SensorClass?
			byte[] response = new byte[1]; 
			response[0] = (byte)getLSM303DigitalAccelRate();
			return response;
		}
		else if(cPD==UART_COMPONENT_PROPERTY.LSM303DLHC_ACCEL.RANGE){//XXX-RS-LSM-SensorClass?
			byte[] response = new byte[1]; 
			response[0] = (byte)getAccelRange();
			return response;
		}
		else if(cPD==UART_COMPONENT_PROPERTY.LSM303DLHC_ACCEL.LP_MODE){//XXX-RS-LSM-SensorClass?
			byte[] response = new byte[1]; 
			response[0] = (byte)(isLowPowerAccelWR()? 1:0);
			return response;
		}
		else if(cPD==UART_COMPONENT_PROPERTY.LSM303DLHC_ACCEL.HR_MODE){//XXX-RS-LSM-SensorClass?
			byte[] response = new byte[1]; 
			response[0] = (byte)(isHighResAccelWR()? 1:0);
			return response;
		}
		else if(cPD==UART_COMPONENT_PROPERTY.LSM303DLHC_ACCEL.FREQ_DIVIDER){//XXX-RS-LSM-SensorClass?
			byte[] response = new byte[1]; 
			response[0] = (byte)(getSamplingDividerLsm303dlhcAccel());
			return response;
		}
		else if(cPD==UART_COMPONENT_PROPERTY.LSM303DLHC_ACCEL.CALIBRATION){//XXX-RS-LSM-SensorClass?
			byte[] response = generateCalParamLSM303DLHCAccel();
			return response;
		}
		else {
			return super.generateUartConfigMessage(cPD);
		}			
	}
	
	@Override
	public double getMinAllowedSamplingRate() {
		double minAllowedSamplingRate = super.getMinAllowedSamplingRate();
		if(isMPLEnable() || isMPU9150DMP()){
			minAllowedSamplingRate = Math.max(51.2, minAllowedSamplingRate);
		}
		return minAllowedSamplingRate;
	}

}
