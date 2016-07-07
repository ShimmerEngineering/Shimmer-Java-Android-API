package com.shimmerresearch.sensors;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.FormatCluster;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driverUtilities.CalibDetails;
import com.shimmerresearch.driverUtilities.CalibDetailsKinematic;
import com.shimmerresearch.driverUtilities.ChannelDetails;
import com.shimmerresearch.driverUtilities.ConfigOptionDetailsSensor;
import com.shimmerresearch.driverUtilities.SensorDetailsRef;
import com.shimmerresearch.driverUtilities.SensorDetails;
import com.shimmerresearch.driverUtilities.SensorGroupingDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;

public abstract class AbstractSensor implements Serializable{
	
	/** * */
	private static final long serialVersionUID = 3465427544416038676L;

	public enum SENSORS{
		GSR("GSR"),
		ECG_TO_HR("ECG to Heart Rate"),
		EXG("EXG"),
		CLOCK("Shimmer Clock"),
		SYSTEM_TIMESTAMP("PC time"),
		MPU9X50("MPU Accel"),
		BMP180("BMP180"),
		KIONIXKXRB52042("Analog Accelerometer"),
		LSM303("LSM303"),
		PPG("PPG"), 
		TEMPLATE("Template sensor - not a real sensor of course!"),
		ADC("ADC"),
		Battery("Battery"),
		Bridge_Amplifier("Bridge Amplifier"),
		
		NONIN_ONYX_II("Nonin Onyx II"),
		QTI_DIRECT_TEMP("QTI DirectTemp"),
		BMP280("BMP280");
		
	    private final String text;

	    /** @param text */
	    private SENSORS(final String text) {
	        this.text = text;
	    }

	    /* (non-Javadoc)
	     * @see java.lang.Enum#toString()
	     */
	    @Override
	    public String toString() {
	        return text;
	    }
	}
	
	//General Configurations options that are normally used in conjunction with a sensorMapKey
	public class GuiLabelConfigCommon{
		public static final String RATE = "Rate";
		public static final String RANGE = "Range";
		public static final String CALIBRATION_PER_SENSOR = "Calibration";
		public static final String CALIBRATION_ALL = "Calibration all";
	}
	
	// --------------- Abstract methods start ----------------
	/** call either createLocalSensorMap() or createLocalSensorMapWithCustomParser() inside depending if a custom parser is needed. */
	public abstract void generateSensorMap(ShimmerVerObject svo);
	public abstract void generateConfigOptionsMap(ShimmerVerObject svo);
	public abstract void generateSensorGroupMapping(ShimmerVerObject svo);
	public abstract void checkShimmerConfigBeforeConfiguring();

	/** for use only if a custom parser is required, i.e. for calibrated data. Use in conjunction with createLocalSensorMapWithCustomParser()*/ 
	public abstract ObjectCluster processDataCustom(SensorDetails sensorDetails, byte[] rawData, COMMUNICATION_TYPE commType, ObjectCluster objectCluster, boolean isTimeSyncEnabled, long pctimeStamp);
//	public abstract ObjectCluster processDataCustom(SensorDetails sensorDetails, byte[] sensorByteArray, COMMUNICATION_TYPE commType, ObjectCluster objectCluster);
	
	public abstract void infoMemByteArrayGenerate(ShimmerDevice shimmerDevice, byte[] mInfoMemBytes);
	public abstract void infoMemByteArrayParse(ShimmerDevice shimmerDevice, byte[] mInfoMemBytes);
	public abstract Object setConfigValueUsingConfigLabel(Integer sensorMapKey, String configLabel, Object valueToSet);
	public abstract Object getConfigValueUsingConfigLabel(Integer sensorMapKey, String configLabel);

	public abstract void setSensorSamplingRate(double samplingRateHz);
	public abstract boolean setDefaultConfigForSensor(int sensorMapKey, boolean isSensorEnabled);
	/** TODO populate in individual AbstractSensor classes the relevant entries from ShimmerObject */
	public abstract boolean checkConfigOptionValues(String stringKey);

	public abstract Object getSettings(String componentName, COMMUNICATION_TYPE commType);
	public abstract ActionSetting setSettings(String componentName, Object valueToSet, COMMUNICATION_TYPE commType);
	
	public abstract void processResponse(Object obj, COMMUNICATION_TYPE commType);
	
	// --------------- Abstract methods end ----------------	

	protected String mSensorName = "";
	protected ShimmerVerObject mShimmerVerObject = new ShimmerVerObject();
	
	protected static boolean mEnableCalibration = true;
	protected boolean mIsDebugOutput = false;
	
	protected Double mMaxSetShimmerSamplingRate = 51.2;
	
	//TODO implement below?
//	protected int mFirmwareSensorIdentifier; // this is how the firmware identifies the sensor
	
	public TreeMap<Integer, SensorDetails> mSensorMap = new TreeMap<Integer, SensorDetails>();
	public HashMap<String,ConfigOptionDetailsSensor> mConfigOptionsMap = new HashMap<String,ConfigOptionDetailsSensor>();
    public LinkedHashMap<Integer, SensorGroupingDetails> mSensorGroupingMap = new LinkedHashMap<Integer, SensorGroupingDetails>();
	public TreeMap<Integer, TreeMap<Integer, CalibDetails>> mCalibMap = new TreeMap<Integer, TreeMap<Integer, CalibDetails>>(); 

    protected ShimmerDevice mShimmerDevice = null;
    
	
	public AbstractSensor(SENSORS sensorType, ShimmerVerObject svo){
		setSensorName(sensorType.toString());
		if(svo!=null){
			mShimmerVerObject = svo;
			generateSensorMap(svo);
			generateConfigOptionsMap(svo);
			generateSensorGroupMapping(svo);
		}
	}

	public AbstractSensor(SENSORS sensorType, ShimmerDevice shimmerDevice) {
		this(sensorType, shimmerDevice.getShimmerVerObject());
		this.mShimmerDevice  = shimmerDevice;
	}
	
	public HashMap<String, ConfigOptionDetailsSensor> getConfigMap() {
		return mConfigOptionsMap;
	}
	
	public void setSensorName(String sensorName) {
		mSensorName = sensorName;
	}

	public String getSensorName() {
		return mSensorName;
	}
	
	public int getNumberOfEnabledChannels(COMMUNICATION_TYPE commType){
		int count = 0;
		Iterator<SensorDetails> iterator = mSensorMap.values().iterator();
		while(iterator.hasNext()){
			SensorDetails sensorEnabledDetails = iterator.next();
			if (sensorEnabledDetails.isEnabled(commType)){
				count += sensorEnabledDetails.mListOfChannels.size();
			}
		}
		return count;
	}

	public boolean isAnySensorChannelEnabled(COMMUNICATION_TYPE commType){
		return (getNumberOfEnabledChannels(commType)>0? true:false);
	}

	public void setIsEnabledSensor(COMMUNICATION_TYPE commType, boolean state){
		Iterator<SensorDetails> iterator = mSensorMap.values().iterator();
		while(iterator.hasNext()){
			SensorDetails sensorDetails = iterator.next();
			sensorDetails.setIsEnabled(commType, state);
		}
	}
	
	public boolean setIsEnabledSensor(COMMUNICATION_TYPE commType, boolean state, int sensorMapKey){
		SensorDetails sensorDetails = mSensorMap.get(sensorMapKey);
		if(sensorDetails!=null){
			sensorDetails.setIsEnabled(commType, state);
			return true;
		}
		return false;
	}
	
	@Deprecated //?? not sure whether it's be to do this here or in ShimmerDevice
	public void updateStateFromEnabledSensorsVars(COMMUNICATION_TYPE commType, long enabledSensors, long derivedSensors) {
		for(SensorDetails sensorDetails:mSensorMap.values()){
			//check as to whether the sensor originates from the Shimmer data packet or the API
//			if(sensorDetails.isApiSensor()){
//				continue;
//			}
			
			sensorDetails.updateFromEnabledSensorsVars(commType, enabledSensors, derivedSensors);
			
//			boolean state = false;
//			if(sensorDetails.isDerivedChannel()){
//				//TODO check enabledSensors as well if required???
//				state = (derivedSensors & sensorDetails.mDerivedSensorBitmapID)>0? true:false;
//			}
//			else {
//				state = (enabledSensors & sensorDetails.mSensorDetailsRef.mSensorBitmapIDStreaming)>0? true:false;
//			}
//			sensorDetails.setIsEnabled(commType, state);
		}
		
//		//TODO: enabledSensors should be directed at channels coming from the Shimmer, derivedSensors at channels from the API 
//		//TODO move to abstract or override in the extended sensor classes so complexities like EXG can be handled
//		mIsEnabled = false;
//		boolean state = (enabledSensors & mSensorBitmapIDStreaming)>0? true:false;
//		mIsEnabled = state;
//		setSensorChannelsState(commType, state);
	}
	
	
	public void updateSensorGroupingMap() {
		for(SensorGroupingDetails sensorGroup:mSensorGroupingMap.values()) {
			// Ok to clear here because variable is initiated in the class
			sensorGroup.mListOfConfigOptionKeysAssociated = new ArrayList<String>();
			for (Integer sensorKey:sensorGroup.mListOfSensorMapKeysAssociated) {
				SensorDetails sensorDetails = mSensorMap.get(sensorKey);
				if(sensorDetails!=null && sensorDetails.mSensorDetailsRef!=null && sensorDetails.mSensorDetailsRef.mListOfConfigOptionKeysAssociated!=null){
					for (String configOption:sensorDetails.mSensorDetailsRef.mListOfConfigOptionKeysAssociated) {
						// do not add duplicates
						if (!(sensorGroup.mListOfConfigOptionKeysAssociated.contains(configOption))) {
							sensorGroup.mListOfConfigOptionKeysAssociated.add(configOption);
						}
					}
				}
				
				//TODO handle mListOfCompatibleVersionInfo here?
//				sensorGroup.mListOfCompatibleVersionInfo = listOfCompatibleVersionInfo;
			}
		}
	}

	public String getSensorGuiFriendlyLabel(int sensorMapKey) {
		SensorDetails sensorDetails = mSensorMap.get(sensorMapKey);
		if(sensorDetails!=null){
			return sensorDetails.mSensorDetailsRef.mGuiFriendlyLabel;
		}
		return null;
	}
	
	public SensorDetails getSensorDetails(int sensorMapKey){
		SensorDetails sensorDetails = mSensorMap.get(sensorMapKey);
		return sensorDetails;
	}

	public List<ShimmerVerObject> getSensorListOfCompatibleVersionInfo(int sensorMapKey) {
		SensorDetails sensorDetails = mSensorMap.get(sensorMapKey);
		if(sensorDetails!=null){
			return sensorDetails.mSensorDetailsRef.mListOfCompatibleVersionInfo;
		}
		return null;
	}
	
//	public void createLocalCalibMap(Map<Integer, CalibDetailsKinematic> sensorMapRef, Map<String, ChannelDetails> channelMapRef){
//		mCalibMap = new TreeMap<Integer, TreeMap<Integer, CalibDetailsKinematic>>();
//		
//		
//	}
	
	
	public void createLocalSensorMap(Map<Integer, SensorDetailsRef> sensorMapRef, Map<String, ChannelDetails> channelMapRef) {
		mSensorMap = new TreeMap<Integer, SensorDetails>();
		for(int sensorMapKey:sensorMapRef.keySet()){
			SensorDetailsRef sensorDetailsRef = sensorMapRef.get(sensorMapKey);
			SensorDetails sensorDetails = new SensorDetails(false, 0, sensorDetailsRef);
			updateSensorDetailsWithChannels(sensorDetails, channelMapRef);
			mSensorMap.put(sensorMapKey, sensorDetails);
		}
	}
	
	public void createLocalSensorMapWithCustomParser(Map<Integer, SensorDetailsRef> sensorMapRef, Map<String, ChannelDetails> channelMapRef) {
		mSensorMap = new TreeMap<Integer, SensorDetails>();
		for(int sensorMapKey:sensorMapRef.keySet()){
			SensorDetailsRef sensorDetailsRef = sensorMapRef.get(sensorMapKey);
			SensorDetails sensorDetails = new SensorDetails(false, 0, sensorDetailsRef){
				@Override
				public ObjectCluster processData(byte[] rawData, COMMUNICATION_TYPE commType, ObjectCluster object, boolean isTimeSyncEnabled, long pcTimestamp) {
//					System.out.println("PARSING\t" + this.mSensorDetails.mGuiFriendlyLabel);
					return processDataCustom(this, rawData, commType, object, isTimeSyncEnabled, pcTimestamp);
//					return super.processData(rawData, commType, object);
				}
			};
			updateSensorDetailsWithChannels(sensorDetails, channelMapRef);
			mSensorMap.put(sensorMapKey, sensorDetails);
		}
	}

	public void updateSensorDetailsWithChannels(SensorDetails sensorDetails, Map<String, ChannelDetails> channelMapRef){
		List<String> listOfChannelsRef = sensorDetails.mSensorDetailsRef.mListOfChannelsRef;
		for(String channelKey:listOfChannelsRef){
			ChannelDetails channelDetails = channelMapRef.get(channelKey);
			if(channelDetails!=null){
				sensorDetails.mListOfChannels.add(channelDetails);
			}
		}
	}
	
	public HashMap<String, ConfigOptionDetailsSensor> getConfigOptionsMap() {
		return mConfigOptionsMap;
	}
	
	public LinkedHashMap<Integer, SensorGroupingDetails> getSensorGroupingMap() {
		return mSensorGroupingMap;
	}

	public boolean isSensorEnabled(int sensorMapKey){
		if(mSensorMap!=null) {
			SensorDetails sensorDetails = mSensorMap.get(sensorMapKey);
			if(sensorDetails!=null){
				return sensorDetails.isEnabled();
			}
		}		
		return false;
	}
	
	public void setSamplingRateFromShimmer(double maxSetRate) {
		mMaxSetShimmerSamplingRate = maxSetRate;
		setSensorSamplingRate(mMaxSetShimmerSamplingRate);
	}
	
	public int getHardwareVersion() {
		return mShimmerVerObject.mHardwareVersion;
	}
	
	public void updateSensorDetailsWithCommsTypes(List<COMMUNICATION_TYPE> listOfSupportedCommsTypes) {
		for(SensorDetails sensorDetails:mSensorMap.values()){
			sensorDetails.updateSensorDetailsWithCommsTypes(listOfSupportedCommsTypes);
		}
	}
	
	public Object setConfigValueUsingConfigLabel(String configLabel, Object valueToSet){
		return setConfigValueUsingConfigLabel(Configuration.Shimmer3.SensorMapKey.RESERVED_ANY_SENSOR, configLabel, valueToSet);
	}
	
	public Object getConfigValueUsingConfigLabel(String configLabel){
		return getConfigValueUsingConfigLabel(Configuration.Shimmer3.SensorMapKey.RESERVED_ANY_SENSOR, configLabel);
	}

	/** Quickly implemented method to print channel data to the console
	 * @param objectCluster 
	 * @param listOfChannelOCNAndType a list of channel objectClusterNames to print to the console
	 */
	public void consolePrintChannelsCal(ObjectCluster objectCluster, List<String[]> listOfChannelOCNAndType) {
		String textToPrint = "";
		for(String[] channelOCN:listOfChannelOCNAndType){
			textToPrint += channelOCN[0] + "_" + channelOCN[1] + ":";
			FormatCluster formatCluster = (FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(channelOCN[0]), channelOCN[1]);
			if(formatCluster==null){
				textToPrint += "null";
			}
			else {
				double tempCal = formatCluster.mData;
				textToPrint += tempCal;
			}
			textToPrint += "\t";
		}
		//TODO use the consolePrintLn system 
		System.out.println(textToPrint);
	}
	
	protected void setCalibration(TreeMap<Integer, TreeMap<Integer, CalibDetails>> mapOfSensorCalibration) {
		for(Integer sensorMapKey:mSensorMap.keySet()){
			TreeMap<Integer, CalibDetails> mapOfCalibPerSensor = mapOfSensorCalibration.get(sensorMapKey);
			if(mapOfCalibPerSensor!=null){
				mCalibMap.put(sensorMapKey, mapOfCalibPerSensor);
			}
		}
	}
	
	protected TreeMap<Integer, CalibDetails> getCalibrationMapForSensor(Integer sensorMapKey) {
		TreeMap<Integer, CalibDetails> calibDetailsPerSensor = mCalibMap.get(sensorMapKey); 
		return calibDetailsPerSensor;
	}
	

	
	//--------- Optional methods to override in Sensor Class start --------
	/**
	 * Checks a specific sensor class to see if it is using it's default
	 * calibration parameters. Used, for example, in SensorLSM, SensorMPU and
	 * SensorKionix
	 * 
	 * @param sensorMapKey
	 * @return boolean indicating that the sensor is using default calibration
	 *         parameters.
	 */
	public boolean isSensorUsingDefaultCal(int sensorMapKey) {
		return false;
	}

	public void handleSpecialCasesAfterSensorMapCreate() {
    	//NOT USED IN THIS CLASS
    	//USED in {SensorEXG}
	}
	
	public void handleSpecCasesBeforeSensorMapUpdateGeneral(ShimmerDevice shimmerDevice) {
    	//NOT USED IN THIS CLASS
    	//USED in {SensorEXG}
	}

    public boolean handleSpecCasesBeforeSensorMapUpdatePerSensor(ShimmerDevice shimmerDevice, Integer sensorMapKey){
    	//NOT USED IN THIS CLASS
    	//USED in {SensorPPG, SensorEXG}
    	return false;
    }
    
    
	public void handleSpecCasesAfterSensorMapUpdateFromEnabledSensors() {
		//NOT IN THIS CLASS
		//USED in {SensorPPG, SensorEXG}
	}

	
	public void handleSpecCasesUpdateEnabledSensors() {
		//TODO Auto-generated method stub
		//USED in {SensorEXG}
	}
	
	public int handleSpecCasesBeforeSetSensorState(int sensorMapKey, boolean state) {
		//NOT USED IN THIS CLASS
    	//USED in {SensorPPG}	
		return sensorMapKey;
	}
	
	public byte[] generateAllCalibByteArray() {
		if(mCalibMap.isEmpty()){
			return null;
		}
		
		byte[] calibBytesAllPerSensor = new byte[]{};
		for(Integer sensorMapKey:mCalibMap.keySet()){
			TreeMap<Integer, CalibDetails> calMapPerSensor = mCalibMap.get(sensorMapKey);
//			SensorDetails sensorDetais = mSensorMap.get(sensorMapKey);
//			Integer calibSensorKey = sensorDetais.mSensorDetailsRef.mCalibSensorKey;
//			if(sensorMapKey!=0){
				for(Integer range:calMapPerSensor.keySet()){
					byte[] calibSensorKeyBytes = new byte[2];
					calibSensorKeyBytes[0] = (byte)((sensorMapKey>>0)&0xFF);
					calibSensorKeyBytes[1] = (byte)((sensorMapKey>>8)&0xFF);
					
					CalibDetails calDetailsPerRange = calMapPerSensor.get(range);		
					byte[] calibBytesPerRange = calDetailsPerRange.generateCalParamByteArrayWithTimestamp();

					//Create a new calibration param array
					byte[] calibBytesPacketPerRange = new byte[calibSensorKeyBytes.length+calibBytesPerRange.length];
					System.arraycopy(calibSensorKeyBytes, 0, calibBytesPacketPerRange, 0, calibSensorKeyBytes.length);
					System.arraycopy(calibBytesPerRange, 0, calibBytesPacketPerRange, calibSensorKeyBytes.length, calibBytesPerRange.length);
					
					//Copy new calib param array to end of exisiting array
					byte[] newCalibBytesAllPerSensor = new byte[calibBytesAllPerSensor.length+calibBytesPacketPerRange.length];
					System.arraycopy(calibBytesAllPerSensor, 0, newCalibBytesAllPerSensor, 0, calibBytesAllPerSensor.length);
					System.arraycopy(calibBytesPacketPerRange, 0, newCalibBytesAllPerSensor, calibBytesAllPerSensor.length, calibBytesPacketPerRange.length);
					calibBytesAllPerSensor = newCalibBytesAllPerSensor;
				}
//			}
		}
		return calibBytesAllPerSensor;
	}
	
	public byte[] parseAllCalibByteArray(byte[] remainingBytes) {
		// TODO Auto-generated method stub
		return remainingBytes;
	}
	
	public Object setConfigValueUsingConfigLabelCommon(Integer sensorMapKey, String configLabel, Object valueToSet) {
		Object returnValue = null;
		switch(configLabel){
			case(GuiLabelConfigCommon.CALIBRATION_ALL):
				setCalibration((TreeMap<Integer, TreeMap<Integer, CalibDetails>>) valueToSet);
				//TODO decide whether to include the below
//				returnValue = valueToSet;
				break;
	        default:
	        	break;
		}
		return returnValue;
	}
	public Object getConfigValueUsingConfigLabelCommon(Integer sensorMapKey, String configLabel) {
		Object returnValue = null;
		switch(configLabel){
			case(GuiLabelConfigCommon.CALIBRATION_PER_SENSOR):
				returnValue = getCalibrationMapForSensor(sensorMapKey);
				break;
			case(GuiLabelConfigCommon.CALIBRATION_ALL):
				returnValue = mCalibMap;
	        	break;
			default:
				break;
		}
		return returnValue;
	}

	
	
	//--------- Optional methods to override in Sensor Class end -------- 

}
