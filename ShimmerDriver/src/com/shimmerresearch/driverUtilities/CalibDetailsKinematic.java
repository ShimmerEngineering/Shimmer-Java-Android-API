package com.shimmerresearch.driverUtilities;

import java.io.Serializable;
import java.util.Arrays;

import com.shimmerresearch.driver.UtilShimmer;

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
	
	protected double[][] mAlignmentMatrix = null; 			
	protected double[][] mSensitivityMatrix = null; 	
	protected double[][] mOffsetVector = null;

	protected double[][] mDefaultAlignmentMatrix = {{-1,0,0},{0,1,0},{0,0,-1}}; 			
	protected double[][] mDefaultSensitivityMatrix = {{0,0,0},{0,0,0},{0,0,0}}; 	
	protected double[][] mDefaultOffsetVector = {{0},{0},{0}};
	
	protected String mRangeString = "";
	protected int mRangeValue = 0;
	
	public CalibDetailsKinematic(int rangeValue, String rangeString) {
		this.mRangeValue = rangeValue;
		this.mRangeString = rangeString;
	}

	public CalibDetailsKinematic(int rangeValue, String rangeString, double[][] alignmentMatrix, double[][] sensitivityMatrix, double[][] offsetVector) {
		this(rangeValue, rangeString);
		this.setCurrentValues(alignmentMatrix, sensitivityMatrix, offsetVector);
	}

	public CalibDetailsKinematic(int rangeValue, String rangeString, 
			double[][] alignmentMatrix, double[][] sensitivityMatrix, double[][] offsetVector,
			double[][] defaultAlignmentMatrix, double[][] defaultSensitivityMatrix, double[][] defaultOffsetVector) {
		this(rangeValue, rangeString, alignmentMatrix, sensitivityMatrix, offsetVector);

		this.setDefaultValues(defaultAlignmentMatrix, defaultSensitivityMatrix, defaultOffsetVector);
	}

	
	public void setCurrentValues(double[][] alignmentMatrix, double[][] sensitivityMatrix, double[][] offsetVector) {
		this.mAlignmentMatrix = alignmentMatrix;
		this.mSensitivityMatrix = sensitivityMatrix;
		this.mOffsetVector = offsetVector;
	}

	public void setDefaultValues(double[][] defaultAlignmentMatrix, double[][] defaultSensitivityMatrix, double[][] defaultOffsetVector) {
		this.mDefaultAlignmentMatrix = defaultAlignmentMatrix;
		this.mDefaultSensitivityMatrix = defaultSensitivityMatrix;
		this.mDefaultOffsetVector = defaultOffsetVector;
	}

	public void resetToDefaultParameters(){
		mAlignmentMatrix = UtilShimmer.deepCopyDoubleMatrix(mDefaultAlignmentMatrix);
		mSensitivityMatrix = UtilShimmer.deepCopyDoubleMatrix(mDefaultSensitivityMatrix);
		mOffsetVector = UtilShimmer.deepCopyDoubleMatrix(mDefaultOffsetVector);
	}
	
	
	
	public boolean isCurrentValuesSet(){
		if(mAlignmentMatrix!=null && mSensitivityMatrix!=null && mOffsetVector!=null){
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
		if(mAlignmentMatrix==null || mDefaultAlignmentMatrix==null){
			return false;
		}
		return Arrays.deepEquals(mAlignmentMatrix, mDefaultAlignmentMatrix);
	}

	public boolean isSensitivityUsingDefault(){
		if(mSensitivityMatrix==null || mDefaultSensitivityMatrix==null){
			return false;
		}
		return Arrays.deepEquals(mSensitivityMatrix, mDefaultSensitivityMatrix);
	}

	public boolean isOffsetVectorUsingDefault(){
		if(mOffsetVector==null || mDefaultOffsetVector==null){
			return false;
		}
		return Arrays.deepEquals(mOffsetVector, mDefaultOffsetVector);
	}

	
	
	public boolean isAllCalibrationValid(){
		if(isAlignmentValid() && isSensitivityValid() && isOffsetVectorValid()){
			return true;
		}
		return false;
	}
	
	public boolean isAlignmentValid(){
		//TODO
		return true;
	}

	public boolean isSensitivityValid(){
		//TODO
		return true;
	}
	
	public boolean isOffsetVectorValid(){
		//TODO
		return true;
	}
}
