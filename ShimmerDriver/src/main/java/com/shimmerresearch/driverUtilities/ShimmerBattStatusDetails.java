package com.shimmerresearch.driverUtilities;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import com.shimmerresearch.driverUtilities.ShimmerBattStatusDetails.BATTERY_LEVEL;
import com.shimmerresearch.sensors.SensorADC;

/**
 * Holds the Shimmer's Battery charging information (state, voltage and
 * percentage charge) received from communication with the Shimmer's UART.
 * 
 * @author Mark Nolan
 *
 */
public class ShimmerBattStatusDetails implements Serializable {
	
	private static final long serialVersionUID = -1108374309087845014L;
	
	private int mBattAdcValue = 0;
	
	private int mChargingStatusRaw = 0;
	private CHARGING_STATUS mChargingStatus = CHARGING_STATUS.UNKNOWN;
	private double mBattVoltage = 0.0;
	private Double mEstimatedChargePercentage = -1.0;
	private static final double BATTERY_ERROR_VOLTAGE = 4.5;
	
	public enum CHARGING_STATUS{
		UNKNOWN("Unknown"),
		CHECKING("Checking..."),
		SUSPENDED("Charging suspended"),
		CHARGING("Charging"),
		FULLY_CHARGED("Fully charged"),
		BAD_BATTERY("Bad battery"),
		ERROR("Error");
		
		private final String text;
		
		private CHARGING_STATUS(final String text){
			this.text = text;
		}
		
		@Override
		public String toString(){
			return text;
		}
	}
	
	public class CHARGING_STATUS_BYTE{
		//This are bits that come directly from the Battery Charging chips output pins
		public static final int SUSPENDED = 0xC0;
		public static final int FULLY_CHARGED = 0x40;
		public static final int PRECONDITIONING = 0x80;
		public static final int BAD_BATTERY = 0x00;
		 //Added for devices that don't support the above status bits
		public static final int UNKNOWN = 0xFF;
	}
	
	public enum BATTERY_LEVEL{
        UNKNOWN,
        LOW,
        MEDIUM,
        HIGH
	}
	
	public ShimmerBattStatusDetails() {
	}
	
	
	/** Used via dock communication
	 * @param rxBuf
	 */
	public ShimmerBattStatusDetails(byte[] rxBuf) {
		if(rxBuf.length >= 3) {
			// Parse response string
            int battAdcValue = (((rxBuf[1]&0xFF) << 8) + (rxBuf[0]&0xFF));
            // Parse as unsigned byte and let full byte through to handle UNKNOWN status
            int chargingStatus = rxBuf[2] & 0xFF; 
            update(battAdcValue, chargingStatus);
		}
	}

	/** Used via wireless communication
	 * @param battAdcValue
	 * @param chargingStatus
	 */
	public ShimmerBattStatusDetails(int battAdcValue, int chargingStatus) {
		update(battAdcValue, chargingStatus);
	}
	
	public byte[] generateBattStatusBytes() {
		byte[] battStatusBytes = new byte[3];
		battStatusBytes[0] = (byte) (mBattAdcValue&0xFF);
		battStatusBytes[1] = (byte) ((mBattAdcValue>>8)&0xFF);
		battStatusBytes[2] = (byte) mChargingStatusRaw;
		return battStatusBytes;
	}
	
	public void update(int battAdcValue, int chargingStatus) {
		setBattAdcValue(battAdcValue);
		setChargingStatus(chargingStatus);
	}
	
	public void setBattAdcValue(int battAdcValue) {
        mBattAdcValue = battAdcValue;
        mBattVoltage = adcValToBattVoltage(mBattAdcValue);
        if (mBattVoltage <= BATTERY_ERROR_VOLTAGE) {
        	calculateBattPercentage(mBattVoltage);
        }
	}

	public void setChargingStatus(int chargingStatus) {
        mChargingStatusRaw = chargingStatus;
        
        if (mBattVoltage > BATTERY_ERROR_VOLTAGE) {
        	mChargingStatus = CHARGING_STATUS.CHECKING;
        }
        else if((mChargingStatusRaw & 0xFF) == CHARGING_STATUS_BYTE.SUSPENDED) {
        	mChargingStatus = CHARGING_STATUS.SUSPENDED;
        }
        else if ((mChargingStatusRaw & 0xFF) == CHARGING_STATUS_BYTE.FULLY_CHARGED) {
        	mChargingStatus = CHARGING_STATUS.FULLY_CHARGED;
        }
        else if ((mChargingStatusRaw & 0xFF) == CHARGING_STATUS_BYTE.PRECONDITIONING) {
            mChargingStatus = CHARGING_STATUS.CHARGING;
        }
        else if ((mChargingStatusRaw & 0xFF) == CHARGING_STATUS_BYTE.BAD_BATTERY) {
        	mChargingStatus = CHARGING_STATUS.BAD_BATTERY;
        }
        else if((mChargingStatusRaw & 0xFF) == CHARGING_STATUS_BYTE.UNKNOWN) {
        	mChargingStatus = CHARGING_STATUS.UNKNOWN;
        }
        else {
        	mChargingStatus = CHARGING_STATUS.ERROR;
        }
	}

	public CHARGING_STATUS getChargingStatus() {
		return mChargingStatus;
	}

	public static double adcValToBattVoltage(int adcVal){
		double calibratedData = SensorADC.calibrateU12AdcValueToMillivolts(adcVal, 0.0, 3.0, 1.0);
		double battVoltage = ((calibratedData * 1.988)) / 1000; // 1.988 is due to components on the Shimmmer, 1000 is to convert to volts
		return battVoltage;
	}

	public static int battVoltageToAdcVal(double battVoltage){
		double uncalibratedData = (battVoltage * 1000) / 1.988;
		int adcVal = SensorADC.uncalibrateU12AdcValueFromMillivolts(uncalibratedData, 0.0, 3.0, 1.0);
		return adcVal;
	}

	public void calculateBattPercentage(double battVoltage) {
		mBattVoltage = battVoltage;
    	
    	// equations are only valid when: 3.2 < x < 4.167. Leaving a 0.2v either side just incase
        if (mBattVoltage > (4.167 + 0.2)) { 
        	mBattVoltage = 4.167;
        }
        else if (mBattVoltage < (3.2 - 0.2)) {
        	mBattVoltage = 3.2;
        }
    	
        mEstimatedChargePercentage = battVoltageToBattPercentage(mBattVoltage);
        if (mEstimatedChargePercentage > 100) {
        	mEstimatedChargePercentage = 100.0;
        }
        else if (mEstimatedChargePercentage < 0) {
        	mEstimatedChargePercentage = 0.0;
        }
	}
	
	public static double battVoltageToBattPercentage(double battVoltage) {
        // 4th order polynomial fit - good enough for purpose
        double battPercentage = (1109.739792 * Math.pow(battVoltage, 4)) - (17167.12674 * Math.pow(battVoltage, 3)) + (99232.71686 * Math.pow(battVoltage, 2)) - (253825.397 * battVoltage) + 242266.0527;
        // 6th order polynomial fit - best fit -> think there is a bug with this one
        //battPercentage = -(29675.10393 * Math.pow(battVoltage, 6)) + (675893.9095 * Math.pow(battVoltage, 5)) - (6404308.2798 * Math.pow(battVoltage, 4)) + (32311485.5704 * Math.pow(battVoltage, 3)) - (91543800.1720 * Math.pow(battVoltage, 2)) + (138081754.0880 * battVoltage) - 86624424.6584;
        return battPercentage;
	}

	public static double battPercentageToBattVoltage(double battPercentage) {
        // 4th order polynomial fit - Excel
//		y = -7E-08x4 + 2E-05x3 - 0.0012x2 + 0.0393x + 3.3044
        // 4th order polynomial fit - MATLAB
//		y = - 6.6e-08*x^{4} + 1.6e-05*x^{3} - 0.0012*x^{2} + 0.039*x + 3.3
        double battVoltage =
        		- (6.6e-8 * Math.pow(battPercentage, 4)) 
        		+ (1.6e-5 * Math.pow(battPercentage, 3)) 
        		- (0.0012 * Math.pow(battPercentage, 2)) 
        		+ (0.039 * battPercentage) 
        		+ 3.3;
        return battVoltage;
	}
	
	public static int battPercentageToAdc(double battPercentage){
		double battVoltage = battPercentageToBattVoltage(battPercentage);
		return battVoltageToAdcVal(battVoltage);
	}


	public String getChargingStatusParsed() {
		String mChargingStatusParsed = mChargingStatus.toString();
        if(mChargingStatus==CHARGING_STATUS.CHARGING){
            if (mBattVoltage < 3.0) {// from lm3658 datasheet
            	mChargingStatusParsed += " (Preconditioning)";
            }
            //else if (battVoltage < .0)
            //{
            //    chargingStage = " (Primary charging)";
            //}
            //else if (battVoltage < .0)
            //{
            //    chargingStage = " (Conditioning)";
            //}
            else {
            	mChargingStatusParsed += "...";
            }
        	
        }
		return mChargingStatusParsed;
	}

	public double getBattVoltage() {
		return mBattVoltage;
	}

	public String getBattVoltageParsed() {
    	String mBattVoltageParsed = String.format("%,.3f",mBattVoltage) + " V";
		return mBattVoltageParsed;
	}

	public double getBattAdcValue() {
		return mBattAdcValue;
	}

	public double getEstimatedChargePercentage() {
		return mEstimatedChargePercentage;
	}

	public String getEstimatedChargePercentageParsed() {
		if ((mChargingStatusRaw&0xFF) != CHARGING_STATUS_BYTE.BAD_BATTERY) {// 0xC0 = Bad battery
			if(mEstimatedChargePercentage==-1.0){
				return null;
			}
			else{
//	        	mEstimatedChargePercentage = String.format(Locale.UK, "%,.1f",battPercentage) + "%";
				return (String.format("%,.1f",mEstimatedChargePercentage) + "%");
			}
		}
		else {
			return "0.0%";
		}
	}

	public BATTERY_LEVEL getEstimatedBatteryLevel(){
		return estimateBatteryLevel(mEstimatedChargePercentage);
	}

	public static BATTERY_LEVEL estimateBatteryLevel(double percentageBattery) {
		if(percentageBattery <= 0.0){
			return BATTERY_LEVEL.UNKNOWN;
		}
		else if(percentageBattery < 33){ // 50 on PanelSetup, 25 on LiveData, firmware is below 
			return BATTERY_LEVEL.LOW;
		}
		else if(percentageBattery < 66){ //Software was set to 75, now 65 to match firmware
			return BATTERY_LEVEL.MEDIUM;
		}
		else{
			return BATTERY_LEVEL.HIGH;
		}
	}
	
	/* SDLog Firmware code
	 * void SetBattVal(){
		   if(battStat & BATT_MID){
		      if(*(uint16_t*)battVal<2400){
		         battStat = BATT_LOW;
		      }else if(*(uint16_t*)battVal<2650){
		         battStat = BATT_MID;
		      }else
		         battStat = BATT_HIGH;
		   }else if(battStat & BATT_LOW){
		      if(*(uint16_t*)battVal<2450){
		         battStat = BATT_LOW;
		      }else if(*(uint16_t*)battVal<2600){
		         battStat = BATT_MID;
		      }else
		         battStat = BATT_HIGH;
		   }else{
		      if(*(uint16_t*)battVal<2400){
		         battStat = BATT_LOW;
		      }else if(*(uint16_t*)battVal<2600){
		         battStat = BATT_MID;
		      }else
		         battStat = BATT_HIGH;
		   }
		   battVal[2] = P2IN & 0xC0;
		}
	*/
	
	public static void main(String[] args) {
		
		ShimmerBattStatusDetails shimmerBattStatusDetails = new ShimmerBattStatusDetails();
		
		System.out.println("Testing SDLog <v0.12.3 firmware thresholds...");
		System.out.println(">>>--------------------------------->");
		List<Integer> listOfSdLogThresholdsForLedChange = Arrays.asList(
				2400, //Below 2400 = LOW batt LED (RED)
				2450, //Buffer for transitioning from LOW to MEDIUM
				2600, //Above 2600 = HIGH batt LED (GREEN)
				2650, //Buffer for transitioning from MEDIUM to HIGH
				2670); //Not used in firmware, just high to trigger HIGH
		
		for(Integer i:listOfSdLogThresholdsForLedChange){
			shimmerBattStatusDetails.update(i, 0);
			System.out.println("ADC val: " + i 
					+ "\tVoltage: " + shimmerBattStatusDetails.getBattVoltage() 
					+ "\tPercent: " + shimmerBattStatusDetails.getEstimatedChargePercentageParsed() 
					+ "\tLevel: " + shimmerBattStatusDetails.getEstimatedBatteryLevel().toString());
		}
		
		
		System.out.println("");
		System.out.println("Calculating ADC values from preferred thresholds");
		System.out.println("<---------------------------------<<<");
		List<Double> listOfPreferredThresholdsForLedChange = Arrays.asList(
				0.0,
				33.0,
				66.0,
				100.0);
		for(Double d:listOfPreferredThresholdsForLedChange){
			System.out.println("ADC val: " + ShimmerBattStatusDetails.battPercentageToAdc(d) 
					+ "\tVoltage: " + ShimmerBattStatusDetails.battPercentageToBattVoltage(d) 
					+ "\tPercent: " + d 
//					+ "\tLevel: " + shimmerBattStatusDetails.getEstimatedBatteryLevel().toString()
					+ "\tADC buffer: " + (ShimmerBattStatusDetails.battPercentageToAdc(d)-25) + "-to-" + (ShimmerBattStatusDetails.battPercentageToAdc(d)+25)
					);
		}
		
	}

}
