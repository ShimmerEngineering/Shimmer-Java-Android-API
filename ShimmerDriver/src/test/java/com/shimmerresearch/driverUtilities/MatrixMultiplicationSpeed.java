package com.shimmerresearch.driverUtilities;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
//import org.jblas.DoubleMatrix;

import com.shimmerresearch.driver.calibration.UtilCalibration;

public class MatrixMultiplicationSpeed {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		double[][] a= {{1,2,3}};
		double[][] b = {{1,2,3},{1,2,3},{1,2,3}};
		long nanotime = System.nanoTime()/1000000;
		double[][] c = UtilCalibration.matrixMultiplication(a,b);
		long nanotime2 = System.nanoTime()/1000000;
		System.out.println("Shimmer Library:" + (nanotime2-nanotime)+"ms");
		System.out.println(c[0][0] + " " + c[0][1]  + " " + c[0][2]);
		
		RealMatrix n = new Array2DRowRealMatrix(a);
		RealMatrix m = new Array2DRowRealMatrix(b);
		long nanotime3 = System.nanoTime()/1000000;
		RealMatrix o = n.multiply(m);
		long nanotime4 = System.nanoTime()/1000000;
		System.out.println("Apache Commons:" + (nanotime4-nanotime3)+"ms");
		System.out.println(o.getData()[0][0]+ " " + o.getData()[0][1]+ " " + o.getData()[0][2]);
		
		/*DoubleMatrix r = new DoubleMatrix(a);
		DoubleMatrix s = new DoubleMatrix(b);
		
		long nanotime5 = System.nanoTime()/1000000;
		DoubleMatrix t = r.mmul(s);
		long nanotime6 = System.nanoTime()/1000000;
		System.out.println("JBLAS:" + (nanotime6-nanotime5)+"ms");
		System.out.println(t.get(0, 0) + " " + t.get(0, 1) + " " + t.get(0, 2));
		*/
		
		

		/*
		r.put(0, a[0][0]);
		r.put(3, a[0][1]);
		r.put(6, a[0][2]);
		r.put(1, a[1][0]);
		r.put(4, a[1][1]);
		r.put(7, a[1][2]);
		r.put(2, a[2][0]);
		r.put(5, a[2][1]);
		r.put(8, a[2][2]);

		s.put(0, a[0][0]);
		s.put(3, a[0][1]);
		s.put(6, a[0][2]);
		s.put(1, a[1][0]);
		s.put(4, a[1][1]);
		s.put(7, a[1][2]);
		s.put(2, a[2][0]);
		s.put(5, a[2][1]);
		s.put(8, a[2][2]);
*/
		
	}

}
