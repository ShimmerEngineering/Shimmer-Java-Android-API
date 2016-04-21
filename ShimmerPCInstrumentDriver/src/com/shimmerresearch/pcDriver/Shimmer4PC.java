package com.shimmerresearch.pcDriver;

import com.shimmerresearch.comms.radioProtocol.ShimmerRadio;
import com.shimmerresearch.driver.Shimmer4;
import com.shimmerresearch.pcRadioDriver.ShimmerBTRadioPC;

public class Shimmer4PC extends Shimmer4{
	
	public void initialize(){
		mShimmerRadio = new ShimmerBTRadioPC();
		super.initialize();
	}

}
