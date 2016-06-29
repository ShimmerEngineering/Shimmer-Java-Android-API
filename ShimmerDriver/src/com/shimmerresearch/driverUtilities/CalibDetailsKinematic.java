package com.shimmerresearch.driverUtilities;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

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

	public double[][] mDefaultAlignmentMatrix =null;   			
	public double[][] mDefaultSensitivityMatrix = null;  	
	public double[][] mDefaultOffsetVector = null; 
	
	public String mRangeString = "";
	public int mRangeValue = 0;
	
	//Not Driver related - consider a different approach?
	public int guiRangeValue = 0;
	//Not Driver related - consider a different approach?
	public Integer[]guiRangeOptions = null;
	
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

	public boolean isAllUsingDefaultParameters(){
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
		if((isAlignmentValid() && isSensitivityValid() && isOffsetVectorValid()) || isAllUsingDefaultParameters()){
			return true;
		}
		return false;
	}
	
	public boolean isAlignmentValid(){
	 if(isAllZeros(mCurrentAlignmentMatrix)){
			return false;
		}else{
			return true;
		}
	}

	public boolean isSensitivityValid(){
	 if(isAllZeros(mCurrentSensitivityMatrix) ){
			return false;
		}else{
			return true;
		}
	}
	
	//TODO : look up is there a valid check for a valid offset vector 
	public boolean isOffsetVectorValid(){
			return true;
	}
	
	//TODO to be replaced with MN utility function
	public boolean isAllZeros(double[][] matrix){
		boolean allZeros = true;
		if(matrix!=null){
//			System.err.println("Matrix lengths  " + matrix[1].length);
//			System.err.println("Matrix length extra  " + matrix.length);
			for(int j = 0; j < matrix[1].length; j++){
				for(int i = 0; i < matrix.length; i++){
			if(matrix[i][j]!=0){
				allZeros = false;
			}
				}
			}
			return allZeros;
			}
		return allZeros;
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
