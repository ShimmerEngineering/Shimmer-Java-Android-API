package com.shimmerresearch.verisense.payloaddesign;

import java.util.HashMap;
import java.util.List;

import com.shimmerresearch.driver.Configuration.CHANNEL_UNITS;
import com.shimmerresearch.sensors.AbstractSensor.SENSORS;
import com.shimmerresearch.verisense.UtilVerisenseDriver;
import com.shimmerresearch.verisense.VerisenseDevice;
import com.shimmerresearch.verisense.payloaddesign.DataBlockDetails.DATABLOCK_SENSOR_ID;
import com.shimmerresearch.verisense.sensors.SensorMLX90632;
import com.shimmerresearch.verisense.sensors.SensorVD6283;

public class UtilCsvSplitting {

	public class FILE_GAP_TOLERANCE_MULTIPLIER {
		// +/- 10%
		public static final double UPPER = 1.1;
		public static final double LOWER = 0.9;
		/**
		 * Slow sensors only (VD6283 light / MLX90632 skin temp): the largest
		 * inter-block gap, as a multiple of the achieved median block spacing, that
		 * is still treated as continuous. The MLX90632's conversions can slip by
		 * several refresh periods and then catch up (observed up to +12.5% block
		 * spacing on the DEV-927 validation recording with no samples lost), and the
		 * window is seeded from the first payload that carries >= 2 blocks - often a
		 * single inter-block gap, i.e. no spread information - so the standard
		 * LOWER (-10%) band is routinely violated by healthy data. A genuinely
		 * dropped block doubles the spacing (2x), so 1.5x keeps comfortable margin
		 * on both sides.
		 */
		public static final double SLOW_SENSOR_MAX_INTER_BLOCK_GAP_RATIO = 1.5;
		/**
		 * MLX90632 only: how far a SINGLE block boundary apparent rate may sit
		 * either side of the configured output rate and still be plausible.
		 * <p>
		 * The chip conversions slip by several refresh periods and then catch up
		 * (+12.5% block spacing observed on the DEV-927 validation recording, with
		 * no samples lost), so one boundary reads ~12.5% slow and the next
		 * correspondingly fast. Only the AVERAGE rate is bounded by the configured
		 * one, which is why the configured rate alone is too tight a bound. 1.15
		 * covers the observed slip with a little margin.
		 * <p>
		 * Not used for the VD6283, whose sampling is a plain periodic timer with no
		 * slip-and-catch-up behaviour: a failed read there costs a whole period, it
		 * never shortens one.
		 */
		public static final double SLOW_SENSOR_CONVERSION_SLIP_TOLERANCE = 1.15;
	}
	
	protected static HashMap<SENSORS, double[]> SAMPLING_RATE_LIMITS_PER_SENSOR = new HashMap<SENSORS, double[]>(); 

	public static boolean isTsDifferenceOutsideOfLimits(double expectedPayloadTsDiffLimits[], double unixTimeInMs_1, double unixTimeInMs_2) {
		double differenceInMillisec = Math.abs(unixTimeInMs_1 - unixTimeInMs_2);
		if(differenceInMillisec < expectedPayloadTsDiffLimits[0] || differenceInMillisec > expectedPayloadTsDiffLimits[1]) {
			return true;
		}
		return false;
	}

	public static String isSamplingRateOutsideOfLimits(double[] samplingRateLimits, DataBlockDetails previousBlockDetails, DataBlockDetails nextBlockDetails, SENSORS sensorClassKey) {
		// The end time in the payload and data blocks comes from the sensor whereas the
		// start time is calculated by the file parser from the end time and the
		// configured sampling rate. As the sampling rate in the Verisense chips can
		// drift, it's better to check that we are getting the correct the number of
		// samples between the end time of the one datablock and the end time of
		// the next block (i.e., the average sampling rate is within a reasonable
		// tolerance) rather than checking the time diff between the end of one
		// datablock and the start of the next datablock.
		double calculatedSamplingRate = UtilVerisenseDriver.calcSamplingRate(previousBlockDetails.getEndTimeRwcMs(), nextBlockDetails.getEndTimeRwcMs(), nextBlockDetails.getSampleCount());
		if(Double.isNaN(calculatedSamplingRate)) {
			return ("WARNING!!! Unable to calculate sampling rate");
		}
		
		if(isSamplingRateOutsideOfLimits(samplingRateLimits, calculatedSamplingRate)) {
			//UtilShimmer.consolePrintCurrentStackTrace();
			
			double timeGapS = Math.abs((nextBlockDetails.getStartTimeRwcMs()-previousBlockDetails.getEndTimeRwcMs())/1000);
			
			String timeGapLocation = (previousBlockDetails.getPayloadIndex()==nextBlockDetails.getPayloadIndex()? "datablocks":"payloads");
			return("WARNING!!! Unexpected sampling rate or time-gap detected for sensor " + sensorClassKey + " in between " + timeGapLocation + ": " 
					+ "\n  |_1) " + previousBlockDetails.generateDebugStr() 
					+ "\n  |_2) " + nextBlockDetails.generateDebugStr() 

//					+ "\n    |_Time between datablocks=" + timeToStr(nextBlockDetails.getStartTimeMs()-previousBlockDetails.getEndTimeMs())
					+ "\n    |_Time between datablocks=" + UtilVerisenseDriver.convertSecondsToHHmmssSSS(timeGapS) + " (HH:mm:ss.SSS)"
					+ "\n    |_Detected=" + freqToStr(calculatedSamplingRate) //+ " (" + timeToStr(1/calculatedSamplingRate) + ")"
					+ ", Limits: Min=" + freqToStr(samplingRateLimits[0]) + " (" + timeToStr(1/samplingRateLimits[0]) + ")"
					+ ", Max=" + freqToStr(samplingRateLimits[1]) + " (" + timeToStr(1/samplingRateLimits[1]) + ")"
//					+ "\n    |_Payload index " + nextBlockDetails.payloadIndex + " EndTime [Minutes=" + nextBlockDetails.rtcEndTimeMinutes + ", Ticks=" + nextBlockDetails.rtcEndTimeTicks + "]"
					);
		}
		return "";
	}

	public static boolean isSamplingRateOutsideOfLimits(double[] samplingRateLimits, double samplingRate) {
		if(samplingRate < samplingRateLimits[0] || samplingRate > samplingRateLimits[1]) {
			return true;
		}
		return false;
	}

	public static void populateExpectedPayloadTsDiffLimitMapIfNeeded(VerisenseDevice verisenseDevice, HashMap<DATABLOCK_SENSOR_ID, List<SENSORS>> mapOfSensorIdsPerDataBlock) {
		for (List<SENSORS> listOfSensorClassKeys : mapOfSensorIdsPerDataBlock.values()) {
			for (SENSORS sensorClassKey : listOfSensorClassKeys) {
				if(!UtilCsvSplitting.SAMPLING_RATE_LIMITS_PER_SENSOR.containsKey(sensorClassKey)) {
					double configuredSamplingRate = verisenseDevice.getSamplingRateForSensor(sensorClassKey);
					double[] samplingRateLimits = calculateSamplingRateLimits(configuredSamplingRate);
					UtilCsvSplitting.SAMPLING_RATE_LIMITS_PER_SENSOR.put(sensorClassKey, samplingRateLimits);
				}
			}
		}
	}

	public static double[] calculateSamplingRateLimits(double configuredSamplingRate) {
		// +/- of configured sampling rate
		return new double[] {configuredSamplingRate*FILE_GAP_TOLERANCE_MULTIPLIER.LOWER, configuredSamplingRate*FILE_GAP_TOLERANCE_MULTIPLIER.UPPER};
	}

	/**
	 * Whether the payload header itself carries this slow sensor configured
	 * sample rate, so that nothing has to be inferred from the data.
	 * <p>
	 * VD6283: true once the firmware stores the rate index in header byte 30
	 * bits 6:3 (FW v2.02.000+). MLX90632: always true for an enabled gen-2
	 * device, because the refresh code has always been in header byte 32.
	 *
	 * @param verisenseDevice the device being parsed
	 * @param slowSensorId the slow sensor data block id
	 * @return true when the configured rate is known from the header alone
	 */
	public static boolean isSlowSensorConfiguredRateKnown(VerisenseDevice verisenseDevice, DATABLOCK_SENSOR_ID slowSensorId) {
		if(slowSensorId==DATABLOCK_SENSOR_ID.LIGHT) {
			SensorVD6283 sensorVd6283 = verisenseDevice.getSensorVD6283();
			return sensorVd6283!=null && sensorVd6283.isConfiguredRateKnown();
		}
		if(slowSensorId==DATABLOCK_SENSOR_ID.SKIN_TEMP) {
			return isRateUsable(verisenseDevice.getSamplingRateForSensor(SENSORS.MLX90632));
		}
		return false;
	}

	private static boolean isRateUsable(double rateHz) {
		return rateHz>0 && !Double.isNaN(rateHz) && !Double.isInfinite(rateHz);
	}

	/**
	 * The {min, max} per-sample rate a slow sensor could legitimately be running
	 * at, which the CSV gap window is then built from.
	 * <p>
	 * When the configured rate is known from the header the range collapses onto
	 * it - exactly for the VD6283, whose timer has no slip behaviour, and widened
	 * by {@link FILE_GAP_TOLERANCE_MULTIPLIER#SLOW_SENSOR_CONVERSION_SLIP_TOLERANCE}
	 * for the MLX90632, whose conversions slip and catch up.
	 * <p>
	 * When it is not known - a light recording from FW earlier than v2.02.000 -
	 * the range is the whole firmware rate table. That is deliberately wide:
	 * [0.5, 20] Hz becomes a [0.33, 22] Hz window, so a 10-sample light block
	 * boundary is accepted anywhere up to 30 s and genuine loss goes unreported.
	 * It is the best that can be done without the rate, it keeps such recordings
	 * in ONE CSV rather than one per block, and it is stateless: nothing is
	 * learned from the data, so no amount of unhealthy data can move it.
	 *
	 * @param verisenseDevice the device being parsed
	 * @param slowSensorId the slow sensor data block id
	 * @return the {min, max} plausible rate in Hz, or null if it cannot be bounded
	 */
	public static double[] getSlowSensorPlausibleRateRangeHz(VerisenseDevice verisenseDevice, DATABLOCK_SENSOR_ID slowSensorId) {
		if(slowSensorId==DATABLOCK_SENSOR_ID.LIGHT) {
			SensorVD6283 sensorVd6283 = verisenseDevice.getSensorVD6283();
			if(sensorVd6283!=null && sensorVd6283.isConfiguredRateKnown()) {
				double configuredRateHz = sensorVd6283.getRateFreq();
				if(isRateUsable(configuredRateHz)) {
					return new double[] {configuredRateHz, configuredRateHz};
				}
			}
			return new double[] {SensorVD6283.MIN_SAMPLE_RATE_HZ, SensorVD6283.MAX_SAMPLE_RATE_HZ};
		}
		if(slowSensorId==DATABLOCK_SENSOR_ID.SKIN_TEMP) {
			double configuredRateHz = verisenseDevice.getSamplingRateForSensor(SENSORS.MLX90632);
			if(isRateUsable(configuredRateHz)) {
				return new double[] {
					configuredRateHz/FILE_GAP_TOLERANCE_MULTIPLIER.SLOW_SENSOR_CONVERSION_SLIP_TOLERANCE,
					configuredRateHz*FILE_GAP_TOLERANCE_MULTIPLIER.SLOW_SENSOR_CONVERSION_SLIP_TOLERANCE};
			}
			return new double[] {SensorMLX90632.MIN_OUTPUT_RATE_HZ, SensorMLX90632.MAX_OUTPUT_RATE_HZ};
		}
		return null;
	}

	/**
	 * Sets a slow sensor CSV gap-splitting window from the rate the payload
	 * header states, replacing the configured-rate +/-10% band that
	 * {@link #populateExpectedPayloadTsDiffLimitMapIfNeeded} would otherwise
	 * install.
	 * <p>
	 * The gap side is {@code min / SLOW_SENSOR_MAX_INTER_BLOCK_GAP_RATIO} so
	 * healthy jitter stays continuous while a dropped block, which doubles the
	 * spacing and so halves the apparent rate, is reported. The fast side is
	 * {@code max * UPPER}, which catches an overlapping block or a backwards
	 * clock step. Worked through at 1 Hz the window is [0.667, 1.1] Hz: a dropped
	 * block presents 0.5 Hz and splits, a block that took one extra period after
	 * a failed I2C read presents 0.909 Hz and does not.
	 * <p>
	 * Nothing here depends on previously seen data, so unlike a measured window
	 * this cannot be pulled onto a wrong cadence by the very loss it is meant to
	 * detect, and it is correct from the FIRST boundary of a CSV set rather than
	 * after several. The put is unconditional because
	 * populateExpectedPayloadTsDiffLimitMapIfNeeded is containsKey-guarded and
	 * runs later in the parse: this window must win.
	 *
	 * @param verisenseDevice the device being parsed
	 * @param slowSensorId the slow sensor data block id
	 */
	public static void seedSlowSensorGapWindow(VerisenseDevice verisenseDevice, DATABLOCK_SENSOR_ID slowSensorId) {
		double[] plausibleRateRangeHz = getSlowSensorPlausibleRateRangeHz(verisenseDevice, slowSensorId);
		if(plausibleRateRangeHz==null) {
			return;
		}
		double[] samplingRateLimits = new double[] {
			plausibleRateRangeHz[0]/FILE_GAP_TOLERANCE_MULTIPLIER.SLOW_SENSOR_MAX_INTER_BLOCK_GAP_RATIO,
			plausibleRateRangeHz[1]*FILE_GAP_TOLERANCE_MULTIPLIER.UPPER};
		for(SENSORS sensorClassKey:verisenseDevice.getOrCreateListOfSensorClassKeysForDataBlockId(slowSensorId)) {
			if(sensorClassKey!=SENSORS.CLOCK) {
				SAMPLING_RATE_LIMITS_PER_SENSOR.put(sensorClassKey, samplingRateLimits);
			}
		}
	}

	public static void clearMapOfSamplingRateLimitsPerSensor() {
		SAMPLING_RATE_LIMITS_PER_SENSOR.clear();
	}
	
	public static String isDataBlockContinuous(SENSORS sensorClassKey, DataSegmentDetails dataSegmentDetailsPrevious, DataBlockDetails nextDataBlockDetails) {
		//Get last data block from existing dataset
		DataBlockDetails previousDataBlockDetails = dataSegmentDetailsPrevious.getListOfDataBlocks().get(dataSegmentDetailsPrevious.getDataBlockCount()-1);
		
		double[] samplingRateLimits = UtilCsvSplitting.SAMPLING_RATE_LIMITS_PER_SENSOR.get(sensorClassKey);
		if(samplingRateLimits==null) {
			return ("WARNING!!! Sampling Rate Limits not set for sensor = " + sensorClassKey);
		}
		return UtilCsvSplitting.isSamplingRateOutsideOfLimits(samplingRateLimits, previousDataBlockDetails, nextDataBlockDetails, sensorClassKey);
	}
	
	private static String freqToStr(double freq) {
		return UtilVerisenseDriver.formatDoubleToNdecimalPlaces(freq, 2) + " " + CHANNEL_UNITS.FREQUENCY;
	}

	private static String timeToStr(double ts) {
		return UtilVerisenseDriver.formatDoubleToNdecimalPlaces(ts, 3) + " " + CHANNEL_UNITS.SECONDS;
	}

}
