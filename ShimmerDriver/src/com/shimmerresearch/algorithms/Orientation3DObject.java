package com.shimmerresearch.algorithms;

public class Orientation3DObject {

	private double quaternionW, quaternionX, quaternionY, quaternionZ;
	private double rho;
	private double theta, angleX, angleY, angleZ;

    public Orientation3DObject(double q1, double q2, double q3, double q4){
        this.quaternionW = q1;
        this.quaternionX = q2;
        this.quaternionY = q3;
        this.quaternionZ = q4;
        calculateAngles();
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
    
}
