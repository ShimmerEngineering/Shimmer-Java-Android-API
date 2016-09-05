package com.shimmerresearch.algorithms;
/* Rev 0.1
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
 * 
 */



/*
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
 */

public class GradDes3DOrientation6DoF extends GradDes3DOrientation {

	/**
	 * @param samplingPeriod
	 * @see GradDes3DOrientation
	 */
	public GradDes3DOrientation6DoF(double samplingPeriod) {
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
	public GradDes3DOrientation6DoF(double beta, double samplingPeriod, double q1, double q2, double q3, double q4) {
		super(beta, samplingPeriod, q1, q2, q3, q4);
	}

	/**
	 * @param ax Accelerometer X in m/(sec^2)
	 * @param ay Accelerometer Y in m/(sec^2)
	 * @param az Accelerometer Z in m/(sec^2)
	 * @param gx Gyroscope X in rad/sec
	 * @param gy Gyroscope X in rad/sec
	 * @param gz Gyroscope X in rad/sec
	 * @return Calculated Quaternion value
	 */
	public Orientation3DObject update(double ax, double ay, double az, double gx, double gy, double gz) {
		double norm;
		double s0, s1, s2, s3;
		double qDot1, qDot2, qDot3, qDot4;
		double _2q1, _2q2, _2q3, _2q4, _4q1, _4q2, _4q3 ,_8q2, _8q3, q1q1, q2q2, q3q3, q4q4;

		// Rate of change of quaternion from gyroscope
		qDot1 = 0.5f * (-q2 * gx - q3 * gy - q4 * gz);
		qDot2 = 0.5f * (q1 * gx + q3 * gz - q4 * gy);
		qDot3 = 0.5f * (q1 * gy - q2 * gz + q4 * gx);
		qDot4 = 0.5f * (q1 * gz + q2 * gy - q3 * gx);

		// Compute feedback only if accelerometer measurement valid (avoids NaN in accelerometer normalisation)
		if(!((ax == 0.0f) && (ay == 0.0f) && (az == 0.0f))) {

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

			// Auxiliary variables to avoid repeated arithmetic
			_2q1 = 2.0f * q1;
			_2q2 = 2.0f * q2;
			_2q3 = 2.0f * q3;
			_2q4 = 2.0f * q4;
			_4q1 = 4.0f * q1;
			_4q2 = 4.0f * q2;
			_4q3 = 4.0f * q3;
			_8q2 = 8.0f * q2;
			_8q3 = 8.0f * q3;
			q1q1 = q1 * q1;
			q2q2 = q2 * q2;
			q3q3 = q3 * q3;
			q4q4 = q4 * q4;

			// Gradient decent algorithm corrective step
			s0 = _4q1 * q3q3 + _2q3 * ax + _4q1 * q2q2 - _2q2 * ay;
			s1 = _4q2 * q4q4 - _2q4 * ax + 4.0f * q1q1 * q2 - _2q1 * ay - _4q2 + _8q2 * q2q2 + _8q2 * q3q3 + _4q2 * az;
			s2 = 4.0f * q1q1 * q3 + _2q1 * ax + _4q3 * q4q4 - _2q4 * ay - _4q3 + _8q3 * q2q2 + _8q3 * q3q3 + _4q3 * az;
			s3 = 4.0f * q2q2 * q4 - _2q2 * ax + 4.0f * q3q3 * q4 - _2q3 * ay;

		    norm = 1.0 / Math.sqrt(s1 * s1 + s2 * s2 + s3 * s3 + s0 * s0);    // normalise
		    s0 *= norm;
		    s1 *= norm;
		    s2 *= norm;
		    s3 *= norm;

			// Apply feedback step
			qDot1 -= mBeta * s0;
			qDot2 -= mBeta * s1;
			qDot3 -= mBeta * s2;
			qDot4 -= mBeta * s3;
		}
		
		 // Integrate to yield quaternion
        q1 += qDot1 * mSamplingPeriod;
        q2 += qDot2 * mSamplingPeriod;
        q3 += qDot3 * mSamplingPeriod;
        q4 += qDot4 * mSamplingPeriod;
	    norm = 1.0 / Math.sqrt(q2 * q2 + q3 * q3 + q4 * q4 + q1 * q1);    // normalise quaternion
	
	    q2 = q2 * norm;
	    q3 = q3 * norm;
	    q4 = q4 * norm;
	    q1 = q1 * norm;

	    
	    return new Orientation3DObject(q1,q2,q3,q4);
	}
}
