package com.shimmerresearch.driverUtilities;

import java.util.ArrayList;
import java.util.List;

import org.jtransforms.fft.DoubleFFT_1D;

import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.Configuration.CHANNEL_UNITS;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_TYPE;
import com.shimmerresearch.guiUtilities.AbstractPlotManager;

/** References 
*
*[1] Angkoon Phinyomark, Pornchai Phukpattaranont, Chusak Limsakul (2012) “Feature Reduction and Selection for EMG Signal Classification” Elsevier, Expert Systems with Applications 39, P7420–7431
*[2] http://luscinia.sourceforge.net/page26/page35/page35.html
* */

public class FftCalculateDetails{
	
	private String[] mTraceName = null;
	private String mShimmerName = null;
	private double mSamplingRate = 1024;//Double.NaN;
	private int mDivider = 2;
	private List<Double> mTimeBuffer = new ArrayList<Double>();
	private List<Double> mDataBuffer = new ArrayList<Double>();
	private double[][] psdResults; 
	public double meanFreq;
	public double medianFreq; 
	
	//TODO add support for the below
	private int mFftOverlapPercent = 0;
	private boolean mIsShowingTwoSidedFFT = false;

	public FftCalculateDetails(String shimmerName, String[] traceName){
		mShimmerName = shimmerName;
		mTraceName = traceName;
	}

	public FftCalculateDetails(String shimmerName, String[] traceName, double samplingRate){
		this(shimmerName, traceName);
		mSamplingRate = samplingRate;
	}
	
	public FftCalculateDetails() {
		// TODO Auto-generated constructor stub
	}
	public void addData(double xData, double yData){
		mTimeBuffer.add(xData);
		mDataBuffer.add(yData);
		
		// TODO need to take both sets of data into consideration as samples
		// maybe missing and therefore interpolation (or similar) needs to be
		// performed before and FFT calculation
	}
	
	public void clearBuffers(){
		mDataBuffer.clear();
		mTimeBuffer.clear();
	}
	
	public void clearDataOverlap(){
		//TODO clear window instead of all based on mFftOverlapPercent
		if(mFftOverlapPercent==0){
			mDataBuffer.clear();
			mTimeBuffer.clear();
		}
		else {
			int dataLength = mDataBuffer.size();
			for(int i=0;i<(dataLength*(mFftOverlapPercent/100));i++){
				mDataBuffer.remove(0);
				mTimeBuffer.remove(0);
			}
		}
	}

	
	//TODO calculate FFT on data buffers
	public double[] calculateFft(int timeDiffMs){
		//TODO interpolate missing samples
		
		System.err.println("" + mSamplingRate*(timeDiffMs/1000));
		//TODO check if size is the same as the required minimum, not just >0
		if(mDataBuffer.size()>(mSamplingRate*(timeDiffMs/1000))){
			Double[] inputTemp = mDataBuffer.toArray(new Double[mDataBuffer.size()]);
			
			double[] input = toPrimitive(inputTemp);
			
	        DoubleFFT_1D fftDo = new DoubleFFT_1D(input.length);
	        double[] fft = new double[input.length * 2];
	        System.arraycopy(input, 0, fft, 0, input.length);
	        fftDo.realForwardFull(fft);
	        
	        return fft;
		}
		
		return new double[]{}; //Fail if not enough data points etc.
	}

	public ObjectCluster getResultsObjectCluster() {
		//TODO convert results to object cluster and plot
		return null;
	}
	
	public ObjectCluster[] calculateFftAndGenerateOJC(int timeDiff) {
		double[] fft = calculateFft(timeDiff);
		
		ObjectCluster[] ojcArray = new ObjectCluster[fft.length];
		
		if(fft.length>0){
			int index = 0;
			for(double d:fft){
				ObjectCluster ojc = new ObjectCluster(mShimmerName);
				ojc.createArrayData(2);

				ojc.addData(mTraceName[1], mTraceName[2], mTraceName[3], d);
				ojc.incrementIndexKeeper();
				
				ojc.addData(Configuration.Shimmer3.ObjectClusterSensorName.FREQUENCY, CHANNEL_TYPE.CAL, CHANNEL_UNITS.FREQUENCY, index);
				ojc.incrementIndexKeeper();
				ojcArray[index] = ojc;
				
				index++;
			}
		}
		
		return ojcArray;
	}
	
	public double[][] calculateFftAndGenerateArray(int timeDiff) {
		double[] fft = calculateFft(timeDiff);

		if(fft.length>0){
			if(mIsShowingTwoSidedFFT){
				mDivider = 1;
			}
			
			//TODO improve below - just implementing quick method to calculate sampling rate for the moment
			if(mTimeBuffer.size()>2){
				mSamplingRate = 1/(mTimeBuffer.get(1) - mTimeBuffer.get(0));
			}
			
			double[][] results = new double[2][fft.length/mDivider];
			
			double multiplier = mSamplingRate/fft.length;
			
			System.err.println("FFT LENGTH = " + fft.length);
			
			for(int i=0;i<fft.length/mDivider;i++){
				if(mSamplingRate==Double.NaN){
					//Use index
					results[0][i] = i;
				}
				else{
					//Use freq
					results[0][i] = i*multiplier;
				}
				results[1][i] = fft[i];
			}
			
			calculatePSDAndGenerateArray(fft);
			return results;
		}
		
		return new double[][]{};
	}
	
	/**
	 * PSD, Mean Freq, Median Freq
	 *
	 * Mean Frequency of a spectrum = sum of the product of the spectrogram intensity (in dB) and the frequency, divided by the total sum of spectrogram intensity.
	 */
	public void calculatePSDAndGenerateArray(double[] fft) {

		psdResults = new double[2][fft.length / mDivider];

		double sumProductFreqPsd = 0;
		double sumPsd = 0;
		boolean isMedianValueReached = false;
		//double multiplier = mSamplingRate/fft.length; 
		double multiplier = 1024/fft.length; 

		for (int i = 0; i < fft.length / mDivider; i++) {

			if (mSamplingRate == Double.NaN) {
				// Use index
				psdResults[0][i] = i;
			} else {
				// Use freq
				psdResults[0][i] = i * multiplier;
			}

			psdResults[1][i] = 2 * ((Math.abs(Math.pow(fft[i], 2))) / mSamplingRate * fft.length); // psd = |fft|^2/(fs*N)
			psdResults[1][i] = 10 * Math.log10(psdResults[1][i]); // Convert to DB

			sumProductFreqPsd = sumProductFreqPsd + (psdResults[1][i]) * (psdResults[0][i]);
			sumPsd = sumPsd + psdResults[1][i];
		}

		meanFreq = sumProductFreqPsd / sumPsd; // Calculate Mean [2]
		
		System.err.println("MEAN FREQUECNY = + " + meanFreq);

		// Calculate Median [1]
		// TODO: Better check?
		if (psdResults.length > 0 & !isMedianValueReached) {

			for (int i = 0; i < fft.length / mDivider; i++) {

				if (psdResults[1][i] > sumPsd / 2) {
					medianFreq = psdResults[0][i];
					isMedianValueReached = true;
					break;
				}
			}
			System.err.println("MEDIAN FREQUECNY = + " + medianFreq);
		}
	}
	
	public double[][] getPSDResults(){
		return psdResults; 
	}
	
	public static double[] toPrimitive(Double[] array) {
		if (array == null) {
			return null;
		} else if (array.length == 0) {
			return new double[]{};
		}
		final double[] result = new double[array.length];
		for (int i = 0; i < array.length; i++) {
			result[i] = array[i].doubleValue();
		}
		return result;
		
		//Alternatives
//		double[] arr = frameList.stream().mapToDouble(Double::doubleValue).toArray(); //via method reference
//		double[] arr = frameList.stream().mapToDouble(d -> d).toArray(); //identity function, Java unboxes automatically to get the double value
	}


	public String getTraceNameJoined(){
		return AbstractPlotManager.joinChannelStringArray(mTraceName);
	}

	public void setFftOverlapPercent(int fftOverlapPercent) {
		mFftOverlapPercent = UtilShimmer.nudgeInteger(fftOverlapPercent, 0, 100);
	}
	
}