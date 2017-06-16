package com.shimmerresearch.sensors.bmpX80;

import com.shimmerresearch.driverUtilities.UtilParseData;
import com.shimmerresearch.driverUtilities.UtilShimmer;

public class CalibDetailsBmp180 extends CalibDetailsBmpX80 {

	/** * */
	private static final long serialVersionUID = 6119627638201576905L;

	public double AC1 = 408;
	public double AC2 = -72;
	public double AC3 = -14383;
	public double AC4 = 332741;
	public double AC5 = 32757;
	public double AC6 = 23153;
	public double B1 = 6190;
	public double B2 = 4;
	public double MB = -32767;
	public double MC = -8711;
	public double MD = 2868;
	
	//JC HACK
//	public double AC1 = 8489;
//	public double AC2 = -1302;
//	public double AC3 = -14539;
//	public double AC4 = 34333;
//	public double AC5 = 25111;
//	public double AC6 = 14686;
//	public double B1 = 6515;
//	public double B2 = 56;
//	public double MB = -32768;
//	public double MC = -11786;
//	public double MD = 2752;


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

			AC1 = UtilParseData.calculatetwoscomplement((int)((int)(pressureResoRes[1] & 0xFF) + ((int)(pressureResoRes[0] & 0xFF) << 8)),16);
			AC2 = UtilParseData.calculatetwoscomplement((int)((int)(pressureResoRes[3] & 0xFF) + ((int)(pressureResoRes[2] & 0xFF) << 8)),16);
			AC3 = UtilParseData.calculatetwoscomplement((int)((int)(pressureResoRes[5] & 0xFF) + ((int)(pressureResoRes[4] & 0xFF) << 8)),16);
			AC4 = (int)((int)(pressureResoRes[7] & 0xFF) + ((int)(pressureResoRes[6] & 0xFF) << 8));
			AC5 = (int)((int)(pressureResoRes[9] & 0xFF) + ((int)(pressureResoRes[8] & 0xFF) << 8));
			AC6 = (int)((int)(pressureResoRes[11] & 0xFF) + ((int)(pressureResoRes[10] & 0xFF) << 8));
			B1 = UtilParseData.calculatetwoscomplement((int)((int)(pressureResoRes[13] & 0xFF) + ((int)(pressureResoRes[12] & 0xFF) << 8)),16);
			B2 = UtilParseData.calculatetwoscomplement((int)((int)(pressureResoRes[15] & 0xFF) + ((int)(pressureResoRes[14] & 0xFF) << 8)),16);
			MB = UtilParseData.calculatetwoscomplement((int)((int)(pressureResoRes[17] & 0xFF) + ((int)(pressureResoRes[16] & 0xFF) << 8)),16);
			MC = UtilParseData.calculatetwoscomplement((int)((int)(pressureResoRes[19] & 0xFF) + ((int)(pressureResoRes[18] & 0xFF) << 8)),16);
			MD = UtilParseData.calculatetwoscomplement((int)((int)(pressureResoRes[21] & 0xFF) + ((int)(pressureResoRes[20] & 0xFF) << 8)),16);
		}
	}

	@Override
	public void resetToDefaultParameters() {
		// TODO Auto-generated method stub
		
	}

	public void setPressureCalib(
			double AC1, double AC2, double AC3, 
			double AC4, double AC5, double AC6, 
			double B1, double B2, 
			double MB, double MC, double MD) {
		this.AC1 = AC1;
		this.AC2 = AC2;
		this.AC3 = AC3;
		this.AC4 = AC4;
		this.AC5 = AC5;
		this.AC6 = AC6;
		this.B1 = B1;
		this.B2 = B2;
		this.MB = MB;
		this.MC = MC;
		this.MD = MD;
	}

	@Override
	public double[] calibratePressureSensorData(double UP, double UT){
		//Calculate the true temperature
		double X1 = (UT - AC6) * (AC5 / 32768);
		double X2 = (MC * 2048 / (X1 + MD));
		double B5 = X1 + X2;
		double T = (B5 + 8) / 16;
		
		double B6 = B5 - 4000;
		X1 = (B2 * (Math.pow(B6,2)/ 4096)) / 2048;
		X2 = AC2 * B6 / 2048;
		double X3 = X1 + X2;
		double B3 = (((AC1 * 4 + X3)*(1<<mRangeValue) + 2)) / 4;
		X1 = AC3 * B6 / 8192;
		X2 = (B1 * (Math.pow(B6,2)/ 4096)) / 65536;
		X3 = ((X1 + X2) + 2) / 4;
		double B4 = AC4 * (X3 + 32768) / 32768;
		double B7 = (UP - B3) * (50000>>mRangeValue);
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

}
