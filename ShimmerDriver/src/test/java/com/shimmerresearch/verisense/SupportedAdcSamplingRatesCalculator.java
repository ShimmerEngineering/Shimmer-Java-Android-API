package com.shimmerresearch.verisense;

import java.util.ArrayList;
import java.util.List;

public class SupportedAdcSamplingRatesCalculator {

	public static void main(String[] args) {

		int acceptableDecimalPlaces = 2;
		int numDecimalPlacesToCheckForZero = 10;
		int resultIndex = 1;

		List<SupportedAdcSamplingRate> listOfResults = new ArrayList<SupportedAdcSamplingRate>();

		divisorLoop:
		for(int ticks=1;ticks<Math.pow(2, 16);ticks++) {
			double samplingRate = 32768.0 / ticks;

			String str = String.format("%." + numDecimalPlacesToCheckForZero + "f", samplingRate);
//			System.out.println(str);
			for(int x=str.length()-1;x>=str.length()-numDecimalPlacesToCheckForZero+acceptableDecimalPlaces;x--) {
				if(str.charAt(x)!='0') {
					continue divisorLoop;
				}
			}
			listOfResults.add(new SupportedAdcSamplingRate(resultIndex, samplingRate, ticks));
			
			resultIndex++;
		}
		
		// Print vertical table
		String codeComment = "# "; // # for Python, // for Java
		System.out.println(codeComment + "Setting Value, Sampling Rate (Hz), Ticks");
		System.out.println(codeComment + "0, Off, Off");
		for(SupportedAdcSamplingRate validResult:listOfResults) {
			System.out.println(codeComment + validResult.settingValue + ", " + validResult.samplingRate + ", " + validResult.ticks);
		}

		System.out.println("\n\n");
		
		// Print horizonal table
		String settingValueRow = "// Setting Value = 0", samplingRateRow = "// Sampling Rate (Hz) = Off", ticksRow = "// Ticks = Off";
		for(SupportedAdcSamplingRate validResult:listOfResults) {
			settingValueRow += ", " + validResult.settingValue;
			samplingRateRow += ", " + validResult.samplingRate;
			ticksRow += ", " + validResult.ticks;
		}
		System.out.println(settingValueRow);
		System.out.println(samplingRateRow);
		System.out.println(ticksRow);

		
	}
	
}
