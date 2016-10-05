package com.shimmerresearch.driverUtilities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

//A work in progress - not complete
public class SensorParsingDetails implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8384885695261304687L;
	
	public int head_byte_mask=-9999;
	public int sampling_divider=-9999;
	public int sampling_period=-9999;
	public double sampling_rate=-9999.0;
	public int data_rate=-9999;
	public int range=-9999;
//		public int lp_mode=-9999;
//		public int hr_mode=-9999;
	public int numChannels=-9999;
	
	public List<ChannelDetails> mListOfChannels = new ArrayList<ChannelDetails>();
	
	public double[][] offsetVector = new double[3][1];
	public double[][] sensitivityVector = new double[3][3];
	public double[][] alignmentVector = new double[3][3];
		
//		double b0=-9999.0;
//		double b1=-9999.0;
//		double b2=-9999.0;
//		double k0=-9999.0;
//		double k1=-9999.0;
//		double k2=-9999.0;
//		double r00=-9999.0;
//		double r01=-9999.0;
//		double r02=-9999.0;
//		double r10=-9999.0;
//		double r11=-9999.0;
//		double r12=-9999.0;
//		double r20=-9999.0;
//		double r21=-9999.0;
//		double r22=-9999.0;
}