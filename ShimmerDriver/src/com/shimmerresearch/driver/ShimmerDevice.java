package com.shimmerresearch.driver;

import java.io.Serializable;

public abstract class ShimmerDevice extends BasicProcessWithCallBack implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5087199076353402591L;

	/**Holds unique location information on a dock or COM port number for Bluetooth connection*/
	public String mUniqueID = "";
	
}
