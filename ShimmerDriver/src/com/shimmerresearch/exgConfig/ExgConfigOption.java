package com.shimmerresearch.exgConfig;

public class ExgConfigOption {
	public int byteIndex = 0;
	public String GuiLabel;
	public int valueInt = 0;
	public int bitShift = 0;
	public int mask = 0;
	
	public ExgConfigOption(int byteIndex, String GuiLabel, int valueInt, int bitShift, int mask){
		this.byteIndex = byteIndex;
		this.GuiLabel = GuiLabel;
		this.valueInt = valueInt;
		this.bitShift = bitShift;
		this.mask = mask;
	}
}
