package com.shimmerresearch.exgConfig;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.shimmerresearch.exgConfig.ExGConfigOptionDetails.EXG_CHIP_INDEX;
import com.shimmerresearch.exgConfig.ExGConfigOptionDetails.SettingType;

public class ExGConfigBytesDetails implements Serializable {
	
	//ADS1292R datasheet
	//http://www.ti.com/lit/ds/sbas502b/sbas502b.pdf
	
	/** * */
	private static final long serialVersionUID = -6128939356278743505L;
	
	public HashMap<String, Integer> mMapOfExGSettingsChip1ThisShimmer = new HashMap<String, Integer>();
	public HashMap<String, Integer> mMapOfExGSettingsChip2ThisShimmer = new HashMap<String, Integer>();
	
	private int BIT0 = 0x01;
	private int BIT1 = 0x02;
	private int BIT2 = 0x04;
	private int BIT3 = 0x08;
	private int BIT4 = 0x10;
	private int BIT5 = 0x20;
	private int BIT6 = 0x40;
	private int BIT7 = 0x80;
	
	public static final String[] ListOfECGReferenceElectrode={"Inverse Wilson CT","Fixed Potential"};
	public static final Integer[] ListOfECGReferenceElectrodeConfigValues={13,0};
	
	public static final String[] ListOfEMGReferenceElectrode={"Fixed Potential", "Inverse of Ch1"};
	public static final Integer[] ListOfEMGReferenceElectrodeConfigValues={0,3};
	
	public static final String[] ListOfExGResolutions={"16-bit","24-bit"};
	public static final Integer[] ListOfExGResolutionsConfigValues={0,1};

	public static final String[] ListOfOnOff={"On","Off"};
	public static final Integer[] ListOfOnOffConfigValues={0x01,0x00};

	// TODO:2015-06-16 get rid of predefined Integer and String array and
	// replace with items from EXG_SETTING_OPTIONS.

	public static final class EXG_REGISTERS{
	    public static final String REG1 = "Configuration Register 1";
	    public static final String REG2 = "Configuration Register 2";
	    public static final String REG3 = "Lead-Off Control Register";
	    public static final String REG4 = "Channel 1 settings";
	    public static final String REG5 = "Channel 2 settings";
	    public static final String REG6 = "Right leg drive sense selection";
	    public static final String REG7 = "Lead-off sense selection";
	    public static final String REG8 = "Lead-off status";
	    public static final String REG9 = "Respiration control register 1";
	    public static final String REG10 = "Respiration control register 2";
	}
	
	public static final class EXG_SETTINGS{
	    public static final String REG1_CONVERSION_MODES = "Conversion Mode";
	    public static final String REG1_DATA_RATE = "Data Rate";
	    
	    public static final String REG2_LEAD_OFF_COMPARATORS = "Lead-off comparators";
	    public static final String REG2_REFERENCE_BUFFER = "Reference buffer";
	    public static final String REG2_VOLTAGE_REFERENCE = "Voltage reference";
	    public static final String REG2_OSCILLATOR_CLOCK_CONNECTION = "Oscillator clock connection";
	    public static final String REG2_TEST_SIGNAL_SELECTION = "Test signal selection";
	    public static final String REG2_TEST_SIGNAL_FREQUENCY = "Test signal frequency";
	    
	    public static final String REG3_COMPARATOR_THRESHOLD = "Comparator threshold";
	    public static final String REG3_LEAD_OFF_CURRENT = "Lead-off current";
	    public static final String REG3_LEAD_OFF_FREQUENCY = "Lead-off frequency";
	    
	    public static final String REG4_CHANNEL_1_POWER_DOWN = "Channel 1 power-down";
	    public static final String REG4_CHANNEL_1_PGA_GAIN = "Channel 1 PGA Gain";
	    public static final String REG4_CHANNEL_1_INPUT_SELECTION = "Channel 1 Input Selection";
	    public static final String REG5_CHANNEL_2_POWER_DOWN = "Channel 2 power-down";
	    public static final String REG5_CHANNEL_2_PGA_GAIN = "Channel 2 PGA Gain";
	    public static final String REG5_CHANNEL_2_INPUT_SELECTION = "Channel 2 Input Selection";
	    
	    public static final String REG6_PGA_CHOP_FREQUENCY = "PGA chop frequency";
	    public static final String REG6_RLD_BUFFER_POWER = "RLD buffer power";
	    public static final String REG6_RLD_LEAD_OFF_SENSE_FUNCTION = "RLD lead-off sense function";
	    public static final String REG6_CH2_RLD_NEG_INPUTS = "Channel 2 RLD negative inputs";
	    public static final String REG6_CH2_RLD_POS_INPUTS = "Channel 2 RLD positive inputs";
	    public static final String REG6_CH1_RLD_NEG_INPUTS = "Channel 1 RLD negative inputs";
	    public static final String REG6_CH1_RLD_POS_INPUTS = "Channel 1 RLD positive inputs";

	    public static final String REG7_CH2_FLIP_CURRENT = "Flip current direction - channel 2";
	    public static final String REG7_CH1_FLIP_CURRENT = "Flip current direction - channel 1";
	    public static final String REG7_CH2_LEAD_OFF_DETECT_NEG_INPUTS = "Channel 2 lead-off detection negative inputs";
	    public static final String REG7_CH2_LEAD_OFF_DETECT_POS_INPUTS = "Channel 2 lead-off detection positive inputs";
	    public static final String REG7_CH1_LEAD_OFF_DETECT_NEG_INPUTS = "Channel 1 lead-off detection negative inputs";
	    public static final String REG7_CH1_LEAD_OFF_DETECT_POS_INPUTS = "Channel 1 lead-off detection positive inputs";
	    
	    public static final String REG8_CLOCK_DIVIDER_SELECTION = "Clock divider selection";
	    public static final String REG8_RLD_LEAD_OFF_STATUS = "RLD lead-off status";
	    public static final String REG8_CH2_NEG_ELECTRODE_STATUS = "Channel 2 negative electrode status";
	    public static final String REG8_CH2_POS_ELECTRODE_STATUS = "Channel 2 positive electrode status";
	    public static final String REG8_CH1_NEG_ELECTRODE_STATUS = "Channel 1 negative electrode status";
	    public static final String REG8_CH1_POS_ELECTRODE_STATUS = "Channel 1 positive electrode status";
	    
	    public static final String REG9_RESPIRATION_DEMOD_CIRCUITRY = "Respiration demodulation circuitry";
	    public static final String REG9_RESPIRATION_MOD_CIRCUITRY = "Respiration modulation circuitry";
	    public static final String REG9_RESPIRATION_PHASE = "Respiration phase";
	    public static final String REG9_RESPIRATION_CONTROL = "Respiration control";

	    public static final String REG10_RESPIRATION_CALIBRATION = "Respiration calibration on";
	    public static final String REG10_RESPIRATION_CONTROL_FREQUENCY = "Respiration control frequency";
	    public static final String REG10_RLD_REFERENCE_SIGNAL = "RLD reference signal";
	}
	
	public static final class EXG_SETTING_OPTIONS{
		public static final class REG1{
			public static final class CONVERSION_MODES{
				public static final ExGConfigOption CONTINUOUS = new ExGConfigOption(EXG_SETTINGS.REG1_CONVERSION_MODES, "Continuous Conversion Mode", 0);
				public static final ExGConfigOption SINGLE_SHOT = new ExGConfigOption(EXG_SETTINGS.REG1_CONVERSION_MODES, "Single-shot mode", 1);
			}
			public static class DATA_RATE{
				public static final ExGConfigOption RATE_125SPS = new ExGConfigOption(EXG_SETTINGS.REG1_DATA_RATE, "125 Hz", 0);
				public static final ExGConfigOption RATE_250SPS = new ExGConfigOption(EXG_SETTINGS.REG1_DATA_RATE, "250 Hz", 1);
				public static final ExGConfigOption RATE_500SPS = new ExGConfigOption(EXG_SETTINGS.REG1_DATA_RATE, "500 Hz", 2);
				public static final ExGConfigOption RATE_1KSPS = new ExGConfigOption(EXG_SETTINGS.REG1_DATA_RATE, "1 kHz", 3);
				public static final ExGConfigOption RATE_2KSPS = new ExGConfigOption(EXG_SETTINGS.REG1_DATA_RATE, "2 kHz", 4);
				public static final ExGConfigOption RATE_4KSPS = new ExGConfigOption(EXG_SETTINGS.REG1_DATA_RATE, "4 kHz", 5);
				public static final ExGConfigOption RATE_8KSPS = new ExGConfigOption(EXG_SETTINGS.REG1_DATA_RATE, "8 kHz", 6);
			}
		}
		public static final class REG2{
			public static final class LEAD_OFF_COMPARATORS{
				public static final ExGConfigOption OFF = new ExGConfigOption(EXG_SETTINGS.REG2_LEAD_OFF_COMPARATORS, "OFF", 0);
				public static final ExGConfigOption ON = new ExGConfigOption(EXG_SETTINGS.REG2_LEAD_OFF_COMPARATORS, "ON", 1);
			}
			public static final class REFERENCE_BUFFER{
				public static final ExGConfigOption OFF = new ExGConfigOption(EXG_SETTINGS.REG2_REFERENCE_BUFFER, "OFF", 0);
				public static final ExGConfigOption ON = new ExGConfigOption(EXG_SETTINGS.REG2_REFERENCE_BUFFER, "ON", 1);
			}
			public static final class VOLTAGE_REFERENCE{
				public static final ExGConfigOption VREF_2_42V = new ExGConfigOption(EXG_SETTINGS.REG2_VOLTAGE_REFERENCE, "2.42 V", 0);
				public static final ExGConfigOption VREF_4_033V = new ExGConfigOption(EXG_SETTINGS.REG2_VOLTAGE_REFERENCE, "4.033 V", 1);
			}
			public static final class OSCILLATOR_CLOCK_CONNECTION{
				public static final ExGConfigOption OFF = new ExGConfigOption(EXG_SETTINGS.REG2_OSCILLATOR_CLOCK_CONNECTION, "OFF", 0);
				public static final ExGConfigOption ON = new ExGConfigOption(EXG_SETTINGS.REG2_OSCILLATOR_CLOCK_CONNECTION, "ON", 1);
			}
			public static final class TEST_SIGNAL_SELECTION{
				public static final ExGConfigOption OFF = new ExGConfigOption(EXG_SETTINGS.REG2_TEST_SIGNAL_SELECTION, "OFF", 0);
				public static final ExGConfigOption ON = new ExGConfigOption(EXG_SETTINGS.REG2_TEST_SIGNAL_SELECTION, "ON", 1);
			}
			public static final class TEST_SIGNAL_FREQUENCY{
				public static final ExGConfigOption DC = new ExGConfigOption(EXG_SETTINGS.REG2_TEST_SIGNAL_FREQUENCY, "DC", 0);
				public static final ExGConfigOption SQUARE_WAVE_1KHZ = new ExGConfigOption(EXG_SETTINGS.REG2_TEST_SIGNAL_FREQUENCY, "1 kHz Square Wave", 1);
			}
		}

		public static final class REG3{
			public static final class COMPARATOR_THRESHOLD{
				public static final ExGConfigOption POS95NEG5 = new ExGConfigOption(EXG_SETTINGS.REG3_COMPARATOR_THRESHOLD, "Pos:95%-Neg:5%", 0);
				public static final ExGConfigOption POS92_5NEG7_5 = new ExGConfigOption(EXG_SETTINGS.REG3_COMPARATOR_THRESHOLD, "Pos:92.5%-Neg:7.5%", 1);
				public static final ExGConfigOption POS90NEG10 = new ExGConfigOption(EXG_SETTINGS.REG3_COMPARATOR_THRESHOLD, "Pos:90%-Neg:10%", 2);
				public static final ExGConfigOption POS87_5NEG12_5 = new ExGConfigOption(EXG_SETTINGS.REG3_COMPARATOR_THRESHOLD, "Pos:87.5%-Neg:12.5%", 3);
				public static final ExGConfigOption POS85NEG15 = new ExGConfigOption(EXG_SETTINGS.REG3_COMPARATOR_THRESHOLD, "Pos:85%-Neg:15%", 4);
				public static final ExGConfigOption POS80NEG20 = new ExGConfigOption(EXG_SETTINGS.REG3_COMPARATOR_THRESHOLD, "Pos:80%-Neg:20%", 5);
				public static final ExGConfigOption POS75NEG25 = new ExGConfigOption(EXG_SETTINGS.REG3_COMPARATOR_THRESHOLD, "Pos:75%-Neg:25%", 6);
				public static final ExGConfigOption POS70NEG30 = new ExGConfigOption(EXG_SETTINGS.REG3_COMPARATOR_THRESHOLD, "Pos:70%-Neg:30%", 7);
			}
			public static final class LEAD_OFF_CURRENT{
				public static final ExGConfigOption CURRENT_6NA = new ExGConfigOption(EXG_SETTINGS.REG3_LEAD_OFF_CURRENT, "6 nA", 0);
				public static final ExGConfigOption CURRENT_22NA = new ExGConfigOption(EXG_SETTINGS.REG3_LEAD_OFF_CURRENT, "22 nA", 1);
				public static final ExGConfigOption CURRENT_6UA = new ExGConfigOption(EXG_SETTINGS.REG3_LEAD_OFF_CURRENT, "6 uA", 2);
				public static final ExGConfigOption CURRENT_22UA = new ExGConfigOption(EXG_SETTINGS.REG3_LEAD_OFF_CURRENT, "22 uA", 3);
			}
			public static final class LEAD_OFF_FREQUENCY{
				public static final ExGConfigOption DC = new ExGConfigOption(EXG_SETTINGS.REG3_LEAD_OFF_FREQUENCY, "DC lead-off detect", 0);
				public static final ExGConfigOption AC = new ExGConfigOption(EXG_SETTINGS.REG3_LEAD_OFF_FREQUENCY, "AC lead-off detect (fs / 4)", 1);
			}
		}
		
		public static final class REG4{
			public static final class CH1_POWER_DOWN{
				public static final ExGConfigOption NORMAL_OPERATION = new ExGConfigOption(EXG_SETTINGS.REG4_CHANNEL_1_POWER_DOWN, "Normal operation", 0);
				public static final ExGConfigOption POWER_DOWN = new ExGConfigOption(EXG_SETTINGS.REG4_CHANNEL_1_POWER_DOWN, "Power-down", 1);
			}
			public static final class CH1_PGA_GAIN{
				public static final ExGConfigOption GAIN_6 = new ExGConfigOption(EXG_SETTINGS.REG4_CHANNEL_1_PGA_GAIN, "6", 0);
				public static final ExGConfigOption GAIN_1 = new ExGConfigOption(EXG_SETTINGS.REG4_CHANNEL_1_PGA_GAIN, "1", 1);
				public static final ExGConfigOption GAIN_2 = new ExGConfigOption(EXG_SETTINGS.REG4_CHANNEL_1_PGA_GAIN, "2", 2);
				public static final ExGConfigOption GAIN_3 = new ExGConfigOption(EXG_SETTINGS.REG4_CHANNEL_1_PGA_GAIN, "3", 3);
				public static final ExGConfigOption GAIN_4 = new ExGConfigOption(EXG_SETTINGS.REG4_CHANNEL_1_PGA_GAIN, "4", 4);
				public static final ExGConfigOption GAIN_8 = new ExGConfigOption(EXG_SETTINGS.REG4_CHANNEL_1_PGA_GAIN, "8", 5);
				public static final ExGConfigOption GAIN_12 = new ExGConfigOption(EXG_SETTINGS.REG4_CHANNEL_1_PGA_GAIN, "12", 6);
			}
			public static final class CH1_INPUT_SELECTION{
				public static final ExGConfigOption NORMAL = new ExGConfigOption(EXG_SETTINGS.REG4_CHANNEL_1_INPUT_SELECTION, "Normal electrode input", 0);
				public static final ExGConfigOption SHORTED = new ExGConfigOption(EXG_SETTINGS.REG4_CHANNEL_1_INPUT_SELECTION, "Input shorted", 1);
				public static final ExGConfigOption RLD_MEASURE = new ExGConfigOption(EXG_SETTINGS.REG4_CHANNEL_1_INPUT_SELECTION, "RLD_MEASURE", 2);
				public static final ExGConfigOption SUPPLY_MEASURE = new ExGConfigOption(EXG_SETTINGS.REG4_CHANNEL_1_INPUT_SELECTION, "MVDD for supply measurement", 3);
				public static final ExGConfigOption TEMPERATURE = new ExGConfigOption(EXG_SETTINGS.REG4_CHANNEL_1_INPUT_SELECTION, "Temperature sensor", 4);
				public static final ExGConfigOption TEST_SIGNAL = new ExGConfigOption(EXG_SETTINGS.REG4_CHANNEL_1_INPUT_SELECTION, "Test signal", 5);
				public static final ExGConfigOption RLDIN_CONNECTED_TO_POS_INPUT = new ExGConfigOption(EXG_SETTINGS.REG4_CHANNEL_1_INPUT_SELECTION, "RLD_DRP (positive side connected to RLDIN)", 6);
				public static final ExGConfigOption RLDIN_CONNECTED_TO_NEG_INPUT = new ExGConfigOption(EXG_SETTINGS.REG4_CHANNEL_1_INPUT_SELECTION, "RLD_DRM (negative side connected to RLDIN)", 7);
				public static final ExGConfigOption RLDIN_CONNECTED_TO_POS_AND_NEG_INPUT = new ExGConfigOption(EXG_SETTINGS.REG4_CHANNEL_1_INPUT_SELECTION, "RLD_DRPM (both positive and negative sides connected to RLDIN)", 8);
				public static final ExGConfigOption ROUTE_CH3_TO_CH1 = new ExGConfigOption(EXG_SETTINGS.REG4_CHANNEL_1_INPUT_SELECTION, "Route IN3P and IN3N to channel 1 inputs", 9);
			}
		}

		public static final class REG5{
			public static final class CH2_POWER_DOWN{
				public static final ExGConfigOption NORMAL_OPERATION = new ExGConfigOption(EXG_SETTINGS.REG5_CHANNEL_2_POWER_DOWN, "Normal operation", 0);
				public static final ExGConfigOption POWER_DOWN = new ExGConfigOption(EXG_SETTINGS.REG5_CHANNEL_2_POWER_DOWN, "Power-down", 1);
			}
			public static final class CH2_PGA_GAIN{
				public static final ExGConfigOption GAIN_6 = new ExGConfigOption(EXG_SETTINGS.REG5_CHANNEL_2_PGA_GAIN, "6", 0);
				public static final ExGConfigOption GAIN_1 = new ExGConfigOption(EXG_SETTINGS.REG5_CHANNEL_2_PGA_GAIN, "1", 1);
				public static final ExGConfigOption GAIN_2 = new ExGConfigOption(EXG_SETTINGS.REG5_CHANNEL_2_PGA_GAIN, "2", 2);
				public static final ExGConfigOption GAIN_3 = new ExGConfigOption(EXG_SETTINGS.REG5_CHANNEL_2_PGA_GAIN, "3", 3);
				public static final ExGConfigOption GAIN_4 = new ExGConfigOption(EXG_SETTINGS.REG5_CHANNEL_2_PGA_GAIN, "4", 4);
				public static final ExGConfigOption GAIN_8 = new ExGConfigOption(EXG_SETTINGS.REG5_CHANNEL_2_PGA_GAIN, "8", 5);
				public static final ExGConfigOption GAIN_12 = new ExGConfigOption(EXG_SETTINGS.REG5_CHANNEL_2_PGA_GAIN, "12", 6);
			}
			public static final class CH2_INPUT_SELECTION{
				public static final ExGConfigOption NORMAL = new ExGConfigOption(EXG_SETTINGS.REG5_CHANNEL_2_INPUT_SELECTION, "Normal electrode input", 0);
				public static final ExGConfigOption SHORTED = new ExGConfigOption(EXG_SETTINGS.REG5_CHANNEL_2_INPUT_SELECTION, "Input shorted", 1);
				public static final ExGConfigOption RLD_MEASURE = new ExGConfigOption(EXG_SETTINGS.REG5_CHANNEL_2_INPUT_SELECTION, "RLD_MEASURE", 2);
				public static final ExGConfigOption SUPPLY_MEASURE = new ExGConfigOption(EXG_SETTINGS.REG5_CHANNEL_2_INPUT_SELECTION, "VDD / 2 for supply measurement", 3);
				public static final ExGConfigOption TEMPERATURE = new ExGConfigOption(EXG_SETTINGS.REG5_CHANNEL_2_INPUT_SELECTION, "Temperature sensor", 4);
				public static final ExGConfigOption TEST_SIGNAL = new ExGConfigOption(EXG_SETTINGS.REG5_CHANNEL_2_INPUT_SELECTION, "Test signal", 5);
				public static final ExGConfigOption RLDIN_CONNECTED_TO_POS_INPUT = new ExGConfigOption(EXG_SETTINGS.REG5_CHANNEL_2_INPUT_SELECTION, "RLD_DRP (positive side connected to RLDIN)", 6);
				public static final ExGConfigOption RLDIN_CONNECTED_TO_NEG_INPUT = new ExGConfigOption(EXG_SETTINGS.REG5_CHANNEL_2_INPUT_SELECTION, "RLD_DRM (negative side connected to RLDIN)", 7);
				public static final ExGConfigOption RLDIN_CONNECTED_TO_POS_AND_NEG_INPUTS = new ExGConfigOption(EXG_SETTINGS.REG5_CHANNEL_2_INPUT_SELECTION, "RLD_DRPM (both positive and negative sides connected to RLDIN)", 8);
				public static final ExGConfigOption ROUTE_CH3_TO_CH1 = new ExGConfigOption(EXG_SETTINGS.REG5_CHANNEL_2_INPUT_SELECTION, "Route IN3P and IN3N to channel 1 inputs", 9);
			}
		}

		public static final class REG6{
			public static final class PGA_CHOP_FREQUENCY{
				public static final ExGConfigOption FMOD_16 = new ExGConfigOption(EXG_SETTINGS.REG6_PGA_CHOP_FREQUENCY, "fMOD / 16", 0);
				public static final ExGConfigOption FMOD_2 = new ExGConfigOption(EXG_SETTINGS.REG6_PGA_CHOP_FREQUENCY, "fMOD / 2", 2);
				public static final ExGConfigOption FMOD_4 = new ExGConfigOption(EXG_SETTINGS.REG6_PGA_CHOP_FREQUENCY, "fMOD / 4", 3);
			}
			public static final class RLD_BUFFER_POWER{
				public static final ExGConfigOption POWERED_DOWN = new ExGConfigOption(EXG_SETTINGS.REG6_RLD_BUFFER_POWER, "Powered down", 0);
				public static final ExGConfigOption ENABLED = new ExGConfigOption(EXG_SETTINGS.REG6_RLD_BUFFER_POWER, "Enabled", 1);
			}
			public static final class RLD_LEAD_OFF_SENSE_FUNCTION{
				public static final ExGConfigOption OFF = new ExGConfigOption(EXG_SETTINGS.REG6_RLD_LEAD_OFF_SENSE_FUNCTION, "OFF", 0);
				public static final ExGConfigOption ON = new ExGConfigOption(EXG_SETTINGS.REG6_RLD_LEAD_OFF_SENSE_FUNCTION, "ON", 1);
			}
			public static final class RLD_NEG_INPUTS_CH2{
				public static final ExGConfigOption NOT_CONNECTED = new ExGConfigOption(EXG_SETTINGS.REG6_CH2_RLD_NEG_INPUTS, "Not connected", 0);
				public static final ExGConfigOption RLD_CONNECTED_TO_IN2N = new ExGConfigOption(EXG_SETTINGS.REG6_CH2_RLD_NEG_INPUTS, "Connected to IN2N", 1);
			}
			public static final class RLD_POS_INPUTS_CH2{
				public static final ExGConfigOption NOT_CONNECTED = new ExGConfigOption(EXG_SETTINGS.REG6_CH2_RLD_POS_INPUTS, "Not connected", 0);
				public static final ExGConfigOption RLD_CONNECTED_TO_IN2P = new ExGConfigOption(EXG_SETTINGS.REG6_CH2_RLD_POS_INPUTS, "Connected to IN2P", 1);
			}
			public static final class RLD_NEG_INPUTS_CH1{
				public static final ExGConfigOption NOT_CONNECTED = new ExGConfigOption(EXG_SETTINGS.REG6_CH1_RLD_NEG_INPUTS, "Not connected", 0);
				public static final ExGConfigOption RLD_CONNECTED_TO_IN1N = new ExGConfigOption(EXG_SETTINGS.REG6_CH1_RLD_NEG_INPUTS, "Connected to IN1N", 1);
			}
			public static final class RLD_POS_INPUTS_CH1{
				public static final ExGConfigOption NOT_CONNECTED = new ExGConfigOption(EXG_SETTINGS.REG6_CH1_RLD_POS_INPUTS, "Not connected", 0);
				public static final ExGConfigOption RLD_CONNECTED_TO_IN1P = new ExGConfigOption(EXG_SETTINGS.REG6_CH1_RLD_POS_INPUTS, "Connected to IN1P", 1);
			}
		}

		public static final class REG7{
			public static final class FLIP_CURRENT_CH2{
				public static final ExGConfigOption OFF = new ExGConfigOption(EXG_SETTINGS.REG7_CH2_FLIP_CURRENT, "OFF", 0);
				public static final ExGConfigOption ON = new ExGConfigOption(EXG_SETTINGS.REG7_CH2_FLIP_CURRENT, "ON", 1);
			}
			public static final class FLIP_CURRENT_CH1{
				public static final ExGConfigOption OFF = new ExGConfigOption(EXG_SETTINGS.REG7_CH1_FLIP_CURRENT, "OFF", 0);
				public static final ExGConfigOption ON = new ExGConfigOption(EXG_SETTINGS.REG7_CH1_FLIP_CURRENT, "ON", 1);
			}
			public static final class LEAD_OFF_DETECT_NEG_INPUTS_CH2{
				public static final ExGConfigOption OFF = new ExGConfigOption(EXG_SETTINGS.REG7_CH2_LEAD_OFF_DETECT_NEG_INPUTS, "OFF", 0);
				public static final ExGConfigOption ON = new ExGConfigOption(EXG_SETTINGS.REG7_CH2_LEAD_OFF_DETECT_NEG_INPUTS, "ON", 1);
			}
			public static final class LEAD_OFF_DETECT_POS_INPUTS_CH2{
				public static final ExGConfigOption OFF = new ExGConfigOption(EXG_SETTINGS.REG7_CH2_LEAD_OFF_DETECT_POS_INPUTS, "OFF", 0);
				public static final ExGConfigOption ON = new ExGConfigOption(EXG_SETTINGS.REG7_CH2_LEAD_OFF_DETECT_POS_INPUTS, "ON", 1);
			}
			public static final class LEAD_OFF_DETECT_NEG_INPUTS_CH1{
				public static final ExGConfigOption OFF = new ExGConfigOption(EXG_SETTINGS.REG7_CH1_LEAD_OFF_DETECT_NEG_INPUTS, "OFF", 0);
				public static final ExGConfigOption ON = new ExGConfigOption(EXG_SETTINGS.REG7_CH1_LEAD_OFF_DETECT_NEG_INPUTS, "ON", 1);
			}
			public static final class LEAD_OFF_DETECT_POS_INPUTS_CH1{
				public static final ExGConfigOption OFF = new ExGConfigOption(EXG_SETTINGS.REG7_CH1_LEAD_OFF_DETECT_POS_INPUTS, "OFF", 0);
				public static final ExGConfigOption ON = new ExGConfigOption(EXG_SETTINGS.REG7_CH1_LEAD_OFF_DETECT_POS_INPUTS, "ON", 1);
			}
		}
		
		public static final class REG8{
			public static final class CLOCK_DIVIDER_SELECTION{
				public static final ExGConfigOption OFF = new ExGConfigOption(EXG_SETTINGS.REG7_CH1_LEAD_OFF_DETECT_POS_INPUTS, "fMOD = fCLK / 4", 0);
				public static final ExGConfigOption ON = new ExGConfigOption(EXG_SETTINGS.REG7_CH1_LEAD_OFF_DETECT_POS_INPUTS, "fMOD = fCLK / 16", 1);
			}
		}

		public static final class REG9{
			public static final class RESPIRATION_DEMOD_CIRCUITRY{
				public static final ExGConfigOption OFF = new ExGConfigOption(EXG_SETTINGS.REG9_RESPIRATION_DEMOD_CIRCUITRY, "OFF", 0);
				public static final ExGConfigOption ON = new ExGConfigOption(EXG_SETTINGS.REG9_RESPIRATION_DEMOD_CIRCUITRY, "ON", 1);
			}
			public static final class RESPIRATION_MOD_CIRCUITRY{
				public static final ExGConfigOption OFF = new ExGConfigOption(EXG_SETTINGS.REG9_RESPIRATION_MOD_CIRCUITRY, "OFF", 0);
				public static final ExGConfigOption ON = new ExGConfigOption(EXG_SETTINGS.REG9_RESPIRATION_MOD_CIRCUITRY, "ON", 1);
			}
			public static final class RESPIRATION_PHASE_AT_32KHZ{
				public static final ExGConfigOption PHASE_0 = new ExGConfigOption(EXG_SETTINGS.REG9_RESPIRATION_PHASE, "0\u00B0", 0);
				public static final ExGConfigOption PHASE_11_25 = new ExGConfigOption(EXG_SETTINGS.REG9_RESPIRATION_PHASE, "11.25\u00B0", 1);
				public static final ExGConfigOption PHASE_22_5 = new ExGConfigOption(EXG_SETTINGS.REG9_RESPIRATION_PHASE, "22.5\u00B0", 2);
				public static final ExGConfigOption PHASE_33_75 = new ExGConfigOption(EXG_SETTINGS.REG9_RESPIRATION_PHASE, "33.75\u00B0", 3);
				public static final ExGConfigOption PHASE_45 = new ExGConfigOption(EXG_SETTINGS.REG9_RESPIRATION_PHASE, "45\u00B0", 4);
				public static final ExGConfigOption PHASE_56_25 = new ExGConfigOption(EXG_SETTINGS.REG9_RESPIRATION_PHASE, "56.25\u00B0", 5);
				public static final ExGConfigOption PHASE_67_5 = new ExGConfigOption(EXG_SETTINGS.REG9_RESPIRATION_PHASE, "67.5\u00B0", 6);
				public static final ExGConfigOption PHASE_78_75 = new ExGConfigOption(EXG_SETTINGS.REG9_RESPIRATION_PHASE, "78.75\u00B0", 7);
				public static final ExGConfigOption PHASE_90 = new ExGConfigOption(EXG_SETTINGS.REG9_RESPIRATION_PHASE, "90\u00B0", 8);
				public static final ExGConfigOption PHASE_101_25 = new ExGConfigOption(EXG_SETTINGS.REG9_RESPIRATION_PHASE, "101.25\u00B0", 9);
				public static final ExGConfigOption PHASE_112_5 = new ExGConfigOption(EXG_SETTINGS.REG9_RESPIRATION_PHASE, "112.5\u00B0", 10);
				public static final ExGConfigOption PHASE_123_75 = new ExGConfigOption(EXG_SETTINGS.REG9_RESPIRATION_PHASE, "123.75\u00B0", 11);
				public static final ExGConfigOption PHASE_135 = new ExGConfigOption(EXG_SETTINGS.REG9_RESPIRATION_PHASE, "135\u00B0", 12);
				public static final ExGConfigOption PHASE_146_25 = new ExGConfigOption(EXG_SETTINGS.REG9_RESPIRATION_PHASE, "146.25\u00B0", 13);
				public static final ExGConfigOption PHASE_157_5 = new ExGConfigOption(EXG_SETTINGS.REG9_RESPIRATION_PHASE, "157.5\u00B0", 14);
				public static final ExGConfigOption PHASE_168_75 = new ExGConfigOption(EXG_SETTINGS.REG9_RESPIRATION_PHASE, "168.75\u00B0", 15);
			}
			public static final class RESPIRATION_PHASE_AT_64KHZ{
				public static final ExGConfigOption PHASE_0 = new ExGConfigOption(EXG_SETTINGS.REG9_RESPIRATION_PHASE, "0\u00B0", 0);
				public static final ExGConfigOption PHASE_22_5 = new ExGConfigOption(EXG_SETTINGS.REG9_RESPIRATION_PHASE, "22.5\u00B0", 1);
				public static final ExGConfigOption PHASE_45 = new ExGConfigOption(EXG_SETTINGS.REG9_RESPIRATION_PHASE, "45\u00B0", 2);
				public static final ExGConfigOption PHASE_67_5 = new ExGConfigOption(EXG_SETTINGS.REG9_RESPIRATION_PHASE, "67.5\u00B0", 3);
				public static final ExGConfigOption PHASE_90 = new ExGConfigOption(EXG_SETTINGS.REG9_RESPIRATION_PHASE, "90\u00B0", 4);
				public static final ExGConfigOption PHASE_112_5 = new ExGConfigOption(EXG_SETTINGS.REG9_RESPIRATION_PHASE, "112.5\u00B0", 5);
				public static final ExGConfigOption PHASE_135 = new ExGConfigOption(EXG_SETTINGS.REG9_RESPIRATION_PHASE, "135\u00B0", 6);
				public static final ExGConfigOption PHASE_157_5 = new ExGConfigOption(EXG_SETTINGS.REG9_RESPIRATION_PHASE, "157.5\u00B0", 7);
			}
			public static final class RESPIRATION_CONTROL{
				public static final ExGConfigOption INTERNAL_CLOCK = new ExGConfigOption(EXG_SETTINGS.REG9_RESPIRATION_CONTROL, "Internal clock", 0);
				public static final ExGConfigOption EXTERNAL_CLOCK = new ExGConfigOption(EXG_SETTINGS.REG9_RESPIRATION_CONTROL, "External Clock", 1);
			}
		}
		
		public static final class REG10{
			public static final class RESPIRATION_CALIBRATION{
				public static final ExGConfigOption OFF = new ExGConfigOption(EXG_SETTINGS.REG10_RESPIRATION_CALIBRATION, "OFF", 0);
				public static final ExGConfigOption ON = new ExGConfigOption(EXG_SETTINGS.REG10_RESPIRATION_CALIBRATION, "ON", 1);
			}
			public static final class RESPIRATION_CONTROL_FREQUENCY{
				public static final ExGConfigOption FREQ_32KHZ = new ExGConfigOption(EXG_SETTINGS.REG10_RESPIRATION_CONTROL_FREQUENCY, "32 kHz", 0);
				public static final ExGConfigOption FREQ_64KHZ = new ExGConfigOption(EXG_SETTINGS.REG10_RESPIRATION_CONTROL_FREQUENCY, "64 kHz", 1);
			}
			public static final class RLD_REFERENCE_SIGNAL{
				public static final ExGConfigOption FED_EXTERNALLY = new ExGConfigOption(EXG_SETTINGS.REG10_RLD_REFERENCE_SIGNAL, "Fed externally", 0);
				public static final ExGConfigOption HALF_OF_SUPPLY = new ExGConfigOption(EXG_SETTINGS.REG10_RLD_REFERENCE_SIGNAL, "(AVDD - AVSS) / 2", 1);
			}
		}
	}
	
	public ExGConfigBytesDetails() {
		super();
		startNewExGConig();
	}
	
	
	public final static Map<String, ExGConfigOptionDetails> mMapOfExGSettingsChip1;
    static {
    	Map<String, ExGConfigOptionDetails> aMap = new HashMap<String, ExGConfigOptionDetails>();
    	
    	ExGConfigOption[] exGConfigOptions = getExGConfigOptionFields(EXG_SETTING_OPTIONS.REG1.CONVERSION_MODES.class);
    	aMap.put(EXG_SETTINGS.REG1_CONVERSION_MODES, new ExGConfigOptionDetails(EXG_CHIP_INDEX.CHIP1, 0, EXG_SETTINGS.REG1_CONVERSION_MODES, exGConfigOptions, 7, 0x01));
    	exGConfigOptions = getExGConfigOptionFields(EXG_SETTING_OPTIONS.REG1.DATA_RATE.class);
    	aMap.put(EXG_SETTINGS.REG1_DATA_RATE, new ExGConfigOptionDetails(EXG_CHIP_INDEX.CHIP1, 0, EXG_SETTINGS.REG1_DATA_RATE, exGConfigOptions, 0, 0x07));

    	aMap.put(EXG_SETTINGS.REG2_LEAD_OFF_COMPARATORS, new ExGConfigOptionDetails(EXG_CHIP_INDEX.CHIP1, 1, EXG_SETTINGS.REG2_LEAD_OFF_COMPARATORS, 6));
    	aMap.put(EXG_SETTINGS.REG2_REFERENCE_BUFFER, new ExGConfigOptionDetails(EXG_CHIP_INDEX.CHIP1, 1, EXG_SETTINGS.REG2_REFERENCE_BUFFER, 5));
    	exGConfigOptions = getExGConfigOptionFields(EXG_SETTING_OPTIONS.REG2.VOLTAGE_REFERENCE.class);
    	aMap.put(EXG_SETTINGS.REG2_VOLTAGE_REFERENCE, new ExGConfigOptionDetails(EXG_CHIP_INDEX.CHIP1, 1, EXG_SETTINGS.REG2_VOLTAGE_REFERENCE, exGConfigOptions, 4, 0x01));
    	aMap.put(EXG_SETTINGS.REG2_OSCILLATOR_CLOCK_CONNECTION, new ExGConfigOptionDetails(EXG_CHIP_INDEX.CHIP1, 1, EXG_SETTINGS.REG2_OSCILLATOR_CLOCK_CONNECTION, 3));
    	aMap.put(EXG_SETTINGS.REG2_TEST_SIGNAL_SELECTION, new ExGConfigOptionDetails(EXG_CHIP_INDEX.CHIP1, 1, EXG_SETTINGS.REG2_TEST_SIGNAL_SELECTION, 1));
    	exGConfigOptions = getExGConfigOptionFields(EXG_SETTING_OPTIONS.REG2.TEST_SIGNAL_FREQUENCY.class);
    	aMap.put(EXG_SETTINGS.REG2_TEST_SIGNAL_FREQUENCY, new ExGConfigOptionDetails(EXG_CHIP_INDEX.CHIP1, 1, EXG_SETTINGS.REG2_TEST_SIGNAL_FREQUENCY, exGConfigOptions, 0, 0x01));

    	exGConfigOptions = getExGConfigOptionFields(EXG_SETTING_OPTIONS.REG3.COMPARATOR_THRESHOLD.class);
    	aMap.put(EXG_SETTINGS.REG3_COMPARATOR_THRESHOLD, new ExGConfigOptionDetails(EXG_CHIP_INDEX.CHIP1, 2, EXG_SETTINGS.REG3_COMPARATOR_THRESHOLD, exGConfigOptions, 5, 0x07));
    	exGConfigOptions = getExGConfigOptionFields(EXG_SETTING_OPTIONS.REG3.LEAD_OFF_CURRENT.class);
    	aMap.put(EXG_SETTINGS.REG3_LEAD_OFF_CURRENT, new ExGConfigOptionDetails(EXG_CHIP_INDEX.CHIP1, 2, EXG_SETTINGS.REG3_LEAD_OFF_CURRENT, exGConfigOptions, 2, 0x03));
    	exGConfigOptions = getExGConfigOptionFields(EXG_SETTING_OPTIONS.REG3.LEAD_OFF_FREQUENCY.class);
    	aMap.put(EXG_SETTINGS.REG3_LEAD_OFF_FREQUENCY, new ExGConfigOptionDetails(EXG_CHIP_INDEX.CHIP1, 2, EXG_SETTINGS.REG3_LEAD_OFF_FREQUENCY, exGConfigOptions, 0, 0x01));

    	exGConfigOptions = getExGConfigOptionFields(EXG_SETTING_OPTIONS.REG4.CH1_POWER_DOWN.class);
    	aMap.put(EXG_SETTINGS.REG4_CHANNEL_1_POWER_DOWN, new ExGConfigOptionDetails(EXG_CHIP_INDEX.CHIP1, 3, EXG_SETTINGS.REG4_CHANNEL_1_POWER_DOWN, exGConfigOptions, 7, 0x01));
    	exGConfigOptions = getExGConfigOptionFields(EXG_SETTING_OPTIONS.REG4.CH1_PGA_GAIN.class);
    	aMap.put(EXG_SETTINGS.REG4_CHANNEL_1_PGA_GAIN, new ExGConfigOptionDetails(EXG_CHIP_INDEX.CHIP1, 3, EXG_SETTINGS.REG4_CHANNEL_1_PGA_GAIN, exGConfigOptions, 4, 0x07));
    	exGConfigOptions = getExGConfigOptionFields(EXG_SETTING_OPTIONS.REG4.CH1_INPUT_SELECTION.class);
    	aMap.put(EXG_SETTINGS.REG4_CHANNEL_1_INPUT_SELECTION, new ExGConfigOptionDetails(EXG_CHIP_INDEX.CHIP1, 3, EXG_SETTINGS.REG4_CHANNEL_1_INPUT_SELECTION, exGConfigOptions, 0, 0x0F));

    	exGConfigOptions = getExGConfigOptionFields(EXG_SETTING_OPTIONS.REG5.CH2_POWER_DOWN.class);
    	aMap.put(EXG_SETTINGS.REG5_CHANNEL_2_POWER_DOWN, new ExGConfigOptionDetails(EXG_CHIP_INDEX.CHIP1, 4, EXG_SETTINGS.REG5_CHANNEL_2_POWER_DOWN, exGConfigOptions, 7, 0x01));
    	exGConfigOptions = getExGConfigOptionFields(EXG_SETTING_OPTIONS.REG5.CH2_PGA_GAIN.class);
    	aMap.put(EXG_SETTINGS.REG5_CHANNEL_2_PGA_GAIN, new ExGConfigOptionDetails(EXG_CHIP_INDEX.CHIP1, 4, EXG_SETTINGS.REG5_CHANNEL_2_PGA_GAIN, exGConfigOptions, 4, 0x07));
    	exGConfigOptions = getExGConfigOptionFields(EXG_SETTING_OPTIONS.REG5.CH2_INPUT_SELECTION.class);
    	aMap.put(EXG_SETTINGS.REG5_CHANNEL_2_INPUT_SELECTION, new ExGConfigOptionDetails(EXG_CHIP_INDEX.CHIP1, 4, EXG_SETTINGS.REG5_CHANNEL_2_INPUT_SELECTION, exGConfigOptions, 0, 0x0F));

    	exGConfigOptions = getExGConfigOptionFields(EXG_SETTING_OPTIONS.REG6.PGA_CHOP_FREQUENCY.class);
    	aMap.put(EXG_SETTINGS.REG6_PGA_CHOP_FREQUENCY, new ExGConfigOptionDetails(EXG_CHIP_INDEX.CHIP1, 5, EXG_SETTINGS.REG6_PGA_CHOP_FREQUENCY, exGConfigOptions, 6, 0x03));
    	exGConfigOptions = getExGConfigOptionFields(EXG_SETTING_OPTIONS.REG6.RLD_BUFFER_POWER.class);
    	aMap.put(EXG_SETTINGS.REG6_RLD_BUFFER_POWER, new ExGConfigOptionDetails(EXG_CHIP_INDEX.CHIP1, 5, EXG_SETTINGS.REG6_RLD_BUFFER_POWER, exGConfigOptions, 5, 0x01));
    	aMap.put(EXG_SETTINGS.REG6_RLD_LEAD_OFF_SENSE_FUNCTION, new ExGConfigOptionDetails(EXG_CHIP_INDEX.CHIP1, 5, EXG_SETTINGS.REG6_RLD_LEAD_OFF_SENSE_FUNCTION, 4));
    	exGConfigOptions = getExGConfigOptionFields(EXG_SETTING_OPTIONS.REG6.RLD_NEG_INPUTS_CH2.class);
    	aMap.put(EXG_SETTINGS.REG6_CH2_RLD_NEG_INPUTS, new ExGConfigOptionDetails(EXG_CHIP_INDEX.CHIP1, 5, EXG_SETTINGS.REG6_CH2_RLD_NEG_INPUTS, exGConfigOptions, 3, 0x01));
    	exGConfigOptions = getExGConfigOptionFields(EXG_SETTING_OPTIONS.REG6.RLD_POS_INPUTS_CH2.class);
    	aMap.put(EXG_SETTINGS.REG6_CH2_RLD_POS_INPUTS, new ExGConfigOptionDetails(EXG_CHIP_INDEX.CHIP1, 5, EXG_SETTINGS.REG6_CH2_RLD_POS_INPUTS, exGConfigOptions, 2, 0x01));
    	exGConfigOptions = getExGConfigOptionFields(EXG_SETTING_OPTIONS.REG6.RLD_NEG_INPUTS_CH1.class);
    	aMap.put(EXG_SETTINGS.REG6_CH1_RLD_NEG_INPUTS, new ExGConfigOptionDetails(EXG_CHIP_INDEX.CHIP1, 5, EXG_SETTINGS.REG6_CH1_RLD_NEG_INPUTS, exGConfigOptions, 1, 0x01));
    	exGConfigOptions = getExGConfigOptionFields(EXG_SETTING_OPTIONS.REG6.RLD_POS_INPUTS_CH1.class);
    	aMap.put(EXG_SETTINGS.REG6_CH1_RLD_POS_INPUTS, new ExGConfigOptionDetails(EXG_CHIP_INDEX.CHIP1, 5, EXG_SETTINGS.REG6_CH1_RLD_POS_INPUTS, exGConfigOptions, 0, 0x01));

    	aMap.put(EXG_SETTINGS.REG7_CH2_FLIP_CURRENT, new ExGConfigOptionDetails(EXG_CHIP_INDEX.CHIP1, 6, EXG_SETTINGS.REG7_CH2_FLIP_CURRENT, 5));
    	aMap.put(EXG_SETTINGS.REG7_CH1_FLIP_CURRENT, new ExGConfigOptionDetails(EXG_CHIP_INDEX.CHIP1, 6, EXG_SETTINGS.REG7_CH1_FLIP_CURRENT, 4));
    	aMap.put(EXG_SETTINGS.REG7_CH2_LEAD_OFF_DETECT_NEG_INPUTS, new ExGConfigOptionDetails(EXG_CHIP_INDEX.CHIP1, 6, EXG_SETTINGS.REG7_CH2_LEAD_OFF_DETECT_NEG_INPUTS, 3));
    	aMap.put(EXG_SETTINGS.REG7_CH2_LEAD_OFF_DETECT_POS_INPUTS, new ExGConfigOptionDetails(EXG_CHIP_INDEX.CHIP1, 6, EXG_SETTINGS.REG7_CH2_LEAD_OFF_DETECT_POS_INPUTS, 2));
    	aMap.put(EXG_SETTINGS.REG7_CH1_LEAD_OFF_DETECT_NEG_INPUTS, new ExGConfigOptionDetails(EXG_CHIP_INDEX.CHIP1, 6, EXG_SETTINGS.REG7_CH1_LEAD_OFF_DETECT_NEG_INPUTS, 1));
    	aMap.put(EXG_SETTINGS.REG7_CH1_LEAD_OFF_DETECT_POS_INPUTS, new ExGConfigOptionDetails(EXG_CHIP_INDEX.CHIP1, 6, EXG_SETTINGS.REG7_CH1_LEAD_OFF_DETECT_POS_INPUTS, 0));

    	exGConfigOptions = getExGConfigOptionFields(EXG_SETTING_OPTIONS.REG8.CLOCK_DIVIDER_SELECTION.class);
    	aMap.put(EXG_SETTINGS.REG8_CLOCK_DIVIDER_SELECTION, new ExGConfigOptionDetails(EXG_CHIP_INDEX.CHIP1, 7, EXG_SETTINGS.REG8_CLOCK_DIVIDER_SELECTION, exGConfigOptions, 6, 0x01));
    	aMap.put(EXG_SETTINGS.REG8_RLD_LEAD_OFF_STATUS, new ExGConfigOptionDetails(EXG_CHIP_INDEX.CHIP1, 7, EXG_SETTINGS.REG8_RLD_LEAD_OFF_STATUS, 4));
    	aMap.put(EXG_SETTINGS.REG8_CH2_NEG_ELECTRODE_STATUS, new ExGConfigOptionDetails(EXG_CHIP_INDEX.CHIP1, 7, EXG_SETTINGS.REG8_CH2_NEG_ELECTRODE_STATUS, 3));
    	aMap.put(EXG_SETTINGS.REG8_CH2_POS_ELECTRODE_STATUS, new ExGConfigOptionDetails(EXG_CHIP_INDEX.CHIP1, 7, EXG_SETTINGS.REG8_CH2_POS_ELECTRODE_STATUS, 2));
    	aMap.put(EXG_SETTINGS.REG8_CH1_NEG_ELECTRODE_STATUS, new ExGConfigOptionDetails(EXG_CHIP_INDEX.CHIP1, 7, EXG_SETTINGS.REG8_CH1_NEG_ELECTRODE_STATUS, 1));
    	aMap.put(EXG_SETTINGS.REG8_CH1_POS_ELECTRODE_STATUS, new ExGConfigOptionDetails(EXG_CHIP_INDEX.CHIP1, 7, EXG_SETTINGS.REG8_CH1_POS_ELECTRODE_STATUS, 0));

    	exGConfigOptions = getExGConfigOptionFields(EXG_SETTING_OPTIONS.REG9.RESPIRATION_DEMOD_CIRCUITRY.class);
    	aMap.put(EXG_SETTINGS.REG9_RESPIRATION_DEMOD_CIRCUITRY, new ExGConfigOptionDetails(EXG_CHIP_INDEX.CHIP1, 8, EXG_SETTINGS.REG9_RESPIRATION_DEMOD_CIRCUITRY, exGConfigOptions, 7, 0x01));
    	exGConfigOptions = getExGConfigOptionFields(EXG_SETTING_OPTIONS.REG9.RESPIRATION_MOD_CIRCUITRY.class);
    	aMap.put(EXG_SETTINGS.REG9_RESPIRATION_MOD_CIRCUITRY, new ExGConfigOptionDetails(EXG_CHIP_INDEX.CHIP1, 8, EXG_SETTINGS.REG9_RESPIRATION_MOD_CIRCUITRY, exGConfigOptions, 6, 0x01));
    	exGConfigOptions = getExGConfigOptionFields(EXG_SETTING_OPTIONS.REG9.RESPIRATION_PHASE_AT_32KHZ.class);
    	aMap.put(EXG_SETTINGS.REG9_RESPIRATION_PHASE, new ExGConfigOptionDetails(EXG_CHIP_INDEX.CHIP1, 8, EXG_SETTINGS.REG9_RESPIRATION_PHASE, exGConfigOptions, 2, 0x0F));
    	exGConfigOptions = getExGConfigOptionFields(EXG_SETTING_OPTIONS.REG9.RESPIRATION_CONTROL.class);
    	aMap.put(EXG_SETTINGS.REG9_RESPIRATION_CONTROL, new ExGConfigOptionDetails(EXG_CHIP_INDEX.CHIP1, 8, EXG_SETTINGS.REG9_RESPIRATION_CONTROL, exGConfigOptions, 0, 0x01));

    	exGConfigOptions = getExGConfigOptionFields(EXG_SETTING_OPTIONS.REG10.RESPIRATION_CALIBRATION.class);
    	aMap.put(EXG_SETTINGS.REG10_RESPIRATION_CALIBRATION, new ExGConfigOptionDetails(EXG_CHIP_INDEX.CHIP1, 9, EXG_SETTINGS.REG10_RESPIRATION_CALIBRATION, exGConfigOptions, 7, 0x01));
    	exGConfigOptions = getExGConfigOptionFields(EXG_SETTING_OPTIONS.REG10.RESPIRATION_CONTROL_FREQUENCY.class);
    	aMap.put(EXG_SETTINGS.REG10_RESPIRATION_CONTROL_FREQUENCY, new ExGConfigOptionDetails(EXG_CHIP_INDEX.CHIP1, 9, EXG_SETTINGS.REG10_RESPIRATION_CONTROL_FREQUENCY, exGConfigOptions, 2, 0x01));
    	exGConfigOptions = getExGConfigOptionFields(EXG_SETTING_OPTIONS.REG10.RLD_REFERENCE_SIGNAL.class);
    	aMap.put(EXG_SETTINGS.REG10_RLD_REFERENCE_SIGNAL, new ExGConfigOptionDetails(EXG_CHIP_INDEX.CHIP1, 9, EXG_SETTINGS.REG10_RLD_REFERENCE_SIGNAL, exGConfigOptions, 1, 0x01));

        mMapOfExGSettingsChip1 = Collections.unmodifiableMap(aMap);
    }
	
    
	public void startNewExGConig(){
		mMapOfExGSettingsChip1ThisShimmer.clear();
		mMapOfExGSettingsChip2ThisShimmer.clear();
		
		for(String key:mMapOfExGSettingsChip1.keySet()){
			mMapOfExGSettingsChip1ThisShimmer.put(key, 0);
			mMapOfExGSettingsChip2ThisShimmer.put(key, 0);
		}
	}

	public static ExGConfigOption[] getExGConfigOptionFields(Class c) {
	    List<ExGConfigOption> fields = new ArrayList<ExGConfigOption>();
	    java.lang.reflect.Field[] f = c.getDeclaredFields();
	    for (java.lang.reflect.Field field : f) {
	        int modifier = field.getModifiers();
	        if (java.lang.reflect.Modifier.isStatic(modifier)){
				try {
					ExGConfigOption val = (ExGConfigOption) field.get( null );
//		            System.out.print(field.getName() +  ": " + val.guiValue + ", ");
		            fields.add(val);
				} catch (IllegalArgumentException | IllegalAccessException e) {
//					e.printStackTrace();
				}
	        }
	    }
	    
		ExGConfigOption[] exGConfigOptions = fields.toArray(new ExGConfigOption[fields.size()]);
	    return exGConfigOptions;
	}
    
    public byte[] generateExgByteArray(EXG_CHIP_INDEX chipIndex){
    	byte[] byteArray = new byte[10];
    	HashMap<String, Integer> mMapOfExGSettingsToRef = mMapOfExGSettingsChip1ThisShimmer;
    	if(chipIndex==EXG_CHIP_INDEX.CHIP2){
    		mMapOfExGSettingsToRef = mMapOfExGSettingsChip2ThisShimmer;
    	}
    	
    	for(String key:mMapOfExGSettingsToRef.keySet()){
    		byteArray[mMapOfExGSettingsChip1.get(key).byteIndex] |= ((mMapOfExGSettingsToRef.get(key) & mMapOfExGSettingsChip1.get(key).mask) << mMapOfExGSettingsChip1.get(key).bitShift);
    	}
    	
    	setExgByteArrayConstants(byteArray);
    	
//		System.out.println(byteArray[5]);
		return byteArray;
    }

    /**Set the 'Must be' bits listed in the manual
     * @param byteArray
     */
    public void setExgByteArrayConstants(byte[] byteArray){
    	//CONFIG1:								//0x00
    	byteArray[0] &= ~(BIT6+BIT5+BIT4+BIT3);
    	//CONFIG2:								//0x80
    	byteArray[1] |= BIT7;
    	byteArray[1] &= ~(BIT2);
    	//LOFF:									//0x10
    	byteArray[2] |= BIT4;
    	byteArray[2] &= ~(BIT1);
    	//LOFF_SENS:							//0x00
    	byteArray[6] &= ~(BIT7+BIT6);
    	//LOFF_STAT:							//0x00
    	byteArray[7] &= ~(BIT7+BIT5+BIT4+BIT3+BIT2+BIT1+BIT0);
    	//RESP1:								//0x02
    	byteArray[8] |= BIT1;
    	//RESP2:								//0x01
    	byteArray[9] &= ~(BIT6+BIT5+BIT4+BIT3);
    	byteArray[9] |= BIT0;    	
    }
    

	public void updateFromRegisterArray(EXG_CHIP_INDEX chipIndex, byte[] registerArray) {
    	HashMap<String, Integer> mMapOfExGSettingsToRef = mMapOfExGSettingsChip1ThisShimmer;
    	if(chipIndex==EXG_CHIP_INDEX.CHIP2){
    		mMapOfExGSettingsToRef = mMapOfExGSettingsChip2ThisShimmer;
    	}
    	
    	for(String key:mMapOfExGSettingsToRef.keySet()){
//    		Integer i = (registerArray[mMapOfExGSettingsChip1.get(key).byteIndex] >> mMapOfExGSettingsChip1.get(key).bitShift) & mMapOfExGSettingsChip1.get(key).mask;
//    		System.err.println(key + " : " +String.valueOf(i));
			mMapOfExGSettingsToRef.put(key,(registerArray[mMapOfExGSettingsChip1.get(key).byteIndex] >> mMapOfExGSettingsChip1.get(key).bitShift) & mMapOfExGSettingsChip1.get(key).mask); 
    	}
	}

	public void setExgPropertySingleChip(EXG_CHIP_INDEX chipIndex, ExGConfigOption option) {
		HashMap<String, Integer> mapToRef = mMapOfExGSettingsChip1ThisShimmer;
		if(chipIndex==EXG_CHIP_INDEX.CHIP2){
			mapToRef = mMapOfExGSettingsChip2ThisShimmer;
		}
		
		if(mapToRef!=null){
			if(mapToRef.containsKey(option.settingTitle)){
				mapToRef.put(option.settingTitle, option.configValueInt); 
			}
		}
	}
	
	public int getExgPropertySingleChip(EXG_CHIP_INDEX chipIndex, String propertyName) {
		HashMap<String, Integer> mapToRef = mMapOfExGSettingsChip1ThisShimmer;
		if(chipIndex==EXG_CHIP_INDEX.CHIP2){
			mapToRef = mMapOfExGSettingsChip2ThisShimmer;
		}
		
		if(mapToRef!=null){
			if(mapToRef.containsKey(propertyName)){
				//System.err.println(propertyName + ": " +mapToRef.get(propertyName));
				return mapToRef.get(propertyName); 
			}
		}
		return -1;
	}
	
	public void setExgPropertyBothChips(ExGConfigOption option) {
		EXG_CHIP_INDEX chipIndex = EXG_CHIP_INDEX.CHIP1;
		for(int i=1;i<=2;i++){
			if(i==2){
				chipIndex = EXG_CHIP_INDEX.CHIP2;
			}
			setExgPropertySingleChip(chipIndex, option);
		}
	}

	public boolean isExgPropertyEnabled(EXG_CHIP_INDEX chipIndex, ExGConfigOption option) {
		HashMap<String, Integer> mapToRef = mMapOfExGSettingsChip1ThisShimmer;
		if(chipIndex==EXG_CHIP_INDEX.CHIP2){
			mapToRef = mMapOfExGSettingsChip2ThisShimmer;
		}
		
		if(mapToRef!=null){
			if(mapToRef.containsKey(option.settingTitle)){
				
				if(option==EXG_SETTING_OPTIONS.REG3.LEAD_OFF_FREQUENCY.DC){
					return (mapToRef.get(option.settingTitle)==0? true:false); 
				}
				else if(option==EXG_SETTING_OPTIONS.REG3.LEAD_OFF_FREQUENCY.AC){
					return (mapToRef.get(option.settingTitle)==1? true:false); 
				}
				
				return (mapToRef.get(option.settingTitle)>=1? true:false); 
			}
		}
		return false;
	}

	public byte[] getEXG1RegisterArray() {
		return generateExgByteArray(EXG_CHIP_INDEX.CHIP1);
	}



	public byte[] getEXG2RegisterArray() {
		return generateExgByteArray(EXG_CHIP_INDEX.CHIP2);
	}



	public void setExgPropertyValue(EXG_CHIP_INDEX chipIndex, String propertyName, int value) {
		HashMap<String, Integer> mapToRef = mMapOfExGSettingsChip1ThisShimmer;
		if(chipIndex==EXG_CHIP_INDEX.CHIP2){
			mapToRef = mMapOfExGSettingsChip2ThisShimmer;
		}
		
		if(mapToRef!=null){
			if(mapToRef.containsKey(propertyName)){
				mapToRef.put(propertyName, value); 
			}
		}
	}
	
	
	public int getEXGReferenceElectrode(){
		int returnVal = 0;
		String[] settingsToCheck = new String[]{
				EXG_SETTINGS.REG6_CH2_RLD_NEG_INPUTS,
				EXG_SETTINGS.REG6_CH2_RLD_POS_INPUTS,
				EXG_SETTINGS.REG6_CH1_RLD_NEG_INPUTS,
				EXG_SETTINGS.REG6_CH1_RLD_POS_INPUTS
		};
		
		for(String propertyName:settingsToCheck){
			int value = getExgPropertySingleChip(EXG_CHIP_INDEX.CHIP1, propertyName);
			if(value!=-1){
				returnVal |= (value & mMapOfExGSettingsChip1.get(propertyName).mask) << mMapOfExGSettingsChip1.get(propertyName).bitShift; 
			}
		}
		
		return returnVal;
	}
	
}
