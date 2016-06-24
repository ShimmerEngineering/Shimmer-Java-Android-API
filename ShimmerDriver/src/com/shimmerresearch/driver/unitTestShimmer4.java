package com.shimmerresearch.driver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.shimmerresearch.driverUtilities.CalibDetailsKinematic;
import com.shimmerresearch.driverUtilities.SensorDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.FW_ID;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.HW_ID;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.HW_ID_SR_CODES;
import com.shimmerresearch.sensors.AbstractSensor;

public class unitTestShimmer4 {

	static ShimmerDevice shimmer4;
	
	@Before
	public void before() {
		
	}

	@BeforeClass
	public static void setUp() {
//		System.out.println("Started");

		ShimmerVerObject sVO = new ShimmerVerObject(HW_ID.SHIMMER_4_SDK, FW_ID.SHIMMER4_SDK_STOCK, 0, 0, 1, HW_ID_SR_CODES.SHIMMER_4_SDK);
		shimmer4 = new Shimmer4();
		shimmer4.setShimmerVersionObject(sVO);
		shimmer4.prepareAllAfterConfigRead();
	}

	@Test
	public void testKinematicReadRanges() throws ExecutionException{
		List<Integer> listOfSensorMapKeys = Arrays.asList(
				Configuration.Shimmer3.SensorMapKey.SHIMMER_ANALOG_ACCEL,
				Configuration.Shimmer3.SensorMapKey.SHIMMER_LSM303DLHC_ACCEL,
				Configuration.Shimmer3.SensorMapKey.SHIMMER_LSM303DLHC_MAG,
				Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_GYRO);
		for(Integer sensorMapKey:listOfSensorMapKeys){
			SensorDetails sensorDetails = shimmer4.getSensorDetails(sensorMapKey);
			if(sensorDetails!=null){
				System.out.print(sensorDetails.mSensorDetailsRef.mGuiFriendlyLabel + "\t Range:");
				
				Object returnValueAccelRange = shimmer4.getConfigValueUsingConfigLabel(sensorMapKey, AbstractSensor.GuiLabelConfigCommon.RANGE);
				if(returnValueAccelRange!=null){
					if(returnValueAccelRange instanceof Integer){
						Integer rangeCast = (Integer)returnValueAccelRange;
						System.out.println(rangeCast);
					}
				}
				else {
					System.out.println("NULL");
				}
			}
		}
		
		//TODO
//		assertEquals(false, retrievedBalance.getLivemode());
//		assertFalse(createdCharge.getRefunded());
//		assertTrue(retrievedCharge.getSource());
//		assertNotNull(retrievedCharge.getSource());
	}
	
	@Test
	public void testReadAllKinematicCalibrationParameters() throws ExecutionException{
//		Object returnValue = shimmer4.getConfigValueUsingConfigLabel(Integer.toString(Configuration.Shimmer3.SensorMapKey.SHIMMER_ANALOG_ACCEL), AbstractSensor.GuiLabelConfigCommon.KINEMATIC_CALIBRATION);
		Object returnValue = shimmer4.getConfigValueUsingConfigLabel(Configuration.Shimmer3.GuiLabelConfig.KINEMATIC_CALIBRATION_ALL);
		if(returnValue!=null){
			TreeMap<Integer, TreeMap<Integer, CalibDetailsKinematic>> mapOfKinematicSensorCalibration = (TreeMap<Integer, TreeMap<Integer, CalibDetailsKinematic>>)returnValue;
			for(Integer sensorMapKey:mapOfKinematicSensorCalibration.keySet()){
				SensorDetails sensorDetails = shimmer4.getSensorDetails(sensorMapKey);
				if(sensorDetails!=null){
					System.out.println(sensorDetails.mSensorDetailsRef.mGuiFriendlyLabel);
					TreeMap<Integer, CalibDetailsKinematic> mapOfKinematicCalibPerRange = mapOfKinematicSensorCalibration.get(sensorMapKey);
					for(CalibDetailsKinematic calibDetailsKinematic:mapOfKinematicCalibPerRange.values()){
						System.out.println(calibDetailsKinematic.generateDebugString());
					}
					System.out.println();
				}
			}
		}
//		System.out.println("Finished");
	}

}
