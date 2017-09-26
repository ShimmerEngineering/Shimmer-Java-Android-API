package com.shimmerresearch.algorithms.orientation;

public class GradDes3DOrientation {
	
	//The Beta variable is described well at the following link
	//https://github.com/sparkfun/MPU-9250_Breakout/blob/master/Libraries/Arduino/src/quaternionFilters.cpp
	//	GyroMeasError = PI * (40.0f / 180.0f); // gyroscope measurement error in rads/s (start at 40 deg/s for the MPU9250)
	//	beta = sqrt(3.0f / 4.0f) * GyroMeasError; // compute beta
	//We have typically found 0.5 works better with the Shimmer
	public static double BETA = 0.5;
	public final static double Q1_INITIAL = 1;
	public final static double Q2_INITIAL = 0;
	public final static double Q3_INITIAL = 0;
	public final static double Q4_INITIAL = 0;

	double mBeta = BETA;
    double mSamplingPeriod = 1;
    double q1, q2, q3, q4;

	/** Initialise Gradient Descent algorithm with default BETA and initial Quaternion.
	 * @param samplingPeriod
	 */
	public GradDes3DOrientation(double samplingPeriod) {
		this(BETA, samplingPeriod, Q1_INITIAL, Q2_INITIAL, Q3_INITIAL, Q4_INITIAL);
	}

	/** Initialise Gradient Descent algorithm with provided BETA and initial Quaternion.
     * @param beta
     * @param samplingPeriod
     * @param q1
     * @param q2
     * @param q3
     * @param q4
     */
    public GradDes3DOrientation(double beta, double samplingPeriod, double q1, double q2, double q3, double q4) {
        setSamplingPeriod(samplingPeriod);
        setInitialConditions(beta, q1, q2, q3, q4);
    }

	public void setSamplingPeriod(double samplingPeriod) {
		mSamplingPeriod = samplingPeriod;		
	}
	
	
	public void setInitialConditions(double beta, double q1, double q2, double q3, double q4) {
        mBeta = beta;
        this.q1 = q1;
        this.q2 = q2;
        this.q3 = q3;
        this.q4 = q4;
	}

}
