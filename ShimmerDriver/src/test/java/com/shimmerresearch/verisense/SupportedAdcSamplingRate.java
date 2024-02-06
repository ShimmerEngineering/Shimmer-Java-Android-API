package com.shimmerresearch.verisense;

public class SupportedAdcSamplingRate {

	int settingValue;
	double samplingRate;
	int ticks;
	
	public SupportedAdcSamplingRate(int index, double samplingRate, int ticks) {
		this.settingValue = index;
		this.samplingRate = samplingRate;
		this.ticks = ticks;
	}
	
}
