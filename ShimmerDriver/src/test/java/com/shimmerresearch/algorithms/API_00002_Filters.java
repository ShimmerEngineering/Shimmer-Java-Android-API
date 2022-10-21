package com.shimmerresearch.algorithms;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.RoundingMode;
import java.text.DecimalFormat;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 * @author Mark Nolan, Ruaidhri Molloy
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class API_00002_Filters {
	
	private static final boolean REPLACE_REFERENCE_CSVS = false;

	private static final String UNIT_TEST_FILES_PATH = System.getenv("USERPROFILE") + "/Shimmer Research Ltd/Shimmer - Shimmer/Applications Team/Resources/Data Repository/JUnitTests/Shimmer-Java-Android-API/API_00002_Filters/";
	
	/**
	 * Passes invalid configuration into the filter initialisation in order to throw an error.
	 */
	@Test
	public void Test_001_InvalidFilterConfig() {
		try {
			Filter filter = new Filter(Filter.LOW_PASS, 50.0, new double[]{75});
			assertTrue("Shouldn't get to this line", false);
		} catch (Exception e) {
			System.out.println("Test correctly threw an error");
		}
	}

	/**
	 *  Basic low-pass filter test
	 */
	@Test
	public void Test_002_LowPass() {
		String testId = "Test_002";
		int filterType = Filter.LOW_PASS;
		double samplingRate = 1024.0;
		double[] cornerFrequency = { 10 };
		int nTaps = 200;
		
		String sourceCsv = UNIT_TEST_FILES_PATH + "WhiteNoise_001.csv";
		String referenceCsv = UNIT_TEST_FILES_PATH + "Reference/" + testId + ".csv";
		
		runTestCommon(testId, sourceCsv, referenceCsv, filterType, samplingRate, cornerFrequency, nTaps);
	}

	/**
	 *  Basic high-pass filter test
	 */
	@Test
	public void Test_003_HighPass() {
		String testId = "Test_003";
		int filterType = Filter.HIGH_PASS;
		double samplingRate = 1024.0;
		double[] cornerFrequency = { 400 };
		int nTaps = 200;
		
		String sourceCsv = UNIT_TEST_FILES_PATH + "WhiteNoise_001.csv";
		String referenceCsv = UNIT_TEST_FILES_PATH + "Reference/" + testId + ".csv";
		
		runTestCommon(testId, sourceCsv, referenceCsv, filterType, samplingRate, cornerFrequency, nTaps);
	}

	/**
	 *  Basic band-stop filter test
	 */
	@Test
	public void Test_004_BandStop() {
		String testId = "Test_004";
		int filterType = Filter.BAND_STOP;
		double samplingRate = 1024.0;
		double[] cornerFrequency = { 100, 400 };
		int nTaps = 200;
		
		String sourceCsv = UNIT_TEST_FILES_PATH + "WhiteNoise_001.csv";
		String referenceCsv = UNIT_TEST_FILES_PATH + "Reference/" + testId + ".csv";
		
		runTestCommon(testId, sourceCsv, referenceCsv, filterType, samplingRate, cornerFrequency, nTaps);
	}
	
	/**
	 * Basic band-pass filter test
	 */
	@Test
	public void Test_005_BandPass() {
		String testId = "Test_005";
		int filterType = Filter.BAND_PASS;
		double samplingRate = 1024.0;
		double[] cornerFrequencies = { 100, 400 };
		int nTaps = 200;
		
		String sourceCsv = UNIT_TEST_FILES_PATH + "WhiteNoise_001.csv";
		String referenceCsv = UNIT_TEST_FILES_PATH + "Reference/" + testId + ".csv";
		
		runTestCommon(testId, sourceCsv, referenceCsv, filterType, samplingRate, cornerFrequencies, nTaps);
	}

	private void runTestCommon(String testId, String sourceCsv, String referenceCsv, int filterType, double samplingRate, double[] cornerFrequency, int nTaps) {
		System.out.println("\n ------------------- " + testId + " start -------------------\n");
		
		printTestDetails(filterType, samplingRate, cornerFrequency, nTaps);
		
		System.out.println("Test steps:");
		try {
			System.out.println("\t1) Loading source CSV");
			double[][] sourceCsvArray = csvToDoubleArray(sourceCsv, 0);
			if(sourceCsvArray==null) {
				assertTrue("dataArray is null", false);
			}
			double[] sourceSignal = sourceCsvArray[0];

			System.out.println("\t2) Initialising the filter");
			Filter filter = new Filter(filterType, samplingRate, cornerFrequency, nTaps);
			
			System.out.println("\t3) Filtering the data");
			double[] filteredSignal = filter.filterData(sourceSignal);
			
			if(REPLACE_REFERENCE_CSVS) {
				System.out.println("\tWARNING! Replacing the reference CSV");
				saveReferenceCsv(referenceCsv, filteredSignal);
			}
			
			System.out.println("\t4) Loading reference CSV");
			double[][] referenceCsvArray = csvToDoubleArray(referenceCsv, 0);
			double[] referenceSignal = referenceCsvArray[0];
			
			System.out.println("\t5) Comparing the filtered array vs. the reference array");
			compareDoubleArrays(filteredSignal, referenceSignal);
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue("Error -> see console", false);
		}
		
		System.out.println("\n ------------------- " + testId + " end -------------------\n");
	}
	
	
	private void printTestDetails(int filterType, double samplingRate, double[] cornerFrequency, int nTaps) {
		String filterTypeStr = "";
		if(filterType==Filter.LOW_PASS) {
			filterTypeStr = "Low-pass";
		} else if(filterType==Filter.HIGH_PASS) {
			filterTypeStr = "High-pass";
		} else if(filterType==Filter.BAND_PASS) {
			filterTypeStr = "Band-pass";
		} else if(filterType==Filter.BAND_STOP) {
			filterTypeStr = "Band-stop";
		}
		
		String cornerFreqs = "{";
		for(int i=0;i<cornerFrequency.length;i++) {
			cornerFreqs += String.valueOf(cornerFrequency[i]);
			if(i<cornerFrequency.length-1) {
				cornerFreqs += ", ";
			}
		}
		cornerFreqs += "}";
		
		System.out.println("Filter Type = " + filterTypeStr
				+ "\nSampling Rate = " + samplingRate
				+ "\nCorner Freq(s) = " + cornerFreqs
				+ "\nOrder = " + nTaps);
	}

	private void compareDoubleArrays(double[] filteredSignal, double[] referenceSignal) {
		if(filteredSignal==null || referenceSignal==null) {
			assertTrue("An array is null", false);
		}
		if(filteredSignal.length!=referenceSignal.length) {
			assertTrue("Array lengths are not equal", false);
		}
		
		compareDoubleArraysToNDecimalPlaces(filteredSignal, referenceSignal, 10);
	}

	private void compareDoubleArraysToNDecimalPlaces(double[] a, double[] b, int nDecimalPlaces) {
		String format = "#.";
		for(int i=0;i<nDecimalPlaces;i++) {
			format += "#";
		}
		DecimalFormat df = new DecimalFormat(format);
		df.setRoundingMode(RoundingMode.HALF_UP);
		
		for(int i=0;i<a.length;i++) {
			System.out.println(df.format(a[i]) + "\t" + df.format(b[i]));
			assertTrue("Arrays are not equal at index:" + i, df.format(a[i]).equals(df.format(b[i])));
		}
	}

	private void saveReferenceCsv(String referenceCsv, double[] filteredSignal) {
		FileWriter fw = null;
		try {
			fw = new FileWriter(referenceCsv, false);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		BufferedWriter bw = new BufferedWriter(fw);
		String tempStr = "";
		try {
			for (double d:filteredSignal) {
				tempStr = String.valueOf(d);
				bw.write(tempStr);
				bw.newLine();
			}
			bw.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	private double[][] csvToDoubleArray(String csvFile, int qtyHeaderLines) throws Exception {
		double[][] dataArray = null;
		
		BufferedReader br = null;
		try {
			FileInputStream fIn = new FileInputStream(csvFile);
			br = new BufferedReader(new InputStreamReader(fIn));
			String line = "";
			
			int lineCount = 0;
			int colCount = 0;
			//count Lines
			while ((line = br.readLine()) != null) {
				lineCount++;
				//Pick a line after the header lines to detect the number of channels
				if(lineCount==qtyHeaderLines+1) {
					String[] data = line.split(",");
					colCount = data.length;
				}
			}

			lineCount-=qtyHeaderLines;

			// "reset" to beginning of file (discard old buffered reader)
			fIn.getChannel().position(0);
			br = new BufferedReader(new InputStreamReader(fIn));
			
			dataArray = new double[colCount][lineCount];

			lineCount = 0;
			while ((line = br.readLine()) != null) {
				if(lineCount>=qtyHeaderLines) {
					String[] data = line.split(",");
					for(int i=0;i<data.length;i++) {
						String d = data[i];
						dataArray[i][lineCount-qtyHeaderLines] = Double.parseDouble(d);
					}
				}
				lineCount++;

			}
		} catch (Exception e) {
			e.printStackTrace();
			throw(e);
		} finally {
			if(br!=null) {
				br.close();
			}
		}
		
		return dataArray;
	}

}
