package com.shimmerresearch.exgConfig;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import com.shimmerresearch.exgConfig.ExGConfigOptionDetails.SettingType;

public class ExGConfigBytesDetails {
	
	//http://www.ti.com/lit/ds/sbas502b/sbas502b.pdf
	
	HashMap<String, ExGConfigOptionDetails> mMapOfExGSettingsChip1 = new HashMap<String, ExGConfigOptionDetails>();
	HashMap<String, ExGConfigOptionDetails> mMapOfExGSettingsChip2 = new HashMap<String, ExGConfigOptionDetails>();
	
	private int BIT0 = 0x01;
	private int BIT1 = 0x02;
	private int BIT2 = 0x04;
	private int BIT3 = 0x08;
	private int BIT4 = 0x10;
	private int BIT5 = 0x20;
	private int BIT6 = 0x40;
	private int BIT7 = 0x80;


//	public static class ReferenceElectrode{
//		public static enum ECG{
//			InverseWilsonCT,
//			FixedPotential
//		}
//		public static enum EMG{
//			FixedPotential,
//			InverseOfCh1
//		}
//	}
	
	
	public final static String[] ListOfECGReferenceElectrode={"Inverse Wilson CT","Fixed Potential"};
	public final static Integer[] ListOfECGReferenceElectrodeConfigValues={13,0};
	
	public final static String[] ListOfEMGReferenceElectrode={"Fixed Potential", "Inverse of Ch1"};
	public final static Integer[] ListOfEMGReferenceElectrodeConfigValues={0,3};
	
	public final static String[] ListOfExGResolutions={"16-bit","24-bit"};
	public final static Integer[] ListOfExGResolutionsConfigValues={0,1};

	public final static String[] ListOfExGRate={"125 Hz","250 Hz","500 Hz","1 kHz","2 kHz","4 kHz","8 kHz"};
	public final static Integer[] ListOfExGRateConfigValues={0,1,2,3,4,5,6};

	public final static String[] ListOfOnOff={"On","Off"};
	public final static Integer[] ListOfOnOffConfigValues={0x01,0x00};
	

	
	//0 - Configuration Register 1
    public static String REG0 = "Configuration Register 1";
	//Bit7	- Conversion Mode
    public static String REG0_CONVERSION_MODES = "Conversion Mode";
	public final static String[] ListOfConversionModes = {"Continuous Conversion Mode", "Single-shot mode"};
	public final static Integer[] ListOfConversionModeConfigValues = {0,1};
	//Bits[6:3]	- Must be set to '0'
	//Bits[2:0]	- Data rate
    public static String REG0_DATA_RATE = "Data Rate";
	public final static String[] ListOfDataRates = {"125 SPS", "250 SPS", "500 SPS", "1 kSPS", "2 kSPS", "4 kSPS", "8 kSPS"};
	public final static Integer[] ListOfDataRateConfigValues = {0,1,2,3,4,5,6};

	//1 - Configuration Register 2
    public static String REG1 = "Configuration Register 2";
	//Bit7	- Must be set to '1' 
	//Bit6	- Lead-off comparators
    public static String REG1_LEAD_OFF_COMPARATORS = "Lead-off comparators";
	//Bit5	- Reference buffer
    public static String REG1_REFERENCE_BUFFER = "Reference buffer";
	//Bit4	- Voltage reference
    public static String REG1_VOLTAGE_REFERENCE = "Voltage reference";
	public final static String[] ListOfVoltageReference = {"2.42 V", "4.033 V"};
	public final static Integer[] ListOfVoltageReferenceConfigValues = {0,1};
	//Bit3	- Oscillator clock connection
    public static String REG1_OSCILLATOR_CLOCK_CONNECTION = "Oscillator clock connection";
	//Bit2	- Must be set to '0'
	//Bit1	- Test signal selection
    public static String REG1_TEST_SIGNAL_SELECTION = "Test signal selection";
	//Bit0	- Test signal frequency
    public static String REG1_TEST_SIGNAL_FREQUENCY = "Test signal frequency";
	public final static String[] ListOfTestFreq = {"DC", "1 kHz Square Wave"};
	public final static Integer[] ListOfTestFreqConfigValues = {0,1};
    
	//2 - Lead-Off Control Register
    public static String REG2 = "Lead-Off Control Register";
	//Comparator threshold - Bits[7:5]
    public static String REG2_COMPARATOR_THRESHOLD = "Comparator threshold";
	public final static String[] ListOfLeadOffCompThres={
		"Pos:95%-Neg:5%",
		"Pos:92.5%-Neg:7.5%",
		"Pos:90%-Neg:10%",
		"Pos:87.5%-Neg:12.5%",
		"Pos:85%-Neg:15%",
		"Pos:80%-Neg:20%",
		"Pos:75%-Neg:25%",
		"Pos:70%-Neg:30%"};
	public final static Integer[] ListOfLeadOffCompThresConfigValues={0,1,2,3,4,5,6,7};
	//Bit4	- Must be set to '1' - 
	//Bits[3:2]	- Lead-off current
    public static String REG2_LEAD_OFF_CURRENT = "Lead-off current";
	public final static String[] ListOfLeadOffCurrent={"6 nA","22 nA", "6 uA", "22 uA"};
	public final static Integer[] ListOfLeadOffCurrentConfigValues={0,1,2,3};
	//Bit1	- Must be set to '0'
	//Bit0	- Lead-off frequency
    public static String REG2_LEAD_OFF_FREQUENCY = "Lead-off frequency";
	public final static String[] ListOfLeadOffDetectionFreq={"DC lead-off detect", "AC lead-off detect (fs / 4)"};
	public final static Integer[] ListOfLeadOffDetectionFreqConfigValues={0,1};
	
	//3 - Channel 1 settings
	//4 - Channel 2 settings
    public static String REG3 = "Channel 1 settings";
    public static String REG4 = "Channel 2 settings";
	//Channel power-down - Bit7
    public static String REG3_CHANNEL_1_POWER_DOWN = "Channel 1 power-down";
    public static String REG4_CHANNEL_2_POWER_DOWN = "Channel 2 power-down";
	public final static String[] ListOfChannelPowerDown={"Normal operation","Power-down"};
	public final static Integer[] ListOfChannelPowerDownConfigValues={0,1};
	//PGA Gain - Bits[6:4]
    public static String REG3_CHANNEL_1_PGA_GAIN = "Channel 1 PGA Gain";
    public static String REG4_CHANNEL_2_PGA_GAIN = "Channel 2 PGA Gain";
	public final static String[] ListOfExGGain={"6","1","2","3","4","8","12"};
	public final static Integer[] ListOfExGGainConfigValues={0,1,2,3,4,5,6};
	//Input selection - Bits[3:0]
    public static String REG3_CHANNEL_1_INPUT_SELECTION = "Channel 1 Input Selection";
    public static String REG4_CHANNEL_2_INPUT_SELECTION = "Channel 2 Input Selection";
	public final static String[] ListOfCh1InputSelection={
		"Normal electrode input",
		"Input shorted",
		"RLD_MEASURE",
		"MVDD for supply measurement",
		"Temperature sensor",
		"Test signal",
		"RLD_DRP (positive side connected to RLDIN)", 
		"RLD_DRM (negative side connected to RLDIN)",
		"RLD_DRPM (both positive and negative sides connected to RLDIN)",
		"Route IN3P and IN3N to channel 1 inputs"		
	};
	public final static String[] ListOfCh2InputSelection={
		"Normal electrode input",
		"Input shorted",
		"RLD_MEASURE",
		"VDD / 2 for supply measurement",
		"Temperature sensor",
		"Test signal",
		"RLD_DRP (positive side connected to RLDIN)", 
		"RLD_DRM (negative side connected to RLDIN)",
		"RLD_DRPM (both positive and negative sides connected to RLDIN)",
		"Route IN3P and IN3N to channel 1 inputs"		
	};
	public final static Integer[] ListOfInputSelectionConfigValues={0,1,2,3,4,5,6,7,8,9};

	//5 - Right leg drive sense selection
    public static String REG5 = "Right leg drive sense selection";
	//PGA chop frequency - Bits[7:6]
    public static String REG5_PGA_CHOP_FREQUENCY = "PGA chop frequency";
	public final static String[] ListOfPgaChopFrequency={"fMOD / 16", "fMOD / 2", "fMOD / 4"};
	public final static Integer[] ListOfPgaChopFrequencyConfigValues={0,2,3};
	//RLD buffer power - Bit5
    public static String REG5_RLD_BUFFER_POWER = "RLD buffer power";
	public final static String[] ListOfRldBufferPower={"Powered down", "Enabled"};
	public final static Integer[] ListOfRldBufferPowerConfigValues={0,1};
	//RLD lead-off sense function - Bit4
    public static String REG5_RLD_LEAD_OFF_SENSE_FUNCTION = "RLD lead-off sense function";
	//Channel 2 RLD negative inputs - Bit3
    public static String REG5_CH2_RLD_NEG_INPUTS = "Channel 2 RLD negative inputs";
	public final static String[] ListOfCh2RldNegInputs={"Not connected", "Connected to IN2N"};
	public final static Integer[] ListOfRldInputsConfigValues={0,1};
	//Channel 2 RLD positive inputs - Bit2
    public static String REG5_CH2_RLD_POS_INPUTS = "Channel 2 RLD positive inputs";
	public final static String[] ListOfCh2RldPosInputs={"Not connected", "Connected to IN2PN"};
	//Channel 1 RLD negative inputs - Bit1
    public static String REG5_CH1_RLD_NEG_INPUTS = "Channel 1 RLD negative inputs";
	public final static String[] ListOfCh1RldNegInputs={"Not connected", "Connected to IN1N"};
	//Channel 1 RLD positive inputs - Bit0
    public static String REG5_CH1_RLD_POS_INPUTS = "Channel 1 RLD positive inputs";
	public final static String[] ListOfCh1RldPosInputs={"Not connected", "Connected to IN1P"};
	
	//6 - Lead-off sense selection
    public static String REG6 = "Lead-off sense selection";
	//Must be set to '0' - Bits[7:6]
	//Flip current direction - channel 2 - Bit5
    public static String REG6_CH2_FLIP_CURRENT = "Flip current direction - channel 2";
	//Flip current direction - channel 1 - Bit4
    public static String REG6_CH1_FLIP_CURRENT = "Flip current direction - channel 1";
	//Channel 2 lead-off detection negative inputs - Bit3
    public static String REG6_CH2_LEAD_OFF_DETECT_NEG_INPUTS = "Channel 2 lead-off detection negative inputs";
	//Channel 2 lead-off detection positive inputs - Bit2
    public static String REG6_CH2_LEAD_OFF_DETECT_POS_INPUTS = "Channel 2 lead-off detection positive inputs";
	//Channel 1 lead-off detection negative inputs - Bit1
    public static String REG6_CH1_LEAD_OFF_DETECT_NEG_INPUTS = "Channel 1 lead-off detection negative inputs";
	//Channel 1 lead-off detection positive inputs - Bit0
    public static String REG6_CH1_LEAD_OFF_DETECT_POS_INPUTS = "Channel 1 lead-off detection positive inputs";
    
    //7 - Lead-off status
    public static String REG7 = "Lead-off status";
	//Bit7 	- Must be set to '0'
	//Bit6	- Flip current direction - channel 2
    public static String REG7_CLOCK_DIVIDER_SELECTION = "Clock divider selection";
	public final static String[] ListOfClockDividerSelection={"fMOD = fCLK / 4", "fMOD = fCLK / 16"};
	public final static Integer[] ListOfClockDividerSelectionConfigValues={0,1};
	//Bit5	- Must be set to '0'
	public final static String[] ListOfConnectedState={"Connected", "Not Connected"};
	public final static Integer[] ListOfConnectedStateConfigValues={0,1};
	//Bit4	- RLD lead-off status
    public static String REG7_RLD_LEAD_OFF_STATUS = "RLD lead-off status";
	//Bit3	- Channel 2 negative electrode status
    public static String REG7_CH2_NEG_ELECTRODE_STATUS = "Channel 2 negative electrode status";
	//Bit2	- Channel 2 positive electrode status
    public static String REG7_CH2_POS_ELECTRODE_STATUS = "Channel 2 positive electrode status";
	//Bit1	- Channel 1 negative electrode status
    public static String REG7_CH1_NEG_ELECTRODE_STATUS = "Channel 1 negative electrode status";
	//Bit0	- Channel 1 positive electrode status
    public static String REG7_CH1_POS_ELECTRODE_STATUS = "Channel 1 positive electrode status";


	//8 - Respiration control register 1
    public static String REG8 = "Respiration control register 1";
    //Bit7 	- Respiration demodulation circuitry
    public static String REG8_RESPIRATION_DEMOD_CIRCUITRY = "Respiration demodulation circuitry";
    //Bit6 	- Respiration modulation circuitry
    public static String REG8_RESPIRATION_MOD_CIRCUITRY = "Respiration modulation circuitry";
    //Bits[5:2]	- Respiration Phase
    public static String REG8_RESPIRATION_PHASE = "Respiration phase";
	public final static String[] ListOfExGRespirationDetectPhase32khz={"0°","11.25°","22.5°","33.75°","45°","56.25°","67.5°","78.75°","90°","101.25°","112.5°","123.75°","135°","146.25°","157.5°","168.75°"};
	public final static Integer[] ListOfExGRespirationDetectPhase32khzConfigValues={0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15};
	public final static String[] ListOfExGRespirationDetectPhase64khz={"0°","22.5°","45°","67.5°","90°","112.5°","135°","157.5°"};
	public final static Integer[] ListOfExGRespirationDetectPhase64khzConfigValues={0,1,2,3,4,5,6,7};
    //Bit1 	- Must be set to '1'
	//Bit0	- Respiration control
    public static String REG8_RESPIRATION_CONTROL = "Respiration control";
	public final static String[] ListOfRespControl={"Internal clock", "External Clock"};
	public final static Integer[] ListOfRespControlConfigValues={0,1};
	
	//9 - Respiration control register 2
    public static String REG9 = "Respiration control register 2";
    //Bit7 	- Respiration demodulation circuitry
    public static String REG9_CALIBRATION = "Calibration on";
    //Bits[6:3] 	- Must be set to '0'
    //Bit2 	- Respiration control frequency
    public static String REG9_RESPIRATION_CONTROL_FREQUENCY = "Respiration control frequency";
	public final static String[] ListOfExGRespirationDetectFreq={"32 kHz","64 kHz"};
	public final static Integer[] ListOfExGRespirationDetectFreqConfigValues={0,1};
    //Bit1 	- RLD reference signal
    public static String REG9_RESPIRATION_REFERENCE_SIGNAL = "RLD reference signal";
	public final static String[] ListOfExGRespirationRefSignal={"Fed externally","(AVDD - AVSS) / 2"};
	public final static Integer[] ListOfExGRespirationRefSignalConfigValues={0,1};

	
	public ExGConfigBytesDetails() {
		super();
		
		mMapOfExGSettingsChip1.put(REG0_CONVERSION_MODES, new ExGConfigOptionDetails(1, 0, REG0_CONVERSION_MODES, ListOfConversionModes, ListOfConversionModeConfigValues, 7, 0x01));
        mMapOfExGSettingsChip1.put(REG0_DATA_RATE, new ExGConfigOptionDetails(1, 0, REG0_DATA_RATE, ListOfDataRates, ListOfDataRateConfigValues, 0, 0x07));
        
        mMapOfExGSettingsChip1.put(REG1_LEAD_OFF_COMPARATORS, new ExGConfigOptionDetails(1, 1, REG1_LEAD_OFF_COMPARATORS, 6));
        mMapOfExGSettingsChip1.put(REG1_REFERENCE_BUFFER, new ExGConfigOptionDetails(1, 1, REG1_REFERENCE_BUFFER, 5));
        mMapOfExGSettingsChip1.put(REG1_VOLTAGE_REFERENCE, new ExGConfigOptionDetails(1, 1, REG1_VOLTAGE_REFERENCE, ListOfVoltageReference, ListOfVoltageReferenceConfigValues, 4, 0x01));
        mMapOfExGSettingsChip1.put(REG1_OSCILLATOR_CLOCK_CONNECTION, new ExGConfigOptionDetails(1, 1, REG1_OSCILLATOR_CLOCK_CONNECTION, 3));
        mMapOfExGSettingsChip1.put(REG1_TEST_SIGNAL_SELECTION, new ExGConfigOptionDetails(1, 1, REG1_TEST_SIGNAL_SELECTION, 1));
        mMapOfExGSettingsChip1.put(REG1_TEST_SIGNAL_FREQUENCY, new ExGConfigOptionDetails(1, 1, REG1_TEST_SIGNAL_FREQUENCY, ListOfTestFreq, ListOfTestFreqConfigValues, 0, 0x01));

        mMapOfExGSettingsChip1.put(REG2_COMPARATOR_THRESHOLD, new ExGConfigOptionDetails(1, 2, REG2_COMPARATOR_THRESHOLD, ListOfLeadOffCompThres, ListOfLeadOffCompThresConfigValues, 5, 0x07));
        mMapOfExGSettingsChip1.put(REG2_LEAD_OFF_CURRENT, new ExGConfigOptionDetails(1, 2, REG2_LEAD_OFF_CURRENT, ListOfLeadOffCurrent, ListOfLeadOffCurrentConfigValues, 2, 0x03));
        mMapOfExGSettingsChip1.put(REG2_LEAD_OFF_FREQUENCY, new ExGConfigOptionDetails(1, 2, REG2_LEAD_OFF_FREQUENCY, ListOfLeadOffDetectionFreq, ListOfLeadOffDetectionFreqConfigValues, 0, 0x01));

        mMapOfExGSettingsChip1.put(REG3_CHANNEL_1_POWER_DOWN, new ExGConfigOptionDetails(1, 3, REG3_CHANNEL_1_POWER_DOWN, ListOfChannelPowerDown, ListOfChannelPowerDownConfigValues, 7, 0x01));
        mMapOfExGSettingsChip1.put(REG3_CHANNEL_1_PGA_GAIN, new ExGConfigOptionDetails(1, 3, REG3_CHANNEL_1_PGA_GAIN, ListOfExGGain, ListOfExGGainConfigValues, 4, 0x07));
        mMapOfExGSettingsChip1.put(REG3_CHANNEL_1_INPUT_SELECTION, new ExGConfigOptionDetails(1, 3, REG3_CHANNEL_1_INPUT_SELECTION, ListOfCh1InputSelection, ListOfInputSelectionConfigValues, 0, 0x07));

        mMapOfExGSettingsChip1.put(REG4_CHANNEL_2_POWER_DOWN, new ExGConfigOptionDetails(1, 4, REG4_CHANNEL_2_POWER_DOWN, ListOfChannelPowerDown, ListOfChannelPowerDownConfigValues, 7, 0x01));
        mMapOfExGSettingsChip1.put(REG4_CHANNEL_2_PGA_GAIN, new ExGConfigOptionDetails(1, 4, REG4_CHANNEL_2_PGA_GAIN, ListOfExGGain, ListOfExGGainConfigValues, 4, 0x07));
        mMapOfExGSettingsChip1.put(REG4_CHANNEL_2_INPUT_SELECTION, new ExGConfigOptionDetails(1, 4, REG4_CHANNEL_2_INPUT_SELECTION, ListOfCh2InputSelection, ListOfInputSelectionConfigValues, 0, 0x07));

        mMapOfExGSettingsChip1.put(REG5_PGA_CHOP_FREQUENCY, new ExGConfigOptionDetails(1, 5, REG5_PGA_CHOP_FREQUENCY, ListOfPgaChopFrequency, ListOfPgaChopFrequencyConfigValues, 6, 0x03));
        mMapOfExGSettingsChip1.put(REG5_RLD_BUFFER_POWER, new ExGConfigOptionDetails(1, 5, REG5_RLD_BUFFER_POWER, ListOfRldBufferPower, ListOfRldBufferPowerConfigValues, 5, 0x01));
        mMapOfExGSettingsChip1.put(REG5_RLD_LEAD_OFF_SENSE_FUNCTION, new ExGConfigOptionDetails(1, 5, REG5_RLD_LEAD_OFF_SENSE_FUNCTION, 4));
        mMapOfExGSettingsChip1.put(REG5_CH2_RLD_NEG_INPUTS, new ExGConfigOptionDetails(1, 5, REG5_CH2_RLD_NEG_INPUTS, ListOfCh2RldNegInputs, ListOfRldInputsConfigValues, 3, 0x01));
        mMapOfExGSettingsChip1.put(REG5_CH2_RLD_POS_INPUTS, new ExGConfigOptionDetails(1, 5, REG5_CH2_RLD_POS_INPUTS, ListOfCh2RldPosInputs, ListOfRldInputsConfigValues, 2, 0x01));
        mMapOfExGSettingsChip1.put(REG5_CH1_RLD_NEG_INPUTS, new ExGConfigOptionDetails(1, 5, REG5_CH1_RLD_NEG_INPUTS, ListOfCh1RldNegInputs, ListOfRldInputsConfigValues, 1, 0x01));
        mMapOfExGSettingsChip1.put(REG5_CH1_RLD_POS_INPUTS, new ExGConfigOptionDetails(1, 5, REG5_CH1_RLD_POS_INPUTS, ListOfCh1RldPosInputs, ListOfRldInputsConfigValues, 0, 0x01));

        mMapOfExGSettingsChip1.put(REG6_CH2_FLIP_CURRENT, new ExGConfigOptionDetails(1, 6, REG6_CH2_FLIP_CURRENT, 5));
        mMapOfExGSettingsChip1.put(REG6_CH1_FLIP_CURRENT, new ExGConfigOptionDetails(1, 6, REG6_CH1_FLIP_CURRENT, 4));
        mMapOfExGSettingsChip1.put(REG6_CH2_LEAD_OFF_DETECT_NEG_INPUTS, new ExGConfigOptionDetails(1, 6, REG6_CH2_LEAD_OFF_DETECT_NEG_INPUTS, 3));
        mMapOfExGSettingsChip1.put(REG6_CH2_LEAD_OFF_DETECT_POS_INPUTS, new ExGConfigOptionDetails(1, 6, REG6_CH2_LEAD_OFF_DETECT_POS_INPUTS, 2));
        mMapOfExGSettingsChip1.put(REG6_CH1_LEAD_OFF_DETECT_NEG_INPUTS, new ExGConfigOptionDetails(1, 6, REG6_CH1_LEAD_OFF_DETECT_NEG_INPUTS, 1));
        mMapOfExGSettingsChip1.put(REG6_CH1_LEAD_OFF_DETECT_POS_INPUTS, new ExGConfigOptionDetails(1, 6, REG6_CH1_LEAD_OFF_DETECT_POS_INPUTS, 0));

        mMapOfExGSettingsChip1.put(REG7_CLOCK_DIVIDER_SELECTION, new ExGConfigOptionDetails(1, 7, REG7_CLOCK_DIVIDER_SELECTION, ListOfClockDividerSelection, ListOfClockDividerSelectionConfigValues, 6, 0x01));
        mMapOfExGSettingsChip1.put(REG7_RLD_LEAD_OFF_STATUS, new ExGConfigOptionDetails(1, 7, REG7_RLD_LEAD_OFF_STATUS, ListOfConnectedState, ListOfConnectedStateConfigValues, 4, 0x01));
        mMapOfExGSettingsChip1.put(REG7_CH2_NEG_ELECTRODE_STATUS, new ExGConfigOptionDetails(1, 7, REG7_CH2_NEG_ELECTRODE_STATUS, ListOfConnectedState, ListOfConnectedStateConfigValues, 3, 0x01));
        mMapOfExGSettingsChip1.put(REG7_CH2_POS_ELECTRODE_STATUS, new ExGConfigOptionDetails(1, 7, REG7_CH2_POS_ELECTRODE_STATUS, ListOfConnectedState, ListOfConnectedStateConfigValues, 2, 0x01));
        mMapOfExGSettingsChip1.put(REG7_CH1_NEG_ELECTRODE_STATUS, new ExGConfigOptionDetails(1, 7, REG7_CH1_NEG_ELECTRODE_STATUS, ListOfConnectedState, ListOfConnectedStateConfigValues, 1, 0x01));
        mMapOfExGSettingsChip1.put(REG7_CH1_POS_ELECTRODE_STATUS, new ExGConfigOptionDetails(1, 7, REG7_CH1_POS_ELECTRODE_STATUS, ListOfConnectedState, ListOfConnectedStateConfigValues, 0, 0x01));

        mMapOfExGSettingsChip1.put(REG8_RESPIRATION_DEMOD_CIRCUITRY, new ExGConfigOptionDetails(1, 8, REG8_RESPIRATION_DEMOD_CIRCUITRY, ListOfOnOff, ListOfOnOffConfigValues, 7, 0x01));
        mMapOfExGSettingsChip1.put(REG8_RESPIRATION_MOD_CIRCUITRY, new ExGConfigOptionDetails(1, 8, REG8_RESPIRATION_MOD_CIRCUITRY, ListOfOnOff, ListOfOnOffConfigValues, 6, 0x01));
        mMapOfExGSettingsChip1.put(REG8_RESPIRATION_PHASE, new ExGConfigOptionDetails(1, 8, REG8_RESPIRATION_PHASE, ListOfExGRespirationDetectPhase32khz, ListOfExGRespirationDetectPhase32khzConfigValues, 2, 0x03));
        mMapOfExGSettingsChip1.put(REG8_RESPIRATION_CONTROL, new ExGConfigOptionDetails(1, 8, REG8_RESPIRATION_CONTROL, ListOfRespControl, ListOfRespControlConfigValues, 0, 0x01));

        mMapOfExGSettingsChip1.put(REG9_CALIBRATION, new ExGConfigOptionDetails(1, 9, REG9_CALIBRATION, ListOfOnOff, ListOfOnOffConfigValues, 7, 0x01));
        mMapOfExGSettingsChip1.put(REG9_RESPIRATION_CONTROL_FREQUENCY, new ExGConfigOptionDetails(1, 9, REG9_RESPIRATION_CONTROL_FREQUENCY, ListOfExGRespirationDetectFreq, ListOfExGRespirationDetectFreqConfigValues, 2, 0x01));
        mMapOfExGSettingsChip1.put(REG9_RESPIRATION_REFERENCE_SIGNAL, new ExGConfigOptionDetails(1, 9, REG9_RESPIRATION_CONTROL_FREQUENCY, ListOfExGRespirationDetectFreq, ListOfExGRespirationDetectFreqConfigValues, 0, 0x01));

        for(String key:mMapOfExGSettingsChip1.keySet()){
        	mMapOfExGSettingsChip2.put(key, mMapOfExGSettingsChip1.get(key));
        	mMapOfExGSettingsChip2.get(key).chipIndex = 2;
        }
	}

	
//	public static final Map<String, ExGSettingDetails> mMapOfExGConfigOptions;
//    static {
//        Map<String, ExGSettingDetails> aMap = new TreeMap<String, ExGSettingDetails>();
//        
//        aMap.put(REG0_CONVERSION_MODES, new ExGSettingDetails(1, 0, REG0_CONVERSION_MODES, ListOfConversionModes, ListOfConversionModeConfigValues, 7, 0x01));
//        aMap.put(REG0_DATA_RATE, new ExGSettingDetails(1, 0, REG0_DATA_RATE, ListOfDataRates, ListOfDataRateConfigValues, 0, 0x07));
//        
//        aMap.put(REG1_LEAD_OFF_COMPARATORS, new ExGSettingDetails(1, 1, REG1_LEAD_OFF_COMPARATORS, 6));
//        aMap.put(REG1_REFERENCE_BUFFER, new ExGSettingDetails(1, 1, REG1_REFERENCE_BUFFER, 5));
//        aMap.put(REG1_VOLTAGE_REFERENCE, new ExGSettingDetails(1, 1, REG1_VOLTAGE_REFERENCE, ListOfVoltageReference, ListOfVoltageReferenceConfigValues, 4, 0x01));
//        aMap.put(REG1_OSCILLATOR_CLOCK_CONNECTION, new ExGSettingDetails(1, 1, REG1_OSCILLATOR_CLOCK_CONNECTION, 3));
//        aMap.put(REG1_TEST_SIGNAL_SELECTION, new ExGSettingDetails(1, 1, REG1_TEST_SIGNAL_SELECTION, 1));
//        aMap.put(REG1_TEST_SIGNAL_FREQUENCY, new ExGSettingDetails(1, 1, REG1_TEST_SIGNAL_FREQUENCY, ListOfTestFreq, ListOfTestFreqConfigValues, 0, 0x01));
//
//        aMap.put(REG2_COMPARATOR_THRESHOLD, new ExGSettingDetails(1, 2, REG2_COMPARATOR_THRESHOLD, ListOfLeadOffCompThres, ListOfLeadOffCompThresConfigValues, 5, 0x07));
//        aMap.put(REG2_LEAD_OFF_CURRENT, new ExGSettingDetails(1, 2, REG2_LEAD_OFF_CURRENT, ListOfLeadOffCurrent, ListOfLeadOffCurrentConfigValues, 2, 0x03));
//        aMap.put(REG2_LEAD_OFF_FREQUENCY, new ExGSettingDetails(1, 2, REG2_LEAD_OFF_FREQUENCY, ListOfLeadOffDetectionFreq, ListOfLeadOffDetectionFreqConfigValues, 0, 0x01));
//
//        aMap.put(REG3_CHANNEL_1_POWER_DOWN, new ExGSettingDetails(1, 3, REG3_CHANNEL_1_POWER_DOWN, ListOfChannelPowerDown, ListOfChannelPowerDownConfigValues, 7, 0x01));
//        aMap.put(REG3_CHANNEL_1_PGA_GAIN, new ExGSettingDetails(1, 3, REG3_CHANNEL_1_PGA_GAIN, ListOfExGGain, ListOfExGGainConfigValues, 4, 0x07));
//        aMap.put(REG3_CHANNEL_1_INPUT_SELECTION, new ExGSettingDetails(1, 3, REG3_CHANNEL_1_INPUT_SELECTION, ListOfCh1InputSelection, ListOfInputSelectionConfigValues, 0, 0x07));
//
//        aMap.put(REG4_CHANNEL_2_POWER_DOWN, new ExGSettingDetails(1, 4, REG4_CHANNEL_2_POWER_DOWN, ListOfChannelPowerDown, ListOfChannelPowerDownConfigValues, 7, 0x01));
//        aMap.put(REG4_CHANNEL_2_PGA_GAIN, new ExGSettingDetails(1, 4, REG4_CHANNEL_2_PGA_GAIN, ListOfExGGain, ListOfExGGainConfigValues, 4, 0x07));
//        aMap.put(REG4_CHANNEL_2_INPUT_SELECTION, new ExGSettingDetails(1, 4, REG4_CHANNEL_2_INPUT_SELECTION, ListOfCh2InputSelection, ListOfInputSelectionConfigValues, 0, 0x07));
//
//        aMap.put(REG5_PGA_CHOP_FREQUENCY, new ExGSettingDetails(1, 5, REG5_PGA_CHOP_FREQUENCY, ListOfPgaChopFrequency, ListOfPgaChopFrequencyConfigValues, 6, 0x03));
//        aMap.put(REG5_RLD_BUFFER_POWER, new ExGSettingDetails(1, 5, REG5_RLD_BUFFER_POWER, ListOfRldBufferPower, ListOfRldBufferPowerConfigValues, 5, 0x01));
//        aMap.put(REG5_RLD_LEAD_OFF_SENSE_FUNCTION, new ExGSettingDetails(1, 5, REG5_RLD_LEAD_OFF_SENSE_FUNCTION, 4));
//        aMap.put(REG5_CH2_RLD_NEG_INPUTS, new ExGSettingDetails(1, 5, REG5_CH2_RLD_NEG_INPUTS, ListOfCh2RldNegInputs, ListOfRldInputsConfigValues, 3, 0x01));
//        aMap.put(REG5_CH2_RLD_POS_INPUTS, new ExGSettingDetails(1, 5, REG5_CH2_RLD_POS_INPUTS, ListOfCh2RldPosInputs, ListOfRldInputsConfigValues, 2, 0x01));
//        aMap.put(REG5_CH1_RLD_NEG_INPUTS, new ExGSettingDetails(1, 5, REG5_CH1_RLD_NEG_INPUTS, ListOfCh1RldNegInputs, ListOfRldInputsConfigValues, 1, 0x01));
//        aMap.put(REG5_CH1_RLD_POS_INPUTS, new ExGSettingDetails(1, 5, REG5_CH1_RLD_POS_INPUTS, ListOfCh1RldPosInputs, ListOfRldInputsConfigValues, 0, 0x01));
//
//        aMap.put(REG6_CH2_FLIP_CURRENT, new ExGSettingDetails(1, 6, REG6_CH2_FLIP_CURRENT, 5));
//        aMap.put(REG6_CH1_FLIP_CURRENT, new ExGSettingDetails(1, 6, REG6_CH1_FLIP_CURRENT, 4));
//        aMap.put(REG6_CH2_LEAD_OFF_DETECT_NEG_INPUTS, new ExGSettingDetails(1, 6, REG6_CH2_LEAD_OFF_DETECT_NEG_INPUTS, 3));
//        aMap.put(REG6_CH2_LEAD_OFF_DETECT_POS_INPUTS, new ExGSettingDetails(1, 6, REG6_CH2_LEAD_OFF_DETECT_POS_INPUTS, 2));
//        aMap.put(REG6_CH1_LEAD_OFF_DETECT_NEG_INPUTS, new ExGSettingDetails(1, 6, REG6_CH1_LEAD_OFF_DETECT_NEG_INPUTS, 1));
//        aMap.put(REG6_CH1_LEAD_OFF_DETECT_POS_INPUTS, new ExGSettingDetails(1, 6, REG6_CH1_LEAD_OFF_DETECT_POS_INPUTS, 0));
//
//        aMap.put(REG7_CLOCK_DIVIDER_SELECTION, new ExGSettingDetails(1, 7, REG7_CLOCK_DIVIDER_SELECTION, ListOfClockDividerSelection, ListOfClockDividerSelectionConfigValues, 6, 0x01));
//        aMap.put(REG7_RLD_LEAD_OFF_STATUS, new ExGSettingDetails(1, 7, REG7_RLD_LEAD_OFF_STATUS, ListOfConnectedState, ListOfConnectedStateConfigValues, 4, 0x01));
//        aMap.put(REG7_CH2_NEG_ELECTRODE_STATUS, new ExGSettingDetails(1, 7, REG7_CH2_NEG_ELECTRODE_STATUS, ListOfConnectedState, ListOfConnectedStateConfigValues, 3, 0x01));
//        aMap.put(REG7_CH2_POS_ELECTRODE_STATUS, new ExGSettingDetails(1, 7, REG7_CH2_POS_ELECTRODE_STATUS, ListOfConnectedState, ListOfConnectedStateConfigValues, 2, 0x01));
//        aMap.put(REG7_CH1_NEG_ELECTRODE_STATUS, new ExGSettingDetails(1, 7, REG7_CH1_NEG_ELECTRODE_STATUS, ListOfConnectedState, ListOfConnectedStateConfigValues, 1, 0x01));
//        aMap.put(REG7_CH1_POS_ELECTRODE_STATUS, new ExGSettingDetails(1, 7, REG7_CH1_POS_ELECTRODE_STATUS, ListOfConnectedState, ListOfConnectedStateConfigValues, 0, 0x01));
//
//        aMap.put(REG8_RESPIRATION_DEMOD_CIRCUITRY, new ExGSettingDetails(1, 8, REG8_RESPIRATION_DEMOD_CIRCUITRY, ListOfOnOff, ListOfOnOffConfigValues, 7, 0x01));
//        aMap.put(REG8_RESPIRATION_MOD_CIRCUITRY, new ExGSettingDetails(1, 8, REG8_RESPIRATION_MOD_CIRCUITRY, ListOfOnOff, ListOfOnOffConfigValues, 6, 0x01));
//        aMap.put(REG8_RESPIRATION_PHASE, new ExGSettingDetails(1, 8, REG8_RESPIRATION_PHASE, ListOfExGRespirationDetectPhase32khz, ListOfExGRespirationDetectPhase32khzConfigValues, 2, 0x03));
//        aMap.put(REG8_RESPIRATION_CONTROL, new ExGSettingDetails(1, 8, REG8_RESPIRATION_CONTROL, ListOfRespControl, ListOfRespControlConfigValues, 0, 0x01));
//
//        aMap.put(REG9_CALIBRATION, new ExGSettingDetails(1, 9, REG9_CALIBRATION, ListOfOnOff, ListOfOnOffConfigValues, 7, 0x01));
//        aMap.put(REG9_RESPIRATION_CONTROL_FREQUENCY, new ExGSettingDetails(1, 9, REG9_RESPIRATION_CONTROL_FREQUENCY, ListOfExGRespirationDetectFreq, ListOfExGRespirationDetectFreqConfigValues, 2, 0x01));
//        aMap.put(REG9_RESPIRATION_REFERENCE_SIGNAL, new ExGSettingDetails(1, 9, REG9_RESPIRATION_CONTROL_FREQUENCY, ListOfExGRespirationDetectFreq, ListOfExGRespirationDetectFreqConfigValues, 0, 0x01));
//
//        mMapOfExGConfigOptions = Collections.unmodifiableMap(aMap);
//    }	
    
    
    public byte[] generateExgByteArray(int chipNumber){
    	byte[] byteArray = new byte[10];
    	HashMap<String, ExGConfigOptionDetails> mMapOfExGSettingsToRef;
    	if(chipNumber==2){
    		mMapOfExGSettingsToRef = mMapOfExGSettingsChip2;
    	}
    	else{
    		mMapOfExGSettingsToRef = mMapOfExGSettingsChip1;
    	}
    	
    	for(ExGConfigOptionDetails eSD:mMapOfExGSettingsToRef.values()){
    		if(eSD.settingType==SettingType.combobox){
        		byteArray[eSD.byteIndex] |= ((eSD.valueInt & eSD.mask) << eSD.bitShift);
    		}
    		else if(eSD.settingType==SettingType.checkbox){
        		byteArray[eSD.byteIndex] |= (((eSD.valueBool? 1:0) & eSD.mask) << eSD.bitShift);
    		} 
    	}
    	
    	//Set the 'Must be' bits listed in the manual
    	//CONFIG1:
    	byteArray[0] &= ~(BIT6+BIT5+BIT4+BIT3);
    	//CONFIG2:
    	byteArray[1] |= BIT7;
    	byteArray[1] &= ~(BIT2);
    	//LOFF:
    	byteArray[2] |= BIT4;
    	byteArray[2] &= ~(BIT1);
    	//LOFF_SENS:
    	byteArray[6] &= ~(BIT7+BIT6);
    	//LOFF_STAT:
    	byteArray[7] &= ~(BIT7+BIT5);
    	//RESP1:
    	byteArray[8] |= BIT1;
    	//RESP2:
    	byteArray[9] &= ~(BIT6+BIT5+BIT4+BIT3);
    	byteArray[9] |= BIT0;
    	
		return byteArray;
    }
	
}
