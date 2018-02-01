package com.shimmerresearch.driver;

import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import com.shimmerresearch.driver.Configuration.CHANNEL_UNITS;
import com.shimmerresearch.driver.Configuration.Shimmer3;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_TYPE;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.shimmerresearch.driver.FormatCluster;
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
		hashMapStoreTest();
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
	
	public static void hashMapStoreTest() {
		//TEST 1 - Storage of items in nested HashMap
		HashMap hashMap1 = null;
		Multimap multiMap1 = null;
		
		int numberOfIterations = 100000;
		long cumulativeHashMapStorageTime = 0;
		long cumulativeMultimapStorageTime = 0;
		
		for(int a=0; a<20; a++) {
			HashMap <String, HashMap<CHANNEL_TYPE, FormatCluster>> hashMap = new HashMap<String, HashMap<CHANNEL_TYPE, FormatCluster>>();
			Multimap<String, FormatCluster> multiMap = HashMultimap.create();

			
			long startHashMapStorage = System.nanoTime();
			for(int i=0; i<numberOfIterations; i++) {
				String sensorName = ShimmerClock.ObjectClusterSensorName.TIMESTAMP_OFFSET + Integer.toString(i);
				HashMap<CHANNEL_TYPE, FormatCluster> formatMap = new HashMap<CHANNEL_TYPE, FormatCluster>();
				formatMap.put(CHANNEL_TYPE.UNCAL, new FormatCluster(CHANNEL_TYPE.UNCAL.toString(), CHANNEL_UNITS.NO_UNITS, 1.0));
				hashMap.put(sensorName, formatMap);
			}
			long endHashMapStorage = System.nanoTime();
				
			long startMultiMapStorage = System.nanoTime();
			for(int i=0; i<numberOfIterations; i++) {
				String sensorName = ShimmerClock.ObjectClusterSensorName.TIMESTAMP_OFFSET + Integer.toString(i);
				multiMap.put(sensorName, new FormatCluster(CHANNEL_TYPE.UNCAL.toString(), CHANNEL_UNITS.NO_UNITS, 2.0));
			}
			long endMultiMapStorage = System.nanoTime();
			
			cumulativeHashMapStorageTime = cumulativeHashMapStorageTime + (endHashMapStorage - startHashMapStorage);
			cumulativeMultimapStorageTime = cumulativeMultimapStorageTime + (endMultiMapStorage - startMultiMapStorage);
			
			System.err.println("Time to put HashMap: " + (endHashMapStorage - startHashMapStorage) + " Time to put Multimap: " + (endMultiMapStorage - startMultiMapStorage));
			
			if(a == 19) {
				hashMap1 = hashMap;
				multiMap1 = multiMap;
			}
			
		}
		
				
		System.err.println("Averages over 20x, HashMap put Time: " + (cumulativeHashMapStorageTime/20) + " Multimap Time: " + (cumulativeMultimapStorageTime/20));

		hashMapRetrieveTest(hashMap1, multiMap1);
	}

	
	public static void hashMapRetrieveTest(HashMap hashMap, Multimap multiMap) {
		
		System.out.print("-------------------------------------------------------------------------\n\n");
		String retrievalKey = ShimmerClock.ObjectClusterSensorName.TIMESTAMP_OFFSET + Integer.toString(99999);
		
		long cumulativeHashMapRetrievalTime = 0;
		long cumulativeMultimapRetrievalTime = 0;
		
		for(int a=0; a<20; a++) {
			HashMap hashMap1 = hashMap; 
			Multimap multiMap1 = multiMap;
			
			long hashMapStartRetrieval = System.nanoTime();
			HashMap<CHANNEL_TYPE, FormatCluster> formatMap = (HashMap<CHANNEL_TYPE, FormatCluster>) hashMap1.get(retrievalKey);
			FormatCluster formatCluster = formatMap.get(CHANNEL_TYPE.UNCAL);
			double data = formatCluster.mData;
			long hashMapEndRetrieval = System.nanoTime();
			
			long multiMapStartRetrieval = System.nanoTime();
			Collection<FormatCluster> allFormats = multiMap.get(retrievalKey);
	        FormatCluster multiMapFormatCluster = ((FormatCluster)ObjectCluster.returnFormatCluster(allFormats, CHANNEL_TYPE.UNCAL.toString()));
	        double multimapData = formatCluster.mData;
	        long multiMapEndRetrieval = System.nanoTime();
	        
	        cumulativeHashMapRetrievalTime = cumulativeHashMapRetrievalTime + (hashMapEndRetrieval - hashMapStartRetrieval);
	        cumulativeMultimapRetrievalTime = cumulativeMultimapRetrievalTime + (multiMapEndRetrieval - multiMapStartRetrieval);
	        
	        System.err.println("HashMap retrieval time: " + (hashMapEndRetrieval - hashMapStartRetrieval) + " Multimap retrieval time: " + (multiMapEndRetrieval - multiMapStartRetrieval));
			
		}
        System.err.println("Averages over 20x, HashMap retrieval time: " + cumulativeHashMapRetrievalTime/20 + " Multimap retrieval time: " + cumulativeMultimapRetrievalTime/20);

		
	}
	
	
}
