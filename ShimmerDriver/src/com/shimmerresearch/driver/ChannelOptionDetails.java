package com.shimmerresearch.driver;

import java.io.Serializable;

/**
 * 
 * @param label
 * @param guiValues
 * @param values
 * @param optionType
 *
 * @author Mark Nolan
 */
public class ChannelOptionDetails implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8894717489924237791L;

	public static final int COMBOBOX = 0;
	public static final int CHECKBOX = 1;
	
	public String mLabel = "";
	public String[] mGuiValues;
	public Integer[] mConfigValues;
	public int mOptionType;
	
	/**
	 * @param label
	 * @param guiValues
	 * @param configValues
	 * @param optionType
	 */
	public ChannelOptionDetails(String label, String[] guiValues, Integer[] configValues, int optionType) {
		mLabel = label;
		mGuiValues = guiValues;
		mConfigValues = configValues;
		mOptionType = optionType;
	}
}
