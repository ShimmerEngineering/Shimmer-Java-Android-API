package com.shimmerresearch.driver;

import com.shimmerresearch.driver.Configuration.CHANNEL_UNITS;
import com.shimmerresearch.driver.Configuration.Shimmer3;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_TYPE;
import com.shimmerresearch.sensors.ShimmerClock;

public abstract class TestObjectCluster {

	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ObjectCluster ojc = new ObjectCluster();
		long time = System.nanoTime();
		long difference = (System.nanoTime()-time)/1000;
		long avgdifference = 0;
		int tries = 10000;
		int numberofojcs=15;
		
		avgdifference = 0;
		for (int j=1;j<tries;j++){
			ojc = new ObjectCluster();
			time = System.nanoTime();
			for (int i=0;i<numberofojcs;i++){
				String name = ShimmerClock.ObjectClusterSensorName.TIMESTAMP_OFFSET + Integer.toString(i);
				ojc.mSensorDataList.add(new SensorData(name, CHANNEL_TYPE.UNCAL.toString(), CHANNEL_UNITS.NO_UNITS, 1.0,false));
			}
			difference = (System.nanoTime()-time)/1000;
			avgdifference=avgdifference+difference;
		}
		avgdifference=avgdifference/10;
		System.out.println(avgdifference);
		
		avgdifference = 0;
		for (int j=1;j<tries;j++){
			ojc = new ObjectCluster();
			time = System.nanoTime();
			for (int i=0;i<numberofojcs;i++){
				String name = ShimmerClock.ObjectClusterSensorName.TIMESTAMP_OFFSET + Integer.toString(i);
				ojc.addDataToMap(name,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,1.0);
			}
			difference = (System.nanoTime()-time)/1000;
			avgdifference=avgdifference+difference;
		}
		avgdifference=avgdifference/10;
		System.out.println(avgdifference);
		

		
	}

	
	
}
