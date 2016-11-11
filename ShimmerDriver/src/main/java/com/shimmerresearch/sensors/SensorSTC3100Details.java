package com.shimmerresearch.sensors;

import com.shimmerresearch.driverUtilities.UtilParseData;
import com.shimmerresearch.driverUtilities.UtilShimmer;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_ENDIAN;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_TYPE;

/**
 * @author Mark Nolan
 *
 */
public class SensorSTC3100Details {
	
	private static double R_SENSE = 0.033; // ohms
	
	/**Gas gauge charge data (mA.h)*/
	public double mBattCharge = 0;
	/**Number of conversions */
	public double mBattCounter = 0;
	/**Battery current value (mA)*/
	public double mBattCurrent = 0;
	/**Battery voltage value (mV)*/
	public double mBattVoltage = 0;
	/**Temperature value (degrees C)*/
	public double mBattTemperature = 0;
	
	private byte[] mRegBytes = new byte[]{};
	
	public SensorSTC3100Details(byte[] regBytes){
		mRegBytes = regBytes;
		
		if(mRegBytes.length>=10){
			
//			mBattCharge = ((mRegBytes[1]&0xFF)<<8) | (mRegBytes[0]&0xFF);
//			mBattCounter = ((mRegBytes[3]&0xFF)<<8) | (mRegBytes[2]&0xFF);
//			mBattCurrent = ((((mRegBytes[5]&0xFF)<<8) | (mRegBytes[4]&0xFF)) * 11.77) / R_SENSE;
//			mBattVoltage = (((mRegBytes[7]&0xFF)<<8) | (mRegBytes[6]&0xFF)) * 2.44;
//			mBattTemperature = (((mRegBytes[9]&0xFF)<<8) | (mRegBytes[8]&0xFF)) * 0.125;

			mBattCharge = (UtilParseData.parseData(new byte[]{mRegBytes[0], mRegBytes[1]}, CHANNEL_DATA_TYPE.INT16, CHANNEL_DATA_ENDIAN.LSB) * 6.70) / R_SENSE;
			mBattCounter = ((mRegBytes[3]&0xFF)<<8) | (mRegBytes[2]&0xFF);
			mBattCurrent = (UtilParseData.parseData(new byte[]{mRegBytes[4], mRegBytes[5]}, CHANNEL_DATA_TYPE.INT14, CHANNEL_DATA_ENDIAN.LSB) * 11.77) / R_SENSE;
			mBattVoltage = UtilParseData.parseData(new byte[]{mRegBytes[6], mRegBytes[7]}, CHANNEL_DATA_TYPE.INT12, CHANNEL_DATA_ENDIAN.LSB) * 2.44;
			mBattTemperature = UtilParseData.parseData(new byte[]{mRegBytes[8], mRegBytes[9]}, CHANNEL_DATA_TYPE.INT12, CHANNEL_DATA_ENDIAN.LSB) * 0.125;
		}
	}

	public String getDebugString(){
		String debugString = "STC3100:" 
				+ "\tCharge = " + String.format("%1$.2f", mBattCharge) + " mA.h"
				+ "\tCounter = " + mBattCounter
				+ "\tCurrent = " + String.format("%1$.2f", mBattCurrent) + " mA"
				+ "\tVoltage = " + String.format("%1$.2f", mBattVoltage) + " mV"
				+ "\tTemperature = " + String.format("%1$.2f", mBattTemperature) + " degrees C";
		
		debugString += "\n" + UtilShimmer.bytesToHexStringWithSpacesFormatted(mRegBytes);
		
		return debugString;
	}
}
