package com.shimmerresearch.driver;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

public class ExGConfigDetails {
	
	//TODO switch to map/list of objects instead of declaring multiple variables
	//http://www.ti.com/lit/ds/sbas502b/sbas502b.pdf
	
	
	public final static String[] ListOfECGReferenceElectrode={"Inverse Wilson CT","Fixed Potential"};
	public final static Integer[] ListOfECGReferenceElectrodeConfigValues={13,0};
	
	public final static String[] ListOfEMGReferenceElectrode={"Fixed Potential", "Inverse of Ch1"};
	public final static Integer[] ListOfEMGReferenceElectrodeConfigValues={0,3};
	
	public final static String[] ListOfExGResolutions={"16-bit","24-bit"};
	public final static Integer[] ListOfExGResolutionsConfigValues={0,1};

	public final static String[] ListOfExGRespirationDetectFreq={"32 kHz","64 kHz"};
	public final static Integer[] ListOfExGRespirationDetectFreqConfigValues={0,1};
	
	public final static String[] ListOfExGRespirationDetectPhase32khz={"0°","11.25°","22.5°","33.75°","45°","56.25°","67.5°","78.75°","90°","101.25°","112.5°","123.75°","135°","146.25°","157.5°","168.75°"};
	public final static Integer[] ListOfExGRespirationDetectPhase32khzConfigValues={0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15};
	
	public final static String[] ListOfExGRespirationDetectPhase64khz={"0°","22.5°","45°","67.5°","90°","112.5°","135°","157.5°"};
	public final static Integer[] ListOfExGRespirationDetectPhase64khzConfigValues={0,1,2,3,4,5,6,7};

	public final static String[] ListOfExGRate={"125 Hz","250 Hz","500 Hz","1 kHz","2 kHz","4 kHz","8 kHz"};
	public final static Integer[] ListOfExGRateConfigValues={0,1,2,3,4,5,6};

	public final static String[] ListOfOnOff={"On","Off"};
	public final static Integer[] ListOfOnOffConfigValues={0x01,0x00};
	

	public static enum SettingType{
		combobox,
		checkbox
	}

	
	//0 - Configuration Register 1
    public static String CH1REG0 = "Configuration Register 1";
	//Conversion Mode - Bit7
    public static String CH1REG0_CONVERSION_MODES = "Conversion Mode";
	public final static String[] ListOfConversionModes = {"Continuous Conversion Mode", "Single-shot mode"};
	public final static Integer[] ListOfConversionModeConfigValues = {0,1};
	//Must be set to '0' - Bits[6:3]
	//Data rate - Bits[2:0]
    public static String CH1REG0_DATA_RATE = "Data Rate";
	public final static String[] ListOfDataRates = {"125 SPS", "250 SPS", "500 SPS", "1 kSPS", "2 kSPS", "4 kSPS", "8 kSPS"};
	public final static Integer[] ListOfDataRateConfigValues = {0,1,2,3,4,5,6};
	

	//1 - Configuration Register 2
    public static String CH1REG1 = "Configuration Register 2";
	//Must be set to '1' - Bit7
	//Lead-off comparators - Bit 6
    public static String CH1REG1_LEAD_OFF_COMPARATORS = "Lead-off comparators";
//	public int leadOffComparators = 0;
//	public int leadOffComparatorsBitShift = 6;
//	public int leadOffComparatorsMask = 0x01;
	//Reference buffer - Bit5
    public static String CH1REG1_REFERENCE_BUFFER = "Reference buffer";
//	public int referenceBuffer = 0;
//	public int referenceBufferBitShift = 5;
//	public int referenceBufferMask = 0x01;
	//Voltage reference - Bit4
    public static String CH1REG1_VOLTAGE_REFERENCE = "Voltage reference";
	public final static String[] ListOfVoltageReference = {"2.42 V", "4.033 V"};
	public final static Integer[] ListOfVoltageReferenceConfigValues = {0,1};
//	public int voltageReference = 0;
//	public int voltageReferenceBitShift = 4;
//	public int voltageReferenceMask = 0x01;
	//Oscillator clock connection
    public static String CH1REG1_OSCILLATOR_CLOCK_CONNECTION = "Oscillator clock connection";
//	public int oscillatorClock = 0;
//	public int oscillatorClockBitShift = 3;
//	public int oscillatorClockMask = 0x01;
	//Must be set to '0' - Bit2
	//Test signal selection - Bit1
    public static String CH1REG1_TEST_SIGNAL_SELECTION = "Test signal selection";
//	public int testSignal = 0;
//	public int testSignalBitShift = 1;
//	public int testSignalMask = 0x01;
	//Test signal frequency - Bit 0
    public static String CH1REG1_TEST_SIGNAL_FREQUENCY = "Test signal frequency";
	public final static String[] ListOfTestFreq = {"DC", "1 kHz Square Wave"};
	public final static Integer[] ListOfTestFreqConfigValues = {0,1};
//	public int testFreq = 1;
//	public int testFreqBitShift = 0;
//	public int testFreqMask = 0x01;
    
	//2 - Lead-Off Control Register
    public static String CH1REG2 = "Lead-Off Control Register";
	//Comparator threshold - Bits[7:5]
    public static String CH1REG2_COMPARATOR_THRESHOLD = "Comparator threshold";
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
//	public int leadOffCompThres = 0;
//	public int leadOffCompThresBitShift = 5;
//	public int leadOffCompThresMask = 0x01;
	//Must be set to '1' - Bit4
	//Lead-off current - Bits[3:2]
    public static String CH1REG2_LEAD_OFF_CURRENT = "Lead-off current";
	public final static String[] ListOfLeadOffCurrent={"6 nA","22 nA", "6 uA", "22 uA"};
	public final static Integer[] ListOfLeadOffCurrentConfigValues={0,1,2,3};
//	public int leadOffCurrent = 0;
//	public int leadOffCurrentBitShift = 2;
//	public int leadOffCurrentFreqMask = 0x01;
	//Must be set to '0' - Bit1
	//Lead-off frequency - Bit0
    public static String CH1REG2_LEAD_OFF_FREQUENCY = "Lead-off frequency";
	public final static String[] ListOfLeadOffDetectionFreq={"DC lead-off detect", "AC lead-off detect (fs / 4)"};
	public final static Integer[] ListOfLeadOffDetectionFreqConfigValues={0,1};
//	public int leadOffDetectionFreq = 0;
//	public int leadOffDetectionFreqBitShift = 0;
//	public int leadOffDetectionFreqMask = 0x01;
	
	//3 - Channel 1 settings
	//4 - Channel 2 settings
    public static String CH1REG3 = "Channel 1 settings";
    public static String CH1REG4 = "Channel 2 settings";
	//Channel power-down - Bit7
    public static String CH1REG3_CHANNEL_1_POWER_DOWN = "Channel 1 power-down";
    public static String CH1REG4_CHANNEL_2_POWER_DOWN = "Channel 2 power-down";
	public final static String[] ListOfChannelPowerDown={"Normal operation","Power-down"};
	public final static Integer[] ListOfChannelPowerDownConfigValues={0,1};
	//PGA Gain - Bits[6:4]
    public static String CH1REG3_CHANNEL_1_PGA_GAIN = "Channel 1 PGA Gain";
    public static String CH1REG4_CHANNEL_2_PGA_GAIN = "Channel 2 PGA Gain";
	public final static String[] ListOfExGGain={"6","1","2","3","4","8","12"};
	public final static Integer[] ListOfExGGainConfigValues={0,1,2,3,4,5,6};
	//Input selection - Bits[3:0]
    public static String CH1REG3_CHANNEL_1_INPUT_SELECTION = "Channel 1 Input Selection";
    public static String CH1REG4_CHANNEL_2_INPUT_SELECTION = "Channel 2 Input Selection";
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


//	5 - Right leg drive sense selection
//	PGA chop frequency
//	0,1,2,3
//	fMOD / 16, Reserved, fMOD / 2, fMOD / 4
//	
//	RLD buffer power
//	0,1
//	Powered down, Enabled
//	
//	RLD lead-off sense function
//	Boolean
//	
//	Channel 2 RLD negative inputs
//	0,1
//	Not connected,Connected to IN2N
//	
//	Channel 2 RLD positive inputs
//	0,1
//	Not connected,Connected to IN2P
//	
//	Channel 1 RLD negative inputs
//	0,1
//	Not connected,Connected to IN1N
//	
//	Channel 1 RLD positive inputs
//	0,1
//	Not connected,Connected to IN1P
	
	//5 - Right leg drive sense selection
    public static String CH1REG5 = "Right leg drive sense selection";
	//PGA chop frequency - Bits[7:6]
    public static String CH1REG5_PGA_CHOP_FREQUENCY = "PGA chop frequency";
	public final static String[] ListOfPgaChopFrequency={"fMOD / 16, Reserved", "fMOD / 2", "fMOD / 4"};
	public final static Integer[] ListOfPgaChopFrequencyConfigValues={0,1,2,3};
	//RLD buffer power - Bit5
    public static String CH1REG5_RLD_BUFFER_POWER = "RLD buffer power";
	//RLD lead-off sense function - Bit4
    public static String CH1REG5_RLD_LEAD_OFF_SENSE_FUNCTION = "RLD lead-off sense function";
	//Channel 2 RLD negative inputs - Bit3
    public static String CH1REG5_CH2_RLD_NEG_INPUTS = "Channel 2 RLD negative inputs";
	//Channel 2 RLD positive inputs - Bit2
    public static String CH1REG5_CH2_RLD_POS_INPUTS = "Channel 2 RLD positive inputs";
	//Channel 1 RLD negative inputs - Bit1
    public static String CH1REG5_CH1_RLD_NEG_INPUTS = "Channel 1 RLD negative inputs";
	//Channel 1 RLD positive inputs - Bit0
    public static String CH1REG5_CH1_RLD_POS_INPUTS = "Channel 1 RLD positive inputs";
	
//	6 - Lead-off sense selection
//	Flip current direction - channel 2
//	Boolean
//	
//	Flip current direction - channel 1
//	Boolean
//	
//	Channel 2 lead-off detection negative inputs
//	Boolean
//	
//	Channel 2 lead-off detection positive inputs
//	Boolean
//	
//	Channel 1 lead-off detection negative inputs
//	Boolean
//	
//	Channel 1 lead-off detection positive inputs
//	Boolean

	//5 - Right leg drive sense selection
    public static String CH1REG6 = "Lead-off sense selection";
	//Must be set to '0' - Bits[7:6]
	//Flip current direction - channel 2 - Bit5
    public static String CH1REG6_CH2_FLIP_CURRENT = "Flip current direction - channel 2";
	//Flip current direction - channel 1 - Bit4
    public static String CH1REG6_CH1_FLIP_CURRENT = "Flip current direction - channel 1";
	//Channel 2 lead-off detection negative inputs - Bit3
    public static String CH1REG6_CH2_LEAD_OFF_DETECT_NEG_INPUTS = "Channel 2 lead-off detection negative inputs";
	//Channel 2 lead-off detection positive inputs - Bit2
    public static String CH1REG6_CH2_LEAD_OFF_DETECT_POS_INPUTS = "Channel 2 lead-off detection positive inputs";
	//Channel 1 lead-off detection negative inputs - Bit1
    public static String CH1REG6_CH1_LEAD_OFF_DETECT_NEG_INPUTS = "Channel 1 lead-off detection negative inputs";
	//Channel 1 lead-off detection positive inputs - Bit0
    public static String CH1REG6_CH1_LEAD_OFF_DETECT_POS_INPUTS = "Channel 1 lead-off detection positive inputs";
    
	public static final Map<String, ExGSettingDetails> mMapOfExGConfigOptions;
    static {
        Map<String, ExGSettingDetails> aMap = new TreeMap<String, ExGSettingDetails>();
        
        aMap.put(CH1REG0_CONVERSION_MODES, new ExGSettingDetails(1, 0, CH1REG0_CONVERSION_MODES, ListOfConversionModes, ListOfConversionModeConfigValues, 7, 0x01));
        aMap.put(CH1REG0_DATA_RATE, new ExGSettingDetails(1, 0, CH1REG0_DATA_RATE, ListOfDataRates, ListOfDataRateConfigValues, 0, 0x07));
        
        aMap.put(CH1REG1_LEAD_OFF_COMPARATORS, new ExGSettingDetails(1, 1, CH1REG1_LEAD_OFF_COMPARATORS, 6));
        aMap.put(CH1REG1_REFERENCE_BUFFER, new ExGSettingDetails(1, 1, CH1REG1_REFERENCE_BUFFER, 5));
        aMap.put(CH1REG1_VOLTAGE_REFERENCE, new ExGSettingDetails(1, 1, CH1REG1_VOLTAGE_REFERENCE, ListOfVoltageReference, ListOfVoltageReferenceConfigValues, 4, 0x01));
        aMap.put(CH1REG1_OSCILLATOR_CLOCK_CONNECTION, new ExGSettingDetails(1, 1, CH1REG1_OSCILLATOR_CLOCK_CONNECTION, 3));
        aMap.put(CH1REG1_TEST_SIGNAL_SELECTION, new ExGSettingDetails(1, 1, CH1REG1_TEST_SIGNAL_SELECTION, 1));
        aMap.put(CH1REG1_TEST_SIGNAL_FREQUENCY, new ExGSettingDetails(1, 1, CH1REG1_TEST_SIGNAL_FREQUENCY, ListOfTestFreq, ListOfTestFreqConfigValues, 0, 0x01));

        aMap.put(CH1REG2_COMPARATOR_THRESHOLD, new ExGSettingDetails(1, 2, CH1REG2_COMPARATOR_THRESHOLD, ListOfLeadOffCompThres, ListOfLeadOffCompThresConfigValues, 5, 0x07));
        aMap.put(CH1REG2_LEAD_OFF_CURRENT, new ExGSettingDetails(1, 2, CH1REG2_LEAD_OFF_CURRENT, ListOfLeadOffCurrent, ListOfLeadOffCurrentConfigValues, 2, 0x03));
        aMap.put(CH1REG2_LEAD_OFF_FREQUENCY, new ExGSettingDetails(1, 2, CH1REG2_LEAD_OFF_FREQUENCY, ListOfLeadOffDetectionFreq, ListOfLeadOffDetectionFreqConfigValues, 0, 0x01));

        aMap.put(CH1REG3_CHANNEL_1_POWER_DOWN, new ExGSettingDetails(1, 3, CH1REG3_CHANNEL_1_POWER_DOWN, ListOfChannelPowerDown, ListOfChannelPowerDownConfigValues, 7, 0x01));
        aMap.put(CH1REG3_CHANNEL_1_PGA_GAIN, new ExGSettingDetails(1, 3, CH1REG3_CHANNEL_1_PGA_GAIN, ListOfExGGain, ListOfExGGainConfigValues, 4, 0x07));
        aMap.put(CH1REG3_CHANNEL_1_INPUT_SELECTION, new ExGSettingDetails(1, 3, CH1REG3_CHANNEL_1_INPUT_SELECTION, ListOfCh1InputSelection, ListOfInputSelectionConfigValues, 0, 0x07));

        aMap.put(CH1REG4_CHANNEL_2_POWER_DOWN, new ExGSettingDetails(1, 4, CH1REG4_CHANNEL_2_POWER_DOWN, ListOfChannelPowerDown, ListOfChannelPowerDownConfigValues, 7, 0x01));
        aMap.put(CH1REG4_CHANNEL_2_PGA_GAIN, new ExGSettingDetails(1, 4, CH1REG4_CHANNEL_2_PGA_GAIN, ListOfExGGain, ListOfExGGainConfigValues, 4, 0x07));
        aMap.put(CH1REG4_CHANNEL_2_INPUT_SELECTION, new ExGSettingDetails(1, 4, CH1REG4_CHANNEL_2_INPUT_SELECTION, ListOfCh2InputSelection, ListOfInputSelectionConfigValues, 0, 0x07));

        aMap.put(CH1REG5_PGA_CHOP_FREQUENCY, new ExGSettingDetails(1, 5, CH1REG5_PGA_CHOP_FREQUENCY, ListOfPgaChopFrequency, ListOfPgaChopFrequencyConfigValues, 6, 0x03));
        aMap.put(CH1REG5_RLD_BUFFER_POWER, new ExGSettingDetails(1, 5, CH1REG5_RLD_BUFFER_POWER, 5));
        aMap.put(CH1REG5_RLD_LEAD_OFF_SENSE_FUNCTION, new ExGSettingDetails(1, 5, CH1REG5_RLD_LEAD_OFF_SENSE_FUNCTION, 4));
        aMap.put(CH1REG5_CH2_RLD_NEG_INPUTS, new ExGSettingDetails(1, 5, CH1REG5_CH2_RLD_NEG_INPUTS, 3));
        aMap.put(CH1REG5_CH2_RLD_POS_INPUTS, new ExGSettingDetails(1, 5, CH1REG5_CH2_RLD_POS_INPUTS, 2));
        aMap.put(CH1REG5_CH1_RLD_NEG_INPUTS, new ExGSettingDetails(1, 5, CH1REG5_CH1_RLD_NEG_INPUTS, 1));
        aMap.put(CH1REG5_CH1_RLD_POS_INPUTS, new ExGSettingDetails(1, 5, CH1REG5_CH1_RLD_POS_INPUTS, 0));

        aMap.put(CH1REG6_CH2_FLIP_CURRENT, new ExGSettingDetails(1, 6, CH1REG6_CH2_FLIP_CURRENT, 5));
        aMap.put(CH1REG6_CH1_FLIP_CURRENT, new ExGSettingDetails(1, 6, CH1REG6_CH1_FLIP_CURRENT, 4));
        aMap.put(CH1REG6_CH2_LEAD_OFF_DETECT_NEG_INPUTS, new ExGSettingDetails(1, 6, CH1REG6_CH2_LEAD_OFF_DETECT_NEG_INPUTS, 3));
        aMap.put(CH1REG6_CH2_LEAD_OFF_DETECT_POS_INPUTS, new ExGSettingDetails(1, 6, CH1REG6_CH2_LEAD_OFF_DETECT_POS_INPUTS, 2));
        aMap.put(CH1REG6_CH1_LEAD_OFF_DETECT_NEG_INPUTS, new ExGSettingDetails(1, 6, CH1REG6_CH1_LEAD_OFF_DETECT_NEG_INPUTS, 1));
        aMap.put(CH1REG6_CH1_LEAD_OFF_DETECT_POS_INPUTS, new ExGSettingDetails(1, 6, CH1REG6_CH1_LEAD_OFF_DETECT_POS_INPUTS, 0));

        mMapOfExGConfigOptions = Collections.unmodifiableMap(aMap);
    }
	
//	7 - Lead-off status
//	Clock divider selection
//	0,1
//	fMOD = fCLK / 4, fMOD = fCLK / 16
//	
//	RLD lead-off status
//	0,1
//	RLD is connected, RLD is not connected
//	
//	Channel 2 negative electrode status
//	0,1
//	Connected, Not Connected
//	
//	Channel 2 positive electrode status
//	0,1
//	Connected, Not Connected
//	
//	Channel 1 negative electrode status
//	0,1
//	Connected, Not Connected
//	
//	Channel 1 positive electrode status
//	0,1
//	Connected, Not Connected
	
	
//	8 - Respiration control register
//	Respiration demodulation circuitry
//	0,1
//	Off, On
//	
//	Respiration modulation circuitry
//	0,1
//	Off, On
//
//	Respiration Detect Phase - Chip 1
//	4,8,C, 10,14,18,1C,20,24,28,2C,30,34,38,3C
//	0°, 11.25°, 22.5°, 33.75°, 45°, 56.25°, 67.5°, 78.75°, 90°, 101.25°, 112.5°, 123.75°, 135°, 146.25°, 157.5°, 168.75°
	
	
//	9 - Respiration control register 2
//	Calibration
//	0,1
//	Off, On
//	
//	Respiration control frequency
//	0,1
//	32 kHz, 64 kHz
//	
//	RLDREF signal
//	0,1
//	Fed externally, (AVDD - AVSS) / 2
	
	
}
