package com.shimmerresearch.driver;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import javax.annotation.Signed;
import javax.print.DocFlavor.STRING;

import org.apache.commons.math.stat.descriptive.moment.FirstMoment;

import com.shimmerresearch.driver.Configuration.CHANNEL_UNITS;
import com.shimmerresearch.driver.Configuration.Shimmer3;
import com.shimmerresearch.driver.ObjectCluster.OBJECTCLUSTER_TYPE;
import com.shimmerresearch.driverUtilities.ChannelDetails;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_TYPE;
import com.shimmerresearch.grpc.ShimmerGRPC.ObjectCluster2;
import com.shimmerresearch.grpc.ShimmerGRPC.ObjectCluster2.Builder;
import com.shimmerresearch.grpc.ShimmerGRPC.ObjectCluster2.FormatCluster2;
import com.shimmerresearch.grpc.ShimmerGRPC.ObjectCluster2.FormatCluster2.DataCluster2;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.shimmerresearch.bluetooth.ShimmerBluetooth.BT_STATE;
import com.shimmerresearch.driver.FormatCluster;
import com.shimmerresearch.sensors.ShimmerClock;

public abstract class TestObjectCluster {

	
	
//	public static void main(String[] args) {
//		// TODO Auto-generated method stub
//		ObjectCluster ojc = new ObjectCluster();
//		long time = System.nanoTime();
//		long difference = (System.nanoTime()-time)/1000;
//		long avgdifference = 0;
//		int tries = 10000;
//		int numberofojcs=15;
//		
//		avgdifference = 0;
//		for (int j=1;j<tries;j++){
//			ojc = new ObjectCluster();
//			time = System.nanoTime();
//			for (int i=0;i<numberofojcs;i++){
//				String name = ShimmerClock.ObjectClusterSensorName.TIMESTAMP_OFFSET + Integer.toString(i);
//				ojc.mSensorDataList.add(new SensorData(name, CHANNEL_TYPE.UNCAL.toString(), CHANNEL_UNITS.NO_UNITS, 1.0,false));
//			}
//			difference = (System.nanoTime()-time)/1000;
//			avgdifference=avgdifference+difference;
//		}
//		avgdifference=avgdifference/10;
//		System.out.println(avgdifference);
//		
//		avgdifference = 0;
//		for (int j=1;j<tries;j++){
//			ojc = new ObjectCluster();
//			time = System.nanoTime();
//			for (int i=0;i<numberofojcs;i++){
//				String name = ShimmerClock.ObjectClusterSensorName.TIMESTAMP_OFFSET + Integer.toString(i);
//				ojc.addDataToMap(name,CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,1.0);
//			}
//			difference = (System.nanoTime()-time)/1000;
//			avgdifference=avgdifference+difference;
//		}
//		avgdifference=avgdifference/10;
//		System.out.println(avgdifference);
//		
//		//JOS: TESTING RETRIEVAL TIME BELOW ------------------------------------------------------
//		//INIT OBJECTCLUSTER
//		ObjectCluster objectCluster = new ObjectCluster();
//		int lastValue = 9999;
//		
//		for (int j=1;j<tries;j++){
//			String name = ShimmerClock.ObjectClusterSensorName.TIMESTAMP_OFFSET + Integer.toString(j);
//			objectCluster.mSensorDataList.add(new SensorData(name, CHANNEL_TYPE.UNCAL.toString(), CHANNEL_UNITS.NO_UNITS, 1.0, false));
//		}		
//		
//		for (int j=1;j<tries;j++){
//			String name = ShimmerClock.ObjectClusterSensorName.TIMESTAMP_OFFSET + Integer.toString(j);
//			objectCluster.addDataToMap(name, CHANNEL_TYPE.UNCAL.toString(), CHANNEL_UNITS.NO_UNITS, 2.0);
//		}		
//		
//		//TEST 1 - Testing ArrayList retrieval time by iterating through list
//		long cumulativeListRetrievalTime = 0;
//		long cumulativeMultimapRetrievalTime = 0;
//		
//		
//		for(int a = 0; a<20; a++) {
//			
//			long worstCaseListRetrievalTime = 0;
//			long worstCaseMultimapRetrievalTime = 0;
//			String retrievalKey = ShimmerClock.ObjectClusterSensorName.TIMESTAMP_OFFSET + Integer.toString(lastValue);
//			
//			long startListRetrievalTime = System.nanoTime();
//			for(SensorData sensorData : objectCluster.mSensorDataList) {
//				if(sensorData.mSensorName.contains(retrievalKey)) {
//					double listData = sensorData.mSensorData;
//	//				System.err.println("SensorData data: " + listData);
//					worstCaseListRetrievalTime = System.nanoTime();
//				}
//			}
//			
//			cumulativeListRetrievalTime = cumulativeListRetrievalTime + (worstCaseListRetrievalTime - startListRetrievalTime);
//			
//			long startMultimapRetrievalTime = System.nanoTime();
//			Collection<FormatCluster> allFormats = objectCluster.getCollectionOfFormatClusters(retrievalKey);
//	        FormatCluster formatCluster = ((FormatCluster)ObjectCluster.returnFormatCluster(allFormats, CHANNEL_TYPE.UNCAL.toString()));
//	        double multimapData = formatCluster.mData;
//	//		System.err.println("Multimap Data: " + multimapData);
//			worstCaseMultimapRetrievalTime = System.nanoTime();
//			
//			cumulativeMultimapRetrievalTime = cumulativeMultimapRetrievalTime + (worstCaseMultimapRetrievalTime - startMultimapRetrievalTime);
//			
//			System.err.println("Time to retrieve ArrayList: " + (worstCaseListRetrievalTime - startListRetrievalTime) + " Time to retrieve Multimap: " + (worstCaseMultimapRetrievalTime - startMultimapRetrievalTime)); 
//		}
//				
//		System.err.println("Averages over 20x, ArrayList Time: " + (cumulativeListRetrievalTime/20) + " Multimap Time: " + (cumulativeMultimapRetrievalTime/20));
//		
//		storeIndexTest(objectCluster);
//		hashMapStoreTest();
//	}
	
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
	
	
	
	/*				objc.dataStructureSelector = 1;	//ArrayList
	objc.addDataToMap(String.valueOf(i), CHANNEL_TYPE.CAL.toString(), CHANNEL_UNITS.NO_UNITS, 1.0);
	objc.dataStructureSelector = 2;	//Nested HashMap
	objc.addDataToMap(String.valueOf(i), CHANNEL_TYPE.CAL.toString(), CHANNEL_UNITS.NO_UNITS, 2.0);
	objc.dataStructureSelector = 3;	//HashMap with Array
	objc.addDataToMap(String.valueOf(i), CHANNEL_TYPE.CAL.toString(), CHANNEL_UNITS.NO_UNITS, 3.0);
	objc.dataStructureSelector = 4;	//Arrays
	objc.addDataToMap(String.valueOf(i), CHANNEL_TYPE.CAL.toString(), CHANNEL_UNITS.NO_UNITS, 4.0);
	objc.dataStructureSelector = 5;	//Multimap
	objc.addDataToMap(String.valueOf(i), CHANNEL_TYPE.CAL.toString(), CHANNEL_UNITS.NO_UNITS, 5.0);
*/				


	public static long[] allInsertionTimes = new long[7];
	public static long[] allRandomRetrievalTimes = new long[7];
	public static long[] allStructuredRetrievalTimes = new long[7];
	
	public static void main(String[] args) throws InterruptedException {
//		ObjectCluster[] objcArray = testDataStructureOptionsInsertion();
//		System.err.println("----------------------------------------------\n");
//		testDataStructureOptionsRetrievalOrdered(objcArray);
//		System.err.println("----------------------------------------------\n");
//		testDataStructureOptionsRetrievalRandom(objcArray);
		
		//JOS: COMMENT THIS BACK IN IF YOU WANT TO RUN THE TESTS ONCE ONLY
//		System.err.println("----------------------------------------------\n");
//		ObjectCluster[][] objcArray = testDataStructureOptionsInsertionUpdated(true);
//		System.err.println("----------------------------------------------\n");
//		testDataStructureOptionsOrderedRetrievalUpdated(objcArray, true);
//		System.err.println("----------------------------------------------\n");
//		testDataStructureOptionsRandomRetrievalUpdated(objcArray, true);
		
		
		//Run the tests 1,000 times
		for(int i=0; i<1000; i++) {
			ObjectCluster[][] objcArray = testDataStructureOptionsInsertionUpdated(false);
			testDataStructureOptionsOrderedRetrievalUpdated(objcArray, false);
			testDataStructureOptionsRandomRetrievalUpdated(objcArray, false);
		}
		
		System.err.println("---------------------------------------------------------------------------------\n");
		System.err.println("-----------------------AVERAGES OVER 1,000 RUNS PER TEST-----------------------\n");
		System.err.println("-----------------------30 OBJECTCLUSTERS PER TEST WITH 30 CHANNELS OF DATA EACH\n");
		System.err.println("----------------------- TIMINGS: -----------------------\n");
		
		Thread.sleep(50);
		
		
		//First, divide all timings by 1,000 runs
		for(int i=0; i<allInsertionTimes.length; i++) {
			allInsertionTimes[i] = allInsertionTimes[i]/1000;
			allStructuredRetrievalTimes[i] = allStructuredRetrievalTimes[i]/1000;
			allRandomRetrievalTimes[i] = allRandomRetrievalTimes[i]/1000;
		}
	
		
		for(int i=0; i<allInsertionTimes.length; i++) {
			System.out.println("Data Structure " + (i+1) + " average insertion time: " + allInsertionTimes[i] + "ns" + "\t\t (" + DATA_STRUCTURE_NAMES[i] + ")");
		}
		Thread.sleep(50);
		System.err.println("----------------------------------------------\n");

		for(int i=0; i<allStructuredRetrievalTimes.length; i++) {
			System.out.println("Data Structure " + (i+1) + " average structured retrieval time: " + allStructuredRetrievalTimes[i] + "ns" + "\t\t (" + DATA_STRUCTURE_NAMES[i] + ")");
		}
		Thread.sleep(50);
		System.err.println("----------------------------------------------\n");

		for(int i=0; i<allRandomRetrievalTimes.length; i++) {
			System.out.println("Data Structure " + (i+1) + " average random retrieval time: " + allRandomRetrievalTimes[i] + "ns" + "\t\t (" + DATA_STRUCTURE_NAMES[i] + ")");
		}
		
		Thread.sleep(50);
		
		System.err.println("\n----------------------- COMPARISON VS MULTIMAP BASELINE: -----------------------\n");
		for(int i=0; i<allInsertionTimes.length; i++) {
			if(i != 4) {
				double factor = 1/((allInsertionTimes[i]*1.0f) / allInsertionTimes[4]);
				System.out.println("Data Structure " + (i+1) + " insertion average improvement: " + factor + "x" + "\t\t (" + DATA_STRUCTURE_NAMES[i] + ")");
			} else {
				System.out.println("Data Structure " + (i+1) + " insertion average improvement: " + "1.00000000000000x" + "\t\t (" + DATA_STRUCTURE_NAMES[i] + ")");
			}
		}
		System.err.println("----------------------------------------------\n");

		for(int i=0; i<allStructuredRetrievalTimes.length; i++) {
			if(i != 4) {
				double factor = 1/((allStructuredRetrievalTimes[i]*1.0f) / allStructuredRetrievalTimes[4]);
				System.out.println("Data Structure " + (i+1) + " sequential retrieval average improvement: " + factor + "x" + "\t\t (" + DATA_STRUCTURE_NAMES[i] + ")");
			} else {
				System.out.println("Data Structure " + (i+1) + " sequential retrieval average improvement: " + "1.00000000000000x" + "\t\t (" + DATA_STRUCTURE_NAMES[i] + ")");
			}
		}
		System.err.println("----------------------------------------------\n");

		for(int i=0; i<allRandomRetrievalTimes.length; i++) {
			if(i != 4) {
				double factor = 1/((allRandomRetrievalTimes[i]*1.0f) / allRandomRetrievalTimes[4]);
				System.out.println("Data Structure " + (i+1) + " random retrieval average improvement: " + factor + "x" + "\t\t (" + DATA_STRUCTURE_NAMES[i] + ")");
			} else {
				System.out.println("Data Structure " + (i+1) + " random retrieval average improvement: " + "1.00000000000000x" + "\t\t (" + DATA_STRUCTURE_NAMES[i] + ")");
			}
		}
	}
	
	
	
	public static ObjectCluster[] testDataStructureOptionsInsertion() {
		// ----------	PUT DATA INTO OBJECTCLUSTER AND TEST INSERTION SPEED ---------- //
		long timings[] = new long[5];
		long cumulativeTimings[] = new long[6];
		ObjectCluster[] objcArray = new ObjectCluster[30];
		
		for(int i=0; i<objcArray.length; i++) {
			ObjectCluster objc = new ObjectCluster();
			objc.setShimmerName("Test" + String.valueOf(i));
			objc.setMacAddress(String.valueOf(i));
			
			//Put 20 data into all the different data structures in each ObjectCluster
			for(int numDataStructures = 1; numDataStructures < 6; numDataStructures++) {
				objc.dataStructureSelector = numDataStructures;
				
				long startInsertion = System.nanoTime();
				for(int channels = 0; channels<20; channels++) {
					objc.addDataToMap(String.valueOf(channels), CHANNEL_TYPE.CAL.toString(), CHANNEL_UNITS.NO_UNITS, channels);
				}
				long endInsertion = System.nanoTime();
				
				cumulativeTimings[numDataStructures] = cumulativeTimings[numDataStructures] + (endInsertion - startInsertion);
				System.out.println("Data Structure " + numDataStructures + " insertion time: " + (endInsertion - startInsertion));
			}
			
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			System.err.println("----------------------- end test " + i + " -----------------------\n");
			objcArray[i] = objc; 
		}
		
		System.err.println("-----------------------Cumulative average over 30 ObjectClusters-----------------------\n");
		for(int a=1; a<cumulativeTimings.length; a++) {
			System.out.println("Option " + a + " cumulative avg insertion time: " + (cumulativeTimings[a]/30));
		}
		System.err.println("-----------------------End Insertion Test-----------------------\n");
		return objcArray;

	}
	
	
	public static void testDataStructureOptionsRetrievalOrdered(ObjectCluster[] objcArray) {
		long cumulativeTimings[] = new long[6];
		double[] dataArray = new double[20];	
		int testsCount = 1;
		
		//Run the test for each ObjectCluster
		for(ObjectCluster objc : objcArray) {
			//Run the test for each respective data structure
			for(int numDataStructures = 1; numDataStructures < 6; numDataStructures++) {
				
				if(numDataStructures == 1) {	//ArrayList
					
					long startArrayList = System.nanoTime();
					int count = 0;
					for(SensorData sd : objc.mSensorDataList) {
						dataArray[count] = sd.mSensorData;
						count++;
					}
					long endArrayList = System.nanoTime();
//					System.out.println("ArrayList ordered retrieval time: " + (endArrayList - startArrayList));
					System.out.println("Option 1 ordered retrieval time: " + (endArrayList - startArrayList));
					cumulativeTimings[numDataStructures] = cumulativeTimings[numDataStructures] + (endArrayList - startArrayList);
					
				} else if(numDataStructures == 2) {	//Nested HashMap
					
					long startNestedHashMap = System.nanoTime();
					int count = 0;
					for(HashMap<String, FormatCluster> formatMap : objc.mHashMap.values()) {
						for(FormatCluster fc : formatMap.values()) {
							dataArray[count] = fc.mData;
						}
						count++;
					}
					long endNestedHashMap = System.nanoTime();
					
//					System.out.println("Nested HashMap ordered retrieval time: " + (endNestedHashMap - startNestedHashMap));
					System.out.println("Option 2 ordered retrieval time: " + (endNestedHashMap - startNestedHashMap));
					cumulativeTimings[numDataStructures] = cumulativeTimings[numDataStructures] + (endNestedHashMap - startNestedHashMap);

				} else if(numDataStructures == 3) {	//HashMap with Array
					
					long startHashMapArray = System.nanoTime();
					int count = 0;
					for(FormatCluster[] fc : objc.mHashMapArray.values()) {
						dataArray[count] = fc[0].mData;
						count++;
					}
					long endHashMapArray = System.nanoTime();
					
//					System.out.println("HashMap Array ordered retrieval time: " + (endHashMapArray - startHashMapArray));
					System.out.println("Option 3 ordered retrieval time: " + (endHashMapArray - startHashMapArray));
					cumulativeTimings[numDataStructures] = cumulativeTimings[numDataStructures] + (endHashMapArray - startHashMapArray);
					
				} else if(numDataStructures == 4) {	//Arrays
					
					long startArrays = System.nanoTime();
					int count = 0;
					for(SensorData sd : objc.mSensorDataArray) {
						if(sd != null) {
							dataArray[count] = sd.mSensorData;
							count++;
						}
					}
					long endArrays = System.nanoTime();

//					System.out.println("Arrays ordered retrieval time: " + (endArrays - startArrays));
					System.out.println("Option 4 ordered retrieval time: " + (endArrays - startArrays));
					cumulativeTimings[numDataStructures] = cumulativeTimings[numDataStructures] + (endArrays - startArrays);

				} else if(numDataStructures == 5) {	//MultiMap

					long startMultimap = System.nanoTime();
					int count = 0;
					for(int channel=0; channel<20; channel++) {
	                    Collection<FormatCluster> allFormats = objc.getCollectionOfFormatClusters(String.valueOf(channel));
	                    FormatCluster fc = ((FormatCluster)ObjectCluster.returnFormatCluster(allFormats,"CAL"));
	                    dataArray[count] = fc.mData;
	                    count++;
					}
					long endMultimap = System.nanoTime();
					
//					System.out.println("Multimap ordered retrieval time: " + (endMultimap - startMultimap));
					System.out.println("Option 5 ordered retrieval time: " + (endMultimap - startMultimap));
					cumulativeTimings[numDataStructures] = cumulativeTimings[numDataStructures] + (endMultimap - startMultimap);
					
				}
			}
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.err.println("----------------------- end test " + testsCount + " -----------------------\n");
			testsCount++;
		}
		
		System.err.println("-----------------------Cumulative average over 30 ObjectClusters-----------------------\n");
		for(int a=1; a<cumulativeTimings.length; a++) {
			System.out.println("Option " + a + " cumulative avg retrieval time: " + (cumulativeTimings[a]/30));
		}

	}
	
	
	public static void testDataStructureOptionsRetrievalRandom(ObjectCluster[] objcArray) {
		
		long cumulativeTimings[] = new long[6];
		double[] dataArray = new double[50];
		int testsCount = 0;
		
		//Run the test for each ObjectCluster
		for(ObjectCluster objc : objcArray) {
			//Run the test for each respective data structure
			for(int numDataStructures = 1; numDataStructures < 6; numDataStructures++) {
				
				if(numDataStructures == 1) {	//ArrayList
					
					long startArrayList = System.nanoTime();
					for(int j=0; j<50; j++) {
						int randomNum = ThreadLocalRandom.current().nextInt(0, 20);
						dataArray[j] = objc.mSensorDataList.get(randomNum).mSensorData;
					}
					long endArrayList = System.nanoTime();
//					System.out.println("ArrayList ordered retrieval time: " + (endArrayList - startArrayList));
					System.out.println("Option 1 random retrieval time: " + (endArrayList - startArrayList));
					cumulativeTimings[numDataStructures] = cumulativeTimings[numDataStructures] + (endArrayList - startArrayList);
					
				} else if(numDataStructures == 2) {	//Nested HashMap
					
					long startNestedHashMap = System.nanoTime();
					for(int j = 0; j<50; j++) {
						int randomNum = ThreadLocalRandom.current().nextInt(0, 20);
						dataArray[j] = objc.mHashMap.get(String.valueOf(randomNum)).get(CHANNEL_TYPE.CAL.toString()).mData;
					}
					long endNestedHashMap = System.nanoTime();
					
//					System.out.println("Nested HashMap ordered retrieval time: " + (endNestedHashMap - startNestedHashMap));
					System.out.println("Option 2 random retrieval time: " + (endNestedHashMap - startNestedHashMap));
					cumulativeTimings[numDataStructures] = cumulativeTimings[numDataStructures] + (endNestedHashMap - startNestedHashMap);

				} else if(numDataStructures == 3) {	//HashMap with Array
					
					long startHashMapArray = System.nanoTime();
					for(int j=0; j<50; j++) {
						int randomNum = ThreadLocalRandom.current().nextInt(0, 20);
						FormatCluster[] fc = objc.mHashMapArray.get(String.valueOf(randomNum));
						dataArray[j] = fc[0].mData;
					}
					long endHashMapArray = System.nanoTime();
					
//					System.out.println("HashMap Array ordered retrieval time: " + (endHashMapArray - startHashMapArray));
					System.out.println("Option 3 random retrieval time: " + (endHashMapArray - startHashMapArray));
					cumulativeTimings[numDataStructures] = cumulativeTimings[numDataStructures] + (endHashMapArray - startHashMapArray);
					
				} else if(numDataStructures == 4) {	//Arrays
					
					long startArrays = System.nanoTime();
					for(int j=0; j<50; j++) {
						int randomNum = ThreadLocalRandom.current().nextInt(0, 20);
						dataArray[j] = objc.mSensorDataArray[randomNum].mSensorData;
					}
					long endArrays = System.nanoTime();

//					System.out.println("Arrays ordered retrieval time: " + (endArrays - startArrays));
					System.out.println("Option 4 random retrieval time: " + (endArrays - startArrays));
					cumulativeTimings[numDataStructures] = cumulativeTimings[numDataStructures] + (endArrays - startArrays);

				} else if(numDataStructures == 5) {	//MultiMap

					long startMultimap = System.nanoTime();
					for(int j=0; j<50; j++) {
						int randomNum = ThreadLocalRandom.current().nextInt(0, 20);
	                    Collection<FormatCluster> allFormats = objc.getCollectionOfFormatClusters(String.valueOf(randomNum));
	                    FormatCluster fc = ((FormatCluster)ObjectCluster.returnFormatCluster(allFormats,"CAL"));
	                    dataArray[j] = fc.mData;
					}
					long endMultimap = System.nanoTime();
					
//					System.out.println("Multimap ordered retrieval time: " + (endMultimap - startMultimap));
					System.out.println("Option 5 random retrieval time: " + (endMultimap - startMultimap));
					cumulativeTimings[numDataStructures] = cumulativeTimings[numDataStructures] + (endMultimap - startMultimap);
					
				}
			}
			
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.err.println("----------------------- end test " + testsCount + " -----------------------\n");
			testsCount++;
		}
		
		System.err.println("-----------------------Cumulative average over 30 ObjectClusters-----------------------\n");
		for(int a=1; a<cumulativeTimings.length; a++) {
			System.out.println("Option " + a + " cumulative avg random retrieval time: " + (cumulativeTimings[a]/30));
		}
	}
	
	
	public static final int NUM_ARRAY_LIST = 0;
	public static final int NUM_NESTED_HASHMAP = 1;
	public static final int NUM_HASHMAP_ARRAY = 2;
	public static final int NUM_SENSORDATA_ARRAY = 3;
	public static final int NUM_MULTIMAP = 4;
	public static final int NUM_ARRAYS = 5;
	
	public static final int NUM_OBJECTCLUSTERS = 30;
	public static final int NUM_DATA_STRUCTURES = 7;
	public static String[] DATA_STRUCTURE_NAMES = {"ArrayList & SensorData", "Nested HashMap", "HashMap & Array", "Arrays & SensorData", "MultiMap", "Arrays", "Arrays with Resizing"};
	
	/**
	 * Now updated to test 30 channels, and with separate ObjectClusters for each data structure option
	 * Also testing array resizing time
	 * 
	 */
	public static ObjectCluster[][] testDataStructureOptionsInsertionUpdated(boolean printToConsole) {
		
		long cumulativeTimings[] = new long[NUM_DATA_STRUCTURES];
		long cumulativeTimingsOverall[] = new long[NUM_DATA_STRUCTURES];
		ObjectCluster[][] objcArray = new ObjectCluster[NUM_DATA_STRUCTURES][NUM_OBJECTCLUSTERS];
		
		for(int i=0; i<objcArray.length; i++) {
			//Iterate through each of the data structures
			
			long startInsertion = 0;
			long endInsertion = 0;
			long difference = 0;
			
			if(printToConsole) {
				System.err.println("\n----------------------- Data Structure " + i + " -----------------------\n");
			}

			for(int j=0; j<NUM_OBJECTCLUSTERS; j++) {
				//Put data into each of the ObjectClusters in each row (data structure) of the 2D Array

				ObjectCluster objc = new ObjectCluster();
				objc.setShimmerName("DataStructure" + String.valueOf(i));
				objc.setMacAddress(String.valueOf(i));

				for(int numOfChannels=0; numOfChannels<30; numOfChannels++) {
					//Put 30 channels of data into each ObjectCluster
					if(i == 0) {	
						//ArrayList of SensorData
						startInsertion = System.nanoTime();
						objc.addDataToArrayList(String.valueOf(numOfChannels), CHANNEL_TYPE.CAL.toString(), CHANNEL_UNITS.NO_UNITS, numOfChannels);				
						endInsertion = System.nanoTime();
					} else if(i == 1) {	
						//Nested HashMap
						startInsertion = System.nanoTime();
						objc.addDataToNestedHashMap(String.valueOf(numOfChannels), CHANNEL_TYPE.CAL.toString(), CHANNEL_UNITS.NO_UNITS, numOfChannels);
						endInsertion = System.nanoTime();
					} else if(i == 2) {
						//HashMap with Array of FormatClusters
						startInsertion = System.nanoTime();
						objc.addDataToHashMapArray(String.valueOf(numOfChannels), CHANNEL_TYPE.CAL.toString(), CHANNEL_UNITS.NO_UNITS, numOfChannels);
						endInsertion = System.nanoTime();
					} else if(i == 3) {
						//Array of SensorData
						startInsertion = System.nanoTime();
						objc.addDataToSensorDataArray(String.valueOf(numOfChannels), CHANNEL_TYPE.CAL.toString(), CHANNEL_UNITS.NO_UNITS, numOfChannels);
						endInsertion = System.nanoTime();
					} else if(i == 4) {
						//Multimap
						startInsertion = System.nanoTime();
						objc.addDataToMap(String.valueOf(numOfChannels), CHANNEL_TYPE.CAL.toString(), CHANNEL_UNITS.NO_UNITS, numOfChannels);
						endInsertion = System.nanoTime();
					} else if(i == 5) {
						//Arrays
						startInsertion = System.nanoTime();
						objc.addDataToArrays(String.valueOf(numOfChannels), CHANNEL_TYPE.CAL.toString(), CHANNEL_UNITS.NO_UNITS, numOfChannels);
						endInsertion = System.nanoTime();
					} else if(i == 6) {
						startInsertion = System.nanoTime();
						testArrayResizing(objc);
						objc.addDataToArraysWithResize(String.valueOf(numOfChannels), CHANNEL_TYPE.CAL.toString(), CHANNEL_UNITS.NO_UNITS, numOfChannels);
						endInsertion = System.nanoTime();
					}
					
					difference = endInsertion - startInsertion;
					cumulativeTimings[i] = cumulativeTimings[i] + difference;
				}
				
				objcArray[i][j] = objc;
				if(printToConsole) {
				System.out.println("Data Structure Type " + i + " avg insertion time of 30 channels: " + cumulativeTimings[i]/30 + "ns");
				}
				cumulativeTimingsOverall[i] = cumulativeTimingsOverall[i] + cumulativeTimings[i];
				cumulativeTimings[i] = 0;	//Reset back to 0
			}			
			
			if(printToConsole) {
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}
		
		for(int a=0; a<cumulativeTimingsOverall.length; a++) {
			//Divide by 30 because 30 ObjectClusters
			cumulativeTimingsOverall[a] = (cumulativeTimingsOverall[a]/30);
			allInsertionTimes[a] = allInsertionTimes[a] + cumulativeTimingsOverall[a];
		}
		
		if(printToConsole) {
			System.err.println("----------------------- Cumulative average over 30 ObjectClusters, 30 channels each -----------------------\n");
			
			for(int a=0; a<cumulativeTimingsOverall.length; a++) {
				System.out.println("Option " + a + " cumulative avg insertion time: " + cumulativeTimingsOverall[a] + "ns");
			}
			System.err.println("----------------------- Comparison of averages over 30 ObjectClusters against Multimap baseline -----------------------\n");
			long baseline = cumulativeTimingsOverall[4];
			for(int a=0; a<cumulativeTimingsOverall.length; a++) {
				if(a != 4) {	//4 is the multimap, the baseline
					double improvement = 1/((cumulativeTimingsOverall[a]*1.0f) / baseline);
					System.err.println("Improvement of data structure " + a + " vs Multimap baseline: " + improvement + "x" + "\t\t" + DATA_STRUCTURE_NAMES[a]);
				} else {
					System.err.println("Improvement of data structure " + a + " vs Multimap baseline: 1.00000000000000x" + "\t\t\t" + DATA_STRUCTURE_NAMES[4]);
				}
			}
	
			System.err.println("-----------------------End Insertion Test-----------------------\n");
		}
		return objcArray;

	}
	
	
	public static void testArrayResizing(ObjectCluster obj) {
		
		if(obj.mCalDataResize == null) {	//if arrays not inialized, then initialize them
			obj.mCalDataResize = new double[1];
			obj.mSensorNamesCalResize = new String[1];
			obj.mUnitCalResize = new String[1];
		}
		
		int currentLength = obj.mCalDataResize.length;
		if(obj.calArrayIndexResize == (currentLength-1)) {	//If the index is already at the end of the array, resize the array to make it bigger
			String[] tempNamesArray = new String[currentLength+5];
			String[] tempUnitArray = new String[currentLength+5];
			double[] tempDataArray = new double[currentLength+5];

			//3 cal arrays to resize
			System.arraycopy(obj.mCalDataResize, 0, tempDataArray, 0, currentLength);
			System.arraycopy(obj.mUnitCalResize, 0, tempUnitArray, 0, currentLength);
			System.arraycopy(obj.mSensorNamesCalResize, 0, tempNamesArray, 0, currentLength);
			
			//Copy the temp arrays into the ObjectCluster
			obj.mSensorNamesCalResize = tempNamesArray;
			obj.mCalDataResize = tempDataArray;
			obj.mUnitCalResize = tempUnitArray;
		}
	}
	
	
	public static void testDataStructureOptionsOrderedRetrievalUpdated(ObjectCluster[][] objcArray, boolean printToConsole) {
		long cumulativeTimings[] = new long[NUM_DATA_STRUCTURES];
		long cumulativeTimingsOverall[] = new long[NUM_DATA_STRUCTURES];
		double[] dataArray = new double[50];	//purpose of this array is to store the data retrieved from the ObjectClusters
		
		for(int i=0; i<objcArray.length; i++) {
			//Iterate through each of the data structures
			
			long startRetrieval = 0;
			long endRetrieval = 0;
			long difference = 0;
			
			if(printToConsole) {
				System.err.println("\n----------------------- Data Structure " + i + " -----------------------\n");
			}

			for(int j=0; j<NUM_OBJECTCLUSTERS; j++) {
				ObjectCluster objc = objcArray[i][j];
				//Retrieve data from each of the ObjectClusters in each row (data structure) of the 2D Array
					if(i == 0) {	
						//ArrayList of SensorData
						startRetrieval = System.nanoTime();
						int count = 0;
						for(SensorData sd : objc.mSensorDataList) {
							dataArray[count] = sd.mSensorData;
							count++;
						}			
						endRetrieval = System.nanoTime();
					} else if(i == 1) {	
						//Nested HashMap
						startRetrieval = System.nanoTime();
						int count = 0;
						for(HashMap<String, FormatCluster> formatMap : objc.mHashMap.values()) {
							for(FormatCluster fc : formatMap.values()) {
								dataArray[count] = fc.mData;
							}
							count++;
						}
						endRetrieval = System.nanoTime();
					} else if(i == 2) {
						//HashMap with Array of FormatClusters
						startRetrieval = System.nanoTime();
						int count = 0;
						for(FormatCluster[] fc : objc.mHashMapArray.values()) {
							dataArray[count] = fc[0].mData;
							count++;
						}
						endRetrieval = System.nanoTime();
					} else if(i == 3) {
						//Array of SensorData
						startRetrieval = System.nanoTime();
						int count = 0;
						for(SensorData sd : objc.mSensorDataArray) {
							if(sd != null) {
								dataArray[count] = sd.mSensorData;
								count++;
							}
						}
						endRetrieval = System.nanoTime();
					} else if(i == 4) {
						//Multimap
						startRetrieval = System.nanoTime();
						int count = 0;
						for(int channel=0; channel<30; channel++) {
		                    Collection<FormatCluster> allFormats = objc.getCollectionOfFormatClusters(String.valueOf(channel));
		                    FormatCluster fc = ((FormatCluster)ObjectCluster.returnFormatCluster(allFormats,"CAL"));
		                    dataArray[count] = fc.mData;
		                    count++;
						}
						endRetrieval = System.nanoTime();
					} else if(i == 5) {
						//Arrays
						startRetrieval = System.nanoTime();
						int count = 0;
//						for(Double data : objc.mCalData) {
						for(int numOfData = 0; numOfData < objc.calArrayIndex; numOfData++) {	//Testing by reading only up to index
							double data = objc.mCalData[numOfData];
							dataArray[count] = data;
							//Testing reading from all arrays
							String channelName = objc.mSensorNamesCal[numOfData];
							String unit = objc.mUnitCal[numOfData];
							count++;
						}
						endRetrieval = System.nanoTime();
					}else if(i == 6) {
						startRetrieval = System.nanoTime();
						int count = 0;
						for(Double data : objc.mCalDataResize) {
							if(data != null) {
								dataArray[count] = data;
								count++;
							}
						}
						endRetrieval = System.nanoTime();
					}
					
					difference = endRetrieval - startRetrieval;
					cumulativeTimings[i] = cumulativeTimings[i] + difference;
				
					if(printToConsole) {
						System.out.println("Data Structure Type " + i + " avg insertion time of 30 channels: " + cumulativeTimings[i]/30 + "ns");
					}
				cumulativeTimingsOverall[i] = cumulativeTimingsOverall[i] + cumulativeTimings[i];
				cumulativeTimings[i] = 0;	//Reset back to 0
			}			
			
			if(printToConsole) {
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		for(int a=0; a<cumulativeTimingsOverall.length; a++) {
			//Divide by 30 because 30 ObjectClusters
			cumulativeTimingsOverall[a] = (cumulativeTimingsOverall[a]/30);
			allStructuredRetrievalTimes[a] = allStructuredRetrievalTimes[a] + cumulativeTimingsOverall[a];
		}
		
		if(printToConsole) {
			System.err.println("----------------------- Cumulative average over 30 ObjectClusters, 30 channels each -----------------------\n");
			
			for(int a=0; a<cumulativeTimingsOverall.length; a++) {
				System.out.println("Option " + a + " cumulative avg structured retrieval time: " + cumulativeTimingsOverall[a] + "ns");
			}
			System.err.println("----------------------- Comparison of averages over 30 ObjectClusters against Multimap baseline -----------------------\n");
			long baseline = cumulativeTimingsOverall[4];
			for(int a=0; a<cumulativeTimingsOverall.length; a++) {
				if(a != 4) {	//4 is the multimap, the baseline
					double improvement = 1/((cumulativeTimingsOverall[a]*1.0f) / baseline);
					System.err.println("Improvement of data structure " + a + " vs Multimap baseline: " + improvement + "x" + "\t\t" + DATA_STRUCTURE_NAMES[a]);
				} else {
					System.err.println("Improvement of data structure " + a + " vs Multimap baseline: 1.00000000000000x" + "\t\t\t" + DATA_STRUCTURE_NAMES[4]);
				}
			}
	
			System.err.println("-----------------------End Structured Retrieval Test-----------------------\n");
		}

	}
	
	
	public static void testDataStructureOptionsRandomRetrievalUpdated(ObjectCluster[][] objcArray, boolean printToConsole) {
		long cumulativeTimings[] = new long[NUM_DATA_STRUCTURES];
		long cumulativeTimingsOverall[] = new long[NUM_DATA_STRUCTURES];
		double[] dataArray = new double[60];
		
		for(int i=0; i<objcArray.length; i++) {
			//Iterate through each of the data structures
			
			long startRetrieval = 0;
			long endRetrieval = 0;
			long difference = 0;
			
			if(printToConsole) {
				System.err.println("\n----------------------- Data Structure " + i + " -----------------------\n");
			}

			for(int j=0; j<NUM_OBJECTCLUSTERS; j++) {
				ObjectCluster objc = objcArray[i][j];
				//Retrieve data from each of the ObjectClusters in each row (data structure) of the 2D Array
					if(i == 0) {	
						//ArrayList of SensorData
						startRetrieval = System.nanoTime();
						for(int k=0; k<60; k++) {
							int randomNum = ThreadLocalRandom.current().nextInt(0, 30);
							dataArray[k] = objc.mSensorDataList.get(randomNum).mSensorData;
						}
						endRetrieval = System.nanoTime();
					} else if(i == 1) {	
						//Nested HashMap
						startRetrieval = System.nanoTime();
						for(int k=0; k<60; k++) {
							int randomNum = ThreadLocalRandom.current().nextInt(0, 30);
							dataArray[k] = objc.mHashMap.get(String.valueOf(randomNum)).get(CHANNEL_TYPE.CAL.toString()).mData;
						}
						endRetrieval = System.nanoTime();
					} else if(i == 2) {
						//HashMap with Array of FormatClusters
						startRetrieval = System.nanoTime();
						for(int k=0; k<60; k++) {
							int randomNum = ThreadLocalRandom.current().nextInt(0, 30);
							FormatCluster[] fc = objc.mHashMapArray.get(String.valueOf(randomNum));
							dataArray[k] = fc[0].mData;
						}
						endRetrieval = System.nanoTime();
					} else if(i == 3) {
						//Array of SensorData
						startRetrieval = System.nanoTime();
						for(int k=0; k<60; k++) {
							int randomNum = ThreadLocalRandom.current().nextInt(0, 30);
							dataArray[k] = objc.mSensorDataArray[randomNum].mSensorData;
						}
						endRetrieval = System.nanoTime();
					} else if(i == 4) {
						//Multimap
						startRetrieval = System.nanoTime();
						for(int k=0; k<60; k++) {
							int randomNum = ThreadLocalRandom.current().nextInt(0, 30);
		                    Collection<FormatCluster> allFormats = objc.getCollectionOfFormatClusters(String.valueOf(randomNum));
		                    FormatCluster fc = ((FormatCluster)ObjectCluster.returnFormatCluster(allFormats,"CAL"));
		                    dataArray[k] = fc.mData;
						}
						endRetrieval = System.nanoTime();
					} else if(i == 5) {
						//Arrays
						String channelName = "";	//Testing retrieving data from all the arrays
						String units = "";
						startRetrieval = System.nanoTime();
						for(int k=0; k<60; k++) {
							int randomNum = ThreadLocalRandom.current().nextInt(0, 30);
							dataArray[k] = objc.mCalData[randomNum];
						}
						endRetrieval = System.nanoTime();
					}else if(i == 6) {
						//Arrays with Resize
						startRetrieval = System.nanoTime();
						for(int k=0; k<60; k++) {
							int randomNum = ThreadLocalRandom.current().nextInt(0, 30);
							dataArray[k] = objc.mCalDataResize[randomNum];
						}
						endRetrieval = System.nanoTime();
					}
					
					difference = endRetrieval - startRetrieval;
					cumulativeTimings[i] = cumulativeTimings[i] + difference;
				
					if(printToConsole) {
						System.out.println("Data Structure Type " + i + " avg random retrieval time of 30 channels retrieved 60 times: " + cumulativeTimings[i]/30 + "ns");
					}
				cumulativeTimingsOverall[i] = cumulativeTimingsOverall[i] + cumulativeTimings[i];
				cumulativeTimings[i] = 0;	//Reset back to 0
			}			
			
			if(printToConsole) {
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}
		
		for(int a=0; a<cumulativeTimingsOverall.length; a++) {
			//Divide by 30 because 30 ObjectClusters
			cumulativeTimingsOverall[a] = (cumulativeTimingsOverall[a]/30);
			allRandomRetrievalTimes[a] = allRandomRetrievalTimes[a] + cumulativeTimingsOverall[a];
		}
		
		if(printToConsole) {
			System.err.println("----------------------- Cumulative average over 30 ObjectClusters, 30 channels each -----------------------\n");
			
			for(int a=0; a<cumulativeTimingsOverall.length; a++) {
				System.out.println("Option " + a + " cumulative avg random retrieval time: " + cumulativeTimingsOverall[a] + "ns");
			}
			System.err.println("----------------------- Comparison of averages over 30 ObjectClusters against Multimap baseline -----------------------\n");
			long baseline = cumulativeTimingsOverall[4];
			for(int a=0; a<cumulativeTimingsOverall.length; a++) {
				if(a != 4) {	//4 is the multimap, the baseline
					double improvement = 1/((cumulativeTimingsOverall[a]*1.0f) / baseline);
					System.err.println("Improvement of data structure " + a + " vs Multimap baseline: " + improvement + "x" + "\t\t" + DATA_STRUCTURE_NAMES[a]);
				} else {
					System.err.println("Improvement of data structure " + a + " vs Multimap baseline: 1.00000000000000x" + "\t\t\t" + DATA_STRUCTURE_NAMES[4]);
				}
			}
	
			System.err.println("-----------------------End Random Retrieval Test-----------------------\n");
		}
	}
	
	

	/**
	 * Test version of ObjectCluster, specific to TestObjectCluster
	 * Production version of ObjectCluster in ShimmerDriver has been changed
	 * This class may not work
	 * @author Jos
	 *
	 */
	final public static class ObjectCluster implements Cloneable,Serializable{
		
		private static final long serialVersionUID = -7601464501144773539L;
		
		// ----------------- JOS: Temp for testing START ----------------- //
		
		//JC: Only temporary for testing this class can be deleted if we decide not to use it in the future
		public ArrayList<SensorData> mSensorDataList = new ArrayList<SensorData>();
		public HashMap<String, HashMap<String, FormatCluster>> mHashMap = new HashMap<>();
		public HashMap<String, FormatCluster[]> mHashMapArray = new HashMap<>();
		public SensorData[] mSensorDataArray = new SensorData[50];
		public int mSensorDataArrayIndex = 0;
		public int dataStructureSelector = 1;	//1 = ArrayList, 2 = HashMap, 3 = HashMapArray, 4 = Arrays, 5 = Multimap
		
		// ----------------- JOS: Temp for testing END ----------------- //

		
		public Multimap<String, FormatCluster> mPropertyCluster = HashMultimap.create();
		//TODO implement below to remove the need for the Guava library?
//		private HashMap<String, HashMap<CHANNEL_TYPE, FormatCluster>> mPropertyClusterProposed = new HashMap<String, HashMap<CHANNEL_TYPE, FormatCluster>>();

		/**
		 * Some times it is necessary to get a list of Channels from the
		 * mPropertyCluster in order of insertion which is not possible with the
		 * Multimap approach. This separate list was created to keep a record
		 * of the order of insertion.
		 */
		private List<String> listOfChannelNames = new ArrayList<String>(); 

		private String mMyName;
		private String mBluetoothAddress;
		
		// ------- Old Array approach - Start -----------
		public byte[] mRawData;
		public double[] mUncalData = new double[50];
		public double[] mCalData = new double[50];
		/** The SensorNames array is an older approach and has been largely replaced
		 * by the mPropertyCluster. Suggest using getChannelNamesByInsertionOrder()
		 * or getChannelNamesFromKeySet() instead. */
		@Deprecated
		public String[] mSensorNames;
		public String[] mUnitCal = new String[50];
		public String[] mUnitUncal = new String[50];
		
		public String[] mSensorNamesCal = new String[50];	//JOS: would 2D arrays be better here?
		public String[] mSensorNamesUncal = new String[50];
		
		//JOS: TEST ARRAYS RESIZING HERE
		public String[] mSensorNamesCalResize;
		public String[] mUnitCalResize;
		public double[] mCalDataResize;
		public double[] mCalDataNew = new double[50];
		public double[] mUncalDataNew = new double[50];
		
		// ------- Old Array approach - End -----------
		
		/** mObjectClusterBuilder needs to be uninitialized to avoid crash when connecting on Android */
		private Builder mObjectClusterBuilder; 
		
		private int indexKeeper = 0;
		
		public byte[] mSystemTimeStamp = new byte[8];
		private double mTimeStampMilliSecs;
		public boolean mIsValidObjectCluster = true;
		
		public boolean useList = false;
		
		public int mPacketIdValue = 0;
		
		public enum OBJECTCLUSTER_TYPE{
			ARRAYS,
			FORMAT_CLUSTER,
			PROTOBUF
		}
		public List<OBJECTCLUSTER_TYPE> mListOfOCTypesEnabled = Arrays.asList(
				OBJECTCLUSTER_TYPE.ARRAYS,
				OBJECTCLUSTER_TYPE.FORMAT_CLUSTER,
				OBJECTCLUSTER_TYPE.PROTOBUF);

		//TODO remove this variable? unused in PC applications
		public BT_STATE mState;

		
		public ObjectCluster(){
		}
		
		public ObjectCluster(String myName){
			mMyName = myName;
		}

		public ObjectCluster(String myName, String myBlueAdd){
			this(myName);
			mBluetoothAddress=myBlueAdd;
		}

		//TODO remove this constructor? unused in PC applications
		public ObjectCluster(String myName, String myBlueAdd, BT_STATE state){
			this(myName, myBlueAdd);
			mState = state;
		}
		
		public ObjectCluster(ObjectCluster2 ojc2){
			ojc2.getDataMap().get("");
			for (String channelName:ojc2.getDataMap().keySet()){
				FormatCluster2 fc=ojc2.getDataMap().get(channelName);
				for (String formatName:fc.getFormatMap().keySet()){
					DataCluster2 data = fc.getFormatMap().get(formatName);
					addDataToMap(channelName,formatName,data.getUnit(),data.getData(),data.getDataArrayList());
				}
			}
			mBluetoothAddress = ojc2.getBluetoothAddress();
			mMyName = ojc2.getName();
		}
		
		public String getShimmerName(){
			return mMyName;
		}
		
		public void setShimmerName(String name){
			mMyName = name;
		}
		
		public String getMacAddress(){
			return mBluetoothAddress;
		}
		
		public void setMacAddress(String macAddress){
			mBluetoothAddress = macAddress;
		}
		
		/**
		 * Takes in a collection of Format Clusters and returns the Format Cluster specified by the string format
		 * @param collectionFormatCluster
		 * @param format 
		 * @return FormatCluster
		 */
		public static FormatCluster returnFormatCluster(Collection<FormatCluster> collectionFormatCluster, String format){
			FormatCluster returnFormatCluster = null;

			Iterator<FormatCluster> iFormatCluster = collectionFormatCluster.iterator();
			while(iFormatCluster.hasNext()){
				FormatCluster formatCluster = iFormatCluster.next();
				if (formatCluster.mFormat.equals(format)){
					returnFormatCluster = formatCluster;
				}
			}
			return returnFormatCluster;
		}

		public double getFormatClusterValueDefaultFormat(ChannelDetails channelDetails){
			return getFormatClusterValue(channelDetails.mObjectClusterName, channelDetails.mListOfChannelTypes.get(0).toString());
		}

		public double getFormatClusterValue(ChannelDetails channelDetails, CHANNEL_TYPE channelType){
			return getFormatClusterValue(channelDetails.mObjectClusterName, channelType.toString());
		}
		
//		public double getFormatClusterValue(ChannelDetails channelDetails, String format){
//			return getFormatClusterValue(channelDetails.mObjectClusterName, format);
//		}

		public double getFormatClusterValue(String channelName, String format){
			FormatCluster formatCluster = getLastFormatCluster(channelName, format);
			if(formatCluster!=null){
				return formatCluster.mData;
			}
			return Double.NaN;
		}
		
		public FormatCluster getLastFormatCluster(String channelName, String format){
			Collection<FormatCluster> formatClusterCollection = getCollectionOfFormatClusters(channelName);
			if(formatClusterCollection != null){
				FormatCluster formatCluster = ObjectCluster.returnFormatCluster(formatClusterCollection, format);
				if(formatCluster!=null){
					return formatCluster;
				}
			}
			return null;
		}

		/**
		 * Users should note that a property has to be removed before it is replaced
		 * @param propertyname Property name you want to delete
		 * @param formatname Format you want to delete
		 */
		public void removePropertyFormat(String propertyname, String formatname){
			Collection<FormatCluster> colFormats = mPropertyCluster.get(propertyname); 
			// first retrieve all the possible formats for the current sensor device
			FormatCluster formatCluster = ((FormatCluster)ObjectCluster.returnFormatCluster(colFormats,formatname)); // retrieve format;
			mPropertyCluster.remove(propertyname, formatCluster);
		}
		
		/**Serializes the object cluster into an array of bytes
		 * @return byte[] an array of bytes
		 * @see java.io.Serializable
		 */
		public byte[] serialize() {
			try {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ObjectOutputStream oos = new ObjectOutputStream(baos);
				oos.writeObject(this);
				return baos.toByteArray();
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}
		
		public List<String[]> generateArrayOfChannelsSorted(){
			List<String[]> listofSignals = new ArrayList<String[]>();
			int size=0;
			for (String fckey : getChannelNamesFromKeySet() ) {
				size++;
			}
			
			//arrange the properties
			String[] properties = new String[size];
			int y=0;
			for (String fckey : getChannelNamesFromKeySet() ) {
				properties[y]=fckey;
				y++;
			}
			
			Arrays.sort(properties);
			
			// now need to try arrange the formats
			int index=0;
			String property;
			for (int k=0;k<size;k++){
				property = properties[k];
				Collection<FormatCluster> ofFormatstemp = getCollectionOfFormatClusters(property);
				// the iterator does not have the same order
				int tempSize=0;
				for (FormatCluster fctemp:ofFormatstemp){
					tempSize++;
				}
				
				String[] formats = new String[tempSize];
				String[] units = new String[tempSize];
				int p=0;
				//sort the formats
				for (FormatCluster fctemp:ofFormatstemp){
					formats[p]=fctemp.mFormat;
					p++;
				
				}
				
				Arrays.sort(formats);
				for (int u=0;u<formats.length;u++){
					for (FormatCluster fctemp:ofFormatstemp){
						if (fctemp.mFormat.equals(formats[u])){
							units[u]=fctemp.mUnits;
						}
					}
				}
				
				for (int u=0;u<formats.length;u++){
					String[] channel = {mMyName,property,formats[u],units[u]};
					listofSignals.add(channel);
					//System.out.println(":::" + address + property + fc.mFormat);		
					System.out.println("Index" + index); 
					
				}
				
			
			}
			return listofSignals;
		}
		
		public List<String[]> generateArrayOfChannels(){
			//First retrieve all the unique keys from the objectClusterLog
			Multimap<String, FormatCluster> m = mPropertyCluster;

			int size = m.size();
			System.out.print(size);
			mSensorNames=new String[size];
			String[] sensorFormats=new String[size];
			String[] sensorUnits=new String[size];
			String[] sensorIsUsingDefaultCal=new String[size];
			int i=0;
			int p=0;
			for(String key : m.keys()) {
				//first check that there are no repeat entries

				if(compareStringArray(mSensorNames, key) == true) {
					for(FormatCluster formatCluster : m.get(key)) {
						sensorFormats[p]=formatCluster.mFormat;
						sensorUnits[p]=formatCluster.mUnits;
						sensorIsUsingDefaultCal[p]=(formatCluster.mIsUsingDefaultCalibration? "*":"");
						//Log.d("Shimmer",key + " " + mSensorFormats[p] + " " + mSensorUnits[p]);
						p++;
					}

				}	

				mSensorNames[i]=key;
				i++;				 
			}
			return getListofEnabledSensorSignalsandFormats(mMyName, mSensorNames, sensorFormats, sensorUnits, sensorIsUsingDefaultCal);
		}
		
		private static List<String[]> getListofEnabledSensorSignalsandFormats(String myName, String[] sensorNames, String[] sensorFormats, String[] sensorUnits, String[] sensorIsUsingDefaultCal){
			List<String[]> listofSignals = new ArrayList<String[]>();
			for (int i=0;i<sensorNames.length;i++){
				String[] channel = new String[]{myName,sensorNames[i],sensorFormats[i],sensorUnits[i],sensorIsUsingDefaultCal[i]};
				listofSignals.add(channel);
			}
			
			return listofSignals;
		}
		
		private boolean compareStringArray(String[] stringArray, String string){
			boolean uniqueString=true;
			int size = stringArray.length;
			for (int i=0;i<size;i++){
				if (stringArray[i]==string){
					uniqueString=false;
				}	
						
			}
			return uniqueString;
		}
		
		public void createArrayData(int length){
			if(mListOfOCTypesEnabled.contains(OBJECTCLUSTER_TYPE.ARRAYS)){
				mUncalData = new double[length];
				mCalData = new double[length];
				mSensorNames = new String[length];
				mUnitCal = new String[length];
				mUnitUncal = new String[length];
			}
		}

		public void addData(ChannelDetails channelDetails, double uncalData, double calData) {
			addData(channelDetails, uncalData, calData, false);
		}

		public void addData(ChannelDetails channelDetails, double uncalData, double calData, boolean usingDefaultParameters) {
			addData(channelDetails, uncalData, calData, indexKeeper, usingDefaultParameters);
		}

		public void addData(ChannelDetails channelDetails, double uncalData, double calData, int index, boolean usingDefaultParameters) {
			if(channelDetails.mListOfChannelTypes.contains(CHANNEL_TYPE.UNCAL)){
				addUncalData(channelDetails, uncalData, index);
			}
			if(channelDetails.mListOfChannelTypes.contains(CHANNEL_TYPE.CAL)){
				addCalData(channelDetails, calData, index, usingDefaultParameters);
			}
			//TODO decide whether to include the below here
//			incrementIndexKeeper();
		}

		public void addCalData(ChannelDetails channelDetails, double calData) {
			addCalData(channelDetails, calData, indexKeeper);
		}

		public void addCalData(ChannelDetails channelDetails, double calData, int index) {
			addCalData(channelDetails, calData, index, false);
		}

		public void addCalData(ChannelDetails channelDetails, double calData, int index, boolean usingDefaultParameters) {
			addData(channelDetails.mObjectClusterName, CHANNEL_TYPE.CAL, channelDetails.mDefaultCalUnits, calData, index, usingDefaultParameters);
		}

		public void addUncalData(ChannelDetails channelDetails, double uncalData) {
			addUncalData(channelDetails, uncalData, indexKeeper);
		}

		public void addUncalData(ChannelDetails channelDetails, double uncalData, int index) {
			addData(channelDetails.mObjectClusterName, CHANNEL_TYPE.UNCAL, channelDetails.mDefaultUncalUnit, uncalData, index);
		}
		
		public void addData(String objectClusterName, CHANNEL_TYPE channelType, String units, double data) {
			addData(objectClusterName, channelType, units, data, indexKeeper);
		}

		public void addData(String objectClusterName, CHANNEL_TYPE channelType, String units, double data, int index) {
			addData(objectClusterName, channelType, units, data, index, false);
		}
		
		public void addData(String objectClusterName, CHANNEL_TYPE channelType, String units, double data, int index, boolean isUsingDefaultCalib) {
			if(mListOfOCTypesEnabled.contains(OBJECTCLUSTER_TYPE.ARRAYS)){
				if(channelType==CHANNEL_TYPE.CAL){
					mCalData[index] = data;
					mUnitCal[index] = units;
				}
				else if(channelType==CHANNEL_TYPE.UNCAL){
					mUncalData[index] = data;
					mUnitUncal[index] = units;
				}
				//TODO below not really needed, just put in to match some legacy code but can be removed. 
				else if(channelType==CHANNEL_TYPE.DERIVED){
					mCalData[index] = data;
					mUnitCal[index] = units;
					mUncalData[index] = data;
					mUnitUncal[index] = units;
				}
				mSensorNames[index] = objectClusterName;
				
				//TODO implement below here and remove everywhere else in the code
//				incrementIndexKeeper();
			}
			
			if(mListOfOCTypesEnabled.contains(OBJECTCLUSTER_TYPE.FORMAT_CLUSTER)){
				addDataToMap(objectClusterName, channelType.toString(), units, data, isUsingDefaultCalib);
			}
			
			if(mListOfOCTypesEnabled.contains(OBJECTCLUSTER_TYPE.PROTOBUF)){
				//TODO
			}
		}

		public void incrementIndexKeeper(){
			if(mListOfOCTypesEnabled.contains(OBJECTCLUSTER_TYPE.ARRAYS)){
				if(indexKeeper<mCalData.length){
					indexKeeper++;
				}
			}
		}

		public int getIndexKeeper() {
			return indexKeeper;
		}

		public void setIndexKeeper(int indexKeeper) {
			this.indexKeeper = indexKeeper;
		}
		
		public void addCalDataToMap(ChannelDetails channelDetails, double data){
			addDataToMap(channelDetails.mObjectClusterName, CHANNEL_TYPE.CAL.toString(), channelDetails.mDefaultCalUnits, data);
		}

		public void addUncalDataToMap(ChannelDetails channelDetails, double data){
			addDataToMap(channelDetails.mObjectClusterName, CHANNEL_TYPE.UNCAL.toString(), channelDetails.mDefaultCalUnits, data);
		}

		public void addDataToMap(String channelName, String channelType, String units, double data){
//			if(useList) {
//				mSensorDataList.add(new SensorData(channelName, channelType, units, data, false));
//			} else {
//				addDataToMap(channelName, channelType, units, data, false);
//			}
			
//			if(dataStructureSelector == 1) {	//JOS: Commented out to put each data structure in its own set method
//				mSensorDataList.add(new SensorData(channelName, channelType, units, data, false));
//			} else if(dataStructureSelector == 2) {
//				HashMap<String, FormatCluster> formatMap = new HashMap<>(3);
//				formatMap.put(channelType, new FormatCluster(channelType, units, data, false));
//				mHashMap.put(channelName, formatMap);
//			} else if(dataStructureSelector == 3) {
//				FormatCluster[] formatClusterArray = new FormatCluster[2];
//				formatClusterArray[0] = new FormatCluster(channelType, units, data, false);
//				mHashMapArray.put(channelName, formatClusterArray);
//			} else if(dataStructureSelector == 4) {
//				mSensorDataArray[mSensorDataArrayIndex] = new SensorData(channelName, channelType, units, data, false);
//				mSensorDataArrayIndex++;
//			} else if(dataStructureSelector == 5) {
//				addDataToMap(channelName, channelType, units, data, false);
//			}
			
			addDataToMap(channelName, channelType, units, data, false);
		}

		public void addDataToMap(String channelName, String channelType, String units, double data, boolean isUsingDefaultCalib){
//			if(useList) {
//				mSensorDataList.add(new SensorData(channelName, channelType, units, data, isUsingDefaultCalib));
//			} else {
//				mPropertyCluster.put(channelName,new FormatCluster(channelType, units, data, isUsingDefaultCalib));
//				addChannelNameToList(channelName);
//			}
			
//			if(dataStructureSelector == 1) {	//ArrayList			//JOS: Commented out to put each data structure in its own set method
//				mSensorDataList.add(new SensorData(channelName, channelType, units, data, isUsingDefaultCalib));
//			} else if(dataStructureSelector == 2) {	//HashMap
//				HashMap<String, FormatCluster> formatMap = new HashMap<>(3);
//				formatMap.put(channelName, new FormatCluster(channelType, units, data, isUsingDefaultCalib));
//				mHashMap.put(channelName, formatMap);
//			} else if(dataStructureSelector == 3) {	//HashMap with Array
//				FormatCluster[] formatClusterArray = new FormatCluster[2];
//				formatClusterArray[0] = new FormatCluster(channelType, units, data, isUsingDefaultCalib);
//				mHashMapArray.put(channelName, formatClusterArray);
//			} else if(dataStructureSelector == 4) {	//Arrays
//				mSensorDataArray[mSensorDataArrayIndex] = new SensorData(channelName, channelType, units, data, isUsingDefaultCalib);
//				mSensorDataArrayIndex++;
//			} else if(dataStructureSelector == 5) {	//Multimap
//				mPropertyCluster.put(channelName, new FormatCluster(channelType, units, data, isUsingDefaultCalib));
////				addChannelNameToList(channelName);		//TODO JOS: Is this necessary?
//			}

			
//			if(channelType.equals(CHANNEL_TYPE.CAL.toString())) {
//				mSensorNamesCal[calArrayIndex] = channelName;
//				mUnitCal[calArrayIndex] = units;
//				mCalDataNew[calArrayIndex] = data;
//				calArrayIndex++;
//			} else {
//				mSensorNamesUncal[uncalArrayIndex] = channelName;
//				mUnitUncal[uncalArrayIndex] = units;
//				mUncalDataNew[uncalArrayIndex] = data;
//				uncalArrayIndex++;
//			}

			
			
			mPropertyCluster.put(channelName, new FormatCluster(channelType, units, data, isUsingDefaultCalib));
//			addChannelNameToList(channelName);		//TODO JOS: Is this necessary?
		}
		
		public void addDataToArrayList(String channelName, String channelType, String units, double data) {
			mSensorDataList.add(new SensorData(channelName, channelType, units, data, false));
		}
		
		public void addDataToNestedHashMap(String channelName, String channelType, String units, double data) {
			HashMap<String, FormatCluster> formatMap = new HashMap<>(3);
			formatMap.put(channelType, new FormatCluster(channelType, units, data, false));
			mHashMap.put(channelName, formatMap);
		}
		
		public void addDataToHashMapArray(String channelName, String channelType, String units, double data) {
			FormatCluster[] formatClusterArray = new FormatCluster[2];
			formatClusterArray[0] = new FormatCluster(channelType, units, data, false);
			mHashMapArray.put(channelName, formatClusterArray);
		}
		
		public void addDataToSensorDataArray(String channelName, String channelType, String units, double data) {
			mSensorDataArray[mSensorDataArrayIndex] = new SensorData(channelName, channelType, units, data, false);
			mSensorDataArrayIndex++;
		}
		
		public int calArrayIndex = 0;
		public int calArrayIndexResize = 0;
		public int uncalArrayIndex = 0;	
		
		public void addDataToArrays(String channelName, String channelType, String units, double data) {
			if(channelType.equals(CHANNEL_TYPE.CAL.toString())) {
				mSensorNamesCal[calArrayIndex] = channelName;
				mUnitCal[calArrayIndex] = units;
				mCalData[calArrayIndex] = data;
				calArrayIndex++;
			} else {
				mSensorNamesUncal[uncalArrayIndex] = channelName;
				mUnitUncal[uncalArrayIndex] = units;
				mUncalData[uncalArrayIndex] = data;
				uncalArrayIndex++;
			}
		}
		
		public void addDataToArraysWithResize(String channelName, String channelType, String units, double data) {
			if(channelType.equals(CHANNEL_TYPE.CAL.toString())) {
				mSensorNamesCalResize[calArrayIndexResize] = channelName;
				mUnitCalResize[calArrayIndexResize] = units;
				mCalDataResize[calArrayIndexResize] = data;
				calArrayIndexResize++;
			} else {
				//TODO JOS: UNCAL ARRAYS HERE
			}
		}
		
		public void updateArraySensorDataIndex() {
			for(int i=0; i<mSensorDataArray.length; i++) {
				if(mSensorDataArray[i] == null) {
					mSensorDataArrayIndex = i;
					return;
				}
			}
		}	

		@Deprecated
		public void addDataToMap(String channelName, String channelType, String units, List<Double> data){
			mPropertyCluster.put(channelName,new FormatCluster(channelType, units, data));
			addChannelNameToList(channelName);
		}
		
		@Deprecated
		public void addDataToMap(String channelName,String channelType, String units, double data, List<Double> dataArray){
			mPropertyCluster.put(channelName,new FormatCluster(channelType, units, data, dataArray));
			addChannelNameToList(channelName);
		}
		
		private void addChannelNameToList(String channelName) {
			if(!listOfChannelNames.contains(channelName)){
				listOfChannelNames.add(channelName);
			}
		}
		
		@Deprecated
		public void removeAll(String channelName){
			mPropertyCluster.removeAll(channelName);
			listOfChannelNames = new ArrayList<String>();
		}
		
		public Collection<FormatCluster> getCollectionOfFormatClusters(String channelName){
			return mPropertyCluster.get(channelName);
		}

		public Set<String> getChannelNamesFromKeySet(){
			return mPropertyCluster.keySet();
		}

		public List<String> getChannelNamesByInsertionOrder(){
			return listOfChannelNames;
		}

		public Multimap<String, FormatCluster> getPropertyCluster(){
			return mPropertyCluster;
		}
		
		public ObjectCluster2 buildProtoBufMsg(){
			mObjectClusterBuilder = ObjectCluster2.newBuilder();
			for (String channelName:mPropertyCluster.keys()){
				Collection<FormatCluster> fcs = mPropertyCluster.get(channelName);
				FormatCluster2.Builder fcb = FormatCluster2.newBuilder();
				for(FormatCluster fc:fcs){
					DataCluster2.Builder dcb = DataCluster2.newBuilder();
					if (fc.mData!=Double.NaN){
						dcb.setData(fc.mData);	
					}
					if (fc.mDataObject!=null && fc.mDataObject.size()>0){
						dcb.addAllDataArray(fc.mDataObject);
					}
					dcb.setUnit(fc.mUnits);
					fcb.getMutableFormatMap().put(fc.mFormat, dcb.build());
				}
				mObjectClusterBuilder.getMutableDataMap().put(channelName, fcb.build());
			}
			if(mBluetoothAddress!=null)
				mObjectClusterBuilder.setBluetoothAddress(mBluetoothAddress);
			if(mMyName!=null)
				mObjectClusterBuilder.setName(mMyName);
			mObjectClusterBuilder.setCalibratedTimeStamp(mTimeStampMilliSecs);
			ByteBuffer bb = ByteBuffer.allocate(8);
	    	bb.put(mSystemTimeStamp);
	    	bb.flip();
	    	long systemTimeStamp = bb.getLong();
			mObjectClusterBuilder.setSystemTime(systemTimeStamp);
			return mObjectClusterBuilder.build();
		}
		
		public double getTimestampMilliSecs() {
			return mTimeStampMilliSecs;
		}

		public void setTimeStampMilliSecs(double timeStampMilliSecs) {
			this.mTimeStampMilliSecs = timeStampMilliSecs;
		}
		
		/**
		 * @return the mListOfOCTypesEnabled
		 */
		public List<OBJECTCLUSTER_TYPE> getListOfOCTypesEnabled() {
			return mListOfOCTypesEnabled;
		}

		/**
		 * @param listOfOCTypesEnabled the mListOfOCTypesEnabled to set
		 */
		public void setListOfOCTypesEnabled(List<OBJECTCLUSTER_TYPE> listOfOCTypesEnabled) {
			mListOfOCTypesEnabled = listOfOCTypesEnabled;
		}

		public void consolePrintChannelsAndDataSingleLine() {
			System.out.println("ShimmerName:" + mMyName);
			System.out.println("Channels in ObjectCluster:");
			String channelsCal = "Cal:\t";
			String channelsUncal = "Uncal:\t";
			for(String channel:getChannelNamesByInsertionOrder()){
				channelsCal += channel + "=" + getFormatClusterValue(channel, CHANNEL_TYPE.CAL.toString()) + "\t";
				channelsUncal += channel + "=" + getFormatClusterValue(channel, CHANNEL_TYPE.UNCAL.toString()) + "\t";
			}
			System.out.println(channelsCal);
			System.out.println(channelsUncal);
			System.out.println("");
		}

		public void consolePrintChannelsAndDataGrouped() {
			System.out.println("Channels in ObjectCluster:");
			for(String channel:getChannelNamesByInsertionOrder()){
				System.out.println("\t" + channel + ":\t(" + getFormatClusterValue(channel, CHANNEL_TYPE.UNCAL.toString()) + "," + getFormatClusterValue(channel, CHANNEL_TYPE.CAL.toString()) + ")");
			}
			System.out.println("");
		}

		public static ObjectCluster[] generateRandomObjectClusterArray(String deviceName, String signalName, int numSamples, int minValue, int maxValue) {
			Random rand = new Random();
			
			double[] dataArray = new double[numSamples];
			for(int i=0;i<numSamples;i++){
				dataArray[i] = rand.nextInt(maxValue);
			}

//			double[] dataArray = rand.doubles(numSamples, minValue, maxValue);
			
			double timestamp = 0.00001;
			ObjectCluster[] ojcArray = new ObjectCluster[numSamples];
			for(int i=0;i<numSamples;i++){
				ObjectCluster ojc = new ObjectCluster(deviceName);
				ojc.createArrayData(1);
				ojc.addData(signalName, CHANNEL_TYPE.CAL, "", dataArray[i]);
				ojc.addCalData(ShimmerClock.channelSystemTimestampPlot, timestamp);
				timestamp+=1;
				ojcArray[i] = ojc;
			}
			
			return ojcArray;
		}

		public ObjectCluster deepClone() {
//			System.out.println("Cloning:" + mUniqueID);
			try {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ObjectOutputStream oos = new ObjectOutputStream(baos);
				oos.writeObject(this);

				ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
				ObjectInputStream ois = new ObjectInputStream(bais);
				return (ObjectCluster) ois.readObject();
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				return null;
			}
		}

	}

	
	
	
}



