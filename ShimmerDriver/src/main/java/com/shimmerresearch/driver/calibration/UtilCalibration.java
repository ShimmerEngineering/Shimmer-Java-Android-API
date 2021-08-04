package com.shimmerresearch.driver.calibration;

import java.util.Arrays;

/** 
 * @author Ruud Stolk
 * @author Mark Nolan
 * 
 */
public class UtilCalibration {
	
	public final static boolean USE_EFFICIENT_CALIBRATION_METHOD = true;

	/**  Based on the theory outlined by Ferraris F, Grimaldi U, and Parvis M.  
    in "Procedure for effortless in-field calibration of three-axis rate gyros and accelerometers" Sens. Mater. 1995; 7: 311-30.            
    C = [R^(-1)] .[K^(-1)] .([U]-[B])
		where.....
		[C] -> [3 x n] Calibrated Data Matrix 
		[U] -> [3 x n] Uncalibrated Data Matrix
		[B] ->  [3 x n] Replicated Sensor Offset Vector Matrix 
		[R^(-1)] -> [3 x 3] Inverse Alignment Matrix
		[K^(-1)] -> [3 x 3] Inverse Sensitivity Matrix
		n = Number of Samples
	 */
	public static double[] calibrateInertialSensorData(double[] data, double[][] AM, double[][] SM, double[][] OV) {
		if(data==null || AM==null || SM==null || OV==null){
			System.out.println("UtilCalibration.calibrateInertialSensorData:" + "ERROR! NaN in input data");
			return null;
		}

		if(USE_EFFICIENT_CALIBRATION_METHOD){
			return calibrateInertialSensorData(data, matrixMultiplication(matrixInverse3x3(AM),matrixInverse3x3(SM)), OV);
		} else {
			double [][] data2d=new double [3][1];
			data2d[0][0]=data[0];
			data2d[1][0]=data[1];
			data2d[2][0]=data[2];
			data2d= matrixMultiplication(matrixMultiplication(matrixInverse3x3(AM),matrixInverse3x3(SM)),matrixMinus(data2d,OV));
			double[] ansdata=new double[3];
			ansdata[0]=data2d[0][0];
			ansdata[1]=data2d[1][0];
			ansdata[2]=data2d[2][0];
			
			if(Double.isNaN(ansdata[0]) || Double.isNaN(ansdata[1]) || Double.isNaN(ansdata[2])){
				System.out.println("UtilCalibration.calibrateInertialSensorData:" + "ERROR! NaN in calibrated data");
			}
			
			return ansdata;
		}
	}

	
	/**  Based on the theory outlined by Ferraris F, Grimaldi U, and Parvis M.  
    in "Procedure for effortless in-field calibration of three-axis rate gyros and accelerometers" Sens. Mater. 1995; 7: 311-30.            
    C = [R^(-1)] .[K^(-1)] .([U]-[B])
		where.....
		[C] -> [3 x n] Calibrated Data Matrix 
		[U] -> [3 x n] Uncalibrated Data Matrix
		[B] ->  [3 x n] Replicated Sensor Offset Vector Matrix 
		[R^(-1)] -> [3 x 3] Inverse Alignment Matrix
		[K^(-1)] -> [3 x 3] Inverse Sensitivity Matrix
		n = Number of Samples
		
		More efficient method whereby one consistent matrixMultiplication calculation is preperformed once and it's 
		result passed in rather then on a per packet basis.  
		
	 */
	public static double[] calibrateInertialSensorData(double[] data, double[][] matrixMultipliedInverseAMSM, double[][] OV) {
		if(data==null || matrixMultipliedInverseAMSM==null || OV==null){
			System.out.println("UtilCalibration.calibrateInertialSensorData:" + "ERROR! NaN in input data");
			return null;
		}

		double [][] data2d=new double [3][1];
		data2d[0][0]=data[0];
		data2d[1][0]=data[1];
		data2d[2][0]=data[2];
		data2d= matrixMultiplication(matrixMultipliedInverseAMSM,matrixMinus(data2d,OV));
		double[] ansdata=new double[3];
		ansdata[0]=data2d[0][0];
		ansdata[1]=data2d[1][0];
		ansdata[2]=data2d[2][0];
		
		if(Double.isNaN(ansdata[0]) || Double.isNaN(ansdata[1]) || Double.isNaN(ansdata[2])){
			System.out.println("UtilCalibration.calibrateInertialSensorData:" + "ERROR! NaN in calibrated data");
		}
		
		return ansdata;
	}

	/**
	 * @param data
	 * @param calibDetails
	 * @return
	 */
	public static double[] calibrateInertialSensorData(double[] data, CalibDetailsKinematic calibDetails) {
		if(calibDetails==null){
			return data;
		}
		
		if(USE_EFFICIENT_CALIBRATION_METHOD){
			return calibrateInertialSensorData(data, 
			calibDetails.getValidMatrixMultipliedInverseAMSM(), 
			calibDetails.getValidOffsetVector());
		} else {
			return calibrateInertialSensorData(data, 
					calibDetails.getValidAlignmentMatrix(), 
					calibDetails.getValidSensitivityMatrix(), 
					calibDetails.getValidOffsetVector());
		}
	}
	
	/**
	 * GGIR runs on default calibrated Accel data. The auto-calibration stage in
	 * GGIR generates correction values (sensitivity and offset -> not alignment)
	 * that can be applied to the default calibrated data to provide auto-calibrated
	 * accel output. This method applies those correction values to the default
	 * calibrated data.
	 * 
	 * @param xyzArray
	 * @param currentSensitivityMatrix
	 * @param currentOffsetVector
	 * @return
	 */
	public static double[] calibrateImuData(double[] xyzArray, double[][] currentSensitivityMatrix, double[][] currentOffsetVector) {
		double[] xyzCalArray = new double[3];
		for(int axis=0; axis<3; axis++) {
			xyzCalArray[axis] = xyzArray[axis]*currentSensitivityMatrix[axis][axis] + currentOffsetVector[axis][0];
		}
		return xyzCalArray;
	}

	public static double[][] matrixInverse3x3(double[][] data) {
		if(data==null){
			return null;
		}
			
		double a,b,c,d,e,f,g,h,i;
		a=data[0][0];
		b=data[0][1];
		c=data[0][2];
		d=data[1][0];
		e=data[1][1];
		f=data[1][2];
		g=data[2][0];
		h=data[2][1];
		i=data[2][2];
		//
		double deter=a*e*i+b*f*g+c*d*h-c*e*g-b*d*i-a*f*h;
		double[][] answer=new double[3][3];
		answer[0][0]=(1/deter)*(e*i-f*h);

		answer[0][1]=(1/deter)*(c*h-b*i);
		answer[0][2]=(1/deter)*(b*f-c*e);
		answer[1][0]=(1/deter)*(f*g-d*i);
		answer[1][1]=(1/deter)*(a*i-c*g);
		answer[1][2]=(1/deter)*(c*d-a*f);
		answer[2][0]=(1/deter)*(d*h-e*g);
		answer[2][1]=(1/deter)*(g*b-a*h);
		answer[2][2]=(1/deter)*(a*e-b*d);
		return answer;
	}
	
	public static double[][] matrixMinus(double[][] a ,double[][] b) {
		if(a==null || b==null){
			return null;
		}
		
		int aRows = a.length,
				aColumns = a[0].length,
				bRows = b.length,
				bColumns = b[0].length;
		if (( aColumns != bColumns )&&( aRows != bRows )) {
			throw new IllegalArgumentException(" Matrix did not match");
		}
		double[][] resultant = new double[aRows][bColumns];
		for(int i = 0; i < aRows; i++) { // aRow
			for(int k = 0; k < aColumns; k++) { // aColumn

				resultant[i][k]=a[i][k]-b[i][k];

			}
		}
		return resultant;
	}
	
	public static double[][] matrixMultiplication(double[][] a, double[][] b) {
		if(a==null || b==null){
			return null;
		}
		
		int aRows = a.length,
				aColumns = a[0].length,
				bRows = b.length,
				bColumns = b[0].length;

		if ( aColumns != bRows ) {
			throw new IllegalArgumentException("A:Rows: " + aColumns + " did not match B:Columns " + bRows + ".");
		}

		double[][] resultant = new double[aRows][bColumns];

		for(int i = 0; i < aRows; i++) { // aRow
			for(int j = 0; j < bColumns; j++) { // bColumn
				for(int k = 0; k < aColumns; k++) { // aColumn
					resultant[i][j] += a[i][k] * b[k][j];
				}
			}
		}
		return resultant;
	}

	public static boolean isCalibrationEqual(
			double[][] alignmentMatrixToTest, 
			double[][] offsetVectorToTest, 
			double[][] sensitivityMatrixToTest, 
			double[][] alignmentVectorToCompare,
			double[][] offsetVectorToCompare, 
			double[][] sensitivityVectorToCompare) {

		boolean alignmentPass = Arrays.deepEquals(alignmentVectorToCompare, alignmentMatrixToTest);
		boolean offsetPass = Arrays.deepEquals(offsetVectorToCompare, offsetVectorToTest);
		boolean sensitivityPass = Arrays.deepEquals(sensitivityVectorToCompare, sensitivityMatrixToTest);

		if(alignmentPass&&offsetPass&&sensitivityPass){
			return true;
		}
		return false;
	}
}
