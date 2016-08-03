package com.shimmerresearch.driverUtilities;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;

import com.shimmerresearch.sensors.SensorMPU9X50;
import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;

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
	
	private double mSensitivityScaleFactor = SENSITIVITY_SCALE_FACTOR.NONE;
	public static final class SENSITIVITY_SCALE_FACTOR{
		public static final double NONE = 1.0;
		public static final double TEN = 10.0;
		public static final double HUNDRED = 100.0;
	}
	
	public CalibDetailsKinematic(int rangeValue, String rangeString) {
		this.mRangeValue = rangeValue;
		this.mRangeString = rangeString;
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

	
	public CalibDetailsKinematic(byte[] bufferCalibrationParameters) {
		parseCalParamByteArray(bufferCalibrationParameters, CALIB_READ_SOURCE.UNKNOWN);
	}

	public CalibDetailsKinematic(int rangeValue, String rangeString, 
			double[][] defaultAlignmentMatrix, double[][] defaultSensitivityMatrix, double[][] defaultOffsetVector,
			double sensitivityScaleFactor) {
		this(rangeValue, rangeString, defaultAlignmentMatrix, defaultSensitivityMatrix, defaultOffsetVector);
		setSensitivityScaleFactor(sensitivityScaleFactor);
	}
	

	public void setCurrentValues(double[][] currentAlignmentMatrix, double[][] currentSensitivityMatrix, double[][] currentOffsetVector) {
		this.mCurrentAlignmentMatrix = currentAlignmentMatrix;
		
//		System.out.println(generateDebugString());

		this.mCurrentSensitivityMatrix = currentSensitivityMatrix;
		this.mCurrentOffsetVector = currentOffsetVector;
	}

	public void setDefaultValues(double[][] defaultAlignmentMatrix, double[][] defaultSensitivityMatrix, double[][] defaultOffsetVector) {
		this.mDefaultAlignmentMatrix = defaultAlignmentMatrix;
		this.mDefaultSensitivityMatrix = defaultSensitivityMatrix;
		this.mDefaultOffsetVector = defaultOffsetVector;
	}

	@Override
	public void resetToDefaultParameters(){
		super.resetToDefaultParametersCommon();
		
		mCurrentAlignmentMatrix = UtilShimmer.deepCopyDoubleMatrix(mDefaultAlignmentMatrix);
		mCurrentSensitivityMatrix = UtilShimmer.deepCopyDoubleMatrix(mDefaultSensitivityMatrix);
		mCurrentOffsetVector = UtilShimmer.deepCopyDoubleMatrix(mDefaultOffsetVector);
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
				AM[i]=((double)formattedPacket[6+i])/100;
			}
			double[][] alignmentMatrix = {{AM[0],AM[1],AM[2]},{AM[3],AM[4],AM[5]},{AM[6],AM[7],AM[8]}}; 				
			double[][] sensitivityMatrix = {{formattedPacket[3],0,0},{0,formattedPacket[4],0},{0,0,formattedPacket[5]}}; 
			double[][] offsetVector = {{formattedPacket[0]},{formattedPacket[1]},{formattedPacket[2]}};
			
			for(int i=0;i<=2;i++){
				sensitivityMatrix[i][i] = sensitivityMatrix[i][i]/mSensitivityScaleFactor;
			}
			
			mCurrentAlignmentMatrix = alignmentMatrix; 			
			mCurrentSensitivityMatrix = sensitivityMatrix; 	
			mCurrentOffsetVector = offsetVector;
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
			sensitivityMatrixToUse[i][i] = Math.round(sensitivityMatrixToUse[i][i]*mSensitivityScaleFactor);
		}

		//Scale the alignment by 100
		double[][] alignmentMatrixToUse = UtilShimmer.deepCopyDoubleMatrix(alignmentMatrix);
		for(int i=0;i<=2;i++){
			alignmentMatrixToUse[i][0] = Math.round(alignmentMatrixToUse[i][0]*100.0);
			alignmentMatrixToUse[i][1] = Math.round(alignmentMatrixToUse[i][1]*100.0);
			alignmentMatrixToUse[i][2] = Math.round(alignmentMatrixToUse[i][2]*100.0);
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
		//TODO check if valid and return default if not?
		return mCurrentAlignmentMatrix;
	}

	public double[][] getCurrentSensitivityMatrix() {
		//TODO check if valid and return default if not?
		return mCurrentSensitivityMatrix;
	}

	public double[][] getCurrentOffsetVector() {
		//TODO check if valid and return default if not?
		return mCurrentOffsetVector;
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

	/** Specifically used by Gyro on the fly calibration
	 * @param mean
	 * @param mean2
	 * @param mean3
	 */
	public void updateCurrentOffsetVector(double XXmean, double XYmean, double XZmean) {
		if(mCurrentOffsetVector==null){
			mCurrentOffsetVector = new double[3][3];
		}
		
		mCurrentOffsetVector[0][0] = XXmean;
		mCurrentOffsetVector[1][0] = XYmean;
		mCurrentOffsetVector[2][0] = XZmean;
	}
	public void updateCurrentSensitivityMatrix(double XXmean, double XYmean, double XZmean) {
		if(mCurrentSensitivityMatrix==null){
			mCurrentSensitivityMatrix = new double[3][3];
		}
		
		mCurrentSensitivityMatrix[0][0] = XXmean;
		mCurrentSensitivityMatrix[1][1] = XYmean;
		mCurrentSensitivityMatrix[2][2] = XZmean;
	}
	public void updateCurrentAlignmentMatrix(double XXmean, double XYmean, double XZmean, 
			double YXmean, double YYmean, double YZmean,
			double ZXmean, double ZYmean, double ZZmean) {
		
		if(mCurrentAlignmentMatrix==null){
			mCurrentAlignmentMatrix = new double[3][3];
		}
		
		mCurrentAlignmentMatrix[0][0] = XXmean;
		mCurrentAlignmentMatrix[0][1] = XYmean;
		mCurrentAlignmentMatrix[0][2] = XZmean;
		
		mCurrentAlignmentMatrix[1][0] = YXmean;
		mCurrentAlignmentMatrix[1][1] = YYmean;
		mCurrentAlignmentMatrix[1][2] = YZmean;
		
		mCurrentAlignmentMatrix[2][0] = ZXmean;
		mCurrentAlignmentMatrix[2][1] = ZYmean;
		mCurrentAlignmentMatrix[2][2] = ZZmean;
	}

	public void setSensitivityScaleFactor(double sensitivityScaleFactor) {
		mSensitivityScaleFactor = sensitivityScaleFactor;
	}

	public String generateDebugString() {
		String debugString = "RangeString:" + mRangeString + "\t" + "RangeValue:" + mRangeValue + "\n";
		debugString += generateDebugStringPerProperty("Default Alignment", mDefaultAlignmentMatrix);
		debugString += generateDebugStringPerProperty("Current Alignment", mCurrentAlignmentMatrix);
		debugString += generateDebugStringPerProperty("Default Sensitivity", mDefaultSensitivityMatrix);
		debugString += generateDebugStringPerProperty("CurrentSensitivity", mCurrentSensitivityMatrix);
		debugString += generateDebugStringPerProperty("Default Offset Vector", mDefaultOffsetVector);
		debugString += generateDebugStringPerProperty("Current Offset Vector", mCurrentOffsetVector);
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
