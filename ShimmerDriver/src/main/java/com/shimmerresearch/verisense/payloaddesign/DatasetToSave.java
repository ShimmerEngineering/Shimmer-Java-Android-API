package com.shimmerresearch.verisense.payloaddesign;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.Map.Entry;

import com.shimmerresearch.driver.Configuration.CHANNEL_UNITS;
import com.shimmerresearch.sensors.AbstractSensor.SENSORS;
import com.shimmerresearch.verisense.UtilVerisenseDriver;

public class DatasetToSave implements Serializable {

	private static final long serialVersionUID = -7451676654204282601L;
	
	// TreeMap in order to keep the CSV header config lines in alphanumeric order per sensor name 
	private TreeMap<SENSORS, List<DataSegmentDetails>> mapOfDataSegmentsPerSensor = new TreeMap<SENSORS, List<DataSegmentDetails>>();

	public List<DataSegmentDetails> getListOfDataSegmentsForSensorClassKey(SENSORS sensorClassKey) {
		return mapOfDataSegmentsPerSensor.get(sensorClassKey);
	}

	public double getStartTimeRwcMsPerSensor(SENSORS sensorClassKey) {
		List<DataSegmentDetails> listOfDataSegments = mapOfDataSegmentsPerSensor.get(sensorClassKey);
		return listOfDataSegments.get(0).getStartTimeRwcMs();
	}

	public double getEndTimeRwcMsPerSensor(SENSORS sensorClassKey) {
		List<DataSegmentDetails> listOfDataSegments = mapOfDataSegmentsPerSensor.get(sensorClassKey);
		return listOfDataSegments.get(listOfDataSegments.size()-1).getEndTimeRwcMs();
	}

	public double getStartTimeUcClockMsPerSensor(SENSORS sensorClassKey) {
		List<DataSegmentDetails> listOfDataSegments = mapOfDataSegmentsPerSensor.get(sensorClassKey);
		return listOfDataSegments.get(0).getStartTimeUcClockMs();
	}

	public double getEndTimeUcClockMsPerSensor(SENSORS sensorClassKey) {
		List<DataSegmentDetails> listOfDataSegments = mapOfDataSegmentsPerSensor.get(sensorClassKey);
		return listOfDataSegments.get(listOfDataSegments.size()-1).getEndTimeUcClockMs();
	}

	public double findCsvStartTimeRwcMs() {
		double startTimeMs = VerisenseTimeDetails.DEFAULT_START_TIME_VALUE;
		for(List<DataSegmentDetails> listOfDataSegments:mapOfDataSegmentsPerSensor.values()) {
			for(DataSegmentDetails dataSegmentDetails:listOfDataSegments) {
				startTimeMs = Math.min(startTimeMs, dataSegmentDetails.getStartTimeRwcMs());
			}
		}
		return startTimeMs;
	}

	public double findCsvEndTimeRwcMs() {
		double endTimeMs = VerisenseTimeDetails.DEFAULT_END_TIME_VALUE;
		for(List<DataSegmentDetails> listOfDataSegments:mapOfDataSegmentsPerSensor.values()) {
			for(DataSegmentDetails dataSegmentDetails:listOfDataSegments) {
				endTimeMs = Math.max(endTimeMs, dataSegmentDetails.getEndTimeRwcMs());
			}
		}
		return endTimeMs;
	}

	public double findCsvStartTimeUcClockMs() {
		double startTimeMs = VerisenseTimeDetails.DEFAULT_START_TIME_VALUE;
		for(List<DataSegmentDetails> listOfDataSegments:mapOfDataSegmentsPerSensor.values()) {
			for(DataSegmentDetails dataSegmentDetails:listOfDataSegments) {
				startTimeMs = Math.min(startTimeMs, dataSegmentDetails.getStartTimeUcClockMs());
			}
		}
		return startTimeMs;
	}

	public double findCsvEndTimeUcClockMs() {
		double endTimeMs = VerisenseTimeDetails.DEFAULT_END_TIME_VALUE;
		for(List<DataSegmentDetails> listOfDataSegments:mapOfDataSegmentsPerSensor.values()) {
			for(DataSegmentDetails dataSegmentDetails:listOfDataSegments) {
				endTimeMs = Math.max(endTimeMs, dataSegmentDetails.getEndTimeUcClockMs());
			}
		}
		return endTimeMs;
	}

	public void reset() {
		mapOfDataSegmentsPerSensor.clear();
	}

	public void addDataSegmentsForSensorClassKey(SENSORS sensorClassKey, List<DataSegmentDetails> listOfDataSegments, boolean isPayloadDesignV8orAbove) {
		List<DataSegmentDetails> listOfDataSegmentsExisting = mapOfDataSegmentsPerSensor.get(sensorClassKey);
		if(listOfDataSegmentsExisting==null) {
			listOfDataSegmentsExisting = new ArrayList<DataSegmentDetails>();
			putInMapOfDataSegmentsPerSensor(sensorClassKey, listOfDataSegmentsExisting);
		}
		
		int offset = 0;
		if(listOfDataSegmentsExisting.size()>0) {
			// Check if the last data segment is a continuation of the previous one. If so, append it.
			DataSegmentDetails latestExistingDataSegment = listOfDataSegmentsExisting.get(listOfDataSegmentsExisting.size()-1);
			DataSegmentDetails firstNewDataSegment = listOfDataSegments.get(0);
			// File Parser support for older payload designs doesn't have the ability to
			// split into different data segments and so there's only one continous
			// datasegment for the entire CSV
			if(!isPayloadDesignV8orAbove || UtilCsvSplitting.isDataBlockContinuous(sensorClassKey, latestExistingDataSegment, firstNewDataSegment.getListOfDataBlocks().get(0)).isEmpty()) {
				latestExistingDataSegment.addDataBlocks(firstNewDataSegment.getListOfDataBlocks());
				offset = 1;
			}
		}

		//Add the remaining (there should only be more if some time-gaps are tolerated in a single CSV)
		for(int i=offset;i<listOfDataSegments.size();i++) {
			//Needs to be a new instance of the object
			DataSegmentDetails dataSegmentToAdd = listOfDataSegments.get(i);
			DataSegmentDetails dataSegmentDetails = new DataSegmentDetails();
			dataSegmentDetails.addDataBlocks(dataSegmentToAdd.getListOfDataBlocks());
			listOfDataSegmentsExisting.add(dataSegmentDetails);
		}
	}
	
	public void printListOfDataBlockDetailsBySensor() {
		for (Entry<SENSORS, List<DataSegmentDetails>> entry : mapOfDataSegmentsPerSensor.entrySet()) {
			System.out.println("Sorted in temporal order for sensor = " + entry.getKey() + ":");
			int index = 1;
			for(DataSegmentDetails dataSegmentDetails:entry.getValue()) {
				System.out.println("DataSegmentIndex=" + index + ", " + dataSegmentDetails.generateReport());
				DataSegmentDetails.printListOfDataBlockDetails(dataSegmentDetails.getListOfDataBlocks(), true);
				System.out.println("");
			}
		}
	}

	public void printReportOfDataSegments() {
		TreeMap<SENSORS, List<DataSegmentDetails>> mapOfDataSegmentsPerSensor = getMapOfDataSegmentsPerSensor();
		
		System.out.println(" |_ Contents:");
		for(SENSORS sensorClassKey:mapOfDataSegmentsPerSensor.keySet()) {
			System.out.println("   |_ Sensor=" + sensorClassKey + ":");
			int index = 0;
			for(DataSegmentDetails dataSegmentDetails:mapOfDataSegmentsPerSensor.get(sensorClassKey)) {
				System.out.println("     |_ DataSegmentIndex=" + index + ", " + dataSegmentDetails.generateReport());
				index++;
			}
		}
		
		// Warn about time-gaps 
		for(Entry<SENSORS, List<DataSegmentDetails>> entry:mapOfDataSegmentsPerSensor.entrySet()) {
			List<DataSegmentDetails> listOfDataSegmentDetails = entry.getValue();
			if(listOfDataSegmentDetails.size()>1) {
				for(int i=1;i<listOfDataSegmentDetails.size();i++) {
					// Don't warn for midday/midnight transitions
					DataSegmentDetails currentDataSegment = listOfDataSegmentDetails.get(i);
					DataSegmentDetails previousDataSegment = listOfDataSegmentDetails.get(i-1);
					if(currentDataSegment.isResultOfSplitAtMiddayOrMidnight()) {
						System.out.println("   |_ WARNING! Midday/Midnight Transition Detected for sensor=" + entry.getKey()
						+ " between DataSegment " + (i-1) + " and " + i);
					} else {
						double timeMissingMs = currentDataSegment.getStartTimeRwcMs() - previousDataSegment.getEndTimeRwcMs();
						System.out.println("   |_ WARNING! Time-gap in middle of payload for sensor=" + entry.getKey()
						+ " between DataSegment " + (i-1) + " and " + i
						+ " of " + UtilVerisenseDriver.convertSecondsToHHmmssSSS(timeMissingMs/1000) + " (HH:mm:ss.SSS)");
					}
				}
			}
		}
	}

	public TreeMap<SENSORS, List<DataSegmentDetails>> getMapOfDataSegmentsPerSensor() {
		return mapOfDataSegmentsPerSensor;
	}

	public void putInMapOfDataSegmentsPerSensor(SENSORS sensorClassKey, List<DataSegmentDetails> listOfDataSegments) {
		mapOfDataSegmentsPerSensor.put(sensorClassKey, listOfDataSegments);;
	}

	public void updateSampleCountForEachDataSegmentDetails() {
		for (List<DataSegmentDetails> listOfDataSegments : getMapOfDataSegmentsPerSensor().values()) {	
			for(DataSegmentDetails dataSegmentDetails:listOfDataSegments) {
				dataSegmentDetails.updateSampleCount();
			}
		}
	}

	public double calculateSamplingRateForSensor(SENSORS sensorClassKey) {
		System.out.println("  |_ Calculating average sampling rate for sensor=" + sensorClassKey);
		double averageSamplingRate = Double.NaN;
		List<DataSegmentDetails> listOfDataSegments = mapOfDataSegmentsPerSensor.get(sensorClassKey);
		if(listOfDataSegments!=null && listOfDataSegments.size()>0) {
			List<Double> listOfSamplingRates = new ArrayList<Double>();
			for (int i = 0; i < listOfDataSegments.size(); i++) {
				DataSegmentDetails dataSegmentDetails = listOfDataSegments.get(i);
				dataSegmentDetails.updateCalculatedSamplingRate();
				double calculatedSamplingRate = dataSegmentDetails.getCalculatedSamplingRate();

				System.out.println("    |_ DataSegmentIndex=" + i
						+ ", " + dataSegmentDetails.generateReport()
						+ " => SamplingRate=" + (Double.isNaN(calculatedSamplingRate)? UtilVerisenseDriver.UNAVAILABLE:UtilVerisenseDriver.formatDoubleToNdecimalPlaces(calculatedSamplingRate, 3) + CHANNEL_UNITS.FREQUENCY));
				
				if(!Double.isNaN(calculatedSamplingRate)) {
					listOfSamplingRates.add(calculatedSamplingRate);
				}
			}
			
			// In a way this is a bad approach because it's not great to be calculating the
			// average of sampling rates where they could be the result of small or large
			// recordings but, since the PPG recordings should be all of the same duration,
			// the chances of a smaller recording happening (e.g. cut off at the start or
			// end of a CSV is small)
			averageSamplingRate = UtilVerisenseDriver.calculateAverage(listOfSamplingRates);
			System.out.println("      |_ AverageSamplingRate=" + UtilVerisenseDriver.formatDoubleToNdecimalPlaces(averageSamplingRate, 3) + CHANNEL_UNITS.FREQUENCY);
		}
		
		return averageSamplingRate;
	}

	public long calculateSampleCount() {
		long sampleCount = 0;
		for (List<DataSegmentDetails> listOfDataSegments : getMapOfDataSegmentsPerSensor().values()) {	
			for(DataSegmentDetails dataSegmentDetails:listOfDataSegments) {
				sampleCount += dataSegmentDetails.getSampleCount();
			}
		}
		return sampleCount;
	}

	public long calculateDataBlockCount() {
		long dataBlockCount = 0;
		for (List<DataSegmentDetails> listOfDataSegments : getMapOfDataSegmentsPerSensor().values()) {	
			for(DataSegmentDetails dataSegmentDetails:listOfDataSegments) {
				dataBlockCount += dataSegmentDetails.listOfDataBlocks.size();
			}
		}
		return dataBlockCount;
	}

	public List<DataSegmentDetails> getOfCreateListOfDataSegmentsForSensor(SENSORS sensorClassKey) {
		List<DataSegmentDetails> listOfDataSegmentsExisting = mapOfDataSegmentsPerSensor.get(sensorClassKey);
		if(listOfDataSegmentsExisting==null) {
			listOfDataSegmentsExisting = new ArrayList<DataSegmentDetails>();
			putInMapOfDataSegmentsPerSensor(sensorClassKey, listOfDataSegmentsExisting);
		}
		return listOfDataSegmentsExisting;
	}

}
