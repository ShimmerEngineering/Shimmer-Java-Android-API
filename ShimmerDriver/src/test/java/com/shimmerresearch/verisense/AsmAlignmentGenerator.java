package com.shimmerresearch.verisense;
import com.shimmerresearch.driver.calibration.UtilCalibration;
import com.shimmerresearch.sensors.AbstractSensor.SENSORS;
import com.shimmerresearch.verisense.sensors.SensorLIS2DW12;

/** A class created to help determining the correct alignment value for calibrating the ASM sensor's IMU
 * 
 * @author Mark Nolan
 *
 */
public class AsmAlignmentGenerator {

//	private CalibDetailsKinematic calibDetailsLis2dw12 = new CalibDetailsKinematic("0", "+- 4g", defaultAlignmentMatrix, defaultSensitivityMatrix, defaultOffsetVector) 

	public static void main(String[] args) {
		
		// As per ASM-DES04-02, for the LIS2DW12 accel:
		// x switched with z
		// y switched with x
		// z switched with y
		double[] lis2dw12Uncal = new double[] {1, 2, 3};
		double[][] lis2dw12AlignmentMatrix = SensorLIS2DW12.DEFAULT_ALIGNMENT_MATRIX_LIS2DW12;
		double[][] lis2dw12SensitivityMatrix = {{1,0,0},{0,1,0},{0,0,1}}; 		
		double[][] lis2dw12OffsetVector = {{0},{0},{0}};
		double[] lis2dw12Cal = UtilCalibration.calibrateInertialSensorData(lis2dw12Uncal, lis2dw12AlignmentMatrix, lis2dw12SensitivityMatrix, lis2dw12OffsetVector);
		printUncalCalData(SENSORS.LIS2DW12.name(), lis2dw12Uncal, lis2dw12Cal);
		
		System.out.println("");
		
		// As per ASM-DES04-02, for the LSM6DS3 accel+gyro:
		// x switched with z
		// y switched with x and the resulting sign changed
		// z switched with y and the resulting sign changed
		double[] lsm6ds3Uncal = new double[] {1, 2, 3};
		double[][] lsm6ds3AlignmentMatrix = {{0,0,1},{-1,0,0},{0,-1,0}};
		double[][] lsm6ds3SensitivityMatrix = {{1,0,0},{0,1,0},{0,0,1}}; 		
		double[][] lsm6ds3OffsetVector = {{0},{0},{0}};
		double[] lsm6ds3Cal = UtilCalibration.calibrateInertialSensorData(lsm6ds3Uncal, lsm6ds3AlignmentMatrix, lsm6ds3SensitivityMatrix, lsm6ds3OffsetVector);
		printUncalCalData("LSM6DS3", lsm6ds3Uncal, lsm6ds3Cal);
	}

	public static void printUncalCalData(String sensor, double[] uncalArray, double[] calArray) {
		System.out.println(sensor + ":");
		System.out.println("\tUncal\tCal");
		System.out.println("\t" + intToAxis(uncalArray[0]) + "  ->  " + checkWhereAxisIsNow(uncalArray[0], calArray));
		System.out.println("\t" + intToAxis(uncalArray[1]) + "  ->  " + checkWhereAxisIsNow(uncalArray[1], calArray));
		System.out.println("\t" + intToAxis(uncalArray[2]) + "  ->  " + checkWhereAxisIsNow(uncalArray[2], calArray));
	}
	
	private static String intToAxis(double d) {
		String axis = (d<0? "-":"");
		if(Math.abs(d)==1) {
			axis += "x";
		} else if(Math.abs(d)==2) {
			axis += "y";
		} else { //if(Math.abs(d)==3) {
			axis += "z";
		}
		return axis;
	}
	
	private static String checkWhereAxisIsNow(double originalAxisValue, double[] calArray) {
		for(int i=0;i<calArray.length;i++) {
			double calValue = calArray[i];
			if(originalAxisValue==Math.abs(calValue)) {
				String result = (calValue<0? "-":"");
				if(i==0) {
					result += "x";
				} else if(i==1) {
					result += "y";
				} else {
					result += "z";
				}
				return result;
			}
		}
		return "?";
	}
	
}
