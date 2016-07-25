package com.shimmerresearch.driverUtilities;

public class CalibDetailsBmp180 extends CalibDetails {

	/** * */
	private static final long serialVersionUID = 6119627638201576905L;

	public double pressTempAC1 = 408;
	public double pressTempAC2 = -72;
	public double pressTempAC3 = -14383;
	public double pressTempAC4 = 332741;
	public double pressTempAC5 = 32757;
	public double pressTempAC6 = 23153;
	public double pressTempB1 = 6190;
	public double pressTempB2 = 4;
	public double pressTempMB = -32767;
	public double pressTempMC = -8711;
	public double pressTempMD = 2868;
	

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

	@Override
	public void resetToDefaultParameters() {
		// TODO Auto-generated method stub
		
	}

}
