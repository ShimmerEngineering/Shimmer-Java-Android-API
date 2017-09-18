package com.shimmerresearch.driver.calibration;

import java.io.Serializable;

import com.shimmerresearch.driverUtilities.UtilShimmer;

public class CalibArraysKinematic implements Serializable{
	
	/** * */
	private static final long serialVersionUID = -6799425106976911611L;
	
	private long mCalibTime = 0;
	public double[][] mOffsetVector = null;
	public double[][] mSensitivityMatrix = null; 	
	public double[][] mAlignmentMatrix = null;  			

	public double[][] mMatrixMultipliedInverseAMSM = null;  			
	private double[][] mMatrixInverseAlignmentMatrix = null;  			
	private double[][] mMatrixInverseSensitivityMatrix = null;  			
	
	public CalibArraysKinematic() {
	}

	public CalibArraysKinematic(double[][] offsetVector, double[][] sensitivityMatrix, double[][] alignmentMatrix) {
		setValues(offsetVector, sensitivityMatrix, alignmentMatrix);
	}

	public void updateOffsetVector(double XXvalue, double YYvalue, double ZZvalue) {
		double[][] offsetVector = new double[3][1];
		offsetVector[0][0] = XXvalue;
		offsetVector[1][0] = YYvalue;
		offsetVector[2][0] = ZZvalue;
		
		setOffsetVector(offsetVector);
	}
	
	public void updateSensitivityMatrix(double XXvalue, double YYvalue, double ZZvalue) {
		double[][] sensitivityMatrix = new double[3][3];
		sensitivityMatrix[0][0] = XXvalue;
		sensitivityMatrix[1][1] = YYvalue;
		sensitivityMatrix[2][2] = ZZvalue;
		
		setSensitivityMatrix(sensitivityMatrix);
	}

	public void updateAlignmentMatrix(
			double XXvalue, double XYvalue, double XZvalue, 
			double YXvalue, double YYvalue, double YZvalue,
			double ZXvalue, double ZYvalue, double ZZvalue) {
		double[][] alignmentMatrix = new double[3][3];
		alignmentMatrix[0][0] = XXvalue;
		alignmentMatrix[0][1] = XYvalue;
		alignmentMatrix[0][2] = XZvalue;
		
		alignmentMatrix[1][0] = YXvalue;
		alignmentMatrix[1][1] = YYvalue;
		alignmentMatrix[1][2] = YZvalue;
		
		alignmentMatrix[2][0] = ZXvalue;
		alignmentMatrix[2][1] = ZYvalue;
		alignmentMatrix[2][2] = ZZvalue;
		
		setAlignmentMatrix(alignmentMatrix);
	}
	
	public void setValues(double[][] offsetVector, double[][] sensitivityMatrix, double[][] alignmentMatrix){
		setOffsetVector(offsetVector);
		setSensitivityMatrix(sensitivityMatrix);
		setAlignmentMatrix(alignmentMatrix);
	}
	
	public void setOffsetVector(double[][] offsetVector) {
		mOffsetVector = offsetVector;
	}

	public void setSensitivityMatrix(double[][] sensitivityMatrix) {
		mSensitivityMatrix = sensitivityMatrix;
		
		mMatrixInverseSensitivityMatrix = UtilCalibration.matrixInverse3x3(mSensitivityMatrix);
		mMatrixMultipliedInverseAMSM = UtilCalibration.matrixMultiplication(mMatrixInverseAlignmentMatrix, mMatrixInverseSensitivityMatrix);
	}

	public void setAlignmentMatrix(double[][] alignmentMatrix) {
		mAlignmentMatrix = alignmentMatrix;
		
		mMatrixInverseAlignmentMatrix = UtilCalibration.matrixInverse3x3(mAlignmentMatrix);
		mMatrixMultipliedInverseAMSM = UtilCalibration.matrixMultiplication(mMatrixInverseAlignmentMatrix, mMatrixInverseSensitivityMatrix);
	}

	
	public boolean isCurrentValuesSet(){
		if(mAlignmentMatrix!=null && mSensitivityMatrix!=null && mOffsetVector!=null){
			return true;
		}
		return false;
	}
	
	public boolean isAlignmentValid(){
		if(UtilShimmer.isAllZeros(mAlignmentMatrix) 
				|| UtilShimmer.isAnyValueOutsideRange(mAlignmentMatrix, 1)){
			return false;
		}else{
			return true;
		}
	}

	public boolean isSensitivityValid(){
		if(isDiagonalFilled(mSensitivityMatrix) ){
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

	public boolean isAllCalibrationValid() {
		if(isAlignmentValid() && isSensitivityValid() && isOffsetVectorValid()){
			return true;
		}
		return false;
	}

	public void setCalibTime(long calibTime) {
		mCalibTime = calibTime;
	}

	public long getCalibTime() {
		return mCalibTime;
	}

}
