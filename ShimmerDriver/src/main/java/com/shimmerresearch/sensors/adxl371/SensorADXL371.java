package com.shimmerresearch.sensors.adxl371;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.ArrayUtils;

import com.shimmerresearch.driver.Configuration.CHANNEL_UNITS;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.driver.Configuration.Shimmer3.CompatibilityInfoForMaps;
import com.shimmerresearch.driver.calibration.CalibDetails;
import com.shimmerresearch.driver.calibration.CalibDetailsKinematic;
import com.shimmerresearch.driver.calibration.UtilCalibration;
import com.shimmerresearch.driver.calibration.CalibDetails.CALIB_READ_SOURCE;
import com.shimmerresearch.driver.shimmer2r3.ConfigByteLayoutShimmer3;
import com.shimmerresearch.bluetooth.BtCommandDetails;
import com.shimmerresearch.driver.ConfigByteLayout;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.FormatCluster;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driver.ShimmerObject;
import com.shimmerresearch.driverUtilities.ChannelDetails;
import com.shimmerresearch.driverUtilities.ConfigOptionDetailsSensor;
import com.shimmerresearch.driverUtilities.SensorDetails;
import com.shimmerresearch.driverUtilities.SensorDetailsRef;
import com.shimmerresearch.driverUtilities.SensorGroupingDetails;
import com.shimmerresearch.driverUtilities.UtilShimmer;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_ENDIAN;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_DATA_TYPE;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_TYPE;
import com.shimmerresearch.sensors.AbstractSensor;
import com.shimmerresearch.sensors.ActionSetting;

public class SensorADXL371 extends AbstractSensor{

	/**
	 * 
	 */
	private static final long serialVersionUID = -841122434330904985L;
	
	// ----------   High-g accel start ---------------
	protected int mSensorIdAccel = -1;
	protected int mAccelRange = 0;

	public CalibDetailsKinematic mCurrentCalibDetailsAccelHighG = null;
	public boolean mIsUsingDefaultHighGAccelParam = true;
	protected boolean mHighResAccelHighG = true;
	
	protected int mADXL371AnalogAccelRate = 0;
	protected int mADXL371AnalogAccelRange = 0;
	
	public class GuiLabelConfig{
		public static final String ADXL371_ACCEL_RANGE = "High G Accel Range"; 
		public static final String ADXL371_ACCEL_RATE = "High G Accel Rate";  
	}
	
	public class GuiLabelSensors{
		public static final String ACCEL_HIGHG = "High-G Accelerometer"; 
	}

	public class LABEL_SENSOR_TILE{
		public static final String HIGH_G_ACCEL = GuiLabelSensors.ACCEL_HIGHG;
		public static final String ACCEL = "ACCEL";
	}
	
	public static class ObjectClusterSensorName{
		public static  String ACCEL_HIGHG_X = "Accel_HighG_X";
		public static  String ACCEL_HIGHG_Y = "Accel_HighG_Y";
		public static  String ACCEL_HIGHG_Z= "Accel_HighG_Z";
	}
	
	public static class DatabaseChannelHandles{
		public static final String HIGHG_ACC_X = "ADXL371_ACC_X";
		public static final String HIGHG_ACC_Y = "ADXL371_ACC_Y";
		public static final String HIGHG_ACC_Z = "ADXL371_ACC_Z";
	}
	
	public static final class DatabaseConfigHandle{
		public static final String HIGHG_ACC = "ADXL371_Acc";
		public static final String HIGHG_ACC_RATE = "ADXL371_Acc_Rate";
		public static final String HIGHG_ACC_RANGE = "ADXL371_Acc_Range";
	
		public static final String HIGHG_ACC_CALIB_TIME = "ADXL371_Acc_Calib_Time";
		public static final String HIGHG_ACC_OFFSET_X = "ADXL371_Acc_Offset_X";
		public static final String HIGHG_ACC_OFFSET_Y = "ADXL371_Acc_Offset_Y";
		public static final String HIGHG_ACC_OFFSET_Z = "ADXL371_Acc_Offset_Z";
		public static final String HIGHG_ACC_GAIN_X = "ADXL371_Acc_Gain_X";
		public static final String HIGHG_ACC_GAIN_Y = "ADXL371_Acc_Gain_Y";
		public static final String HIGHG_ACC_GAIN_Z = "ADXL371_Acc_Gain_Z";
		public static final String HIGHG_ACC_ALIGN_XX = "ADXL371_Acc_Align_XX";
		public static final String HIGHG_ACC_ALIGN_XY = "ADXL371_Acc_Align_XY";
		public static final String HIGHG_ACC_ALIGN_XZ = "ADXL371_Acc_Align_XZ";
		public static final String HIGHG_ACC_ALIGN_YX = "ADXL371_Acc_Align_YX";
		public static final String HIGHG_ACC_ALIGN_YY = "ADXL371_Acc_Align_YY";
		public static final String HIGHG_ACC_ALIGN_YZ = "ADXL371_Acc_Align_YZ";
		public static final String HIGHG_ACC_ALIGN_ZX = "ADXL371_Acc_Align_ZX";
		public static final String HIGHG_ACC_ALIGN_ZY = "ADXL371_Acc_Align_ZY";
		public static final String HIGHG_ACC_ALIGN_ZZ = "ADXL371_Acc_Align_ZZ";
	
		public static final List<String> LIST_OF_CALIB_HANDLES_HIGHG_ACCEL = Arrays.asList(
				DatabaseConfigHandle.HIGHG_ACC_OFFSET_X, DatabaseConfigHandle.HIGHG_ACC_OFFSET_Y, DatabaseConfigHandle.HIGHG_ACC_OFFSET_Z,
				DatabaseConfigHandle.HIGHG_ACC_GAIN_X, DatabaseConfigHandle.HIGHG_ACC_GAIN_Y, DatabaseConfigHandle.HIGHG_ACC_GAIN_Z,
				DatabaseConfigHandle.HIGHG_ACC_ALIGN_XX, DatabaseConfigHandle.HIGHG_ACC_ALIGN_XY, DatabaseConfigHandle.HIGHG_ACC_ALIGN_XZ,
				DatabaseConfigHandle.HIGHG_ACC_ALIGN_YX, DatabaseConfigHandle.HIGHG_ACC_ALIGN_YY, DatabaseConfigHandle.HIGHG_ACC_ALIGN_YZ,
				DatabaseConfigHandle.HIGHG_ACC_ALIGN_ZX, DatabaseConfigHandle.HIGHG_ACC_ALIGN_ZY, DatabaseConfigHandle.HIGHG_ACC_ALIGN_ZZ);
	}

	public static final double[][] DefaultAlignmentADXL371 = {{0,1,0},{1,0,0},{0,0,-1}};			

	public static final double[][] DefaultAlignmentMatrixHighGAccelShimmer3R = DefaultAlignmentADXL371;	
	public static final double[][] DefaultOffsetVectorHighGAccelShimmer3R = {{10},{10},{10}};	
	public static final double[][] DefaultSensitivityMatrixHighGAccelShimmer3R = {{1,0,0},{0,1,0},{0,0,1}};

	private CalibDetailsKinematic calibDetailsAccelHighG = new CalibDetailsKinematic(
			ListofADXL371AccelRangeConfigValues[0],
			ListofADXL371AccelRange[0],
			DefaultAlignmentMatrixHighGAccelShimmer3R, 
			DefaultSensitivityMatrixHighGAccelShimmer3R, 
			DefaultOffsetVectorHighGAccelShimmer3R);

	// ----------   High-g accel end ---------------
	
	//--------- Sensor info start --------------
	public static final SensorDetailsRef sensorADXL371Accel = new SensorDetailsRef(
			0x400000, //== Configuration.Shimmer3.SensorBitmap.SENSOR_D_ACCEL will be: SensorBitmap.SENSOR_D_ACCEL
			0x400000, //== Configuration.Shimmer3.SensorBitmap.SENSOR_D_ACCEL will be: SensorBitmap.SENSOR_D_ACCEL
			GuiLabelSensors.ACCEL_HIGHG,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoADXL371,
			Arrays.asList(GuiLabelConfig.ADXL371_ACCEL_RANGE,
				GuiLabelConfig.ADXL371_ACCEL_RATE),
			Arrays.asList(ObjectClusterSensorName.ACCEL_HIGHG_X,
					ObjectClusterSensorName.ACCEL_HIGHG_Y,
					ObjectClusterSensorName.ACCEL_HIGHG_Z));
	
	public static final Map<Integer, SensorDetailsRef> mSensorMapRef;
    static {
        Map<Integer, SensorDetailsRef> aMap = new LinkedHashMap<Integer, SensorDetailsRef>();
        aMap.put(Configuration.Shimmer3.SENSOR_ID.SHIMMER_ADXL371_ACCEL_HIGHG, SensorADXL371.sensorADXL371Accel);  
		mSensorMapRef = Collections.unmodifiableMap(aMap);
    }
	//--------- Sensor info end --------------
    
	//--------- Bluetooth commands start --------------
	//still not being implemented for high g accel sensor due to unavailability in doc
	public static final byte SET_ALT_ACCEL_CALIBRATION_COMMAND 	= (byte) 0xA9; 
	public static final byte ALT_ACCEL_CALIBRATION_RESPONSE	 	= (byte) 0xAA; 
	public static final byte GET_ALT_ACCEL_CALIBRATION_COMMAND  	= (byte) 0xAB; 
	
	public static final byte SET_ALT_ACCEL_SAMPLING_RATE_COMMAND  	= (byte) 0xAC; 
	public static final byte ALT_ACCEL_SAMPLING_RATE_RESPONSE  		= (byte) 0xAD; 
	public static final byte GET_ALT_ACCEL_SAMPLING_RATE_COMMAND  	= (byte) 0xAE; 

    public static final Map<Byte, BtCommandDetails> mBtGetCommandMap;
    static {
        Map<Byte, BtCommandDetails> aMap = new LinkedHashMap<Byte, BtCommandDetails>();
        aMap.put(GET_ALT_ACCEL_CALIBRATION_COMMAND, new BtCommandDetails(GET_ALT_ACCEL_CALIBRATION_COMMAND, "GET_ALT_ACCEL_CALIBRATION_COMMAND", ALT_ACCEL_CALIBRATION_RESPONSE));
        aMap.put(GET_ALT_ACCEL_SAMPLING_RATE_COMMAND, new BtCommandDetails(GET_ALT_ACCEL_SAMPLING_RATE_COMMAND, "GET_ALT_ACCEL_SAMPLING_RATE_COMMAND", ALT_ACCEL_SAMPLING_RATE_RESPONSE));
        mBtGetCommandMap = Collections.unmodifiableMap(aMap);
    }
    
    public static final Map<Byte, BtCommandDetails> mBtSetCommandMap;
    static {
        Map<Byte, BtCommandDetails> aMap = new LinkedHashMap<Byte, BtCommandDetails>();
        aMap.put(SET_ALT_ACCEL_CALIBRATION_COMMAND, new BtCommandDetails(SET_ALT_ACCEL_CALIBRATION_COMMAND, "SET_ALT_ACCEL_CALIBRATION_COMMAND"));
        aMap.put(SET_ALT_ACCEL_SAMPLING_RATE_COMMAND, new BtCommandDetails(SET_ALT_ACCEL_SAMPLING_RATE_COMMAND, "SET_ALT_ACCEL_SAMPLING_RATE_COMMAND"));
         mBtSetCommandMap = Collections.unmodifiableMap(aMap);
    }
	//--------- Bluetooth commands end --------------
    
	
	//--------- Configuration options start --------------
	

	public static final String[] ListofADXL371AccelRate={"320Hz", "640Hz", "1280Hz", "2560Hz"};
	public static final Integer[] ListofADXL371AccelRateConfigValues={0,1,2,3};
	public static final String[] ListofADXL371AccelRange={"+/- 200g"}; 
	public static final Integer[] ListofADXL371AccelRangeConfigValues={0};  

	public static final ConfigOptionDetailsSensor configOptionAccelRate = new ConfigOptionDetailsSensor(
			SensorADXL371.GuiLabelConfig.ADXL371_ACCEL_RATE,
			SensorADXL371.DatabaseConfigHandle.HIGHG_ACC_RATE,
			ListofADXL371AccelRate, 
			ListofADXL371AccelRateConfigValues, 
			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoADXL371);
	
	public static final ConfigOptionDetailsSensor configOptionAccelRange = new ConfigOptionDetailsSensor(
			SensorADXL371.GuiLabelConfig.ADXL371_ACCEL_RANGE,
			SensorADXL371.DatabaseConfigHandle.HIGHG_ACC_RANGE,
			ListofADXL371AccelRange, 
			ListofADXL371AccelRangeConfigValues, 
			ConfigOptionDetailsSensor.GUI_COMPONENT_TYPE.COMBOBOX,
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoADXL371);
	
	//--------- Configuration options end --------------
    
  //--------- Channel info start --------------
    public static final ChannelDetails channelADXL371AccelX = new ChannelDetails(
			ObjectClusterSensorName.ACCEL_HIGHG_X,
			ObjectClusterSensorName.ACCEL_HIGHG_X,
			DatabaseChannelHandles.HIGHG_ACC_X,
			CHANNEL_DATA_TYPE.UNKOWN, 2, CHANNEL_DATA_ENDIAN.MSB,
			CHANNEL_UNITS.METER_PER_SECOND_SQUARE,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL),
			0x14);
    
    public static final ChannelDetails channelADXL371AccelY = new ChannelDetails(
			ObjectClusterSensorName.ACCEL_HIGHG_Y,
			ObjectClusterSensorName.ACCEL_HIGHG_Y,
			DatabaseChannelHandles.HIGHG_ACC_Y,
			CHANNEL_DATA_TYPE.UNKOWN, 2, CHANNEL_DATA_ENDIAN.MSB,
			CHANNEL_UNITS.METER_PER_SECOND_SQUARE,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL),
			0x15);
    
    public static final ChannelDetails channelADXL371AccelZ = new ChannelDetails(
			ObjectClusterSensorName.ACCEL_HIGHG_Z,
			ObjectClusterSensorName.ACCEL_HIGHG_Z,
			DatabaseChannelHandles.HIGHG_ACC_Z,
			CHANNEL_DATA_TYPE.UNKOWN, 2, CHANNEL_DATA_ENDIAN.MSB,
			CHANNEL_UNITS.METER_PER_SECOND_SQUARE,
			Arrays.asList(CHANNEL_TYPE.CAL, CHANNEL_TYPE.UNCAL),
			0x16);
    
    public static final Map<String, ChannelDetails> mChannelMapRef;
    static {
        Map<String, ChannelDetails> aMap = new LinkedHashMap<String, ChannelDetails>();
        aMap.put(SensorADXL371.ObjectClusterSensorName.ACCEL_HIGHG_X, SensorADXL371.channelADXL371AccelX);
        aMap.put(SensorADXL371.ObjectClusterSensorName.ACCEL_HIGHG_Y, SensorADXL371.channelADXL371AccelY);
        aMap.put(SensorADXL371.ObjectClusterSensorName.ACCEL_HIGHG_Z, SensorADXL371.channelADXL371AccelZ);
		mChannelMapRef = Collections.unmodifiableMap(aMap);
    }
	//--------- Channel info end --------------
	
    public static final SensorGroupingDetails sensorGroupAdxlAccel = new SensorGroupingDetails(
			LABEL_SENSOR_TILE.HIGH_G_ACCEL,
			Arrays.asList(Configuration.Shimmer3.SENSOR_ID.SHIMMER_ADXL371_ACCEL_HIGHG),
			CompatibilityInfoForMaps.listOfCompatibleVersionInfoADXL371);

  //--------- Constructors for this class start --------------

	public SensorADXL371() {
		super(SENSORS.ADXL371);
		initialise();
	}
	
	public SensorADXL371(ShimmerObject obj) {
		super(SENSORS.ADXL371, obj);
		initialise();
	}
	
	public SensorADXL371(ShimmerDevice shimmerDevice) {
		super(SENSORS.ADXL371, shimmerDevice);
		initialise();
	}
    
  //--------- Constructors for this class end --------------
	
	@Override 
	public void generateSensorMap() {
		super.createLocalSensorMapWithCustomParser(mSensorMapRef, mChannelMapRef);
	}

	
	@Override 
	public void generateConfigOptionsMap() {
		addConfigOption(configOptionAccelRate);
		addConfigOption(configOptionAccelRange);
	}
	
	@Override 
	public void generateSensorGroupMapping() {
		mSensorGroupingMap = new LinkedHashMap<Integer, SensorGroupingDetails>();
		mSensorGroupingMap.put(Configuration.Shimmer3.LABEL_SENSOR_TILE.HIGH_G_ACCEL.ordinal(), sensorGroupAdxlAccel);
		super.updateSensorGroupingMap();	
	}	
	
//--------- Abstract methods implemented start --------------

	@Override
	public ObjectCluster processDataCustom(SensorDetails sensorDetails, byte[] rawData, COMMUNICATION_TYPE commType,
		ObjectCluster objectCluster, boolean isTimeSyncEnabled, double pctimeStampMs) {
	// process data originating from the Shimmer
			objectCluster = sensorDetails.processDataCommon(rawData, commType, objectCluster, isTimeSyncEnabled, pctimeStampMs);
			
			//Calibration
			if(mEnableCalibration){
				// get uncalibrated data for each (sub)sensor
				if(sensorDetails.mSensorDetailsRef.mGuiFriendlyLabel.equals(GuiLabelSensors.ACCEL_HIGHG) && mCurrentCalibDetailsAccelHighG!=null){
					double[] unCalibratedAccelHighGData = new double[3];
					for (ChannelDetails channelDetails:sensorDetails.mListOfChannels){
						//Uncalibrated Accelerometer data
						if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.ACCEL_HIGHG_X)){
							unCalibratedAccelHighGData[0] = ((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(channelDetails.mObjectClusterName), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
						}
						else if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.ACCEL_HIGHG_Y)){
							unCalibratedAccelHighGData[1]  = ((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(channelDetails.mObjectClusterName), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
						}
						else if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.ACCEL_HIGHG_Z)){
							unCalibratedAccelHighGData[2]  = ((FormatCluster)ObjectCluster.returnFormatCluster(objectCluster.getCollectionOfFormatClusters(channelDetails.mObjectClusterName), channelDetails.mChannelFormatDerivedFromShimmerDataPacket.toString())).mData;
						}
					}
					
					double[] calibratedAccelHighGData = UtilCalibration.calibrateInertialSensorData(unCalibratedAccelHighGData, mCurrentCalibDetailsAccelHighG);
//						double[] calibratedAccelWrData = UtilCalibration.calibrateInertialSensorData(unCalibratedAccelWrData, mAlignmentMatrixHIGHGAccel, mSensitivityMatrixHIGHGAccel, mOffsetVectorHIGHGAccel);
		
					//Add calibrated data to Object cluster
					if(sensorDetails.mSensorDetailsRef.mGuiFriendlyLabel.equals(GuiLabelSensors.ACCEL_HIGHG)){	
						for (ChannelDetails channelDetails:sensorDetails.mListOfChannels){
							if (channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.ACCEL_HIGHG_X)){
								objectCluster.addCalData(channelDetails, calibratedAccelHighGData[0], objectCluster.getIndexKeeper()-3, isUsingDefaultHighGAccelParam());
							}
							else if(channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.ACCEL_HIGHG_Y)){
								objectCluster.addCalData(channelDetails, calibratedAccelHighGData[1], objectCluster.getIndexKeeper()-2, isUsingDefaultHighGAccelParam());
							}
							else if(channelDetails.mObjectClusterName.equals(ObjectClusterSensorName.ACCEL_HIGHG_Z)){
								objectCluster.addCalData(channelDetails, calibratedAccelHighGData[2], objectCluster.getIndexKeeper()-1, isUsingDefaultHighGAccelParam());
							}
						}
					}
		
					//Debugging
					if(mIsDebugOutput){
						super.consolePrintChannelsCal(objectCluster, Arrays.asList(
								new String[]{ObjectClusterSensorName.ACCEL_HIGHG_X, CHANNEL_TYPE.UNCAL.toString()}, 
								new String[]{ObjectClusterSensorName.ACCEL_HIGHG_Y, CHANNEL_TYPE.UNCAL.toString()}, 
								new String[]{ObjectClusterSensorName.ACCEL_HIGHG_Z, CHANNEL_TYPE.UNCAL.toString()}, 
								new String[]{ObjectClusterSensorName.ACCEL_HIGHG_X, CHANNEL_TYPE.CAL.toString()}, 
								new String[]{ObjectClusterSensorName.ACCEL_HIGHG_Y, CHANNEL_TYPE.CAL.toString()},
								new String[]{ObjectClusterSensorName.ACCEL_HIGHG_Z, CHANNEL_TYPE.CAL.toString()}));
					}
				}
			}
			return objectCluster;
	}

	@Override
	public void checkShimmerConfigBeforeConfiguring() {
		if(!isSensorEnabled(mSensorIdAccel)) {
			setDefaultAdxl371AccelSensorConfig(false);
		}
	}

	@Override
	public void configBytesGenerate(ShimmerDevice shimmerDevice, byte[] configBytes, COMMUNICATION_TYPE commType) {

		ConfigByteLayout configByteLayout = shimmerDevice.getConfigByteLayout();
		if(configByteLayout instanceof ConfigByteLayoutShimmer3){
			ConfigByteLayoutShimmer3 configByteLayoutCast = (ConfigByteLayoutShimmer3) configByteLayout;
			
			configBytes[configByteLayoutCast.idxConfigSetupByte4] |= (byte) ((getADXL371AnalogAccelRate() & configByteLayoutCast.maskADXL371AltAccelSamplingRate) << configByteLayoutCast.bitShiftADXL371AltAccelSamplingRate);

			byte[] bufferCalibrationParameters = generateCalParamADXL371Accel();
			System.arraycopy(bufferCalibrationParameters, 0, configBytes, configByteLayoutCast.idxADXL371AltAccelCalibration, configByteLayoutCast.lengthGeneralCalibrationBytes);

		}
	}

	@Override
	public void configBytesParse(ShimmerDevice shimmerDevice, byte[] configBytes, COMMUNICATION_TYPE commType) {
		ConfigByteLayout configByteLayout = shimmerDevice.getConfigByteLayout();
		if(configByteLayout instanceof ConfigByteLayoutShimmer3){
			ConfigByteLayoutShimmer3 configByteLayoutCast = (ConfigByteLayoutShimmer3) configByteLayout;

			setADXL371AnalogAccelRate((configBytes[configByteLayoutCast.idxConfigSetupByte4] >> configByteLayoutCast.bitShiftADXL371AltAccelSamplingRate) & configByteLayoutCast.maskADXL371AltAccelSamplingRate); 
			
			if (shimmerDevice.isConnected()){
				getCurrentCalibDetailsAccelHighG().mCalibReadSource=CALIB_READ_SOURCE.INFOMEM;
			}

			byte[] bufferCalibrationParameters = new byte[configByteLayoutCast.lengthGeneralCalibrationBytes];
			System.arraycopy(configBytes, configByteLayoutCast.idxADXL371AltAccelCalibration, bufferCalibrationParameters, 0 , configByteLayoutCast.lengthGeneralCalibrationBytes);
			parseCalibParamFromPacketAccelAdxl(bufferCalibrationParameters, CALIB_READ_SOURCE.INFOMEM);
		}
		
	}

	@Override
	public Object setConfigValueUsingConfigLabel(Integer sensorId, String configLabel, Object valueToSet) {
		Object returnValue = null;
		
		switch(configLabel){
			case(GuiLabelConfig.ADXL371_ACCEL_RANGE):
				setADXL371AccelRange((int)valueToSet);
				break;
			case(GuiLabelConfig.ADXL371_ACCEL_RATE):
				setADXL371AnalogAccelRate((int)valueToSet);
				break;
			case(GuiLabelConfigCommon.RANGE):
				if(sensorId==mSensorIdAccel){
					this.setConfigValueUsingConfigLabel(GuiLabelConfig.ADXL371_ACCEL_RANGE, valueToSet);
				}
				break;
			case(GuiLabelConfigCommon.RATE):
				if(sensorId==mSensorIdAccel){
					this.setConfigValueUsingConfigLabel(GuiLabelConfig.ADXL371_ACCEL_RATE, valueToSet);
				}
				break;
			default:
				returnValue = super.setConfigValueUsingConfigLabelCommon(sensorId, configLabel, valueToSet);
				break;
		}	
		
        if(configLabel.equals(SensorADXL371.GuiLabelConfig.ADXL371_ACCEL_RATE)){
        	checkConfigOptionValues(configLabel);
        }
		
		return returnValue;
	}

	@Override
	public Object getConfigValueUsingConfigLabel(Integer sensorId, String configLabel) {
		Object returnValue = null;
		
		if(configLabel.equals(GuiLabelConfig.ADXL371_ACCEL_RATE)){
        	checkConfigOptionValues(configLabel);
        }
		
		switch(configLabel){
			case(GuiLabelConfig.ADXL371_ACCEL_RANGE): 
				returnValue = getAccelRange();
		    	break;
			case(GuiLabelConfig.ADXL371_ACCEL_RATE): 
				int configValue = getADXL371AnalogAccelRate(); 
				returnValue = configValue;
				break;
			case(GuiLabelConfigCommon.RANGE):
				if(sensorId==mSensorIdAccel){
					returnValue = this.getConfigValueUsingConfigLabel(GuiLabelConfig.ADXL371_ACCEL_RANGE);
				}
				break;
			case(GuiLabelConfigCommon.RATE):
				if(sensorId==mSensorIdAccel){
					returnValue = this.getConfigValueUsingConfigLabel(GuiLabelConfig.ADXL371_ACCEL_RATE);
				}
				break;
			default:
				returnValue = super.getConfigValueUsingConfigLabelCommon(sensorId, configLabel);
				break;
			
		}
		return returnValue;
	}

	@Override
	public void setSensorSamplingRate(double samplingRateHz) {
		//set sampling rate of the sensors as close to the Shimmer sampling rate as possible (sensor sampling rate >= shimmer sampling rate)
		setADXL371AccelRateFromFreq(samplingRateHz);

	}

	@Override
	public boolean setDefaultConfigForSensor(int sensorId, boolean isSensorEnabled) {
		if(mSensorMap.containsKey(sensorId)){
			if(sensorId==mSensorIdAccel) {
				setDefaultAdxl371AccelSensorConfig(isSensorEnabled);		
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean checkConfigOptionValues(String stringKey) {
		if(mConfigOptionsMap.containsKey(stringKey)){
			if(stringKey==GuiLabelConfig.ADXL371_ACCEL_RATE){

			}		
			return true;
		}
		return true;
	}

	@Override
	public Object getSettings(String componentName, COMMUNICATION_TYPE commType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ActionSetting setSettings(String componentName, Object valueToSet, COMMUNICATION_TYPE commType) {
		ActionSetting actionsetting = new ActionSetting(commType);

		return actionsetting;
	}

	@Override
	public boolean processResponse(int responseCommand, Object parsedResponse, COMMUNICATION_TYPE commType) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public LinkedHashMap<String, Object> generateConfigMap() {
		LinkedHashMap<String, Object> mapOfConfig = new LinkedHashMap<String, Object>();
		
		mapOfConfig.put(SensorADXL371.DatabaseConfigHandle.HIGHG_ACC_RATE, getADXL371AnalogAccelRate());
		mapOfConfig.put(SensorADXL371.DatabaseConfigHandle.HIGHG_ACC_RANGE, getADXL371AnalogAccelRange());
		
		super.addCalibDetailsToDbMap(mapOfConfig, 
				getCurrentCalibDetailsAccelHighG(), 
				SensorADXL371.DatabaseConfigHandle.LIST_OF_CALIB_HANDLES_HIGHG_ACCEL,
				SensorADXL371.DatabaseConfigHandle.HIGHG_ACC_CALIB_TIME);

		return mapOfConfig;
	}	

	@Override
	public void parseConfigMap(LinkedHashMap<String, Object> mapOfConfigPerShimmer) {
		
		if(mapOfConfigPerShimmer.containsKey(SensorADXL371.DatabaseConfigHandle.HIGHG_ACC_RATE)){
			setADXL371AnalogAccelRate(((Double) mapOfConfigPerShimmer.get(SensorADXL371.DatabaseConfigHandle.HIGHG_ACC_RATE)).intValue());
		}
		
		//Analog Accel Calibration Configuration
		parseCalibDetailsKinematicFromDb(mapOfConfigPerShimmer, 
				Configuration.Shimmer3.SENSOR_ID.SHIMMER_ADXL371_ACCEL_HIGHG, 
				0, 
				SensorADXL371.DatabaseConfigHandle.LIST_OF_CALIB_HANDLES_HIGHG_ACCEL,
				SensorADXL371.DatabaseConfigHandle.HIGHG_ACC_CALIB_TIME);
	}
	
	//--------- Optional methods to override in Sensor Class start --------
	@Override
	public void initialise() {
		mSensorIdAccel = Configuration.Shimmer3.SENSOR_ID.SHIMMER_ADXL371_ACCEL_HIGHG;
		super.initialise();
		mADXL371AnalogAccelRange = ListofADXL371AccelRangeConfigValues[0];

		updateCurrentAccelHighGCalibInUse();
	}

	@Override
	public void generateCalibMap() {
		super.generateCalibMap();
		
		TreeMap<Integer, CalibDetails> calibMapAccelHighG= new TreeMap<Integer, CalibDetails>();
		calibMapAccelHighG.put(calibDetailsAccelHighG.mRangeValue, calibDetailsAccelHighG);
		setCalibrationMapPerSensor(Configuration.Shimmer3.SENSOR_ID.SHIMMER_ADXL371_ACCEL_HIGHG, calibMapAccelHighG);

		updateCurrentAccelHighGCalibInUse();
	}
	
	@Override
	public boolean isSensorUsingDefaultCal(int sensorId) {
		if(sensorId==Configuration.Shimmer3.SENSOR_ID.SHIMMER_ADXL371_ACCEL_HIGHG){
			return isUsingDefaultHighGAccelParam();
		}
		return false;
	}
	
	//--------- Optional methods to override in Sensor Class end --------	
	
	//--------- Sensor specific methods start --------------

	public void updateCurrentAccelHighGCalibInUse(){
		mCurrentCalibDetailsAccelHighG = getCurrentCalibDetailsIfKinematic(mSensorIdAccel, getAccelRange());
	}
	
	public int getAccelRange() {
		return mAccelRange;
	}
	
	public boolean isUsingDefaultHighGAccelParam(){
		return mCurrentCalibDetailsAccelHighG.isUsingDefaultParameters(); 
	}
	
	public CalibDetailsKinematic getCurrentCalibDetailsAccelHighG(){
//		return getCurrentCalibDetails(mSensorIdAccel, getAccelRange());
		return mCurrentCalibDetailsAccelHighG;
	}
	
	public byte[] generateCalParamADXL371Accel(){
		return mCurrentCalibDetailsAccelHighG.generateCalParamByteArray();
	}
	
	public void parseCalibParamFromPacketAccelAdxl(byte[] bufferCalibrationParameters, CALIB_READ_SOURCE calibReadSource) {
		mCurrentCalibDetailsAccelHighG.parseCalParamByteArray(bufferCalibrationParameters, calibReadSource);
	}
	
	public void updateIsUsingDefaultHighGAccelParam() {
		mIsUsingDefaultHighGAccelParam = getCurrentCalibDetailsAccelHighG().isUsingDefaultParameters();
	}
	
	public int setADXL371AccelRateFromFreq(double freq) {
		boolean isEnabled = isSensorEnabled(mSensorIdAccel);
//		System.out.println("Setting Sampling Rate: " + freq + "\tmLowPowerAccelWR:" + mLowPowerAccelWR);
		setADXL371AnalogAccelRateInternal(getAccelRateFromFreqForSensor(isEnabled, freq));
		return mADXL371AnalogAccelRate;
	}
	
	public void setADXL371AnalogAccelRateInternal(int valueToSet) {
		//System.out.println("Accel Rate:\t" + valueToSet);
		//UtilShimmer.consolePrintCurrentStackTrace();
		mADXL371AnalogAccelRate = valueToSet;
	}
		
	public int getAccelRateFromFreqForSensor(boolean isEnabled, double freq) {
		return SensorADXL371.getAccelRateFromFreq(isEnabled, freq);
	}

	public static int getAccelRateFromFreq(boolean isEnabled, double freq) {
		int accelRate = 0; // Power down
		
		if(isEnabled){
			if (freq<=320){
				accelRate = 0; // 320Hz
			} else if (freq<=640){
				accelRate = 1; // 640Hz
			} else if (freq<=1280){
				accelRate = 2; // 1280Hz
			} else if (freq<=2560){
				accelRate = 3; // 2560Hz
			}
		}
		return accelRate;
	}
	
	public void setADXL371AccelRange(int valueToSet){
		if(ArrayUtils.contains(ListofADXL371AccelRangeConfigValues, valueToSet)){
			mAccelRange = valueToSet;
			updateCurrentAccelHighGCalibInUse();
		}
	}
	
	public void setADXL371AnalogAccelRate(int valueToSet) {
		setADXL371AnalogAccelRateInternal(valueToSet);
//		setADXL371AnalogAccelRateInternal(SensorADXL371.ListofADXL371AccelRateConfigValues[SensorADXL371.ListofADXL371AccelRateConfigValues.length-1]);
	}
	
	public void setDefaultAdxl371AccelSensorConfig(boolean isSensorEnabled) {
		if(isSensorEnabled) {
			setADXL371AccelRange(0);
		}
	}
	
	public double getCalibTimeHighGAccel() {
		return mCurrentCalibDetailsAccelHighG.getCalibTimeMs();
	}
	
	public boolean isUsingValidHighGAccelParam(){
		if(!UtilShimmer.isAllZeros(getAlignmentMatrixHighGAccel()) && !UtilShimmer.isAllZeros(getSensitivityMatrixHighGAccel())){
			return true;
		}else{
			return false;
		}
	}
	
	public double[][] getAlignmentMatrixHighGAccel(){
		return mCurrentCalibDetailsAccelHighG.getValidAlignmentMatrix();
	}
	
	public double[][] getSensitivityMatrixHighGAccel(){
		return mCurrentCalibDetailsAccelHighG.getValidSensitivityMatrix();
	}
	
	public double[][] getOffsetVectorMatrixHighGAccel(){
		return mCurrentCalibDetailsAccelHighG.getValidOffsetVector();
	}
	
	public int getADXL371AnalogAccelRate() {
		return mADXL371AnalogAccelRate;
	}
	
	public int getADXL371AnalogAccelRange() {
		return mADXL371AnalogAccelRange;	
	}
	
	public void setADXL371AnalogAccelRange(int i) {
		mADXL371AnalogAccelRange = i;
		updateCurrentAccelHighGCalibInUse();
	}
	
	public static String parseFromDBColumnToGUIChannel(String databaseChannelHandle) {
		return AbstractSensor.parseFromDBColumnToGUIChannel(mChannelMapRef, databaseChannelHandle);
	}

	public static String parseFromGUIChannelsToDBColumn(String objectClusterName) {
		return AbstractSensor.parseFromGUIChannelsToDBColumn(mChannelMapRef, objectClusterName);
	}
	
	//--------- Sensor specific methods end --------------

}
