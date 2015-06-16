package com.shimmerresearch.exgConfig;

public class ExgConfigOption {
//	public int byteIndex = 0;
	public String settingTitle = "";
	public String guiValue;
	public int configValueInt = 0;
	public int bitShift = 0;
	public int mask = 0;

	public ExgConfigOption(String byteTitle, String guiValue, int configValue){
		this.settingTitle = byteTitle;
		this.guiValue = guiValue;
		this.configValueInt = configValue;
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
