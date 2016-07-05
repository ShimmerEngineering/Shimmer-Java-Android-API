package com.shimmerresearch.driverUtilities;

import java.io.Serializable;

public abstract class CalibDetails implements Serializable {
	
	/** * */
	private static final long serialVersionUID = 3071359258303179516L;

	public abstract byte[] generateCalParamByteArrayWithTimestamp();

}
