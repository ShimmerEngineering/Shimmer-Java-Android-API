package com.shimmerresearch.driverUtilities;

import java.io.Serializable;
import java.util.Locale;

/**
 * Holds the Shimmer's Battery charging information (state, voltage and
 * percentage charge) received from communication with the Shimmer's UART.
 * 
 * @author Mark Nolan
 *
 */
public class ShimmerBattStatusDetails implements Serializable {
	
	/****/
	private static final long serialVersionUID = -1108374309087845014L;
	
	public int mChargingStatusRaw = 0;
	private String mChargingStatusParsed = "";
	private CHARGING_STATUS mChargingStatus = CHARGING_STATUS.UNKNOWN;
	public int mBattAdcValue = 0;
	public String mBattVoltageParsed = "";
	public double mBattVoltage = 0.0;
	public Double mEstimatedChargePercentage = 0.0;
	public String mEstimatedChargePercentageParsed = "";
	
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
	
	private class CHARGING_STATUS_BYTE{
		public static final int SUSPENDED = 0x00;
		public static final int FULLY_CHARGED = 0x40;
		public static final int PRECONDITIONING = 0x80;
		public static final int BAD_BATTERY = 0xC0;
	}
	
	public ShimmerBattStatusDetails() {
	}
	
	public ShimmerBattStatusDetails(byte[] rxBuf) {
		if(rxBuf.length >= 3) {
			// Parse response string
            int battAdcValue = (((rxBuf[1]&0xFF) << 8) + (rxBuf[0]&0xFF));
            int chargingStatus = rxBuf[2] & 0xC0;
            update(battAdcValue, chargingStatus);
		}
	}

	public ShimmerBattStatusDetails(int battAdcValue, int chargingStatus) {
		update(battAdcValue, chargingStatus);
	}
	
	private void update(int battAdcValue, int chargingStatus) {
        boolean adcVoltageError = false;
        
        mBattAdcValue = battAdcValue;
        mChargingStatusRaw = chargingStatus;

		// Calibration method copied from
		// com.shimmerresearch.driver.ShimmerObject.calibrateU12AdcValue
		double calibratedData=((double)battAdcValue-0.0)*(((3.0*1000.0)/1.0)/4095.0);
		//double calibratedData = calibrateU12AdcValue((double)battAdcValue, 0.0, 3.0, 1.0);
		mBattVoltage = ((calibratedData * 1.988)) / 1000;
        
        if (mBattVoltage > 4.5) {
        	mChargingStatus = CHARGING_STATUS.CHECKING;
            adcVoltageError = true;
        }
        else if((chargingStatus & 0xFF) == CHARGING_STATUS_BYTE.SUSPENDED) {
        	mChargingStatus = CHARGING_STATUS.SUSPENDED;
        }
        else if ((chargingStatus & 0xFF) == CHARGING_STATUS_BYTE.FULLY_CHARGED) {
        	mChargingStatus = CHARGING_STATUS.FULLY_CHARGED;
        }
        else if ((chargingStatus & 0xFF) == CHARGING_STATUS_BYTE.PRECONDITIONING) {
            mChargingStatus = CHARGING_STATUS.CHARGING;
        }
        else if ((chargingStatus & 0xFF) == CHARGING_STATUS_BYTE.BAD_BATTERY) {
        	mChargingStatus = CHARGING_STATUS.BAD_BATTERY;
        }
        else {
        	mChargingStatus = CHARGING_STATUS.ERROR;
        }

        if(adcVoltageError == false) {
        	processBattPercentage(mBattVoltage);
        	
            if ((chargingStatus&0xFF) != 0xC0) {// Bad battery
//            	mEstimatedChargePercentage = String.format(Locale.UK, "%,.1f",battPercentage) + "%";
            	mEstimatedChargePercentageParsed = String.format("%,.1f",mEstimatedChargePercentage) + "%";
            }
            else {
            	mEstimatedChargePercentageParsed = "0.0%";
            }
        }
	}

	public void processBattPercentage(double battVoltage) {
		mBattVoltage = battVoltage;
//    	mBattVoltage = String.format(Locale.UK, "%,.1f",battVoltage) + " V";
    	mBattVoltageParsed = String.format("%,.1f",mBattVoltage) + " V";
    	
    	// equations are only valid when: 3.2 < x < 4.167. Leaving a 0.2v either side just incase
        if (mBattVoltage > (4.167 + 0.2)) { 
        	mBattVoltage = 4.167;
        }
        else if (mBattVoltage < (3.2 - 0.2)) {
        	mBattVoltage = 3.2;
        }
    	
        // 4th order polynomial fit - good enough for purpose
        mEstimatedChargePercentage = (1109.739792 * Math.pow(mBattVoltage, 4)) - (17167.12674 * Math.pow(mBattVoltage, 3)) + (99232.71686 * Math.pow(mBattVoltage, 2)) - (253825.397 * mBattVoltage) + 242266.0527;

        // 6th order polynomial fit - best fit -> think there is a bug with this one
        //battPercentage = -(29675.10393 * Math.pow(battVoltage, 6)) + (675893.9095 * Math.pow(battVoltage, 5)) - (6404308.2798 * Math.pow(battVoltage, 4)) + (32311485.5704 * Math.pow(battVoltage, 3)) - (91543800.1720 * Math.pow(battVoltage, 2)) + (138081754.0880 * battVoltage) - 86624424.6584;

        if (mEstimatedChargePercentage > 100) {
        	mEstimatedChargePercentage = 100.0;
        }
        else if (mEstimatedChargePercentage < 0) {
        	mEstimatedChargePercentage = 0.0;
        }

    	mEstimatedChargePercentageParsed = String.format("%,.1f",mEstimatedChargePercentage) + "%";
	}

	public String getChargingStatusParsed() {
		mChargingStatusParsed = mChargingStatus.toString();
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

	public String getBattVoltage() {
		return mBattVoltageParsed;
	}

}
