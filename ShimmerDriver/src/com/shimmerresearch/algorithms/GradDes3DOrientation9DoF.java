package com.shimmerresearch.algorithms;

/* Rev 0.2
 * 
 * Madgwick, Sebastian OH, Andrew JL Harrison, and Ravi Vaidyanathan. "Estimation of imu and marg orientation using a gradient descent algorithm." Rehabilitation Robotics (ICORR), 2011 IEEE International Conference on. IEEE, 2011.
 *
 * 3D orientation code taken from https://code.google.com/p/labview-quaternion-ahrs/ which is licensed under GNU_Lesser_GPL
 *
 * Copyright (c) 2010, Shimmer Research, Ltd.
 * All rights reserved
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:

 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above
 *       copyright notice, this list of conditions and the following
 *       disclaimer in the documentation and/or other materials provided
 *       with the distribution.
 *     * Neither the name of Shimmer Research, Ltd. nor the names of its
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.

 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * @author Jong Chern Lim
 * @date   October, 2013
 * 
 * 
 * Changes since Rev 0.1 
 * - updated java doc
 * 
 */

public class GradDes3DOrientation9DoF extends GradDes3DOrientation{

	/**
	 * @param samplingPeriod
	 * @see GradDes3DOrientation
	 */
	public GradDes3DOrientation9DoF(double samplingPeriod) {
		super(samplingPeriod);
	}

	/**
	 * @param beta
	 * @param samplingPeriod
	 * @param q1
	 * @param q2
	 * @param q3
	 * @param q4
	 * @see GradDes3DOrientation
	 */
	public GradDes3DOrientation9DoF(double beta, double samplingPeriod, double q1, double q2, double q3, double q4) {
		super(beta, samplingPeriod, q1, q2, q3, q4);
	}

	/**
	 * @param ax Accelerometer X in m/(sec^2)
	 * @param ay Accelerometer Y in m/(sec^2)
	 * @param az Accelerometer Z in m/(sec^2)
	 * @param gx Gyroscope X in rad/sec
	 * @param gy Gyroscope X in rad/sec
	 * @param gz Gyroscope X in rad/sec
	 * @param mx Magnetometer X in local
	 * @param my Magnetometer X in local
	 * @param mz Magnetometer X in local
	 * @return Calculated Quaternion value
	 */
	public Orientation3DObject update(double ax, double ay, double az, double gx, double gy, double gz, double mx, double my, double mz) {
	    double norm;
	    double hx, hy;
	    double s1, s2, s3, s4;
	    double qDot1, qDot2, qDot3, qDot4;
	
	    double q1q1, q1q2, q1q3, q1q4, q2q2, q2q3, q2q4, q3q3, q3q4, q4q4, twoq1, twoq2, twoq3, twoq4, twobz, twobx;
	
        q1q1 = q1*q1;
        q2q2 = q2*q2;
        q3q3 = q3*q3;
        q4q4 = q4*q4;
        
        twoq1 = 2*q1;
        twoq2 = 2*q2;
        twoq3 = 2*q3;
        twoq4 = 2*q4;
        
        q1q2 = q1*q2;
        q1q3 = q1*q3;
        q1q4 = q1*q4;
        q2q3 = q2*q3;
        q2q4 = q2*q4;
        q3q4 = q3*q4;
        
	
	    // Normalise accelerometer measurement
	    norm = Math.sqrt(ax * ax + ay * ay + az * az);
	    if (norm > 0.0){
	       norm = 1.0 / norm; 
	       ax *= norm;
	       ay *= norm;
	       az *= norm;
	    }
	    else{
	
	    }
	
	    // Normalise magnetometer measurement
	    norm = Math.sqrt(mx * mx + my * my + mz * mz);
	    if (norm > 0.0){
	       norm = 1.0 / norm;
	       mx *= norm;
	       my *= norm;
	       mz *= norm;
	    }
	    else{
	
	    }
	
        hx = mx*(q1q1 + q2q2 - q3q3-q4q4) + 2*my*(q2q3 - q1q4) + 2*mz*(q2q4 + q1q3);
        hy = 2*mx*(q1q4 + q2q3) + my*(q1q1 - q2q2 + q3q3 - q4q4) * 2*mz*(q3q4 - q1q2);
        twobz = 2*mx*(q2q4 - q1q3) + 2*my*(q1q2 + q3q4) + mz*(q1q1 - q2q2 - q3q3 + q4q4);
        twobx = Math.sqrt(hx * hx + hy * hy);

	
	    // Corrective step
       
        s1 = -twoq3 * (2*(q2q4 - q1q3) - ax) +
        		twoq2*(2*(q1q2 + q3q4) - ay) - twobz*q3*(twobx*(0.5 - q3q3 - q4q4) + 
        		twobz*(q2q4 - q1q3) - mx) + (-twobx*q4 + twobz*q2)*(twobx*(q2q3 - q1q4) + 
        		twobz*(q1q2 + q3q4) - my) +twobx*q3*(twobx*(q1q3 + q2q4) + twobz*(0.5 - q2q2 - q3q3) - mz);
            
            s2 = twoq4*(2*(q2q4 - q1q3) - ax) +
                twoq1*(2*(q1q2 + q3q4) - ay) - 
                4*q2*(1 - 2*(q2q2 + q3q3) - az) +
                twobz*q4*(twobx*(0.5 - q3q3 - q4q4) + twobz*(q2q4 - q1q3) - mx) + 
                (twobx*q3 + twobz*q1)*(twobx*(q2q3 - q1q4) + twobz*(q1q2 + q3q4) - my) +
                (twobx*q4 - twobz*twoq2)*(twobx*(q1q3 + q2q4) + twobz*(0.5 - q2q2 - q3q3) - mz);
            
            s3 = -twoq1*(2*(q2q4 - q1q3) - ax) + 
                twoq4*(2*(q1q2 + q3q4) - ay) - 
                4*q3*(1 - 2*(q2q2 + q3q3) - az) + 
                (-twobx*twoq3 - twobz*q1)*(twobx*(0.5 - q3q3 - q4q4) + twobz*(q2q4 - q1q3) - mx) + 
                (twobx * q2 + twobz * q4)*(twobx*(q2q3 - q1q4) + twobz*(q1q2 + q3q4) - my) + 
                (twobx * q1 - twobz*twoq3)*(twobx*(q1q3 + q2q4) + twobz*(0.5 - q2q2 - q3q3) - mz);
            
            s4 = twoq2 * (2.0 * (q2q4 - q1q3) - ax) + 
                twoq3 * (2*(q1q2 + q3q4) - ay) + 
                (-twobx * twoq4 + twobz * q2) * (twobx * (0.5 - q3q3 - q4q4) + twobz * (q2q4 - q1q3) - mx) + 
                (-twobx * q1 + twobz * q3) * (twobx * (q2q3 - q1q4) + twobz * (q1q2 + q3q4) - my) + 
                twobx * q2 * (twobx * (q1q3 + q2q4) + twobz * (0.5 - q2q2 - q3q3) - mz);
            
            
	    norm = 1.0 / Math.sqrt(s1 * s1 + s2 * s2 + s3 * s3 + s4 * s4);    // normalise
	    s1 *= norm;
	    s2 *= norm;
	    s3 *= norm;
	    s4 *= norm;
	
	    // Compute rate of change of quaternion
	    qDot1 = 0.5 * (-q2 * gx - q3 * gy - q4 * gz) - mBeta * s1;
	    qDot2 = 0.5 * ( q1 * gx - q4 * gy + q3 * gz) - mBeta * s2;
	    qDot3 = 0.5 * ( q4 * gx + q1 * gy - q2 * gz)  - mBeta * s3;
	    qDot4 = 0.5 * (-q3 * gx + q2 * gy + q1 * gz) - mBeta * s4;
	
	    // Integrate to yield quaternion
        q1 += qDot1 * mSamplingPeriod;
        q2 += qDot2 * mSamplingPeriod;
        q3 += qDot3 * mSamplingPeriod;
        q4 += qDot4 * mSamplingPeriod;
	    norm = 1.0 / Math.sqrt(q1 * q1 + q2 * q2 + q3 * q3 + q4 * q4);    // normalise quaternion
	
	    q1 = q1 * norm;
	    q2 = q2 * norm;
	    q3 = q3 * norm;
	    q4 = q4 * norm;

	    
	    return new Orientation3DObject(q1,q2,q3,q4);
    }
}
