package com.shimmerresearch.sensor;

import java.io.Serializable;
import java.util.HashMap;

import com.shimmerresearch.driver.Configuration.CHANNEL_UNITS;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driver.Configuration.Shimmer3;
import com.shimmerresearch.driver.Configuration.Shimmer3.CompatibilityInfoForMaps;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driverUtilities.ChannelDetails;
import com.shimmerresearch.driverUtilities.SensorConfigOptionDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_TYPE;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.HW_ID;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.FormatCluster;
import com.shimmerresearch.driver.ShimmerObject;

public class ShimmerGSRSensor extends AbstractSensor implements Serializable{
	
	private int mGSRRange;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1773291747371088953L;

	public ShimmerGSRSensor(ShimmerVerObject svo) {
		super(svo);
		mSensorName = SENSOR_NAMES.GSR;
	}

	@Override
	public String getSensorName() {
		// TODO Auto-generated method stub
		return mSensorName;
	}

	@Override
	public Object getSettings(String componentName, COMMUNICATION_TYPE comType) {
		// TODO Auto-generated method stub
		return null;
	}




	@Override
	public ActionSetting setSettings(String componentName, Object valueToSet,
			COMMUNICATION_TYPE comType) {
		// TODO Auto-generated method stub
		ActionSetting actionSetting = new ActionSetting(comType);
		switch(componentName){
			case(Configuration.Shimmer3.GuiLabelConfig.GSR_RANGE):
				if (comType == COMMUNICATION_TYPE.BLUETOOTH){
					
				} else if (comType == COMMUNICATION_TYPE.DOCK){
					
				}
			break;
		}
		return actionSetting;
	}

	@Override
	public HashMap<COMMUNICATION_TYPE, HashMap<Integer, ChannelDetails>> generateChannelDetailsMap(
			ShimmerVerObject svo) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HashMap<String, SensorConfigOptionDetails> generateConfigOptionsMap(
			ShimmerVerObject svo) {
		// TODO Auto-generated method stub
		if (svo.mFirmwareIdentifier == ShimmerVerDetails.FW_ID.BTSTREAM ||
				svo.mFirmwareIdentifier == ShimmerVerDetails.FW_ID.SDLOG)
				mConfigOptionsMap.put(Configuration.Shimmer3.GuiLabelConfig.GSR_RANGE, 
						new SensorConfigOptionDetails(Configuration.Shimmer3.ListofGSRRange, 
												Configuration.Shimmer3.ListofGSRRangeConfigValues, 
												SensorConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX,
												CompatibilityInfoForMaps.listOfCompatibleVersionInfoGsr));
				
		return mConfigOptionsMap;
	}

	@Override
	public Object processData(byte[] rawData, COMMUNICATION_TYPE comType, Object obj) {

		if (comType == COMMUNICATION_TYPE.IEEE802154){
			String[] format = new String[1];
			format[0] = "u16";
			long[] rawValue = parsedData(rawData,format);
			ObjectCluster objectCluster = (ObjectCluster) obj;
			int newGSRRange = -1; // initialized to -1 so it will only come into play if mGSRRange = 4  
			double p1=0,p2=0;
			if (mGSRRange==4){
				newGSRRange=(49152 & (int)rawData[0])>>14; 
			}
			if (mGSRRange==0 || newGSRRange==0) { //Note that from FW 1.0 onwards the MSB of the GSR data contains the range
				// the polynomial function used for calibration has been deprecated, it is replaced with a linear function
				if (mShimmerVerObject.mHardwareVersion!=HW_ID.SHIMMER_3){
					p1 = 0.0373;
					p2 = -24.9915;

				} else { //Values have been reverted to 2r values
					//p1 = 0.0363;
					//p2 = -24.8617;
					p1 = 0.0373;
					p2 = -24.9915;
				}
			} else if (mGSRRange==1 || newGSRRange==1) {
				if (mShimmerVerObject.mHardwareVersion!=HW_ID.SHIMMER_3){
					p1 = 0.0054;
					p2 = -3.5194;
				} else {
					//p1 = 0.0051;
					//p2 = -3.8357;
					p1 = 0.0054;
					p2 = -3.5194;
				}
			} else if (mGSRRange==2 || newGSRRange==2) {
				if (mShimmerVerObject.mHardwareVersion!=HW_ID.SHIMMER_3){
					p1 = 0.0015;
					p2 = -1.0163;
				} else {
					//p1 = 0.0015;
					//p2 = -1.0067;
					p1 = 0.0015;
					p2 = -1.0163;
				}
			} else if (mGSRRange==3  || newGSRRange==3) {
				if (mShimmerVerObject.mHardwareVersion!=HW_ID.SHIMMER_3){
					p1 = 4.5580e-04;
					p2 = -0.3014;
				} else {
					//p1 = 4.4513e-04;
					//p2 = -0.3193;
					p1 = 4.5580e-04;
					p2 = -0.3014;
				}
			}
			//Temp commented out by MN to stop errors in driver
//			objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.GSR,new FormatCluster(CHANNEL_TYPE.UNCAL.toString(),CHANNEL_UNITS.NO_UNITS,(double)newPacketInt[iGSR]));
//			uncalibratedData[iGSR]=(double)newPacketInt[iGSR];
//			uncalibratedDataUnits[iGSR]=CHANNEL_UNITS.NO_UNITS;
//			if (mEnableCalibration){
//				calibratedData[iGSR] = calibrateGsrData(tempData[0],p1,p2);
//				calibratedDataUnits[iGSR]=CHANNEL_UNITS.KOHMS;
//				objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.GSR,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.KOHMS,calibratedData[iGSR]));
//				//			calibratedData[iGSR] = calibrateGsrDataToSiemens(tempData[0],p1,p2);
//				//			calibratedDataUnits[iGSR]=CHANNEL_UNITS.MICROSIEMENS;
//				//			objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.GSR,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MICROSIEMENS,calibratedData[iGSR]));
//			}

		}
		return null;
	}

	
	
}
