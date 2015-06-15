package com.shimmerresearch.exgConfig;


public class ExGConfigOptionDetails {

	public int chipIndex = 0;
	public int byteIndex = 0;
	public String GuiLabel;
	public String[] GuiValues;
	public Integer[] ConfigValues;
	public int valueInt = 0;
	public int bitShift = 0;
	public int mask = 0;

	public static enum SettingType{
		combobox,
		checkbox
	}
	
	public SettingType settingType = SettingType.checkbox;
	
	public boolean valueBool = false;
	
	public ExGConfigOptionDetails(int chipIndex, int byteIndex, String GuiLabel, int bitShift){
		super();
		this.chipIndex = chipIndex;
		this.byteIndex = byteIndex;
		this.GuiLabel = GuiLabel;
		this.bitShift = bitShift;
		this.mask = 0x01;
		this.settingType = SettingType.checkbox;
	}
	
	public ExGConfigOptionDetails(int chipIndex, int byteIndex, String GuiLabel, String[] GuiValues, Integer[] ConfigValues, int bitShift, int mask){
		super();
		this.chipIndex = chipIndex;
		this.byteIndex = byteIndex;
		this.GuiLabel = GuiLabel;
		this.GuiValues = GuiValues;
		this.ConfigValues = ConfigValues;
		this.bitShift = bitShift;
		this.mask = mask;
		this.settingType = SettingType.combobox;
	}
}
