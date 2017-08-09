

import com.shimmerresearch.driver.Configuration.Shimmer3.SENSOR_ID;
import com.shimmerresearch.pcDriver.ShimmerPC;

public class Shimmer3Initialization {

	public static void main(String[] args) {
		// TODO Auto-generated method stub// TODO Auto-generated method stub
		String myName="";
		double samplingRate=128;
		int accelRange=1;
		int gsrRange=0;
		Integer[] sensorIDs = new Integer[3];
		sensorIDs[0]= SENSOR_ID.SHIMMER_LSM303_ACCEL;
		sensorIDs[1]= SENSOR_ID.SHIMMER_GSR;
		sensorIDs[2]= SENSOR_ID.HOST_PPG_A13;
		boolean continousSync = false;
		int orientation=0;
		int gyroRange = 0;
		int magRange=0;
		ShimmerPC s = new ShimmerPC(myName, samplingRate, accelRange, gsrRange, sensorIDs, gyroRange, magRange, orientation);
		s.connect("COM12",null);
	}

}
