package com.shimmerresearch.verisense.payloaddesign;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.shimmerresearch.verisense.UtilVerisenseDriver;

/**
 * This class represents a list of continuous DataBlocks whereby it has been
 * detected that no time-gap or over-lap has occurred between them.
 * 
 * @author Mark Nolan
 *
 */
public class DataSegmentDetails implements Serializable {

	private static final long serialVersionUID = -4767277394199485045L;
	
	public List<DataBlockDetails> listOfDataBlocks = new ArrayList<DataBlockDetails>();
	public double calculatedSamplingRate = Double.NaN;
	public int sampleCount = 0;

	public void updateSampleCount() {
		sampleCount = 0;
		for(DataBlockDetails dataBlockDetails:listOfDataBlocks) {
			sampleCount += dataBlockDetails.getSampleCount();
		}
	}
	
	public int getSampleCount() {
		return sampleCount;
	}

	public double getStartTimeRwcMs() {
		return getStartTimeMsFromDataBlockList(listOfDataBlocks, true);
	}

	public double getEndTimeRwcMs() {
		return getEndTimeMsFromDataBlockList(listOfDataBlocks, true);
	}

	public double getStartTimeUcClockMs() {
		return getStartTimeMsFromDataBlockList(listOfDataBlocks, false);
	}

	public double getEndTimeUcClockMs() {
		return getEndTimeMsFromDataBlockList(listOfDataBlocks, false);
	}

	public static double getStartTimeMsFromDataBlockList(List<DataBlockDetails> listOfDataBlocks, boolean returnRwcTime) {
		if(listOfDataBlocks!=null && listOfDataBlocks.size()>0) {
			// Return the first datablock belonging to the sensor from the list
			DataBlockDetails dataBlockDetails = listOfDataBlocks.get(0); 
			if(returnRwcTime) {
				return dataBlockDetails.getTimeDetailsRwc().getStartTimeMs();
			} else {
				return dataBlockDetails.getTimeDetailsUcClock().getStartTimeMs();
			}
		}
		return VerisenseTimeDetails.DEFAULT_START_TIME_VALUE;
	}

	public static double getEndTimeMsFromDataBlockList(List<DataBlockDetails> listOfDataBlocks, boolean returnRwcTime) {
		if(listOfDataBlocks!=null && listOfDataBlocks.size()>0) {
			// Return the last datablock belonging to the sensor from the list
			DataBlockDetails dataBlockDetails = listOfDataBlocks.get(listOfDataBlocks.size()-1);
			if(returnRwcTime) {
				return dataBlockDetails.getTimeDetailsRwc().getEndTimeMs();
			} else {
				return dataBlockDetails.getTimeDetailsUcClock().getEndTimeMs();
			}
		}
		return VerisenseTimeDetails.DEFAULT_END_TIME_VALUE;
	}

	public double getCalculatedSamplingRate() {
		return calculatedSamplingRate;
	}
	
	public void updateCalculatedSamplingRate() {
		if(getSampleCount()>1) {
			calculatedSamplingRate = UtilVerisenseDriver.calcSamplingRate(getStartTimeRwcMs(), getEndTimeRwcMs(), getSampleCount());
		}
	}

	public String generateReport() {
		return ("DataBlocks=" + getDataBlockCount()
		+ ", Samples=" + getSampleCount()
		+ ", Timimg [Start=" + UtilVerisenseDriver.convertMilliSecondsToCsvHeaderFormat((long) getStartTimeRwcMs())
		+ ", End=" + UtilVerisenseDriver.convertMilliSecondsToCsvHeaderFormat((long) getEndTimeRwcMs())
		+ ", Duration=" + UtilVerisenseDriver.convertSecondsToHHmmssSSS((getEndTimeRwcMs()-getStartTimeRwcMs())/1000)
		 + "]"
//		+ ", CalculatedSamplingRate=" + getCalculatedSamplingRate()
		);
	}

	public List<DataBlockDetails> getListOfDataBlocks() {
		return listOfDataBlocks;
	}

	public int getDataBlockCount() {
		return listOfDataBlocks.size();
	}

	public static void printListOfDataBlockDetails(List<DataBlockDetails> listOfDataBlocks, boolean showTimeDiff) {
		int index = 1;
		double lastEndTime = Double.NaN;
		for(DataBlockDetails dataBlockDetails:listOfDataBlocks) {
			double timeDiffMs = Double.NaN;
			if(!Double.isNaN(lastEndTime)) {
				timeDiffMs =  dataBlockDetails.getStartTimeRwcMs() - lastEndTime;
			}
			System.out.println(index++ + ") " + dataBlockDetails.generateDebugStr() 
				+ (showTimeDiff? ", TimeDiff=" + (Double.isNaN(timeDiffMs)? UtilVerisenseDriver.FEATURE_NOT_AVAILABLE:(timeDiffMs+"ms")):""));
			lastEndTime = dataBlockDetails.getEndTimeRwcMs();
		}
	}

	public void addDataBlock(DataBlockDetails dataBlockDetails) {
		listOfDataBlocks.add(dataBlockDetails);
	}

	public void addDataBlocks(List<DataBlockDetails> listOfDataBlockDetails) {
		listOfDataBlocks.addAll(listOfDataBlockDetails);
	}

	public void clearOjcArray() {
		for(DataBlockDetails dataBlockDetails:listOfDataBlocks) {
			dataBlockDetails.setupOjcArray(0);
		}
	}
	
	public DataSegmentDetails deepClone() {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(this);

			ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
			ObjectInputStream ois = new ObjectInputStream(bais);
			return (DataSegmentDetails) ois.readObject();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

	public boolean isResultOfSplitAtMiddayOrMidnight() {
		if(listOfDataBlocks.size()>0) {
			DataBlockDetails firstDataBlockDetails = listOfDataBlocks.get(0);
			return firstDataBlockDetails.isResultOfSplitAtMiddayOrMidnight();
		}
		return false;
	}
	
}
