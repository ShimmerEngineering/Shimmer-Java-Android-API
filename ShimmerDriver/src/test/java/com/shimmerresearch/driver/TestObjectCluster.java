package com.shimmerresearch.driver;

import java.util.ArrayList;
import java.util.Collection;

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
		
		//JOS: TESTING RETRIEVAL TIME BELOW ------------------------------------------------------
		//INIT OBJECTCLUSTER
		ObjectCluster objectCluster = new ObjectCluster();
		int lastValue = 9999;
		
		for (int j=1;j<tries;j++){
			String name = ShimmerClock.ObjectClusterSensorName.TIMESTAMP_OFFSET + Integer.toString(j);
			objectCluster.mSensorDataList.add(new SensorData(name, CHANNEL_TYPE.UNCAL.toString(), CHANNEL_UNITS.NO_UNITS, 1.0, false));
		}		
		
		for (int j=1;j<tries;j++){
			String name = ShimmerClock.ObjectClusterSensorName.TIMESTAMP_OFFSET + Integer.toString(j);
			objectCluster.addDataToMap(name, CHANNEL_TYPE.UNCAL.toString(), CHANNEL_UNITS.NO_UNITS, 2.0);
		}		
		
		//TEST 1 - Testing ArrayList retrieval time by iterating through list
		long cumulativeListRetrievalTime = 0;
		long cumulativeMultimapRetrievalTime = 0;
		
		
		for(int a = 0; a<20; a++) {
			
			long worstCaseListRetrievalTime = 0;
			long worstCaseMultimapRetrievalTime = 0;
			String retrievalKey = ShimmerClock.ObjectClusterSensorName.TIMESTAMP_OFFSET + Integer.toString(lastValue);
			
			long startListRetrievalTime = System.nanoTime();
			for(SensorData sensorData : objectCluster.mSensorDataList) {
				if(sensorData.mSensorName.contains(retrievalKey)) {
					double listData = sensorData.mSensorData;
	//				System.err.println("SensorData data: " + listData);
					worstCaseListRetrievalTime = System.nanoTime();
				}
			}
			
			cumulativeListRetrievalTime = cumulativeListRetrievalTime + (worstCaseListRetrievalTime - startListRetrievalTime);
			
			long startMultimapRetrievalTime = System.nanoTime();
			Collection<FormatCluster> allFormats = objectCluster.getCollectionOfFormatClusters(retrievalKey);
	        FormatCluster formatCluster = ((FormatCluster)ObjectCluster.returnFormatCluster(allFormats, CHANNEL_TYPE.UNCAL.toString()));
	        double multimapData = formatCluster.mData;
	//		System.err.println("Multimap Data: " + multimapData);
			worstCaseMultimapRetrievalTime = System.nanoTime();
			
			cumulativeMultimapRetrievalTime = cumulativeMultimapRetrievalTime + (worstCaseMultimapRetrievalTime - startMultimapRetrievalTime);
			
			System.err.println("Time to retrieve ArrayList: " + (worstCaseListRetrievalTime - startListRetrievalTime) + " Time to retrieve Multimap: " + (worstCaseMultimapRetrievalTime - startMultimapRetrievalTime)); 
		}
				
		System.err.println("Averages over 20x, ArrayList Time: " + (cumulativeListRetrievalTime/20) + " Multimap Time: " + (cumulativeMultimapRetrievalTime/20));
		
		storeIndexTest(objectCluster);
	}
	
	private static void storeIndexTest(ObjectCluster objectCluster) {
		//TEST 2 - Testing ArrayList retrieval time by iterating once, then storing index
		int lastValue = 9998;
		int index = 0;
		long cumulativeListRetrievalTime = 0;
		long cumulativeMultimapRetrievalTime = 0;
		boolean firstRun = true;
		
		for(int a = 0; a<20; a++) {
			
			long worstCaseListRetrievalTime = 0;
			long worstCaseMultimapRetrievalTime = 0;
			String retrievalKey = ShimmerClock.ObjectClusterSensorName.TIMESTAMP_OFFSET + Integer.toString(lastValue);
			
			long startListRetrievalTime = System.nanoTime();
			if(firstRun) {
				for(int i = 0; i<9999; i++) {
					SensorData sensorData = objectCluster.mSensorDataList.get(i);
					if(sensorData.mSensorName.contains(retrievalKey)) {
						double listData = sensorData.mSensorData;
		//				System.err.println("SensorData data: " + listData);
						index = i;
						worstCaseListRetrievalTime = System.nanoTime();
					}
				}
				firstRun = false;
			} else {
				SensorData sensorData = objectCluster.mSensorDataList.get(index);
				double listData = sensorData.mSensorData;
				worstCaseListRetrievalTime = System.nanoTime();
			}
			
			cumulativeListRetrievalTime = cumulativeListRetrievalTime + (worstCaseListRetrievalTime - startListRetrievalTime);
			
			long startMultimapRetrievalTime = System.nanoTime();
			Collection<FormatCluster> allFormats = objectCluster.getCollectionOfFormatClusters(retrievalKey);
	        FormatCluster formatCluster = ((FormatCluster)ObjectCluster.returnFormatCluster(allFormats, CHANNEL_TYPE.UNCAL.toString()));
	        double multimapData = formatCluster.mData;
	//		System.err.println("Multimap Data: " + multimapData);
			worstCaseMultimapRetrievalTime = System.nanoTime();
			
			cumulativeMultimapRetrievalTime = cumulativeMultimapRetrievalTime + (worstCaseMultimapRetrievalTime - startMultimapRetrievalTime);
			
			System.err.println("Time to retrieve ArrayList: " + (worstCaseListRetrievalTime - startListRetrievalTime) + " Time to retrieve Multimap: " + (worstCaseMultimapRetrievalTime - startMultimapRetrievalTime)); 
		}
				
		System.err.println("Averages over 20x, ArrayList Time: " + (cumulativeListRetrievalTime/20) + " Multimap Time: " + (cumulativeMultimapRetrievalTime/20));

	}

	
	
}
