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
	
	protected double[][] mAlignmentMatrix = {{-1,0,0},{0,1,0},{0,0,-1}}; 			
	protected double[][] mSensitivityMatrix = {{0,0,0},{0,0,0},{0,0,0}}; 	
	protected double[][] mOffsetVector = {{0},{0},{0}};

	protected double[][] mDefaultAlignmentMatrix = {{-1,0,0},{0,1,0},{0,0,-1}}; 			
	protected double[][] mDefaultSensitivityMatrix = {{0,0,0},{0,0,0},{0,0,0}}; 	
	protected double[][] mDefaultOffsetVector = {{0},{0},{0}};
	
	protected String mRangeString = "";
	protected int mRangeValue = 0;
	@Deprecated
	public boolean mIsDefaultCal = false;

	public CalibDetailsKinematic(int rangeValue, String rangeString, double[][] alignmentMatrix, double[][] sensitivityMatrix, double[][] offsetVector) {
		this.mRangeValue = rangeValue;
		this.mRangeString = rangeString;
		
		this.setCurrentValues(alignmentMatrix, sensitivityMatrix, offsetVector);
	}

	public CalibDetailsKinematic(int rangeValue, String rangeString, 
			double[][] alignmentMatrix, double[][] sensitivityMatrix, double[][] offsetVector,
			double[][] defaultAlignmentMatrix, double[][] defaultSensitivityMatrix, double[][] defaultOffsetVector) {
		this.mRangeValue = rangeValue;
		this.mRangeString = rangeString;
		
		this.setCurrentValues(alignmentMatrix, sensitivityMatrix, offsetVector);
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

	public boolean isDefaultParameters(){
		boolean alignmentPass = Arrays.deepEquals(mAlignmentMatrix, mDefaultAlignmentMatrix);
		boolean offsetPass = Arrays.deepEquals(mSensitivityMatrix, mDefaultSensitivityMatrix);
		boolean sensitivityPass = Arrays.deepEquals(mOffsetVector, mDefaultOffsetVector);
		
		if(alignmentPass&&offsetPass&&sensitivityPass){
			return true;
		}
		return false;
	}
	
	public void resetToDefaultParameters(){
		mAlignmentMatrix = UtilShimmer.deepCopyDoubleMatrix(mDefaultAlignmentMatrix);
		mSensitivityMatrix = UtilShimmer.deepCopyDoubleMatrix(mDefaultSensitivityMatrix);
		mOffsetVector = UtilShimmer.deepCopyDoubleMatrix(mDefaultOffsetVector);
	}

}
