package com.shimmerresearch.sensors.bmpX80;

import com.shimmerresearch.driverUtilities.UtilParseData;
import com.shimmerresearch.driverUtilities.UtilShimmer;

/**
 * BMP280 Calibration details based on information from 
 * https://cdn-shop.adafruit.com/datasheets/BST-BMP280-DS001-11.pdf
 * and sample parsing code from 
 * https://github.com/ControlEverythingCommunity/BMP280/blob/master/Java/BMP280.java
 * 
 * @author Mark Nolan
 *
 */
public class CalibDetailsBmp280 extends CalibDetailsBmpX80 {

	/** * */
	private static final long serialVersionUID = 3020209119724202014L;

	private static final class DEFAULT_COMPENSATION_VALUES{
		public static double DIG_T1 = 27504;	 	// unsigned short 
		public static double DIG_T2 = 26435;	 	// signed short
		public static double DIG_T3 = -1000;		// signed short
		public static double DIG_P1 = 36477;		// unsigned short 
		public static double DIG_P2 = -10685;		// signed short
		public static double DIG_P3 = 3024;			// signed short
		public static double DIG_P4 = 2855;			// signed short
		public static double DIG_P5 = 140;	 		// signed short
		public static double DIG_P6 = -7;			// signed short
		public static double DIG_P7 = 15500;		// signed short
		public static double DIG_P8 = -14600;		// signed short
		public static double DIG_P9 = 6000;			// signed short
	}
	
	public double dig_T1 = DEFAULT_COMPENSATION_VALUES.DIG_T1; 
	public double dig_T2 = DEFAULT_COMPENSATION_VALUES.DIG_T2;
	public double dig_T3 = DEFAULT_COMPENSATION_VALUES.DIG_T3;
	public double dig_P1 = DEFAULT_COMPENSATION_VALUES.DIG_P1;
	public double dig_P2 = DEFAULT_COMPENSATION_VALUES.DIG_P2;
	public double dig_P3 = DEFAULT_COMPENSATION_VALUES.DIG_P3;
	public double dig_P4 = DEFAULT_COMPENSATION_VALUES.DIG_P4;
	public double dig_P5 = DEFAULT_COMPENSATION_VALUES.DIG_P5;
	public double dig_P6 = DEFAULT_COMPENSATION_VALUES.DIG_P6;
	public double dig_P7 = DEFAULT_COMPENSATION_VALUES.DIG_P7;
	public double dig_P8 = DEFAULT_COMPENSATION_VALUES.DIG_P8;
	public double dig_P9 = DEFAULT_COMPENSATION_VALUES.DIG_P9;
	
	@Override
	public byte[] generateCalParamByteArray() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void parseCalParamByteArray(byte[] pressureResoRes, CALIB_READ_SOURCE calibReadSource) {
		if(calibReadSource.ordinal()>getCalibReadSource().ordinal()){
			if(UtilShimmer.isAllFF(pressureResoRes)
					||UtilShimmer.isAllZeros(pressureResoRes)){
				return;
			}
			
			setPressureRawCoefficients(pressureResoRes);
			setCalibReadSource(calibReadSource);

			dig_T1 = (int)((int)(pressureResoRes[0] & 0xFF) + ((int)(pressureResoRes[1] & 0xFF) << 8));
			dig_T2 = UtilParseData.calculatetwoscomplement((int)((int)(pressureResoRes[2] & 0xFF) + ((int)(pressureResoRes[3] & 0xFF) << 8)),16);
			dig_T3 = UtilParseData.calculatetwoscomplement((int)((int)(pressureResoRes[4] & 0xFF) + ((int)(pressureResoRes[5] & 0xFF) << 8)),16);

			dig_P1 = (int)((int)(pressureResoRes[6] & 0xFF) + ((int)(pressureResoRes[7] & 0xFF) << 8));
			dig_P2 = UtilParseData.calculatetwoscomplement((int)((int)(pressureResoRes[8] & 0xFF) + ((int)(pressureResoRes[9] & 0xFF) << 8)),16);
			dig_P3 = UtilParseData.calculatetwoscomplement((int)((int)(pressureResoRes[10] & 0xFF) + ((int)(pressureResoRes[11] & 0xFF) << 8)),16);
			dig_P4 = UtilParseData.calculatetwoscomplement((int)((int)(pressureResoRes[12] & 0xFF) + ((int)(pressureResoRes[13] & 0xFF) << 8)),16);
			dig_P5 = UtilParseData.calculatetwoscomplement((int)((int)(pressureResoRes[14] & 0xFF) + ((int)(pressureResoRes[15] & 0xFF) << 8)),16);
			dig_P6 = UtilParseData.calculatetwoscomplement((int)((int)(pressureResoRes[16] & 0xFF) + ((int)(pressureResoRes[17] & 0xFF) << 8)),16);
			dig_P7 = UtilParseData.calculatetwoscomplement((int)((int)(pressureResoRes[18] & 0xFF) + ((int)(pressureResoRes[19] & 0xFF) << 8)),16);
			dig_P8 = UtilParseData.calculatetwoscomplement((int)((int)(pressureResoRes[20] & 0xFF) + ((int)(pressureResoRes[21] & 0xFF) << 8)),16);
			dig_P9 = UtilParseData.calculatetwoscomplement((int)((int)(pressureResoRes[22] & 0xFF) + ((int)(pressureResoRes[23] & 0xFF) << 8)),16);
		}
	}

	@Override
	public void resetToDefaultParameters() {
		dig_T1 = DEFAULT_COMPENSATION_VALUES.DIG_T1; 
		dig_T2 = DEFAULT_COMPENSATION_VALUES.DIG_T2;
		dig_T3 = DEFAULT_COMPENSATION_VALUES.DIG_T3;
		dig_P1 = DEFAULT_COMPENSATION_VALUES.DIG_P1;
		dig_P2 = DEFAULT_COMPENSATION_VALUES.DIG_P2;
		dig_P3 = DEFAULT_COMPENSATION_VALUES.DIG_P3;
		dig_P4 = DEFAULT_COMPENSATION_VALUES.DIG_P4;
		dig_P5 = DEFAULT_COMPENSATION_VALUES.DIG_P5;
		dig_P6 = DEFAULT_COMPENSATION_VALUES.DIG_P6;
		dig_P7 = DEFAULT_COMPENSATION_VALUES.DIG_P7;
		dig_P8 = DEFAULT_COMPENSATION_VALUES.DIG_P8;
		dig_P9 = DEFAULT_COMPENSATION_VALUES.DIG_P9;
	}

	public void setPressureCalib(
			double T1, double T2, double T3,
			double P1, double P2, double P3, 
			double P4, double P5, double P6, 
			double P7, double P8, double P9) {
		this.dig_T1 = T1; 
		this.dig_T2 = T2;
		this.dig_T3 = T3;
		this.dig_P1 = P1;
		this.dig_P2 = P2;
		this.dig_P3 = P3;
		this.dig_P4 = P4;
		this.dig_P5 = P5;
		this.dig_P6 = P6;
		this.dig_P7 = P7;
		this.dig_P8 = P8;
		this.dig_P9 = P9;
	}

	@Override
	public double[] calibratePressureSensorData(double UP, double UT){
		double var1 = (UT / 16384.0 - (dig_T1) / 1024.0) * (dig_T2); 
		double var2 = ((UT / 131072.0 - (dig_T1) / 8192.0) * (UT/131072.0 - (dig_T1)/8192.0)) * (dig_T3); 
		double t_fine = var1 + var2; 
		double cTemp = t_fine / 5120.0; //Celsius 
//		double fTemp = cTemp * 1.8 + 32; // Fahrenheit
		
		// Pressure offset calculations
		var1 = (t_fine / 2.0) - 64000.0;
		var2 = var1 * var1 * (dig_P6) / 32768.0;
		var2 = var2 + var1 * (dig_P5) * 2.0;
		var2 = (var2 / 4.0) + ((dig_P4) * 65536.0);
		var1 = (( dig_P3) * var1 * var1 / 524288.0 + ( dig_P2) * var1) / 524288.0;
		var1 = (1.0 + var1 / 32768.0) * (dig_P1);
		double p = 1048576.0 - UP;
		p = (p - (var2 / 4096.0)) * 6250.0 / var1;
		var1 = ( dig_P9) * p * p / 2147483648.0;
		var2 = p * ( dig_P8) / 32768.0;
		double pressure = (p + (var1 + var2 + (dig_P7)) / 16.0) / 100;
		
		double[] caldata = new double[2];
		caldata[0]=pressure;
		caldata[1]=cTemp;///10; // TODO divided by 10 in BMP180, needed here?
		return caldata;
	}
	
	//C code from datasheet

//	// Returns temperature in DegC, resolution is 0.01 DegC. Output value of �5123� equals 51.23 DegC.
//	// t_fine carries fine temperature as global value
//	BMP280_S32_t t_fine;
//	BMP280_S32_t bmp280_compensate_T_int32(BMP280_S32_t adc_T)
//	{
//	BMP280_S32_t var1, var2, T;
//	var1 = ((((adc_T>>3) � ((BMP280_S32_t)dig_T1<<1))) * ((BMP280_S32_t)dig_T2)) >> 11;
//	var2 = (((((adc_T>>4) � ((BMP280_S32_t)dig_T1)) * ((adc_T>>4) � ((BMP280_S32_t)dig_T1))) >> 12) *
//	((BMP280_S32_t)dig_T3)) >> 14;
//	t_fine = var1 + var2;
//	T = (t_fine * 5 + 128) >> 8;
//	return T;
//	}
//	���
//	// Returns pressure in Pa as unsigned 32 bit integer in Q24.8 format (24 integer bits and 8 fractional bits).
//	// Output value of �24674867� represents 24674867/256 = 96386.2 Pa = 963.862 hPa
//	BMP280_U32_t bmp280_compensate_P_int64(BMP280_S32_t adc_P)
//	{
//	BMP280_S64_t var1, var2, p;
//	var1 = ((BMP280_S64_t)t_fine) � 128000;
//	var2 = var1 * var1 * (BMP280_S64_t)dig_P6;
//	var2 = var2 + ((var1*(BMP280_S64_t)dig_P5)<<17);
//	var2 = var2 + (((BMP280_S64_t)dig_P4)<<35);
//	var1 = ((var1 * var1 * (BMP280_S64_t)dig_P3)>>8) + ((var1 * (BMP280_S64_t)dig_P2)<<12);
//	var1 = (((((BMP280_S64_t)1)<<47)+var1))*((BMP280_S64_t)dig_P1)>>33;
//	if (var1 == 0)
//	{
//	return 0; // avoid exception caused by division by zero
//	}
//	p = 1048576-adc_P;
//	p = (((p<<31)-var2)*3125)/var1;
//	var1 = (((BMP280_S64_t)dig_P9) * (p>>13) * (p>>13)) >> 25;
//	var2 = (((BMP280_S64_t)dig_P8) * p) >> 19;
//	p = ((p + var1 + var2) >> 8) + (((BMP280_S64_t)dig_P7)<<4);
//	return (BMP280_U32_t)p;
}
