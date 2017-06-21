package com.shimmerresearch.sensors;

import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;
import com.shimmerresearch.sensors.SensorLSM303DLHC.GuiLabelSensors;

public abstract class SensorLSM303 extends AbstractSensor {

	private static final long serialVersionUID = -4885535001690922548L;

	public class GuiLabelSensorTiles{
		public static final String MAG = GuiLabelSensors.MAG;
		public static final String WIDE_RANGE_ACCEL = GuiLabelSensors.ACCEL_WR;
	}

	//--------- Configuration options start --------------
	public static final String[] ListofLSM303AccelRange={"+/- 2g","+/- 4g","+/- 8g","+/- 16g"};  
	public static final Integer[] ListofLSM303DLHCAccelRangeConfigValues={0,1,2,3};  
	//--------- Configuration options end --------------

	
    //--------- Constructors for this class start --------------
    /** This constructor is just used for accessing calibration*/
	public SensorLSM303() {
		super(SENSORS.LSM303);
		initialise();
	}
	
	public SensorLSM303(ShimmerVerObject svo) {
		super(SENSORS.LSM303, svo);
		initialise();
	}

	public SensorLSM303(ShimmerDevice shimmerDevice) {
		super(SENSORS.LSM303, shimmerDevice);
		initialise();
	}
    //--------- Constructors for this class end --------------

}
