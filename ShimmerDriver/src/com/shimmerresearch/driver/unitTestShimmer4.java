package com.shimmerresearch.driver;

import static org.junit.Assert.*;

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
import com.shimmerresearch.driverUtilities.UtilShimmer;
import com.shimmerresearch.sensors.AbstractSensor;

public class unitTestShimmer4 {

	static ShimmerDevice shimmerDevice;
	private static final String titleSurround = "\n--------------------------------------";

	private void printTestTitle(String title){
		System.out.println(titleSurround + "\nTEST: " + title + titleSurround + "\n");
	}
	
	@Before
	public void before() {
		
	}

	@BeforeClass
	public static void setUp() {
		ShimmerVerObject sVO = new ShimmerVerObject(HW_ID.SHIMMER_4_SDK, FW_ID.SHIMMER4_SDK_STOCK, 0, 0, 1, HW_ID_SR_CODES.SHIMMER_4_SDK);
		shimmerDevice = new Shimmer4();
//		shimmerDevice = new ShimmerPCMSS();
		shimmerDevice.setShimmerVersionObject(sVO);
		shimmerDevice.prepareAllAfterConfigRead();
	}

	@Test
	public void testConfigKinematicRangesRead() throws AssertionError{
		printTestTitle("testConfigKinematicRangesRead");
		
		List<Integer> listOfSensorMapKeys = Arrays.asList(
				Configuration.Shimmer3.SensorMapKey.SHIMMER_ANALOG_ACCEL,
				Configuration.Shimmer3.SensorMapKey.SHIMMER_LSM303DLHC_ACCEL,
				Configuration.Shimmer3.SensorMapKey.SHIMMER_LSM303DLHC_MAG,
				Configuration.Shimmer3.SensorMapKey.SHIMMER_MPU9150_GYRO);
		for(Integer sensorMapKey:listOfSensorMapKeys){
			SensorDetails sensorDetails = shimmerDevice.getSensorDetails(sensorMapKey);
			assertNotNull(sensorDetails);

			System.out.print(sensorDetails.mSensorDetailsRef.mGuiFriendlyLabel + "\t Range:");
			
			Object returnedRange = shimmerDevice.getConfigValueUsingConfigLabel(sensorMapKey, AbstractSensor.GuiLabelConfigCommon.RANGE);
			if(returnedRange!=null){
				if(returnedRange instanceof Integer){
					Integer rangeCast = (Integer)returnedRange;
					System.out.println(rangeCast);
				}
			}
			else {
				System.out.println("NULL");
				assertNotNull(returnedRange);
			}
		}
	}
	
	@Test
	public void testConfigKinematicCalParamRead() throws AssertionError{
		printTestTitle("testConfigKinematicCalParamRead");

		Object returnValue = shimmerDevice.getConfigValueUsingConfigLabel(Configuration.Shimmer3.GuiLabelConfig.KINEMATIC_CALIBRATION_ALL);
		assertNotNull(returnValue);

		TreeMap<Integer, TreeMap<Integer, CalibDetailsKinematic>> mapOfKinematicSensorCalibrationAll = (TreeMap<Integer, TreeMap<Integer, CalibDetailsKinematic>>)returnValue;
		for(Integer sensorMapKey:mapOfKinematicSensorCalibrationAll.keySet()){
			TreeMap<Integer, CalibDetailsKinematic> route1CalParamMapPerSensor = mapOfKinematicSensorCalibrationAll.get(sensorMapKey);
			
			SensorDetails sensorDetails = shimmerDevice.getSensorDetails(sensorMapKey);
			assertNotNull(sensorDetails);
			
			System.out.println(sensorDetails.mSensorDetailsRef.mGuiFriendlyLabel);
			TreeMap<Integer, CalibDetailsKinematic> mapOfKinematicCalibPerRange = mapOfKinematicSensorCalibrationAll.get(sensorMapKey);
			for(CalibDetailsKinematic calibDetailsKinematic:mapOfKinematicCalibPerRange.values()){
				System.out.println(calibDetailsKinematic.generateDebugString());
			}
			System.out.println();
			
			Object returnValuePerSensor = shimmerDevice.getConfigValueUsingConfigLabel(sensorMapKey, AbstractSensor.GuiLabelConfigCommon.KINEMATIC_CALIBRATION);
			assertNull(returnValuePerSensor);
			TreeMap<Integer, TreeMap<Integer, CalibDetailsKinematic>> mapOfKinematicSensorCalibration = (TreeMap<Integer, TreeMap<Integer, CalibDetailsKinematic>>)returnValue;
			TreeMap<Integer, CalibDetailsKinematic> route2CalParamMapPerSensor = mapOfKinematicSensorCalibration.get(sensorMapKey);
			assertNotNull(route2CalParamMapPerSensor);

			for(Integer rangeValue:route1CalParamMapPerSensor.keySet()){
				CalibDetailsKinematic route1CalibDetails = route1CalParamMapPerSensor.get(rangeValue); 
				CalibDetailsKinematic route2CalibDetails = route2CalParamMapPerSensor.get(rangeValue); 
				
				assertTrue(Arrays.deepEquals(route1CalibDetails.mDefaultAlignmentMatrix, route2CalibDetails.mDefaultAlignmentMatrix));
				assertTrue(Arrays.deepEquals(route1CalibDetails.mCurrentAlignmentMatrix, route2CalibDetails.mCurrentAlignmentMatrix));
				assertTrue(Arrays.deepEquals(route1CalibDetails.mDefaultSensitivityMatrix, route2CalibDetails.mDefaultSensitivityMatrix));
				assertTrue(Arrays.deepEquals(route1CalibDetails.mCurrentSensitivityMatrix, route2CalibDetails.mCurrentSensitivityMatrix));
				assertTrue(Arrays.deepEquals(route1CalibDetails.mDefaultOffsetVector, route2CalibDetails.mDefaultOffsetVector));
				assertTrue(Arrays.deepEquals(route1CalibDetails.mCurrentOffsetVector, route2CalibDetails.mCurrentOffsetVector));
			}
		}
	}

}
