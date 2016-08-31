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

	public CalibArraysKinematic() {
	}

	public CalibArraysKinematic(double[][] offsetVector, double[][] sensitivityMatrix, double[][] alignmentMatrix) {
		setValues(offsetVector, sensitivityMatrix, alignmentMatrix);
	}

	public void updateOffsetVector(double XXvalue, double YYvalue, double ZZvalue) {
		mOffsetVector = new double[3][1];
		mOffsetVector[0][0] = XXvalue;
		mOffsetVector[1][0] = YYvalue;
		mOffsetVector[2][0] = ZZvalue;
	}
	
	public void updateSensitivityMatrix(double XXvalue, double YYvalue, double ZZvalue) {
		mSensitivityMatrix = new double[3][3];
		mSensitivityMatrix[0][0] = XXvalue;
		mSensitivityMatrix[1][1] = YYvalue;
		mSensitivityMatrix[2][2] = ZZvalue;
	}

	public void updateAlignmentMatrix(
			double XXvalue, double XYvalue, double XZvalue, 
			double YXvalue, double YYvalue, double YZvalue,
			double ZXvalue, double ZYvalue, double ZZvalue) {
		mAlignmentMatrix = new double[3][3];
		mAlignmentMatrix[0][0] = XXvalue;
		mAlignmentMatrix[0][1] = XYvalue;
		mAlignmentMatrix[0][2] = XZvalue;
		
		mAlignmentMatrix[1][0] = YXvalue;
		mAlignmentMatrix[1][1] = YYvalue;
		mAlignmentMatrix[1][2] = YZvalue;
		
		mAlignmentMatrix[2][0] = ZXvalue;
		mAlignmentMatrix[2][1] = ZYvalue;
		mAlignmentMatrix[2][2] = ZZvalue;
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
	}

	public void setAlignmentMatrix(double[][] alignmentMatrix) {
		mAlignmentMatrix = alignmentMatrix;
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
