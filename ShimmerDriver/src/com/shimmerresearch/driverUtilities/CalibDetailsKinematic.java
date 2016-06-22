package com.shimmerresearch.driverUtilities;

/**
 * Class that holds the calibration parameters for a particular range in a
 * Kinematic sensor
 * 
 * @author Mark Nolan
 *
 */
public class CalibDetailsKinematic {
	
	protected double[][] mAlignmentMatrix = {{-1,0,0},{0,1,0},{0,0,-1}}; 			
	protected double[][] mSensitivityMatrix = {{0,0,0},{0,0,0},{0,0,0}}; 	
	protected double[][] mOffsetVector = {{0},{0},{0}};
	
	protected String mRangeString = "";
	protected int mRangeValue = 0;
	public boolean mIsDefaultCal = false;

	public CalibDetailsKinematic(int rangeValue, String rangeString, double[][] alignmentMatrix, double[][] sensitivityMatrix, double[][] offsetVector) {
		this.mRangeValue = rangeValue;
		this.mRangeString = rangeString;
		this.mAlignmentMatrix = alignmentMatrix;
		this.mSensitivityMatrix = sensitivityMatrix;
		this.mOffsetVector = offsetVector;
	}

}
