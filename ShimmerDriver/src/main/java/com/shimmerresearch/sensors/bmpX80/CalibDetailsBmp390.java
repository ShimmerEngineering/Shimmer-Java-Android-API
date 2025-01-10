package com.shimmerresearch.sensors.bmpX80;

import java.awt.EventQueue;

import com.shimmerresearch.driverUtilities.UtilParseData;
import com.shimmerresearch.driverUtilities.UtilShimmer;

public class CalibDetailsBmp390 extends CalibDetailsBmpX80 {

	/**	 * 	 */
	private static final long serialVersionUID = 4602512529781344807L;

	public class Bmp3Constants {
	    // Floating-point constants
	    public static final double BMP3_MIN_PRES_DOUBLE = 30000.0;
	    public static final double BMP3_MAX_PRES_DOUBLE = 125000.0;
	    public static final double BMP3_MIN_TEMP_DOUBLE = -40.0;
	    public static final double BMP3_MAX_TEMP_DOUBLE = 85.0;

	    // Byte constants
	    public static final byte BMP3_OK = 0;
	    public static final byte BMP3_W_MIN_TEMP = 3;
	    public static final byte BMP3_W_MAX_TEMP = 4;
	    public static final byte BMP3_W_MIN_PRES = 5;
	    public static final byte BMP3_W_MAX_PRES = 6;
	}
	
	 private static final class Bmp3QuantizedCalibData  {
		 public static double ParT1;
	        public static double ParT2;
	        public static double ParT3;
	        public static double ParP1;
	        public static double ParP2;
	        public static double ParP3;
	        public static double ParP4;
	        public static double ParP5;
	        public static double ParP6;
	        public static double ParP7;
	        public static double ParP8;
	        public static double ParP9;
	        public static double ParP10;
	        public static double ParP11;
	        public static double TLin;
	 }
	 
	 private static final class Bmp3RegCalibData {
		    // Trim Variables
		 public static short ParT1;
	        public static short ParT2;
	        public static byte ParT3;
	        public static short ParP1;
	        public static short ParP2;
	        public static byte ParP3;
	        public static byte ParP4;
	        public static int ParP5;
	        public static int ParP6;
	        public static byte ParP7;
	        public static byte ParP8;
	        public static short ParP9;
	        public static byte ParP10;
	        public static byte ParP11;
	 }
	 
	 public double par_T1 = Bmp3QuantizedCalibData.ParT1; 
	 public double par_T2 = Bmp3QuantizedCalibData.ParT2; 
	 public double par_T3 = Bmp3QuantizedCalibData.ParT3; 
	 public double par_P1 = Bmp3QuantizedCalibData.ParP1;
	 public double par_P2 = Bmp3QuantizedCalibData.ParP2;
	 public double par_P3 = Bmp3QuantizedCalibData.ParP3;
	 public double par_P4 = Bmp3QuantizedCalibData.ParP4;
	 public double par_P5 = Bmp3QuantizedCalibData.ParP5;
	 public double par_P6 = Bmp3QuantizedCalibData.ParP6;
	 public double par_P7 = Bmp3QuantizedCalibData.ParP7;
	 public double par_P8 = Bmp3QuantizedCalibData.ParP8;
	 public double par_P9 =Bmp3QuantizedCalibData.ParP9;
	 public double par_P10 = Bmp3QuantizedCalibData.ParP10;
	 public double par_P11 = Bmp3QuantizedCalibData.ParP11;
	 
	 public void setPressureCalib(
			 	double T1, double T2, double T3,
				double P1, double P2, double P3, 
				double P4, double P5, double P6, 
				double P7, double P8, double P9,
				double P10, double P11) {
			this.par_T1 = T1; 
			this.par_T2 = T2;
			this.par_T3 = T3;
			this.par_P1 = P1;
			this.par_P2 = P2;
			this.par_P3 = P3;
			this.par_P4 = P4;
			this.par_P5 = P5;
			this.par_P6 = P6;
			this.par_P7 = P7;
			this.par_P8 = P8;
			this.par_P9 = P9;
			this.par_P10 = P10;
			this.par_P11 = P11;
	}
	 
	@Override
	public double[] calibratePressureSensorData(double UP, double UT) {
		byte rslt = Bmp3Constants.BMP3_OK;

        double uncompTemp = UT;
        double partialDataT1;
        double partialDataT2;

        partialDataT1 = uncompTemp - Bmp3QuantizedCalibData.ParT1;
        partialDataT2 = partialDataT1 * Bmp3QuantizedCalibData.ParT2;

        Bmp3QuantizedCalibData.TLin = partialDataT2 + (partialDataT1 * partialDataT1) * Bmp3QuantizedCalibData.ParT3;

        if (Bmp3QuantizedCalibData.TLin < Bmp3Constants.BMP3_MIN_TEMP_DOUBLE) {
            Bmp3QuantizedCalibData.TLin = Bmp3Constants.BMP3_MIN_TEMP_DOUBLE;
            rslt = Bmp3Constants.BMP3_W_MIN_TEMP;
        }

        if (Bmp3QuantizedCalibData.TLin > Bmp3Constants.BMP3_MAX_TEMP_DOUBLE) {
            Bmp3QuantizedCalibData.TLin = Bmp3Constants.BMP3_MAX_TEMP_DOUBLE;
            rslt = Bmp3Constants.BMP3_W_MAX_TEMP;
        }		
        
        
        byte result = Bmp3Constants.BMP3_OK;

        // Variable to store the compensated pressure
        double compPress;

        // Temporary variables used for compensation
        double partialData1;
        double partialData2;
        double partialData3;
        double partialData4;
        double partialOut1;
        double partialOut2;

        partialData1 = Bmp3QuantizedCalibData.ParP6 * Bmp3QuantizedCalibData.TLin;
        partialData2 = Bmp3QuantizedCalibData.ParP7 * powBmp3(Bmp3QuantizedCalibData.TLin, 2);
        partialData3 = Bmp3QuantizedCalibData.ParP8 * powBmp3(Bmp3QuantizedCalibData.TLin, 3);
        partialOut1 = Bmp3QuantizedCalibData.ParP5 + partialData1 + partialData2 + partialData3;

        partialData1 = Bmp3QuantizedCalibData.ParP2 * Bmp3QuantizedCalibData.TLin;
        partialData2 = Bmp3QuantizedCalibData.ParP3 * powBmp3(Bmp3QuantizedCalibData.TLin, 2);
        partialData3 = Bmp3QuantizedCalibData.ParP4 * powBmp3(Bmp3QuantizedCalibData.TLin, 3);
        partialOut2 = UP * (Bmp3QuantizedCalibData.ParP1 + partialData1 + partialData2 + partialData3);

        partialData1 = powBmp3(UP, 2);
        partialData2 = Bmp3QuantizedCalibData.ParP9 + Bmp3QuantizedCalibData.ParP10 * Bmp3QuantizedCalibData.TLin;
        partialData3 = partialData1 * partialData2;
        partialData4 = partialData3 + powBmp3(UP, 3) * Bmp3QuantizedCalibData.ParP11;
        compPress = partialOut1 + partialOut2 + partialData4;

        if (compPress < Bmp3Constants.BMP3_MIN_PRES_DOUBLE) {
            compPress = Bmp3Constants.BMP3_MIN_PRES_DOUBLE;
            result = Bmp3Constants.BMP3_W_MIN_PRES;
        }

        if (compPress > Bmp3Constants.BMP3_MAX_PRES_DOUBLE) {
            compPress = Bmp3Constants.BMP3_MAX_PRES_DOUBLE;
            result = Bmp3Constants.BMP3_W_MAX_PRES;
        }

        double[] caldata = new double[2];
		caldata[0]=compPress;
		caldata[1]=Bmp3QuantizedCalibData.TLin;
		return caldata;
	}

	// Method to calculate the power of a number
    private double powBmp3(double base, int exponent) {
        return Math.pow(base, exponent);
    }
    
	@Override
	public byte[] generateCalParamByteArray() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void parseCalParamByteArray(byte[] bufferCalibrationParameters, CALIB_READ_SOURCE calibReadSource) {
		if(calibReadSource.ordinal()>getCalibReadSource().ordinal()){

		if(UtilShimmer.isAllFF(bufferCalibrationParameters)
				||UtilShimmer.isAllZeros(bufferCalibrationParameters)){
			return;
		}
		
		setPressureRawCoefficients(bufferCalibrationParameters);
		setCalibReadSource(calibReadSource);
		 byte[] pressureResoResTest = bufferCalibrationParameters;
	        double tempVar;

	        // 1 / 2^8
	        tempVar = 0.00390625;
	        Bmp3RegCalibData.ParT1 = (short) concatenateBytes(pressureResoResTest[1], pressureResoResTest[0]);
	        Bmp3QuantizedCalibData.ParT1 = ((double) Bmp3RegCalibData.ParT1 / tempVar);

	        Bmp3RegCalibData.ParT2 = (short) concatenateBytes(pressureResoResTest[3], pressureResoResTest[2]);
	        tempVar = 1073741824.0;
	        Bmp3QuantizedCalibData.ParT2 = ((double) Bmp3RegCalibData.ParT2 / tempVar);

	        Bmp3RegCalibData.ParT3 = (byte) pressureResoResTest[4];
	        tempVar = 281474976710656.0;
	        Bmp3QuantizedCalibData.ParT3 = ((double) Bmp3RegCalibData.ParT3 / tempVar);

	        Bmp3RegCalibData.ParP1 = (short) concatenateBytes(pressureResoResTest[6], pressureResoResTest[5]);
	        tempVar = 1048576.0;
	        Bmp3QuantizedCalibData.ParP1 = ((double) (Bmp3RegCalibData.ParP1 - 16384) / tempVar);

	        Bmp3RegCalibData.ParP2 = (short) concatenateBytes(pressureResoResTest[8], pressureResoResTest[7]);
	        tempVar = 536870912.0;
	        Bmp3QuantizedCalibData.ParP2 = ((double) (Bmp3RegCalibData.ParP2 - 16384) / tempVar);

	        Bmp3RegCalibData.ParP3 = (byte) pressureResoResTest[9];
	        tempVar = 4294967296.0;
	        Bmp3QuantizedCalibData.ParP3 = ((double) Bmp3RegCalibData.ParP3 / tempVar);

	        Bmp3RegCalibData.ParP4 = (byte) pressureResoResTest[10];
	        tempVar = 137438953472.0;
	        Bmp3QuantizedCalibData.ParP4 = ((double) Bmp3RegCalibData.ParP4 / tempVar);

	        Bmp3RegCalibData.ParP5 = concatenateBytes(pressureResoResTest[12], pressureResoResTest[11]);

	        // 1 / 2^3
	        tempVar = 0.125;
	        Bmp3QuantizedCalibData.ParP5 = ((double) Bmp3RegCalibData.ParP5 / tempVar);

	        Bmp3RegCalibData.ParP6 = concatenateBytes(pressureResoResTest[14], pressureResoResTest[13]);
	        tempVar = 64.0;
	        Bmp3QuantizedCalibData.ParP6 = ((double) Bmp3RegCalibData.ParP6 / tempVar);

	        Bmp3RegCalibData.ParP7 = (byte) pressureResoResTest[15];
	        tempVar = 256.0;
	        Bmp3QuantizedCalibData.ParP7 = ((double) Bmp3RegCalibData.ParP7 / tempVar);

	        Bmp3RegCalibData.ParP8 = (byte) pressureResoResTest[16];
	        tempVar = 32768.0;
	        Bmp3QuantizedCalibData.ParP8 = ((double) Bmp3RegCalibData.ParP8 / tempVar);

	        Bmp3RegCalibData.ParP9 = (short) concatenateBytes(pressureResoResTest[18], pressureResoResTest[17]);
	        tempVar = 281474976710656.0;
	        Bmp3QuantizedCalibData.ParP9 = ((double) Bmp3RegCalibData.ParP9 / tempVar);

	        Bmp3RegCalibData.ParP10 = (byte) pressureResoResTest[19];
	        tempVar = 281474976710656.0;
	        Bmp3QuantizedCalibData.ParP10 = ((double) Bmp3RegCalibData.ParP10 / tempVar);

	        Bmp3RegCalibData.ParP11 = (byte) pressureResoResTest[20];
	        tempVar = 36893488147419103232.0;
	        Bmp3QuantizedCalibData.ParP11 = ((double) Bmp3RegCalibData.ParP11 / tempVar);

	        System.out.println("par_T1 = " + Bmp3QuantizedCalibData.ParT1);
	        System.out.println("par_T2 = " + Bmp3QuantizedCalibData.ParT2);
	        System.out.println("par_T3 = " + Bmp3QuantizedCalibData.ParT3);
	        System.out.println("par_P1 = " + Bmp3QuantizedCalibData.ParP1);
	        System.out.println("par_P2 = " + Bmp3QuantizedCalibData.ParP2);
	        System.out.println("par_P3 = " + Bmp3QuantizedCalibData.ParP3);
	        System.out.println("par_P4 = " + Bmp3QuantizedCalibData.ParP4);
	        System.out.println("par_P5 = " + Bmp3QuantizedCalibData.ParP5);
	        System.out.println("par_P6 = " + Bmp3QuantizedCalibData.ParP6);
	        System.out.println("par_P7 = " + Bmp3QuantizedCalibData.ParP7);
	        System.out.println("par_P8 = " + Bmp3QuantizedCalibData.ParP8);
	        System.out.println("par_P9 = " + Bmp3QuantizedCalibData.ParP9);
	        System.out.println("par_P10 = " + Bmp3QuantizedCalibData.ParP10);
	        System.out.println("par_P11 = " + Bmp3QuantizedCalibData.ParP11);
		}
	}

	// Concatenate bytes method
    private int concatenateBytes(byte highByte, byte lowByte) {
        return ((highByte & 0xFF) << 8) | (lowByte & 0xFF);
    }
	@Override
	public void resetToDefaultParameters() {
		// TODO Auto-generated method stub
		
	}
	
	public static void main(String[] args) {

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					CalibDetailsBmp390 calibDetailsBmp390 = new CalibDetailsBmp390();
					byte[] pressureResoResTest = {
						    (byte) 0xE7, (byte) 0x6B, (byte) 0xF0, (byte) 0x4A, (byte) 0xF9, 
						    (byte) 0xAB, (byte) 0x1C, (byte) 0x9B, (byte) 0x15, (byte) 0x06, 
						    (byte) 0x01, (byte) 0xD2, (byte) 0x49, (byte) 0x18, (byte) 0x5F, 
						    (byte) 0x03, (byte) 0xFA, (byte) 0x3A, (byte) 0x0F, (byte) 0x07, 
						    (byte) 0xF5
						};
					calibDetailsBmp390.parseCalParamByteArray(pressureResoResTest,CALIB_READ_SOURCE.INFOMEM);
					byte[] sensorDataP = { 
					    (byte) 0x00, (byte) 0x0D, (byte) 0x64 
					};

					byte[] sensorDataT = { 
					    (byte) 0x00, (byte) 0xBA, (byte) 0x7F 
					};

					// Array of strings in Java
					String[] sensorDataType = { "u24" };
					long[] uncalibResultP = UtilParseData.parseData(sensorDataP, sensorDataType);
					long[] uncalibResultT = UtilParseData.parseData(sensorDataT, sensorDataType);
			        System.out.println("uncalibResultP = " + uncalibResultP[0]);
			        System.out.println("uncalibResultT = " + uncalibResultT[0]);

					double[] calibdata = calibDetailsBmp390.calibratePressureSensorData(uncalibResultP[0], uncalibResultT[0]);
			        System.out.println("PRESSURE = " + calibdata[0]);
			        System.out.println("TEMPERATURE = " + calibdata[1]);
			        
			    	byte[] sensorDataP2 = { 
						    (byte) 0x00, (byte) 0x17, (byte) 0x64 
						};

					byte[] sensorDataT2 = { 
						    (byte) 0x00, (byte) 0xCF, (byte) 0x7F 
						};

						// Array of strings in Java
						long[] uncalibResultP2 = UtilParseData.parseData(sensorDataP2, sensorDataType);
						long[] uncalibResultT2 = UtilParseData.parseData(sensorDataT2, sensorDataType);
				        System.out.println("uncalibResultP2 = " + uncalibResultP2[0]);
				        System.out.println("uncalibResultT2 = " + uncalibResultT2[0]);

						double[] calibdata2 = calibDetailsBmp390.calibratePressureSensorData(uncalibResultP2[0], uncalibResultT2[0]);
				        System.out.println("PRESSURE = " + calibdata2[0]);
				        System.out.println("TEMPERATURE = " + calibdata2[1]);

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}


}
