package com.shimmerresearch.pcdriver;

import com.shimmerresearch.driver.Shimmer4;
import com.shimmerresearch.pcradiodriver.ShimmerBTRadioPC;
import com.shimmerresearch.radiodriver.ShimmerRadio;

public class Shimmer4PC extends Shimmer4{
	
	public void initialize(){
		mShimmerRadio = new ShimmerBTRadioPC();
		super.initialize();
	}

}
