package com.shimmerresearch.driverUtilities;

import java.util.ArrayList;
import java.util.Arrays;
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
	public double mSamplingRate = 1024; //Double.NaN;
	private int mDivider = 2;
	private List<Double> mTimeBuffer = new ArrayList<Double>();
	private List<Double> mDataBuffer = new ArrayList<Double>();
	private double[][] fftResults; 
	private double[][] psdFrequenciesAndAmplitudes; 
	private double maxPSDFrequency;
	private double meanPSDFreq;
	private double medianPSDFreq; 
	private double sumProductFreqPsd = 0;
	private double sumPsdAmplitude = 0;
	
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

	public double[] calculateFFT(double[] data){
	
		DoubleFFT_1D fftDo = new DoubleFFT_1D(data.length);
        double[] fft = new double[data.length * 2];
        System.arraycopy(data, 0, fft, 0, data.length);
        fftDo.realForwardFull(fft);
     
        fft = rectifyFFT(fft);
        
        return fft;
	}
	
	//TODO calculate FFT on data buffers
	public double[] calculateFft(int timeDiffMs){
		//TODO interpolate missing samples
		
//		System.err.println("" + mSamplingRate*(timeDiffMs/1000));
		//TODO check if size is the same as the required minimum, not just >0
		if(mDataBuffer.size()>((mSamplingRate*(timeDiffMs/1000))*0.9)){ //setting 90% packet loss to be acceptable (arbitrary)
			Double[] inputTemp = mDataBuffer.toArray(new Double[mDataBuffer.size()]);
			
			double[] input = toPrimitive(inputTemp);
			
	        DoubleFFT_1D fftDo = new DoubleFFT_1D(input.length);
	        double[] fft = new double[input.length * 2];
	        System.arraycopy(input, 0, fft, 0, input.length);
	        fftDo.realForwardFull(fft);
	     
	        fft = rectifyFFT(fft);
	        
	        return fft;
		}
		
		return new double[]{}; //Fail if not enough data points etc.
	}

	private double[] rectifyFFT(double[] signal) {
		double[] rectifiedSignal = new double[signal.length];
		for(int i=0;i<signal.length;i++){
			if(signal[i]<0){
				rectifiedSignal[i] = signal[i] * -1;
			}
			else{
				rectifiedSignal[i] = signal[i];
			}
		}
		return rectifiedSignal;
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

				ojc.addDataToMap(mTraceName[1], mTraceName[2], mTraceName[3], d);
				ojc.incrementIndexKeeper();
				
				ojc.addData(Configuration.Shimmer3.ObjectClusterSensorName.FREQUENCY, CHANNEL_TYPE.CAL, CHANNEL_UNITS.FREQUENCY, index);
				ojc.incrementIndexKeeper();
				ojcArray[index] = ojc;
				
				index++;
			}
		}
		
		return ojcArray;
	}
	
	public double[][] calculateFftAndGenerateArray(int periodMs) {

		//TODO improve below - just implementing quick method to calculate sampling rate for the moment
		if(mTimeBuffer.size()>2 && mTimeBuffer.get(0)!=null && mTimeBuffer.get(1)!=null){
			mSamplingRate = 1000/(mTimeBuffer.get(1) - mTimeBuffer.get(0));
		}

		double[] fft = calculateFft(periodMs);

		if(fft.length>0){
			if(mIsShowingTwoSidedFFT){
				mDivider = 1;
			}
			
			fftResults = new double[2][fft.length/mDivider];
			double multiplier = mSamplingRate/fft.length;
			
			for(int index=0; index<fft.length/mDivider; index++){
				setFFTFrequency(index, multiplier);
				setFFTAmplitude(fft, index);
			}	
			
			calculatePSDAndGenerateArray(fft);
			
			return fftResults;
		}
		
		return new double[][]{};
	}
	
	/**
	 * PSD, Mean Freq, Median Freq
	 *
	 * Mean Frequency of a spectrum = sum of the product of the spectrogram intensity (in dB) and the frequency, divided by the total sum of spectrogram intensity.
	 */
	public void calculatePSDAndGenerateArray(double[] fft) {

		int numElements = fft.length/mDivider;
		double multiplier = mSamplingRate/fft.length; 
		
		psdFrequenciesAndAmplitudes = new double[2][numElements];
		sumProductFreqPsd = 0;
		sumPsdAmplitude = 0;

		for (int index = 0; index < numElements; index++) { //is divided by divider right? instead of just fft - half? 

			setPSDFrequency(index, multiplier);
			setPSDAmplitude(fft, index);
			//setPSDAmplitudeInDbs(fft, index);
			
			sumPSDproductFreq(index);
			sumPSDAmplitude(index);
		}

		setMeanPSDFrequency();
		setMedianPSDFrequency();
		setMaxPSDFrequency();
		
	}
	
	private void setFFTFrequency(int index, double multiplier){
		if(Double.isNaN(mSamplingRate)){
			//Use index
			fftResults[0][index] = index;
		}
		else{
			//Use freq
			fftResults[0][index] = index*multiplier;
		}
	}
	
	private void setFFTAmplitude(double[] fft, int index){
		fftResults[1][index] = fft[index];
	}
	
	private void setPSDFrequency(int index, double multiplier){
		if (mSamplingRate == Double.NaN) {
			// Use index
			psdFrequenciesAndAmplitudes[0][index] = index;
		} 
		else{
			// Use freq
			psdFrequenciesAndAmplitudes[0][index] = index * multiplier; //problem i = 0; 
		}
	}
	
	private void setPSDAmplitude(double[] fft, int index){
		psdFrequenciesAndAmplitudes[1][index] = 2*((Math.abs(Math.pow(fft[index], 2)))/mSamplingRate*fft.length); // psd = |fft|^2/(fs*N)
	}
	
	private void setPSDAmplitudeInDbs(double[] fft, int index){
		psdFrequenciesAndAmplitudes[1][index] = 10 * Math.log10(2*((Math.abs(Math.pow(fft[index], 2)))/mSamplingRate*fft.length)); // Convert to DB - Madeleine doesn't use 
	}
	
	private void sumPSDproductFreq(int index){
		sumProductFreqPsd = sumProductFreqPsd + (psdFrequenciesAndAmplitudes[1][index]) * (psdFrequenciesAndAmplitudes[0][index]);
	}
	
	private void sumPSDAmplitude(int index){
		sumPsdAmplitude = sumPsdAmplitude + psdFrequenciesAndAmplitudes[1][index];
	}
	
	private void setMeanPSDFrequency(){
		meanPSDFreq = sumProductFreqPsd/sumPsdAmplitude; 
	}
	
	public double getMeanPSDFrequnecy(){
		return meanPSDFreq;
	}
	
	private void setMedianPSDFrequency(){
		// Calculate Median [1]
		// TODO: Better check?
		for (int index = 0; index < psdFrequenciesAndAmplitudes[0].length; index++) {
			if (psdFrequenciesAndAmplitudes[1][index] > sumPsdAmplitude / 2) {
				medianPSDFreq = psdFrequenciesAndAmplitudes[0][index];
				break;
			}
		}
		
//	    int middle = psdFrequenciesAndAmplitudes[0].length/2;
//	    if (psdFrequenciesAndAmplitudes[0].length%2 == 1) {
//	    	medianPSDFreq = psdFrequenciesAndAmplitudes[0][middle];
//	    } else {
//	    	medianPSDFreq = (psdFrequenciesAndAmplitudes[0][middle-1] + psdFrequenciesAndAmplitudes[0][middle]) / 2.0;
//	    }
	}
	
	public double getMedianPSDFrequency(){
		return medianPSDFreq;
	}
	
	private void setMaxPSDFrequency(){
		
		double[] frequency = psdFrequenciesAndAmplitudes[0];
		double[] amplitude = psdFrequenciesAndAmplitudes[1];
		
		double maxAmplitude = Double.NEGATIVE_INFINITY;
		int indexOfMaxAmplitude = 0;
		
		for(int index=0; index<amplitude.length; index++){
			if(amplitude[index] > maxAmplitude){
				maxAmplitude = amplitude[index];
				indexOfMaxAmplitude = index;
			}
		}
		
		maxPSDFrequency = frequency[indexOfMaxAmplitude];
	}
	
	public double getMaxPSDFrequency(){
		return maxPSDFrequency; 
	}
	
	public double[][] getPSDFrequenciesAndAmplitudes(){
		return psdFrequenciesAndAmplitudes; 
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
