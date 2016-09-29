package com.shimmerresearch.algorithms;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.vecmath.Vector3d;

import com.shimmerresearch.algorithms.AbstractAlgorithm;
import com.shimmerresearch.algorithms.AlgorithmDetails;
import com.shimmerresearch.algorithms.AlgorithmResultObject;
import com.shimmerresearch.algorithms.AbstractAlgorithm.ALGORITHM_RESULT_TYPE;
import com.shimmerresearch.algorithms.AbstractAlgorithm.ALGORITHM_TYPE;
import com.shimmerresearch.algorithms.AbstractAlgorithm.FILTERING_OPTION;
import com.shimmerresearch.algorithms.AbstractAlgorithm.GuiLabelConfigCommon;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.FormatCluster;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.ShimmerMsg;
import com.shimmerresearch.driver.Configuration.CHANNEL_UNITS;
import com.shimmerresearch.driver.Configuration.Shimmer3;
import com.shimmerresearch.driver.ShimmerObject.DerivedSensorsBitMask;
import com.shimmerresearch.driverUtilities.ChannelDetails;
import com.shimmerresearch.driverUtilities.ExpansionBoardDetails;
import com.shimmerresearch.driverUtilities.SensorGroupingDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_TYPE;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.HW_ID;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.HW_ID_SR_CODES;

public class ActivityAlgorithmModule extends AbstractAlgorithm {

	private static final long serialVersionUID = 1L;
	
	// --------- Sensor specific variables start --------------
	private transient ActivityProcessing mActivityProcessing;
	double mSamplingRate;
	
	private Vector3d accValues;
	
	public static class ObjectClusterSensorName{
		public static  String ACTIVITY = "Activity";
		public static  String ACTIVITY_INTENSITY = "Intensity";
		public static  String ACTIVITY_SHORT = "Period_Short";
		public static  String ACTIVITY_MEDIUM = "Period_Medium";
		public static  String ACTIVITY_LONG = "Period_Long";
		public static  String ACTIVITY_STEP_COUNT = "Step_Count";
		public static  String ACTIVITY_PERCENTAGE_SEDENTARY = "Percentage_Sedentary";
		public static  String ACTIVITY_PERCENTAGE_ACTVIVE = "Percentage_Active";
	}
	
	// --------------- copied from Configuration.Shimmer3 start -----------
	public class GuiLabelConfig {
		public static final String ACTIVITY_SIGNAL_NAME = "Activity Signal Name";
		public static final String ACTIVITY_SIGNAL_FORMAT = "Activity Signal Format";
	}
	
	private static final ShimmerVerObject svoSh3Module = new ShimmerVerObject(
			HW_ID.SHIMMER_3,ShimmerVerDetails.ANY_VERSION,
			ShimmerVerDetails.ANY_VERSION,
			ShimmerVerDetails.ANY_VERSION,
			ShimmerVerDetails.ANY_VERSION,
			HW_ID_SR_CODES.EXP_BRD_EXG);
	
	// --------- Channel maps start --------------
	
	private static final ChannelDetails channelActivityIntensity = new ChannelDetails(
			ObjectClusterSensorName.ACTIVITY_INTENSITY, 
			ObjectClusterSensorName.ACTIVITY_INTENSITY, 
			CHANNEL_UNITS.NO_UNITS, 
			Arrays.asList(CHANNEL_TYPE.CAL));
	
	private static final ChannelDetails channelActivityPeriodShort = new ChannelDetails(
			ObjectClusterSensorName.ACTIVITY_SHORT, 
			ObjectClusterSensorName.ACTIVITY_SHORT, 
			CHANNEL_UNITS.NO_UNITS, 
			Arrays.asList(CHANNEL_TYPE.CAL));
	
	private static final ChannelDetails channelActivityPeriodMedium = new ChannelDetails(
			ObjectClusterSensorName.ACTIVITY_MEDIUM, 
			ObjectClusterSensorName.ACTIVITY_MEDIUM, 
			CHANNEL_UNITS.NO_UNITS, 
			Arrays.asList(CHANNEL_TYPE.CAL));
	
	private static final ChannelDetails channelActivityPeriodLong = new ChannelDetails(
			ObjectClusterSensorName.ACTIVITY_LONG, 
			ObjectClusterSensorName.ACTIVITY_LONG, 
			CHANNEL_UNITS.NO_UNITS, 
			Arrays.asList(CHANNEL_TYPE.CAL));
	
	private static final ChannelDetails channelActivityStepCount = new ChannelDetails(
			ObjectClusterSensorName.ACTIVITY_STEP_COUNT, 
			ObjectClusterSensorName.ACTIVITY_STEP_COUNT, 
			CHANNEL_UNITS.NO_UNITS, 
			Arrays.asList(CHANNEL_TYPE.CAL));
	
	private static final ChannelDetails channelActivitySendentaryActive = new ChannelDetails(
			ObjectClusterSensorName.ACTIVITY_PERCENTAGE_SEDENTARY,
			ObjectClusterSensorName.ACTIVITY_PERCENTAGE_SEDENTARY,
			CHANNEL_UNITS.NO_UNITS, 
			Arrays.asList(CHANNEL_TYPE.CAL));
	
	private static final ChannelDetails channelActivityPercentageActive = new ChannelDetails(
			ObjectClusterSensorName.ACTIVITY_PERCENTAGE_ACTVIVE,
			ObjectClusterSensorName.ACTIVITY_PERCENTAGE_ACTVIVE,
			CHANNEL_UNITS.NO_UNITS, 
			Arrays.asList(CHANNEL_TYPE.CAL));
	
	public static final Map<String, ChannelDetails> mChannelMapActivity;
	static {
		Map<String, ChannelDetails> aMap = new LinkedHashMap<String, ChannelDetails>();
		aMap.put(ObjectClusterSensorName.ACTIVITY_INTENSITY, channelActivityIntensity);
		aMap.put(ObjectClusterSensorName.ACTIVITY_SHORT, channelActivityPeriodShort);
		aMap.put(ObjectClusterSensorName.ACTIVITY_MEDIUM, channelActivityPeriodMedium);
		aMap.put(ObjectClusterSensorName.ACTIVITY_LONG, channelActivityPeriodLong);
		aMap.put(ObjectClusterSensorName.ACTIVITY_STEP_COUNT, channelActivityStepCount);
		aMap.put(ObjectClusterSensorName.ACTIVITY_PERCENTAGE_SEDENTARY, channelActivitySendentaryActive);
		aMap.put(ObjectClusterSensorName.ACTIVITY_PERCENTAGE_ACTVIVE, channelActivityPercentageActive);
		
		mChannelMapActivity = Collections.unmodifiableMap(aMap);
	}
	
	
	// --------- Algorithm maps start --------------
	
	public static final AlgorithmDetails algoActivity = new AlgorithmDetails(
			ObjectClusterSensorName.ACTIVITY, 
			ObjectClusterSensorName.ACTIVITY, 
			Arrays.asList(Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_WR_X,
					Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_WR_Y,
					Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_WR_Z),
			DerivedSensorsBitMask.EMG_PROCESSING_CHAN1,
			Arrays.asList(Configuration.Shimmer3.SensorMapKey.SHIMMER_LSM303DLHC_ACCEL), 
			CHANNEL_UNITS.NO_UNITS,
			Arrays.asList(ActivityAlgorithmModule.channelActivityIntensity,
					ActivityAlgorithmModule.channelActivityStepCount,
					ActivityAlgorithmModule.channelActivitySendentaryActive,
					ActivityAlgorithmModule.channelActivityPercentageActive));
	
	
	public static final Map<String, AlgorithmDetails> mAlgorithmMapRef;
	static {
		Map<String, AlgorithmDetails> aMap = new LinkedHashMap<String, AlgorithmDetails>();
		aMap.put(ActivityAlgorithmModule.algoActivity.mAlgorithmName, ActivityAlgorithmModule.algoActivity);
		mAlgorithmMapRef = Collections.unmodifiableMap(aMap);
	}
	
	// ------------------- Algorithms grouping map start -----------------------
	private static final SensorGroupingDetails sGDActivity = new SensorGroupingDetails(
			Configuration.Shimmer3.GuiLabelAlgorithmGrouping.ACTIVITY.getTileText(), 
			Arrays.asList(ActivityAlgorithmModule.algoActivity),
			null,
			0);
	
	
	@Override
	public void setGeneralAlgorithmName() {
		// TODO Auto-generated method stub
		mAlgorithmName = "ActivityModule";
	}

	@Override
	public void setFilteringOption() {
		// TODO Auto-generated method stub
		mFilteringOptions = FILTERING_OPTION.DEFAULT;
	}

	@Override
	public void setMinSamplingRateForAlgorithm() {
		// TODO Auto-generated method stub
		mMinSamplingRateForAlgorithhm = 51.2;
	}

	@Override
	public void setSupportedVerInfo() {
		// TODO Auto-generated method stub
		mListOfCompatibleSVO.add(svoSh3Module);
	}

	@Override
	public void generateConfigOptionsMap() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void generateAlgorithmGroupingMap() {
		// TODO Auto-generated method stub
		mMapOfAlgorithmGrouping.put(Configuration.Shimmer3.GuiLabelAlgorithmGrouping.ACTIVITY.ordinal(), sGDActivity);
	}

	// --------- Constructors for this class start --------------

	public ActivityAlgorithmModule() {
		super();
	}
	
	public ActivityAlgorithmModule(AlgorithmDetails algorithmDetails) {
		
		super(algorithmDetails);
		setSignalFormat(CHANNEL_TYPE.CAL.toString());
		mAlgorithmType = ALGORITHM_TYPE.ALGORITHM_TYPE_CONTINUOUS;
		mAlgorithmResultType = ALGORITHM_RESULT_TYPE.ALGORITHM_RESULT_TYPE_SINGLE_OBJECT_CLUSTER;
	}
	
	@Override
	public void initialize() throws Exception {
		// TODO Auto-generated method stub
		System.out.print("Activity Initializing");
		mActivityProcessing = new ActivityProcessing(mSamplingRate);
		
		try {
		} catch (Exception exc) {
			// TODO Auto-generated catch block
			System.err.println(mAlgorithmName + "\tERROR\t" + "configured sampling rate=" + getSamplingRate() + "\tmin=" + mMinSamplingRateForAlgorithhm);
			exc.printStackTrace();
		}

		mInitialized = true;
	}

	@Override
	public void reset() throws Exception {
		// TODO Auto-generated method stub
	}

	@Override
	public Object getSettings(String componentName) {
		// TODO Auto-generated method stub
		Object returnValue = null;
		switch(componentName){

			case(GuiLabelConfigCommon.SAMPLING_RATE):
				returnValue = getSamplingRate();
				break;
			case(GuiLabelConfig.ACTIVITY_SIGNAL_NAME):
				returnValue = getSignalName();
				break;
			case(GuiLabelConfig.ACTIVITY_SIGNAL_FORMAT):
				returnValue = getSignalFormat();
				break;
			case(GuiLabelConfigCommon.TIMESTAMP_SIGNAL_NAME):
				returnValue = getTimeStampName();
				break;
			case(GuiLabelConfigCommon.TIMESTAMP_SIGNAL_FORMAT):
				returnValue = getTimeStampFormat();
				break;
		}
		return returnValue;
	}

	@Override
	public void setSettings(String componentName, Object valueToSet) {
		// TODO Auto-generated method stub

		switch (componentName) {
		// Booleans
		case (GuiLabelConfigCommon.SAMPLING_RATE):
			if (valueToSet instanceof String) {
				if (!((String) valueToSet).isEmpty()) {
					setSamplingRate(Double.parseDouble((String) valueToSet));
				}
			}
			break;
		}
	}

	@Override
	public Object getDefaultSettings(String componentName) {
		// TODO Auto-generated method stub
		Object returnValue = null;
		switch(componentName){

			case(GuiLabelConfigCommon.SAMPLING_RATE):
				returnValue = String.valueOf(mMinSamplingRateForAlgorithhm);
				break;
			case(GuiLabelConfig.ACTIVITY_SIGNAL_NAME):
				returnValue = Shimmer3.ObjectClusterSensorName.EMG_CH1_24BIT;
				break;
			case(GuiLabelConfig.ACTIVITY_SIGNAL_FORMAT):
				returnValue = CHANNEL_TYPE.CAL.toString();
				break;
			case(GuiLabelConfigCommon.TIMESTAMP_SIGNAL_NAME):
				returnValue = Shimmer3.ObjectClusterSensorName.TIMESTAMP;
				break;
			case(GuiLabelConfigCommon.TIMESTAMP_SIGNAL_FORMAT):
				returnValue = CHANNEL_TYPE.CAL.toString();
				break;
		}
		return returnValue;
	}

	@Override
	public AlgorithmResultObject processDataPostCapture(Object object) throws Exception {
		throw new Error("Method: Object processDataPostCapture(List<?> objectList) is not valid for ActivityAlgorithm Module. Use: Object processDataRealTime(Object object).");
	}
	
	@Override
	public AlgorithmResultObject processDataRealTime(ObjectCluster ojc) throws Exception {
		accValues = new Vector3d();

		for(String associatedChannel: mAlgorithmDetails.mListOfAssociatedSensors){
			Collection<FormatCluster> dataFormatsSignal = ojc.getCollectionOfFormatClusters(associatedChannel);  // first retrieve all the possible formats for the current sensor device
			if(dataFormatsSignal!=null){
				FormatCluster formatClusterSignal = ((FormatCluster)ObjectCluster.returnFormatCluster(dataFormatsSignal, mAlgorithmDetails.mChannelType.toString())); // retrieve the calibrated data
				if(formatClusterSignal != null){
					double value = formatClusterSignal.mData;
					if(!Double.isNaN(value)){	
						setChannelValue(associatedChannel, value);
					}
					else{
						return null;
					}
				}
				else{
					return null;
				}
			}
			else{
				return null;
			}
		}
		
		// process the data i.e. algorithm implementation
		mActivityProcessing.activityProcessing(accValues);
		
		// add the result of the algorithm to the objectcluster
		for (ChannelDetails channelDetails : mChannelMapActivity.values()) {
			addActivityProcessingChannelToObjectCluster(ojc, channelDetails);
		}
		
		AlgorithmResultObject aro = new AlgorithmResultObject(mAlgorithmResultType, ojc, getTrialName());
		return aro;		
	}
	
	private void setChannelValue(String channelName, double value){
		if(channelName.equals(Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_WR_X)){
			accValues.x = value;
		}
		else if(channelName.equals(Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_WR_Y)){
			accValues.y = value;
		}
		else if(channelName.equals(Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_WR_Z)){
			accValues.z = value;
		}
	}
	
	private void addActivityProcessingChannelToObjectCluster(ObjectCluster ojc, ChannelDetails channelDetails) {
		double result = 0.0;
		
		if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.ACTIVITY_INTENSITY)){
			result = mActivityProcessing.getActivityIntensity();
		}	
		else if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.ACTIVITY_SHORT)){
			result = mActivityProcessing.getActivityPeriodShort();
		}
		else if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.ACTIVITY_MEDIUM)){
			result = mActivityProcessing.getActivityPeriodMedium();
		}	
		else if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.ACTIVITY_LONG)){
			result = mActivityProcessing.getActivityPeriodLong();
		}
		else if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.ACTIVITY_STEP_COUNT)){
			result = mActivityProcessing.getStepCount();
		}	
		else if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.ACTIVITY_PERCENTAGE_SEDENTARY)){
			result = mActivityProcessing.getActivityPercentageSendentary();
		}
		else if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.ACTIVITY_PERCENTAGE_ACTVIVE)){
			result = mActivityProcessing.getActivityPercentageActive();
		}
		
		// hack because Graphs hang if they try to plot a 0.
		if (result == 0.0) {
			result = -1;
		}
		
		ojc.addData(mAlgorithmDetails.mAlgorithmName + "_" + channelDetails.mObjectClusterName, CHANNEL_TYPE.CAL, mAlgorithmDetails.mUnits, 1);
		ojc.incrementIndexKeeper();
	}
	
	@Override
	public String printBatchMetrics() {
		String outputString = "";
		return outputString;
	}

	@Override
	public void eventDataReceived(ShimmerMsg shimmerMSG) {
		// TODO Auto-generated method stub
		
	}
	
	public void setSamplingRate(double samplingRate){
		mSamplingRate = samplingRate;
		try {
			initialize();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private double getSamplingRate() {
		return mSamplingRate;
	}
	
	/** Filter here depending on Shimmer version */
	public static LinkedHashMap<String, AlgorithmDetails> getMapOfSupportedAlgorithms() {
		LinkedHashMap<String, AlgorithmDetails> mapOfSupportedAlgorithms = new LinkedHashMap<String, AlgorithmDetails>();
		//TODO Filter here depending on Shimmer version
		mapOfSupportedAlgorithms.putAll(mAlgorithmMapRef);
		return mapOfSupportedAlgorithms;
	}
}
