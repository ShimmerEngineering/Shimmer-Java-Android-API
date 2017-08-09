

import com.shimmerresearch.pcDriver.ShimmerPC;

public class Shimmer3Initialization {

	public static void main(String[] args) {
		// TODO Auto-generated method stub// TODO Auto-generated method stub
		String myName="";
		double samplingRate=128;
		int accelRange=0;
		int gsrRange=4;
		int setEnabledSensors=  ShimmerPC.SENSOR_ACCEL;
		boolean continousSync = false;
		int orientation=0;
		boolean enableLowPowerAccel = false;
		boolean enableLowPowerGyro = false;
		boolean enableLowPowerMag = false;
		int gyroRange = 0;
		int magRange=0;
		ShimmerPC s = new ShimmerPC(myName, samplingRate, accelRange, gsrRange, setEnabledSensors, continousSync, gyroRange, magRange, orientation);
		s.connect("COM12",null);
	}

}
