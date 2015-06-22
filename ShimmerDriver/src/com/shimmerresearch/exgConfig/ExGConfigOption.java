package com.shimmerresearch.exgConfig;

import java.io.Serializable;

public class ExGConfigOption implements Serializable {
	public String settingTitle = "";
	public String guiValue;
	public int configValueInt = 0;
	public int bitShift = 0;
	public int mask = 0;

	public ExGConfigOption(String byteTitle, String guiValue, int configValue){
		this.settingTitle = byteTitle;
		this.guiValue = guiValue;
		this.configValueInt = configValue;
	}

}
