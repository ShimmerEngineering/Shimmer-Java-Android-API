package com.shimmerresearch.algorithms.verisense.gyroAutoCal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.shimmerresearch.algorithms.AlgorithmDetails;
import com.shimmerresearch.algorithms.AlgorithmDetails.SENSOR_CHECK_METHOD;
import com.shimmerresearch.algorithms.gyroOnTheFlyCal.GyroOnTheFlyCalModule;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driver.Configuration.CHANNEL_UNITS;
import com.shimmerresearch.verisense.sensors.SensorLSM6DS3;

public class GyroOnTheFlyCalModuleVerisense extends GyroOnTheFlyCalModule {

	private static final long serialVersionUID = -8105079411104081532L;
	
	public static final AlgorithmDetails algoGyroOnTheFlyCalVerisense = new AlgorithmDetails(
			GENERAL_ALGORITHM_NAME,
			GENERAL_ALGORITHM_NAME,
			new ArrayList<String>(),
			Configuration.Verisense.DerivedSensorsBitMask.GYRO_ON_THE_FLY_CAL,
			Arrays.asList(Configuration.Verisense.SENSOR_ID.LSM6DS3_GYRO),
			CHANNEL_UNITS.NO_UNITS);
	{
		algoGyroOnTheFlyCalVerisense.mSensorCheckMethod = SENSOR_CHECK_METHOD.ANY;
	}

	public static final Map<String, AlgorithmDetails> mAlgorithmMapRefVerisense;
	static {
		Map<String, AlgorithmDetails> aMap = new LinkedHashMap<String, AlgorithmDetails>();
		aMap.put(algoGyroOnTheFlyCalVerisense.mAlgorithmName, algoGyroOnTheFlyCalVerisense);
		mAlgorithmMapRefVerisense = Collections.unmodifiableMap(aMap);
	}

	public GyroOnTheFlyCalModuleVerisense(ShimmerDevice shimmerDevice, AlgorithmDetails algorithmDetails,
			double samplingRateShimmer) {
		super(shimmerDevice, algorithmDetails, samplingRateShimmer);
		
		super.setSensorId(Configuration.Verisense.SENSOR_ID.LSM6DS3_GYRO);
		super.setGyroAxisLabels(SensorLSM6DS3.ObjectClusterSensorName.LSM6DS3_GYRO_X,
				SensorLSM6DS3.ObjectClusterSensorName.LSM6DS3_GYRO_Y,
				SensorLSM6DS3.ObjectClusterSensorName.LSM6DS3_GYRO_Z);
	}
	
}
