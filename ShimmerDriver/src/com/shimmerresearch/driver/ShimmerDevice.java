package com.shimmerresearch.driver;

import java.io.Serializable;

public abstract class ShimmerDevice extends BasicProcessWithCallBack implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5087199076353402591L;

	/**Holds unique location information on a dock or COM port number for Bluetooth connection*/
	public String mUniqueID = "";
	
	public ShimmerVerObject mShimmerVerObject = new ShimmerVerObject();
	public ShimmerBattStatusDetails mShimmerBattStatusDetails = new ShimmerBattStatusDetails(); 
	public ShimmerSDCardDetails mShimmerSDCardDetails = new ShimmerSDCardDetails(); 

	public void setShimmerVerObject(ShimmerVerObject sVO) {
		mShimmerVerObject = sVO;
	}

	public void setBattStatusDetails(ShimmerBattStatusDetails shimmerBattStatusDetails) {
		mShimmerBattStatusDetails = shimmerBattStatusDetails;
	}

	public void setShimmerDriveInfo(ShimmerSDCardDetails shimmerSDCardDetails) {
		mShimmerSDCardDetails = shimmerSDCardDetails;
	}

	public abstract ShimmerDevice deepClone();


	
}
