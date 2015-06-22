package com.shimmerresearch.exgConfig;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import com.shimmerresearch.exgConfig.ExGConfigOptionDetails.CHIP_INDEX;


public class ExGConfigOptionDetails implements Serializable {

	public CHIP_INDEX chipIndex = CHIP_INDEX.CHIP1;
	public int byteIndex = 0;
	public String GuiLabel;
	public String[] GuiValues;
	public Integer[] ConfigValues;
	public int valueInt = 0;
	public int bitShift = 0;
	public int mask = 0;

	public static enum SettingType{
		COMBOBOX,
		CHECKBOX
	}
	
	public static enum CHIP_INDEX{
		CHIP1,
		CHIP2
	}
	
	public SettingType settingType = SettingType.CHECKBOX;
	
	public boolean valueBool = false;
	
	public ExGConfigOptionDetails(CHIP_INDEX chipIndex, int byteIndex, String GuiLabel, int bitShift){
		super();
		this.chipIndex = chipIndex;
		this.byteIndex = byteIndex;
		this.GuiLabel = GuiLabel;
		this.bitShift = bitShift;
		this.mask = 0x01;
		this.settingType = SettingType.CHECKBOX;
	}
	
	public ExGConfigOptionDetails(CHIP_INDEX chipIndex, int byteIndex, String GuiLabel, String[] GuiValues, Integer[] ConfigValues, int bitShift, int mask){
		super();
		this.chipIndex = chipIndex;
		this.byteIndex = byteIndex;
		this.GuiLabel = GuiLabel;
		this.GuiValues = GuiValues;
		this.ConfigValues = ConfigValues;
		this.bitShift = bitShift;
		this.mask = mask;
		this.settingType = SettingType.COMBOBOX;
	}

	public ExGConfigOptionDetails(CHIP_INDEX chipIndex, int byteIndex, String GuiLabel, ExGConfigOption[] exGConfigOptions, int bitShift, int mask) {
		super();
		this.chipIndex = chipIndex;
		this.byteIndex = byteIndex;
		this.GuiLabel = GuiLabel;
		this.bitShift = bitShift;
		this.mask = mask;
		this.settingType = SettingType.COMBOBOX;
		
		this.GuiValues = new String[exGConfigOptions.length];
		this.ConfigValues = new Integer[exGConfigOptions.length];
		for(int i=0;i<exGConfigOptions.length;i++){
			ExGConfigOption exGConfigOption = exGConfigOptions[i];
			this.GuiValues[i] = exGConfigOption.guiValue;
			this.ConfigValues[i] = exGConfigOption.configValueInt;
		}
	}

	public String[] getGuiValues() {
		return GuiValues;
	}

	public Integer[] getConfigValues() {
		return ConfigValues;
	}
	
	/**Performs a deep copy of this object by Serializing
	 * @return ExGConfigOptionDetails the deep copy of the object
	 * @see java.io.Serializable
	 */
	public ExGConfigOptionDetails deepClone() {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(this);

			ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
			ObjectInputStream ois = new ObjectInputStream(bais);
			return (ExGConfigOptionDetails) ois.readObject();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

	
}
