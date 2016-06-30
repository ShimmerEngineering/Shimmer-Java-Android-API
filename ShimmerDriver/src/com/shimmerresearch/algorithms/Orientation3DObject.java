package com.shimmerresearch.algorithms;

public class Orientation3DObject {

	private double quaternionW, quaternionX, quaternionY, quaternionZ;
	private double rho;
	
	private double theta, angleX, angleY, angleZ;
	/** (Yaw, Pitch, Roll) == (Heading, Attitude, Bank) == (Azimuth, elevation, tilt)*/
	private double yaw, pitch, roll; 
	
	private static boolean isUseQuatToEuler = true;

    public Orientation3DObject(double q1, double q2, double q3, double q4){
        this.quaternionW = q1;
        this.quaternionX = q2;
        this.quaternionY = q3;
        this.quaternionZ = q4;
        calculateAngles();
        calculateEuler();
    }
    
    private void calculateAngles(){
    	rho = Math.acos(quaternionW);
    	theta = rho * 2;
    	angleX = quaternionX / Math.sin(rho);
    	angleY = quaternionY / Math.sin(rho);
    	angleZ = quaternionZ / Math.sin(rho);
    }

	public double getQuaternionW() {
		return quaternionW;
	}

	public double getQuaternionX() {
		return quaternionX;
	}

	public double getQuaternionY() {
		return quaternionY;
	}

	public double getQuaternionZ() {
		return quaternionZ;
	}

	public double getTheta() {
		return theta;
	}

	public double getAngleX() {
		return angleX;
	}

	public double getAngleY() {
		return angleY;
	}

	public double getAngleZ() {
		return angleZ;
	}

	
	//TODO MN temporary code -> needs testing 
	public void calculateEuler() {
		if(isUseQuatToEuler){
			quaternianToEuler(quaternionW, quaternionX, quaternionY, quaternionZ);
		}
		else {
			axisAngleToEuler(angleX, angleY, angleZ, theta);
		}
	}
	
	//TODO MN temporary code -> needs testing 
	//From: http://www.euclideanspace.com/maths/geometry/rotations/conversions/angleToEuler/
	public void axisAngleToEuler(double x, double y, double z, double angle) {
		double s=Math.sin(angle);
		double c=Math.cos(angle);
		double t=1-c;
		//  if axis is not already normalised then uncomment this
		// double magnitude = Math.sqrt(x*x + y*y + z*z);
		// if (magnitude==0) throw error;
		// x /= magnitude;
		// y /= magnitude;
		// z /= magnitude;
		if ((x*y*t + z*s) > 0.998) { // north pole singularity detected
			yaw = 2*Math.atan2(x*Math.sin(angle/2),Math.cos(angle/2));
			pitch = Math.PI/2;
			roll = 0;
			return;
		}
		if ((x*y*t + z*s) < -0.998) { // south pole singularity detected
			yaw = -2*Math.atan2(x*Math.sin(angle/2),Math.cos(angle/2));
			pitch = -Math.PI/2;
			roll = 0;
			return;
		}
		yaw = Math.atan2(y * s- x * z * t , 1 - (y*y+ z*z ) * t);
		pitch = Math.asin(x * y * t + z * s) ;
		roll = Math.atan2(x * s - y * z * t , 1 - (x*x + z*z) * t);
	}
	
	//TODO MN temporary code -> needs testing 
	//From: https://en.wikipedia.org/wiki/Conversion_between_quaternions_and_Euler_angles
	public void quaternianToEuler(double q0, double q1, double q2, double q3){
		roll = Math.atan2(2*(q0*q1 + q2*q3), 1 - 2*(Math.pow(q1,2) + Math.pow(q2,2)));
		pitch = Math.asin(2*(q0*q2 - q3*q1));
		yaw = Math.atan2(2*(q0*q3 + q1*q2), 1- 2*(Math.pow(q2,2) + Math.pow(q3,2)));
	}
	
//	//TODO MN temporary code -> needs testing 
//	//From: http://www.euclideanspace.com/maths/geometry/rotations/conversions/quaternionToEuler/indexLocal.htm
//	public void quaternianToEuler(double q1w, double q1x, double q1y, double q1z){
//		double sqw = q1w*q1w;
//		double sqx = q1x*q1x;
//		double sqy = q1y*q1y;
//		double sqz = q1z*q1z;
//				 
//		yaw = Math.atan2(2.0 * (q1x*q1y + q1z*q1w),(sqx - sqy - sqz + sqw));
//		roll = Math.atan2(2.0 * (q1y*q1z + q1x*q1w),(-sqx - sqy + sqz + sqw));
//		pitch = Math.asin(-2.0 * (q1x*q1z - q1y*q1w)/sqx + sqy + sqz + sqw); 
//	}
	
	public double getYaw() {
		return yaw;
	}

	public double getPitch() {
		return pitch;
	}

	public double getRoll() {
		return roll;
	}
    
}
