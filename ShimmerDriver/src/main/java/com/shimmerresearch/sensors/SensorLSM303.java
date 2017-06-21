package com.shimmerresearch.sensors;

import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;

public abstract class SensorLSM303 extends AbstractSensor {

	private static final long serialVersionUID = -4885535001690922548L;

    //--------- Constructors for this class start --------------
	public SensorLSM303(SENSORS sensorType) {
		super(sensorType);
		initialise();
	}
	
	public SensorLSM303(SENSORS sensorType, ShimmerVerObject svo) {
		super(sensorType, svo);
		initialise();
	}

	public SensorLSM303(SENSORS sensorType, ShimmerDevice shimmerDevice) {
		super(sensorType, shimmerDevice);
		initialise();
	}
    //--------- Constructors for this class end --------------

}
