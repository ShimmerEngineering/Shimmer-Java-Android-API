package com.shimmerresearch.sensors;

/**
 * Class that holds the calibration parameters for a particular range in a
 * Kinematic sensor
 * 
 * @author Mark Nolan
 *
 */
public class CalDetailsKinematic {
	
	protected double[][] mAlignmentMatrix = {{-1,0,0},{0,1,0},{0,0,-1}}; 			
	protected double[][] mSensitivityMatrix = {{0,0,0},{0,0,0},{0,0,0}}; 	
	protected double[][] mOffsetVector = {{0},{0},{0}};
	
	protected String mRangeStrng = "";
	protected int mRangeValue = 0;

	public CalDetailsKinematic(double[][] alignmentMatrix, double[][] sensitivityMatrix, double[][] offsetVector) {
		super();
		this.mAlignmentMatrix = alignmentMatrix;
		this.mSensitivityMatrix = sensitivityMatrix;
		this.mOffsetVector = offsetVector;
	}

}