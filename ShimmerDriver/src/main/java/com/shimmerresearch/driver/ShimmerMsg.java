package com.shimmerresearch.driver;

public class ShimmerMsg {
	public int mIdentifier;
	public Object mB;

	public ShimmerMsg(int a) {
		mIdentifier=a;
	}

	public ShimmerMsg(int a, Object b){
		this(a);
		mB=b;
	}

}
