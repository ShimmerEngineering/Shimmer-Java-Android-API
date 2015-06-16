package com.shimmerresearch.exgConfig;

public class ExgConfigOption {
//	public int byteIndex = 0;
	public String byteTitle = "";
	public String GuiLabel;
	public int valueInt = 0;
	public int bitShift = 0;
	public int mask = 0;

	public ExgConfigOption(String byteTitle, String GuiLabel, int valueInt){
		this.byteTitle = byteTitle;
		this.GuiLabel = GuiLabel;
		this.valueInt = valueInt;
	}
	
//	public ExgConfigOption(String byteTitle, String GuiLabel, int valueInt, int bitShift, int mask){
////		this.byteIndex = byteIndex;
//		this.byteTitle = byteTitle;
//		this.GuiLabel = GuiLabel;
//		this.valueInt = valueInt;
//		this.bitShift = bitShift;
//		this.mask = mask;
//	}
//	
//	public ExgConfigOption(String byteTitle, String GuiLabel, int valueInt, int bitShift){
////		this.byteIndex = byteIndex;
//		this.byteTitle = byteTitle;
//		this.GuiLabel = GuiLabel;
//		this.valueInt = valueInt;
//		this.bitShift = bitShift;
//		this.mask = 0x01;
//	}

}
