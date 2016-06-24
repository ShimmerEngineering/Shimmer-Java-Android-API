package com.shimmerresearch.driverUtilities;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Class that holds the calibration parameters for a particular range in a
 * Kinematic sensor
 * 
 * @author Mark Nolan
 *
 */
public class CalibDetailsKinematic implements Serializable {
	
	/** * */
	private static final long serialVersionUID = -3556098650349506733L;
	
	public double[][] mCurrentAlignmentMatrix = null; 			
	public double[][] mCurrentSensitivityMatrix = null; 	
	public double[][] mCurrentOffsetVector = null;

	public double[][] mDefaultAlignmentMatrix = {{-1,0,0},{0,1,0},{0,0,-1}}; 			
	public double[][] mDefaultSensitivityMatrix = {{0,0,0},{0,0,0},{0,0,0}}; 	
	public double[][] mDefaultOffsetVector = {{0},{0},{0}};
	
	public String mRangeString = "";
	public int mRangeValue = 0;
	
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

	
	public void setCurrentValues(double[][] currentAlignmentMatrix, double[][] currentSensitivityMatrix, double[][] currentOffsetVector) {
		this.mCurrentAlignmentMatrix = currentAlignmentMatrix;
		this.mCurrentSensitivityMatrix = currentSensitivityMatrix;
		this.mCurrentOffsetVector = currentOffsetVector;
	}

	public void setDefaultValues(double[][] defaultAlignmentMatrix, double[][] defaultSensitivityMatrix, double[][] defaultOffsetVector) {
		this.mDefaultAlignmentMatrix = defaultAlignmentMatrix;
		this.mDefaultSensitivityMatrix = defaultSensitivityMatrix;
		this.mDefaultOffsetVector = defaultOffsetVector;
	}

	public void resetToDefaultParameters(){
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

	
	
	public boolean isAnyUsingDefaultParameters(){
		if(isAlignmentUsingDefault() || isSensitivityUsingDefault() || isOffsetVectorUsingDefault()){
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
		if(isAlignmentValid() && isSensitivityValid() && isOffsetVectorValid()){
			return true;
		}
		return false;
	}
	
	public boolean isAlignmentValid(){
		//TODO for Eimear
		return true;
	}

	public boolean isSensitivityValid(){
		//TODO for Eimear
		return true;
	}
	
	public boolean isOffsetVectorValid(){
		//TODO for Eimear
		return true;
	}

	public String generateDebugString() {
		String debugString = "RangeString:" + mRangeString + "\t" + "RangeValue:" + mRangeValue + "\n";
		
		debugString += "Default Alignment = ";
		if(mDefaultAlignmentMatrix==null){
			debugString += "NULL";
		}
		else{
			debugString += UtilShimmer.doubleArrayToString(mDefaultAlignmentMatrix);
		}

		debugString += "Default Sensitivity = ";
		if(mDefaultSensitivityMatrix==null){
			debugString += "NULL";
		}
		else{
			debugString += UtilShimmer.doubleArrayToString(mDefaultSensitivityMatrix);
		}

		debugString += "Default Offset Vector = ";
		if(mDefaultOffsetVector==null){
			debugString += "NULL";
		}
		else{
			debugString += UtilShimmer.doubleArrayToString(mDefaultOffsetVector);
		}
		
		return debugString;
	}
}
