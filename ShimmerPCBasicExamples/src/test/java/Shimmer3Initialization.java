

import com.shimmerresearch.driver.Configuration.Shimmer3.SENSOR_ID;
import com.shimmerresearch.pcDriver.ShimmerPC;

public class Shimmer3Initialization {

	public static void main(String[] args) {
		// TODO Auto-generated method stub// TODO Auto-generated method stub
		String myName="";
		double samplingRate=512;
		int accelRange=0;
		int gsrRange=4;
		Integer[] sensorIDs = new Integer[3];
		sensorIDs[0]= SENSOR_ID.SHIMMER_LSM303_MAG;
		sensorIDs[1]= SENSOR_ID.SHIMMER_MPU9X50_GYRO;
		sensorIDs[2]= SENSOR_ID.HOST_PPG_A12;
		boolean continousSync = false;
		int orientation=0;
		int gyroRange = 1;
		int magRange=5;
		ShimmerPC s = new ShimmerPC(myName, samplingRate, accelRange, gsrRange, sensorIDs, gyroRange, magRange, orientation);
		s.connect("COM12",null);
	}

}
