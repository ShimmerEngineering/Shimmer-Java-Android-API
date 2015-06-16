package com.shimmerresearch.exgConfig;

import java.io.Serializable;
import java.util.HashMap;

import com.shimmerresearch.exgConfig.ExGConfigOptionDetails.CHIP_INDEX;
import com.shimmerresearch.exgConfig.ExGConfigOptionDetails.SettingType;

public class ExGConfigBytesDetails implements Serializable {
	
	//http://www.ti.com/lit/ds/sbas502b/sbas502b.pdf
	
	public HashMap<String, ExGConfigOptionDetails> mMapOfExGSettingsChip1 = new HashMap<String, ExGConfigOptionDetails>();
	public HashMap<String, ExGConfigOptionDetails> mMapOfExGSettingsChip2 = new HashMap<String, ExGConfigOptionDetails>();
	
	private int BIT0 = 0x01;
	private int BIT1 = 0x02;
	private int BIT2 = 0x04;
	private int BIT3 = 0x08;
	private int BIT4 = 0x10;
	private int BIT5 = 0x20;
	private int BIT6 = 0x40;
	private int BIT7 = 0x80;
	
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
    public static final String REG1 = "Configuration Register 1";
	//Bit7	- Conversion Mode
    public static final String REG1_CONVERSION_MODES = "Conversion Mode";
	public static final String[] ListOfConversionModes = {"Continuous Conversion Mode", "Single-shot mode"};
	public final static Integer[] ListOfConversionModeConfigValues = {0,1};
	//Bits[6:3]	- Must be set to '0'
	//Bits[2:0]	- Data rate
    public static final String REG1_DATA_RATE = "Data Rate";
	public static final String[] ListOfDataRates = {"125 SPS", "250 SPS", "500 SPS", "1 kSPS", "2 kSPS", "4 kSPS", "8 kSPS"};
	public final static Integer[] ListOfDataRateConfigValues = {0,1,2,3,4,5,6};

	//1 - Configuration Register 2
    public static final String REG2 = "Configuration Register 2";
	//Bit7	- Must be set to '1' 
	//Bit6	- Lead-off comparators
    public static final String REG2_LEAD_OFF_COMPARATORS = "Lead-off comparators";
	//Bit5	- Reference buffer
    public static final String REG2_REFERENCE_BUFFER = "Reference buffer";
	//Bit4	- Voltage reference
    public static final String REG2_VOLTAGE_REFERENCE = "Voltage reference";
	public static final String[] ListOfVoltageReference = {"2.42 V", "4.033 V"};
	public final static Integer[] ListOfVoltageReferenceConfigValues = {0,1};
	//Bit3	- Oscillator clock connection
    public static final String REG2_OSCILLATOR_CLOCK_CONNECTION = "Oscillator clock connection";
	//Bit2	- Must be set to '0'
	//Bit1	- Test signal selection
    public static final String REG2_TEST_SIGNAL_SELECTION = "Test signal selection";
	//Bit0	- Test signal frequency
    public static final String REG2_TEST_SIGNAL_FREQUENCY = "Test signal frequency";
	public static final String[] ListOfTestFreq = {"DC", "1 kHz Square Wave"};
	public final static Integer[] ListOfTestFreqConfigValues = {0,1};
    
	//2 - Lead-Off Control Register
    public static final String REG3 = "Lead-Off Control Register";
	//Comparator threshold - Bits[7:5]
    public static final String REG3_COMPARATOR_THRESHOLD = "Comparator threshold";
	public static final String[] ListOfLeadOffCompThres={
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
    public static final String REG3_LEAD_OFF_CURRENT = "Lead-off current";
	public static final String[] ListOfLeadOffCurrent={"6 nA","22 nA", "6 uA", "22 uA"};
	public final static Integer[] ListOfLeadOffCurrentConfigValues={0,1,2,3};
	//Bit1	- Must be set to '0'
	//Bit0	- Lead-off frequency
    public static final String REG3_LEAD_OFF_FREQUENCY = "Lead-off frequency";
	public static final String[] ListOfLeadOffDetectionFreq={"DC lead-off detect", "AC lead-off detect (fs / 4)"};
	public final static Integer[] ListOfLeadOffDetectionFreqConfigValues={0,1};
	
	//3 - Channel 1 settings
	//4 - Channel 2 settings
    public static final String REG4 = "Channel 1 settings";
    public static final String REG5 = "Channel 2 settings";
	//Channel power-down - Bit7
    public static final String REG4_CHANNEL_1_POWER_DOWN = "Channel 1 power-down";
    public static final String REG5_CHANNEL_2_POWER_DOWN = "Channel 2 power-down";
	public static final String[] ListOfChannelPowerDown={"Normal operation","Power-down"};
	public final static Integer[] ListOfChannelPowerDownConfigValues={0,1};
	//PGA Gain - Bits[6:4]
    public static final String REG4_CHANNEL_1_PGA_GAIN = "Channel 1 PGA Gain";
    public static final String REG5_CHANNEL_2_PGA_GAIN = "Channel 2 PGA Gain";
	public static final String[] ListOfExGGain={"6","1","2","3","4","8","12"};
	public final static Integer[] ListOfExGGainConfigValues={0,1,2,3,4,5,6};
	//Input selection - Bits[3:0]
    public static final String REG4_CHANNEL_1_INPUT_SELECTION = "Channel 1 Input Selection";
    public static final String REG5_CHANNEL_2_INPUT_SELECTION = "Channel 2 Input Selection";
	public static final String[] ListOfCh1InputSelection={
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
	public static final String[] ListOfCh2InputSelection={
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
    public static final String REG6 = "Right leg drive sense selection";
	//PGA chop frequency - Bits[7:6]
    public static final String REG6_PGA_CHOP_FREQUENCY = "PGA chop frequency";
	public static final String[] ListOfPgaChopFrequency={"fMOD / 16", "fMOD / 2", "fMOD / 4"};
	public final static Integer[] ListOfPgaChopFrequencyConfigValues={0,2,3};
	//RLD buffer power - Bit5
    public static final String REG6_RLD_BUFFER_POWER = "RLD buffer power";
	public static final String[] ListOfRldBufferPower={"Powered down", "Enabled"};
	public final static Integer[] ListOfRldBufferPowerConfigValues={0,1};
	//RLD lead-off sense function - Bit4
    public static final String REG6_RLD_LEAD_OFF_SENSE_FUNCTION = "RLD lead-off sense function";
	//Channel 2 RLD negative inputs - Bit3
    public static final String REG6_CH2_RLD_NEG_INPUTS = "Channel 2 RLD negative inputs";
	public static final String[] ListOfCh2RldNegInputs={"Not connected", "Connected to IN2N"};
	public final static Integer[] ListOfRldInputsConfigValues={0,1};
	//Channel 2 RLD positive inputs - Bit2
    public static final String REG6_CH2_RLD_POS_INPUTS = "Channel 2 RLD positive inputs";
	public static final String[] ListOfCh2RldPosInputs={"Not connected", "Connected to IN2PN"};
	//Channel 1 RLD negative inputs - Bit1
    public static final String REG6_CH1_RLD_NEG_INPUTS = "Channel 1 RLD negative inputs";
	public static final String[] ListOfCh1RldNegInputs={"Not connected", "Connected to IN1N"};
	//Channel 1 RLD positive inputs - Bit0
    public static final String REG6_CH1_RLD_POS_INPUTS = "Channel 1 RLD positive inputs";
	public static final String[] ListOfCh1RldPosInputs={"Not connected", "Connected to IN1P"};
	
	//6 - Lead-off sense selection
    public static final String REG7 = "Lead-off sense selection";
	//Must be set to '0' - Bits[7:6]
	//Flip current direction - channel 2 - Bit5
    public static final String REG7_CH2_FLIP_CURRENT = "Flip current direction - channel 2";
	//Flip current direction - channel 1 - Bit4
    public static final String REG7_CH1_FLIP_CURRENT = "Flip current direction - channel 1";
	//Channel 2 lead-off detection negative inputs - Bit3
    public static final String REG7_CH2_LEAD_OFF_DETECT_NEG_INPUTS = "Channel 2 lead-off detection negative inputs";
	//Channel 2 lead-off detection positive inputs - Bit2
    public static final String REG7_CH2_LEAD_OFF_DETECT_POS_INPUTS = "Channel 2 lead-off detection positive inputs";
	//Channel 1 lead-off detection negative inputs - Bit1
    public static final String REG7_CH1_LEAD_OFF_DETECT_NEG_INPUTS = "Channel 1 lead-off detection negative inputs";
	//Channel 1 lead-off detection positive inputs - Bit0
    public static final String REG7_CH1_LEAD_OFF_DETECT_POS_INPUTS = "Channel 1 lead-off detection positive inputs";
    
    //7 - Lead-off status
    public static final String REG8 = "Lead-off status";
	//Bit7 	- Must be set to '0'
	//Bit6	- Flip current direction - channel 2
    public static final String REG8_CLOCK_DIVIDER_SELECTION = "Clock divider selection";
	public static final String[] ListOfClockDividerSelection={"fMOD = fCLK / 4", "fMOD = fCLK / 16"};
	public final static Integer[] ListOfClockDividerSelectionConfigValues={0,1};
	//Bit5	- Must be set to '0'
	public static final String[] ListOfConnectedState={"Connected", "Not Connected"};
	public final static Integer[] ListOfConnectedStateConfigValues={0,1};
	//Bit4	- RLD lead-off status
    public static final String REG8_RLD_LEAD_OFF_STATUS = "RLD lead-off status";
	//Bit3	- Channel 2 negative electrode status
    public static final String REG8_CH2_NEG_ELECTRODE_STATUS = "Channel 2 negative electrode status";
	//Bit2	- Channel 2 positive electrode status
    public static final String REG8_CH2_POS_ELECTRODE_STATUS = "Channel 2 positive electrode status";
	//Bit1	- Channel 1 negative electrode status
    public static final String REG8_CH1_NEG_ELECTRODE_STATUS = "Channel 1 negative electrode status";
	//Bit0	- Channel 1 positive electrode status
    public static final String REG8_CH1_POS_ELECTRODE_STATUS = "Channel 1 positive electrode status";


	//8 - Respiration control register 1
    public static final String REG9 = "Respiration control register 1";
    //Bit7 	- Respiration demodulation circuitry
    public static final String REG9_RESPIRATION_DEMOD_CIRCUITRY = "Respiration demodulation circuitry";
    //Bit6 	- Respiration modulation circuitry
    public static final String REG9_RESPIRATION_MOD_CIRCUITRY = "Respiration modulation circuitry";
    //Bits[5:2]	- Respiration Phase
    public static final String REG9_RESPIRATION_PHASE = "Respiration phase";
	public static final String[] ListOfExGRespirationDetectPhase32khz={"0°","11.25°","22.5°","33.75°","45°","56.25°","67.5°","78.75°","90°","101.25°","112.5°","123.75°","135°","146.25°","157.5°","168.75°"};
	public final static Integer[] ListOfExGRespirationDetectPhase32khzConfigValues={0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15};
	public static final String[] ListOfExGRespirationDetectPhase64khz={"0°","22.5°","45°","67.5°","90°","112.5°","135°","157.5°"};
	public final static Integer[] ListOfExGRespirationDetectPhase64khzConfigValues={0,1,2,3,4,5,6,7};
    //Bit1 	- Must be set to '1'
	//Bit0	- Respiration control
    public static final String REG9_RESPIRATION_CONTROL = "Respiration control";
	public static final String[] ListOfRespControl={"Internal clock", "External Clock"};
	public final static Integer[] ListOfRespControlConfigValues={0,1};
	
	//9 - Respiration control register 2
    public static final String REG10 = "Respiration control register 2";
    //Bit7 	- Respiration demodulation circuitry
    public static final String REG10_RESPIRATION_CALIBRATION = "Respiration calibration on";
    //Bits[6:3] 	- Must be set to '0'
    //Bit2 	- Respiration control frequency
    public static final String REG10_RESPIRATION_CONTROL_FREQUENCY = "Respiration control frequency";
	public static final String[] ListOfExGRespirationDetectFreq={"32 kHz","64 kHz"};
	public final static Integer[] ListOfExGRespirationDetectFreqConfigValues={0,1};
    //Bit1 	- RLD reference signal
    public static final String REG10_RLD_REFERENCE_SIGNAL = "RLD reference signal";
	public static final String[] ListOfExGRespirationRefSignal={"Fed externally","(AVDD - AVSS) / 2"};
	public final static Integer[] ListOfExGRespirationRefSignalConfigValues={0,1};

	
	public ExGConfigBytesDetails() {
		super();
		
		startNewExGConig();
	}
	
	public static final class EXG_SETTING{
//		public static final class REG0{
			public static final class CONVERSION_MODES{
				public static final ExgConfigOption CONTINUOUS = new ExgConfigOption(REG1_CONVERSION_MODES, "Continuous Conversion Mode", 0);
				public static final ExgConfigOption SINGLE_SHOT = new ExgConfigOption(REG1_CONVERSION_MODES, "Single-shot mode", 1);
			}
			public static final class DATA_RATE{
				public static final ExgConfigOption RATE_125SPS = new ExgConfigOption(REG1_DATA_RATE, "125 SPS", 0);
				public static final ExgConfigOption RATE_250SPS = new ExgConfigOption(REG1_DATA_RATE, "250 SPS", 1);
				public static final ExgConfigOption RATE_500SPS = new ExgConfigOption(REG1_DATA_RATE, "500 SPS", 2);
				public static final ExgConfigOption RATE_1KSPS = new ExgConfigOption(REG1_DATA_RATE, "1 kSPS", 3);
				public static final ExgConfigOption RATE_2KSPS = new ExgConfigOption(REG1_DATA_RATE, "2 kSPS", 4);
				public static final ExgConfigOption RATE_4KSPS = new ExgConfigOption(REG1_DATA_RATE, "4 kSPS", 5);
				public static final ExgConfigOption RATE_8KSPS = new ExgConfigOption(REG1_DATA_RATE, "8 kSPS", 6);
			}
//		}
//		public static final class REG1{
			public static final class LEAD_OFF_COMPARATORS{
				public static final ExgConfigOption OFF = new ExgConfigOption(REG2_LEAD_OFF_COMPARATORS, "OFF", 0);
				public static final ExgConfigOption ON = new ExgConfigOption(REG2_LEAD_OFF_COMPARATORS, "ON", 1);
			}
			public static final class REFERENCE_BUFFER{
				public static final ExgConfigOption OFF = new ExgConfigOption(REG2_REFERENCE_BUFFER, "OFF", 0);
				public static final ExgConfigOption ON = new ExgConfigOption(REG2_REFERENCE_BUFFER, "ON", 1);
			}
			public static final class VOLTAGE_REFERENCE{
				public static final ExgConfigOption VREF_2_42V = new ExgConfigOption(REG2_VOLTAGE_REFERENCE, "2.42 V", 0);
				public static final ExgConfigOption VREF_4_033V = new ExgConfigOption(REG2_VOLTAGE_REFERENCE, "4.033 V", 1);
			}
			public static final class OSCILLATOR_CLOCK_CONNECTION{
				public static final ExgConfigOption OFF = new ExgConfigOption(REG2_OSCILLATOR_CLOCK_CONNECTION, "OFF", 0);
				public static final ExgConfigOption ON = new ExgConfigOption(REG2_OSCILLATOR_CLOCK_CONNECTION, "ON", 1);
			}
			public static final class TEST_SIGNAL_SELECTION{
				public static final ExgConfigOption OFF = new ExgConfigOption(REG2_TEST_SIGNAL_SELECTION, "OFF", 0);
				public static final ExgConfigOption ON = new ExgConfigOption(REG2_TEST_SIGNAL_SELECTION, "ON", 1);
			}
			public static final class TEST_SIGNAL_FREQUENCY{
				public static final ExgConfigOption DC = new ExgConfigOption(REG2_TEST_SIGNAL_FREQUENCY, "DC", 0);
				public static final ExgConfigOption SQUARE_WAVE_1KHZ = new ExgConfigOption(REG2_TEST_SIGNAL_FREQUENCY, "1 kHz Square Wave", 1);
			}
//		}

			public static final class COMPARATOR_THRESHOLD{
				public static final ExgConfigOption POS95NEG5 = new ExgConfigOption(REG3_COMPARATOR_THRESHOLD, "Pos:95%-Neg:5%", 0);
				public static final ExgConfigOption POS92_5NEG7_5 = new ExgConfigOption(REG3_COMPARATOR_THRESHOLD, "Pos:92.5%-Neg:7.5%", 1);
				public static final ExgConfigOption POS90NEG10 = new ExgConfigOption(REG3_COMPARATOR_THRESHOLD, "Pos:90%-Neg:10%", 2);
				public static final ExgConfigOption POS87_5NEG12_5 = new ExgConfigOption(REG3_COMPARATOR_THRESHOLD, "Pos:87.5%-Neg:12.5%", 3);
				public static final ExgConfigOption POS85NEG15 = new ExgConfigOption(REG3_COMPARATOR_THRESHOLD, "Pos:85%-Neg:15%", 4);
				public static final ExgConfigOption POS80NEG20 = new ExgConfigOption(REG3_COMPARATOR_THRESHOLD, "Pos:80%-Neg:20%", 5);
				public static final ExgConfigOption POS75NEG25 = new ExgConfigOption(REG3_COMPARATOR_THRESHOLD, "Pos:75%-Neg:25%", 6);
				public static final ExgConfigOption POS70NEG30 = new ExgConfigOption(REG3_COMPARATOR_THRESHOLD, "Pos:70%-Neg:30%", 7);
			}
			public static final class LEAD_OFF_CURRENT{
				public static final ExgConfigOption CURRENT_6NA = new ExgConfigOption(REG3_LEAD_OFF_CURRENT, "6 nA", 0);
				public static final ExgConfigOption CURRENT_22NA = new ExgConfigOption(REG3_LEAD_OFF_CURRENT, "22 nA", 1);
				public static final ExgConfigOption CURRENT_6UA = new ExgConfigOption(REG3_LEAD_OFF_CURRENT, "6 uA", 2);
				public static final ExgConfigOption CURRENT_22UA = new ExgConfigOption(REG3_LEAD_OFF_CURRENT, "22 uA", 3);
			}
			public static final class LEAD_OFF_FREQUENCY{
				public static final ExgConfigOption DC = new ExgConfigOption(REG3_LEAD_OFF_FREQUENCY, "DC lead-off detect", 0);
				public static final ExgConfigOption AC = new ExgConfigOption(REG3_LEAD_OFF_FREQUENCY, "AC lead-off detect (fs / 4)", 1);
			}
			
			public static final class POWER_DOWN_CH1{
				public static final ExgConfigOption NORMAL_OPERATION = new ExgConfigOption(REG4_CHANNEL_1_POWER_DOWN, "Normal operation", 0);
				public static final ExgConfigOption POWER_DOWN = new ExgConfigOption(REG4_CHANNEL_1_POWER_DOWN, "Power-down", 1);
			}
			public static final class GAIN_PGA_CH1{
				public static final ExgConfigOption GAIN_6 = new ExgConfigOption(REG4_CHANNEL_1_PGA_GAIN, "6", 0);
				public static final ExgConfigOption GAIN_1 = new ExgConfigOption(REG4_CHANNEL_1_PGA_GAIN, "1", 1);
				public static final ExgConfigOption GAIN_2 = new ExgConfigOption(REG4_CHANNEL_1_PGA_GAIN, "2", 2);
				public static final ExgConfigOption GAIN_3 = new ExgConfigOption(REG4_CHANNEL_1_PGA_GAIN, "3", 3);
				public static final ExgConfigOption GAIN_4 = new ExgConfigOption(REG4_CHANNEL_1_PGA_GAIN, "4", 4);
				public static final ExgConfigOption GAIN_8 = new ExgConfigOption(REG4_CHANNEL_1_PGA_GAIN, "8", 5);
				public static final ExgConfigOption GAIN_12 = new ExgConfigOption(REG4_CHANNEL_1_PGA_GAIN, "12", 6);
			}
			public static final class INPUT_SELECTION_CH1{
				public static final ExgConfigOption NORMAL = new ExgConfigOption(REG4_CHANNEL_1_INPUT_SELECTION, "Normal electrode input", 0);
				public static final ExgConfigOption SHORTED = new ExgConfigOption(REG4_CHANNEL_1_INPUT_SELECTION, "Input shorted", 1);
				public static final ExgConfigOption RLD_MEASURE = new ExgConfigOption(REG4_CHANNEL_1_INPUT_SELECTION, "RLD_MEASURE", 2);
				public static final ExgConfigOption SUPPLY_MEASURE = new ExgConfigOption(REG4_CHANNEL_1_INPUT_SELECTION, "MVDD for supply measurement", 3);
				public static final ExgConfigOption TEMPERATURE = new ExgConfigOption(REG4_CHANNEL_1_INPUT_SELECTION, "Temperature sensor", 4);
				public static final ExgConfigOption TEST_SIGNAL = new ExgConfigOption(REG4_CHANNEL_1_INPUT_SELECTION, "Test signal", 5);
				public static final ExgConfigOption RLDIN_CONNECTED_TO_POS_INPUT = new ExgConfigOption(REG4_CHANNEL_1_INPUT_SELECTION, "RLD_DRP (positive side connected to RLDIN)", 6);
				public static final ExgConfigOption RLDIN_CONNECTED_TO_NEG_INPUT = new ExgConfigOption(REG4_CHANNEL_1_INPUT_SELECTION, "RLD_DRM (negative side connected to RLDIN)", 7);
				public static final ExgConfigOption RLDIN_CONNECTED_TO_POS_AND_NEG_INPUT = new ExgConfigOption(REG4_CHANNEL_1_INPUT_SELECTION, "RLD_DRPM (both positive and negative sides connected to RLDIN)", 8);
				public static final ExgConfigOption ROUTE_CH3_TO_CH1 = new ExgConfigOption(REG4_CHANNEL_1_INPUT_SELECTION, "Route IN3P and IN3N to channel 1 inputs", 9);
			}

			public static final class POWER_DOWN_CH2{
				public static final ExgConfigOption NORMAL_OPERATION = new ExgConfigOption(REG5_CHANNEL_2_POWER_DOWN, "Normal operation", 0);
				public static final ExgConfigOption POWER_DOWN = new ExgConfigOption(REG5_CHANNEL_2_POWER_DOWN, "Power-down", 1);
			}
			public static final class GAIN_PGA_CH2{
				public static final ExgConfigOption GAIN_6 = new ExgConfigOption(REG5_CHANNEL_2_PGA_GAIN, "6", 0);
				public static final ExgConfigOption GAIN_1 = new ExgConfigOption(REG5_CHANNEL_2_PGA_GAIN, "1", 1);
				public static final ExgConfigOption GAIN_2 = new ExgConfigOption(REG5_CHANNEL_2_PGA_GAIN, "2", 2);
				public static final ExgConfigOption GAIN_3 = new ExgConfigOption(REG5_CHANNEL_2_PGA_GAIN, "3", 3);
				public static final ExgConfigOption GAIN_4 = new ExgConfigOption(REG5_CHANNEL_2_PGA_GAIN, "4", 4);
				public static final ExgConfigOption GAIN_8 = new ExgConfigOption(REG5_CHANNEL_2_PGA_GAIN, "8", 5);
				public static final ExgConfigOption GAIN_12 = new ExgConfigOption(REG5_CHANNEL_2_PGA_GAIN, "12", 6);
			}
			public static final class INPUT_SELECTION_CH2{
				public static final ExgConfigOption NORMAL = new ExgConfigOption(REG5_CHANNEL_2_INPUT_SELECTION, "Normal electrode input", 0);
				public static final ExgConfigOption SHORTED = new ExgConfigOption(REG5_CHANNEL_2_INPUT_SELECTION, "Input shorted", 1);
				public static final ExgConfigOption RLD_MEASURE = new ExgConfigOption(REG5_CHANNEL_2_INPUT_SELECTION, "RLD_MEASURE", 2);
				public static final ExgConfigOption SUPPLY_MEASURE = new ExgConfigOption(REG5_CHANNEL_2_INPUT_SELECTION, "VDD / 2 for supply measurement", 3);
				public static final ExgConfigOption TEMPERATURE = new ExgConfigOption(REG5_CHANNEL_2_INPUT_SELECTION, "Temperature sensor", 4);
				public static final ExgConfigOption TEST_SIGNAL = new ExgConfigOption(REG5_CHANNEL_2_INPUT_SELECTION, "Test signal", 5);
				public static final ExgConfigOption RLDIN_CONNECTED_TO_POS_INPUT = new ExgConfigOption(REG5_CHANNEL_2_INPUT_SELECTION, "RLD_DRP (positive side connected to RLDIN)", 6);
				public static final ExgConfigOption RLDIN_CONNECTED_TO_NEG_INPUT = new ExgConfigOption(REG5_CHANNEL_2_INPUT_SELECTION, "RLD_DRM (negative side connected to RLDIN)", 7);
				public static final ExgConfigOption RLDIN_CONNECTED_TO_POS_AND_NEG_INPUTS = new ExgConfigOption(REG5_CHANNEL_2_INPUT_SELECTION, "RLD_DRPM (both positive and negative sides connected to RLDIN)", 8);
				public static final ExgConfigOption ROUTE_CH3_TO_CH1 = new ExgConfigOption(REG5_CHANNEL_2_INPUT_SELECTION, "Route IN3P and IN3N to channel 1 inputs", 9);
			}

			public static final class PGA_CHOP_FREQUENCY{
				public static final ExgConfigOption FMOD_16 = new ExgConfigOption(REG6_PGA_CHOP_FREQUENCY, "fMOD / 16", 0);
				public static final ExgConfigOption FMOD_2 = new ExgConfigOption(REG6_PGA_CHOP_FREQUENCY, "fMOD / 2", 2);
				public static final ExgConfigOption FMOD_4 = new ExgConfigOption(REG6_PGA_CHOP_FREQUENCY, "fMOD / 4", 3);
			}
			public static final class RLD_BUFFER_POWER{
				public static final ExgConfigOption POWERED_DOWN = new ExgConfigOption(REG6_RLD_BUFFER_POWER, "Powered down", 0);
				public static final ExgConfigOption ENABLED = new ExgConfigOption(REG6_RLD_BUFFER_POWER, "Enabled", 1);
			}
			public static final class RLD_NEG_INPUTS_CH2{
				public static final ExgConfigOption NOT_CONNECTED = new ExgConfigOption(REG6_CH2_RLD_NEG_INPUTS, "Not connected", 0);
				public static final ExgConfigOption RLD_CONNECTED_TO_IN2N = new ExgConfigOption(REG6_CH2_RLD_NEG_INPUTS, "Connected to IN2N", 1);
			}
			public static final class RLD_POS_INPUTS_CH2{
				public static final ExgConfigOption NOT_CONNECTED = new ExgConfigOption(REG6_CH2_RLD_POS_INPUTS, "Not connected", 0);
				public static final ExgConfigOption RLD_CONNECTED_TO_IN2P = new ExgConfigOption(REG6_CH2_RLD_POS_INPUTS, "Connected to IN2P", 1);
			}
			public static final class RLD_NEG_INPUTS_CH1{
				public static final ExgConfigOption NOT_CONNECTED = new ExgConfigOption(REG6_CH1_RLD_NEG_INPUTS, "Not connected", 0);
				public static final ExgConfigOption RLD_CONNECTED_TO_IN1N = new ExgConfigOption(REG6_CH1_RLD_NEG_INPUTS, "Connected to IN1N", 1);
			}
			public static final class RLD_POS_INPUTS_CH1{
				public static final ExgConfigOption NOT_CONNECTED = new ExgConfigOption(REG6_CH1_RLD_POS_INPUTS, "Not connected", 0);
				public static final ExgConfigOption RLD_CONNECTED_TO_IN1P = new ExgConfigOption(REG6_CH1_RLD_POS_INPUTS, "Connected to IN1P", 1);
			}

			public static final class FLIP_CURRENT_CH2{
				public static final ExgConfigOption OFF = new ExgConfigOption(REG7_CH2_FLIP_CURRENT, "OFF", 0);
				public static final ExgConfigOption ON = new ExgConfigOption(REG7_CH2_FLIP_CURRENT, "ON", 1);
			}
			public static final class FLIP_CURRENT_CH1{
				public static final ExgConfigOption OFF = new ExgConfigOption(REG7_CH1_FLIP_CURRENT, "OFF", 0);
				public static final ExgConfigOption ON = new ExgConfigOption(REG7_CH1_FLIP_CURRENT, "ON", 1);
			}
			public static final class LEAD_OFF_DETECT_NEG_INPUTS_CH2{
				public static final ExgConfigOption OFF = new ExgConfigOption(REG7_CH2_LEAD_OFF_DETECT_NEG_INPUTS, "OFF", 0);
				public static final ExgConfigOption ON = new ExgConfigOption(REG7_CH2_LEAD_OFF_DETECT_NEG_INPUTS, "ON", 1);
			}
			public static final class LEAD_OFF_DETECT_POS_INPUTS_CH2{
				public static final ExgConfigOption OFF = new ExgConfigOption(REG7_CH2_LEAD_OFF_DETECT_POS_INPUTS, "OFF", 0);
				public static final ExgConfigOption ON = new ExgConfigOption(REG7_CH2_LEAD_OFF_DETECT_POS_INPUTS, "ON", 1);
			}
			public static final class LEAD_OFF_DETECT_NEG_INPUTS_CH1{
				public static final ExgConfigOption OFF = new ExgConfigOption(REG7_CH1_LEAD_OFF_DETECT_NEG_INPUTS, "OFF", 0);
				public static final ExgConfigOption ON = new ExgConfigOption(REG7_CH1_LEAD_OFF_DETECT_NEG_INPUTS, "ON", 1);
			}
			public static final class LEAD_OFF_DETECT_POS_INPUTS_CH1{
				public static final ExgConfigOption OFF = new ExgConfigOption(REG7_CH1_LEAD_OFF_DETECT_POS_INPUTS, "OFF", 0);
				public static final ExgConfigOption ON = new ExgConfigOption(REG7_CH1_LEAD_OFF_DETECT_POS_INPUTS, "ON", 1);
			}

			public static final class RESPIRATION_DEMOD_CIRCUITRY{
				public static final ExgConfigOption OFF = new ExgConfigOption(REG9_RESPIRATION_DEMOD_CIRCUITRY, "OFF", 0);
				public static final ExgConfigOption ON = new ExgConfigOption(REG9_RESPIRATION_DEMOD_CIRCUITRY, "ON", 1);
			}
			public static final class RESPIRATION_MOD_CIRCUITRY{
				public static final ExgConfigOption OFF = new ExgConfigOption(REG9_RESPIRATION_MOD_CIRCUITRY, "OFF", 0);
				public static final ExgConfigOption ON = new ExgConfigOption(REG9_RESPIRATION_MOD_CIRCUITRY, "ON", 1);
			}

			//TODO:2015-06-16 handle both FREQ and PHASE together
			public static final class RESPIRATION_PHASE{
				public static final ExgConfigOption FREQ32KHZ_PHASE_0 = new ExgConfigOption(REG9_RESPIRATION_PHASE, "0°", 0);
				public static final ExgConfigOption FREQ32KHZ_PHASE_11_25 = new ExgConfigOption(REG9_RESPIRATION_PHASE, "11.25°", 1);
				public static final ExgConfigOption FREQ32KHZ_PHASE_22_5 = new ExgConfigOption(REG9_RESPIRATION_PHASE, "22.5°", 2);
				public static final ExgConfigOption FREQ32KHZ_PHASE_33_75 = new ExgConfigOption(REG9_RESPIRATION_PHASE, "33.75°", 3);
				public static final ExgConfigOption FREQ32KHZ_PHASE_45 = new ExgConfigOption(REG9_RESPIRATION_PHASE, "45°", 4);
				public static final ExgConfigOption FREQ32KHZ_PHASE_56_25 = new ExgConfigOption(REG9_RESPIRATION_PHASE, "56.25°", 5);
				public static final ExgConfigOption FREQ32KHZ_PHASE_67_5 = new ExgConfigOption(REG9_RESPIRATION_PHASE, "67.5°", 6);
				public static final ExgConfigOption FREQ32KHZ_PHASE_78_75 = new ExgConfigOption(REG9_RESPIRATION_PHASE, "78.75°", 7);
				public static final ExgConfigOption FREQ32KHZ_PHASE_90 = new ExgConfigOption(REG9_RESPIRATION_PHASE, "90°", 8);
				public static final ExgConfigOption FREQ32KHZ_PHASE_101_25 = new ExgConfigOption(REG9_RESPIRATION_PHASE, "101.25°", 9);
				public static final ExgConfigOption FREQ32KHZ_PHASE_112_5 = new ExgConfigOption(REG9_RESPIRATION_PHASE, "112.5°", 10);
				public static final ExgConfigOption FREQ32KHZ_PHASE_123_75 = new ExgConfigOption(REG9_RESPIRATION_PHASE, "123.75°", 11);
				public static final ExgConfigOption FREQ32KHZ_PHASE_135 = new ExgConfigOption(REG9_RESPIRATION_PHASE, "135°", 12);
				public static final ExgConfigOption FREQ32KHZ_PHASE_146_25 = new ExgConfigOption(REG9_RESPIRATION_PHASE, "146.25°", 13);
				public static final ExgConfigOption FREQ32KHZ_PHASE_157_5 = new ExgConfigOption(REG9_RESPIRATION_PHASE, "157.5°", 14);
				public static final ExgConfigOption FREQ32KHZ_PHASE_168_75 = new ExgConfigOption(REG9_RESPIRATION_PHASE, "168.75°", 15);
				
				public static final ExgConfigOption FREQ64KHZ_PHASE_0 = new ExgConfigOption(REG9_RESPIRATION_PHASE, "0°", 0);
				public static final ExgConfigOption FREQ64KHZ_PHASE_11_25 = new ExgConfigOption(REG9_RESPIRATION_PHASE, "22.5°", 1);
				public static final ExgConfigOption FREQ64KHZ_PHASE_22_5 = new ExgConfigOption(REG9_RESPIRATION_PHASE, "45°", 2);
				public static final ExgConfigOption FREQ64KHZ_PHASE_33_75 = new ExgConfigOption(REG9_RESPIRATION_PHASE, "67.5°", 3);
				public static final ExgConfigOption FREQ64KHZ_PHASE_45 = new ExgConfigOption(REG9_RESPIRATION_PHASE, "90°", 4);
				public static final ExgConfigOption FREQ64KHZ_PHASE_56_25 = new ExgConfigOption(REG9_RESPIRATION_PHASE, "112.5°", 5);
				public static final ExgConfigOption FREQ64KHZ_PHASE_67_5 = new ExgConfigOption(REG9_RESPIRATION_PHASE, "135°", 6);
				public static final ExgConfigOption FREQ64KHZ_PHASE_78_75 = new ExgConfigOption(REG9_RESPIRATION_PHASE, "157.5°", 7);
			}
			public static final class RESPIRATION_CONTROL{
				public static final ExgConfigOption INTERNAL_CLOCK = new ExgConfigOption(REG9_RESPIRATION_CONTROL, "Internal clock", 0);
				public static final ExgConfigOption EXTERNAL_CLOCK = new ExgConfigOption(REG9_RESPIRATION_CONTROL, "External Clock", 1);
			}
			
			public static final class RESPIRATION_CALIBRATION{
				public static final ExgConfigOption OFF = new ExgConfigOption(REG10_RESPIRATION_CALIBRATION, "OFF", 0);
				public static final ExgConfigOption ON = new ExgConfigOption(REG10_RESPIRATION_CALIBRATION, "ON", 1);
			}
			public static final class RESPIRATION_CONTROL_FREQUENCY{
				public static final ExgConfigOption FREQ_32KHZ = new ExgConfigOption(REG10_RESPIRATION_CONTROL_FREQUENCY, "32 kHz", 0);
				public static final ExgConfigOption FREQ_64KHZ = new ExgConfigOption(REG10_RESPIRATION_CONTROL_FREQUENCY, "64 kHz", 1);
			}
			public static final class RLD_REFERENCE_SIGNAL{
				public static final ExgConfigOption FED_EXTERNALLY = new ExgConfigOption(REG10_RLD_REFERENCE_SIGNAL, "Fed externally", 0);
				public static final ExgConfigOption HALF_OF_SUPPLY = new ExgConfigOption(REG10_RLD_REFERENCE_SIGNAL, "(AVDD - AVSS) / 2", 1);
			}
			
			
	}
	
	
	public void startNewExGConig(){
		mMapOfExGSettingsChip1.clear();
		mMapOfExGSettingsChip2.clear();

		mMapOfExGSettingsChip1.put(REG1_CONVERSION_MODES, new ExGConfigOptionDetails(CHIP_INDEX.CHIP1, 0, REG1_CONVERSION_MODES, ListOfConversionModes, ListOfConversionModeConfigValues, 7, 0x01));
        mMapOfExGSettingsChip1.put(REG1_DATA_RATE, new ExGConfigOptionDetails(CHIP_INDEX.CHIP1, 0, REG1_DATA_RATE, ListOfDataRates, ListOfDataRateConfigValues, 0, 0x07));
        
        mMapOfExGSettingsChip1.put(REG2_LEAD_OFF_COMPARATORS, new ExGConfigOptionDetails(CHIP_INDEX.CHIP1, 1, REG2_LEAD_OFF_COMPARATORS, 6));
        mMapOfExGSettingsChip1.put(REG2_REFERENCE_BUFFER, new ExGConfigOptionDetails(CHIP_INDEX.CHIP1, 1, REG2_REFERENCE_BUFFER, 5));
        mMapOfExGSettingsChip1.put(REG2_VOLTAGE_REFERENCE, new ExGConfigOptionDetails(CHIP_INDEX.CHIP1, 1, REG2_VOLTAGE_REFERENCE, ListOfVoltageReference, ListOfVoltageReferenceConfigValues, 4, 0x01));
        mMapOfExGSettingsChip1.put(REG2_OSCILLATOR_CLOCK_CONNECTION, new ExGConfigOptionDetails(CHIP_INDEX.CHIP1, 1, REG2_OSCILLATOR_CLOCK_CONNECTION, 3));
        mMapOfExGSettingsChip1.put(REG2_TEST_SIGNAL_SELECTION, new ExGConfigOptionDetails(CHIP_INDEX.CHIP1, 1, REG2_TEST_SIGNAL_SELECTION, 1));
        mMapOfExGSettingsChip1.put(REG2_TEST_SIGNAL_FREQUENCY, new ExGConfigOptionDetails(CHIP_INDEX.CHIP1, 1, REG2_TEST_SIGNAL_FREQUENCY, ListOfTestFreq, ListOfTestFreqConfigValues, 0, 0x01));

        mMapOfExGSettingsChip1.put(REG3_COMPARATOR_THRESHOLD, new ExGConfigOptionDetails(CHIP_INDEX.CHIP1, 2, REG3_COMPARATOR_THRESHOLD, ListOfLeadOffCompThres, ListOfLeadOffCompThresConfigValues, 5, 0x07));
        mMapOfExGSettingsChip1.put(REG3_LEAD_OFF_CURRENT, new ExGConfigOptionDetails(CHIP_INDEX.CHIP1, 2, REG3_LEAD_OFF_CURRENT, ListOfLeadOffCurrent, ListOfLeadOffCurrentConfigValues, 2, 0x03));
        mMapOfExGSettingsChip1.put(REG3_LEAD_OFF_FREQUENCY, new ExGConfigOptionDetails(CHIP_INDEX.CHIP1, 2, REG3_LEAD_OFF_FREQUENCY, ListOfLeadOffDetectionFreq, ListOfLeadOffDetectionFreqConfigValues, 0, 0x01));

        mMapOfExGSettingsChip1.put(REG4_CHANNEL_1_POWER_DOWN, new ExGConfigOptionDetails(CHIP_INDEX.CHIP1, 3, REG4_CHANNEL_1_POWER_DOWN, ListOfChannelPowerDown, ListOfChannelPowerDownConfigValues, 7, 0x01));
        mMapOfExGSettingsChip1.put(REG4_CHANNEL_1_PGA_GAIN, new ExGConfigOptionDetails(CHIP_INDEX.CHIP1, 3, REG4_CHANNEL_1_PGA_GAIN, ListOfExGGain, ListOfExGGainConfigValues, 4, 0x07));
        mMapOfExGSettingsChip1.put(REG4_CHANNEL_1_INPUT_SELECTION, new ExGConfigOptionDetails(CHIP_INDEX.CHIP1, 3, REG4_CHANNEL_1_INPUT_SELECTION, ListOfCh1InputSelection, ListOfInputSelectionConfigValues, 0, 0x0F));

        mMapOfExGSettingsChip1.put(REG5_CHANNEL_2_POWER_DOWN, new ExGConfigOptionDetails(CHIP_INDEX.CHIP1, 4, REG5_CHANNEL_2_POWER_DOWN, ListOfChannelPowerDown, ListOfChannelPowerDownConfigValues, 7, 0x01));
        mMapOfExGSettingsChip1.put(REG5_CHANNEL_2_PGA_GAIN, new ExGConfigOptionDetails(CHIP_INDEX.CHIP1, 4, REG5_CHANNEL_2_PGA_GAIN, ListOfExGGain, ListOfExGGainConfigValues, 4, 0x07));
        mMapOfExGSettingsChip1.put(REG5_CHANNEL_2_INPUT_SELECTION, new ExGConfigOptionDetails(CHIP_INDEX.CHIP1, 4, REG5_CHANNEL_2_INPUT_SELECTION, ListOfCh2InputSelection, ListOfInputSelectionConfigValues, 0, 0x0F));

        mMapOfExGSettingsChip1.put(REG6_PGA_CHOP_FREQUENCY, new ExGConfigOptionDetails(CHIP_INDEX.CHIP1, 5, REG6_PGA_CHOP_FREQUENCY, ListOfPgaChopFrequency, ListOfPgaChopFrequencyConfigValues, 6, 0x03));
        mMapOfExGSettingsChip1.put(REG6_RLD_BUFFER_POWER, new ExGConfigOptionDetails(CHIP_INDEX.CHIP1, 5, REG6_RLD_BUFFER_POWER, ListOfRldBufferPower, ListOfRldBufferPowerConfigValues, 5, 0x01));
        mMapOfExGSettingsChip1.put(REG6_RLD_LEAD_OFF_SENSE_FUNCTION, new ExGConfigOptionDetails(CHIP_INDEX.CHIP1, 5, REG6_RLD_LEAD_OFF_SENSE_FUNCTION, 4));
        mMapOfExGSettingsChip1.put(REG6_CH2_RLD_NEG_INPUTS, new ExGConfigOptionDetails(CHIP_INDEX.CHIP1, 5, REG6_CH2_RLD_NEG_INPUTS, ListOfCh2RldNegInputs, ListOfRldInputsConfigValues, 3, 0x01));
        mMapOfExGSettingsChip1.put(REG6_CH2_RLD_POS_INPUTS, new ExGConfigOptionDetails(CHIP_INDEX.CHIP1, 5, REG6_CH2_RLD_POS_INPUTS, ListOfCh2RldPosInputs, ListOfRldInputsConfigValues, 2, 0x01));
        mMapOfExGSettingsChip1.put(REG6_CH1_RLD_NEG_INPUTS, new ExGConfigOptionDetails(CHIP_INDEX.CHIP1, 5, REG6_CH1_RLD_NEG_INPUTS, ListOfCh1RldNegInputs, ListOfRldInputsConfigValues, 1, 0x01));
        mMapOfExGSettingsChip1.put(REG6_CH1_RLD_POS_INPUTS, new ExGConfigOptionDetails(CHIP_INDEX.CHIP1, 5, REG6_CH1_RLD_POS_INPUTS, ListOfCh1RldPosInputs, ListOfRldInputsConfigValues, 0, 0x01));

        mMapOfExGSettingsChip1.put(REG7_CH2_FLIP_CURRENT, new ExGConfigOptionDetails(CHIP_INDEX.CHIP1, 6, REG7_CH2_FLIP_CURRENT, 5));
        mMapOfExGSettingsChip1.put(REG7_CH1_FLIP_CURRENT, new ExGConfigOptionDetails(CHIP_INDEX.CHIP1, 6, REG7_CH1_FLIP_CURRENT, 4));
        mMapOfExGSettingsChip1.put(REG7_CH2_LEAD_OFF_DETECT_NEG_INPUTS, new ExGConfigOptionDetails(CHIP_INDEX.CHIP1, 6, REG7_CH2_LEAD_OFF_DETECT_NEG_INPUTS, 3));
        mMapOfExGSettingsChip1.put(REG7_CH2_LEAD_OFF_DETECT_POS_INPUTS, new ExGConfigOptionDetails(CHIP_INDEX.CHIP1, 6, REG7_CH2_LEAD_OFF_DETECT_POS_INPUTS, 2));
        mMapOfExGSettingsChip1.put(REG7_CH1_LEAD_OFF_DETECT_NEG_INPUTS, new ExGConfigOptionDetails(CHIP_INDEX.CHIP1, 6, REG7_CH1_LEAD_OFF_DETECT_NEG_INPUTS, 1));
        mMapOfExGSettingsChip1.put(REG7_CH1_LEAD_OFF_DETECT_POS_INPUTS, new ExGConfigOptionDetails(CHIP_INDEX.CHIP1, 6, REG7_CH1_LEAD_OFF_DETECT_POS_INPUTS, 0));

        mMapOfExGSettingsChip1.put(REG8_CLOCK_DIVIDER_SELECTION, new ExGConfigOptionDetails(CHIP_INDEX.CHIP1, 7, REG8_CLOCK_DIVIDER_SELECTION, ListOfClockDividerSelection, ListOfClockDividerSelectionConfigValues, 6, 0x01));
        mMapOfExGSettingsChip1.put(REG8_RLD_LEAD_OFF_STATUS, new ExGConfigOptionDetails(CHIP_INDEX.CHIP1, 7, REG8_RLD_LEAD_OFF_STATUS, ListOfConnectedState, ListOfConnectedStateConfigValues, 4, 0x01));
        mMapOfExGSettingsChip1.put(REG8_CH2_NEG_ELECTRODE_STATUS, new ExGConfigOptionDetails(CHIP_INDEX.CHIP1, 7, REG8_CH2_NEG_ELECTRODE_STATUS, ListOfConnectedState, ListOfConnectedStateConfigValues, 3, 0x01));
        mMapOfExGSettingsChip1.put(REG8_CH2_POS_ELECTRODE_STATUS, new ExGConfigOptionDetails(CHIP_INDEX.CHIP1, 7, REG8_CH2_POS_ELECTRODE_STATUS, ListOfConnectedState, ListOfConnectedStateConfigValues, 2, 0x01));
        mMapOfExGSettingsChip1.put(REG8_CH1_NEG_ELECTRODE_STATUS, new ExGConfigOptionDetails(CHIP_INDEX.CHIP1, 7, REG8_CH1_NEG_ELECTRODE_STATUS, ListOfConnectedState, ListOfConnectedStateConfigValues, 1, 0x01));
        mMapOfExGSettingsChip1.put(REG8_CH1_POS_ELECTRODE_STATUS, new ExGConfigOptionDetails(CHIP_INDEX.CHIP1, 7, REG8_CH1_POS_ELECTRODE_STATUS, ListOfConnectedState, ListOfConnectedStateConfigValues, 0, 0x01));

        mMapOfExGSettingsChip1.put(REG9_RESPIRATION_DEMOD_CIRCUITRY, new ExGConfigOptionDetails(CHIP_INDEX.CHIP1, 8, REG9_RESPIRATION_DEMOD_CIRCUITRY, ListOfOnOff, ListOfOnOffConfigValues, 7, 0x01));
        mMapOfExGSettingsChip1.put(REG9_RESPIRATION_MOD_CIRCUITRY, new ExGConfigOptionDetails(CHIP_INDEX.CHIP1, 8, REG9_RESPIRATION_MOD_CIRCUITRY, ListOfOnOff, ListOfOnOffConfigValues, 6, 0x01));
        mMapOfExGSettingsChip1.put(REG9_RESPIRATION_PHASE, new ExGConfigOptionDetails(CHIP_INDEX.CHIP1, 8, REG9_RESPIRATION_PHASE, ListOfExGRespirationDetectPhase32khz, ListOfExGRespirationDetectPhase32khzConfigValues, 2, 0x0F));
        mMapOfExGSettingsChip1.put(REG9_RESPIRATION_CONTROL, new ExGConfigOptionDetails(CHIP_INDEX.CHIP1, 8, REG9_RESPIRATION_CONTROL, ListOfRespControl, ListOfRespControlConfigValues, 0, 0x01));

        mMapOfExGSettingsChip1.put(REG10_RESPIRATION_CALIBRATION, new ExGConfigOptionDetails(CHIP_INDEX.CHIP1, 9, REG10_RESPIRATION_CALIBRATION, ListOfOnOff, ListOfOnOffConfigValues, 7, 0x01));
        mMapOfExGSettingsChip1.put(REG10_RESPIRATION_CONTROL_FREQUENCY, new ExGConfigOptionDetails(CHIP_INDEX.CHIP1, 9, REG10_RESPIRATION_CONTROL_FREQUENCY, ListOfExGRespirationDetectFreq, ListOfExGRespirationDetectFreqConfigValues, 2, 0x01));
        mMapOfExGSettingsChip1.put(REG10_RLD_REFERENCE_SIGNAL, new ExGConfigOptionDetails(CHIP_INDEX.CHIP1, 9, REG10_RLD_REFERENCE_SIGNAL, ListOfExGRespirationRefSignal, ListOfExGRespirationRefSignalConfigValues, 1, 0x01));

        for(String key:mMapOfExGSettingsChip1.keySet()){
        	mMapOfExGSettingsChip2.put(key, mMapOfExGSettingsChip1.get(key).deepClone());
        	mMapOfExGSettingsChip2.get(key).chipIndex = CHIP_INDEX.CHIP2;
        }
        
	}

    
    
    public byte[] generateExgByteArray(CHIP_INDEX chipIndex){
    	byte[] byteArray = new byte[10];
    	HashMap<String, ExGConfigOptionDetails> mMapOfExGSettingsToRef;
    	if(chipIndex==CHIP_INDEX.CHIP1){
    		mMapOfExGSettingsToRef = mMapOfExGSettingsChip1;
    	}
    	else{
    		mMapOfExGSettingsToRef = mMapOfExGSettingsChip2;
    	}
    	
    	for(ExGConfigOptionDetails eSD:mMapOfExGSettingsToRef.values()){
    		if(eSD.settingType==SettingType.COMBOBOX){
        		byteArray[eSD.byteIndex] |= ((eSD.valueInt & eSD.mask) << eSD.bitShift);
//    			if(eSD.GuiLabel==REG6_RLD_BUFFER_POWER){
//    				System.out.println(byteArray[eSD.byteIndex]);
//    			}
    		}
    		else if(eSD.settingType==SettingType.CHECKBOX){
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
    	
//		System.out.println(byteArray[5]);
		return byteArray;
    }


	public void setExgPropertySingleChip(CHIP_INDEX chipIndex,ExgConfigOption option) {
		HashMap<String, ExGConfigOptionDetails> mapToRef = mMapOfExGSettingsChip1;
		if(chipIndex==CHIP_INDEX.CHIP2){
			mapToRef = mMapOfExGSettingsChip2;
		}
		
		if(mapToRef!=null){
			if(mapToRef.containsKey(option.settingTitle)){
				if(mapToRef.get(option.settingTitle).settingType==SettingType.CHECKBOX){
					mapToRef.get(option.settingTitle).valueBool = (option.configValueInt==1? true:false); 
				}
				else if(mapToRef.get(option.settingTitle).settingType==SettingType.COMBOBOX){
					mapToRef.get(option.settingTitle).valueInt = option.configValueInt; 
				}
			}
			else{
				System.out.println("WTF");
			}
		}
	}
	
	public void setExgPropertyBothChips(ExgConfigOption option) {
		CHIP_INDEX chipIndex = CHIP_INDEX.CHIP1;
		for(int i=1;i<=2;i++){
			if(i==2){
				chipIndex = CHIP_INDEX.CHIP2;
			}
			setExgPropertySingleChip(chipIndex, option);
		}
	}


	public byte[] getEXG1RegisterArray() {
		return generateExgByteArray(CHIP_INDEX.CHIP1);
	}



	public byte[] getEXG2RegisterArray() {
		return generateExgByteArray(CHIP_INDEX.CHIP2);
	}



	public void setExgPropertyValue(CHIP_INDEX chipIndex, String propertyName, Object value) {
		HashMap<String, ExGConfigOptionDetails> mapToRef = mMapOfExGSettingsChip1;
		if(chipIndex==CHIP_INDEX.CHIP2){
			mapToRef = mMapOfExGSettingsChip2;
		}
		
		if(mapToRef!=null){
			if(mapToRef.containsKey(propertyName)){
				if(mapToRef.get(propertyName).settingType==SettingType.CHECKBOX){
					mapToRef.get(propertyName).valueBool = (boolean)value; 
				}
				else if(mapToRef.get(propertyName).settingType==SettingType.COMBOBOX){
					mapToRef.get(propertyName).valueInt = (int)value; 
				}
			}
			else{
				System.out.println("WTF");
			}
		}
	}
	
}
