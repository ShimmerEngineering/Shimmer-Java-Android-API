package com.shimmerresearch.sensors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.shimmerresearch.driver.Configuration.CHANNEL_UNITS;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driver.Configuration.Shimmer3;
import com.shimmerresearch.driver.Configuration.Shimmer3.CompatibilityInfoForMaps;
import com.shimmerresearch.driver.Configuration.Shimmer3.DatabaseChannelHandles;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.ShimmerMsg;
import com.shimmerresearch.driverUtilities.ChannelDetails;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_SOURCE;
import com.shimmerresearch.driverUtilities.SensorConfigOptionDetails;
import com.shimmerresearch.driverUtilities.SensorDetailsRef;
import com.shimmerresearch.driverUtilities.SensorDetails;
import com.shimmerresearch.driverUtilities.SensorGroupingDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_TYPE;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_ENDIAN;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_TYPE;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.HW_ID;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.FormatCluster;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driver.ShimmerObject;

public class SensorGSR extends AbstractSensor implements Serializable{

	/**	 */
	private static final long serialVersionUID = 1773291747371088953L;

	//--------- Sensor specific variables start --------------
	public int mGSRRange = 4; 					// 4 = Auto
	
	public static String calUnitToUse = Configuration.CHANNEL_UNITS.U_SIEMENS;
//	private static String calUnitToUse = Configuration.CHANNEL_UNITS.KOHMS;
	//--------- Sensor specific variables end --------------
	
	//--------- Bluetooth commands start --------------
	//--------- Bluetooth commands end --------------

	//--------- Configuration options start --------------
	public static final String[] ListofGSRRange={"10k\u2126 to 56k\u2126","56k\u2126 to 220k\u2126","220k\u2126 to 680k\u2126","680k\u2126 to 4.7M\u2126","Auto"};
	public static final Integer[] ListofGSRRangeConfigValues={0,1,2,3,4};

	public static final SensorConfigOptionDetails configOptionGsrRange = new SensorConfigOptionDetails(
			Configuration.Shimmer3.ListofGSRRange, 
			Configuration.Shimmer3.ListofGSRRangeConfigValues, 
			SensorConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoGsr);
	//--------- Configuration options end --------------

	//--------- Sensor info start --------------
	public static final SensorDetailsRef sensorGsrRef = new SensorDetailsRef(
			0x04<<(0*8), 
			0x04<<(0*8), 
			Shimmer3.GuiLabelSensors.GSR,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoGsr,
			Arrays.asList(
					Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A1,
					Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A14,
					Configuration.Shimmer3.SensorMapKey.HOST_ECG,
					Configuration.Shimmer3.SensorMapKey.HOST_EMG,
					Configuration.Shimmer3.SensorMapKey.HOST_EXG_TEST,
					Configuration.Shimmer3.SensorMapKey.HOST_EXG_CUSTOM,
					Configuration.Shimmer3.SensorMapKey.HOST_EXG_RESPIRATION,
//					Configuration.Shimmer3.SensorMapKey.EXG1_16BIT,
//					Configuration.Shimmer3.SensorMapKey.EXG2_16BIT,
//					Configuration.Shimmer3.SensorMapKey.EXG1_24BIT,
//					Configuration.Shimmer3.SensorMapKey.EXG2_24BIT,
					Configuration.Shimmer3.SensorMapKey.SHIMMER_RESISTANCE_AMP,
					Configuration.Shimmer3.SensorMapKey.SHIMMER_BRIDGE_AMP),
			Arrays.asList(Configuration.Shimmer3.GuiLabelConfig.GSR_RANGE),
			Arrays.asList(Configuration.Shimmer3.ObjectClusterSensorName.GSR),
			true);
	
    public static final Map<Integer, SensorDetailsRef> mSensorMapRef;
    static {
        Map<Integer, SensorDetailsRef> aMap = new LinkedHashMap<Integer, SensorDetailsRef>();
		aMap.put(Configuration.Shimmer3.SensorMapKey.SHIMMER_GSR, SensorGSR.sensorGsrRef);
		mSensorMapRef = Collections.unmodifiableMap(aMap);
    }
	//--------- Sensor info end --------------
    
	//--------- Channel info start --------------
	public static final ChannelDetails channelGsr = new ChannelDetails(
			Configuration.Shimmer3.ObjectClusterSensorName.GSR,
			Configuration.Shimmer3.ObjectClusterSensorName.GSR,
			DatabaseChannelHandles.GSR,
			CHANNEL_DATA_TYPE.UINT16, 2, CHANNEL_DATA_ENDIAN.LSB,
			CHANNEL_UNITS.KOHMS,
//			CHANNEL_UNITS.MICROSIEMENS,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
	{
		//TODO put below into constructor - not sure if it's possible to modify here because the channel is a static final
		channelGsr.mChannelSource = CHANNEL_SOURCE.SHIMMER;
		channelGsr.mDefaultUnit = CHANNEL_UNITS.NO_UNITS;
		channelGsr.mChannelFormatDerivedFromShimmerDataPacket = CHANNEL_TYPE.UNCAL;
	}
	
    public static final Map<String, ChannelDetails> mChannelMapRef;
    static {
        Map<String, ChannelDetails> aMap = new LinkedHashMap<String, ChannelDetails>();
		aMap.put(Configuration.Shimmer3.ObjectClusterSensorName.GSR, SensorGSR.channelGsr);
		mChannelMapRef = Collections.unmodifiableMap(aMap);
    }
	//--------- Channel info end --------------

	/** Constructor for this Sensor
	 * @param svo
	 */
	public SensorGSR(ShimmerVerObject svo) {
		super(svo);
		mSensorName = SENSORS.GSR.toString();
	}

	@Override
	public void generateSensorMap(ShimmerVerObject svo) {
		//TODO populate the other channels depending on firmware version
		super.createLocalSensorMapWithCustomParser(mSensorMapRef, mChannelMapRef);
	}


	@Override
	public void generateSensorGroupMapping(ShimmerVerObject svo) {
		if(svo.mHardwareVersion==HW_ID.SHIMMER_3 || svo.mHardwareVersion==HW_ID.SHIMMER_4_SDK){
			mSensorGroupingMap.put(Configuration.Shimmer3.GuiLabelSensorTiles.GSR, new SensorGroupingDetails(
					Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_GSR,
								Configuration.Shimmer3.SensorMapKey.HOST_PPG_DUMMY)));
			mSensorGroupingMap.get(Configuration.Shimmer3.GuiLabelSensorTiles.GSR).mListOfCompatibleVersionInfo = CompatibilityInfoForMaps.listOfCompatibleVersionInfoGsr;
		}
		else if((svo.mHardwareVersion==HW_ID.SHIMMER_GQ_802154_LR)
				||(svo.mHardwareVersion==HW_ID.SHIMMER_GQ_802154_NR)
				||(svo.mHardwareVersion==HW_ID.SHIMMER_2R_GQ)){
			mSensorGroupingMap.put(Configuration.Shimmer3.GuiLabelSensorTiles.GSR, new SensorGroupingDetails(
					Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_GSR)));
			mSensorGroupingMap.get(Configuration.Shimmer3.GuiLabelSensorTiles.GSR).mListOfCompatibleVersionInfo = CompatibilityInfoForMaps.listOfCompatibleVersionInfoGsr;
			
//			mSensorGroupingMap.get(Configuration.Shimmer3.GuiLabelSensorTiles.GSR).mListOfConfigOptionKeysAssociated.add(e)
		}
	}

	@Override
	public void generateConfigOptionsMap(ShimmerVerObject svo) {
		if (svo.mFirmwareIdentifier == ShimmerVerDetails.FW_ID.BTSTREAM 
				|| svo.mFirmwareIdentifier == ShimmerVerDetails.FW_ID.SDLOG
				|| svo.mFirmwareIdentifier == ShimmerVerDetails.FW_ID.GQ_802154) {
			mConfigOptionsMap.put(Configuration.Shimmer3.GuiLabelConfig.GSR_RANGE, configOptionGsrRange); 
		}
	}
	
	//TODO BIG BUG HERE, SHIMMERGQ IS USING INCORRECT CALIBRATION PARAMETERS BECAUSE HW_ID.SHIMMER3 is the only one listed!!!!!!!!
	
	//TODO: include somewhere (SensorDetails/ChannelDetails??)
	@Override
	public ObjectCluster processDataCustom(SensorDetails sensorDetails, byte[] sensorByteArray, COMMUNICATION_TYPE commType, ObjectCluster objectCluster) {
		int index = 0;
		for (ChannelDetails channelDetails:sensorDetails.mListOfChannels){
			//first process the data originating from the Shimmer sensor
			byte[] channelByteArray = new byte[channelDetails.mDefaultNumBytes];
			System.arraycopy(sensorByteArray, index, channelByteArray, 0, channelDetails.mDefaultNumBytes);
			objectCluster = SensorDetails.processShimmerChannelData(sensorByteArray, channelDetails, objectCluster);
			
			//next process other data
			if (channelDetails.mObjectClusterName.equals(Configuration.Shimmer3.ObjectClusterSensorName.GSR)){
//				ObjectCluster objectCluster = (ObjectCluster) object;
				double rawData = ((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.mPropertyCluster.get(channelDetails.mObjectClusterName), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
				int newGSRRange = -1; // initialized to -1 so it will only come into play if mGSRRange = 4  
				double p1=0,p2=0;
				if (mGSRRange==4){
					newGSRRange=(49152 & (int)rawData)>>14; 
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
				} else if (mGSRRange==3 || newGSRRange==3) {
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

				// ---------- Method 1 - ShimmerObject Style -----------
//				//kOhms
//				objectCluster.mCalData[objectCluster.indexKeeper] = calibrateGsrData(rawData,p1,p2);
//				objectCluster.mUnitCal[objectCluster.indexKeeper]=CHANNEL_UNITS.KOHMS;
//				objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.GSR,new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.KOHMS,objectCluster.mCalData[objectCluster.indexKeeper]));
//					
//				//uS
//				objectCluster.mCalData[objectCluster.indexKeeper] = calibrateGsrDataToSiemens(rawData,p1,p2);
//				objectCluster.mUnitCal[objectCluster.indexKeeper] = CHANNEL_UNITS.MICROSIEMENS;
//				objectCluster.mPropertyCluster.put(Shimmer3.ObjectClusterSensorName.GSR, new FormatCluster(CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MICROSIEMENS,objectCluster.mCalData[objectCluster.indexKeeper]));
//					objectCluster.indexKeeper++;

				
				// ---------- Method 2 - Simplified ShimmerObject Style -----------
				
//				objectCluster.addData(Shimmer3.ObjectClusterSensorName.GSR, CHANNEL_TYPE.UNCAL, CHANNEL_UNITS.NO_UNITS, rawData);
//				//kOhms
//				objectCluster.addData(Shimmer3.ObjectClusterSensorName.GSR, CHANNEL_TYPE.CAL, CHANNEL_UNITS.KOHMS, calibrateGsrData(rawData,p1,p2));
////				//uS
////				objectCluster.addData(Shimmer3.ObjectClusterSensorName.GSR, CHANNEL_TYPE.CAL, CHANNEL_UNITS.MICROSIEMENS, calibrateGsrDataToSiemens(rawData,p1,p2));
//				objectCluster.indexKeeper++;



				// ----- Method 3 - Approaching dynamic object based approach  -----------
				//TODO: Doesn't support having both units
				
//				if(channelDetails.mChannelFormatDerivedFromShimmerDataPacket!=CHANNEL_TYPE.CAL){
				double calData = 0.0;
				if(calUnitToUse.equals(Configuration.CHANNEL_UNITS.KOHMS)){
					calData = calibrateGsrData(rawData,p1,p2);
				}
				else if(calUnitToUse.equals(Configuration.CHANNEL_UNITS.U_SIEMENS)){
					calData = calibrateGsrDataToSiemens(rawData,p1,p2);
				}
				objectCluster.addCalData(channelDetails, calData);
				objectCluster.indexKeeper++;
//				}
//				System.err.println(String.format("%16s", Integer.toBinaryString((int) rawData)).replace(' ', '0') + "\t" + calData + " " + channelDetails.mDefaultCalibratedUnits);

			}
			index = index + channelDetails.mDefaultNumBytes;
		}
		return objectCluster;
	}
	
	public static double calibrateGsrData(double gsrUncalibratedData,double p1, double p2){
		gsrUncalibratedData = (double)((int)gsrUncalibratedData & 4095); 
		//the following polynomial is deprecated and has been replaced with a more accurate linear one, see GSR user guide for further details
		//double gsrCalibratedData = (p1*Math.pow(gsrUncalibratedData,4)+p2*Math.pow(gsrUncalibratedData,3)+p3*Math.pow(gsrUncalibratedData,2)+p4*gsrUncalibratedData+p5)/1000;
		//the following is the new linear method see user GSR user guide for further details
		double gsrCalibratedData = (1/((p1*gsrUncalibratedData)+p2)*1000); //kohms 
		return gsrCalibratedData;  
	}

	public static double calibrateGsrDataToSiemens(double gsrUncalibratedData,double p1, double p2){
		gsrUncalibratedData = (double)((int)gsrUncalibratedData & 4095); 
		//the following polynomial is deprecated and has been replaced with a more accurate linear one, see GSR user guide for further details
		//double gsrCalibratedData = (p1*Math.pow(gsrUncalibratedData,4)+p2*Math.pow(gsrUncalibratedData,3)+p3*Math.pow(gsrUncalibratedData,2)+p4*gsrUncalibratedData+p5)/1000;
		//the following is the new linear method see user GSR user guide for further details
		double gsrCalibratedData = (((p1*gsrUncalibratedData)+p2)); //microsiemens 
		return gsrCalibratedData;  
	}
	
	
	@Override
	public Object getSettings(String componentName, COMMUNICATION_TYPE commType) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public ActionSetting setSettings(String componentName, Object valueToSet, COMMUNICATION_TYPE commType) {
		
		// TODO Auto-generated method stub
		ActionSetting actionSetting = new ActionSetting(commType);
		switch(componentName){
			case(Configuration.Shimmer3.GuiLabelConfig.GSR_RANGE):
				if (commType == COMMUNICATION_TYPE.BLUETOOTH){
					
				} else if (commType == COMMUNICATION_TYPE.DOCK){
					
				} else if (commType == COMMUNICATION_TYPE.CLASS){
					//this generates the infomem
					
				}
			break;
		}
		return actionSetting;

	}

	@Override
	//TODO: Not sure yet whether store sensors infomem layout in Sensor class or in InfoMemLayout class
	public void infoMemByteArrayGenerate(ShimmerDevice shimmerDevice, byte[] mInfoMemBytes) {
		//TODO: tidy
		int idxConfigSetupByte3 =	9;
		int bitShiftGSRRange =		1;
		int maskGSRRange =			0x07;
		mInfoMemBytes[idxConfigSetupByte3] |= (byte) ((mGSRRange & maskGSRRange) << bitShiftGSRRange);
	}

	@Override
	public void infoMemByteArrayParse(ShimmerDevice shimmerDevice, byte[] mInfoMemBytes) {
		//TODO: tidy
		int idxConfigSetupByte3 =	9;
		int bitShiftGSRRange =		1;
		int maskGSRRange =			0x07;
		mGSRRange = (mInfoMemBytes[idxConfigSetupByte3] >> bitShiftGSRRange) & maskGSRRange;
	}

	@Override
	public Object setConfigValueUsingConfigLabel(String componentName, Object valueToSet) {
		Object returnValue = null;
		int buf = 0;

		switch(componentName){
			case(Configuration.Shimmer3.GuiLabelConfig.GSR_RANGE):
	    		setGSRRange((int)valueToSet);
				returnValue = valueToSet;
	        	break;
	        default:
	        	break;
		}

		
		// TODO Auto-generated method stub
		return returnValue;
	}

	@Override
	public Object getConfigValueUsingConfigLabel(String componentName) {
		Object returnValue = null;
		switch(componentName){
			case(Configuration.Shimmer3.GuiLabelConfig.GSR_RANGE):
				returnValue = getGSRRange(); //TODO: check with RM re firmware bug??
		    	break;
	        default:
	        	break;
		}

		return returnValue;
	}

	public int getGSRRange(){
		return mGSRRange;
	}

	public void setGSRRange(int i){
		mGSRRange = i;
	}

//	@Override
//	public List<String> generateListOfConfigOptionKeysAssociated(ShimmerVerObject svo) {
//		return mListOfConfigOptionKeysAssociated = Arrays.asList(
//				Configuration.Shimmer3.GuiLabelConfig.GSR_RANGE);
//		
//		
//	}
//
//	@Override
//	public List<Integer> generateListOfSensorMapKeysConflicting(ShimmerVerObject svo) {
//		return mListOfSensorMapKeysConflicting = Arrays.asList(
//				Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A1,
//				Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A14,
//				Configuration.Shimmer3.SensorMapKey.HOST_ECG,
//				Configuration.Shimmer3.SensorMapKey.HOST_EMG,
//				Configuration.Shimmer3.SensorMapKey.HOST_EXG_TEST,
//				Configuration.Shimmer3.SensorMapKey.HOST_EXG_CUSTOM,
//				Configuration.Shimmer3.SensorMapKey.HOST_EXG_RESPIRATION,
////				Configuration.Shimmer3.SensorMapKey.EXG1_16BIT,
////				Configuration.Shimmer3.SensorMapKey.EXG2_16BIT,
////				Configuration.Shimmer3.SensorMapKey.EXG1_24BIT,
////				Configuration.Shimmer3.SensorMapKey.EXG2_24BIT,
//				Configuration.Shimmer3.SensorMapKey.SHIMMER_RESISTANCE_AMP,
//				Configuration.Shimmer3.SensorMapKey.SHIMMER_BRIDGE_AMP);
//		
//	}

	@Override
	public void setSamplingRateFromFreq() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean setDefaultConfiguration(int sensorMapKey, boolean state) {
		if(mSensorMap.containsKey(sensorMapKey)){
			//TODO set defaults for particular sensor
			return true;
		}
		return false;
	}

	@Override
	public boolean checkConfigOptionValues(String stringKey) {
		// TODO Auto-generated method stub
		return false;
	}


}
