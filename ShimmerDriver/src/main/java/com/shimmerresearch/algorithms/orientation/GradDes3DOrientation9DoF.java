package com.shimmerresearch.algorithms.orientation;


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
	 * @param q0
	 * @param q1
	 * @param q2
	 * @param q3
	 * @see GradDes3DOrientation
	 */
	public GradDes3DOrientation9DoF(double beta, double samplingPeriod, double q0, double q1, double q2, double q3) {
		super(beta, samplingPeriod, q0, q1, q2, q3);
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
	    double recipNorm;
	    double s0, s1, s2, s3;
	    double qDot1, qDot2, qDot3, qDot4;
	    double hx, hy;
	    double _2q0, _2q1, _2q2, _2q3, _2bz, _2bx, q0q0, q0q1, q0q2, q0q3, q1q1, q1q2, q1q3, q2q2, q2q3, q3q3;
	
        q0q0 = q0*q0;
        q1q1 = q1*q1;
        q2q2 = q2*q2;
        q3q3 = q3*q3;
        
        _2q0 = 2*q0;
        _2q1 = 2*q1;
        _2q2 = 2*q2;
        _2q3 = 2*q3;
        
        q0q1 = q0*q1;
        q0q2 = q0*q2;
        q0q3 = q0*q3;
        q1q2 = q1*q2;
        q1q3 = q1*q3;
        q2q3 = q2*q3;
        
	    // Normalise accelerometer measurement
	    recipNorm = Math.sqrt(ax * ax + ay * ay + az * az);
		//Above line has a potential NaN/Infinity result so need to check the result
		if (Double.isFinite(recipNorm) && recipNorm > 0.0) {
	       recipNorm = 1.0 / recipNorm; 
	       ax *= recipNorm;
	       ay *= recipNorm;
	       az *= recipNorm;
		} else {
			//TODO
	    }
	
	    // Normalise magnetometer measurement
	    recipNorm = Math.sqrt(mx * mx + my * my + mz * mz);
		//Above line has a potential NaN/Infinity result so need to check the result
		if (Double.isFinite(recipNorm) && recipNorm > 0.0) {
	       recipNorm = 1.0 / recipNorm;
	       mx *= recipNorm;
	       my *= recipNorm;
	       mz *= recipNorm;
		} else {
			//TODO
	    }
	
		// Reference direction of Earth's magnetic field
        hx = mx*(q0q0 + q1q1 - q2q2-q3q3) + 2*my*(q1q2 - q0q3) + 2*mz*(q1q3 + q0q2);
        hy = 2*mx*(q0q3 + q1q2) + my*(q0q0 - q1q1 + q2q2 - q3q3) * 2*mz*(q2q3 - q0q1);
        _2bz = 2*mx*(q1q3 - q0q2) + 2*my*(q0q1 + q2q3) + mz*(q0q0 - q1q1 - q2q2 + q3q3);
        _2bx = Math.sqrt(hx * hx + hy * hy);

        // Gradient decent algorithm corrective step
        s0 = -_2q2 * (2*(q1q3 - q0q2) - ax) +
        		_2q1*(2*(q0q1 + q2q3) - ay) - _2bz*q2*(_2bx*(0.5 - q2q2 - q3q3) + 
        		_2bz*(q1q3 - q0q2) - mx) + (-_2bx*q3 + _2bz*q1)*(_2bx*(q1q2 - q0q3) + 
        		_2bz*(q0q1 + q2q3) - my) +_2bx*q2*(_2bx*(q0q2 + q1q3) + _2bz*(0.5 - q1q1 - q2q2) - mz);
            
        s1 = _2q3*(2*(q1q3 - q0q2) - ax) +
            _2q0*(2*(q0q1 + q2q3) - ay) - 
            4*q1*(1 - 2*(q1q1 + q2q2) - az) +
            _2bz*q3*(_2bx*(0.5 - q2q2 - q3q3) + _2bz*(q1q3 - q0q2) - mx) + 
            (_2bx*q2 + _2bz*q0)*(_2bx*(q1q2 - q0q3) + _2bz*(q0q1 + q2q3) - my) +
            (_2bx*q3 - _2bz*_2q1)*(_2bx*(q0q2 + q1q3) + _2bz*(0.5 - q1q1 - q2q2) - mz);
        
        s2 = -_2q0*(2*(q1q3 - q0q2) - ax) + 
            _2q3*(2*(q0q1 + q2q3) - ay) - 
            4*q2*(1 - 2*(q1q1 + q2q2) - az) + 
            (-_2bx*_2q2 - _2bz*q0)*(_2bx*(0.5 - q2q2 - q3q3) + _2bz*(q1q3 - q0q2) - mx) + 
            (_2bx * q1 + _2bz * q3)*(_2bx*(q1q2 - q0q3) + _2bz*(q0q1 + q2q3) - my) + 
            (_2bx * q0 - _2bz*_2q2)*(_2bx*(q0q2 + q1q3) + _2bz*(0.5 - q1q1 - q2q2) - mz);
        
        s3 = _2q1 * (2.0 * (q1q3 - q0q2) - ax) + 
            _2q2 * (2*(q0q1 + q2q3) - ay) + 
            (-_2bx * _2q3 + _2bz * q1) * (_2bx * (0.5 - q2q2 - q3q3) + _2bz * (q1q3 - q0q2) - mx) + 
            (-_2bx * q0 + _2bz * q2) * (_2bx * (q1q2 - q0q3) + _2bz * (q0q1 + q2q3) - my) + 
            _2bx * q1 * (_2bx * (q0q2 + q1q3) + _2bz * (0.5 - q1q1 - q2q2) - mz);
            
            
        // normalise step magnitude
        recipNorm = 1.0 / Math.sqrt(s0 * s0 + s1 * s1 + s2 * s2 + s3 * s3);
		//Above line has a potential NaN/Infinity result so need to check the result
		if (Double.isFinite(recipNorm) && recipNorm > 0.0) {
		    s0 *= recipNorm;
		    s1 *= recipNorm;
		    s2 *= recipNorm;
		    s3 *= recipNorm;
		} else {
			//TODO
		}
	
	    // Compute rate of change of quaternion
	    qDot1 = 0.5 * (-q1 * gx - q2 * gy - q3 * gz) - mBeta * s0;
	    qDot2 = 0.5 * ( q0 * gx - q3 * gy + q2 * gz) - mBeta * s1;
	    qDot3 = 0.5 * ( q3 * gx + q0 * gy - q1 * gz)  - mBeta * s2;
	    qDot4 = 0.5 * (-q2 * gx + q1 * gy + q0 * gz) - mBeta * s3;
	
	    // Integrate rate of change of quaternion to yield quaternion
        q0 += qDot1 * mSamplingPeriod;
        q1 += qDot2 * mSamplingPeriod;
        q2 += qDot3 * mSamplingPeriod;
        q3 += qDot4 * mSamplingPeriod;
	    recipNorm = 1.0 / Math.sqrt(q0 * q0 + q1 * q1 + q2 * q2 + q3 * q3);    // normalise quaternion
	
	    // Normalise quaternion
	    q0 *= recipNorm;
	    q1 *= recipNorm;
	    q2 *= recipNorm;
	    q3 *= recipNorm;

	    return new Orientation3DObject(q0,q1,q2,q3);
    }
}
