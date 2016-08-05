package com.shimmerresearch.driverUtilities;

import java.io.Serializable;
import java.util.Arrays;

import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_TYPE;

/**
 * Class that holds the calibration parameters for a particular range in a
 * Kinematic sensor
 * 
 * @author Mark Nolan
 *
 */
public class CalibDetailsKinematic extends CalibDetails implements Serializable {
	
	/** * */
	private static final long serialVersionUID = -3556098650349506733L;
	
	public class CALIB_KINEMATIC_PARAM{
		public static final String OFFSET_B0 = "b0";
		public static final String OFFSET_B1 = "b1";
		public static final String OFFSET_B2 = "b2";
		public static final String SENSITIVITY_K0 = "k0";
		public static final String SENSITIVITY_K1 = "k1";
		public static final String SENSITIVITY_K2 = "k2";
		public static final String ALIGNMENT_R00 = "r00";
		public static final String ALIGNMENT_R01 = "r01";
		public static final String ALIGNMENT_R02 = "r02";
		public static final String ALIGNMENT_R10 = "r10";
		public static final String ALIGNMENT_R11 = "r11";
		public static final String ALIGNMENT_R12 = "r12";
		public static final String ALIGNMENT_R20 = "r20";
		public static final String ALIGNMENT_R21 = "r21";
		public static final String ALIGNMENT_R22 = "r22";
	}
	
	private double[][] mCurrentAlignmentMatrix = null; 			
	private double[][] mCurrentSensitivityMatrix = null; 	
	private double[][] mCurrentOffsetVector = null;

	private double[][] mDefaultAlignmentMatrix = null;   			
	private double[][] mDefaultSensitivityMatrix = null;  	
	private double[][] mDefaultOffsetVector = null; 
	
	//TODO: improve below, needed here?
	private double[][] mEmptyAlignmentMatrix = new double[][]{{0.0, 0.0, 0.0}, {0.0, 0.0, 0.0}, {0.0, 0.0, 0.0}};  			
	private double[][] mEmptySensitivityMatrix = new double[][]{{0.0, 0.0, 0.0}, {0.0, 0.0, 0.0}, {0.0, 0.0, 0.0}}; 	
	private double[][] mEmptyOffsetVector = new double[][]{{0.0, 0.0, 0.0}, {0.0, 0.0, 0.0}, {0.0, 0.0, 0.0}}; 
	
	public enum CALIBRATION_SCALE_FACTOR{
		NONE(1.0),
		TEN(10.0),
		ONE_HUNDRED(100.0);
		
		double scaleFactor = 1.0;
		private CALIBRATION_SCALE_FACTOR(double scaleFactor) {
			this.scaleFactor = scaleFactor;
		}
	}
	//TODO offset scale factor currently not used
	private CALIBRATION_SCALE_FACTOR mOffsetScaleFactor = CALIBRATION_SCALE_FACTOR.NONE;
	private CALIBRATION_SCALE_FACTOR mSensitivityScaleFactor = CALIBRATION_SCALE_FACTOR.NONE;
	private CALIBRATION_SCALE_FACTOR mAlignmentScaleFactor = CALIBRATION_SCALE_FACTOR.ONE_HUNDRED;
	
	private CHANNEL_DATA_TYPE mAlignmentDataType = CHANNEL_DATA_TYPE.INT8; 
	public double mAlignmentMax = 0.0;
	public double mAlignmentMin = 0.0;
	public int mAlignmentPrecision = 2;
	
	private CHANNEL_DATA_TYPE mSensitivityDataType = CHANNEL_DATA_TYPE.INT16; 
	public double mSensitivityMax = 0.0;
	public double mSensitivityMin = 0.0;  
	public int mSensitivityPrecision = 0;
	
	private CHANNEL_DATA_TYPE mOffsetDataType = CHANNEL_DATA_TYPE.INT16; 
	public double mOffsetMax = 0.0;
	public double mOffsetMin = 0.0;  
	public int mOffsetPrecision = 0;
	
	public CalibDetailsKinematic(int rangeValue, String rangeString) {
		this.mRangeValue = rangeValue;
		this.mRangeString = rangeString;
		
		updateOffsetMaxMin();
		updateSensitivityMaxMin();
		updateAlignmentMaxMin();
	}

	public CalibDetailsKinematic(int rangeValue, String rangeString, double[][] defaultAlignmentMatrix, double[][] defaultSensitivityMatrix, double[][] defaultOffsetVector) {
		this(rangeValue, rangeString);
		this.setDefaultValues(defaultAlignmentMatrix, defaultSensitivityMatrix, defaultOffsetVector);
	}

	public CalibDetailsKinematic(int rangeValue, String rangeString, 
			double[][] defaultAlignmentMatrix, double[][] defaultSensitivityMatrix, double[][] defaultOffsetVector,
			double[][] currentAlignmentMatrix, double[][] currentSensitivityMatrix, double[][] currentOffsetVector) {
		this(rangeValue, rangeString, defaultAlignmentMatrix, defaultSensitivityMatrix, defaultOffsetVector);
		this.setCurrentValues(currentAlignmentMatrix, currentSensitivityMatrix, currentOffsetVector);
	}

	public CalibDetailsKinematic(int rangeValue, String rangeString, 
			double[][] defaultAlignmentMatrix, double[][] defaultSensitivityMatrix, double[][] defaultOffsetVector,
			CALIBRATION_SCALE_FACTOR sensitivityScaleFactor) {
		this(rangeValue, rangeString, defaultAlignmentMatrix, defaultSensitivityMatrix, defaultOffsetVector);
		setSensitivityScaleFactor(sensitivityScaleFactor);
	}

	
	/**NOT CURRENTLY USED and could cause problems unless used for a specific purpose as the defaults param etc. will not be set
	 * @param bufferCalibrationParameters
	 */
	public CalibDetailsKinematic(byte[] bufferCalibrationParameters) {
		parseCalParamByteArray(bufferCalibrationParameters, CALIB_READ_SOURCE.UNKNOWN);
	}


	public void setCurrentValues(double[][] currentAlignmentMatrix, double[][] currentSensitivityMatrix, double[][] currentOffsetVector) {
		setCurrentAlignmentMatrix(currentAlignmentMatrix);
		setCurrentSensitivityMatrix(currentSensitivityMatrix);
		setCurrentOffsetVector(currentOffsetVector);
		
//		System.out.println(generateDebugString());
	}

	public void setDefaultValues(double[][] defaultAlignmentMatrix, double[][] defaultSensitivityMatrix, double[][] defaultOffsetVector) {
		this.mDefaultAlignmentMatrix = defaultAlignmentMatrix;
		this.mDefaultSensitivityMatrix = defaultSensitivityMatrix;
		this.mDefaultOffsetVector = defaultOffsetVector;
	}

	@Override
	public void resetToDefaultParameters(){
		super.resetToDefaultParametersCommon();
		
		setCurrentAlignmentMatrix(UtilShimmer.deepCopyDoubleMatrix(mDefaultAlignmentMatrix));
		setCurrentSensitivityMatrix(UtilShimmer.deepCopyDoubleMatrix(mDefaultSensitivityMatrix));
		setCurrentOffsetVector(UtilShimmer.deepCopyDoubleMatrix(mDefaultOffsetVector));
	}
	
	
	public boolean isCurrentValuesSet(){
		if(mCurrentAlignmentMatrix!=null && mCurrentSensitivityMatrix!=null && mCurrentOffsetVector!=null){
			return true;
		}
		return false;
	}

	public boolean isUsingDefaultParameters(){
		if(isAlignmentUsingDefault() && isSensitivityUsingDefault() && isOffsetVectorUsingDefault()){
			return true;
		}
		return false;
	}
	
	public boolean isAlignmentUsingDefault(){
		if(mCurrentAlignmentMatrix==null || mDefaultAlignmentMatrix==null){
			return false;
		}
		return Arrays.deepEquals(mCurrentAlignmentMatrix, mDefaultAlignmentMatrix);
	}

	public boolean isSensitivityUsingDefault(){
		if(mCurrentSensitivityMatrix==null || mDefaultSensitivityMatrix==null){
			return false;
		}
		return Arrays.deepEquals(mCurrentSensitivityMatrix, mDefaultSensitivityMatrix);
	}

	public boolean isOffsetVectorUsingDefault(){
		if(mCurrentOffsetVector==null || mDefaultOffsetVector==null){
			return false;
		}
		return Arrays.deepEquals(mCurrentOffsetVector, mDefaultOffsetVector);
	}

	public boolean isAllCalibrationValid(){
		if((isAlignmentValid() && isSensitivityValid() && isOffsetVectorValid()) || isUsingDefaultParameters()){
			return true;
		}
		return false;
	}
	
	public boolean isAlignmentValid(){
	 if(UtilShimmer.isAllZeros(mCurrentAlignmentMatrix)){
			return false;
		}else{
			return true;
		}
	}

	public boolean isSensitivityValid(){
	 if(isDiagonalFilled(mCurrentSensitivityMatrix) ){
			return true;
		}else{
			return false;
		}
	}
	
	//TODO : look up is there a valid check for a valid offset vector 
	public boolean isOffsetVectorValid(){
			return true;
	}
	
	public boolean isDiagonalFilled(double[][] matrix){
		if(matrix==null){
			return false;
		}
		
		boolean diagonalFilled = true;
		for(int j = 0; j < matrix[1].length; j++){
			if(matrix[0][j]==0 &&  matrix[1][j]==0 && matrix[2][j]==0){
				diagonalFilled =false;
			}
		}
		return diagonalFilled;
	}

	@Override
	public void parseCalParamByteArray(byte[] bufferCalibrationParameters, CALIB_READ_SOURCE calibReadSource){
		if(calibReadSource.ordinal()>=getCalibReadSource().ordinal()){
			if(UtilShimmer.isAllFF(bufferCalibrationParameters)
					||UtilShimmer.isAllZeros(bufferCalibrationParameters)){
				return;
			}
			
			setCalibReadSource(calibReadSource);
			
			String[] dataType={"i16","i16","i16","i16","i16","i16","i8","i8","i8","i8","i8","i8","i8","i8","i8"};
			int[] formattedPacket = UtilParseData.formatDataPacketReverse(bufferCalibrationParameters,dataType);
			double[] AM=new double[9];
			for (int i=0;i<9;i++) {
				AM[i]=((double)formattedPacket[6+i])/mAlignmentScaleFactor.scaleFactor;
			}
			double[][] alignmentMatrix = {{AM[0],AM[1],AM[2]},{AM[3],AM[4],AM[5]},{AM[6],AM[7],AM[8]}}; 				
			double[][] sensitivityMatrix = {{formattedPacket[3],0,0},{0,formattedPacket[4],0},{0,0,formattedPacket[5]}}; 
			double[][] offsetVector = {{formattedPacket[0]},{formattedPacket[1]},{formattedPacket[2]}};
			
			for(int i=0;i<=2;i++){
				sensitivityMatrix[i][i] = sensitivityMatrix[i][i]/mSensitivityScaleFactor.scaleFactor;
			}
			
			setCurrentAlignmentMatrix(alignmentMatrix);
			setCurrentSensitivityMatrix(sensitivityMatrix); 	
			setCurrentOffsetVector(offsetVector);
		}
	}

	@Override
	public byte[] generateCalParamByteArray() {
		if(isCurrentValuesSet()){
			return generateCalParamByteArray(mCurrentOffsetVector, mCurrentSensitivityMatrix, mCurrentAlignmentMatrix);
		}
		else{
			return generateCalParamByteArray(mDefaultOffsetVector, mDefaultSensitivityMatrix, mDefaultAlignmentMatrix);
		}
	}
	
	public byte[] generateCalParamByteArray(double[][] offsetVector, double[][] sensitivityMatrix, double[][] alignmentMatrix) {
		
		//Scale the sensitivity if needed
		double[][] sensitivityMatrixToUse = UtilShimmer.deepCopyDoubleMatrix(sensitivityMatrix);
		for(int i=0;i<=2;i++){
			sensitivityMatrixToUse[i][i] = Math.round(sensitivityMatrixToUse[i][i]*mSensitivityScaleFactor.scaleFactor);
		}

		//Scale the alignment by 100
		double[][] alignmentMatrixToUse = UtilShimmer.deepCopyDoubleMatrix(alignmentMatrix);
		for(int i=0;i<=2;i++){
			alignmentMatrixToUse[i][0] = Math.round(alignmentMatrixToUse[i][0]*mAlignmentScaleFactor.scaleFactor);
			alignmentMatrixToUse[i][1] = Math.round(alignmentMatrixToUse[i][1]*mAlignmentScaleFactor.scaleFactor);
			alignmentMatrixToUse[i][2] = Math.round(alignmentMatrixToUse[i][2]*mAlignmentScaleFactor.scaleFactor);
		}
		
		//Generate the calibration bytes
		byte[] bufferCalibParam = new byte[21];
		// offsetVector -> buffer offset = 0
		for (int i=0; i<3; i++) {
			bufferCalibParam[0+(i*2)] = (byte) ((((int)offsetVector[i][0]) >> 8) & 0xFF);
			bufferCalibParam[0+(i*2)+1] = (byte) ((((int)offsetVector[i][0]) >> 0) & 0xFF);
		}
		// sensitivityMatrix -> buffer offset = 6
		for (int i=0; i<3; i++) {
			bufferCalibParam[6+(i*2)] = (byte) ((((int)sensitivityMatrixToUse[i][i]) >> 8) & 0xFF);
			bufferCalibParam[6+(i*2)+1] = (byte) ((((int)sensitivityMatrixToUse[i][i]) >> 0) & 0xFF);
		}
		// alignmentMatrix -> buffer offset = 12
		for (int i=0; i<3; i++) {
			bufferCalibParam[12+(i*3)] = (byte) (((int)(alignmentMatrixToUse[i][0])) & 0xFF);
			bufferCalibParam[12+(i*3)+1] = (byte) (((int)(alignmentMatrixToUse[i][1])) & 0xFF);
			bufferCalibParam[12+(i*3)+2] = (byte) (((int)(alignmentMatrixToUse[i][2])) & 0xFF);
		}
		return bufferCalibParam;
	}	
	
	public double[][] getCurrentAlignmentMatrix() {
		if(mCurrentAlignmentMatrix!=null)
			return mCurrentAlignmentMatrix;
		else
			return getDefaultAlignmentMatrix();
	}

	public double[][] getCurrentSensitivityMatrix() {
		if(mCurrentSensitivityMatrix!=null)
			return mCurrentSensitivityMatrix;
		else
			return getDefaultSensitivityMatrix();
	}

	public double[][] getCurrentOffsetVector() {
		if(mCurrentOffsetVector!=null)
			return mCurrentOffsetVector;
		else
			return getDefaultOffsetVector();
	}
	
	public double[][] getEmptyOffsetVector() {
		return mEmptyOffsetVector;
	}

	public double[][] getEmptySensitivityMatrix() {
		return mEmptySensitivityMatrix;
	}

	public double[][] getEmptyAlignmentMatrix() {
		return mEmptyAlignmentMatrix;
	}

	public double[][] getDefaultOffsetVector() {
		return mDefaultOffsetVector;
	}

	public double[][] getDefaultSensitivityMatrix() {
		return mDefaultSensitivityMatrix;
	}

	public double[][] getDefaultAlignmentMatrix() {
		return mDefaultAlignmentMatrix;
	}

	/** For example used by Gyro on the fly calibration
	 * @param mean
	 * @param mean2
	 * @param mean3
	 */
	public void updateCurrentOffsetVector(double XXmean, double XYmean, double XZmean) {
		double[][] newArray = new double[3][1];
		newArray[0][0] = XXmean;
		newArray[1][0] = XYmean;
		newArray[2][0] = XZmean;
		setCurrentOffsetVector(newArray);
	}
	
	private void setCurrentOffsetVector(double[][] newArray) {
		mCurrentOffsetVector = UtilShimmer.nudgeDoubleArray(mOffsetMax, mOffsetMin, mOffsetPrecision, newArray);
//		mCurrentOffsetVector = newArray;
	}

	public void updateCurrentSensitivityMatrix(double XXmean, double XYmean, double XZmean) {
		double[][] newArray = new double[3][3];
		newArray[0][0] = XXmean;
		newArray[1][1] = XYmean;
		newArray[2][2] = XZmean;
		setCurrentSensitivityMatrix(newArray);
	}

	private void setCurrentSensitivityMatrix(double[][] newArray) {
		mCurrentSensitivityMatrix = UtilShimmer.nudgeDoubleArray(mSensitivityMax, mSensitivityMin, mSensitivityPrecision, newArray);
//		mCurrentSensitivityMatrix = newArray;
	}

	public void updateCurrentAlignmentMatrix(
			double XXmean, double XYmean, double XZmean, 
			double YXmean, double YYmean, double YZmean,
			double ZXmean, double ZYmean, double ZZmean) {
		
		double[][] newMatrix = new double[3][3];
		newMatrix[0][0] = XXmean;
		newMatrix[0][1] = XYmean;
		newMatrix[0][2] = XZmean;
		
		newMatrix[1][0] = YXmean;
		newMatrix[1][1] = YYmean;
		newMatrix[1][2] = YZmean;
		
		newMatrix[2][0] = ZXmean;
		newMatrix[2][1] = ZYmean;
		newMatrix[2][2] = ZZmean;
		
		setCurrentAlignmentMatrix(newMatrix);
	}

	private void setCurrentAlignmentMatrix(double[][] newArray) {
		mCurrentAlignmentMatrix = UtilShimmer.nudgeDoubleArray(mAlignmentMax, mAlignmentMin, mAlignmentPrecision, newArray);
//		mCurrentAlignmentMatrix = newMatrix;
	}

	public void setSensitivityScaleFactor(CALIBRATION_SCALE_FACTOR scaleFactor) {
		mSensitivityScaleFactor = scaleFactor;
		setSensitivityPrecision(calculatePrecision(mSensitivityScaleFactor.scaleFactor));
	}

	private void setSensitivityPrecision(int precision) {
		mSensitivityPrecision = precision;
		updateSensitivityMaxMin();
	}

	public void setAlignmentScaleFactor(CALIBRATION_SCALE_FACTOR scaleFactor) {
		mAlignmentScaleFactor = scaleFactor;
		setAlignmentPrecision(calculatePrecision(mAlignmentScaleFactor.scaleFactor));
	}

	private void setAlignmentPrecision(int precision) {
		mAlignmentPrecision = precision;
		updateAlignmentMaxMin();
	}

	public void setOffsetScaleFactor(CALIBRATION_SCALE_FACTOR scaleFactor) {
		mOffsetScaleFactor = scaleFactor;
		setOffsetPrecision(calculatePrecision(mOffsetScaleFactor.scaleFactor));
	}

	private void setOffsetPrecision(int precision) {
		mOffsetPrecision = precision;
		updateOffsetMaxMin();
	}

	private static int calculatePrecision(double scaleFactor) {
		int length = (int)(Math.log10(scaleFactor)+1);
		return length-1;
	}

	private void updateOffsetMaxMin() {
		mOffsetMax = mOffsetDataType.getMaxVal()/mOffsetScaleFactor.scaleFactor;
		mOffsetMax = UtilShimmer.applyPrecisionCorrection(mOffsetMax, mOffsetPrecision);
		
		mOffsetMin = mOffsetDataType.getMinVal()/mOffsetScaleFactor.scaleFactor;
		mOffsetMin = UtilShimmer.applyPrecisionCorrection(mOffsetMin, mOffsetPrecision);
	}

	private void updateSensitivityMaxMin() {
		mSensitivityMax = mSensitivityDataType.getMaxVal()/mSensitivityScaleFactor.scaleFactor;
		mSensitivityMax = UtilShimmer.applyPrecisionCorrection(mSensitivityMax, mSensitivityPrecision);
		
		mSensitivityMin = mSensitivityDataType.getMinVal()/mSensitivityScaleFactor.scaleFactor;
		mSensitivityMin = UtilShimmer.applyPrecisionCorrection(mSensitivityMin, mSensitivityPrecision);
	}
	
	public double getSensitivityScaleFactor() {
		return mSensitivityScaleFactor.scaleFactor;
	}

	private void updateAlignmentMaxMin() {
		mAlignmentMax = mAlignmentDataType.getMaxVal()/mAlignmentScaleFactor.scaleFactor;
		mAlignmentMax = UtilShimmer.applyPrecisionCorrection(mAlignmentMax, mAlignmentPrecision);
		
		mAlignmentMin = mAlignmentDataType.getMinVal()/mAlignmentScaleFactor.scaleFactor;
		mAlignmentMin = UtilShimmer.applyPrecisionCorrection(mAlignmentMin, mAlignmentPrecision);
	}
	
	public String generateDebugString() {
		String debugString = "RangeString:" + mRangeString + "\t" + "RangeValue:" + mRangeValue + "\n";
		debugString += generateDebugStringPerProperty("Default Alignment", mDefaultAlignmentMatrix);
		debugString += generateDebugStringPerProperty("Current Alignment", getCurrentAlignmentMatrix());//mCurrentAlignmentMatrix);
		debugString += generateDebugStringPerProperty("Default Sensitivity", mDefaultSensitivityMatrix);
		debugString += generateDebugStringPerProperty("CurrentSensitivity", getCurrentSensitivityMatrix());//mCurrentSensitivityMatrix);
		debugString += generateDebugStringPerProperty("Default Offset Vector", mDefaultOffsetVector);
		debugString += generateDebugStringPerProperty("Current Offset Vector", getCurrentOffsetVector());//mCurrentOffsetVector);
		return debugString;
	}

	private String generateDebugStringPerProperty(String property, double[][] calMatrix) {
		String debugString = property + " =\n";
		if(calMatrix==null){
			debugString += "NULL\n";
		}
		else{
			debugString += UtilShimmer.doubleArrayToString(calMatrix);
		}
		return debugString;
	}

}
