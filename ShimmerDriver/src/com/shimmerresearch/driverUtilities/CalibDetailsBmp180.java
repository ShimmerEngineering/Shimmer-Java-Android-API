package com.shimmerresearch.driverUtilities;

public class CalibDetailsBmp180 extends CalibDetails {

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

}
