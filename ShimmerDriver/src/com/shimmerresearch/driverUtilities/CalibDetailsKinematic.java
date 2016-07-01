package com.shimmerresearch.driverUtilities;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

/**
 * Class that holds the calibration parameters for a particular range in a
 * Kinematic sensor
 * 
 * @author Mark Nolan
 *
 */
public class CalibDetailsKinematic implements Serializable {
	
	/** * */
	private static final long serialVersionUID = -3556098650349506733L;
	
	public double[][] mCurrentAlignmentMatrix = null; 			
	public double[][] mCurrentSensitivityMatrix = null; 	
	public double[][] mCurrentOffsetVector = null;

	public double[][] mDefaultAlignmentMatrix =null;   			
	public double[][] mDefaultSensitivityMatrix = null;  	
	public double[][] mDefaultOffsetVector = null; 
	
	public String mRangeString = "";
	public int mRangeValue = 0;

	public long mCalibTime = 0;

	//Not Driver related - consider a different approach?
	public int guiRangeValue = 0;
	//Not Driver related - consider a different approach?
	public Integer[]guiRangeOptions = null;
	
	public CalibDetailsKinematic(int rangeValue, String rangeString) {
		this.mRangeValue = rangeValue;
		this.mRangeString = rangeString;
	}

	public CalibDetailsKinematic(int rangeValue, String rangeString, double[][] defaultAlignmentMatrix, double[][] defaultSensitivityMatrix, double[][] defaultOffsetVector) {
		this(rangeValue, rangeString);
		this.setDefaultValues(defaultAlignmentMatrix, defaultSensitivityMatrix, defaultOffsetVector);
	}

	public CalibDetailsKinematic(int rangeValue, String rangeString, 
			double[][] defaultAlignmentMatrix, double[][] defaultSensitivityMatrix, double[][] defaultOffsetVector,
			double[][] currentAlignmentMatrix, double[][] currentSensitivityMatrix, double[][] currentOffsetVector) {
		this(rangeValue, rangeString, defaultAlignmentMatrix, defaultSensitivityMatrix, defaultOffsetVector);
		this.setCurrentValues(currentAlignmentMatrix, currentSensitivityMatrix, currentOffsetVector);
	}

	
	public void setCurrentValues(double[][] currentAlignmentMatrix, double[][] currentSensitivityMatrix, double[][] currentOffsetVector) {
		this.mCurrentAlignmentMatrix = currentAlignmentMatrix;
		this.mCurrentSensitivityMatrix = currentSensitivityMatrix;
		this.mCurrentOffsetVector = currentOffsetVector;
	}

	public void setDefaultValues(double[][] defaultAlignmentMatrix, double[][] defaultSensitivityMatrix, double[][] defaultOffsetVector) {
		this.mDefaultAlignmentMatrix = defaultAlignmentMatrix;
		this.mDefaultSensitivityMatrix = defaultSensitivityMatrix;
		this.mDefaultOffsetVector = defaultOffsetVector;
	}

	public void resetToDefaultParameters(){
		mCurrentAlignmentMatrix = UtilShimmer.deepCopyDoubleMatrix(mDefaultAlignmentMatrix);
		mCurrentSensitivityMatrix = UtilShimmer.deepCopyDoubleMatrix(mDefaultSensitivityMatrix);
		mCurrentOffsetVector = UtilShimmer.deepCopyDoubleMatrix(mDefaultOffsetVector);
	}
	
	public void setCalibTime(long calibTime){
		mCalibTime = calibTime;
	}

	public long getCalibTime(){
		return mCalibTime;
	}

	public String getCalibTimeParsed(){
		return UtilShimmer.convertMilliSecondsToDateString(mCalibTime);
	}
	
	public boolean isCurrentValuesSet(){
		if(mCurrentAlignmentMatrix!=null && mCurrentSensitivityMatrix!=null && mCurrentOffsetVector!=null){
			return true;
		}
		return false;
	}

	public boolean isAllUsingDefaultParameters(){
		if(isAlignmentUsingDefault() && isSensitivityUsingDefault() && isOffsetVectorUsingDefault()){
			return true;
		}
		return false;
	}
	
	public boolean isAlignmentUsingDefault(){
		if(mCurrentAlignmentMatrix==null || mDefaultAlignmentMatrix==null){
			return false;
		}
		return Arrays.deepEquals(mCurrentAlignmentMatrix, mDefaultAlignmentMatrix);
	}

	public boolean isSensitivityUsingDefault(){
		if(mCurrentSensitivityMatrix==null || mDefaultSensitivityMatrix==null){
			return false;
		}
		return Arrays.deepEquals(mCurrentSensitivityMatrix, mDefaultSensitivityMatrix);
	}

	public boolean isOffsetVectorUsingDefault(){
		if(mCurrentOffsetVector==null || mDefaultOffsetVector==null){
			return false;
		}
		return Arrays.deepEquals(mCurrentOffsetVector, mDefaultOffsetVector);
	}

	public boolean isAllCalibrationValid(){
		if((isAlignmentValid() && isSensitivityValid() && isOffsetVectorValid()) || isAllUsingDefaultParameters()){
			return true;
		}
		return false;
	}
	
	public boolean isAlignmentValid(){
	 if(UtilShimmer.isAllZeros(mCurrentAlignmentMatrix)){
			return false;
		}else{
			return true;
		}
	}

	public boolean isSensitivityValid(){
	 if(isDiagonalFilled(mCurrentSensitivityMatrix) ){
			return true;
		}else{
			return false;
		}
	}
	
	//TODO : look up is there a valid check for a valid offset vector 
	public boolean isOffsetVectorValid(){
			return true;
	}
	
	public boolean isDiagonalFilled(double[][] matrix){
		boolean diagonalFilled = true;
		for(int j = 0; j < matrix[1].length; j++){
			if(matrix[0][j]==0 &&  matrix[1][j]==0 && matrix[2][j]==0){
				diagonalFilled =false;
			}
		}
		return diagonalFilled;
	}

	public String generateDebugString() {
		String debugString = "RangeString:" + mRangeString + "\t" + "RangeValue:" + mRangeValue + "\n";
		debugString += generateDebugStringPerProperty("Default Alignment", mDefaultAlignmentMatrix);
		debugString += generateDebugStringPerProperty("Current Alignment", mCurrentAlignmentMatrix);
		debugString += generateDebugStringPerProperty("Default Sensitivity", mDefaultSensitivityMatrix);
		debugString += generateDebugStringPerProperty("CurrentSensitivity", mCurrentSensitivityMatrix);
		debugString += generateDebugStringPerProperty("Default Offset Vector", mDefaultOffsetVector);
		debugString += generateDebugStringPerProperty("Current Offset Vector", mCurrentOffsetVector);
		return debugString;
	}

	private String generateDebugStringPerProperty(String property, double[][] calMatrix) {
		String debugString = property + " =\n";
		if(calMatrix==null){
			debugString += "NULL\n";
		}
		else{
			debugString += UtilShimmer.doubleArrayToString(calMatrix);
		}
		return debugString;
	}

	public void parseCalParamByteArray(byte[] bufferCalibrationParameters){
		if(bufferCalibrationParameters.length>21){
			//TODO pick off 8 byte timestamp and parse
		}
		
		String[] dataType={"i16","i16","i16","i16","i16","i16","i8","i8","i8","i8","i8","i8","i8","i8","i8"};
		int[] formattedPacket = UtilParseData.formatDataPacketReverse(bufferCalibrationParameters,dataType);
		double[] AM=new double[9];
		for (int i=0;i<9;i++) {
			AM[i]=((double)formattedPacket[6+i])/100;
		}
		double[][] alignmentMatrixMPLMag = {{AM[0],AM[1],AM[2]},{AM[3],AM[4],AM[5]},{AM[6],AM[7],AM[8]}}; 				
		double[][] sensitivityMatrixMPLMag = {{formattedPacket[3],0,0},{0,formattedPacket[4],0},{0,0,formattedPacket[5]}}; 
		double[][] offsetVectorMPLMag = {{formattedPacket[0]},{formattedPacket[1]},{formattedPacket[2]}};
		mCurrentAlignmentMatrix = alignmentMatrixMPLMag; 			
		mCurrentSensitivityMatrix = sensitivityMatrixMPLMag; 	
		mCurrentOffsetVector = offsetVectorMPLMag;
	}

	public byte[] generateCalParamByteArray() {
		if(isCurrentValuesSet()){
			return generateCalParamByteArray(mCurrentOffsetVector, mCurrentSensitivityMatrix, mCurrentAlignmentMatrix);
		}
		else{
			return generateCalParamByteArray(mDefaultOffsetVector, mDefaultSensitivityMatrix, mDefaultAlignmentMatrix);
		}
	}
	
	public byte[] generateCalParamByteArrayWithTimestamp() {
		byte[] range = ByteBuffer.allocate(1).putLong(mRangeValue).array();
		byte[] timestamp = ByteBuffer.allocate(8).putLong(mCalibTime).array();
		byte[] bufferCalibParam = generateCalParamByteArray();
		
		byte[] bufferCalibParamWithTimestamp = new byte[range.length + bufferCalibParam.length + timestamp.length];
		System.arraycopy(range, 0, bufferCalibParamWithTimestamp, 0, range.length);
		System.arraycopy(timestamp, 0, bufferCalibParamWithTimestamp, range.length, timestamp.length);
		System.arraycopy(bufferCalibParam, 0, bufferCalibParamWithTimestamp, range.length + timestamp.length, bufferCalibParam.length);
		return bufferCalibParamWithTimestamp;
	}

	public static byte[] generateCalParamByteArray(double[][] offsetVector, double[][] sensitivityMatrix, double[][] alignmentMatrix) {
		byte[] bufferCalibParam = new byte[21];
		// offsetVector -> buffer offset = 0
		for (int i=0; i<3; i++) {
			bufferCalibParam[0+(i*2)] = (byte) ((((int)offsetVector[i][0]) >> 8) & 0xFF);
			bufferCalibParam[0+(i*2)+1] = (byte) ((((int)offsetVector[i][0]) >> 0) & 0xFF);
		}
		// sensitivityMatrix -> buffer offset = 6
		for (int i=0; i<3; i++) {
			bufferCalibParam[6+(i*2)] = (byte) ((((int)sensitivityMatrix[i][i]) >> 8) & 0xFF);
			bufferCalibParam[6+(i*2)+1] = (byte) ((((int)sensitivityMatrix[i][i]) >> 0) & 0xFF);
		}
		// alignmentMatrix -> buffer offset = 12
		for (int i=0; i<3; i++) {
			bufferCalibParam[12+(i*3)] = (byte) (((int)(alignmentMatrix[i][0]*100)) & 0xFF);
			bufferCalibParam[12+(i*3)+1] = (byte) (((int)(alignmentMatrix[i][1]*100)) & 0xFF);
			bufferCalibParam[12+(i*3)+2] = (byte) (((int)(alignmentMatrix[i][2]*100)) & 0xFF);
		}
		return bufferCalibParam;
	}	
}
