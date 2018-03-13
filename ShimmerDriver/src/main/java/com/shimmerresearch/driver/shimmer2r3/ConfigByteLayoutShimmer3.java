package com.shimmerresearch.driver.shimmer2r3;

import java.io.Serializable;
import java.util.HashMap;

import com.shimmerresearch.driver.ConfigByteLayout;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails;
import com.shimmerresearch.driverUtilities.UtilShimmer;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.FW_ID;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;

/**
 * Hold the Shimmer3's microcontroller information memory layout. This region of
 * the the microcontrollers RAM can be used to configure all properties of the
 * Shimmer when configured through a docking station using Consensys. Variables
 * stored in this class are based on firmware header files for mapping which
 * bits in each information memory byte represents various configurable settings
 * on the Shimmer.
 * 
 * @author Mark Nolan
 *
 */
public class ConfigByteLayoutShimmer3 extends ConfigByteLayout implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5729543049033754281L;
	
//	public byte[] invalidMacId = new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
//	public byte[] invalidMacId2 = new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};
	public int maxNumOfExperimentNodes = 21;

//	int mFirmwareIdentifier = -1;
//	int mFirmwareVersionMajor = -1;
//	int mFirmwareVersionMinor = -1;
//	int mFirmwareVersionInternal = -1;
//	int mInfoMemSize = 512;
//	
//	public int MSP430_5XX_INFOMEM_D_ADDRESS = 0x001800; 
//	public int MSP430_5XX_INFOMEM_C_ADDRESS = 0x001880; 
//	public int MSP430_5XX_INFOMEM_B_ADDRESS = 0x001900;
//	public int MSP430_5XX_INFOMEM_A_ADDRESS = 0x001980; 
//	public int MSP430_5XX_INFOMEM_LAST_ADDRESS = 0x0019FF;
////	public final static int MSP430_5XX_PROGRAM_START_ADDRESS = 0x00FFFE; 

	
//	//SENSORS0
//	public int SENSOR_A_ACCEL =                     0x80;
//	public int SENSOR_MPU9150_GYRO =                0x40;
//	public int SENSOR_LSM303DLHC_MAG =              0x20;
//	public int SENSOR_EXG1_24BIT =                  0x10;
//	public int SENSOR_EXG2_24BIT =                  0x08;
//	public int SENSOR_GSR =                         0x04;
//	public int SENSOR_EXT_A7 =                      0x02;
//	public int SENSOR_EXT_A6 =                      0x01;
//	//SENSORS1
//	public int SENSOR_STRAIN =                      0x80;   //higher priority than SENSOR_INT_A13 and SENSOR_INT_A14
//	public int SENSOR_VBATT =                       0x20;
//	public int SENSOR_LSM303DLHC_ACCEL =            0x10;
//	public int SENSOR_EXT_A15 =                     0x08;
//	public int SENSOR_INT_A1 =                      0x04;
//	public int SENSOR_INT_A12 =                     0x02;
//	public int SENSOR_INT_A13 =                     0x01;
//	//SENORS2
//	public int SENSOR_INT_A14 =                     0x80;
//	public int SENSOR_MPU9150_ACCEL =               0x40;
//	public int SENSOR_MPU9150_MAG =                 0x20;
//	public int SENSOR_EXG1_16BIT =                  0x10;
//	public int SENSOR_EXG2_16BIT =                  0x08;
//	public int SENSOR_BMP180_PRESSURE =             0x04;
//	public int SENSOR_MPU9150_TEMP =                0x02;
//
//	//SENSORS3
//	public int SENSOR_MPU9150_MPL_QUAT_6DOF =       0x80;
//	public int SENSOR_MPU9150_MPL_QUAT_9DOF =       0x40;
//	public int SENSOR_MPU9150_MPL_EULER_6DOF =      0x20;
//	public int SENSOR_MPU9150_MPL_EULER_9DOF =      0x10;
//	public int SENSOR_MPU9150_MPL_HEADING =         0x08;
//	public int SENSOR_MPU9150_MPL_PEDOMETER =       0x04;
//	public int SENSOR_MPU9150_MPL_TAP =             0x02;
//	public int SENSOR_MPU9150_MPL_MOTION_ORIENT =   0x01;
//
//	//SENSORS4
//	public int SENSOR_MPU9150_GYRO_CAL =            0x80;
//	public int SENSOR_MPU9150_ACCEL_CAL =           0x40;
//	public int SENSOR_MPU9150_MAG_CAL =             0x20;
//	public int SENSOR_MPU9150_MPL_QUAT_6DOF_RAW =   0x10;
//
//	//public int SENSOR_LSM303DLHC_TEMPERATURE =    0x08;
//	//public int SENSOR_MSP430_TEMPERATURE =        0x01;
//	//public int SENSOR_BMP180_TEMPERATURE =        0x02;
//	//public int SENSOR_EXP_POWER =                 0x01;
//	//public int SENSOR_MPU9150_MPL_ROT_MAT =       0x;	
	
	public int idxShimmerSamplingRate =				0;
	public int idxBufferSize =                     	2;
	public int idxSensors0 =                        3;
	public int idxSensors1 =                        4;
	public int idxSensors2 =                        5;
	public int idxConfigSetupByte0 =              	6; //sensors setting bytes
	public int idxConfigSetupByte1 =              	7;
	public int idxConfigSetupByte2 =              	8;
	public int idxConfigSetupByte3 =              	9;
	public int idxEXGADS1292RChip1Config1 =         10;// exg bytes, not implemented yet
	public int idxEXGADS1292RChip1Config2 =         11;
	public int idxEXGADS1292RChip1LOff =            12;
	public int idxEXGADS1292RChip1Ch1Set =          13;
	public int idxEXGADS1292RChip1Ch2Set =          14;
	public int idxEXGADS1292RChip1RldSens =         15;
	public int idxEXGADS1292RChip1LOffSens =        16;
	public int idxEXGADS1292RChip1LOffStat =        17;
	public int idxEXGADS1292RChip1Resp1 =           18;
	public int idxEXGADS1292RChip1Resp2 =           19;
	public int idxEXGADS1292RChip2Config1 =         20;
	public int idxEXGADS1292RChip2Config2 =         21;
	public int idxEXGADS1292RChip2LOff =            22;
	public int idxEXGADS1292RChip2Ch1Set =          23;
	public int idxEXGADS1292RChip2Ch2Set =          24;
	public int idxEXGADS1292RChip2RldSens =         25;
	public int idxEXGADS1292RChip2LOffSens =        26;
	public int idxEXGADS1292RChip2LOffStat =        27;
	public int idxEXGADS1292RChip2Resp1 =           28;
	public int idxEXGADS1292RChip2Resp2 =           29;
	public int idxBtCommBaudRate =              	30;
	public int idxAnalogAccelCalibration =          31;
	public int idxMPU9150GyroCalibration =        	52;
	public int idxLSM303DLHCMagCalibration =      	73;
	public int idxLSM303DLHCAccelCalibration =    	94; //94->114

	// Derived Channels - used by SW not FW
	public int idxDerivedSensors0 =		    		0;
	public int idxDerivedSensors1 =		    		0;
	public int idxDerivedSensors2 =		    		0;

	public int idxDerivedSensors3 =		    		0;
	public int idxDerivedSensors4 =		    		0;
	public int idxDerivedSensors5 =		    		0;
	public int idxDerivedSensors6 =		    		0;
	public int idxDerivedSensors7 =		    		0;
	
	public int idxConfigSetupByte4 =              	128+0;
	public int idxConfigSetupByte5 =              	128+1;
	public int idxSensors3 =                        128+2;
	public int idxSensors4 =                        128+3;
	public int idxConfigSetupByte6 =              	128+4;
	public int idxMPLAccelCalibration =           	128+5; //+21
	public int idxMPLMagCalibration =             	128+26; //+21
	public int idxMPLGyroCalibration =            	128+47; //+12
	public int idxSDShimmerName =                 	128+59;   // +12 bytes
	public int idxSDEXPIDName =                  	128+71;   // +12 bytes
	public int idxSDConfigTime0 =                  	128+83;   // +4 bytes
	public int idxSDConfigTime1 =                  	128+84;
	public int idxSDConfigTime2 =                  	128+85;
	public int idxSDConfigTime3 =                  	128+86;
	public int idxSDMyTrialID =                   	128+87;   // 1 byte
	public int idxSDNumOfShimmers =                 128+88;   // 1 byte
	public int idxSDExperimentConfig0 =             128+89;
	public int idxSDExperimentConfig1 =             128+90;
	public int idxSDBTInterval =                  	128+91;
	public int idxEstimatedExpLengthMsb =           128+92; // 2bytes
	public int idxEstimatedExpLengthLsb =           128+93;
	public int idxMaxExpLengthMsb =                 128+94; // 2bytes
	public int idxMaxExpLengthLsb =                 128+95;
	public int idxMacAddress =                     	128+96; // 6bytes
	public int idxSDConfigDelayFlag =            	128+102;
	public int idxBtFactoryReset =            		0;

	public int idxNode0 =                           128+128+0;

//	public int idxMplCalibration =                  0;
	
	// Masks and Bitshift values
	public int maskShimmerSamplingRate =				0xFF;
	public int maskBufferSize =							0xFF;

	// Sensors
	public int maskSensors = 							0xFF;
	public int byteShiftSensors0 = 						0;
	public int byteShiftSensors1 =						8;
	public int byteShiftSensors2 =						16;
	
	//Config Byte0
	public int bitShiftLSM303DLHCAccelSamplingRate = 	4;
	public int maskLSM303DLHCAccelSamplingRate =    	0x0F;
	public int bitShiftLSM303DLHCAccelRange =			2;
	public int maskLSM303DLHCAccelRange =           	0x03;
	
	public int bitShiftLSM303DLHCAccelLPM = 			1;
	public int maskLSM303DLHCAccelLPM = 	  			0x01;

	public int bitShiftLSM303DLHCAccelHRM = 			0; // HIGH_RESOLUTION_MODE
	public int maskLSM303DLHCAccelHRM = 	  			0x01; // HIGH_RESOLUTION_MODE

	//Config Byte1
	public int bitShiftMPU9150AccelGyroSamplingRate =	0;
	public int maskMPU9150AccelGyroSamplingRate =		0xFF;

	//Config Byte2
	public int bitShiftLSM303DLHCMagRange =             5;
	public int maskLSM303DLHCMagRange =                 0x07;
	public int bitShiftLSM303DLHCMagSamplingRate =     	2;
	public int maskLSM303DLHCMagSamplingRate =          0x07;
	public int bitShiftMPU9150GyroRange =               0;
	public int maskMPU9150GyroRange =                   0x03;
	//Config Byte3
	public int bitShiftMPU9150AccelRange =              6;
	public int maskMPU9150AccelRange =                  0x03;
	public int bitShiftBMPX80PressureResolution =       4;
	public int maskBMPX80PressureResolution =           0x03;
	public int bitShiftGSRRange =                       1;
	public int maskGSRRange =                           0x07;
	public int bitShiftEXPPowerEnable =                 0;
	public int maskEXPPowerEnable =                     0x01;
	//Unused bits 3-0
	
	// Derived Channels - used by SW not FW
	public int maskDerivedChannelsByte =				0xFF;
	public int byteShiftDerivedSensors0 =				8*0;
	public int byteShiftDerivedSensors1 =				8*1;
	public int byteShiftDerivedSensors2 =				8*2;

	public int byteShiftDerivedSensors3 =				8*3;
	public int byteShiftDerivedSensors4 =				8*4;
	public int byteShiftDerivedSensors5 =				8*5;
	public int byteShiftDerivedSensors6 =				8*6;
	public int byteShiftDerivedSensors7 =				8*7;

//	public int maskDerivedChannel = 					0x01;
//	public int bitShiftDerivedChannelResAmp = 			0;
//	public int bitShiftDerivedChannelSkinTemp =			1;
//	public int bitShiftDerivedChannelPpg_ADC12ADC13 =	2;
//	public int bitShiftDerivedChannelPpg1_ADC12ADC13 =	3;
//	public int bitShiftDerivedChannelPpg2_ADC1ADC14 =	4;
//	public int bitShiftDerivedChannelPpgToHr = 			5;
//	public int bitShiftDerivedChannelEcgToHr = 			6;
//	public int bitShiftDerivedChannel6DofMadgewick =	8;
//	public int bitShiftDerivedChannel9DofMadgewick =	9;
	
	public int maskDerivedChannelResAmp = 			0x000001;
	public int maskDerivedChannelSkinTemp =			0x000002;
	public int maskDerivedChannelPpg_ADC12ADC13 =	0x000004;
	public int maskDerivedChannelPpg1_ADC12ADC13 =	0x000008;
	public int maskDerivedChannelPpg2_ADC1ADC14 =	0x000010;
//	public int maskDerivedChannelPpgToHr = 			0x000020;
//	public int maskDerivedChannelEcgToHr = 			0x000040;
//	
//	public int maskDerivedChannel6DofMadgewick =	0x000100;
//	public int maskDerivedChannel9DofMadgewick =	0x000200;

	
	// ExG related config bytes
	public int idxEXGADS1292RConfig1 = 			0;
	public int idxEXGADS1292RConfig2 = 			1;
	public int idxEXGADS1292RLOff = 			2;
	public int idxEXGADS1292RCH1Set = 			3;
	public int idxEXGADS1292RCH2Set = 			4;
	public int idxEXGADS1292RRLDSens = 			5;
	public int idxEXGADS1292RLOffSens = 		6;
	public int idxEXGADS1292RLOffStat = 		7;
	public int idxEXGADS1292RResp1 = 			8;
	public int idxEXGADS1292RResp2 = 			9;
	
//	public int bitShiftEXGRateSetting = 		0;
//	public int maskEXGRateSetting = 			0x07;
//	
//	public int bitShiftEXGGainSetting = 		4;
//	public int maskEXGGainSetting = 			0x07;
//
//	public int bitShiftEXGReferenceElectrode = 	0;
//	public int maskEXGReferenceElectrode = 		0x07;
	
	public int maskBaudRate = 					0xFF;
	
	public int lengthGeneralCalibrationBytes =	21;
	
	public int lengthShimmerName = 				12;
	public int lengthExperimentName = 			12;

	public int lengthConfigTimeBytes = 			4;
	
	// MPL related
	public int bitShiftMPU9150DMP = 			7;
	public int maskMPU9150DMP = 				0x01;

	public int bitShiftMPU9150LPF = 			3;
	public int maskMPU9150LPF = 				0x07;
	
	public int bitShiftMPU9150MotCalCfg = 		0;
	public int maskMPU9150MotCalCfg = 			0x07;

	public int bitShiftMPU9150MPLSamplingRate = 5;
	public int maskMPU9150MPLSamplingRate = 	0x07;

	public int bitShiftMPU9150MagSamplingRate = 2;
	public int maskMPU9150MagSamplingRate = 	0x07;
	
	public int bitShiftSensors3 =				24;
	public int bitShiftSensors4 =				32;
	
	public int bitShiftMPLSensorFusion = 		7;
	public int maskMPLSensorFusion = 			0x01;

	public int bitShiftMPLGyroCalTC = 			6;
	public int maskMPLGyroCalTC = 				0x01;

	public int bitShiftMPLVectCompCal = 		5;
	public int maskMPLVectCompCal = 			0x01;

	public int bitShiftMPLMagDistCal = 			4;
	public int maskMPLMagDistCal = 				0x01;
	
	public int bitShiftMPLEnable = 				3;
	public int maskMPLEnable = 					0x01;
	
	// SD logging related
	public int bitShiftButtonStart = 			5;
	public int maskButtonStart = 				0x01;

	public int bitShiftShowErrorLedsRwc = 		4;
	public int maskShowErrorLedsRwc =	 		0; // Only applicable for certain FW

	public int bitShiftShowErrorLedsSd = 		0;
	public int maskShowErrorLedsSd =	 		0; // Only applicable for certain FW

	public int bitShiftTimeSyncWhenLogging =	2;
	public int maskTimeSyncWhenLogging = 		0x01;

	public int bitShiftMasterShimmer = 			1;
	public int maskTimeMasterShimmer = 			0x01;
	
	public int bitShiftSingleTouch = 			7;
	public int maskTimeSingleTouch = 			0x01;
	
	public int bitShiftLowBattStop = 			0;
	public int maskLowBattStop =				0x01;

	public int bitShiftTCX0 = 					4;
	public int maskTimeTCX0 = 					0x01;

	public int lengthMacIdBytes = 				6;

	public int bitShiftSDConfigTime0 = 			24;
	public int bitShiftSDConfigTime1 = 			16;
	public int bitShiftSDConfigTime2 = 			8;
	public int bitShiftSDConfigTime3 = 			0;

	
	public int bitShiftSDCfgFileWriteFlag =		0; 
	public int maskSDCfgFileWriteFlag =			0x01; 
	public int bitShiftSDCalibFileWriteFlag =	1; 
	public int maskSDCalibFileWriteFlag =		0x01; 

	public int maskExg1_24bitFlag =			0x10<<(0*8); 
	public int maskExg2_24bitFlag =			0x08<<(0*8); 
	public int maskExg1_16bitFlag =			0x10<<(2*8); 
	public int maskExg2_16bitFlag =			0x08<<(2*8); 

	
	
	//	//ADC initialisation mask
//	public int MASK_A_ACCEL =        0x0001;
//	public int MASK_VBATT =          0x0002;
//	public int MASK_EXT_A7 =         0x0004;
//	public int MASK_EXT_A6 =         0x0008;
//	public int MASK_EXT_A15 =        0x0010;
//	public int MASK_INT_A1 =         0x0020;
//	public int MASK_GSR =            0x0020;   //uses ADC1
//	public int MASK_INT_A12 =        0x0040;
//	public int MASK_INT_A13 =        0x0080;
//	public int MASK_INT_A14 =        0x0100;
//	public int MASK_STRAIN =         0x0180;   //uses ADC13 and ADC14
//	//public int MASK_MSP_TEMP =     0x0200;
//
//	//LSM303DLHC Accel Range
//	//Corresponds to the FS field of the LSM303DLHC's CTRL_REG4_A register
//	//and the AFS_SEL field of the MPU9150's ACCEL_CONFIG register
//	public int ACCEL_2G =                     0x00;
//	public int ACCEL_4G =                     0x01;
//	public int ACCEL_8G =                     0x02;
//	public int ACCEL_16G =                    0x03;
//
//	//LSM303DLHC Accel Sampling Rate
//	//Corresponds to the ODR field of the LSM303DLHC's CTRL_REG1_A register
//	public int LSM303DLHC_ACCEL_POWER_DOWN =  0x00;
//	public int LSM303DLHC_ACCEL_1HZ =         0x01;
//	public int LSM303DLHC_ACCEL_10HZ =        0x02;
//	public int LSM303DLHC_ACCEL_25HZ =        0x03;
//	public int LSM303DLHC_ACCEL_50HZ =        0x04;
//	public int LSM303DLHC_ACCEL_100HZ =       0x05;
//	public int LSM303DLHC_ACCEL_200HZ =       0x06;
//	public int LSM303DLHC_ACCEL_400HZ =       0x07;
//	public int LSM303DLHC_ACCEL_1_620KHZ =    0x08; //1.620kHz in Low-power mode only
//	public int LSM303DLHC_ACCEL_1_344KHZ =    0x09; //1.344kHz in normal mode, 5.376kHz in low-power mode
//
//	//LSM303DLHC Mag gain
//	public int LSM303DLHC_MAG_1_3G =          0x01; //+/-1.3 Gauss
//	public int LSM303DLHC_MAG_1_9G =          0x02; //+/-1.9 Gauss
//	public int LSM303DLHC_MAG_2_5G =          0x03; //+/-2.5 Gauss
//	public int LSM303DLHC_MAG_4_0G =          0x04; //+/-4.0 Gauss
//	public int LSM303DLHC_MAG_4_7G =          0x05; //+/-4.7 Gauss
//	public int LSM303DLHC_MAG_5_6G =          0x06; //+/-5.6 Gauss
//	public int LSM303DLHC_MAG_8_1G =          0x07; //+/-8.1 Gauss
//
//	//LSM303DLHC Mag sampling rate
//	public int LSM303DLHC_MAG_0_75HZ =        0x00; //0.75 Hz
//	public int LSM303DLHC_MAG_1_5HZ =         0x01; //1.5 Hz
//	public int LSM303DLHC_MAG_3HZ =           0x02; //3.0 Hz
//	public int LSM303DLHC_MAG_7_5HZ =         0x03; //7.5 Hz
//	public int LSM303DLHC_MAG_15HZ =          0x04; //15 Hz
//	public int LSM303DLHC_MAG_30HZ =          0x05; //30 Hz
//	public int LSM303DLHC_MAG_75HZ =          0x06; //75 Hz
//	public int LSM303DLHC_MAG_220HZ =         0x07; //220 Hz
//
//
//	//calibration info
//	public int S_ACCEL =                      0;
//	public int S_GYRO =                       1;
//	public int S_MAG =                        2;
//	public int S_ACCEL_A =                    3;
//	public int S_MPL_ACCEL =                  4;
//	public int S_MPL_MAG =                    5;
//	public int S_MPL_GYRO =                   6;
//	//public int S_ECG =                     3;
//	//public int S_EMG =                     4;
//
//	//MPU9150 Gyro range
//	public int MPU9150_GYRO_250DPS =          0x00; //+/-250 dps
//	public int MPU9150_GYRO_500DPS =          0x01; //+/-500 dps
//	public int MPU9150_GYRO_1000DPS =         0x02; //+/-1000 dps
//	public int MPU9150_GYRO_2000DPS =         0x03; //+/-2000 dps
//
//	//#digital accel_range
//	public int RANGE_2G =                     0;
//	public int RANGE_4G =                     1;
//	public int RANGE_8G =                     2;
//	public int RANGE_16G =                    3;
//
//	//#mag_gain
//	public int LSM303_MAG_13GA =              1;
//	public int LSM303_MAG_19GA =              2;
//	public int LSM303_MAG_25GA =              3;
//	public int LSM303_MAG_40GA =              4;
//	public int LSM303_MAG_47GA =              5;
//	public int LSM303_MAG_56GA =              6;
//	public int LSM303_MAG_81GA =              7;
//
//	// MPU Low Pass filter cut-off
//	public int MPU9150_LPF_256HZ_NOLPF2 =     0x00;
//	public int MPU9150_LPF_188HZ =            0x01;
//	public int MPU9150_LPF_98HZ =             0x02;
//	public int MPU9150_LPF_42HZ =             0x03;
//	public int MPU9150_LPF_20HZ =             0x04;
//	public int MPU9150_LPF_10HZ =             0x05;
//	public int MPU9150_LPF_5HZ =              0x06;
//	public int MPU9150_LPF_2500HZ_NOLPF =     0x07;
//
//	// On-the-fly gyro calibration settings
//	public int MPL_MOT_CAL_OFF =              0x00;
//	public int MPL_MOT_CAL_FAST_NO_MOT =      0x01;
//	public int MPL_MOT_CAL_MOT_NO_MOT_1S =    0x02;
//	public int MPL_MOT_CAL_MOT_NO_MOT_2S =    0x03;
//	public int MPL_MOT_CAL_MOT_NO_MOT_5S =    0x04;
//	public int MPL_MOT_CAL_MOT_NO_MOT_10S =   0x05;
//	public int MPL_MOT_CAL_MOT_NO_MOT_30S =   0x06;
//	public int MPL_MOT_CAL_MOT_NO_MOT_60S =   0x07;
//
//	public int MPL_RATE_10HZ =                0x00;
//	public int MPL_RATE_20HZ =                0x01;
//	public int MPL_RATE_40HZ =                0x02;
//	public int MPL_RATE_50HZ =                0x03;
//	public int MPL_RATE_100HZ =               0x04;
//	//public int MPL_RATE_200HZ =             0x05;
//
//	//Pansenti 9DOF MagMix
//	public int GYRO_ONLY =                    0x00;
//	public int MAG_ONLY =                     0x01;
//	public int GYRO_AND_MAG =                 0x02;
//	public int GYRO_AND_SOME_MAG =            0x03;
	
	public ConfigByteLayoutShimmer3() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * Hold the Shimmer3's microcontroller information memory layout. This
	 * region of the the microcontrollers RAM can be used to configure all
	 * properties of the Shimmer when configured through a docking station using
	 * Consensys. Variables stored in this class are based on firmware header
	 * files for mapping which bits in each information memory byte represents
	 * various configurable settings on the Shimmer.
	 * 
	 * @param firmwareIdentifier
	 * @param firmwareVersionMajor
	 * @param firmwareVersionMinor
	 * @param firmwareVersionInternal
	 */
	public ConfigByteLayoutShimmer3(int firmwareIdentifier, int firmwareVersionMajor, int firmwareVersionMinor, int firmwareVersionInternal) {
		mShimmerVerObject = new ShimmerVerObject(firmwareIdentifier, firmwareVersionMajor, firmwareVersionMinor, firmwareVersionInternal);
		
		mInfoMemSize = calculateConfigByteLength();

		//Include changes to mapping below in order of oldest to newest in separate "if statements"
		
		if((compareVersions(FW_ID.SDLOG,0,8,42))
				||(compareVersions(FW_ID.LOGANDSTREAM,0,3,4))
				||(compareVersions(FW_ID.SHIMMER4_SDK_STOCK,ShimmerVerDetails.ANY_VERSION,ShimmerVerDetails.ANY_VERSION,ShimmerVerDetails.ANY_VERSION))) {
			idxSensors3 =			128+0;
			idxSensors4 =			128+1;
			idxConfigSetupByte4 =	128+2;
			idxConfigSetupByte5 =	128+3;
			idxConfigSetupByte6 =	128+4;
			idxDerivedSensors0 = 115;
			idxDerivedSensors1 = 116;
			idxDerivedSensors2 = 117;
		}
		
		if((compareVersions(FW_ID.SDLOG,0,8,68))
				||(compareVersions(FW_ID.LOGANDSTREAM,0,3,17))
				||(compareVersions(FW_ID.BTSTREAM,0,6,0))
				||(compareVersions(FW_ID.SHIMMER4_SDK_STOCK,ShimmerVerDetails.ANY_VERSION,ShimmerVerDetails.ANY_VERSION,ShimmerVerDetails.ANY_VERSION))) {
			idxDerivedSensors0 =		    31;
			idxDerivedSensors1 =		    32;
			idxDerivedSensors2 =		    33;
			idxAnalogAccelCalibration =		34;
			idxMPU9150GyroCalibration =     55;
			idxLSM303DLHCMagCalibration =   76;
			idxLSM303DLHCAccelCalibration = 97;
		}

		if((compareVersions(FW_ID.SDLOG,0,11,3))
				||(compareVersions(FW_ID.LOGANDSTREAM,0,5,12))
				||(compareVersions(FW_ID.SHIMMER4_SDK_STOCK,ShimmerVerDetails.ANY_VERSION,ShimmerVerDetails.ANY_VERSION,ShimmerVerDetails.ANY_VERSION))) {
			maskShowErrorLedsRwc =	 		0x01;
		}
		
		if((compareVersions(FW_ID.SDLOG,0,11,5))
				||(compareVersions(FW_ID.LOGANDSTREAM,0,5,16))
				||(compareVersions(FW_ID.BTSTREAM,0,7,4))
				||(compareVersions(FW_ID.SHIMMER4_SDK_STOCK,ShimmerVerDetails.ANY_VERSION,ShimmerVerDetails.ANY_VERSION,ShimmerVerDetails.ANY_VERSION))) {
			MSP430_5XX_INFOMEM_D_ADDRESS = 0; 
			MSP430_5XX_INFOMEM_C_ADDRESS = 128; 
			MSP430_5XX_INFOMEM_B_ADDRESS = 256;
			MSP430_5XX_INFOMEM_A_ADDRESS = 384; 
			MSP430_5XX_INFOMEM_LAST_ADDRESS = 511;
		}
		
		if(mShimmerVerObject.isSupportedEightByteDerivedSensors()){
			idxDerivedSensors3 =		    		118;
			idxDerivedSensors4 =		    		119;
			idxDerivedSensors5 =		    		120;
			idxDerivedSensors6 =		    		121;
			idxDerivedSensors7 =		    		122;
		}

		if(compareVersions(FW_ID.LOGANDSTREAM,0,7,12)) {
			maskShowErrorLedsSd = 0x01;
		}
		
		if(compareVersions(FW_ID.LOGANDSTREAM,0,8,1)) {
			idxBtFactoryReset =            		128+103;
		}

//		if(Util.compareVersions(mFirmwareIdentifier,mFirmwareVersionMajor,mFirmwareVersionMinor,mFirmwareVersionInternal,FW_ID.SDLOG,0,10,1)){
//			idxMplCalibration = 128+128+128+0;
//		}

	}
	
	public int calculateConfigByteLength(ShimmerVerObject shimmerVersionObject) {
		
		//TODO: should add full FW version checking here to support different size InfoMems in the future
//		if(Util.compareVersions(firmwareIdentifier, firmwareVersionMajor, firmwareVersionMinor, firmwareVersionRelease,
//				FW_ID.SDLOG, 0, 10, 1)) {
//			return 512;
//		}
		
//		if(firmwareIdentifier == FW_ID.SDLOG) {
//			return 384;
//		}
//		else if(firmwareIdentifier == FW_ID.BTSTREAM) {
//			return 128;
//		}
//		else if(firmwareIdentifier == FW_ID.LOGANDSTREAM) {
//			return 384;
//		}
//		else if(firmwareIdentifier == FW_ID.GQ_GSR) {
//			return 128;
//		}
//		else {
//			return 512; 
//		}
		
		return 384;
	}

	
	
	@Override
	public HashMap<Integer, String> getMapOfByteDescriptions(){
		HashMap<Integer, String> mapOfByteDescriptions = new HashMap<Integer, String>();
		
		mapOfByteDescriptions.put(idxShimmerSamplingRate, "SamplingRate_LSB");
		mapOfByteDescriptions.put(idxShimmerSamplingRate+1, "SamplingRate_MSB");
		mapOfByteDescriptions.put(idxBufferSize, "BufferSize");
		mapOfByteDescriptions.put(idxSensors0, "Sensors0");
		mapOfByteDescriptions.put(idxSensors1, "Sensors1");
		mapOfByteDescriptions.put(idxSensors2, "Sensors2");
		mapOfByteDescriptions.put(idxConfigSetupByte0, "ConfigSetupByte0");
		mapOfByteDescriptions.put(idxConfigSetupByte1, "ConfigSetupByte1");
		mapOfByteDescriptions.put(idxConfigSetupByte2, "ConfigSetupByte2");
		mapOfByteDescriptions.put(idxConfigSetupByte3, "ConfigSetupByte3");
		
		mapOfByteDescriptions.put(idxEXGADS1292RChip1Config1, "EXGADS1292RChip1Config1");
		mapOfByteDescriptions.put(idxEXGADS1292RChip1Config2, "EXGADS1292RChip1Config2");
		mapOfByteDescriptions.put(idxEXGADS1292RChip1LOff, "EXGADS1292RChip1LOff");
		mapOfByteDescriptions.put(idxEXGADS1292RChip1Ch1Set, "EXGADS1292RChip1Ch1Set");
		mapOfByteDescriptions.put(idxEXGADS1292RChip1Ch2Set, "EXGADS1292RChip1Ch2Set");
		mapOfByteDescriptions.put(idxEXGADS1292RChip1RldSens, "EXGADS1292RChip1RldSens");
		mapOfByteDescriptions.put(idxEXGADS1292RChip1LOffSens, "EXGADS1292RChip1LOffSens");
		mapOfByteDescriptions.put(idxEXGADS1292RChip1LOffStat, "EXGADS1292RChip1LOffStat");
		mapOfByteDescriptions.put(idxEXGADS1292RChip1Resp1, "EXGADS1292RChip1Resp1");
		mapOfByteDescriptions.put(idxEXGADS1292RChip1Resp2, "EXGADS1292RChip1Resp2");
		mapOfByteDescriptions.put(idxEXGADS1292RChip2Config1, "EXGADS1292RChip2Config1");
		mapOfByteDescriptions.put(idxEXGADS1292RChip2Config2, "EXGADS1292RChip2Config2");
		mapOfByteDescriptions.put(idxEXGADS1292RChip2LOff, "EXGADS1292RChip2LOff");
		mapOfByteDescriptions.put(idxEXGADS1292RChip2Ch1Set, "EXGADS1292RChip2Ch1Set");
		mapOfByteDescriptions.put(idxEXGADS1292RChip2Ch2Set, "EXGADS1292RChip2Ch2Set");
		mapOfByteDescriptions.put(idxEXGADS1292RChip2RldSens, "EXGADS1292RChip2RldSens");
		mapOfByteDescriptions.put(idxEXGADS1292RChip2LOffSens, "EXGADS1292RChip2LOffSens");
		mapOfByteDescriptions.put(idxEXGADS1292RChip2LOffStat, "EXGADS1292RChip2LOffStat");
		mapOfByteDescriptions.put(idxEXGADS1292RChip2Resp1, "EXGADS1292RChip2Resp1");
		mapOfByteDescriptions.put(idxEXGADS1292RChip2Resp2, "EXGADS1292RChip2Resp2");

		mapOfByteDescriptions.put(idxBtCommBaudRate, "idxBtCommBaudRate");
		mapOfByteDescriptions.put(idxAnalogAccelCalibration, "idxAnalogAccelCalibration");
		mapOfByteDescriptions.put(idxMPU9150GyroCalibration, "idxMPU9150GyroCalibration");
		mapOfByteDescriptions.put(idxLSM303DLHCMagCalibration, "idxLSM303DLHCMagCalibration");
		mapOfByteDescriptions.put(idxLSM303DLHCAccelCalibration, "idxLSM303DLHCAccelCalibration");

		if(idxDerivedSensors0>0){
			mapOfByteDescriptions.put(idxDerivedSensors0, "DerivedSensors0");
		}
		if(idxDerivedSensors1>0){
			mapOfByteDescriptions.put(idxDerivedSensors1, "DerivedSensors1");
		}
		if(idxDerivedSensors2>0){
			mapOfByteDescriptions.put(idxDerivedSensors2, "DerivedSensors2");
		}
		if(idxDerivedSensors3>0){
			mapOfByteDescriptions.put(idxDerivedSensors3, "DerivedSensors3");
		}
		if(idxDerivedSensors4>0){
			mapOfByteDescriptions.put(idxDerivedSensors4, "DerivedSensors4");
		}
		if(idxDerivedSensors5>0){
			mapOfByteDescriptions.put(idxDerivedSensors5, "DerivedSensors5");
		}
		if(idxDerivedSensors6>0){
			mapOfByteDescriptions.put(idxDerivedSensors6, "DerivedSensors6");
		}
		if(idxDerivedSensors7>0){
			mapOfByteDescriptions.put(idxDerivedSensors7, "DerivedSensors7");
		}

		mapOfByteDescriptions.put(idxConfigSetupByte4, "idxConfigSetupByte4");
		mapOfByteDescriptions.put(idxConfigSetupByte5, "idxConfigSetupByte5");
		mapOfByteDescriptions.put(idxSensors3, "idxSensors3");
		mapOfByteDescriptions.put(idxSensors4, "idxSensors4");
		mapOfByteDescriptions.put(idxConfigSetupByte6, "idxConfigSetupByte6");
		mapOfByteDescriptions.put(idxMPLAccelCalibration, "idxMPLAccelCalibration");
		mapOfByteDescriptions.put(idxMPLMagCalibration, "idxMPLMagCalibration");
		mapOfByteDescriptions.put(idxMPLGyroCalibration, "idxMPLGyroCalibration");

		mapOfByteDescriptions.put(idxSDShimmerName, "SDShimmerName");
		mapOfByteDescriptions.put(idxSDEXPIDName, "SDEXPIDName");
		mapOfByteDescriptions.put(idxSDConfigTime0, "SDConfigTime0");
		mapOfByteDescriptions.put(idxSDConfigTime1, "SDConfigTime1");
		mapOfByteDescriptions.put(idxSDConfigTime2, "SDConfigTime2");
		mapOfByteDescriptions.put(idxSDConfigTime3, "SDConfigTime3");
		
		mapOfByteDescriptions.put(idxSDMyTrialID, "idxSDMyTrialID");
		mapOfByteDescriptions.put(idxSDNumOfShimmers, "idxSDNumOfShimmers");
		mapOfByteDescriptions.put(idxSDExperimentConfig0, "idxSDExperimentConfig0");
		mapOfByteDescriptions.put(idxSDExperimentConfig1, "idxSDExperimentConfig1");
		mapOfByteDescriptions.put(idxSDBTInterval, "idxSDBTInterval");
		mapOfByteDescriptions.put(idxEstimatedExpLengthMsb, "idxEstimatedExpLengthMsb");

		mapOfByteDescriptions.put(idxEstimatedExpLengthLsb, "idxEstimatedExpLengthLsb");
		mapOfByteDescriptions.put(idxMaxExpLengthMsb, "idxMaxExpLengthMsb");
		mapOfByteDescriptions.put(idxMaxExpLengthLsb, "idxMaxExpLengthLsb");
		mapOfByteDescriptions.put(idxMacAddress, "idxMacAddress");
		mapOfByteDescriptions.put(idxSDConfigDelayFlag, "idxSDConfigDelayFlag");
		
		if(idxBtFactoryReset>0){
			mapOfByteDescriptions.put(idxBtFactoryReset, "idxBtFactoryReset");
		}
		
		mapOfByteDescriptions.put(idxNode0, "Node0");
		
		return mapOfByteDescriptions;
	}
	
}
