package com.shimmerresearch.sensors;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.shimmerresearch.bluetooth.BtCommandDetails;
import com.shimmerresearch.driver.Configuration.CHANNEL_UNITS;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driver.Configuration.Shimmer3.CompatibilityInfoForMaps;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driverUtilities.ChannelDetails;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_SOURCE;
import com.shimmerresearch.driverUtilities.ConfigOptionDetailsSensor;
import com.shimmerresearch.driverUtilities.SensorDetailsRef;
import com.shimmerresearch.driverUtilities.SensorDetails;
import com.shimmerresearch.driverUtilities.SensorGroupingDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_TYPE;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_ENDIAN;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_TYPE;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.FormatCluster;
import com.shimmerresearch.driver.ShimmerDevice;

public class SensorGSR extends AbstractSensor {

	private static final long serialVersionUID = 1773291747371088953L;

	//--------- Sensor specific variables start --------------
	/** Shimmer3 Values have been reverted to 2r values */
	public static boolean isShimmer3and4UsingShimmer2rVal = true;
	
	public int mGSRRange = 4; 					// 4 = Auto
	
	public class GuiLabelConfig{
		public static final String GSR_RANGE = "GSR Range";
		public static final String SAMPLING_RATE_DIVIDER_GSR = "GSR Divider";
	}
	
	public class GuiLabelSensors{
		public static final String GSR = "GSR";
	}
	
	public class GuiLabelSensorTiles{
		public static final String GSR = "GSR+";
	}
	
	public static class DatabaseChannelHandles{
		public static final String GSR = "F5437a_Int_A1_GSR";
	}
	
	public static final class DatabaseConfigHandle{
		public static final String GSR_RANGE = "F5437a_Int_A1_GSR_Range";
	}
	
	public static class ObjectClusterSensorName{
		public static String GSR = "GSR";
		public static String GSR_CONDUCTANCE = "GSR_Conductance";
		public static String GSR_RANGE_CURRENT = "GSR_Range";
		public static String GSR_ADC_VALUE = "GSR_ADC_Value";
	}	
	//--------- Sensor specific variables end --------------
	
	//--------- Bluetooth commands start --------------
	public static final byte SET_GSR_RANGE_COMMAND			   		= (byte) 0x21;
	public static final byte GSR_RANGE_RESPONSE			   			= (byte) 0x22;
	public static final byte GET_GSR_RANGE_COMMAND			   		= (byte) 0x23;
	
	  public static final Map<Byte, BtCommandDetails> mBtGetCommandMap;
	    static {
	        Map<Byte, BtCommandDetails> aMap = new LinkedHashMap<Byte, BtCommandDetails>();
	        aMap.put(GET_GSR_RANGE_COMMAND, new BtCommandDetails(GET_GSR_RANGE_COMMAND, "GET_GSR_RANGE_COMMAND", GSR_RANGE_RESPONSE));
	        mBtGetCommandMap = Collections.unmodifiableMap(aMap);
	    }
	    
	    public static final Map<Byte, BtCommandDetails> mBtSetCommandMap;
	    static {
	        Map<Byte, BtCommandDetails> aMap = new LinkedHashMap<Byte, BtCommandDetails>();
	        aMap.put(SET_GSR_RANGE_COMMAND, new BtCommandDetails(SET_GSR_RANGE_COMMAND, "SET_GSR_RANGE_COMMAND"));
	        mBtSetCommandMap = Collections.unmodifiableMap(aMap);
	    }
	//--------- Bluetooth commands end --------------

	//--------- Configuration options start --------------
	public static final String[] ListofGSRRange = {
		"10k\u2126 to 56k\u2126",
		"56k\u2126 to 220k\u2126",
		"220k\u2126 to 680k\u2126",
		"680k\u2126 to 4.7M\u2126",
		"Auto"};
	public static final Integer[] ListofGSRRangeConfigValues = {0,1,2,3,4};

	public static final ConfigOptionDetailsSensor configOptionGsrRange = new ConfigOptionDetailsSensor(
			ListofGSRRange, 
			ListofGSRRangeConfigValues, 
			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoGsr);
	//--------- Configuration options end --------------

	//--------- Sensor info start --------------
	public static final SensorDetailsRef sensorGsrRef = new SensorDetailsRef(
			0x04<<(0*8), 
			0x04<<(0*8), 
			GuiLabelSensors.GSR,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoGsr,
			Arrays.asList(
					Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A1,
					Configuration.Shimmer3.SensorMapKey.SHIMMER_INT_EXP_ADC_A14,
					Configuration.Shimmer3.SensorMapKey.HOST_ECG,
					Configuration.Shimmer3.SensorMapKey.HOST_EMG,
					Configuration.Shimmer3.SensorMapKey.HOST_EXG_TEST,
					Configuration.Shimmer3.SensorMapKey.HOST_EXG_CUSTOM,
					Configuration.Shimmer3.SensorMapKey.HOST_EXG_RESPIRATION,
					Configuration.Shimmer3.SensorMapKey.HOST_EXG_THREE_UNIPOLAR,
//					Configuration.Shimmer3.SensorMapKey.EXG1_16BIT,
//					Configuration.Shimmer3.SensorMapKey.EXG2_16BIT,
//					Configuration.Shimmer3.SensorMapKey.EXG1_24BIT,
//					Configuration.Shimmer3.SensorMapKey.EXG2_24BIT,
					Configuration.Shimmer3.SensorMapKey.SHIMMER_RESISTANCE_AMP,
					Configuration.Shimmer3.SensorMapKey.SHIMMER_BRIDGE_AMP),
			Arrays.asList(GuiLabelConfig.GSR_RANGE),
			Arrays.asList(
					//Comment in/out channel you want to appear as normal Shimmer channels
					ObjectClusterSensorName.GSR,
					ObjectClusterSensorName.GSR_CONDUCTANCE,
					//Only internal at the moment
					ObjectClusterSensorName.GSR_RANGE_CURRENT,
					//Only internal at the moment
					ObjectClusterSensorName.GSR_ADC_VALUE
					),
			true);
	
    public static final Map<Integer, SensorDetailsRef> mSensorMapRef;
    static {
        Map<Integer, SensorDetailsRef> aMap = new LinkedHashMap<Integer, SensorDetailsRef>();
		aMap.put(Configuration.Shimmer3.SensorMapKey.SHIMMER_GSR, SensorGSR.sensorGsrRef);
		mSensorMapRef = Collections.unmodifiableMap(aMap);
    }
	//--------- Sensor info end --------------
    
	//--------- Channel info start --------------
    
    //TODO only use one channel details for GSR and have a list of supported units inside it. Also have a variable stating which one(s) are currently selected
	public static final ChannelDetails channelGsrKOhms = new ChannelDetails(
			ObjectClusterSensorName.GSR,
			ObjectClusterSensorName.GSR,
			DatabaseChannelHandles.GSR,
			CHANNEL_DATA_TYPE.UINT16, 2, CHANNEL_DATA_ENDIAN.LSB,
			CHANNEL_UNITS.KOHMS,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL),
			0x1C);
	{
		//TODO add to constructor
		channelGsrKOhms.mChannelSource = CHANNEL_SOURCE.SHIMMER;
		channelGsrKOhms.mDefaultUncalUnit = CHANNEL_UNITS.NO_UNITS;
		channelGsrKOhms.mChannelFormatDerivedFromShimmerDataPacket = CHANNEL_TYPE.UNCAL;
	}

	public static final ChannelDetails channelGsrMicroSiemensGq = new ChannelDetails(
			ObjectClusterSensorName.GSR,
			ObjectClusterSensorName.GSR,
			DatabaseChannelHandles.GSR,
			CHANNEL_DATA_TYPE.UINT16, 2, CHANNEL_DATA_ENDIAN.LSB,
			CHANNEL_UNITS.U_SIEMENS,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL),
			0x1C);
	{
		//TODO add to constructor
		channelGsrMicroSiemens.mChannelSource = CHANNEL_SOURCE.SHIMMER;
		channelGsrMicroSiemens.mDefaultUncalUnit = CHANNEL_UNITS.NO_UNITS;
		channelGsrMicroSiemens.mChannelFormatDerivedFromShimmerDataPacket = CHANNEL_TYPE.UNCAL;
	}

	public static final ChannelDetails channelGsrMicroSiemens = new ChannelDetails(
			ObjectClusterSensorName.GSR_CONDUCTANCE,
			ObjectClusterSensorName.GSR_CONDUCTANCE,
			ObjectClusterSensorName.GSR_CONDUCTANCE,
			CHANNEL_UNITS.U_SIEMENS,
			Arrays.asList(CHANNEL_TYPE.CAL));
	{
		//TODO add to constructor
		channelGsrMicroSiemens.mChannelSource = CHANNEL_SOURCE.API;
	}

	public static final ChannelDetails channelGsrRangeCurrent = new ChannelDetails(
			ObjectClusterSensorName.GSR_RANGE_CURRENT,
			ObjectClusterSensorName.GSR_RANGE_CURRENT,
			CHANNEL_UNITS.NO_UNITS,
			Arrays.asList(CHANNEL_TYPE.CAL));
	{
		//TODO add to constructor
		channelGsrMicroSiemens.mChannelSource = CHANNEL_SOURCE.API;
	}
	
	public static final ChannelDetails channelGsrAdc = new ChannelDetails(
			ObjectClusterSensorName.GSR_ADC_VALUE,
			ObjectClusterSensorName.GSR_ADC_VALUE,
			DatabaseChannelHandles.GSR,
			CHANNEL_UNITS.MILLIVOLTS,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL));
	{
		//TODO add to constructor
		channelGsrMicroSiemens.mChannelSource = CHANNEL_SOURCE.API;
	}

	/** used for Shimmer3 in Shimmer*/
    public static final Map<String, ChannelDetails> mChannelMapRef;
    static {
        Map<String, ChannelDetails> aMap = new LinkedHashMap<String, ChannelDetails>();
		aMap.put(ObjectClusterSensorName.GSR, SensorGSR.channelGsrKOhms);
		aMap.put(ObjectClusterSensorName.GSR_CONDUCTANCE, SensorGSR.channelGsrMicroSiemens);
		aMap.put(ObjectClusterSensorName.GSR_RANGE_CURRENT, SensorGSR.channelGsrRangeCurrent);
		aMap.put(ObjectClusterSensorName.GSR_ADC_VALUE, SensorGSR.channelGsrAdc);
		mChannelMapRef = Collections.unmodifiableMap(aMap);
    }

    public static final Map<String, ChannelDetails> mChannelMapRefGq;
    static {
        Map<String, ChannelDetails> aMap = new LinkedHashMap<String, ChannelDetails>();
		aMap.put(ObjectClusterSensorName.GSR, SensorGSR.channelGsrMicroSiemensGq);
		mChannelMapRefGq = Collections.unmodifiableMap(aMap);
    }

	//--------- Channel info end --------------

    
	//--------- Constructors for this class start --------------
	/** Constructor for this Sensor
	 * @param svo
	 */
	public SensorGSR(ShimmerVerObject svo) {
		super(SENSORS.GSR, svo);
		initialise();
	}
	//--------- Constructors for this class end --------------

	
	//--------- Abstract methods implemented start --------------
	@Override
	public void generateSensorMap() {
		//Allow NeuroLynQ to just use a single GSR channel based on MicroSiemens
		if(mShimmerVerObject.isShimmerGenGq()){
			Map<String, ChannelDetails> channelMapRef = new LinkedHashMap<String, ChannelDetails>();
			channelMapRef.put(Configuration.Shimmer3.ObjectClusterSensorName.GSR, SensorGSR.channelGsrMicroSiemensGq);
			super.createLocalSensorMapWithCustomParser(mSensorMapRef, channelMapRef);
		}
		else{
			super.createLocalSensorMapWithCustomParser(mSensorMapRef, mChannelMapRef);
		}
	}

	
	@Override
	public void generateConfigOptionsMap() {
		mConfigOptionsMap.put(GuiLabelConfig.GSR_RANGE, configOptionGsrRange); 
	}

	
	@Override
	public void generateSensorGroupMapping() {
		
		int groupIndex = Configuration.Shimmer3.GuiLabelSensorTiles.GSR.ordinal();
		
		if(mShimmerVerObject.isShimmerGenGq()){
			mSensorGroupingMap.put(groupIndex, new SensorGroupingDetails(
					GuiLabelSensorTiles.GSR,
					Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_GSR),
					CompatibilityInfoForMaps.listOfCompatibleVersionInfoGsr));
		}
		else if((mShimmerVerObject.isShimmerGen3() || mShimmerVerObject.isShimmerGen4())){
			mSensorGroupingMap.put(groupIndex, new SensorGroupingDetails(
					GuiLabelSensorTiles.GSR,
					Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_GSR,
							//TODO PPG not working here because it's not contained within this sensor class
								Configuration.Shimmer3.SensorMapKey.HOST_PPG_DUMMY),
					CompatibilityInfoForMaps.listOfCompatibleVersionInfoGsr));
		}
		super.updateSensorGroupingMap();	
	}


	@Override
	public ObjectCluster processDataCustom(SensorDetails sensorDetails, byte[] sensorByteArray, COMMUNICATION_TYPE commType, ObjectCluster objectCluster, boolean isTimeSyncEnabled, long pcTimestamp) {
		int index = 0;
		for (ChannelDetails channelDetails:sensorDetails.mListOfChannels){
			//first process the data originating from the Shimmer sensor
			byte[] channelByteArray = new byte[channelDetails.mDefaultNumBytes];
			System.arraycopy(sensorByteArray, index, channelByteArray, 0, channelDetails.mDefaultNumBytes);
			objectCluster = SensorDetails.processShimmerChannelData(channelByteArray, channelDetails, objectCluster);
			
			//next process other data
			if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.GSR)){
//				ObjectCluster objectCluster = (ObjectCluster) object;
				double rawData = ((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(channelDetails.mObjectClusterName), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
				int newGSRRange = -1; // initialized to -1 so it will only come into play if mGSRRange = 4  
				double p1=0,p2=0;
				if (mGSRRange==4){
					newGSRRange=(49152 & (int)rawData)>>14; 
				}
				if (mGSRRange==0 || newGSRRange==0) { //Note that from FW 1.0 onwards the MSB of the GSR data contains the range
					// the polynomial function used for calibration has been deprecated, it is replaced with a linear function
					if (mShimmerVerObject.isShimmerGen2() || SensorGSR.isShimmer3and4UsingShimmer2rVal){
						p1 = 0.0373;
						p2 = -24.9915;
					} else {
						p1 = 0.0363;
						p2 = -24.8617;
					}
				} else if (mGSRRange==1 || newGSRRange==1) {
					if (mShimmerVerObject.isShimmerGen2() || SensorGSR.isShimmer3and4UsingShimmer2rVal){
						p1 = 0.0054;
						p2 = -3.5194;
					} else {
						p1 = 0.0051;
						p2 = -3.8357;
					}
				} else if (mGSRRange==2 || newGSRRange==2) {
					if (mShimmerVerObject.isShimmerGen2() || SensorGSR.isShimmer3and4UsingShimmer2rVal){
						p1 = 0.0015;
						p2 = -1.0163;
					} else {
						p1 = 0.0015;
						p2 = -1.0067;
					}
				} else if (mGSRRange==3 || newGSRRange==3) {
					if (mShimmerVerObject.isShimmerGen2() || SensorGSR.isShimmer3and4UsingShimmer2rVal){
						p1 = 4.5580e-04;
						p2 = -0.3014;
					} else {
						p1 = 4.4513e-04;
						p2 = -0.3193;
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
				
				double calData = 0.0;
				//This section is needed for GQ since the primary GSR KOHMS channel is replaced by U_SIEMENS 
				if(channelDetails.mDefaultCalUnits.equals(Configuration.CHANNEL_UNITS.KOHMS)){
					calData = calibrateGsrData(rawData,p1,p2);
				}
				else if(channelDetails.mDefaultCalUnits.equals(Configuration.CHANNEL_UNITS.U_SIEMENS)){
					calData = calibrateGsrDataToSiemens(rawData,p1,p2);
				}
				objectCluster.addCalData(channelDetails, calData);
				objectCluster.incrementIndexKeeper();

				
				if(sensorDetails.mListOfChannels.contains(channelGsrMicroSiemens)){
					double calDatauS = calibrateGsrDataToSiemens(rawData,p1,p2);
					objectCluster.addCalData(channelGsrMicroSiemens, calDatauS);
					objectCluster.incrementIndexKeeper();
				}
				if(sensorDetails.mListOfChannels.contains(channelGsrRangeCurrent)){
					objectCluster.addCalData(channelGsrRangeCurrent, newGSRRange);
					objectCluster.incrementIndexKeeper();
				}
				if(sensorDetails.mListOfChannels.contains(channelGsrAdc)){
					objectCluster.addUncalData(channelGsrAdc, rawData);
					objectCluster.addCalData(channelGsrAdc, SensorADC.calibrateMspAdcChannel(rawData));
					objectCluster.incrementIndexKeeper();
				}
				
			}
			index = index + channelDetails.mDefaultNumBytes;
			
			
			// All GSR channels are calculated after the
			// ObjectClusterSensorName.GSR channel is process (shown above) -
			// therefore no need to cycle through the rest of the channels.
			continue;
		}
		
		//Debugging
//		super.consolePrintChannelsCal(objectCluster, Arrays.asList(
//				new String[]{ObjectClusterSensorName.GSR_CONDUCTANCE, CHANNEL_TYPE.UNCAL.toString()},
//				new String[]{ObjectClusterSensorName.GSR, CHANNEL_TYPE.UNCAL.toString()})); 
		
		return objectCluster;
	}
	

	@Override
	public void configByteArrayGenerate(ShimmerDevice shimmerDevice, byte[] mInfoMemBytes) {
		int idxConfigSetupByte3 =	9;
		int bitShiftGSRRange =		1;
		int maskGSRRange =			0x07;
		
		mInfoMemBytes[idxConfigSetupByte3] |= (byte) ((mGSRRange & maskGSRRange) << bitShiftGSRRange);
	}

	
	@Override
	public void configByteArrayParse(ShimmerDevice shimmerDevice, byte[] mInfoMemBytes) {
		int idxConfigSetupByte3 =	9;
		int bitShiftGSRRange =		1;
		int maskGSRRange =			0x07;
		
		mGSRRange = (mInfoMemBytes[idxConfigSetupByte3] >> bitShiftGSRRange) & maskGSRRange;
	}
	
	
	@Override
	public Object setConfigValueUsingConfigLabel(Integer sensorMapKey, String configLabel, Object valueToSet) {
		Object returnValue = null;

		switch(configLabel){
			case(GuiLabelConfig.GSR_RANGE):
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
	public Object getConfigValueUsingConfigLabel(Integer sensorMapKey, String configLabel) {
		Object returnValue = null;
		switch(configLabel){
			case(GuiLabelConfig.GSR_RANGE):
				returnValue = getGSRRange(); //TODO: check with RM re firmware bug?? -> //RS (25/05/2016): Still relevant?
		    	break;
	        default:
	        	break;
		}

		return returnValue;
	}
	
	
	@Override
	public void setSensorSamplingRate(double samplingRateHz) {
		// TODO Auto-generated method stub
		
	}

	
	@Override
	public boolean setDefaultConfigForSensor(int sensorMapKey, boolean isSensorEnabled) {
		if(mSensorMap.containsKey(sensorMapKey)){
			setDefaultGsrSensorConfig(isSensorEnabled);
			return true;
		}
		return false;
	}
	

	@Override
	public boolean checkConfigOptionValues(String stringKey) {
		if(mConfigOptionsMap.containsKey(stringKey)){
			return true;
		}
		return false;
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
			case(GuiLabelConfig.GSR_RANGE):
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
	public LinkedHashMap<String, Object> getConfigMapForDb() {
		LinkedHashMap<String, Object> mapOfConfig = new LinkedHashMap<String, Object>();
		
		mapOfConfig.put(DatabaseConfigHandle.GSR_RANGE, getGSRRange());
		return mapOfConfig;
	}

	@Override
	public void parseConfigMapFromDb(LinkedHashMap<String, Object> mapOfConfigPerShimmer) {
		if(mapOfConfigPerShimmer.containsKey(DatabaseConfigHandle.GSR_RANGE)){
			setGSRRange(((Double) mapOfConfigPerShimmer.get(DatabaseConfigHandle.GSR_RANGE)).intValue());
		}
	}

	@Override
	public boolean processResponse(int responseCommand, Object parsedResponse, COMMUNICATION_TYPE commType) {
		// TODO Auto-generated method stub
		return false;
	}	
	
	@Override
	public void checkShimmerConfigBeforeConfiguring() {
		// If Shimmer name is default, update with MAC ID if available.
//		if(mShimmerUserAssignedName.equals(DEFAULT_SHIMMER_NAME)){
//			setDefaultShimmerName();
//		}
		if(!isSensorEnabled(Configuration.Shimmer3.SensorMapKey.SHIMMER_GSR)) {
			setDefaultGsrSensorConfig(false);
		}
	}
	//--------- Abstract methods implemented end --------------


	//--------- Sensor specific methods start --------------
	public static double calibrateGsrData(double gsrUncalibratedData, double p1, double p2){
//		gsrUncalibratedData = (double)((int)gsrUncalibratedData & 4095); 
//		//the following polynomial is deprecated and has been replaced with a more accurate linear one, see GSR user guide for further details
//		//double gsrCalibratedData = (p1*Math.pow(gsrUncalibratedData,4)+p2*Math.pow(gsrUncalibratedData,3)+p3*Math.pow(gsrUncalibratedData,2)+p4*gsrUncalibratedData+p5)/1000;
//		//the following is the new linear method see user GSR user guide for further details
//		double gsrCalibratedData = (1/((p1*gsrUncalibratedData)+p2)*1000); //kohms 
		
		double gsrCalibratedDatauS = calibrateGsrDataToSiemens(gsrUncalibratedData, p1, p2);
		double gsrCalibratedData = (1/(gsrCalibratedDatauS)*1000); //kohms 

		return gsrCalibratedData;  
	}

	
	public static double calibrateGsrDataToSiemens(double gsrUncalibratedData, double p1, double p2){
		double gsrUncalibratedDataLcl = (double)((int)gsrUncalibratedData & 4095); 
		//the following polynomial is deprecated and has been replaced with a more accurate linear one, see GSR user guide for further details
		//double gsrCalibratedData = (p1*Math.pow(gsrUncalibratedData,4)+p2*Math.pow(gsrUncalibratedData,3)+p3*Math.pow(gsrUncalibratedData,2)+p4*gsrUncalibratedData+p5)/1000;
		//the following is the new linear method see user GSR user guide for further details
		double gsrCalibratedData = (((p1*gsrUncalibratedDataLcl)+p2)); //microsiemens 
		return gsrCalibratedData;  
	}
	
	
	
	private void setDefaultGsrSensorConfig(boolean isSensorEnabled) {
		//RS (30/5/2016): from ShimmerObject
		if(isSensorEnabled) {
		}
		else{
			mGSRRange=4;
		}
	}
	
	
	public void setGSRRange(int valueToSet){
		mGSRRange = valueToSet;
	}
	
	
	public int getGSRRange(){
		return mGSRRange;
	}
	

	//--------- Sensor specific methods end --------------

	//--------- Optional methods to override in Sensor Class start --------
	//--------- Optional methods to override in Sensor Class end --------

}
	
